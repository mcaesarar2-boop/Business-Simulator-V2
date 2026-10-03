package com.example.domain.subsystems.creative

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.MovieProject
import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Domain subsystem engine for the Film Production Company (Studio Perfilman).
 * Handles:
 * 1. Production timeline progression (Antrean -> Pra-Produksi -> Produksi Utama -> Pasca-Produksi -> QC).
 * 2. Theatrical box office run (Tayang di bioskop 4-6 bulan dengan akumulasi tiket bulanan).
 * 3. IP library management & OTT/Broadcaster licensing fees collection.
 * 4. Studio operating costs and infrastructure maintenance.
 */
class FilmProductionEngine : MonthlyTickSubscriber {

    override val subscriberId: String = "media_production"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        var totalGrossRevenue = 0L
        var totalUpkeep = 0L
        val logMessages = mutableListOf<String>()
        val policyRepo = com.example.corporate.repository.CorporatePolicyRepository.getInstance()
        val distributionResults = mutableListOf<com.example.corporate.model.CashDistributionResult>()

        val updatedOwned = currentState.ownedBusinesses.map { biz ->
            if (biz.catalogId == "media_production") {
                val (studioResult, gross, upkeep, logs) = processStudioMonthly(biz, event)
                totalGrossRevenue += gross
                totalUpkeep += upkeep
                logMessages.addAll(logs)

                val netProfit = gross - upkeep
                val policy = policyRepo.getStandalonePolicy(biz.instanceId)
                val (updatedBiz, result) = com.example.corporate.engine.CashFlowDistributionEngine.processStandaloneBusinessCashFlow(
                    business = studioResult.copy(companyCash = biz.companyCash),
                    netProfit = netProfit,
                    policy = policy
                )
                distributionResults.add(result)
                updatedBiz
            } else biz
        }

        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            var changed = false
            var currentTreasuryCash = holding.holdingCash
            val holdingPolicy = policyRepo.getHoldingPolicy(holding.instanceId)
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.catalogId == "media_production") {
                    changed = true
                    val (studioResult, gross, upkeep, logs) = processStudioMonthly(sub, event)
                    totalGrossRevenue += gross
                    totalUpkeep += upkeep
                    logMessages.addAll(logs)

                    val netProfit = gross - upkeep
                    val (updatedSub, treasuryDelta, result) = com.example.corporate.engine.CashFlowDistributionEngine.processMergedSubsidiaryCashFlow(
                        subsidiary = studioResult.copy(companyCash = sub.companyCash),
                        parentHolding = holding.copy(holdingCash = currentTreasuryCash),
                        netProfit = netProfit,
                        policy = holdingPolicy
                    )
                    currentTreasuryCash = (currentTreasuryCash + treasuryDelta).coerceAtLeast(0.0)
                    distributionResults.add(result)
                    updatedSub
                } else sub
            }
            if (changed) holding.copy(subsidiaries = newSubs, holdingCash = currentTreasuryCash) else holding
        }

        if (distributionResults.isNotEmpty()) {
            policyRepo.recordDistributionResults(distributionResults)
        }

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = totalGrossRevenue,
            expenses = totalUpkeep,
            dividendToGlobal = 0L,
            logMessages = logMessages,
            stateModifier = { state ->
                state.copy(
                    ownedBusinesses = updatedOwned,
                    holdingCompanies = updatedHoldings
                )
            }
        )
    }

    private fun processStudioMonthly(
        business: OwnedBusiness,
        event: MonthTickEvent
    ): StudioMonthlyResult {
        val upkeep = business.calculateTotalExpenses()
        var monthlyGross = 0L
        val logs = mutableListOf<String>()
        val isAnim = business.studioType == "ANIMATION"

        val updatedProjects = business.projectHistory.map { project ->
            when (project.status) {
                "IN_PRODUCTION" -> {
                    // Cek jika film masih di antrean jadwal rilis mendatang
                    if (project.productionPhase == "ANTREAN") {
                        val schedMonth = project.scheduledMonth
                        val schedYear = project.scheduledYear
                        val shouldStart = if (schedMonth != null && schedYear != null) {
                            event.currentYear > schedYear || (event.currentYear == schedYear && event.currentMonth >= schedMonth)
                        } else true

                        if (shouldStart) {
                            val totalMonths = if (project.totalProductionMonths > 0) project.totalProductionMonths else maxOf(project.productionDelayMonths, 1)
                            val snapshot = FilmTimelineEngine.getProgress(
                                isAnimation = isAnim,
                                totalMonths = totalMonths,
                                remainingMonths = project.productionDelayMonths
                            )
                            logs.add("🎬 [${project.title}] Produksi resmi dimulai! Tahap awal: ${snapshot.displayLabel}")
                            project.copy(
                                productionPhase = snapshot.displayLabel,
                                prodStartMonth = event.currentMonth,
                                prodStartYear = event.currentYear,
                                totalProductionMonths = totalMonths
                            )
                        } else {
                            project // Tetap dalam antrean jadwal
                        }
                    } else if (project.isAwaitingRelease) {
                        // Film sudah selesai produksi & QC, menunggu jadwal rilis bioskop yang telah ditentukan
                        val relParts = project.scheduledReleaseDate?.split("/")
                        val sM = relParts?.getOrNull(0)?.toIntOrNull()
                        val sY = relParts?.getOrNull(1)?.toIntOrNull()
                        val normalizedYear = if (sY != null && sY >= 2019) sY - 2019 else sY
                        val isReadyToRelease = if (sM != null && normalizedYear != null) {
                            event.currentYear > normalizedYear || (event.currentYear == normalizedYear && event.currentMonth >= sM)
                        } else true

                        if (isReadyToRelease) {
                            val totalTheatrical = project.totalTheatricalMonths ?: if (project.distributionScale == "Global") 5 else 3
                            val month1Revenue = calculateOpeningMonthRevenue(project, totalTheatrical)
                            monthlyGross += month1Revenue
                            logs.add("🍿 [${project.title}] Tiba tanggal rilis! Resmi TAYANG di Bioskop (${project.distributionScale})! Box Office Pembuka (Bln 1): +${com.example.ui.formatCurrencyRingkas(month1Revenue, false)}")

                            project.copy(
                                status = "IN_THEATERS",
                                isAwaitingRelease = false,
                                isQcPhase = false,
                                productionPhase = "TAYANG",
                                productionDelayMonths = 0,
                                remainingMonths = totalTheatrical - 1,
                                totalTheatricalMonths = totalTheatrical,
                                theaterStartMonth = event.currentMonth,
                                theaterStartYear = event.currentYear,
                                currentRevenue = month1Revenue,
                                lastMonthRevenue = month1Revenue
                            )
                        } else {
                            project // Tetap menunggu bulan rilis
                        }
                    } else if (project.isQcPhase) {
                        project // Tetap dalam fase QC menunggu aksi pemain (poles / jadwalkan)
                    } else {
                        // Sedang dalam fase produksi aktif
                        val newDelay = (project.productionDelayMonths - 1).coerceAtLeast(0)
                        if (newDelay == 0) {
                            // Produksi rampung dan melewati fase QC
                            if (project.scheduledReleaseDate != null) {
                                val relParts = project.scheduledReleaseDate.split("/")
                                val sM = relParts.getOrNull(0)?.toIntOrNull()
                                val sY = relParts.getOrNull(1)?.toIntOrNull()
                                val normalizedYear = if (sY != null && sY >= 2019) sY - 2019 else sY
                                val isReleaseMonthNow = if (sM != null && normalizedYear != null) {
                                    event.currentYear > normalizedYear || (event.currentYear == normalizedYear && event.currentMonth >= sM)
                                } else true

                                if (isReleaseMonthNow) {
                                    val totalTheatrical = project.totalTheatricalMonths ?: if (project.distributionScale == "Global") 5 else 3
                                    val month1Revenue = calculateOpeningMonthRevenue(project, totalTheatrical)
                                    monthlyGross += month1Revenue
                                    logs.add("🍿 [${project.title}] Resmi TAYANG di Bioskop (${project.distributionScale})! Box Office Pembuka (Bln 1): +${com.example.ui.formatCurrencyRingkas(month1Revenue, false)}")

                                    project.copy(
                                        status = "IN_THEATERS",
                                        productionPhase = "TAYANG",
                                        productionDelayMonths = 0,
                                        remainingMonths = totalTheatrical - 1,
                                        totalTheatricalMonths = totalTheatrical,
                                        theaterStartMonth = event.currentMonth,
                                        theaterStartYear = event.currentYear,
                                        currentRevenue = month1Revenue,
                                        lastMonthRevenue = month1Revenue
                                    )
                                } else {
                                    logs.add("🔍 [${project.title}] Tahap QC selesai! Menunggu tanggal rilis bioskop yang telah dijadwalkan: ${project.scheduledReleaseDate}")
                                    project.copy(
                                        productionDelayMonths = 0,
                                        isAwaitingRelease = true,
                                        isQcPhase = false,
                                        productionPhase = "Menunggu Rilis Bioskop"
                                    )
                                }
                            } else {
                                // Belum dijadwalkan rilis -> Masuk fase QC internal studio agar pemain dapat memoles atau menjadwalkan
                                logs.add("🔍 [${project.title}] Produksi rampung! Masuk ke tahap QC internal studio (Skor: ${project.reviewScore}/100). Atur jadwal rilis bioskop Anda.")
                                project.copy(
                                    productionDelayMonths = 0,
                                    isQcPhase = true,
                                    internalScore = project.reviewScore,
                                    productionPhase = "Fase QC Internal"
                                )
                            }
                        } else {
                            // Perbarui tahapan dinamis sesuai standar studio Pixar/Disney/Marvel/Sony
                            val totalEstimate = if (project.totalProductionMonths > 0) project.totalProductionMonths else maxOf(newDelay + 1, 6)
                            val snapshot = FilmTimelineEngine.getProgress(
                                isAnimation = isAnim,
                                totalMonths = totalEstimate,
                                remainingMonths = newDelay
                            )
                            project.copy(
                                productionDelayMonths = newDelay,
                                productionPhase = snapshot.displayLabel,
                                totalProductionMonths = totalEstimate
                            )
                        }
                    }
                }

                "IN_THEATERS" -> {
                    val totalTheatrical = project.totalTheatricalMonths ?: if (project.distributionScale == "Global") 5 else 3
                    val remaining = project.remainingMonths
                    val monthInRun = (totalTheatrical - remaining + 1).coerceIn(2, totalTheatrical)

                    val thisMonthRevenue = calculateNextMonthRevenue(project, monthInRun, totalTheatrical)
                    monthlyGross += thisMonthRevenue
                    val newTotalCurrentRevenue = project.currentRevenue + thisMonthRevenue
                    val newRemaining = remaining - 1

                    if (newRemaining <= 0) {
                        // Selesai tayang di bioskop, masuk katalog IP permanen studio
                        val finalBoxOffice = newTotalCurrentRevenue
                        val totalNetProfit = finalBoxOffice - (project.budget + project.promoBudget)
                        logs.add("🏆 [${project.title}] Selesai masa tayang bioskop! Box Office penutup Bln ke-$monthInRun: +${com.example.ui.formatCurrencyRingkas(thisMonthRevenue, false)}. Total Box Office: ${com.example.ui.formatCurrencyRingkas(finalBoxOffice, false)} (Net Profit: ${com.example.ui.formatCurrencyRingkas(totalNetProfit, false)})")
                        project.copy(
                            status = "FINISHED",
                            productionPhase = "Rilis Selesai",
                            remainingMonths = 0,
                            currentRevenue = finalBoxOffice,
                            boxOffice = finalBoxOffice,
                            netProfit = totalNetProfit,
                            lastMonthRevenue = thisMonthRevenue,
                            releaseMonth = event.currentMonth,
                            releaseYear = event.currentYear
                        )
                    } else {
                        logs.add("🎟️ [${project.title}] Bln ke-$monthInRun di Bioskop: Tambahan Box Office +${com.example.ui.formatCurrencyRingkas(thisMonthRevenue, false)} (Total Terkumpul: ${com.example.ui.formatCurrencyRingkas(newTotalCurrentRevenue, false)})")
                        project.copy(
                            remainingMonths = newRemaining,
                            currentRevenue = newTotalCurrentRevenue,
                            lastMonthRevenue = thisMonthRevenue
                        )
                    }
                }

                "FINISHED" -> {
                    // Pemasukan lisensi streaming OTT / TV jika aktif
                    var fee = project.licenseMonthlyFee ?: 0L
                    var rem = project.licenseRemainingMonths
                    if (rem != null && rem > 0) {
                        monthlyGross += fee
                        rem -= 1
                        if (rem <= 0) {
                            logs.add("📑 Kontrak lisensi OTT untuk [${project.title}] telah berakhir.")
                            project.copy(
                                licenseRemainingMonths = 0,
                                licenseMonthlyFee = 0L,
                                licenseeName = null
                            )
                        } else {
                            project.copy(licenseRemainingMonths = rem)
                        }
                    } else {
                        project
                    }
                }

                else -> project
            }
        }

        val updatedBiz = business.copy(
            projectHistory = updatedProjects
        )

        return StudioMonthlyResult(
            business = updatedBiz,
            grossRevenue = monthlyGross,
            upkeep = upkeep,
            logs = logs
        )
    }

    private fun calculateOpeningMonthRevenue(project: MovieProject, totalTheatrical: Int): Long {
        val basePotential = if (project.targetMaxRevenue > 0) project.targetMaxRevenue else project.boxOffice
        val safeBase = if (basePotential > 0) basePotential else {
            val cost = project.budget + project.promoBudget
            (cost * (1.15 + (project.reviewScore / 100.0))).toLong().coerceAtLeast(1_000_000L)
        }

        // Opening month portion: 40-50% for <=3 months, 32-40% for 4+ months
        val basePortion = when {
            totalTheatrical <= 3 -> safeBase * 0.44
            totalTheatrical == 4 -> safeBase * 0.36
            else -> safeBase * 0.30
        }

        // Quality & Review score multiplier (0.80x to 1.30x)
        val scoreFactor = 0.80 + (project.reviewScore.coerceIn(1, 100) / 200.0)

        // Marketing / Promo budget leverage (up to +25%)
        val totalCost = project.budget + project.promoBudget
        val promoRatio = if (totalCost > 0) (project.promoBudget.toDouble() / totalCost.toDouble()).coerceIn(0.0, 0.5) else 0.1
        val marketingBoost = 1.0 + (promoRatio * 0.5)

        // Random market opening volatility / luck (0.82x to 1.25x)
        val randomVariance = (82..125).random() / 100.0

        val openingGross = (basePortion * scoreFactor * marketingBoost * randomVariance).toLong()
        return openingGross.coerceAtLeast(100_000L)
    }

    private fun calculateNextMonthRevenue(project: MovieProject, monthInRun: Int, totalTheatrical: Int): Long {
        val prevRev = if (project.lastMonthRevenue > 0L) {
            project.lastMonthRevenue
        } else {
            (project.currentRevenue / maxOf(1, monthInRun - 1)).coerceAtLeast(100_000L)
        }

        val score = project.reviewScore.coerceIn(1, 100)

        // Word-of-Mouth retention & legs based on review score
        val (retentionMin, retentionMax) = when {
            score >= 85 -> Pair(0.70, 0.95) // Masterpiece / Blockbuster: high staying power
            score >= 70 -> Pair(0.55, 0.78) // Good Word-of-Mouth
            score >= 50 -> Pair(0.40, 0.62) // Average / Mixed reception
            else -> Pair(0.20, 0.40)        // Flop / Steep drop
        }

        var retention = retentionMin + (Math.random() * (retentionMax - retentionMin))

        // 15% chance of a viral sleeper-hit bump if review score >= 75
        if (score >= 75 && (1..100).random() <= 15) {
            retention *= ((110..135).random() / 100.0)
        }

        // Market variance (±15%)
        val marketFactor = (85..118).random() / 100.0

        val monthGross = (prevRev * retention * marketFactor).toLong()
        return monthGross.coerceAtLeast(20_000L)
    }

    private data class StudioMonthlyResult(
        val business: OwnedBusiness,
        val grossRevenue: Long,
        val upkeep: Long,
        val logs: List<String>
    )
}
