package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialHistoryScreen(
    onDismiss: () -> Unit,
    playerState: PlayerState
) {
    val navyBg = Color(0xFF0F141D)
    val darkCardBg = Color(0xFF1E242E)
    val textGray = Color(0xFF9FB2C6)
    val gold = Color(0xFFFFD700)
    val neonGreen = Color(0xFF00FF87)
    val red = Color(0xFFFF4D4D)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(1.dp, gold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📊 Histori Laba & Kinerja Finansial",
                    color = gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Catatan Keuangan 5 Tahun Terakhir",
                    color = textGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        containerColor = navyBg,
        textContentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Panel for Retained Earnings Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .border(1.dp, gold.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = darkCardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Laba Ditahan Saat Ini",
                            color = textGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Total Laba Ditahan (Retained Earnings):\n${formatCurrencyRingkas(playerState.retainedEarnings.toDouble(), false)}",
                            color = neonGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Card Laba Bersih Tahun Berjalan (Current Year Net Income)
                val monthInYear = if (playerState.inGameMonth <= 0) 1 else (playerState.inGameMonth - 1) % 12 + 1
                val currentYearHistory = playerState.financialHistory.takeLast(monthInYear)
                val currentYearNetIncome = currentYearHistory.sumOf { it.netIncome }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, neonGreen.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = darkCardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Laba Bersih Tahun Berjalan",
                            color = textGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${formatCurrencyRingkas(currentYearNetIncome.toDouble(), false)}",
                            color = neonGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ini setahun belakangan ini penghasilan bersihnya segini loh!",
                            color = textGray.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (playerState.financialHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada catatan keuangan bulanan.\n\nLanjutkan bulan berikutnya untuk mulai mencatat histori perusahaan otomatis.",
                            color = textGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val reversedHistory = playerState.financialHistory.reversed()
                        items(reversedHistory) { item ->
                            val isPositive = item.netIncome >= 0
                            val netColor = if (isPositive) neonGreen else red
                            val netSign = if (isPositive) "+" else "-"
                            val netIncomeValue = Math.abs(item.netIncome)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = darkCardBg.copy(alpha = 0.6f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Bulan ke-${item.monthTick}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Laba Bersih: $netSign${formatCurrencyRingkas(netIncomeValue.toDouble(), false)}",
                                            color = netColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.weight(1.2f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Pendapatan: +${formatCurrencyRingkas(item.totalRevenue.toDouble(), false)}",
                                            color = textGray,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Pengeluaran: -${formatCurrencyRingkas(item.totalExpense.toDouble(), false)}",
                                            color = textGray,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = gold, fontWeight = FontWeight.Bold)
            }
        }
    )
}
