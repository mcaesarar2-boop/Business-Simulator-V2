package com.example.data

import java.util.UUID

// ===================================================
// GANTI DATA LAMA DENGAN DATA BARU INI:
// ===================================================

object CruiseCatalogData {

// 1. KELAS KAPAL (Skala Miliaran Dolar)
val SHIP_CLASSES = listOf(
    mapOf(
        "id" to "class_yacht",
        "name" to "Luxury Expedition Yacht",
        "maxPax" to 500,
        "basePrice" to 150_000_000L, 
        "buildMonths" to 12,
        "description" to "Kapal kecil ultra-mewah untuk rute eksotis dan pelabuhan terpencil."
    ),
    mapOf(
        "id" to "class_small",
        "name" to "Premium Small Cruiser",
        "maxPax" to 1500,
        "basePrice" to 450_000_000L, 
        "buildMonths" to 16,
        "description" to "Fokus pada layanan butik premium dengan fasilitas eksklusif."
    ),
    mapOf(
        "id" to "class_midsize",
        "name" to "Midsize Ocean Liner",
        "maxPax" to 3000,
        "basePrice" to 850_000_000L, 
        "buildMonths" to 20,
        "description" to "Standar emas industri pelayaran masa kini."
    ),
    mapOf(
        "id" to "class_large",
        "name" to "Large Resort Ship",
        "maxPax" to 5000,
        "basePrice" to 1_350_000_000L, 
        "buildMonths" to 24,
        "description" to "Kota wisata terapung dengan taman air dan teater raksasa."
    ),
    mapOf(
        "id" to "class_mega",
        "name" to "Mega-Ship Class",
        "maxPax" to 7000,
        "basePrice" to 1_800_000_000L, 
        "buildMonths" to 30,
        "description" to "Kapal raksasa setara Oasis-Class. Mesin pencetak uang utama."
    ),
    mapOf(
        "id" to "class_titan",
        "name" to "Titan Record-Breaker",
        "maxPax" to 9500, 
        "basePrice" to 2_500_000_000L, 
        "buildMonths" to 42,
        "description" to "Kapal terbesar dalam sejarah peradaban manusia. Sebuah mahakarya maritim."
    )
)

// 2. MITRA GALANGAN KAPAL
val SHIPYARDS = listOf(
    mapOf("id" to "sy_hyundai", "name" to "Hyundai Heavy Industries", "country" to "KOR", "costMod" to -0.10, "timeMod" to -2, "maintMod" to 0.0, "perk" to "Murah & Cepat"),
    mapOf("id" to "sy_samsung", "name" to "Samsung Heavy Industries", "country" to "KOR", "costMod" to 0.05, "timeMod" to 0, "maintMod" to -0.15, "perk" to "Smart Ship (Maint. Murah)"),
    mapOf("id" to "sy_meyer", "name" to "Meyer Werft", "country" to "GER", "costMod" to 0.15, "timeMod" to 2, "maintMod" to -0.05, "perk" to "Kualitas Jerman (Prestige +20%)"),
    mapOf("id" to "sy_fincantieri", "name" to "Fincantieri", "country" to "ITA", "costMod" to 0.25, "timeMod" to 0, "maintMod" to 0.0, "perk" to "Master Luxury (VIP Demand +30%)"),
    mapOf("id" to "sy_chantiers", "name" to "Chantiers de l'Atlantique", "country" to "FRA", "costMod" to 0.10, "timeMod" to 4, "maintMod" to 0.0, "perk" to "Satu-satunya untuk Titan Class")
)

// 3. FASILITAS MEGA-KAPAL
val FACILITIES = listOf(
    mapOf("id" to "fac_pool", "name" to "Infinity Pool & Cabanas", "cost" to 25_000_000L, "maint" to 100_000L, "spendBoost" to 5, "appeal" to 10),
    mapOf("id" to "fac_theater", "name" to "Broadway Theater", "cost" to 45_000_000L, "maint" to 150_000L, "spendBoost" to 15, "appeal" to 15),
    mapOf("id" to "fac_casino", "name" to "Royale Sea Casino", "cost" to 85_000_000L, "maint" to 300_000L, "spendBoost" to 80, "appeal" to 15),
    mapOf("id" to "fac_spa", "name" to "Panoramic Ocean Spa", "cost" to 30_000_000L, "maint" to 80_000L, "spendBoost" to 35, "appeal" to 15),
    mapOf("id" to "fac_waterpark", "name" to "Mega Deck Waterpark", "cost" to 60_000_000L, "maint" to 250_000L, "spendBoost" to 20, "appeal" to 30),
    mapOf("id" to "fac_bionic", "name" to "Bionic Robot Bar", "cost" to 20_000_000L, "maint" to 50_000L, "spendBoost" to 25, "appeal" to 20),
    mapOf("id" to "fac_icerink", "name" to "Indoor Ice Skating", "cost" to 40_000_000L, "maint" to 180_000L, "spendBoost" to 10, "appeal" to 25),
    mapOf("id" to "fac_gokart", "name" to "Top-Deck Go-Kart", "cost" to 55_000_000L, "maint" to 200_000L, "spendBoost" to 40, "appeal" to 35),
    mapOf("id" to "fac_rollercoaster", "name" to "Sea Rollercoaster", "cost" to 150_000_000L, "maint" to 500_000L, "spendBoost" to 35, "appeal" to 60),
    mapOf("id" to "fac_submarine", "name" to "Deep Sea Submarine", "cost" to 90_000_000L, "maint" to 300_000L, "spendBoost" to 70, "appeal" to 50),
    mapOf("id" to "fac_michelin", "name" to "Michelin Star Dining", "cost" to 35_000_000L, "maint" to 120_000L, "spendBoost" to 60, "appeal" to 25),
    mapOf("id" to "fac_botanic", "name" to "Central Park Garden", "cost" to 70_000_000L, "maint" to 250_000L, "spendBoost" to 15, "appeal" to 40),
    mapOf("id" to "fac_lng", "name" to "LNG Green Engine", "cost" to 150_000_000L, "maint" to -150_000L, "spendBoost" to 0, "appeal" to 10)
)

// 4. PELABUHAN GLOBAL (HUBS & DESTINATIONS)
val PORTS = listOf(
    mapOf("id" to "port_miami", "name" to "PortMiami, USA", "type" to "HUB", "portFee" to 250_000L, "baseDemand" to 20000, "prestigeReq" to 0),
    mapOf("id" to "port_canaveral", "name" to "Port Canaveral, USA", "type" to "HUB", "portFee" to 180_000L, "baseDemand" to 15000, "prestigeReq" to 0),
    mapOf("id" to "port_nassau", "name" to "Nassau, Bahamas", "type" to "DESTINATION", "portFee" to 65_000L, "baseDemand" to 12000, "prestigeReq" to 0),
    mapOf("id" to "port_cozumel", "name" to "Cozumel, Mexico", "type" to "DESTINATION", "portFee" to 55_000L, "baseDemand" to 10000, "prestigeReq" to 0),
    mapOf("id" to "port_juneau", "name" to "Juneau, Alaska", "type" to "DESTINATION", "portFee" to 120_000L, "baseDemand" to 8000, "prestigeReq" to 30),
    mapOf("id" to "port_southampton", "name" to "Southampton, UK", "type" to "HUB", "portFee" to 160_000L, "baseDemand" to 13000, "prestigeReq" to 20),
    mapOf("id" to "port_barcelona", "name" to "Barcelona, Spain", "type" to "HUB", "portFee" to 190_000L, "baseDemand" to 14000, "prestigeReq" to 15),
    mapOf("id" to "port_venice", "name" to "Venice, Italy", "type" to "DESTINATION", "portFee" to 220_000L, "baseDemand" to 9000, "prestigeReq" to 40),
    mapOf("id" to "port_santorini", "name" to "Santorini, Greece", "type" to "DESTINATION", "portFee" to 140_000L, "baseDemand" to 8500, "prestigeReq" to 35),
    mapOf("id" to "port_monaco", "name" to "Monte Carlo, Monaco", "type" to "YACHT_ONLY", "portFee" to 350_000L, "baseDemand" to 5000, "prestigeReq" to 85),
    mapOf("id" to "port_singapore", "name" to "Marina Bay, Singapore", "type" to "HUB", "portFee" to 150_000L, "baseDemand" to 12000, "prestigeReq" to 10),
    mapOf("id" to "port_bali", "name" to "Benoa Port, Bali - ID", "type" to "HUB", "portFee" to 90_000L, "baseDemand" to 10000, "prestigeReq" to 15),
    mapOf("id" to "port_yokohama", "name" to "Yokohama, Japan", "type" to "HUB", "portFee" to 200_000L, "baseDemand" to 13000, "prestigeReq" to 35),
    mapOf("id" to "port_sydney", "name" to "Sydney Harbour, AUS", "type" to "HUB", "portFee" to 220_000L, "baseDemand" to 11000, "prestigeReq" to 25),
    mapOf("id" to "port_rajaampat", "name" to "Raja Ampat, ID", "type" to "YACHT_ONLY", "portFee" to 85_000L, "baseDemand" to 4000, "prestigeReq" to 75),
    mapOf("id" to "port_komodo", "name" to "Komodo Island, ID", "type" to "YACHT_ONLY", "portFee" to 75_000L, "baseDemand" to 4500, "prestigeReq" to 70),
    mapOf("id" to "port_dubai", "name" to "Dubai Harbour, UAE", "type" to "HUB", "portFee" to 300_000L, "baseDemand" to 15000, "prestigeReq" to 55)
)

}

