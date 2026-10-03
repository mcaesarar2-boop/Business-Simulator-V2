package com.example.data

import java.util.UUID

enum class TvFacilityType(
    val displayName: String,
    val description: String,
    val buildCost: Long,
    val monthlyUpkeep: Long,
    val supportedGenres: List<String>
) {
    NEWSROOM(
        displayName = "Newsroom & Studio Berita",
        description = "Studio redaksi khusus program Berita, Talkshow, Investigasi, & Dokumenter.",
        buildCost = 150_000L,
        monthlyUpkeep = 4_500L,
        supportedGenres = listOf("Berita", "Dokumenter", "Talkshow", "Investigasi Kriminal")
    ),
    STUDIO_KECIL(
        displayName = "Studio Kecil (Indoor)",
        description = "Set kompak untuk Sitkom, Kuis Ringan, Game Show, FTV, dan Animasi.",
        buildCost = 100_000L,
        monthlyUpkeep = 3_000L,
        supportedGenres = listOf("Sitkom", "Kuis Interaktif (Game Show)", "Animasi Anak", "FTV", "Late Night Show")
    ),
    STUDIO_RAKSASA(
        displayName = "Studio Raksasa (Stage)",
        description = "Panggung megah berkapasitas penonton besar untuk Reality Show, Pencarian Bakat, & Variety Show.",
        buildCost = 350_000L,
        monthlyUpkeep = 10_000L,
        supportedGenres = listOf("Reality Show", "Pencarian Bakat (Talent Show)", "Variety Show", "Sinetron", "Sitkom", "FTV")
    ),
    MASTER_CONTROL(
        displayName = "Control Room / Master Control",
        description = "Pusat kendali transmisi live & switcher penyiaran simultan multi-kanal.",
        buildCost = 200_000L,
        monthlyUpkeep = 5_000L,
        supportedGenres = emptyList()
    )
}

data class TvStudioFacility(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: TvFacilityType,
    val buildCost: Long = type.buildCost,
    val monthlyUpkeep: Long = type.monthlyUpkeep,
    val isUnderConstruction: Boolean = false,
    val buildMonthsLeft: Int = 0
)

enum class TvDirectorRole(
    val displayName: String,
    val titleRole: String,
    val monthlySalary: Long,
    val unlockedGenres: List<String>,
    val description: String
) {
    CHIEF_NEWS_DIRECTOR(
        displayName = "Chief News Director",
        titleRole = "Direktur Berita & Jurnalistik",
        monthlySalary = 1_500L,
        unlockedGenres = listOf("Berita", "Dokumenter", "Talkshow", "Investigasi Kriminal"),
        description = "Memimpin redaksi berita, dokumenter investigasi, dan talkshow politik/publik."
    ),
    HEAD_OF_ENTERTAINMENT(
        displayName = "Head of Entertainment",
        titleRole = "Kepala Divisi Hiburan",
        monthlySalary = 2_000L,
        unlockedGenres = listOf("Reality Show", "Pencarian Bakat (Talent Show)", "Variety Show", "Kuis Interaktif (Game Show)"),
        description = "Merancang format megah talent show, reality show berating tinggi, & game show."
    ),
    HEAD_OF_DRAMA(
        displayName = "Head of Drama",
        titleRole = "Kepala Divisi Sinetron & Drama",
        monthlySalary = 1_800L,
        unlockedGenres = listOf("Sinetron", "Sitkom", "FTV"),
        description = "Mengawasi produksi drama seri harian, sitkom keluarga, dan FTV remaja."
    ),
    HEAD_OF_CREATIVE(
        displayName = "Head of Creative & Animation",
        titleRole = "Kepala Kreatif & Animasi",
        monthlySalary = 1_200L,
        unlockedGenres = listOf("Animasi Anak", "Late Night Show"),
        description = "Mengembangkan serial animasi anak ber-IP lokal dan format talkshow larut malam."
    )
}

