package com.example.data

import java.util.UUID

enum class FoundationType(val label: String, val legalCost: Long, val setupMonths: Int) {
    EDUCATION("Yayasan Pendidikan & Riset", 5_000_000L, 3),
    HEALTHCARE("Yayasan Kesehatan & Medis", 10_000_000L, 6),
    HUMANITARIAN("Badan Amal & Kemanusiaan Terpadu", 2_000_000L, 2)
}

data class FacilityItem(
    val id: String = UUID.randomUUID().toString(),
    val typeId: String = "",
    val name: String = "",
    val baseName: String = name,
    var customName: String = "",
    val gradeName: String = "",
    val maintenanceCost: Long = 0L,
    var constructionTotalMonths: Int = 0,
    var constructionLeftMonths: Int = 0
) {
    val isUnderConstruction: Boolean get() = constructionLeftMonths > 0
}

data class StaffRole(
    val active: Int = 0,
    val recruiting: Int = 0,
    val target: Int = 0, // Target kuota yang di-set pemain
    val customSalary: Long = 0L // Gaji bulanan yang bisa diedit
)

data class TeacherStaff(
    val umum: StaffRole = StaffRole(customSalary = 3000L),
    val spesialis: StaffRole = StaffRole(customSalary = 5000L),
    val senior: StaffRole = StaffRole(customSalary = 8000L),
    @Deprecated("Use umum.active instead") val umumCount: Int? = null,
    @Deprecated("Use spesialis.active instead") val spesialisCount: Int? = null,
    @Deprecated("Use senior.active instead") val seniorCount: Int? = null
)

data class SupportStaff(
    val ob: StaffRole = StaffRole(customSalary = 800L),
    val satpam: StaffRole = StaffRole(customSalary = 1000L),
    val admin: StaffRole = StaffRole(customSalary = 1200L),
    val chef: StaffRole = StaffRole(customSalary = 2500L),
    @Deprecated("Use ob.active instead") val janitorCount: Int? = null,
    @Deprecated("Use satpam.active instead") val securityCount: Int? = null,
    @Deprecated("Use admin.active instead") val adminCount: Int? = null,
    @Deprecated("Use chef.active instead") val chefCount: Int? = null
)

data class EducationInstitution(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val level: String, // TK, SD, SMA, UNIV
    val curriculumType: String, // Merdeka, K13, Cambridge, IB, dsb.
    val facilityLevel: Int, // 1 - 5 (Upgrade fasilitas)
    val accreditationPoints: Int, // 0 - 100
    val monthlyOperationalCost: Long, // Biaya bakar duit per bulan
    val prestigeScore: Int,
    val imageUrl: String = "", // <-- Tambahan baru
    val monthlySpp: Long = 0L, // Biaya SPP per murid yang diatur pemain
    val currentStudents: Int = 0, // Jumlah murid yang saat ini terdaftar
    val buildingGrade: String = "Grade A", // Menyimpan grade gedung
    val baseMaintenanceCost: Long = 0L, // Biaya rawat gedung dasar per bulan
    val additionalFacilities: List<FacilityItem> = emptyList(),
    val constructionMonthsTotal: Int = 0,
    val constructionMonthsLeft: Int = 0,
    val isOperational: Boolean = false,
    val teachers: TeacherStaff = TeacherStaff(),
    val supportStaff: SupportStaff = SupportStaff()
) {
    val isUnderConstruction: Boolean get() = constructionMonthsLeft > 0
}

data class MedicalStaff(
    val perawat: StaffRole = StaffRole(customSalary = 4000L),
    val dokterUmum: StaffRole = StaffRole(customSalary = 8000L),
    val dokterSpesialis: StaffRole = StaffRole(customSalary = 15000L)
)

