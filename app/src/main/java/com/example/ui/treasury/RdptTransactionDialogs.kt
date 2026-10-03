package com.example.ui.treasury

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.treasury.FixedIncomeFund
import com.example.data.treasury.FundHolding
import java.util.Locale

@Composable
fun RdptTopUpDialog(
    fund: FixedIncomeFund,
    currentNav: Double,
    holdingCash: Long,
    onDismiss: () -> Unit,
    onConfirmTopUp: (Long) -> Unit
) {
    val cardDark = Color(0xFF1E1E1E)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val neonGreen = Color(0xFF00FF00)

    var amountText by remember { mutableStateOf("") }
    val parsedAmount = amountText.toLongOrNull() ?: 0L
    val unitsAcquired = if (currentNav > 0) parsedAmount.toDouble() / currentNav else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardDark,
        title = {
            Column {
                Text("Beli Unit RDPT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(fund.name, color = gold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column {
                Text(
                    "Penempatan dana kas holding ke reksadana pendapatan tetap korporasi dengan likuiditas harian dan imbal hasil bertumbuh.",
                    color = textGray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

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
                        Text("NAV / Unit Saat Ini:", color = textGray, fontSize = 12.sp)
                        Text("$${String.format(Locale.US, "%,.4f", currentNav)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal Investasi USD", color = textGray) },
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
                    Text("Kas Holding: $${String.format(Locale.US, "%,d", holdingCash)}", color = textGray, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { amountText = "50000" },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text("+50K", fontSize = 11.sp, color = textGray)
                        }
                        TextButton(
                            onClick = { amountText = holdingCash.toString() },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.height(22.dp)
                        ) {
                            Text("MAX", fontSize = 11.sp, color = gold, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (parsedAmount > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
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
                            Text("Estimasi Unit Didapat:", color = textGray, fontSize = 12.sp)
                            Text("${String.format(Locale.US, "%,.2f", unitsAcquired)} Unit", color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedAmount in 1..holdingCash) {
                        onConfirmTopUp(parsedAmount)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                enabled = parsedAmount in 1..holdingCash
            ) {
                Text("Konfirmasi Beli", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = textGray)
            }
        }
    )
}

@Composable
fun RdptRedeemDialog(
    fund: FixedIncomeFund,
    currentNav: Double,
    holding: FundHolding,
    onDismiss: () -> Unit,
    onConfirmRedeem: (Double) -> Unit
) {
    val cardDark = Color(0xFF1E1E1E)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val neonGreen = Color(0xFF00FF00)

    var redeemUnitsText by remember { mutableStateOf("") }
    val parsedUnits = redeemUnitsText.toDoubleOrNull() ?: 0.0
    val estCashReceived = (parsedUnits * currentNav).toLong()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cardDark,
        title = {
            Column {
                Text("Pencairan (Redeem) Unit RDPT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Bebas Biaya Penalti Likuidasi", color = neonGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        text = {
            Column {
                Surface(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Unit Dimiliki:", color = textGray, fontSize = 12.sp)
                            Text("${String.format(Locale.US, "%,.2f", holding.totalUnits)} Unit", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("NAV / Unit Terkini:", color = textGray, fontSize = 12.sp)
                            Text("$${String.format(Locale.US, "%,.4f", currentNav)}", color = gold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = redeemUnitsText,
                    onValueChange = { redeemUnitsText = it },
                    label = { Text("Jumlah Unit yang Dicairkan", color = textGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = gold,
                        unfocusedBorderColor = textGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick percentage selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(25, 50, 75, 100).forEach { pct ->
                        val targetUnits = holding.totalUnits * (pct / 100.0)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { redeemUnitsText = String.format(Locale.US, "%.2f", targetUnits) }
                        ) {
                            Text(
                                text = "$pct%",
                                color = if (pct == 100) gold else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                if (parsedUnits > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
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
                            Text("Estimasi Dana Masuk ke Kas:", color = textGray, fontSize = 12.sp)
                            Text("$${String.format(Locale.US, "%,d", estCashReceived)}", color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedUnits > 0 && parsedUnits <= holding.totalUnits) {
                        onConfirmRedeem(parsedUnits)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                enabled = parsedUnits > 0 && parsedUnits <= holding.totalUnits
            ) {
                Text("Cairkan Dana", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = textGray)
            }
        }
    )
}