enum class TvRegionalTransmission(
    val regionKey: String,
    val displayName: String,
    val populationCoverage: String,
    val buildCost: Long,
    val monthlyUpkeep: Long,
    val maxRatingBonus: Double,
    val adRevenueBoostPercent: Double,
    val description: String
) {
    JAWA(
        regionKey = "JAWA",
        displayName = "Menara Transmisi Regional Jawa",
        populationCoverage = "150 Juta Jiwa",
        buildCost = 150_000L,
        monthlyUpkeep = 3_000L,
        maxRatingBonus = 10.0,
        adRevenueBoostPercent = 0.20,
        description = "Pusat rating & perputaran kue iklan terbesar nasional."
    ),
    SUMATERA(
        regionKey = "SUMATERA",
        displayName = "Menara Transmisi Regional Sumatera",
        populationCoverage = "60 Juta Jiwa",
        buildCost = 120_000L,
        monthlyUpkeep = 2_500L,
        maxRatingBonus = 8.0,
        adRevenueBoostPercent = 0.15,
        description = "Jangkauan koridor barat pulau Sumatera dari Aceh hingga Lampung."
    ),
    KALIMANTAN(
        regionKey = "KALIMANTAN",
        displayName = "Menara Transmisi Regional Kalimantan",
        populationCoverage = "18 Juta Jiwa",
        buildCost = 100_000L,
        monthlyUpkeep = 2_000L,
        maxRatingBonus = 6.0,
        adRevenueBoostPercent = 0.12,
        description = "Menghubungkan pemirsa pulau Borneo dan kawasan ibukota baru."
    ),
    SULAWESI(
        regionKey = "SULAWESI",
        displayName = "Menara Transmisi Regional Sulawesi",
        populationCoverage = "20 Juta Jiwa",
        buildCost = 90_000L,
        monthlyUpkeep = 1_800L,
        maxRatingBonus = 6.0,
        adRevenueBoostPercent = 0.10,
        description = "Hub siaran terestrial digital wilayah Indonesia bagian tengah."
    ),
    BALI_NUSA_TENGGARA(
        regionKey = "BALI_NUSA_TENGGARA",
        displayName = "Menara Transmisi Bali & Nusa Tenggara",
        populationCoverage = "15 Juta Jiwa",
        buildCost = 80_000L,
        monthlyUpkeep = 1_500L,
        maxRatingBonus = 5.0,
        adRevenueBoostPercent = 0.10,
        description = "Zona pariwisata dan demografi pemirsa muda yang aktif."
    ),
    PAPUA_MALUKU(
        regionKey = "PAPUA_MALUKU",
        displayName = "Menara Transmisi Papua & Maluku",
        populationCoverage = "8 Juta Jiwa",
        buildCost = 75_000L,
        monthlyUpkeep = 1_500L,
        maxRatingBonus = 5.0,
        adRevenueBoostPercent = 0.08,
        description = "Kedaulatan siaran nasional hingga pelosok timur nusantara."
    )
}

