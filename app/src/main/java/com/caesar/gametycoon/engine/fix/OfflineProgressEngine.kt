package com.caesar.gametycoon.engine.fix

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickRegistry
import com.example.data.MonthlyFinancialRecord
import com.example.data.PlayerState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.math.min

/**
 * Audit Fix: OfflineProgressEngine
 *
 * Resolves the asynchronous coroutine race condition during offline catch-up
 * by enforcing a strict, thread-safe, sequential progression loop (State_N -> State_N+1)
 * guarded by a Kotlin Coroutine [Mutex].
 */
object OfflineProgressEngine {

    private val executionMutex = Mutex()

    // 1 Real-life day (86,400,000 ms) = 1 in-game year (12 months)
    // 1 Offline in-game month = 7,200,000 ms (2 real hours)
    const val DEFAULT_OFFLINE_MONTH_MS: Long = 7_200_000L
    const val DEFAULT_MAX_OFFLINE_MONTHS: Int = 24

    /**
     * Sequential execution delegate for offline month catch-up ticks.
     * Replaces the concurrent repeat { launch { ... } } anti-pattern with an atomic sequential loop.
     */
    suspend fun processSequentialOfflineTicks(
        missedMonths: Int,
        tickStep: suspend () -> Unit
    ) = executionMutex.withLock {
        for (i in 1..missedMonths) {
            tickStep()
        }
    }

    data class OfflineCatchUpResult(
        val missedMonths: Int,
        val processedMonths: Int,
        val finalState: PlayerState,
        val totalRevenueAccumulated: Long,
        val totalExpensesAccumulated: Long,
        val netIncomeAccumulated: Long,
        val logMessages: List<String>
    )

    /**
     * Executes offline catch-up ticks sequentially.
     * Guarantees that each tick receives the mutated state of the preceding month,
     * completely eliminating concurrent state clobbering and dropped updates.
     */
    suspend fun executeSequentialCatchUp(
        currentState: PlayerState,
        lastSavedTimeMs: Long,
        nowMs: Long = System.currentTimeMillis(),
        tickRegistry: MonthlyTickRegistry,
        offlineMonthMs: Long = DEFAULT_OFFLINE_MONTH_MS,
        maxCatchUpMonths: Int = DEFAULT_MAX_OFFLINE_MONTHS,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        onMonthProcessed: ((monthIndex: Int, totalMonths: Int, intermediateState: PlayerState) -> Unit)? = null
    ): OfflineCatchUpResult = withContext(dispatcher) {
        executionMutex.withLock {
            if (lastSavedTimeMs <= 0L || nowMs <= lastSavedTimeMs) {
                return@withLock OfflineCatchUpResult(
                    missedMonths = 0,
                    processedMonths = 0,
                    finalState = currentState.copy(lastSavedTimeMs = nowMs),
                    totalRevenueAccumulated = 0L,
                    totalExpensesAccumulated = 0L,
                    netIncomeAccumulated = 0L,
                    logMessages = listOf("Offline progress check: No elapsed offline time detected.")
                )
            }

            val elapsedMs = nowMs - lastSavedTimeMs
            if (elapsedMs < offlineMonthMs) {
                return@withLock OfflineCatchUpResult(
                    missedMonths = 0,
                    processedMonths = 0,
                    finalState = currentState.copy(lastSavedTimeMs = nowMs),
                    totalRevenueAccumulated = 0L,
                    totalExpensesAccumulated = 0L,
                    netIncomeAccumulated = 0L,
                    logMessages = listOf("Offline progress check: Elapsed time ($elapsedMs ms) below single-month threshold ($offlineMonthMs ms).")
                )
            }

            val rawMissedMonths = (elapsedMs / offlineMonthMs).toInt()
            val missedMonths = min(rawMissedMonths, maxCatchUpMonths)
            val logs = mutableListOf<String>()
            logs.add("OfflineProgress: Catching up $missedMonths months sequentially (capped from $rawMissedMonths raw months).")

            var rollingState = currentState
            var accumulatedRevenue = 0L
            var accumulatedExpenses = 0L

            for (monthStep in 1..missedMonths) {
                var nextMonth = rollingState.inGameMonth + 1
                var nextYear = rollingState.inGameYear
                if (nextMonth > 12) {
                    nextMonth = 1
                    nextYear += 1
                }

                val tickEvent = MonthTickEvent(
                    currentMonth = nextMonth,
                    currentYear = nextYear,
                    totalMonthsElapsed = (nextYear - 2026) * 12 + nextMonth,
                    isOfflineCatchup = true,
                    deltaMonths = 1
                )

                // 1. Dispatch tick to all registered subsystem engines with current state
                val (stateAfterSubsystems, financials) = tickRegistry.dispatchTick(tickEvent, rollingState)

                accumulatedRevenue += financials.totalRevenue
                accumulatedExpenses += financials.totalExpenses

                // 2. Safely apply corporate cash and ledger history for this month
                val newFinancialRecord = MonthlyFinancialRecord(
                    monthTick = nextMonth + (nextYear - 2026) * 12,
                    totalRevenue = financials.totalRevenue,
                    totalExpense = financials.totalExpenses,
                    netIncome = financials.netIncome
                )

                val updatedFinancialHistory = (stateAfterSubsystems.financialHistory + newFinancialRecord).takeLast(60)

                // Advance calendar and record last month stats
                rollingState = stateAfterSubsystems.copy(
                    inGameMonth = nextMonth,
                    inGameYear = nextYear,
                    lastMonthIncome = financials.totalRevenue,
                    lastMonthExpenses = financials.totalExpenses,
                    lastMonthNetProfit = financials.netIncome,
                    financialHistory = updatedFinancialHistory
                )

                onMonthProcessed?.invoke(monthStep, missedMonths, rollingState)
            }

            val finalState = rollingState.copy(lastSavedTimeMs = nowMs)
            logs.add("OfflineProgress: Catch-up complete. Processed $missedMonths months. Net delta: ${accumulatedRevenue - accumulatedExpenses}.")

            return@withLock OfflineCatchUpResult(
                missedMonths = rawMissedMonths,
                processedMonths = missedMonths,
                finalState = finalState,
                totalRevenueAccumulated = accumulatedRevenue,
                totalExpensesAccumulated = accumulatedExpenses,
                netIncomeAccumulated = accumulatedRevenue - accumulatedExpenses,
                logMessages = logs
            )
        }
    }
}
