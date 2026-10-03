package com.example.privateequity.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.privateequity.model.ActiveLoan
import com.example.privateequity.model.FundingType
import com.example.privateequity.model.LoanEstimation
import com.example.privateequity.model.SectorOffer
import com.example.ui.formatCurrencyRingkas
import java.util.Locale

object PrivateEquityTheme {
    val BgDark = Color(0xFF0F172A)
    val CardDark = Color(0xFF1E293B)
    val AccentCyan = Color(0xFF00E5FF)
    val TextWhite = Color.White
    val TextGray = Color(0xFF94A3B8)
    val NeonGreen = Color(0xFF10B981)
    val ErrorRed = Color(0xFFEF4444)
    val Gold = Color(0xFFFFD700)
}

@Composable
fun PrivateEquityPortfolioCard(
    playerEquityShare: Double,
    totalBusinessValuation: Long,
    totalOutstandingDebt: Long,
    totalMonthlyPayment: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrivateEquityTheme.CardDark),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PORTFOLIO PEMBIAYAAN & EKUITAS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PrivateEquityTheme.AccentCyan,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Saham Tersisa", fontSize = 11.sp, color = PrivateEquityTheme.TextGray)
                    Text(
                        text = String.format(Locale.US, "%.1f%%", playerEquityShare),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (playerEquityShare < 60.0) PrivateEquityTheme.ErrorRed else PrivateEquityTheme.TextWhite
                    )
                }
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Total Valuasi Bisnis", fontSize = 11.sp, color = PrivateEquityTheme.TextGray)
                    Text(
                        text = formatCurrencyRingkas(totalBusinessValuation, false),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrivateEquityTheme.TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Outstanding Debt PE", fontSize = 11.sp, color = PrivateEquityTheme.TextGray)
                    Text(
                        text = formatCurrencyRingkas(totalOutstandingDebt, false),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalOutstandingDebt > 0) PrivateEquityTheme.ErrorRed else PrivateEquityTheme.TextWhite
                    )
                }
                Column(modifier = Modifier.weight(1.2f)) {
                    Text("Beban Cicilan/Bulan", fontSize = 11.sp, color = PrivateEquityTheme.TextGray)
                    Text(
                        text = formatCurrencyRingkas(totalMonthlyPayment, false),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalMonthlyPayment > 0) PrivateEquityTheme.ErrorRed else PrivateEquityTheme.TextWhite
                    )
                }
            }
        }
    }
}

@Composable
fun SectorSelectorSection(
    sectors: List<SectorOffer>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit
) {
    val selectedSector = sectors[selectedIndex]

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = PrivateEquityTheme.AccentCyan,
        edgePadding = 0.dp,
        divider = {}
    ) {
        sectors.forEachIndexed { idx, sector ->
            Tab(
                selected = selectedIndex == idx,
                onClick = { onSelectIndex(idx) },
                text = {
                    Text(
                        sector.name,
                        fontSize = 12.sp,
                        color = if (selectedIndex == idx) PrivateEquityTheme.TextWhite else PrivateEquityTheme.TextGray
                    )
                }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = selectedSector.description,
        fontSize = 12.sp,
        color = PrivateEquityTheme.TextGray,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Tenor Kontrak", fontSize = 11.sp, color = PrivateEquityTheme.TextGray)
            Text("${selectedSector.tenor} Bulan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrivateEquityTheme.TextWhite)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Suku Bunga", fontSize = 11.sp, color = PrivateEquityTheme.TextGray)
            Text("${(selectedSector.interestRate * 100).toInt()}% Flat", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrivateEquityTheme.TextWhite)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Multiplier Resiko", fontSize = 11.sp, color = PrivateEquityTheme.TextGray)
            Text("${selectedSector.dilutionMultiplier}x", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrivateEquityTheme.TextWhite)
        }
    }
}

