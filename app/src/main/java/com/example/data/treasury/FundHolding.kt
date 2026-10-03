package com.example.data.treasury

/**
 * Tracks corporate treasury holdings in Fixed Income Mutual Funds (RDPT).
 */
data class FundHolding(
    val fundId: String = "premier_rdpt",
    val totalUnits: Double = 0.0,
    val totalInvestedCost: Long = 0L,
    val totalRedeemedCash: Long = 0L,
    val totalFeePaid: Long = 0L
) {
    fun currentValue(navPerUnit: Double): Long {
        return (totalUnits * navPerUnit).toLong()
    }

    fun unrealizedPnl(navPerUnit: Double): Long {
        if (totalUnits <= 0.0 || totalInvestedCost <= 0L) return 0L
        return currentValue(navPerUnit) - totalInvestedCost
    }

    fun returnPercentage(navPerUnit: Double): Double {
        if (totalInvestedCost <= 0L || totalUnits <= 0.0) return 0.0
        val currVal = currentValue(navPerUnit).toDouble()
        return ((currVal - totalInvestedCost.toDouble()) / totalInvestedCost.toDouble()) * 100.0
    }
}
