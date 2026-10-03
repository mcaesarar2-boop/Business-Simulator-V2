package com.example.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.core.treasury.HoldingInvestmentEngine
import com.example.data.PreciousMetal
import com.example.data.getMarketStats
import com.example.ui.treasury.BondMarketDialog
import com.example.ui.treasury.CorporateBondsCard
import com.example.ui.treasury.FixedIncomeFundCard
import com.example.ui.treasury.RdptRedeemDialog
import com.example.ui.treasury.RdptTopUpDialog
import com.example.viewmodel.GameViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetManagementScreen(navController: NavController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val metalsList by viewModel.preciousMetalsList.collectAsState()
    val stockList by viewModel.stockList.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()

    val holdingState = playerState.megaHolding
    val companyName = if (holdingState.investmentCompanyName.isNotBlank()) {
        holdingState.investmentCompanyName
    } else {
        "Asset Management & Investment Co."
    }

    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val slateDark = Color(0xFF252A34)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val neonGreen = Color(0xFF00FF00)
    val red = Color(0xFFFF5252)

    // Calculate Assets Under Management (Holding Investment Co. specific assets)
    val currentMetalsValue = holdingState.holdingMetals.entries.sumOf { (id, amount) ->
        val livePrice = metalsList.find { it.id == id }?.currentPrice ?: 0.0
        (amount * livePrice).toLong()
    }

    val totalTimeDeposits = holdingState.holdingTimeDeposits.sumOf { it.principal }

    val ownedStocks = playerState.ownedStocks.filter { it.shares > 0 }
    val stocksValue = ownedStocks.sumOf { owned ->
        val liveStock = stockList.find { it.ticker == owned.ticker }
        (owned.shares * (liveStock?.currentPrice ?: owned.averagePrice)).toLong()
    }

    var totalDividendIncome = 0L
    ownedStocks.forEach { owned ->
        val liveStock = stockList.find { it.ticker == owned.ticker }
        if (liveStock != null) {
            val currentPriceUsd = liveStock.currentPrice
            val stats = getMarketStats(liveStock)
            val monthlyYieldPercent = stats.dividendYield / 12.0 / 100.0
            totalDividendIncome += (owned.shares * currentPriceUsd * monthlyYieldPercent).toLong()
        }
    }

    // Corporate Treasury Bonds & RDPT
    val availableBonds by viewModel.holdingInvestmentRepository.availableBonds.collectAsState()
    val fundCatalog by viewModel.holdingInvestmentRepository.fundCatalog.collectAsState()
    val totalBondsValue = holdingState.holdingBonds.sumOf { it.principal }
    val totalRdptValue = holdingState.holdingRdpt.currentValue(holdingState.holdingRdptNav)
    val totalMonthlyCoupon = HoldingInvestmentEngine.calculateMonthlyCouponPayout(holdingState.holdingBonds)

    val totalAum = currentMetalsValue + totalTimeDeposits + stocksValue + totalBondsValue + totalRdptValue

    // Commodity Dialog State
    var showTransactionDialog by remember { mutableStateOf(false) }
    var selectedMetal by remember { mutableStateOf<PreciousMetal?>(null) }
    var transactionAmount by remember { mutableStateOf("") }
    var isBuying by remember { mutableStateOf(true) }

    // Time Deposit Dialog State
    var showTimeDepositDialog by remember { mutableStateOf(false) }
    var timeDepositAmount by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(3) }

    // Treasury Instruments Dialog State
    var showBondMarketDialog by remember { mutableStateOf(false) }
    var showRdptTopUpDialog by remember { mutableStateOf(false) }
    var showRdptRedeemDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = companyName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "HOLDING ASSET MANAGEMENT & INVESTMENT",
                            color = gold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = gold)
                    }
                },
                actions = {
                    val stalledOffers by viewModel.maRepository.pendingStalledOffers.collectAsState()
                    IconButton(onClick = { viewModel.maRepository.openInboxDialog() }) {
                        BadgedBox(
                            badge = {
                                if (stalledOffers.isNotEmpty()) {
                                    Badge(containerColor = Color(0xFFE57373)) {
                                        Text("${stalledOffers.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.MarkEmailRead, contentDescription = "M&A Proposals", tint = gold)
                        }
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
            // Hero AUM Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(170.dp)) {
                        AsyncImage(
                            model = "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?q=80&w=1200&auto=format&fit=crop",
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF0F172A).copy(alpha = 0.5f),
                                            Color(0xFF0F172A).copy(alpha = 0.95f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .matchParentSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = gold.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, gold.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "DIRECT SUBSIDIARY OF HOLDING",
                                        color = gold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                if (totalDividendIncome > 0L) {
                                    Text(
                                        text = "+${formatCurrencyRingkas(totalDividendIncome, useShortFormat)}/bln",
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Total Assets Under Management (AUM)",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$ ${String.format(Locale.US, "%,d", totalAum)}",
                                    color = gold,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // Investment Portfolio Hub Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { navController.navigate("my_portfolio_detail") },
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, gold.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = gold.copy(alpha = 0.15f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = gold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Portofolio Investasi & Saham",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "${ownedStocks.size} Saham Aktif Dimiliki",
                                        color = textGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Detail Saham",
                                tint = gold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Nilai Saham", color = textGray, fontSize = 11.sp)
                                Text(
                                    "$${String.format(Locale.US, "%,d", stocksValue)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Estimasi Dividen / Bln", color = textGray, fontSize = 11.sp)
                                Text(
                                    "+$${String.format(Locale.US, "%,d", totalDividendIncome)}",
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { navController.navigate("my_portfolio_detail") },
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = gold,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Kelola Portofolio", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { navController.navigate("global_stock_market") },
                                modifier = Modifier.weight(1f).height(42.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = gold),
                                border = androidx.compose.foundation.BorderStroke(1.dp, gold.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Bursa Efek (Beli)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Time Deposits Header
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Time Deposit (Deposito)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "Total: $${String.format(Locale.US, "%,d", totalTimeDeposits)}",
                            color = gold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showTimeDepositDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = gold,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Deposito Baru", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (holdingState.holdingTimeDeposits.isEmpty()) {
                item {
                    Surface(
                        color = cardDark,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Belum ada deposito berjangka aktif di entitas Holding Asset Management.",
                            color = textGray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(holdingState.holdingTimeDeposits) { deposit ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Deposit ${deposit.durationMonths} Bulan",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "Yield: ${(deposit.interestRate * 100).toInt()}% | Sisa: ${deposit.monthsRemaining} bln",
                                        color = textGray,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    "$${String.format(Locale.US, "%,d", deposit.principal)}",
                                    color = gold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    viewModel.withdrawHoldingTimeDeposit(
                                        deposit.id,
                                        isEarly = deposit.monthsRemaining > 1
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (deposit.monthsRemaining <= 1) neonGreen else red.copy(alpha = 0.2f),
                                    contentColor = if (deposit.monthsRemaining <= 1) Color.Black else red
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    if (deposit.monthsRemaining <= 1) "Cairkan (Tersedia)" else "Tarik Paksa (Penalti -5%)",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Instrument 1: Corporate & Sovereign Bonds Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                CorporateBondsCard(
                    holdings = holdingState.holdingBonds,
                    totalEstimatedCouponMonthly = totalMonthlyCoupon,
                    totalCouponEarnedAllTime = holdingState.totalBondCouponIncomeEarned,
                    useShortFormat = useShortFormat,
                    onOpenBondMarket = { showBondMarketDialog = true },
                    onLiquidateEarly = { holding, _ ->
                        viewModel.liquidateHoldingBondEarly(holding.id)
                    },
                    onClaimMatured = { holding ->
                        viewModel.claimMaturedHoldingBond(holding.id)
                    }
                )
            }

            // Instrument 2: Fixed Income Mutual Funds (RDPT) Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                FixedIncomeFundCard(
                    fund = fundCatalog,
                    currentNav = holdingState.holdingRdptNav,
                    holding = holdingState.holdingRdpt,
                    useShortFormat = useShortFormat,
                    onOpenTopUp = { showRdptTopUpDialog = true },
                    onOpenRedeem = { showRdptRedeemDialog = true }
                )
            }

            // Safe Haven Commodities Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Komoditas & Logam Mulia",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "Total: $${String.format(Locale.US, "%,d", currentMetalsValue)}",
                        color = gold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(metalsList.distinctBy { it.id }) { metal ->
                val ownedAmount = holdingState.holdingMetals[metal.id] ?: 0.0
                val averagePrice = holdingState.holdingMetalsAveragePrices[metal.id] ?: 0.0
                val totalValue = (ownedAmount * metal.currentPrice).toLong()

                val profitLossPct = if (averagePrice > 0) ((metal.currentPrice - averagePrice) / averagePrice) * 100 else 0.0
                val plColor = if (profitLossPct >= 0) neonGreen else red
                val plSign = if (profitLossPct >= 0) "+" else ""

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = slateDark,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = gold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        metal.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Harga: $${String.format(Locale.US, "%,.2f", metal.currentPrice)} / ${metal.unit}",
                                        color = textGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Dimiliki: ${String.format(Locale.US, "%.2f", ownedAmount)} ${metal.unit}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "$${String.format(Locale.US, "%,d", totalValue)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                if (ownedAmount > 0 && averagePrice > 0) {
                                    Text(
                                        "Avg: $${String.format(Locale.US, "%.2f", averagePrice)} ($plSign${String.format(Locale.US, "%.1f", profitLossPct)}%)",
                                        color = plColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    selectedMetal = metal
                                    isBuying = true
                                    transactionAmount = ""
                                    showTransactionDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = gold,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Beli", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    selectedMetal = metal
                                    isBuying = false
                                    transactionAmount = ""
                                    showTransactionDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                enabled = ownedAmount > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = slateDark,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Jual", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }

    // Precious Metals Transaction Dialog (Using Holding Cash)
    if (showTransactionDialog && selectedMetal != null) {
        val metal = selectedMetal!!
        val parsedAmount = transactionAmount.toDoubleOrNull() ?: 0.0
        val totalWorth = (parsedAmount * metal.currentPrice).toLong()

        AlertDialog(
            onDismissRequest = { showTransactionDialog = false },
            containerColor = cardDark,
            title = {
                Text(
                    if (isBuying) "Beli ${metal.name}" else "Jual ${metal.name}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Total ${if (isBuying) "Biaya" else "Pendapatan"}: $${String.format(Locale.US, "%,d", totalWorth)}",
                        color = neonGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = transactionAmount,
                        onValueChange = { transactionAmount = it },
                        label = { Text("Jumlah (${metal.unit})", color = textGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = gold,
                            unfocusedBorderColor = textGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isBuying) {
                            Text(
                                "Saldo Kas Holding: $${String.format(Locale.US, "%,d", playerState.cash)}",
                                color = textGray,
                                fontSize = 12.sp
                            )
                            val maxCanBuy = if (metal.currentPrice > 0) playerState.cash / metal.currentPrice else 0.0
                            TextButton(
                                onClick = { transactionAmount = String.format(Locale.US, "%.2f", maxCanBuy) },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("MAX", fontSize = 12.sp, color = gold, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            val owned = holdingState.holdingMetals[metal.id] ?: 0.0
                            Text(
                                "Dimiliki: ${String.format(Locale.US, "%.2f", owned)} ${metal.unit}",
                                color = textGray,
                                fontSize = 12.sp
                            )
                            TextButton(
                                onClick = {
                                    transactionAmount = String.format(
                                        Locale.US,
                                        "%.2f",
                                        owned
                                    )
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("MAX", fontSize = 12.sp, color = red, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isBuying) {
                            if (parsedAmount > 0 && playerState.cash >= totalWorth) {
                                viewModel.buyHoldingMetal(metal.id, parsedAmount)
                                showTransactionDialog = false
                            }
                        } else {
                            val owned = holdingState.holdingMetals[metal.id] ?: 0.0
                            if (parsedAmount in 0.0..owned) {
                                viewModel.sellHoldingMetal(metal.id, parsedAmount)
                                showTransactionDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                ) {
                    Text("Konfirmasi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransactionDialog = false }) {
                    Text("Batal", color = textGray)
                }
            }
        )
    }

    // Time Deposit Dialog (Using Holding Cash)
    if (showTimeDepositDialog) {
        val parsedAmount = timeDepositAmount.toLongOrNull() ?: 0L
        AlertDialog(
            onDismissRequest = { showTimeDepositDialog = false },
            containerColor = cardDark,
            title = { Text("Buka Time Deposit (Holding)", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = timeDepositAmount,
                        onValueChange = { timeDepositAmount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah USD", color = textGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = gold,
                            unfocusedBorderColor = textGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Saldo Kas Holding: $${String.format(Locale.US, "%,d", playerState.cash)}",
                            color = textGray,
                            fontSize = 12.sp
                        )
                        TextButton(
                            onClick = { timeDepositAmount = playerState.cash.toString() },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("MAX", fontSize = 12.sp, color = gold, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Pilih Durasi:", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    val durations = listOf(
                        3 to "3 Bulan (Yield: 5%)",
                        6 to "6 Bulan (Yield: 12%)",
                        12 to "12 Bulan (Yield: 30%)"
                    )
                    durations.forEach { (months, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDuration = months }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedDuration == months,
                                onClick = { selectedDuration = months },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = gold,
                                    unselectedColor = textGray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                label,
                                color = if (selectedDuration == months) gold else Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (parsedAmount > 0 && playerState.cash >= parsedAmount) {
                            viewModel.openHoldingTimeDeposit(parsedAmount, selectedDuration)
                            showTimeDepositDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                    enabled = parsedAmount > 0 && playerState.cash >= parsedAmount
                ) {
                    Text("Buka Deposit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDepositDialog = false }) {
                    Text("Batal", color = textGray)
                }
            }
        )
    }

    // Bond Market Dialog
    if (showBondMarketDialog) {
        BondMarketDialog(
            bonds = availableBonds,
            holdingCash = playerState.cash,
            onDismiss = { showBondMarketDialog = false },
            onPurchaseBond = { bond, amount ->
                viewModel.buyHoldingBond(bond, amount)
            }
        )
    }

    // RDPT Top Up Dialog
    if (showRdptTopUpDialog) {
        RdptTopUpDialog(
            fund = fundCatalog,
            currentNav = holdingState.holdingRdptNav,
            holdingCash = playerState.cash,
            onDismiss = { showRdptTopUpDialog = false },
            onConfirmTopUp = { amount ->
                viewModel.topUpHoldingRdpt(amount)
            }
        )
    }

    // RDPT Redeem Dialog
    if (showRdptRedeemDialog) {
        RdptRedeemDialog(
            fund = fundCatalog,
            currentNav = holdingState.holdingRdptNav,
            holding = holdingState.holdingRdpt,
            onDismiss = { showRdptRedeemDialog = false },
            onConfirmRedeem = { units ->
                viewModel.redeemHoldingRdpt(units)
            }
        )
    }
}
