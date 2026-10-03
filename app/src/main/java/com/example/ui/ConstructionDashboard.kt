package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.viewmodel.ConstructionEngine
import kotlin.math.roundToLong

@Composable
fun ConstructionDashboard(
    business: OwnedBusiness,
    playerCash: Long,
    useShortFormat: Boolean,
    hasLogisticsSynergy: Boolean,
    onAllocatePhase: (String) -> String?,
    onSubmitBid: (String, Long, Boolean, Boolean) -> Pair<ConstructionEngine.BiddingResult?, String?>,
    onResolveEvent: (String, String) -> String?,
    onUpgradeCapacity: (String, Boolean) -> String?,
    onRefreshMarket: () -> String?,
    // Legacy support
    onStartTender: ((String, Long, Int, Long, Boolean) -> String?)? = null,
    onTakeClientProject: ((String) -> String?)? = null
) {
    val context = LocalContext.current
    var selectedTenderForBid by remember { mutableStateOf<ConstructionTenderOpportunity?>(null) }
    var biddingResultDialog by remember { mutableStateOf<ConstructionEngine.BiddingResult?>(null) }
    var showCapacityUpgradeDialog by remember { mutableStateOf(false) }
    var selectedEventProject by remember { mutableStateOf<ConstructionProject?>(null) }

    val usedCrews = ConstructionEngine.getUsedCrews(business)
    val totalCrews = business.constructionData.maxCrews
    val availableCrews = (totalCrews - usedCrews).coerceAtLeast(0)

    val usedMachinery = ConstructionEngine.getUsedMachinery(business)
    val totalMachinery = business.constructionData.maxMachinery
    val availableMachinery = (totalMachinery - usedMachinery).coerceAtLeast(0)

    val trustScore = business.constructionData.trustScore
    val certLevel = business.constructionData.safetyCertLevel

    val activeProjects = business.activeTenders

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ==========================================
        // 1. CONSTRUCTION COMMAND CENTER (GAUGES & METRICS)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Title & Upgrades Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE65100).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Engineering,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Construction Command Center",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kas Perusahaan: ${formatCurrencyRingkas(business.companyCash.toLong(), useShortFormat)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = { showCapacityUpgradeDialog = true },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upgrade,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ekspansi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Logistics Synergy Banner if unlocked
                if (hasLogisticsSynergy) {
                    Surface(
                        color = Color(0xFF00695C).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF00695C).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = Color(0xFF00796B),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Sinergi In-House Logistics Aktif: Diskon HPP Material 15% & Bonus Termin +5%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF004D40),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // 3 Resource Gauges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Gauge 1: Crew Allocation
                    ResourceGaugeCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Groups,
                        iconTint = Color(0xFF1976D2),
                        title = "Kru Pekerja",
                        valueText = "$usedCrews / $totalCrews",
                        subText = if (availableCrews > 0) "$availableCrews Bebas" else "Semua Sibuk",
                        progress = if (totalCrews > 0) usedCrews.toFloat() / totalCrews else 0f,
                        progressColor = if (availableCrews > 0) Color(0xFF1976D2) else Color(0xFFE53935)
                    )

                    // Gauge 2: Heavy Machinery
                    ResourceGaugeCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Agriculture,
                        iconTint = Color(0xFFF57C00),
                        title = "Alat Berat",
                        valueText = "$usedMachinery / $totalMachinery",
                        subText = if (availableMachinery > 0) "$availableMachinery Siap" else "0 Standby",
                        progress = if (totalMachinery > 0) usedMachinery.toFloat() / totalMachinery else 0f,
                        progressColor = if (availableMachinery > 0) Color(0xFFF57C00) else Color(0xFFE53935)
                    )

                    // Gauge 3: Trust & Safety Score
                    ResourceGaugeCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Verified,
                        iconTint = Color(0xFF388E3C),
                        title = "Trust Score",
                        valueText = "$trustScore/100",
                        subText = "K3 Lv.$certLevel",
                        progress = trustScore.toFloat() / 100f,
                        progressColor = Color(0xFF388E3C)
                    )
                }
            }
        }

        // ==========================================
        // 2. PROYEK AKTIF & MULTI-PHASE TIMELINE (KANBAN/GANTT)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewKanban,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Proyek Berjalan (${activeProjects.count { !it.isFinished }})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Total Selesai: ${business.constructionData.completedProjectsCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }

                if (activeProjects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Construction,
                                contentDescription = null,
                                tint = Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "Belum ada kontrak konstruksi aktif.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "Ikuti pelelangan tender di bawah untuk memulai proyek!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    activeProjects.forEach { project ->
                        ProjectPhaseTimelineCard(
                            project = project,
                            availableCrews = availableCrews,
                            availableMachinery = availableMachinery,
                            useShortFormat = useShortFormat,
                            onAllocateCurrentPhase = {
                                val err = onAllocatePhase(project.id)
                                if (err != null) {
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Kru & alat berat berhasil dialokasikan!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onEventClicked = {
                                selectedEventProject = project
                            }
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. PASAR TENDER DINAMIS (DYNAMIC BIDDING MARKET)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = Color(0xFFD84315),
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Pasar Tender & Lelang",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Bidding kompetitif berbasis Owner Estimate (OE)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val err = onRefreshMarket()
                            if (err != null) {
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Daftar tender lelang diperbarui!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Market",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                val marketTenders = if (business.constructionData.availableTenderMarket.isNotEmpty()) {
                    business.constructionData.availableTenderMarket
                } else {
                    ConstructionEngine.generateTenderMarket(business.level, trustScore)
                }

                if (marketTenders.isEmpty()) {
                    Text(
                        text = "Tidak ada penawaran tender saat ini. Tekan tombol refresh untuk mencari tender baru.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                } else {
                    marketTenders.forEach { tender ->
                        TenderMarketItemCard(
                            tender = tender,
                            useShortFormat = useShortFormat,
                            hasLogisticsSynergy = hasLogisticsSynergy,
                            onBidClick = {
                                selectedTenderForBid = tender
                            }
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOGS & BOTTOM SHEETS
    // ==========================================

    // 1. Dynamic Tender Bidding Dialog
    selectedTenderForBid?.let { tender ->
        TenderBiddingModal(
            tender = tender,
            playerCash = playerCash,
            companyCash = business.companyCash.toLong(),
            trustScore = trustScore,
            hasLogisticsSynergy = hasLogisticsSynergy,
            useShortFormat = useShortFormat,
            onDismiss = { selectedTenderForBid = null },
            onSubmitBid = { bidAmount, useCompanyCash, useInHouseLogistics ->
                selectedTenderForBid = null
                val (result, error) = onSubmitBid(tender.id, bidAmount, useCompanyCash, useInHouseLogistics)
                if (error != null) {
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                } else if (result != null) {
                    biddingResultDialog = result
                }
            }
        )
    }

    // 2. Bidding Result Announcement Dialog
    biddingResultDialog?.let { res ->
        BiddingResultAnnouncementDialog(
            result = res,
            useShortFormat = useShortFormat,
            onDismiss = { biddingResultDialog = null }
        )
    }

    // 3. Capacity Upgrade Dialog
    if (showCapacityUpgradeDialog) {
        CapacityUpgradeDialog(
            business = business,
            playerCash = playerCash,
            useShortFormat = useShortFormat,
            onDismiss = { showCapacityUpgradeDialog = false },
            onUpgrade = { type, useCompany ->
                val err = onUpgradeCapacity(type, useCompany)
                if (err != null) {
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Kapasitas berhasil ditingkatkan!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // 4. RNG Event Resolution Dialog
    selectedEventProject?.let { proj ->
        proj.activeEvent?.let { event ->
            RngEventResolutionDialog(
                project = proj,
                event = event,
                companyCash = business.companyCash.toLong(),
                safetyCertLevel = certLevel,
                useShortFormat = useShortFormat,
                onDismiss = { selectedEventProject = null },
                onResolve = { actionChoice ->
                    selectedEventProject = null
                    val err = onResolveEvent(proj.id, actionChoice)
                    if (err != null) {
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Insiden berhasil ditangani!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

// ==========================================
// COMPONENT 1: RESOURCE GAUGE CARD
// ==========================================
@Composable
fun ResourceGaugeCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    valueText: String,
    subText: String,
    progress: Float,
    progressColor: Color
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = valueText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            Text(
                text = subText,
                style = MaterialTheme.typography.labelSmall,
                color = if (progress >= 1f) Color(0xFFE53935) else Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ==========================================
// COMPONENT 2: PROJECT PHASE TIMELINE CARD
// ==========================================
@Composable
fun ProjectPhaseTimelineCard(
    project: ConstructionProject,
    availableCrews: Int,
    availableMachinery: Int,
    useShortFormat: Boolean,
    onAllocateCurrentPhase: () -> Unit,
    onEventClicked: () -> Unit
) {
    val totalVal = if (project.agreedBidPrice > 0) project.agreedBidPrice else project.totalContractValue.toLong()
    val isFinished = project.isFinished || project.currentPhaseIndex >= project.phases.size
    val phases = if (project.phases.isEmpty()) {
        ConstructionEngine.createDefaultPhases(project.name, project.durationMonths)
    } else {
        project.phases
    }

    val currentPhase = phases.getOrNull(project.currentPhaseIndex)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isFinished) Color(0xFF2E7D32).copy(alpha = 0.06f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isFinished) Color(0xFF2E7D32).copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Project Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = when (project.clientType) {
                                "PEMERINTAH" -> Color(0xFF1565C0)
                                "LUXURY" -> Color(0xFFE65100)
                                else -> Color(0xFF2E7D32)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = project.clientType,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (project.usesInHouseLogistics) {
                            Surface(
                                color = Color(0xFF00796B),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "🚚 LOGISTIK IN-HOUSE",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Klien: ${project.clientName} | Nilai Kontrak: ${formatCurrencyRingkas(totalVal, useShortFormat)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                if (isFinished) {
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32))
                    ) {
                        Text(
                            text = "SELESAI 100%",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Sisa ${project.remainingMonths} Bln",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // RNG Event Alert Banner (if active)
            if (!isFinished && project.activeEvent != null && !project.activeEvent.isResolved) {
                val event = project.activeEvent
                Surface(
                    color = Color(0xFFD32F2F).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEventClicked() }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB71C1C)
                                )
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = onEventClicked,
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Tangani", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3-Phase Stepper / Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Alur Fase Konstruksi & Termin Pembayaran:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )

                phases.forEachIndexed { idx, phase ->
                    val isPast = idx < project.currentPhaseIndex || phase.isCompleted
                    val isCurrent = idx == project.currentPhaseIndex && !isFinished
                    val isFuture = idx > project.currentPhaseIndex

                    val terminAmt = (totalVal * phase.payoutPercent).toLong()

                    Surface(
                        color = when {
                            isPast -> Color(0xFF2E7D32).copy(alpha = 0.08f)
                            isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else -> MaterialTheme.colorScheme.surface
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            when {
                                isPast -> Color(0xFF2E7D32).copy(alpha = 0.3f)
                                isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isPast -> Color(0xFF2E7D32)
                                                isCurrent -> MaterialTheme.colorScheme.primary
                                                else -> Color.Gray.copy(alpha = 0.3f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPast) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${idx + 1}",
                                            color = if (isCurrent) Color.White else Color.DarkGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = phase.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isFuture) Color.Gray else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Termin ${(phase.payoutPercent * 100).toInt()}% (+${formatCurrencyRingkas(terminAmt, useShortFormat)}) • Durasi ${phase.durationMonths} Bulan",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isPast) Color(0xFF2E7D32) else Color.Gray
                                    )
                                }
                            }

                            // Status / Allocation Action on right
                            when {
                                isPast -> {
                                    Text(
                                        text = "Cair ✅",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                isCurrent -> {
                                    if (phase.isAllocated) {
                                        Surface(
                                            color = Color(0xFFE65100).copy(alpha = 0.12f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "🔨 Dikerjakan (${phase.remainingMonths} bln)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFE65100),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    } else {
                                        val canAllocate = availableCrews >= project.requiredCrews && availableMachinery >= project.requiredMachinery
                                        Button(
                                            onClick = onAllocateCurrentPhase,
                                            enabled = canAllocate,
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(
                                                text = "Alokasikan Kru",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                else -> {
                                    Text(
                                        text = "Terkunci",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Allocation Requirements notice if current phase is unallocated
            if (!isFinished && currentPhase != null && !currentPhase.isAllocated) {
                val hasEnoughCrew = availableCrews >= project.requiredCrews
                val hasEnoughMach = availableMachinery >= project.requiredMachinery

                Surface(
                    color = if (hasEnoughCrew && hasEnoughMach) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = if (hasEnoughCrew && hasEnoughMach) MaterialTheme.colorScheme.primary else Color(0xFFE65100),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Dibutuhkan: ${project.requiredCrews} Kru & ${project.requiredMachinery} Alat Berat (Tersedia: $availableCrews Kru, $availableMachinery Alat)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasEnoughCrew && hasEnoughMach) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFFBF360C),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// COMPONENT 3: TENDER MARKET ITEM CARD
// ==========================================
@Composable
fun TenderMarketItemCard(
    tender: ConstructionTenderOpportunity,
    useShortFormat: Boolean,
    hasLogisticsSynergy: Boolean,
    onBidClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Scale Badge + Client on Left, Duration on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Surface(
                        color = when (tender.projectScale) {
                            "MEGA" -> Color(0xFF6A1B9A)
                            "LARGE" -> Color(0xFF0277BD)
                            else -> Color(0xFF2E7D32)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = tender.projectScale,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = tender.clientName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = "Durasi: ${tender.durationMonths} Bulan",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = Color.Gray,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Text(
                text = tender.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = tender.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Financial & Resource Specs (Clean 3-Column Grid)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Column 1: Pagu Anggaran (OE)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Pagu Anggaran (OE):",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatCurrencyRingkas(tender.ownerEstimateBudget, useShortFormat),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Column 2: Estimasi HPP Dasar
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Estimasi HPP Dasar:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val effCost = if (hasLogisticsSynergy) (tender.estimatedBaseCost * 0.85).toLong() else tender.estimatedBaseCost
                    Text(
                        text = formatCurrencyRingkas(effCost, useShortFormat),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Column 3: Kebutuhan Armada
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Kebutuhan Armada:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${tender.requiredCrews} Kru • ${tender.requiredMachinery} Alat",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Footer Row: Bid Bond & Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "Jaminan Penawaran (Bid Bond 5%):",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${formatCurrencyRingkas(tender.minBidBond, useShortFormat)} (Dikembalikan jika selesai)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onBidClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ikuti Lelang",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

// ==========================================
// MODAL 1: TENDER BIDDING MODAL
// ==========================================
@Composable
fun TenderBiddingModal(
    tender: ConstructionTenderOpportunity,
    playerCash: Long,
    companyCash: Long,
    trustScore: Int,
    hasLogisticsSynergy: Boolean,
    useShortFormat: Boolean,
    onDismiss: () -> Unit,
    onSubmitBid: (Long, Boolean, Boolean) -> Unit
) {
    val oe = tender.ownerEstimateBudget
    var bidRatio by remember { mutableStateOf(0.92f) } // Default 92% of OE
    var useCompanyCash by remember { mutableStateOf(true) }
    var useInHouseLogistics by remember { mutableStateOf(hasLogisticsSynergy) }

    val playerBidAmount = (oe * bidRatio).roundToLong()
    val baseCost = if (useInHouseLogistics) (tender.estimatedBaseCost * 0.85).roundToLong() else tender.estimatedBaseCost
    val estimatedProfit = playerBidAmount - baseCost
    val marginPercent = if (playerBidAmount > 0) (estimatedProfit.toDouble() / playerBidAmount * 100.0) else 0.0

    // Win probability estimate
    val winProbability = remember(bidRatio, trustScore) {
        val priceScore = when {
            bidRatio <= 0.75f -> 0.98f
            bidRatio <= 0.85f -> 0.88f - (bidRatio - 0.75f) * 1.0f
            bidRatio <= 0.95f -> 0.78f - (bidRatio - 0.85f) * 1.5f
            bidRatio <= 1.05f -> 0.63f - (bidRatio - 0.95f) * 2.5f
            bidRatio <= 1.15f -> 0.38f - (bidRatio - 1.05f) * 2.5f
            else -> 0.05f
        }
        val trustBonus = ((trustScore - 50) / 100.0f) * 0.35f
        ((priceScore + trustBonus).coerceIn(0.05f, 0.99f) * 100).toInt()
    }

    val bond = tender.minBidBond
    val canAffordBond = if (useCompanyCash) companyCash >= bond else playerCash >= bond

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Gavel,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Pengajuan Dokumen Tender", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = tender.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Slider for Bidding Price
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Nilai Penawaran Anda:",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = "${(bidRatio * 100).toInt()}% dari OE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = formatCurrencyRingkas(playerBidAmount, useShortFormat),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Slider(
                        value = bidRatio,
                        onValueChange = { bidRatio = it },
                        valueRange = 0.70f..1.20f,
                        steps = 50,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("70% OE (Agresif/Murah)", fontSize = 10.sp, color = Color.Gray)
                        Text("100% OE", fontSize = 10.sp, color = Color.Gray)
                        Text("120% OE (Margin Tinggi)", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                // Dynamic Evaluation Indicators
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimasi Peluang Menang:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "$winProbability% Peluang",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (winProbability >= 70) Color(0xFF2E7D32) else if (winProbability >= 40) Color(0xFFF57C00) else Color(0xFFE53935)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimasi Laba Bersih:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "${formatCurrencyRingkas(estimatedProfit, useShortFormat)} (${marginPercent.toInt()}%)",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (estimatedProfit > 0) Color(0xFF2E7D32) else Color(0xFFE53935)
                            )
                        }

                        if (hasLogisticsSynergy) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Gunakan Sinergi Logistik (-15% Biaya):", style = MaterialTheme.typography.bodySmall)
                                Switch(
                                    checked = useInHouseLogistics,
                                    onCheckedChange = { useInHouseLogistics = it }
                                )
                            }
                        }
                    }
                }

                // Bid Bond & Payment Source
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Jaminan Penawaran Tender: ${formatCurrencyRingkas(bond, useShortFormat)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD84315)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = useCompanyCash,
                            onClick = { useCompanyCash = true },
                            label = { Text("Kas Perusahaan (${formatCurrencyRingkas(companyCash, useShortFormat)})", fontSize = 11.sp) }
                        )

                        FilterChip(
                            selected = !useCompanyCash,
                            onClick = { useCompanyCash = false },
                            label = { Text("Kas Pribadi (${formatCurrencyRingkas(playerCash, useShortFormat)})", fontSize = 11.sp) }
                        )
                    }

                    if (!canAffordBond) {
                        Text(
                            text = "Saldo tidak mencukupi untuk menyetor jaminan lelang.",
                            color = Color(0xFFE53935),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmitBid(playerBidAmount, useCompanyCash, useInHouseLogistics) },
                enabled = canAffordBond
            ) {
                Text("Kirim Dokumen Penawaran")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// ==========================================
// MODAL 2: BIDDING RESULT ANNOUNCEMENT DIALOG
// ==========================================
@Composable
fun BiddingResultAnnouncementDialog(
    result: ConstructionEngine.BiddingResult,
    useShortFormat: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (result.isWon) Icons.Default.EmojiEvents else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (result.isWon) Color(0xFFFFA000) else Color(0xFFE53935)
                )
                Text(
                    text = if (result.isWon) "PENGUMUMAN: ANDA MENANG TENDER!" else "PENGUMUMAN: TENDER BELUM BERHASIL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = result.feedbackMessage,
                    style = MaterialTheme.typography.bodyMedium
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Rekapitulasi Penawaran Peserta Tender:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Player's bid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "1. Perusahaan Anda ${if (result.isWon) "(Pemenang)" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (result.isWon) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = formatCurrencyRingkas(result.winningBid, useShortFormat),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Competitor bids
                        result.competitorBids.forEachIndexed { idx, comp ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${idx + 2}. ${comp.first}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Text(
                                    text = formatCurrencyRingkas(comp.second, useShortFormat),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                if (result.isWon) {
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "✅ Kontrak resmi telah ditandatangani!",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Proyek siap dikerjakan di papan Proyek Berjalan. Alokasikan kru untuk memulai Fase 1.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

// ==========================================
// MODAL 3: CAPACITY UPGRADE DIALOG
// ==========================================
@Composable
fun CapacityUpgradeDialog(
    business: OwnedBusiness,
    playerCash: Long,
    useShortFormat: Boolean,
    onDismiss: () -> Unit,
    onUpgrade: (String, Boolean) -> Unit
) {
    val companyCash = business.companyCash.toLong()
    val curCrews = business.constructionData.maxCrews
    val curMach = business.constructionData.maxMachinery
    val curCert = business.constructionData.safetyCertLevel

    var useCompanyCash by remember { mutableStateOf(true) }

    val crewUpgradeCost = curCrews * 350_000L
    val machUpgradeCost = curMach * 150_000L
    val certUpgradeCost = curCert * 1_200_000L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Upgrade,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Ekspansi Kapasitas & K3", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = useCompanyCash,
                        onClick = { useCompanyCash = true },
                        label = { Text("Kas Perusahaan (${formatCurrencyRingkas(companyCash, useShortFormat)})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = !useCompanyCash,
                        onClick = { useCompanyCash = false },
                        label = { Text("Kas Pribadi (${formatCurrencyRingkas(playerCash, useShortFormat)})", fontSize = 11.sp) }
                    )
                }

                // 1. Kru Pekerja
                UpgradeItemCard(
                    title = "Rekrut Kru Tambahan (+2 Kru)",
                    description = "Kapasitas saat ini: $curCrews Kru Pekerja",
                    cost = crewUpgradeCost,
                    useShortFormat = useShortFormat,
                    canAfford = if (useCompanyCash) companyCash >= crewUpgradeCost else playerCash >= crewUpgradeCost,
                    onUpgradeClick = { onUpgrade("RECRUIT_CREW", useCompanyCash) }
                )

                // 2. Heavy Machinery
                UpgradeItemCard(
                    title = "Beli Armada Alat Berat (+4 Unit)",
                    description = "Armada saat ini: $curMach Unit Excavator & Crane",
                    cost = machUpgradeCost,
                    useShortFormat = useShortFormat,
                    canAfford = if (useCompanyCash) companyCash >= machUpgradeCost else playerCash >= machUpgradeCost,
                    onUpgradeClick = { onUpgrade("BUY_MACHINERY", useCompanyCash) }
                )

                // 3. K3 & Safety Certification
                UpgradeItemCard(
                    title = "Sertifikasi K3 & ISO (Tingkat ${curCert + 1})",
                    description = "Tingkatkan reputasi tender (+10 Trust) & kebal kecelakaan",
                    cost = certUpgradeCost,
                    useShortFormat = useShortFormat,
                    canAfford = if (useCompanyCash) companyCash >= certUpgradeCost else playerCash >= certUpgradeCost,
                    isMax = curCert >= 5,
                    onUpgradeClick = { onUpgrade("SAFETY_CERT", useCompanyCash) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun UpgradeItemCard(
    title: String,
    description: String,
    cost: Long,
    useShortFormat: Boolean,
    canAfford: Boolean,
    isMax: Boolean = false,
    onUpgradeClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                if (!isMax) {
                    Text(
                        text = "Biaya: ${formatCurrencyRingkas(cost, useShortFormat)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (canAfford) Color(0xFF2E7D32) else Color(0xFFE53935),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (isMax) {
                Text("MAKSIMAL", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            } else {
                Button(
                    onClick = onUpgradeClick,
                    enabled = canAfford,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Tingkatkan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==========================================
// MODAL 4: RNG EVENT RESOLUTION DIALOG
// ==========================================
@Composable
fun RngEventResolutionDialog(
    project: ConstructionProject,
    event: ConstructionRngEvent,
    companyCash: Long,
    safetyCertLevel: Int,
    useShortFormat: Boolean,
    onDismiss: () -> Unit,
    onResolve: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ReportProblem,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
                Text(text = event.title, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = event.description, style = MaterialTheme.typography.bodyMedium)

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Dampak Insiden:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        if (event.costImpact > 0) {
                            Text("• Biaya Ekstra: ${formatCurrencyRingkas(event.costImpact, useShortFormat)}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD32F2F))
                        }
                        if (event.delayMonths > 0) {
                            Text("• Keterlambatan: +${event.delayMonths} Bulan", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE65100))
                        }
                    }
                }

                Text(
                    text = "Pilih Solusi Penanganan:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                // Option 1: Pay Full Cost
                Button(
                    onClick = { onResolve("PAY_COST") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Tanggung Biaya Penuh (${formatCurrencyRingkas(event.costImpact, useShortFormat)})")
                }

                // Option 2: Insurance Claim if cert >= 2
                if (safetyCertLevel >= 2) {
                    val insDeductible = (event.costImpact * 0.35).toLong()
                    OutlinedButton(
                        onClick = { onResolve("CLAIM_INSURANCE") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Klaim Asuransi K3 (Deductible ${formatCurrencyRingkas(insDeductible, useShortFormat)})")
                    }
                }

                // Option 3: In house materials
                if (project.usesInHouseLogistics) {
                    val inHouseCost = (event.costImpact * 0.5).toLong()
                    OutlinedButton(
                        onClick = { onResolve("USE_IN_HOUSE") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Gunakan Material In-House (${formatCurrencyRingkas(inHouseCost, useShortFormat)})")
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
