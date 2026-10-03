package com.example.businessunit.contentcreator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessunit.contentcreator.model.CoProductionFundingScheme
import com.example.businessunit.contentcreator.model.ContentSortOption
import com.example.businessunit.contentcreator.model.ContentStatus
import com.example.businessunit.contentcreator.model.ContentWork
import com.example.ui.formatCurrencyRingkas

@Composable
fun ContentCreatorPortfolioCard(
    portfolio: List<ContentWork>,
    onStartProductionClick: () -> Unit,
    onPitchPHClick: (ContentWork) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedSort by remember { mutableStateOf(ContentSortOption.ENGAGEMENT_HIGHEST) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val filteredWorks = remember(portfolio, selectedFilter, selectedSort) {
        val filtered = when (selectedFilter) {
            "AVAILABLE" -> portfolio.filter { it.status == ContentStatus.AVAILABLE }
            "LICENSED" -> portfolio.filter { it.status == ContentStatus.LICENSED }
            "ACQUIRED" -> portfolio.filter { it.status == ContentStatus.ACQUIRED_LUMP_SUM }
            else -> portfolio
        }
        when (selectedSort) {
            ContentSortOption.ENGAGEMENT_HIGHEST -> filtered.sortedByDescending { it.engagementScore ?: 0 }
            ContentSortOption.ROYALTY_HIGHEST -> filtered.sortedByDescending { it.monthlyRoyalty }
            ContentSortOption.EXPIRING_SOON -> filtered.sortedBy { it.remainingContractMonths ?: 999 }
            ContentSortOption.BUDGET_HIGHEST -> filtered.sortedByDescending { it.budget }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header & Produksi Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ContentCreatorTheme.AccentMagenta.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = ContentCreatorTheme.AccentMagenta, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("Bank Konten (Katalog Karya)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ContentCreatorTheme.TextWhite)
                    Text("Portofolio IP Original & Lisensi PH", fontSize = 11.sp, color = ContentCreatorTheme.TextGray)
                }
            }

            Button(
                onClick = onStartProductionClick,
                colors = ButtonDefaults.buttonColors(containerColor = ContentCreatorTheme.AccentMagenta),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Produksi", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Filter Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val totalAll = portfolio.size
            val totalAvail = portfolio.count { it.status == ContentStatus.AVAILABLE }
            val totalLic = portfolio.count { it.status == ContentStatus.LICENSED }
            val totalAcq = portfolio.count { it.status == ContentStatus.ACQUIRED_LUMP_SUM }

            val tabs = listOf(
                "ALL" to "Semua ($totalAll)",
                "AVAILABLE" to "Tersedia ($totalAvail)",
                "LICENSED" to "Lisensi ($totalLic)",
                "ACQUIRED" to "Jual Putus ($totalAcq)"
            )

            tabs.forEach { (id, label) ->
                val isSelected = selectedFilter == id
                Surface(
                    onClick = { selectedFilter = id },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) ContentCreatorTheme.AccentMagenta else ContentCreatorTheme.CardDark,
                    border = BorderStroke(1.dp, if (isSelected) ContentCreatorTheme.AccentMagenta else Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else ContentCreatorTheme.TextGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        // Sort Control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Karya Aktif di Portofolio", fontSize = 12.sp, color = ContentCreatorTheme.TextGray)
            Box {
                OutlinedButton(
                    onClick = { sortMenuExpanded = true },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ContentCreatorTheme.TextWhite),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(14.dp), tint = ContentCreatorTheme.AccentMagenta)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(selectedSort.displayName, fontSize = 11.sp, color = ContentCreatorTheme.TextWhite)
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                    modifier = Modifier.background(ContentCreatorTheme.CardDark)
                ) {
                    ContentSortOption.values().forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.displayName, color = ContentCreatorTheme.TextWhite, fontSize = 12.sp) },
                            onClick = {
                                selectedSort = opt
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Works List
        if (filteredWorks.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada karya dalam kategori ini.\nKlik tombol '+ Produksi' untuk merilis IP original baru.",
                        color = ContentCreatorTheme.TextGray,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                filteredWorks.forEach { work ->
                    val isStudioDominant = work.isShadowRecord ||
                        work.fundingScheme == CoProductionFundingScheme.STUDIO_30_70 ||
                        work.fundingScheme == CoProductionFundingScheme.FULL_STUDIO

                    val isCoProd = work.partnerStudioName != null || work.fundingScheme != null

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ContentCreatorTheme.CardDark),
                        border = BorderStroke(
                            1.dp,
                            if (isCoProd) ContentCreatorTheme.NeonGreen.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Top Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = ContentCreatorTheme.AccentMagenta.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = work.type.displayName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ContentCreatorTheme.AccentMagenta,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (isCoProd) {
                                        Surface(
                                            color = ContentCreatorTheme.NeonGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(Icons.Default.Handshake, contentDescription = null, tint = ContentCreatorTheme.NeonGreen, modifier = Modifier.size(10.dp))
                                                Text(
                                                    text = when (work.fundingScheme) {
                                                        CoProductionFundingScheme.CREATOR_70_30 -> "Co-Prod (70% Kreator)"
                                                        CoProductionFundingScheme.JOINT_VENTURE_50_50 -> "Co-Prod (50/50 JV)"
                                                        CoProductionFundingScheme.STUDIO_30_70 -> "Co-Prod (70% Studio)"
                                                        CoProductionFundingScheme.FULL_STUDIO -> "100% Studio"
                                                        else -> "Co-Production"
                                                    },
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ContentCreatorTheme.NeonGreen
                                                )
                                            }
                                        }
                                    }
                                }

                                Surface(
                                    color = when (work.status) {
                                        ContentStatus.AVAILABLE -> ContentCreatorTheme.NeonGreen.copy(alpha = 0.15f)
                                        ContentStatus.LICENSED -> ContentCreatorTheme.AccentCyan.copy(alpha = 0.15f)
                                        ContentStatus.ACQUIRED_LUMP_SUM -> ContentCreatorTheme.Gold.copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = work.status.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (work.status) {
                                            ContentStatus.AVAILABLE -> ContentCreatorTheme.NeonGreen
                                            ContentStatus.LICENSED -> ContentCreatorTheme.AccentCyan
                                            ContentStatus.ACQUIRED_LUMP_SUM -> ContentCreatorTheme.Gold
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = work.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = ContentCreatorTheme.TextWhite
                            )

                            if (work.creativeFocus != null) {
                                Text(
                                    text = "Fokus: ${work.creativeFocus}",
                                    fontSize = 10.sp,
                                    color = ContentCreatorTheme.TextGray
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Engagement Score", fontSize = 10.sp, color = ContentCreatorTheme.TextGray)
                                    Text("★ ${work.engagementScore ?: 0}/100", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.Gold)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Budget Produksi", fontSize = 10.sp, color = ContentCreatorTheme.TextGray)
                                    Text(formatCurrencyRingkas(work.budget, false), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.TextWhite)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    when (work.status) {
                                        ContentStatus.LICENSED -> {
                                            Text("Royalti / Bln", fontSize = 10.sp, color = ContentCreatorTheme.TextGray)
                                            Text("+${formatCurrencyRingkas(work.monthlyRoyalty, false)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.NeonGreen)
                                        }
                                        ContentStatus.ACQUIRED_LUMP_SUM -> {
                                            Text("Hasil Jual Putus", fontSize = 10.sp, color = ContentCreatorTheme.TextGray)
                                            Text("+${formatCurrencyRingkas(work.acquiredLumpSum, false)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.Gold)
                                        }
                                        ContentStatus.AVAILABLE -> {
                                            Text("Status Lisensi", fontSize = 10.sp, color = ContentCreatorTheme.TextGray)
                                            Text(
                                                text = if (isStudioDominant) "Katalog Studio" else "Siap Ditawarkan",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isStudioDominant) ContentCreatorTheme.Gold else ContentCreatorTheme.NeonGreen
                                            )
                                        }
                                    }
                                }
                            }

                            if (work.status == ContentStatus.LICENSED) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Mitra: ${work.acquiredByPH ?: "Production House"}", fontSize = 10.sp, color = ContentCreatorTheme.AccentCyan)
                                    Text("Sisa: ${work.remainingContractMonths ?: 0} bln", fontSize = 10.sp, color = ContentCreatorTheme.TextGray)
                                }
                            }

                            // Distribution Actions / Studio Ownership Notice
                            if (work.status == ContentStatus.AVAILABLE) {
                                Spacer(modifier = Modifier.height(10.dp))
                                if (isStudioDominant) {
                                    // Studio Dominant (70% Studio / 100% Studio) -> Portfolio showcase only in Creator Bank!
                                    Surface(
                                        color = ContentCreatorTheme.Gold.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, ContentCreatorTheme.Gold.copy(alpha = 0.3f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = null, tint = ContentCreatorTheme.Gold, modifier = Modifier.size(14.dp))
                                            Text(
                                                text = "🔒 Hak distribusi & lisensi dipegang oleh Studio Film (${work.partnerStudioName ?: "Studio"}). Dikelola melalui Katalog Film.",
                                                fontSize = 10.sp,
                                                color = ContentCreatorTheme.Gold
                                            )
                                        }
                                    }
                                } else {
                                    // Creator Dominant / Joint Venture -> Creator can pitch to PH/OTT
                                    Button(
                                        onClick = { onPitchPHClick(work) },
                                        modifier = Modifier.fillMaxWidth().height(36.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ContentCreatorTheme.AccentCyan.copy(alpha = 0.2f)),
                                        border = BorderStroke(1.dp, ContentCreatorTheme.AccentCyan),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = if (work.fundingScheme == CoProductionFundingScheme.JOINT_VENTURE_50_50)
                                                "Tawarkan ke PH / OTT (Bagi Hasil 50%) ➔"
                                            else
                                                "Tawarkan ke PH / OTT ➔",
                                            color = ContentCreatorTheme.AccentCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
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
