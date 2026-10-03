package com.example.data

enum class BusinessCategory { CULINARY, RETAIL, PROPERTY, CREATIVE, LOGISTICS, ENTERTAINMENT, FINANCE, CORPORATION, TECHNOLOGY, AVIATION, THEME_PARK_HOLDING, HOSPITALITY, CRUISE_LINE, SPORTS }

data class DummyAircraft(val id: String, val model: String, val type: String, val price: Long, val maxPax: Int, val range: String, val deliveryTime: Int, val leasePrice: Long = 0L)

data class DummyHubUpgrade(val id: String, val name: String, val cost: Long, val buildTime: Int, val level: Int, val unlocks: List<String>)

val DUMMY_AIRCRAFTS = listOf(
    DummyAircraft("atr72", "ATR 72-600", "PROPELLER", 20000000L, 72, "SHORT", 1),
    DummyAircraft("a320", "Airbus A320neo", "NARROW_BODY", 55000000L, 180, "MEDIUM", 2),
    DummyAircraft("b777", "Boeing 777-300ER", "WIDE_BODY", 150000000L, 350, "LONG", 4)
)

val AVIATION_AIRCRAFT_CATALOG = listOf(
    // --- TURBOPROP (Baling-baling) - Rute Pendek / Perintis ---
    DummyAircraft("dh8_100", "De Havilland Dash 8-100", "PROPELLER", 20000000L, 37, "SHORT", 3, 80000L),
    DummyAircraft("atr42_600", "ATR 42-600", "PROPELLER", 21000000L, 50, "SHORT", 3, 120000L),
    DummyAircraft("atr72_500", "ATR 72-500", "PROPELLER", 22000000L, 74, "SHORT", 4, 130000L),
    DummyAircraft("dh8_300", "De Havilland Dash 8-300", "PROPELLER", 25000000L, 50, "SHORT", 4, 100000L),
    DummyAircraft("atr72_600", "ATR 72-600", "PROPELLER", 26000000L, 78, "SHORT", 5, 150000L),
    DummyAircraft("q400", "Dash 8 Q400", "PROPELLER", 32000000L, 90, "SHORT", 6, 180000L),

    // --- REGIONAL JET - Rute Menengah/Singkat ---
    DummyAircraft("e170", "Embraer E170", "REGIONAL_JET", 25000000L, 78, "SHORT_MEDIUM", 6, 150000L),
    DummyAircraft("crj700", "Bombardier CRJ700", "REGIONAL_JET", 27000000L, 78, "SHORT_MEDIUM", 6, 180000L),
    DummyAircraft("e175", "Embraer E175", "REGIONAL_JET", 28000000L, 88, "SHORT_MEDIUM", 7, 200000L),
    DummyAircraft("e190", "Embraer E190", "REGIONAL_JET", 32000000L, 114, "SHORT_MEDIUM", 8, 250000L),
    DummyAircraft("crj900", "Bombardier CRJ900", "REGIONAL_JET", 35000000L, 90, "SHORT_MEDIUM", 7, 200000L),
    DummyAircraft("e195", "Embraer E195", "REGIONAL_JET", 38000000L, 124, "SHORT_MEDIUM", 9, 300000L),
    DummyAircraft("crj1000", "Bombardier CRJ1000", "REGIONAL_JET", 45000000L, 104, "SHORT_MEDIUM", 9, 250000L),
    DummyAircraft("m90", "Mitsubishi SpaceJet M90", "REGIONAL_JET", 47000000L, 88, "SHORT_MEDIUM", 10, 250000L),

    // --- NARROW BODY - Tulang Punggung Maskapai ---
    DummyAircraft("e190_e2", "Embraer E190-E2", "NARROW_BODY", 60000000L, 114, "MEDIUM", 12, 180000L),
    DummyAircraft("e195_e2", "Embraer E195-E2", "NARROW_BODY", 70000000L, 146, "MEDIUM", 12, 250000L),
    DummyAircraft("a220_100", "Airbus A220-100", "NARROW_BODY", 81000000L, 135, "MEDIUM", 13, 250000L),
    DummyAircraft("a220_300", "Airbus A220-300", "NARROW_BODY", 91000000L, 160, "MEDIUM", 14, 280000L),
    DummyAircraft("b737m7", "Boeing 737 MAX 7", "NARROW_BODY", 100000000L, 172, "MEDIUM", 15, 380000L),
    DummyAircraft("a319neo", "Airbus A319neo", "NARROW_BODY", 100000000L, 160, "MEDIUM", 15, 350000L),
    DummyAircraft("a320neo", "Airbus A320neo", "NARROW_BODY", 110000000L, 195, "MEDIUM", 16, 400000L),
    DummyAircraft("b737m8", "Boeing 737 MAX 8", "NARROW_BODY", 120000000L, 210, "MEDIUM", 16, 400000L),
    DummyAircraft("b737m9", "Boeing 737 MAX 9", "NARROW_BODY", 125000000L, 220, "MEDIUM", 17, 430000L),
    DummyAircraft("a321neo", "Airbus A321neo", "NARROW_BODY", 130000000L, 244, "MEDIUM", 18, 460000L),
    DummyAircraft("b737m10", "Boeing 737 MAX 10", "NARROW_BODY", 135000000L, 230, "MEDIUM", 18, 450000L),

    // --- WIDE BODY & JUMBO JET - Rute Internasional Jarak Jauh ---
    DummyAircraft("b747_200", "Boeing 747-200 (Classic)", "WIDE_BODY", 39000000L, 480, "LONG", 12, 200000L),
    DummyAircraft("b747_300", "Boeing 747-300", "WIDE_BODY", 82000000L, 496, "LONG", 15, 300000L),
    DummyAircraft("b747_400", "Boeing 747-400", "WIDE_BODY", 230000000L, 660, "LONG", 24, 550000L),
    DummyAircraft("b787_8", "Boeing 787-8 Dreamliner", "WIDE_BODY", 250000000L, 242, "LONG", 26, 950000L),
    DummyAircraft("a330_800neo", "Airbus A330-800neo", "WIDE_BODY", 260000000L, 406, "LONG", 27, 800000L),
    DummyAircraft("a340_600", "Airbus A340-600", "WIDE_BODY", 280000000L, 440, "LONG", 24, 700000L),
    DummyAircraft("a330_900neo", "Airbus A330-900neo", "WIDE_BODY", 290000000L, 440, "LONG", 28, 900000L),
    DummyAircraft("b787_9", "Boeing 787-9 Dreamliner", "WIDE_BODY", 300000000L, 290, "LONG", 29, 1050000L),
    DummyAircraft("a350_900", "Airbus A350-900", "WIDE_BODY", 308000000L, 440, "LONG", 30, 110000L),
    DummyAircraft("b787_10", "Boeing 787-10 Dreamliner", "WIDE_BODY", 340000000L, 330, "LONG", 32, 1150000L),
    DummyAircraft("b777_300er", "Boeing 777-300ER", "WIDE_BODY", 340000000L, 396, "LONG", 31, 120000L),
    DummyAircraft("a350_1000", "Airbus A350-1000", "WIDE_BODY", 356000000L, 480, "LONG", 34, 130000L),
    DummyAircraft("b777_8", "Boeing 777-8", "WIDE_BODY", 410000000L, 384, "LONG", 35, 1400000L),
    DummyAircraft("b747_8", "Boeing 747-8 Intercontinental", "WIDE_BODY", 418000000L, 467, "LONG", 36, 1800000L),
    DummyAircraft("b777_9", "Boeing 777-9", "WIDE_BODY", 440000000L, 426, "LONG", 36, 1600000L),
    DummyAircraft("a380", "Airbus A380-800", "WIDE_BODY", 445000000L, 853, "LONG", 48, 2200000L),

    // --- HELICOPTER - Penerbangan VIP / Ultra Short (Helipad) ---
    DummyAircraft("r44", "Robinson R44 Raven II", "HELICOPTER", 650000L, 3, "ULTRA_SHORT", 1, 7500L),
    DummyAircraft("r66", "Robinson R66 Turbine", "HELICOPTER", 1300000L, 4, "ULTRA_SHORT", 1, 12000L),
    DummyAircraft("bell206", "Bell 206 JetRanger", "HELICOPTER", 1500000L, 4, "ULTRA_SHORT", 2, 15000L),
    DummyAircraft("bell505", "Bell 505 Jet Ranger X", "HELICOPTER", 1500000L, 4, "ULTRA_SHORT", 2, 16000L),
    DummyAircraft("bell407", "Bell 407GXi", "HELICOPTER", 3500000L, 6, "ULTRA_SHORT", 3, 22000L),
    DummyAircraft("h125", "Airbus H125", "HELICOPTER", 4000000L, 5, "ULTRA_SHORT", 3, 30000L),
    DummyAircraft("aw119", "Leonardo AW119Kx", "HELICOPTER", 4000000L, 7, "ULTRA_SHORT", 3, 32000L),
    DummyAircraft("h130", "Airbus H130", "HELICOPTER", 4500000L, 6, "ULTRA_SHORT", 3, 35000L),
    DummyAircraft("aw109", "Leonardo AW109 GrandNew", "HELICOPTER", 6500000L, 7, "ULTRA_SHORT", 4, 70000L),
    DummyAircraft("h135", "Airbus H135", "HELICOPTER", 7000000L, 6, "ULTRA_SHORT", 4, 55000L),
    DummyAircraft("bell429", "Bell 429", "HELICOPTER", 8000000L, 7, "ULTRA_SHORT", 4, 70000L),
    DummyAircraft("h145", "Airbus H145", "HELICOPTER", 10000000L, 9, "ULTRA_SHORT", 5, 80000L),
    DummyAircraft("s76d", "Sikorsky S-76D", "HELICOPTER", 13000000L, 12, "ULTRA_SHORT", 5, 150000L),
    DummyAircraft("aw139", "Leonardo AW139", "HELICOPTER", 14000000L, 15, "ULTRA_SHORT", 5, 180000L),
    DummyAircraft("mi8", "Mil Mi-8", "HELICOPTER", 15000000L, 24, "ULTRA_SHORT", 6, 220000L),
    DummyAircraft("h225", "Airbus H225 Super Puma", "HELICOPTER", 25000000L, 19, "ULTRA_SHORT", 6, 350000L),
    DummyAircraft("s92", "Sikorsky S-92", "HELICOPTER", 30000000L, 19, "ULTRA_SHORT", 6, 450000L)
)

