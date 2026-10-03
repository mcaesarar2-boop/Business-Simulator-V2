package com.example.data

import com.example.data.treasury.BondHolding
import com.example.data.treasury.FundHolding

data class MonthlyFinancialRecord(
    val monthTick: Int,      // Penanda bulan ke-berapa
    val totalRevenue: Long,  // Pemasukan kotor bulan itu
    val totalExpense: Long,  // Pengeluaran bulan itu
    val netIncome: Long      // Laba Bersih bulan itu
)

data class PrivateLedgerRecord(
    val monthTick: Int,
    val title: String, // Contoh: "Gaji CEO (Mega Holding)", "Pajak Pribadi", "Dividen Saham (AAPL)"
    val amount: Long,
    val isIncome: Boolean // true = Uang Masuk (Hijau), false = Uang Keluar (Merah)
)

data class OwnedStock(val ticker: String, val averagePrice: Double, val shares: Long, val isIntegratedToHolding: Boolean = false)

data class TvProgram(
    val id: String,
    val title: String,
    val type: String,
    val productionCost: Double,
    val monthlyAdRevenue: Double,
    val rating: Double,
    val active: Boolean = true,
    val remainingMonths: Int = -1, // -1 means infinite/manual cancel
    val isOriginalIP: Boolean = true,
    val totalAccumulatedProfit: Double = 0.0,
    val monthsAired: Int = 0,
    val currentOperationalCost: Double = productionCost * 0.15,
    val previousRating: Double = rating,
    val timeSlots: List<String> = emptyList(),
    val assignedStudioId: String? = null,
    val assignedStudioName: String? = null,
    val requiredCrews: Int = 10
)

data class TimeDeposit(
    val id: String,
    val principal: Long,
    val durationMonths: Int,
    val monthsRemaining: Int,
    val interestRate: Double
)

enum class ProjectType {
    CLIENT_B2B, INDEPENDENT_SAAS, ECOSYSTEM_SYNERGY
}

enum class ProjectStatus {
    DEVELOPMENT, MAINTENANCE, COMPLETED
}

data class AppProject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val type: ProjectType,
    val budgetCost: Double,
    val targetRevenue: Double,
    val devTimeMonths: Int,
    val currentMonth: Int = 0,
    val status: ProjectStatus = ProjectStatus.DEVELOPMENT,
    val targetBusinessId: String? = null,
    // Enhanced fields for Realistic DevOps Kanban & SaaS
    val kanbanColumn: String = "IN_PROGRESS", // "BACKLOG", "IN_PROGRESS", "DEPLOYED"
    val requiredUiUx: Int = 1,
    val requiredFrontend: Int = 1,
    val requiredBackend: Int = 1,
    val assignedUiUx: Int = 0,
    val assignedFrontend: Int = 0,
    val assignedBackend: Int = 0,
    val isAssigned: Boolean = false,
    val activeUsers: Long = 1000L,
    val churnRate: Double = 0.04,
    val currentMrr: Double = targetRevenue,
    val serverResourceUnits: Int = 10,
    val isBugFixTask: Boolean = false,
    val parentSaaSId: String? = null,
    val bugSeverity: String = "NORMAL",
    val description: String = "",
    val clientName: String = "Klien Korporat",
    val categoryTag: String = "Web & Mobile"
)

data class MegaHoldingState(
    val isActive: Boolean = false,
    val companyName: String = "",
    val includesInvestments: Boolean = false,
    val investmentCompanyName: String = "",
    val ownershipPercentage: Double = 100.0,
    val holdingMetals: Map<String, Double> = emptyMap(),
    val holdingMetalsAveragePrices: Map<String, Double> = emptyMap(),
    val holdingTimeDeposits: List<TimeDeposit> = emptyList(),
    val holdingBonds: List<BondHolding> = emptyList(),
    val holdingRdpt: FundHolding = FundHolding(),
    val holdingRdptNav: Double = 1500.0,
    val totalBondCouponIncomeEarned: Long = 0L
)

