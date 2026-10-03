package com.caesar.gametycoon.stock.takeover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ControllingStakeSlider(
    targetStake: Double,
    onStakeChange: (Double) -> Unit
) {
    val gold = Color(0xFFFFD700)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF202020), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Target Hak Suara / Kepemilikan",
                color = Color.LightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = gold.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${String.format(Locale.US, "%.1f", targetStake)}%",
                    color = gold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }

        Slider(
            value = targetStake.toFloat(),
            onValueChange = { onStakeChange(it.toDouble()) },
            valueRange = 51.0f..100.0f,
            steps = 48,
            colors = SliderDefaults.colors(
                thumbColor = gold,
                activeTrackColor = gold,
                inactiveTrackColor = Color(0xFF333333)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("stake_slider")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("51% (Mayoritas Pengendali)", color = Color.Gray, fontSize = 10.sp)
            Text("100% (Buyout Penuh)", color = Color.Gray, fontSize = 10.sp)
        }
    }
}

@Composable
fun FinancialSummaryCard(
    biddingState: TakeoverBiddingState,
    playerCash: Long,
    formatter: NumberFormat
) {
    val isAffordable = playerCash >= biddingState.estimatedTotalCost
    val gold = Color(0xFFFFD700)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF202020), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Lembar Saham Dibeli", color = Color.Gray, fontSize = 12.sp)
            Text("${formatter.format(biddingState.additionalSharesNeeded)} lembar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Estimasi Biaya Akuisisi", color = Color.Gray, fontSize = 12.sp)
            Text(
                "$${formatter.format(biddingState.estimatedTotalCost)}",
                color = if (isAffordable) gold else Color(0xFFEF4444),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(color = Color(0xFF303030), thickness = 0.8.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Kas Tersedia", color = Color.Gray, fontSize = 12.sp)
            Text(
                "$${formatter.format(playerCash)}",
                color = if (isAffordable) Color(0xFF10B981) else Color(0xFFEF4444),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BoardSentimentMeter(
    mood: BoardMoodInfo,
    round: Int,
    maxRounds: Int,
    boardAskingPrice: Double
) {
    val moodColor = Color(mood.moodColorHex)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF202020), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Sentimen Dewan Direksi", color = Color.LightGray, fontSize = 11.sp)
                Text(
                    text = mood.moodLabel,
                    color = moodColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                Text(
                    text = "Putaran $round/$maxRounds",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Probability Progress Bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { mood.acceptanceProbability.toFloat() },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp),
                color = moodColor,
                trackColor = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "${(mood.acceptanceProbability * 100).toInt()}% Setuju",
                color = moodColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tuntutan Dewan: $${String.format(Locale.US, "%.2f", boardAskingPrice)}/lembar",
            color = Color(0xFFA0A0A0),
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = mood.commentary,
            color = Color(0xFFCCCCCC),
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}

@Composable
fun PlayerBidInputCard(
    offerText: String,
    onOfferChange: (String) -> Unit,
    marketPrice: Double,
    boardAsk: Double
) {
    val inputBg = Color(0xFF282828)
    val gold = Color(0xFFFFD700)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF202020), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Text("Tawaran Harga Anda (USD per lembar)", color = Color.LightGray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = offerText,
            onValueChange = onOfferChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            leadingIcon = {
                Text("$", color = gold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = inputBg,
                unfocusedContainerColor = inputBg,
                focusedBorderColor = gold,
                unfocusedBorderColor = Color(0xFF444444),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bid_price_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Preset Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PresetButton(
                label = "Pasar ($${String.format(Locale.US, "%.0f", marketPrice)})",
                onClick = { onOfferChange(String.format(Locale.US, "%.2f", marketPrice)) },
                modifier = Modifier.weight(1f)
            )
            PresetButton(
                label = "+15% Premi",
                onClick = { onOfferChange(String.format(Locale.US, "%.2f", marketPrice * 1.15)) },
                modifier = Modifier.weight(1f)
            )
            PresetButton(
                label = "Tuntutan Dewan",
                onClick = { onOfferChange(String.format(Locale.US, "%.2f", boardAsk)) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PresetButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF2E2E2E),
        modifier = modifier.height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun NegotiationLogItem(entry: NegotiationLogEntry) {
    val isAccepted = entry.resultType == "ACCEPTED"
    val accentColor = if (isAccepted) Color(0xFF10B981) else Color(0xFF3B82F6)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1C1C1C), RoundedCornerShape(8.dp))
            .border(0.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Putaran ${entry.round} • Tawaran: $${String.format(Locale.US, "%.2f", entry.playerOffer)}", color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(entry.resultType, color = Color.Gray, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(entry.boardResponse, color = Color(0xFFCCCCCC), fontSize = 11.sp, lineHeight = 15.sp)
    }
}

@Composable
fun DealClosedSuccessView(
    agreedPrice: Double,
    totalCost: Long,
    targetStake: Double,
    subsidiaryName: String,
    onNameChange: (String) -> Unit,
    playerCash: Long
) {
    val gold = Color(0xFFFFD700)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(16.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "KESEPAKATAN TERCAPAI!",
            color = Color(0xFF10B981),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Dewan resmi menyetujui pelepasan ${String.format(Locale.US, "%.1f", targetStake)}% hak suara di harga $${String.format(Locale.US, "%.2f", agreedPrice)}/lembar (Total: $$totalCost).",
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Beri Nama Unit Bisnis / Anak Perusahaan:",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = subsidiaryName,
            onValueChange = onNameChange,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF181818),
                unfocusedContainerColor = Color(0xFF181818),
                focusedBorderColor = gold,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("subsidiary_name_input")
        )
    }
}

@Composable
fun WalkedAwayView(
    reason: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEF4444).copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(16.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "NEGOSIASI TERPUTUS",
            color = Color(0xFFEF4444),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = reason,
            color = Color.White,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E2E)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Buka Sesi Negosiasi Baru", color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
fun ActionButtonsBar(
    biddingState: TakeoverBiddingState,
    playerCash: Long,
    onSubmitBid: () -> Unit,
    onAcceptCounter: () -> Unit,
    onFinalizeDeal: () -> Unit,
    onCancel: () -> Unit
) {
    val gold = Color(0xFFFFD700)
    val isAffordable = playerCash >= biddingState.estimatedTotalCost

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (biddingState.isDealClosed) {
            Button(
                onClick = onFinalizeDeal,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp),
                enabled = isAffordable,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_finalize_takeover")
            ) {
                Text(
                    text = "🏛️ Selesaikan Pengambilalihan & Merge ke Holding",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        } else if (!biddingState.isWalkedAway) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAcceptCounter,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, gold.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_accept_board_counter")
                ) {
                    Text(
                        text = "Terima Dewan",
                        color = gold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onSubmitBid,
                    colors = ButtonDefaults.buttonColors(containerColor = gold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_submit_bid_offer")
                ) {
                    Text(
                        text = "Ajukan Tawaran",
                        color = Color(0xFF121212),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onCancel,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("btn_cancel_takeover")
        ) {
            Text(
                text = if (biddingState.isDealClosed) "Tutup" else "Batalkan Negosiasi",
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}