data class HealthInstitution(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val level: String, // Klinik, RS Umum, RS Khusus, RS Internasional
    val serviceType: String, // Subsidi, Reguler, VIP, VVIP
    val facilityLevel: Int = 1,
    val accreditationPoints: Int = 0,
    val monthlyOperationalCost: Long = 0L,
    val prestigeScore: Int = 0,
    val imageUrl: String = "",
    val monthlyBillPerPatient: Long = 0L, 
    val currentPatients: Int = 0, 
    val maxPatients: Int = 0,
    val buildingGrade: String = "Grade A",
    val baseMaintenanceCost: Long = 0L,
    val additionalFacilities: List<FacilityItem> = emptyList(),
    val constructionMonthsTotal: Int = 0,
    val constructionMonthsLeft: Int = 0,
    val isOperational: Boolean = false,
    val medicalStaff: MedicalStaff = MedicalStaff(),
    val supportStaff: SupportStaff = SupportStaff()
) {
    val isUnderConstruction: Boolean get() = constructionMonthsLeft > 0
}

data class FoundationEntity(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: FoundationType,
    val isLegalized: Boolean = false,
    val constructionMonthsLeft: Int,
    val endowmentFund: Long = 0L, // Dana abadi yayasan
    val facilities: List<FoundationFacility> = emptyList(),
    val educationInstitutions: List<EducationInstitution> = emptyList(),
    val healthInstitutions: List<HealthInstitution> = emptyList(),
    val charityInstitutions: List<CharityInstitution> = emptyList()
)

data class FoundationFacility(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String, // Cth: "Sekolah Dasar", "RSUD", etc.
    val tier: String, // Cth: "Subsidi", "Swasta Nasional", "Internasional"
    val buildCost: Long,
    val buildMonthsLeft: Int,
    val monthlyOperationalCost: Long, // Memotong Dana Abadi (Endowment)
    val prestigeReward: Long = 0L, // Menghasilkan Legacy Points setelah beroperasi
    val isOperational: Boolean = false
)

// Standard blueprints for facilities
data class FacilityBlueprint(
    val category: String,
    val baseCost: Long,
    val buildMonths: Int,
    val baseMonthlyOps: Long,
    val basePrestige: Long,
    val description: String
)

object FoundationBlueprints {
    val blueprints = mapOf(
        FoundationType.EDUCATION to listOf(
            FacilityBlueprint("Sekolah Dasar (SD)", 1_000_000L, 2, 20_000L, 100L, "Pendidikan dasar bagi tunas bangsa."),
            FacilityBlueprint("Sekolah Menengah (SMP/SMA)", 3_500_000L, 3, 60_000L, 300L, "Jenjang menengah untuk membentuk karakter."),
            FacilityBlueprint("Universitas & Kampus Riset", 15_000_000L, 8, 300_000L, 1500L, "Institusi pendidikan tinggi & pusat inovasi ilmu pengetahuan.")
        ),
        FoundationType.HEALTHCARE to listOf(
            FacilityBlueprint("Klinik Pelosok Desa", 500_000L, 2, 12_000L, 80L, "Pelayanan medis primer bagi masyarakat terpencil."),
            FacilityBlueprint("Rumah Sakit Umum Daerah (RSUD)", 6_000_000L, 5, 120_000L, 600L, "Fasilitas rawat inap & pembedahan skala menengah."),
            FacilityBlueprint("Pusat Riset Kanker Tingkat Lanjut", 20_000_000L, 10, 500_000L, 2200L, "Riset mutakhir untuk pengobatan onkologi global.")
        ),
        FoundationType.HUMANITARIAN to listOf(
            FacilityBlueprint("Dapur Umum & Sandang", 300_000L, 1, 8_000L, 50L, "Bantuan makanan & kebutuhan dasar harian bagi dhuafa."),
            FacilityBlueprint("Panti Asuhan & Jompo Terpadu", 1_500_000L, 3, 30_000L, 200L, "Tempat bernaung layak bagi anak yatim & lansia telantar."),
            FacilityBlueprint("Badan Penanggulangan Bencana", 8_000_000L, 6, 180_000L, 900L, "Satgas tanggap darurat logistik & evakuasi bencana alam.")
        )
    )

    val tiers = listOf(
        FacilityTier("Sekolah Subsidi Rakyat / Klinik Gratis", "Subsidi", 0.5, 0.4, 1.5, "Murni amal nirlaba. Biaya murah, dampak sosial & legacy maksimal."),
        FacilityTier("Swasta Nasional", "Swasta", 1.0, 1.0, 1.0, "Standar kurikulum & fasilitas nasional."),
        FacilityTier("Internasional Elite (Cambridge / Mayo)", "Internasional", 3.0, 2.5, 3.5, "Gengsi legendaris luar biasa, biaya tinggi, reputasi internasional.")
    )
}

