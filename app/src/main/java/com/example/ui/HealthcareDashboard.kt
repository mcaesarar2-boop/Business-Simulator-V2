package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HealthcareEpidemicEvent
import com.example.data.HealthcareUnit
import com.example.data.OwnedBusiness

@Composable
fun HealthcareDashboard(
    units: List<HealthcareUnit>,
    activeEpidemic: HealthcareEpidemicEvent? = null,
    playerCash: Long,
    useShortFormat: Boolean,
    constructionVendors: List<OwnedBusiness>,
    onBuildUnit: (name: String, type: String, vendorId: String?, level: Int) -> Unit,
    onUpgradeDepartment: (unitId: String, department: String) -> Unit = { _, _ -> },
    onUpdatePremium: (unitId: String, premium: Double) -> Unit = { _, _ -> },
    onInjectCash: (unitId: String, amount: Long) -> Unit = { _, _ -> },
    onUpgradeTier: (unitId: String) -> Unit = { _ -> }
) {
    var showBuildDialog by remember { mutableStateOf(false) }
    var selectedUnitId by remember { mutableStateOf<String?>(null) }
    var showEmergencyInjectDialog by remember { mutableStateOf(false) }

    val currentSelectedUnit = units.find { it.id == selectedUnitId }

    val hospitalUnits = units.filter { it.type == "HOSPITAL" || it.type == "CLINIC" }
    val insuranceUnits = units.filter { it.type == "INSURANCE" }

    val totalPatients = hospitalUnits.sumOf { it.members }
    val totalInsured = insuranceUnits.sumOf { it.members }
    val combinedCash = units.sumOf { it.unitCash }
    val activeUnitsCount = units.size

    // Aggregate Actuary Loss Ratio
    val totalPremiumRev = insuranceUnits.sumOf { it.monthlyRevenue }
    val totalClaimsPaid = insuranceUnits.sumOf { it.lastMonthClaimsPaid }
    val aggregateLossRatio = if (totalPremiumRev > 0) (totalClaimsPaid / totalPremiumRev) * 100.0 else 0.0

    // Aggregate Synergy Rate
    val totalClaimsCount = insuranceUnits.sumOf { it.lastMonthClaimsCount }
    val totalInNetworkCount = insuranceUnits.sumOf { it.inNetworkClaimsCount }
    val aggregateSynergyRate = if (totalClaimsCount > 0) (totalInNetworkCount.toDouble() / totalClaimsCount) * 100.0 else 0.0

    if (currentSelectedUnit != null) {
        HealthcareUnitDetailScreen(
            unit = currentSelectedUnit,
            allHospitalUnits = hospitalUnits,
            playerCash = playerCash,
            useShortFormat = useShortFormat,
            activeEpidemic = activeEpidemic,
            onClose = { selectedUnitId = null },
            onUpgradeDepartment = { dept -> onUpgradeDepartment(currentSelectedUnit.id, dept) },
            onUpdatePremium = { prem -> onUpdatePremium(currentSelectedUnit.id, prem) },
            onInjectCash = { amt -> onInjectCash(currentSelectedUnit.id, amt) },
            onUpgradeTier = { onUpgradeTier(currentSelectedUnit.id) }
        )
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Healthcare & Insurance Holding",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Ekosistem Medis & Aktuaria Terintegrasi",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // EPIDEMIC EVENT ALERT BANNER
        if (activeEpidemic != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFD32F2F).copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(activeEpidemic.icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "PERINGATAN WABAH: ${activeEpidemic.title}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F),
                                fontSize = 14.sp
                            )
                            Text(
                                "Durasi Tersisa: ${activeEpidemic.remainingMonths} Bulan",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(activeEpidemic.description, fontSize = 12.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = Color(0xFFD32F2F).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "⚠️ ${activeEpidemic.effectDescription}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB71C1C),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (insuranceUnits.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showEmergencyInjectDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Healing, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Suntik Dana Cadangan Darurat (Reserve Fund)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // AGGREGATE TRANSPARENCY DASHBOARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Dashboard Transparansi Agregat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "$activeUnitsCount Unit",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Total Kas Internal Gabungan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        Text(
                            formatCurrencyRingkas(combinedCash.toLong(), useShortFormat),
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFF00796B)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Status Epidemi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        val epiText = if (activeEpidemic != null) "🚨 Waspada Wabah" else "🛡️ Aman (Kondusif)"
                        val epiColor = if (activeEpidemic != null) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                        Text(epiText, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = epiColor)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // NEW METRICS: Loss Ratio & Synergy Rate
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Loss Ratio Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Loss Ratio (Klaim)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val lossColor = when {
                                aggregateLossRatio > 85.0 -> Color(0xFFD32F2F)
                                aggregateLossRatio > 65.0 -> Color(0xFFF57C00)
                                else -> Color(0xFF2E7D32)
                            }
                            Text(
                                String.format("%.1f%%", aggregateLossRatio),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = lossColor
                            )
                            Text(
                                when {
                                    aggregateLossRatio > 85.0 -> "Klaim Tinggi (Rugi)"
                                    aggregateLossRatio > 65.0 -> "Moderat (Stabil)"
                                    insuranceUnits.isEmpty() -> "Belum ada asuransi"
                                    else -> "Profit Prima (Sehat)"
                                },
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    // Synergy Rate Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF6A1B9A))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Synergy Rate (In-Net)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                String.format("%.1f%%", aggregateSynergyRate),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF6A1B9A)
                            )
                            Text(
                                "Klaim dialirkan ke RS Sendiri",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // PATIENTS & INSURED SUMMARY CARDS
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Card(
                modifier = Modifier.weight(1f).padding(end = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, contentDescription = "Patients", tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pasien Terlayani", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatCurrencyRingkas(totalPatients, useShortFormat).replace("$", ""),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            Card(
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "Insurance", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Member Asuransi", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatCurrencyRingkas(totalInsured, useShortFormat).replace("$", ""),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Daftar Unit Medis & Asuransi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (units.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Belum ada unit medis yang dibangun.", color = Color.Gray, fontWeight = FontWeight.Medium)
                    Text("Bangun Rumah Sakit atau Asuransi untuk memulai ekosistem kesehatan terpadu.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            units.forEach { unit ->
                val icon = when (unit.type) {
                    "HOSPITAL" -> Icons.Default.LocalHospital
                    "INSURANCE" -> Icons.Default.Security
                    else -> Icons.Default.MedicalServices
                }
                val typeLabel = when (unit.type) {
                    "HOSPITAL" -> "Rumah Sakit"
                    "INSURANCE" -> "Asuransi Kesehatan"
                    else -> "Klinik Pratama"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedUnitId = unit.id },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when (unit.type) {
                                "HOSPITAL" -> Color(0xFF1976D2)
                                "INSURANCE" -> Color(0xFF7B1FA2)
                                else -> Color(0xFF00897B)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(unit.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            if (unit.isUpgrading) {
                                Text("🚧 Pembangunan (Sisa ${unit.upgradeDelayMonths} bln)", fontSize = 11.sp, color = Color(0xFFA66400))
                            } else {
                                if (unit.type == "INSURANCE") {
                                    Text("Tier: ${unit.tierCategory} | Premi: $${unit.monthlyPremium.toInt()}/bln", fontSize = 11.sp, color = Color.DarkGray)
                                    Text("Nasabah: ${formatCurrencyRingkas(unit.members, useShortFormat).replace("$", "")} | Loss Ratio: ${String.format("%.1f%%", unit.lossRatio)}", fontSize = 11.sp, color = Color.Gray)
                                } else {
                                    Text("Kapasitas Kasur: ${unit.totalBeds} Bed | BOR: ${String.format("%.1f%%", unit.bor)}", fontSize = 11.sp, color = Color.DarkGray)
                                    Text("IGD Lvl ${unit.igdLevel} • Spesialis Lvl ${unit.specialistLevel} • Farmasi Lvl ${unit.pharmacyLevel}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Kas Internal", fontSize = 10.sp, color = Color.Gray)
                            Text(
                                formatCurrencyRingkas(unit.unitCash.toLong(), useShortFormat),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (unit.unitCash >= 0) Color(0xFF00796B) else Color.Red
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Text("Detail >", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { showBuildDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Bangun Unit Medis / Asuransi Baru")
        }

        // BUILD DIALOG
        if (showBuildDialog) {
            var selectedType by remember { mutableStateOf("HOSPITAL") }
            var customName by remember { mutableStateOf("") }
            var selectedVendor by remember { mutableStateOf<String?>(null) }
            var hospitalLevel by remember { mutableStateOf(1f) }

            AlertDialog(
                onDismissRequest = { showBuildDialog = false },
                title = { Text("Bangun Unit Kesehatan") },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Pilih Tipe Unit:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedType = "HOSPITAL" }) {
                            RadioButton(selected = selectedType == "HOSPITAL", onClick = { selectedType = "HOSPITAL" })
                            Text("Rumah Sakit Umum (Provider)")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedType = "INSURANCE" }) {
                            RadioButton(selected = selectedType == "INSURANCE", onClick = { selectedType = "INSURANCE" })
                            Text("Asuransi Kesehatan ($2.00M - Payer)")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedType = "CLINIC" }) {
                            RadioButton(selected = selectedType == "CLINIC", onClick = { selectedType = "CLINIC" })
                            Text("Klinik Pratama ($150k)")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Nama Unit") },
                            placeholder = { Text(if (selectedType == "INSURANCE") "PT Asuransi Sehat Mandiri" else "RS Medika Utama") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (selectedType == "HOSPITAL") {
                            Spacer(modifier = Modifier.height(16.dp))
                            val cost = 500_000L + (hospitalLevel.toInt() * 100_000L)
                            Text("Skala Fasilitas Awal: Level ${hospitalLevel.toInt()} (${formatCurrencyRingkas(cost, useShortFormat)})")
                            Slider(value = hospitalLevel, onValueChange = { hospitalLevel = it }, valueRange = 1f..10f, steps = 8)
                        }

                        if (selectedType == "HOSPITAL" || selectedType == "CLINIC") {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Pilih Kontraktor Pembangunan:")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = selectedVendor == null, onClick = { selectedVendor = null })
                                Text("Vendor Eksternal (Instan)")
                            }
                            if (constructionVendors.isNotEmpty()) {
                                constructionVendors.forEach { vendor ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = selectedVendor == vendor.instanceId, onClick = { selectedVendor = vendor.instanceId })
                                        Column {
                                            Text("In-House: ${vendor.customName ?: "PT Konstruksi"}", fontSize = 14.sp)
                                            Text("Profit 40% masuk holding, Selesai 3 Bulan", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    val cost = when (selectedType) {
                        "HOSPITAL" -> 500_000L + (hospitalLevel.toInt() * 100_000L)
                        "INSURANCE" -> 2_000_000L
                        "CLINIC" -> 150_000L
                        else -> 0L
                    }
                    Button(
                        onClick = {
                            val finalName = customName.ifBlank {
                                when (selectedType) {
                                    "HOSPITAL" -> "RS Medika Nusantara"
                                    "INSURANCE" -> "Asuransi Jiwa & Kesehatan"
                                    "CLINIC" -> "Klinik Pratama Sehat"
                                    else -> "Unit Medis"
                                }
                            }
                            onBuildUnit(finalName, selectedType, selectedVendor, hospitalLevel.toInt())
                            showBuildDialog = false
                        },
                        enabled = playerCash >= cost
                    ) {
                        Text("Bangun")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBuildDialog = false }) { Text("Batal") }
                }
            )
        }

        // EMERGENCY INJECT DIALOG FOR ACTIVE EPIDEMIC
        if (showEmergencyInjectDialog) {
            var selectedInsId by remember { mutableStateOf(insuranceUnits.firstOrNull()?.id ?: "") }
            var injectAmount by remember { mutableStateOf(1_000_000L) }

            AlertDialog(
                onDismissRequest = { showEmergencyInjectDialog = false },
                title = { Text("Suntik Dana Cadangan Darurat") },
                text = {
                    Column {
                        Text("Pilih unit asuransi yang akan menerima suntikan modal darurat untuk menahan lonjakan klaim wabah:")
                        Spacer(modifier = Modifier.height(12.dp))
                        insuranceUnits.forEach { ins ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedInsId = ins.id }) {
                                RadioButton(selected = selectedInsId == ins.id, onClick = { selectedInsId = ins.id })
                                Column {
                                    Text(ins.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Kas Saat Ini: ${formatCurrencyRingkas(ins.unitCash.toLong(), useShortFormat)}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Pilih Nominal Suntikan:")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { injectAmount = 500_000L },
                                colors = if (injectAmount == 500_000L) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("$500k", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { injectAmount = 1_000_000L },
                                colors = if (injectAmount == 1_000_000L) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("$1.0M", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { injectAmount = 5_000_000L },
                                colors = if (injectAmount == 5_000_000L) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("$5.0M", fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Kas Pribadi Anda: ${formatCurrencyRingkas(playerCash, useShortFormat)}", fontSize = 12.sp, color = Color.DarkGray)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (selectedInsId.isNotBlank()) {
                                onInjectCash(selectedInsId, injectAmount)
                            }
                            showEmergencyInjectDialog = false
                        },
                        enabled = playerCash >= injectAmount && selectedInsId.isNotBlank()
                    ) {
                        Text("Suntik Modal")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEmergencyInjectDialog = false }) { Text("Batal") }
                }
            )
        }
    }
}

@Composable
fun HealthcareUnitDetailScreen(
    unit: HealthcareUnit,
    allHospitalUnits: List<HealthcareUnit>,
    playerCash: Long,
    useShortFormat: Boolean,
    activeEpidemic: HealthcareEpidemicEvent? = null,
    onClose: () -> Unit,
    onUpgradeDepartment: (department: String) -> Unit,
    onUpdatePremium: (premium: Double) -> Unit,
    onInjectCash: (amount: Long) -> Unit,
    onUpgradeTier: () -> Unit
) {
    var showInjectModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // TOP BAR
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(unit.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                val subtitle = when (unit.type) {
                    "HOSPITAL" -> "Rumah Sakit Umum (Provider Medis)"
                    "INSURANCE" -> "Unit Asuransi & Pengelolaan Risiko"
                    else -> "Klinik Pratama Rawat Jalan"
                }
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // FINANCIAL OVERVIEW CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Status Keuangan & Likuiditas Internal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Kas Internal Unit (Unit Cash)", fontSize = 13.sp)
                    Text(
                        formatCurrencyRingkas(unit.unitCash.toLong(), useShortFormat),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = if (unit.unitCash >= 0) Color(0xFF00796B) else Color.Red
                    )
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Pendapatan Bulan Lalu", fontSize = 13.sp)
                    Text(
                        formatCurrencyRingkas(unit.monthlyRevenue.toLong(), useShortFormat),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
                if (unit.reserveFund > 0) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Total Dana Cadangan Terinjeksi", fontSize = 12.sp, color = Color.Gray)
                        Text(formatCurrencyRingkas(unit.reserveFund.toLong(), useShortFormat), fontSize = 12.sp, color = Color(0xFF1976D2))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showInjectModal = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Suntik Kas / Dana Cadangan dari Kas Pribadi", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BRANCH SPECIFIC SCREEN: HOSPITAL / CLINIC vs INSURANCE
        if (unit.type == "HOSPITAL" || unit.type == "CLINIC") {
            HospitalDetailSection(
                unit = unit,
                useShortFormat = useShortFormat,
                activeEpidemic = activeEpidemic,
                onUpgradeDepartment = onUpgradeDepartment
            )
        } else if (unit.type == "INSURANCE") {
            InsuranceDetailSection(
                unit = unit,
                allHospitalUnits = allHospitalUnits,
                useShortFormat = useShortFormat,
                activeEpidemic = activeEpidemic,
                onUpdatePremium = onUpdatePremium,
                onUpgradeTier = onUpgradeTier
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // INJECT MODAL
    if (showInjectModal) {
        var injectAmt by remember { mutableStateOf(500_000L) }
        AlertDialog(
            onDismissRequest = { showInjectModal = false },
            title = { Text("Suntik Kas ke ${unit.name}") },
            text = {
                Column {
                    Text("Injeksi dana dari kas pribadi untuk memperkuat likuiditas unit atau membiayai departemen / klaim asuransi:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Kas Pribadi Anda: ${formatCurrencyRingkas(playerCash, useShortFormat)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Pilih Jumlah:")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(100_000L, 500_000L, 1_000_000L, 5_000_000L).forEach { amt ->
                            Button(
                                onClick = { injectAmt = amt },
                                colors = if (injectAmt == amt) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(formatCurrencyRingkas(amt, true).replace("$", ""), fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onInjectCash(injectAmt)
                        showInjectModal = false
                    },
                    enabled = playerCash >= injectAmt
                ) {
                    Text("Suntik Sekarang")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInjectModal = false }) { Text("Batal") }
            }
        )
    }
}

// -------------------------------------------------------------
// HOSPITAL & MEDICAL DEPARTMENT MANAGEMENT
// -------------------------------------------------------------
@Composable
fun HospitalDetailSection(
    unit: HealthcareUnit,
    useShortFormat: Boolean,
    activeEpidemic: HealthcareEpidemicEvent? = null,
    onUpgradeDepartment: (String) -> Unit
) {
    val borRatio = (unit.currentOccupiedBeds.toFloat() / maxOf(1, unit.totalBeds)).coerceIn(0f, 1f)
    val isFullCapacity = unit.bor >= 98.0

    // BED OCCUPANCY RATE (BOR) CARD
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isFullCapacity) Color(0xFFD32F2F).copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isFullCapacity) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Hotel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bed Occupancy Rate (BOR)", fontWeight = FontWeight.Bold)
                }
                Text(
                    String.format("%.1f%%", unit.bor),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (isFullCapacity) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { borRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = when {
                    borRatio >= 0.95f -> Color(0xFFD32F2F)
                    borRatio >= 0.75f -> Color(0xFFF57C00)
                    else -> Color(0xFF2E7D32)
                },
                trackColor = Color.LightGray.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Terisi: ${unit.currentOccupiedBeds} Kasur", fontSize = 12.sp, color = Color.DarkGray)
                Text("Total Kapasitas: ${unit.totalBeds} Kasur", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            if (isFullCapacity) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFD32F2F).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Kapasitas RS Penuh! Pasien ditolak: ${unit.rejectedPatientsLastMonth} orang bulan lalu.",
                            fontSize = 11.sp,
                            color = Color(0xFFB71C1C),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onUpgradeDepartment("BEDS") },
                enabled = unit.unitCash >= 100_000.0,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah Bangsal (+50 Kasur) - $100k")
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text("Manajemen Departemen Medis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Text("Tingkatkan kapasitas departemen untuk melayani pasien dan menaikkan omset.", fontSize = 12.sp, color = Color.Gray)
    Spacer(modifier = Modifier.height(12.dp))

    // 1. IGD DEPARTMENT
    val igdCost = unit.igdLevel * 80_000.0
    DepartmentCard(
        title = "Instalasi Gawat Darurat (IGD)",
        level = unit.igdLevel,
        icon = Icons.Default.Emergency,
        benefitText = "Menampung pasien darurat 24 jam & menambah +40 kasur inap/level.",
        upgradeCost = igdCost,
        unitCash = unit.unitCash,
        useShortFormat = useShortFormat,
        onUpgrade = { onUpgradeDepartment("IGD") }
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 2. SPECIALIST & SURGERY DEPARTMENT
    val specCost = unit.specialistLevel * 150_000.0
    DepartmentCard(
        title = "Poli Spesialis & Bedah",
        level = unit.specialistLevel,
        icon = Icons.Default.MedicalServices,
        benefitText = "Margin profit tinggi ($4,500/pasien) untuk tindakan rawat inap & bedah spesialis (+25 kasur).",
        upgradeCost = specCost,
        unitCash = unit.unitCash,
        useShortFormat = useShortFormat,
        onUpgrade = { onUpgradeDepartment("SPECIALIST") }
    )

    Spacer(modifier = Modifier.height(10.dp))

    // 3. PHARMACY & APOTEK
    val pharmCost = unit.pharmacyLevel * 50_000.0
    DepartmentCard(
        title = "Farmasi & Apotek Medis",
        level = unit.pharmacyLevel,
        icon = Icons.Default.LocalPharmacy,
        benefitText = "Pasif income obat ($600/pasien + $8k tetap/level/bulan).",
        upgradeCost = pharmCost,
        unitCash = unit.unitCash,
        useShortFormat = useShortFormat,
        onUpgrade = { onUpgradeDepartment("PHARMACY") }
    )
}

@Composable
fun DepartmentCard(
    title: String,
    level: Int,
    icon: ImageVector,
    benefitText: String,
    upgradeCost: Double,
    unitCash: Double,
    useShortFormat: Boolean,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Level $level", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(benefitText, fontSize = 11.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onUpgrade,
                enabled = unitCash >= upgradeCost,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Upgrade Level ${level + 1} (${formatCurrencyRingkas(upgradeCost.toLong(), useShortFormat)})", fontSize = 12.sp)
            }
            if (unitCash < upgradeCost) {
                Text("Kas internal unit tidak cukup.", color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// INSURANCE & ACTUARY MANAGEMENT SECTION
// -------------------------------------------------------------
@Composable
fun InsuranceDetailSection(
    unit: HealthcareUnit,
    allHospitalUnits: List<HealthcareUnit>,
    useShortFormat: Boolean,
    activeEpidemic: HealthcareEpidemicEvent? = null,
    onUpdatePremium: (Double) -> Unit,
    onUpgradeTier: () -> Unit
) {
    var sliderValue by remember(unit.id, unit.monthlyPremium) {
        mutableStateOf(unit.monthlyPremium.toFloat().coerceIn(20f, 500f))
    }

    // TIER BADGE & UPGRADE CARD
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tier Polis Asuransi", fontSize = 12.sp, color = Color.DarkGray)
                    Text(unit.tierCategory, fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF6A1B9A))
                }
                Surface(
                    color = Color(0xFF6A1B9A),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        when (unit.tierCategory) {
                            "ELITE" -> "Tier Tertinggi"
                            "PREMIUM" -> "Tier Menengah"
                            else -> "Tier Dasar"
                        },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            val (nextTier, upgradeCost) = when (unit.tierCategory) {
                "BASIC" -> "PREMIUM" to 1_500_000.0
                "PREMIUM" -> "ELITE" to 5_000_000.0
                else -> null to 0.0
            }

            if (nextTier != null) {
                Button(
                    onClick = onUpgradeTier,
                    enabled = unit.unitCash >= upgradeCost,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upgrade ke $nextTier (${formatCurrencyRingkas(upgradeCost.toLong(), useShortFormat)})", fontSize = 12.sp)
                }
            } else {
                Text("Polis Asuransi telah mencapai Tier Tertinggi (ELITE).", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 11.sp, color = Color.DarkGray)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // ACTUARY & PREMIUM SLIDER
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pengaturan Premi Bulanan (Aktuaria)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Tentukan besaran premi yang dibayarkan nasabah per bulan.", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Besaran Premi:", fontSize = 13.sp)
                Text("$${sliderValue.toInt()} / nasabah / bulan", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            }

            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onUpdatePremium(sliderValue.toDouble()) },
                valueRange = 20f..500f,
                steps = 47
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("$20 (Murah - Viral)", fontSize = 10.sp, color = Color.Gray)
                Text("$250 (Standar)", fontSize = 10.sp, color = Color.Gray)
                Text("$500 (Mahal - Eksklusif)", fontSize = 10.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("💡 Proyeksi Pertumbuhan Aktuaria:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text(
                        when {
                            sliderValue <= 40f -> "Pertumbuhan nasabah sangat pesat (+1,500 - 3,000/bln), namun risiko rugi bandar tinggi jika klaim melonjak."
                            sliderValue <= 80f -> "Pertumbuhan stabil (+800 - 1,800/bln) dengan margin aktuaria seimbang."
                            sliderValue <= 150f -> "Pertumbuhan selektif (+300 - 900/bln) dengan pendapatan per nasabah tinggi."
                            else -> "Segmen ultra-kaya (+50 - 200/bln). Sangat tahan terhadap lonjakan klaim."
                        },
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // CLAIM ATTACK & LOSS RATIO CARD
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Laporan Risiko & Klaim (Claim Attack)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Nasabah Aktif:", fontSize = 13.sp)
                Text("${unit.members} Orang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Klaim Terakhir:", fontSize = 13.sp)
                Text("${unit.lastMonthClaimsCount} Kasus (${formatCurrencyRingkas(unit.lastMonthClaimsPaid.toLong(), useShortFormat)})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFD32F2F))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Loss Ratio (Rasio Klaim):", fontSize = 13.sp)
                val lossColor = when {
                    unit.lossRatio > 85.0 -> Color(0xFFD32F2F)
                    unit.lossRatio > 65.0 -> Color(0xFFF57C00)
                    else -> Color(0xFF2E7D32)
                }
                Text(String.format("%.1f%%", unit.lossRatio), fontWeight = FontWeight.Black, fontSize = 14.sp, color = lossColor)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // SYNERGY BREAKDOWN (IN-NETWORK VS OUT-NETWORK)
            Text("The Ultimate Synergy (Routing Klaim)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF6A1B9A))
            Spacer(modifier = Modifier.height(8.dp))

            // IN-NETWORK
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("In-Network (RS Holding Sendiri)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1B5E20))
                        Text("${unit.inNetworkClaimsCount} Pasien • ${formatCurrencyRingkas(unit.inNetworkClaimsAmount.toLong(), useShortFormat)}", fontSize = 11.sp, color = Color.DarkGray)
                        Text("100% Uang klaim kembali masuk ke kas RS Holding!", fontSize = 10.sp, color = Color(0xFF2E7D32))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // OUT-NETWORK
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFD32F2F).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Out-Network (Bocor ke RS Lain)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFB71C1C))
                        Text("${unit.outNetworkClaimsCount} Pasien • ${formatCurrencyRingkas(unit.outNetworkClaimsAmount.toLong(), useShortFormat)}", fontSize = 11.sp, color = Color.DarkGray)
                        Text("Uang keluar karena kasur RS sendiri penuh / tidak cukup!", fontSize = 10.sp, color = Color(0xFFC62828))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Synergy Efficiency:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(String.format("%.1f%% Terkunci di Holding", unit.synergyRate), fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF6A1B9A))
            }
        }
    }
}
