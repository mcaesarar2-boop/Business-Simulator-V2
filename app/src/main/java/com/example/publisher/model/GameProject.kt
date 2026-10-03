package com.example.publisher.model

import java.util.UUID

/**
 * Funding tiers available for indie developers and in-house productions.
 */
enum class FundingTier(
    val displayName: String,
    val baseBudget: Long,
    val baseDevMonths: Int,
    val riskDescription: String,
    val potentialDescription: String,
    val talentThresholdBonus: Double
) {
    BOOTSTRAP(
        displayName = "Bootstrap",
        baseBudget = 50_000L,
        baseDevMonths = 6,
        riskDescription = "Resiko rendah, tim kecil dan lincah, overhead minimal.",
        potentialDescription = "Penjualan stabil, turnaround cepat, margin keuntungan sehat.",
        talentThresholdBonus = 0.8
    ),
    STANDARD(
        displayName = "Standard Indie",
        baseBudget = 250_000L,
        baseDevMonths = 10,
        riskDescription = "Resiko seimbang, grafis 2D/3D profesional, kompetitif di pasar global.",
        potentialDescription = "Potensi breakout hit, daya tarik multi-platform tinggi, profit besar.",
        talentThresholdBonus = 1.0
    ),
    TRIPLE_I(
        displayName = "Triple-I Premium",
        baseBudget = 1_200_000L,
        baseDevMonths = 16,
        riskDescription = "Modal raksasa, burn rate tinggi, fatal jika kualitas visual/gameplay gagal.",
        potentialDescription = "Kandidat Game of the Year, jutaan kopi terjual, pendapatan blockbuster.",
        talentThresholdBonus = 1.35
    );

    val baseDevWeeks: Int get() = baseDevMonths * 4
}

/**
 * Target distribution platforms and stores.
 */
enum class TargetPlatform(
    val displayName: String,
    val storeCut: Double, // e.g. 0.30 for 30% cut
    val organicReachMultiplier: Double,
    val costMultiplier: Double,
    val timeMultiplier: Double,
    val isConsole: Boolean
) {
    PC_STEAM(
        displayName = "Steam",
        storeCut = 0.30,
        organicReachMultiplier = 1.60,
        costMultiplier = 1.0,
        timeMultiplier = 1.0,
        isConsole = false
    ),
    PC_EPIC(
        displayName = "Epic Games Store",
        storeCut = 0.12,
        organicReachMultiplier = 0.85,
        costMultiplier = 1.0,
        timeMultiplier = 1.0,
        isConsole = false
    ),
    PLAYSTATION_5(
        displayName = "PlayStation 5",
        storeCut = 0.30,
        organicReachMultiplier = 1.35,
        costMultiplier = 0.30, // +30%
        timeMultiplier = 0.25, // +25%
        isConsole = true
    ),
    XBOX_SERIES_X(
        displayName = "Xbox Series X|S",
        storeCut = 0.30,
        organicReachMultiplier = 1.25,
        costMultiplier = 0.25, // +25%
        timeMultiplier = 0.20, // +20%
        isConsole = true
    ),
    NINTENDO_SWITCH(
        displayName = "Nintendo Switch",
        storeCut = 0.30,
        organicReachMultiplier = 1.40,
        costMultiplier = 0.35, // +35%
        timeMultiplier = 0.30, // +30%
        isConsole = true
    )
}

/**
 * Types of creative direction adjustments players can inject during active development.
 */
enum class CreativeAdjustmentType(
    val title: String,
    val costBonus: Long,
    val devTimeBonusMonths: Int,
    val hypeBoost: Double,
    val bugRiskDelta: Double,
    val description: String
) {
    FEATURE_CREEP(
        title = "Inject Feature Creep",
        costBonus = 45_000L,
        devTimeBonusMonths = 1,
        hypeBoost = 35.0,
        bugRiskDelta = +12.0,
        description = "Tambahkan fitur & mekanik mutakhir! Menambah durasi & budget, tapi melipatgandakan hype pemain."
    ),
    QA_SPRINT(
        title = "QA & Polish Sprint",
        costBonus = 25_000L,
        devTimeBonusMonths = 1,
        hypeBoost = 5.0,
        bugRiskDelta = -28.0,
        description = "Sewa tim QA bug-squasher profesional. Memangkas bug porting secara masif dan menjaga review rilis."
    ),
    COMMUNITY_PLAYTEST(
        title = "Public Demo & Playtest",
        costBonus = 15_000L,
        devTimeBonusMonths = 0,
        hypeBoost = 22.0,
        bugRiskDelta = -14.0,
        description = "Rilis demo publik di festival game untuk menguji respon komunitas dan mendeteksi bug kritis."
    ),
    EMERGENCY_CRUNCH(
        title = "Sprint Crunch Mode",
        costBonus = 20_000L,
        devTimeBonusMonths = -1,
        hypeBoost = 0.0,
        bugRiskDelta = +22.0,
        description = "Percepat jadwal rilis 1 bulan lebih awal. Resiko bug tinggi yang berpotensi menurunkan skor review!"
    );

    val devTimeBonusWeeks: Int get() = devTimeBonusMonths * 4
}

/**
 * Represents a game under development or already released into the market.
 */
data class GameProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val genre: GameGenre,
    val studioId: String,
    val studioName: String,
    val fundingTier: FundingTier,
    val targetPlatforms: Set<TargetPlatform> = setOf(TargetPlatform.PC_STEAM),
    
    // In-House vs Third Party Indie
    val isInHouseProject: Boolean = false,
    
    // Development Metrics in Months
    val currentProgress: Float = 0.0f, // 0.0 to 100.0
    val totalBudget: Long = fundingTier.baseBudget,
    val devTimeMonths: Int = fundingTier.baseDevMonths,
    val currentMonthInDev: Int = 0,
    val bugRiskScore: Double = 5.0, // 0.0 to 100.0
    val hypeScore: Double = 25.0,
    val qaInvestment: Long = 0L,
    val creativeAdjustmentsCount: Int = 0,
    
    // Release Status & Financials
    val isReleased: Boolean = false,
    val releaseMonth: Int = 0,
    val lifetimeRevenue: Long = 0L,
    val totalSales: Long = 0L, // Copies sold
    val activePlayers: Long = 0L,
    val reviewScore: Int = 0, // Metacritic style 0-100
    val publisherRevenueShare: Double = 0.60, // e.g., 0.6 for 60%, or 1.0 for in-house
    val publisherLifetimeProfit: Long = 0L,
    
    // Post-Release Metrics
    val monthlyRevenue: Long = 0L,
    val patchCount: Int = 0,
    val posterImageUrl: String = ""
) {
    // Backwards compatibility helpers
    val devTimeWeeks: Int get() = devTimeMonths * 4
    val currentWeek: Int get() = currentMonthInDev * 4
    val weeklyRevenue: Long get() = monthlyRevenue / 4
}
