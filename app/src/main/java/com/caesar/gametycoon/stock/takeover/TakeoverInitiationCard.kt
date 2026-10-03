package com.caesar.gametycoon.stock.takeover

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StockItem
import java.util.Locale

/**
 * Modern M3 Card component positioned within the Stock Detail sheet.
 * Showcases the Strategic Takeover gateway with cumulative stake tracking and lock indicators.
 */
@Composable
fun TakeoverInitiationCard(
    stock: StockItem,
    currentOwnedShares: Long,
    isMegaHoldingActive: Boolean,
    isAlreadySubsidiary: Boolean,
    onInitiateTakeover: () -> Unit,
    onShowRequirementDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalShares = stock.sharesOutstanding.coerceAtLeast(1L)
    val currentStakePct = (currentOwnedShares.toDouble() / totalShares.toDouble()) * 100.0
    val gold = Color(0xFFFFD700)
    val purpleAccent = Color(0xFF8B5CF6)
    val cardDark = Color(0xFF1E1E1E)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("takeover_initiation_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(gold.copy(alpha = 0.4f), purpleAccent.copy(alpha = 0.4f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(gold.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BusinessCenter,
                            contentDescription = null,
                            tint = gold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Strategic Takeover (>50%)",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Akuisisi Pengendalian Korporasi",
                            color = Color(0xFFA0A0A0),
                            fontSize = 12.sp
                        )
                    }
                }

                // Stake Pill Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (currentStakePct > 0) gold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = "Hak Suara: ${String.format(Locale.US, "%.1f", currentStakePct)}%",
                        color = if (currentStakePct > 0) gold else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mega Holding Status Ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isMegaHoldingActive) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mega Holding Aktif — Siap Integrasi Governance",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Terkunci: Wajib Memiliki Mega Holding",
                        color = Color(0xFFF59E0B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button
            if (isAlreadySubsidiary) {
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color(0xFF333333),
                        disabledContentColor = Color.LightGray
                    )
                ) {
                    Text(
                        text = "👑 Telah Menjadi Anak Perusahaan (Subsidiary)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                Button(
                    onClick = {
                        if (isMegaHoldingActive) {
                            onInitiateTakeover()
                        } else {
                            onShowRequirementDialog()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_strategic_takeover_action"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMegaHoldingActive) gold else Color(0xFF2A2A2A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (!isMegaHoldingActive) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (isMegaHoldingActive) "🏛️ Mulai Negosiasi Akuisisi Saham (>50%)" else "Buka Syarat Akuisisi Mega Holding",
                        color = if (isMegaHoldingActive) Color(0xFF121212) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
