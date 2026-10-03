package com.example

import com.example.data.PlayerState
import com.example.privateequity.capitalinjection.CapitalInjectionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CapitalInjectionEngineTest {

    @Test
    fun testSuccessfulCapitalInjectionOneWay() {
        val initialCash = 100_000L
        val initialPrivateBalance = 500_000L
        val injectionAmount = 200_000L

        val state = PlayerState(
            cash = initialCash,
            privateBalance = initialPrivateBalance
        )

        val result = CapitalInjectionEngine.injectCapitalToCompany(
            state = state,
            amount = injectionAmount
        )

        assertTrue(result.isSuccess)
        assertNotNull(result.updatedState)

        val updated = result.updatedState!!

        // VERIFY: Private Balance reduced by amount, Company Cash increased by amount
        assertEquals(initialPrivateBalance - injectionAmount, updated.privateBalance)
        assertEquals(initialCash + injectionAmount, updated.cash)

        // VERIFY: Ledger record recorded
        assertTrue(updated.privateLedgerHistory.any { it.title.contains("Suntikan Modal") && it.amount == injectionAmount })
    }

    @Test
    fun testCannotInjectMoreThanPrivateBalance() {
        val state = PlayerState(
            cash = 50_000L,
            privateBalance = 100_000L
        )

        val result = CapitalInjectionEngine.injectCapitalToCompany(
            state = state,
            amount = 150_000L
        )

        assertFalse(result.isSuccess)
        assertTrue(result.message.contains("tidak mencukupi"))
    }

    @Test
    fun testCannotInjectZeroOrNegativeAmount() {
        val state = PlayerState(
            cash = 50_000L,
            privateBalance = 100_000L
        )

        val resultZero = CapitalInjectionEngine.injectCapitalToCompany(state, 0L)
        assertFalse(resultZero.isSuccess)

        val resultNegative = CapitalInjectionEngine.injectCapitalToCompany(state, -10_000L)
        assertFalse(resultNegative.isSuccess)
    }
}
