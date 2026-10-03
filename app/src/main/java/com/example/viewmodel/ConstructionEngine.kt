package com.example.viewmodel

import com.example.data.*
import kotlin.math.roundToInt
import kotlin.random.Random

data class TenderTemplate(
    val title: String,
    val clientName: String,
    val clientType: String,
    val scale: String,
    val baseBudget: Long,
    val baseCost: Long,
    val duration: Int,
    val crews: Int,
    val machinery: Int,
    val desc: String
)

data class ConstructionUpgradeSpecs(
    val cost: Long,
    val newCrews: Int,
    val newMachinery: Int,
    val newSafetyCert: Int,
    val trustGain: Int
)

object ConstructionEngine {

    fun getUsedCrews(business: OwnedBusiness): Int {
        return business.activeTenders
            .filter { !it.isFinished && it.currentPhaseIndex < it.phases.size && it.phases.getOrNull(it.currentPhaseIndex)?.isAllocated == true }
            .sumOf { it.requiredCrews }
    }

    fun getUsedMachinery(business: OwnedBusiness): Int {
        return business.activeTenders
            .filter { !it.isFinished && it.currentPhaseIndex < it.phases.size && it.phases.getOrNull(it.currentPhaseIndex)?.isAllocated == true }
            .sumOf { it.requiredMachinery }
    }

    fun getAvailableCrews(business: OwnedBusiness): Int {
        val max = business.constructionData.maxCrews.coerceAtLeast(1)
        return (max - getUsedCrews(business)).coerceAtLeast(0)
    }

    fun getAvailableMachinery(business: OwnedBusiness): Int {
        val max = business.constructionData.maxMachinery.coerceAtLeast(1)
        return (max - getUsedMachinery(business)).coerceAtLeast(0)
    }

    fun hasLogisticsSynergy(playerState: PlayerState): Boolean {
        return playerState.ownedBusinesses.any { it.catalogId == "mid_logistics" } ||
                playerState.holdingCompanies.any { h -> h.subsidiaries.any { it.catalogId == "mid_logistics" } }
    }

    fun createDefaultPhases(projectName: String, duration: Int): List<ConstructionPhase> {
        val dur = duration.coerceAtLeast(3)
        val p1Months = maxOf(1, (dur * 0.30).roundToInt())
        val p2Months = maxOf(1, (dur * 0.45).roundToInt())
        val p3Months = maxOf(1, dur - p1Months - p2Months)

        val isAirport = projectName.contains("Bandara", ignoreCase = true) || projectName.contains("Airport", ignoreCase = true)
        val isHighway = projectName.contains("Tol", ignoreCase = true) || projectName.contains("Jembatan", ignoreCase = true) || projectName.contains("Jalan", ignoreCase = true)
        val isHighRise = projectName.contains("Apartemen", ignoreCase = true) || projectName.contains("Hotel", ignoreCase = true) || projectName.contains("Gedung", ignoreCase = true) || projectName.contains("Mall", ignoreCase = true) || projectName.contains("Rumah Sakit", ignoreCase = true)
        val isIndustrial = projectName.contains("Pabrik", ignoreCase = true) || projectName.contains("Pelabuhan", ignoreCase = true) || projectName.contains("Logistik", ignoreCase = true)

        val (phase1Name, phase2Name, phase3Name) = when {
            isAirport -> Triple(
                "Fase 1: Land Clearing & Runway Earthworks",
                "Fase 2: Struktur Terminal & ATC Tower",
                "Fase 3: Runway Paving, MEP & Commissioning"
            )
            isHighway -> Triple(
                "Fase 1: Pembebasan Lahan & Subgrade Base",
                "Fase 2: Pengecoran Beton & Girder Jembatan",
                "Fase 3: Pengaspalan Hotmix, Barrier & Gerbang Tol"
            )
            isHighRise -> Triple(
                "Fase 1: Bore Pile, Retaining Wall & Basement",
                "Fase 2: Struktur Utama Kolom, Balok & Lantai",
                "Fase 3: Arsitektur Fasad, MEP, Lift & Finishing"
            )
            isIndustrial -> Triple(
                "Fase 1: Cut & Fill, Pondasi Baja Berat",
                "Fase 2: Ereksi Rangka Baja & Cladding Dinding",
                "Fase 3: Instalasi Mesin, Loading Dock & Utilitas"
            )
            else -> Triple(
                "Fase 1: Land Clearing & Ground Foundation",
                "Fase 2: Struktur Utama & Konstruksi Fisik",
                "Fase 3: MEP, Interior Finishing & Serah Terima"
            )
        }

        return listOf(
            ConstructionPhase(phaseNumber = 1, name = phase1Name, durationMonths = p1Months, remainingMonths = p1Months, isAllocated = false, isCompleted = false, payoutPercent = 0.30),
            ConstructionPhase(phaseNumber = 2, name = phase2Name, durationMonths = p2Months, remainingMonths = p2Months, isAllocated = false, isCompleted = false, payoutPercent = 0.40),
            ConstructionPhase(phaseNumber = 3, name = phase3Name, durationMonths = p3Months, remainingMonths = p3Months, isAllocated = false, isCompleted = false, payoutPercent = 0.30)
        )
    }

