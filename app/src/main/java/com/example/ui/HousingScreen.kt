package com.example.ui

import com.example.viewmodel.GameViewModel
import com.example.data.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousingScreen(navController: NavController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val housingList by viewModel.housingList.collectAsState()
    
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val neonGreen = Color(0xFF39FF14)
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    
    var tabIndex by remember { mutableIntStateOf(0) } // 0: Milik Sendiri, 1: Marketplace
    var searchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        containerColor = bgDark,
        topBar = {
            TopAppBar(
                title = { Text("Properti Hunian", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = bgDark,
                contentColor = gold,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                        color = gold
                    )
                }
            ) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = { Text("Milik Sendiri", fontWeight = FontWeight.Bold, color = if (tabIndex == 0) gold else textGray) }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = { Text("Marketplace", fontWeight = FontWeight.Bold, color = if (tabIndex == 1) gold else textGray) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (tabIndex == 0) {
                // Dimiliki & Disewa
                var isGridMode by remember { mutableStateOf(false) }
                var showSellDialog by remember { mutableStateOf(false) }
                var assetToSell by remember { mutableStateOf<com.example.ui.HousingListData?>(null) }
                var sellValuation by remember { mutableStateOf(0L) }
                
                if (showSellDialog && assetToSell != null) {
                    AlertDialog(
                        onDismissRequest = { showSellDialog = false },
                        containerColor = cardDark,
                        title = { Text("Jual Properti", color = Color.White) },
                        text = {
                            Text(
                                "Apakah Anda yakin ingin menjual properti ${assetToSell!!.item.name} seharga ${currencyFormat.format(sellValuation)}?",
                                color = textGray
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.sellHousing(assetToSell!!.instanceId, sellValuation)
                                    showSellDialog = false
                                    assetToSell = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                            ) {
                                Text("Jual")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSellDialog = false }) {
                                Text("Batal", color = Color.White)
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Koleksi Pribadi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = { isGridMode = !isGridMode }) {
                        Icon(if (isGridMode) Icons.Default.ViewList else Icons.Default.GridView, contentDescription = "Toggle Grid/List", tint = gold)
                    }
                }
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val ownedList = playerState.ownedHouses.mapNotNull { owned ->
                        val item = housingList.find { it.id == owned.housingId }
                        if (item != null) com.example.ui.HousingListData(owned.instanceId, item, owned.customImageUrl, true, owned.purchasedPrice) else null
                    }
                    val rentedList = playerState.rentedHouses.mapNotNull { rented ->
                        val item = housingList.find { it.id == rented.housingId }
                        if (item != null) com.example.ui.HousingListData(rented.instanceId, item, rented.customImageUrl, false, rented.monthlyRent) else null
                    }
                    
                    val combined = ownedList + rentedList
                    
                    if (combined.isEmpty()) {
                        item {
                            Surface(
                                color = cardDark.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().clickable { tabIndex = 1 }
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Home, contentDescription = null, tint = textGray.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("🏘️ Belum ada properti hunian.", color = textGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Beli dari Marketplace atau Tambahkan Aset Kustom via Pengaturan.", color = textGray.copy(alpha = 0.7f), fontSize = 14.sp)
                                }
                            }
                        }
                    } else {
                        if (isGridMode) {
                            val chunked = combined.chunked(2)
                            items(chunked) { rowItems ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    rowItems.forEach { data ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            HousingCardOwned(
                                                instanceId = data.instanceId,
                                                item = data.item,
                                                imageUrl = data.imageUrl,
                                                isOwnedFull = data.isOwnedFull,
                                                priceRecord = data.priceRecord,
                                                currencyFormat = currencyFormat,
                                                viewModel = viewModel,
                                                cardDark = cardDark,
                                                gold = gold,
                                                textGray = textGray,
                                                neonGreen = neonGreen,
                                                isGridMode = true,
                                                onSellClick = { valuation ->
                                                    assetToSell = data
                                                    sellValuation = valuation
                                                    showSellDialog = true
                                                }
                                            )
                                        }
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            items(combined) { data ->
                                HousingCardOwned(
                                    instanceId = data.instanceId,
                                    item = data.item,
                                    imageUrl = data.imageUrl,
                                    isOwnedFull = data.isOwnedFull,
                                    priceRecord = data.priceRecord,
                                    currencyFormat = currencyFormat,
                                    viewModel = viewModel,
                                    cardDark = cardDark,
                                    gold = gold,
                                    textGray = textGray,
                                    neonGreen = neonGreen,
                                    isGridMode = false,
                                    onSellClick = { valuation ->
                                        assetToSell = data
                                        sellValuation = valuation
                                        showSellDialog = true
                                    }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            } else {
                // Marketplace
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari rumah, apartemen...", color = textGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = gold,
                        unfocusedBorderColor = textGray,
                        cursorColor = gold
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                var filtered = housingList.filter { item ->
                    playerState.ownedHouses.none { it.housingId == item.id } &&
                    playerState.rentedHouses.none { it.housingId == item.id }
                }
                if (searchQuery.isNotBlank()) {
                    filtered = filtered.filter { it.name.contains(searchQuery, ignoreCase = true) || it.location.contains(searchQuery, ignoreCase = true) }
                }
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (filtered.isEmpty()) {
                        item {
                            Text("Tidak ada properti yang tersedia di pasar saat ini.", color = textGray, modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(filtered) { item ->
                            HousingCardMarket(
                                item = item,
                                currencyFormat = currencyFormat,
                                viewModel = viewModel,
                                playerCash = playerState.privateBalance,
                                cardDark = cardDark,
                                gold = gold,
                                textGray = textGray
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
fun HousingCardOwned(
    instanceId: String,
    item: HousingItem,
    imageUrl: String?,
    isOwnedFull: Boolean,
    priceRecord: Long,
    currencyFormat: NumberFormat,
    viewModel: GameViewModel,
    cardDark: Color,
    gold: Color,
    textGray: Color,
    neonGreen: Color,
    isGridMode: Boolean = false,
    onSellClick: (Long) -> Unit = {}
) {
    var editingItem by remember { mutableStateOf(false) }
    var tempUrl by remember { mutableStateOf(imageUrl ?: "") }
    
    // Gain/Loss Logic for Real Estate
    val fakeAge = 3 + Math.abs(instanceId.hashCode() % 8)
    val fakeYearlyGain = 0.04 + ((item.id.hashCode() % 5) * 0.01) // 4% to 8%
    val totalGainPercent = fakeAge * fakeYearlyGain
    val valuation = if (isOwnedFull) (priceRecord + (priceRecord * totalGainPercent)).toLong() else 0L
    val diff = valuation - priceRecord
    val diffStr = "+%.1f%%".format(Locale.US, totalGainPercent * 100) + if (!isGridMode) " (${currencyFormat.format(diff)})" else ""
    
    if (editingItem) {
        AlertDialog(
            onDismissRequest = { editingItem = false },
            containerColor = cardDark,
            title = { Text("Ubah Gambar Properti", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = tempUrl,
                    onValueChange = { tempUrl = it },
                    label = { Text("URL Gambar", color = textGray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = gold,
                        unfocusedBorderColor = textGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateHousingImage(instanceId, tempUrl, isRented = !isOwnedFull)
                        editingItem = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = false }) {
                    Text("Batal", color = Color.White)
                }
            }
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            Column {
                val imgHeight = if (isGridMode) 100.dp else 180.dp
                if (!imageUrl.isNullOrBlank()) {
                    coil.compose.AsyncImage(
                        model = imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imgHeight)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imgHeight)
                            .background(Color.DarkGray)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(if (isGridMode) 32.dp else 64.dp))
                    }
                }
                
                Column(modifier = Modifier.padding(if (isGridMode) 12.dp else 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name, 
                            color = Color.White, 
                            fontWeight = FontWeight.Bold, 
                            fontSize = if (isGridMode) 14.sp else 18.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.End) {
                            IconButton(
                                onClick = {
                                    tempUrl = imageUrl ?: ""
                                    editingItem = true
                                },
                                modifier = Modifier.size(if (isGridMode) 24.dp else 32.dp).padding(start = 4.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Foto", tint = textGray)
                            }
                            if (isOwnedFull) {
                                IconButton(
                                    onClick = {
                                        onSellClick(valuation)
                                    },
                                    modifier = Modifier.size(if (isGridMode) 24.dp else 32.dp).padding(start = 4.dp)
                                ) {
                                    Icon(Icons.Rounded.AttachMoney, contentDescription = "Jual Properti", tint = Color.Yellow)
                                }
                            }
                        }
                    }
                    
                    Text(item.location, color = textGray, fontSize = if (isGridMode) 10.sp else 14.sp, maxLines = 1)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (isOwnedFull) {
                        if (isGridMode) {
                            Text("Beli: ${currencyFormat.format(priceRecord)}", color = textGray, fontSize = 11.sp)
                            Text("Now: ${currencyFormat.format(valuation)}", color = Color.White, fontSize = 11.sp)
                            Text(diffStr, color = neonGreen, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        } else {
                            Text("Harga Beli: ${currencyFormat.format(priceRecord)}", color = textGray, fontSize = 14.sp)
                            Text("Nilai Sekarang (Valuation): ${currencyFormat.format(valuation)}", color = Color.White, fontSize = 14.sp)
                            Text("Gain/Loss: $diffStr", color = neonGreen, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Text("Biaya Sewa: ${currencyFormat.format(priceRecord)}/bln", color = textGray, fontSize = if (isGridMode) 11.sp else 14.sp)
                    }
                }
            }
            
            val badgeColor = if (isOwnedFull) neonGreen else gold
            val badgeText = if (isOwnedFull) "MILIK PRIBADI" else "SEWA"
            Surface(
                color = badgeColor.copy(alpha = 0.9f),
                shape = RoundedCornerShape(bottomStart = 8.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(badgeText, color = Color.Black, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HousingCardMarket(
    item: HousingItem,
    currencyFormat: NumberFormat,
    viewModel: GameViewModel,
    playerCash: Long,
    cardDark: Color,
    gold: Color,
    textGray: Color
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Card(
        colors = CardDefaults.cardColors(containerColor = cardDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (!item.imageUrl.isNullOrBlank()) {
                coil.compose.AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.DarkGray)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.location, color = textGray, fontSize = 14.sp)
                    Text(item.type, color = gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Kiri: Opsi Beli
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Beli (Tunai)", color = textGray, fontSize = 11.sp)
                            Text(currencyFormat.format(item.buyPrice), color = gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { 
                                    val err = viewModel.buyResidentialProperty(item.id)
                                    if (err != null) {
                                        android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Properti berhasil dibeli!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = playerCash >= item.buyPrice,
                                colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("Beli", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    // Kanan: Opsi Sewa
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Sewa Bulanan", color = textGray, fontSize = 11.sp)
                            Text(currencyFormat.format(item.rentPrice), color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { 
                                    val err = viewModel.rentHousing(item.id)
                                    if (err != null) {
                                        android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                    } else {
                                        android.widget.Toast.makeText(context, "Mulai menyewa properti!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = playerCash >= item.rentPrice,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = gold),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("Sewa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class HousingListData(
    val instanceId: String,
    val item: com.example.data.HousingItem,
    val imageUrl: String?,
    val isOwnedFull: Boolean,
    val priceRecord: Long
)
