package com.example.publisher.engine

import com.example.publisher.model.CreativeAdjustmentType
import com.example.publisher.model.FundingTier
import com.example.publisher.model.GameGenre
import com.example.publisher.model.GameProject
import com.example.publisher.model.TargetPlatform
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Result data holder for platform matrix calculations.
 */
data class PlatformComplexityResult(
    val costMultiplier: Double,
    val timeMultiplier: Double,
    val combinedReachMultiplier: Double,
    val averageStoreCut: Double
)

/**
 * Financial breakdown generated when a game launches.
 */
data class LaunchFinancialResult(
    val baseHype: Double,
    val reviewScore: Int,
    val reviewScoreMultiplier: Double,
    val platformMultiplier: Double,
    val grossRevenue: Long,
    val storeCutAmount: Long,
    val finalRevenue: Long,
    val publisherProfit: Long,
    val unitsSold: Long,
    val initialActivePlayers: Long,
    val reviewSummaryText: String
) {
    val platformFeeDeduction: Long get() = storeCutAmount
    val studioPayout: Long get() = (grossRevenue - storeCutAmount - publisherProfit).coerceAtLeast(0L)
}

/**
 * Dedicated Engine handling simulation math, platform matrix algorithms,
 * porting bug risk curves, and distribution revenue calculations.
 */
object GamePublisherEngine {

    fun computePlatformDistributionMetrics(platforms: Set<TargetPlatform>): PlatformComplexityResult {
        return calculatePlatformMultipliers(platforms)
    }

    // =========================================================================
    // 1. PLATFORM MATRIX & PORTING COMPLEXITY ENGINE
    // =========================================================================

    /**
     * Calculates the cost, time, reach, and store cut multipliers based on the platform selection.
     */
    fun calculatePlatformMultipliers(platforms: Set<TargetPlatform>): PlatformComplexityResult {
        if (platforms.isEmpty()) {
            return PlatformComplexityResult(
                costMultiplier = 1.0,
                timeMultiplier = 1.0,
                combinedReachMultiplier = 1.0,
                averageStoreCut = 0.30
            )
        }

        val hasPc = platforms.contains(TargetPlatform.PC_STEAM) || platforms.contains(TargetPlatform.PC_EPIC)
        val consoleCount = platforms.count { it.isConsole }

        val (costMultiplier, timeMultiplier) = when {
            // PC only
            hasPc && consoleCount == 0 -> {
                val pcStoreCount = platforms.size
                if (pcStoreCount > 1) Pair(1.10, 1.05) else Pair(1.0, 1.0)
            }
            // Cross-Platform (PC + Multiple Consoles)
            hasPc && consoleCount >= 2 -> {
                val extraConsoleOverhead = (consoleCount - 2) * 0.25
                Pair(2.50 + extraConsoleOverhead, 1.80 + (consoleCount - 2) * 0.10)
            }
            // Cross-Platform (PC + Single Console)
            hasPc && consoleCount == 1 -> {
                Pair(1.65, 1.45)
            }
            // Multi-Console only (no PC)
            !hasPc && consoleCount > 1 -> {
                Pair(1.80 + (consoleCount - 2) * 0.30, 1.50)
            }
            // Single Console only
            !hasPc && consoleCount == 1 -> {
                val console = platforms.first()
                Pair(1.0 + console.costMultiplier, 1.0 + console.timeMultiplier)
            }
            else -> Pair(1.0, 1.0)
        }

        val reachSum = platforms.sumOf { it.organicReachMultiplier }
        val avgStoreCut = if (platforms.isNotEmpty()) {
            platforms.sumOf { it.storeCut } / platforms.size
        } else {
            0.30
        }

        return PlatformComplexityResult(
            costMultiplier = costMultiplier,
            timeMultiplier = timeMultiplier,
            combinedReachMultiplier = (reachSum * 0.9).coerceAtLeast(0.8),
            averageStoreCut = avgStoreCut
        )
    }

    // =========================================================================
    // 2. PORTING BUG RISK ALGORITHM
    // =========================================================================

    fun calculatePortingBugRisk(
        platforms: Set<TargetPlatform>,
        baseBudget: Long,
        qaInvestment: Long,
        currentBugRisk: Double
    ): Double {
        val platformCount = platforms.size
        if (platformCount <= 1 && platforms.all { !it.isConsole }) {
            val qaMitigationRatio = (qaInvestment.toDouble() / max(10_000.0, baseBudget * 0.08)).coerceIn(0.0, 1.0)
            return (currentBugRisk * (1.0 - (qaMitigationRatio * 0.5))).coerceIn(0.0, 100.0)
        }

        val consoleCount = platforms.count { it.isConsole }
        val complexityExponent = 1.0 + (consoleCount * 0.45) + (if (platforms.size > 2) 0.3 else 0.0)
        val requiredQaBudget = (baseBudget * 0.12 * complexityExponent).toLong()

        val qaFundingRatio = if (requiredQaBudget > 0) {
            (qaInvestment.toDouble() / requiredQaBudget.toDouble()).coerceIn(0.0, 1.5)
        } else 1.0

        val baseRiskIncrease = when {
            consoleCount >= 3 -> 45.0
            consoleCount == 2 -> 30.0
            consoleCount == 1 -> 18.0
            else -> 8.0
        }

        val mitigatedRisk = baseRiskIncrease * (1.0 - (qaFundingRatio * 0.85))
        return (currentBugRisk * 0.4 + mitigatedRisk * 0.6).coerceIn(0.0, 100.0)
    }

