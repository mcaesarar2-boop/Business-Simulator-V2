package com.example.ui

import com.example.viewmodel.GameViewModel
import com.example.data.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxLegalScreen(navController: NavController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val report = playerState.taxLegalReport
    
    val bgDark = Color(0xFF0F141D)
    val cardDark = Color(0xFF19202C)
    val cardDarkSecondary = Color(0xFF222B3A)
    val gold = Color(0xFFFFD700)
    val neonGreen = Color(0xFF00FF87)
    val softCyan = Color(0xFF4EEBF7)
    val textGray = Color(0xFF9FB2C6)
    val errorColor = Color(0xFFFF4D4D)
    val warningGold = Color(0xFFFFB300)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val format = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    
    // Monthly Corporate Net Profit before taxes
    var megaHoldingMonthlyProfit = playerState.ownedBusinesses.sumOf {
        val ct = com.example.data.getCatalogItem(it.catalogId, playerState)
        if (ct != null) getBusinessStats(it, ct, playerState).let { (rev, mnt) -> rev - mnt } else 0L
    } + playerState.holdingCompanies.sumOf { h ->
        h.subsidiaries.sumOf { sub ->
            val ct = com.example.data.getCatalogItem(sub.catalogId, playerState)
            if (ct != null) getBusinessStats(sub, ct, playerState).let { (rev, mnt) -> rev - mnt } else 0L
        }
    }
    if (megaHoldingMonthlyProfit < 0) megaHoldingMonthlyProfit = 0L
    
    val activeCorpTaxRate = if (report.isTaxHavenActive) 0.05 else 0.20
    val estMonthlyCorpTax = (megaHoldingMonthlyProfit * activeCorpTaxRate).toLong()
    
    Scaffold(
        containerColor = bgDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Holding Tax & Legal", color = gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Kepatuhan Pajak Badan & Manajemen Risiko Hukum", color = textGray, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            
            // Treasury & Compliance Status Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, gold.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = gold.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.CorporateFare, contentDescription = null, tint = gold, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Kas Korporasi Holding", color = textGray, fontSize = 11.sp)
                                    Text(
                                        formatCurrencyRingkas(playerState.cash.toDouble(), true),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                }
                            }
                            
                            // Status Chip
                            val (statusText, statusBg, statusColor) = when {
                                report.frozenBusinessId != null -> Triple("DIBEKUKAN", errorColor.copy(alpha = 0.2f), errorColor)
                                report.unpaidTaxes > 0 -> Triple("ADA TUNGGAKAN", warningGold.copy(alpha = 0.2f), warningGold)
                                report.hasNotary -> Triple("AUTO-PATUH", neonGreen.copy(alpha = 0.2f), neonGreen)
                                else -> Triple("PATUH (MANUAL)", softCyan.copy(alpha = 0.2f), softCyan)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusBg,
                                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    statusText,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Tax Section (Corporate Tax)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = gold, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kepatuhan PPh Badan", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Pajak penghasilan korporasi atas laba entitas usaha Mega Holding (terpisah dari PPh 21 pribadi CEO).",
                            color = textGray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Metric 1: Total Pajak Badan Dibayar
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = cardDarkSecondary,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total PPh Badan Disetor", color = textGray, fontSize = 12.sp)
                                Text(formatCurrencyRingkas(playerState.corporateTaxPaid.toDouble(), true), color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        
                        // Metric 2: Estimasi Laba Bulanan
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = cardDarkSecondary,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Laba Bersih Holding (Bulanan)", color = textGray, fontSize = 12.sp)
                                Text(formatCurrencyRingkas(megaHoldingMonthlyProfit.toDouble(), true), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                        
                        // Metric 3: Tarif Aktif & Estimasi Pajak
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = cardDarkSecondary,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Tarif Pajak Aktif", color = textGray, fontSize = 12.sp)
                                    Text(
                                        if (report.isTaxHavenActive) "5% (Tax Haven Offshore)" else "20% (Domestik Standard)",
                                        color = if (report.isTaxHavenActive) gold else softCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Estimasi Pajak/Bulan", color = textGray, fontSize = 12.sp)
                                    Text(
                                        formatCurrencyRingkas(estMonthlyCorpTax.toDouble(), true),
                                        color = errorColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        
                        // Unpaid Taxes Alert & Pay Button
                        if (report.unpaidTaxes > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = errorColor.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, errorColor.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = errorColor, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Tunggakan / Denda Pajak", color = errorColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        Text(formatCurrencyRingkas(report.unpaidTaxes.toDouble(), true), color = errorColor, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            val ok = viewModel.payTaxesManually(report.unpaidTaxes)
                                            scope.launch {
                                                if (ok) snackbarHostState.showSnackbar("✅ Berhasil melunasi seluruh tunggakan pajak!")
                                                else snackbarHostState.showSnackbar("❌ Kas korporasi tidak mencukupi untuk bayar denda.")
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = playerState.cash >= report.unpaidTaxes
                                    ) {
                                        Text("Lunasi Tunggakan Pajak Sekarang", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                        
                        // Frozen subsidiary alert
                        if (report.frozenBusinessId != null) {
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                color = Color(0xFF441010),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, errorColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = errorColor, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("OPERASIONAL ANAK PERUSAHAAN DIBEKUKAN!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Sebagian operasional anak usaha disita oleh Dirjen Pajak karena akumulasi tunggakan. Lunasi denda di atas untuk memulihkan arus kas bisnis Anda.",
                                        color = Color(0xFFFFB4B4),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Tax Haven Offshore Switch Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (report.isTaxHavenActive) gold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (report.isTaxHavenActive) gold.copy(alpha = 0.15f) else cardDarkSecondary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = if (report.isTaxHavenActive) gold else textGray, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Offshore Tax Haven", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                if (report.isTaxHavenActive) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = gold.copy(alpha = 0.2f)) {
                                        Text("5% PAJAK", color = gold, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Tarif pajak dipotong menjadi 5%, namun ada risiko 5% tiap bulan terkena Audit DJP Khusus dengan denda 5x lipat.",
                                color = textGray,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = report.isTaxHavenActive,
                            onCheckedChange = { viewModel.toggleTaxHaven() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = gold,
                                checkedTrackColor = gold.copy(alpha = 0.4f),
                                uncheckedThumbColor = textGray,
                                uncheckedTrackColor = cardDarkSecondary
                            )
                        )
                    }
                }
            }
            
            // Notary Auto-Pay Switch Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (report.hasNotary) neonGreen.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (report.hasNotary) neonGreen.copy(alpha = 0.15f) else cardDarkSecondary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = if (report.hasNotary) neonGreen else textGray, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Jasa Notaris & Auto-Pay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                if (report.hasNotary) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = neonGreen.copy(alpha = 0.2f)) {
                                        Text("AUTO-AKTIF", color = neonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Notaris otomatis membayar pajak badan dari kas korporasi tiap bulan ($1,000/bln retainer), menjaga reputasi holding 100% patuh bebas sita.",
                                color = textGray,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = report.hasNotary,
                            onCheckedChange = { viewModel.toggleNotary(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = neonGreen,
                                checkedTrackColor = neonGreen.copy(alpha = 0.4f),
                                uncheckedThumbColor = textGray,
                                uncheckedTrackColor = cardDarkSecondary
                            )
                        )
                    }
                }
            }
            
            // Legal Issues Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = gold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sengketa & Gugatan Hukum", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    if (report.activeLawsuits.isNotEmpty()) {
                        Surface(shape = RoundedCornerShape(6.dp), color = errorColor.copy(alpha = 0.2f)) {
                            Text("${report.activeLawsuits.size} KASUS AKTIF", color = errorColor, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }
            
            if (report.activeLawsuits.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardDark.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = neonGreen, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Tidak Ada Gugatan Hukum Aktif", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Seluruh operasional bisnis Anda saat ini berjalan aman dan terlindungi dari somasi hukum.", color = textGray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                items(report.activeLawsuits) { lawsuit ->
                    val scale = lawsuit.scaleFactor
                    val topLayerCost = (scale * 0.4).toLong()
                    val internCost = (scale * 0.1).toLong()
                    val settlementCost = (scale * 1.5).toLong()
                    
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardDark),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, errorColor.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = errorColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(lawsuit.title, color = errorColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(lawsuit.description, color = textGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(14.dp))
                            
                            Text("Pilih Strategi Penyelesaian Hukum:", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 1. Premium Law Firm Button (95% win)
                            Button(
                                onClick = {
                                    val msg = viewModel.resolveLawsuit(lawsuit.id, 2)
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                enabled = playerState.cash >= topLayerCost
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Firma Hukum Tier-1 (95% Menang)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(formatCurrencyRingkas(topLayerCost.toDouble(), true), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // 2. Intern Lawyer Button (40% win)
                            Button(
                                onClick = {
                                    val msg = viewModel.resolveLawsuit(lawsuit.id, 1)
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = softCyan, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                enabled = playerState.cash >= internCost
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Pengacara Magang (40% Menang)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(formatCurrencyRingkas(internCost.toDouble(), true), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            // 3. Settle / Damai Out of Court Button (100%)
                            OutlinedButton(
                                onClick = {
                                    val msg = viewModel.resolveLawsuit(lawsuit.id, 0)
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, textGray.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                enabled = playerState.cash >= settlementCost
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Mediasi / Damai Luar Pengadilan", fontSize = 12.sp)
                                    Text(formatCurrencyRingkas(settlementCost.toDouble(), true), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
