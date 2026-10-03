package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.data.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteManagerUI(
    navController: NavHostController,
    viewModel: GameViewModel,
    businessId: String
) {
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsStateWithLifecycle()

    // Find business instance
    val ownedBusiness = playerState.ownedBusinesses.find { it.instanceId == businessId }
        ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == businessId }

    if (ownedBusiness == null) {
        Box(modifier = Modifier.fillMaxSize().background(DeepSeaNavy), contentAlignment = Alignment.Center) {
            Text("Memuat Hub Rute Pelabuhan...", color = WarmOffWhite)
        }
        return
    }

    val unlockedPorts = ownedBusiness.cruisePortsUnlocked ?: emptyList()
    val prestige = ownedBusiness.cruiseBrandPrestige

    // Define static unlock prices for ports
    val getUnlockCostOfPort: (String) -> Long = { pid ->
        when (pid) {
            "nassau" -> 500_000L
            "bali" -> 1_200_000L
            "singapore" -> 2_000_000L
            "southampton" -> 3_500_000L
            "raja_ampat" -> 8_000_000L
            else -> 0L
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Pelabuhan & Hub Berlayar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("back_button_routes")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepSeaNavy)
            )
        },
        containerColor = DeepSeaNavy
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // BRAND PRESTIGE NOTIFICATION HEADER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CoralBlue.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = OceanBlue)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("PRESTIGE BRAND DIVISI", color = SoftCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Ubah status kelayakan sandar kapal Anda.", color = Color.Gray, fontSize = 11.sp)
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = SandGold, modifier = Modifier.size(16.dp))
                            Text("$prestige PTS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "SEKTOR PELABUHAN GLOBAL",
                    color = SoftCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            // PORTS LISTING
            items(CRUISE_PORTS_CATALOG) { port ->
                val isMiami = port.id == "miami"
                val isUnlocked = isMiami || unlockedPorts.contains(port.id)
                val unlockCost = getUnlockCostOfPort(port.id)
                val isPrestigePassed = prestige >= port.requiredPrestige
                
                val canAfford = ownedBusiness.companyCash >= unlockCost
                val canUnlock = canAfford && isPrestigePassed

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (isUnlocked) NeonCyan.copy(alpha = 0.3f) else CoralBlue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) OceanBlue else OceanBlue.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = port.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp
                                    )
                                    if (port.isYachtOnly) {
                                        Box(
                                            modifier = Modifier
                                                .background(SandGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Yacht Only", color = SandGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text("Negara: ${port.country}", color = Color.Gray, fontSize = 11.sp)
                            }

                            // Dynamic Status Icon (Unlocked vs Locked)
                            Icon(
                                imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isUnlocked) Color.Green else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(port.description, color = Color.LightGray, fontSize = 12.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text("Monthly Port Fee: ${formatCurrency(port.portFee)}", color = SoftCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Kapasitas Demand: ${port.baseDemand} Pax / Bulan", color = Color.Gray, fontSize = 11.sp)
                                if (port.requiredPrestige > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Prestise Syarat:", color = Color.Gray, fontSize = 11.sp)
                                        Icon(Icons.Default.Security, contentDescription = null, tint = if (isPrestigePassed) Color.Green else Color(0xFFFF5252), modifier = Modifier.size(10.dp))
                                        Text("${port.requiredPrestige} PTS", color = if (isPrestigePassed) Color.Green else Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (!isUnlocked) {
                                Button(
                                    onClick = {
                                        viewModel.unlockCruisePort(businessId, port.id, unlockCost)
                                    },
                                    enabled = canUnlock,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonCyan,
                                        contentColor = DeepSeaNavy,
                                        disabledContainerColor = Color.DarkGray
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp).testTag("unlock_${port.id}")
                                ) {
                                    Text("Beli Lisensi ${formatCurrencyRingkas(unlockCost, useShortFormat)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Green.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Terbuka", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