    // =========================================================================
    // 3. DISTRIBUTION & LAUNCH FINANCIALS
    // =========================================================================

    fun calculateLaunchFinancials(
        project: GameProject,
        studioTalentScore: Double,
        marketSaturation: Double = 15.0
    ): LaunchFinancialResult {
        val platformMetrics = calculatePlatformMultipliers(project.targetPlatforms)

        val tierWeight = when (project.fundingTier) {
            FundingTier.BOOTSTRAP -> 1.0
            FundingTier.STANDARD -> 2.8
            FundingTier.TRIPLE_I -> 8.5
        }

        val effectiveTalent = (studioTalentScore * project.fundingTier.talentThresholdBonus).coerceIn(10.0, 100.0)
        val baseHype = max(10.0, ((project.totalBudget / 10_000.0) * (effectiveTalent / 50.0)) - marketSaturation + (project.hypeScore * 1.5))

        val baseReview = (effectiveTalent * 0.75) + Random.nextDouble(5.0, 15.0)
        val bugPenalty = (project.bugRiskScore * 0.45).coerceAtMost(40.0)
        val finalReviewScore = (baseReview - bugPenalty).toInt().coerceIn(35, 99)

        val reviewMultiplier = when {
            finalReviewScore >= 90 -> 3.20
            finalReviewScore >= 80 -> 2.00
            finalReviewScore >= 70 -> 1.35
            finalReviewScore >= 60 -> 0.90
            finalReviewScore >= 50 -> 0.55
            else -> 0.25
        }

        val baseUnits = (baseHype * 120.0 * tierWeight).toLong()
        val totalUnitsSold = max(500L, (baseUnits * platformMetrics.combinedReachMultiplier * reviewMultiplier).toLong())

        val averagePricePerUnit = when (project.fundingTier) {
            FundingTier.BOOTSTRAP -> 14.99
            FundingTier.STANDARD -> 24.99
            FundingTier.TRIPLE_I -> 39.99
        }

        val grossRevenue = (totalUnitsSold * averagePricePerUnit).toLong()
        val storeCutAmount = (grossRevenue * platformMetrics.averageStoreCut).toLong()
        val netFinalRevenue = max(0L, grossRevenue - storeCutAmount)
        val publisherProfit = (netFinalRevenue * project.publisherRevenueShare).toLong()

        val initialActivePlayers = (totalUnitsSold * 0.28).toLong()

        val reviewSummary = when {
            finalReviewScore >= 85 -> "Universal Acclaim: Critics praise the polished mechanics and artistic mastery!"
            finalReviewScore >= 70 -> "Generally Favorable: Solid gameplay that resonates well with the community."
            finalReviewScore >= 50 -> "Mixed or Average: Fun core concept hindered by occasional bugs or uneven pacing."
            else -> "Overwhelmingly Negative: Plagued by game-breaking porting bugs and performance stutter."
        }

        return LaunchFinancialResult(
            baseHype = baseHype,
            reviewScore = finalReviewScore,
            reviewScoreMultiplier = reviewMultiplier,
            platformMultiplier = platformMetrics.combinedReachMultiplier,
            grossRevenue = grossRevenue,
            storeCutAmount = storeCutAmount,
            finalRevenue = netFinalRevenue,
            publisherProfit = publisherProfit,
            unitsSold = totalUnitsSold,
            initialActivePlayers = initialActivePlayers,
            reviewSummaryText = reviewSummary
        )
    }

    // =========================================================================
    // 4. CREATIVE ADJUSTMENT INJECTION
    // =========================================================================

    fun applyCreativeAdjustment(
        project: GameProject,
        adjustment: CreativeAdjustmentType
    ): GameProject {
        val newBudget = project.totalBudget + adjustment.costBonus
        val newDevTime = max(2, project.devTimeMonths + adjustment.devTimeBonusMonths)
        val newHype = (project.hypeScore + adjustment.hypeBoost).coerceIn(0.0, 100.0)
        val newBugRisk = (project.bugRiskScore + adjustment.bugRiskDelta).coerceIn(0.0, 100.0)

        return project.copy(
            totalBudget = newBudget,
            devTimeMonths = newDevTime,
            hypeScore = newHype,
            bugRiskScore = newBugRisk,
            creativeAdjustmentsCount = project.creativeAdjustmentsCount + 1
        )
    }