data class FacilityTier(
    val name: String,
    val key: String,
    val costMultiplier: Double,
    val opsMultiplier: Double,
    val prestigeMultiplier: Double,
    val description: String
)

fun calculateEduOperationalCost(level: String, facilityLevel: Int, curriculumType: String): Long {
    val baseOps = when (level) {
        "TK" -> 10000L
        "SD" -> 30000L
        "SMA" -> 100000L
        "UNIV" -> 400000L
        else -> 10000L
    }
    val multiplier = if (level == "SMA") {
        when (curriculumType) {
            "Nasional" -> 1.2
            "Kejuruan (SMK)" -> 1.5
            "Cambridge (A-Level)" -> 2.5
            "IB (International Baccalaureate)" -> 3.0
            else -> 1.0
        }
    } else if (level == "UNIV") {
        when (curriculumType) {
            "Nasional (Teaching Univ)" -> 1.5
            "Internasional (Double Degree)" -> 3.0
            "World-Class Research Univ" -> 5.0
            else -> 1.0
        }
    } else {
        when (curriculumType) {
            "Montessori", "Waldorf" -> 1.5
            "Agama Terpadu" -> if (level == "SD") 1.2 else 1.75
            "Nasional Plus (Bilingual)" -> 1.8
            "Cambridge Primary" -> 2.5
            "Cambridge", "IB" -> 3.0
            "Internasional" -> 6.0
            else -> 1.0
        }
    }
    return (baseOps * facilityLevel * multiplier).toLong()
}

data class BuildingGrade(
    val name: String,
    val baseMaintenanceCost: Long,
    val description: String,
    val constructionMonths: Int
)

val BUILDING_GRADES = listOf(
    BuildingGrade("Grade A", 500L, "Fungsional standar.", 1),
    BuildingGrade("Grade A2", 800L, "Material lebih baik.", 1),
    BuildingGrade("Grade A3", 1200L, "Desain modern minimalis.", 2),
    BuildingGrade("Grade A4", 1800L, "Fasilitas ramah lingkungan.", 3),
    BuildingGrade("Grade A5", 2500L, "Infrastruktur tahan gempa.", 3),
    BuildingGrade("Grade A6", 3500L, "Arsitektur kustom menengah.", 4),
    BuildingGrade("Grade S", 10000L, "Premium standar elit.", 6),
    BuildingGrade("Grade S1", 15000L, "Material impor premium.", 7),
    BuildingGrade("Grade S2", 25000L, "Fasilitas smart-building.", 8),
    BuildingGrade("Grade S3", 40000L, "Sistem keamanan biometrik.", 9),
    BuildingGrade("Grade S4", 60000L, "Desain arsitek ternama dunia.", 12),
    BuildingGrade("Grade S5", 90000L, "Fasilitas setara hotel bintang 5.", 13),
    BuildingGrade("Grade S6", 130000L, "Kawasan terpadu mandiri.", 15),
    BuildingGrade("Grade S7", 180000L, "Infrastruktur futuristik.", 18),
    BuildingGrade("Grade S8", 250000L, "Ikon arsitektur kota.", 19),
    BuildingGrade("Grade S9", 350000L, "Mahakarya seni & teknologi.", 20),
    BuildingGrade("Grade SS", 500000L, "Ultra-elit. Kualitas nomor 1 di dunia.", 24)
)

fun EducationInstitution.getCurriculumMultiplier(): Double {
    return if (this.level == "SMA") {
        when (this.curriculumType) {
            "Nasional" -> 1.2
            "Kejuruan (SMK)" -> 1.5
            "Cambridge (A-Level)" -> 2.5
            "IB (International Baccalaureate)" -> 3.0
            else -> 1.0
        }
    } else if (this.level == "UNIV") {
        when (this.curriculumType) {
            "Nasional (Teaching Univ)" -> 1.5
            "Internasional (Double Degree)" -> 3.0
            "World-Class Research Univ" -> 5.0
            else -> 1.0
        }
    } else {
        when (this.curriculumType) {
            "Montessori", "Waldorf" -> 1.5
            "Agama Terpadu" -> if (this.level == "SD") 1.2 else 1.75
            "Nasional Plus (Bilingual)" -> 1.8
            "Cambridge Primary" -> 2.5
            "Cambridge", "IB" -> 3.0
            "Internasional" -> 6.0
            else -> 1.0
        }
    }
}