// Enums and keys for Shipyard
enum class ShipyardId(
    val title: String,
    val costModifier: Double,
    val buildTimeReduction: Int,
    val maintenanceModifier: Double,
    val prestigeBonus: Int,
    val description: String
) {
    HYUNDAI("Hyundai Heavy Industries", -0.10, 2, 0.0, 0, "Murah & Cepat"),
    SAMSUNG("Samsung Heavy Industries", 0.05, 0, -0.15, 0, "Smart Ship (Maint. Murah)"),
    MEYER_WERFT("Meyer Werft", 0.15, -2, -0.05, 20, "Kualitas Jerman (Prestige +20%)"),
    FINCANTIERI("Fincantieri", 0.25, 0, 0.0, 30, "Master Luxury (VIP Demand +30%)"),
    CHANTIERS("Chantiers de l'Atlantique", 0.10, -4, 0.0, 15, "Satu-satunya untuk Titan Class")
}

// Enums and keys for Ship Classes
enum class CruiseShipClass(
    val title: String,
    val maxPax: Int,
    val basePrice: Long,
    val baseBuildTime: Int,
    val requiredShipyard: ShipyardId?
) {
    YACHT("Luxury Expedition Yacht", 500, 150_000_000L, 12, null),
    SMALL("Premium Small Cruiser", 1500, 450_000_000L, 16, null),
    MIDSIZE("Midsize Ocean Liner", 3000, 850_000_000L, 20, null),
    LARGE("Large Resort Ship", 5000, 1_350_000_000L, 24, null),
    MEGA_SHIP("Mega-Ship Class", 7000, 1_800_000_000L, 30, null),
    TITAN("Titan Record-Breaker", 9500, 2_500_000_000L, 42, ShipyardId.CHANTIERS)
}

