package com.caesar.gametycoon.stock.takeover

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * Pure mathematical logic and negotiation engine for Strategic Stock Acquisitions.
 * Handles board sentiment, takeover premiums, distress probabilities, and counter-offers.
 */
object StockAcquisitionEngine {

    private const val DISTRESS_EVENT_PROBABILITY = 0.003 // 0.3% rare chance
    private const val MIN_TAKEOVER_STAKE = 51.0
    private const val MAX_TAKEOVER_STAKE = 100.0

    /**
     * Checks if a target company is under rare financial distress (0.3% chance).
     */
    fun checkDistressEvent(ticker: String, randomSeed: Long? = null): Boolean {
        val rand = if (randomSeed != null) Random(randomSeed) else Random(System.currentTimeMillis() xor ticker.hashCode().toLong())
        return rand.nextDouble() < DISTRESS_EVENT_PROBABILITY
    }

    /**
     * Computes the initial asking price demanded by the target company's Board of Directors.
     * Standard: +15% to +40% premium above market.
     * Distress: 40% to 55% discount below market (desperate for liquidity).
     */
    fun computeInitialBoardAskingPrice(
        marketPrice: Double,
        targetStakePercent: Double,
        isDistressed: Boolean
    ): Double {
        if (isDistressed) {
            // Desperate distress bailout: 45% - 60% of market price
            val discountFactor = 0.45 + (Random.nextDouble() * 0.15)
            return (marketPrice * discountFactor).coerceAtLeast(1.0)
        }

        // Higher stake requires higher control premium
        val stakeFactor = ((targetStakePercent - MIN_TAKEOVER_STAKE) / (MAX_TAKEOVER_STAKE - MIN_TAKEOVER_STAKE)).coerceIn(0.0, 1.0)
        val minPremium = 0.15 + (stakeFactor * 0.05) // 15% - 20%
        val maxPremium = 0.35 + (stakeFactor * 0.05) // 35% - 40%
        val randomPremium = minPremium + (Random.nextDouble() * (maxPremium - minPremium))
        
        return marketPrice * (1.0 + randomPremium)
    }

    /**
     * Calculates the acceptance probability (0.0 to 1.0) based on player's offer,
     * target market price, board asking price, and distress mode.
     */
    fun calculateAcceptanceProbability(
        offerPrice: Double,
        marketPrice: Double,
        boardAskingPrice: Double,
        isDistressed: Boolean
    ): Double {
        if (isDistressed) {
            val ratioToBoardAsk = offerPrice / max(1.0, boardAskingPrice)
            return when {
                ratioToBoardAsk >= 1.0 -> 0.98
                ratioToBoardAsk >= 0.85 -> 0.85 + (ratioToBoardAsk - 0.85) * 0.86
                ratioToBoardAsk >= 0.70 -> 0.60 + (ratioToBoardAsk - 0.70) * 1.66
                ratioToBoardAsk >= 0.50 -> 0.25 + (ratioToBoardAsk - 0.50) * 1.75
                else -> 0.05
            }.coerceIn(0.0, 1.0)
        }

        // Normal takeover evaluation
        val ratioToMarket = offerPrice / max(1.0, marketPrice)
        val ratioToBoardAsk = offerPrice / max(1.0, boardAskingPrice)

        if (ratioToMarket < 0.70) {
            return 0.01 // Insultingly low offer (<70% market price)
        }

        if (ratioToBoardAsk >= 1.0) {
            return 0.95
        }

        if (ratioToMarket >= 1.0) {
            // Between market price and board asking price
            val gap = boardAskingPrice - marketPrice
            val progress = if (gap > 0.0) (offerPrice - marketPrice) / gap else 1.0
            return (0.35 + (progress * 0.55)).coerceIn(0.05, 0.92)
        }

        // Below market price (70% - 99%)
        val discountProgress = (ratioToMarket - 0.70) / 0.30
        return (0.05 + (discountProgress * 0.25)).coerceIn(0.01, 0.30)
    }

