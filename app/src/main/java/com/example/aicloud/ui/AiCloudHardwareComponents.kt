package com.example.aicloud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.aicloud.model.*

@Composable
fun HardwareManagementSection(
    dataCenter: AiDataCenter,
    onDeployMoreRacks: () -> Unit,
    onServiceRack: (String) -> Unit,
    onTradeInRack: (ServerRackUnit) -> Unit,
    onDecommissionRack: (ServerRackUnit) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "${dataCenter.name} • Server Racks",
                    color = AiCloudColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Text(
                    text = "Total Floor Slots: ${dataCenter.totalRackUnitsUsed} / ${dataCenter.tier.maxRackUnits}",
                    color = AiCloudColors.TextSecondary,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onDeployMoreRacks,
                colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.CyanPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Racks", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1, softWrap = false)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (dataCenter.serverRacks.isEmpty()) {
            Surface(
                color = AiCloudColors.CardBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Memory, contentDescription = null, tint = AiCloudColors.TextSecondary, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No Server Racks Deployed", color = AiCloudColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Floor space is ready. Deploy GPU clusters to begin computing.", color = AiCloudColors.TextSecondary, fontSize = 12.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                dataCenter.serverRacks.forEach { rack ->
                    RackUnitItemCard(
                        rack = rack,
                        onService = { onServiceRack(rack.id) },
                        onTradeIn = { onTradeInRack(rack) },
                        onDecommission = { onDecommissionRack(rack) }
                    )
                }
            }
        }
    }
}

@Composable
fun RackUnitItemCard(
    rack: ServerRackUnit,
    onService: () -> Unit,
    onTradeIn: () -> Unit,
    onDecommission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AiCloudColors.CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: GPU Model & Lifecycle Phase Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Memory,
                        contentDescription = null,
                        tint = AiCloudColors.CyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${rack.quantity}x ${rack.gpuTier.displayName}",
                        color = AiCloudColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }

                Surface(
                    color = Color(rack.lifecyclePhase.badgeColorHex).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(rack.lifecyclePhase.badgeColorHex).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = rack.lifecyclePhase.label,
                        color = Color(rack.lifecyclePhase.badgeColorHex),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Specs Row
            Text(
                text = "${String.format("%,.0f", rack.totalTflops)} TFLOPS FP16 • ${String.format("%.1f", rack.totalPowerKw)} kW • Nilai Salvage: $${String.format("%,d", rack.resaleValueUsd)}",
                color = AiCloudColors.CyanPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Time cycle info: Age and Condition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Masa Aktif: ${rack.ageMonths} Bulan (Siklus 36-48 bln)",
                    color = AiCloudColors.TextSecondary,
                    fontSize = 11.sp
                )

                Text(
                    text = "Kondisi: ${String.format("%.1f", rack.healthConditionPct)}%",
                    color = if (rack.healthConditionPct < 70.0) AiCloudColors.AmberWarning else AiCloudColors.GreenNeon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Condition Bar
            LinearProgressIndicator(
                progress = { (rack.healthConditionPct / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (rack.healthConditionPct < 60.0) AiCloudColors.RedAlert else if (rack.healthConditionPct < 85.0) AiCloudColors.AmberWarning else AiCloudColors.GreenNeon,
                trackColor = AiCloudColors.SurfaceHighlight
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (rack.healthConditionPct < 90.0) {
                    OutlinedButton(
                        onClick = onService,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AiCloudColors.CyanPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.CyanPrimary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Servis ($${String.format("%,d", rack.routineServiceCostUsd)})", fontSize = 11.sp, maxLines = 1)
                    }
                }

                OutlinedButton(
                    onClick = onTradeIn,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AiCloudColors.GreenNeon),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.GreenNeon),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tukar Tambah", fontSize = 11.sp, maxLines = 1)
                }

                OutlinedButton(
                    onClick = onDecommission,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AiCloudColors.RedAlert),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.RedAlert.copy(alpha = 0.7f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Lepas/Jual Rack", modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeployRackDialog(
    dataCenter: AiDataCenter,
    companyBalance: Long,
    unlockedResearchIds: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (GpuChipTier, Int) -> Unit
) {
    var selectedGpu by remember { mutableStateOf(GpuChipTier.FLAGSHIP_H100_SXM5) }
    var quantity by remember { mutableStateOf(1) }

    val availableSlots = dataCenter.tier.maxRackUnits - dataCenter.totalRackUnitsUsed
    val totalCost = selectedGpu.costPerRackUsd * quantity
    val canAfford = companyBalance >= totalCost && quantity <= availableSlots

    val isCustomTpuLocked = selectedGpu == GpuChipTier.CUSTOM_ASIC_TPU_V5P && !unlockedResearchIds.contains("tech_silicon_custom_tpu")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deploy GPU Racks into ${dataCenter.name}", color = AiCloudColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Available Floor Space: $availableSlots rack slots", fontSize = 11.sp, color = AiCloudColors.CyanPrimary)
                }

                item {
                    Text("Select GPU Chip Tier", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AiCloudColors.TextPrimary)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        GpuChipTier.values().forEach { gpu ->
                            val isSelected = selectedGpu == gpu
                            val isLocked = gpu == GpuChipTier.CUSTOM_ASIC_TPU_V5P && !unlockedResearchIds.contains("tech_silicon_custom_tpu")
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !isLocked) { selectedGpu = gpu },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AiCloudColors.CyanPrimary.copy(alpha = 0.15f) else AiCloudColors.SurfaceHighlight,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.CyanPrimary) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(gpu.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isLocked) AiCloudColors.TextSecondary else AiCloudColors.TextPrimary)
                                        Text("${String.format("%,.0f", gpu.tflopsFp16)} TFLOPS • ${gpu.powerDrawKwPerRack} kW/rack", fontSize = 10.sp, color = AiCloudColors.TextSecondary)
                                        if (isLocked) {
                                            Text("🔒 Requires R&D: Proprietary ASIC", fontSize = 10.sp, color = AiCloudColors.AmberWarning)
                                        }
                                    }
                                    Text("$${String.format("%,d", gpu.costPerRackUsd)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.CyanPrimary)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Quantity (Racks to Deploy)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AiCloudColors.TextPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { if (quantity > 1) quantity-- },
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("-", fontSize = 18.sp, color = AiCloudColors.TextPrimary) }

                        Text(
                            text = "$quantity Units (${String.format("%,.0f", selectedGpu.tflopsFp16 * quantity)} TFLOPS)",
                            color = AiCloudColors.CyanPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        OutlinedButton(
                            onClick = { if (quantity < availableSlots) quantity++ },
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("+", fontSize = 18.sp, color = AiCloudColors.TextPrimary) }
                    }
                }

                item {
                    Surface(
                        color = AiCloudColors.SurfaceHighlight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Procurement Cost:", fontSize = 11.sp, color = AiCloudColors.TextSecondary)
                                Text("$${String.format("%,d", totalCost)} USD", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (canAfford) AiCloudColors.GreenNeon else AiCloudColors.RedAlert)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedGpu, quantity) },
                enabled = canAfford && !isCustomTpuLocked,
                colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.CyanPrimary)
            ) {
                Text("Deploy Racks", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = AiCloudColors.TextSecondary) }
        },
        containerColor = AiCloudColors.CardBg
    )
}

