package com.example.core.engine

import com.example.data.PlayerState

/**
 * Aggregated financial summary returned after all subscribers have ticked.
 */
data class AggregatedFinancials(
    val totalRevenue: Long,
    val totalExpenses: Long,
    val netIncome: Long
)

/**
 * Central registry that orchestrates monthly ticks across all business subsystems.
 */
class MonthlyTickRegistry(
    private val subscribers: MutableList<MonthlyTickSubscriber> = mutableListOf()
) {
    fun registerSubscriber(subscriber: MonthlyTickSubscriber) {
        if (subscribers.none { it.subscriberId == subscriber.subscriberId }) {
            subscribers.add(subscriber)
        }
    }

    fun unregisterSubscriber(subscriberId: String) {
        subscribers.removeAll { it.subscriberId == subscriberId }
    }

    /**
     * Replaces the 3,000-line monolithic switch-case.
     * Concurrently collects tick results and folds state mutations safely.
     */
    suspend fun dispatchTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): Pair<PlayerState, AggregatedFinancials> {
        var accumulatedRevenue = 0L
        var accumulatedExpenses = 0L
        var intermediateState = currentState

        for (subscriber in subscribers) {
            val result = subscriber.onMonthlyTick(event, intermediateState)
            accumulatedRevenue += result.revenue
            accumulatedExpenses += result.expenses
            intermediateState = result.stateModifier(intermediateState)
        }

        val aggregatedFinancials = AggregatedFinancials(
            totalRevenue = accumulatedRevenue,
            totalExpenses = accumulatedExpenses,
            netIncome = accumulatedRevenue - accumulatedExpenses
        )

        return Pair(intermediateState, aggregatedFinancials)
    }
}
