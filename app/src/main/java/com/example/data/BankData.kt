package com.example.data

data class PreciousMetal(
    val id: String,
    val name: String,
    val currentPrice: Double, // Harga per unit
    val unit: String
)

val initialPreciousMetals = listOf(
    PreciousMetal("gold", "Emas", 85.0, "gram"),      // baseline ~$85/gram
    PreciousMetal("silver", "Perak", 1.5, "gram"),    // baseline ~$1.5/gram
    PreciousMetal("diamond", "Berlian", 5000.0, "karat"), // baseline ~$5000/karat
    PreciousMetal("platinum", "Platinum", 40.0, "gram")   // baseline ~$40/gram
)
