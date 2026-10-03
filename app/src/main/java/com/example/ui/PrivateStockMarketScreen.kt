package com.example.ui

import com.example.viewmodel.GameViewModel
import com.example.data.*
import com.example.ui.formatMarketCap

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

@Composable
fun PrivateStockLineChart(
    prices: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFFFFD700)
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

@Composable
fun PrivateStockMarketScreen(navController: NavHostController, viewModel: GameViewModel, initialTicker: String? = null) {
    // Theme Colors
    val bgDark = Color(0xFF10141D)
    val cardDark = Color(0xFF161C26)
    val dividerColor = Color(0xFF1E2638)
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
    
    val privateBalance = playerState.privateBalance.toDouble()
    
    // Interactive Detail Selection States
    var selectedStockTicker by remember { mutableStateOf<String?>(initialTicker) }
    var chartInterval by remember { mutableStateOf("1D") }
    
    // Private Portfolio is now privateStockPortfolio
    val myPrivHoldings = playerState.privateStockPortfolio.associate { it.ticker to it.shares }
    
    // Transaction Modals State
    var showBuyDialog by remember { mutableStateOf(false) }
    var buySharesAmount by remember { mutableStateOf("") }
    var buySuccessMessage by remember { mutableStateOf<String?>(null) }
    
    var showNewsHistoryDialog by remember { mutableStateOf(false) }

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
                    Text("Private Stock Market", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
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

            // Private Balance Indicator Bar
            Surface(
                color = cardDark.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, gold.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kas Pribadi (CEO Private Cash):", color = textGray, fontSize = 12.sp)
                    Text("$ ${String.format(Locale.US, "%,d", playerState.privateBalance)}", color = gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari berdasarkan nama, emiten, atau sektor...", color = textGray) },
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
                        val myHoldShares = myPrivHoldings[stock.ticker] ?: 0L
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = stock.name,
                                color = if (myHoldShares > 0) gold else Color.White,
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
                                if (myHoldShares > 0) {
                                    Text(
                                        text = "💼 [Dimiliki: $myHoldShares]",
                                        color = gold,
                                        fontSize = 11.sp,
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

    // Details Dialog overlay
    val selectedStock = stockListState.find { it.ticker == selectedStockTicker }
    if (selectedStock != null) {
        val stats = getMarketStats(selectedStock)
        val symbolPrefix = "$"
        val baseHistory = if (selectedStock.priceHistory.size > 2) selectedStock.priceHistory else getSimulatedHistory(selectedStock.ticker, selectedStock.currentPrice, chartInterval)
        
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
                modifier = Modifier.fillMaxSize(),
                color = bgDark
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                            .statusBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { selectedStockTicker = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                        }
                        
                        Text(
                            text = "Aset Saham Pribadi CEO",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        
                        IconButton(onClick = { 
                            showBuyDialog = true
                            buySharesAmount = ""
                        }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Beli", tint = gold)
                        }
                    }
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Title Area
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = selectedStock.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                lineHeight = 34.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${selectedStock.ticker} • ${selectedStock.sector}",
                                    color = textGray,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if ((myPrivHoldings[selectedStock.ticker] ?: 0L) > 0L) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = gold.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "DI-KERANJANG",
                                            color = gold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Live price section
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = formatPriceWithUsd(selectedStock.currentPrice),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isPositive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = changeColor
                                    )
                                    Text(
                                        text = "$sign $diffStr (${String.format(Locale.US, "%.2f", Math.abs(selectedStock.changePercentage))}%)",
                                        color = changeColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            // Visual badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = cardDark,
                                modifier = Modifier.size(52.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = selectedStock.ticker.take(2).uppercase(Locale.ROOT),
                                        color = gold,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Chart Interval selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("1D", "1W", "1M").forEach { interval ->
                                val isSelected = chartInterval == interval
                                Surface(
                                    color = if (isSelected) gold else cardDark,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { chartInterval = interval }
                                ) {
                                    Text(
                                        text = interval,
                                        color = if (isSelected) Color.Black else textGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Draw Private Stock Line Chart
                        PrivateStockLineChart(
                            prices = historyPoints,
                            lineColor = changeColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(horizontal = 24.dp)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Rendah: " + formatPrice(minOfHistory), color = textGray, fontSize = 11.sp)
                            Text(text = "Volatilitas: ${String.format(Locale.US, "%.1f", marketVolatilityFactor * 100)}%", color = textGray, fontSize = 11.sp)
                            Text(text = "Tinggi: " + formatPrice(maxOfHistory), color = textGray, fontSize = 11.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions for Private Stock: Buy and Sell using Private Balance
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardDark),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                val myShares = myPrivHoldings[selectedStock.ticker] ?: 0L
                                
                                Button(
                                    onClick = {
                                        navController.navigate("my_private_portfolio")
                                        selectedStockTicker = null
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Portfolio", fontSize = 14.sp)
                                }
                                
                                Button(
                                    onClick = { 
                                        showBuyDialog = true
                                        buySharesAmount = ""
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("BELI SAHAM", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

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

    // Transaction Buying Secondary Dialog
    if (showBuyDialog && selectedStock != null) {
        val currentPrice = selectedStock.currentPrice
        val balanceStr = "$ ${String.format(Locale.US, "%,.2f", privateBalance)}"
        val stats = getMarketStats(selectedStock)
        
        androidx.compose.ui.window.Dialog(onDismissRequest = { showBuyDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardDark,
                modifier = Modifier.padding(16.dp).fillMaxWidth().testTag("buy_transaction_dialog")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Beli Saham (Pribadi) ${selectedStock.ticker}",
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
                        text = "CEO Private Balance: $balanceStr",
                        color = gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("private_balance_label")
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
                        modifier = Modifier.fillMaxWidth().testTag("private_buy_shares_input")
                    )
                    
                    val quantity = buySharesAmount.toLongOrNull() ?: 0L
                    val totalCost = quantity * currentPrice
                    val requiredUsd = totalCost
                    
                    val outstandingShares = stats.sharesOutstanding
                    val ownedShares = playerState.privateStockPortfolio.find { it.ticker == selectedStock.ticker }?.shares ?: 0L
                    val remainingSharesToBuy = maxOf(0L, outstandingShares - ownedShares)
                    
                    val maxByCash = (privateBalance / currentPrice).toLong()
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
                    
                    val canSubmit = quantity > 0 && requiredUsd <= privateBalance && quantity <= remainingSharesToBuy
                    Button(
                        onClick = {
                            viewModel.buyPrivateStock(selectedStock.ticker, currentPrice, quantity)
                            buySuccessMessage = "Berhasil membeli $quantity lembar saham ${selectedStock.name} (${selectedStock.ticker}) dari bursa saham menggunakan Kas Pribadi dengan total nominal $totalCostStr."
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
                            text = if (requiredUsd > privateBalance) "Kas Pribadi Tidak Cukup" else "Konfirmasi Pembelian", 
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
