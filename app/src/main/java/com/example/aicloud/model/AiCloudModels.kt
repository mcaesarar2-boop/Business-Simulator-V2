package com.example.aicloud.model

import java.util.UUID

/**
 * Global regions where AI Data Centers can be constructed.
 */
enum class CloudRegion(
    val id: String,
    val displayName: String,
    val country: String,
    val powerCostPerKwhUsd: Double,
    val latencyMs: Int,
    val greenEnergyRebatePct: Double,
    val baseLandCostUsd: Long,
    val marketDemandFactor: Double,
    val climateCoolingBonusPue: Double
) {
    US_EAST_VIRGINIA(
        id = "us_east_va",
        displayName = "US East (Northern Virginia)",
        country = "United States",
        powerCostPerKwhUsd = 0.08,
        latencyMs = 12,
        greenEnergyRebatePct = 0.05,
        baseLandCostUsd = 3_500_000L,
        marketDemandFactor = 1.35,
        climateCoolingBonusPue = 0.0
    ),
    US_WEST_OREGON(
        id = "us_west_or",
        displayName = "US West (Oregon)",
        country = "United States",
        powerCostPerKwhUsd = 0.06,
        latencyMs = 28,
        greenEnergyRebatePct = 0.15,
        baseLandCostUsd = 2_800_000L,
        marketDemandFactor = 1.20,
        climateCoolingBonusPue = -0.04
    ),
    EU_FRANKFURT(
        id = "eu_central_fra",
        displayName = "EU Central (Frankfurt)",
        country = "Germany",
        powerCostPerKwhUsd = 0.16,
        latencyMs = 18,
        greenEnergyRebatePct = 0.10,
        baseLandCostUsd = 4_200_000L,
        marketDemandFactor = 1.25,
        climateCoolingBonusPue = -0.02
    ),
    ASIA_TOKYO(
        id = "asia_ne_tokyo",
        displayName = "Asia Pacific (Tokyo)",
        country = "Japan",
        powerCostPerKwhUsd = 0.14,
        latencyMs = 22,
        greenEnergyRebatePct = 0.08,
        baseLandCostUsd = 5_000_000L,
        marketDemandFactor = 1.30,
        climateCoolingBonusPue = 0.0
    ),
    ASIA_SINGAPORE(
        id = "asia_se_sg",
        displayName = "Asia Southeast (Singapore)",
        country = "Singapore",
        powerCostPerKwhUsd = 0.18,
        latencyMs = 15,
        greenEnergyRebatePct = 0.02,
        baseLandCostUsd = 6_500_000L,
        marketDemandFactor = 1.40,
        climateCoolingBonusPue = 0.08
    ),
    ASIA_JAKARTA(
        id = "asia_se_jkt",
        displayName = "Asia Southeast (Jakarta)",
        country = "Indonesia",
        powerCostPerKwhUsd = 0.07,
        latencyMs = 35,
        greenEnergyRebatePct = 0.06,
        baseLandCostUsd = 1_800_000L,
        marketDemandFactor = 1.15,
        climateCoolingBonusPue = 0.05
    ),
    NORDIC_ICELAND(
        id = "eu_nordic_ice",
        displayName = "Nordic Green (Reykjavik)",
        country = "Iceland",
        powerCostPerKwhUsd = 0.038,
        latencyMs = 65,
        greenEnergyRebatePct = 0.30,
        baseLandCostUsd = 1_500_000L,
        marketDemandFactor = 1.05,
        climateCoolingBonusPue = -0.12
    )
}

/**
 * Data Center Tier Standard.
 */
enum class DataCenterTier(
    val displayName: String,
    val baseCapacityMw: Double,
    val maxRackUnits: Int,
    val constructionCostUsd: Long,
    val uptimeSla: Double,
    val buildMonths: Int
) {
    TIER_1_STANDARD(
        displayName = "Tier I Standard Facility",
        baseCapacityMw = 5.0,
        maxRackUnits = 20,
        constructionCostUsd = 4_000_000L,
        uptimeSla = 99.67,
        buildMonths = 3
    ),
    TIER_2_ENTERPRISE(
        displayName = "Tier II Enterprise Campus",
        baseCapacityMw = 15.0,
        maxRackUnits = 60,
        constructionCostUsd = 12_000_000L,
        uptimeSla = 99.75,
        buildMonths = 6
    ),
    TIER_3_HYPERSCALE(
        displayName = "Tier III Hyperscale Hub",
        baseCapacityMw = 40.0,
        maxRackUnits = 180,
        constructionCostUsd = 35_000_000L,
        uptimeSla = 99.98,
        buildMonths = 9
    ),
    TIER_4_QUANTUM_READY(
        displayName = "Tier IV Fault-Tolerant AI Gigafactory",
        baseCapacityMw = 100.0,
        maxRackUnits = 500,
        constructionCostUsd = 90_000_000L,
        uptimeSla = 99.995,
        buildMonths = 12
    )
}

