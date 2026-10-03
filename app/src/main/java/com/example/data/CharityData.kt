package com.example.data

import java.util.UUID

data class CharityStaff(
    var relawan: StaffRole = StaffRole(customSalary = 500L), // Volunteer/Logistik
    var staffSosial: StaffRole = StaffRole(customSalary = 3000L), // Pekerja Sosial/Caregiver
    var ahliProgram: StaffRole = StaffRole(customSalary = 7000L) // Manajer Program/Ahli Bencana
)

data class CharityInstitution(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val level: String, // "Humanitarian Aid", "Social Care", "Disaster Relief", "Community Empowerment"
    val scope: String, // "Lokal", "Nasional", "Internasional"
    val facilityLevel: Int = 1,
    val accreditationPoints: Int = 0,
    val prestigeScore: Int = 0,
    val imageUrl: String = "",
    var baseMaintenanceCost: Long = 0L,
    var constructionTotalMonths: Int = 0,
    var constructionLeftMonths: Int = 0,
    var isOperational: Boolean = false,
    var monthlyBeneficiaries: Int = 0, // Jumlah orang yang dibantu (Pengganti Murid/Pasien)
    var maxCapacity: Int = 0,
    var additionalFacilities: List<FacilityItem> = emptyList(),
    var charityStaff: CharityStaff = CharityStaff(),
    val buildingGrade: String = "Grade A"
) {
    val isUnderConstruction: Boolean get() = constructionLeftMonths > 0
}

fun CharityInstitution.getScopeMultiplier(): Double {
    return when (this.scope) {
        "Lokal" -> 1.0
        "Nasional" -> 2.5
        "Internasional" -> 5.0
        else -> 1.0
    }
}

fun calculateCharityMaxCapacity(level: String, scope: String): Int {
    val base = when (level) {
        "Humanitarian Aid" -> 150
        "Social Care" -> 100
        "Disaster Relief" -> 200
        "Community Empowerment" -> 120
        else -> 100
    }
    val multiplier = when (scope) {
        "Lokal" -> 1.0
        "Nasional" -> 3.0
        "Internasional" -> 10.0
        else -> 1.0
    }
    return (base * multiplier).toInt()
}

fun CharityInstitution.calculateTotalOpsCost(): Long {
    if (this.constructionLeftMonths > 0) return 0L // Gedung belum jadi = $0 Ops

    // 1. Biaya Perawatan Fisik
    val activeFacCost = (this.additionalFacilities ?: emptyList())
        .filter { it.constructionLeftMonths <= 0 }
        .sumOf { it.maintenanceCost }

    val baseBldg = if (this.baseMaintenanceCost > 0L) {
        this.baseMaintenanceCost
    } else {
        when (this.level) {
            "Humanitarian Aid" -> 10000L
            "Social Care" -> 20000L
            "Disaster Relief" -> 45000L
            "Community Empowerment" -> 30000L
            else -> 15000L
        }
    }

    val physicalCost = (baseBldg + activeFacCost) * getScopeMultiplier()

    // 2. Biaya Gaji Seluruh SDM Aktif
    val staffCost = (this.charityStaff.relawan.active * this.charityStaff.relawan.customSalary) +
                    (this.charityStaff.staffSosial.active * this.charityStaff.staffSosial.customSalary) +
                    (this.charityStaff.ahliProgram.active * this.charityStaff.ahliProgram.customSalary)

    return physicalCost.toLong() + staffCost
}

fun CharityInstitution.calculateTotalMonthlyOpsCost(): Long {
    return this.calculateTotalOpsCost()
}

val CharityInstitution.monthlySponsorshipRevenue: Long
    get() = if (this.accreditationPoints >= 90 && this.isOperational) 150000L else 0L
