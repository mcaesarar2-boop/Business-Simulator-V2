package com.example.filmstudio.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MovieProject
import com.example.data.OwnedBusiness
import com.example.domain.subsystems.creative.FilmTimelineEngine
import com.example.ui.FilmTimelineDialog
import com.example.ui.formatCurrencyRingkas

/**
 * Card section displaying active movie productions and theatrical screenings.
 */
@Composable
fun FilmActiveProjectsCard(
    owned: OwnedBusiness,
    useShortFormat: Boolean,
    currentMonth: Int,
    currentYear: Int,
    onPolish: (title: String, budgetCost: Long, extraMonths: Int) -> Unit = { _, _, _ -> },
    onSchedule: (title: String, schedStr: String) -> Unit = { _, _ -> },
    onCancel: (title: String, refundAmount: Long) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val activeMovies = owned.projectHistory.filter { it.status != "FINISHED" }
    val type = owned.studioType ?: "LIVE_ACTION"

    var activePolishProject by remember { mutableStateOf<MovieProject?>(null) }
    var activeScheduleProject by remember { mutableStateOf<MovieProject?>(null) }
    var activeTimelineProject by remember { mutableStateOf<MovieProject?>(null) }
    var filmToCancel by remember { mutableStateOf<MovieProject?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }

    if (activeMovies.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🎬 Sedang Berjalan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Text(
                        "${activeMovies.size} Proyek Aktif",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.LightGray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            activeMovies.forEach { proj ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Top Row: Title, Format Tag, Status & Cancel Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = proj.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color.White.copy(alpha = 0.08f)
                                    ) {
                                        Text(proj.filmFormat, fontSize = 9.5.sp, color = Color.LightGray, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp))
                                    }
                                    if (proj.genres.isNotEmpty()) {
                                        Text(
                                            proj.genres.take(2).joinToString(", "),
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (proj.status == "IN_PRODUCTION") {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Budget", fontSize = 10.sp, color = Color.Gray)
                                        Text(
                                            formatCurrencyRingkas(proj.budget, useShortFormat),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.error,
                                            maxLines = 1
                                        )
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total Box Office", fontSize = 9.5.sp, color = Color.Gray)
                                        Text(
                                            formatCurrencyRingkas(proj.currentRevenue, useShortFormat),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF4CAF50),
                                            maxLines = 1
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { filmToCancel = proj; showCancelDialog = true },
                                    modifier = Modifier
                                        .padding(start = 2.dp)
                                        .size(28.dp)
                                        .background(Color.Red.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Batalkan",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Middle: Phase Information and Progress
                        if (proj.status == "IN_PRODUCTION") {
                            val isAnimStudio = (type == "ANIMATION")
                            val totalMonths = if (proj.totalProductionMonths > 0) proj.totalProductionMonths else maxOf(proj.productionDelayMonths, 1)
                            val snapshot = FilmTimelineEngine.getProgress(
                                isAnimation = isAnimStudio,
                                totalMonths = totalMonths,
                                remainingMonths = proj.productionDelayMonths
                            )

                            when {
                                proj.isAwaitingRelease -> {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFFB300).copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("⏳ Menunggu Rilis Bioskop:", fontSize = 11.5.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.SemiBold)
                                            Text(proj.scheduledReleaseDate ?: "Segera", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD54F))
                                        }
                                    }
                                }
                                proj.isQcPhase -> {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFF4081).copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("🔍 Fase QC Internal", fontSize = 11.5.sp, color = Color(0xFFFF80AB), fontWeight = FontWeight.Bold)
                                            Text("Skor Film: ${proj.internalScore}/100", fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF80AB))
                                        }
                                    }
                                }
                                proj.productionPhase == "ANTREAN" && proj.scheduledMonth != null && proj.scheduledYear != null -> {
                                    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
                                    val mIndex = (proj.scheduledMonth - 1).coerceIn(0, 11)
                                    val vYear = proj.scheduledYear + 2019
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFFD54F).copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.3f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("⏳ Menunggu Jadwal Garap:", fontSize = 11.5.sp, color = Color(0xFFFFD54F))
                                            Text("${monthNames[mIndex]} $vYear", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                                        }
                                    }
                                }
                                else -> {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = proj.productionPhase,
                                                fontSize = 12.sp,
                                                color = Color(0xFF00E5FF),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${snapshot.progressPercent}% Selesai",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF00E5FF)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(5.dp))
                                        LinearProgressIndicator(
                                            progress = { snapshot.progressPercent / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = Color(0xFF00E5FF),
                                            trackColor = Color.White.copy(alpha = 0.1f)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Sisa pengerjaan: ${proj.productionDelayMonths} bulan (${snapshot.universalPhase.codeLabel})",
                                                fontSize = 10.5.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.weight(1f, fill = false),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (!proj.scheduledReleaseDate.isNullOrEmpty()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFFFD54F).copy(alpha = 0.12f),
                                                    border = BorderStroke(0.5.dp, Color(0xFFFFD54F).copy(alpha = 0.4f))
                                                ) {
                                                    Text(
                                                        text = "🎬 Target Tayang: ${proj.scheduledReleaseDate}",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFFFFD54F),
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            val totalTheatrical = proj.totalTheatricalMonths ?: if (proj.distributionScale == "Global") 5 else 3
                            val passedMonths = (totalTheatrical - proj.remainingMonths).coerceIn(1, totalTheatrical)
                            val progressPercent = ((passedMonths.toFloat() / totalTheatrical.toFloat()) * 100).toInt().coerceIn(1, 100)

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "🍿 Tayang di Bioskop",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                                            border = BorderStroke(0.5.dp, Color(0xFF4CAF50).copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "Bulan ke-$passedMonths / $totalTheatrical",
                                                fontSize = 9.5.sp,
                                                color = Color(0xFF81C784),
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    if (proj.lastMonthRevenue > 0L) {
                                        Text(
                                            text = "+${formatCurrencyRingkas(proj.lastMonthRevenue, useShortFormat)} bln ini",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF81C784)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                LinearProgressIndicator(
                                    progress = { progressPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF4CAF50),
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⭐ Skor Ulasan: ${proj.reviewScore}/100",
                                        fontSize = 10.5.sp,
                                        color = Color.LightGray
                                    )
                                    Text(
                                        text = if (proj.remainingMonths == 0) "🏁 Bulan Terakhir Tayang" else "⏳ Sisa Tayang: ${proj.remainingMonths} bulan",
                                        fontSize = 10.sp,
                                        color = if (proj.remainingMonths <= 1) Color(0xFFFFB300) else Color.LightGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Detail Timeline Button
                        if (proj.status == "IN_PRODUCTION") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { activeTimelineProject = proj },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp),
                                shape = RoundedCornerShape(9.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00E5FF).copy(alpha = 0.14f),
                                    contentColor = Color(0xFF80DEEA)
                                ),
                                border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "📋 Detail Timeline Produksi",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF80DEEA)
                                    )
                                }
                            }
                        }

                        // QC action row if in QC phase
                        if (proj.isQcPhase) {
                            Spacer(Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { activePolishProject = proj },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(9.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Poles Film", fontSize = 11.5.sp, textAlign = TextAlign.Center)
                                }
                                Button(
                                    onClick = { activeScheduleProject = proj },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(9.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
                                ) {
                                    Text("Jadwalkan Rilis", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Cancellation Dialog
    if (showCancelDialog && filmToCancel != null) {
        val totalInvested = (filmToCancel!!.budget + filmToCancel!!.promoBudget)
        val isScreening = filmToCancel?.status == "IN_THEATERS"

        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(if (isScreening) "Tarik Film dari Bioskop?" else "Batalkan Proyek Film?") },
            text = {
                Text(
                    if (isScreening) {
                        "Apakah Anda yakin ingin menarik paksa film '${filmToCancel!!.title}' dari peredaran? Pendapatan akan langsung dihentikan dan TIDAK ADA pengembalian dana (Hangus/Rugi Total)."
                    } else {
                        "Apakah Anda yakin ingin membatalkan proyek '${filmToCancel!!.title}' secara permanen? Karena proses sudah berjalan, dana produksi dan promosi hanya bisa ditarik kembali sebagian (30% - 50%)."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isScreening) {
                            onCancel(filmToCancel!!.title, 0L)
                            Toast.makeText(context, "Film ditarik dari bioskop. Tidak ada pengembalian dana.", Toast.LENGTH_LONG).show()
                        } else {
                            val refundPercentage = (30..50).random() / 100.0
                            val refundAmount = (totalInvested * refundPercentage).toLong()
                            onCancel(filmToCancel!!.title, refundAmount)
                            Toast.makeText(context, "Proyek dibatalkan. Dana sebesar ${formatCurrencyRingkas(refundAmount, false)} berhasil diselamatkan.", Toast.LENGTH_LONG).show()
                        }
                        showCancelDialog = false
                        filmToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text(if (isScreening) "Tarik Film" else "Hapus Proyek") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCancelDialog = false }) { Text("Batal") }
            }
        )
    }

    // Polish Dialog
    var pCost by remember { mutableStateOf(0L) }
    var eMonths by remember { mutableStateOf(0) }

    LaunchedEffect(activePolishProject) {
        if (activePolishProject != null) {
            val p = activePolishProject!!
            pCost = (p.budget * (10..20).random() / 100).toLong()
            eMonths = (2..12).random()
        }
    }

    if (activePolishProject != null) {
        AlertDialog(
            onDismissRequest = { activePolishProject = null },
            title = { Text("Poles Film: ${activePolishProject!!.title}") },
            text = { Text("Suntik dana tambahan ${formatCurrencyRingkas(pCost, useShortFormat)} untuk memoles film dan menambah waktu produksi $eMonths bulan?") },
            confirmButton = {
                Button(onClick = {
                    onPolish(activePolishProject!!.title, pCost, eMonths)
                    activePolishProject = null
                }) { Text("Konfirmasi") }
            },
            dismissButton = {
                OutlinedButton(onClick = { activePolishProject = null }) { Text("Batal") }
            }
        )
    }

    // Release Schedule Dialog
    if (activeScheduleProject != null) {
        val proj = activeScheduleProject!!
        val startM = proj.prodStartMonth ?: currentMonth
        val startY = proj.prodStartYear ?: currentYear
        val estDur = proj.totalProductionMonths.takeIf { it > 0 } ?: maxOf(proj.productionDelayMonths, 1)

        FilmReleaseSchedulePickerDialog(
            projectTitle = proj.title,
            startMonth = startM,
            startYear = startY,
            estDurationMonths = estDur,
            qcMonth = currentMonth,
            qcYear = currentYear,
            currentMonth = currentMonth,
            currentYear = currentYear,
            initialSelectedMonth = proj.scheduledMonth,
            initialSelectedYear = proj.scheduledYear,
            allProjects = owned.projectHistory,
            isStudioAnimation = owned.studioType == "ANIMATION",
            onDismiss = { activeScheduleProject = null },
            onConfirm = { m, y ->
                val str = if (m != null && y != null) {
                    val mNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
                    "${mNames[(m - 1).coerceIn(0, 11)]} ${y + 2019}"
                } else ""
                onSchedule(proj.title, str)
                activeScheduleProject = null
            }
        )
    }

    // Timeline Inspector Dialog
    if (activeTimelineProject != null) {
        FilmTimelineDialog(
            project = activeTimelineProject!!,
            isAnimation = (owned.studioType == "ANIMATION"),
            onDismiss = { activeTimelineProject = null }
        )
    }
}
