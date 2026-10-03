package com.example.publisher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.publisher.engine.LaunchFinancialResult
import com.example.publisher.viewmodel.GamePublisherViewModel
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonGreen

private const val BANNER_IMAGE_URL =
    "https://images.unsplash.com/photo-1604586376807-f73185cf5867?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"

/**
 * Main Publisher Hub Screen featuring:
 * - Hero Banner Image
 * - Publisher Treasury & Reputation HUD
 * - Cash Injection & Dividend Transfer Dialog
 * - Settings Menu with Business Liquidation / Deletion
 * - 3 Dedicated Screen Tabs:
 *   1. IncubatorPipelineScreen (with In-House manual game creation)
 *   2. ActiveDevelopmentScreen
 *   3. ReleasedCatalogScreen
 * - Launch Celebration & Payout Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePublisherHubScreen(
    viewModel: GamePublisherViewModel = viewModel(),
    onNavigateBack: (() -> Unit)? = null,
    onLiquidateBusiness: ((Long) -> Unit)? = null,
    onInjectCash: ((Long) -> Unit)? = null,
    onWithdrawCash: ((Long) -> Unit)? = null,
    playerCash: Long = 10_000_000L,
    holdingCash: Long = 25_000_000L,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val pitches by viewModel.pitches.collectAsState()
    val activeProjects by viewModel.activeProjects.collectAsState()
    val releasedProjects by viewModel.releasedProjects.collectAsState()
    val treasury by viewModel.publisherTreasury.collectAsState()
    val reputation by viewModel.publisherReputation.collectAsState()
    val lifetimeProfit by viewModel.lifetimePublisherProfit.collectAsState()
    val currentMonth by viewModel.currentSimulationMonth.collectAsState()
    val lastLaunchResult by viewModel.lastLaunchResult.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showOptionsMenu by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.eventMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Game Publisher & Incubator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali"
                            )
                        }
                    }
                },
                actions = {
                    // Quick Transfer Cash Action
                    IconButton(
                        onClick = { viewModel.openTransferCashDialog() },
                        modifier = Modifier.testTag("transfer_cash_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Transfer Kas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Gear Settings Action for Liquidation
                    IconButton(
                        onClick = { showOptionsMenu = true },
                        modifier = Modifier.testTag("publisher_settings_gear")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan Bisnis"
                        )
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Suntik / Tarik Modal")
                                }
                            },
                            onClick = {
                                showOptionsMenu = false
                                viewModel.openTransferCashDialog()
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteForever,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Likuidasi / Hapus Bisnis", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            onClick = {
                                showOptionsMenu = false
                                viewModel.openLiquidationDialog()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero Image Banner with Gradient Overlay & HUD Metrics
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = BANNER_IMAGE_URL,
                    contentDescription = "Game Publisher Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark Gradient Scrim Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.45f),
                                    Color.Black.copy(alpha = 0.88f)
                                )
                            )
                        )
                )

                // Top Info Badge: Month & Reputation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Reputasi",
                                tint = GoldAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reputasi: $reputation/100",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Bulan",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bulan ke-$currentMonth",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // HUD Financial Metrics Row at bottom of Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Treasury Balance with quick Transfer Chip
                    Column {
                        Text(
                            text = "Kas Internal Publisher",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$${formatMoney(treasury)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                modifier = Modifier.clickable { viewModel.openTransferCashDialog() }
                            ) {
                                Text(
                                    text = "Transfer",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Total Publisher Earnings
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Laba Bersih",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                        Text(
                            text = "$${formatMoney(lifetimeProfit)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }
                }
            }

            // 3-Tab Primary Navigation Bar with Badges
            TabRow(
                selectedTabIndex = uiState.selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = uiState.selectedTabIndex == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Inkubator", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            if (pitches.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("${pitches.size}", fontSize = 10.sp)
                                }
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )

                Tab(
                    selected = uiState.selectedTabIndex == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Produksi", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            if (activeProjects.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(containerColor = NeonGreen) {
                                    Text("${activeProjects.size}", fontSize = 10.sp, color = Color.Black)
                                }
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )

                Tab(
                    selected = uiState.selectedTabIndex == 2,
                    onClick = { viewModel.selectTab(2) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Katalog Rilis", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            if (releasedProjects.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(containerColor = GoldAccent) {
                                    Text("${releasedProjects.size}", fontSize = 10.sp, color = Color.Black)
                                }
                            }
                        }
                    },
                    icon = {
                        Icon(Icons.Default.FolderSpecial, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }

            // Screen Content Routing Based on Selected Tab
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiState.selectedTabIndex) {
                    0 -> IncubatorPipelineScreen(
                        pitches = pitches,
                        treasuryBalance = treasury,
                        studioToSign = uiState.studioToSign,
                        showManualGameDialog = uiState.showManualGameDialog,
                        onScoutPitches = { viewModel.scoutPitches() },
                        onOpenManualGameDialog = { viewModel.openManualGameDialog() },
                        onCloseManualGameDialog = { viewModel.closeManualGameDialog() },
                        onConfirmCreateManualGame = { title, genre, tier, platforms ->
                            viewModel.createInHouseGame(title, genre, tier, platforms)
                        },
                        onOpenSignDialog = { studio -> viewModel.openSignContractDialog(studio) },
                        onCloseSignDialog = { viewModel.closeSignContractDialog() },
                        onConfirmSign = { studioId, tier, revShare, platforms ->
                            viewModel.confirmSignContract(studioId, tier, revShare, platforms)
                        },
                        onRejectPitch = { studioId -> viewModel.rejectPitch(studioId) }
                    )

                    1 -> ActiveDevelopmentScreen(
                        activeProjects = activeProjects,
                        treasuryBalance = treasury,
                        projectForAdjustment = uiState.projectForAdjustment,
                        projectForQaInjection = uiState.projectForQaInjection,
                        projectForLaunchConfirm = uiState.showLaunchConfirmationFor,
                        onTogglePlatform = { projectId, platform ->
                            viewModel.togglePlatform(projectId, platform)
                        },
                        onOpenAdjustmentDialog = { project -> viewModel.openAdjustmentDialog(project) },
                        onCloseAdjustmentDialog = { viewModel.closeAdjustmentDialog() },
                        onApplyAdjustment = { projectId, adj ->
                            viewModel.applyCreativeAdjustment(projectId, adj)
                        },
                        onOpenQaDialog = { project -> viewModel.openQaDialog(project) },
                        onCloseQaDialog = { viewModel.closeQaDialog() },
                        onInvestQa = { projectId, amount -> viewModel.investQa(projectId, amount) },
                        onOpenLaunchConfirm = { project -> viewModel.openLaunchConfirmation(project) },
                        onCloseLaunchConfirm = { viewModel.closeLaunchConfirmation() },
                        onConfirmLaunch = { projectId -> viewModel.launchGame(projectId) }
                    )

                    2 -> ReleasedCatalogScreen(
                        releasedProjects = releasedProjects,
                        selectedProjectDetails = uiState.projectForDetails,
                        onOpenDetails = { project -> viewModel.openProjectDetails(project) },
                        onCloseDetails = { viewModel.closeProjectDetails() },
                        onDeployPatch = { projectId -> viewModel.deployPatch(projectId) }
                    )
                }
            }
        }

        // Worldwide Launch Celebration & Payout Dialog Modal
        lastLaunchResult?.let { result ->
            LaunchSuccessDialog(
                result = result,
                onDismiss = { viewModel.dismissLaunchResultModal() }
            )
        }

        // Transfer / Suntik & Tarik Modal Dialog Modal
        if (uiState.showTransferCashDialog) {
            CashTransferDialog(
                publisherCash = treasury,
                playerCash = playerCash,
                holdingCash = holdingCash,
                onDismiss = { viewModel.closeTransferCashDialog() },
                onInjectCash = { amount ->
                    viewModel.repository.injectCapital(amount)
                    onInjectCash?.invoke(amount)
                    viewModel.closeTransferCashDialog()
                },
                onWithdrawCash = { amount ->
                    val success = viewModel.repository.withdrawCapital(amount)
                    if (success) {
                        onWithdrawCash?.invoke(amount)
                        viewModel.closeTransferCashDialog()
                    }
                }
            )
        }

        // Liquidation & Delete Business Dialog Modal
        if (uiState.showLiquidationDialog) {
            val valuation = viewModel.calculateValuation()
            BusinessLiquidationDialog(
                valuation = valuation,
                onDismiss = { viewModel.closeLiquidationDialog() },
                onConfirmLiquidate = {
                    viewModel.closeLiquidationDialog()
                    onLiquidateBusiness?.invoke(valuation)
                    onNavigateBack?.invoke()
                }
            )
        }
    }
}

/**
 * Cash Transfer Dialog: Inject funds from Holding / Personal or Withdraw Dividends to Holding / Personal.
 */
