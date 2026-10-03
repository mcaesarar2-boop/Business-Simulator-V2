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

val COMMUNITY_EMPOWERMENT_CATALOGUE = listOf(
    // Dasar
    CharityCatalogueFacility("ce_kelas_pelatihan", "Ruang Pelatihan Kejuruan", "Ruang kelas fungsional untuk melatih keterampilan tata boga, menjahit, dan administrasi.", 9000L, "Dasar", Icons.Default.School),
    CharityCatalogueFacility("ce_balai_warga", "Balai Pertemuan Warga", "Tempat berkumpulnya komunitas warga untuk bermusyawarah, sosialisasi, dan penyuluhan.", 6000L, "Dasar", Icons.Default.Groups),
    CharityCatalogueFacility("ce_konseling_bisnis", "Pojok Konseling Bisnis", "Layanan pendampingan dasar perencanaan keuangan dan pendaftaran legalitas usaha.", 7000L, "Dasar", Icons.Default.Handshake),
    CharityCatalogueFacility("ce_perpustakaan_kom", "Perpustakaan Komunitas", "Koleksi buku-buku terapan pertanian, pertukangan, dan buku motivasi bisnis.", 5000L, "Dasar", Icons.Default.LibraryBooks),
    CharityCatalogueFacility("ce_koperasi_warga", "Pojok Koperasi Warga", "Unit simpan pinjam bunga rendah untuk permodalan usaha ultra-mikro warga.", 6000L, "Dasar", Icons.Default.Store),
    CharityCatalogueFacility("ce_kebun_gizi", "Kebun Gizi Komunitas", "Kebun sayur hidroponik warga asuh guna menjaga ketahanan pangan keluarga.", 5000L, "Dasar", Icons.Default.Grass),

    // Menengah
    CharityCatalogueFacility("ce_lab_komputer", "Lab Komputer Pemberdayaan", "Pelatihan komputer dasar, pembuatan website toko online, dan literasi digital.", 26000L, "Menengah", Icons.Default.Computer),
    CharityCatalogueFacility("ce_bengkel_kerja", "Bengkel Otomotif & Elektro", "Ruang praktik perbaikan motor, instalasi AC, dan perbaikan perangkat elektronik.", 28000L, "Menengah", Icons.Default.Handyman),
    CharityCatalogueFacility("ce_sentra_tani", "Sentra Pengolahan Tani (UMKM)", "Mesin pengering gabah, penggiling kopi, dan pembuat keripik skala UMKM warga.", 30000L, "Menengah", Icons.Default.Agriculture),
    CharityCatalogueFacility("ce_galeri_umkm", "Galeri Pameran Produk Warga", "Showroom fisik tempat memajang dan menjual kerajinan tangan hasil warga asuh.", 22000L, "Menengah", Icons.Default.ShoppingBag),
    CharityCatalogueFacility("ce_pusat_karakter", "Pusat Mentoring Karakter", "Program mingguan pembinaan etika bisnis, kepemimpinan, dan spiritualitas.", 18000L, "Menengah", Icons.Default.AutoAwesome),
    CharityCatalogueFacility("ce_lapangan_komunitas", "Lapangan Olahraga Komunitas", "Fasilitas olahraga multifungsi guna memupuk kerukunan dan kesehatan warga.", 25000L, "Menengah", Icons.Default.SportsBasketball),

    // Tinggi
    CharityCatalogueFacility("ce_inkubator_bisnis", "Inkubator Bisnis Digital", "Fasilitas akselerasi startup lokal, pembinaan branding produk, dan modal ventura.", 95000L, "Tinggi", Icons.Default.Lightbulb),
    CharityCatalogueFacility("ce_studio_konten", "Studio Produksi Konten Kreatif", "Studio podcast, kamera profesional, dan PC editing melatih keahlian digital creator.", 90000L, "Tinggi", Icons.Default.VideoCameraBack),
    CharityCatalogueFacility("ce_lab_mutu", "Lab Pengujian Mutu Produk", "Lab standarisasi kelayakan pangan, sertifikasi halal, dan pengemasan kedap udara.", 100000L, "Tinggi", Icons.Default.Science),
    CharityCatalogueFacility("ce_energi_mandiri", "Sentra Energi Mandiri (Solar)", "Instalasi panel surya dan instalasi biogas kotoran ternak ramah lingkungan.", 85000L, "Tinggi", Icons.Default.Co2),
    CharityCatalogueFacility("ce_budidaya_ikan", "Kolam Budidaya Ikan Intensif", "Kolam ikan bioflok teknologi tinggi untuk ketahanan ekonomi pangan warga panti.", 80000L, "Tinggi", Icons.Default.Water),
    CharityCatalogueFacility("ce_koperasi_besar", "Gedung Koperasi Skala Besar", "Pusat koperasi berkapasitas ratusan anggota dengan manajemen modern.", 110000L, "Tinggi", Icons.Default.Business),

    // Internasional
    CharityCatalogueFacility("ce_agro_inovasi", "Pusat Inovasi Agro-Teknologi", "Fasilitas riset pertanian hidroponik canggih terintegrasi sensor kelembaban IoT.", 260000L, "Internasional", Icons.Default.Eco),
    CharityCatalogueFacility("ce_riset_energi", "Lab Riset Energi Terbarukan", "Lab pengembangan generator listrik tenaga ombak dan turbin angin mikro.", 240000L, "Internasional", Icons.Default.FlashOn),
    CharityCatalogueFacility("ce_sekolah_tinggi", "Sekolah Vokasi Inovatif Global", "Sekolah tinggi vokasi dengan kurikulum kerja sama industri manufaktur dunia.", 320000L, "Internasional", Icons.Default.School),
    CharityCatalogueFacility("ce_fulfillment", "Sentra Ekspor & Fulfillment Center", "Gudang penyimpanan dan pengiriman komoditas lokal UMKM ke mancanegara.", 300000L, "Internasional", Icons.Default.LocalShipping),
    CharityCatalogueFacility("ce_smart_grid", "Smart Micro-Grid AI Center", "Pusat kendali AI pembagi beban listrik energi terbarukan komunitas secara cerdas.", 290000L, "Internasional", Icons.Default.Computer),
    CharityCatalogueFacility("ce_eco_tourism", "Botanical Eco-Tourism & Forest", "Hutan konservasi eduwisata alam terpadu menghasilkan devisa bagi warga.", 270000L, "Internasional", Icons.Default.Forest)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityEmpowermentFacilityCatalogueScreen(
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

    val filteredList = COMMUNITY_EMPOWERMENT_CATALOGUE.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Katalog Konstruksi Sentra",
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
                        "Dana Abadi Pemberdayaan",
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
