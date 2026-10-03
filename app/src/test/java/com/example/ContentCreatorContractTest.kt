package com.example

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickRegistry
import com.example.data.ActiveCreatorContract
import com.example.data.ContentStatus
import com.example.data.ContentWork
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import com.example.domain.subsystems.creative.ContentCreatorEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentCreatorContractTest {

    @Test
    fun testContentCreatorContractsCountdownAndPayout() = runBlocking {
        val engine = ContentCreatorEngine()
        val initialContract = ActiveCreatorContract(
            id = "contract_emirates_1",
            brandName = "Emirates Worldwide",
            tierLevel = 7,
            categoryTag = "Luxury Travel",
            monthlyPayout = 835_000L,
            totalMonths = 24,
            remainingMonths = 24,
            totalPaidSoFar = 0L
        )

        val initialContent = ContentWork(
            id = "work_janji_1",
            title = "Janji",
            budget = 50_000L,
            engagementScore = 85,
            status = ContentStatus.LICENSED,
            monthlyRoyalty = 16_500L,
            acquiredByPH = "Disney+ Originals",
            contractDurationMonths = 36,
            remainingContractMonths = 36
        )

        val ccBusiness = OwnedBusiness(
            instanceId = "cc_biz_1",
            catalogId = "content_creator",
            customName = "Studio Content Creator",
            contentCreatorCash = 100_000L,
            contentCreatorContracts = listOf(initialContract),
            contentPortfolio = listOf(initialContent)
        )

        val state = PlayerState(
            ownedBusinesses = listOf(ccBusiness)
        )

        val event = MonthTickEvent(
            currentMonth = 2,
            currentYear = 2026,
            totalMonthsElapsed = 1
        )

        val result = engine.onMonthlyTick(event, state)
        val nextState = result.stateModifier(state)
        val updatedBiz = nextState.ownedBusinesses.first { it.instanceId == "cc_biz_1" }

        // Expected brand contract countdown
        assertEquals(1, updatedBiz.contentCreatorContracts.size)
        val updatedContract = updatedBiz.contentCreatorContracts.first()
        assertEquals(23, updatedContract.remainingMonths)
        assertEquals(835_000L, updatedContract.totalPaidSoFar)

        // Expected portfolio licensing contract countdown
        assertEquals(1, updatedBiz.contentPortfolio.size)
        val updatedWork = updatedBiz.contentPortfolio.first()
        assertEquals(35, updatedWork.remainingContractMonths)
        assertEquals(ContentStatus.LICENSED, updatedWork.status)

        // Expected cash increment: 100_000 + 835_000 + 16_500 = 951_500
        assertEquals(951_500L, updatedBiz.contentCreatorCash)
        assertEquals(835_000L + 16_500L, result.revenue)
    }

    @Test
    fun testContractExpirationWhenDurationReachesZero() = runBlocking {
        val engine = ContentCreatorEngine()
        val expiringContract = ActiveCreatorContract(
            id = "contract_expiring",
            brandName = "Local Brand",
            tierLevel = 1,
            categoryTag = "Retail",
            monthlyPayout = 50_000L,
            totalMonths = 3,
            remainingMonths = 1,
            totalPaidSoFar = 100_000L
        )

        val expiringWork = ContentWork(
            id = "work_expiring",
            title = "Short Film",
            budget = 10_000L,
            engagementScore = 70,
            status = ContentStatus.LICENSED,
            monthlyRoyalty = 5_000L,
            contractDurationMonths = 12,
            remainingContractMonths = 1
        )

        val ccBusiness = OwnedBusiness(
            instanceId = "cc_biz_2",
            catalogId = "content_creator",
            customName = "Creator Studio",
            contentCreatorContracts = listOf(expiringContract),
            contentPortfolio = listOf(expiringWork)
        )

        val state = PlayerState(ownedBusinesses = listOf(ccBusiness))
        val event = MonthTickEvent(currentMonth = 3, currentYear = 2026, totalMonthsElapsed = 2)

        val result = engine.onMonthlyTick(event, state)
        val nextState = result.stateModifier(state)
        val updatedBiz = nextState.ownedBusinesses.first { it.instanceId == "cc_biz_2" }

        // Expired contract should be removed from active contracts
        assertTrue(updatedBiz.contentCreatorContracts.isEmpty())

        // Expired licensing should revert to AVAILABLE
        val updatedWork = updatedBiz.contentPortfolio.first()
        assertEquals(ContentStatus.AVAILABLE, updatedWork.status)
        assertEquals(null, updatedWork.remainingContractMonths)
    }
}
