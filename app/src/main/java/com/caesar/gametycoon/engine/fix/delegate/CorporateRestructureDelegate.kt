package com.caesar.gametycoon.engine.fix.delegate

import com.caesar.gametycoon.engine.fix.HierarchyRestructureFixEngine
import com.example.data.CorporateFinanceManager
import com.example.data.PlayerState

/**
 * Phase 3 Refactor: CorporateRestructureDelegate
 *
 * Extracts corporate M&A hierarchy operations, subsidiary reassignment,
 * and IPO divestment logic from GameViewModel.
 */
object CorporateRestructureDelegate {

    data class IpoResult(
        val updatedState: PlayerState,
        val isSuccess: Boolean,
        val cashGained: Long,
        val message: String
    )

    /**
     * Delegates business restructuring to [HierarchyRestructureFixEngine].
     */
    fun restructureBusiness(
        currentState: PlayerState,
        sourceInstanceId: String,
        targetId: String?,
        isTargetHolding: Boolean
    ): HierarchyRestructureFixEngine.RestructureResult {
        return HierarchyRestructureFixEngine.restructureBusiness(
            currentState = currentState,
            sourceInstanceId = sourceInstanceId,
            targetHoldingId = targetId,
            isTargetHolding = isTargetHolding
        )
    }

    /**
     * Ejects a subsidiary out of any holding company into the root standalone list.
     */
    fun ejectBusinessToRoot(
        currentState: PlayerState,
        sourceInstanceId: String
    ): HierarchyRestructureFixEngine.RestructureResult {
        return HierarchyRestructureFixEngine.ejectToStandalone(
            currentState = currentState,
            sourceInstanceId = sourceInstanceId
        )
    }

    /**
     * Conducts an Initial Public Offering (IPO) for a holding company with validation.
     */
    fun processIPO(
        currentState: PlayerState,
        holdingId: String,
        percentToSell: Float
    ): IpoResult {
        val holding = currentState.holdingCompanies.find { it.instanceId == holdingId }
            ?: return IpoResult(currentState, false, 0L, "Holding company tidak ditemukan.")

        if (holding.isPublic) {
            return IpoResult(currentState, false, 0L, "Holding company sudah go-public sebelumnya.")
        }

        if (percentToSell <= 0f || percentToSell > holding.ownershipPercentage) {
            return IpoResult(currentState, false, 0L, "Persentase saham IPO tidak valid.")
        }

        val (updatedHolding, cashGained) = CorporateFinanceManager.processIPO(holding, percentToSell, currentState)

        val updatedHoldings = currentState.holdingCompanies.map {
            if (it.instanceId == holdingId) updatedHolding else it
        }

        val updatedState = currentState.copy(
            holdingCompanies = updatedHoldings,
            cash = currentState.cash + cashGained
        )

        return IpoResult(
            updatedState = updatedState,
            isSuccess = true,
            cashGained = cashGained,
            message = "IPO ${holding.name} berhasil! Kas bertambah USD ${String.format("%,d", cashGained)}."
        )
    }
}
