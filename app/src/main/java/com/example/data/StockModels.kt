package com.example.data

data class StockItem(
    val id: String,
    val ticker: String,
    val name: String,
    val currentPrice: Double,
    val changeAbsolute: Double,
    val changePercentage: Double,
    val sector: String,
    val priceHistory: List<Double> = emptyList(),
    val sharesOutstanding: Long = 0L
) {
    val logoDomain: String?
        get() = getDomainForTicker(ticker)
        
    val logoUrl: String?
        get() = logoDomain?.let { "https://t3.gstatic.com/faviconV2?client=SOCIAL&type=FAVICON&fallback_opts=TYPE,SIZE,URL&url=http://$it&size=128" }
}

data class StockStats(
    val sharesOutstanding: Long,
    val dividendYield: Double,
    val peRatio: Double,
    val highToday: Double,
    val lowToday: Double
)

data class MarketNews(
    val id: String,
    val text: String,
    val type: String,
    val timestamp: String = "Now"
)
