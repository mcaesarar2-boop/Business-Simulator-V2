package com.caesar.gametycoon.engine.fix

import com.example.corporate.engine.CashFlowDistributionEngine
import com.example.corporate.model.CashDistributionResult
import com.example.corporate.model.CorporateTier
import com.example.corporate.model.MergedDividendPolicy
import com.example.corporate.model.StandaloneDividendPolicy
import com.example.corporate.repository.CorporatePolicyRepository
import com.example.data.HoldingCompany
import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Audit Fix: CashFlowFixEngine
 *
 * Resolves the 170%-200% Corporate Profit Duplication Glitch.
 *
 * ROOT CAUSE FIXED:
 * In legacy flow, businesses retained 20-30% of profits in [OwnedBusiness.companyCash],
 * holdings absorbed 50% into [HoldingCompany.holdingCash], but [PlayerState.cash] (Root Cash)
 * ALSO received 100% of gross [com.example.core.engine.AggregatedFinancials.netIncome].
 *
 * CORRECTED SPECIFICATION:
 * - Standalone Units: 30% retained in unit cash, 70% paid to Mega Holding.
 * - Merged Units: 20% retained in unit cash, 50% transferred to Sub-Holding treasury, 30% paid to Mega Holding.
 * - Root Cash ([PlayerState.cash]) ONLY receives [CashDistributionResult.megaHoldingDividend].
 * - Total Distributed = Retained + Treasury + Mega Dividend = exactly 100% of Net Profit.
 */
object CashFlowFixEngine {

    data class ReconciledCashFlow(
        val updatedState: PlayerState,
        val totalMegaHoldingPayout: Long,
        val totalUnitRetained: Long,
        val totalHoldingTreasuryInflow: Long,
        val distributionResults: List<CashDistributionResult>
    )

    /**
     * Calculates the true net dividend payout to Mega Holding Corporate Cash (cash),
     * completely eliminating the 170%-200% double-counting glitch.
     */
    fun calculateGlobalPayout(
        state: PlayerState,
        financials: com.example.core.engine.AggregatedFinancials,
        policyRepo: CorporatePolicyRepository = CorporatePolicyRepository.getInstance()
    ): Long {
        var totalPayout = 0L

        state.ownedBusinesses.forEach { biz ->
            val isMerged = !biz.parentId.isNullOrEmpty() ||
                    state.holdingCompanies.any { h -> h.subsidiaries.any { it.instanceId == biz.instanceId } }
            if (!isMerged) {
                val net = biz.calculateGrossRevenue() - biz.calculateTotalExpenses()
                if (net > 0L) {
                    val policy = policyRepo.getStandalonePolicy(biz.instanceId)
                    val (_, payout) = CashFlowDistributionEngine.calculateStandaloneProfitSplit(net, policy)
                    totalPayout += payout
                }
            }
        }

        state.holdingCompanies.forEach { holding ->
            val policy = policyRepo.getHoldingPolicy(holding.instanceId)
            holding.subsidiaries.forEach { sub ->
                val net = sub.calculateGrossRevenue() - sub.calculateTotalExpenses()
                if (net > 0L) {
                    val (_, _, megaDiv) = CashFlowDistributionEngine.calculateMergedProfitSplit(net, policy)
                    totalPayout += megaDiv
                }
            }
        }

        return totalPayout
    }

    /**
     * Convenience method returning the updated root cash balance.
     */
    fun calculateGlobalPayout(
        currentCash: Long,
        state: PlayerState,
        financials: com.example.core.engine.AggregatedFinancials
    ): Long {
        val payout = calculateGlobalPayout(state, financials)
        return (currentCash + payout).coerceAtLeast(0L)
    }

    /**
     * Executes mathematically sound profit allocation across all corporate tiers.
     */
    fun processReconciledMonthlyCashFlow(
        currentState: PlayerState,
        policyRepo: CorporatePolicyRepository = CorporatePolicyRepository.getInstance()
    ): ReconciledCashFlow {
        val distributionResults = mutableListOf<CashDistributionResult>()
        var totalMegaHoldingDividends = 0L
        var totalUnitRetained = 0L
        var totalHoldingTreasuryInflow = 0L

        // 1. Process Standalone Businesses (Mega Holding -> Business Unit)
        val updatedOwned = currentState.ownedBusinesses.map { biz ->
            val isMerged = !biz.parentId.isNullOrEmpty() ||
                    currentState.holdingCompanies.any { h -> h.subsidiaries.any { it.instanceId == biz.instanceId } }

            if (!isMerged) {
                val rev = biz.calculateGrossRevenue()
                val exp = biz.calculateTotalExpenses()
                val net = rev - exp
                val policy = policyRepo.getStandalonePolicy(biz.instanceId)

                val (updatedBiz, result) = processStandaloneUnit(biz, net, policy)
                distributionResults.add(result)

                totalMegaHoldingDividends += result.megaHoldingDividend
                totalUnitRetained += result.retainedInUnit
                updatedBiz
            } else {
                biz
            }
        }

        // 2. Process Merged Subsidiaries (Mega Holding -> Holding Company -> Business Unit)
        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            val holdingPolicy = policyRepo.getHoldingPolicy(holding.instanceId)
            var currentHoldingTreasury = holding.holdingCash

            val updatedSubs = holding.subsidiaries.map { sub ->
                val rev = sub.calculateGrossRevenue()
                val exp = sub.calculateTotalExpenses()
                val net = rev - exp

                val (updatedSub, treasuryDelta, result) = processMergedSubUnit(
                    subsidiary = sub,
                    parentHoldingCash = currentHoldingTreasury,
                    parentHoldingId = holding.instanceId,
                    netProfit = net,
                    policy = holdingPolicy
                )
                currentHoldingTreasury = (currentHoldingTreasury + treasuryDelta).coerceAtLeast(0.0)
                distributionResults.add(result)

                totalMegaHoldingDividends += result.megaHoldingDividend
                totalUnitRetained += result.retainedInUnit
                totalHoldingTreasuryInflow += result.holdingTreasuryTransfer
                updatedSub
            }

            holding.copy(
                subsidiaries = updatedSubs,
                holdingCash = currentHoldingTreasury
            )
        }

