package com.example.filmstudio.model

import androidx.compose.ui.graphics.Color
import com.example.data.MovieProject

/**
 * Stages in a film's lifecycle for calendar and slate visualization.
 */
enum class FilmScheduleStage {
    PLANNED_START,       // ⏳ Rencana Mulai Produksi
    PRE_PRODUCTION,      // 📝 Pra-Produksi
    ACTIVE_PRODUCTION,   // 🎥 Syuting / 🎨 Animasi
    POST_PRODUCTION,     // 💻 Pasca-Produksi
    QUALITY_CONTROL,     // 🔍 Quality Control (QC)
    AWAITING_RELEASE,    // ⏳ Menunggu Rilis
    IN_THEATERS,         // 🎬 Tayang di Bioskop
    CLOSING_RUN,         // 🏁 Menjelang Tutup Layar
    JUST_FINISHED,       // 📦 Selesai / Turun Layar (Bulan Ini)
    PAST_PRODUCTION,     // 🎞️ Histori Masa Produksi (Abu-abu / Pudar)
    PAST_THEATERS,       // 🎬 Histori Tayang Bioskop (Abu-abu / Pudar)
    PAST_FINISHED        // 📦 Histori Turun Layar & Arsip IP (Abu-abu / Pudar)
}

/**
 * Detail activity projection of a film in a specific calendar month.
 */
data class FilmMonthDetail(
    val film: MovieProject,
    val stage: FilmScheduleStage,
    val stageLabel: String,
    val detailText: String,
    val accentColor: Color,
    val isPastEvent: Boolean = false
)

/**
 * Production approaches affecting budget, duration, and score volatility.
 */
enum class FilmProductionFocus(
    val id: String,
    val title: String,
    val description: String,
    val budgetMultiplier: Double,
    val extraMonths: Int
) {
    REGULER("REGULER", "Reguler", "Standar", 1.0, 0),
    KUALITAS("KUALITAS", "Fokus Kualitas", "Budget +30%, Durasi +6Bln", 1.3, 6),
    MAHAKARYA("MAHAKARYA", "Ambisi Mahakarya", "Budget +80%, Durasi +12Bln", 1.8, 12);

    companion object {
        fun fromId(id: String?): FilmProductionFocus =
            values().find { it.id.equals(id, ignoreCase = true) } ?: REGULER
    }
}

/**
 * Available formats for film production.
 */
object FilmFormats {
    const val FEATURE_FILM = "Feature Film"
    const val SHORT_FILM = "Short Film"
    val ALL = listOf(SHORT_FILM, FEATURE_FILM)
}

/**
 * Distribution scale options.
 */
object FilmDistributionScales {
    const val LOCAL = "Local"
    const val GLOBAL = "Global"
}

/**
 * Genre catalog for film studio projects.
 */
object FilmGenres {
    val ALL = listOf(
        "Action", "Drama", "Comedy", "Horror", "Sci-Fi",
        "Romance", "Thriller", "Adventure", "Fantasy",
        "Documentary", "Animation"
    )
}

/**
 * Box office analyst pitch projection.
 */
data class FilmPitchProjection(
    val totalInvestment: Long,
    val productionBudget: Long,
    val promoBudget: Long,
    val minBoxOffice: Long,
    val maxBoxOffice: Long,
    val riskLevel: String
) {
    val estBoxOfficeMin: Long get() = minBoxOffice
    val estBoxOfficeMax: Long get() = maxBoxOffice
}
