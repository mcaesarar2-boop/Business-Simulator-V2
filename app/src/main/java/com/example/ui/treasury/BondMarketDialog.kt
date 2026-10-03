package com.example.ui.treasury

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.treasury.BondType
import com.example.data.treasury.CorporateBond
import com.example.data.treasury.CreditRating
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BondMarketDialog(
    bonds: List<CorporateBond>,
    holdingCash: Long,
    onDismiss: () -> Unit,
    onPurchaseBond: (CorporateBond, Long) -> Unit
) {
    val bgDark = Color(0xFF1E1E1E)
    val cardDark = Color(0xFF282828)
    val gold = Color(0xFFFFD700)
    val neonGreen = Color(0xFF00FF00)
    val textGray = Color(0xFFA0A0A0)

    var selectedFilter by remember { mutableStateOf("Semua") }
    var selectedBond by remember { mutableStateOf<CorporateBond?>(null) }
    var investAmountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filterOptions = listOf("Semua", "Negara (SBN)", "Korporasi AAA/AA", "High-Yield (BBB)")

    val filteredBonds = remember(selectedFilter, bonds) {
        when (selectedFilter) {
            "Negara (SBN)" -> bonds.filter { it.type == BondType.SOVEREIGN }
            "Korporasi AAA/AA" -> bonds.filter { it.type == BondType.CORPORATE && (it.rating == CreditRating.AAA || it.rating == CreditRating.AA) }
            "High-Yield (BBB)" -> bonds.filter { it.rating == CreditRating.BBB }
            else -> bonds
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bgDark,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Bursa Obligasi Korporat & SBN",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            "Treasury Fixed-Income Market",
                            color = gold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filterOptions.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) gold else Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedFilter = filter }
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedBond == null) {
                    // Bond List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredBonds) { bond ->
                            BondMarketItem(
                                bond = bond,
                                onClick = {
                                    selectedBond = bond
                                    investAmountText = bond.minInvestment.toString()
                                    errorMessage = null
                                }
                            )
                        }
                    }
                } else {
                    // Purchase Confirmation View
                    val bond = selectedBond!!
                    val amountLong = investAmountText.toLongOrNull() ?: 0L
                    val monthlyCoupon = (amountLong * (bond.annualCouponRate / 12.0)).toLong()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardDark, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                bond.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                            RatingBadge(bond.rating)
                        }
                        Text(bond.issuer, color = textGray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Imbal Hasil (Kupon):", color = textGray, fontSize = 12.sp)
                            Text("${String.format(Locale.US, "%.1f", bond.annualCouponPercent)}% p.a.", color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tenor Jatuh Tempo:", color = textGray, fontSize = 12.sp)
                            Text("${bond.tenureMonths} Bulan", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Minimal Penempatan:", color = textGray, fontSize = 12.sp)
                            Text("$${String.format(Locale.US, "%,d", bond.minInvestment)}", color = gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = investAmountText,
                            onValueChange = { 
                                investAmountText = it.filter { ch -> ch.isDigit() }
                                errorMessage = null
                            },
                            label = { Text("Nominal Pembelian USD", color = textGray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = gold,
                                unfocusedBorderColor = textGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Kas: $${String.format(Locale.US, "%,d", holdingCash)}", color = textGray, fontSize = 11.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = { investAmountText = bond.minInvestment.toString() },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                    modifier = Modifier.height(22.dp)
                                ) {
                                    Text("MIN", fontSize = 11.sp, color = textGray)
                                }
                                TextButton(
                                    onClick = { investAmountText = holdingCash.toString() },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                    modifier = Modifier.height(22.dp)
                                ) {
                                    Text("MAX", fontSize = 11.sp, color = gold, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (amountLong > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Estimasi Kupon Bulanan:", color = Color.White, fontSize = 12.sp)
                                    Text("+$${String.format(Locale.US, "%,d", monthlyCoupon)}/bln", color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(errorMessage!!, color = Color(0xFFFF5252), fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { selectedBond = null },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = textGray)
                            ) {
                                Text("Kembali")
                            }
                            Button(
                                onClick = {
                                    if (amountLong < bond.minInvestment) {
                                        errorMessage = "Minimal pembelian $${String.format(Locale.US, "%,d", bond.minInvestment)}"
                                    } else if (amountLong > holdingCash) {
                                        errorMessage = "Kas holding tidak mencukupi."
                                    } else {
                                        onPurchaseBond(bond, amountLong)
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier.weight(1.5f),
                                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                enabled = amountLong in bond.minInvestment..holdingCash
                            ) {
                                Text("Beli Obligasi", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BondMarketItem(
    bond: CorporateBond,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF262626)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (bond.type == BondType.SOVEREIGN) Icons.Default.Security else Icons.Default.Business,
                        contentDescription = null,
                        tint = if (bond.type == BondType.SOVEREIGN) Color(0xFF64B5F6) else Color(0xFFFFB74D),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        bond.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                RatingBadge(bond.rating)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(bond.issuer, color = Color.LightGray, fontSize = 11.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Kupon Tahunan", color = Color.Gray, fontSize = 10.sp)
                    Text("${String.format(Locale.US, "%.1f", bond.annualCouponPercent)}% p.a.", color = Color(0xFF00FF00), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tenor", color = Color.Gray, fontSize = 10.sp)
                    Text("${bond.tenureMonths} Bulan", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Min. Pembelian", color = Color.Gray, fontSize = 10.sp)
                    Text("$${String.format(Locale.US, "%,d", bond.minInvestment)}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun RatingBadge(rating: CreditRating) {
    val bgColor = when (rating) {
        CreditRating.AAA -> Color(0xFF2E7D32)
        CreditRating.AA -> Color(0xFF1565C0)
        CreditRating.BBB -> Color(0xFFE65100)
    }
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = rating.code,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
