package com.example.corporate.model

/**
 * 3-Tier Corporate Hierarchy representation in the tycoon simulation.
 */
enum class CorporateTier(val displayName: String, val level: Int) {
    MEGA_HOLDING("Mega Holding (Root / Profil)", 1),
    SUB_HOLDING("Holding Company (Sub-Holding)", 2),
    BUSINESS_UNIT("Child Business Unit (Operasional)", 3)
}

/**
 * Policy defining profit allocation for a Standalone Business Unit (2-Tier).
 * - unitRetainedPercent: stays in unit's companyCash for local maintenance/growth.
 * - megaHoldingPayoutPercent: paid directly to Mega Holding (player cash).
 */
data class StandaloneDividendPolicy(
    val unitRetainedPercent: Int = 30,
    val megaHoldingPayoutPercent: Int = 70
) {
    init {
        require(unitRetainedPercent + megaHoldingPayoutPercent == 100) {
            "Standalone distribution percentages must sum to 100% (got $unitRetainedPercent + $megaHoldingPayoutPercent)"
        }
    }

    companion object {
        val DEFAULT = StandaloneDividendPolicy(unitRetainedPercent = 30, megaHoldingPayoutPercent = 70)
        val GROWTH = StandaloneDividendPolicy(unitRetainedPercent = 60, megaHoldingPayoutPercent = 40)
        val CASH_COW = StandaloneDividendPolicy(unitRetainedPercent = 10, megaHoldingPayoutPercent = 90)
    }
}

/**
 * Policy defining profit allocation for a Merged Business Unit under a Holding Company (3-Tier).
 * - unitRetainedPercent: stays in unit's companyCash for operations.
 * - holdingTreasuryPercent: deposited to holding's internal treasury (holdingCash).
 * - megaHoldingDividendPercent: upward dividend paid to Mega Holding (player cash).
 */
data class MergedDividendPolicy(
    val unitRetainedPercent: Int = 20,
    val holdingTreasuryPercent: Int = 50,
    val megaHoldingDividendPercent: Int = 30
) {
    init {
        require(unitRetainedPercent + holdingTreasuryPercent + megaHoldingDividendPercent == 100) {
            "Merged distribution percentages must sum to 100% (got $unitRetainedPercent + $holdingTreasuryPercent + $megaHoldingDividendPercent)"
        }
    }

    companion object {
        val DEFAULT = MergedDividendPolicy(unitRetainedPercent = 20, holdingTreasuryPercent = 50, megaHoldingDividendPercent = 30)
        val GROWTH = MergedDividendPolicy(unitRetainedPercent = 40, holdingTreasuryPercent = 45, megaHoldingDividendPercent = 15)
        val CASH_COW = MergedDividendPolicy(unitRetainedPercent = 10, holdingTreasuryPercent = 20, megaHoldingDividendPercent = 70)
        val TREASURY_FOCUSED = MergedDividendPolicy(unitRetainedPercent = 15, holdingTreasuryPercent = 70, megaHoldingDividendPercent = 15)
    }
}

/**
 * Preset configuration archetypes for quick player selection.
 */
enum class DividendPolicyPreset(val title: String, val description: String) {
    BALANCED("Seimbang (Default)", "Distribusi standar untuk stabilitas operasional dan likuiditas."),
    GROWTH_REINVESTMENT("Fokus Ekspansi", "Retensi kas tinggi untuk mempercepat upgrade dan buffer risiko."),
    CASH_COW_LIQUIDITY("Cash Cow (Maks. Dividen)", "Alirkan mayoritas laba ke kas profil untuk akuisisi baru."),
    TREASURY_ACCUMULATION("Kas Internal Holding", "Perkuat treasury holding untuk investasi dan IPO."),
    CUSTOM("Kustom", "Konfigurasi persentase bebas sesuai strategi pemain.")
}

/**
 * Detailed breakdown of a single cash distribution calculation on tick.
 */
data class CashDistributionResult(
    val businessId: String,
    val businessName: String,
    val netProfit: Long,
    val retainedInUnit: Long,
    val holdingTreasuryTransfer: Long,
    val megaHoldingDividend: Long,
    val absorbedDeficit: Long = 0L,
    val sourceTier: CorporateTier,
    val parentHoldingId: String? = null
)

/**
 * Aggregate distribution summary for a Holding Company.
 */
data class HoldingCashFlowSummary(
    val holdingId: String,
    val holdingName: String,
    val totalSubsidiariesNet: Long,
    val retainedInSubsidiaries: Long,
    val depositedToTreasury: Long,
    val paidToMegaHolding: Long,
    val currentTreasuryCash: Double
)
