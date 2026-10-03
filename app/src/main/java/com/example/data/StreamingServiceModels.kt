package com.example.data

import java.util.UUID

/**
 * Model data lengkap untuk unit bisnis OTT / Streaming Service Platform
 */

enum class StreamingContentType(val displayName: String, val icon: String) {
    ORIGINAL_SERIES("Original Series", "🎬"),
    BLOCKBUSTER_MOVIE("Blockbuster Movie", "🎥"),
    DOCUMENTARY("Documentary Special", "🌍"),
    ANIME_ANIMATION("Anime & Animation", "✨"),
    INDIE_DARLING("Indie Cinema", "🎭")
}

enum class StreamingContentSource(val displayName: String) {
    IN_HOUSE_STUDIO("Studio Film Sendiri (In-House)"),
    CONTENT_BANK("Bank Konten Creator"),
    LICENSED_CONTRACT("Kontrak Lisensi Eksternal"),
    ORIGINAL_OTT("Produksi OTT Original")
}

data class StreamingContent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: StreamingContentType = StreamingContentType.BLOCKBUSTER_MOVIE,
    val source: StreamingContentSource = StreamingContentSource.ORIGINAL_OTT,
    val rating: Int = 75, // 1 - 100 review score
    val views: Long = 0L,
    val monthlyViewerSpike: Long = 10_000L,
    val isViral: Boolean = false,
    val licenseExpiryMonths: Int = 0, // 0 = Perpetual / Owned
    val monthlyLicenseFee: Long = 0L,
    val genre: String = "Action / Thriller",
    val releaseYear: Int = 2026,
    val posterUrl: String = "",
    val synopsis: String = ""
)

data class StreamingServerSpec(
    val tier: Int,
    val name: String,
    val maxCapacity: Long,
    val monthlyUpkeep: Long,
    val upgradeCost: Long,
    val description: String
)

val STREAMING_SERVER_TIERS = listOf(
    StreamingServerSpec(
        tier = 1,
        name = "Cloud VPS Tier-1",
        maxCapacity = 50_000L,
        monthlyUpkeep = 10_000L,
        upgradeCost = 0L,
        description = "Kapasitas 50.000 penonton serentak. Cukup untuk peluncuran awal."
    ),
    StreamingServerSpec(
        tier = 2,
        name = "Dedicated High-Perf Clusters",
        maxCapacity = 250_000L,
        monthlyUpkeep = 35_000L,
        upgradeCost = 500_000L,
        description = "Kapasitas 250.000 penonton serentak dengan Load Balancer otomatis."
    ),
    StreamingServerSpec(
        tier = 3,
        name = "Elastic Multi-Region Cloud",
        maxCapacity = 1_000_000L,
        monthlyUpkeep = 120_000L,
        upgradeCost = 2_000_000L,
        description = "Kapasitas 1 Juta penonton serentak. Siap menampung film viral regional."
    ),
    StreamingServerSpec(
        tier = 4,
        name = "Global CDN Edge Supercluster",
        maxCapacity = 5_000_000L,
        monthlyUpkeep = 400_000L,
        upgradeCost = 8_000_000L,
        description = "Kapasitas 5 Juta penonton serentak dengan edge caching ultra cepat."
    ),
    StreamingServerSpec(
        tier = 5,
        name = "Hyperscale Datacenter Hubs",
        maxCapacity = 25_000_000L,
        monthlyUpkeep = 1_200_000L,
        upgradeCost = 25_000_000L,
        description = "Kapasitas 25 Juta penonton serentak. Menjamin streaming 4K tanpa buffering."
    ),
    StreamingServerSpec(
        tier = 6,
        name = "Sovereign Global Optical Grid",
        maxCapacity = 100_000_000L,
        monthlyUpkeep = 3_500_000L,
        upgradeCost = 75_000_000L,
        description = "Infrastruktur tingkat dunia 100 Juta+ kapasitas, anti-downtime 99.999%."
    )
)

