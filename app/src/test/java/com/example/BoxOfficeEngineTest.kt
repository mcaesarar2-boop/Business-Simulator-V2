package com.example

import com.example.filmstudio.engine.BoxOfficeEngine
import com.example.filmstudio.model.BoxOfficeRatingTier
import com.example.filmstudio.model.BudgetScaleCategory
import org.junit.Assert.*
import org.junit.Test

class BoxOfficeEngineTest {

    @Test
    fun testScenarioA_BigBudgetBlockbuster() {
        // Scenario A: Big Budget Movie ($70M Production + $30M Marketing = $100M Total)
        // Rating: 85/100 (Blockbuster -> Base: 4.5x)
        // Budget Scale Factor: 0.8x (Big Budget)
        // Random Variance: 1.0x (Neutral)
        // Final Multiplier: 4.5 * 0.8 * 1.0 = 3.6x
        // Gross Box Office: $100M * 3.6 = $360,000,000
        // Studio Net Share (48%): $172,800,000
        // Studio Net Profit: $172,800,000 - $100,000,000 = +$72,800,000 Net Profit
        val result = BoxOfficeEngine.calculateFinancials(
            productionBudget = 70_000_000L,
            marketingBudget = 30_000_000L,
            rating = 85,
            studioSharePercentage = 0.48,
            forcedBaseMultiplier = 4.5,
            forcedScaleFactor = 0.8,
            forcedVariance = 1.0
        )

        assertEquals(100_000_000L, result.totalCost)
        assertEquals(BoxOfficeRatingTier.BLOCKBUSTER, result.ratingTier)
        assertEquals(BudgetScaleCategory.BIG_BUDGET, result.budgetCategory)
        assertEquals(0.8, result.budgetScaleFactor, 0.001)
        assertEquals(3.6, result.finalMultiplier, 0.001)
        assertEquals(360_000_000L, result.grossBoxOffice)
        assertEquals(172_800_000L, result.studioNetRevenue)
        assertEquals(72_800_000L, result.netProfit)
        assertEquals(72.8, result.returnOnInvestmentPercent, 0.01)
        assertTrue(result.isProfitable)
    }

    @Test
    fun testScenarioB_IndieMasterpieceBreakout() {
        // Scenario B: Indie Movie ($3M Production + $1M Marketing = $4M Total)
        // Rating: 94/100 (Masterpiece -> Base: 7.0x)
        // Budget Scale Factor: 2.2x (Indie)
        // Random Variance: 1.1x (Viral Word-of-Mouth)
        // Final Multiplier: 7.0 * 2.2 * 1.1 = 16.94x
        // Gross Box Office: $4M * 16.94 = $67,760,000
        // Studio Net Share (48%): $32,524,800
        // Studio Net Profit: $32,524,800 - $4,000,000 = +$28,524,800 Net Profit (ROI: +713.12%)
        val result = BoxOfficeEngine.calculateFinancials(
            productionBudget = 3_000_000L,
            marketingBudget = 1_000_000L,
            rating = 94,
            studioSharePercentage = 0.48,
            forcedBaseMultiplier = 7.0,
            forcedScaleFactor = 2.2,
            forcedVariance = 1.1
        )

        assertEquals(4_000_000L, result.totalCost)
        assertEquals(BoxOfficeRatingTier.MASTERPIECE, result.ratingTier)
        assertEquals(BudgetScaleCategory.MICRO_INDIE, result.budgetCategory)
        assertEquals(2.2, result.budgetScaleFactor, 0.001)
        assertEquals(16.94, result.finalMultiplier, 0.001)
        assertEquals(67_760_000L, result.grossBoxOffice)
        assertEquals(32_524_800L, result.studioNetRevenue)
        assertEquals(28_524_800L, result.netProfit)
        assertEquals(713.12, result.returnOnInvestmentPercent, 0.01)
        assertTrue(result.isProfitable)
    }

    @Test
    fun testBudgetScaleCategories() {
        assertEquals(BudgetScaleCategory.MICRO_INDIE, BoxOfficeEngine.getBudgetCategory(1_000_000L))
        assertEquals(2.2, BoxOfficeEngine.getBudgetScaleFactor(4_999_999L), 0.001)

        assertEquals(BudgetScaleCategory.MID_BUDGET, BoxOfficeEngine.getBudgetCategory(15_000_000L))
        assertEquals(1.4, BoxOfficeEngine.getBudgetScaleFactor(25_000_000L), 0.001)

        assertEquals(BudgetScaleCategory.STANDARD_STUDIO, BoxOfficeEngine.getBudgetCategory(50_000_000L))
        assertEquals(1.0, BoxOfficeEngine.getBudgetScaleFactor(75_000_000L), 0.001)

        assertEquals(BudgetScaleCategory.BIG_BUDGET, BoxOfficeEngine.getBudgetCategory(100_000_000L))
        assertEquals(0.8, BoxOfficeEngine.getBudgetScaleFactor(150_000_000L), 0.001)

        assertEquals(BudgetScaleCategory.MEGA_TENTPOLE, BoxOfficeEngine.getBudgetCategory(250_000_000L))
        assertEquals(0.65, BoxOfficeEngine.getBudgetScaleFactor(200_000_000L), 0.001)
    }

    @Test
    fun testRatingTiers() {
        assertEquals(BoxOfficeRatingTier.DISASTER_FLOP, BoxOfficeEngine.getRatingTier(25))
        assertEquals(BoxOfficeRatingTier.UNDERPERFORMER, BoxOfficeEngine.getRatingTier(45))
        assertEquals(BoxOfficeRatingTier.MEDIOCRE, BoxOfficeEngine.getRatingTier(60))
        assertEquals(BoxOfficeRatingTier.COMMERCIAL_HIT, BoxOfficeEngine.getRatingTier(75))
        assertEquals(BoxOfficeRatingTier.BLOCKBUSTER, BoxOfficeEngine.getRatingTier(88))
        assertEquals(BoxOfficeRatingTier.MASTERPIECE, BoxOfficeEngine.getRatingTier(98))
    }

    @Test
    fun testRandomVarianceBounds() {
        for (i in 0 until 100) {
            val variance = BoxOfficeEngine.generateRandomVariance()
            assertTrue("Variance $variance must be >= 0.82", variance >= 0.82)
            assertTrue("Variance $variance must be <= 1.18", variance <= 1.18)
        }
    }

    @Test
    fun testDisasterFlopLoss() {
        // A $50M movie with rating 20 (Disaster Flop)
        val result = BoxOfficeEngine.calculateFinancials(
            productionBudget = 40_000_000L,
            marketingBudget = 10_000_000L,
            rating = 20,
            studioSharePercentage = 0.48,
            forcedVariance = 1.0
        )
        assertTrue(result.netProfit < 0)
        assertFalse(result.isProfitable)
    }
}
