package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsEventScreen(
    instanceId: String,
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsState()
    val ownedData = playerState.ownedBusinesses.find { it.instanceId == instanceId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }

    if (ownedData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bisnis tidak ditemukan.", color = Color.White)
        }
        return
    }

    val hq = ownedData.eoCompanyHqLevel ?: "HOUSE"
    val standardAssets = ownedData.eoOwnedAssets ?: emptyMap()
    val customAssets = ownedData.eoCustomAssets ?: emptyList()

    // Count storage usage
    val standardCount = standardAssets.values.sum()
    val customCount = customAssets.sumOf { it.quantity }
    val totalCount = standardCount + customCount

    val capacity = when (hq) {
        "HOUSE" -> 5
        "OFFICE" -> 15
        "REGIONAL" -> 40
        "NATIONAL" -> 100
        else -> 9999
    }

    var isGridView by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(value = false) }
    var selectedCustomAssetIdForEdit by remember { mutableStateOf("") }
    var editNameInput by remember { mutableStateOf("") }
    var editUrlInput by remember { mutableStateOf("") }

    // Dialog state for buying new asset
    var inputName by remember { mutableStateOf("") }
    var inputType by remember { mutableStateOf("Stage") }
    var inputQty by remember { mutableStateOf("1") }
    var inputPrice by remember { mutableStateOf("1000") }
    var inputUrl by remember { mutableStateOf("") }

    val assetTypes = listOf(
        "Stage", "Sound System", "Lighting", "Multimedia", "LED Wall", 
        "Power Generator", "Security", "Mobile Toilet", "Barricade", 
        "Ambulance", "Tent & Truss", "Heavy Equipment", "Logistics Truck", 
        "Warehouse Storage", "VIP Heli", "Other"
    )

    var typeDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📦 Gudang & Aset Permanen", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                actions = {
                    // Toggle Grid / List
                    IconButton(onClick = { isGridView = !isGridView }) {
                        Icon(
                            imageVector = if (isGridView) Icons.Filled.List else Icons.Filled.GridView,
                            contentDescription = "Toggle View",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    inputName = ""
                    inputType = "Stage"
                    inputQty = "1"
                    inputPrice = ""
                    inputUrl = ""
                    showAddDialog = true
                },
                containerColor = Color(0xFF3B82F6),
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah Aset Baru", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Warehouse capacity card
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Penyimpanan Gudang (${viewModel.getHqDisplayName(hq)})", fontSize = 12.sp, color = Color.Gray)
                            Text("Aset Milik Sendiri", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(
                            "$totalCount / $capacity Unit",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalCount >= capacity) Color(0xFFF43F5E) else Color(0xFF10B981)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = (totalCount.toFloat() / capacity.toFloat()).coerceIn(0f, 1f),
                        color = if (totalCount >= capacity) Color(0xFFF43F5E) else Color(0xFF10B981),
                        trackColor = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Kapasitas aset dipengaruhi oleh tingkat HQ Anda. Upgrade HQ untuk memperluas gudang penyimpanan.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (standardAssets.isEmpty() && customAssets.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🛠️ Belum ada aset yang dibeli.", fontSize = 15.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tekan tombol (+) di bawah untuk membeli aset kustom secara manual!", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            } else {
                Text(
                    "Daftar Aset Inventory",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (isGridView) {
                    // Grid View implementation
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        // Standard assets
                        val ownedStandard = standardAssets.filter { it.value > 0 }
                        items(ownedStandard.keys.toList()) { asset ->
                            val qty = ownedStandard[asset] ?: 0
                            AssetGridItem(
                                name = asset,
                                type = asset,
                                qty = qty,
                                price = viewModel.getAssetPurchasePrice(asset),
                                imageUrl = null,
                                isCustom = false,
                                onEdit = {},
                                onSell = {
                                    // Sell standard asset (simulated as buying/reducing)
                                    // standard assets can be sold or just custom assets. Let's make it simple.
                                }
                            )
                        }

                        // Custom assets
                        items(customAssets) { asset ->
                            AssetGridItem(
                                name = asset.name,
                                type = asset.type,
                                qty = asset.quantity,
                                price = asset.priceUnit,
                                imageUrl = asset.imageUrl,
                                isCustom = true,
                                onEdit = {
                                    selectedCustomAssetIdForEdit = asset.id
                                    editNameInput = asset.name
                                    editUrlInput = asset.imageUrl ?: ""
                                    showEditDialog = true
                                },
                                onSell = {
                                    val err = viewModel.sellEoCustomAsset(instanceId, asset.id)
                                    if (err != null) {
                                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Aset ${asset.name} berhasil dijual!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                } else {
                    // List View implementation
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        // Standard assets
                        val ownedStandard = standardAssets.filter { it.value > 0 }
                        items(ownedStandard.keys.toList()) { asset ->
                            val qty = ownedStandard[asset] ?: 0
                            AssetListItem(
                                name = asset,
                                type = asset,
                                qty = qty,
                                price = viewModel.getAssetPurchasePrice(asset),
                                imageUrl = null,
                                isCustom = false,
                                onEdit = {},
                                onSell = {}
                            )
                        }

                        // Custom assets
                        items(customAssets) { asset ->
                            AssetListItem(
                                name = asset.name,
                                type = asset.type,
                                qty = asset.quantity,
                                price = asset.priceUnit,
                                imageUrl = asset.imageUrl,
                                isCustom = true,
                                onEdit = {
                                    selectedCustomAssetIdForEdit = asset.id
                                    editNameInput = asset.name
                                    editUrlInput = asset.imageUrl ?: ""
                                    showEditDialog = true
                                },
                                onSell = {
                                    val err = viewModel.sellEoCustomAsset(instanceId, asset.id)
                                    if (err != null) {
                                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Aset ${asset.name} berhasil dijual!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Purchase Dialog (+)
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("Beli / Tambah Aset Kustom", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("Nama Aset", color = Color.Gray) },
                            textStyle = LocalTextStyle.current.copy(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Column {
                            Text("Jenis Aset", fontSize = 12.sp, color = Color.Gray)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { typeDropdownExpanded = true },
                                    border = BorderStroke(1.dp, Color.DarkGray),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(inputType, color = Color.White)
                                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
                                    }
                                }
                                DropdownMenu(
                                    expanded = typeDropdownExpanded,
                                    onDismissRequest = { typeDropdownExpanded = false },
                                    modifier = Modifier.background(Color(0xFF1E293B)).fillMaxWidth(0.8f)
                                ) {
                                    assetTypes.forEach { type ->
                                        DropdownMenuItem(
                                            text = { Text(type, color = Color.White) },
                                            onClick = {
                                                inputType = type
                                                typeDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = inputQty,
                                onValueChange = { inputQty = it.filter { char -> char.isDigit() } },
                                label = { Text("Jumlah (Unit)", color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = LocalTextStyle.current.copy(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color.DarkGray
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = inputPrice,
                                onValueChange = { inputPrice = it.filter { char -> char.isDigit() || char == '.' } },
                                label = { Text("Harga / Unit ($)", color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = LocalTextStyle.current.copy(color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color.DarkGray
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            label = { Text("URL Gambar Kustom (Opsional)", color = Color.Gray) },
                            placeholder = { Text("https://example.com/image.jpg", color = Color.DarkGray) },
                            textStyle = LocalTextStyle.current.copy(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        val parsedPrice = inputPrice.toDoubleOrNull() ?: 0.0
                        val parsedQty = inputQty.toIntOrNull() ?: 1
                        val totalEstimated = parsedPrice * parsedQty
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Pembayaran:", fontSize = 13.sp, color = Color.Gray)
                            Text("$${String.format("%,.0f", totalEstimated)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = inputName.trim()
                        val qty = inputQty.toIntOrNull() ?: 1
                        val price = inputPrice.toDoubleOrNull() ?: 0.0

                        if (name.isEmpty()) {
                            Toast.makeText(context, "Nama aset tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (qty <= 0) {
                            Toast.makeText(context, "Jumlah harus minimal 1 unit!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (price < 0.0) {
                            Toast.makeText(context, "Harga tidak boleh negatif!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val err = viewModel.buyEoCustomAsset(
                            instanceId = instanceId,
                            name = name,
                            type = inputType,
                            quantity = qty,
                            priceUnit = price,
                            imageUrl = inputUrl
                        )

                        if (err != null) {
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Berhasil membeli $qty unit aset $name!", Toast.LENGTH_SHORT).show()
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("Beli & Simpan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    // Edit Custom Asset Dialog (Pencil)
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text("Edit Detail Aset", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        label = { Text("Nama Baru Aset", color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editUrlInput,
                        onValueChange = { editUrlInput = it },
                        label = { Text("URL Gambar Baru", color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newName = editNameInput.trim()
                        if (newName.isEmpty()) {
                            Toast.makeText(context, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val err = viewModel.editEoCustomAsset(
                            instanceId = instanceId,
                            assetId = selectedCustomAssetIdForEdit,
                            newName = newName,
                            newImageUrl = editUrlInput
                        )

                        if (err != null) {
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Aset berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Perbarui", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun AssetGridItem(
    name: String,
    type: String,
    qty: Int,
    price: Double,
    imageUrl: String?,
    isCustom: Boolean,
    onEdit: () -> Unit,
    onSell: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Column {
            // Asset Image or Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(Color(0xFF0F172A))
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(getAssetTypeEmoji(type), fontSize = 36.sp)
                    }
                }

                // Custom Badge
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .background(
                            if (isCustom) Color(0xFF10B981) else Color(0xFF3B82F6),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        if (isCustom) "Kustom" else "Sewa/Standard",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    type,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Qty: $qty", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24))
                    Text("$${String.format("%,.0f", price)}", fontSize = 11.sp, color = Color.LightGray)
                }

                if (isCustom) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.DarkGray.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                        }

                        Button(
                            onClick = onSell,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E).copy(alpha = 0.15f)),
                            contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                        ) {
                            Text("Jual (50%)", fontSize = 9.sp, color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssetListItem(
    name: String,
    type: String,
    qty: Int,
    price: Double,
    imageUrl: String?,
    isCustom: Boolean,
    onEdit: () -> Unit,
    onSell: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small Image or Placeholder
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(getAssetTypeEmoji(type), fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (isCustom) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF3B82F6).copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            if (isCustom) "Kustom" else "Standard",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCustom) Color(0xFF10B981) else Color(0xFF3B82F6)
                        )
                    }
                }
                Text("Kategori: $type", fontSize = 11.sp, color = Color.Gray)
                Text(
                    "Milik Sendiri • Satuan: $${String.format("%,.0f", price)}",
                    fontSize = 11.sp,
                    color = Color.LightGray
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Jumlah: $qty",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBF24),
                    fontSize = 13.sp
                )

                if (isCustom) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(14.dp))
                        }

                        Button(
                            onClick = onSell,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E).copy(alpha = 0.2f)),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Jual", fontSize = 10.sp, color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

fun getAssetTypeEmoji(type: String): String {
    return when (type) {
        "Stage" -> "🎪"
        "Sound System" -> "🔊"
        "Lighting" -> "💡"
        "Multimedia" -> "🎬"
        "LED Wall" -> "🖥️"
        "Power Generator" -> "⚡"
        "Security" -> "🛡️"
        "Mobile Toilet" -> "🚽"
        "Barricade" -> "🚧"
        "Ambulance" -> "🚑"
        "Tent & Truss" -> "⛺"
        "Heavy Equipment" -> "🚜"
        "Logistics Truck" -> "🚚"
        "Warehouse Storage" -> "🏢"
        "VIP Heli" -> "🚁"
        else -> "📦"
    }
}
