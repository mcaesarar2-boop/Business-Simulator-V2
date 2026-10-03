package com.example

import com.example.data.LifestyleItem
import com.example.data.defaultLifestyleItems
import org.junit.Assert.*
import org.junit.Test

class CustomLifestyleExpenseTest {

    @Test
    fun testDefaultLifestyleItems_containsPengeluaranKustom() {
        val kustomItems = defaultLifestyleItems.filter { it.tabCategory == "pengeluaran_kustom" }
        assertTrue("defaultLifestyleItems should include pengeluaran_kustom items", kustomItems.isNotEmpty())
        assertTrue("Should have at least 3 default proposals/expenses", kustomItems.size >= 3)

        val golfProposal = kustomItems.find { it.name.contains("Golf") }
        assertNotNull("Should contain golf tournament proposal", golfProposal)
        assertEquals("Proposal Acara & Sponsorship", golfProposal?.sectionName)
        assertEquals(25000L, golfProposal?.price)
        assertFalse(golfProposal?.isOwned ?: true)
        assertFalse(golfProposal?.isActive ?: true)
    }

    @Test
    fun testLifestyleItem_copyAndStateTransitions() {
        val item = LifestyleItem(
            tabCategory = "pengeluaran_kustom",
            sectionName = "Proposal Acara & Sponsorship",
            name = "Donasi Panggung Budaya",
            price = 15000L,
            imgUrl = "https://images.unsplash.com/photo-sample",
            desc = "Pendanaan proposal acara pentas musik pemuda",
            isRecurring = false,
            fundedCount = 0
        )

        assertEquals("pengeluaran_kustom", item.tabCategory)
        assertEquals(15000L, item.price)
        assertEquals(0, item.fundedCount)
        assertFalse(item.isOwned)

        // Simulate funding
        val funded = item.copy(isOwned = true, fundedCount = item.fundedCount + 1)
        assertTrue(funded.isOwned)
        assertEquals(1, funded.fundedCount)

        // Simulate re-funding
        val reFunded = funded.copy(fundedCount = funded.fundedCount + 1)
        assertEquals(2, reFunded.fundedCount)

        // Test recurring configuration
        val recurring = item.copy(isRecurring = true, isActive = true)
        assertTrue(recurring.isRecurring)
        assertTrue(recurring.isActive)
    }
}
