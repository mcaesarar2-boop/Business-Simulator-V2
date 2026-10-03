package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.*
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FootballClubScreen(
    navController: NavController,
    viewModel: GameViewModel
) {
    val clubState by viewModel.footballClubState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Dialog states
    var showBudgetManagementDialog by remember { mutableStateOf(false) }
    var showInjectCapitalDialog by remember { mutableStateOf(false) }
    var showWithdrawDividendsDialog by remember { mutableStateOf(false) }
    var lastSimulatedMatch by remember { mutableStateOf<FootballMatch?>(null) }
    var showMatchResultDialog by remember { mutableStateOf(false) }

    // Match Simulation Loading & Deliberation States
    val coroutineScope = rememberCoroutineScope()
    var isSimulatingMatch by remember { mutableStateOf(false) }
    var simulationProgress by remember { mutableStateOf(0f) }
    var simulationMinuteText by remember { mutableStateOf("01'") }
    var simulationMessageText by remember { mutableStateOf("Menyiapkan taktik & formasi...") }
    var simulatingFixture by remember { mutableStateOf<FootballMatch?>(null) }

    val tabs = listOf(
        "Overview & Skuad",
        "Managerial Desk",
        "Bursa Transfer",
        "Finansial Klub",
        "Jadwal & Klasemen"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("football_club_screen"),
        containerColor = Color(0xFF0D131A),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (clubState.isInitialized) clubState.clubName else "Football Club Ownership",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        if (clubState.isInitialized) {
                            Text(
                                text = "${clubState.leagueName} (Level ${clubState.leagueLevel}) • Musim ${clubState.currentSeason}",
                                fontSize = 12.sp,
                                color = Color(0xFF4ADE80)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("football_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("football_club_acquisition") },
                        modifier = Modifier.testTag("football_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Ganti / Setup Klub",
                            tint = Color(0xFFFFD700)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF151D28))
            )
        },
        snackbarHost = {
            snackbarMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) {
                            Text("Tutup", color = Color(0xFFFFD700))
                        }
                    },
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color.White
                ) {
                    Text(msg)
                }
            }
        }
    ) { innerPadding ->
        if (!clubState.isInitialized) {
            // Uninitialized Welcome Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                    border = BorderStroke(1.dp, Color(0xFF2E3D52)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(Color(0xFF22C55E), Color(0xFF15803D)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Football Club Ownership",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Masuki peran sebagai Pemilik Klub (Chairman/Investor). Pilih klub impian Anda dari liga dunia, sewa manajer kelas elit, kendalikan bursa transfer, negosiasikan sponsor, dan bawa klub meraih promosi ke kasta tertinggi!",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.navigate("football_club_acquisition") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_club_setup_button")
                        ) {
                            Icon(Icons.Default.AddBusiness, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pilih Klub & Mulai Kepemilikan", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Main Dashboard with Tabs
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Global Chairman Status Banner with Transfer Budget + Button
                ChairmanStatusBanner(
                    clubState = clubState,
                    onOpenBudgetManagement = { showBudgetManagementDialog = true }
                )

                // Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF151D28),
                    contentColor = Color(0xFFFFD700),
                    edgePadding = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Color(0xFFFFD700) else Color(0xFF94A3B8)
                                )
                            },
                            modifier = Modifier.testTag("tab_$index")
                        )
                    }
                }

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> SquadOverviewTab(
                            clubState = clubState,
                            onSellPlayer = { playerId ->
                                val (ok, msg) = viewModel.sellFootballPlayer(playerId)
                                snackbarMessage = msg
                            }
                        )
                        1 -> ManagerialDeskTab(
                            clubState = clubState,
                            onOpenHireModal = { navController.navigate("football_manager_market") },
                            onFireManager = {
                                val (ok, msg) = viewModel.fireFootballManager()
                                snackbarMessage = msg
                            }
                        )
                        2 -> TransferScoutingTab(
                            clubState = clubState,
                            onApproveTransfer = { inboxId ->
                                val (ok, msg) = viewModel.approveFootballTransfer(inboxId)
                                snackbarMessage = msg
                            },
                            onRejectTransfer = { inboxId ->
                                val (ok, msg) = viewModel.rejectFootballTransfer(inboxId)
                                snackbarMessage = msg
                            },
                            onOpenScoutMarket = { navController.navigate("football_scouting_market") }
                        )
                        3 -> ClubFinancesTab(
                            clubState = clubState,
                            playerCash = playerState.cash,
                            onSignSponsor = { sponsor ->
                                val (ok, msg) = viewModel.signFootballSponsor(sponsor)
                                snackbarMessage = msg
                            },
                            onOpenInjectCapital = { showBudgetManagementDialog = true },
                            onOpenWithdrawDividends = { showWithdrawDividendsDialog = true }
                        )
                        4 -> SimulationStandingsTab(
                            clubState = clubState,
                            onSimulateMatch = {
                                val next = clubState.fixtures.firstOrNull { !it.isPlayed }
                                if (next != null) {
                                    simulatingFixture = next
                                    isSimulatingMatch = true
                                    coroutineScope.launch {
                                        simulationProgress = 0.08f
                                        simulationMinuteText = "05'"
                                        simulationMessageText = "🏟️ Kick-off! Wasit meniup peluit dimulainya pertandingan di stadion..."
                                        delay(500)

                                        simulationProgress = 0.32f
                                        simulationMinuteText = "28'"
                                        simulationMessageText = "⚔️ Duel sengit di lini tengah! Formasi taktik kedua tim saling adu pressing..."
                                        delay(500)

                                        simulationProgress = 0.52f
                                        simulationMinuteText = "45'"
                                        simulationMessageText = "⏱️ Peluit jeda babak pertama (HT) — Manajer memberikan arahan taktis di ruang ganti..."
                                        delay(500)

                                        simulationProgress = 0.74f
                                        simulationMinuteText = "68'"
                                        simulationMessageText = "⚡ Serangan balik cepat membahayakan! Peluang emas tercipta di mulut gawang..."
                                        delay(500)

                                        simulationProgress = 0.92f
                                        simulationMinuteText = "89'"
                                        simulationMessageText = "🔥 Menit krusial penentuan! Tensi laga memuncak diiringi sorak ribuan suporter..."
                                        delay(450)

                                        simulationProgress = 1.0f
                                        simulationMinuteText = "90+3'"
                                        simulationMessageText = "🏁 Peluit panjang ditiup wasit! Mengompilasi statistik pertandingan & klasemen..."
                                        delay(350)

                                        val match = viewModel.simulateNextFootballMatch()
                                        isSimulatingMatch = false
                                        if (match != null) {
                                            lastSimulatedMatch = match
                                            showMatchResultDialog = true
                                        }
                                    }
                                } else {
                                    snackbarMessage = "Seluruh pertandingan musim ini telah selesai."
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // BUDGET & CAPITAL MANAGEMENT DIALOG (Autonomous Manager Proposal + Direct Injection)
    if (showBudgetManagementDialog) {
        BudgetManagementDialog(
            clubState = clubState,
            playerState = playerState,
            viewModel = viewModel,
            navController = navController,
            onDismiss = { showBudgetManagementDialog = false },
            onMessage = { snackbarMessage = it }
        )
    }

    // INJECT CAPITAL DIALOG
    if (showInjectCapitalDialog) {
        InjectCapitalDialog(
            playerCash = playerState.cash,
            onDismiss = { showInjectCapitalDialog = false },
            onInject = { amount ->
                val (ok, msg) = viewModel.injectCapitalToFootballClub(amount)
                snackbarMessage = msg
                showInjectCapitalDialog = false
            }
        )
    }

    // WITHDRAW DIVIDENDS DIALOG
    if (showWithdrawDividendsDialog) {
        WithdrawDividendsDialog(
            clubBudget = clubState.transferBudget,
            onDismiss = { showWithdrawDividendsDialog = false },
            onWithdraw = { amount ->
                val (ok, msg) = viewModel.withdrawFootballDividends(amount)
                snackbarMessage = msg
                showWithdrawDividendsDialog = false
            }
        )
    }

    // MATCH SIMULATION ANIMATED LOADING DIALOG
    if (isSimulatingMatch && simulatingFixture != null) {
        MatchSimulationLoadingDialog(
            fixture = simulatingFixture!!,
            progress = simulationProgress,
            minuteText = simulationMinuteText,
            statusMessage = simulationMessageText
        )
    }

    // MATCH RESULT COMMENTARY DIALOG
    if (showMatchResultDialog && lastSimulatedMatch != null) {
        MatchResultDialog(
            match = lastSimulatedMatch!!,
            userClubName = clubState.clubName,
            onDismiss = { showMatchResultDialog = false }
        )
    }
}

// ==============================================================
// CHAIRMAN STATUS BANNER
// ==============================================================
@Composable
fun ChairmanStatusBanner(
    clubState: FootballClubState,
    onOpenBudgetManagement: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
        border = BorderStroke(1.dp, Color(0xFF2E3D52))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = clubState.customChairmanName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Pemilik ${clubState.clubName}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Club Reputation Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reputasi: ${clubState.clubReputation}/100",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4 Key Indicators with Clickable Transfer Budget Pill (+)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatPill(
                    title = "Club OVR",
                    value = "${clubState.starting11Ovr}",
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )

                // Transfer Budget Pill with + icon
                Surface(
                    modifier = Modifier
                        .weight(1.4f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenBudgetManagement() }
                        .testTag("transfer_budget_pill"),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF4ADE80).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF4ADE80).copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Transfer Budget", fontSize = 9.sp, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "€${String.format("%.1f", clubState.transferBudget / 1_000_000.0)}M",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF4ADE80),
                                maxLines = 1
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Kelola Anggaran",
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                StatPill(
                    title = "Sisa Gaji/Bln",
                    value = "€${String.format("%.1f", clubState.remainingMonthlyWageBudget / 1_000_000.0)}M",
                    color = if (clubState.remainingMonthlyWageBudget >= 0) Color(0xFFFBBF24) else Color(0xFFEF4444),
                    modifier = Modifier.weight(1.2f)
                )
                StatPill(
                    title = "Morale",
                    value = "${clubState.teamMorale}%",
                    color = Color(0xFFA78BFA),
                    modifier = Modifier.weight(0.9f)
                )
            }
        }
    }
}

