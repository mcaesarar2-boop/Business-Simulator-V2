package com.example.data

import java.util.UUID

// =========================================================================
// 1. TIPE UNIT APARTEMEN & KATEGORI
// =========================================================================
enum class ApartmentUnitType(
    val displayName: String,
    val iconEmoji: String,
    val baseFairRent: Long,
    val defaultInitialUnits: Int,
    val unlockCost: Long,
    val description: String,
    val targetSatisfactionFor100Percent: Double = 4.0
) {
    STUDIO(
        displayName = "Tipe Studio",
        iconEmoji = "🛋️",
        baseFairRent = 500L,
        defaultInitialUnits = 60,
        unlockCost = 0L,
        description = "Unit minimalis untuk mahasiswa & profesional muda. Permintaan sangat tinggi & cepat terisi.",
        targetSatisfactionFor100Percent = 3.5
    ),
    TWO_BEDROOM(
        displayName = "Tipe 2-Bedroom",
        iconEmoji = "🛏️",
        baseFairRent = 1_200L,
        defaultInitialUnits = 30,
        unlockCost = 50_000L,
        description = "Unit keluarga muda menengah. Okupansi stabil jika fasilitas keamanan & parkir baik.",
        targetSatisfactionFor100Percent = 4.0
    ),
    PENTHOUSE(
        displayName = "Penthouse Mewah",
        iconEmoji = "👑",
        baseFairRent = 3_500L,
        defaultInitialUnits = 8,
        unlockCost = 150_000L,
        description = "Unit eksklusif lantai atas dengan view kota. Margin tinggi, namun menuntut kepuasan 4.5+ ⭐.",
        targetSatisfactionFor100Percent = 4.5
    ),
    COMMERCIAL_RETAIL(
        displayName = "Ruko Komersial Ground",
        iconEmoji = "🏬",
        baseFairRent = 5_000L,
        defaultInitialUnits = 4,
        unlockCost = 100_000L,
        description = "Disewa untuk minimarket, laundry, dan cafe. Menambah kenyamanan dan cuan stabil.",
        targetSatisfactionFor100Percent = 4.0
    )
}

data class ApartmentUnitCategoryData(
    val type: ApartmentUnitType,
    val totalUnits: Int,
    val occupiedUnits: Int,
    val monthlyRentPrice: Long, // Harga sewa yang diset player
    val isUnlocked: Boolean = true
) {
    val occupancyRateFraction: Float
        get() = if (totalUnits > 0) (occupiedUnits.toFloat() / totalUnits.toFloat()).coerceIn(0f, 1f) else 0f

    val totalMonthlyRent: Long
        get() = occupiedUnits * monthlyRentPrice
}

// =========================================================================
// 2. FASILITAS & UPGRADE PROPERTI
// =========================================================================
enum class ApartmentFacilityType(
    val displayName: String,
    val iconEmoji: String,
    val installCost: Long,
    val monthlyUpkeep: Long,
    val satisfactionBonus: Double, // Menambah batas maksimal kepuasan penghuni
    val rentToleranceBoost: Long, // Menambah batas harga sewa wajar yang ditoleransi
    val description: String,
    val incidentReductionPercent: Int = 0
) {
    ROOFTOP_POOL(
        displayName = "Kolam Renang Rooftop & Sky Deck",
        iconEmoji = "🏊‍♂️",
        installCost = 150_000L,
        monthlyUpkeep = 4_500L,
        satisfactionBonus = 0.8,
        rentToleranceBoost = 400L,
        description = "Fasilitas mewah favorit penghuni Penthouse & 2-BR (+0.8 ⭐ Kepuasan)."
    ),
    FITNESS_GYM(
        displayName = "Pusat Kebugaran & Gym Modern",
        iconEmoji = "🏋️",
        installCost = 65_000L,
        monthlyUpkeep = 2_000L,
        satisfactionBonus = 0.5,
        rentToleranceBoost = 250L,
        description = "Peralatan cardio & beban lengkap (+0.5 ⭐ Kepuasan)."
    ),
    SECURITY_24H(
        displayName = "Smart Security 24 Jam & CCTV AI",
        iconEmoji = "🛡️",
        installCost = 45_000L,
        monthlyUpkeep = 2_500L,
        satisfactionBonus = 0.4,
        rentToleranceBoost = 200L,
        description = "Petugas sigap, access card gate, -40% frekuensi laporan rusak.",
        incidentReductionPercent = 40
    ),
    FIBER_INTERNET(
        displayName = "High-Speed Fiber & Smart Home",
        iconEmoji = "📶",
        installCost = 35_000L,
        monthlyUpkeep = 1_500L,
        satisfactionBonus = 0.4,
        rentToleranceBoost = 180L,
        description = "Internet gigabit tanpa putus untuk penghuni WFH & milenial."
    ),
    GREEN_PARK(
        displayName = "Taman Hijau & Playground",
        iconEmoji = "🌳",
        installCost = 50_000L,
        monthlyUpkeep = 1_200L,
        satisfactionBonus = 0.4,
        rentToleranceBoost = 200L,
        description = "Area terbuka asri, jogging track & tempat bermain anak."
    ),
    AUTO_PARKING(
        displayName = "Gedung Parkir Bawah Tanah Otomatis",
        iconEmoji = "🚗",
        installCost = 85_000L,
        monthlyUpkeep = 2_800L,
        satisfactionBonus = 0.5,
        rentToleranceBoost = 300L,
        description = "Sistem parkir valet robotik tanpa antri (+0.5 ⭐ Kepuasan)."
    ),
    SKY_LOUNGE(
        displayName = "Sky Lounge & Co-Working Cafe",
        iconEmoji = "☕",
        installCost = 120_000L,
        monthlyUpkeep = 3_500L,
        satisfactionBonus = 0.6,
        rentToleranceBoost = 350L,
        description = "Ruang santai eksklusif dengan panorama kota 360 derajat."
    )
}

