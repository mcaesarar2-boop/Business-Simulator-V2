package com.example.domain.subsystems.construction

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.ConstructionProject
import com.example.data.ConstructionTenderOpportunity
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import com.example.viewmodel.ConstructionEngine as LegacyConstructionEngine

/**
 * Domain subsystem engine for Construction Firm / Infrastructure Contractor.
 * Handles:
 * 1. Monthly project phase construction progression, weather delays, and milestone completions.
 * 2. Termin billing payouts and logistics synergy bonuses (+5% payout when in-house logistics exists).
 * 3. Trust score progression and contractor certification.
 * 4. User actions: bidding on public/commercial tenders, procurement of heavy machinery, hiring project crews.
 */
class ConstructionEngine : MonthlyTickSubscriber {

    override val subscriberId: String = "construction"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        var totalTerminIncome = 0L
        val logs = mutableListOf<String>()

        val hasLogistics = currentState.ownedBusinesses.any { it.catalogId == "mid_logistics" } ||
                currentState.holdingCompanies.any { h -> h.subsidiaries.any { it.catalogId == "mid_logistics" } }

        val updatedOwned = currentState.ownedBusinesses.map { biz ->
            if (biz.catalogId == "construction") {
                val result = LegacyConstructionEngine.processMonthlyTick(biz, hasLogistics)
                totalTerminIncome += result.netTerminIncome
                logs.addAll(result.notifications)
                result.updatedBusiness
            } else biz
        }

        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            var changed = false
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.catalogId == "construction") {
                    changed = true
                    val result = LegacyConstructionEngine.processMonthlyTick(sub, hasLogistics)
                    totalTerminIncome += result.netTerminIncome
                    logs.addAll(result.notifications)
                    result.updatedBusiness
                } else sub
            }
            if (changed) holding.copy(subsidiaries = newSubs) else holding
        }

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = totalTerminIncome,
            expenses = 0L,
            dividendToGlobal = 0L,
            logMessages = listOf(
                "Construction: Total Termin Revenue Injected: $totalTerminIncome"
            ) + logs,
            stateModifier = { state ->
                state.copy(
                    ownedBusinesses = updatedOwned,
                    holdingCompanies = updatedHoldings
                )
            }
        )
    }

    // --- User Actions / Reducers ---

    fun evaluateAndSubmitBid(
        business: OwnedBusiness,
        tender: ConstructionTenderOpportunity,
        playerBid: Long,
        usesInHouseLogistics: Boolean
    ): Pair<OwnedBusiness, LegacyConstructionEngine.BiddingResult>? {
        if (business.companyCash < tender.minBidBond) return null

        val biddingResult = LegacyConstructionEngine.evaluateBid(
            tender = tender,
            playerBid = playerBid,
            trustScore = business.constructionData.trustScore,
            usesInHouseLogistics = usesInHouseLogistics
        )

        val updatedBusiness = if (biddingResult.isWon) {
            val newProject = ConstructionProject(
                name = tender.title,
                totalContractValue = biddingResult.winningBid.toDouble(),
                durationMonths = tender.durationMonths,
                remainingMonths = tender.durationMonths,
                isFinished = false,
                clientName = tender.clientName,
                clientType = tender.clientType,
                projectScale = tender.projectScale,
                ownerEstimateBudget = tender.ownerEstimateBudget,
                agreedBidPrice = biddingResult.winningBid,
                currentPhaseIndex = 0,
                phases = tender.phases,
                requiredCrews = tender.requiredCrews,
                requiredMachinery = tender.requiredMachinery,
                initialSecurityDeposit = tender.minBidBond,
                usesInHouseLogistics = usesInHouseLogistics
            )
            val updatedTenders = business.activeTenders + newProject
            val newTrust = (business.constructionData.trustScore + biddingResult.trustScoreDelta).coerceIn(0, 100)
            business.copy(
                companyCash = business.companyCash - tender.minBidBond,
                activeTenders = updatedTenders,
                constructionData = business.constructionData.copy(trustScore = newTrust)
            )
        } else {
            val newTrust = (business.constructionData.trustScore + biddingResult.trustScoreDelta).coerceIn(0, 100)
            business.copy(
                constructionData = business.constructionData.copy(trustScore = newTrust)
            )
        }

        return Pair(updatedBusiness, biddingResult)
    }

    fun allocateResourcesToPhase(
        business: OwnedBusiness,
        projectId: String,
        phaseIndex: Int
    ): OwnedBusiness? {
        val project = business.activeTenders.find { it.id == projectId } ?: return null
        val phase = project.phases.getOrNull(phaseIndex) ?: return null

        val availableCrews = LegacyConstructionEngine.getAvailableCrews(business)
        val availableMachinery = LegacyConstructionEngine.getAvailableMachinery(business)

        if (availableCrews < project.requiredCrews || availableMachinery < project.requiredMachinery) {
            return null
        }

        val updatedPhases = project.phases.mapIndexed { idx, p ->
            if (idx == phaseIndex) p.copy(isAllocated = true)
            else p
        }
        val updatedProjects = business.activeTenders.map {
            if (it.id == projectId) it.copy(phases = updatedPhases)
            else it
        }

        return business.copy(activeTenders = updatedProjects)
    }
}
