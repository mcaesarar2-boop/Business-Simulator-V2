package com.caesar.gametycoon.engine.fix

import com.example.corporate.engine.CashFlowDistributionEngine
import com.example.corporate.model.StandaloneDividendPolicy
import com.example.corporate.repository.CorporatePolicyRepository
import com.example.data.ContentStatus
import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Audit Fix: ContentCreatorCashFixEngine
 *
 * Resolves financial calculation desyncs and ghost negative deductions
 * in the Content Creator subsystem:
 *
 * 1. REVENUE DESYNC BUG:
 *    Forces dynamic ContentCreatorEngine earnings (AdSense + Sponsorship Contracts +
 *    Portfolio Royalties) to overwrite stale generic catalog values in [OwnedBusiness.customRevenue].
 *
 * 2. NET PROFIT MISMATCH BUG:
 *    Enforces the canonical accounting equation:
 *    NetIncome = TotalSubsystemRevenue - TotalMaintenanceExpenses - ExplicitTaxes
 *    Completely eliminates ghost negative deductions that resulted in -$14,814.
 *
 * 3. STANDALONE PROFIT PAYOUT:
 *    Guarantees that positive net profit splits cleanly according to policy
 *    (Default: 70% Mega Holding Main Cash Balance, 30% Unit Company Cash).
 */
object ContentCreatorCashFixEngine {

    const val CATALOG_ID = "content_creator"
    const val DEFAULT_BASE_MAINTENANCE = 500L
    const val EMPLOYEE_SALARY_PER_HEAD = 2000L

    data class PayoutBreakdown(
        val businessId: String,
        val businessName: String,
        val grossRevenue: Long,
        val maintenanceExpenses: Long,
        val explicitTaxes: Long,
        val netIncome: Long,
        val dividendPayout: Long,
        val unitRetained: Long
    )

    data class ContentCreatorPayoutResult(
        val updatedState: PlayerState,
        val totalRevenue: Long,
        val totalExpenses: Long,
        val netIncome: Long,
        val totalDividendPaidToMainCash: Long,
        val totalRetainedInUnitCash: Long,
        val breakdowns: List<PayoutBreakdown>
    )

    /**
     * Calculates the true monthly subsystem gross revenue:
     * AdSense (scaled by employees) + Active Brand Deal Sponsorships + Licensed Content Royalties.
     */
    fun calculateSubsystemRevenue(business: OwnedBusiness): Long {
        if (business.catalogId != CATALOG_ID) {
            return business.calculateGrossRevenue()
        }

        val employeeMultiplier = 1.0 + (business.contentCreatorEmployees * 0.05)
        val baseAdSense = (business.contentCreatorSubscribers * 0.05).toLong()
        val totalAdSense = (baseAdSense * employeeMultiplier).toLong()

        val contractsRevenue = business.contentCreatorContracts.sumOf { it.monthlyPayout }

        val royaltiesRevenue = business.contentPortfolio
            .filter { it.status == ContentStatus.LICENSED }
            .sumOf { it.monthlyRoyalty }

        return totalAdSense + contractsRevenue + royaltiesRevenue
    }

    /**
     * Calculates total maintenance expenses for the Content Creator studio.
     */
    fun calculateSubsystemExpenses(business: OwnedBusiness): Long {
        if (business.catalogId != CATALOG_ID) {
            return business.calculateTotalExpenses()
        }
        val employeeExpenses = business.contentCreatorEmployees * EMPLOYEE_SALARY_PER_HEAD
        return DEFAULT_BASE_MAINTENANCE + employeeExpenses
    }

    /**
     * Synchronizes a single OwnedBusiness entity so its grossRevenue reflects
     * the live ContentCreatorEngine output via [OwnedBusiness.customRevenue].
     */
    fun syncSubsystemRevenue(business: OwnedBusiness): OwnedBusiness {
        if (business.catalogId != CATALOG_ID) return business
        val trueRevenue = calculateSubsystemRevenue(business)
        return business.copy(customRevenue = trueRevenue)
    }

