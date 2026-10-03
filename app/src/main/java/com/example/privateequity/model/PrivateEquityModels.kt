package com.example.privateequity.model

import com.example.data.MarketNews
import com.example.data.PlayerState
import java.util.UUID

/**
 * Funding types for Private Equity & Investor financing.
 */
enum class FundingType {
    DEBT,    // Full monthly installment, 0% equity dilution
    HYBRID,  // 50% monthly installment, 50% equity dilution (Mezzanine)
    EQUITY   // 0 monthly installment, 100% equity dilution (Venture Capital)
}

/**
 * Active loan / financing record owed to an investor or private equity firm.
 */
data class ActiveLoan(
    val id: String = UUID.randomUUID().toString(),
    val sectorName: String,
    val fundingType: FundingType,
    val totalLoan: Long,
    val monthlyPayment: Long,
    var remainingMonths: Int,
    val equityGiven: Double
)

/**
 * Sector offer available for investor financing.
 */
data class SectorOffer(
    val name: String,
    val interestRate: Double,
    val tenor: Int,
    val dilutionMultiplier: Double,
    val description: String
)

/**
 * Pre-calculated estimation of loan parameters for UI feedback.
 */
data class LoanEstimation(
    val baseMonthlyPayment: Long,
    val baseEquityGiven: Double,
    val monthlyPayment: Long,
    val equityGiven: Double,
    val remainingEquityPostDilution: Double,
    val isControlViolation: Boolean
)

/**
 * Result of loan application submission.
 */
data class LoanApplicationResult(
    val isSuccess: Boolean,
    val message: String,
    val updatedState: PlayerState? = null,
    val newsFeedItem: MarketNews? = null
)
