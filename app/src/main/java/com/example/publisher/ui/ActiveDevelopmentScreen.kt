package com.example.publisher.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.publisher.engine.GamePublisherEngine
import com.example.publisher.model.CreativeAdjustmentType
import com.example.publisher.model.GameProject
import com.example.publisher.model.TargetPlatform
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonGreen

/**
 * Screen 2: Active Development Screen.
 * Manages game projects in production, live progress tracking, budget injections,
 * platform selection matrix, and porting bug risk mitigation.
 */
@Composable
fun ActiveDevelopmentScreen(
    activeProjects: List<GameProject>,
    treasuryBalance: Long,
    projectForAdjustment: GameProject?,
    projectForQaInjection: GameProject?,
    projectForLaunchConfirm: GameProject?,
    onTogglePlatform: (projectId: String, platform: TargetPlatform) -> Unit,
    onOpenAdjustmentDialog: (GameProject) -> Unit,
    onCloseAdjustmentDialog: () -> Unit,
    onApplyAdjustment: (projectId: String, adjustment: CreativeAdjustmentType) -> Unit,
    onOpenQaDialog: (GameProject) -> Unit,
    onCloseQaDialog: () -> Unit,
    onInvestQa: (projectId: String, amount: Long) -> Unit,
    onOpenLaunchConfirm: (GameProject) -> Unit,
    onCloseLaunchConfirm: () -> Unit,
    onConfirmLaunch: (projectId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 84.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pipeline Produksi Game",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pantau progres bulanan, optimasi porting multi-platform & QA.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${activeProjects.size} Aktif",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            if (activeProjects.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tidak ada proyek dalam produksi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Kunjungi tab Inkubator untuk menandatangani kontrak studio indie baru atau membuat game in-house.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(activeProjects, key = { it.id }) { project ->
                    ActiveProjectCard(
                        project = project,
                        onTogglePlatform = { platform -> onTogglePlatform(project.id, platform) },
                        onAdjustmentClick = { onOpenAdjustmentDialog(project) },
                        onQaClick = { onOpenQaDialog(project) },
                        onLaunchClick = { onOpenLaunchConfirm(project) }
                    )
                }
            }
        }

        // Creative Adjustments Dialog Modal
        projectForAdjustment?.let { project ->
            CreativeAdjustmentDialog(
                project = project,
                treasuryBalance = treasuryBalance,
                onDismiss = onCloseAdjustmentDialog,
                onApply = { adj -> onApplyAdjustment(project.id, adj) }
            )
        }

        // QA Budget Injection Dialog Modal
        projectForQaInjection?.let { project ->
            QaInvestmentDialog(
                project = project,
                treasuryBalance = treasuryBalance,
                onDismiss = onCloseQaDialog,
                onConfirmInvest = { amount -> onInvestQa(project.id, amount) }
            )
        }

        // Launch Confirmation Dialog Modal
        projectForLaunchConfirm?.let { project ->
            LaunchConfirmationDialog(
                project = project,
                onDismiss = onCloseLaunchConfirm,
                onConfirmLaunch = { onConfirmLaunch(project.id) }
            )
        }
    }
}

