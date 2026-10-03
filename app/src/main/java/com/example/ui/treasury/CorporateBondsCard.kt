package com.example.ui.treasury

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.treasury.HoldingInvestmentEngine
import com.example.data.treasury.BondHolding
import com.example.data.treasury.BondType
import java.util.Locale

@Composable
fun CorporateBondsCard(
    holdings: List<BondHolding>,
    totalEstimatedCouponMonthly: Long,
    totalCouponEarnedAllTime: Long,
    useShortFormat: Boolean,
    onOpenBondMarket: () -> Unit,
    onLiquidateEarly: (BondHolding, Long) -> Unit,
    onClaimMatured: (BondHolding) -> Unit
) {
    val cardDark = Color(0xFF1E1E1E)
    val slateDark = Color(0xFF252A34)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val neonGreen = Color(0xFF00FF00)
    val red = Color(0xFFFF5252)

    val totalBondPrincipal = holdings.sumOf { it.principal }
    var selectedEarlySaleBond by remember { mutableStateOf<Pair<BondHolding, Long>?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Obligasi Negara & Korporasi",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    "Institutional Fixed-Income Securities",
                    color = textGray,
                    fontSize = 11.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Total: $${String.format(Locale.US, "%,d", totalBondPrincipal)}",
                    color = gold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                if (totalEstimatedCouponMonthly > 0) {
                    Text(
                        "+$${String.format(Locale.US, "%,d", totalEstimatedCouponMonthly)}/bln",
                        color = neonGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Button: Beli Obligasi
        Button(
            onClick = onOpenBondMarket,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = gold,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Bursa Obligasi (Beli)", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (holdings.isEmpty()) {
            Surface(
                color = cardDark,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Belum ada portofolio obligasi korporat atau SBN yang aktif. Beli surat utang di bursa untuk mengamankan kupon pendapatan tetap bulanan.",
                    color = textGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                holdings.forEach { holding ->
                    val estSecondaryPrice = remember(holding) {
                        HoldingInvestmentEngine.calculateBondSecondaryMarketValue(holding)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Top row: Title & Rating
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (holding.type == BondType.SOVEREIGN) Icons.Default.Security else Icons.Default.Business,
                                        contentDescription = null,
                                        tint = if (holding.type == BondType.SOVEREIGN) Color(0xFF64B5F6) else Color(0xFFFFB74D),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        holding.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                RatingBadge(holding.rating)
                            }

                            Text(
                                holding.issuer,
                                color = textGray,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Principal & Kupon Info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Pokok Penempatan", color = textGray, fontSize = 10.sp)
                                    Text(
                                        "$${String.format(Locale.US, "%,d", holding.principal)}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Kupon Bulanan", color = textGray, fontSize = 10.sp)
                                    Text(
                                        "+$${String.format(Locale.US, "%,d", holding.monthlyCouponAmount)}/bln",
                                        color = neonGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Kupon Diterima", color = textGray, fontSize = 10.sp)
                                    Text(
                                        "+$${String.format(Locale.US, "%,d", holding.totalCouponEarned)}",
                                        color = gold,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Tenure Progress Bar
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        if (holding.isMatured) "Status: JATUH TEMPO (Matang)" else "Sisa Waktu: ${holding.monthsRemaining} dari ${holding.totalTenureMonths} bln",
                                        color = if (holding.isMatured) neonGreen else textGray,
                                        fontSize = 11.sp,
                                        fontWeight = if (holding.isMatured) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        "${(holding.tenureProgress * 100).toInt()}%",
                                        color = if (holding.isMatured) neonGreen else textGray,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { holding.tenureProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (holding.isMatured) neonGreen else gold,
                                    trackColor = slateDark
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons
                            if (holding.isMatured) {
                                Button(
                                    onClick = { onClaimMatured(holding) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = neonGreen,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Klaim Pokok 100% ($${String.format(Locale.US, "%,d", holding.principal)})", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { selectedEarlySaleBond = Pair(holding, estSecondaryPrice) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFB74D)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "Jual Pasar Sekunder (~$${String.format(Locale.US, "%,d", estSecondaryPrice)})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Early Sale Confirmation Dialog
    if (selectedEarlySaleBond != null) {
        val (holding, estPrice) = selectedEarlySaleBond!!
        val diff = estPrice - holding.principal
        val diffPercent = ((diff.toDouble() / holding.principal.toDouble()) * 100.0)

        AlertDialog(
            onDismissRequest = { selectedEarlySaleBond = null },
            containerColor = cardDark,
            title = {
                Text("Likuidasi Pasar Sekunder", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Anda akan menjual obligasi '${holding.name}' sebelum jatuh tempo ke pasar sekunder institusi.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Modal Pokok Awal:", color = textGray, fontSize = 12.sp)
                                Text("$${String.format(Locale.US, "%,d", holding.principal)}", color = Color.White, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estimasi Dana Cair:", color = textGray, fontSize = 12.sp)
                                Text("$${String.format(Locale.US, "%,d", estPrice)}", color = gold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Capital Gain/Loss:", color = textGray, fontSize = 12.sp)
                                Text(
                                    "${if (diff >= 0) "+" else ""}$${String.format(Locale.US, "%,d", diff)} (${String.format(Locale.US, "%.2f", diffPercent)}%)",
                                    color = if (diff >= 0) neonGreen else red,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onLiquidateEarly(holding, estPrice)
                        selectedEarlySaleBond = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                ) {
                    Text("Jual Sekarang", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedEarlySaleBond = null }) {
                    Text("Batal", color = textGray)
                }
            }
        )
    }
}
