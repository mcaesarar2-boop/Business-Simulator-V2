package com.example.privateequity.engine

import com.example.data.MarketNews
import com.example.data.PlayerState
import com.example.privateequity.data.PrivateEquityRepository
import com.example.privateequity.model.ActiveLoan
import com.example.privateequity.model.FundingType
import com.example.privateequity.model.LoanApplicationResult
import com.example.privateequity.model.LoanEstimation
import com.example.privateequity.model.SectorOffer
import com.example.ui.formatCurrencyRingkas
import java.util.Locale
import kotlin.math.ceil

/**
 * Pure calculation & execution engine for Private Equity & Investor financing.
 * Ensures corporate cash (cash utama) receives financing disbursements and pays monthly debt obligations.
 */
object PrivateEquityEngine {

    /**
     * Estimates loan terms and dilution for live UI feedback.
     */
    fun calculateEstimation(
        loanAmount: Long,
        sector: SectorOffer,
        valuation: Long,
        fundingType: FundingType,
        currentEquity: Double
    ): LoanEstimation {
        val safeValuation = valuation.coerceAtLeast(100_000L)
        val baseEquityGiven = (loanAmount.toDouble() / safeValuation.toDouble()) * sector.dilutionMultiplier * 100.0
        val totalBunga = (loanAmount * sector.interestRate).toLong()
        val totalPayment = loanAmount + totalBunga
        val baseMonthlyPayment = ceil(totalPayment.toDouble() / sector.tenor).toLong()

        val (monthlyPayment, equityGiven) = when (fundingType) {
            FundingType.DEBT -> Pair(baseMonthlyPayment, 0.0)
            FundingType.HYBRID -> Pair(baseMonthlyPayment / 2, baseEquityGiven / 2.0)
            FundingType.EQUITY -> Pair(0L, baseEquityGiven)
        }

        val remainingEquityPostDilution = (currentEquity - equityGiven).coerceAtLeast(0.0)
        val isControlViolation = remainingEquityPostDilution < 51.0

        return LoanEstimation(
            baseMonthlyPayment = baseMonthlyPayment,
            baseEquityGiven = baseEquityGiven,
            monthlyPayment = monthlyPayment,
            equityGiven = equityGiven,
            remainingEquityPostDilution = remainingEquityPostDilution,
            isControlViolation = isControlViolation
        )
    }

    /**
     * Executes the loan application:
     * - Validates 51% control ownership threshold.
     * - Adds the disbursed funds directly to Kas Utama (cash), NOT privateBalance.
     * - Records the active loan and applies equity dilution if applicable.
     */
    fun applyForInvestorLoan(
        state: PlayerState,
        sectorName: String,
        loanAmount: Long,
        tenorMonths: Int,
        interestRate: Double,
        dilutionMultiplier: Double,
        fundingType: FundingType
    ): LoanApplicationResult {
        val valuation = PrivateEquityRepository.getBusinessValuation(state)
        val estimation = calculateEstimation(
            loanAmount = loanAmount,
            sector = SectorOffer(
                name = sectorName,
                interestRate = interestRate,
                tenor = tenorMonths,
                dilutionMultiplier = dilutionMultiplier,
                description = ""
            ),
            valuation = valuation,
            fundingType = fundingType,
            currentEquity = state.playerEquityShare
        )

        if (estimation.isControlViolation) {
            return LoanApplicationResult(
                isSuccess = false,
                message = "Pengajuan ditolak! Total kepemilikan saham Anda akan jatuh di bawah 51% (${String.format(Locale.US, "%.1f", estimation.remainingEquityPostDilution)}%) yang melanggar syarat mutlak kontrol holding."
            )
        }

        val newLoan = ActiveLoan(
            sectorName = sectorName,
            fundingType = fundingType,
            totalLoan = loanAmount,
            monthlyPayment = estimation.monthlyPayment,
            remainingMonths = tenorMonths,
            equityGiven = estimation.equityGiven
        )

        val newEquity = estimation.remainingEquityPostDilution

        // IMPORTANT: Funds are credited directly to Kas Utama Perusahaan (cash), NOT privateBalance!
        val newState = state.copy(
            cash = state.cash + loanAmount,
            playerEquityShare = newEquity,
            companyOwnershipPercent = newEquity,
            activeInvestorsLoans = state.activeInvestorsLoans + newLoan,
            megaHolding = state.megaHolding.copy(ownershipPercentage = newEquity)
        )

        val logFundingText = when (fundingType) {
            FundingType.DEBT -> "Debt Financing (Cicilan penuh, 0% dilusi)"
            FundingType.HYBRID -> "Mezzanine (Hybrid: Cicilan 50%, dilusi ${String.format(Locale.US, "%.1f", estimation.equityGiven)}%)"
            FundingType.EQUITY -> "Venture Capital (0 cicilan, dilusi ${String.format(Locale.US, "%.1f", estimation.equityGiven)}%)"
        }

        val newsItem = MarketNews(
            id = "loan_taken_${System.currentTimeMillis()}",
            text = "PRIVATE EQUITY: Mengambil fasilitas pendanaan $logFundingText senilai ${formatCurrencyRingkas(loanAmount, false)} dari Sektor $sectorName ($tenorMonths bln). Dana masuk ke Kas Utama Perusahaan.",
            type = "BULL"
        )

        return LoanApplicationResult(
            isSuccess = true,
            message = "Pengajuan dana sebesar ${formatCurrencyRingkas(loanAmount, false)} disetujui! Dana langsung cair ke Kas Utama Perusahaan.",
            updatedState = newState,
            newsFeedItem = newsItem
        )
    }

    /**
     * Processes monthly debt repayment during the global monthly game tick:
     * - Deducts monthly installment from available corporate cash.
     * - Decrements remaining tenor months of active loans.
     * - Removes loans that have reached zero remaining months.
     */
    fun processMonthlyDebtTick(
        state: PlayerState,
        availableCorpCash: Long
    ): MonthlyDebtTickResult {
        val activeLoans = state.activeInvestorsLoans
        if (activeLoans.isEmpty()) {
            return MonthlyDebtTickResult(
                updatedLoans = emptyList(),
                totalPaid = 0L,
                unpaidDebt = 0L
            )
        }

        val totalObligation = activeLoans.sumOf { it.monthlyPayment }
        val actualPaid = totalObligation.coerceAtMost(availableCorpCash.coerceAtLeast(0L))
        val unpaidDelta = (totalObligation - actualPaid).coerceAtLeast(0L)

        val updatedLoans = activeLoans.mapNotNull { loan ->
            val nextMonths = loan.remainingMonths - 1
            if (nextMonths > 0) {
                loan.copy(remainingMonths = nextMonths)
            } else {
                null // Loan fully paid off
            }
        }

        return MonthlyDebtTickResult(
            updatedLoans = updatedLoans,
            totalPaid = actualPaid,
            unpaidDebt = unpaidDelta
        )
    }
}

/**
 * Result data for monthly debt tick calculation.
 */
data class MonthlyDebtTickResult(
    val updatedLoans: List<ActiveLoan>,
    val totalPaid: Long,
    val unpaidDebt: Long
)