    // =========================================================================
    // 5. MONTHLY DEVELOPMENT PROGRESSION & SIMULATION (GAME LOOP SYNC)
    // =========================================================================

    /**
     * Advances development by 1 Month (synced with game's core monthly tick).
     */
    fun simulateMonthlyDevelopment(project: GameProject, talentScore: Double): GameProject {
        if (project.isReleased || project.currentProgress >= 100.0f) {
            return project
        }

        val newCurrentMonth = project.currentMonthInDev + 1
        val platformMetrics = calculatePlatformMultipliers(project.targetPlatforms)

        // Monthly progress rate
        val baseMonthlyStep = (100.0f / max(1, project.devTimeMonths).toFloat())
        val talentFactor = (talentScore.toFloat() / 75.0f).coerceIn(0.75f, 1.4f)
        val timePenalty = platformMetrics.timeMultiplier.toFloat()

        val progressGained = (baseMonthlyStep * talentFactor) / timePenalty
        val newProgress = min(100.0f, project.currentProgress + progressGained)

        val updatedBugRisk = calculatePortingBugRisk(
            platforms = project.targetPlatforms,
            baseBudget = project.totalBudget,
            qaInvestment = project.qaInvestment,
            currentBugRisk = project.bugRiskScore
        )

        return project.copy(
            currentMonthInDev = newCurrentMonth,
            currentProgress = newProgress,
            bugRiskScore = updatedBugRisk
        )
    }

    /**
     * Monthly simulation loop for released titles (back catalog sales & royalties).
     */
    fun simulateMonthlyReleasedSales(project: GameProject): GameProject {
        if (!project.isReleased) return project

        val decayRate = if (project.reviewScore >= 85) 0.88 else 0.75
        val monthlyGross = max(800L, (project.monthlyRevenue * decayRate).toLong())
        val monthlyNet = (monthlyGross * (1.0 - 0.30)).toLong()
        val publisherMonthlyProfit = (monthlyNet * project.publisherRevenueShare).toLong()

        val unitPrice = when (project.fundingTier) {
            FundingTier.BOOTSTRAP -> 14.99
            FundingTier.STANDARD -> 24.99
            FundingTier.TRIPLE_I -> 39.99
        }
        val newUnitsSold = project.totalSales + max(20L, (monthlyGross / unitPrice).toLong())
        val newActivePlayers = max(50L, (project.activePlayers * decayRate).toLong())

        return project.copy(
            monthlyRevenue = monthlyGross,
            lifetimeRevenue = project.lifetimeRevenue + monthlyNet,
            totalSales = newUnitsSold,
            activePlayers = newActivePlayers,
            publisherLifetimeProfit = project.publisherLifetimeProfit + publisherMonthlyProfit
        )
    }

    fun deployPostLaunchPatch(project: GameProject, patchCost: Long): GameProject {
        if (!project.isReleased) return project

        val scoreBoost = if (project.reviewScore < 95) 2 else 0
        val playerBoost = (project.activePlayers * 0.45).toLong() + 800L
        val revenueSpike = max(25_000L, project.monthlyRevenue * 2)

        return project.copy(
            patchCount = project.patchCount + 1,
            reviewScore = min(99, project.reviewScore + scoreBoost),
            activePlayers = project.activePlayers + playerBoost,
            monthlyRevenue = revenueSpike,
            bugRiskScore = max(1.0, project.bugRiskScore - 20.0),
            totalBudget = project.totalBudget + patchCost
        )
    }

    /**
     * Creates an in-house project developed directly by the player's publishing studio.
     */
    fun createInHouseProject(
        title: String,
        genre: GameGenre,
        fundingTier: FundingTier,
        targetPlatforms: Set<TargetPlatform>,
        publisherStudioName: String = "Vanguard Internal Studios"
    ): GameProject {
        val platformMetrics = calculatePlatformMultipliers(targetPlatforms)
        val adjustedBudget = (fundingTier.baseBudget * platformMetrics.costMultiplier).toLong()
        val adjustedMonths = max(2, (fundingTier.baseDevMonths * platformMetrics.timeMultiplier).toInt())

        return GameProject(
            title = title,
            genre = genre,
            studioId = "in_house_studio",
            studioName = publisherStudioName,
            fundingTier = fundingTier,
            targetPlatforms = targetPlatforms,
            isInHouseProject = true,
            currentProgress = 0.0f,
            totalBudget = adjustedBudget,
            devTimeMonths = adjustedMonths,
            currentMonthInDev = 0,
            bugRiskScore = 5.0,
            hypeScore = 30.0,
            publisherRevenueShare = 1.0 // 100% profit kept in-house
        )
    }
}
