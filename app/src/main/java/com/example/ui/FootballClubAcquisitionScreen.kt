package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FootballClubAcquisitionScreen(
    navController: NavController,
    viewModel: GameViewModel,
    holdingId: String? = null
) {
    val playerState by viewModel.playerState.collectAsState()
    val allLeagues = remember { FootballDatabase.leagues }

    val countries = remember {
        listOf(
            "England", "Spain", "Italy", "Germany", "France", 
            "Saudi Arabia", "United States", "Indonesia", "Netherlands", "Portugal"
        )
    }

    var selectedCountry by remember { mutableStateOf("England") }
    val availableLeaguesForCountry = remember(selectedCountry) {
        allLeagues.filter { it.country.equals(selectedCountry, ignoreCase = true) }
    }

    var selectedLeague by remember(selectedCountry) {
        mutableStateOf(availableLeaguesForCountry.firstOrNull() ?: allLeagues.first())
    }

    // Keep selectedLeague synced when country changes
    LaunchedEffect(selectedCountry) {
        val firstInCountry = availableLeaguesForCountry.firstOrNull()
        if (firstInCountry != null) {
            selectedLeague = firstInCountry
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedClub by remember(selectedLeague) {
        mutableStateOf(selectedLeague.clubs.firstOrNull() ?: "")
    }

    var chairmanName by remember {
        mutableStateOf("Chairman")
    }

    // Selected Holding if funding from holding
    var selectedHoldingId by remember { mutableStateOf(holdingId) }
    val targetHolding = playerState.holdingCompanies.find { it.instanceId == selectedHoldingId }

    val availableCash = if (targetHolding != null) {
        targetHolding.holdingCash.toLong()
    } else {
        playerState.cash
    }

    val selectedClubPriceUsd = remember(selectedClub, selectedLeague) {
        if (selectedClub.isNotBlank()) {
            FootballDatabase.getClubAcquisitionPriceUsd(selectedClub, selectedLeague)
        } else 0L
    }

    val canAfford = availableCash >= selectedClubPriceUsd
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val filteredClubs = remember(selectedLeague, searchQuery) {
        if (searchQuery.isBlank()) {
            selectedLeague.clubs
        } else {
            selectedLeague.clubs.filter { it.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("football_acquisition_screen"),
        containerColor = Color(0xFF0B1118),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Akuisisi Klub Sepak Bola",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (targetHolding != null) "Akuisisi Anak Perusahaan: ${targetHolding.name}" else "Chairman Mode (Kepemilikan Utama)",
                            fontSize = 12.sp,
                            color = Color(0xFF4ADE80)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("football_acquisition_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        bottomBar = {
            // Sticky Bottom Acquisition Summary & Action Bar
            Surface(
                modifier = Modifier.fillMaxWidth().testTag("acquisition_bottom_bar"),
                color = Color(0xFF0F172A),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Klub Dipilih: $selectedClub",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "${selectedLeague.name} (${selectedLeague.country})",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "USD ${String.format("%,d", selectedClubPriceUsd)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = Color(0xFFFFD700)
                            )
                            Text(
                                text = "Saldo: USD ${String.format("%,d", availableCash)}",
                                fontSize = 11.sp,
                                color = if (canAfford) Color(0xFF4ADE80) else Color(0xFFEF4444)
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (selectedClub.isBlank()) {
                                errorMessage = "Pilih klub terlebih dahulu."
                                return@Button
                            }
                            if (!canAfford) {
                                errorMessage = "Dana tidak mencukupi untuk mengakuisisi $selectedClub."
                                return@Button
                            }
                            isProcessing = true
                            val (success, msg) = viewModel.initializeFootballClub(
                                clubName = selectedClub,
                                leagueName = selectedLeague.name,
                                chairmanName = chairmanName.ifBlank { "Chairman" },
                                acquisitionPriceUsd = selectedClubPriceUsd,
                                targetHoldingId = selectedHoldingId
                            )
                            isProcessing = false
                            if (success) {
                                navController.navigate("football_club_dashboard") {
                                    popUpTo("football_club_acquisition") { inclusive = true }
                                }
                            } else {
                                errorMessage = msg
                            }
                        },
                        enabled = canAfford && !isProcessing && selectedClub.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("confirm_acquisition_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF22C55E),
                            disabledContainerColor = Color.DarkGray
                        )
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (canAfford) "Sahkan Akuisisi $selectedClub" else "Dana Tidak Cukup (USD)",
                                fontWeight = FontWeight.Bold,
                                color = if (canAfford) Color.Black else Color.LightGray,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Hero Banner
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF064E3B), Color(0xFF0F172A))
                            )
                        )
                ) {
                    AsyncImage(
                        model = "https://images.unsplash.com/photo-1551958219-acbc608c6377?q=80&w=1170&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D",
                        contentDescription = "Stadium",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.35f
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "KONSORSIUM KEPEMILIKAN SEPAK BOLA",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF4ADE80)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Kuasai Klub Impian Anda",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Sebagai Chairman & Investor, kendalikan arah klub, rekrut pelatih top, dan investasikan modal!",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }

            // 2. Chairman Name Input
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131F2E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Identitas Pemilik (Chairman / Owner)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = chairmanName,
                            onValueChange = { chairmanName = it },
                            placeholder = { Text("Contoh: Todd Boehly / Sheikh Mansour") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chairman_name_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF22C55E),
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF22C55E))
                            }
                        )
                    }
                }
            }

            // 3. Sumber Pendanaan (Kas Utama vs Kas Holding)
            if (playerState.holdingCompanies.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131F2E))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Sumber Pendanaan Akuisisi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedHoldingId = null }
                                    .background(
                                        if (selectedHoldingId == null) Color(0xFF22C55E).copy(alpha = 0.2f) else Color.Transparent,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (selectedHoldingId == null) Color(0xFF22C55E) else Color.DarkGray,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedHoldingId == null,
                                    onClick = { selectedHoldingId = null }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Kas Utama / Mega Holding Pribadi", fontWeight = FontWeight.SemiBold, color = Color.White)
                                    Text("Saldo Tersedia: USD ${String.format("%,d", playerState.cash)}", fontSize = 12.sp, color = Color(0xFF4ADE80))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            playerState.holdingCompanies.forEach { holding ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedHoldingId = holding.instanceId }
                                        .background(
                                            if (selectedHoldingId == holding.instanceId) Color(0xFF22C55E).copy(alpha = 0.2f) else Color.Transparent,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (selectedHoldingId == holding.instanceId) Color(0xFF22C55E) else Color.DarkGray,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedHoldingId == holding.instanceId,
                                        onClick = { selectedHoldingId = holding.instanceId }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Holding: ${holding.name}", fontWeight = FontWeight.SemiBold, color = Color.White)
                                        Text("Kas Holding: USD ${String.format("%,d", holding.holdingCash.toLong())}", fontSize = 12.sp, color = Color(0xFFFFD700))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }

            // 4. Pilih Negara (Visual Country Chips)
            item {
                Column {
                    Text(
                        text = "1. Pilih Negara Asal Liga",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("country_selector_row")
                    ) {
                        items(countries) { country ->
                            val isSelected = country.equals(selectedCountry, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { selectedCountry = country }
                                    .testTag("country_chip_$country"),
                                color = if (isSelected) Color(0xFF22C55E) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFF4ADE80) else Color.Transparent)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = when (country) {
                                            "England" -> "🏴󠁧󠁢󠁥󠁮󠁧󠁿 England"
                                            "Spain" -> "🇪🇸 Spain"
                                            "Italy" -> "🇮🇹 Italy"
                                            "Germany" -> "🇩🇪 Germany"
                                            "France" -> "🇫🇷 France"
                                            "Saudi Arabia" -> "🇸🇦 Saudi Arabia"
                                            "United States" -> "🇺🇸 United States"
                                            "Indonesia" -> "🇮🇩 Indonesia"
                                            "Netherlands" -> "🇳🇱 Netherlands"
                                            "Portugal" -> "🇵🇹 Portugal"
                                            else -> country
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Pilih Kasta Liga
            item {
                Column {
                    Text(
                        text = "2. Pilih Kasta Kompetisi (${availableLeaguesForCountry.size} Kasta Tersedia)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableLeaguesForCountry.forEach { league ->
                            val isSelected = league.name == selectedLeague.name
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedLeague = league }
                                    .testTag("league_card_${league.level}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF064E3B) else Color(0xFF16202D)
                                ),
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isSelected) Color(0xFF22C55E) else Color.White.copy(alpha = 0.05f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFF22C55E) else Color(0xFF334155)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "T${league.level}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color.Black else Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = league.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${league.clubs.size} Klub",
                                        fontSize = 11.sp,
                                        color = Color.LightGray
                                    )
                                    if (league.promotionSlots > 0) {
                                        Text(
                                            text = "Promosi: ${league.promotionSlots} tim",
                                            fontSize = 10.sp,
                                            color = Color(0xFF4ADE80)
                                        )
                                    }
                                    Text(
                                        text = "Degradasi: ${league.relegationSlots} tim",
                                        fontSize = 10.sp,
                                        color = Color(0xFFF87171)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Cari dan Pilih Klub
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. Pilih Klub untuk Diakuisisi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${filteredClubs.size} Klub",
                            fontSize = 12.sp,
                            color = Color(0xFF4ADE80)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari nama klub di ${selectedLeague.name}...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("club_search_field"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF22C55E),
                            unfocusedBorderColor = Color.DarkGray
                        )
                    )
                }
            }

            // 7. Daftar Klub beserta Valuasi Harga Akuisisi (USD)
            items(filteredClubs) { clubName ->
                val isSelected = clubName.equals(selectedClub, ignoreCase = true)
                val priceUsd = FootballDatabase.getClubAcquisitionPriceUsd(clubName, selectedLeague)
                val canAffordThis = availableCash >= priceUsd

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedClub = clubName }
                        .testTag("club_item_$clubName"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF152A1E) else Color(0xFF131D28)
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (isSelected) Color(0xFF22C55E) else Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFF22C55E).copy(alpha = 0.2f) else Color(0xFF1E293B)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.SportsSoccer,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF22C55E) else Color.LightGray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = clubName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${selectedLeague.name} • Tier ${selectedLeague.level} • Skuad: 16-24 Pemain",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "USD ${String.format("%,d", priceUsd)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (isSelected) Color(0xFFFFD700) else Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (canAffordThis) Color(0xFF065F46) else Color(0xFF7F1D1D)
                            ) {
                                Text(
                                    text = if (canAffordThis) "Dana Cukup" else "Kurang",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAffordThis) Color(0xFF34D399) else Color(0xFFFCA5A5),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
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
