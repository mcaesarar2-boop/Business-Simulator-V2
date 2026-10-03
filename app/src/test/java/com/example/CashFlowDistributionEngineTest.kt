package com.example

import com.example.corporate.engine.CashFlowDistributionEngine
import com.example.corporate.model.DividendPolicyPreset
import com.example.corporate.model.MergedDividendPolicy
import com.example.corporate.model.StandaloneDividendPolicy
import com.example.corporate.repository.CorporatePolicyRepository
import com.example.data.HoldingCompany
import com.example.data.OwnedBusiness
import org.junit.Assert.*
import org.junit.Test

class CashFlowDistributionEngineTest {

    @Test
    fun testScenarioA_StandaloneDefaultSplit() {
        // Standalone Unit Default: 30% Retained in Unit, 70% to Mega Holding
        val netProfit = 100_000L
        val (retained, megaPayout) = CashFlowDistributionEngine.calculateStandaloneProfitSplit(
            netProfit = netProfit,
            policy = StandaloneDividendPolicy.DEFAULT
        )

        assertEquals(30_000L, retained)
        assertEquals(70_000L, megaPayout)
        assertEquals(netProfit, retained + megaPayout)
    }

    @Test
    fun testScenarioA_StandaloneCustomPolicy() {
        val customPolicy = StandaloneDividendPolicy(unitRetainedPercent = 60, megaHoldingPayoutPercent = 40)
        val netProfit = 250_000L
        val (retained, megaPayout) = CashFlowDistributionEngine.calculateStandaloneProfitSplit(
            netProfit = netProfit,
            policy = customPolicy
        )

        assertEquals(150_000L, retained)
        assertEquals(100_000L, megaPayout)
    }

    @Test
    fun testScenarioA_StandaloneDeficitAbsorbedByUnitCash() {
        val biz = OwnedBusiness(
            instanceId = "unit_1",
            customName = "Studio Mandiri",
            companyCash = 50_000.0
        )
        val operationalLoss = -30_000L

        val (updatedBiz, result) = CashFlowDistributionEngine.processStandaloneBusinessCashFlow(
            business = biz,
            netProfit = operationalLoss,
            policy = StandaloneDividendPolicy.DEFAULT
        )

        // Unit cash absorbs all 30k loss
        assertEquals(20_000.0, updatedBiz.companyCash, 0.001)
        assertEquals(30_000L, result.absorbedDeficit)
        assertEquals(0L, result.megaHoldingDividend) // No deficit spilled over to player
    }

    @Test
    fun testScenarioB_MergedDefaultSplit() {
        // Merged Unit Default: 20% Unit, 50% Kas Internal Holding, 30% Mega Holding Dividend
        val netProfit = 200_000L
        val (unitRetained, holdingTreasury, megaDividend) = CashFlowDistributionEngine.calculateMergedProfitSplit(
            netProfit = netProfit,
            policy = MergedDividendPolicy.DEFAULT
        )

        assertEquals(40_000L, unitRetained)
        assertEquals(100_000L, holdingTreasury)
        assertEquals(60_000L, megaDividend)
        assertEquals(netProfit, unitRetained + holdingTreasury + megaDividend)
    }

    @Test
    fun testScenarioB_MergedPresets() {
        val netProfit = 100_000L

        // Growth preset: 40% Unit, 45% Holding, 15% Mega
        val (gUnit, gHolding, gMega) = CashFlowDistributionEngine.calculateMergedProfitSplit(
            netProfit, MergedDividendPolicy.GROWTH
        )
        assertEquals(40_000L, gUnit)
        assertEquals(45_000L, gHolding)
        assertEquals(15_000L, gMega)

        // Cash Cow preset: 10% Unit, 20% Holding, 70% Mega
        val (cUnit, cHolding, cMega) = CashFlowDistributionEngine.calculateMergedProfitSplit(
            netProfit, MergedDividendPolicy.CASH_COW
        )
        assertEquals(10_000L, cUnit)
        assertEquals(20_000L, cHolding)
        assertEquals(70_000L, cMega)
    }

