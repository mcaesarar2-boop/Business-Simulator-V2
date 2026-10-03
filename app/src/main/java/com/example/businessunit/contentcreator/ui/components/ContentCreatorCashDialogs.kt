package com.example.businessunit.contentcreator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessunit.contentcreator.model.ActiveCreatorContract
import com.example.businessunit.contentcreator.model.ProductionHouseOffer
import com.example.ui.formatCurrencyRingkas

@Composable
fun ContentCreatorCashTransferDialog(
    isInject: Boolean,
    maxAvailable: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val parsedAmount = amountInput.toLongOrNull() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ContentCreatorTheme.BgDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (isInject) "Suntik Modal ke Channel" else "Tarik Profit Channel ke Kas Utama",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ContentCreatorTheme.TextWhite
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isInject) "Tersedia di Kas Utama: ${formatCurrencyRingkas(maxAvailable, false)}" else "Tersedia di Kas Channel: ${formatCurrencyRingkas(maxAvailable, false)}",
                    fontSize = 12.sp,
                    color = ContentCreatorTheme.TextGray
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = {
                        amountInput = it.filter { ch -> ch.isDigit() }
                        errorMsg = null
                    },
                    label = { Text("Jumlah Nominal ($)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ContentCreatorTheme.TextWhite,
                        unfocusedTextColor = ContentCreatorTheme.TextWhite,
                        focusedBorderColor = if (isInject) ContentCreatorTheme.NeonGreen else ContentCreatorTheme.AccentCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick presets
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val fractions = listOf(0.25, 0.50, 0.75, 1.0)
                    fractions.forEach { frac ->
                        val amt = (maxAvailable * frac).toLong()
                        Surface(
                            onClick = { amountInput = amt.toString(); errorMsg = null },
                            shape = RoundedCornerShape(6.dp),
                            color = ContentCreatorTheme.CardDark,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${(frac * 100).toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ContentCreatorTheme.TextWhite,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                errorMsg?.let {
                    Text(it, color = ContentCreatorTheme.ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedAmount <= 0) {
                        errorMsg = "Jumlah harus lebih dari $0!"
                        return@Button
                    }
                    if (parsedAmount > maxAvailable) {
                        errorMsg = "Saldo tidak mencukupi!"
                        return@Button
                    }
                    onConfirm(parsedAmount)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isInject) ContentCreatorTheme.NeonGreen else ContentCreatorTheme.AccentCyan
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isInject) "Suntik Modal" else "Tarik Profit",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = ContentCreatorTheme.TextGray)
            }
        }
    )
}

@Composable
fun TerminateContractConfirmDialog(
    contract: ActiveCreatorContract,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val penaltyFee = contract.monthlyPayout / 2

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ContentCreatorTheme.BgDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Konfirmasi Pemutusan Kontrak", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ContentCreatorTheme.ErrorRed)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Apakah Anda yakin ingin memutuskan kontrak dengan ${contract.brandName} secara sepihak?",
                    color = ContentCreatorTheme.TextWhite,
                    fontSize = 12.sp
                )
                Text(
                    "Biaya Penalti Pemutusan: ${formatCurrencyRingkas(penaltyFee, false)}",
                    color = ContentCreatorTheme.Gold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ContentCreatorTheme.ErrorRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Putus Kontrak Sekarang", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = ContentCreatorTheme.TextGray)
            }
        }
    )
}

@Composable
fun ProductionHouseOfferDialog(
    offer: ProductionHouseOffer,
    onDismiss: () -> Unit,
    onAcceptLumpSum: () -> Unit,
    onAcceptRoyalty: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ContentCreatorTheme.BgDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text("Tawaran Mitra: ${offer.phName}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ContentCreatorTheme.AccentCyan)
                Text("Karya: ${offer.contentTitle}", fontSize = 12.sp, color = ContentCreatorTheme.TextGray)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "\"${offer.pitchMessage}\"",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = ContentCreatorTheme.TextWhite
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Option 1: Jual Putus
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
                    border = BorderStroke(1.dp, ContentCreatorTheme.Gold.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("OPSI 1: JUAL PUTUS (ACQUISITION)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.Gold)
                        Text(
                            text = "Dapatkan dana tunai instan +${formatCurrencyRingkas(offer.lumpSumOffer, false)} sekaligus.",
                            fontSize = 11.sp,
                            color = ContentCreatorTheme.TextWhite
                        )
                    }
                }

                // Option 2: Lisensi Royalti
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
                    border = BorderStroke(1.dp, ContentCreatorTheme.NeonGreen.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("OPSI 2: KONTRAK LISENSI ROYALTI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.NeonGreen)
                        Text(
                            text = "Royalti +${formatCurrencyRingkas(offer.monthlyRoyalty, false)} / bln selama ${offer.contractDurationMonths} Bulan (Upfront +${formatCurrencyRingkas(offer.royaltyUpfront, false)})",
                            fontSize = 11.sp,
                            color = ContentCreatorTheme.TextWhite
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onAcceptRoyalty,
                    colors = ButtonDefaults.buttonColors(containerColor = ContentCreatorTheme.NeonGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text("Lisensi Royalti", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAcceptLumpSum,
                    colors = ButtonDefaults.buttonColors(containerColor = ContentCreatorTheme.Gold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text("Jual Putus", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tolak", color = ContentCreatorTheme.ErrorRed)
            }
        }
    )
}

@Composable
fun DeleteChannelConfirmDialog(
    channelName: String,
    refundCash: Long,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ContentCreatorTheme.BgDark,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "⚠️ Tutup & Likuidasi Channel",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ContentCreatorTheme.ErrorRed
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Apakah Anda yakin ingin menutup unit usaha '$channelName'?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ContentCreatorTheme.TextWhite
                )
                Text(
                    text = "Seluruh data subscriber, kontrak brand deals aktif, dan portofolio karya channel akan dihapus secara permanen.",
                    fontSize = 12.sp,
                    color = ContentCreatorTheme.TextGray,
                    lineHeight = 16.sp
                )
                if (refundCash > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.NeonGreen.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, ContentCreatorTheme.NeonGreen.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "💰 Sisa kas usaha unit sebesar ${formatCurrencyRingkas(refundCash, false)} akan dikembalikan secara otomatis ke Kas Utama Holding.",
                            fontSize = 11.sp,
                            color = ContentCreatorTheme.NeonGreen,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ContentCreatorTheme.ErrorRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Ya, Tutup Channel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = ContentCreatorTheme.TextGray)
            }
        }
    )
}
