package com.example.privateequity.capitalinjection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.formatCurrencyRingkas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CapitalInjectionDialog(
    currentPrivateBalance: Long,
    currentCompanyCash: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var injectionSliderValue by remember {
        mutableStateOf(if (currentPrivateBalance > 0) (currentPrivateBalance * 0.25).coerceAtLeast(1.0) else 0.0)
    }

    val maxAmount = currentPrivateBalance.coerceAtLeast(0L)
    val chosenAmount = injectionSliderValue.toLong().coerceIn(0L, maxAmount)

    val newPrivateBalance = (currentPrivateBalance - chosenAmount).coerceAtLeast(0L)
    val newCompanyCash = currentCompanyCash + chosenAmount

    val bgDark = Color(0xFF151921)
    val cardDark = Color(0xFF1E2530)
    val accentCyan = Color(0xFF00E5FF)
    val neonGreen = Color(0xFF00FF00)
    val textGray = Color(0xFFA0A0A0)
    val textWhite = Color.White
    val gold = Color(0xFFFFD700)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = bgDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💉 Suntik Modal ke Kas Utama",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textWhite
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Alirkan modal likuid pribadi (Family Office) ke kas operasional perusahaan untuk mendanai akuisisi bisnis baru.",
                    fontSize = 12.sp,
                    color = textGray
                )

                // Visual Flow Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("👤 Kas Pribadi", fontSize = 11.sp, color = textGray)
                            Text(
                                text = formatCurrencyRingkas(currentPrivateBalance, false),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentCyan
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Transfer",
                            tint = neonGreen,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("🏢 Kas Utama", fontSize = 11.sp, color = textGray)
                            Text(
                                text = formatCurrencyRingkas(currentCompanyCash, false),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = neonGreen
                            )
                        }
                    }
                }

                if (currentPrivateBalance <= 0L) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2D1616), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Kas pribadi (Family Office) Anda saat ini $0. Dapatkan dividen atau tantiem terlebih dahulu untuk menambah kas pribadi.",
                            color = Color(0xFFFFA4A4),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Slider & Quick Amount Selection
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nominal Suntik Modal:",
                                fontSize = 12.sp,
                                color = textGray
                            )
                            Text(
                                text = formatCurrencyRingkas(chosenAmount, false),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = gold
                            )
                        }

                        Slider(
                            value = injectionSliderValue.toFloat(),
                            onValueChange = { injectionSliderValue = it.toDouble() },
                            valueRange = 0f..maxAmount.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = gold,
                                activeTrackColor = gold,
                                inactiveTrackColor = textGray.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick Percentage Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val chips = listOf(
                                "25%" to 0.25,
                                "50%" to 0.50,
                                "75%" to 0.75,
                                "100% (Max)" to 1.0
                            )
                            chips.forEach { (label, fraction) ->
                                val targetVal = (maxAmount * fraction).toLong()
                                val isSelected = chosenAmount == targetVal
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) gold.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                                        .border(
                                            1.dp,
                                            if (isSelected) gold else Color.White.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            injectionSliderValue = (maxAmount * fraction).coerceAtLeast(0.0)
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) gold else textWhite
                                    )
                                }
                            }
                        }
                    }

                    // Result Preview
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(10.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sisa Kas Pribadi:", fontSize = 11.sp, color = textGray)
                            Text(
                                text = formatCurrencyRingkas(newPrivateBalance, false),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textWhite
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kas Utama Baru:", fontSize = 11.sp, color = textGray)
                            Text(
                                text = formatCurrencyRingkas(newCompanyCash, false),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = neonGreen
                            )
                        }
                    }

                    // One-Way Transfer Warning
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E2838), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = accentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Transaksi Satu Arah: Dana yang disuntikkan menjadi kas perusahaan dan tidak dapat ditarik langsung ke rekening pribadi.",
                            fontSize = 10.sp,
                            color = textGray,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (chosenAmount > 0L) {
                        onConfirm(chosenAmount)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = gold,
                    contentColor = Color.Black
                ),
                enabled = chosenAmount > 0L && currentPrivateBalance > 0L,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Suntikkan Dana (${formatCurrencyRingkas(chosenAmount, false)})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = textGray, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
