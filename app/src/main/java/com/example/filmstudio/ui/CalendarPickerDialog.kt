package com.example.filmstudio.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.MovieProject
import com.example.filmstudio.engine.FilmTimelineProjector
import com.example.filmstudio.model.FilmScheduleStage

/**
 * Interactive 12-month calendar picker for selecting film start schedule.
 */
@Composable
fun CalendarPickerDialog(
    currentMonth: Int,
    currentYear: Int,
    initialMonth: Int?,
    initialYear: Int?,
    bookedSchedules: List<Pair<Int, Int>> = emptyList(),
    allProjects: List<MovieProject> = emptyList(),
    isStudioAnimation: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (month: Int?, year: Int?) -> Unit
) {
    var viewYear by remember { mutableStateOf(initialYear ?: currentYear) }
    var selectedM by remember { mutableStateOf(initialMonth) }
    var selectedY by remember { mutableStateOf(initialYear ?: currentYear) }

    val visualViewYear = viewYear + 2019
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
    val monthFullNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            border = BorderStroke(1.dp, Color(0xFF333333)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("📅 Pilih Jadwal Rilis & Produksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Tetapkan bulan rencana rilis bioskop atau mulai garap", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Header (Tahun Switcher)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewYear-- },
                        enabled = viewYear > currentYear
                    ) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (viewYear > currentYear) Color.White else Color.DarkGray)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tahun $visualViewYear",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD54F)
                        )
                        Text(
                            text = if (viewYear == currentYear) "Tahun Berjalan" else "Tahun ke-${viewYear - currentYear + 1}",
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

                // Grid 12 Bulan (4 Baris x 3 Kolom)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    monthNames.chunked(3).forEachIndexed { rowIndex, rowMonths ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowMonths.forEachIndexed { colIndex, monthName ->
                                val monthNum = rowIndex * 3 + colIndex + 1
                                val isPastDisabled = viewYear == currentYear && monthNum < currentMonth
                                val isAlreadyBooked = bookedSchedules.contains(Pair(monthNum, viewYear))
                                val isSelected = selectedY == viewYear && selectedM == monthNum
                                val isCurrent = viewYear == currentYear && monthNum == currentMonth

                                val monthActivities = FilmTimelineProjector.getStudioFilmActivities(
                                    targetMonth = monthNum,
                                    targetYear = viewYear,
                                    currentMonth = currentMonth,
                                    currentYear = currentYear,
                                    allFilms = allProjects,
                                    isAnimation = isStudioAnimation
                                )

                                val containerColor = when {
                                    isSelected -> Color(0xFFFFD54F).copy(alpha = 0.25f)
                                    isCurrent -> Color.White.copy(alpha = 0.09f)
                                    isPastDisabled -> Color.White.copy(alpha = 0.02f)
                                    monthActivities.isNotEmpty() -> Color(0xFF222222)
                                    else -> Color(0xFF1E1E1E)
                                }

                                val borderColor = when {
                                    isSelected -> Color(0xFFFFD54F)
                                    isCurrent -> Color(0xFF00E676)
                                    isAlreadyBooked -> Color(0xFFFF5252).copy(alpha = 0.6f)
                                    monthActivities.isNotEmpty() -> Color.White.copy(alpha = 0.2f)
                                    else -> Color.White.copy(alpha = 0.06f)
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp)
                                        .clickable(enabled = !isPastDisabled) {
                                            selectedM = monthNum
                                            selectedY = viewYear
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = containerColor),
                                    border = BorderStroke(if (isSelected || isCurrent) 1.5.dp else 1.dp, borderColor)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 4.dp, vertical = 5.dp),
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
                                                color = if (isSelected) Color(0xFFFFD54F) else if (isCurrent) Color(0xFF00E676) else if (isPastDisabled) Color.DarkGray else Color.White,
                                                fontWeight = if (isSelected || isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.5.sp
                                            )
                                            if (isCurrent) {
                                                Box(modifier = Modifier.size(5.dp).background(Color(0xFF00E676), CircleShape))
                                            }
                                        }

                                        if (isAlreadyBooked) {
                                            Text("⚠️ Terisi", color = Color(0xFFFF5252), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        } else if (monthActivities.isNotEmpty()) {
                                            val first = monthActivities.first()
                                            Text(
                                                text = when (first.stage) {
                                                    FilmScheduleStage.PLANNED_START -> "⏳ Rencana"
                                                    FilmScheduleStage.IN_THEATERS -> "🎬 Tayang"
                                                    FilmScheduleStage.CLOSING_RUN -> "🏁 Tutup"
                                                    FilmScheduleStage.QUALITY_CONTROL -> "🔍 QC"
                                                    else -> "🎥 Garap"
                                                },
                                                color = first.accentColor,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        } else {
                                            Text(
                                                text = if (isPastDisabled) "-" else "Bebas",
                                                color = if (isPastDisabled) Color(0xFF444444) else Color(0xFF888888),
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selected Month Preview Inspector
                if (selectedM != null) {
                    val selActivities = FilmTimelineProjector.getStudioFilmActivities(
                        targetMonth = selectedM!!,
                        targetYear = selectedY,
                        currentMonth = currentMonth,
                        currentYear = currentYear,
                        allFilms = allProjects,
                        isAnimation = isStudioAnimation
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF242424)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Kondisi: ${monthFullNames[selectedM!! - 1]} ${selectedY + 2019}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                            if (selActivities.isEmpty()) {
                                Text(
                                    text = "🟢 Slot Bersih: Belum ada film yang digarap atau tayang. Sangat ideal untuk target rilis!",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF81C784),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            } else {
                                selActivities.take(2).forEach { act ->
                                    Text(
                                        text = "• ${act.film.title}: ${act.stageLabel}",
                                        fontSize = 10.5.sp,
                                        color = act.accentColor,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onConfirm(null, null) },
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Text("Langsung Mulai", color = Color(0xFFFF7043), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.7f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Text("Batal", fontSize = 11.5.sp, color = Color.LightGray)
                    }

                    Button(
                        onClick = { onConfirm(selectedM, selectedY) },
                        enabled = selectedM != null,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F),
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF333333),
                            disabledContentColor = Color.Gray
                        )
                    ) {
                        Text("Pilih Jadwal", fontWeight = FontWeight.Bold, fontSize = 11.5.sp, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}
