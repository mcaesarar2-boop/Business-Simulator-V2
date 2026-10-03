package com.example.filmstudio.engine

import com.example.filmstudio.model.BoxOfficeFinancialResult
import com.example.filmstudio.model.BoxOfficeProjectionRange
import com.example.filmstudio.model.BoxOfficeRatingTier
import com.example.filmstudio.model.BudgetScaleCategory
import java.util.Random
import kotlin.math.roundToLong

/**
 * Game Balance & Financial Algorithm Engine for Movie Studio Box Office Simulation.
 * Implements realistic box office revenue, studio cut, and diminishing returns dynamics.
 */
object BoxOfficeEngine {

    const val DEFAULT_STUDIO_SHARE_PERCENTAGE: Double = 0.48 // 48% studio theatrical cut
    const val DEFAULT_MIN_VARIANCE: Double = 0.82 // -18% market headwind
    const val DEFAULT_MAX_VARIANCE: Double = 1.18 // +18% viral boost

    private val random = Random()

    /**
     * Mode for calculating base multiplier from rating tier.
     */
    enum class BaseMultiplierMode {
        INTERPOLATE, // Smooth linear interpolation based on score position in tier
        RANDOM_UNIFORM, // Random sample uniformly distributed within tier range
        MIDPOINT // Exact average of the tier range
    }

    /**
     * Calculates total investment cost (Production Budget + Marketing / P&A).
     */
    fun calculateTotalCost(productionBudget: Long, marketingBudget: Long): Long {
        return (productionBudget + marketingBudget).coerceAtLeast(0L)
    }

    /**
     * Retrieves the rating tier corresponding to a critical/audience review score (0-100).
     */
    fun getRatingTier(rating: Int): BoxOfficeRatingTier {
        return BoxOfficeRatingTier.fromRating(rating)
    }

    /**
     * Calculates the base box office multiplier according to the rating score.
     */
    fun calculateBaseRatingMultiplier(
        rating: Int,
        mode: BaseMultiplierMode = BaseMultiplierMode.INTERPOLATE,
        customRandom: Random = random
    ): Double {
        val tier = getRatingTier(rating)
        return when (mode) {
            BaseMultiplierMode.INTERPOLATE -> {
                val clamped = rating.coerceIn(tier.minRating, tier.maxRating)
                val span = maxOf(1, tier.maxRating - tier.minRating)
                val fraction = (clamped - tier.minRating).toDouble() / span.toDouble()
                tier.minMultiplier + (fraction * (tier.maxMultiplier - tier.minMultiplier))
            }
            BaseMultiplierMode.RANDOM_UNIFORM -> {
                val r = customRandom.nextDouble()
                tier.minMultiplier + (r * (tier.maxMultiplier - tier.minMultiplier))
            }
            BaseMultiplierMode.MIDPOINT -> {
                (tier.minMultiplier + tier.maxMultiplier) / 2.0
            }
        }
    }

    /**
     * Identifies the budget scaling category for a given total investment budget.
     */
    fun getBudgetCategory(totalBudget: Long): BudgetScaleCategory {
        return BudgetScaleCategory.fromTotalBudget(totalBudget)
    }

    /**
     * Returns the budget scale factor (diminishing return multiplier) for a given total investment.
     */
    fun getBudgetScaleFactor(totalBudget: Long): Double {
        return getBudgetCategory(totalBudget).scaleFactor
    }

    /**
     * Generates a random market variance multiplier.
     * Models external factors such as competition, weather, macro economy, and word-of-mouth luck.
     *
     * @param min Minimum variance factor (default 0.82)
     * @param max Maximum variance factor (default 1.18)
     * @param useGaussian If true, uses a normal distribution centered at 1.0 (StdDev = 0.08)
     */
    fun generateRandomVariance(
        min: Double = DEFAULT_MIN_VARIANCE,
        max: Double = DEFAULT_MAX_VARIANCE,
        useGaussian: Boolean = true,
        customRandom: Random = random
    ): Double {
        val value = if (useGaussian) {
            // Gaussian centered at 1.0 with standard deviation 0.08
            1.0 + (customRandom.nextGaussian() * 0.08)
        } else {
            min + (customRandom.nextDouble() * (max - min))
        }
        return value.coerceIn(min, max)
    }