data class LifestyleItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val tabCategory: String, // "langganan", "gadget", "ekspedisi", "wellness", "filantropi", "pengeluaran_kustom", dll.
    var sectionName: String, // Contoh: "Entertainment", "Productivity", "Vehicles"
    var name: String,
    var price: Long,
    var imgUrl: String, // Menyimpan link URL gambar (PNG/SVG/JPG)
    var desc: String, // Deskripsi item
    var isActive: Boolean = false, // Untuk sistem toggle/langganan
    var isOwned: Boolean = false, // Untuk sistem one-time purchase
    val isCustom: Boolean = false, // Penanda jika ini buatan pemain
    val isRecurring: Boolean = false, // True jika pengeluaran rutin bulanan
    val fundedCount: Int = 0 // Berapa kali sudah didanai
)

data class TravelDestination(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val region: String,
    val pricePerDay: Long, // Harga dasar per hari
    val imageUrl: String,
    val isCustom: Boolean = false
)

val defaultTravelDestinations = listOf(
    TravelDestination(name = "Disneyland VIP Tour", region = "Orlando, USA", pricePerDay = 2000L, imageUrl = "https://images.unsplash.com/photo-1545231027-63b3f1626a5e?auto=format&fit=crop&w=500&q=80"),
    TravelDestination(name = "Maldives Private Island Retreat", region = "Maldives", pricePerDay = 5000L, imageUrl = "https://images.unsplash.com/photo-1514282401047-d79a71a590e8?auto=format&fit=crop&w=500&q=80"),
    TravelDestination(name = "Alps Luxury Ski Chalet", region = "Zermatt, Switzerland", pricePerDay = 8000L, imageUrl = "https://images.unsplash.com/photo-1502784444187-359ac186c5bb?auto=format&fit=crop&w=500&q=80"),
    TravelDestination(name = "Necker Island Sanctuary", region = "British Virgin Islands", pricePerDay = 15000L, imageUrl = "https://images.unsplash.com/photo-1548574505-5e239809ee19?auto=format&fit=crop&w=500&q=80"),
    TravelDestination(name = "Kyoto Imperial Villa Retreat", region = "Kyoto, Japan", pricePerDay = 4000L, imageUrl = "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=500&q=80")
)

