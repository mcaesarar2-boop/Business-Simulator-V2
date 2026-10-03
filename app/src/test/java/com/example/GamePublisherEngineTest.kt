package com.example

import com.example.publisher.data.GamePublisherRepository
import com.example.publisher.engine.GamePublisherEngine
import com.example.publisher.model.CreativeAdjustmentType
import com.example.publisher.model.FundingTier
import com.example.publisher.model.GameGenre
import com.example.publisher.model.GameProject
import com.example.publisher.model.TargetPlatform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePublisherEngineTest {

    @Test
    fun testPlatformMatrix_pcOnly_hasBaselineMultiplier() {
        val pcSteam = setOf(TargetPlatform.PC_STEAM)
        val result = GamePublisherEngine.calculatePlatformMultipliers(pcSteam)

        assertEquals(1.0, result.costMultiplier, 0.01)
        assertEquals(1.0, result.timeMultiplier, 0.01)
        assertTrue("Reach multiplier should be positive", result.combinedReachMultiplier >= 1.0)
        assertEquals(0.30, result.averageStoreCut, 0.01)
    }

    @Test
    fun testPlatformMatrix_crossPlatform_scalesSignificantly() {
        val crossPlatform = setOf(
            TargetPlatform.PC_STEAM,
            TargetPlatform.PLAYSTATION_5,
            TargetPlatform.NINTENDO_SWITCH
        )
        val result = GamePublisherEngine.calculatePlatformMultipliers(crossPlatform)

        // Cross-platform PC + 2 consoles should be >= 2.5x cost and >= 1.8x time
        assertTrue("Cost multiplier should be >= 2.5x", result.costMultiplier >= 2.50)
        assertTrue("Time multiplier should be >= 1.8x", result.timeMultiplier >= 1.80)
        assertTrue("Reach should combine platforms", result.combinedReachMultiplier > 3.0)
    }

    @Test
    fun testPortingBugRisk_increasesWithoutAdequateQa() {
        val multiPlatforms = setOf(
            TargetPlatform.PC_STEAM,
            TargetPlatform.PLAYSTATION_5,
            TargetPlatform.XBOX_SERIES_X,
            TargetPlatform.NINTENDO_SWITCH
        )
        val budget = 500_000L

        val highRisk = GamePublisherEngine.calculatePortingBugRisk(
            platforms = multiPlatforms,
            baseBudget = budget,
            qaInvestment = 0L,
            currentBugRisk = 10.0
        )

        val mitigatedRisk = GamePublisherEngine.calculatePortingBugRisk(
            platforms = multiPlatforms,
            baseBudget = budget,
            qaInvestment = 150_000L,
            currentBugRisk = 10.0
        )

        assertTrue("Unfunded multi-platform QA must have higher risk", highRisk > mitigatedRisk)
        assertTrue("High risk should escalate significantly", highRisk >= 30.0)
    }

    @Test
    fun testLaunchFinancials_highBugRiskTanksReviewScore() {
        val cleanProject = GameProject(
            title = "Clean Game",
            genre = GameGenre.ROGUELIKE,
            studioId = "s1",
            studioName = "Good Studio",
            fundingTier = FundingTier.STANDARD,
            targetPlatforms = setOf(TargetPlatform.PC_STEAM),
            totalBudget = 250_000L,
            bugRiskScore = 0.0,
            hypeScore = 50.0
        )

        val buggyProject = cleanProject.copy(
            title = "Buggy Port",
            bugRiskScore = 80.0
        )

        val cleanResult = GamePublisherEngine.calculateLaunchFinancials(cleanProject, studioTalentScore = 85.0)
        val buggyResult = GamePublisherEngine.calculateLaunchFinancials(buggyProject, studioTalentScore = 85.0)

        assertTrue(
            "Clean project review score (${cleanResult.reviewScore}) should be higher than buggy (${buggyResult.reviewScore})",
            cleanResult.reviewScore > buggyResult.reviewScore
        )
        assertTrue(
            "Clean project profit (${cleanResult.publisherProfit}) should be higher than buggy (${buggyResult.publisherProfit})",
            cleanResult.publisherProfit > buggyResult.publisherProfit
        )
    }

    @Test
    fun testCreativeAdjustments_featureCreepIncreasesHypeAndDevTime() {
        val initial = GameProject(
            title = "Project Chrono",
            genre = GameGenre.METROIDVANIA,
            studioId = "s2",
            studioName = "Pixel Team",
            fundingTier = FundingTier.STANDARD,
            devTimeMonths = 10,
            hypeScore = 20.0,
            totalBudget = 250_000L
        )

        val adjusted = GamePublisherEngine.applyCreativeAdjustment(initial, CreativeAdjustmentType.FEATURE_CREEP)

        assertTrue(adjusted.devTimeWeeks > initial.devTimeWeeks)
        assertTrue(adjusted.totalBudget > initial.totalBudget)
        assertTrue(adjusted.hypeScore > initial.hypeScore)
        assertEquals(1, adjusted.creativeAdjustmentsCount)
    }

    @Test
    fun testRepository_signPitchAndLaunchSimulation() {
        val repo = GamePublisherRepository()

        val initialPitches = repo.pitches.value
        assertTrue("Should have initial pitches", initialPitches.isNotEmpty())

        val pitchToSign = initialPitches.first()
        val initialActiveCount = repo.activeProjects.value.size

        val signed = repo.acceptPitch(
            studioId = pitchToSign.id,
            chosenTier = FundingTier.BOOTSTRAP,
            agreedRevenueShare = 0.60,
            platforms = setOf(TargetPlatform.PC_STEAM)
        )

        assertTrue("Signing pitch should succeed", signed)
        assertEquals(initialActiveCount + 1, repo.activeProjects.value.size)

        // Advance month
        repo.advanceMonth()
        assertEquals(13, repo.currentSimulationMonth.value)

        // Launch an active project
        val projectToLaunch = repo.activeProjects.value.first()
        val launchResult = repo.launchGame(projectToLaunch.id)

        assertNotNull(launchResult)
        assertTrue(launchResult!!.reviewScore in 35..99)
        assertTrue(launchResult.publisherProfit > 0)
    }
}
