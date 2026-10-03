package com.example.businessunit.contentcreator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OwnedBusiness
import com.example.ui.formatCurrencyRingkas
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

@Composable
fun ContentCreatorUpgradeCard(
    business: OwnedBusiness,
    onLevelUp: () -> Unit,
    onHireEmployee: () -> Unit,
    onUnlockOffice: () -> Unit
) {
    val level = business.level
    val employees = business.contentCreatorEmployees
    val cash = business.contentCreatorCash
    val officeUnlocked = business.contentCreatorOfficeUnlocked
    val numFormat = NumberFormat.getNumberInstance(Locale.US)

    // Level Upgrade Calculations
    val isMaxLevel = level >= 100
    val isBlockedAtOffice = level == 40 && !officeUnlocked
    val levelUpCost = if (!isMaxLevel) (500.0 * 1.18.pow((level - 1).toDouble())).toLong() else 0L
    val canAffordLevelUp = !isMaxLevel && !isBlockedAtOffice && cash >= levelUpCost
    val nextLevelSubsBonus = if (!isMaxLevel) (100.0 * 1.16.pow((level + 1).toDouble())).toLong() else 0L

    // Employee Calculations
    val maxEmployees = when {
        level >= 81 -> 100
        level >= 61 -> 50
        level >= 41 -> 20
        level >= 21 -> 5
        else -> 0
    }
    val isMaxEmployees = employees >= maxEmployees
    val hireCost = if (!isMaxEmployees) (1500.0 * 1.20.pow(employees.toDouble())).toLong() else 0L
    val canAffordHire = !isMaxEmployees && cash >= hireCost

    // Office Unlock Calculation
    val officeUnlockCost = 5_000_000L
    val canAffordOffice = isBlockedAtOffice && cash >= officeUnlockCost

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(ContentCreatorTheme.AccentMagenta.copy(alpha = 0.4f), ContentCreatorTheme.AccentCyan.copy(alpha = 0.4f)))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ContentCreatorTheme.AccentMagenta.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Upgrade, contentDescription = null, tint = ContentCreatorTheme.AccentMagenta, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Upgrade & Skalabilitas Studio", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.TextWhite)
                        Text("Tingkatkan level channel, tim produksi & markas", fontSize = 11.sp, color = ContentCreatorTheme.TextGray)
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            // 1. Channel Level Upgrade (Level 1 - 100)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Channel Tier & Level", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.TextWhite)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ContentCreatorTheme.AccentMagenta.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, ContentCreatorTheme.AccentMagenta.copy(alpha = 0.5f))
                            ) {
                                Text("Lvl $level / 100", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.AccentMagenta, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Text(
                            text = if (isMaxLevel) "Level Maksimal Tercapai!" else "+${numFormat.format(nextLevelSubsBonus)} Subs per naik level",
                            fontSize = 11.sp,
                            color = if (isMaxLevel) ContentCreatorTheme.Gold else ContentCreatorTheme.NeonGreen
                        )
                    }

                    if (!isMaxLevel && !isBlockedAtOffice) {
                        Button(
                            onClick = onLevelUp,
                            enabled = canAffordLevelUp,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ContentCreatorTheme.AccentMagenta,
                                disabledContainerColor = Color.White.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Upgrade (${formatCurrencyRingkas(levelUpCost, false)})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (canAffordLevelUp) Color.White else ContentCreatorTheme.TextGray
                            )
                        }
                    }
                }

                // Level Progress Bar
                LinearProgressIndicator(
                    progress = { (level / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = ContentCreatorTheme.AccentMagenta,
                    trackColor = Color.White.copy(alpha = 0.08f)
                )
            }

            // Office Lock Warning at Level 40
            if (isBlockedAtOffice) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.Gold.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, ContentCreatorTheme.Gold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ContentCreatorTheme.Gold, modifier = Modifier.size(22.dp))
                            Column {
                                Text("Buka Creative Headquarters", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.Gold)
                                Text("Dibutuhkan markas resmi untuk ekspansi level 41-100", fontSize = 10.sp, color = ContentCreatorTheme.TextWhite)
                            }
                        }
                        Button(
                            onClick = onUnlockOffice,
                            enabled = canAffordOffice,
                            colors = ButtonDefaults.buttonColors(containerColor = ContentCreatorTheme.Gold, disabledContainerColor = Color.White.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Beli ($5M)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (canAffordOffice) Color.Black else ContentCreatorTheme.TextGray)
                        }
                    }
                }
            } else if (officeUnlocked) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ContentCreatorTheme.CardLight)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Apartment, contentDescription = null, tint = ContentCreatorTheme.NeonGreen, modifier = Modifier.size(16.dp))
                    Text("Creative Headquarters Resmi: AKTIF (Slot Maksimal 100 Level)", fontSize = 10.sp, color = ContentCreatorTheme.NeonGreen, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            // 2. Production Crew & Staff
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ContentCreatorTheme.AccentCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = ContentCreatorTheme.AccentCyan, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Karyawan & Crew Produksi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.TextWhite)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = ContentCreatorTheme.AccentCyan.copy(alpha = 0.2f)
                            ) {
                                Text("$employees / $maxEmployees", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.AccentCyan, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                            }
                        }
                        Text(
                            text = if (maxEmployees == 0) "Buka di Channel Level 21" else "+5% Skor Kualitas & AdSense per kru",
                            fontSize = 10.sp,
                            color = if (maxEmployees == 0) ContentCreatorTheme.Gold else ContentCreatorTheme.TextGray
                        )
                    }
                }

                if (!isMaxEmployees && maxEmployees > 0) {
                    Button(
                        onClick = onHireEmployee,
                        enabled = canAffordHire,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ContentCreatorTheme.AccentCyan,
                            disabledContainerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Rekrut (${formatCurrencyRingkas(hireCost, false)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (canAffordHire) Color.Black else ContentCreatorTheme.TextGray
                        )
                    }
                }
            }
        }
    }
}
