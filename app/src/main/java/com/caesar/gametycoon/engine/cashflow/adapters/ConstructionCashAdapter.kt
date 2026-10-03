package com.caesar.gametycoon.engine.cashflow.adapters

import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Modular Cash Flow Adapter for Construction Developers (catalogId: "construction").
 */
class ConstructionCashAdapter : BusinessCashFlowAdapter {

    override fun canHandle(business: OwnedBusiness): Boolean {
        return business.catalogId == "construction" || business.catalogId == "construction_developer"
    }

    override fun resolveFinancials(business: OwnedBusiness, state: PlayerState): UnitFinancialResolution {
        val grossRevenue = business.customRevenue ?: business.calculateGrossRevenue()
        val expenses = business.calculateTotalExpenses()

        val updatedBusiness = business.copy(customRevenue = grossRevenue)

        return UnitFinancialResolution(
            grossRevenue = grossRevenue,
            expenses = expenses,
            updatedBusiness = updatedBusiness
        )
    }
}
