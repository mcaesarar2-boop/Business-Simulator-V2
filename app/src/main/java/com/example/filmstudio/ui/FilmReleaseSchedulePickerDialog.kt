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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.MovieProject
import com.example.filmstudio.engine.FilmTimelineProjector
import com.example.filmstudio.model.FilmScheduleStage

/**
 * Release date picker allowing player to schedule theatrical release post-QC.
 */
@Composable
fun FilmReleaseSchedulePickerDialog(
    projectTitle: String,
    startMonth: Int,
    startYear: Int,
    estDurationMonths: Int,
    qcMonth: Int,
    qcYear: Int,
    currentMonth: Int,
    currentYear: Int,
    initialSelectedMonth: Int?,
    initialSelectedYear: Int?,
    allProjects: List<MovieProject> = emptyList(),
    isStudioAnimation: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (month: Int?, year: Int?) -> Unit
) {
    val qcAbs = qcYear * 12 + qcMonth
    val startAbs = startYear * 12 + startMonth
    val firstAvailableAbs = qcAbs + 1
    val firstAvailableMonth = ((firstAvailableAbs - 1) % 12) + 1
    val firstAvailableYear = (firstAvailableAbs - 1) / 12

    val defaultYear = initialSelectedYear ?: firstAvailableYear
    var viewYear by remember { mutableStateOf(maxOf(defaultYear, firstAvailableYear)) }
    var selectedM by remember { mutableStateOf<Int?>(initialSelectedMonth ?: firstAvailableMonth) }
    var selectedY by remember { mutableStateOf(initialSelectedYear ?: firstAvailableYear) }

    val visualViewYear = viewYear + 2019
    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
    val monthFullNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎬 Atur Jadwal Tayang Bioskop",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD54F)
                        )
                        Text(
                            text = "Pilih bulan rilis perdana setelah tahap kontrol kualitas (QC)",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Project Schedule Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF242424)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = projectTitle.ifBlank { "Film Baru" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🚀 Mulai: ${monthNames[(startMonth - 1).coerceIn(0, 11)]} ${startYear + 2019}", fontSize = 10.5.sp, color = Color(0xFF80DEEA))
                            Text("⏱️ Estimasi: ~$estDurationMonths Bulan", fontSize = 10.5.sp, color = Color.LightGray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔍 QC: ${monthNames[(qcMonth - 1).coerceIn(0, 11)]} ${qcYear + 2019}", fontSize = 10.5.sp, color = Color(0xFFFF80AB), fontWeight = FontWeight.SemiBold)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF81C784).copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, Color(0xFF81C784).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    "🎯 Siap Tayang: ${monthNames[(firstAvailableMonth - 1).coerceIn(0, 11)]} ${firstAvailableYear + 2019}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Presets Row
                Text("Pilihan Cepat Target Rilis:", fontSize = 10.5.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Preset 1: Tepat Setelah QC
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedM = firstAvailableMonth
                                selectedY = firstAvailableYear
                                viewYear = firstAvailableYear
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF00E5FF).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "⚡ Langsung Pasca QC\n(${monthNames[(firstAvailableMonth - 1).coerceIn(0, 11)]} ${firstAvailableYear + 2019})",
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF80DEEA),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp)
                        )
                    }

                    // Preset 2: Libur Musim Panas (Juni/Juli)
                    val summerCandidate = run {
                        val c1 = qcYear * 12 + 7
                        if (c1 > qcAbs) Pair(7, qcYear)
                        else Pair(7, qcYear + 1)
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedM = summerCandidate.first
                                selectedY = summerCandidate.second
                                viewYear = summerCandidate.second
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFB300).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "☀️ Musim Panas\n(${monthNames[summerCandidate.first - 1]} ${summerCandidate.second + 2019})",
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFFFD54F),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp)
                        )
                    }

                    // Preset 3: Libur Akhir Tahun (Desember)
                    val holidayCandidate = run {
                        val c1 = qcYear * 12 + 12
                        if (c1 > qcAbs) Pair(12, qcYear)
                        else Pair(12, qcYear + 1)
                    }
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedM = holidayCandidate.first
                                selectedY = holidayCandidate.second
                                viewYear = holidayCandidate.second
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "🎄 Libur Akhir Tahun\n(Des ${holidayCandidate.second + 2019})",
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF81C784),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Year Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewYear-- },
                        enabled = viewYear > firstAvailableYear
                    ) {
                        Text("<", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = if (viewYear > firstAvailableYear) Color.White else Color.DarkGray)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Tahun $visualViewYear",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD54F)
                        )
                        Text(
                            text = if (viewYear == firstAvailableYear) "Tahun Penyelesaian QC" else "Tahun ke-${viewYear - firstAvailableYear + 1} Pasca-QC",
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

                Spacer(modifier = Modifier.height(10.dp))

                // 12 Months Grid
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    monthNames.chunked(3).forEachIndexed { rowIndex, rowMonths ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowMonths.forEachIndexed { colIndex, monthName ->
                                val monthNum = rowIndex * 3 + colIndex + 1
                                val targetAbs = viewYear * 12 + monthNum
                                val isBeforeOrAtQc = targetAbs <= qcAbs
                                val isSelected = selectedY == viewYear && selectedM == monthNum

                                val monthActivities = FilmTimelineProjector.getStudioFilmActivities(
                                    targetMonth = monthNum,
                                    targetYear = viewYear,
                                    currentMonth = currentMonth,
                                    currentYear = currentYear,
                                    allFilms = allProjects,
                                    isAnimation = isStudioAnimation
                                )
                                val hasTheatricalRun = monthActivities.any { it.stage == FilmScheduleStage.IN_THEATERS }

                                val containerColor = when {
                                    isSelected -> Color(0xFFFFD54F).copy(alpha = 0.25f)
                                    isBeforeOrAtQc -> {
                                        if (targetAbs == qcAbs) Color(0xFFFF4081).copy(alpha = 0.12f)
                                        else if (targetAbs in startAbs until qcAbs) Color(0xFF2979FF).copy(alpha = 0.08f)
                                        else Color.White.copy(alpha = 0.02f)
                                    }
                                    hasTheatricalRun -> Color(0xFF252015)
                                    else -> Color(0xFF1E241E)
                                }

                                val borderColor = when {
                                    isSelected -> Color(0xFFFFD54F)
                                    targetAbs == qcAbs -> Color(0xFFFF4081).copy(alpha = 0.5f)
                                    isBeforeOrAtQc -> Color.White.copy(alpha = 0.04f)
                                    hasTheatricalRun -> Color(0xFFFFB300).copy(alpha = 0.4f)
                                    else -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(54.dp)
                                        .clickable(enabled = !isBeforeOrAtQc) {
                                            selectedM = monthNum
                                            selectedY = viewYear
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = containerColor),
                                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 4.dp, vertical = 4.dp),
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
                                                color = if (isSelected) Color(0xFFFFD54F) else if (isBeforeOrAtQc) Color.DarkGray else Color.White,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                fontSize = 11.sp
                                            )
                                            if (isSelected) {
                                                Box(modifier = Modifier.size(5.dp).background(Color(0xFFFFD54F), CircleShape))
                                            }
                                        }

                                        when {
                                            isSelected -> {
                                                Text("🎬 Terpilih", color = Color(0xFFFFD54F), fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                            }
                                            targetAbs == qcAbs -> {
                                                Text("🔍 QC Selesai", color = Color(0xFFFF4081), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                            targetAbs in startAbs until qcAbs -> {
                                                Text("🛠️ Garap", color = Color(0xFF82B1FF).copy(alpha = 0.7f), fontSize = 8.5.sp)
                                            }
                                            targetAbs < startAbs -> {
                                                Text("-", color = Color(0xFF444444), fontSize = 8.5.sp)
                                            }
                                            hasTheatricalRun -> {
                                                Text("⚠️ Ada Film", color = Color(0xFFFFB300), fontSize = 8.5.sp, fontWeight = FontWeight.Medium)
                                            }
                                            else -> {
                                                Text("🟢 Tersedia", color = Color(0xFF81C784), fontSize = 8.5.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Inspector Card
                if (selectedM != null) {
                    val selAbs = selectedY * 12 + selectedM!!
                    val diffFromQc = selAbs - qcAbs
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
                        border = BorderStroke(1.dp, Color(0xFFFFD54F).copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎬 Target Rilis: ", fontSize = 11.5.sp, color = Color.LightGray)
                                Text(
                                    "${monthFullNames[selectedM!! - 1]} ${selectedY + 2019}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD54F)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            val guideText = when {
                                diffFromQc <= 1 -> "Langsung tayang 1 bulan pasca-QC. Cepat dan menjaga antusiasme studio!"
                                diffFromQc in 2..4 -> "Tayang $diffFromQc bulan pasca-QC. Jeda waktu ideal untuk promosi, festival & billboard!"
                                else -> "Tayang $diffFromQc bulan pasca-QC. Strategi perilisan jangka panjang untuk musim tayang puncak!"
                            }
                            Text(
                                text = "💡 $guideText",
                                fontSize = 10.sp,
                                color = Color(0xFF81C784),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dialog Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { onConfirm(null, null) },
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Text("Reset / Otomatis", color = Color(0xFFFF7043), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
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
                        modifier = Modifier.weight(1.4f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F),
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF333333),
                            disabledContentColor = Color.Gray
                        )
                    ) {
                        Text(
                            "Simpan Jadwal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
