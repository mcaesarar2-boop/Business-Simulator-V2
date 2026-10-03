package com.caesar.gametycoon.stock.takeover

import com.example.data.MarketNews
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import com.example.data.StockItem

/**
 * Domain & Presentation models for Strategic Stock Takeover (>50% Stake Bidding Engine).
 */

enum class BoardSentiment {
    HOSTILE,       // Insultingly low offer (<70% market price)
    RESISTANT,     // Demanding higher premium (10% - 39% acceptance chance)
    NEUTRAL,       // Standard negotiation (40% - 79% acceptance chance)
    ENTHUSIASTIC,  // Strong premium offered (>= 80% acceptance chance)
    DISTRESSED     // 0.3% rare distress event; Board desperate for bailout capital
}

data class BoardMoodInfo(
    val sentiment: BoardSentiment,
    val acceptanceProbability: Double, // 0.0 to 1.0
    val moodLabel: String,
    val moodColorHex: Long,
    val commentary: String
)

sealed class BoardDecisionResult {
    data class Accepted(
        val agreedPricePerShare: Double,
        val totalCost: Long,
        val message: String
    ) : BoardDecisionResult()

    data class CounterDemand(
        val counterPricePerShare: Double,
        val message: String
    ) : BoardDecisionResult()

    data class Rejected(
        val reason: String,
        val walkedAway: Boolean
    ) : BoardDecisionResult()
}

enum class TakeoverEligibility {
    ELIGIBLE,
    REQUIRES_MEGA_HOLDING,
    INSUFFICIENT_FUNDS,
    ALREADY_ACQUIRED
}

data class NegotiationLogEntry(
    val round: Int,
    val playerOffer: Double,
    val boardResponse: String,
    val resultType: String // "PLAYER_OFFER", "BOARD_COUNTER", "ACCEPTED", "REJECTED"
)

data class TakeoverBiddingState(
    val targetStock: StockItem,
    val currentMarketPrice: Double,
    val targetStakePercent: Double = 51.0,
    val currentOwnedShares: Long = 0L,
    val initialBoardAskingPrice: Double,
    val currentBoardAskingPrice: Double,
    val playerOfferPricePerShare: Double,
    val isDistressed: Boolean = false,
    val negotiationRound: Int = 1,
    val maxRounds: Int = 5,
    val isDealClosed: Boolean = false,
    val isWalkedAway: Boolean = false,
    val statusMessage: String = "",
    val boardMood: BoardMoodInfo,
    val negotiationHistory: List<NegotiationLogEntry> = emptyList()
) {
    val totalSharesOutstanding: Long get() = targetStock.sharesOutstanding.coerceAtLeast(1L)
    
    val targetTotalShares: Long
        get() = ((totalSharesOutstanding.toDouble() * (targetStakePercent / 100.0)).toLong())
            .coerceIn(1L, totalSharesOutstanding)

    val additionalSharesNeeded: Long
        get() = (targetTotalShares - currentOwnedShares).coerceAtLeast(1L)

    val estimatedTotalCost: Long
        get() = (additionalSharesNeeded * playerOfferPricePerShare).toLong()

    val boardDemandedTotalCost: Long
        get() = (additionalSharesNeeded * currentBoardAskingPrice).toLong()
}

data class StrategicTakeoverResult(
    val isSuccess: Boolean,
    val updatedPlayerState: PlayerState,
    val acquiredBusiness: OwnedBusiness? = null,
    val message: String,
    val marketNews: MarketNews? = null
)
