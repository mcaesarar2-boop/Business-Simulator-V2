package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay

data class SpecialEventTender(
    val name: String,
    val category: String,
    val budget: Double,
    val fee: Double,
    val prestigeReward: Int,
    val requiredHq: String,
    val requiredRep: Double,
    val requiredPrestige: Int,
    val requirements: List<String>
)

val specialEventTenders = listOf(
    SpecialEventTender("Taylor Swift Tour", "Concert", 8000000.0, 2400000.0, 30, "NATIONAL", 85.0, 80, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance")),
    SpecialEventTender("Formula 1 Ceremony", "Exhibition", 5000000.0, 1500000.0, 25, "NATIONAL", 90.0, 75, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance")),
    SpecialEventTender("World Expo Opening", "International Summit", 15000000.0, 4500000.0, 45, "INTERNATIONAL", 92.0, 110, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance", "Helicopter")),
    SpecialEventTender("FIFA World Cup Ceremony", "Sports Event", 12000000.0, 3600000.0, 50, "INTERNATIONAL", 95.0, 120, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance", "Helicopter")),
    SpecialEventTender("Olympic Opening Ceremony", "Sports Event", 20000000.0, 7000000.0, 60, "INTERNATIONAL", 98.0, 150, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance", "Helicopter")),
    SpecialEventTender("New Year's Celebration", "Festival", 2000000.0, 600000.0, 15, "REGIONAL", 70.0, 45, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade"))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventOrganizerScreen(navController: NavHostController, viewModel: GameViewModel, instanceId: String) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsState()
    val ownedData = playerState.ownedBusinesses.find { it.instanceId == instanceId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
        
    if (ownedData == null) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("Bisnis tidak ditemukan.", color = Color.White)
            Button(onClick = { navController.popBackStack() }) { Text("Kembali") }
        }
        return
    }

    var activeSubScreen by remember { mutableStateOf<String?>(null) }

    if (activeSubScreen == "HQ") {
        HQEventScreen(
            instanceId = instanceId,
            viewModel = viewModel,
            onBack = { activeSubScreen = null }
        )
        return
    }

    if (activeSubScreen == "ASSETS") {
        AssetsEventScreen(
            instanceId = instanceId,
            viewModel = viewModel,
            onBack = { activeSubScreen = null }
        )
        return
    }

    var activeTab by remember { mutableStateOf(0) } // 0: Dashboard & Kantor, 1: Tender & Tawaran, 2: Proyek Aktif
    var showTransferDialog by remember { mutableStateOf(false) }
    var transferType by remember { mutableStateOf("DEPOSIT") } // "DEPOSIT" atau "WITHDRAW"
    var transferAmountInput by remember { mutableStateOf("") }
    
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showLiquidationDialog by remember { mutableStateOf(false) }
    var liquidationConfirmInput by remember { mutableStateOf("") }
    
    var selectedReviewEvent by remember { mutableStateOf<com.example.data.EventProject?>(null) }

    // Active Events ticker
    val activeEvents = ownedData.activeEvents
    LaunchedEffect(activeEvents) {
        while (true) {
            val now = System.currentTimeMillis()
            var changed = false
            for (ev in activeEvents) {
                if (ev.phase == "EXECUTING" && now >= ev.executionEndTime) {
                    viewModel.calculateEventResults(instanceId, ev.id)
                    changed = true
                }
            }
            if (changed) break
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(ownedData.customName ?: "Mega Event Organizer", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("HQ: ${getHqDisplayName(ownedData.eoCompanyHqLevel)}", fontSize = 12.sp, color = Color.LightGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali") 
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSettingsMenu = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Pengaturan Bisnis", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = showSettingsMenu,
                            onDismissRequest = { showSettingsMenu = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.DeleteForever,
                                            contentDescription = null,
                                            tint = Color(0xFFF43F5E),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Likuidasi Bisnis (Jual)", color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    showSettingsMenu = false
                                    liquidationConfirmInput = ""
                                    showLiquidationDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A), 
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            
            // Internal Cash Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Kas Internal Perusahaan", fontSize = 12.sp, color = Color.Gray)
                        Text("$${String.format("%,.0f", ownedData.companyCash)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { transferType = "DEPOSIT"; transferAmountInput = ""; showTransferDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Suntik", fontSize = 12.sp) }
                        Button(
                            onClick = { transferType = "WITHDRAW"; transferAmountInput = ""; showTransferDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("Tarik", fontSize = 12.sp) }
                    }
                }
            }

            // Material 3 Custom Tab Row
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = Color(0xFF3B82F6)
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("🏢 Kantor", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("📋 Tender", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("⚡ Proyek (${ownedData.activeEvents.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            // Tab Contents
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (activeTab) {
                    0 -> DashboardTab(
                        ownedData = ownedData,
                        viewModel = viewModel,
                        instanceId = instanceId,
                        context = context,
                        onOpenHq = { activeSubScreen = "HQ" },
                        onOpenAssets = { activeSubScreen = "ASSETS" }
                    )
                    1 -> TenderTab(ownedData, viewModel, instanceId, context)
                    2 -> ActiveProjectsTab(ownedData, viewModel, instanceId, context, onShowReview = { selectedReviewEvent = it })
                }
            }
        }
    }

    // Review Result Dialog
    if (selectedReviewEvent != null) {
        val ev = selectedReviewEvent!!
        AlertDialog(
            onDismissRequest = { /* force action completion */ },
            containerColor = Color(0xFF1E293B),
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Review Hasil Acara", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(ev.name, fontSize = 14.sp, color = Color.LightGray)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    // Star rating display
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val fullStars = ev.resultRating.toInt()
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Star",
                                tint = if (i <= fullStars) Color(0xFFFBBF24) else Color.DarkGray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text("${String.format("%.1f", ev.resultRating)} / 5.0 Stars", fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Quality Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Event Quality", fontSize = 12.sp, color = Color.Gray)
                            Text("${String.format("%.1f", ev.quality)}%", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (ev.quality >= 75) Color(0xFF10B981) else Color(0xFFF43F5E))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "\"${ev.resultReviewText}\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Divider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Payout details
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pengembalian Modal:", fontSize = 12.sp, color = Color.LightGray)
                        Text("$${String.format("%,.0f", ev.totalBudget)}", fontSize = 12.sp, color = Color.White)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Komisi EO + Bonus:", fontSize = 12.sp, color = Color.LightGray)
                        Text("$${String.format("%,.0f", ev.finalProfit)}", fontSize = 12.sp, color = Color(0xFF10B981))
                    }
                    
                    val totalCollect = ev.totalBudget + ev.finalProfit
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total Dana Diterima:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("$${String.format("%,.0f", totalCollect)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val err = viewModel.collectEventEarnings(instanceId, ev.id)
                        if (err != null) {
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Dana ditransfer ke Kas Internal!", Toast.LENGTH_SHORT).show()
                        }
                        selectedReviewEvent = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Terima Pembayaran", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Suntik/Tarik Dana Dialog
    if (showTransferDialog) {
        val parentHolding = playerState.holdingCompanies.find { h -> h.subsidiaries.any { it.instanceId == instanceId } }
        val parentName = parentHolding?.name
        val titleText = if (transferType == "DEPOSIT") "Suntik Dana Modal" else "Tarik Dana"

        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text(titleText, color = Color.White) },
            text = {
                Column {
                    val parentCashDesc = if (parentHolding != null) String.format("%,.0f", parentHolding.holdingCash) else String.format("%,d", playerState.cash)

                    if (parentName != null) {
                        Text("Sumber/Tujuan Dana: Kas Induk ($parentName)", color = Color.LightGray, fontSize = 13.sp)
                        Text("Kas Induk ($parentName): $$parentCashDesc", color = Color.White, fontSize = 14.sp)
                    } else {
                        Text("Sumber/Tujuan Dana: Global Balance", color = Color.LightGray, fontSize = 13.sp)
                        Text("Global Balance: $$parentCashDesc", color = Color.White, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Kas Internal Bisnis: $${String.format("%,.0f", ownedData.companyCash)}", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = transferAmountInput,
                        onValueChange = { transferAmountInput = it },
                        label = { Text("Jumlah ($)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amountStr = transferAmountInput.replace("[^0-9]".toRegex(), "")
                    val amountLong = amountStr.toLongOrNull() ?: 0L
                    if (amountLong > 0) {
                        if (transferType == "DEPOSIT") {
                            viewModel.injectCapitalToBusiness(instanceId, amountLong)
                        } else {
                            viewModel.withdrawCapitalFromBusiness(instanceId, amountLong)
                        }
                    }
                    showTransferDialog = false
                }) { Text("Transfer", fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6)) }
            },
            dismissButton = { TextButton(onClick = { showTransferDialog = false }) { Text("Batal", color = Color.Gray) } }
        )
    }

    // Modal Dialog: Danger Zone Likuidasi Bisnis
    if (showLiquidationDialog) {
        val standardAssetsVal = (ownedData.eoOwnedAssets ?: emptyMap()).entries.sumOf { (assetName, qty) ->
            (viewModel.getAssetPurchasePrice(assetName) * qty).toLong()
        }
        val customAssetsVal = (ownedData.eoCustomAssets ?: emptyList()).sumOf {
            (it.quantity * it.priceUnit).toLong()
        }
        val totalAssetsInWarehouse = standardAssetsVal + customAssetsVal
        val prestigeBonusVal = ownedData.eoPrestige * 10_000L
        val internalCashVal = ownedData.companyCash.toLong()
        val totalValuation = viewModel.calculateEventCompanyValuation(ownedData)

        AlertDialog(
            onDismissRequest = { showLiquidationDialog = false },
            containerColor = Color(0xFF1E1E2E),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF43F5E).copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Peringatan", tint = Color(0xFFF43F5E), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Danger Zone: Likuidasi Bisnis", color = Color(0xFFF43F5E), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Unit Bisnis: ${ownedData.customName ?: "Mega Event Organizer"}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Valuation Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("TOTAL VALUASI BISNIS ANDA", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$${String.format("%,d", totalValuation)}",
                                color = Color(0xFF10B981),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Divider(color = Color.DarkGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Kas Internal Bisnis:", color = Color.LightGray, fontSize = 11.sp)
                                Text("$${String.format("%,d", internalCashVal)}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Nilai Aset di Gudang:", color = Color.LightGray, fontSize = 11.sp)
                                Text("$${String.format("%,d", totalAssetsInWarehouse)}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Prestige (${ownedData.eoPrestige} Pt x $10k):", color = Color.LightGray, fontSize = 11.sp)
                                Text("$${String.format("%,d", prestigeBonusVal)}", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Warning Text
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF43F5E).copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ Tindakan ini permanen. Seluruh aset, proyek, dan tim akan dihapus. Uang valuasi akan ditransfer ke Saldo Perusahaan Utama.",
                            color = Color(0xFFFF8A9E),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Ketik \"JUAL\" untuk mengonfirmasi:",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = liquidationConfirmInput,
                        onValueChange = { liquidationConfirmInput = it },
                        placeholder = { Text("Ketik JUAL di sini", color = Color.DarkGray, fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF43F5E),
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val isConfirmationValid = liquidationConfirmInput.trim() == "JUAL"
                Button(
                    onClick = {
                        if (isConfirmationValid) {
                            viewModel.liquidateBusiness(instanceId)
                            showLiquidationDialog = false
                            Toast.makeText(context, "Bisnis berhasil dilikuidasi!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    },
                    enabled = isConfirmationValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF43F5E),
                        disabledContainerColor = Color(0xFF334155),
                        contentColor = Color.White,
                        disabledContentColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ya, Jual Bisnis", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLiquidationDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
}

// ==========================================
// TAB 1: DASHBOARD & KANTOR
// ==========================================
@Composable
fun DashboardTab(
    ownedData: com.example.data.OwnedBusiness,
    viewModel: GameViewModel,
    instanceId: String,
    context: android.content.Context,
    onOpenHq: () -> Unit,
    onOpenAssets: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Reputation & Prestige Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Reputasi Perusahaan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${String.format("%.1f", ownedData.eoReputation)} / 100.0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = (ownedData.eoReputation / 100f).toFloat().coerceIn(0f, 1f),
                        color = Color(0xFF3B82F6),
                        trackColor = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Prestige Level (CV)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Makin tinggi, tender makin raksasa", fontSize = 11.sp, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = "Prestige", tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${ownedData.eoPrestige} Pt", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                        }
                    }
                    
                    // Completed special events list (Prestige CV)
                    if (ownedData.eoCompletedSpecialEvents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("CV Event Akbar / Portofolio:", fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            ownedData.eoCompletedSpecialEvents.forEach { evName ->
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("🏆 $evName", fontSize = 11.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // launcher 1: HQ & Divisions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f)),
                modifier = Modifier.clickable { onOpenHq() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Business, contentDescription = "HQ", tint = Color(0xFF3B82F6), modifier = Modifier.size(30.dp))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kelola Markas & Divisi (HQ)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        val hqLevel = ownedData.eoCompanyHqLevel ?: "HOUSE"
                        val divsHired = ownedData.eoDivisions?.size ?: 0
                        val maxDivs = when (hqLevel) {
                            "HOUSE" -> 3
                            "OFFICE" -> 5
                            "REGIONAL" -> 7
                            else -> 9
                        }
                        Text(
                            "Level: ${viewModel.getHqDisplayName(hqLevel)} • Divisi: $divsHired / $maxDivs",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Icon(Icons.Filled.ArrowForwardIos, contentDescription = "Go", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }
            }
        }

        // launcher 2: Permanent Assets & Warehouse
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                modifier = Modifier.clickable { onOpenAssets() }
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                    .size(52.dp)
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Inventory, contentDescription = "Assets", tint = Color(0xFF10B981), modifier = Modifier.size(30.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gudang & Aset Permanen", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            val standardCount = (ownedData.eoOwnedAssets ?: emptyMap()).values.sum()
                            val customCount = (ownedData.eoCustomAssets ?: emptyList()).sumOf { it.quantity }
                            val totalCount = standardCount + customCount
                            val capacity = when (ownedData.eoCompanyHqLevel ?: "HOUSE") {
                                "HOUSE" -> 5
                                "OFFICE" -> 15
                                "REGIONAL" -> 40
                                "NATIONAL" -> 100
                                else -> 9999
                            }
                            Text(
                                "Total Aset: $totalCount / $capacity unit • Kustom: ${ownedData.eoCustomAssets?.size ?: 0} jenis",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Icon(Icons.Filled.ArrowForwardIos, contentDescription = "Go", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }

                    // Warehouse status bar
                    Spacer(modifier = Modifier.height(12.dp))
                    val totalCount = (ownedData.eoOwnedAssets ?: emptyMap()).values.sum() + (ownedData.eoCustomAssets ?: emptyList()).sumOf { it.quantity }
                    val capacity = when (ownedData.eoCompanyHqLevel ?: "HOUSE") {
                        "HOUSE" -> 5
                        "OFFICE" -> 15
                        "REGIONAL" -> 40
                        "NATIONAL" -> 100
                        else -> 9999
                    }
                    LinearProgressIndicator(
                        progress = (totalCount.toFloat() / capacity.toFloat()).coerceIn(0f, 1f),
                        color = if (totalCount >= capacity) Color(0xFFF43F5E) else Color(0xFF10B981),
                        trackColor = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

// ==========================================
// TAB 2: TENDER & TAWARAN
// ==========================================
@Composable
fun TenderTab(ownedData: com.example.data.OwnedBusiness, viewModel: GameViewModel, instanceId: String, context: android.content.Context) {
    val clientRequests = ownedData.clientEventRequests

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Special Prestige Tenders Header
        item {
            Text("🏆 Portofolio & Tender Akbar (Prestige Event)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        items(specialEventTenders) { spec ->
            val hqLevel = ownedData.eoCompanyHqLevel ?: "HOUSE"
            val hasCompleted = ownedData.eoCompletedSpecialEvents.contains(spec.name)
            
            val levelRequiredValue = getHqLevelWeight(spec.requiredHq)
            val currentHqWeight = getHqLevelWeight(hqLevel)
            
            val isLocked = currentHqWeight < levelRequiredValue || ownedData.eoReputation < spec.requiredRep || ownedData.eoPrestige < spec.requiredPrestige - 15
            
            val isCurrentlyActive = ownedData.activeEvents.any { it.name == spec.name }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        hasCompleted -> Color(0x3310B981)
                        isLocked -> Color(0x33F43F5E)
                        else -> Color(0xFF1E293B)
                    }
                ),
                border = BorderStroke(1.dp, when {
                    hasCompleted -> Color(0xFF10B981)
                    isLocked -> Color.DarkGray
                    else -> Color(0xFFFBBF24)
                }),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(spec.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text("Kategori: ${spec.category} | Reward: +${spec.prestigeReward} Pt Prestige", color = Color(0xFFFBBF24), fontSize = 11.sp)
                        }
                        if (hasCompleted) {
                            Box(modifier = Modifier.background(Color(0xFF10B981), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)) {
                                Text("SELESAI", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (isLocked) {
                            Icon(Icons.Filled.Lock, contentDescription = "Terkunci", tint = Color(0xFFF43F5E), modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Budget Proyek: $${String.format("%,.0f", spec.budget)} | Komisi EO: $${String.format("%,.0f", spec.fee)}", fontSize = 12.sp, color = Color.LightGray)
                    
                    if (isLocked) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Syarat: HQ Minimal ${getHqDisplayName(spec.requiredHq)}, Reputasi ${spec.requiredRep}, Prestige ${spec.requiredPrestige} Pt",
                            color = Color(0xFFF43F5E),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (!hasCompleted) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (isCurrentlyActive) {
                                    Toast.makeText(context, "Event sedang direncanakan / berjalan!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val err = viewModel.startSpecialEvent(instanceId, spec.name, spec.category, spec.budget, spec.fee, spec.requirements, spec.prestigeReward)
                                    if (err != null) Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    else Toast.makeText(context, "Kontrak Tender Akbar Diambil!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24), contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            enabled = !isCurrentlyActive
                        ) {
                            Text(if (isCurrentlyActive) "Sedang Berjalan" else "Ajukan Tender Akbar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Standard Client Offers Header
        item {
            Text("📩 Tawaran Acara Masuk (Klien)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        if (clientRequests.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada tawaran. Tunggu pergantian bulan.", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(clientRequests) { req ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(req.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Tier: ", fontSize = 11.sp, color = Color.Gray)
                                    repeat(req.tier) {
                                        Icon(Icons.Filled.Star, contentDescription = "*", tint = Color(0xFFFBBF24), modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                            Button(
                                onClick = {
                                    val err = viewModel.acceptClientEvent(instanceId, req.id)
                                    if (err != null) Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    else Toast.makeText(context, "Tawaran diterima! Atur di Proyek Aktif.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Ambil", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pax: ${req.pax} | Cuaca: ${if (req.weather == "SUNNY") "🌞 Cerah" else "🌧 Hujan"}", fontSize = 11.sp, color = Color.LightGray)
                            Text("Budget: $${String.format("%,.0f", req.totalBudget)}", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: PROYEK AKTIF
// ==========================================
@Composable
fun ActiveProjectsTab(
    ownedData: com.example.data.OwnedBusiness,
    viewModel: GameViewModel,
    instanceId: String,
    context: android.content.Context,
    onShowReview: (com.example.data.EventProject) -> Unit
) {
    val activeList = ownedData.activeEvents

    if (activeList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Info, contentDescription = "Info", tint = Color.Gray, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Tidak ada proyek yang sedang berjalan.", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
                Text("Ambil tawaran atau ajukan tender di tab sebelah!", color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(activeList) { ev ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = BorderStroke(1.dp, Color(0xFF3B82F6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(ev.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text("Budget: $${String.format("%,.0f", ev.totalBudget)} | Estimasi Fee: $${String.format("%,.0f", ev.eoFee)}", fontSize = 11.sp, color = Color.LightGray)
                        }
                        
                        // Status badge
                        Box(
                            modifier = Modifier
                                .background(
                                    when (ev.phase) {
                                        "PLANNING" -> Color(0xFFF59E0B)
                                        "EXECUTING" -> Color(0xFF3B82F6)
                                        else -> Color(0xFF10B981)
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(ev.phase, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))

                    when (ev.phase) {
                        "PLANNING" -> {
                            // Planning phase layout: Checklist
                            Text("📋 Checklist Kebutuhan Event:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            ev.requirements.forEach { req ->
                                val mappedType = when (req.lowercase()) {
                                    "stage" -> "stage"
                                    "sound" -> "sound system"
                                    "lighting" -> "lighting"
                                    "led" -> "led wall"
                                    "power" -> "power generator"
                                    "security" -> "security"
                                    "toilet" -> "mobile toilet"
                                    "barricade" -> "barricade"
                                    "ambulance" -> "ambulance"
                                    "tent" -> "tent & truss"
                                    "truss" -> "tent & truss"
                                    "forklift" -> "heavy equipment"
                                    "truck" -> "logistics truck"
                                    "warehouse" -> "warehouse storage"
                                    "helicopter" -> "vip heli"
                                    else -> req.lowercase()
                                }
                                val hasCustom = (ownedData.eoCustomAssets ?: emptyList()).any { it.type.lowercase() == mappedType && it.quantity > 0 }
                                val isOwned = ((ownedData.eoOwnedAssets[req] ?: 0) > 0) || hasCustom
                                val isRented = ev.rentedAssets.contains(req)
                                val rentCost = viewModel.getAssetPurchasePrice(req) * 0.05
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isOwned || isRented) {
                                            Icon(Icons.Filled.Check, contentDescription = "OK", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                        } else {
                                            Icon(Icons.Filled.Close, contentDescription = "Kurang", tint = Color(0xFFF43F5E), modifier = Modifier.size(16.dp))
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(req, fontSize = 12.sp, color = Color.LightGray)
                                        if (isOwned) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("(In-House Asset)", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Medium)
                                        } else if (isRented) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("(Disewa)", fontSize = 10.sp, color = Color(0xFF3B82F6), fontWeight = FontWeight.Medium)
                                        }
                                    }
                                    
                                    if (!isOwned && !isRented) {
                                        Button(
                                            onClick = {
                                                val err = viewModel.rentAssetForEvent(instanceId, ev.id, req)
                                                if (err != null) Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                                else Toast.makeText(context, "Menyewa $req untuk event!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                            border = BorderStroke(1.dp, Color.Gray),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Sewa Vendor ($${String.format("%,.0f", rentCost)})", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                            
                            // Weather forecast banner
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (ev.weather == "SUNNY") "🌞" else "🌧", fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Prakiraan Cuaca: ${if (ev.weather == "SUNNY") "Cerah" else "Hujan"}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        if (ev.isOutdoor && ev.weather == "RAINY") {
                                            Text("⚠️ Outdoor Event & Hujan! Pastikan punya Tent / Truss agar kualitas tidak drop!", fontSize = 9.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold)
                                        } else if (ev.isOutdoor) {
                                            Text("Outdoor Event - aman jika cuaca cerah.", fontSize = 9.sp, color = Color.Gray)
                                        } else {
                                            Text("Indoor Event - tidak terpengaruh cuaca.", fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val err = viewModel.startEventExecution(instanceId, ev.id)
                                    if (err != null) Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    else Toast.makeText(context, "Event dimulai! Masuk ke eksekusi.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🚀 MULAI EVENT SEKARANG", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        "EXECUTING" -> {
                            // Executing state: Ticker progress
                            var timeLeftMs by remember { mutableStateOf(Math.max(0L, ev.executionEndTime - System.currentTimeMillis())) }
                            LaunchedEffect(ev.executionEndTime) {
                                while (timeLeftMs > 0) {
                                    delay(500)
                                    timeLeftMs = Math.max(0L, ev.executionEndTime - System.currentTimeMillis())
                                }
                            }
                            
                            val secondsLeft = timeLeftMs / 1000
                            val progress = (1f - (timeLeftMs.toFloat() / 30000f)).coerceIn(0f, 1f)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Eksekusi Sedang Berjalan...", fontSize = 12.sp, color = Color.LightGray)
                                Text("${secondsLeft}s", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = progress,
                                color = Color(0xFF3B82F6),
                                trackColor = Color.DarkGray,
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                            )
                            
                            // Incident management UI
                            if (ev.activeIncident != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)), // Dark Red
                                    border = BorderStroke(1.dp, Color(0xFFF43F5E)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🚨 INSIDEN DARURAT!", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(getIncidentDescription(ev.activeIncident), fontSize = 11.sp, color = Color.LightGray)
                                        
                                        if (ev.incidentResolved) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Box(modifier = Modifier.background(Color(0xFF10B981), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                                Text("TERATASI", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text("Ambil Keputusan Cepat:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            
                                            val options = getIncidentOptions(ev.activeIncident, ownedData)
                                            options.forEachIndexed { index, option ->
                                                Button(
                                                    onClick = {
                                                        val err = viewModel.resolveEventIncident(instanceId, ev.id, index)
                                                        if (err != null) Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                                        else Toast.makeText(context, "Insiden berhasil diatasi!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                                    border = BorderStroke(1.dp, Color.Gray),
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(option, fontSize = 10.sp, color = Color.White, textAlign = TextAlign.Center)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("🌞 Acara berjalan mulus, tidak ada kendala teknis berarti.", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        
                        "REVIEW" -> {
                            // Review state: Click to open summary Dialog
                            Button(
                                onClick = { onShowReview(ev) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🎉 LIHAT HASIL & AMBIL FEE", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// STATIC HELPERS FOR TEXT & FORMATTING
// ==========================================
fun getHqDisplayName(level: String?): String {
    return when (level) {
        "OFFICE" -> "🏢 Kantor Cabang"
        "REGIONAL" -> "🏢 Kantor Regional"
        "NATIONAL" -> "🏢 HQ Nasional"
        "INTERNATIONAL" -> "🌐 Headquarters Internasional"
        else -> "🏠 Rumah Kecil"
    }
}

fun getHqDescription(level: String?): String {
    return when (level) {
        "OFFICE" -> "Membuka tawaran klien Regional (Up to ⭐⭐⭐)"
        "REGIONAL" -> "Membuka tawaran Nasional (Up to ⭐⭐⭐⭐) & Special Event Kecil"
        "NATIONAL" -> "Membuka tawaran Internasional (⭐⭐⭐⭐⭐) & Event Concert Akbar"
        "INTERNATIONAL" -> "Unlocks all high-budget prestige World Expo & Olympics!"
        else -> "Tawaran lokal kecil (Up to ⭐⭐)"
    }
}

fun getHqLevelWeight(level: String?): Int {
    return when (level) {
        "OFFICE" -> 2
        "REGIONAL" -> 3
        "NATIONAL" -> 4
        "INTERNATIONAL" -> 5
        else -> 1
    }
}

fun getDivisionIcon(div: String): String {
    return when (div) {
        "Sales" -> "📋"
        "Creative" -> "🎨"
        "Production" -> "📐"
        "Multimedia" -> "🎥"
        "Talent" -> "🎤"
        "Logistics" -> "🚚"
        "Finance" -> "💰"
        "Legal" -> "⚖"
        "Marketing" -> "📣"
        else -> "💼"
    }
}

fun getDivisionBenefit(div: String): String {
    return when (div) {
        "Sales" -> "Meningkatkan budget penawaran klien sebesar +15%"
        "Creative" -> "Meningkatkan kualitas dasar semua event +5%"
        "Production" -> "Mengurangi biaya sewa vendor event sebesar -20%"
        "Multimedia" -> "Meningkatkan feedback kepuasan konsumen +10%"
        "Talent" -> "Meningkatkan skor review akhir bintang sebesar +0.3"
        "Logistics" -> "Mengurangi biaya ekstra cuaca buruk sebesar -25%"
        "Finance" -> "Efisiensi pajak, menambah profit bersih komisi sebesar +5%"
        "Legal" -> "Melindungi reputasi, memotong kerugian reputasi event gagal -50%"
        "Marketing" -> "Membuka lebih banyak request bulanan & reputasi naik +20%"
        else -> "Meningkatkan produktivitas bisnis"
    }
}

fun getAssetEmoji(asset: String): String {
    return when (asset) {
        "Stage" -> "🎭"
        "Sound" -> "🔊"
        "Lighting" -> "💡"
        "LED" -> "📺"
        "Power" -> "⚡"
        "Security" -> "🛡"
        "Toilet" -> "🚽"
        "Barricade" -> "🚧"
        "Ambulance" -> "🚑"
        "Tent" -> "🎪"
        "Truss" -> "🏗"
        "Forklift" -> "🚜"
        "Truck" -> "🚚"
        "Warehouse" -> "🏬"
        "Helicopter" -> "🚁"
        else -> "📦"
    }
}

fun getIncidentDescription(incident: String): String {
    return when (incident) {
        "GENSET_BROKEN" -> "Genset panggung meledak dan listrik mati! Konser terhenti total dan penonton mulai rusuh."
        "VENDOR_LATE" -> "Vendor utama pengirim catering / material telat kena macet parah. Rundown terancam mundur."
        "ARTIST_CANCELED" -> "Artis pengisi acara utama mendadak batal tampil karena sakit/penerbangan delayed."
        "TICKETS_OVERSOLD" -> "Penjualan tiket membludak melebihi kapasitas tempat, kerumunan sangat padat dan sesak."
        "DEMONSTRATION" -> "Ada demonstrasi massa di dekat pintu gerbang utama masuk, memicu kemacetan dan ketegangan keamanan."
        "STRONG_WINDS" -> "Angin kencang berhembus, membuat struktur rigging panggung utama bergoyang membahayakan."
        else -> "Kendala teknis mendadak terjadi di lapangan!"
    }
}

fun getIncidentOptions(incident: String, ownedData: com.example.data.OwnedBusiness): List<String> {
    return when (incident) {
        "GENSET_BROKEN" -> {
            val hasGenerator = (ownedData.eoOwnedAssets["Power"] ?: 0) > 0
            listOf(
                "Sewa Genset Darurat Cepat ($10,000)",
                if (hasGenerator) "Gunakan Backup Generator Sendiri (Free)" else "Gunakan Genset Biasa (Kualitas -10%, $5,000)",
                "Biarkan saja (Kualitas Drop Parah -30%)"
            )
        }
        "VENDOR_LATE" -> listOf(
            "Bayar Kurir Transportasi Cepat ($5,000)",
            "Tunggu saja sampai tiba (Kualitas -15%)"
        )
        "ARTIST_CANCELED" -> listOf(
            "Hubungi & Bayar Artis Pengganti Kelas S ($20,000)",
            "Negosiasi & Tawarkan Diskon Kompensasi ($8,000, Kualitas -10%)",
            "Batalkan Sesi Utama (Kualitas -40%)"
        )
        "TICKETS_OVERSOLD" -> listOf(
            "Sewa Layar LED & Upgrade Area Luar ($15,000)",
            "Abaikan (Desak-desakan, Kualitas -25%)"
        )
        "DEMONSTRATION" -> listOf(
            "Sewa Pasukan Pengamanan Tambahan ($12,000)",
            "Negosiasi Damai / Pindahkan Akses (Kualitas -20%)"
        )
        "STRONG_WINDS" -> listOf(
            "Bayar Teknisi Perkuat Konstruksi Rigging ($8,000)",
            "Abaikan & Berdoa Aman (Kualitas -30%)"
        )
        else -> listOf("Atasi Masalah ($5,000)", "Abaikan (Kualitas -15%)")
    }
}