/**
 * Cooling systems affecting PUE (Power Usage Effectiveness).
 */
enum class CoolingSystemType(
    val displayName: String,
    val basePue: Double,
    val upgradeCostUsd: Long,
    val monthlyMaintenanceUsd: Long,
    val reliabilityBoostPct: Double
) {
    AIR_COOLED_STANDARD(
        displayName = "Direct Air Economizer",
        basePue = 1.55,
        upgradeCostUsd = 0L,
        monthlyMaintenanceUsd = 15_000L,
        reliabilityBoostPct = 0.0
    ),
    DIRECT_TO_CHIP_LIQUID(
        displayName = "Direct-to-Chip Liquid Loops",
        basePue = 1.25,
        upgradeCostUsd = 1_800_000L,
        monthlyMaintenanceUsd = 35_000L,
        reliabilityBoostPct = 0.03
    ),
    TWO_PHASE_IMMERSION(
        displayName = "Two-Phase Immersion Tank Pods",
        basePue = 1.12,
        upgradeCostUsd = 4_500_000L,
        monthlyMaintenanceUsd = 60_000L,
        reliabilityBoostPct = 0.06
    ),
    GEOTHERMAL_ZERO_CARBON(
        displayName = "Geothermal Deep Loop Cryo",
        basePue = 1.06,
        upgradeCostUsd = 10_000_000L,
        monthlyMaintenanceUsd = 90_000L,
        reliabilityBoostPct = 0.09
    )
}

/**
 * Power source types affecting carbon credits and electricity rates.
 */
enum class PowerSourceType(
    val displayName: String,
    val costDiscountPct: Double,
    val greenBonusPct: Double,
    val capacityMwBonus: Double,
    val upgradeCostUsd: Long
) {
    MUNICIPAL_GRID(
        displayName = "Standard Municipal High-Voltage Grid",
        costDiscountPct = 0.0,
        greenBonusPct = 0.0,
        capacityMwBonus = 0.0,
        upgradeCostUsd = 0L
    ),
    SOLAR_BATTERY_HYBRID(
        displayName = "Industrial Solar & Megapack Storage",
        costDiscountPct = 0.12,
        greenBonusPct = 0.20,
        capacityMwBonus = 5.0,
        upgradeCostUsd = 2_500_000L
    ),
    OFFSHORE_WIND_TURBINE(
        displayName = "Dedicated Offshore Wind Array",
        costDiscountPct = 0.20,
        greenBonusPct = 0.35,
        capacityMwBonus = 15.0,
        upgradeCostUsd = 6_000_000L
    ),
    NUCLEAR_SMR_DEDICATED(
        displayName = "Small Modular Nuclear Reactor (SMR)",
        costDiscountPct = 0.35,
        greenBonusPct = 0.50,
        capacityMwBonus = 50.0,
        upgradeCostUsd = 25_000_000L
    )
}

/**
 * Server GPU chip tiers deployed in racks.
 */
