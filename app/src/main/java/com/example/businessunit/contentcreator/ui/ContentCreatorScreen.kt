package com.example.businessunit.contentcreator.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.businessunit.contentcreator.data.ContentCreatorRepository
import com.example.businessunit.contentcreator.engine.ContentCreatorActionHandler
import com.example.businessunit.contentcreator.engine.ContentProductionEngine
import com.example.businessunit.contentcreator.model.ActiveCreatorContract
import com.example.businessunit.contentcreator.model.BrandDealOffer
import com.example.businessunit.contentcreator.model.ContentWork
import com.example.businessunit.contentcreator.model.ProductionHouseOffer
import com.example.businessunit.contentcreator.ui.components.*
import com.example.ui.formatCurrencyRingkas
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentCreatorScreen(
    navController: NavController,
    viewModel: GameViewModel,
    instanceId: String? = null
) {
    val playerState by viewModel.playerState.collectAsState()
    val monthDurationSeconds by viewModel.monthDurationSeconds.collectAsState()
    val monthCycleProgress by viewModel.monthProgress.collectAsState()

    val business = remember(playerState, instanceId) {
        ContentCreatorActionHandler.getContentCreatorBusiness(playerState, instanceId)
    }

    val filmStudios = remember(playerState) {
        (playerState.ownedBusinesses.filter { it.catalogId == "media_production" } +
         playerState.holdingCompanies.flatMap { it.subsidiaries }.filter { it.catalogId == "media_production" }).distinctBy { it.instanceId }
    }

    // Modal dialog states
    var showProductionDialog by remember { mutableStateOf(false) }
    var latestProducedWork by remember { mutableStateOf<ContentWork?>(null) }
    var showCashDialogType by remember { mutableStateOf<String?>(null) } // "INJECT" or "WITHDRAW"
    var contractToTerminate by remember { mutableStateOf<ActiveCreatorContract?>(null) }
    var pendingBrandOffer by remember { mutableStateOf<BrandDealOffer?>(null) }
    var activePHOffer by remember { mutableStateOf<ProductionHouseOffer?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Pitch Sponsor Timers (10s offer countdown, 10s freeze cooldown)
    var offerSecondsRemaining by remember { mutableIntStateOf(10) }
    var freezeSecondsRemaining by remember { mutableIntStateOf(0) }
    val isFreezeCooldown = freezeSecondsRemaining > 0

    // Active Offer Countdown Ticker (10s -> 0s)
    LaunchedEffect(pendingBrandOffer) {
        if (pendingBrandOffer != null) {
            offerSecondsRemaining = 10
            while (offerSecondsRemaining > 0 && pendingBrandOffer != null) {
                delay(1000)
                offerSecondsRemaining -= 1
            }
            if (pendingBrandOffer != null) {
                // Offer expired -> Trigger 10s Freeze Cooldown
                pendingBrandOffer = null
                freezeSecondsRemaining = 10
            }
        }
    }

    // Freeze Cooldown Ticker (10s -> 0s)
    LaunchedEffect(freezeSecondsRemaining) {
        if (freezeSecondsRemaining > 0) {
            delay(1000)
            freezeSecondsRemaining -= 1
        }
    }

    // Periodic Background Brand Deal (Only when idle and not in freeze)
    LaunchedEffect(business?.level, business?.contentCreatorSubscribers, freezeSecondsRemaining) {
        if (business != null && pendingBrandOffer == null && freezeSecondsRemaining <= 0) {
            delay(25000)
            if (pendingBrandOffer == null && freezeSecondsRemaining <= 0) {
                pendingBrandOffer = ContentCreatorRepository.generateRandomBrandDeal(
                    level = business.level,
                    subscribers = business.contentCreatorSubscribers
                )
            }
        }
    }

    if (business == null) {
        Scaffold(containerColor = ContentCreatorTheme.BgDark) { padding ->
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Unit Usaha Content Creator tidak ditemukan.",
                    color = ContentCreatorTheme.TextWhite
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            ContentCreatorTopBar(
                onBack = { navController.popBackStack() },
                isCooldown = isFreezeCooldown,
                cooldownSeconds = freezeSecondsRemaining,
                hasActiveOffer = pendingBrandOffer != null,
                onPitchSponsor = {
                    if (!isFreezeCooldown && pendingBrandOffer == null) {
                        pendingBrandOffer = ContentCreatorRepository.generateRandomBrandDeal(
                            level = business.level,
                            subscribers = business.contentCreatorSubscribers
                        )
                    }
                }
            )
        },
        containerColor = ContentCreatorTheme.BgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Dashboard Channel Header
            item {
                ContentCreatorDashboardHeader(
                    business = business,
                    monthDurationSeconds = monthDurationSeconds,
                    monthCycleProgress = monthCycleProgress,
                    onSuntikModal = { showCashDialogType = "INJECT" },
                    onTarikProfit = { showCashDialogType = "WITHDRAW" }
                )
            }

            // 2. Upgrade & Skalabilitas Studio (Level 1-100, Crew, Office)
            item {
                ContentCreatorUpgradeCard(
                    business = business,
                    onLevelUp = { viewModel.levelUpContentCreator(business.instanceId) },
                    onHireEmployee = { viewModel.hireEmployeeContentCreator(business.instanceId) },
                    onUnlockOffice = { viewModel.unlockOfficeContentCreator(business.instanceId) }
                )
            }

            // 3. Active Sponsorship Contracts Card
            item {
                ContentCreatorContractsCard(
                    contracts = business.contentCreatorContracts,
                    onTerminateContract = { contractToTerminate = it }
                )
            }

            // 4. Sponsorship Offers Inbox (with 10s Timer)
            item {
                ContentCreatorInboxCard(
                    currentOffer = pendingBrandOffer,
                    remainingOfferSeconds = offerSecondsRemaining,
                    onAcceptOffer = { offer ->
                        viewModel.acceptContentCreatorBrandDealOffer(offer, business.instanceId)
                        pendingBrandOffer = null
                        freezeSecondsRemaining = 10 // Start 10s freeze cooldown after deal
                    },
                    onRejectOffer = {
                        pendingBrandOffer = null
                        freezeSecondsRemaining = 10 // Start 10s freeze cooldown after reject
                    }
                )
            }

            // 5. Bank Konten (Portfolio)
            item {
                ContentCreatorPortfolioCard(
                    portfolio = business.contentPortfolio,
                    onStartProductionClick = { showProductionDialog = true },
                    onPitchPHClick = { work ->
                        activePHOffer = ContentProductionEngine.generateTargetedOffer(work)
                    }
                )
            }

            // 6. Tutup & Likuidasi Unit Usaha Channel (Danger Zone)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
                    border = BorderStroke(1.dp, ContentCreatorTheme.ErrorRed.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = ContentCreatorTheme.ErrorRed, modifier = Modifier.size(20.dp))
                            Text("Zona Penutupan Unit Bisnis", fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = ContentCreatorTheme.TextWhite)
                        }
                        Text(
                            text = "Tutup channel YouTube dan likuidasi aset. Sisa kas unit sebesar ${formatCurrencyRingkas(business.contentCreatorCash, false)} akan otomatis ditransfer kembali ke kas utama holding.",
                            fontSize = 11.sp,
                            color = ContentCreatorTheme.TextGray,
                            lineHeight = 15.sp
                        )
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, ContentCreatorTheme.ErrorRed),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ContentCreatorTheme.ErrorRed),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tutup & Likuidasi Unit Channel", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- DIALOGS ---

    if (showProductionDialog) {
        ContentCreatorProductionDialog(
            creatorCash = business.contentCreatorCash,
            subscribers = business.contentCreatorSubscribers,
            filmStudios = filmStudios,
            onDismiss = { showProductionDialog = false },
            onStartProduction = { title, type, budget, studioId, scheme, focus ->
                val produced = viewModel.produceContentWork(
                    title = title,
                    type = type,
                    budget = budget,
                    targetStudioInstanceId = studioId,
                    fundingScheme = scheme,
                    targetCreatorInstanceId = business.instanceId,
                    creativeFocus = focus
                )
                showProductionDialog = false
                if (produced != null) {
                    latestProducedWork = produced
                }
            }
        )
    }

    latestProducedWork?.let { work ->
        ContentCreatorProductionResultDialog(
            work = work,
            onDismiss = { latestProducedWork = null },
            onPitchPH = {
                val offer = ContentProductionEngine.generateTargetedOffer(work)
                latestProducedWork = null
                activePHOffer = offer
            }
        )
    }

    showCashDialogType?.let { type ->
        val isInject = type == "INJECT"
        ContentCreatorCashTransferDialog(
            isInject = isInject,
            maxAvailable = if (isInject) playerState.cash else business.contentCreatorCash,
            onDismiss = { showCashDialogType = null },
            onConfirm = { amount ->
                if (isInject) {
                    viewModel.injectCashToContentCreator(amount, business.instanceId)
                } else {
                    viewModel.withdrawCashFromContentCreator(amount, business.instanceId)
                }
                showCashDialogType = null
            }
        )
    }

    contractToTerminate?.let { contract ->
        TerminateContractConfirmDialog(
            contract = contract,
            onDismiss = { contractToTerminate = null },
            onConfirm = {
                viewModel.terminateContentCreatorContract(
                    contractId = contract.id,
                    penaltyFee = contract.monthlyPayout / 2,
                    targetInstanceId = business.instanceId
                )
                contractToTerminate = null
            }
        )
    }

    activePHOffer?.let { offer ->
        ProductionHouseOfferDialog(
            offer = offer,
            onDismiss = { activePHOffer = null },
            onAcceptLumpSum = {
                viewModel.acceptContentCreatorPHOffer(offer, isLumpSum = true, targetInstanceId = business.instanceId)
                activePHOffer = null
            },
            onAcceptRoyalty = {
                viewModel.acceptContentCreatorPHOffer(offer, isLumpSum = false, targetInstanceId = business.instanceId)
                activePHOffer = null
            }
        )
    }

    if (showDeleteDialog) {
        DeleteChannelConfirmDialog(
            channelName = business.customName ?: business.name,
            refundCash = business.contentCreatorCash,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteContentCreatorBusiness(business.instanceId)
                navController.popBackStack()
            }
        )
    }
}
