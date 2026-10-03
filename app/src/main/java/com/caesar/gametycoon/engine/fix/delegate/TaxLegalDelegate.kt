package com.caesar.gametycoon.engine.fix.delegate

import com.example.data.ActiveLawsuit
import com.example.data.PlayerState
import kotlin.math.min

/**
 * Phase 3 Refactor: TaxLegalDelegate
 *
 * Extracts corporate tax handling, notary retainer toggles,
 * tax haven status, and lawsuit settlements from GameViewModel.
 */
object TaxLegalDelegate {

    data class TaxPaymentResult(
        val updatedState: PlayerState,
        val isSuccess: Boolean,
        val amountPaid: Long,
        val message: String
    )

    data class LawsuitResolutionResult(
        val updatedState: PlayerState,
        val isSuccess: Boolean,
        val costPaid: Long,
        val message: String
    )

    /**
     * Manually pays accumulated corporate taxes with validation.
     */
    fun payTaxesManually(currentState: PlayerState, amount: Long): TaxPaymentResult {
        if (amount <= 0L) {
            return TaxPaymentResult(currentState, false, 0L, "Nominal pembayaran pajak harus lebih dari nol.")
        }
        if (currentState.taxLegalReport.unpaidTaxes <= 0L) {
            return TaxPaymentResult(currentState, false, 0L, "Tidak ada tunggakan pajak tertunda.")
        }
        if (currentState.cash < amount) {
            return TaxPaymentResult(currentState, false, 0L, "Saldo Kas Utama tidak mencukupi untuk bayar pajak.")
        }

        val payAmount = min(amount, currentState.taxLegalReport.unpaidTaxes)
        val newUnpaid = currentState.taxLegalReport.unpaidTaxes - payAmount
        val newFrozenId = if (newUnpaid <= 0L) null else currentState.taxLegalReport.frozenBusinessId

        val newState = currentState.copy(
            cash = currentState.cash - payAmount,
            corporateTaxPaid = currentState.corporateTaxPaid + payAmount,
            totalTaxPaid = currentState.totalTaxPaid + payAmount,
            taxLegalReport = currentState.taxLegalReport.copy(
                unpaidTaxes = newUnpaid,
                frozenBusinessId = newFrozenId
            )
        )

        return TaxPaymentResult(
            updatedState = newState,
            isSuccess = true,
            amountPaid = payAmount,
            message = "Berhasil menyetor pajak korporasi sebesar USD ${String.format("%,d", payAmount)}."
        )
    }

    /**
     * Resolves an active corporate lawsuit via out-of-court settlement, intern lawyer, or premium firm.
     */
    fun resolveLawsuit(
        currentState: PlayerState,
        lawsuitId: String,
        lawyerTier: Int
    ): LawsuitResolutionResult {
        val lawsuit = currentState.taxLegalReport.activeLawsuits.find { it.id == lawsuitId }
            ?: return LawsuitResolutionResult(currentState, false, 0L, "Gugatan hukum tidak ditemukan.")

        val scale = lawsuit.scaleFactor
        var isWon = false
        val totalCost: Long
        val message: String

        when (lawyerTier) {
            1 -> {
                val lawyerFee = (scale * 0.10).toLong()
                isWon = Math.random() < 0.40
                totalCost = lawyerFee + if (isWon) 0L else scale
                message = if (isWon) "🎉 Sukses! Pengacara Magang berhasil memenangkan perkara."
                else "❌ Pengacara Magang kalah di sidang pengadilan."
            }
            2 -> {
                val lawyerFee = (scale * 0.40).toLong()
                isWon = Math.random() < 0.95
                totalCost = lawyerFee + if (isWon) 0L else scale
                message = if (isWon) "🎉 Sukses! Firma Hukum Premium berhasil memenangkan dan menutup perkara."
                else "❌ Putusan banding hakim menolak pembelaan."
            }
            else -> {
                // Tier 0: Direct Settlement
                totalCost = (scale * 1.50).toLong()
                message = "🤝 Kesepakatan Damai luar pengadilan disetujui."
            }
        }

        if (currentState.cash < totalCost) {
            return LawsuitResolutionResult(currentState, false, 0L, "Saldo Kas Utama tidak mencukupi untuk biaya perkara.")
        }

        val updatedLawsuits = currentState.taxLegalReport.activeLawsuits.filterNot { it.id == lawsuitId }
        val newState = currentState.copy(
            cash = currentState.cash - totalCost,
            taxLegalReport = currentState.taxLegalReport.copy(activeLawsuits = updatedLawsuits)
        )

        return LawsuitResolutionResult(
            updatedState = newState,
            isSuccess = true,
            costPaid = totalCost,
            message = message
        )
    }

    /**
     * Toggles notary retainer service for automatic tax reporting.
     */
    fun toggleNotary(currentState: PlayerState, enabled: Boolean): PlayerState {
        return currentState.copy(
            taxLegalReport = currentState.taxLegalReport.copy(hasNotary = enabled)
        )
    }

    /**
     * Toggles offshore tax haven status (lowers tax rate to 5% with audit risk).
     */
    fun toggleTaxHaven(currentState: PlayerState, enabled: Boolean): PlayerState {
        return currentState.copy(
            taxLegalReport = currentState.taxLegalReport.copy(isTaxHavenActive = enabled)
        )
    }
}