data class GlobalAviationHubDef(
    val id: String,
    val city: String,
    val country: String,
    val baseCost: Long,
    val buildTime: Int,
    val maxSlots: Int
)

val GLOBAL_AVIATION_HUBS = listOf(
    // LOKAL / REGIONAL
    GlobalAviationHubDef("cgk", "Jakarta (CGK)", "Indonesia", 25000000L, 3, 10),
    GlobalAviationHubDef("dps", "Denpasar (DPS)", "Indonesia", 35000000L, 4, 8),
    GlobalAviationHubDef("sin", "Singapore (SIN)", "Singapore", 60000000L, 6, 15),
    GlobalAviationHubDef("bkk", "Bangkok (BKK)", "Thailand", 45000000L, 5, 12),
    
    // ASIA PASIFIK
    GlobalAviationHubDef("hnd", "Tokyo (HND)", "Japan", 80000000L, 8, 20),
    GlobalAviationHubDef("icn", "Seoul (ICN)", "South Korea", 70000000L, 7, 18),
    GlobalAviationHubDef("pek", "Beijing (PEK)", "China", 75000000L, 8, 25),
    GlobalAviationHubDef("syd", "Sydney (SYD)", "Australia", 65000000L, 7, 15),

    // TIMUR TENGAH & EROPA
    GlobalAviationHubDef("dxb", "Dubai (DXB)", "UAE", 150000000L, 12, 30),
    GlobalAviationHubDef("doh", "Doha (DOH)", "Qatar", 120000000L, 10, 25),
    GlobalAviationHubDef("lhr", "London (LHR)", "UK", 180000000L, 14, 20),
    GlobalAviationHubDef("cdg", "Paris (CDG)", "France", 160000000L, 12, 18),
    GlobalAviationHubDef("fra", "Frankfurt (FRA)", "Germany", 150000000L, 12, 20),

    // AMERIKA
    GlobalAviationHubDef("jfk", "New York (JFK)", "USA", 200000000L, 15, 25),
    GlobalAviationHubDef("lax", "Los Angeles (LAX)", "USA", 180000000L, 14, 25),
    GlobalAviationHubDef("atl", "Atlanta (ATL)", "USA", 160000000L, 12, 30)
)

val DUMMY_HUB_UPGRADES = listOf(
    DummyHubUpgrade("upg_dom", "Terminal Domestik Standard", 10000000L, 2, 1, listOf("SHORT")),
    DummyHubUpgrade("upg_intl_1", "Gerbang Internasional Dasar", 50000000L, 4, 2, listOf("MEDIUM")),
    DummyHubUpgrade("upg_vip", "VIP Executive Lounge", 25000000L, 3, 1, listOf("PREMIUM_TICKET")),
    DummyHubUpgrade("upg_intl_2", "Landasan Pacu Wide-Body", 100000000L, 6, 3, listOf("LONG")),
    DummyHubUpgrade("upg_cargo", "Fasilitas Kargo Udara", 40000000L, 3, 1, listOf("CARGO"))
)

data class Aircraft(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: String,
    val name: String,
    val capacity: Int,
    val isUsed: Boolean,
    var condition: Int = 100,
    val maintenanceCost: Double,
    val deliveryDelay: Int = 0
)

data class AircraftInstance(
    val id: String = java.util.UUID.randomUUID().toString(),
    val modelId: String, // 'atr72', 'a320', 'b777'
    val condition: Double = 100.0,
    val status: String = "DELIVERING", // DELIVERING, STANDBY, ASSIGNED, MAINTENANCE
    val monthsUntilDelivery: Int = 2,
    val stationedHubId: String? = null,
    val assignedRouteId: String? = null,
    val isLeased: Boolean = false,
    val leasePrice: Long = 0L
)

data class AviationHub(
    val id: String = java.util.UUID.randomUUID().toString(),
    val city: String,
    val baseCost: Long = 150000000L,
    val activeUpgrades: List<String> = emptyList(), // ID of upgrades completed
    val constructionQueue: List<HubConstructionItem> = emptyList(),
    val isConstructing: Boolean = false,
    val constructionMonthsLeft: Int = 0
)

data class HubConstructionItem(
    val upgradeId: String,
    val monthsRemaining: Int
)

data class FlightRoute(
    val id: String = java.util.UUID.randomUUID().toString(),
    val originHubId: String,
    val destination: String,
    val distanceCategory: String, // SHORT, MEDIUM, LONG
    val baseDemand: Int,
    val ticketPrice: Int,
    val assignedAircraftIds: List<String> = emptyList()
)

data class BusinessUpgrade(
    val id: String,
    val name: String,
    val description: String,
    val baseCost: Long,
    val costMultiplier: Float = 1.0f,
    val revenueMultiplier: Float = 1.0f,
    val maintenanceMultiplier: Float = 1.0f,
    val revenueFlatBoost: Long = 0,
    val maintenanceFlatReduction: Long = 0,
    val maxLevel: Int = 1
)

data class BusinessCatalogItem(
    val id: String,
    val name: String,
    val category: BusinessCategory,
    val imageUrl: String? = null,
    val costToBuy: Long,
    val monthlyRevenue: Long,
    val monthlyMaintenanceCost: Long,
    val isFluctuating: Boolean = false,
    val upgrades: List<BusinessUpgrade> = emptyList()
)

data class ActiveUpgrade(
    val selectedUpgradeId: String,
    val targetLevel: Int,
    val startTimeMs: Long,
    val finishTimeMs: Long
)

data class CoProductionMeta(
    val isCoProd: Boolean = false,
    val partnerName: String = "",
    val fundingType: String = "",
    val revenueSplit: Double = 0.0
)

data class MovieProject(
    val title: String,
    val budget: Long,
    val genres: List<String>,
    val distributionScale: String,
    val reviewScore: Int,
    val boxOffice: Long,
    val netProfit: Long,
    val status: String = "FINISHED",
    val remainingMonths: Int = 0,
    val currentRevenue: Long = 0L,
    val targetMaxRevenue: Long = 0L,
    val productionPhase: String = "TAYANG",
    val productionDelayMonths: Int = 0,
    val promoBudget: Long = 0L,
    val scheduledMonth: Int? = null,
    val scheduledYear: Int? = null,
    val filmFormat: String = "Feature Film",
    val isQcPhase: Boolean = false,
    val internalScore: Int? = null,
    val isAwaitingRelease: Boolean = false,
    val scheduledReleaseDate: String? = null,
    var releaseMonth: Int? = null,
    var releaseYear: Int? = null,
    var licenseeName: String? = null,
    var licenseMonthlyFee: Long? = null,
    var licenseRemainingMonths: Int? = null,
    var productionFocus: String? = "REGULER",
    val coProductionMeta: CoProductionMeta? = null,
    val prodStartMonth: Int? = null,
    val prodStartYear: Int? = null,
    val theaterStartMonth: Int? = null,
    val theaterStartYear: Int? = null,
    val totalTheatricalMonths: Int? = null,
    val totalProductionMonths: Int = 0,
    val lastMonthRevenue: Long = 0L
) {
    val isCoProd: Boolean get() = coProductionMeta?.isCoProd == true
}

