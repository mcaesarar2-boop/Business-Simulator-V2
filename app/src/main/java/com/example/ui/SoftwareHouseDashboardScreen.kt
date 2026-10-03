package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.*
import com.example.viewmodel.GameViewModel
import java.text.NumberFormat
import java.util.Locale

// Custom Cyber/DevOps Palette
private val DevBgDark = Color(0xFF0B0F19)
private val DevCardDark = Color(0xFF131B2E)
private val DevNeonCyan = Color(0xFF06B6D4)
private val DevNeonEmerald = Color(0xFF10B981)
private val DevNeonPink = Color(0xFFEC4899)
private val DevNeonAmber = Color(0xFFF59E0B)
private val DevNeonPurple = Color(0xFF8B5CF6)
private val DevNeonRed = Color(0xFFEF4444)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftwareHouseDashboardScreen(
    navController: NavController,
    viewModel: GameViewModel,
    instanceId: String
) {
    val playerState by viewModel.playerState.collectAsState()
    val isShortFormat = true

    // Find owned upper_tech business
    var isNested = false
    var holdingId: String? = null
    var ownedBusiness = playerState.ownedBusinesses.find { it.instanceId == instanceId }
    if (ownedBusiness == null) {
        for (h in playerState.holdingCompanies) {
            ownedBusiness = h.subsidiaries.find { it.instanceId == instanceId }
            if (ownedBusiness != null) {
                isNested = true
                holdingId = h.instanceId
                break
            }
        }
    }

    if (ownedBusiness == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("DevOps Command Center") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Unit bisnis Software House tidak ditemukan.")
            }
        }
        return
    }

    val softData = ownedBusiness.softwareHouseData
    val appProjects = playerState.appProjects

    // Team calculation
    val assignedUiUx = appProjects.filter { it.kanbanColumn == "IN_PROGRESS" || it.isAssigned }.sumOf { it.assignedUiUx }
    val assignedFe = appProjects.filter { it.kanbanColumn == "IN_PROGRESS" || it.isAssigned }.sumOf { it.assignedFrontend }
    val assignedBe = appProjects.filter { it.kanbanColumn == "IN_PROGRESS" || it.isAssigned }.sumOf { it.assignedBackend }

    val totalDevs = softData.uiUxDesigners + softData.frontendDevelopers + softData.backendEngineers
    val totalAssigned = assignedUiUx + assignedFe + assignedBe
    val totalIdle = (totalDevs - totalAssigned).coerceAtLeast(0)

    // SaaS metrics calculation
    val liveSaaS = appProjects.filter { (it.status == ProjectStatus.MAINTENANCE || it.kanbanColumn == "DEPLOYED") && it.type == ProjectType.INDEPENDENT_SAAS && !it.isBugFixTask }
    val totalMRR = liveSaaS.sumOf { it.currentMrr }
    val totalActiveUsers = liveSaaS.sumOf { it.activeUsers }
    val effectiveUsers = if (softData.hasMicroservices) (totalActiveUsers * 0.7).toLong() else totalActiveUsers
    val serverCapacity = softData.maxServerCapacity
    val serverLoadPercent = if (serverCapacity > 0) ((effectiveUsers.toFloat() / serverCapacity.toFloat()) * 100f).coerceIn(0f, 150f) else 0f
    val isServerOverloaded = effectiveUsers > serverCapacity

    // UI state
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Kanban Board", "B2B Market", "SaaS Portfolio", "Synergy Hub", "Dev Team", "Tech & Infra")
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    var showAssignDialogForProject by remember { mutableStateOf<AppProject?>(null) }
    var showCashDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showLiquidationDialog by remember { mutableStateOf(false) }
    var liquidationConfirmInput by remember { mutableStateOf("") }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DevBgDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. HERO HEADER WITH STOCK-FREE DEVOPS BANNER & TITLE
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = "https://plus.unsplash.com/premium_photo-1664297989345-f4ff2063b212?q=80&w=1098&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                        contentDescription = "Server Room & Dev Ops Command Center",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x990B0F19),
                                        Color(0xDF0B0F19),
                                        DevBgDark
                                    )
                                )
                            )
                    )

                    // Top Bar Nav & Title
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0x66000000), CircleShape)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = Color.White
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0x991E293B),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DevNeonEmerald.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .clickable { showCashDialog = true }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = DevNeonEmerald,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Kas: $${formatDevMoney(ownedBusiness.companyCash.toLong(), isShortFormat)}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Box {
                                    IconButton(
                                        onClick = { showSettingsMenu = true },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color(0x66000000), CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = "Pengaturan Bisnis",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = showSettingsMenu,
                                        onDismissRequest = { showSettingsMenu = false },
                                        modifier = Modifier.background(DevCardDark)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.DeleteForever,
                                                        contentDescription = null,
                                                        tint = DevNeonRed,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Likuidasi Bisnis (Jual)", color = DevNeonRed, fontWeight = FontWeight.Bold)
                                                }
                                            },
                                            onClick = {
                                                showSettingsMenu = false
                                                liquidationConfirmInput = ""
                                                showLiquidationDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = DevNeonCyan.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DevNeonCyan)
                                ) {
                                    Text(
                                        "DEVOPS COMMAND CENTER",
                                        color = DevNeonCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isServerOverloaded) DevNeonRed.copy(alpha = 0.2f) else DevNeonEmerald.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isServerOverloaded) DevNeonRed else DevNeonEmerald)
                                ) {
                                    Text(
                                        if (isServerOverloaded) "SERVER OUTAGE" else "SYSTEM STABLE",
                                        color = if (isServerOverloaded) DevNeonRed else DevNeonEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                ownedBusiness.name,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // 2. THREE KEY HERO METRICS (MRR, SERVER LOAD, DEV CAPACITY)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Metric 1: Total MRR
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DevCardDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DevNeonEmerald.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = DevNeonEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Total MRR", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$${formatDevMoney(totalMRR.toLong(), isShortFormat)}/bln",
                                color = DevNeonEmerald,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text("${liveSaaS.size} SaaS Aktif", color = Color(0xFFA7F3D0), fontSize = 10.sp)
                        }
                    }

                    // Metric 2: Server Load
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DevCardDark),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isServerOverloaded) DevNeonRed.copy(alpha = 0.6f) else DevNeonCyan.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudQueue, contentDescription = null, tint = if (isServerOverloaded) DevNeonRed else DevNeonCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cloud Load", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${serverLoadPercent.toInt()}% Terpakai",
                                color = if (isServerOverloaded) DevNeonRed else Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            LinearProgressIndicator(
                                progress = { (serverLoadPercent / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (isServerOverloaded) DevNeonRed else DevNeonCyan,
                                trackColor = Color(0xFF334155)
                            )
                        }
                    }

                    // Metric 3: Dev Capacity
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DevCardDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DevNeonPurple.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Groups, contentDescription = null, tint = DevNeonPurple, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Dev Team", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$totalIdle Idle",
                                color = if (totalIdle > 0) DevNeonPurple else Color.LightGray,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text("$totalDevs Kru Total", color = Color(0xFFDDD6FE), fontSize = 10.sp)
                        }
                    }
                }
            }

            // 3. NAVIGATION TABS
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DevCardDark,
                    contentColor = DevNeonCyan,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = DevNeonCyan,
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
                                    title,
                                    color = if (selectedTab == index) DevNeonCyan else Color.Gray,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 4. TAB CONTENTS
            when (selectedTab) {
                0 -> {
                    // TAB 0: KANBAN BOARD (Backlog -> In Progress -> Deployed)
                    item {
                        KanbanBoardView(
                            appProjects = appProjects,
                            softData = softData,
                            isShortFormat = isShortFormat,
                            onAssignClick = { project -> showAssignDialogForProject = project },
                            onUnassignClick = { project ->
                                snackbarMessage = viewModel.unassignTeamFromProject(instanceId, project.id)
                            },
                            onCancelBacklog = { project ->
                                snackbarMessage = viewModel.cancelBacklogProject(project.id)
                            },
                            onSellSaaS = { project ->
                                snackbarMessage = viewModel.sellSaaSProject(project.id, instanceId)
                            }
                        )
                    }
                }
                1 -> {
                    // TAB 1: B2B MARKET
                    item {
                        B2BMarketView(
                            softData = softData,
                            isShortFormat = isShortFormat,
                            idleUiUx = (softData.uiUxDesigners - assignedUiUx).coerceAtLeast(0),
                            idleFe = (softData.frontendDevelopers - assignedFe).coerceAtLeast(0),
                            idleBe = (softData.backendEngineers - assignedBe).coerceAtLeast(0),
                            onStartDirect = { title, budget, rev, duration, reqUiUx, reqFe, reqBe, desc, client, tag ->
                                snackbarMessage = viewModel.startProjectDirectly(
                                    instanceId = instanceId,
                                    title = title,
                                    type = ProjectType.CLIENT_B2B,
                                    budgetCost = budget,
                                    targetRevenue = rev,
                                    devTimeMonths = duration,
                                    targetBusinessId = null,
                                    reqUiUx = reqUiUx,
                                    reqFrontend = reqFe,
                                    reqBackend = reqBe,
                                    description = desc,
                                    clientName = client,
                                    categoryTag = tag
                                )
                            },
                            onAddToBacklog = { title, budget, rev, duration, reqUiUx, reqFe, reqBe, desc, client, tag ->
                                snackbarMessage = viewModel.addProjectToBacklog(
                                    title = title,
                                    type = ProjectType.CLIENT_B2B,
                                    budgetCost = budget,
                                    targetRevenue = rev,
                                    devTimeMonths = duration,
                                    targetBusinessId = null,
                                    reqUiUx = reqUiUx,
                                    reqFrontend = reqFe,
                                    reqBackend = reqBe,
                                    description = desc,
                                    clientName = client,
                                    categoryTag = tag
                                )
                            }
                        )
                    }
                }
                2 -> {
                    // TAB 2: SAAS PORTFOLIO
                    item {
                        SaaSPortfolioView(
                            appProjects = appProjects,
                            softData = softData,
                            isShortFormat = isShortFormat,
                            idleUiUx = (softData.uiUxDesigners - assignedUiUx).coerceAtLeast(0),
                            idleFe = (softData.frontendDevelopers - assignedFe).coerceAtLeast(0),
                            idleBe = (softData.backendEngineers - assignedBe).coerceAtLeast(0),
                            onBuildSaaS = { title, budget, mrr, duration, reqUiUx, reqFe, reqBe, desc ->
                                snackbarMessage = viewModel.startProjectDirectly(
                                    instanceId = instanceId,
                                    title = title,
                                    type = ProjectType.INDEPENDENT_SAAS,
                                    budgetCost = budget,
                                    targetRevenue = mrr,
                                    devTimeMonths = duration,
                                    targetBusinessId = null,
                                    reqUiUx = reqUiUx,
                                    reqFrontend = reqFe,
                                    reqBackend = reqBe,
                                    description = desc,
                                    clientName = "Internal SaaS Product",
                                    categoryTag = "Cloud SaaS"
                                )
                            },
                            onSellSaaS = { project ->
                                snackbarMessage = viewModel.sellSaaSProject(project.id, instanceId)
                            }
                        )
                    }
                }
                3 -> {
                    // TAB 3: SYNERGY HUB
                    item {
                        SynergyHubView(
                            ownedBusinesses = playerState.ownedBusinesses + playerState.holdingCompanies.flatMap { it.subsidiaries },
                            appProjects = appProjects,
                            softData = softData,
                            isShortFormat = isShortFormat,
                            idleUiUx = (softData.uiUxDesigners - assignedUiUx).coerceAtLeast(0),
                            idleFe = (softData.frontendDevelopers - assignedFe).coerceAtLeast(0),
                            idleBe = (softData.backendEngineers - assignedBe).coerceAtLeast(0),
                            onBuildSynergy = { title, targetId, budget, duration, reqUiUx, reqFe, reqBe, desc ->
                                snackbarMessage = viewModel.startProjectDirectly(
                                    instanceId = instanceId,
                                    title = title,
                                    type = ProjectType.ECOSYSTEM_SYNERGY,
                                    budgetCost = budget,
                                    targetRevenue = 0.0,
                                    devTimeMonths = duration,
                                    targetBusinessId = targetId,
                                    reqUiUx = reqUiUx,
                                    reqFrontend = reqFe,
                                    reqBackend = reqBe,
                                    description = desc,
                                    clientName = "Internal Holding Synergy",
                                    categoryTag = "Ecosystem Synergy"
                                )
                            }
                        )
                    }
                }
                4 -> {
                    // TAB 4: DEV TEAM (SDM)
                    item {
                        DevTeamManagementView(
                            softData = softData,
                            assignedUiUx = assignedUiUx,
                            assignedFe = assignedFe,
                            assignedBe = assignedBe,
                            isShortFormat = isShortFormat,
                            onHire = { role, useCompanyCash ->
                                snackbarMessage = viewModel.hireDevSpecialist(instanceId, role, useCompanyCash)
                            },
                            onFire = { role ->
                                snackbarMessage = viewModel.fireDevSpecialist(instanceId, role)
                            }
                        )
                    }
                }
                5 -> {
                    // TAB 5: TECH & INFRASTRUCTURE
                    item {
                        TechInfrastructureView(
                            softData = softData,
                            isShortFormat = isShortFormat,
                            onUpgrade = { techType, useCompanyCash ->
                                snackbarMessage = viewModel.upgradeTechInfrastructure(instanceId, techType, useCompanyCash)
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Modal: Assign Team to Backlog Project
    showAssignDialogForProject?.let { proj ->
        var reqUiUx by remember { mutableIntStateOf(proj.requiredUiUx) }
        var reqFe by remember { mutableIntStateOf(proj.requiredFrontend) }
        var reqBe by remember { mutableIntStateOf(proj.requiredBackend) }

        val idleUiUx = (softData.uiUxDesigners - assignedUiUx).coerceAtLeast(0)
        val idleFe = (softData.frontendDevelopers - assignedFe).coerceAtLeast(0)
        val idleBe = (softData.backendEngineers - assignedBe).coerceAtLeast(0)

        AlertDialog(
            onDismissRequest = { showAssignDialogForProject = null },
            containerColor = DevCardDark,
            title = {
                Text(
                    "Assign Tim Developer",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        proj.title,
                        color = DevNeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        "Kebutuhan proyek: ${proj.requiredUiUx} UX, ${proj.requiredFrontend} Frontend, ${proj.requiredBackend} Backend",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Counter for UI/UX
                    SpecialistAssignmentRow(
                        title = "UI/UX Designer",
                        badgeColor = DevNeonPink,
                        current = reqUiUx,
                        available = idleUiUx,
                        onDecrement = { if (reqUiUx > 0) reqUiUx-- },
                        onIncrement = { if (reqUiUx < idleUiUx) reqUiUx++ }
                    )

                    // Counter for Frontend
                    SpecialistAssignmentRow(
                        title = "Frontend Dev (React/Next)",
                        badgeColor = DevNeonCyan,
                        current = reqFe,
                        available = idleFe,
                        onDecrement = { if (reqFe > 0) reqFe-- },
                        onIncrement = { if (reqFe < idleFe) reqFe++ }
                    )

                    // Counter for Backend
                    SpecialistAssignmentRow(
                        title = "Backend/AI Engineer (Py)",
                        badgeColor = DevNeonEmerald,
                        current = reqBe,
                        available = idleBe,
                        onDecrement = { if (reqBe > 0) reqBe-- },
                        onIncrement = { if (reqBe < idleBe) reqBe++ }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        snackbarMessage = viewModel.assignTeamToProject(instanceId, proj.id, reqUiUx, reqFe, reqBe)
                        showAssignDialogForProject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevNeonCyan)
                ) {
                    Text("Mulai Pengerjaan", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignDialogForProject = null }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    // Modal: Cash Injection / Dividend Transfer
    if (showCashDialog) {
        var transferAmount by remember { mutableStateOf("50000") }
        AlertDialog(
            onDismissRequest = { showCashDialog = false },
            containerColor = DevCardDark,
            title = { Text("Kas Perusahaan Software House", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Kas Perusahaan: $${formatDevMoney(ownedBusiness.companyCash.toLong(), isShortFormat)}", color = DevNeonEmerald, fontWeight = FontWeight.Bold)
                    Text("Saldo Pribadi: $${formatDevMoney(playerState.cash, isShortFormat)}", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = transferAmount,
                        onValueChange = { transferAmount = it },
                        label = { Text("Nominal ($)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DevNeonCyan,
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = transferAmount.toLongOrNull() ?: 0L
                        if (amount > 0) {
                            val success = viewModel.injectCapitalToBusiness(instanceId, amount)
                            snackbarMessage = if (success) "Suntik dana sebesar $${formatDevMoney(amount, isShortFormat)} berhasil!" else "Saldo pribadi tidak cukup."
                        }
                        showCashDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DevNeonEmerald)
                ) {
                    Text("Suntik Dana", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val amount = transferAmount.toLongOrNull() ?: 0L
                        if (amount > 0) {
                            val success = viewModel.withdrawCapitalFromBusiness(instanceId, amount)
                            snackbarMessage = if (success) "Tarik dividen sebesar $${formatDevMoney(amount, isShortFormat)} berhasil!" else "Kas perusahaan tidak cukup."
                        }
                        showCashDialog = false
                    }
                ) {
                    Text("Tarik Dividen", color = DevNeonAmber)
                }
            }
        )
    }

    // Modal: Danger Zone Likuidasi Bisnis
    if (showLiquidationDialog) {
        val liveSaaS = playerState.appProjects.filter { 
            (it.status == ProjectStatus.MAINTENANCE || it.kanbanColumn == "DEPLOYED") && 
            it.type == ProjectType.INDEPENDENT_SAAS && !it.isBugFixTask 
        }
        val totalActiveMrr = liveSaaS.sumOf { it.currentMrr }.toLong()
        val annualArr = totalActiveMrr * 12L
        val totalDevCount = softData.uiUxDesigners + softData.frontendDevelopers + softData.backendEngineers
        val devTeamAssetVal = totalDevCount * 5_000L
        val internalCashVal = ownedBusiness.companyCash.toLong()
        val totalValuation = viewModel.calculateSoftwareHouseValuation(ownedBusiness, playerState.appProjects)

        AlertDialog(
            onDismissRequest = { showLiquidationDialog = false },
            containerColor = Color(0xFF1E1E2E),
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(DevNeonRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Peringatan", tint = DevNeonRed, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Danger Zone: Likuidasi Bisnis", color = DevNeonRed, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Unit Bisnis: ${ownedBusiness.customName ?: "Software Developer"}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Valuation Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DevNeonEmerald.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("TOTAL VALUASI BISNIS ANDA", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$${String.format("%,d", totalValuation)}",
                                color = DevNeonEmerald,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Divider(color = Color.DarkGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Kas Internal Bisnis:", color = Color.LightGray, fontSize = 11.sp)
                                Text("$${String.format("%,d", internalCashVal)}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Annual ARR (MRR $${String.format("%,d", totalActiveMrr)}/bln x 12):", color = Color.LightGray, fontSize = 11.sp)
                                Text("$${String.format("%,d", annualArr)}", color = DevNeonCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• Aset Tim ($totalDevCount Dev x $5k):", color = Color.LightGray, fontSize = 11.sp)
                                Text("$${String.format("%,d", devTeamAssetVal)}", color = DevNeonAmber, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Warning Text
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DevNeonRed.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DevNeonRed.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ Tindakan ini permanen. Seluruh aset, proyek, dan tim akan dihapus. Uang valuasi akan ditransfer ke Saldo Perusahaan Utama.",
                            color = Color(0xFFFF8A9E),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp),
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Ketik \"JUAL\" untuk mengonfirmasi:",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = liquidationConfirmInput,
                        onValueChange = { liquidationConfirmInput = it },
                        placeholder = { Text("Ketik JUAL di sini", color = Color.DarkGray, fontSize = 13.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DevNeonRed,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val isConfirmationValid = liquidationConfirmInput.trim() == "JUAL"
                Button(
                    onClick = {
                        if (isConfirmationValid) {
                            viewModel.liquidateBusiness(instanceId)
                            showLiquidationDialog = false
                            Toast.makeText(context, "Bisnis berhasil dilikuidasi!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                    },
                    enabled = isConfirmationValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DevNeonRed,
                        disabledContainerColor = Color(0xFF334155),
                        contentColor = Color.White,
                        disabledContentColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ya, Jual Bisnis", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLiquidationDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
}

// ----------------------------------------------------
// TAB 0: KANBAN BOARD VIEW
// ----------------------------------------------------
@Composable
fun KanbanBoardView(
    appProjects: List<AppProject>,
    softData: SoftwareHouseCompanyData,
    isShortFormat: Boolean,
    onAssignClick: (AppProject) -> Unit,
    onUnassignClick: (AppProject) -> Unit,
    onCancelBacklog: (AppProject) -> Unit,
    onSellSaaS: (AppProject) -> Unit
) {
    var selectedKanbanTab by remember { mutableIntStateOf(0) }

    val backlogProjects = appProjects.filter { it.kanbanColumn == "BACKLOG" || (!it.isAssigned && it.status == ProjectStatus.DEVELOPMENT) }
    val inProgressProjects = appProjects.filter { it.kanbanColumn == "IN_PROGRESS" || (it.isAssigned && it.status == ProjectStatus.DEVELOPMENT) }
    val deployedProjects = appProjects.filter { it.kanbanColumn == "DEPLOYED" || it.status == ProjectStatus.MAINTENANCE || it.status == ProjectStatus.COMPLETED }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        // Horizontal Kanban Column Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KanbanHeaderPill(
                title = "Backlog",
                count = backlogProjects.size,
                isSelected = selectedKanbanTab == 0,
                color = DevNeonAmber,
                onClick = { selectedKanbanTab = 0 },
                modifier = Modifier.weight(1f)
            )
            KanbanHeaderPill(
                title = "In Progress",
                count = inProgressProjects.size,
                isSelected = selectedKanbanTab == 1,
                color = DevNeonCyan,
                onClick = { selectedKanbanTab = 1 },
                modifier = Modifier.weight(1f)
            )
            KanbanHeaderPill(
                title = "Deployed",
                count = deployedProjects.size,
                isSelected = selectedKanbanTab == 2,
                color = DevNeonEmerald,
                onClick = { selectedKanbanTab = 2 },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Column Cards Render
        when (selectedKanbanTab) {
            0 -> {
                // BACKLOG COLUMN
                if (backlogProjects.isEmpty()) {
                    EmptyKanbanBox("Backlog Kosong", "Ambil kontrak dari B2B Market, SaaS Hub, atau Synergy Hub untuk menambahkan antrean proyek.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        backlogProjects.forEach { proj ->
                            BacklogKanbanCard(
                                project = proj,
                                isShortFormat = isShortFormat,
                                onAssign = { onAssignClick(proj) },
                                onCancel = { onCancelBacklog(proj) }
                            )
                        }
                    }
                }
            }
            1 -> {
                // IN PROGRESS COLUMN
                if (inProgressProjects.isEmpty()) {
                    EmptyKanbanBox("Tidak Ada Proyek Aktif", "Pilih proyek dari Backlog dan assign tim developer untuk mulai coding.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        inProgressProjects.forEach { proj ->
                            InProgressKanbanCard(
                                project = proj,
                                softData = softData,
                                isShortFormat = isShortFormat,
                                onUnassign = { onUnassignClick(proj) }
                            )
                        }
                    }
                }
            }
            2 -> {
                // DEPLOYED COLUMN
                if (deployedProjects.isEmpty()) {
                    EmptyKanbanBox("Belum Ada Produk Deployed", "Selesaikan proyek In Progress untuk meluncurkan SaaS atau menerima payout B2B.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        deployedProjects.forEach { proj ->
                            DeployedKanbanCard(
                                project = proj,
                                isShortFormat = isShortFormat,
                                onSellSaaS = { onSellSaaS(proj) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KanbanHeaderPill(
    title: String,
    count: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else DevCardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else Color(0xFF334155)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                title,
                color = if (isSelected) color else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
                shape = CircleShape,
                color = if (isSelected) color else Color(0xFF1E293B)
            ) {
                Text(
                    "$count",
                    color = if (isSelected) Color.Black else Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun BacklogKanbanCard(
    project: AppProject,
    isShortFormat: Boolean,
    onAssign: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevCardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (project.isBugFixTask) DevNeonRed.copy(alpha = 0.8f) else Color(0xFF334155)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (project.isBugFixTask) DevNeonRed.copy(alpha = 0.2f) else DevNeonAmber.copy(alpha = 0.2f)
                ) {
                    Text(
                        if (project.isBugFixTask) "CRITICAL HOTFIX" else project.categoryTag.uppercase(),
                        color = if (project.isBugFixTask) DevNeonRed else DevNeonAmber,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    "Est: ${project.devTimeMonths} Bln",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                project.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            if (project.description.isNotEmpty()) {
                Text(
                    project.description,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Specialist requirements badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SpecialistBadge("UX: ${project.requiredUiUx}", DevNeonPink)
                SpecialistBadge("FE: ${project.requiredFrontend}", DevNeonCyan)
                SpecialistBadge("BE: ${project.requiredBackend}", DevNeonEmerald)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (project.type == ProjectType.INDEPENDENT_SAAS) "Target MRR" else if (project.type == ProjectType.ECOSYSTEM_SYNERGY) "Synergy Boost" else "Payout Klien",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    Text(
                        if (project.type == ProjectType.ECOSYSTEM_SYNERGY) "+25% Multiplier" else "$${formatDevMoney(project.targetRevenue.toLong(), isShortFormat)}",
                        color = DevNeonEmerald,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = onCancel) {
                        Text("Batal", color = Color.Gray, fontSize = 12.sp)
                    }
                    Button(
                        onClick = onAssign,
                        colors = ButtonDefaults.buttonColors(containerColor = if (project.isBugFixTask) DevNeonRed else DevNeonCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assign & Start", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun InProgressKanbanCard(
    project: AppProject,
    softData: SoftwareHouseCompanyData,
    isShortFormat: Boolean,
    onUnassign: () -> Unit
) {
    val progress = if (project.devTimeMonths > 0) (project.currentMonth.toFloat() / project.devTimeMonths.toFloat()).coerceIn(0f, 1f) else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevCardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, DevNeonCyan.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DevNeonCyan.copy(alpha = 0.2f)
                ) {
                    Text(
                        "IN PROGRESS (${project.currentMonth}/${project.devTimeMonths} BLN)",
                        color = DevNeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Tech Speed Booster indicators
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (softData.hasCiCdPipeline) {
                        Surface(shape = RoundedCornerShape(4.dp), color = DevNeonEmerald.copy(alpha = 0.2f)) {
                            Text("CI/CD +25%", color = DevNeonEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                    if (softData.hasAiCopilot) {
                        Surface(shape = RoundedCornerShape(4.dp), color = DevNeonPurple.copy(alpha = 0.2f)) {
                            Text("AI COPILOT +50%", color = DevNeonPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                project.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = DevNeonCyan,
                trackColor = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Assigned Devs Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Assigned:", color = Color.Gray, fontSize = 11.sp)
                SpecialistBadge("UX: ${project.assignedUiUx}", DevNeonPink)
                SpecialistBadge("FE: ${project.assignedFrontend}", DevNeonCyan)
                SpecialistBadge("BE: ${project.assignedBackend}", DevNeonEmerald)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Target: $${formatDevMoney(project.targetRevenue.toLong(), isShortFormat)}",
                    color = DevNeonEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                OutlinedButton(
                    onClick = onUnassign,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                ) {
                    Text("Tarik Tim / Pause", color = Color.LightGray, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun DeployedKanbanCard(
    project: AppProject,
    isShortFormat: Boolean,
    onSellSaaS: () -> Unit
) {
    val isSaaS = project.type == ProjectType.INDEPENDENT_SAAS && !project.isBugFixTask
    val acquisitionVal = (project.currentMrr * 50).toLong().coerceAtLeast(100_000L)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevCardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, DevNeonEmerald.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DevNeonEmerald.copy(alpha = 0.2f)
                ) {
                    Text(
                        if (isSaaS) "LIVE SAAS IN PRODUCTION" else "PROJECT COMPLETED",
                        color = DevNeonEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                if (isSaaS) {
                    Text(
                        "${project.activeUsers} Users",
                        color = DevNeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                project.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            if (isSaaS) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("MRR: $${formatDevMoney(project.currentMrr.toLong(), isShortFormat)}/bln", color = DevNeonEmerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Churn Rate: ${(project.churnRate * 100).toInt()}%", color = if (project.churnRate > 0.08) DevNeonRed else Color.Gray, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onSellSaaS,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DevNeonAmber),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Exit / Jual SaaS ($${formatDevMoney(acquisitionVal, isShortFormat)} • 50x MRR)",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Payout diterima: $${formatDevMoney(project.targetRevenue.toLong(), isShortFormat)}",
                    color = DevNeonEmerald,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun EmptyKanbanBox(title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131B2E)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.DashboardCustomize, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, color = Color.Gray, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

// ----------------------------------------------------
// TAB 1: B2B MARKET VIEW
// ----------------------------------------------------
@Composable
fun B2BMarketView(
    softData: SoftwareHouseCompanyData,
    isShortFormat: Boolean,
    idleUiUx: Int,
    idleFe: Int,
    idleBe: Int,
    onStartDirect: (String, Double, Double, Int, Int, Int, Int, String, String, String) -> Unit,
    onAddToBacklog: (String, Double, Double, Int, Int, Int, Int, String, String, String) -> Unit
) {
    val b2bContracts = listOf(
        B2BContractTemplate("Website & Brand Portal", 5000.0, 25000.0, 2, 1, 1, 0, "Website korporat modern untuk perusahaan ekspor impor.", "PT Samudra Logistik", "Web Dev"),
        B2BContractTemplate("Sistem Kasir Cloud POS", 12000.0, 55000.0, 3, 1, 1, 2, "Aplikasi POS kasir multi-cabang dengan sinkronisasi inventori.", "Retail Chain Nusantara", "Mobile & Cloud"),
        B2BContractTemplate("Aplikasi Booking Faskes", 25000.0, 110000.0, 4, 1, 2, 2, "Platform pendaftaran & rekam medis klinik terpadu.", "Klinik Medika Utama", "Healthcare App"),
        B2BContractTemplate("Marketplace E-Commerce MVP", 50000.0, 220000.0, 5, 2, 2, 3, "Arsitektur marketplace multivendor dengan payment gateway.", "IndoTrade Digital", "E-Commerce"),
        B2BContractTemplate("AI Customer Support Bot", 80000.0, 350000.0, 5, 1, 2, 4, "LLM Chatbot cerdas untuk automasi tiket komplain 24/7.", "Fintech Unicorn Asia", "AI / LLM"),
        B2BContractTemplate("Core Banking Security Gateway", 150000.0, 650000.0, 6, 2, 3, 5, "Modul enkripsi end-to-end dan otentikasi biometrik perbankan.", "Bank Swasta Nasional", "Fintech / Sec"),
        B2BContractTemplate("Super App Ecosystem", 300000.0, 1400000.0, 8, 3, 4, 6, "Platform terintegrasi ride-hailing, e-wallet, dan merchant loyalty.", "Konglomerasi Multinasional", "Enterprise")
    )

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Kontrak Klien (B2B Agency)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Kerjakan proyek klien untuk mendapatkan payout tunai langsung.", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            b2bContracts.forEach { item ->
                val canStartDirect = idleUiUx >= item.reqUiUx && idleFe >= item.reqFe && idleBe >= item.reqBe

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DevCardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(4.dp), color = DevNeonCyan.copy(alpha = 0.2f)) {
                                Text(item.tag.uppercase(), color = DevNeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Text("Durasi: ${item.durationMonths} Bulan", color = Color.LightGray, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Klien: ${item.clientName}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(item.desc, color = Color.Gray, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SpecialistBadge("UX: ${item.reqUiUx}", DevNeonPink)
                            SpecialistBadge("FE: ${item.reqFe}", DevNeonCyan)
                            SpecialistBadge("BE: ${item.reqBe}", DevNeonEmerald)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Payout Kontrak", color = Color.Gray, fontSize = 10.sp)
                                Text("+$${formatDevMoney(item.targetRevenue.toLong(), isShortFormat)}", color = DevNeonEmerald, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        onAddToBacklog(item.title, item.budgetCost, item.targetRevenue, item.durationMonths, item.reqUiUx, item.reqFe, item.reqBe, item.desc, item.clientName, item.tag)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+ Backlog", color = DevNeonAmber, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = {
                                        onStartDirect(item.title, item.budgetCost, item.targetRevenue, item.durationMonths, item.reqUiUx, item.reqFe, item.reqBe, item.desc, item.clientName, item.tag)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (canStartDirect) DevNeonCyan else Color(0xFF334155)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Ambil & Assign", color = if (canStartDirect) Color.Black else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class B2BContractTemplate(
    val title: String,
    val budgetCost: Double,
    val targetRevenue: Double,
    val durationMonths: Int,
    val reqUiUx: Int,
    val reqFe: Int,
    val reqBe: Int,
    val desc: String,
    val clientName: String,
    val tag: String
)

// ----------------------------------------------------
// TAB 2: SAAS PORTFOLIO VIEW
// ----------------------------------------------------
@Composable
fun SaaSPortfolioView(
    appProjects: List<AppProject>,
    softData: SoftwareHouseCompanyData,
    isShortFormat: Boolean,
    idleUiUx: Int,
    idleFe: Int,
    idleBe: Int,
    onBuildSaaS: (String, Double, Double, Int, Int, Int, Int, String) -> Unit,
    onSellSaaS: (AppProject) -> Unit
) {
    val saasTemplates = listOf(
        SaaSTemplate("Cloud Task & Kanban SaaS", 30000.0, 12000.0, 4, 1, 2, 2, "Aplikasi kolaborasi tim modern dengan model langganan bulanan."),
        SaaSTemplate("AI Image & Video Studio SaaS", 80000.0, 40000.0, 5, 2, 2, 4, "Platform generator visual AI dengan credit package berbasis cloud."),
        SaaSTemplate("Omnichannel POS & ERP SaaS", 140000.0, 75000.0, 6, 2, 3, 4, "SaaS kasir & inventori terintegrasi untuk ribuan merchant ritel."),
        SaaSTemplate("Enterprise Cybersecurity & Auth", 250000.0, 150000.0, 7, 2, 4, 5, "Infrastruktur otentikasi zero-trust & monitoring ancaman siber."),
        SaaSTemplate("Hyperscale Data Lake & AI Ops", 450000.0, 280000.0, 8, 3, 5, 6, "Platform analitik big data enterprise dengan query ultra-cepat.")
    )

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Bangun Produk SaaS (Monthly Recurring Revenue)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Produk SaaS menghasilkan passive income bulanan yang terus bertambah seiring jumlah user.", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            saasTemplates.forEach { item ->
                val canBuild = idleUiUx >= item.reqUiUx && idleFe >= item.reqFe && idleBe >= item.reqBe

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DevCardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DevNeonEmerald.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(4.dp), color = DevNeonEmerald.copy(alpha = 0.2f)) {
                                Text("SAAS STARTUP", color = DevNeonEmerald, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                            Text("Dev Time: ${item.durationMonths} Bulan", color = Color.LightGray, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(item.desc, color = Color.Gray, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SpecialistBadge("UX: ${item.reqUiUx}", DevNeonPink)
                            SpecialistBadge("FE: ${item.reqFe}", DevNeonCyan)
                            SpecialistBadge("BE: ${item.reqBe}", DevNeonEmerald)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Estimasi Base MRR", color = Color.Gray, fontSize = 10.sp)
                                Text("$${formatDevMoney(item.targetMrr.toLong(), isShortFormat)}/bln", color = DevNeonEmerald, fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }

                            Button(
                                onClick = {
                                    onBuildSaaS(item.title, item.budgetCost, item.targetMrr, item.durationMonths, item.reqUiUx, item.reqFe, item.reqBe, item.desc)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (canBuild) DevNeonEmerald else Color(0xFF334155)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Mulai Dev SaaS", color = if (canBuild) Color.Black else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class SaaSTemplate(
    val title: String,
    val budgetCost: Double,
    val targetMrr: Double,
    val durationMonths: Int,
    val reqUiUx: Int,
    val reqFe: Int,
    val reqBe: Int,
    val desc: String
)

// ----------------------------------------------------
// TAB 3: SYNERGY HUB VIEW
// ----------------------------------------------------
@Composable
fun SynergyHubView(
    ownedBusinesses: List<OwnedBusiness>,
    appProjects: List<AppProject>,
    softData: SoftwareHouseCompanyData,
    isShortFormat: Boolean,
    idleUiUx: Int,
    idleFe: Int,
    idleBe: Int,
    onBuildSynergy: (String, String, Double, Int, Int, Int, Int, String) -> Unit
) {
    val synergyCatalog = listOf(
        SynergyDefinition("mid_logistics", "AI Fleet & Route Optimizer App", "Diskon Biaya Armada 20% & +25% Omset", 40000.0, 4, 1, 1, 3, "Optimasi rute kurir & truk ekspedisi secara real-time."),
        SynergyDefinition("upper_realestate", "Smart Apartment & Tenant Access", "+25% Kepuasan Penghuni & Nilai Sewa", 50000.0, 4, 1, 2, 2, "Aplikasi smart key, billing iuran, dan maintenance apartemen."),
        SynergyDefinition("media_tv", "OTT Streaming & VOD Platform", "+25% Viewership & Monetisasi Program TV", 80000.0, 5, 2, 3, 3, "Platform video streaming digital untuk stasiun televisi."),
        SynergyDefinition("shop_department_store", "Omnichannel E-Commerce Superstore", "+25% Gross Penjualan Ritel", 60000.0, 5, 2, 2, 3, "Integrasi belanja online & pickup drive-thru untuk departemen store."),
        SynergyDefinition("healthcare", "Telemedicine & Smart Hospital EHR", "+20% Kapasitas & Pendapatan Medis", 75000.0, 5, 2, 2, 4, "Sistem antrian online dokter dan rekam medis elektronik rumah sakit."),
        SynergyDefinition("tycoon_bank", "Core Banking Mobile & Payment QRIS", "+20% Volume Transaksi Perbankan", 150000.0, 6, 2, 4, 5, "Aplikasi mobile banking modern dengan transfer instan & tabungan digital.")
    )

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Synergy Hub (Ecosystem Multipliers)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Bangun software khusus untuk mendongkrak performa dan margin unit bisnis lain di holding kamu.", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            synergyCatalog.forEach { syn ->
                val targetBiz = ownedBusinesses.find { it.catalogId == syn.targetCatalogId }
                val isAlreadyActive = appProjects.any { it.type == ProjectType.ECOSYSTEM_SYNERGY && it.title == syn.title && (it.status == ProjectStatus.COMPLETED || it.kanbanColumn == "DEPLOYED") }
                val isInProgress = appProjects.any { it.type == ProjectType.ECOSYSTEM_SYNERGY && it.title == syn.title && it.status == ProjectStatus.DEVELOPMENT }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DevCardDark),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAlreadyActive) DevNeonEmerald else if (targetBiz != null) DevNeonPurple.copy(alpha = 0.5f) else Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isAlreadyActive) DevNeonEmerald.copy(alpha = 0.2f) else DevNeonPurple.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    if (isAlreadyActive) "SYNERGY ACTIVE (+25%)" else "HOLDING SYNERGY",
                                    color = if (isAlreadyActive) DevNeonEmerald else DevNeonPurple,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                if (targetBiz != null) "Tersedia (${targetBiz.name})" else "Belum Memiliki Unit",
                                color = if (targetBiz != null) DevNeonCyan else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(syn.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(syn.benefit, color = DevNeonEmerald, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(syn.desc, color = Color.Gray, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SpecialistBadge("UX: ${syn.reqUiUx}", DevNeonPink)
                            SpecialistBadge("FE: ${syn.reqFe}", DevNeonCyan)
                            SpecialistBadge("BE: ${syn.reqBe}", DevNeonEmerald)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Durasi: ${syn.durationMonths} Bulan", color = Color.LightGray, fontSize = 12.sp)

                            if (isAlreadyActive) {
                                Surface(shape = RoundedCornerShape(6.dp), color = DevNeonEmerald.copy(alpha = 0.2f)) {
                                    Text("Aktif di Ekosistem", color = DevNeonEmerald, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                }
                            } else if (isInProgress) {
                                Surface(shape = RoundedCornerShape(6.dp), color = DevNeonCyan.copy(alpha = 0.2f)) {
                                    Text("Sedang Dikerjakan...", color = DevNeonCyan, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (targetBiz != null) {
                                            onBuildSynergy(syn.title, targetBiz.instanceId, syn.budgetCost, syn.durationMonths, syn.reqUiUx, syn.reqFe, syn.reqBe, syn.desc)
                                        }
                                    },
                                    enabled = targetBiz != null,
                                    colors = ButtonDefaults.buttonColors(containerColor = DevNeonPurple),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Bangun Sinergi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class SynergyDefinition(
    val targetCatalogId: String,
    val title: String,
    val benefit: String,
    val budgetCost: Double,
    val durationMonths: Int,
    val reqUiUx: Int,
    val reqFe: Int,
    val reqBe: Int,
    val desc: String
)

// ----------------------------------------------------
// TAB 4: DEV TEAM MANAGEMENT (SDM)
// ----------------------------------------------------
@Composable
fun DevTeamManagementView(
    softData: SoftwareHouseCompanyData,
    assignedUiUx: Int,
    assignedFe: Int,
    assignedBe: Int,
    isShortFormat: Boolean,
    onHire: (String, Boolean) -> Unit,
    onFire: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Manajemen SDM & Kru Developer", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Rekrut spesialis untuk meningkatkan throughput pengerjaan proyek dan skalabilitas SaaS.", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(14.dp))

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DevCardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, DevNeonPurple.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Total Beban Gaji Bulanan", color = Color.Gray, fontSize = 12.sp)
                Text("$${formatDevMoney(softData.totalTeamSalaries, isShortFormat)}/bln", color = DevNeonEmerald, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Role 1: UI/UX Designer
        SpecialistCard(
            roleName = "UI/UX Designer",
            roleKey = "UI_UX",
            badgeColor = DevNeonPink,
            count = softData.uiUxDesigners,
            assigned = assignedUiUx,
            salary = 4000L,
            hireCost = 8000L,
            icon = Icons.Default.Palette,
            isShortFormat = isShortFormat,
            onHire = onHire,
            onFire = onFire
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Role 2: Frontend Developer
        SpecialistCard(
            roleName = "Frontend Developer (React/Next)",
            roleKey = "FRONTEND",
            badgeColor = DevNeonCyan,
            count = softData.frontendDevelopers,
            assigned = assignedFe,
            salary = 6000L,
            hireCost = 12000L,
            icon = Icons.Default.Code,
            isShortFormat = isShortFormat,
            onHire = onHire,
            onFire = onFire
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Role 3: Backend & AI Engineer
        SpecialistCard(
            roleName = "Backend / AI Engineer (Python)",
            roleKey = "BACKEND",
            badgeColor = DevNeonEmerald,
            count = softData.backendEngineers,
            assigned = assignedBe,
            salary = 8000L,
            hireCost = 16000L,
            icon = Icons.Default.Storage,
            isShortFormat = isShortFormat,
            onHire = onHire,
            onFire = onFire
        )
    }
}

@Composable
fun SpecialistCard(
    roleName: String,
    roleKey: String,
    badgeColor: Color,
    count: Int,
    assigned: Int,
    salary: Long,
    hireCost: Long,
    icon: ImageVector,
    isShortFormat: Boolean,
    onHire: (String, Boolean) -> Unit,
    onFire: (String) -> Unit
) {
    val idle = (count - assigned).coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevCardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = badgeColor.copy(alpha = 0.2f)) {
                        Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.padding(8.dp).size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(roleName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Gaji: $${formatDevMoney(salary, isShortFormat)}/bln • Rekrut: $${formatDevMoney(hireCost, isShortFormat)}", color = Color.Gray, fontSize = 11.sp)
                    }
                }

                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1E293B)) {
                    Text(
                        "$count Anggota",
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(4.dp), color = DevNeonEmerald.copy(alpha = 0.15f)) {
                        Text("$idle Idle", color = DevNeonEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Surface(shape = RoundedCornerShape(4.dp), color = DevNeonCyan.copy(alpha = 0.15f)) {
                        Text("$assigned Sibuk", color = DevNeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { onFire(roleKey) },
                        enabled = count > 0,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("- PHK", color = if (count > 0) DevNeonRed else Color.Gray, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { onHire(roleKey, true) },
                        colors = ButtonDefaults.buttonColors(containerColor = badgeColor),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("+ Rekrut ($${formatDevMoney(hireCost, isShortFormat)})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 5: TECH & INFRASTRUCTURE (TECH TREE)
// ----------------------------------------------------
@Composable
fun TechInfrastructureView(
    softData: SoftwareHouseCompanyData,
    isShortFormat: Boolean,
    onUpgrade: (String, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Text("Tech Stack & Cloud Infrastructure", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Tingkatkan arsitektur sistem untuk menampung jutaan user SaaS dan mempercepat kecepatan coding tim.", color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(14.dp))

        // 1. Cloud Server Scale
        val nextServerCost = when (softData.serverTier + 1) {
            2 -> 25_000L
            3 -> 100_000L
            4 -> 350_000L
            5 -> 1_200_000L
            else -> 0L
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DevCardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, DevNeonCyan.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = DevNeonCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Cloud Server Capacity", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Tier ${softData.serverTier}: ${softData.serverTierName}", color = DevNeonCyan, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Kapasitas Maksimal: ${formatDevMoney(softData.maxServerCapacity, isShortFormat)} Active Users",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
                Text(
                    "Biaya Cloud: $${formatDevMoney(softData.serverMonthlyCost, isShortFormat)}/bulan",
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
                if (softData.serverTier < 5) {
                    Button(
                        onClick = { onUpgrade("SERVER_TIER", true) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DevNeonCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Scale Server ke Tier ${softData.serverTier + 1} ($${formatDevMoney(nextServerCost, isShortFormat)})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Surface(shape = RoundedCornerShape(8.dp), color = DevNeonEmerald.copy(alpha = 0.2f), modifier = Modifier.fillMaxWidth()) {
                        Text("Maksimum Cloud Tier Tercapai (Hyperscale Cluster)", color = DevNeonEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Automated CI/CD Pipeline
        TechTreeCard(
            title = "Automated CI/CD Pipeline",
            desc = "Automasi testing & build deployment. Mempercepat siklus dev In Progress sebesar 25%.",
            cost = 50000L,
            isUnlocked = softData.hasCiCdPipeline,
            icon = Icons.Default.Speed,
            color = DevNeonEmerald,
            isShortFormat = isShortFormat,
            onUnlock = { onUpgrade("CICD", true) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. AI Code Assistant
        TechTreeCard(
            title = "AI Code Assistant (Copilot / Gemini)",
            desc = "Asisten AI cerdas untuk seluruh programmer. Meningkatkan produktivitas coding hingga +50%.",
            cost = 75000L,
            isUnlocked = softData.hasAiCopilot,
            icon = Icons.Default.Psychology,
            color = DevNeonPurple,
            isShortFormat = isShortFormat,
            onUnlock = { onUpgrade("AI_COPILOT", true) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Microservices Architecture
        TechTreeCard(
            title = "Microservices & Distributed Cloud",
            desc = "Pecah monolith menjadi modular microservices. Mengurangi beban server resource per user sebesar 30%.",
            cost = 150000L,
            isUnlocked = softData.hasMicroservices,
            icon = Icons.Default.Hub,
            color = DevNeonCyan,
            isShortFormat = isShortFormat,
            onUnlock = { onUpgrade("MICROSERVICES", true) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Automated QA Suite
        TechTreeCard(
            title = "Automated QA & Security Testing Suite",
            desc = "Zero-flaw testing suite. Menurunkan frekuensi insiden bug/hotfix sebesar 50% dan menekan user churn rate.",
            cost = 90000L,
            isUnlocked = softData.hasAutomatedQa,
            icon = Icons.Default.Shield,
            color = DevNeonPink,
            isShortFormat = isShortFormat,
            onUnlock = { onUpgrade("AUTOMATED_QA", true) }
        )
    }
}

@Composable
fun TechTreeCard(
    title: String,
    desc: String,
    cost: Long,
    isUnlocked: Boolean,
    icon: ImageVector,
    color: Color,
    isShortFormat: Boolean,
    onUnlock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DevCardDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isUnlocked) color else Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                if (isUnlocked) {
                    Surface(shape = RoundedCornerShape(4.dp), color = DevNeonEmerald.copy(alpha = 0.2f)) {
                        Text("ACTIVE", color = DevNeonEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(desc, color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(10.dp))
            if (!isUnlocked) {
                Button(
                    onClick = onUnlock,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Deploy Upgrade ($${formatDevMoney(cost, isShortFormat)})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------
// HELPER COMPOSABLES & FORMATTERS
// ----------------------------------------------------
@Composable
fun SpecialistBadge(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Text(
            text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SpecialistAssignmentRow(
    title: String,
    badgeColor: Color,
    current: Int,
    available: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Tersedia: $available Idle", color = Color.Gray, fontSize = 11.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement, enabled = current > 0) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Kurang", tint = if (current > 0) badgeColor else Color.Gray)
            }
            Text("$current", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            IconButton(onClick = onIncrement, enabled = current < available) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = "Tambah", tint = if (current < available) badgeColor else Color.Gray)
            }
        }
    }
}

private fun formatDevMoney(amount: Long, isShort: Boolean): String {
    if (!isShort) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount)
    }
    return when {
        amount >= 1_000_000_000L -> String.format(Locale.US, "%.1fB", amount / 1_000_000_000.0)
        amount >= 1_000_000L -> String.format(Locale.US, "%.1fM", amount / 1_000_000.0)
        amount >= 1_000L -> String.format(Locale.US, "%.1fK", amount / 1_000.0)
        else -> amount.toString()
    }
}
