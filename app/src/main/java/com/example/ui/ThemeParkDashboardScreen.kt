package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ui.formatCurrencyRingkas
import com.example.data.ThemeParkLandType
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeParkDashboardScreen(navController: NavController, viewModel: GameViewModel, instanceId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()
    
    var owned = playerState.ownedBusinesses.find { it.instanceId == instanceId }
    if (owned == null) {
        for (holding in playerState.holdingCompanies) {
            owned = holding.subsidiaries.find { it.instanceId == instanceId }
            if (owned != null) break
        }
    }
    
    if (owned == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Data Taman Hiburan Tidak Ditemukan", color = Color.White)
        }
        return
    }

    val branches = owned.themeParkBranches
    val biddings = owned.activeThemeParkBiddings
    var showAcquisitionDialog by remember { mutableStateOf(false) }
    
    var selectedLandToBuy by remember { mutableStateOf<ThemeParkLandType?>(null) }
    var selectedBidding by remember { mutableStateOf<com.example.data.ActiveBidding?>(null) }
    var showErrorInsufficientFunds by remember { mutableStateOf(false) }
    
    var showCapitalDialog by remember { mutableStateOf(false) }
    var actionType by remember { mutableStateOf("") }
    var capitalInput by remember { mutableStateOf("") }
    var showLiquidateDialog by remember { mutableStateOf(false) }
    
    val parentHolding = playerState.holdingCompanies.find { it.subsidiaries.any { s -> s.instanceId == instanceId } }
    val isNested = parentHolding != null
    val parentCashDesc = if (isNested) parentHolding?.holdingCash ?: 0L else playerState.cash
    val parentName = if (isNested) parentHolding?.name ?: "" else "Global Balance"

    val totalVisitors = branches.sumOf { it.lastMonthVisitors.toLong() }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = { Text(owned.customName ?: "Universal Theme Park", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { showAcquisitionDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
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
                        Text("Akuisisi Lahan Baru", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1502136969935-8d8eef54d77b?q=80&w=1169&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFF121212)),
                            startY = 0f,
                            endY = 700f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
            ) {
                // Header: Dark Metallic
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.linearGradient(listOf(Color(0xFF2C2C2C).copy(alpha=0.9f), Color(0xFF151515).copy(alpha=0.9f))))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Total Kas Divisi", color = Color.LightGray, fontSize = 12.sp)
                                    Text(formatCurrencyRingkas(owned.companyCash, useShortFormat), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledIconButton(
                                        onClick = { actionType = "tarik"; capitalInput = ""; showCapitalDialog = true },
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Tarik Kas")
                                    }
                                    FilledIconButton(
                                        onClick = { actionType = "suntik"; capitalInput = ""; showCapitalDialog = true },
                                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black),
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Suntik Dana")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Total Pengunjung Global", color = Color.LightGray, fontSize = 12.sp)
                            Text(java.text.NumberFormat.getInstance().format(totalVisitors), color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Text("Cabang Taman Hiburan", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

                if (branches.isEmpty() && biddings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Park, contentDescription = null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Belum ada taman hiburan.",
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Mulai akuisisi lahan pertama Anda.",
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (biddings.isNotEmpty()) {
                            item { Text("Negosiasi Lahan Berjalan", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp)) }
                            items(biddings) { bidding ->
                                ThemeParkBiddingCard(bidding, onClick = {
                                    selectedBidding = bidding
                                }, useShortFormat = useShortFormat)
                            }
                            item { Spacer(modifier = Modifier.height(16.dp)) }
                        }
                        
                        if (branches.isNotEmpty()) {
                            item { Text("Cabang Terbuka / Sedang Dibangun", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp)) }
                            items(branches) { branch ->
                                ThemeParkBranchCard(branch, onClick = {
                                    navController.navigate("theme_park_branch_detail/${instanceId}/${branch.id}")
                                })
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { showLiquidateDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFE53935).copy(alpha = 0.05f),
                        contentColor = Color(0xFFE53935)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE53935))
                ) {
                    Text("Likuidasi Usaha (Tutup Holding)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    
    if (showLiquidateDialog) {
        AlertDialog(
            onDismissRequest = { showLiquidateDialog = false },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color(0xFFE53935),
            textContentColor = Color.LightGray,
            title = { Text("Likuidasi Mega Holding?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menjual dan menutup seluruh jaringan Theme Park ini? Semua aset lahan dan wahana akan dilikuidasi. Aksi ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.liquidateBusiness(instanceId)
                        showLiquidateDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Ya, Likuidasi", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLiquidateDialog = false }) {
                    Text("Batal", color = Color.LightGray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showAcquisitionDialog) {
        ModalBottomSheet(
            onDismissRequest = { showAcquisitionDialog = false },
            containerColor = Color(0xFF1E1E1E)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxHeight(0.8f)) {
                Text("Pilih Tipe Lahan", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(ThemeParkLandType.entries.toTypedArray()) { land ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable {
                                    if (owned.companyCash >= land.basePrice) {
                                        selectedLandToBuy = land
                                    } else {
                                        showErrorInsufficientFunds = true
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
                        ) {
                            Box(modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(16.dp)) {
                                Column {
                                    Text(land.locationName, color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Harga: ${formatCurrencyRingkas(land.basePrice.toDouble(), useShortFormat)}", color = Color.White, fontSize = 14.sp)
                                        Text("${land.biddingMonths} Bulan", color = Color.LightGray, fontSize = 14.sp)
                                    }
                                    Text("Kapasitas: ${land.maxSlots} Wahana", color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCapitalDialog) {
        AlertDialog(
            onDismissRequest = { showCapitalDialog = false },
            title = { Text(if (actionType == "tarik") "Tarik Kas Internal" else "Suntik Modal") },
            text = {
                Column {
                    Text("Kas Internal Divisi: ${formatCurrencyRingkas(owned.companyCash, useShortFormat)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Kas Induk ($parentName): ${formatCurrencyRingkas(parentCashDesc.toDouble(), useShortFormat)}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = capitalInput,
                        onValueChange = { newValue ->
                            val digits = newValue.filter { it.isDigit() }
                            if (digits.isEmpty()) { capitalInput = "" } else {
                                val p = digits.toLongOrNull()
                                if (p != null) capitalInput = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(p)
                            }
                        },
                        label = { Text("Jumlah (USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        leadingIcon = { Text("$", modifier = Modifier.padding(start = 12.dp)) }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val parsedInput = capitalInput.replace(",", "").toLongOrNull() ?: 0L
                    if (parsedInput > 0) {
                        if (actionType == "tarik") {
                            viewModel.withdrawCapitalFromBusiness(instanceId, parsedInput)
                        } else {
                            viewModel.injectCapitalToBusiness(instanceId, parsedInput)
                        }
                    }
                    showCapitalDialog = false
                }) {
                    Text("Konfirmasi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCapitalDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showErrorInsufficientFunds) {
        AlertDialog(
            onDismissRequest = { showErrorInsufficientFunds = false },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color(0xFFE53935),
            textContentColor = Color.LightGray,
            title = { Text("Dana Tidak Mencukupi", fontWeight = FontWeight.Bold) },
            text = { Text("Kas Internal Divisi tidak cukup untuk mengakuisisi lahan ini. Silakan suntik dana dari Holding terlebih dahulu.") },
            confirmButton = {
                TextButton(onClick = { showErrorInsufficientFunds = false }) {
                    Text("Tutup", color = Color.White)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    selectedLandToBuy?.let { land ->
        AlertDialog(
            onDismissRequest = { selectedLandToBuy = null },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color(0xFFFFD700),
            textContentColor = Color.White,
            title = { Text("Konfirmasi Mulai Negosiasi", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Mulai negosiasi pencarian lahan di:", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(land.locationName, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estimasi Harga Dasar: $${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(land.basePrice)}")
                    Text("Kapasitas: ${land.maxSlots} Slot Wahana")
                    Text("Periode Kontak Awal: ${land.biddingMonths} Bulan")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.purchaseThemeParkLand(instanceId, land)
                        selectedLandToBuy = null
                        showAcquisitionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.05f),
                        contentColor = Color(0xFFFFD700)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Mulai Negosiasi Lahan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLandToBuy = null }) {
                    Text("Batal", color = Color.LightGray)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    selectedBidding?.let { bidding ->
        if (bidding.phase == com.example.data.BiddingPhase.OWNER_COUNTERED) {
            var offerInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { selectedBidding = null },
                containerColor = Color(0xFF1E1E1E),
                title = { Text("Negosiasi Harga Lahan", color = Color(0xFFFFD700)) },
                text = {
                    Column {
                        Text("Pemilik lahan di ${bidding.landType.locationName} meminta harga $${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(bidding.currentAskingPrice)}!", color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tawaran Anda:", color = Color.LightGray)
                        OutlinedTextField(
                            value = offerInput,
                            onValueChange = { newValue ->
                                val digits = newValue.filter { it.isDigit() }
                                if (digits.isEmpty()) { offerInput = "" } else {
                                    val p = digits.toLongOrNull()
                                    if (p != null) offerInput = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(p)
                                }
                            },
                            label = { Text("USD") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Text("Hint: Menawarkan harga 20-40% di atas permintaan akan memperbesar peluang disetujui (minimal tawar seharga permintaan).", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsedOffer = offerInput.replace(",", "").toLongOrNull() ?: 0L
                            if (parsedOffer > 0) {
                                viewModel.submitThemeParkBiddingOffer(instanceId, bidding.id, parsedOffer)
                                selectedBidding = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                    ) {
                        Text("Kirim (Tunggu 2 Bulan)")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        viewModel.cancelThemeParkBidding(instanceId, bidding.id)
                        selectedBidding = null 
                    }) {
                        Text("Batalkan (Tarik Diri)", color = Color.Red)
                    }
                }
            )
        } else if (bidding.phase == com.example.data.BiddingPhase.DEAL_REACHED) {
            AlertDialog(
                onDismissRequest = { selectedBidding = null },
                containerColor = Color(0xFF1E1E1E),
                title = { Text("Tanda Tangan Akta Lahan", color = Color(0xFF00FF00)) },
                text = {
                    Column {
                        Text("Kesepakatan tercapai di harga $${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(bidding.currentAskingPrice)}.", color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (owned.companyCash < bidding.currentAskingPrice) {
                            Text("Pemberitahuan: Kas Divisi tidak mencukupi untuk menandatangani akta. Suntik dana terlebih dahulu.", color = Color.Red)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (owned.companyCash >= bidding.currentAskingPrice) {
                                val success = viewModel.resolveThemeParkBiddingDeal(instanceId, bidding.id)
                                if (!success) {
                                    showErrorInsufficientFunds = true
                                }
                                selectedBidding = null
                            } else {
                                showErrorInsufficientFunds = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00), contentColor = Color.Black)
                    ) {
                        Text("Beli Lahan ($${formatCurrencyRingkas(bidding.currentAskingPrice.toDouble(), useShortFormat)})")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedBidding = null }) { Text("Tunda Dulu", color = Color.LightGray) }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { selectedBidding = null },
                containerColor = Color(0xFF1E1E1E),
                title = { Text("Negosiasi Sedang Berjalan", color = Color.White) },
                text = { Text("Harap tunggu hingga pemilik lahan merespons. Sisa waktu penungguan: ${bidding.monthsLeft} bulan.", color = Color.LightGray) },
                confirmButton = {
                    TextButton(onClick = { selectedBidding = null }) { Text("Tutup", color = Color(0xFFFFD700)) }
                }
            )
        }
    }
}

@Composable
fun ThemeParkBiddingCard(bidding: com.example.data.ActiveBidding, onClick: () -> Unit, useShortFormat: Boolean) {
    val phaseProps = when (bidding.phase) {
        com.example.data.BiddingPhase.WAITING_INITIAL -> Pair(Color.Gray, "Menunggu Respons Pemilik (${bidding.monthsLeft} bln)")
        com.example.data.BiddingPhase.WAITING_REPLY -> Pair(Color.Gray, "Menunggu Konfirmasi Tawaran Anda (${bidding.monthsLeft} bln)")
        com.example.data.BiddingPhase.OWNER_COUNTERED -> Pair(Color(0xFFFFA000), "Pemilik Meminta Harga Baru! Membutuhkan Aksi.")
        com.example.data.BiddingPhase.DEAL_REACHED -> Pair(Color(0xFF00FF00), "Kesepakatan Tercapai! Konfirmasi Pembelian.")
        com.example.data.BiddingPhase.REJECTED -> Pair(Color.Red, "Ditolak")
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.border(1.dp, phaseProps.first.copy(alpha = 0.5f), RoundedCornerShape(12.dp)).padding(16.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Lahan ${bidding.landType.locationName}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Surface(color = phaseProps.first.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                        Text("Bidding Aktif", color = phaseProps.first, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (bidding.phase == com.example.data.BiddingPhase.OWNER_COUNTERED || bidding.phase == com.example.data.BiddingPhase.DEAL_REACHED) {
                    Text("Diminta: $${formatCurrencyRingkas(bidding.currentAskingPrice.toDouble(), useShortFormat)}", color = Color.White, fontSize = 14.sp)
                }
                Text(phaseProps.second, color = phaseProps.first, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ThemeParkBranchCard(branch: com.example.data.ThemeParkBranch, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Box(modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)).padding(16.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(branch.customName ?: "Tanpa Nama (${branch.locationName})", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Surface(
                        color = Color(0xFFFFD700).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(branch.landType.locationName, color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                val statusText = branch.currentStatus
                val statusColor = when(statusText) {
                    "Negosiasi Lahan" -> Color.Gray
                    "Tahap Perencanaan" -> Color.LightGray
                    "Pembangunan Infrastruktur" -> Color(0xFFFFD700)
                    "Beroperasi" -> Color(0xFF00FF00)
                    else -> Color.White
                }
                
                if (branch.remainingBiddingMonths > 0) {
                    Text("Status: Negosiasi Lahan Berjalan", color = Color.Gray, fontSize = 12.sp)
                    Text("Sisa Waktu: ${branch.remainingBiddingMonths} Bulan", color = Color(0xFFFFD700), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                } else {
                    Text("Status: $statusText", color = statusColor, fontSize = 14.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Wahana: ${branch.rides.size} / ${branch.landType.maxSlots}", color = Color.LightGray, fontSize = 12.sp)
                        if (branch.isLaunched) {
                            Text("Profit: $${com.example.ui.formatCurrencyRingkas(branch.lastMonthProfit, true)}", color = if (branch.lastMonthProfit >= 0) Color(0xFF00E676) else Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

