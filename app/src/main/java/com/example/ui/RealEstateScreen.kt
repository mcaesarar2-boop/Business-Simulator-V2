package com.example.ui

import com.example.viewmodel.GameViewModel

import com.example.data.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun RealEstateScreen(viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val cash = playerState.privateBalance
    val ownedProperties = playerState.ownedProperties
    val market by viewModel.realEstateMarket.collectAsState()
    
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val neonGreen = Color(0xFF00FF00)
    
    var showBuyDialog by remember { mutableStateOf(false) }
    var selectedProperty by remember { mutableStateOf<com.example.data.PropertyItem?>(null) }
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    
    val totalRentalIncome = ownedProperties.sumOf { owned ->
        val propItem = market.find { it.id == owned.propertyId }
        if (propItem != null) {
            val isSultan = owned.condition == 100 && owned.currentEstimatedValue > propItem.basePrice
            val multiplier = if (isSultan) 1.5 else (owned.condition / 100.0)
            (propItem.baseRentalIncome * multiplier).toLong()
        } else 0L
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
    ) {
        Column {
            Text(currencyFormat.format(totalRentalIncome), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Pendapatan sewa per bulan", color = textGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        Text("Properti Saya", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        if (ownedProperties.isEmpty()) {
            Text("Anda belum memiliki properti.", color = textGray)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        ownedProperties.forEach { owned ->
            val prop = market.find { it.id == owned.propertyId }
            if (prop != null) {
                val isSultan = owned.condition == 100 && owned.currentEstimatedValue > prop.basePrice
                val currentRent = (prop.baseRentalIncome * (if (isSultan) 1.5 else owned.condition / 100.0)).toLong()
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = Color.DarkGray, modifier = Modifier.size(50.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.HomeWork, contentDescription = null, tint = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prop.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(prop.location, color = textGray, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${currencyFormat.format(currentRent)}/bln", color = neonGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        if (owned.condition < 100) {
                            Spacer(Modifier.height(12.dp))
                            val renoCost = viewModel.getRenovationCost(prop, owned.condition)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("🛠️ Kondisi: ${owned.condition}%", color = Color.Yellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(4.dp))
                                    LinearProgressIndicator(progress = { owned.condition / 100f }, modifier = Modifier.fillMaxWidth().padding(end = 16.dp), color = neonGreen)
                                }
                                Button(
                                    onClick = { viewModel.renovateProperty(owned.propertyId) },
                                    enabled = cash >= renoCost,
                                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen, contentColor = Color.Black),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("Renovasi\n${currencyFormat.format(renoCost)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                }
                            }
                        } else {
                            if (!owned.isFlipped) {
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.sellPropertySultan(owned.propertyId) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                                ) {
                                    Text("Jual Sultan (Dapatkan ${currencyFormat.format(owned.currentEstimatedValue)})", fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("Pasar Properti", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        market.forEach { prop ->
            val isOwned = ownedProperties.any { it.propertyId == prop.id }
            if (!isOwned) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = Color.DarkGray, modifier = Modifier.size(50.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.RealEstateAgent, contentDescription = null, tint = gold)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(prop.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    if (prop.condition < 100) {
                                        Spacer(Modifier.width(8.dp))
                                        Surface(color = Color(0xFF550000), shape = RoundedCornerShape(4.dp)) {
                                            Text("Diskon Bapuk", color = Color.Red, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(prop.location, color = textGray, fontSize = 12.sp)
                                Text("🛠️ Kondisi: ${prop.condition}%", color = if (prop.condition < 100) Color.Yellow else textGray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Harga", color = textGray, fontSize = 12.sp)
                                if (prop.condition < 100) {
                                    Text(currencyFormat.format(prop.basePrice), color = textGray, fontSize = 11.sp, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                }
                                Text(currencyFormat.format(prop.currentPrice), color = Color.White, fontWeight = FontWeight.Medium)
                            }
                            Column {
                                Text("Sewa/bln", color = textGray, fontSize = 12.sp)
                                Text(currencyFormat.format(prop.baseRentalIncome), color = neonGreen, fontWeight = FontWeight.Medium)
                            }
                            Button(
                                onClick = { selectedProperty = prop; showBuyDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color(0xFFFFD700)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Beli", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showBuyDialog && selectedProperty != null) {
        val prop = selectedProperty!!
        AlertDialog(
            onDismissRequest = { showBuyDialog = false },
            containerColor = cardDark,
            title = { Text("Beli Properti", color = Color.White) },
            text = {
                Column {
                    Text("${prop.name} di ${prop.location}", color = textGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Harga: ${currencyFormat.format(prop.currentPrice)}", color = Color.White)
                    Text("Saldo Anda: ${currencyFormat.format(cash)}", color = if (cash >= prop.currentPrice) neonGreen else Color.Red)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.buyProperty(prop.id)
                        showBuyDialog = false
                    },
                    enabled = cash >= prop.currentPrice,
                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen, contentColor = bgDark)
                ) {
                    Text("Konfirmasi Beli")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBuyDialog = false }) {
                    Text("Batal", color = textGray)
                }
            }
        )
    }
}
