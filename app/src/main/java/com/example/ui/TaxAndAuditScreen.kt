package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.viewmodel.GameViewModel
import com.example.data.getCatalogItem
import com.example.data.getBusinessValuation
import com.example.data.getBusinessStats
import com.example.data.calculateMegaHoldingValuation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxAndAuditScreen(navController: NavController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val context = LocalContext.current
    
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val gold = Color(0xFFFFD700)
    val neonGreen = Color(0xFF39FF14)
    val textGray = Color(0xFFA0A0A0)
    val lightRed = Color(0xFFFF3B30)
    val warningYellow = Color(0xFFFFCC00)
    
    // ----------------------------------------------------
    // ASSET VALUATION CALCULATIONS FOR SPT LISTING
    // ----------------------------------------------------
    val stockList by viewModel.stockList.collectAsState()
    val cryptoList by viewModel.cryptoList.collectAsState()
    val realEstateMarket by viewModel.realEstateMarket.collectAsState()
    val collectionList by viewModel.collectionList.collectAsState()

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
    
    val privateStocksValue = playerState.privateStockPortfolio.sumOf { owned ->
        val liveStock = stockList.find { it.ticker == owned.ticker }
        val livePrice = liveStock?.currentPrice ?: owned.averagePrice
        (owned.shares * livePrice).toLong()
    }

    val privateStocksCostBasis = playerState.privateStockPortfolio.sumOf { owned ->
        val liveStock = stockList.find { it.ticker == owned.ticker }
        val livePrice = liveStock?.currentPrice ?: owned.averagePrice
        val safeAveragePrice = if (owned.averagePrice <= 0.0) livePrice else owned.averagePrice
        (owned.shares * safeAveragePrice).toLong()
    }

    val privateGains = maxOf(0L, privateStocksValue - privateStocksCostBasis)
    val wealthTaxAnnualEstimate = (privateGains * 0.01).toLong()
    
    // Total physical and liquid portfolio values
    val holdingValuation = businessValue
    val rawCashValue = playerState.cash + playerState.privateBalance
    val propertyValue = realEstateValue + housingValue
    val vehicleCollectionValue = collectionsValue + vehiclesValue + metalsValue
    val totalWealth = rawCashValue + stocksValue + cryptoValue + holdingValuation + propertyValue + vehicleCollectionValue + privateStocksValue

    // ----------------------------------------------------
    // IN-GAME ESTIMATED MONTHLY INCOME REMUNERATION
    // ----------------------------------------------------
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

    val monthlyGaji = (megaHoldingMonthlyProfit * (playerState.currentCeoSalaryPercent / 100.0)).toLong()
    val monthlyPerks = (businessValue * 0.000005).toLong()
    val grossMonthlyIncomeComp = monthlyGaji + monthlyPerks
    
    // Progressive Tax estimate
    val estProgressiveTax = viewModel.calculateProgressiveTax(grossMonthlyIncomeComp, playerState.privateTaxServiceLevel)
    val effectiveTaxRate = if (grossMonthlyIncomeComp > 0) {
        (estProgressiveTax.toDouble() / grossMonthlyIncomeComp.toDouble() * 100.0).toInt()
    } else {
        0
    }

    Scaffold(
        containerColor = bgDark,
        topBar = {
            TopAppBar(
                title = { Text("⚖️ Auditor & Tax Department", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF11141A))
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Header (The Patriot Card)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, gold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10141D))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "KONTRIBUSI PAJAK PRIBADI & KORPORAT",
                            color = gold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pajak Pribadi CEO", color = textGray, fontSize = 11.sp)
                                Text(
                                    text = formatCurrencyRingkas(playerState.personalTaxPaid.toDouble(), false),
                                    color = neonGreen,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pajak Mega Holding", color = textGray, fontSize = 11.sp)
                                Text(
                                    text = formatCurrencyRingkas(playerState.corporateTaxPaid.toDouble(), false),
                                    color = gold,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "TOTAL PAJAK DISUMBANGKAN: " + formatCurrencyRingkas(playerState.totalTaxPaid.toDouble(), false),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pemisahan ketat antara Pajak Penghasilan Individu (CEO) vs Pajak Penghasilan Badan (PT).",
                            color = textGray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Card 1: Rincian PPh 21 (Gaji & Fasilitas)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardDark)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "💼 Pajak Penghasilan Pribadi (PPh 21 Progresif)",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PPh 21 progresif dipotong setiap bulan dari total Gaji CEO dan Natura (Perks) yang diterima.",
                            color = textGray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimasi Gaji CEO Bulanan", color = textGray, fontSize = 13.sp)
                            Text(formatCurrencyRingkas(monthlyGaji.toDouble(), false), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimasi Tunjangan Natura (Perks)", color = textGray, fontSize = 13.sp)
                            Text(formatCurrencyRingkas(monthlyPerks.toDouble(), false), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Penghasilan Eksekutif kotor", color = textGray, fontSize = 13.sp)
                            Text(formatCurrencyRingkas(grossMonthlyIncomeComp.toDouble(), false), color = gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Effective Tax Rate (Lapisan Progresif)", color = textGray, fontSize = 13.sp)
                            Text("$effectiveTaxRate%", color = warningYellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimasi Pajak Dipotong Per Bulan", color = textGray, fontSize = 13.sp)
                            Text("- " + formatCurrencyRingkas(estProgressiveTax.toDouble(), false), color = lightRed, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        if (privateStocksValue > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "⚖️ Pajak Kekayaan Saham Pribadi (Wealth Tax)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Nilai Portofolio Pribadi", color = textGray, fontSize = 13.sp)
                                Text(formatCurrencyRingkas(privateStocksValue.toDouble(), false), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimasi Akumulasi Untung Saham (Gains)", color = textGray, fontSize = 13.sp)
                                Text(formatCurrencyRingkas(privateGains.toDouble(), false), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pajak Kekayaan Pribadi SFO (1% Gains/thn)", color = textGray, fontSize = 13.sp)
                                Text(formatCurrencyRingkas(wealthTaxAnnualEstimate.toDouble(), false), color = lightRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Card 2: Kebijakan Pajak Dividen
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardDark)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "📈 Kebijakan Pajak Dividen (PPh Final)",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Pajak Dividen Final: 10%",
                                    color = gold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Sesuai regulasi domestik, dividen yang ditarik dari cadangan Mega Holding ke saku pribadi Anda dipotong PPh Final 10% secara langsung oleh lembaga kustodian internal.",
                                    color = textGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // Card 3: Pelaporan SPT Tahunan (Interactive)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardDark)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "📋 Pelaporan SPT Tahunan & Audit Aset",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kewajiban tahunan menutup buku dengan melaporkan seluruh rincian kekayaan Anda. Keterlambatan pelaporan akan dikenakan denda administrasi secara mutlak.",
                            color = textGray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "RINCIAN ASET YANG DILAPORKAN:",
                            color = gold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📈 Ekuitas Mega Holding & Subs",
                                    color = textGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatCurrencyRingkas(holdingValuation.toDouble(), false),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💵 Cash (Holding + Balance Pribadi)",
                                    color = textGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatCurrencyRingkas(rawCashValue.toDouble(), false),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏠 Portofolio Properti & Rumah",
                                    color = textGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatCurrencyRingkas(propertyValue.toDouble(), false),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏎️ Koleksi, Logam Mulia, Mobil & Pesawat",
                                    color = textGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatCurrencyRingkas(vehicleCollectionValue.toDouble(), false),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📊 Portofolio Saham Pribadi",
                                    color = textGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatCurrencyRingkas(privateStocksValue.toDouble(), false),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            Divider(color = Color.White.copy(alpha = 0.08f))
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Valuasi Terlapor",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = formatCurrencyRingkas(totalWealth.toDouble(), false),
                                    color = neonGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (playerState.privateTaxServiceLevel > 0) {
                            val managerName = if (playerState.privateTaxServiceLevel == 1) "Big Four" else "Tax Lawyer & SFO"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F2613), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selesai", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "✅ Pelaporan SPT tahun ini dikelola otomatis oleh $managerName. Anda bebas dari denda.",
                                    color = Color(0xFF81C784),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            if (playerState.isSptReportedThisYear) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F2613), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Selesai", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "✅ SPT Tahun Ini Telah Dilaporkan",
                                        color = Color(0xFF81C784),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {},
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.White.copy(alpha = 0.08f))
                                ) {
                                    Text("Sudah Dilaporkan", fontWeight = FontWeight.SemiBold)
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF2C1E11), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Peringatan", tint = warningYellow, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "⚠️ Batas Waktu Pelaporan SPT Berakhir Tahun Ini!",
                                        color = Color(0xFFFFB74D),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                if (playerState.consecutiveUnreportedSpt > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Akumulasi keterlambatan saat ini: ${playerState.consecutiveUnreportedSpt} tahun denda.",
                                        color = lightRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        val msg = viewModel.reportSptTahunan()
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                                ) {
                                    Text("Lapor SPT Tahunan & Audit Aset", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Section: Layanan Kepatuhan Pajak Pribadi & Wealth Management
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, gold.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardDark)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "🛡️ Layanan Kepatuhan Pajak Pribadi (Tax Management)",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gunakan bantuan firma penasehat pajak elite untuk lapor SPT otomatis, memangkas birokrasi, dan menata struktur perpajakan pribadi Anda secara legal.",
                            color = textGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        val services = listOf(
                            Triple(0, "❌ Lapor Mandiri (Gratis)", "Anda harus melapor SPT secara manual di akhir tahun. Risiko denda jika lupa melapor sebelum tahun berganti."),
                            Triple(1, "🏢 Firma Akuntansi Top (Big Four) - $50,000 / thn", "Audit otomatis oleh Deloitte, PwC, EY, atau KPMG. Melaporkan SPT otomatis tanpa denda setiap akhir tahun."),
                            Triple(2, "🕴️ Tax Lawyer & SFO - $500,000 / thn", "Kantor keluarga (Family Office) merestrukturisasi aset Anda. PPh 21 maksimal dipangkas menjadi 25%, PPh Final Dividen serta Tantiem turun jadi 5%, lapor SPT otomatis.")
                        )

                        services.forEach { (level, title, desc) ->
                            val isSelected = playerState.privateTaxServiceLevel == level
                            val cardBg = if (isSelected) Color(0xFF1B2332) else Color(0xFF161616)
                            val borderColor = if (isSelected) gold else Color.White.copy(alpha = 0.05f)

                            Card(
                                onClick = { viewModel.setPrivateTaxServiceLevel(level) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.setPrivateTaxServiceLevel(level) },
                                        colors = RadioButtonDefaults.colors(selectedColor = gold, unselectedColor = textGray)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = title,
                                            color = if (isSelected) gold else Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = desc,
                                            color = textGray,
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
