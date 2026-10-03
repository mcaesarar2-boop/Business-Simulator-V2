package com.example.data.treasury

/**
 * Definition of a Fixed Income Mutual Fund (Reksadana Pendapatan Tetap / RDPT)
 * specifically designed for corporate liquidity and institutional treasury management.
 */
data class FixedIncomeFund(
    val id: String = "premier_rdpt",
    val name: String = "Premier Corporate Fixed Income Fund",
    val investmentManager: String = "Schroder & Batavia Institutional Asset Management",
    val allocationDebtPaperPct: Double = 85.0, // 80%+ underlying debt instruments & sovereign bonds
    val allocationMoneyMarketPct: Double = 15.0, // Liquidity buffer
    val annualManagementFeePct: Double = 0.75, // 0.75% APR auto-deducted
    val currentNavPerUnit: Double = 1500.0,
    val initialNavPerUnit: Double = 1000.0,
    val ytdReturnPct: Double = 8.4,
    val description: String = "Instrumen reksadana pendapatan tetap dengan alokasi dominan pada obligasi negara dan sukuk korporasi berperingkat tinggi. Likuiditas harian tanpa masa penguncian (lock-up)."
)