val defaultLifestyleItems = listOf(
    // LANGGANAN
    LifestyleItem(tabCategory = "langganan", sectionName = "Entertainment", name = "Spotify Premium", price = 10L, imgUrl = "", desc = "Dengarkan musik resolusi tinggi tanpa gangguan."),
    LifestyleItem(tabCategory = "langganan", sectionName = "Entertainment", name = "YouTube Premium", price = 15L, imgUrl = "", desc = "Nonton offline & bebas iklan untuk video tech startup."),
    LifestyleItem(tabCategory = "langganan", sectionName = "Entertainment", name = "Disney+ & Netflix Bundle", price = 30L, imgUrl = "", desc = "Paket hiburan film akhir pekan 4K HDR."),
    LifestyleItem(tabCategory = "langganan", sectionName = "Entertainment", name = "Apple TV+", price = 15L, imgUrl = "", desc = "Katalog serial orisinal kualitas sinematik terbaik."),
    LifestyleItem(tabCategory = "langganan", sectionName = "Productivity", name = "Adobe Creative Cloud & CapCut Pro", price = 60L, imgUrl = "", desc = "Aset pengeditan video startup marketing."),
    LifestyleItem(tabCategory = "langganan", sectionName = "Productivity", name = "Google One 30TB", price = 150L, imgUrl = "", desc = "Penyimpanan cloud raksasa untuk data & sasis kecerdasan buatan."),

    // GADGET
    LifestyleItem(tabCategory = "gadget", sectionName = "Mobile", name = "Smartphone Lipat", price = 2000L, imgUrl = "", desc = "Layar ganda fleksibel terkini untuk mobilitas level eksekutif."),
    LifestyleItem(tabCategory = "gadget", sectionName = "Mobile", name = "Smartwatch Titanium", price = 1000L, imgUrl = "", desc = "Pelacak kebugaran berlapis titanium dengan sinkronisasi satelit."),
    LifestyleItem(tabCategory = "gadget", sectionName = "Work", name = "Laptop Pribadi", price = 5000L, imgUrl = "", desc = "Grafis termutakhir dengan prosesor kustom ultra hemat daya."),
    LifestyleItem(tabCategory = "gadget", sectionName = "Entertainment", name = "Computer Gaming Super", price = 15000L, imgUrl = "", desc = "Pendingin cairan dual-loop dengan sasis pencahayaan RGB kustom."),
    LifestyleItem(tabCategory = "gadget", sectionName = "Infrastructure", name = "Computer AI & Server", price = 45000L, imgUrl = "", desc = "Server cluster modular mandiri berisi 4 kartu akselerator AI."),

    // EKSPEDISI
    LifestyleItem(tabCategory = "ekspedisi", sectionName = "Leisure", name = "Couples Private Getaway", price = 50000L, imgUrl = "", desc = "Resor pulau tropis terpencil ultra mewah dengan pelayan pribadi 24 jam."),
    LifestyleItem(tabCategory = "ekspedisi", sectionName = "Leisure", name = "First-Class Europe Trip", price = 120000L, imgUrl = "", desc = "Terbang first-class ke 5 ibu kota monarki Eropa & menginap di istana kastel orisinal."),
    LifestyleItem(tabCategory = "ekspedisi", sectionName = "High-End Adventure", name = "Multi-Country Overland Expedition", price = 300000L, imgUrl = "", desc = "Perjalanan konvoi helikopter kustom menyusuri dataran tinggi bersalju & gurun murni."),

    // WELLNESS
    LifestyleItem(tabCategory = "wellness", sectionName = "Health", name = "Personal Trainer & Chef", price = 15000L, imgUrl = "", desc = "Kombinasi nutrisi kustom organik bernutrisi tinggi & latihan kardio personal harian."),
    LifestyleItem(tabCategory = "wellness", sectionName = "Health", name = "Private Doctor On-Call", price = 20000L, imgUrl = "", desc = "Tim medis klinis elit pribadi yang siaga 24 jam dengan peralatan diagnostik portabel canggih."),
    LifestyleItem(tabCategory = "wellness", sectionName = "Security", name = "Tim Bodyguard Elite", price = 50000L, imgUrl = "", desc = "Rejimen penjaga bersenjata bersertifikasi militer yang mengamankan rute perjalanan & kediaman holding."),

    // FILANTROPI
    LifestyleItem(tabCategory = "filantropi", sectionName = "Social Impact", name = "Yayasan Sosial CEO", price = 100000L, imgUrl = "", desc = "Mendirikan yayasan kesejahteraan masyarakat untuk mengurangi kemiskinan perkotaan."),
    LifestyleItem(tabCategory = "filantropi", sectionName = "Education", name = "Beasiswa Global Muda", price = 250000L, imgUrl = "", desc = "Program beasiswa penuh universitas top dunia bagi talenta lokal berprestasi."),
    LifestyleItem(tabCategory = "filantropi", sectionName = "Healthcare", name = "Pusat Riset Medis", price = 1000000L, imgUrl = "", desc = "Mendanai laboratorium penelitian obat langka dan terapi mutakhir."),

    // PENGELUARAN KUSTOM & PROPOSAL
    LifestyleItem(
        tabCategory = "pengeluaran_kustom",
        sectionName = "Proposal Acara & Sponsorship",
        name = "Proposal Turnamen Golf Eksekutif",
        price = 25000L,
        imgUrl = "https://images.unsplash.com/photo-1535131749006-b7f58c99034b?auto=format&fit=crop&w=500&q=80",
        desc = "Sponsorship turnamen golf tahunan bergengsi para pebisnis & mitra holding.",
        isCustom = false
    ),
    LifestyleItem(
        tabCategory = "pengeluaran_kustom",
        sectionName = "Proposal Acara & Sponsorship",
        name = "Sponsorship Malam Gala Seni & Busana",
        price = 40000L,
        imgUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=500&q=80",
        desc = "Pendanaan proposal pergelaran gala seni orkestra & lelang busana karya desainer lokal.",
        isCustom = false
    ),
    LifestyleItem(
        tabCategory = "pengeluaran_kustom",
        sectionName = "Proposal Acara & Sponsorship",
        name = "Sponsorship Festival Kuliner",
        price = 15000L,
        imgUrl = "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?auto=format&fit=crop&w=500&q=80",
        desc = "Dukungan stan dan promosi festival pangan & pameran kuliner nusantara.",
        isCustom = false
    ),
    LifestyleItem(
        tabCategory = "pengeluaran_kustom",
        sectionName = "Dukungan Komunitas & Hobi",
        name = "Sponsorship Tim Balap Komunitas",
        price = 35000L,
        imgUrl = "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&w=500&q=80",
        desc = "Dukungan sponsor unit kendaraan modifikasi dan perlengkapan safety paddock tim balap.",
        isCustom = false
    ),
    LifestyleItem(
        tabCategory = "pengeluaran_kustom",
        sectionName = "Pengeluaran Khusus Personal",
        name = "Donasi Pembangunan Sasana Budaya",
        price = 60000L,
        imgUrl = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=500&q=80",
        desc = "Kontribusi hibah pembangunan paviliun budaya dan galeri seni kontemporer personal.",
        isCustom = false
    ),
    LifestyleItem(
        tabCategory = "pengeluaran_kustom",
        sectionName = "Pengeluaran Khusus Personal",
        name = "Beasiswa Khusus Talenta Muda",
        price = 20000L,
        imgUrl = "https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=500&q=80",
        desc = "Bantuan biaya hidup dan perlengkapan riset mahasiswa binaan pilihan keluarga CEO.",
        isCustom = false
    )
)