@Composable
fun RetrofitPlantDialog(
    dataCenter: AiDataCenter,
    companyBalance: Long,
    onDismiss: () -> Unit,
    onUpgradeCooling: (CoolingSystemType) -> Unit,
    onUpgradePower: (PowerSourceType) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Retrofit Plant & Infrastructure", color = AiCloudColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = AiCloudColors.SurfaceHighlight,
                    contentColor = AiCloudColors.CyanPrimary
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Cooling & PUE", fontSize = 12.sp, modifier = Modifier.padding(8.dp), color = AiCloudColors.TextPrimary)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Power & Grid", fontSize = 12.sp, modifier = Modifier.padding(8.dp), color = AiCloudColors.TextPrimary)
                    }
                }

                if (selectedTab == 0) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(CoolingSystemType.values()) { cooling ->
                            val isCurrent = dataCenter.coolingSystem == cooling
                            val canAfford = companyBalance >= cooling.upgradeCostUsd
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) AiCloudColors.GreenNeon.copy(alpha = 0.15f) else AiCloudColors.SurfaceHighlight,
                                border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.GreenNeon) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(cooling.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.TextPrimary)
                                        Text("Base PUE: ${cooling.basePue} • Maint: $${String.format("%,d", cooling.monthlyMaintenanceUsd)}/mo", fontSize = 10.sp, color = AiCloudColors.TextSecondary)
                                    }
                                    if (isCurrent) {
                                        Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.GreenNeon)
                                    } else {
                                        Button(
                                            onClick = { onUpgradeCooling(cooling) },
                                            enabled = canAfford,
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.CyanPrimary),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("$${String.format("%,d", cooling.upgradeCostUsd)}", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PowerSourceType.values()) { power ->
                            val isCurrent = dataCenter.powerSource == power
                            val canAfford = companyBalance >= power.upgradeCostUsd
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) AiCloudColors.PurpleAccent.copy(alpha = 0.15f) else AiCloudColors.SurfaceHighlight,
                                border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.PurpleAccent) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(power.displayName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.TextPrimary)
                                        Text("Capacity Bonus: +${power.capacityMwBonus} MW • Power Discount: ${String.format("%.0f", power.costDiscountPct * 100)}%", fontSize = 10.sp, color = AiCloudColors.TextSecondary)
                                    }
                                    if (isCurrent) {
                                        Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.PurpleAccent)
                                    } else {
                                        Button(
                                            onClick = { onUpgradePower(power) },
                                            enabled = canAfford,
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.PurpleAccent),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("$${String.format("%,d", power.upgradeCostUsd)}", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = AiCloudColors.TextPrimary) }
        },
        containerColor = AiCloudColors.CardBg
    )
}