data class TvStationData(
    val reputation: Double = 55.0,
    val facilities: List<TvStudioFacility> = listOf(
        TvStudioFacility(name = "Studio Berita 1", type = TvFacilityType.NEWSROOM),
        TvStudioFacility(name = "Studio Kecil A", type = TvFacilityType.STUDIO_KECIL),
        TvStudioFacility(name = "Master Control Alpha", type = TvFacilityType.MASTER_CONTROL)
    ),
    val hiredDirectors: Set<String> = setOf("CHIEF_NEWS_DIRECTOR", "HEAD_OF_DRAMA"),
    val totalCrews: Int = 20,
    val crewSalaryPerPerson: Long = 250L,
    val dewanPersCertified: Boolean = false,
    val nationalBroadcastLicense: Boolean = false,
    val unlockedTransmissions: Set<String> = setOf("JAWA")
) {
    val totalDirectorSalaries: Long get() {
        return hiredDirectors.sumOf { roleName ->
            try {
                TvDirectorRole.valueOf(roleName).monthlySalary
            } catch (e: Exception) {
                0L
            }
        }
    }

    val totalCrewSalaries: Long get() = totalCrews * crewSalaryPerPerson

    val totalFacilityUpkeep: Long get() = facilities.sumOf { it.monthlyUpkeep }

    val totalTransmissionUpkeep: Long get() {
        return unlockedTransmissions.sumOf { regionKey ->
            try {
                TvRegionalTransmission.valueOf(regionKey).monthlyUpkeep
            } catch (e: Exception) {
                0L
            }
        }
    }

    val totalMonthlyExpenses: Long get() = totalDirectorSalaries + totalCrewSalaries + totalFacilityUpkeep + totalTransmissionUpkeep

    val totalTransmissionRatingBonus: Double get() {
        return unlockedTransmissions.sumOf { regionKey ->
            try {
                TvRegionalTransmission.valueOf(regionKey).maxRatingBonus
            } catch (e: Exception) {
                0.0
            }
        }
    }

    val totalTransmissionRevenueMultiplier: Double get() {
        var multiplier = 1.0
        unlockedTransmissions.forEach { regionKey ->
            try {
                multiplier += TvRegionalTransmission.valueOf(regionKey).adRevenueBoostPercent
            } catch (e: Exception) {
                // ignore
            }
        }
        if (nationalBroadcastLicense) multiplier += 0.15
        return multiplier
    }

    val masterControlCount: Int get() = facilities.count { it.type == TvFacilityType.MASTER_CONTROL }
    
    // Each master control enables up to 6 simultaneous broadcast programs, base 4 without MC
    val maxSimultaneousPrograms: Int get() = maxOf(4, masterControlCount * 6)
}

fun getRequiredCrewsForProgram(type: String): Int {
    return when (type) {
        "Pencarian Bakat (Talent Show)" -> 20
        "Reality Show" -> 16
        "Variety Show" -> 14
        "Sinetron" -> 12
        "Berita" -> 10
        "Sitkom" -> 8
        "FTV" -> 8
        "Late Night Show" -> 8
        "Kuis Interaktif (Game Show)" -> 8
        "Talkshow" -> 6
        "Dokumenter" -> 6
        "Investigasi Kriminal" -> 6
        "Animasi Anak" -> 4
        "Sports/Event" -> 15
        else -> 8
    }
}

fun getCompatibleStudioTypes(programType: String): List<TvFacilityType> {
    return when (programType) {
        "Berita", "Dokumenter", "Talkshow", "Investigasi Kriminal" -> listOf(TvFacilityType.NEWSROOM, TvFacilityType.STUDIO_RAKSASA)
        "Reality Show", "Pencarian Bakat (Talent Show)", "Variety Show" -> listOf(TvFacilityType.STUDIO_RAKSASA)
        "Sinetron", "Sitkom", "FTV" -> listOf(TvFacilityType.STUDIO_KECIL, TvFacilityType.STUDIO_RAKSASA)
        "Kuis Interaktif (Game Show)", "Animasi Anak", "Late Night Show" -> listOf(TvFacilityType.STUDIO_KECIL, TvFacilityType.STUDIO_RAKSASA)
        "Sports/Event" -> listOf(TvFacilityType.NEWSROOM, TvFacilityType.STUDIO_RAKSASA, TvFacilityType.MASTER_CONTROL)
        else -> listOf(TvFacilityType.STUDIO_KECIL, TvFacilityType.STUDIO_RAKSASA, TvFacilityType.NEWSROOM)
    }
}

fun getRequiredDirectorRole(programType: String): TvDirectorRole? {
    return when (programType) {
        "Berita", "Dokumenter", "Talkshow", "Investigasi Kriminal" -> TvDirectorRole.CHIEF_NEWS_DIRECTOR
        "Reality Show", "Pencarian Bakat (Talent Show)", "Variety Show", "Kuis Interaktif (Game Show)" -> TvDirectorRole.HEAD_OF_ENTERTAINMENT
        "Sinetron", "Sitkom", "FTV" -> TvDirectorRole.HEAD_OF_DRAMA
        "Animasi Anak", "Late Night Show" -> TvDirectorRole.HEAD_OF_CREATIVE
        else -> null
    }
}

fun isDewanPersRequired(programType: String): Boolean {
    return programType == "Berita" || programType == "Investigasi Kriminal"
}
