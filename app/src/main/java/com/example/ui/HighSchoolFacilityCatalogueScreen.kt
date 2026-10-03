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

// Re-declare local CatalogueFacility inside this separate file to avoid conflicts
data class HighSchoolCatalogueFacility(
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

val FULL_HIGH_SCHOOL_CATALOGUE = listOf(
    // === FASILITAS DASAR ===
    HighSchoolCatalogueFacility("sma_ruang_kelas", "Ruang Kelas SMA", "Ruang kelas fungsional lengkap dengan penyejuk udara dan proyektor presentasi.", 28000L, "Dasar", Icons.Default.School),
    HighSchoolCatalogueFacility("sma_perpustakaan_utama", "Perpustakaan Utama", "Koleksi buku kurikulum lengkap, jurnal ilmiah, dan sudut baca tenang.", 20000L, "Dasar", Icons.Default.LibraryBooks),
    HighSchoolCatalogueFacility("sma_uks_sma", "UKS SMA", "Layanan pertolongan pertama terstandarisasi dengan obat komplit dan petugas siaga.", 15000L, "Dasar", Icons.Default.LocalHospital),
    HighSchoolCatalogueFacility("sma_kantin_luas", "Kantin Luas", "Kantin semi-outdoor higienis menyajikan hidangan sehat bagi para siswa.", 22000L, "Dasar", Icons.Default.Restaurant),
    HighSchoolCatalogueFacility("sma_lapangan_upacara", "Lapangan Upacara", "Lapangan serbaguna untuk upacara formal, apel pagi, dan apel kedisplinan.", 25000L, "Dasar", Icons.Default.Flag),
    HighSchoolCatalogueFacility("sma_toilet_siswa", "Toilet Siswa", "Sanitasi bersih terpisah pria/wanita dengan sistem pembuangan higienis.", 16000L, "Dasar", Icons.Default.Wc),
    HighSchoolCatalogueFacility("sma_ruang_osis", "Ruang OSIS", "Pusat kegiatan ekstrakurikuler, rapat kepengurusan OSIS, dan organisasi.", 18000L, "Dasar", Icons.Default.Group),
    HighSchoolCatalogueFacility("sma_ruang_guru", "Ruang Guru", "Ruang koordinasi pendidik, bimbingan konseling dasar, dan administrasi.", 24000L, "Dasar", Icons.Default.CorporateFare),

    // === FASILITAS MENENGAH ===
    HighSchoolCatalogueFacility("sma_lab_komputer", "Lab Komputer", "Fasilitas PC berspesifikasi tinggi untuk pemrograman dasar dan pengolahan data.", 65000L, "Menengah", Icons.Default.Computer),
    HighSchoolCatalogueFacility("sma_lab_kimia", "Lab Kimia", "Dilengkapi reagen kimia, wastafel laboratorium, dan lemari asam keamanan.", 60000L, "Menengah", Icons.Default.Science),
    HighSchoolCatalogueFacility("sma_lab_fisika", "Lab Fisika", "Peralatan mekanika, kelistrikan, optik, dan kit percobaan fisika tingkat lanjut.", 58000L, "Menengah", Icons.Default.Settings),
    HighSchoolCatalogueFacility("sma_lab_biologi", "Lab Biologi", "Mikroskop binokuler elektrik, preparat awetan, dan torso anatomi lengkap.", 55000L, "Menengah", Icons.Default.Coronavirus),
    HighSchoolCatalogueFacility("sma_lapangan_basket_voli", "Lapangan Basket/Voli", "Lapangan olahraga semen keras berstandar nasional dengan ring hidrolik.", 50000L, "Menengah", Icons.Default.SportsBasketball),
    HighSchoolCatalogueFacility("sma_studio_seni", "Studio Seni", "Ruang melukis, seni rupa kriya, studio patung, dan galeri pameran mini.", 42000L, "Menengah", Icons.Default.Brush),
    HighSchoolCatalogueFacility("sma_ruang_musik_band", "Ruang Musik / Band", "Dilengkapi set drum, keyboard, gitar listrik, bass, dan peredam suara ruangan.", 48000L, "Menengah", Icons.Default.MusicNote),
    HighSchoolCatalogueFacility("sma_koperasi", "Koperasi Sekolah", "Koperasi komplit menjual seragam, atribut, buku paket, dan atribut pramuka.", 26000L, "Menengah", Icons.Default.Store),

    // === FASILITAS TINGGI ===
    HighSchoolCatalogueFacility("sma_auditorium_grand", "Auditorium Grand", "Gedung pertemuan bertingkat untuk wisuda, pementasan teater, dan seminar.", 190000L, "Tinggi", Icons.Default.MeetingRoom),
    HighSchoolCatalogueFacility("sma_lab_bahasa", "Lab Bahasa", "Sistem audio nirkabel modern untuk mendengarkan, melatih vokal, dan ujian TOEFL/IELTS.", 125000L, "Tinggi", Icons.Default.Translate),
    HighSchoolCatalogueFacility("sma_perpustakaan_digital_lounge", "Perpustakaan Digital & Lounge", "Dilengkapi komputer akses e-journal, tablet baca, dan sofa santai belajar.", 130000L, "Tinggi", Icons.Default.MenuBook),
    HighSchoolCatalogueFacility("sma_lapangan_sepak_bola", "Lapangan Sepak Bola", "Lapangan berumput alami berukuran penuh dengan sistem drainase bawah tanah.", 160000L, "Tinggi", Icons.Default.SportsKabaddi),
    HighSchoolCatalogueFacility("sma_kolam_renang", "Kolam Renang Siswa", "Kolam renang ukuran olympic mini dengan penjaga pantai dan saringan modern.", 170000L, "Tinggi", Icons.Default.Pool),
    HighSchoolCatalogueFacility("sma_ruang_podcast", "Ruang Podcast / Broadcasting", "Peralatan mikrofon kondensor profesional, mixer audio, dan studio rekaman kedap.", 115000L, "Tinggi", Icons.Default.Mic),

    // === FASILITAS INTERNASIONAL ===
    HighSchoolCatalogueFacility("sma_career_counseling_center", "Career Counseling Center", "Layanan konseling persiapan universitas top dunia dan bimbingan karir.", 260000L, "Internasional", Icons.Default.Work),
    HighSchoolCatalogueFacility("sma_stem_robotics_lab", "STEM & Robotics Lab", "Kit LEGO Mindstorms, Arduino, mesin cetak 3D, dan workstation pemrograman.", 360000L, "Internasional", Icons.Default.SettingsAccessibility),
    HighSchoolCatalogueFacility("sma_makerspace_fablab", "Makerspace / FabLab", "Bengkel kreatif pembuatan purwarupa hardware, elektro, dan robotika.", 320000L, "Internasional", Icons.Default.Construction),
    HighSchoolCatalogueFacility("sma_observatorium", "Observatorium", "Kubah observatorium astronomi dengan teleskop pelacak bintang otomatis.", 310000L, "Internasional", Icons.Default.Search),
    HighSchoolCatalogueFacility("sma_indoor_sports_arena", "Indoor Sports Arena", "Gedung olahraga tertutup ber-AC untuk futsal, basket, bulutangkis, dan gimnasium.", 420000L, "Internasional", Icons.Default.FitnessCenter),
    HighSchoolCatalogueFacility("sma_cafetaria_premium", "Kafetaria Premium", "Kafetaria modern dengan menu sehat ahli gizi, mesin kopi espresso, dan kasir cashless.", 290000L, "Internasional", Icons.Default.SoupKitchen)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighSchoolFacilityCatalogueScreen(
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

    var selectedItemForBuild by remember { mutableStateOf<HighSchoolCatalogueFacility?>(null) }
    var showGradeDialog by remember { mutableStateOf(false) }

    val filteredList = FULL_HIGH_SCHOOL_CATALOGUE.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Katalog Konstruksi SMA/SMK",
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
        HighSchoolGradeSelectionDialog(
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
fun HighSchoolGradeSelectionDialog(
    item: HighSchoolCatalogueFacility,
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
