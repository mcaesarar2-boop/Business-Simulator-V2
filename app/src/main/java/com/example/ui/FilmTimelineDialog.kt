package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.MovieProject
import com.example.domain.subsystems.creative.FilmTimelineEngine
import com.example.domain.subsystems.creative.TimelineStage

@Composable
fun FilmTimelineDialog(
    project: MovieProject,
    isAnimation: Boolean,
    onDismiss: () -> Unit
) {
    val totalMonths = if (project.totalProductionMonths > 0) project.totalProductionMonths else maxOf(project.productionDelayMonths, 1)
    val snapshot = FilmTimelineEngine.getProgress(
        isAnimation = isAnimation,
        totalMonths = totalMonths,
        remainingMonths = project.productionDelayMonths
    )
    val allStages = FilmTimelineEngine.getStagesForType(isAnimation)
    val currentRatio = (snapshot.elapsedMonths.toFloat() / totalMonths.toFloat()).coerceIn(0f, 1f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = project.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isAnimation) Color(0xFF7C4DFF).copy(alpha = 0.25f) else Color(0xFF00E5FF).copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = if (isAnimation) "🎨 Animasi" else "🎬 Live Action",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAnimation) Color(0xFFD1C4E9) else Color(0xFFB2EBF2),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Universal Phase: ${snapshot.universalPhase.codeLabel}",
                            fontSize = 12.sp,
                            color = Color(0xFFFFB300),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Bar Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progres Produksi Studio",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${snapshot.progressPercent}% (${snapshot.elapsedMonths}/$totalMonths bln)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { snapshot.progressPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF00E5FF),
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Fase Saat Ini: ${snapshot.displayLabel}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Overlapping Active Parallel Departments Banner
                if (snapshot.activeConcurrentStages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF263238))
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = "⚡ Departemen Paralel Berjalan Aktif (Overlapping Pipeline):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF80DEEA)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                snapshot.activeConcurrentStages.take(3).forEach { stage ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White.copy(alpha = 0.08f)
                                    ) {
                                        Text(
                                            text = "${stage.icon} ${stage.name}",
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Tahapan Produksi Studio (${allStages.size} Tahap)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Detailed Stages List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allStages) { stage ->
                        val isPassed = currentRatio >= stage.sequentialEnd
                        val isCurrent = currentRatio >= stage.sequentialStart && currentRatio < stage.sequentialEnd
                        val isOverlappingActive = currentRatio >= stage.overlapStart && currentRatio <= stage.overlapEnd && !isCurrent

                        TimelineStageItemRow(
                            stage = stage,
                            isPassed = isPassed,
                            isCurrent = isCurrent,
                            isOverlappingActive = isOverlappingActive
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF))
                ) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TimelineStageItemRow(
    stage: TimelineStage,
    isPassed: Boolean,
    isCurrent: Boolean,
    isOverlappingActive: Boolean
) {
    val bgColor = when {
        isCurrent -> Color(0xFF00E5FF).copy(alpha = 0.12f)
        isOverlappingActive -> Color(0xFF7C4DFF).copy(alpha = 0.10f)
        isPassed -> Color(0xFF1E1E1E).copy(alpha = 0.6f)
        else -> Color(0xFF181818).copy(alpha = 0.4f)
    }

    val borderColor = when {
        isCurrent -> Color(0xFF00E5FF)
        isOverlappingActive -> Color(0xFF7C4DFF)
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isCurrent || isOverlappingActive) 1.dp else 0.dp, borderColor, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status icon indicator
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isPassed -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                isCurrent -> Color(0xFF00E5FF).copy(alpha = 0.25f)
                                isOverlappingActive -> Color(0xFF7C4DFF).copy(alpha = 0.25f)
                                else -> Color.White.copy(alpha = 0.05f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isPassed -> Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                        isCurrent -> Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                        else -> Text(text = stage.icon, fontSize = 12.sp)
                    }
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "${stage.stageNumber}. ${stage.name}",
                            fontSize = 12.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) Color(0xFF00E5FF) else if (isPassed) Color.LightGray else Color.Gray
                        )
                        if (isCurrent) {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF00E5FF)) {
                                Text("AKTIF", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        } else if (isOverlappingActive) {
                            Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF7C4DFF)) {
                                Text("PARALEL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                    }
                    Text(
                        text = "${stage.universalPhase.codeLabel} • ${stage.notes}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = "${stage.percentage}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCurrent) Color(0xFF00E5FF) else Color.Gray
            )
        }
    }
}
