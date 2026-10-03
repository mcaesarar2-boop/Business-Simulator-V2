package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.data.AVIATION_AIRCRAFT_CATALOG
import com.example.data.DummyAircraft
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AviationCatalogScreen(navController: NavHostController, viewModel: GameViewModel, businessId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()
    val context = LocalContext.current

    val currentBusiness = remember(playerState.ownedBusinesses, playerState.holdingCompanies, businessId) {
        playerState.ownedBusinesses.find { it.instanceId == businessId }
            ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == businessId }
    }
    val businessCash = currentBusiness?.companyCash ?: 0.0

    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedAircraft by remember { mutableStateOf<DummyAircraft?>(null) }
    var quantity by remember { mutableStateOf(1) }

    LaunchedEffect(selectedAircraft) {
        quantity = 1
    }

    val filteredAircrafts = remember(selectedCategory) {
        if (selectedCategory == "ALL") {
            AVIATION_AIRCRAFT_CATALOG
        } else {
            AVIATION_AIRCRAFT_CATALOG.filter { it.type == selectedCategory }
        }
    }

    Scaffold(
        containerColor = Color(0xFF090A0E),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Global Aircraft Market",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Acquire New or Leased Aircraft",
                            fontSize = 11.sp,
                            color = Color(0xFF8A9099)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xCC11121A),
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0C0E14), Color(0xFF141724))
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Player Cash Info Widget
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1F222533)),
                    border = BorderStroke(1.dp, Color(0x22FFFFFF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("KAS DIVISI MASKAPAI", fontSize = 10.sp, color = Color(0xFF90A4AE), fontWeight = FontWeight.SemiBold)
                            Text(
                                text = formatCurrencyRingkas(businessCash, useShortFormat),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFD700).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("PREMIUM DEALER", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Filter Bar (Horizontal Scroll Pill Buttons)
                val categories = listOf("ALL", "PROPELLER", "REGIONAL_JET", "NARROW_BODY", "WIDE_BODY", "HELICOPTER")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSel = selectedCategory == cat
                        val label = when (cat) {
                            "ALL" -> "Semua"
                            "PROPELLER" -> "Propeller"
                            "REGIONAL_JET" -> "Regional Jet"
                            "NARROW_BODY" -> "Narrow Body"
                            "WIDE_BODY" -> "Wide Body"
                            "HELICOPTER" -> "Helicopter"
                            else -> cat
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSel) Color(0xFFFFD700) else Color(0x1AFFFFFF))
                                .border(1.dp, if (isSel) Color(0xFFFFD700) else Color(0x14FFFFFF), RoundedCornerShape(16.dp))
                                .clickable { 
                                    selectedCategory = cat
                                    selectedAircraft = null // reset selection when category changes
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.Black else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Scrollable Aircraft Catalog list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = if (selectedAircraft != null) 120.dp else 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredAircrafts) { p ->
                        val isSelected = selectedAircraft?.id == p.id

                        val emojiSymbol = when (p.type) {
                            "PROPELLER" -> "✈️"
                            "REGIONAL_JET" -> "🛩️"
                            "NARROW_BODY" -> "✈️"
                            "WIDE_BODY" -> "🚀"
                            "HELICOPTER" -> "🚁"
                            else -> "✈️"
                        }

                        val themeBadgeColor = when (p.type) {
                            "PROPELLER" -> Color(0xFF66BB6A)
                            "REGIONAL_JET" -> Color(0xFF42A5F5)
                            "NARROW_BODY" -> Color(0xFFFFA726)
                            "WIDE_BODY" -> Color(0xFFAB47BC)
                            "HELICOPTER" -> Color(0xFF26A69A)
                            else -> Color.Gray
                        }

                        Card(
                            onClick = { selectedAircraft = p },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0x2EFFFFFF) else Color(0x0EFFFFFF)
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFFFFD700) else Color(0x12FFFFFF)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Specs and Image Column (Left)
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = emojiSymbol,
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = p.model,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(themeBadgeColor.copy(alpha = 0.15f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = p.type,
                                                color = themeBadgeColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = "Pax: ${p.maxPax} | Jarak: ${p.range}",
                                            color = Color(0xFFB0BEC5),
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = if (p.deliveryTime > 0) "⏳ Waktu Inden: ${p.deliveryTime} bulan" else "⚡ Siap Kirim Instan (0 bln)",
                                        color = if (p.deliveryTime > 0) Color(0xFFFFB300) else Color(0xFF4CAF50),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Money Prices (Right)
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.weight(0.8f)
                                ) {
                                    Text(
                                        text = formatCurrencyRingkas(p.price, useShortFormat),
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Tunai / Cash",
                                        color = Color(0xFF78909C),
                                        fontSize = 10.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${formatCurrencyRingkas(p.leasePrice, useShortFormat)}/bln",
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "Sewa / Lease",
                                        color = Color(0xFF78909C),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Elegant Sticky Bottom Action Bar / Checkout Bar with Glassmorphic design
            if (selectedAircraft != null) {
                val p = selectedAircraft!!
                val totalCashPrice = p.price * quantity
                val totalLeasePrice = p.leasePrice * quantity

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0x00000000), Color(0xFA0F111A), Color(0xFF0F111A))
                            )
                        )
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xE61A1D2B)),
                        border = BorderStroke(1.dp, Color(0x3DFFFFFF)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Konfirmasi Pemesanan: ${p.model}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )

                            // Quantity Selector
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Jumlah Unit Pesawat",
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (quantity > 1) quantity-- },
                                        modifier = Modifier.size(28.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    ) {
                                        Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Text(
                                        text = "$quantity",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    IconButton(
                                        onClick = { quantity++ },
                                        modifier = Modifier.size(28.dp).background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    ) {
                                        Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }

                            // Total Cost Display
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estimasi Total Pembelian Tunai",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = formatCurrencyRingkas(totalCashPrice, false),
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Lease option
                                Button(
                                    onClick = {
                                        if (businessCash < totalLeasePrice) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Kas Divisi Tidak Cukup. Silakan suntik dana dari Dashboard Maskapai terlebih dahulu.",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            viewModel.buyComplexAircraft(
                                                businessId = businessId,
                                                modelId = p.id,
                                                cost = p.price,
                                                deliveryTime = p.deliveryTime,
                                                isLeased = true,
                                                leasePrice = p.leasePrice,
                                                quantity = quantity
                                            )
                                            navController.popBackStack()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2E7D32)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Sewa Bulanan",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            "${formatCurrencyRingkas(totalLeasePrice, useShortFormat)}/bln",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                // Buy Cash option
                                Button(
                                    onClick = {
                                        if (businessCash < totalCashPrice) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Kas Divisi Tidak Cukup. Silakan suntik dana dari Dashboard Maskapai terlebih dahulu.",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            viewModel.buyComplexAircraft(
                                                businessId = businessId,
                                                modelId = p.id,
                                                cost = p.price,
                                                deliveryTime = p.deliveryTime,
                                                isLeased = false,
                                                leasePrice = 0L,
                                                quantity = quantity
                                            )
                                            navController.popBackStack()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1976D2)
                                    ),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "Beli Tunai",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            formatCurrencyRingkas(totalCashPrice, useShortFormat),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.White.copy(alpha = 0.8f)
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