typealias FundingType = com.example.privateequity.model.FundingType
typealias ActiveLoan = com.example.privateequity.model.ActiveLoan

data class PlayerState(
    val lastSavedTimeMs: Long = System.currentTimeMillis(),
    val cash: Long = 5000,
    val netWorth: Long = 5000,
    val inGameMonth: Int = 1,
    val inGameYear: Int = 1,
    val lastMonthIncome: Long = 0,
    val lastMonthExpenses: Long = 0,
    val lastMonthNetProfit: Long = 0,
    val maxBusinessSlots: Int = 11,
    val ownedBusinesses: List<OwnedBusiness> = emptyList(),
    val ownedStocks: List<OwnedStock> = emptyList(),
    val corporateStockPortfolio: List<OwnedStock> = emptyList(),
    val privateStockPortfolio: List<OwnedStock> = emptyList(),
    val ownedProperties: List<com.example.data.OwnedProperty> = emptyList(),
    val ownedCrypto: List<com.example.data.OwnedCrypto> = emptyList(),
    val activeStartupInvestments: List<com.example.data.ActiveStartupInvestment> = emptyList(),
    val ownedCollections: List<com.example.data.OwnedCollection> = emptyList(),
    val ownedMetals: Map<String, Double> = emptyMap(),
    val ownedMetalsAveragePrices: Map<String, Double> = emptyMap(),
    val ownedHouses: List<com.example.data.OwnedHousing> = emptyList(),
    val rentedHouses: List<com.example.data.RentedHousing> = emptyList(),
    val customBusinessCatalog: List<BusinessCatalogItem> = emptyList(),
    val taxLegalReport: com.example.data.TaxLegalReport = com.example.data.TaxLegalReport(),
    val rebrandedCompanies: Map<String, String> = emptyMap(),
    val timeDeposits: List<TimeDeposit> = emptyList(),
    val holdingCompanies: List<HoldingCompany> = emptyList(),
    val activeTvPrograms: List<TvProgram> = emptyList(),
    val ipLibraryHistory: List<TvProgram> = emptyList(),
    val appProjects: List<AppProject> = emptyList(),
    val megaHolding: MegaHoldingState = MegaHoldingState(),
    val customMarketAssets: List<com.example.data.PropertyItem>? = null,
    val customCollectionAssets: List<com.example.data.CollectionItem>? = null,
    val customHousingAssets: List<com.example.data.HousingItem>? = null,
    val personalDebt: Long = 0L,
    val companyOwnershipPercent: Double = 100.0,
    val playerEquityShare: Double = 100.0,
    val activeInvestorsLoans: List<ActiveLoan> = emptyList(),
    val monthlyCeoSalary: Long = 0L,
    val currentCeoSalaryPercent: Double = 4.0,
    val pendingCeoSalaryPercent: Double? = null,
    val boardApprovalMonthsLeft: Int = 0,
    val lastSalaryRequestMonth: Int = -12,
    val boardReplyMessage: String? = null,
    val privateBalance: Long = 0L,
    val currentDividendPercent: Double = 0.0,
    val pendingDividendPercent: Double? = null,
    val dividendApprovalMonthsLeft: Int = 0,
    val lastDividendRequestMonth: Int = -12,
    val currentTantiemPercent: Double = 0.0,
    val pendingTantiemPercent: Double? = null,
    val tantiemApprovalMonthsLeft: Int = 0,
    val retainedEarnings: Long = 0L,
    val totalTaxPaid: Long = 0L,
    val corporateTaxPaid: Long = 0L,
    val personalTaxPaid: Long = 0L,
    val isSptReportedThisYear: Boolean = true,
    val consecutiveUnreportedSpt: Int = 0,
    val privateTaxServiceLevel: Int = 0,
    val privateLedgerHistory: List<PrivateLedgerRecord> = emptyList(),
    val financialHistory: List<com.example.data.MonthlyFinancialRecord> = emptyList(),
    val activeSubscriptions: List<String> = emptyList(),
    val allSubscriptions: List<LifestyleItem> = defaultLifestyleItems,
    val monthlyLifestyleCost: Long = 0L,
    val ownedGadgets: List<String> = emptyList(),
    val travelHistory: Int = 0,
    val totalCharityDonated: Long = 0L,
    val travelDestinations: List<TravelDestination> = defaultTravelDestinations,
    val totalTripsTaken: Int = 0,
    val foundationLegacyPoints: Long = 0L,
    val foundations: List<com.example.data.FoundationEntity> = emptyList()
)