// Moduler Facilities catalog
data class CruiseFacility(
    val id: String,
    val name: String,
    val cost: Long,
    val maintenance: Long,
    val buffDemand: Double,   // percentage increase in demand e.g. 0.25
    val buffRevenue: Long,    // Flat cash generated per guest onboard spend
    val prestige: Int,
    val description: String
)

val CRUISE_FACILITIES_CATALOG = CruiseCatalogData.FACILITIES.map { map ->
    val id = map["id"] as String
    val name = map["name"] as String
    val cost = map["cost"] as Long
    val maint = map["maint"] as Long
    val spendBoost = map["spendBoost"] as Int
    val appeal = map["appeal"] as Int
    CruiseFacility(
        id = id,
        name = name,
        cost = cost,
        maintenance = maint,
        buffDemand = appeal / 100.0,
        buffRevenue = spendBoost.toLong(),
        prestige = appeal,
        description = "Meningkatkan daya tarik sebesar $appeal% dan pendapatan onboard sebesar $${spendBoost} per tamu."
    )
}

// Ports and routes
data class CruisePort(
    val id: String,
    val name: String,
    val country: String,
    val portFee: Long,
    val baseDemand: Int,
    val requiredPrestige: Int,
    val isYachtOnly: Boolean, // Raja Ampat is yacht-only!
    val description: String
)

val CRUISE_PORTS_CATALOG = CruiseCatalogData.PORTS.map { map ->
    val id = map["id"] as String
    val name = map["name"] as String
    val type = map["type"] as String
    val portFee = map["portFee"] as Long
    val baseDemand = map["baseDemand"] as Int
    val prestigeReq = map["prestigeReq"] as Int
    val country = name.substringAfter(", ").trim()
    CruisePort(
        id = id,
        name = name,
        country = country,
        portFee = portFee,
        baseDemand = baseDemand,
        requiredPrestige = prestigeReq,
        isYachtOnly = type == "YACHT_ONLY",
        description = "Tipe: $type | Kebutuhan Reputasi: $prestigeReq"
    )
}

// Cruise Ship Instance Model
data class CruiseShip(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val customImageUrl: String? = null,
    val shipClass: CruiseShipClass,
    val shipyard: ShipyardId,
    val maxPax: Int,
    val pricePaid: Long,
    val builtFacilities: List<String> = emptyList(), // Facility ids
    var hullHealth: Double = 100.0,
    var engineHealth: Double = 100.0,
    
    // Delivery status
    val monthsUntilDelivery: Int = 0,
    val totalBuildTime: Int = 10,
    val constructionProgressPercent: Int = 0,
    
    var targetTicketPrice: Long = 150L,
    var ticketPriceRegular: Long = 150L,
    var ticketPriceVip: Long = 300L,
    var ticketPriceVvip: Long = 600L,
    var ticketPriceGrandSuite: Long = 1200L,
    var assignedPortId: String? = null,
    
    var monthsSinceDrydock: Int = 0,
    var isUnderDrydock: Boolean = false,
    var drydockMonthsRemaining: Int = 0,
    
    // Monthly statistics
    var lastMonthPassengers: Int = 0,
    var lastMonthPassengersRegular: Int = 0,
    var lastMonthPassengersVip: Int = 0,
    var lastMonthPassengersVvip: Int = 0,
    var lastMonthPassengersGrandSuite: Int = 0,
    var lastMonthTicketRevenue: Long = 0L,
    var lastMonthOnboardRevenue: Long = 0L,
    var lastMonthExpenses: Long = 0L,
    
    var lastMonthAccidentOccurred: Boolean = false,
    var lastMonthAccidentReport: String? = null
)

