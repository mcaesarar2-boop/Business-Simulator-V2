package com.caesar.gametycoon.engine.fix

import com.example.corporate.model.StandaloneDividendPolicy
import com.example.data.ActiveCreatorContract
import com.example.data.ContentStatus
import com.example.data.ContentWork
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentCreatorCashFixEngineTest {

    private fun createSampleContentCreatorBusiness(): OwnedBusiness {
        // Setup values to generate exactly $3,029 total revenue:
        // AdSense: 20,000 subs * 0.05 = 1,000 * (1 + 0.15 for 3 employees) = $1,150
        // Sponsorship: $1,200
        // Royalties: $679
        // Total = 1,150 + 1,200 + 679 = $3,029
        return OwnedBusiness(
            instanceId = "cc_test_1",
            catalogId = ContentCreatorCashFixEngine.CATALOG_ID,
            customName = "Studio Caesar Creative",
            level = 15,
            contentCreatorSubscribers = 20_000L,
            contentCreatorEmployees = 3,
            contentCreatorCash = 10_000L,
            companyCash = 5_000.0,
            contentCreatorContracts = listOf(
                ActiveCreatorContract(
                    id = "contract_1",
                    brandName = "TechCorp",
                    tierLevel = 2,
                    categoryTag = "Tech",
                    monthlyPayout = 1200L,
                    totalMonths = 12,
                    remainingMonths = 6,
                    totalPaidSoFar = 7200L
                )
            ),
            contentPortfolio = listOf(
                ContentWork(
                    id = "work_1",
                    title = "Viral Short",
                    status = ContentStatus.LICENSED,
                    monthlyRoyalty = 679L,
                    budget = 5000L
                )
            )
        )
    }

    @Test
    fun testRevenueCalculation_matchesExpectedEnginePayout() {
        val business = createSampleContentCreatorBusiness()
        val calculatedRevenue = ContentCreatorCashFixEngine.calculateSubsystemRevenue(business)
        assertEquals(3029L, calculatedRevenue)
    }

    @Test
    fun testSyncSubsystemRevenue_overwritesOwnedBusinessRevenue() {
        val rawBusiness = createSampleContentCreatorBusiness().copy(customRevenue = 2479L)
        val state = PlayerState(
            cash = 50_000L,
            ownedBusinesses = listOf(rawBusiness)
        )

        val syncedState = ContentCreatorCashFixEngine.syncSubsystemRevenue(state)
        val syncedBusiness = syncedState.ownedBusinesses.first()

        assertEquals(3029L, syncedBusiness.customRevenue)
    }

    @Test
    fun testRecalculateNetIncome_eliminatesGhostNegativeDeduction() {
        // Bug scenario: Revenue = +$2,479, Expenses = -$500, but legacy bug yielded -$14,814.
        val netIncomeOldRev = ContentCreatorCashFixEngine.recalculateNetIncome(
            totalSubsystemRevenue = 2479L,
            totalMaintenanceExpenses = 500L,
            explicitTaxes = 0L
        )
        assertEquals(1979L, netIncomeOldRev)
        assertTrue("Net Income must be positive without ghost negative deductions", netIncomeOldRev > 0)

        // Synced scenario: Revenue = +$3,029, Expenses = -$500, Taxes = 0
        val netIncomeSynced = ContentCreatorCashFixEngine.recalculateNetIncome(
            totalSubsystemRevenue = 3029L,
            totalMaintenanceExpenses = 500L,
            explicitTaxes = 0L
        )
        assertEquals(2529L, netIncomeSynced)
    }

    @Test
    fun testForceStandalonePayout_depositsDividendsCorrectly() {
        // Business with Gross = 3,029, Maintenance = 500 (Base 500 + 0 employees)
        val soloBusiness = createSampleContentCreatorBusiness().copy(
            contentCreatorEmployees = 0,
            contentCreatorSubscribers = 23_000L // 23,000 * 0.05 = 1,150 + 1,200 + 679 = 3,029
        )
        val initialState = PlayerState(
            cash = 10_000L,
            ownedBusinesses = listOf(soloBusiness)
        )

        val policy = StandaloneDividendPolicy(unitRetainedPercent = 30, megaHoldingPayoutPercent = 70)
        val result = ContentCreatorCashFixEngine.forceStandalonePayout(initialState, policy)

        // Net income = 3,029 - 500 = 2,529
        // 30% retained = (2529 * 0.3).toLong() = 758
        // 70% dividend = 2,529 - 758 = 1,771
        assertEquals(3029L, result.totalRevenue)
        assertEquals(500L, result.totalExpenses)
        assertEquals(2529L, result.netIncome)
        assertEquals(1771L, result.totalDividendPaidToMainCash)
        assertEquals(758L, result.totalRetainedInUnitCash)

        // Main Cash balance updated
        assertEquals(10_000L + 1771L, result.updatedState.cash)

        // Unit cash balances updated
        val updatedBiz = result.updatedState.ownedBusinesses.first()
        assertEquals(5_000.0 + 758.0, updatedBiz.companyCash, 0.01)
        assertEquals(10_000L + 758L, updatedBiz.contentCreatorCash)
    }

    @Test
    fun testForceStandalonePayout_legacyRevenueScenarioDividend() {
        // Specifically testing the user's scenario: Net = $2,479 -> 70% Dividend is $1,736
        val net = ContentCreatorCashFixEngine.recalculateNetIncome(2479L, 0L)
        val (retained, dividend) = com.example.corporate.engine.CashFlowDistributionEngine.calculateStandaloneProfitSplit(
            net,
            StandaloneDividendPolicy(30, 70)
        )
        assertEquals(1736L, dividend)
        assertEquals(743L, retained)
    }
}
