package com.example.ma.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ma.model.AcquisitionOffer
import com.example.ma.model.NegotiationOutcome
import com.example.ma.model.NegotiationResult
import java.util.Locale

@Composable
fun MaNegotiationDialog(
    offer: AcquisitionOffer,
    lastResult: NegotiationResult?,
    onSubmitCounter: (counterMultiplier: Double, counterStake: Double) -> Unit,
    onAcceptDeal: () -> Unit,
    onDismiss: () -> Unit
) {
    // Initial premium demand: default +20% above original offer multiplier
    val minMultiplier = (offer.valuationMultiplier * 1.15)
    val maxMultiplier = (offer.valuationMultiplier * 1.50)
    var counterMultiplier by remember(offer) {
        mutableStateOf(minMultiplier.coerceAtLeast(1.15))
    }
    var counterStake by remember(offer) {
        mutableStateOf(offer.stakePercent)
    }

    val counterValuation = (offer.currentEntityValuation * counterMultiplier).toLong()
    val counterCashDemand = (counterValuation * (counterStake / 100.0)).toLong()

    val premiumDemandPercent = ((counterMultiplier / offer.valuationMultiplier) - 1.0) * 100.0
    val estimatedRiskPercent = (12.0 + (premiumDemandPercent * 0.6) + (offer.bidder.aggressiveness * 10.0)).toInt().coerceIn(10, 45)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .testTag("ma_negotiation_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF13151F)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF7E57C2), Color(0xFF311B92), Color(0xFF1E2230))
                ),
                width = 1.5.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = Color(0xFFB388FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MEJA NEGOSIASI M&A",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (lastResult == null) {
                        // Negotiation Controls View
                        NegotiationControlsView(
                            offer = offer,
                            counterMultiplier = counterMultiplier,
                            counterStake = counterStake,
                            minMultiplier = minMultiplier,
                            maxMultiplier = maxMultiplier,
                            counterCashDemand = counterCashDemand,
                            counterValuation = counterValuation,
                            premiumDemandPercent = premiumDemandPercent,
                            estimatedRiskPercent = estimatedRiskPercent,
                            onMultiplierChange = { counterMultiplier = it },
                            onStakeChange = { counterStake = it }
                        )
                    } else {
                        // Negotiation Result View
                        NegotiationOutcomeView(
                            result = lastResult,
                            onAcceptDeal = onAcceptDeal,
                            onTryAnotherCounter = {
                                // Reset for next counter if applicable
                            },
                            onClose = onDismiss
                        )
                    }
                }

                if (lastResult == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onSubmitCounter(
                                Math.round(counterMultiplier * 100.0) / 100.0,
                                Math.round(counterStake * 10.0) / 10.0
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("ma_btn_submit_counter"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7E57C2),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ajukan Penawaran Tandingan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NegotiationControlsView(
    offer: AcquisitionOffer,
    counterMultiplier: Double,
    counterStake: Double,
    minMultiplier: Double,
    maxMultiplier: Double,
    counterCashDemand: Long,
    counterValuation: Long,
    premiumDemandPercent: Double,
    estimatedRiskPercent: Int,
    onMultiplierChange: (Double) -> Unit,
    onStakeChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2235))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "INVESTOR: ${offer.bidder.name}",
                color = Color(0xFFFFD700),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tawaran Awal: ${String.format(Locale.US, "%.2f", offer.valuationMultiplier)}x Multiple ($${String.format(Locale.US, "%,d", offer.totalCashOffer)} USD)",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Slider 1: Valuation Multiple Demand (+15% to +50%)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181B28))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tuntutan Kenaikan Valuasi",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "+${String.format(Locale.US, "%.1f", premiumDemandPercent)}% (${String.format(Locale.US, "%.2f", counterMultiplier)}x)",
                    color = Color(0xFFB388FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = counterMultiplier.toFloat(),
                onValueChange = { onMultiplierChange(it.toDouble()) },
                valueRange = minMultiplier.toFloat()..maxMultiplier.toFloat(),
                modifier = Modifier.fillMaxWidth().testTag("ma_slider_multiplier"),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFB388FF),
                    activeTrackColor = Color(0xFF7E57C2),
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Konservatif (+15%)",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
                Text(
                    text = "Agresif (+50%)",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 10.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Slider 2: Adjusted Stake (10% to 60%)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181B28))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Porsi Saham Yang Dilepas",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${String.format(Locale.US, "%.1f", counterStake)}%",
                    color = Color(0xFF80D8FF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = counterStake.toFloat(),
                onValueChange = { onStakeChange(it.toDouble()) },
                valueRange = 10.0f..60.0f,
                modifier = Modifier.fillMaxWidth().testTag("ma_slider_stake"),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF80D8FF),
                    activeTrackColor = Color(0xFF0091EA),
                    inactiveTrackColor = Color.White.copy(alpha = 0.1f)
                )
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Dynamic Calculated Cash Demand
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF221E36)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFF7E57C2))),
            width = 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "TOTAL KAS YANG ANDA TUNTUT",
                color = Color(0xFFFFD700),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${String.format(Locale.US, "%,d", counterCashDemand)} USD",
                color = Color(0xFFFFD700),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Implied Valuasi Entitas: $${String.format(Locale.US, "%,d", counterValuation)} USD",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Risk of Walking Away Meter
    RiskMeterCard(estimatedRiskPercent = estimatedRiskPercent)
}

