package com.caesar.gametycoon.engine.fix

import com.example.aicloud.engine.AiCloudTickSubscriber
import com.example.core.engine.CoreBusinessEngine
import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickRegistry
import com.example.data.PlayerState
import com.example.domain.subsystems.aviation.AviationEngine
import com.example.domain.subsystems.banking.BankingSubsystemEngine
import com.example.domain.subsystems.construction.ConstructionEngine
import com.example.domain.subsystems.creative.ContentCreatorEngine
import com.example.domain.subsystems.creative.FilmProductionEngine
import com.example.domain.subsystems.creative.StreamingEngine
import com.example.domain.subsystems.hospitality.HospitalitySubsystemEngine
import com.example.domain.subsystems.logistics.LogisticsEngine
import com.example.domain.subsystems.sports.FootballClubEngine
import com.example.domain.subsystems.themepark.ThemeParkSubsystemEngine
import com.example.publisher.engine.GamePublisherTickSubscriber
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GhostDeductionDiagnosticTest {

    @Test
    fun diagnoseFreshGameTick() = runBlocking {
        val registry = MonthlyTickRegistry()
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val footballClubManager = com.example.data.FootballClubManager(context)
        val subscribers = listOf(
            CoreBusinessEngine(),
            ContentCreatorEngine(),
            StreamingEngine(),
            FilmProductionEngine(),
            BankingSubsystemEngine(),
            ThemeParkSubsystemEngine(),
            HospitalitySubsystemEngine(),
            FootballClubEngine(footballClubManager),
            AviationEngine(),
            LogisticsEngine(),
            ConstructionEngine(),
            GamePublisherTickSubscriber(),
            AiCloudTickSubscriber()
        )
        subscribers.forEach { registry.registerSubscriber(it) }

        val freshState = PlayerState(cash = 5000, netWorth = 5000)
        val tickEvent = MonthTickEvent(
            currentMonth = 2,
            currentYear = 1,
            totalMonthsElapsed = 2,
            isOfflineCatchup = false,
            deltaMonths = 1
        )

        for (sub in subscribers) {
            val res = sub.onMonthlyTick(tickEvent, freshState)
            println("DIAGNOSTIC: Subscriber '${sub.subscriberId}' -> revenue=${res.revenue}, expenses=${res.expenses}, dividend=${res.dividendToGlobal}")
        }

        val (nextState, financials) = registry.dispatchTick(tickEvent, freshState)
        println("DIAGNOSTIC TOTAL: revenue=${financials.totalRevenue}, expenses=${financials.totalExpenses}, netIncome=${financials.netIncome}")
        println("DIAGNOSTIC STATE: cash=${nextState.cash}, privateBalance=${nextState.privateBalance}")

        org.junit.Assert.assertEquals(0L, financials.totalRevenue)
        org.junit.Assert.assertEquals(0L, financials.totalExpenses)
        org.junit.Assert.assertEquals(0L, financials.netIncome)
        org.junit.Assert.assertEquals(5000L, nextState.cash)
    }
}