    @Test
    fun testScenarioB_MergedDeficitBufferHierarchy() {
        val subsidiary = OwnedBusiness(
            instanceId = "sub_hotel",
            customName = "Grand Hotel",
            companyCash = 10_000.0
        )
        val parentHolding = HoldingCompany(
            instanceId = "holding_1",
            name = "Nusantara Hospitality Group",
            holdingCash = 50_000.0
        )
        val severeLoss = -25_000L // 25k loss

        val (updatedSub, treasuryDelta, result) = CashFlowDistributionEngine.processMergedSubsidiaryCashFlow(
            subsidiary = subsidiary,
            parentHolding = parentHolding,
            netProfit = severeLoss,
            policy = MergedDividendPolicy.DEFAULT
        )

        // 1. Unit cash absorbs its entire 10k
        assertEquals(0.0, updatedSub.companyCash, 0.001)
        // 2. Remaining 15k is absorbed by holding internal cash
        assertEquals(-15_000.0, treasuryDelta, 0.001)
        assertEquals(25_000L, result.absorbedDeficit)
        // 3. Mega Holding player cash is shielded (0 deficit spilled to Mega)
        assertEquals(0L, result.megaHoldingDividend)
    }

    @Test
    fun testHoldingCompanyMultiSubsidiaryCashFlow() {
        val sub1 = OwnedBusiness(instanceId = "sub1", customName = "App Studio", companyCash = 5_000.0)
        val sub2 = OwnedBusiness(instanceId = "sub2", customName = "Fintech", companyCash = 10_000.0)
        val holding = HoldingCompany(
            instanceId = "h1",
            name = "Alpha Tech Holdings",
            subsidiaries = listOf(sub1, sub2),
            holdingCash = 100_000.0
        )

        val profits = mapOf("sub1" to 50_000L, "sub2" to 150_000L) // Total 200k profit
        val (updatedHolding, results) = CashFlowDistributionEngine.processHoldingCompanyCashFlow(
            holding = holding,
            subNetProfits = profits,
            policy = MergedDividendPolicy.DEFAULT
        )

        // Holding treasury should receive 50% of 200k = +100k
        assertEquals(200_000.0, updatedHolding.holdingCash, 0.001)

        // Sub1 should receive 20% of 50k = +10k -> 15k total
        assertEquals(15_000.0, updatedHolding.subsidiaries[0].companyCash, 0.001)
        // Sub2 should receive 20% of 150k = +30k -> 40k total
        assertEquals(40_000.0, updatedHolding.subsidiaries[1].companyCash, 0.001)

        // Total Mega Holding dividend = 30% of 200k = 60k
        val totalMegaDiv = results.sumOf { it.megaHoldingDividend }
        assertEquals(60_000L, totalMegaDiv)
    }

    @Test
    fun testCorporatePolicyRepository_StateFlowAndPresets() {
        val repo = CorporatePolicyRepository.getInstance()
        val holdingId = "test_holding_repo"

        repo.applyPresetToHolding(holdingId, DividendPolicyPreset.GROWTH_REINVESTMENT)
        val holdingPolicy = repo.getHoldingPolicy(holdingId)
        assertEquals(40, holdingPolicy.unitRetainedPercent)
        assertEquals(45, holdingPolicy.holdingTreasuryPercent)
        assertEquals(15, holdingPolicy.megaHoldingDividendPercent)

        val standaloneId = "test_standalone_repo"
        repo.applyPresetToStandalone(standaloneId, DividendPolicyPreset.CASH_COW_LIQUIDITY)
        val standalonePolicy = repo.getStandalonePolicy(standaloneId)
        assertEquals(10, standalonePolicy.unitRetainedPercent)
        assertEquals(90, standalonePolicy.megaHoldingPayoutPercent)
    }
}
