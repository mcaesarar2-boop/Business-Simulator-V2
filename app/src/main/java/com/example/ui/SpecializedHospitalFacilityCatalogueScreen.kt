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

val FULL_SPECIALIZED_HOSPITAL_CATALOGUE = listOf(
    // === FASILITAS DASAR ===
    CatalogueFacility("dasar_ugd_spesifik", "IGD Gawat Darurat Khusus", "UGD khusus penanganan serangan jantung, stroke, atau trauma akut.", 80000L, "Dasar", Icons.Default.MedicalServices),
    CatalogueFacility("dasar_konsultasi_sp", "Klinik Ahli Spesialis", "Ruang konsultasi khusus dokter subspesialis terakreditasi.", 40000L, "Dasar", Icons.Default.PersonSearch),
    CatalogueFacility("dasar_kamar_mikro", "Kamar Bedah Mikro", "Kamar operasi presisi tinggi untuk bedah saraf dan pembuluh darah halus.", 150000L, "Dasar", Icons.Default.Handyman),
    CatalogueFacility("dasar_icu_jantung", "ICU / ICCU Jantung", "Intensive Coronary Care Unit khusus pemulihan pasca operasi jantung.", 120000L, "Dasar", Icons.Default.MonitorHeart),
    CatalogueFacility("dasar_farmasi_onko", "Depo Farmasi Onkologi & Kanker", "Penyimpanan dan pencampuran obat kemoterapi steril.", 60000L, "Dasar", Icons.Default.LocalPharmacy),

    // === FASILITAS MENENGAH ===
    CatalogueFacility("menengah_patologi_mol", "Lab Patologi Molekuler", "Analisis mutasi genetik tumor dan kanker secara molekuler.", 150000L, "Menengah", Icons.Default.Biotech),
    CatalogueFacility("menengah_nuklir", "Instalasi Kedokteran Nuklir", "Diagnosis fungsional organ dalam menggunakan radiofarmaka.", 180000L, "Menengah", Icons.Default.Science),
    CatalogueFacility("menengah_kemo_sentral", "Ruang Kemoterapi Sentral", "Kabin kemoterapi personal dilengkapi infusa otomatis yang tenang.", 130000L, "Menengah", Icons.Default.Chair),
    CatalogueFacility("menengah_bed_isolasi", "Bed Isolasi Imunodefisiensi", "Kamar rawat steril bagi pasien pasca transplantasi sumsum tulang.", 90000L, "Menengah", Icons.Default.Bed),

    // === FASILITAS TINGGI ===
    CatalogueFacility("tinggi_cathlab_sp", "Cath Lab Multiguna", "Laboratorium intervensi kardiovaskular dan DSA stroke.", 300000L, "Tinggi", Icons.Default.Favorite),
    CatalogueFacility("tinggi_rehab_stroke", "Pusat Rehabilitasi Stroke", "Terapi sensorik motorik canggih menggunakan lokomotor robotik.", 140000L, "Tinggi", Icons.Default.AccessibilityNew),
    CatalogueFacility("tinggi_terapi_sel", "Lab Terapi Sel & Stem Cell", "Kultur sel punca untuk regenerasi jaringan dan terapi selular.", 250000L, "Tinggi", Icons.Default.SettingsAccessibility),
    CatalogueFacility("tinggi_neuro_ambulans", "Ambulans Stroke Unit", "Ambulans darurat dilengkapi CT Scan mobile untuk diagnosis stroke di jalan.", 200000L, "Tinggi", Icons.Default.AirportShuttle),

    // === FASILITAS INTERNASIONAL ===
    CatalogueFacility("inter_linear_acc", "Radioterapi Linear Accelerator", "Penembakan radiasi kanker super presisi tanpa merusak sel sehat.", 600000L, "Internasional", Icons.Default.Psychology),
    CatalogueFacility("inter_robotic_surgery", "Robot Bedah DaVinci", "Sistem bedah robotik jarak jauh dengan tingkat presisi mikroskopis.", 750000L, "Internasional", Icons.Default.Settings),
    CatalogueFacility("inter_rare_disease", "Rare Disease Research Hub", "Laboratorium riset internasional untuk penyakit langka dunia.", 500000L, "Internasional", Icons.Default.Language)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecializedHospitalFacilityCatalogueScreen(
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

    val filteredList = FULL_SPECIALIZED_HOSPITAL_CATALOGUE.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Katalog Fasilitas RSK",
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