    fun generateTenderMarket(level: Int, trustScore: Int): List<ConstructionTenderOpportunity> {
        val templates = listOf(
            TenderTemplate(
                title = "Megaproyek Jalan Tol Trans-Sumatera Ruas Prioritas",
                clientName = "Kementerian PUPR & BPJT",
                clientType = "PEMERINTAH",
                scale = "MEGA",
                baseBudget = 180_000_000L,
                baseCost = 120_000_000L,
                duration = 18,
                crews = 3,
                machinery = 9,
                desc = "Konstruksi jalan tol bebas hambatan 4 lajur sepanjang 42 km melintasi perbukitan dan sungai."
            ),
            TenderTemplate(
                title = "Bandara Internasional Hub Terminal 3 & Runway 2",
                clientName = "PT Angkasa Pura Indonesia (BUMN)",
                clientType = "PEMERINTAH",
                scale = "MEGA",
                baseBudget = 250_000_000L,
                baseCost = 160_000_000L,
                duration = 24,
                crews = 4,
                machinery = 12,
                desc = "Pembangunan terminal megah ramah lingkungan & landasan pacu wide-body Boeing 777/A380."
            ),
            TenderTemplate(
                title = "Superblock Menara Apartemen & Hotel Bintang 5",
                clientName = "Pakuwon & Agung Sedayu Group",
                clientType = "LUXURY",
                scale = "LARGE",
                baseBudget = 95_000_000L,
                baseCost = 65_000_000L,
                duration = 14,
                crews = 2,
                machinery = 6,
                desc = "Menara kembar 48 lantai dengan basement 4 tingkat dan skybridge infinity pool."
            ),
            TenderTemplate(
                title = "Pabrik Perakitan Baterai & Giga EV Cell",
                clientName = "Indonesia Battery Corporation (IBC)",
                clientType = "SWASTA",
                scale = "LARGE",
                baseBudget = 75_000_000L,
                baseCost = 50_000_000L,
                duration = 12,
                crews = 2,
                machinery = 5,
                desc = "Fasilitas manufaktur berteknologi clean-room kelas tinggi dengan sistem otomasi penuh."
            ),
            TenderTemplate(
                title = "Pusat Logistik & Pergudangan Otomatis Nasional",
                clientName = "PT Global Distribusi Sentosa",
                clientType = "SWASTA",
                scale = "MEDIUM",
                baseBudget = 35_000_000L,
                baseCost = 24_000_000L,
                duration = 8,
                crews = 1,
                machinery = 3,
                desc = "Gudang modern bertingkat dengan lantai super flat heavy-duty dan 30 loading dock."
            ),
            TenderTemplate(
                title = "Rumah Sakit Umum Spesialis Kanker Terpadu",
                clientName = "Dinas Kesehatan Provinsi",
                clientType = "PEMERINTAH",
                scale = "MEDIUM",
                baseBudget = 45_000_000L,
                baseCost = 31_000_000L,
                duration = 9,
                crews = 2,
                machinery = 4,
                desc = "Gedung rumah sakit 10 lantai berstandar internasional dengan bunker radiologi khusus."
            ),
            TenderTemplate(
                title = "Jembatan Cable-Stayed Penghubung Kawasan Pesisir",
                clientName = "Bappeda & Dinas Bina Marga",
                clientType = "PEMERINTAH",
                scale = "LARGE",
                baseBudget = 85_000_000L,
                baseCost = 58_000_000L,
                duration = 15,
                crews = 3,
                machinery = 7,
                desc = "Jembatan gantung bentang panjang 800 meter anti gempa berkekuatan magnitudo 8."
            ),
            TenderTemplate(
                title = "Kawasan Mall & Lifestyle Center Terintegrasi MRT",
                clientName = "Lippo Development Corp",
                clientType = "LUXURY",
                scale = "LARGE",
                baseBudget = 110_000_000L,
                baseCost = 76_000_000L,
                duration = 16,
                crews = 3,
                machinery = 8,
                desc = "Pusat perbelanjaan 7 lantai terhubung langsung dengan stasiun bawah tanah MRT."
            )
        )

        val shuffled = templates.shuffled().take(4)
        return shuffled.map { tmpl ->
            val scaleMult = (1.0 + (level - 1) * 0.15)
            val budget = (tmpl.baseBudget * scaleMult).toLong()
            val cost = (tmpl.baseCost * scaleMult).toLong()
            val phases = createDefaultPhases(tmpl.title, tmpl.duration)

            val rival1 = (budget * Random.nextDouble(0.89, 0.94)).toLong()
            val rival2 = (budget * Random.nextDouble(0.93, 0.99)).toLong()
            val rival3 = (budget * Random.nextDouble(0.98, 1.06)).toLong()

            ConstructionTenderOpportunity(
                title = tmpl.title,
                clientName = tmpl.clientName,
                clientType = tmpl.clientType,
                projectScale = tmpl.scale,
                ownerEstimateBudget = budget,
                estimatedBaseCost = cost,
                durationMonths = tmpl.duration,
                requiredCrews = tmpl.crews,
                requiredMachinery = tmpl.machinery,
                phases = phases,
                rivalAiBids = listOf(rival1, rival2, rival3),
                minBidBond = (budget * 0.05).toLong(),
                description = tmpl.desc
            )
        }
    }

