package com.caesar.gametycoon.stock.takeover

import com.example.data.MegaHoldingState
import com.example.data.OwnedStock
import com.example.data.PlayerState
import com.example.data.StockItem
import org.junit.Assert.*
import org.junit.Test

class StockTakeoverEngineTest {

    private val testStock = StockItem(
        id = "aapl_test",
        ticker = "AAPL",
        name = "Apple Inc.",
        currentPrice = 100.0,
        changeAbsolute = 2.0,
        changePercentage = 2.04,
        sector = "Technology",
        sharesOutstanding = 1_000_000L
    )

    @Test
    fun testRetailCapEnforcement_blocksAbove49PercentWithoutMegaHolding() {
        val baseState = PlayerState(
            cash = 50_000_000L,
            megaHolding = MegaHoldingState(isActive = false),
            ownedStocks = listOf(OwnedStock("AAPL", 100.0, 400_000L)) // 40%
        )

        // Attempting to buy 100,000 more shares -> 50% cumulative stake
        val canBuy10Percent = StockAcquisitionRepository.canBuyRetailShares(
            playerState = baseState,
            stock = testStock,
            additionalShares = 100_000L
        )
        assertFalse("Player without Mega Holding should be blocked from reaching 50%", canBuy10Percent)

        // Buying 50,000 shares -> 45% cumulative stake
        val canBuy5Percent = StockAcquisitionRepository.canBuyRetailShares(
            playerState = baseState,
            stock = testStock,
            additionalShares = 50_000L
        )
        assertTrue("Player should be permitted to buy retail stake <= 49.9%", canBuy5Percent)
    }

    @Test
    fun testRetailCapEnforcement_permitsWhenMegaHoldingIsActive() {
        val baseState = PlayerState(
            cash = 50_000_000L,
            megaHolding = MegaHoldingState(isActive = true, companyName = "Caesar Global Corp"),
            ownedStocks = listOf(OwnedStock("AAPL", 100.0, 400_000L))
        )

        val canBuy = StockAcquisitionRepository.canBuyRetailShares(
            playerState = baseState,
            stock = testStock,
            additionalShares = 200_000L
        )
        assertTrue("Mega Holding owner can acquire stakes >50%", canBuy)
    }

    @Test
    fun testTakeoverEligibility_requiresMegaHolding() {
        val nonHoldingState = PlayerState(
            cash = 100_000_000L,
            megaHolding = MegaHoldingState(isActive = false)
        )
        val eligibility = StockAcquisitionRepository.checkTakeoverEligibility(nonHoldingState, testStock)
        assertEquals(TakeoverEligibility.REQUIRES_MEGA_HOLDING, eligibility)

        val holdingState = PlayerState(
            cash = 100_000_000L,
            megaHolding = MegaHoldingState(isActive = true, companyName = "Caesar Holding")
        )
        val holdingEligibility = StockAcquisitionRepository.checkTakeoverEligibility(holdingState, testStock)
        assertEquals(TakeoverEligibility.ELIGIBLE, holdingEligibility)
    }

    @Test
    fun testInitialBoardAskingPrice_hasControllingPremium() {
        val initialAsk = StockAcquisitionEngine.computeInitialBoardAskingPrice(
            marketPrice = 100.0,
            targetStakePercent = 51.0,
            isDistressed = false
        )
        assertTrue("Initial board ask should demand at least 15% premium", initialAsk >= 115.0)
        assertTrue("Initial board ask should not exceed 150%", initialAsk <= 150.0)
    }

    @Test
    fun testHostileLowball_rejectionWhenUnder70Percent() {
        val hostileOffer = 50.0 // 50% of 100.0 market price
        val decision = StockAcquisitionEngine.processBiddingRound(
            offerPrice = hostileOffer,
            marketPrice = 100.0,
            currentBoardAskingPrice = 125.0,
            round = 1,
            maxRounds = 5,
            targetStakePercent = 51.0,
            additionalSharesNeeded = 510_000L,
            isDistressed = false
        )

        assertTrue("Hostile offer <70% market should be rejected", decision is BoardDecisionResult.Rejected)
        val rejected = decision as BoardDecisionResult.Rejected
        assertTrue("Board must walk away from hostile lowball", rejected.walkedAway)
    }

    @Test
    fun testDistressTakeover_discountAndHighAcceptance() {
        val initialAskDistressed = StockAcquisitionEngine.computeInitialBoardAskingPrice(
            marketPrice = 100.0,
            targetStakePercent = 51.0,
            isDistressed = true
        )
        assertTrue("Distressed board should ask for a deep discount (<70%)", initialAskDistressed < 70.0)

        // Probability for distressed company at 50% market price should be high (>50%)
        val prob = StockAcquisitionEngine.calculateAcceptanceProbability(
            offerPrice = 55.0,
            marketPrice = 100.0,
            boardAskingPrice = initialAskDistressed,
            isDistressed = true
        )
        assertTrue("Acceptance probability for bailout capital should be high", prob >= 0.50)
    }

    @Test
    fun testExecuteTakeover_mergesToHoldingAndCreatesSubsidiary() {
        val playerState = PlayerState(
            cash = 80_000_000L,
            megaHolding = MegaHoldingState(isActive = true, companyName = "Caesar Mega Corp"),
            ownedStocks = listOf(OwnedStock("AAPL", 90.0, 50_000L)),
            corporateStockPortfolio = listOf(OwnedStock("AAPL", 90.0, 50_000L))
        )

        val result = StockAcquisitionRepository.executeTakeover(
            currentState = playerState,
            stock = testStock,
            finalPricePerShare = 110.0,
            targetStakePercent = 51.0,
            customName = "Apple Subsidiary Tech"
        )

        assertTrue("Takeover execution must succeed", result.isSuccess)
        assertNotNull("Acquired business unit must be created", result.acquiredBusiness)
        assertEquals("Apple Subsidiary Tech", result.acquiredBusiness?.name)
        assertEquals("AAPL", result.acquiredBusiness?.acquiredStockTicker)
        assertEquals(51.0, result.acquiredBusiness?.ownershipPercent ?: 0.0, 0.01)

        // Verify passive stock cleaned up
        assertFalse(
            "Target stock must be removed from retail ownedStocks",
            result.updatedPlayerState.ownedStocks.any { it.ticker == "AAPL" }
        )
        assertFalse(
            "Target stock must be removed from corporateStockPortfolio",
            result.updatedPlayerState.corporateStockPortfolio.any { it.ticker == "AAPL" }
        )

        // Verify subsidiary added to player's business entities
        assertTrue(
            "Subsidiary must exist in ownedBusinesses or holdingCompanies",
            result.updatedPlayerState.ownedBusinesses.any { it.acquiredStockTicker == "AAPL" } ||
                result.updatedPlayerState.holdingCompanies.any { h -> h.subsidiaries.any { it.acquiredStockTicker == "AAPL" } }
        )

        // Verify breaking news generated
        assertNotNull("Breaking merger news must be generated", result.marketNews)
        assertTrue("News should mention AAPL", result.marketNews?.text?.contains("AAPL") == true)
    }
}
