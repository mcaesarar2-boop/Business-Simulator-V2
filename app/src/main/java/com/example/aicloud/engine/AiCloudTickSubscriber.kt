package com.example.aicloud.engine

import com.example.aicloud.data.AiCloudRepository
import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.PlayerState

/**
 * Connects the AI Cloud Provider & Data Center Infrastructure module to the core tycoon game loop.
 */
class AiCloudTickSubscriber(
    private val repository: AiCloudRepository = AiCloudRepository.getInstance()
) : MonthlyTickSubscriber {

    override val subscriberId: String = "ai_cloud_provider"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        // Check if player owns AI Cloud Provider
        val ownsAiCloud = currentState.ownedBusinesses.any { it.catalogId == "ai_cloud_provider" } ||
                currentState.holdingCompanies.any { holding -> holding.subsidiaries.any { it.catalogId == "ai_cloud_provider" } }

        if (!ownsAiCloud) {
            return SubsystemTickResult(
                subsystemId = subscriberId,
                revenue = 0L,
                expenses = 0L,
                dividendToGlobal = 0L,
                logMessages = emptyList(),
                stateModifier = { it }
            )
        }

        val monthYear = "${event.currentMonth}/${event.currentYear}"
        val simulationOutput = AiCloudEngine.processMonthlyTick(
            monthYear = monthYear,
            dataCenters = repository.dataCenters.value,
            activeContracts = repository.activeContracts.value,
            marketContracts = repository.marketContracts.value,
            unlockedResearchIds = repository.unlockedResearchIds.value
        )

        repository.updateAllState(
            updatedDcs = simulationOutput.updatedDataCenters,
            updatedActiveContracts = simulationOutput.updatedActiveContracts,
            updatedMarketContracts = simulationOutput.updatedMarketContracts,
            newReport = simulationOutput.report,
            netProfit = simulationOutput.netProfit
        )

        simulationOutput.incidents.forEach {
            repository.addIncident(it.title, it.description, it.severity, it.financialImpactUsd)
        }

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = simulationOutput.grossRevenue,
            expenses = simulationOutput.totalExpenses,
            dividendToGlobal = 0L,
            logMessages = simulationOutput.report.logSummaries,
            stateModifier = { state ->
                val policyRepo = com.example.corporate.repository.CorporatePolicyRepository.getInstance()
                val distributionResults = mutableListOf<com.example.corporate.model.CashDistributionResult>()

                val newBusinesses = state.ownedBusinesses.map { biz ->
                    if (biz.catalogId == "ai_cloud_provider") {
                        val policy = policyRepo.getStandalonePolicy(biz.instanceId)
                        val (updatedBiz, result) = com.example.corporate.engine.CashFlowDistributionEngine.processStandaloneBusinessCashFlow(
                            business = biz.copy(customRevenue = simulationOutput.grossRevenue),
                            netProfit = simulationOutput.netProfit,
                            policy = policy
                        )
                        distributionResults.add(result)
                        updatedBiz
                    } else biz
                }
                val newHoldings = state.holdingCompanies.map { holding ->
                    var changed = false
                    var currentTreasuryCash = holding.holdingCash
                    val holdingPolicy = policyRepo.getHoldingPolicy(holding.instanceId)
                    val newSubs = holding.subsidiaries.map { sub ->
                        if (sub.catalogId == "ai_cloud_provider") {
                            changed = true
                            val (updatedSub, treasuryDelta, result) = com.example.corporate.engine.CashFlowDistributionEngine.processMergedSubsidiaryCashFlow(
                                subsidiary = sub.copy(customRevenue = simulationOutput.grossRevenue),
                                parentHolding = holding.copy(holdingCash = currentTreasuryCash),
                                netProfit = simulationOutput.netProfit,
                                policy = holdingPolicy
                            )
                            currentTreasuryCash = (currentTreasuryCash + treasuryDelta).coerceAtLeast(0.0)
                            distributionResults.add(result)
                            updatedSub
                        } else sub
                    }
                    if (changed) holding.copy(subsidiaries = newSubs, holdingCash = currentTreasuryCash) else holding
                }

                if (distributionResults.isNotEmpty()) {
                    policyRepo.recordDistributionResults(distributionResults)
                }

                state.copy(
                    ownedBusinesses = newBusinesses,
                    holdingCompanies = newHoldings
                )
            }
        )
    }
}
