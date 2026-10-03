package com.example.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerState
import com.example.data.totalLiabilities
import com.example.ui.formatCurrencyRingkas

object ProfileTheme {
    val CardDark = Color(0xFF1A1E24)
    val NeonGreen = Color(0xFF00FF00)
    val TextGray = Color(0xFFA0A0A0)
    val Gold = Color(0xFFFFD700)
    val Red = Color(0xFFFF453A)
    val AccentCyan = Color(0xFF00E5FF)
}

/**
 * Clean, modern header card showing Total Fortune along with Gross Fortune (GAV) and Corporate Liabilities (PE Debt).
 */
@Composable
fun ProfileFortuneHeader(
    totalWealth: Long,
    playerState: PlayerState,
    useShortFormat: Boolean
) {
    val totalLiabilities = playerState.totalLiabilities
    val grossFortune = totalWealth + totalLiabilities

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = formatCurrencyRingkas(totalWealth, useShortFormat),
            color = ProfileTheme.NeonGreen,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Total Fortune (Net Worth)",
            color = ProfileTheme.TextGray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Corporate Liabilities & Gross Fortune Row (Moved to front dashboard as requested)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Gross Fortune (GAV)",
                    fontSize = 10.sp,
                    color = ProfileTheme.TextGray
                )
                Text(
                    text = formatCurrencyRingkas(grossFortune, useShortFormat),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Total Liabilities (Debt)",
                    fontSize = 10.sp,
                    color = ProfileTheme.TextGray
                )
                Text(
                    text = if (totalLiabilities > 0) "-${formatCurrencyRingkas(totalLiabilities, useShortFormat)}" else "$0",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (totalLiabilities > 0) ProfileTheme.Red else Color.White
                )
            }
        }
    }
}

/**
 * Quick action card to inject capital from Private Balance into Company Cash.
 * Redesigned with proper responsive constraints, crisp badges, and no text wrapping glitches.
 */
@Composable
fun CapitalInjectionQuickPill(
    privateBalance: Long,
    onClick: () -> Unit
) {
    if (privateBalance <= 0L) return

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E211A)),
        border = BorderStroke(1.dp, ProfileTheme.Gold.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(ProfileTheme.Gold.copy(alpha = 0.15f))
                    .border(1.dp, ProfileTheme.Gold.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Inject Capital",
                    tint = ProfileTheme.Gold,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Suntik Modal ke Kas Utama",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfileTheme.Gold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Transfer 1 arah: Kas Pribadi (${formatCurrencyRingkas(privateBalance, false)}) ➔ Kas Perusahaan",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ProfileTheme.Gold,
                contentColor = Color.Black
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Suntik",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
