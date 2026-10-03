package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.data.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipyardUI(
    navController: NavHostController,
    viewModel: GameViewModel,
    businessId: String
) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsStateWithLifecycle()

    // Find business instance
    val ownedBusiness = playerState.ownedBusinesses.find { it.instanceId == businessId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == businessId }

    if (ownedBusiness == null) {
        Box(modifier = Modifier.fillMaxSize().background(DeepSeaNavy), contentAlignment = Alignment.Center) {
            Text("Memuat Galangan...", color = WarmOffWhite)
        }
        return
    }

    // Shipyard selection
    var selectedShipyard by remember { mutableStateOf(ShipyardId.HYUNDAI) }
    var selectedClass by remember { mutableStateOf(CruiseShipClass.YACHT) }
    var shipNameInput by remember { mutableStateOf("Maritima") }

    // Computations based on modifiers
    val rawCost = selectedClass.basePrice
    val costModifierPercent = selectedShipyard.costModifier
    val finalPrice = (rawCost * (1.0 + costModifierPercent)).toLong()
    val rawBuildTime = selectedClass.baseBuildTime
    val buildTimeReduction = selectedShipyard.buildTimeReduction
    val finalBuildTime = (rawBuildTime - buildTimeReduction).coerceAtLeast(3)

    val canAfford = ownedBusiness.companyCash >= finalPrice
    
    // Check constraints: Titan class requires Chantiers yard
    val isTitan = selectedClass == CruiseShipClass.TITAN
    val isBlockedDueToYard = isTitan && selectedShipyard != ShipyardId.CHANTIERS

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Galangan Marina & Pembuatan Kapal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("back_button_yard")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepSeaNavy)
            )
        },
        containerColor = DeepSeaNavy
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // INFORMATION NOTE
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = OceanBlue.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = SoftCyan)
                        Text(
                            text = "Gunakan kas divisi untuk mendesain dan membeli kapal baru. Pengerjaan akan diproses di Galangan Mitra terpilih selama beberapa bulan.",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // FORM INPUT: SHIP NAME
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("1. NAMA VESSEL BARU", color = SoftCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    OutlinedTextField(
                        value = shipNameInput,
                        onValueChange = { shipNameInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("new_ship_name"),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = CoralBlue,
                            focusedBorderColor = NeonCyan,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }

            // SELECTION: MITRA SHIPYARD
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. PILIH GALANGAN KAPAL (MITRA)", color = SoftCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ShipyardId.values()) { yard ->
                            val isSelected = selectedShipyard == yard
                            Card(
                                modifier = Modifier
                                    .width(200.dp)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) NeonCyan else CoralBlue.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedShipyard = yard }
                                    .testTag("yard_${yard.name.lowercase()}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) OceanBlue else OceanBlue.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(yard.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(yard.description, color = SandGold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Cost Mod: " + (if (yard.costModifier >= 0) "+" else "") + "${(yard.costModifier * 100).toInt()}%",
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                    if (yard.buildTimeReduction > 0) {
                                        Text("Pengerjaan: -${yard.buildTimeReduction} Bulan", color = SoftCyan, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SELECTION: TIERS CAPACITY CLASS
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. PILIH KELAS DESIGN / UKURAN", color = SoftCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CruiseShipClass.values().forEach { shipClass ->
                            val isSelected = selectedClass == shipClass
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) NeonCyan else CoralBlue.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedClass = shipClass }
                                    .testTag("class_${shipClass.name.lowercase()}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) OceanBlue else OceanBlue.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(shipClass.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Kapasitas Penumpang Terbuka: ${shipClass.maxPax} Pax maksimal", color = Color.Gray, fontSize = 11.sp)
                                        Text("Estimasi Pengerjaan: ${shipClass.baseBuildTime} Bulan standar", color = Color.Gray, fontSize = 11.sp)
                                    }
                                    
                                    Text(
                                        text = formatCurrencyRingkas(shipClass.basePrice, useShortFormat),
                                        color = SoftCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ORDER SUMMARY STATEMENT
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CoralBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = OceanBlue)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("RINGKASAN PESANAN VESSEL", color = SoftCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Divider(color = CoralBlue.copy(alpha = 0.3f))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Model & Kelas", color = Color.Gray, fontSize = 12.sp)
                            Text(selectedClass.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Galangan Pengerjaan", color = Color.Gray, fontSize = 12.sp)
                            Text(selectedShipyard.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Estimasi Waktu Pengerjaan", color = Color.Gray, fontSize = 12.sp)
                            Text("$finalBuildTime Bulan", color = SoftCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Daya Tampung", color = Color.Gray, fontSize = 12.sp)
                            Text("${selectedClass.maxPax} Pax", color = Color.White, fontSize = 12.sp)
                        }

                        Divider(color = CoralBlue.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("TOTAL INVESTASI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = formatCurrencyRingkas(finalPrice, useShortFormat),
                                color = NeonCyan,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                        
                        Text(
                            text = "Kas Divisi Tersedia: ${formatCurrency(ownedBusiness.companyCash.toLong())}",
                            color = if (canAfford) Color.Green else Color(0xFFFF5252),
                            fontSize = 11.sp,
                            modifier = Modifier.align(Alignment.End)
                        )

                        // Warning message if blocking logic hit
                        if (isBlockedDueToYard) {
                            Text(
                                text = "KENDALA TEKNIK: Pembuatan Kapal kelas Titan / Record-Breaker (kapasitas >6.000 pax) WAJIB menggunakan jasa Galangan Chantiers de l'Atlantique.",
                                color = Color(0xFFFF5252),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            // ORDER SUBMISSION BUTTON
            item {
                Button(
                    onClick = {
                        val ordered = viewModel.orderCruiseShip(
                            businessId = businessId,
                            name = shipNameInput.trim().ifEmpty { "Maritima Pride" },
                            shipClass = selectedClass,
                            shipyard = selectedShipyard
                        )
                        if (ordered) {
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_order_btn"),
                    enabled = canAfford && !isBlockedDueToYard,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = DeepSeaNavy,
                        disabledContainerColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("KIRIM SPESIFIKASI DAN MULAI PRODUKSI", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
