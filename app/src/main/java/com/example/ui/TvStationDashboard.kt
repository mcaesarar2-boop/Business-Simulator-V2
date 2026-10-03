package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvStationDashboard(
    activePrograms: List<com.example.data.TvProgram>,
    playerCash: Long,
    useShortFormat: Boolean,
    inGameYear: Int,
    businessLevel: Int,
    bookedTimeSlots: List<String>,
    onAddProgram: (String, String, Double, Boolean, Long, Int, List<String>) -> Boolean,
    onCancelProgram: (String) -> Unit,
    onEditSchedule: (String, List<String>) -> Unit
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var editScheduleProgramId by remember { mutableStateOf<String?>(null) }

    // Bidding War State
    var showBiddingDialog by remember { mutableStateOf(false) }
    var bidItemTitle by remember { mutableStateOf("") }
    var bidItemDuration by remember { mutableStateOf(-1) }
    var bidBasePrice by remember { mutableStateOf(0.0) }
    var currentRivalBid by remember { mutableStateOf(0.0) }
    var playerBidStr by remember { mutableStateOf("") }
    var bidMessage by remember { mutableStateOf("Jaringan TV Pesaing (Rival Network) ikut masuk dalam lelang. Masukkan penawaran Anda.") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Program Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { showAddSheet = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Text("Tambah Program / Hak Siar")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (activePrograms.isEmpty()) {
                Text("Belum ada program mengudara.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                activePrograms.sortedBy { it.timeSlots.firstOrNull() ?: "24:00" }.forEach { prog ->
                    val opsCost = prog.currentOperationalCost
                    val isProfit = prog.monthlyAdRevenue >= opsCost
                    val net = prog.monthlyAdRevenue - opsCost
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prog.title, fontWeight = FontWeight.Bold)
                                    val isPremium = prog.remainingMonths > 0
                                    
                                    val ratingDiff = prog.rating - prog.previousRating
                                    val ratingInd = if (ratingDiff > 0) " (↑)" else if (ratingDiff < 0) " (↓)" else ""
                                    Text("${prog.type} • Rating: ${java.lang.String.format("%.1f", prog.rating)}%$ratingInd", style = MaterialTheme.typography.bodySmall)
                                    
                                    if (prog.timeSlots.isNotEmpty()) {
                                        val sortedSlots = prog.timeSlots.sorted()
                                        val firstSlot = sortedSlots.first()
                                        val lastSlot = sortedSlots.last()
                                        val lastHour = lastSlot.substringBefore(":").toInt()
                                        val lastMin = lastSlot.substringAfter(":").toInt()
                                        var endMin = lastMin + 30
                                        var endHour = lastHour
                                        if (endMin >= 60) {
                                            endMin -= 60
                                            endHour += 1
                                        }
                                        if (endHour >= 24) endHour -= 24
                                        val endTimeStr = java.lang.String.format("%02d:%02d", endHour, endMin)
                                        Text("Jam: $firstSlot - $endTimeStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }

                                    if (isPremium) {
                                        Text("Sisa Kontrak: ${prog.remainingMonths} Bulan", style = MaterialTheme.typography.bodySmall, color = if (prog.remainingMonths in 1..2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Ad Rev: +${formatCurrencyRingkas(prog.monthlyAdRevenue.toLong(), useShortFormat)}", color = Color(0xFF00C853), style = MaterialTheme.typography.bodySmall)
                                    Text("Ops: -${formatCurrencyRingkas(opsCost.toLong(), useShortFormat)}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "Net: ${if (net >= 0) "+" else "-"}${formatCurrencyRingkas(kotlin.math.abs(net).toLong(), useShortFormat)}/bln", 
                                        color = if (isProfit) Color(0xFF00C853) else MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Button(onClick = { editScheduleProgramId = prog.id }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), modifier = Modifier.padding(end = 8.dp)) {
                                    Text("EDIT JADWAL")
                                }
                                Button(onClick = { onCancelProgram(prog.id) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                    Text("BUNGKUS")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editScheduleProgramId != null) {
        val progToEdit = activePrograms.find { it.id == editScheduleProgramId }
        var selectedSlots by remember { mutableStateOf(progToEdit?.timeSlots?.toSet() ?: emptySet()) }
        val allSlots = (0..23).flatMap { h -> listOf(String.format("%02d:00", h), String.format("%02d:30", h)) }

        AlertDialog(
            onDismissRequest = { editScheduleProgramId = null },
            title = { Text("Edit Jadwal: ${progToEdit?.title}") },
            text = {
                Column {
                    Text("Pilih Jam Tayang (Maks 2 Jam / 4 Slot)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(allSlots.size) { idx ->
                            val slot = allSlots[idx]
                            val isOtherBooked = bookedTimeSlots.contains(slot) && !(progToEdit?.timeSlots?.contains(slot) ?: false)
                            val isSelected = selectedSlots.contains(slot)
                            androidx.compose.material3.FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedSlots = selectedSlots - slot
                                    } else {
                                        if (selectedSlots.size < 4) {
                                            selectedSlots = selectedSlots + slot
                                        }
                                    }
                                },
                                label = { Text(slot, fontSize = 10.sp) },
                                enabled = !isOtherBooked,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEditSchedule(editScheduleProgramId!!, selectedSlots.toList().sorted())
                        editScheduleProgramId = null
                    },
                    enabled = selectedSlots.isNotEmpty()
                ) {
                    Text("Simpan Jadwal")
                }
            },
            dismissButton = {
                TextButton(onClick = { editScheduleProgramId = null }) { Text("Batal") }
            }
        )
    }

    if (showBiddingDialog) {
        AlertDialog(
            onDismissRequest = { showBiddingDialog = false },
            title = { Text("Bidding War: $bidItemTitle") },
            text = {
                Column {
                    Text(bidMessage, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tawaran Tertinggi Saat Ini (Rival): ${formatCurrencyRingkas(currentRivalBid.toLong(), useShortFormat)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = playerBidStr,
                        onValueChange = { playerBidStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Tawaran Anda ($)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val playerBid = playerBidStr.toDoubleOrNull() ?: 0.0
                        if (playerBid <= currentRivalBid) {
                            bidMessage = "Tawaran Anda harus lebih tinggi dari ${formatCurrencyRingkas(currentRivalBid.toLong(), useShortFormat)}!"
                        } else if (playerBid > playerCash) {
                            bidMessage = "Kas perusahaan Anda tidak mencukupi untuk tawaran ini!"
                        } else {
                            val isMajor = bidItemTitle.contains("World Cup") || bidItemTitle.contains("Euro") || bidItemTitle.contains("Champions League")
                            val rivalAggression = if (isMajor) 2.0 else 1.3
                            val maxRivalTol = currentRivalBid * rivalAggression
                            
                            val rivalGiveUpChance = if (playerBid > maxRivalTol) 0.9 else (playerBid - currentRivalBid) / (maxRivalTol - currentRivalBid + 1)
                            
                            if (Math.random() < rivalGiveUpChance) {
                                val success = onAddProgram(bidItemTitle, "Sports/Event", bidBasePrice, true, playerBid.toLong(), bidItemDuration, emptyList())
                                if (success) {
                                    showBiddingDialog = false
                                } else {
                                    android.widget.Toast.makeText(context, "Judul sudah digunakan!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val bumpFactor = 1.05 + (Math.random() * 0.1)
                                currentRivalBid = playerBid * bumpFactor
                                bidMessage = "RIVAL COUNTER-BID! Rival langsung menaikkan tawaran. Apakah Anda bersedia menaikkannya lagi?"
                            }
                        }
                    }
                ) {
                    Text("Ajukan Bid")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBiddingDialog = false }) { Text("Mundur") }
            }
        )
    }

    if (showAddSheet) {
        ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
            var selectedTab by remember { mutableStateOf(0) }
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().padding(bottom = 32.dp)) {
                Text("Tambah Program TV", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Siaran Internal") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Siaran Premium") })
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedTab == 0) {
                    var title by remember { mutableStateOf("") }
                    var type by remember { mutableStateOf("Sinetron") }
                    val types = listOf("Sinetron", "Sitkom", "Berita", "Talkshow", "Reality Show", "Pencarian Bakat (Talent Show)", "Dokumenter", "Animasi Anak", "FTV", "Kuis Interaktif (Game Show)", "Variety Show", "Late Night Show", "Investigasi Kriminal")
                    var typeExpanded by remember { mutableStateOf(false) }
                    var budgetStr by remember { mutableStateOf("") }
                    var selectedSlots by remember { mutableStateOf(setOf<String>()) }
                    val allSlots = (0..23).flatMap { h -> listOf(String.format("%02d:00", h), String.format("%02d:30", h)) }
                    
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Judul Program") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                        OutlinedTextField(
                            value = type, onValueChange = {}, readOnly = true,
                            label = { Text("Tipe Program") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            types.forEach { t ->
                                DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeExpanded = false })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = budgetStr, onValueChange = { budgetStr = it.filter { c -> c.isDigit() } }, label = { Text("Budget Produksi Awal ($)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Pilih Jam Tayang (Maks 2 Jam / 4 Slot)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(allSlots.size) { idx ->
                            val slot = allSlots[idx]
                            val isBooked = bookedTimeSlots.contains(slot)
                            val isSelected = selectedSlots.contains(slot)
                            androidx.compose.material3.FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedSlots = selectedSlots - slot
                                    } else {
                                        if (selectedSlots.size < 4) {
                                            selectedSlots = selectedSlots + slot
                                        }
                                    }
                                },
                                label = { Text(slot, fontSize = 10.sp) },
                                enabled = !isBooked,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val budg = budgetStr.toDoubleOrNull() ?: 0.0
                    Button(
                        onClick = {
                            if (title.isNotEmpty() && budg > 0 && selectedSlots.isNotEmpty()) {
                                val success = onAddProgram(title, type, budg, false, budg.toLong(), -1, selectedSlots.toList().sorted())
                                if (success) {
                                    showAddSheet = false
                                } else {
                                    android.widget.Toast.makeText(context, "Judul sudah digunakan!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = title.isNotEmpty() && budg > 0 && playerCash >= budg.toLong() && selectedSlots.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (playerCash >= budg.toLong()) "Produksi Sekarang" else "Dana Tidak Cukup")
                    }
                } else {
                    val isWorldCupYear = inGameYear % 4 == 0
                    
                    data class PremiumRight(val title: String, val basePrice: Double, val requiredLevel: Int, val durationMonths: Int)
                    
                    val premiumOptions = mutableListOf(
                        PremiumRight("Liga 1 Indonesia", 50000000.0, 20, 10),
                        PremiumRight("AFC Cup", 60000000.0, 20, 10),
                        PremiumRight("Liga Africa", 70000000.0, 30, 10),
                        PremiumRight("Liga Arab Saudi", 80000000.0, 30, 10),
                        PremiumRight("DFB Pokal", 110000000.0, 40, 10),
                        PremiumRight("FA Cup", 120000000.0, 40, 10),
                        PremiumRight("Super Copa", 90000000.0, 40, 10),
                        PremiumRight("Serie A", 180000000.0, 40, 10),
                        PremiumRight("La Liga", 250000000.0, 40, 10),
                        PremiumRight("Bundesliga", 150000000.0, 50, 10),
                        PremiumRight("F1", 200000000.0, 50, 10),
                        PremiumRight("Premier League", 300000000.0, 60, 10),
                        PremiumRight("UEFA Champions League", 400000000.0, 60, 10)
                    )
                    
                    if (isWorldCupYear) {
                        premiumOptions.add(0, PremiumRight("FIFA World Cup (Major Event)", 800000000.0, 70, 2))
                        premiumOptions.add(1, PremiumRight("UEFA Euro (Major Event)", 600000000.0, 70, 2))
                        premiumOptions.add(2, PremiumRight("FIFA Club World Cup", 300000000.0, 70, 2))
                    }
                    
                    LazyColumn {
                        items(count = premiumOptions.size) { idx ->
                            val opt = premiumOptions[idx]
                            val isLocked = businessLevel < opt.requiredLevel
                            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(opt.title, fontWeight = FontWeight.Bold)
                                        Text("Base/Start Price: ${formatCurrencyRingkas(opt.basePrice.toLong(), useShortFormat)}", style = MaterialTheme.typography.bodySmall)
                                        Text("Kontrak: ${opt.durationMonths} Bulan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { 
                                            bidItemTitle = opt.title
                                            bidItemDuration = opt.durationMonths
                                            bidBasePrice = opt.basePrice
                                            currentRivalBid = opt.basePrice * (1.0 + Math.random() * 0.1) // Rival starts slightly above base price
                                            playerBidStr = ""
                                            bidMessage = "Jaringan TV Pesaing (Rival Network) ikut masuk dalam lelang. Masukkan penawaran Anda."
                                            showAddSheet = false 
                                            showBiddingDialog = true
                                        },
                                        enabled = !isLocked,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isLocked) Color.Gray else MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text(if (isLocked) "Terkunci (Lvl ${opt.requiredLevel})" else "Ikut Lelang")
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