data class HealthcareEpidemicEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val severityMultiplier: Double = 3.0, // 300% claims increase
    val durationMonths: Int = 3,
    val remainingMonths: Int = 3,
    val icon: String = "🦠",
    val effectDescription: String = "Lonjakan Klaim Asuransi +300% & Pasien Membludak!"
)

data class HealthcareUnit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: String, // "HOSPITAL", "INSURANCE", "CLINIC"
    var level: Int = 1,
    var members: Long = 0,
    var monthlyRevenue: Double = 0.0,
    val isUpgrading: Boolean = false,
    val upgradeDelayMonths: Int = 0,
    val companyCash: Double = 0.0,
    val tierCategory: String = "BASIC",
    val unitCash: Double = 0.0,
    
    // Medical Department Management (HOSPITAL & CLINIC)
    val igdLevel: Int = 1,
    val specialistLevel: Int = 1,
    val pharmacyLevel: Int = 1,
    val totalBeds: Int = 100,
    val currentOccupiedBeds: Int = 0,
    val bor: Double = 0.0, // Bed Occupancy Rate (0% - 100%)
    val rejectedPatientsLastMonth: Int = 0,
    
    // Actuary & Insurance Specifics (INSURANCE)
    val monthlyPremium: Double = 50.0, // Monthly premium ($20 - $500)
    val lastMonthClaimsPaid: Double = 0.0,
    val lastMonthClaimsCount: Long = 0L,
    val inNetworkClaimsCount: Long = 0L,
    val outNetworkClaimsCount: Long = 0L,
    val inNetworkClaimsAmount: Double = 0.0,
    val outNetworkClaimsAmount: Double = 0.0,
    val lossRatio: Double = 0.0, // (Claims Paid / Premium Income) * 100
    val synergyRate: Double = 0.0, // (In-Network / Total Claims) * 100
    val reserveFund: Double = 0.0
)

data class ConstructionPhase(
    val phaseNumber: Int = 1,
    val name: String = "",
    val durationMonths: Int = 2,
    var remainingMonths: Int = 2,
    var isAllocated: Boolean = false,
    var isCompleted: Boolean = false,
    val payoutPercent: Double = 0.30
)

data class ConstructionRngEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val description: String = "",
    val type: String = "PRICE_HIKE", // "PRICE_HIKE", "BAD_WEATHER", "ACCIDENT", "QUALITY_BONUS"
    val costImpact: Long = 0L,
    val delayMonths: Int = 0,
    val trustScoreImpact: Int = 0,
    var isResolved: Boolean = false
)

data class ConstructionTenderOpportunity(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "",
    val clientName: String = "",
    val clientType: String = "PEMERINTAH", // "PEMERINTAH", "SWASTA", "LUXURY", "MEGA"
    val projectScale: String = "MEDIUM", // "MEDIUM", "LARGE", "MEGA"
    val ownerEstimateBudget: Long = 25_000_000L,
    val estimatedBaseCost: Long = 18_000_000L,
    val durationMonths: Int = 6,
    val requiredCrews: Int = 1,
    val requiredMachinery: Int = 3,
    val phases: List<ConstructionPhase> = emptyList(),
    val rivalAiBids: List<Long> = emptyList(),
    val minBidBond: Long = 1_500_000L,
    val description: String = ""
)

data class SoftwareHouseCompanyData(
    val uiUxDesigners: Int = 2,
    val frontendDevelopers: Int = 2,
    val backendEngineers: Int = 2,
    val serverTier: Int = 1, // 1: Starter (25k), 2: Pro (100k), 3: Enterprise (500k), 4: Multi-Region (2.5M), 5: Hyperscale (10M)
    val hasCiCdPipeline: Boolean = false,
    val hasAiCopilot: Boolean = false,
    val hasMicroservices: Boolean = false,
    val hasAutomatedQa: Boolean = false,
    val completedB2BCount: Int = 0,
    val totalB2BRevenue: Long = 0L,
    val totalSaaSMade: Int = 0,
    val totalAcquisitionValue: Long = 0L,
    val serverIncidentCount: Int = 0,
    val isOutageActive: Boolean = false
) {
    val maxServerCapacity: Long get() = when (serverTier) {
        1 -> 25_000L
        2 -> 100_000L
        3 -> 500_000L
        4 -> 2_500_000L
        5 -> 10_000_000L
        else -> 25_000L
    }
    
    val serverTierName: String get() = when (serverTier) {
        1 -> "Starter Cloud (Vercel / DigitalOcean)"
        2 -> "Pro Scaled (AWS Lightsail / RDS Cluster)"
        3 -> "Enterprise Cloud (AWS Multi-AZ / EKS)"
        4 -> "Multi-Region Cluster (GCP Spanner / Cloudflare)"
        5 -> "Hyperscale AI Supercluster (NVidia H100 GPU Pool)"
        else -> "Starter Cloud"
    }

    val serverMonthlyCost: Long get() = when (serverTier) {
        1 -> 2_000L
        2 -> 6_000L
        3 -> 18_000L
        4 -> 45_000L
        5 -> 120_000L
        else -> 2_000L
    }

    val totalTeamSalaries: Long get() = (uiUxDesigners * 4_000L) + (frontendDevelopers * 6_000L) + (backendEngineers * 8_000L)
    val totalMonthlyExpenses: Long get() = totalTeamSalaries + serverMonthlyCost + (if (hasAiCopilot) 2000L else 0L) + (if (hasCiCdPipeline) 1500L else 0L)
}

data class ConstructionFirmData(
    val maxCrews: Int = 3,
    val maxMachinery: Int = 10,
    val trustScore: Int = 65,
    val safetyCertLevel: Int = 1,
    val completedProjectsCount: Int = 0,
    val totalRevenueGenerated: Long = 0L,
    val availableTenderMarket: List<ConstructionTenderOpportunity> = emptyList()
)

data class ConstructionProject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val totalContractValue: Double,
    val durationMonths: Int,
    var remainingMonths: Int,
    val isFinished: Boolean = false,
    val clientName: String = "Kementerian PUPR",
    val clientType: String = "PEMERINTAH",
    val projectScale: String = "MEDIUM",
    val ownerEstimateBudget: Long = 0L,
    val agreedBidPrice: Long = 0L,
    val currentPhaseIndex: Int = 0,
    val phases: List<ConstructionPhase> = emptyList(),
    val requiredCrews: Int = 1,
    val requiredMachinery: Int = 3,
    val totalPaidOut: Long = 0L,
    val initialSecurityDeposit: Long = 0L,
    val usesInHouseLogistics: Boolean = false,
    val activeEvent: ConstructionRngEvent? = null
)

data class EventProject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val pax: Int,
    val totalBudget: Double,
    val eoFee: Double,
    val techFee: Double,
    val useInHouseTech: Boolean,
    val executionEndTime: Long = 0L,
    
    // NEW properties for advanced mechanics:
    val tier: Int = 2, // Client tier: 2 to 5 Stars
    val isSpecial: Boolean = false, // If true, it is an iconic prestige event
    val requiredPrestige: Int = 0, // Prestige level required
    val requiredReputation: Double = 0.0, // Reputation required
    val weather: String = "SUNNY", // "SUNNY", "RAINY"
    val isOutdoor: Boolean = false, // If outdoor, susceptible to weather
    val requirements: List<String> = emptyList(), // "Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance"
    
    // Incident tracking:
    val activeIncident: String? = null, // Current active incident during execution
    val incidentResolved: Boolean = false,
    val incidentImpactQuality: Double = 0.0,
    val incidentImpactCost: Double = 0.0,
    
    // Planning/Execution status:
    val phase: String = "PLANNING", // "PLANNING", "EXECUTING", "REVIEW"
    val rentedAssets: List<String> = emptyList(), // Assets currently rented for this event
    val quality: Double = 100.0, // Event Quality: starts at 100.0
    val executionStartTime: Long = 0L,
    
    // Result details:
    val resultRating: Double = 0.0, // Stars awarded: 1 to 5
    val resultReviewText: String = "",
    val finalProfit: Double = 0.0
)

data class EoCustomAsset(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: String,
    val quantity: Int = 1,
    val priceUnit: Double = 0.0,
    val imageUrl: String? = null
)