    /**
     * Core calculation method executing the complete financial pipeline:
     * 1. TotalCost = ProductionBudget + MarketingBudget
     * 2. FinalMultiplier = BaseRatingMultiplier * BudgetScaleFactor * RandomVariance
     * 3. GrossBoxOffice = TotalCost * FinalMultiplier
     * 4. StudioNetRevenue = GrossBoxOffice * StudioSharePercentage
     * 5. NetProfit = StudioNetRevenue - TotalCost
     */
    fun calculateFinancials(
        productionBudget: Long,
        marketingBudget: Long,
        rating: Int,
        studioSharePercentage: Double = DEFAULT_STUDIO_SHARE_PERCENTAGE,
        forcedBaseMultiplier: Double? = null,
        forcedScaleFactor: Double? = null,
        forcedVariance: Double? = null,
        baseMultiplierMode: BaseMultiplierMode = BaseMultiplierMode.INTERPOLATE
    ): BoxOfficeFinancialResult {
        val totalCost = calculateTotalCost(productionBudget, marketingBudget)
        val tier = getRatingTier(rating)
        val baseRatingMultiplier = forcedBaseMultiplier
            ?: calculateBaseRatingMultiplier(rating, baseMultiplierMode)

        val budgetCategory = getBudgetCategory(totalCost)
        val budgetScaleFactor = forcedScaleFactor ?: budgetCategory.scaleFactor

        val randomVariance = forcedVariance ?: generateRandomVariance()

        val finalMultiplier = baseRatingMultiplier * budgetScaleFactor * randomVariance

        val grossBoxOffice = (totalCost.toDouble() * finalMultiplier).roundToLong().coerceAtLeast(0L)
        val studioNetRevenue = (grossBoxOffice.toDouble() * studioSharePercentage).roundToLong().coerceAtLeast(0L)
        val netProfit = studioNetRevenue - totalCost

        val roiPercent = if (totalCost > 0L) {
            (netProfit.toDouble() / totalCost.toDouble()) * 100.0
        } else 0.0

        return BoxOfficeFinancialResult(
            productionBudget = productionBudget,
            marketingBudget = marketingBudget,
            totalCost = totalCost,
            rating = rating.coerceIn(0, 100),
            ratingTier = tier,
            baseRatingMultiplier = baseRatingMultiplier,
            budgetCategory = budgetCategory,
            budgetScaleFactor = budgetScaleFactor,
            randomVariance = randomVariance,
            finalMultiplier = finalMultiplier,
            grossBoxOffice = grossBoxOffice,
            studioSharePercentage = studioSharePercentage,
            studioNetRevenue = studioNetRevenue,
            netProfit = netProfit,
            returnOnInvestmentPercent = roiPercent
        )
    }

    /**
     * Calculates worst-case, expected, and best-case box office revenue projections
     * for pre-production planning, investor pitch decks, and UI forecasts.
     */
    fun calculateProjectionRange(
        productionBudget: Long,
        marketingBudget: Long,
        expectedRating: Int = 75,
        studioSharePercentage: Double = DEFAULT_STUDIO_SHARE_PERCENTAGE
    ): BoxOfficeProjectionRange {
        val totalCost = calculateTotalCost(productionBudget, marketingBudget)
        val budgetCategory = getBudgetCategory(totalCost)
        val scaleFactor = budgetCategory.scaleFactor

        // Worst case: Disastrous critical reception (score ~30) + bad luck (0.82x variance)
        val worstTier = BoxOfficeRatingTier.DISASTER_FLOP
        val worstBase = worstTier.minMultiplier
        val worstMult = worstBase * scaleFactor * DEFAULT_MIN_VARIANCE
        val worstGross = (totalCost.toDouble() * worstMult).roundToLong().coerceAtLeast(0L)
        val worstNet = (worstGross.toDouble() * studioSharePercentage).roundToLong()

        // Expected case: Target rating + neutral variance (1.0x)
        val expTier = getRatingTier(expectedRating)
        val expBase = calculateBaseRatingMultiplier(expectedRating, BaseMultiplierMode.INTERPOLATE)
        val expMult = expBase * scaleFactor * 1.0
        val expGross = (totalCost.toDouble() * expMult).roundToLong().coerceAtLeast(0L)
        val expNet = (expGross.toDouble() * studioSharePercentage).roundToLong()

        // Best case: Blockbuster / Masterpiece breakout reception (score 95) + viral luck (1.18x variance)
        val bestTier = BoxOfficeRatingTier.MASTERPIECE
        val bestBase = bestTier.maxMultiplier
        val bestMult = bestBase * scaleFactor * DEFAULT_MAX_VARIANCE
        val bestGross = (totalCost.toDouble() * bestMult).roundToLong().coerceAtLeast(0L)
        val bestNet = (bestGross.toDouble() * studioSharePercentage).roundToLong()

        return BoxOfficeProjectionRange(
            totalInvestment = totalCost,
            productionBudget = productionBudget,
            marketingBudget = marketingBudget,
            worstCaseGross = worstGross,
            worstCaseStudioNet = worstNet,
            worstCaseProfit = worstNet - totalCost,
            expectedGross = expGross,
            expectedStudioNet = expNet,
            expectedProfit = expNet - totalCost,
            bestCaseGross = bestGross,
            bestCaseStudioNet = bestNet,
            bestCaseProfit = bestNet - totalCost,
            budgetCategory = budgetCategory,
            studioSharePercentage = studioSharePercentage
        )
    }
}
