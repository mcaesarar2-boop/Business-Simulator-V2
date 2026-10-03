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
fun MyPortfolioScreen(navController: NavController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val stockList by viewModel.stockList.collectAsState()
    
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val slateDark = Color(0xFF252A34)
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
    val ownedStocks = playerState.ownedStocks.filter { it.shares > 0 }

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
                title = { Text("My Stock Portfolio", color = Color.White, fontWeight = FontWeight.Bold) },
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
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Total Portfolio Value", color = textGray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$ ${String.format(Locale.US, "%,.2f", currentStocksValue)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$profitSign$ ${String.format(Locale.US, "%,.2f", Math.abs(profitLoss))} (${String.format(Locale.US, "%.2f", profitLossPct)}%)", color = profitColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (ownedStocks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Portofolio Anda Kosong", color = textGray, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Yuk, mulai investasi pertamamu hari ini!", color = textGray, fontSize = 14.sp)
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
                            .clickable { navController.navigate("global_stock_market?ticker=${liveStock.ticker}") }
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
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
                                        Text("${owned.shares} Lembar | Harga Rata-rata: $$displayAvgPrice", color = textGray, fontSize = 12.sp)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = slateDark)
                            Spacer(modifier = Modifier.height(12.dp))

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
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Kepemilikan", color = textGray, fontSize = 12.sp)
                                    // Make sure it handles tiny ownerships format nicely
                                    Text(
                                        String.format(Locale.US, "%.4f%%", ownershipPct),
                                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Kapitalisasi Pasar", color = textGray, fontSize = 12.sp)
                                    Text("$${String.format(Locale.US, "%,.0f", marketCap)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                                        showSellDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color(0xFFE53935)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Jual", fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = {
                                        selectedStockToBuy = liveStock
                                        showBuyDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color(0xFF4CAF50)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Beli Lagi", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }

    if (showBuyDialog && selectedStockToBuy != null) {
        val stock = selectedStockToBuy!!
        val shares = buySharesAmount.toLongOrNull() ?: 0L
        val stockPriceUsd = stock.currentPrice
        
        val cost = shares * stock.currentPrice
        val costInUsd = cost
        val maxShares = (playerState.cash / stockPriceUsd).toLong()

        AlertDialog(
            onDismissRequest = { 
                showBuyDialog = false
                buySharesAmount = ""
            },
            containerColor = cardDark,
            title = {
                Text("Beli ${stock.ticker}", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    val priceStr = "$${String.format(Locale.US, "%,.2f", stock.currentPrice)}"
                    Text("Harga: $priceStr per lembar", color = textGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Saldo: $${String.format(Locale.US, "%,.2f", playerState.cash.toDouble())}", color = neonGreen, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = buySharesAmount,
                        onValueChange = { buySharesAmount = it.filter { c -> c.isDigit() } },
                        label = { Text("Jumlah Lembar", color = textGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = gold,
                            unfocusedBorderColor = textGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Maks yang bisa dibeli: %,d lembar".format(Locale.US, maxShares), color = textGray, fontSize = 12.sp)
                        TextButton(onClick = { buySharesAmount = maxShares.toString() }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                            Text("MAX", fontSize = 13.sp, color = gold, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val costStr = "$${String.format(Locale.US, "%,.2f", cost)}"
                    Text("Total Biaya: $costStr", color = if (playerState.cash >= costInUsd && shares > 0) neonGreen else red, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (shares > 0 && playerState.cash >= costInUsd) {
                            viewModel.buyStock(stock.ticker, stock.currentPrice, shares)
                            showBuyDialog = false
                            buySharesAmount = ""
                        }
                    },
                    enabled = shares > 0 && playerState.cash >= costInUsd,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = gold,
                        contentColor = Color.Black,
                        disabledContainerColor = slateDark,
                        disabledContentColor = textGray
                    )
                ) {
                    Text("Konfirmasi Beli")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showBuyDialog = false
                        buySharesAmount = ""
                    }
                ) {
                    Text("Batal", color = textGray)
                }
            }
        )
    }

    if (showSellDialog && selectedStockToSell != null) {
        val stock = selectedStockToSell!!
        val shares = sellSharesAmount.toLongOrNull() ?: 0L
        var cost = shares * stock.currentPrice
        val revenueUsd = cost
        
        val ownedShares = playerState.ownedStocks.find { it.ticker == stock.ticker }?.shares ?: 0L

        AlertDialog(
            onDismissRequest = { 
                showSellDialog = false
                sellSharesAmount = ""
            },
            containerColor = cardDark,
            title = {
                Text("Jual ${stock.ticker}", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    val priceStr = "$${String.format(Locale.US, "%,.2f", stock.currentPrice)}"
                    Text("Harga Saat Ini: $priceStr per lembar", color = textGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Jumlah Dimiliki: %,d lembar".format(Locale.US, ownedShares), color = neonGreen, fontSize = 14.sp)
                        TextButton(onClick = { sellSharesAmount = ownedShares.toString() }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                            Text("MAX", fontSize = 13.sp, color = red, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = sellSharesAmount,
                        onValueChange = { sellSharesAmount = it.filter { c -> c.isDigit() } },
                        label = { Text("Jumlah Lembar", color = textGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = red,
                            unfocusedBorderColor = textGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estimasi Pendapatan: $${String.format(Locale.US, "%,.2f", revenueUsd)}", color = if (shares > 0 && shares <= ownedShares) neonGreen else red, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (shares > 0 && shares <= ownedShares) {
                            viewModel.sellStock(stock.ticker, stock.currentPrice, shares)
                            showSellDialog = false
                            sellSharesAmount = ""
                        }
                    },
                    enabled = shares > 0 && shares <= ownedShares,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = red,
                        contentColor = Color.White,
                        disabledContainerColor = slateDark,
                        disabledContentColor = textGray
                    )
                ) {
                    Text("Konfirmasi Jual")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showSellDialog = false
                        sellSharesAmount = ""
                    }
                ) {
                    Text("Batal", color = textGray)
                }
            }
        )
    }
}