        // 3. Record audit trail in policy repository
        if (distributionResults.isNotEmpty()) {
            policyRepo.recordDistributionResults(distributionResults)
        }

        // 4. Root corporate cash ONLY receives totalMegaHoldingDividends (upward payouts)
        val newRootCash = (currentState.cash + totalMegaHoldingDividends).coerceAtLeast(0L)

        val finalState = currentState.copy(
            cash = newRootCash,
            ownedBusinesses = updatedOwned,
            holdingCompanies = updatedHoldings
        )

        return ReconciledCashFlow(
            updatedState = finalState,
            totalMegaHoldingPayout = totalMegaHoldingDividends,
            totalUnitRetained = totalUnitRetained,
            totalHoldingTreasuryInflow = totalHoldingTreasuryInflow,
            distributionResults = distributionResults
        )
    }

    private fun processStandaloneUnit(
        business: OwnedBusiness,
        netProfit: Long,
        policy: StandaloneDividendPolicy
    ): Pair<OwnedBusiness, CashDistributionResult> {
        val name = business.customName ?: business.catalogId

        if (netProfit >= 0L) {
            val retained = (netProfit * (policy.unitRetainedPercent / 100.0)).toLong()
            val megaDividend = netProfit - retained

            val updatedBiz = business.copy(
                companyCash = business.companyCash + retained.toDouble()
            )
            val result = CashDistributionResult(
                businessId = business.instanceId,
                businessName = name,
                netProfit = netProfit,
                retainedInUnit = retained,
                holdingTreasuryTransfer = 0L,
                megaHoldingDividend = megaDividend,
                absorbedDeficit = 0L,
                sourceTier = CorporateTier.BUSINESS_UNIT,
                parentHoldingId = null
            )
            return Pair(updatedBiz, result)
        } else {
            // Deficit handling: absorbed first by business unit cash
            val loss = -netProfit
            val availableCash = business.companyCash.toLong()
            val coveredByUnit = minOf(availableCash, loss)
            val remainingDeficit = loss - coveredByUnit

            val updatedBiz = business.copy(
                companyCash = (business.companyCash - coveredByUnit.toDouble()).coerceAtLeast(0.0)
            )
            val result = CashDistributionResult(
                businessId = business.instanceId,
                businessName = name,
                netProfit = netProfit,
                retainedInUnit = -coveredByUnit,
                holdingTreasuryTransfer = 0L,
                megaHoldingDividend = -remainingDeficit,
                absorbedDeficit = coveredByUnit,
                sourceTier = CorporateTier.BUSINESS_UNIT,
                parentHoldingId = null
            )
            return Pair(updatedBiz, result)
        }
    }

    private fun processMergedSubUnit(
        subsidiary: OwnedBusiness,
        parentHoldingCash: Double,
        parentHoldingId: String,
        netProfit: Long,
        policy: MergedDividendPolicy
    ): Triple<OwnedBusiness, Double, CashDistributionResult> {
        val name = subsidiary.customName ?: subsidiary.catalogId

        if (netProfit >= 0L) {
            val unitRetained = (netProfit * (policy.unitRetainedPercent / 100.0)).toLong()
            val holdingTreasury = (netProfit * (policy.holdingTreasuryPercent / 100.0)).toLong()
            val megaDividend = (netProfit - unitRetained - holdingTreasury).coerceAtLeast(0L)

            val updatedSub = subsidiary.copy(
                companyCash = subsidiary.companyCash + unitRetained.toDouble()
            )
            val result = CashDistributionResult(
                businessId = subsidiary.instanceId,
                businessName = name,
                netProfit = netProfit,
                retainedInUnit = unitRetained,
                holdingTreasuryTransfer = holdingTreasury,
                megaHoldingDividend = megaDividend,
                absorbedDeficit = 0L,
                sourceTier = CorporateTier.BUSINESS_UNIT,
                parentHoldingId = parentHoldingId
            )
            return Triple(updatedSub, holdingTreasury.toDouble(), result)
        } else {
            // Deficit: Unit cash -> Holding treasury -> Mega Holding
            val loss = -netProfit
            val unitCash = subsidiary.companyCash.toLong()
            val coveredByUnit = minOf(unitCash, loss)
            val remainingAfterUnit = loss - coveredByUnit

            val holdingCash = parentHoldingCash.toLong()
            val coveredByHolding = minOf(holdingCash, remainingAfterUnit)
            val remainingDeficitToMega = remainingAfterUnit - coveredByHolding

            val updatedSub = subsidiary.copy(
                companyCash = (subsidiary.companyCash - coveredByUnit.toDouble()).coerceAtLeast(0.0)
            )
            val result = CashDistributionResult(
                businessId = subsidiary.instanceId,
                businessName = name,
                netProfit = netProfit,
                retainedInUnit = -coveredByUnit,
                holdingTreasuryTransfer = -coveredByHolding,
                megaHoldingDividend = -remainingDeficitToMega,
                absorbedDeficit = coveredByUnit + coveredByHolding,
                sourceTier = CorporateTier.BUSINESS_UNIT,
                parentHoldingId = parentHoldingId
            )
            return Triple(updatedSub, -coveredByHolding.toDouble(), result)
        }
    }
}
