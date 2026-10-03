package com.example.ui

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.data.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoundationDetailScreen(navController: NavHostController, viewModel: GameViewModel, foundationId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val foundation = playerState.foundations.find { it.id == foundationId }

    if (foundation == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF050C1A)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Yayasan tidak ditemukan", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Kembali")
                }
            }
        }
        return
    }

    var showInjectDialog by remember { mutableStateOf(false) }
    var injectAmountString by remember { mutableStateOf("") }
    var injectError by remember { mutableStateOf<String?>(null) }

    var showBuildDialog by remember { mutableStateOf(false) }
    var selectedBlueprint by remember { mutableStateOf<FacilityBlueprint?>(null) }
    var selectedTier by remember { mutableStateOf(FoundationBlueprints.tiers[0]) }
    var customFacilityName by remember { mutableStateOf("") }
    var buildError by remember { mutableStateOf<String?>(null) }

    var showEduBuildDialog by remember { mutableStateOf(false) }
    var selectedEduLevel by remember { mutableStateOf("TK") }
    var customEduName by remember { mutableStateOf("") }

    var showHealthBuildDialog by remember { mutableStateOf(false) }
    var selectedHealthLevel by remember { mutableStateOf("Klinik") }
    var customHealthName by remember { mutableStateOf("") }

    var showCurriculumDialog by remember { mutableStateOf(false) }
    var curriculumTargetInstitution by remember { mutableStateOf<com.example.data.EducationInstitution?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(foundation.name, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B192C))
            )
        },
        containerColor = Color(0xFF050C1A)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foundation Info & Legal State Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101C2E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "INFORMASI INSTITUSI",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4AF37),
                                fontSize = 12.sp
                            )
                            if (foundation.isLegalized) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("AKTIF", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE65100).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("LEGALISASI", color = Color(0xFFFF9800), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = foundation.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "Jenis: ${foundation.type.label}", color = Color.LightGray, fontSize = 13.sp)

                        if (!foundation.isLegalized) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "⚠️ Yayasan sedang dalam tahap legalisasi notaris & hukum. Anda baru dapat menyuntikkan dana abadi dan mendirikan fasilitas setelah status berizin resmi (Sisa ${foundation.constructionMonthsLeft} Bulan).",
                                color = Color(0xFFFF9800),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (foundation.isLegalized) {
                // Endowment Fund Card (Dana Abadi)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E19)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🏦 DANA ABADI (ENDOWMENT FUND)",
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = com.example.ui.formatCurrency(foundation.endowmentFund),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Dana ini digunakan murni untuk membiayai operasional bulanan fasilitas nirlaba. Pengeluaran fasilitas tidak memotong saldo pribadi bulanan Anda.",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        injectAmountString = ""
                                        injectError = null
                                        showInjectDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Icon(imageVector = Icons.Default.CurrencyExchange, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Suntik Dana", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                
                                Button(
                                    onClick = {
                                        if (foundation.type == FoundationType.EDUCATION || foundation.type == FoundationType.HUMANITARIAN) {
                                            navController.navigate("foundation_pre_built/${foundation.id}")
                                        } else if (foundation.type == FoundationType.HEALTHCARE) {
                                            customHealthName = ""
                                            selectedHealthLevel = "Klinik"
                                            buildError = null
                                            showHealthBuildDialog = true
                                        } else {
                                            selectedBlueprint = FoundationBlueprints.blueprints[foundation.type]?.firstOrNull()
                                            selectedTier = FoundationBlueprints.tiers[0]
                                            customFacilityName = ""
                                            buildError = null
                                            showBuildDialog = true
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                                ) {
                                    Icon(imageVector = Icons.Default.AddBusiness, contentDescription = null, tint = Color(0xFF0F1E36))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Bangun Fasilitas", fontWeight = FontWeight.Bold, color = Color(0xFF0F1E36))
                                }
                            }
                        }
                    }
                }

                if (foundation.type == FoundationType.EDUCATION) {
                    item {
                        Text(
                            text = "Cetak Biru Institusi Pendidikan & Riset",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    val eduList = foundation.educationInstitutions ?: emptyList()
                    if (eduList.isNotEmpty()) {
                        val chunks = eduList.chunked(2)
                        chunks.forEach { pair ->
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    pair.forEach { inst ->
                                        Box(modifier = Modifier.weight(1.0f)) {
                                            EducationInstitutionCard(
                                                inst = inst,
                                                endowmentFund = foundation.endowmentFund,
                                                onUpgrade = {
                                                    viewModel.upgradeEduFacility(foundation.id, inst.id)
                                                },
                                                onChangeCurriculum = {
                                                    curriculumTargetInstitution = inst
                                                    showCurriculumDialog = true
                                                },
                                                onCardClick = {
                                                    val route = when (inst.level) {
                                                        "TK" -> "kindergarten_dashboard/${foundation.id}/${inst.id}"
                                                        "SD" -> "primary_school_dashboard/${foundation.id}/${inst.id}"
                                                        "SMA" -> "high_school_dashboard/${foundation.id}/${inst.id}"
                                                        "UNIV" -> "university_dashboard/${foundation.id}/${inst.id}"
                                                        else -> ""
                                                    }
                                                    if (route.isNotEmpty()) {
                                                        navController.navigate(route)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    if (pair.size < 2) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HomeWork,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Belum ada fasilitas dibangun.",
                                        color = Color.LightGray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Klik tombol \"Bangun Fasilitas\" di atas untuk mendirikan sekolah atau kampus riset.",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else if (foundation.type == FoundationType.HEALTHCARE) {
                    item {
                        Text(
                            text = "Cetak Biru Institusi Kesehatan & Medis",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    val healthList = foundation.healthInstitutions ?: emptyList()
                    if (healthList.isNotEmpty()) {
                        val chunks = healthList.chunked(2)
                        chunks.forEach { pair ->
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    pair.forEach { inst ->
                                        Box(modifier = Modifier.weight(1.0f)) {
                                            HealthInstitutionCard(
                                                inst = inst,
                                                endowmentFund = foundation.endowmentFund,
                                                onUpgrade = {
                                                    viewModel.upgradeHealthFacility(foundation.id, inst.id)
                                                },
                                                onCardClick = {
                                                    val route = when (inst.level) {
                                                        "Klinik" -> "clinic_dashboard/${foundation.id}/${inst.id}"
                                                        "RS Umum" -> "general_hospital_dashboard/${foundation.id}/${inst.id}"
                                                        "RS Khusus" -> "specialized_hospital_dashboard/${foundation.id}/${inst.id}"
                                                        "RS Internasional" -> "international_hospital_dashboard/${foundation.id}/${inst.id}"
                                                        else -> ""
                                                    }
                                                    if (route.isNotEmpty()) {
                                                        navController.navigate(route)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    if (pair.size < 2) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Belum ada fasilitas dibangun.",
                                        color = Color.LightGray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Klik tombol \"Bangun Fasilitas\" di atas untuk mendirikan klinik atau rumah sakit.",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else if (foundation.type == FoundationType.HUMANITARIAN) {
                    item {
                        Text(
                            text = "Institusi Sosial & Kemanusiaan",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    val charityList = foundation.charityInstitutions ?: emptyList()
                    if (charityList.isNotEmpty()) {
                        val chunks = charityList.chunked(2)
                        chunks.forEach { pair ->
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    pair.forEach { inst ->
                                        Box(modifier = Modifier.weight(1.0f)) {
                                            CharityInstitutionCard(
                                                inst = inst,
                                                endowmentFund = foundation.endowmentFund,
                                                onCardClick = {
                                                    val route = when (inst.level) {
                                                        "Humanitarian Aid" -> "humanitarian_aid_dashboard/${foundation.id}/${inst.id}"
                                                        "Social Care" -> "social_care_dashboard/${foundation.id}/${inst.id}"
                                                        "Disaster Relief" -> "disaster_relief_dashboard/${foundation.id}/${inst.id}"
                                                        "Community Empowerment" -> "community_empowerment_dashboard/${foundation.id}/${inst.id}"
                                                        else -> ""
                                                    }
                                                    if (route.isNotEmpty()) {
                                                        navController.navigate(route)
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    if (pair.size < 2) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Belum ada institusi dibangun.",
                                        color = Color.LightGray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Klik tombol \"Bangun Fasilitas\" di atas untuk mendirikan pos bantuan sosial atau posko kemanusiaan.",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Title: Built Facilities
                    item {
                        Text(
                            text = "Fasilitas & Layanan Sosial",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    if (foundation.facilities.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HomeWork,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Belum ada fasilitas dibangun.",
                                        color = Color.LightGray,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Gunakan tombol di atas untuk mendirikan sekolah, klinik, atau panti.",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(foundation.facilities) { facility ->
                            FacilityItemCard(facility = facility)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialog: Inject Endowment Fund
    if (showInjectDialog) {
        AlertDialog(
            onDismissRequest = { showInjectDialog = false },
            title = { Text("Suntik Dana Abadi (Endowment)", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Donasikan murni dari Kas Pribadi CEO (privateBalance) Anda ke dalam yayasan ini. Dana nirlaba tidak dapat ditarik kembali ke kas pribadi.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = injectAmountString,
                        onValueChange = { injectAmountString = it },
                        label = { Text("Jumlah Suntikan Saldo ($)") },
                        placeholder = { Text("Cth: 1000000") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // Quick buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(500_000L, 1_000_000L, 5_000_000L).forEach { quickAmt ->
                            Button(
                                onClick = { injectAmountString = quickAmt.toString() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B3B2B)),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(com.example.ui.formatCurrencyRingkas(quickAmt, false), fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }

                    Text(
                        text = "Saldo Pribadi Anda: ${com.example.ui.formatCurrency(playerState.privateBalance)}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        fontSize = 12.sp
                    )

                    if (injectError != null) {
                        Text(text = injectError!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = injectAmountString.toLongOrNull()
                        if (amt == null || amt <= 0) {
                            injectError = "Jumlah harus berupa angka positif!"
                            return@Button
                        }
                        if (playerState.privateBalance < amt) {
                            injectError = "Saldo Kas Pribadi Anda kurang!"
                            return@Button
                        }
                        val success = viewModel.injectEndowmentFund(foundation.id, amt)
                        if (success) {
                            showInjectDialog = false
                        } else {
                            injectError = "Gagal menyuntikkan dana."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Donasikan", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInjectDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    // Dialog: Build Facility
    if (showBuildDialog) {
        AlertDialog(
            onDismissRequest = { showBuildDialog = false },
            title = { Text("Bangun Fasilitas Nirlaba", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = customFacilityName,
                        onValueChange = { customFacilityName = it },
                        label = { Text("Nama Sekolah / Fasilitas") },
                        placeholder = { Text("Cth: SD Negeri Tunas Harapan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Pilih Cetak Biru Fasilitas:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    // Blueprints selection
                    val blueprints = FoundationBlueprints.blueprints[foundation.type] ?: emptyList()
                    blueprints.forEach { blueprint ->
                        val isSel = selectedBlueprint == blueprint
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBlueprint = blueprint }
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) Color(0xFFD4AF37) else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) Color(0xFF10223D) else Color.Transparent
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = blueprint.category, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text(text = blueprint.description, color = Color.Gray, fontSize = 11.sp, lineHeight = 13.sp)
                            }
                        }
                    }

                    Text("Pilih Kurikulum / Kualitas Layanan:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    // Tiers selection
                    FoundationBlueprints.tiers.forEach { tier ->
                        val isSel = selectedTier == tier
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTier = tier }
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) Color(0xFFD4AF37) else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) Color(0xFF10223D) else Color.Transparent
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = tier.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text(text = tier.description, color = Color.Gray, fontSize = 11.sp, lineHeight = 13.sp)
                            }
                        }
                    }

                    // Calculation Summary
                    selectedBlueprint?.let { bp ->
                        val cost = (bp.baseCost * selectedTier.costMultiplier).toLong()
                        val ops = (bp.baseMonthlyOps * selectedTier.opsMultiplier).toLong()
                        val prestige = (bp.basePrestige * selectedTier.prestigeMultiplier).toLong()

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("ESTIMASI BIAYA & MANFAAT", fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37), fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Biaya Pembangunan:", fontSize = 11.sp, color = Color.LightGray)
                                    Text(com.example.ui.formatCurrency(cost), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Operasional Bulanan:", fontSize = 11.sp, color = Color.LightGray)
                                    Text("${com.example.ui.formatCurrency(ops)} / Bulan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Legacy Bulanan:", fontSize = 11.sp, color = Color.LightGray)
                                    Text("+${prestige} Prestige Points", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Masa Konstruksi:", fontSize = 11.sp, color = Color.LightGray)
                                    Text("${bp.buildMonths} Bulan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    if (buildError != null) {
                        Text(text = buildError!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bp = selectedBlueprint
                        if (bp == null) {
                            buildError = "Silakan pilih cetak biru!"
                            return@Button
                        }
                        if (customFacilityName.trim().isEmpty()) {
                            buildError = "Nama fasilitas tidak boleh kosong!"
                            return@Button
                        }
                        val cost = (bp.baseCost * selectedTier.costMultiplier).toLong()
                        val ops = (bp.baseMonthlyOps * selectedTier.opsMultiplier).toLong()
                        val prestige = (bp.basePrestige * selectedTier.prestigeMultiplier).toLong()

                        if (foundation.endowmentFund < cost) {
                            buildError = "Dana Abadi Yayasan kurang! Butuh ${com.example.ui.formatCurrency(cost)}"
                            return@Button
                        }

                        val success = viewModel.buildFoundationFacility(
                            foundationId = foundation.id,
                            name = customFacilityName.trim(),
                            category = bp.category,
                            tier = selectedTier.name,
                            buildCost = cost,
                            buildMonths = bp.buildMonths,
                            monthlyOps = ops,
                            prestigeReward = prestige
                        )
                        if (success) {
                            showBuildDialog = false
                        } else {
                            buildError = "Gagal membangun fasilitas."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                ) {
                    Text("Mulai Bangun", color = Color(0xFF0F1E36), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBuildDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showCurriculumDialog && curriculumTargetInstitution != null) {
        val inst = curriculumTargetInstitution!!
        val options = when (inst.level) {
            "TK" -> listOf("Nasional", "Montessori", "Waldorf")
            "SD" -> listOf("Nasional", "Agama Terpadu")
            "SMA" -> listOf("Nasional", "Cambridge", "IB")
            "UNIV" -> listOf("Nasional (Teaching Univ)", "Internasional (Double Degree)", "World-Class Research Univ")
            else -> listOf("Nasional")
        }

        AlertDialog(
            onDismissRequest = { showCurriculumDialog = false },
            title = { Text("Pilih Kurikulum - ${if (inst.level == "TK") "TK/TKA" else if (inst.level == "SD") "SD/MI" else if (inst.level == "SMA") "SMA/SMK" else "Universitas"}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Memilih kurikulum khusus meningkatkan kualitas akreditasi, namun meningkatkan biaya operasional bulanan secara signifikan.",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    options.forEach { option ->
                        val costMultiplier = when (option) {
                            "Montessori", "Waldorf" -> "+50% Ops"
                            "Agama Terpadu" -> "+75% Ops"
                            "Cambridge", "IB" -> "+200% Ops"
                            "Internasional" -> "+500% Ops"
                            "Nasional (Teaching Univ)" -> "+50% Ops"
                            "Internasional (Double Degree)" -> "+200% Ops"
                            "World-Class Research Univ" -> "+400% Ops"
                            else -> "Biaya Standar"
                        }
                        
                        val isSelected = inst.curriculumType == option
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.changeEduCurriculum(foundation.id, inst.id, option)
                                    showCurriculumDialog = false
                                }
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFFD4AF37) else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1B2C3F) else Color(0xFF101B2B)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = option, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFFD4AF37) else Color.White, fontSize = 13.sp)
                                    Text(text = costMultiplier, color = Color.LightGray, fontSize = 11.sp)
                                }
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurriculumDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showEduBuildDialog) {
        AlertDialog(
            onDismissRequest = { showEduBuildDialog = false },
            title = { Text("Bangun Institusi Pendidikan", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Pilih jenjang sekolah atau kampus riset yang ingin didirikan di bawah naungan yayasan Anda. Semua institusi baru akan menggunakan Kurikulum Nasional secara default.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = customEduName,
                        onValueChange = { customEduName = it },
                        label = { Text("Nama Institusi", color = Color.Gray) },
                        placeholder = { Text("Cth: Sekolah Tunas Bangsa", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4AF37),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = Color(0xFFD4AF37),
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Text("Pilih Jenjang Pendidikan:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)

                    val levels = listOf(
                        Triple("TK", "Taman Kanak-Kanak", 200_000L),
                        Triple("SD", "Sekolah Dasar", 500_000L),
                        Triple("SMA", "Sekolah Menengah Atas", 1_500_000L),
                        Triple("UNIV", "Universitas & Riset", 5_000_000L)
                    )

                    levels.forEach { (lvl, label, cost) ->
                        val isSel = selectedEduLevel == lvl
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedEduLevel = lvl }
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) Color(0xFFD4AF37) else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) Color(0xFF10223D) else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = label, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text(text = "Jenjang: $lvl", color = Color.Gray, fontSize = 11.sp)
                                }
                                Text(
                                    text = com.example.ui.formatCurrency(cost),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSel) Color(0xFFD4AF37) else Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    if (buildError != null) {
                        Text(text = buildError!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cost = when (selectedEduLevel) {
                            "TK" -> 200000L
                            "SD" -> 500000L
                            "SMA" -> 1500000L
                            "UNIV" -> 5000000L
                            else -> 200000L
                        }

                        if (customEduName.isBlank()) {
                            buildError = "Nama institusi tidak boleh kosong!"
                            return@Button
                        }

                        if (foundation.endowmentFund < cost) {
                            buildError = "Dana Abadi Yayasan kurang! Butuh ${com.example.ui.formatCurrency(cost)}"
                            return@Button
                        }

                        val success = viewModel.buildEducationInstitution(
                            foundationId = foundation.id,
                            name = customEduName,
                            level = selectedEduLevel
                        )
                        if (success) {
                            showEduBuildDialog = false
                        } else {
                            buildError = "Gagal mendirikan institusi pendidikan."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                ) {
                    Text("Mulai Bangun", color = Color(0xFF0F1E36), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEduBuildDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showHealthBuildDialog) {
        AlertDialog(
            onDismissRequest = { showHealthBuildDialog = false },
            title = { Text("Bangun Institusi Medis & Kesehatan", fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Pilih jenjang fasilitas medis yang ingin didirikan di bawah naungan yayasan kesehatan Anda. Semua institusi baru akan menggunakan pelayanan Subsidi secara default.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = customHealthName,
                        onValueChange = { customHealthName = it },
                        label = { Text("Nama Institusi Kesehatan", color = Color.Gray) },
                        placeholder = { Text("Cth: Klinik Sehat Medika", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD4AF37),
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                            focusedLabelColor = Color(0xFFD4AF37),
                            unfocusedLabelColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Text("Pilih Jenjang Layanan Kesehatan:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)

                    val healthLevels = listOf(
                        Triple("Klinik", "Klinik Pratama", 300_000L),
                        Triple("RS Umum", "Rumah Sakit Umum", 1_000_000L),
                        Triple("RS Khusus", "Rumah Sakit Khusus", 3_000_000L),
                        Triple("RS Internasional", "Rumah Sakit Internasional", 10_000_000L)
                    )

                    healthLevels.forEach { (lvl, label, cost) ->
                        val isSel = selectedHealthLevel == lvl
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedHealthLevel = lvl }
                                .border(
                                    width = if (isSel) 2.dp else 1.dp,
                                    color = if (isSel) Color(0xFF2E7D32) else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSel) Color(0xFF0F241D) else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = label, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                    Text(text = "Jenjang: $lvl", color = Color.Gray, fontSize = 11.sp)
                                }
                                Text(
                                    text = com.example.ui.formatCurrency(cost),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSel) Color(0xFF4CAF50) else Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    if (buildError != null) {
                        Text(text = buildError!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cost = when (selectedHealthLevel) {
                            "Klinik" -> 300000L
                            "RS Umum" -> 1000000L
                            "RS Khusus" -> 3000000L
                            "RS Internasional" -> 10000000L
                            else -> 300000L
                        }

                        if (customHealthName.isBlank()) {
                            buildError = "Nama institusi tidak boleh kosong!"
                            return@Button
                        }

                        if (foundation.endowmentFund < cost) {
                            buildError = "Dana Abadi Yayasan kurang! Butuh ${com.example.ui.formatCurrency(cost)}"
                            return@Button
                        }

                        val success = viewModel.buildHealthInstitution(
                            foundationId = foundation.id,
                            name = customHealthName,
                            level = selectedHealthLevel
                        )
                        if (success) {
                            showHealthBuildDialog = false
                        } else {
                            buildError = "Gagal mendirikan institusi kesehatan."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Mulai Bangun", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHealthBuildDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun FacilityItemCard(facility: FoundationFacility) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(text = facility.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text(text = "${facility.category} | ${facility.tier}", color = Color.LightGray, fontSize = 11.sp)
                }

                if (facility.buildMonthsLeft > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE65100).copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("KONSTRUKSI", color = Color(0xFFFF9800), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    if (facility.isOperational) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("AKTIF", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFC62828).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("MANGKRAK", color = Color(0xFFE53935), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))

            if (facility.buildMonthsLeft > 0) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sisa Masa Konstruksi:", color = Color.Gray, fontSize = 11.sp)
                        Text("${facility.buildMonthsLeft} Bulan", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Biaya Operasional", color = Color.Gray, fontSize = 11.sp)
                        Text(
                            text = "${com.example.ui.formatCurrency(facility.monthlyOperationalCost)} / bln",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Manfaat Legacy", color = Color.Gray, fontSize = 11.sp)
                        Text(
                            text = if (facility.isOperational) "+${facility.prestigeReward} Legacy" else "+0 Legacy",
                            color = if (facility.isOperational) Color(0xFFD4AF37) else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EducationInstitutionCard(
    inst: com.example.data.EducationInstitution,
    endowmentFund: Long,
    onUpgrade: () -> Unit,
    onChangeCurriculum: () -> Unit,
    onCardClick: () -> Unit
) {
    val levelLabel = when (inst.level) {
        "TK" -> "TK/TKA"
        "SD" -> "SD/MI"
        "SMA" -> "SMA/SMK"
        "UNIV" -> "Universitas & Riset"
        else -> inst.level
    }

    val baseUpgradeCost = when (inst.level) {
        "TK" -> 150000L
        "SD" -> 400000L
        "SMA" -> 1200000L
        "UNIV" -> 4000000L
        else -> 150000L
    }
    val upgradeCost = baseUpgradeCost * inst.facilityLevel
    val canUpgrade = inst.facilityLevel < 5 && endowmentFund >= upgradeCost && inst.constructionMonthsLeft <= 0

    val opsCost = inst.calculateTotalMonthlyOpsCost()

    val isUnderConstruction = inst.constructionMonthsLeft > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isUnderConstruction) {
                    Color(0xFFFBC02D).copy(alpha = 0.5f)
                } else if (!inst.isOperational) {
                    Color(0xFF81D4FA).copy(alpha = 0.4f)
                } else {
                    Color.White.copy(alpha = 0.05f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .then(
                if (isUnderConstruction) {
                    Modifier
                } else {
                    Modifier.clickable { onCardClick() }
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnderConstruction) {
                Color(0xFF1E1B10)
            } else if (!inst.isOperational) {
                Color(0xFF152238) // Abu-abu / Biru Pucat
            } else {
                Color(0xFF101B2B)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (isUnderConstruction) {
                // UNDER CONSTRUCTION STATE UI
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🚧 SEDANG DIBANGUN",
                        color = Color(0xFFFFB300),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFB300).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = inst.buildingGrade,
                            color = Color(0xFFFFB300),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                // Level & Name
                Text(
                    text = inst.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "$levelLabel | Rencana Level 1/5",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                val total = if (inst.constructionMonthsTotal > 0) inst.constructionMonthsTotal else 1
                val left = inst.constructionMonthsLeft
                val built = total - left
                val progressVal = (built.toFloat() / total.toFloat()).coerceIn(0f, 1f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Progress Konstruksi:", color = Color.Gray, fontSize = 11.sp)
                    Text("${(progressVal * 100).toInt()}%", color = Color(0xFFFFB300), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progressVal },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFFB300),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Sisa Konstruksi: $left Bulan",
                        color = Color(0xFFFFB300),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // NORMAL STATE UI
                // Accreditation Points Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Akreditasi: ${inst.accreditationPoints}/100",
                        color = if (inst.accreditationPoints >= 90) Color(0xFF4CAF50) else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                    if (inst.accreditationPoints >= 90) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "UNGGUL", 
                                color = Color(0xFF4CAF50), 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    } else if (inst.accreditationPoints >= 60) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1976D2).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "BAIK", 
                                color = Color(0xFF2196F3), 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE65100).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "CUKUP", 
                                color = Color(0xFFFF9800), 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { inst.accreditationPoints / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (inst.accreditationPoints >= 90) Color(0xFF4CAF50) else Color(0xFFD4AF37),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Level & Name
                Text(
                    text = inst.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = if (!inst.isOperational) "Menunggu Akreditasi/Fasilitas Dasar" else "$levelLabel | Level ${inst.facilityLevel}/5 | ${inst.buildingGrade}",
                    color = if (!inst.isOperational) Color(0xFFFFB300) else Color.LightGray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(6.dp))

                // Info Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kurikulum:", color = Color.Gray, fontSize = 11.sp)
                    Text(inst.curriculumType, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ops Bulanan:", color = Color.Gray, fontSize = 11.sp)
                    Text(
                        text = com.example.ui.formatCurrency(opsCost),
                        color = Color(0xFFE57373),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SPP Bulanan:", color = Color.Gray, fontSize = 11.sp)
                    Text(
                        text = "${com.example.ui.formatCurrency(inst.monthlySpp)}/murid",
                        color = Color(0xFF81C784),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }


            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthInstitutionCard(
    inst: com.example.data.HealthInstitution,
    endowmentFund: Long,
    onUpgrade: () -> Unit,
    onCardClick: () -> Unit
) {
    val isUnderConstruction = inst.constructionMonthsLeft > 0
    val opsCost = inst.calculateTotalMonthlyOpsCost()

    val cardColor = Color(0xFF0F241D)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Accreditation Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AKREDITASI",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isUnderConstruction) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE65100).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("KONSTRUKSI", color = Color(0xFFFF9800), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (inst.accreditationPoints >= 90) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "UNGGUL", 
                                color = Color(0xFF4CAF50), 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    } else if (inst.accreditationPoints >= 60) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1976D2).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "BAIK", 
                                color = Color(0xFF2196F3), 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE65100).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "CUKUP", 
                                color = Color(0xFFFF9800), 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { inst.accreditationPoints / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (inst.accreditationPoints >= 90) Color(0xFF4CAF50) else Color(0xFFD4AF37),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Level & Name
                Text(
                    text = inst.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isUnderConstruction) "Konstruksi: Sisa ${inst.constructionMonthsLeft} Bln" else "${inst.level} | Level ${inst.facilityLevel}/5 | ${inst.buildingGrade}",
                    color = if (isUnderConstruction) Color(0xFFFFB300) else Color.LightGray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(6.dp))

                // Info Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Layanan:", color = Color.Gray, fontSize = 11.sp)
                    Text(inst.serviceType, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ops Bulanan:", color = Color.Gray, fontSize = 11.sp)
                    Text(
                        text = com.example.ui.formatCurrency(opsCost),
                        color = Color(0xFFE57373),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tarif Pasien:", color = Color.Gray, fontSize = 11.sp)
                    Text(
                        text = "${com.example.ui.formatCurrency(inst.monthlyBillPerPatient)}/pasien",
                        color = Color(0xFF81C784),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharityInstitutionCard(
    inst: com.example.data.CharityInstitution,
    endowmentFund: Long,
    onCardClick: () -> Unit
) {
    val isUnderConstruction = inst.constructionLeftMonths > 0
    val opsCost = inst.calculateTotalMonthlyOpsCost()

    val cardColor = Color(0xFF2E1C0F) // warm reddish amber/brown for charity

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Accreditation Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AKREDITASI",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (isUnderConstruction) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE65100).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("KONSTRUKSI", color = Color(0xFFFF9800), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (inst.accreditationPoints >= 90) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "UNGGUL", 
                                color = Color(0xFF4CAF50), 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    } else if (inst.accreditationPoints >= 60) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1976D2).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "BAIK", 
                                color = Color(0xFF2196F3), 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1, softWrap = false
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE65100).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "CUKUP", 
                                color = Color(0xFFFF9800), 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Bold,
                                maxLines = 1, softWrap = false
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { inst.accreditationPoints / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (inst.accreditationPoints >= 90) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Level & Name
                Text(
                    text = inst.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isUnderConstruction) "Konstruksi: Sisa ${inst.constructionLeftMonths} Bln" else "${inst.level} | Level ${inst.facilityLevel}/5 | ${inst.buildingGrade}",
                    color = if (isUnderConstruction) Color(0xFFFFB300) else Color.LightGray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(6.dp))

                // Info Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cakupan Wilayah:", color = Color.Gray, fontSize = 11.sp)
                    Text(inst.scope, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ops Bulanan:", color = Color.Gray, fontSize = 11.sp)
                    Text(
                        text = com.example.ui.formatCurrency(opsCost),
                        color = Color(0xFFE57373),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Penerima Manfaat:", color = Color.Gray, fontSize = 11.sp)
                    Text(
                        text = "${inst.monthlyBeneficiaries}/${inst.maxCapacity} jiwa/bln",
                        color = Color(0xFFFFB74D),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

