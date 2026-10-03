package com.caesar.gametycoon.engine.fix

import com.example.data.OwnedProperty
import com.example.data.PlayerState
import com.example.data.PrivateLedgerRecord

/**
 * Phase 2 Fix: RentalIncomeFixEngine
 *
 * Eliminates the Personal Rental Income Double-Dipping Bug.
 *
 * ROOT CAUSE FIXED:
 * In HospitalitySubsystemEngine, personal residential property rental yield was:
 * 1. Added to `PlayerState.privateBalance` via the state modifier.
 * 2. ALSO bundled into `SubsystemTickResult.revenue`, causing MonthlyTickRegistry to add it
 *    to `AggregatedFinancials.netIncome`, which GameViewModel subsequently deposited into
 *    Root Corporate Cash (`PlayerState.cash`).
 *
 * CORRECTED SPECIFICATION:
 * - Personal rental yields belong exclusively to Private Wealth ([PlayerState.privateBalance]).
 * - Corporate accounts ([PlayerState.cash] & Sub-Holdings) receive $0 from personal residential rent.
 * - Condition wear and rent calculations are isolated with clean ledger accounting.
 */
object RentalIncomeFixEngine {

    data class RentalProcessingResult(
        val updatedProperties: List<OwnedProperty>,
        val totalPersonalRentEarned: Long,
        val updatedState: PlayerState
    )

    /**
     * Calculates personal residential rental income with condition decay,
     * crediting ONLY the personal balance ([PlayerState.privateBalance])
     * and recording an entry in [PlayerState.privateLedgerHistory].
     */
    fun processPersonalRentalYield(
        currentState: PlayerState,
        decayMin: Int = 1,
        decayMax: Int = 3
    ): RentalProcessingResult {
        var totalRentYield = 0L

        val updatedProperties = currentState.ownedProperties.map { prop ->
            val conditionDecay = (decayMin..decayMax).random()
            val newCondition = (prop.condition - conditionDecay).coerceAtLeast(20)

            // Monthly yield is 0.5% of property value scaled by condition health
            val rentYield = (prop.currentEstimatedValue * 0.005 * (newCondition / 100.0)).toLong()
            totalRentYield += rentYield

            prop.copy(condition = newCondition)
        }

        val updatedPrivateBalance = (currentState.privateBalance + totalRentYield).coerceAtLeast(0L)

        // Record entry in private ledger history if rent was earned
        val updatedLedger = if (totalRentYield > 0L) {
            val record = PrivateLedgerRecord(
                monthTick = currentState.inGameMonth + (currentState.inGameYear - 2026) * 12,
                title = "Hasil Sewa Properti Pribadi (Rental Yield)",
                amount = totalRentYield,
                isIncome = true
            )
            (currentState.privateLedgerHistory + record).takeLast(100)
        } else {
            currentState.privateLedgerHistory
        }

        val finalState = currentState.copy(
            ownedProperties = updatedProperties,
            privateBalance = updatedPrivateBalance,
            privateLedgerHistory = updatedLedger
            // Note: `cash` (Corporate Cash) is intentionally untouched!
        )

        return RentalProcessingResult(
            updatedProperties = updatedProperties,
            totalPersonalRentEarned = totalRentYield,
            updatedState = finalState
        )
    }

    /**
     * Sanitizes subsystem tick revenue to ensure personal rental yields
     * never leak into corporate revenue calculations.
     */
    fun sanitizeCorporateRevenue(
        subsystemGrossRevenue: Long,
        personalRentalYieldIncluded: Long
    ): Long {
        return (subsystemGrossRevenue - personalRentalYieldIncluded).coerceAtLeast(0L)
    }
}