// =========================================================================
// 3. LAPORAN PENGHUNI & MAINTENANCE EVENTS
// =========================================================================
data class ApartmentIncident(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val iconEmoji: String,
    val affectedFloor: Int,
    val affectedRoomIndex: Int,
    val repairCost: Long,
    val satisfactionPenalty: Double,
    val moveOutTenantCount: Int,
    val remainingSeconds: Int = 40,
    val totalDurationSeconds: Int = 40,
    val isResolved: Boolean = false
)

// =========================================================================
// 4. MODEL REPRESENTASI GRID LANTAI / KAMAR
// =========================================================================
data class BuildingRoomCell(
    val floorNumber: Int,
    val roomIndex: Int, // 0..roomsPerFloor-1
    val roomCode: String,
    val unitType: ApartmentUnitType,
    val isOccupied: Boolean,
    val hasIncident: Boolean = false
)

// =========================================================================
// 5. MASTER DATA APARTMENT PROPERTY
// =========================================================================
data class ApartmentPropertyData(
    val internalCash: Long = 80_000L,
    val buildingName: String = "Apartment (Property Management)",
    val buildingFloors: Int = 8, // Lantai apartemen (bisa diekspansi)
    val unitCategories: List<ApartmentUnitCategoryData> = defaultUnitCategories(),
    val installedFacilities: List<ApartmentFacilityType> = listOf(ApartmentFacilityType.SECURITY_24H),
    val activeIncidents: List<ApartmentIncident> = emptyList(),
    val tenantSatisfaction: Double = 4.3, // 1.0 - 5.0 ⭐
    val totalRentCollectedLifetime: Long = 0L,
    val totalMaintenanceSpentLifetime: Long = 0L,
    val lastIncidentTimestamp: Long = 0L
) {
    val totalUnits: Int
        get() = unitCategories.filter { it.isUnlocked }.sumOf { it.totalUnits }

    val totalOccupiedUnits: Int
        get() = unitCategories.filter { it.isUnlocked }.sumOf { it.occupiedUnits }

    val occupancyRatePercentage: Double
        get() = if (totalUnits > 0) {
            (totalOccupiedUnits.toDouble() / totalUnits.toDouble()) * 100.0
        } else 0.0

    val grossMonthlyRent: Long
        get() = unitCategories.filter { it.isUnlocked }.sumOf { it.totalMonthlyRent }

    val monthlyFacilityUpkeep: Long
        get() = installedFacilities.sumOf { it.monthlyUpkeep }

    val monthlyStaffBaseCost: Long
        get() = buildingFloors * 2_500L

    val totalMonthlyExpenses: Long
        get() = monthlyFacilityUpkeep + monthlyStaffBaseCost

    val netMonthlyCashflow: Long
        get() = grossMonthlyRent - totalMonthlyExpenses

    val maxSatisfactionCap: Double
        get() {
            var cap = 3.2
            for (fac in installedFacilities) {
                cap += fac.satisfactionBonus
            }
            return cap.coerceAtMost(5.0)
        }
}

// =========================================================================
// 6. DEFAULT DATA & GENERATORS
// =========================================================================
fun defaultUnitCategories(): List<ApartmentUnitCategoryData> {
    return listOf(
        ApartmentUnitCategoryData(
            type = ApartmentUnitType.STUDIO,
            totalUnits = 60,
            occupiedUnits = 52,
            monthlyRentPrice = 500L,
            isUnlocked = true
        ),
        ApartmentUnitCategoryData(
            type = ApartmentUnitType.TWO_BEDROOM,
            totalUnits = 30,
            occupiedUnits = 24,
            monthlyRentPrice = 1_200L,
            isUnlocked = true
        ),
        ApartmentUnitCategoryData(
            type = ApartmentUnitType.PENTHOUSE,
            totalUnits = 8,
            occupiedUnits = 5,
            monthlyRentPrice = 3_500L,
            isUnlocked = false
        ),
        ApartmentUnitCategoryData(
            type = ApartmentUnitType.COMMERCIAL_RETAIL,
            totalUnits = 4,
            occupiedUnits = 3,
            monthlyRentPrice = 5_000L,
            isUnlocked = false
        )
    )
}

