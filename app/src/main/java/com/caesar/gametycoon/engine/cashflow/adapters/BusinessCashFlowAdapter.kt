package com.caesar.gametycoon.engine.cashflow.adapters

import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Resolved financial metrics for a single business unit during a monthly cycle.
 */
data class UnitFinancialResolution(
    val grossRevenue: Long,
    val expenses: Long,
    val updatedBusiness: OwnedBusiness
) {
    val netIncome: Long get() = grossRevenue - expenses
}

/**
 * Modular adapter interface for calculating business-unit-specific
 * revenues, operating expenses, and internal business state.
 *
 * Strict constraint: Implementations must remain compact, decoupled,
 * and under 300 lines of code.
 */
interface BusinessCashFlowAdapter {

    /**
     * Determines whether this adapter can process the given business unit.
     */
    fun canHandle(business: OwnedBusiness): Boolean

    /**
     * Resolves the gross revenue, operational expenses, and any updated business state.
     */
    fun resolveFinancials(business: OwnedBusiness, state: PlayerState): UnitFinancialResolution
}
