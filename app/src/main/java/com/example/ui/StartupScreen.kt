package com.example.ui

import com.example.viewmodel.GameViewModel

import com.example.data.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun StartupScreen(viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val cash = playerState.cash
    val activeInvestments = playerState.activeStartupInvestments
    val currentStartups by viewModel.currentYearStartups.collectAsState()
    
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val neonGreen = Color(0xFF00FF00)
    val red = Color(0xFFFF3B30)
    
    var showInvestDialog by remember { mutableStateOf(false) }
    var selectedStartup by remember { mutableStateOf<com.example.data.StartupInvestment?>(null) }
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
    ) {
        // Active Investments
        if (activeInvestments.isNotEmpty()) {
            Text("Investasi Aktif Saya", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            activeInvestments.forEach { inv ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(inv.startupName, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${inv.monthsRemaining} bln tersisa", color = gold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Modal: ${currencyFormat.format(inv.investedAmount)}", color = textGray, fontSize = 14.sp)
                            Text("Potensi: ${currencyFormat.format(inv.potentialReturn)}", color = neonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Sukses Rate: ${String.format(Locale.US, "%.1f%%", inv.successProbability * 100)}", color = textGray, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        Text("Peluang Angel Invest Tahun Ini", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (currentStartups.isEmpty()) {
            Text("Pergantian tahun ajaran baru, sedang memuat pitch startup...", color = textGray)
        }
        
        currentStartups.forEach { startup ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(startup.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(startup.description, color = textGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Kebutuhan Dana", color = textGray, fontSize = 12.sp)
                            Text(currencyFormat.format(startup.requiredInvestment), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Potensi (x ${String.format(Locale.US, "%.1f", startup.potentialReturnMultiplier)})", color = textGray, fontSize = 12.sp)
                            Text(currencyFormat.format(startup.requiredInvestment * startup.potentialReturnMultiplier), color = neonGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Win Rate: ${String.format(Locale.US, "%.1f%%", startup.successProbability * 100)}  |  Durasi: ${startup.durationMonths} bln", color = gold, fontSize = 12.sp)
                        Button(
                            onClick = { selectedStartup = startup; showInvestDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f), contentColor = Color(0xFFFFD700)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Investasi", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
    
    if (showInvestDialog && selectedStartup != null) {
        val startup = selectedStartup!!
        val reqCash = startup.requiredInvestment
        AlertDialog(
            onDismissRequest = { showInvestDialog = false },
            containerColor = cardDark,
            title = { Text("Pitch Deck: ${startup.name}", color = Color.White) },
            text = {
                Column {
                    Text(startup.description, color = textGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Durasi Penguncian Dana: ${startup.durationMonths} Bulan", color = Color.White)
                    Text("Peluang Berhasil: ${String.format(Locale.US, "%.1f%%", startup.successProbability * 100)}", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Laporan Proposal", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Anda diminta menyuntik modal sebesar ${currencyFormat.format(reqCash)}", color = textGray)
                    Text("Potensi pengembalian: ${currencyFormat.format(reqCash * startup.potentialReturnMultiplier)}", color = neonGreen, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Saldo Anda: ${currencyFormat.format(cash)}", color = if (cash >= reqCash) gold else red)
                }
            },
            confirmButton = {
                val canBuy = cash >= reqCash
                Button(
                    onClick = {
                        viewModel.investInStartup(startup.id)
                        showInvestDialog = false
                    },
                    enabled = canBuy,
                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen, contentColor = Color.Black)
                ) {
                    Text("Kontrak Deal")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInvestDialog = false }) {
                    Text("Batal", color = textGray)
                }
            }
        )
    }
}
