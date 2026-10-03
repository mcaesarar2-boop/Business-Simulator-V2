package com.example.data.treasury

import java.util.UUID

/**
 * Representation of an active bond contract owned by the corporate treasury.
 */
data class BondHolding(
    val id: String = UUID.randomUUID().toString(),
    val bondId: String,
    val name: String,
    val issuer: String,
    val type: BondType = BondType.SOVEREIGN,
    val rating: CreditRating = CreditRating.AAA,
    val principal: Long, // Nominal modal pokok yang ditempatkan
    val annualCouponRate: Double, // Kupon tahunan
    val totalTenureMonths: Int,
    val monthsRemaining: Int,
    val purchasedMonth: Int = 1,
    val purchasedYear: Int = 2026,
    val totalCouponEarned: Long = 0L
) {
    val isMatured: Boolean get() = monthsRemaining <= 0
    val monthlyCouponAmount: Long get() = (principal * (annualCouponRate / 12.0)).toLong()
    val tenureProgress: Float get() = if (totalTenureMonths > 0) {
        ((totalTenureMonths - monthsRemaining).toFloat() / totalTenureMonths.toFloat()).coerceIn(0f, 1f)
    } else 1f
}