@Composable
fun TradeInRackDialog(
    rack: ServerRackUnit,
    companyBalance: Long,
    unlockedResearchIds: Set<String>,
    onDismiss: () -> Unit,
    onConfirmTradeIn: (GpuChipTier) -> Unit
) {
    var selectedTier by remember { mutableStateOf(GpuChipTier.FLAGSHIP_H100_SXM5) }

    val tradeInCredit = rack.resaleValueUsd
    val newCost = selectedTier.costPerRackUsd * rack.quantity
    val netPayable = (newCost - tradeInCredit).coerceAtLeast(0L)
    val canAfford = companyBalance >= netPayable
    val isTpuLocked = selectedTier == GpuChipTier.CUSTOM_ASIC_TPU_V5P && !unlockedResearchIds.contains("tech_silicon_custom_tpu")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Tukar Tambah GPU Cluster", color = AiCloudColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Upgrade rack ke arsitektur GPU lebih baru dengan potongan kredit", color = AiCloudColors.TextSecondary, fontSize = 11.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Old Hardware Summary Card
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AiCloudColors.SurfaceHighlight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("GPU Lama yang Ditukar:", fontSize = 11.sp, color = AiCloudColors.TextSecondary)
                        Text("${rack.quantity}x ${rack.gpuTier.displayName}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.TextPrimary)
                        Text(
                            "Usia: ${rack.ageMonths} Bulan • Kondisi: ${String.format("%.1f", rack.healthConditionPct)}%",
                            fontSize = 11.sp,
                            color = AiCloudColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Potongan Nilai Tukar: +$${String.format("%,d", tradeInCredit)} USD",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AiCloudColors.GreenNeon
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Pilih Model GPU Pengganti:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AiCloudColors.TextPrimary)
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GpuChipTier.values().forEach { tier ->
                        val isSelected = tier == selectedTier
                        val tierNewCost = tier.costPerRackUsd * rack.quantity
                        val tierNetPayable = (tierNewCost - tradeInCredit).coerceAtLeast(0L)
                        val tierTpuLocked = tier == GpuChipTier.CUSTOM_ASIC_TPU_V5P && !unlockedResearchIds.contains("tech_silicon_custom_tpu")

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) AiCloudColors.GreenNeon.copy(alpha = 0.12f) else AiCloudColors.SurfaceHighlight,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.GreenNeon) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !tierTpuLocked) { selectedTier = tier }
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        tier.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tierTpuLocked) AiCloudColors.TextSecondary else AiCloudColors.TextPrimary
                                    )
                                    Text(
                                        "${String.format("%,.0f", tier.tflopsFp16 * rack.quantity)} TFLOPS FP16 • ${tier.powerDrawKwPerRack * rack.quantity} kW",
                                        fontSize = 10.sp,
                                        color = AiCloudColors.CyanPrimary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    if (tierTpuLocked) {
                                        Text("Locked (R&D)", fontSize = 10.sp, color = AiCloudColors.RedAlert)
                                    } else {
                                        Text(
                                            "Biaya Bersih: $${String.format("%,d", tierNetPayable)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (companyBalance >= tierNetPayable) AiCloudColors.GreenNeon else AiCloudColors.RedAlert
                                        )
                                        Text("Hemat -$${String.format("%,d", tradeInCredit)}", fontSize = 9.sp, color = AiCloudColors.TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmTradeIn(selectedTier) },
                enabled = canAfford && !isTpuLocked,
                colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.GreenNeon)
            ) {
                Text(
                    "Tukar Tambah ($${String.format("%,d", netPayable)})",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = AiCloudColors.TextSecondary) }
        },
        containerColor = AiCloudColors.CardBg
    )
}

@Composable
fun DecommissionConfirmDialog(
    rack: ServerRackUnit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Konfirmasi Lepas/Jual Rack", color = AiCloudColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column {
                Text(
                    text = "Apakah Anda yakin ingin melepas dan menjual unit ${rack.quantity}x ${rack.gpuTier.displayName}?",
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
                        Text("Detail Decommission:", fontSize = 11.sp, color = AiCloudColors.TextSecondary)
                        Text("• Slot rak yang dibebaskan: +${rack.quantity} slot", fontSize = 12.sp, color = AiCloudColors.CyanPrimary)
                        Text("• Dana salvage masuk ke kas: +$${String.format("%,d", rack.resaleValueUsd)} USD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.GreenNeon)
                        Text("• Penurunan komputasi cluster: -${String.format("%,.0f", rack.totalTflops)} TFLOPS", fontSize = 12.sp, color = AiCloudColors.AmberWarning)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.RedAlert)
            ) {
                Text("Ya, Jual & Lepas Rack", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = AiCloudColors.TextSecondary) }
        },
        containerColor = AiCloudColors.CardBg
    )
}
