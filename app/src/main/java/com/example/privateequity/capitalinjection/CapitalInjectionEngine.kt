package com.example.privateequity.capitalinjection

import com.example.data.MarketNews
import com.example.data.PlayerState
import com.example.data.PrivateLedgerRecord
import com.example.ui.formatCurrencyRingkas

/**
 * Domain engine responsible for owner capital injection (Suntik Modal Satu Arah: Kas Pribadi -> Kas Utama).
 * Strictly allows one-way liquidity transfer from the player's personal wealth (Family Office)
 * directly into the corporate operating cash (Cash Utama) to fund business acquisitions and growth.
 */
object CapitalInjectionEngine {

    fun injectCapitalToCompany(
        state: PlayerState,
        amount: Long
    ): CapitalInjectionResult {
        if (amount <= 0L) {
            return CapitalInjectionResult(
                isSuccess = false,
                message = "Jumlah dana yang disuntikkan harus lebih besar dari $0."
            )
        }

        if (state.privateBalance < amount) {
            return CapitalInjectionResult(
                isSuccess = false,
                message = "Kas pribadi (Family Office) Anda tidak mencukupi untuk melakukan suntikan modal sebesar ${formatCurrencyRingkas(amount, false)}."
            )
        }

        val newPrivateBalance = state.privateBalance - amount
        val newCompanyCash = state.cash + amount

        val ledgerRecord = PrivateLedgerRecord(
            monthTick = state.inGameMonth,
            title = "Suntikan Modal Pemilik (Capital Injection ke Kas Utama)",
            amount = amount,
            isIncome = false
        )
        val updatedLedger = (listOf(ledgerRecord) + state.privateLedgerHistory).take(200)

        val newsItem = MarketNews(
            id = "capital_injection_${System.currentTimeMillis()}",
            text = "CAPITAL INJECTION: Pemilik menyuntikkan modal segar sebesar ${formatCurrencyRingkas(amount, false)} dari dana pribadi ke Kas Utama Perusahaan.",
            type = "BULL"
        )

        val updatedState = state.copy(
            privateBalance = newPrivateBalance,
            cash = newCompanyCash,
            privateLedgerHistory = updatedLedger
        )

        return CapitalInjectionResult(
            isSuccess = true,
            message = "Berhasil menyuntikkan modal sebesar ${formatCurrencyRingkas(amount, false)} ke Kas Utama Perusahaan!",
            updatedState = updatedState,
            newsFeedItem = newsItem
        )
    }
}
