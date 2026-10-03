package com.example.publisher.engine

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.PlayerState
import com.example.publisher.data.GamePublisherRepository

/**
 * Connects the Indie Game Publisher & Incubator module to the core simulation loop.
 * Advances the publisher monthly cycle when the global game month ticks.
 */
class GamePublisherTickSubscriber(
    private val repository: GamePublisherRepository = GamePublisherRepository.getInstance()
) : MonthlyTickSubscriber {

    override val subscriberId: String = "indie_game_publisher"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        val ownsPublisher = currentState.ownedBusinesses.any { it.catalogId == "indie_game_publisher" } ||
                currentState.holdingCompanies.any { holding -> holding.subsidiaries.any { it.catalogId == "indie_game_publisher" } }

        if (!ownsPublisher) {
            return SubsystemTickResult(
                subsystemId = subscriberId,
                revenue = 0L,
                expenses = 0L,
                dividendToGlobal = 0L,
                logMessages = emptyList(),
                stateModifier = { it }
            )
        }

        // Advance month for all active projects, sales decays, and publisher treasury
        val tickReport = repository.advanceMonth()

        val monthlyGross = tickReport.totalSalesRevenue
        val monthlyMaint = tickReport.maintenanceExpenses

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = monthlyGross,
            expenses = monthlyMaint,
            dividendToGlobal = 0L,
            logMessages = tickReport.logSummaries,
            stateModifier = { state -> state }
        )
    }
}
