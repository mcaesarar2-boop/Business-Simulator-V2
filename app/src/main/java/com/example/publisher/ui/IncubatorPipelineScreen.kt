package com.example.publisher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.publisher.model.FundingTier
import com.example.publisher.model.GameGenre
import com.example.publisher.model.IndieStudio
import com.example.publisher.model.TargetPlatform
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonGreen

/**
 * Screen 1: Incubator Pipeline Screen.
 * Dedicated to scouting independent developer talent and producing in-house games.
 */
@Composable
fun IncubatorPipelineScreen(
    pitches: List<IndieStudio>,
    treasuryBalance: Long,
    studioToSign: IndieStudio?,
    showManualGameDialog: Boolean,
    onScoutPitches: () -> Unit,
    onOpenManualGameDialog: () -> Unit,
    onCloseManualGameDialog: () -> Unit,
    onConfirmCreateManualGame: (title: String, genre: GameGenre, tier: FundingTier, platforms: Set<TargetPlatform>) -> Unit,
    onOpenSignDialog: (IndieStudio) -> Unit,
    onCloseSignDialog: () -> Unit,
    onConfirmSign: (studioId: String, tier: FundingTier, revShare: Double, platforms: Set<TargetPlatform>) -> Unit,
    onRejectPitch: (studioId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // In-House Game Production Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inhouse_production_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gamepad,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "In-House Game Studio",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Rancang & produksi game original sendiri (100% Royalti)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Button(
                            onClick = onOpenManualGameDialog,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("create_manual_game_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Game Sendiri", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Header Bar with Scout Action (Fixed Layout: No Squishing!)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = "Indie Pitch Scouting",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Peluang proposal dari studio indie berbakat global",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    FilledTonalButton(
                        onClick = onScoutPitches,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("scout_pitches_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scout Talents",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Scout Baru", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }

            if (pitches.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tidak ada proposal pitch aktif",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Klik tombol 'Scout Baru' di atas untuk mencari studio indie baru atau rancang game original sendiri.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onScoutPitches,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Cari Studio Indie")
                            }
                        }
                    }
                }
            } else {
                items(pitches, key = { it.id }) { studio ->
                    IndieStudioPitchCard(
                        studio = studio,
                        onSignClicked = { onOpenSignDialog(studio) },
                        onRejectClicked = { onRejectPitch(studio.id) }
                    )
                }
            }
        }

        // Deal Negotiation Dialog
        studioToSign?.let { studio ->
            ContractNegotiationDialog(
                studio = studio,
                treasuryBalance = treasuryBalance,
                onDismiss = onCloseSignDialog,
                onConfirm = { tier, revShare, platforms ->
                    onConfirmSign(studio.id, tier, revShare, platforms)
                }
            )
        }

        // Manual / In-House Game Creation Dialog
        if (showManualGameDialog) {
            ManualGameCreationDialog(
                treasuryBalance = treasuryBalance,
                onDismiss = onCloseManualGameDialog,
                onConfirm = onConfirmCreateManualGame
            )
        }
    }
}

/**
 * Card representing an Indie Studio's pitch proposal.
 */