/**
 * Card representing an actively developed game title.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveProjectCard(
    project: GameProject,
    onTogglePlatform: (TargetPlatform) -> Unit,
    onAdjustmentClick: () -> Unit,
    onQaClick: () -> Unit,
    onLaunchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val platformMetrics = GamePublisherEngine.computePlatformDistributionMetrics(project.targetPlatforms)

    val progressAnimated by animateFloatAsState(
        targetValue = project.currentProgress / 100.0f,
        label = "progress"
    )

    val isLaunchReady = project.currentProgress >= 100.0f
    val isHighBugRisk = project.bugRiskScore >= 35.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_project_${project.id}"),
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
            // Top Row: Title, Genre, Studio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Studio: ${project.studioName} • ${project.genre.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = project.fundingTier.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar & Month Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progres Produksi: ${(project.currentProgress).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Bulan ke-${project.currentMonthInDev} / ${project.devTimeMonths} Bln",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progressAnimated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isLaunchReady) NeonGreen else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Stats Row: Hype, Budget, QA, Royalti
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hype Komunitas", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("${project.hypeScore.toInt()}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Anggaran", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${project.totalBudget / 1_000}k", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("QA Terpasang", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${project.qaInvestment / 1_000}k", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bagi Hasil", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${(project.publisherRevenueShare * 100).toInt()}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Platform Selection Matrix & Porting Complexity Engine
            Text(
                text = "Platform Target & Matrix Porting:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TargetPlatform.entries.forEach { platform ->
                    val isSelected = project.targetPlatforms.contains(platform)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTogglePlatform(platform) },
                        label = { Text(platform.displayName, fontSize = 10.sp) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Porting Bug Risk Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Bug Risk",
                        tint = if (isHighBugRisk) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Resiko Bug Porting:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = "${project.bugRiskScore.toInt()} / 100",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        project.bugRiskScore >= 50.0 -> MaterialTheme.colorScheme.error
                        project.bugRiskScore >= 25.0 -> GoldAccent
                        else -> NeonGreen
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { (project.bugRiskScore / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    project.bugRiskScore >= 50.0 -> MaterialTheme.colorScheme.error
                    project.bugRiskScore >= 25.0 -> GoldAccent
                    else -> NeonGreen
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // High Bug Risk warning banner
            if (isHighBugRisk) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Perhatian: Porting multi-platform tanpa QA memadai akan menghancurkan skor review launch!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (Proper layout with compact padding to prevent vertical squish)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onAdjustmentClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("creative_adjust_button_${project.id}")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Arahan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onQaClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("inject_qa_button_${project.id}")
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Suntik QA", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onLaunchClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLaunchReady) NeonGreen else MaterialTheme.colorScheme.primary,
                        contentColor = if (isLaunchReady) Color.Black else MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("launch_game_button_${project.id}")
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isLaunchReady) "Rilis Global!" else "Rilis Awal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Creative Direction Adjustments Dialog.
 */
@Composable
fun CreativeAdjustmentDialog(
    project: GameProject,
    treasuryBalance: Long,
    onDismiss: () -> Unit,
    onApply: (CreativeAdjustmentType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Penyesuaian Arahan Kreatif",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${project.title} • Kas: $${treasuryBalance / 1_000}k",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CreativeAdjustmentType.entries.forEach { adj ->
                    val canAfford = treasuryBalance >= adj.costBonus
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = canAfford) { onApply(adj) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (canAfford) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (canAfford) MaterialTheme.colorScheme.outlineVariant else Color.Transparent
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = adj.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (adj.costBonus > 0) "+$${adj.costBonus / 1_000}k" else "Gratis",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAfford) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = adj.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

/**
 * QA Investment Budget Injection Dialog.
 */
@Composable
fun QaInvestmentDialog(
    project: GameProject,
    treasuryBalance: Long,
    onDismiss: () -> Unit,
    onConfirmInvest: (Long) -> Unit
) {
    val tierOptions = listOf(25_000L, 50_000L, 100_000L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Suntik Anggaran QA & Testing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${project.title} • Resiko Bug Saat Ini: ${project.bugRiskScore.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Injeksi tim QA tambahan untuk membersihkan bug porting sebelum rilis:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                tierOptions.forEach { amount ->
                    val canAfford = treasuryBalance >= amount
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = canAfford) { onConfirmInvest(amount) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (canAfford) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                                Text(
                                    text = "$${amount / 1_000}k QA Package",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Mengurangi ~${(amount / 3000).toInt()} poin resiko bug",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { onConfirmInvest(amount) },
                                enabled = canAfford,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(if (canAfford) "Suntik" else "Kas Kurang", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

/**
 * Launch Confirmation Modal Dialog.
 */
@Composable
fun LaunchConfirmationDialog(
    project: GameProject,
    onDismiss: () -> Unit,
    onConfirmLaunch: () -> Unit
) {
    val isUnderdeveloped = project.currentProgress < 100.0f
    val isHighBugRisk = project.bugRiskScore >= 35.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isUnderdeveloped) "Konfirmasi Rilis Dini (Early Access)" else "Konfirmasi Peluncuran Global",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Anda akan meluncurkan '${project.title}' ke platform global (${project.targetPlatforms.joinToString { it.displayName }}).",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (isUnderdeveloped || isHighBugRisk) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (isUnderdeveloped) {
                                Text(
                                    text = "• Progres game baru ${(project.currentProgress).toInt()}%. Rilis sebelum 100% akan memotong skor review!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 11.sp
                                )
                            }
                            if (isHighBugRisk) {
                                Text(
                                    text = "• Resiko bug tinggi (${project.bugRiskScore.toInt()}%). Pemain akan komplain performa buruk!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmLaunch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUnderdeveloped || isHighBugRisk) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Luncurkan Sekarang", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
