package com.example.viewmodel

import com.example.data.*
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object BankingEngine {

    /**
     * Generate fresh credit application pipeline.
     */
    fun generateApplicationPipeline(
        currentTier: BankTier,
        baseLendingRate: Double,
        ownedBusinesses: List<OwnedBusiness> = emptyList(),
        count: Int = 6
    ): List<LoanApplication> {
        val list = mutableListOf<LoanApplication>()

        // 1. Check if we can include B2B synergy loans if Tier 3 or if player owns eligible businesses
        val eligibleSynergyBusinesses = ownedBusinesses.filter { 
            it.catalogId != "tycoon_bank" && (
                it.catalogId == "construction" || 
                it.catalogId == "aviation_group" || 
                it.catalogId == "hospitality_holding" || 
                it.catalogId == "upper_tech" || 
                it.catalogId == "mid_logistics" || 
                it.catalogId == "theme_park_holding" || 
                it.catalogId == "cruise_line_holding"
            )
        }

        if (currentTier == BankTier.TIER_3_CORPORATE && eligibleSynergyBusinesses.isNotEmpty()) {
            val synergyBiz = eligibleSynergyBusinesses.random()
            val (sector, namePrefix, amountRange) = when (synergyBiz.catalogId) {
                "construction" -> Triple(LoanSector.CORPORATE_CONSTRUCTION, "Tender Mega Infrastruktur & Properti", 3_000_000L..15_000_000L)
                "aviation_group" -> Triple(LoanSector.CORPORATE_AVIATION, "Pengadaan Armada Pesawat Komersial", 5_000_000L..20_000_000L)
                "hospitality_holding" -> Triple(LoanSector.CORPORATE_HOSPITALITY, "Sindikasi Resor Bintang 5 Bali & Resort", 2_000_000L..10_000_000L)
                "upper_tech" -> Triple(LoanSector.CORPORATE_TECH, "Ekspansi AI Cloud & High-Density Server Farm", 1_000_000L..5_000_000L)
                "mid_logistics" -> Triple(LoanSector.CORPORATE_LOGISTICS, "Armada Truk Kontainer Ekspedisi Nasional", 1_000_000L..4_000_000L)
                "theme_park_holding" -> Triple(LoanSector.CORPORATE_THEMEPARK, "Wahana Roller Coaster Generasi Baru", 4_000_000L..12_000_000L)
                "cruise_line_holding" -> Triple(LoanSector.CORPORATE_CRUISE, "Sindikasi Pembuatan Kapal Pesiar Oasis", 8_000_000L..20_000_000L)
                else -> Triple(LoanSector.COMMERCIAL_SME, "Ekspansi Modal Kerja Unit Usaha", 1_000_000L..5_000_000L)
            }

            val amount = (amountRange.random() / 50_000L) * 50_000L
            val tenor = listOf(24, 36, 48, 60).random()
            val bizName = synergyBiz.customName ?: synergyBiz.name
            
            // Preferential B2B rate (Grade A Prime)
            val synergyRate = (baseLendingRate - 0.02).coerceAtLeast(0.06)

            list.add(
                LoanApplication(
                    applicantName = "[B2B Synergy] $bizName",
                    sector = sector,
                    tier = BankTier.TIER_3_CORPORATE,
                    creditGrade = CreditGrade.GRADE_A,
                    principalAmount = amount,
                    tenorMonths = tenor,
                    annualInterestRate = synergyRate,
                    isInternalCorporateSynergy = true,
                    linkedBusinessInstanceId = synergyBiz.instanceId,
                    linkedBusinessName = bizName,
                    proposalNote = "Pengajuan pinjaman korporasi internal untuk $namePrefix. Saldo kas unit usaha akan langsung bertambah!"
                )
            )
        }

        // 2. Generate standard borrower applications based on unlocked Tiers
        val availableTiers = when (currentTier) {
            BankTier.TIER_1_MICRO -> listOf(BankTier.TIER_1_MICRO)
            BankTier.TIER_2_RETAIL -> listOf(BankTier.TIER_1_MICRO, BankTier.TIER_2_RETAIL)
            BankTier.TIER_3_CORPORATE -> listOf(BankTier.TIER_1_MICRO, BankTier.TIER_2_RETAIL, BankTier.TIER_3_CORPORATE)
        }

        val firstNames = listOf("Budi", "Siti", "Ahmad", "Hendro", "Jessica", "Kevin", "Dewi", "Rian", "Aditya", "Farhan", "Nadia", "Chandra", "Putri", "Tommy", "Grace")
        val companyPrefixes = listOf("PT", "CV", "Firma", "Konsorsium", "Mega Corp", "Global", "Nusantara")
        val companyNames = listOf("Surya Perkasa", "Mitra Abadi", "Prima Logistik", "Sentosa Jaya", "Tech Nusantara", "Bumi Makmur", "Graha Propertindo", "Cahaya Mandiri", "Pancaran Energi")

        while (list.size < count) {
            val tier = availableTiers.random()
            val grade = listOf(CreditGrade.GRADE_A, CreditGrade.GRADE_B, CreditGrade.GRADE_B, CreditGrade.GRADE_C).random()
            
            val (applicantName, sector, amount, tenor, note) = when (tier) {
                BankTier.TIER_1_MICRO -> {
                    val name = "${firstNames.random()} ${listOf("Pratama", "Wijaya", "Santoso", "Kusuma", "Hidayat", "Saputra", "Tan").random()}"
                    val sec = listOf(LoanSector.PERSONAL_LOAN, LoanSector.MICRO_MERCHANT).random()
                    val amt = (Random.nextLong(2_000L, 25_000L) / 500L) * 500L
                    val ten = listOf(3, 6, 9, 12).random()
                    val n = if (sec == LoanSector.PERSONAL_LOAN) "Pinjaman konsumtif multiguna & renovasi rumah tinggal." else "Modal kerja penambahan inventaris toko sembako & kedai kopi."
                    tuple5(name, sec, amt, ten, n)
                }
                BankTier.TIER_2_RETAIL -> {
                    val sec = listOf(LoanSector.HOME_MORTGAGE, LoanSector.AUTO_FINANCING, LoanSector.COMMERCIAL_SME).random()
                    val name = if (sec == LoanSector.COMMERCIAL_SME) {
                        "${companyPrefixes.random()} ${companyNames.random()}"
                    } else {
                        "${firstNames.random()} ${listOf("Suryanto", "Gunawan", "Hartono", "Darmawan", "Siregar").random()}"
                    }
                    val amt = (Random.nextLong(60_000L, 750_000L) / 5_000L) * 5_000L
                    val ten = listOf(12, 18, 24, 36).random()
                    val n = when (sec) {
                        LoanSector.HOME_MORTGAGE -> "KPR unit apartemen & cluster perumahan residensial (Agunan SHM)."
                        LoanSector.AUTO_FINANCING -> "Pembiayaan mobil operasional komersial (Agunan BPKB)."
                        else -> "Ekspansi pembukaan cabang baru dan peremajaan alat produksi."
                    }
                    tuple5(name, sec, amt, ten, n)
                }
                BankTier.TIER_3_CORPORATE -> {
                    val sec = listOf(
                        LoanSector.CORPORATE_CONSTRUCTION,
                        LoanSector.CORPORATE_TECH,
                        LoanSector.CORPORATE_HOSPITALITY,
                        LoanSector.CORPORATE_LOGISTICS,
                        LoanSector.CORPORATE_HEALTHCARE
                    ).random()
                    val name = "${listOf("PT", "Konsorsium Global").random()} ${companyNames.random()} ${listOf("Holding", "Internasional", "Tbk", "Capital").random()}"
                    val amt = (Random.nextLong(1_500_000L, 18_000_000L) / 100_000L) * 100_000L
                    val ten = listOf(24, 36, 48, 60).random()
                    val n = "Fasilitas kredit modal kerja sindikasi korporasi dengan agunan fixed asset perusahaan."
                    tuple5(name, sec, amt, ten, n)
                }
            }

            val adjustedRate = (baseLendingRate + grade.rateSpreadAdjustment).coerceIn(0.04, 0.40)

            list.add(
                LoanApplication(
                    applicantName = applicantName,
                    sector = sector,
                    tier = tier,
                    creditGrade = grade,
                    principalAmount = amount,
                    tenorMonths = tenor,
                    annualInterestRate = adjustedRate,
                    isInternalCorporateSynergy = false,
                    linkedBusinessInstanceId = null,
                    linkedBusinessName = null,
                    proposalNote = note
                )
            )
        }

        return list
    }

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
    private fun <A, B, C, D, E> tuple5(a: A, b: B, c: C, d: D, e: E) = Tuple5(a, b, c, d, e)

    /**
     * Process monthly simulation tick for the Bank unit.
     */
    fun processMonthlyTick(
        bankData: BankingCompanyData,
        ownedBusinesses: List<OwnedBusiness>
    ): Pair<BankingCompanyData, List<OwnedBusiness>> {
        var currentDpk = bankData.totalCustomerDepositsDpk
        var internalCash = bankData.internalCash
        var lifetimeInterestEarned = bankData.lifetimeInterestEarned
        var lifetimeInterestPaid = bankData.lifetimeInterestPaidToDepositors
        var lifetimeLosses = bankData.lifetimeDefaultLossesWrittenOff
        var warningCount = bankData.centralBankWarningCount

        // 1. DPK Dynamics (Customer Deposits Inflow / Outflow based on Deposit Rate)
        // Market baseline deposit rate is ~4.0% (0.040)
        val depositRate = bankData.depositInterestRate
        val rateDiff = depositRate - 0.040
        val growthFactor = when {
            rateDiff >= 0.04 -> 0.08 + Random.nextDouble(0.02, 0.05) // Sangat tinggi (8-13% growth per month)
            rateDiff >= 0.02 -> 0.04 + Random.nextDouble(0.01, 0.03) // Menarik (5-7% growth)
            rateDiff >= 0.00 -> 0.01 + Random.nextDouble(0.005, 0.015) // Normal (1.5-2.5% growth)
            rateDiff >= -0.02 -> -0.01 - Random.nextDouble(0.005, 0.015) // Kurang kompetitif (-1.5% outflow)
            else -> -0.04 - Random.nextDouble(0.01, 0.03) // Sangat rendah -> nasabah kabur (-5% outflow)
        }

        val dpkChange = (currentDpk * growthFactor).toLong()
        currentDpk = maxOf(1_000_000L, currentDpk + dpkChange)

        // 2. Deposit Interest Expense (Beban Bunga Simpanan ke Nasabah)
        val monthlyDepositInterestExpense = (currentDpk * (depositRate / 12.0)).toLong()
        lifetimeInterestPaid += monthlyDepositInterestExpense

        // 3. Process Active Disbursed Loans
        val updatedLoans = mutableListOf<ActiveDisbursedLoan>()
        var monthlyInterestCollected = 0L
        var monthlyPrincipalCollected = 0L
        var monthlyDefaultWrittenOff = 0L

        val lendingRate = bankData.lendingInterestRate

        for (loan in bankData.activeLoans) {
            if (loan.healthStatus == LoanHealthStatus.SETTLED) {
                // Keep settled records clean or omit if old
                continue
            }

            if (loan.healthStatus == LoanHealthStatus.NON_PERFORMING) {
                // Already in default, keep in NPL book
                updatedLoans.add(loan)
                continue
            }

            // Calculate default risk for this month
            // Higher lending rate increases stress on borrower
            val stressMultiplier = 1.0 + max(0.0, (lendingRate - 0.12) * 2.5)
            val annualDefaultRate = loan.creditGrade.baseDefaultRisk * stressMultiplier
            val monthlyDefaultProbability = (annualDefaultRate / 12.0).coerceIn(0.001, 0.25)

            val roll = Random.nextDouble(0.0, 1.0)
            if (roll < monthlyDefaultProbability) {
                // Loan Defaults! (Kredit Macet / NPL)
                val defaultedLoan = loan.copy(
                    healthStatus = LoanHealthStatus.NON_PERFORMING,
                    consecutiveMissedPayments = loan.consecutiveMissedPayments + 1
                )
                monthlyDefaultWrittenOff += loan.remainingPrincipal
                lifetimeLosses += loan.remainingPrincipal
                updatedLoans.add(defaultedLoan)
            } else {
                // Performing loan pays monthly principal + interest
                val monthlyPrincipal = loan.monthlyPrincipalPayment
                val monthlyInterest = loan.monthlyInterestPayment
                
                monthlyInterestCollected += monthlyInterest
                monthlyPrincipalCollected += monthlyPrincipal
                lifetimeInterestEarned += monthlyInterest

                val newRemainingPrincipal = max(0L, loan.remainingPrincipal - monthlyPrincipal)
                val newRemainingMonths = max(0, loan.remainingMonths - 1)

                if (newRemainingMonths <= 0 || newRemainingPrincipal <= 0L) {
                    // Selesai / Lunas!
                    updatedLoans.add(
                        loan.copy(
                            remainingPrincipal = 0L,
                            remainingMonths = 0,
                            healthStatus = LoanHealthStatus.SETTLED,
                            totalInterestCollected = loan.totalInterestCollected + monthlyInterest
                        )
                    )
                } else {
                    updatedLoans.add(
                        loan.copy(
                            remainingPrincipal = newRemainingPrincipal,
                            remainingMonths = newRemainingMonths,
                            totalInterestCollected = loan.totalInterestCollected + monthlyInterest
                        )
                    )
                }
            }
        }

        // Net Interest Income for the month = Interest Revenue - Deposit Expense - Operational Overhead
        val aiOperationalCost = if (bankData.aiRiskManager.isEnabled) bankData.aiRiskManager.monthlyMaintenanceCost else 0L
        val bankOverhead = (currentDpk * 0.0005).toLong() + 25_000L + aiOperationalCost // Standard operational cost + AI subscription
        val netIncomeMonth = monthlyInterestCollected - monthlyDepositInterestExpense - bankOverhead

        // Absorb net income and loan cash flows into internal cash & vault cash
        internalCash += netIncomeMonth

        // 4. Central Bank Liquidity & Reserve Check (GWM)
        val totalActiveLoans = updatedLoans
            .filter { it.healthStatus != LoanHealthStatus.SETTLED && it.healthStatus != LoanHealthStatus.NON_PERFORMING }
            .sumOf { it.remainingPrincipal }
        
        val vaultCash = max(0L, currentDpk + internalCash - totalActiveLoans)
        val liquidityRatio = if (currentDpk > 0) (vaultCash.toDouble() / currentDpk.toDouble()) * 100.0 else 100.0

        if (liquidityRatio < 10.0) {
            warningCount++
            // Penalty fine for violating GWM Reserve
            val fine = 50_000L
            internalCash -= fine
        }

        // 5. Calculate NPL rate
        val nplPrincipal = updatedLoans.filter { it.healthStatus == LoanHealthStatus.NON_PERFORMING }.sumOf { it.remainingPrincipal }
        val allActivePrincipal = updatedLoans.filter { it.healthStatus != LoanHealthStatus.SETTLED }.sumOf { it.remainingPrincipal }
        val nplRate = if (allActivePrincipal > 0) (nplPrincipal.toDouble() / allActivePrincipal.toDouble()) * 100.0 else 0.0

        // 6. Refresh incoming applications if pipeline is depleted
        val refreshedApplications = if (bankData.incomingApplications.size < 4) {
            generateApplicationPipeline(
                currentTier = bankData.currentTier,
                baseLendingRate = bankData.lendingInterestRate,
                ownedBusinesses = ownedBusinesses,
                count = 6
            )
        } else {
            bankData.incomingApplications
        }

        var updatedBankData = bankData.copy(
            internalCash = internalCash,
            totalCustomerDepositsDpk = currentDpk,
            activeLoans = updatedLoans,
            incomingApplications = refreshedApplications,
            lifetimeInterestEarned = lifetimeInterestEarned,
            lifetimeInterestPaidToDepositors = lifetimeInterestPaid,
            lifetimeDefaultLossesWrittenOff = lifetimeLosses,
            centralBankWarningCount = warningCount,
            lastMonthNetIncome = netIncomeMonth,
            lastMonthNplRate = nplRate,
            lastRefreshedTimestamp = System.currentTimeMillis()
        )

        var finalOwned = ownedBusinesses

        // 7. Process AI Risk Manager Auto-Approval if active
        if (updatedBankData.aiRiskManager.isEnabled) {
            val aiResult = runAiRiskCycle(updatedBankData, ownedBusinesses)
            updatedBankData = aiResult.first
            finalOwned = aiResult.second
        }

        return Pair(updatedBankData, finalOwned)
    }

    /**
     * Run an AI Risk Manager automated underwriting cycle.
     */
    fun runAiRiskCycle(
        bankData: BankingCompanyData,
        ownedBusinesses: List<OwnedBusiness>
    ): Pair<BankingCompanyData, List<OwnedBusiness>> {
        val ai = bankData.aiRiskManager
        if (!ai.isEnabled || bankData.incomingApplications.isEmpty()) {
            return Pair(bankData, ownedBusinesses)
        }

        var currentBank = bankData
        var currentOwned = ownedBusinesses
        val remainingApps = mutableListOf<LoanApplication>()
        val newLogs = mutableListOf<AiRiskLogEntry>()
        val timeFormat = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US)
        val currentTime = timeFormat.format(java.util.Date())

        var approvedCount = 0
        var rejectedCount = 0
        var disbursedVol = 0L

        for (app in bankData.incomingApplications) {
            // 1. Check B2B Synergy auto-approve rule
            if (ai.autoApproveB2BSynergy && app.isInternalCorporateSynergy) {
                if (currentBank.vaultCash >= app.principalAmount) {
                    val res = approveLoan(currentBank, app, currentOwned)
                    if (res != null) {
                        currentBank = res.first
                        currentOwned = res.second
                        approvedCount++
                        disbursedVol += app.principalAmount
                        newLogs.add(
                            AiRiskLogEntry(
                                timestampFormatted = currentTime,
                                applicantName = app.applicantName,
                                amount = app.principalAmount,
                                grade = app.creditGrade,
                                action = "APPROVED",
                                reason = "B2B Synergy Disbursed to Subsidiary"
                            )
                        )
                        continue
                    }
                } else {
                    newLogs.add(
                        AiRiskLogEntry(
                            timestampFormatted = currentTime,
                            applicantName = app.applicantName,
                            amount = app.principalAmount,
                            grade = app.creditGrade,
                            action = "SKIPPED",
                            reason = "Vault Cash Insufficient"
                        )
                    )
                    remainingApps.add(app)
                    continue
                }
            }

            // 2. Check Auto-Reject rule
            if (app.creditGrade in ai.autoRejectGrades && !app.isInternalCorporateSynergy) {
                rejectedCount++
                newLogs.add(
                    AiRiskLogEntry(
                        timestampFormatted = currentTime,
                        applicantName = app.applicantName,
                        amount = app.principalAmount,
                        grade = app.creditGrade,
                        action = "REJECTED",
                        reason = "Grade ${app.creditGrade.name.takeLast(1)} in Auto-Reject Rule"
                    )
                )
                continue
            }

            // 3. Check Auto-Approve rule
            if (app.creditGrade in ai.autoApproveGrades && app.principalAmount <= ai.maxAutoApprovePrincipal) {
                // Accuracy check (Margin of error on level 1 / level 2 AI)
                val accuracyRoll = Random.nextDouble(0.0, 100.0)
                val isAccurate = accuracyRoll <= ai.accuracyPercent

                if (!isAccurate && app.creditGrade == CreditGrade.GRADE_A) {
                    // False negative
                    rejectedCount++
                    newLogs.add(
                        AiRiskLogEntry(
                            timestampFormatted = currentTime,
                            applicantName = app.applicantName,
                            amount = app.principalAmount,
                            grade = app.creditGrade,
                            action = "REJECTED",
                            reason = "AI Algorithmic Prudence (Margin Error)"
                        )
                    )
                    continue
                }

                if (currentBank.vaultCash >= app.principalAmount) {
                    val res = approveLoan(currentBank, app, currentOwned)
                    if (res != null) {
                        currentBank = res.first
                        currentOwned = res.second
                        approvedCount++
                        disbursedVol += app.principalAmount
                        newLogs.add(
                            AiRiskLogEntry(
                                timestampFormatted = currentTime,
                                applicantName = app.applicantName,
                                amount = app.principalAmount,
                                grade = app.creditGrade,
                                action = "APPROVED",
                                reason = "Auto-Approve Grade ${app.creditGrade.name.takeLast(1)} (${ai.accuracyPercent}% Acc.)"
                            )
                        )
                        continue
                    }
                } else {
                    newLogs.add(
                        AiRiskLogEntry(
                            timestampFormatted = currentTime,
                            applicantName = app.applicantName,
                            amount = app.principalAmount,
                            grade = app.creditGrade,
                            action = "SKIPPED",
                            reason = "Vault Cash Insufficient"
                        )
                    )
                    remainingApps.add(app)
                    continue
                }
            }

            // Not matched by rules -> keep for manual review
            remainingApps.add(app)
        }

        val combinedLogs = (newLogs + ai.executionLogs).take(30)
        val updatedAi = ai.copy(
            executionLogs = combinedLogs,
            totalProcessedCount = ai.totalProcessedCount + approvedCount + rejectedCount,
            totalApprovedCount = ai.totalApprovedCount + approvedCount,
            totalRejectedCount = ai.totalRejectedCount + rejectedCount,
            totalDisbursedVolume = ai.totalDisbursedVolume + disbursedVol
        )

        // If pipeline is now depleted, generate new pipeline
        val finalApps = if (remainingApps.size < 4) {
            generateApplicationPipeline(
                currentTier = currentBank.currentTier,
                baseLendingRate = currentBank.lendingInterestRate,
                ownedBusinesses = currentOwned,
                count = 6
            )
        } else {
            remainingApps
        }

        val finalBank = currentBank.copy(
            incomingApplications = finalApps,
            aiRiskManager = updatedAi
        )

        return Pair(finalBank, currentOwned)
    }

    /**
     * Approve a loan application:
     * - Disburses principal from vault cash (increases active loan book)
     * - If internal B2B synergy loan: injects capital into the target subsidiary's companyCash
     */
    fun approveLoan(
        bankData: BankingCompanyData,
        application: LoanApplication,
        ownedBusinesses: List<OwnedBusiness>
    ): Pair<BankingCompanyData, List<OwnedBusiness>>? {
        // Check if bank has sufficient vault cash
        if (bankData.vaultCash < application.principalAmount) {
            return null // Not enough liquidity
        }

        val newActiveLoan = ActiveDisbursedLoan(
            borrowerName = application.applicantName,
            sector = application.sector,
            tier = application.tier,
            creditGrade = application.creditGrade,
            originalPrincipal = application.principalAmount,
            remainingPrincipal = application.principalAmount,
            tenorMonthsTotal = application.tenorMonths,
            remainingMonths = application.tenorMonths,
            annualInterestRate = application.annualInterestRate,
            isInternalCorporateSynergy = application.isInternalCorporateSynergy,
            linkedBusinessInstanceId = application.linkedBusinessInstanceId,
            linkedBusinessName = application.linkedBusinessName,
            startMonthYear = "Active"
        )

        var updatedOwned = ownedBusinesses
        var lifetimeSynergy = bankData.lifetimeSynergyDisbursed

        // If internal corporate loan, inject funds directly into target business companyCash!
        if (application.isInternalCorporateSynergy && application.linkedBusinessInstanceId != null) {
            lifetimeSynergy += application.principalAmount
            updatedOwned = ownedBusinesses.map { biz ->
                if (biz.instanceId == application.linkedBusinessInstanceId) {
                    biz.copy(companyCash = biz.companyCash + application.principalAmount)
                } else {
                    biz
                }
            }
        }

        val updatedBankData = bankData.copy(
            activeLoans = bankData.activeLoans + newActiveLoan,
            incomingApplications = bankData.incomingApplications.filter { it.id != application.id },
            lifetimeSynergyDisbursed = lifetimeSynergy
        )

        return Pair(updatedBankData, updatedOwned)
    }

    /**
     * Reject a loan application proposal
     */
    fun rejectLoan(
        bankData: BankingCompanyData,
        applicationId: String
    ): BankingCompanyData {
        return bankData.copy(
            incomingApplications = bankData.incomingApplications.filter { it.id != applicationId }
        )
    }

    /**
     * Write-off / settle a defaulted NPL loan
     */
    fun writeOffNplLoan(
        bankData: BankingCompanyData,
        loanId: String
    ): BankingCompanyData {
        return bankData.copy(
            activeLoans = bankData.activeLoans.filter { it.id != loanId }
        )
    }
}
