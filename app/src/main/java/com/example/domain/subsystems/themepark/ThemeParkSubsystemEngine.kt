package com.example.domain.subsystems.themepark

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.ActiveBidding
import com.example.data.BiddingPhase
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import com.example.data.RideTier
import com.example.data.ThemeParkBranch
import com.example.data.ThemeParkRide
import com.example.viewmodel.ThemeParkEngine as LegacyThemeParkEngine
import java.util.UUID

/**
 * Domain subsystem engine for Theme Park Holdings.
 * Handles:
 * 1. Seasonal multipliers (summer vacation, winter holiday boosts vs. rainy off-season dips).
 * 2. Active bidding progression for city land parcels.
 * 3. Monthly ride maintenance wear and tear, construction completion countdowns.
 * 4. User actions: ride construction, land bidding offers, ad package activations.
 */
class ThemeParkSubsystemEngine : MonthlyTickSubscriber {

    override val subscriberId: String = "theme_park_holding"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        var totalProfit = 0L

        val seasonMultiplier = when (event.currentMonth) {
            6, 7 -> 3.0 // Peak summer holiday
            12 -> 4.0   // Peak Christmas & New Year holiday
            2, 9 -> 0.7 // Rainy / School re-entry low season
            else -> 1.0
        }

        val policyRepo = com.example.corporate.repository.CorporatePolicyRepository.getInstance()
        val distributionResults = mutableListOf<com.example.corporate.model.CashDistributionResult>()

        val updatedOwned = currentState.ownedBusinesses.map { biz ->
            if (biz.catalogId == "theme_park_holding") {
                val (updatedBiz, profit) = processBusinessMonthly(biz, seasonMultiplier)
                totalProfit += profit
                val policy = policyRepo.getStandalonePolicy(biz.instanceId)
                val (finalBiz, result) = com.example.corporate.engine.CashFlowDistributionEngine.processStandaloneBusinessCashFlow(
                    business = updatedBiz,
                    netProfit = profit,
                    policy = policy
                )
                distributionResults.add(result)
                finalBiz
            } else biz
        }

        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            var changed = false
            var currentTreasuryCash = holding.holdingCash
            val holdingPolicy = policyRepo.getHoldingPolicy(holding.instanceId)
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.catalogId == "theme_park_holding") {
                    changed = true
                    val (updatedSub, profit) = processBusinessMonthly(sub, seasonMultiplier)
                    totalProfit += profit
                    val (finalSub, treasuryDelta, result) = com.example.corporate.engine.CashFlowDistributionEngine.processMergedSubsidiaryCashFlow(
                        subsidiary = updatedSub,
                        parentHolding = holding.copy(holdingCash = currentTreasuryCash),
                        netProfit = profit,
                        policy = holdingPolicy
                    )
                    currentTreasuryCash = (currentTreasuryCash + treasuryDelta).coerceAtLeast(0.0)
                    distributionResults.add(result)
                    finalSub
                } else sub
            }
            if (changed) holding.copy(subsidiaries = newSubs, holdingCash = currentTreasuryCash) else holding
        }

        if (distributionResults.isNotEmpty()) {
            policyRepo.recordDistributionResults(distributionResults)
        }

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = if (totalProfit > 0) totalProfit else 0L,
            expenses = if (totalProfit < 0) -totalProfit else 0L,
            dividendToGlobal = 0L,
            logMessages = listOf("Theme Park: Monthly net outcome: $totalProfit"),
            stateModifier = { state ->
                state.copy(
                    ownedBusinesses = updatedOwned,
                    holdingCompanies = updatedHoldings
                )
            }
        )
    }

    private fun processBusinessMonthly(
        business: OwnedBusiness,
        seasonMultiplier: Double
    ): Pair<OwnedBusiness, Long> {
        var netProfit = 0L

        // Update branches
        val updatedBranches = business.themeParkBranches.map { branch ->
            val updatedBranch = LegacyThemeParkEngine.processMonthlyTick(branch, seasonMultiplier)
            netProfit += updatedBranch.lastMonthProfit
            updatedBranch
        }

        // Process active biddings
        val updatedBiddings = business.activeThemeParkBiddings.map { bidding ->
            if (bidding.monthsLeft > 0) {
                bidding.copy(monthsLeft = bidding.monthsLeft - 1)
            } else {
                when (bidding.phase) {
                    BiddingPhase.WAITING_INITIAL -> {
                        val rand = (1..100).random()
                        if (rand <= 30) bidding.copy(phase = BiddingPhase.DEAL_REACHED)
                        else bidding.copy(
                            phase = BiddingPhase.OWNER_COUNTERED,
                            currentAskingPrice = (bidding.currentAskingPrice * 1.2).toLong(),
                            monthsLeft = 2
                        )
                    }
                    BiddingPhase.WAITING_REPLY -> {
                        val offer = bidding.playerOffer
                        val target = bidding.currentAskingPrice
                        if (offer >= target) bidding.copy(phase = BiddingPhase.DEAL_REACHED)
                        else bidding.copy(phase = BiddingPhase.REJECTED)
                    }
                    else -> bidding
                }
            }
        }.filter { it.phase != BiddingPhase.REJECTED }

        val updatedBiz = business.copy(
            themeParkBranches = updatedBranches,
            activeThemeParkBiddings = updatedBiddings
        )
        return Pair(updatedBiz, netProfit)
    }

    // --- User Actions / Reducers ---

    fun submitBiddingOffer(
        business: OwnedBusiness,
        biddingId: String,
        offer: Long
    ): OwnedBusiness {
        val updatedBiddings = business.activeThemeParkBiddings.map { bidding ->
            if (bidding.id == biddingId) {
                bidding.copy(
                    phase = BiddingPhase.WAITING_REPLY,
                    monthsLeft = 2,
                    playerOffer = offer
                )
            } else bidding
        }
        return business.copy(activeThemeParkBiddings = updatedBiddings)
    }

    fun buildRide(
        business: OwnedBusiness,
        branchId: String,
        rideTier: RideTier,
        customRideName: String,
        imageUrl: String? = null,
        zoneName: String? = null,
        ipThemeTitle: String? = null,
        ipThemeScore: Int? = null
    ): OwnedBusiness? {
        if (business.companyCash < rideTier.cost) return null

        val updatedBranches = business.themeParkBranches.map { branch ->
            if (branch.id == branchId) {
                val newRide = ThemeParkRide(
                    id = UUID.randomUUID().toString(),
                    name = customRideName.ifBlank { rideTier.description },
                    tierDescription = rideTier.description,
                    cost = rideTier.cost,
                    zoneName = zoneName ?: "Fantasy Land",
                    imageUrl = imageUrl,
                    constructionMonthsLeft = rideTier.buildMonths,
                    isConstructing = rideTier.buildMonths > 0,
                    ipThemeTitle = ipThemeTitle,
                    ipThemeScore = ipThemeScore
                )
                branch.copy(rides = (branch.rides + newRide).toMutableList())
            } else branch
        }

        return business.copy(
            companyCash = business.companyCash - rideTier.cost.toDouble(),
            themeParkBranches = updatedBranches
        )
    }
}
