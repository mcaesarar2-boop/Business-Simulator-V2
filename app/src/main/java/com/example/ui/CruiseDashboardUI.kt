package com.example.ui

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.data.*
import com.example.viewmodel.GameViewModel

// Color Constants for Ocean Blue / Deep Sea Navy Theme
val DeepSeaNavy = Color(0xFF0B132B)
val OceanBlue = Color(0xFF1C2541)
val CoralBlue = Color(0xFF3A506B)
val NeonCyan = Color(0xFF00EBFF)
val SoftCyan = Color(0xFF80F5FF)
val SandGold = Color(0xFFF7B538)
val WarmOffWhite = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CruiseDashboardUI(
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
            Text("Membuka data atau pemulihan data...", color = WarmOffWhite)
        }
        return
    }

    // Modal state controllers
    var showCapitalDialog by remember { mutableStateOf(false) }
    var capitalActionType by remember { mutableStateOf("inject") } // "inject" or "withdraw"
    var capitalInputAmount by remember { mutableStateOf("") }
    
    var shipToRename by remember { mutableStateOf<CruiseShip?>(null) }
    var newShipNameInput by remember { mutableStateOf("") }
    var newShipImageInput by remember { mutableStateOf("") }
    
    var shipToManageFacilities by remember { mutableStateOf<CruiseShip?>(null) }
    var shipToAssignRoute by remember { mutableStateOf<CruiseShip?>(null) }
    var selectedShipId by remember { mutableStateOf<String?>(null) }

    if (selectedShipId != null) {
        ShipsScreen(
            instanceId = businessId,
            shipId = selectedShipId!!,
            viewModel = viewModel,
            onBack = { selectedShipId = null }
        )
        return
    }

    val shipsList = ownedBusiness.cruiseShips ?: emptyList()
    val prestige = ownedBusiness.cruiseBrandPrestige
    val totalCashflow = shipsList.sumOf { (it.lastMonthTicketRevenue + it.lastMonthOnboardRevenue) - it.lastMonthExpenses }
    val totalPassengers = shipsList.sumOf { it.lastMonthPassengers }
    val totalFleetCount = shipsList.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = ownedBusiness.customName ?: "Oceanic Cruise Group",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Divisi Kapal Pesiar / Cruise Line",
                            fontSize = 12.sp,
                            color = SoftCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("back_button_dash")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Portfolio",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepSeaNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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
            // HERO CARD BACKGROUND ART
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(OceanBlue, DeepSeaNavy),
                                startY = 0f,
                                endY = 400f
                            )
                        )
                        .border(1.dp, CoralBlue.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    SubcomposeAsyncImage(
                        model = "https://images.unsplash.com/photo-1559600088-01f7d8974913?q=80&w=1171&auto=format&fit=crop",
                        contentDescription = "Kapal Pesiar Mewah di Lautan",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize().clip(RoundedCornerShape(16.dp)),
                        alpha = 0.45f
                    )
                    Column(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "PRESTIGE BRAND",
                            color = SoftCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = SandGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "$prestige / 100 PTS",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            // DYNAMIC GLOBAL STATS ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, CoralBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = OceanBlue)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Fleet Size", color = Color.LightGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("$totalFleetCount Kapal", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, CoralBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = OceanBlue)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Penumpang", color = Color.LightGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${formatCurrencyRingkas(totalPassengers.toLong(), useShortFormat)} Pax", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, CoralBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = OceanBlue)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Estimasi Arus Kas", color = Color.LightGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            val isLoss = totalCashflow < 0
                            Text(
                                text = (if (!isLoss) "+" else "") + formatCurrencyRingkas(totalCashflow, useShortFormat),
                                color = if (isLoss) Color(0xFFFF5252) else Color(0xFF00FF66),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // TREASURY: INJECT AND WITHDRAWAL PANEL
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CoralBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = OceanBlue)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("KAS DIVISI (TREASURY)", color = SoftCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SoftCyan, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatCurrencyRingkas(ownedBusiness.companyCash.toLong(), useShortFormat),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    capitalActionType = "inject"
                                    capitalInputAmount = ""
                                    showCapitalDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("inject_cash_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308), contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Suntik Modal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            
                            Button(
                                onClick = {
                                    capitalActionType = "withdraw"
                                    capitalInputAmount = ""
                                    showCapitalDialog = true
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("withdraw_cash_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tarik Dividen", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // NAVIGATION BUTTONS: SHIPYARD & PORT MANAGER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { navController.navigate("cruise_shipyard/${businessId}") },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("navigate_shipyard_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftCyan),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, NeonCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DirectionsBoat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("YARD PEMBUATAN", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { navController.navigate("cruise_route_manager/${businessId}") },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("navigate_routes_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftCyan),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, NeonCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("KELOLA RUTE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // FLEET SECTION HEADER
            item {
                Text(
                    text = "DAFTAR ARMADA VESSEL",
                    color = SoftCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // FLEET SHIPS ITERATION
            if (shipsList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CoralBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = OceanBlue.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Inbox, contentDescription = null, tint = CoralBlue, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Belum memiliki kapal pesiar aktif.", color = WarmOffWhite, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Pergi ke Yard Pembuatan untuk memesan Kapal pertama Anda dan merintis kemakmuran samudra!", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { navController.navigate("cruise_shipyard/${businessId}") },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSeaNavy)
                            ) {
                                Text("Pesan Kapal Sekarang", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(shipsList) { ship ->
                    ShipItemCard(
                        ship = ship,
                        useShortFormat = useShortFormat,
                        availablePorts = CRUISE_PORTS_CATALOG,
                        onRenameClick = {
                            shipToRename = ship
                            newShipNameInput = ship.name
                            newShipImageInput = ship.customImageUrl ?: ""
                        },
                        onManageFacilitiesClick = {
                            shipToManageFacilities = ship
                        },
                        onAssignRouteClick = {
                            shipToAssignRoute = ship
                        },
                        onDrydockClick = {
                            val ok = viewModel.sendCruiseShipToDrydock(businessId, ship.id)
                            if (ok) {
                                // successful
                            }
                        },
                        onScrapClick = {
                            viewModel.scrapCruiseShip(businessId, ship.id)
                        },
                        onCardClick = {
                            selectedShipId = ship.id
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // --- MODAL DIALOGS ---

    // 1. CAPITAL TREASURY TRANSFER DIALOG
    if (showCapitalDialog) {
        AlertDialog(
            onDismissRequest = { showCapitalDialog = false },
            containerColor = OceanBlue,
            title = {
                Text(
                    text = if (capitalActionType == "inject") "Suntik Modal Bisnis" else "Tarik Dividen Perusahaan",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (capitalActionType == "inject")
                            "Suntik modal dari Kas Global ke divisi ini. Sisa Kas Global Anda: ${formatCurrency(playerState.cash)}"
                        else
                            "Tarik kas divisi ini ke dalam Dompet Global Anda. Cadangan Kas divisi: ${formatCurrency(ownedBusiness.companyCash.toLong())}",
                        color = Color.LightGray,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = capitalInputAmount,
                        onValueChange = { capitalInputAmount = it },
                        placeholder = { Text("Masukkan Jumlah Dana", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = CoralBlue,
                            focusedBorderColor = NeonCyan,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("treasury_amount_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = capitalInputAmount.toLongOrNull() ?: 0L
                        if (amt > 0) {
                            val isDone = if (capitalActionType == "inject") {
                                viewModel.injectCapitalToBusiness(businessId, amt)
                            } else {
                                viewModel.withdrawCapitalFromBusiness(businessId, amt)
                            }
                            if (isDone) {
                                showCapitalDialog = false
                            }
                        }
                    },
                    modifier = Modifier.testTag("treasury_confirm_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSeaNavy)
                ) {
                    Text("Konfirmasi", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCapitalDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // 2. RENAME / CUSTOM CUSTOMIZATION DIALOG
    if (shipToRename != null) {
        val target = shipToRename!!
        AlertDialog(
            onDismissRequest = { shipToRename = null },
            containerColor = OceanBlue,
            title = {
                Text("Kustomisasi Kapal", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Ubah nama dan background representatif armada kapal pesiar Anda.", color = Color.LightGray, fontSize = 13.sp)
                    
                    OutlinedTextField(
                        value = newShipNameInput,
                        onValueChange = { newShipNameInput = it },
                        label = { Text("Nama Vessel", color = SoftCyan) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = CoralBlue,
                            focusedBorderColor = NeonCyan,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("ship_rename_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newShipImageInput,
                        onValueChange = { newShipImageInput = it },
                        label = { Text("URL Gambar Kustom (Opsional)", color = SoftCyan) },
                        placeholder = { Text("https://url_unsplash_gambar.com") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = CoralBlue,
                            focusedBorderColor = NeonCyan,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameCruiseShip(
                            businessId,
                            target.id,
                            newShipNameInput.trim().ifEmpty { target.name },
                            newShipImageInput.trim().ifEmpty { null }
                        )
                        shipToRename = null
                    },
                    modifier = Modifier.testTag("ship_rename_confirm"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DeepSeaNavy)
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { shipToRename = null }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // 3. MODULAR FACILITIES MANAGER DIALOG
    if (shipToManageFacilities != null) {
        val target = shipToManageFacilities!!
        var innerCashOfCompany by remember { mutableStateOf(ownedBusiness.companyCash.toLong()) }
        
        AlertDialog(
            onDismissRequest = { shipToManageFacilities = null },
            containerColor = DeepSeaNavy,
            modifier = Modifier.fillMaxWidth(0.95f),
            title = {
                Text("Fasilitas Moduler: ${target.name}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text(
                        text = "Modul terinstal pada kapal meningkatkan occupancy/demand dan nambah onboard-spend per pax.\nKas Kasir Divisi: ${formatCurrencyRingkas(ownedBusiness.companyCash.toLong(), useShortFormat)}",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(CRUISE_FACILITIES_CATALOG) { facility ->
                            val alreadyInstalled = target.builtFacilities.contains(facility.id)
                            val canAfford = ownedBusiness.companyCash >= facility.cost
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = if (alreadyInstalled) NeonCyan.copy(alpha = 0.5f) else CoralBlue.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (alreadyInstalled) OceanBlue else OceanBlue.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(facility.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(facility.description, color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Biaya: ${formatCurrencyRingkas(facility.cost, useShortFormat)} • Pemeliharaan: ${formatCurrencyRingkas(facility.maintenance, useShortFormat)}/bln",
                                            color = SoftCyan,
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            "Efek: +${(facility.buffDemand * 100).toInt()}% Demand • +$${facility.buffRevenue} Onboard Spend",
                                            color = SandGold,
                                            fontSize = 10.sp
                                        )
                                    }
                                    
                                    Button(
                                        onClick = {
                                            val done = viewModel.buyCruiseFacility(businessId, target.id, facility.id)
                                            if (done) {
                                                // Refresh local simulation state safely
                                                innerCashOfCompany -= facility.cost
                                                shipToManageFacilities = shipToManageFacilities?.copy(
                                                    builtFacilities = (shipToManageFacilities?.builtFacilities ?: emptyList()) + facility.id
                                                )
                                            }
                                        },
                                        enabled = !alreadyInstalled && canAfford,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (alreadyInstalled) CoralBlue else NeonCyan,
                                            contentColor = DeepSeaNavy,
                                            disabledContainerColor = Color.DarkGray
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp).testTag("install_${facility.id}")
                                    ) {
                                        Text(
                                            text = if (alreadyInstalled) "Terpasang" else "Instal",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { shipToManageFacilities = null },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralBlue, contentColor = Color.White)
                ) {
                    Text("Selesai")
                }
            }
        )
    }

    // 4. PORT ROUTE ASSIGN DIALOG
    if (shipToAssignRoute != null) {
        val target = shipToAssignRoute!!
        val unlockedPorts = CRUISE_PORTS_CATALOG.filter { port ->
            port.id == "miami" || (ownedBusiness.cruisePortsUnlocked ?: emptyList()).contains(port.id)
        }

        AlertDialog(
            onDismissRequest = { shipToAssignRoute = null },
            containerColor = DeepSeaNavy,
            title = {
                Text("Navigasi Rute Berlayar", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text(
                        "Pilih pelabuhan tujuan kapal ini. Port fee akan ditagihkan ke operasional kapal tiap bulan. Raja Ampat hanya bisa disandari kapal kelas Yacht/Small.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    if (unlockedPorts.isEmpty()) {
                        Text("Membuka rute...", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(250.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(unlockedPorts) { port ->
                                // Raja Ampat safety check: only for Yacht/Small
                                val isRajaAmpat = port.id == "raja_ampat"
                                val isYacht = target.shipClass == CruiseShipClass.YACHT
                                val isBlockedRajaAmpat = isRajaAmpat && !isYacht
                                
                                val alreadyAssigned = target.assignedPortId == port.id
                                
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
                                                Text(port.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                if (isBlockedRajaAmpat) {
                                                    Text("(Yacht Only)", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Text(port.description, color = Color.Gray, fontSize = 11.sp)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                "Port Fee: ${formatCurrencyRingkas(port.portFee, useShortFormat)} • Base Demand: ${port.baseDemand} pax",
                                                color = SoftCyan,
                                                fontSize = 10.sp
                                            )
                                        }
                                        
                                        Button(
                                            onClick = {
                                                viewModel.assignCruiseShipPort(businessId, target.id, port.id)
                                                shipToAssignRoute = null
                                            },
                                            enabled = !alreadyAssigned && !isBlockedRajaAmpat,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (alreadyAssigned) CoralBlue else NeonCyan,
                                                contentColor = DeepSeaNavy,
                                                disabledContainerColor = Color.DarkGray
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp).testTag("assign_${port.id}")
                                        ) {
                                            Text(
                                                text = if (alreadyAssigned) "Aktif" else "Pilih",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { shipToAssignRoute = null }) {
                    Text("Tutup", color = Color.LightGray)
                }
            }
        )
    }
}

// COMPONENT: INDIVIDUAL SHIP ITEM CARD
@Composable
fun ShipItemCard(
    ship: CruiseShip,
    useShortFormat: Boolean,
    availablePorts: List<CruisePort>,
    onRenameClick: () -> Unit,
    onManageFacilitiesClick: () -> Unit,
    onAssignRouteClick: () -> Unit,
    onDrydockClick: () -> Unit,
    onScrapClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val assignedPort = availablePorts.find { it.id == ship.assignedPortId }
    val isBuilding = ship.monthsUntilDelivery > 0
    val isDrydocking = ship.isUnderDrydock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .border(
                width = 1.dp,
                color = if (isDrydocking) SandGold.copy(alpha = 0.5f) else CoralBlue.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDrydocking) OceanBlue.copy(alpha = 0.8f) else OceanBlue
        )
    ) {
        Column {
            // REPRESENTATIVE IMAGE WITH DECORATIVE OVERLAYS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                // FALLBACK URLS BASED ON CLASSES
                val fallbackImgUrl = when (ship.shipClass) {
                    CruiseShipClass.YACHT -> "https://images.unsplash.com/photo-1559600088-01f7d8974913?auto=format"
                    CruiseShipClass.MIDSIZE -> "https://images.unsplash.com/photo-1548574505-5e239809ee19?auto=format"
                    CruiseShipClass.LARGE -> "https://images.unsplash.com/photo-1569263979104-865ab7cd8d13?auto=format"
                    else -> "https://images.unsplash.com/photo-1559600088-01f7d8974913?auto=format"
                }
                
                SubcomposeAsyncImage(
                    model = ship.customImageUrl ?: fallbackImgUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF1C2541)),
                                startY = 150f
                            )
                        )
                )

                // STATE BADGE (In Construction, Drydock, Active)
                val badgeText: String
                val badgeColor: Color
                val badgeBg: Color
                
                when {
                    isBuilding -> {
                        badgeText = "DALAM PEMBANGUNAN"
                        badgeColor = Color.Black
                        badgeBg = SoftCyan
                    }
                    isDrydocking -> {
                        badgeText = "DRYDOCK MAINTENANCE"
                        badgeColor = Color.Black
                        badgeBg = SandGold
                    }
                    else -> {
                        badgeText = "SAIL ACTIVE"
                        badgeColor = Color.White
                        badgeBg = Color(0xFF4CAF50)
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(badgeText, color = badgeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(ship.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${ship.shipClass.title} Class • Built by ${ship.shipyard.title}", color = SoftCyan, fontSize = 11.sp)
                    }
                    IconButton(
                        onClick = onRenameClick,
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .testTag("rename_${ship.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Nama/Gambar", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // DISSOLVING MAIN DETAIL AREA
            Column(modifier = Modifier.padding(16.dp)) {
                if (isBuilding) {
                    // Progress construction layout
                    val progress = ship.constructionProgressPercent
                    Text("Estimasi Penyelesaian Yard: ${ship.monthsUntilDelivery} Bulan lagi", color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = NeonCyan,
                        trackColor = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Progress: $progress%", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.End))
                } else if (isDrydocking) {
                    // Progress Drydocking
                    Text("Peralatan kapal, lambung bawah air, mesin sedang direstorasi penuh.", color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (3 - ship.drydockMonthsRemaining) / 2f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = SandGold,
                        trackColor = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Sisa Waktu Drydock: ${ship.drydockMonthsRemaining} Bulan", color = SandGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = SandGold, modifier = Modifier.size(16.dp))
                        Text(ship.lastMonthAccidentReport ?: "Sedang bersandar di dermaga yard.", color = WarmOffWhite, fontSize = 11.sp)
                    }
                } else {
                    // Normal Operating boat details
                    
                    // Route & Destination Assigned
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Rute Berlayar: " + (assignedPort?.name ?: "BELUM DITETAPKAN"),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        
                        OutlinedButton(
                            onClick = onAssignRouteClick,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp).testTag("assign_route_${ship.id}"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
                        ) {
                            Text("Set Rute", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // Ship Hull and Engine Wear Progress Bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Lambung (Hull)", color = Color.Gray, fontSize = 11.sp)
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
                                Text("Mesin (Engine)", color = Color.Gray, fontSize = 11.sp)
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

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pemakaian Drydock berkala wajib dalam: ${(60 - ship.monthsSinceDrydock).coerceAtLeast(0)} bln lagi",
                        color = if (60 - ship.monthsSinceDrydock < 10) Color(0xFFFFCC00) else Color.Gray,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // MONTHLY LOG / REVENUE STATEMENT
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DeepSeaNavy.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Status Operasional Bulan Lalu", color = SoftCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val isAccident = ship.lastMonthAccidentOccurred
                                    Icon(
                                        imageVector = if (isAccident) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isAccident) Color.Red else Color.Green,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isAccident) "Incident Gagal" else "Berlayar Lancar",
                                        color = if (isAccident) Color.Red else Color.Green,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(ship.lastMonthAccidentReport ?: "Pelayaran normal, tidak ada isu mesin.", color = Color.LightGray, fontSize = 11.sp)
                            
                            if (!ship.lastMonthAccidentOccurred && ship.lastMonthPassengers > 0) {
                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = CoralBlue.copy(alpha = 0.3f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Jumlah Pax", color = Color.Gray, fontSize = 11.sp)
                                    Text("${ship.lastMonthPassengers} Penumpang", color = Color.White, fontSize = 11.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Karcis Tiket", color = Color.Gray, fontSize = 11.sp)
                                    Text(formatCurrencyRingkas(ship.lastMonthTicketRevenue, useShortFormat), color = Color.White, fontSize = 11.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Jajan Penumpang (Onboard)", color = Color.Gray, fontSize = 11.sp)
                                    Text(formatCurrencyRingkas(ship.lastMonthOnboardRevenue, useShortFormat), color = Color.White, fontSize = 11.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Biaya Fuel & Maint", color = Color.Gray, fontSize = 11.sp)
                                    Text("-" + formatCurrencyRingkas(ship.lastMonthExpenses, useShortFormat), color = Color(0xFFFF5252), fontSize = 11.sp)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Laba Bersih Kapal", color = SoftCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    val earn = (ship.lastMonthTicketRevenue + ship.lastMonthOnboardRevenue) - ship.lastMonthExpenses
                                    Text(
                                        text = (if (earn >= 0) "+" else "") + formatCurrencyRingkas(earn, useShortFormat),
                                        color = if (earn >= 0) Color(0xFF00FF66) else Color.Red,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ACTION BUTTON BAR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onManageFacilitiesClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("facilities_${ship.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralBlue, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fasilitas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDrydockClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("drydock_${ship.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEAB308), contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Drydock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onScrapClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("scrap_${ship.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scrap (15%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// HELPER CURRENCY FUNCTION
fun formatCurrency(amount: Long): String {
    val format = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US)
    format.minimumFractionDigits = 0
    format.maximumFractionDigits = 0
    return format.format(amount)
}

fun formatCurrencyRingkas(amount: Long, useShortFormat: Boolean): String {
    if (!useShortFormat) {
        return formatCurrency(amount)
    }
    val absAmount = kotlin.math.abs(amount)
    val formatted = when {
        absAmount >= 1_000_000_000_000L -> String.format(java.util.Locale.US, "%.2f T", absAmount.toDouble() / 1_000_000_000_000L)
        absAmount >= 1_000_000_000L -> String.format(java.util.Locale.US, "%.2f B", absAmount.toDouble() / 1_000_000_000L)
        absAmount >= 1_000_000L -> String.format(java.util.Locale.US, "%.2f M", absAmount.toDouble() / 1_000_000L)
        absAmount >= 1_000L -> String.format(java.util.Locale.US, "%.1f K", absAmount.toDouble() / 1_000L)
        else -> "$absAmount"
    }
    return if (amount < 0) "-\$$formatted" else "\$$formatted"
}