fun getBusinessStats(owned: OwnedBusiness, catalog: BusinessCatalogItem, playerState: PlayerState? = null): Pair<Long, Long> {
    if (owned.isUpgradingRealTime) {
        return Pair(0L, owned.calculateTotalExpenses())
    }
    var totalRev = owned.calculateGrossRevenue()
    var totalMaint = owned.calculateTotalExpenses()

    if (playerState != null && catalog.id == "media_tv") {
        var tvRev = 0.0
        var tvMaint = 0.0
        playerState.activeTvPrograms.forEach { prog ->
            if (prog.active) {
                tvRev += prog.monthlyAdRevenue
                tvMaint += prog.currentOperationalCost
            }
        }
        totalRev += tvRev.toLong()
        totalMaint += tvMaint.toLong()
    }

    return Pair(totalRev, totalMaint)
}

fun getBusinessValuation(owned: OwnedBusiness, catalog: BusinessCatalogItem): Long {
    return owned.calculateBusinessValuation()
}

fun getUpgradeCost(upgrade: BusinessUpgrade, currentLevel: Int): Long {
    var costMultiplierTotal = 1.0f
    repeat(currentLevel) { costMultiplierTotal *= upgrade.costMultiplier }
    return (upgrade.baseCost * costMultiplierTotal).toLong()
}

fun getCatalogItem(catalogId: String, playerState: PlayerState): BusinessCatalogItem? {
    val found = businessCatalog.find { it.id == catalogId } ?: playerState.customBusinessCatalog.find { it.id == catalogId }
    if (found != null) return found

    if (catalogId == "umkm_foodcart") return BusinessCatalogItem("umkm_foodcart", "Gerobak Gorengan (Legacy)", BusinessCategory.CULINARY, costToBuy = 500, monthlyRevenue = 600, monthlyMaintenanceCost = 150)
    if (catalogId == "umkm_laundry") return BusinessCatalogItem("umkm_laundry", "Laundry Kiloan (Legacy)", BusinessCategory.RETAIL, costToBuy = 1500, monthlyRevenue = 1200, monthlyMaintenanceCost = 400)
    
    return null
}

val PlayerState.totalOutstandingDebt: Long get() = (activeInvestorsLoans ?: emptyList()).sumOf { it.monthlyPayment * it.remainingMonths }
val PlayerState.totalMonthlyDebtObligation: Long get() = (activeInvestorsLoans ?: emptyList()).sumOf { it.monthlyPayment }

// 1. TOTAL LIABILITIES (Hutang Keseluruhan)
val PlayerState.totalLiabilities: Long get() {
    val lombardDebt = personalDebt
    val investorDebt = (activeInvestorsLoans ?: emptyList()).sumOf { it.monthlyPayment * it.remainingMonths }
    return lombardDebt + investorDebt
}

fun PlayerState.calculateMegaHoldingValuation(): Long {
    // 1. Bisnis yang sudah masuk holding (termasuk holdingCash & companyCash subsidiary)
    val holdingsValue = holdingCompanies.sumOf { it.calculateHoldingValuation() }
    
    // 2. Bisnis yang BELUM masuk holding / Independent (termasuk companyCash)
    val independentBusinessValue = ownedBusinesses.filter { it.parentId.isNullOrEmpty() }.sumOf { it.calculateBusinessValuation() }
    
    return holdingsValue + independentBusinessValue
}

