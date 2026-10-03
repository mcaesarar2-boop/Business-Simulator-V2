package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.data.GLOBAL_AVIATION_HUBS
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubCatalogScreen(navController: NavHostController, viewModel: GameViewModel, businessId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val useShortFormat by viewModel.useShortNumberFormat.collectAsState()
    val context = LocalContext.current

    val currentBusiness = remember(playerState.ownedBusinesses, playerState.holdingCompanies, businessId) {
        playerState.ownedBusinesses.find { it.instanceId == businessId }
            ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == businessId }
    }

    if (currentBusiness == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF090A0E)),
            contentAlignment = Alignment.Center
        ) {
            Text("Bisnis tidak ditemukan", color = Color.White)
        }
        return
    }

    Scaffold(
        containerColor = Color(0xFF090A0E),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Buka Aviation Hub",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kas Maskapai: ${formatCurrencyRingkas(currentBusiness.companyCash.toLong(), useShortFormat)}",
                            color = Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF13161C)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = Color(0xFF1E2630).copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Tips",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Setiap hub dinamis memerlukan waktu pembangunan sebelum siap beroperasi penuh untuk menampung armada pesawat dan rute penerbangan.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Grouping lists logically
                val categories = listOf(
                    "REGIONAL & DOMESTIK" to GLOBAL_AVIATION_HUBS.filter { it.baseCost < 65000000L },
                    "ASIA PASIFIK" to GLOBAL_AVIATION_HUBS.filter { it.baseCost in 65000000L..100000000L },
                    "GLOBAL / PREMIUM HUB" to GLOBAL_AVIATION_HUBS.filter { it.baseCost > 100000000L }
                )

                categories.forEach { (catTitle, hubsInCategory) ->
                    if (hubsInCategory.isNotEmpty()) {
                        item {
                            Text(
                                text = catTitle,
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(hubsInCategory) { hub ->
                            val alreadyOwned = currentBusiness.airlineHubsComplex.find { it.city == hub.city }
                            val canAfford = currentBusiness.companyCash >= hub.baseCost

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (alreadyOwned != null) Color(0xFF131922) else Color(0xFF1E2630)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = when {
                                        alreadyOwned != null -> Color.Green.copy(alpha = 0.3f)
                                        canAfford -> Color(0xFFFFD700).copy(alpha = 0.15f)
                                        else -> Color.White.copy(alpha = 0.05f)
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = hub.city,
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = hub.country,
                                            color = Color.LightGray,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(start = 20.dp, top = 2.dp)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.padding(start = 20.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Column {
                                                Text("Waktu Konstruksi", color = Color.Gray, fontSize = 10.sp)
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Build,
                                                        contentDescription = null,
                                                        tint = Color.LightGray,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${hub.buildTime} bln",
                                                        color = Color.LightGray,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }

                                            Column {
                                                Text("Maksimal Slot", color = Color.Gray, fontSize = 10.sp)
                                                Text(
                                                    text = "${hub.maxSlots} Pesawat",
                                                    color = Color.LightGray,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.padding(start = 20.dp)
                                        ) {
                                            Text(
                                                text = "Investasi: ${formatCurrencyRingkas(hub.baseCost, false)}",
                                                color = Color(0xFFFFD700),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(horizontalAlignment = Alignment.End) {
                                        if (alreadyOwned != null) {
                                            if (alreadyOwned.isConstructing) {
                                                Button(
                                                    onClick = {},
                                                    enabled = false,
                                                    colors = ButtonDefaults.buttonColors(
                                                        disabledContainerColor = Color(0xFF2C2415)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    modifier = Modifier.widthIn(min = 100.dp)
                                                ) {
                                                    Text(
                                                        text = "Konstruksi: ${alreadyOwned.constructionMonthsLeft} bln",
                                                        color = Color(0xFFFF9800),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            } else {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = Color.Green,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Aktif",
                                                        color = Color.Green,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    if (currentBusiness.companyCash < hub.baseCost) {
                                                        Toast.makeText(context, "Kas Maskapai Tidak Cukup! Silakan suntik dana dari menu utama.", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        viewModel.buyAviationHubComplex(businessId, hub.city, hub.baseCost, hub.buildTime)
                                                        Toast.makeText(context, "Mulai pembangunan hub ${hub.city} selama ${hub.buildTime} bulan!", Toast.LENGTH_SHORT).show()
                                                        navController.popBackStack()
                                                    }
                                                },
                                                enabled = true,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (canAfford) Color(0xFF4CAF50) else Color(0xFFD32F2F).copy(alpha = 0.5f),
                                                    disabledContainerColor = Color.DarkGray
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "Buka Hub",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
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
    }
}
