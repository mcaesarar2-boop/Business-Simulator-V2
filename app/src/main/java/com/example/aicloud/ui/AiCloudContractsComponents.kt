package com.example.aicloud.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aicloud.model.ClientContract

@Composable
fun ContractsOverviewSection(
    activeContracts: List<ClientContract>,
    marketContracts: List<ClientContract>,
    availableFreeTflops: Double,
    onSignContract: (String) -> Unit,
    onCancelContract: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Spot Market Status Card
        SpotMarketSummaryCard(availableFreeTflops = availableFreeTflops)

        // Active Enterprise SLA Contracts
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Enterprise Contracts (${activeContracts.size})",
                    color = AiCloudColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Total: $${String.format("%,d", activeContracts.sumOf { it.monthlyPaymentUsd })}/mo",
                    color = AiCloudColors.GreenNeon,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            if (activeContracts.isEmpty()) {
                Surface(
                    color = AiCloudColors.CardBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Handshake, contentDescription = null, tint = AiCloudColors.TextSecondary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No Active Client Contracts", color = AiCloudColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Review market tenders below to sign enterprise GPU leases.", color = AiCloudColors.TextSecondary, fontSize = 11.sp)
                    }
                }
            } else {
                activeContracts.forEach { contract ->
                    ActiveContractCard(contract = contract, onCancel = { onCancelContract(contract.id) })
                }
            }
        }

        // Available Market Procurement Tenders
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Market RFPs & Tenders",
                color = AiCloudColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            if (marketContracts.isEmpty()) {
                Text("Searching global brokers for new AI workload tenders...", color = AiCloudColors.TextSecondary, fontSize = 12.sp)
            } else {
                marketContracts.forEach { contract ->
                    MarketContractCard(
                        contract = contract,
                        availableFreeTflops = availableFreeTflops,
                        onSign = { onSignContract(contract.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SpotMarketSummaryCard(availableFreeTflops: Double) {
    val estimatedSpotRevenue = (availableFreeTflops * 22.0).toLong()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AiCloudColors.CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.CyanPrimary.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = AiCloudColors.CyanPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Spot AI Inference Pool", color = AiCloudColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Surplus uncontracted TFLOPS are automatically monetized via the open spot market.",
                    color = AiCloudColors.TextSecondary,
                    fontSize = 11.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%,.0f", availableFreeTflops)} TFLOPS",
                    color = AiCloudColors.CyanPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "~$${String.format("%,d", estimatedSpotRevenue)}/mo",
                    color = AiCloudColors.GreenNeon,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ActiveContractCard(contract: ClientContract, onCancel: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AiCloudColors.CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (contract.isSatisfied) AiCloudColors.CardBorder else AiCloudColors.RedAlert)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(contract.clientName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AiCloudColors.TextPrimary)
                    Text(contract.workloadType.displayName, fontSize = 11.sp, color = AiCloudColors.TextSecondary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text("$${String.format("%,d", contract.monthlyPaymentUsd)}/mo", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AiCloudColors.GreenNeon)
                    Text("${contract.monthsRemaining} bln tersisa", fontSize = 10.sp, color = AiCloudColors.TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Compute: ${String.format("%,.0f", contract.requiredTflops)} TFLOPS • SLA ${String.format("%.1f", contract.slaRequirementUptime)}%",
                    fontSize = 11.sp,
                    color = AiCloudColors.CyanPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(8.dp))

                TextButton(
                    onClick = onCancel,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Terminate", color = AiCloudColors.RedAlert, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun MarketContractCard(
    contract: ClientContract,
    availableFreeTflops: Double,
    onSign: () -> Unit
) {
    val canFulfill = availableFreeTflops >= contract.requiredTflops * 0.7

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AiCloudColors.CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, AiCloudColors.CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(contract.clientName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AiCloudColors.TextPrimary)
                    Text(contract.workloadType.displayName, fontSize = 11.sp, color = AiCloudColors.CyanPrimary)
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AiCloudColors.SurfaceHighlight
                ) {
                    Text(
                        text = "${contract.contractDurationMonthsTotal} Months",
                        color = AiCloudColors.TextSecondary,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Required: ${String.format("%,.0f", contract.requiredTflops)} TFLOPS", fontSize = 11.sp, color = AiCloudColors.TextPrimary)
                    Text("Rate: $${String.format("%,d", contract.monthlyPaymentUsd)}/mo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AiCloudColors.GreenNeon)
                }

                Button(
                    onClick = onSign,
                    enabled = canFulfill,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AiCloudColors.CyanPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (canFulfill) "Sign Contract" else "Need Capacity",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
