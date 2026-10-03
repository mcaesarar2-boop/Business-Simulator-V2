package com.example.corporate.engine

import com.caesar.gametycoon.engine.cashflow.CentralCashFlowPipelineEngine
import com.caesar.gametycoon.engine.cashflow.adapters.UnitFinancialResolution
import com.example.corporate.model.CashDistributionResult
import com.example.corporate.model.MergedDividendPolicy
import com.example.corporate.model.StandaloneDividendPolicy
import com.example.data.HoldingCompany
import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Lead Financial Engine executing the 3-Tier Corporate Cash Flow (Arus Kas)
 * distribution logic across standalone and merged business entities.
 *
 * Fully integrated with and backed by [CentralCashFlowPipelineEngine].
 */
object CashFlowDistributionEngine {

    /**
     * Splits profit for a Standalone Business Unit (2-Tier: Mega Holding -> Business Unit).
     * Default: 30% Retained in Unit, 70% Profit Payout to Mega Holding.
     */
    fun calculateStandaloneProfitSplit(
        netProfit: Long,
        policy: StandaloneDividendPolicy = StandaloneDividendPolicy.DEFAULT
    ): Pair<Long, Long> {
        if (netProfit <= 0L) return Pair(0L, 0L)
        val retained = (netProfit * (policy.unitRetainedPercent / 100.0)).toLong()
        val payoutToMega = netProfit - retained
        return Pair(retained, payoutToMega)
    }

    /**
     * Splits profit for a Merged Business Unit (3-Tier: Mega Holding -> Holding Company -> Business Unit).
     * Default: 20% Retained in Unit, 50% Holding Internal Cash, 30% Upward Dividend to Mega Holding.
     */
    fun calculateMergedProfitSplit(
        netProfit: Long,
        policy: MergedDividendPolicy = MergedDividendPolicy.DEFAULT
    ): Triple<Long, Long, Long> {
        if (netProfit <= 0L) return Triple(0L, 0L, 0L)
        val unitRetained = (netProfit * (policy.unitRetainedPercent / 100.0)).toLong()
        val holdingTreasury = (netProfit * (policy.holdingTreasuryPercent / 100.0)).toLong()
        val megaDividend = (netProfit - unitRetained - holdingTreasury).coerceAtLeast(0L)
        return Triple(unitRetained, holdingTreasury, megaDividend)
    }

    /**
     * Processes cash distribution for a standalone business unit during a monthly tick.
     */
    fun processStandaloneBusinessCashFlow(
        business: OwnedBusiness,
        netProfit: Long,
        policy: StandaloneDividendPolicy = StandaloneDividendPolicy.DEFAULT
    ): Pair<OwnedBusiness, CashDistributionResult> {
        val gross = if (netProfit >= 0) netProfit else 0L
        val exp = if (netProfit < 0) -netProfit else 0L
        val resolution = UnitFinancialResolution(grossRevenue = gross, expenses = exp, updatedBusiness = business)

        val (result, _) = CentralCashFlowPipelineEngine.processUnitCashFlow(
            unit = business,
            parentHolding = null,
            state = PlayerState(),
            standalonePolicy = policy,
            customFinancialResolution = resolution
        )

        return Pair(result.updatedBusiness, result.toCashDistributionResult())
    }

    /**
     * Processes cash distribution for a merged subsidiary under a parent Holding Company.
     * Guarantees Holding Treasury receives its percentage payout (fixes $0 bug).
     */
    fun processMergedSubsidiaryCashFlow(
        subsidiary: OwnedBusiness,
        parentHolding: HoldingCompany,
        netProfit: Long,
        policy: MergedDividendPolicy = MergedDividendPolicy.DEFAULT
    ): Triple<OwnedBusiness, Double, CashDistributionResult> {
        val gross = if (netProfit >= 0) netProfit else 0L
        val exp = if (netProfit < 0) -netProfit else 0L
        val resolution = UnitFinancialResolution(grossRevenue = gross, expenses = exp, updatedBusiness = subsidiary)

        val (result, holdingDelta) = CentralCashFlowPipelineEngine.processUnitCashFlow(
            unit = subsidiary,
            parentHolding = parentHolding,
            state = PlayerState(holdingCompanies = listOf(parentHolding)),
            mergedPolicy = policy,
            customFinancialResolution = resolution
        )

        return Triple(result.updatedBusiness, holdingDelta, result.toCashDistributionResult())
    }

    /**
     * Executes cash distribution across an entire Holding Company and its subsidiaries.
     */
    fun processHoldingCompanyCashFlow(
        holding: HoldingCompany,
        subNetProfits: Map<String, Long>,
        policy: MergedDividendPolicy = MergedDividendPolicy.DEFAULT
    ): Pair<HoldingCompany, List<CashDistributionResult>> {
        val exec = CentralCashFlowPipelineEngine.processHoldingCompanyCashFlow(
            holding = holding,
            state = PlayerState(holdingCompanies = listOf(holding)),
            policy = policy,
            overrideSubsidiaryNetProfits = subNetProfits
        )

        val distributionResults = exec.subsidiaryResults.map { it.toCashDistributionResult() }
        return Pair(exec.updatedHolding, distributionResults)
    }

    /**
     * Performs a comprehensive cash flow distribution sweep for a PlayerState.
     */
    fun distributeAllCorporateCashFlows(
        currentState: PlayerState,
        standalonePolicies: Map<String, StandaloneDividendPolicy> = emptyMap(),
        holdingPolicies: Map<String, MergedDividendPolicy> = emptyMap()
    ): Triple<PlayerState, Long, List<CashDistributionResult>> {
        val distributionResults = mutableListOf<CashDistributionResult>()
        var totalMegaHoldingPayout = 0L

        // 1. Process Standalone Businesses
        val updatedOwned = currentState.ownedBusinesses.map { biz ->
            val isMerged = CentralCashFlowPipelineEngine.isUnitMerged(biz, currentState.holdingCompanies)
            if (!isMerged) {
                val net = biz.calculateGrossRevenue() - biz.calculateTotalExpenses()
                val policy = standalonePolicies[biz.instanceId] ?: StandaloneDividendPolicy.DEFAULT
                val (updatedBiz, result) = processStandaloneBusinessCashFlow(biz, net, policy)
                distributionResults.add(result)
                totalMegaHoldingPayout += result.megaHoldingDividend
                updatedBiz
            } else {
                biz
            }
        }

        // 2. Process Holding Companies & Merged Subsidiaries
        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            val policy = holdingPolicies[holding.instanceId] ?: MergedDividendPolicy.DEFAULT
            val (updatedHolding, results) = processHoldingCompanyCashFlow(holding, emptyMap(), policy)
            distributionResults.addAll(results)
            totalMegaHoldingPayout += results.sumOf { it.megaHoldingDividend }
            updatedHolding
        }

        val updatedState = currentState.copy(
            ownedBusinesses = updatedOwned,
            holdingCompanies = updatedHoldings
        )

        return Triple(updatedState, totalMegaHoldingPayout, distributionResults)
    }
}

/**
 * Convenience compatibility alias.
 */
typealias CashFlowEngine = CashFlowDistributionEngine
