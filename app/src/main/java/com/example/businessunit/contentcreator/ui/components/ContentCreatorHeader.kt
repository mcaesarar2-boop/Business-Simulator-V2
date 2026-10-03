package com.example.businessunit.contentcreator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessunit.contentcreator.model.ContentStatus
import com.example.data.OwnedBusiness
import com.example.ui.formatCurrencyRingkas
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ContentCreatorTopBar(
    onBack: () -> Unit,
    isCooldown: Boolean = false,
    cooldownSeconds: Int = 0,
    hasActiveOffer: Boolean = false,
    onPitchSponsor: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = ContentCreatorTheme.TextWhite
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ContentCreatorTheme.YouTubeRed.copy(alpha = 0.2f))
                    .border(1.dp, ContentCreatorTheme.YouTubeRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = ContentCreatorTheme.YouTubeRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = "Studio Content Creator",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ContentCreatorTheme.TextWhite
                )
                Text(
                    text = "YouTube & Sponsorship Engine",
                    fontSize = 11.sp,
                    color = ContentCreatorTheme.TextGray
                )
            }
        }

        OutlinedButton(
            onClick = onPitchSponsor,
            enabled = !isCooldown && !hasActiveOffer,
            border = BorderStroke(1.dp, if (isCooldown) Color.White.copy(alpha = 0.2f) else ContentCreatorTheme.Gold),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = ContentCreatorTheme.Gold,
                disabledContentColor = ContentCreatorTheme.TextGray
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when {
                    isCooldown -> "❄️ Freeze (${cooldownSeconds}s)"
                    hasActiveOffer -> "⏱️ Tawaran Aktif"
                    else -> "Pitch Sponsor"
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ContentCreatorDashboardHeader(
    business: OwnedBusiness,
    monthDurationSeconds: Float,
    monthCycleProgress: Float,
    onSuntikModal: () -> Unit,
    onTarikProfit: () -> Unit
) {
    val subsFormat = NumberFormat.getNumberInstance(Locale.US)
    val subs = business.contentCreatorSubscribers
    val level = business.level
    val employees = business.contentCreatorEmployees

    val adSenseRevenue = (subs * 0.05 * (1.0 + employees * 0.05)).toLong().coerceAtLeast(100L)
    val sponsorshipRevenue = business.contentCreatorContracts.sumOf { it.monthlyPayout }
    val royaltyRevenue = business.contentPortfolio.filter { it.status == ContentStatus.LICENSED }.sumOf { it.monthlyRoyalty }
    val totalPayoutEstimate = adSenseRevenue + sponsorshipRevenue + royaltyRevenue

    val valuation = (business.contentCreatorCash + totalPayoutEstimate * 36 + subs * 10).coerceAtLeast(100_000L)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Channel Title & Valuation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Dashboard Channel",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ContentCreatorTheme.TextWhite
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ContentCreatorTheme.NeonGreen))
                    Text(
                        text = if (level >= 40) "Enterprise Media Studio" else "Small-Medium Studio",
                        fontSize = 12.sp,
                        color = ContentCreatorTheme.NeonGreen
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), horizontalAlignment = Alignment.End) {
                    Text("Valuasi Channel", fontSize = 10.sp, color = ContentCreatorTheme.TextGray)
                    Text(
                        text = formatCurrencyRingkas(valuation, false),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ContentCreatorTheme.NeonGreen
                    )
                }
            }
        }

        // 2. Kas Usaha Channel Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Kas Usaha Channel", fontSize = 11.sp, color = ContentCreatorTheme.TextGray)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatCurrencyRingkas(business.contentCreatorCash, false),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ContentCreatorTheme.Gold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onSuntikModal,
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ContentCreatorTheme.NeonGreen.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.CallReceived, contentDescription = null, tint = ContentCreatorTheme.NeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Suntik Modal", color = ContentCreatorTheme.NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onTarikProfit,
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ContentCreatorTheme.AccentCyan.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.CallMade, contentDescription = null, tint = ContentCreatorTheme.AccentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("➔ Tarik Profit", color = ContentCreatorTheme.AccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. Statistik Utama & Payday Cycle
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Statistik Utama", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.TextWhite)
                    Surface(
                        color = ContentCreatorTheme.AccentMagenta.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "LEVEL $level/100",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ContentCreatorTheme.AccentMagenta,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("SUBSCRIBERS", fontSize = 9.sp, color = ContentCreatorTheme.TextGray, fontWeight = FontWeight.Bold)
                            Text(subsFormat.format(subs), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.TextWhite)
                            Text("Status: Tier ${level / 20 + 1}", fontSize = 10.sp, color = ContentCreatorTheme.AccentCyan)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("TIM PRODUKSI", fontSize = 9.sp, color = ContentCreatorTheme.TextGray, fontWeight = FontWeight.Bold)
                            Text("$employees Staf", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.TextWhite)
                            Text("Bonus AdSense: +${employees * 5}%", fontSize = 10.sp, color = ContentCreatorTheme.NeonGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payday Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Siklus Payday (1 Bulan Game)", fontSize = 11.sp, color = ContentCreatorTheme.TextGray)
                    val remainingSecs = ((1f - monthCycleProgress) * monthDurationSeconds).toInt()
                    Text("${remainingSecs}s (${(monthCycleProgress * 100).toInt()}%)", fontSize = 11.sp, color = ContentCreatorTheme.AccentCyan, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { monthCycleProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = ContentCreatorTheme.NeonGreen,
                    trackColor = Color.White.copy(alpha = 0.08f),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Breakdown list
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("AdSense Video:", fontSize = 11.sp, color = ContentCreatorTheme.TextGray)
                        Text("+${formatCurrencyRingkas(adSenseRevenue, false)} / bln", fontSize = 11.sp, color = ContentCreatorTheme.NeonGreen, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kontrak Sponsorship (${business.contentCreatorContracts.size}):", fontSize = 11.sp, color = ContentCreatorTheme.TextGray)
                        Text("+${formatCurrencyRingkas(sponsorshipRevenue, false)} / bln", fontSize = 11.sp, color = ContentCreatorTheme.Gold, fontWeight = FontWeight.SemiBold)
                    }
                    val licensedCount = business.contentPortfolio.count { it.status == ContentStatus.LICENSED }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Royalti Lisensi Bank Konten ($licensedCount Karya):", fontSize = 11.sp, color = ContentCreatorTheme.TextGray)
                        Text("+${formatCurrencyRingkas(royaltyRevenue, false)} / bln", fontSize = 11.sp, color = ContentCreatorTheme.AccentMagenta, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Estimasi Payout:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.TextWhite)
                        Text("+${formatCurrencyRingkas(totalPayoutEstimate, false)} / bln", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.NeonGreen)
                    }
                }
            }
        }
    }
}
