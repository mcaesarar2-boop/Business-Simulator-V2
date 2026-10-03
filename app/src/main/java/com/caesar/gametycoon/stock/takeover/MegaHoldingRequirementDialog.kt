package com.caesar.gametycoon.stock.takeover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Compliance warning modal displayed when a player attempts to accumulate or bid
 * on a controlling stake (>50%) without an active Mega Holding governance entity.
 */
@Composable
fun MegaHoldingRequirementDialog(
    onDismiss: () -> Unit,
    onNavigateToMegaHolding: (() -> Unit)? = null
) {
    val gold = Color(0xFFFFD700)
    val cardDark = Color(0xFF1E1E1E)
    val amber = Color(0xFFF59E0B)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, amber.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .testTag("mega_holding_requirement_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(amber.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                        .border(1.dp, amber.copy(alpha = 0.5f), RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Mega Holding Required",
                        tint = gold,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Akses Akuisisi Terkunci",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Strategic Controlling Stake (>50%) requires a Mega Holding Entity to integrate corporate governance.",
                    color = amber,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Batas perdagangan saham ritel dibatasi maksimal kumulatif 49.9%. Untuk mengeksekusi hak suara mayoritas dan mengintegrasikan target ke dalam pembagian arus kas 3-tier, Anda harus memiliki entitas Mega Holding aktif.",
                    color = Color(0xFFB0B0B0),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (onNavigateToMegaHolding != null) {
                    Button(
                        onClick = {
                            onDismiss()
                            onNavigateToMegaHolding()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = gold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_go_to_mega_holding")
                    ) {
                        Text(
                            text = "Dirikan Mega Holding",
                            color = Color(0xFF121212),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedButton(
                    onClick = onDismiss,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_close_requirement_dialog")
                ) {
                    Text(
                        text = "Mengerti",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
