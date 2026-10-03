package com.example.core.treasury

import com.example.data.treasury.BondHolding
import com.example.data.treasury.BondType
import com.example.data.treasury.CorporateBond
import com.example.data.treasury.CreditRating
import com.example.data.treasury.FixedIncomeFund
import kotlin.math.max
import kotlin.random.Random

/**
 * Pure calculation engine for corporate treasury bond mathematics,
 * secondary market pricing, and mutual fund NAV dynamics.
 */
object HoldingInvestmentEngine {

    /**
     * Calculates the aggregate monthly coupon cash payout for all active, non-matured bonds.
     */
    fun calculateMonthlyCouponPayout(holdings: List<BondHolding>): Long {
        return holdings.filter { !it.isMatured }.sumOf { it.monthlyCouponAmount }
    }

    /**
     * Calculates steady monthly NAV appreciation for RDPT holdings.
     * Underlying bonds yield approx 6.5% - 9% annualized net of fees.
     */
    fun calculateRdptNavGrowth(
        currentNav: Double,
        marketBenchmarkRate: Double = 0.075,
        annualFeeRate: Double = 0.0075,
        randomFactor: Double = (Random.nextDouble(-0.001, 0.002))
    ): Double {
        val netAnnualRate = (marketBenchmarkRate - annualFeeRate + randomFactor).coerceAtLeast(0.02)
        val monthlyRate = netAnnualRate / 12.0
        val newNav = currentNav * (1.0 + monthlyRate)
        return String.format(java.util.Locale.US, "%.4f", newNav).toDouble()
    }

    /**
     * Computes the secondary market liquidation value of an active bond if liquidated before maturity.
     * Price Formula: Bond Principal * [1 + (Holding Coupon - Benchmark Rate) * (Remaining Term in Years)]
     * Applies a small liquidity haircut (1.5% - 2.5%) for early exit.
     */
    fun calculateBondSecondaryMarketValue(
        holding: BondHolding,
        currentMarketInterestRate: Double = 0.072
    ): Long {
        if (holding.isMatured) {
            return holding.principal
        }

        val remainingYears = holding.monthsRemaining.toDouble() / 12.0
        val couponSpread = holding.annualCouponRate - currentMarketInterestRate
        
        // Capital gain or loss factor based on interest rate spread
        val interestRateFactor = 1.0 + (couponSpread * remainingYears * 0.8)
        
        // Credit rating adjustment
        val ratingFactor = when (holding.rating) {
            CreditRating.AAA -> 0.995 // 0.5% spread
            CreditRating.AA -> 0.985  // 1.5% spread
            CreditRating.BBB -> 0.965 // 3.5% discount due to lower liquidity
        }

        val fairValue = holding.principal.toDouble() * interestRateFactor * ratingFactor
        return max(1_000L, fairValue.toLong())
    }

    /**
     * Curated catalog of sovereign and corporate bonds for corporate treasury.
     */
    fun getDefaultBondCatalogue(): List<CorporateBond> {
        return listOf(
            CorporateBond(
                id = "sbn_ori_026",
                name = "Surat Berharga Negara ORI-026",
                issuer = "Kementerian Keuangan RI",
                type = BondType.SOVEREIGN,
                rating = CreditRating.AAA,
                annualCouponRate = 0.068, // 6.8% p.a.
                tenureMonths = 6,
                minInvestment = 10_000L,
                description = "Obligasi ritel negara dengan jaminan 100% pokok dan kupon oleh UU APBN."
            ),
            CorporateBond(
                id = "sbn_fr_benchmark",
                name = "Fixed Rate Benchmark FR-0102",
                issuer = "Bank Sentral & Treasury Negara",
                type = BondType.SOVEREIGN,
                rating = CreditRating.AAA,
                annualCouponRate = 0.073, // 7.3% p.a.
                tenureMonths = 12,
                minInvestment = 50_000L,
                description = "Seri obligasi negara acuan pasar sekunder likuiditas tinggi dengan kupon tetap."
            ),
            CorporateBond(
                id = "us_treasury_note",
                name = "US Treasury 2-Yr Yield Note",
                issuer = "United States Federal Reserve",
                type = BondType.SOVEREIGN,
                rating = CreditRating.AAA,
                annualCouponRate = 0.065, // 6.5% p.a.
                tenureMonths = 24,
                minInvestment = 100_000L,
                description = "Instrumen surat utang global safe-haven berdenominasi USD rating AAA."
            ),
            CorporateBond(
                id = "corp_telco_infra",
                name = "Telco Infra Nusantara Sukuk II",
                issuer = "PT Telekomunikasi Digital Tbk",
                type = BondType.CORPORATE,
                rating = CreditRating.AA,
                annualCouponRate = 0.095, // 9.5% p.a.
                tenureMonths = 12,
                minInvestment = 25_000L,
                description = "Sukuk korporasi pembiayaan ekspansi fiber optic nasional dengan rating AA stabil."
            ),
            CorporateBond(
                id = "corp_tech_global",
                name = "TechGlobal Cloud Expansion Bond",
                issuer = "TechGlobal Corporation AAA",
                type = BondType.CORPORATE,
                rating = CreditRating.AA,
                annualCouponRate = 0.102, // 10.2% p.a.
                tenureMonths = 18,
                minInvestment = 50_000L,
                description = "Obligasi korporat pendanaan data center hyperscale berimbal hasil kompetitif."
            ),
            CorporateBond(
                id = "corp_energy_highyield",
                name = "Mega Energy Transition Bond",
                issuer = "Nusantara Energy Resources BBB",
                type = BondType.CORPORATE,
                rating = CreditRating.BBB,
                annualCouponRate = 0.138, // 13.8% p.a.
                tenureMonths = 24,
                minInvestment = 100_000L,
                description = "High yield corporate bond energi terbarukan dengan kupon premium rating BBB."
            ),
            CorporateBond(
                id = "corp_prop_archipelago",
                name = "Property Superblock Commercial Bond",
                issuer = "Archipelago Realty Development",
                type = BondType.CORPORATE,
                rating = CreditRating.BBB,
                annualCouponRate = 0.145, // 14.5% p.a.
                tenureMonths = 12,
                minInvestment = 20_000L,
                description = "Obligasi proyek kawasan industri dan komersial dengan kupon bulanan tinggi."
            )
        )
    }

    /**
     * Default Fixed Income Mutual Fund instance.
     */
    fun getDefaultFund(): FixedIncomeFund {
        return FixedIncomeFund()
    }
}
