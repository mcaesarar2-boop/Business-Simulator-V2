package com.example.data

data class CryptoItem(
    val id: String,
    val symbol: String,
    val name: String,
    val currentPrice: Double,
    val changePercentage: Double
) {
    val logoUrl: String
        get() = "https://assets.coincap.io/assets/icons/${symbol.lowercase()}@2x.png"
}

data class OwnedCrypto(
    val symbol: String,
    val averagePrice: Double,
    val amount: Double
)
