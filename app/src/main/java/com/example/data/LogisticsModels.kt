package com.example.data

import java.util.UUID

// ==========================================
// 1. TIPE KENDARAAN & STATUS ARMADA
// ==========================================
enum class LogisticsVehicleType(
    val displayName: String,
    val capacity: Int,
    val baseTripSeconds: Float,
    val buyCost: Long,
    val tripCost: Long,
    val iconEmoji: String,
    val description: String,
    val isImmuneToTraffic: Boolean = false,
    val isVulnerableToRain: Boolean = false,
    val isVulnerableToTraffic: Boolean = false,
    val requiredTechPath: String? = null // null, "ECO_3", "AI_3"
) {
    MOTOR_COURIER(
        displayName = "Motor Kurir",
        capacity = 10,
        baseTripSeconds = 8.0f,
        buyCost = 1_200L,
        tripCost = 5L,
        iconEmoji = "🛵",
        description = "Murah, gesit, dan kebal macet jalan raya. Rentan cuaca hujan badai.",
        isImmuneToTraffic = true,
        isVulnerableToRain = true,
        isVulnerableToTraffic = false
    ),
    BOX_VAN(
        displayName = "Mobil Box / Van",
        capacity = 80,
        baseTripSeconds = 16.0f,
        buyCost = 12_500L,
        tripCost = 35L,
        iconEmoji = "🚐",
        description = "Kapasitas sedang dan seimbang untuk pengiriman paket dalam kota.",
        isImmuneToTraffic = false,
        isVulnerableToRain = false,
        isVulnerableToTraffic = true
    ),
    HEAVY_TRUCK(
        displayName = "Truk Tronton Freight",
        capacity = 600,
        baseTripSeconds = 30.0f,
        buyCost = 65_000L,
        tripCost = 180L,
        iconEmoji = "🚛",
        description = "Kapasitas raksasa untuk distribusi antar-hub. Sangat lambat dan rentan macet parah.",
        isImmuneToTraffic = false,
        isVulnerableToRain = false,
        isVulnerableToTraffic = true
    ),
    CARGO_DRONE(
        displayName = "Drone Kargo AI",
        capacity = 25,
        baseTripSeconds = 5.0f,
        buyCost = 35_000L,
        tripCost = 15L,
        iconEmoji = "🛸",
        description = "Pengiriman kilat point-to-point tanpa hambatan jalan raya. Membutuhkan Riset AI Level 3.",
        isImmuneToTraffic = true,
        isVulnerableToRain = true,
        isVulnerableToTraffic = false,
        requiredTechPath = "AI_3"
    ),
    ELECTRIC_TRUCK(
        displayName = "Electric Semi-Truck",
        capacity = 750,
        baseTripSeconds = 24.0f,
        buyCost = 95_000L,
        tripCost = 50L,
        iconEmoji = "⚡🚚",
        description = "Kapasitas ekstra besar dengan efisiensi energi tinggi (-70% biaya bahan bakar). Membutuhkan Riset Eco Level 3.",
        isImmuneToTraffic = false,
        isVulnerableToRain = false,
        isVulnerableToTraffic = true,
        requiredTechPath = "ECO_3"
    )
}

enum class FleetVehicleStatus {
    IDLE,
    EN_ROUTE,
    MAINTENANCE,
    BROKEN
}

data class FleetVehicle(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: LogisticsVehicleType,
    val conditionHp: Double = 100.0, // 0.0 - 100.0%
    val status: FleetVehicleStatus = FleetVehicleStatus.IDLE,
    val currentTripProgress: Float = 0f, // 0f..1f
    val currentTripTotalSeconds: Float = 10f,
    val currentTripElapsedSeconds: Float = 0f,
    val carryingPackages: Int = 0,
    val tripRatePerPackage: Double = 0.0,
    val assignedContractName: String = "",
    val totalTripsCompleted: Int = 0,
    val totalPackagesDelivered: Long = 0L
)