enum class GpuChipTier(
    val displayName: String,
    val manufacturer: String,
    val tflopsFp16: Double,
    val powerDrawKwPerRack: Double, // 8 GPUs per rack unit
    val costPerRackUsd: Long,
    val monthlyMaintenanceUsd: Long,
    val computeCategory: String
) {
    BUDGET_L40S(
        displayName = "NVIDIA L40S Cluster",
        manufacturer = "NVIDIA",
        tflopsFp16 = 366.0,
        powerDrawKwPerRack = 3.2,
        costPerRackUsd = 55_000L,
        monthlyMaintenanceUsd = 1_200L,
        computeCategory = "Inference & Fine-Tuning"
    ),
    ENTERPRISE_A100_80G(
        displayName = "NVIDIA A100 80GB SXM4",
        manufacturer = "NVIDIA",
        tflopsFp16 = 624.0,
        powerDrawKwPerRack = 4.0,
        costPerRackUsd = 110_000L,
        monthlyMaintenanceUsd = 2_200L,
        computeCategory = "Mid-Scale Training"
    ),
    FLAGSHIP_H100_SXM5(
        displayName = "NVIDIA H100 SXM5 Tensor Core",
        manufacturer = "NVIDIA",
        tflopsFp16 = 2_000.0,
        powerDrawKwPerRack = 7.5,
        costPerRackUsd = 280_000L,
        monthlyMaintenanceUsd = 4_800L,
        computeCategory = "Frontier Foundation Models"
    ),
    NEXTGEN_B200_BLACKWELL(
        displayName = "NVIDIA B200 Blackwell NVL72",
        manufacturer = "NVIDIA",
        tflopsFp16 = 4_500.0,
        powerDrawKwPerRack = 12.0,
        costPerRackUsd = 520_000L,
        monthlyMaintenanceUsd = 8_500L,
        computeCategory = "Trillion-Parameter AGI Clusters"
    ),
    CUSTOM_ASIC_TPU_V5P(
        displayName = "Custom TPU v5p Pod Slices",
        manufacturer = "In-House Silicon",
        tflopsFp16 = 3_200.0,
        powerDrawKwPerRack = 5.8,
        costPerRackUsd = 360_000L,
        monthlyMaintenanceUsd = 5_200L,
        computeCategory = "High-Efficiency Distributed AI"
    )
}

enum class GpuLifecyclePhase(val label: String, val badgeColorHex: Long, val description: String) {
    PRIME("Prima (Baru)", 0xFF00E676, "Kondisi optimal, efisiensi komputasi puncak."),
    STABLE("Operasional Stabil", 0xFF00E5FF, "Performa stabil dan andal untuk komputasi enterprise."),
    MAINTENANCE_DUE("Perlu Servis Rutin", 0xFFFFB300, "Thermal paste mulai kering, servis rutin disarankan."),
    LEGACY("Siklus Usang (Legacy)", 0xFFFF5252, "Efisiensi menurun, disarankan tukar tambah ke GPU baru.")
}

/**
 * Server Rack unit deployed in a data center.
 */
data class ServerRackUnit(
    val id: String = UUID.randomUUID().toString(),
    val customName: String = "Rack Unit",
    val gpuTier: GpuChipTier = GpuChipTier.ENTERPRISE_A100_80G,
    val quantity: Int = 1,
    val healthConditionPct: Double = 100.0,
    val isOperational: Boolean = true,
    val installedMonthYear: String = "Jan 2026",
    val ageMonths: Int = 0
) {
    val totalTflops: Double
        get() = if (isOperational) gpuTier.tflopsFp16 * quantity * (healthConditionPct / 100.0) else 0.0

    val totalPowerKw: Double
        get() = if (isOperational) gpuTier.powerDrawKwPerRack * quantity else 0.0

    val totalMonthlyMaintenance: Long
        get() = gpuTier.monthlyMaintenanceUsd * quantity

    val lifecyclePhase: GpuLifecyclePhase
        get() = when {
            healthConditionPct >= 90.0 && ageMonths < 14 -> GpuLifecyclePhase.PRIME
            healthConditionPct >= 75.0 && ageMonths < 36 -> GpuLifecyclePhase.STABLE
            healthConditionPct >= 50.0 && ageMonths < 48 -> GpuLifecyclePhase.MAINTENANCE_DUE
            else -> GpuLifecyclePhase.LEGACY
        }

    val resaleValueUsd: Long
        get() {
            val originalCost = gpuTier.costPerRackUsd * quantity
            val ageFactor = (1.0 - (ageMonths / 60.0)).coerceIn(0.25, 0.90)
            val healthFactor = (healthConditionPct / 100.0).coerceIn(0.30, 1.0)
            return (originalCost * 0.65 * ageFactor * healthFactor).toLong().coerceAtLeast(1_500L * quantity)
        }

    val routineServiceCostUsd: Long
        get() = (1_200L * quantity).coerceAtLeast(800L)
}

/**
 * AI Data Center facility.
 */
