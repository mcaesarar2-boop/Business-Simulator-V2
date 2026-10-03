package com.example.ui.charity

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.viewmodel.GameViewModel
import com.example.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialCareDashboardScreen(
    navController: NavHostController,
    viewModel: GameViewModel,
    foundationId: String,
    institutionId: String
) {
    val playerState by viewModel.playerState.collectAsState()
    val foundation = playerState.foundations.find { f -> f.id == foundationId }
    val institution = foundation?.charityInstitutions?.find { inst -> inst.id == institutionId }

    if (foundation == null || institution == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F141D)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Institusi Rehabilitasi & Sosial tidak ditemukan", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Kembali")
                }
            }
        }
        return
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(institution.name) }
    var editImageUrl by remember { mutableStateOf(institution.imageUrl) }
    var editError by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showScopeDialog by remember { mutableStateOf(false) }

    var facilitySortBy by remember { mutableStateOf("Terbaru") }
    var selectedFacilityForEdit by remember { mutableStateOf<com.example.data.FacilityItem?>(null) }
    var showEditFacilityDialog by remember { mutableStateOf(false) }
    var editFacilityCustomName by remember { mutableStateOf("") }

    var selectedFacilityForDelete by remember { mutableStateOf<com.example.data.FacilityItem?>(null) }
    var showDeleteFacilityConfirmDialog by remember { mutableStateOf(false) }

    val totalMonthlyOps = institution.calculateTotalMonthlyOpsCost()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen Panti & Rehab Sosial", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Tutup & Hibahkan",
                            tint = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1410))
            )
        },
        containerColor = Color(0xFF0F0B09)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F0B09))
        ) {
            // Hero Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                if (institution.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = institution.imageUrl,
                        contentDescription = "Hero Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFE07A5F), Color(0xFF1A1512))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Accessibility,
                                contentDescription = null,
                                tint = Color(0xFFF4A261),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Panti & Rehabilitasi Sosial",
                                color = Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Overlay Edit Button
                IconButton(
                    onClick = {
                        editName = institution.name
                        editImageUrl = institution.imageUrl
                        editError = null
                        showEditDialog = true
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profil", tint = Color.White)
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sisa Konstruksi Banner (Jika belum operasional)
                if (!institution.isOperational) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD4AF37).copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                progress = { (institution.constructionTotalMonths - institution.constructionLeftMonths).toFloat() / institution.constructionTotalMonths.toFloat() },
                                modifier = Modifier.size(36.dp),
                                color = Color(0xFFD4AF37),
                                strokeWidth = 3.dp,
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "DALAM KONSTRUKSI",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37),
                                    fontSize = 13.sp
                                )
                                Text(
                                    "Fasilitas baru selesai didirikan dalam sisa ${institution.constructionLeftMonths} bulan lagi in-game.",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                // Status Ringkas & Kinerja Posko
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1612)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Jangkauan Program", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                institution.scope,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF4A261),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1612)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Penerima Manfaat", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${institution.monthlyBeneficiaries} / ${institution.maxCapacity} Jiwa",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81C784),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1612)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Sponsor Bulanan", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$${com.example.ui.formatCurrencyRingkas(institution.monthlySponsorshipRevenue, false)} / bln",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB300),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Status Keuangan & Operasional Posko
                val netIncome = institution.monthlySponsorshipRevenue - totalMonthlyOps
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (netIncome < 0) Color(0xFFE57373).copy(alpha = 0.15f) else Color(0xFF81C784).copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (netIncome < 0) "⚠️ Posko Defisit (Disubsidi Dana Abadi)" else "✅ Posko Mandiri (Surplus)",
                            color = if (netIncome < 0) Color(0xFFEF5350) else Color(0xFF66BB6A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Button to catalog
                Button(
                    onClick = {
                        navController.navigate("social_care_facility_catalogue/$foundationId/$institutionId")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4A261)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF1E1612),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "[+] Bangun Fasilitas Panti Tambahan",
                        color = Color(0xFF1E1612),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                // Scope Selector Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1612)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Atur Cakupan Rehabilitasi",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Pilih jangkauan layanan rehabilitasi panti Anda. Cakupan lebih luas melipatgandakan kapasitas panti dan reputasi, namun menuntut biaya operasional yang jauh lebih tinggi.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Cakupan Aktif:", color = Color.Gray, fontSize = 11.sp)
                                Text(institution.scope, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                            Button(
                                onClick = { showScopeDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4A261).copy(alpha = 0.2f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF4A261))
                            ) {
                                Text("Ubah Cakupan", color = Color(0xFFF4A261), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // HR management panel
                CharitySdmManagementPanel(foundationId, institution, viewModel)

                // Main Building details
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF4A261).copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1612)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = Color(0xFFF4A261),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Gedung Panti Utama & Infrastruktur",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Spesifikasi Gedung: ${institution.buildingGrade}",
                                    color = Color(0xFFF4A261),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Biaya Rawat Dasar Gedung:", color = Color.Gray, fontSize = 11.sp)
                            Text(
                                text = "$${com.example.ui.formatCurrency(institution.baseMaintenanceCost)} / bln",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Additional facilities section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1612)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Fasilitas Panti Tambahan",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Fasilitas tambahan untuk mempermudah layanan panti dan rehabilitasi sosial.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val facilitiesList = institution.additionalFacilities ?: emptyList()
                        if (facilitiesList.isEmpty()) {
                            Text(
                                "Belum ada fasilitas tambahan yang dibangun.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Urutan:", color = Color.Gray, fontSize = 11.sp)
                                listOf("Terbaru", "A-Z", "Biaya ⬇️").forEach { opt ->
                                    val isSelected = when (opt) {
                                        "Terbaru" -> facilitySortBy == "Terbaru"
                                        "A-Z" -> facilitySortBy == "Abjad"
                                        "Biaya ⬇️" -> facilitySortBy == "Biaya"
                                        else -> false
                                    }
                                    val label = when (opt) {
                                        "Terbaru" -> "Terbaru"
                                        "A-Z" -> "Abjad"
                                        "Biaya ⬇️" -> "Biaya Operasional"
                                        else -> opt
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Color(0xFFF4A261) else Color(0xFF261D19))
                                            .border(1.dp, if (isSelected) Color(0xFFF4A261) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                            .clickable {
                                                facilitySortBy = when (opt) {
                                                    "Terbaru" -> "Terbaru"
                                                    "A-Z" -> "Abjad"
                                                    "Biaya ⬇️" -> "Biaya"
                                                    else -> "Terbaru"
                                                }
                                            }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color(0xFF1E1612) else Color.LightGray,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            val sortedFacilities = when (facilitySortBy) {
                                "Abjad" -> facilitiesList.sortedBy { (if (!it.customName.isNullOrBlank()) it.customName else it.name).lowercase() }
                                "Biaya" -> facilitiesList.sortedByDescending { it.maintenanceCost }
                                else -> facilitiesList
                            }

                            sortedFacilities.forEach { fac ->
                                val displayName = if (!fac.customName.isNullOrBlank()) "${fac.customName} (${fac.name})" else fac.name
                                val isUnderConstruction = fac.constructionLeftMonths > 0
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .background(Color(0xFF261D19), RoundedCornerShape(8.dp))
                                        .padding(start = 12.dp, top = 10.dp, end = 6.dp, bottom = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            displayName,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "Kualitas: ${fac.gradeName}",
                                                color = Color.Gray,
                                                fontSize = 10.sp
                                            )
                                            if (isUnderConstruction) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "🚧 Sisa ${fac.constructionLeftMonths} Bulan",
                                                    color = Color(0xFFFFB300),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isUnderConstruction) {
                                            Text(
                                                "Konstruksi",
                                                color = Color(0xFFFFB300),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        } else {
                                            Text(
                                                "$${com.example.ui.formatCurrency(fac.maintenanceCost)} / bln",
                                                color = Color(0xFF81C784),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                selectedFacilityForEdit = fac
                                                editFacilityCustomName = fac.customName.ifBlank { fac.name }
                                                showEditFacilityDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Nama",
                                                tint = Color.LightGray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                selectedFacilityForDelete = fac
                                                showDeleteFacilityConfirmDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Bongkar",
                                                tint = Color.Red.copy(alpha = 0.7f),
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

    // Dialog: Edit Profil
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profil Panti", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nama Panti Rehabilitasi", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF4A261),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = Color(0xFFF4A261),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editImageUrl,
                        onValueChange = { editImageUrl = it },
                        label = { Text("URL Gambar / Logo", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF4A261),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = Color(0xFFF4A261),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    if (editError != null) {
                        Text(editError!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isBlank()) {
                            editError = "Nama tidak boleh kosong!"
                            return@Button
                        }
                        // Re-use same update model flow
                        val success = viewModel.updateCharityInstitutionProfile(
                            foundationId = foundationId,
                            institutionId = institutionId,
                            newName = editName,
                            newImageUrl = editImageUrl
                        )
                        if (success) {
                            showEditDialog = false
                        } else {
                            editError = "Gagal memperbarui profil."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4A261))
                ) {
                    Text("Simpan", color = Color(0xFF1E1612), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal")
                }
            },
            containerColor = Color(0xFF1E1612)
        )
    }

    // Dialog: Delete Confirm
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hibahkan Panti Rehabilitasi?", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Text(
                    "Apakah Anda yakin ingin menutup '${institution.name}' dan menyerahkan pengelolaannya sepenuhnya kepada komunitas lokal? Tindakan ini permanen dan menghapus panti dari daftar yayasan Anda.",
                    color = Color.LightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCharityInstitution(foundationId, institutionId)
                        showDeleteConfirmDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Ya, Hibahkan", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1612)
        )
    }

    // Dialog: Scope Selector
    if (showScopeDialog) {
        val options = listOf("Lokal", "Nasional", "Internasional")
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showScopeDialog = false },
            title = { Text("Pilih Jangkauan Rehabilitasi", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Memperluas cakupan meningkatkan jangkauan penerima manfaat secara eksponensial, namun menambah beban logistik dasar.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    options.forEach { option ->
                        val isSelected = institution.scope == option
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val success = viewModel.changeCharityScope(foundationId, institutionId, option)
                                    if (success) {
                                        Toast.makeText(context, "Cakupan rehabilitasi panti diubah ke $option.", Toast.LENGTH_SHORT).show()
                                    }
                                    showScopeDialog = false
                                }
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFFF4A261) else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF33231B) else Color(0xFF261D19)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFFF4A261) else Color.White,
                                    fontSize = 13.sp
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Selected",
                                        tint = Color(0xFFF4A261),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showScopeDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1612)
        )
    }

    // Dialog: Edit Facility Custom Name
    if (showEditFacilityDialog && selectedFacilityForEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditFacilityDialog = false },
            title = { Text("Ubah Nama Fasilitas", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column {
                    Text("Ubah nama tampilan untuk fasilitas panti ini:", color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editFacilityCustomName,
                        onValueChange = { editFacilityCustomName = it },
                        label = { Text("Nama Kustom") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFF4A261),
                            unfocusedBorderColor = Color.Gray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fac = selectedFacilityForEdit!!
                        viewModel.renameCharityFacilityItem(foundationId, institutionId, fac.id, editFacilityCustomName)
                        showEditFacilityDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4A261))
                ) {
                    Text("Simpan", color = Color(0xFF1E1612), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditFacilityDialog = false }) {
                    Text("Batal", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1612)
        )
    }

    // Dialog: Delete Facility Confirm
    if (showDeleteFacilityConfirmDialog && selectedFacilityForDelete != null) {
        val fac = selectedFacilityForDelete!!
        AlertDialog(
            onDismissRequest = { showDeleteFacilityConfirmDialog = false },
            title = { Text("Bongkar Fasilitas Panti", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Text(
                    "Bongkar fasilitas ${fac.customName.ifBlank { fac.name }}? Tindakan ini permanen dan akan menghentikan biaya perawatan bulanan sebesar $${com.example.ui.formatCurrency(fac.maintenanceCost)}.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCharityFacilityItem(foundationId, institutionId, fac.id)
                        showDeleteFacilityConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Bongkar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteFacilityConfirmDialog = false }) {
                    Text("Batal", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1612)
        )
    }
}
