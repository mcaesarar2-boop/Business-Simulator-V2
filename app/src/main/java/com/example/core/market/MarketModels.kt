package com.example.core.market

/**
 * High-level market macro regimes governing asset price action.
 */
enum class MarketTrend {
    BULL_MARKET,
    BEAR_MARKET,
    STEADY_GROWTH,
    STEADY_BLEED,
    THE_LOST_DECADE,
    LONG_TERM_CYCLICAL,
    WHIPSAW_TRAP
}

/**
 * State tracking an active macro trend for an individual stock ticker.
 */
data class StockTrendState(
    var currentTrend: MarketTrend,
    var durationLeftMs: Long,
    var cyclePhase: Int = 1
)
