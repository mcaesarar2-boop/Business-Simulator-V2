package com.example.data

import kotlin.random.Random

typealias BrandDealType = com.example.businessunit.contentcreator.model.BrandDealType
typealias BrandDealOffer = com.example.businessunit.contentcreator.model.BrandDealOffer
typealias ActiveCreatorContract = com.example.businessunit.contentcreator.model.ActiveCreatorContract
typealias BrandTierInfo = com.example.businessunit.contentcreator.model.BrandTierInfo

object BrandDealGenerator {

    // ==========================================
    // DATASET LENGKAP BRAND DEALS TIER 0 - TIER 7
    // ==========================================
    private val TIER_0_BRANDS = listOf(
        "GlowTea", "CaseNova", "PixelSkin", "ClipMic GO", "SnackLoop",
        "Boba Planet", "QuickCharge Mini", "NeonBottle", "Campus Backpack", "MiniTripod Co.",
        "Pet Treat Factory", "CoffeeRush Local", "Daily Journal App", "Budget Earbuds", "FitBand Lite",
        "Street Socks", "Cozy Blanket", "Phone Stand Pro", "Notebook Studio", "FreshMint Gum",
        "Local Clothing Store", "Bubble Drink Shop", "Indie Coffee Roaster", "Gaming Mouse Pad", "LED Desk Lamp"
    )

    private val TIER_1_BRANDS = listOf(
        "AuraVPN", "StreamNest", "EditFlow", "SkillSprint", "QuickMeal",
        "FitFuel", "Mellow Coffee", "Neo Wallet", "Cloud Storage+", "Urban Shoes",
        "Nova Apparel", "Creator Desk", "Phone Armor", "StudyHub", "RideNow",
        "Fresh Laundry", "Protein Nation", "Creator Chair", "RGB Keyboard Co.", "Gaming Desk",
        "SleepCloud Pillow", "Drone Mini", "Wireless Charger", "Portable SSD", "Podcast Starter Kit",
        "Local Hotel Chain", "Food Delivery App", "Budget Smartphone", "Energy Drink Startup", "Fitness Apparel"
    )

    private val TIER_2_BRANDS = listOf(
        "Adobe Express", "Canva Pro", "Insta360", "DJI Mini", "NordVPN",
        "ExpressVPN", "Surfshark", "Skillshare", "MasterClass", "Logitech",
        "HyperX", "Razer", "Corsair", "Elgato", "GoPro",
        "Anker", "UGREEN", "ASUS", "MSI", "Acer Predator",
        "Lenovo Legion", "Samsung Galaxy A", "Xiaomi", "realme", "OPPO",
        "Vivo", "Foodpanda", "Grab", "Shopee", "Tokopedia",
        "Traveloka", "Tiket.com", "Spotify Premium", "Disney+", "Crunchyroll",
        "Epic Games", "Riot Games", "CapCut Pro", "FilmConvert", "CreatorCloud"
    )

    private val TIER_3_BRANDS = listOf(
        "Netflix", "Prime Video", "Adobe Creative Cloud", "Samsung", "Sony",
        "Canon", "Nikon", "DJI", "Bose", "JBL",
        "Sennheiser", "Marshall", "LG", "Intel", "AMD",
        "NVIDIA", "Acer", "ASUS ROG", "Lenovo Legion", "HP Omen",
        "Nothing", "OnePlus", "Garmin", "Fitbit", "Nike",
        "Adidas", "Puma", "Under Armour", "Red Bull", "Monster Energy",
        "Gatorade", "Spotify", "Notion", "Dropbox", "Google Workspace",
        "Microsoft 365", "PayPal", "Wise", "AirAsia", "Garuda Indonesia"
    )

