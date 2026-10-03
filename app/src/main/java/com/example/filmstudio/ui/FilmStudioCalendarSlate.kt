package com.example.filmstudio.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OwnedBusiness
import com.example.filmstudio.engine.FilmTimelineProjector
import com.example.filmstudio.model.FilmScheduleStage
import com.example.ui.formatCurrencyRingkas

/**
 * Calendar Slate widget showing ongoing productions, scheduled releases, and historical retrospectives.
 */
@Composable
fun FilmStudioCalendarSlate(
    owned: OwnedBusiness,
    currentMonth: Int,
    currentYear: Int,
    selectedSchedMonth: Int?,
    selectedSchedYear: Int?,
    onSelectScheduleMonth: (month: Int?, year: Int?) -> Unit,
    useShortFormat: Boolean
) {
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
    val monthFullNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
    val isAnimation = owned.studioType == "ANIMATION"
    val allFilms = owned.projectHistory

    var viewYear by remember { mutableStateOf(selectedSchedYear ?: currentYear) }
    var inspectedMonth by remember { mutableStateOf(selectedSchedMonth ?: currentMonth) }
    var inspectedYear by remember { mutableStateOf(selectedSchedYear ?: currentYear) }
    var isExpanded by remember { mutableStateOf(true) }

    val visualViewYear = viewYear + 2019
    val inspectedVisualYear = inspectedYear + 2019

    // Active counts summary
    val scheduledCount = allFilms.count { it.productionPhase == "ANTREAN" }
    val inProdCount = allFilms.count { it.status == "IN_PRODUCTION" && it.productionPhase != "ANTREAN" }
    val inTheatersCount = allFilms.count { it.status == "IN_THEATERS" }
    val historyCount = allFilms.count { it.status == "FINISHED" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp))
            .border(BorderStroke(1.dp, Color(0xFF2E2E2E)), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD54F).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "📅 Kalender & Slate Produksi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = if (isAnimation) "Studio Animasi • Acuan Jadwal Produksi, Rilis & Histori" else "Studio Live Action • Acuan Jadwal Produksi, Rilis & Histori",
                            fontSize = 11.sp,
                            color = Color(0xFFB0B0B0)
                        )
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Kalender",
                        tint = Color(0xFFFFD54F)
                    )
                }
            }

            // Quick Status Chips
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Text(
                        "Kini: ${monthNames[currentMonth - 1]} ${currentYear + 2019}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00E676),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFD54F).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.3f))
                ) {
                    Text(
                        "⏳ $scheduledCount Rencana",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFD54F),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF00E5FF).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.3f))
                ) {
                    Text(
                        "🎥 $inProdCount Digarap",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF00E5FF),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
                ) {
                    Text(
                        "🎬 $inTheatersCount Tayang Bioskop",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                if (historyCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF90A4AE).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF90A4AE).copy(alpha = 0.3f))
                    ) {
                        Text(
                            "📦 $historyCount Arsip Histori",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB0BEC5),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(14.dp))

                // Year Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewYear-- },
                        enabled = viewYear > 0
                    ) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (viewYear > 0) Color.White else Color.DarkGray)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Tahun $visualViewYear",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD54F)
                            )
                            if (viewYear == currentYear) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF00E676).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        "Kini",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E676),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = when {
                                viewYear == currentYear -> "Tahun Berjalan • Pantau Garapan & Rilis"
                                viewYear < currentYear -> "Histori Masa Lalu (${currentYear - viewYear} Tahun Lalu)"
                                else -> "Rencana Mendatang (${viewYear - currentYear} Tahun Lagi)"
                            },
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = { viewYear++ }
                    ) {
                        Text(">", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 12 Months Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    monthNames.chunked(4).forEachIndexed { rowIndex, rowMonths ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowMonths.forEachIndexed { colIndex, monthName ->
                                val monthNum = rowIndex * 4 + colIndex + 1
                                val isCurrent = viewYear == currentYear && monthNum == currentMonth
                                val isInspected = inspectedYear == viewYear && inspectedMonth == monthNum
                                val isSelectedAsSchedule = selectedSchedYear == viewYear && selectedSchedMonth == monthNum
                                val isPast = viewYear < currentYear || (viewYear == currentYear && monthNum < currentMonth)

                                val activities = FilmTimelineProjector.getStudioFilmActivities(
                                    targetMonth = monthNum,
                                    targetYear = viewYear,
                                    currentMonth = currentMonth,
                                    currentYear = currentYear,
                                    allFilms = allFilms,
                                    isAnimation = isAnimation
                                )

                                val containerColor = when {
                                    isSelectedAsSchedule -> Color(0xFFFFD54F).copy(alpha = 0.22f)
                                    isInspected -> Color.White.copy(alpha = 0.12f)
                                    isCurrent -> Color(0xFF00E676).copy(alpha = 0.08f)
                                    isPast -> Color.White.copy(alpha = 0.02f)
                                    activities.isNotEmpty() -> Color(0xFF212121)
                                    else -> Color(0xFF191919)
                                }

                                val borderColor = when {
                                    isSelectedAsSchedule -> Color(0xFFFFD54F)
                                    isInspected -> Color.White.copy(alpha = 0.4f)
                                    isCurrent -> Color(0xFF00E676).copy(alpha = 0.8f)
                                    isPast -> Color.White.copy(alpha = 0.05f)
                                    activities.isNotEmpty() -> Color.White.copy(alpha = 0.18f)
                                    else -> Color.White.copy(alpha = 0.06f)
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(68.dp)
                                        .clickable {
                                            inspectedMonth = monthNum
                                            inspectedYear = viewYear
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = containerColor),
                                    border = BorderStroke(if (isSelectedAsSchedule || isInspected || isCurrent) 1.5.dp else 1.dp, borderColor)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 5.dp, vertical = 5.dp),
                                        verticalArrangement = Arrangement.SpaceBetween,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = monthName,
                                                color = if (isSelectedAsSchedule) Color(0xFFFFD54F) else if (isCurrent) Color(0xFF00E676) else if (isPast) Color(0xFF888888) else Color.White,
                                                fontWeight = if (isSelectedAsSchedule || isCurrent || isInspected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.5.sp
                                            )
                                            if (isCurrent) {
                                                Box(modifier = Modifier.size(5.dp).background(Color(0xFF00E676), CircleShape))
                                            }
                                        }

                                        if (activities.isNotEmpty()) {
                                            val first = activities.first()
                                            val shortLabel = when (first.stage) {
                                                FilmScheduleStage.PLANNED_START -> "⏳ Rencana"
                                                FilmScheduleStage.PRE_PRODUCTION -> "📝 Pra-Prod"
                                                FilmScheduleStage.ACTIVE_PRODUCTION -> if (isAnimation) "🎨 Animasi" else "🎥 Syuting"
                                                FilmScheduleStage.POST_PRODUCTION -> "💻 Pasca"
                                                FilmScheduleStage.QUALITY_CONTROL -> "🔍 QC"
                                                FilmScheduleStage.AWAITING_RELEASE -> "⏳ Rilis"
                                                FilmScheduleStage.IN_THEATERS -> "🎬 Tayang"
                                                FilmScheduleStage.CLOSING_RUN -> "🏁 Tutup"
                                                FilmScheduleStage.JUST_FINISHED -> "📦 Selesai"
                                                FilmScheduleStage.PAST_PRODUCTION -> "🎞️ Prod(H)"
                                                FilmScheduleStage.PAST_THEATERS -> "🎬 Tayang(H)"
                                                FilmScheduleStage.PAST_FINISHED -> "📦 Arsip(H)"
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = first.accentColor.copy(alpha = 0.2f),
                                                border = BorderStroke(0.5.dp, first.accentColor.copy(alpha = 0.5f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = shortLabel,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = first.accentColor,
                                                    maxLines = 1,
                                                    modifier = Modifier.padding(horizontal = 2.dp, vertical = 1.dp)
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = if (isPast) "Lewat" else "Bebas",
                                                fontSize = 9.sp,
                                                color = if (isPast) Color(0xFF555555) else Color(0xFF757575)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Month Inspector Card
                val inspectedActivities = FilmTimelineProjector.getStudioFilmActivities(
                    targetMonth = inspectedMonth,
                    targetYear = inspectedYear,
                    currentMonth = currentMonth,
                    currentYear = currentYear,
                    allFilms = allFilms,
                    isAnimation = isAnimation
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1C))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🗓️ Agenda: ${monthFullNames[inspectedMonth - 1]} $inspectedVisualYear",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color.White
                                )
                            }

                            val timingLabel = when {
                                inspectedYear == currentYear && inspectedMonth == currentMonth -> "🟢 Bulan Ini"
                                inspectedYear < currentYear || (inspectedYear == currentYear && inspectedMonth < currentMonth) -> "⚪ Telah Lewat"
                                else -> "⏳ Mendatang"
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.06f)
                            ) {
                                Text(
                                    timingLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (inspectedActivities.isEmpty()) {
                            val isInspectedPast = inspectedYear < currentYear || (inspectedYear == currentYear && inspectedMonth < currentMonth)
                            if (isInspectedPast) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Arsip Masa Lalu Kosong", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.LightGray)
                                        Text(
                                            "Tidak ada catatan pengerjaan atau penayangan film pada bulan ini.",
                                            fontSize = 10.5.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF4CAF50).copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                        .border(BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.25f)), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Slot Studio Bersih & Tersedia", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF4CAF50))
                                        Text(
                                            "Belum ada proyek film yang digarap maupun tayang di bioskop pada bulan ini. Waktu optimal untuk memulai film baru!",
                                            fontSize = 10.5.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                inspectedActivities.forEach { act ->
                                    val isHistorical = act.isPastEvent
                                    val cardBorder = if (isHistorical) {
                                        BorderStroke(1.dp, Color(0xFF78909C).copy(alpha = 0.35f))
                                    } else {
                                        BorderStroke(1.dp, act.accentColor.copy(alpha = 0.35f))
                                    }
                                    val cardBg = if (isHistorical) Color(0xFF1E2124) else Color(0xFF242424)

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(cardBorder, RoundedCornerShape(12.dp)),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = cardBg)
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .height(72.dp)
                                                    .background(if (isHistorical) act.accentColor.copy(alpha = 0.5f) else act.accentColor)
                                            )
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = act.film.title,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = if (isHistorical) Color(0xFFCFD8DC) else Color.White,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (isHistorical) Color(0xFF37474F).copy(alpha = 0.35f) else act.accentColor.copy(alpha = 0.15f),
                                                        border = BorderStroke(1.dp, if (isHistorical) Color(0xFF78909C).copy(alpha = 0.4f) else act.accentColor.copy(alpha = 0.5f))
                                                    ) {
                                                        Text(
                                                            text = act.stageLabel,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isHistorical) Color(0xFFB0BEC5) else act.accentColor,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "${act.film.filmFormat} • ${act.film.genres.take(3).joinToString(", ")}",
                                                    fontSize = 10.sp,
                                                    color = if (isHistorical) Color(0xFF888888) else Color.Gray
                                                )
                                                Text(
                                                    text = act.detailText,
                                                    fontSize = 10.5.sp,
                                                    color = if (isHistorical) Color(0xFF90A4AE) else Color.LightGray,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                                if (isHistorical && act.film.status == "FINISHED") {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            "⭐ Skor: ${act.film.internalScore ?: 0}/100",
                                                            fontSize = 9.5.sp,
                                                            color = Color(0xFFFFD54F)
                                                        )
                                                        Text("•", fontSize = 9.5.sp, color = Color.Gray)
                                                        Text(
                                                            "Box Office: ${formatCurrencyRingkas(act.film.boxOffice, true)}",
                                                            fontSize = 9.5.sp,
                                                            color = Color(0xFF81C784)
                                                        )
                                                        Text("•", fontSize = 9.5.sp, color = Color.Gray)
                                                        Text(
                                                            "Profit: ${formatCurrencyRingkas(act.film.netProfit, true)}",
                                                            fontSize = 9.5.sp,
                                                            color = if (act.film.netProfit >= 0) Color(0xFF81C784) else Color(0xFFE57373)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Planning Action Button
                        val isPastMonth = inspectedYear < currentYear || (inspectedYear == currentYear && inspectedMonth < currentMonth)
                        if (!isPastMonth) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val isCurrentlyChosen = selectedSchedMonth == inspectedMonth && selectedSchedYear == inspectedYear
                            if (isCurrentlyChosen) {
                                OutlinedButton(
                                    onClick = { onSelectScheduleMonth(null, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD54F)),
                                    border = BorderStroke(1.dp, Color(0xFFFFD54F))
                                ) {
                                    Text("⭐ Bulan Ini Dipilih (Klik untuk Batalkan & Mulai Sekarang)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { onSelectScheduleMonth(inspectedMonth, inspectedYear) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
                                ) {
                                    Text(
                                        "📅 Pasang Jadwal Produksi ke ${monthNames[inspectedMonth - 1]} $inspectedVisualYear",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White.copy(alpha = 0.03f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📜 Rekam Jejak Masa Lalu", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF90A4AE))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("• Histori produksi & rilis tersimpan utuh di kalender studio.", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
