package com.caesar.gametycoon.engine.cashflow.adapters

import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Modular Cash Flow Adapter for Airline & Aviation Groups (catalogId: "aviation_group").
 */
class AviationCashAdapter : BusinessCashFlowAdapter {

    override fun canHandle(business: OwnedBusiness): Boolean {
        return business.catalogId == "aviation_group" || business.catalogId == "aviation_subsystem"
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
