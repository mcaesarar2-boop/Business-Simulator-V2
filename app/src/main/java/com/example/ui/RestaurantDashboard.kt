package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OwnedBusiness

import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

fun getRestaurantTierName(level: Int): Pair<String, Color> {
    return when (level) {
        in 1..10 -> Pair("Small F&B / Mini Restaurant", Color(0xFF8BC34A)) // Greenish
        in 11..30 -> Pair("Upper Restaurant", Color(0xFF2196F3)) // Blue
        in 31..40 -> Pair("Large F&B / Large Restaurant", Color(0xFFFF9800)) // Orange
        in 41..50 -> Pair("Big House Restography", Color(0xFFCDDC39)) // Gold/Lime-green
        else -> Pair("Big House Restography", Color(0xFFD4AF37))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDashboard(
    ownedBusiness: OwnedBusiness,
    playerCash: Long,
    useShortFormat: Boolean,
    onOpenBranch: (Long, String) -> Unit,
    onUpgradeBranch: (String, String, Long) -> Unit,
    onUpgradeParent: (Long) -> Unit = {},
    onFinishParentUpgrade: () -> Unit = {},
    onFinishBranchUpgrade: (String) -> Unit = {}
) {
    val branchCost = 500_000L
    var selectedBranch by remember { mutableStateOf<OwnedBusiness?>(null) }
    var showBranchDialog by remember { mutableStateOf(false) }
    var branchNameInput by remember { mutableStateOf("") }
    val branches = ownedBusiness.subsidiaries.filter { it.catalogId == "RESTAURANT_BRANCH" }
    
    // Ticking for realtime updates
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while(true) {
            currentTime = System.currentTimeMillis()
            delay(1000L)
        }
    }
    
    if (ownedBusiness.isUpgradingRealTime && ownedBusiness.upgradeEndTimeRealTime <= currentTime) {
        onFinishParentUpgrade()
    }
    
    branches.forEach { b ->
        if (b.isUpgradingRealTime && b.upgradeEndTimeRealTime <= currentTime) {
            onFinishBranchUpgrade(b.instanceId)
        }
    }

    // Aggregations
    val parentLevel = ownedBusiness.level
    val parentIncome = when {
        parentLevel in 1..10 -> 5000L + (parentLevel * 2000L)
        parentLevel in 11..30 -> 25000L + ((parentLevel - 10) * 8000L)
        parentLevel in 31..40 -> 185000L + ((parentLevel - 30) * 50000L)
        parentLevel in 41..50 -> 685000L + ((parentLevel - 40) * 200000L)
        else -> 685000L + ((parentLevel - 40) * 200000L)
    }

    var totalValuation = 0L
    var totalBranchIncome = 0L
    val baseIncomePerBranch = 15000L
    branches.forEach { b ->
        totalValuation += 500_000L + (b.level * 250_000L) + (b.michelinStars * 2_000_000L)
        val branchIncome = (baseIncomePerBranch * b.level) * (1.0 + (b.michelinStars * 1.5))
        totalBranchIncome += branchIncome.toLong()
    }

    val grandTotalIncome = parentIncome + totalBranchIncome

    val (tierName, tierColor) = getRestaurantTierName(parentLevel)
    val parentUpgradeCost = ((15000L * parentLevel) * (1.1 + (parentLevel.toDouble() / 10.0))).toLong()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🍽️ F&B Sub-Holding System", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            
            Spacer(modifier = Modifier.height(4.dp))
            // Tier badge with color
            Surface(
                color = tierColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, tierColor)
            ) {
                Text(
                    text = "$tierName (Lv. $parentLevel)",
                    color = tierColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Cabang", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text("${branches.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Valuasi Cabang", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text(formatCurrencyRingkas(totalValuation, useShortFormat), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Est. Pendapatan (Agregasi)", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Text("+${formatCurrencyRingkas(grandTotalIncome, useShortFormat)}/bln", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    Text("(Induk: ${formatCurrencyRingkas(parentIncome, useShortFormat)} + Cabang: ${formatCurrencyRingkas(totalBranchIncome, useShortFormat)})", fontSize = 11.sp, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // UPGRADES SECTION for Parent Restoran Induk
            if (parentLevel < 50) {
                Text("🔥 Upgrade Induk Tersedia", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                if (ownedBusiness.isUpgradingRealTime) {
                    val remainingMillis = ownedBusiness.upgradeEndTimeRealTime - currentTime
                    val remainingSeconds = (remainingMillis / 1000).coerceAtLeast(0)
                    val minutesForDisplay = remainingSeconds / 60
                    val secondsForDisplay = remainingSeconds % 60
                    val formattedTime = String.format("%02d:%02d", minutesForDisplay, secondsForDisplay)
                    
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(disabledContainerColor = Color.DarkGray.copy(alpha = 0.5f))
                    ) {
                        Text("🚧 Membangun... (Sisa: ${formattedTime}s)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { onUpgradeParent(parentUpgradeCost) },
                        enabled = ownedBusiness.companyCash >= parentUpgradeCost,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = tierColor)
                    ) {
                        Text("Upgrade Induk ke Lv. ${parentLevel + 1} (${formatCurrencyRingkas(parentUpgradeCost, useShortFormat)})", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Text("🍽️ Restoran Induk Maksimum (Lv. 50)", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Button(
                onClick = { showBranchDialog = true },
                enabled = ownedBusiness.companyCash >= branchCost, // PENTING: Validasi pakai class internal (companyCash)
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Buka Cabang Baru (${formatCurrencyRingkas(branchCost, useShortFormat)})")
            }
            if (ownedBusiness.companyCash < branchCost) {
                Text("Kas internal: ${formatCurrencyRingkas(ownedBusiness.companyCash.toLong(), useShortFormat)} - Butuh suntikan dana", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Daftar Cabang:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (branches.isEmpty()) {
                Text("Belum ada cabang.", color = Color.Gray, fontSize = 12.sp)
            } else {
                branches.forEach { branch ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { selectedBranch = branch },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(branch.customName ?: "Cabang", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                val statusText = if (branch.isUpgrading) "⏳ Renovasi / Upgrading... (${branch.upgradeDelayMonths} bln)" else "Lv. ${branch.level}/50  •  ⭐️ ${branch.michelinStars}"
                                val statusColor = if (branch.isUpgrading) Color(0xFFFFA000) else Color.Gray
                                Text(statusText, fontSize = 12.sp, color = statusColor)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
    
    if (showBranchDialog) {
        AlertDialog(
            onDismissRequest = { showBranchDialog = false },
            title = { Text("Buka Cabang Baru") },
            text = {
                Column {
                    Text("Pilih nama untuk cabang restoran baru Anda. Biaya renovasi & persiapan adalah ${formatCurrencyRingkas(branchCost, useShortFormat)}.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = branchNameInput,
                        onValueChange = { branchNameInput = it },
                        label = { Text("Nama Cabang") },
                        placeholder = { Text("Contoh: Cabang Jakarta") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onOpenBranch(branchCost, branchNameInput.trim())
                        showBranchDialog = false
                        branchNameInput = ""
                    }
                ) {
                    Text("Buka Cabang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBranchDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
    
    // Modal Dashboard Cabang (Individual Branch)
    if (selectedBranch != null) {
        val branch = selectedBranch!!
        // find latest instance just in case
        val liveBranch = ownedBusiness.subsidiaries.find { it.instanceId == branch.instanceId } ?: branch
        
        AlertDialog(
            onDismissRequest = { selectedBranch = null },
            title = { Text(liveBranch.customName ?: "Cabang Restoran") },
            text = {
                Column {
                    val branchIncome = (baseIncomePerBranch * liveBranch.level) * (1.0 + (liveBranch.michelinStars * 1.5))
                    Text("Level Cabang: ${liveBranch.level}/50", fontWeight = FontWeight.SemiBold)
                    Text("Bintang Michelin: ${liveBranch.michelinStars} ⭐️", fontWeight = FontWeight.SemiBold)
                    Text("Pemasukan Kas: ${formatCurrencyRingkas(branchIncome.toLong(), useShortFormat)}/bln", color = Color(0xFF4CAF50))
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val levelUpCost = 250_000L
                    val michelinCost = 1_000_000L
                    
                    if (liveBranch.isUpgradingRealTime) {
                        val remainingMillis = liveBranch.upgradeEndTimeRealTime - currentTime
                        val remainingSeconds = (remainingMillis / 1000).coerceAtLeast(0)
                        val minutesForDisplay = remainingSeconds / 60
                        val secondsForDisplay = remainingSeconds % 60
                        val formattedTime = String.format("%02d:%02d", minutesForDisplay, secondsForDisplay)
                        
                        Surface(modifier = Modifier.fillMaxWidth(), color = Color.LightGray.copy(alpha=0.3f), shape = RoundedCornerShape(8.dp)) {
                            Text("⏳ Sedang Memproses: ${liveBranch.pendingAction}\nSisa Waktu: ${formattedTime}s", modifier = Modifier.padding(12.dp), color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    } else if (liveBranch.isUpgrading) {
                        Surface(modifier = Modifier.fillMaxWidth(), color = Color.LightGray.copy(alpha=0.3f), shape = RoundedCornerShape(8.dp)) {
                            Text("⏳ Sedang Memproses: ${liveBranch.pendingAction}\nSisa Waktu: ${liveBranch.upgradeDelayMonths} bulan", modifier = Modifier.padding(12.dp), color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { 
                                onUpgradeBranch(liveBranch.instanceId, "LEVEL_UP", levelUpCost)
                                selectedBranch = null
                            },
                            enabled = ownedBusiness.companyCash >= levelUpCost && liveBranch.level < 50,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Upgrade Level (${formatCurrencyRingkas(levelUpCost, useShortFormat)})")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { 
                                onUpgradeBranch(liveBranch.instanceId, "MICHELIN_HUNT", michelinCost)
                                selectedBranch = null
                            },
                            enabled = ownedBusiness.companyCash >= michelinCost && liveBranch.michelinStars < 3,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val btnTxt = if (liveBranch.michelinStars < 3) "Hire Executive Chef (${formatCurrencyRingkas(michelinCost, useShortFormat)})" else "Maksimal Bintang (⭐️⭐️⭐️)"
                            Text(btnTxt, color = if (liveBranch.michelinStars < 3) Color.Black else Color.White)
                        }
                        if (ownedBusiness.companyCash < levelUpCost) {
                            Text("Kas internal tidak cukup untuk upgrade.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBranch = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}
