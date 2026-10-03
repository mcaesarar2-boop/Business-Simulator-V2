package com.example.privateequity.data

import com.example.data.PlayerState
import com.example.data.calculateMegaHoldingValuation
import com.example.privateequity.model.SectorOffer

/**
 * Repository providing sector catalogs and dynamic loan limit configurations for Private Equity.
 */
object PrivateEquityRepository {

    val defaultSectors: List<SectorOffer> = listOf(
        SectorOffer(
            name = "Infrastruktur & Energi",
            interestRate = 0.08,
            tenor = 48,
            dilutionMultiplier = 1.8,
            description = "Suku Bunga Rendah, Tenor Sangat Panjang, Dilusi Tinggi. Sempurna untuk proyek energi & utilitas skala masif."
        ),
        SectorOffer(
            name = "Finansial & Fintech",
            interestRate = 0.18,
            tenor = 24,
            dilutionMultiplier = 1.4,
            description = "Tenor sedang, suku bunga tinggi untuk penetrasi likuiditas instan industri keuangan digital modern."
        ),
        SectorOffer(
            name = "Konsumer & Retail",
            interestRate = 0.12,
            tenor = 12,
            dilutionMultiplier = 1.2,
            description = "Tenor pendek 12 bulan dengan dilusi paling minimal. Tepat untuk pendanaan ekspansi toko fisik retail Anda."
        ),
        SectorOffer(
            name = "Teknologi & AI",
            interestRate = 0.15,
            tenor = 36,
            dilutionMultiplier = 1.6,
            description = "High Risk, High Return. Melepas kendali sedang dengan tenor 36 bulan untuk mendominasi industri otomasi AI."
        )
    )

    /**
     * Calculates total business valuation for borrowing power.
     */
    fun getBusinessValuation(state: PlayerState): Long {
        return state.calculateMegaHoldingValuation().coerceAtLeast(100_000L)
    }

    /**
     * Dynamic max loan limit based on 20% of valuation, capped between 100k and 50M.
     */
    fun getMaxLoanLimit(businessValuation: Long): Long {
        return (businessValuation * 0.20).toLong().coerceIn(100_000L, 50_000_000L)
    }
}
