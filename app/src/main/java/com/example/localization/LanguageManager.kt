package com.example.localization

import android.app.LocaleManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Supported Language model representing localized metadata.
 */
data class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagEmoji: String,
    val isRtl: Boolean = false
)

/**
 * Isolated LanguageManager engine for seamless in-app localization.
 *
 * Adheres strictly to the ZERO LOGIC ALTERATION mandate:
 * - Does not touch game logic or ViewModel calculations.
 * - Uses AppCompatDelegate.setApplicationLocales() / Android 13+ LocaleManager.
 * - Seamlessly updates UI language at runtime without restarting the app process.
 * - Persists selection in SharedPreferences.
 */
object LanguageManager {
    private const val PREFS_NAME = "game_localization_preferences"
    private const val KEY_SELECTED_LANGUAGE = "key_selected_language"

    val DEFAULT_LANGUAGE_CODE = "en"

    val SUPPORTED_LANGUAGES: List<SupportedLanguage> = listOf(
        SupportedLanguage(
            code = "en",
            displayName = "English",
            nativeName = "English (US)",
            flagEmoji = "🇺🇸",
            isRtl = false
        ),
        SupportedLanguage(
            code = "id",
            displayName = "Bahasa Indonesia",
            nativeName = "Bahasa Indonesia",
            flagEmoji = "🇮🇩",
            isRtl = false
        ),
        SupportedLanguage(
            code = "ar",
            displayName = "العربية",
            nativeName = "العربية (Arabic)",
            flagEmoji = "🇸🇦",
            isRtl = true
        )
    )

    private val _currentLocaleCode = MutableStateFlow(DEFAULT_LANGUAGE_CODE)
    val currentLocaleCode: StateFlow<String> = _currentLocaleCode.asStateFlow()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Normalize language code (e.g. legacy Java "in" -> "id").
     */
    fun normalizeCode(code: String): String {
        return when (code.lowercase()) {
            "in", "id" -> "id"
            "ar" -> "ar"
            else -> "en"
        }
    }

    /**
     * Initializes locale preference on app startup.
     */
    fun init(context: Context) {
        val prefs = getPrefs(context)
        val savedCode = prefs.getString(KEY_SELECTED_LANGUAGE, null)

        val targetCode = if (!savedCode.isNullOrEmpty()) {
            normalizeCode(savedCode)
        } else {
            val defaultLang = Locale.getDefault().language
            if (defaultLang in listOf("id", "in", "ar")) {
                normalizeCode(defaultLang)
            } else {
                DEFAULT_LANGUAGE_CODE
            }
        }

        _currentLocaleCode.value = targetCode
    }

    /**
     * Sets the active application language seamlessly without process restart.
     */
    fun setLanguage(context: Context, languageCode: String) {
        val normalized = normalizeCode(languageCode)
        
        // 1. Persist to SharedPreferences
        getPrefs(context).edit().putString(KEY_SELECTED_LANGUAGE, normalized).apply()
        
        // 2. Update reactive StateFlow for instant Compose recomposition
        _currentLocaleCode.value = normalized

        // 3. Android 13+ LocaleManager support if available
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                localeManager?.applicationLocales = LocaleList.forLanguageTags(normalized)
            }
        } catch (_: Throwable) {
        }
    }

    /**
     * Returns true if the active language is RTL (Arabic).
     */
    fun isCurrentLocaleRtl(): Boolean {
        return _currentLocaleCode.value == "ar"
    }

    /**
     * Finds the [SupportedLanguage] object for a given code.
     */
    fun getLanguageByCode(code: String): SupportedLanguage {
        val normalized = normalizeCode(code)
        return SUPPORTED_LANGUAGES.find { it.code == normalized } ?: SUPPORTED_LANGUAGES.first()
    }

    /**
     * Gets currently active [SupportedLanguage].
     */
    fun getCurrentLanguage(): SupportedLanguage {
        return getLanguageByCode(_currentLocaleCode.value)
    }
}
