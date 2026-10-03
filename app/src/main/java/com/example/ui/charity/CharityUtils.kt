package com.example.ui.charity

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CharityInstitution
import com.example.ui.StaffHireRow
import com.example.viewmodel.GameViewModel

@Composable
fun CharitySdmManagementPanel(
    foundationId: String,
    institution: CharityInstitution,
    viewModel: GameViewModel
) {
    val totalActive = institution.charityStaff.relawan.active + institution.charityStaff.staffSosial.active + institution.charityStaff.ahliProgram.active
    val idealStaff = when (institution.level) {
        "Humanitarian Aid" -> 5
        "Social Care" -> 8
        "Disaster Relief" -> 15
        "Community Empowerment" -> 10
        else -> 5
    }
    val staffShortage = totalActive < idealStaff

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(
                1.dp,
                Color(0xFFF4A261).copy(alpha = 0.2f),
                RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1612)),
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
                        tint = Color(0xFFF4A261),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Manajemen Staff & Relawan",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Kebutuhan Minimum Operasional", color = Color.Gray, fontSize = 12.sp)
                Text("$totalActive / $idealStaff Staff", fontWeight = FontWeight.Bold, color = if (staffShortage) Color(0xFFE57373) else Color(0xFF81C784), fontSize = 12.sp)
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
                        Text("⚠️ Kurang Personel! Program bantuan terhambat.", color = Color(0xFFE57373), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Salary Edit Dialog State
            var showEditSalaryDialog by remember { mutableStateOf(false) }
            var dialogRoleTitle by remember { mutableStateOf("") }
            var dialogRoleType by remember { mutableStateOf("") }
            var dialogCurrentSalary by remember { mutableStateOf(0L) }
            var editSalaryValue by remember { mutableStateOf("") }

            if (showEditSalaryDialog) {
                AlertDialog(
                    onDismissRequest = { showEditSalaryDialog = false },
                    title = { Text("Atur Kompensasi - $dialogRoleTitle", fontWeight = FontWeight.Bold, color = Color.White) },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Masukkan jumlah tunjangan/gaji bulanan baru untuk seluruh personel ($):", color = Color.LightGray, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = editSalaryValue,
                                onValueChange = { editSalaryValue = it.filter { char -> char.isDigit() } },
                                label = { Text("Kompensasi Bulanan ($)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFF4A261),
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = Color(0xFFF4A261),
                                    cursorColor = Color(0xFFF4A261),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val newSal = editSalaryValue.toLongOrNull() ?: dialogCurrentSalary
                                viewModel.updateCharityStaffSalary(foundationId, institution.id, dialogRoleType, newSal)
                                showEditSalaryDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF4A261))
                        ) {
                            Text("Simpan", color = Color(0xFF1E1612), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditSalaryDialog = false }) {
                            Text("Batal", color = Color.Gray)
                        }
                    },
                    containerColor = Color(0xFF1E1612)
                )
            }

            // Staff rows
            StaffHireRow(
                title = "Relawan Kemanusiaan",
                salary = institution.charityStaff.relawan.customSalary,
                active = institution.charityStaff.relawan.active,
                recruiting = institution.charityStaff.relawan.recruiting,
                target = institution.charityStaff.relawan.target,
                onHire = { viewModel.hireCharityStaff(foundationId, institution.id, "relawan") },
                onFire = { viewModel.fireCharityStaff(foundationId, institution.id, "relawan") },
                onEditSalary = { sal ->
                    dialogRoleTitle = "Relawan Kemanusiaan"
                    dialogRoleType = "relawan"
                    dialogCurrentSalary = sal
                    editSalaryValue = sal.toString()
                    showEditSalaryDialog = true
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            StaffHireRow(
                title = "Staff Lapangan & Sosial",
                salary = institution.charityStaff.staffSosial.customSalary,
                active = institution.charityStaff.staffSosial.active,
                recruiting = institution.charityStaff.staffSosial.recruiting,
                target = institution.charityStaff.staffSosial.target,
                onHire = { viewModel.hireCharityStaff(foundationId, institution.id, "staffSosial") },
                onFire = { viewModel.fireCharityStaff(foundationId, institution.id, "staffSosial") },
                onEditSalary = { sal ->
                    dialogRoleTitle = "Staff Lapangan & Sosial"
                    dialogRoleType = "staffSosial"
                    dialogCurrentSalary = sal
                    editSalaryValue = sal.toString()
                    showEditSalaryDialog = true
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            StaffHireRow(
                title = "Ahli Program Bantuan",
                salary = institution.charityStaff.ahliProgram.customSalary,
                active = institution.charityStaff.ahliProgram.active,
                recruiting = institution.charityStaff.ahliProgram.recruiting,
                target = institution.charityStaff.ahliProgram.target,
                onHire = { viewModel.hireCharityStaff(foundationId, institution.id, "ahliProgram") },
                onFire = { viewModel.fireCharityStaff(foundationId, institution.id, "ahliProgram") },
                onEditSalary = { sal ->
                    dialogRoleTitle = "Ahli Program Bantuan"
                    dialogRoleType = "ahliProgram"
                    dialogCurrentSalary = sal
                    editSalaryValue = sal.toString()
                    showEditSalaryDialog = true
                }
            )
        }
    }
}
