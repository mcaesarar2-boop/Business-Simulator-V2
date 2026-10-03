package com.example.corporate.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.corporate.model.DividendPolicyPreset
import com.example.corporate.model.MergedDividendPolicy
import com.example.corporate.model.StandaloneDividendPolicy
import com.example.corporate.repository.CorporatePolicyRepository
import com.example.ui.formatCurrencyRingkas

/**
 * Modern Jetpack Compose Dialog for configuring 3-Tier Corporate Cash Flow
 * and Dynamic Dividend Distribution policies for Holdings and Standalone Units.
 */
@Composable
fun DividendPolicyDialog(
    holdingId: String? = null,
    holdingName: String? = null,
    businessId: String? = null,
    businessName: String? = null,
    estimatedMonthlyProfit: Long = 100_000L,
    onDismiss: () -> Unit,
    onPolicyApplied: () -> Unit = {}
) {
    val repository = remember { CorporatePolicyRepository.getInstance() }
    val isHoldingMode = holdingId != null

    // State for Merged Holding Policy
    val currentHoldingPolicy = remember(holdingId) {
        if (holdingId != null) repository.getHoldingPolicy(holdingId) else MergedDividendPolicy.DEFAULT
    }
    var mergedUnitPercent by remember { mutableFloatStateOf(currentHoldingPolicy.unitRetainedPercent.toFloat()) }
    var mergedHoldingPercent by remember { mutableFloatStateOf(currentHoldingPolicy.holdingTreasuryPercent.toFloat()) }
    var mergedMegaPercent by remember { mutableFloatStateOf(currentHoldingPolicy.megaHoldingDividendPercent.toFloat()) }

    // State for Standalone Policy
    val currentStandalonePolicy = remember(businessId) {
        if (businessId != null) repository.getStandalonePolicy(businessId) else StandaloneDividendPolicy.DEFAULT
    }
    var standaloneUnitPercent by remember { mutableFloatStateOf(currentStandalonePolicy.unitRetainedPercent.toFloat()) }

    var selectedPreset by remember { mutableStateOf(DividendPolicyPreset.BALANCED) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (isHoldingMode && holdingId != null) {
                        val policy = MergedDividendPolicy(
                            unitRetainedPercent = mergedUnitPercent.toInt(),
                            holdingTreasuryPercent = mergedHoldingPercent.toInt(),
                            megaHoldingDividendPercent = (100 - mergedUnitPercent.toInt() - mergedHoldingPercent.toInt()).coerceAtLeast(0)
                        )
                        repository.setHoldingPolicy(holdingId, policy)
                    } else if (businessId != null) {
                        val policy = StandaloneDividendPolicy(
                            unitRetainedPercent = standaloneUnitPercent.toInt(),
                            megaHoldingPayoutPercent = 100 - standaloneUnitPercent.toInt()
                        )
                        repository.setStandalonePolicy(businessId, policy)
                    }
                    onPolicyApplied()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Terapkan Kebijakan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0288D1).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color(0xFF0288D1),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "Kebijakan Arus Kas & Dividen",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (isHoldingMode) "Holding: ${holdingName ?: "Sub-Holding"}"
                        else "Unit Mandiri: ${businessName ?: "Unit Operasional"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tier Indicator Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Color(0xFF0288D1).copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (isHoldingMode) "Struktur 3-Tier: Merged Holding" else "Struktur 2-Tier: Standalone Unit",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0288D1)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isHoldingMode)
                                "Laba anak perusahaan dibagi ke Kas Unit (Tier 3), Kas Internal Holding (Tier 2), dan Dividen Pemain (Tier 1)."
                            else
                                "Laba unit dibagi antara Kas Operasional Unit (Tier 3) dan Kas Global Mega Holding (Tier 1).",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Preset Strategy Chips
                Text("Preset Strategi Perusahaan", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedPreset == DividendPolicyPreset.BALANCED,
                        onClick = {
                            selectedPreset = DividendPolicyPreset.BALANCED
                            if (isHoldingMode) {
                                mergedUnitPercent = 20f
                                mergedHoldingPercent = 50f
                                mergedMegaPercent = 30f
                            } else {
                                standaloneUnitPercent = 30f
                            }
                        },
                        label = { Text("Seimbang", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedPreset == DividendPolicyPreset.GROWTH_REINVESTMENT,
                        onClick = {
                            selectedPreset = DividendPolicyPreset.GROWTH_REINVESTMENT
                            if (isHoldingMode) {
                                mergedUnitPercent = 40f
                                mergedHoldingPercent = 45f
                                mergedMegaPercent = 15f
                            } else {
                                standaloneUnitPercent = 60f
                            }
                        },
                        label = { Text("Ekspansi", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedPreset == DividendPolicyPreset.CASH_COW_LIQUIDITY,
                        onClick = {
                            selectedPreset = DividendPolicyPreset.CASH_COW_LIQUIDITY
                            if (isHoldingMode) {
                                mergedUnitPercent = 10f
                                mergedHoldingPercent = 20f
                                mergedMegaPercent = 70f
                            } else {
                                standaloneUnitPercent = 10f
                            }
                        },
                        label = { Text("Cash Cow", fontSize = 11.sp) }
                    )
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                if (isHoldingMode) {
                    // Merged Holding Sliders (3 Sliders)
                    PolicySliderRow(
                        title = "1. Kas Unit (Operasional)",
                        subtitle = "Buffer perawatan & biaya operasional anak cabang",
                        percent = mergedUnitPercent.toInt(),
                        color = Color(0xFF43A047),
                        onValueChange = { newVal ->
                            selectedPreset = DividendPolicyPreset.CUSTOM
                            val maxAllowed = (100f - mergedHoldingPercent).coerceAtLeast(0f)
                            mergedUnitPercent = newVal.coerceIn(0f, maxAllowed)
                            mergedMegaPercent = (100f - mergedUnitPercent - mergedHoldingPercent).coerceAtLeast(0f)
                        }
                    )

                    PolicySliderRow(
                        title = "2. Kas Internal Holding (Treasury)",
                        subtitle = "Kas sub-holding untuk M&A, investasi, dan valuasi",
                        percent = mergedHoldingPercent.toInt(),
                        color = Color(0xFFFFA000),
                        onValueChange = { newVal ->
                            selectedPreset = DividendPolicyPreset.CUSTOM
                            val maxAllowed = (100f - mergedUnitPercent).coerceAtLeast(0f)
                            mergedHoldingPercent = newVal.coerceIn(0f, maxAllowed)
                            mergedMegaPercent = (100f - mergedUnitPercent - mergedHoldingPercent).coerceAtLeast(0f)
                        }
                    )

                    PolicySliderRow(
                        title = "3. Dividen Mega Holding (Kas Pemain)",
                        subtitle = "Uang kas langsung ke saldo profil utama pemain",
                        percent = mergedMegaPercent.toInt(),
                        color = Color(0xFF1976D2),
                        onValueChange = { newVal ->
                            selectedPreset = DividendPolicyPreset.CUSTOM
                            val clamped = newVal.coerceIn(0f, 100f)
                            val remaining = 100f - clamped
                            val ratioUnit = if (mergedUnitPercent + mergedHoldingPercent > 0)
                                mergedUnitPercent / (mergedUnitPercent + mergedHoldingPercent) else 0.3f
                            mergedUnitPercent = (remaining * ratioUnit).coerceAtLeast(0f)
                            mergedHoldingPercent = (remaining - mergedUnitPercent).coerceAtLeast(0f)
                            mergedMegaPercent = clamped
                        }
                    )
                } else {
                    // Standalone Sliders (2-Tier)
                    val payoutPercent = 100 - standaloneUnitPercent.toInt()
                    PolicySliderRow(
                        title = "1. Retained Earnings (Kas Unit)",
                        subtitle = "Ditahan di saldo unit untuk upgrade & maintenance",
                        percent = standaloneUnitPercent.toInt(),
                        color = Color(0xFF43A047),
                        onValueChange = { newVal ->
                            selectedPreset = DividendPolicyPreset.CUSTOM
                            standaloneUnitPercent = newVal.coerceIn(0f, 100f)
                        }
                    )

                    PolicySliderRow(
                        title = "2. Setoran Laba ke Mega Holding",
                        subtitle = "Uang disetor langsung ke saldo kas profil pemain",
                        percent = payoutPercent,
                        color = Color(0xFF1976D2),
                        onValueChange = { newVal ->
                            selectedPreset = DividendPolicyPreset.CUSTOM
                            standaloneUnitPercent = (100f - newVal).coerceIn(0f, 100f)
                        }
                    )
                }

                // Financial Simulation Preview Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Simulasi Arus Kas Bulanan",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Est. Laba: ${formatCurrencyRingkas(estimatedMonthlyProfit, false)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF38BDF8)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (isHoldingMode) {
                            val uVal = (estimatedMonthlyProfit * (mergedUnitPercent / 100.0)).toLong()
                            val hVal = (estimatedMonthlyProfit * (mergedHoldingPercent / 100.0)).toLong()
                            val mVal = (estimatedMonthlyProfit * (mergedMegaPercent / 100.0)).toLong()

                            SimulationItem(label = "Kas Unit Operasional", amount = uVal, percent = mergedUnitPercent.toInt(), color = Color(0xFF4ADE80))
                            SimulationItem(label = "Kas Internal Holding", amount = hVal, percent = mergedHoldingPercent.toInt(), color = Color(0xFFFBBF24))
                            SimulationItem(label = "Dividen Mega Holding", amount = mVal, percent = mergedMegaPercent.toInt(), color = Color(0xFF60A5FA))
                        } else {
                            val uVal = (estimatedMonthlyProfit * (standaloneUnitPercent / 100.0)).toLong()
                            val mVal = estimatedMonthlyProfit - uVal

                            SimulationItem(label = "Kas Unit Operasional", amount = uVal, percent = standaloneUnitPercent.toInt(), color = Color(0xFF4ADE80))
                            SimulationItem(label = "Setoran Kas Mega Holding", amount = mVal, percent = 100 - standaloneUnitPercent.toInt(), color = Color(0xFF60A5FA))
                        }
                    }
                }

                // Strategic Gameplay Tip
                Text(
                    text = if (isHoldingMode) {
                        "💡 Tip Strategi: Kas Internal Holding yang tinggi memperkuat likuiditas untuk mengakuisisi saham dan memperbesar valuasi saat IPO."
                    } else {
                        "💡 Tip Strategi: Retensi kas unit yang cukup mencegah pembekuan aset saat terjadi kenaikan biaya operasional atau gugatan hukum."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    )
}

@Composable
private fun PolicySliderRow(
    title: String,
    subtitle: String,
    percent: Int,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 10.sp)
            }
            Text(
                "$percent%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
        Slider(
            value = percent.toFloat(),
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            steps = 99,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun SimulationItem(
    label: String,
    amount: Long,
    percent: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("• $label ($percent%)", fontSize = 11.sp, color = Color(0xFFCBD5E1))
        Text(
            formatCurrencyRingkas(amount, false),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
