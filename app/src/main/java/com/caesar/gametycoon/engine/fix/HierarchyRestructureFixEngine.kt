package com.caesar.gametycoon.engine.fix

import com.example.data.HoldingCompany
import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Phase 2 Fix: HierarchyRestructureFixEngine
 *
 * Resolves the "Orphan Business Unit" bug during corporate restructuring.
 *
 * ROOT CAUSE FIXED:
 * In legacy code:
 * 1. Setting `isTargetHolding == false` set `biz.parentId = targetId` inside `ownedBusinesses`
 *    without assigning it to a holding. CashFlowDistributionEngine saw `parentId != null`
 *    and skipped standalone cash flow, while no holding had it in `subsidiaries`.
 * 2. Moving a subsidiary between two holding companies failed because lookup only searched `ownedBusinesses`.
 *
 * CORRECTED SPECIFICATION:
 * - Standalone units strictly have `parentId = null` and reside in `ownedBusinesses`.
 * - Merged subsidiaries strictly reside in `holding.subsidiaries` with `parentId = holding.instanceId`.
 * - Moving between holdings or ejecting to root cleans up references and guarantees 100% active cash flow.
 */
object HierarchyRestructureFixEngine {

    data class RestructureResult(
        val updatedState: PlayerState,
        val isSuccess: Boolean,
        val message: String
    )

    /**
     * Moves a business unit into a holding company or ejects it to standalone root.
     */
    fun restructureBusiness(
        currentState: PlayerState,
        sourceInstanceId: String,
        targetHoldingId: String?,
        isTargetHolding: Boolean
    ): RestructureResult {
        if (!isTargetHolding || targetHoldingId.isNullOrBlank()) {
            return ejectToStandalone(currentState, sourceInstanceId)
        } else {
            return moveToHolding(currentState, sourceInstanceId, targetHoldingId)
        }
    }

    /**
     * Ejects a subsidiary out of its holding company into a Standalone Mega Holding Unit.
     * Cleans up `parentId` so [com.example.corporate.engine.CashFlowDistributionEngine]
     * immediately resumes 2-tier standalone cash flow.
     */
    fun ejectToStandalone(
        currentState: PlayerState,
        sourceInstanceId: String
    ): RestructureResult {
        var extractedBusiness: OwnedBusiness? = null

        // 1. Remove from any holding company subsidiaries
        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            val found = holding.subsidiaries.find { it.instanceId == sourceInstanceId }
            if (found != null) {
                extractedBusiness = found.copy(parentId = null)
                holding.copy(subsidiaries = holding.subsidiaries.filter { it.instanceId != sourceInstanceId })
            } else {
                holding
            }
        }

        // 2. Also check if it was already in ownedBusinesses with an orphaned parentId
        val existingInOwned = currentState.ownedBusinesses.find { it.instanceId == sourceInstanceId }
        val updatedOwned = if (existingInOwned != null) {
            currentState.ownedBusinesses.map { biz ->
                if (biz.instanceId == sourceInstanceId) biz.copy(parentId = null) else biz
            }
        } else if (extractedBusiness != null) {
            currentState.ownedBusinesses + extractedBusiness!!
        } else {
            return RestructureResult(currentState, false, "Business unit $sourceInstanceId not found in portfolio.")
        }

        val cleanedState = currentState.copy(
            ownedBusinesses = updatedOwned,
            holdingCompanies = updatedHoldings
        )

        return RestructureResult(
            updatedState = sanitizeHierarchy(cleanedState),
            isSuccess = true,
            message = "Unit successfully restructured to Standalone (Root Mega Holding)."
        )
    }

    /**
     * Merges a business into a target holding company (from standalone or from another holding).
     */
    fun moveToHolding(
        currentState: PlayerState,
        sourceInstanceId: String,
        targetHoldingId: String
    ): RestructureResult {
        val targetHolding = currentState.holdingCompanies.find { it.instanceId == targetHoldingId }
            ?: return RestructureResult(currentState, false, "Target Holding Company not found.")

        var targetBusiness: OwnedBusiness? = null

        // Look in standalone list
        val fromStandalone = currentState.ownedBusinesses.find { it.instanceId == sourceInstanceId }
        val newOwnedBusinesses = if (fromStandalone != null) {
            targetBusiness = fromStandalone.copy(parentId = targetHoldingId)
            currentState.ownedBusinesses.filter { it.instanceId != sourceInstanceId }
        } else {
            currentState.ownedBusinesses
        }

        // Look in other holdings if not in standalone
        val updatedHoldingsWithoutSource = currentState.holdingCompanies.map { holding ->
            val match = holding.subsidiaries.find { it.instanceId == sourceInstanceId }
            if (match != null) {
                targetBusiness = match.copy(parentId = targetHoldingId)
                holding.copy(subsidiaries = holding.subsidiaries.filter { it.instanceId != sourceInstanceId })
            } else {
                holding
            }
        }

        if (targetBusiness == null) {
            return RestructureResult(currentState, false, "Source business $sourceInstanceId not found.")
        }

        // Add to destination holding
        val finalHoldings = updatedHoldingsWithoutSource.map { holding ->
            if (holding.instanceId == targetHoldingId) {
                holding.copy(subsidiaries = holding.subsidiaries + targetBusiness!!)
            } else {
                holding
            }
        }

        val resultState = currentState.copy(
            ownedBusinesses = newOwnedBusinesses,
            holdingCompanies = finalHoldings
        )

        return RestructureResult(
            updatedState = sanitizeHierarchy(resultState),
            isSuccess = true,
            message = "Unit successfully merged into ${targetHolding.name}."
        )
    }

    /**
     * Sanitizes all corporate entities to guarantee no orphan states exist:
     * - Units in `ownedBusinesses` MUST have `parentId = null`.
     * - Units in `holding.subsidiaries` MUST have `parentId = holding.instanceId`.
     * - Removes any duplicate instances across tiers.
     */
    fun sanitizeHierarchy(state: PlayerState): PlayerState {
        val subsidiaryIds = mutableSetOf<String>()

        val sanitizedHoldings = state.holdingCompanies.map { holding ->
            val cleanSubs = holding.subsidiaries.map { sub ->
                subsidiaryIds.add(sub.instanceId)
                sub.copy(parentId = holding.instanceId)
            }
            holding.copy(subsidiaries = cleanSubs)
        }

        val sanitizedOwned = state.ownedBusinesses
            .filter { !subsidiaryIds.contains(it.instanceId) }
            .map { it.copy(parentId = null) }

        return state.copy(
            ownedBusinesses = sanitizedOwned,
            holdingCompanies = sanitizedHoldings
        )
    }
}
