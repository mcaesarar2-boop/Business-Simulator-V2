package com.example.data

import java.util.UUID

/**
 * Bank Tier Classification (Leveling Perbankan)
 */
enum class BankTier(
    val title: String,
    val shortName: String,
    val minDpkRequired: Long,
    val description: String,
    val avgLoanRange: String,
    val avgTenorMonths: String
) {
    TIER_1_MICRO(
        title = "Tier 1: Microfinance & Konsumer",
        shortName = "Tier 1: Micro",
        minDpkRequired = 0L,
        description = "Kredit mikro tanpa agunan & pinjaman personal. Bunga tinggi, volume dinamis, risiko gagal bayar cukup tinggi.",
        avgLoanRange = "$1K - $25K",
        avgTenorMonths = "3 - 12 bln"
    ),
    TIER_2_RETAIL(
        title = "Tier 2: Retail & Komersial UKM",
        shortName = "Tier 2: Retail",
        minDpkRequired = 5_000_000L,
        description = "KPR hunian, kredit kendaraan bermotor & modal kerja komersial. Tenor panjang dengan jaminan agunan aset.",
        avgLoanRange = "$50K - $750K",
        avgTenorMonths = "12 - 36 bln"
    ),
    TIER_3_CORPORATE(
        title = "Tier 3: Corporate & Sindikasi B2B",
        shortName = "Tier 3: Corporate",
        minDpkRequired = 50_000_000L,
        description = "Pembiayaan korporasi multinasional, sindikasi megaproyek & cross-lending ekosistem konglomerasi holding Anda.",
        avgLoanRange = "$1M - $20M",
        avgTenorMonths = "24 - 60 bln"
    );

    val label: String get() = title
    val requiredDpkToUnlock: Long get() = minDpkRequired
    val level: Int get() = when(this) { TIER_1_MICRO -> 1; TIER_2_RETAIL -> 2; TIER_3_CORPORATE -> 3 }
    val upgradeCost: Long get() = when(this) { TIER_1_MICRO -> 0L; TIER_2_RETAIL -> 1_500_000L; TIER_3_CORPORATE -> 7_500_000L }

    fun nextTier(): BankTier? = when (this) {
        TIER_1_MICRO -> TIER_2_RETAIL
        TIER_2_RETAIL -> TIER_3_CORPORATE
        TIER_3_CORPORATE -> null
    }
}

/**
 * Credit Underwriting Grade (Kualitas & Profil Risiko Nasabah)
 */
enum class CreditGrade(
    val label: String,
    val shortLabel: String,
    val baseDefaultRisk: Double,
    val rateSpreadAdjustment: Double,
    val colorHex: Long
) {
    GRADE_A(
        label = "Grade A (Prime / Low Risk)",
        shortLabel = "Grade A (Prime)",
        baseDefaultRisk = 0.015, // 1.5% default chance
        rateSpreadAdjustment = -0.015, // Discounted rate for safe borrowers
        colorHex = 0xFF10B981 // Emerald
    ),
    GRADE_B(
        label = "Grade B (Standard / Moderate)",
        shortLabel = "Grade B (Standard)",
        baseDefaultRisk = 0.060, // 6.0% default chance
        rateSpreadAdjustment = 0.0,
        colorHex = 0xFFF59E0B // Amber / Gold
    ),
    GRADE_C(
        label = "Grade C (Subprime / High Risk)",
        shortLabel = "Grade C (Subprime)",
        baseDefaultRisk = 0.220, // 22% default chance
        rateSpreadAdjustment = 0.045, // High risk premium
        colorHex = 0xFFEF4444 // Red
    );

    val ratingLabel: String get() = shortLabel
}

/**
 * Sektor / Tujuan Penggunaan Dana Pinjaman
 */
enum class LoanSector(
    val title: String,
    val defaultTier: BankTier
) {
    PERSONAL_LOAN("Pinjaman Konsumtif & Paylater", BankTier.TIER_1_MICRO),
    MICRO_MERCHANT("Modal Kerja Warung & UMKM", BankTier.TIER_1_MICRO),
    HOME_MORTGAGE("KPR Hunian & Apartemen", BankTier.TIER_2_RETAIL),
    AUTO_FINANCING("Kredit Kendaraan & Otomotif", BankTier.TIER_2_RETAIL),
    COMMERCIAL_SME("Ekspansi Cabang & Retail Chain", BankTier.TIER_2_RETAIL),
    CORPORATE_CONSTRUCTION("Sindikasi Mega Konstruksi & Infrastruktur", BankTier.TIER_3_CORPORATE),
    CORPORATE_AVIATION("Pembiayaan Armada Maskapai Penerbangan", BankTier.TIER_3_CORPORATE),
    CORPORATE_HOSPITALITY("Ekspansi Hotel Bintang 5 & Resor", BankTier.TIER_3_CORPORATE),
    CORPORATE_TECH("Investasi Data Center & AI Cloud Studio", BankTier.TIER_3_CORPORATE),
    CORPORATE_LOGISTICS("Pengadaan Armada Cargo & Truk Logistik", BankTier.TIER_3_CORPORATE),
    CORPORATE_THEMEPARK("Pengembangan Wahana Universal Theme Park", BankTier.TIER_3_CORPORATE),
    CORPORATE_CRUISE("Sindikasi Galangan Kapal Pesiar Mewah", BankTier.TIER_3_CORPORATE),
    CORPORATE_HEALTHCARE("Pembangunan Rumah Sakit & Bio-Medis", BankTier.TIER_3_CORPORATE);

    val label: String get() = title
}

