package com.caesar.gametycoon.engine.fix

import com.example.data.MovieProject

/**
 * Audit Fix: FilmProductionFixEngine
 *
 * Resolves the premature movie release bug in FilmProductionEngine.
 *
 * ROOT CAUSE FIXED:
 * Legacy code contained `val normalizedYear = if (sY >= 2019) sY - 2019 else sY`.
 * When a movie was scheduled for 2027, `normalizedYear` became 8.
 * The engine compared `event.currentYear (2026) > normalizedYear (8)`,
 * which always evaluated to TRUE, immediately pushing all future films
 * into theaters on the very first tick.
 *
 * CORRECTED SPECIFICATION:
 * Calendar years are preserved as absolute years (e.g. 2026, 2027, 2030).
 * Accurate comparison: (currentYear > targetYear) || (currentYear == targetYear && currentMonth >= targetMonth).
 */
object FilmProductionFixEngine {

    data class ScheduledReleaseDate(
        val month: Int,
        val year: Int
    )

    /**
     * Parses a release date string in "MM/YYYY" or "M/YYYY" format.
     * Accurately normalizes any vintage relative save offsets without corrupting absolute calendar years.
     */
    fun parseReleaseDate(dateString: String?): ScheduledReleaseDate? {
        if (dateString.isNullOrBlank()) return null
        val parts = dateString.split("/")
        if (parts.size < 2) return null

        val month = parts[0].trim().toIntOrNull()?.coerceIn(1, 12) ?: return null
        val rawYear = parts[1].trim().toIntOrNull() ?: return null

        // If a vintage legacy save stored year as relative counter (< 100, e.g. Year 8),
        // translate it to the modern calendar baseline (2019 + 8 = 2027).
        // Otherwise preserve the absolute year (e.g. 2026, 2027) as-is.
        val canonicalYear = if (rawYear < 100) 2019 + rawYear else rawYear

        return ScheduledReleaseDate(month = month, year = canonicalYear)
    }

    /**
     * Checks if a movie scheduled in the release queue is ready for theatrical release.
     */
    fun isMovieReadyForRelease(
        project: MovieProject,
        currentMonth: Int,
        currentYear: Int
    ): Boolean {
        return isReadyForTheatricalRelease(project.scheduledReleaseDate, currentMonth, currentYear)
    }

    /**
     * Checks if a movie scheduled in the release queue is ready for theatrical release.
     */
    fun isReadyForTheatricalRelease(
        scheduledReleaseDateStr: String?,
        currentMonth: Int,
        currentYear: Int
    ): Boolean {
        val parsed = parseReleaseDate(scheduledReleaseDateStr) ?: return true
        return isDateReached(
            targetMonth = parsed.month,
            targetYear = parsed.year,
            currentMonth = currentMonth,
            currentYear = currentYear
        )
    }

    /**
     * Checks if a queued project in the production backlog should start pre-production.
     */
    fun shouldStartProduction(
        scheduledMonth: Int?,
        scheduledYear: Int?,
        currentMonth: Int,
        currentYear: Int
    ): Boolean {
        if (scheduledMonth == null || scheduledYear == null) return true

        val canonicalYear = if (scheduledYear < 100) 2019 + scheduledYear else scheduledYear
        return isDateReached(
            targetMonth = scheduledMonth.coerceIn(1, 12),
            targetYear = canonicalYear,
            currentMonth = currentMonth,
            currentYear = currentYear
        )
    }

    /**
     * Exact calendar timeline comparison.
     */
    fun isDateReached(
        targetMonth: Int,
        targetYear: Int,
        currentMonth: Int,
        currentYear: Int
    ): Boolean {
        return currentYear > targetYear || (currentYear == targetYear && currentMonth >= targetMonth)
    }

    /**
     * Formats month and year into the canonical "MM/YYYY" representation.
     */
    fun formatReleaseDate(month: Int, year: Int): String {
        val safeMonth = month.coerceIn(1, 12)
        val safeYear = if (year < 100) 2019 + year else year
        return "$safeMonth/$safeYear"
    }

    /**
     * Calculates remaining months until theatrical release.
     * Returns 0 if release date has already passed or is current.
     */
    fun calculateMonthsUntilRelease(
        scheduledReleaseDateStr: String?,
        currentMonth: Int,
        currentYear: Int
    ): Int {
        val parsed = parseReleaseDate(scheduledReleaseDateStr) ?: return 0
        val targetMonthsTotal = (parsed.year * 12) + parsed.month
        val currentMonthsTotal = (currentYear * 12) + currentMonth
        return (targetMonthsTotal - currentMonthsTotal).coerceAtLeast(0)
    }
}
