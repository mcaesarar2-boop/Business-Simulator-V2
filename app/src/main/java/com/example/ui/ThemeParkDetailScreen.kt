package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.RideTier
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ThemeParkEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeParkDetailScreen(navController: NavController, viewModel: GameViewModel, instanceId: String, branchId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()
    
    var owned = playerState.ownedBusinesses.find { it.instanceId == instanceId }
    if (owned == null) {
        for (holding in playerState.holdingCompanies) {
            owned = holding.subsidiaries.find { it.instanceId == instanceId }
            if (owned != null) break
        }
    }
    
    val branch = owned?.themeParkBranches?.find { it.id == branchId }
    
    if (owned == null || branch == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Data Cabang Tidak Ditemukan", color = Color.White)
        }
        return
    }

    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf(branch.customName ?: "") }
    var branchImageUrlInput by remember { mutableStateOf(branch.imageUrl ?: "") }
    
    var showRideCatalog by remember { mutableStateOf(false) }
    

    var showErrorInsufficientFunds by remember { mutableStateOf(false) }
    
    var showFacilityEditDialog by remember { mutableStateOf(false) }
    var selectedFacilityForEdit by remember { mutableStateOf<com.example.data.ThemeParkFacility?>(null) }
    var facilityEditName by remember { mutableStateOf("") }
    var facilityEditZone by remember { mutableStateOf("") }
    var facilityEditImageUrl by remember { mutableStateOf("") }
    
    var showFacilityDeleteConfirm by remember { mutableStateOf(false) }
    var selectedFacilityForDelete by remember { mutableStateOf<com.example.data.ThemeParkFacility?>(null) }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(false) }
    var sortType by remember { mutableStateOf("TERBARU") }
    var expandSortMenu by remember { mutableStateOf(false) }
    
    var showEditRideDialog by remember { mutableStateOf<com.example.data.ThemeParkRide?>(null) }
    var editRideNameInput by remember { mutableStateOf("") }
    var editRideImageUrlInput by remember { mutableStateOf("") }
    var editRideZoneInput by remember { mutableStateOf<String?>(null) }
    var editRideIpTitleInput by remember { mutableStateOf<String?>(null) }
    var editRideIpScoreInput by remember { mutableStateOf<Int?>(null) }
    
    var showTicketManagement by remember { mutableStateOf(false) }
    var showZoneManagement by remember { mutableStateOf(false) }
    var showFinancialLedger by remember { mutableStateOf(false) }
    
    var showRideDemolishDialog by remember { mutableStateOf<com.example.data.ThemeParkRide?>(null) }
    var showRidePauseDialog by remember { mutableStateOf<com.example.data.ThemeParkRide?>(null) }
    var showRideMaintenanceDialog by remember { mutableStateOf<com.example.data.ThemeParkRide?>(null) }
    var showErrorInsufficientForMaint by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = { Text("Detail Cabang", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showFinancialLedger = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Financial Ledger", tint = Color(0xFF00FFCC))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Branch", tint = Color(0xFFE53935))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            if (branch.remainingBiddingMonths == 0) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = { showRideCatalog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))),
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+ Bangun Wahana Kustom", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Column {
            // Header Top
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (branch.imageUrl != null) {
                        AsyncImage(
                            model = branch.imageUrl,
                            contentDescription = "Header Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (branch.imageUrl == null) Modifier.background(Brush.linearGradient(listOf(Color(0xFF2C2C2C), Color(0xFF151515)))) else Modifier)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            if (branch.customName == null) {
                                Text("Tanpa Nama (${branch.locationName})", color = Color.LightGray, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        renameInput = ""
                                        branchImageUrlInput = ""
                                        showRenameDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.copy(alpha = 0.05f),
                                        contentColor = Color(0xFFFFD700)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Beri Nama Taman Hiburan", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        branch.customName!!,
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        renameInput = branch.customName!!
                                        branchImageUrlInput = branch.imageUrl ?: ""
                                        showRenameDialog = true
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Nama", tint = Color.LightGray)
                                    }
                                }
                                Text("Lokasi: ${branch.locationName}", color = Color.LightGray, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // Status Bar
            val statusText = branch.currentStatus
            val statusColor = when(statusText) {
                "Riset & Akuisisi" -> Color.Gray
                "Perencanaan Lahan" -> Color.LightGray
                "Pembangunan Infrastruktur" -> Color(0xFFFFA000)
                "Siap Launching" -> Color(0xFFFFD700)
                "Hype Season!" -> Color(0xFFFF0055)
                "Beroperasi" -> Color(0xFF00FF00)
                else -> Color.White
            }
            
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Box(modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Status Cabang", color = Color.Gray, fontSize = 12.sp)
                            Text(statusText, color = statusColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (branch.remainingBiddingMonths > 0) {
                                Text("Sisa Waktu Negosiasi: ${branch.remainingBiddingMonths} Bulan", color = Color(0xFFFFD700), fontSize = 14.sp)
                            } else {
                                Column {
                                    Text("Wahana Terisi: ${branch.rides.size} / ${branch.landType.maxSlots}", color = Color.White, fontSize = 14.sp)
                                    val safeFacilities = branch.facilities ?: emptyList()
                                    if (safeFacilities.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Fasilitas Publik: ${safeFacilities.size} Unit", color = Color(0xFF00FFCC), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                        
                        if (branch.remainingBiddingMonths <= 0) {
                            Button(
                                onClick = {
                                    navController.navigate("theme_park_facilities/${instanceId}/${branch.id}")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF005B5C),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("🛋️ Fasilitas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (branch.activeDisaster != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF4444).copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, Color(0xFFFF4444).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚠️ DARURAT: ${branch.activeDisaster}", color = Color(0xFFFF4444), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Taman ditutup sementara, biaya perbaikan meningkat!", color = Color.LightGray, fontSize = 14.sp)
                    }
                }
            }
            
            if (branch.isLaunched) {
                Button(
                    onClick = { showTicketManagement = true },
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🎫 Manajemen Harga Tiket", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }

            if (!branch.isLaunched && branch.remainingBiddingMonths <= 0) {
                var showLaunchAlert by remember { mutableStateOf(false) }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Persiapan Grand Launching", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        val finished = branch.rides.count { !it.isConstructing }
                        Text("Selesai: $finished / ${branch.minRidesToLaunch} Wahana Minimum", color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (!branch.hasHypeMarketing) {
                            Button(
                                onClick = { if (!viewModel.activateThemeParkHype(instanceId, branchId, 1000000L)) showErrorInsufficientFunds = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                            ) {
                                Text("Bakar Uang untuk Hype Launching ($1M)", color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        } else {
                            Text("🔥 Marketing Hype Aktif!", color = Color(0xFFFF0055), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = { 
                                if (finished >= branch.minRidesToLaunch) showLaunchAlert = true 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (finished >= branch.minRidesToLaunch) Color(0xFFFFD700) else Color.Gray,
                                contentColor = Color.Black
                            ),
                            enabled = finished >= branch.minRidesToLaunch
                        ) {
                            Text("GRAND LAUNCHING", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                if (showLaunchAlert) {
                    AlertDialog(
                        onDismissRequest = { showLaunchAlert = false },
                        containerColor = Color(0xFF1E1E1E),
                        titleContentColor = Color(0xFFFFD700),
                        textContentColor = Color.LightGray,
                        title = { Text("Buka Taman Hiburan?", fontWeight = FontWeight.Bold) },
                        text = { Text("Taman akan dibuka untuk publik. ${if (branch.hasHypeMarketing) "Efek Hype Marketing aktif bulan ini!" else ""}") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.launchThemeParkBranch(instanceId, branchId)
                                    showLaunchAlert = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                            ) {
                                Text("Ya, Buka!", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLaunchAlert = false }) { Text("Batal", color = Color.White) }
                        }
                    )
                }
            } else if (branch.isLaunched) {
                val currentMonth = playerState.inGameMonth
                val seasonMultiplier = when(currentMonth) {
                    6, 7 -> 3.0
                    12 -> 4.0
                    2, 9 -> 0.7
                    else -> 1.0
                }
                val seasonText = if(seasonMultiplier > 1.0) "(Musim Liburan 🔥)" else if (seasonMultiplier < 1.0) "(Low Season)" else "(Normal)"
                
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Pengunjung Terakhir", color = Color.Gray, fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${java.text.NumberFormat.getInstance().format(branch.lastMonthVisitors)} org", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(seasonText, color = if(seasonMultiplier > 1.0) Color(0xFFFF5500) else Color.LightGray, fontSize = 12.sp)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Profit Bersih", color = Color.Gray, fontSize = 12.sp)
                                Text("$${java.text.NumberFormat.getInstance().format(branch.lastMonthProfit)}", color = if (branch.lastMonthProfit >= 0) Color(0xFF00FF00) else Color(0xFFFF4444), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Pendapatan Kotor", color = Color.Gray, fontSize = 12.sp)
                                Text("+ $${java.text.NumberFormat.getInstance().format(branch.lastMonthRevenue)}", color = Color(0xFF00FF00), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Pengeluaran", color = Color.Gray, fontSize = 12.sp)
                                Text("- $${java.text.NumberFormat.getInstance().format(branch.lastMonthExpense)}", color = Color(0xFFFF4444), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // --- KARTU BIRO IKLAN & MARKETING ---
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (branch.activeAdName != null) Color(0xFF3A3000) else Color.White.copy(alpha = 0.05f)
                    ),
                    border = if (branch.activeAdName != null) BorderStroke(1.5.dp, Color(0xFFFFD700)) else null
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (branch.activeAdName != null) {
                                    Text(
                                        "🔥 Promosi Aktif: ${branch.activeAdName} (+${(branch.adBoostMultiplier * 100 - 100).toInt()}%)",
                                        color = Color(0xFFFFD700),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Sisa Kontrak: ${branch.adMonthsLeft} Bulan",
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Text(
                                        "Status Promosi: Inaktif",
                                        color = Color.LightGray,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Biarkan biro mempromosikan wahana Anda guna pelipatgandaan pengunjung secara agresif.",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    navController.navigate("theme_park_marketing/${instanceId}/${branch.id}")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (branch.activeAdName != null) Color(0xFFFFD700) else Color(0xFF2C2C2C),
                                    contentColor = if (branch.activeAdName != null) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (branch.activeAdName != null) "Ubah Kontrak" else "Buka Biro Iklan",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { showZoneManagement = true },
                modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("🗺️ Manajemen Zonasi", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daftar Wahana", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(onClick = { expandSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = expandSortMenu,
                            onDismissRequest = { expandSortMenu = false },
                            modifier = Modifier.background(Color(0xFF2C2C2C))
                        ) {
                            val options = listOf(
                                "TERBARU" to "Terbaru",
                                "NAMA_AZ" to "Nama (A-Z)",
                                "HARGA_DESC" to "Harga/Tier (Termahal)",
                                "STATUS" to "Status Konstruksi"
                            )
                            options.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, color = if (sortType == key) Color(0xFFFFD700) else Color.White) },
                                    onClick = {
                                        sortType = key
                                        expandSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            if (isGridView) Icons.Default.List else Icons.Default.GridView,
                            contentDescription = "Toggle View",
                            tint = Color.White
                        )
                    }
                }
            }
            }
            }
            
            val safeBranchFacilities = branch.facilities ?: emptyList()
            if (branch.rides.isEmpty() && safeBranchFacilities.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Park, contentDescription = null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Lahan masih kosong.",
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Rencanakan tata letak, bangun wahana, atau fasilitas pertama Anda.",
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } else {
                val sortedRides = when(sortType) {
                    "NAMA_AZ" -> branch.rides.sortedBy { it.name }
                    "HARGA_DESC" -> branch.rides.sortedByDescending { it.cost }
                    "STATUS" -> branch.rides.sortedByDescending { if (it.isConstructing) 1 else 0 }
                    else -> branch.rides.toList()
                }

                val allZones = (branch.parkZones ?: emptyList()) + listOf("Belum Terzonasi")

                allZones.forEach { zone ->
                    val ridesInZone = sortedRides.filter { (it.zoneName ?: "Belum Terzonasi") == zone }
                    val facilitiesInZone = safeBranchFacilities.filter { (it.zoneName ?: "Belum Terzonasi") == zone }

                    if (ridesInZone.isNotEmpty() || facilitiesInZone.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)) {
                                Text("📍 Zona: $zone", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (zone != "Belum Terzonasi") {
                                    androidx.compose.material3.HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }

                        if (ridesInZone.isNotEmpty()) {
                            if (isGridView) {
                                val chunks = ridesInZone.chunked(2)
                                items(chunks) { rowRides ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        for (ride in rowRides) {
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f).aspectRatio(0.85f),
                                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    if (ride.imageUrl != null) {
                                                        AsyncImage(
                                                            model = ride.imageUrl,
                                                            contentDescription = "Ride Image",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.matchParentSize()
                                                        )
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .matchParentSize()
                                                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))))
                                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                                            .padding(12.dp)
                                                    ) {
                                                        Column(
                                                            modifier = Modifier
                                                                .align(Alignment.BottomStart)
                                                                .fillMaxWidth()
                                                        ) {
                                                            Text(ride.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(ride.tierDescription, color = Color.LightGray, fontSize = 10.sp, maxLines = 1)
                                                            if (ride.zoneName != null) {
                                                                Text("📍 Zona: ${ride.zoneName}", color = Color.White, fontSize = 9.sp, maxLines = 1)
                                                            }
                                                            if (ride.ipThemeTitle != null) {
                                                                val ipColor = if ((ride.ipThemeScore ?: 0) > 80) Color(0xFFFFD700) else Color.LightGray
                                                                Text("🎬 Tema: ${ride.ipThemeTitle}", color = ipColor, fontSize = 9.sp, maxLines = 1)
                                                            }
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            
                                                            val statusText: String
                                                            val statusColor: Color
                                                            if (ride.isConstructing) {
                                                                statusText = "Membangun... (${ride.constructionMonthsLeft} bln)"
                                                                statusColor = Color(0xFFFFA000)
                                                            } else if (ride.isUnderMaintenance) {
                                                                statusText = "Maint. (${ride.maintenanceMonthsLeft} bln)"
                                                                statusColor = Color(0xFFFF9800)
                                                            } else if (ride.isPaused) {
                                                                statusText = "Tutup Smtara"
                                                                statusColor = Color.Gray
                                                            } else if (!branch.isLaunched) {
                                                                statusText = "Standby"
                                                                statusColor = Color.LightGray
                                                            } else {
                                                                statusText = "Beroperasi"
                                                                statusColor = Color(0xFF00FF00)
                                                            }
                                                            
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                            ) {
                                                                Text(
                                                                    text = statusText,
                                                                    color = statusColor,
                                                                    fontSize = 10.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    modifier = Modifier.weight(1f)
                                                                )
                                                                
                                                                if (!ride.isConstructing && !ride.isUnderMaintenance) {
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                                    ) {
                                                                        IconButton(
                                                                            onClick = {
                                                                                if (ride.isPaused) {
                                                                                    viewModel.toggleThemeParkRidePause(instanceId, branchId, ride.id)
                                                                                } else {
                                                                                    showRidePauseDialog = ride
                                                                                }
                                                                            },
                                                                            modifier = Modifier.size(24.dp)
                                                                        ) {
                                                                            Icon(
                                                                                imageVector = if (ride.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                                                                contentDescription = "Pause Toggle",
                                                                                tint = if (ride.isPaused) Color(0xFF00FFCC) else Color.LightGray,
                                                                                modifier = Modifier.size(14.dp)
                                                                            )
                                                                        }
                                                                        IconButton(
                                                                            onClick = { showRideMaintenanceDialog = ride },
                                                                            modifier = Modifier.size(24.dp)
                                                                        ) {
                                                                            Icon(
                                                                                imageVector = Icons.Default.Build,
                                                                                contentDescription = "Maintenance",
                                                                                tint = Color(0xFFFF9800),
                                                                                modifier = Modifier.size(12.dp)
                                                                            )
                                                                        }
                                                                        IconButton(
                                                                            onClick = { showRideDemolishDialog = ride },
                                                                            modifier = Modifier.size(24.dp)
                                                                        ) {
                                                                            Icon(
                                                                                imageVector = Icons.Default.Delete,
                                                                                contentDescription = "Gusur",
                                                                                tint = Color.Red,
                                                                                modifier = Modifier.size(14.dp)
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                editRideNameInput = ride.name
                                                                editRideImageUrlInput = ride.imageUrl ?: ""
                                                                editRideZoneInput = ride.zoneName
                                                                editRideIpTitleInput = ride.ipThemeTitle
                                                                editRideIpScoreInput = ride.ipThemeScore
                                                                showEditRideDialog = ride
                                                            },
                                                            modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
                                                        ) {
                                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (rowRides.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            } else {
                                items(ridesInZone) { ride ->
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().height(100.dp).padding(bottom = 16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            if (ride.imageUrl != null) {
                                                AsyncImage(
                                                    model = ride.imageUrl,
                                                    contentDescription = "Ride Image",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.matchParentSize()
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .background(Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                                    .padding(16.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(ride.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            IconButton(onClick = {
                                                                editRideNameInput = ride.name
                                                                editRideImageUrlInput = ride.imageUrl ?: ""
                                                                editRideZoneInput = ride.zoneName
                                                                editRideIpTitleInput = ride.ipThemeTitle
                                                                editRideIpScoreInput = ride.ipThemeScore
                                                                showEditRideDialog = ride
                                                            }, modifier = Modifier.size(24.dp)) {
                                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(ride.tierDescription, color = Color.LightGray, fontSize = 12.sp)
                                                        if (ride.zoneName != null) {
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text("📍 Zona: ${ride.zoneName}", color = Color.White, fontSize = 11.sp)
                                                        }
                                                        if (ride.ipThemeTitle != null) {
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            val ipColor = if ((ride.ipThemeScore ?: 0) > 80) Color(0xFFFFD700) else Color.LightGray
                                                            Text("🎬 Tema: ${ride.ipThemeTitle}", color = ipColor, fontSize = 11.sp)
                                                        }
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        val statusText: String
                                                        val statusSubtext: String?
                                                        val statusColor: Color
                                                        if (ride.isConstructing) {
                                                            statusText = "Membangun..."
                                                            statusSubtext = "Sisa: ${ride.constructionMonthsLeft} bln"
                                                            statusColor = Color(0xFFFFA000)
                                                        } else if (ride.isUnderMaintenance) {
                                                            statusText = "Dalam Perbaikan"
                                                            statusSubtext = "Sisa: ${ride.maintenanceMonthsLeft} bln"
                                                            statusColor = Color(0xFFFF9800)
                                                        } else if (ride.isPaused) {
                                                            statusText = "Ditutup Sementara"
                                                            statusSubtext = null
                                                            statusColor = Color.Gray
                                                        } else if (!branch.isLaunched) {
                                                            statusText = "Standby"
                                                            statusSubtext = "Menunggu Launching"
                                                            statusColor = Color.LightGray
                                                        } else {
                                                            statusText = "Beroperasi"
                                                            statusSubtext = null
                                                            statusColor = Color(0xFF00FFCC)
                                                        }
                                                        
                                                        Text(statusText, color = statusColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                        if (statusSubtext != null) {
                                                            Text(statusSubtext, color = Color.Gray, fontSize = 11.sp)
                                                        }
                                                        
                                                        if (!ride.isConstructing && !ride.isUnderMaintenance) {
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                IconButton(
                                                                    onClick = {
                                                                        if (ride.isPaused) {
                                                                            viewModel.toggleThemeParkRidePause(instanceId, branchId, ride.id)
                                                                        } else {
                                                                            showRidePauseDialog = ride
                                                                        }
                                                                    },
                                                                    modifier = Modifier.size(24.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = if (ride.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                                                        contentDescription = "Pause Toggle",
                                                                        tint = if (ride.isPaused) Color(0xFF00FFCC) else Color.LightGray,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                                IconButton(
                                                                    onClick = { showRideMaintenanceDialog = ride },
                                                                    modifier = Modifier.size(24.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Build,
                                                                        contentDescription = "Maintenance",
                                                                        tint = Color(0xFFFF9800),
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                }
                                                                IconButton(
                                                                    onClick = { showRideDemolishDialog = ride },
                                                                    modifier = Modifier.size(24.dp)
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Delete,
                                                                        contentDescription = "Gusur",
                                                                        tint = Color.Red,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (facilitiesInZone.isNotEmpty()) {
                            items(facilitiesInZone) { facility ->
                                val catalogEntry = com.example.viewmodel.ThemeParkEngine.facilitiesCatalog.find { it.catalogId == facility.catalogId }
                                val icon = catalogEntry?.icon ?: "🛋️"
                                val desc = catalogEntry?.description ?: ""
                                val nFormat = java.text.NumberFormat.getInstance()

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1B2A)),
                                    border = BorderStroke(1.dp, Color(0xFF00FFCC).copy(alpha = 0.25f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                if (!facility.imageUrl.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = facility.imageUrl,
                                                        contentDescription = facility.name,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .border(1.dp, Color(0xFF00FFCC).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                    )
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(44.dp)
                                                            .background(Color.White.copy(alpha = 0.05f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(icon, fontSize = 22.sp)
                                                    }
                                                }
                                                Column {
                                                    Text(facility.name, color = Color(0xFFE0E1DD), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                                    Text("Zonasi: ${facility.zoneName}", color = Color.LightGray.copy(alpha = 0.7f), fontSize = 11.sp)
                                                }
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                IconButton(
                                                    onClick = {
                                                        selectedFacilityForEdit = facility
                                                        facilityEditName = facility.name
                                                        facilityEditZone = facility.zoneName
                                                        facilityEditImageUrl = facility.imageUrl ?: ""
                                                        showFacilityEditDialog = true
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit Fasilitas", tint = Color(0xFF00FFCC), modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = {
                                                        selectedFacilityForDelete = facility
                                                        showFacilityDeleteConfirm = true
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Bongkar Fasilitas", tint = Color(0xFFFF5555), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }

                                        if (desc.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(desc, color = Color.Gray, fontSize = 12.sp)
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFF4A1A1A).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0xFFFF5555).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                                    Text("MAINTENANCE", color = Color.LightGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text("$${nFormat.format(facility.maintenanceCost)}", color = Color(0xFFFF8888), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1.2f)
                                                    .background(Color(0xFF1D3F2D).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0xFF00FF88).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                                    Text("BUFF F&B REVENUE", color = Color.LightGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text("+${facility.fnbBoostPercent}%", color = Color(0xFF00FF00), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color(0xFF1D3F2D).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color(0xFF00FF88).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                                    Text("APPEAL BUFF", color = Color.LightGray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text("+${facility.appealBoost} Poin", color = Color(0xFF00FF00), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            title = { Text("Edit Cabang Taman", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Pilih nama dan atur gambar header (opsional) untuk cabang taman hiburan ini.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("Nama Taman Hiburan") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = branchImageUrlInput,
                        onValueChange = { branchImageUrlInput = it },
                        label = { Text("URL Gambar Header (Opsional)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameInput.isNotBlank()) {
                        viewModel.renameThemeParkBranch(instanceId, branchId, renameInput)
                    }
                    viewModel.updateThemeParkBranchImage(instanceId, branchId, branchImageUrlInput)
                    showRenameDialog = false
                }) {
                    Text("Simpan", color = Color(0xFFFFD700))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color(0xFFE53935),
            textContentColor = Color.LightGray,
            title = { Text("Gusur Taman Hiburan?", fontWeight = FontWeight.Bold) },
            text = { Text("Seluruh wahana di cabang ini akan hilang dan lahan akan dikembalikan ke kas Divisi (50% nilai dasar lahan). Aksi ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteThemeParkBranch(instanceId, branchId)
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Ya, Gusur", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showFinancialLedger) {
        val nFormat = java.text.NumberFormat.getInstance()
        val totalRidesValue = branch.rides.sumOf { it.cost }
        val totalFacilitiesValue = (branch.facilities ?: emptyList()).sumOf { it.buildCost }
        val totalAssetValue = branch.landType.basePrice + totalRidesValue + totalFacilitiesValue

        // Estimasi Tiket & F&B
        val targetVisitors = branch.lastMonthVisitors
        val vipGuestsCount = (targetVisitors * 0.10).toInt()
        val familyCount = ((targetVisitors * 0.25) / 4).toInt()
        val terusanCount = (targetVisitors * 0.30).toInt()
        val regularCount = targetVisitors - vipGuestsCount - (familyCount * 4) - terusanCount
        
        var ticketRevEst = (vipGuestsCount * branch.priceVIP) + 
                          (familyCount * branch.priceFamily) + 
                          (terusanCount * branch.priceTerusan) + 
                          (regularCount * branch.priceRegular)
        
        val fnbFacBoost = 1.0 + ((branch.facilities ?: emptyList()).sumOf { it.fnbBoostPercent } / 100.0)
        val avgSpend = 45.0 * fnbFacBoost
        val baseFnbRev = (targetVisitors * 0.8 * avgSpend).toLong()
        var fnbRevEst = (baseFnbRev * branch.adBoostMultiplier).toLong()

        if (branch.hypeMonthsLeft > 0) {
            ticketRevEst = (ticketRevEst * 2.0).toLong()
            fnbRevEst = (fnbRevEst * 2.0).toLong()
        }

        // Jika tidak beroperasi, paksa ke 0
        val finalTicketRev = if (branch.isLaunched && branch.lastMonthRevenue > 0) ticketRevEst else 0L
        val finalFnbRev = if (branch.isLaunched && branch.lastMonthRevenue > 0) branch.lastMonthRevenue - finalTicketRev else 0L

        // OPEX
        val totalRidesMaintenance = branch.rides.sumOf { it.maintenanceCost }
        val totalFacilitiesMaintenance = (branch.facilities ?: emptyList()).sumOf { it.maintenanceCost }
        val staffCost = if (branch.isLaunched) (targetVisitors / 100) * 1500L else 0L

        // Hitung Iklan Bulanan
        val totalAppealForAd = branch.rides.filter { !it.isConstructing }.sumOf { it.baseMonthlyVisitors / 100 }
        val idealPriceForAd = ThemeParkEngine.calculateIdealPrice(totalAppealForAd.toDouble())
        val adPackagesForAd = ThemeParkEngine.calculateAdPackages(branch, idealPriceForAd)
        val activeAdPackage = adPackagesForAd.find { it.name == branch.activeAdName }
        val monthlyAdExpense = if (activeAdPackage != null) activeAdPackage.totalPrice / activeAdPackage.durationMonths else 0L

        AlertDialog(
            onDismissRequest = { showFinancialLedger = false },
            containerColor = Color(0xFF161D26),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📊 Laporan Finansial & Valuasi", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF00FFCC))
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Analisis terperinci kesehatan finansial unit bisnis Theme Park di ${branch.customName ?: branch.locationName}.",
                        fontSize = 12.sp,
                        color = Color.LightGray.copy(alpha = 0.8f)
                    )

                    // A. VALUASI ASET
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF202A37)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("🏦 Valuasi Aset (Total Asset)", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Nilai Lahan", color = Color.Gray, fontSize = 12.sp)
                                Text("$${nFormat.format(branch.landType.basePrice)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Valuasi Wahana (${branch.rides.size} unit)", color = Color.Gray, fontSize = 12.sp)
                                Text("$${nFormat.format(totalRidesValue)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Valuasi Fasilitas (${(branch.facilities ?: emptyList()).size} unit)", color = Color.Gray, fontSize = 12.sp)
                                Text("$${nFormat.format(totalFacilitiesValue)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Aset Fisik", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("$${nFormat.format(totalAssetValue)}", color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }
                        }
                    }

                    // B. PEMASUKAN BULANAN
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF202A37)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("📈 Pemasukan Bulanan (Revenue)", color = Color(0xFF00FF88), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pendapatan Tiket Masuk", color = Color.Gray, fontSize = 12.sp)
                                Text("$${nFormat.format(finalTicketRev)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Pendapatan Makan/Minum (F&B)", color = Color.Gray, fontSize = 12.sp)
                                Text("$${nFormat.format(finalFnbRev)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Pemasukan Kotor", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("$${nFormat.format(branch.lastMonthRevenue)}", color = Color(0xFF00FF88), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }
                        }
                    }

                    // C. PENGELUARAN BULANAN
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF202A37)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("📉 Pengeluaran Bulanan (OPEX)", color = Color(0xFFFF5555), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Biaya Perawatan Wahana", color = Color.Gray, fontSize = 12.sp)
                                Text("$${nFormat.format(totalRidesMaintenance)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Biaya Perawatan Fasilitas", color = Color.Gray, fontSize = 12.sp)
                                Text("$${nFormat.format(totalFacilitiesMaintenance)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Beban Gaji Karyawan", color = Color.Gray, fontSize = 12.sp)
                                Text("$${nFormat.format(staffCost)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            if (branch.activeAdName != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Beban Iklan (${branch.activeAdName})", color = Color.Gray, fontSize = 12.sp)
                                    Text("$${nFormat.format(monthlyAdExpense)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                            
                            val totalCalculatedExpense = totalRidesMaintenance + totalFacilitiesMaintenance + staffCost + monthlyAdExpense
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Pengeluaran", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("$${nFormat.format(branch.lastMonthExpense)}", color = Color(0xFFFF5555), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            }
                        }
                    }

                    // PROFIT BERSIH
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (branch.lastMonthProfit >= 0) Color(0xFF1E3A2F) else Color(0xFF4A1E1E)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (branch.lastMonthProfit >= 0) Color(0xFF00FF88).copy(alpha = 0.4f) else Color(0xFFFF5555).copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("PROFIT BERSIH BULAN LALU", color = Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (branch.lastMonthProfit >= 0) "SURPLUS (UNTUNG)" else "DEFISIT (RUGI)",
                                    color = if (branch.lastMonthProfit >= 0) Color(0xFF00FF88) else Color(0xFFFF5555),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "$${nFormat.format(branch.lastMonthProfit)}",
                                color = if (branch.lastMonthProfit >= 0) Color(0xFF00FF88) else Color(0xFFFF5555),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showFinancialLedger = false }
                ) {
                    Text("Tutup", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    showEditRideDialog?.let { ride ->
        ModalBottomSheet(
            onDismissRequest = { showEditRideDialog = null },
            containerColor = Color(0xFF1E1E1E),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            var showZoneDropdown by remember { mutableStateOf(false) }
            var showIpLiveDropdown by remember { mutableStateOf(false) }
            var showIpAnimDropdown by remember { mutableStateOf(false) }

            // Cross-division fetching from all holding entities + directly owned businesses
            val allBusinesses = playerState.ownedBusinesses + playerState.holdingCompanies.flatMap { h -> h.subsidiaries ?: emptyList() }
            val allStudios = allBusinesses.filter { it.catalogId == "media_studio" && (it.studioType ?: "LIVE_ACTION") != "ANIMATION" }
            val allAnims = allBusinesses.filter { it.catalogId == "media_studio" && (it.studioType ?: "LIVE_ACTION") == "ANIMATION" }
            val liveFilms = allStudios.flatMap { it.projectHistory ?: emptyList() }.filter { it.status == "FINISHED" }
            val animFilms = allAnims.flatMap { it.projectHistory ?: emptyList() }.filter { it.status == "FINISHED" }

            val isBuildNew = ride.id.startsWith("NEW_RIDE")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                Text(
                    text = if (isBuildNew) "Bangun Wahana (${ride.tierDescription})" else "Edit Wahana (${ride.tierDescription})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = editRideNameInput,
                    onValueChange = { editRideNameInput = it },
                    label = { Text("Nama Wahana") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editRideImageUrlInput,
                    onValueChange = { editRideImageUrlInput = it },
                    label = { Text("URL Gambar (Opsional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Text("Pilihan Zonasi", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                if (branch.parkZones.isEmpty()) {
                    Text("Belum ada zona dibuat di taman ini.", color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                } else {
                    Box {
                        OutlinedButton(onClick = { showZoneDropdown = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(editRideZoneInput ?: "Pilih Zona", color = Color.White)
                        }
                        DropdownMenu(expanded = showZoneDropdown, onDismissRequest = { showZoneDropdown = false }) {
                            DropdownMenuItem(text = { Text("Tanpa Zona") }, onClick = { editRideZoneInput = null; showZoneDropdown = false })
                            branch.parkZones.forEach { z ->
                                DropdownMenuItem(text = { Text(z) }, onClick = { editRideZoneInput = z; showZoneDropdown = false })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Tema IP & Lisensi Internal", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                if (editRideIpTitleInput != null) {
                    Text("Preview Bonus: Tema: $editRideIpTitleInput | Kualitas IP: ${editRideIpScoreInput}/100", color = Color(0xFFFFD700), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { editRideIpTitleInput = null; editRideIpScoreInput = null }) {
                        Text("Hapus Tema IP", color = Color.Red)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                        Button(
                            onClick = { showIpLiveDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🎬 Live-Action", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        DropdownMenu(
                            expanded = showIpLiveDropdown,
                            onDismissRequest = { showIpLiveDropdown = false },
                            modifier = Modifier.background(Color(0xFF2E2E2E))
                        ) {
                            if (liveFilms.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Belum ada IP rilis dari seluruh Divisi Studio Anda.", color = Color.Gray, fontSize = 13.sp) },
                                    onClick = {},
                                    enabled = false
                                )
                            } else {
                                liveFilms.forEach { f ->
                                    DropdownMenuItem(
                                        text = { Text("${f.title} (Box Office: $${f.boxOffice / 1_000_000} Juta)", color = Color.White, fontSize = 13.sp) },
                                        onClick = {
                                            editRideIpTitleInput = f.title
                                            editRideIpScoreInput = f.reviewScore
                                            showIpLiveDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                        Button(
                            onClick = { showIpAnimDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🎨 Animation", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        DropdownMenu(
                            expanded = showIpAnimDropdown,
                            onDismissRequest = { showIpAnimDropdown = false },
                            modifier = Modifier.background(Color(0xFF2E2E2E))
                        ) {
                            if (animFilms.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Belum ada IP rilis dari seluruh Divisi Studio Anda.", color = Color.Gray, fontSize = 13.sp) },
                                    onClick = {},
                                    enabled = false
                                )
                            } else {
                                animFilms.forEach { f ->
                                    DropdownMenuItem(
                                        text = { Text("${f.title} (Box Office: $${f.boxOffice / 1_000_000} Juta)", color = Color.White, fontSize = 13.sp) },
                                        onClick = {
                                            editRideIpTitleInput = f.title
                                            editRideIpScoreInput = f.reviewScore
                                            showIpAnimDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        if (editRideNameInput.isNotBlank()) {
                            if (isBuildNew) {
                                val tier = com.example.data.RideTier.values().find { it.cost == ride.cost }
                                if (tier != null) {
                                    viewModel.buildThemeParkRide(
                                        businessInstanceId = instanceId,
                                        branchId = branchId,
                                        rideTier = tier,
                                        customRideName = editRideNameInput,
                                        imageUrl = editRideImageUrlInput,
                                        zoneName = editRideZoneInput,
                                        ipThemeTitle = editRideIpTitleInput,
                                        ipThemeScore = editRideIpScoreInput
                                    )
                                }
                            } else {
                                viewModel.updateThemeParkRideDetails(instanceId, branchId, ride.id, editRideNameInput, editRideImageUrlInput)
                                viewModel.updateThemeParkRideZoneAndIP(instanceId, branchId, ride.id, editRideZoneInput, editRideIpTitleInput, editRideIpScoreInput)
                            }
                        }
                        showEditRideDialog = null
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text(if (isBuildNew) "Bangun Wahana" else "Simpan Perubahan", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showRideCatalog) {
        ModalBottomSheet(
            onDismissRequest = { showRideCatalog = false },
            containerColor = Color(0xFF1E1E1E)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.85f)) {
                Text("Katalog Wahana", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(RideTier.entries.toTypedArray()) { tier ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Box(modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(16.dp)) {
                                Column {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Tier ${tier.level}", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        Button(
                                            onClick = {
                                                if (owned.companyCash >= tier.cost) {
                                                    editRideNameInput = ""
                                                    editRideImageUrlInput = ""
                                                    editRideZoneInput = null
                                                    editRideIpTitleInput = null
                                                    editRideIpScoreInput = null
                                                    showEditRideDialog = com.example.data.ThemeParkRide(
                                                        id = "NEW_RIDE",
                                                        name = "",
                                                        constructionMonthsLeft = tier.buildMonths,
                                                        tierDescription = tier.description,
                                                        cost = tier.cost
                                                    )
                                                    showRideCatalog = false
                                                } else {
                                                    showErrorInsufficientFunds = true
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.White.copy(alpha = 0.05f),
                                                contentColor = Color(0xFFFFD700)
                                            ),
                                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("Pilih")
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(tier.description, color = Color.White, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Harga: ${formatCurrencyRingkas(tier.cost.toDouble(), useShortFormat)}", color = Color.LightGray, fontSize = 12.sp)
                                        Text("Durasi: ${tier.buildMonths} Bulan", color = Color.LightGray, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showErrorInsufficientFunds) {
        AlertDialog(
            onDismissRequest = { showErrorInsufficientFunds = false },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color(0xFFE53935),
            textContentColor = Color.LightGray,
            title = { Text("Dana Tidak Mencukupi", fontWeight = FontWeight.Bold) },
            text = { Text("Kas Internal Divisi tidak cukup untuk membangun wahana ini. Silakan suntik dana dari Holding terlebih dahulu.") },
            confirmButton = {
                TextButton(onClick = { showErrorInsufficientFunds = false }) {
                    Text("Tutup", color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }



    if (showTicketManagement) {
        var inputRegularStr by remember { mutableStateOf(branch.priceRegular.toString()) }
        var inputTerusanStr by remember { mutableStateOf(branch.priceTerusan.toString()) }
        var inputVIPStr by remember { mutableStateOf(branch.priceVIP.toString()) }
        var inputFamilyStr by remember { mutableStateOf(branch.priceFamily.toString()) }
        var showPriceGuide by remember { mutableStateOf(false) }

        val activeRides = branch.rides.count { !it.isConstructing }
        val baseRidesAppeal = branch.rides.filter { !it.isConstructing }.sumOf { (it.baseMonthlyVisitors / 100).toDouble() }
        val facilitiesAppeal = (branch.facilities ?: emptyList()).sumOf { it.appealBoost }
        val totalAppeal = baseRidesAppeal + facilitiesAppeal
        val fairTicketValue = ThemeParkEngine.calculateIdealPrice(totalAppeal).toLong()

        val maxTheoreticalCapacity = branch.landType.maxSlots * 3000
        val activeCapacity = 5000 + (activeRides * 3000)
        val finalCapacityCap = minOf(activeCapacity, maxTheoreticalCapacity)

        if (showPriceGuide) {
            AlertDialog(
                onDismissRequest = { showPriceGuide = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📖 Strategi Harga & Kapasitas Taman", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Pendapatan taman sangat bergantung pada strategi harga Anda terhadap 'Harga Ideal' wahana saat ini: $$fairTicketValue.",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🛡️ The Golden Rule (Aturan Emas)", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Kunci: Pastikan harga Tiket Reguler ATAU Tiket Terusan Anda berada di sekitar Harga Ideal.", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Efek: Jika salah satu tiket ini di harga wajar, sistem menjamin taman Anda akan selalu terisi minimal 85% dari kapasitas, seburuk apapun Anda mengatur harga tiket lainnya!", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🎟️ Tiket Reguler (Fokus Keramaian)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("70% - 80% dari Harga Ideal ($${(fairTicketValue * 0.7).toLong()} - $${(fairTicketValue * 0.8).toLong()})", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Efek: Mengundang banyak pengunjung. Profit margin kecil dari tiket, tapi sangat menguntungkan untuk penjualan makanan (F&B) di dalam taman.", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🎢 Tiket Terusan (Standar Profit)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("100% - 120% dari Harga Ideal ($${(fairTicketValue * 1.0).toLong()} - $${(fairTicketValue * 1.2).toLong()})", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Efek: Tiket wajib untuk balik modal operasional wahana. Jika di bawah harga ideal, taman akan merugi menanggung biaya listrik dan perawatan!", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("👑 Tiket VIP / Fast Track (Fokus Margin)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("200% - 250% dari Harga Ideal ($${(fairTicketValue * 2.0).toLong()} - $${(fairTicketValue * 2.5).toLong()})", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Efek: \"Mesin Uang\" utama Anda. Segmen pengunjung elit tidak peduli harga selama mereka tidak perlu mengantre.", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("👨‍👩‍👧‍👦 Paket Keluarga - 4 Orang (Taktik Diskon)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("3.2x - 3.5x dari Harga Ideal ($${(fairTicketValue * 3.2).toLong()} - $${(fairTicketValue * 3.5).toLong()})", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Efek: Harga normal 4 orang adalah 4x Harga Ideal. Jual di kisaran 3.2x agar rombongan keluarga merasa mendapat \"Diskon Besar\", namun kas Anda tetap aman dan surplus!", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showPriceGuide = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                    ) {
                        Text("Paham, Hubungkan Strategi", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }

        ModalBottomSheet(
            onDismissRequest = { showTicketManagement = false },
            containerColor = Color(0xFF1E1E1E),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🎫 Manajemen Harga Tiket",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    TextButton(
                        onClick = { showPriceGuide = true }
                    ) {
                        Text("📖 Panduan Harga", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                val nFormat = java.text.NumberFormat.getInstance()
                Text("Kapasitas Taman Saat Ini: ${nFormat.format(activeCapacity)} / ${nFormat.format(maxTheoreticalCapacity)} pengunjung per bln", color = Color(0xFFFFD700), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Harga Ideal Saat Ini: $$fairTicketValue", color = Color.LightGray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Reguler Field
                OutlinedTextField(
                    value = inputRegularStr,
                    onValueChange = { newVal ->
                        inputRegularStr = newVal.filter { it.isDigit() }
                    },
                    label = { Text("Harga Reguler") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Text("$", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    placeholder = { Text("0", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFFFD700),
                        unfocusedTextColor = Color(0xFFFFD700),
                        focusedLabelColor = Color(0xFFFFD700),
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color(0xFF121212),
                        focusedContainerColor = Color(0xFF121212)
                    ),
                    singleLine = true,
                    supportingText = { Text("Memancing keramaian, profit dari jajan F&B", color = Color.Gray, fontSize = 11.sp) }
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 2. Terusan Field
                OutlinedTextField(
                    value = inputTerusanStr,
                    onValueChange = { newVal ->
                        inputTerusanStr = newVal.filter { it.isDigit() }
                    },
                    label = { Text("Harga Terusan") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Text("$", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    placeholder = { Text("0", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFFFD700),
                        unfocusedTextColor = Color(0xFFFFD700),
                        focusedLabelColor = Color(0xFFFFD700),
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color(0xFF121212),
                        focusedContainerColor = Color(0xFF121212)
                    ),
                    singleLine = true,
                    supportingText = { Text("Sistem standard wajib balik modal operasional", color = Color.Gray, fontSize = 11.sp) }
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 3. VIP Field
                OutlinedTextField(
                    value = inputVIPStr,
                    onValueChange = { newVal ->
                        inputVIPStr = newVal.filter { it.isDigit() }
                    },
                    label = { Text("Harga VIP") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Text("$", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    placeholder = { Text("0", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFFFD700),
                        unfocusedTextColor = Color(0xFFFFD700),
                        focusedLabelColor = Color(0xFFFFD700),
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color(0xFF121212),
                        focusedContainerColor = Color(0xFF121212)
                    ),
                    singleLine = true,
                    supportingText = { Text("Margin tinggi untuk segmen pengunjung elit", color = Color.Gray, fontSize = 11.sp) }
                )
                Spacer(modifier = Modifier.height(12.dp))

                // 4. Family Field
                OutlinedTextField(
                    value = inputFamilyStr,
                    onValueChange = { newVal ->
                        inputFamilyStr = newVal.filter { it.isDigit() }
                    },
                    label = { Text("Harga Paket Keluarga (4 Orang)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Text("$", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                    placeholder = { Text("0", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFFFD700),
                        unfocusedTextColor = Color(0xFFFFD700),
                        focusedLabelColor = Color(0xFFFFD700),
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        unfocusedContainerColor = Color(0xFF121212),
                        focusedContainerColor = Color(0xFF121212)
                    ),
                    singleLine = true,
                    supportingText = { Text("Taktik diskon besar rombongan, kas tetap surplus", color = Color.Gray, fontSize = 11.sp) }
                )
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val pReg = inputRegularStr.toLongOrNull() ?: branch.priceRegular
                        val pTer = inputTerusanStr.toLongOrNull() ?: branch.priceTerusan
                        val pVip = inputVIPStr.toLongOrNull() ?: branch.priceVIP
                        val pFam = inputFamilyStr.toLongOrNull() ?: branch.priceFamily
                        
                        viewModel.updateThemeParkTicketPrices(instanceId, branchId, pReg, pTer, pVip, pFam)
                        showTicketManagement = false
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Jalankan Kebijakan Harga", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showZoneManagement) {
        var newZoneInput by remember { mutableStateOf("") }
        ModalBottomSheet(
            onDismissRequest = { showZoneManagement = false },
            containerColor = Color(0xFF1E1E1E),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text("🗺️ Manajemen Zonasi Taman", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Daftar Zona:", color = Color.LightGray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (branch.parkZones.isEmpty()) {
                    Text("Belum ada zona dibuat di taman ini.", color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(branch.parkZones.size) { index ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📍 ${branch.parkZones[index]}", color = Color.White)
                                    Row {
                                        IconButton(
                                            onClick = { viewModel.moveThemeParkZone(instanceId, branchId, index, true) },
                                            enabled = index > 0,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = if (index > 0) Color.White else Color.DarkGray)
                                        }
                                        IconButton(
                                            onClick = { viewModel.moveThemeParkZone(instanceId, branchId, index, false) },
                                            enabled = index < branch.parkZones.size - 1,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = if (index < branch.parkZones.size - 1) Color.White else Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = newZoneInput,
                    onValueChange = { newZoneInput = it },
                    label = { Text("Nama Zona Baru") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (newZoneInput.isNotBlank()) {
                            viewModel.addThemeParkZone(instanceId, branchId, newZoneInput)
                            newZoneInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    enabled = newZoneInput.isNotBlank()
                ) {
                    Text("+ Tambah Zona", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showFacilityEditDialog && selectedFacilityForEdit != null) {
        var showFacilityZoneDropdown by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showFacilityEditDialog = false },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            title = { Text("Edit & Zonasi Fasilitas", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Kustomisasi nama fasilitas, pilih ikon/gambar kustom, dan tentukan penempatan zona penunjang.", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = facilityEditName,
                        onValueChange = { facilityEditName = it },
                        label = { Text("Nama Fasilitas") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00FFCC),
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = facilityEditImageUrl,
                        onValueChange = { facilityEditImageUrl = it },
                        label = { Text("URL Gambar (Opsional)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF00FFCC),
                            unfocusedBorderColor = Color.Gray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://example.com/image.jpg", color = Color.Gray) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column {
                        Text("Zonasi Penempatan", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box {
                            OutlinedButton(
                                onClick = { showFacilityZoneDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = if (facilityEditZone.isBlank() || facilityEditZone == "Belum Terzonasi") "Belum Terzonasi" else facilityEditZone,
                                    color = Color.White
                                )
                            }
                            DropdownMenu(
                                expanded = showFacilityZoneDropdown,
                                onDismissRequest = { showFacilityZoneDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Belum Terzonasi") },
                                    onClick = {
                                        facilityEditZone = "Belum Terzonasi"
                                        showFacilityZoneDropdown = false
                                    }
                                )
                                branch.parkZones.forEach { z ->
                                    DropdownMenuItem(
                                        text = { Text(z) },
                                        onClick = {
                                            facilityEditZone = z
                                            showFacilityZoneDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (facilityEditName.isNotBlank()) {
                        viewModel.updateThemeParkFacilityDetails(
                            businessInstanceId = instanceId,
                            branchId = branchId,
                            facilityId = selectedFacilityForEdit!!.id,
                            newName = facilityEditName,
                            newZoneName = facilityEditZone,
                            newImageUrl = facilityEditImageUrl
                        )
                    }
                    showFacilityEditDialog = false
                }) {
                    Text("Simpan", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFacilityEditDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showFacilityDeleteConfirm && selectedFacilityForDelete != null) {
        AlertDialog(
            onDismissRequest = { showFacilityDeleteConfirm = false },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            title = { Text("Bongkar Fasilitas?", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Text("Apakah Anda yakin ingin menggusur/membongkar fasilitas ${selectedFacilityForDelete!!.name}? Tindakan ini bersifat permanen dan kontribusi buff F&B serta Daya Tarik akan langsung hilang.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.demolishThemeParkFacility(
                        businessInstanceId = instanceId,
                        branchId = branchId,
                        facilityId = selectedFacilityForDelete!!.id
                    )
                    showFacilityDeleteConfirm = false
                }) {
                    Text("Ya, Bongkar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFacilityDeleteConfirm = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showRideDemolishDialog != null) {
        val ride = showRideDemolishDialog!!
        AlertDialog(
            onDismissRequest = { showRideDemolishDialog = null },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            title = { Text("Gusur Wahana?", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text("Anda yakin ingin membongkar ${ride.name}? Wahana akan hilang permanen dan Anda tidak mendapatkan refund.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.demolishThemeParkRide(instanceId, branchId, ride.id)
                    showRideDemolishDialog = null
                }) {
                    Text("Gusur", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRideDemolishDialog = null }) {
                    Text("Batal", color = Color.LightGray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showRidePauseDialog != null) {
        val ride = showRidePauseDialog!!
        AlertDialog(
            onDismissRequest = { showRidePauseDialog = null },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            title = { Text("Tutup Sementara?", fontWeight = FontWeight.Bold, color = Color.White) },
            text = { Text("Menutup wahana akan menghemat biaya perawatan hingga 50%, namun wahana tidak akan menarik pengunjung. Lanjutkan?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.toggleThemeParkRidePause(instanceId, branchId, ride.id)
                    showRidePauseDialog = null
                }) {
                    Text("Tutup Wahana", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRidePauseDialog = null }) {
                    Text("Batal", color = Color.LightGray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showRideMaintenanceDialog != null) {
        val ride = showRideMaintenanceDialog!!
        val tier = com.example.data.RideTier.values().find { it.cost == ride.cost }?.level ?: 1
        val duration = (tier / 2).coerceAtLeast(1)
        val extCost = ride.maintenanceCost * duration
        val nFormat = java.text.NumberFormat.getInstance()
        
        AlertDialog(
            onDismissRequest = { showRideMaintenanceDialog = null },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            title = { Text("Mulai Siklus Perbaikan?", fontWeight = FontWeight.Bold, color = Color(0xFFFFA000)) },
            text = { Text("Wahana ini berada di Kelas (Tier) $tier. Butuh waktu $duration Bulan dan biaya ekstra $${nFormat.format(extCost)} untuk perbaikan menyeluruh. Wahana tidak akan beroperasi selama masa ini.") },
            confirmButton = {
                TextButton(onClick = {
                    val success = viewModel.startThemeParkRideMaintenance(instanceId, branchId, ride.id)
                    if (!success) {
                        showErrorInsufficientForMaint = true
                    }
                    showRideMaintenanceDialog = null
                }) {
                    Text("Mulai Perbaikan", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRideMaintenanceDialog = null }) {
                    Text("Batal", color = Color.LightGray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showErrorInsufficientForMaint) {
        AlertDialog(
            onDismissRequest = { showErrorInsufficientForMaint = false },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            title = { Text("Kas Perusahaan Kurang", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text("Kas perusahaan Anda tidak mencukupi untuk membiayai siklus perbaikan wahana ini.") },
            confirmButton = {
                TextButton(onClick = { showErrorInsufficientForMaint = false }) {
                    Text("OK", color = Color(0xFF00FFCC), fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
