package com.example.filmstudio.engine

import com.example.data.OwnedBusiness
import com.example.filmstudio.model.FilmPitchProjection
import com.example.filmstudio.model.FilmProductionFocus

/**
 * Math and formula engine for the Film Studio / Movie Production system.
 * Encapsulates all budget multipliers, review score RNG with studio upgrades,
 * box office simulation, and financial projections.
 */
object FilmProductionMath {

    /**
     * Calculates the adjusted budget taking into account production focus multipliers.
     */
    fun calculateEffectiveBudget(rawBudget: Long, productionFocus: String): Long {
        val focus = FilmProductionFocus.fromId(productionFocus)
        return (rawBudget * focus.budgetMultiplier).toLong()
    }

    /**
     * Determines the risk level of a project based on focus and genre combinations.
     */
    fun calculateRiskLevel(productionFocus: String, genreCount: Int): String {
        return when {
            productionFocus == "MAHAKARYA" -> "Low" // Stabilized Risk
            productionFocus == "KUALITAS" -> "Medium"
            genreCount >= 4 -> "EXTREME"
            genreCount == 3 -> "High"
            genreCount == 2 -> "Medium"
            else -> "Low"
        }
    }

    /**
     * Simulates the review score based on production focus, studio level, genre count,
     * and studio infrastructure upgrades.
     */
    fun calculateReviewScore(
        productionFocus: String,
        studioLevel: Int,
        genreCount: Int,
        hasSoundstage: Boolean,
        hasPostPipeline: Boolean,
        hasVirtualProd: Boolean,
        hasMocapLab: Boolean
    ): Int {
        var reviewScore = when (productionFocus) {
            "KUALITAS" -> (65..100).random()
            "MAHAKARYA" -> (85..100).random()
            else -> {
                val lowerBound = minOf(10 + (studioLevel * 2), 70)
                var score = (lowerBound..100).random()

                // High volatility for 3+ genres
                if (genreCount >= 3) {
                    if ((0..1).random() == 0) {
                        score -= (10..30).random()
                    } else {
                        score += (10..20).random()
                    }
                    score = score.coerceIn(0, 100)
                }
                score
            }
        }

        // Apply infrastructure quality boosts
        if (hasSoundstage) reviewScore = minOf(100, reviewScore + 4)
        if (hasPostPipeline) reviewScore = minOf(100, reviewScore + 4)
        if (hasVirtualProd) reviewScore = minOf(100, reviewScore + 3)
        if (hasMocapLab) reviewScore = minOf(100, reviewScore + 5)

        return reviewScore.coerceIn(1, 100)
    }

    /**
     * Calculates the projected box office revenue of a film project using the BoxOfficeEngine.
     */
    fun calculateBoxOffice(
        budget: Long,
        promoBudget: Long,
        reviewScore: Int,
        isGlobal: Boolean,
        studioLevel: Int,
        studioType: String?,
        hasVfxStunt: Boolean,
        hasPhysicsSim: Boolean
    ): Long {
        val distBonus = if (isGlobal) 1.25 else 1.0
        val levelBonus = 1.0 + (studioLevel * 0.03)

        var techBonus = 1.0
        if (hasVfxStunt) techBonus *= 1.10
        if (hasPhysicsSim) techBonus *= 1.15

        val animBonus = if (studioType == "ANIMATION" && budget > 50_000_000L) 1.2 else 1.0
        val studioMultiplier = distBonus * levelBonus * techBonus * animBonus

        val financialResult = BoxOfficeEngine.calculateFinancials(
            productionBudget = budget,
            marketingBudget = promoBudget,
            rating = reviewScore,
            studioSharePercentage = BoxOfficeEngine.DEFAULT_STUDIO_SHARE_PERCENTAGE
        )

        return (financialResult.grossBoxOffice * studioMultiplier).toLong()
    }

    /**
     * Generates a pitch deck projection for UI estimation and analyst preview using BoxOfficeEngine.
     */
    fun generatePitchProjection(
        budget: Long,
        promoBudget: Long,
        isGlobal: Boolean,
        studioLevel: Int,
        productionFocus: String,
        genreCount: Int
    ): FilmPitchProjection {
        val expectedScore = when (productionFocus) {
            "MAHAKARYA" -> 90
            "KUALITAS" -> 80
            else -> 68
        }

        val projRange = BoxOfficeEngine.calculateProjectionRange(
            productionBudget = budget,
            marketingBudget = promoBudget,
            expectedRating = expectedScore
        )

        val distBonus = if (isGlobal) 1.25 else 1.0
        val levelBonus = 1.0 + (studioLevel * 0.03)
        val studioMult = distBonus * levelBonus

        val minProj = (projRange.worstCaseGross * studioMult).toLong()
        val maxProj = (projRange.bestCaseGross * studioMult).toLong()
        val risk = calculateRiskLevel(productionFocus, genreCount)

        return FilmPitchProjection(
            totalInvestment = projRange.totalInvestment,
            productionBudget = budget,
            promoBudget = promoBudget,
            minBoxOffice = minProj,
            maxBoxOffice = maxProj,
            riskLevel = risk
        )
    }

    /**
     * Alias for generatePitchProjection for UI consumption.
     */
    fun calculatePitchDeckProjection(
        budget: Long,
        promoBudget: Long,
        isGlobal: Boolean,
        studioLevel: Int,
        productionFocus: String,
        genreCount: Int
    ): FilmPitchProjection = generatePitchProjection(budget, promoBudget, isGlobal, studioLevel, productionFocus, genreCount)

    /**
     * Calculates the refund amount for a cancelled project.
     * In-theater cancellations yield 0 refund. In-production yields 30-50%.
     */
    fun calculateCancellationRefund(budget: Long, promoBudget: Long, isScreening: Boolean): Long {
        if (isScreening) return 0L
        val totalInvested = budget + promoBudget
        val refundPercentage = (30..50).random() / 100.0
        return (totalInvested * refundPercentage).toLong()
    }

    /**
     * Calculates cost and extra months required to polish a film in QC.
     */
    fun calculatePolishRequirements(budget: Long): Pair<Long, Int> {
        val pCost = (budget * (10..20).random() / 100).toLong()
        val eMonths = (2..12).random()
        return Pair(pCost, eMonths)
    }
}