enum class HotelTier(val title: String, val baseBuildCost: Long, val buildMonths: Int, val maxRooms: Int, val baseRoomRate: Long) {
    CAPSULE_HOTEL("Capsule Hotel", 2_000_000, 2, 100, 20),
    TRANSIT_MOTEL("Transit Motel", 5_000_000, 2, 50, 40),
    BUDGET_INN("Budget Inn", 8_000_000, 3, 80, 50),
    GUESTHOUSE("Boutique Guesthouse", 12_000_000, 3, 30, 80),
    CITY_HOTEL_2STAR("City Hotel 2-Star", 18_000_000, 4, 100, 70),
    CITY_HOTEL_3STAR("City Hotel 3-Star", 30_000_000, 5, 150, 100),
    BUSINESS_HOTEL_3STAR("Business Hotel 3-Star", 45_000_000, 6, 200, 120),
    SUBURBAN_HOTEL_4STAR("Suburban Hotel 4-Star", 60_000_000, 7, 250, 150),
    BUSINESS_HOTEL_4STAR("Business Hotel 4-Star", 85_000_000, 8, 300, 200),
    AIRPORT_HOTEL_4STAR("Premium Airport Hotel", 100_000_000, 9, 350, 220),
    BOUTIQUE_HOTEL_4STAR("Luxury Boutique Hotel", 120_000_000, 10, 80, 350),
    RESORT_4STAR("Beachfront Resort 4-Star", 150_000_000, 12, 400, 280),
    CITY_HOTEL_5STAR("Grand City Hotel 5-Star", 250_000_000, 14, 500, 450),
    LUXURY_RESORT_5STAR("Luxury Resort 5-Star", 350_000_000, 16, 600, 600),
    ECO_LODGE_5STAR("Premium Eco Lodge", 400_000_000, 15, 150, 800),
    HERITAGE_HOTEL_5STAR("Heritage Palace Hotel", 500_000_000, 18, 200, 1000),
    SKYSCRAPER_HOTEL_5STAR("Skyscraper Luxury Hotel", 750_000_000, 20, 800, 750),
    CASINO_RESORT_5STAR("Mega Casino Resort", 1_200_000_000, 24, 1500, 500),
    PRIVATE_VILLA_ESTATE("Exclusive Villa Estate", 1_500_000_000, 20, 50, 2500),
    ULTRA_LUXURY_6STAR("6-Star Ultra Luxury", 2_000_000_000, 26, 300, 3500),
    PRIVATE_ISLAND_7STAR("7-Star Private Island", 3_500_000_000, 36, 100, 10000),
    UNDERWATER_HOTEL("Underwater Luxury Suites", 5_000_000_000, 48, 50, 25000)
}

enum class HotelFacility(val title: String, val buildCost: Long, val maintenanceCost: Long, val bonusOccupancy: Double, val bonusRevenue: Long) {
    BASIC_POOL("Standard Swimming Pool", 1_000_000, 10_000, 0.02, 0),
    FITNESS_CENTER("24/7 Fitness Center", 2_000_000, 20_000, 0.03, 50_000),
    CAFE_BAKERY("Artisan Cafe & Bakery", 3_000_000, 30_000, 0.02, 100_000),
    KIDS_CLUB("Premium Kids Club", 5_000_000, 50_000, 0.04, 100_000),
    ROOFTOP_BAR("Rooftop Sky Bar", 8_000_000, 80_000, 0.05, 250_000),
    COWORKING_SPACE("Executive Co-working", 8_000_000, 60_000, 0.04, 150_000),
    INFINITY_POOL("Infinity Pool", 10_000_000, 100_000, 0.05, 50_000),
    HELIPAD("VIP Helipad", 10_000_000, 100_000, 0.02, 200_000),
    SPA_WELLNESS("Luxury Spa & Wellness", 15_000_000, 150_000, 0.06, 400_000),
    FINE_DINING("Michelin Star Resto", 25_000_000, 250_000, 0.08, 800_000),
    BALLROOM("Grand Ballroom", 30_000_000, 300_000, 0.08, 1_000_000),
    NIGHTCLUB("Underground VIP Nightclub", 40_000_000, 400_000, 0.06, 1_500_000),
    PRIVATE_BEACH("Exclusive Private Beach", 50_000_000, 400_000, 0.10, 500_000),
    MEDICAL_CLINIC("Premium Aesthetic Clinic", 60_000_000, 500_000, 0.05, 2_000_000),
    CONVENTION_CENTER("Mega Convention Hall", 80_000_000, 800_000, 0.12, 3_000_000),
    AQUARIUM("Giant Indoor Aquarium", 100_000_000, 1_200_000, 0.08, 2_500_000),
    YACHT_MARINA("Private Yacht Marina", 120_000_000, 1_000_000, 0.10, 3_000_000),
    GOLF_COURSE("18-Hole Golf Course", 150_000_000, 1_500_000, 0.15, 4_000_000),
    SHOPPING_ARCADE("Luxury Shopping Arcade", 200_000_000, 1_500_000, 0.12, 6_000_000),
    SKI_SLOPE("Indoor Ski Slope", 250_000_000, 2_500_000, 0.15, 8_000_000),
    CASINO("Luxury Casino", 300_000_000, 3_000_000, 0.20, 15_000_000),
    THEME_PARK_ACCESS("Integrated Theme Park", 400_000_000, 4_000_000, 0.18, 12_000_000)
}

data class HotelProperty(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    val location: String, // ex: "Bali", "Las Vegas", "Maldives"
    val tier: HotelTier,
    var isConstructing: Boolean = true,
    var remainingBuildMonths: Int,
    var customRoomRate: Long, // MEKANIK GILA: Pemain menentukan harga kamar sendiri
    var builtFacilities: MutableList<HotelFacility> = mutableListOf(),
    
    // Tracking performa bulanan
    var lastMonthOccupancyRate: Double = 0.0, // 0.0 to 1.0
    var lastMonthRevenue: Long = 0L,
    var lastMonthExpense: Long = 0L,
    var activeMegaEvent: String? = null, // Event raksasa acak (G20, Formula 1)
    
    var imageUrl: String? = null,
    var targetRoomStrategy: String? = "STANDARD", // Canceled/Deprecated, replace with roomConfigs but kept for backwards compatibility
    var roomConfigs: MutableMap<String, RoomClassConfig> = mutableMapOf()
)

data class RoomClassConfig(
    var isEnabled: Boolean = false,
    var allocationPercent: Double = 0.0,
    var customPrice: Long = 0L,
    var lastMonthOccupancy: Double = 0.0,
    var lastMonthRevenue: Long = 0L
)

enum class RoomClassStrategy(val title: String, val priceMultiplier: Double, val requiredPrestige: Int) {
    STANDARD("Standard Room", 1.0, 0),
    SUPERIOR("Superior Room", 1.5, 10),
    DELUXE("Deluxe Room", 2.0, 30),
    JUNIOR_SUITE("Junior Suite", 3.5, 50),
    SUITE("Suite Room", 6.0, 80),
    PRESIDENTIAL("Presidential / Penthouse", 12.0, 150)
}

fun getSectorMultiplier(sector: String): Int {
    return when (sector.uppercase()) {
        "TECH", "APP DEV", "AI" -> 25 
        "ENTERTAINMENT", "MEDIA", "FILM", "TV STATION" -> 18
        "FINANCE", "BANK SWASTA", "PRIVATE WEALTH" -> 15
        "HEALTHCARE", "HOSPITAL", "AVIATION", "CRUISE LINE", "THEME PARK", "RESORT" -> 12
        "RETAIL", "SUPERMARKET", "MINIMARKET", "MEGA DEPT STORE" -> 10
        "CONSTRUCTION", "REAL ESTATE", "LOGISTICS", "KURIR EXPRESS" -> 10
        "CULINARY", "F&B RESTAURANT", "COFFEE SHOP" -> 8 
        "CONTENT CREATOR" -> 5 
        else -> 10
    }
}

fun getSectorForCatalogId(catalogId: String): String {
    return when (catalogId) {
        "content_creator" -> "CONTENT CREATOR"
        "fine_dining" -> "F&B RESTAURANT"
        "mid_cafe" -> "COFFEE SHOP"
        "construction" -> "CONSTRUCTION"
        "upper_realestate" -> "REAL ESTATE"
        "shop_local" -> "MINIMARKET"
        "shop_small_chain" -> "MINIMARKET"
        "shop_large_chain" -> "SUPERMARKET"
        "shop_department_store" -> "MEGA DEPT STORE"
        "mid_logistics" -> "LOGISTICS"
        "upper_tech" -> "TECH"
        "tycoon_bank" -> "BANK SWASTA"
        "tycoon_corp" -> "FINANCE"
        "media_print" -> "MEDIA"
        "media_radio" -> "ENTERTAINMENT"
        "media_tv" -> "TV STATION"
        "media_production" -> "FILM"
        "healthcare" -> "HEALTHCARE"
        "aviation_group" -> "AVIATION"
        "theme_park_holding" -> "THEME PARK"
        "hospitality_holding" -> "RESORT"
        "cruise_line_holding" -> "CRUISE LINE"
        else -> "RETAIL"
    }
}

