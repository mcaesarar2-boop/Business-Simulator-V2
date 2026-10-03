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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.*
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

enum class GarageSortOrder {
    NONE, HIGH_TO_LOW, LOW_TO_HIGH
}

fun calculateAssetValuation(item: com.example.data.CollectionItem, purchasePrice: Long): Long {
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    var percentage = 0.0

    val releaseYear = item.releaseYear
    if (releaseYear != null) {
        val age = currentYear - releaseYear
        
        if (releaseYear <= 1995) {
            // LOGIKA ASET KLASIK (Apresiasi / Naik Harga)
            percentage = (age * 0.03) 
        } else {
            // LOGIKA ASET MODERN (Depresiasi / Turun Harga)
            percentage = -(age * 0.05).coerceAtLeast(-0.80)
        }
    } else {
        // LOGIKA DEFAULT (Jika aset lama tidak punya data tahun)
        if (listOf("cars", "motorcycles", "yachts", "airplanes").contains(item.categoryId)) {
            percentage = -0.15
        } else {
            percentage = 0.20
        }
    }
    
    return (purchasePrice + (purchasePrice * percentage)).toLong()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageScreen(navController: NavController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val collectionItems by viewModel.collectionList.collectAsState()
    
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    
    var sortOrder by remember { mutableStateOf(GarageSortOrder.NONE) }
    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf("CARD") }
    
    // Filter owned vehicles
    var ownedVehicles = playerState.ownedCollections.filter { owned ->
        val cat = collectionItems.find { c -> c.id == owned.itemId }?.categoryId
        listOf("cars", "motorcycles", "yachts", "airplanes").contains(cat)
    }.mapNotNull { owned ->
        val item = collectionItems.find { c -> c.id == owned.itemId }
        if (item != null) Pair(owned, item) else null
    }

    if (searchQuery.isNotBlank()) {
        ownedVehicles = ownedVehicles.filter { it.second.name.contains(searchQuery, ignoreCase = true) }
    }

    ownedVehicles = when (sortOrder) {
        GarageSortOrder.HIGH_TO_LOW -> ownedVehicles.sortedByDescending { it.first.purchasedPrice }
        GarageSortOrder.LOW_TO_HIGH -> ownedVehicles.sortedBy { it.first.purchasedPrice }
        GarageSortOrder.NONE -> ownedVehicles
    }
    
    val totalAssetValue = ownedVehicles.sumOf { (owned, item) -> calculateAssetValuation(item, owned.purchasedPrice) }
    
    val cars = ownedVehicles.filter { it.second.categoryId == "cars" }
    val motorcycles = ownedVehicles.filter { it.second.categoryId == "motorcycles" }
    val yachts = ownedVehicles.filter { it.second.categoryId == "yachts" }
    val airplanes = ownedVehicles.filter { it.second.categoryId == "airplanes" }
    
    Scaffold(
        containerColor = bgDark,
        topBar = {
            TopAppBar(
                title = { Text("Garasi Pribadi", color = gold, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = gold)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewMode = when(viewMode) {
                            "CARD" -> "GRID"
                            "GRID" -> "COMPACT"
                            else -> "CARD"
                        }
                    }) {
                        val viewIcon = when(viewMode) {
                            "CARD" -> Icons.Default.ViewDay
                            "GRID" -> Icons.Default.GridView
                            else -> Icons.Default.ViewList
                        }
                        Icon(viewIcon, contentDescription = "View Mode", tint = gold)
                    }
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.Sort, contentDescription = "Sortir", tint = gold)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = cardDark
                    ) {
                        DropdownMenuItem(
                            text = { Text("Harga Tertinggi", color = Color.White) },
                            onClick = { sortOrder = GarageSortOrder.HIGH_TO_LOW; menuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Harga Terendah", color = Color.White) },
                            onClick = { sortOrder = GarageSortOrder.LOW_TO_HIGH; menuExpanded = false }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgDark)
            )
        }
    ) { innerPadding ->
        var showSellDialog by remember { mutableStateOf(false) }
        var assetToSell by remember { mutableStateOf<Pair<com.example.data.OwnedCollection, com.example.data.CollectionItem>?>(null) }
        var sellValuation by remember { mutableStateOf(0L) }
        
        if (showSellDialog && assetToSell != null) {
            AlertDialog(
                onDismissRequest = { showSellDialog = false },
                containerColor = cardDark,
                title = { Text("Jual Kendaraan", color = Color.White) },
                text = {
                    Text(
                        "Apakah Anda yakin ingin menjual ${assetToSell!!.second.name} seharga ${currencyFormat.format(sellValuation)}?",
                        color = textGray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.sellCollection(assetToSell!!.first.instanceId, sellValuation)
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari aset...", color = textGray) },
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
            }
            
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Asset Value", color = textGray, fontSize = 16.sp)
                    Text(currencyFormat.format(totalAssetValue), color = Color(0xFF10B981), fontSize = 36.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            // CARS
            item {
                GarageSection(
                    title = "Garasi Mobil",
                    icon = Icons.Default.DirectionsCar,
                    items = cars,
                    cardDark = cardDark,
                    textGray = textGray,
                    gold = gold,
                    currencyFormat = currencyFormat,
                    viewModel = viewModel,
                    navController = navController,
                    viewMode = viewMode,
                    onSellCardClick = { pair, valuation ->
                        assetToSell = pair
                        sellValuation = valuation
                        showSellDialog = true
                    }
                )
            }
            
            // MOTORCYCLES
            item {
                GarageSection(
                    title = "Garasi Motor",
                    icon = Icons.Default.TwoWheeler,
                    items = motorcycles,
                    cardDark = cardDark,
                    textGray = textGray,
                    gold = gold,
                    currencyFormat = currencyFormat,
                    viewModel = viewModel,
                    navController = navController,
                    viewMode = viewMode,
                    onSellCardClick = { pair, valuation ->
                        assetToSell = pair
                        sellValuation = valuation
                        showSellDialog = true
                    }
                )
            }
            
            // YACHTS
            item {
                GarageSection(
                    title = "Pelabuhan Kapal Pesiar",
                    icon = Icons.Default.DirectionsBoat,
                    items = yachts,
                    cardDark = cardDark,
                    textGray = textGray,
                    gold = gold,
                    currencyFormat = currencyFormat,
                    viewModel = viewModel,
                    navController = navController,
                    viewMode = viewMode,
                    onSellCardClick = { pair, valuation ->
                        assetToSell = pair
                        sellValuation = valuation
                        showSellDialog = true
                    }
                )
            }
            
            // AIRPLANES
            item {
                GarageSection(
                    title = "Hangar Pesawat",
                    icon = Icons.Default.Flight,
                    items = airplanes,
                    cardDark = cardDark,
                    textGray = textGray,
                    gold = gold,
                    currencyFormat = currencyFormat,
                    viewModel = viewModel,
                    navController = navController,
                    viewMode = viewMode,
                    onSellCardClick = { pair, valuation ->
                        assetToSell = pair
                        sellValuation = valuation
                        showSellDialog = true
                    }
                )
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun GarageSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    items: List<Pair<com.example.data.OwnedCollection, com.example.data.CollectionItem>>,
    cardDark: Color,
    textGray: Color,
    gold: Color,
    currencyFormat: NumberFormat,
    viewModel: GameViewModel,
    navController: NavController,
    viewMode: String,
    onSellCardClick: (Pair<com.example.data.OwnedCollection, com.example.data.CollectionItem>, Long) -> Unit
) {
    var editingItem by remember { mutableStateOf<String?>(null) }
    var tempUrl by remember { mutableStateOf("") }
    val neonGreen = Color(0xFF39FF14)

    if (editingItem != null) {
        val ownedIns = editingItem!!
        AlertDialog(
            onDismissRequest = { editingItem = null },
            containerColor = cardDark,
            title = { Text("Set Foto Kustom / Detail", color = Color.White) },
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
                        viewModel.updateCollectionImage(ownedIns, tempUrl)
                        editingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) {
                    Text("Batal", color = Color.White)
                }
            }
        )
    }

    val onCardClick: (String, String) -> Unit = { id, customUrl ->
        tempUrl = customUrl
        editingItem = id
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = gold, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        if (items.isEmpty()) {
            Surface(
                color = cardDark.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable { navController.popBackStack() }
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(icon, contentDescription = null, tint = textGray.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Belum ada koleksi di sini", color = textGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("➕ Tambahkan aset pertama Anda", color = textGray.copy(alpha = 0.7f), fontSize = 14.sp)
                }
            }
        } else {
            if (viewMode == "CARD") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items.forEach { (owned, item) ->
                        AssetCardFull(owned, item, cardDark, textGray, gold, currencyFormat, icon, onCardClick, onSellCardClick)
                    }
                }
            } else if (viewMode == "GRID") {
                val chunked = items.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    chunked.forEach { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            rowItems.forEach { (owned, item) ->
                                Box(modifier = Modifier.weight(1f)) {
                                    AssetCardGrid(owned, item, cardDark, textGray, gold, currencyFormat, icon, onCardClick, onSellCardClick)
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else { // COMPACT
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items.forEach { (owned, item) ->
                        AssetCardCompact(owned, item, cardDark, textGray, gold, currencyFormat, icon, onCardClick, onSellCardClick)
                    }
                }
            }
        }
    }
}

@Composable
fun AssetCardFull(
    owned: com.example.data.OwnedCollection,
    item: com.example.data.CollectionItem,
    cardDark: Color,
    textGray: Color,
    gold: Color,
    currencyFormat: NumberFormat,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCardClick: (String, String) -> Unit,
    onSellCardClick: (Pair<com.example.data.OwnedCollection, com.example.data.CollectionItem>, Long) -> Unit
) {
    val valuation = calculateAssetValuation(item, owned.purchasedPrice)
    val diff = valuation - owned.purchasedPrice
    val diffPercent = if (owned.purchasedPrice > 0) (diff.toDouble() / owned.purchasedPrice.toDouble()) * 100 else 0.0
    val diffStr = "${if (diff >= 0) "+" else ""}${String.format(Locale.US, "%.1f", diffPercent)}% (${currencyFormat.format(diff)})"
    val diffColor = if (diff >= 0) Color(0xFF10B981) else Color(0xFFF43F5E)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onCardClick(owned.instanceId, owned.customImageUrl ?: "") }
    ) {
        Column {
            val activeUrl = if (!owned.customImageUrl.isNullOrBlank()) owned.customImageUrl else item.imageUrl
            if (activeUrl.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = activeUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp).background(Color.DarkGray).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1)
                        if (item.type.isNotBlank()) {
                            Text(item.type, color = gold, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.End) {
                        androidx.compose.material3.IconButton(
                            onClick = { onCardClick(owned.instanceId, owned.customImageUrl ?: "") },
                            modifier = Modifier.size(32.dp).padding(start = 4.dp)
                        ) {
                            Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = "Edit Foto", tint = textGray)
                        }
                        androidx.compose.material3.IconButton(
                            onClick = { onSellCardClick(owned to item, valuation) },
                            modifier = Modifier.size(32.dp).padding(start = 4.dp)
                        ) {
                            Icon(Icons.Default.AttachMoney, contentDescription = "Jual", tint = Color.Yellow)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Harga Beli: ${currencyFormat.format(owned.purchasedPrice)}", color = textGray, fontSize = 14.sp)
                Text("Nilai Sekarang (Valuation): ${currencyFormat.format(valuation)}", color = Color.White, fontSize = 14.sp)
                Text("Gain/Loss: $diffStr", color = diffColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun AssetCardGrid(
    owned: com.example.data.OwnedCollection,
    item: com.example.data.CollectionItem,
    cardDark: Color,
    textGray: Color,
    gold: Color,
    currencyFormat: NumberFormat,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCardClick: (String, String) -> Unit,
    onSellCardClick: (Pair<com.example.data.OwnedCollection, com.example.data.CollectionItem>, Long) -> Unit
) {
    val valuation = calculateAssetValuation(item, owned.purchasedPrice)
    val diff = valuation - owned.purchasedPrice
    val diffPercent = if (owned.purchasedPrice > 0) (diff.toDouble() / owned.purchasedPrice.toDouble()) * 100 else 0.0
    val diffStr = "${if (diff >= 0) "+" else ""}${String.format(Locale.US, "%.1f", diffPercent)}%"
    val diffColor = if (diff >= 0) Color(0xFF10B981) else Color(0xFFF43F5E)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardDark),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onCardClick(owned.instanceId, owned.customImageUrl ?: "") }
    ) {
        Column {
            val activeUrl = if (!owned.customImageUrl.isNullOrBlank()) owned.customImageUrl else item.imageUrl
            if (activeUrl.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = activeUrl,
                    contentDescription = item.name,
                    modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.DarkGray).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                        if (item.type.isNotBlank()) {
                            Text(item.type, color = gold, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.End) {
                        androidx.compose.material3.IconButton(
                            onClick = { onCardClick(owned.instanceId, owned.customImageUrl ?: "") },
                            modifier = Modifier.size(24.dp).padding(start = 2.dp)
                        ) {
                            Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = "Edit Foto", tint = textGray)
                        }
                        androidx.compose.material3.IconButton(
                            onClick = { onSellCardClick(owned to item, valuation) },
                            modifier = Modifier.size(24.dp).padding(start = 2.dp)
                        ) {
                            Icon(Icons.Default.AttachMoney, contentDescription = "Jual", tint = Color.Yellow)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Beli: ${currencyFormat.format(owned.purchasedPrice)}", color = textGray, fontSize = 11.sp)
                Text("Now: ${currencyFormat.format(valuation)}", color = Color.White, fontSize = 11.sp)
                Text(diffStr, color = diffColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AssetCardCompact(
    owned: com.example.data.OwnedCollection,
    item: com.example.data.CollectionItem,
    cardDark: Color,
    textGray: Color,
    gold: Color,
    currencyFormat: NumberFormat,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCardClick: (String, String) -> Unit,
    onSellCardClick: (Pair<com.example.data.OwnedCollection, com.example.data.CollectionItem>, Long) -> Unit
) {
    val valuation = calculateAssetValuation(item, owned.purchasedPrice)
    val diff = valuation - owned.purchasedPrice
    val diffPercent = if (owned.purchasedPrice > 0) (diff.toDouble() / owned.purchasedPrice.toDouble()) * 100 else 0.0
    val diffStr = "${if (diff >= 0) "+" else ""}${String.format(Locale.US, "%.1f", diffPercent)}%"
    val diffColor = if (diff >= 0) Color(0xFF10B981) else Color(0xFFF43F5E)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardDark),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().clickable { onCardClick(owned.instanceId, owned.customImageUrl ?: "") }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            val activeUrl = if (!owned.customImageUrl.isNullOrBlank()) owned.customImageUrl else item.imageUrl
            if (activeUrl.isNotBlank()) {
                coil.compose.AsyncImage(
                    model = activeUrl,
                    contentDescription = item.name,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(64.dp).background(Color.DarkGray).clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                        if (item.type.isNotBlank()) {
                            Text(item.type, color = gold, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.End) {
                        androidx.compose.material3.IconButton(
                            onClick = { onCardClick(owned.instanceId, owned.customImageUrl ?: "") },
                            modifier = Modifier.size(24.dp).padding(start = 2.dp)
                        ) {
                            Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = "Edit Foto", tint = textGray)
                        }
                        androidx.compose.material3.IconButton(
                            onClick = { onSellCardClick(owned to item, valuation) },
                            modifier = Modifier.size(24.dp).padding(start = 2.dp)
                        ) {
                            Icon(Icons.Default.AttachMoney, contentDescription = "Jual", tint = Color.Yellow)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text("Harga Beli: ${currencyFormat.format(owned.purchasedPrice)}", color = textGray, fontSize = 12.sp)
                Text("Valuation: ${currencyFormat.format(valuation)}", color = Color.White, fontSize = 12.sp)
            }
            Text(diffStr, color = diffColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
