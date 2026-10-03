package com.example.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BusinessCatalogItem
import com.example.data.BusinessCategory
import com.example.data.OwnedBusiness

data class SectorVisuals(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

fun getSectorVisuals(sector: String): SectorVisuals {
    return when (sector.uppercase()) {
        "PROPERTY", "REAL_ESTATE" -> SectorVisuals(Icons.Default.Home, Color(0xFF0D47A1))
        "FINANCE", "BANKING" -> SectorVisuals(Icons.Default.AccountBalance, Color(0xFF1B5E20))
        "AVIATION", "TRANSPORT" -> SectorVisuals(Icons.Default.Send, Color(0xFF03A9F4))
        "CONSUMER", "RETAIL" -> SectorVisuals(Icons.Default.ShoppingCart, Color(0xFFFF9800))
        "MINING", "ENERGY", "BASIC_MATERIALS" -> SectorVisuals(Icons.Default.Build, Color(0xFF424242))
        else -> SectorVisuals(Icons.Default.BusinessCenter, Color(0xFF37474F))
    }
}

@Composable
fun getSectorIcon(sector: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (sector.uppercase(java.util.Locale.US)) {
        "CULINARY", "RESTAURANT" -> androidx.compose.material.icons.Icons.Default.Restaurant
        "ENTERTAINMENT", "MEDIA" -> androidx.compose.material.icons.Icons.Default.Movie
        "RETAIL", "E_COMMERCE" -> androidx.compose.material.icons.Icons.Default.ShoppingCart
        "PROPERTY", "CONSTRUCTION", "REAL_ESTATE" -> androidx.compose.material.icons.Icons.Default.LocationCity
        "HEALTHCARE", "HOSPITAL" -> androidx.compose.material.icons.Icons.Default.LocalHospital
        "AVIATION", "TRANSPORT" -> androidx.compose.material.icons.Icons.Default.Flight
        "EVENT", "EXHIBITION" -> androidx.compose.material.icons.Icons.Default.Event
        "FINANCE", "BANKING" -> androidx.compose.material.icons.Icons.Default.AccountBalance
        else -> androidx.compose.material.icons.Icons.Default.BusinessCenter
    }
}

val com.example.data.HoldingCompany.type: String
    get() {
        val typesList = listOf("Entertainment Holdings", "F&B Holdings", "Property Holdings", "Retail Holdings", "Tech Holdings", "Finance Holdings", "Healthcare & Insurance Holdings", "Transportation Holdings")
        typesList.forEach { if (name.contains(it, ignoreCase = true)) return it }
        if (name.contains("Healthcare", ignoreCase = true) || name.contains("Daycare", ignoreCase = true) || name.contains("Insurance", ignoreCase = true)) return "Healthcare & Insurance Holdings"
        return typesList[kotlin.math.abs(instanceId.hashCode()) % typesList.size]
    }

fun getHoldingBackgroundImage(type: String?): String {
    val safeType = type ?: ""
    return when (safeType) {
        "Entertainment Holdings" -> "https://images.unsplash.com/photo-1476242906366-d8eb64c2f661?q=80&w=1769&auto=format&fit=crop"
        "F&B Holdings" -> "https://images.unsplash.com/photo-1682142882978-c19f975d2407?q=80&w=1170&auto=format&fit=crop"
        "Property Holdings" -> "https://images.unsplash.com/photo-1560518883-ce09059eeffa?q=80&w=1073&auto=format&fit=crop"
        "Retail Holdings" -> "https://plus.unsplash.com/premium_photo-1683141052679-942eb9e77760?q=80&w=1170&auto=format&fit=crop"
        "Tech Holdings" -> "https://images.unsplash.com/photo-1488590528505-98d2b5aba04b?q=80&w=1170&auto=format&fit=crop"
        "Finance Holdings" -> "https://plus.unsplash.com/premium_photo-1681487769650-a0c3fbaed85a?q=80&w=1255&auto=format&fit=crop"
        "Healthcare & Insurance Holdings", "Healthcare Holdings", "Daycare Holdings" -> "https://images.unsplash.com/photo-1519494026892-80bbd2d6fd0d?q=80&w=1170&auto=format&fit=crop"
        "Transportation Holdings" -> "https://images.unsplash.com/photo-1591768793355-74d04bb6608f?q=80&w=1172&auto=format&fit=crop"
        else -> "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab"
    }
}

@Composable
fun HoldingItemCard(
    holding: com.example.data.HoldingCompany,
    rev: Long,
    useShortFormat: Boolean,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth().clickable { onClick() },
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 140.dp)) {
                // Layer 1: Background Image
                val finalUrl = getHoldingBackgroundImage(holding.type)
                coil.compose.AsyncImage(
                    model = finalUrl,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                // Layer 2: Gradient Overlay (Hitam Transparan ke Hitam Pekat)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.95f))
                            )
                        )
                )

                // Layer 3: Text & Star Icon
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFFFD700).copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = holding.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = holding.type,
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${holding.subsidiaries.size} Anak Perusahaan",
                                fontSize = 12.sp,
                                color = Color(0xFFE0E0E0),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Text(
                        text = "+$${com.example.ui.formatCurrencyRingkas(rev, useShortFormat)}/bln",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun getFilmStatusColor(status: String): Color {
    val s = status.lowercase()
    return when {
        s.contains("tayang") -> Color(0xFF4CAF50) // Hijau terang
        s.contains("pra-produksi") || s.contains("antrean") -> Color.Gray
        s.contains("syuting") || s.contains("produksi animasi") -> Color(0xFFFFC107) // Kuning Amber
        s.contains("pasca produksi") || s.contains("qc") || s.contains("poles") -> Color(0xFF64FFDA) // Biru/hijau muda
        s.contains("produksi") -> Color(0xFFFFC107)
        s.contains("menunggu") -> Color.LightGray // Abu-abu
        else -> Color.White
    }
}

@Composable
fun BusinessItemCard(
    owned: OwnedBusiness,
    catalogItem: BusinessCatalogItem,
    rev: Long,
    useShortFormat: Boolean,
    stockSector: String? = null,
    onClick: () -> Unit
) {
    var isPreviewExpanded by remember { mutableStateOf(false) }
    var showUnitPolicyDialog by remember { mutableStateOf(false) }
    
    // Safe Variables (Null-Safety)
    val safeChildCount = owned.subsidiaries?.size ?: 0
    val safeFilms = owned.projectHistory ?: emptyList()
    val safeCustomName = owned.customName ?: catalogItem.name
    val safeStudioType = owned.studioType ?: "LIVE_ACTION"
    
    val isAcquired = owned.acquiredStockTicker != null

    val cardIcon: androidx.compose.ui.graphics.vector.ImageVector
    val sectorText: String

    if (isAcquired && stockSector != null) {
        cardIcon = getSectorIcon(stockSector)
        sectorText = "Sektor: ${stockSector ?: "General"}"
    } else {
        cardIcon = getSectorIcon(catalogItem.category.name)
        sectorText = if (catalogItem.id == "media_production") {
            if (safeStudioType == "ANIMATION") "Sektor: ${catalogItem.category.name} • Animation Studio" else "Sektor: ${catalogItem.category.name} • Live-Action Studio"
        } else "Sektor: ${catalogItem.category.name}"
    }

    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !owned.isUpgrading) { onClick() },
            color = if (owned.isUpgrading) Color.LightGray.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 140.dp)) {
                // Layer 1: Background Image
                val fallbackImage = "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=1470&auto=format&fit=crop"
                val finalUrl = catalogItem.imageUrl ?: fallbackImage
                coil.compose.AsyncImage(
                    model = finalUrl,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                // Layer 2: Gradient Overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Black.copy(alpha = 0.9f), Color.Black)
                            )
                        )
                )

                // Layer 3: Konten Teks & Icon
                Column(modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = if (catalogItem.id == "media_production") 0.dp else 16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                    // Left: Icon
                    Surface(
                        color = if (owned.isUpgrading) Color.Gray else MaterialTheme.colorScheme.primary,
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                cardIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Middle: Content
                    Column(
                        modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            text = safeCustomName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = sectorText,
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        
                        if (catalogItem.id == "fine_dining") {
                            Text(
                                text = "Total Cabang: $safeChildCount",
                                fontSize = 12.sp,
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        if (owned.isUpgrading) {
                            Text(
                                text = "🚧 Dikonstruksi (${owned.upgradeDelayMonths} bln)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Lvl ${owned.level}",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${owned.purchasedUpgrades.size}/${catalogItem.upgrades.size} Upg",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (rev == 0L) "$0" else formatCurrencyRingkas(rev, useShortFormat),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFFD700)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (rev == 0L) "Pending" else "/bln",
                                    fontSize = 10.sp,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                                if (owned.companyCash > 0.0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• Kas: ${formatCurrencyRingkas(owned.companyCash.toLong(), useShortFormat)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF81C784),
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }
                        }
                        
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    // Right: Tune Policy & Navigation Arrow
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        IconButton(
                            onClick = { showUnitPolicyDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "Kebijakan Arus Kas Unit",
                                tint = Color(0xFF90CAF9),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
                
                // Live Preview for Movie Studio
                if (catalogItem.id == "media_production") {
                    val films = safeFilms.filter { it.status == "IN_PRODUCTION" || it.status == "IN_THEATERS" }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.animateContentSize(
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        ) {
                            if (films.isEmpty()) {
                                Text("Tidak ada proyek film yang sedang diproduksi.", color = Color.Gray, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            } else {
                                val displayFilms = if (isPreviewExpanded) films else films.take(2)
                                displayFilms.forEach { film ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = film.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                            val statusText = if (film.status == "IN_PRODUCTION") {
                                                if (film.isAwaitingRelease) "Menunggu Rilis" else if (film.isQcPhase) "Fase QC Internal" else if (film.productionPhase == "ANTREAN") "Menunggu Jadwal" else film.productionPhase
                                            } else "Tayang"
                                            Text(text = statusText, color = getFilmStatusColor(statusText), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        }
                                        val rightInfo = if (film.status == "IN_THEATERS") if (film.currentRevenue == 0L) "$0" else formatCurrencyRingkas(film.currentRevenue, useShortFormat) else "Sisa: ${film.productionDelayMonths} bln"
                                        Text(text = rightInfo, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                if (films.size > 2) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { isPreviewExpanded = !isPreviewExpanded },
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = if (isPreviewExpanded) "Tutup Detail" else "Lihat Semua (${films.size})", color = Color.Yellow.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                        Icon(
                                            imageVector = if (isPreviewExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color.Yellow.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp).padding(start = 2.dp)
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

        
        // Notification Badge (Red Circle) - Always on Finance as requested dummy
        if (catalogItem.category == BusinessCategory.FINANCE) {
            Surface(
                color = MaterialTheme.colorScheme.error, // Red
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
            ) {
                Text(
                    text = "!", // Required dummy
                    color = MaterialTheme.colorScheme.onError,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }

    if (showUnitPolicyDialog) {
        com.example.corporate.ui.DividendPolicyDialog(
            businessId = owned.instanceId,
            businessName = safeCustomName,
            estimatedMonthlyProfit = if (rev > 0L) rev else 50_000L,
            onDismiss = { showUnitPolicyDialog = false }
        )
    }
}

// Re-export / delegate Film Studio components for backward compatibility
typealias FilmScheduleStage = com.example.filmstudio.model.FilmScheduleStage
typealias FilmMonthDetail = com.example.filmstudio.model.FilmMonthDetail

fun getStudioFilmActivities(
    targetMonth: Int,
    targetYear: Int,
    currentMonth: Int,
    currentYear: Int,
    allFilms: List<com.example.data.MovieProject>,
    isAnimation: Boolean
): List<FilmMonthDetail> = com.example.filmstudio.engine.FilmTimelineProjector.getStudioFilmActivities(
    targetMonth, targetYear, currentMonth, currentYear, allFilms, isAnimation
)

@Composable
fun FilmStudioCalendarSlate(
    owned: com.example.data.OwnedBusiness,
    currentMonth: Int,
    currentYear: Int,
    selectedSchedMonth: Int?,
    selectedSchedYear: Int?,
    onSelectScheduleMonth: (Int?, Int?) -> Unit,
    useShortFormat: Boolean
) = com.example.filmstudio.ui.FilmStudioCalendarSlate(
    owned, currentMonth, currentYear, selectedSchedMonth, selectedSchedYear, onSelectScheduleMonth, useShortFormat
)

@Composable
fun FilmProductionForm(
    owned: com.example.data.OwnedBusiness,
    playerCash: Long,
    useShortFormat: Boolean,
    currentMonth: Int,
    currentYear: Int,
    onProduce: (title: String, budget: Long, promoBudget: Long, genres: List<String>, isGlobal: Boolean, schedMonth: Int?, schedYear: Int?, filmFormat: String, productionFocus: String, scheduledReleaseDate: String?, targetDurationMonths: Int?) -> Boolean,
    onPolish: (title: String, budgetCost: Long, extraMonths: Int) -> Unit = { _,_,_ -> },
    onSchedule: (title: String, schedStr: String) -> Unit = { _,_ -> },
    onCancel: (title: String, refundAmount: Long) -> Unit = { _,_ -> },
    onOpenHistory: () -> Unit
) = com.example.filmstudio.ui.FilmProductionForm(
    owned, playerCash, useShortFormat, currentMonth, currentYear, onProduce, onPolish, onSchedule, onCancel, onOpenHistory
)

@Composable
fun CalendarPickerDialog(
    currentMonth: Int,
    currentYear: Int,
    initialMonth: Int?,
    initialYear: Int?,
    bookedSchedules: List<Pair<Int, Int>> = emptyList(),
    allProjects: List<com.example.data.MovieProject> = emptyList(),
    isStudioAnimation: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Int?, Int?) -> Unit
) = com.example.filmstudio.ui.CalendarPickerDialog(
    currentMonth, currentYear, initialMonth, initialYear, bookedSchedules, allProjects, isStudioAnimation, onDismiss, onConfirm
)

@Composable
fun FilmReleaseSchedulePickerDialog(
    projectTitle: String,
    startMonth: Int,
    startYear: Int,
    estDurationMonths: Int,
    qcMonth: Int,
    qcYear: Int,
    currentMonth: Int,
    currentYear: Int,
    initialSelectedMonth: Int?,
    initialSelectedYear: Int?,
    allProjects: List<com.example.data.MovieProject> = emptyList(),
    isStudioAnimation: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Int?, Int?) -> Unit
) = com.example.filmstudio.ui.FilmReleaseSchedulePickerDialog(
    projectTitle, startMonth, startYear, estDurationMonths, qcMonth, qcYear, currentMonth, currentYear, initialSelectedMonth, initialSelectedYear, allProjects, isStudioAnimation, onDismiss, onConfirm
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SoftwareHouseDashboard(
    appProjects: List<com.example.data.AppProject>,
    businessLevel: Int,
    ownedBusinesses: List<com.example.data.OwnedBusiness>,
    playerCash: Long,
    useShortFormat: Boolean,
    onStartProject: (String, com.example.data.ProjectType, Double, Double, Int, String?) -> Unit,
    onSellSaaS: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("B2B Market", "SaaS Portfolio", "Synergy Hub")

    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Software House Kanban", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            androidx.compose.material3.TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    androidx.compose.material3.Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when(selectedTab) {
                0 -> {
                    val activeB2B = appProjects.filter { it.type == com.example.data.ProjectType.CLIENT_B2B && it.status == com.example.data.ProjectStatus.DEVELOPMENT }
                    if (activeB2B.isNotEmpty()) {
                        Text("Active B2B Projects:", fontWeight = FontWeight.Bold)
                        activeB2B.forEach { proj ->
                             val prog = proj.currentMonth.toFloat() / proj.devTimeMonths.coerceAtLeast(1)
                             Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                 Column(modifier = Modifier.padding(12.dp)) {
                                      Text(proj.title, fontWeight = FontWeight.Bold)
                                      Text("Month: ${proj.currentMonth} / ${proj.devTimeMonths}", style = MaterialTheme.typography.bodySmall)
                                      androidx.compose.material3.LinearProgressIndicator(progress = { prog }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                                      Text("Budget/mo: -${formatCurrencyRingkas((proj.budgetCost / proj.devTimeMonths).toLong(), useShortFormat)} | Payout: +${formatCurrencyRingkas(proj.targetRevenue.toLong(), useShortFormat)}", style = MaterialTheme.typography.labelSmall)
                                 }
                             }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    Text("Available B2B Contracts:", fontWeight = FontWeight.Bold)
                    val available = remember(businessLevel) {
                         val list = mutableListOf<Triple<String, Double, Double>>() // title, budget, revenue
                         if (businessLevel < 10) {
                              list.add(Triple("Sistem Kasir Toko", 10000.0, 30000.0))
                              list.add(Triple("Website Company Profile", 5000.0, 15000.0))
                              list.add(Triple("Aplikasi Antrian Faskes", 15000.0, 40000.0))
                         } else {
                              list.add(Triple("Integrasi Big Data", 150000.0, 450000.0))
                              list.add(Triple("Super App Ekosistem", 500000.0, 2000000.0))
                              list.add(Triple("AI Customer Service", 200000.0, 800000.0))
                         }
                         list
                    }
                    val devTime = if (businessLevel < 10) 3 else 7
                    available.forEach { opt ->
                         Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                             Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                  Column(modifier = Modifier.weight(1f)) {
                                      Text(opt.first, fontWeight = FontWeight.Bold)
                                      Text("Dev: $devTime bulan", style = MaterialTheme.typography.labelSmall)
                                      Text("Budget: ${formatCurrencyRingkas(opt.second.toLong(), useShortFormat)} | Payout: ${formatCurrencyRingkas(opt.third.toLong(), useShortFormat)}", style = MaterialTheme.typography.labelSmall)
                                  }
                                  Button(
                                      onClick = { onStartProject(opt.first, com.example.data.ProjectType.CLIENT_B2B, opt.second, opt.third, devTime, null) },
                                      enabled = playerCash >= opt.second.toLong()
                                  ) {
                                      Text("Ambil")
                                  }
                             }
                         }
                    }
                }
                1 -> {
                    val activeDev = appProjects.filter { it.type == com.example.data.ProjectType.INDEPENDENT_SAAS && it.status == com.example.data.ProjectStatus.DEVELOPMENT }
                    if (activeDev.isNotEmpty()) {
                        Text("In Development:", fontWeight = FontWeight.Bold)
                        activeDev.forEach { proj ->
                             val prog = proj.currentMonth.toFloat() / proj.devTimeMonths.coerceAtLeast(1)
                             Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                 Column(modifier = Modifier.padding(12.dp)) {
                                      Text(proj.title, fontWeight = FontWeight.Bold)
                                      Text("Month: ${proj.currentMonth} / ${proj.devTimeMonths}", style = MaterialTheme.typography.bodySmall)
                                      androidx.compose.material3.LinearProgressIndicator(progress = { prog }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                                 }
                             }
                        }
                    }

                    val released = appProjects.filter { it.type == com.example.data.ProjectType.INDEPENDENT_SAAS && it.status == com.example.data.ProjectStatus.MAINTENANCE }
                    if (released.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Live SaaS Apps:", fontWeight = FontWeight.Bold)
                        released.forEach { proj ->
                             Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                 Column(modifier = Modifier.padding(12.dp)) {
                                      Text(proj.title, fontWeight = FontWeight.Bold)
                                      Text("MRR: ${formatCurrencyRingkas(proj.targetRevenue.toLong(), useShortFormat)}/bln", color = Color(0xFF00C853))
                                      Button(onClick = { onSellSaaS(proj.id) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                          Text("Acquisition/Jual (+${formatCurrencyRingkas((proj.targetRevenue * 50).toLong(), useShortFormat)})")
                                      }
                                 }
                             }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                         onStartProject("Custom SaaS App", com.example.data.ProjectType.INDEPENDENT_SAAS, 50000.0, 15000.0, 5, null)
                    }, modifier = Modifier.fillMaxWidth(), enabled = playerCash >= 50000) {
                         Text("Bangun SaaS Baru (Cost: 50k, 5bln)")
                    }
                }
                2 -> {
                    Text("In-House Synergy", fontWeight = FontWeight.Bold)
                    val synergyOptions = mutableListOf<Triple<String, String, String>>()
                    synergyOptions.add(Triple("media_tv", "Bangun Platform Streaming", "Sinergi TV Station (+25% Rev)"))
                    synergyOptions.add(Triple("retail_supermarket", "Bangun Aplikasi E-Commerce", "Sinergi Supermarket (+25% Rev)"))

                    synergyOptions.forEach { opt ->
                        val targetBiz = ownedBusinesses.find { it.catalogId == opt.first }
                        if (targetBiz != null) {
                            val activeProj = appProjects.find { it.targetBusinessId == targetBiz.instanceId && it.status == com.example.data.ProjectStatus.DEVELOPMENT }
                            val completedProj = appProjects.find { it.targetBusinessId == targetBiz.instanceId && it.status == com.example.data.ProjectStatus.COMPLETED }
                            
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                    Text(opt.third, fontWeight = FontWeight.Bold)
                                    if (completedProj != null) {
                                        Text("Selesai & Aktif", color = Color(0xFF00C853))
                                    } else if (activeProj != null) {
                                        val prog = activeProj.currentMonth.toFloat() / activeProj.devTimeMonths.coerceAtLeast(1)
                                        Text("In Development: ${activeProj.currentMonth}/${activeProj.devTimeMonths} bln")
                                        androidx.compose.material3.LinearProgressIndicator(progress = { prog }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                                    } else {
                                        Button(
                                            onClick = { onStartProject(opt.second, com.example.data.ProjectType.ECOSYSTEM_SYNERGY, 200000.0, 0.0, 6, targetBiz.instanceId) },
                                            enabled = playerCash >= 200000
                                        ) {
                                            Text("Mulai Bangun (Cost 200k, 6 bln)")
                                        }
                                    }
                                }
                            }
                        } else {
                            Text("Belum punya bisnis untuk: ${opt.third}", color = Color.Gray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        }
    }
}