    /**
     * Determines Board mood and provides human-readable feedback.
     */
    fun evaluateBoardMood(
        offerPrice: Double,
        marketPrice: Double,
        boardAskingPrice: Double,
        isDistressed: Boolean
    ): BoardMoodInfo {
        val probability = calculateAcceptanceProbability(offerPrice, marketPrice, boardAskingPrice, isDistressed)

        if (isDistressed) {
            return when {
                probability >= 0.75 -> BoardMoodInfo(
                    sentiment = BoardSentiment.DISTRESSED,
                    acceptanceProbability = probability,
                    moodLabel = "Krisis Likuiditas (Siap Terima Bailout)",
                    moodColorHex = 0xFF10B981, // Green
                    commentary = "Direksi berada di ambang kebangkrutan. Tawaran likuiditas Anda sangat diharapkan untuk menyelamatkan kelangsungan korporasi."
                )
                probability >= 0.40 -> BoardMoodInfo(
                    sentiment = BoardSentiment.DISTRESSED,
                    acceptanceProbability = probability,
                    moodLabel = "Tertekan Utang (Mempertimbangkan Penyelamatan)",
                    moodColorHex = 0xFFF59E0B, // Amber
                    commentary = "Dewan Komisaris mempertimbangkan proposal penyelamatan, namun meminta sedikit tambahan per lembar demi kreditor."
                )
                else -> BoardMoodInfo(
                    sentiment = BoardSentiment.RESISTANT,
                    acceptanceProbability = probability,
                    moodLabel = "Ragu-ragu (Tawaran Terlalu Mencekik)",
                    moodColorHex = 0xFFEF4444, // Red
                    commentary = "Meskipun terdesak utang, penawaran ini terlalu rendah untuk disetujui rapat umum pemegang saham darurat."
                )
            }
        }

        val ratioToMarket = offerPrice / max(1.0, marketPrice)

        return when {
            ratioToMarket < 0.70 -> BoardMoodInfo(
                sentiment = BoardSentiment.HOSTILE,
                acceptanceProbability = probability,
                moodLabel = "Marah / Menolak Tegas (<70% Harga Pasar)",
                moodColorHex = 0xFFEF4444,
                commentary = "Dewan menganggap penawaran Anda sebagai pelecehan korporat hostile. Tawaran di bawah 70% harga pasar akan ditolak mentah-mentah!"
            )
            probability >= 0.80 -> BoardMoodInfo(
                sentiment = BoardSentiment.ENTHUSIASTIC,
                acceptanceProbability = probability,
                moodLabel = "Sangat Tertarik / Premium Menggiurkan",
                moodColorHex = 0xFF10B981,
                commentary = "Pemegang saham pengendali sangat terkesan dengan premi akuisisi yang Anda tawarkan. Kesepakatan hampir pasti disahkan."
            )
            probability >= 0.40 -> BoardMoodInfo(
                sentiment = BoardSentiment.NEUTRAL,
                acceptanceProbability = probability,
                moodLabel = "Negosiasi Wajar / Sedang Dikaji",
                moodColorHex = 0xFF3B82F6, // Blue
                commentary = "Harga mendekati batas wajar valuasi pengendali. Dewan meminta sedikit penyesuaian agar merger dapat disetujui."
            )
            else -> BoardMoodInfo(
                sentiment = BoardSentiment.RESISTANT,
                acceptanceProbability = probability,
                moodLabel = "Menolak Keras / Minta Premi Kendali",
                moodColorHex = 0xFFF59E0B,
                commentary = "Tawaran Anda dinilai belum mencerminkan nilai strategis hak suara mayoritas. Dewan menuntut premi yang lebih memadai."
            )
        }
    }

    /**
     * Executes the Board decision algorithm for a given round.
     */
    fun processBiddingRound(
        offerPrice: Double,
        marketPrice: Double,
        currentBoardAskingPrice: Double,
        round: Int,
        maxRounds: Int,
        targetStakePercent: Double,
        additionalSharesNeeded: Long,
        isDistressed: Boolean
    ): BoardDecisionResult {
        // Hostile rejection check
        if (!isDistressed && (offerPrice / max(1.0, marketPrice)) < 0.70) {
            return BoardDecisionResult.Rejected(
                reason = "Dewan Komisaris menghentikan perundingan karena penawaran dinilai sebagai hostile takeover yang merendahkan nilai korporasi (<70% harga pasar).",
                walkedAway = true
            )
        }

        val probability = calculateAcceptanceProbability(offerPrice, marketPrice, currentBoardAskingPrice, isDistressed)
        val roll = Random.nextDouble()

        // 1. Direct Acceptance
        if (roll < probability || offerPrice >= currentBoardAskingPrice) {
            val totalCost = (additionalSharesNeeded * offerPrice).toLong()
            val msg = if (isDistressed) {
                "Dewan Direksi dan Kreditor resmi MENYETUJUI tawaran restrukturisasi/bailout Anda di harga $${String.format(java.util.Locale.US, "%.2f", offerPrice)} per lembar!"
            } else {
                "Dewan Komisaris resmi MENYETUJUI kesepakatan akuisisi mayoritas ${String.format(java.util.Locale.US, "%.1f", targetStakePercent)}% di harga $${String.format(java.util.Locale.US, "%.2f", offerPrice)} per lembar!"
            }
            return BoardDecisionResult.Accepted(
                agreedPricePerShare = offerPrice,
                totalCost = totalCost,
                message = msg
            )
        }

        // 2. Final Round Expiration
        if (round >= maxRounds) {
            return BoardDecisionResult.Rejected(
                reason = "Batas putaran negosiasi (Putaran $round/$maxRounds) telah berakhir tanpa titik temu. Negosiasi akuisisi ditutup.",
                walkedAway = true
            )
        }

        // 3. Counter Demand by Board
        // Board slightly lowers asking price towards player's offer as a concession
        val concessionFactor = if (isDistressed) 0.35 else 0.20
        val priceDiff = currentBoardAskingPrice - offerPrice
        val newAskingPrice = if (priceDiff > 0) {
            (currentBoardAskingPrice - (priceDiff * concessionFactor)).coerceAtLeast(offerPrice + 0.5)
        } else {
            currentBoardAskingPrice
        }

        val counterMsg = if (isDistressed) {
            "Kreditor menuntut harga minimal $${String.format(java.util.Locale.US, "%.2f", newAskingPrice)} per lembar untuk menutup kewajiban pokok obligasi."
        } else {
            "Dewan belum dapat menerima $${String.format(java.util.Locale.US, "%.2f", offerPrice)}, namun bersedia menurunkan tuntutan menjadi $${String.format(java.util.Locale.US, "%.2f", newAskingPrice)} per lembar."
        }

        return BoardDecisionResult.CounterDemand(
            counterPricePerShare = newAskingPrice,
            message = counterMsg
        )
    }
}
