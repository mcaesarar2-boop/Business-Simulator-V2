package com.example.data.treasury

import com.example.core.treasury.HoldingInvestmentEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Repository responsible for managing Corporate Treasury Investment instruments:
 * 1. Corporate & Sovereign Bonds (Bursa Obligasi)
 * 2. Fixed Income Mutual Funds (RDPT)
 */
class HoldingInvestmentRepository {

    private val _availableBonds = MutableStateFlow<List<CorporateBond>>(HoldingInvestmentEngine.getDefaultBondCatalogue())
    val availableBonds: StateFlow<List<CorporateBond>> = _availableBonds.asStateFlow()

    private val _fundCatalog = MutableStateFlow(HoldingInvestmentEngine.getDefaultFund())
    val fundCatalog: StateFlow<FixedIncomeFund> = _fundCatalog.asStateFlow()

    /**
     * Executes the purchase of a Sovereign or Corporate bond for the holding portfolio.
     */
    fun purchaseBond(
        bond: CorporateBond,
        investmentAmount: Long,
        currentMonth: Int,
        currentYear: Int,
        currentHoldings: List<BondHolding>,
        availableCash: Long
    ): Result<Pair<List<BondHolding>, Long>> {
        if (investmentAmount < bond.minInvestment) {
            return Result.failure(
                IllegalArgumentException("Minimal pembelian obligasi ini adalah $${String.format(java.util.Locale.US, "%,d", bond.minInvestment)}.")
            )
        }
        if (availableCash < investmentAmount) {
            return Result.failure(
                IllegalStateException("Kas Holding tidak mencukupi untuk membeli obligasi ini.")
            )
        }

        val newHolding = BondHolding(
            id = UUID.randomUUID().toString(),
            bondId = bond.id,
            name = bond.name,
            issuer = bond.issuer,
            type = bond.type,
            rating = bond.rating,
            principal = investmentAmount,
            annualCouponRate = bond.annualCouponRate,
            totalTenureMonths = bond.tenureMonths,
            monthsRemaining = bond.tenureMonths,
            purchasedMonth = currentMonth,
            purchasedYear = currentYear,
            totalCouponEarned = 0L
        )

        val updatedHoldings = currentHoldings + newHolding
        return Result.success(Pair(updatedHoldings, investmentAmount))
    }

    /**
     * Early liquidation on the secondary market before maturity.
     */
    fun liquidateBondEarly(
        holdingId: String,
        currentHoldings: List<BondHolding>,
        marketBenchmarkRate: Double = 0.072
    ): Result<Pair<List<BondHolding>, Long>> {
        val target = currentHoldings.find { it.id == holdingId }
            ?: return Result.failure(IllegalArgumentException("Obligasi tidak ditemukan di portofolio."))

        val cashReceived = HoldingInvestmentEngine.calculateBondSecondaryMarketValue(target, marketBenchmarkRate)
        val updatedHoldings = currentHoldings.filter { it.id != holdingId }

        return Result.success(Pair(updatedHoldings, cashReceived))
    }

    /**
     * Claim principal back 100% when bond has matured.
     */
    fun claimMaturedBond(
        holdingId: String,
        currentHoldings: List<BondHolding>
    ): Result<Pair<List<BondHolding>, Long>> {
        val target = currentHoldings.find { it.id == holdingId }
            ?: return Result.failure(IllegalArgumentException("Obligasi tidak ditemukan di portofolio."))

        if (!target.isMatured) {
            return Result.failure(IllegalStateException("Obligasi belum jatuh tempo. Gunakan opsi jual pasar sekunder jika ingin likuidasi dini."))
        }

        val principalToReturn = target.principal
        val updatedHoldings = currentHoldings.filter { it.id != holdingId }

        return Result.success(Pair(updatedHoldings, principalToReturn))
    }

    /**
     * Top-up money into Fixed Income Mutual Fund (RDPT) at current NAV.
     */
    fun topUpRdpt(
        fund: FixedIncomeFund,
        currentNav: Double,
        depositAmount: Long,
        currentHolding: FundHolding,
        availableCash: Long
    ): Result<Pair<FundHolding, Long>> {
        if (depositAmount <= 0L) {
            return Result.failure(IllegalArgumentException("Nominal deposit harus lebih dari 0."))
        }
        if (availableCash < depositAmount) {
            return Result.failure(IllegalStateException("Kas Holding tidak mencukupi untuk top-up RDPT."))
        }

        val acquiredUnits = depositAmount.toDouble() / currentNav
        val updatedHolding = currentHolding.copy(
            fundId = fund.id,
            totalUnits = currentHolding.totalUnits + acquiredUnits,
            totalInvestedCost = currentHolding.totalInvestedCost + depositAmount
        )

        return Result.success(Pair(updatedHolding, depositAmount))
    }

    /**
     * Redeem / Cash-out units from Fixed Income Mutual Fund (RDPT) with 0% lock-up penalty.
     */
    fun redeemRdpt(
        unitsToRedeem: Double,
        currentNav: Double,
        currentHolding: FundHolding
    ): Result<Pair<FundHolding, Long>> {
        if (unitsToRedeem <= 0.0 || unitsToRedeem > currentHolding.totalUnits) {
            return Result.failure(IllegalArgumentException("Jumlah unit yang dicairkan tidak valid."))
        }

        val grossCash = (unitsToRedeem * currentNav).toLong()
        val remainingUnits = (currentHolding.totalUnits - unitsToRedeem).coerceAtLeast(0.0)

        // Proportionally reduce invested cost
        val costDeduction = if (currentHolding.totalUnits > 0.0) {
            (currentHolding.totalInvestedCost * (unitsToRedeem / currentHolding.totalUnits)).toLong()
        } else 0L

        val updatedHolding = currentHolding.copy(
            totalUnits = remainingUnits,
            totalInvestedCost = (currentHolding.totalInvestedCost - costDeduction).coerceAtLeast(0L),
            totalRedeemedCash = currentHolding.totalRedeemedCash + grossCash
        )

        return Result.success(Pair(updatedHolding, grossCash))
    }

    /**
     * Advances monthly tick for treasury holdings:
     * 1. Collects bond coupon payments.
     * 2. Decrements bond maturity months.
     * 3. Appreciates RDPT NAV.
     */
    fun processMonthlyTick(
        bonds: List<BondHolding>,
        currentNav: Double
    ): MonthlyTreasuryTickResult {
        var totalCouponCollected = 0L
        val updatedBonds = bonds.map { bond ->
            if (!bond.isMatured) {
                val coupon = bond.monthlyCouponAmount
                totalCouponCollected += coupon
                bond.copy(
                    monthsRemaining = (bond.monthsRemaining - 1).coerceAtLeast(0),
                    totalCouponEarned = bond.totalCouponEarned + coupon
                )
            } else {
                bond
            }
        }

        val newNav = HoldingInvestmentEngine.calculateRdptNavGrowth(currentNav)

        return MonthlyTreasuryTickResult(
            updatedBonds = updatedBonds,
            totalCouponCollected = totalCouponCollected,
            updatedNav = newNav
        )
    }
}

data class MonthlyTreasuryTickResult(
    val updatedBonds: List<BondHolding>,
    val totalCouponCollected: Long,
    val updatedNav: Double
)
