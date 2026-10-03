package com.caesar.gametycoon.stock.takeover

import com.example.data.MarketNews
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import com.example.data.StockItem
import java.util.UUID

/**
 * Repository and Orchestrator for Strategic Takeover validation, bidding progression,
 * and subsidiary merger into Mega Holding governance.
 */
object StockAcquisitionRepository {

    const val MAX_RETAIL_STAKE_PERCENT = 49.9
    const val DEFAULT_TAKEOVER_STAKE = 51.0

    /**
     * Anti-Cheat / Cumulative Cap Enforcement for retail trading (<50%).
     * Returns true if purchase keeps cumulative stake <= 49.9% OR if player has a Mega Holding.
     */
    fun canBuyRetailShares(
        playerState: PlayerState,
        stock: StockItem,
        additionalShares: Long
    ): Boolean {
        if (playerState.megaHolding.isActive) return true

        val currentOwnedShares = playerState.ownedStocks.find { it.ticker == stock.ticker }?.shares ?: 0L
        val totalSharesOutstanding = stock.sharesOutstanding.coerceAtLeast(1L)
        val prospectiveTotalShares = currentOwnedShares + additionalShares
        val prospectiveStakePercent = (prospectiveTotalShares.toDouble() / totalSharesOutstanding.toDouble()) * 100.0

        return prospectiveStakePercent <= MAX_RETAIL_STAKE_PERCENT
    }

    /**
     * Checks if player is legally eligible to initiate a strategic takeover (>50% stake).
     */
    fun checkTakeoverEligibility(
        playerState: PlayerState,
        stock: StockItem
    ): TakeoverEligibility {
        // 1. Hard lock: Requires active Mega Holding Entity
        if (!playerState.megaHolding.isActive) {
            return TakeoverEligibility.REQUIRES_MEGA_HOLDING
        }

        // 2. Already acquired as a business subsidiary
        val isAlreadyAcquired = playerState.ownedBusinesses.any { it.acquiredStockTicker == stock.ticker } ||
            playerState.holdingCompanies.any { h -> h.subsidiaries.any { it.acquiredStockTicker == stock.ticker } }
        if (isAlreadyAcquired) {
            return TakeoverEligibility.ALREADY_ACQUIRED
        }

        return TakeoverEligibility.ELIGIBLE
    }

    /**
     * Factory for initializing a new Bidding Negotiation session.
     */
    fun createInitialBiddingState(
        stock: StockItem,
        currentOwnedShares: Long,
        initialStakePercent: Double = DEFAULT_TAKEOVER_STAKE
    ): TakeoverBiddingState {
        val isDistressed = StockAcquisitionEngine.checkDistressEvent(stock.ticker)
        val initialBoardAsk = StockAcquisitionEngine.computeInitialBoardAskingPrice(
            marketPrice = stock.currentPrice,
            targetStakePercent = initialStakePercent,
            isDistressed = isDistressed
        )
        val initialPlayerOffer = if (isDistressed) {
            (stock.currentPrice * 0.50).coerceAtLeast(1.0)
        } else {
            stock.currentPrice
        }

        val initialMood = StockAcquisitionEngine.evaluateBoardMood(
            offerPrice = initialPlayerOffer,
            marketPrice = stock.currentPrice,
            boardAskingPrice = initialBoardAsk,
            isDistressed = isDistressed
        )

        val greetingMessage = if (isDistressed) {
            "⚠️ PERINGATAN DISTRESS: Korporasi ${stock.name} mengalami krisis kas akut. Dewan bersedia menerima penawaran restrukturisasi di bawah harga pasar!"
        } else {
            "Sesi negosiasi akuisisi dibuka. Dewan meminta premi pengendalian awal sebesar $${String.format(java.util.Locale.US, "%.2f", initialBoardAsk)}/lembar."
        }

        return TakeoverBiddingState(
            targetStock = stock,
            currentMarketPrice = stock.currentPrice,
            targetStakePercent = initialStakePercent.coerceIn(51.0, 100.0),
            currentOwnedShares = currentOwnedShares,
            initialBoardAskingPrice = initialBoardAsk,
            currentBoardAskingPrice = initialBoardAsk,
            playerOfferPricePerShare = initialPlayerOffer,
            isDistressed = isDistressed,
            negotiationRound = 1,
            maxRounds = 5,
            isDealClosed = false,
            isWalkedAway = false,
            statusMessage = greetingMessage,
            boardMood = initialMood,
            negotiationHistory = listOf(
                NegotiationLogEntry(
                    round = 0,
                    playerOffer = 0.0,
                    boardResponse = greetingMessage,
                    resultType = "GREETING"
                )
            )
        )
    }

