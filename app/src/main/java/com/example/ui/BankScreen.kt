package com.example.ui

import com.example.viewmodel.GameViewModel

import com.example.data.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankScreen(navController: NavController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val metalsList by viewModel.preciousMetalsList.collectAsState()
    
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val slateDark = Color(0xFF252A34)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val neonGreen = Color(0xFF00FF00)

    val red = Color(0xFFFF5252)

    val currentMetalsValue = playerState.ownedMetals.entries.sumOf { (id, amount) ->
        val livePrice = metalsList.find { it.id == id }?.currentPrice ?: 0.0
        (amount * livePrice).toLong()
    }
    
    val totalTimeDeposits = playerState.timeDeposits.sumOf { it.principal }

    var showTransactionDialog by remember { mutableStateOf(false) }
    var selectedMetal by remember { mutableStateOf<com.example.data.PreciousMetal?>(null) }
    var transactionAmount by remember { mutableStateOf("") }
    var isBuying by remember { mutableStateOf(true) }
    
    var showTimeDepositDialog by remember { mutableStateOf(false) }
    var timeDepositAmount by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(3) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Brankas & Tabungan", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
            )
        },
        containerColor = bgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                        coil.compose.AsyncImage(
                            model = "https://plus.unsplash.com/premium_photo-1679870442588-2e26c81eab42?q=80&w=1332&auto=format&fit=crop",
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                        
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        colors = listOf(Color(0xFF121212).copy(alpha = 0.4f), Color(0xFF121212).copy(alpha = 0.95f))
                                    )
                                )
                        )
                        
                        Column(
                            modifier = Modifier.matchParentSize().padding(24.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text("Total Nilai Tabungan", color = Color.LightGray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$ ${String.format(Locale.US, "%,d", currentMetalsValue + totalTimeDeposits)}", color = Color(0xFFFFD700), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
            
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Time Deposit (Deposito Berjangka)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showTimeDepositDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+ Buka Deposito", fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
            
            items(playerState.timeDeposits) { deposit ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Column {
                                Text("Deposit ${deposit.durationMonths} Bulan", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Yield: ${(deposit.interestRate * 100).toInt()}% | Sisa: ${deposit.monthsRemaining} bln", color = textGray, fontSize = 12.sp)
                            }
                            Text("$${String.format(Locale.US, "%,d", deposit.principal)}", color = gold, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.withdrawTimeDeposit(deposit.id, isEarly = deposit.monthsRemaining > 1) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = if (deposit.monthsRemaining <= 1) neonGreen else red.copy(alpha = 0.2f), contentColor = if (deposit.monthsRemaining <= 1) Color.Black else red),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (deposit.monthsRemaining <= 1) "Cairkan (Tersedia)" else "Tarik Paksa (Penalti -5%)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Safe Haven (Komoditas)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            items(metalsList.distinctBy { it.id }) { metal ->
                val ownedAmount = playerState.ownedMetals[metal.id] ?: 0.0
                val averagePrice = playerState.ownedMetalsAveragePrices[metal.id] ?: 0.0
                val totalValue = (ownedAmount * metal.currentPrice).toLong()
                
                val profitLossPct = if (averagePrice > 0) ((metal.currentPrice - averagePrice) / averagePrice) * 100 else 0.0
                val plColor = if (profitLossPct >= 0) neonGreen else red
                val plSign = if (profitLossPct >= 0) "+" else ""

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = slateDark, modifier = Modifier.size(40.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = gold, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(metal.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Harga: $${String.format(Locale.US, "%,.2f", metal.currentPrice)} / ${metal.unit}", color = textGray, fontSize = 12.sp)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Dimiliki: ${String.format(Locale.US, "%.2f", ownedAmount)} ${metal.unit}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("$${String.format(Locale.US, "%,d", totalValue)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (ownedAmount > 0 && averagePrice > 0) {
                                    Text("Avg: $${String.format(Locale.US, "%.2f", averagePrice)} ($plSign${String.format(Locale.US, "%.1f", profitLossPct)}%)", color = plColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    selectedMetal = metal
                                    isBuying = true
                                    transactionAmount = ""
                                    showTransactionDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Beli", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    selectedMetal = metal
                                    isBuying = false
                                    transactionAmount = ""
                                    showTransactionDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                enabled = ownedAmount > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = slateDark, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Jual", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    if (showTransactionDialog && selectedMetal != null) {
        val metal = selectedMetal!!
        val parsedAmount = transactionAmount.toDoubleOrNull() ?: 0.0
        val totalWorth = (parsedAmount * metal.currentPrice).toLong()

        AlertDialog(
            onDismissRequest = { showTransactionDialog = false },
            containerColor = cardDark,
            title = {
                Text(if (isBuying) "Beli ${metal.name}" else "Jual ${metal.name}", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Total ${if (isBuying) "Biaya" else "Pendapatan"}: $${String.format(Locale.US, "%,d", totalWorth)}", color = neonGreen, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = transactionAmount,
                        onValueChange = { transactionAmount = it },
                        label = { Text("Jumlah (${metal.unit})", color = textGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = gold, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        if (isBuying) {
                            Text("Saldo: $${String.format(Locale.US, "%,d", playerState.privateBalance)}", color = textGray, fontSize = 12.sp)
                            val maxCanBuy = if (metal.currentPrice > 0) playerState.privateBalance / metal.currentPrice else 0.0
                            TextButton(onClick = { transactionAmount = String.format(Locale.US, "%.2f", maxCanBuy) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                                Text("MAX", fontSize = 12.sp, color = gold, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text("Dimiliki: ${playerState.ownedMetals[metal.id] ?: 0.0} ${metal.unit}", color = textGray, fontSize = 12.sp)
                            TextButton(onClick = { transactionAmount = String.format(Locale.US, "%.2f", playerState.ownedMetals[metal.id] ?: 0.0) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                                Text("MAX", fontSize = 12.sp, color = red, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isBuying) {
                            if (parsedAmount > 0 && playerState.privateBalance >= totalWorth) {
                                viewModel.buyMetal(metal.id, parsedAmount)
                                showTransactionDialog = false
                            }
                        } else {
                            val owned = playerState.ownedMetals[metal.id] ?: 0.0
                            if (parsedAmount in 0.0..owned) {
                                viewModel.sellMetal(metal.id, parsedAmount)
                                showTransactionDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                ) {
                    Text("Konfirmasi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransactionDialog = false }) {
                    Text("Batal", color = textGray)
                }
            }
        )
    }

    if (showTimeDepositDialog) {
        val parsedAmount = timeDepositAmount.toLongOrNull() ?: 0L
        AlertDialog(
            onDismissRequest = { showTimeDepositDialog = false },
            containerColor = cardDark,
            title = { Text("Buka Time Deposit", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = timeDepositAmount,
                        onValueChange = { timeDepositAmount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah USD", color = textGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = gold, unfocusedBorderColor = textGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Saldo: $${String.format(Locale.US, "%,d", playerState.privateBalance)}", color = textGray, fontSize = 12.sp)
                        TextButton(onClick = { timeDepositAmount = playerState.privateBalance.toString() }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(24.dp)) {
                            Text("MAX", fontSize = 12.sp, color = gold, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Pilih Durasi:", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val durations = listOf(3 to "3 Bulan (Yield: 5%)", 6 to "6 Bulan (Yield: 12%)", 12 to "12 Bulan (Yield: 30%)")
                    durations.forEach { (months, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDuration = months }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedDuration == months,
                                onClick = { selectedDuration = months },
                                colors = RadioButtonDefaults.colors(selectedColor = gold, unselectedColor = textGray)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, color = if (selectedDuration == months) gold else Color.White, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (parsedAmount > 0 && playerState.privateBalance >= parsedAmount) {
                            viewModel.openTimeDeposit(parsedAmount, selectedDuration)
                            showTimeDepositDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                    enabled = parsedAmount > 0 && playerState.privateBalance >= parsedAmount
                ) {
                    Text("Buka Deposit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDepositDialog = false }) {
                    Text("Batal", color = textGray)
                }
            }
        )
    }
}
