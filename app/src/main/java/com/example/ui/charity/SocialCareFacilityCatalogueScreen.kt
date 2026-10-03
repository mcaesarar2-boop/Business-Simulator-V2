package com.example.ui.charity

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
import androidx.navigation.NavHostController
import com.example.data.*
import com.example.viewmodel.GameViewModel

val SOCIAL_CARE_CATALOGUE = listOf(
    // Dasar
    CharityCatalogueFacility("sc_ruang_asuhan", "Ruang Asuhan Standar", "Ruang istirahat anak/lansia yang bersih, nyaman, dan ramah lingkungan.", 9000L, "Dasar", Icons.Default.Bed),
    CharityCatalogueFacility("sc_ruang_makan", "Ruang Makan Bersama", "Area berkumpul untuk makan pagi, siang, dan malam bersama pengurus panti.", 6000L, "Dasar", Icons.Default.Restaurant),
    CharityCatalogueFacility("sc_ruang_konseling", "Ruang Konseling Dasar", "Ruang tenang privat untuk pendampingan kesehatan mental dan curhat warga asuh.", 8000L, "Dasar", Icons.Default.Forum),
    CharityCatalogueFacility("sc_toilet_difabel", "Toilet Khusus Difabel", "Sanitasi ramah kursi roda dengan besi penyangga keselamatan.", 5000L, "Dasar", Icons.Default.Wc),
    CharityCatalogueFacility("sc_taman_mini", "Taman Terapi Mini", "Area hijau kecil luar ruangan untuk terapi relaksasi pikiran dan gerak.", 6000L, "Dasar", Icons.Default.Yard),
    CharityCatalogueFacility("sc_kelas_kreatif", "Ruang Kelas Kreativitas", "Tempat melatih prakarya, membaca, dan menggambar bagi warga asuh panti.", 7000L, "Dasar", Icons.Default.Palette),

    // Menengah
    CharityCatalogueFacility("sc_klinik_fisioterapi", "Klinik Fisioterapi Rehab", "Alat terapi fisik untuk melatih saraf motorik lansia dan warga asuh difabel.", 28000L, "Menengah", Icons.Default.Elderly),
    CharityCatalogueFacility("sc_ambulans_lansia", "Ambulans Lansia", "Armada medis khusus lansia yang nyaman untuk kontrol kesehatan ke rumah sakit.", 32000L, "Menengah", Icons.Default.AirportShuttle),
    CharityCatalogueFacility("sc_asrama_bertingkat", "Gedung Asrama Bertingkat", "Gedung asrama bertingkat dengan sirkulasi udara optimal penampungan masal.", 45000L, "Menengah", Icons.Default.Apartment),
    CharityCatalogueFacility("sc_workshop_kerajinan", "Workshop Kerajinan Tangan", "Pelatihan pembuatan keset kaki, lilin aroma, dan tas anyaman bernilai jual.", 24000L, "Menengah", Icons.Default.Handyman),
    CharityCatalogueFacility("sc_braille_library", "Perpustakaan Braille & Audio", "Koleksi buku braille serta sarana audio-book untuk warga asuh tunanetra.", 22000L, "Menengah", Icons.Default.MenuBook),
    CharityCatalogueFacility("sc_masjid_spiritual", "Masjid Terapi Spiritual", "Pusat ibadah spiritual tenang untuk bimbingan ketenangan batin.", 35000L, "Menengah", Icons.Default.Place),

    // Tinggi
    CharityCatalogueFacility("sc_lab_prostetik", "Lab Prostetik Kaki Palsu", "Lab produksi kaki dan tangan palsu kustom gratis bagi difabel prasejahtera.", 95000L, "Tinggi", Icons.Default.Build),
    CharityCatalogueFacility("sc_gym_rehab", "Gym Rehabilitasi Ortopedi", "Ruang kebugaran medis dengan instruktur ahli melatih kekuatan otot tubuh.", 90000L, "Tinggi", Icons.Default.FitnessCenter),
    CharityCatalogueFacility("sc_pusat_latih_difabel", "Pusat Pelatihan Kerja Difabel", "Gedung kejuruan desain grafis, entri data, dan barista ramah disabilitas.", 110000L, "Tinggi", Icons.Default.Laptop),
    CharityCatalogueFacility("sc_terapi_wicara", "Klinik Terapi Wicara & Autisme", "Ruangan sensorik khusus penanganan autisme dan terapi wicara anak panti.", 85000L, "Tinggi", Icons.Default.InterpreterMode),
    CharityCatalogueFacility("sc_kolam_hidroterapi", "Kolam Terapi Hidroterapi", "Kolam air hangat penunjang terapi pemulihan kekuatan saraf tulang belakang.", 105000L, "Tinggi", Icons.Default.Pool),
    CharityCatalogueFacility("sc_podcast_sosial", "Ruang Podcast Aspirasi Sosial", "Sarana siaran, berbagi cerita warga asuh, dan kampanye kepedulian publik.", 75000L, "Tinggi", Icons.Default.Mic),

    // Internasional
    CharityCatalogueFacility("sc_riset_inklusi", "Pusat Riset Inklusi Global", "Gedung pusat riset penemuan metode terapi disabilitas kerja sama universitas global.", 260000L, "Internasional", Icons.Default.CorporateFare),
    CharityCatalogueFacility("sc_apartemen_lansia", "Apartemen Lansia Modular (Smart)", "Apartemen lansia modern ramah sensor pintar deteksi jatuh darurat.", 240000L, "Internasional", Icons.Default.HomeWork),
    CharityCatalogueFacility("sc_robotik_sensor", "Lab Robotik Sensorik Motorik", "Alat terapi saraf canggih bertenaga robotik luar negeri melatih kelumpuhan.", 340000L, "Internasional", Icons.Default.SettingsAccessibility),
    CharityCatalogueFacility("sc_kitchen_pro", "Kitchen Workshop Profesional", "Workshop tata boga berstandar sertifikasi industri koki hotel berbintang.", 280000L, "Internasional", Icons.Default.SoupKitchen),
    CharityCatalogueFacility("sc_smarthome_ai", "Smart Home Difabel berbasis AI", "Kamar panti berbasis asisten AI yang merespons perintah suara tunadaksa.", 300000L, "Internasional", Icons.Default.Computer),
    CharityCatalogueFacility("sc_eco_botanical", "Eco-Therapy Botanical Garden", "Kebun botani luas khusus terapi penyembuhan depresi warga asuh panti.", 250000L, "Internasional", Icons.Default.Grass)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialCareFacilityCatalogueScreen(
    navController: NavHostController,
    viewModel: GameViewModel,
    foundationId: String,
    institutionId: String
) {
    val playerState by viewModel.playerState.collectAsState()
    val foundation = playerState.foundations.find { it.id == foundationId }
    val institution = foundation?.charityInstitutions?.find { it.id == institutionId }

    if (foundation == null || institution == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0F0B09)),
            contentAlignment = Alignment.Center
        ) {
            Text("Data tidak ditemukan.", color = Color.White)
        }
        return
    }

    var selectedCategory by remember { mutableStateOf("Dasar") }
    val categories = listOf("Dasar", "Menengah", "Tinggi", "Internasional")

    var selectedItemForBuild by remember { mutableStateOf<CharityCatalogueFacility?>(null) }
    var showGradeDialog by remember { mutableStateOf(false) }

    val filteredList = SOCIAL_CARE_CATALOGUE.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Katalog Konstruksi Panti",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Text(
                            text = institution.name,
                            color = Color(0xFFF4A261),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1410))
            )
        },
        containerColor = Color(0xFF0F0B09)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Kategori
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = Color(0xFF1E1410),
                contentColor = Color(0xFFF4A261),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        text = {
                            Text(
                                text = cat,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFF4A261) else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Info Dana Abadi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E1612))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Dana Abadi Rehabilitasi Sosial",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    Text(
                        com.example.ui.formatCurrency(foundation.endowmentFund),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFF4A261).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFF4A261).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Fasilitas Saat Ini: ${institution.additionalFacilities?.size ?: 0}",
                        color = Color(0xFFF4A261),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(filteredList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1310)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF2E1C16), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = Color(0xFFF4A261),
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Biaya Konstruksi: $${com.example.ui.formatCurrencyRingkas(item.constructionCost, false)}",
                                        color = Color(0xFF81C784),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    selectedItemForBuild = item
                                    showGradeDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4A261)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    "Bangun",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1E1410),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGradeDialog && selectedItemForBuild != null) {
        val item = selectedItemForBuild!!
        CharityGradeSelectionDialog(
            item = item,
            endowmentFund = foundation.endowmentFund,
            onDismiss = {
                showGradeDialog = false
                selectedItemForBuild = null
            },
            onConfirm = { grade, customName, buildTime ->
                val success = viewModel.addCharityFacilityItem(
                    foundationId = foundationId,
                    institutionId = institutionId,
                    typeId = item.typeId,
                    name = item.name,
                    customName = customName,
                    gradeId = grade.name,
                    maintenanceCost = grade.baseMaintenanceCost,
                    constructionCost = item.constructionCost,
                    constructionTotalMonths = buildTime,
                    constructionLeftMonths = buildTime
                )
                if (success) {
                    showGradeDialog = false
                    selectedItemForBuild = null
                }
            }
        )
    }
}
