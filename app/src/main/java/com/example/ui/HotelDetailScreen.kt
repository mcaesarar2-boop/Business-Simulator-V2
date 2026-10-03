package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.data.*
import com.example.viewmodel.GameViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(navController: NavHostController, viewModel: GameViewModel, instanceId: String, hotelId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val ownedBusiness = playerState.ownedBusinesses.find { it.instanceId == instanceId } ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
    
    val hotel = ownedBusiness?.hospitalityProperties?.find { it.id == hotelId }

    if (ownedBusiness == null || hotel == null) {
        navController.popBackStack()
        return
    }

    val nFormat = NumberFormat.getNumberInstance(Locale.US)
    
    // Dynamic Pricing Slider State
    var customPriceRaw by remember { mutableStateOf(hotel.customRoomRate.toFloat()) }
    // When dragged, we update local state, when released we update viewmodel
    
    val minPrice = hotel.tier.baseRoomRate * 0.5f
    val maxPrice = hotel.tier.baseRoomRate * 3.0f

    var showImageDialog by remember { mutableStateOf(false) }
    var imageUrlInput by remember { mutableStateOf(hotel.imageUrl ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(hotel.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E1E1E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            
            item {
                // Header Banner
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        coil.compose.AsyncImage(
                            model = hotel.imageUrl ?: "https://images.unsplash.com/photo-1542314831-c6a4d14d8349?w=800&q=80",
                            contentDescription = "Hotel image",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF121212)))))
                        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(end = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(hotel.tier.title, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Box(modifier = Modifier.padding(start = 8.dp)) {
                                    IconButton(onClick = { showImageDialog = true }, modifier = Modifier.size(32.dp).background(Color(0x88000000), androidx.compose.foundation.shape.CircleShape)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Ubah Gambar", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("📍 ${hotel.location}", color = Color.LightGray, fontSize = 14.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Info & Performance Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (hotel.isConstructing) {
                            Text("Sedang Dibangun", color = Color(0xFFFFA000), fontWeight = FontWeight.Bold)
                            Text("${hotel.remainingBuildMonths} bulan tersisa", color = Color.LightGray)
                        } else {
                            val occPct = (hotel.lastMonthOccupancyRate * 100).toInt()
                            val occColor = if (occPct < 40) Color(0xFFFF5252) else if (occPct > 80) Color(0xFFFFD700) else Color(0xFF00E676)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Occupancy Rate", color = Color.LightGray)
                                    Text("$occPct%", color = occColor, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                                }
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = hotel.lastMonthOccupancyRate.toFloat(),
                                        modifier = Modifier.size(64.dp),
                                        color = occColor,
                                        trackColor = Color.DarkGray,
                                        strokeWidth = 6.dp
                                    )
                                }
                            }
                            
                            hotel.activeMegaEvent?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("🔔 ACTIVE EVENT: $it", color = Color(0xFFFF4081), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (!hotel.isConstructing) {
                item {
                    Text("Kamar Terjual & Tarif", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val safeRoomConfigs = hotel.roomConfigs ?: mutableMapOf()
                    val totalActiveRooms = safeRoomConfigs.values.filter { it.isEnabled }.sumOf { (hotel.tier.maxRooms * (it.allocationPercent / 100.0)).toInt() }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Manajemen Kamar Multi-Class", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Total Kelas Aktif: ${safeRoomConfigs.count { it.value.isEnabled }}", color = Color.White, fontSize = 14.sp)
                            Text("Kamar Terjual (Berbagai Kelas): ${totalActiveRooms} / ${hotel.tier.maxRooms}", color = Color.White, fontSize = 14.sp)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Pendapatan Kotor: $${nFormat.format(hotel.lastMonthRevenue)}", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Pengeluaran Total: -$${nFormat.format(hotel.lastMonthExpense)}", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            
                            val displayedStaff = 10 + safeRoomConfigs.values.filter { it.isEnabled && it.allocationPercent > 0.0 }.sumOf { config ->
                                val roomClassKey = safeRoomConfigs.entries.find{it.value == config}?.key ?: "STANDARD"
                                val roomClassEnum = try { com.example.data.RoomClassStrategy.valueOf(roomClassKey) } catch(e: Exception) { com.example.data.RoomClassStrategy.STANDARD }
                                val allocatedRooms = (hotel.tier.maxRooms * (config.allocationPercent / 100.0)).toInt()
                                val staffRatio = when(roomClassEnum) {
                                    com.example.data.RoomClassStrategy.STANDARD, com.example.data.RoomClassStrategy.SUPERIOR -> 10
                                    com.example.data.RoomClassStrategy.DELUXE, com.example.data.RoomClassStrategy.JUNIOR_SUITE -> 5
                                    com.example.data.RoomClassStrategy.SUITE, com.example.data.RoomClassStrategy.PRESIDENTIAL -> 2
                                }
                                maxOf(1, allocatedRooms / staffRatio)
                            } + (hotel.builtFacilities.size * 5)
                            
                            val avgSalary = 2000L + (hotel.tier.baseRoomRate * 5)
                            val displayedStaffExpense = displayedStaff * avgSalary
                            
                            var roomOps = 0L
                            for ((k, c) in safeRoomConfigs) {
                                if(c.isEnabled && c.allocationPercent > 0.0) {
                                    val rce = try { com.example.data.RoomClassStrategy.valueOf(k) } catch(e: Exception) { com.example.data.RoomClassStrategy.STANDARD }
                                    val ar = (hotel.tier.maxRooms * (c.allocationPercent / 100.0)).toInt()
                                    roomOps += (ar * (rce.priceMultiplier * 15L).toLong() * 30L)
                                }
                            }
                            
                            val facMaint = hotel.builtFacilities.sumOf { it.maintenanceCost }
                            
                            Text("💡 Gaji Staf (${displayedStaff} org): $${nFormat.format(displayedStaffExpense)} | Ops: $${nFormat.format(roomOps)} | Fasilitas: $${nFormat.format(facMaint)}", color = Color.Gray, fontSize = 10.sp)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigate("room_management/${instanceId}/${hotelId}") },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("⚙️ Manajemen Kelas Kamar & Tarif", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Fasilitas & Layanan Tambahan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                val facilityChunks = HotelFacility.values().toList().chunked(2)
                items(facilityChunks.size) { index ->
                    val rowFacilities = facilityChunks[index]
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (facility in rowFacilities) {
                            val isBuilt = hotel.builtFacilities.contains(facility)
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = if (isBuilt) Color(0xFF2E7D32) else Color(0xFF2C2C2C))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(facility.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (isBuilt) {
                                        Text("Sudah Dibangun", color = Color(0xFFA5D6A7), fontSize = 10.sp)
                                    } else {
                                        Text("Biaya: $${nFormat.format(facility.buildCost)}", color = Color.LightGray, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { viewModel.buildHotelFacility(instanceId, hotelId, facility) },
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.height(30.dp).fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                                        ) {
                                            Text("Bangun", color = Color.Black, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                        if (rowFacilities.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showImageDialog) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text("Ubah Gambar Header", color = Color.White) },
            text = {
                Column {
                    Text("Punya URL gambar yang lebih bagus? Masukkan di sini.", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = imageUrlInput,
                        onValueChange = { imageUrlInput = it },
                        label = { Text("URL Gambar", color = Color.Gray) },
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
                    val finalUrl = if (imageUrlInput.isBlank()) "https://images.unsplash.com/photo-1542314831-c6a4d14d8349?w=800&q=80" else imageUrlInput
                    viewModel.updateHotelImage(instanceId, hotelId, finalUrl)
                    showImageDialog = false
                }) {
                    Text("Simpan", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageDialog = false }) {
                    Text("Batal", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}