@Composable
fun LoanSliderSection(
    loanAmount: Long,
    sliderValue: Double,
    maxLimit: Long,
    onSliderChange: (Double) -> Unit
) {
    Text(
        text = "Jumlah Dana Diajukan: ${formatCurrencyRingkas(loanAmount, false)}",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = PrivateEquityTheme.TextWhite
    )
    Slider(
        value = sliderValue.coerceIn(10_000.0, maxLimit.toDouble()).toFloat(),
        onValueChange = { onSliderChange(it.toDouble()) },
        valueRange = 10_000f..maxLimit.toFloat(),
        colors = SliderDefaults.colors(
            thumbColor = PrivateEquityTheme.AccentCyan,
            activeTrackColor = PrivateEquityTheme.AccentCyan,
            inactiveTrackColor = PrivateEquityTheme.TextGray.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(formatCurrencyRingkas(10_000L, true), fontSize = 10.sp, color = PrivateEquityTheme.TextGray)
        Text("Max: " + formatCurrencyRingkas(maxLimit, true), fontSize = 10.sp, color = PrivateEquityTheme.TextGray)
    }
}

@Composable
fun FundingTypeSelector(
    selectedType: FundingType,
    baseMonthlyPayment: Long,
    baseEquityGiven: Double,
    onSelectType: (FundingType) -> Unit
) {
    Text(
        text = "PILIH OPSI PENDANAAN",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = PrivateEquityTheme.TextGray,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        FundingType.values().forEach { type ->
            val isSelected = selectedType == type
            val (title, desc, details) = when (type) {
                FundingType.DEBT -> Triple(
                    "Debt Financing (Hutang Murni)",
                    "Pembayaran cicilan bulanan penuh, tanpa dilusi saham (0.0%).",
                    "Cicilan: ${formatCurrencyRingkas(baseMonthlyPayment, false)} | Saham: 0.0%"
                )
                FundingType.HYBRID -> Triple(
                    "Mezzanine (Hybrid)",
                    "Pembayaran cicilan 50%, dengan dilusi saham proporsional.",
                    "Cicilan: ${formatCurrencyRingkas(baseMonthlyPayment / 2, false)} | Saham: ${String.format(Locale.US, "%.2f%%", baseEquityGiven)}"
                )
                FundingType.EQUITY -> Triple(
                    "Venture Capital (Jual Saham)",
                    "Tanpa cicilan bulanan, melepas kepemilikan saham penuh.",
                    "Cicilan: $0 | Saham: ${String.format(Locale.US, "%.2f%%", baseEquityGiven)}"
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) PrivateEquityTheme.AccentCyan.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.02f))
                    .border(
                        width = 1.dp,
                        color = if (isSelected) PrivateEquityTheme.AccentCyan else Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectType(type) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onSelectType(type) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = PrivateEquityTheme.AccentCyan,
                        unselectedColor = PrivateEquityTheme.TextGray
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrivateEquityTheme.TextWhite)
                    Text(desc, fontSize = 11.sp, color = PrivateEquityTheme.TextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        details,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) PrivateEquityTheme.AccentCyan else PrivateEquityTheme.Gold
                    )
                }
            }
        }
    }
}

@Composable
fun EstimationBreakdownCard(estimation: LoanEstimation) {
    Text(
        text = "ESTIMASI TRANSAKSI SEBELUM DIAJUKAN",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = PrivateEquityTheme.TextGray
    )
    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.01f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Estimasi Dilusi Saham", fontSize = 12.sp, color = PrivateEquityTheme.TextGray)
            Text(
                text = String.format(Locale.US, "-%.2f%%", estimation.equityGiven),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (estimation.equityGiven > 0) PrivateEquityTheme.ErrorRed else PrivateEquityTheme.TextGray
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Estimasi Sisa Saham Anda", fontSize = 12.sp, color = PrivateEquityTheme.TextGray)
            Text(
                text = String.format(Locale.US, "%.2f%%", estimation.remainingEquityPostDilution),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (estimation.remainingEquityPostDilution < 51.0) PrivateEquityTheme.ErrorRed else PrivateEquityTheme.NeonGreen
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Estimasi Cicilan per Bulan", fontSize = 12.sp, color = PrivateEquityTheme.TextGray)
            Text(
                text = formatCurrencyRingkas(estimation.monthlyPayment, false),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PrivateEquityTheme.TextWhite
            )
        }
    }

    if (estimation.isControlViolation) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Batas kontrol mayoritas tercapai! Anda tidak bisa melepas saham lagi (Minimal 51%).",
            color = PrivateEquityTheme.ErrorRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun LazyListScope.activeLoansListItems(loans: List<ActiveLoan>) {
    item {
        Text(
            text = "Daftar Hutang & Pembiayaan Aktif (${loans.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PrivateEquityTheme.TextWhite,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    if (loans.isEmpty()) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.01f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada hutang atau pembiayaan aktif kepada investor saat ini.",
                    color = PrivateEquityTheme.TextGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        items(loans) { loan ->
            val outstandingVal = loan.monthlyPayment * loan.remainingMonths
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrivateEquityTheme.CardDark.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = loan.sectorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrivateEquityTheme.AccentCyan
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (loan.fundingType) {
                                            FundingType.DEBT -> PrivateEquityTheme.Gold.copy(alpha = 0.15f)
                                            FundingType.HYBRID -> PrivateEquityTheme.AccentCyan.copy(alpha = 0.15f)
                                            FundingType.EQUITY -> PrivateEquityTheme.ErrorRed.copy(alpha = 0.15f)
                                        },
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = loan.fundingType.name,
                                    fontSize = 10.sp,
                                    color = when (loan.fundingType) {
                                        FundingType.DEBT -> PrivateEquityTheme.Gold
                                        FundingType.HYBRID -> PrivateEquityTheme.AccentCyan
                                        FundingType.EQUITY -> PrivateEquityTheme.ErrorRed
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(PrivateEquityTheme.AccentCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${loan.remainingMonths} bln tersisa",
                                    fontSize = 11.sp,
                                    color = PrivateEquityTheme.AccentCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Cicilan Bulanan", fontSize = 11.sp, color = PrivateEquityTheme.TextGray)
                            Text(formatCurrencyRingkas(loan.monthlyPayment, false), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrivateEquityTheme.TextWhite)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Saham Digadaikan", fontSize = 11.sp, color = PrivateEquityTheme.TextGray)
                            Text(String.format(Locale.US, "%.1f%%", loan.equityGiven), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrivateEquityTheme.ErrorRed)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Sisa Hutang", fontSize = 11.sp, color = PrivateEquityTheme.TextGray)
                            Text(formatCurrencyRingkas(outstandingVal, false), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrivateEquityTheme.Gold)
                        }
                    }
                }
            }
        }
    }
}
