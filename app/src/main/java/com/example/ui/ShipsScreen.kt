package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShipsScreen(
    instanceId: String,
    shipId: String,
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()

    val ownedBusiness = playerState.ownedBusinesses.find { it.instanceId == instanceId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }

    if (ownedBusiness == null) {
        Box(modifier = Modifier.fillMaxSize().background(DeepSeaNavy), contentAlignment = Alignment.Center) {
            Text("Data bisnis tidak ditemukan.", color = WarmOffWhite)
        }
        return
    }

    val ship = ownedBusiness.cruiseShips?.find { it.id == shipId }
    if (ship == null) {
        Box(modifier = Modifier.fillMaxSize().background(DeepSeaNavy), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Kapal tidak ditemukan atau telah di-Scrap.", color = WarmOffWhite)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSeaNavy)) {
                    Text("Kembali")
                }
            }
        }
        return
    }

    // Modal state controllers
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameNameInput by remember { mutableStateOf(ship.name) }
    var renameImageInput by remember { mutableStateOf(ship.customImageUrl ?: "") }

    var showPriceDialog by remember { mutableStateOf<String?>(null) } // "REGULAR", "VIP", "VVIP", "GRAND_SUITE"
    var priceInputString by remember { mutableStateOf("") }

    var showRouteDialog by remember { mutableStateOf(false) }
    var showScrapDialog by remember { mutableStateOf(false) }

    val isBuilding = ship.monthsUntilDelivery > 0
    val isDrydocking = ship.isUnderUnderDrydockCheck(ship) // safely check
    val assignedPort = CRUISE_PORTS_CATALOG.find { it.id == ship.assignedPortId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(ship.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text("${ship.shipClass.title} • Armada ${ownedBusiness.customName ?: "Oceanic Cruise"}", fontSize = 12.sp, color = SoftCyan)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button_ships")) {
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
            // 1. SHIP HERO IMAGE with Rename Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CoralBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    val fallbackImgUrl = when (ship.shipClass) {
                        CruiseShipClass.YACHT -> "https://images.unsplash.com/photo-1559600088-01f7d8974913?auto=format"
                        CruiseShipClass.MIDSIZE -> "https://images.unsplash.com/photo-1548574505-5e239809ee19?auto=format"
                        CruiseShipClass.LARGE -> "https://images.unsplash.com/photo-1569263979104-865ab7cd8d13?auto=format"
                        else -> "https://images.unsplash.com/photo-1559600088-01f7d8974913?auto=format"
                    }

                    SubcomposeAsyncImage(
                        model = ship.customImageUrl ?: fallbackImgUrl,
                        contentDescription = "Kapal Pesiar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, DeepSeaNavy.copy(alpha = 0.85f)),
                                    startY = 100f
                                )
                            )
                    )

                    // Rename action trigger floating on image
                    IconButton(
                        onClick = {
                            renameNameInput = ship.name
                            renameImageInput = ship.customImageUrl ?: ""
                            showRenameDialog = true
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Nama Kapal", tint = Color.White, modifier = Modifier.size(16.dp))
                    }

                    // Operational status badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Column {
                            val statusText = when {
                                isBuilding -> "DALAM PEMBANGUNAN (${ship.constructionProgressPercent}%)"
                                ship.isUnderDrydock -> "DRYDOCK MAINTENANCE"
                                ship.assignedPortId == null -> "STANDBY (BELUM ADA RUTE)"
                                else -> "AKTIF BERLAYAR"
                            }
                            val statusBg = when {
                                isBuilding -> SoftCyan.copy(alpha = 0.85f)
                                ship.isUnderDrydock -> SandGold.copy(alpha = 0.85f)
                                ship.assignedPortId == null -> Color.Gray
                                else -> Color(0xFF4CAF50)
                            }
                            val statusColor = if (isBuilding || ship.isUnderDrydock) Color.Black else Color.White

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // CONSTRUCTION PROGRESS CARD (IF STILL IN SHIPYARD)
            if (isBuilding) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = OceanBlue)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Status Pembuatan di Galangan", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Penyedia: ${ship.shipyard.title}", color = SoftCyan, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Sisa Waktu Pembuatan: ${ship.monthsUntilDelivery} Bulan", color = Color.LightGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { ship.constructionProgressPercent / 100f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = NeonCyan,
                                trackColor = Color.DarkGray
                            )
                        }
                    }
                }
            }

            // 2. HEALTH & MAINTENANCE SECTOR
            if (!isBuilding) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = OceanBlue)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Kondisi Fisik & Pemeliharaan", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Lambung (Hull)", color = Color.LightGray, fontSize = 11.sp)
                                        Text("${ship.hullHealth.toInt()}%", color = if (ship.hullHealth < 50.0) Color.Red else Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { (ship.hullHealth / 100.0).toFloat() },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = if (ship.hullHealth < 50.0) Color.Red else NeonCyan,
                                        trackColor = Color.DarkGray
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text("Mesin (Engine)", color = Color.LightGray, fontSize = 11.sp)
                                        Text("${ship.engineHealth.toInt()}%", color = if (ship.engineHealth < 50.0) Color.Red else Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { (ship.engineHealth / 100.0).toFloat() },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = if (ship.engineHealth < 50.0) Color.Red else NeonCyan,
                                        trackColor = Color.DarkGray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (ship.isUnderDrydock) "Status: Kapal sedang berada di Drydock untuk perbaikan penuh."
                                else "Pemakaian Drydock berkala wajib dalam: ${(60 - ship.monthsSinceDrydock).coerceAtLeast(0)} bulan lagi",
                                color = if (60 - ship.monthsSinceDrydock < 10) Color(0xFFFFCC00) else Color.LightGray,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val ok = viewModel.sendCruiseShipToDrydock(instanceId, ship.id)
                                        if (ok) {
                                            Toast.makeText(context, "Kapal berhasil dikirim ke Drydock (2 Bulan)", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Gagal mengirim ke Drydock.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    enabled = !ship.isUnderDrydock && !isBuilding,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SandGold,
                                        contentColor = Color.Black,
                                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kirim Drydock", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { showScrapDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252), contentColor = Color.White),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scrap (Jual Kapal)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 3. EDITABLE TICKET PRICES BY PASSENGER CATEGORY
            if (!isBuilding) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = OceanBlue)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Manajemen Tarif Tiket Per Kategori", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text("Penumpang dibagi merata secara otomatis berdasarkan kapasitas kapal.", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            val categories = listOf(
                                Triple("REGULAR", "Reguler (70%)", ship.ticketPriceRegular),
                                Triple("VIP", "VIP (20%)", ship.ticketPriceVip),
                                Triple("VVIP", "VVIP (8%)", ship.ticketPriceVvip),
                                Triple("GRAND_SUITE", "Grand Suite (2%)", ship.ticketPriceGrandSuite)
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                categories.forEach { (key, label, currentPrice) ->
                                    val lastMonthPax = when (key) {
                                        "REGULAR" -> ship.lastMonthPassengersRegular
                                        "VIP" -> ship.lastMonthPassengersVip
                                        "VVIP" -> ship.lastMonthPassengersVvip
                                        else -> ship.lastMonthPassengersGrandSuite
                                    }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(label, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                            Text("Penumpang Bulan Lalu: $lastMonthPax pax", color = Color.Gray, fontSize = 11.sp)
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = formatCurrencyRingkas(currentPrice, useShortFormat),
                                                color = NeonCyan,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 15.sp
                                            )
                                            IconButton(
                                                onClick = {
                                                    priceInputString = currentPrice.toString()
                                                    showPriceDialog = key
                                                },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Ubah Tarif", tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. NAVIGATIONAL ROUTE ASSIGNMENT
            if (!isBuilding) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = OceanBlue)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Rute & Destinasi Berlayar", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Button(
                                    onClick = { showRouteDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSeaNavy),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Ubah Rute", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (assignedPort == null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = SandGold, modifier = Modifier.size(20.dp))
                                    Text("Belum ada rute berlayar. Kapal standby dan memakan biaya pemeliharaan tanpa omset!", color = WarmOffWhite, fontSize = 12.sp)
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(assignedPort.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text(assignedPort.description, color = Color.Gray, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Biaya Port Fee / Bln:", color = Color.Gray, fontSize = 11.sp)
                                        Text(formatCurrencyRingkas(assignedPort.portFee, useShortFormat), color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Daya Tarik Base Demand:", color = Color.Gray, fontSize = 11.sp)
                                        Text("${assignedPort.baseDemand} Pax", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. MODULAR FACILITIES SECTION
            if (!isBuilding) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = OceanBlue)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Manajemen Fasilitas Moduler", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text(
                                text = "Instalasi modul meningkatkan daya tarik (demand) dan onboard-spend tiap tamu.\nKas Divisi: ${formatCurrencyRingkas(ownedBusiness.companyCash.toLong(), useShortFormat)}",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                CRUISE_FACILITIES_CATALOG.forEach { facility ->
                                    val alreadyInstalled = ship.builtFacilities.contains(facility.id)
                                    val canAfford = ownedBusiness.companyCash >= facility.cost

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = 1.dp,
                                                color = if (alreadyInstalled) NeonCyan.copy(alpha = 0.5f) else CoralBlue.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (alreadyInstalled) Color.Black.copy(alpha = 0.2f) else DeepSeaNavy.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(facility.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(facility.description, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Text("Beli: ${formatCurrencyRingkas(facility.cost, useShortFormat)}", color = SoftCyan, fontSize = 10.sp)
                                                    Text("Maint: ${formatCurrencyRingkas(facility.maintenance, useShortFormat)}/bln", color = Color(0xFFFF5252), fontSize = 10.sp)
                                                }
                                                Text(
                                                    text = "Efek: +${(facility.buffDemand * 100).toInt()}% Tarik • +$${facility.buffRevenue} Spend/Pax",
                                                    color = SandGold,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    val done = viewModel.buyCruiseFacility(instanceId, ship.id, facility.id)
                                                    if (done) {
                                                        Toast.makeText(context, "${facility.name} sukses dipasang!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Kas divisi tidak mencukupi!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                enabled = !alreadyInstalled && canAfford,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = NeonCyan,
                                                    contentColor = DeepSeaNavy,
                                                    disabledContainerColor = Color.Gray.copy(alpha = 0.2f)
                                                ),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text(if (alreadyInstalled) "Terpasang" else "Instal", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. LAST MONTH OPERATIONS LOG
            if (!isBuilding) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = OceanBlue)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Laporan Keuangan & Ops Bulan Lalu", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Jumlah Penumpang:", color = Color.Gray, fontSize = 12.sp)
                                Text("${ship.lastMonthPassengers} Pax / ${ship.maxPax} Kapasitas", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = CoralBlue.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pendapatan Tiket:", color = Color.Gray, fontSize = 12.sp)
                                Text(formatCurrencyRingkas(ship.lastMonthTicketRevenue, useShortFormat), color = Color.Green, fontSize = 12.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pendapatan Onboard Spend:", color = Color.Gray, fontSize = 12.sp)
                                Text(formatCurrencyRingkas(ship.lastMonthOnboardRevenue, useShortFormat), color = Color.Green, fontSize = 12.sp)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pengeluaran Operasional & Maint:", color = Color.Gray, fontSize = 12.sp)
                                Text("-" + formatCurrencyRingkas(ship.lastMonthExpenses, useShortFormat), color = Color(0xFFFF5252), fontSize = 12.sp)
                            }

                            val netMonthProfit = (ship.lastMonthTicketRevenue + ship.lastMonthOnboardRevenue) - ship.lastMonthExpenses
                            val netColor = if (netMonthProfit >= 0) Color.Green else Color(0xFFFF5252)

                            Divider(modifier = Modifier.padding(vertical = 8.dp), color = CoralBlue.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Profit Bersih Kapal:", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text(
                                    text = (if (netMonthProfit >= 0) "+" else "") + formatCurrencyRingkas(netMonthProfit, useShortFormat),
                                    fontWeight = FontWeight.Black,
                                    color = netColor,
                                    fontSize = 13.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(
                                            imageVector = if (ship.lastMonthAccidentOccurred) Icons.Default.Warning else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = if (ship.lastMonthAccidentOccurred) Color.Red else Color.Green,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (ship.lastMonthAccidentOccurred) "Catatan Insiden" else "Laporan Perjalanan",
                                            color = if (ship.lastMonthAccidentOccurred) Color.Red else Color.Green,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ship.lastMonthAccidentReport ?: "Kapal berlayar mulus tanpa kendala.",
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // --- DIALOGS ---

    // 1. RENAME & IMAGE EDIT DIALOG
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = OceanBlue,
            title = {
                Text("Ubah Identitas Kapal", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tentukan nama baru dan tautan gambar representatif untuk kapal pesiar Anda.", color = Color.LightGray, fontSize = 12.sp)

                    OutlinedTextField(
                        value = renameNameInput,
                        onValueChange = { renameNameInput = it },
                        label = { Text("Nama Kapal") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = renameImageInput,
                        onValueChange = { renameImageInput = it },
                        label = { Text("Tautan URL Gambar (Opsional)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameNameInput.isNotBlank()) {
                            viewModel.renameCruiseShip(
                                instanceId,
                                ship.id,
                                renameNameInput,
                                if (renameImageInput.isBlank()) null else renameImageInput
                            )
                            showRenameDialog = false
                            Toast.makeText(context, "Identitas kapal berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSeaNavy)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // 2. EDIT TICKET PRICE DIALOG
    if (showPriceDialog != null) {
        val cat = showPriceDialog!!
        val catLabel = when (cat) {
            "REGULAR" -> "Reguler"
            "VIP" -> "VIP"
            "VVIP" -> "VVIP"
            else -> "Grand Suite"
        }

        AlertDialog(
            onDismissRequest = { showPriceDialog = null },
            containerColor = OceanBlue,
            title = {
                Text("Tarif Tiket: $catLabel", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tentukan tarif tiket per penumpang dalam mata uang Dollar ($).", color = Color.LightGray, fontSize = 12.sp)

                    OutlinedTextField(
                        value = priceInputString,
                        onValueChange = { priceInputString = it },
                        label = { Text("Tarif Tiket ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Helper steppers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val curVal = priceInputString.toLongOrNull() ?: 0L
                                priceInputString = (curVal - 50).coerceAtLeast(0).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralBlue, contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("-$50")
                        }

                        Button(
                            onClick = {
                                val curVal = priceInputString.toLongOrNull() ?: 0L
                                priceInputString = (curVal + 50).toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralBlue, contentColor = Color.White),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+$50")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val inputPrice = priceInputString.toLongOrNull() ?: 0L
                        if (inputPrice >= 0) {
                            var reg = ship.ticketPriceRegular
                            var vip = ship.ticketPriceVip
                            var vvip = ship.ticketPriceVvip
                            var grand = ship.ticketPriceGrandSuite

                            when (cat) {
                                "REGULAR" -> reg = inputPrice
                                "VIP" -> vip = inputPrice
                                "VVIP" -> vvip = inputPrice
                                "GRAND_SUITE" -> grand = inputPrice
                            }

                            viewModel.updateCruiseShipTicketPrices(instanceId, ship.id, reg, vip, vvip, grand)
                            showPriceDialog = null
                            Toast.makeText(context, "Tarif tiket $catLabel berhasil diubah!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Masukkan angka tarif yang valid!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSeaNavy)
                ) {
                    Text("Terapkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPriceDialog = null }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // 3. SET PORT/ROUTE DIALOG
    if (showRouteDialog) {
        val unlockedPorts = CRUISE_PORTS_CATALOG.filter { port ->
            port.id == "miami" || (ownedBusiness.cruisePortsUnlocked ?: emptyList()).contains(port.id)
        }

        AlertDialog(
            onDismissRequest = { showRouteDialog = false },
            containerColor = DeepSeaNavy,
            title = {
                Text("Pilih Rute Pelayaran", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text(
                        "Raja Ampat hanya dapat disandari oleh jenis Yacht mewah.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(unlockedPorts) { port ->
                            val isRajaAmpat = port.id == "raja_ampat"
                            val isYacht = ship.shipClass == CruiseShipClass.YACHT
                            val isBlockedRajaAmpat = isRajaAmpat && !isYacht

                            val alreadyAssigned = ship.assignedPortId == port.id

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = if (alreadyAssigned) NeonCyan.copy(alpha = 0.5f) else CoralBlue.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isBlockedRajaAmpat) Color.DarkGray.copy(alpha = 0.3f) else if (alreadyAssigned) OceanBlue else OceanBlue.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(port.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            if (isBlockedRajaAmpat) {
                                                Text("(Yacht Only)", color = Color(0xFFFF5252), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(port.description, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Port Fee: ${formatCurrencyRingkas(port.portFee, useShortFormat)} • Base Demand: ${port.baseDemand} pax",
                                            color = SoftCyan,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.assignCruiseShipPort(instanceId, ship.id, port.id)
                                            showRouteDialog = false
                                            Toast.makeText(context, "Rute kapal dialihkan ke ${port.name}!", Toast.LENGTH_SHORT).show()
                                        },
                                        enabled = !alreadyAssigned && !isBlockedRajaAmpat,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NeonCyan,
                                            contentColor = DeepSeaNavy,
                                            disabledContainerColor = Color.DarkGray.copy(alpha = 0.3f)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(if (alreadyAssigned) "Aktif" else "Pilih", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRouteDialog = false }) {
                    Text("Tutup", color = Color.LightGray)
                }
            }
        )
    }

    // 4. SCRAP VESSEL WARNING DIALOG
    if (showScrapDialog) {
        val scrapValue = (ship.pricePaid * 0.15).toLong()

        AlertDialog(
            onDismissRequest = { showScrapDialog = false },
            containerColor = OceanBlue,
            title = {
                Text("Scrap Vessel (Bongkar Kapal)", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Apakah Anda yakin ingin membongkar kapal ${ship.name}? Tindakan ini bersifat permanen dan tidak dapat dibatalkan.",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Anda akan menerima pengembalian dana besi tua sebesar 15% dari harga pembelian awal: ${formatCurrencyRingkas(scrapValue, useShortFormat)}.",
                        color = SandGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.scrapCruiseShip(instanceId, ship.id)
                        showScrapDialog = false
                        onBack()
                        Toast.makeText(context, "Kapal ${ship.name} berhasil dibongkar untuk scrap besi tua!", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252), contentColor = Color.White)
                ) {
                    Text("Ya, Bongkar Kapal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showScrapDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }
}

// Extension or inline helper to safely check if ship is in drydock or check values
fun CruiseShip.isUnderUnderDrydockCheck(ship: CruiseShip): Boolean {
    return ship.isUnderDrydock
}
