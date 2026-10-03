package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.RoomClassConfig
import com.example.data.RoomClassStrategy
import com.example.viewmodel.GameViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomManagementScreen(
    navController: NavController,
    viewModel: GameViewModel,
    instanceId: String,
    hotelId: String
) {
    val playerState by viewModel.playerState.collectAsState()
    val allBusinesses = playerState.ownedBusinesses + playerState.holdingCompanies.flatMap { it.subsidiaries }
    val business = allBusinesses.find { it.instanceId == instanceId }
    val hotel = business?.hospitalityProperties?.find { it.id == hotelId }

    if (hotel == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Hotel tidak ditemukan.", color = Color.White)
        }
        return
    }

    val nFormat = NumberFormat.getNumberInstance(Locale.US)
    val maxRooms = hotel.tier.maxRooms
    
    val hotelPrestige = hotel.tier.baseBuildCost / 1_000_000 + hotel.builtFacilities.size * 10
    
    val safeRoomConfigs = hotel.roomConfigs ?: mutableMapOf()
    var totalAllocation = safeRoomConfigs.values.filter { it.isEnabled }.sumOf { it.allocationPercent }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Multi-Class", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Kapasitas Total: $maxRooms Kamar", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val allocColor = if (totalAllocation > 100.0) Color.Red else if (totalAllocation == 100.0) Color(0xFF00E676) else Color.Yellow
                        Text("Alokasi Kamar: ${totalAllocation.toInt()}% / 100%", color = allocColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        
                        if (totalAllocation > 100.0) {
                            Text("⚠️ Alokasi melebihi 100%! Ubah konfigurasi di bawah.", color = Color.Red, fontSize = 12.sp)
                        } else if (totalAllocation < 100.0) {
                            Text("⚠️ Ada ${100 - totalAllocation.toInt()}% kamar kosong tidak terpakai.", color = Color.Yellow, fontSize = 12.sp)
                        }
                    }
                }
            }

            items(RoomClassStrategy.values()) { strategy ->
                val config = safeRoomConfigs[strategy.name] ?: RoomClassConfig()
                val isEnabled = config.isEnabled
                val baseRate = (hotel.tier.baseRoomRate * strategy.priceMultiplier).toLong()
                val currentPrice = if (config.customPrice > 0) config.customPrice else baseRate

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isEnabled) Color(0xFF1E1E1E) else Color(0xFF262626))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(strategy.title, color = if (isEnabled) Color(0xFFFFD700) else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                if (isEnabled) {
                                    val allocatedRoomsThisClass = (maxRooms * (config.allocationPercent / 100.0)).toInt()
                                    Text("$allocatedRoomsThisClass Kamar Dialokasikan", color = Color.White, fontSize = 12.sp)
                                }
                            }
                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    val safeConfig = config.copy(
                                        isEnabled = checked, 
                                        customPrice = if (currentPrice == 0L) baseRate else currentPrice
                                    )
                                    viewModel.updateRoomClassConfig(instanceId, hotelId, strategy.name, safeConfig)
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD700), checkedTrackColor = Color(0xFF554400))
                            )
                        }

                        if (isEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Allocation Slider
                            Text("Alokasi Kamar: ${config.allocationPercent.toInt()}%", color = Color.White)
                            Slider(
                                value = config.allocationPercent.toFloat(),
                                onValueChange = { newVal ->
                                    val safeConfig = config.copy(allocationPercent = newVal.toDouble())
                                    viewModel.updateRoomClassConfig(instanceId, hotelId, strategy.name, safeConfig)
                                },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = Color.Cyan, activeTrackColor = Color.Cyan)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Price Slider
                            Text("Harga Per Malam: $${nFormat.format(currentPrice)}", color = Color.White)
                            Text("Harga Dasar: $${nFormat.format(baseRate)}", color = Color.Gray, fontSize = 12.sp)
                            Slider(
                                value = currentPrice.toFloat(),
                                onValueChange = { newVal ->
                                    val safeConfig = config.copy(customPrice = newVal.toLong())
                                    viewModel.updateRoomClassConfig(instanceId, hotelId, strategy.name, safeConfig)
                                },
                                valueRange = (baseRate * 0.3f)..(baseRate * 5f),
                                colors = SliderDefaults.colors(thumbColor = Color(0xFFFFD700), activeTrackColor = Color(0xFFFFD700))
                            )

                            // Warnings
                            val priceRatio = currentPrice.toDouble() / baseRate
                            if (priceRatio < 0.6) {
                                Text("Obral murah! Tamu membludak namun merusak profit margin.", color = Color.Yellow, fontSize = 12.sp)
                            } else if (priceRatio > 2.0) {
                                Text("Harga premium! Tamu akan sepi.", color = Color.Red, fontSize = 12.sp)
                            }
                            
                            if (strategy.requiredPrestige > hotelPrestige) {
                                Text("⚠️ Prestige tidak cukup! Butuh ${strategy.requiredPrestige}, hotel cuma punya $hotelPrestige.", color = Color.Red, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            // Stats Info
                            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Occupancy (Bln Lalu)", color = Color.Gray, fontSize = 10.sp)
                                    val occPct = (config.lastMonthOccupancy * 100).toInt()
                                    Text("$occPct%", color = if (occPct < 40) Color(0xFFFF5252) else Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Pendapatan Kotor (Bln Lalu)", color = Color.Gray, fontSize = 10.sp)
                                    Text("$${nFormat.format(config.lastMonthRevenue)}", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
