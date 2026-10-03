package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ThemeParkEngine
import com.example.data.*
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacilityCatalogScreen(
    navController: NavController,
    viewModel: GameViewModel,
    instanceId: String,
    branchId: String
) {
    val playerState by viewModel.playerState.collectAsState()
    
    var owned = playerState.ownedBusinesses.find { it.instanceId == instanceId }
    if (owned == null) {
        for (holding in playerState.holdingCompanies) {
            owned = holding.subsidiaries.find { it.instanceId == instanceId }
            if (owned != null) break
        }
    }
    
    if (owned == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bisnis Tidak Ditemukan", color = Color.White)
        }
        return
    }

    val branch = owned.themeParkBranches.find { it.id == branchId }
    if (branch == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Cabang Lahan Tidak Ditemukan", color = Color.White)
        }
        return
    }

    val nFormat = NumberFormat.getInstance()
    val availableCash = owned.companyCash
    val catalog = ThemeParkEngine.facilitiesCatalog

    var showSuccessDialog by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Konstruksi Fasilitas Publik", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text(branch.customName ?: branch.locationName, color = Color.Gray, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF151515))
            )
        },
        containerColor = Color(0xFF101010)
    ) { padValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🛋️", fontSize = 24.sp)
                            Text("Manajemen Sarana Penunjang (Amenities)", color = Color(0xFF00FFCC), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Fasilitas Publik bukanlah wahana atraksi langsung, melainkan prasarana krusial untuk menaikkan selera belanja jajan (F&B) pengunjung, daya pikat taman, serta menambah gengsi taman hiburan Anda.",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color.Gray.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Saldo Kas Bisnis:", color = Color.Gray, fontSize = 13.sp)
                            Text("$${nFormat.format(availableCash)}", color = Color(0xFF00FF00), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            item {
                Text(
                    "KATALOG PRASARANA & AMENITIES",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(catalog) { entry ->
                val canAfford = availableCash >= entry.buildCost
                val countBuilt = (branch.facilities ?: emptyList()).count { it.catalogId == entry.catalogId }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (countBuilt > 0) Color(0xFF00FFCC).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (countBuilt > 0) Color(0xFF122220) else Color(0xFF1A1A1A)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color.White.copy(alpha = 0.05f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(entry.icon, fontSize = 22.sp)
                                }
                                Column {
                                    Text(entry.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    if (countBuilt > 0) {
                                        Text("Sudah Terbaangun: $countBuilt unit", color = Color(0xFF00FFCC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            entry.description,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Spesifikasi / Buff
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("BIAYA PEMBANGUNAN", color = Color.Gray, fontSize = 9.sp)
                                Text("$${nFormat.format(entry.buildCost)}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BIAYA RAWAT / BLN", color = Color.Gray, fontSize = 9.sp)
                                Text("$${nFormat.format(entry.maintenanceCost)}", color = Color(0xFFFF5555), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("PASOKAN BUFF", color = Color.Gray, fontSize = 9.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("+${entry.fnbBoostPercent}% Jajan", color = Color(0xFF00FF00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(" | ", color = Color.Gray, fontSize = 10.sp)
                                    Text("+${entry.appealBoost} Poin", color = Color(0xFF00FF00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (!canAfford) {
                                    showErrorDialog = "Kas divisi Anda ($${nFormat.format(availableCash)}) tidak mencukupi untuk mendirikan ${entry.name} seharga $${nFormat.format(entry.buildCost)}."
                                } else {
                                    val success = viewModel.buildThemeParkFacility(
                                        businessInstanceId = instanceId,
                                        branchId = branchId,
                                        catalogEntry = entry
                                    )
                                    if (success) {
                                        showSuccessDialog = entry.name
                                    } else {
                                        showErrorDialog = "Terjadi galat tak dikenal saat memproses transaksi."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAfford) Color(0xFF00FFCC) else Color(0xFF333333),
                                contentColor = if (canAfford) Color.Black else Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Build, contentDescription = "Bangun", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bangun Fasilitas", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog != null) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = null },
            title = { Text("Pembangunan Selesai! 🎉", color = Color.White) },
            text = { Text("Fasilitas ${showSuccessDialog} berhasil didirikan dan diintegrasikan ke sistem operasional taman hiburan Anda secara real-time.", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessDialog = null
                }) {
                    Text("Alhamdulillah", color = Color(0xFF00FFCC))
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    if (showErrorDialog != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = null },
            title = { Text("Pembangunan Gagal!", color = Color.White) },
            text = { Text(showErrorDialog ?: "", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = null }) {
                    Text("Tutup", color = Color.Red)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}
