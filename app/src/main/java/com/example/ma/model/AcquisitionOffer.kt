package com.example.ma.model

import java.util.UUID

/**
 * Defines the target hierarchical level of the player's corporate empire.
 */
enum class TargetLevel(val displayName: String, val badgeColorHex: Long) {
    MEGA_HOLDING("Mega Holding Apex", 0xFFFFD700),
    SUB_HOLDING("Sub-Holding Unit", 0xFF64B5F6),
    UNIT_BUSINESS("Individual Business Asset", 0xFF81C784)
}

/**
 * Status lifecycle of an M&A offer proposal.
 */
enum class OfferStatus(val label: String) {
    PENDING("Menunggu Keputusan"),
    STALLED("Ditunda ke Dewan Direksi"),
    NEGOTIATING("Proses Negosiasi"),
    ACCEPTED("Tawaran Diterima & Saham Dialihkan"),
    REJECTED("Tawaran Ditolak"),
    EXPIRED("Tawaran Kedaluwarsa"),
    WITHDRAWN("Investor Mundur")
}

/**
 * Classification category of the institutional bidder.
 */
enum class BidderType(val label: String, val badgeTag: String) {
    GLOBAL_GIANT("Global Conglomerate & Sovereign Fund", "GLOBAL ELITE"),
    INDONESIAN_CONGLOMERATE("Konglomerasi Konglomerat Indonesia", "INDONESIAN POWERHOUSE")
}

/**
 * Bidder profile metadata.
 */
data class MaBidder(
    val id: String,
    val name: String,
    val type: BidderType,
    val headquarters: String,
    val motto: String,
    val avatarInitials: String,
    val primaryColorHex: Long,
    val aggressiveness: Float = 0.5f, // 0.0 (Conservative) to 1.0 (Predatory/Aggressive)
    val reputationScore: Int = 95
)

/**
 * Negotiation simulation outcomes.
 */
enum class NegotiationOutcome {
    ACCEPTED,      // Bidder accepts player counter-offer
    RE_COUNTERED,  // Bidder re-counters with compromise
    WALKED_AWAY    // Bidder is offended and aborts acquisition
}

/**
 * Result data holder for interactive negotiation.
 */
data class NegotiationResult(
    val outcome: NegotiationOutcome,
    val requestedMultiplier: Double,
    val agreedMultiplier: Double,
    val agreedStake: Double,
    val finalCashOffer: Long,
    val headline: String,
    val detailMessage: String,
    val walkAwayRiskPercent: Int
)

/**
 * Full acquisition & investment offer data model.
 */
data class AcquisitionOffer(
    val offerId: String = UUID.randomUUID().toString(),
    val bidder: MaBidder,
    val targetEntityId: String,
    val targetEntityName: String,
    val targetLevel: TargetLevel,
    val currentEntityValuation: Long,
    val valuationMultiplier: Double,
    val stakePercent: Double,
    val status: OfferStatus = OfferStatus.PENDING,
    val creationMonth: Int,
    val creationYear: Int,
    val stalledTicksRemaining: Int = 3,
    val currentOwnerStake: Double = 100.0,
    val letterOfIntent: String = "",
    val counterRoundsCount: Int = 0
) {
    val offeredValuation: Long
        get() = (currentEntityValuation * valuationMultiplier).toLong()

    val totalCashOffer: Long
        get() = ((currentEntityValuation * valuationMultiplier) * (stakePercent / 100.0)).toLong()

    val premiumPercent: Double
        get() = (valuationMultiplier - 1.0) * 100.0

    val dealPremiumText: String
        get() = when {
            valuationMultiplier >= 1.20 -> "+${String.format(java.util.Locale.US, "%.1f", premiumPercent)}% Premium Hype Acquisition"
            valuationMultiplier >= 1.00 -> "+${String.format(java.util.Locale.US, "%.1f", premiumPercent)}% Above Fair Market Value"
            else -> "${String.format(java.util.Locale.US, "%.1f", premiumPercent)}% Hostile Discount Offer"
        }

    val remainingOwnershipAfterDeal: Double
        get() = (currentOwnerStake - stakePercent).coerceAtLeast(0.0)
}
