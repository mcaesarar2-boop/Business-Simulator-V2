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

data class UnivCatalogueFacility(
    val typeId: String,
    val name: String,
    val description: String,
    val constructionCost: Long,
    val category: String, // "Akademik Umum", "Tech & Eng", "Science & Med", "Art & Business", "Riset Elit", "Fasilitas Kampus"
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

val FULL_UNIV_CATALOGUE = listOf(
    // === Akademik Umum ===
    UnivCatalogueFacility("univ_smart_classroom", "Smart Classroom", "Ruang kelas interaktif dengan smart board, mikrofon multi-arah, dan kamera auto-track.", 40000L, "Akademik Umum", Icons.Default.DesktopMac),
    UnivCatalogueFacility("univ_lecture_hall", "Lecture Hall", "Ruang kuliah bergaya teater berkapasitas besar dengan akustik kelas dunia.", 70000L, "Akademik Umum", Icons.Default.Groups),
    UnivCatalogueFacility("univ_seminar_room", "Seminar Room", "Ruang rapat meja bundar kondusif untuk diskusi kelompok kecil dan kolokium.", 30000L, "Akademik Umum", Icons.Default.MeetingRoom),
    UnivCatalogueFacility("univ_active_learning", "Active Learning Classroom", "Ruang belajar kolaboratif yang didukung layar proyektor mandiri per tim.", 45000L, "Akademik Umum", Icons.Default.CoPresent),
    UnivCatalogueFacility("univ_library_utama", "Library Utama", "Perpustakaan makro penyimpan jutaan koleksi literatur fisik dan ruang baca individu.", 90000L, "Akademik Umum", Icons.Default.LibraryBooks),
    UnivCatalogueFacility("univ_digital_library", "Digital Library", "Koneksi super cepat ke database jurnal internasional terlengkap dan arsip digital.", 80000L, "Akademik Umum", Icons.Default.Laptop),
    UnivCatalogueFacility("univ_rare_book", "Rare Book Library", "Perpustakaan khusus dengan pengatur suhu udara protektif untuk naskah-naskah kuno berharga.", 110000L, "Akademik Umum", Icons.Default.MenuBook),
    UnivCatalogueFacility("univ_thesis_repository", "Thesis Repository", "Arsip fisik dan digital seluruh tesis, disertasi, dan karya ilmiah alumni.", 50000L, "Akademik Umum", Icons.Default.Book),
    UnivCatalogueFacility("univ_auditorium", "Auditorium", "Gedung pertunjukan megah untuk acara wisuda, seminar nasional, dan pidato rektor.", 15000L, "Akademik Umum", Icons.Default.TheaterComedy),
    UnivCatalogueFacility("univ_convention_hall", "Convention Hall", "Pusat eksibisi raksasa untuk pameran hasil riset tahunan dan rekrutmen kerja.", 220000L, "Akademik Umum", Icons.Default.HomeWork),

    // === Tech & Eng ===
    UnivCatalogueFacility("univ_ai_ml_lab", "AI & Machine Learning Lab", "Supercomputer mini & workstation GPU NVIDIA Tensor untuk pemodelan AI mutakhir.", 180000L, "Tech & Eng", Icons.Default.Memory),
    UnivCatalogueFacility("univ_robotics_lab", "Robotics Lab", "Lengan robotik industri, kit drone, mikrokontroler, dan lab uji navigasi otonom.", 190000L, "Tech & Eng", Icons.Default.PrecisionManufacturing),
    UnivCatalogueFacility("univ_cyber_sec_lab", "Cyber Security Lab", "Ruang simulasi serangan siber red-team vs blue-team terisolasi.", 160000L, "Tech & Eng", Icons.Default.Security),
    UnivCatalogueFacility("univ_cloud_comp_lab", "Cloud Computing Lab", "Infrastruktur server lokal untuk simulasi arsitektur cloud terdistribusi.", 150000L, "Tech & Eng", Icons.Default.CloudQueue),
    UnivCatalogueFacility("univ_quantum_comp", "Quantum Computing Lab", "Peralatan simulasi algoritma kuantum dan riset material superkonduktor.", 280000L, "Tech & Eng", Icons.Default.Science),
    UnivCatalogueFacility("univ_vr_ar_xr_lab", "VR/AR/XR Lab", "Kacamata VR/AR, motion capture suit, dan mesin rendering real-time.", 140000L, "Tech & Eng", Icons.Default.VideogameAsset),
    UnivCatalogueFacility("univ_cnc_workshop", "CNC Workshop", "Mesin bubut industri, CNC 5-axis, pemotong laser logam, dan las otomatis.", 120000L, "Tech & Eng", Icons.Default.Handyman),
    UnivCatalogueFacility("univ_wind_tunnel", "Wind Tunnel", "Lorong angin uji aerodinamika pesawat terbang dan mobil balap formula mahasiswa.", 250000L, "Tech & Eng", Icons.Default.Air),
    UnivCatalogueFacility("univ_engine_test", "Engine Test Cell", "Ruang uji dyno untuk mesin pembakaran dalam dan motor listrik masa depan.", 220000L, "Tech & Eng", Icons.Default.Settings),
    UnivCatalogueFacility("univ_smart_grid_lab", "Smart Grid Lab", "Simulasi distribusi kelistrikan kota pintar berbasis energi terbarukan.", 170000L, "Tech & Eng", Icons.Default.ElectricBolt),
    UnivCatalogueFacility("univ_concrete_lab", "Concrete Lab", "Mesin pres uji kuat tekan beton struktur sipil berat berkapasitas ratusan ton.", 110000L, "Tech & Eng", Icons.Default.Landscape),
    UnivCatalogueFacility("univ_earthquake_sim", "Earthquake Simulation Lab", "Meja getar raksasa terkendali komputer untuk mensimulasikan ketahanan gempa gedung pencakar langit.", 240000L, "Tech & Eng", Icons.Default.Tornado),
    UnivCatalogueFacility("univ_drone_testing", "Drone Testing Center", "Kandang jaring outdoor masif untuk penerbangan uji otonom UAV.", 130000L, "Tech & Eng", Icons.Default.FlightTakeoff),
    UnivCatalogueFacility("univ_flight_sim", "Flight Simulator", "Kokpit simulator penerbangan Boeing/Airbus berskala penuh untuk pelatihan penerbangan.", 310000L, "Tech & Eng", Icons.Default.Flight),

    // === Science & Med ===
    UnivCatalogueFacility("univ_nanotech_lab", "Nanotechnology Lab", "Uji sintesis nanomaterial menggunakan ruang bersih tingkat partikel super rendah.", 290000L, "Science & Med", Icons.Default.Hub),
    UnivCatalogueFacility("univ_laser_lab", "Laser Lab", "Peralatan optik laser presisi tinggi untuk riset fisika fotonika lanjutan.", 210000L, "Science & Med", Icons.Default.Bolt),
    UnivCatalogueFacility("univ_mass_spect", "Mass Spectrometry Center", "Mesin spektrometri massa presisi tinggi untuk mengidentifikasi komponen kimia kompleks.", 260000L, "Science & Med", Icons.Default.Biotech),
    UnivCatalogueFacility("univ_dna_seq", "DNA Sequencing Facility", "Sistem sekuensing DNA cepat untuk riset genetika dan mikrobiologi modern.", 240000L, "Science & Med", Icons.Default.Coronavirus),
    UnivCatalogueFacility("univ_teaching_hosp", "Teaching Hospital", "Rumah sakit pendidikan utama yang melayani masyarakat umum sekaligus pusat residen.", 750000L, "Science & Med", Icons.Default.LocalHospital),
    UnivCatalogueFacility("univ_sim_hospital", "Simulation Hospital", "Ruang simulasi medis canggih lengkap dengan manekin pasien yang dapat bernapas dan bersuara.", 380000L, "Science & Med", Icons.Default.MedicalInformation),
    UnivCatalogueFacility("univ_cadaver_lab", "Cadaver Lab", "Fasilitas anatomi medis steril dengan penyimpanan jenazah formalin yang aman.", 180000L, "Science & Med", Icons.Default.Person2),
    UnivCatalogueFacility("univ_mri_research", "MRI Research Center", "Mesin MRI riset berkapasitas tesla tinggi untuk neuroimaging dan studi fungsi otak.", 420000L, "Science & Med", Icons.Default.SettingsSuggest),

    // === Art & Business ===
    UnivCatalogueFacility("univ_film_tv_studio", "Film & TV Studio", "Studio rekaman berlayar hijau besar dengan pencahayaan panggung bioskop.", 160000L, "Art & Business", Icons.Default.Movie),
    UnivCatalogueFacility("univ_virtual_prod", "Virtual Production Studio", "Layar LED Volume raksasa terintegrasi Unreal Engine untuk syuting efek visual real-time.", 350000L, "Art & Business", Icons.Default.Tv),
    UnivCatalogueFacility("univ_audio_rec", "Audio Recording Studio", "Ruang isolasi suara, mixer konsol analog Neve, dan mikrofon studio legendaris.", 120000L, "Art & Business", Icons.Default.Mic),
    UnivCatalogueFacility("univ_dolby_atmos", "Dolby Atmos Room", "Studio tata suara surround bersertifikasi Dolby untuk pasca-produksi audio film.", 150000L, "Art & Business", Icons.Default.VolumeUp),
    UnivCatalogueFacility("univ_concert_hall", "Concert Hall", "Aula simfoni berdesain akustik kayu mahoni mewah dilengkapi organ pipa besar.", 280000L, "Art & Business", Icons.Default.MusicNote),
    UnivCatalogueFacility("univ_bloomberg_room", "Bloomberg Trading Room", "Terminal Bloomberg finansial real-time aktif untuk simulasi transaksi saham wall street.", 190000L, "Art & Business", Icons.Default.TrendingUp),
    UnivCatalogueFacility("univ_startup_inc", "Startup Incubator", "Ruang kerja kolaboratif bebas biaya dengan pendampingan modal ventura.", 140000L, "Art & Business", Icons.Default.Lightbulb),
    UnivCatalogueFacility("univ_3d_printing", "3D Printing & Laser Lab", "Puluhan printer 3D FDM/SLA presisi tinggi untuk pembuatan purwarupa bisnis.", 110000L, "Art & Business", Icons.Default.Interests),
    UnivCatalogueFacility("univ_fine_dining", "Fine Dining Restaurant", "Restoran percontohan praktikum jurusan perhotelan dan manajemen kuliner.", 130000L, "Art & Business", Icons.Default.Restaurant),

    // === Riset Elit ===
    UnivCatalogueFacility("univ_supercomputer", "Supercomputer Center", "Gugusan superkomputer teraflop tinggi untuk simulasi cuaca dan dinamika molekuler.", 650000L, "Riset Elit", Icons.Default.DeveloperBoard),
    UnivCatalogueFacility("univ_clean_room", "Clean Room (Semiconductor)", "Laboratorium kelas 10 ISO steril bebas debu untuk pabrikasi chip semikonduktor mikro.", 550000L, "Riset Elit", Icons.Default.AdfScanner),
    UnivCatalogueFacility("univ_nanofab_center", "Nanofabrication Center", "Sistem lithografi berkas elektron mutakhir untuk merancang sirkuit nano.", 580000L, "Riset Elit", Icons.Default.Hub),
    UnivCatalogueFacility("univ_electron_micro", "Electron Microscope Center", "Koleksi mikroskop elektron transmisi (TEM) dan pemindaian (SEM) berkekuatan jutaan kali pembesaran.", 320000L, "Riset Elit", Icons.Default.Search),
    UnivCatalogueFacility("univ_science_park", "Science Park", "Kompleks industri-akademik masif yang memfasilitasi riset komersial bersama korporasi multinasional.", 800000L, "Riset Elit", Icons.Default.LocationCity),

    // === Fasilitas Kampus ===
    UnivCatalogueFacility("univ_dormitory", "Dormitory", "Asrama modern mahasiswa terintegrasi dengan akses kartu pintar, binatu, dan ruang sosial.", 180000L, "Fasilitas Kampus", Icons.Default.Bed),
    UnivCatalogueFacility("univ_student_center", "Student Center", "Gedung pusat bimbingan konseling mahasiswa, kafetaria, dan sekretariat Unit Kegiatan Mahasiswa (UKM).", 150000L, "Fasilitas Kampus", Icons.Default.SportsEsports),
    UnivCatalogueFacility("univ_olympic_pool", "Olympic Swimming Pool", "Kolam renang berukuran olimpiade standar internasional lengkap dengan pemanas air otomatis.", 220000L, "Fasilitas Kampus", Icons.Default.Pool),
    UnivCatalogueFacility("univ_indoor_stadium", "Indoor Stadium", "Stadion olahraga dalam ruangan serbaguna berkapasitas ribuan penonton.", 320000L, "Fasilitas Kampus", Icons.Default.FitnessCenter),
    UnivCatalogueFacility("univ_athletics_track", "Athletics Track", "Lintasan lari sintetis berstandar IAAF mengelilingi lapangan sepak bola utama.", 190000L, "Fasilitas Kampus", Icons.Default.DirectionsRun),
    UnivCatalogueFacility("univ_coworking", "Co-working Space", "Area belajar bersama berkonsep kafe estetis yang didukung colokan melimpah.", 80000L, "Fasilitas Kampus", Icons.Default.Chair),
    UnivCatalogueFacility("univ_makerspace", "Makerspace", "Bengkel kreatif bersama berisi perkakas kayu, logam, solder, dan komponen elektronik.", 110000L, "Fasilitas Kampus", Icons.Default.Construction)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversityFacilityCatalogueScreen(
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
            modifier = Modifier.fillMaxSize().background(Color(0xFF0D0A07)),
            contentAlignment = Alignment.Center
        ) {
            Text("Data tidak ditemukan.", color = Color.White)
        }
        return
    }

    var selectedCategory by remember { mutableStateOf("Akademik Umum") }
    val categories = listOf("Akademik Umum", "Tech & Eng", "Science & Med", "Art & Business", "Riset Elit", "Fasilitas Kampus")

    var selectedItemForBuild by remember { mutableStateOf<UnivCatalogueFacility?>(null) }
    var showGradeDialog by remember { mutableStateOf(false) }

    val filteredList = FULL_UNIV_CATALOGUE.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Katalog Konstruksi Universitas",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = institution.name,
                            color = Color(0xFFD4AF37),
                            fontSize = 11.sp,
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E170F))
            )
        },
        containerColor = Color(0xFF0D0A07)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Kategori
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = Color(0xFF1E170F),
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
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }

            // Info Dana Abadi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1B150E))
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B150E)),
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
                                    .background(Color(0xFF231D15), RoundedCornerShape(8.dp)),
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
                                        text = "Biaya Konstruksi: ${formatCurrency(item.constructionCost)}",
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
                                    color = Color(0xFF0D0A07),
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
        UniversityGradeSelectionDialog(
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
fun UniversityGradeSelectionDialog(
    item: UnivCatalogueFacility,
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E170F))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Pilih Kualitas Gedung Kampus",
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

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))

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
                                containerColor = if (isSelected) Color(0xFF2E2417) else Color(0xFF1B150E)
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

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))

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
                            Toast.makeText(context, "Fasilitas riset berhasil didirikan!", Toast.LENGTH_SHORT).show()
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