@Composable
fun CashTransferDialog(
    publisherCash: Long,
    playerCash: Long,
    holdingCash: Long,
    onDismiss: () -> Unit,
    onInjectCash: (Long) -> Unit,
    onWithdrawCash: (Long) -> Unit
) {
    var transferMode by remember { mutableStateOf(0) } // 0: Suntik Modal (Masuk), 1: Tarik Dana (Keluar)
    var amountText by remember { mutableStateOf("500000") }

    val amountLong = amountText.toLongOrNull() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Transfer Modal & Dividen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mode Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { transferMode = 0 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (transferMode == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (transferMode == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Suntik Modal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { transferMode = 1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (transferMode == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (transferMode == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Tarik Dividen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Balance summary
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kas Internal Publisher:", fontSize = 11.sp)
                            Text("$${formatMoney(publisherCash)}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NeonGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kas Holding / Mega Holding:", fontSize = 11.sp)
                            Text("$${formatMoney(holdingCash)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Amount text field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Jumlah Nominal ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(100_000L, 500_000L, 1_000_000L, 5_000_000L).forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { amountText = preset.toString() }
                        ) {
                            Text(
                                text = "$${preset / 1_000}k",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (transferMode == 0) {
                        onInjectCash(amountLong)
                    } else {
                        onWithdrawCash(amountLong)
                    }
                },
                enabled = amountLong > 0 && (transferMode == 0 || publisherCash >= amountLong),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (transferMode == 0) "Konfirmasi Suntik Modal" else "Konfirmasi Tarik Dividen",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

/**
 * Liquidation Dialog: Confirm business deletion and asset liquidation.
 */
@Composable
fun BusinessLiquidationDialog(
    valuation: Long,
    onDismiss: () -> Unit,
    onConfirmLiquidate: () -> Unit
) {
    var confirmationInput by remember { mutableStateOf("") }
    val isConfirmed = confirmationInput.trim().equals("JUAL", ignoreCase = true) ||
            confirmationInput.trim().equals("HAPUS", ignoreCase = true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Likuidasi & Hapus Bisnis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Apakah Anda yakin ingin menjual dan melikuidasi seluruh unit usaha Game Publisher ini?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Estimasi Nilai Likuidasi Aset:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "$${formatMoney(valuation)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Seluruh kas, katalog game terbit, dan proyek produksi akan dilikuidasi menjadi uang tunai.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Text(
                    text = "Ketik 'JUAL' atau 'HAPUS' untuk konfirmasi:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = confirmationInput,
                    onValueChange = { confirmationInput = it },
                    placeholder = { Text("Ketik JUAL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmLiquidate,
                enabled = isConfirmed,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Likuidasi Sekarang", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

/**
 * Worldwide Launch Success Celebration Modal Dialog.
 */
@Composable
fun LaunchSuccessDialog(
    result: LaunchFinancialResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Peluncuran Global Sukses!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Game resmi tersedia di toko digital global!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Metacritic Review Score Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            result.reviewScore >= 85 -> Color(0xFF43A047).copy(alpha = 0.2f)
                            result.reviewScore >= 70 -> Color(0xFFFDD835).copy(alpha = 0.2f)
                            else -> Color(0xFFE53935).copy(alpha = 0.2f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Skor Ulasan Kritikus", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = when {
                                    result.reviewScore >= 85 -> "Karya Mahakarya Universal"
                                    result.reviewScore >= 70 -> "Ulasan Positif & Populer"
                                    result.reviewScore >= 50 -> "Ulasan Beragam"
                                    else -> "Respon Negatif & Penuh Bug"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "${result.reviewScore}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = when {
                                result.reviewScore >= 85 -> Color(0xFF43A047)
                                result.reviewScore >= 70 -> Color(0xFFFDD835)
                                else -> Color(0xFFE53935)
                            }
                        )
                    }
                }

                // Financial Breakdown Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Unit Terjual (Minggu 1):", style = MaterialTheme.typography.bodySmall)
                            Text(formatNumber(result.unitsSold), fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pendapatan Kotor Global:", style = MaterialTheme.typography.bodySmall)
                            Text("$${formatMoney(result.finalRevenue)}", fontWeight = FontWeight.Bold, color = GoldAccent)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Potongan Platform (30% Steam/Console):", style = MaterialTheme.typography.bodySmall)
                            Text("-$${formatMoney(result.platformFeeDeduction)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Royalti Tim Studio:", style = MaterialTheme.typography.bodySmall)
                            Text("-$${formatMoney(result.studioPayout)}", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Laba Bersih Publisher:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("+$${formatMoney(result.publisherProfit)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = NeonGreen)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Klaim Keuntungan", fontWeight = FontWeight.Bold)
            }
        }
    )
}

private fun formatNumber(number: Long): String {
    return when {
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.1fk", number / 1_000.0)
        else -> number.toString()
    }
}

private fun formatMoney(amount: Long): String {
    return when {
        amount >= 1_000_000 -> String.format("%.2fM", amount / 1_000_000.0)
        amount >= 1_000 -> String.format("%.0fk", amount / 1_000.0)
        else -> amount.toString()
    }
}
