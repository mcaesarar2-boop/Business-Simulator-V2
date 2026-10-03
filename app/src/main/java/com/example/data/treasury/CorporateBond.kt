package com.example.data.treasury

import java.util.UUID

/**
 * Categorization of Bond types available in corporate treasury.
 */
enum class BondType(val displayName: String, val badgeLabel: String) {
    SOVEREIGN("Obligasi Negara / SBN", "Negara"),
    CORPORATE("Obligasi Korporasi", "Korporat")
}

/**
 * Credit Rating for evaluating risk, default probability, and expected coupon yield.
 */
enum class CreditRating(
    val code: String,
    val riskLabel: String,
    val baseYieldRange: String,
    val score: Int
) {
    AAA("AAA", "Risiko Sangat Rendah (Prime)", "6.0% - 8.0%", 1),
    AA("AA", "Risiko Rendah (High Quality)", "8.5% - 11.5%", 2),
    BBB("BBB", "Risiko Menengah (High Yield)", "12.0% - 15.5%", 3)
}

/**
 * Immutable catalogue definition of an institutional bond offering.
 */
data class CorporateBond(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val issuer: String,
    val type: BondType,
    val rating: CreditRating,
    val annualCouponRate: Double, // e.g. 0.075 for 7.5% p.a.
    val tenureMonths: Int, // e.g. 6, 12, 24, 36
    val minInvestment: Long = 10_000L,
    val description: String = ""
) {
    val monthlyCouponRate: Double get() = annualCouponRate / 12.0
    val annualCouponPercent: Double get() = annualCouponRate * 100.0
}