// ==========================================
// 2. KONTRAK PENGIRIMAN E-COMMERCE & CLIENTS
// ==========================================
data class LogisticsContract(
    val id: String = UUID.randomUUID().toString(),
    val clientName: String,
    val clientLogo: String,
    val categoryTag: String,
    val inboundPackagesPerSec: Double,
    val payoutPerPackage: Double,
    val targetTotalPackages: Long,
    val deliveredPackages: Long = 0L,
    val remainingDurationSeconds: Int = 90,
    val totalContractDurationSeconds: Int = 90,
    val completionBonusCash: Long = 10_000L,
    val tierLevel: Int = 1,
    val isSigned: Boolean = false
) {
    val progressFraction: Float
        get() = if (targetTotalPackages > 0) {
            (deliveredPackages.toFloat() / targetTotalPackages.toFloat()).coerceIn(0f, 1f)
        } else {
            1f - (remainingDurationSeconds.toFloat() / totalContractDurationSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
        }
}

// ==========================================
// 3. SISTEM RNG EVENTS (CUACA & KEMACETAN)
// ==========================================
enum class LogisticsEventType {
    SUNNY,
    RAIN_STORM,
    TRAFFIC_JAM,
    HARBOLNAS_FLASH_SALE,
    ROAD_CONSTRUCTION
}

data class LogisticsWeatherTrafficEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val badgeIcon: String,
    val eventType: LogisticsEventType = LogisticsEventType.SUNNY,
    val speedMultiplierMotor: Float = 1.0f,
    val speedMultiplierVan: Float = 1.0f,
    val speedMultiplierTruck: Float = 1.0f,
    val speedMultiplierDrone: Float = 1.0f,
    val inboundFlowMultiplier: Float = 1.0f,
    val conditionWearMultiplier: Float = 1.0f,
    val bonusPayoutMultiplier: Float = 1.0f,
    val durationRemainingSeconds: Int = 35,
    val totalDurationSeconds: Int = 35
)

// ==========================================
// 4. TECH TREE PROGRESSION
// ==========================================
data class LogisticsTechTree(
    val ecoLevel: Int = 0, // 0..3 (1: Biosolar -15% cost, 2: Hybrid -30% cost, 3: Electric Hub -50% cost & unlock EV Truck)
    val aiSpeedLevel: Int = 0 // 0..3 (1: AI Routing +20% speed, 2: Auto-Dispatch AI, 3: Drone Hub & unlock Drone)
)

// ==========================================
// 5. MASTER LOGISTICS COMPANY DATA
// ==========================================
data class LogisticsCompanyData(
    val internalCash: Long = 20_000L,
    val warehouseLevel: Int = 1, // 1..10
    val currentWarehousePackages: Long = 80L,
    val successRate: Double = 99.0, // 0.0 - 100.0%
    val totalPackagesDelivered: Long = 0L,
    val totalRevenueEarned: Long = 0L,
    val totalDemurragePaid: Long = 0L,
    val fleet: List<FleetVehicle> = defaultInitialFleet(),
    val activeContracts: List<LogisticsContract> = defaultInitialActiveContracts(),
    val availableContracts: List<LogisticsContract> = defaultAvailableContracts(),
    val currentEvent: LogisticsWeatherTrafficEvent = defaultNormalEvent(),
    val techTree: LogisticsTechTree = LogisticsTechTree(),
    val autoDispatchEnabled: Boolean = false,
    val lastOverloadTimestamp: Long = 0L
) {
    val warehouseCapacity: Long
        get() = getWarehouseCapacityForLevel(warehouseLevel)

    val isOverloaded: Boolean
        get() = currentWarehousePackages > warehouseCapacity

    val loadPercentage: Float
        get() = if (warehouseCapacity > 0) (currentWarehousePackages.toFloat() / warehouseCapacity.toFloat()).coerceIn(0f, 2f) else 0f
}

// ==========================================
// 6. HELPER FUNCTIONS & DATA GENERATORS
// ==========================================
fun getWarehouseCapacityForLevel(level: Int): Long {
    return when (level) {
        1 -> 500L
        2 -> 1_200L
        3 -> 3_000L
        4 -> 7_500L
        5 -> 18_000L
        6 -> 40_000L
        7 -> 100_000L
        8 -> 250_000L
        9 -> 600_000L
        else -> 1_500_000L
    }
}

fun getWarehouseUpgradeCost(currentLevel: Int): Long {
    return when (currentLevel) {
        1 -> 8_000L
        2 -> 20_000L
        3 -> 45_000L
        4 -> 95_000L
        5 -> 200_000L
        6 -> 450_000L
        7 -> 950_000L
        8 -> 2_000_000L
        9 -> 4_500_000L
        else -> 10_000_000L
    }
}

