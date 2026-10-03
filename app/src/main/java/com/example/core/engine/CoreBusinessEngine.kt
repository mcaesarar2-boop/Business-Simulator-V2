package com.example.core.engine

import com.example.corporate.engine.CashFlowDistributionEngine
import com.example.corporate.model.CashDistributionResult
import com.example.corporate.repository.CorporatePolicyRepository
import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Domain engine responsible for standard businesses not handled by isolated industry subsystems.
 * Covers: Retail (warung, fnb, supermarket), Tech Startups, Manufacturing, Automotive,
 * Healthcare, Education, Mining, Energy, Media Radio/TV, and standard Holding Subsidiaries.
 * Also processes portfolio passive dividends and executes 3-tier cash flow distribution.
 */
class CoreBusinessEngine : MonthlyTickSubscriber {

    override val subscriberId: String = "core_business_engine"

    companion object {
        val SPECIALIZED_SUBSYSTEM_IDS = setOf(
            "content_creator",
            "streaming_service",
            "banking_finance",
            "theme_park",
            "hospitality_hotel",
            "sports_football",
            "aviation_subsystem",
            "mid_logistics",
            "construction_developer",
            "media_production",
            "indie_game_publisher",
            "ai_cloud_provider"
        )
    }

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        var totalGrossRevenue = 0L
        var totalExpenses = 0L
        val policyRepo = CorporatePolicyRepository.getInstance()
        val distributionResults = mutableListOf<CashDistributionResult>()

        val updatedOwned = currentState.ownedBusinesses.map { biz ->
            if (biz.catalogId !in SPECIALIZED_SUBSYSTEM_IDS) {
                val rev = biz.calculateGrossRevenue()
                val exp = biz.calculateTotalExpenses()
                val net = rev - exp
                if (biz.parentId.isNullOrEmpty()) {
                    totalGrossRevenue += rev
                    totalExpenses += exp
                    val policy = policyRepo.getStandalonePolicy(biz.instanceId)
                    val (updatedBiz, result) = CashFlowDistributionEngine.processStandaloneBusinessCashFlow(
                        business = biz,
                        netProfit = net,
                        policy = policy
                    )
                    distributionResults.add(result)
                    updatedBiz
                } else {
                    biz
                }
            } else {
                biz
            }
        }

        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            val holdingPolicy = policyRepo.getHoldingPolicy(holding.instanceId)
            var currentTreasuryCash = holding.holdingCash
            var changed = false
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.catalogId !in SPECIALIZED_SUBSYSTEM_IDS) {
                    changed = true
                    val rev = sub.calculateGrossRevenue()
                    val exp = sub.calculateTotalExpenses()
                    totalGrossRevenue += rev
                    totalExpenses += exp
                    val net = rev - exp
                    val (updatedSub, treasuryDelta, result) = CashFlowDistributionEngine.processMergedSubsidiaryCashFlow(
                        subsidiary = sub,
                        parentHolding = holding.copy(holdingCash = currentTreasuryCash),
                        netProfit = net,
                        policy = holdingPolicy
                    )
                    currentTreasuryCash = (currentTreasuryCash + treasuryDelta).coerceAtLeast(0.0)
                    distributionResults.add(result)
                    updatedSub
                } else {
                    sub
                }
            }
            if (changed) holding.copy(subsidiaries = newSubs, holdingCash = currentTreasuryCash) else holding
        }

        if (distributionResults.isNotEmpty()) {
            policyRepo.recordDistributionResults(distributionResults)
        }

        // Dividen portofolio saham (asumsi yield dividen pasar ~3.6% tahunan atau ~0.3%/bulan)
        var stockDividends = 0L
        currentState.ownedStocks.forEach { stock ->
            if (stock.shares > 0 && stock.averagePrice > 0.0) {
                val div = (stock.shares * stock.averagePrice * 0.003).toLong()
                stockDividends += div
            }
        }
        totalGrossRevenue += stockDividends

        // Program TV aktif jika memiliki media_tv
        var tvRevenue = 0L
        var tvCosts = 0L
        currentState.activeTvPrograms.forEach { prog ->
            if (prog.active) {
                tvRevenue += prog.monthlyAdRevenue.toLong()
                tvCosts += prog.currentOperationalCost.toLong()
            }
        }
        totalGrossRevenue += tvRevenue
        totalExpenses += tvCosts

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = totalGrossRevenue,
            expenses = totalExpenses,
            dividendToGlobal = stockDividends,
            stateModifier = { state ->
                state.copy(
                    ownedBusinesses = updatedOwned,
                    holdingCompanies = updatedHoldings
                )
            }
        )
    }
}
