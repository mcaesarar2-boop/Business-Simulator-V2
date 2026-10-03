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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.data.*
import com.example.viewmodel.GameViewModel

val FULL_INTERNATIONAL_HOSPITAL_CATALOGUE = listOf(
    // === FASILITAS DASAR ===
    CatalogueFacility("dasar_helipad_double", "Helipad Medis Ganda", "Dua landasan evakuasi helikopter komersial maupun militer sekaligus.", 250000L, "Dasar", Icons.Default.FlightLand),
    CatalogueFacility("dasar_vip_emergency", "VIP Emergency Suite", "IGD berfasilitas hotel bintang lima dengan jalur aman rekam medis super rahasia.", 120000L, "Dasar", Icons.Default.MedicalServices),
    CatalogueFacility("dasar_smart_ward", "Smart Ward Room (VIP)", "Kamar rawat inap otomatis berkontrol suara dan tablet terintegrasi ranjang medis.", 100000L, "Dasar", Icons.Default.Bed),
    CatalogueFacility("dasar_preventive", "Pusat Kesehatan Preventif", "Fasilitas deteksi risiko penyakit genetik sebelum gejala klinis muncul.", 90000L, "Dasar", Icons.Default.Security),
    CatalogueFacility("dasar_farmasi_robot", "Farmasi Robotik Terintegrasi", "Sistem pembagian dan pengantaran obat otomatis presisi tanpa eror manusia.", 110000L, "Dasar", Icons.Default.LocalPharmacy),

    // === FASILITAS MENENGAH ===
    CatalogueFacility("menengah_hybrid_or", "Kamar Operasi Hybrid", "Kamar operasi terintegrasi dengan angiografi, CT, dan MRI real-time.", 300000L, "Menengah", Icons.Default.Handyman),
    CatalogueFacility("menengah_mri_7t", "Pencitraan MRI 7 Tesla", "Teknologi visualisasi anatomi saraf dan otak beresolusi ultra-tinggi.", 350000L, "Menengah", Icons.Default.Psychology),
    CatalogueFacility("menengah_genomik", "Lab Genomik Komparatif", "Pemetaan DNA lengkap pasien untuk pengobatan kustom kanker/autoimun.", 280000L, "Menengah", Icons.Default.Biotech),
    CatalogueFacility("menengah_bsl3", "Biosafety Level 3 Lab", "Laboratorium penelitian patogen berbahaya berstandar WHO.", 240000L, "Menengah", Icons.Default.Science),

    // === FASILITAS TINGGI ===
    CatalogueFacility("tinggi_proton_therapy", "Proton Therapy Center", "Pusat terapi kanker tercanggih di dunia menggunakan radiasi berkas proton.", 700000L, "Tinggi", Icons.Default.FlashOn),
    CatalogueFacility("tinggi_icu_digital", "ICU Digital AI-Monitored", "Ruang ICU canggih dengan algoritma prediksi kondisi vital pasien.", 400000L, "Tinggi", Icons.Default.MonitorHeart),
    CatalogueFacility("tinggi_presidential", "Presidential Suite Ward", "Kamar rawat inap termewah sekelas delegasi negara dan selebritas.", 350000L, "Tinggi", Icons.Default.Hotel),
    CatalogueFacility("tinggi_regeneratif", "Pusat Kedokteran Regeneratif", "Terapi kloning sel organ biologis untuk peremajaan sel tubuh.", 450000L, "Tinggi", Icons.Default.SettingsAccessibility),

    // === FASILITAS INTERNASIONAL ===
    CatalogueFacility("inter_ai_mainframe", "AI Diagnostics Mainframe", "Superkomputer diagnosis medis terintegrasi rekam medis dunia.", 900000L, "Internasional", Icons.Default.Memory),
    CatalogueFacility("inter_telehealth", "Global Telehealth Command", "Ruang kendali operasi bedah jarak jauh antar benua real-time.", 800000L, "Internasional", Icons.Default.Language),
    CatalogueFacility("inter_subatomic", "Subatomic Medical Science Center", "Riset nuklir tingkat lanjut dan akselerator partikel medis.", 1200000L, "Internasional", Icons.Default.Hub)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InternationalHospitalFacilityCatalogueScreen(
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

    val filteredList = FULL_INTERNATIONAL_HOSPITAL_CATALOGUE.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Katalog Fasilitas RSI",
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