interface BusinessEntity {
    val id: String
    val name: String
    val sectorMultiplier: Float
    val ownershipPercent: Double

    fun calculateGrossRevenue(): Long 
    fun calculateTotalExpenses(): Long 

    val internalCash: Long get() {
        return if (this is OwnedBusiness) {
            if (this.catalogId == "content_creator") this.contentCreatorCash else this.companyCash.toLong()
        } else {
            0L
        }
    }

    val sector: String get() {
        return if (this is OwnedBusiness) {
            getSectorForCatalogId(this.catalogId)
        } else {
            ""
        }
    }

    fun calculateNetIncome(): Long {
        return calculateNetMargin()
    }

    fun calculateNetMargin(): Long {
        return calculateGrossRevenue() - calculateTotalExpenses()
    }

    fun calculateBusinessValuation(): Long {
        val owned = this as? OwnedBusiness
        val catItem = if (owned != null) businessCatalog.find { it.id == owned.catalogId } else null
        val costToBuyOriginal = catItem?.costToBuy ?: 0L

        // Khusus unit bisnis Studio Film (Live Action & Animasi):
        // Valuasi = Total Nilai Pasar Aset (Fasilitas Studio + Upgrade + Portofolio Katalog Film) + Kas Internal
        if (owned != null && owned.catalogId == "media_production") {
            var uCost = 0L
            catItem?.upgrades?.forEach { upg ->
                val lvl = owned.upgradeLevels[upg.id] ?: if (owned.purchasedUpgrades.contains(upg.id)) 1 else 0
                var curCost = upg.baseCost.toDouble()
                for (i in 0 until lvl) {
                    uCost += curCost.toLong()
                    curCost *= upg.costMultiplier
                }
            }
            val studioFacilityVal = costToBuyOriginal + uCost
            // Hanya film yang sudah selesai tayang (atau sedang tayang) yang dihitung gross riilnya
            val catalogFilmAssetVal = owned.projectHistory
                .filter { it.status == "FINISHED" || it.status == "IN_THEATERS" }
                .sumOf { it.currentRevenue }
            val totalMarketAssets = studioFacilityVal + catalogFilmAssetVal
            return totalMarketAssets + this.internalCash
        }

        // 1. Modal akuisisi awal / baseline license cost (Nilai wajar awal tepat 100% harga beli)
        val baseCost = costToBuyOriginal

        // 2. Akumulasi investasi modal riil dari upgrade, armada, cabang, wahana, properti & aset
        val upgradeInvestments: Long = if (owned != null) {
            var uCost = 0L
            catItem?.upgrades?.forEach { upg ->
                val lvl = owned.upgradeLevels[upg.id] ?: if (owned.purchasedUpgrades.contains(upg.id)) 1 else 0
                var curCost = upg.baseCost.toDouble()
                for (i in 0 until lvl) {
                    uCost += curCost.toLong()
                    curCost *= upg.costMultiplier
                }
            }
            
            val specializedAssets = when (owned.catalogId) {
                "fine_dining" -> {
                    ((owned.level - 1) * 25000L) + (owned.restaurantBranches * 50000L) + (owned.michelinStars * 250000L)
                }
                "content_creator" -> {
                    var ccSum = 0L
                    var ccCost = 500.0
                    for (i in 1 until owned.level) {
                        ccSum += ccCost.toLong()
                        ccCost *= 1.18
                    }
                    val portfolioVal = owned.contentPortfolio.sumOf { item ->
                        when (item.status) {
                            ContentStatus.AVAILABLE -> item.budget + ((item.engagementScore ?: 50) * 2_000L)
                            ContentStatus.ACQUIRED_LUMP_SUM -> item.acquiredLumpSum
                            ContentStatus.LICENSED -> (item.monthlyRoyalty * 24L) + item.budget
                        }
                    }
                    ccSum + (owned.contentCreatorEmployees * 5000L) + (if (owned.contentCreatorOfficeUnlocked) 50000L else 0L) + portfolioVal
                }
                "aviation_group" -> {
                    val fleetVal = owned.airlineFleetComplex.sumOf { plane ->
                        if (!plane.isLeased) {
                            when (plane.modelId) {
                                "atr72" -> 15000000L
                                "a320" -> 35000000L
                                "b777" -> 90000000L
                                else -> 20000000L
                            }
                        } else 0L
                    }
                    val hubsVal = owned.airlineHubsComplex.size * 10000000L
                    fleetVal + hubsVal
                }
                "theme_park_holding" -> {
                    owned.themeParkBranches.sumOf { branch ->
                        20000000L + (branch.rides.size * 5000000L) + (branch.facilities.size * 1500000L)
                    }
                }
                "hospitality_holding" -> {
                    owned.hospitalityProperties.sumOf { prop ->
                        25000000L + (prop.builtFacilities.size * 3000000L)
                    }
                }
                "cruise_line_holding" -> {
                    owned.cruiseShips?.sumOf { ship ->
                        when (ship.shipClass.name) {
                            "TITAN" -> 150000000L
                            "MEGA_SHIP" -> 100000000L
                            "LARGE" -> 70000000L
                            "MIDSIZE" -> 50000000L
                            else -> 25000000L
                        }
                    } ?: 0L
                }
                "healthcare" -> {
                    owned.healthcareSubsidiaries.sumOf { unit ->
                        val unitMultiplier = when (unit.type) {
                            "HOSPITAL" -> 5000000L
                            "CLINIC" -> 1000000L
                            "INSURANCE" -> 3000000L
                            else -> 1000000L
                        }
                        unitMultiplier * unit.level
                    }
                }
                "construction" -> {
                    val crewVal = (owned.constructionData.maxCrews) * 250000L
                    val machVal = (owned.constructionData.maxMachinery) * 150000L
                    val trustBonus = (owned.constructionData.trustScore) * 50000L
                    val certVal = (owned.constructionData.safetyCertLevel) * 500000L
                    val projVal = owned.activeTenders.sumOf { it.totalContractValue.toLong() }
                    ((owned.level - 1) * 100000L) + crewVal + machVal + trustBonus + certVal + projVal
                }
                "upper_tech" -> {
                    val teamVal = (owned.softwareHouseData.uiUxDesigners * 200000L) +
                            (owned.softwareHouseData.frontendDevelopers * 300000L) +
                            (owned.softwareHouseData.backendEngineers * 400000L)
                    val infraVal = (owned.softwareHouseData.serverTier * 250000L) +
                            (if (owned.softwareHouseData.hasCiCdPipeline) 500000L else 0L) +
                            (if (owned.softwareHouseData.hasAiCopilot) 750000L else 0L) +
                            (if (owned.softwareHouseData.hasMicroservices) 1500000L else 0L) +
                            (if (owned.softwareHouseData.hasAutomatedQa) 900000L else 0L)
                    val trackVal = (owned.softwareHouseData.totalB2BRevenue / 2) + owned.softwareHouseData.totalAcquisitionValue
                    teamVal + infraVal + trackVal
                }
                "media_production" -> {
                    val streamingVal = owned.projectHistory.filter { it.status == "FINISHED" }.sumOf { (it.licenseMonthlyFee ?: 0L) * 24L }
                    val activeTheaters = owned.projectHistory.filter { it.status == "IN_THEATERS" }.sumOf { it.currentRevenue }
                    val infraVal = owned.purchasedUpgrades.size * 1500000L
                    val baseStudioVal = 500000L
                    baseStudioVal + infraVal + streamingVal + activeTheaters
                }
                "media_radio" -> {
                    val eventAssetsVal = owned.eoOwnedAssets.values.sum() * 5000L
                    val hqVal = when (owned.eoCompanyHqLevel) {
                        "INTERNATIONAL" -> 2000000L
                        "NATIONAL" -> 1000000L
                        "REGIONAL" -> 400000L
                        "OFFICE" -> 100000L
                        else -> 0L
                    }
                    eventAssetsVal + hqVal
                }
                "media_tv" -> {
                    val facilityVal = owned.tvStationData.facilities.sumOf { it.buildCost }
                    val towerVal = owned.tvStationData.unlockedTransmissions.sumOf { regionKey ->
                        try {
                            TvRegionalTransmission.valueOf(regionKey).buildCost
                        } catch (e: Exception) {
                            0L
                        }
                    }
                    val licenseVal = (if (owned.tvStationData.dewanPersCertified) 50000L else 0L) +
                            (if (owned.tvStationData.nationalBroadcastLicense) 75000L else 0L)
                    facilityVal + towerVal + licenseVal
                }
                else -> 0L
            }
            uCost + specializedAssets
        } else 0L

        // 3. Profit Growth Goodwill (Hanya mengkapitalisasi penambahan laba di atas base level 1)
        val baseMonthlyMargin = catItem?.let { maxOf(0L, it.monthlyRevenue - it.monthlyMaintenanceCost) } ?: 0L
        val currentMonthlyMargin = calculateNetMargin()
        val addedMonthlyMargin = maxOf(0L, currentMonthlyMargin - baseMonthlyMargin)
        val addedAnnualProfit = addedMonthlyMargin * 12
        val peMultiplier = (getSectorMultiplier(this.sector) / 10.0).coerceIn(1.0, 2.5)
        val profitGrowthGoodwill = (addedAnnualProfit * peMultiplier).toLong()

        val extraVal = owned?.extraValuation ?: 0L

        val baseEnterpriseValue = baseCost + upgradeInvestments + profitGrowthGoodwill
        return maxOf(costToBuyOriginal, baseEnterpriseValue) + this.internalCash + extraVal
    }

