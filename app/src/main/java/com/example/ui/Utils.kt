package com.example.ui

import com.example.viewmodel.GameViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Fungsi untuk menghitung harga baru dari aset yang fluktuatif menggunakan Random Walk dengan Drift.
 *
 * @param currentPrice Harga aset saat ini
 * @param volatility Tingkat keliaran/volatilitas aset (Contoh: 0.05 untuk 5% per tick)
 * @param trend Kecenderungan pasar / drift (Contoh: 0.001 untuk tren naik tipis)
 * @param eventShock Efek berita/kejutan eksternal (Contoh: -0.10 jika ada krisis/crash)
 * @param minPrice Batas bawah harga agar tidak bernilai 0 atau negatif
 * @return Harga baru aset
 */
fun calculateFluctuatingPrice(
    currentPrice: Double,
    volatility: Double,
    trend: Double,
    eventShock: Double = 0.0,
    minPrice: Double = 0.01
): Double {
    // 1. Random factor antara -1.0 sampai 1.0
    val randomFactor = (Math.random() * 2.0) - 1.0
    
    // 2. Pergerakan pasar murni dari trend + volatilitas
    val marketMovement = trend + (randomFactor * volatility)
    
    // 3. Gabungkan pergerakan pasar dengan kejutan eksternal ( event shock )
    val totalPercentageChange = marketMovement + eventShock
    
    // 4. Hitung harga baru
    var newPrice = currentPrice * (1.0 + totalPercentageChange)
    
    // 5. Pastikan harga tidak pernah berada di bawah batas minimal
    if (newPrice < minPrice) {
        newPrice = minPrice
    }
    
    return newPrice
}

fun formatMarketCap(value: Double): String {
    val absVal = Math.abs(value)
    
    // Format full number using standard dot separators for thousands
    val numberFormat = java.text.NumberFormat.getNumberInstance(java.util.Locale.US).apply {
        maximumFractionDigits = 0
    }
    val fullFormatted = numberFormat.format(value)
    
    val (suffix, divisor) = when {
        absVal >= 1_000_000_000_000.0 -> "Trillion" to 1_000_000_000_000.0
        absVal >= 1_000_000_000.0 -> "Billion" to 1_000_000_000.0
        absVal >= 1_000_000.0 -> "Million" to 1_000_000.0
        else -> "" to 1.0
    }
    
    return if (suffix.isNotEmpty()) {
        val shortVal = value / divisor
        val shortFormatted = String.format(java.util.Locale.US, "%.2f", shortVal)
        "$$fullFormatted ($shortFormatted $suffix)"
    } else {
        "$$fullFormatted"
    }
}

fun formatGlobalDate(month: Int, year: Int): String {
    val baseYear = 2019
    val currentYear = baseYear + year
    val monthNames = arrayOf(
        "Januari", "Februari", "Maret", "April", "Mei", "Juni", 
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    )
    val monthIndex = (month - 1).coerceIn(0, 11)
    val monthName = monthNames[monthIndex]
    
    return "$monthName $currentYear"
}

fun formatCurrencyRingkas(amount: Number, isShort: Boolean): String {
    val currencyFormat = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US).apply { 
        maximumFractionDigits = 0 
    }
    if (!isShort) return currencyFormat.format(amount)
    
    val doubleAmount = amount.toDouble()
    val absVal = Math.abs(doubleAmount)
    val (suffix, divisor) = when {
        absVal >= 1_000_000_000_000.0 -> "T" to 1_000_000_000_000.0
        absVal >= 1_000_000_000.0 -> "B" to 1_000_000_000.0
        absVal >= 1_000_000.0 -> "M" to 1_000_000.0
        absVal >= 1_000.0 -> "K" to 1_000.0
        else -> return currencyFormat.format(amount)
    }
    
    val shortVal = doubleAmount / divisor
    val shortStr = String.format(java.util.Locale.US, "%.1f", shortVal).replace(".0", "")
    return if (doubleAmount < 0) "-\$$shortStr$suffix" else "\$$shortStr$suffix"
}

