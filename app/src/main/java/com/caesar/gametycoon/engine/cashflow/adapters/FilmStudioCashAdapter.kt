package com.caesar.gametycoon.engine.cashflow.adapters

import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Modular Cash Flow Adapter for Film Production Studios (catalogId: "media_production").
 *
 * Resolves:
 * - OTT / Broadcast catalog licensing royalties (FINISHED films).
 * - Active theatrical box office run revenues (IN_THEATERS films).
 * - Studio facility maintenance and infrastructure upkeep expenses.
 */
class FilmStudioCashAdapter : BusinessCashFlowAdapter {

    override fun canHandle(business: OwnedBusiness): Boolean {
        return business.catalogId == "media_production"
    }

    override fun resolveFinancials(business: OwnedBusiness, state: PlayerState): UnitFinancialResolution {
        // 1. Streaming / OTT licensing royalties from finished catalog library
        val streamingRevenue = business.projectHistory
            .filter { it.status == "FINISHED" }
            .sumOf { it.licenseMonthlyFee ?: 0L }

        // 2. Active theatrical box office runs
        val activeTheatersRevenue = business.projectHistory
            .filter { it.status == "IN_THEATERS" }
            .sumOf { project ->
                if (project.lastMonthRevenue > 0L) {
                    project.lastMonthRevenue
                } else {
                    val remaining = maxOf(1, project.remainingMonths + 1)
                    project.currentRevenue / remaining
                }
            }

        val dynamicRevenue = streamingRevenue + activeTheatersRevenue
        val grossRevenue = if (dynamicRevenue > 0L) {
            dynamicRevenue
        } else {
            business.customRevenue ?: business.calculateGrossRevenue()
        }

        // 3. Operational expenses (base facility maintenance + soundstage/CGI infrastructure upgrades)
        val expenses = business.calculateTotalExpenses()

        val updatedBusiness = business.copy(customRevenue = grossRevenue)

        return UnitFinancialResolution(
            grossRevenue = grossRevenue,
            expenses = expenses,
            updatedBusiness = updatedBusiness
        )
    }
}