    fun calculateValuation(): Long {
        return calculateBusinessValuation()
    }

    fun calculateTotalValuation(): Long {
        return calculateBusinessValuation()
    }

    fun calculateOwnedValuation(): Long {
        return (calculateTotalValuation() * (ownershipPercent / 100.0)).toLong()
    }
}

fun getCatalogNameForId(catalogId: String): String {
    return when (catalogId) {
        "content_creator" -> "Content Creator"
        "fine_dining" -> "F&B Restaurant Chain"
        "construction" -> "Construction Firm"
        "shop_local" -> "Toko Kelontong"
        "shop_small_chain" -> "Minimarket"
        "shop_large_chain" -> "Supermarket"
        "shop_department_store" -> "Mega Dept Store"
        "mid_cafe" -> "Coffee Shop"
        "mid_logistics" -> "Express (Logistics)"
        "upper_tech" -> "App Dev"
        "upper_realestate" -> "Apartment (Property Management)"
        "tycoon_bank" -> "Bank Swasta"
        "tycoon_corp" -> "Konglomerasi Multinasional"
        "media_print" -> "Print & Digital Media"
        "media_radio" -> "Mega Event Organizer"
        "media_tv" -> "TV Station"
        "media_production" -> "Film Production"
        "healthcare" -> "Healthcare Group"
        "aviation_group" -> "Aviation Group"
        "theme_park_holding" -> "Theme Park"
        "hospitality_holding" -> "Hospitality Resort"
        "cruise_line_holding" -> "Cruise Line"
        else -> catalogId
    }
}