    data class BiddingResult(
        val isWon: Boolean,
        val winningBid: Long,
        val winningContractor: String,
        val competitorBids: List<Pair<String, Long>>,
        val estimatedMarginPercent: Double,
        val estimatedProfit: Long,
        val feedbackMessage: String,
        val trustScoreDelta: Int
    )

    fun evaluateBid(
        tender: ConstructionTenderOpportunity,
        playerBid: Long,
        trustScore: Int,
        usesInHouseLogistics: Boolean
    ): BiddingResult {
        val oe = tender.ownerEstimateBudget.toDouble()
        val bidRatio = playerBid / oe
        val effectiveBaseCost = if (usesInHouseLogistics) {
            (tender.estimatedBaseCost * 0.85).toLong()
        } else {
            tender.estimatedBaseCost
        }

        val estimatedProfit = playerBid - effectiveBaseCost
        val marginPercent = if (playerBid > 0) (estimatedProfit.toDouble() / playerBid * 100.0) else 0.0

        val competitorNames = listOf("PT Adhi Karya Mandiri", "PT Wijaya Bangun Prima", "PT Hutama Infrastruktur")
        val compBids = tender.rivalAiBids.mapIndexed { idx, bid ->
            val name = competitorNames.getOrElse(idx) { "Kontraktor AI #${idx + 1}" }
            Pair(name, bid)
        }

        val priceScore = when {
            bidRatio <= 0.75 -> 0.98
            bidRatio <= 0.85 -> 0.88 - (bidRatio - 0.75) * 1.0
            bidRatio <= 0.95 -> 0.78 - (bidRatio - 0.85) * 1.5
            bidRatio <= 1.05 -> 0.63 - (bidRatio - 0.95) * 2.5
            bidRatio <= 1.15 -> 0.38 - (bidRatio - 1.05) * 2.5
            else -> 0.05
        }

        val trustBonus = ((trustScore - 50) / 100.0) * 0.35
        val totalWinProb = (priceScore + trustBonus).coerceIn(0.02, 0.99)

        val roll = Random.nextDouble(0.0, 1.0)
        val isWon = roll < totalWinProb

        return if (isWon) {
            val deltaTrust = if (marginPercent < 5.0) 1 else if (marginPercent < 20.0) 3 else 2
            BiddingResult(
                isWon = true,
                winningBid = playerBid,
                winningContractor = "Perusahaan Anda",
                competitorBids = compBids,
                estimatedMarginPercent = marginPercent,
                estimatedProfit = estimatedProfit,
                feedbackMessage = "Selamat! Penawaran Anda dinilai paling kompetitif oleh panitia lelang klien (${tender.clientName}). Kontrak resmi diterbitkan!",
                trustScoreDelta = deltaTrust
            )
        } else {
            val lowestComp = compBids.minByOrNull { it.second } ?: Pair("PT Adhi Karya Mandiri", (tender.ownerEstimateBudget * 0.92).toLong())
            val feedback = if (bidRatio > 1.05) {
                "Penawaran Anda (${(bidRatio * 100).toInt()}% OE) terlalu tinggi dibandingkan penawaran kompetitor ${lowestComp.first}."
            } else {
                "Panitia tender memilih proposal ${lowestComp.first} berdasarkan pertimbangan teknis dan penawaran harga."
            }
            BiddingResult(
                isWon = false,
                winningBid = lowestComp.second,
                winningContractor = lowestComp.first,
                competitorBids = compBids,
                estimatedMarginPercent = marginPercent,
                estimatedProfit = estimatedProfit,
                feedbackMessage = feedback,
                trustScoreDelta = 0
            )
        }
    }