    /**
     * Updates target controlling stake (51% - 100%) and recalibrates board expectations.
     */
    fun updateTargetStake(
        currentState: TakeoverBiddingState,
        newStakePercent: Double
    ): TakeoverBiddingState {
        val clampedStake = newStakePercent.coerceIn(51.0, 100.0)
        val updatedBoardAsk = StockAcquisitionEngine.computeInitialBoardAskingPrice(
            marketPrice = currentState.currentMarketPrice,
            targetStakePercent = clampedStake,
            isDistressed = currentState.isDistressed
        )
        val updatedMood = StockAcquisitionEngine.evaluateBoardMood(
            offerPrice = currentState.playerOfferPricePerShare,
            marketPrice = currentState.currentMarketPrice,
            boardAskingPrice = updatedBoardAsk,
            isDistressed = currentState.isDistressed
        )
        return currentState.copy(
            targetStakePercent = clampedStake,
            currentBoardAskingPrice = updatedBoardAsk,
            boardMood = updatedMood
        )
    }

    /**
     * Updates player offer per share and dynamically recalculates board mood & probability.
     */
    fun updatePlayerOffer(
        currentState: TakeoverBiddingState,
        newOfferPrice: Double
    ): TakeoverBiddingState {
        val clampedOffer = newOfferPrice.coerceAtLeast(0.1)
        val updatedMood = StockAcquisitionEngine.evaluateBoardMood(
            offerPrice = clampedOffer,
            marketPrice = currentState.currentMarketPrice,
            boardAskingPrice = currentState.currentBoardAskingPrice,
            isDistressed = currentState.isDistressed
        )
        return currentState.copy(
            playerOfferPricePerShare = clampedOffer,
            boardMood = updatedMood
        )
    }

    /**
     * Submits player bid offer to the board and processes the decision outcome.
     */
    fun submitPlayerBid(currentState: TakeoverBiddingState): Pair<TakeoverBiddingState, BoardDecisionResult> {
        if (currentState.isDealClosed || currentState.isWalkedAway) {
            return Pair(currentState, BoardDecisionResult.Rejected("Negosiasi telah selesai.", walkedAway = true))
        }

        val decision = StockAcquisitionEngine.processBiddingRound(
            offerPrice = currentState.playerOfferPricePerShare,
            marketPrice = currentState.currentMarketPrice,
            currentBoardAskingPrice = currentState.currentBoardAskingPrice,
            round = currentState.negotiationRound,
            maxRounds = currentState.maxRounds,
            targetStakePercent = currentState.targetStakePercent,
            additionalSharesNeeded = currentState.additionalSharesNeeded,
            isDistressed = currentState.isDistressed
        )

        val newHistory = currentState.negotiationHistory.toMutableList()

        return when (decision) {
            is BoardDecisionResult.Accepted -> {
                newHistory.add(
                    NegotiationLogEntry(
                        round = currentState.negotiationRound,
                        playerOffer = currentState.playerOfferPricePerShare,
                        boardResponse = decision.message,
                        resultType = "ACCEPTED"
                    )
                )
                val updatedState = currentState.copy(
                    isDealClosed = true,
                    statusMessage = decision.message,
                    negotiationHistory = newHistory
                )
                Pair(updatedState, decision)
            }
            is BoardDecisionResult.CounterDemand -> {
                newHistory.add(
                    NegotiationLogEntry(
                        round = currentState.negotiationRound,
                        playerOffer = currentState.playerOfferPricePerShare,
                        boardResponse = decision.message,
                        resultType = "BOARD_COUNTER"
                    )
                )
                val nextRound = currentState.negotiationRound + 1
                val updatedMood = StockAcquisitionEngine.evaluateBoardMood(
                    offerPrice = currentState.playerOfferPricePerShare,
                    marketPrice = currentState.currentMarketPrice,
                    boardAskingPrice = decision.counterPricePerShare,
                    isDistressed = currentState.isDistressed
                )
                val updatedState = currentState.copy(
                    currentBoardAskingPrice = decision.counterPricePerShare,
                    negotiationRound = nextRound,
                    statusMessage = decision.message,
                    boardMood = updatedMood,
                    negotiationHistory = newHistory
                )
                Pair(updatedState, decision)
            }
            is BoardDecisionResult.Rejected -> {
                newHistory.add(
                    NegotiationLogEntry(
                        round = currentState.negotiationRound,
                        playerOffer = currentState.playerOfferPricePerShare,
                        boardResponse = decision.reason,
                        resultType = "REJECTED"
                    )
                )
                val updatedState = currentState.copy(
                    isWalkedAway = decision.walkedAway,
                    statusMessage = decision.reason,
                    negotiationHistory = newHistory
                )
                Pair(updatedState, decision)
            }
        }
    }

