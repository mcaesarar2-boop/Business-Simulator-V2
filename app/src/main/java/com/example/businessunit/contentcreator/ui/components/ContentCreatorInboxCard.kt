package com.example.businessunit.contentcreator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessunit.contentcreator.model.BrandDealOffer
import com.example.businessunit.contentcreator.model.BrandDealType
import com.example.ui.formatCurrencyRingkas

@Composable
fun ContentCreatorInboxCard(
    currentOffer: BrandDealOffer?,
    remainingOfferSeconds: Int = 10,
    onAcceptOffer: (BrandDealOffer) -> Unit,
    onRejectOffer: (BrandDealOffer) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = ContentCreatorTheme.AccentCyan, modifier = Modifier.size(18.dp))
                Text(
                    text = "Peti Masuk Penawaran (Sponsorship)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ContentCreatorTheme.TextWhite
                )
            }

            if (currentOffer != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ContentCreatorTheme.Gold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, ContentCreatorTheme.Gold.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = ContentCreatorTheme.Gold, modifier = Modifier.size(12.dp))
                        Text(
                            text = "${remainingOfferSeconds}s",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ContentCreatorTheme.Gold
                        )
                    }
                }
            }
        }

        if (currentOffer == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ContentCreatorTheme.AccentCyan.copy(alpha = 0.1f))
                            .border(1.dp, ContentCreatorTheme.AccentCyan.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ContentCreatorTheme.AccentCyan, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("Menunggu Tawaran Brand Masuk...", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ContentCreatorTheme.TextWhite)
                        Text(
                            "Brand partners memantau performa channel Anda. Tawaran baru akan masuk secara periodik atau lewat Pitch Sponsor (Durasi 10s & Freeze 10s).",
                            fontSize = 11.sp,
                            color = ContentCreatorTheme.TextGray
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
                border = BorderStroke(1.dp, ContentCreatorTheme.Gold.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(currentOffer.brandName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ContentCreatorTheme.TextWhite)
                            Text(currentOffer.categoryTag, fontSize = 10.sp, color = ContentCreatorTheme.TextGray)
                        }

                        Surface(
                            color = if (currentOffer.dealType == BrandDealType.CONTRACT) ContentCreatorTheme.AccentCyan.copy(alpha = 0.15f) else ContentCreatorTheme.NeonGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (currentOffer.dealType == BrandDealType.CONTRACT) "KONTRAK RESMI" else "SPONSOR SEKILAS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentOffer.dealType == BrandDealType.CONTRACT) ContentCreatorTheme.AccentCyan else ContentCreatorTheme.NeonGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (currentOffer.dealType == BrandDealType.CONTRACT)
                            "Nilai: ${formatCurrencyRingkas(currentOffer.monthlyPayout, false)} / bln selama ${currentOffer.durationMonths} Bulan (Total: ${formatCurrencyRingkas(currentOffer.contractValue, false)})"
                        else
                            "Nilai Instan: +${formatCurrencyRingkas(currentOffer.contractValue, false)} (Langsung cair)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ContentCreatorTheme.Gold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Offer Lifetime Progress Bar (10s)
                    LinearProgressIndicator(
                        progress = { (remainingOfferSeconds / 10f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = ContentCreatorTheme.Gold,
                        trackColor = Color.White.copy(alpha = 0.08f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onRejectOffer(currentOffer) },
                            modifier = Modifier.weight(1f).height(38.dp),
                            border = BorderStroke(1.dp, ContentCreatorTheme.ErrorRed),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Tolak", color = ContentCreatorTheme.ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onAcceptOffer(currentOffer) },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ContentCreatorTheme.NeonGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Terima Deal", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
