package com.example.ma.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ma.model.AcquisitionOffer
import com.example.ma.model.BidderType
import java.util.Locale

@Composable
fun MaOfferTermSheetDialog(
    offer: AcquisitionOffer,
    onAccept: () -> Unit,
    onCounter: () -> Unit,
    onStall: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("ma_offer_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF12141A)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFD700).copy(alpha = 0.6f),
                        Color(0xFF2E3345),
                        Color(0xFF1E2230)
                    )
                ),
                width = 1.5.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Header: Confidential Letter of Intent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFD700).copy(alpha = 0.15f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFFFD700), Color(0xFFFFA000))
                            ),
                            width = 1.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CONFIDENTIAL // LETTER OF INTENT",
                                color = Color(0xFFFFD700),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("ma_dialog_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Bidder Profile Badge
                    BidderProfileHeader(offer)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Target Entity & Scope Banner
                    TargetScopeCard(offer)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Primary Valuation & Cash Term Sheet Highlight
                    DealTermSheetHighlight(offer)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Formal Letter of Intent Body
                    LetterOfIntentBody(offer)

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons Matrix
                DealActionButtonsMatrix(
                    onAccept = onAccept,
                    onCounter = onCounter,
                    onStall = onStall,
                    onReject = onReject
                )
            }
        }
    }
}

@Composable
private fun BidderProfileHeader(offer: AcquisitionOffer) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1F2C))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Badge
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(offer.bidder.primaryColorHex)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = offer.bidder.avatarInitials,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = offer.bidder.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = offer.bidder.type.badgeTag,
                    color = Color(0xFFFFD700),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = " • ${offer.bidder.headquarters}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
            Text(
                text = "\"${offer.bidder.motto}\"",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TargetScopeCard(offer: AcquisitionOffer) {
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
                Text(
                    text = "TARGET ENTITAS",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(offer.targetLevel.badgeColorHex).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = offer.targetLevel.displayName,
                        color = Color(offer.targetLevel.badgeColorHex),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = offer.targetEntityName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Valuasi Saat Ini",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%,d", offer.currentEntityValuation)} USD",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Kepemilikan Anda",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", offer.currentOwnerStake)}% → ${String.format(Locale.US, "%.1f", offer.remainingOwnershipAfterDeal)}%",
                        color = if (offer.remainingOwnershipAfterDeal >= 50.0) Color(0xFF81C784) else Color(0xFFFFB74D),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DealTermSheetHighlight(offer: AcquisitionOffer) {
    val isPremium = offer.valuationMultiplier >= 1.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2333)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(
                    if (isPremium) Color(0xFFFFD700) else Color(0xFFE57373),
                    Color(0xFF262C3F)
                )
            ),
            width = 1.2.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOTAL PENAWARAN KAS TUNAI",
                    color = Color(0xFFFFD700),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = (if (isPremium) Color(0xFF4CAF50) else Color(0xFFE57373)).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = offer.dealPremiumText,
                        color = if (isPremium) Color(0xFF81C784) else Color(0xFFEF9A9A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$${String.format(Locale.US, "%,d", offer.totalCashOffer)} USD",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Porsi Saham Ditawar",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.1f", offer.stakePercent)}%",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Kelipatan Valuasi",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.2f", offer.valuationMultiplier)}x Multiple",
                        color = if (isPremium) Color(0xFF81C784) else Color(0xFFEF9A9A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Implied Valuasi 100%",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$${String.format(Locale.US, "%,d", offer.offeredValuation)} USD",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LetterOfIntentBody(offer: AcquisitionOffer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141722))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ISI LETTER OF INTENT (LOI)",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = offer.letterOfIntent,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = FontFamily.Serif
            )
        }
    }
}

@Composable
private fun DealActionButtonsMatrix(
    onAccept: () -> Unit,
    onCounter: () -> Unit,
    onStall: () -> Unit,
    onReject: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Row 1: Accept Cash Offer (Primary Gold)
        Button(
            onClick = onAccept,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("ma_btn_accept"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color(0xFF12141A)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Terima Tawaran Kas",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Counter / Negotiate & Stall Decision
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCounter,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("ma_btn_counter"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5E35B1),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PriceChange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Negosiasi",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            OutlinedButton(
                onClick = onStall,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("ma_btn_stall"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF64B5F6)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF64B5F6), Color(0xFF1976D2))),
                    width = 1.dp
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tunda (3 Bln)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 3: Reject Offer
        OutlinedButton(
            onClick = onReject,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .testTag("ma_btn_reject"),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFE57373)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(Color(0xFFE57373), Color(0xFFC62828))),
                width = 1.dp
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Tolak Penawaran",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            )
        }
    }
}
