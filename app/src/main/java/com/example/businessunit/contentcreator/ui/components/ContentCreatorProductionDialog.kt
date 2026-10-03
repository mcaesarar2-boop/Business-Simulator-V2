package com.example.businessunit.contentcreator.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessunit.contentcreator.data.ContentCreatorRepository
import com.example.businessunit.contentcreator.model.CoProductionFundingScheme
import com.example.businessunit.contentcreator.model.ContentType
import com.example.businessunit.contentcreator.model.CreativeFocus
import com.example.data.OwnedBusiness
import com.example.ui.formatCurrencyRingkas
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentCreatorProductionDialog(
    creatorCash: Long,
    subscribers: Long,
    filmStudios: List<OwnedBusiness>,
    onDismiss: () -> Unit,
    onStartProduction: (
        title: String,
        type: ContentType,
        budget: Long,
        studioId: String?,
        fundingScheme: CoProductionFundingScheme,
        creativeFocus: CreativeFocus
    ) -> Unit
) {
    val subsFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val hasFilmStudio = filmStudios.isNotEmpty()
    val isSynergyUnlocked = subscribers >= ContentCreatorRepository.MIN_SUBS_STUDIO_SYNERGY && hasFilmStudio

    var isCoProductionMode by remember { mutableStateOf(isSynergyUnlocked) }
    var selectedType by remember { mutableStateOf(ContentType.SHORT_FILM) }
    var selectedFocus by remember { mutableStateOf(CreativeFocus.STORY_NARRATIVE) }
    var titleInput by remember { mutableStateOf(ContentCreatorRepository.getRandomIdea(ContentType.SHORT_FILM)) }
    var budgetInput by remember { mutableStateOf("50000") }
    var budgetError by remember { mutableStateOf<String?>(null) }
    var selectedStudioId by remember { mutableStateOf(filmStudios.firstOrNull()?.instanceId) }
    var selectedFundingScheme by remember { mutableStateOf(CoProductionFundingScheme.JOINT_VENTURE_50_50) }

    val parsedBudget = budgetInput.toLongOrNull() ?: 0L
    val scrollState = rememberScrollState()

    val selectedStudio = filmStudios.firstOrNull { it.instanceId == selectedStudioId }
    val creatorShareCost = if (isCoProductionMode) (parsedBudget * selectedFundingScheme.creatorRatio).toLong() else parsedBudget
    val studioShareCost = if (isCoProductionMode) (parsedBudget * selectedFundingScheme.studioRatio).toLong() else 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ContentCreatorTheme.BgDark,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ContentCreatorTheme.AccentMagenta.copy(alpha = 0.2f))
                        .border(1.dp, ContentCreatorTheme.AccentMagenta, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Movie, contentDescription = null, tint = ContentCreatorTheme.AccentMagenta, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Produksi Karya Original Baru",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ContentCreatorTheme.TextWhite
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Rilis karya original untuk monetisasi lisensi Production House, atau kolaborasi co-produksi bersama Studio Film milik holding.",
                    fontSize = 12.sp,
                    color = ContentCreatorTheme.TextGray,
                    lineHeight = 16.sp
                )

                // 1. MODE PRODUKSI (INDIE vs CO-PRODUCTION)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("MODE PRODUKSI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.TextGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { isCoProductionMode = false },
                            shape = RoundedCornerShape(10.dp),
                            color = if (!isCoProductionMode) ContentCreatorTheme.AccentMagenta.copy(alpha = 0.3f) else ContentCreatorTheme.CardDark,
                            border = BorderStroke(1.dp, if (!isCoProductionMode) ContentCreatorTheme.AccentMagenta else Color.White.copy(alpha = 0.1f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = if (!isCoProductionMode) ContentCreatorTheme.AccentMagenta else ContentCreatorTheme.TextGray, modifier = Modifier.size(16.dp))
                                Column {
                                    Text("Produksi Indie", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!isCoProductionMode) ContentCreatorTheme.TextWhite else ContentCreatorTheme.TextGray)
                                    Text("Hanya Kreator (Max $50k)", fontSize = 9.sp, color = ContentCreatorTheme.TextGray)
                                }
                            }
                        }

                        Surface(
                            onClick = {
                                if (isSynergyUnlocked) {
                                    isCoProductionMode = true
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCoProductionMode) ContentCreatorTheme.NeonGreen.copy(alpha = 0.2f) else ContentCreatorTheme.CardDark,
                            border = BorderStroke(1.dp, if (isCoProductionMode) ContentCreatorTheme.NeonGreen else if (isSynergyUnlocked) Color.White.copy(alpha = 0.2f) else ContentCreatorTheme.ErrorRed.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Handshake, contentDescription = null, tint = if (isCoProductionMode) ContentCreatorTheme.NeonGreen else if (isSynergyUnlocked) ContentCreatorTheme.TextGray else ContentCreatorTheme.ErrorRed, modifier = Modifier.size(16.dp))
                                Column {
                                    Text("Co-Produksi Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isCoProductionMode) ContentCreatorTheme.NeonGreen else ContentCreatorTheme.TextGray)
                                    Text(if (isSynergyUnlocked) "Sinergi 100k Subs ✅" else "Butuh 100k Subs & Studio 🔒", fontSize = 9.sp, color = if (isSynergyUnlocked) ContentCreatorTheme.NeonGreen else ContentCreatorTheme.Gold)
                                }
                            }
                        }
                    }
                }

                // 2. TIPE KARYA
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("TIPE KARYA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.TextGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ContentType.values().forEach { type ->
                            val isSelected = selectedType == type
                            Surface(
                                onClick = {
                                    selectedType = type
                                    titleInput = ContentCreatorRepository.getRandomIdea(type)
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) ContentCreatorTheme.AccentMagenta else ContentCreatorTheme.CardDark,
                                border = BorderStroke(1.dp, if (isSelected) ContentCreatorTheme.AccentMagenta else Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = when (type) {
                                            ContentType.FEATURE_FILM -> "🎥"
                                            ContentType.SHORT_FILM -> "🎬"
                                            ContentType.DEEP_DIVE_ESSAY -> "🧠"
                                            ContentType.DOCUMENTARY -> "🌍"
                                        },
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = type.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else ContentCreatorTheme.TextGray,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. FOKUS KREATIF UTAMA
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("FOKUS KREATIF UTAMA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.TextGray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CreativeFocus.values().forEach { focus ->
                            val isSelected = selectedFocus == focus
                            Surface(
                                onClick = { selectedFocus = focus },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) ContentCreatorTheme.AccentMagenta else ContentCreatorTheme.CardDark,
                                border = BorderStroke(1.dp, if (isSelected) ContentCreatorTheme.AccentMagenta else Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(focus.icon, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = focus.displayName.split("&")[0].trim(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else ContentCreatorTheme.TextGray,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "${selectedFocus.icon} ${selectedFocus.displayName}: ${selectedFocus.desc}",
                        fontSize = 10.sp,
                        color = ContentCreatorTheme.Gold,
                        lineHeight = 13.sp
                    )
                }

                // 4. JUDUL KARYA
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("JUDUL KARYA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.TextGray)
                        Row(
                            modifier = Modifier.clickable { titleInput = ContentCreatorRepository.getRandomIdea(selectedType) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = ContentCreatorTheme.Gold, modifier = Modifier.size(14.dp))
                            Text("Acak Ide", fontSize = 11.sp, color = ContentCreatorTheme.Gold, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        placeholder = { Text("Misal: Sinema Noir: Jakarta 2045", color = ContentCreatorTheme.TextGray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ContentCreatorTheme.TextWhite,
                            unfocusedTextColor = ContentCreatorTheme.TextWhite,
                            focusedBorderColor = ContentCreatorTheme.AccentMagenta,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 5. CO-PRODUCTION PARTNER & FUNDING SCHEME (IF IN CO-PROD MODE)
                if (isCoProductionMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("STUDIO FILM PARTNER & PEMBAGIAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ContentCreatorTheme.NeonGreen)

                        // Studio Selector
                        if (filmStudios.size > 1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                filmStudios.forEach { studio ->
                                    val isSelected = selectedStudioId == studio.instanceId
                                    Surface(
                                        onClick = { selectedStudioId = studio.instanceId },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) ContentCreatorTheme.NeonGreen.copy(alpha = 0.2f) else ContentCreatorTheme.CardDark,
                                        border = BorderStroke(1.dp, if (isSelected) ContentCreatorTheme.NeonGreen else Color.White.copy(alpha = 0.1f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = studio.customName ?: studio.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) ContentCreatorTheme.NeonGreen else ContentCreatorTheme.TextWhite,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }
                            }
                        } else if (selectedStudio != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ContentCreatorTheme.CardDark)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Business, contentDescription = null, tint = ContentCreatorTheme.NeonGreen, modifier = Modifier.size(16.dp))
                                Text("Partner: ${selectedStudio.customName ?: selectedStudio.name} (Kas: ${formatCurrencyRingkas(selectedStudio.companyCash.toLong(), false)})", fontSize = 11.sp, color = ContentCreatorTheme.TextWhite)
                            }
                        }

                        // Funding Schemes (50/50, 70/30, 30/70, 100% Creator, 100% Studio)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            CoProductionFundingScheme.values().forEach { scheme ->
                                val isSelected = selectedFundingScheme == scheme
                                Surface(
                                    onClick = { selectedFundingScheme = scheme },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) ContentCreatorTheme.NeonGreen.copy(alpha = 0.2f) else ContentCreatorTheme.CardDark,
                                    border = BorderStroke(1.dp, if (isSelected) ContentCreatorTheme.NeonGreen else Color.White.copy(alpha = 0.08f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(scheme.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) ContentCreatorTheme.NeonGreen else ContentCreatorTheme.TextWhite)
                                            Text(scheme.shortDesc, fontSize = 9.sp, color = ContentCreatorTheme.TextGray)
                                        }
                                        Text(
                                            text = if (scheme.creatorRatio >= 0.5) "📁 Bank Konten" else "🎞️ Katalog Film",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (scheme.creatorRatio >= 0.5) ContentCreatorTheme.AccentMagenta else ContentCreatorTheme.Gold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. BUDGET PRODUKSI & DETAIL ALOKASI
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isCoProductionMode) "TOTAL BUDGET PRODUKSI" else "BUDGET PRODUKSI (INDIE MAX $50K)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ContentCreatorTheme.TextGray
                        )
                        Text(
                            text = "Kas Kreator: ${formatCurrencyRingkas(creatorCash, false)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ContentCreatorTheme.NeonGreen
                        )
                    }

                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = {
                            budgetInput = it.filter { ch -> ch.isDigit() }
                            budgetError = null
                        },
                        label = { Text("Nominal Budget Total ($)", fontSize = 11.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ContentCreatorTheme.TextWhite,
                            unfocusedTextColor = ContentCreatorTheme.TextWhite,
                            focusedBorderColor = ContentCreatorTheme.AccentMagenta,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Budget presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val presets = if (isCoProductionMode) {
                            listOf(25_000L, 50_000L, 100_000L, 250_000L)
                        } else {
                            listOf(10_000L, 25_000L, 50_000L)
                        }
                        presets.forEach { preset ->
                            val isSelected = parsedBudget == preset
                            Surface(
                                onClick = { budgetInput = preset.toString(); budgetError = null },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) ContentCreatorTheme.AccentMagenta.copy(alpha = 0.3f) else ContentCreatorTheme.CardDark,
                                border = BorderStroke(1.dp, if (isSelected) ContentCreatorTheme.AccentMagenta else Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = formatCurrencyRingkas(preset, true),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) ContentCreatorTheme.AccentMagenta else ContentCreatorTheme.TextWhite,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Cost Breakdown if in Co-Production
                    if (isCoProductionMode && parsedBudget > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ContentCreatorTheme.CardDark)
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Beban Kreator: ${formatCurrencyRingkas(creatorShareCost, false)}", fontSize = 10.sp, color = ContentCreatorTheme.AccentMagenta, fontWeight = FontWeight.Bold)
                            Text("Beban Studio: ${formatCurrencyRingkas(studioShareCost, false)}", fontSize = 10.sp, color = ContentCreatorTheme.NeonGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    budgetError?.let {
                        Text(text = it, color = ContentCreatorTheme.ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleInput.isBlank()) {
                        budgetError = "Judul karya tidak boleh kosong!"
                        return@Button
                    }
                    if (parsedBudget <= 0) {
                        budgetError = "Budget produksi harus lebih besar dari $0!"
                        return@Button
                    }
                    if (!isCoProductionMode && parsedBudget > ContentCreatorRepository.MAX_INDIE_BUDGET) {
                        budgetError = "Status Indie: Budget maksimal dibatasi $50,000! (Gunakan Co-Produksi Studio Film untuk budget > $50k)"
                        return@Button
                    }
                    if (creatorCash < creatorShareCost) {
                        budgetError = "Kas kreator tidak mencukupi untuk porsinya (${formatCurrencyRingkas(creatorShareCost, false)})!"
                        return@Button
                    }
                    if (isCoProductionMode && selectedStudio != null && selectedStudio.companyCash < studioShareCost) {
                        budgetError = "Kas Studio Film tidak mencukupi untuk porsinya (${formatCurrencyRingkas(studioShareCost, false)})!"
                        return@Button
                    }

                    onStartProduction(
                        titleInput,
                        selectedType,
                        parsedBudget,
                        if (isCoProductionMode) selectedStudioId else null,
                        if (isCoProductionMode) selectedFundingScheme else CoProductionFundingScheme.FULL_CREATOR,
                        selectedFocus
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = ContentCreatorTheme.AccentMagenta),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mulai Produksi", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = ContentCreatorTheme.TextGray)
            }
        }
    )
}