fun defaultInitialFleet(): List<FleetVehicle> {
    return listOf(
        FleetVehicle(
            id = "motor_1",
            name = "Motor Kurir #01 (Beat)",
            type = LogisticsVehicleType.MOTOR_COURIER,
            conditionHp = 100.0,
            status = FleetVehicleStatus.IDLE
        ),
        FleetVehicle(
            id = "motor_2",
            name = "Motor Kurir #02 (Vario)",
            type = LogisticsVehicleType.MOTOR_COURIER,
            conditionHp = 95.0,
            status = FleetVehicleStatus.IDLE
        ),
        FleetVehicle(
            id = "van_1",
            name = "Mobil Box #01 (GranMax)",
            type = LogisticsVehicleType.BOX_VAN,
            conditionHp = 100.0,
            status = FleetVehicleStatus.IDLE
        )
    )
}

fun defaultNormalEvent(): LogisticsWeatherTrafficEvent {
    return LogisticsWeatherTrafficEvent(
        id = "event_sunny",
        title = "Cuaca Cerah & Jalanan Lancar",
        description = "Kondisi operasional optimal. Semua armada melaju dengan kecepatan normal.",
        badgeIcon = "☀️",
        eventType = LogisticsEventType.SUNNY,
        speedMultiplierMotor = 1.0f,
        speedMultiplierVan = 1.0f,
        speedMultiplierTruck = 1.0f,
        speedMultiplierDrone = 1.0f,
        inboundFlowMultiplier = 1.0f,
        conditionWearMultiplier = 1.0f,
        bonusPayoutMultiplier = 1.0f,
        durationRemainingSeconds = 40,
        totalDurationSeconds = 40
    )
}

fun defaultInitialActiveContracts(): List<LogisticsContract> {
    return listOf(
        LogisticsContract(
            id = "contract_init_1",
            clientName = "TokoNusantara Express",
            clientLogo = "🛍️",
            categoryTag = "E-Commerce Reguler",
            inboundPackagesPerSec = 4.0,
            payoutPerPackage = 5.50,
            targetTotalPackages = 1_500L,
            deliveredPackages = 0L,
            remainingDurationSeconds = 90,
            totalContractDurationSeconds = 90,
            completionBonusCash = 7_500L,
            tierLevel = 1,
            isSigned = true
        )
    )
}

fun defaultAvailableContracts(): List<LogisticsContract> {
    return listOf(
        LogisticsContract(
            id = "contract_shopee",
            clientName = "ShopeeMart 2-Hour Delivery",
            clientLogo = "⚡",
            categoryTag = "Groceries & Fresh",
            inboundPackagesPerSec = 8.0,
            payoutPerPackage = 7.00,
            targetTotalPackages = 3_000L,
            remainingDurationSeconds = 100,
            totalContractDurationSeconds = 100,
            completionBonusCash = 15_000L,
            tierLevel = 2,
            isSigned = false
        ),
        LogisticsContract(
            id = "contract_amazone",
            clientName = "Amazone Prime Cargo",
            clientLogo = "📦",
            categoryTag = "Bulk Electronics & Home",
            inboundPackagesPerSec = 14.0,
            payoutPerPackage = 9.50,
            targetTotalPackages = 6_000L,
            remainingDurationSeconds = 120,
            totalContractDurationSeconds = 120,
            completionBonusCash = 35_000L,
            tierLevel = 3,
            isSigned = false
        ),
        LogisticsContract(
            id = "contract_global",
            clientName = "Global Supply Chain Ltd",
            clientLogo = "🌐",
            categoryTag = "Industrial Freight Hub",
            inboundPackagesPerSec = 22.0,
            payoutPerPackage = 13.00,
            targetTotalPackages = 12_000L,
            remainingDurationSeconds = 150,
            totalContractDurationSeconds = 150,
            completionBonusCash = 80_000L,
            tierLevel = 4,
            isSigned = false
        )
    )
}

object LogisticsDataGenerator {
    private val clientPool = listOf(
        Pair("TokoNusantara Express", "🛍️") to "E-Commerce Reguler",
        Pair("ShopeeMart Instant", "⚡") to "Groceries & Fresh",
        Pair("Amazone Prime Cargo", "📦") to "Bulk Electronics",
        Pair("BliBli Fast Fulfillment", "🏬") to "Gadget & Lifestyle",
        Pair("TikTokShop Viral Hub", "📱") to "Fashion & Cosmetics",
        Pair("Global Supply Chain Ltd", "🌐") to "Industrial Cargo",
        Pair("Apotek Sehat Delivery", "💊") to "Pharma & Healthcare",
        Pair("Super Indo Logistics", "🛒") to "Supermarket Distribution"
    )

