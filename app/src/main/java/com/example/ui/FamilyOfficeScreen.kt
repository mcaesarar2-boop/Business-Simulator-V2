package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.viewmodel.GameViewModel
import com.example.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyOfficeScreen(navController: NavHostController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()
    val stockList by viewModel.stockList.collectAsState()
    val cryptoList by viewModel.cryptoList.collectAsState()
    val realEstateMarket by viewModel.realEstateMarket.collectAsState()
    val collectionList by viewModel.collectionList.collectAsState()

    // ----------------------------------------------------
    // VALUATION CALCULATIONS
    // ----------------------------------------------------
    val stocksValue = playerState.ownedStocks.sumOf { owned ->
        val liveStock = stockList.find { it.ticker == owned.ticker }
        val livePrice = liveStock?.currentPrice ?: owned.averagePrice
        (owned.shares * livePrice).toLong()
    }

    val cryptoValue = playerState.ownedCrypto.sumOf { owned ->
        val livePrice = cryptoList.find { it.symbol == owned.symbol }?.currentPrice ?: owned.averagePrice
        (owned.amount * livePrice).toLong()
    }

    val realEstateValue = playerState.ownedProperties.sumOf { owned ->
        val prop = realEstateMarket.find { it.id == owned.propertyId }
        prop?.basePrice ?: owned.purchasedPrice
    }

    val businessValue = playerState.calculateMegaHoldingValuation()

    val collectionsValue = playerState.ownedCollections.filter { owned ->
        val cat = collectionList.find { c -> c.id == owned.itemId }?.categoryId
        val isVehicle = listOf("cars", "motorcycles", "yachts", "airplanes").contains(cat)
        cat != null && !isVehicle
    }.sumOf { it.purchasedPrice }

    val vehiclesValue = playerState.ownedCollections.filter { owned ->
        val cat = collectionList.find { c -> c.id == owned.itemId }?.categoryId
        listOf("cars", "motorcycles", "yachts", "airplanes").contains(cat)
    }.sumOf { it.purchasedPrice }

    val metalsValue = playerState.ownedMetals.entries.sumOf { (id, amount) ->
        val livePrice = viewModel.preciousMetalsList.value.find { it.id == id }?.currentPrice ?: 0.0
        (amount * livePrice).toLong()
    } + playerState.timeDeposits.sumOf { it.principal }

    val housingValue = playerState.ownedHouses.sumOf { it.purchasedPrice }

    // ----------------------------------------------------
    // FAMILY OFFICE CORE METRICS
    // ----------------------------------------------------
    // VALUASI MEGA HOLDING
    val holdingValuation = playerState.calculateMegaHoldingValuation()

    // 2. Liabilities
    val liabilities = playerState.totalLiabilities

    // 3. Net Asset Value (NAV)
    val netAssetValue = playerState.netAssetValue(stockList, cryptoList, realEstateMarket, collectionList, viewModel.preciousMetalsList.value)
    val isUnlocked = netAssetValue >= 100_000L

    // 1. Gross Asset Value (GAV)
    val grossAssetValue = netAssetValue + liabilities

    val peValue = playerState.playerBusinessValuation
    val liquidMarketsValue = playerState.privateBalance + playerState.totalLiquidInvestments(stockList, cryptoList)
    val tangibleValue = playerState.totalTangibleAssets(realEstateMarket, collectionList, viewModel.preciousMetalsList.value)
    val totalWeight = (peValue + liquidMarketsValue + tangibleValue).coerceAtLeast(1)

    val pePct = (peValue.toDouble() / totalWeight * 100).coerceIn(0.0, 100.0)
    val liquidPct = (liquidMarketsValue.toDouble() / totalWeight * 100).coerceIn(0.0, 100.0)
    val tangiblePct = (tangibleValue.toDouble() / totalWeight * 100).coerceIn(0.0, 100.0)

    // Mega Holding Idle Cash (Treasurer Cash)
    val idleHoldingCash = playerState.holdingCompanies.sumOf { it.holdingCash } + playerState.ownedBusinesses.sumOf { it.companyCash }

    // Calculate Mega Holding Monthly Profit
    var megaHoldingMonthlyProfit = playerState.ownedBusinesses.sumOf {
        val ct = getCatalogItem(it.catalogId, playerState)
        if (ct != null) getBusinessStats(it, ct, playerState).let { (rev, mnt) -> rev - mnt } else 0L
    } + playerState.holdingCompanies.sumOf { h ->
        h.subsidiaries.sumOf { sub ->
            val ct = getCatalogItem(sub.catalogId, playerState)
            if (ct != null) getBusinessStats(sub, ct, playerState).let { (rev, mnt) -> rev - mnt } else 0L
        }
    }
    if (megaHoldingMonthlyProfit < 0) megaHoldingMonthlyProfit = 0L

    // Sliders & Dialog states
    var showDialogType by remember { mutableStateOf<String?>(null) } // "SALARY", "DIVIDEND", "LOMBARD", "SHARE_SALE", "LIQUIDATION"
    var dialogError by remember { mutableStateOf<String?>(null) }
    var dialogSuccess by remember { mutableStateOf<String?>(null) }

    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val gold = Color(0xFFFFD700)
    val navyBlue = Color(0xFF1B2A4A)
    val neonGreen = Color(0xFF00FF00)
    val textGray = Color(0xFFA0A0A0)
    val red = Color(0xFFFF3B30)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏛️ Family Office & Private Wealth", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF11141A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = bgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // A. HEADER (Net Worth Summary Card)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF323B4A), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151921))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NET ASSET VALUE (NAV)",
                            color = textGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatCurrencyRingkas(netAssetValue, useShortFormat),
                            color = neonGreen,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color(0xFF232B36))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Gross Assets (GAV)", color = textGray, fontSize = 12.sp)
                                Text(formatCurrencyRingkas(grossAssetValue, useShortFormat), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Personal Debt (Lombard)", color = textGray, fontSize = 12.sp)
                                Text(
                                    text = if (playerState.personalDebt > 0) "-" + formatCurrency(playerState.personalDebt) else "$0",
                                    color = if (playerState.personalDebt > 0) red else Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isUnlocked) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, gold.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C12))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🔒", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Family Office & Private Wealth Terkunci",
                                color = gold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Layanan eksklusif Family Office dan Kas Pribadi hanya terbuka untuk pengusaha & investor dengan Kekayaan Total (Total Fortune / NAV) minimal $100,000 USD.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            val progress = (netAssetValue.toFloat() / 100_000f).coerceIn(0f, 1f)
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Progres Total Fortune:",
                                        color = textGray,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${formatCurrencyRingkas(netAssetValue, false)} / $100,000 USD",
                                        color = gold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = gold,
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "💡 Tips: Kembangkan bisnis, tingkatkan pendapatan, atau akumulasi investasi aset hingga kekayaan Anda mencapai $100,000 USD untuk membuka akses Kas Pribadi, Gaji CEO, Dividen, Utang Lombard, dan Layanan Concierge VVIP.",
                                color = textGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("private_ledger") }
                            .border(2.dp, Color(0xFF00FF00).copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1D12))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "💰 KAS PRIBADI (LIQUID WEALTH)",
                                color = Color(0xFF00FF00),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = formatCurrencyRingkas(playerState.privateBalance, false),
                                color = Color(0xFF00FF00),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Uang tunai pribadi hasil Gaji & Dividen yang siap dibelanjakan.\n(Klik untuk membuka Buku Besar / Riwayat Transaksi)",
                                color = textGray,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (isUnlocked) {
                // B. ASSET ALLOCATION
            item {
                Column {
                    Text(
                        text = "Asset Allocation",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Triple bar allocation slider style
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                    ) {
                        if (pePct > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(pePct.toFloat())
                                    .fillMaxHeight()
                                    .background(Color(0xFFFF9800))
                            )
                        }
                        if (liquidPct > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(liquidPct.toFloat())
                                    .fillMaxHeight()
                                    .background(Color(0xFF2196F3))
                            )
                        }
                        if (tangiblePct > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(tangiblePct.toFloat())
                                    .fillMaxHeight()
                                    .background(Color(0xFF4CAF50))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFFF9800)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("PE Valuation", color = textGray, fontSize = 11.sp)
                                Text("${String.format("%.1f", pePct)}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF2196F3)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Liquid Mkts", color = textGray, fontSize = 11.sp)
                                Text("${String.format("%.1f", liquidPct)}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF4CAF50)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Tangibles", color = textGray, fontSize = 11.sp)
                                Text("${String.format("%.1f", tangiblePct)}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 📈 Private Investment Desk
            item {
                Column {
                    Text(
                        text = "📈 Private Investment Desk",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(125.dp)
                                .clickable { navController.navigate("private_stock_market") }
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E242E))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = "Global Stock Market",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Trading saham mandiri menggunakan kas pribadi",
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(125.dp)
                                .clickable { navController.navigate("my_private_portfolio") }
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E242E))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(28.dp)
                                )
                                Column {
                                    Text(
                                        text = "My Private Portfolio",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Pantau aset saham & capital gain pribadi",
                                        color = Color.LightGray,
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Passive Income Stock Dividends Banner
                    var estimatedStockYieldPerMonth = 0.0
                    playerState.privateStockPortfolio.forEach { owned ->
                        val liveStock = stockList.find { it.ticker == owned.ticker }
                        if (liveStock != null) {
                            val livePrice = liveStock.currentPrice
                            val value = owned.shares * livePrice
                            val stats = com.example.data.getMarketStats(liveStock)
                            estimatedStockYieldPerMonth += value * (stats.dividendYield / 100.0 / 12.0)
                        }
                    }
                    val estimatedStockYieldPerQuarter = estimatedStockYieldPerMonth * 3.0
                    val estimatedStockYieldPerYear = estimatedStockYieldPerMonth * 12.0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF00FF00).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1610))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Paid, 
                                    contentDescription = null,
                                    tint = Color(0xFF00FF00),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Passive Income: Dividen Saham Pribadi",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Estimasi akumulasi pendapatan dividen pasif dari portofolio saham pribadi Anda saat ini:",
                                color = textGray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Bulanan", color = textGray, fontSize = 10.sp)
                                    Text(
                                        text = "$" + String.format("%,.2f", estimatedStockYieldPerMonth),
                                        color = Color(0xFF00FF00),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Kuartalan", color = textGray, fontSize = 10.sp)
                                    Text(
                                        text = "$" + String.format("%,.2f", estimatedStockYieldPerQuarter),
                                        color = Color(0xFF2196F3),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tahunan", color = textGray, fontSize = 10.sp)
                                    Text(
                                        text = "$" + String.format("%,.2f", estimatedStockYieldPerYear),
                                        color = Color(0xFFFFD700),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("private_lifestyle") }
                            .border(2.dp, Color(0xFFFF2E93).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0A14))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🛍️ Lifestyle & Personal Spending",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Nikmati kekayaan Anda! Gadget, Liburan, Langganan, dan Amal.",
                                    color = Color(0xFFFFD1E6),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Paid,
                                contentDescription = null,
                                tint = Color(0xFFFF2E93),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("private_foundation_dashboard") }
                            .border(2.dp, Color(0xFFD4AF37).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🏛️ Private Foundation & Legacy",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Bangun institusi nirlaba, sumbangkan kekayaan pribadi, dan abadikan nama Anda.",
                                    color = Color(0xFFE0E0E0),
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                                if (playerState.foundationLegacyPoints > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Reputasi Abadi: ${playerState.foundationLegacyPoints} Prestige Points",
                                        color = Color(0xFFD4AF37),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                // Note: we can use a simpler line if we want.
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // C. MERGE / LIQUIDITY CHANNELS
            item {
                Column {
                    Text(
                        text = "Liquidity & Extraction Channels",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // 1. CEO Salary Card
                        val pendingText = if (playerState.pendingCeoSalaryPercent != null) "\n⏳ Menunggu Keputusan RUPS..." else ""
                        val estimasiGaji = (megaHoldingMonthlyProfit * (playerState.currentCeoSalaryPercent / 100.0)).toLong()
                        val formattedGaji = if (estimasiGaji > 0) formatCurrencyRingkas(estimasiGaji.toDouble(), false) else "$0"
                        val formattedGajiPercent = String.format(java.util.Locale.US, "%.1f", playerState.currentCeoSalaryPercent)
                        LiquidityCard(
                            title = "💼 Executive Salary & Remuneration",
                            subtitle = "Mencairkan laba holding reguler masuk ke saku pribadi secara berkala tiap bulan.${pendingText}",
                            indicator = "$formattedGaji / bln",
                            percentageText = "Porsi Gaji: $formattedGajiPercent% dari Laba Holding",
                            onClick = null
                        )

                        // 2. Dividends Card
                        val dividendPendingText = if (playerState.pendingDividendPercent != null) "\n⏳ Menunggu RUPS..." else ""
                        val labaEnamBulanTerakhir = playerState.financialHistory.takeLast(6).sumOf { it.netIncome }
                        val estimasiDividenPool = (labaEnamBulanTerakhir * (playerState.currentDividendPercent / 100.0)).toLong()
                        val playerDividenBagian = (estimasiDividenPool * (playerState.companyOwnershipPercent / 100.0)).toLong()
                        val formattedDividen = if (playerDividenBagian > 0) formatCurrencyRingkas(playerDividenBagian.toDouble(), false) else "$0"
                        val formattedDividendPercent = String.format(java.util.Locale.US, "%.1f", playerState.currentDividendPercent)
                        LiquidityCard(
                            title = "📈 Corporate Dividends",
                            subtitle = "Mencairkan kas mengendap (treasury) perusahaan ke saku pribadi secara otomatis.${dividendPendingText}",
                            indicator = "$formattedDividen / 6 bln",
                            percentageText = "Persentase Dividen: $formattedDividendPercent% dari Laba 6 Bulan Terakhir",
                            onClick = null
                        )

                        // 2b. Annual Bonus (Tantiem) Card
                        val tantiemPendingText = if (playerState.pendingTantiemPercent != null) "\n⏳ Menunggu Keputusan RUPS..." else ""
                        val historyAnnualProfit = playerState.financialHistory.takeLast(12).sumOf { it.netIncome }
                        val estimasiTantiemPayout = (historyAnnualProfit * (playerState.currentTantiemPercent / 100.0)).toLong()
                        val formattedTantiem = if (estimasiTantiemPayout > 0) formatCurrencyRingkas(estimasiTantiemPayout.toDouble(), false) else "$0"
                        val formattedTantiemPercent = String.format(java.util.Locale.US, "%.1f", playerState.currentTantiemPercent)
                        LiquidityCard(
                            title = "🎁 Annual Bonus (Tantiem)",
                            subtitle = "Bonus kinerja akhir tahun (Disetujui: $formattedTantiemPercent%). Pencairan setiap siklus 12 bulan.${tantiemPendingText}",
                            indicator = "$formattedTantiem / thn",
                            percentageText = "Estimasi Bonus (Berdasarkan Kinerja 12 Bln Terakhir): $formattedTantiem",
                            onClick = null
                        )

                        // 2c. Corporate Perks Card
                        val monthlyPerksValue = (businessValue * 0.000005).toLong()
                        val formattedPerks = if (monthlyPerksValue > 0) formatCurrencyRingkas(monthlyPerksValue.toDouble(), false) else "$0"
                        LiquidityCard(
                            title = "🛩️ Corporate Perks & Allowances",
                            subtitle = "Tunjangan fasilitas Natura otomatis dari perusahaan (Asuransi VVIP, Akomodasi, Keamanan, dsb) yang menyesuaikan dengan Valuasi Holding Anda.",
                            indicator = "$formattedPerks / bln",
                            percentageText = "Alokasi tunjangan bulanan tunai natura.",
                            indicatorColor = gold,
                            onClick = null
                        )

                        // 3. Lombard Loan Card
                        LiquidityCard(
                            title = "🏛️ Lombard Loan (Leverage)",
                            subtitle = "Meminjam uang tunai instan dari bank komersial dengan agunan saham bursa/holding Anda (LTV Maksimal 20%).",
                            indicator = "Total Utang: ${formatCurrencyRingkas(playerState.personalDebt, false)}",
                            onClick = {
                                showDialogType = "LOMBARD"
                                dialogError = null
                                dialogSuccess = null
                            }
                        )

                        // 3b. Capital Injection (Suntik Modal Satu Arah)
                        LiquidityCard(
                            title = "💉 Suntik Modal ke Kas Perusahaan",
                            subtitle = "Suntikkan modal dana pribadi (Kas Pribadi) langsung ke Kas Utama Perusahaan (One-Way) untuk ekspansi bisnis holding.",
                            indicator = "Kas Pribadi: ${formatCurrencyRingkas(playerState.privateBalance, false)}",
                            onClick = {
                                showDialogType = "CAPITAL_INJECTION"
                                dialogError = null
                                dialogSuccess = null
                            }
                        )

                        // 4. Secondary Sale
                        LiquidityCard(
                            title = "📦 Secondary Market Share Sale",
                            subtitle = "Menjual fraksi kepemilikan saham holding ke institusi/privat investor demi kas likuid masif.",
                            indicator = "Saham Milik: ${String.format("%.1f", playerState.companyOwnershipPercent)}%",
                            onClick = {
                                showDialogType = "SHARE_SALE"
                                dialogError = null
                                dialogSuccess = null
                            }
                        )

                        // 5. Short cut physical assets sales
                        LiquidityCard(
                            title = "🏡 Tangible Asset Liquidation",
                            subtitle = "Buka aset fisik yang ideal seperti mobil, kapal pesiar, ataupun real-estate Anda untuk segera dilikuidasi.",
                            indicator = "Nilai Fisik Semenjana: ${formatCurrencyRingkas(tangibleValue, useShortFormat)}",
                            onClick = {
                                navController.navigate("items")
                            }
                        )

                        // 6. Auditor & Tax Department
                        LiquidityCard(
                            title = "⚖️ Auditor & Tax Department",
                            subtitle = "Kelola PPh 21, Pajak Dividen, dan Pelaporan SPT Tahunan.",
                            indicator = if (playerState.privateTaxServiceLevel > 0) {
                                "✅ Dikelola Otomatis"
                            } else if (playerState.isSptReportedThisYear) {
                                "✅ Terlaporkan"
                            } else {
                                "⚠️ Belum Lapor"
                            },
                            indicatorColor = if (playerState.privateTaxServiceLevel > 0 || playerState.isSptReportedThisYear) {
                                Color(0xFF00FF00)
                            } else {
                                Color(0xFFFF3B30)
                            },
                            percentageText = "Kontribusi Pajak: " + formatCurrencyRingkas(playerState.totalTaxPaid.toDouble(), false),
                            onClick = {
                                navController.navigate("tax_and_audit")
                            }
                        )
                    }
                }
            }
            }
        }
    }

    // ----------------------------------------------------
    // MODAL DIALOGS FOR EXECUTIONS
    // ----------------------------------------------------
    if (showDialogType == "CAPITAL_INJECTION") {
        com.example.privateequity.capitalinjection.CapitalInjectionDialog(
            currentPrivateBalance = playerState.privateBalance,
            currentCompanyCash = playerState.cash,
            onDismiss = { showDialogType = null },
            onConfirm = { amount ->
                val err = viewModel.injectCapitalToCompany(amount)
                if (err != null) {
                    dialogError = err
                } else {
                    dialogSuccess = "Berhasil menyuntikkan modal sebesar ${formatCurrencyRingkas(amount, false)} ke Kas Utama Perusahaan!"
                }
                showDialogType = null
            }
        )
    }

    showDialogType?.takeIf { it != "CAPITAL_INJECTION" }?.let { type ->
        AlertDialog(
            onDismissRequest = { showDialogType = null },
            title = {
                val title = when (type) {
                    "DIVIDEND" -> "Pencairan Dividen Korporat"
                    "LOMBARD" -> "Lombard Loan (Agunan Valuasi)"
                    "SHARE_SALE" -> "Penjualan Saham Pasar Sekunder"
                    else -> ""
                }
                Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = gold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    dialogError?.let {
                        Text(it, color = red, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    dialogSuccess?.let {
                        Text(it, color = neonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    when (type) {
                        "DIVIDEND" -> {
                            var pctSeek by remember { mutableStateOf(10f) }
                            val totalToWithdraw = idleHoldingCash * (pctSeek / 100.0)
                            val taxDeducted = totalToWithdraw * 0.15
                            val netAmount = totalToWithdraw * 0.85

                            Text(
                                text = "Tarik sebagian Kas Diam Mega Holding menuju saldo Tunai Pribadi Anda.",
                                color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Saldo Kas Tersedia: ${formatCurrencyRingkas(idleHoldingCash, false)}",
                                color = textGray, fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "${pctSeek.toInt()}% Dividen Korporat",
                                color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ditarik: ${formatCurrencyRingkas(totalToWithdraw, false)}",
                                color = neonGreen, fontSize = 14.sp
                            )
                            Text(
                                text = "Pajak (15%): -${formatCurrencyRingkas(taxDeducted, false)}",
                                color = red, fontSize = 11.sp
                            )
                            Text(
                                text = "Diterima Bersih: ${formatCurrencyRingkas(netAmount, false)}",
                                color = neonGreen, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold
                            )

                            if (idleHoldingCash > 0) {
                                Slider(
                                    value = pctSeek,
                                    onValueChange = { pctSeek = it },
                                    valueRange = 1f..100f,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        val err = viewModel.withdrawCorporateDividends(pctSeek.toDouble())
                                        if (err != null) {
                                            dialogError = err
                                        } else {
                                            dialogSuccess = "Cair dividen sebesar ${formatCurrencyRingkas(netAmount, false)} bersih berhasil dikirim ke tabungan!"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                                ) {
                                    Text("Eksekusi Tarik Dividen", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).background(Color.White.copy(alpha=0.04f)).padding(12.dp)
                                ) {
                                    Text("Kas Diam Holding kosong. Perusahaan tidak mempunyai likuiditas untuk ditarik.", color = red, fontSize = 12.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }

                        "LOMBARD" -> {
                            var borrowAmt by remember { mutableStateOf("") }
                            var repayAmt by remember { mutableStateOf("") }
                            var showBorrowTab by remember { mutableStateOf(true) }

                            val maxDebtAllowed = (holdingValuation * 0.20).toLong()
                            val remainingQuota = (maxDebtAllowed - playerState.personalDebt).coerceAtLeast(0L)

                            Text(
                                text = "Gunakan valuasi holding saham Anda sebagai agunan pinjaman tunai instan dari bank komite.",
                                color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            TabRow(
                                selectedTabIndex = if (showBorrowTab) 0 else 1,
                                containerColor = Color.Transparent,
                                contentColor = gold,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Tab(selected = showBorrowTab, onClick = { showBorrowTab = true }) {
                                    Text("Pinjam Tunai", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                                }
                                Tab(selected = !showBorrowTab, onClick = { showBorrowTab = false }) {
                                    Text("Bayar Utang", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            if (showBorrowTab) {
                                Text("Agunan Valuasi Holding: ${formatCurrencyRingkas(holdingValuation, false)}", fontSize = 11.sp, color = textGray)
                                Text("Maksimal Plafon LTV (20%): ${formatCurrencyRingkas(maxDebtAllowed, false)}", fontSize = 11.sp, color = textGray)
                                Text("Tersisa Kuota: ${formatCurrencyRingkas(remainingQuota, false)}", fontSize = 12.sp, color = neonGreen, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = borrowAmt,
                                    onValueChange = { borrowAmt = it },
                                    label = { Text("Jumlah Pinjaman (Tunai)") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = gold, unfocusedBorderColor = Color.Gray),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val amt = borrowAmt.toLongOrNull()
                                        if (amt == null || amt <= 0) {
                                            dialogError = "Jumlah masukan tidak valid"
                                        } else {
                                            val err = viewModel.borrowLombardLoan(amt, 0.20)
                                            if (err != null) {
                                                dialogError = err
                                            } else {
                                                dialogSuccess = "Lombard Loan dicairkan! Tunai bertambah ${formatCurrencyRingkas(amt, false)}"
                                                borrowAmt = ""
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                                ) {
                                    Text("Eksekusi Pencairan", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("Kas Pribadi Tersedia: ${formatCurrencyRingkas(playerState.cash, false)}", fontSize = 11.sp, color = textGray)
                                Text("Sisa Saldo Utang: ${formatCurrencyRingkas(playerState.personalDebt, false)}", fontSize = 12.sp, color = red, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = repayAmt,
                                    onValueChange = { repayAmt = it },
                                    label = { Text("Jumlah Pembayaran") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = gold, unfocusedBorderColor = Color.Gray),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val amt = repayAmt.toLongOrNull()
                                        if (amt == null || amt <= 0) {
                                            dialogError = "Jumlah masukan tidak valid"
                                        } else {
                                            val err = viewModel.repayLombardLoan(amt)
                                            if (err != null) {
                                                dialogError = err
                                            } else {
                                                dialogSuccess = "Sisa utang dicicil sejumlah ${formatCurrencyRingkas(amt, false)}"
                                                repayAmt = ""
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen, contentColor = Color.Black)
                                ) {
                                    Text("Bayar Lombard", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        "SHARE_SALE" -> {
                            val maxAllowedSell = (playerState.companyOwnershipPercent - 51.0).toFloat().coerceAtLeast(0f)
                            var sellPct by remember { mutableStateOf(if (maxAllowedSell > 0f) Math.min(5.0f, maxAllowedSell) else 0f) }
                            val incomeGained = (holdingValuation * (sellPct / 100.0)).toLong()

                            Text(
                                text = "Lepaskan beberapa persen saham kepemilikan holding Anda selamanya untuk dicairkan oleh Sovereign Wealth Fund / Privat Investor.",
                                color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Valuasi Mega Holding: ${formatCurrencyRingkas(holdingValuation, false)}",
                                color = textGray, fontSize = 12.sp
                            )
                            Text(
                                text = "Saham Milik Saat Ini: ${String.format("%.1f", playerState.companyOwnershipPercent)}%",
                                color = textGray, fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Jual Saham Sebesar: ${String.format("%.1f", sellPct)}%",
                                color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dana Cair yang Diterima: ${formatCurrencyRingkas(incomeGained, false)}",
                                color = neonGreen, fontSize = 18.sp, fontWeight = FontWeight.Black
                            )

                            if (playerState.companyOwnershipPercent > 51.0) {
                                val maxAllowedSell = (playerState.companyOwnershipPercent - 51.0).toFloat()
                                Slider(
                                    value = sellPct.coerceIn(0.1f, maxAllowedSell),
                                    onValueChange = { sellPct = it },
                                    valueRange = 0.1f..maxAllowedSell,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        val err = viewModel.sellMegaHoldingShares(sellPct.toDouble())
                                        if (err != null) {
                                            dialogError = err
                                        } else {
                                            dialogSuccess = "Saham sekunder terjual! Mendapat kekayaan likuid ${formatCurrencyRingkas(incomeGained, false)}!"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = red, contentColor = Color.White)
                                ) {
                                    Text("Jual Saham Sekunder", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).background(Color.White.copy(alpha=0.04f)).padding(12.dp)
                                ) {
                                    Text("Anda tidak boleh menjual saham lebih banyak karena harus mempertahankan minimal kepemilikan 51% (kontrol perusahaan).", color = red, fontSize = 12.sp, textAlign = TextAlign.Center)
                                }
                            }

                            if (playerState.companyOwnershipPercent < 100.0) {
                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                val maxBuyback = (100.0 - playerState.companyOwnershipPercent).toFloat()
                                var buybackPct by remember { mutableStateOf(Math.min(5.0f, maxBuyback)) }
                                val buybackCost = (holdingValuation * (buybackPct / 100.0)).toLong()

                                Text(
                                    text = "Buyback Saham Dari Investor: ${String.format("%.1f", buybackPct)}%",
                                    color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Biaya Buyback: ${formatCurrencyRingkas(buybackCost, false)}",
                                    color = gold, fontSize = 16.sp, fontWeight = FontWeight.Bold
                                )
                                Slider(
                                    value = buybackPct.coerceIn(0.1f, maxBuyback),
                                    onValueChange = { buybackPct = it },
                                    valueRange = 0.1f..maxBuyback,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val err = viewModel.buybackMegaHoldingShares(buybackPct.toDouble())
                                        if (err != null) {
                                            dialogError = err
                                        } else {
                                            dialogSuccess = "Berhasil membeli kembali ${String.format("%.1f", buybackPct)}% saham dari investor!"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen, contentColor = Color.Black)
                                ) {
                                    Text("Buyback Saham Investor", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialogType = null }) {
                    Text("Tutup", color = gold, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun LiquidityCard(
    title: String,
    subtitle: String,
    indicator: String,
    percentageText: String? = null,
    indicatorColor: Color = Color(0xFF00FF00),
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E242E)),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .border(1.dp, Color.White.copy(alpha=0.06f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (subtitle.contains("\n⏳")) {
                    val parts = subtitle.split("\n⏳")
                    Text(
                        text = parts[0],
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "⏳ " + parts[1],
                        color = Color(0xFFFFD700),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = subtitle,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (percentageText != null) {
                    Column {
                        Text(
                            text = indicator,
                            color = indicatorColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = percentageText,
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF11141A),
                        border = BorderStroke(0.5.dp, Color(0xFFFFD700).copy(alpha=0.3f))
                    ) {
                        Text(
                            text = indicator,
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.padding(start = 12.dp).size(20.dp)
                )
            }
        }
    }
}
