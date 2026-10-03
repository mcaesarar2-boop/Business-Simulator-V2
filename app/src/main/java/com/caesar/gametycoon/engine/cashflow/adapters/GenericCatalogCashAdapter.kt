package com.caesar.gametycoon.engine.cashflow.adapters

import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Universal Fallback Cash Flow Adapter for standard catalog business units.
 *
 * Handles:
 * - Retail, Culinary, Logistics, Media, and any other general commercial business units.
 * - Respects business.customRevenue if configured, otherwise computes canonical gross revenue
 *   and operating maintenance expenses.
 */
class GenericCatalogCashAdapter : BusinessCashFlowAdapter {

    override fun canHandle(business: OwnedBusiness): Boolean {
        // Universal fallback accepts all business entities
        return true
    }

    override fun resolveFinancials(business: OwnedBusiness, state: PlayerState): UnitFinancialResolution {
        val grossRevenue = business.customRevenue ?: business.calculateGrossRevenue()
        val expenses = business.calculateTotalExpenses()

        return UnitFinancialResolution(
            grossRevenue = grossRevenue,
            expenses = expenses,
            updatedBusiness = business
        )
    }
}
