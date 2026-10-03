package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.ThemeParkEngine
import com.example.viewmodel.AdPackage
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingAgencyScreen(
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

    // Hitung daya tarik wahana aktif untuk mencari idealPrice yang tepat
    val totalAppeal = branch.rides.filter { !it.isConstructing }.sumOf { it.baseMonthlyVisitors / 100 }
    val idealPrice = ThemeParkEngine.calculateIdealPrice(totalAppeal.toDouble())
    val packages = ThemeParkEngine.calculateAdPackages(branch, idealPrice)

    val nFormat = NumberFormat.getInstance()
    var showSuccessDialog by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biro Iklan & Marketing", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
            )
        },
        containerColor = Color(0xFF121212)
    ) { padValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🏢 Premium Promotion Partner", color = Color(0xFFFFD700), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Tandatangani kontrak jangka panjang dengan Biro Iklan profesional untuk melipatgandakan popularitas wahana Anda. Nilai kontrak dihitung dinamis berdasarkan Harga Ideal ($${nFormat.format(idealPrice)}) dan kapasitas lahan.",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color.Gray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Dana Tersedia Divisi:", color = Color.Gray, fontSize = 13.sp)
                            Text("$${nFormat.format(owned.companyCash)}", color = Color(0xFF00FF00), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        if (branch.activeAdName != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Kontrak Aktif Saat Ini:", color = Color.Gray, fontSize = 13.sp)
                                Text("${branch.activeAdName} (${branch.adMonthsLeft} Bln Tersisa)", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "PILIH PAKET PROMOSI EXCLUSIVE",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(packages) { ad ->
                val pricePerMonth = ad.totalPrice / ad.durationMonths
                val isCurrentAd = branch.activeAdName == ad.name
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = if (isCurrentAd) 2.dp else 1.dp,
                            color = if (isCurrentAd) Color(0xFFFFD700) else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentAd) Color(0xFF2C2405) else Color(0xFF1E1E1E)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(ad.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    if (isCurrentAd) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text("Aktif", fontSize = 10.sp, color = Color.Black) },
                                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFFFD700))
                                        )
                                    }
                                }
                                Text("Durasi Kontrak: ${ad.durationMonths} Bulan (${ad.durationMonths / 12} Tahun)", color = Color.Gray, fontSize = 12.sp)
                            }
                            Text("+${ad.boostPercentage}% Boost", color = Color(0xFF00FF00), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Biaya Upfront (Membayar di Muka):",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "$${nFormat.format(ad.totalPrice)}",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            "Setara $${nFormat.format(pricePerMonth)} / bulan",
                            color = Color(0xFF00FF00),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (owned!!.companyCash < ad.totalPrice) {
                                    showErrorDialog = "Saldo divisi tidak mencukupi untuk menandatangani kontrak upfront sebesar $${nFormat.format(ad.totalPrice)}."
                                } else {
                                    val success = viewModel.purchaseAdPackage(
                                        businessInstanceId = instanceId,
                                        branchId = branchId,
                                        adName = ad.name,
                                        durationMonths = ad.durationMonths,
                                        boostMultiplier = ad.boostMultiplier,
                                        cost = ad.totalPrice
                                    )
                                    if (success) {
                                        showSuccessDialog = ad.name
                                    } else {
                                        showErrorDialog = "Terjadi kegagalan memproses kontrak iklan."
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrentAd) Color(0xFFFFF2A1) else Color(0xFF333333),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (isCurrentAd) "Perpanjang Kontrak" else "Tanda Tangani Kontrak",
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrentAd) Color.Black else Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog != null) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = null },
            title = { Text("Kontrak Sukses Ditandatangani! ⚡", color = Color.White) },
            text = { Text("Paket Iklan ${showSuccessDialog} berhasil diaktifkan untuk taman hiburan ini! Menjamin peningkatan demand pengunjung sebesar ${if (showSuccessDialog == "Bronze") "30%" else if (showSuccessDialog == "Silver") "45%" else if (showSuccessDialog == "Gold") "65%" else if (showSuccessDialog == "Platinum") "75%" else "100%"} upfront.", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = { 
                    showSuccessDialog = null
                    navController.popBackStack()
                }) {
                    Text("Luar Biasa", color = Color(0xFFFFD700))
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    if (showErrorDialog != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = null },
            title = { Text("Opps, Gagal Menandatangani Kontrak!", color = Color.White) },
            text = { Text(showErrorDialog ?: "", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = null }) {
                    Text("Kembali", color = Color.Red)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}
