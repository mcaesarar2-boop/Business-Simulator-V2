package com.example.domain.subsystems.banking

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.LoanApplication
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import com.example.viewmodel.BankingEngine as LegacyBankingEngine

/**
 * Domain subsystem engine for Tycoon Banking.
 * Handles:
 * 1. Monthly credit portfolio underwriting, risk grading, and interest collection.
 * 2. Automated Non-Performing Loan (NPL) defaults and recoveries.
 * 3. Deposit reserve maintenance, interest payouts, and liquidity calculations.
 * 4. User actions: loan approvals/rejections, NPL write-offs, AI risk manager cycles.
 */
class BankingSubsystemEngine : MonthlyTickSubscriber {

    override val subscriberId: String = "tycoon_bank"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        var totalBankProfit = 0L

        // Gather all businesses for synergy checks and internal banking accounts
        val allBusinesses = currentState.ownedBusinesses + currentState.holdingCompanies.flatMap { it.subsidiaries }

        val updatedOwned = currentState.ownedBusinesses.map { biz ->
            if (biz.catalogId == "tycoon_bank") {
                val (updatedBankData, _) = LegacyBankingEngine.processMonthlyTick(biz.bankingData, allBusinesses)
                val profit = updatedBankData.lastMonthNetIncome
                totalBankProfit += profit
                biz.copy(bankingData = updatedBankData)
            } else biz
        }

        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            var changed = false
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.catalogId == "tycoon_bank") {
                    changed = true
                    val (updatedBankData, _) = LegacyBankingEngine.processMonthlyTick(sub.bankingData, allBusinesses)
                    val profit = updatedBankData.lastMonthNetIncome
                    totalBankProfit += profit
                    sub.copy(bankingData = updatedBankData)
                } else sub
            }
            if (changed) holding.copy(subsidiaries = newSubs) else holding
        }

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = if (totalBankProfit > 0) totalBankProfit else 0L,
            expenses = if (totalBankProfit < 0) -totalBankProfit else 0L,
            dividendToGlobal = 0L,
            logMessages = listOf("Banking: Monthly Net Income: $totalBankProfit"),
            stateModifier = { state ->
                state.copy(
                    ownedBusinesses = updatedOwned,
                    holdingCompanies = updatedHoldings
                )
            }
        )
    }

    // --- User Actions / Reducers ---

    fun approveLoan(
        bankBusiness: OwnedBusiness,
        application: LoanApplication,
        allBusinesses: List<OwnedBusiness>
    ): Pair<OwnedBusiness, List<OwnedBusiness>>? {
        val result = LegacyBankingEngine.approveLoan(bankBusiness.bankingData, application, allBusinesses) ?: return null
        val (updatedBankData, updatedAll) = result
        return Pair(bankBusiness.copy(bankingData = updatedBankData), updatedAll)
    }

    fun rejectLoan(bankBusiness: OwnedBusiness, applicationId: String): OwnedBusiness {
        val updated = LegacyBankingEngine.rejectLoan(bankBusiness.bankingData, applicationId)
        return bankBusiness.copy(bankingData = updated)
    }

    fun writeOffNpl(bankBusiness: OwnedBusiness, loanId: String): OwnedBusiness {
        val updated = LegacyBankingEngine.writeOffNplLoan(bankBusiness.bankingData, loanId)
        return bankBusiness.copy(bankingData = updated)
    }

    fun runAiRiskCycle(
        bankBusiness: OwnedBusiness,
        allBusinesses: List<OwnedBusiness>
    ): Pair<OwnedBusiness, List<OwnedBusiness>> {
        val (updatedBankData, updatedAll) = LegacyBankingEngine.runAiRiskCycle(bankBusiness.bankingData, allBusinesses)
        return Pair(bankBusiness.copy(bankingData = updatedBankData), updatedAll)
    }
}