@androidx.compose.runtime.Composable
fun ChecklistItem(label: String, current: Int, target: Int, isOk: Boolean) {
    androidx.compose.foundation.layout.Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            androidx.compose.material3.Text(
                text = if (isOk) "✅" else "⬜",
                fontSize = 14.sp
            )
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
            androidx.compose.material3.Text(
                text = label,
                color = if (isOk) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.LightGray,
                fontSize = 13.sp
            )
        }
        androidx.compose.material3.Text(
            text = "$current / $target",
            color = if (isOk) androidx.compose.ui.graphics.Color(0xFF81C784) else androidx.compose.ui.graphics.Color(0xFFE57373),
            fontSize = 13.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

@androidx.compose.runtime.Composable
fun SdmManagementPanel(
    foundationId: String,
    institution: com.example.data.EducationInstitution,
    viewModel: GameViewModel
) {
    var activeTab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    
    val premiumList = listOf(
        "Montessori", "Waldorf", "Nasional Plus (Bilingual)", "Cambridge Primary", "Cambridge", "IB", "Internasional",
        "Cambridge (A-Level)", "IB (International Baccalaureate)",
        "Internasional (Double Degree)", "World-Class Research Univ"
    )
    val isPremium = premiumList.contains(institution.curriculumType)
    
    // Calculate teacher requirements
    val teacherRatio = when (institution.level) {
        "TK" -> 20
        "SD" -> 25
        "SMA" -> 30
        else -> 20
    }
    val idealTeachers = Math.max(2, (institution.currentStudents + (teacherRatio - 1)) / teacherRatio)
    val actualTeachers = institution.teachers.umum.active + institution.teachers.spesialis.active + institution.teachers.senior.active
    val teacherShortage = actualTeachers < idealTeachers
    
    // Calculate support staff requirements
    val builtFacCount = institution.additionalFacilities.count { it.constructionLeftMonths <= 0 }
    val idealOb = 1 + (builtFacCount / 4)
    val actualOb = institution.supportStaff.ob.active
    val obShortage = actualOb < idealOb
    
    val idealSecurity = Math.max(1, (institution.currentStudents + 99) / 100)
    val actualSecurity = institution.supportStaff.satpam.active
    val securityShortage = actualSecurity < idealSecurity
    
    val idealAdmin = Math.max(1, (institution.currentStudents + 149) / 150)
    val actualAdmin = institution.supportStaff.admin.active
    val adminShortage = actualAdmin < idealAdmin
    
    val idealChef = if (isPremium) Math.max(1, (institution.currentStudents + 99) / 100) else 0
    val actualChef = institution.supportStaff.chef.active
    val chefShortage = isPremium && (actualChef < idealChef)
    
    val staffShortage = obShortage || securityShortage || adminShortage || chefShortage

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(
                1.dp,
                Color(0xFFD4AF37).copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Manajemen Staff & Pengajar",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Tab Row
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color(0xFF0B121E),
                contentColor = Color(0xFFD4AF37),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = Color(0xFFD4AF37)
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Tenaga Pengajar", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Staff Penunjang", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (activeTab == 0) {
                // UI Tenaga Pengajar
                // Info ratio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rasio Siswa vs Guru (1:$teacherRatio)", color = Color.Gray, fontSize = 12.sp)
                    Text("$actualTeachers / $idealTeachers Guru", fontWeight = FontWeight.Bold, color = if (teacherShortage) Color(0xFFE57373) else Color(0xFF81C784), fontSize = 12.sp)
                }
                
                if (teacherShortage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE57373).copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE57373), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⚠️ Kekurangan Staff! Operasional terganggu.", color = Color(0xFFE57373), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Salary Edit Dialog State
                var showEditSalaryDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                var dialogRoleTitle by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
                var dialogRoleType by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
                var dialogIsTeacher by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
                var dialogCurrentSalary by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0L) }
                var editSalaryValue by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

                if (showEditSalaryDialog) {
                    AlertDialog(
                        onDismissRequest = { showEditSalaryDialog = false },
                        title = { Text("Atur Gaji Bulanan - $dialogRoleTitle", fontWeight = FontWeight.Bold, color = Color.White) },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("Masukkan jumlah gaji bulanan baru untuk seluruh staff dengan peran ini (USD):", color = Color.LightGray, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = editSalaryValue,
                                    onValueChange = { editSalaryValue = it.filter { char -> char.isDigit() } },
                                    label = { Text("Gaji Bulanan ($)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFD4AF37),
                                        unfocusedBorderColor = Color.Gray,
                                        focusedLabelColor = Color(0xFFD4AF37),
                                        cursorColor = Color(0xFFD4AF37)
                                    )
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val newSal = editSalaryValue.toLongOrNull() ?: dialogCurrentSalary
                                    viewModel.updateStaffSalary(foundationId, institution.id, dialogIsTeacher, dialogRoleType, newSal)
                                    showEditSalaryDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                            ) {
                                Text("Simpan", color = Color(0xFF101B2B), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditSalaryDialog = false }) {
                                Text("Batal", color = Color.Gray)
                            }
                        },
                        containerColor = Color(0xFF101B2B)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Staff rows
                StaffHireRow(
                    title = "Guru Umum",
                    salary = institution.teachers.umum.customSalary,
                    active = institution.teachers.umum.active,
                    recruiting = institution.teachers.umum.recruiting,
                    target = institution.teachers.umum.target,
                    onHire = { viewModel.hireTeacher(foundationId, institution.id, "umum") },
                    onFire = { viewModel.fireTeacher(foundationId, institution.id, "umum") },
                    onEditSalary = { sal ->
                        dialogRoleTitle = "Guru Umum"
                        dialogRoleType = "umum"
                        dialogIsTeacher = true
                        dialogCurrentSalary = sal
                        editSalaryValue = sal.toString()
                        showEditSalaryDialog = true
                    }
                )
                StaffHireRow(
                    title = "Guru Spesialis",
                    salary = institution.teachers.spesialis.customSalary,
                    active = institution.teachers.spesialis.active,
                    recruiting = institution.teachers.spesialis.recruiting,
                    target = institution.teachers.spesialis.target,
                    onHire = { viewModel.hireTeacher(foundationId, institution.id, "spesialis") },
                    onFire = { viewModel.fireTeacher(foundationId, institution.id, "spesialis") },
                    onEditSalary = { sal ->
                        dialogRoleTitle = "Guru Spesialis"
                        dialogRoleType = "spesialis"
                        dialogIsTeacher = true
                        dialogCurrentSalary = sal
                        editSalaryValue = sal.toString()
                        showEditSalaryDialog = true
                    }
                )
                StaffHireRow(
                    title = "Guru Senior",
                    salary = institution.teachers.senior.customSalary,
                    active = institution.teachers.senior.active,
                    recruiting = institution.teachers.senior.recruiting,
                    target = institution.teachers.senior.target,
                    onHire = { viewModel.hireTeacher(foundationId, institution.id, "senior") },
                    onFire = { viewModel.fireTeacher(foundationId, institution.id, "senior") },
                    onEditSalary = { sal ->
                        dialogRoleTitle = "Guru Senior"
                        dialogRoleType = "senior"
                        dialogIsTeacher = true
                        dialogCurrentSalary = sal
                        editSalaryValue = sal.toString()
                        showEditSalaryDialog = true
                    }
                )
            } else {
                // UI Staff Penunjang
                // Info ratio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rasio Fasilitas vs OB (1 OB per 4 Fas)", color = Color.Gray, fontSize = 12.sp)
                    Text("$actualOb / $idealOb OB", fontWeight = FontWeight.Bold, color = if (obShortage) Color(0xFFE57373) else Color(0xFF81C784), fontSize = 12.sp)
                }
                
                if (staffShortage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE57373).copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE57373), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⚠️ Kekurangan Staff! Operasional terganggu.", color = Color(0xFFE57373), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                // Salary Edit Dialog State
                var showEditSalaryDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                var dialogRoleTitle by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
                var dialogRoleType by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
                var dialogIsTeacher by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                var dialogCurrentSalary by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0L) }
                var editSalaryValue by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

                if (showEditSalaryDialog) {
                    AlertDialog(
                        onDismissRequest = { showEditSalaryDialog = false },
                        title = { Text("Atur Gaji Bulanan - $dialogRoleTitle", fontWeight = FontWeight.Bold, color = Color.White) },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text("Masukkan jumlah gaji bulanan baru untuk seluruh staff dengan peran ini (USD):", color = Color.LightGray, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = editSalaryValue,
                                    onValueChange = { editSalaryValue = it.filter { char -> char.isDigit() } },
                                    label = { Text("Gaji Bulanan ($)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFD4AF37),
                                        unfocusedBorderColor = Color.Gray,
                                        focusedLabelColor = Color(0xFFD4AF37),
                                        cursorColor = Color(0xFFD4AF37)
                                    )
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val newSal = editSalaryValue.toLongOrNull() ?: dialogCurrentSalary
                                    viewModel.updateStaffSalary(foundationId, institution.id, dialogIsTeacher, dialogRoleType, newSal)
                                    showEditSalaryDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
                            ) {
                                Text("Simpan", color = Color(0xFF101B2B), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditSalaryDialog = false }) {
                                Text("Batal", color = Color.Gray)
                            }
                        },
                        containerColor = Color(0xFF101B2B)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                StaffHireRow(
                    title = "OB / Janitor",
                    salary = institution.supportStaff.ob.customSalary,
                    active = institution.supportStaff.ob.active,
                    recruiting = institution.supportStaff.ob.recruiting,
                    target = institution.supportStaff.ob.target,
                    onHire = { viewModel.hireSupportStaff(foundationId, institution.id, "janitor") },
                    onFire = { viewModel.fireSupportStaff(foundationId, institution.id, "janitor") },
                    onEditSalary = { sal ->
                        dialogRoleTitle = "OB / Janitor"
                        dialogRoleType = "janitor"
                        dialogIsTeacher = false
                        dialogCurrentSalary = sal
                        editSalaryValue = sal.toString()
                        showEditSalaryDialog = true
                    }
                )
                StaffHireRow(
                    title = "Satpam / Security",
                    salary = institution.supportStaff.satpam.customSalary,
                    active = institution.supportStaff.satpam.active,
                    recruiting = institution.supportStaff.satpam.recruiting,
                    target = institution.supportStaff.satpam.target,
                    onHire = { viewModel.hireSupportStaff(foundationId, institution.id, "security") },
                    onFire = { viewModel.fireSupportStaff(foundationId, institution.id, "security") },
                    onEditSalary = { sal ->
                        dialogRoleTitle = "Satpam / Security"
                        dialogRoleType = "security"
                        dialogIsTeacher = false
                        dialogCurrentSalary = sal
                        editSalaryValue = sal.toString()
                        showEditSalaryDialog = true
                    }
                )
                StaffHireRow(
                    title = "Tata Usaha / Admin",
                    salary = institution.supportStaff.admin.customSalary,
                    active = institution.supportStaff.admin.active,
                    recruiting = institution.supportStaff.admin.recruiting,
                    target = institution.supportStaff.admin.target,
                    onHire = { viewModel.hireSupportStaff(foundationId, institution.id, "admin") },
                    onFire = { viewModel.fireSupportStaff(foundationId, institution.id, "admin") },
                    onEditSalary = { sal ->
                        dialogRoleTitle = "Tata Usaha / Admin"
                        dialogRoleType = "admin"
                        dialogIsTeacher = false
                        dialogCurrentSalary = sal
                        editSalaryValue = sal.toString()
                        showEditSalaryDialog = true
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))
                
                if (isPremium) {
                    StaffHireRow(
                        title = "Koki / Ahli Gizi",
                        salary = institution.supportStaff.chef.customSalary,
                        active = institution.supportStaff.chef.active,
                        recruiting = institution.supportStaff.chef.recruiting,
                        target = institution.supportStaff.chef.target,
                        onHire = { viewModel.hireSupportStaff(foundationId, institution.id, "chef") },
                        onFire = { viewModel.fireSupportStaff(foundationId, institution.id, "chef") },
                        onEditSalary = { sal ->
                            dialogRoleTitle = "Koki / Ahli Gizi"
                            dialogRoleType = "chef"
                            dialogIsTeacher = false
                            dialogCurrentSalary = sal
                            editSalaryValue = sal.toString()
                            showEditSalaryDialog = true
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Chef / Ahli Gizi (Gaji: $2,500/bln)", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("🔒 Terkunci - Hanya untuk Kurikulum Premium", color = Color.Gray.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun StaffHireRow(
    title: String,
    salary: Long,
    active: Int,
    recruiting: Int,
    target: Int,
    onHire: () -> Unit,
    onFire: () -> Unit,
    onEditSalary: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$active Aktif", color = Color.LightGray, fontSize = 11.sp)
                if (recruiting > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("•  ⏳ $recruiting Proses Rekrut", color = Color(0xFFD4AF37), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Gaji: $${com.example.ui.formatCurrencyRingkas(salary, false)}/bln", color = Color.LightGray.copy(alpha = 0.6f), fontSize = 10.sp)
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = { onEditSalary(salary) },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Gaji",
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onFire,
                enabled = target > 0,
                modifier = Modifier.size(28.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFFE57373).copy(alpha = 0.2f),
                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Kurangi", tint = if (target > 0) Color(0xFFE57373) else Color.Gray, modifier = Modifier.size(16.dp))
            }
            Text(
                text = target.toString(),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.widthIn(min = 20.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = onHire,
                modifier = Modifier.size(28.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFF81C784).copy(alpha = 0.2f),
                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Target", tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun MedicalSdmManagementPanel(
    foundationId: String,
    institution: com.example.data.HealthInstitution,
    viewModel: GameViewModel
) {
    var activeTab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    
    val premiumList = listOf("VIP", "VVIP")
    val isPremium = premiumList.contains(institution.serviceType)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(
                1.dp,
                Color(0xFF2E7D32).copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF102722)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Manajemen Staff Medis & Penunjang",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color(0xFF091E1A),
                contentColor = Color(0xFF81C784),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = Color(0xFF81C784)
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Tenaga Medis", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Staff Penunjang", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            var showSalaryDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            var salaryTargetRole by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
            var salaryIsMedical by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
            var salaryInputValue by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
            
            if (activeTab == 0) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MedicalStaffStepper(
                        label = "Perawat",
                        role = institution.medicalStaff.perawat,
                        onHire = { viewModel.hireMedicalStaff(foundationId, institution.id, "perawat") },
                        onFire = { viewModel.fireMedicalStaff(foundationId, institution.id, "perawat") },
                        onEditSalary = {
                            salaryTargetRole = "perawat"
                            salaryIsMedical = true
                            salaryInputValue = institution.medicalStaff.perawat.customSalary.toString()
                            showSalaryDialog = true
                        }
                    )
                    
                    MedicalStaffStepper(
                        label = "Dokter Umum",
                        role = institution.medicalStaff.dokterUmum,
                        onHire = { viewModel.hireMedicalStaff(foundationId, institution.id, "dokterUmum") },
                        onFire = { viewModel.fireMedicalStaff(foundationId, institution.id, "dokterUmum") },
                        onEditSalary = {
                            salaryTargetRole = "dokterUmum"
                            salaryIsMedical = true
                            salaryInputValue = institution.medicalStaff.dokterUmum.customSalary.toString()
                            showSalaryDialog = true
                        }
                    )
                    
                    MedicalStaffStepper(
                        label = "Dokter Spesialis",
                        role = institution.medicalStaff.dokterSpesialis,
                        onHire = { viewModel.hireMedicalStaff(foundationId, institution.id, "dokterSpesialis") },
                        onFire = { viewModel.fireMedicalStaff(foundationId, institution.id, "dokterSpesialis") },
                        onEditSalary = {
                            salaryTargetRole = "dokterSpesialis"
                            salaryIsMedical = true
                            salaryInputValue = institution.medicalStaff.dokterSpesialis.customSalary.toString()
                            showSalaryDialog = true
                        }
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MedicalStaffStepper(
                        label = "Staff Kebersihan (Janitor)",
                        role = institution.supportStaff.ob,
                        onHire = { viewModel.hireHealthSupportStaff(foundationId, institution.id, "janitor") },
                        onFire = { viewModel.fireHealthSupportStaff(foundationId, institution.id, "janitor") },
                        onEditSalary = {
                            salaryTargetRole = "janitor"
                            salaryIsMedical = false
                            salaryInputValue = institution.supportStaff.ob.customSalary.toString()
                            showSalaryDialog = true
                        }
                    )
                    
                    MedicalStaffStepper(
                        label = "Petugas Keamanan (Security)",
                        role = institution.supportStaff.satpam,
                        onHire = { viewModel.hireHealthSupportStaff(foundationId, institution.id, "security") },
                        onFire = { viewModel.fireHealthSupportStaff(foundationId, institution.id, "security") },
                        onEditSalary = {
                            salaryTargetRole = "security"
                            salaryIsMedical = false
                            salaryInputValue = institution.supportStaff.satpam.customSalary.toString()
                            showSalaryDialog = true
                        }
                    )
                    
                    MedicalStaffStepper(
                        label = "Staf Administrasi (Admin)",
                        role = institution.supportStaff.admin,
                        onHire = { viewModel.hireHealthSupportStaff(foundationId, institution.id, "admin") },
                        onFire = { viewModel.fireHealthSupportStaff(foundationId, institution.id, "admin") },
                        onEditSalary = {
                            salaryTargetRole = "admin"
                            salaryIsMedical = false
                            salaryInputValue = institution.supportStaff.admin.customSalary.toString()
                            showSalaryDialog = true
                        }
                    )
                    
                    MedicalStaffStepper(
                        label = "Ahli Gizi (Chef)",
                        role = institution.supportStaff.chef,
                        onHire = { viewModel.hireHealthSupportStaff(foundationId, institution.id, "chef") },
                        onFire = { viewModel.fireHealthSupportStaff(foundationId, institution.id, "chef") },
                        onEditSalary = {
                            salaryTargetRole = "chef"
                            salaryIsMedical = false
                            salaryInputValue = institution.supportStaff.chef.customSalary.toString()
                            showSalaryDialog = true
                        }
                    )
                }
            }
            
            if (showSalaryDialog) {
                androidx.compose.ui.window.Dialog(onDismissRequest = { showSalaryDialog = false }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF102722)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Atur Gaji Kustom", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                            Text("Tentukan gaji bulanan per kepala staff untuk mempercepat rekruitmen atau menghemat anggaran.", color = Color.LightGray, fontSize = 11.sp)
                            
                            OutlinedTextField(
                                value = salaryInputValue,
                                onValueChange = { salaryInputValue = it },
                                label = { Text("Gaji Bulanan (Rp)", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF81C784),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showSalaryDialog = false }) {
                                    Text("Batal", color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val amt = salaryInputValue.toLongOrNull() ?: 0L
                                        if (amt > 0) {
                                            viewModel.updateHealthStaffSalary(
                                                foundationId = foundationId,
                                                institutionId = institution.id,
                                                isMedical = salaryIsMedical,
                                                roleType = salaryTargetRole,
                                                newSalary = amt
                                            )
                                        }
                                        showSalaryDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Text("Simpan", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun MedicalStaffStepper(
    label: String,
    role: com.example.data.StaffRole,
    onHire: () -> Unit,
    onFire: () -> Unit,
    onEditSalary: () -> Unit
) {
    val active = role.active
    val target = role.target
    val salary = role.customSalary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF091E1A), RoundedCornerShape(10.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "Aktif: $active", color = Color.LightGray, fontSize = 11.sp)
                Text(text = "•", color = Color.Gray, fontSize = 11.sp)
                Text(
                    text = "Gaji: ${com.example.ui.formatCurrency(salary)}",
                    color = Color(0xFF81C784),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onEditSalary, modifier = Modifier.size(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Gaji",
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onFire,
                enabled = target > 0,
                modifier = Modifier.size(28.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFFE57373).copy(alpha = 0.2f),
                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Kurangi", tint = if (target > 0) Color(0xFFE57373) else Color.Gray, modifier = Modifier.size(16.dp))
            }
            Text(
                text = target.toString(),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.widthIn(min = 20.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = onHire,
                modifier = Modifier.size(28.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color(0xFF81C784).copy(alpha = 0.2f),
                    disabledContainerColor = Color.White.copy(alpha = 0.05f)
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Target", tint = Color(0xFF81C784), modifier = Modifier.size(16.dp))
            }
        }
    }
}
