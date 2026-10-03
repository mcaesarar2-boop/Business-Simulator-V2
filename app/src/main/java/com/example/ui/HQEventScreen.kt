package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HQEventScreen(
    instanceId: String,
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsState()
    val ownedData = playerState.ownedBusinesses.find { it.instanceId == instanceId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }

    if (ownedData == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Bisnis tidak ditemukan.", color = Color.White)
        }
        return
    }

    val currentHq = ownedData.eoCompanyHqLevel ?: "HOUSE"
    val divisions = ownedData.eoDivisions ?: emptySet()

    // HQ Rules
    val maxDivs = when (currentHq) {
        "HOUSE" -> 3
        "OFFICE" -> 5
        "REGIONAL" -> 7
        else -> 9
    }

    val nextHq = when (currentHq) {
        "HOUSE" -> "OFFICE"
        "OFFICE" -> "REGIONAL"
        "REGIONAL" -> "NATIONAL"
        "NATIONAL" -> "INTERNATIONAL"
        else -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏢 Markas & Divisi Kantor", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main HQ Status Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Home, contentDescription = "HQ Icon", tint = Color(0xFF3B82F6), modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Tingkat Markas Saat Ini", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    viewModel.getHqDisplayName(currentHq),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            getHqDetailedDescription(currentHq),
                            fontSize = 13.sp,
                            color = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Indicator for Divisions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Divisi Dibentuk", fontSize = 13.sp, color = Color.LightGray)
                            Text(
                                "${divisions.size} / $maxDivs",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (divisions.size >= maxDivs) Color(0xFFF43F5E) else Color(0xFF10B981)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = (divisions.size.toFloat() / maxDivs.toFloat()).coerceIn(0f, 1f),
                            color = if (divisions.size >= maxDivs) Color(0xFFF43F5E) else Color(0xFF10B981),
                            trackColor = Color.DarkGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            // HQ Upgrade Action Button Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Upgrade Markas (HQ)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))

                        if (nextHq != null) {
                            val upgradeCost = viewModel.getHqUpgradeCost(nextHq)
                            val hasEnoughCash = ownedData.companyCash >= upgradeCost

                            Column {
                                Text(
                                    "Tingkat Berikutnya: ${viewModel.getHqDisplayName(nextHq)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF3B82F6)
                                )
                                Text(
                                    "Membuka kapasitas divisi menjadi ${getMaxDivisionsForHqDisplay(nextHq)} & mengurangi batas lock divisi.",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val err = viewModel.upgradeEoHq(instanceId)
                                        if (err != null) {
                                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "HQ berhasil diupgrade ke ${viewModel.getHqDisplayName(nextHq)}!",
                                                Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (hasEnoughCash) Color(0xFF3B82F6) else Color.DarkGray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Upgrade")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Upgrade ($${String.format("%,.0f", upgradeCost)})")
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Max", tint = Color(0xFF10B981))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Markas Anda sudah berada di tingkat maksimal!", fontSize = 13.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Divisions list section title
            item {
                Text(
                    "Daftar Divisi Kantor",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Divisions
            val allDivisions = listOf("Sales", "Creative", "Production", "Multimedia", "Talent", "Logistics", "Finance", "Legal", "Marketing")
            items(allDivisions) { div ->
                val isHired = divisions.contains(div)
                val cost = viewModel.getDivisionHiringCost(div)
                val isLocked = isDivisionLockedForHqDisplay(currentHq, div)

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isLocked -> Color(0xFF0F172A).copy(alpha = 0.5f)
                            isHired -> Color(0xFF1E293B)
                            else -> Color(0xFF1E293B).copy(alpha = 0.4f)
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        when {
                            isLocked -> Color.DarkGray.copy(alpha = 0.3f)
                            isHired -> Color(0xFF3B82F6)
                            else -> Color.DarkGray
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(getDivisionIconDisplay(div), fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    div,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLocked) Color.Gray else Color.White,
                                    fontSize = 15.sp
                                )
                                if (isLocked) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFF43F5E).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("🔒 LOCKED", fontSize = 9.sp, color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                getDivisionBenefitDisplay(div),
                                fontSize = 12.sp,
                                color = if (isLocked) Color.Gray.copy(alpha = 0.6f) else Color.Gray
                            )
                        }

                        if (isHired) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("AKTIF", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        } else if (isLocked) {
                            Text(
                                "Locked",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            val hasEnoughCash = ownedData.companyCash >= cost
                            val canHireMore = divisions.size < maxDivs

                            Button(
                                onClick = {
                                    if (!canHireMore) {
                                        Toast.makeText(context, "Kapasitas divisi penuh! Upgrade HQ Anda.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        val err = viewModel.hireEoDivision(instanceId, div)
                                        if (err != null) {
                                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Divisi $div berhasil dibentuk!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasEnoughCash && canHireMore) Color(0xFF3B82F6) else Color.DarkGray
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Bentuk ($${String.format("%,.0f", cost)})", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getHqDetailedDescription(hq: String): String {
    return when (hq) {
        "HOUSE" -> "Rumah Pribadi: Memulai bisnis EO dari garasi rumah. Kapasitas sangat terbatas. Maksimal hanya bisa membentuk 3 divisi dasar (Production, Logistics, Creative, Marketing). Tidak dapat merekrut divisi khusus seperti Sales atau Finance."
        "OFFICE" -> "Kantor Sewaan: Kantor ruko yang representatif untuk operasional harian. Kapasitas bertambah hingga maksimal 5 divisi. Divisi Sales, Finance, dan Multimedia sekarang sudah bisa dibentuk."
        "REGIONAL" -> "Kantor Regional: Gedung kantor regional dengan fasilitas lengkap. Kapasitas bertambah hingga maksimal 7 divisi. Divisi Talent dan Legal sekarang sudah bisa dibentuk."
        "NATIONAL" -> "Kantor Nasional: Markas besar megah skala nasional. Kapasitas bertambah hingga maksimal 9 divisi (seluruh divisi dapat dibentuk tanpa batasan)."
        "INTERNATIONAL" -> "Markas Internasional: Gedung pencakar langit eksklusif yang mengoordinasikan event akbar di seluruh dunia. Semua fitur terbuka maksimal!"
        else -> ""
    }
}

fun getMaxDivisionsForHqDisplay(hq: String): Int {
    return when (hq) {
        "HOUSE" -> 3
        "OFFICE" -> 5
        "REGIONAL" -> 7
        else -> 9
    }
}

fun isDivisionLockedForHqDisplay(hq: String, div: String): Boolean {
    return when (hq) {
        "HOUSE" -> !listOf("Production", "Logistics", "Creative", "Marketing").contains(div)
        "OFFICE" -> !listOf("Production", "Logistics", "Creative", "Marketing", "Sales", "Finance", "Multimedia").contains(div)
        "REGIONAL" -> !listOf("Production", "Logistics", "Creative", "Marketing", "Sales", "Finance", "Multimedia", "Talent", "Legal").contains(div)
        else -> false
    }
}

fun getDivisionIconDisplay(div: String): String {
    return when (div) {
        "Sales" -> "💼"
        "Creative" -> "🎨"
        "Production" -> "⚙️"
        "Multimedia" -> "🎬"
        "Talent" -> "🎤"
        "Logistics" -> "📦"
        "Finance" -> "💰"
        "Legal" -> "⚖️"
        "Marketing" -> "📢"
        else -> "🏢"
    }
}

fun getDivisionBenefitDisplay(div: String): String {
    return when (div) {
        "Sales" -> "Meningkatkan nilai fee proyek klien sebesar +15%"
        "Creative" -> "Meningkatkan kualitas perencanaan awal event"
        "Production" -> "Memberikan bonus +10% profit margin pada setiap event"
        "Multimedia" -> "Membantu menaikkan rating event pasca-produksi"
        "Talent" -> "Mengurangi biaya artis/talent eksternal sebesar 15%"
        "Logistics" -> "Membuka sewa aset lebih murah dan cepat"
        "Finance" -> "Bonus +5% profit margin serta diskon pajak usaha"
        "Legal" -> "Mengurangi dampak denda penalti insiden sebesar 50%"
        "Marketing" -> "Meningkatkan perolehan reputasi dari event sukses sebesar 20%"
        else -> ""
    }
}
