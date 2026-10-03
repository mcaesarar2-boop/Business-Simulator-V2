package com.example.data

data class PropertyItem(
    val id: String,
    val name: String,
    val location: String,
    val type: String,
    val basePrice: Long,
    val baseRentalIncome: Long,
    val imageUrl: String = "",
    val condition: Int = 100
) {
    val currentPrice: Long 
        get() = if (condition < 100) (basePrice * condition / 100.0).toLong() else basePrice
}

data class OwnedProperty(
    val propertyId: String,
    val purchasedPrice: Long,
    val currentEstimatedValue: Long,
    val condition: Int = 100,
    val isFlipped: Boolean = false
) {
    val isBapuk: Boolean get() = condition < 100
}
