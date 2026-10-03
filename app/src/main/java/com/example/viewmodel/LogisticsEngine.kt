package com.example.viewmodel

import com.example.data.*
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object LogisticsEngine {

    /**
     * Process 1 simulation tick for the logistics business.
     * dtSeconds: time step in seconds (e.g. 0.5s or 1.0s).
     */
    fun processTick(
        data: LogisticsCompanyData,
        dtSeconds: Float = 0.5f
    ): LogisticsCompanyData {
        var currentCash = data.internalCash
        var currentPackages = data.currentWarehousePackages.toDouble()
        var successRate = data.successRate
        var totalDelivered = data.totalPackagesDelivered
        var totalRevenue = data.totalRevenueEarned
        var totalDemurrage = data.totalDemurragePaid

        // 1. Advance Event Timer
        var event = data.currentEvent
        val remainingEventSec = event.durationRemainingSeconds - dtSeconds.toInt()
        if (remainingEventSec <= 0) {
            event = LogisticsDataGenerator.generateRandomEvent()
        } else {
            event = event.copy(durationRemainingSeconds = max(1, remainingEventSec))
        }

        // 2. Inbound Package Flow from Active Contracts
        val updatedActiveContracts = mutableListOf<LogisticsContract>()
        val completedContracts = mutableListOf<LogisticsContract>()

        for (contract in data.activeContracts) {
            val flowRate = contract.inboundPackagesPerSec * event.inboundFlowMultiplier
            val inboundDelta = flowRate * dtSeconds
            currentPackages += inboundDelta

            val newRemainingSec = contract.remainingDurationSeconds - dtSeconds.toInt()
            val isTargetReached = contract.targetTotalPackages > 0 && contract.deliveredPackages >= contract.targetTotalPackages
            val isTimeExpired = newRemainingSec <= 0

            if (isTargetReached || isTimeExpired) {
                // Contract Completed!
                completedContracts.add(contract)
                val bonus = contract.completionBonusCash
                currentCash += bonus
                totalRevenue += bonus
                successRate = (successRate + 0.5).coerceAtMost(100.0)
            } else {
                updatedActiveContracts.add(
                    contract.copy(
                        remainingDurationSeconds = max(1, newRemainingSec)
                    )
                )
            }
        }

        // 3. Warehouse Overload & Demurrage Penalties
        val capacity = data.warehouseCapacity
        var lastOverloadTime = data.lastOverloadTimestamp

        if (currentPackages > capacity) {
            lastOverloadTime = System.currentTimeMillis()
            val excess = currentPackages - capacity
            val demurragePenalty = ((excess * 0.20 + 25.0) * dtSeconds).toLong()
            currentCash -= demurragePenalty
            totalDemurrage += demurragePenalty
            successRate = (successRate - (0.35 * dtSeconds)).coerceAtLeast(50.0)
        } else {
            // Gradual success rate recovery
            successRate = (successRate + (0.04 * dtSeconds)).coerceAtMost(100.0)
        }

        // 4. Fleet Movement & Trip Progress
        var updatedFleet = data.fleet.map { vehicle ->
            if (vehicle.status == FleetVehicleStatus.EN_ROUTE) {
                val speedMult = getVehicleSpeedMultiplier(vehicle.type, event, data.techTree)
                val newElapsed = vehicle.currentTripElapsedSeconds + (dtSeconds * speedMult)
                val progress = (newElapsed / vehicle.currentTripTotalSeconds.coerceAtLeast(1f)).coerceIn(0f, 1f)

                if (progress >= 1f) {
                    // Vehicle Finished Trip!
                    val rate = if (vehicle.tripRatePerPackage > 0) vehicle.tripRatePerPackage else 6.0
                    val bonusFactor = if (successRate >= 95.0) 1.10 else 1.00
                    val grossTripRev = (vehicle.carryingPackages * rate * bonusFactor * event.bonusPayoutMultiplier).toLong()
                    val tripCost = calculateVehicleTripCost(vehicle.type, data.techTree)
                    val netProfit = grossTripRev - tripCost

                    currentCash += netProfit
                    totalRevenue += grossTripRev
                    totalDelivered += vehicle.carryingPackages

                    // Wear calculation
                    val baseWear = getVehicleBaseWear(vehicle.type)
                    val wearMultiplier = event.conditionWearMultiplier * if (data.techTree.ecoLevel >= 2) 0.80f else 1.0f
                    val actualWear = (baseWear * wearMultiplier).coerceAtLeast(1.0)
                    val newHp = (vehicle.conditionHp - actualWear).coerceAtLeast(0.0)

                    // Update corresponding active contract delivered packages
                    if (vehicle.assignedContractName.isNotBlank()) {
                        for (i in updatedActiveContracts.indices) {
                            if (updatedActiveContracts[i].clientName == vehicle.assignedContractName) {
                                val c = updatedActiveContracts[i]
                                updatedActiveContracts[i] = c.copy(
                                    deliveredPackages = c.deliveredPackages + vehicle.carryingPackages
                                )
                                break
                            }
                        }
                    }

                    if (newHp <= 0.0) {
                        // Breakdown!
                        successRate = (successRate - 1.5).coerceAtLeast(50.0)
                        vehicle.copy(
                            conditionHp = 0.0,
                            status = FleetVehicleStatus.BROKEN,
                            currentTripProgress = 0f,
                            currentTripElapsedSeconds = 0f,
                            carryingPackages = 0,
                            totalTripsCompleted = vehicle.totalTripsCompleted + 1,
                            totalPackagesDelivered = vehicle.totalPackagesDelivered + vehicle.carryingPackages
                        )
                    } else {
                        successRate = (successRate + 0.08).coerceAtMost(100.0)
                        vehicle.copy(
                            conditionHp = newHp,
                            status = FleetVehicleStatus.IDLE,
                            currentTripProgress = 0f,
                            currentTripElapsedSeconds = 0f,
                            carryingPackages = 0,
                            totalTripsCompleted = vehicle.totalTripsCompleted + 1,
                            totalPackagesDelivered = vehicle.totalPackagesDelivered + vehicle.carryingPackages
                        )
                    }
                } else {
                    vehicle.copy(
                        currentTripProgress = progress,
                        currentTripElapsedSeconds = newElapsed
                    )
                }
            } else vehicle
        }

        // 5. Auto-Dispatch AI (if unlocked & enabled & warehouse has packages)
        if (data.autoDispatchEnabled && data.techTree.aiSpeedLevel >= 2 && currentPackages > 0) {
            val idleVehicles = updatedFleet.filter { it.status == FleetVehicleStatus.IDLE && it.conditionHp > 20.0 }
            if (idleVehicles.isNotEmpty()) {
                val newFleetList = updatedFleet.toMutableList()
                for (idleVehicle in idleVehicles) {
                    if (currentPackages <= 0) break
                    val takePackages = min(idleVehicle.type.capacity, currentPackages.toInt())
                    if (takePackages > 0) {
                        currentPackages -= takePackages
                        val activeContract = updatedActiveContracts.firstOrNull()
                        val rate = activeContract?.payoutPerPackage ?: 5.50
                        val contractName = activeContract?.clientName ?: "General Dispatch"

                        val index = newFleetList.indexOfFirst { it.id == idleVehicle.id }
                        if (index != -1) {
                            newFleetList[index] = idleVehicle.copy(
                                status = FleetVehicleStatus.EN_ROUTE,
                                carryingPackages = takePackages,
                                tripRatePerPackage = rate,
                                assignedContractName = contractName,
                                currentTripProgress = 0f,
                                currentTripElapsedSeconds = 0f,
                                currentTripTotalSeconds = idleVehicle.type.baseTripSeconds
                            )
                        }
                    }
                }
                updatedFleet = newFleetList
            }
        }

        // 6. Ensure Available Contracts Pool is stocked
        var availableList = data.availableContracts
        if (availableList.size < 3) {
            val newList = availableList.toMutableList()
            while (newList.size < 3) {
                newList.add(LogisticsDataGenerator.generateNewContract(data.warehouseLevel))
            }
            availableList = newList
        }

        return data.copy(
            internalCash = currentCash,
            currentWarehousePackages = max(0.0, currentPackages).toLong(),
            successRate = (Math.round(successRate * 10.0) / 10.0),
            totalPackagesDelivered = totalDelivered,
            totalRevenueEarned = totalRevenue,
            totalDemurragePaid = totalDemurrage,
            fleet = updatedFleet,
            activeContracts = updatedActiveContracts,
            availableContracts = availableList,
            currentEvent = event,
            lastOverloadTimestamp = lastOverloadTime
        )
    }

    fun deployVehicle(
        data: LogisticsCompanyData,
        vehicleId: String
    ): LogisticsCompanyData {
        val vehicle = data.fleet.find { it.id == vehicleId } ?: return data
        if (vehicle.status != FleetVehicleStatus.IDLE || vehicle.conditionHp <= 0.0) return data
        if (data.currentWarehousePackages <= 0) return data

        val packagesToCarry = min(vehicle.type.capacity, data.currentWarehousePackages.toInt())
        if (packagesToCarry <= 0) return data

        val activeContract = data.activeContracts.firstOrNull()
        val rate = activeContract?.payoutPerPackage ?: 5.50
        val contractName = activeContract?.clientName ?: "Pengiriman Ekspres"

        val updatedFleet = data.fleet.map {
            if (it.id == vehicleId) {
                it.copy(
                    status = FleetVehicleStatus.EN_ROUTE,
                    carryingPackages = packagesToCarry,
                    tripRatePerPackage = rate,
                    assignedContractName = contractName,
                    currentTripProgress = 0f,
                    currentTripElapsedSeconds = 0f,
                    currentTripTotalSeconds = it.type.baseTripSeconds
                )
            } else it
        }

        return data.copy(
            currentWarehousePackages = max(0L, data.currentWarehousePackages - packagesToCarry),
            fleet = updatedFleet
        )
    }

    fun deployAllIdle(data: LogisticsCompanyData): LogisticsCompanyData {
        var currentPackages = data.currentWarehousePackages
        if (currentPackages <= 0) return data

        val activeContract = data.activeContracts.firstOrNull()
        val rate = activeContract?.payoutPerPackage ?: 5.50
        val contractName = activeContract?.clientName ?: "Pengiriman Ekspres"

        val updatedFleet = data.fleet.map { vehicle ->
            if (vehicle.status == FleetVehicleStatus.IDLE && vehicle.conditionHp > 0.0 && currentPackages > 0) {
                val toCarry = min(vehicle.type.capacity, currentPackages.toInt())
                currentPackages = max(0L, currentPackages - toCarry)
                vehicle.copy(
                    status = FleetVehicleStatus.EN_ROUTE,
                    carryingPackages = toCarry,
                    tripRatePerPackage = rate,
                    assignedContractName = contractName,
                    currentTripProgress = 0f,
                    currentTripElapsedSeconds = 0f,
                    currentTripTotalSeconds = vehicle.type.baseTripSeconds
                )
            } else vehicle
        }

        return data.copy(
            currentWarehousePackages = currentPackages,
            fleet = updatedFleet
        )
    }

    fun repairVehicle(
        data: LogisticsCompanyData,
        vehicleId: String
    ): LogisticsCompanyData {
        val vehicle = data.fleet.find { it.id == vehicleId } ?: return data
        val repairCost = calculateRepairCost(vehicle)
        if (data.internalCash < repairCost) return data

        val updatedFleet = data.fleet.map {
            if (it.id == vehicleId) {
                it.copy(
                    conditionHp = 100.0,
                    status = FleetVehicleStatus.IDLE
                )
            } else it
        }

        return data.copy(
            internalCash = data.internalCash - repairCost,
            fleet = updatedFleet
        )
    }

    fun repairAllVehicles(data: LogisticsCompanyData): LogisticsCompanyData {
        var totalCost = 0L
        val needsRepair = data.fleet.filter { it.conditionHp < 100.0 || it.status == FleetVehicleStatus.BROKEN }
        if (needsRepair.isEmpty()) return data

        for (v in needsRepair) {
            totalCost += calculateRepairCost(v)
        }

        if (data.internalCash < totalCost) return data

        val updatedFleet = data.fleet.map {
            if (it.conditionHp < 100.0 || it.status == FleetVehicleStatus.BROKEN) {
                it.copy(
                    conditionHp = 100.0,
                    status = FleetVehicleStatus.IDLE
                )
            } else it
        }

        return data.copy(
            internalCash = data.internalCash - totalCost,
            fleet = updatedFleet
        )
    }

    fun buyVehicle(
        data: LogisticsCompanyData,
        type: LogisticsVehicleType,
        customName: String? = null
    ): LogisticsCompanyData {
        val cost = type.buyCost
        if (data.internalCash < cost) return data

        // Check tech requirement
        if (type.requiredTechPath == "ECO_3" && data.techTree.ecoLevel < 3) return data
        if (type.requiredTechPath == "AI_3" && data.techTree.aiSpeedLevel < 3) return data

        val vehicleCount = data.fleet.count { it.type == type } + 1
        val finalName = customName?.takeIf { it.isNotBlank() } ?: "${type.displayName} #0$vehicleCount"

        val newVehicle = FleetVehicle(
            id = UUID.randomUUID().toString(),
            name = finalName,
            type = type,
            conditionHp = 100.0,
            status = FleetVehicleStatus.IDLE
        )

        return data.copy(
            internalCash = data.internalCash - cost,
            fleet = data.fleet + newVehicle
        )
    }

    fun upgradeWarehouseCapacity(data: LogisticsCompanyData): LogisticsCompanyData {
        if (data.warehouseLevel >= 10) return data
        val cost = getWarehouseUpgradeCost(data.warehouseLevel)
        if (data.internalCash < cost) return data

        return data.copy(
            internalCash = data.internalCash - cost,
            warehouseLevel = data.warehouseLevel + 1
        )
    }

    fun signContract(
        data: LogisticsCompanyData,
        contractId: String
    ): LogisticsCompanyData {
        val contract = data.availableContracts.find { it.id == contractId } ?: return data
        if (data.activeContracts.size >= 3) return data

        val updatedAvailable = data.availableContracts.filter { it.id != contractId }
        val updatedActive = data.activeContracts + contract.copy(isSigned = true)

        return data.copy(
            availableContracts = updatedAvailable,
            activeContracts = updatedActive
        )
    }

    fun cancelContract(
        data: LogisticsCompanyData,
        contractId: String
    ): LogisticsCompanyData {
        val updatedActive = data.activeContracts.filter { it.id != contractId }
        return data.copy(activeContracts = updatedActive)
    }

    fun researchTech(
        data: LogisticsCompanyData,
        path: String // "ECO" or "AI"
    ): LogisticsCompanyData {
        if (path == "ECO") {
            val currentLvl = data.techTree.ecoLevel
            if (currentLvl >= 3) return data
            val cost = when (currentLvl) {
                0 -> 25_000L
                1 -> 75_000L
                2 -> 200_000L
                else -> 500_000L
            }
            if (data.internalCash < cost) return data
            return data.copy(
                internalCash = data.internalCash - cost,
                techTree = data.techTree.copy(ecoLevel = currentLvl + 1)
            )
        } else if (path == "AI") {
            val currentLvl = data.techTree.aiSpeedLevel
            if (currentLvl >= 3) return data
            val cost = when (currentLvl) {
                0 -> 35_000L
                1 -> 100_000L
                2 -> 250_000L
                else -> 600_000L
            }
            if (data.internalCash < cost) return data
            return data.copy(
                internalCash = data.internalCash - cost,
                techTree = data.techTree.copy(aiSpeedLevel = currentLvl + 1),
                autoDispatchEnabled = if (currentLvl + 1 >= 2) true else data.autoDispatchEnabled
            )
        }
        return data
    }

    fun toggleAutoDispatch(data: LogisticsCompanyData): LogisticsCompanyData {
        if (data.techTree.aiSpeedLevel < 2) return data
        return data.copy(autoDispatchEnabled = !data.autoDispatchEnabled)
    }

    fun calculateRepairCost(vehicle: FleetVehicle): Long {
        val missingHp = (100.0 - vehicle.conditionHp).coerceAtLeast(0.0)
        val baseFactor = when (vehicle.type) {
            LogisticsVehicleType.MOTOR_COURIER -> 2.5
            LogisticsVehicleType.BOX_VAN -> 12.0
            LogisticsVehicleType.HEAVY_TRUCK -> 50.0
            LogisticsVehicleType.CARGO_DRONE -> 30.0
            LogisticsVehicleType.ELECTRIC_TRUCK -> 65.0
        }
        val emergencyFee = if (vehicle.status == FleetVehicleStatus.BROKEN) 250L else 0L
        return (missingHp * baseFactor).toLong() + emergencyFee
    }

    fun calculateVehicleTripCost(type: LogisticsVehicleType, techTree: LogisticsTechTree): Long {
        var base = type.tripCost
        if (techTree.ecoLevel >= 1) {
            base = (base * 0.85).toLong()
        }
        if (techTree.ecoLevel >= 2) {
            base = (base * 0.70).toLong()
        }
        if (techTree.ecoLevel >= 3 && type == LogisticsVehicleType.ELECTRIC_TRUCK) {
            base = (base * 0.50).toLong()
        }
        return max(1L, base)
    }

    private fun getVehicleSpeedMultiplier(
        type: LogisticsVehicleType,
        event: LogisticsWeatherTrafficEvent,
        techTree: LogisticsTechTree
    ): Float {
        var eventSpeed = when (type) {
            LogisticsVehicleType.MOTOR_COURIER -> event.speedMultiplierMotor
            LogisticsVehicleType.BOX_VAN -> event.speedMultiplierVan
            LogisticsVehicleType.HEAVY_TRUCK -> event.speedMultiplierTruck
            LogisticsVehicleType.CARGO_DRONE -> event.speedMultiplierDrone
            LogisticsVehicleType.ELECTRIC_TRUCK -> event.speedMultiplierTruck
        }
        if (techTree.aiSpeedLevel >= 1) {
            eventSpeed *= 1.20f // +20% GPS AI speed boost
        }
        return eventSpeed
    }

    private fun getVehicleBaseWear(type: LogisticsVehicleType): Double {
        return when (type) {
            LogisticsVehicleType.MOTOR_COURIER -> (3.5..5.5).randomDouble()
            LogisticsVehicleType.BOX_VAN -> (2.5..4.0).randomDouble()
            LogisticsVehicleType.HEAVY_TRUCK -> (2.0..3.5).randomDouble()
            LogisticsVehicleType.CARGO_DRONE -> (3.0..5.0).randomDouble()
            LogisticsVehicleType.ELECTRIC_TRUCK -> (1.5..2.8).randomDouble()
        }
    }

    private fun ClosedRange<Double>.randomDouble(): Double {
        return start + (endInclusive - start) * Random.nextDouble()
    }

    data class LogisticsValuationBreakdown(
        val internalCash: Long,
        val fleetValue: Long,
        val warehouseTechValue: Long,
        val performanceMultiplier: Double,
        val totalValuation: Long
    )

    fun calculateLiquidationValuation(data: LogisticsCompanyData): LogisticsValuationBreakdown {
        val cash = data.internalCash

        // Armada: Harga beli * 80% * rasio kondisi HP
        val fleetVal = data.fleet.sumOf { v ->
            val conditionRatio = (v.conditionHp / 100.0).coerceIn(0.2, 1.0)
            (v.type.buyCost * 0.80 * conditionRatio).toLong()
        }

        // Level Gudang & Teknologi Riset
        val whVal = data.warehouseLevel * 45_000L
        val techVal = (data.techTree.ecoLevel * 30_000L) + (data.techTree.aiSpeedLevel * 40_000L)
        val warehouseTechVal = whVal + techVal

        // Performance / Goodwill Multiplier berdasarkan Success Rate (0% - 100%)
        val perfMult = when {
            data.successRate >= 98.0 -> 1.30
            data.successRate >= 95.0 -> 1.15
            data.successRate >= 85.0 -> 1.00
            data.successRate >= 70.0 -> 0.85
            else -> 0.70
        }

        val assetBase = fleetVal + warehouseTechVal
        val adjustedAssets = (assetBase * perfMult).toLong()
        val total = max(15_000L, cash + adjustedAssets)

        return LogisticsValuationBreakdown(
            internalCash = cash,
            fleetValue = fleetVal,
            warehouseTechValue = warehouseTechVal,
            performanceMultiplier = perfMult,
            totalValuation = total
        )
    }
}