data class AiDataCenter(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val region: CloudRegion,
    val tier: DataCenterTier,
    val coolingSystem: CoolingSystemType = CoolingSystemType.AIR_COOLED_STANDARD,
    val powerSource: PowerSourceType = PowerSourceType.MUNICIPAL_GRID,
    val serverRacks: List<ServerRackUnit> = emptyList(),
    val constructionMonthsRemaining: Int = 0,
    val isOperational: Boolean = true,
    val facilityUptimeLastMonth: Double = 99.99,
    val isOverloaded: Boolean = false,
    val builtTimestamp: Long = System.currentTimeMillis()
) {
    val totalRackUnitsUsed: Int
        get() = serverRacks.sumOf { it.quantity }

    val effectivePowerCapacityMw: Double
        get() = tier.baseCapacityMw + powerSource.capacityMwBonus

    val effectivePue: Double
        get() = (coolingSystem.basePue + region.climateCoolingBonusPue).coerceAtLeast(1.04)

    val currentRawComputeKw: Double
        get() = serverRacks.sumOf { it.totalPowerKw }

    val currentFacilityPowerMw: Double
        get() = (currentRawComputeKw * effectivePue) / 1000.0

    val powerUtilizationPct: Double
        get() = if (effectivePowerCapacityMw > 0) (currentFacilityPowerMw / effectivePowerCapacityMw) * 100.0 else 0.0

    val totalComputeTflops: Double
        get() = serverRacks.sumOf { it.totalTflops }

    val totalComputePflops: Double
        get() = totalComputeTflops / 1000.0

    val baseFacilityMaintenanceUsd: Long
        get() = coolingSystem.monthlyMaintenanceUsd + (tier.constructionCostUsd / 200)
}

/**
 * AI Workload types for client contracts.
 */
enum class AiWorkloadType(
    val displayName: String,
    val minTflopsRequired: Double,
    val baseRatePerTflopsUsd: Double,
    val penaltyMultiplier: Double
) {
    FRONTIER_LLM_TRAINING(
        displayName = "Frontier LLM Pre-Training",
        minTflopsRequired = 5_000.0,
        baseRatePerTflopsUsd = 18.0,
        penaltyMultiplier = 2.5
    ),
    ENTERPRISE_INFERENCE_API(
        displayName = "Global Enterprise API Inference",
        minTflopsRequired = 1_500.0,
        baseRatePerTflopsUsd = 22.0,
        penaltyMultiplier = 2.0
    ),
    AUTONOMOUS_VISION_SIMULATION(
        displayName = "Autonomous Vehicle Spatial AI",
        minTflopsRequired = 2_500.0,
        baseRatePerTflopsUsd = 20.0,
        penaltyMultiplier = 1.8
    ),
    BIOTECH_GENOMICS_RESEARCH(
        displayName = "Biotech Protein Folding Cluster",
        minTflopsRequired = 1_000.0,
        baseRatePerTflopsUsd = 16.0,
        penaltyMultiplier = 1.2
    ),
    INDIE_STARTUP_ACCELERATOR(
        displayName = "AI Startup Incubator Burst Pool",
        minTflopsRequired = 400.0,
        baseRatePerTflopsUsd = 24.0,
        penaltyMultiplier = 1.0
    )
}

/**
 * Enterprise client contracts renting compute capacity.
 */
data class ClientContract(
    val id: String = UUID.randomUUID().toString(),
    val clientName: String,
    val workloadType: AiWorkloadType,
    val requiredTflops: Double,
    val monthlyPaymentUsd: Long,
    val contractDurationMonthsTotal: Int,
    val monthsRemaining: Int,
    val targetRegionId: String? = null,
    val slaRequirementUptime: Double = 99.95,
    val outagePenaltyUsd: Long = 50_000L,
    val isSatisfied: Boolean = true
)

/**
 * Available Research & Development Technologies.
 */
data class AiResearchTech(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val costUsd: Long,
    val unlockMonths: Int = 1,
    val powerReductionPct: Double = 0.0,
    val revenueBoostPct: Double = 0.0,
    val reliabilityBoostPct: Double = 0.0,
    val pueImprovement: Double = 0.0
)

/**
 * Monthly financial and operational report.
 */
data class AiCloudMonthlyReport(
    val monthYear: String,
    val enterpriseContractRevenue: Long,
    val spotMarketRevenue: Long,
    val totalRevenue: Long,
    val powerElectricityCost: Long,
    val hardwareMaintenanceCost: Long,
    val facilityOperationalCost: Long,
    val outagePenalties: Long,
    val totalExpenses: Long,
    val netProfit: Long,
    val totalTflopsDelivered: Double,
    val averagePue: Double,
    val logSummaries: List<String>
)

/**
 * System Incident / Event logs.
 */
data class AiCloudIncident(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val severity: IncidentSeverity,
    val financialImpactUsd: Long = 0L
)

enum class IncidentSeverity {
    INFO,
    WARNING,
    CRITICAL
}
