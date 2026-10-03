package com.example.aicloud.engine

import com.example.aicloud.data.AiCloudInitialData
import com.example.aicloud.model.*

object AiCloudEngine {

    fun processMonthlyTick(
        monthYear: String,
        dataCenters: List<AiDataCenter>,
        activeContracts: List<ClientContract>,
        marketContracts: List<ClientContract>,
        unlockedResearchIds: Set<String>
    ): SimulationTickOutput {
        val logSummaries = mutableListOf<String>()
        val incidentsToAdd = mutableListOf<AiCloudIncident>()

        // 1. Research modifiers
        val researchList = AiCloudInitialData.RESEARCH_CATALOG.filter { it.id in unlockedResearchIds }
        val totalPowerReductionPct = researchList.sumOf { it.powerReductionPct }.coerceAtMost(0.50)
        val totalRevenueBoostPct = researchList.sumOf { it.revenueBoostPct }
        val totalReliabilityBoostPct = researchList.sumOf { it.reliabilityBoostPct }
        val totalPueImprovement = researchList.sumOf { it.pueImprovement }

        // 2. Process Data Centers & Construction
        var totalOperationalComputeTflops = 0.0
        var totalElectricityExpenses = 0L
        var totalHardwareMaintenance = 0L
        var totalFacilityMaintenance = 0L
        var weightedPueSum = 0.0
        var operationalDcCount = 0

        val updatedDataCenters = dataCenters.map { dc ->
            if (dc.constructionMonthsRemaining > 0) {
                val rem = dc.constructionMonthsRemaining - 1
                if (rem == 0) {
                    logSummaries.add("🏗️ Construction Complete: ${dc.name} (${dc.region.displayName}) is now online!")
                    incidentsToAdd.add(
                        AiCloudIncident(
                            title = "Facility Online: ${dc.name}",
                            description = "High-voltage substation energized. Data Center is fully operational.",
                            severity = IncidentSeverity.INFO
                        )
                    )
                    dc.copy(constructionMonthsRemaining = 0, isOperational = true)
                } else {
                    dc.copy(constructionMonthsRemaining = rem)
                }
            } else {
                // Operational Facility Simulation
                operationalDcCount++

                // Hardware wear & tear (Realistic lifecycle: 36-60 months useful life)
                val wearRate = when (dc.coolingSystem) {
                    CoolingSystemType.GEOTHERMAL_ZERO_CARBON -> 0.15
                    CoolingSystemType.TWO_PHASE_IMMERSION -> 0.25
                    CoolingSystemType.DIRECT_TO_CHIP_LIQUID -> 0.40
                    CoolingSystemType.AIR_COOLED_STANDARD -> 0.65
                }

                val updatedRacks = dc.serverRacks.map { rack ->
                    val newCondition = (rack.healthConditionPct - wearRate).coerceAtLeast(15.0)
                    rack.copy(
                        healthConditionPct = newCondition,
                        ageMonths = rack.ageMonths + 1
                    )
                }

                // Power & Overload Check
                val rawComputeKw = updatedRacks.sumOf { it.totalPowerKw } * (1.0 - totalPowerReductionPct)
                val effectivePue = (dc.effectivePue - totalPueImprovement).coerceAtLeast(1.03)
                val facilityMw = (rawComputeKw * effectivePue) / 1000.0
                val capacityMw = dc.effectivePowerCapacityMw
                val isOverloaded = facilityMw > capacityMw && capacityMw > 0

                val facilityUptime = if (isOverloaded) {
                    93.50 + (Math.random() * 3.0)
                } else {
                    (dc.tier.uptimeSla + totalReliabilityBoostPct).coerceAtMost(99.999)
                }

                if (isOverloaded) {
                    logSummaries.add("⚠️ POWER OVERLOAD at ${dc.name}! Demand (${String.format("%.1f", facilityMw)} MW) exceeds capacity (${String.format("%.1f", capacityMw)} MW). Brownouts active!")
                    incidentsToAdd.add(
                        AiCloudIncident(
                            title = "Grid Capacity Exceeded: ${dc.name}",
                            description = "Thermal throttling activated. Brownout caused ${String.format("%.2f", 100.0 - facilityUptime)}% cluster downtime.",
                            severity = IncidentSeverity.CRITICAL
                        )
                    )
                }

                // Compute electricity cost (730 hours in a month)
                val kwhUsed = facilityMw * 1000.0 * 730.0
                val effectiveKwhRate = dc.region.powerCostPerKwhUsd * (1.0 - dc.powerSource.costDiscountPct - dc.region.greenEnergyRebatePct).coerceAtLeast(0.02)
                val dcPowerCost = (kwhUsed * effectiveKwhRate).toLong()
                val dcHwMaint = updatedRacks.sumOf { it.totalMonthlyMaintenance }
                val dcFacMaint = dc.baseFacilityMaintenanceUsd

                totalElectricityExpenses += dcPowerCost
                totalHardwareMaintenance += dcHwMaint
                totalFacilityMaintenance += dcFacMaint

                weightedPueSum += effectivePue
                totalOperationalComputeTflops += if (isOverloaded) updatedRacks.sumOf { it.totalTflops } * 0.6 else updatedRacks.sumOf { it.totalTflops }

                dc.copy(
                    serverRacks = updatedRacks,
                    isOverloaded = isOverloaded,
                    facilityUptimeLastMonth = facilityUptime
                )
            }
        }

        val averagePue = if (operationalDcCount > 0) weightedPueSum / operationalDcCount else 1.25

        // 3. Process Active Enterprise Contracts
        var totalContractRevenue = 0L
        var totalOutagePenalties = 0L
        var committedTflops = 0.0
        val updatedActiveContracts = mutableListOf<ClientContract>()

        activeContracts.forEach { contract ->
            committedTflops += contract.requiredTflops
            val hasComputeCapacity = totalOperationalComputeTflops >= committedTflops
            val remMonths = contract.monthsRemaining - 1

            if (hasComputeCapacity) {
                val revenue = (contract.monthlyPaymentUsd * (1.0 + totalRevenueBoostPct)).toLong()
                totalContractRevenue += revenue
                if (remMonths > 0) {
                    updatedActiveContracts.add(contract.copy(monthsRemaining = remMonths, isSatisfied = true))
                } else {
                    logSummaries.add("✅ Contract Finished: ${contract.clientName} successfully completed ${contract.workloadType.displayName} term.")
                    incidentsToAdd.add(
                        AiCloudIncident(
                            title = "Contract Completed: ${contract.clientName}",
                            description = "Client completed full term with pristine SLA metrics.",
                            severity = IncidentSeverity.INFO
                        )
                    )
                }
            } else {
                // Compute shortage or brownout penalty
                val penalty = contract.outagePenaltyUsd
                totalOutagePenalties += penalty
                logSummaries.add("🚨 SLA Breach: ${contract.clientName} experienced outage! Incurred $$penalty USD penalty.")
                incidentsToAdd.add(
                    AiCloudIncident(
                        title = "SLA Violation: ${contract.clientName}",
                        description = "Compute shortage prevented full workload delivery.",
                        severity = IncidentSeverity.WARNING,
                        financialImpactUsd = penalty
                    )
                )
                if (remMonths > 0) {
                    updatedActiveContracts.add(contract.copy(monthsRemaining = remMonths, isSatisfied = false))
                }
            }
        }

        // 4. Spot Market AI Inference (surplus compute)
        val freeTflops = (totalOperationalComputeTflops - committedTflops).coerceAtLeast(0.0)
        val spotMarketRevenue = if (freeTflops > 0.0) {
            // ~$22 USD per TFLOPS-month on open spot broker
            ((freeTflops * 22.0) * (1.0 + totalRevenueBoostPct)).toLong()
        } else 0L

        if (spotMarketRevenue > 0) {
            logSummaries.add("⚡ Spot Market: Sold ${String.format("%,.0f", freeTflops)} surplus TFLOPS for $${String.format("%,d", spotMarketRevenue)} USD.")
        }

        // 5. Total Financials
        val grossRevenue = totalContractRevenue + spotMarketRevenue
        val totalExpenses = totalElectricityExpenses + totalHardwareMaintenance + totalFacilityMaintenance + totalOutagePenalties
        val netProfit = grossRevenue - totalExpenses

        // 6. Market Contracts Generation & Refresh
        val refreshedMarket = marketContracts.filter { Math.random() < 0.85 }.toMutableList()
        val existingNames = (activeContracts.map { it.clientName } + refreshedMarket.map { it.clientName }).toSet()
        if (refreshedMarket.size < 4) {
            refreshedMarket.add(AiCloudInitialData.generateRandomContract(existingNames))
        }

        val report = AiCloudMonthlyReport(
            monthYear = monthYear,
            enterpriseContractRevenue = totalContractRevenue,
            spotMarketRevenue = spotMarketRevenue,
            totalRevenue = grossRevenue,
            powerElectricityCost = totalElectricityExpenses,
            hardwareMaintenanceCost = totalHardwareMaintenance,
            facilityOperationalCost = totalFacilityMaintenance,
            outagePenalties = totalOutagePenalties,
            totalExpenses = totalExpenses,
            netProfit = netProfit,
            totalTflopsDelivered = totalOperationalComputeTflops,
            averagePue = averagePue,
            logSummaries = logSummaries
        )

        return SimulationTickOutput(
            updatedDataCenters = updatedDataCenters,
            updatedActiveContracts = updatedActiveContracts,
            updatedMarketContracts = refreshedMarket,
            report = report,
            incidents = incidentsToAdd,
            grossRevenue = grossRevenue,
            totalExpenses = totalExpenses,
            netProfit = netProfit
        )
    }
}

data class SimulationTickOutput(
    val updatedDataCenters: List<AiDataCenter>,
    val updatedActiveContracts: List<ClientContract>,
    val updatedMarketContracts: List<ClientContract>,
    val report: AiCloudMonthlyReport,
    val incidents: List<AiCloudIncident>,
    val grossRevenue: Long,
    val totalExpenses: Long,
    val netProfit: Long
)
