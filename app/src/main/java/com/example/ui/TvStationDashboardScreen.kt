package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.data.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TvStationDashboardScreen(
    ownedBusiness: OwnedBusiness,
    playerState: PlayerState,
    useShortFormat: Boolean,
    viewModel: GameViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("📺 Program Siaran", "🏢 Manajemen Fasilitas", "👥 Struktur Organisasi", "📡 Lisensi & Transmisi")

    val tvData = ownedBusiness.tvStationData
    val activePrograms = playerState.activeTvPrograms.filter { it.active }
    val bookedSlots = activePrograms.flatMap { it.timeSlots }

    var showAddProgramModal by remember { mutableStateOf(false) }
    var showBiddingModal by remember { mutableStateOf(false) }
    var showBuildFacilityDialog by remember { mutableStateOf(false) }
    var showQuickStaffModal by remember { mutableStateOf(false) }
    var showQuickLicenseModal by remember { mutableStateOf(false) }
    var showEditScheduleDialog by remember { mutableStateOf<TvProgram?>(null) }
    var showRenameFacilityDialog by remember { mutableStateOf<TvStudioFacility?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(bottom = 16.dp)) {
        // Broadcast Network Header Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFE53935), Color(0xFFFF7043))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = ownedBusiness.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // FlowRow to wrap badges gracefully with generous padding and whitespace-nowrap
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "LVL ${ownedBusiness.level}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Surface(
                                    color = Color(0xFFFFB300).copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "⭐ Reputasi: ${String.format("%.1f", tvData.reputation)}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        color = Color(0xFFFFB300)
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("Kas Internal TV", style = MaterialTheme.typography.labelSmall, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                formatMoney(ownedBusiness.companyCash.toLong(), useShortFormat),
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Network Operational Summary Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val totalAdRev = activePrograms.sumOf { it.monthlyAdRevenue }.toLong()
                    val totalOps = tvData.totalMonthlyExpenses + activePrograms.sumOf { it.currentOperationalCost }.toLong()
                    val netIncome = totalAdRev - totalOps

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Kapasitas Siaran", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${activePrograms.size}/${tvData.maxSimultaneousPrograms} Program",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text("Omzet Iklan/Bln", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            formatMoney(totalAdRev, useShortFormat),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.End) {
                        Text("Beban Biaya/Bln", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            formatMoney(totalOps, useShortFormat),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Horizontal Scrollable Navigation Tabs Container (No squishing, full horizontal scroll)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("tv_navigation_tab_container"),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(tabTitles) { index, title ->
                val isSelected = selectedTab == index
                Surface(
                    onClick = { selectedTab = index },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .wrapContentWidth()
                        .testTag("tv_tab_button_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // -------------------------------------------------------------
        // DYNAMIC ACTION AREA (CALL-TO-ACTION) PER ACTIVE TAB
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("tv_dynamic_cta_area")
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: PROGRAM SIARAN -> Tombol Besar Kuning
                    Button(
                        onClick = { showAddProgramModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("tv_cta_add_program"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF59E0B), // Vibrant Amber / Yellow-500
                            contentColor = Color(0xFF111827)   // High contrast dark/black text
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 6.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF111827))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "📺 Produksi Program TV Baru",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
                1 -> {
                    // TAB 1: MANAJEMEN FASILITAS -> Tombol Biru/Teal
                    Button(
                        onClick = { showBuildFacilityDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("tv_cta_build_facility"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7), // Sky-600 / Blue-Teal
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "🏗️ Bangun Studio Fisik Baru",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
                2 -> {
                    // TAB 2: STRUKTUR ORGANISASI -> Tombol Hijau
                    Button(
                        onClick = { showQuickStaffModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("tv_cta_recruit_staff"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16A34A), // Green-600
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 6.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "👥 Rekrut Direksi & Staf",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
                3 -> {
                    // TAB 3: LISENSI & TRANSMISI -> Tombol Ungu
                    Button(
                        onClick = { showQuickLicenseModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("tv_cta_buy_license"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6), // Purple-500 / Violet
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 6.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "📡 Beli Lisensi / Transmisi",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Content per Tab
        Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp)) {
            when (selectedTab) {
                0 -> TvBroadcastProgramsTab(
                    ownedBusiness = ownedBusiness,
                    activePrograms = activePrograms,
                    useShortFormat = useShortFormat,
                    onOpenAddModal = { showAddProgramModal = true },
                    onOpenBiddingModal = { showBiddingModal = true },
                    onOpenIpLibrary = { navController.navigate("tv_ip_library") },
                    onCancelProgram = { progId -> viewModel.cancelTvProgram(progId) },
                    onEditSchedule = { prog -> showEditScheduleDialog = prog }
                )
                1 -> TvFacilityManagementTab(
                    ownedBusiness = ownedBusiness,
                    tvData = tvData,
                    activePrograms = activePrograms,
                    useShortFormat = useShortFormat,
                    onOpenBuildDialog = { showBuildFacilityDialog = true },
                    onRenameFacility = { facility -> showRenameFacilityDialog = facility },
                    onDemolishFacility = { facilityId ->
                        val res = viewModel.demolishTvFacility(ownedBusiness.instanceId, facilityId)
                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                    }
                )
                2 -> TvOrganizationHrTab(
                    ownedBusiness = ownedBusiness,
                    tvData = tvData,
                    activePrograms = activePrograms,
                    useShortFormat = useShortFormat,
                    onHireDirector = { role ->
                        val res = viewModel.hireTvDirector(ownedBusiness.instanceId, role)
                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                    },
                    onFireDirector = { role ->
                        val res = viewModel.fireTvDirector(ownedBusiness.instanceId, role)
                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                    },
                    onHireCrews = { amount ->
                        val res = viewModel.hireTvCrews(ownedBusiness.instanceId, amount)
                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                    },
                    onLayoffCrews = { amount ->
                        val res = viewModel.layoffTvCrews(ownedBusiness.instanceId, amount)
                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                    }
                )
                3 -> TvLicensesAndTransmissionTab(
                    ownedBusiness = ownedBusiness,
                    tvData = tvData,
                    useShortFormat = useShortFormat,
                    onAcquireDewanPers = {
                        val res = viewModel.acquireDewanPersCertification(ownedBusiness.instanceId)
                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                    },
                    onAcquireNationalLicense = {
                        val res = viewModel.acquireNationalBroadcastLicense(ownedBusiness.instanceId)
                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                    },
                    onBuildTransmission = { regionKey ->
                        val res = viewModel.buildRegionalTransmissionTower(ownedBusiness.instanceId, regionKey)
                        Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // Modal Add Program TV
    if (showAddProgramModal) {
        AddTvProgramModal(
            ownedBusiness = ownedBusiness,
            activePrograms = activePrograms,
            bookedSlots = bookedSlots,
            useShortFormat = useShortFormat,
            onDismiss = { showAddProgramModal = false },
            onSubmit = { title, type, cost, timeSlots, studioId ->
                val result = viewModel.addTvProgramWithDetails(
                    instanceId = ownedBusiness.instanceId,
                    title = title,
                    type = type,
                    productionCost = cost,
                    isPremiumRights = false,
                    finalCost = cost.toLong(),
                    durationMonths = -1,
                    timeSlots = timeSlots,
                    assignedStudioId = studioId
                )
                Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                if (result.first) {
                    showAddProgramModal = false
                }
            }
        )
    }

    // Modal Bidding Hak Siar Olahraga
    if (showBiddingModal) {
        TvSportsBiddingDialog(
            ownedBusiness = ownedBusiness,
            useShortFormat = useShortFormat,
            onDismiss = { showBiddingModal = false },
            onSubmit = { title, fee, duration, timeSlots ->
                val result = viewModel.addTvProgramWithDetails(
                    instanceId = ownedBusiness.instanceId,
                    title = title,
                    type = "Hak Siar Olahraga",
                    productionCost = fee.toDouble(),
                    isPremiumRights = true,
                    finalCost = fee,
                    durationMonths = duration,
                    timeSlots = timeSlots,
                    assignedStudioId = null
                )
                Toast.makeText(context, result.second, Toast.LENGTH_LONG).show()
                if (result.first) {
                    showBiddingModal = false
                }
            }
        )
    }

    // Modal Bangun Fasilitas Studio Baru
    if (showBuildFacilityDialog) {
        BuildTvFacilityDialog(
            ownedBusiness = ownedBusiness,
            useShortFormat = useShortFormat,
            onDismiss = { showBuildFacilityDialog = false },
            onBuild = { type, customName ->
                val res = viewModel.buildTvFacility(ownedBusiness.instanceId, type, customName)
                Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                if (res.first) showBuildFacilityDialog = false
            }
        )
    }

    // Modal Rekrut Direksi & Staf (Dynamic CTA)
    if (showQuickStaffModal) {
        TvQuickStaffModal(
            ownedBusiness = ownedBusiness,
            tvData = tvData,
            useShortFormat = useShortFormat,
            onDismiss = { showQuickStaffModal = false },
            onHireDirector = { role ->
                val res = viewModel.hireTvDirector(ownedBusiness.instanceId, role)
                Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
            },
            onFireDirector = { role ->
                val res = viewModel.fireTvDirector(ownedBusiness.instanceId, role)
                Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
            },
            onHireCrews = { amount ->
                val res = viewModel.hireTvCrews(ownedBusiness.instanceId, amount)
                Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
            },
            onLayoffCrews = { amount ->
                val res = viewModel.layoffTvCrews(ownedBusiness.instanceId, amount)
                Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Beli Lisensi & Transmisi (Dynamic CTA)
    if (showQuickLicenseModal) {
        TvQuickLicenseModal(
            ownedBusiness = ownedBusiness,
            tvData = tvData,
            useShortFormat = useShortFormat,
            onDismiss = { showQuickLicenseModal = false },
            onAcquireDewanPers = {
                val res = viewModel.acquireDewanPersCertification(ownedBusiness.instanceId)
                Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
            },
            onAcquireNationalLicense = {
                val res = viewModel.acquireNationalBroadcastLicense(ownedBusiness.instanceId)
                Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
            },
            onBuildTransmission = { regionKey ->
                val res = viewModel.buildRegionalTransmissionTower(ownedBusiness.instanceId, regionKey)
                Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Ubah Jadwal Slot
    showEditScheduleDialog?.let { prog ->
        EditTvScheduleDialog(
            program = prog,
            allBookedSlots = bookedSlots,
            onDismiss = { showEditScheduleDialog = null },
            onSave = { newSlots ->
                viewModel.updateTvProgramSchedule(prog.id, newSlots)
                Toast.makeText(context, "Jadwal tayang ${prog.title} berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                showEditScheduleDialog = null
            }
        )
    }

    // Modal Ganti Nama Fasilitas
    showRenameFacilityDialog?.let { facility ->
        var renameText by remember { mutableStateOf(facility.name) }
        AlertDialog(
            onDismissRequest = { showRenameFacilityDialog = null },
            title = { Text("Ganti Nama Studio") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Nama Studio Baru") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val res = viewModel.renameTvFacility(ownedBusiness.instanceId, facility.id, renameText)
                    Toast.makeText(context, res.second, Toast.LENGTH_SHORT).show()
                    if (res.first) showRenameFacilityDialog = null
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameFacilityDialog = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 0: PROGRAM SIARAN
// -------------------------------------------------------------
@Composable
fun TvBroadcastProgramsTab(
    ownedBusiness: OwnedBusiness,
    activePrograms: List<TvProgram>,
    useShortFormat: Boolean,
    onOpenAddModal: () -> Unit,
    onOpenBiddingModal: () -> Unit,
    onOpenIpLibrary: () -> Unit,
    onCancelProgram: (String) -> Unit,
    onEditSchedule: (TvProgram) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenAddModal,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Program", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                FilledTonalButton(
                    onClick = onOpenBiddingModal,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lelang Hak Siar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onOpenIpLibrary,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("📚 Buka Gudang IP & Arsip Program TV", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Program Aktif Mengudara (${activePrograms.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (activePrograms.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada program siaran yang mengudara.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Bangun studio di tab Fasilitas, rekrut Direktur di tab Organisasi, lalu buat program TV pertamamu!", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(activePrograms, key = { it.id }) { prog ->
                TvProgramCard(
                    prog = prog,
                    useShortFormat = useShortFormat,
                    onCancel = { onCancelProgram(prog.id) },
                    onEditSchedule = { onEditSchedule(prog) }
                )
            }
        }
    }
}

@Composable
fun TvProgramCard(
    prog: TvProgram,
    useShortFormat: Boolean,
    onCancel: () -> Unit,
    onEditSchedule: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        prog.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                prog.type,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (prog.assignedStudioName != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "🎬 ${prog.assignedStudioName}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFF607D8B).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "👥 ${prog.requiredCrews} Kru",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF37474F),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Surface(
                    color = Color(0xFFFFB300).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text("Rating", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF8F00))
                        Text(
                            "${String.format("%.1f", prog.rating)}%",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFF8F00)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Time slots preview
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (prog.timeSlots.isNotEmpty()) "Jam Tayang: ${prog.timeSlots.joinToString(", ")}" else "Belum dijadwalkan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Finance telemetry
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Omzet Iklan/Bln", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        formatMoney(prog.monthlyAdRevenue.toLong(), useShortFormat),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column {
                    Text("Beban Ops/Bln", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        formatMoney(prog.currentOperationalCost.toLong(), useShortFormat),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    val net = (prog.monthlyAdRevenue - prog.currentOperationalCost).toLong()
                    Text("Laba Bersih/Bln", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        formatMoney(net, useShortFormat),
                        fontWeight = FontWeight.ExtraBold,
                        color = if (net >= 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEditSchedule,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Ubah Jam Tayang", fontSize = 12.sp)
                }

                Button(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Hentikan Siaran", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: MANAJEMEN FASILITAS (TUGAS 1)
// -------------------------------------------------------------
@Composable
fun TvFacilityManagementTab(
    ownedBusiness: OwnedBusiness,
    tvData: TvStationData,
    activePrograms: List<TvProgram>,
    useShortFormat: Boolean,
    onOpenBuildDialog: () -> Unit,
    onRenameFacility: (TvStudioFacility) -> Unit,
    onDemolishFacility: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary & Build button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Infrastruktur Gedung Utama", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Total Fasilitas: ${tvData.facilities.size} Studio & Control Room", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = onOpenBuildDialog,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bangun Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Biaya Perawatan Fasilitas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(formatMoney(tvData.totalFacilityUpkeep, useShortFormat) + "/bln", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Master Control Room", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("${tvData.masterControlCount} Unit (${tvData.maxSimultaneousPrograms} Slot Maks)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        item {
            Text("Daftar Studio & Fasilitas Fisik", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        if (tvData.facilities.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Belum ada studio yang dibangun.", fontWeight = FontWeight.Bold)
                        Text("Klik 'Bangun Studio' untuk mulai membangun Newsroom atau Studio Indoor!", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        } else {
            items(tvData.facilities, key = { it.id }) { facility ->
                val occupyingProg = activePrograms.find { it.assignedStudioId == facility.id }
                TvFacilityCard(
                    facility = facility,
                    occupyingProgram = occupyingProg,
                    useShortFormat = useShortFormat,
                    onRename = { onRenameFacility(facility) },
                    onDemolish = { onDemolishFacility(facility.id) }
                )
            }
        }
    }
}

@Composable
fun TvFacilityCard(
    facility: TvStudioFacility,
    occupyingProgram: TvProgram?,
    useShortFormat: Boolean,
    onRename: () -> Unit,
    onDemolish: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (facility.type) {
                                    TvFacilityType.NEWSROOM -> Color(0xFF1976D2).copy(alpha = 0.2f)
                                    TvFacilityType.STUDIO_KECIL -> Color(0xFF388E3C).copy(alpha = 0.2f)
                                    TvFacilityType.STUDIO_RAKSASA -> Color(0xFF7B1FA2).copy(alpha = 0.2f)
                                    TvFacilityType.MASTER_CONTROL -> Color(0xFFFF8F00).copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (facility.type) {
                                TvFacilityType.NEWSROOM -> Icons.Default.Info
                                TvFacilityType.STUDIO_KECIL -> Icons.Default.Face
                                TvFacilityType.STUDIO_RAKSASA -> Icons.Default.Star
                                TvFacilityType.MASTER_CONTROL -> Icons.Default.Settings
                            },
                            contentDescription = null,
                            tint = when (facility.type) {
                                TvFacilityType.NEWSROOM -> Color(0xFF1976D2)
                                TvFacilityType.STUDIO_KECIL -> Color(0xFF388E3C)
                                TvFacilityType.STUDIO_RAKSASA -> Color(0xFF7B1FA2)
                                TvFacilityType.MASTER_CONTROL -> Color(0xFFFF8F00)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(facility.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = onRename, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Nama", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Text(facility.type.displayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Surface(
                    color = if (occupyingProgram != null) Color(0xFFE53935).copy(alpha = 0.15f) else Color(0xFF4CAF50).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (occupyingProgram != null) "🔴 Dipakai" else "🟢 Tersedia",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (occupyingProgram != null) Color(0xFFE53935) else Color(0xFF4CAF50)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(facility.type.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (occupyingProgram != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Mengudara: ${occupyingProgram.title} (${occupyingProgram.timeSlots.joinToString()})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Biaya Perawatan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(formatMoney(facility.monthlyUpkeep, useShortFormat) + "/bln", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }

                OutlinedButton(
                    onClick = onDemolish,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bongkar Studio", fontSize = 11.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: STRUKTUR ORGANISASI (HR & DIREKSI) (TUGAS 2)
// -------------------------------------------------------------
@Composable
fun TvOrganizationHrTab(
    ownedBusiness: OwnedBusiness,
    tvData: TvStationData,
    activePrograms: List<TvProgram>,
    useShortFormat: Boolean,
    onHireDirector: (TvDirectorRole) -> Unit,
    onFireDirector: (TvDirectorRole) -> Unit,
    onHireCrews: (Int) -> Unit,
    onLayoffCrews: (Int) -> Unit
) {
    val usedCrews = activePrograms.sumOf { it.requiredCrews }
    val standbyCrews = maxOf(0, tvData.totalCrews - usedCrews)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // SDM & Crew Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Manajemen Kru Produksi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Kru", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${tvData.totalCrews} Orang", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Kru Bertugas (Aktif)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$usedCrews Orang", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Kru Siaga (Standby)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$standbyCrews Orang", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { if (tvData.totalCrews > 0) (usedCrews.toFloat() / tvData.totalCrews).coerceIn(0f, 1f) else 0f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Beban Gaji Kru: ${formatMoney(tvData.totalCrewSalaries, useShortFormat)}/bln (@${formatMoney(tvData.crewSalaryPerPerson, useShortFormat)}/orang)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onHireCrews(5) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("+5 Kru ($5k)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onHireCrews(10) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("+10 Kru ($10k)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onLayoffCrews(5) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            Text("PHK 5 Kru", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Text("Struktur Direksi (Unlocker Genre)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        items(TvDirectorRole.values().toList(), key = { it.name }) { role ->
            val isHired = tvData.hiredDirectors.contains(role.name)
            TvDirectorCard(
                role = role,
                isHired = isHired,
                useShortFormat = useShortFormat,
                onHire = { onHireDirector(role) },
                onFire = { onFireDirector(role) }
            )
        }
    }
}

@Composable
fun TvDirectorCard(
    role: TvDirectorRole,
    isHired: Boolean,
    useShortFormat: Boolean,
    onHire: () -> Unit,
    onFire: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHired) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isHired) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outlineVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isHired) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(role.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text(role.titleRole, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Surface(
                    color = if (isHired) Color(0xFF4CAF50).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isHired) "✓ Menjabat" else "Kosong",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isHired) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(role.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Genre yang dibuka:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(role.unlockedGenres) { genre ->
                    Surface(
                        color = if (isHired) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = genre,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isHired) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Gaji Bulanan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(formatMoney(role.monthlySalary, useShortFormat) + "/bln", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }

                if (isHired) {
                    OutlinedButton(
                        onClick = onFire,
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("Berhentikan (Pecat)", fontSize = 11.sp)
                    }
                } else {
                    Button(
                        onClick = onHire,
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Rekrut Direktur", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: LISENSI & JARINGAN TRANSMISI REGIONAL (TUGAS 3)
// -------------------------------------------------------------
@Composable
fun TvLicensesAndTransmissionTab(
    ownedBusiness: OwnedBusiness,
    tvData: TvStationData,
    useShortFormat: Boolean,
    onAcquireDewanPers: () -> Unit,
    onAcquireNationalLicense: () -> Unit,
    onBuildTransmission: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Regulasi & Sertifikasi Penyiaran", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }

        // Sertifikasi Dewan Pers
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF1976D2))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Sertifikasi Dewan Pers", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("Syarat mutlak program Berita & Investigasi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }

                        Surface(
                            color = if (tvData.dewanPersCertified) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFFF9800).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (tvData.dewanPersCertified) "✓ Tersertifikasi" else "Belum Ada",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (tvData.dewanPersCertified) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Syarat Pengajuan: Reputasi TV > 50 (Saat ini: ${String.format("%.1f", tvData.reputation)}) & Memiliki minimal 1 Newsroom.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!tvData.dewanPersCertified) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onAcquireDewanPers,
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Ajukan Sertifikasi Dewan Pers ($50,000)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Lisensi Penyiaran KPID/Kominfo
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Lisensi Penyiaran Nasional", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                Text("Legalitas spektrum terestrial Kominfo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }

                        Surface(
                            color = if (tvData.nationalBroadcastLicense) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFFF9800).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (tvData.nationalBroadcastLicense) "✓ Terbit" else "Belum Ada",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (tvData.nationalBroadcastLicense) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Benefit: +15% Rating Dasar Nasional untuk seluruh program siaran. (Syarat: Stasiun TV Level >= 2).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!tvData.nationalBroadcastLicense) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onAcquireNationalLicense,
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Beli Lisensi Nasional ($75,000)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Menara Transmisi Regional", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "Total Boost Iklan: +${((tvData.totalTransmissionRevenueMultiplier - 1.0) * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        items(TvRegionalTransmission.values().toList(), key = { it.regionKey }) { region ->
            val isUnlocked = tvData.unlockedTransmissions.contains(region.regionKey)
            TvTransmissionCard(
                region = region,
                isUnlocked = isUnlocked,
                useShortFormat = useShortFormat,
                onBuild = { onBuildTransmission(region.regionKey) }
            )
        }
    }
}

@Composable
fun TvTransmissionCard(
    region: TvRegionalTransmission,
    isUnlocked: Boolean,
    useShortFormat: Boolean,
    onBuild: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isUnlocked) Color(0xFF4CAF50).copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            tint = if (isUnlocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(region.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Jangkauan: ${region.populationCoverage}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Surface(
                    color = if (isUnlocked) Color(0xFF4CAF50).copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isUnlocked) "✓ Aktif" else "Belum Dibangun",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(region.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("📈 Max Rating +${region.maxRatingBonus.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF8F00))
                Text("💰 Omzet Iklan +${(region.adRevenueBoostPercent * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4CAF50))
            }

            if (!isUnlocked) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Biaya Konstruksi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(formatMoney(region.buildCost, useShortFormat), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                    Button(
                        onClick = onBuild,
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text("Bangun Menara", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODAL TAMBAH PROGRAM TV (TUGAS 4 OVERHAUL)
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTvProgramModal(
    ownedBusiness: OwnedBusiness,
    activePrograms: List<TvProgram>,
    bookedSlots: List<String>,
    useShortFormat: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (title: String, type: String, cost: Double, timeSlots: List<String>, studioId: String?) -> Unit
) {
    val tvData = ownedBusiness.tvStationData
    var programTitle by remember { mutableStateOf("") }
    val genres = listOf(
        "Berita" to 100_000.0,
        "Dokumenter" to 80_000.0,
        "Talkshow" to 60_000.0,
        "Investigasi Kriminal" to 120_000.0,
        "Sinetron" to 200_000.0,
        "Sitkom" to 90_000.0,
        "FTV" to 75_000.0,
        "Reality Show" to 250_000.0,
        "Pencarian Bakat (Talent Show)" to 350_000.0,
        "Variety Show" to 180_000.0,
        "Kuis Interaktif (Game Show)" to 110_000.0,
        "Animasi Anak" to 150_000.0,
        "Late Night Show" to 85_000.0
    )

    var selectedGenre by remember { mutableStateOf(genres[0].first) }
    var selectedSlots by remember { mutableStateOf(setOf<String>()) }
    var selectedStudioId by remember { mutableStateOf<String?>(null) }

    val currentGenreCost = genres.find { it.first == selectedGenre }?.second ?: 100_000.0
    val requiredDirector = getRequiredDirectorRole(selectedGenre)
    val hasDirector = requiredDirector == null || tvData.hiredDirectors.contains(requiredDirector.name)
    val needsDewanPers = isDewanPersRequired(selectedGenre)
    val hasDewanPers = !needsDewanPers || tvData.dewanPersCertified
    val compatibleStudioTypes = getCompatibleStudioTypes(selectedGenre)
    val compatibleFacilities = tvData.facilities.filter { compatibleStudioTypes.contains(it.type) }
    val requiredCrews = getRequiredCrewsForProgram(selectedGenre)
    val usedCrews = activePrograms.sumOf { it.requiredCrews }
    val standbyCrews = maxOf(0, tvData.totalCrews - usedCrews)
    val hasEnoughCrews = standbyCrews >= requiredCrews

    // Check studio clash
    val studioClashMap = remember(selectedSlots, activePrograms) {
        tvData.facilities.associate { facility ->
            val clashingProg = activePrograms.find { prog ->
                prog.assignedStudioId == facility.id && prog.timeSlots.any { slot -> selectedSlots.contains(slot) }
            }
            facility.id to clashingProg
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Produksi Program TV Baru", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 1. Judul Program
                        item {
                            OutlinedTextField(
                                value = programTitle,
                                onValueChange = { programTitle = it },
                                label = { Text("Judul Program Acara") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // 2. Pilih Genre / Format
                        item {
                            Text("Pilih Format / Genre:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                genres.forEach { (genre, _) ->
                                    FilterChip(
                                        selected = selectedGenre == genre,
                                        onClick = {
                                            selectedGenre = genre
                                            selectedStudioId = null
                                        },
                                        label = {
                                            Text(
                                                genre,
                                                fontSize = 11.sp,
                                                fontWeight = if (selectedGenre == genre) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        // Status Syarat Direksi & Regulasi
                        item {
                            if (!hasDirector || !hasDewanPers) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("⚠️ Syarat Belum Terpenuhi:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
                                        if (!hasDirector && requiredDirector != null) {
                                            Text("• Wajib merekrut ${requiredDirector.displayName} di tab Struktur Organisasi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                        }
                                        if (!hasDewanPers) {
                                            Text("• Wajib memiliki Sertifikasi Dewan Pers di tab Lisensi & Transmisi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("✓ Syarat Direksi & Regulasi Lengkap", modifier = Modifier.padding(8.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                                }
                            }
                        }

                        // 3. Jam Tayang (Tetris Grid - 4 Columns Responsive)
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Pilih Jam Tayang (Maks 4 Slot / 2 Jam):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("${selectedSlots.size}/4 Dipilih", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val allTimeSlots = (0..23).flatMap { h ->
                                val hStr = h.toString().padStart(2, '0')
                                listOf("$hStr:00", "$hStr:30")
                            }

                            // 4-column structured grid without horizontal scrolling
                            val chunkedSlots = allTimeSlots.chunked(4)
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                chunkedSlots.forEach { rowSlots ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        rowSlots.forEach { slot ->
                                            val isPrime = slot.startsWith("18") || slot.startsWith("19") || slot.startsWith("20") || slot.startsWith("21")
                                            val isSelected = selectedSlots.contains(slot)
                                            val isTaken = bookedSlots.contains(slot)

                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = when {
                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                    isTaken -> MaterialTheme.colorScheme.surfaceVariant
                                                    isPrime -> Color(0xFFFFB300).copy(alpha = 0.25f)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(34.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable(enabled = !isTaken) {
                                                        if (isSelected) {
                                                            selectedSlots = selectedSlots - slot
                                                        } else if (selectedSlots.size < 4) {
                                                            selectedSlots = selectedSlots + slot
                                                        }
                                                    }
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = slot,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSelected || isPrime) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) Color.White else if (isTaken) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                        }
                                        // Fill remaining slots if row has less than 4
                                        repeat(4 - rowSlots.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Alokasi Studio Fisik
                        item {
                            Text("Alokasi Studio Fisik:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))

                            if (compatibleFacilities.isEmpty()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "⚠️ Anda belum memiliki studio yang kompatibel! Genre $selectedGenre memerlukan: ${compatibleStudioTypes.joinToString { it.displayName }}. Bangun di tab Fasilitas!",
                                        modifier = Modifier.padding(8.dp),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    compatibleFacilities.forEach { facility ->
                                        val clashingProg = studioClashMap[facility.id]
                                        val isSelected = selectedStudioId == facility.id
                                        val isClashed = clashingProg != null

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable(enabled = !isClashed) {
                                                    selectedStudioId = facility.id
                                                },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = when {
                                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                    isClashed -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                }
                                            ),
                                            border = if (isSelected) CardDefaults.outlinedCardBorder() else null
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(facility.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text(facility.type.displayName, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                                }
                                                if (isClashed) {
                                                    Text("Bentrok: ${clashingProg?.title}", fontSize = 10.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                                } else if (isSelected) {
                                                    Text("✓ Terpilih", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                } else {
                                                    Text("Tersedia", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 5. Kalkulasi Biaya & Kru
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Biaya Produksi Awal:", fontSize = 11.sp)
                                        Text(formatMoney(currentGenreCost.toLong(), useShortFormat), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Kebutuhan Kru Lapangan:", fontSize = 11.sp)
                                        Text("$requiredCrews Kru (Tersedia: $standbyCrews)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (hasEnoughCrews) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tombol Konfirmasi Produksi
                val isValid = programTitle.isNotBlank() &&
                        selectedSlots.isNotEmpty() &&
                        selectedStudioId != null &&
                        hasDirector &&
                        hasDewanPers &&
                        hasEnoughCrews &&
                        ownedBusiness.companyCash >= currentGenreCost

                Button(
                    onClick = {
                        onSubmit(
                            programTitle.trim(),
                            selectedGenre,
                            currentGenreCost,
                            selectedSlots.toList().sorted(),
                            selectedStudioId
                        )
                    },
                    enabled = isValid,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Mulai Produksi & Mengudara", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG LELANG HAK SIAR OLAHRAGA (BIDDING WAR)
// -------------------------------------------------------------
@Composable
fun TvSportsBiddingDialog(
    ownedBusiness: OwnedBusiness,
    useShortFormat: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (title: String, fee: Long, duration: Int, slots: List<String>) -> Unit
) {
    val rightsList = listOf(
        Triple("FIFA World Cup 2026", 1_500_000L, 3),
        Triple("UEFA Champions League", 900_000L, 6),
        Triple("English Premier League", 1_200_000L, 9),
        Triple("Formula 1 Grand Prix Season", 750_000L, 8),
        Triple("Liga 1 Indonesia Championship", 500_000L, 6)
    )

    var selectedRight by remember { mutableStateOf(rightsList[0]) }
    var selectedSlots by remember { mutableStateOf(listOf("20:00", "20:30", "21:00", "21:30")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lelang Hak Siar Olahraga Premium") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Dapatkan lisensi penyiaran resmi event olahraga bergengsi dunia dengan rating masif!", fontSize = 12.sp)

                rightsList.forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedRight = item },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedRight == item) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(item.first, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Biaya Lisensi: ${formatMoney(item.second, useShortFormat)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                Text("Durasi: ${item.third} Bln", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSubmit(selectedRight.first, selectedRight.second, selectedRight.third, selectedSlots)
            }) {
                Text("Beli Hak Siar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// -------------------------------------------------------------
// DIALOG BANGUN FASILITAS BARU
// -------------------------------------------------------------
@Composable
fun BuildTvFacilityDialog(
    ownedBusiness: OwnedBusiness,
    useShortFormat: Boolean,
    onDismiss: () -> Unit,
    onBuild: (TvFacilityType, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(TvFacilityType.NEWSROOM) }
    var customName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bangun Fasilitas Studio Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TvFacilityType.values().forEach { type ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedType = type
                                if (customName.isBlank()) customName = type.displayName
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedType == type) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(type.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(type.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Biaya: ${formatMoney(type.buildCost, useShortFormat)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("Upkeep: ${formatMoney(type.monthlyUpkeep, useShortFormat)}/bln", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Nama Kustom Studio") },
                    placeholder = { Text(selectedType.displayName) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onBuild(selectedType, customName) }) {
                Text("Bangun (${formatMoney(selectedType.buildCost, useShortFormat)})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// -------------------------------------------------------------
// DIALOG UBAH JAM TAYANG PROGRAM
// -------------------------------------------------------------
@Composable
fun EditTvScheduleDialog(
    program: TvProgram,
    allBookedSlots: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var selectedSlots by remember { mutableStateOf(program.timeSlots.toSet()) }
    val allTimeSlots = (0..23).flatMap { h ->
        val hStr = h.toString().padStart(2, '0')
        listOf("$hStr:00", "$hStr:30")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Jam Tayang '${program.title}'") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pilih hingga 4 slot jam tayang (2 jam):", fontSize = 12.sp)
                    Text("${selectedSlots.size}/4 Dipilih", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }

                val chunkedSlots = allTimeSlots.chunked(4)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(chunkedSlots) { rowSlots ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rowSlots.forEach { slot ->
                                val isSelected = selectedSlots.contains(slot)
                                val isBookedByOther = allBookedSlots.contains(slot) && !program.timeSlots.contains(slot)
                                val isPrime = slot.startsWith("18") || slot.startsWith("19") || slot.startsWith("20") || slot.startsWith("21")

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isBookedByOther -> MaterialTheme.colorScheme.surfaceVariant
                                        isPrime -> Color(0xFFFFB300).copy(alpha = 0.25f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable(enabled = !isBookedByOther) {
                                            if (isSelected) {
                                                selectedSlots = selectedSlots - slot
                                            } else if (selectedSlots.size < 4) {
                                                selectedSlots = selectedSlots + slot
                                            }
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = slot,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected || isPrime) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else if (isBookedByOther) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            repeat(4 - rowSlots.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedSlots.toList().sorted()) },
                enabled = selectedSlots.isNotEmpty()
            ) {
                Text("Simpan Jadwal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

// -------------------------------------------------------------
// MODAL REKRUT DIREKSI & KRU (DYNAMIC CTA TAB 2)
// -------------------------------------------------------------
@Composable
fun TvQuickStaffModal(
    ownedBusiness: OwnedBusiness,
    tvData: TvStationData,
    useShortFormat: Boolean,
    onDismiss: () -> Unit,
    onHireDirector: (TvDirectorRole) -> Unit,
    onFireDirector: (TvDirectorRole) -> Unit,
    onHireCrews: (Int) -> Unit,
    onLayoffCrews: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rekrutmen Direksi & Kru Staf", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("1. Tambah Kru Produksi Lapangan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total Kru Saat Ini: ${tvData.totalCrews} Orang (Gaji: ${formatMoney(tvData.totalCrewSalaries, useShortFormat)}/bln)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { onHireCrews(5) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Text("+5 Kru ($5k)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onHireCrews(10) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Text("+10 Kru ($10k)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("2. Rekrut Direktur Program (Membuka Format)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                items(TvDirectorRole.values().toList(), key = { it.name }) { role ->
                    val isHired = tvData.hiredDirectors.contains(role.name)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, if (isHired) Color(0xFF16A34A) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(role.displayName, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                Text("Gaji: ${formatMoney(role.monthlySalary, useShortFormat)}/bln", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                Text("Buka: ${role.unlockedGenres.joinToString(", ")}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            if (isHired) {
                                OutlinedButton(
                                    onClick = { onFireDirector(role) },
                                    modifier = Modifier
                                        .widthIn(min = 80.dp)
                                        .height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Text("Pecat", fontSize = 11.sp, maxLines = 1)
                                }
                            } else {
                                Button(
                                    onClick = { onHireDirector(role) },
                                    modifier = Modifier
                                        .widthIn(min = 80.dp)
                                        .height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Text("Rekrut", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Selesai")
            }
        }
    )
}

// -------------------------------------------------------------
// MODAL BELI LISENSI & TRANSMISI (DYNAMIC CTA TAB 3)
// -------------------------------------------------------------
@Composable
fun TvQuickLicenseModal(
    ownedBusiness: OwnedBusiness,
    tvData: TvStationData,
    useShortFormat: Boolean,
    onDismiss: () -> Unit,
    onAcquireDewanPers: () -> Unit,
    onAcquireNationalLicense: () -> Unit,
    onBuildTransmission: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Beli Lisensi & Transmisi", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Sertifikasi & Lisensi Regulasi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Dewan Pers
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, if (tvData.dewanPersCertified) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text("Sertifikasi Dewan Pers", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                Text("Syarat Program Berita & Investigasi", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            if (tvData.dewanPersCertified) {
                                Text("✓ Aktif", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 12.sp)
                            } else {
                                Button(
                                    onClick = onAcquireDewanPers,
                                    modifier = Modifier
                                        .widthIn(min = 80.dp)
                                        .height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Text("Beli ($50k)", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                // Lisensi Nasional
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, if (tvData.nationalBroadcastLicense) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text("Lisensi Nasional Kominfo", fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                Text("+15% Rating Dasar Nasional", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            if (tvData.nationalBroadcastLicense) {
                                Text("✓ Aktif", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 12.sp)
                            } else {
                                Button(
                                    onClick = onAcquireNationalLicense,
                                    modifier = Modifier
                                        .widthIn(min = 80.dp)
                                        .height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Text("Beli ($75k)", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Jaringan Pemancar Transmisi Daerah", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                items(TvRegionalTransmission.values().toList(), key = { it.regionKey }) { region ->
                    val isUnlocked = tvData.unlockedTransmissions.contains(region.regionKey)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, if (isUnlocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(region.displayName, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                Text("+${(region.adRevenueBoostPercent * 100).toInt()}% Omzet Iklan | Biaya: ${formatMoney(region.buildCost, useShortFormat)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            if (isUnlocked) {
                                Text("✓ Terhubung", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 12.sp)
                            } else {
                                Button(
                                    onClick = { onBuildTransmission(region.regionKey) },
                                    modifier = Modifier
                                        .widthIn(min = 80.dp)
                                        .height(34.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    Text("Bangun", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

// -------------------------------------------------------------
// HELPER FORMAT CURRENCY
// -------------------------------------------------------------
private fun formatMoney(amount: Long, useShortFormat: Boolean): String {
    return if (useShortFormat) {
        when {
            amount >= 1_000_000_000_000L -> String.format("$%.1f T", amount / 1_000_000_000_000.0)
            amount >= 1_000_000_000L -> String.format("$%.1f B", amount / 1_000_000_000.0)
            amount >= 1_000_000L -> String.format("$%.1f M", amount / 1_000_000.0)
            amount >= 1_000L -> String.format("$%.1f K", amount / 1_000.0)
            else -> "$$amount"
        }
    } else {
        "$" + java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(amount)
    }
}
