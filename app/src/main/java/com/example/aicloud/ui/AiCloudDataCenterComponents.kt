package com.example.aicloud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aicloud.model.*

@Composable
fun DataCenterCard(
    dataCenter: AiDataCenter,
    onManageHardware: () -> Unit,
    onRetrofitFacility: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (dataCenter.isOverloaded) AiCloudColors.RedAlert else AiCloudColors.CardBorder,
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AiCloudColors.CardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Status Badge, Title, Region
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    !dataCenter.isOperational -> AiCloudColors.AmberWarning
                                    dataCenter.isOverloaded -> AiCloudColors.RedAlert
                                    else -> AiCloudColors.GreenNeon
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dataCenter.name,
                        color = AiCloudColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AiCloudColors.SurfaceHighlight
                ) {
                    Text(
                        text = dataCenter.region.country,
                        color = AiCloudColors.CyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${dataCenter.tier.displayName} • ${dataCenter.region.displayName}",
                color = AiCloudColors.TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!dataCenter.isOperational) {
                // Construction Progress Indicator
                Surface(
                    color = AiCloudColors.AmberWarning.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = AiCloudColors.AmberWarning,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Facility Under Construction",
                                color = AiCloudColors.AmberWarning,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Commissioning ETA: ${dataCenter.constructionMonthsRemaining} game-month(s)",
                                color = AiCloudColors.TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            } else {
                // Operational Telemetry Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricBadge(
                        label = "Compute Power",
                        value = "${String.format("%,.0f", dataCenter.totalComputeTflops)} TFLOPS",
                        subtext = "${String.format("%.2f", dataCenter.totalComputePflops)} PFLOPS",
                        color = AiCloudColors.CyanPrimary
                    )
                    MetricBadge(
                        label = "PUE Efficiency",
                        value = String.format("%.2f", dataCenter.effectivePue),
                        subtext = dataCenter.coolingSystem.displayName.take(14) + "...",
                        color = if (dataCenter.effectivePue < 1.15) AiCloudColors.GreenNeon else AiCloudColors.AmberWarning
                    )
                    MetricBadge(
                        label = "Rack Density",
                        value = "${dataCenter.totalRackUnitsUsed} / ${dataCenter.tier.maxRackUnits}",
                        subtext = "Slots Filled",
                        color = AiCloudColors.PurpleAccent
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Power Grid Usage Bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Grid Power Load: ${String.format("%.1f", dataCenter.currentFacilityPowerMw)} / ${String.format("%.1f", dataCenter.effectivePowerCapacityMw)} MW",
                            fontSize = 11.sp,
                            color = if (dataCenter.isOverloaded) AiCloudColors.RedAlert else AiCloudColors.TextSecondary
                        )
                        Text(
                            text = "${String.format("%.1f", dataCenter.powerUtilizationPct)}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dataCenter.isOverloaded) AiCloudColors.RedAlert else AiCloudColors.CyanPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (dataCenter.powerUtilizationPct / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (dataCenter.isOverloaded) AiCloudColors.RedAlert else AiCloudColors.CyanPrimary,
                        trackColor = AiCloudColors.SurfaceHighlight,
                    )
                }

                if (dataCenter.isOverloaded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ Brownout Warning: Power draw exceeds grid transformer limit! Upgrade power substation to avert SLA fines.",
                        color = AiCloudColors.RedAlert,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onManageHardware,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.SurfaceHighlight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = AiCloudColors.CyanPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Deploy Racks", fontSize = 12.sp, color = AiCloudColors.TextPrimary)
                    }

                    Button(
                        onClick = onRetrofitFacility,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.SurfaceHighlight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AcUnit, contentDescription = null, tint = AiCloudColors.PurpleAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Retrofit Plant", fontSize = 12.sp, color = AiCloudColors.TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBadge(label: String, value: String, subtext: String, color: Color) {
    Surface(
        color = AiCloudColors.SurfaceHighlight,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(text = label, fontSize = 10.sp, color = AiCloudColors.TextSecondary)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = subtext, fontSize = 9.sp, color = AiCloudColors.TextSecondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildDataCenterDialog(
    companyBalance: Long,
    onDismiss: () -> Unit,
    onConfirm: (String, CloudRegion, DataCenterTier, CoolingSystemType, PowerSourceType) -> Unit
) {
    var facilityName by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf(CloudRegion.US_EAST_VIRGINIA) }
    var selectedTier by remember { mutableStateOf(DataCenterTier.TIER_1_STANDARD) }
    var selectedCooling by remember { mutableStateOf(CoolingSystemType.AIR_COOLED_STANDARD) }
    var selectedPower by remember { mutableStateOf(PowerSourceType.MUNICIPAL_GRID) }

    val totalCost = selectedRegion.baseLandCostUsd + selectedTier.constructionCostUsd + selectedCooling.upgradeCostUsd + selectedPower.upgradeCostUsd
    val canAfford = companyBalance >= totalCost

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Groundbreak AI Data Center", color = AiCloudColors.TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = facilityName,
                        onValueChange = { facilityName = it },
                        label = { Text("Facility Name (Optional)") },
                        placeholder = { Text("e.g. Apex HyperCluster 1") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AiCloudColors.CyanPrimary,
                            unfocusedBorderColor = AiCloudColors.CardBorder,
                            focusedTextColor = AiCloudColors.TextPrimary,
                            unfocusedTextColor = AiCloudColors.TextPrimary
                        )
                    )
                }

                item {
                    Text("Select Global Region", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AiCloudColors.CyanPrimary)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CloudRegion.values().forEach { region ->
                            val isSelected = selectedRegion == region
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedRegion = region },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AiCloudColors.CyanPrimary.copy(alpha = 0.15f) else AiCloudColors.SurfaceHighlight,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.CyanPrimary) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(region.displayName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AiCloudColors.TextPrimary)
                                        Text("Power: $${region.powerCostPerKwhUsd}/kWh • Land: $${String.format("%,d", region.baseLandCostUsd)}", fontSize = 10.sp, color = AiCloudColors.TextSecondary)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AiCloudColors.CyanPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Facility Tier & Scale", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AiCloudColors.CyanPrimary)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DataCenterTier.values().forEach { tier ->
                            val isSelected = selectedTier == tier
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTier = tier },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) AiCloudColors.PurpleAccent.copy(alpha = 0.15f) else AiCloudColors.SurfaceHighlight,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.PurpleAccent) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(tier.displayName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AiCloudColors.TextPrimary)
                                        Text("Capacity: ${tier.baseCapacityMw} MW • Max ${tier.maxRackUnits} Racks • Build: ${tier.buildMonths} mo", fontSize = 10.sp, color = AiCloudColors.TextSecondary)
                                    }
                                    Text("$${String.format("%,d", tier.constructionCostUsd)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.PurpleAccent)
                                }
                            }
                        }
                    }
                }

                item {
                    Surface(
                        color = AiCloudColors.SurfaceHighlight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Initial Capex:", fontSize = 12.sp, color = AiCloudColors.TextSecondary)
                                Text("$${String.format("%,d", totalCost)} USD", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (canAfford) AiCloudColors.GreenNeon else AiCloudColors.RedAlert)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Treasury Balance:", fontSize = 11.sp, color = AiCloudColors.TextSecondary)
                                Text("$${String.format("%,d", companyBalance)} USD", fontSize = 11.sp, color = AiCloudColors.TextPrimary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(facilityName, selectedRegion, selectedTier, selectedCooling, selectedPower)
                },
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.CyanPrimary)
            ) {
                Text("Authorize Construction", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AiCloudColors.TextSecondary)
            }
        },
        containerColor = AiCloudColors.CardBg
    )
}
