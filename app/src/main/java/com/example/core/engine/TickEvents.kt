package com.example.core.engine

/**
 * Event broadcast when an in-game month ticks over.
 *
 * @property currentMonth Month of the in-game year (1..12).
 * @property currentYear In-game year (e.g., 2026).
 * @property totalMonthsElapsed Monotonically increasing month counter since game start.
 * @property isOfflineCatchup True if this tick is part of catching up missed offline progress.
 * @property deltaMonths Number of months this tick represents (usually 1).
 */
data class MonthTickEvent(
    val currentMonth: Int = 1,
    val currentYear: Int = 2026,
    val totalMonthsElapsed: Int = 0,
    val isOfflineCatchup: Boolean = false,
    val deltaMonths: Int = 1
)

/**
 * Sub-second frame/tick update event (dispatched every ~100ms)
 * for realtime sub-progress (e.g. Content Creator progress bars, upgrade timers, logistics dt).
 */
data class SubTickEvent(
    val dtSeconds: Float = 0.1f,
    val currentTimeMs: Long = System.currentTimeMillis()
)