    fun generateNewContract(warehouseLevel: Int): LogisticsContract {
        val (clientPair, category) = clientPool.random()
        val tier = (1..minOf(5, 1 + warehouseLevel / 2)).random()
        val inboundRate = (3.0 + tier * 3.5 + (0..4).random()).coerceIn(4.0, 35.0)
        val payout = 4.50 + (tier * 2.20) + (Math.random() * 1.5)
        val target = (1_000L * tier) + ((1..5).random() * 500L)
        val duration = 60 + (tier * 20)
        val bonus = (target * payout * 0.25).toLong()

        return LogisticsContract(
            id = UUID.randomUUID().toString(),
            clientName = clientPair.first,
            clientLogo = clientPair.second,
            categoryTag = category,
            inboundPackagesPerSec = inboundRate,
            payoutPerPackage = (payout * 100).toLong() / 100.0,
            targetTotalPackages = target,
            remainingDurationSeconds = duration,
            totalContractDurationSeconds = duration,
            completionBonusCash = bonus,
            tierLevel = tier,
            isSigned = false
        )
    }

    fun generateRandomEvent(): LogisticsWeatherTrafficEvent {
        val dice = (1..100).random()
        return when {
            dice <= 35 -> defaultNormalEvent()
            dice <= 55 -> LogisticsWeatherTrafficEvent(
                id = UUID.randomUUID().toString(),
                title = "Hujan Badai & Genangan Air",
                description = "Jalanan licin dan banjir. Motor Kurir melambat -50% & wear +50%. Truk & Van melambat -15%.",
                badgeIcon = "🌧️",
                eventType = LogisticsEventType.RAIN_STORM,
                speedMultiplierMotor = 0.50f,
                speedMultiplierVan = 0.85f,
                speedMultiplierTruck = 0.80f,
                speedMultiplierDrone = 0.30f,
                inboundFlowMultiplier = 0.9f,
                conditionWearMultiplier = 1.5f,
                bonusPayoutMultiplier = 1.0f,
                durationRemainingSeconds = 30,
                totalDurationSeconds = 30
            )
            dice <= 75 -> LogisticsWeatherTrafficEvent(
                id = UUID.randomUUID().toString(),
                title = "Macet Parah & Jam Pulang Kerja",
                description = "Kemacetan lalu lintas parah. Mobil Box melambat -50%, Truk Tronton -65%. Motor Kurir kebal macet!",
                badgeIcon = "🚗",
                eventType = LogisticsEventType.TRAFFIC_JAM,
                speedMultiplierMotor = 1.0f,
                speedMultiplierVan = 0.50f,
                speedMultiplierTruck = 0.35f,
                speedMultiplierDrone = 1.0f,
                inboundFlowMultiplier = 1.0f,
                conditionWearMultiplier = 1.2f,
                bonusPayoutMultiplier = 1.0f,
                durationRemainingSeconds = 30,
                totalDurationSeconds = 30
            )
            dice <= 90 -> LogisticsWeatherTrafficEvent(
                id = UUID.randomUUID().toString(),
                title = "Promo Harbolnas 11.11 / Flash Sale",
                description = "Volume paket masuk melonjak +150%! Bonus bayaran +25%/paket. Waspadai risiko Gudang Overload!",
                badgeIcon = "🔥",
                eventType = LogisticsEventType.HARBOLNAS_FLASH_SALE,
                speedMultiplierMotor = 1.0f,
                speedMultiplierVan = 1.0f,
                speedMultiplierTruck = 1.0f,
                speedMultiplierDrone = 1.0f,
                inboundFlowMultiplier = 2.5f,
                conditionWearMultiplier = 1.1f,
                bonusPayoutMultiplier = 1.25f,
                durationRemainingSeconds = 25,
                totalDurationSeconds = 25
            )
            else -> LogisticsWeatherTrafficEvent(
                id = UUID.randomUUID().toString(),
                title = "Perbaikan Jalan Tol & Arteri",
                description = "Penyempitan jalur dan pengalihan rute. Truk Tronton melambat -40%, Mobil Box -25%.",
                badgeIcon = "🚧",
                eventType = LogisticsEventType.ROAD_CONSTRUCTION,
                speedMultiplierMotor = 0.95f,
                speedMultiplierVan = 0.75f,
                speedMultiplierTruck = 0.60f,
                speedMultiplierDrone = 1.0f,
                inboundFlowMultiplier = 1.0f,
                conditionWearMultiplier = 1.3f,
                bonusPayoutMultiplier = 1.0f,
                durationRemainingSeconds = 30,
                totalDurationSeconds = 30
            )
        }
    }
}
