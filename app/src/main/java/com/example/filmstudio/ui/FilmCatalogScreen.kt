package com.example.filmstudio.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.data.CoProductionFundingScheme
import com.example.data.MovieProject
import com.example.ui.formatCurrencyRingkas
import com.example.viewmodel.GameViewModel

/**
 * Film IP Catalog and Historical Archives Screen.
 * Allows managing archived theatrical runs, licensing to external/internal streaming services,
 * or liquidating IP assets.
 */
@Composable
fun FilmCatalogScreen(
    navController: NavHostController,
    viewModel: GameViewModel,
    instanceId: String
) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()
    val ownedData = playerState.ownedBusinesses.find { it.instanceId == instanceId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }

    if (ownedData == null) {
        navController.popBackStack()
        return
    }

    val finishedMovies = ownedData.projectHistory.filter { it.status == "FINISHED" }.reversed()

    // Detect all owned OTT platforms (standalone or under any holding company)
    val ownedOttPlatforms = remember(playerState) {
        (playerState.ownedBusinesses.filter { it.catalogId == "streaming_service" } +
            playerState.holdingCompanies.flatMap { it.subsidiaries }.filter { it.catalogId == "streaming_service" }).distinctBy { it.instanceId }
    }

    var showStreamingDialog by remember { mutableStateOf<MovieProject?>(null) }

    if (showStreamingDialog != null) {
        val proj = showStreamingDialog!!
        val grossD = proj.currentRevenue.toDouble()
        val score = proj.reviewScore
        val baseFee = ((grossD * 0.02) * (score / 100.0)).toLong()

        val netflikFee = (baseFee * 1.2).toLong()
        val disnetFee = (baseFee * 0.9).toLong()
        val lokalFee = (baseFee * 0.5).toLong()

        AlertDialog(
            onDismissRequest = { showStreamingDialog = null },
            containerColor = Color(0xFF191B26),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            title = { Text("🍿 Lisensi Streaming: ${proj.title}", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Pilih platform layanan streaming untuk menayangkan film ini:", color = Color(0xFFB0B5C4), fontSize = 12.sp)

                    // 1. IN-HOUSE OTT PLATFORMS (MILIK SENDIRI)
                    Text(
                        "PLATFORM OTT KITA SENDIRI",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    if (ownedOttPlatforms.isNotEmpty()) {
                        ownedOttPlatforms.forEach { ottBiz ->
                            Card(
                                onClick = {
                                    viewModel.distributeMovieToInHouseOtt(instanceId, proj.title, ottBiz.instanceId)
                                    showStreamingDialog = null
                                },
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E2238)),
                                border = BorderStroke(1.5.dp, Color(0xFF00E5FF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(ottBiz.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Surface(
                                            color = Color(0xFFE50914),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "IN-HOUSE OTT",
                                                color = Color.White,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("Penayangan Eksklusif Mandiri (Permanen)", color = Color(0xFF80D8FF), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Nilai Kontrak: $0 / bln (Bebas Royalti In-House)",
                                        color = Color(0xFFFFD54F),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        "✨ Seluruh penonton & lonjakan traffic mengalir mendongkrak keuntungan platform OTT Anda!",
                                        color = Color(0xFFB2EBF2),
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Belum punya platform OTT (Disabled card)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E212E).copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Platform OTT Sendiri (Terkunci)", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Anda belum mendirikan unit bisnis Streaming Service / OTT Platform. Dirikan unit bisnis OTT untuk menyiarkan film studio sendiri secara mandiri.",
                                    color = Color(0xFF8E95A5),
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    // 2. MITRA STREAMING EKSTERNAL
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "MITRA PLATFORM EKSTERNAL",
                        color = Color(0xFF9E9E9E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    // Options
                    Card(
                        onClick = {
                            viewModel.startStreamingLicense(instanceId, proj.title, "Netflik Global", netflikFee, 12)
                            showStreamingDialog = null
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Netflik Global", color = Color.Red, fontWeight = FontWeight.Bold)
                            Text("Sewa 1 Tahun (12 bln)", color = Color.LightGray, fontSize = 12.sp)
                            Text("+ $${formatCurrencyRingkas(netflikFee, useShortFormat)} / bln", color = Color(0xFF00FF00), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        }
                    }

                    Card(
                        onClick = {
                            viewModel.startStreamingLicense(instanceId, proj.title, "Disnet+", disnetFee, 36)
                            showStreamingDialog = null
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.Blue.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, Color.Blue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Disnet+", color = Color(0xFF44AAFF), fontWeight = FontWeight.Bold)
                            Text("Sewa 3 Tahun (36 bln)", color = Color.LightGray, fontSize = 12.sp)
                            Text("+ $${formatCurrencyRingkas(disnetFee, useShortFormat)} / bln", color = Color(0xFF00FF00), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        }
                    }

                    Card(
                        onClick = {
                            viewModel.startStreamingLicense(instanceId, proj.title, "LokalFlix", lokalFee, 6)
                            showStreamingDialog = null
                        },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFAA00).copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, Color(0xFFFFAA00)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("LokalFlix", color = Color(0xFFFFAA00), fontWeight = FontWeight.Bold)
                            Text("Sewa 6 Bulan (6 bln)", color = Color.LightGray, fontSize = 12.sp)
                            Text("+ $${formatCurrencyRingkas(lokalFee, useShortFormat)} / bln", color = Color(0xFF00FF00), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStreamingDialog = null }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kembali")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("🎬 Katalog IP & Histori Film", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        if (finishedMovies.isEmpty()) {
            Text("Belum ada film yang selesai tayang.", color = Color.Gray, modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(finishedMovies) { proj ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(1.dp, if (proj.isCoProd) Color(0xFFFFC727).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    proj.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                if (proj.isCoProd) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color(0xFFFFC727).copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, Color(0xFFFFC727)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "🤝 Co-Production",
                                                color = Color(0xFFFFC727),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Co-Production details box if co-produced
                            if (proj.isCoProd) {
                                Spacer(modifier = Modifier.height(6.dp))
                                val meta = proj.coProductionMeta
                                val fundingLabel = when (meta?.fundingType) {
                                    CoProductionFundingScheme.FULL_CREATOR.name, "FULL_CREATOR" -> "100% Kas Creator"
                                    CoProductionFundingScheme.CREATOR_70_30.name, "CREATOR_70_30" -> "70% Kas Creator / 30% Kas Studio"
                                    CoProductionFundingScheme.JOINT_VENTURE_50_50.name, "JOINT_VENTURE_50_50" -> "Joint Venture (50/50)"
                                    CoProductionFundingScheme.STUDIO_30_70.name, "STUDIO_30_70" -> "30% Kas Creator / 70% Kas Studio"
                                    CoProductionFundingScheme.FULL_STUDIO.name, "FULL_STUDIO" -> "100% Kas Studio"
                                    else -> meta?.fundingType ?: "Co-Production"
                                }
                                val profitShareLabel = when (meta?.fundingType) {
                                    CoProductionFundingScheme.FULL_CREATOR.name, "FULL_CREATOR" -> "100% Creator"
                                    CoProductionFundingScheme.CREATOR_70_30.name, "CREATOR_70_30" -> "70% Creator / 30% Studio"
                                    CoProductionFundingScheme.STUDIO_30_70.name, "STUDIO_30_70" -> "30% Creator / 70% Studio"
                                    CoProductionFundingScheme.FULL_STUDIO.name, "FULL_STUDIO" -> "100% Studio"
                                    else -> "50% Creator / 50% Studio"
                                }
                                Surface(
                                    color = Color(0xFF262012),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.8.dp, Color(0xFF664D00)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "✨ Hasil Kolaborasi: Content Creator × Studio Film",
                                            color = Color(0xFFFFE082),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Skema Dana: $fundingLabel | Bagi Hasil: $profitShareLabel",
                                            color = Color(0xFFFFCA28),
                                            fontSize = 9.5.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            val releaseText = if (proj.releaseMonth != null && proj.releaseYear != null) " | Rilis: Bln ${proj.releaseMonth}, ${proj.releaseYear}" else ""
                            val focusText = when (proj.productionFocus) {
                                "KUALITAS" -> " 🌟 (Fokus Kualitas)"
                                "MAHAKARYA" -> " 🏆 (Ambisi Mahakarya)"
                                else -> ""
                            }
                            Text("Score: ${proj.reviewScore}/100$focusText | ${proj.distributionScale}$releaseText", fontSize = 12.sp, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Prod. Budget:", color = Color.Gray)
                                Text(formatCurrencyRingkas(proj.budget, useShortFormat), color = Color.LightGray)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                val pb = proj.promoBudget
                                Text("Promo Budget:", color = Color.Gray)
                                Text(formatCurrencyRingkas(pb, useShortFormat), color = Color.LightGray)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Cost:", fontWeight = FontWeight.SemiBold, color = Color.LightGray)
                                Text(formatCurrencyRingkas(proj.budget + proj.promoBudget, useShortFormat), fontWeight = FontWeight.SemiBold, color = Color.LightGray)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Gross:", color = Color.Gray)
                                Text(formatCurrencyRingkas(proj.currentRevenue, useShortFormat), color = Color(0xFF00FF00))
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            val isProfit = proj.netProfit >= 0
                            val profitLabel = if (isProfit) "UNTUNG (Profit: +${formatCurrencyRingkas(proj.netProfit, useShortFormat)})" else "RUGI (Loss: ${formatCurrencyRingkas(proj.netProfit, useShortFormat)})"
                            Text(profitLabel, fontWeight = FontWeight.Bold, color = if (isProfit) Color(0xFF4CAF50) else Color(0xFFFF5555))

                            Spacer(modifier = Modifier.height(16.dp))

                            if (proj.licenseRemainingMonths != null && proj.licenseRemainingMonths!! > 0) {
                                val isInHouseOtt = (proj.licenseMonthlyFee ?: 0L) == 0L || (proj.licenseeName?.contains("In-House", ignoreCase = true) == true) || (proj.licenseeName?.contains("OTT", ignoreCase = true) == true)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = if (isInHouseOtt) Color(0xFF0D1E30) else Color(0xFF004400)),
                                    border = BorderStroke(1.dp, if (isInHouseOtt) Color(0xFF00E5FF) else Color(0xFF00FF00)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        if (isInHouseOtt) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("🍿 Disiarkan di: ${proj.licenseeName ?: "Platform OTT"}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Surface(
                                                    color = Color(0xFFE50914),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        "IN-HOUSE OTT",
                                                        color = Color.White,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("Eksklusif Platform OTT Sendiri | Bebas Royalti ($0/bln) • Penonton & viewer spike mengalir ke platform OTT Anda.", color = Color(0xFF80D8FF), fontSize = 11.sp)
                                        } else {
                                            Text("🟢 Disewa oleh: ${proj.licenseeName}", color = Color.White, fontWeight = FontWeight.Bold)
                                            Text("Sisa Kontrak: ${proj.licenseRemainingMonths} Bulan | Pendapatan: +${formatCurrencyRingkas(proj.licenseMonthlyFee ?: 0L, useShortFormat)}/bln", color = Color(0xFF00FF00), fontSize = 12.sp)
                                        }
                                    }
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val sellPrice = (proj.currentRevenue * 0.75).toLong()
                                    Button(
                                        onClick = {
                                            viewModel.sellMovieIp(instanceId, proj.title, sellPrice)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color(0xFFFF5555))
                                    ) {
                                        val sellLabel = if (proj.isCoProd) {
                                            "Jual IP (Bagi Hasil)\n(+${formatCurrencyRingkas(sellPrice, useShortFormat)})"
                                        } else {
                                            "Jual IP\n(+${formatCurrencyRingkas(sellPrice, useShortFormat)})"
                                        }
                                        Text(sellLabel, textAlign = TextAlign.Center, fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = { showStreamingDialog = proj },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0055FF).copy(alpha = 0.2f), contentColor = Color(0xFF44AAFF))
                                    ) {
                                        Text("Tawarkan\nLisensi Streaming", textAlign = TextAlign.Center, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
