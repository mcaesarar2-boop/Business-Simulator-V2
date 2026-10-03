package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.data.*
import com.example.viewmodel.GameViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalityDashboardScreen(navController: NavHostController, viewModel: GameViewModel, instanceId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val ownedBusiness = playerState.ownedBusinesses.find { it.instanceId == instanceId } ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
    
    if (ownedBusiness == null) {
        navController.popBackStack()
        return
    }

    val catalogItem = getCatalogItem(ownedBusiness.catalogId, playerState)
    val properties = ownedBusiness.hospitalityProperties

    val totalRooms = properties.sumOf { it.tier.maxRooms }
    val avgOccupancy = if (properties.isNotEmpty()) properties.map { it.lastMonthOccupancyRate }.average() else 0.0
    val totalRevenue = properties.sumOf { it.lastMonthRevenue - it.lastMonthExpense }
    
    val nFormat = NumberFormat.getNumberInstance(Locale.US)
    val context = androidx.compose.ui.platform.LocalContext.current

    var showTransferDialog by remember { mutableStateOf(false) }
    var isDeposit by remember { mutableStateOf(true) }
    var transferAmount by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(ownedBusiness.customName ?: catalogItem?.name ?: "Hospitality Holding", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color(0xFFFFD700),
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("build_hotel_property/$instanceId") },
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Bangun Properti")
            }
        },
        containerColor = Color(0xFF1E1E1E)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))), shape = RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Global Analytics", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Divisional Cash", color = Color.White, fontSize = 12.sp)
                                Text("$${nFormat.format(ownedBusiness.companyCash)}", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    IconButton(
                                        onClick = { 
                                            isDeposit = false
                                            showTransferDialog = true
                                        },
                                        modifier = Modifier.size(36.dp).background(Color(0xFFFF5252).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Tarik Kas", tint = Color(0xFFFF5252))
                                    }
                                    IconButton(
                                        onClick = { 
                                            isDeposit = true
                                            showTransferDialog = true
                                        },
                                        modifier = Modifier.size(36.dp).background(Color(0xFFFFD700).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Suntik Kas", tint = Color(0xFFFFD700))
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Monthly Profit", color = Color.White, fontSize = 12.sp)
                                Text("$${nFormat.format(totalRevenue)}", color = if (totalRevenue >= 0) Color(0xFF00E676) else Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Rooms", color = Color.LightGray, fontSize = 12.sp)
                                Text("${nFormat.format(totalRooms)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Avg Occupancy", color = Color.LightGray, fontSize = 12.sp)
                                val occPercent = (avgOccupancy * 100).toInt()
                                Text("$occPercent%", color = if (occPercent > 70) Color(0xFFFFD700) else Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Daftar Properti", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(if (isGridView) Icons.Default.ViewList else Icons.Default.GridView, contentDescription = "Toggle View", tint = Color.Gray)
                    }
                }
            }

            if (properties.isEmpty()) {
                item {
                    Text("Belum ada properti. Silakan bangun properti baru.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                }
            } else {
                if (isGridView) {
                    val chunked = properties.chunked(2)
                    items(chunked) { rowProps ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            for (hotel in rowProps) {
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { navController.navigate("hotel_detail/${instanceId}/${hotel.id}") },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column {
                                        if (hotel.imageUrl != null) {
                                            AsyncImage(
                                                model = hotel.imageUrl,
                                                contentDescription = hotel.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxWidth().height(100.dp)
                                            )
                                        } else {
                                            Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color(0xFF333333)), contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.LocationCity, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(40.dp))
                                            }
                                        }
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(hotel.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                                            Text(hotel.location, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            if (hotel.isConstructing) {
                                                Text("Dibangun: ${hotel.remainingBuildMonths} bln", color = Color(0xFFFFA000), fontSize = 10.sp)
                                            } else {
                                                val rateStr = (hotel.lastMonthOccupancyRate * 100).toInt()
                                                val profit = hotel.lastMonthRevenue - hotel.lastMonthExpense
                                                Text("Occ: $rateStr% • $${nFormat.format(profit)}", color = Color(0xFF00E676), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            if (rowProps.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(properties) { hotel ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("hotel_detail/${instanceId}/${hotel.id}") },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (hotel.imageUrl != null) {
                                    AsyncImage(
                                        model = hotel.imageUrl,
                                        contentDescription = hotel.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(56.dp).background(Color(0xFF333333), shape = RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Box(modifier = Modifier.size(56.dp).background(Color(0xFF333333), shape = RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.LocationCity, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(hotel.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("${hotel.tier.title} • ${hotel.location}", color = Color.Gray, fontSize = 12.sp)
                                    if (hotel.isConstructing) {
                                        Text("Dibangun: ${hotel.remainingBuildMonths} bulan lagi", color = Color(0xFFFFA000), fontSize = 12.sp)
                                    } else {
                                        val rateStr = (hotel.lastMonthOccupancyRate * 100).toInt()
                                        val profit = hotel.lastMonthRevenue - hotel.lastMonthExpense
                                        Text("Occupancy: $rateStr% • Laba: $${nFormat.format(profit)}", color = Color(0xFF00E676), fontSize = 12.sp)
                                    }
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showTransferDialog) {
        val title = if (isDeposit) "Suntik Dana Murni" else "Tarik Dana Divisi"
        val desc = if (isDeposit) "Masukkan jumlah uang untuk disuntikkan dari Dompet Utama (Mega Holding) ke divisi ini." else "Masukkan jumlah uang untuk ditarik ke Dompet Utama (Mega Holding)."
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text(title, color = Color.White) },
            text = {
                Column {
                    Text(desc, color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = transferAmount,
                        onValueChange = { transferAmount = it },
                        label = { Text("Jumlah Uang ($)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFFD700),
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amountLong = transferAmount.toLongOrNull()
                    if (amountLong != null && amountLong > 0) {
                        if (isDeposit) {
                            if (amountLong <= playerState.cash) {
                                viewModel.injectCapitalToBusiness(instanceId, amountLong)
                                showTransferDialog = false
                                transferAmount = ""
                            } else {
                                android.widget.Toast.makeText(context, "Kas Utama Tidak Cukup", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            if (amountLong <= ownedBusiness.companyCash) {
                                viewModel.withdrawCapitalFromBusiness(instanceId, amountLong)
                                showTransferDialog = false
                                transferAmount = ""
                            } else {
                                android.widget.Toast.makeText(context, "Kas Divisi Tidak Cukup", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Nominal tidak valid", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(if (isDeposit) "Suntik Dana" else "Tarik Dana", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false; transferAmount = "" }) {
                    Text("Batal", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}
