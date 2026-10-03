package com.caesar.gametycoon.stock.takeover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.StockItem
import java.text.NumberFormat
import java.util.Locale

/**
 * Interactive Term-Sheet window for Strategic Takeover Bidding negotiations.
 */
@Composable
fun TakeoverBiddingDialog(
    stock: StockItem,
    currentOwnedShares: Long,
    playerCash: Long,
    onDismiss: () -> Unit,
    onTakeoverFinalized: (finalPrice: Double, targetStake: Double, customName: String) -> Unit
) {
    var biddingState by remember {
        mutableStateOf(
            StockAcquisitionRepository.createInitialBiddingState(
                stock = stock,
                currentOwnedShares = currentOwnedShares
            )
        )
    }

    var offerInputText by remember {
        mutableStateOf(String.format(Locale.US, "%.2f", biddingState.playerOfferPricePerShare))
    }
    var subsidiaryNameInput by remember { mutableStateOf(stock.name) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val gold = Color(0xFFFFD700)
    val cardBg = Color(0xFF181818)
    val inputBg = Color(0xFF242424)
    val moneyFormatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Dialog(
        onDismissRequest = {
            if (!biddingState.isDealClosed) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .border(1.dp, gold.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                .testTag("takeover_bidding_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header: Logo, Ticker, Company Name, Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = Color(0xFF2A2A2A)
                        ) {
                            if (stock.logoUrl != null) {
                                AsyncImage(
                                    model = stock.logoUrl,
                                    contentDescription = stock.name,
                                    modifier = Modifier.padding(6.dp)
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = stock.ticker.take(2),
                                        fontWeight = FontWeight.Bold,
                                        color = gold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = stock.name,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${stock.ticker} • Pasar: $${String.format(Locale.US, "%.2f", stock.currentPrice)}",
                                color = Color(0xFFA0A0A0),
                                fontSize = 12.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Distress Banner if 0.3% Rare Event Triggered
                AnimatedVisibility(visible = biddingState.isDistressed) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFDC2626).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DISTRESS TAKEOVER EVENT: Korporasi mengalami krisis solvabilitas kritis! Penawaran diskon agresif berpeluang tinggi disetujui dewan.",
                                color = Color(0xFFFECACA),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Deal Closed View OR Negotiation Term Sheet
                    if (biddingState.isDealClosed) {
                        item {
                            DealClosedSuccessView(
                                agreedPrice = biddingState.playerOfferPricePerShare,
                                totalCost = biddingState.estimatedTotalCost,
                                targetStake = biddingState.targetStakePercent,
                                subsidiaryName = subsidiaryNameInput,
                                onNameChange = { subsidiaryNameInput = it },
                                playerCash = playerCash
                            )
                        }
                    } else if (biddingState.isWalkedAway) {
                        item {
                            WalkedAwayView(
                                reason = biddingState.statusMessage,
                                onRetry = {
                                    biddingState = StockAcquisitionRepository.createInitialBiddingState(
                                        stock = stock,
                                        currentOwnedShares = currentOwnedShares
                                    )
                                    offerInputText = String.format(Locale.US, "%.2f", biddingState.playerOfferPricePerShare)
                                }
                            )
                        }
                    } else {
                        // Slider: Target Controlling Stake
                        item {
                            ControllingStakeSlider(
                                targetStake = biddingState.targetStakePercent,
                                onStakeChange = { newStake ->
                                    biddingState = StockAcquisitionRepository.updateTargetStake(biddingState, newStake)
                                }
                            )
                        }

                        // Summary Statistics Grid
                        item {
                            FinancialSummaryCard(
                                biddingState = biddingState,
                                playerCash = playerCash,
                                formatter = moneyFormatter
                            )
                        }

                        // Board Sentiment & Acceptance Meter
                        item {
                            BoardSentimentMeter(
                                mood = biddingState.boardMood,
                                round = biddingState.negotiationRound,
                                maxRounds = biddingState.maxRounds,
                                boardAskingPrice = biddingState.currentBoardAskingPrice
                            )
                        }

                        // Player Bidding Input Box
                        item {
                            PlayerBidInputCard(
                                offerText = offerInputText,
                                onOfferChange = { text ->
                                    offerInputText = text
                                    text.toDoubleOrNull()?.let { newPrice ->
                                        biddingState = StockAcquisitionRepository.updatePlayerOffer(biddingState, newPrice)
                                    }
                                },
                                marketPrice = stock.currentPrice,
                                boardAsk = biddingState.currentBoardAskingPrice
                            )
                        }

                        // Negotiation History Log
                        if (biddingState.negotiationHistory.size > 1) {
                            item {
                                Text(
                                    text = "Log Negosiasi",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            items(biddingState.negotiationHistory.drop(1)) { entry ->
                                NegotiationLogItem(entry = entry)
                            }
                        }
                    }
                }

                // Error Message if any
                errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                ActionButtonsBar(
                    biddingState = biddingState,
                    playerCash = playerCash,
                    onSubmitBid = {
                        val parsed = offerInputText.toDoubleOrNull()
                        if (parsed == null || parsed <= 0.0) {
                            errorMessage = "Masukkan nominal tawaran yang valid."
                            return@ActionButtonsBar
                        }
                        errorMessage = null
                        val (newState, _) = StockAcquisitionRepository.submitPlayerBid(biddingState)
                        biddingState = newState
                        offerInputText = String.format(Locale.US, "%.2f", newState.playerOfferPricePerShare)
                    },
                    onAcceptCounter = {
                        biddingState = StockAcquisitionRepository.updatePlayerOffer(
                            biddingState,
                            biddingState.currentBoardAskingPrice
                        )
                        offerInputText = String.format(Locale.US, "%.2f", biddingState.currentBoardAskingPrice)
                        val (newState, _) = StockAcquisitionRepository.submitPlayerBid(biddingState)
                        biddingState = newState
                    },
                    onFinalizeDeal = {
                        if (playerCash < biddingState.estimatedTotalCost) {
                            errorMessage = "Kas tidak mencukupi untuk menyelesaikan transaksi."
                            return@ActionButtonsBar
                        }
                        onTakeoverFinalized(
                            biddingState.playerOfferPricePerShare,
                            biddingState.targetStakePercent,
                            subsidiaryNameInput
                        )
                    },
                    onCancel = onDismiss
                )
            }
        }
    }
}
