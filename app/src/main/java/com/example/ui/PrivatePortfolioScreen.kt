package com.example.ui

import com.example.viewmodel.GameViewModel
import com.example.data.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.StockItem
import com.example.data.getMarketStats
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivatePortfolioScreen(navController: NavController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val stockList by viewModel.stockList.collectAsState()
    
    val bgDark = Color(0xFF10141D)
    val cardDark = Color(0xFF161C26)
    val slateDark = Color(0xFF1E2638)
    val gold = Color(0xFFFFD700)
    val neonGreen = Color(0xFF00FF00)
    val textGray = Color(0xFFA0A0A0)
    val red = Color(0xFFFF3B30)

    var showBuyDialog by remember { mutableStateOf(false) }
    var selectedStockToBuy by remember { mutableStateOf<StockItem?>(null) }
    var buySharesAmount by remember { mutableStateOf("") }
    
    var showSellDialog by remember { mutableStateOf(false) }
    var selectedStockToSell by remember { mutableStateOf<StockItem?>(null) }
    var sellSharesAmount by remember { mutableStateOf("") }

    var totalCostBasis = 0.0
    var currentStocksValue = 0.0

    // Only non-empty portfolios
    val ownedStocks = playerState.privateStockPortfolio.filter { it.shares > 0 }

    ownedStocks.forEach { owned ->
        val liveStock = stockList.find { it.ticker == owned.ticker }
        val livePrice = liveStock?.currentPrice ?: owned.averagePrice
        currentStocksValue += owned.shares * livePrice
        
        val safeAveragePrice = if (owned.averagePrice <= 0.0) livePrice else owned.averagePrice
        totalCostBasis += owned.shares * safeAveragePrice
    }

    val profitLoss = currentStocksValue - totalCostBasis
    val profitLossPct = if (totalCostBasis > 0) (profitLoss / totalCostBasis) * 100 else 0.0
    val profitColor = if (profitLoss >= 0) neonGreen else red
    val profitSign = if (profitLoss >= 0) "+" else ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Private Portfolio", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
            )
        },
        containerColor = bgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(cardDark, Color(0xFF10141D))),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, gold.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Total Private Portfolio Value", color = textGray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$ ${String.format(Locale.US, "%,.2f", currentStocksValue)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$profitSign$ ${String.format(Locale.US, "%,.2f", Math.abs(profitLoss))} (${String.format(Locale.US, "%.2f", profitLossPct)}%)", color = profitColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kas Pribadi (Private Balance)", color = textGray, fontSize = 12.sp)
                            Text("$ ${String.format(Locale.US, "%,d", playerState.privateBalance)}", color = gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            if (ownedStocks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Portofolio Pribadi Anda Kosong", color = textGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Yuk, gunakan Kas Pribadi Anda untuk trading di Private Stock Market!", color = textGray, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }

            items(ownedStocks) { owned ->
                val liveStock = stockList.find { it.ticker == owned.ticker }
                if (liveStock != null) {
                    val currentPriceUsd = liveStock.currentPrice
                    val livePrice = currentPriceUsd
                    val marketStats = getMarketStats(liveStock)
                    
                    val safeAveragePrice = if (owned.averagePrice <= 0.0) livePrice else owned.averagePrice
                    val value = owned.shares * livePrice
                    val cost = owned.shares * safeAveragePrice
                    val pl = value - cost
                    val plPct = if (cost > 0) (pl / cost) * 100 else 0.0
                    val isProfit = pl >= 0
                    
                    val marketCap = marketStats.sharesOutstanding * livePrice
                    val rawOwnershipPct = if (marketStats.sharesOutstanding > 0) {
                        (owned.shares.toDouble() / marketStats.sharesOutstanding.toDouble()) * 100
                    } else 0.0
                    val ownershipPct = rawOwnershipPct.coerceAtMost(100.0)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("private_stock_market?ticker=${liveStock.ticker}") }
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(cardDark, Color(0xFF10141D))),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(40.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (liveStock.logoUrl != null) {
                                                coil.compose.SubcomposeAsyncImage(
                                                    model = liveStock.logoUrl,
                                                    contentDescription = liveStock.name,
                                                    modifier = Modifier.fillMaxSize().padding(4.dp),
                                                    loading = { Icon(Icons.Default.AccountBalance, null, tint = Color.LightGray) },
                                                    error = { Icon(Icons.Default.AccountBalance, null, tint = Color.LightGray) }
                                                )
                                            } else {
                                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = gold, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("${liveStock.ticker} - ${liveStock.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        val displayAvgPrice = if (owned.averagePrice < 0.01 && owned.averagePrice > 0) {
                                            String.format(Locale.US, "%,.4f", owned.averagePrice)
                                        } else {
                                            String.format(Locale.US, "%,.2f", owned.averagePrice)
                                        }
                                        Text("${owned.shares} Lembar | Rata-rata: $$displayAvgPrice", color = textGray, fontSize = 12.sp)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = slateDark)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Nilai Saat Ini", color = textGray, fontSize = 12.sp)
                                    Text("$${String.format(Locale.US, "%,.2f", value)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("P/L", color = textGray, fontSize = 12.sp)
                                    Text(
                                        "${if (isProfit) "+" else ""}$${String.format(Locale.US, "%,.2f", pl)} (${String.format(Locale.US, "%.2f", plPct)}%)",
                                        color = if (isProfit) neonGreen else red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Kepemilikan Pribadi", color = textGray, fontSize = 12.sp)
                                    Text(
                                        String.format(Locale.US, "%.4f%%", ownershipPct),
                                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Harga Live", color = textGray, fontSize = 12.sp)
                                    Text("$${String.format(Locale.US, "%,.2f", livePrice)}", color = gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        selectedStockToSell = liveStock
                                        sellSharesAmount = ""
                                        showSellDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color(0xFFE53935)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("JUAL", fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = {
                                        selectedStockToBuy = liveStock
                                        buySharesAmount = ""
                                        showBuyDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("BELI LAGI", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Sell Dialog
    if (showSellDialog && selectedStockToSell != null) {
        val stock = selectedStockToSell!!
        val ownedShares = playerState.privateStockPortfolio.find { it.ticker == stock.ticker }?.shares ?: 0L
        
        AlertDialog(
            onDismissRequest = { showSellDialog = false },
            title = { Text("Jual Saham Pribadi ${stock.ticker}", color = Color.White) },
            text = {
                Column {
                    Text("Harga saat ini: $ ${String.format(Locale.US, "%,.2f", stock.currentPrice)}", color = textGray)
                    Text("Kepemilikan pribadi: $ownedShares lembar", color = textGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = sellSharesAmount,
                        onValueChange = { sellSharesAmount = it },
                        label = { Text("Jumlah Saham", color = gold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = gold,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val inputShares = sellSharesAmount.toLongOrNull() ?: 0L
                Button(
                    onClick = {
                        viewModel.sellPrivateStock(stock.ticker, stock.currentPrice, inputShares)
                        showSellDialog = false
                    },
                    enabled = inputShares > 0 && inputShares <= ownedShares,
                    colors = ButtonDefaults.buttonColors(containerColor = red, contentColor = Color.White)
                ) {
                    Text("Konfirmasi Jual")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSellDialog = false }) {
                    Text("Batal", color = textGray)
                }
            },
            containerColor = cardDark
        )
    }

    // Buy Dialog
    if (showBuyDialog && selectedStockToBuy != null) {
        val stock = selectedStockToBuy!!
        val maxBuy = (playerState.privateBalance / stock.currentPrice).toLong()
        
        AlertDialog(
            onDismissRequest = { showBuyDialog = false },
            title = { Text("Beli Saham Pribadi ${stock.ticker}", color = Color.White) },
            text = {
                Column {
                    Text("Harga saat ini: $ ${String.format(Locale.US, "%,.2f", stock.currentPrice)}", color = textGray)
                    Text("Maksimal beli dengan Kas Pribadi: $maxBuy lembar", color = textGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = buySharesAmount,
                        onValueChange = { buySharesAmount = it },
                        label = { Text("Jumlah Saham", color = gold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = gold,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val inputShares = buySharesAmount.toLongOrNull() ?: 0L
                val totalCost = inputShares * stock.currentPrice
                Button(
                    onClick = {
                        viewModel.buyPrivateStock(stock.ticker, stock.currentPrice, inputShares)
                        showBuyDialog = false
                    },
                    enabled = inputShares > 0 && totalCost <= playerState.privateBalance,
                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen, contentColor = Color.Black)
                ) {
                    Text("Konfirmasi Beli")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBuyDialog = false }) {
                    Text("Batal", color = textGray)
                }
            },
            containerColor = cardDark
        )
    }
}
