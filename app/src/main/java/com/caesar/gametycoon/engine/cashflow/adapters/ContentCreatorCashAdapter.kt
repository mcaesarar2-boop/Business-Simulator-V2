package com.caesar.gametycoon.engine.cashflow.adapters

import com.caesar.gametycoon.engine.fix.ContentCreatorCashFixEngine
import com.example.data.ContentStatus
import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Modular Cash Flow Adapter for Content Creator Studios (catalogId: "content_creator").
 *
 * Resolves:
 * - Scaled AdSense revenue (subscribers * employee production multiplier).
 * - Brand sponsorship contracts monthly payouts.
 * - Licensed intellectual property content royalties.
 * - Crew salaries and studio maintenance expenses.
 */
class ContentCreatorCashAdapter : BusinessCashFlowAdapter {

    override fun canHandle(business: OwnedBusiness): Boolean {
        return business.catalogId == "content_creator"
    }

    override fun resolveFinancials(business: OwnedBusiness, state: PlayerState): UnitFinancialResolution {
        val employeeMultiplier = 1.0 + (business.contentCreatorEmployees * 0.05)
        val baseAdSense = (business.contentCreatorSubscribers * 0.05).toLong()
        val totalAdSense = (baseAdSense * employeeMultiplier).toLong()

        val contractsRevenue = business.contentCreatorContracts.sumOf { it.monthlyPayout }
        val royaltiesRevenue = business.contentPortfolio
            .filter { it.status == ContentStatus.LICENSED }
            .sumOf { it.monthlyRoyalty }

        val grossRevenue = totalAdSense + contractsRevenue + royaltiesRevenue

        val employeeSalaries = business.contentCreatorEmployees * ContentCreatorCashFixEngine.EMPLOYEE_SALARY_PER_HEAD
        val expenses = ContentCreatorCashFixEngine.DEFAULT_BASE_MAINTENANCE + employeeSalaries

        val updatedBusiness = business.copy(
            customRevenue = grossRevenue,
            contentCreatorCash = business.companyCash.toLong()
        )

        return UnitFinancialResolution(
            grossRevenue = grossRevenue,
            expenses = expenses,
            updatedBusiness = updatedBusiness
        )
    }
}
