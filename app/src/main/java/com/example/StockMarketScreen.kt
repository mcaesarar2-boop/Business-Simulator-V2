package com.example

import com.example.viewmodel.GameViewModel

import com.example.data.*
import com.example.ui.formatMarketCap
import com.caesar.gametycoon.stock.takeover.TakeoverInitiationCard
import com.caesar.gametycoon.stock.takeover.TakeoverBiddingDialog
import com.caesar.gametycoon.stock.takeover.MegaHoldingRequirementDialog

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.foundation.lazy.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import java.text.NumberFormat
import java.util.Locale

// 6. GLOBAL STOCK MARKET SCREEN
// ==========================================
@Composable
fun StockLineChart(
    prices: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF10B981)
) {
    Canvas(modifier = modifier) {
        if (prices.isEmpty()) return@Canvas
        
        val maxPrice = prices.maxOrNull() ?: 1.0
        val minPrice = prices.minOrNull() ?: 0.0
        val priceRange = if (maxPrice - minPrice == 0.0) 1.0 else maxPrice - minPrice
        
        val width = size.width
        val height = size.height
        
        val points = prices.mapIndexed { index, price ->
            val x = index.toDouble() / (prices.size - 1) * width
            val y = height - ((price - minPrice) / priceRange * height * 0.82 + height * 0.09)
            Offset(x.toFloat(), y.toFloat())
        }
        
        // Grid lines
        val gridCount = 3
        for (i in 0..gridCount) {
            val y = height * 0.09f + (height * 0.82f) / gridCount * i
            drawLine(
                color = Color.White.copy(alpha = 0.07f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Draw path with smooth curve
        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val pPrev = points[i - 1]
                    val pCurr = points[i]
                    val controlX = (pPrev.x + pCurr.x) / 2
                    cubicTo(controlX, pPrev.y, controlX, pCurr.y, pCurr.x, pCurr.y)
                }
            }
        }
        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Filled shadow gradient area
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.18f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )
    }
}

data class MarketNews(
    val id: String,
    val text: String,
    val type: String, // "BULL", "BEAR", "NEUTRAL"
    val timestamp: String = "Now"
)

