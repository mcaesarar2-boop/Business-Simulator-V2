package com.example.publisher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.publisher.engine.GamePublisherEngine
import com.example.publisher.model.GameProject
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NeonGreen

/**
 * Screen 3: Released Catalog Screen (Katalog Game Terbit).
 * A poster grid showcasing published titles, Metacritic review badges,
 * active player populations, lifetime sales, and post-launch patching.
 */
@Composable
fun ReleasedCatalogScreen(
    releasedProjects: List<GameProject>,
    selectedProjectDetails: GameProject?,
    onOpenDetails: (GameProject) -> Unit,
    onCloseDetails: () -> Unit,
    onDeployPatch: (projectId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 84.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Master Vault Catalog Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Text(
                        text = "Katalog Game Komersial",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Portofolio game yang telah dirilis ke pasar global dan menghasilkan royalti pasif bulanan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Aggregate stats summary card
                    val totalCopies = releasedProjects.sumOf { it.totalSales }
                    val totalRevenue = releasedProjects.sumOf { it.lifetimeRevenue }
                    val totalPublisherProfit = releasedProjects.sumOf { it.publisherLifetimeProfit }
                    val totalPlayers = releasedProjects.sumOf { it.activePlayers }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Total Kopi", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatNumber(totalCopies), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Omset Global", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${formatMoney(totalRevenue)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = GoldAccent)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Laba Bersih", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${formatMoney(totalPublisherProfit)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = NeonGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pemain Aktif", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(formatNumber(totalPlayers), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }
            }

            if (releasedProjects.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Belum Ada Game Yang Rilis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Selesaikan proyek di tab Produksi dan lakukan 'Rilis Global' untuk mulai menjual game ke seluruh dunia.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(releasedProjects, key = { it.id }) { project ->
                    CinematicPosterCard(
                        project = project,
                        onClick = { onOpenDetails(project) },
                        onPatchClick = { onDeployPatch(project.id) }
                    )
                }
            }
        }

        // Detailed Game Inspect Dialog
        selectedProjectDetails?.let { project ->
            CinematicGameInspectDialog(
                project = project,
                onDismiss = onCloseDetails,
                onDeployPatch = { onDeployPatch(project.id) }
            )
        }
    }
}

/**
 * Cinematic Movie/Game Poster style card.
 */
@Composable
fun CinematicPosterCard(
    project: GameProject,
    onClick: () -> Unit,
    onPatchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("released_poster_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Poster Box Image with Review Badge Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.9f)
                    .background(Color(0xFF1E1E2E))
            ) {
                // Background Poster Image
                AsyncImage(
                    model = if (project.posterImageUrl.isNotEmpty()) project.posterImageUrl
                    else "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=600&auto=format&fit=crop",
                    contentDescription = project.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Gradient Scrim for readable badges
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                            )
                        )
                )

                // Bottom Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                // Metacritic Review Score Badge (Top Right)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        project.reviewScore >= 85 -> Color(0xFF43A047) // Universal Acclaim Green
                        project.reviewScore >= 70 -> Color(0xFFFDD835) // Favorable Yellow
                        project.reviewScore >= 50 -> Color(0xFFFB8C00) // Mixed Orange
                        else -> Color(0xFFE53935) // Disliked Red
                    }
                ) {
                    Text(
                        text = "${project.reviewScore}",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (project.reviewScore >= 70 && project.reviewScore < 85) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                // Genre Tag (Top Left)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = project.genre.displayName.split(" ").first(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Bottom Title & Studio on Image
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = project.studioName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Stats Dashboard Bottom Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Total Sales & Publisher Profit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Terjual", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatNumber(project.totalSales), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Laba Publisher", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$${formatMoney(project.publisherLifetimeProfit)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = NeonGreen)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Active Players & Patches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NeonGreen)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${formatNumber(project.activePlayers)} aktif",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (project.patchCount > 0) {
                        Text(
                            text = "v1.${project.patchCount}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Patch button
                OutlinedButton(
                    onClick = onPatchClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .testTag("patch_button_${project.id}")
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Update Patch ($20k)", fontSize = 10.sp)
                }
            }
        }
    }
}

/**
 * Detailed Game Inspect Dialog.
 */
@Composable
fun CinematicGameInspectDialog(
    project: GameProject,
    onDismiss: () -> Unit,
    onDeployPatch: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Studio: ${project.studioName} • Genre: ${project.genre.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Skor Metacritic:", style = MaterialTheme.typography.bodySmall)
                    Text("${project.reviewScore} / 100", fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Platform:", style = MaterialTheme.typography.bodySmall)
                    Text(project.targetPlatforms.joinToString { it.displayName }, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Kopi Terjual:", style = MaterialTheme.typography.bodySmall)
                    Text(formatNumber(project.totalSales), fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Laba Bersih Publisher:", style = MaterialTheme.typography.bodySmall)
                    Text("$${formatMoney(project.publisherLifetimeProfit)}", fontWeight = FontWeight.Bold, color = NeonGreen)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Royalti Pasif Bulanan:", style = MaterialTheme.typography.bodySmall)
                    Text("+$${formatMoney(project.monthlyRevenue)} / bln", fontWeight = FontWeight.Bold, color = GoldAccent)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDeployPatch()
                    onDismiss()
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Rilis Patch ($20k)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

private fun formatNumber(number: Long): String {
    return when {
        number >= 1_000_000 -> String.format("%.1fM", number / 1_000_000.0)
        number >= 1_000 -> String.format("%.1fk", number / 1_000.0)
        else -> number.toString()
    }
}

private fun formatMoney(amount: Long): String {
    return when {
        amount >= 1_000_000 -> String.format("%.2fM", amount / 1_000_000.0)
        amount >= 1_000 -> String.format("%.0fk", amount / 1_000.0)
        else -> amount.toString()
    }
}
