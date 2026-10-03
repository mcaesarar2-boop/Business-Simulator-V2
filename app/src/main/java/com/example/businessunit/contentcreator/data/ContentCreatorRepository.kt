package com.example.businessunit.contentcreator.data

import com.example.businessunit.contentcreator.model.BrandDealOffer
import com.example.businessunit.contentcreator.model.BrandDealType
import com.example.businessunit.contentcreator.model.BrandTierInfo
import com.example.businessunit.contentcreator.model.ContentType
import kotlin.random.Random

object ContentCreatorRepository {

    /**
     * Minimum subscriber count required to unlock Studio Film Co-Production / budget > $50k.
     * Changed to 100k (100,000 subscribers) as per user request.
     */
    const val MIN_SUBS_STUDIO_SYNERGY = 100_000L

    const val MAX_INDIE_BUDGET = 50_000L

    val BRAND_TIER_0 = listOf(
        "GlowTea", "CaseNova", "PixelSkin", "ClipMic GO", "SnackLoop",
        "Boba Planet", "QuickCharge Mini", "NeonBottle", "Campus Backpack", "MiniTripod Co.",
        "Pet Treat Factory", "CoffeeRush Local", "Daily Journal App", "Budget Earbuds", "FitBand Lite",
        "Street Socks", "Cozy Blanket", "Phone Stand Pro", "Notebook Studio", "FreshMint Gum"
    )

    val BRAND_TIER_1 = listOf(
        "NordVpn", "Raid Shadow", "HelloFresh", "Manscaped", "Skillshare",
        "Surfshark", "Displate", "GFUEL", "Anker Power", "Grammarly",
        "SeatGeek", "Audible", "CuriosityStream", "Ridge Wallet", "Raycon",
        "Babbel", "Factor Meals", "ExpressVPN", "BetterHelp", "Squarespace"
    )

    val BRAND_TIER_2 = listOf(
        "Samsung Galaxy", "Logitech G", "Razer Chroma", "Sony Audio", "ASUS ROG",
        "GoPro Hero", "Secretlab", "MSI Gaming", "Elgato Wave", "HyperX Alloy",
        "SteelSeries", "DJI Osmo", "Shure MV7", "Corsair Gaming", "Sennheiser HD",
        "Canon EOS Creator", "Rode Wireless", "BenQ Mobiuz", "Nanoleaf", "Keychron"
    )

    val BRAND_TIER_3 = listOf(
        "Nike Pro", "Red Bull", "Monster Energy", "Adidas Originals", "Under Armour",
        "Puma Motorsport", "Gymshark", "Gatorade Sports", "Beats by Dre", "Oakley",
        "Vans Off The Wall", "The North Face", "Lululemon", "GoPro RedBull Extreme", "Casio G-Shock"
    )

    val BRAND_TIER_4 = listOf(
        "Apple Inc.", "Google Pixel", "Tesla Cyber", "BMW Motorsport", "Mercedes-AMG",
        "Porsche Motorsport", "Sony PlayStation", "Microsoft Xbox", "Rolex Precision", "NVIDIA RTX"
    )

    fun getBrandTiers(level: Int): List<BrandTierInfo> {
        return listOf(
            BrandTierInfo(0, "Local SME", BRAND_TIER_0, 100L, 500L, 0.50f),
            BrandTierInfo(1, "Online Creator Brands", BRAND_TIER_1, 500L, 2_500L, if (level >= 10) 0.35f else 0.05f),
            BrandTierInfo(2, "Tech & Gaming Giants", BRAND_TIER_2, 2_500L, 10_000L, if (level >= 30) 0.25f else 0.0f),
            BrandTierInfo(3, "Lifestyle & Energy", BRAND_TIER_3, 10_000L, 40_000L, if (level >= 55) 0.15f else 0.0f),
            BrandTierInfo(4, "Luxury & Mega Global", BRAND_TIER_4, 40_000L, 200_000L, if (level >= 75) 0.10f else 0.0f)
        )
    }