    private val TIER_4_BRANDS = listOf(
        "Apple", "Google", "Samsung Galaxy", "Sony Alpha", "Canon Cinema EOS",
        "Netflix Originals", "Disney+", "Amazon", "Meta", "TikTok Global",
        "YouTube", "PlayStation", "Xbox", "EA Sports", "Ubisoft",
        "Epic Games", "Tesla", "Toyota", "Hyundai", "BMW",
        "Mercedes-Benz", "Porsche", "L'Oréal", "Sephora", "Maybelline",
        "Levi's", "Converse", "Crocs", "IKEA", "Dyson",
        "Philips Hue", "Marriott", "Hilton", "Airbnb", "Emirates",
        "Qatar Airways", "Coca-Cola", "Pepsi", "McDonald's", "KFC"
    )

    private val TIER_5_BRANDS = listOf(
        "Louis Vuitton", "Gucci", "Prada", "Dior", "Chanel",
        "Balenciaga", "Versace", "Calvin Klein", "Tommy Hilfiger", "Rolex",
        "Omega", "TAG Heuer", "Cartier", "Tiffany & Co.", "Ferrari",
        "Lamborghini", "Bugatti", "Porsche", "Red Bull Global", "Nike Signature Campaign",
        "Adidas Originals", "Netflix Worldwide", "Warner Bros.", "Universal Pictures", "Paramount",
        "Formula E", "FIFA Campaign", "Olympic Partner", "Apple Worldwide", "Samsung Unpacked"
    )

    private val TIER_6_BRANDS = listOf(
        "Apple Global Campaign", "Google Pixel Worldwide", "Nike Global Ambassador", "Adidas Global", "Coca-Cola Worldwide",
        "Samsung Worldwide", "Netflix Global Launch", "Disney Studios", "Warner Bros.", "Universal Pictures",
        "Marvel Studios", "Formula One", "Porsche Global", "Ferrari Experience", "Emirates Worldwide",
        "Qatar Airways Global", "LVMH", "Dior Beauty", "Chanel House", "Rolex Ambassador"
    )

    private val TIER_7_BRANDS = listOf(
        "Apple Keynote Partnership", "Netflix Originals Exclusive", "Disney Global Event", "Google Worldwide Launch", "Samsung Galaxy Global Reveal",
        "Nike Signature Collection", "Adidas Originals Global", "Louis Vuitton Worldwide", "Formula One Season Campaign", "FIFA World Cup Campaign",
        "Olympic Worldwide Partner", "Amazon Global Commercial", "McDonald's Global Menu Campaign", "Coca-Cola Worldwide Campaign", "Tesla Special Collaboration",
        "Space Tourism Campaign", "PlayStation Global Reveal", "Xbox Worldwide Launch", "LEGO Signature Collection", "Warner Bros. Blockbuster Partnership"
    )

    fun getTierInfo(subscribers: Long): BrandTierInfo {
        return when {
            subscribers < 10_000 -> BrandTierInfo(
                tier = 0,
                name = "Tier 0: Nano Creator",
                brands = TIER_0_BRANDS,
                minVal = 100,
                maxVal = 700,
                chance = 0.55f
            )
            subscribers < 100_000 -> BrandTierInfo(
                tier = 1,
                name = "Tier 1: Micro Creator",
                brands = TIER_1_BRANDS,
                minVal = 500,
                maxVal = 2500,
                chance = 0.40f
            )
            subscribers < 500_000 -> BrandTierInfo(
                tier = 2,
                name = "Tier 2: Macro Creator",
                brands = TIER_2_BRANDS,
                minVal = 3000,
                maxVal = 12000,
                chance = 0.30f
            )
            subscribers < 1_000_000 -> BrandTierInfo(
                tier = 3,
                name = "Tier 3: Major Creator",
                brands = TIER_3_BRANDS,
                minVal = 10000,
                maxVal = 35000,
                chance = 0.24f
            )
            subscribers < 5_000_000 -> BrandTierInfo(
                tier = 4,
                name = "Tier 4: Mega Creator",
                brands = TIER_4_BRANDS,
                minVal = 50000,
                maxVal = 150000,
                chance = 0.18f
            )
            subscribers < 10_000_000 -> BrandTierInfo(
                tier = 5,
                name = "Tier 5: Superstar Creator",
                brands = TIER_5_BRANDS,
                minVal = 150000,
                maxVal = 500000,
                chance = 0.12f
            )
            subscribers < 50_000_000 -> BrandTierInfo(
                tier = 6,
                name = "Tier 6: Elite Creator",
                brands = TIER_6_BRANDS,
                minVal = 500000,
                maxVal = 2000000,
                chance = 0.08f
            )
            else -> BrandTierInfo(
                tier = 7,
                name = "Tier 7: Legendary Creator",
                brands = TIER_7_BRANDS,
                minVal = 2000000,
                maxVal = 10000000,
                chance = 0.04f
            )
        }
    }

