package com.caesar.gametycoon.engine.cashflow.adapters

import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Modular Cash Flow Adapter for Hospitality & Resort holdings (catalogId: "hospitality_holding").
 *
 * Resolves:
 * - Dynamic hotel property occupancy and room rate revenues across room classes.
 * - Mega Event revenue spikes (e.g. G20 Summit host).
 * - Staff salaries, room operational utilities, and facility maintenance upkeep.
 */
class HospitalityCashAdapter : BusinessCashFlowAdapter {

    override fun canHandle(business: OwnedBusiness): Boolean {
        return business.catalogId == "hospitality_holding"
    }

    override fun resolveFinancials(business: OwnedBusiness, state: PlayerState): UnitFinancialResolution {
        // Aggregate actual monthly revenue across all hotel properties
        val hotelPropertiesRevenue = business.hospitalityProperties.sumOf { it.lastMonthRevenue }
        val grossRevenue = if (hotelPropertiesRevenue > 0L) {
            hotelPropertiesRevenue
        } else {
            business.customRevenue ?: business.calculateGrossRevenue()
        }

        // Aggregate actual monthly expenses across all hotel properties
        val hotelPropertiesExpense = business.hospitalityProperties.sumOf { it.lastMonthExpense }
        val expenses = if (hotelPropertiesExpense > 0L) {
            hotelPropertiesExpense
        } else {
            business.calculateTotalExpenses()
        }

        val updatedBusiness = business.copy(customRevenue = grossRevenue)

        return UnitFinancialResolution(
            grossRevenue = grossRevenue,
            expenses = expenses,
            updatedBusiness = updatedBusiness
        )
    }
}
