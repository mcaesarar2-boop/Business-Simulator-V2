package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.*
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.LogisticsEngine
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticsScreen(
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
                    title = { Text("Express (Logistics)") },
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
                Text("Unit bisnis tidak ditemukan.", color = Color.White)
            }
        }
        return
    }

    val logisticsData = business.logisticsData

    var selectedTab by remember { mutableIntStateOf(0) }
    var showBuyVehicleDialog by remember { mutableStateOf(false) }
    var showCapitalDialog by remember { mutableStateOf(false) }
    var capitalActionType by remember { mutableStateOf("suntik") } // "suntik" or "tarik"
    var showUpgradeWarehouseDialog by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showLiquidationDialog by remember { mutableStateOf(false) }
    var liquidationInputText by remember { mutableStateOf("") }
    var showSoldSuccessModal by remember { mutableStateOf(false) }
    var soldValuationAmount by remember { mutableLongStateOf(0L) }

    val valuationBreakdown = remember(logisticsData) {
        LogisticsEngine.calculateLiquidationValuation(logisticsData)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAngle"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = business.customName ?: "Express (Logistics)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Command Center • Hub Lv ${logisticsData.warehouseLevel} • ${if (logisticsData.isOverloaded) "🚨 OVERLOAD" else "🟢 NORMAL"}",
                            fontSize = 12.sp,
                            color = if (logisticsData.isOverloaded) Color(0xFFFF5252) else Color(0xFF00E676)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (logisticsData.techTree.aiSpeedLevel >= 2) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text(
                                text = "Auto AI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (logisticsData.autoDispatchEnabled) Color(0xFF00E676) else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = logisticsData.autoDispatchEnabled,
                                onCheckedChange = { viewModel.toggleLogisticsAutoDispatch(instanceId) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFF00E676),
                                    checkedTrackColor = Color(0xFF1B5E20)
                                )
                            )
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
            // 1. MINI-MAP / RADAR NETWORK HEADER
            // ==============================================================
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color(0xFF020617))
                ) {
                    // Background Hero Image
                    AsyncImage(
                        model = "https://plus.unsplash.com/premium_photo-1661901122974-e280f0c1efab?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                        contentDescription = "Logistics Hub Banner",
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.25f
                    )

                    // Canvas Radar Network Visualizer
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        val center = Offset(size.width / 2f, size.height / 2f - 10.dp.toPx())
                        val maxRadius = minOf(size.width, size.height) * 0.42f

                        // Draw Grid Rings
                        drawCircle(
                            color = Color(0xFF0284C7).copy(alpha = 0.2f),
                            radius = maxRadius * 0.33f,
                            center = center,
                            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
                        )
                        drawCircle(
                            color = Color(0xFF0284C7).copy(alpha = 0.25f),
                            radius = maxRadius * 0.66f,
                            center = center,
                            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
                        )
                        drawCircle(
                            color = Color(0xFF0284C7).copy(alpha = 0.35f),
                            radius = maxRadius,
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // Crosshair lines
                        drawLine(
                            color = Color(0xFF0284C7).copy(alpha = 0.2f),
                            start = Offset(center.x - maxRadius, center.y),
                            end = Offset(center.x + maxRadius, center.y),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawLine(
                            color = Color(0xFF0284C7).copy(alpha = 0.2f),
                            start = Offset(center.x, center.y - maxRadius),
                            end = Offset(center.x, center.y + maxRadius),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Delivery Route Nodes
                        val nodeAngles = listOf(30f, 110f, 170f, 240f, 310f)
                        val nodeDistances = listOf(0.85f, 0.70f, 0.90f, 0.75f, 0.80f)
                        val nodeColors = listOf(Color(0xFF38BDF8), Color(0xFF4ADE80), Color(0xFFFBBF24), Color(0xFFA78BFA), Color(0xFFF472B6))

                        val nodePositions = mutableListOf<Offset>()
                        for (i in nodeAngles.indices) {
                            val rad = nodeAngles[i] * (PI / 180.0)
                            val dist = maxRadius * nodeDistances[i]
                            val nodePos = Offset(
                                x = center.x + (dist * cos(rad)).toFloat(),
                                y = center.y + (dist * sin(rad)).toFloat()
                            )
                            nodePositions.add(nodePos)

                            // Draw Route Lines from Center HQ to Node
                            drawLine(
                                color = nodeColors[i].copy(alpha = 0.4f),
                                start = center,
                                end = nodePos,
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )

                            // Outer glow & node circle
                            drawCircle(
                                color = nodeColors[i].copy(alpha = 0.3f),
                                radius = 8.dp.toPx(),
                                center = nodePos
                            )
                            drawCircle(
                                color = nodeColors[i],
                                radius = 4.dp.toPx(),
                                center = nodePos
                            )
                        }

                        // Draw Radar Sweep Line
                        val sweepRad = radarAngle * (PI / 180.0)
                        val sweepEnd = Offset(
                            x = center.x + (maxRadius * cos(sweepRad)).toFloat(),
                            y = center.y + (maxRadius * sin(sweepRad)).toFloat()
                        )
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.1f), Color(0xFF38BDF8)),
                                start = center,
                                end = sweepEnd
                            ),
                            start = center,
                            end = sweepEnd,
                            strokeWidth = 2.5.dp.toPx()
                        )

                        // Central Hub (Pusat Gudang SRC)
                        drawCircle(
                            color = Color(0xFF00E676).copy(alpha = 0.3f),
                            radius = (14.dp * pulseScale).toPx(),
                            center = center
                        )
                        drawCircle(
                            color = Color(0xFF00E676),
                            radius = 7.dp.toPx(),
                            center = center
                        )

                        // Draw En-Route Moving Vehicles on Paths
                        val enRouteVehicles = logisticsData.fleet.filter { it.status == FleetVehicleStatus.EN_ROUTE }
                        for ((idx, vehicle) in enRouteVehicles.withIndex()) {
                            val targetNode = nodePositions[idx % nodePositions.size]
                            val progress = vehicle.currentTripProgress
                            val currentPos = Offset(
                                x = center.x + (targetNode.x - center.x) * progress,
                                y = center.y + (targetNode.y - center.y) * progress
                            )

                            drawCircle(
                                color = Color(0xFFFFD600).copy(alpha = 0.5f),
                                radius = 7.dp.toPx(),
                                center = currentPos
                            )
                            drawCircle(
                                color = Color(0xFFFFD600),
                                radius = 4.dp.toPx(),
                                center = currentPos
                            )
                        }
                    }

                    // Live Weather & Traffic Event Floating Banner
                    val event = logisticsData.currentEvent
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0F172A).copy(alpha = 0.92f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.badgeIcon,
                                fontSize = 22.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = event.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${event.durationRemainingSeconds}s",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                                Text(
                                    text = event.description,
                                    fontSize = 11.sp,
                                    color = Color.LightGray,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // ==============================================================
            // 2. TIGA INDIKATOR UTAMA (COMMAND STATS)
            // ==============================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Indikator 1: Kas Internal Usaha
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
                                Text("Kas Internal Logistik", fontSize = 12.sp, color = Color.LightGray)
                                Text(
                                    text = formatCurrencyRingkas(logisticsData.internalCash.toDouble(), useShortFormat),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    color = if (logisticsData.internalCash >= 0) Color(0xFF00E676) else Color(0xFFFF5252)
                                )
                                Text(
                                    text = "Total Cuan: +${formatCurrencyRingkas(logisticsData.totalRevenueEarned.toDouble(), useShortFormat)}",
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
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Suntik", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        capitalActionType = "tarik"
                                        showCapitalDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.ArrowOutward, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Tarik", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Indikator 2: Kapasitas Gudang (Warehouse Load)
                    val loadPct = logisticsData.loadPercentage
                    val isOverload = logisticsData.isOverloaded
                    val loadBarColor = when {
                        isOverload -> Color(0xFFFF1744)
                        loadPct > 0.70f -> Color(0xFFFFD600)
                        else -> Color(0xFF00E676)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOverload) Color(0xFF3B0B13) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isOverload) 2.dp else 1.dp,
                            color = if (isOverload) Color(0xFFFF1744) else Color(0xFF334155)
                        )
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📦 Gudang Pusat (Level ${logisticsData.warehouseLevel})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                    if (isOverload) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFFF1744))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("OVERLOAD!", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                        }
                                    }
                                }
                                Text(
                                    text = "${logisticsData.currentWarehousePackages} / ${logisticsData.warehouseCapacity} Pkt",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = loadBarColor
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { loadPct.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = loadBarColor,
                                trackColor = Color(0xFF0F172A),
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isOverload) {
                                    Text(
                                        text = "🚨 Denda Demurrage aktif! Cepat deploy armada!",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFF8A80),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                } else {
                                    Text(
                                        text = "Kapasitas aman (${(loadPct * 100).toInt()}% terisi)",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                }

                                if (logisticsData.warehouseLevel < 10) {
                                    val upgradeCost = getWarehouseUpgradeCost(logisticsData.warehouseLevel)
                                    Button(
                                        onClick = { showUpgradeWarehouseDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "Upgrade (${formatCurrencyRingkas(upgradeCost.toDouble(), useShortFormat)})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Indikator 3: Tingkat Keberhasilan (Success Rate)
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
                                Text("Tingkat Keberhasilan Pengiriman", fontSize = 12.sp, color = Color.LightGray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${logisticsData.successRate}%",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = if (logisticsData.successRate >= 95.0) Color(0xFF00E676) else if (logisticsData.successRate >= 80.0) Color(0xFFFFD600) else Color(0xFFFF5252)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (logisticsData.successRate >= 95.0) {
                                        Text("🏆 +10% Bonus Bayaran", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                    }
                                }
                                Text(
                                    text = "${logisticsData.totalPackagesDelivered} paket terkirim sukses",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Icon(
                                imageVector = if (logisticsData.successRate >= 90.0) Icons.Default.Verified else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (logisticsData.successRate >= 90.0) Color(0xFF00E676) else Color(0xFFFFD600),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            // ==============================================================
            // 3. TAB CONTROLLER
            // ==============================================================
            item {
                val tabs = listOf("Garasi Armada", "Kontrak Klien", "Pusat Riset")
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0F172A),
                    contentColor = Color.White,
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
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (selectedTab == index) Color(0xFF38BDF8) else Color.LightGray
                                )
                            }
                        )
                    }
                }
            }

            // ==============================================================
            // 4. TAB 0: GARASI ARMADA (FLEET MANAGEMENT)
            // ==============================================================
            if (selectedTab == 0) {
                item {
                    val idleCount = logisticsData.fleet.count { it.status == FleetVehicleStatus.IDLE }
                    val enRouteCount = logisticsData.fleet.count { it.status == FleetVehicleStatus.EN_ROUTE }
                    val brokenCount = logisticsData.fleet.count { it.status == FleetVehicleStatus.BROKEN }
                    val needsRepairCount = logisticsData.fleet.count { it.conditionHp < 100.0 || it.status == FleetVehicleStatus.BROKEN }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Action Bar: Deploy All, Repair All, Buy New
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.deployAllIdleFleet(instanceId) },
                                enabled = idleCount > 0 && logisticsData.currentWarehousePackages > 0,
                                modifier = Modifier.weight(1.3f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7),
                                    disabledContainerColor = Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Deploy All ($idleCount)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            if (needsRepairCount > 0) {
                                Button(
                                    onClick = { viewModel.repairAllFleet(instanceId) },
                                    modifier = Modifier.weight(1.1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Servis Semua", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { showBuyVehicleDialog = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Beli (+)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Daftar Armada (${logisticsData.fleet.size} Unit: $idleCount Standby, $enRouteCount Jalan, $brokenCount Mogok)",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                items(logisticsData.fleet) { vehicle ->
                    val repairCost = com.example.viewmodel.LogisticsEngine.calculateRepairCost(vehicle)
                    val hpColor = when {
                        vehicle.conditionHp > 65.0 -> Color(0xFF00E676)
                        vehicle.conditionHp > 30.0 -> Color(0xFFFFD600)
                        else -> Color(0xFFFF5252)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (vehicle.status == FleetVehicleStatus.BROKEN) Color(0xFF331317) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (vehicle.status == FleetVehicleStatus.BROKEN) Color(0xFFFF1744) else Color(0xFF334155)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(vehicle.type.iconEmoji, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                                    Column {
                                        Text(vehicle.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                        Text(
                                            text = "${vehicle.type.displayName} • Kapasitas ${vehicle.type.capacity} Pkt",
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }

                                // Status Badge
                                val (statusText, statusBg, statusTextColor) = when (vehicle.status) {
                                    FleetVehicleStatus.IDLE -> Triple("STANDBY", Color(0xFF065F46), Color(0xFF34D399))
                                    FleetVehicleStatus.EN_ROUTE -> Triple("JALAN (${(vehicle.currentTripProgress * 100).toInt()}%)", Color(0xFF1E40AF), Color(0xFF60A5FA))
                                    FleetVehicleStatus.MAINTENANCE -> Triple("SERVIS", Color(0xFF854D0E), Color(0xFFFDE047))
                                    FleetVehicleStatus.BROKEN -> Triple("MOGOK!", Color(0xFF991B1B), Color(0xFFF87171))
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusBg)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(statusText, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = statusTextColor)
                                }
                            }

                            // En Route Progress Bar
                            if (vehicle.status == FleetVehicleStatus.EN_ROUTE) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Mengantar ${vehicle.carryingPackages} paket ke ${vehicle.assignedContractName}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF38BDF8)
                                        )
                                        Text(
                                            text = "${(vehicle.currentTripProgress * 100).toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { vehicle.currentTripProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFF38BDF8),
                                        trackColor = Color(0xFF0F172A),
                                    )
                                }
                            }

                            // HP Condition Bar
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Kondisi Mesin: ", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.width(4.dp))
                                LinearProgressIndicator(
                                    progress = { (vehicle.conditionHp / 100.0).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = hpColor,
                                    trackColor = Color(0xFF0F172A),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${vehicle.conditionHp.toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = hpColor
                                )
                            }

                            // Action Buttons (Deploy or Repair)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (vehicle.conditionHp < 100.0 || vehicle.status == FleetVehicleStatus.BROKEN) {
                                    OutlinedButton(
                                        onClick = { viewModel.repairFleetVehicle(instanceId, vehicle.id) },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (vehicle.status == FleetVehicleStatus.BROKEN) Color(0xFFFF5252) else Color(0xFFFFD600)
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (vehicle.status == FleetVehicleStatus.BROKEN) Color(0xFFFF5252) else Color(0xFFFFD600)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (vehicle.status == FleetVehicleStatus.BROKEN) "Perbaikan Darurat (${formatCurrencyRingkas(repairCost.toDouble(), useShortFormat)})" else "Servis (${formatCurrencyRingkas(repairCost.toDouble(), useShortFormat)})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                if (vehicle.status == FleetVehicleStatus.IDLE) {
                                    Button(
                                        onClick = { viewModel.deployFleetVehicle(instanceId, vehicle.id) },
                                        enabled = logisticsData.currentWarehousePackages > 0 && vehicle.conditionHp > 0.0,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF0284C7),
                                            disabledContainerColor = Color(0xFF334155)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Tugaskan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==============================================================
            // 5. TAB 1: KONTRAK KLIEN (DELIVERY CONTRACTS)
            // ==============================================================
            if (selectedTab == 1) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Kontrak Aktif (${logisticsData.activeContracts.size} / 3 Slot)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Paket dari kontrak aktif akan terus mengalir masuk ke Gudang Utama.",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                if (logisticsData.activeContracts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Tidak ada kontrak aktif. Pilih tawaran kontrak di bawah!", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    items(logisticsData.activeContracts) { contract ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(contract.clientLogo, fontSize = 22.sp, modifier = Modifier.padding(end = 8.dp))
                                        Column {
                                            Text(contract.clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                            Text("${contract.categoryTag} • Tier ${contract.tierLevel}", fontSize = 11.sp, color = Color.LightGray)
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF065F46))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("AKTIF", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF34D399))
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Inbound: +${contract.inboundPackagesPerSec} pkt/dtk",
                                        fontSize = 11.sp,
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Bayaran: $${contract.payoutPerPackage}/pkt",
                                        fontSize = 11.sp,
                                        color = Color(0xFF00E676),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Sisa: ${contract.remainingDurationSeconds}s",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFFD600),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Target: ${contract.deliveredPackages} / ${contract.targetTotalPackages} paket terkirim",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { contract.progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF00E676),
                                    trackColor = Color(0xFF0F172A),
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Bonus Selesai: +${formatCurrencyRingkas(contract.completionBonusCash.toDouble(), useShortFormat)}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFFD700),
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(
                                        onClick = { viewModel.cancelLogisticsContract(instanceId, contract.id) },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Putus Kontrak", fontSize = 11.sp, color = Color(0xFFFF5252))
                                    }
                                }
                            }
                        }
                    }
                }

                // Available Contract Offers
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tawaran Kontrak Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                items(logisticsData.availableContracts) { offer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(offer.clientLogo, fontSize = 22.sp, modifier = Modifier.padding(end = 8.dp))
                                    Column {
                                        Text(offer.clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                        Text("${offer.categoryTag} • Tier ${offer.tierLevel}", fontSize = 11.sp, color = Color.LightGray)
                                    }
                                }

                                Button(
                                    onClick = { viewModel.signLogisticsContract(instanceId, offer.id) },
                                    enabled = logisticsData.activeContracts.size < 3,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF0284C7),
                                        disabledContainerColor = Color(0xFF334155)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Tanda Tangani", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Aliran Paket Masuk", fontSize = 10.sp, color = Color.Gray)
                                    Text("+${offer.inboundPackagesPerSec} pkt/dtk", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                }
                                Column {
                                    Text("Bayaran per Paket", fontSize = 10.sp, color = Color.Gray)
                                    Text("$${offer.payoutPerPackage}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                                }
                                Column {
                                    Text("Target Total", fontSize = 10.sp, color = Color.Gray)
                                    Text("${offer.targetTotalPackages} Pkt", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column {
                                    Text("Bonus Selesai", fontSize = 10.sp, color = Color.Gray)
                                    Text("+${formatCurrencyRingkas(offer.completionBonusCash.toDouble(), useShortFormat)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                }
                            }
                        }
                    }
                }
            }

            // ==============================================================
            // 6. TAB 2: PUSAT RISET & TECH TREE
            // ==============================================================
            if (selectedTab == 2) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Riset Strategis & Ekspresi Teknologi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Kembangkan efisiensi bahan bakar ramah lingkungan atau otomasi rute AI berkecepatan tinggi.",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                // Branch A: Eco-Friendly Fleet
                item {
                    val ecoLevel = logisticsData.techTree.ecoLevel
                    val nextCost = when (ecoLevel) {
                        0 -> 25_000L
                        1 -> 75_000L
                        2 -> 200_000L
                        else -> 0L
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF132A1C)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🌱", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                                    Column {
                                        Text("Cabang Eco-Friendly Fleet", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                        Text("Level $ecoLevel / 3", fontSize = 11.sp, color = Color(0xFF34D399))
                                    }
                                }

                                if (ecoLevel < 3) {
                                    Button(
                                        onClick = { viewModel.researchLogisticsTech(instanceId, "ECO") },
                                        enabled = logisticsData.internalCash >= nextCost,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF059669),
                                            disabledContainerColor = Color(0xFF334155)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Riset (${formatCurrencyRingkas(nextCost.toDouble(), useShortFormat)})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF065F46))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("MAX TIER", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF34D399))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            TechStepRow(
                                stepNumber = 1,
                                title = "Biosolar B35 Fuel Optimizer",
                                desc = "Mengurangi biaya operasional trip armada sebesar -15%.",
                                isUnlocked = ecoLevel >= 1
                            )
                            TechStepRow(
                                stepNumber = 2,
                                title = "Hybrid Drivetrain & Durability",
                                desc = "Biaya operasional -30% dan ketahanan HP aus berkurang -20%.",
                                isUnlocked = ecoLevel >= 2
                            )
                            TechStepRow(
                                stepNumber = 3,
                                title = "Zero-Emission Electric Hub",
                                desc = "Membuka pembelian armada Electric Semi-Truck (-50% biaya).",
                                isUnlocked = ecoLevel >= 3
                            )
                        }
                    }
                }

                // Branch B: Hyper-Speed AI Logistics
                item {
                    val aiLevel = logisticsData.techTree.aiSpeedLevel
                    val nextCost = when (aiLevel) {
                        0 -> 35_000L
                        1 -> 100_000L
                        2 -> 250_000L
                        else -> 0L
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🤖", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                                    Column {
                                        Text("Cabang AI & Hyper-Speed", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                        Text("Level $aiLevel / 3", fontSize = 11.sp, color = Color(0xFF38BDF8))
                                    }
                                }

                                if (aiLevel < 3) {
                                    Button(
                                        onClick = { viewModel.researchLogisticsTech(instanceId, "AI") },
                                        enabled = logisticsData.internalCash >= nextCost,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF0284C7),
                                            disabledContainerColor = Color(0xFF334155)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Riset (${formatCurrencyRingkas(nextCost.toDouble(), useShortFormat)})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF0369A1))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("MAX TIER", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            TechStepRow(
                                stepNumber = 1,
                                title = "GPS & AI Dynamic Routing",
                                desc = "Mempercepat seluruh laju perjalanan armada sebesar +20%.",
                                isUnlocked = aiLevel >= 1
                            )
                            TechStepRow(
                                stepNumber = 2,
                                title = "Auto-Dispatch Logistics AI",
                                desc = "Otomatis menugaskan armada standby saat gudang memiliki paket.",
                                isUnlocked = aiLevel >= 2
                            )
                            TechStepRow(
                                stepNumber = 3,
                                title = "Autonomous Drone Cargo Network",
                                desc = "Membuka pembelian armada Drone Kargo AI (pengiriman kilat 5s).",
                                isUnlocked = aiLevel >= 3
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ==============================================================
    // DIALOG 1: BELI ARMADA BARU
    // ==============================================================
    if (showBuyVehicleDialog) {
        var selectedVehicleType by remember { mutableStateOf(LogisticsVehicleType.MOTOR_COURIER) }
        var vehicleCustomName by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showBuyVehicleDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("Beli Armada Pengiriman Baru", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Pilih tipe armada yang sesuai kebutuhan kapasitas dan rute:", fontSize = 12.sp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(10.dp))

                    LogisticsVehicleType.values().forEach { type ->
                        val isLocked = (type.requiredTechPath == "ECO_3" && logisticsData.techTree.ecoLevel < 3) ||
                                (type.requiredTechPath == "AI_3" && logisticsData.techTree.aiSpeedLevel < 3)

                        val isSelected = selectedVehicleType == type

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !isLocked) { selectedVehicleType = type },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.3f) else Color(0xFF0F172A)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF38BDF8) else if (isLocked) Color(0xFF334155) else Color(0xFF475569)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Text(type.iconEmoji, fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                                    Column {
                                        Text(
                                            text = type.displayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (isLocked) Color.Gray else Color.White
                                        )
                                        Text(
                                            text = if (isLocked) "🔒 Butuh Riset ${type.requiredTechPath}" else "Kapasitas ${type.capacity} pkt • ${type.baseTripSeconds.toInt()}s tempuh",
                                            fontSize = 11.sp,
                                            color = if (isLocked) Color(0xFFFF5252) else Color.LightGray
                                        )
                                    }
                                }
                                Text(
                                    text = formatCurrencyRingkas(type.buyCost.toDouble(), useShortFormat),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isLocked) Color.Gray else Color(0xFF00E676)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = vehicleCustomName,
                        onValueChange = { vehicleCustomName = it },
                        label = { Text("Nama Kustom Armada (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                val isLocked = (selectedVehicleType.requiredTechPath == "ECO_3" && logisticsData.techTree.ecoLevel < 3) ||
                        (selectedVehicleType.requiredTechPath == "AI_3" && logisticsData.techTree.aiSpeedLevel < 3)
                val canAfford = logisticsData.internalCash >= selectedVehicleType.buyCost && !isLocked

                Button(
                    onClick = {
                        viewModel.buyFleetVehicle(instanceId, selectedVehicleType, vehicleCustomName)
                        showBuyVehicleDialog = false
                    },
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text("Beli Sekarang", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBuyVehicleDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // ==============================================================
    // DIALOG 2: SUNTIK MODAL / TARIK PROFIT
    // ==============================================================
    if (showCapitalDialog) {
        var inputAmountStr by remember { mutableStateOf("") }
        val isSuntik = capitalActionType == "suntik"
        val maxAvailable = if (isSuntik) playerState.cash else logisticsData.internalCash

        AlertDialog(
            onDismissRequest = { showCapitalDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text(
                    text = if (isSuntik) "Suntik Modal Kas Logistik" else "Tarik Profit ke Kas Utama",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (isSuntik) "Transfer dana dari kas utama ke kas operasional Express Logistics." else "Tarik dana dari kas Express Logistics ke kas pribadi pemain.",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Saldo tersedia: ${formatCurrencyRingkas(maxAvailable.toDouble(), useShortFormat)}",
                        fontSize = 12.sp,
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inputAmountStr,
                        onValueChange = { inputAmountStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah (USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(10_000L, 50_000L, 200_000L).forEach { quickAmt ->
                            OutlinedButton(
                                onClick = { inputAmountStr = quickAmt.toString() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(formatCurrencyRingkas(quickAmt.toDouble(), useShortFormat), fontSize = 11.sp, color = Color.White)
                            }
                        }
                        OutlinedButton(
                            onClick = { inputAmountStr = maxAvailable.toString() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Max", fontSize = 11.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                val amt = inputAmountStr.toLongOrNull() ?: 0L
                val isValid = amt in 1..maxAvailable

                Button(
                    onClick = {
                        if (isSuntik) {
                            viewModel.injectCapitalToLogistics(instanceId, amt)
                        } else {
                            viewModel.withdrawCapitalFromLogistics(instanceId, amt)
                        }
                        showCapitalDialog = false
                    },
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuntik) Color(0xFF0284C7) else Color(0xFF00E676)
                    )
                ) {
                    Text(if (isSuntik) "Suntikkan" else "Tarik Dana", fontWeight = FontWeight.Bold, color = if (isSuntik) Color.White else Color.Black)
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
    // DIALOG 3: KONFIRMASI UPGRADE GUDANG
    // ==============================================================
    if (showUpgradeWarehouseDialog) {
        val currentLvl = logisticsData.warehouseLevel
        val nextCap = getWarehouseCapacityForLevel(currentLvl + 1)
        val cost = getWarehouseUpgradeCost(currentLvl)
        val canAfford = logisticsData.internalCash >= cost

        AlertDialog(
            onDismissRequest = { showUpgradeWarehouseDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("Upgrade Gudang Pusat", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Tingkatkan fasilitas gudang dari Level $currentLvl ke Level ${currentLvl + 1}?",
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Kapasitas Baru: $nextCap Paket (Sebelumnya: ${logisticsData.warehouseCapacity})",
                        fontSize = 12.sp,
                        color = Color(0xFF00E676),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• Biaya Upgrade: ${formatCurrencyRingkas(cost.toDouble(), useShortFormat)}",
                        fontSize = 12.sp,
                        color = Color(0xFFFFD600)
                    )
                    Text(
                        text = "• Saldo Kas Internal: ${formatCurrencyRingkas(logisticsData.internalCash.toDouble(), useShortFormat)}",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.upgradeWarehouseCapacity(instanceId)
                        showUpgradeWarehouseDialog = false
                    },
                    enabled = canAfford,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Text("Upgrade Sekarang", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeWarehouseDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        )
    }

    // ==============================================================
    // 6. PENGATURAN BISNIS BOTTOM SHEET
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
                            text = "Pengaturan Bisnis",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = business.customName ?: "Express (Logistics)",
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
                            Text("Estimasi Nilai Likuidasi", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = formatCurrencyRingkas(valuationBreakdown.totalValuation.toDouble(), useShortFormat),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676)
                            )
                        }
                        Text(
                            text = "Efisiensi: ${logisticsData.successRate.toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (logisticsData.successRate >= 90.0) Color(0xFF00E676) else Color(0xFFFFD600)
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
                                text = "Likuidasi & Jual Unit Bisnis",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Jual seluruh unit logistik, armada kendaraan, dan cairkan saldo kas ke Saldo Perusahaan Utama.",
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
    // 7. DANGER ZONE LIQUIDATION CONFIRMATION MODAL
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
                        text = "Likuidasi Bisnis Logistik",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444),
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Total Valuasi Bisnis Anda:",
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
                                Text("• Armada (${logisticsData.fleet.size} unit):", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    formatCurrencyRingkas(valuationBreakdown.fleetValue.toDouble(), useShortFormat),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• Hub & Riset Tech:", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    formatCurrencyRingkas(valuationBreakdown.warehouseTechValue.toDouble(), useShortFormat),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• Goodwill / Kinerja (${logisticsData.successRate.toInt()}%):", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    "${String.format("%.2f", valuationBreakdown.performanceMultiplier)}x",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (valuationBreakdown.performanceMultiplier >= 1.0) Color(0xFF00E676) else Color(0xFFFF5252)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "⚠️ PERINGATAN: Semua aset, armada, atau unit di dalam bisnis ini akan dihapus permanen. Uang valuasi akan ditransfer ke Saldo Perusahaan Utama.",
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
                        val amount = viewModel.liquidateLogisticsBusiness(instanceId)
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
    // 8. LIQUIDATION SUCCESS TRANSITION MODAL
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
                        text = "Bisnis Berhasil Terjual!",
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
                        text = "🎉 DANA LIKUIDASI DICAIRKAN",
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
                        text = "Uang hasil penjualan bisnis telah berhasil ditransfer ke Saldo Perusahaan Utama Anda.",
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

@Composable
fun TechStepRow(
    stepNumber: Int,
    title: String,
    desc: String,
    isUnlocked: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) Color(0xFF00E676) else Color(0xFF334155)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) Color.Black else Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (isUnlocked) Color.White else Color.Gray
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = if (isUnlocked) Color.LightGray else Color.Gray
            )
        }
    }
}