@Composable
private fun RiskMeterCard(estimatedRiskPercent: Int) {
    val riskColor = when {
        estimatedRiskPercent < 20 -> Color(0xFF81C784)
        estimatedRiskPercent < 32 -> Color(0xFFFFB74D)
        else -> Color(0xFFE57373)
    }

    val riskTag = when {
        estimatedRiskPercent < 20 -> "RENDAH (Peluang Sepakat Tinggi)"
        estimatedRiskPercent < 32 -> "SEDANG (Risiko Kompromi)"
        else -> "TINGGI (Risiko Investor Membatalkan Transaksi!)"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF171B26))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Risiko Investor Walk-Away",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "$estimatedRiskPercent%",
                    color = riskColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar Indicator
            LinearProgressIndicator(
                progress = { estimatedRiskPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = riskColor,
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = riskTag,
                color = riskColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun NegotiationOutcomeView(
    result: NegotiationResult,
    onAcceptDeal: () -> Unit,
    onTryAnotherCounter: () -> Unit,
    onClose: () -> Unit
) {
    val isSuccess = result.outcome == NegotiationOutcome.ACCEPTED
    val isCompromise = result.outcome == NegotiationOutcome.RE_COUNTERED
    val isWalkedAway = result.outcome == NegotiationOutcome.WALKED_AWAY

    val outcomeColor = when {
        isSuccess -> Color(0xFFFFD700)
        isCompromise -> Color(0xFF64B5F6)
        else -> Color(0xFFE57373)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2230)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(listOf(outcomeColor, Color(0xFF1E2230))),
            width = 1.5.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when {
                    isSuccess -> Icons.Default.CheckCircle
                    isCompromise -> Icons.Default.SwapHoriz
                    else -> Icons.Default.Cancel
                },
                contentDescription = null,
                tint = outcomeColor,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = result.headline,
                color = outcomeColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = result.detailMessage,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (!isWalkedAway) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151824))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "KAS AKHIR YANG DIINJEKSI",
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${String.format(Locale.US, "%,d", result.finalCashOffer)} USD",
                            color = Color(0xFFFFD700),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isWalkedAway) {
                Button(
                    onClick = onAcceptDeal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("ma_btn_accept_negotiated"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color(0xFF12141A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Sahkan & Terima Kas Sekarang",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else {
                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E3345),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Tutup Meja Negosiasi",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