fun EducationInstitution.calculateTotalOpsCost(): Long {
    if (this.constructionMonthsLeft > 0) return 0L // Gedung belum jadi = $0 Ops

    // 1. Biaya Perawatan Fisik
    val activeFacCost = (this.additionalFacilities ?: emptyList())
        .filter { it.constructionLeftMonths <= 0 }
        .sumOf { it.maintenanceCost }

    val baseBldg = if (this.baseMaintenanceCost > 0L) {
        this.baseMaintenanceCost
    } else {
        when (this.level) {
            "TK" -> 10000L
            "SD" -> 30000L
            "SMA" -> 100000L
            "UNIV" -> 400000L
            else -> 10000L
        }
    }

    val physicalCost = (baseBldg + activeFacCost) * getCurriculumMultiplier()

    // 2. Biaya Gaji Seluruh SDM Aktif
    val teacherCost = (this.teachers.umum.active * this.teachers.umum.customSalary) + 
                      (this.teachers.spesialis.active * this.teachers.spesialis.customSalary) + 
                      (this.teachers.senior.active * this.teachers.senior.customSalary)
                      
    val supportCost = (this.supportStaff.ob.active * this.supportStaff.ob.customSalary) +
                      (this.supportStaff.satpam.active * this.supportStaff.satpam.customSalary) +
                      (this.supportStaff.admin.active * this.supportStaff.admin.customSalary) +
                      (this.supportStaff.chef.active * this.supportStaff.chef.customSalary)

    return physicalCost.toLong() + teacherCost + supportCost
}

fun EducationInstitution.calculateTotalMonthlyOpsCost(): Long {
    return this.calculateTotalOpsCost()
}

fun HealthInstitution.getServiceTypeMultiplier(): Double {
    return when (this.serviceType) {
        "Subsidi" -> 0.5
        "Reguler" -> 1.0
        "VIP" -> 2.0
        "VVIP" -> 3.5
        else -> 1.0
    }
}

fun HealthInstitution.calculateTotalOpsCost(): Long {
    if (this.constructionMonthsLeft > 0) return 0L // Gedung belum jadi = $0 Ops

    // 1. Biaya Perawatan Fisik
    val activeFacCost = (this.additionalFacilities ?: emptyList())
        .filter { it.constructionLeftMonths <= 0 }
        .sumOf { it.maintenanceCost }

    val baseBldg = if (this.baseMaintenanceCost > 0L) {
        this.baseMaintenanceCost
    } else {
        when (this.level) {
            "Klinik" -> 15000L
            "RS Umum" -> 80000L
            "RS Khusus" -> 200000L
            "RS Internasional" -> 600000L
            else -> 15000L
        }
    }

    val physicalCost = (baseBldg + activeFacCost) * getServiceTypeMultiplier()

    // 2. Biaya Gaji Seluruh SDM Aktif
    val medicalCost = (this.medicalStaff.perawat.active * this.medicalStaff.perawat.customSalary) + 
                      (this.medicalStaff.dokterUmum.active * this.medicalStaff.dokterUmum.customSalary) + 
                      (this.medicalStaff.dokterSpesialis.active * this.medicalStaff.dokterSpesialis.customSalary)
                      
    val supportCost = (this.supportStaff.ob.active * this.supportStaff.ob.customSalary) +
                      (this.supportStaff.satpam.active * this.supportStaff.satpam.customSalary) +
                      (this.supportStaff.admin.active * this.supportStaff.admin.customSalary) +
                      (this.supportStaff.chef.active * this.supportStaff.chef.customSalary)

    return physicalCost.toLong() + medicalCost + supportCost
}

fun HealthInstitution.calculateTotalMonthlyOpsCost(): Long {
    return this.calculateTotalOpsCost()
}