@Composable
fun GlobalStockMarketScreen(navController: NavHostController, viewModel: GameViewModel, initialTicker: String? = null) {
    // Theme Colors
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val dividerColor = Color(0xFF333333)
    val logoBg = Color(0xFF2A2A2A)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val neonGreen = Color(0xFF00FF00)
    val red = Color(0xFFFF3B30)

    val stockInterval by viewModel.stockIntervalSeconds.collectAsState()
    val marketVolatilityFactor by viewModel.marketVolatilityFactor.collectAsState()

    val filters = listOf("All", "Indonesia", "Global/US", "Top Gainers", "Top Losers", "US Tech", "IDX Bluechips", "Dividends", "Highest Dividend", "Lowest Dividend", "Highest Market Cap", "Lowest Market Cap")
    var activeFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    // Simulation State
    val rawStockListState by viewModel.stockList.collectAsState()
    val newsFeed by viewModel.newsFeed.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    
    val stockListState = rawStockListState.map { stock ->
        val newName = playerState.rebrandedCompanies[stock.ticker]
        if (newName != null) stock.copy(name = newName) else stock
    }
    
    val usdBalance = playerState.cash.toDouble()
    
    // Interactive Detail Selection States
    var selectedStockTicker by remember { mutableStateOf<String?>(initialTicker) }
    var chartInterval by remember { mutableStateOf("1D") }
    
    // Virtual Portfolio is now ownedStocks
    val myHoldings = playerState.ownedStocks.associate { it.ticker to it.shares }
    
    // Transaction Modals State
    var showBuyDialog by remember { mutableStateOf(false) }
    var buySharesAmount by remember { mutableStateOf("") }
    var buySuccessMessage by remember { mutableStateOf<String?>(null) }
    var showNewsHistoryDialog by remember { mutableStateOf(false) }
    
    // Hostile Takeover States
    var showRebrandDialog by remember { mutableStateOf(false) }
    var rebrandNameInput by remember { mutableStateOf("") }
    var showTakeoverDialog by remember { mutableStateOf(false) }
    var showMegaHoldingReqDialog by remember { mutableStateOf(false) }

    if (showNewsHistoryDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showNewsHistoryDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardDark,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Log Berita & Sentimen Pasar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        IconButton(onClick = { showNewsHistoryDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(newsFeed) { news ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                val indicatorColor = when (news.type) {
                                    "BULL" -> neonGreen
                                    "BEAR" -> red
                                    else -> textGray
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(indicatorColor, CircleShape)
                                        .align(Alignment.CenterVertically)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(news.text, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = bgDark,
        topBar = {
            Surface(color = bgDark, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp).statusBarsPadding()
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.clickable { navController.navigateUp() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Global Stock Market", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            
            // Interactive Market News Alert Panel
            val latestNews = newsFeed.firstOrNull()
            if (latestNews != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val alertBg = when (latestNews.type) {
                            "BULL" -> neonGreen.copy(alpha = 0.15f)
                            "BEAR" -> red.copy(alpha = 0.15f)
                            else -> gold.copy(alpha = 0.15f)
                        }
                        val alertColor = when (latestNews.type) {
                            "BULL" -> neonGreen
                            "BEAR" -> red
                            else -> gold
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = alertBg,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "LIVE ALERT",
                                color = alertColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        
                        Text(
                            text = latestNews.text,
                            color = Color.White,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = { showNewsHistoryDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Log Berita",
                                tint = gold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name, ticker, or sector...", color = textGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = textGray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = textGray)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = gold,
                    unfocusedBorderColor = dividerColor,
                    focusedContainerColor = cardDark,
                    unfocusedContainerColor = cardDark
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = activeFilter == filter
                    Surface(
                        color = if (isSelected) bgDark else cardDark,
                        shape = RoundedCornerShape(20.dp),
                        border = if (isSelected) BorderStroke(1.dp, gold) else null,
                        modifier = Modifier.clickable { activeFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) gold else textGray,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            // Stock List
            val filteredStocks = stockListState.filter { stock ->
                val matchesFilter = when(activeFilter) {
                    "Indonesia" -> stock.ticker.contains(".JK")
                    "Global/US" -> !stock.ticker.contains(".JK")
                    "Top Gainers" -> stock.changePercentage > 0
                    "Top Losers" -> stock.changePercentage < 0
                    "US Tech" -> stock.sector == "Technology" && !stock.ticker.contains(".JK")
                    "IDX Bluechips" -> stock.ticker.contains(".JK")
                    "Dividends" -> stock.sector == "Finance" // proxy for dividend stocks
                    else -> true
                }
                
                val matchesSearch = stock.name.contains(searchQuery, ignoreCase = true) ||
                        stock.ticker.contains(searchQuery, ignoreCase = true) ||
                        stock.sector.contains(searchQuery, ignoreCase = true)

                matchesFilter && matchesSearch
            }

            val displayedStocks = viewModel.getSortedStocks(filteredStocks, activeFilter)
            
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(displayedStocks) { stock ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStockTicker = stock.ticker }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo (Real Logo via Clearbit with Letter fallback)
                        val domain = stock.logoDomain
                        Surface(
                            shape = CircleShape,
                            color = if (domain != null) Color.White else logoBg,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                if (domain != null) {
                                    coil.compose.SubcomposeAsyncImage(
                                        model = stock.logoUrl,
                                        contentDescription = stock.name,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.White)
                                            .padding(if (domain.contains(".id") || domain.contains("toyota")) 4.dp else 2.dp),
                                        loading = {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(logoBg)) {
                                                Text(
                                                    text = stock.name.take(1).uppercase(Locale.ROOT),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                            }
                                        },
                                        error = {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().background(logoBg)) {
                                                Text(
                                                    text = stock.name.take(1).uppercase(Locale.ROOT),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp
                                                )
                                            }
                                        }
                                    )
                                } else {
                                    Text(
                                        text = stock.name.take(1).uppercase(Locale.ROOT),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                        
                        // Middle Info
                        val acquiredDataList = playerState.ownedBusinesses.find { it.acquiredStockTicker == stock.ticker }
                        val displayTitle = acquiredDataList?.customName ?: stock.name
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = displayTitle,
                                color = if (acquiredDataList != null) gold else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stock.ticker,
                                    color = textGray,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (acquiredDataList != null) {
                                    Text(
                                        text = "👑 [Milik Pribadi]",
                                        color = gold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = stock.sector,
                                        color = textGray.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        
                        // Right Price Info
                        Column(horizontalAlignment = Alignment.End) {
                            val priceStr = "$ ${String.format(Locale.US, "%.2f", stock.currentPrice)}"
                            Text(
                                text = priceStr,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            val isPositive = stock.changeAbsolute >= 0
                            val changeColor = if (isPositive) neonGreen else red
                            val sign = if (isPositive) "+" else "-"
                            
                            val changeStr = "$ ${String.format(Locale.US, "%.2f", Math.abs(stock.changeAbsolute))}"
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = changeColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$sign $changeStr (${String.format(Locale.US, "%.2f", Math.abs(stock.changePercentage))}%)",
                                    color = changeColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = dividerColor)
                }
            }
        }
    }

    // Interactive details overlay dialog
    val selectedStock = stockListState.find { it.ticker == selectedStockTicker }
    val acquiredDataDetail = if (selectedStock != null) playerState.ownedBusinesses.find { it.acquiredStockTicker == selectedStock.ticker } else null

    if (selectedStock != null) {
        val stats = getMarketStats(selectedStock)
        val symbolPrefix = "$"
        val baseHistory = if (selectedStock.priceHistory.size > 2) selectedStock.priceHistory else getSimulatedHistory(selectedStock.ticker, selectedStock.currentPrice, chartInterval)
        
        // Simulasikan variasi chart interval dengan menggunakan sub-list atau padding secara visual
        val historyPoints = when (chartInterval) {
            "1D" -> baseHistory.takeLast(10)
            "1W" -> baseHistory.takeLast(20)
            else -> baseHistory
        }.ifEmpty { listOf(selectedStock.currentPrice, selectedStock.currentPrice) }
        
        val maxOfHistory = historyPoints.maxOrNull() ?: selectedStock.currentPrice
        val minOfHistory = historyPoints.minOrNull() ?: selectedStock.currentPrice
        
        val formatPrice: (Double) -> String = { p ->
            val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            "$ ${formatter.format(p)}"
        }

        val formatPriceWithUsd = formatPrice

        val isPositive = selectedStock.changeAbsolute >= 0
        val changeColor = if (isPositive) neonGreen else red
        val sign = if (isPositive) "+" else "-"
        val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        val diffStr = "$ ${formatter.format(Math.abs(selectedStock.changeAbsolute))}"

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedStockTicker = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().testTag("stock_detail_dialog"),
                color = bgDark
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Custom Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { selectedStockTicker = null },
                            modifier = Modifier.testTag("close_detail_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (selectedStock.logoUrl != null) {
                                        coil.compose.SubcomposeAsyncImage(
                                            model = selectedStock.logoUrl,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().padding(2.dp)
                                        )
                                    } else {
                                        Text(selectedStock.name.take(1).uppercase(java.util.Locale.ROOT), color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = acquiredDataDetail?.customName ?: selectedStock.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = selectedStock.ticker,
                            color = gold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.testTag("detail_ticker_label")
                        )
                    }
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header Price Info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("Current Share Price", color = textGray, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatPriceWithUsd(selectedStock.currentPrice),
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.testTag("detail_price_value")
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = changeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$sign $diffStr",
                                        color = changeColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        modifier = Modifier.testTag("detail_change_value")
                                    )
                                }
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", selectedStock.changePercentage)}%",
                                    color = changeColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chart Container Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(240.dp)
                        ) {
                            // Boundary grid labels
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(vertical = 12.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatPrice(maxOfHistory),
                                    color = textGray.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatPrice((maxOfHistory + minOfHistory) / 2),
                                    color = textGray.copy(alpha = 0.4f),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = formatPrice(minOfHistory),
                                    color = textGray.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Custom Line Chart Canvas rendering with offset
                            StockLineChart(
                                prices = historyPoints,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 75.dp),
                                lineColor = if (selectedStock.changePercentage >= 0) neonGreen else red
                            )
                        }

                        // Timescale row of interval selector chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val intervals = listOf("1D", "1W", "1M", "3M", "1Y")
                            intervals.forEach { interval ->
                                val isSelected = chartInterval == interval
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) gold.copy(alpha = 0.15f) else Color.Transparent,
                                    contentColor = if (isSelected) gold else textGray,
                                    border = if (isSelected) BorderStroke(1.dp, gold) else null,
                                    modifier = Modifier
                                        .clickable { chartInterval = interval }
                                        .testTag("interval_chip_${interval.lowercase()}")
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = interval,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Owned holdings status block
                        val ownedShares = myHoldings[selectedStock.ticker] ?: 0
                        if (ownedShares > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .background(gold.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .border(BorderStroke(1.dp, gold.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                                    .testTag("user_holdings_card"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Your holdings", color = gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("$ownedShares shares", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                val currentWorth = ownedShares * selectedStock.currentPrice
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Current worth", color = textGray, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(formatPriceWithUsd(currentWorth), color = neonGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Buy shares broad action buttons
                        val accentColor = if (selectedStock.changePercentage >= 0) Color(0xFF4CAF50) else Color(0xFF29B6F6)
                        Button(
                            onClick = { showBuyDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(54.dp)
                                .testTag("buy_shares_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.05f),
                                contentColor = accentColor
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddBusiness, contentDescription = null, tint = accentColor)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Buy shares",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        TakeoverInitiationCard(
                            stock = selectedStock,
                            currentOwnedShares = ownedShares,
                            isMegaHoldingActive = playerState.megaHolding.isActive,
                            isAlreadySubsidiary = acquiredDataDetail != null,
                            onInitiateTakeover = { showTakeoverDialog = true },
                            onShowRequirementDialog = { showMegaHoldingReqDialog = true },
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        val ownershipPct = (ownedShares.toDouble() / stats.sharesOutstanding.toDouble()) * 100.0
                        if (acquiredDataDetail != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = Color(0xFF333333),
                                    disabledContentColor = Color.LightGray
                                )
                            ) {
                                Text(
                                    text = "👑 Perusahaan Telah Diakuisisi (Milik Anda)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        } else if (ownershipPct >= 70.0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { 
                                    rebrandNameInput = selectedStock.name
                                    showRebrandDialog = true 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .height(54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = gold
                                )
                            ) {
                                Text(
                                    text = "👑 Akuisisi & Rebrand Perusahaan",
                                    color = bgDark,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Details grid Header
                        Text(
                            text = "Details",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Details parameters Card list
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardDark),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                DetailItem(label = "Share price", value = formatPriceWithUsd(selectedStock.currentPrice), subValue = "$sign $diffStr (${String.format(Locale.US, "%.2f", Math.abs(selectedStock.changePercentage))}%)", subValueColor = changeColor)
                                HorizontalDivider(color = dividerColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))
                                
                                DetailItem(label = "Dividend yield in the period*", value = "${String.format(Locale.US, "%.2f", stats.dividendYield)} %", subValue = "*Period = 12 months")
                                HorizontalDivider(color = dividerColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))
                                
                                val marketCapVal = stats.sharesOutstanding * selectedStock.currentPrice
                                val marketCapFormatted = formatMarketCap(marketCapVal)
                                DetailItem(label = "Company capitalization", value = marketCapFormatted)
                                HorizontalDivider(color = dividerColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))
                                
                                val sharesFormatted = java.text.NumberFormat.getNumberInstance(java.util.Locale.GERMANY).format(stats.sharesOutstanding)
                                DetailItem(label = "Number of available shares", value = sharesFormatted)
                                HorizontalDivider(color = dividerColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))
                                
                                DetailItem(label = "P/E Ratio", value = "${String.format(Locale.US, "%.1f", stats.peRatio)}x")
                                HorizontalDivider(color = dividerColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))
                                
                                DetailItem(label = "High / Low Today", value = "${formatPriceWithUsd(stats.highToday)} / ${formatPriceWithUsd(stats.lowToday)}")
                                HorizontalDivider(color = dividerColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))
                                
                                DetailItem(label = "Sector", value = selectedStock.sector)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }

    // Hostile Takeover Rebrand Dialog
    if (showRebrandDialog && selectedStock != null) {
        val originalStock = rawStockListState.find { it.ticker == selectedStock.ticker }
        val oldName = originalStock?.name ?: selectedStock.name
        
        androidx.compose.ui.window.Dialog(onDismissRequest = { showRebrandDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardDark,
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💎 Hostile Takeover",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = gold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Anda sekarang adalah pemilik mayoritas dari $oldName. Anda memiliki hak penuh untuk merestrukturisasi perusahaan ini.",
                        fontSize = 13.sp,
                        color = textGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = rebrandNameInput,
                        onValueChange = { rebrandNameInput = it },
                        label = { Text("Nama Baru Perusahaan", color = textGray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = gold,
                            unfocusedBorderColor = dividerColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    val canRebrand = rebrandNameInput.isNotBlank()
                    Button(
                        onClick = {
                            if (canRebrand) {
                                viewModel.rebrandCompany(selectedStock.ticker, oldName, rebrandNameInput)
                                buySuccessMessage = "Berhasil mengubah nama secara publik $oldName menjadi $rebrandNameInput!"
                                showRebrandDialog = false
                            }
                        },
                        enabled = canRebrand,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gold,
                            contentColor = bgDark,
                            disabledContainerColor = textGray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Biarkan Independen (Hanya Rebrand)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (canRebrand) {
                                viewModel.integrateStockToHolding(selectedStock.ticker, rebrandNameInput)
                                buySuccessMessage = "$oldName telah diintegrasikan menjadi anak usaha: $rebrandNameInput!"
                                showRebrandDialog = false
                            }
                        },
                        enabled = canRebrand,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00C853),
                            contentColor = bgDark,
                            disabledContainerColor = textGray.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Integrasi ke Mega Holding", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showRebrandDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Batal", color = textGray)
                    }
                }
            }
        }
    }

    // Transaction Buying Secondary Dialog
    if (showBuyDialog && selectedStock != null) {
        val currentPrice = selectedStock.currentPrice
        val balanceStr = "$ ${String.format(Locale.US, "%,.2f", usdBalance)}"
        val stats = getMarketStats(selectedStock)
        
        androidx.compose.ui.window.Dialog(onDismissRequest = { showBuyDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardDark,
                modifier = Modifier.padding(16.dp).fillMaxWidth().testTag("buy_transaction_dialog")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Beli Saham ${selectedStock.ticker}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedStock.name,
                        fontSize = 13.sp,
                        color = textGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Your Available Cash: $balanceStr",
                        color = gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("virtual_balance_label")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = buySharesAmount,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                buySharesAmount = input
                            }
                        },
                        label = { Text("Jumlah Saham (Lembar)", color = textGray) },
                        placeholder = { Text("0", color = textGray.copy(alpha = 0.5f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = gold,
                            unfocusedBorderColor = dividerColor
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("buy_shares_input")
                    )
                    
                    val quantity = buySharesAmount.toLongOrNull() ?: 0L
                    val totalCost = quantity * currentPrice
                    val requiredUsd = totalCost
                    
                    val outstandingShares = stats.sharesOutstanding
                    val ownedShares = playerState.ownedStocks.find { it.ticker == selectedStock.ticker }?.shares ?: 0L
                    val remainingSharesToBuy = maxOf(0L, outstandingShares - ownedShares)
                    
                    val maxByCash = (usdBalance / currentPrice).toLong()
                    val maxShares = minOf(maxByCash, remainingSharesToBuy)
                    
                    val totalCostStr = "$ ${String.format(Locale.US, "%.2f", totalCost)}"
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Maks yang bisa dibeli: %,d lembar".format(Locale.US, maxShares), color = textGray, fontSize = 12.sp)
                        TextButton(onClick = { buySharesAmount = maxShares.toString() }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                            Text("MAX", fontSize = 13.sp, color = gold, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Estimasi Biaya:", color = textGray, fontSize = 13.sp)
                        Text(totalCostStr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.testTag("total_cost_label"))
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    val canSubmit = quantity > 0 && requiredUsd <= usdBalance && quantity <= remainingSharesToBuy
                    Button(
                        onClick = {
                            val totalProspectiveShares = ownedShares + quantity
                            val totalProspectivePct = (totalProspectiveShares.toDouble() / outstandingShares.toDouble()) * 100.0
                            if (totalProspectivePct > 49.9 && !playerState.megaHolding.isActive) {
                                showMegaHoldingReqDialog = true
                                return@Button
                            }
                            viewModel.buyStock(selectedStock.ticker, currentPrice, quantity)
                            buySuccessMessage = "Berhasil membeli $quantity lembar saham ${selectedStock.name} (${selectedStock.ticker}) dengan estimasi harga $totalCostStr."
                            buySharesAmount = ""
                            showBuyDialog = false
                        },
                        enabled = canSubmit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.05f),
                            contentColor = Color(0xFF4CAF50),
                            disabledContainerColor = Color.White.copy(alpha = 0.05f),
                            disabledContentColor = Color.Gray
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("buy_submit_button")
                    ) {
                        Text(
                            text = if (requiredUsd > usdBalance) "Virtual Balance Tidak Cukup" else "Konfirmasi Pembelian", 
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showBuyDialog = false },
                        modifier = Modifier.fillMaxWidth().testTag("buy_cancel_button")
                    ) {
                        Text("Batal", color = textGray)
                    }
                }
            }
        }
    }

    // Success transaction feedback popup
    if (buySuccessMessage != null) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { buySuccessMessage = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardDark,
                modifier = Modifier.padding(16.dp).fillMaxWidth().testTag("success_transaction_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = neonGreen,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Transaksi Berhasil!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = buySuccessMessage ?: "",
                        fontSize = 13.sp,
                        color = textGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("success_message_txt")
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { buySuccessMessage = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("success_dismiss_button")
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))), 
                                RoundedCornerShape(12.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) { 
                            Text("Selesai", color = Color.Black, fontWeight = FontWeight.Bold) 
                        }
                    }
                }
            }
        }
    }

    if (showTakeoverDialog && selectedStock != null) {
        val ownedCount = myHoldings[selectedStock.ticker] ?: 0L
        TakeoverBiddingDialog(
            stock = selectedStock,
            currentOwnedShares = ownedCount,
            playerCash = playerState.cash,
            onDismiss = { showTakeoverDialog = false },
            onTakeoverFinalized = { finalPrice, targetStake, customName ->
                viewModel.executeStrategicTakeover(
                    stock = selectedStock,
                    finalPrice = finalPrice,
                    targetStake = targetStake,
                    customName = customName
                )
                showTakeoverDialog = false
            }
        )
    }

    if (showMegaHoldingReqDialog) {
        MegaHoldingRequirementDialog(
            onDismiss = { showMegaHoldingReqDialog = false },
            onNavigateToMegaHolding = {
                showMegaHoldingReqDialog = false
                selectedStockTicker = null
                navController.navigate("mega_holding_board")
            }
        )
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String,
    subValue: String? = null,
    subValueColor: Color = Color(0xFF9CA3AF)
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, color = Color(0xFF9CA3AF), fontSize = 12.sp, fontWeight = FontWeight.Normal)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        if (subValue != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subValue, color = subValueColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
