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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.example.data.*
import com.example.viewmodel.GameViewModel

data class CharityCatalogueFacility(
    val typeId: String,
    val name: String,
    val description: String,
    val constructionCost: Long,
    val category: String, // "Dasar", "Menengah", "Tinggi", "Internasional"
    val icon: ImageVector,
    val baseBuildTime: Int = when {
        category == "Internasional" -> 3
        category == "Tinggi" -> 2
        else -> 1
    }
)

val HUMANITARIAN_AID_CATALOGUE = listOf(
    // Dasar
    CharityCatalogueFacility("ha_tenda_darurat", "Tenda Darurat Utama", "Tenda shelter tanggap darurat fungsional untuk menampung warga terdampak.", 8000L, "Dasar", Icons.Default.Home),
    CharityCatalogueFacility("ha_dapur_ringkas", "Dapur Umum Ringkas", "Penyediaan makanan hangat darurat siap saji dengan peralatan standar.", 5000L, "Dasar", Icons.Default.Restaurant),
    CharityCatalogueFacility("ha_pos_sehatan", "Pos Kesehatan Lapangan", "Layanan pertolongan pertama dan obat-obatan darurat untuk korban bencana.", 7000L, "Dasar", Icons.Default.LocalHospital),
    CharityCatalogueFacility("ha_gudang_sembako", "Gudang Sembako Lapangan", "Penyimpanan bahan makanan pokok kering untuk didistribusikan ke posko.", 6000L, "Dasar", Icons.Default.Store),
    CharityCatalogueFacility("ha_tangki_air", "Tangki Air Bersih", "Penyediaan air bersih layak minum untuk sanitasi dasar pengungsian.", 4000L, "Dasar", Icons.Default.WaterDrop),
    CharityCatalogueFacility("ha_posko_relawan", "Posko Koordinasi Relawan", "Tempat briefing, registrasi, dan pembagian tugas harian relawan krisis.", 5000L, "Dasar", Icons.Default.Groups),

    // Menengah
    CharityCatalogueFacility("ha_klinik_trauma", "Trauma Center Lapangan", "Layanan terapi mental pemulihan trauma psikologis bagi anak-anak dan dewasa.", 25000L, "Menengah", Icons.Default.Psychology),
    CharityCatalogueFacility("ha_mobil_rescue", "Armada Mobil Rescue", "Mobil serba guna berkemampuan off-road tinggi untuk evakuasi medan berat.", 35000L, "Menengah", Icons.Default.MinorCrash),
    CharityCatalogueFacility("ha_ambulans_taktis", "Ambulans Medis Taktis", "Kendaraan medis darurat lengkap dengan oksigen dan alat pacu jantung.", 30000L, "Menengah", Icons.Default.AirportShuttle),
    CharityCatalogueFacility("ha_gudang_logistik", "Gudang Logistik Utama", "Infrastruktur penampungan bantuan sandang, pangan, papan dalam skala besar.", 40000L, "Menengah", Icons.Default.Warehouse),
    CharityCatalogueFacility("ha_dapur_mobile", "Dapur Umum Truk Mobile", "Truk katering tanggap darurat yang mampu berpindah lokasi dengan cepat.", 45000L, "Menengah", Icons.Default.SoupKitchen),
    CharityCatalogueFacility("ha_pusat_konseling", "Pusat Konseling Sosial", "Layanan advokasi sosial dan pemetaan kebutuhan warga terdampak bencana.", 20000L, "Menengah", Icons.Default.InterpreterMode),

    // Tinggi
    CharityCatalogueFacility("ha_shelter_prefab", "Shelter Prefabrikasi Tahan Gempa", "Hunian sementara terstruktur baja ringan ramah lingkungan tahan gempa.", 90000L, "Tinggi", Icons.Default.HomeWork),
    CharityCatalogueFacility("ha_lab_lapangan", "Lab Kesehatan Lapangan", "Fasilitas diagnostik dan pengujian kualitas air serta wabah penyakit menular.", 85000L, "Tinggi", Icons.Default.Science),
    CharityCatalogueFacility("ha_helikopter_rescue", "Armada Helikopter Rescue", "Helikopter penyelamat untuk evakuasi udara daerah terisolir bencana.", 150000L, "Tinggi", Icons.Default.Airplay),
    CharityCatalogueFacility("ha_pusat_data", "Pusat Data & Koordinasi Satelit", "Pusat transmisi informasi satelit untuk pemetaan krisis secara real-time.", 120000L, "Tinggi", Icons.Default.SettingsInputAntenna),
    CharityCatalogueFacility("ha_pengungsian_ac", "Pengungsian Bertenda Pendingin", "Ruang aula pengungsian besar ber-AC untuk menjamin kelayakan hunian.", 100000L, "Tinggi", Icons.Default.AcUnit),
    CharityCatalogueFacility("ha_ipal_portabel", "Instalasi Air Bersih Portabel", "Mesin filtrasi canggih portabel penyaring air keruh menjadi air layak minum.", 80000L, "Tinggi", Icons.Default.Water),

    // Internasional
    CharityCatalogueFacility("ha_rs_darurat_int", "RS Darurat Internasional (Modular)", "Rumah sakit darurat lengkap dengan ruang operasi bedah intensif standar WHO.", 250000L, "Internasional", Icons.Default.LocalHospital),
    CharityCatalogueFacility("ha_shelter_iot", "Shelter Modular Cerdas (IoT)", "Shelter hunian dengan monitor suhu otomatis, solar panel, dan koneksi internet.", 220000L, "Internasional", Icons.Default.SettingsAccessibility),
    CharityCatalogueFacility("ha_cargo_pesawat", "Armada Cargo Pesawat Kemanusiaan", "Akses penerbangan charter logistik kapasitas ratusan ton lintas negara.", 350000L, "Internasional", Icons.Default.FlightTakeoff),
    CharityCatalogueFacility("ha_lab_karang", "Lab Karantina Bio-Security", "Infrastruktur penanganan medis khusus untuk meredam epidemi global.", 300000L, "Internasional", Icons.Default.Thermostat),
    CharityCatalogueFacility("ha_krisis_ai", "Pusat Komando & AI Krisis Global", "Sistem AI pemodelan bencana alam, perkiraan jalur badai, dan rute logistik.", 280000L, "Internasional", Icons.Default.Computer),
    CharityCatalogueFacility("ha_listrik_hijau", "Pembangkit Listrik Energi Hijau", "Pembangkit mikro-grid bertenaga surya dan angin mandiri tanpa emisi.", 240000L, "Internasional", Icons.Default.Co2)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HumanitarianAidFacilityCatalogueScreen(
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

    val filteredList = HUMANITARIAN_AID_CATALOGUE.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Katalog Konstruksi Posko",
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
                        "Dana Abadi Kemanusiaan",
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
fun CharityGradeSelectionDialog(
    item: CharityCatalogueFacility,
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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1410))
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
                    color = Color(0xFFF4A261),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Konstruksi: $${com.example.ui.formatCurrency(item.constructionCost)} (Kas Dana Abadi)",
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
                        focusedBorderColor = Color(0xFFF4A261),
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
                                    color = if (isSelected) Color(0xFFF4A261) else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF33231B) else Color(0xFF1A1310)
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
                                            color = if (isSelected) Color(0xFFF4A261) else Color.White,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$${com.example.ui.formatCurrency(grade.baseMaintenanceCost)}/bln",
                                            color = Color(0xFF81C784),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Konstruksi: +${item.baseBuildTime + getGradeBuildTimeModifier(grade.name)} bln",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = Color(0xFFF4A261),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (endowmentFund < item.constructionCost) {
                                Toast.makeText(context, "Kas Dana Abadi Yayasan tidak mencukupi!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val finalBuildTime = item.baseBuildTime + getGradeBuildTimeModifier(selectedGrade.name)
                            onConfirm(selectedGrade, customName, finalBuildTime)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4A261))
                    ) {
                        Text("Mulai Konstruksi", color = Color(0xFF1E1410), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
