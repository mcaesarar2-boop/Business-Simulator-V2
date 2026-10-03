package com.example.ui

import com.example.viewmodel.GameViewModel

import com.example.data.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CryptoScreen(viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val cash = playerState.cash
    val ownedCrypto = playerState.ownedCrypto
    val cryptoList by viewModel.cryptoList.collectAsState()
    
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val neonGreen = Color(0xFF00FF00)
    val red = Color(0xFFFF3B30)
    
    var showBuyDialog by remember { mutableStateOf(false) }
    var selectedCrypto by remember { mutableStateOf<com.example.data.CryptoItem?>(null) }
    var buyAmountStr by remember { mutableStateOf("") }
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    
    val totalCryptoValue = ownedCrypto.sumOf { owned ->
        val livePrice = cryptoList.find { it.symbol == owned.symbol }?.currentPrice ?: owned.averagePrice
        owned.amount * livePrice
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
    ) {
        Column {
            Text(currencyFormat.format(totalCryptoValue), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Total Crypto Portfolio", color = textGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        Text("Portofolio Saya", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (ownedCrypto.isEmpty()) {
            Text("Anda belum memiliki aset Crypto.", color = textGray)
        }
        
        ownedCrypto.forEach { owned ->
            val crypto = cryptoList.find { it.symbol == owned.symbol }
            val livePrice = crypto?.currentPrice ?: owned.averagePrice
            val totalValue = owned.amount * livePrice
            val changePercent = ((livePrice - owned.averagePrice) / owned.averagePrice) * 100
            val color = if (changePercent >= 0) neonGreen else red
            val sign = if (changePercent >= 0) "+" else ""

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = cardDark)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = crypto?.logoUrl ?: "",
                        contentDescription = "Logo",
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(owned.symbol, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(String.format(Locale.US, "%.4f koin", owned.amount), color = textGray, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(currencyFormat.format(totalValue), color = Color.White, fontWeight = FontWeight.Bold)
                        Text(String.format(Locale.US, "%s%.2f%%", sign, changePercent), color = color, fontSize = 12.sp)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Pasar Crypto", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        cryptoList.forEach { crypto ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = crypto.logoUrl,
                        contentDescription = "Logo",
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(crypto.name, color = Color.White, fontWeight = FontWeight.Bold)
                        Text(crypto.symbol, color = textGray, fontSize = 12.sp)
                    }
                    val color = if (crypto.changePercentage >= 0) neonGreen else red
                    val sign = if (crypto.changePercentage >= 0) "+" else ""
                    Column(horizontalAlignment = Alignment.End) {
                        Text(currencyFormat.format(crypto.currentPrice), color = Color.White, fontWeight = FontWeight.Medium)
                        Text(String.format(Locale.US, "%s%.2f%%", sign, crypto.changePercentage), color = color, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { selectedCrypto = crypto; showBuyDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color(0xFFFFD700)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Beli", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
    
    if (showBuyDialog && selectedCrypto != null) {
        val crypto = selectedCrypto!!
        val currentPrice = crypto.currentPrice
        AlertDialog(
            onDismissRequest = { showBuyDialog = false; buyAmountStr = "" },
            containerColor = cardDark,
            title = { Text("Beli ${crypto.name}", color = Color.White) },
            text = {
                Column {
                    Text("Harga Saat Ini: ${currencyFormat.format(currentPrice)}", color = textGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = buyAmountStr,
                        onValueChange = { buyAmountStr = it },
                        label = { Text("Jumlah Koin") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = gold,
                            unfocusedBorderColor = textGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val amount = buyAmountStr.toDoubleOrNull() ?: 0.0
                    val totalCost = amount * currentPrice
                    Text("Total Biaya: ${currencyFormat.format(totalCost)}", color = if (totalCost > cash) red else neonGreen)
                    Text("Saldo Anda: ${currencyFormat.format(cash)}", color = Color.White)
                }
            },
            confirmButton = {
                val amount = buyAmountStr.toDoubleOrNull() ?: 0.0
                val totalCost = amount * currentPrice
                val canBuy = amount > 0 && totalCost <= cash
                Button(
                    onClick = {
                        viewModel.buyCrypto(crypto.symbol, currentPrice, amount)
                        showBuyDialog = false
                        buyAmountStr = ""
                    },
                    enabled = canBuy,
                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen, contentColor = Color.Black)
                ) {
                    Text("Konfirmasi Beli")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBuyDialog = false; buyAmountStr = "" }) {
                    Text("Batal", color = textGray)
                }
            }
        )
    }
}
