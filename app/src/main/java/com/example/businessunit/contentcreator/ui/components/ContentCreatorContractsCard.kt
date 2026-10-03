package com.example.businessunit.contentcreator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessunit.contentcreator.model.ActiveCreatorContract
import com.example.ui.formatCurrencyRingkas

@Composable
fun ContentCreatorContractsCard(
    contracts: List<ActiveCreatorContract>,
    onTerminateContract: (ActiveCreatorContract) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = ContentCreatorTheme.Gold, modifier = Modifier.size(18.dp))
                Text(
                    text = "Manajemen Kontrak Aktif",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ContentCreatorTheme.TextWhite
                )
            }
            Text(
                text = "${contracts.size}/3 Slot",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (contracts.size >= 3) ContentCreatorTheme.Gold else ContentCreatorTheme.TextGray
            )
        }

        if (contracts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada kontrak sponsorship aktif.\nTerima penawaran brand untuk mendapatkan penghasilan bulanan rutin.",
                        color = ContentCreatorTheme.TextGray,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            contracts.forEach { contract ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
                    border = BorderStroke(1.dp, ContentCreatorTheme.AccentCyan.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ContentCreatorTheme.AccentCyan, modifier = Modifier.size(20.dp))
                                Column {
                                    Text(contract.brandName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ContentCreatorTheme.TextWhite)
                                    Text(contract.categoryTag, fontSize = 10.sp, color = ContentCreatorTheme.TextGray)
                                }
                            }

                            Surface(
                                color = ContentCreatorTheme.Gold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "+${formatCurrencyRingkas(contract.monthlyPayout, false)} / bln",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ContentCreatorTheme.Gold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sisa Waktu: ${contract.remainingMonths} dari ${contract.totalMonths} Bulan", fontSize = 11.sp, color = ContentCreatorTheme.TextGray)
                            Text("Diterima: ${formatCurrencyRingkas(contract.totalPaidSoFar, false)}", fontSize = 11.sp, color = ContentCreatorTheme.NeonGreen)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { contract.progressFraction },
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                            color = ContentCreatorTheme.AccentCyan,
                            trackColor = Color.White.copy(alpha = 0.08f),
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Kontrak: ${formatCurrencyRingkas(contract.totalContractValue, false)}", fontSize = 11.sp, color = ContentCreatorTheme.TextGray)
                            Row(
                                modifier = Modifier.clickable { onTerminateContract(contract) },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Cancel, contentDescription = null, tint = ContentCreatorTheme.ErrorRed, modifier = Modifier.size(14.dp))
                                Text("Putus Kontrak", color = ContentCreatorTheme.ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