    /**
     * Forces ContentCreatorEngine output to overwrite ownedBusinesses.customRevenue
     * across standalone and holding-owned creator units.
     */
    fun syncSubsystemRevenue(state: PlayerState): PlayerState {
        val updatedOwned = state.ownedBusinesses.map { biz ->
            if (biz.catalogId == CATALOG_ID) syncSubsystemRevenue(biz) else biz
        }

        val updatedHoldings = state.holdingCompanies.map { holding ->
            val updatedSubs = holding.subsidiaries.map { sub ->
                if (sub.catalogId == CATALOG_ID) syncSubsystemRevenue(sub) else sub
            }
            holding.copy(subsidiaries = updatedSubs)
        }

        return state.copy(
            ownedBusinesses = updatedOwned,
            holdingCompanies = updatedHoldings
        )
    }

    /**
     * Fixes the Net Profit calculation by strictly enforcing:
     * NetIncome = TotalSubsystemRevenue - TotalMaintenanceExpenses - ExplicitTaxes
     * Eliminates rogue ghost negative deductions.
     */
    fun recalculateNetIncome(
        totalSubsystemRevenue: Long,
        totalMaintenanceExpenses: Long,
        explicitTaxes: Long = 0L
    ): Long {
        return totalSubsystemRevenue - totalMaintenanceExpenses - explicitTaxes
    }

    /**
     * Convenience method to recalculate net income for an OwnedBusiness instance.
     */
    fun recalculateNetIncome(business: OwnedBusiness, explicitTaxes: Long = 0L): Long {
        val revenue = calculateSubsystemRevenue(business)
        val expenses = calculateSubsystemExpenses(business)
        return recalculateNetIncome(revenue, expenses, explicitTaxes)
    }

    /**
     * Ensures that if Net Income > 0, the configured 70% dividend is explicitly added
     * to PlayerState.cash (Main Balance) and 30% to companyCash/contentCreatorCash.
     */
    fun forceStandalonePayout(
        currentState: PlayerState,
        policy: StandaloneDividendPolicy = StandaloneDividendPolicy.DEFAULT,
        explicitTaxes: Long = 0L
    ): ContentCreatorPayoutResult {
        var totalRev = 0L
        var totalExp = 0L
        var totalNet = 0L
        var totalDividend = 0L
        var totalRetained = 0L
        val breakdowns = mutableListOf<PayoutBreakdown>()

        val updatedOwnedBusinesses = currentState.ownedBusinesses.map { biz ->
            if (biz.catalogId == CATALOG_ID && biz.parentId.isNullOrEmpty()) {
                val rev = calculateSubsystemRevenue(biz)
                val exp = calculateSubsystemExpenses(biz)
                val net = recalculateNetIncome(rev, exp, explicitTaxes)

                totalRev += rev
                totalExp += exp
                totalNet += net

                if (net > 0L) {
                    val (retained, dividend) = CashFlowDistributionEngine.calculateStandaloneProfitSplit(net, policy)
                    totalDividend += dividend
                    totalRetained += retained

                    breakdowns.add(
                        PayoutBreakdown(
                            businessId = biz.instanceId,
                            businessName = biz.customName ?: "Content Creator Studio",
                            grossRevenue = rev,
                            maintenanceExpenses = exp,
                            explicitTaxes = explicitTaxes,
                            netIncome = net,
                            dividendPayout = dividend,
                            unitRetained = retained
                        )
                    )

                    biz.copy(
                        customRevenue = rev,
                        companyCash = biz.companyCash + retained.toDouble(),
                        contentCreatorCash = biz.contentCreatorCash + retained
                    )
                } else {
                    biz.copy(customRevenue = rev)
                }
            } else {
                biz
            }
        }

        val finalPlayerState = currentState.copy(
            cash = currentState.cash + totalDividend,
            ownedBusinesses = updatedOwnedBusinesses
        )

        return ContentCreatorPayoutResult(
            updatedState = finalPlayerState,
            totalRevenue = totalRev,
            totalExpenses = totalExp,
            netIncome = totalNet,
            totalDividendPaidToMainCash = totalDividend,
            totalRetainedInUnitCash = totalRetained,
            breakdowns = breakdowns
        )
    }
}