    fun rollRandomEvent(project: ConstructionProject, safetyCertLevel: Int): ConstructionRngEvent? {
        if (project.activeEvent != null && !project.activeEvent.isResolved) return null
        if (Random.nextDouble() > 0.20) return null

        val roll = (1..4).random()
        val phaseCost = (project.totalContractValue * 0.05).toLong().coerceAtLeast(250_000L)
        return when (roll) {
            1 -> ConstructionRngEvent(
                title = "Lonjakan Harga Semen & Baja",
                description = "Kenaikan harga bahan baku konstruksi global meningkatkan biaya operasional fase berjalan.",
                type = "PRICE_HIKE",
                costImpact = phaseCost,
                delayMonths = 0,
                trustScoreImpact = 0
            )
            2 -> ConstructionRngEvent(
                title = "Cuaca Buruk & Hujan Badai",
                description = "Hujan lebat dan genangan air menunda pekerjaan pengecoran & pengaspalan selama 1 bulan.",
                type = "BAD_WEATHER",
                costImpact = (phaseCost * 0.5).toLong(),
                delayMonths = 1,
                trustScoreImpact = 0
            )
            3 -> {
                if (safetyCertLevel >= 3) {
                    ConstructionRngEvent(
                        title = "Sistem K3 Berhasil Cegah Insiden",
                        description = "Prosedur keselamatan kerja tingkat lanjut mendeteksi potensi bahaya crane dan mengamankan lokasi tanpa korban.",
                        type = "SAFETY_PASS",
                        costImpact = 0L,
                        delayMonths = 0,
                        trustScoreImpact = 2
                    )
                } else {
                    ConstructionRngEvent(
                        title = "Kecelakaan Kerja Lapangan",
                        description = "Terjadi kerusakan scaffolding dan insiden kerja ringan di area proyek.",
                        type = "ACCIDENT",
                        costImpact = (phaseCost * 0.75).toLong(),
                        delayMonths = 0,
                        trustScoreImpact = -4
                    )
                }
            }
            else -> ConstructionRngEvent(
                title = "Inspeksi Kualitas Klien Memuaskan",
                description = "Pengawas klien sangat puas dengan presisi dan kerapian pengerjaan fase ini.",
                type = "QUALITY_BONUS",
                costImpact = -(phaseCost * 0.4).toLong(),
                delayMonths = 0,
                trustScoreImpact = 3
            )
        }
    }

    data class MonthlyConstructionTickResult(
        val updatedBusiness: OwnedBusiness,
        val netTerminIncome: Long,
        val notifications: List<String>
    )

