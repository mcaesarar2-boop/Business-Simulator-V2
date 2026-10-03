package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.data.*
import com.example.viewmodel.GameViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildHotelPropertyScreen(navController: NavHostController, viewModel: GameViewModel, instanceId: String) {
    val playerState by viewModel.playerState.collectAsState()
    val ownedBusiness = playerState.ownedBusinesses.find { it.instanceId == instanceId } ?: playerState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
    
    if (ownedBusiness == null) {
        navController.popBackStack()
        return
    }

    val nFormat = NumberFormat.getNumberInstance(Locale.US)
    
    var propName by remember { mutableStateOf("") }
    var propLoc by remember { mutableStateOf("") }
    var selectedTier by remember { mutableStateOf<HotelTier?>(null) }

    val canBuild = propName.isNotBlank() && propLoc.isNotBlank() && selectedTier != null &&
                   ownedBusiness.companyCash >= (selectedTier?.baseBuildCost ?: Long.MAX_VALUE)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Katalog Properti Baru", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color(0xFFFFD700),
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xFF1E1E1E),
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total Kas Divisi:", color = Color.Gray, fontSize = 12.sp)
                        Text("$${nFormat.format(ownedBusiness.companyCash)}", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total Biaya:", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("$${nFormat.format(selectedTier?.baseBuildCost ?: 0L)}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (canBuild) {
                                viewModel.buildHotelProperty(instanceId, propName, propLoc, selectedTier!!)
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = canBuild,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            disabledContainerColor = Color(0xFF4A4A4A),
                            contentColor = Color.Black,
                            disabledContentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("BANGUN PROPERTI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Section 1: Input Form
            var locationDropdownExpanded by remember { mutableStateOf(false) }
            val themeParks = remember(playerState) {
                val list = mutableListOf<String>()
                val allBus = playerState.ownedBusinesses + playerState.holdingCompanies.flatMap { it.subsidiaries }
                allBus.forEach { bus ->
                    bus.themeParkBranches.forEach { branch ->
                        list.add("Integrasi: ${branch.customName ?: branch.locationName}")
                    }
                }
                list
            }

            OutlinedTextField(
                value = propName,
                onValueChange = { propName = it },
                label = { Text("Nama Properti / Hotel", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFD700),
                    unfocusedBorderColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box {
                OutlinedTextField(
                    value = propLoc,
                    onValueChange = { 
                        propLoc = it
                        if (themeParks.isNotEmpty() && propLoc.isNotEmpty()) locationDropdownExpanded = true
                    },
                    label = { Text("Lokasi (Cth: Bali, Tokyo, Maldives)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth().clickable { 
                        if (themeParks.isNotEmpty()) locationDropdownExpanded = true 
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (themeParks.isNotEmpty()) {
                            IconButton(onClick = { locationDropdownExpanded = !locationDropdownExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Pilih Lokasi Terintegrasi", tint = Color.Gray)
                            }
                        }
                    }
                )
                DropdownMenu(
                    expanded = locationDropdownExpanded,
                    onDismissRequest = { locationDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f).background(Color(0xFF2A2A2A))
                ) {
                    themeParks.forEach { tp ->
                        DropdownMenuItem(
                            text = { Text(tp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) },
                            onClick = {
                                propLoc = tp
                                locationDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Section 2: Katalog Tier Hotel
            Text("Pilih Kelas Properti", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(HotelTier.values()) { tier ->
                    val isSelected = selectedTier == tier
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTier = tier },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF2A2A2A) else Color(0xFF1E1E1E)
                        ),
                        border = if (isSelected) BorderStroke(2.dp, Color(0xFFFFD700)) else BorderStroke(1.dp, Color.Transparent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(tier.title, color = if (isSelected) Color(0xFFFFD700) else Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$${nFormat.format(tier.baseBuildCost)}", color = Color(0xFF00E676), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kapasitas: ${tier.maxRooms} Kamar", color = Color.LightGray, fontSize = 10.sp)
                            Text("Waktu: ${tier.buildMonths} bln", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
