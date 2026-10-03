package com.caesar.gametycoon.engine.cashflow

import com.caesar.gametycoon.engine.cashflow.adapters.*
import com.example.corporate.model.DividendPolicyPreset
import com.example.corporate.model.MergedDividendPolicy
import com.example.corporate.model.StandaloneDividendPolicy
import com.example.corporate.repository.CorporatePolicyRepository
import com.example.data.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CentralCashFlowPipelineEngineTest {

    private lateinit var policyRepo: CorporatePolicyRepository

    @Before
    fun setup() {
        policyRepo = CorporatePolicyRepository.getInstance()
    }

    @Test
    fun testStep1_NetLossSubtractedDirectlyFromUnitCash() {
        // When UnitNetIncome < 0 and Unit has sufficient cash:
        val standaloneBiz = OwnedBusiness(
            instanceId = "unit_loss_1",
            customName = "Cafe Alpha",
            companyCash = 50_000.0,
            parentId = null
        )

        val lossResolution = UnitFinancialResolution(
            grossRevenue = 20_000L,
            expenses = 40_000L, // Net loss = -20,000L
            updatedBusiness = standaloneBiz
        )

        val (result, holdingDelta) = CentralCashFlowPipelineEngine.processUnitCashFlow(
            unit = standaloneBiz,
            parentHolding = null,
            state = PlayerState(),
            customFinancialResolution = lossResolution
        )

        assertEquals(-20_000L, result.netIncome)
        assertEquals(20_000L, result.lossCoveredByUnit)
        assertEquals(0L, result.lossCoveredByHolding)
        assertEquals(0L, result.lossPassedToMegaHolding)
        assertEquals(30_000.0, result.updatedBusiness.companyCash, 0.001)
        assertEquals(0.0, holdingDelta, 0.001)
        assertEquals(0L, result.megaHoldingPayout)
    }

    @Test
    fun testStep1_NetLossCascadesToParentHoldingWhenMergedAndUnitCashDepleted() {
        // Merged unit with companyCash $10,000 suffers loss $25,000.
        // Unit cash reaches $0; parent holding absorbs remaining $15,000.
        val subBiz = OwnedBusiness(
            instanceId = "sub_resort_1",
            catalogId = "hospitality_holding",
            customName = "Bali Luxury Resort",
            companyCash = 10_000.0,
            parentId = "holding_nusantara"
        )
        val parentHolding = HoldingCompany(
            instanceId = "holding_nusantara",
            name = "Nusantara Hospitality Holding",
            subsidiaries = listOf(subBiz),
            holdingCash = 50_000.0
        )

        val lossResolution = UnitFinancialResolution(
            grossRevenue = 15_000L,
            expenses = 40_000L, // Net Loss = -25,000L
            updatedBusiness = subBiz
        )

        val (result, holdingDelta) = CentralCashFlowPipelineEngine.processUnitCashFlow(
            unit = subBiz,
            parentHolding = parentHolding,
            state = PlayerState(holdingCompanies = listOf(parentHolding)),
            customFinancialResolution = lossResolution
        )

        assertEquals(-25_000L, result.netIncome)
        assertTrue(result.isMerged)
        assertEquals(10_000L, result.lossCoveredByUnit)
        assertEquals(15_000L, result.lossCoveredByHolding)
        assertEquals(0L, result.lossPassedToMegaHolding)
        assertEquals(0.0, result.updatedBusiness.companyCash, 0.001)
        assertEquals(-15_000.0, holdingDelta, 0.001)
        assertEquals(0L, result.megaHoldingPayout)
    }

    @Test
    fun testStep1_NetLossCascadesToMegaHoldingWhenBothUnitAndHoldingCashDepleted() {
        // Merged unit: companyCash $5,000, holdingCash $10,000, loss $25,000.
        // Unit absorbs $5,000 -> reaches $0.
        // Holding absorbs $10,000 -> reaches $0.
        // Remaining $10,000 passed up to MegaHolding (Root Cash).
        val subBiz = OwnedBusiness(
            instanceId = "sub_deep_loss",
            catalogId = "upper_tech",
            companyCash = 5_000.0,
            parentId = "holding_tech"
        )
        val parentHolding = HoldingCompany(
            instanceId = "holding_tech",
            name = "Tech Holding",
            subsidiaries = listOf(subBiz),
            holdingCash = 10_000.0
        )

        val lossResolution = UnitFinancialResolution(
            grossRevenue = 0L,
            expenses = 25_000L, // Loss = -25,000L
            updatedBusiness = subBiz
        )

        val (result, holdingDelta) = CentralCashFlowPipelineEngine.processUnitCashFlow(
            unit = subBiz,
            parentHolding = parentHolding,
            state = PlayerState(holdingCompanies = listOf(parentHolding)),
            customFinancialResolution = lossResolution
        )

        assertEquals(5_000L, result.lossCoveredByUnit)
        assertEquals(10_000L, result.lossCoveredByHolding)
        assertEquals(10_000L, result.lossPassedToMegaHolding)
        assertEquals(-10_000L, result.megaHoldingPayout)
        assertEquals(-10_000.0, holdingDelta, 0.001)
        assertEquals(0.0, result.updatedBusiness.companyCash, 0.001)
    }

    @Test
    fun testStep1_StandaloneNetLossCascadesDirectlyToMegaHoldingWhenUnitCashDepleted() {
        // Standalone unit with companyCash $8,000 suffers loss $20,000.
        // Unit absorbs $8,000; remaining $12,000 passed to MegaHolding cash.
        val standaloneBiz = OwnedBusiness(
            instanceId = "standalone_1",
            customName = "Warung Kopi",
            companyCash = 8_000.0,
            parentId = null
        )

        val lossResolution = UnitFinancialResolution(
            grossRevenue = 10_000L,
            expenses = 30_000L,
            updatedBusiness = standaloneBiz
        )

        val (result, holdingDelta) = CentralCashFlowPipelineEngine.processUnitCashFlow(
            unit = standaloneBiz,
            parentHolding = null,
            state = PlayerState(),
            customFinancialResolution = lossResolution
        )

        assertFalse(result.isMerged)
        assertEquals(8_000L, result.lossCoveredByUnit)
        assertEquals(12_000L, result.lossPassedToMegaHolding)
        assertEquals(-12_000L, result.megaHoldingPayout)
        assertEquals(0.0, result.updatedBusiness.companyCash, 0.001)
        assertEquals(0.0, holdingDelta, 0.001)
    }

    @Test
    fun testStep2_HierarchyRoutingCheck_MergedVsStandalone() {
        val holding = HoldingCompany(
            instanceId = "holding_prime",
            name = "Prime Conglomerate",
            subsidiaries = emptyList()
        )

        val standaloneUnit = OwnedBusiness(instanceId = "biz_a", parentId = null)
        val orphanUnit = OwnedBusiness(instanceId = "biz_b", parentId = "non_existent_holding")
        val mergedUnitWithParentId = OwnedBusiness(instanceId = "biz_c", parentId = "holding_prime")
        val mergedUnitInSubsList = OwnedBusiness(instanceId = "biz_d", parentId = null)
        val holdingWithSub = holding.copy(subsidiaries = listOf(mergedUnitInSubsList))

        val holdings = listOf(holdingWithSub)

        assertFalse(CentralCashFlowPipelineEngine.isUnitMerged(standaloneUnit, holdings))
        assertFalse(CentralCashFlowPipelineEngine.isUnitMerged(orphanUnit, holdings))
        assertTrue(CentralCashFlowPipelineEngine.isUnitMerged(mergedUnitWithParentId, holdings))
        assertTrue(CentralCashFlowPipelineEngine.isUnitMerged(mergedUnitInSubsList, holdings))
    }

    @Test
    fun testStep3_ScenarioA_StandaloneDefaultPolicy_70_30Split() {
        // Standalone Unit Default Policy: 30% Unit Retained, 70% Mega Holding Payout
        val standaloneBiz = OwnedBusiness(
            instanceId = "biz_sa",
            customName = "Studio Mandiri",
            companyCash = 10_000.0,
            parentId = null
        )

        val profitResolution = UnitFinancialResolution(
            grossRevenue = 150_000L,
            expenses = 50_000L, // Net Income = 100,000L
            updatedBusiness = standaloneBiz
        )

        val (result, holdingDelta) = CentralCashFlowPipelineEngine.processUnitCashFlow(
            unit = standaloneBiz,
            parentHolding = null,
            state = PlayerState(),
            standalonePolicy = StandaloneDividendPolicy.DEFAULT,
            customFinancialResolution = profitResolution
        )

        assertFalse(result.isMerged)
        assertEquals(100_000L, result.netIncome)
        assertEquals(30_000L, result.retainedInUnit)
        assertEquals(0L, result.holdingTreasuryPayout)
        assertEquals(70_000L, result.megaHoldingPayout)
        assertEquals(40_000.0, result.updatedBusiness.companyCash, 0.001) // 10k + 30k
        assertEquals(0.0, holdingDelta, 0.001)
    }

    @Test
    fun testStep3_ScenarioB_MergedSubsidiary_HoldingTreasuryReceivesPayout_FixesZeroDollarBug() {
        // Merged Subsidiary Default Policy: 20% Unit, 50% Holding Treasury, 30% Mega Holding
        val subBiz = OwnedBusiness(
            instanceId = "sub_merged_1",
            customName = "Merged Cinema Studio",
            companyCash = 25_000.0,
            parentId = "holding_media"
        )
        val parentHolding = HoldingCompany(
            instanceId = "holding_media",
            name = "Media Mega Group",
            subsidiaries = listOf(subBiz),
            holdingCash = 100_000.0
        )

        val profitResolution = UnitFinancialResolution(
            grossRevenue = 300_000L,
            expenses = 100_000L, // Net Income = 200,000L
            updatedBusiness = subBiz
        )

        val (result, holdingDelta) = CentralCashFlowPipelineEngine.processUnitCashFlow(
            unit = subBiz,
            parentHolding = parentHolding,
            state = PlayerState(holdingCompanies = listOf(parentHolding)),
            mergedPolicy = MergedDividendPolicy.DEFAULT,
            customFinancialResolution = profitResolution
        )

        assertTrue(result.isMerged)
        assertEquals(200_000L, result.netIncome)
        // 1. RetainedUnitCash = 200,000 * 20% = 40,000
        assertEquals(40_000L, result.retainedInUnit)
        assertEquals(65_000.0, result.updatedBusiness.companyCash, 0.001) // 25k + 40k

        // 2. HoldingTreasuryPayout = 200,000 * 50% = 100,000 (FIXES $0 HOLDING PAYOUT BUG!)
        assertEquals(100_000L, result.holdingTreasuryPayout)
        assertEquals(100_000.0, holdingDelta, 0.001)

        // 3. MegaHoldingDividend = 200,000 * 30% = 60,000
        assertEquals(60_000L, result.megaHoldingPayout)
    }

    @Test
    fun testStep3_ScenarioB_MergedCustomPresets() {
        val subBiz = OwnedBusiness(instanceId = "sub_x", companyCash = 0.0, parentId = "h_x")
        val holding = HoldingCompany(instanceId = "h_x", name = "Test Holding", subsidiaries = listOf(subBiz))
        val netProfit = 100_000L
        val res = UnitFinancialResolution(100_000L, 0L, subBiz)

        // Growth preset: 40% Unit, 45% Holding, 15% Mega
        val (growthResult, _) = CentralCashFlowPipelineEngine.processUnitCashFlow(
            unit = subBiz,
            parentHolding = holding,
            state = PlayerState(holdingCompanies = listOf(holding)),
            mergedPolicy = MergedDividendPolicy.GROWTH,
            customFinancialResolution = res
        )
        assertEquals(40_000L, growthResult.retainedInUnit)
        assertEquals(45_000L, growthResult.holdingTreasuryPayout)
        assertEquals(15_000L, growthResult.megaHoldingPayout)

        // Cash Cow preset: 10% Unit, 20% Holding, 70% Mega
        val (cashCowResult, _) = CentralCashFlowPipelineEngine.processUnitCashFlow(
            unit = subBiz,
            parentHolding = holding,
            state = PlayerState(holdingCompanies = listOf(holding)),
            mergedPolicy = MergedDividendPolicy.CASH_COW,
            customFinancialResolution = res
        )
        assertEquals(10_000L, cashCowResult.retainedInUnit)
        assertEquals(20_000L, cashCowResult.holdingTreasuryPayout)
        assertEquals(70_000L, cashCowResult.megaHoldingPayout)
    }

    @Test
    fun testAdapters_FilmStudioCashAdapter() {
        val adapter = FilmStudioCashAdapter()
        val filmBiz = OwnedBusiness(
            instanceId = "film_1",
            catalogId = "media_production",
            projectHistory = listOf(
                MovieProject(
                    title = "Blockbuster",
                    budget = 10_000_000L,
                    genres = listOf("Action"),
                    distributionScale = "Global",
                    reviewScore = 85,
                    boxOffice = 50_000_000L,
                    netProfit = 25_000_000L,
                    status = "FINISHED",
                    licenseMonthlyFee = 35_000L
                ),
                MovieProject(
                    title = "Current Hit",
                    budget = 5_000_000L,
                    genres = listOf("Drama"),
                    distributionScale = "National",
                    reviewScore = 80,
                    boxOffice = 20_000_000L,
                    netProfit = 10_000_000L,
                    status = "IN_THEATERS",
                    lastMonthRevenue = 65_000L
                )
            )
        )

        assertTrue(adapter.canHandle(filmBiz))
        val resolution = adapter.resolveFinancials(filmBiz, PlayerState())
        assertEquals(100_000L, resolution.grossRevenue) // 35k + 65k
        assertTrue(resolution.expenses > 0L)
    }

    @Test
    fun testAdapters_ContentCreatorCashAdapter() {
        val adapter = ContentCreatorCashAdapter()
        val creatorBiz = OwnedBusiness(
            instanceId = "creator_1",
            catalogId = "content_creator",
            contentCreatorSubscribers = 20_000L, // Base AdSense = 1000
            contentCreatorEmployees = 2, // 10% boost -> 1100
            contentCreatorContracts = listOf(
                ActiveCreatorContract(
                    brandName = "Tech Brand",
                    tierLevel = 2,
                    categoryTag = "Tech",
                    monthlyPayout = 8_900L,
                    totalMonths = 6,
                    remainingMonths = 4
                )
            )
        )

        assertTrue(adapter.canHandle(creatorBiz))
        val resolution = adapter.resolveFinancials(creatorBiz, PlayerState())
        assertEquals(10_000L, resolution.grossRevenue) // 1100 + 8900
        assertEquals(4_500L, resolution.expenses) // 500 base + 2 * 2000
        assertEquals(5_500L, resolution.netIncome)
    }

    @Test
    fun testAdapters_HospitalityCashAdapter() {
        val adapter = HospitalityCashAdapter()
        val resort = HotelProperty(
            name = "Grand Resort",
            location = "Bali",
            tier = HotelTier.LUXURY_RESORT_5STAR,
            isConstructing = false,
            remainingBuildMonths = 0,
            customRoomRate = 500L,
            lastMonthRevenue = 80_000L,
            lastMonthExpense = 30_000L
        )
        val hospBiz = OwnedBusiness(
            instanceId = "hosp_1",
            catalogId = "hospitality_holding",
            hospitalityProperties = listOf(resort)
        )

        assertTrue(adapter.canHandle(hospBiz))
        val resolution = adapter.resolveFinancials(hospBiz, PlayerState())
        assertEquals(80_000L, resolution.grossRevenue)
        assertEquals(30_000L, resolution.expenses)
        assertEquals(50_000L, resolution.netIncome)
    }

    @Test
    fun testAdapters_GenericCatalogCashAdapter_Fallback() {
        val adapter = GenericCatalogCashAdapter()
        val retailBiz = OwnedBusiness(
            instanceId = "retail_1",
            catalogId = "shop_small_chain",
            customRevenue = 45_000L
        )

        assertTrue(adapter.canHandle(retailBiz))
        val resolution = adapter.resolveFinancials(retailBiz, PlayerState())
        assertEquals(45_000L, resolution.grossRevenue)
        assertTrue(resolution.expenses > 0L)
    }

    @Test
    fun testFullMonthlyPipelineSweep_MultiHolding_MultiSubsidiary() {
        // Setup complex state:
        // 1. Standalone Unit (Content Creator): profit 50,000 -> 15,000 unit, 35,000 mega
        val standaloneCreator = OwnedBusiness(
            instanceId = "creator_standalone",
            catalogId = "content_creator",
            customRevenue = 60_000L,
            companyCash = 10_000.0,
            parentId = null
        )

        // 2. Holding Company Alpha with 2 subsidiaries:
        // Sub 1 (Film): profit 100,000 -> 20,000 sub, 50,000 holding treasury, 30,000 mega
        // Sub 2 (Hospitality): profit 200,000 -> 40,000 sub, 100,000 holding treasury, 60,000 mega
        val subFilm = OwnedBusiness(
            instanceId = "sub_film",
            catalogId = "media_production",
            customRevenue = 145_000L,
            companyCash = 5_000.0,
            parentId = "holding_alpha"
        )
        val subHosp = OwnedBusiness(
            instanceId = "sub_hosp",
            catalogId = "hospitality_holding",
            customRevenue = 200_000L,
            companyCash = 10_000.0,
            parentId = "holding_alpha"
        )
        val holdingAlpha = HoldingCompany(
            instanceId = "holding_alpha",
            name = "Alpha Conglomerate",
            holdingCash = 50_000.0,
            subsidiaries = listOf(subFilm, subHosp)
        )

        val initialState = PlayerState(
            cash = 100_000L,
            ownedBusinesses = listOf(standaloneCreator),
            holdingCompanies = listOf(holdingAlpha)
        )

        val result = CentralCashFlowPipelineEngine.processMonthlyCashFlow(initialState, policyRepo)

        // Verify holding treasury receives exactly the 50% allocated treasury inflows
        val finalHolding = result.updatedState.holdingCompanies.first { it.instanceId == "holding_alpha" }
        assertTrue("Holding cash must be significantly higher than initial $50,000", finalHolding.holdingCash > 50_000.0)

        // Verify root MegaHolding cash receives the upward payouts
        assertTrue("Root Mega Holding cash must receive dividend payouts", result.updatedState.cash > 100_000L)

        // Total distributed check: unit retained + holding treasury + mega payout = net income
        result.unitResults.forEach { unitRes ->
            if (unitRes.netIncome > 0L) {
                assertEquals(
                    unitRes.netIncome,
                    unitRes.retainedInUnit + unitRes.holdingTreasuryPayout + unitRes.megaHoldingPayout
                )
            }
        }
    }
}