    private fun getTagForTier(tier: Int): String {
        return when (tier) {
            0 -> "Local / Indie Sponsor"
            1 -> "App & Lifestyle Promo"
            2 -> "Tech & SaaS Partnership"
            3 -> "Major Brand Placement"
            4 -> "Global Enterprise Campaign"
            5 -> "Luxury & Worldwide Feature"
            6 -> "Global Ambassador Deal"
            else -> "Legendary Headline Event"
        }
    }

    private fun roundNicely(value: Long): Long {
        return when {
            value > 1_000_000 -> (value / 50_000) * 50_000
            value > 100_000 -> (value / 5_000) * 5_000
            value > 10_000 -> (value / 500) * 500
            value > 1_000 -> (value / 50) * 50
            else -> (value / 10) * 10
        }
    }

    fun generateBrandDeal(
        subscribers: Long,
        activeContractsCount: Int = 0,
        forceSpawn: Boolean = false
    ): BrandDealOffer? {
        val tierInfo = getTierInfo(subscribers)
        
        if (!forceSpawn) {
            val roll = Random.nextFloat()
            if (roll > tierInfo.chance) {
                return null
            }
        }

        val brand = tierInfo.brands.randomOrNull() ?: "TechNova Sponsor"
        
        // Tentukan tipe deal: 60% One-Off, 40% Contract (kecuali jika slot kontrak penuh >= 3, paksa One-Off)
        val dealType = if (activeContractsCount >= 3) {
            BrandDealType.ONE_OFF
        } else {
            if (Random.nextFloat() < 0.60f) BrandDealType.ONE_OFF else BrandDealType.CONTRACT
        }

        return when (dealType) {
            BrandDealType.ONE_OFF -> {
                val rawValue = Random.nextLong(tierInfo.minVal, tierInfo.maxVal + 1)
                val roundedValue = roundNicely(rawValue).coerceAtLeast(tierInfo.minVal)
                BrandDealOffer(
                    brandName = brand,
                    dealType = BrandDealType.ONE_OFF,
                    contractValue = roundedValue,
                    monthlyPayout = 0L,
                    durationMonths = 1,
                    tierLevel = tierInfo.tier,
                    tierName = tierInfo.name,
                    categoryTag = getTagForTier(tierInfo.tier),
                    durationSeconds = 15
                )
            }
            BrandDealType.CONTRACT -> {
                val duration = listOf(3, 6, 12, 24).random()
                // Gaji bulanan dihitung proporsional dengan bonus retainer
                val monthlyBase = Random.nextLong(
                    (tierInfo.minVal * 0.4).toLong().coerceAtLeast(50L),
                    (tierInfo.maxVal * 0.85).toLong().coerceAtLeast(100L)
                )
                val roundedMonthly = roundNicely(monthlyBase).coerceAtLeast(50L)
                val totalValue = roundedMonthly * duration
                BrandDealOffer(
                    brandName = brand,
                    dealType = BrandDealType.CONTRACT,
                    contractValue = totalValue,
                    monthlyPayout = roundedMonthly,
                    durationMonths = duration,
                    tierLevel = tierInfo.tier,
                    tierName = tierInfo.name,
                    categoryTag = getTagForTier(tierInfo.tier),
                    durationSeconds = 15
                )
            }
        }
    }
}
