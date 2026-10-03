package com.example.ui.lifestyle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifestyleDashboardScreen(
    navController: NavHostController,
    viewModel: GameViewModel
) {
    val playerState by viewModel.playerState.collectAsState()
    
    val darkBg = Color(0xFF0F1319)
    val neonGreen = Color(0xFF00FF00)
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Langganan", "Tech Gadget", "Ekspedisi", "Filantropi", "Wellness & Proteksi", "Pengeluaran Kustom")
    
    // Modals state
    var editingItem by remember { mutableStateOf<com.example.data.LifestyleItem?>(null) }
    var showAddModal by remember { mutableStateOf(false) }

    // Map selected tab to tabCategory key
    val currentTabCategory = when (selectedTab) {
        0 -> "langganan"
        1 -> "gadget"
        2 -> "ekspedisi"
        3 -> "filantropi"
        4 -> "wellness"
        else -> "pengeluaran_kustom"
    }

    // Filter items based on selected tab
    val filteredItems = playerState.allSubscriptions.filter { it.tabCategory == currentTabCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gaya Hidup & Pengeluaran Pribadi",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBg)
            )
        },
        containerColor = darkBg
    ) { paddingValues ->
        // Single Layer Scroll using a single LazyColumn
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Header: Kas Pribadi (Always visible for excellent gameplay clarity)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .border(2.dp, neonGreen.copy(alpha = 0.35f), RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1D12))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👑 KAS PRIBADI CEO",
                            color = neonGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$" + String.format("%,d", playerState.privateBalance),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Danai gaya hidup berkelas dari dividen & gaji murni",
                            color = Color(0xFF90A4AE),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 2. Navigation TabRow
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = neonGreen,
                    edgePadding = 20.dp,
                    divider = { HorizontalDivider(color = Color(0xFF232B36)) },
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = neonGreen
                            )
                        }
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) neonGreen else Color(0xFF90A4AE)
                                )
                            }
                        )
                    }
                }
            }

            // 3. Info Card showing Total Cost based on Tab
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    when (selectedTab) {
                        0 -> {
                            val activeSubs = filteredItems.filter { it.isActive }
                            val totalCost = activeSubs.sumOf { it.price }
                            InfoCard(
                                title = "Total Biaya Langganan Bulanan",
                                value = "$${String.format("%,d", totalCost)} / bln",
                                subtitle = "${activeSubs.size} Langganan Aktif",
                                themeColor = Color(0xFF00FF00),
                                cardBg = Color(0xFF0B1F11)
                            )
                        }
                        1 -> {
                            val ownedGadgetsCount = filteredItems.filter { it.isOwned }.size
                            InfoCard(
                                title = "Koleksi Gadget Premium & AI",
                                value = "$ownedGadgetsCount Dimiliki",
                                subtitle = "Investasi perangkat keras eksekutif CEO",
                                themeColor = Color(0xFF2196F3),
                                cardBg = Color(0xFF0B1724)
                            )
                        }
                        2 -> {
                            InfoCard(
                                title = "Total Perjalanan Liburan",
                                value = "${playerState.travelHistory} Perjalanan",
                                subtitle = "Aktivitas Healing & Rekreasi CEO",
                                themeColor = Color(0xFFFF9800),
                                cardBg = Color(0xFF22160C)
                            )
                        }
                        3 -> {
                            InfoCard(
                                title = "Total Donasi Filantropi",
                                value = "$${String.format("%,d", playerState.totalCharityDonated)}",
                                subtitle = "Meningkatkan status sosial CEO secara global",
                                themeColor = Color(0xFFE91E63),
                                cardBg = Color(0xFF240D16)
                            )
                        }
                        4 -> {
                            val activeWellness = filteredItems.filter { it.isActive }
                            val totalCost = activeWellness.sumOf { it.price }
                            InfoCard(
                                title = "Total Biaya Wellness & Proteksi",
                                value = "$${String.format("%,d", totalCost)} / bln",
                                subtitle = "${activeWellness.size} Layanan Proteksi Aktif",
                                themeColor = Color(0xFF9C27B0),
                                cardBg = Color(0xFF221124)
                            )
                        }
                        else -> {
                            val fundedList = filteredItems.filter { it.isOwned || it.isActive }
                            val totalSpent = filteredItems.filter { it.isOwned }.sumOf { it.price * it.fundedCount.coerceAtLeast(1) }
                            val monthlyCost = filteredItems.filter { it.isActive && it.isRecurring }.sumOf { it.price }
                            val subtitleText = if (monthlyCost > 0L) {
                                "${fundedList.size} Teralokasi • $${String.format("%,d", monthlyCost)}/bln Rutin"
                            } else {
                                "${fundedList.size} Proposal & Pengeluaran Teralokasi"
                            }
                            InfoCard(
                                title = "Total Pengeluaran Kustom & Proposal",
                                value = "$${String.format("%,d", totalSpent)}",
                                subtitle = subtitleText,
                                themeColor = Color(0xFF00E5FF),
                                cardBg = Color(0xFF091E24)
                            )
                        }
                    }
                }
            }

            // Special interactive quick card for Pengeluaran Kustom / Proposal
            if (selectedTab == 5) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        CustomExpenseProposalHeaderCard(
                            onAddClick = { showAddModal = true }
                        )
                    }
                }
            }

            // Special interactive manual input card for Filantropi
            if (selectedTab == 3) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        CharityDonationInputCard(viewModel = viewModel, playerState = playerState)
                    }
                }
            }

            // Special premium banner for Private Travel Concierge when in Ekspedisi tab
            if (selectedTab == 2) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate("private_travel_concierge") }
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B160C)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF2E2413)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👑", fontSize = 24.sp)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Private Travel Concierge",
                                        color = Color(0xFFFFD700),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Rancang rute VVIP kustom & konversi hari perjalanan secara dinamis",
                                        color = Color(0xFFCFD8DC),
                                        fontSize = 11.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                                Text("➔", color = Color(0xFFFFD700), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 4. Catalog Title
            item {
                Text(
                    text = when (selectedTab) {
                        0 -> "Katalog Langganan Eksekutif"
                        1 -> "Katalog Gadget Premium & AI"
                        2 -> "Eksplorasi Perjalanan Eksklusif"
                        3 -> "Kampanye Donasi Kemanusiaan"
                        4 -> "Wellness & Proteksi Kelas Atas"
                        else -> "Daftar Proposal & Pengeluaran Personal"
                    },
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
                )
            }

            // 5. Grouped Catalog Items (Grouping by Section)
            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada item di kategori ini.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                val grouped = filteredItems.groupBy { it.sectionName }
                grouped.forEach { (section, itemsInSection) ->
                    // Section Title Item
                    item {
                        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)) {
                            Text(
                                text = section.uppercase(),
                                color = neonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = Color(0xFF232B36), thickness = 1.dp)
                        }
                    }

                    // Render items as a Grid of 2 items per row
                    val chunked = itemsInSection.chunked(2)
                    items(chunked.size) { rowIndex ->
                        val rowItems = chunked[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (i in 0 until 2) {
                                if (i < rowItems.size) {
                                    val item = rowItems[i]
                                    Box(modifier = Modifier.weight(1f)) {
                                        LifestyleItemCard(
                                            item = item,
                                            viewModel = viewModel,
                                            onEditClick = { editingItem = item }
                                        )
                                    }
                                } else {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // 6. Large universal add custom button at the very bottom
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showAddModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(56.dp)
                        .border(1.dp, neonGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("[ + ]", color = neonGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Item Kustom Baru", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal Overlays
    if (editingItem != null) {
        EditLifestyleItemDialog(
            item = editingItem!!,
            viewModel = viewModel,
            onDismiss = { editingItem = null }
        )
    }

    if (showAddModal) {
        AddLifestyleItemDialog(
            viewModel = viewModel,
            initialTabCategory = currentTabCategory,
            onDismiss = { showAddModal = false }
        )
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String,
    subtitle: String,
    themeColor: Color,
    cardBg: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = Color(0xFF90A4AE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    color = themeColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFF78909C),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun CharityDonationInputCard(
    viewModel: GameViewModel,
    playerState: com.example.data.PlayerState
) {
    var donationAmountText by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessMessage by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151921)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Lakukan Donasi Kemanusiaan Bebas",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Masukkan nilai donasi tunai secara bebas untuk mendirikan yayasan sosial, membangun sekolah rakyat, atau mendanai penelitian medis darurat.",
                color = Color(0xFF90A4AE),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = donationAmountText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        donationAmountText = input
                    }
                },
                label = { Text("Jumlah Donasi ($)", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00FF00),
                    unfocusedBorderColor = Color(0xFF232B36),
                    focusedLabelColor = Color(0xFF00FF00),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amt = donationAmountText.toLongOrNull() ?: 0L
                    if (amt <= 0L) {
                        resultMessage = "Silakan masukkan jumlah donasi yang valid!"
                        isSuccessMessage = false
                    } else {
                        val success = viewModel.donateToCharity(amt)
                        if (success) {
                            resultMessage = "Terima kasih atas kemurahan hati Anda! Donasi sebesar $${String.format("%,d", amt)} berhasil disalurkan."
                            isSuccessMessage = true
                            donationAmountText = ""
                        } else {
                            resultMessage = "Kas Pribadi Anda tidak mencukupi untuk donasi ini!"
                            isSuccessMessage = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Kirim Donasi", fontWeight = FontWeight.Bold)
            }

            if (resultMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = resultMessage!!,
                    color = if (isSuccessMessage) Color(0xFF00FF00) else Color(0xFFEF5350),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun CustomExpenseProposalHeaderCard(
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1822))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF132B3A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📑", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Proposal Acara & Pengeluaran Khusus",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Danai proposal acara, sponsorship, atau kebutuhan personal",
                        color = Color(0xFF80DEEA),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Kategori pengeluaran bebas diluar filantropi resmi. Anda dapat menambah item proposal acara, sponsorship mitra, kegiatan hobi, atau donasi personal dengan gambar kustom dari URL.",
                color = Color(0xFFB0BEC5),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E5FF),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("[ + ]", fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buat Proposal / Tambah Item Baru", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun LifestyleItemCard(
    item: com.example.data.LifestyleItem,
    viewModel: GameViewModel,
    onEditClick: () -> Unit
) {
    val fallbackEmoji = when (item.tabCategory) {
        "langganan" -> "📺"
        "gadget" -> "📱"
        "ekspedisi" -> "✈️"
        "wellness" -> "🩺"
        "filantropi" -> "🎗️"
        "pengeluaran_kustom" -> when {
            item.sectionName.contains("Golf", true) || item.name.contains("Golf", true) -> "⛳"
            item.sectionName.contains("Balap", true) || item.name.contains("Balap", true) -> "🏎️"
            item.sectionName.contains("Seni", true) || item.name.contains("Seni", true) || item.name.contains("Gala", true) -> "🎨"
            item.sectionName.contains("Kuliner", true) || item.name.contains("Kuliner", true) -> "🍲"
            item.sectionName.contains("Budaya", true) || item.name.contains("Budaya", true) -> "🏛️"
            item.sectionName.contains("Beasiswa", true) || item.name.contains("Beasiswa", true) -> "🎓"
            item.sectionName.contains("Proposal", true) -> "📑"
            else -> "🎟️"
        }
        else -> "⭐️"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 230.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151921)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Top row with Image/Icon & Edit Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(fallbackEmoji, fontSize = 22.sp)
                        if (item.imgUrl.isNotBlank()) {
                            AsyncImage(
                                model = item.imgUrl,
                                contentDescription = item.name,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("✏️", fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.desc,
                    color = Color(0xFF90A4AE),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 3
                )
            }

            Column {
                Spacer(modifier = Modifier.height(12.dp))

                // Price display
                val priceLabel = when {
                    item.isRecurring || item.tabCategory == "langganan" || item.tabCategory == "wellness" -> "$${String.format("%,d", item.price)}/bln"
                    else -> "$${String.format("%,d", item.price)}"
                }
                Text(
                    text = priceLabel,
                    color = if (item.isActive || item.isOwned) Color(0xFF90A4AE) else Color(0xFF00FF00),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                when (item.tabCategory) {
                    "langganan", "wellness" -> {
                        Button(
                            onClick = { viewModel.toggleLifestyleItemActive(item.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (item.isActive) Color(0xFFEF5350) else Color(0xFF00FF00),
                                contentColor = if (item.isActive) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Text(
                                text = if (item.isActive) "Nonaktifkan" else "Aktifkan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    "pengeluaran_kustom" -> {
                        if (item.isRecurring) {
                            Button(
                                onClick = { viewModel.toggleLifestyleItemActive(item.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (item.isActive) Color(0xFFEF5350) else Color(0xFF00E5FF),
                                    contentColor = if (item.isActive) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = if (item.isActive) "Nonaktifkan Rutin" else "Aktifkan Rutin",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            if (item.isOwned) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF0D281E)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val countBadge = if (item.fundedCount > 1) " (${item.fundedCount}x)" else ""
                                        Text(
                                            text = "✓ Didanai$countBadge",
                                            color = Color(0xFF00FF00),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    var reFundMsg by remember { mutableStateOf<String?>(null) }
                                    Button(
                                        onClick = {
                                            val success = viewModel.reFundLifestyleProposal(item.id)
                                            if (!success) {
                                                reFundMsg = "Kurang!"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF00E5FF),
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text(
                                            text = reFundMsg ?: "+ Danai",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                var buyError by remember { mutableStateOf<String?>(null) }
                                Button(
                                    onClick = {
                                        val success = viewModel.purchaseLifestyleItemOwned(item.id)
                                        if (!success) {
                                            buyError = "Kas Kurang!"
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00E5FF),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                ) {
                                    Text(
                                        text = buyError ?: "Danai Proposal",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                    "gadget", "filantropi" -> {
                        if (item.isOwned) {
                            Button(
                                onClick = {},
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = Color(0xFF1E2530),
                                    disabledContentColor = Color(0xFF455A64)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text("Dimiliki", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            var buyError by remember { mutableStateOf<String?>(null) }
                            Button(
                                onClick = {
                                    val success = viewModel.purchaseLifestyleItemOwned(item.id)
                                    if (!success) {
                                        buyError = "Saldo Kurang!"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00FF00),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                            ) {
                                Text(
                                    text = buyError ?: "Beli",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    "ekspedisi" -> {
                        var tripResult by remember { mutableStateOf<String?>(null) }
                        Button(
                            onClick = {
                                val success = viewModel.goOnLifestyleExpedition(item.id)
                                tripResult = if (success) "✈️ Berangkat!" else "Saldo Kurang!"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Text(
                                text = tripResult ?: "Pergi Liburan",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLifestyleItemDialog(
    item: com.example.data.LifestyleItem,
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var priceText by remember { mutableStateOf(item.price.toString()) }
    var sectionName by remember { mutableStateOf(item.sectionName) }
    var desc by remember { mutableStateOf(item.desc) }
    var imgUrl by remember { mutableStateOf(item.imgUrl) }
    var isRecurring by remember { mutableStateOf(item.isRecurring) }
    
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val presetImages = listOf(
        "⛳ Golf" to "https://images.unsplash.com/photo-1535131749006-b7f58c99034b?auto=format&fit=crop&w=500&q=80",
        "🎪 Acara" to "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=500&q=80",
        "🏎️ Balap" to "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&w=500&q=80",
        "🍲 Kuliner" to "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?auto=format&fit=crop&w=500&q=80",
        "🏛️ Hibah" to "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=500&q=80",
        "🎓 Beasiswa" to "https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=500&q=80"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (item.tabCategory == "pengeluaran_kustom") "Edit Proposal / Pengeluaran" else "Edit Item Gaya Hidup",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        containerColor = Color(0xFF1E293B),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (item.tabCategory == "pengeluaran_kustom") {
                    Text("Tipe Pengeluaran", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isRecurring) Color(0xFF00E5FF) else Color.Transparent)
                                .clickable { isRecurring = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sekali Bayar",
                                color = if (!isRecurring) Color.Black else Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = if (!isRecurring) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isRecurring) Color(0xFF00E5FF) else Color.Transparent)
                                .clickable { isRecurring = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Rutin Bulanan",
                                color = if (isRecurring) Color.Black else Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = if (isRecurring) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (item.tabCategory == "pengeluaran_kustom") "Nama Proposal" else "Nama Item", color = Color.Gray) },
                    placeholder = { Text(if (item.tabCategory == "pengeluaran_kustom") "Contoh: Donasi Proposal Acara A" else "Nama", color = Color.DarkGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input -> if (input.all { it.isDigit() }) priceText = input },
                    label = { Text("Biaya / Alokasi Dana ($)", color = Color.Gray) },
                    placeholder = { Text("Contoh: 25000", color = Color.DarkGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sectionName,
                    onValueChange = { sectionName = it },
                    label = { Text("Kategori / Section", color = Color.Gray) },
                    placeholder = { Text("Contoh: Proposal Acara & Sponsorship", color = Color.DarkGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Deskripsi", color = Color.Gray) },
                    placeholder = { Text("Keterangan proposal / rincian pengeluaran", color = Color.DarkGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = imgUrl,
                    onValueChange = { imgUrl = it },
                    label = { Text("URL Gambar / Ikon (Opsional)", color = Color.Gray) },
                    placeholder = { Text("https://contoh.com/gambar.jpg", color = Color.DarkGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Live image preview
                if (imgUrl.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = "Preview",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column {
                            Text("Preview Gambar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Gambar akan ditampilkan di kartu item", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }

                // Preset image suggestions
                Text("Preset Gambar Cepat:", color = Color.Gray, fontSize = 11.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetImages.forEach { (label, url) ->
                        SuggestionChip(
                            onClick = { imgUrl = url },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }

                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = priceText.toLongOrNull()
                    if (name.isBlank() || priceVal == null || sectionName.isBlank() || desc.isBlank()) {
                        errorMsg = "Semua field harus diisi dengan benar!"
                    } else {
                        viewModel.updateLifestyleItem(
                            id = item.id,
                            name = name,
                            price = priceVal,
                            sectionName = sectionName,
                            desc = desc,
                            imgUrl = imgUrl,
                            isRecurring = isRecurring
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00), contentColor = Color.Black)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isCustom || item.tabCategory == "pengeluaran_kustom") {
                    Button(
                        onClick = {
                            viewModel.deleteLifestyleItem(item.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Hapus", color = Color.White)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Batal", color = Color.LightGray)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLifestyleItemDialog(
    viewModel: GameViewModel,
    initialTabCategory: String,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var sectionName by remember { mutableStateOf(if (initialTabCategory == "pengeluaran_kustom") "Proposal Acara & Sponsorship" else "") }
    var desc by remember { mutableStateOf("") }
    var imgUrl by remember { mutableStateOf("") }
    var tabCategory by remember { mutableStateOf(initialTabCategory) }
    var isRecurring by remember { mutableStateOf(false) }
    
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    val categories = listOf(
        "langganan" to "Langganan",
        "gadget" to "Tech Gadget",
        "ekspedisi" to "Ekspedisi",
        "filantropi" to "Filantropi",
        "wellness" to "Wellness",
        "pengeluaran_kustom" to "Pengeluaran Kustom"
    )

    val presetImages = listOf(
        "⛳ Golf" to "https://images.unsplash.com/photo-1535131749006-b7f58c99034b?auto=format&fit=crop&w=500&q=80",
        "🎪 Acara" to "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?auto=format&fit=crop&w=500&q=80",
        "🏎️ Balap" to "https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?auto=format&fit=crop&w=500&q=80",
        "🍲 Kuliner" to "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?auto=format&fit=crop&w=500&q=80",
        "🏛️ Hibah" to "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=500&q=80",
        "🎓 Beasiswa" to "https://images.unsplash.com/photo-1523240795612-9a054b0db644?auto=format&fit=crop&w=500&q=80"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (tabCategory == "pengeluaran_kustom") "Tambah Proposal / Pengeluaran Kustom" else "Tambah Item Gaya Hidup Baru",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        containerColor = Color(0xFF1E293B),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Kategori Tab", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (catKey, catLabel) ->
                        val isSelected = tabCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                tabCategory = catKey
                                if (catKey == "pengeluaran_kustom" && sectionName.isBlank()) {
                                    sectionName = "Proposal Acara & Sponsorship"
                                }
                            },
                            label = { Text(catLabel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (catKey == "pengeluaran_kustom") Color(0xFF00E5FF) else Color(0xFF00FF00),
                                selectedLabelColor = Color.Black,
                                labelColor = Color.LightGray
                            )
                        )
                    }
                }

                if (tabCategory == "pengeluaran_kustom") {
                    Text("Tipe Pengeluaran", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!isRecurring) Color(0xFF00E5FF) else Color.Transparent)
                                .clickable { isRecurring = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sekali Bayar",
                                color = if (!isRecurring) Color.Black else Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = if (!isRecurring) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isRecurring) Color(0xFF00E5FF) else Color.Transparent)
                                .clickable { isRecurring = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Rutin Bulanan",
                                color = if (isRecurring) Color.Black else Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = if (isRecurring) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (tabCategory == "pengeluaran_kustom") "Nama Proposal" else "Nama Item", color = Color.Gray) },
                    placeholder = { Text(if (tabCategory == "pengeluaran_kustom") "Contoh: Donasi Proposal Acara A" else "Nama", color = Color.DarkGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { input -> if (input.all { it.isDigit() }) priceText = input },
                    label = { Text("Biaya / Alokasi Dana ($)", color = Color.Gray) },
                    placeholder = { Text("Contoh: 25000", color = Color.DarkGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = sectionName,
                    onValueChange = { sectionName = it },
                    label = { Text("Kategori / Section", color = Color.Gray) },
                    placeholder = { Text("Contoh: Proposal Acara & Sponsorship", color = Color.DarkGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Deskripsi", color = Color.Gray) },
                    placeholder = { Text("Keterangan proposal / rincian pengeluaran", color = Color.DarkGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = imgUrl,
                    onValueChange = { imgUrl = it },
                    label = { Text("URL Gambar / Ikon (Opsional)", color = Color.Gray) },
                    placeholder = { Text("https://contoh.com/gambar.jpg", color = Color.DarkGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00FF00),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF00FF00),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Live image preview
                if (imgUrl.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = "Preview",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column {
                            Text("Preview Gambar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Gambar akan ditampilkan di kartu item", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }

                // Preset image suggestions
                Text("Preset Gambar Cepat:", color = Color.Gray, fontSize = 11.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetImages.forEach { (label, url) ->
                        SuggestionChip(
                            onClick = { imgUrl = url },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }

                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceVal = priceText.toLongOrNull()
                    if (name.isBlank() || priceVal == null || sectionName.isBlank() || desc.isBlank()) {
                        errorMsg = "Semua field harus diisi dengan benar!"
                    } else {
                        viewModel.addLifestyleItem(
                            tabCategory = tabCategory,
                            sectionName = sectionName,
                            name = name,
                            price = priceVal,
                            imgUrl = imgUrl,
                            desc = desc,
                            isRecurring = isRecurring
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF00), contentColor = Color.Black)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.LightGray)
            }
        }
    )
}
