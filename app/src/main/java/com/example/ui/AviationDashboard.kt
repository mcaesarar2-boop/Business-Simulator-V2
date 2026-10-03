package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.data.*
import com.example.viewmodel.GameViewModel

// Color Palette Constants for Aviation UI
private val AviationDarkBg = Color(0xFF0C0E14)
private val AviationCardBg = Color(0xFF161A24)
private val AviationCardBorder = Color(0xFF262C3D)
private val AviationSurfaceHeader = Color(0xFF12151F)
private val AviationGold = Color(0xFFFFD700)
private val AviationEmerald = Color(0xFF00E676)
private val AviationAmber = Color(0xFFFFB300)
private val AviationCrimson = Color(0xFFFF5252)
private val AviationCyan = Color(0xFF00E5FF)
private val AviationBlue = Color(0xFF2979FF)
private val AviationMutedGray = Color(0xFF90A4AE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AviationDashboardScreen(
    navController: NavHostController,
    viewModel: GameViewModel,
    instanceId: String
) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()

    val ownedData = playerState.ownedBusinesses.find { it.instanceId == instanceId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }

    if (ownedData == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AviationDarkBg),
            contentAlignment = Alignment.Center
        ) {
            Text("Data Maskapai tidak ditemukan.", color = Color.White, fontSize = 18.sp)
        }
        return
    }

    // Tab state: 0 = UTAMA, 1 = FLEET, 2 = HUBS, 3 = ROUTES
    var selectedTab by remember { mutableStateOf(0) }

    // Dialog flags
    var showAddRouteDialog by remember { mutableStateOf(false) }
    var showHubUpgradesDialog by remember { mutableStateOf(false) }
    var selectedHubForUpgrades by remember { mutableStateOf<AviationHub?>(null) }
    var showLiquidationDialog by remember { mutableStateOf(false) }
    var showLiquidationConfirmSecondDialog by remember { mutableStateOf(false) }

    // Route edit modal state
    var selectedRouteForEdit by remember { mutableStateOf<FlightRoute?>(null) }

    // Transfer Kas states
    var showTransferDialog by remember { mutableStateOf(false) }
    var isDeposit by remember { mutableStateOf(false) }
    var transferAmount by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        containerColor = AviationDarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = ownedData.customName ?: "Aviation Group",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AviationEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Aviation Command Center",
                                fontSize = 11.sp,
                                color = AviationGold,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AviationSurfaceHeader,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dashboard Balance and Main KPI Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = AviationSurfaceHeader),
                shape = RoundedCornerShape(0.dp),
                border = BorderStroke(1.dp, AviationCardBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SALDO DIVISI MASKAPAI",
                                fontSize = 10.sp,
                                color = AviationMutedGray,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = formatCurrencyRingkas(ownedData.companyCash.toLong(), useShortFormat),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = AviationGold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "STATUS OPERASI",
                                fontSize = 10.sp,
                                color = AviationMutedGray,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            val hubCount = ownedData.airlineHubsComplex.size
                            val fleetCount = ownedData.airlineFleetComplex.count { it.status != "DELIVERING" }
                            Text(
                                text = if (hubCount == 0) "Belum Punya Hub" else "$hubCount Hub • $fleetCount Armada Siap",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hubCount == 0) AviationCrimson else AviationEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tarik Kas Button (Cohesive Muted Outline Style)
                        OutlinedButton(
                            onClick = {
                                isDeposit = false
                                showTransferDialog = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AviationCrimson
                            ),
                            border = BorderStroke(1.dp, AviationCrimson.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Tarik Kas",
                                tint = AviationCrimson,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tarik Kas",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Suntik Kas Button (Cohesive Muted Outline Style)
                        OutlinedButton(
                            onClick = {
                                isDeposit = true
                                showTransferDialog = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AviationEmerald
                            ),
                            border = BorderStroke(1.dp, AviationEmerald.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Suntik Kas",
                                tint = AviationEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Suntik Kas",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Material 3 Navigation Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AviationSurfaceHeader,
                contentColor = AviationGold,
                divider = { HorizontalDivider(color = AviationCardBorder) }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Utama", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Fleet (${ownedData.airlineFleetComplex.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Flight, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Hubs (${ownedData.airlineHubsComplex.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Routes (${ownedData.flightRoutes.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Tab contents
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                when (selectedTab) {
                    0 -> OverviewTabContent(
                        ownedData = ownedData,
                        useShortFormat = useShortFormat,
                        onRentHubClick = { navController.navigate("aviation_hub_catalog/$instanceId") },
                        onLiquidateClick = { showLiquidationDialog = true }
                    )
                    1 -> FleetTabContent(
                        ownedData = ownedData,
                        playerCash = playerState.cash,
                        useShortFormat = useShortFormat,
                        onBuyAircraftClick = { navController.navigate("aviation_catalog_screen/$instanceId") },
                        onRepairAircraft = { pl ->
                            viewModel.updateBusiness(instanceId, cost = 2_000_000L) { biz ->
                                val upd = biz.airlineFleetComplex.map { plane ->
                                    if (plane.id == pl.id) plane.copy(condition = 100.0) else plane
                                }
                                biz.copy(airlineFleetComplex = upd)
                            }
                        },
                        onAssignHub = { plane, hubId ->
                            viewModel.assignAircraftToHubComplex(instanceId, plane.id, hubId)
                        }
                    )
                    2 -> HubsTabContent(
                        ownedData = ownedData,
                        playerCash = playerState.cash,
                        useShortFormat = useShortFormat,
                        onRentHubClick = { navController.navigate("aviation_hub_catalog/$instanceId") },
                        onOpenUpgrade = { hub ->
                            selectedHubForUpgrades = hub
                            showHubUpgradesDialog = true
                        }
                    )
                    3 -> RoutesTabContent(
                        ownedData = ownedData,
                        useShortFormat = useShortFormat,
                        onCreateRouteClick = { showAddRouteDialog = true },
                        onDeleteRoute = { routeId ->
                            viewModel.deleteFlightRouteComplex(instanceId, routeId)
                        },
                        onEditRoute = { route ->
                            selectedRouteForEdit = route
                        }
                    )
                }
            }
        }
    }

    // Modal Edit Route Dialog
    if (selectedRouteForEdit != null) {
        val currentRoute = ownedData.flightRoutes.find { it.id == selectedRouteForEdit!!.id }
        if (currentRoute == null) {
            selectedRouteForEdit = null
        } else {
            EditRouteDialog(
                route = currentRoute,
                ownedData = ownedData,
                onDismiss = { selectedRouteForEdit = null },
                onAssignPlane = { planeId ->
                    viewModel.assignAircraftToRouteComplex(instanceId, planeId, currentRoute.id)
                },
                onUnassignPlane = { planeId ->
                    viewModel.assignAircraftToRouteComplex(instanceId, planeId, null)
                }
            )
        }
    }

    // Liquidation Confirm Dialogs
    if (showLiquidationDialog) {
        AlertDialog(
            onDismissRequest = { showLiquidationDialog = false },
            containerColor = AviationCardBg,
            title = { Text("Likuidasi Maskapai?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Apakah Anda yakin ingin melikuidasi unit bisnis ini? Semua armada dan hub yang beroperasi akan ditutup, dan Anda menerima nilai pemulihan sebesar 70% dari aset perusahaan.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLiquidationDialog = false
                        showLiquidationConfirmSecondDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AviationCrimson),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Lanjut", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLiquidationDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    if (showLiquidationConfirmSecondDialog) {
        AlertDialog(
            onDismissRequest = { showLiquidationConfirmSecondDialog = false },
            containerColor = AviationCardBg,
            title = { Text("Konfirmasi Akhir", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Peringatan: Keputusan ini tidak dapat dibatalkan! Yakinkah Anda mau melikuidasi keseluruhan divisi maskapai ini sekarang?",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLiquidationConfirmSecondDialog = false
                        viewModel.liquidateBusiness(instanceId)
                        android.widget.Toast.makeText(context, "Bisnis maskapai telah dilikuidasi!", android.widget.Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("YA, TUTUP USAHA", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLiquidationConfirmSecondDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // Dialog Transfer Kas (Suntik / Tarik Dana)
    if (showTransferDialog) {
        val titleText = if (isDeposit) "Suntik Dana Divisi" else "Tarik Dana Divisi"
        val descText = if (isDeposit) {
            val holdingParent = playerState.holdingCompanies.find { it.instanceId == ownedData.parentId }
            if (holdingParent != null) {
                "Tarik uang dari kas induk (${holdingParent.name} - Saldo: ${formatCurrencyRingkas(holdingParent.holdingCash, useShortFormat)}) ke divisi maskapai ini."
            } else {
                "Tarik uang dari dompet pribadi (Saldo: ${formatCurrencyRingkas(playerState.cash, useShortFormat)}) ke divisi maskapai ini."
            }
        } else {
            val holdingParent = playerState.holdingCompanies.find { it.instanceId == ownedData.parentId }
            if (holdingParent != null) {
                "Kirim dividen dari divisi maskapai ini ke kas induk (${holdingParent.name}).\nSisa Kas Divisi: ${formatCurrencyRingkas(ownedData.companyCash.toLong(), useShortFormat)}"
            } else {
                "Kirim dividen dari divisi maskapai ini ke dompet pribadi.\nSisa Kas Divisi: ${formatCurrencyRingkas(ownedData.companyCash.toLong(), useShortFormat)}"
            }
        }

        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text(titleText, color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(descText, color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = transferAmount,
                        onValueChange = { transferAmount = it },
                        label = { Text("Jumlah Uang ($)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AviationGold,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountLong = transferAmount.toLongOrNull()
                        if (amountLong != null && amountLong > 0) {
                            if (isDeposit) {
                                val holdingParent = playerState.holdingCompanies.find { it.instanceId == ownedData.parentId }
                                val maxAvailable = holdingParent?.holdingCash?.toLong() ?: playerState.cash
                                if (amountLong <= maxAvailable) {
                                    val success = viewModel.injectCapitalToBusiness(instanceId, amountLong)
                                    if (success) {
                                        showTransferDialog = false
                                        transferAmount = ""
                                        android.widget.Toast.makeText(context, "Suntik Dana Berhasil!", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Suntik Dana Gagal. Silakan coba lagi.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "Dana Tidak Cukup", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (amountLong <= ownedData.companyCash) {
                                    val success = viewModel.withdrawCapitalFromBusiness(instanceId, amountLong)
                                    if (success) {
                                        showTransferDialog = false
                                        transferAmount = ""
                                        android.widget.Toast.makeText(context, "Tarik Dana Berhasil!", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Tarik Dana Gagal. Silakan coba lagi.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    android.widget.Toast.makeText(context, "Kas Divisi Tidak Cukup", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            android.widget.Toast.makeText(context, "Nominal tidak valid", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDeposit) AviationEmerald else AviationCrimson),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isDeposit) "Suntik Dana" else "Tarik Dana", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false; transferAmount = "" }) {
                    Text("Batal", color = Color.LightGray)
                }
            },
            containerColor = AviationCardBg
        )
    }

    // DIALOG: Hub Upgrades
    if (showHubUpgradesDialog && selectedHubForUpgrades != null) {
        val hub = selectedHubForUpgrades!!
        AlertDialog(
            onDismissRequest = { showHubUpgradesDialog = false },
            containerColor = AviationCardBg,
            title = { Text("Fasilitas & Upgrade Hub", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        Text(
                            text = "Hub: ${hub.city}",
                            color = AviationGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    items(DUMMY_HUB_UPGRADES) { upg ->
                        val isConstructing = hub.constructionQueue.any { it.upgradeId == upg.id }
                        val isOwned = hub.activeUpgrades.contains(upg.id)
                        val canAfford = playerState.cash >= upg.cost

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF222938))
                                .border(1.dp, Color(0xFF333D52), RoundedCornerShape(8.dp))
                                .clickable(enabled = !isConstructing && !isOwned && canAfford) {
                                    viewModel.startHubUpgradeComplex(instanceId, hub.id, upg.id, upg.cost, upg.buildTime)
                                    showHubUpgradesDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(upg.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Konstruksi: ${upg.buildTime} bulan", color = Color.LightGray, fontSize = 11.sp)
                                if (isOwned) {
                                    Text("STATUS: AKTIF", color = AviationEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else if (isConstructing) {
                                    val itemQ = hub.constructionQueue.find { it.upgradeId == upg.id }
                                    Text("STATUS: DIBANGUN (${itemQ?.monthsRemaining} bln)", color = AviationAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (!isOwned && !isConstructing) {
                                Text(
                                    text = formatCurrencyRingkas(upg.cost, useShortFormat),
                                    color = AviationGold,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showHubUpgradesDialog = false }) {
                    Text("Tutup", color = Color.LightGray)
                }
            }
        )
    }

    // DIALOG: Create Flight Route (Revamped with Tabbed Selector, Smart Pricing & Route Preview)
    if (showAddRouteDialog) {
        CreateFlightRouteDialog(
            ownedData = ownedData,
            onDismiss = { showAddRouteDialog = false },
            onCreateRoute = { originHubId, destination, distanceCategory, demand, ticketPrice ->
                viewModel.createFlightRouteComplex(
                    businessId = instanceId,
                    originHubId = originHubId,
                    destination = destination,
                    distanceCategory = distanceCategory,
                    demand = demand,
                    ticketPrice = ticketPrice
                )
                showAddRouteDialog = false
            }
        )
    }
}

// -------------------------------------------------------------
// DIALOG COMPONENT: Create Flight Route (Tabbed, Search, Smart Pricing, Visual Arc)
// -------------------------------------------------------------
@Composable
fun CreateFlightRouteDialog(
    ownedData: OwnedBusiness,
    onDismiss: () -> Unit,
    onCreateRoute: (originHubId: String, destination: String, distanceCategory: String, demand: Int, ticketPrice: Int) -> Unit
) {
    var selectedHubIndex by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Domestik, 1 = Internasional
    var searchQuery by remember { mutableStateOf("") }

    val allRoutes = FlightRouteCatalogue.ROUTES
    val domesticRoutes = remember { allRoutes.filter { it.type == "domestik" } }
    val internationalRoutes = remember { allRoutes.filter { it.type == "internasional" } }

    val currentTabRoutes = if (selectedTab == 0) domesticRoutes else internationalRoutes
    val filteredRoutes = remember(searchQuery, selectedTab) {
        if (searchQuery.isBlank()) {
            currentTabRoutes
        } else {
            val q = searchQuery.trim().lowercase()
            currentTabRoutes.filter {
                it.dest.lowercase().contains(q) ||
                it.id.lowercase().contains(q) ||
                it.airportCode.lowercase().contains(q) ||
                it.origin.lowercase().contains(q)
            }
        }
    }

    var selectedDestination by remember {
        mutableStateOf(domesticRoutes.firstOrNull())
    }

    var inputTicketPrice by remember(selectedDestination) {
        mutableStateOf(selectedDestination?.medianPrice?.toString() ?: "100")
    }

    val priceInt = inputTicketPrice.toIntOrNull()
    val minPrice = selectedDestination?.minPrice ?: 20
    val maxPrice = selectedDestination?.maxPrice ?: 500
    val isTooLow = priceInt != null && priceInt < minPrice
    val isTooHigh = priceInt != null && priceInt > maxPrice
    val isPriceValid = priceInt != null && priceInt in minPrice..maxPrice

    val estimatedDemand = if (isPriceValid && selectedDestination != null && priceInt != null) {
        FlightRouteCatalogue.calculateEstimatedDemand(1000, priceInt, minPrice, maxPrice)
    } else 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = AviationDarkBg),
            border = BorderStroke(1.5.dp, AviationGold.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header with Title & Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AviationBlue.copy(alpha = 0.2f))
                                .border(1.dp, AviationBlue, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = AviationCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Buka Rute Penerbangan Baru",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Ekspansi jaringan & tetapkan harga tiket cerdas",
                                color = AviationMutedGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (ownedData.airlineHubsComplex.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(Icons.Default.LocationOff, contentDescription = null, tint = AviationCrimson, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Belum Ada Bandara Hub Terbuka!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Anda harus menyewa Hub bandara operasional di tab 'Hub' sebelum dapat membentangkan rute penerbangan.",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Hub Asal Selector
                        val currentHub = ownedData.airlineHubsComplex.getOrNull(selectedHubIndex)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AviationCardBg)
                                .border(1.dp, AviationCardBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Pangkalan Hub Asal:", color = AviationMutedGray, fontSize = 10.sp)
                                Text(
                                    text = currentHub?.city ?: "Jakarta (CGK)",
                                    color = AviationGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            if (ownedData.airlineHubsComplex.size > 1) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            selectedHubIndex = if (selectedHubIndex > 0) selectedHubIndex - 1 else ownedData.airlineHubsComplex.size - 1
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Prev Hub", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                    Text(
                                        "${selectedHubIndex + 1}/${ownedData.airlineHubsComplex.size}",
                                        color = Color.LightGray,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    IconButton(
                                        onClick = {
                                            selectedHubIndex = if (selectedHubIndex < ownedData.airlineHubsComplex.size - 1) selectedHubIndex + 1 else 0
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next Hub", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(AviationEmerald.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("HUB UTAMA", color = AviationEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // TUGAS 1: TAB TOGGLE (Domestik vs Internasional)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AviationCardBg)
                                .border(1.dp, AviationCardBorder, RoundedCornerShape(10.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val isDom = selectedTab == 0
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDom) AviationBlue else Color.Transparent)
                                    .clickable {
                                        selectedTab = 0
                                        selectedDestination = domesticRoutes.firstOrNull()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🇮🇩 Domestik (${domesticRoutes.size})",
                                    color = if (isDom) Color.White else Color.LightGray,
                                    fontWeight = if (isDom) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }

                            val isIntl = selectedTab == 1
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isIntl) AviationBlue else Color.Transparent)
                                    .clickable {
                                        selectedTab = 1
                                        selectedDestination = internationalRoutes.firstOrNull()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🌐 Internasional (${internationalRoutes.size})",
                                    color = if (isIntl) Color.White else Color.LightGray,
                                    fontWeight = if (isIntl) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // TUGAS 1: SEARCH BAR KECIL
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text("Cari kota / bandara (mis: Bali, DPS, Tokyo, LHR)...", color = AviationMutedGray, fontSize = 12.sp)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = AviationMutedGray, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(10.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = AviationCardBg,
                                unfocusedContainerColor = AviationCardBg,
                                focusedBorderColor = AviationCyan,
                                unfocusedBorderColor = AviationCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // DESTINATION LIST / SELECTOR
                        Text(
                            text = "Pilih Destinasi Tujuan (${filteredRoutes.size} tersedia):",
                            color = AviationMutedGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (filteredRoutes.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AviationCardBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Tidak ditemukan kota dengan kata kunci \"$searchQuery\"",
                                    color = AviationMutedGray,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            // Compact vertical selector with 140dp height
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF11141D))
                                    .border(1.dp, AviationCardBorder, RoundedCornerShape(10.dp))
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(filteredRoutes, key = { it.id }) { dest ->
                                        val isSelected = selectedDestination?.id == dest.id
                                        val categoryColor = when (dest.distanceCategory) {
                                            "SHORT" -> AviationCyan
                                            "MEDIUM" -> AviationAmber
                                            else -> Color(0xFFE040FB)
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) AviationBlue.copy(alpha = 0.25f) else AviationCardBg)
                                                .border(
                                                    width = if (isSelected) 1.5.dp else 1.dp,
                                                    color = if (isSelected) AviationCyan else Color.Transparent,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    selectedDestination = dest
                                                    inputTicketPrice = dest.medianPrice.toString()
                                                }
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(dest.flag, fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = dest.dest,
                                                        color = if (isSelected) Color.White else Color.LightGray,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 13.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "${dest.distance} km • Rekom: $${dest.minPrice}-$${dest.maxPrice}",
                                                        color = AviationMutedGray,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(categoryColor.copy(alpha = 0.15f))
                                                        .border(1.dp, categoryColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = dest.distanceCategory,
                                                        color = categoryColor,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                if (isSelected) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = "Selected",
                                                        tint = AviationCyan,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ROUTE VISUAL PREVIEW BANNER (Flight Radar Arc)
                        if (selectedDestination != null) {
                            val dest = selectedDestination!!
                            val originName = currentHub?.city ?: "Jakarta (CGK)"
                            val estHours = String.format(java.util.Locale.US, "%.1f", (dest.distance / 750.0).coerceAtLeast(0.5))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131926)),
                                border = BorderStroke(1.dp, AviationBlue.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🛰️ SIMULASI KORIDOR UDARA",
                                            color = AviationCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "${dest.distance} KM • ~${estHours} Jam Terbang",
                                            color = Color.LightGray,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.Start) {
                                            Text(originName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("ORIGIN", color = AviationMutedGray, fontSize = 9.sp)
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        ) {
                                            Text("──✈️──", color = AviationGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(dest.flag, fontSize = 12.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(dest.dest, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                            Text("DESTINATION", color = AviationMutedGray, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // TUGAS 2: SMART PRICING UX
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AviationCardBg)
                                    .border(1.dp, AviationCardBorder, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Harga Tiket per Pax (USD):",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )

                                    // Auto Price Button (Median)
                                    OutlinedButton(
                                        onClick = {
                                            inputTicketPrice = dest.medianPrice.toString()
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AviationGold),
                                        border = BorderStroke(1.dp, AviationGold.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Tune, contentDescription = null, tint = AviationGold, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Set Auto-Price ($${dest.medianPrice})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val borderCol = when {
                                    !isPriceValid -> AviationCrimson
                                    else -> AviationEmerald
                                }

                                OutlinedTextField(
                                    value = inputTicketPrice,
                                    onValueChange = { inputTicketPrice = it.filter { ch -> ch.isDigit() } },
                                    prefix = {
                                        Text("$ ", color = AviationGold, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF10131B),
                                        unfocusedContainerColor = Color(0xFF10131B),
                                        focusedBorderColor = borderCol,
                                        unfocusedBorderColor = borderCol,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Helper text & dynamic validation alerts
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Rekomendasi Harga: $${dest.minPrice} - $${dest.maxPrice}",
                                        color = AviationMutedGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Median: $${dest.medianPrice}",
                                        color = AviationGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Warning state or Demand Elasticity preview
                                if (isTooLow) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AviationCrimson.copy(alpha = 0.15f))
                                            .border(1.dp, AviationCrimson.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "⚠️ Harga terlalu rendah di bawah $${dest.minPrice}! Anda berisiko merugi karena biaya avtur & operasional tidak tertutup.",
                                            color = AviationCrimson,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                } else if (isTooHigh) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AviationCrimson.copy(alpha = 0.15f))
                                            .border(1.dp, AviationCrimson.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "⚠️ Harga melebihi batas rasional ($${dest.maxPrice} max)! Penumpang akan menolak terbang dan Demand menjadi 0.",
                                            color = AviationCrimson,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                } else if (isPriceValid) {
                                    val marketPct = ((estimatedDemand.toFloat() / 1000f) * 100).toInt()
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AviationEmerald.copy(alpha = 0.12f))
                                            .border(1.dp, AviationEmerald.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "🔥 Estimasi Demand: ~${estimatedDemand} Pax/bln",
                                                color = AviationEmerald,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Serapan Pasar: ${marketPct}%",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ACTION BUTTONS (Confirm / Dismiss)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Text("Batal", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        val canSubmit = isPriceValid && selectedDestination != null && ownedData.airlineHubsComplex.isNotEmpty()
                        Button(
                            onClick = {
                                val currentHub = ownedData.airlineHubsComplex.getOrNull(selectedHubIndex) ?: return@Button
                                val dest = selectedDestination ?: return@Button
                                val price = priceInt ?: dest.medianPrice
                                val demand = estimatedDemand.coerceAtLeast(150)
                                onCreateRoute(currentHub.id, dest.dest, dest.distanceCategory, demand, price)
                            },
                            enabled = canSubmit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AviationEmerald,
                                disabledContainerColor = Color(0xFF232A38)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(46.dp)
                        ) {
                            Icon(
                                Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = if (canSubmit) Color.Black else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Buka Rute",
                                color = if (canSubmit) Color.Black else Color.Gray,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMPONENT: Condition Progress Bar (Horizontal with color indicator)
// -------------------------------------------------------------
@Composable
fun ConditionProgressBar(
    condition: Double,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val clamped = condition.coerceIn(0.0, 100.0).toFloat()
    val progress = clamped / 100f

    val barColor = when {
        clamped >= 50f -> AviationEmerald
        clamped >= 20f -> AviationAmber
        else -> AviationCrimson
    }

    val trackColor = barColor.copy(alpha = 0.2f)

    Column(modifier = modifier) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kondisi Lambung",
                    fontSize = 11.sp,
                    color = AviationMutedGray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${clamped.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = barColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}

// -------------------------------------------------------------
// COMPONENT: Modern Status Badge
// -------------------------------------------------------------
@Composable
fun ModernStatusBadge(
    status: String,
    monthsUntilDelivery: Int = 0,
    isLeased: Boolean = false
) {
    val (bg, text, border, label) = when {
        status == "DELIVERING" -> Quad(
            AviationAmber.copy(alpha = 0.18f),
            AviationAmber,
            AviationAmber.copy(alpha = 0.35f),
            "INDEN (${monthsUntilDelivery} BLN)"
        )
        status == "ASSIGNED" -> Quad(
            AviationEmerald.copy(alpha = 0.18f),
            AviationEmerald,
            AviationEmerald.copy(alpha = 0.35f),
            "AKTIF RUTE"
        )
        else -> Quad(
            AviationCyan.copy(alpha = 0.15f),
            AviationCyan,
            AviationCyan.copy(alpha = 0.35f),
            "STANDBY"
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        if (isLeased) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFAB47BC).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFFAB47BC).copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "SEWA",
                    color = Color(0xFFE1BEE7),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(bg)
                .border(1.dp, border, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = label,
                color = text,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// -------------------------------------------------------------
// COMPONENT: Modern Category Badge
// -------------------------------------------------------------
@Composable
fun AircraftCategoryBadge(type: String) {
    val (color, name) = when (type.uppercase()) {
        "PROPELLER" -> Pair(Color(0xFF81C784), "PROPELLER")
        "REGIONAL_JET", "SHORT_MEDIUM" -> Pair(Color(0xFF64B5F6), "REGIONAL JET")
        "NARROW_BODY" -> Pair(Color(0xFFFFB74D), "NARROW BODY")
        "WIDE_BODY" -> Pair(Color(0xFFBA68C8), "WIDE BODY")
        "HELICOPTER" -> Pair(Color(0xFF4DB6AC), "HELICOPTER")
        else -> Pair(AviationMutedGray, type)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = name,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// -------------------------------------------------------------
// COMPONENT: Segmented View Switch (List vs Grid)
// -------------------------------------------------------------
@Composable
fun ViewModeToggle(
    isGrid: Boolean,
    onModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E2330))
            .border(1.dp, AviationCardBorder, RoundedCornerShape(8.dp))
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (!isGrid) AviationGold.copy(alpha = 0.25f) else Color.Transparent)
                .clickable { onModeChange(false) }
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "List View",
                tint = if (!isGrid) AviationGold else AviationMutedGray,
                modifier = Modifier.size(16.dp)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isGrid) AviationGold.copy(alpha = 0.25f) else Color.Transparent)
                .clickable { onModeChange(true) }
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "Grid View",
                tint = if (isGrid) AviationGold else AviationMutedGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// TAB 0: OVERVIEW TAB CONTENT
// -------------------------------------------------------------
@Composable
fun OverviewTabContent(
    ownedData: OwnedBusiness,
    useShortFormat: Boolean,
    onRentHubClick: () -> Unit,
    onLiquidateClick: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        if (ownedData.airlineHubsComplex.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF261D22)),
                    border = BorderStroke(1.dp, AviationCrimson.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AviationCrimson,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Menunggu Pembukaan Hub Pertama",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Untuk melayani penumpang dan memperoleh pendapatan rute, Anda harus membuka setidaknya satu pusat penerbangan (Hub) pertama di tab Hub.",
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRentHubClick,
                            colors = ButtonDefaults.buttonColors(containerColor = AviationBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Buka Hub Sekarang", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "Performa Finansial & Operasional",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AviationCardBg),
                    border = BorderStroke(1.dp, AviationCardBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val monthlyRev = ownedData.customRevenue ?: 0L
                        val totalExp = ownedData.calculateTotalExpenses()
                        val netProfit = monthlyRev - totalExp
                        val activePlanes = ownedData.airlineFleetComplex.count { it.status != "DELIVERING" }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estimasi Pendapatan Tiket", color = AviationMutedGray, fontSize = 13.sp)
                            Text(
                                text = "+ ${formatCurrencyRingkas(monthlyRev, useShortFormat)} /bln",
                                color = AviationEmerald,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        val expenseLabel = if (activePlanes == 0 && ownedData.airlineHubsComplex.isNotEmpty()) {
                            "Biaya Sewa Hub (Idle)"
                        } else {
                            "Biaya Operasional & Upkeep Hub"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(expenseLabel, color = AviationMutedGray, fontSize = 13.sp)
                            Text(
                                text = "- ${formatCurrencyRingkas(totalExp, useShortFormat)} /bln",
                                color = AviationCrimson,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        HorizontalDivider(color = AviationCardBorder, modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estimasi Net Cashflow", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val profitColor = if (netProfit >= 0) AviationEmerald else AviationCrimson
                            val profitSign = if (netProfit >= 0) "+" else "-"
                            Text(
                                text = "$profitSign ${formatCurrencyRingkas(kotlin.math.abs(netProfit), useShortFormat)} /bln",
                                color = profitColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick stats cards
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = AviationCardBg),
                        border = BorderStroke(1.dp, AviationCardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("TOTAL ARMADA", fontSize = 10.sp, color = AviationMutedGray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${ownedData.airlineFleetComplex.size} Unit",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = AviationCardBg),
                        border = BorderStroke(1.dp, AviationCardBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("RUTE TERBANG", fontSize = 10.sp, color = AviationMutedGray, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${ownedData.flightRoutes.size} Rute",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = AviationGold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onLiquidateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AviationCrimson
                ),
                border = BorderStroke(1.dp, AviationCrimson.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Likuidasi Maskapai (Tutup Usaha)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: FLEET TAB CONTENT (LIST / GRID & SORT/FILTER)
// -------------------------------------------------------------
@Composable
fun FleetTabContent(
    ownedData: OwnedBusiness,
    playerCash: Long,
    useShortFormat: Boolean,
    onBuyAircraftClick: () -> Unit,
    onRepairAircraft: (AircraftInstance) -> Unit,
    onAssignHub: (AircraftInstance, String?) -> Unit
) {
    var isGridView by remember { mutableStateOf(false) }

    // Filter states
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedConditionSort by remember { mutableStateOf("DEFAULT") } // DEFAULT, BEST, WORST

    // Computed filtered & sorted fleet
    val fleetList = remember(ownedData.airlineFleetComplex, selectedCategoryFilter, selectedStatusFilter, selectedConditionSort) {
        var list = ownedData.airlineFleetComplex

        // Filter Category
        if (selectedCategoryFilter != "ALL") {
            list = list.filter { pl ->
                val model = AVIATION_AIRCRAFT_CATALOG.find { it.id == pl.modelId }
                    ?: DUMMY_AIRCRAFTS.find { it.id == pl.modelId }
                model?.type == selectedCategoryFilter
            }
        }

        // Filter Status
        if (selectedStatusFilter != "ALL") {
            list = when (selectedStatusFilter) {
                "ASSIGNED" -> list.filter { it.status == "ASSIGNED" }
                "STANDBY" -> list.filter { it.status == "STANDBY" }
                "DELIVERING" -> list.filter { it.status == "DELIVERING" }
                else -> list
            }
        }

        // Sort Condition
        list = when (selectedConditionSort) {
            "BEST" -> list.sortedByDescending { it.condition }
            "WORST" -> list.sortedBy { it.condition }
            else -> list
        }

        list
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Fleet Header with Action & Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Armada Saya (${ownedData.airlineFleetComplex.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${fleetList.size} unit ditampilkan",
                    fontSize = 11.sp,
                    color = AviationMutedGray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewModeToggle(
                    isGrid = isGridView,
                    onModeChange = { isGridView = it }
                )

                Button(
                    onClick = onBuyAircraftClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AviationGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Beli Pesawat", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Sort & Filter Bar
        FleetFilterAndSortBar(
            selectedCategory = selectedCategoryFilter,
            onCategoryChange = { selectedCategoryFilter = it },
            selectedStatus = selectedStatusFilter,
            onStatusChange = { selectedStatusFilter = it },
            selectedCondition = selectedConditionSort,
            onConditionChange = { selectedConditionSort = it }
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Content
        if (fleetList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Flight,
                        contentDescription = null,
                        tint = AviationMutedGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (ownedData.airlineFleetComplex.isEmpty()) "Belum ada pesawat terdaftar. Silakan pilih 'Beli Pesawat' di atas." else "Tidak ada armada yang sesuai filter saat ini.",
                        color = AviationMutedGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            if (isGridView) {
                // Grid View Layout (2 Columns)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(fleetList, key = { it.id }) { pl ->
                        FleetGridCard(
                            plane = pl,
                            ownedData = ownedData,
                            playerCash = playerCash,
                            onRepairAircraft = onRepairAircraft,
                            onAssignHub = onAssignHub
                        )
                    }
                }
            } else {
                // List View Layout
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(fleetList, key = { it.id }) { pl ->
                        FleetListCard(
                            plane = pl,
                            ownedData = ownedData,
                            playerCash = playerCash,
                            onRepairAircraft = onRepairAircraft,
                            onAssignHub = onAssignHub
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FleetFilterAndSortBar(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    selectedCondition: String,
    onConditionChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status Filter Chips
        FilterChipItem(
            label = "Semua Status",
            isSelected = selectedStatus == "ALL",
            onClick = { onStatusChange("ALL") }
        )
        FilterChipItem(
            label = "🟢 Aktif Rute",
            isSelected = selectedStatus == "ASSIGNED",
            onClick = { onStatusChange("ASSIGNED") }
        )
        FilterChipItem(
            label = "🔵 Standby",
            isSelected = selectedStatus == "STANDBY",
            onClick = { onStatusChange("STANDBY") }
        )
        FilterChipItem(
            label = "🟠 Inden",
            isSelected = selectedStatus == "DELIVERING",
            onClick = { onStatusChange("DELIVERING") }
        )

        VerticalDivider(modifier = Modifier.height(20.dp), color = AviationCardBorder)

        // Condition Sort Chips
        FilterChipItem(
            label = "Kondisi Terbaik ↓",
            isSelected = selectedCondition == "BEST",
            onClick = { onConditionChange(if (selectedCondition == "BEST") "DEFAULT" else "BEST") }
        )
        FilterChipItem(
            label = "Kondisi Terburuk ↑",
            isSelected = selectedCondition == "WORST",
            onClick = { onConditionChange(if (selectedCondition == "WORST") "DEFAULT" else "WORST") }
        )

        VerticalDivider(modifier = Modifier.height(20.dp), color = AviationCardBorder)

        // Category Filter Chips
        listOf(
            "PROPELLER" to "Propeller",
            "REGIONAL_JET" to "Regional Jet",
            "NARROW_BODY" to "Narrow Body",
            "WIDE_BODY" to "Wide Body",
            "HELICOPTER" to "Helicopter"
        ).forEach { (key, label) ->
            FilterChipItem(
                label = label,
                isSelected = selectedCategory == key,
                onClick = { onCategoryChange(if (selectedCategory == key) "ALL" else key) }
            )
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) AviationGold.copy(alpha = 0.2f) else Color(0xFF1E2330))
            .border(
                1.dp,
                if (isSelected) AviationGold else AviationCardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) AviationGold else Color.LightGray
        )
    }
}

// -------------------------------------------------------------
// FLEET GRID CARD
// -------------------------------------------------------------
@Composable
fun FleetGridCard(
    plane: AircraftInstance,
    ownedData: OwnedBusiness,
    playerCash: Long,
    onRepairAircraft: (AircraftInstance) -> Unit,
    onAssignHub: (AircraftInstance, String?) -> Unit
) {
    val modelInfo = AVIATION_AIRCRAFT_CATALOG.find { it.id == plane.modelId }
        ?: DUMMY_AIRCRAFTS.find { it.id == plane.modelId }

    val iconVector = when (modelInfo?.type) {
        "HELICOPTER" -> Icons.Default.Flight
        else -> Icons.Default.Flight
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = AviationCardBg),
        border = BorderStroke(1.dp, AviationCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header Row: Icon + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = AviationGold,
                    modifier = Modifier.size(18.dp)
                )
                ModernStatusBadge(
                    status = plane.status,
                    monthsUntilDelivery = plane.monthsUntilDelivery,
                    isLeased = plane.isLeased
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Model & Reg
            Text(
                text = modelInfo?.model ?: plane.modelId,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "PK-${plane.id.take(5).uppercase()}",
                color = AviationMutedGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Condition Bar
            ConditionProgressBar(
                condition = plane.condition,
                showLabel = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Stationed Hub
            val deployedHub = ownedData.airlineHubsComplex.find { it.id == plane.stationedHubId }
            Text(
                text = "📍 ${deployedHub?.city?.take(14) ?: "Belum Set Hub"}",
                color = Color.LightGray,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons in Grid
            if (plane.status != "DELIVERING") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (plane.condition < 100) {
                        OutlinedButton(
                            onClick = { onRepairAircraft(plane) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AviationEmerald),
                            border = BorderStroke(1.dp, AviationEmerald.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            enabled = playerCash >= 2_000_000L
                        ) {
                            Text("Rawat ($2M)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    var showHubDropdown by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showHubDropdown = !showHubDropdown },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AviationBlue),
                            border = BorderStroke(1.dp, AviationBlue.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Set Hub", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        DropdownMenu(
                            expanded = showHubDropdown,
                            onDismissRequest = { showHubDropdown = false },
                            modifier = Modifier.background(AviationCardBg)
                        ) {
                            ownedData.airlineHubsComplex.forEach { hub ->
                                DropdownMenuItem(
                                    text = { Text(hub.city, color = Color.White, fontSize = 12.sp) },
                                    onClick = {
                                        onAssignHub(plane, hub.id)
                                        showHubDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(AviationAmber.copy(alpha = 0.15f))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Antrian Pabrik", color = AviationAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// FLEET LIST CARD
// -------------------------------------------------------------
@Composable
fun FleetListCard(
    plane: AircraftInstance,
    ownedData: OwnedBusiness,
    playerCash: Long,
    onRepairAircraft: (AircraftInstance) -> Unit,
    onAssignHub: (AircraftInstance, String?) -> Unit
) {
    val modelInfo = AVIATION_AIRCRAFT_CATALOG.find { it.id == plane.modelId }
        ?: DUMMY_AIRCRAFTS.find { it.id == plane.modelId }

    Card(
        colors = CardDefaults.cardColors(containerColor = AviationCardBg),
        border = BorderStroke(1.dp, AviationCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = modelInfo?.model ?: plane.modelId,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PK-${plane.id.take(5).uppercase()}",
                            color = AviationMutedGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (modelInfo != null) {
                            AircraftCategoryBadge(type = modelInfo.type)
                        }
                    }
                }

                ModernStatusBadge(
                    status = plane.status,
                    monthsUntilDelivery = plane.monthsUntilDelivery,
                    isLeased = plane.isLeased
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats info & Condition Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConditionProgressBar(
                    condition = plane.condition,
                    modifier = Modifier.weight(1.2f)
                )

                Column(modifier = Modifier.weight(0.8f)) {
                    Text("Home Hub", color = AviationMutedGray, fontSize = 10.sp)
                    val deployedHub = ownedData.airlineHubsComplex.find { it.id == plane.stationedHubId }
                    Text(
                        text = deployedHub?.city ?: "Belum Ditugaskan",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            if (plane.status != "DELIVERING") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onRepairAircraft(plane) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AviationEmerald),
                        border = BorderStroke(1.dp, AviationEmerald.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        enabled = plane.condition < 100 && playerCash >= 2_000_000L,
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = AviationEmerald, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Perawatan ($2M)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (ownedData.airlineHubsComplex.isNotEmpty()) {
                        var showHubDropdown by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { showHubDropdown = !showHubDropdown },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AviationBlue),
                                border = BorderStroke(1.dp, AviationBlue.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AviationBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Set Home Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            DropdownMenu(
                                expanded = showHubDropdown,
                                onDismissRequest = { showHubDropdown = false },
                                modifier = Modifier.background(AviationCardBg)
                            ) {
                                ownedData.airlineHubsComplex.forEach { hub ->
                                    DropdownMenuItem(
                                        text = { Text(hub.city, color = Color.White) },
                                        onClick = {
                                            onAssignHub(plane, hub.id)
                                            showHubDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF261E14), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏭 Antrian Pabrik Manufaktur:",
                            color = AviationAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sisa ${plane.monthsUntilDelivery} Bulan",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val totalEstimate = when (modelInfo?.type) {
                        "PROPELLER" -> 6f
                        "REGIONAL_JET" -> 18f
                        "NARROW_BODY" -> 18f
                        "WIDE_BODY" -> 36f
                        "HELICOPTER" -> 12f
                        else -> 12f
                    }
                    val deliveredPct = ((totalEstimate - plane.monthsUntilDelivery) / totalEstimate).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { deliveredPct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AviationAmber,
                        trackColor = Color(0xFF3E3120)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: HUBS TAB CONTENT (LIST / GRID & CAPACITY INDICATOR)
// -------------------------------------------------------------
@Composable
fun HubsTabContent(
    ownedData: OwnedBusiness,
    playerCash: Long,
    useShortFormat: Boolean,
    onRentHubClick: () -> Unit,
    onOpenUpgrade: (AviationHub) -> Unit
) {
    var isGridView by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pusat Bandara Hub (${ownedData.airlineHubsComplex.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Pangkalan operasional armada",
                    fontSize = 11.sp,
                    color = AviationMutedGray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewModeToggle(
                    isGrid = isGridView,
                    onModeChange = { isGridView = it }
                )

                Button(
                    onClick = onRentHubClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AviationGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buka Hub Baru", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (ownedData.airlineHubsComplex.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AviationMutedGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum ada Hub terbuka. Klik 'Buka Hub Baru' untuk memulainya.",
                        color = AviationMutedGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(ownedData.airlineHubsComplex, key = { it.id }) { hub ->
                        HubGridCard(
                            hub = hub,
                            ownedData = ownedData,
                            useShortFormat = useShortFormat,
                            onOpenUpgrade = onOpenUpgrade
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(ownedData.airlineHubsComplex, key = { it.id }) { hub ->
                        HubListCard(
                            hub = hub,
                            ownedData = ownedData,
                            useShortFormat = useShortFormat,
                            onOpenUpgrade = onOpenUpgrade
                        )
                    }
                }
            }
        }
    }
}

// Visual Indicator for Hub Aircraft Capacity (Mini Bar Chart)
@Composable
fun HubCapacityIndicator(
    assignedCount: Int,
    maxSlots: Int,
    modifier: Modifier = Modifier
) {
    val ratio = (assignedCount.toFloat() / maxSlots.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
    val color = when {
        ratio >= 0.9f -> AviationCrimson
        ratio >= 0.6f -> AviationAmber
        else -> AviationCyan
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kapasitas Hanggar Hub",
                color = AviationMutedGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$assignedCount / $maxSlots Pesawat",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = ratio)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun HubGridCard(
    hub: AviationHub,
    ownedData: OwnedBusiness,
    useShortFormat: Boolean,
    onOpenUpgrade: (AviationHub) -> Unit
) {
    val assignedPlanesCount = ownedData.airlineFleetComplex.count { it.stationedHubId == hub.id }
    val hubDef = GLOBAL_AVIATION_HUBS.find { it.city.contains(hub.city) || hub.city.contains(it.city) }
    val maxSlots = hubDef?.maxSlots ?: 15

    Card(
        colors = CardDefaults.cardColors(containerColor = AviationCardBg),
        border = BorderStroke(1.dp, AviationCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = hub.city,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            HubCapacityIndicator(
                assignedCount = assignedPlanesCount,
                maxSlots = maxSlots
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Upgrades: ${hub.activeUpgrades.size} aktif",
                color = AviationMutedGray,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { onOpenUpgrade(hub) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AviationBlue),
                border = BorderStroke(1.dp, AviationBlue.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text("Upgrades", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HubListCard(
    hub: AviationHub,
    ownedData: OwnedBusiness,
    useShortFormat: Boolean,
    onOpenUpgrade: (AviationHub) -> Unit
) {
    val assignedPlanesCount = ownedData.airlineFleetComplex.count { it.stationedHubId == hub.id }
    val hubDef = GLOBAL_AVIATION_HUBS.find { it.city.contains(hub.city) || hub.city.contains(it.city) }
    val maxSlots = hubDef?.maxSlots ?: 15

    var hubUpkeep = 100000L
    hub.activeUpgrades.forEach { upgId ->
        val addCost = when (upgId) {
            "upg_dom" -> 50000L
            "upg_intl_1" -> 150000L
            "upg_vip" -> 100000L
            "upg_intl_2" -> 300000L
            "upg_cargo" -> 120000L
            else -> 50000L
        }
        hubUpkeep += addCost
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = AviationCardBg),
        border = BorderStroke(1.dp, AviationCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AviationGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = hub.city,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AviationCyan.copy(alpha = 0.15f))
                        .border(1.dp, AviationCyan.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$assignedPlanesCount Home",
                        color = AviationCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Capacity Indicator
            HubCapacityIndicator(
                assignedCount = assignedPlanesCount,
                maxSlots = maxSlots
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Active Upgrades Badges
            if (hub.activeUpgrades.isNotEmpty()) {
                Text("Fasilitas Aktif:", color = AviationMutedGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hub.activeUpgrades.forEach { upgId ->
                        val upgDef = DUMMY_HUB_UPGRADES.find { it.id == upgId }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AviationEmerald.copy(alpha = 0.15f))
                                .border(1.dp, AviationEmerald.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = upgDef?.name?.take(18) ?: upgId,
                                fontSize = 9.sp,
                                color = AviationEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Construction Queue
            if (hub.constructionQueue.isNotEmpty()) {
                Text("Dalam Konstruksi:", color = AviationMutedGray, fontSize = 11.sp)
                hub.constructionQueue.forEach { qi ->
                    val upgDef = DUMMY_HUB_UPGRADES.find { it.id == qi.upgradeId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(upgDef?.name ?: qi.upgradeId, color = AviationAmber, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("${qi.monthsRemaining} bulan", color = Color.LightGray, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            val upkeepLabel = if (assignedPlanesCount == 0) "Biaya Sewa Hub (Idle):" else "Biaya Upkeep Hub:"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(upkeepLabel, color = AviationMutedGray, fontSize = 12.sp)
                Text(
                    text = "${formatCurrencyRingkas(hubUpkeep, useShortFormat)} /bln",
                    color = AviationCrimson,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onOpenUpgrade(hub) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AviationBlue),
                border = BorderStroke(1.dp, AviationBlue.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Build, contentDescription = null, tint = AviationBlue, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Konstruksi & Upgrade Fasilitas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: ROUTES TAB CONTENT (LIST / GRID & EDIT ROUTE MODAL)
// -------------------------------------------------------------
@Composable
fun RoutesTabContent(
    ownedData: OwnedBusiness,
    useShortFormat: Boolean,
    onCreateRouteClick: () -> Unit,
    onDeleteRoute: (String) -> Unit,
    onEditRoute: (FlightRoute) -> Unit
) {
    var isGridView by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Jaringan Penerbangan (${ownedData.flightRoutes.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Operasi rute dan penugasan armada",
                    fontSize = 11.sp,
                    color = AviationMutedGray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewModeToggle(
                    isGrid = isGridView,
                    onModeChange = { isGridView = it }
                )

                Button(
                    onClick = onCreateRouteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AviationGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buka Rute", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (ownedData.flightRoutes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = AviationMutedGray.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Belum ada rute aktif. Buat rute baru dan tugaskan pesawat untuk mulai mendatangkan profit.",
                        color = AviationMutedGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            if (isGridView) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(ownedData.flightRoutes, key = { it.id }) { route ->
                        RouteGridCard(
                            route = route,
                            ownedData = ownedData,
                            onDeleteRoute = onDeleteRoute,
                            onEditRoute = onEditRoute
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(ownedData.flightRoutes, key = { it.id }) { route ->
                        RouteListCard(
                            route = route,
                            ownedData = ownedData,
                            onDeleteRoute = onDeleteRoute,
                            onEditRoute = onEditRoute
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RouteGridCard(
    route: FlightRoute,
    ownedData: OwnedBusiness,
    onDeleteRoute: (String) -> Unit,
    onEditRoute: (FlightRoute) -> Unit
) {
    val originHub = ownedData.airlineHubsComplex.find { it.id == route.originHubId }
    val assignedPlanes = ownedData.airlineFleetComplex.filter { route.assignedAircraftIds.contains(it.id) }

    Card(
        colors = CardDefaults.cardColors(containerColor = AviationCardBg),
        border = BorderStroke(1.dp, AviationCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AviationGold.copy(alpha = 0.15f))
                        .border(1.dp, AviationGold.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = route.distanceCategory,
                        color = AviationGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { onDeleteRoute(route.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus Rute", tint = AviationCrimson, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${originHub?.city?.take(10) ?: "?"} ➔ ${route.destination.take(10)}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Tiket", color = AviationMutedGray, fontSize = 9.sp)
                    Text(
                        text = "$${route.ticketPrice}",
                        color = AviationGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Armada", color = AviationMutedGray, fontSize = 9.sp)
                    Text(
                        text = "${assignedPlanes.size} Pesawat",
                        color = if (assignedPlanes.isNotEmpty()) AviationEmerald else AviationAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Edit Route Button
            OutlinedButton(
                onClick = { onEditRoute(route) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AviationCyan),
                border = BorderStroke(1.dp, AviationCyan.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = AviationCyan, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Rute", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RouteListCard(
    route: FlightRoute,
    ownedData: OwnedBusiness,
    onDeleteRoute: (String) -> Unit,
    onEditRoute: (FlightRoute) -> Unit
) {
    val originHub = ownedData.airlineHubsComplex.find { it.id == route.originHubId }
    val assignedPlanes = ownedData.airlineFleetComplex.filter { route.assignedAircraftIds.contains(it.id) }

    Card(
        colors = CardDefaults.cardColors(containerColor = AviationCardBg),
        border = BorderStroke(1.dp, AviationCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flight,
                        contentDescription = null,
                        tint = AviationGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${originHub?.city ?: "Unknown"} ➔ ${route.destination}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AviationGold.copy(alpha = 0.15f))
                            .border(1.dp, AviationGold.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = route.distanceCategory,
                            color = AviationGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(onClick = { onDeleteRoute(route.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Rute", tint = AviationCrimson, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Route Metrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B202D), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Demand Pasar", color = AviationMutedGray, fontSize = 10.sp)
                    Text(
                        text = "${route.baseDemand} pax/hari",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column {
                    Text("Harga Tiket", color = AviationMutedGray, fontSize = 10.sp)
                    Text(
                        text = "$${route.ticketPrice}",
                        color = AviationGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Status Rute", color = AviationMutedGray, fontSize = 10.sp)
                    Text(
                        text = if (assignedPlanes.isNotEmpty()) "${assignedPlanes.size} Pesawat Aktif" else "Menunggu Pesawat",
                        color = if (assignedPlanes.isNotEmpty()) AviationEmerald else AviationAmber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Assigned Aircraft Listing
            Text("Pesawat Bertugas:", color = AviationMutedGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))

            if (assignedPlanes.isEmpty()) {
                Text(
                    text = "Belum ada pesawat ditugaskan di rute ini. Klik 'Edit Rute' untuk menugaskan pesawat.",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    assignedPlanes.forEach { p ->
                        val pDef = AVIATION_AIRCRAFT_CATALOG.find { it.id == p.modelId }
                            ?: DUMMY_AIRCRAFTS.find { it.id == p.modelId }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF222938))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "✈️ PK-${p.id.take(5).uppercase()}",
                                    color = AviationEmerald,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pDef?.model ?: p.modelId,
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = "Kondisi: ${p.condition.toInt()}%",
                                color = if (p.condition < 40) AviationCrimson else AviationEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Edit Route Button
            OutlinedButton(
                onClick = { onEditRoute(route) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AviationCyan),
                border = BorderStroke(1.dp, AviationCyan.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = AviationCyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Penugasan Pesawat Rute", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// MODAL DIALOG: EDIT ROUTE (Assign & Unassign Fleet)
// -------------------------------------------------------------
@Composable
fun EditRouteDialog(
    route: FlightRoute,
    ownedData: OwnedBusiness,
    onDismiss: () -> Unit,
    onAssignPlane: (String) -> Unit,
    onUnassignPlane: (String) -> Unit
) {
    val originHub = ownedData.airlineHubsComplex.find { it.id == route.originHubId }

    // Currently assigned planes on this route
    val assignedPlanes = ownedData.airlineFleetComplex.filter { route.assignedAircraftIds.contains(it.id) }

    // Compatible available planes:
    // 1. Not delivering
    // 2. Stationed at this route's origin Hub
    // 3. Not currently assigned to any route (assignedRouteId == null)
    // 4. Matches route distance category specifications
    val availablePlanes = ownedData.airlineFleetComplex.filter { pl ->
        pl.status != "DELIVERING" &&
        pl.assignedRouteId == null &&
        pl.stationedHubId == route.originHubId &&
        isAircraftCompatibleWithRoute(
            aircraftType = (AVIATION_AIRCRAFT_CATALOG.find { it.id == pl.modelId } ?: DUMMY_AIRCRAFTS.find { it.id == pl.modelId })?.type ?: "",
            distanceCategory = route.distanceCategory
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AviationCardBg,
            border = BorderStroke(1.dp, AviationCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pengaturan Armada Rute",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${originHub?.city ?: "?"} ➔ ${route.destination} (${route.distanceCategory})",
                            color = AviationGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.LightGray)
                    }
                }

                HorizontalDivider(color = AviationCardBorder, modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // SECTION 1: ASSIGNED FLEET
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. Pesawat Bertugas di Rute (${assignedPlanes.size})",
                                color = AviationEmerald,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (assignedPlanes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1B202D))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada pesawat yang ditugaskan di rute ini.",
                                    color = AviationMutedGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        items(assignedPlanes) { plane ->
                            val model = AVIATION_AIRCRAFT_CATALOG.find { it.id == plane.modelId }
                                ?: DUMMY_AIRCRAFTS.find { it.id == plane.modelId }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2433)),
                                border = BorderStroke(1.dp, AviationEmerald.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = model?.model ?: plane.modelId,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "PK-${plane.id.take(5).uppercase()} • Kondisi: ${plane.condition.toInt()}%",
                                            color = AviationMutedGray,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Button(
                                        onClick = { onUnassignPlane(plane.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AviationCrimson.copy(alpha = 0.2f)),
                                        border = BorderStroke(1.dp, AviationCrimson.copy(alpha = 0.5f)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Lepas / Unassign", color = AviationCrimson, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 2: AVAILABLE FLEET
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2. Pesawat Tersedia (Hub: ${originHub?.city ?: "?"})",
                            color = AviationCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (availablePlanes.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1B202D))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tidak ada pesawat standby di Hub ${originHub?.city ?: "-"} yang sesuai untuk rute ${route.distanceCategory}.",
                                    color = AviationMutedGray,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(availablePlanes) { plane ->
                            val model = AVIATION_AIRCRAFT_CATALOG.find { it.id == plane.modelId }
                                ?: DUMMY_AIRCRAFTS.find { it.id == plane.modelId }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A222F)),
                                border = BorderStroke(1.dp, AviationCardBorder),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = model?.model ?: plane.modelId,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "PK-${plane.id.take(5).uppercase()}",
                                                color = AviationMutedGray,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            if (model != null) {
                                                AircraftCategoryBadge(type = model.type)
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { onAssignPlane(plane.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AviationEmerald),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Tugaskan", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AviationGold),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Selesai / Simpan Perubahan", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// Helper: Check aircraft category compatibility with route distance category
fun isAircraftCompatibleWithRoute(aircraftType: String, distanceCategory: String): Boolean {
    return when (distanceCategory.uppercase()) {
        "SHORT" -> aircraftType in listOf("PROPELLER", "REGIONAL_JET", "HELICOPTER", "SHORT_MEDIUM", "NARROW_BODY")
        "MEDIUM" -> aircraftType in listOf("REGIONAL_JET", "SHORT_MEDIUM", "NARROW_BODY", "WIDE_BODY")
        "LONG" -> aircraftType in listOf("WIDE_BODY")
        else -> true
    }
}