    fun processMonthlyTick(business: OwnedBusiness, hasLogistics: Boolean): MonthlyConstructionTickResult {
        var companyCash = business.companyCash
        var trustScore = business.constructionData.trustScore
        var completedCount = business.constructionData.completedProjectsCount
        var totalRev = business.constructionData.totalRevenueGenerated
        val notifications = mutableListOf<String>()
        var totalTerminInjected = 0L

        val updatedProjects = business.activeTenders.map { project ->
            if (project.isFinished) return@map project

            // Ensure project has phases initialized
            val safePhases = if (project.phases.isEmpty()) {
                createDefaultPhases(project.name, project.durationMonths)
            } else {
                project.phases
            }

            var currentPhaseIdx = project.currentPhaseIndex
            var currentEvent = project.activeEvent

            // Check if current phase is valid
            if (currentPhaseIdx < safePhases.size) {
                val curPhase = safePhases[currentPhaseIdx]

                // Only progress if allocated
                if (curPhase.isAllocated && !curPhase.isCompleted) {
                    // Check if there is an unresolved BAD_WEATHER delay
                    if (currentEvent != null && currentEvent.type == "BAD_WEATHER" && !currentEvent.isResolved) {
                        // Project delayed by weather
                        notifications.add("🌧️ Proyek '${project.name}' tertunda 1 bulan akibat cuaca buruk.")
                        return@map project.copy(phases = safePhases)
                    }

                    val newPhaseRemaining = curPhase.remainingMonths - 1
                    val newProjRemaining = maxOf(0, project.remainingMonths - 1)

                    if (newPhaseRemaining <= 0) {
                        // Phase is finished!
                        val updatedPhaseList = safePhases.mapIndexed { idx, p ->
                            if (idx == currentPhaseIdx) p.copy(remainingMonths = 0, isCompleted = true, isAllocated = false)
                            else p
                        }

                        // Calculate Termin Payment
                        val totalVal = if (project.agreedBidPrice > 0) project.agreedBidPrice.toDouble() else project.totalContractValue
                        val terminAmount = (totalVal * curPhase.payoutPercent).toLong()
                        val logisticsBonus = if (project.usesInHouseLogistics || hasLogistics) (terminAmount * 0.05).toLong() else 0L
                        val finalTermin = terminAmount + logisticsBonus

                        companyCash += finalTermin
                        totalTerminInjected += finalTermin
                        totalRev += finalTermin
                        trustScore = (trustScore + 1).coerceAtMost(100)

                        val nextPhaseIdx = currentPhaseIdx + 1
                        val isAllFinished = nextPhaseIdx >= updatedPhaseList.size

                        if (isAllFinished) {
                            // Return Security Deposit & Bonus
                            val depositRefund = project.initialSecurityDeposit
                            companyCash += depositRefund
                            completedCount += 1
                            trustScore = (trustScore + 4).coerceAtMost(100)

                            notifications.add("🎉 Megaproyek '${project.name}' SELESAI 100%! Termin final + pengembalian jaminan dicairkan ke Kas Perusahaan.")

                            project.copy(
                                phases = updatedPhaseList,
                                currentPhaseIndex = nextPhaseIdx,
                                remainingMonths = 0,
                                isFinished = true,
                                totalPaidOut = project.totalPaidOut + finalTermin,
                                activeEvent = null
                            )
                        } else {
                            notifications.add("✅ ${curPhase.name} selesai! Termin ${ (curPhase.payoutPercent * 100).toInt() }% cair. Segera alokasikan kru untuk fase berikutnya.")

                            project.copy(
                                phases = updatedPhaseList,
                                currentPhaseIndex = nextPhaseIdx,
                                remainingMonths = newProjRemaining,
                                totalPaidOut = project.totalPaidOut + finalTermin,
                                activeEvent = null
                            )
                        }
                    } else {
                        // Roll RNG Event during ongoing phase
                        val newEvent = if (currentEvent == null) {
                            rollRandomEvent(project, business.constructionData.safetyCertLevel)
                        } else currentEvent

                        if (newEvent != null && newEvent != currentEvent) {
                            if (newEvent.type == "QUALITY_BONUS") {
                                val bonusTip = -newEvent.costImpact
                                companyCash += bonusTip
                                trustScore = (trustScore + newEvent.trustScoreImpact).coerceAtMost(100)
                                notifications.add("⭐ Klien memberikan tip bonus atas kualitas pengerjaan proyek '${project.name}'!")
                            } else if (newEvent.type == "SAFETY_PASS") {
                                trustScore = (trustScore + newEvent.trustScoreImpact).coerceAtMost(100)
                                notifications.add("🛡️ Prosedur K3 mendeteksi dan mencegah potensi insiden di proyek '${project.name}'.")
                            }
                        }

                        val updatedPhaseList = safePhases.mapIndexed { idx, p ->
                            if (idx == currentPhaseIdx) p.copy(remainingMonths = newPhaseRemaining)
                            else p
                        }

                        project.copy(
                            phases = updatedPhaseList,
                            remainingMonths = newProjRemaining,
                            activeEvent = newEvent
                        )
                    }
                } else {
                    // Waiting for allocation
                    project.copy(phases = safePhases)
                }
            } else {
                project.copy(isFinished = true)
            }
        }

        // Generate or refresh tender market if empty
        val currentMarket = if (business.constructionData.availableTenderMarket.isEmpty()) {
            generateTenderMarket(business.level, trustScore)
        } else {
            business.constructionData.availableTenderMarket
        }

        val updatedFirmData = business.constructionData.copy(
            trustScore = trustScore,
            completedProjectsCount = completedCount,
            totalRevenueGenerated = totalRev,
            availableTenderMarket = currentMarket
        )

        val finalBiz = business.copy(
            companyCash = companyCash,
            activeTenders = updatedProjects,
            constructionData = updatedFirmData
        )

        return MonthlyConstructionTickResult(
            updatedBusiness = finalBiz,
            netTerminIncome = totalTerminInjected,
            notifications = notifications
        )
    }
}