data class OwnedBusiness(
    val instanceId: String = java.util.UUID.randomUUID().toString(),
    val catalogId: String = "",
    val customName: String? = null,
    val level: Int = 1,
    val purchasedUpgrades: Set<String> = emptySet(),
    val upgradeLevels: Map<String, Int> = emptyMap(),
    val activeUpgrades: List<ActiveUpgrade> = emptyList(),
    val customRevenue: Long? = null,
    val projectHistory: List<MovieProject> = emptyList(),
    val extraValuation: Long = 0L,
    val synergyMultiplier: Double = 1.0,
    val studioType: String? = "LIVE_ACTION",
    val companyCash: Double = 0.0,
    val acquiredStockTicker: String? = null,
    val parentId: String? = null,
    val subsidiaries: List<OwnedBusiness> = emptyList(),
    val restaurantBranches: Int = 1,
    val michelinStars: Int = 0,
    val activeTenders: List<ConstructionProject> = emptyList(),
    val availableClientProjects: List<ConstructionProject> = emptyList(),
    val healthcareSubsidiaries: List<HealthcareUnit> = emptyList(),
    val isUpgrading: Boolean = false,
    val upgradeDelayMonths: Int = 0,
    val pendingAction: String? = null,
    val airlineFleet: List<Aircraft> = emptyList(),
    val airlineHubs: List<String> = emptyList(),
    val airlineFleetComplex: List<AircraftInstance> = emptyList(),
    val airlineHubsComplex: List<AviationHub> = emptyList(),
    val flightRoutes: List<FlightRoute> = emptyList(),
    val marketDemand: Int = 0,
    val upgradeEndTimeRealTime: Long = 0L,
    val isUpgradingRealTime: Boolean = false,
    val hasRentalDivision: Boolean = false,
    val activeEvents: List<EventProject> = emptyList(),
    val clientEventRequests: List<EventProject> = emptyList(),
    val eoReputation: Double = 50.0,
    val eoPrestige: Int = 0,
    val eoCompletedSpecialEvents: Set<String> = emptySet(),
    val eoCompanyHqLevel: String = "HOUSE", // HOUSE, OFFICE, REGIONAL, NATIONAL, INTERNATIONAL
    val eoDivisions: Set<String> = emptySet(),
    val eoOwnedAssets: Map<String, Int> = emptyMap(),
    val eoCustomAssets: List<EoCustomAsset> = emptyList(),
    val themeParkBranches: List<ThemeParkBranch> = emptyList(),
    val activeThemeParkBiddings: List<ActiveBidding> = emptyList(),
    val hospitalityProperties: List<HotelProperty> = emptyList(),
    val cruiseShips: List<CruiseShip> = emptyList(),
    val cruisePortsUnlocked: List<String> = emptyList(),
    val cruiseBrandPrestige: Int = 50,
    val contentCreatorProgress: Float = 0f,
    val contentCreatorSubscribers: Long = 100L,
    val contentCreatorEmployees: Int = 0,
    val contentCreatorOfficeUnlocked: Boolean = false,
    val contentCreatorCash: Long = 5000L,
    val contentCreatorContracts: List<ActiveCreatorContract> = emptyList(),
    val contentPortfolio: List<ContentWork> = emptyList(),
    val logisticsData: LogisticsCompanyData = LogisticsCompanyData(),
    val apartmentData: ApartmentPropertyData = ApartmentPropertyData(),
    val constructionData: ConstructionFirmData = ConstructionFirmData(),
    val softwareHouseData: SoftwareHouseCompanyData = SoftwareHouseCompanyData(),
    val bankingData: BankingCompanyData = BankingCompanyData(),
    val tvStationData: TvStationData = TvStationData(),
    val streamingData: StreamingServiceData = StreamingServiceData(),
    val activeEpidemicEvent: HealthcareEpidemicEvent? = null,
    override val ownershipPercent: Double = 100.0
) : BusinessEntity {
    override val id: String get() = instanceId
    override val name: String get() = customName ?: getCatalogNameForId(catalogId)
    override val sectorMultiplier: Float get() = when (catalogId) {
        "upper_tech", "media_tv", "streaming_service" -> 15.0f
        "tycoon_bank" -> 12.0f
        "tycoon_corp", "healthcare" -> 10.0f
        "content_creator", "media_print", "media_radio", "media_production" -> 8.0f
        "upper_realestate", "construction" -> 6.0f
        "fine_dining", "mid_cafe", "shop_local", "shop_small_chain", "shop_large_chain", "shop_department_store", "mid_logistics" -> 5.0f
        "aviation_group", "theme_park_holding", "hospitality_holding", "cruise_line_holding" -> 7.0f
        else -> 5.0f
    }

    override fun calculateGrossRevenue(): Long {
        return when (catalogId) {
            "streaming_service" -> {
                val subRev = (streamingData.subscribers * streamingData.subscriptionFee).toLong()
                val catalogViewsBonus = (streamingData.streamingCatalog.sumOf { it.views } * 0.002).toLong()
                val encodingBonus = 1.0 + (streamingData.encodingLevel * 0.05) + (streamingData.aiAlgorithmLevel * 0.04)
                val base = if (streamingData.serverOutage) (subRev * 0.4).toLong() else subRev
                ((base + catalogViewsBonus) * encodingBonus).toLong()
            }
            "content_creator" -> {
                val multiplier = 1.0 + (contentCreatorEmployees * 0.05)
                val baseRev = (contentCreatorSubscribers * 0.05).toLong()
                val contractsRev = contentCreatorContracts.sumOf { it.monthlyPayout }
                val royaltiesRev = contentPortfolio.filter { it.status == ContentStatus.LICENSED }.sumOf { it.monthlyRoyalty }
                (baseRev * multiplier).toLong() + contractsRev + royaltiesRev
            }
            "fine_dining" -> {
                val lvl = level
                when {
                    lvl in 1..10 -> 5000L + (lvl * 2000L)
                    lvl in 11..30 -> 25000L + ((lvl - 10) * 8000L)
                    lvl in 31..40 -> 185000L + ((lvl - 30) * 50000L)
                    lvl in 41..50 -> 685000L + ((lvl - 40) * 200000L)
                    else -> 685000L + ((lvl - 40) * 200000L)
                }
            }
            "construction" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseRev = cat?.monthlyRevenue ?: 150000L
                val contractRev = activeTenders.sumOf { (it.totalContractValue / maxOf(1, it.durationMonths)).toLong() }
                (baseRev * synergyMultiplier).toLong() + contractRev
            }
            "mid_logistics" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val base = cat?.monthlyRevenue ?: 25000L
                val contractsBonus = logisticsData.activeContracts.sumOf { (it.inboundPackagesPerSec * it.payoutPerPackage * 30).toLong() }
                val fleetCapacityBonus = logisticsData.fleet.sumOf { (it.type.capacity * 12).toLong() }
                val techMultiplier = 1.0 + (logisticsData.techTree.ecoLevel * 0.1) + (logisticsData.techTree.aiSpeedLevel * 0.15)
                val totalGross = ((base + contractsBonus + fleetCapacityBonus) * techMultiplier * synergyMultiplier).toLong()
                totalGross
            }
            "upper_realestate" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val dynamicRent = apartmentData.grossMonthlyRent
                val effectiveRent = if (dynamicRent > 0) dynamicRent else (cat?.monthlyRevenue ?: 120000L)
                (effectiveRent * synergyMultiplier).toLong()
            }
            "shop_local", "shop_small_chain", "shop_large_chain", "shop_department_store", "mid_cafe", "upper_tech", "tycoon_corp", "media_print", "media_tv" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                var flatRev = 0L
                var multRev = 1.0f
                cat?.upgrades?.forEach { upgrade ->
                    val lvl = upgradeLevels[upgrade.id] ?: if (purchasedUpgrades.contains(upgrade.id)) 1 else 0
                    if (lvl > 0) {
                        flatRev += upgrade.revenueFlatBoost * lvl
                        repeat(lvl) { multRev *= upgrade.revenueMultiplier }
                    }
                }
                val baseRev = ((cat?.monthlyRevenue ?: 5000L) + flatRev) * multRev
                (baseRev * synergyMultiplier).toLong()
            }
            "tycoon_bank" -> {
                val activeLoanInterest = bankingData.activeLoans
                    .filter { it.healthStatus != LoanHealthStatus.SETTLED && it.healthStatus != LoanHealthStatus.NON_PERFORMING }
                    .sumOf { it.monthlyInterestPayment }
                if (activeLoanInterest > 0L) {
                    (activeLoanInterest * synergyMultiplier).toLong()
                } else {
                    val cat = businessCatalog.find { it.id == catalogId }
                    (cat?.monthlyRevenue ?: 8500000L)
                }
            }
            "media_radio" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseRev = cat?.monthlyRevenue ?: 100000L
                val eventRev = activeEvents.sumOf { it.eoFee + it.techFee }.toLong()
                (baseRev * synergyMultiplier).toLong() + eventRev
            }
            "media_production" -> {
                val streamingRev = projectHistory.filter { it.status == "FINISHED" }.sumOf { it.licenseMonthlyFee ?: 0L }
                val activeTheatersRev = projectHistory.filter { it.status == "IN_THEATERS" }.sumOf {
                    if (it.lastMonthRevenue > 0L) it.lastMonthRevenue else (it.currentRevenue / maxOf(1, it.remainingMonths + 1))
                }
                streamingRev + activeTheatersRev
            }
            "healthcare" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseRev = cat?.monthlyRevenue ?: 4000000L
                val unitsRev = healthcareSubsidiaries.sumOf { it.monthlyRevenue }.toLong()
                (baseRev * synergyMultiplier).toLong() + unitsRev
            }
            "aviation_group" -> {
                customRevenue ?: 0L
            }
            "theme_park_holding" -> {
                themeParkBranches.sumOf { it.lastMonthRevenue }
            }
            "hospitality_holding" -> {
                hospitalityProperties.sumOf { it.lastMonthRevenue }
            }
            "cruise_line_holding" -> {
                cruiseShips?.sumOf { it.lastMonthTicketRevenue + it.lastMonthOnboardRevenue } ?: 0L
            }
            else -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseRev = cat?.monthlyRevenue ?: 1000L
                (baseRev * synergyMultiplier).toLong()
            }
        }
    }

    override fun calculateTotalExpenses(): Long {
        return when (catalogId) {
            "content_creator" -> {
                val employeeExpenses = contentCreatorEmployees * 2000L
                500L + employeeExpenses
            }
            "fine_dining" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 2000L
                val staffExpenses = level * 1200L
                baseMaint + staffExpenses
            }
            "construction" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 50000L
                val crewSalaries = constructionData.maxCrews * 8000L
                val machUpkeep = constructionData.maxMachinery * 1500L
                baseMaint + crewSalaries + machUpkeep
            }
            "shop_local" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 400L
                val staffExpenses = level * 300L
                baseMaint + staffExpenses
            }
            "shop_small_chain" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 2500L
                val staffExpenses = level * 1000L
                baseMaint + staffExpenses
            }
            "shop_large_chain" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 22000L
                val staffExpenses = level * 4000L
                baseMaint + staffExpenses
            }
            "shop_department_store" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 120000L
                val staffExpenses = level * 20000L
                baseMaint + staffExpenses
            }
            "mid_cafe" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 3500L
                val staffExpenses = level * 800L
                baseMaint + staffExpenses
            }
            "mid_logistics" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 12000L
                val staffExpenses = level * 2500L
                baseMaint + staffExpenses
            }
            "upper_tech" -> {
                val dynamicExpenses = softwareHouseData.totalMonthlyExpenses
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = if (dynamicExpenses > 0) dynamicExpenses else (cat?.monthlyMaintenanceCost ?: 40000L)
                baseMaint
            }
            "upper_realestate" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val dynamicExpenses = apartmentData.totalMonthlyExpenses
                val baseMaint = if (dynamicExpenses > 0) dynamicExpenses else (cat?.monthlyMaintenanceCost ?: 70000L)
                val staffExpenses = level * 3000L
                baseMaint + staffExpenses
            }
            "tycoon_bank" -> {
                val depositExpense = (bankingData.totalCustomerDepositsDpk * (bankingData.depositInterestRate / 12.0)).toLong()
                val overhead = (bankingData.totalCustomerDepositsDpk * 0.0005).toLong() + 25000L
                depositExpense + overhead
            }
            "tycoon_corp" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 4500000L
                val staffExpenses = level * 500000L
                baseMaint + staffExpenses
            }
            "media_print" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 6000L
                val staffExpenses = level * 1500L
                baseMaint + staffExpenses
            }
            "media_radio" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 28000L
                val staffExpenses = level * 3500L
                baseMaint + staffExpenses
            }
            "media_tv" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 15000L
                val tvExpenses = tvStationData.totalMonthlyExpenses
                baseMaint + (level * 5000L) + tvExpenses
            }
            "media_production" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 45000L
                val infraUpkeep = purchasedUpgrades.size * 12000L
                baseMaint + infraUpkeep
            }
            "healthcare" -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 1200000L
                val unitsOps = (healthcareSubsidiaries.sumOf { it.monthlyRevenue } * 0.3).toLong()
                val sdmSalaries = healthcareSubsidiaries.sumOf { unit ->
                    val salaryMultiplier = when (unit.type) {
                        "HOSPITAL" -> 25000L
                        "CLINIC" -> 8000L
                        "INSURANCE" -> 15000L
                        else -> 10000L
                    }
                    unit.level * salaryMultiplier
                }
                baseMaint + unitsOps + sdmSalaries
            }
            "aviation_group" -> {
                var totalExp = 0L
                airlineFleetComplex.forEach { plane ->
                    if (plane.status != "DELIVERING") {
                        val pDef = AVIATION_AIRCRAFT_CATALOG.find { it.id == plane.modelId }
                            ?: DUMMY_AIRCRAFTS.find { it.id == plane.modelId }
                        val baseUpkeep = if (pDef != null) {
                            pDef.price * 0.005
                        } else {
                            when (plane.modelId) {
                                "atr72" -> 100000.0
                                "a320" -> 250000.0
                                "b777" -> 600000.0
                                else -> 150000.0
                            }
                        }
                        val finalUpkeep = baseUpkeep * (1.5 - plane.condition / 200.0)
                        totalExp += finalUpkeep.toLong()
                    }
                    if (plane.isLeased) {
                        totalExp += plane.leasePrice
                    }
                }
                airlineHubsComplex.forEach { hub ->
                    var hubUpkeep = 100000L
                    hub.activeUpgrades.forEach { upgId ->
                        val addCost = when (upgId) {
                            "upg_dom" -> 50000L
                            "upg_intl_1" -> 150000L
                            "upg_vip" -> 100000L
                            "upg_intl_2" -> 300000L
                            "upg_cargo" -> 120000L
                            else -> 50000L
                        }
                        hubUpkeep += addCost
                    }
                    totalExp += hubUpkeep
                }
                val staffExpenses = airlineFleetComplex.size * 12000L
                totalExp + staffExpenses
            }
            "theme_park_holding" -> {
                val branchExp = themeParkBranches.sumOf { it.lastMonthExpense }
                val sdmSalaries = themeParkBranches.size * 25000L
                branchExp + sdmSalaries
            }
            "hospitality_holding" -> {
                val hotelExp = hospitalityProperties.sumOf { it.lastMonthExpense }
                val sdmSalaries = hospitalityProperties.size * 40000L
                hotelExp + sdmSalaries
            }
            "cruise_line_holding" -> {
                val cruiseExp = cruiseShips?.sumOf { it.lastMonthExpenses } ?: 0L
                val sdmSalaries = (cruiseShips?.size ?: 0) * 80000L
                val baseMaint = if ((cruiseShips?.size ?: 0) > 0) 100000L else 0L
                cruiseExp + sdmSalaries + baseMaint
            }
            "streaming_service" -> {
                val serverSpec = STREAMING_SERVER_TIERS.find { it.tier == streamingData.serverTier }
                    ?: STREAMING_SERVER_TIERS.first()
                val serverUpkeep = serverSpec.monthlyUpkeep
                val licensingFees = streamingData.streamingCatalog.sumOf { it.monthlyLicenseFee }
                val devBandwidth = (streamingData.subscribers * 0.15).toLong()
                val techUpkeep = (streamingData.encodingLevel + streamingData.aiAlgorithmLevel + streamingData.drmProtectionLevel + streamingData.localizationLevel) * 5000L
                serverUpkeep + licensingFees + devBandwidth + techUpkeep
            }
            else -> {
                val cat = businessCatalog.find { it.id == catalogId }
                val baseMaint = cat?.monthlyMaintenanceCost ?: 100L
                val staffExpenses = level * 100L
                baseMaint + staffExpenses
            }
        }
    }
}

