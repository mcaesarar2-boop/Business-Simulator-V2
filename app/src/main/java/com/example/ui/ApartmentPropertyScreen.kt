package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.*
import com.example.viewmodel.GameViewModel
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentPropertyScreen(
    navController: NavController,
    viewModel: GameViewModel,
    instanceId: String
) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()

    val business = playerState.ownedBusinesses.find { it.instanceId == instanceId }
        ?: playerState.ownedBusinesses.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }

    if (business == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Apartment (Property Management)") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Unit bisnis properti tidak ditemukan.", color = Color.White)
            }
        }
        return
    }

    val apartmentData = business.apartmentData

    var selectedTab by remember { mutableIntStateOf(0) }
    var showCapitalDialog by remember { mutableStateOf(false) }
    var capitalActionType by remember { mutableStateOf("suntik") } // "suntik" or "tarik"
    var showExpandFloorsDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showLiquidationDialog by remember { mutableStateOf(false) }
    var liquidationInputText by remember { mutableStateOf("") }
    var showSoldSuccessModal by remember { mutableStateOf(false) }
    var soldValuationAmount by remember { mutableLongStateOf(0L) }

    val valuationBreakdown = remember(apartmentData) {
        com.example.viewmodel.ApartmentEngine.calculateLiquidationValuation(apartmentData)
    }

    // Pulsing animation for incident warning nodes
    val infiniteTransition = rememberInfiniteTransition(label = "IncidentPulse")
    val incidentAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "incidentPulseAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = business.customName ?: "Apartment (Property Management)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Property Management • ${apartmentData.buildingFloors} Lantai • ${apartmentData.totalOccupiedUnits}/${apartmentData.totalUnits} Unit Terisi (${apartmentData.occupancyRatePercentage.toInt()}%)",
                            fontSize = 12.sp,
                            color = if (apartmentData.occupancyRatePercentage >= 80.0) Color(0xFF00E676) else if (apartmentData.occupancyRatePercentage >= 50.0) Color(0xFFFFD600) else Color(0xFFFF5252)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (apartmentData.activeIncidents.isNotEmpty()) {
                        Badge(
                            containerColor = Color(0xFFFF1744),
                            contentColor = Color.White,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text("${apartmentData.activeIncidents.size} Laporan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    IconButton(onClick = { showSettingsSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan Bisnis",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0B1120)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ==============================================================
            // 1. VISUALISASI UTAMA: BUILDING GRID (LANTAI & KAMAR)
            // ==============================================================
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "🏢 Visualisasi Denah Gedung",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Status okupansi real-time lantai 1 s/d ${apartmentData.buildingFloors}",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                            }

                            // Legend Indicator
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00E676))
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Terisi", fontSize = 10.sp, color = Color.LightGray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF334155))
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Kosong", fontSize = 10.sp, color = Color.LightGray)
                                }
                                if (apartmentData.activeIncidents.isNotEmpty()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFF1744))
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Rusak", fontSize = 10.sp, color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Building Floors Grid Box
                        val gridCells = ApartmentDataGenerator.generateBuildingGrid(
                            floors = apartmentData.buildingFloors,
                            categories = apartmentData.unitCategories,
                            incidents = apartmentData.activeIncidents
                        )
                        val groupedByFloor = gridCells.groupBy { it.floorNumber }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF020617))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (floor in apartmentData.buildingFloors downTo 1) {
                                val roomsOnFloor = groupedByFloor[floor] ?: emptyList()
                                val isRooftop = floor == apartmentData.buildingFloors && apartmentData.installedFacilities.contains(ApartmentFacilityType.ROOFTOP_POOL)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Floor Label Badge
                                    Box(
                                        modifier = Modifier
                                            .width(34.dp)
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (floor == apartmentData.buildingFloors) Color(0xFF7C3AED) else if (floor == 1) Color(0xFF0284C7) else Color(0xFF1E293B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (floor == apartmentData.buildingFloors && apartmentData.unitCategories.any { it.type == ApartmentUnitType.PENTHOUSE && it.isUnlocked }) "PH" else if (floor == 1 && apartmentData.unitCategories.any { it.type == ApartmentUnitType.COMMERCIAL_RETAIL && it.isUnlocked }) "G" else "L$floor",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }

                                    // Room Units for this floor
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        roomsOnFloor.forEach { room ->
                                            val cellColor = when {
                                                room.hasIncident -> Color(0xFFFF1744).copy(alpha = incidentAlpha)
                                                room.isOccupied -> when (room.unitType) {
                                                    ApartmentUnitType.PENTHOUSE -> Color(0xFFA855F7) // Luxury Purple
                                                    ApartmentUnitType.COMMERCIAL_RETAIL -> Color(0xFF06B6D4) // Cyan Retail
                                                    ApartmentUnitType.TWO_BEDROOM -> Color(0xFF10B981) // Emerald 2BR
                                                    ApartmentUnitType.STUDIO -> Color(0xFF22C55E) // Bright Green Studio
                                                }
                                                else -> Color(0xFF1E293B) // Empty
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(24.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(cellColor)
                                                    .border(
                                                        width = if (room.hasIncident) 1.5.dp else 0.5.dp,
                                                        color = if (room.hasIncident) Color.White else Color(0xFF334155),
                                                        shape = RoundedCornerShape(3.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (room.hasIncident) {
                                                    Text("⚠️", fontSize = 10.sp)
                                                } else if (room.isOccupied) {
                                                    Text(
                                                        text = when (room.unitType) {
                                                            ApartmentUnitType.PENTHOUSE -> "👑"
                                                            ApartmentUnitType.COMMERCIAL_RETAIL -> "🏬"
                                                            else -> ""
                                                        },
                                                        fontSize = 8.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Rooftop Amenities Badge
                        if (apartmentData.installedFacilities.contains(ApartmentFacilityType.ROOFTOP_POOL)) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0369A1).copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏊‍♂️", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Rooftop Sky Deck & Infinity Pool aktif melayani penghuni",
                                    fontSize = 11.sp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // ==============================================================
            // 2. TIGA METRIK UTAMA (OCCUPANCY, SATISFACTION, NET CASHFLOW)
            // ==============================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metrik 1: Tingkat Okupansi (Occupancy Rate)
                    val occPct = apartmentData.occupancyRatePercentage
                    val occBarColor = when {
                        occPct >= 80.0 -> Color(0xFF00E676)
                        occPct >= 50.0 -> Color(0xFFFFD600)
                        else -> Color(0xFFFF5252)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📊 Tingkat Okupansi", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(occBarColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (occPct >= 85.0) "PRIMA" else if (occPct >= 60.0) "STABIL" else "PERLU PROMO",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = occBarColor
                                        )
                                    }
                                }
                                Text(
                                    text = "${apartmentData.occupancyRatePercentage.toInt()}% (${apartmentData.totalOccupiedUnits}/${apartmentData.totalUnits} Unit)",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = occBarColor
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (occPct / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = occBarColor,
                                trackColor = Color(0xFF0F172A),
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${apartmentData.totalUnits - apartmentData.totalOccupiedUnits} unit siap disewa",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )

                                if (apartmentData.buildingFloors < 20) {
                                    val expandCost = com.example.viewmodel.ApartmentEngine.getFloorExpansionCost(apartmentData.buildingFloors)
                                    Button(
                                        onClick = { showExpandFloorsDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "+2 Lantai (${formatCurrencyRingkas(expandCost.toDouble(), useShortFormat)})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Metrik 2: Kepuasan Penghuni (Tenant Satisfaction)
                    val starScore = apartmentData.tenantSatisfaction
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("⭐ Kepuasan Penghuni", fontSize = 12.sp, color = Color.LightGray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$starScore / 5.0",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = if (starScore >= 4.2) Color(0xFFFFD700) else if (starScore >= 3.5) Color(0xFF38BDF8) else Color(0xFFFF5252)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "★".repeat(starScore.roundToInt().coerceIn(1, 5)) + "☆".repeat(5 - starScore.roundToInt().coerceIn(1, 5)),
                                        color = Color(0xFFFFD700),
                                        fontSize = 14.sp
                                    )
                                }
                                Text(
                                    text = "Batas Maks: ${apartmentData.maxSatisfactionCap} ⭐ (${apartmentData.installedFacilities.size} Fasilitas Terpasang)",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Icon(
                                imageVector = if (starScore >= 4.0) Icons.Default.Mood else if (starScore >= 3.0) Icons.Default.SentimentNeutral else Icons.Default.MoodBad,
                                contentDescription = null,
                                tint = if (starScore >= 4.0) Color(0xFF00E676) else if (starScore >= 3.0) Color(0xFFFFD600) else Color(0xFFFF5252),
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    // Metrik 3: Net Cashflow & Kas Internal Usaha
                    val netProfit = apartmentData.netMonthlyCashflow
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("💵 Net Monthly Cashflow", fontSize = 12.sp, color = Color.LightGray)
                                    Text(
                                        text = (if (netProfit >= 0) "+" else "") + formatCurrencyRingkas(netProfit.toDouble(), useShortFormat) + " / bln",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = if (netProfit >= 0) Color(0xFF00E676) else Color(0xFFFF5252)
                                    )
                                    Text(
                                        text = "Gross Sewa: +${formatCurrencyRingkas(apartmentData.grossMonthlyRent.toDouble(), useShortFormat)} • Biaya: -${formatCurrencyRingkas(apartmentData.totalMonthlyExpenses.toDouble(), useShortFormat)}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            capitalActionType = "suntik"
                                            showCapitalDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Suntik", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            capitalActionType = "tarik"
                                            showCapitalDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Tarik", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Saldo Kas Internal: ${formatCurrencyRingkas(apartmentData.internalCash.toDouble(), useShortFormat)}",
                                fontSize = 11.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // ==============================================================
            // 3. TAB CONTROLLER
            // ==============================================================
            item {
                val tabs = listOf("Manajemen Unit", "Fasilitas & Layanan", "Laporan Penghuni")
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color.White,
                    modifier = Modifier.padding(top = 14.dp),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF38BDF8),
                            height = 3.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (selectedTab == index) Color(0xFF38BDF8) else Color.LightGray
                                    )
                                    if (index == 2 && apartmentData.activeIncidents.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFF1744))
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // ==============================================================
            // 4. TAB 0: MANAJEMEN TIPE UNIT & HARGA SEWA (SUPPLY & DEMAND)
            // ==============================================================
            if (selectedTab == 0) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Manajemen Tipe Unit & Harga Sewa",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Atur harga sewa per bulan. Cari 'Sweet Spot' antara kepuasan penghuni dan profit maksimal!",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                items(apartmentData.unitCategories) { category ->
                    val totalToleranceBoost = apartmentData.installedFacilities.sumOf { it.rentToleranceBoost }
                    val satisfactionFactor = apartmentData.tenantSatisfaction / category.type.targetSatisfactionFor100Percent
                    val fairMarketPrice = (category.type.baseFairRent * satisfactionFactor + totalToleranceBoost).toLong().coerceAtLeast(100L)

                    val priceRatio = fairMarketPrice.toDouble() / category.monthlyRentPrice.toDouble().coerceAtLeast(1.0)
                    val sentimentBadge = when {
                        priceRatio >= 1.15 -> Triple("🔥 Sangat Murah (Cepat Penuh)", Color(0xFF065F46), Color(0xFF34D399))
                        priceRatio >= 0.95 -> Triple("✅ Sweet Spot (Ideal)", Color(0xFF1E40AF), Color(0xFF60A5FA))
                        priceRatio >= 0.75 -> Triple("⚠️ Sedikit Mahal", Color(0xFF854D0E), Color(0xFFFDE047))
                        else -> Triple("🚨 Kemahalan! (Penghuni Pindah)", Color(0xFF991B1B), Color(0xFFF87171))
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (category.isUnlocked) Color(0xFF1E293B) else Color(0xFF161E2E)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (category.isUnlocked) Color(0xFF334155) else Color(0xFF1E293B)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(category.type.iconEmoji, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                                    Column {
                                        Text(category.type.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                        Text(
                                            text = if (category.isUnlocked) "${category.occupiedUnits} / ${category.totalUnits} Unit (${(category.occupancyRateFraction * 100).toInt()}% Terisi)" else "Belum Di-Unlock",
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }

                                if (category.isUnlocked) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(sentimentBadge.second)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = sentimentBadge.first,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = sentimentBadge.third
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.unlockApartmentUnitType(instanceId, category.type) },
                                        enabled = apartmentData.internalCash >= category.type.unlockCost,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Unlock (${formatCurrencyRingkas(category.type.unlockCost.toDouble(), useShortFormat)})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(category.type.description, fontSize = 11.sp, color = Color.Gray)

                            if (category.isUnlocked) {
                                Spacer(modifier = Modifier.height(10.dp))

                                // Occupancy progress bar
                                LinearProgressIndicator(
                                    progress = { category.occupancyRateFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (category.occupancyRateFraction >= 0.8f) Color(0xFF00E676) else Color(0xFF38BDF8),
                                    trackColor = Color(0xFF0F172A),
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Slider & Controls for Rent Price
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0F172A))
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Harga Sewa Unit", fontSize = 11.sp, color = Color.LightGray)
                                            Text(
                                                text = "${formatCurrencyRingkas(category.monthlyRentPrice.toDouble(), useShortFormat)} / bulan",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp,
                                                color = Color(0xFF38BDF8)
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Estimasi Sewa Wajar Pasar", fontSize = 10.sp, color = Color.Gray)
                                            Text(
                                                text = "≈ ${formatCurrencyRingkas(fairMarketPrice.toDouble(), useShortFormat)} / bulan",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFFBBF24)
                                            )
                                        }
                                    }

                                    // Stepper & Slider
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val step = when (category.type) {
                                                    ApartmentUnitType.STUDIO -> 50L
                                                    ApartmentUnitType.TWO_BEDROOM -> 100L
                                                    ApartmentUnitType.PENTHOUSE -> 250L
                                                    ApartmentUnitType.COMMERCIAL_RETAIL -> 500L
                                                }
                                                viewModel.setApartmentRentPrice(instanceId, category.type, category.monthlyRentPrice - step)
                                            },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF1E293B))
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }

                                        Slider(
                                            value = category.monthlyRentPrice.toFloat(),
                                            onValueChange = { newPrice ->
                                                viewModel.setApartmentRentPrice(instanceId, category.type, newPrice.toLong())
                                            },
                                            valueRange = (category.type.baseFairRent * 0.3f)..(category.type.baseFairRent * 2.8f),
                                            modifier = Modifier.weight(1f),
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFF38BDF8),
                                                activeTrackColor = Color(0xFF0284C7),
                                                inactiveTrackColor = Color(0xFF334155)
                                            )
                                        )

                                        IconButton(
                                            onClick = {
                                                val step = when (category.type) {
                                                    ApartmentUnitType.STUDIO -> 50L
                                                    ApartmentUnitType.TWO_BEDROOM -> 100L
                                                    ApartmentUnitType.PENTHOUSE -> 250L
                                                    ApartmentUnitType.COMMERCIAL_RETAIL -> 500L
                                                }
                                                viewModel.setApartmentRentPrice(instanceId, category.type, category.monthlyRentPrice + step)
                                            },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF1E293B))
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Kontribusi Sewa: +${formatCurrencyRingkas(category.totalMonthlyRent.toDouble(), useShortFormat)} / bln",
                                            fontSize = 11.sp,
                                            color = Color(0xFF00E676),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Sensitivitas: ${if (category.type == ApartmentUnitType.STUDIO) "Tinggi" else if (category.type == ApartmentUnitType.TWO_BEDROOM) "Sedang" else "Mewah"}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==============================================================
            // 5. TAB 1: FASILITAS & LAYANAN (UPGRADES & UPKEEP COSTS)
            // ==============================================================
            if (selectedTab == 1) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Fasilitas & Upkeep Biaya Operasional",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Membangun fasilitas menaikkan batas maksimal Kepuasan & Toleransi Harga Sewa penghuni.",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                items(ApartmentFacilityType.values()) { facility ->
                    val isInstalled = apartmentData.installedFacilities.contains(facility)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isInstalled) Color(0xFF0F2338) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isInstalled) Color(0xFF0284C7) else Color(0xFF334155)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(facility.iconEmoji, fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = facility.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isInstalled) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("TERPASANG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(facility.description, fontSize = 11.sp, color = Color.LightGray)

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Biaya Upkeep: -${formatCurrencyRingkas(facility.monthlyUpkeep.toDouble(), useShortFormat)}/bln",
                                            fontSize = 11.sp,
                                            color = Color(0xFFFF8A80)
                                        )
                                        Text(
                                            text = "Bonus: +${facility.satisfactionBonus} ⭐ • +${formatCurrencyRingkas(facility.rentToleranceBoost.toDouble(), useShortFormat)} Toleransi",
                                            fontSize = 10.sp,
                                            color = Color(0xFF4ADE80),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    if (!isInstalled) {
                                        Button(
                                            onClick = { viewModel.installApartmentFacility(instanceId, facility) },
                                            enabled = apartmentData.internalCash >= facility.installCost,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "Bangun (${formatCurrencyRingkas(facility.installCost.toDouble(), useShortFormat)})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==============================================================
            // 6. TAB 2: LAPORAN PENGHUNI & MAINTENANCE EVENTS
            // ==============================================================
            if (selectedTab == 2) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Laporan Kerusakan & Keluhan Penghuni",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Perbaiki cepat laporan darurat agar kepuasan penghuni tidak anjlok.",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                if (apartmentData.activeIncidents.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF065F46))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Semua Unit Berjalan Lancar!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                Text("Tidak ada laporan kerusakan atau kebocoran pipa saat ini.", fontSize = 12.sp, color = Color.LightGray, textAlign = TextAlign.Center)
                            }
                        }
                    }
                } else {
                    items(apartmentData.activeIncidents) { incident ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3B0B13)),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF1744).copy(alpha = incidentAlpha))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(incident.iconEmoji, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                                        Column {
                                            Text(incident.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                            Text(
                                                text = "Lantai ${incident.affectedFloor} • Sisa Waktu: ${incident.remainingSeconds}s",
                                                fontSize = 11.sp,
                                                color = Color(0xFFFF8A80),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFFF1744))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("DARURAT", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(incident.description, fontSize = 12.sp, color = Color.LightGray)

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "⚠️ Jika diabaikan: Kepuasan turun -${incident.satisfactionPenalty} ⭐ & ${incident.moveOutTenantCount} penghuni pindah.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFCDD2)
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.ignoreApartmentIncident(instanceId, incident.id) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Abaikan", fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { viewModel.resolveApartmentIncident(instanceId, incident.id) },
                                        enabled = apartmentData.internalCash >= incident.repairCost,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 3.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Perbaiki Cepat (${formatCurrencyRingkas(incident.repairCost.toDouble(), useShortFormat)})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // ==============================================================
    // DIALOG: SUNTIK / TARIK KAS INTERNAL PROPERTI
    // ==============================================================
    if (showCapitalDialog) {
        var inputAmount by remember { mutableStateOf("") }
        val isSuntik = capitalActionType == "suntik"

        AlertDialog(
            onDismissRequest = { showCapitalDialog = false },
            title = {
                Text(
                    text = if (isSuntik) "Suntik Modal ke Properti" else "Tarik Dividen Kas Properti",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isSuntik) {
                            "Transfer uang pribadi ke kas operasional apartemen.\nUang Pribadi Anda: ${formatCurrencyRingkas(playerState.cash.toDouble(), useShortFormat)}"
                        } else {
                            "Tarik kas surplus apartemen ke rekening pribadi.\nKas Tersedia: ${formatCurrencyRingkas(apartmentData.internalCash.toDouble(), useShortFormat)}"
                        },
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = inputAmount.toLongOrNull() ?: 0L
                        if (amount > 0) {
                            if (isSuntik) {
                                viewModel.injectCapitalToApartment(instanceId, amount)
                            } else {
                                viewModel.withdrawCapitalFromApartment(instanceId, amount)
                            }
                        }
                        showCapitalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuntik) Color(0xFF0284C7) else Color(0xFF059669)
                    )
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

    // ==============================================================
    // DIALOG: EKSPANSI LANTAI GEDUNG
    // ==============================================================
    if (showExpandFloorsDialog) {
        val cost = com.example.viewmodel.ApartmentEngine.getFloorExpansionCost(apartmentData.buildingFloors)
        AlertDialog(
            onDismissRequest = { showExpandFloorsDialog = false },
            title = {
                Text("Ekspansi Konstruksi Gedung", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        text = "Tambah +2 lantai baru ke gedung apartemen. Menambah +10 unit Studio, +6 unit 2-Bedroom, dan +2 unit Penthouse.",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Biaya Konstruksi: ${formatCurrencyRingkas(cost.toDouble(), useShortFormat)}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        text = "Kas Properti: ${formatCurrencyRingkas(apartmentData.internalCash.toDouble(), useShortFormat)}",
                        fontSize = 12.sp,
                        color = if (apartmentData.internalCash >= cost) Color(0xFF00E676) else Color(0xFFFF5252)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.expandApartmentFloors(instanceId)
                        showExpandFloorsDialog = false
                    },
                    enabled = apartmentData.internalCash >= cost,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("Bangun Sekarang", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpandFloorsDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // ==============================================================
    // PENGATURAN BISNIS BOTTOM SHEET
    // ==============================================================
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false },
            containerColor = Color(0xFF1E293B),
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pengaturan Properti",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = business.customName ?: "Apartment (Property Management)",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }

                // Info Ringkas Valuasi
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Estimasi Nilai Likuidasi Properti", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = formatCurrencyRingkas(valuationBreakdown.totalValuation.toDouble(), useShortFormat),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676)
                            )
                        }
                        Text(
                            text = "⭐ ${String.format("%.1f", apartmentData.tenantSatisfaction)} / 5.0",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFFD600)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Danger Zone Section
                Text(
                    text = "DANGER ZONE (ZONA BERBAHAYA)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5252),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1219)),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C))))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Likuidasi & Jual Unit Properti",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Jual seluruh unit apartemen, bangunan fisik gedung, dan cairkan saldo kas ke Saldo Perusahaan Utama.",
                            fontSize = 12.sp,
                            color = Color(0xFFFCA5A5)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                showSettingsSheet = false
                                liquidationInputText = ""
                                showLiquidationDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Likuidasi Bisnis (Jual)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ==============================================================
    // DANGER ZONE LIQUIDATION CONFIRMATION MODAL
    // ==============================================================
    if (showLiquidationDialog) {
        AlertDialog(
            onDismissRequest = { showLiquidationDialog = false },
            containerColor = Color(0xFF18181B),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Likuidasi Bisnis Properti",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Total Valuasi Properti Anda:",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = formatCurrencyRingkas(valuationBreakdown.totalValuation.toDouble(), useShortFormat),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E676),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Breakdown Valuasi
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF27272A)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• Saldo Kas Internal:", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    formatCurrencyRingkas(valuationBreakdown.internalCash.toDouble(), useShortFormat),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• Gedung & Lantai (${apartmentData.buildingFloors} F):", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    formatCurrencyRingkas(valuationBreakdown.buildingValue.toDouble(), useShortFormat),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• Fasilitas Terpasang (${apartmentData.installedFacilities.size}):", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    formatCurrencyRingkas(valuationBreakdown.facilitiesValue.toDouble(), useShortFormat),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• Goodwill Okupansi Sewa:", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    formatCurrencyRingkas(valuationBreakdown.occupancyGoodwill.toDouble(), useShortFormat),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• Kepuasan Penghuni (⭐ ${String.format("%.1f", apartmentData.tenantSatisfaction)}):", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    "${String.format("%.2f", valuationBreakdown.satisfactionMultiplier)}x",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (valuationBreakdown.satisfactionMultiplier >= 1.0) Color(0xFF00E676) else Color(0xFFFF5252)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "⚠️ PERINGATAN: Seluruh kepemilikan gedung, lantai unit apartemen, dan fasilitas akan dihapus secara permanen. Total uang hasil penjualan akan ditransfer ke Saldo Perusahaan Utama.",
                        fontSize = 11.sp,
                        color = Color(0xFFFCA5A5),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Ketik kata \"JUAL\" untuk konfirmasi:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = liquidationInputText,
                        onValueChange = { liquidationInputText = it },
                        placeholder = { Text("Ketik JUAL di sini...", color = Color.DarkGray, fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEF4444),
                            unfocusedBorderColor = Color(0xFF52525B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val isConfirmationValid = liquidationInputText.trim().equals("JUAL", ignoreCase = true)
                Button(
                    onClick = {
                        val amount = viewModel.liquidateApartmentBusiness(instanceId)
                        soldValuationAmount = amount
                        showLiquidationDialog = false
                        showSoldSuccessModal = true
                    },
                    enabled = isConfirmationValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626),
                        disabledContainerColor = Color(0xFF451A1A),
                        disabledContentColor = Color(0xFF7F1D1D)
                    )
                ) {
                    Text("Ya, Jual Bisnis", fontWeight = FontWeight.Bold, color = if (isConfirmationValid) Color.White else Color.Gray)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLiquidationDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // ==============================================================
    // LIQUIDATION SUCCESS TRANSITION MODAL
    // ==============================================================
    if (showSoldSuccessModal) {
        AlertDialog(
            onDismissRequest = {
                showSoldSuccessModal = false
                navController.popBackStack()
            },
            containerColor = Color(0xFF0F172A),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Properti Berhasil Terjual!",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 DANA LIKUIDASI PROPERTI DICAIRKAN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+${formatCurrencyRingkas(soldValuationAmount.toDouble(), useShortFormat)}",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF00E676)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Uang hasil penjualan aset properti apartemen telah berhasil ditransfer ke Saldo Perusahaan Utama Anda.",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSoldSuccessModal = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kembali ke Portfolio Utama", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        )
    }
}
