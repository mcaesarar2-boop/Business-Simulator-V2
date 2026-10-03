package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.viewmodel.GameViewModel
import com.example.data.calculateTotalOpsCost
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KindergartenDashboardScreen(
    navController: NavHostController,
    viewModel: GameViewModel,
    foundationId: String,
    institutionId: String
) {
    val playerState by viewModel.playerState.collectAsState()
    val foundation = playerState.foundations.find { f -> f.id == foundationId }
    val institution = foundation?.educationInstitutions?.find { inst -> inst.id == institutionId }

    if (foundation == null || institution == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF050C1A)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Institusi TK tidak ditemukan", color = Color.White)
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
    var showCurriculumDialog by remember { mutableStateOf(false) }

    var facilitySortBy by remember { mutableStateOf("Terbaru") }
    var selectedFacilityForEdit by remember { mutableStateOf<com.example.data.FacilityItem?>(null) }
    var showEditFacilityDialog by remember { mutableStateOf(false) }
    var editFacilityCustomName by remember { mutableStateOf("") }

    var selectedFacilityForDelete by remember { mutableStateOf<com.example.data.FacilityItem?>(null) }
    var showDeleteFacilityConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manajemen TK / TKA", fontWeight = FontWeight.Bold, color = Color.White) },
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
                            contentDescription = "Hibahkan",
                            tint = Color.Red.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B192C))
            )
        },
        containerColor = Color(0xFF050C1A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF050C1A))
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
                                    colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F1E36))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ChildCare,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Jenjang Paud & Taman Kanak-Kanak",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF050C1A))
                            )
                        )
                )
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title and Edit Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = institution.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Akreditasi: ${institution.accreditationPoints}/100",
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            editName = institution.name
                            editImageUrl = institution.imageUrl
                            editError = null
                            showEditDialog = true
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF101B2B), RoundedCornerShape(22.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(22.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profil",
                            tint = Color(0xFFD4AF37),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!institution.isOperational) {
                    // MODE A: PRA-OPERASIONAL
                    val facilities = institution.additionalFacilities ?: emptyList()
                    val builtClasses = facilities.count { it.typeId == "dasar_ruang_kelas" && it.constructionLeftMonths <= 0 }
                    val builtGuru = facilities.count { it.typeId == "dasar_kantor_guru" && it.constructionLeftMonths <= 0 }
                    val builtToilet = facilities.count { it.typeId == "dasar_toilet_anak" && it.constructionLeftMonths <= 0 }

                    val classOk = builtClasses >= 2
                    val guruOk = builtGuru >= 1
                    val toiletOk = builtToilet >= 1

                    val teachersCount = institution.teachers.umum.active + institution.teachers.spesialis.active + institution.teachers.senior.active
                    val teachersOk = teachersCount >= 2
                    val janitorOk = institution.supportStaff.ob.active >= 1

                    val allOk = classOk && guruOk && toiletOk && teachersOk && janitorOk

                    // Warning Banner
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF57C00).copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF57C00))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️ Menunggu Akreditasi & Kelengkapan Fasilitas Dasar",
                                color = Color(0xFFFFB74D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Checklist Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Checklist Syarat Operasional",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            ChecklistItem(label = "Ruang Kelas TK (Min. 2)", current = builtClasses, target = 2, isOk = classOk)
                            ChecklistItem(label = "Kantor Guru & Staf (Min. 1)", current = builtGuru, target = 1, isOk = guruOk)
                            ChecklistItem(label = "Toilet Anak Higienis (Min. 1)", current = builtToilet, target = 1, isOk = toiletOk)
                            ChecklistItem(label = "Kuota Tenaga Pengajar Dasar (Min. 2)", current = teachersCount, target = 2, isOk = teachersOk)
                            ChecklistItem(label = "Kuota Staff Kebersihan Dasar (OB) (Min. 1)", current = institution.supportStaff.ob.active, target = 1, isOk = janitorOk)

                            Spacer(modifier = Modifier.height(16.dp))

                            val context = LocalContext.current
                            Button(
                                onClick = {
                                    if (allOk) {
                                        val success = viewModel.activateEducationInstitution(foundationId, institutionId)
                                        if (success) {
                                            Toast.makeText(context, "Akreditasi disetujui! Institusi kini resmi beroperasi.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = allOk,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color.White.copy(alpha = 0.05f),
                                    disabledContentColor = Color.Gray
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Ajukan Akreditasi & Buka Sekolah", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SdmManagementPanel(foundationId, institution, viewModel)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Button Bangun Fasilitas Tambahan (so they can complete the checklist)
                    Button(
                        onClick = {
                            navController.navigate("kindergarten_facility_catalogue/$foundationId/$institutionId")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF0B121E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "[+] Bangun Fasilitas Tambahan",
                            color = Color(0xFF0B121E),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                } else {
                    // MODE B: BEROPERASI (Original stats grid and SPP Card)
                    // Stats Grid
                    Text(
                        text = "Statistik Institusi",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val maxStudents = when (institution.curriculumType) {
                        "Nasional" -> 300
                        "Montessori" -> 90
                        "Waldorf" -> 40
                        else -> 300
                    }
                    val activeCurrMultiplier = when (institution.curriculumType) {
                        "Montessori", "Waldorf" -> 1.5
                        "Agama Terpadu" -> 1.75
                        "Cambridge", "IB" -> 3.0
                        "Internasional" -> 6.0
                        else -> 1.0
                    }
                    val totalFacilityMaintenanceCost = institution.additionalFacilities?.sumOf { it.maintenanceCost } ?: 0L
                    val baseCost = if (institution.baseMaintenanceCost > 0L) {
                        institution.baseMaintenanceCost + totalFacilityMaintenanceCost
                    } else {
                        institution.monthlyOperationalCost
                    }
                    val opsCost = institution.calculateTotalOpsCost()
                    val monthlyRevenue = institution.currentStudents * institution.monthlySpp
                    val netIncome = monthlyRevenue - opsCost

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showCurriculumDialog = true },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Kurikulum", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        institution.curriculumType,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Ganti Kurikulum",
                                        tint = Color(0xFFD4AF37),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Jumlah Murid", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${institution.currentStudents} / $maxStudents Maks",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Ops Bulanan", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    formatCurrency(opsCost),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE57373),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Pemasukan SPP Bulanan", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    formatCurrency(monthlyRevenue),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF81C784),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Prestige Score", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "+${institution.prestigeScore} Prestige",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF81C784),
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Level Fasilitas", color = Color.Gray, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Tier ${institution.facilityLevel} / 5",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF64B5F6),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Keuangan (Banner)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
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
                                text = if (netIncome < 0) "⚠️ Yayasan Defisit (Disubsidi Dana Abadi)" else "✅ Yayasan Mandiri (Surplus)",
                                color = if (netIncome < 0) Color(0xFFEF5350) else Color(0xFF66BB6A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            navController.navigate("kindergarten_facility_catalogue/$foundationId/$institutionId")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF0B121E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "[+] Bangun Fasilitas Tambahan",
                            color = Color(0xFF0B121E),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Menu Manajemen SPP
                    var sppInputText by remember(institution.monthlySpp) { mutableStateOf(institution.monthlySpp.toString()) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Atur Kebijakan SPP",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Tentukan SPP bulanan per murid. Gratis meningkatkan reputasi, sedangkan SPP tinggi menghasilkan pemasukan untuk dana abadi.",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = sppInputText,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                        sppInputText = newValue
                                    }
                                },
                                label = { Text("Atur Biaya SPP Bulanan per Murid ($)", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD4AF37),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedLabelColor = Color(0xFFD4AF37),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val context = LocalContext.current
                            Button(
                                onClick = {
                                    val newSpp = sppInputText.toLongOrNull() ?: 0L
                                    val success = viewModel.updateEducationInstitutionSpp(foundationId, institutionId, newSpp)
                                    if (success) {
                                        Toast.makeText(context, "Kebijakan SPP berhasil diubah menjadi ${formatCurrency(newSpp)}/murid.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Terapkan Kebijakan SPP", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    SdmManagementPanel(foundationId, institution, viewModel)

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Gedung & Infrastruktur Utama
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(
                            1.dp,
                            Color(0xFFD4AF37).copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Gedung & Infrastruktur Utama",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Kualitas Fisik: ${institution.buildingGrade}",
                                    color = Color(0xFFD4AF37),
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
                            Text("Biaya Rawat Gedung Dasar:", color = Color.Gray, fontSize = 11.sp)
                            Text(
                                text = "${formatCurrency(institution.baseMaintenanceCost)} / bln",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Daftar Fasilitas Tambahan
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Fasilitas Tambahan yang Dimiliki",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Fasilitas ekstra yang menunjang kegiatan belajar-mengajar dan melengkapi kualitas institusi.",
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
                                        "Biaya ⬇️" -> "Biaya Perawatan"
                                        else -> opt
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Color(0xFFD4AF37) else Color(0xFF14223A))
                                            .border(1.dp, if (isSelected) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
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
                                            color = if (isSelected) Color(0xFF0F1E36) else Color.LightGray,
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
                                        .background(Color(0xFF14223A), RoundedCornerShape(8.dp))
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
                                                "Grade: ${fac.gradeName}",
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
                                                "Dalam Konstruksi",
                                                color = Color(0xFFFFB300),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        } else {
                                            Text(
                                                "${formatCurrency(fac.maintenanceCost)} / bln",
                                                color = Color(0xFF10B981),
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

                // Banner / Card Promosi Katalog
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .clickable {
                            navController.navigate("kindergarten_facility_catalogue/$foundationId/$institutionId")
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14223A)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFFD4AF37).copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFF0F1E36), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = Color(0xFFD4AF37)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Konstruksi & Upgrade Fasilitas",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                "Mulai dari fasilitas dasar hingga fasilitas internasional.",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Buka",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profil Institusi", fontWeight = FontWeight.Bold, color = Color.White) },
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
                        label = { Text("Ubah Nama Institusi", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4AF37),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = Color(0xFFD4AF37),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = editImageUrl,
                        onValueChange = { editImageUrl = it },
                        label = { Text("URL Gambar / Logo (PNG/JPG)", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4AF37),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = Color(0xFFD4AF37),
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
                        val success = viewModel.updateEducationInstitutionProfile(
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                ) {
                    Text("Simpan", color = Color(0xFF0F1E36), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Tutup & Hibahkan Fasilitas?", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Text(
                    "Apakah Anda yakin ingin menutup '${institution.name}' dan menghibahkannya ke pemerintah daerah? Bangunan ini akan dihapus secara permanen dari yayasan Anda.",
                    color = Color.LightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteEducationInstitution(foundationId, institutionId)
                        showDeleteConfirmDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Ya, Tutup Fasilitas", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    if (showCurriculumDialog) {
        val options = listOf("Nasional", "Montessori", "Waldorf")
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showCurriculumDialog = false },
            title = { Text("Pilih Kurikulum TK/TKA", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Kurikulum khusus meningkatkan kualitas akreditasi dan reputasi, namun meningkatkan biaya operasional.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    options.forEach { option ->
                        val isSelected = institution.curriculumType == option
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val success = viewModel.updateInstitutionCurriculum(foundationId, institutionId, option)
                                    if (success) {
                                        Toast.makeText(context, "Kurikulum diubah. Kapasitas dan jumlah murid telah disesuaikan.", Toast.LENGTH_SHORT).show()
                                    }
                                    showCurriculumDialog = false
                                }
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1B2C3F) else Color(0xFF101B2B)
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
                                    color = if (isSelected) Color(0xFFD4AF37) else Color.White,
                                    fontSize = 13.sp
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Selected",
                                        tint = Color(0xFFD4AF37),
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
                TextButton(onClick = { showCurriculumDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    if (showEditFacilityDialog && selectedFacilityForEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditFacilityDialog = false },
            title = { Text("Ubah Nama Fasilitas", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column {
                    Text("Ubah nama tampilan untuk fasilitas ini:", color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editFacilityCustomName,
                        onValueChange = { editFacilityCustomName = it },
                        label = { Text("Nama Kustom") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFD4AF37),
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
                        viewModel.renameFacility(foundationId, institutionId, fac.id, editFacilityCustomName)
                        showEditFacilityDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                ) {
                    Text("Simpan", color = Color(0xFF0F1E36), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditFacilityDialog = false }) {
                    Text("Batal", color = Color.White)
                }
            },
            containerColor = Color(0xFF0F1E36)
        )
    }

    if (showDeleteFacilityConfirmDialog && selectedFacilityForDelete != null) {
        val fac = selectedFacilityForDelete!!
        AlertDialog(
            onDismissRequest = { showDeleteFacilityConfirmDialog = false },
            title = { Text("Bongkar Fasilitas", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Text(
                    "Bongkar fasilitas ${fac.customName.ifBlank { fac.name }}? Tindakan ini tidak dapat dibatalkan dan akan menghentikan biaya perawatan bulanan sebesar ${formatCurrency(fac.maintenanceCost)}.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFacility(foundationId, institutionId, fac.id)
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
            containerColor = Color(0xFF0F1E36)
        )
    }
}
