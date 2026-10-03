package com.example.ma.engine

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.PlayerState
import com.example.ma.data.MaRepository

/**
 * Monthly game-loop tick subscriber connecting the M&A Offer Engine to the main tycoon clock.
 */
class MaTickSubscriber(
    private val repository: MaRepository = MaRepository.getInstance()
) : MonthlyTickSubscriber {

    override val subscriberId: String = "corporate_ma_engine"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        val (updatedState, logMessages) = repository.processMonthlyTick(
            playerState = currentState,
            currentMonth = event.currentMonth,
            currentYear = event.currentYear
        )

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = 0L,
            expenses = 0L,
            dividendToGlobal = 0L,
            logMessages = logMessages,
            stateModifier = { updatedState }
        )
    }
}
