package com.example.businessunit.contentcreator.engine

import com.example.businessunit.contentcreator.model.ActiveCreatorContract
import com.example.businessunit.contentcreator.model.ContentStatus
import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import kotlin.math.pow

/**
 * Domain engine responsible for Content Creator business simulation and monthly tick subscriber.
 */
class ContentCreatorEngine : MonthlyTickSubscriber {

    override val subscriberId: String = "content_creator"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        var totalEarnings = 0L
        val updatedOwnedMap = mutableMapOf<String, OwnedBusiness>()

        currentState.ownedBusinesses.forEach { biz ->
            if (biz.catalogId == "content_creator") {
                val (updatedBiz, earnings) = processBusinessMonthly(biz)
                totalEarnings += earnings
                updatedOwnedMap[biz.instanceId] = updatedBiz
            }
        }

        val updatedSubsMap = mutableMapOf<String, OwnedBusiness>()
        currentState.holdingCompanies.forEach { holding ->
            holding.subsidiaries.forEach { sub ->
                if (sub.catalogId == "content_creator") {
                    val (updatedSub, earnings) = processBusinessMonthly(sub)
                    totalEarnings += earnings
                    updatedSubsMap[sub.instanceId] = updatedSub
                }
            }
        }

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = totalEarnings,
            expenses = 0L,
            dividendToGlobal = 0L,
            logMessages = if (totalEarnings > 0) listOf("Content Creator earned $$totalEarnings from brand deals and royalties.") else emptyList(),
            stateModifier = { state ->
                val newOwned = state.ownedBusinesses.map { biz ->
                    updatedOwnedMap[biz.instanceId] ?: biz
                }
                val newHoldings = state.holdingCompanies.map { holding ->
                    val newSubs = holding.subsidiaries.map { sub ->
                        updatedSubsMap[sub.instanceId] ?: sub
                    }
                    holding.copy(subsidiaries = newSubs)
                }
                state.copy(
                    ownedBusinesses = newOwned,
                    holdingCompanies = newHoldings
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
            contentCreatorCash = business.contentCreatorCash + monthlyEarnings,
            contentPortfolio = updatedPortfolio,
            contentCreatorContracts = updatedContracts
        )
        return Pair(updatedBusiness, monthlyEarnings)
    }

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
