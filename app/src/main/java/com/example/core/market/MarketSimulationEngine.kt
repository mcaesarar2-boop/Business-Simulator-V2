package com.example.core.market

import com.example.data.CryptoItem
import com.example.data.MarketNews
import com.example.data.PreciousMetal
import com.example.data.StockItem
import com.example.data.Tycoon
import com.example.data.getInitialBillionaires
import com.example.data.initialCryptoList
import com.example.data.initialPreciousMetals
import com.example.ui.calculateFluctuatingPrice
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Standalone engine managing all real-time market simulations:
 * 1. Global Stock Market (Random Walk with Drift, Regimes, Inverse Correlation to Safe Havens).
 * 2. Crypto Market (High-volatility simulations, Pump & Dump shock events).
 * 3. Precious Metals (Gold / Silver / Platinum flight-to-safety mechanics).
 * 4. Global Tycoon / Billionaire Index rankings.
 * 5. Dynamic Financial News Feed.
 */
class MarketSimulationEngine(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    initialVolatilityFactor: Float = 1.0f,
    initialIntervalSeconds: Float = 30.0f
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    // Configurable parameters
    private val _stockIntervalSeconds = MutableStateFlow(initialIntervalSeconds.coerceIn(1.0f, 60.0f))
    val stockIntervalSeconds: StateFlow<Float> = _stockIntervalSeconds.asStateFlow()

    private val _marketVolatilityFactor = MutableStateFlow(initialVolatilityFactor.coerceIn(0.1f, 5.0f))
    val marketVolatilityFactor: StateFlow<Float> = _marketVolatilityFactor.asStateFlow()

    // Observable Asset & Economy States
    private val _stockList = MutableStateFlow<List<StockItem>>(emptyList())
    val stockList: StateFlow<List<StockItem>> = _stockList.asStateFlow()

    private val _cryptoList = MutableStateFlow<List<CryptoItem>>(initialCryptoList)
    val cryptoList: StateFlow<List<CryptoItem>> = _cryptoList.asStateFlow()

    private val _preciousMetalsList = MutableStateFlow<List<PreciousMetal>>(initialPreciousMetals)
    val preciousMetalsList: StateFlow<List<PreciousMetal>> = _preciousMetalsList.asStateFlow()

    private val _tycoonList = MutableStateFlow<List<Tycoon>>(getInitialBillionaires())
    val tycoonList: StateFlow<List<Tycoon>> = _tycoonList.asStateFlow()

    private val _newsFeed = MutableStateFlow<List<MarketNews>>(
        listOf(MarketNews("0", "Sesi Pasar dibuka. Seluruh pasar global dan domestik beroperasi normal.", "NEUTRAL"))
    )
    val newsFeed: StateFlow<List<MarketNews>> = _newsFeed.asStateFlow()

    // Internal simulation memory
    private val initialStockPrices = ConcurrentHashMap<String, Double>()
    private val stockTrends = ConcurrentHashMap<String, StockTrendState>()
    private var newsCounter = 1L

    private var stockLoopJob: Job? = null
    private var cryptoLoopJob: Job? = null
    private var tycoonLoopJob: Job? = null

    // Provider for in-game month duration (needed for macro cycle math)
    var monthDurationMsProvider: () -> Long = { 120_000L }

    // Provider for player net worth (needed for dynamic Tycoon leaderboard)
    var playerNetWorthProvider: () -> Long = { 0L }

    /**
     * Seed or replace the stock market catalogue.
     */
    fun setStockList(stocks: List<StockItem>) {
        stocks.forEach { stock ->
            if (!initialStockPrices.containsKey(stock.ticker)) {
                initialStockPrices[stock.ticker] = stock.currentPrice
            }
        }
        _stockList.value = stocks
    }

    fun setPreciousMetals(metals: List<PreciousMetal>) {
        _preciousMetalsList.value = metals
    }

    fun setCryptoList(cryptos: List<CryptoItem>) {
        _cryptoList.value = cryptos
    }

    fun updateStockInterval(seconds: Float) {
        _stockIntervalSeconds.value = seconds.coerceIn(1.0f, 60.0f)
    }

    fun updateMarketVolatility(volatility: Float) {
        _marketVolatilityFactor.value = volatility.coerceIn(0.1f, 5.0f)
    }

    /**
     * Starts all background market simulation coroutines.
     */
    fun startMarketLoops() {
        startStockMarketLoop()
        startCryptoMarketLoop()
        startTycoonMarketLoop()
    }

    /**
     * Halts all background market loops.
     */
    fun stopMarketLoops() {
        stockLoopJob?.cancel()
        cryptoLoopJob?.cancel()
        tycoonLoopJob?.cancel()
        stockLoopJob = null
        cryptoLoopJob = null
        tycoonLoopJob = null
    }

    private fun startStockMarketLoop() {
        if (stockLoopJob?.isActive == true) return
        stockLoopJob = scope.launch {
            while (isActive) {
                val delayMs = (_stockIntervalSeconds.value * 1000f).toLong().coerceAtLeast(100L)
                delay(delayMs)

                val volatility = _marketVolatilityFactor.value
                val monthMs = monthDurationMsProvider()

                val currentStocks = _stockList.value
                if (currentStocks.isEmpty()) continue

                val updatedList = currentStocks.map { stock ->
                    val baseline = initialStockPrices[stock.ticker] ?: stock.currentPrice

                    var trendState = stockTrends[stock.ticker]
                    var newlyTransitioned = false
                    val oldTrend = trendState?.currentTrend

                    if (trendState == null || trendState.durationLeftMs <= 0) {
                        trendState = assignNewTrend(stock.ticker, oldTrend, monthMs)
                        stockTrends[stock.ticker] = trendState
                        newlyTransitioned = true
                    } else {
                        trendState.durationLeftMs -= delayMs
                    }

                    var baseDrift = 0.0
                    var randVolatility = 0.0

                    when (trendState.currentTrend) {
                        MarketTrend.BULL_MARKET -> {
                            baseDrift = 0.003
                            randVolatility = 0.012
                        }
                        MarketTrend.BEAR_MARKET -> {
                            baseDrift = -0.004
                            randVolatility = 0.015
                        }
                        MarketTrend.STEADY_GROWTH -> {
                            baseDrift = 0.0015
                            randVolatility = 0.004
                        }
                        MarketTrend.STEADY_BLEED -> {
                            baseDrift = -0.0015
                            randVolatility = 0.004
                        }
                        MarketTrend.THE_LOST_DECADE -> {
                            baseDrift = 0.0
                            randVolatility = 0.001
                        }
                        MarketTrend.LONG_TERM_CYCLICAL -> {
                            val cyclePhase = (trendState.durationLeftMs / monthMs).toInt() % 2
                            baseDrift = if (cyclePhase == 0) 0.008 else -0.008
                            randVolatility = 0.018
                        }
                        MarketTrend.WHIPSAW_TRAP -> {
                            baseDrift = 0.006
                            randVolatility = 0.025
                        }
                    }

                    // Calculation using Random Walk with Drift algorithm
                    val safeCurrentPrice = if (stock.currentPrice <= 0.00) 0.05 else stock.currentPrice
                    val effectiveVolatility = randVolatility * volatility
                    val newPrice = calculateFluctuatingPrice(
                        currentPrice = safeCurrentPrice,
                        volatility = effectiveVolatility,
                        trend = baseDrift,
                        eventShock = 0.0,
                        minPrice = 0.01
                    )
                    val newChangeAbs = newPrice - baseline
                    val newChangePct = (newChangeAbs / baseline) * 100
                    val newHistory = (stock.priceHistory + newPrice).takeLast(40)

                    // Alert system hook logic
                    if (newlyTransitioned && Math.random() < 0.35) {
                        pushMarketNewsForTransition(stock.ticker, stock.name, trendState.currentTrend)
                    }

                    stock.copy(
                        currentPrice = newPrice,
                        changeAbsolute = newChangeAbs,
                        changePercentage = newChangePct,
                        priceHistory = newHistory
                    )
                }

                _stockList.value = updatedList

                // Inverse Correlation: Global Stock Market vs. Gold & Precious Metals (Safe Haven Assets)
                val totalStocksCount = stockTrends.size
                val bearCount = stockTrends.values.count {
                    it.currentTrend == MarketTrend.BEAR_MARKET ||
                            it.currentTrend == MarketTrend.STEADY_BLEED ||
                            it.currentTrend == MarketTrend.THE_LOST_DECADE
                }
                val bearRatio = if (totalStocksCount > 0) bearCount.toDouble() / totalStocksCount else 0.0

                val updatedMetals = _preciousMetalsList.value.map { metal ->
                    val metalVolatility = 0.005 * volatility // Low volatility commodity
                    val metalTrend = 0.001 // Baseline inflation drift

                    // Safe Haven Inverse Shock: Stock Crash -> Gold Surge; Stock Bull -> Gold Stagnation
                    val metalEventShock = when {
                        bearRatio > 0.45 -> 0.020 + (Math.random() * 0.015) // Flight to safety during stock panic
                        bearRatio > 0.30 -> 0.008 + (Math.random() * 0.008) // Mild market uncertainty
                        bearRatio < 0.15 -> -0.003 - (Math.random() * 0.004) // Strong bull market, gold/metals stagnate
                        else -> 0.0
                    }

                    val newPrice = calculateFluctuatingPrice(
                        currentPrice = metal.currentPrice,
                        volatility = metalVolatility,
                        trend = metalTrend,
                        eventShock = metalEventShock,
                        minPrice = 0.01
                    )
                    metal.copy(currentPrice = newPrice)
                }
                _preciousMetalsList.value = updatedMetals
            }
        }
    }

    private fun assignNewTrend(ticker: String, previousTrend: MarketTrend?, monthMs: Long): StockTrendState {
        val yearMs = 12 * monthMs

        // Post-Crash Whipsaw Rule
        if (previousTrend == MarketTrend.BEAR_MARKET && Math.random() < 0.60) {
            return StockTrendState(MarketTrend.WHIPSAW_TRAP, (Math.random() * yearMs).toLong())
        }

        // Forced follow-up after Whipsaw Trap
        if (previousTrend == MarketTrend.WHIPSAW_TRAP) {
            val next = if (Math.random() < 0.5) MarketTrend.STEADY_BLEED else MarketTrend.BEAR_MARKET
            val dur = ((if (next == MarketTrend.BEAR_MARKET) 1 else 2) + Math.random() * 2) * yearMs
            return StockTrendState(next, dur.toLong())
        }

        // Standard distribution
        val rand = Math.random()
        val (trend, durationYears) = when {
            rand < 0.15 -> MarketTrend.BULL_MARKET to (1 + Math.random() * 2)
            rand < 0.30 -> MarketTrend.BEAR_MARKET to (1 + Math.random() * 1)
            rand < 0.50 -> MarketTrend.STEADY_GROWTH to (3 + Math.random() * 2)
            rand < 0.70 -> MarketTrend.STEADY_BLEED to (2 + Math.random() * 2)
            rand < 0.85 -> MarketTrend.THE_LOST_DECADE to (5 + Math.random() * 5)
            else -> MarketTrend.LONG_TERM_CYCLICAL to listOf(3.0, 5.0, 7.0, 10.0).random()
        }
        return StockTrendState(trend, (durationYears * yearMs).toLong())
    }

    private fun pushMarketNewsForTransition(ticker: String, name: String, targetTrend: MarketTrend) {
        val (text, type) = when (targetTrend) {
            MarketTrend.BULL_MARKET -> "INSIDER TIP: Laporan keuangan $ticker ($name) sangat positif! Broker memprediksi lonjakan harga tajam." to "BULL"
            MarketTrend.BEAR_MARKET -> "PANIC SELL: Skandal internal melanda $ticker ($name). Harga diprediksi akan terjun bebas!" to "BEAR"
            MarketTrend.THE_LOST_DECADE -> "ANALISIS: Prospek $ticker ($name) dinilai stagnan untuk beberapa tahun ke depan. Pasar merespon dingin." to "NEUTRAL"
            MarketTrend.WHIPSAW_TRAP -> "BREAKING: $ticker ($name) tiba-tiba meroket keras hari ini! Momen kebangkitan atau hanya jebakan banteng (Bull Trap)?" to "BULL"
            MarketTrend.LONG_TERM_CYCLICAL -> "MARKET WATCH: $ticker ($name) berpotensi mengalami volatilitas ekstrem. Awas ayunan harga yang liar!" to "NEUTRAL"
            else -> return // STEADY_GROWTH, STEADY_BLEED do not need to spam news
        }

        val newsItem = MarketNews(
            id = (newsCounter++).toString(),
            text = text,
            type = type
        )
        _newsFeed.value = (listOf(newsItem) + _newsFeed.value).take(20)
    }

    private fun startCryptoMarketLoop() {
        if (cryptoLoopJob?.isActive == true) return
        cryptoLoopJob = scope.launch {
            val initialCryptoPrices = initialCryptoList.associate { it.symbol to it.currentPrice }
            while (isActive) {
                delay((_stockIntervalSeconds.value * 1000f).toLong().coerceAtLeast(100L))
                val volatilityMultiplier = _marketVolatilityFactor.value * 2.5f // Crypto is highly volatile
                val triggerNews = Math.random() < 0.10

                var shock = 0.0
                var newsItem: MarketNews? = null

                if (triggerNews) {
                    val rand = Math.random()
                    if (rand < 0.5) {
                        shock = 0.04 + (Math.random() * 0.06) // PUMP: +4% to +10%
                        newsItem = MarketNews(
                            id = "crypto_b_${System.currentTimeMillis()}",
                            text = "CRYPTO PUMP: Institusi besar mulai adopsi masal blockchain!",
                            type = "BULL"
                        )
                    } else {
                        shock = -0.04 - (Math.random() * 0.06) // CRASH: -4% to -10%
                        newsItem = MarketNews(
                            id = "crypto_b_${System.currentTimeMillis()}",
                            text = "CRYPTO CRASH: Regulasi ketat memukul pasar kripto!",
                            type = "BEAR"
                        )
                    }
                    val newFeeds = listOf(newsItem) + _newsFeed.value
                    _newsFeed.value = newFeeds.take(20)
                }

                val updatedCrypto = _cryptoList.value.map { crypto ->
                    val baseline = initialCryptoPrices[crypto.symbol] ?: crypto.currentPrice
                    val cryptoTrend = 0.0005 // Mild trend
                    val cryptoVol = 0.015 * volatilityMultiplier

                    val newPrice = calculateFluctuatingPrice(
                        currentPrice = crypto.currentPrice,
                        volatility = cryptoVol,
                        trend = cryptoTrend,
                        eventShock = shock,
                        minPrice = 0.000001
                    )
                    val newChangeAbs = newPrice - baseline
                    val newChangePct = (newChangeAbs / baseline) * 100

                    crypto.copy(
                        currentPrice = newPrice,
                        changePercentage = newChangePct
                    )
                }

                _cryptoList.value = updatedCrypto
            }
        }
    }

    private fun startTycoonMarketLoop() {
        if (tycoonLoopJob?.isActive == true) return
        tycoonLoopJob = scope.launch {
            while (isActive) {
                delay((_stockIntervalSeconds.value * 2000f).toLong().coerceAtLeast(200L))
                updateTycoons()
            }
        }
    }

    private fun updateTycoons() {
        val currentTycoons = _tycoonList.value
        val playerWorth = playerNetWorthProvider()

        // Remove old player dummy entry if exists
        var updated = currentTycoons.filter { !it.isPlayer }.toMutableList()

        // Add player live entry
        updated.add(Tycoon("player", "You", playerWorth, true))

        updated = updated.map { tycoon ->
            if (tycoon.isPlayer) {
                tycoon
            } else {
                val fluctuation = (Math.random() * 0.03 - 0.01) // -1% to +2%
                val newWorth = tycoon.netWorth + (tycoon.netWorth * fluctuation).toLong()
                tycoon.copy(netWorth = newWorth)
            }
        }.toMutableList()

        _tycoonList.value = updated.sortedByDescending { it.netWorth }
    }

    /**
     * Allows pushing external news events (e.g. from acquisitions, breaking corporate events).
     */
    fun pushCustomNews(text: String, type: String) {
        val newsItem = MarketNews(
            id = "custom_${System.currentTimeMillis()}_${newsCounter++}",
            text = text,
            type = type
        )
        _newsFeed.value = (listOf(newsItem) + _newsFeed.value).take(20)
    }

    fun dispose() {
        stopMarketLoops()
        scope.cancel()
    }
}