/**
 * Status Kesehatan Portofolio Kredit
 */
enum class LoanHealthStatus(
    val label: String,
    val colorHex: Long
) {
    PERFORMING("Lancar (Performing)", 0xFF10B981),
    SPECIAL_MENTION("Dalam Perhatian Khusus", 0xFFF59E0B),
    NON_PERFORMING("Kredit Macet (NPL / Default)", 0xFFEF4444),
    SETTLED("Lunas (Paid Off)", 0xFF3B82F6)
}

/**
 * Pengajuan Pinjaman Masuk (Credit Desk Proposal)
 */
data class LoanApplication(
    val id: String = UUID.randomUUID().toString(),
    val applicantName: String,
    val sector: LoanSector,
    val tier: BankTier,
    val creditGrade: CreditGrade,
    val principalAmount: Long,
    val tenorMonths: Int,
    val annualInterestRate: Double, // e.g. 0.14 = 14%
    val isInternalCorporateSynergy: Boolean = false,
    val linkedBusinessInstanceId: String? = null,
    val linkedBusinessName: String? = null,
    val proposalNote: String = ""
) {
    val monthlyInterestRate: Double get() = annualInterestRate / 12.0
    val monthlyPrincipalPayment: Long get() = (principalAmount / maxOf(1, tenorMonths))
    val monthlyInterestPayment: Long get() = (principalAmount * monthlyInterestRate).toLong()
    val totalMonthlyInstallment: Long get() = monthlyPrincipalPayment + monthlyInterestPayment
    val totalRepaymentGross: Long get() = totalMonthlyInstallment * tenorMonths
    val expectedNetInterestProfit: Long get() = totalRepaymentGross - principalAmount
    val defaultProbabilityPercent: Double get() = (creditGrade.baseDefaultRisk * (1.0 + (annualInterestRate - 0.12).coerceAtLeast(0.0) * 2.2) * 100.0).coerceIn(1.0, 65.0)
}

/**
 * Pinjaman Aktif dalam Portofolio (Active Loan Book)
 */
data class ActiveDisbursedLoan(
    val id: String = UUID.randomUUID().toString(),
    val borrowerName: String,
    val sector: LoanSector,
    val tier: BankTier,
    val creditGrade: CreditGrade,
    val originalPrincipal: Long,
    val remainingPrincipal: Long,
    val tenorMonthsTotal: Int,
    val remainingMonths: Int,
    val annualInterestRate: Double,
    val totalInterestCollected: Long = 0L,
    val healthStatus: LoanHealthStatus = LoanHealthStatus.PERFORMING,
    val consecutiveMissedPayments: Int = 0,
    val isInternalCorporateSynergy: Boolean = false,
    val linkedBusinessInstanceId: String? = null,
    val linkedBusinessName: String? = null,
    val startMonthYear: String = ""
) {
    val monthlyInterestRate: Double get() = annualInterestRate / 12.0
    val monthlyPrincipalPayment: Long get() = if (remainingMonths > 0) (remainingPrincipal / remainingMonths) else remainingPrincipal
    val monthlyInterestPayment: Long get() = (remainingPrincipal * monthlyInterestRate).toLong()
    val totalMonthlyInstallment: Long get() = monthlyPrincipalPayment + monthlyInterestPayment
}

/**
 * Log Riwayat Keputusan Risk Manager AI
 */
data class AiRiskLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestampFormatted: String = "",
    val applicantName: String,
    val amount: Long,
    val grade: CreditGrade,
    val action: String, // "APPROVED", "REJECTED", "SKIPPED"
    val reason: String
)

/**
 * Konfigurasi & Status Otomasi "Risk Manager AI"
 */
