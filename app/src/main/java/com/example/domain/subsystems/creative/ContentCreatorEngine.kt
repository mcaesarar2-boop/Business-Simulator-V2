package com.example.domain.subsystems.creative

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.ActiveCreatorContract
import com.example.data.ContentStatus
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import kotlin.math.pow

/**
 * Isolated domain engine for the Content Creator subsystem.
 * Handles:
 * 1. Monthly royalty collection and licensing duration decrement.
 * 2. Monthly brand deal sponsorship contract payouts, countdowns, and completions.
 * 3. User actions (level up, hire employees, unlock studio office, produce creative works).
 */
class ContentCreatorEngine : MonthlyTickSubscriber {

    override val subscriberId: String = "content_creator"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        var totalEarnings = 0L
        val policyRepo = com.example.corporate.repository.CorporatePolicyRepository.getInstance()
        val distributionResults = mutableListOf<com.example.corporate.model.CashDistributionResult>()
        val updatedOwnedMap = mutableMapOf<String, OwnedBusiness>()

        currentState.ownedBusinesses.forEach { biz ->
            if (biz.catalogId == "content_creator") {
                val (updatedBiz, earnings) = processBusinessMonthly(biz)
                totalEarnings += earnings
                val policy = policyRepo.getStandalonePolicy(biz.instanceId)
                val (finalBiz, result) = com.example.corporate.engine.CashFlowDistributionEngine.processStandaloneBusinessCashFlow(
                    business = updatedBiz,
                    netProfit = earnings,
                    policy = policy
                )
                distributionResults.add(result)
                updatedOwnedMap[biz.instanceId] = finalBiz
            }
        }

        val updatedSubsMap = mutableMapOf<String, OwnedBusiness>()
        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            var changed = false
            var currentTreasuryCash = holding.holdingCash
            val holdingPolicy = policyRepo.getHoldingPolicy(holding.instanceId)
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.catalogId == "content_creator") {
                    changed = true
                    val (updatedSub, earnings) = processBusinessMonthly(sub)
                    totalEarnings += earnings
                    val (finalSub, treasuryDelta, result) = com.example.corporate.engine.CashFlowDistributionEngine.processMergedSubsidiaryCashFlow(
                        subsidiary = updatedSub,
                        parentHolding = holding.copy(holdingCash = currentTreasuryCash),
                        netProfit = earnings,
                        policy = holdingPolicy
                    )
                    currentTreasuryCash = (currentTreasuryCash + treasuryDelta).coerceAtLeast(0.0)
                    distributionResults.add(result)
                    updatedSubsMap[sub.instanceId] = finalSub
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
            revenue = totalEarnings,
            expenses = 0L,
            dividendToGlobal = 0L,
            logMessages = if (totalEarnings > 0) listOf("Content Creator earned $totalEarnings from brand deals and royalties.") else emptyList(),
            stateModifier = { state ->
                val newOwned = state.ownedBusinesses.map { biz ->
                    updatedOwnedMap[biz.instanceId] ?: biz
                }
                state.copy(
                    ownedBusinesses = newOwned,
                    holdingCompanies = updatedHoldings
                )
            }
        )
    }

    private fun processBusinessMonthly(business: OwnedBusiness): Pair<OwnedBusiness, Long> {
        var monthlyEarnings = 0L

        // 1. Process Content Portfolio Licensing Contracts
        val updatedPortfolio = business.contentPortfolio.map { work ->
            if (work.status == ContentStatus.LICENSED) {
                monthlyEarnings += work.monthlyRoyalty
                val remaining = (work.remainingContractMonths ?: work.contractDurationMonths ?: 24) - 1
                if (remaining <= 0) {
                    work.copy(
                        status = ContentStatus.AVAILABLE,
                        monthlyRoyalty = 0L,
                        contractDurationMonths = null,
                        remainingContractMonths = null,
                        acquiredByPH = null
                    )
                } else {
                    work.copy(remainingContractMonths = remaining)
                }
            } else work
        }

        // 2. Process Active Brand Deal Sponsorship Contracts
        val updatedContracts = mutableListOf<ActiveCreatorContract>()
        for (contract in business.contentCreatorContracts) {
            monthlyEarnings += contract.monthlyPayout
            val newRemaining = contract.remainingMonths - 1
            val newPaid = contract.totalPaidSoFar + contract.monthlyPayout
            if (newRemaining > 0) {
                updatedContracts.add(
                    contract.copy(
                        remainingMonths = newRemaining,
                        totalPaidSoFar = newPaid
                    )
                )
            }
        }

        val updatedBusiness = business.copy(
            contentPortfolio = updatedPortfolio,
            contentCreatorContracts = updatedContracts
        )
        return Pair(updatedBusiness, monthlyEarnings)
    }

    // --- User Intent Handlers / Reducers ---

    fun levelUp(business: OwnedBusiness): OwnedBusiness? {
        if (business.level >= 100) return null
        if (business.level == 40 && !business.contentCreatorOfficeUnlocked) return null

        val cost = (500.0 * 1.18.pow((business.level - 1).toDouble())).toLong()
        if (business.contentCreatorCash < cost) return null

        val newLevel = business.level + 1
        val newSubs = business.contentCreatorSubscribers + (100.0 * 1.16.pow(newLevel.toDouble())).toLong()
        return business.copy(
            level = newLevel,
            contentCreatorSubscribers = newSubs,
            contentCreatorCash = business.contentCreatorCash - cost
        )
    }

    fun hireEmployee(business: OwnedBusiness): OwnedBusiness? {
        val maxEmp = when {
            business.level >= 81 -> 100
            business.level >= 61 -> 50
            business.level >= 41 -> 20
            business.level >= 21 -> 5
            else -> 0
        }
        if (business.contentCreatorEmployees >= maxEmp) return null

        val cost = (1500.0 * 1.2.pow(business.contentCreatorEmployees.toDouble())).toLong()
        if (business.contentCreatorCash < cost) return null

        return business.copy(
            contentCreatorEmployees = business.contentCreatorEmployees + 1,
            contentCreatorCash = business.contentCreatorCash - cost
        )
    }

    fun unlockOffice(business: OwnedBusiness): OwnedBusiness? {
        val cost = 5_000_000L
        if (business.level == 40 && !business.contentCreatorOfficeUnlocked && business.contentCreatorCash >= cost) {
            return business.copy(
                contentCreatorOfficeUnlocked = true,
                contentCreatorCash = business.contentCreatorCash - cost
            )
        }
        return null
    }
}