    fun generateRandomBrandDeal(level: Int, subscribers: Long): BrandDealOffer {
        val tiers = getBrandTiers(level).filter { it.chance > 0f }
        val chosenTier = tiers.randomOrNull() ?: tiers.first()
        val brandName = chosenTier.brands.random()

        val isContract = level >= 15 && Random.nextFloat() < 0.45f
        val dealType = if (isContract) BrandDealType.CONTRACT else BrandDealType.ONE_OFF

        val baseVal = (chosenTier.minVal + (chosenTier.maxVal - chosenTier.minVal) * Random.nextDouble()).toLong()
        val subsMultiplier = 1.0 + (subscribers / 500_000.0).coerceAtMost(3.0)
        val finalMonthlyOrInstant = (baseVal * subsMultiplier).toLong()

        val durationMonths = if (isContract) listOf(3, 6, 12, 24).random() else 1
        val totalContractVal = if (isContract) finalMonthlyOrInstant * durationMonths else finalMonthlyOrInstant

        return BrandDealOffer(
            brandName = brandName,
            dealType = dealType,
            contractValue = totalContractVal,
            monthlyPayout = if (isContract) finalMonthlyOrInstant else 0L,
            durationMonths = durationMonths,
            tierLevel = chosenTier.tier,
            tierName = chosenTier.name,
            categoryTag = when (chosenTier.tier) {
                0 -> "UMKM & Produk Lokal"
                1 -> "Digital Brand & SaaS"
                2 -> "Gadget & Peripheral"
                3 -> "Sport & Lifestyle"
                else -> "Global Enterprise"
            }
        )
    }

    private val FEATURE_FILM_TITLES = listOf(
        "Mahakarya Sang Maestro", "Konspirasi Taipan Nusantara", "Jejak Langkah Sang Konglomerat",
        "Takhta Emas Ibu Kota", "Misteri Korporasi Terlarang", "Saga Dinasti Bisnis",
        "Pertarungan Pasar Saham", "Rahasia Warisan Terakhir", "Bintang di Ujung Samudra"
    )

    private val SHORT_FILM_TITLES = listOf(
        "Sinema Noir: Jakarta 2045", "Bayangan di Ujung Senja", "Dua Detik Terakhir",
        "Kunci Rahasia Kamar 404", "Surat Tanpa Pengirim", "Melodi yang Hilang",
        "Gerbang Tak Terlihat", "Siklus Tak Berujung", "Keluarga di Seberang Meja",
        "Hujan di Sudut Kota", "Pertemuan Terakhir", "Suara dalam Sunyi"
    )

    private val ESSAY_TITLES = listOf(
        "Mengapa Industri Film Berubah Selamanya", "Anatomi Algoritma Viral",
        "Misteri AI: Akankah Menggantikan Sineas?", "Ekonomi Kreator: Ilusi atau Masa Depan?",
        "Evolusi Sinematografi Digital", "Psikologi Retensi Audiens", "Strategi Monopolisasi Media"
    )

    private val DOCUMENTARY_TITLES = listOf(
        "Jejak Mafia Korporasi Media", "Di Balik Layar Studio Independen",
        "Dapur Rekaman: Fakta Tersembunyi", "Investigasi: Eksploitasi Hak Cipta",
        "Perang Streaming: Siapa yang Bertahan?", "Kisah Nyata Para Sutradara Indie"
    )

    fun getRandomIdea(type: ContentType): String {
        return when (type) {
            ContentType.FEATURE_FILM -> FEATURE_FILM_TITLES.random()
            ContentType.SHORT_FILM -> SHORT_FILM_TITLES.random()
            ContentType.DEEP_DIVE_ESSAY -> ESSAY_TITLES.random()
            ContentType.DOCUMENTARY -> DOCUMENTARY_TITLES.random()
        }
    }
}
