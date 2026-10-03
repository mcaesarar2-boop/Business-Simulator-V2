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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.School
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoundationPreBuiltScreen(
    navController: NavHostController,
    viewModel: GameViewModel,
    foundationId: String
) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsState()
    val foundation = playerState.foundations.find { it.id == foundationId }

    if (foundation == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0B121E)),
            contentAlignment = Alignment.Center
        ) {
            Text("Yayasan tidak ditemukan.", color = Color.White)
        }
        return
    }

    var customName by remember { mutableStateOf("") }
    var selectedLevel by remember(foundation.type) { mutableStateOf(if (foundation.type == FoundationType.HUMANITARIAN) "Humanitarian Aid" else "TK") }
    var selectedGrade by remember { mutableStateOf(BUILDING_GRADES.first()) }
    var buildError by remember { mutableStateOf<String?>(null) }

    val buildCost = if (foundation.type == FoundationType.HUMANITARIAN) {
        when (selectedLevel) {
            "Humanitarian Aid" -> 150000L
            "Social Care" -> 300000L
            "Disaster Relief" -> 800000L
            "Community Empowerment" -> 500000L
            else -> 150000L
        }
    } else {
        when (selectedLevel) {
            "TK" -> 200000L
            "SD" -> 500000L
            "SMA" -> 1500000L
            "UNIV" -> 5000000L
            else -> 200000L
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cetak Biru Fasilitas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F1E36),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0B121E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14223A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = Color(0xFFD4AF37),
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Konstruksi: ${foundation.name}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (foundation.type == FoundationType.HUMANITARIAN) {
                                        "Mendirikan posko kemanusiaan dan pelayanan sosial dengan spesifikasi gedung terbaik."
                                    } else {
                                        "Sediakan pendidikan berkualitas berlandaskan spesifikasi gedung unggul."
                                    },
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                item {
                    // Inputs Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Informasi Cetak Biru",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = customName,
                                onValueChange = { customName = it },
                                label = { Text(if (foundation.type == FoundationType.HUMANITARIAN) "Nama Institusi Sosial/Bantuan" else "Nama Institusi Pendidikan", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD4AF37),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                                    focusedLabelColor = Color(0xFFD4AF37),
                                    unfocusedLabelColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                if (foundation.type == FoundationType.HUMANITARIAN) "Pilih Divisi Bantuan" else "Pilih Jenjang Sekolah",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val levels = if (foundation.type == FoundationType.HUMANITARIAN) {
                                listOf(
                                    Triple("Humanitarian Aid", "Aid", "Kemanusiaan"),
                                    Triple("Social Care", "Care", "Panti & Sosial"),
                                    Triple("Disaster Relief", "Relief", "Bencana"),
                                    Triple("Community Empowerment", "Empower", "Pemberdayaan")
                                )
                            } else {
                                listOf(
                                    Triple("TK", "TK", "Taman Kanak-Kanak"),
                                    Triple("SD", "SD", "Sekolah Dasar"),
                                    Triple("SMA", "SMA", "Sekolah Menengah"),
                                    Triple("UNIV", "UNIV", "Universitas")
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                levels.forEach { (fullLevel, shortCode, label) ->
                                    val isSelected = selectedLevel == fullLevel
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedLevel = fullLevel }
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.05f),
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFF1B2C3F) else Color(0xFF14223A)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = shortCode,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color(0xFFD4AF37) else Color.White,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = label.split(" ").first(),
                                                color = Color.Gray,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Pilih Spesifikasi & Kualitas Gedung (Grade)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                items(BUILDING_GRADES) { grade ->
                    val isSelected = selectedGrade.name == grade.name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedGrade = grade }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1B2C3F) else Color(0xFF101B2B)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = grade.name,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFFD4AF37) else Color.White,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.1f))
                                    ) {
                                        Text(
                                            text = "${formatCurrency(grade.baseMaintenanceCost)}/bln",
                                            color = Color(0xFF10B981),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = grade.description,
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Estimasi Waktu Konstruksi: ${grade.constructionMonths} Bulan In-Game",
                                    color = if (isSelected) Color(0xFFD4AF37) else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cost calculation and Build Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14223A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Kas Dana Abadi Yayasan:", color = Color.Gray, fontSize = 11.sp)
                        Text(formatCurrency(foundation.endowmentFund), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Biaya Konstruksi Awal:", color = Color.Gray, fontSize = 11.sp)
                        Text(formatCurrency(buildCost), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimasi Waktu Konstruksi:", color = Color.Gray, fontSize = 11.sp)
                        Text("${selectedGrade.constructionMonths} Bulan In-Game", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estimasi Biaya Perawatan Awal:",
                            color = Color(0xFFD4AF37),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${formatCurrency(selectedGrade.baseMaintenanceCost)} / Bulan",
                            color = Color(0xFFD4AF37),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (buildError != null) {
                Text(
                    text = buildError!!,
                    color = Color.Red,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (customName.isBlank()) {
                        buildError = "Nama institusi tidak boleh kosong!"
                        return@Button
                    }
                    if (foundation.endowmentFund < buildCost) {
                        buildError = "Dana abadi kurang! Butuh ${formatCurrency(buildCost)}"
                        return@Button
                    }

                    val success = if (foundation.type == FoundationType.HUMANITARIAN) {
                        viewModel.buildCharityInstitution(
                            foundationId = foundation.id,
                            name = customName,
                            level = selectedLevel,
                            buildingGrade = selectedGrade.name,
                            baseMaintenanceCost = selectedGrade.baseMaintenanceCost
                        )
                    } else {
                        viewModel.buildEducationInstitution(
                            foundationId = foundation.id,
                            name = customName,
                            level = selectedLevel,
                            buildingGrade = selectedGrade.name,
                            baseMaintenanceCost = selectedGrade.baseMaintenanceCost
                        )
                    }

                    if (success) {
                        val message = if (foundation.type == FoundationType.HUMANITARIAN) {
                            "Berhasil merancang & mendirikan posko ${selectedLevel} baru!"
                        } else {
                            "Berhasil merancang & mendirikan sekolah ${selectedLevel} baru!"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        navController.navigateUp()
                    } else {
                        buildError = if (foundation.type == FoundationType.HUMANITARIAN) {
                            "Gagal mendirikan institusi kemanusiaan."
                        } else {
                            "Gagal mendirikan institusi pendidikan."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Sah-kan Cetak Biru & Bangun (${formatCurrency(buildCost)})",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
