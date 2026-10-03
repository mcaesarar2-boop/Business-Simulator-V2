package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoldingDashboardScreen(navController: NavHostController, viewModel: GameViewModel, holdingId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()
    val stockList by viewModel.stockList.collectAsState()
    val holding = playerState.holdingCompanies.find { it.instanceId == holdingId }
    
    if (holding == null) {
        navController.popBackStack()
        return
    }

    var showIpoDialog by remember { mutableStateOf(false) }
    var showDivestDialog by remember { mutableStateOf(false) }
    var showDemergerDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var showEjectDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newHoldingName by remember { mutableStateOf(holding.name) }
    var transferType by remember { mutableStateOf("DEPOSIT") }
    var transferAmountInput by remember { mutableStateOf("") }
    var showDividendPolicyDialog by remember { mutableStateOf(false) }
    val policyRepo = remember { com.example.corporate.repository.CorporatePolicyRepository.getInstance() }
    val holdingPolicies by policyRepo.holdingPolicies.collectAsState()
    val currentPolicy = holdingPolicies[holdingId] ?: com.example.corporate.model.MergedDividendPolicy.DEFAULT

    val holdingSubsidiaries = (holding.subsidiaries + playerState.ownedBusinesses.filter { it.parentId == holdingId }).distinctBy { it.instanceId }

    val valuation = CorporateFinanceManager.calculateHoldingValuation(holding, playerState)
    val monthlyRev = CorporateFinanceManager.calculateHoldingMargin(holding, playerState)
    val isIpoEligible = valuation >= 5_000_000L && !holding.isPublic

    val onSubsidiaryClick: (OwnedBusiness, BusinessCatalogItem) -> Unit = { b, ct ->
        if (b.catalogId == "asset_management") {
            navController.navigate("asset_management_hub")
        } else if (b.catalogId == "streaming_service") {
            navController.navigate("streaming_service_screen/${b.instanceId}")
        } else if (b.catalogId == "content_creator") {
            navController.navigate("content_creator_screen/${b.instanceId}")
        } else if (b.catalogId == "football_club" || ct.category == com.example.data.BusinessCategory.SPORTS) {
            navController.navigate("football_club_dashboard")
        } else if (b.catalogId == "indie_game_publisher") {
            navController.navigate("game_publisher_hub/${b.instanceId}")
        } else if (b.catalogId == "mid_logistics" || ct.category == com.example.data.BusinessCategory.LOGISTICS) {
            navController.navigate("logistics_dashboard/${b.instanceId}")
        } else if (b.catalogId == "upper_realestate") {
            navController.navigate("apartment_property/${b.instanceId}")
        } else if (b.catalogId == "tycoon_bank" || ct.category == com.example.data.BusinessCategory.FINANCE) {
            navController.navigate("banking_dashboard/${b.instanceId}")
        } else if (b.catalogId == "upper_tech") {
            navController.navigate("software_house_dashboard/${b.instanceId}")
        } else if (b.catalogId == "ai_cloud_provider") {
            navController.navigate("ai_cloud_dashboard/${b.instanceId}")
        } else if (ct.category == com.example.data.BusinessCategory.AVIATION) {
            navController.navigate("aviation_dashboard/${b.instanceId}")
        } else if (ct.category == com.example.data.BusinessCategory.THEME_PARK_HOLDING) {
            navController.navigate("theme_park_dashboard/${b.instanceId}")
        } else if (ct.category == com.example.data.BusinessCategory.HOSPITALITY) {
            navController.navigate("hospitality_dashboard/${b.instanceId}")
        } else if (ct.category == com.example.data.BusinessCategory.CRUISE_LINE) {
            navController.navigate("cruise_dashboard/${b.instanceId}")
        } else {
            navController.navigate("business_detail/${b.instanceId}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Corporate Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF2A2A35), Color(0xFF121212))),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = holding.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    newHoldingName = holding.name
                                    showRenameDialog = true
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Nama",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Valuation", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                                Text(
                                    text = com.example.ui.formatCurrencyRingkas(valuation, useShortFormat),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Monthly Margin", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                                Text(
                                    text = com.example.ui.formatCurrencyRingkas(monthlyRev, useShortFormat),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Yellow
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kepemilikan Saham:", fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
                            Text("${String.format("%.1f", holding.ownershipPercentage)}%", fontWeight = FontWeight.Bold, color = if(holding.isPublic) Color(0xFF4CAF50) else Color.White)
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Kas Internal Holding", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                            Text(
                                text = com.example.ui.formatCurrencyRingkas(holding.holdingCash.toLong(), useShortFormat),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            androidx.compose.material3.IconButton(onClick = { showDividendPolicyDialog = true }) {
                                androidx.compose.material3.Icon(
                                    androidx.compose.material.icons.Icons.Default.Tune,
                                    contentDescription = "Kebijakan Arus Kas & Dividen",
                                    tint = Color(0xFF42A5F5)
                                )
                            }
                            androidx.compose.material3.IconButton(onClick = { transferType = "WITHDRAW"; transferAmountInput = ""; showTransferDialog = true }) {
                                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Upload, contentDescription = "Tarik Dana", tint = Color(0xFFE57373))
                            }
                            androidx.compose.material3.IconButton(onClick = { transferType = "DEPOSIT"; transferAmountInput = ""; showTransferDialog = true }) {
                                androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Suntik Dana", tint = Color(0xFFFFD700))
                            }
                        }
                    }
                }
            }

            // Cash Flow Policy Banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDividendPolicyDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.8f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                Icons.Default.PieChart,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Kebijakan Arus Kas (3-Tier)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Text(
                                    text = "${currentPolicy.unitRetainedPercent}% Kas Unit • ${currentPolicy.holdingTreasuryPercent}% Kas Holding • ${currentPolicy.megaHoldingDividendPercent}% Dividen",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                        Text(
                            text = "Ubah",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            // Actions Buttons 
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { navController.navigate("business_catalog?holdingId=${holdingId}") },
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color(0xFFFFD700)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tambah Divisi", fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    if (isIpoEligible) {
                        Button(
                            onClick = { showIpoDialog = true },
                            modifier = Modifier.weight(1f).height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color(0xFF4CAF50)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Go Public (IPO)", fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    } else if (!holding.isPublic) {
                        Button(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.weight(1f).height(54.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), disabledContainerColor = Color.White.copy(alpha = 0.05f), disabledContentColor = Color.Gray),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("IPO (Bth \$5M)", fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                    
                    Button(
                        onClick = { showDivestDialog = true },
                        modifier = Modifier.weight(1f).height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color.Red),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Divestment", fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showDemergerDialog = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.05f),
                        contentColor = Color(0xFFFF9800)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Demerger (Pecah Holding)", fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }

            // Subsidiaries Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.holding_subsidiaries_count, holdingSubsidiaries.size),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    if (holdingSubsidiaries.isNotEmpty()) {
                        Surface(
                            color = Color.Red.copy(alpha = 0.08f),
                            contentColor = Color(0xFFEF5350),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF5350).copy(alpha = 0.3f)),
                            modifier = Modifier.clickable { showEjectDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Launch,
                                    contentDescription = "Keluarkan",
                                    modifier = Modifier.size(12.dp),
                                    tint = Color(0xFFEF5350)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Keluarkan Aset",
                                    color = Color(0xFFEF5350),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Subsidiaries List
            if (holdingSubsidiaries.isEmpty()) {
                item {
                    Text("Belum ada anak perusahaan.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(holdingSubsidiaries) { sub ->
                    val stockSector = if (sub.acquiredStockTicker != null) stockList.find { it.ticker == sub.acquiredStockTicker }?.sector else null
                    val catalogItem = getCatalogItem(sub.catalogId, playerState) ?: if (sub.acquiredStockTicker != null) {
                        com.example.data.BusinessCatalogItem(
                            id = sub.catalogId,
                            name = sub.customName ?: "Perusahaan Publik",
                            category = when (stockSector?.uppercase()) {
                                "PROPERTY", "REAL_ESTATE" -> com.example.data.BusinessCategory.PROPERTY
                                "AVIATION" -> com.example.data.BusinessCategory.AVIATION
                                "CULINARY" -> com.example.data.BusinessCategory.CULINARY
                                "RETAIL" -> com.example.data.BusinessCategory.RETAIL
                                "HOSPITALITY" -> com.example.data.BusinessCategory.HOSPITALITY
                                "ENTERTAINMENT" -> com.example.data.BusinessCategory.ENTERTAINMENT
                                else -> com.example.data.BusinessCategory.CULINARY
                            },
                            costToBuy = 0L,
                            monthlyRevenue = 0L,
                            monthlyMaintenanceCost = 0L
                        )
                    } else null

                    if (catalogItem != null) {
                        val (rev, _) = getBusinessStats(sub, catalogItem, playerState)
                        
                        if (catalogItem.category == com.example.data.BusinessCategory.THEME_PARK_HOLDING) {
                            ThemeParkHoldingCard(
                                owned = sub,
                                catalogItem = catalogItem,
                                useShortFormat = useShortFormat,
                                onClick = { onSubsidiaryClick(sub, catalogItem) }
                            )
                        } else if (catalogItem.category == com.example.data.BusinessCategory.HOSPITALITY) {
                            HospitalityHoldingCard(
                                owned = sub,
                                catalogItem = catalogItem,
                                useShortFormat = useShortFormat,
                                onClick = { onSubsidiaryClick(sub, catalogItem) }
                            )
                        } else {
                            BusinessItemCard(
                                owned = sub,
                                catalogItem = catalogItem,
                                rev = rev,
                                useShortFormat = useShortFormat,
                                stockSector = stockSector,
                                onClick = { onSubsidiaryClick(sub, catalogItem) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showIpoDialog) {
        var sliderValue by remember { mutableStateOf(20f) }
        AlertDialog(
            onDismissRequest = { showIpoDialog = false },
            title = { Text("Initial Public Offering (IPO)") },
            text = {
                Column {
                    Text("Jual sebagian saham ke publik untuk mendapatkan capital instan. Maksimal 49% agar tetap menjadi pemegang saham mayoritas.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Saham yang dijual: ${sliderValue.toInt()}%", fontWeight = FontWeight.Bold)
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        valueRange = 10f..49f,
                        steps = 38
                    )
                    val estFund = (valuation * (sliderValue / 100f)).toLong()
                    Text("Estimasi Dana Didapat: ${com.example.ui.formatCurrencyRingkas(estFund, useShortFormat)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.processIPO(holdingId, sliderValue)
                    showIpoDialog = false 
                }) { Text("Luncurkan IPO") }
            },
            dismissButton = {
                TextButton(onClick = { showIpoDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showDivestDialog) {
        AlertDialog(
            onDismissRequest = { showDivestDialog = false },
            title = { Text("Divestment (Jual Perusahaan)") },
            text = {
                val estFund = (valuation * (holding.ownershipPercentage / 100f)).toLong()
                Text("Anda akan melepas seluruh kepemilikan Anda (${String.format("%.1f", holding.ownershipPercentage)}%). Perusahaan akan dihapus dari portofolio Anda selamanya.\n\nEstimasi Dana Kas: ${com.example.ui.formatCurrencyRingkas(estFund, useShortFormat)}")
            },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.processDivestment(holdingId)
                    showDivestDialog = false 
                    navController.popBackStack()
                }) { Text("Jual Sekarang", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDivestDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showDemergerDialog) {
        AlertDialog(
            onDismissRequest = { showDemergerDialog = false },
            title = { Text("Demerger Holding", color = Color(0xFFF57C00), fontWeight = FontWeight.Bold) },
            text = {
                Text("Apakah Anda yakin ingin membubarkan holding ini? Seluruh anak perusahaan di dalamnya akan dikembalikan sebagai bisnis individu ke Dashboard Utama Anda.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.demergerHolding(holdingId)
                        showDemergerDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00))
                ) {
                    Text("Proses Demerger", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDemergerDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showTransferDialog) {
        val titleText = if (transferType == "DEPOSIT") "Suntik Dana (Kas Pribadi -> Holding)" else "Tarik Dana (Holding -> Kas Pribadi)"
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text(titleText) },
            text = {
                Column {
                    Text("Kas Pribadi (Global): ${com.example.ui.formatCurrencyRingkas(playerState.cash, false)}")
                    Text("Kas Holding: ${com.example.ui.formatCurrencyRingkas(holding.holdingCash.toLong(), false)}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = transferAmountInput,
                        onValueChange = { transferAmountInput = it },
                        label = { Text("Jumlah (tanpa titik)") },
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
                            viewModel.injectCapitalToHolding(holdingId, amountLong)
                        } else {
                            viewModel.withdrawCapitalFromHolding(holdingId, amountLong)
                        }
                    }
                    showTransferDialog = false
                }) { Text("Transfer") }
            },
            dismissButton = { TextButton(onClick = { showTransferDialog = false }) { Text("Batal") } }
        )
    }

    if (showDividendPolicyDialog) {
        com.example.corporate.ui.DividendPolicyDialog(
            holdingId = holdingId,
            holdingName = holding.name,
            estimatedMonthlyProfit = monthlyRev,
            onDismiss = { showDividendPolicyDialog = false }
        )
    }

    if (showEjectDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        AlertDialog(
            onDismissRequest = { showEjectDialog = false },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Keluarkan Anak Perusahaan",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Pilih anak perusahaan yang ingin dikeluarkan dari Holding ini kembali ke Dashboard Utama (Root).",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(holdingSubsidiaries) { sub ->
                                val catItem = getCatalogItem(sub.catalogId, playerState) ?: if (sub.acquiredStockTicker != null) {
                                    val stockSector = stockList.find { it.ticker == sub.acquiredStockTicker }?.sector
                                    com.example.data.BusinessCatalogItem(
                                        id = sub.catalogId,
                                        name = sub.customName ?: "Perusahaan Publik",
                                        category = when (stockSector?.uppercase()) {
                                            "PROPERTY", "REAL_ESTATE" -> com.example.data.BusinessCategory.PROPERTY
                                            "AVIATION" -> com.example.data.BusinessCategory.AVIATION
                                            "CULINARY" -> com.example.data.BusinessCategory.CULINARY
                                            "RETAIL" -> com.example.data.BusinessCategory.RETAIL
                                            "HOSPITALITY" -> com.example.data.BusinessCategory.HOSPITALITY
                                            "ENTERTAINMENT" -> com.example.data.BusinessCategory.ENTERTAINMENT
                                            else -> com.example.data.BusinessCategory.CULINARY
                                        },
                                        costToBuy = 0L,
                                        monthlyRevenue = 0L,
                                        monthlyMaintenanceCost = 0L
                                    )
                                } else null
                                
                                val name = sub.customName ?: catItem?.name ?: "Tanpa Nama"
                                
                                Surface(
                                    color = Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            if (sub.acquiredStockTicker != null) {
                                                Text(
                                                    text = "Ticker: ${sub.acquiredStockTicker}",
                                                    color = Color.Yellow,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.ejectBusinessToRoot(sub.instanceId, holdingId)
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "$name berhasil dipindahkan ke Dashboard Utama",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                                if (holdingSubsidiaries.size <= 1) {
                                                    showEjectDialog = false
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Red.copy(alpha = 0.8f),
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(
                                                    imageVector = Icons.Default.Launch,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text("Keluarkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showEjectDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    if (showRenameDialog) {
        val context = androidx.compose.ui.platform.LocalContext.current
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Rebranding Holding Company",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Masukkan nama baru untuk entitas korporat ini agar memiliki recognisi global yang kokoh.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newHoldingName,
                        onValueChange = { newHoldingName = it },
                        label = { Text("Nama Holding Baru", color = Color(0xFFFFD700)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = Color(0xFFFFD700),
                            unfocusedLabelColor = Color.LightGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newHoldingName.trim().isNotEmpty()) {
                            viewModel.renameHoldingCompany(holdingId, newHoldingName)
                            android.widget.Toast.makeText(
                                context,
                                "Nama korporasi diubah menjadi $newHoldingName",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            showRenameDialog = false
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "Nama tidak boleh kosong!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MegaHoldingDetailScreen(navController: NavHostController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val holdingState = playerState.megaHolding
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()
    val stockList by viewModel.stockList.collectAsState()

    var showBoardConfig by remember { mutableStateOf(false) }

    if (!holdingState.isActive) {
        navController.popBackStack()
        return
    }

    val onSubsidiaryClick: (com.example.data.OwnedBusiness, com.example.data.BusinessCatalogItem) -> Unit = { b, ct ->
        if (b.catalogId == "asset_management") {
            navController.navigate("asset_management_hub")
        } else if (b.catalogId == "content_creator") {
            navController.navigate("content_creator_screen/${b.instanceId}")
        } else if (b.catalogId == "football_club" || ct.category == com.example.data.BusinessCategory.SPORTS) {
            navController.navigate("football_club_dashboard")
        } else if (b.catalogId == "mid_logistics" || ct.category == com.example.data.BusinessCategory.LOGISTICS) {
            navController.navigate("logistics_dashboard/${b.instanceId}")
        } else if (b.catalogId == "upper_realestate") {
            navController.navigate("apartment_property/${b.instanceId}")
        } else if (b.catalogId == "tycoon_bank" || ct.category == com.example.data.BusinessCategory.FINANCE) {
            navController.navigate("banking_dashboard/${b.instanceId}")
        } else if (b.catalogId == "upper_tech") {
            navController.navigate("software_house_dashboard/${b.instanceId}")
        } else if (ct.category == com.example.data.BusinessCategory.AVIATION) {
            navController.navigate("aviation_dashboard/${b.instanceId}")
        } else if (ct.category == com.example.data.BusinessCategory.THEME_PARK_HOLDING) {
            navController.navigate("theme_park_dashboard/${b.instanceId}")
        } else if (ct.category == com.example.data.BusinessCategory.HOSPITALITY) {
            navController.navigate("hospitality_dashboard/${b.instanceId}")
        } else if (ct.category == com.example.data.BusinessCategory.CRUISE_LINE) {
            navController.navigate("cruise_dashboard/${b.instanceId}")
        } else {
            navController.navigate("business_detail/${b.instanceId}")
        }
    }

    val totalBusinessValuation = playerState.ownedBusinesses.sumOf {
        it.calculateTotalValuation()
    }
    val totalHoldingValuation = playerState.holdingCompanies.sumOf { holding ->
        holding.subsidiaries.sumOf { sub ->
            sub.calculateTotalValuation()
        }
    }
    var stocksValue = 0L
    var totalDividendIncome = 0L
    val baseMegaBusinessValuation = totalBusinessValuation + totalHoldingValuation
    var baseMegaValuation = baseMegaBusinessValuation

    var baseMegaBusinessIncome = playerState.ownedBusinesses.sumOf { 
         it.calculateNetIncome()
    } + playerState.holdingCompanies.sumOf { h -> 
         h.subsidiaries.sumOf { sub -> 
             sub.calculateNetIncome()
         }
    }
    var baseMegaIncome = baseMegaBusinessIncome

    if (holdingState.includesInvestments) {
        stocksValue = playerState.ownedStocks.sumOf { owned ->
            val liveStock = stockList.find { it.ticker == owned.ticker }
            (owned.shares * (liveStock?.currentPrice ?: owned.averagePrice)).toLong()
        }
        baseMegaValuation += stocksValue

        playerState.ownedStocks.forEach { owned ->
            val liveStock = stockList.find { it.ticker == owned.ticker }
            if (liveStock != null) {
                val currentPriceUsd = liveStock.currentPrice
                val stats = com.example.data.getMarketStats(liveStock)
                val monthlyYieldPercent = stats.dividendYield / 12.0 / 100.0 // Annual yield / 12 / 100
                totalDividendIncome += (owned.shares * currentPriceUsd * monthlyYieldPercent).toLong()
            }
        }
        baseMegaIncome += totalDividendIncome
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ruang Direksi Mega Holding", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showBoardConfig = true }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                            contentDescription = "Board Config",
                            tint = Color(0xFFFFD700)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = Color(0xFFFFD700),
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        if (showBoardConfig) {
            MegaHoldingsBoardConfig(
                onDismiss = { showBoardConfig = false },
                viewModel = viewModel
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    coil.compose.AsyncImage(
                        model = "https://plus.unsplash.com/premium_photo-1682716270464-9a91cbbcf3b7?q=80&w=1171&auto=format&fit=crop",
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.95f))
                                )
                            )
                    )
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = holdingState.companyName.ifBlank { "Mega Holding Corporation" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Market Cap Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total Market Cap:",
                                color = Color(0xFFE0E0E0),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = com.example.ui.formatCurrencyRingkas(baseMegaValuation, useShortFormat),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                " ↳ Valuasi Bisnis",
                                color = Color(0xFFB0B0B0),
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = com.example.ui.formatCurrencyRingkas(baseMegaBusinessValuation, useShortFormat),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "   ↳ Porsi Anda (${String.format(java.util.Locale.US, "%.1f", playerState.companyOwnershipPercent)}%)",
                                color = Color(0xFF00FF00),
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = com.example.ui.formatCurrencyRingkas(playerState.playerBusinessValuation, useShortFormat),
                                color = Color(0xFF00FF00),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        if (holdingState.includesInvestments) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = " ↳ Aset Investasi (${holdingState.investmentCompanyName.ifBlank { "Asset Mgmt" }})", 
                                    color = Color(0xFFB0B0B0), 
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = com.example.ui.formatCurrencyRingkas(stocksValue, useShortFormat), 
                                    color = Color.White, 
                                    fontWeight = FontWeight.SemiBold, 
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Revenue Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total Revenue / Bulan:",
                                color = Color(0xFF4CAF50),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "+${com.example.ui.formatCurrencyRingkas(baseMegaIncome, useShortFormat)}",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                " ↳ Laba Bersih Bisnis",
                                color = Color(0xFFB0B0B0),
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "+${com.example.ui.formatCurrencyRingkas(baseMegaBusinessIncome, useShortFormat)}",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        if (holdingState.includesInvestments) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    " ↳ Dividen Investasi",
                                    color = Color(0xFFB0B0B0),
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "+${com.example.ui.formatCurrencyRingkas(totalDividendIncome, useShortFormat)}",
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "Termasuk Aset Investasi: ${if (holdingState.includesInvestments) "Ya" else "Tidak"}",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Kepemilikan Saham Pribadi: ${String.format(java.util.Locale.US, "%.1f", playerState.companyOwnershipPercent)}%",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                var showIpoDialog by remember { mutableStateOf(false) }
                var showFinancialHistoryDialog by remember { mutableStateOf(false) }

                Button(
                    onClick = { showFinancialHistoryDialog = true },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2A4A), contentColor = Color(0xFFFFD700)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📊 Histori Laba & Kinerja Finansial", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { showIpoDialog = true },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color(0xFFFFD700)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Opsi IPO (Jual Saham)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                if (showFinancialHistoryDialog) {
                    FinancialHistoryScreen(
                        onDismiss = { showFinancialHistoryDialog = false },
                        playerState = playerState
                    )
                }

                if (showIpoDialog) {
                    var sliderValue by remember { mutableStateOf(0f) }
                    AlertDialog(
                        onDismissRequest = { showIpoDialog = false },
                        title = { Text("Jual Saham Mega Holding", fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                Text("Jual sebagian saham untuk mendapatkan fresh money. Anda tidak bisa menjual lebih dari yang Anda miliki (${String.format("%.1f", holdingState.ownershipPercentage)}%).")
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Melepas: ${sliderValue.toInt()}% Saham", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Sisa Kepemilikan Anda: ${String.format("%.1f", holdingState.ownershipPercentage - sliderValue)}%", color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                val estFund = (baseMegaValuation * (sliderValue / 100f)).toLong()
                                Text("Estimasi Dana Segar Diterima: +${com.example.ui.formatCurrencyRingkas(estFund, false)}", color = Color(0xFF4CAF50), fontWeight = FontWeight.ExtraBold)
                                Spacer(modifier = Modifier.height(16.dp))
                                Slider(
                                    value = sliderValue,
                                    onValueChange = { sliderValue = it },
                                    valueRange = 0f..holdingState.ownershipPercentage.toFloat(),
                                    steps = if (holdingState.ownershipPercentage.toInt() >= 1) holdingState.ownershipPercentage.toInt() - 1 else 0
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    if (sliderValue > 0f) {
                                        viewModel.ipoMegaHolding(sliderValue.toDouble())
                                        showIpoDialog = false
                                    }
                                },
                                enabled = sliderValue > 0f
                            ) { Text("Eksekusi", fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showIpoDialog = false }) { Text("Batal") }
                        }
                    )
                }
            }

            val rootOwnedBusinesses = playerState.ownedBusinesses.filter { it.parentId.isNullOrEmpty() || it.acquiredStockTicker != null }
            val totalAnakPerusahaan = rootOwnedBusinesses.size + playerState.holdingCompanies.sumOf { it.subsidiaries.size } + if (holdingState.includesInvestments) 1 else 0
            item {
                Text("Anak Perusahaan (Total: $totalAnakPerusahaan Entitas)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
            }

            if (holdingState.includesInvestments) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { navController.navigate("asset_management_hub") }
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = holdingState.investmentCompanyName.ifBlank { "Portofolio Investasi Pribadi" },
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFA5D6A7),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ASSET MANAGEMENT & INVESTMENTS",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "+${com.example.ui.formatCurrencyRingkas(totalDividendIncome, useShortFormat)}/bln",
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForwardIos,
                                    contentDescription = "Buka Detail Portofolio",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            items(rootOwnedBusinesses) { b ->
                val ct = com.example.data.getCatalogItem(b.catalogId, playerState)
                if (ct != null) {
                    val (rev, main) = com.example.data.getBusinessStats(b, ct, playerState)
                    val net = rev - main
                    val stockSector = if (b.acquiredStockTicker != null) stockList.find { it.ticker == b.acquiredStockTicker }?.sector else null
                    
                    if (ct.category == com.example.data.BusinessCategory.THEME_PARK_HOLDING) {
                        ThemeParkHoldingCard(
                            owned = b,
                            catalogItem = ct,
                            useShortFormat = useShortFormat,
                            onClick = { onSubsidiaryClick(b, ct) }
                        )
                    } else if (ct.category == com.example.data.BusinessCategory.HOSPITALITY) {
                        HospitalityHoldingCard(
                            owned = b,
                            catalogItem = ct,
                            useShortFormat = useShortFormat,
                            onClick = { onSubsidiaryClick(b, ct) }
                        )
                    } else {
                        BusinessItemCard(
                            owned = b,
                            catalogItem = ct,
                            rev = net,
                            useShortFormat = useShortFormat,
                            stockSector = stockSector,
                            onClick = { onSubsidiaryClick(b, ct) }
                        )
                    }
                }
            }

            items(playerState.holdingCompanies.size) { index ->
                val h = playerState.holdingCompanies[index]
                HoldingItemCard(
                    holding = h,
                    rev = com.example.data.CorporateFinanceManager.calculateHoldingMargin(h, playerState),
                    useShortFormat = useShortFormat,
                    onClick = { navController.navigate("holding_dashboard/${h.instanceId}") }
                )
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun ThemeParkHoldingCard(owned: OwnedBusiness, catalogItem: com.example.data.BusinessCatalogItem, useShortFormat: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(owned.customName ?: catalogItem.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Text("Universal Theme Park & Leisure", fontSize = 12.sp, color = Color(0xFFFFD700))
                }
                Icon(Icons.Default.Attractions, contentDescription = "Theme Park", tint = Color(0xFFFFD700))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Cabang", color = Color.Gray, fontSize = 12.sp)
                    Text("${owned.themeParkBranches.size} Taman", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Valuasi Aset Khusus", color = Color.Gray, fontSize = 12.sp)
                    val customValuation = owned.themeParkBranches.sumOf { it.landType.basePrice }
                    Text(com.example.ui.formatCurrencyRingkas(customValuation.toDouble(), useShortFormat), color = Color.Green, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun HospitalityHoldingCard(owned: OwnedBusiness, catalogItem: com.example.data.BusinessCatalogItem, useShortFormat: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3E2723).copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(owned.customName ?: catalogItem.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Text("Global Hospitality & Hotels", fontSize = 12.sp, color = Color(0xFFFFB300))
                }
                Icon(Icons.Default.Hotel, contentDescription = "Hotel", tint = Color(0xFFFFB300))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Properti & Kamar", color = Color.Gray, fontSize = 12.sp)
                    val totalRooms = owned.hospitalityProperties.sumOf { it.tier.maxRooms }
                    Text("${owned.hospitalityProperties.size} Properti, $totalRooms Kamar", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
