package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.data.*
import com.example.viewmodel.GameViewModel

val FULL_CLINIC_CATALOGUE = listOf(
    // === FASILITAS DASAR ===
    CatalogueFacility("dasar_ruang_periksa", "Ruang Periksa Utama", "Ruang konsultasi dokter umum dan pemeriksaan dasar pasien.", 15000L, "Dasar", Icons.Default.MedicalServices),
    CatalogueFacility("dasar_resepsionis", "Meja Resepsionis & Registrasi", "Pendaftaran pasien dan administrasi rekam medis.", 5000L, "Dasar", Icons.Default.AppRegistration),
    CatalogueFacility("dasar_apotek_mini", "Apotek Mini Klinik", "Tempat penyimpanan dan distribusi obat resep dasar.", 10000L, "Dasar", Icons.Default.LocalPharmacy),
    CatalogueFacility("dasar_kursi_tunggu", "Kursi Tunggu Pasien", "Area tunggu berkapasitas memadai dan nyaman.", 4000L, "Dasar", Icons.Default.Chair),
    CatalogueFacility("dasar_sterilisator", "Alat Sterilisator Medis", "Menjaga kebersihan dan sterilitas alat-alat bedah ringan.", 8000L, "Dasar", Icons.Default.CleanHands),
    CatalogueFacility("dasar_p3k", "Kotak P3K Lengkap", "Perlengkapan pertolongan pertama pada kecelakaan darurat.", 3000L, "Dasar", Icons.Default.AddBox),
    CatalogueFacility("dasar_toilet_klinik", "Toilet Pasien Higienis", "Fasilitas sanitasi bersih ramah penyandang disabilitas.", 9000L, "Dasar", Icons.Default.Wc),

    // === FASILITAS MENENGAH ===
    CatalogueFacility("menengah_usg", "Alat USG Portable", "Pemeriksaan ultrasonografi dasar untuk pasien kandungan & umum.", 25000L, "Menengah", Icons.Default.PersonalVideo),
    CatalogueFacility("menengah_laboratorium", "Laboratorium Darah Mini", "Pemeriksaan darah lengkap, kolesterol, dan gula darah instan.", 35000L, "Menengah", Icons.Default.Science),
    CatalogueFacility("menengah_rontgen", "Ruang Rontgen Mini", "Sistem radiologi X-Ray digital dengan proteksi radiasi timbal.", 45000L, "Menengah", Icons.Default.Camera),
    CatalogueFacility("menengah_ruang_tindakan", "Ruang Tindakan Bedah Minor", "Penjahitan luka, bedah ringan, dan pertolongan pertama trauma.", 30000L, "Menengah", Icons.Default.Handyman),
    CatalogueFacility("menengah_bed_observasi", "Bed Observasi Pasien", "Tempat istirahat pasien pasca tindakan bedah minor.", 15000L, "Menengah", Icons.Default.Bed),
    CatalogueFacility("menengah_tabung_oksigen", "Tabung Oksigen & Nebulizer", "Penanganan darurat sesak napas dan asma akut.", 12000L, "Menengah", Icons.Default.Air),

    // === FASILITAS TINGGI ===
    CatalogueFacility("tinggi_ambulans", "Ambulans Klinik Siaga", "Armada rujukan darurat pasien ke rumah sakit besar terdekat.", 80000L, "Tinggi", Icons.Default.AirportShuttle),
    CatalogueFacility("tinggi_poli_gigi", "Poli Gigi Terpadu", "Kursi pemeriksaan gigi komplit dengan pembersih karang gigi.", 70000L, "Tinggi", Icons.Default.Healing),
    CatalogueFacility("tinggi_poli_kia", "Poli Kesehatan Ibu & Anak (KIA)", "Pemeriksaan kehamilan rutin, KB, dan imunisasi balita.", 60000L, "Tinggi", Icons.Default.ChildFriendly),
    CatalogueFacility("tinggi_ruang_laktasi", "Ruang Laktasi & Menyusui", "Area privat dan steril bagi ibu menyusui.", 20000L, "Tinggi", Icons.Default.Favorite),
    CatalogueFacility("tinggi_pcr_lab", "Laboratorium PCR & Swab", "Fasilitas pengetesan virus cepat dan aman.", 55000L, "Tinggi", Icons.Default.Biotech),

    // === FASILITAS INTERNASIONAL ===
    CatalogueFacility("inter_telemedicine", "Telemedicine Center", "Konsultasi jarak jauh interaktif dengan dokter spesialis global.", 120000L, "Internasional", Icons.Default.SettingsAccessibility),
    CatalogueFacility("inter_fisio", "Ruang Fisioterapi Modern", "Peralatan pemulihan cedera fisik dan terapi okupasi.", 110000L, "Internasional", Icons.Default.AccessibilityNew),
    CatalogueFacility("inter_ekg", "Alat EKG Jantung Premium", "Deteksi dini kelainan irama jantung secara presisi.", 130000L, "Internasional", Icons.Default.MonitorHeart)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicFacilityCatalogueScreen(
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

    var selectedCategory by remember { mutableStateOf("Dasar") }
    val categories = listOf("Dasar", "Menengah", "Tinggi", "Internasional")

    var selectedItemForBuild by remember { mutableStateOf<CatalogueFacility?>(null) }
    var showGradeDialog by remember { mutableStateOf(false) }

    val filteredList = FULL_CLINIC_CATALOGUE.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Katalog Fasilitas Medis",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Text(
                            text = institution.name,
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
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
            // Category Tabs
            TabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = Color(0xFF0F3227),
                contentColor = Color.White
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = { Text(category, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dana Abadi Yayasan:",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
                Text(
                    text = formatCurrency(foundation.endowmentFund),
                    color = Color(0xFF81C784),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Facilities list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { facility ->
                    val alreadyBuilt = institution.additionalFacilities.any { it.typeId == facility.typeId }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (alreadyBuilt) Color(0xFF2E7D32) else Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (alreadyBuilt) Color(0xFF0F3227).copy(alpha = 0.4f) else Color(0xFF102722)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (alreadyBuilt) Color(0xFF2E7D32) else Color(0xFF1B3B32),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = facility.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = facility.name,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = facility.description,
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(
                                        text = "Biaya: ${formatCurrency(facility.constructionCost)}",
                                        color = Color(0xFF81C784),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Waktu: ${facility.baseBuildTime} Bln",
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (alreadyBuilt) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF2E7D32).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("AKTIF", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        selectedItemForBuild = facility
                                        showGradeDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Bangun", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog choosing building grade
    if (showGradeDialog && selectedItemForBuild != null) {
        val item = selectedItemForBuild!!
        val context = LocalContext.current

        val grades = listOf(
            Triple("Grade C", 1.0, 1.0),
            Triple("Grade B", 1.5, 1.2),
            Triple("Grade A", 2.2, 1.5),
            Triple("Grade S", 3.5, 2.0)
        )

        Dialog(onDismissRequest = { showGradeDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF102722)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Pilih Grade Bangunan",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )

                    Text(
                        text = "Pilihlah spesifikasi material konstruksi untuk '${item.name}'. Grade yang lebih tinggi menaikkan performa fasilitas medis jangka panjang.",
                        color = Color.LightGray,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    grades.forEach { (grade, costMult, mainMult) ->
                        val finalCost = (item.constructionCost * costMult).toLong()
                        val monthlyMain = (item.constructionCost * 0.005 * mainMult).toLong()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (foundation.endowmentFund < finalCost) {
                                        Toast.makeText(context, "Dana Abadi Yayasan kurang!", Toast.LENGTH_SHORT).show()
                                        return@clickable
                                    }

                                    val success = viewModel.buildHealthAdditionalFacility(
                                        foundationId = foundationId,
                                        institutionId = institutionId,
                                        typeId = item.typeId,
                                        name = item.name,
                                        customName = "",
                                        gradeId = grade,
                                        maintenanceCost = monthlyMain,
                                        constructionCost = finalCost,
                                        constructionTotalMonths = item.baseBuildTime,
                                        constructionLeftMonths = item.baseBuildTime
                                    )

                                    if (success) {
                                        Toast.makeText(context, "Konstruksi '${item.name}' dimulai!", Toast.LENGTH_SHORT).show()
                                        showGradeDialog = false
                                    } else {
                                        Toast.makeText(context, "Konstruksi gagal.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3227))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = grade, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    Text(
                                        text = formatCurrency(finalCost),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF81C784),
                                        fontSize = 13.sp
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Pemeliharaan/bln:", color = Color.Gray, fontSize = 11.sp)
                                    Text(text = "${formatCurrency(monthlyMain)}/bln", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TextButton(
                        onClick = { showGradeDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Batal", color = Color.White)
                    }
                }
            }
        }
    }
}
