package com.example.filmstudio.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OwnedBusiness
import com.example.domain.subsystems.creative.FilmTimelineEngine
import com.example.filmstudio.engine.FilmProductionMath
import com.example.ui.formatCurrencyRingkas
import java.text.NumberFormat
import java.util.Locale

/**
 * Main form for configuring and launching a new film production,
 * integrating calendar slate, budget inputs, demographic selectors,
 * pitch deck projections, and active project monitor.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilmProductionForm(
    owned: OwnedBusiness,
    playerCash: Long,
    useShortFormat: Boolean,
    currentMonth: Int,
    currentYear: Int,
    onProduce: (
        title: String,
        budget: Long,
        promoBudget: Long,
        genres: List<String>,
        isGlobal: Boolean,
        schedMonth: Int?,
        schedYear: Int?,
        filmFormat: String,
        productionFocus: String,
        scheduledReleaseDate: String?,
        targetDurationMonths: Int?
    ) -> Boolean,
    onPolish: (title: String, budgetCost: Long, extraMonths: Int) -> Unit = { _, _, _ -> },
    onSchedule: (title: String, schedStr: String) -> Unit = { _, _ -> },
    onCancel: (title: String, refundAmount: Long) -> Unit = { _, _ -> },
    onOpenHistory: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var budgetInput by remember { mutableStateOf("") }
    var promoBudgetInput by remember { mutableStateOf("") }
    val type = owned.studioType ?: "LIVE_ACTION"
    var selectedGenres by remember { mutableStateOf(if (type == "ANIMATION") setOf("Animation") else setOf()) }
    var isGlobal by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf("Feature Film") }
    var selectedFocus by remember { mutableStateOf("REGULER") }

    var selectedSchedMonth by remember { mutableStateOf<Int?>(null) }
    var selectedSchedYear by remember { mutableStateOf<Int?>(null) }
    var selectedReleaseMonth by remember { mutableStateOf<Int?>(null) }
    var selectedReleaseYear by remember { mutableStateOf<Int?>(null) }
    var showCalendarPicker by remember { mutableStateOf(false) }
    var showReleaseDatePicker by remember { mutableStateOf(false) }

    val allGenres = listOf("Action", "Romance", "Sci-Fi", "Horror", "Comedy", "Drama", "Fantasy", "Animation", "Thriller", "Mystery")
    val hasGlobalPipeline = if (type == "ANIMATION") owned.purchasedUpgrades.contains("anim_global_distrib") else owned.purchasedUpgrades.contains("la_global_distrib")
    val canGlobal = owned.level >= 20 || hasGlobalPipeline

    // Interactive Studio Calendar Slate
    FilmStudioCalendarSlate(
        owned = owned,
        currentMonth = currentMonth,
        currentYear = currentYear,
        selectedSchedMonth = selectedSchedMonth,
        selectedSchedYear = selectedSchedYear,
        onSelectScheduleMonth = { m, y ->
            selectedSchedMonth = m
            selectedSchedYear = y
        },
        useShortFormat = useShortFormat
    )

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
            Text("🎬 Studio Mgt: Box Office Simulator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Judul Film") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Yellow,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.Yellow
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = budgetInput,
                onValueChange = { newValue ->
                    val digits = newValue.filter { it.isDigit() }
                    if (digits.isEmpty()) {
                        budgetInput = ""
                    } else {
                        val parsed = digits.toLongOrNull()
                        if (parsed != null) {
                            val formatter = NumberFormat.getNumberInstance(Locale.US)
                            budgetInput = formatter.format(parsed)
                        }
                    }
                },
                label = { Text("Production Budget (USD)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                leadingIcon = { Text("$", modifier = Modifier.padding(start = 12.dp)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Yellow,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.Yellow
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = promoBudgetInput,
                onValueChange = { newValue ->
                    val digits = newValue.filter { it.isDigit() }
                    if (digits.isEmpty()) {
                        promoBudgetInput = ""
                    } else {
                        val parsed = digits.toLongOrNull()
                        if (parsed != null) {
                            val formatter = NumberFormat.getNumberInstance(Locale.US)
                            promoBudgetInput = formatter.format(parsed)
                        }
                    }
                },
                label = { Text("Promotion Budget (USD)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                leadingIcon = { Text("$", modifier = Modifier.padding(start = 12.dp)) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Yellow,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.Yellow
                )
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("Distribution Scale", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = !isGlobal, onClick = { isGlobal = false })
                Text("Local Release")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = isGlobal, onClick = { if (canGlobal) isGlobal = true }, enabled = canGlobal)
                Text("Global (Hollywood)" + if (!canGlobal) " [Butuh Upgrade Distribusi Global / Lvl 20]" else "")
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Format Film", fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selectedFormat == "Short Film", onClick = { selectedFormat = "Short Film" })
                Text("Short Film")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = selectedFormat == "Feature Film", onClick = { selectedFormat = "Feature Film" })
                Text("Feature Film")
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Demographics & Genres (Select 1-6)", fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                allGenres.forEach { genre ->
                    if (type == "LIVE_ACTION" && genre == "Animation") return@forEach

                    val isAnimTag = type == "ANIMATION" && genre == "Animation"
                    FilterChip(
                        selected = selectedGenres.contains(genre),
                        onClick = {
                            if (isAnimTag) return@FilterChip
                            if (selectedGenres.contains(genre)) selectedGenres = selectedGenres - genre
                            else if (selectedGenres.size < 6) selectedGenres = selectedGenres + genre
                        },
                        label = { Text(genre) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Pendekatan Produksi (Production Focus)", fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("REGULER", "Reguler", "Standar"),
                    Triple("KUALITAS", "Fokus Kualitas", "Budget +30%, Durasi +6Bln"),
                    Triple("MAHAKARYA", "Ambisi Mahakarya", "Budget +80%, Durasi +12Bln")
                ).forEach { (id, t, desc) ->
                    val isSelected = selectedFocus == id
                    val focusContainerColor = if (isSelected) Color(0xFFDAA520).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                    val focusBorderColor = if (isSelected) Color(0xFFDAA520) else Color.White.copy(alpha = 0.1f)
                    Card(
                        onClick = { selectedFocus = id },
                        colors = CardDefaults.cardColors(containerColor = focusContainerColor),
                        border = BorderStroke(1.dp, focusBorderColor),
                        modifier = Modifier.width(160.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(t, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFFDAA520) else Color.White)
                            Text(desc, fontSize = 10.sp, color = Color.LightGray)
                        }
                    }
                }
            }

            val bLongRaw = budgetInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
            val bLong = when (selectedFocus) {
                "KUALITAS" -> (bLongRaw * 1.3).toLong()
                "MAHAKARYA" -> (bLongRaw * 1.8).toLong()
                else -> bLongRaw
            }
            val pLong = promoBudgetInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
            val totalInvestment = bLong + pLong

            val hasVirtualProd = owned.purchasedUpgrades.contains("la_virtual_prod")
            val hasAnimPipeline = owned.purchasedUpgrades.contains("anim_pipeline")
            val hasRenderFarm = owned.purchasedUpgrades.contains("anim_renderfarm")
            val hasPostPipeline = owned.purchasedUpgrades.contains("la_post_pipeline")

            val startM = selectedSchedMonth ?: currentMonth
            val startY = selectedSchedYear ?: currentYear
            val startAbs = startY * 12 + startM

            val estimatedDurationMonths = remember(type, selectedFormat, bLong, selectedFocus, hasVirtualProd, hasAnimPipeline, hasRenderFarm, hasPostPipeline) {
                FilmTimelineEngine.estimateProjectDuration(
                    isAnimation = (type == "ANIMATION"),
                    filmFormat = selectedFormat,
                    budget = bLong,
                    productionFocus = selectedFocus,
                    hasVirtualProd = hasVirtualProd,
                    hasAnimPipeline = hasAnimPipeline,
                    hasRenderFarm = hasRenderFarm,
                    hasPostPipeline = hasPostPipeline
                )
            }

            val qcAbs = startAbs + estimatedDurationMonths
            val qcMonth = ((qcAbs - 1) % 12) + 1
            val qcYear = (qcAbs - 1) / 12

            val isReleaseValid = selectedReleaseMonth != null && selectedReleaseYear != null &&
                ((selectedReleaseYear!! * 12 + selectedReleaseMonth!!) > qcAbs)
            val effectiveReleaseMonth = if (isReleaseValid) selectedReleaseMonth else null
            val effectiveReleaseYear = if (isReleaseValid) selectedReleaseYear else null

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Jadwalkan Mulai Produksi (Opsional)", fontWeight = FontWeight.Bold, color = Color.White)
                if (selectedSchedMonth != null && selectedSchedYear != null) {
                    TextButton(onClick = {
                        selectedSchedMonth = null
                        selectedSchedYear = null
                    }) {
                        Text("Reset ke Mulai Sekarang", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = { showCalendarPicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (selectedSchedMonth != null) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.2f))
            ) {
                if (selectedSchedMonth != null && selectedSchedYear != null) {
                    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
                    val mIndex = (selectedSchedMonth!! - 1).coerceIn(0, 11)
                    val vYear = selectedSchedYear!! + 2019
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📅 Rencana Mulai: ", color = Color.LightGray)
                        Text("${monthNames[mIndex]} $vYear", fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD54F))
                        Text(" (Klik untuk ubah)", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 6.dp))
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📅 Jadwal: Mulai Langsung Sekarang (Bulan $currentMonth/${currentYear + 2019})", color = Color.White)
                    }
                }
            }

            if (showCalendarPicker) {
                val bookedSchedules = owned.projectHistory
                    .filter { it.productionPhase == "ANTREAN" && it.scheduledMonth != null && it.scheduledYear != null }
                    .map { Pair(it.scheduledMonth!!, it.scheduledYear!!) }

                CalendarPickerDialog(
                    currentMonth = currentMonth,
                    currentYear = currentYear,
                    initialMonth = selectedSchedMonth,
                    initialYear = selectedSchedYear,
                    bookedSchedules = bookedSchedules,
                    allProjects = owned.projectHistory,
                    isStudioAnimation = owned.studioType == "ANIMATION",
                    onDismiss = { showCalendarPicker = false },
                    onConfirm = { m, y ->
                        selectedSchedMonth = m
                        selectedSchedYear = y
                        showCalendarPicker = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Theatrical Release Target Schedule Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
                border = BorderStroke(1.dp, if (effectiveReleaseMonth != null) Color(0xFFFFD54F).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("🎬 Target Jadwal Tayang Bioskop", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color.White)
                            if (effectiveReleaseMonth != null) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                    border = BorderStroke(0.5.dp, Color(0xFF4CAF50).copy(alpha = 0.6f))
                                ) {
                                    Text("Terjadwal", fontSize = 9.sp, color = Color(0xFF81C784), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                        if (effectiveReleaseMonth != null) {
                            Text(
                                "Reset / Otomatis",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.clickable {
                                    selectedReleaseMonth = null
                                    selectedReleaseYear = null
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("⏱️ Estimasi Garap: ~$estimatedDurationMonths Bulan", fontSize = 11.sp, color = Color.LightGray)
                        Text("🔍 Perkiraan QC: ${monthNames[(qcMonth - 1).coerceIn(0, 11)]} ${qcYear + 2019}", fontSize = 11.sp, color = Color(0xFFFF80AB), fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showReleaseDatePicker = true },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (effectiveReleaseMonth != null) Color(0xFFFFD54F).copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f)
                        ),
                        border = BorderStroke(1.dp, if (effectiveReleaseMonth != null) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (effectiveReleaseMonth != null && effectiveReleaseYear != null) {
                                Text("🎬 Target Rilis: ", fontSize = 12.sp, color = Color.White)
                                Text(
                                    "${monthNames[(effectiveReleaseMonth - 1).coerceIn(0, 11)]} ${effectiveReleaseYear + 2019}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD54F)
                                )
                                Text(" (Klik untuk ganti)", fontSize = 10.5.sp, color = Color.LightGray, modifier = Modifier.padding(start = 4.dp))
                            } else {
                                Text("📅 Atur Jadwal Tayang Bioskop (Setelah QC)", fontSize = 12.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            if (showReleaseDatePicker) {
                FilmReleaseSchedulePickerDialog(
                    projectTitle = title,
                    startMonth = startM,
                    startYear = startY,
                    estDurationMonths = estimatedDurationMonths,
                    qcMonth = qcMonth,
                    qcYear = qcYear,
                    currentMonth = currentMonth,
                    currentYear = currentYear,
                    initialSelectedMonth = effectiveReleaseMonth,
                    initialSelectedYear = effectiveReleaseYear,
                    allProjects = owned.projectHistory,
                    isStudioAnimation = (type == "ANIMATION"),
                    onDismiss = { showReleaseDatePicker = false },
                    onConfirm = { m, y ->
                        selectedReleaseMonth = m
                        selectedReleaseYear = y
                        showReleaseDatePicker = false
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val projection = FilmProductionMath.calculatePitchDeckProjection(
                budget = bLong,
                promoBudget = pLong,
                isGlobal = isGlobal,
                studioLevel = owned.level,
                productionFocus = selectedFocus,
                genreCount = selectedGenres.size
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A))), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("📊 Pitch Deck / Estimasi Analis", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Total Cost: ${formatCurrencyRingkas(projection.totalInvestment, useShortFormat)} (Prod: ${formatCurrencyRingkas(bLong, useShortFormat)} | Promo: ${formatCurrencyRingkas(pLong, useShortFormat)})",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        "Volatility Risk: ${projection.riskLevel}",
                        color = if (projection.riskLevel == "EXTREME" || projection.riskLevel == "High") Color.Red else Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        "Est. Box Office: ${formatCurrencyRingkas(projection.estBoxOfficeMin, useShortFormat)} - ${formatCurrencyRingkas(projection.estBoxOfficeMax, useShortFormat)}",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val releaseStr = if (effectiveReleaseMonth != null && effectiveReleaseYear != null) {
                        "$effectiveReleaseMonth/$effectiveReleaseYear"
                    } else null

                    val success = onProduce(
                        title.ifBlank { "Untitled Project" },
                        bLong,
                        pLong,
                        selectedGenres.toList(),
                        isGlobal,
                        selectedSchedMonth,
                        selectedSchedYear,
                        selectedFormat,
                        selectedFocus,
                        releaseStr,
                        estimatedDurationMonths
                    )
                    if (success) {
                        title = ""
                        budgetInput = ""
                        promoBudgetInput = ""
                        selectedSchedMonth = null
                        selectedSchedYear = null
                        selectedReleaseMonth = null
                        selectedReleaseYear = null
                    } else {
                        Toast.makeText(context, "Gagal memulai produksi! (Judul sudah digunakan atau error)", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = totalInvestment in 10000..playerCash && selectedGenres.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("🎬 MULAI PRODUKSI (Potong Saldo)", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }

            if (bLong > playerCash) {
                Text("Saldo tidak cukup!", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            } else if (bLong < 10000 && bLong > 0) {
                Text("Minimal budget $10,000", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }

    // Active Projects section
    FilmActiveProjectsCard(
        owned = owned,
        useShortFormat = useShortFormat,
        currentMonth = currentMonth,
        currentYear = currentYear,
        onPolish = onPolish,
        onSchedule = onSchedule,
        onCancel = onCancel
    )

    Button(
        onClick = onOpenHistory,
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Text("🎬 Buka Katalog IP & Histori Film")
    }
}
