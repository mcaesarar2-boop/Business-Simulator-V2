package com.example

import com.example.viewmodel.GameViewModel


import com.example.data.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.foundation.clickable
import java.text.NumberFormat
import java.util.Locale

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import coil.compose.AsyncImage
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.ui.res.stringResource
import com.example.ui.LanguageSettingsCard

// Format Uang Default
val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 }

// ==========================================
// 1. INVESTING SCREEN (Dashboard Investasi)
// ==========================================
@Composable
fun InvestingScreen(navController: NavHostController, viewModel: GameViewModel) {
    // Theme Colors
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val slateDark = Color(0xFF252A34)
    val gold = Color(0xFFFFD700)
    val darkGold = Color(0xFFB8860B)
    val neonGreen = Color(0xFF00FF00)
    val textGray = Color(0xFFA0A0A0)
    val red = Color(0xFFFF3B30)

    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()
    val monthProgress by viewModel.monthProgress.collectAsState()
    val stockList by viewModel.stockList.collectAsState()

    var activeTab by remember { mutableStateOf("Stocks") }
    val tabs = listOf(
        Pair("Stocks", R.string.investing_tab_stocks),
        Pair("Real Estate", R.string.investing_tab_real_estate),
        Pair("Crypto", R.string.investing_tab_crypto),
        Pair("Startups", R.string.investing_tab_startups)
    )

    var showBuyDialog by remember { mutableStateOf(false) }
    var selectedStockToBuy by remember { mutableStateOf<StockItem?>(null) }
    var buySharesAmount by remember { mutableStateOf("") }
    var buySuccessMessage by remember { mutableStateOf<String?>(null) }

    // Calculate real stock portfolio values
    var totalCostBasis = 0.0
    var currentStocksValue = 0.0
    var estimatedYieldPerMonth = 0.0 // Simplified calculation

    playerState.ownedStocks.forEach { owned ->
        val liveStock = stockList.find { it.ticker == owned.ticker }
        val livePrice = liveStock?.currentPrice ?: owned.averagePrice
        currentStocksValue += owned.shares * livePrice
        
        val safeAveragePrice = if (owned.averagePrice <= 0.0) livePrice else owned.averagePrice
        totalCostBasis += owned.shares * safeAveragePrice
        
        // Let's estimate yield simply based on divYield or just a flat percentage for demonstration
        if (liveStock != null) {
            val stats = getMarketStats(liveStock)
            estimatedYieldPerMonth += (owned.shares * livePrice) * (stats.dividendYield / 100.0 / 12.0)
        }
    }

    val profitLoss = currentStocksValue - totalCostBasis
    val profitLossPct = if (totalCostBasis > 0) (profitLoss / totalCostBasis) * 100 else 0.0
    val profitColor = if (profitLoss >= 0) neonGreen else red
    val profitSign = if (profitLoss >= 0) "+" else ""

    Scaffold(
        containerColor = bgDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Content
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = stringResource(R.string.investing_title),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Custom Tabs
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(androidx.compose.foundation.rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEach { (tabKey, tabResId) ->
                        val isSelected = activeTab == tabKey
                        Column(
                            modifier = Modifier
                                .clickable { activeTab = tabKey }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(tabResId),
                                color = if (isSelected) gold else textGray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (isSelected) {
                                Box(modifier = Modifier.height(2.dp).fillMaxWidth(0.5f).background(gold))
                            } else {
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (activeTab) {
                    "Stocks" -> {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().clickable { navController.navigate("my_portfolio_detail") }.defaultMinSize(minHeight = 140.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 140.dp)) {
                                    coil.compose.AsyncImage(
                                        model = "https://images.unsplash.com/photo-1649003515353-c58a239cf662?q=80&w=1170&auto=format&fit=crop",
                                        contentDescription = null,
                                        modifier = Modifier.matchParentSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(
                                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                                    colors = listOf(Color(0xFF121212).copy(alpha = 0.4f), Color(0xFF121212).copy(alpha = 0.95f))
                                                )
                                            )
                                    )
                                    Column(modifier = Modifier.matchParentSize().padding(24.dp), verticalArrangement = Arrangement.Bottom) {
                                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Text(stringResource(R.string.investing_my_stock_portfolio), color = Color.LightGray, fontSize = 14.sp)
                                            Icon(Icons.Default.ArrowForwardIos, contentDescription = "Detail", tint = gold, modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(com.example.ui.formatCurrencyRingkas(currentStocksValue, useShortFormat), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("$profitSign${com.example.ui.formatCurrencyRingkas(Math.abs(profitLoss), useShortFormat)} (${String.format(java.util.Locale.US, "%.2f", profitLossPct)}%)", color = profitColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(stringResource(R.string.investing_est_yield_month, com.example.ui.formatCurrencyRingkas(estimatedYieldPerMonth, useShortFormat)), color = Color.LightGray, fontSize = 14.sp)
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = { navController.navigate("global_stock_market") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))), 
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(stringResource(R.string.investing_global_stock_market_btn), color = Color.Black, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }

                        item {
                            val stableIncome = stockList
                                .filter { it.changePercentage >= 0 }
                                .sortedBy { it.changePercentage }
                                .take(2)
                            
                            val growthPotential = stockList
                                .sortedByDescending { it.changePercentage }
                                .take(2)

                            Column {
                                Text(stringResource(R.string.investing_stable_income), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                stableIncome.forEach { stock ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(shape = CircleShape, color = cardDark, modifier = Modifier.size(40.dp)) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = gold, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text("${stock.ticker} - ${stock.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                                                Text("${com.example.ui.formatCurrencyRingkas(stock.currentPrice, useShortFormat)} (${String.format(java.util.Locale.US, "%.2f", stock.changePercentage)}%)", color = textGray, fontSize = 12.sp)
                                            }
                                        }
                                        Button(
                                            onClick = { 
                                                selectedStockToBuy = stock
                                                showBuyDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color(0xFFFFD700)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(stringResource(R.string.btn_buy), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                Text(stringResource(R.string.investing_growth_potential), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                growthPotential.forEach { stock ->
                                     Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${stock.ticker} - ${stock.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                                            Text("${com.example.ui.formatCurrencyRingkas(stock.currentPrice, useShortFormat)} (${String.format(java.util.Locale.US, "%+.2f", stock.changePercentage)}%)", color = neonGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Button(
                                            onClick = { 
                                                selectedStockToBuy = stock
                                                showBuyDialog = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color(0xFFFFD700)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text(stringResource(R.string.btn_buy), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    "Real Estate" -> {
                        item { com.example.ui.RealEstateScreen(viewModel) }
                    }
                    "Crypto" -> {
                        item { com.example.ui.CryptoScreen(viewModel) }
                    }
                    "Startups" -> {
                        item { com.example.ui.StartupScreen(viewModel) }
                    }
                }
            }
        }
        // --- Buy Dialog ---
        if (showBuyDialog && selectedStockToBuy != null) {
            val stock = selectedStockToBuy!!
            val isIndo = stock.ticker.contains(".JK")
            val currentPrice = stock.currentPrice
            val balanceStr = com.example.ui.formatCurrencyRingkas(playerState.cash.toDouble(), useShortFormat)
            
            androidx.compose.ui.window.Dialog(onDismissRequest = { showBuyDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardDark,
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Beli Saham", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${stock.name} (${stock.ticker})", color = textGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Harga per Lembar", color = textGray, fontSize = 12.sp)
                        Text(com.example.ui.formatCurrencyRingkas(currentPrice, useShortFormat), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = buySharesAmount,
                            onValueChange = { buySharesAmount = it },
                            label = { Text("Jumlah Lembar") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = neonGreen,
                                unfocusedBorderColor = textGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val quantity = buySharesAmount.toLongOrNull() ?: 0L
                        val totalRequired = quantity * currentPrice
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.investing_buy_total_cost), color = textGray)
                            Text(com.example.ui.formatCurrencyRingkas(totalRequired, useShortFormat), color = if (totalRequired > playerState.cash) red else neonGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(R.string.investing_buy_your_balance), color = textGray)
                            Text(balanceStr, color = Color.White)
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        val canBuy = quantity > 0 && totalRequired <= playerState.cash
                        
                        Button(
                            onClick = {
                                viewModel.buyStock(stock.ticker, currentPrice, quantity)
                                buySuccessMessage = "Berhasil membeli $quantity lembar saham ${stock.ticker}"
                                buySharesAmount = ""
                                showBuyDialog = false
                            },
                            enabled = canBuy,
                            colors = ButtonDefaults.buttonColors(containerColor = neonGreen),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(stringResource(R.string.investing_buy_confirm), color = bgDark, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { showBuyDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.investing_buy_cancel), color = textGray)
                        }
                    }
                }
            }
        }
        
        // --- Success Snackbar/Dialog (optional if wanted) ---
        if (buySuccessMessage != null) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { buySuccessMessage = null }) {
                Surface(shape = RoundedCornerShape(12.dp), color = neonGreen.copy(alpha = 0.2f), border = BorderStroke(1.dp, neonGreen)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(buySuccessMessage!!, color = Color.White, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { buySuccessMessage = null }, colors = ButtonDefaults.buttonColors(containerColor = neonGreen)) {
                            Text("OK", color = bgDark)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. BUSINESS SCREENS (Dashboard, Catalog, Detail)
// ==========================================
@Composable
fun BusinessDashboardScreen(navController: NavHostController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()
    var showMergerDialog by remember { mutableStateOf(false) }
    var showSlotUpgradeDialog by remember { mutableStateOf(false) }
    var showRestructuringDialog by remember { mutableStateOf(false) }

    var totalProjectedIncome = 0L
    playerState.ownedBusinesses.forEach { owned ->
        if (owned.parentId.isNullOrEmpty()) {
            val catalogItem = getCatalogItem(owned.catalogId, playerState)
            if (catalogItem != null) {
                val (rev, _) = getBusinessStats(owned, catalogItem, playerState)
                totalProjectedIncome += rev
            }
        }
    }
    playerState.holdingCompanies.forEach { holding ->
        totalProjectedIncome += com.example.data.CorporateFinanceManager.calculateHoldingMonthlyRevenue(holding, playerState)
    }

    val stockList by viewModel.stockList.collectAsState()
    var estimatedStockYield = 0.0
    playerState.ownedStocks.forEach { owned ->
        val liveStock = stockList.find { it.ticker == owned.ticker }
        if (liveStock != null) {
            val isIndo = owned.ticker.contains(".JK")
            val currentPriceUsd = liveStock.currentPrice
            val stats = getMarketStats(liveStock)
            estimatedStockYield += (owned.shares * currentPriceUsd) * (stats.dividendYield / 100.0 / 12.0)
        }
    }

    val totalMonthlyIncome = totalProjectedIncome + estimatedStockYield.toLong()

    Scaffold(
        containerColor = Color.Transparent // 1. SCREEN BACKGROUND
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 2. HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.business_title),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color.Yellow.copy(alpha = 0.08f),
                            contentColor = Color(0xFFFFD700),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { showRestructuringDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.business_restructuring),
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFFFD700)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.business_restructuring),
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.08f),
                            contentColor = Color(0xFFB39DDB),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB39DDB).copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { showSlotUpgradeDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFB39DDB)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.business_slot_format, playerState.ownedBusinesses.size + playerState.holdingCompanies.size, playerState.maxBusinessSlots),
                                    color = Color(0xFFB39DDB),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. TOTAL INCOME CARD
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 140.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 140.dp)) {
                        coil.compose.AsyncImage(
                            model = "https://plus.unsplash.com/premium_photo-1681469490587-cf7ff1d6fc00?q=80&w=1074&auto=format&fit=crop",
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color(0xFF121212).copy(alpha = 0.4f), Color(0xFF121212).copy(alpha = 0.95f))
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier.matchParentSize().padding(24.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = com.example.ui.formatCurrencyRingkas(totalMonthlyIncome, useShortFormat),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.business_gross_income_format, com.example.ui.formatCurrencyRingkas(estimatedStockYield, useShortFormat)),
                                fontSize = 12.sp,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 3.5 MEGA HOLDING CARD / BUTTON
            item {
                if (playerState.megaHolding.isActive) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("mega_holding_detail") }
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF2C2C2C), Color(0xFF151515))),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.Yellow.copy(alpha = 0.4f), Color.Transparent)), // Garis emas memudar
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "👑 ${playerState.megaHolding.companyName}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFFD700) // Gold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.business_personal_ownership, String.format("%.1f", playerState.megaHolding.ownershipPercentage)),
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.ArrowForwardIos,
                                    contentDescription = "Lihat Detail",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                } else {
                    var showFormDialog by remember { mutableStateOf(false) }
                    Button(
                        onClick = { showFormDialog = true },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2B2B), contentColor = Color(0xFFFFD700))
                    ) {
                        Text(stringResource(R.string.business_form_mega_holding), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    if (showFormDialog) {
                        var mhName by remember { mutableStateOf("") }
                        var includesInv by remember { mutableStateOf(false) }
                        var invCompName by remember { mutableStateOf("") }
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showFormDialog = false },
                            title = { Text("Bentuk Mega Holding", fontWeight = FontWeight.Bold) },
                            text = {
                                Column {
                                    Text("Bentuk satu entitas elit global untuk menyatukan seluruh kerajaan bisnis Anda. Ini akan bertindak sebagai payung utama di mana keseluruhan akumulasi Valuasi dijumlahkan secara total.")
                                    Spacer(modifier = Modifier.height(16.dp))
                                    androidx.compose.material3.OutlinedTextField(
                                        value = mhName,
                                        onValueChange = { mhName = it },
                                        label = { Text("Nama Mega Holding") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        androidx.compose.material3.Checkbox(checked = includesInv, onCheckedChange = { includesInv = it })
                                        Text("Gabungkan portofolio saham/investasi pribadi ke dalam Mega Holding.")
                                    }
                                    if (includesInv) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        androidx.compose.material3.OutlinedTextField(
                                            value = invCompName,
                                            onValueChange = { invCompName = it },
                                            label = { Text("Nama Entitas Perusahaan Investasi (Contoh: PT SRC Investama)") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    if (mhName.isNotBlank() && (!includesInv || invCompName.isNotBlank())) {
                                        viewModel.formMegaHolding(mhName, includesInv, invCompName)
                                        showFormDialog = false
                                    }
                                }) { Text("Konfirmasi", fontWeight = FontWeight.Bold) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showFormDialog = false }) { Text("Batal") }
                            }
                        )
                    }
                }
            }

            // 4. ACTION BUTTONS ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("business_catalog") },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))), // Gold gradient
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(stringResource(R.string.business_start_new), color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Button(
                        onClick = { showMergerDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.05f),
                            contentColor = Color.White
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.business_merger), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // 5. LIST HEADER ("My companies")
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = stringResource(R.string.business_my_businesses),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${playerState.ownedBusinesses.size + playerState.holdingCompanies.size}/${playerState.maxBusinessSlots}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 6. COMPANY CARDS
            if (playerState.ownedBusinesses.isEmpty() && playerState.holdingCompanies.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.business_empty), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                items(playerState.holdingCompanies) { holding ->
                    com.example.ui.HoldingItemCard(
                        holding = holding,
                        rev = com.example.data.CorporateFinanceManager.calculateHoldingMargin(holding, playerState),
                        useShortFormat = useShortFormat,
                        onClick = { navController.navigate("holding_dashboard/${holding.instanceId}") }
                    )
                }
                
                val rootBusinesses = playerState.ownedBusinesses.filter { it.parentId.isNullOrEmpty() || it.acquiredStockTicker != null }
                items(rootBusinesses) { owned ->
                    val stockSector = if (owned.acquiredStockTicker != null) stockList.find { it.ticker == owned.acquiredStockTicker }?.sector else null
                    val catalogItem = getCatalogItem(owned.catalogId, playerState) ?: if (owned.acquiredStockTicker != null) {
                        com.example.data.BusinessCatalogItem(
                            id = owned.catalogId,
                            name = owned.customName ?: "Perusahaan Publik",
                            category = when (stockSector?.uppercase()) {
                                "PROPERTY", "REAL_ESTATE" -> com.example.data.BusinessCategory.PROPERTY
                                "FINANCE", "BANKING" -> com.example.data.BusinessCategory.FINANCE
                                "CONSUMER", "RETAIL" -> com.example.data.BusinessCategory.RETAIL
                                else -> com.example.data.BusinessCategory.PROPERTY
                            },
                            costToBuy = 0L,
                            monthlyRevenue = 0L,
                            monthlyMaintenanceCost = 0L
                        )
                    } else null

                    if (catalogItem != null) {
                        val rev = if (owned.acquiredStockTicker != null) {
                            val liveStock = stockList.find { it.ticker == owned.acquiredStockTicker }
                            val stockInPortfolio = playerState.ownedStocks.find { it.ticker == owned.acquiredStockTicker }
                            val baseDiv = if (liveStock != null && stockInPortfolio != null) {
                                val stats = com.example.data.getMarketStats(liveStock)
                                val monthlyYieldPercent = stats.dividendYield / 12.0 / 100.0
                                (stockInPortfolio.shares * liveStock.currentPrice * monthlyYieldPercent).toLong()
                            } else 0L
                            val myDivisions = playerState.ownedBusinesses.filter { it.parentId == owned.instanceId }
                            val subsidiariesProfit = myDivisions.sumOf { sub ->
                                val cat = getCatalogItem(sub.catalogId, playerState)
                                if (cat != null) {
                                    val (subRev, subMaint) = getBusinessStats(sub, cat, playerState)
                                    subRev - subMaint
                                } else 0L
                            }
                            baseDiv + subsidiariesProfit
                        } else {
                            val (r, _) = getBusinessStats(owned, catalogItem, playerState)
                            r
                        }
                        com.example.ui.BusinessItemCard(
                            owned = owned,
                            catalogItem = catalogItem,
                            rev = rev,
                            useShortFormat = useShortFormat,
                            stockSector = stockSector,
                            onClick = {
                                if (owned.catalogId == "streaming_service") {
                                    navController.navigate("streaming_service_screen/${owned.instanceId}")
                                } else if (owned.catalogId == "content_creator") {
                                    navController.navigate("content_creator_screen/${owned.instanceId}")
                                } else if (owned.catalogId == "mid_logistics" || catalogItem.category == com.example.data.BusinessCategory.LOGISTICS) {
                                    navController.navigate("logistics_dashboard/${owned.instanceId}")
                                } else if (owned.catalogId == "upper_realestate") {
                                    navController.navigate("apartment_property/${owned.instanceId}")
                                } else if (owned.catalogId == "tycoon_bank" || catalogItem.category == com.example.data.BusinessCategory.FINANCE) {
                                    navController.navigate("banking_dashboard/${owned.instanceId}")
                                } else if (catalogItem.category == com.example.data.BusinessCategory.AVIATION) {
                                    navController.navigate("aviation_dashboard/${owned.instanceId}")
                                } else if (catalogItem.category == com.example.data.BusinessCategory.THEME_PARK_HOLDING) {
                                    navController.navigate("theme_park_dashboard/${owned.instanceId}")
                                } else if (catalogItem.category == com.example.data.BusinessCategory.HOSPITALITY) {
                                    navController.navigate("hospitality_dashboard/${owned.instanceId}")
                                } else if (catalogItem.category == com.example.data.BusinessCategory.CRUISE_LINE) {
                                    navController.navigate("cruise_dashboard/${owned.instanceId}")
                                } else if (owned.catalogId == "football_club" || catalogItem.category == com.example.data.BusinessCategory.SPORTS) {
                                    navController.navigate("football_club_dashboard")
                                } else if (owned.catalogId == "indie_game_publisher") {
                                    navController.navigate("game_publisher_hub/${owned.instanceId}")
                                } else if (owned.catalogId == "upper_tech") {
                                    navController.navigate("software_house_dashboard/${owned.instanceId}")
                                } else if (owned.catalogId == "ai_cloud_provider") {
                                    navController.navigate("ai_cloud_dashboard/${owned.instanceId}")
                                } else {
                                    navController.navigate("business_detail/${owned.instanceId}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (showSlotUpgradeDialog) {
        val upgradePrice = viewModel.getBusinessSlotUpgradePrice()
        AlertDialog(
            onDismissRequest = { showSlotUpgradeDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("Tambah Slot Bisnis", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Saat ini Anda memiliki ${playerState.maxBusinessSlots} slot bisnis.", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Harga upgrade untuk slot berikutnya adalah:", color = Color.LightGray)
                    Text(com.example.ui.formatCurrencyRingkas(upgradePrice, useShortFormat), color = Color(0xFF00FF00), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.upgradeBusinessSlot()
                        showSlotUpgradeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                    enabled = playerState.cash >= upgradePrice
                ) {
                    Text("Beli Slot")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSlotUpgradeDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    if (showMergerDialog) {
        var selectedBusinesses by remember { mutableStateOf(setOf<String>()) }
        var mergerName by remember { mutableStateOf("") }
        var mergerType by remember { mutableStateOf("Holding Company") }
        val types = listOf(
            "Entertainment Holdings", "F&B Holdings", "Property Holdings", "Retail Holdings", 
            "Tech Holdings", "Finance Holdings", "Healthcare & Insurance Holdings", "Transportation Holdings"
        )
        var typeExpanded by remember { mutableStateOf(false) }
        
        var totalValuationBase = 0L
        selectedBusinesses.forEach { instanceId ->
            val o = playerState.ownedBusinesses.find { it.instanceId == instanceId }
            val c = o?.let { getCatalogItem(it.catalogId, playerState) }
            if (o != null && c != null) {
                totalValuationBase += c.costToBuy + (o.level * c.costToBuy / 2)
            }
        }
        val estimatedFee = (totalValuationBase * 0.20).toLong()

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showMergerDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF141414)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                        coil.compose.AsyncImage(
                            model = com.example.ui.getHoldingBackgroundImage(mergerType).ifBlank { "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab" },
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xFF141414)),
                                        startY = 0f,
                                        endY = Float.POSITIVE_INFINITY
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            Text("Merger Bisnis", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.Yellow)
                            Text("Pilih minimal 2 bisnis untuk dilebur.", color = Color.LightGray, fontSize = 14.sp)
                        }
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 8.dp)
                    ) {
                        Text("Pilih Bisnis:", fontWeight = FontWeight.SemiBold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Box(modifier = Modifier.weight(1f)) {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val rootBusinesses = playerState.ownedBusinesses.filter { it.parentId.isNullOrEmpty() || it.acquiredStockTicker != null }
                                items(rootBusinesses) { owned ->
                                    val catItem = getCatalogItem(owned.catalogId, playerState)
                                    if (catItem != null) {
                                        val isSelected = selectedBusinesses.contains(owned.instanceId)
                                        val bgColor = if (isSelected) Color.Yellow.copy(alpha = 0.1f) else Color(0xFF1E1E1E)
                                        val borderStroke = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.Yellow) else null
                                        val textColor = if (isSelected) Color.White else Color.LightGray
                                        val displayName = (owned.customName?.takeIf { it.isNotBlank() } ?: catItem.name) + " (Lvl ${owned.level})"
                                        
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedBusinesses = if (isSelected) selectedBusinesses - owned.instanceId else selectedBusinesses + owned.instanceId
                                                },
                                            shape = RoundedCornerShape(12.dp),
                                            color = bgColor,
                                            border = borderStroke
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(displayName, color = textColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                                if (isSelected) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Yellow)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = mergerName,
                            onValueChange = { mergerName = it },
                            label = { Text("Nama Perusahaan Baru", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Yellow,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.Yellow
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { typeExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
                            ) {
                                Text("Tipe: $mergerType")
                            }
                            DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                                types.forEach { t ->
                                    DropdownMenuItem(text = { Text(t) }, onClick = { mergerType = t; typeExpanded = false })
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Biaya Merger:", color = Color.Gray, fontSize = 12.sp)
                                Text("$${com.example.ui.formatCurrencyRingkas(estimatedFee, useShortFormat)}", fontWeight = FontWeight.Bold, color = Color(0xFFE57373))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { showMergerDialog = false }) {
                                    Text("Batal", color = Color.LightGray)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (mergerName.isNotBlank() && selectedBusinesses.size >= 2) {
                                            val success = viewModel.mergeBusinesses(selectedBusinesses.toList(), mergerName, mergerType)
                                            if (success) {
                                                showMergerDialog = false
                                                selectedBusinesses = emptySet()
                                                mergerName = ""
                                                mergerType = "Holding Company"
                                            }
                                        }
                                    },
                                    enabled = selectedBusinesses.size >= 2 && estimatedFee <= playerState.cash && mergerName.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black, disabledContainerColor = Color.DarkGray, disabledContentColor = Color.Gray),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text("Proses Merger", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRestructuringDialog) {
        val rootBusinesses = playerState.ownedBusinesses.filter { it.parentId.isNullOrEmpty() || it.acquiredStockTicker != null }
        val context = androidx.compose.ui.platform.LocalContext.current
        
        var selectedSourceId by remember { mutableStateOf("") }
        var selectedSourceName by remember { mutableStateOf("Pilih Aset Sumber...") }
        var sourceExpanded by remember { mutableStateOf(false) }
        
        var selectedTargetId by remember { mutableStateOf("") }
        var selectedTargetName by remember { mutableStateOf("Pilih Holding/Tujuan...") }
        var isSelectedTargetHolding by remember { mutableStateOf(false) }
        var targetExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showRestructuringDialog = false },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Restrukturisasi Aset Korporat", 
                    color = Color.White, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tata ulang kepemilikan bisnis Anda dengan memindahkan entitas dari Root ke dalam entitas induk (Holding) atau divisi lain.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Pilih Aset yang Akan Dipindah (Source):", 
                        color = Color(0xFFFFD700), 
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { sourceExpanded = true },
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedSourceName,
                                    color = if (selectedSourceId.isEmpty()) Color.Gray else Color.White,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.LightGray
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = sourceExpanded,
                            onDismissRequest = { sourceExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(Color(0xFF1E293B))
                        ) {
                            if (rootBusinesses.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Tidak ada aset di root", color = Color.Gray) },
                                    onClick = { sourceExpanded = false }
                                )
                            } else {
                                rootBusinesses.forEach { owned ->
                                    val name = owned.customName ?: getCatalogItem(owned.catalogId, playerState)?.name ?: "Tanpa Nama"
                                    DropdownMenuItem(
                                        text = { Text(name, color = Color.White) },
                                        onClick = {
                                            selectedSourceId = owned.instanceId
                                            selectedSourceName = name
                                            sourceExpanded = false
                                            
                                            selectedTargetId = ""
                                            selectedTargetName = "Pilih Holding/Tujuan..."
                                            isSelectedTargetHolding = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Pilih Holding Tujuan (Destination):", 
                        color = Color(0xFFFFD700), 
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { targetExpanded = true },
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedTargetName,
                                    color = if (selectedTargetId.isEmpty()) Color.Gray else Color.White,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color.LightGray
                                )
                            }
                        }
                        
                        DropdownMenu(
                            expanded = targetExpanded,
                            onDismissRequest = { targetExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .background(Color(0xFF1E293B))
                        ) {
                            val holdingOptions = playerState.holdingCompanies.map { holding ->
                                Triple(holding.instanceId, "[Holding] ${holding.name}", true)
                            }
                            val businessOptions = rootBusinesses.filter { it.instanceId != selectedSourceId }.map { biz ->
                                val name = biz.customName ?: getCatalogItem(biz.catalogId, playerState)?.name ?: "Tanpa Nama"
                                Triple(biz.instanceId, "[Divisi] $name", false)
                            }
                            val combinedOptions = holdingOptions + businessOptions
                            
                            if (combinedOptions.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Tidak ada holding/tujuan tersedia", color = Color.Gray) },
                                    onClick = { targetExpanded = false }
                                )
                            } else {
                                combinedOptions.forEach { (id, label, isHolding) ->
                                    DropdownMenuItem(
                                        text = { Text(label, color = Color.White) },
                                        onClick = {
                                            selectedTargetId = id
                                            selectedTargetName = label
                                            isSelectedTargetHolding = isHolding
                                            targetExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedSourceId.isNotEmpty() && selectedTargetId.isNotEmpty()) {
                            viewModel.restructureBusiness(
                                sourceInstanceId = selectedSourceId,
                                targetId = selectedTargetId,
                                isTargetHolding = isSelectedTargetHolding
                            )
                            android.widget.Toast.makeText(
                                context, 
                                "Aset berhasil direstrukturisasi!", 
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            showRestructuringDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700), 
                        contentColor = Color.Black,
                        disabledContainerColor = Color.DarkGray,
                        disabledContentColor = Color.Gray
                    ),
                    enabled = selectedSourceId.isNotEmpty() && selectedTargetId.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pindahkan Aset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestructuringDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun BusinessCatalogScreen(navController: NavHostController, viewModel: GameViewModel, holdingId: String? = null, targetParentId: String? = null) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kembali")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("🛒 Katalog Bisnis", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(businessCatalog) { catalogItem ->
                var showBuyDialog by remember { mutableStateOf(false) }
                var customName by remember { mutableStateOf("") }
                var studioType by remember { mutableStateOf("LIVE_ACTION") }
                var vendorId by remember { mutableStateOf<String?>(null) }
                var selectedHub by remember { mutableStateOf("Jakarta (CGK)") }
                val canAfford = playerState.cash >= catalogItem.costToBuy
                val isPhysical = catalogItem.category in listOf(
                    com.example.data.BusinessCategory.PROPERTY,
                    com.example.data.BusinessCategory.RETAIL,
                    com.example.data.BusinessCategory.CULINARY,
                    com.example.data.BusinessCategory.ENTERTAINMENT
                )
                val constructionVendors = playerState.ownedBusinesses.filter { it.catalogId == "construction" } +
                        playerState.holdingCompanies.flatMap { it.subsidiaries }.filter { it.catalogId == "construction" }
                val ownsSoftwareHouse = playerState.ownedBusinesses.any { it.catalogId == "upper_tech" } ||
                        playerState.holdingCompanies.flatMap { it.subsidiaries }.any { it.catalogId == "upper_tech" }
                val meetsPrerequisite = if (catalogItem.id == "ai_cloud_provider") ownsSoftwareHouse else true
                val canBuy = canAfford && meetsPrerequisite

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 160.dp)) {
                        val fallbackImage = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=1470&auto=format&fit=crop"
                        val finalUrl = catalogItem.imageUrl ?: fallbackImage
                        coil.compose.SubcomposeAsyncImage(
                            model = finalUrl,
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0xFF121212).copy(alpha = 0.95f))
                                    )
                                )
                        )
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Spacer(modifier = Modifier.height(60.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = com.example.ui.getSectorIcon(catalogItem.category.name),
                                    contentDescription = null,
                                    tint = Color.Yellow,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = catalogItem.name,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            
                            Text(
                                text = "Sektor: ${catalogItem.category.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "Potensi Pendapatan: ${if(catalogItem.isFluctuating) "Fluktuaktif" else com.example.ui.formatCurrencyRingkas(catalogItem.monthlyRevenue, useShortFormat) + "/bln"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Biaya Perawatan: ${com.example.ui.formatCurrencyRingkas(catalogItem.monthlyMaintenanceCost, useShortFormat)}/bln",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF6B6B),
                                fontWeight = FontWeight.SemiBold
                            )

                            if (!meetsPrerequisite) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFE65100).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Terkunci",
                                        tint = Color(0xFFFFB74D),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Prasyarat: Wajib memiliki Software House & SaaS Studio",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFFB74D),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { 
                                    if (catalogItem.id == "football_club") {
                                        val route = if (!holdingId.isNullOrEmpty()) "football_club_acquisition/$holdingId" else "football_club_acquisition"
                                        navController.navigate(route)
                                    } else {
                                        showBuyDialog = true 
                                    }
                                },
                                enabled = canBuy,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canBuy) Color(0xFFFFD700) else Color.DarkGray,
                                    disabledContainerColor = Color.DarkGray
                                )
                            ) {
                                Text(
                                    text = if (!meetsPrerequisite) "Terkunci (Butuh Software House)" else if (canAfford) "Beli ${com.example.ui.formatCurrencyRingkas(catalogItem.costToBuy, useShortFormat)}" else "Dana Tidak Cukup",
                                    color = if (canBuy) Color.Black else Color.LightGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                if (showBuyDialog) {
                    AlertDialog(
                        onDismissRequest = { showBuyDialog = false },
                        title = { Text("Beli ${catalogItem.name}") },
                        text = {
                            Column {
                                Text("Beri nama khusus untuk bisnis ini (Opsional):")
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customName,
                                    onValueChange = { customName = it },
                                    label = { Text("Nama Bisnis") }
                                )
                                if (catalogItem.id == "media_production") {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Tipe Studio (Penting!):", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = studioType == "LIVE_ACTION", onClick = { studioType = "LIVE_ACTION" })
                                        Text("Live-Action Studio", fontSize = 14.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = studioType == "ANIMATION", onClick = { studioType = "ANIMATION" })
                                        Text("Animation Studio", fontSize = 14.sp)
                                    }
                                }
                                
                                 if (isPhysical && constructionVendors.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Vendor Konstruksi:", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = vendorId == null, onClick = { vendorId = null })
                                        Text("Vendor Eksternal (Instan)", fontSize = 14.sp)
                                    }
                                    
                                    constructionVendors.forEach { vendor ->
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = vendorId == vendor.instanceId, onClick = { vendorId = vendor.instanceId })
                                            Column {
                                                Text("Vendor In-House: ${vendor.customName ?: "PT Konstruksi"}", fontSize = 14.sp)
                                                Text("(Otomatis Profit 40% margin, proses 3 bulan)", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val firstHub: String? = null
                                if (!holdingId.isNullOrEmpty()) {
                                    viewModel.buyBusinessForHolding(holdingId, catalogItem.id, customName.takeIf { it.isNotBlank() }, studioType, vendorId, firstHub)
                                } else {
                                    viewModel.buyBusiness(catalogItem.id, customName.takeIf { it.isNotBlank() }, studioType, vendorId, firstHub, parentId = targetParentId)
                                }
                                showBuyDialog = false
                                navController.popBackStack()
                            }) {
                                Text("Beli")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showBuyDialog = false }) {
                                Text("Batal")
                            }
                        }
                    )
                }
            }
        }
    }
}

fun Modifier.premiumContainer() = this
    .shadow(8.dp, RoundedCornerShape(16.dp))
    .background(
        brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))
        ),
        shape = RoundedCornerShape(16.dp)
    )
    .border(
        width = 1.dp,
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp)
    )

fun Modifier.premiumUpgradeContainer() = this
    .background(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp)
    )
    .border(
        width = 1.dp,
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp)
    )

@Composable
fun BusinessDetailScreen(navController: NavHostController, viewModel: GameViewModel, instanceId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()
    val stockList by viewModel.stockList.collectAsState()
    val ownedData = playerState.ownedBusinesses.find { it.instanceId == instanceId }
        ?: playerState.ownedBusinesses.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
    val stockSector = ownedData?.let { data -> if (data.acquiredStockTicker != null) stockList.find { it.ticker == data.acquiredStockTicker }?.sector else null }
    val catalogItem = ownedData?.let { data ->
        getCatalogItem(data.catalogId, playerState) ?: if (data.acquiredStockTicker != null) {
            com.example.data.BusinessCatalogItem(
                id = data.catalogId,
                name = data.customName ?: "Perusahaan Publik",
                category = when (stockSector?.uppercase()) {
                    "PROPERTY", "REAL_ESTATE" -> com.example.data.BusinessCategory.PROPERTY
                    "FINANCE", "BANKING" -> com.example.data.BusinessCategory.FINANCE
                    "CONSUMER", "RETAIL" -> com.example.data.BusinessCategory.RETAIL
                    else -> com.example.data.BusinessCategory.PROPERTY
                },
                costToBuy = 0L,
                monthlyRevenue = 0L,
                monthlyMaintenanceCost = 0L
            )
        } else null
    }

    if (catalogItem == null || ownedData == null) {
        Text("Data tidak ditemukan.", modifier = Modifier.padding(16.dp))
        return
    }

    val ctx = androidx.compose.ui.platform.LocalContext.current

    if (ownedData.catalogId == "media_radio") {
        com.example.ui.EventOrganizerScreen(navController, viewModel, instanceId)
        return
    }

    if (ownedData.catalogId == "upper_tech") {
        com.example.ui.SoftwareHouseDashboardScreen(navController, viewModel, instanceId)
        return
    }

    var showCapitalDialog by remember { mutableStateOf(false) }
    var capitalInput by remember { mutableStateOf("") }
    var actionType by remember { mutableStateOf("suntik") } // "suntik" or "tarik"

    var showLiquidateDialog by remember { mutableStateOf(false) }

    val (currentRev, currentMaint) = getBusinessStats(ownedData, catalogItem, playerState)
    
    val acquiredTicker = ownedData.acquiredStockTicker
    val isAcquired = acquiredTicker != null
    
    val valuationOriginal: Long
    val monthlyIncomeStrOriginal: String
    var monthlyMaintStrOriginal = ""
    
    if (isAcquired && acquiredTicker != null) {
        val stock = stockList.find { it.ticker == acquiredTicker }
        val stockInPortfolio = playerState.ownedStocks.find { it.ticker == acquiredTicker }
        val livePrice = stock?.currentPrice ?: stockInPortfolio?.averagePrice ?: 0.0
        val shares = stockInPortfolio?.shares ?: 0L
        val initialVal = (shares * livePrice).toLong()
        
        val myDivisions = playerState.ownedBusinesses.filter { it.parentId == instanceId }
        val subsidiariesValuation = myDivisions.sumOf { sub ->
            val cat = getCatalogItem(sub.catalogId, playerState)
            if (cat != null) getBusinessValuation(sub, cat) else 0L
        }
        valuationOriginal = initialVal + ownedData.companyCash.toLong() + subsidiariesValuation
        
        val stats = if (stock != null) com.example.data.getMarketStats(stock) else null
        val yieldPercent = stats?.let { it.dividendYield / 12.0 / 100.0 } ?: 0.0
        val estIncomeFromShares = (initialVal * yieldPercent).toLong()
        
        var totalSubsValProfit = 0L
        myDivisions.forEach { sub ->
            val cat = getCatalogItem(sub.catalogId, playerState)
            if (cat != null) {
                val (subRev, subMaint) = getBusinessStats(sub, cat, playerState)
                totalSubsValProfit += (subRev - subMaint)
            }
        }
        val estIncomeTotal = estIncomeFromShares + totalSubsValProfit
        monthlyIncomeStrOriginal = "+${com.example.ui.formatCurrencyRingkas(estIncomeTotal, useShortFormat)}"
    } else {
        valuationOriginal = getBusinessValuation(ownedData, catalogItem)
        monthlyIncomeStrOriginal = "+${com.example.ui.formatCurrencyRingkas(currentRev, useShortFormat)}"
        monthlyMaintStrOriginal = "-${com.example.ui.formatCurrencyRingkas(currentMaint, useShortFormat)}"
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kembali")
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (catalogItem.imageUrl != null) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp))) {
                coil.compose.SubcomposeAsyncImage(
                    model = catalogItem.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Box(
                    modifier = Modifier.matchParentSize().background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF121212)),
                            startY = 100f
                        )
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        val currFormat = remember { java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US).apply { maximumFractionDigits = 0 } }
        
        val valuation = valuationOriginal
        val monthlyIncomeStr = monthlyIncomeStrOriginal
        var monthlyMaintStr = monthlyMaintStrOriginal

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(ownedData.customName ?: catalogItem.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                if (isAcquired && acquiredTicker != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) {
                        Text("🏢 Anak Usaha Publik (Eks: $acquiredTicker)", color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                } else {
                    Text("Sektor: ${catalogItem.category.name} • Level ${ownedData.level}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                }
            }
            Box(
                modifier = Modifier.premiumContainer()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.End) {
                    Text("Valuasi Usaha", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                    Text(com.example.ui.formatCurrencyRingkas(valuation, useShortFormat), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00C853))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Corporate Treasury Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .premiumContainer()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Kas Internal (Treasury)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha=0.7f))
                    Text(
                        text = com.example.ui.formatCurrencyRingkas(ownedData.companyCash.toLong(), useShortFormat),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = { actionType = "tarik"; capitalInput = ""; showCapitalDialog = true },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Tarik Dividen")
                    }
                    FilledIconButton(
                        onClick = { actionType = "suntik"; capitalInput = ""; showCapitalDialog = true },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Suntik Dana")
                    }
                }
            }
        }
        
        if (isAcquired) {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { 
                    navController.navigate("business_catalog?targetParentId=${instanceId}")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD54F),
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Divisi", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (catalogItem.category == BusinessCategory.FINANCE) {
            val phase = when (ownedData.level) {
                in 1..10 -> "Microfinance/Credit Union"
                in 11..25 -> "Local City Bank"
                in 26..50 -> "National Commercial Bank"
                else -> "Global Investment Mega-Bank"
            }
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.2f))) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Global Finance Empire", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Text("Fase Saat Ini: $phase", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        
        if (catalogItem.category == BusinessCategory.ENTERTAINMENT && catalogItem.id == "media_production") {
            com.example.ui.FilmProductionForm(
                owned = ownedData,
                playerCash = ownedData.companyCash.toLong(),
                useShortFormat = useShortFormat,
                currentMonth = playerState.inGameMonth,
                currentYear = playerState.inGameYear,
                onProduce = { title, budget, promoBudget, genres, isGlobal, schedMonth, schedYear, format, focus, releaseDate, targetDuration ->
                    viewModel.produceMovie(instanceId, title, budget, promoBudget, genres, isGlobal, schedMonth, schedYear, format, focus, releaseDate, targetDuration)
                },
                onPolish = { title, budgetCost, extraMonths ->
                    viewModel.polishMovieProject(instanceId, title, budgetCost, extraMonths)
                },
                onSchedule = { title, schedStr ->
                    viewModel.scheduleMovieRelease(instanceId, title, schedStr)
                },
                onCancel = { title, refundAmount ->
                    viewModel.cancelMovieProject(instanceId, title, refundAmount)
                },
                onOpenHistory = {
                    navController.navigate("ip_history/$instanceId")
                }
            )
        }

        if (catalogItem.category == BusinessCategory.ENTERTAINMENT && catalogItem.id == "media_tv") {
            com.example.ui.TvStationDashboardScreen(
                ownedBusiness = ownedData,
                playerState = playerState,
                useShortFormat = useShortFormat,
                viewModel = viewModel,
                navController = navController
            )
        }

        if (catalogItem.id == "upper_tech") {
            com.example.ui.SoftwareHouseDashboard(
                appProjects = playerState.appProjects,
                businessLevel = ownedData.level,
                ownedBusinesses = playerState.ownedBusinesses,
                playerCash = ownedData.companyCash.toLong(),
                useShortFormat = useShortFormat,
                onStartProject = { title, type, budget, rev, duration, targetId ->
                    viewModel.startAppProject(title, type, budget, rev, duration, targetId)
                },
                onSellSaaS = { projId ->
                    viewModel.sellSaaSProject(projId)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (catalogItem.id == "fine_dining") {
            val ctx = androidx.compose.ui.platform.LocalContext.current
            com.example.ui.RestaurantDashboard(
                ownedBusiness = ownedData,
                playerCash = playerState.cash,
                useShortFormat = useShortFormat,
                onOpenBranch = { cost, branchName ->
                    val error = viewModel.openRestaurantBranch(instanceId, cost, branchName)
                    if (error != null) android.widget.Toast.makeText(ctx, error, android.widget.Toast.LENGTH_SHORT).show()
                    else android.widget.Toast.makeText(ctx, "Cabang baru sedang direnovasi!", android.widget.Toast.LENGTH_SHORT).show()
                },
                onUpgradeBranch = { branchId, option, cost ->
                    val error = viewModel.upgradeRestaurantBranch(instanceId, branchId, option, cost)
                    if (error != null) android.widget.Toast.makeText(ctx, error, android.widget.Toast.LENGTH_SHORT).show()
                },
                onUpgradeParent = { cost ->
                    val error = viewModel.startParentBusinessRealtimeUpgrade(instanceId, cost)
                    if (error != null) android.widget.Toast.makeText(ctx, error, android.widget.Toast.LENGTH_SHORT).show()
                    else android.widget.Toast.makeText(ctx, "Upgrade restoran dimulai!", android.widget.Toast.LENGTH_SHORT).show()
                },
                onFinishParentUpgrade = {
                    viewModel.finishBusinessRealtimeUpgrade(instanceId)
                },
                onFinishBranchUpgrade = { branchId ->
                    viewModel.finishRestaurantBranchRealtimeUpgrade(instanceId, branchId)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (catalogItem.id == "construction") {
            val hasLogistics = playerState.ownedBusinesses.any { it.catalogId == "mid_logistics" } ||
                    playerState.holdingCompanies.any { h -> h.subsidiaries.any { it.catalogId == "mid_logistics" } }

            com.example.ui.ConstructionDashboard(
                business = ownedData,
                playerCash = playerState.cash,
                useShortFormat = useShortFormat,
                hasLogisticsSynergy = hasLogistics,
                onAllocatePhase = { projectId ->
                    viewModel.allocateConstructionPhase(instanceId, projectId)
                },
                onSubmitBid = { tenderId, bidAmount, useCompanyCash, useInHouseLogistics ->
                    viewModel.submitConstructionTenderBid(instanceId, tenderId, bidAmount, useCompanyCash, useInHouseLogistics)
                },
                onResolveEvent = { projectId, actionChoice ->
                    viewModel.resolveConstructionEvent(instanceId, projectId, actionChoice)
                },
                onUpgradeCapacity = { upgradeType, useCompanyCash ->
                    viewModel.upgradeConstructionCapacity(instanceId, upgradeType, useCompanyCash)
                },
                onRefreshMarket = {
                    viewModel.refreshConstructionTenderMarket(instanceId)
                },
                onStartTender = { name, contractValue, duration, initialCapital, useCompanyCash ->
                    viewModel.startConstructionTender(instanceId, name, contractValue, duration, initialCapital, useCompanyCash)
                },
                onTakeClientProject = { projectId ->
                    viewModel.takeClientProject(instanceId, projectId)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (catalogItem.id == "healthcare") {
            val constructionVendors = playerState.ownedBusinesses.filter { it.catalogId == "construction" } +
                    playerState.holdingCompanies.flatMap { it.subsidiaries }.filter { it.catalogId == "construction" }

            com.example.ui.HealthcareDashboard(
                units = ownedData.healthcareSubsidiaries,
                activeEpidemic = ownedData.activeEpidemicEvent,
                playerCash = playerState.cash,
                useShortFormat = useShortFormat,
                constructionVendors = constructionVendors,
                onBuildUnit = { name, type, vendorId, level ->
                    viewModel.buildHealthcareUnit(instanceId, name, type, vendorId, level)
                },
                onUpgradeDepartment = { unitId, dept ->
                    viewModel.upgradeMedicalDepartment(instanceId, unitId, dept)
                },
                onUpdatePremium = { unitId, prem ->
                    viewModel.updateInsurancePremium(instanceId, unitId, prem)
                },
                onInjectCash = { unitId, amt ->
                    viewModel.injectHealthcareUnitCash(instanceId, unitId, amt)
                },
                onUpgradeTier = { unitId ->
                    viewModel.upgradeHealthcareTier(instanceId, unitId)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Financial Stats
        Box(modifier = Modifier.fillMaxWidth().premiumContainer()) {
            Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(if (ownedData.acquiredStockTicker != null) "Estimasi Pemasukan / Bulan" else "Pendapatan / Bulan", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelMedium)
                    Text(monthlyIncomeStr, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                if (ownedData.acquiredStockTicker == null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Biaya / Bulan", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelMedium)
                        Text(monthlyMaintStr, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        if (ownedData.acquiredStockTicker == null) {
            Text("🔥 Upgrade Tersedia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        }

        if (ownedData.acquiredStockTicker != null) {
            val myDivisions = playerState.ownedBusinesses.filter { it.parentId == instanceId }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Daftar Divisi (${myDivisions.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            if (myDivisions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .premiumUpgradeContainer()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada divisi. Klik \"Tambah Divisi\" di atas untuk ekspansi!", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                items(myDivisions) { sub ->
                    val subCatalog = getCatalogItem(sub.catalogId, playerState)
                    if (subCatalog != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    navController.navigate("business_detail/${sub.instanceId}")
                                }
                                .premiumUpgradeContainer()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sub.customName ?: subCatalog.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("Sektor: ${subCatalog.category.name} • Level ${sub.level}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                                    }
                                    val (subRev, subMaint) = getBusinessStats(sub, subCatalog, playerState)
                                    val estProfit = subRev - subMaint
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "+${com.example.ui.formatCurrencyRingkas(subRev, useShortFormat)}/bln",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "-${com.example.ui.formatCurrencyRingkas(subMaint, useShortFormat)}/bln",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "Profit: ${com.example.ui.formatCurrencyRingkas(estProfit, useShortFormat)}/bln",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (estProfit >= 0) Color(0xFF00C853) else MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (ownedData.acquiredStockTicker == null) {
            val upgradesToDisplay = if (catalogItem.id == "media_production") {
                val isAnim = ownedData.studioType == "ANIMATION"
                catalogItem.upgrades.filter { up ->
                    if (isAnim) up.id.startsWith("anim_") || (up.id == "1" && ownedData.purchasedUpgrades.contains("1"))
                    else up.id.startsWith("la_") || (up.id == "1" && ownedData.purchasedUpgrades.contains("1"))
                }
            } else {
                catalogItem.upgrades
            }
            items(upgradesToDisplay) { upgrade ->
                val activeUpgrade = ownedData.activeUpgrades.find { it.selectedUpgradeId == upgrade.id }
                val isUpgrading = activeUpgrade != null
                
                var remainingTimeSecs by remember { mutableStateOf(0L) }
                LaunchedEffect(activeUpgrade, remainingTimeSecs) {
                    if (activeUpgrade != null) {
                        while(true) {
                            val rem = (activeUpgrade.finishTimeMs - System.currentTimeMillis()) / 1000
                            remainingTimeSecs = rem.coerceAtLeast(0)
                            if (rem <= 0) break
                            kotlinx.coroutines.delay(1000)
                        }
                    }
                }
                
                val currentLevel = ownedData.upgradeLevels[upgrade.id] ?: if (ownedData.purchasedUpgrades.contains(upgrade.id)) 1 else 0
                val isMaxedOut = currentLevel >= upgrade.maxLevel
                val cost = getUpgradeCost(upgrade, currentLevel)
                val canAfford = ownedData.companyCash >= cost

                Box(modifier = Modifier.fillMaxWidth().premiumUpgradeContainer()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${upgrade.name} (Lvl $currentLevel/${if (upgrade.maxLevel > 1) upgrade.maxLevel else "-" })", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if(isMaxedOut) MaterialTheme.colorScheme.primary else Color.White)
                            if (isMaxedOut) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Maksimal", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(upgrade.description, style = MaterialTheme.typography.bodySmall, color = if(isMaxedOut) MaterialTheme.colorScheme.primary.copy(alpha=0.7f) else Color.White.copy(alpha = 0.6f))
                        
                        if (!isMaxedOut && !isUpgrading) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Kas Perusahaan (Internal):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "${com.example.ui.formatCurrencyRingkas(ownedData.companyCash.toLong(), useShortFormat)} / ${com.example.ui.formatCurrencyRingkas(cost, useShortFormat)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAfford) Color(0xFF00C853) else Color(0xFFFF5252)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val progress = if (cost > 0) (ownedData.companyCash / cost.toDouble()).coerceIn(0.0, 1.0).toFloat() else 1.0f
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = if (canAfford) Color(0xFF00C853) else Color(0xFFFF9800),
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                            if (!canAfford) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "💡 Uang kurang? Gunakan 'Suntik Dana' dari Dompet Pribadi (${com.example.ui.formatCurrencyRingkas(playerState.cash, useShortFormat)}) di menu Treasury atas.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFB74D),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (isUpgrading) {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Memproses... ($remainingTimeSecs dtk)")
                            }
                        } else if (!isMaxedOut) {
                            Button(
                                onClick = { 
                                    val error = viewModel.purchaseUpgrade(instanceId, upgrade.id)
                                    if (error != null) {
                                        android.widget.Toast.makeText(ctx, error, android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(ctx, "Upgrade ${upgrade.name} berhasil diproses!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = canAfford,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = if (canAfford) MaterialTheme.colorScheme.secondary else Color(0xFF333333))
                            ) {
                                Text(
                                    if (canAfford) "Upgrade (${com.example.ui.formatCurrencyRingkas(cost, useShortFormat)})" 
                                    else "Kas Perusahaan Kurang (${com.example.ui.formatCurrencyRingkas(cost, useShortFormat)})", 
                                    color = if (canAfford) MaterialTheme.colorScheme.onSecondary else Color.LightGray
                                )
                            }
                        } else {
                            Text("Level Maksimal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedButton(
                    onClick = { showLiquidateDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935))
                ) {
                    Text("Likuidasi Usaha (Jual)", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showLiquidateDialog) {
        val valuationToReceive = valuationOriginal
        AlertDialog(
            onDismissRequest = { showLiquidateDialog = false },
            title = { Text("Likuidasi Usaha", color = Color(0xFFE53935), fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah Anda yakin ingin menggulung tikar dan menjual bisnis ini? Anda akan menerima suntikan dana sebesar $${com.example.ui.formatCurrencyRingkas(valuationToReceive, useShortFormat)}.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.liquidateBusiness(instanceId)
                        showLiquidateDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Jual & Tutup Bisnis", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLiquidateDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showCapitalDialog) {
        val parsedInput = capitalInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
        AlertDialog(
            onDismissRequest = { showCapitalDialog = false },
            title = { Text(if (actionType == "suntik") "Suntik Dana Modal" else "Tarik Dividen") },
            text = {
                Column {
                    val parentHolding = playerState.holdingCompanies.find { h -> h.subsidiaries.any { it.instanceId == instanceId } }
                    val parentName = parentHolding?.name
                    val parentCashDesc = if (parentHolding != null) com.example.ui.formatCurrencyRingkas(parentHolding.holdingCash.toLong(), useShortFormat) else com.example.ui.formatCurrencyRingkas(playerState.cash, useShortFormat)

                    if (parentName != null) {
                        Text("Sumber/Tujuan Dana: Kas Internal $parentName")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kas Induk ($parentName): $parentCashDesc")
                    } else {
                        Text("Sumber/Tujuan Dana: Global Balance")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Global Balance: $parentCashDesc")
                    }
                    Text("Kas Internal Bisnis: ${com.example.ui.formatCurrencyRingkas(ownedData.companyCash.toLong(), useShortFormat)}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = capitalInput,
                        onValueChange = { newValue ->
                            val digits = newValue.filter { it.isDigit() }
                            if (digits.isEmpty()) { capitalInput = "" } else {
                                val p = digits.toLongOrNull()
                                if (p != null) capitalInput = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(p)
                            }
                        },
                        label = { Text("Jumlah (USD)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true,
                        leadingIcon = { Text("$", modifier = Modifier.padding(start = 12.dp)) }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (parsedInput > 0) {
                        if (actionType == "suntik") {
                            viewModel.injectCapitalToBusiness(instanceId, parsedInput)
                        } else {
                            viewModel.withdrawCapitalFromBusiness(instanceId, parsedInput)
                        }
                    }
                    showCapitalDialog = false
                }) {
                    Text("Eksekusi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCapitalDialog = false }) { Text("Batal") }
            }
        )
    }

}

// ==========================================
// 3. EARNINGS SCREEN (Laporan Keuangan)
// ==========================================
@Composable
fun EarningsScreen(viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()
    val monthProgress by viewModel.monthProgress.collectAsState()
    
    val earningsData by viewModel.earningsReport.collectAsState()
    
    // Hitung estimasi (Projected)
    var projectedIncome = playerState.ownedBusinesses.filter { it.parentId.isNullOrEmpty() }.sumOf { it.calculateGrossRevenue() } +
            playerState.holdingCompanies.sumOf { com.example.data.CorporateFinanceManager.calculateHoldingMonthlyRevenue(it, playerState) } +
            earningsData.monthlyRentIncome + earningsData.monthlyDividendIncome
    val totalMonthlyDebtObligation = playerState.totalMonthlyDebtObligation
    var projectedExpense = totalMonthlyDebtObligation +
            playerState.ownedBusinesses.filter { it.parentId.isNullOrEmpty() }.sumOf { it.calculateTotalExpenses() } +
            playerState.holdingCompanies.sumOf { com.example.data.CorporateFinanceManager.calculateHoldingMonthlyMaintenance(it, playerState) } +
            playerState.rentedHouses.sumOf { it.monthlyRent.toLong() }
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            // Header & Time Tracker
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.earnings_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Column(horizontalAlignment = Alignment.End) {
                    Text(com.example.ui.formatGlobalDate(playerState.inGameMonth, playerState.inGameYear), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Payday
            Text("Progres ke Payday (Akhir Bulan):", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { monthProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            // Global Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Layer 1: Background Chart
                    coil.compose.AsyncImage(
                        model = "https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3",
                        contentDescription = null,
                        modifier = Modifier.matchParentSize().alpha(0.4f),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    
                    // Layer 2: Gradient Overlay
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFF121212).copy(alpha = 0.9f))
                                )
                            )
                    )
                    
                    // Layer 3: Konten Teks
                    Column(modifier = Modifier.padding(20.dp).fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Uang Tunai Senggang (Balance)", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                com.example.ui.formatCurrencyRingkas(playerState.cash.toDouble(), useShortFormat), 
                                style = MaterialTheme.typography.displayMedium, 
                                color = Color(0xFFFFD700), 
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Estim. Pemasukan", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                                Text(com.example.ui.formatCurrencyRingkas(projectedIncome, useShortFormat), color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                            }
                            
                            val netProfitVal = playerState.lastMonthNetProfit - totalMonthlyDebtObligation
                            val profitColor = if (netProfitVal >= 0) Color(0xFF4CAF50) else Color(0xFFFF6B6B)
                            val profitSign = if (netProfitVal >= 0) "+" else "-"
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Net Profit (Bulan)", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                                Text(
                                    "$profitSign ${com.example.ui.formatCurrencyRingkas(kotlin.math.abs(netProfitVal.toDouble()), useShortFormat)}", 
                                    style = MaterialTheme.typography.titleMedium, 
                                    color = profitColor, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Estim. Pengeluaran", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                                Text(com.example.ui.formatCurrencyRingkas(projectedExpense, useShortFormat), color = Color(0xFFFF6B6B), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Pemasukan (Cash Flow)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        if (playerState.ownedBusinesses.isEmpty() && playerState.holdingCompanies.isEmpty() && earningsData.monthlyRentIncome == 0L && earningsData.monthlyDividendIncome == 0L) {
            item {
                Text("Belum ada sumber pendapatan. Beli bisnis untuk menghasilkan uang!", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(playerState.ownedBusinesses) { owned ->
                val rev = owned.calculateGrossRevenue()
                val revStr = com.example.ui.formatCurrencyRingkas(rev, useShortFormat)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                    Text(
                        text = "${owned.name} (Lvl ${owned.level})",
                        color = Color.LightGray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "+$revStr",
                        color = Color.Green,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
            items(playerState.holdingCompanies) { holding ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                    Text(
                        text = "${holding.name} (Holding)",
                        color = Color.LightGray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    val rev = com.example.data.CorporateFinanceManager.calculateHoldingMargin(holding, playerState)
                    Text(
                        text = "+${com.example.ui.formatCurrencyRingkas(rev, useShortFormat)}",
                        color = Color.Green,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
            if (earningsData.monthlyRentIncome > 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color.Green,
                            modifier = Modifier.size(24.dp).padding(end = 8.dp)
                        )
                        Text(
                            text = "Uang Sewa (Properti)",
                            color = Color.LightGray,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "+${com.example.ui.formatCurrencyRingkas(earningsData.monthlyRentIncome, useShortFormat)}",
                            color = Color.Green,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
            if (earningsData.monthlyDividendIncome > 0) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color.Green,
                            modifier = Modifier.size(24.dp).padding(end = 8.dp)
                        )
                        Text(
                            text = "Dividen (Saham)",
                            color = Color.LightGray,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "+${com.example.ui.formatCurrencyRingkas(earningsData.monthlyDividendIncome, useShortFormat)}",
                            color = Color.Green,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Pengeluaran (Expenses)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        
        if (playerState.ownedBusinesses.isEmpty() && playerState.holdingCompanies.isEmpty() && playerState.rentedHouses.isEmpty()) {
            item {
                Text("Belum ada pengeluaran rutin.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            items(playerState.ownedBusinesses) { owned ->
                val maint = owned.calculateTotalExpenses()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingDown,
                        contentDescription = null,
                        tint = Color(0xFFE57373),
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                    Text(
                        text = "${owned.name} (Maintenance)",
                        color = Color.LightGray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "-${com.example.ui.formatCurrencyRingkas(maint, useShortFormat)}",
                        color = Color(0xFFE57373),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
            items(playerState.holdingCompanies) { holding ->
                val maint = com.example.data.CorporateFinanceManager.calculateHoldingMonthlyMaintenance(holding, playerState)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingDown,
                        contentDescription = null,
                        tint = Color(0xFFE57373),
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                    Text(
                        text = "${holding.name} (Maintenance Mgt)",
                        color = Color.LightGray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "-${com.example.ui.formatCurrencyRingkas(maint, useShortFormat)}",
                        color = Color(0xFFE57373),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
            items(playerState.rentedHouses) { rented ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingDown,
                        contentDescription = null,
                        tint = Color(0xFFE57373),
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                    Text(
                        text = "Sewa Tempat Tinggal",
                        color = Color.LightGray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "-${com.example.ui.formatCurrencyRingkas(rented.monthlyRent, useShortFormat)}",
                        color = Color(0xFFE57373),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }

        if (totalMonthlyDebtObligation > 0) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Beban Keuangan/Investor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingDown,
                        contentDescription = null,
                        tint = Color(0xFFE57373),
                        modifier = Modifier.size(24.dp).padding(end = 8.dp)
                    )
                    Text(
                        text = "Beban Cicilan Private Equity",
                        color = Color.LightGray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "-${com.example.ui.formatCurrencyRingkas(totalMonthlyDebtObligation, useShortFormat)}",
                        color = Color(0xFFE57373),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 0.5.dp)
            }
        }
    }
}

// ==========================================
// 4. ITEMS SCREEN (Aset & Koleksi)
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemsScreen() {
    // Premium Dark Mode Colors
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val slateDark = Color(0xFF1A1E24)
    val accentGold = Color(0xFFFFD700)
    val neonGreen = Color(0xFF39FF14)
    val dividerColor = Color(0xFF333333)

    // Data for collections
    val collectionsData = listOf(
        Triple("Retro Cars", Icons.Default.DirectionsCar, Color(0xFFE53935)),
        Triple("Jewels", Icons.Default.Diamond, Color(0xFF00BCD4)),
        Triple("Fine Art", Icons.Default.Palette, Color(0xFFFF9800)),
        Triple("NFTs", Icons.Default.Token, Color(0xFF9C27B0)),
        Triple("Private Islands", Icons.Default.Landscape, Color(0xFF4CAF50)),
        Triple("Sports Franchises", Icons.Default.SportsBasketball, Color(0xFFFF5722)),
        Triple("Space Rockets", Icons.Default.RocketLaunch, Color(0xFF607D8B)),
        Triple("Historical Artifacts", Icons.Default.Museum, Color(0xFF795548))
    )

    Scaffold(
        containerColor = bgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. HEADER
            item {
                Text(
                    text = "Items",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // 2. FACILITIES ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val facilities = listOf(
                        "Garage" to Icons.Default.Build,
                        "Hangar" to Icons.Default.Flight,
                        "Harbor" to Icons.Default.DirectionsBoat
                    )
                    facilities.forEach { (name, icon) ->
                        Surface(
                            color = cardDark,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(100.dp).padding(horizontal = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(icon, contentDescription = name, tint = Color.White, modifier = Modifier.size(32.dp))
                                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 3. SHOPS ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val shops = listOf(
                        "Car Showroom" to Icons.Default.Storefront,
                        "Aircraft Shop" to Icons.Default.Store,
                        "Yacht Shop" to Icons.Default.Sailing
                    )
                    shops.forEach { (name, icon) ->
                        Surface(
                            color = bgDark,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, accentGold),
                            modifier = Modifier.size(100.dp).padding(horizontal = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Box(
                                    modifier = Modifier.weight(1f).fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(icon, contentDescription = name, tint = accentGold, modifier = Modifier.size(28.dp))
                                }
                                Surface(
                                    color = Color(0xFFB8860B), // Dark Gold
                                    modifier = Modifier.fillMaxWidth().height(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. DIVIDER
            item {
                HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            }

            // 5. RESIDENCE (Prime Asset Card)
            item {
                Surface(
                    color = slateDark,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    ) {
                        Surface(
                            color = Color(0xFF8B6508), // Dark bronze/gold
                            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                            modifier = Modifier.width(120.dp).fillMaxHeight()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Home, contentDescription = "Residence", tint = Color.White, modifier = Modifier.size(48.dp))
                            }
                        }
                        
                        Column(
                            modifier = Modifier.weight(1f).padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Residence", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Row {
                                repeat(3) {
                                    Icon(Icons.Default.Star, contentDescription = "Tier", tint = accentGold, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text("$ 664.1 M", color = neonGreen, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                        }
                    }
                }
            }

            // 6. DIVIDER
            item {
                HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            }

            // 7. COLLECTIONS
            item {
                Text("Collections", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            items(collectionsData.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { (name, icon, iconColor) ->
                        Surface(
                            color = cardDark,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).aspectRatio(1f) // Square somewhat
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(icon, contentDescription = name, tint = iconColor, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("0 of 20", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. PROFILE SCREEN (Ringkasan Pemain)
// ==========================================
fun parseSvgPath(pathStr: String): Path {
    val path = Path()
    try {
        val tokens = pathStr.trim().split(Regex("[\\s,]+"))
        var idx = 0
        while (idx < tokens.size) {
            val token = tokens[idx].uppercase()
            if (token.isEmpty()) {
                idx++
                continue
            }
            when (token) {
                "M" -> {
                    if (idx + 2 < tokens.size) {
                        val x = tokens[idx + 1].toFloatOrNull() ?: 0f
                        val y = tokens[idx + 2].toFloatOrNull() ?: 0f
                        path.moveTo(x, y)
                        idx += 3
                    } else {
                        idx++
                    }
                }
                "L" -> {
                    if (idx + 2 < tokens.size) {
                        val x = tokens[idx + 1].toFloatOrNull() ?: 0f
                        val y = tokens[idx + 2].toFloatOrNull() ?: 0f
                        path.lineTo(x, y)
                        idx += 3
                    } else {
                        idx++
                    }
                }
                "C" -> {
                    if (idx + 6 < tokens.size) {
                        val x1 = tokens[idx + 1].toFloatOrNull() ?: 0f
                        val y1 = tokens[idx + 2].toFloatOrNull() ?: 0f
                        val x2 = tokens[idx + 3].toFloatOrNull() ?: 0f
                        val y2 = tokens[idx + 4].toFloatOrNull() ?: 0f
                        val x3 = tokens[idx + 5].toFloatOrNull() ?: 0f
                        val y3 = tokens[idx + 6].toFloatOrNull() ?: 0f
                        path.cubicTo(x1, y1, x2, y2, x3, y3)
                        idx += 7
                    } else {
                        idx++
                    }
                }
                "Z" -> {
                    path.close()
                    idx++
                }
                else -> {
                    idx++
                }
            }
        }
    } catch (e: Exception) {
        path.reset()
        path.moveTo(50f, 10f)
        path.lineTo(90f, 95f)
        path.lineTo(10f, 95f)
        path.close()
    }
    return path
}

@Composable
fun SvgLogoPreview(
    svgPathStr: String,
    tintColorHex: String,
    modifier: Modifier = Modifier
) {
    val logoColor = remember(tintColorHex) {
        try {
            Color(android.graphics.Color.parseColor(tintColorHex))
        } catch (e: Exception) {
            Color(0xFFFFD700)
        }
    }
    
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val scaleX = size.width / 100f
        val scaleY = size.height / 100f
        
        val composePath = parseSvgPath(svgPathStr)
        
        drawContext.canvas.save()
        drawContext.canvas.scale(scaleX, scaleY)
        
        drawPath(
            path = composePath,
            color = logoColor,
            style = androidx.compose.ui.graphics.drawscope.Fill
        )
        
        drawPath(
            path = composePath,
            color = Color.White.copy(alpha = 0.3f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
        
        drawContext.canvas.restore()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(navController: NavHostController, viewModel: GameViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val contentResolver = context.contentResolver
    
    var showBackupMessage by remember { mutableStateOf<String?>(null) }
    
    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                contentResolver.openOutputStream(uri)?.use { out ->
                    val jsonStr = viewModel.exportSaveGame()
                    out.write(jsonStr.toByteArray())
                }
                showBackupMessage = "Save Game berhasil diexport!"
            } catch (e: Exception) {
                e.printStackTrace()
                showBackupMessage = "Gagal export: ${e.message}"
            }
        }
    }

    val importLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                val jsonStr = contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader().use { it.readText() }
                }
                if (jsonStr != null) {
                    val success = viewModel.importSaveGame(jsonStr)
                    if (success) {
                        showBackupMessage = "Save Game berhasil diimport!"
                    } else {
                        showBackupMessage = "File Save Corrupt atau Tidak Valid"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showBackupMessage = "Gagal import: ${e.message}"
            }
        }
    }

    val player by viewModel.playerState.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showCapitalInjectionDialog by remember { mutableStateOf(false) }
    
    // Theme Colors
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1A1E24)
    val gold = Color(0xFFFFD700)
    val darkGold = Color(0xFFB8860B)
    val neonGreen = Color(0xFF00FF00)
    val textGray = Color(0xFFA0A0A0)
    val dividerColor = Color(0xFF333333)
 
    val companyLogoSvgPath by viewModel.companyLogoSvgPath.collectAsState()
    val companyLogoFillColorHex by viewModel.companyLogoFillColorHex.collectAsState()
    val difficulty by viewModel.gameDifficulty.collectAsState()
    val stockList by viewModel.stockList.collectAsState()
    val cryptoList by viewModel.cryptoList.collectAsState()
    val realEstateMarket by viewModel.realEstateMarket.collectAsState()

    val stocksValue = player.ownedStocks.sumOf { owned ->
        val liveStock = stockList.find { it.ticker == owned.ticker }
        val livePrice = liveStock?.currentPrice ?: owned.averagePrice
        (owned.shares * livePrice).toLong()
    }
    
    val cryptoValue = player.ownedCrypto.sumOf { owned ->
        val livePrice = cryptoList.find { it.symbol == owned.symbol }?.currentPrice ?: owned.averagePrice
        (owned.amount * livePrice).toLong()
    }
    
    val realEstateValue = player.ownedProperties.sumOf { owned ->
        val prop = realEstateMarket.find { it.id == owned.propertyId }
        prop?.basePrice ?: owned.purchasedPrice
    }

    val collectionList by viewModel.collectionList.collectAsState()
    val collectionsValue = player.ownedCollections.filter { owned -> 
        val cat = collectionList.find { c -> c.id == owned.itemId }?.categoryId
        val isVehicle = listOf("cars", "motorcycles", "yachts", "airplanes").contains(cat)
        cat != null && !isVehicle 
    }.sumOf { it.purchasedPrice }
    
    val vehiclesValue = player.ownedCollections.filter { owned -> 
        val cat = collectionList.find { c -> c.id == owned.itemId }?.categoryId
        listOf("cars", "motorcycles", "yachts", "airplanes").contains(cat) 
    }.sumOf { it.purchasedPrice }

    val allMetals by viewModel.preciousMetalsList.collectAsState()
    val metalsValue = player.ownedMetals.entries.sumOf { (id, amount) ->
        val livePrice = allMetals.find { it.id == id }?.currentPrice ?: 0.0
        (amount * livePrice).toLong()
    } + player.timeDeposits.sumOf { it.principal }
    
    val housingValue = player.ownedHouses.sumOf { it.purchasedPrice }
    
    val totalWealth = player.netAssetValue(stockList, cryptoList, realEstateMarket, collectionList, allMetals)
    val isFamilyOfficeUnlocked = totalWealth >= 100_000L
    var showLockedFoDialog by remember { mutableStateOf(false) }

    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()

    if (showLockedFoDialog) {
        AlertDialog(
            onDismissRequest = { showLockedFoDialog = false },
            title = {
                Text(stringResource(R.string.profile_fo_locked_title), fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
            },
            text = {
                Column {
                    Text(
                        stringResource(R.string.profile_fo_locked_dialog_text),
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.profile_fo_current_wealth_format, com.example.ui.formatCurrencyRingkas(totalWealth, useShortFormat)),
                        fontSize = 12.sp,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLockedFoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                ) {
                    Text(stringResource(R.string.btn_understand), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    Scaffold(
        containerColor = bgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.profile_title),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = cardDark,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = gold)
                        }
                    }
                }
            }

            // 2. FORTUNE & PORTFOLIO DISTRIBUTION
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    com.example.ui.profile.ProfileFortuneHeader(
                        totalWealth = totalWealth,
                        playerState = player,
                        useShortFormat = useShortFormat
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Dynamic progress bar representing diversification
                    val total = totalWealth.coerceAtLeast(1)
                    val categories = listOf(
                        Pair(player.cash, Color(0xFF00E676)),
                        Pair(player.privateBalance, Color(0xFF2196F3)),
                        Pair(player.playerBusinessValuation, Color(0xFFF44336)),
                        Pair(stocksValue, Color(0xFFFF9800)),
                        Pair(realEstateValue, Color(0xFF9C27B0)),
                        Pair(housingValue, Color(0xFFE91E63)),
                        Pair(cryptoValue, Color(0xFF3F51B5)),
                        Pair(collectionsValue, Color(0xFF00BCD4)),
                        Pair(vehiclesValue, Color(0xFF4CAF50)),
                        Pair(metalsValue, Color(0xFFFFC107))
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        categories.forEach { (value, color) ->
                            if (value > 0L) {
                                val weight = value.toFloat() / total
                                if (weight > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .weight(weight)
                                            .fillMaxHeight()
                                            .background(color)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (isFamilyOfficeUnlocked) {
                                navController.navigate("family_office")
                            } else {
                                showLockedFoDialog = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFamilyOfficeUnlocked) Color(0xFF1B2A4A) else Color(0xFF1C222E),
                            contentColor = if (isFamilyOfficeUnlocked) Color(0xFFFFD700) else Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isFamilyOfficeUnlocked) Color(0xFFFFD700).copy(alpha = 0.5f) else Color(0xFFFFC107).copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isFamilyOfficeUnlocked) stringResource(R.string.profile_fo_unlocked_btn) else stringResource(R.string.profile_fo_locked_btn),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFamilyOfficeUnlocked) Color(0xFFFFD700) else Color(0xFFFFC107)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isFamilyOfficeUnlocked)
                                    stringResource(R.string.profile_fo_unlocked_desc)
                                else
                                    stringResource(R.string.profile_fo_locked_desc, com.example.ui.formatCurrencyRingkas(totalWealth, useShortFormat)),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (player.privateBalance > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        com.example.ui.profile.CapitalInjectionQuickPill(
                            privateBalance = player.privateBalance,
                            onClick = { showCapitalInjectionDialog = true }
                        )
                    }
                }
            }

            // 3. WEALTH BREAKDOWN (Grid)
            item {
                val wealthItems = listOf(
                    Triple(stringResource(R.string.asset_cash), com.example.ui.formatCurrencyRingkas(player.cash, useShortFormat), Color(0xFF00E676)),
                    Triple(stringResource(R.string.asset_private_balance), if (isFamilyOfficeUnlocked) com.example.ui.formatCurrencyRingkas(player.privateBalance, useShortFormat) else stringResource(R.string.asset_locked_nav), Color(0xFF2196F3)),
                    Triple(stringResource(R.string.asset_businesses), com.example.ui.formatCurrencyRingkas(player.playerBusinessValuation, useShortFormat), Color(0xFFF44336)),
                    Triple(stringResource(R.string.asset_stocks), com.example.ui.formatCurrencyRingkas(stocksValue, useShortFormat), Color(0xFFFF9800)),
                    Triple(stringResource(R.string.asset_real_estate), com.example.ui.formatCurrencyRingkas(realEstateValue, useShortFormat), Color(0xFF9C27B0)),
                    Triple(stringResource(R.string.asset_housing), com.example.ui.formatCurrencyRingkas(housingValue, useShortFormat), Color(0xFFE91E63)),
                    Triple(stringResource(R.string.asset_crypto), com.example.ui.formatCurrencyRingkas(cryptoValue, useShortFormat), Color(0xFF3F51B5)),
                    Triple(stringResource(R.string.asset_collections), com.example.ui.formatCurrencyRingkas(collectionsValue, useShortFormat), Color(0xFF00BCD4)),
                    Triple(stringResource(R.string.asset_vehicles), com.example.ui.formatCurrencyRingkas(vehiclesValue, useShortFormat), Color(0xFF4CAF50)),
                    Triple(stringResource(R.string.asset_banks), com.example.ui.formatCurrencyRingkas(metalsValue, useShortFormat), Color(0xFFFFC107))
                )
                
                // Chunk into rows of 2
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    wealthItems.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { (label, value, barColor) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))), 
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(32.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(barColor)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(label, color = textGray, fontSize = 12.sp)
                                            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 3b. PRIVATE EQUITY & INVESTORS
            item {
                Button(
                    onClick = { navController.navigate("private_equity") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.05f), 
                        contentColor = Color(0xFF00E5FF) 
                    ),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = "Private Equity", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Private Equity & Investors", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = textGray)
                    }
                }
            }

            // 4. TAX HAVENS & LEGAL
            item {
                Button(
                    onClick = { navController.navigate("tax_legal") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.05f), 
                        contentColor = Color(0xFFFFD700) 
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = "Tax", modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Tax & Legal Department", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = textGray)
                    }
                }
            }

            // 5. GLOBAL TYCOON INDEX
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("global_tycoon_index") }
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))), 
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Global Tycoon Index", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            val tycoons by viewModel.tycoonList.collectAsState()
                            val playerRank = tycoons.indexOfFirst { it.isPlayer } + 1
                            
                            Text(if (playerRank > 0) "#$playerRank - Your Rating" else "Unranked", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Trophy", tint = Color.Black.copy(alpha = 0.8f), modifier = Modifier.size(48.dp))
                    }
                }
            }

            // 6. MASTER STATISTICS & EARNINGS BOARD
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))), 
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        // Section A: Statistics
                        Text("Statistics", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val collectionList by viewModel.collectionList.collectAsState()
                        val realEstateList by viewModel.realEstateMarket.collectAsState()
                        
                        val ownedByCategory = player.ownedCollections.mapNotNull { owned -> 
                            collectionList.find { it.id == owned.itemId }?.categoryId
                        }.groupingBy { it }.eachCount()
                        
                        val carsCount = ownedByCategory["cars"] ?: 0
                        val motorcyclesCount = ownedByCategory["motorcycles"] ?: 0
                        val yachtsCount = ownedByCategory["yachts"] ?: 0
                        val airplanesCount = ownedByCategory["airplanes"] ?: 0
                        val islandsCount = ownedByCategory["private_islands"] ?: 0

                        val stats = listOf(
                            "Number of businesses" to "${player.ownedBusinesses.size + player.holdingCompanies.sumOf { it.subsidiaries.size }}",
                            "Real estate" to "${player.ownedProperties.size} of ${realEstateList.size}",
                            "Cars" to "$carsCount",
                            "Motorcycles" to "$motorcyclesCount",
                            "Yachts" to "$yachtsCount",
                            "Airplanes" to "$airplanesCount",
                            "Private Islands" to "$islandsCount",
                            "Jewels & Fine Art" to "${(ownedByCategory["jewels"] ?: 0) + (ownedByCategory["fine_art"] ?: 0)}"
                        )
                        
                        stats.forEach { (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, color = textGray, fontSize = 14.sp)
                                Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = dividerColor)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Section B: Earned
                        Text("Monthly Projected Earnings (Earned)", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val earningsData by viewModel.earningsReport.collectAsState()
                        
                        val earnings = listOf(
                            "In business" to com.example.ui.formatCurrencyRingkas(earningsData.monthlyBusinessIncome, useShortFormat),
                            "On rent" to com.example.ui.formatCurrencyRingkas(earningsData.monthlyRentIncome, useShortFormat),
                            "On dividends" to com.example.ui.formatCurrencyRingkas(earningsData.monthlyDividendIncome, useShortFormat)
                        )
                        
                        earnings.forEach { (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, color = textGray, fontSize = 14.sp)
                                Text(value, color = if(value.contains("-")) Color.Red else neonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // 7. FOOTER
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    
                    Button(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.05f), 
                            contentColor = Color(0xFFFFD700) 
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Help & Settings", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Join our community:", color = textGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icons = listOf(Icons.Default.Chat, Icons.Default.Send, Icons.Default.Tag, Icons.Default.Language)
                        icons.forEach { icon ->
                            Surface(
                                shape = CircleShape,
                                color = cardDark,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        val isNotificationEnabled by viewModel.isNotificationEnabled.collectAsState()
        val isDarkModeSimulated by viewModel.isDarkModeSimulated.collectAsState()
        val soundVolume by viewModel.soundVolume.collectAsState()
        val gameDifficulty by viewModel.gameDifficulty.collectAsState()
        val useShortNumberFormat by viewModel.useShortNumberFormat.collectAsState()
        
        val monthDurationSeconds by viewModel.monthDurationSeconds.collectAsState()
        val stockIntervalSeconds by viewModel.stockIntervalSeconds.collectAsState()
        val marketVolatilityFactor by viewModel.marketVolatilityFactor.collectAsState()
        
        val companyLogoSvgPath by viewModel.companyLogoSvgPath.collectAsState()
        val companyLogoFillColorHex by viewModel.companyLogoFillColorHex.collectAsState()
        
        var selectedTab by remember { mutableStateOf(0) } // 0: General, 1: Gameplay, 2: Sandbox, 3: Assets 
        
        // Modal copy values
        var localSvgPath by remember { mutableStateOf(companyLogoSvgPath) }
        var localColorHex by remember { mutableStateOf(companyLogoFillColorHex) }
        
        var localNotification by remember { mutableStateOf(isNotificationEnabled) }
        var localDarkTheme by remember { mutableStateOf(isDarkModeSimulated) }
        var localVolume by remember { mutableStateOf(soundVolume) }
        var localDifficulty by remember { mutableStateOf(gameDifficulty) }
        var localUseShortFormat by remember { mutableStateOf(useShortNumberFormat) }
        
        var localMonthDuration by remember { mutableStateOf(monthDurationSeconds) }
        var localStockInterval by remember { mutableStateOf(stockIntervalSeconds) }
        var localVolatility by remember { mutableStateOf(marketVolatilityFactor) }
        
        var showResetProgressConfirm by remember { mutableStateOf(false) }

        // Cloud save login inputs
        var emailInput by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }

        // States for Adding Real Estate
        var customPropName by remember { mutableStateOf("") }
        var customPropLocation by remember { mutableStateOf("") }
        var customPropPrice by remember { mutableStateOf("") }
        var customPropRent by remember { mutableStateOf("") }


        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showSettingsDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(24.dp)),
                color = Color(0xFF15181F),
                border = BorderStroke(1.dp, Color(0xFF2C3240))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Area
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F2430))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = gold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(stringResource(R.string.settings_title), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showSettingsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    
                    // Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF1A1F2B),
                        contentColor = gold,
                        edgePadding = 0.dp
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text(stringResource(R.string.settings_tab_general), fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            selectedContentColor = gold,
                            unselectedContentColor = textGray
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text(stringResource(R.string.settings_tab_gameplay), fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            selectedContentColor = gold,
                            unselectedContentColor = textGray
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text(stringResource(R.string.settings_tab_sandbox), fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            selectedContentColor = gold,
                            unselectedContentColor = textGray
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text(stringResource(R.string.settings_tab_assets), fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            selectedContentColor = gold,
                            unselectedContentColor = textGray
                        )
                    }

                    // Content Scrollable
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        when (selectedTab) {
                            0 -> {
                            Text(stringResource(R.string.settings_general_section), color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            // Language Selector Card
                            LanguageSettingsCard()

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            // simulated dark mode
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.settings_dark_theme), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(stringResource(R.string.settings_dark_theme_desc), color = textGray, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = localDarkTheme,
                                    onCheckedChange = { 
                                        localDarkTheme = it
                                        viewModel.updateGeneralSettings(localNotification, localDarkTheme, localVolume, localUseShortFormat)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = gold, checkedTrackColor = gold.copy(alpha = 0.5f))
                                )
                            }

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                                // Number format
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(stringResource(R.string.settings_compact_format), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(stringResource(R.string.settings_compact_format_desc), color = textGray, fontSize = 11.sp)
                                    }
                                    Switch(
                                        checked = localUseShortFormat,
                                        onCheckedChange = { 
                                            localUseShortFormat = it
                                            viewModel.updateGeneralSettings(localNotification, localDarkTheme, localVolume, localUseShortFormat)
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = gold, checkedTrackColor = gold.copy(alpha = 0.5f))
                                    )
                                }

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            // warning alerts
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.settings_warnings), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(stringResource(R.string.settings_warnings_desc), color = textGray, fontSize = 11.sp)
                                }
                                Switch(
                                    checked = localNotification,
                                    onCheckedChange = { 
                                        localNotification = it
                                        viewModel.updateGeneralSettings(localNotification, localDarkTheme, localVolume, localUseShortFormat)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = gold, checkedTrackColor = gold.copy(alpha = 0.5f))
                                )
                            }

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            // SFX volume slider
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("System Sound & SFX Volume", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${(localVolume * 100).toInt()}%", color = gold, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                                Slider(
                                    value = localVolume,
                                    onValueChange = { 
                                        localVolume = it
                                        viewModel.updateGeneralSettings(localNotification, localDarkTheme, localVolume, localUseShortFormat)
                                    },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(thumbColor = gold, activeTrackColor = gold)
                                )
                            }

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            // DATA BACKUP & RECOVERY
                            Text("DATA BACKUP & RECOVERY", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            // CLOUD BACKUP & SYNC (Firebase Firestore & Auth)
                            val isCloudSyncInProgress by viewModel.cloudSyncProgress.collectAsState()
                            val cloudSyncMessage by viewModel.cloudSyncMessage.collectAsState()
                            val lastSyncTimeMs by viewModel.lastSyncTimeMs.collectAsState()
                            val isUserLoggedIn = viewModel.isUserLoggedIn()
                            val userEmail = viewModel.getCurrentUserEmail() ?: ""

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E2330).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "☁️ Cloud Backup & Sync",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        if (isCloudSyncInProgress) {
                                            androidx.compose.material3.CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = gold,
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    }

                                    // Status Akun
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (isUserLoggedIn) neonGreen else Color.Red)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isUserLoggedIn) "Terhubung: $userEmail" else "Status: Belum Terhubung ke Cloud",
                                            color = if (isUserLoggedIn) Color.White else textGray,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    // Sync messages / last updated
                                    cloudSyncMessage?.let { msg ->
                                        Text(
                                            text = msg,
                                            color = if (msg.contains("berhasil") || msg.contains("terjalin")) neonGreen else Color(0xFFFFA726),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    if (lastSyncTimeMs > 0L) {
                                        val timeAgo = run {
                                            val diff = System.currentTimeMillis() - lastSyncTimeMs
                                            if (diff < 60_000) "Baru saja"
                                            else if (diff < 3600_000) "${diff / 60000} menit yang lalu"
                                            else if (diff < 86400_000) "${diff / 3600000} jam yang lalu"
                                            else "${diff / 86400000} hari yang lalu"
                                        }
                                        Text(
                                            text = "Terakhir disinkronkan: $timeAgo",
                                            color = textGray,
                                            fontSize = 11.sp
                                        )
                                    } else {
                                        if (isUserLoggedIn) {
                                            Text(
                                                text = "Terakhir disinkronkan: Belum pernah",
                                                color = textGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    if (!isUserLoggedIn) {
                                        // Email & Password Fields
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = emailInput,
                                                onValueChange = { emailInput = it },
                                                label = { Text("Email", color = textGray, fontSize = 12.sp) },
                                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedBorderColor = gold,
                                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                                    focusedLabelColor = gold
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )

                                            OutlinedTextField(
                                                value = passwordInput,
                                                onValueChange = { passwordInput = it },
                                                label = { Text("Password", color = textGray, fontSize = 12.sp) },
                                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedBorderColor = gold,
                                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                                    focusedLabelColor = gold
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true,
                                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                                            )
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    viewModel.signInWithEmail(
                                                        emailInput,
                                                        passwordInput,
                                                        onSuccess = {
                                                            emailInput = ""
                                                            passwordInput = ""
                                                        },
                                                        onError = {}
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50)),
                                                modifier = Modifier.weight(1f).height(44.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Masuk", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.signUpWithEmail(
                                                        emailInput,
                                                        passwordInput,
                                                        onSuccess = {
                                                            emailInput = ""
                                                            passwordInput = ""
                                                        },
                                                        onError = {}
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2A4A)),
                                                modifier = Modifier.weight(1f).height(44.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, gold.copy(alpha = 0.5f))
                                            ) {
                                                Text("Daftar", color = gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(
                                            text = "⚠️ Silakan masuk/daftar terlebih dahulu untuk menikmati pencadangan cloud otomatis gratis.",
                                            color = Color(0xFFFF6B6B),
                                            fontSize = 10.sp,
                                            lineHeight = 12.sp
                                        )
                                    } else {
                                        // Backup & Restore & Log Out Buttons
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = { viewModel.backupGameToCloud() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F4E79)),
                                                    modifier = Modifier.weight(1f).height(44.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, gold.copy(alpha = 0.3f))
                                                ) {
                                                    Text("☁️ Backup Cloud", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }

                                                Button(
                                                    onClick = { viewModel.restoreGameFromCloud() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF196F3D)),
                                                    modifier = Modifier.weight(1f).height(44.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("📥 Restore Cloud", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Button(
                                                onClick = { viewModel.signOut() },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E333F)),
                                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Keluar Akun", color = textGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("LOCAL BACKUP & OFFLINE PORT", color = textGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                            showBackupMessage?.let { msg ->
                                Text(msg, color = if(msg.contains("berhasil", ignoreCase = true)) neonGreen else Color.Red, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { exportLauncher.launch("BoxOfficeTycoon_Backup.json") },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Export Save", color = gold, fontSize = 12.sp)
                                }
                                
                                Button(
                                    onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Import Save", color = gold, fontSize = 12.sp)
                                }
                            }

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            // Reset Progression controls
                            Text("DANGER ZONE", color = Color(0xFFFF3B30), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            
                            Button(
                                onClick = { showResetProgressConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reset Seluruh Progress Game", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            } // end of tab 0
                            
                            1 -> {
                            // Game preset scenario simulation modes
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("SKENARIO GAMEPLAY", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                
                                val difficulties = listOf(
                                    "Easy" to "Wirausaha Pemula (Peluang modal besar, krisis 0%)",
                                    "Normal" to "Konglomerat (Standard multiplier)",
                                    "Hard" to "Krisis Finansial (-15% profit bisnis secara berkala)",
                                    "Elite Tycoon" to "Krisis Ekonomi Global (Uang disusut inflasi 2.5% per tahun)"
                                )
                                
                                difficulties.forEach { (diff, desc) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                localDifficulty = diff
                                                viewModel.updateGameDifficultySettings(localDifficulty, localVolatility)
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = localDifficulty == diff,
                                            onClick = { 
                                                localDifficulty = diff
                                                viewModel.updateGameDifficultySettings(localDifficulty, localVolatility)
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = gold, unselectedColor = textGray)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(diff, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(desc, color = textGray, fontSize = 11.sp)
                                        }
                                    }
                                }
                            } // end of SKENARIO GAMEPLAY

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            Text("EXPERIMENT: LOOP DURATION CYCLES", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            // Slider: Month game cycle speed rate
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Kecepatan Durasi 1 Bulan Game", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    val hrs = (localMonthDuration / 3600f).toInt()
                                    val mins = ((localMonthDuration % 3600f) / 60f).toInt()
                                    val secs = (localMonthDuration % 60f).toInt()
                                    val timeStr = buildString {
                                        if (hrs > 0) append("$hrs Jam ")
                                        if (mins > 0) append("$mins Menit ")
                                        if (secs > 0 || (hrs == 0 && mins == 0)) append("$secs Detik")
                                    }.trim()
                                    Text(timeStr, color = gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Slider(
                                    value = localMonthDuration,
                                    onValueChange = { 
                                        localMonthDuration = it
                                        viewModel.updateMonthDuration(localMonthDuration)
                                    },
                                    valueRange = 10f..7200f,
                                    colors = SliderDefaults.colors(thumbColor = gold, activeTrackColor = gold)
                                )
                                Text(
                                    text = "Mengatur seberapa lama (dalam detik nyata) satu siklus bulan pendapatan bisnis Anda berganti.",
                                    color = textGray,
                                    fontSize = 11.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))

                            // Slider: Stock fluctuating frequency
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Rentang Fluktuasi Pasar Saham", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${String.format(java.util.Locale.US, "%.1f", localStockInterval)} Detik", color = gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Slider(
                                    value = localStockInterval,
                                    onValueChange = { 
                                        localStockInterval = it
                                        viewModel.updateStockInterval(localStockInterval)
                                    },
                                    valueRange = 1.0f..15.0f,
                                    colors = SliderDefaults.colors(thumbColor = gold, activeTrackColor = gold)
                                )
                                Text(
                                    text = "Kecepatan waktu delay refresh pergeseran harga saham naik/turun di tab bursa investasi.",
                                    color = textGray,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Slider: Volatility
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Intensitas Volatilitas Pasar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${String.format(java.util.Locale.US, "%.1f", localVolatility)}x", color = gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Slider(
                                    value = localVolatility,
                                    onValueChange = { 
                                        localVolatility = it
                                        viewModel.updateGameDifficultySettings(localDifficulty, localVolatility)
                                    },
                                    valueRange = 0.1f..5.0f,
                                    colors = SliderDefaults.colors(thumbColor = gold, activeTrackColor = gold)
                                )
                                Text(
                                    text = "Memperkuat efek naik turun grafik/harga saham. Nilai tinggi membuat bursa kian liar (Sangat Spekulatif!).",
                                    color = textGray,
                                    fontSize = 11.sp
                                )
                            }
                            
                            } // end of tab 1
                            
                            2 -> {
                            // ==========================================
                            Text("EXPERIMENT: CORPORATE BRAND LOGO (CUSTOM SVG)", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F1218), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = Color(0xFF1D222E),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                                        SvgLogoPreview(
                                            svgPathStr = localSvgPath,
                                            tintColorHex = localColorHex,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Logo ID Preview (Canvas)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Diskalakan dinamis range (0,0) s.d (100,100)", color = textGray, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Logo Color Tint: $localColorHex", color = gold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            
                            // Presets grid (2 Columns / row structure)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Pilih Preset Desain Bawaan:", color = textGray, fontSize = 12.sp)
                                val presets = listOf(
                                    "Crown" to "M 10 90 L 10 30 L 35 60 L 50 20 L 65 60 L 90 30 L 90 90 Z",
                                    "Star" to "M 50 5 L 63 33 L 94 38 L 71 61 L 76 92 L 50 78 L 24 92 L 29 61 L 6 38 L 37 33 Z",
                                    "Diamond" to "M 50 5 L 85 40 L 50 95 L 15 40 Z",
                                    "Shield" to "M 50 5 L 90 20 L 90 60 C 90 85 50 98 50 98 C 50 98 10 85 10 60 L 10 20 Z",
                                    "Rocket" to "M 50 5 C 65 25 70 50 70 80 Q 60 75 50 95 Q 40 75 30 80 C 30 50 35 25 50 5 Z"
                                )
                                
                                presets.chunked(3).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowItems.forEach { (name, path) ->
                                            Surface(
                                                color = if (localSvgPath.trim() == path.trim()) gold else Color(0xFF222834),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { 
                                                        localSvgPath = path
                                                        viewModel.updateCompanyLogo(localSvgPath, localColorHex)
                                                    }
                                            ) {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                                                    Text(
                                                        text = name,
                                                        color = if (localSvgPath.trim() == path.trim()) Color.Black else Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        if (rowItems.size < 3) {
                                            repeat(3 - rowItems.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Custom Path Text Entry
                            OutlinedTextField(
                                value = localSvgPath,
                                onValueChange = { 
                                    localSvgPath = it
                                    viewModel.updateCompanyLogo(localSvgPath, localColorHex)
                                },
                                label = { Text("Custom SVG Path Data", color = gold) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = gold,
                                    unfocusedBorderColor = Color(0xFF2C3240)
                                )
                            )
                            
                            // Tint Color Code Entry
                            OutlinedTextField(
                                value = localColorHex,
                                onValueChange = { 
                                    localColorHex = it
                                    viewModel.updateCompanyLogo(localSvgPath, localColorHex)
                                },
                                label = { Text("Warna Logo (Hex Code, misal: #FFD700)", color = gold) },
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = gold,
                                    unfocusedBorderColor = Color(0xFF2C3240)
                                )
                            )

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            // Cheat: Set Balance
                            var customBalanceInput by remember { mutableStateOf("") }
                            Text("PENGATURAN SALDO / MODAL AWAL", color = neonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = customBalanceInput,
                                    onValueChange = { customBalanceInput = it.filter { char -> char.isDigit() } },
                                    label = { Text("Modal ($)", color = textGray) },
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = neonGreen, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    singleLine = true
                                )
                                Button(
                                    onClick = { 
                                        val amt = customBalanceInput.toLongOrNull()
                                        if (amt != null) {
                                            viewModel.setPlayerCash(amt)
                                            customBalanceInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen, contentColor = Color.Black)
                                ) {
                                    Text("Set Saldo", fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f))

                            // Custom Asset entry (Dropdown form)
                            var customAssetType by remember { mutableStateOf("Real Estate") }
                            var customAssetExpanded by remember { mutableStateOf(false) }
                            var customImageUrl by remember { mutableStateOf("") }
                            var customReleaseYear by remember { mutableStateOf("") }
                            
                            val assetTypes = listOf("Real Estate") + com.example.data.collectionCategories.map { it.name } + com.example.data.vehicleCategories.map { it.name }
                            
                            LaunchedEffect(selectedTab) {
                                if (assetTypes.isNotEmpty() && !assetTypes.contains(customAssetType)) {
                                    customAssetType = assetTypes.first()
                                }
                            }
                            
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("TAMBAH ASET KUSTOM", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { customAssetExpanded = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                        border = BorderStroke(1.dp, textGray)
                                    ) {
                                        Text("Tipe: $customAssetType")
                                    }
                                    DropdownMenu(
                                        expanded = customAssetExpanded,
                                        onDismissRequest = { customAssetExpanded = false },
                                        modifier = Modifier.background(cardDark)
                                    ) {
                                        assetTypes.forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type, color = Color.White) },
                                                onClick = { customAssetType = type; customAssetExpanded = false }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = customPropName,
                                    onValueChange = { customPropName = it },
                                    label = { Text("Nama Aset", color = textGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = gold, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customPropLocation,
                                    onValueChange = { customPropLocation = it },
                                    label = { Text(if (customAssetType == "Real Estate") "Lokasi" else "Deskripsi Kustom", color = textGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = gold, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customImageUrl,
                                    onValueChange = { customImageUrl = it },
                                    label = { Text("URL Gambar Kustom (opsional)", color = textGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = gold, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customReleaseYear,
                                    onValueChange = { customReleaseYear = it.filter { char -> char.isDigit() } },
                                    label = { Text("Tahun Rilis (Opsional, untuk aset Klasik)", color = textGray) },
                                    placeholder = { Text("Contoh: 1967 atau 2020", color = textGray.copy(alpha=0.5f)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = gold, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customPropPrice,
                                    onValueChange = { customPropPrice = it.filter { char -> char.isDigit() } },
                                    label = { Text("Biaya / Harga Beli ($)", color = textGray) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = gold, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                    singleLine = true
                                )
                                
                                if (customAssetType == "Real Estate") {
                                    OutlinedTextField(
                                        value = customPropRent,
                                        onValueChange = { customPropRent = it.filter { char -> char.isDigit() } },
                                        label = { Text("Sewa Bulanan ($)", color = textGray) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = gold, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                        singleLine = true
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        val price = customPropPrice.toLongOrNull()
                                        if (customAssetType == "Real Estate") {
                                            val rent = customPropRent.toLongOrNull()
                                            if (customPropName.isNotBlank() && customPropLocation.isNotBlank() && price != null && rent != null) {
                                                viewModel.addProperty(customPropName, customPropLocation, price, rent, customImageUrl)
                                                customPropName = ""
                                                customPropLocation = ""
                                                customPropPrice = ""
                                                customPropRent = ""
                                                customImageUrl = ""
                                            }
                                        } else {
                                            if (customPropName.isNotBlank() && customPropLocation.isNotBlank() && price != null) {
                                                val catId = com.example.data.collectionCategories.find { it.name == customAssetType }?.id 
                                                    ?: com.example.data.vehicleCategories.find { it.name == customAssetType }?.id
                                                    ?: "retro_cars"
                                                val releaseYearVal = customReleaseYear.toIntOrNull()
                                                viewModel.addCollectionItem(catId, customPropName, customPropLocation, price, customImageUrl, releaseYearVal)
                                                customPropName = ""
                                                customPropLocation = ""
                                                customPropPrice = ""
                                                customImageUrl = ""
                                                customReleaseYear = ""
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                                ) {
                                    Text("Simpan ke Katalog", fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f), modifier = Modifier.padding(top = 16.dp))

                            // TROUBLESHOOTING & RECOVERY
                            var showRepairDialog by remember { mutableStateOf(false) }
                            var repairSuccessMsg by remember { mutableStateOf(false) }

                            Text("TROUBLESHOOTING & RECOVERY", color = Color(0xFFFF5252), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = { showRepairDialog = true },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252), contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("🛠 Perbaiki Struktur Data (Fix Save Game)", fontWeight = FontWeight.Bold)
                            }
                            
                            if (repairSuccessMsg) {
                                Text("✅ Struktur data berhasil diperbaiki dan disinkronisasi!", color = neonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                LaunchedEffect(Unit) {
                                    kotlinx.coroutines.delay(3000)
                                    repairSuccessMsg = false
                                }
                            }

                            if (showRepairDialog) {
                                AlertDialog(
                                    onDismissRequest = { showRepairDialog = false },
                                    title = { Text("Perbaiki Struktur Data", fontWeight = FontWeight.Bold) },
                                    text = { Text("Gunakan fitur ini HANYA jika Anda mengalami error, force close, atau data tidak sinkron setelah update. Lanjutkan?") },
                                    confirmButton = {
                                        TextButton(onClick = { 
                                            viewModel.repairDataStructure()
                                            showRepairDialog = false
                                            repairSuccessMsg = true
                                        }) { Text("Ya", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold) }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showRepairDialog = false }) { Text("Batal") }
                                    }
                                )
                            }
                            
                            } // end of tab 2
                            3 -> {
                                Text("KELOLA ASET (GAMBAR & HAPUS)", color = gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                
                                val allCollections by viewModel.collectionList.collectAsState()
                                
                                val categories = listOf(
                                    "🏢 Kelola Gambar Properti (Real Estate)",
                                    "🚘 Kelola Gambar Mobil",
                                    "🏍️ Kelola Gambar Motor",
                                    "💎 Kelola Koleksi & Lainnya"
                                )
                                
                                categories.forEach { catName ->
                                    var catExpanded by remember { mutableStateOf(false) }
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D222E)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { catExpanded = !catExpanded },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                Text(catName, color = gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Icon(if (catExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = gold)
                                            }
                                            
                                            if (catExpanded) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                if (catName == "🏢 Kelola Gambar Properti (Real Estate)") {
                                                    Text("Properti saat ini berdiri sebagai aset sistem dan tidak dapat dimodifikasi gambar secara manual.", color = textGray, fontSize = 12.sp)
                                                } else {
                                                    val itemsToShow = when (catName) {
                                                        "🚘 Kelola Gambar Mobil" -> allCollections.filter { item -> com.example.data.vehicleCategories.any { it.id == item.categoryId && it.id.contains("cars") } }
                                                        "🏍️ Kelola Gambar Motor" -> allCollections.filter { item -> com.example.data.vehicleCategories.any { it.id == item.categoryId && it.id.contains("motor") } }
                                                        else -> allCollections.filter { item -> com.example.data.collectionCategories.any { it.id == item.categoryId } }
                                                    }
                                                    
                                                    if (itemsToShow.isEmpty()) {
                                                        Text("Belum ada aset kustom di kategori ini.", color = textGray, fontSize = 12.sp)
                                                    } else {
                                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            itemsToShow.forEach { item ->
                                                                var itemExpanded by remember { mutableStateOf(false) }
                                                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF15181F)), modifier = Modifier.clickable { itemExpanded = !itemExpanded }) {
                                                                    Column(modifier = Modifier.padding(12.dp)) {
                                                                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                                Icon(if (itemExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = textGray, modifier = Modifier.size(16.dp))
                                                                                Spacer(modifier = Modifier.width(8.dp))
                                                                                Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                                            }
                                                                            if (item.id.startsWith("col_custom_") || item.id.startsWith("veh_custom_")) {
                                                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(20.dp).clickable {
                                                                                    viewModel.removeCollectionItem(item.id)
                                                                                })
                                                                            }
                                                                        }
                                                                        if (itemExpanded) {
                                                                            Spacer(modifier = Modifier.height(12.dp))
                                                                            OutlinedTextField(
                                                                                value = item.imageUrl ?: "",
                                                                                onValueChange = { viewModel.updateCollectionImageUrl(item.id, it) },
                                                                                label = { Text("URL Gambar", color = textGray) },
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = gold, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                                                                singleLine = true
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Reset Confirmation dialogues
        if (showResetProgressConfirm) {
            AlertDialog(
                onDismissRequest = { showResetProgressConfirm = false },
                containerColor = Color(0xFF1E222D),
                title = { Text("Hapus Semua Progress?", color = Color.White, fontWeight = FontWeight.Bold) },
                text = { Text("Apakah Anda yakin ingin mengatur ulang profil konglomerat Anda ke kondisi awal awal ($0)? Semua sejarah, portofolio, dan kepemilikan bisnis akan dihapus.", color = textGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetGameProgress()
                            showResetProgressConfirm = false
                            showSettingsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))
                    ) {
                        Text("Reset Permanen", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetProgressConfirm = false }) {
                        Text("Batal", color = Color.White)
                    }
                }
            )
        }
    }

    if (showCapitalInjectionDialog) {
        com.example.privateequity.capitalinjection.CapitalInjectionDialog(
            currentPrivateBalance = player.privateBalance,
            currentCompanyCash = player.cash,
            onDismiss = { showCapitalInjectionDialog = false },
            onConfirm = { amount ->
                viewModel.injectCapitalToCompany(amount)
                showCapitalInjectionDialog = false
            }
        )
    }
}

// ==========================================

@Composable
fun IPLibraryHistoryScreen(navController: NavHostController, viewModel: GameViewModel, instanceId: String) {
    com.example.filmstudio.ui.FilmCatalogScreen(navController, viewModel, instanceId)
}

// ==========================================

@Composable
fun TvIpLibraryScreen(navController: NavHostController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()
    
    val ipList = playerState.ipLibraryHistory.reversed()

    val showRebootDialog = remember { mutableStateOf(false) }
    val selectedIp = remember { mutableStateOf<com.example.data.TvProgram?>(null) }

    if (showRebootDialog.value && selectedIp.value != null) {
        val rebootCost = selectedIp.value!!.productionCost * 2
        AlertDialog(
            onDismissRequest = { showRebootDialog.value = false },
            title = { Text("Reboot Program") },
            text = { Text("Apakah Anda yakin ingin menayangkan ulang '${selectedIp.value!!.title}'? \n\nBiaya Bongkar Pasang Studio: $${com.example.ui.formatCurrencyRingkas(rebootCost.toLong(), useShortFormat)}\n\n(Perhatian: Rating akan di-roll ulang secara acak)") },
            confirmButton = {
                Button(
                    onClick = {
                        if (playerState.cash >= rebootCost.toLong()) {
                            viewModel.rebootTvProgram(selectedIp.value!!.id)
                            showRebootDialog.value = false
                        }
                    },
                    enabled = playerState.cash >= rebootCost.toLong()
                ) {
                    Text(if (playerState.cash >= rebootCost.toLong()) "Reboot" else "Kas Tidak Cukup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebootDialog.value = false }) { Text("Batal") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kembali")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("📺 Gudang IP & Histori Program", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (ipList.isEmpty()) {
            Text("Belum ada aset IP yang dimasukkan ke gudang.\nHentikan (Bungkus) Siaran Internal untuk menjadikannya IP Asset.", color = Color.Gray, modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(ipList) { ip ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(ip.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tipe: ${ip.type} | Lama Tayang: ${ip.monthsAired} Bulan", fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val isProfit = ip.totalAccumulatedProfit >= 0
                            val profitLabel = if (isProfit) "Total Profit: +${com.example.ui.formatCurrencyRingkas(ip.totalAccumulatedProfit.toLong(), useShortFormat)}" else "Total Rugi: ${com.example.ui.formatCurrencyRingkas(ip.totalAccumulatedProfit.toLong(), useShortFormat)}"
                            
                            Text(profitLabel, color = if (isProfit) Color(0xFF00C853) else MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Hak Cipta Dimiliki Sepenuhnya (IP Asset)", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    selectedIp.value = ip
                                    showRebootDialog.value = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Tayangkan Ulang (Reboot)")
                            }

                            val tvSellPrice = ((ip.productionCost * 0.5) + maxOf(0.0, ip.totalAccumulatedProfit * 0.8)).toLong()
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.sellTvIp(ip.id, tvSellPrice)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Jual Hak Cipta (Sell IP) : +${com.example.ui.formatCurrencyRingkas(tvSellPrice, useShortFormat)}", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
