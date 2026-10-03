package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.data.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternationalHospitalDashboardScreen(
    navController: NavHostController,
    viewModel: GameViewModel,
    foundationId: String,
    institutionId: String
) {
    val playerState by viewModel.playerState.collectAsState()
    val foundation = playerState.foundations.find { it.id == foundationId }
    val institution = foundation?.healthInstitutions?.find { it.id == institutionId }

    if (foundation == null || institution == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF091E1A)),
            contentAlignment = Alignment.Center
        ) {
            Text("Data tidak ditemukan.", color = Color.White)
        }
        return
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(institution.name) }
    var editImageUrl by remember { mutableStateOf(institution.imageUrl) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var facilitySortBy by remember { mutableStateOf("Terbaru") }

    var showEditFacilityDialog by remember { mutableStateOf(false) }
    var selectedFacilityForEdit by remember { mutableStateOf<FacilityItem?>(null) }
    var editFacilityCustomName by remember { mutableStateOf("") }

    var showDeleteFacilityConfirmDialog by remember { mutableStateOf(false) }
    var selectedFacilityForDelete by remember { mutableStateOf<FacilityItem?>(null) }

    var showBillDialog by remember { mutableStateOf(false) }
    var billInputValue by remember { mutableStateOf(institution.monthlyBillPerPatient.toString()) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen RS Internasional", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Hibahkan", tint = Color.Red.copy(alpha = 0.8f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B241D))
            )
        },
        containerColor = Color(0xFF091E1A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF091E1A))
        ) {
            // Hero banner section
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                if (institution.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = institution.imageUrl,
                        contentDescription = "RSI Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF003300)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = Color.White, modifier = Modifier.size(54.dp))
                            Text("Rumah Sakit Internasional", color = Color.LightGray, fontSize = 11.sp)
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF091E1A)))))
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Header Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = institution.name, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFD4AF37), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Akreditasi: ${institution.accreditationPoints}/100", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = {
                            editName = institution.name
                            editImageUrl = institution.imageUrl
                            showEditDialog = true
                        },
                        modifier = Modifier.background(Color(0xFF102722), RoundedCornerShape(22.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF81C784))
                    }
                }

                // Check operational status
                if (!institution.isOperational) {
                    val builtHeli = institution.additionalFacilities.count { it.typeId == "dasar_helipad_double" && it.constructionLeftMonths <= 0 }
                    val builtVipUgd = institution.additionalFacilities.count { it.typeId == "dasar_vip_emergency" && it.constructionLeftMonths <= 0 }
                    val builtHybrid = institution.additionalFacilities.count { it.typeId == "menengah_hybrid_or" && it.constructionLeftMonths <= 0 }

                    val heliOk = builtHeli >= 1
                    val vipUgdOk = builtVipUgd >= 1
                    val hybridOk = builtHybrid >= 1

                    val nursesCount = institution.medicalStaff.perawat.active
                    val doctorsCount = institution.medicalStaff.dokterUmum.active
                    val specialistsCount = institution.medicalStaff.dokterSpesialis.active
                    val nurseOk = nursesCount >= 10
                    val doctorOk = doctorsCount >= 4
                    val specialistOk = specialistsCount >= 4

                    val allOk = heliOk && vipUgdOk && hybridOk && nurseOk && doctorOk && specialistOk

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE65100).copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color(0xFFE65100))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "⚠️ PRA-OPERASIONAL: RS Internasional Belum Beroperasi", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Untuk mengaktifkan Rumah Sakit Internasional ini, lengkapi prasyarat berikut:", color = Color.LightGray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(8.dp))

                            ChecklistItem(label = "Helipad Medis Ganda (Min. 1)", current = builtHeli, target = 1, isOk = heliOk)
                            ChecklistItem(label = "VIP Emergency Suite (Min. 1)", current = builtVipUgd, target = 1, isOk = vipUgdOk)
                            ChecklistItem(label = "Kamar Operasi Hybrid (Min. 1)", current = builtHybrid, target = 1, isOk = hybridOk)
                            ChecklistItem(label = "Perawat Medis Aktif (Min. 10)", current = nursesCount, target = 10, isOk = nurseOk)
                            ChecklistItem(label = "Dokter Umum Aktif (Min. 4)", current = doctorsCount, target = 4, isOk = doctorOk)
                            ChecklistItem(label = "Dokter Spesialis Aktif (Min. 4)", current = specialistsCount, target = 4, isOk = specialistOk)

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (allOk) {
                                        val success = viewModel.activateHealthInstitution(foundationId, institution.id)
                                        if (success) {
                                            Toast.makeText(context, "Rumah Sakit Internasional resmi dibuka berstandar dunia!", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = allOk,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32),
                                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                                )
                            ) {
                                Text("Ajukan Izin & Buka RSI", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                } else {
                    // Operational Details Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF102722))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Layanan Operasional RSI", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Jumlah Pasien Aktif:", color = Color.Gray, fontSize = 12.sp)
                                Text("${institution.currentPatients} Pasien", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tarif Layanan:", color = Color.Gray, fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${formatCurrency(institution.monthlyBillPerPatient)} /pasien", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { showBillDialog = true }, modifier = Modifier.size(24.dp)) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Tarif", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Jenis Pelayanan Kesehatan:", color = Color.Gray, fontSize = 12.sp)
                                Box(modifier = Modifier.clickable {
                                    val levels = listOf("Subsidi", "Reguler", "VIP", "VVIP")
                                    val currIndex = levels.indexOf(institution.serviceType)
                                    val nextIndex = (currIndex + 1) % levels.size
                                    viewModel.changeHealthServiceType(foundationId, institution.id, levels[nextIndex])
                                }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(institution.serviceType, color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // HR & Staff Management Panel
                MedicalSdmManagementPanel(foundationId = foundationId, institution = institution, viewModel = viewModel)

                // Additional Built Facilities
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Daftar Sarana & Fasilitas", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Urutkan:", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.clickable {
                                val sortingOptions = listOf("Terbaru", "Termahal", "Termurah")
                                val curr = sortingOptions.indexOf(facilitySortBy)
                                val next = (curr + 1) % sortingOptions.size
                                facilitySortBy = sortingOptions[next]
                            }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(facilitySortBy, color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val rawFac = institution.additionalFacilities ?: emptyList()
                    val sortedFac = when (facilitySortBy) {
                        "Termahal" -> rawFac.sortedByDescending { it.maintenanceCost }
                        "Termurah" -> rawFac.sortedBy { it.maintenanceCost }
                        else -> rawFac // Terbaru (default)
                    }

                    if (sortedFac.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF102722))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Belum ada fasilitas tambahan built-in.", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    } else {
                        sortedFac.forEach { facility ->
                            val constructing = facility.constructionLeftMonths > 0
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF102722))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = facility.customName.ifBlank { facility.name }, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text(text = "Grade: ${facility.gradeName} | Biaya Perawatan: ${formatCurrency(facility.maintenanceCost)}/bln", color = Color.LightGray, fontSize = 11.sp)
                                        if (constructing) {
                                            Text(text = "🚧 Sedang Konstruksi: Sisa ${facility.constructionLeftMonths} Bulan", color = Color(0xFFFFB74D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = {
                                            selectedFacilityForEdit = facility
                                            editFacilityCustomName = facility.customName.ifBlank { facility.name }
                                            showEditFacilityDialog = true
                                        }) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Nama", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(onClick = {
                                            selectedFacilityForDelete = facility
                                            showDeleteFacilityConfirmDialog = true
                                        }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { navController.navigate("international_hospital_facility_catalogue/$foundationId/$institutionId") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mendirikan Sarana Medis Tambahan", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // dialog set bill
    if (showBillDialog) {
        Dialog(onDismissRequest = { showBillDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF102722)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Atur Tarif Layanan Medis", fontWeight = FontWeight.Bold, color = Color.White)
                    OutlinedTextField(
                        value = billInputValue,
                        onValueChange = { billInputValue = it },
                        label = { Text("Tarif Bulanan per Pasien (Rp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showBillDialog = false }) { Text("Batal") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val value = billInputValue.toLongOrNull() ?: 0L
                            viewModel.updateHealthInstitutionBill(foundationId, institution.id, value)
                            showBillDialog = false
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }

    // dialog edit profile
    if (showEditDialog) {
        Dialog(onDismissRequest = { showEditDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF102722)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Ubah Profil Rumah Sakit", fontWeight = FontWeight.Bold, color = Color.White)
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nama RS Internasional") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editImageUrl,
                        onValueChange = { editImageUrl = it },
                        label = { Text("URL Gambar Cover") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditDialog = false }) { Text("Batal") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.updateHealthInstitutionProfile(foundationId, institution.id, editName, editImageUrl)
                            showEditDialog = false
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }

    // dialog edit facility name
    if (showEditFacilityDialog && selectedFacilityForEdit != null) {
        Dialog(onDismissRequest = { showEditFacilityDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF102722)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Edit Nama Sarana", fontWeight = FontWeight.Bold, color = Color.White)
                    OutlinedTextField(
                        value = editFacilityCustomName,
                        onValueChange = { editFacilityCustomName = it },
                        label = { Text("Nama Kustom") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditFacilityDialog = false }) { Text("Batal") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.renameHealthFacility(foundationId, institution.id, selectedFacilityForEdit!!.id, editFacilityCustomName)
                            showEditFacilityDialog = false
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }

    // dialog delete facility
    if (showDeleteFacilityConfirmDialog && selectedFacilityForDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteFacilityConfirmDialog = false },
            title = { Text("Hapus Sarana Medis", color = Color.White) },
            text = { Text("Apakah Anda yakin ingin menghapus sarana '${selectedFacilityForDelete!!.customName.ifBlank { selectedFacilityForDelete!!.name }}'? Biaya konstruksi awal tidak akan kembali.", color = Color.LightGray) },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteHealthFacility(foundationId, institution.id, selectedFacilityForDelete!!.id)
                    showDeleteFacilityConfirmDialog = false
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteFacilityConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // dialog delete institution (hibahkan)
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hibahkan RS Internasional", color = Color.White) },
            text = { Text("Apakah Anda yakin ingin menghibahkan RSI '${institution.name}' kepada negara/sosial? Tindakan ini tidak dapat dibatalkan.", color = Color.LightGray) },
            confirmButton = {
                Button(onClick = {
                    val success = viewModel.deleteHealthInstitution(foundationId, institution.id)
                    if (success) {
                        showDeleteConfirmDialog = false
                        navController.popBackStack()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Hibahkan", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
