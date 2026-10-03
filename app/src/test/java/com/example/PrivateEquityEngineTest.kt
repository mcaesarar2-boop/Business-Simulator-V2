package com.example

import com.example.data.PlayerState
import com.example.privateequity.data.PrivateEquityRepository
import com.example.privateequity.engine.PrivateEquityEngine
import com.example.privateequity.model.FundingType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivateEquityEngineTest {

    @Test
    fun testLoanDisbursesToCompanyCashNotPrivateBalance() {
        val initialCash = 5000L
        val initialPrivateBalance = 25000L
        val initialEquity = 100.0

        val state = PlayerState(
            cash = initialCash,
            privateBalance = initialPrivateBalance,
            playerEquityShare = initialEquity
        )

        val loanAmount = 100_000L
        val result = PrivateEquityEngine.applyForInvestorLoan(
            state = state,
            sectorName = "Infrastruktur & Energi",
            loanAmount = loanAmount,
            tenorMonths = 48,
            interestRate = 0.08,
            dilutionMultiplier = 1.8,
            fundingType = FundingType.DEBT
        )

        assertTrue(result.isSuccess)
        assertNotNull(result.updatedState)

        val updatedState = result.updatedState!!

        // VERIFY: Funds MUST go to cash (Kas Utama), NOT privateBalance!
        assertEquals(initialCash + loanAmount, updatedState.cash)
        assertEquals(initialPrivateBalance, updatedState.privateBalance)

        // For DEBT, equity remains unchanged (0% dilution)
        assertEquals(100.0, updatedState.playerEquityShare, 0.001)

        // Active loans list must contain the loan
        assertEquals(1, updatedState.activeInvestorsLoans.size)
        val loan = updatedState.activeInvestorsLoans[0]
        assertEquals("Infrastruktur & Energi", loan.sectorName)
        assertEquals(48, loan.remainingMonths)
        assertEquals(FundingType.DEBT, loan.fundingType)
    }

    @Test
    fun testEquityDilutionAndControlThreshold() {
        val state = PlayerState(
            cash = 10_000L,
            privateBalance = 0L,
            playerEquityShare = 55.0
        )

        // Attempting loan that dilutes > 5% should fail because player equity would drop below 51%
        val result = PrivateEquityEngine.applyForInvestorLoan(
            state = state,
            sectorName = "Infrastruktur & Energi",
            loanAmount = 100_000L,
            tenorMonths = 48,
            interestRate = 0.08,
            dilutionMultiplier = 1.8,
            fundingType = FundingType.EQUITY
        )

        assertFalse(result.isSuccess)
        assertTrue(result.message.contains("51%"))
    }

    @Test
    fun testMonthlyDebtTickProcessing() {
        val state = PlayerState(
            cash = 50_000L,
            privateBalance = 10_000L
        )

        val loanResult = PrivateEquityEngine.applyForInvestorLoan(
            state = state,
            sectorName = "Konsumer & Retail",
            loanAmount = 12_000L,
            tenorMonths = 12,
            interestRate = 0.12,
            dilutionMultiplier = 1.2,
            fundingType = FundingType.DEBT
        )

        val stateWithLoan = loanResult.updatedState!!
        val activeLoan = stateWithLoan.activeInvestorsLoans[0]
        val expectedMonthlyPayment = activeLoan.monthlyPayment

        val tickResult = PrivateEquityEngine.processMonthlyDebtTick(
            state = stateWithLoan,
            availableCorpCash = 62_000L
        )

        assertEquals(expectedMonthlyPayment, tickResult.totalPaid)
        assertEquals(1, tickResult.updatedLoans.size)
        assertEquals(11, tickResult.updatedLoans[0].remainingMonths)
    }
}