enum class BiddingPhase {
    WAITING_INITIAL,
    OWNER_COUNTERED,
    WAITING_REPLY,
    DEAL_REACHED,
    REJECTED
}

data class ActiveBidding(
    val id: String = java.util.UUID.randomUUID().toString(),
    val landType: ThemeParkLandType,
    var phase: BiddingPhase = BiddingPhase.WAITING_INITIAL,
    var monthsLeft: Int = landType.biddingMonths,
    var currentAskingPrice: Long = landType.basePrice,
    var playerOffer: Long = landType.basePrice
)

enum class ThemeParkLandType(val locationName: String, val maxSlots: Int, val basePrice: Long, val biddingMonths: Int) {
    // Kelas Lokal/Kecil (20-30 Slot)
    BOGOR("Bogor, ID", 20, 150_000_000, 4),
    BANDUNG("Bandung, ID", 25, 200_000_000, 5),
    BATAM("Batam, ID", 30, 250_000_000, 5),
    PEKANBARU("Pekanbaru, ID", 20, 160_000_000, 4),
    
    // Kelas Menengah (30-50 Slot)
    JAKARTA("Jakarta, ID", 35, 500_000_000, 8),
    BALI("Bali, ID", 40, 750_000_000, 10),
    MANILA("Manila, PH", 35, 450_000_000, 8),
    SEOUL("Seoul, KR", 50, 900_000_000, 10),
    
    // Kelas Berat/Besar (50-70 Slot)
    TEXAS("Texas, USA", 60, 2_000_000_000, 14),
    OSAKA("Osaka, JP", 65, 2_500_000_000, 14),
    SHANGHAI("Shanghai, CN", 70, 3_000_000_000, 16),
    
    // Kelas Raksasa/Sultan (70-100 Slot)
    FLORIDA("Orlando, USA", 80, 5_000_000_000, 18),
    CALIFORNIA("Anaheim, USA", 90, 7_500_000_000, 24),
    PARIS("Paris, FR", 85, 6_000_000_000, 24),
    TOKYO("Tokyo, JP", 100, 10_000_000_000, 30)
}

enum class RideTier(val level: Int, val cost: Long, val buildMonths: Int, val description: String) {
    TIER_1(1, 1_500_000, 2, "Kios / Atraksi Minor"),
    TIER_2(2, 5_500_000, 3, "Atraksi Keluarga Dasar"),
    TIER_3(3, 10_000_000, 4, "Wahana Kelas Menengah"),
    TIER_4(4, 25_000_000, 6, "Roller Coaster Mini / Dark Ride"),
    TIER_5(5, 50_000_000, 8, "Atraksi Tema Standar"),
    TIER_6(6, 80_000_000, 10, "Wahana Ekstrem Besar"),
    TIER_7(7, 100_000_000, 12, "Atraksi Ikonik Taman"),
    TIER_8(8, 160_000_000, 15, "Zona Tema Eksklusif"),
    TIER_9(9, 200_000_000, 18, "Hyper Coaster / 4D Ride"),
    TIER_10(10, 300_000_000, 24, "Mega Wahana Revolusioner")
}

data class ThemeParkRide(
    val id: String = java.util.UUID.randomUUID().toString(),
    var name: String,
    val ipFilmId: String? = null,
    var isConstructing: Boolean = true,
    var constructionMonthsLeft: Int,
    val tierDescription: String = "Wahana Kustom",
    var imageUrl: String? = null,
    val cost: Long = 0L,
    var zoneName: String? = null,
    var ipThemeTitle: String? = null,
    var ipThemeScore: Int? = null,
    var isPaused: Boolean = false,
    var isUnderMaintenance: Boolean = false,
    var maintenanceMonthsLeft: Int = 0
) {
    val estimatedIncome: Long get() = (cost * 0.15).toLong()
    val maintenanceCost: Long get() = (cost * 0.05).toLong()
    val baseMonthlyVisitors: Int get() = (cost / 50_000).toInt() + 100
}

data class ThemeParkBranch(
    val id: String = java.util.UUID.randomUUID().toString(),
    val locationName: String,
    var customName: String? = null,
    val landType: ThemeParkLandType,
    var remainingBiddingMonths: Int = 0,
    var rides: MutableList<ThemeParkRide> = mutableListOf(),
    var imageUrl: String? = null,
    var isLaunched: Boolean = false,
    var hasHypeMarketing: Boolean = false,
    var hypeMonthsLeft: Int = 0,
    var lastMonthVisitors: Int = 0,
    var activeDisaster: String? = null,
    var priceRegular: Long = 15L,
    var priceTerusan: Long = 35L,
    var priceVIP: Long = 100L,
    var priceFamily: Long = 80L,
    var lastMonthProfit: Long = 0L,
    var lastMonthRevenue: Long = 0L,
    var lastMonthExpense: Long = 0L,
    var parkZones: MutableList<String> = mutableListOf(),
    var activeAdName: String? = null,
    var adMonthsLeft: Int = 0,
    var adBoostMultiplier: Double = 1.0,
    var facilities: List<ThemeParkFacility> = emptyList()
) {
    val minRidesToLaunch: Int get() = when {
        landType.maxSlots <= 30 -> 5
        landType.maxSlots <= 60 -> 10
        else -> 20
    }

    val currentStatus: String get() {
        if (remainingBiddingMonths > 0) return "Riset & Akuisisi"
        if (!isLaunched) {
            if (rides.isEmpty()) return "Perencanaan Lahan"
            val finishedRidesCount = rides.count { !it.isConstructing }
            if (finishedRidesCount < minRidesToLaunch) return "Pembangunan Infrastruktur"
            return "Siap Launching"
        }
        if (hypeMonthsLeft > 0) return "Hype Season!"
        return "Beroperasi"
    }
}

data class ThemeParkFacility(
    val id: String = java.util.UUID.randomUUID().toString(),
    val catalogId: String,
    val name: String,
    val buildCost: Long,
    val maintenanceCost: Long,
    val fnbBoostPercent: Double, // Persentase kenaikan jajan pengunjung
    val appealBoost: Double,     // Tambahan poin daya tarik taman
    val zoneName: String = "Public Area",
    val imageUrl: String? = null // Link gambar kustom
)
