package com.caesar.gametycoon.engine.fix

import com.example.data.BusinessUpgrade
import kotlin.math.min
import kotlin.math.pow

/**
 * Phase 2 Fix: UpgradeCostSafetyUtils
 *
 * Prevents numeric overflow and precision loss in upgrade cost calculations.
 *
 * ROOT CAUSE FIXED:
 * In legacy code:
 * `var costMultiplierTotal = 1.0f; repeat(currentLevel) { costMultiplierTotal *= upgrade.costMultiplier }`
 * At higher levels (Level 30+), Float repeated multiplication loses precision and quickly overflows
 * to `Float.POSITIVE_INFINITY`, which `.toLong()` converts to `Long.MAX_VALUE` ($9.22 Quintillion).
 *
 * CORRECTED SPECIFICATION:
 * - Uses 64-bit Double precision for exponentiation (`pow`).
 * - Employs a hard safety ceiling [MAX_SAFE_UPGRADE_COST] to prevent integer overflow.
 * - Detects `NaN` or `Infinity` and clamps gracefully.
 */
object UpgradeCostSafetyUtils {

    // $100 Trillion cap prevents exceeding maximum displayable game currencies
    const val MAX_SAFE_UPGRADE_COST: Long = 100_000_000_000_000L

    /**
     * Calculates the safe upgrade cost for a given level with overflow protection.
     */
    fun calculateSafeUpgradeCost(
        baseCost: Long,
        costMultiplier: Float,
        currentLevel: Int,
        maxCostCap: Long = MAX_SAFE_UPGRADE_COST
    ): Long {
        if (baseCost <= 0L || currentLevel <= 0) return baseCost.coerceAtLeast(0L)

        val multiplier = costMultiplier.toDouble().coerceIn(1.0, 10.0)
        val level = currentLevel.coerceIn(0, 1000)

        // Use Double.pow for exact and fast exponential calculation
        val totalMultiplier = multiplier.pow(level.toDouble())

        if (totalMultiplier.isInfinite() || totalMultiplier.isNaN()) {
            return maxCostCap
        }

        val rawCost = baseCost.toDouble() * totalMultiplier

        if (rawCost.isInfinite() || rawCost.isNaN() || rawCost > maxCostCap.toDouble()) {
            return maxCostCap
        }

        return rawCost.toLong().coerceIn(0L, maxCostCap)
    }

    /**
     * Overload accepting [BusinessUpgrade] model directly.
     */
    fun calculateSafeUpgradeCost(
        upgrade: BusinessUpgrade,
        currentLevel: Int,
        maxCostCap: Long = MAX_SAFE_UPGRADE_COST
    ): Long {
        return calculateSafeUpgradeCost(
            baseCost = upgrade.baseCost,
            costMultiplier = upgrade.costMultiplier,
            currentLevel = currentLevel,
            maxCostCap = maxCostCap
        )
    }

    /**
     * Calculates the cumulative cost to purchase multiple upgrade levels sequentially.
     */
    fun calculateCumulativeUpgradeCost(
        baseCost: Long,
        costMultiplier: Float,
        startLevel: Int,
        levelsToBuy: Int,
        maxCostCap: Long = MAX_SAFE_UPGRADE_COST
    ): Long {
        if (levelsToBuy <= 0) return 0L
        var accumulated = 0L

        for (lvl in startLevel until (startLevel + levelsToBuy)) {
            val stepCost = calculateSafeUpgradeCost(baseCost, costMultiplier, lvl, maxCostCap)
            accumulated = (accumulated + stepCost).coerceAtMost(maxCostCap)
            if (accumulated >= maxCostCap) break
        }

        return accumulated
    }

    /**
     * Calculates the maximum number of upgrade levels affordable with the available cash.
     */
    fun calculateMaxAffordableLevels(
        availableCash: Long,
        baseCost: Long,
        costMultiplier: Float,
        currentLevel: Int,
        maxLevelCap: Int = 100
    ): Int {
        if (availableCash <= 0L) return 0

        var remainingCash = availableCash
        var count = 0

        for (lvl in currentLevel until (currentLevel + maxLevelCap)) {
            val cost = calculateSafeUpgradeCost(baseCost, costMultiplier, lvl)
            if (remainingCash >= cost) {
                remainingCash -= cost
                count++
            } else {
                break
            }
        }

        return count
    }
}