    /**
     * Executes the finalized corporate takeover merger:
     * 1. Deducts acquisition cost from player cash.
     * 2. Cleanses passive retail stock shares from portfolio.
     * 3. Creates an active OwnedBusiness subsidiary with preserved market cap & cash flow.
     * 4. Seamlessly routes subsidiary into Mega Holding / 3-Tier cash flow distribution.
     * 5. Emits breaking merger news item.
     */
    fun executeTakeover(
        currentState: PlayerState,
        stock: StockItem,
        finalPricePerShare: Double,
        targetStakePercent: Double,
        customName: String? = null
    ): StrategicTakeoverResult {
        if (!currentState.megaHolding.isActive) {
            return StrategicTakeoverResult(
                isSuccess = false,
                updatedPlayerState = currentState,
                message = "Akuisisi gagal: Memerlukan entitas Mega Holding aktif."
            )
        }

        val totalSharesOutstanding = stock.sharesOutstanding.coerceAtLeast(1L)
        val targetShares = ((totalSharesOutstanding.toDouble() * (targetStakePercent / 100.0)).toLong())
            .coerceIn(1L, totalSharesOutstanding)
        val currentOwnedShares = currentState.ownedStocks.find { it.ticker == stock.ticker }?.shares ?: 0L
        val additionalSharesNeeded = (targetShares - currentOwnedShares).coerceAtLeast(1L)
        val requiredCash = (additionalSharesNeeded * finalPricePerShare).toLong()

        if (currentState.cash < requiredCash) {
            return StrategicTakeoverResult(
                isSuccess = false,
                updatedPlayerState = currentState,
                message = "Saldo kas tidak mencukupi untuk menyelesaikan akuisisi ($${requiredCash} dibutuhkan)."
            )
        }

        // Cleanse passive stock portfolio
        val updatedOwnedStocks = currentState.ownedStocks.filterNot { it.ticker == stock.ticker }
        val updatedCorporatePortfolio = currentState.corporateStockPortfolio.filterNot { it.ticker == stock.ticker }

        // Determine matching corporate catalog archetype based on sector
        val catalogId = when (stock.sector.lowercase()) {
            "technology", "tech", "us tech" -> "upper_tech"
            "finance", "banking" -> "tycoon_bank"
            "media", "entertainment" -> "media_tv"
            "property", "real estate" -> "upper_realestate"
            "logistics", "transportation" -> "mid_logistics"
            else -> "tycoon_corp"
        }

        val businessName = customName?.takeIf { it.isNotBlank() } ?: stock.name
        val marketCap = (totalSharesOutstanding * stock.currentPrice).toLong()
        val initialCompanyTreasury = (marketCap * 0.05).coerceAtLeast(500_000.0) // 5% cash reserve
        val monthlyRevenueFlow = ((marketCap * 0.015) / 12).toLong().coerceAtLeast(200_000L)

        val newSubsidiary = OwnedBusiness(
            instanceId = UUID.randomUUID().toString(),
            catalogId = catalogId,
            customName = businessName,
            level = 1,
            companyCash = initialCompanyTreasury,
            acquiredStockTicker = stock.ticker,
            extraValuation = marketCap,
            customRevenue = monthlyRevenueFlow,
            ownershipPercent = targetStakePercent
        )

        // Integrate into 3-Tier cash flow distribution (Holding Company -> Mega Holding)
        var updatedHoldings = currentState.holdingCompanies
        var addedToSubHolding = false

        if (updatedHoldings.isNotEmpty()) {
            val firstHolding = updatedHoldings.first()
            updatedHoldings = updatedHoldings.map { h ->
                if (h.instanceId == firstHolding.instanceId) {
                    h.copy(subsidiaries = h.subsidiaries + newSubsidiary)
                } else h
            }
            addedToSubHolding = true
        }

        val updatedBusinesses = if (addedToSubHolding) {
            currentState.ownedBusinesses
        } else {
            currentState.ownedBusinesses + newSubsidiary
        }

        val updatedPlayerState = currentState.copy(
            cash = currentState.cash - requiredCash,
            ownedStocks = updatedOwnedStocks,
            corporateStockPortfolio = updatedCorporatePortfolio,
            ownedBusinesses = updatedBusinesses,
            holdingCompanies = updatedHoldings
        )

        val newsItem = MarketNews(
            id = "merger_${System.currentTimeMillis()}",
            text = "🏛️ STRATEGIC TAKEOVER: ${currentState.megaHolding.companyName.ifBlank { "Mega Holding" }} resmi mengakuisisi ${String.format(java.util.Locale.US, "%.1f", targetStakePercent)}% saham pengendali ${stock.name} (${stock.ticker}) senilai $${requiredCash}!",
            type = "BULL"
        )

        return StrategicTakeoverResult(
            isSuccess = true,
            updatedPlayerState = updatedPlayerState,
            acquiredBusiness = newSubsidiary,
            message = "Akuisisi mayoritas ${stock.name} (${stock.ticker}) berhasil! Entitas kini beroperasi penuh di bawah Mega Holding.",
            marketNews = newsItem
        )
    }
}