val PlayerState.playerBusinessWealth: Long get() {
    val ownershipDecimal = companyOwnershipPercent / 100.0
    return (calculateMegaHoldingValuation() * ownershipDecimal).toLong()
}

// 2. TOTAL BUSINESS VALUATION (Murni milik pemain)
fun PlayerState.calculatePlayerBusinessWealth(): Long {
    return playerBusinessWealth
}

val PlayerState.rawMegaHoldingValuation: Long get() {
    return calculateMegaHoldingValuation()
}

val PlayerState.playerBusinessValuation: Long get() {
    return playerBusinessWealth
}

fun PlayerState.totalFortune(
    stockList: List<StockItem> = emptyList(),
    cryptoList: List<CryptoItem> = emptyList(),
    realEstateMarket: List<PropertyItem> = emptyList(),
    collectionList: List<CollectionItem> = emptyList(),
    preciousMetalsList: List<PreciousMetal> = emptyList()
): Long {
    return netAssetValue(stockList, cryptoList, realEstateMarket, collectionList, preciousMetalsList)
}

val PlayerState.totalFortune: Long get() {
    return netAssetValue()
}

// 3. TOTAL INVESTASI LIKUID (Kertas/Digital)
fun PlayerState.totalLiquidInvestments(
    stockList: List<StockItem> = emptyList(),
    cryptoList: List<CryptoItem> = emptyList()
): Long {
    val stockValue = ownedStocks.sumOf { owned ->
        val liveStock = stockList.find { it.ticker == owned.ticker }
        val livePrice = liveStock?.currentPrice ?: owned.averagePrice
        (owned.shares * livePrice).toLong()
    }
    val cryptoValue = ownedCrypto.sumOf { owned ->
        val livePrice = cryptoList.find { it.symbol == owned.symbol }?.currentPrice ?: owned.averagePrice
        (owned.amount * livePrice).toLong()
    }
    val bankDeposits = timeDeposits.sumOf { it.principal }
    return stockValue + cryptoValue + bankDeposits
}

// 4. TOTAL ASET FISIK (Tangibles)
fun PlayerState.totalTangibleAssets(
    realEstateMarket: List<PropertyItem> = emptyList(),
    collectionList: List<CollectionItem> = emptyList(),
    preciousMetalsList: List<PreciousMetal> = emptyList()
): Long {
    val realEstateValue = ownedProperties.sumOf { owned ->
        val prop = realEstateMarket.find { it.id == owned.propertyId }
        prop?.basePrice ?: owned.purchasedPrice
    }
    
    val collectionsValue = ownedCollections.filter { owned ->
        val cat = collectionList.find { c -> c.id == owned.itemId }?.categoryId
        val isVehicle = listOf("cars", "motorcycles", "yachts", "airplanes").contains(cat)
        cat != null && !isVehicle
    }.sumOf { it.purchasedPrice }
    
    val vehiclesValue = ownedCollections.filter { owned ->
        val cat = collectionList.find { c -> c.id == owned.itemId }?.categoryId
        listOf("cars", "motorcycles", "yachts", "airplanes").contains(cat)
    }.sumOf { it.purchasedPrice }
    
    val metalsValue = ownedMetals.entries.sumOf { (id, amount) ->
        val livePrice = preciousMetalsList.find { it.id == id }?.currentPrice ?: 0.0
        (amount * livePrice).toLong()
    }
    
    val housingValue = ownedHouses.sumOf { it.purchasedPrice }
    
    return realEstateValue + vehiclesValue + collectionsValue + metalsValue + housingValue
}

// 5. THE ULTIMATE NET WORTH (NAV / Total Fortune)
fun PlayerState.netAssetValue(
    stockList: List<StockItem> = emptyList(),
    cryptoList: List<CryptoItem> = emptyList(),
    realEstateMarket: List<PropertyItem> = emptyList(),
    collectionList: List<CollectionItem> = emptyList(),
    preciousMetalsList: List<PreciousMetal> = emptyList()
): Long {
    val grossAssets = cash + privateBalance + playerBusinessValuation + 
            totalLiquidInvestments(stockList, cryptoList) + 
            totalTangibleAssets(realEstateMarket, collectionList, preciousMetalsList)
    return grossAssets - totalLiabilities
}

data class Billionaire(val id: Int, val name: String, val netWorth: Long, val rank: Int = 0)