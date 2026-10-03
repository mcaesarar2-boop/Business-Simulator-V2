package com.example.aicloud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aicloud.data.AiCloudInitialData
import com.example.aicloud.model.AiCloudIncident
import com.example.aicloud.model.AiCloudMonthlyReport
import com.example.aicloud.model.AiResearchTech
import com.example.aicloud.model.IncidentSeverity

@Composable
fun ResearchTechTreeSection(
    unlockedTechIds: Set<String>,
    companyBalance: Long,
    onUnlockTech: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "R&D Silicon & Facility Patents",
            color = AiCloudColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        AiCloudInitialData.RESEARCH_CATALOG.forEach { tech ->
            val isUnlocked = unlockedTechIds.contains(tech.id)
            val canAfford = companyBalance >= tech.costUsd

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AiCloudColors.CardBg),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isUnlocked) AiCloudColors.GreenNeon else AiCloudColors.CardBorder
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tech.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AiCloudColors.TextPrimary)
                            Text(tech.category, fontSize = 10.sp, color = AiCloudColors.CyanPrimary)
                        }

                        if (isUnlocked) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AiCloudColors.GreenNeon.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "PATENTED",
                                    color = AiCloudColors.GreenNeon,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = { onUnlockTech(tech.id) },
                                enabled = canAfford,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.CyanPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Fund $${String.format("%,d", tech.costUsd)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(tech.description, fontSize = 11.sp, color = AiCloudColors.TextSecondary)
                }
            }
        }
    }
}

@Composable
fun FinancialAnalyticsSection(lastReport: AiCloudMonthlyReport?) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Monthly Financial Statement & P&L",
            color = AiCloudColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        if (lastReport == null) {
            Surface(
                color = AiCloudColors.CardBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Awaiting first monthly accounting cycle...",
                    color = AiCloudColors.TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AiCloudColors.CardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Statement for Month ${lastReport.monthYear}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AiCloudColors.CyanPrimary
                    )

                    Divider(color = AiCloudColors.CardBorder)

                    // Revenues
                    Text("REVENUES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.GreenNeon)
                    FinancialRow("Enterprise SLA Contracts", "+$${String.format("%,d", lastReport.enterpriseContractRevenue)}", AiCloudColors.GreenNeon)
                    FinancialRow("Spot Inference Broker", "+$${String.format("%,d", lastReport.spotMarketRevenue)}", AiCloudColors.GreenNeon)
                    FinancialRow("Gross Monthly Revenue", "$${String.format("%,d", lastReport.totalRevenue)}", AiCloudColors.TextPrimary, isBold = true)

                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(color = AiCloudColors.CardBorder)

                    // Expenses
                    Text("EXPENSES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.RedAlert)
                    FinancialRow("Grid Electricity (kWh)", "-$${String.format("%,d", lastReport.powerElectricityCost)}", AiCloudColors.RedAlert)
                    FinancialRow("Hardware Maintenance", "-$${String.format("%,d", lastReport.hardwareMaintenanceCost)}", AiCloudColors.RedAlert)
                    FinancialRow("Facility & Cooling Operations", "-$${String.format("%,d", lastReport.facilityOperationalCost)}", AiCloudColors.RedAlert)
                    if (lastReport.outagePenalties > 0) {
                        FinancialRow("SLA Outage Penalties", "-$${String.format("%,d", lastReport.outagePenalties)}", AiCloudColors.RedAlert)
                    }
                    FinancialRow("Total Operational OPEX", "$${String.format("%,d", lastReport.totalExpenses)}", AiCloudColors.TextPrimary, isBold = true)

                    Spacer(modifier = Modifier.height(4.dp))
                    Divider(color = AiCloudColors.CardBorder)

                    // Net Income
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Net Monthly Profit / Cashflow", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AiCloudColors.TextPrimary)
                        Text(
                            text = "$${String.format("%,d", lastReport.netProfit)} USD",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = if (lastReport.netProfit >= 0) AiCloudColors.GreenNeon else AiCloudColors.RedAlert
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialRow(label: String, value: String, valueColor: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 11.sp, color = AiCloudColors.TextSecondary, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, fontSize = 12.sp, color = valueColor, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun IncidentsLogSection(incidents: List<AiCloudIncident>) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Telemetry & Event Logs",
            color = AiCloudColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        if (incidents.isEmpty()) {
            Text("No critical telemetry incidents logged.", color = AiCloudColors.TextSecondary, fontSize = 12.sp)
        } else {
            incidents.take(8).forEach { incident ->
                Surface(
                    color = AiCloudColors.CardBg,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (incident.severity) {
                            IncidentSeverity.CRITICAL -> AiCloudColors.RedAlert
                            IncidentSeverity.WARNING -> AiCloudColors.AmberWarning
                            IncidentSeverity.INFO -> AiCloudColors.CardBorder
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(incident.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AiCloudColors.TextPrimary)
                            Text(
                                text = incident.severity.name,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (incident.severity) {
                                    IncidentSeverity.CRITICAL -> AiCloudColors.RedAlert
                                    IncidentSeverity.WARNING -> AiCloudColors.AmberWarning
                                    IncidentSeverity.INFO -> AiCloudColors.CyanPrimary
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(incident.description, fontSize = 10.sp, color = AiCloudColors.TextSecondary)
                    }
                }
            }
        }
    }
}