@Composable
fun StatPill(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 9.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color, maxLines = 1)
        }
    }
}

// ==============================================================
// TAB 1: SQUAD OVERVIEW
// ==============================================================
@Composable
fun SquadOverviewTab(
    clubState: FootballClubState,
    onSellPlayer: (String) -> Unit
) {
    var selectedPositionFilter by remember { mutableStateOf("ALL") }
    val filteredSquad = remember(clubState.squad, selectedPositionFilter) {
        if (selectedPositionFilter == "ALL") clubState.squad
        else clubState.squad.filter { it.position == selectedPositionFilter }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("squad_overview_list")
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Squad stats summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D28)),
                border = BorderStroke(1.dp, Color(0xFF2E3D52))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Anggota Skuad", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text("${clubState.squad.size} Pemain (Minimal 16-24)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF22C55E).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Rata-rata OVR: ${clubState.starting11Ovr}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF22C55E),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Position Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "GK", "DEF", "MID", "FWD").forEach { pos ->
                    FilterChip(
                        selected = selectedPositionFilter == pos,
                        onClick = { selectedPositionFilter = pos },
                        label = { Text(pos, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF22C55E),
                            selectedLabelColor = Color.Black,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedPositionFilter == pos,
                            borderColor = Color(0xFF334155),
                            selectedBorderColor = Color(0xFF22C55E)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(filteredSquad) { player ->
            PlayerCard(
                player = player,
                canSell = clubState.squad.size > 16,
                onSell = { onSellPlayer(player.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun PlayerCard(
    player: FootballPlayer,
    canSell: Boolean,
    onSell: () -> Unit
) {
    var showConfirmSell by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
        border = BorderStroke(1.dp, Color(0xFF233246))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // OVR Badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                player.rating >= 88 -> Color(0xFFFFD700)
                                player.rating >= 80 -> Color(0xFF22C55E)
                                player.rating >= 70 -> Color(0xFF38BDF8)
                                else -> Color(0xFF94A3B8)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${player.rating}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (player.isRealPlayer) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF38BDF8).copy(alpha = 0.2f)
                            ) {
                                Text("Real", fontSize = 9.sp, color = Color(0xFF38BDF8), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${player.position} • Usia ${player.age} • ${player.nationality} • Taktik: ${player.tacticalFit}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Nilai: €${String.format("%,d", player.marketValue)} • Gaji: €${String.format("%,d", player.monthlyWage)}/bln",
                        fontSize = 11.sp,
                        color = Color(0xFF4ADE80),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            IconButton(
                onClick = { showConfirmSell = true },
                enabled = canSell,
                modifier = Modifier.testTag("sell_player_${player.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Jual Pemain",
                    tint = if (canSell) Color(0xFFFFD700) else Color.Gray
                )
            }
        }
    }

    if (showConfirmSell) {
        val proceeds = (player.marketValue * 0.90).toLong()
        AlertDialog(
            onDismissRequest = { showConfirmSell = false },
            title = { Text("Jual Pemain?") },
            text = {
                Text("Apakah Anda ingin melepas ${player.name} ke bursa transfer? Anda akan menerima 90% dari nilai pasar (€${String.format("%,d", proceeds)}) ke kas klub.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmSell = false
                    onSell()
                }) {
                    Text("Jual Pemain", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSell = false }) {
                    Text("Batal")
                }
            },
            containerColor = Color(0xFF1E293B),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1)
        )
    }
}

// ==============================================================
// TAB 2: MANAGERIAL DESK
// ==============================================================
@Composable
fun ManagerialDeskTab(
    clubState: FootballClubState,
    onOpenHireModal: () -> Unit,
    onFireManager: () -> Unit
) {
    var showFireConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("managerial_desk_screen")
    ) {
        item {
            val manager = clubState.hiredManager
            if (manager == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Posisi Manajer Kepala Kosong!",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Klub belum memiliki pelatih kepala. Rekrut manajer berkelas dunia dari database untuk meracik taktik, meningkatkan performa tim, dan memberikan rekomendasi pemain.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onOpenHireModal,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("hire_manager_button")
                        ) {
                            Text("Buka Bursa Rekrutmen Manajer", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Active Manager Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                    border = BorderStroke(1.dp, Color(0xFF2E3D52))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Brush.radialGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${manager.rating}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = manager.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${manager.country} • Mantan: ${manager.previousClubOrNation}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            // Morale indicator
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (manager.morale >= 70) Color(0xFF22C55E).copy(alpha = 0.2f)
                                else if (manager.morale >= 40) Color(0xFFFBBF24).copy(alpha = 0.2f)
                                else Color(0xFFEF4444).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Morale: ${manager.morale}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (manager.morale >= 70) Color(0xFF22C55E)
                                    else if (manager.morale >= 40) Color(0xFFFBBF24)
                                    else Color(0xFFEF4444),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFF233246))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Attributes
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Taktik Favorit", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(manager.favoriteTactic, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Gaji Bulanan", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text("€${String.format("%,d", manager.monthlySalary)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Rekam Jejak: ${manager.fameDescription}",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = onOpenHireModal,
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8))
                            ) {
                                Text("Ganti Pelatih", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showFireConfirmDialog = true },
                                modifier = Modifier.weight(1f).height(44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                            ) {
                                Text("Pecat Pelatih", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Tentang Otoritas Manajer & Chairman",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D28)),
                border = BorderStroke(1.dp, Color(0xFF233246))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "• Pelatih berating tinggi (seperti Pep Guardiola, Ancelotti, Klopp) memiliki reputasi tinggi dan hanya bersedia melatih klub dengan ClubReputation & Anggaran yang memadai.\n\n• Jika Chairman memaksakan pembelian pemain (Owner Dictation) yang tidak cocok dengan taktik Manajer, Morale Manajer akan anjlok.\n\n• Jika Morale Manajer turun drastis (<= 20%), Manajer dapat mengundurkan diri (Resign).",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    if (showFireConfirmDialog) {
        val severance = (clubState.hiredManager?.monthlySalary ?: 0L) * 3
        AlertDialog(
            onDismissRequest = { showFireConfirmDialog = false },
            title = { Text("Pecat Manajer?") },
            text = {
                Text("Memutus kontrak ${clubState.hiredManager?.name} akan mewajibkan pembayaran pesangon 3 bulan gaji sebesar €${String.format("%,d", severance)} dari anggaran transfer.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showFireConfirmDialog = false
                    onFireManager()
                }) {
                    Text("Pecat Sekarang", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFireConfirmDialog = false }) {
                    Text("Batal")
                }
            },
            containerColor = Color(0xFF1E293B),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1)
        )
    }
}

// ==============================================================
// TAB 3: TRANSFER & SCOUTING (TWO-WAY TRANSFERS)
// ==============================================================
@Composable
fun TransferScoutingTab(
    clubState: FootballClubState,
    onApproveTransfer: (String) -> Unit,
    onRejectTransfer: (String) -> Unit,
    onOpenScoutMarket: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("transfer_scouting_screen")
    ) {
        item {
            // Header actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Daftar Incaran Manajer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Interaksi Dua Arah (Manager Request vs Owner)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }

                Button(
                    onClick = onOpenScoutMarket,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("open_scouting_market_button")
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scouting Database", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (clubState.managerInbox.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                    border = BorderStroke(1.dp, Color(0xFF233246))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Inbox, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (clubState.hiredManager == null) "Rekrut Manajer untuk menerima permintaan transfer terarah."
                            else "Belum ada wishlist baru dari Manajer saat ini.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(clubState.managerInbox) { inboxItem ->
                ManagerInboxCard(
                    item = inboxItem,
                    onApprove = { onApproveTransfer(inboxItem.id) },
                    onReject = { onRejectTransfer(inboxItem.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D28)),
                border = BorderStroke(1.dp, Color(0xFF233246))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💡 Opsi Owner Dictation (Paksa Beli)", fontWeight = FontWeight.Bold, color = Color(0xFFFFD700), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Sebagai Chairman, Anda dapat membuka 'Scouting Database' dan membeli pemain manapun secara langsung. Namun hati-hati: jika pemain tersebut tidak selaras dengan taktik manajer, morale pelatih akan turun drastis!",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 17.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ManagerInboxCard(
    item: ManagerInboxItem,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val player = item.targetPlayer
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
        border = BorderStroke(
            1.dp,
            if (item.status == "APPROVED") Color(0xFF22C55E)
            else if (item.status == "REJECTED") Color(0xFFEF4444)
            else Color(0xFFFFD700).copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF22C55E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${player.rating}", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(player.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text("${player.position} • ${player.currentClub} • Usia ${player.age}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (item.status) {
                        "APPROVED" -> Color(0xFF22C55E).copy(alpha = 0.2f)
                        "REJECTED" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                        else -> Color(0xFFFFD700).copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = item.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (item.status) {
                            "APPROVED" -> Color(0xFF22C55E)
                            "REJECTED" -> Color(0xFFEF4444)
                            else -> Color(0xFFFFD700)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "💬 Catatan Manajer: \"${item.managerNote}\"",
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Harga: €${String.format("%,d", player.marketValue)} • Gaji: €${String.format("%,d", player.monthlyWage)}/bln",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4ADE80)
            )

            if (item.status == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Text("Approve (Beli)", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onReject,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Text("Reject (Tolak)", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ==============================================================
// TAB 4: CLUB FINANCES & SPONSORSHIP
// ==============================================================
@Composable
fun ClubFinancesTab(
    clubState: FootballClubState,
    playerCash: Long,
    onSignSponsor: (FootballSponsor) -> Unit,
    onOpenInjectCapital: () -> Unit,
    onOpenWithdrawDividends: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("club_finances_screen")
    ) {
        item {
            // Financial Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                border = BorderStroke(1.dp, Color(0xFF2E3D52))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ringkasan Keuangan Klub", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Kas / Transfer Budget", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("€${String.format("%,d", clubState.transferBudget)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Hak Siar TV (Bulanan)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("€${String.format("%,d", clubState.monthlyTvRights)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Gaji Skuad & Manager", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("€${String.format("%,d", clubState.totalMonthlyWages)} / bulan", fontSize = 13.sp, color = Color(0xFFF87171), fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pemasukan Tiket (Matchday)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            val matchdayRev = (clubState.stadiumCapacity * clubState.matchdayAttendanceRate * clubState.ticketPrice).toLong() * 2
                            Text("€${String.format("%,d", matchdayRev)} / bulan", fontSize = 13.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons to Inject / Withdraw
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onOpenInjectCapital,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Suntik Modal", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onOpenWithdrawDividends,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(42.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                            border = BorderStroke(1.dp, Color(0xFFFFD700))
                        ) {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tarik Dividen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Sponsorship Engine", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Nilai sponsor bergantung pada Kasta Liga (${clubState.leagueLevel}) dan Reputasi Klub (${clubState.clubReputation})", fontSize = 12.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Active Jersey Sponsor
        item {
            SponsorStatusCard(
                type = "JERSEY SPONSOR (Sponsor Baju)",
                sponsor = clubState.jerseySponsor
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Active Stadium Sponsor
        item {
            SponsorStatusCard(
                type = "STADIUM SPONSOR (Hak Nama Stadion)",
                sponsor = clubState.stadiumSponsor
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Available Sponsor Offers
        item {
            Text("Penawaran Sponsor Baru Masuk", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(clubState.availableSponsors) { offer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                border = BorderStroke(1.dp, Color(0xFF233246))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(offer.sponsorName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text("Kategori: ${offer.category} • Durasi: ${offer.contractYears} Tahun", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Nilai: €${String.format("%,d", offer.annualPayout)}/thn (€${String.format("%,d", offer.monthlyPayout)}/bln)", fontSize = 12.sp, color = Color(0xFF4ADE80), fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { onSignSponsor(offer) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Tanda Tangani", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SponsorStatusCard(type: String, sponsor: FootballSponsor?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151D28)),
        border = BorderStroke(1.dp, Color(0xFF233246))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(type, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(4.dp))
            if (sponsor == null) {
                Text("Belum ada sponsor aktif.", fontSize = 13.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(sponsor.sponsorName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("€${String.format("%,d", sponsor.monthlyPayout)} / bln", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4ADE80))
                }
            }
        }
    }
}

// ==============================================================
// TAB 5: SIMULATION & STANDINGS (AUTO-RESOLVE & PROMOTIONS)
// ==============================================================
@Composable
fun SimulationStandingsTab(
    clubState: FootballClubState,
    onSimulateMatch: () -> Unit
) {
    val nextMatch = clubState.fixtures.firstOrNull { !it.isPlayed }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("simulation_standings_screen")
    ) {
        item {
            // Next Match Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PERTANDINGAN BERIKUTNYA • MATCHDAY ${clubState.currentMatchday}/${clubState.totalMatchdays}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (nextMatch != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(nextMatch.homeTeam, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                                Text("Tuan Rumah", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                            Text("VS", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700))
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(nextMatch.awayTeam, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                                Text("Tamu", fontSize = 10.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onSimulateMatch,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("play_match_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulasi Pertandingan (Auto-Resolve)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("Seluruh pertandingan musim ini telah dimainkan!", fontSize = 14.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Text("Klasemen Liga: ${clubState.leagueName}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("#", modifier = Modifier.width(24.dp), fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                Text("Klub", modifier = Modifier.weight(1f), fontSize = 11.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                Text("P", modifier = Modifier.width(28.dp), fontSize = 11.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
                Text("W", modifier = Modifier.width(28.dp), fontSize = 11.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
                Text("D", modifier = Modifier.width(28.dp), fontSize = 11.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
                Text("L", modifier = Modifier.width(28.dp), fontSize = 11.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
                Text("GD", modifier = Modifier.width(32.dp), fontSize = 11.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center)
                Text("PTS", modifier = Modifier.width(36.dp), fontSize = 12.sp, color = Color(0xFFFFD700), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = Color(0xFF233246))
        }

        items(clubState.standings.size) { index ->
            val standing = clubState.standings[index]
            val isPromotedZone = index < 3 && clubState.leagueLevel > 1
            val isRelegatedZone = index >= (clubState.standings.size - 3)

            Surface(
                color = if (standing.isUserClub) Color(0xFF22C55E).copy(alpha = 0.15f)
                else if (isPromotedZone) Color(0xFF38BDF8).copy(alpha = 0.05f)
                else if (isRelegatedZone) Color(0xFFEF4444).copy(alpha = 0.05f)
                else Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}",
                        modifier = Modifier.width(24.dp),
                        fontSize = 11.sp,
                        fontWeight = if (standing.isUserClub) FontWeight.Bold else FontWeight.Normal,
                        color = if (isPromotedZone) Color(0xFF38BDF8)
                        else if (isRelegatedZone) Color(0xFFEF4444)
                        else Color.White
                    )
                    Text(
                        text = standing.clubName,
                        modifier = Modifier.weight(1f),
                        fontSize = 12.sp,
                        fontWeight = if (standing.isUserClub) FontWeight.Bold else FontWeight.Normal,
                        color = if (standing.isUserClub) Color(0xFF22C55E) else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("${standing.played}", modifier = Modifier.width(28.dp), fontSize = 11.sp, color = Color(0xFFCBD5E1), textAlign = TextAlign.Center)
                    Text("${standing.won}", modifier = Modifier.width(28.dp), fontSize = 11.sp, color = Color(0xFFCBD5E1), textAlign = TextAlign.Center)
                    Text("${standing.drawn}", modifier = Modifier.width(28.dp), fontSize = 11.sp, color = Color(0xFFCBD5E1), textAlign = TextAlign.Center)
                    Text("${standing.lost}", modifier = Modifier.width(28.dp), fontSize = 11.sp, color = Color(0xFFCBD5E1), textAlign = TextAlign.Center)
                    Text("${standing.goalDifference}", modifier = Modifier.width(32.dp), fontSize = 11.sp, color = Color(0xFFCBD5E1), textAlign = TextAlign.Center)
                    Text("${standing.points}", modifier = Modifier.width(36.dp), fontSize = 12.sp, color = Color(0xFFFFD700), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(color = Color(0xFF1E293B))
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF38BDF8)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Zona Promosi (Top 3)", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Zona Degradasi (Bottom 3)", fontSize = 10.sp, color = Color(0xFF94A3B8))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==============================================================
// MODAL & DIALOGS
// ==============================================================

// Budget & Capital Management Dialog (Autonomous Manager Proposal + Direct Injection)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetManagementDialog(
    clubState: FootballClubState,
    playerState: PlayerState,
    viewModel: GameViewModel,
    navController: NavController,
    onDismiss: () -> Unit,
    onMessage: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val proposal = remember(clubState) {
        FootballDatabase.generateManagerBudgetProposal(clubState)
    }

    var selectedHoldingId by remember { mutableStateOf<String?>(null) }
    val selectedHolding = playerState.holdingCompanies.find { it.instanceId == selectedHoldingId }
    val availableFunds = if (selectedHolding != null) {
        selectedHolding.holdingCash.toLong()
    } else {
        playerState.cash
    }

    var directAmountInput by remember { mutableStateOf("10000000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Pengelolaan Anggaran & Modal Klub",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp
                )
                Text(
                    text = "${clubState.clubName} • Transfer Budget: €${String.format("%,d", clubState.transferBudget)}",
                    fontSize = 11.sp,
                    color = Color(0xFF4ADE80)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF131D28),
                    contentColor = Color(0xFF22C55E)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Proposal Manajer",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Suntik Modal Mandiri",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Funding Source Selection (Available in both tabs)
                Text(
                    text = "Pilih Sumber Pendanaan:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { selectedHoldingId = null }
                        .background(if (selectedHoldingId == null) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFF16202D))
                        .border(1.dp, if (selectedHoldingId == null) Color(0xFF22C55E) else Color.DarkGray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selectedHoldingId == null, onClick = { selectedHoldingId = null })
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Kas Utama / Mega Holding Pribadi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Saldo: USD ${String.format("%,d", playerState.cash)}", fontSize = 11.sp, color = Color(0xFF4ADE80))
                    }
                }

                if (playerState.holdingCompanies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    playerState.holdingCompanies.forEach { holding ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedHoldingId = holding.instanceId }
                                .background(if (selectedHoldingId == holding.instanceId) Color(0xFF22C55E).copy(alpha = 0.15f) else Color(0xFF16202D))
                                .border(1.dp, if (selectedHoldingId == holding.instanceId) Color(0xFF22C55E) else Color.DarkGray, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedHoldingId == holding.instanceId, onClick = { selectedHoldingId = holding.instanceId })
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Holding: ${holding.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Kas Holding: USD ${String.format("%,d", holding.holdingCash.toLong())}", fontSize = 11.sp, color = Color(0xFFFFD700))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // TAB 0: AUTONOMOUS MANAGER PROPOSAL
                    val manager = clubState.hiredManager
                    if (manager == null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum Ada Manajer Utama",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Rekrut pelatih kepala dari Bursa Manajer agar mereka dapat mengevaluasi kelemahan skuad dan mengajukan anggaran belanja secara otonom.",
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        onDismiss()
                                        navController.navigate("football_manager_market")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Buka Bursa Manajer", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else if (proposal != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131F2E)),
                            border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Pengajuan dari ${manager.name}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Taktik: ${manager.favoriteTactic} • OVR: ${manager.rating}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF4ADE80)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF064E3B)
                                    ) {
                                        Text(
                                            text = "Lini ${proposal.targetPosition}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF34D399),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "\"${proposal.managerQuote}\"",
                                    fontSize = 12.sp,
                                    color = Color(0xFFE2E8F0),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = proposal.reasonExplanation,
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Nominal Pengajuan:", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            "USD ${String.format("%,d", proposal.requestedAmount)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = Color(0xFFFFD700)
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Estimasi Dampak:", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            proposal.estimatedSquadImpact,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                val canAffordProposal = availableFunds >= proposal.requestedAmount

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            val (ok, msg) = viewModel.approveAutonomousManagerBudget(
                                                proposal = proposal,
                                                source = if (selectedHoldingId != null) "HOLDING" else "CASH",
                                                holdingId = selectedHoldingId
                                            )
                                            onMessage(msg)
                                            if (ok) onDismiss()
                                        },
                                        enabled = canAffordProposal,
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF22C55E),
                                            disabledContainerColor = Color.DarkGray
                                        )
                                    ) {
                                        Text(
                                            text = if (canAffordProposal) "Setujui & Kucurkan" else "Saldo Kurang",
                                            color = if (canAffordProposal) Color.Black else Color.LightGray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val msg = viewModel.rejectAutonomousManagerBudget()
                                            onMessage(msg)
                                            onDismiss()
                                        },
                                        modifier = Modifier.weight(0.7f).height(40.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                                    ) {
                                        Text("Tolak", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: DIRECT CAPITAL INJECTION
                    Column {
                        Text(
                            text = "Suntikkan dana langsung dari kepemilikan Anda ke kas belanja transfer klub.",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Pilih Cepat Nominal (USD):", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1_000_000L to "+1M", 5_000_000L to "+5M", 20_000_000L to "+20M", 50_000_000L to "+50M").forEach { (amt, label) ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { directAmountInput = amt.toString() },
                                    color = if (directAmountInput == amt.toString()) Color(0xFF22C55E) else Color(0xFF1E293B)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (directAmountInput == amt.toString()) Color.Black else Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = directAmountInput,
                            onValueChange = { directAmountInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Nominal Suntikan Modal (USD)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF22C55E)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        val parsedAmt = directAmountInput.toLongOrNull() ?: 0L
                        val canAffordDirect = parsedAmt > 0 && availableFunds >= parsedAmt

                        Button(
                            onClick = {
                                val (ok, msg) = if (selectedHoldingId != null) {
                                    viewModel.injectCapitalFromHoldingToFootballClub(selectedHoldingId!!, parsedAmt)
                                } else {
                                    viewModel.injectCapitalToFootballClub(parsedAmt)
                                }
                                onMessage(msg)
                                if (ok) onDismiss()
                            },
                            enabled = canAffordDirect,
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF22C55E),
                                disabledContainerColor = Color.DarkGray
                            )
                        ) {
                            Text(
                                text = if (canAffordDirect) "Suntikkan USD ${String.format("%,d", parsedAmt)}" else "Dana Tidak Cukup",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (canAffordDirect) Color.Black else Color.LightGray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = Color.LightGray)
            }
        },
        containerColor = Color(0xFF0F172A)
    )
}

// 4. Inject Capital Dialog
@Composable
fun InjectCapitalDialog(
    playerCash: Long,
    onDismiss: () -> Unit,
    onInject: (Long) -> Unit
) {
    var amountInput by remember { mutableStateOf("10000000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Suntik Modal Pribadi ke Klub", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column {
                Text("Saldo Tunai Pribadi: €${String.format("%,d", playerCash)}", color = Color(0xFF4ADE80), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Nominal Suntikan Modal (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toLongOrNull() ?: 0L
                    onInject(amt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
            ) {
                Text("Transfer Dana", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        containerColor = Color(0xFF1E293B)
    )
}

// 5. Withdraw Dividends Dialog
@Composable
fun WithdrawDividendsDialog(
    clubBudget: Long,
    onDismiss: () -> Unit,
    onWithdraw: (Long) -> Unit
) {
    var amountInput by remember { mutableStateOf("5000000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tarik Dividen ke Rekening Pribadi", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column {
                Text("Kas Transfer Klub Tersedia: €${String.format("%,d", clubBudget)}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Nominal Dividen (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toLongOrNull() ?: 0L
                    onWithdraw(amt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
            ) {
                Text("Tarik Dividen", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        containerColor = Color(0xFF1E293B)
    )
}

// 6. Match Result Commentary Dialog (with Dynamic Upset Alert)
@Composable
fun MatchResultDialog(
    match: FootballMatch,
    userClubName: String,
    onDismiss: () -> Unit
) {
    val isUpset = match.matchSummary.contains("SENSASIONAL", ignoreCase = true) ||
        match.matchSummary.contains("KEJUTAN", ignoreCase = true) ||
        match.matchSummary.contains("UPSET", ignoreCase = true) ||
        match.matchEvents.any { it.contains("GIANT KILLING", ignoreCase = true) || it.contains("UPSET", ignoreCase = true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = null,
                    tint = Color(0xFF22C55E),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hasil Akhir Pertandingan", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 17.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                // Notifikasi Upset jika ada kejutan dramatis (15%-20% Dynamic Chance)
                if (isUpset) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF78350F)),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B))
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("🔥 UPSET DRAMATIS (GIANT KILLER)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                Text("Peluang kejutan terwujud! Determinasi tim underdog berhasil mengguncang prediksi di atas kertas.", fontSize = 10.sp, color = Color(0xFFFDE68A), lineHeight = 14.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Scoreboard Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151D28)),
                    border = BorderStroke(1.dp, Color(0xFF2E3D52))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = match.homeTeam,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${match.homeScore ?: 0} - ${match.awayScore ?: 0}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD700),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            Text(
                                text = match.awayTeam,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = match.matchSummary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isUpset) Color(0xFFFFD700) else Color(0xFF4ADE80),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Komentar Jalannya Pertandingan:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))

                match.matchEvents.forEach { ev ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A)
                    ) {
                        Text(
                            text = ev,
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
            ) {
                Text("Tutup & Lanjutkan", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}

// 7. Match Simulation Animated Loading Dialog (Cycle Icon & In-Game Time Engine)
@Composable
fun MatchSimulationLoadingDialog(
    fixture: FootballMatch,
    progress: Float,
    minuteText: String,
    statusMessage: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sim_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    AlertDialog(
        onDismissRequest = { /* Modal non-cancelable during in-game match */ },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Sync,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(spinAngle)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SIMULASI PERTANDINGAN",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8),
                    letterSpacing = 1.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Team Matchup
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(fixture.homeTeam, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                            Text("Tuan Rumah", fontSize = 10.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("VS", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD700))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(fixture.awayTeam, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                            Text("Tim Tamu", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // In-Game Minute Display
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF22C55E).copy(alpha = 0.25f), Color(0xFF0F172A))
                            )
                        )
                        .border(2.dp, Color(0xFF22C55E), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SportsSoccer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp).rotate(spinAngle)
                        )
                        Text(
                            text = minuteText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF22C55E),
                    trackColor = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Ticker commentary message
                Text(
                    text = statusMessage,
                    fontSize = 12.sp,
                    color = Color(0xFFE2E8F0),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF16202E)
    )
}
