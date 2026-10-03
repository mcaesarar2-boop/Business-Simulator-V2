package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.LanguageManager
import com.example.localization.SupportedLanguage

/**
 * Modular Jetpack Compose component for language selection in the Settings Screen.
 *
 * Adheres to:
 * - ZERO LOGIC ALTERATION: Decoupled from GameViewModel and financial logic.
 * - Material 3 theming with gold accents and dark surface styling.
 * - Reactive locale switching using LanguageManager.
 */
@Composable
fun LanguageSettingsCard(
    modifier: Modifier = Modifier,
    onLanguageChanged: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val currentLocaleCode by LanguageManager.currentLocaleCode.collectAsState()

    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)
    val cardBg = Color(0xFF1E2330)
    val borderColor = Color(0xFF2C3240)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("language_settings_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(gold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = stringResource(R.string.settings_language_title),
                            tint = gold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.settings_language_title),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.settings_language_subtitle),
                            color = textGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = borderColor.copy(alpha = 0.7f), thickness = 0.8.dp)

            // Language Options List (Radio Group Style)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LanguageManager.SUPPORTED_LANGUAGES.forEach { lang ->
                    val isSelected = lang.code == currentLocaleCode
                    LanguageOptionItem(
                        language = lang,
                        isSelected = isSelected,
                        onClick = {
                            if (!isSelected) {
                                LanguageManager.setLanguage(context, lang.code)
                                onLanguageChanged?.invoke(lang.code)
                            }
                        }
                    )
                }
            }

            // Notice about seamless runtime switching
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141720), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = gold.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.settings_language_restart_notice),
                    color = textGray,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun LanguageOptionItem(
    language: SupportedLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val gold = Color(0xFFFFD700)
    val textGray = Color(0xFFA0A0A0)

    val itemBorderColor by animateColorAsState(
        targetValue = if (isSelected) gold else Color(0xFF2C3240),
        animationSpec = tween(durationMillis = 200),
        label = "itemBorderColor"
    )

    val itemBgColor by animateColorAsState(
        targetValue = if (isSelected) gold.copy(alpha = 0.10f) else Color(0xFF161922),
        animationSpec = tween(durationMillis = 200),
        label = "itemBgColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = itemBorderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(itemBgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("language_option_${language.code}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Flag Emoji
            Text(
                text = language.flagEmoji,
                fontSize = 22.sp
            )

            // Name & Native Script
            Column {
                Text(
                    text = language.displayName,
                    color = if (isSelected) gold else Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = language.nativeName,
                    color = textGray,
                    fontSize = 11.sp
                )
            }
        }

        // Badges & Selection Indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // RTL Indicator Badge for Arabic
            if (language.isRtl) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF3B2A1A),
                    border = BorderStroke(0.5.dp, Color(0xFFD97706))
                ) {
                    Text(
                        text = "RTL",
                        color = Color(0xFFFBBF24),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Radio Button / Active checkmark
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(gold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.settings_language_active),
                        tint = Color.Black,
                        modifier = Modifier.size(15.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFF4A5568), CircleShape)
                )
            }
        }
    }
}
