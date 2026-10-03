package com.example.filmstudio.engine

import androidx.compose.ui.graphics.Color
import com.example.data.MovieProject
import com.example.domain.subsystems.creative.FilmTimelineEngine
import com.example.filmstudio.model.FilmMonthDetail
import com.example.filmstudio.model.FilmScheduleStage
import com.example.ui.formatCurrencyRingkas

/**
 * Engine responsible for projecting film studio schedule, active productions,
 * theatrical releases, and historical retrospectives onto any specified calendar month.
 */
object FilmTimelineProjector {

    fun getStudioFilmActivities(
        targetMonth: Int,
        targetYear: Int,
        currentMonth: Int,
        currentYear: Int,
        allFilms: List<MovieProject>,
        isAnimation: Boolean
    ): List<FilmMonthDetail> {
        val result = mutableListOf<FilmMonthDetail>()
        val targetAbs = targetYear * 12 + targetMonth
        val currentAbs = currentYear * 12 + currentMonth
        val isPastMonth = targetAbs < currentAbs

        allFilms.forEach { film ->
            when (film.status) {
                "FINISHED" -> {
                    // Historical / Finished film from catalog
                    val theatricalDuration = film.totalTheatricalMonths ?: if (film.distributionScale == "Global") 4 else 3
                    val finishAbs = if (film.releaseYear != null && film.releaseMonth != null) {
                        film.releaseYear!! * 12 + film.releaseMonth!!
                    } else null

                    val theaterStartAbs = if (film.theaterStartYear != null && film.theaterStartMonth != null) {
                        film.theaterStartYear!! * 12 + film.theaterStartMonth!!
                    } else if (finishAbs != null) {
                        finishAbs - theatricalDuration + 1
                    } else null

                    val theaterEndAbs = finishAbs ?: (theaterStartAbs?.let { it + theatricalDuration - 1 })

                    val prodDuration = maxOf(1, film.productionDelayMonths.let { if (it <= 0) 4 else it })
                    val prodStartAbs = if (film.prodStartYear != null && film.prodStartMonth != null) {
                        film.prodStartYear!! * 12 + film.prodStartMonth!!
                    } else if (theaterStartAbs != null) {
                        theaterStartAbs - prodDuration
                    } else if (finishAbs != null) {
                        finishAbs - theatricalDuration - prodDuration + 1
                    } else null

                    val prodEndAbs = if (theaterStartAbs != null) theaterStartAbs - 1 else (prodStartAbs?.let { it + prodDuration - 1 })

                    if (finishAbs != null && targetAbs == finishAbs) {
                        // Month it finished / wrapped run
                        if (isPastMonth) {
                            result.add(
                                FilmMonthDetail(
                                    film = film,
                                    stage = FilmScheduleStage.PAST_FINISHED,
                                    stageLabel = "📦 Turun Layar (Histori)",
                                    detailText = "Film '${film.title}' resmi menyelesaikan masa tayang bioskop. Box Office: ${formatCurrencyRingkas(film.boxOffice, true)} • Profit: ${formatCurrencyRingkas(film.netProfit, true)}.",
                                    accentColor = Color(0xFF90A4AE),
                                    isPastEvent = true
                                )
                            )
                        } else {
                            result.add(
                                FilmMonthDetail(
                                    film = film,
                                    stage = FilmScheduleStage.JUST_FINISHED,
                                    stageLabel = "📦 Turun Layar & Arsip IP",
                                    detailText = "Film resmi menyelesaikan masa tayang bioskop dan masuk ke Portofolio / Katalog IP.",
                                    accentColor = Color(0xFF9E9E9E),
                                    isPastEvent = false
                                )
                            )
                        }
                    } else if (theaterStartAbs != null && theaterEndAbs != null && targetAbs in theaterStartAbs..theaterEndAbs) {
                        val monthInRun = targetAbs - theaterStartAbs + 1
                        if (isPastMonth) {
                            result.add(
                                FilmMonthDetail(
                                    film = film,
                                    stage = FilmScheduleStage.PAST_THEATERS,
                                    stageLabel = "🎬 Tayang Bioskop (Histori • Bln ke-$monthInRun)",
                                    detailText = "Histori masa tayang film '${film.title}' di jaringan bioskop (${film.distributionScale}). Perolehan total: ${formatCurrencyRingkas(film.boxOffice, true)}.",
                                    accentColor = Color(0xFF78909C),
                                    isPastEvent = true
                                )
                            )
                        } else {
                            result.add(
                                FilmMonthDetail(
                                    film = film,
                                    stage = FilmScheduleStage.IN_THEATERS,
                                    stageLabel = "🎬 Tayang Bioskop (Bulan ke-$monthInRun)",
                                    detailText = "Film '${film.title}' tayang di bioskop. Box Office: ${formatCurrencyRingkas(film.boxOffice, true)}",
                                    accentColor = Color(0xFF4CAF50),
                                    isPastEvent = false
                                )
                            )
                        }
                    } else if (prodStartAbs != null && prodEndAbs != null && targetAbs in prodStartAbs..prodEndAbs) {
                        val monthInProd = targetAbs - prodStartAbs + 1
                        val prodTypeLabel = if (isAnimation) "🎨 Produksi Animasi" else "🎥 Syuting & Produksi"
                        if (isPastMonth) {
                            result.add(
                                FilmMonthDetail(
                                    film = film,
                                    stage = FilmScheduleStage.PAST_PRODUCTION,
                                    stageLabel = "$prodTypeLabel (Histori • Bln ke-$monthInProd)",
                                    detailText = "Histori penggarapan film '${film.title}' (${film.filmFormat}). Budget: ${formatCurrencyRingkas(film.budget, true)}.",
                                    accentColor = Color(0xFF6B7280),
                                    isPastEvent = true
                                )
                            )
                        } else {
                            result.add(
                                FilmMonthDetail(
                                    film = film,
                                    stage = FilmScheduleStage.ACTIVE_PRODUCTION,
                                    stageLabel = prodTypeLabel,
                                    detailText = "Penggarapan film '${film.title}' di studio.",
                                    accentColor = Color(0xFF2979FF),
                                    isPastEvent = false
                                )
                            )
                        }
                    }
                }
                "IN_THEATERS" -> {
                    val totalRemaining = film.remainingMonths
                    val theaterEndAbs = currentAbs + totalRemaining - 1
                    val theatricalDuration = film.totalTheatricalMonths ?: if (film.distributionScale == "Global") 4 else 3
                    val theaterStartAbs = if (film.theaterStartYear != null && film.theaterStartMonth != null) {
                        film.theaterStartYear!! * 12 + film.theaterStartMonth!!
                    } else (theaterEndAbs - theatricalDuration + 1).coerceAtMost(currentAbs)

                    val prodDuration = maxOf(1, film.productionDelayMonths.let { if (it <= 0) 4 else it })
                    val prodStartAbs = if (film.prodStartYear != null && film.prodStartMonth != null) {
                        film.prodStartYear!! * 12 + film.prodStartMonth!!
                    } else (theaterStartAbs - prodDuration)
                    val prodEndAbs = theaterStartAbs - 1

                    if (targetAbs in theaterStartAbs..theaterEndAbs) {
                        if (isPastMonth) {
                            val monthInRun = targetAbs - theaterStartAbs + 1
                            result.add(
                                FilmMonthDetail(
                                    film = film,
                                    stage = FilmScheduleStage.PAST_THEATERS,
                                    stageLabel = "🎬 Tayang Bioskop (Histori • Bln ke-$monthInRun)",
                                    detailText = "Film '${film.title}' tayang di bioskop. Box Office saat ini: ${formatCurrencyRingkas(film.currentRevenue, true)}.",
                                    accentColor = Color(0xFF78909C),
                                    isPastEvent = true
                                )
                            )
                        } else {
                            val diffFromCurrent = targetAbs - currentAbs
                            val monthInRun = targetAbs - theaterStartAbs + 1
                            val monthsLeft = totalRemaining - diffFromCurrent
                            val isClosing = monthsLeft == 1
                            val stage = if (isClosing) FilmScheduleStage.CLOSING_RUN else FilmScheduleStage.IN_THEATERS
                            val label = if (isClosing) "🏁 Menjelang Tutup Layar" else "🎬 Tayang Bioskop (Bulan ke-$monthInRun)"
                            val revText = if (film.currentRevenue > 0) " • Box Office: ${formatCurrencyRingkas(film.currentRevenue, true)}" else ""
                            val desc = if (isClosing) {
                                "Bulan terakhir penayangan di bioskop sebelum resmi turun layar ke katalog IP$revText"
                            } else {
                                "Sedang tayang di jaringan bioskop. Sisa $monthsLeft bulan penayangan$revText"
                            }
                            result.add(
                                FilmMonthDetail(
                                    film = film,
                                    stage = stage,
                                    stageLabel = label,
                                    detailText = desc,
                                    accentColor = if (isClosing) Color(0xFFFF9800) else Color(0xFF4CAF50),
                                    isPastEvent = false
                                )
                            )
                        }
                    } else if (targetAbs in prodStartAbs..prodEndAbs) {
                        val monthInProd = targetAbs - prodStartAbs + 1
                        val prodTypeLabel = if (isAnimation) "🎨 Produksi Animasi" else "🎥 Syuting & Produksi"
                        result.add(
                            FilmMonthDetail(
                                film = film,
                                stage = FilmScheduleStage.PAST_PRODUCTION,
                                stageLabel = "$prodTypeLabel (Histori • Bln ke-$monthInProd)",
                                detailText = "Histori masa pengerjaan film '${film.title}'. Budget: ${formatCurrencyRingkas(film.budget, true)}.",
                                accentColor = Color(0xFF6B7280),
                                isPastEvent = true
                            )
                        )
                    }
                }
                "IN_PRODUCTION" -> {
                    if (film.productionPhase == "ANTREAN") {
                        val sM = film.scheduledMonth
                        val sY = film.scheduledYear
                        if (sM != null && sY != null) {
                            val schedAbs = sY * 12 + sM
                            val diffFromSched = targetAbs - schedAbs
                            if (targetAbs == schedAbs) {
                                result.add(
                                    FilmMonthDetail(
                                        film = film,
                                        stage = FilmScheduleStage.PLANNED_START,
                                        stageLabel = "⏳ Rencana Mulai Produksi",
                                        detailText = "Bulan rencana dimulainya garapan film '${film.title}' (${film.filmFormat}).",
                                        accentColor = Color(0xFFFFD54F),
                                        isPastEvent = isPastMonth
                                    )
                                )
                            } else if (diffFromSched > 0) {
                                val estDuration = maxOf(1, film.productionDelayMonths)
                                if (diffFromSched <= estDuration) {
                                    val ratio = diffFromSched.toDouble() / estDuration.toDouble()
                                    val (stage, label, color) = when {
                                        ratio < 0.25 -> Triple(FilmScheduleStage.PRE_PRODUCTION, "📝 Pra-Produksi", Color(0xFF00E5FF))
                                        ratio < 0.80 -> Triple(FilmScheduleStage.ACTIVE_PRODUCTION, if (isAnimation) "🎨 Produksi Animasi" else "🎥 Syuting Utama", Color(0xFF2979FF))
                                        ratio < 1.0 -> Triple(FilmScheduleStage.POST_PRODUCTION, "💻 Pasca-Produksi", Color(0xFF7C4DFF))
                                        else -> Triple(FilmScheduleStage.QUALITY_CONTROL, "🔍 Quality Control (QC)", Color(0xFFFF4081))
                                    }
                                    val monthsLeft = estDuration - diffFromSched
                                    result.add(
                                        FilmMonthDetail(
                                            film = film,
                                            stage = stage,
                                            stageLabel = label,
                                            detailText = "Proyeksi studio setelah jadwal rilis. Sisa perkiraan ~$monthsLeft bulan.",
                                            accentColor = color,
                                            isPastEvent = isPastMonth
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        val diffFromCurrent = targetAbs - currentAbs
                        val prodStartAbs = if (film.prodStartYear != null && film.prodStartMonth != null) {
                            film.prodStartYear!! * 12 + film.prodStartMonth!!
                        } else (currentAbs - 2).coerceAtMost(currentAbs)

                        if (targetAbs in prodStartAbs until currentAbs) {
                            val monthInProd = targetAbs - prodStartAbs + 1
                            val prodTypeLabel = if (isAnimation) "🎨 Produksi Animasi" else "🎥 Syuting & Produksi"
                            result.add(
                                FilmMonthDetail(
                                    film = film,
                                    stage = FilmScheduleStage.PAST_PRODUCTION,
                                    stageLabel = "$prodTypeLabel (Histori • Bln ke-$monthInProd)",
                                    detailText = "Histori pengerjaan awal film '${film.title}'.",
                                    accentColor = Color(0xFF6B7280),
                                    isPastEvent = true
                                )
                            )
                        } else if (diffFromCurrent == 0) {
                            val totalMonths = if (film.totalProductionMonths > 0) film.totalProductionMonths else maxOf(film.productionDelayMonths, 1)
                            val snapshot = FilmTimelineEngine.getProgress(isAnimation, totalMonths, film.productionDelayMonths)
                            val (stage, label, color) = when {
                                film.isAwaitingRelease -> Triple(FilmScheduleStage.AWAITING_RELEASE, "⏳ Menunggu Tanggal Tayang (${film.scheduledReleaseDate ?: "?"})", Color(0xFFFFB300))
                                film.isQcPhase -> Triple(FilmScheduleStage.QUALITY_CONTROL, "🔍 Quality Control (Skor ${film.internalScore ?: 0}/100)", Color(0xFFFF4081))
                                film.productionPhase == "ANTREAN" -> Triple(FilmScheduleStage.PRE_PRODUCTION, "⏳ Menunggu Jadwal Rilis", Color(0xFFFFA000))
                                snapshot.universalPhase.phaseNumber <= 6 -> Triple(FilmScheduleStage.PRE_PRODUCTION, snapshot.displayLabel, Color(0xFF00E5FF))
                                snapshot.universalPhase.phaseNumber <= 8 -> Triple(FilmScheduleStage.ACTIVE_PRODUCTION, snapshot.displayLabel, Color(0xFF2979FF))
                                else -> Triple(FilmScheduleStage.POST_PRODUCTION, snapshot.displayLabel, Color(0xFF7C4DFF))
                            }
                            result.add(
                                FilmMonthDetail(
                                    film = film,
                                    stage = stage,
                                    stageLabel = label,
                                    detailText = if (film.isQcPhase) "Film rampung, tahap review internal atau menunggu tanggal rilis." else "${snapshot.phaseDetailText} | Sisa ${film.productionDelayMonths} bulan (${snapshot.progressPercent}%).",
                                    accentColor = color,
                                    isPastEvent = false
                                )
                            )
                        } else if (diffFromCurrent > 0) {
                            if (diffFromCurrent <= film.productionDelayMonths) {
                                val remainingAtTarget = film.productionDelayMonths - diffFromCurrent
                                val totalEst = if (film.totalProductionMonths > 0) film.totalProductionMonths else maxOf(film.productionDelayMonths, 4)
                                val projSnapshot = FilmTimelineEngine.getProgress(isAnimation, totalEst, remainingAtTarget)
                                val (stage, color) = when {
                                    remainingAtTarget == 0 -> Pair(FilmScheduleStage.QUALITY_CONTROL, Color(0xFFFF4081))
                                    projSnapshot.universalPhase.phaseNumber <= 6 -> Pair(FilmScheduleStage.PRE_PRODUCTION, Color(0xFF00E5FF))
                                    projSnapshot.universalPhase.phaseNumber <= 8 -> Pair(FilmScheduleStage.ACTIVE_PRODUCTION, Color(0xFF2979FF))
                                    else -> Pair(FilmScheduleStage.POST_PRODUCTION, Color(0xFF7C4DFF))
                                }
                                result.add(
                                    FilmMonthDetail(
                                        film = film,
                                        stage = stage,
                                        stageLabel = if (remainingAtTarget == 0) "🔍 Quality Control (QC)" else projSnapshot.displayLabel,
                                        detailText = "${projSnapshot.phaseDetailText} | Sisa pengerjaan $remainingAtTarget bulan (${projSnapshot.progressPercent}% selesai).",
                                        accentColor = color,
                                        isPastEvent = false
                                    )
                                )
                            }
                        }
                    }

                    // Cek target tanggal rilis bioskop yang telah dijadwalkan
                    if (film.scheduledReleaseDate != null) {
                        val parts = film.scheduledReleaseDate.split("/")
                        if (parts.size >= 2) {
                            val sM = parts[0].toIntOrNull()
                            val sY = parts[1].toIntOrNull()
                            val normalizedYear = if (sY != null && sY >= 2019) sY - 2019 else sY
                            if (sM != null && normalizedYear != null && targetMonth == sM && (targetYear == normalizedYear || targetYear == sY)) {
                                if (result.none { it.film.title == film.title && it.stage == FilmScheduleStage.IN_THEATERS }) {
                                    result.add(
                                        FilmMonthDetail(
                                            film = film,
                                            stage = FilmScheduleStage.IN_THEATERS,
                                            stageLabel = "🎬 Target Tayang: ${film.title}",
                                            detailText = "Jadwal resmi tayang perdana bioskop untuk film '${film.title}' (${film.filmFormat}).",
                                            accentColor = Color(0xFF4CAF50),
                                            isPastEvent = isPastMonth
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        return result
    }
}