@Composable
fun IndieStudioPitchCard(
    studio: IndieStudio,
    onSignClicked: () -> Unit,
    onRejectClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pitch_card_${studio.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Studio Name & Talent Score Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = studio.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Didirikan ${studio.foundedYear} • ${studio.teamSize} Developer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Talent Score Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        studio.talentScore >= 90.0 -> GoldAccent.copy(alpha = 0.2f)
                        studio.talentScore >= 75.0 -> NeonGreen.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.primaryContainer
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (studio.talentScore >= 90.0) GoldAccent else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Talent Score",
                            tint = if (studio.talentScore >= 90.0) GoldAccent else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format("%.1f", studio.talentScore)} Talent",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Game Pitch Title
            Text(
                text = "“${studio.pitchTitle}”",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Genre & Tier Tags
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = studio.preferredGenre.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = "Target: ${studio.requestedTier.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Logline
            Text(
                text = studio.logline,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onRejectClicked,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("reject_pitch_${studio.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tolak",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tolak", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSignClicked,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("sign_pitch_${studio.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = "Negosiasi",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Negosiasi Kontrak", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Interactive In-House / Manual Game Creation Dialog
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManualGameCreationDialog(
    treasuryBalance: Long,
    onDismiss: () -> Unit,
    onConfirm: (title: String, genre: GameGenre, tier: FundingTier, platforms: Set<TargetPlatform>) -> Unit
) {
    var gameTitle by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf(GameGenre.CYBERPUNK) }
    var selectedTier by remember { mutableStateOf(FundingTier.STANDARD) }
    var selectedPlatforms by remember {
        mutableStateOf(setOf(TargetPlatform.PC_STEAM, TargetPlatform.PLAYSTATION_5))
    }

    val totalCost = selectedTier.baseBudget
    val canAfford = treasuryBalance >= totalCost

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Gamepad, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Rancang Game Sendiri (In-House)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = gameTitle,
                    onValueChange = { gameTitle = it },
                    label = { Text("Judul Game") },
                    placeholder = { Text("contoh: Project Stellar Nexus") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Pilih Genre:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameGenre.entries.forEach { genre ->
                        val isSelected = genre == selectedGenre
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGenre = genre },
                            label = { Text(genre.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Text(
                    text = "Pilih Skala Anggaran (Tier):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                FundingTier.entries.forEach { tier ->
                    val isSelected = tier == selectedTier
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTier = tier }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tier.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Estimasi: ${tier.baseDevMonths} Bulan • ${tier.riskDescription}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(
                                "$${tier.baseBudget / 1_000}k",
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Platform Rilis:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TargetPlatform.entries.forEach { platform ->
                        val isSelected = selectedPlatforms.contains(platform)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPlatforms = if (isSelected) {
                                    if (selectedPlatforms.size > 1) selectedPlatforms - platform else selectedPlatforms
                                } else {
                                    selectedPlatforms + platform
                                }
                            },
                            label = { Text(platform.displayName, fontSize = 11.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            } else null
                        )
                    }
                }

                // Summary Cost
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (canAfford) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Biaya Awal:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "$${String.format("%,d", totalCost)}",
                            fontWeight = FontWeight.Bold,
                            color = if (canAfford) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(gameTitle, selectedGenre, selectedTier, selectedPlatforms)
                },
                enabled = canAfford,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (canAfford) "Mulai Produksi Game" else "Kas Tidak Cukup", fontWeight = FontWeight.Bold)
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
 * Interactive Contract Negotiation Dialog with Funding Tier selection,
 * Revenue Share slider, Target Platform matrix pre-selection, and budget verification.
 */
@Composable
fun ContractNegotiationDialog(
    studio: IndieStudio,
    treasuryBalance: Long,
    onDismiss: () -> Unit,
    onConfirm: (FundingTier, Double, Set<TargetPlatform>) -> Unit
) {
    var selectedTier by remember { mutableStateOf(studio.requestedTier) }
    var revenueShare by remember { mutableStateOf(studio.requestedRevenueShare.toFloat()) }
    var selectedPlatforms by remember {
        mutableStateOf(setOf(TargetPlatform.PC_STEAM))
    }

    val cost = selectedTier.baseBudget
    val canAfford = treasuryBalance >= cost

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Negosiasi Kontrak Publisher",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${studio.name} • ${studio.pitchTitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Funding Tier Selection
                Text(
                    text = "1. Pilih Tier Pendanaan:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                FundingTier.entries.forEach { tier ->
                    val isSelected = tier == selectedTier
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTier = tier }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tier.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$${tier.baseBudget / 1_000}k Upfront",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Durasi Dev: ${tier.baseDevMonths} Bulan • ${tier.potentialDescription}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 2. Revenue Share Split
                Text(
                    text = "2. Bagi Hasil Royalti Publisher: ${(revenueShare * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = revenueShare,
                    onValueChange = { revenueShare = it },
                    valueRange = 0.35f..0.85f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("35% (Ramah Dev)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("85% (Publisher Dominan)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // 3. Platform Distribution
                Text(
                    text = "3. Target Platform Peluncuran:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TargetPlatform.entries.forEach { platform ->
                        val isSelected = selectedPlatforms.contains(platform)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPlatforms = if (isSelected) {
                                    if (selectedPlatforms.size > 1) selectedPlatforms - platform else selectedPlatforms
                                } else {
                                    selectedPlatforms + platform
                                }
                            },
                            label = { Text(platform.displayName, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                // Affordability Warning
                if (!canAfford) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kas internal tidak cukup ($${treasuryBalance / 1_000}k). Butuh $${cost / 1_000}k.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedTier, revenueShare.toDouble(), selectedPlatforms)
                },
                enabled = canAfford,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (canAfford) "Tandatangani Kontrak" else "Kas Tidak Cukup", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
