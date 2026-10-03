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



data class CatalogueFacility(
    val typeId: String,
    val name: String,
    val description: String,
    val constructionCost: Long,
    val category: String, // "Dasar", "Menengah", "Tinggi", "Internasional"
    val icon: ImageVector,
    val baseBuildTime: Int = when {
        typeId.contains("supercomputer") || typeId.contains("research") -> 8
        typeId.contains("lecture") || typeId.contains("hall") || typeId.contains("hospital") || typeId.contains("stadium") -> 3
        typeId.contains("lab") || typeId.contains("computer") || typeId.contains("playroom") || typeId.contains("gym") || typeId.contains("library") || typeId.contains("pool") -> 2
        category == "Internasional" || category == "Riset Elit" -> 3
        category == "Tinggi" || category == "Science & Med" || category == "Tech & Eng" -> 2
        else -> 1
    }
)

val FULL_KINDERGARTEN_CATALOGUE = listOf(
    // === FASILITAS DASAR ===
    CatalogueFacility("dasar_ruang_kelas", "Ruang Kelas TK", "Ruang utama kegiatan belajar mengajar interaktif.", 15000L, "Dasar", Icons.Default.School),
    CatalogueFacility("dasar_whiteboard", "Whiteboard & Board", "Media tulis & visual interaktif untuk presentasi guru.", 5000L, "Dasar", Icons.Default.BorderColor),
    CatalogueFacility("dasar_area_membaca", "Area Membaca Cerita", "Pojok membaca dengan buku bergambar yang berwarna.", 8000L, "Dasar", Icons.Default.Book),
    CatalogueFacility("dasar_toilet_anak", "Toilet Anak Higienis", "Sanitasi ramah anak dengan ukuran ergonomis.", 12000L, "Dasar", Icons.Default.Wc),
    CatalogueFacility("dasar_cuci_tangan", "Tempat Cuci Tangan", "Membiasakan cuci tangan demi kesehatan balita.", 6000L, "Dasar", Icons.Default.Wash),
    CatalogueFacility("dasar_kantor_guru", "Kantor Guru & Staf", "Ruang koordinasi, administrasi, dan bimbingan guru.", 18000L, "Dasar", Icons.Default.CorporateFare),
    CatalogueFacility("dasar_gudang", "Gudang Logistik", "Penyimpanan alat edukasi, mainan, & inventaris.", 7000L, "Dasar", Icons.Default.Home),
    CatalogueFacility("dasar_halaman_bermain", "Halaman Bermain Luar", "Area terbuka hijau untuk eksplorasi fisik kasar.", 20000L, "Dasar", Icons.Default.NaturePeople),
    CatalogueFacility("dasar_ayunan", "Ayunan Gembira", "Meningkatkan keseimbangan & motorik balita.", 10000L, "Dasar", Icons.Default.Toys),
    CatalogueFacility("dasar_perosotan", "Perosotan Ceria", "Mainan favorit yang melatih keberanian anak.", 10000L, "Dasar", Icons.Default.ArrowDownward),
    CatalogueFacility("dasar_jungkat_jungkit", "Jungkat-jungkit Anak", "Melatih kerjasama tim & keseimbangan.", 10000L, "Dasar", Icons.Default.CompareArrows),
    CatalogueFacility("dasar_area_upacara", "Area Upacara Bendera", "Menanamkan jiwa nasionalisme sejak dini.", 14000L, "Dasar", Icons.Default.Flag),
    CatalogueFacility("dasar_tempat_parkir", "Tempat Parkir Aman", "Area drop-off anak yang tertata dan terlindungi.", 12000L, "Dasar", Icons.Default.LocalParking),
    CatalogueFacility("dasar_mushola", "Mushola Sekolah", "Pendidikan karakter keagamaan & ibadah bersama.", 15000L, "Dasar", Icons.Default.Place),

    // === FASILITAS MENENGAH ===
    CatalogueFacility("menengah_indoor_playroom", "Indoor Playroom", "Pusat aktivitas bermain dalam ruangan saat hujan.", 45000L, "Menengah", Icons.Default.SportsBasketball),
    CatalogueFacility("menengah_art_room", "Art & Craft Room", "Mengasah kreativitas menggambar dan mewarnai.", 40000L, "Menengah", Icons.Default.Brush),
    CatalogueFacility("menengah_music_room", "Music & Rhythm Room", "Mengenalkan ketukan, alat perkusi, & bernyanyi.", 50000L, "Menengah", Icons.Default.MusicNote),
    CatalogueFacility("menengah_library", "Perpustakaan Mini", "Koleksi lengkap dongeng Nusantara & dunia.", 35000L, "Menengah", Icons.Default.LibraryBooks),
    CatalogueFacility("menengah_computer_corner", "Computer Corner", "Pengenalan teknologi dasar ramah anak.", 60000L, "Menengah", Icons.Default.Computer),
    CatalogueFacility("menengah_science_corner", "Science Corner", "Eksperimen sederhana menguak rahasia alam.", 40000L, "Menengah", Icons.Default.Science),
    CatalogueFacility("menengah_outdoor_playground", "Outdoor Playground", "Wahana bermain outbound lengkap dengan Flying Fox.", 65000L, "Menengah", Icons.Default.Nature),
    CatalogueFacility("menengah_mini_garden", "Mini Garden & Farm", "Belajar menanam bunga & menghargai bumi.", 30000L, "Menengah", Icons.Default.Eco),
    CatalogueFacility("menengah_sand_pit", "Sand Pit (Bak Pasir)", "Sangat baik untuk eksplorasi sensorik motorik.", 25000L, "Menengah", Icons.Default.Grain),
    CatalogueFacility("menengah_water_play", "Water Play Area", "Bermain air interaktif yang melatih sensorik.", 35000L, "Menengah", Icons.Default.Water),

    // === FASILITAS TINGGI ===
    CatalogueFacility("tinggi_multipurpose", "Multipurpose Hall", "Aula serbaguna untuk pentas seni & rapat wali murid.", 120000L, "Tinggi", Icons.Default.MeetingRoom),
    CatalogueFacility("tinggi_indoor_gym", "Indoor Kids Gym", "Peralatan senam & kelenturan fisik yang aman.", 110000L, "Tinggi", Icons.Default.FitnessCenter),
    CatalogueFacility("tinggi_sensory", "Sensory Room", "Ruang stimulasi sensorik khusus anak berkebutuhan khusus.", 130000L, "Tinggi", Icons.Default.RemoveRedEye),
    CatalogueFacility("tinggi_stem", "STEM Discovery Lab", "Menstimulasi logika sains & matematika dasar.", 150000L, "Tinggi", Icons.Default.Psychology),
    CatalogueFacility("tinggi_cooking", "Cooking Class Room", "Melatih kemandirian & kreativitas resep praktis.", 100000L, "Tinggi", Icons.Default.LocalPizza),
    CatalogueFacility("tinggi_drama", "Drama & Roleplay Room", "Panggung peran (dokter, kasir) melatih sosial emosional.", 115000L, "Tinggi", Icons.Default.TheaterComedy),
    CatalogueFacility("tinggi_language", "Language Lab Room", "Fokus penguasaan bahasa asing (English/Mandarin).", 140000L, "Tinggi", Icons.Default.Translate),
    CatalogueFacility("tinggi_nap", "Exquisite Nap Room", "Kamar tidur siang higienis untuk optimalkan kognitif.", 90000L, "Tinggi", Icons.Default.Bed),
    CatalogueFacility("tinggi_clinic", "Klinik / UKS Anak", "Penanganan medis darurat didukung dokter anak berkala.", 80000L, "Tinggi", Icons.Default.LocalHospital),
    CatalogueFacility("tinggi_parent_lounge", "Parent Lounge", "Ruang tunggu nyaman bagi wali murid berkelas.", 95000L, "Tinggi", Icons.Default.People),
    CatalogueFacility("tinggi_counseling", "Counseling & Psikolog", "Konsultasi tumbuh kembang bersama ahli psikologi.", 110000L, "Tinggi", Icons.Default.QuestionAnswer),
    CatalogueFacility("tinggi_resource_room", "Teacher Resource Room", "Pusat pengembangan materi & riset pengajaran guru.", 100000L, "Tinggi", Icons.Default.FolderShared),

    // === FASILITAS INTERNASIONAL ===
    CatalogueFacility("inter_discovery", "Discovery Lab", "Lab eksplorasi tingkat lanjut dengan teknologi mutakhir.", 280000L, "Internasional", Icons.Default.Search),
    CatalogueFacility("inter_makerspace", "Makerspace Studio", "Merancang proyek kerajinan, seni & teknik kayu anak.", 300000L, "Internasional", Icons.Default.Build),
    CatalogueFacility("inter_greenhouse", "Greenhouse Edu", "Eksperimen botani eksotis di bawah kubah kaca.", 260000L, "Internasional", Icons.Default.Yard),
    CatalogueFacility("inter_hydroponic", "Hydroponic Garden", "Sistem berkebun vertikal otomatis bernilai sains.", 250000L, "Internasional", Icons.Default.Grass),
    CatalogueFacility("inter_outdoor_class", "Outdoor Eco Classroom", "Belajar di alam terbuka untuk memacu imajinasi bebas.", 270000L, "Internasional", Icons.Default.OutdoorGrill),
    CatalogueFacility("inter_forest", "Forest Playground", "Wahana petualangan hutan buatan ramah lingkungan.", 350000L, "Internasional", Icons.Default.Terrain),
    CatalogueFacility("inter_bike_track", "Bike Track & Sirkuit", "Melatih koordinasi motorik sepeda roda tiga.", 290000L, "Internasional", Icons.Default.DirectionsBike),
    CatalogueFacility("inter_traffic_park", "Traffic Education Park", "Simulasi lalu lintas mini melatih kepatuhan hukum.", 310000L, "Internasional", Icons.Default.Traffic),
    CatalogueFacility("inter_supermarket", "Mini Supermarket Store", "Wahana bermain peran matematika belanja & kasir.", 260000L, "Internasional", Icons.Default.Store),
    CatalogueFacility("inter_hospital", "Mini Hospital Care", "Mengenalkan ilmu kesehatan & pertolongan pertama.", 270000L, "Internasional", Icons.Default.MedicalServices),
    CatalogueFacility("inter_fire", "Mini Fire Station", "Melatih ketangkasan & simulasi pemadam kebakaran.", 280000L, "Internasional", Icons.Default.FireTruck),
    CatalogueFacility("inter_airport", "Mini Airport Gate", "Eksplorasi geografi, paspor & perjalanan udara.", 320000L, "Internasional", Icons.Default.FlightTakeoff),
    CatalogueFacility("inter_kitchen", "Pro Innovation Kitchen", "Dapur standar koki cilik untuk rekayasa kuliner.", 290000L, "Internasional", Icons.Default.SoupKitchen),
    CatalogueFacility("inter_innovation", "AI Innovation Lab", "Pengenalan koding balita, robotika & augmented reality.", 450000L, "Internasional", Icons.Default.SettingsAccessibility),
    CatalogueFacility("inter_pool", "Olympic Kids Pool", "Kolam renang air hangat dengan sistem filtrasi ozon.", 380000L, "Internasional", Icons.Default.Pool),
    CatalogueFacility("inter_amphitheater", "Amphitheater Terbuka", "Panggung melingkar luar ruangan untuk pertunjukan kolosal.", 330000L, "Internasional", Icons.Default.Audiotrack),
    CatalogueFacility("inter_auditorium", "Indoor Auditorium", "Gedung teater megah dengan sistem tata suara terbaik.", 500000L, "Internasional", Icons.Default.SpeakerGroup),
    CatalogueFacility("inter_recording", "Recording & Podcast", "Mengembangkan bakat suara, bercerita & rekaman audio.", 260000L, "Internasional", Icons.Default.Mic),
    CatalogueFacility("inter_dance", "Dance & Ballet Studio", "Lantai kayu khusus didesain aman bagi persendian anak.", 280000L, "Internasional", Icons.Default.DirectionsRun),
    CatalogueFacility("inter_theater", "Black Box Theater", "Eksplorasi seni peran & tata lampu panggung modern.", 350000L, "Internasional", Icons.Default.TheaterComedy),
    CatalogueFacility("inter_trail", "Nature Trail Area", "Eksplorasi hayati, serangga, tanaman & batuan sungai.", 270000L, "Internasional", Icons.Default.Map),
    CatalogueFacility("inter_zoo", "Petting Zoo Mini", "Interaksi edukatif dengan hewan peliharaan jinak.", 400000L, "Internasional", Icons.Default.Pets)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KindergartenFacilityCatalogueScreen(
    navController: NavHostController,
    viewModel: GameViewModel,
    foundationId: String,
    institutionId: String
) {
    val playerState by viewModel.playerState.collectAsState()
    val foundation = playerState.foundations.find { it.id == foundationId }
    val institution = foundation?.educationInstitutions?.find { it.id == institutionId }

    if (foundation == null || institution == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0B121E)),
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

    val filteredList = FULL_KINDERGARTEN_CATALOGUE.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Katalog Konstruksi Fasilitas",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Text(
                            text = institution.name,
                            color = Color(0xFFD4AF37),
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F1E36))
            )
        },
        containerColor = Color(0xFF0B121E)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Kategori
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = Color(0xFF0F1E36),
                contentColor = Color(0xFFD4AF37),
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
                                color = if (isSelected) Color(0xFFD4AF37) else Color.Gray,
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
                    .background(Color(0xFF14223A))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Dana Abadi Yayasan",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                    Text(
                        formatCurrency(foundation.endowmentFund),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFD4AF37).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Fasilitas Saat Ini: ${institution.additionalFacilities?.size ?: 0}",
                        color = Color(0xFFD4AF37),
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14223A)),
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
                                    .background(Color(0xFF101B2B), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = Color(0xFFD4AF37),
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
                                        text = "Biaya: ${formatCurrency(item.constructionCost)}",
                                        color = Color(0xFF10B981),
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    "Bangun",
                                    fontSize = 12.sp,
                                    color = Color(0xFF0B121E),
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
        GradeSelectionDialog(
            item = item,
            endowmentFund = foundation.endowmentFund,
            onDismiss = {
                showGradeDialog = false
                selectedItemForBuild = null
            },
            onConfirm = { grade, customName, buildTime ->
                val success = viewModel.buildAdditionalFacility(
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

private fun getGradeBuildTimeModifier(gradeName: String): Int {
    return when {
        gradeName == "Grade SS" -> 12
        gradeName.startsWith("Grade S") -> {
            val suffix = gradeName.removePrefix("Grade S")
            val num = suffix.toIntOrNull()
            if (num != null) {
                if (num >= 7) 8
                else if (num >= 4) 5
                else 3
            } else {
                3
            }
        }
        else -> 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeSelectionDialog(
    item: CatalogueFacility,
    endowmentFund: Long,
    onDismiss: () -> Unit,
    onConfirm: (BuildingGrade, String, Int) -> Unit
) {
    val context = LocalContext.current
    var selectedGrade by remember { mutableStateOf(BUILDING_GRADES.first()) }
    var customName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Pilih Kualitas Gedung (Grade)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "Fasilitas: ${item.name}",
                    fontSize = 12.sp,
                    color = Color(0xFFD4AF37),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Konstruksi: ${formatCurrency(item.constructionCost)} (Dipotong dari Kas Dana Abadi)",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Beri Nama Fasilitas Ini (Opsional)", color = Color.LightGray) },
                    placeholder = { Text(item.name, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD4AF37),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                    )
                )

                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(BUILDING_GRADES) { grade ->
                        val isSelected = selectedGrade.name == grade.name
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedGrade = grade }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1B2C3F) else Color(0xFF14223A)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = grade.name,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFFD4AF37) else Color.White,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${formatCurrency(grade.baseMaintenanceCost)}/bln",
                                            color = Color(0xFF10B981),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = grade.description,
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Kas Dana Abadi:", color = Color.Gray, fontSize = 10.sp)
                        Text(formatCurrency(endowmentFund), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    val totalMonths = item.baseBuildTime + getGradeBuildTimeModifier(selectedGrade.name)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Waktu Konstruksi:", color = Color.Gray, fontSize = 10.sp)
                        Text("$totalMonths Bulan", color = Color(0xFFFFB300), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Perawatan/bln:", color = Color.Gray, fontSize = 10.sp)
                        Text("${formatCurrency(selectedGrade.baseMaintenanceCost)}/bln", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (endowmentFund < item.constructionCost) {
                                Toast.makeText(context, "Kas Dana Abadi Yayasan tidak mencukupi!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val finalBuildTime = item.baseBuildTime + getGradeBuildTimeModifier(selectedGrade.name)
                            onConfirm(selectedGrade, customName, finalBuildTime)
                            Toast.makeText(context, "Fasilitas berhasil didirikan!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Bangun & Sah-kan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
