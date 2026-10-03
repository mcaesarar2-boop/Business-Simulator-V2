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

val DISASTER_RELIEF_CATALOGUE = listOf(
    // Dasar
    CharityCatalogueFacility("dr_pemantau_cuaca", "Ruang Pantau Cuaca", "Sistem pemantauan cuaca dan peta potensi bencana standar lokal.", 10000L, "Dasar", Icons.Default.Cloud),
    CharityCatalogueFacility("dr_gudang_logistik", "Gudang Logistik Darurat", "Penyimpanan perkakas sekop, tali, tenda terpal, dan lampu darurat.", 6000L, "Dasar", Icons.Default.Warehouse),
    CharityCatalogueFacility("dr_helipad", "Lapangan Heli-pad Darurat", "Area landasan helikopter beton standar untuk pendaratan armada logistik udara.", 8000L, "Dasar", Icons.Default.Airplay),
    CharityCatalogueFacility("dr_pos_jaga_air", "Pos Pemantauan Air & Banjir", "Pos pemantau ketinggian debit air sungai atau rawan tanah longsor.", 5000L, "Dasar", Icons.Default.Water),
    CharityCatalogueFacility("dr_ruang_taktis", "Ruang Rapat Taktis", "Ruang rapat penyusunan peta evakuasi dan koordinasi harian tim SAR.", 7000L, "Dasar", Icons.Default.MeetingRoom),
    CharityCatalogueFacility("dr_perahu_karet", "Gudang Jaket & Perahu Karet", "Penyimpanan perahu karet mesin tempel dan jaket keselamatan banjir.", 6000L, "Dasar", Icons.Default.DirectionsBoat),

    // Menengah
    CharityCatalogueFacility("dr_command_truck", "Mobile Command Truck", "Truk pusat komando bergerak berkemampuan radio satelit menjelajah area blank-spot.", 32000L, "Menengah", Icons.Default.MinorCrash),
    CharityCatalogueFacility("dr_lab_sanitasi", "Lab Pengujian Air & Sanitasi", "Peralatan pengujian kualitas kejernihan air guna mencegah diare pengungsian.", 26000L, "Menengah", Icons.Default.LocalHospital),
    CharityCatalogueFacility("dr_menara_pengawas", "Menara Pengawas Multi-Sensor", "Menara pantau dilengkapi sensor inframerah pendeteksi pergerakan tanah longsor.", 24000L, "Menengah", Icons.Default.Visibility),
    CharityCatalogueFacility("dr_pangan_cadangan", "Gudang Pangan Strategis", "Gudang berinsulasi tinggi penyimpan berton-ton beras cadangan darurat.", 38000L, "Menengah", Icons.Default.Store),
    CharityCatalogueFacility("dr_posko_sar", "Posko SAR Terintegrasi", "Markas latihan menyelam, pemadam kebakaran, dan penyelamatan reruntuhan.", 35000L, "Menengah", Icons.Default.Groups),
    CharityCatalogueFacility("dr_stasiun_gempa", "Stasiun Sensor Seismograf", "Sensor pendeteksi getaran gempa bumi terintegrasi lembaga nasional.", 28000L, "Menengah", Icons.Default.SettingsInputAntenna),

    // Tinggi
    CharityCatalogueFacility("dr_shelter_anti_badai", "Shelter Pengungsian Anti Badai", "Kubah beton aerodinamis tahan terjangan angin topan badai ekstrem.", 98000L, "Tinggi", Icons.Default.HomeWork),
    CharityCatalogueFacility("dr_lab_forensik", "Lab Identifikasi & Forensik", "Fasilitas identifikasi medis darurat forensik korban gempa bumi masal.", 90000L, "Tinggi", Icons.Default.Science),
    CharityCatalogueFacility("dr_hanggar_helikopter", "Hanggar Helikopter SAR", "Hanggar perawatan khusus armada helikopter evakuasi kebencanaan.", 120000L, "Tinggi", Icons.Default.Warehouse),
    CharityCatalogueFacility("dr_conveyor_logistik", "Pusat Logistik Conveyor Otomatis", "Sistem sortir logistik otomatis mempercepat packing ribuan bantuan harian.", 110000L, "Tinggi", Icons.Default.Settings),
    CharityCatalogueFacility("dr_pemancar_radio", "Stasiun Pemancar Radio Darurat", "Pemancar radio AM/FM darurat guna menyebarkan instruksi keselamatan warga.", 80000L, "Tinggi", Icons.Default.Radio),
    CharityCatalogueFacility("dr_evakuasi_amfibi", "Truk Evakuasi Amfibi (All-Terrain)", "Truk raksasa segala medan berkemampuan mengapung melintasi banjir bandang.", 100000L, "Tinggi", Icons.Default.AirportShuttle),

    // Internasional
    CharityCatalogueFacility("dr_ai_satelit", "Pusat Komando AI Satelit Global", "Infrastruktur superkomputer pemetaan bencana berbasis satelit citra radar AI.", 270000L, "Internasional", Icons.Default.Computer),
    CharityCatalogueFacility("dr_bunker_nuklir", "Bunker Perlindungan Ekstrem", "Bunker bawah tanah terdalam tahan radiasi nuklir dan bencana meteorologi.", 250000L, "Internasional", Icons.Default.Roofing),
    CharityCatalogueFacility("dr_pesawat_garam", "Armada Pesawat Penabur Garam", "Pesawat penabur awan garam memodifikasi cuaca hujan peredam banjir/kemarau.", 360000L, "Internasional", Icons.Default.FlightTakeoff),
    CharityCatalogueFacility("dr_rs_bedah_udara", "RS Bedah Darurat Udara (Pesawat)", "Pesawat khusus termodifikasi menjadi ruang bedah darurat operasi di udara.", 320000L, "Internasional", Icons.Default.LocalHospital),
    CharityCatalogueFacility("dr_lab_patogen", "Lab Genomik Patogen & Epidemi", "Lab biosafety level 4 mendeteksi ancaman biologi/wabah penyakit menular.", 310000L, "Internasional", Icons.Default.Thermostat),
    CharityCatalogueFacility("dr_vr_simulasi", "Pusat Simulasi Bencana VR Global", "Ruang pelatihan simulasi evakuasi gempa berskala masal teknologi Virtual Reality.", 280000L, "Internasional", Icons.Default.SettingsAccessibility)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisasterReliefFacilityCatalogueScreen(
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

    val filteredList = DISASTER_RELIEF_CATALOGUE.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Katalog Konstruksi Posko Bencana",
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
                        "Dana Abadi Mitigasi Bencana",
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
