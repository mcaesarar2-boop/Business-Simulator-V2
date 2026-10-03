package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.data.FoundationEntity
import com.example.data.FoundationType
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoundationDashboardScreen(navController: NavHostController, viewModel: GameViewModel) {
    val playerState by viewModel.playerState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var showEstablishDialog by remember { mutableStateOf(false) }
    var newFoundationName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(FoundationType.EDUCATION) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var foundationToDelete by remember { mutableStateOf<com.example.data.FoundationEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Private Foundation & Legacy", fontWeight = FontWeight.Bold, color = Color.White) },
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
            // Stats Header Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏛️ NILAI REPUTASI & WARISAN",
                            color = Color(0xFFD4AF37),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${playerState.foundationLegacyPoints} pts",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Legacy / Prestige Points",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Kas Pribadi (CEO)", color = Color.LightGray, fontSize = 11.sp)
                                Text(
                                    text = com.example.ui.formatCurrency(playerState.privateBalance),
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Institusi Aktif", color = Color.LightGray, fontSize = 11.sp)
                                val legalizedCount = playerState.foundations.count { it.isLegalized }
                                Text(
                                    text = "$legalizedCount / ${playerState.foundations.size}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Call to Action: Establish Foundation Button
            item {
                Button(
                    onClick = {
                        newFoundationName = ""
                        errorMessage = null
                        showEstablishDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddHomeWork, contentDescription = null, tint = Color(0xFF0F1E36))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dirikan Yayasan Baru", color = Color(0xFF0F1E36), fontWeight = FontWeight.Bold)
                }
            }

            // Foundations Section
            item {
                Text(
                    text = "Daftar Yayasan Pribadi",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            if (playerState.foundations.isEmpty()) {
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
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Anda belum mendirikan yayasan nirlaba.",
                                color = Color.LightGray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Salurkan kekayaan Anda untuk membangun pendidikan, kesehatan, dan aksi kemanusiaan demi mengabadikan nama Anda.",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(playerState.foundations) { foundation ->
                    FoundationCard(
                        foundation = foundation,
                        onClick = {
                            navController.navigate("private_foundation_detail/${foundation.id}")
                        },
                        onDeleteClick = {
                            foundationToDelete = foundation
                            showDeleteConfirmDialog = true
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Dialog: Establish New Foundation
    if (showEstablishDialog) {
        AlertDialog(
            onDismissRequest = { showEstablishDialog = false },
            title = { Text("Dirikan Yayasan Baru", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newFoundationName,
                        onValueChange = { newFoundationName = it },
                        label = { Text("Nama Yayasan / Institusi") },
                        placeholder = { Text("Cth: Yayasan Bakti Mulia") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Pilih Fokus & Jenis Gerakan:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    
                    FoundationType.values().forEach { type ->
                        val isSelected = selectedType == type
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedType = type }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFD4AF37) else Color.Gray.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF10223D) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = type.label,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFFFFD700) else Color.White,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Masa Legalitas: ${type.setupMonths} Bulan",
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = com.example.ui.formatCurrency(type.legalCost),
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFoundationName.trim().isEmpty()) {
                            errorMessage = "Nama yayasan tidak boleh kosong!"
                            return@Button
                        }
                        if (playerState.privateBalance < selectedType.legalCost) {
                            errorMessage = "Kas Pribadi Anda tidak cukup! Dibutuhkan ${com.example.ui.formatCurrency(selectedType.legalCost)}"
                            return@Button
                        }
                        val success = viewModel.createPrivateFoundation(newFoundationName.trim(), selectedType)
                        if (success) {
                            showEstablishDialog = false
                        } else {
                            errorMessage = "Gagal mendirikan yayasan."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                ) {
                    Text("Dirikan & Bayar", color = Color(0xFF0F1E36), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEstablishDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        val f = foundationToDelete
        if (f != null) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("Hibahkan & Tutup Yayasan?", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Text(
                        "Apakah Anda yakin ingin menghibahkan yayasan '${f.name}' ke pemerintah? Seluruh Dana Abadi dan aset di dalamnya akan dilepaskan secara permanen. Operasional bulanan akan dihentikan.",
                        color = Color.LightGray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletePrivateFoundation(f.id)
                            showDeleteConfirmDialog = false
                            foundationToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Ya, Hibahkan", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Batal", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun FoundationCard(foundation: FoundationEntity, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    val icon = when (foundation.type) {
        FoundationType.EDUCATION -> Icons.Default.School
        FoundationType.HEALTHCARE -> Icons.Default.LocalHospital
        FoundationType.HUMANITARIAN -> Icons.Default.VolunteerActivism
    }
    val iconColor = when (foundation.type) {
        FoundationType.EDUCATION -> Color(0xFF2196F3)
        FoundationType.HEALTHCARE -> Color(0xFFE53935)
        FoundationType.HUMANITARIAN -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101C2E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(iconColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = foundation.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = foundation.type.label,
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (foundation.isLegalized) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("RESMI", color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE65100).copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("LEGALITAS", color = Color(0xFFFF9800), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { onDeleteClick() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hibahkan",
                            tint = Color.Red.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))

            if (!foundation.isLegalized) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pengurusan Akta Notaris & Legalitas...", color = Color.Gray, fontSize = 11.sp)
                        Text("Sisa ${foundation.constructionMonthsLeft} Bulan", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val progress = 1f - (foundation.constructionMonthsLeft.toFloat() / foundation.type.setupMonths.toFloat())
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFFFF9800),
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dana Abadi (Endowment)", color = Color.Gray, fontSize = 11.sp)
                        Text(
                            text = com.example.ui.formatCurrency(foundation.endowmentFund),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Fasilitas Beroperasi", color = Color.Gray, fontSize = 11.sp)
                        val active = foundation.facilities.count { it.isOperational }
                        Text(
                            text = "$active / ${foundation.facilities.size}",
                            color = Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
