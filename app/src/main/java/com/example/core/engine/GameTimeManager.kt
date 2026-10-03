package com.example.core.engine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * GameTimeManager is responsible for:
 * 1. Managing the core game loop ticker (100ms interval).
 * 2. Emitting high-frequency [SubTickEvent] for realtime animations/progressbars.
 * 3. Accumulating in-game month progress and emitting [MonthTickEvent] via [SharedFlow].
 * 4. Calculating and executing offline time catch-up calculations cleanly.
 */
class GameTimeManager(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    initialMonthDurationSeconds: Float = 120f
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private var tickerJob: Job? = null

    // 0.0f .. 1.0f progress within the current in-game month
    private val _monthProgress = MutableStateFlow(0f)
    val monthProgress: StateFlow<Float> = _monthProgress.asStateFlow()

    // Configurable month duration (e.g. 10s to 7200s, default 120s)
    private val _monthDurationSeconds = MutableStateFlow(initialMonthDurationSeconds.coerceIn(10f, 7200f))
    val monthDurationSeconds: StateFlow<Float> = _monthDurationSeconds.asStateFlow()

    // Game calendar tracking
    private val _currentMonth = MutableStateFlow(1)
    val currentMonth: StateFlow<Int> = _currentMonth.asStateFlow()

    private val _currentYear = MutableStateFlow(2026)
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()

    private val _totalMonthsElapsed = MutableStateFlow(0)
    val totalMonthsElapsed: StateFlow<Int> = _totalMonthsElapsed.asStateFlow()

    // Event broadcast channels
    private val _monthTickEvents = MutableSharedFlow<MonthTickEvent>(extraBufferCapacity = 64)
    val monthTickEvents: SharedFlow<MonthTickEvent> = _monthTickEvents.asSharedFlow()

    private val _subTickEvents = MutableSharedFlow<SubTickEvent>(extraBufferCapacity = 16)
    val subTickEvents: SharedFlow<SubTickEvent> = _subTickEvents.asSharedFlow()

    /**
     * Set the in-game calendar state directly (e.g., when loading saved state).
     */
    fun syncCalendar(month: Int, year: Int, totalMonths: Int = 0) {
        _currentMonth.value = month.coerceIn(1, 12)
        _currentYear.value = year
        _totalMonthsElapsed.value = totalMonths
    }

    /**
     * Configure how many real-time seconds represent 1 in-game month.
     */
    fun updateMonthDuration(seconds: Float) {
        _monthDurationSeconds.value = seconds.coerceIn(10f, 7200f)
    }

    /**
     * Calculates missed offline months and emits catch-up [MonthTickEvent]s.
     *
     * In-game time ratio:
     * 1 real-life day (86,400,000 ms) = 1 in-game year (12 months).
     * 1 offline in-game month = 7,200,000 ms (2 real hours).
     * Maximum catch-up capped at 24 months (2 in-game years).
     *
     * @return The number of missed months caught up.
     */
    suspend fun processOfflineCatchUp(lastSavedTimeMs: Long, nowMs: Long = System.currentTimeMillis()): Int {
        if (lastSavedTimeMs <= 0) return 0

        val elapsedMs = nowMs - lastSavedTimeMs
        val offlineMonthMs = 7_200_000L // 2 real hours per in-game month
        if (elapsedMs < offlineMonthMs) return 0

        val missedMonths = min((elapsedMs / offlineMonthMs).toInt(), 24)
        for (i in 1..missedMonths) {
            advanceCalendarMonth()
            val event = MonthTickEvent(
                currentMonth = _currentMonth.value,
                currentYear = _currentYear.value,
                totalMonthsElapsed = _totalMonthsElapsed.value,
                isOfflineCatchup = true,
                deltaMonths = 1
            )
            _monthTickEvents.emit(event)
        }
        _monthProgress.value = 0f
        return missedMonths
    }

    /**
     * Starts or resumes the primary game loop ticker.
     */
    fun startGameLoop() {
        if (tickerJob?.isActive == true) return

        tickerJob = scope.launch {
            val tickIntervalMs = 100L // 100ms per tick
            while (isActive) {
                delay(tickIntervalMs)
                updateProgress(tickIntervalMs)
            }
        }
    }

    /**
     * Pauses the game loop ticker.
     */
    fun stopGameLoop() {
        tickerJob?.cancel()
        tickerJob = null
    }

    /**
     * Dispatches sub-tick events and advances month progress towards completion.
     */
    private suspend fun updateProgress(tickIntervalMs: Long) {
        val dtSeconds = tickIntervalMs / 1000f
        _subTickEvents.tryEmit(SubTickEvent(dtSeconds = dtSeconds, currentTimeMs = System.currentTimeMillis()))

        val durationMs = _monthDurationSeconds.value * 1000f
        val step = tickIntervalMs.toFloat() / durationMs
        val newProgress = _monthProgress.value + step

        if (newProgress >= 1f) {
            _monthProgress.value = 0f
            advanceCalendarMonth()
            val tickEvent = MonthTickEvent(
                currentMonth = _currentMonth.value,
                currentYear = _currentYear.value,
                totalMonthsElapsed = _totalMonthsElapsed.value,
                isOfflineCatchup = false,
                deltaMonths = 1
            )
            _monthTickEvents.emit(tickEvent)
        } else {
            _monthProgress.value = newProgress
        }
    }

    private fun advanceCalendarMonth() {
        _totalMonthsElapsed.value += 1
        var nextMonth = _currentMonth.value + 1
        var nextYear = _currentYear.value
        if (nextMonth > 12) {
            nextMonth = 1
            nextYear += 1
        }
        _currentMonth.value = nextMonth
        _currentYear.value = nextYear
    }

    fun reset() {
        stopGameLoop()
        _monthProgress.value = 0f
        _currentMonth.value = 1
        _currentYear.value = 2026
        _totalMonthsElapsed.value = 0
    }

    fun dispose() {
        scope.cancel()
    }
}
