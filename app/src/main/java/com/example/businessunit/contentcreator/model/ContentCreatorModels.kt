package com.example.businessunit.contentcreator.model

import java.util.UUID

enum class ContentType(val displayName: String, val categoryTag: String) {
    FEATURE_FILM("Full-Length Movie", "Film Panjang Bioskop"),
    SHORT_FILM("Short Film", "Sinema Pendek"),
    DEEP_DIVE_ESSAY("Deep-Dive Essay", "Video Essay"),
    DOCUMENTARY("Dokumenter", "Dokumenter Investigatif")
}

enum class ContentStatus(val displayName: String) {
    AVAILABLE("Tersedia"),
    ACQUIRED_LUMP_SUM("Diakuisisi - Jual Putus"),
    LICENSED("Lisensi Berjalan")
}

enum class ContentSortOption(val displayName: String) {
    ENGAGEMENT_HIGHEST("Engagement Teratas"),
    ROYALTY_HIGHEST("Royalti Tertinggi"),
    EXPIRING_SOON("Kontrak Segera Habis"),
    BUDGET_HIGHEST("Budget Produksi Terbesar")
}

enum class CoProductionFundingScheme(val displayName: String, val shortDesc: String, val creatorRatio: Double = 1.0, val studioRatio: Double = 0.0) {
    FULL_CREATOR("100% Dana Kreator", "100% Kas Kreator. Karya penuh di Bank Konten Kreator.", 1.0, 0.0),
    CREATOR_70_30("Skema 70/30 (Kreator Dominan)", "70% Kas Kreator & 30% Kas Studio Film. Utama di Bank Konten Kreator.", 0.7, 0.3),
    JOINT_VENTURE_50_50("Joint Venture (50/50)", "50% Kas Kreator & 50% Kas Studio Film. Tercatat seimbang di kedua unit usaha.", 0.5, 0.5),
    STUDIO_30_70("Skema 30/70 (Studio Dominan)", "30% Kas Kreator & 70% Kas Studio Film. Utama di Katalog Proyek Studio Film.", 0.3, 0.7),
    FULL_STUDIO("100% Dana Studio Film", "100% Kas Studio Film. Karya tayang & masuk Katalog Film Studio.", 0.0, 1.0)
}

enum class CreativeFocus(val id: String, val displayName: String, val desc: String, val icon: String) {
    STORY_NARRATIVE("STORY", "Naskah & Plot Cerita", "Fokus dialog tajam, twist emosional, dan penokohan. Sinergi tinggi dengan Short Film.", "📖"),
    CINEMATOGRAPHY("VISUAL", "Visual & Sinematografi", "Fokus kualitas sinematik, lighting, audio & visual polish. Memaksimalkan budget.", "🎥"),
    INVESTIGATION("INVESTIGASI", "Riset & Fakta Eksklusif", "Fokus riset mendalam, arsip, dan pembongkaran data. Sinergi tinggi dengan Essay & Dokumenter.", "🔍"),
    VIRAL_HOOK("VIRAL", "Sensasi Populer & Hook", "Fokus retensi tinggi, pacing cepat, dan tren media sosial untuk memicu viralitas instan.", "⚡")
}

data class ContentWork(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val type: ContentType = ContentType.SHORT_FILM,
    val budget: Long = 0L,
    val engagementScore: Int? = 50, // 1 - 100
    val status: ContentStatus = ContentStatus.AVAILABLE,
    val monthlyRoyalty: Long = 0L,
    val acquiredLumpSum: Long = 0L,
    val acquiredByPH: String? = null,
    val contractDurationMonths: Int? = null,
    val remainingContractMonths: Int? = null,
    val partnerStudioName: String? = null,
    val partnerStudioId: String? = null,
    val fundingScheme: CoProductionFundingScheme? = null,
    val isShadowRecord: Boolean = false,
    val movieProjectId: String? = null,
    val creativeFocus: String? = null,
    val receptionVerdict: String? = null,
    val receptionNote: String? = null,
    val createdTimestamp: Long = System.currentTimeMillis()
)

data class ProductionHouseOffer(
    val id: String = UUID.randomUUID().toString(),
    val phName: String = "",
    val phTier: String = "Major Global Studio",
    val contentId: String = "",
    val contentTitle: String = "",
    val contentType: ContentType = ContentType.SHORT_FILM,
    val engagementScore: Int = 50,
    val lumpSumOffer: Long = 0L,
    val royaltyUpfront: Long = 0L,
    val monthlyRoyalty: Long = 0L,
    val contractDurationMonths: Int = 24,
    val pitchMessage: String = "",
    val durationSeconds: Int = 20,
    val createdTimestamp: Long = System.currentTimeMillis()
)

data class ContentScoreEvaluation(
    val score: Int,
    val verdictTitle: String,
    val verdictDescription: String,
    val technicalScore: Int,
    val conceptScore: Int,
    val audienceScore: Int
)

enum class BrandDealType {
    ONE_OFF,   // Sponsor Sekilas (1 video, instan cash)
    CONTRACT   // Kontrak Resmi (Multi-bulan, gajian per payday siklus)
}

data class BrandDealOffer(
    val id: String = UUID.randomUUID().toString(),
    val brandName: String,
    val dealType: BrandDealType = BrandDealType.ONE_OFF,
    val contractValue: Long,
    val monthlyPayout: Long = 0L,
    val durationMonths: Int = 1,
    val tierLevel: Int,
    val tierName: String,
    val categoryTag: String,
    val durationSeconds: Int = 15,
    val createdTimestamp: Long = System.currentTimeMillis()
)

data class ActiveCreatorContract(
    val id: String = UUID.randomUUID().toString(),
    val brandName: String,
    val tierLevel: Int,
    val categoryTag: String,
    val monthlyPayout: Long,
    val totalMonths: Int,
    val remainingMonths: Int,
    val totalPaidSoFar: Long = 0L
) {
    val totalContractValue: Long get() = monthlyPayout * totalMonths
    val progressFraction: Float get() = if (totalMonths > 0) (totalMonths - remainingMonths).toFloat() / totalMonths.toFloat() else 1f
}

data class BrandTierInfo(
    val tier: Int,
    val name: String,
    val brands: List<String>,
    val minVal: Long,
    val maxVal: Long,
    val chance: Float
)
