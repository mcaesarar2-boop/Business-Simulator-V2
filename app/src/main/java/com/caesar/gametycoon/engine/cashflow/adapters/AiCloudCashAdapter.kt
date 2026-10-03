package com.caesar.gametycoon.engine.cashflow.adapters

import com.example.aicloud.data.AiCloudRepository
import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Modular Cash Flow Adapter for AI Cloud & GPU Data Center Providers (catalogId: "ai_cloud_provider").
 */
class AiCloudCashAdapter : BusinessCashFlowAdapter {

    override fun canHandle(business: OwnedBusiness): Boolean {
        return business.catalogId == "ai_cloud_provider"
    }

    override fun resolveFinancials(business: OwnedBusiness, state: PlayerState): UnitFinancialResolution {
        val repo = AiCloudRepository.getInstance()
        val latestReport = repo.lastReport.value

        val grossRevenue = if (latestReport != null && latestReport.totalRevenue > 0L) {
            latestReport.totalRevenue
        } else {
            business.customRevenue ?: business.calculateGrossRevenue()
        }

        val expenses = if (latestReport != null && latestReport.totalExpenses > 0L) {
            latestReport.totalExpenses
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