data class AiRiskManagerData(
    val isEnabled: Boolean = false,
    val level: Int = 1, // 1: Heuristic AI ($15k/bln, 85% akurasi), 2: ML Risk Engine ($45k/bln, 95% akurasi), 3: Deep Quant Neural ($120k/bln, 99.5% akurasi)
    val autoApproveGrades: Set<CreditGrade> = setOf(CreditGrade.GRADE_A, CreditGrade.GRADE_B),
    val autoRejectGrades: Set<CreditGrade> = setOf(CreditGrade.GRADE_C),
    val maxAutoApprovePrincipal: Long = 250_000L,
    val autoApproveB2BSynergy: Boolean = true,
    val executionLogs: List<AiRiskLogEntry> = emptyList(),
    val totalProcessedCount: Int = 0,
    val totalApprovedCount: Int = 0,
    val totalRejectedCount: Int = 0,
    val totalDisbursedVolume: Long = 0L
) {
    val monthlyMaintenanceCost: Long get() = when (level) {
        1 -> 15_000L
        2 -> 45_000L
        3 -> 120_000L
        else -> 15_000L
    }

    val accuracyPercent: Double get() = when (level) {
        1 -> 85.0
        2 -> 95.0
        3 -> 99.5
        else -> 85.0
    }

    val modelName: String get() = when (level) {
        1 -> "Heuristic Rule-Based AI v1.2"
        2 -> "Machine Learning Risk Engine v2.5"
        3 -> "Deep Quant Neural Scoring Core v4.0"
        else -> "Basic AI Manager"
    }

    val nextLevelUpgradeCost: Long get() = when (level) {
        1 -> 250_000L
        2 -> 1_000_000L
        else -> 0L
    }
}

/**
 * Data Utama Perusahaan Bank (BankingCompanyData)
 */
data class BankingCompanyData(
    val currentTier: BankTier = BankTier.TIER_1_MICRO,
    val internalCash: Long = 20_000_000L, // Modal Internal Bank (Suntikan/Penarikan Pemain)
    val totalCustomerDepositsDpk: Long = 10_000_000L, // Dana Pihak Ketiga (Uang Nasabah)
    val depositInterestRate: Double = 0.045, // 4.5% p.a.
    val lendingInterestRate: Double = 0.135, // 13.5% p.a.
    val activeLoans: List<ActiveDisbursedLoan> = emptyList(),
    val incomingApplications: List<LoanApplication> = emptyList(),
    val aiRiskManager: AiRiskManagerData = AiRiskManagerData(),
    val lifetimeInterestEarned: Long = 0L,
    val lifetimeInterestPaidToDepositors: Long = 0L,
    val lifetimeDefaultLossesWrittenOff: Long = 0L,
    val lifetimeSynergyDisbursed: Long = 0L,
    val centralBankWarningCount: Int = 0,
    val lastMonthNetIncome: Long = 0L,
    val lastMonthNplRate: Double = 0.0,
    val lastRefreshedTimestamp: Long = 0L
) {
    // Total Kredit Aktif yang Sedang Disalurkan (Loan Book)
    val totalActiveLoanBook: Long get() = activeLoans
        .filter { it.healthStatus != LoanHealthStatus.SETTLED && it.healthStatus != LoanHealthStatus.NON_PERFORMING }
        .sumOf { it.remainingPrincipal }

    val totalOutstandingLoansPrincipal: Long get() = totalActiveLoanBook

    // Uang Tunai di Brankas (Vault Cash)
    // Formula: Dana Nasabah (DPK) + Modal Internal Bank - Total Pinjaman Disalurkan
    val vaultCash: Long get() = maxOf(0L, totalCustomerDepositsDpk + internalCash - totalActiveLoanBook)

    // Liquidity Ratio = (Vault Cash / Total DPK) * 100%
    val liquidityRatioPercent: Double get() = if (totalCustomerDepositsDpk > 0) {
        (vaultCash.toDouble() / totalCustomerDepositsDpk.toDouble()) * 100.0
    } else {
        100.0
    }

    // Peringatan Bank Sentral jika Likuiditas < 10% (Giro Wajib Minimum / GWM)
    val isLiquidityWarning: Boolean get() = liquidityRatioPercent < 10.0

    // Net Interest Margin (Spread Suku Bunga)
    val netInterestMarginPercent: Double get() = (lendingInterestRate - depositInterestRate) * 100.0

    // Non-Performing Loan (NPL %)
    val totalNplPrincipal: Long get() = activeLoans
        .filter { it.healthStatus == LoanHealthStatus.NON_PERFORMING }
        .sumOf { it.remainingPrincipal }

    val totalAllActivePrincipal: Long get() = activeLoans
        .filter { it.healthStatus != LoanHealthStatus.SETTLED }
        .sumOf { it.remainingPrincipal }

    val nplRatioPercent: Double get() = if (totalAllActivePrincipal > 0) {
        (totalNplPrincipal.toDouble() / totalAllActivePrincipal.toDouble()) * 100.0
    } else {
        0.0
    }

    fun canUnlockTier(tier: BankTier): Boolean = totalCustomerDepositsDpk >= tier.minDpkRequired
}
