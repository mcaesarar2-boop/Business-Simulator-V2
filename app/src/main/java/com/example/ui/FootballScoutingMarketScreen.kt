package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.*
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FootballScoutingMarketScreen(
    navController: NavController,
    viewModel: GameViewModel
) {
    val clubState by viewModel.footballClubState.collectAsState()
    val allPlayers = remember { FootballDatabase.realPlayers }

    var searchQuery by remember { mutableStateOf("") }
    var selectedPosition by remember { mutableStateOf("ALL") }
    var sortBy by remember { mutableStateOf("OVR_DESC") }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }
    var negotiatingPlayer by remember { mutableStateOf<FootballPlayer?>(null) }

    val positions = listOf("ALL", "GK", "DEF", "MID", "FWD")

    val filteredPlayers = remember(searchQuery, selectedPosition, sortBy, clubState.squad) {
        val list = allPlayers.filter { player ->
            val matchesSearch = searchQuery.isBlank() ||
                player.name.contains(searchQuery, ignoreCase = true) ||
                player.currentClub.contains(searchQuery, ignoreCase = true) ||
                player.nationality.contains(searchQuery, ignoreCase = true)

            val matchesPos = selectedPosition == "ALL" || player.position.equals(selectedPosition, ignoreCase = true)
            matchesSearch && matchesPos
        }

        when (sortBy) {
            "OVR_DESC" -> list.sortedByDescending { it.rating }
            "VALUE_DESC" -> list.sortedByDescending { it.marketValue }
            "WAGE_ASC" -> list.sortedBy { it.monthlyWage }
            else -> list.sortedByDescending { it.rating }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("football_scouting_screen"),
        containerColor = Color(0xFF090E14),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bursa Transfer & Scouting Global",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${filteredPlayers.size} Pemain Bintang Dunia Terdaftar",
                            fontSize = 12.sp,
                            color = Color(0xFF4ADE80)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("scouting_market_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Financial Status Header
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D28)),
                    border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Anggaran Belanja Tersedia",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = "€${String.format("%,d", clubState.transferBudget)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color(0xFFFFD700)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Sisa Wage Budget",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                                Text(
                                    text = "€${String.format("%,d", clubState.remainingMonthlyWageBudget)}/bln",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (clubState.remainingMonthlyWageBudget >= 0) Color(0xFF34D399) else Color(0xFFF87171)
                                )
                            }
                        }

                        if (clubState.hiredManager != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFF4ADE80), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Taktik Manajer (${clubState.hiredManager?.name}): ${clubState.hiredManager?.favoriteTactic}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Feedback Message Banner
            if (feedbackMessage != null) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSuccessMessage) Color(0xFF065F46) else Color(0xFF7F1D1D)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isSuccessMessage) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feedbackMessage ?: "",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Search Field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari bintang dunia (Haaland, Bellingham, Rodri, dsb)...") },
                    modifier = Modifier.fillMaxWidth().testTag("player_search_field"),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF22C55E),
                        unfocusedBorderColor = Color.DarkGray
                    )
                )
            }

            // 3. Position Filter Tabs & Sort Options
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f).testTag("position_filters_row")
                    ) {
                        items(positions) { pos ->
                            val isSelected = selectedPosition == pos
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedPosition = pos },
                                color = if (isSelected) Color(0xFF22C55E) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF4ADE80) else Color.Transparent)
                            ) {
                                Text(
                                    text = pos,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Sort button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                sortBy = when (sortBy) {
                                    "OVR_DESC" -> "VALUE_DESC"
                                    "VALUE_DESC" -> "WAGE_ASC"
                                    else -> "OVR_DESC"
                                }
                            },
                        color = Color(0xFF1E293B)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (sortBy) {
                                    "OVR_DESC" -> "OVR ↓"
                                    "VALUE_DESC" -> "Harga ↓"
                                    else -> "Gaji ↑"
                                },
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 4. List of Players
            items(filteredPlayers) { player ->
                val isAlreadyOwned = clubState.squad.any { it.name.equals(player.name, ignoreCase = true) }
                val canAffordFee = clubState.transferBudget >= player.marketValue
                val canAffordWage = clubState.remainingMonthlyWageBudget >= player.monthlyWage

                val isTacticalFit = clubState.hiredManager != null && (
                    player.tacticalFit.equals(clubState.hiredManager?.tacticalStyle, ignoreCase = true) ||
                    player.tacticalFit == "All-Round"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scout_player_card_${player.name}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAlreadyOwned) Color(0xFF132018) else Color(0xFF131D28)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isAlreadyOwned) Color(0xFF22C55E).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${player.rating}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = player.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        if (player.isRealPlayer) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF0284C7)
                                            ) {
                                                Text(
                                                    text = "STAR",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${player.position} • ${player.age} thn • ${player.nationality} (${player.currentClub})",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "€${String.format("%,d", player.marketValue)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFFFD700)
                                )
                                Text(
                                    text = "€${String.format("%,d", player.monthlyWage)}/bln",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }
                        }

                        // Tactical Synergy Tag
                        if (clubState.hiredManager != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isTacticalFit) Color(0xFF064E3B) else Color(0xFF450A0A)
                            ) {
                                Text(
                                    text = if (isTacticalFit)
                                        "✓ Sinergi Taktik: Cocok dengan filosofi ${clubState.hiredManager?.name}"
                                    else
                                        "⚠ Mismatch: Beda gaya dengan taktik ${clubState.hiredManager?.name}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isTacticalFit) Color(0xFF34D399) else Color(0xFFFCA5A5),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        if (isAlreadyOwned) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF064E3B)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("Sudah Terdaftar di Skuad Anda", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    negotiatingPlayer = player
                                },
                                enabled = canAffordFee,
                                modifier = Modifier.fillMaxWidth().height(40.dp).testTag("buy_player_button_${player.name}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF22C55E),
                                    disabledContainerColor = Color.DarkGray
                                )
                            ) {
                                Icon(Icons.Default.Handshake, contentDescription = null, tint = if (canAffordFee) Color.Black else Color.LightGray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (canAffordFee) "Negosiasi & Beli Pemain" else "Budget Belanja Tidak Cukup",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (canAffordFee) Color.Black else Color.LightGray
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // DIALOG NEGOSIASI TRANSFER PEMAIN DENGAN WAKTU IN-GAME
    if (negotiatingPlayer != null) {
        PlayerTransferNegotiationDialog(
            player = negotiatingPlayer!!,
            clubState = clubState,
            viewModel = viewModel,
            onDismiss = { negotiatingPlayer = null },
            onBought = { success, msg ->
                feedbackMessage = msg
                isSuccessMessage = success
                negotiatingPlayer = null
            }
        )
    }
}

// ==============================================================
// PLAYER TRANSFER NEGOTIATION DIALOG (DECISION TIME & DYNAMIC OUTCOME)
// ==============================================================
@Composable
fun PlayerTransferNegotiationDialog(
    player: FootballPlayer,
    clubState: FootballClubState,
    viewModel: GameViewModel,
    onDismiss: () -> Unit,
    onBought: (Boolean, String) -> Unit
) {
    var feeAdjustmentPct by remember { mutableStateOf(0) } // -15, -10, 0, +10, +20
    var wageAdjustmentPct by remember { mutableStateOf(0) } // -10, 0, +15
    var signingBonusOption by remember { mutableStateOf(0L) } // 0, 500k, 1M, 2.5M

    var isDeliberating by remember { mutableStateOf(false) }
    var deliberationSecondsLeft by remember { mutableStateOf(60) } // In-game 1 minute ticker
    var outcome by remember { mutableStateOf<PlayerNegotiationOutcome?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val calculatedFee = (player.marketValue * (1.0 + feeAdjustmentPct / 100.0)).toLong()
    val calculatedWage = (player.monthlyWage * (1.0 + wageAdjustmentPct / 100.0)).toLong()

    val infiniteTransition = rememberInfiniteTransition(label = "player_deliberation_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle_player"
    )

    AlertDialog(
        onDismissRequest = {
            if (!isDeliberating) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SportsSoccer, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Negosiasi Transfer Pemain", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Text("${player.name} (${player.position} • ${player.rating} OVR • ${player.currentClub})", fontSize = 11.sp, color = Color.LightGray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Info ringkas harga pasar & budget
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D28))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Nilai Pasar Resmi:", fontSize = 11.sp, color = Color.Gray)
                            Text("€${String.format("%,d", player.marketValue)}", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Gaji Standar Pemain:", fontSize = 11.sp, color = Color.Gray)
                            Text("€${String.format("%,d", player.monthlyWage)}/bln", fontSize = 11.sp, color = Color.White)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kas Belanja Klub:", fontSize = 11.sp, color = Color.Gray)
                            Text("€${String.format("%,d", clubState.transferBudget)}", fontSize = 11.sp, color = Color(0xFF4ADE80), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // KONDISI 1: SEDANG PROSES KEPUTUSAN IN-GAME
                if (isDeliberating) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier
                                    .size(40.dp)
                                    .rotate(spinAngle)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "WAKTU KEPUTUSAN IN-GAME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "00:${if (deliberationSecondsLeft < 10) "0$deliberationSecondsLeft" else "$deliberationSecondsLeft"}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (60 - deliberationSecondsLeft) / 60f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF38BDF8),
                                trackColor = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Direktur olahraga ${player.currentClub} dan agen ${player.name} sedang mempelajari dokumen tawaran...",
                                fontSize = 11.sp,
                                color = Color.LightGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (outcome != null) {
                    // KONDISI 2: HASIL NEGOSIASI TERSEDIA
                    val res = outcome!!
                    val cardBg = when (res.status) {
                        NegotiationStatus.DISCOUNT_SURPRISE, NegotiationStatus.ACCEPTED -> Color(0xFF064E3B)
                        NegotiationStatus.COUNTER_OFFER -> Color(0xFF78350F)
                        NegotiationStatus.REJECTED -> Color(0xFF7F1D1D)
                    }
                    val badgeColor = when (res.status) {
                        NegotiationStatus.DISCOUNT_SURPRISE, NegotiationStatus.ACCEPTED -> Color(0xFF22C55E)
                        NegotiationStatus.COUNTER_OFFER -> Color(0xFFF59E0B)
                        NegotiationStatus.REJECTED -> Color(0xFFEF4444)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    when (res.status) {
                                        NegotiationStatus.DISCOUNT_SURPRISE, NegotiationStatus.ACCEPTED -> Icons.Default.CheckCircle
                                        NegotiationStatus.COUNTER_OFFER -> Icons.Default.Cached
                                        NegotiationStatus.REJECTED -> Icons.Default.Cancel
                                    },
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (res.status) {
                                        NegotiationStatus.DISCOUNT_SURPRISE -> "Kejutan: Klub Menerima Diskon FFP!"
                                        NegotiationStatus.ACCEPTED -> "Tawaran Transfer Disetujui Penuh!"
                                        NegotiationStatus.COUNTER_OFFER -> "Klub Penjual Mengajukan Counter"
                                        NegotiationStatus.REJECTED -> "Tawaran Transfer Ditolak"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = res.narrative,
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 16.sp
                            )

                            if (res.status == NegotiationStatus.COUNTER_OFFER) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.3f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Klausul Tandingan Penjual:", fontSize = 10.sp, color = Color.LightGray)
                                        Text("• Biaya Transfer Diminta: €${String.format("%,d", res.counterFee)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                        Text("• Tuntutan Gaji: €${String.format("%,d", res.counterWage)}/bln", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // KONDISI 3: FORM TAWARAN TRANSFER
                    Text("1. Tawaran Biaya Transfer (Transfer Fee):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            -15 to "-15%",
                            -10 to "-10%",
                            0 to "Pasar",
                            10 to "+10%",
                            20 to "+20%"
                        ).forEach { (pct, label) ->
                            val isSel = feeAdjustmentPct == pct
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { feeAdjustmentPct = pct },
                                color = if (isSel) Color(0xFF22C55E) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFF4ADE80) else Color.Transparent)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Transfer Fee Diajukan: €${String.format("%,d", calculatedFee)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("2. Penyesuaian Gaji Pemain:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            -10 to "-10% (Hemat)",
                            0 to "Standar (€${String.format("%,d", player.monthlyWage)})",
                            15 to "+15% (Insentif Bintang)"
                        ).forEach { (pct, label) ->
                            val isSel = wageAdjustmentPct == pct
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { wageAdjustmentPct = pct },
                                color = if (isSel) Color(0xFF38BDF8) else Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("3. Bonus Penandatanganan (Signing-on Bonus):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            0L to "€0",
                            500_000L to "+€500K",
                            1_000_000L to "+€1M",
                            2_500_000L to "+€2.5M"
                        ).forEach { (amt, label) ->
                            val isSel = signingBonusOption == amt
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { signingBonusOption = amt },
                                color = if (isSel) Color(0xFFA855F7) else Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else Color.LightGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ℹ️ Algoritma Negosiasi Realistis: Klub penjual dapat menerima diskon transfer jika butuh likuiditas darurat, menerima langsung, atau meminta counter-offer.",
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        lineHeight = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            if (outcome == null && !isDeliberating) {
                val totalCost = calculatedFee + signingBonusOption
                val canSubmit = clubState.transferBudget >= totalCost
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isDeliberating = true
                            deliberationSecondsLeft = 60
                            delay(500)
                            deliberationSecondsLeft = 45
                            delay(500)
                            deliberationSecondsLeft = 30
                            delay(500)
                            deliberationSecondsLeft = 15
                            delay(400)
                            deliberationSecondsLeft = 0
                            delay(200)

                            outcome = viewModel.evaluatePlayerNegotiation(
                                player = player,
                                offeredFee = calculatedFee,
                                offeredWage = calculatedWage,
                                signingBonus = signingBonusOption
                            )
                            isDeliberating = false
                        }
                    },
                    enabled = canSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) {
                    Text(
                        text = if (canSubmit) "Kirim Proposal Transfer (1 Menit In-Game)" else "Budget Belanja Kurang",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            } else if (outcome != null) {
                val res = outcome!!
                when (res.status) {
                    NegotiationStatus.DISCOUNT_SURPRISE, NegotiationStatus.ACCEPTED -> {
                        Button(
                            onClick = {
                                val (ok, msg) = viewModel.forceBuyFootballPlayer(
                                    player = player,
                                    agreedFee = res.negotiatedFee,
                                    agreedWage = res.negotiatedWage,
                                    signingBonus = res.negotiatedBonus
                                )
                                onBought(ok, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                        ) {
                            Text("Selesaikan Transfer (€${String.format("%,d", res.negotiatedFee)})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    NegotiationStatus.COUNTER_OFFER -> {
                        val canAffordCounter = clubState.transferBudget >= (res.counterFee + res.negotiatedBonus)
                        Button(
                            onClick = {
                                val (ok, msg) = viewModel.forceBuyFootballPlayer(
                                    player = player,
                                    agreedFee = res.counterFee,
                                    agreedWage = res.counterWage,
                                    signingBonus = res.negotiatedBonus
                                )
                                onBought(ok, msg)
                            },
                            enabled = canAffordCounter,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                        ) {
                            Text(
                                text = if (canAffordCounter) "Terima Counter & Beli (€${String.format("%,d", res.counterFee)})" else "Kas Tidak Cukup Untuk Counter",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    NegotiationStatus.REJECTED -> {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("Tutup Negosiasi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        dismissButton = {
            if (!isDeliberating) {
                TextButton(onClick = onDismiss) {
                    Text(if (outcome != null && outcome?.status == NegotiationStatus.COUNTER_OFFER) "Batalkan Transfer" else "Batal", color = Color.Gray)
                }
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}
