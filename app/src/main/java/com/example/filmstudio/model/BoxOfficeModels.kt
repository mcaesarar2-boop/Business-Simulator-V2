package com.example.filmstudio.model

/**
 * Rating classification tiers based on critical and audience reception (0-100 scale).
 * Defines the baseline box office multiplier potential and real-world performance tier.
 */
enum class BoxOfficeRatingTier(
    val minRating: Int,
    val maxRating: Int,
    val minMultiplier: Double,
    val maxMultiplier: Double,
    val statusName: String,
    val description: String,
    val realWorldEquivalent: String
) {
    DISASTER_FLOP(
        minRating = 0,
        maxRating = 39,
        minMultiplier = 0.2,
        maxMultiplier = 0.5,
        statusName = "Disaster Flop",
        description = "Catastrophic loss, empty theaters",
        realWorldEquivalent = "Severe box office bomb (e.g., Cats, Morbius)"
    ),
    UNDERPERFORMER(
        minRating = 40,
        maxRating = 54,
        minMultiplier = 0.6,
        maxMultiplier = 0.9,
        statusName = "Underperformer",
        description = "Loss, fails to break even",
        realWorldEquivalent = "Disappointing commercial return"
    ),
    MEDIOCRE(
        minRating = 55,
        maxRating = 69,
        minMultiplier = 1.0,
        maxMultiplier = 1.4,
        statusName = "Mediocre / Average",
        description = "Barely breaks even after studio cut",
        realWorldEquivalent = "Standard commercial performance"
    ),
    COMMERCIAL_HIT(
        minRating = 70,
        maxRating = 81,
        minMultiplier = 1.8,
        maxMultiplier = 3.2,
        statusName = "Commercial Hit",
        description = "Healthy profit margin",
        realWorldEquivalent = "Crowd-pleaser and critical success"
    ),
    BLOCKBUSTER(
        minRating = 82,
        maxRating = 92,
        minMultiplier = 3.5,
        maxMultiplier = 5.5,
        statusName = "Blockbuster",
        description = "Major hit, high profit",
        realWorldEquivalent = "Worldwide box office sensation"
    ),
    MASTERPIECE(
        minRating = 93,
        maxRating = 100,
        minMultiplier = 6.0,
        maxMultiplier = 9.0,
        statusName = "Cultural Masterpiece",
        description = "Rare global phenomenon",
        realWorldEquivalent = "Historic cinematic milestone (e.g., Titanic, Avatar)"
    );

    companion object {
        fun fromRating(rating: Int): BoxOfficeRatingTier {
            val clamped = rating.coerceIn(0, 100)
            return values().firstOrNull { clamped in it.minRating..it.maxRating } ?: MEDIOCRE
        }
    }
}

/**
 * Budget scaling categories modeling diminishing returns on large financial investments.
 */
enum class BudgetScaleCategory(
    val minBudget: Long,
    val maxBudget: Long,
    val scaleFactor: Double,
    val categoryName: String,
    val upsideBehavior: String
) {
    MICRO_INDIE(
        minBudget = 0L,
        maxBudget = 5_000_000L - 1L,
        scaleFactor = 2.2,
        categoryName = "Micro / Indie (< $5M)",
        upsideBehavior = "High viral upside, potential massive ROI %"
    ),
    MID_BUDGET(
        minBudget = 5_000_000L,
        maxBudget = 25_000_000L,
        scaleFactor = 1.4,
        categoryName = "Mid-Budget ($5M - $25M)",
        upsideBehavior = "Balanced risk and reward"
    ),
    STANDARD_STUDIO(
        minBudget = 25_000_001L,
        maxBudget = 75_000_000L,
        scaleFactor = 1.0,
        categoryName = "Standard Studio ($25M - $75M)",
        upsideBehavior = "Baseline standard scaling"
    ),
    BIG_BUDGET(
        minBudget = 75_000_001L,
        maxBudget = 150_000_000L,
        scaleFactor = 0.8,
        categoryName = "Big Budget ($75M - $150M)",
        upsideBehavior = "Harder to achieve massive ROI %"
    ),
    MEGA_TENTPOLE(
        minBudget = 150_000_001L,
        maxBudget = Long.MAX_VALUE,
        scaleFactor = 0.65,
        categoryName = "Mega Tentpole (> $150M)",
        upsideBehavior = "High absolute $ profit, low ROI % multiplier"
    );

    companion object {
        fun fromTotalBudget(totalBudget: Long): BudgetScaleCategory {
            return when {
                totalBudget < 5_000_000L -> MICRO_INDIE
                totalBudget <= 25_000_000L -> MID_BUDGET
                totalBudget <= 75_000_000L -> STANDARD_STUDIO
                totalBudget <= 150_000_000L -> BIG_BUDGET
                else -> MEGA_TENTPOLE
            }
        }
    }
}

/**
 * Result data class containing all financial metrics from a Box Office simulation run.
 */
data class BoxOfficeFinancialResult(
    val productionBudget: Long,
    val marketingBudget: Long,
    val totalCost: Long,
    val rating: Int,
    val ratingTier: BoxOfficeRatingTier,
    val baseRatingMultiplier: Double,
    val budgetCategory: BudgetScaleCategory,
    val budgetScaleFactor: Double,
    val randomVariance: Double,
    val finalMultiplier: Double,
    val grossBoxOffice: Long,
    val studioSharePercentage: Double,
    val studioNetRevenue: Long,
    val netProfit: Long,
    val returnOnInvestmentPercent: Double
) {
    val isProfitable: Boolean get() = netProfit > 0
}

/**
 * Pre-release range estimation for investor pitch decks and UI projections.
 */
data class BoxOfficeProjectionRange(
    val totalInvestment: Long,
    val productionBudget: Long,
    val marketingBudget: Long,
    val worstCaseGross: Long,
    val worstCaseStudioNet: Long,
    val worstCaseProfit: Long,
    val expectedGross: Long,
    val expectedStudioNet: Long,
    val expectedProfit: Long,
    val bestCaseGross: Long,
    val bestCaseStudioNet: Long,
    val bestCaseProfit: Long,
    val budgetCategory: BudgetScaleCategory,
    val studioSharePercentage: Double
)