data class StreamingServiceData(
    val subscribers: Long = 10_000L,
    val subscriptionFee: Double = 9.99,
    val serverTier: Int = 1,
    val serverHealth: Int = 100, // 0-100%
    val serverOutage: Boolean = false,
    val currentTraffic: Long = 15_000L,
    val peakTraffic: Long = 15_000L,
    val streamingCash: Long = 250_000L,
    val streamingProgress: Float = 0f,
    val streamingCatalog: List<StreamingContent> = listOf(
        StreamingContent(
            title = "Midnight Odyssey: Launch Special",
            type = StreamingContentType.ORIGINAL_SERIES,
            source = StreamingContentSource.ORIGINAL_OTT,
            rating = 82,
            views = 450_000L,
            monthlyViewerSpike = 20_000L,
            isViral = true,
            genre = "Sci-Fi / Thriller",
            synopsis = "Serial eksklusif peluncuran platform OTT yang memikat ratusan ribu penonton."
        ),
        StreamingContent(
            title = "Jakarta Cyberpunk Chronicles",
            type = StreamingContentType.BLOCKBUSTER_MOVIE,
            source = StreamingContentSource.LICENSED_CONTRACT,
            rating = 76,
            views = 310_000L,
            monthlyViewerSpike = 12_000L,
            isViral = false,
            licenseExpiryMonths = 12,
            monthlyLicenseFee = 8_000L,
            genre = "Action / Cyberpunk",
            synopsis = "Film aksi laga futuristik berlisensi eksklusif dari distributor ternama."
        )
    ),
    val encodingLevel: Int = 1, // 1..5 (1080p -> 4K HDR -> Dolby Atmos -> AI 8K)
    val aiAlgorithmLevel: Int = 1, // 1..5 (Basic -> Collaborative -> Deep Learning Rec)
    val drmProtectionLevel: Int = 1, // 1..5 (Basic -> Widevine L1 -> Blockchain Watermark)
    val localizationLevel: Int = 1, // 1..5 (Indonesian -> English/Mandarin -> 30+ Languages Dubbing)
    val marketingTier: Int = 1 // 1..5 (Social -> Influencer -> Superbowl / Billboard -> Global Blitz)
)

data class ExternalFilmContractOffer(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: StreamingContentType,
    val genre: String,
    val rating: Int,
    val upfrontLicenseCost: Long,
    val monthlyLicenseFee: Long,
    val contractDurationMonths: Int,
    val estimatedMonthlyViewers: Long,
    val studioPartner: String,
    val isViralCandidate: Boolean = false,
    val synopsis: String
)

val SAMPLE_EXTERNAL_FILM_OFFERS = listOf(
    ExternalFilmContractOffer(
        title = "Chronicles of Dune: Sandstorm",
        type = StreamingContentType.BLOCKBUSTER_MOVIE,
        genre = "Sci-Fi / Epic",
        rating = 92,
        upfrontLicenseCost = 250_000L,
        monthlyLicenseFee = 25_000L,
        contractDurationMonths = 12,
        estimatedMonthlyViewers = 450_000L,
        studioPartner = "Apex Global Pictures",
        isViralCandidate = true,
        synopsis = "Film blockbuster pemenang penghargaan internasional dengan visual menakjubkan."
    ),
    ExternalFilmContractOffer(
        title = "Neon Tokyo: Samurai 2099",
        type = StreamingContentType.ANIME_ANIMATION,
        genre = "Anime / Cyberpunk",
        rating = 88,
        upfrontLicenseCost = 150_000L,
        monthlyLicenseFee = 15_000L,
        contractDurationMonths = 10,
        estimatedMonthlyViewers = 280_000L,
        studioPartner = "Studio Mappa International",
        isViralCandidate = true,
        synopsis = "Anime masterpiece yang sangat digandrungi komunitas pop culture global."
    ),
    ExternalFilmContractOffer(
        title = "Secrets of Deep Ocean Trench",
        type = StreamingContentType.DOCUMENTARY,
        genre = "Nature / Science",
        rating = 81,
        upfrontLicenseCost = 60_000L,
        monthlyLicenseFee = 6_000L,
        contractDurationMonths = 18,
        estimatedMonthlyViewers = 95_000L,
        studioPartner = "National Geographic Explorer",
        isViralCandidate = false,
        synopsis = "Dokumenter alam bawah laut 4K yang memukau dengan narasi saintifik mendalam."
    ),
    ExternalFilmContractOffer(
        title = "Shadows in the Alley",
        type = StreamingContentType.INDIE_DARLING,
        genre = "Crime / Noir Mystery",
        rating = 84,
        upfrontLicenseCost = 45_000L,
        monthlyLicenseFee = 4_500L,
        contractDurationMonths = 12,
        estimatedMonthlyViewers = 80_000L,
        studioPartner = "Sundance Independent Syndicate",
        isViralCandidate = false,
        synopsis = "Film misteri detektif beranggaran independen dengan alur cerita cerdas."
    ),
    ExternalFilmContractOffer(
        title = "Galactic Odyssey: Season 1-3",
        type = StreamingContentType.ORIGINAL_SERIES,
        genre = "Space Opera / Drama",
        rating = 89,
        upfrontLicenseCost = 350_000L,
        monthlyLicenseFee = 35_000L,
        contractDurationMonths = 15,
        estimatedMonthlyViewers = 600_000L,
        studioPartner = "Warner Media Alliance",
        isViralCandidate = true,
        synopsis = "Serial antariksa multi-musim dengan basis penggemar jutaan orang di seluruh dunia."
    )
)
