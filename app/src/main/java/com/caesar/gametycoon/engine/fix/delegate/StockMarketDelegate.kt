package com.caesar.gametycoon.engine.fix.delegate

import com.example.data.OwnedStock
import com.example.data.PlayerState
import com.example.data.StockItem

/**
 * Phase 3 Refactor: StockMarketDelegate
 *
 * Extracts stock buying, selling, and portfolio valuation from GameViewModel.
 * Encapsulates validation, weighted average price calculations, and overflow protection.
 */
object StockMarketDelegate {

    data class TradeResult(
        val updatedState: PlayerState,
        val isSuccess: Boolean,
        val message: String
    )

    /**
     * Executes a stock purchase with validation and weighted average price update.
     */
    fun buyStock(
        currentState: PlayerState,
        ticker: String,
        price: Double,
        quantity: Long,
        availableStocks: List<StockItem>
    ): TradeResult {
        if (quantity <= 0L || price <= 0.0) {
            return TradeResult(currentState, false, "Kuantitas atau harga tidak valid.")
        }

        val stockToBuy = availableStocks.find { it.ticker == ticker }
            ?: return TradeResult(currentState, false, "Saham $ticker tidak ditemukan di bursa.")

        if (!com.caesar.gametycoon.stock.takeover.StockAcquisitionRepository.canBuyRetailShares(currentState, stockToBuy, quantity)) {
            return TradeResult(currentState, false, "Strategic Controlling Stake (>50%) requires a Mega Holding Entity to integrate corporate governance.")
        }

        val requiredCashUsd = (price * quantity).toLong()
        if (currentState.cash < requiredCashUsd) {
            return TradeResult(currentState, false, "Saldo Kas Utama tidak mencukupi.")
        }

        val existingStocks = currentState.ownedStocks.toMutableList()
        val existingIndex = existingStocks.indexOfFirst { it.ticker == ticker }

        if (existingIndex != -1) {
            val existing = existingStocks[existingIndex]
            val newShares = existing.shares + quantity
            if (newShares < 0L) return TradeResult(currentState, false, "Melebihi batas saham.") // anti-overflow

            val newAvgPrice = ((existing.shares * existing.averagePrice) + (quantity * price)) / newShares
            existingStocks[existingIndex] = existing.copy(shares = newShares, averagePrice = newAvgPrice)
        } else {
            existingStocks.add(OwnedStock(ticker, price, quantity))
        }

        val newState = currentState.copy(
            cash = currentState.cash - requiredCashUsd,
            ownedStocks = existingStocks,
            corporateStockPortfolio = existingStocks
        )

        return TradeResult(newState, true, "Berhasil membeli $quantity lembar $ticker.")
    }

    /**
     * Executes a stock sale with validation and portfolio update.
     */
    fun sellStock(
        currentState: PlayerState,
        ticker: String,
        price: Double,
        quantity: Long,
        availableStocks: List<StockItem>
    ): TradeResult {
        if (quantity <= 0L || price <= 0.0) {
            return TradeResult(currentState, false, "Kuantitas atau harga tidak valid.")
        }

        val stockToSell = availableStocks.find { it.ticker == ticker }
            ?: return TradeResult(currentState, false, "Saham $ticker tidak ditemukan di bursa.")

        val existingStocks = currentState.ownedStocks.toMutableList()
        val existingIndex = existingStocks.indexOfFirst { it.ticker == ticker }

        if (existingIndex == -1) {
            return TradeResult(currentState, false, "Anda tidak memiliki saham $ticker.")
        }

        val existing = existingStocks[existingIndex]
        if (existing.shares < quantity) {
            return TradeResult(currentState, false, "Jumlah lembar saham yang dimiliki tidak mencukupi.")
        }

        val revenueUsd = (price * quantity).toLong()
        val newShares = existing.shares - quantity

        if (newShares == 0L) {
            existingStocks.removeAt(existingIndex)
        } else {
            existingStocks[existingIndex] = existing.copy(shares = newShares)
        }

        val newState = currentState.copy(
            cash = currentState.cash + revenueUsd,
            ownedStocks = existingStocks,
            corporateStockPortfolio = existingStocks
        )

        return TradeResult(newState, true, "Berhasil menjual $quantity lembar $ticker.")
    }

    /**
     * Computes the current total market value of the corporate stock portfolio.
     */
    fun calculatePortfolioValue(
        ownedStocks: List<OwnedStock>,
        currentMarketPrices: Map<String, Double>
    ): Long {
        return ownedStocks.sumOf { owned ->
            val livePrice = currentMarketPrices[owned.ticker] ?: owned.averagePrice
            (livePrice * owned.shares).toLong()
        }
    }
}
