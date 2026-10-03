package com.example.core.engine

import com.example.data.PlayerState

/**
 * Result returned by an industry subsystem after processing a tick.
 * Eliminates direct mutation of global state within the engine.
 */
data class SubsystemTickResult(
    val subsystemId: String,
    val revenue: Long = 0L,
    val expenses: Long = 0L,
    val dividendToGlobal: Long = 0L,
    val logMessages: List<String> = emptyList(),
    val stateModifier: (PlayerState) -> PlayerState = { it }
)

/**
 * Interface implemented by all decoupled industry engines.
 */
interface MonthlyTickSubscriber {
    /** Unique identifier for the subsystem (e.g. "content_creator", "streaming_service") */
    val subscriberId: String

    /**
     * Executes business logic for the tick.
     * Pure function or isolated state transformation returning financial deltas
     * and a state modifier lambda.
     */
    suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult
}
