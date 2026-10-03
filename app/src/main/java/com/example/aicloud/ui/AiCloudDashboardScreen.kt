package com.example.aicloud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aicloud.model.*
import com.example.aicloud.viewmodel.AiCloudViewModel
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCloudDashboardScreen(
    navController: NavController,
    gameViewModel: GameViewModel,
    businessInstanceId: String,
    cloudViewModel: AiCloudViewModel = viewModel()
) {
    val playerState by gameViewModel.playerState.collectAsState()
    val companyName by cloudViewModel.companyName.collectAsState()
    val companyBalance by cloudViewModel.companyBalance.collectAsState()
    val dataCenters by cloudViewModel.dataCenters.collectAsState()
    val activeContracts by cloudViewModel.activeContracts.collectAsState()
    val marketContracts by cloudViewModel.marketContracts.collectAsState()
    val unlockedResearchIds by cloudViewModel.unlockedResearchIds.collectAsState()
    val lastReport by cloudViewModel.lastReport.collectAsState()
    val incidents by cloudViewModel.incidents.collectAsState()
    val overviewStats by cloudViewModel.overviewStats.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog States
    var showBuildDcDialog by remember { mutableStateOf(false) }
    var selectedDcForDeploy by remember { mutableStateOf<AiDataCenter?>(null) }
    var selectedDcForRetrofit by remember { mutableStateOf<AiDataCenter?>(null) }
    var selectedRackForTradeIn by remember { mutableStateOf<Pair<AiDataCenter, ServerRackUnit>?>(null) }
    var selectedRackForDecommission by remember { mutableStateOf<Pair<AiDataCenter, ServerRackUnit>?>(null) }
    var contractToTerminate by remember { mutableStateOf<ClientContract?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var transferMode by remember { mutableStateOf("DEPOSIT") } // DEPOSIT or WITHDRAW
    var transferAmountInput by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newNameInput by remember { mutableStateOf(companyName) }

    var selectedTab by remember { mutableStateOf(0) }

    // Synchronize initial corporate cash with AI Cloud repository balance
    LaunchedEffect(businessInstanceId) {
        val ownedBiz = playerState.ownedBusinesses.find { it.instanceId == businessInstanceId }
            ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == businessInstanceId }
        val name = ownedBiz?.customName ?: "Apex Neural Infrastructure Corp."
        val canonicalCash = ownedBiz?.companyCash?.toLong()
        cloudViewModel.initializeIfNeeded(name, canonicalCash)
    }

    // Keep global business cash in sync when companyBalance updates internally
    LaunchedEffect(companyBalance) {
        val ownedBiz = playerState.ownedBusinesses.find { it.instanceId == businessInstanceId }
            ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == businessInstanceId }
        if (ownedBiz != null && ownedBiz.companyCash.toLong() != companyBalance) {
            gameViewModel.setBusinessCash(businessInstanceId, companyBalance.toDouble())
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(companyName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.TextPrimary)
                            IconButton(onClick = { newNameInput = companyName; showRenameDialog = true }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Rename", tint = AiCloudColors.CyanPrimary, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text("AI Cloud & Data Center Infrastructure", fontSize = 11.sp, color = AiCloudColors.TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AiCloudColors.TextPrimary)
                    }
                },
                actions = {
                    Surface(
                        color = AiCloudColors.SurfaceHighlight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = AiCloudColors.GreenNeon, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$${String.format("%,d", companyBalance)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.GreenNeon)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AiCloudColors.BgDark)
            )
        },
        containerColor = AiCloudColors.BgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Overview Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = AiCloudColors.CardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("GLOBAL COMPUTE CAPACITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.CyanPrimary)
                                Text(
                                    text = "${String.format("%,.0f", overviewStats.totalTflops)} TFLOPS",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AiCloudColors.TextPrimary
                                )
                                Text(
                                    text = "${String.format("%.2f", overviewStats.totalPflops)} PFLOPS Across ${overviewStats.operationalFacilities} Facilities",
                                    fontSize = 11.sp,
                                    color = AiCloudColors.TextSecondary
                                )
                            }

                            Button(
                                onClick = { showTransferDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.CyanPrimary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Transfer", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = AiCloudColors.CardBorder)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Power Draw", fontSize = 10.sp, color = AiCloudColors.TextSecondary)
                                Text("${String.format("%.1f", overviewStats.currentPowerDrawMw)} / ${String.format("%.1f", overviewStats.totalPowerCapacityMw)} MW", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.TextPrimary)
                            }
                            Column {
                                Text("Average PUE", fontSize = 10.sp, color = AiCloudColors.TextSecondary)
                                Text(String.format("%.2f", overviewStats.averagePue), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (overviewStats.averagePue < 1.15) AiCloudColors.GreenNeon else AiCloudColors.AmberWarning)
                            }
                            Column {
                                Text("Active Contracts", fontSize = 10.sp, color = AiCloudColors.TextSecondary)
                                Text("${overviewStats.activeContractsCount} Leases", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.CyanPrimary)
                            }
                            Column {
                                Text("MRR Revenue", fontSize = 10.sp, color = AiCloudColors.TextSecondary)
                                Text("$${String.format("%,d", overviewStats.activeContractRevenueMonthly)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.GreenNeon)
                            }
                        }
                    }
                }
            }

            // Tab Navigation Switcher
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = AiCloudColors.BgDark,
                    contentColor = AiCloudColors.CyanPrimary,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    val tabs = listOf("Data Centers", "Hardware Racks", "AI Contracts", "R&D Tech", "Financials")
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) AiCloudColors.CyanPrimary else AiCloudColors.TextSecondary
                                )
                            }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Data Centers Tab
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Worldwide Data Centers (${dataCenters.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AiCloudColors.TextPrimary)
                            Button(
                                onClick = { showBuildDcDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.CyanPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.AddLocation, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Build Facility", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    if (dataCenters.isEmpty()) {
                        item {
                            Surface(
                                color = AiCloudColors.CardBg,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Apartment, contentDescription = null, tint = AiCloudColors.TextSecondary, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No Facilities Constructed", color = AiCloudColors.TextPrimary, fontWeight = FontWeight.Bold)
                                    Text("Build a Tier 1 Standard facility to start deploying compute.", color = AiCloudColors.TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        items(dataCenters) { dc ->
                            DataCenterCard(
                                dataCenter = dc,
                                onManageHardware = { selectedDcForDeploy = dc },
                                onRetrofitFacility = { selectedDcForRetrofit = dc }
                            )
                        }
                    }
                }

                1 -> {
                    // Hardware Racks Tab
                    if (dataCenters.isEmpty()) {
                        item {
                            Text("Construct a data center first before configuring hardware.", color = AiCloudColors.TextSecondary, fontSize = 13.sp)
                        }
                    } else {
                        items(dataCenters) { dc ->
                            HardwareManagementSection(
                                dataCenter = dc,
                                onDeployMoreRacks = { selectedDcForDeploy = dc },
                                onServiceRack = { rackId ->
                                    cloudViewModel.serviceRack(dc.id, rackId) { success, msg ->
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                },
                                onTradeInRack = { rack ->
                                    selectedRackForTradeIn = Pair(dc, rack)
                                },
                                onDecommissionRack = { rack ->
                                    selectedRackForDecommission = Pair(dc, rack)
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                2 -> {
                    // AI Contracts Tab
                    item {
                        val operationalCompute = dataCenters.filter { it.isOperational }.sumOf { it.totalComputeTflops }
                        val committed = activeContracts.sumOf { it.requiredTflops }
                        val freeTflops = (operationalCompute - committed).coerceAtLeast(0.0)

                        ContractsOverviewSection(
                            activeContracts = activeContracts,
                            marketContracts = marketContracts,
                            availableFreeTflops = freeTflops,
                            onSignContract = { contractId ->
                                cloudViewModel.signContract(contractId) { success, msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            },
                            onCancelContract = { contractId ->
                                contractToTerminate = activeContracts.find { it.id == contractId }
                            }
                        )
                    }
                }

                3 -> {
                    // R&D Tech Tree Tab
                    item {
                        ResearchTechTreeSection(
                            unlockedTechIds = unlockedResearchIds,
                            companyBalance = companyBalance,
                            onUnlockTech = { techId ->
                                cloudViewModel.unlockResearch(techId) { success, msg ->
                                    scope.launch { snackbarHostState.showSnackbar(msg) }
                                }
                            }
                        )
                    }
                }

                4 -> {
                    // Financials & Incidents Tab
                    item {
                        FinancialAnalyticsSection(lastReport = lastReport)
                    }
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        IncidentsLogSection(incidents = incidents)
                    }
                }
            }
        }
    }

    // Build Data Center Dialog
    if (showBuildDcDialog) {
        BuildDataCenterDialog(
            companyBalance = companyBalance,
            onDismiss = { showBuildDcDialog = false },
            onConfirm = { name, region, tier, cooling, power ->
                cloudViewModel.buildDataCenter(name, region, tier, cooling, power) { success, msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                    if (success) showBuildDcDialog = false
                }
            }
        )
    }

    // Deploy Racks Dialog
    selectedDcForDeploy?.let { dc ->
        DeployRackDialog(
            dataCenter = dc,
            companyBalance = companyBalance,
            unlockedResearchIds = unlockedResearchIds,
            onDismiss = { selectedDcForDeploy = null },
            onConfirm = { gpuTier, quantity ->
                cloudViewModel.deployRacks(dc.id, gpuTier, quantity) { success, msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                    if (success) selectedDcForDeploy = null
                }
            }
        )
    }

    // Retrofit Plant Dialog
    selectedDcForRetrofit?.let { dc ->
        RetrofitPlantDialog(
            dataCenter = dc,
            companyBalance = companyBalance,
            onDismiss = { selectedDcForRetrofit = null },
            onUpgradeCooling = { cooling ->
                cloudViewModel.upgradeCooling(dc.id, cooling) { success, msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                    if (success) selectedDcForRetrofit = null
                }
            },
            onUpgradePower = { power ->
                cloudViewModel.upgradePower(dc.id, power) { success, msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                    if (success) selectedDcForRetrofit = null
                }
            }
        )
    }

    // Trade In GPU Rack Dialog
    selectedRackForTradeIn?.let { (dc, rack) ->
        TradeInRackDialog(
            rack = rack,
            companyBalance = companyBalance,
            unlockedResearchIds = unlockedResearchIds,
            onDismiss = { selectedRackForTradeIn = null },
            onConfirmTradeIn = { newTier ->
                cloudViewModel.tradeInRack(dc.id, rack.id, newTier) { success, msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                    if (success) selectedRackForTradeIn = null
                }
            }
        )
    }

    // Decommission / Resale Rack Dialog
    selectedRackForDecommission?.let { (dc, rack) ->
        DecommissionConfirmDialog(
            rack = rack,
            onDismiss = { selectedRackForDecommission = null },
            onConfirm = {
                cloudViewModel.decommissionRack(dc.id, rack.id) { success, msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
                    if (success) selectedRackForDecommission = null
                }
            }
        )
    }

    // Contract Termination Confirmation Dialog
    contractToTerminate?.let { contract ->
        AlertDialog(
            onDismissRequest = { contractToTerminate = null },
            title = {
                Text("Konfirmasi Pemutusan Kontrak", color = AiCloudColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Apakah Anda yakin ingin memutuskan kontrak SLA dengan ${contract.clientName}?",
                        color = AiCloudColors.TextPrimary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AiCloudColors.SurfaceHighlight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Dampak Pemutusan Kontrak:", fontSize = 11.sp, color = AiCloudColors.TextSecondary)
                            Text("• Pendapatan hilang: -$${String.format("%,d", contract.monthlyPaymentUsd)}/bln", fontSize = 12.sp, color = AiCloudColors.RedAlert)
                            Text("• Sisa durasi yang dibatalkan: ${contract.monthsRemaining} bulan", fontSize = 12.sp, color = AiCloudColors.TextSecondary)
                            Text("• Komputasi yang dibebaskan: +${String.format("%,.0f", contract.requiredTflops)} TFLOPS", fontSize = 12.sp, color = AiCloudColors.GreenNeon)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cId = contract.id
                        contractToTerminate = null
                        cloudViewModel.cancelContract(cId) { success, msg ->
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.RedAlert)
                ) {
                    Text("Ya, Putuskan Kontrak", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { contractToTerminate = null }) {
                    Text("Batal", color = AiCloudColors.TextSecondary)
                }
            },
            containerColor = AiCloudColors.CardBg
        )
    }

    // Rename Company Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Infrastructure Provider", color = AiCloudColors.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newNameInput,
                    onValueChange = { newNameInput = it },
                    label = { Text("Company Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AiCloudColors.CyanPrimary,
                        unfocusedBorderColor = AiCloudColors.CardBorder,
                        focusedTextColor = AiCloudColors.TextPrimary,
                        unfocusedTextColor = AiCloudColors.TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNameInput.isNotBlank()) {
                            cloudViewModel.updateName(newNameInput)
                            showRenameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.CyanPrimary)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel", color = AiCloudColors.TextSecondary) }
            },
            containerColor = AiCloudColors.CardBg
        )
    }

    // Capital Transfer Dialog
    if (showTransferDialog) {
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text("Capital & Dividend Transfer", color = AiCloudColors.TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { transferMode = "DEPOSIT" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (transferMode == "DEPOSIT") AiCloudColors.CyanPrimary else AiCloudColors.SurfaceHighlight
                            )
                        ) {
                            Text("Inject Capital", color = if (transferMode == "DEPOSIT") Color.Black else AiCloudColors.TextPrimary, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { transferMode = "WITHDRAW" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (transferMode == "WITHDRAW") AiCloudColors.GreenNeon else AiCloudColors.SurfaceHighlight
                            )
                        ) {
                            Text("Withdraw Dividend", color = if (transferMode == "WITHDRAW") Color.Black else AiCloudColors.TextPrimary, fontSize = 11.sp)
                        }
                    }

                    Text(
                        text = if (transferMode == "DEPOSIT")
                            "Personal Cash: $${String.format("%,d", playerState.cash)}"
                        else
                            "AI Cloud Treasury: $${String.format("%,d", companyBalance)}",
                        fontSize = 11.sp,
                        color = AiCloudColors.TextSecondary
                    )

                    OutlinedTextField(
                        value = transferAmountInput,
                        onValueChange = { transferAmountInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Amount (USD)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AiCloudColors.CyanPrimary,
                            unfocusedBorderColor = AiCloudColors.CardBorder,
                            focusedTextColor = AiCloudColors.TextPrimary,
                            unfocusedTextColor = AiCloudColors.TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = transferAmountInput.toLongOrNull() ?: 0L
                        if (amount > 0) {
                            if (transferMode == "DEPOSIT") {
                                val success = gameViewModel.injectCapitalToBusiness(businessInstanceId, amount)
                                if (success) {
                                    cloudViewModel.depositCapital(amount)
                                    scope.launch { snackbarHostState.showSnackbar("Successfully injected $${String.format("%,d", amount)} into AI Cloud treasury.") }
                                    showTransferDialog = false
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Insufficient funds to inject capital.") }
                                }
                            } else {
                                if (companyBalance >= amount) {
                                    val ok = cloudViewModel.withdrawCapital(amount)
                                    if (ok) {
                                        gameViewModel.withdrawCapitalFromBusiness(businessInstanceId, amount)
                                        scope.launch { snackbarHostState.showSnackbar("Successfully paid out $${String.format("%,d", amount)} dividend to personal balance.") }
                                        showTransferDialog = false
                                    }
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Insufficient company treasury balance.") }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.CyanPrimary)
                ) {
                    Text("Execute Transfer", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) { Text("Cancel", color = AiCloudColors.TextSecondary) }
            },
            containerColor = AiCloudColors.CardBg
        )
    }
}