object ApartmentDataGenerator {
    private val incidentPool = listOf(
        Triple("Pipa Air Bocor di Lantai", "Pipa utama saluran air rembes ke koridor dan merusak lantai.", "🚰") to (3_500L to 0.4),
        Triple("Lift Penumpang Utama Macet", "Lift macet di jam sibuk pagi, penghuni harus antri tangga darurat.", "🛗") to (6_500L to 0.6),
        Triple("AC Sentral Unit Mengalami Mati", "Kompresor AC jebol akibat cuaca panas terik.", "❄️") to (2_800L to 0.3),
        Triple("Genset Backup Listrik Rusak", "Genset otomatis gagal menyala saat tegangan PLN drop.", "⚡") to (8_000L to 0.7),
        Triple("Lampu Koridor & Parkir Padam", "Korsleting instalasi kabel di area basement.", "💡") to (2_000L to 0.2),
        Triple("Keluhan Suara & Kebersihan Sampah", "Chute tempat sampah mampet dan menimbulkan bau tak sedap.", "🗑️") to (1_500L to 0.3),
        Triple("Pintu Otomatis Lobi Error", "Sensor gerak gerbang utama tidak merespon.", "🚪") to (3_000L to 0.3)
    )

    fun generateRandomIncident(buildingFloors: Int, totalUnits: Int): ApartmentIncident {
        val (info, costPenalty) = incidentPool.random()
        val floor = (1..buildingFloors).random()
        val roomIdx = (0..9).random()
        val (cost, penalty) = costPenalty

        return ApartmentIncident(
            id = UUID.randomUUID().toString(),
            title = "${info.first} $floor",
            description = info.second,
            iconEmoji = info.third,
            affectedFloor = floor,
            affectedRoomIndex = roomIdx,
            repairCost = cost,
            satisfactionPenalty = penalty,
            moveOutTenantCount = (1..3).random(),
            remainingSeconds = 35,
            totalDurationSeconds = 35,
            isResolved = false
        )
    }

    /**
     * Generate visual room cells for the interactive building grid.
     */
    fun generateBuildingGrid(
        floors: Int,
        categories: List<ApartmentUnitCategoryData>,
        incidents: List<ApartmentIncident>
    ): List<BuildingRoomCell> {
        val cells = mutableListOf<BuildingRoomCell>()
        val totalOccupied = categories.filter { it.isUnlocked }.sumOf { it.occupiedUnits }
        val totalUnits = categories.filter { it.isUnlocked }.sumOf { it.totalUnits }
        val overallOccRate = if (totalUnits > 0) totalOccupied.toDouble() / totalUnits.toDouble() else 0.0

        val incidentMap = incidents.associateBy { "${it.affectedFloor}_${it.affectedRoomIndex}" }

        // Rooms per floor: 10
        val roomsPerFloor = 10
        for (floor in floors downTo 1) {
            val isPenthouseFloor = floor >= floors && categories.any { it.type == ApartmentUnitType.PENTHOUSE && it.isUnlocked }
            val isGroundFloor = floor == 1 && categories.any { it.type == ApartmentUnitType.COMMERCIAL_RETAIL && it.isUnlocked }

            for (roomIdx in 0 until roomsPerFloor) {
                val unitType = when {
                    isPenthouseFloor && roomIdx < 4 -> ApartmentUnitType.PENTHOUSE
                    isGroundFloor && roomIdx < 4 -> ApartmentUnitType.COMMERCIAL_RETAIL
                    floor >= (floors / 2) -> ApartmentUnitType.TWO_BEDROOM
                    else -> ApartmentUnitType.STUDIO
                }

                val category = categories.find { it.type == unitType }
                val catOccupancyRate = category?.occupancyRateFraction ?: overallOccRate.toFloat()
                
                // Deterministic pseudo-randomness based on floor and roomIdx
                val seed = (floor * 31 + roomIdx * 17) % 100
                val isOccupied = (category?.isUnlocked == true) && (seed < (catOccupancyRate * 100))

                val incidentKey = "${floor}_${roomIdx}"
                val hasIncident = incidentMap.containsKey(incidentKey)

                val roomCode = when (unitType) {
                    ApartmentUnitType.PENTHOUSE -> "PH-${floor}0${roomIdx + 1}"
                    ApartmentUnitType.COMMERCIAL_RETAIL -> "RUKO-${roomIdx + 1}"
                    else -> "${floor}0${roomIdx + 1}"
                }

                cells.add(
                    BuildingRoomCell(
                        floorNumber = floor,
                        roomIndex = roomIdx,
                        roomCode = roomCode,
                        unitType = unitType,
                        isOccupied = isOccupied,
                        hasIncident = hasIncident
                    )
                )
            }
        }
        return cells
    }
}
