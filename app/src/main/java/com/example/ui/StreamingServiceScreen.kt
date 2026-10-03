package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.data.*
import com.example.viewmodel.GameViewModel
import java.text.NumberFormat
import java.util.Locale

// 🎨 THEME COLORS: Modern Cinema & Cyber OTT Neon
private val bgDark = Color(0xFF090A10)
private val cardDark = Color(0xFF131520)
private val cardElevated = Color(0xFF1B1E2E)
private val ottRed = Color(0xFFE50914)
private val ottCyan = Color(0xFF00E5FF)
private val ottPurple = Color(0xFF9D4EDD)
private val ottGold = Color(0xFFFFB703)
private val ottGreen = Color(0xFF06D6A0)
private val textGray = Color(0xFF9EA3B5)
private val textMuted = Color(0xFF6B7280)

enum class OttTab(val title: String, val icon: String) {
    CATALOG("Katalog Tayangan", "🎬"),
    PULL_CONTENT("Tarik Film Sendiri", "📥"),
    LICENSING_DEALS("Bursa Lisensi", "📜"),
    SERVER_TECH("Server & Teknologi", "⚡")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamingServiceScreen(
    navController: NavController,
    gameViewModel: GameViewModel,
    businessInstanceId: String? = null
) {
    val playerState by gameViewModel.playerState.collectAsState()
    val business = if (!businessInstanceId.isNullOrEmpty()) {
        playerState.ownedBusinesses.find { it.instanceId == businessInstanceId }
            ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == businessInstanceId }
            ?: playerState.ownedBusinesses.find { it.catalogId == "streaming_service" }
            ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.catalogId == "streaming_service" }
    } else {
        playerState.ownedBusinesses.find { it.catalogId == "streaming_service" }
            ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.catalogId == "streaming_service" }
    }

    if (business == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("OTT & Streaming Service", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
                )
            },
            containerColor = bgDark
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Icon(Icons.Default.LiveTv, contentDescription = null, tint = textGray, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Bisnis Belum Dimiliki", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Anda belum mendirikan unit bisnis platform OTT / Streaming Service (Modal $1.000.000).", color = textGray, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = ottRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Kembali ke Menu Utama", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val streamingData = business.streamingData
    val currentServerSpec = STREAMING_SERVER_TIERS.find { it.tier == streamingData.serverTier }
        ?: STREAMING_SERVER_TIERS.first()

    val currFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val numFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    var selectedTab by remember { mutableStateOf(OttTab.CATALOG) }

    // Dialog States
    var showSuntikDialog by remember { mutableStateOf(false) }
    var showTarikDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showProduceOriginalDialog by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successToast by remember { mutableStateOf<String?>(null) }

    // External Film Offers (Marketplace)
    var externalOffers by remember { mutableStateOf(SAMPLE_EXTERNAL_FILM_OFFERS) }

    // In-House Film Studios & Creator Channels
    val filmStudios = remember(playerState) {
        (playerState.ownedBusinesses.filter { it.catalogId == "media_production" } +
         playerState.holdingCompanies.flatMap { it.subsidiaries }.filter { it.catalogId == "media_production" }).distinctBy { it.instanceId }
    }
    val contentCreators = remember(playerState) {
        (playerState.ownedBusinesses.filter { it.catalogId == "content_creator" } +
         playerState.holdingCompanies.flatMap { it.subsidiaries }.filter { it.catalogId == "content_creator" }).distinctBy { it.instanceId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = business.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = ottRed,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "OTT PLATFORM",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Level ${business.level} • ${numFormat.format(streamingData.subscribers)} Pelanggan Aktif",
                            color = textGray,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showSuntikDialog = true }) {
                        Icon(Icons.Default.AddCard, contentDescription = "Suntik Dana", tint = ottGreen)
                    }
                    IconButton(onClick = { showTarikDialog = true }) {
                        Icon(Icons.Default.Payments, contentDescription = "Tarik Dana", tint = ottGold)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus Bisnis", tint = Color.Red.copy(alpha = 0.7f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
            )
        },
        containerColor = bgDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Server Alert / Outage Warning
            if (streamingData.serverOutage) {
                Surface(
                    color = ottRed.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ottRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = ottRed, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "SERVER OVERLOAD & OUTAGE!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                "Trafik (${numFormat.format(streamingData.currentTraffic)}) melampaui kapasitas server! Segera upgrade server untuk menghindari kehilangan jutaan subscriber.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Success Toast
            if (successToast != null) {
                Surface(
                    color = ottGreen.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, ottGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = successToast ?: "",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Top Quick Metrics
            OttMetricsHeader(
                streamingData = streamingData,
                serverSpec = currentServerSpec,
                currFormat = currFormat,
                numFormat = numFormat
            )

            // Horizontal Tab Selector
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = cardDark,
                contentColor = Color.White,
                edgePadding = 16.dp,
                divider = {}
            ) {
                OttTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tab.icon, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    tab.title,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == tab) ottCyan else textGray
                                )
                            }
                        }
                    )
                }
            }

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    OttTab.CATALOG -> {
                        OttCatalogTab(
                            streamingData = streamingData,
                            numFormat = numFormat,
                            onProduceClick = { showProduceOriginalDialog = true },
                            onTerminate = { contentId ->
                                gameViewModel.terminateStreamingContract(contentId, business.instanceId)
                                successToast = "Tayangan telah dihentikan/dihapus dari katalog."
                            }
                        )
                    }
                    OttTab.PULL_CONTENT -> {
                        OttPullContentTab(
                            filmStudios = filmStudios,
                            contentCreators = contentCreators,
                            streamingData = streamingData,
                            onPullFromStudio = { studioId, title ->
                                val success = gameViewModel.pullFromFilmStudioToOtt(studioId, title, business.instanceId)
                                if (success) {
                                    successToast = "Film '$title' berhasil ditarik dan disiarkan eksklusif di OTT!"
                                }
                            },
                            onPullFromCreator = { creatorId, title ->
                                val success = gameViewModel.pullFromContentCreatorBankToOtt(creatorId, title, business.instanceId)
                                if (success) {
                                    successToast = "Karya '$title' dari Bank Konten berhasil dimasukkan ke katalog OTT!"
                                }
                            },
                            onProduceOriginal = { showProduceOriginalDialog = true }
                        )
                    }
                    OttTab.LICENSING_DEALS -> {
                        OttLicensingMarketTab(
                            offers = externalOffers,
                            streamingData = streamingData,
                            currFormat = currFormat,
                            numFormat = numFormat,
                            onSignContract = { offer ->
                                val success = gameViewModel.addLicensedFilmContract(offer, business.instanceId)
                                if (success) {
                                    externalOffers = externalOffers.filterNot { it.id == offer.id }
                                    successToast = "Kontrak lisensi '${offer.title}' dari ${offer.studioPartner} resmi diteken!"
                                }
                            }
                        )
                    }
                    OttTab.SERVER_TECH -> {
                        OttServerTechTab(
                            streamingData = streamingData,
                            currFormat = currFormat,
                            numFormat = numFormat,
                            onUpgradeServer = {
                                val success = gameViewModel.upgradeServerTier(business.instanceId)
                                if (success) {
                                    successToast = "Infrastruktur server berhasil diupgrade!"
                                }
                            },
                            onUpgradeTech = { techType ->
                                val success = gameViewModel.upgradeStreamingTech(techType, business.instanceId)
                                if (success) {
                                    successToast = "Peningkatan teknologi $techType berhasil diaplikasikan!"
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. Suntik Modal Dialog
    if (showSuntikDialog) {
        Dialog(onDismissRequest = { showSuntikDialog = false; errorMessage = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardDark,
                border = BorderStroke(1.dp, ottGreen.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Suntik Kas ke OTT Platform", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Kas Utama Anda: ${currFormat.format(playerState.cash)}", color = textGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Jumlah Dana ($)", color = textGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ottGreen,
                            unfocusedBorderColor = textMuted
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage ?: "", color = ottRed, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showSuntikDialog = false; errorMessage = null }) {
                            Text("Batal", color = textGray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = amountInput.toLongOrNull() ?: 0L
                                if (amt <= 0) {
                                    errorMessage = "Jumlah dana tidak valid!"
                                } else if (amt > playerState.cash) {
                                    errorMessage = "Kas utama tidak mencukupi!"
                                } else {
                                    gameViewModel.injectCashToStreamingService(amt, business.instanceId)
                                    showSuntikDialog = false
                                    amountInput = ""
                                    errorMessage = null
                                    successToast = "Berhasil menyuntikkan ${currFormat.format(amt)} ke Kas OTT!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ottGreen)
                        ) {
                            Text("Suntik Dana", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 2. Tarik Kas Dialog
    if (showTarikDialog) {
        Dialog(onDismissRequest = { showTarikDialog = false; errorMessage = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardDark,
                border = BorderStroke(1.dp, ottGold.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Tarik Kas dari OTT Platform", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Kas OTT Tersedia: ${currFormat.format(streamingData.streamingCash)}", color = textGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Jumlah Penarikan ($)", color = textGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ottGold,
                            unfocusedBorderColor = textMuted
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMessage ?: "", color = ottRed, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showTarikDialog = false; errorMessage = null }) {
                            Text("Batal", color = textGray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = amountInput.toLongOrNull() ?: 0L
                                if (amt <= 0) {
                                    errorMessage = "Jumlah penarikan tidak valid!"
                                } else if (amt > streamingData.streamingCash) {
                                    errorMessage = "Kas OTT tidak mencukupi!"
                                } else {
                                    gameViewModel.withdrawCashFromStreamingService(amt, business.instanceId)
                                    showTarikDialog = false
                                    amountInput = ""
                                    errorMessage = null
                                    successToast = "Berhasil menarik ${currFormat.format(amt)} ke Kas Utama!"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ottGold)
                        ) {
                            Text("Tarik Dana", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 3. Produksi Original OTT Dialog
    if (showProduceOriginalDialog) {
        var prodTitle by remember { mutableStateOf("") }
        var prodGenre by remember { mutableStateOf("Action / Thriller") }
        var prodType by remember { mutableStateOf(StreamingContentType.ORIGINAL_SERIES) }
        var prodBudget by remember { mutableStateOf("500000") }
        var prodSynopsis by remember { mutableStateOf("") }
        var prodError by remember { mutableStateOf<String?>(null) }

        Dialog(onDismissRequest = { showProduceOriginalDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardDark,
                border = BorderStroke(1.dp, ottPurple.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("🎬 Produksi Tayangan Original OTT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Biayai produksi serial orisinal eksklusif untuk platform Anda. Menjadi magnet subscriber baru!", color = textGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = prodTitle,
                        onValueChange = { prodTitle = it },
                        label = { Text("Judul Tayangan", color = textGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ottPurple
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Tipe Format", color = textGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StreamingContentType.values().take(3).forEach { t ->
                            FilterChip(
                                selected = prodType == t,
                                onClick = { prodType = t },
                                label = { Text(t.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ottPurple,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = prodGenre,
                        onValueChange = { prodGenre = it },
                        label = { Text("Genre (e.g. Sci-Fi / Drama)", color = textGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ottPurple
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = prodBudget,
                        onValueChange = { prodBudget = it },
                        label = { Text("Anggaran Produksi ($)", color = textGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ottPurple
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = prodSynopsis,
                        onValueChange = { prodSynopsis = it },
                        label = { Text("Sinopsis Singkat", color = textGray) },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ottPurple
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (prodError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(prodError ?: "", color = ottRed, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showProduceOriginalDialog = false }) {
                            Text("Batal", color = textGray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val b = prodBudget.toLongOrNull() ?: 0L
                                if (prodTitle.isBlank()) {
                                    prodError = "Judul tayangan tidak boleh kosong!"
                                } else if (b <= 0) {
                                    prodError = "Anggaran produksi harus lebih dari 0!"
                                } else if (b > streamingData.streamingCash) {
                                    prodError = "Kas OTT tidak mencukupi! Silakan suntik modal terlebih dahulu."
                                } else {
                                    val success = gameViewModel.produceOriginalOttTitle(
                                        title = prodTitle,
                                        type = prodType,
                                        genre = prodGenre,
                                        budget = b,
                                        synopsis = prodSynopsis,
                                        targetInstanceId = business.instanceId
                                    )
                                    if (success) {
                                        showProduceOriginalDialog = false
                                        successToast = "Produksi '$prodTitle' sukses dirilis ke platform OTT!"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ottPurple)
                        ) {
                            Text("Mulai Produksi & Rilis", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // 4. Tutup Bisnis Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Tutup Unit Bisnis OTT?", color = Color.White) },
            text = { Text("Seluruh katalog, infrastruktur server, dan basis subscriber akan dihapus secara permanen.", color = textGray) },
            confirmButton = {
                Button(
                    onClick = {
                        gameViewModel.deleteStreamingServiceBusiness(business.instanceId)
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Ya, Tutup Bisnis", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = textGray)
                }
            },
            containerColor = cardDark
        )
    }
}

// ==========================================
// SUB-COMPONENTS: HEADER & METRICS
// ==========================================

@Composable
private fun OttMetricsHeader(
    streamingData: StreamingServiceData,
    serverSpec: StreamingServerSpec,
    currFormat: NumberFormat,
    numFormat: NumberFormat
) {
    val trafficPercentage = (streamingData.currentTraffic.toFloat() / serverSpec.maxCapacity.toFloat()).coerceIn(0f, 1.5f)
    val isNearCapacity = trafficPercentage >= 0.8f

    Surface(
        color = cardDark,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (streamingData.serverOutage) ottRed else cardElevated)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Row 1: Subscribers & Cash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("TOTAL SUBSCRIBERS", color = textGray, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = numFormat.format(streamingData.subscribers),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+$${numFormat.format((streamingData.subscribers * streamingData.subscriptionFee).toLong())}/bln",
                        color = ottGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("KAS OTT PLATFORM", color = textGray, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = currFormat.format(streamingData.streamingCash),
                        color = ottGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Biaya Server: -$${numFormat.format(serverSpec.monthlyUpkeep)}/bln",
                        color = ottRed.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 2: Traffic vs Server Capacity Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (streamingData.serverOutage) Icons.Default.CloudOff else Icons.Default.CloudQueue,
                        contentDescription = null,
                        tint = if (streamingData.serverOutage) ottRed else if (isNearCapacity) ottGold else ottCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${serverSpec.name} (Tier ${serverSpec.tier})",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "${numFormat.format(streamingData.currentTraffic)} / ${numFormat.format(serverSpec.maxCapacity)} Penonton",
                    color = if (streamingData.serverOutage) ottRed else if (isNearCapacity) ottGold else textGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { trafficPercentage.coerceAtMost(1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (streamingData.serverOutage) ottRed else if (isNearCapacity) ottGold else ottCyan,
                trackColor = cardElevated
            )
        }
    }
}

// ==========================================
// TAB 1: KATALOG TAYANGAN OTT
// ==========================================

@Composable
private fun OttCatalogTab(
    streamingData: StreamingServiceData,
    numFormat: NumberFormat,
    onProduceClick: () -> Unit,
    onTerminate: (String) -> Unit
) {
    val catalog = streamingData.streamingCatalog

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Koleksi Tayangan (${catalog.size} Judul)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = onProduceClick,
                colors = ButtonDefaults.buttonColors(containerColor = ottPurple),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Produksi Baru", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (catalog.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MovieFilter, contentDescription = null, tint = textGray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Katalog OTT Masih Kosong", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Tarik film dari studio sendiri atau beli lisensi penayangan.", color = textGray, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(catalog, key = { it.id }) { item ->
                    OttCatalogItemCard(item = item, numFormat = numFormat, onTerminate = { onTerminate(item.id) })
                }
            }
        }
    }
}

@Composable
private fun OttCatalogItemCard(
    item: StreamingContent,
    numFormat: NumberFormat,
    onTerminate: () -> Unit
) {
    Surface(
        color = cardDark,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, if (item.isViral) ottGold.copy(alpha = 0.6f) else cardElevated),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.type.icon, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.genre} • ${item.source.displayName}",
                        color = textGray,
                        fontSize = 11.sp
                    )
                }

                // Rating Badge
                Surface(
                    color = if (item.rating >= 80) ottGreen.copy(alpha = 0.2f) else cardElevated,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, if (item.rating >= 80) ottGreen else textMuted)
                ) {
                    Text(
                        text = "★ ${item.rating}/100",
                        color = if (item.rating >= 80) ottGreen else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (item.synopsis.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.synopsis,
                    color = textGray,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = cardElevated, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = ottCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${numFormat.format(item.views)} Streamed",
                        color = Color.White,
                        fontSize = 11.sp
                    )

                    if (item.isViral) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            color = ottGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "🔥 VIRAL HIT",
                                color = ottGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (item.source == StreamingContentSource.LICENSED_CONTRACT) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${item.licenseExpiryMonths} bln sisa",
                            color = if (item.licenseExpiryMonths <= 3) ottRed else textGray,
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(onClick = onTerminate, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Putus Kontrak", tint = textMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: TARIK DARI STUDIO & BANK KONTEN
// ==========================================

@Composable
private fun OttPullContentTab(
    filmStudios: List<OwnedBusiness>,
    contentCreators: List<OwnedBusiness>,
    streamingData: StreamingServiceData,
    onPullFromStudio: (String, String) -> Unit,
    onPullFromCreator: (String, String) -> Unit,
    onProduceOriginal: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Option 1: Produksi Original OTT Banner
        Surface(
            color = cardDark,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ottPurple.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎬", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Produksi Orisinal OTT Platform", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Garap serial atau film eksklusif tanpa perantara distributor.", color = textGray, fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onProduceOriginal,
                    colors = ButtonDefaults.buttonColors(containerColor = ottPurple),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Mulai Produksi Orisinal", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Option 2: Tarik Film dari Studio Film Sendiri
        Text("Studio Produksi Film Sendiri", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        if (filmStudios.isEmpty()) {
            Surface(
                color = cardDark,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Anda belum memiliki unit bisnis Studio Film (Media Production). Dirikan studio film untuk menayangkan film karya sendiri di OTT.",
                    color = textGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            filmStudios.forEach { studio ->
                val projects = studio.projectHistory.filter { p ->
                    streamingData.streamingCatalog.none { it.title.equals(p.title, ignoreCase = true) }
                }

                Surface(
                    color = cardDark,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Videocam, contentDescription = null, tint = ottCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(studio.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (projects.isEmpty()) {
                            Text("Belum ada proyek film selesai yang siap ditarik.", color = textGray, fontSize = 11.sp)
                        } else {
                            projects.forEach { proj ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(proj.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Skor ${proj.reviewScore}/100 • ${proj.filmFormat}", color = textGray, fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { onPullFromStudio(studio.instanceId, proj.title) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ottCyan),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Tayangkan di OTT", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Option 3: Tarik dari Bank Konten Creator Channel
        Text("Bank Konten dari Channel Kreator", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        if (contentCreators.isEmpty()) {
            Surface(
                color = cardDark,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Anda belum memiliki unit bisnis Content Creator. Buat channel YouTube/kreator untuk memproduksi film dokumenter & serial pendek.",
                    color = textGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            contentCreators.forEach { creator ->
                val works = creator.contentPortfolio.filter { w ->
                    streamingData.streamingCatalog.none { it.title.equals(w.title, ignoreCase = true) }
                }

                Surface(
                    color = cardDark,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = ottGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(creator.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        if (works.isEmpty()) {
                            Text("Belum ada karya di Bank Konten yang belum ditayangkan.", color = textGray, fontSize = 11.sp)
                        } else {
                            works.forEach { work ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(work.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${work.type.displayName} • Skor: ${work.engagementScore ?: 75}", color = textGray, fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { onPullFromCreator(creator.instanceId, work.title) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ottGold),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Impor ke OTT", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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

// ==========================================
// TAB 3: BURSA KONTRAK LISENSI EKSTERNAL
// ==========================================

@Composable
private fun OttLicensingMarketTab(
    offers: List<ExternalFilmContractOffer>,
    streamingData: StreamingServiceData,
    currFormat: NumberFormat,
    numFormat: NumberFormat,
    onSignContract: (ExternalFilmContractOffer) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Bursa Lisensi Film & Serial Global",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Beli hak siar eksklusif film pemenang box office internasional untuk memicu ledakan subscriber.",
            color = textGray,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (offers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Semua tawaran lisensi telah diteken.", color = textGray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(offers, key = { it.id }) { offer ->
                    val canAfford = streamingData.streamingCash >= offer.upfrontLicenseCost

                    Surface(
                        color = cardDark,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (offer.isViralCandidate) ottGold.copy(alpha = 0.8f) else cardElevated),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(offer.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${offer.genre} • Studio: ${offer.studioPartner}", color = textGray, fontSize = 11.sp)
                                }
                                Surface(
                                    color = ottGold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "★ ${offer.rating}/100",
                                        color = ottGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(offer.synopsis, color = textGray, fontSize = 11.sp)

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = cardElevated, thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Biaya Lisensi Awal: ${currFormat.format(offer.upfrontLicenseCost)}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Text("Royalti Bulanan: ${currFormat.format(offer.monthlyLicenseFee)} (${offer.contractDurationMonths} Bulan)", color = textGray, fontSize = 10.sp)
                                }
                                Button(
                                    onClick = { onSignContract(offer) },
                                    enabled = canAfford,
                                    colors = ButtonDefaults.buttonColors(containerColor = ottRed),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (canAfford) "Teken Kontrak" else "Kas Kurang",
                                        color = Color.White,
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
}

// ==========================================
// TAB 4: SERVER & TEKNOLOGI
// ==========================================

@Composable
private fun OttServerTechTab(
    streamingData: StreamingServiceData,
    currFormat: NumberFormat,
    numFormat: NumberFormat,
    onUpgradeServer: () -> Unit,
    onUpgradeTech: (String) -> Unit
) {
    val currentTier = streamingData.serverTier
    val nextTierSpec = STREAMING_SERVER_TIERS.getOrNull(currentTier)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Server Upgrade Card
        Surface(
            color = cardDark,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ottCyan.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = ottCyan, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Infrastruktur Server Cloud & CDN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Tingkatkan kapasitas penonton serentak untuk mencegah server crash saat film viral.", color = textGray, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (nextTierSpec != null) {
                    val canAfford = streamingData.streamingCash >= nextTierSpec.upgradeCost
                    Text("Tingkat Berikutnya: ${nextTierSpec.name} (Tier ${nextTierSpec.tier})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Kapasitas: ${numFormat.format(nextTierSpec.maxCapacity)} Penonton • Biaya Upkeep: ${currFormat.format(nextTierSpec.monthlyUpkeep)}/bln", color = textGray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onUpgradeServer,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(containerColor = ottCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (canAfford) "Upgrade Server (${currFormat.format(nextTierSpec.upgradeCost)})" else "Kas OTT Kurang (${currFormat.format(nextTierSpec.upgradeCost)})",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Surface(
                        color = ottGreen.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Tier Maksimal! Infrastruktur beroperasi pada Sovereign Global Fiber Grid.",
                            color = ottGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Tech Upgrades Section
        Text("Peningkatan Teknologi & Fitur Platform", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        val techItems = listOf(
            Triple("ENCODING", "4K HDR & Dolby Atmos Encoding (Lvl ${streamingData.encodingLevel}/5)", "Meningkatkan kepuasan penonton dan retensi subscription."),
            Triple("AI_ALGO", "AI Recommendation Algorithm (Lvl ${streamingData.aiAlgorithmLevel}/5)", "Meningkatkan engagement penonton dan mempermudah film viral."),
            Triple("DRM", "Widevine DRM & Anti-Piracy (Lvl ${streamingData.drmProtectionLevel}/5)", "Mencegah pembajakan dan memaksimalkan pendapatan lisensi."),
            Triple("LOCALIZATION", "Multi-Language AI Dubbing (Lvl ${streamingData.localizationLevel}/5)", "Membuka pasar penonton mancanegara dan ekspansi global."),
            Triple("MARKETING", "Global Billboard & Ad Blitz (Lvl ${streamingData.marketingTier}/5)", "Mempercepat pertumbuhan subscriber bulanan secara masif.")
        )

        techItems.forEach { (type, title, desc) ->
            Surface(
                color = cardDark,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(desc, color = textGray, fontSize = 10.sp)
                    }
                    Button(
                        onClick = { onUpgradeTech(type) },
                        colors = ButtonDefaults.buttonColors(containerColor = ottPurple),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Upgrade", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
