package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MegaHoldingsBoardConfig(
    onDismiss: () -> Unit,
    viewModel: GameViewModel
) {
    val playerState by viewModel.playerState.collectAsState()
    
    val navyBg = Color(0xFF0F141D)
    val darkCardBg = Color(0xFF1E242E)
    val textGray = Color(0xFF9FB2C6)
    val gold = Color(0xFFFFD700)
    val neonGreen = Color(0xFF00FF87)
    val red = Color(0xFFFF4D4D)

    var requestSliderValue by remember {
        mutableStateOf(playerState.currentCeoSalaryPercent.toFloat())
    }

    var requestDividendSliderValue by remember {
        mutableStateOf(playerState.currentDividendPercent.toFloat())
    }

    var requestTantiemSliderValue by remember {
        mutableStateOf(playerState.currentTantiemPercent.toFloat())
    }

    val megaHoldingMonthlyProfit = playerState.ownedBusinesses.sumOf {
        val ct = com.example.data.getCatalogItem(it.catalogId, playerState)
        if (ct != null) com.example.data.getBusinessStats(it, ct, playerState).let { (rev, mnt) -> rev - mnt } else 0L
    } + playerState.holdingCompanies.sumOf { h ->
        h.subsidiaries.sumOf { sub ->
            val ct = com.example.data.getCatalogItem(sub.catalogId, playerState)
            if (ct != null) com.example.data.getBusinessStats(sub, ct, playerState).let { (rev, mnt) -> rev - mnt } else 0L
        }
    }

    val estimasiGaji = (megaHoldingMonthlyProfit * (playerState.currentCeoSalaryPercent / 100.0)).toLong()
    val idleHoldingCash = playerState.holdingCompanies.sumOf { it.holdingCash } + playerState.ownedBusinesses.sumOf { it.companyCash }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(1.dp, gold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏛️ Rapat Dewan Direksi (RUPS)",
                    color = gold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
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
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Info Gaji Aktif & Dividen Aktif Row/Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(darkCardBg, RoundedCornerShape(10.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Gaji CEO Aktif",
                                color = textGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${formatCurrencyRingkas(estimasiGaji.toDouble(), false)}",
                                color = neonGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "(${playerState.currentCeoSalaryPercent}% Laba)",
                                color = textGray,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(darkCardBg, RoundedCornerShape(10.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Dividen Aktif",
                                color = textGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${playerState.currentDividendPercent}%",
                                color = neonGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "(Cair tiap 6 bln)",
                                color = textGray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section Pesan Dewan (boardReplyMessage)
                playerState.boardReplyMessage?.let { reply ->
                    val isApproved = reply.contains("DISETUJUI")
                    val alertColor = if (isApproved) neonGreen else red
                    val boxBgColor = if (isApproved) neonGreen.copy(alpha = 0.08f) else red.copy(alpha = 0.08f)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(boxBgColor, RoundedCornerShape(10.dp))
                            .border(1.dp, alertColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Keputusan RUPS Terakhir:",
                                    color = textGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(
                                    onClick = { viewModel.dismissBoardReplyMessage() },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.size(width = 54.dp, height = 24.dp)
                                ) {
                                    Text("Tutup", color = gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = reply,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ================= SECTION 1: GAJI CEO =================
                Text(
                    text = "💼 REMUNERASI KINERJA CEO",
                    color = gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(8.dp))

                val currentInGameMonth = playerState.inGameMonth
                val lastSalaryRequestMonth = playerState.lastSalaryRequestMonth
                val monthsSinceLastRequest = currentInGameMonth - lastSalaryRequestMonth
                val cooldownPeriod = 6
                val sisaCooldown = if (lastSalaryRequestMonth < 0 || lastSalaryRequestMonth > currentInGameMonth) {
                    0
                } else {
                    maxOf(0, cooldownPeriod - monthsSinceLastRequest)
                }

                if (playerState.pendingCeoSalaryPercent != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(gold.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .border(1.dp, gold.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "⏳ Pengajuan sebesar ${playerState.pendingCeoSalaryPercent}% sedang dievaluasi oleh Dewan Direksi.\n\nSisa waktu tunggu: ${playerState.boardApprovalMonthsLeft} bulan.",
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (sisaCooldown > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "🔒 Anda baru saja mengajukan RUPS. Dewan hanya menerima evaluasi gaji setiap 6 bulan.\n\n(Sisa cooldown: $sisaCooldown bulan)",
                            color = textGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val estimasiGajiSliderVal = (megaHoldingMonthlyProfit * (requestSliderValue / 100.0)).toLong()
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Estimasi: ${formatCurrencyRingkas(estimasiGajiSliderVal.toDouble(), false)} / bln",
                                color = neonGreen,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Berdasarkan ${String.format(java.util.Locale.US, "%.1f", requestSliderValue)}% dari Laba Holding Monthly",
                                color = textGray,
                                fontSize = 11.sp
                            )
                        }
                        Slider(
                            value = requestSliderValue,
                            onValueChange = { requestSliderValue = (it * 10f).roundToInt() / 10f },
                            valueRange = 0f..10f,
                            steps = 99,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = gold,
                                activeTrackColor = gold,
                                inactiveTrackColor = textGray.copy(alpha = 0.2f)
                            )
                        )
                        Text(
                            text = "Kenaikan bertahap (Maks 1.0%) memiliki peluang disetujui lebih tinggi.",
                            color = textGray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.submitCeoSalaryRequest(requestSliderValue.toDouble())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Ajukan Remunerasi Gaji", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))

                // ================= SECTION 2: DIVIDEN KORPORAT =================
                Text(
                    text = "📈 PENGAJUAN KEBIJAKAN DIVIDEN",
                    color = gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(8.dp))

                val monthsSinceLastDivRequest = currentInGameMonth - playerState.lastDividendRequestMonth
                val divCooldown = 12
                val sisaDivCooldown = if (playerState.lastDividendRequestMonth < 0 || playerState.lastDividendRequestMonth > currentInGameMonth) {
                    0
                } else {
                    maxOf(0, divCooldown - monthsSinceLastDivRequest)
                }

                if (playerState.pendingDividendPercent != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(gold.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .border(1.dp, gold.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "⏳ Pengajuan dividen ${playerState.pendingDividendPercent}% dievaluasi.\n\nSisa waktu tunggu: ${playerState.dividendApprovalMonthsLeft} bulan.",
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (sisaDivCooldown > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "🔒 Evaluasi dividen hanya bisa dilakukan setahun sekali.\n\n(Sisa cooldown: $sisaDivCooldown bulan)",
                            color = textGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (playerState.retainedEarnings <= 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(red.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .border(1.dp, red.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "🔒 Perusahaan tidak memiliki Laba Ditahan untuk dibagikan.",
                            color = red,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val labaEnamBulanTerakhir = playerState.financialHistory.takeLast(6).sumOf { it.netIncome }
                        val estimasiDividendPayout = (labaEnamBulanTerakhir * (requestDividendSliderValue / 100.0)).toLong()
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Pool Dividen: ${formatCurrencyRingkas(estimasiDividendPayout.toDouble(), false)}",
                                color = neonGreen,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Total Laba 6 Bulan Terakhir: ${formatCurrencyRingkas(labaEnamBulanTerakhir.toDouble(), false)}",
                                color = textGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                             )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Persentase Usulan: ${String.format(java.util.Locale.US, "%.1f", requestDividendSliderValue)}% dari Laba 6 Bulan Terakhir",
                                color = textGray,
                                fontSize = 11.sp
                            )
                        }
                        Slider(
                            value = requestDividendSliderValue,
                            onValueChange = { requestDividendSliderValue = (it * 10f).roundToInt() / 10f },
                            valueRange = 0f..50f,
                            steps = 499,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = gold,
                                activeTrackColor = gold,
                                inactiveTrackColor = textGray.copy(alpha = 0.2f)
                            )
                        )
                        Text(
                            text = "Tip: Pengajuan di atas 30% berisiko sangat tinggi ditolak dewan demi menjaga cashflow perusahaan.",
                            color = textGray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.submitDividendRequest(requestDividendSliderValue.toDouble())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Ajukan Dividen ke Dewan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(20.dp))

                // ================= SECTION 3: PENGAJUAN BONUS TAHUNAN (TANTIEM) =================
                Text(
                    text = "🎁 PENGAJUAN BONUS TAHUNAN (TANTIEM)",
                    color = gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (playerState.pendingTantiemPercent != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(gold.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .border(1.dp, gold.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "⏳ Pengajuan Tantiem sebesar ${playerState.pendingTantiemPercent}% sedang dievaluasi oleh Dewan.\n\nSisa waktu tunggu: ${playerState.tantiemApprovalMonthsLeft} bulan.",
                            color = Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val historyAnnualProfit = playerState.financialHistory.takeLast(12).sumOf { it.netIncome }
                        val estimasiTantiemPayout = (historyAnnualProfit * (requestTantiemSliderValue / 100.0)).toLong()
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Estimasi Bonus (Berdasarkan Kinerja 12 Bln Terakhir):",
                                color = textGray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${formatCurrencyRingkas(estimasiTantiemPayout.toDouble(), false)}",
                                color = gold,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Laba Bersih 12 Bln Terakhir: ${formatCurrencyRingkas(historyAnnualProfit.toDouble(), false)} (Usulan: ${String.format(java.util.Locale.US, "%.1f", requestTantiemSliderValue)}%)",
                                color = textGray,
                                fontSize = 11.sp
                            )
                        }

                        Slider(
                            value = requestTantiemSliderValue,
                            onValueChange = { requestTantiemSliderValue = (it * 10f).roundToInt() / 10f },
                            valueRange = 0f..10f,
                            steps = 99,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = gold,
                                activeTrackColor = gold,
                                inactiveTrackColor = textGray.copy(alpha = 0.2f)
                            )
                        )

                        Text(
                            text = "Bonus Tantiem dibagikan sekali setahun di akhir tahun finansial (Siklus 12 bulan) apabila Laba Bersih Tahunan bernilai positif.",
                            color = textGray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Button(
                            onClick = {
                                viewModel.submitTantiemRequest(requestTantiemSliderValue.toDouble())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Ajukan Tantiem ke Dewan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
