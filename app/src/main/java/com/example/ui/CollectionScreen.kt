package com.example.ui

import com.example.viewmodel.GameViewModel

import com.example.data.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.R

@Composable
fun CollectionScreen(navController: NavController, viewModel: GameViewModel) {
    val collectionCategories = com.example.data.collectionCategories
    val collectionItems by viewModel.collectionList.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val metalsList by viewModel.preciousMetalsList.collectAsState()
    
    val bgDark = Color(0xFF121212)
    val cardDark = Color(0xFF1E1E1E)
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val neonGreen = Color(0xFF00FF00)
    
    var selectedCategory by remember { mutableStateOf<com.example.data.CollectionCategory?>(null) }
    var itemToBuy by remember { mutableStateOf<com.example.data.CollectionItem?>(null) }
    var storeSearchQuery by remember { mutableStateOf("") }
    var storeSortOrder by remember { mutableStateOf(GarageSortOrder.NONE) }
    
    var showSellDialog by remember { mutableStateOf(false) }
    var instanceToSell by remember { mutableStateOf<String?>(null) }
    var itemToSell by remember { mutableStateOf<com.example.data.CollectionItem?>(null) }
    var sellValuation by remember { mutableStateOf(0L) }
    
    val currentMetalsValue = playerState.ownedMetals.entries.sumOf { (id, amount) ->
        val livePrice = metalsList.find { it.id == id }?.currentPrice ?: 0.0
        (amount * livePrice).toLong()
    } + playerState.timeDeposits.sumOf { it.principal }
    
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        containerColor = bgDark
    ) { innerPadding ->
        if (selectedCategory == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(stringResource(R.string.assets_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(stringResource(R.string.assets_subtitle), color = textGray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, gold.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = cardDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.assets_family_office_cash), color = textGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currencyFormat.format(playerState.privateBalance),
                                    color = gold,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Box(
                                    modifier = Modifier
                                        .background(gold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Kekayaan Likuid", color = gold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().clickable { navController.navigate("bank_savings") },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                            coil.compose.AsyncImage(
                                model = "https://plus.unsplash.com/premium_photo-1679870442588-2e26c81eab42?q=80&w=1332&auto=format&fit=crop",
                                contentDescription = null,
                                modifier = Modifier.matchParentSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                            
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color(0xFF121212).copy(alpha = 0.4f), Color(0xFF121212).copy(alpha = 0.95f))
                                        )
                                    )
                            )
                            
                            Row(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = gold, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Brankas & Tabungan", color = gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Aset Likuid (Deposito, Valas, Logam Mulia)", color = Color.LightGray, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(currencyFormat.format(currentMetalsValue), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                                }
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clickable { navController.navigate("garage") },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            coil.compose.AsyncImage(
                                model = "https://images.unsplash.com/photo-1619335680796-54f13b88c6ba?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                                contentDescription = "Garasi Utama",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.95f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Garasi Utama", color = gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val catIds = com.example.data.vehicleCategories.map { it.id }
                                    val totalVehicles = playerState.ownedCollections.count { owned -> 
                                        collectionItems.find { c -> c.id == owned.itemId }?.categoryId in catIds 
                                    }
                                    Text("Dimiliki: $totalVehicles", color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                val vehCats = com.example.data.vehicleCategories
                val chunkedVehCats = vehCats.chunked(3)
                chunkedVehCats.forEach { rowCats ->
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowCats.forEach { category ->
                                val ownedCount = playerState.ownedCollections.count { owned -> collectionItems.find { c -> c.id == owned.itemId }?.categoryId == category.id }
                                val bgImg = when (category.id) {
                                    "cars" -> "https://images.unsplash.com/photo-1692406069831-0bb7ea297645?q=80&w=1336&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
                                    "airplanes" -> "https://images.unsplash.com/photo-1651463833423-5f1a6e7159a1?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"
                                    "yachts" -> "https://images.unsplash.com/photo-1567899378494-47b22a2ae96a?auto=format&fit=crop&w=400&q=80"
                                    else -> "https://images.unsplash.com/photo-1773940792913-94baf5fa0130?q=80&w=1172&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D" // Motorcycles
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedCategory = category }
                                ) {
                                    coil.compose.AsyncImage(
                                        model = bgImg,
                                        contentDescription = category.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                                                    startY = 50f
                                                )
                                            )
                                    )
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(category.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 1)
                                        if (ownedCount > 0) {
                                            Text("$ownedCount Unit", color = neonGreen, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                            if (rowCats.size < 3) {
                                repeat(3 - rowCats.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clickable { navController.navigate("housing") },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            coil.compose.AsyncImage(
                                model = "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=600&q=80",
                                contentDescription = "Properti Hunian",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.95f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Properti Hunian", color = gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val totalHousing = playerState.ownedHouses.size + playerState.rentedHouses.size
                                    Text("Dimiliki/Disewa: $totalHousing", color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Koleksi Lainnya", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                val chunkedCollectionCats = collectionCategories.chunked(2)
                chunkedCollectionCats.forEach { rowCats ->
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowCats.forEach { category ->
                                val ownedCount = playerState.ownedCollections.count { owned -> collectionItems.find { c -> c.id == owned.itemId }?.categoryId == category.id }
                                
                                Card(
                                    modifier = Modifier.weight(1f).clickable { selectedCategory = category },
                                    colors = CardDefaults.cardColors(containerColor = cardDark)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(android.graphics.Color.parseColor(category.hexColor)).copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(android.graphics.Color.parseColor(category.hexColor)), modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(category.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                                            Text("Dimiliki: $ownedCount", color = if (ownedCount > 0) neonGreen else textGray, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                            if (rowCats.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else {
            // Category Detail Page
            val cat = selectedCategory!!
            var itemsInCateg = collectionItems.filter { it.categoryId == cat.id }
            
            if (storeSearchQuery.isNotBlank()) {
                itemsInCateg = itemsInCateg.filter { it.name.contains(storeSearchQuery, ignoreCase = true) }
            }
            
            itemsInCateg = when (storeSortOrder) {
                GarageSortOrder.HIGH_TO_LOW -> itemsInCateg.sortedByDescending { it.basePrice }
                GarageSortOrder.LOW_TO_HIGH -> itemsInCateg.sortedBy { it.basePrice }
                GarageSortOrder.NONE -> itemsInCateg
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { 
                            selectedCategory = null
                            storeSearchQuery = ""
                            storeSortOrder = GarageSortOrder.NONE
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = gold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cat.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        
                        var storeMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { storeMenuExpanded = true }) {
                                Icon(Icons.Default.Sort, contentDescription = "Sortir", tint = gold)
                            }
                            DropdownMenu(
                                expanded = storeMenuExpanded,
                                onDismissRequest = { storeMenuExpanded = false },
                                containerColor = cardDark
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Harga Tertinggi", color = Color.White) },
                                    onClick = { storeSortOrder = GarageSortOrder.HIGH_TO_LOW; storeMenuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Harga Terendah", color = Color.White) },
                                    onClick = { storeSortOrder = GarageSortOrder.LOW_TO_HIGH; storeMenuExpanded = false }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = storeSearchQuery,
                        onValueChange = { storeSearchQuery = it },
                        placeholder = { Text("Cari...", color = textGray) },
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
                }
                
                if (itemsInCateg.isEmpty()) {
                    item { Text("Belum ada item yang sesuai.", color = textGray) }
                }
                
                items(itemsInCateg) { item ->
                    val isOwned = playerState.ownedCollections.any { it.itemId == item.id }
                    val canAfford = playerState.privateBalance >= item.basePrice
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = cardDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (item.imageUrl.isNotBlank()) {
                                coil.compose.AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.name,
                                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            if (item.type.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    color = gold.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = item.type,
                                        color = gold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(item.description, color = textGray, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(currencyFormat.format(item.basePrice), color = gold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val ownedInstances = playerState.ownedCollections.filter { it.itemId == item.id }
                                    val ownedCount = ownedInstances.size
                                    if (ownedCount > 0) {
                                        Surface(color = neonGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(end = 8.dp)) {
                                            Text("Dimiliki: $ownedCount", color = neonGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        androidx.compose.material3.IconButton(
                                            onClick = {
                                                val instance = ownedInstances.first()
                                                instanceToSell = instance.instanceId
                                                itemToSell = item
                                                val valuation = calculateAssetValuation(item, instance.purchasedPrice)
                                                sellValuation = valuation
                                                showSellDialog = true
                                            },
                                            modifier = Modifier.padding(end = 4.dp)
                                        ) {
                                            Icon(Icons.Default.AttachMoney, contentDescription = "Jual", tint = Color.Yellow)
                                        }
                                    }
                                    Button(
                                        onClick = { itemToBuy = item },
                                        enabled = canAfford,
                                        colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = Color.Black)
                                    ) {
                                        Text(if (cat.id == "airplanes") "Beli Pribadi" else if (ownedCount > 0) "Beli Lagi" else "Beli")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Dialog Konfirmasi Jual
    if (showSellDialog && instanceToSell != null && itemToSell != null) {
        val i = itemToSell!!
        AlertDialog(
            onDismissRequest = { showSellDialog = false },
            containerColor = cardDark,
            title = { Text("Jual Koleksi", color = Color.White) },
            text = { 
                Column {
                    Text("Jual ${i.name} seharga ${currencyFormat.format(sellValuation)}?", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total Saldo Setelah Jual: ${currencyFormat.format(playerState.privateBalance + sellValuation)}", color = textGray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sellCollection(instanceToSell!!, sellValuation)
                        showSellDialog = false
                        instanceToSell = null
                        itemToSell = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                ) {
                    Text("Konfirmasi Jual")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSellDialog = false }) {
                    Text("Batal", color = Color.White)
                }
            }
        )
    }

    // Dialog Konfirmasi Beli
    if (itemToBuy != null) {
        val i = itemToBuy!!
        val isAirplane = selectedCategory?.id == "airplanes"
        AlertDialog(
            onDismissRequest = { itemToBuy = null },
            containerColor = cardDark,
            title = { Text(if (isAirplane) "Beli Pesawat Pribadi" else "Beli Koleksi", color = Color.White) },
            text = { 
                Column {
                    Text(if (isAirplane) "Beli ${i.name} seharga ${currencyFormat.format(i.basePrice)} untuk Hangar Pribadi?" else "Beli ${i.name} seharga ${currencyFormat.format(i.basePrice)}?", color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sisa Saldo: ${currencyFormat.format(playerState.privateBalance - i.basePrice)}", color = textGray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val err = viewModel.buyCollection(i.id)
                        if (err != null) {
                            android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                        } else {
                            android.widget.Toast.makeText(context, "Koleksi berhasil dibeli!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        itemToBuy = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = neonGreen, contentColor = Color.Black)
                ) {
                    Text(if (isAirplane) "Beli Pribadi" else "Konfirmasi Beli")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToBuy = null }) {
                    Text("Batal", color = textGray)
                }
            }
        )
    }
}
