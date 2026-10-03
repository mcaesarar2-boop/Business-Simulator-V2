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
fun FootballManagerMarketScreen(
    navController: NavController,
    viewModel: GameViewModel
) {
    val clubState by viewModel.footballClubState.collectAsState()
    val allManagers = remember { FootballDatabase.managers }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTacticalStyle by remember { mutableStateOf("ALL") }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }
    var negotiatingManager by remember { mutableStateOf<FootballManager?>(null) }

    val tacticalStyles = listOf(
        "ALL" to "Semua Taktik",
        "TIKI_TAKA" to "Tiki-Taka",
        "GEGENPRESS" to "Gegenpressing",
        "FLUID_DIAMOND" to "Fluid Diamond",
        "COUNTER" to "Direct Counter",
        "CATENACCIO" to "Catenaccio",
        "POSITIONAL" to "Positional Play"
    )

    val filteredManagers = remember(searchQuery, selectedTacticalStyle) {
        allManagers.filter { manager ->
            val matchesSearch = searchQuery.isBlank() ||
                manager.name.contains(searchQuery, ignoreCase = true) ||
                manager.country.contains(searchQuery, ignoreCase = true) ||
                manager.previousClubOrNation.contains(searchQuery, ignoreCase = true)

            val matchesTactic = selectedTacticalStyle == "ALL" || manager.tacticalStyle == selectedTacticalStyle
            matchesSearch && matchesTactic
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("football_manager_market_screen"),
        containerColor = Color(0xFF090E14),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bursa Pelatih & Manajer Dunia",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${filteredManagers.size} dari ${allManagers.size} Pelatih Top Tersedia",
                            fontSize = 12.sp,
                            color = Color(0xFF4ADE80)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("manager_market_back")
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
            // 1. Club Status Banner
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
                                    text = clubState.clubName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${clubState.leagueName} • Pelatih Saat Ini: ${clubState.hiredManager?.name ?: "Kosong (Interim)"}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4ADE80)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF064E3B)
                            ) {
                                Text(
                                    text = "Reputasi: ${clubState.clubReputation}/100",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Transfer Budget: €${String.format("%,d", clubState.transferBudget)}",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                            Text(
                                text = "Sisa Wage: €${String.format("%,d", clubState.remainingMonthlyWageBudget)}/bln",
                                fontSize = 11.sp,
                                color = if (clubState.remainingMonthlyWageBudget >= 0) Color(0xFF34D399) else Color(0xFFF87171)
                            )
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
                    placeholder = { Text("Cari pelatih, negara, klub lama (Pep, Klopp, dsb)...") },
                    modifier = Modifier.fillMaxWidth().testTag("manager_search_field"),
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

            // 3. Tactical Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("tactic_filters_row")
                ) {
                    items(tacticalStyles) { (styleKey, styleLabel) ->
                        val isSelected = selectedTacticalStyle == styleKey
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedTacticalStyle = styleKey }
                                .testTag("tactic_chip_$styleKey"),
                            color = if (isSelected) Color(0xFF22C55E) else Color(0xFF1E293B),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFF4ADE80) else Color.Transparent)
                        ) {
                            Text(
                                text = styleLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 4. List of Managers
            items(filteredManagers) { manager ->
                val isCurrentlyHired = clubState.hiredManager?.id == manager.id
                val meetsRep = clubState.clubReputation >= manager.minClubReputationRequired
                val meetsBudget = clubState.transferBudget >= manager.minTransferBudgetRequired
                val canHire = meetsRep && meetsBudget

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manager_card_${manager.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentlyHired) Color(0xFF0F3020) else Color(0xFF131D28)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isCurrentlyHired) Color(0xFF22C55E) else Color.White.copy(alpha = 0.08f)
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
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF22C55E), Color(0xFF065F46))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${manager.rating}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = manager.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${manager.country} • Klub/Negara: ${manager.previousClubOrNation}",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            if (isCurrentlyHired) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF22C55E)
                                ) {
                                    Text(
                                        text = "Aktif",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "\"${manager.fameDescription}\"",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Filosofi Taktik:",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = manager.favoriteTactic,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4ADE80)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Gaji Bulanan:",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "€${String.format("%,d", manager.monthlySalary)}/bln",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        // Requirements Evaluation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (meetsRep) Color(0xFF064E3B) else Color(0xFF450A0A),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (meetsRep) "✓ Reputasi Cukup (${manager.minClubReputationRequired}+)" else "✕ Reputasi Kurang (${manager.minClubReputationRequired}+)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (meetsRep) Color(0xFF34D399) else Color(0xFFFCA5A5),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (meetsBudget) Color(0xFF064E3B) else Color(0xFF450A0A),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (meetsBudget) "✓ Budget Memadai" else "✕ Min Budget €${String.format("%.1f", manager.minTransferBudgetRequired / 1_000_000.0)}M",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (meetsBudget) Color(0xFF34D399) else Color(0xFFFCA5A5),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        if (!isCurrentlyHired) {
                            Button(
                                onClick = {
                                    negotiatingManager = manager
                                },
                                enabled = canHire,
                                modifier = Modifier.fillMaxWidth().height(42.dp).testTag("hire_button_${manager.id}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF22C55E),
                                    disabledContainerColor = Color.DarkGray
                                )
                            ) {
                                Icon(Icons.Default.Handshake, contentDescription = null, tint = if (canHire) Color.Black else Color.LightGray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (canHire) "Tawarkan Kontrak & Negosiasi" else "Tawaran Ditolak (Syarat Tidak Terpenuhi)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (canHire) Color.Black else Color.LightGray
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

    // DIALOG NEGOSIASI KONTRAK MANAJER DENGAN WAKTU IN-GAME
    if (negotiatingManager != null) {
        ManagerContractNegotiationDialog(
            manager = negotiatingManager!!,
            clubState = clubState,
            viewModel = viewModel,
            onDismiss = { negotiatingManager = null },
            onSigned = { success, msg ->
                feedbackMessage = msg
                isSuccessMessage = success
                negotiatingManager = null
            }
        )
    }
}

// ==============================================================
// MANAGER CONTRACT NEGOTIATION DIALOG (DECISION TIME & RANDOM LOGIC)
// ==============================================================
@Composable
fun ManagerContractNegotiationDialog(
    manager: FootballManager,
    clubState: FootballClubState,
    viewModel: GameViewModel,
    onDismiss: () -> Unit,
    onSigned: (Boolean, String) -> Unit
) {
    var salaryPercentAdjustment by remember { mutableStateOf(0) } // -15, -10, 0, +10, +20
    var bonusTrophyOption by remember { mutableStateOf(0L) } // 0, 500k, 1M, 2M
    var contractDurationYears by remember { mutableStateOf(2) }

    var isDeliberating by remember { mutableStateOf(false) }
    var deliberationSecondsLeft by remember { mutableStateOf(60) } // In-game 1 minute ticker
    var outcome by remember { mutableStateOf<ManagerNegotiationOutcome?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val calculatedSalary = (manager.monthlySalary * (1.0 + salaryPercentAdjustment / 100.0)).toLong()

    // Animasi putaran ikon saat waktu negosiasi berlangsung
    val infiniteTransition = rememberInfiniteTransition(label = "deliberation_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    AlertDialog(
        onDismissRequest = {
            if (!isDeliberating) onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Handshake, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Negosiasi Kontrak Manajer", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Text("${manager.name} (${manager.rating} OVR • ${manager.favoriteTactic})", fontSize = 11.sp, color = Color.LightGray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Info ringkas manajer
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131D28))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Gaji Standar Agen:", fontSize = 11.sp, color = Color.Gray)
                            Text("€${String.format("%,d", manager.monthlySalary)}/bln", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sisa Pagu Gaji Klub:", fontSize = 11.sp, color = Color.Gray)
                            Text("€${String.format("%,d", clubState.remainingMonthlyWageBudget)}/bln", fontSize = 11.sp, color = Color(0xFF4ADE80))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Reputasi Klub Anda:", fontSize = 11.sp, color = Color.Gray)
                            Text("${clubState.clubReputation}/100", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // KONDISI 1: SEDANG PROSES KEPUTUSAN (WAKTU IN-GAME 1 MENIT)
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
                                text = "Agen & ${manager.name} sedang mempelajari klausul kontrak dan reputasi klub di ruang perundingan...",
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
                                        NegotiationStatus.DISCOUNT_SURPRISE -> "Kejutan: Diskon Kontrak Diterima!"
                                        NegotiationStatus.ACCEPTED -> "Tawaran Kontrak Disetujui!"
                                        NegotiationStatus.COUNTER_OFFER -> "Agen Mengajukan Counter-Offer"
                                        NegotiationStatus.REJECTED -> "Negosiasi Berakhir Buntu"
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
                                        Text("Klausul Tandingan dari Agen:", fontSize = 10.sp, color = Color.LightGray)
                                        Text("• Gaji Diinginkan: €${String.format("%,d", res.counterSalary)}/bln", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                        Text("• Bonus Prestasi: €${String.format("%,d", res.counterBonus)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // KONDISI 3: FORM TAWARAN CHAIRMAN
                    Text("1. Penyesuaian Nilai Gaji Pokok:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            -15 to "-15%",
                            -10 to "-10%",
                            0 to "Standar",
                            10 to "+10%",
                            20 to "+20%"
                        ).forEach { (pct, label) ->
                            val isSel = salaryPercentAdjustment == pct
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { salaryPercentAdjustment = pct },
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

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nilai yang Diajukan: €${String.format("%,d", calculatedSalary)}/bulan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("2. Bonus Target Trofi Juara:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            0L to "Tanpa Bonus",
                            500_000L to "+€500K",
                            1_000_000L to "+€1M",
                            2_000_000L to "+€2M"
                        ).forEach { (amt, label) ->
                            val isSel = bonusTrophyOption == amt
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { bonusTrophyOption = amt },
                                color = if (isSel) Color(0xFF38BDF8) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFF7DD3FC) else Color.Transparent)
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
                    Text("3. Durasi Kontrak Kerja:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1 to "1 Tahun (Uji Coba)", 2 to "2 Tahun (Standar)", 3 to "3 Tahun (Proyek)").forEach { (yrs, label) ->
                            val isSel = contractDurationYears == yrs
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { contractDurationYears = yrs },
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
                        text = "ℹ️ Algoritma Negosiasi Realistis: Pelatih dapat menerima diskon jika menyukai reputasi klub, langsung sepakat, atau meminta klausul tandingan.",
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        lineHeight = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            if (outcome == null && !isDeliberating) {
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

                            outcome = viewModel.evaluateManagerNegotiation(
                                manager = manager,
                                offeredSalary = calculatedSalary,
                                offeredBonus = bonusTrophyOption
                            )
                            isDeliberating = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) {
                    Text("Kirim Negosiasi Kontrak (1 Menit In-Game)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else if (outcome != null) {
                val res = outcome!!
                when (res.status) {
                    NegotiationStatus.DISCOUNT_SURPRISE, NegotiationStatus.ACCEPTED -> {
                        Button(
                            onClick = {
                                val (ok, msg) = viewModel.hireFootballManagerWithNegotiation(
                                    manager = manager,
                                    agreedSalary = res.negotiatedSalary,
                                    agreedBonus = res.negotiatedBonus
                                )
                                onSigned(ok, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                        ) {
                            Text("Tandatangani Kontrak Resmi (€${String.format("%,d", res.negotiatedSalary)}/bln)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    NegotiationStatus.COUNTER_OFFER -> {
                        Button(
                            onClick = {
                                val (ok, msg) = viewModel.hireFootballManagerWithNegotiation(
                                    manager = manager,
                                    agreedSalary = res.counterSalary,
                                    agreedBonus = res.counterBonus
                                )
                                onSigned(ok, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                        ) {
                            Text("Terima Counter-Offer & Kontrak", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                    Text(if (outcome != null && outcome?.status == NegotiationStatus.COUNTER_OFFER) "Tolak Tawaran & Batal" else "Batal", color = Color.Gray)
                }
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}
