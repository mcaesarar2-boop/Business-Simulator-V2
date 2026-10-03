package com.example.data

import kotlin.random.Random

typealias ContentType = com.example.businessunit.contentcreator.model.ContentType
typealias ContentStatus = com.example.businessunit.contentcreator.model.ContentStatus
typealias ContentSortOption = com.example.businessunit.contentcreator.model.ContentSortOption
typealias CoProductionFundingScheme = com.example.businessunit.contentcreator.model.CoProductionFundingScheme
typealias CreativeFocus = com.example.businessunit.contentcreator.model.CreativeFocus
typealias ContentWork = com.example.businessunit.contentcreator.model.ContentWork
typealias ProductionHouseOffer = com.example.businessunit.contentcreator.model.ProductionHouseOffer
typealias ContentScoreEvaluation = com.example.businessunit.contentcreator.model.ContentScoreEvaluation

object ContentProductionEngine {

    /**
     * Evaluasi realistis & arcade-fun:
     * Budget memberi kualitas teknis (diminishing return), tapi TIDAK otomatis menjamin rating 100.
     * Rating ditentukan oleh perpaduan:
     * 1. Kualitas Teknis & Produksi (Budget & Eksekusi Tim, maks ~28 pt)
     * 2. Sinergi Konsep & Fokus Kreatif (Ide, Naskah & Pengalaman Kreator, maks ~36 pt)
     * 3. Respon Audiens & Viralitas Algoritma (Bisa jadi Sleeper Hit tak terduga atau Over-Budget Flop, maks ~36 pt)
     */
    fun calculateDetailedScore(
        budget: Long,
        channelLevel: Int,
        employees: Int,
        contentType: ContentType,
        creativeFocus: CreativeFocus? = null,
        isStudioCoProd: Boolean = false,
        studioLevel: Int = 1
    ): ContentScoreEvaluation {
        // 1. KUALITAS TEKNIS DARI BUDGET (Diminishing return)
        val technicalBudgetPoints = when (contentType) {
            ContentType.FEATURE_FILM -> when {
                budget >= 500_000L -> 24
                budget >= 200_000L -> 21
                budget >= 100_000L -> 18
                budget >= 50_000L -> 15
                else -> 10
            }
            ContentType.SHORT_FILM -> when {
                budget >= 150_000L -> 24
                budget >= 50_000L -> 21
                budget >= 20_000L -> 18
                budget >= 8_000L -> 14
                budget >= 3_000L -> 10
                else -> 7
            }
            ContentType.DEEP_DIVE_ESSAY -> when {
                budget >= 30_000L -> 23 // Essay tidak butuh ratusan ribu dolar
                budget >= 15_000L -> 22
                budget >= 6_000L -> 19
                budget >= 2_000L -> 15
                else -> 10
            }
            ContentType.DOCUMENTARY -> when {
                budget >= 120_000L -> 24
                budget >= 50_000L -> 21
                budget >= 20_000L -> 17
                budget >= 7_000L -> 13
                else -> 8
            }
        }
        val crewConsistency = (employees * 1.2).toInt().coerceAtMost(6)
        val finalTechnicalScore = (technicalBudgetPoints + crewConsistency).coerceIn(8, 30)

        // 2. KONSEPTUAL, NASKAH & SINERGI FOKUS KREATIF
        var synergyPoints = 14
        val focus = creativeFocus ?: when (contentType) {
            ContentType.FEATURE_FILM -> CreativeFocus.STORY_NARRATIVE
            ContentType.SHORT_FILM -> CreativeFocus.STORY_NARRATIVE
            ContentType.DEEP_DIVE_ESSAY -> CreativeFocus.INVESTIGATION
            ContentType.DOCUMENTARY -> CreativeFocus.INVESTIGATION
        }

        when (contentType) {
            ContentType.FEATURE_FILM -> {
                if (focus == CreativeFocus.STORY_NARRATIVE) synergyPoints += 10
                else if (focus == CreativeFocus.CINEMATOGRAPHY) synergyPoints += 9
                else if (focus == CreativeFocus.VIRAL_HOOK) synergyPoints += 5
                else synergyPoints += 4
            }
            ContentType.SHORT_FILM -> {
                if (focus == CreativeFocus.STORY_NARRATIVE) synergyPoints += 9
                else if (focus == CreativeFocus.CINEMATOGRAPHY) synergyPoints += 7
                else if (focus == CreativeFocus.VIRAL_HOOK) synergyPoints += 5
                else synergyPoints += 3
            }
            ContentType.DEEP_DIVE_ESSAY -> {
                if (focus == CreativeFocus.INVESTIGATION) synergyPoints += 10
                else if (focus == CreativeFocus.STORY_NARRATIVE) synergyPoints += 7
                else if (focus == CreativeFocus.VIRAL_HOOK) synergyPoints += 5
                else synergyPoints += 2
            }
            ContentType.DOCUMENTARY -> {
                if (focus == CreativeFocus.INVESTIGATION) synergyPoints += 10
                else if (focus == CreativeFocus.CINEMATOGRAPHY) synergyPoints += 8
                else if (focus == CreativeFocus.STORY_NARRATIVE) synergyPoints += 6
                else synergyPoints += 3
            }
        }

        val creatorMaturity = (channelLevel / 7).coerceAtMost(7)
        val studioBonus = if (isStudioCoProd) (studioLevel / 8).coerceAtMost(5) else 0
        val finalConceptScore = (synergyPoints + creatorMaturity + studioBonus).coerceIn(15, 36)

        // 3. AUDIENCE RECEPTION & LUCK (The Arcade Magic)
        val luckRoll = Random.nextInt(1, 101)
        var audienceScore: Int
        val verdictTitle: String
        val verdictDescription: String

        // A. Resiko Over-Budget Flop: Budget tinggi tapi naskah dangkal/klise (16% kemungkinan jika budget >= $35k)
        if (budget >= 35_000L && luckRoll <= 16) {
            audienceScore = Random.nextInt(8, 16)
            verdictTitle = "📉 Over-Budget Tapi Naskah Kurang"
            verdictDescription = "Visual terpoles mahal, tetapi naskah klise dan pacing lambat menuai kritik tajam dari komunitas penonton."
        }
        // B. Indie Sleeper Hit: Budget indie/moderat mendadak viral fenomenal (18% kemungkinan jika budget <= $35k)
        else if (budget <= 35_000L && luckRoll >= 82) {
            audienceScore = Random.nextInt(29, 36)
            verdictTitle = "🚀 Sleeper Hit Indie Viral!"
            verdictDescription = "Dengan budget sederhana, karya ini viral besar di medsos berkat ide segar yang mengguncang industri!"
        }
        // C. Mahakarya Spektakuler / Masterpiece (Roll tinggi 88-100)
        else if (luckRoll >= 88) {
            audienceScore = Random.nextInt(28, 35)
            verdictTitle = "🌟 Mahakarya yang Diakui Global"
            verdictDescription = "Perpaduan sinematik memikat dan storytelling brilian menuai standing ovation dan pujian kritikus ternama!"
        }
        // D. Trending Hit Sukses (Roll 55-87)
        else if (luckRoll >= 55) {
            audienceScore = Random.nextInt(22, 28)
            verdictTitle = "🔥 Trending & Digemari Penonton"
            verdictDescription = "Disambut hangat audiens dengan retensi tontonan tinggi. Algoritma merekomendasikan karya ini secara luas."
        }
        // E. Rata-Rata / Niche Komunitas (Roll 20-54)
        else if (luckRoll >= 20) {
            audienceScore = Random.nextInt(16, 22)
            verdictTitle = "👌 Solid untuk Komunitas Niche"
            verdictDescription = "Eksekusi rapi dan stabil. Sangat dinikmati oleh penggemar setia genre ini meskipun jangkauannya terbatas."
        }
        // F. Flop Wajar / Kurang Diminati (Roll < 20)
        else {
            audienceScore = Random.nextInt(10, 16)
            verdictTitle = "⚠️ Kurang Relevan di Tren Pasar"
            verdictDescription = "Kurang mendapat traksi di algoritma dan audiens menganggap topiknya kurang memiliki daya tarik emosional."
        }

        val totalRawScore = finalTechnicalScore + finalConceptScore + audienceScore
        val finalScore = totalRawScore.coerceIn(28, 98)

        return ContentScoreEvaluation(
            score = finalScore,
            verdictTitle = verdictTitle,
            verdictDescription = verdictDescription,
            technicalScore = finalTechnicalScore,
            conceptScore = finalConceptScore,
            audienceScore = audienceScore
        )
    }

    fun calculateEngagementScore(budget: Long, channelLevel: Int, employees: Int): Int {
        return calculateDetailedScore(
            budget = budget,
            channelLevel = channelLevel,
            employees = employees,
            contentType = ContentType.SHORT_FILM
        ).score
    }

    private val MAJOR_PH_NAMES = listOf(
        "Warner Bros. Pictures",
        "Netflix Originals",
        "A24 Studios",
        "Paramount Pictures",
        "Universal Pictures",
        "Sony Pictures Entertainment",
        "Falcon Pictures",
        "Starvision Plus",
        "Lionsgate Films",
        "Blumhouse Productions",
        "HBO Max Studios",
        "Amazon MGM Studios",
        "Disney+ Originals",
        "Apple Original Films"
    )

    /**
     * Menghasilkan penawaran PH yang realistis namun tetap arcade-fun & rewarding:
     * - Nilai didorong oleh Virality Score (kualitas & daya tarik pasar), bukan sekadar modal budget.
     * - Jual Putus (Lump Sum): Beli hak cipta & eksploitasi penuh.
     * - Sewa Kontrak (Lisensi & Royalti): Uang muka (upfront) + royalti pasif bulanan selama masa kontrak (12, 18, 24, atau 36 bln).
     *   Setelah masa kontrak selesai, hak cipta kembali lagi ke kreator di Bank Konten.
     */
    fun generateTargetedOffer(
        portfolio: List<ContentWork>,
        channelLevel: Int = 1,
        forceTarget: ContentWork? = null
    ): ProductionHouseOffer? {
        val eligibleWorks = portfolio.filter {
            it.status == ContentStatus.AVAILABLE &&
            !it.isShadowRecord &&
            it.engagementScore != null &&
            it.engagementScore > 0
        }
        if (eligibleWorks.isEmpty() && forceTarget == null) return null

        val targetWork = if (forceTarget != null) {
            if (forceTarget.isShadowRecord || forceTarget.status != ContentStatus.AVAILABLE || forceTarget.engagementScore == null || forceTarget.engagementScore <= 0) {
                return null
            }
            forceTarget
        } else {
            // Weighted random selection based on engagement score squared
            val totalWeight = eligibleWorks.sumOf {
                val s = it.engagementScore ?: 50
                (s * s).coerceAtLeast(1)
            }
            var randomWeight = Random.nextInt(totalWeight)
            var selected: ContentWork = eligibleWorks.first()
            for (work in eligibleWorks) {
                val s = work.engagementScore ?: 50
                val weight = (s * s).coerceAtLeast(1)
                if (randomWeight < weight) {
                    selected = work
                    break
                }
                randomWeight -= weight
            }
            selected
        }

        val phName = MAJOR_PH_NAMES.random()
        val score = targetWork.engagementScore ?: 50
        val baseBudget = maxOf(targetWork.budget, 4_000L)

        // Creator brand leverage: level memberi sedikit daya tawar (1.0x - 1.45x)
        val channelLeverage = 1.0 + (channelLevel.coerceAtMost(30) * 0.015)

        // PERHITUNGAN VALUASI REALISTIS & ARCADE-REWARDING:
        // Score adalah raja: karya dengan score tinggi bernilai kelipatan besar,
        // bahkan karya low-budget yang viral mendapat penawaran fantastis!
        val lumpSumMultiplier: Double
        val baseFloorValuation: Long
        when {
            score >= 88 -> { // Mahakarya / Viral Fenomenal
                lumpSumMultiplier = Random.nextDouble(7.0, 13.5)
                baseFloorValuation = 180_000L
            }
            score >= 72 -> { // Trending Hit
                lumpSumMultiplier = Random.nextDouble(3.8, 6.5)
                baseFloorValuation = 80_000L
            }
            score >= 50 -> { // Solid / Niche
                lumpSumMultiplier = Random.nextDouble(1.8, 3.2)
                baseFloorValuation = 25_000L
            }
            else -> { // Flop / Low traksi
                lumpSumMultiplier = Random.nextDouble(0.85, 1.35)
                baseFloorValuation = 8_000L
            }
        }

        val calculatedLumpSum = maxOf(
            baseFloorValuation,
            (baseBudget * lumpSumMultiplier * channelLeverage).toLong()
        )
        val lumpSumOffer = roundToNiceNumber(calculatedLumpSum)

        // PILIHAN SEWA KONTRAK / LISENSI EKSKLUSIF BERJANGKA:
        // Creator tetap memiliki hak cipta. PH membayar:
        // 1. Upfront Cash: 28% - 38% dari total nilai valuasi
        // 2. Monthly Royalty: Cash flow pasif per bulan selama masa kontrak (12, 18, 24, atau 36 bulan)
        val contractDurationMonths = when {
            score >= 88 -> listOf(24, 36).random()
            score >= 72 -> listOf(18, 24).random()
            else -> listOf(12, 18).random()
        }

        val royaltyUpfrontFraction = when {
            score >= 88 -> Random.nextDouble(0.32, 0.40)
            score >= 72 -> Random.nextDouble(0.28, 0.35)
            else -> Random.nextDouble(0.24, 0.30)
        }
        val rawUpfront = (lumpSumOffer * royaltyUpfrontFraction).toLong()
        val royaltyUpfront = roundToNiceNumber(maxOf(2_500L, rawUpfront))

        // Total royalti bulanan yang dibagikan selama durasi:
        // Porsi sisa kontrak dialokasikan per bulan dengan sedikit bonus jangka panjang
        val remainingValuationForRoyalty = (lumpSumOffer * (1.0 - royaltyUpfrontFraction) * 1.15).toLong()
        val rawMonthlyRoyalty = remainingValuationForRoyalty / contractDurationMonths
        val monthlyRoyalty = roundToNiceNumber(maxOf(500L, rawMonthlyRoyalty))

        val pitchMessage = when {
            score >= 88 -> "Studio $phName terkagum-kagum dengan respons luas atas \"${targetWork.title}\" (Score $score/100)! Mereka ingin mengamankan hak penayangan sebelum studio lain mendahului."
            score >= 72 -> "Studio $phName melihat lonjakan tren dari \"${targetWork.title}\" (Score $score/100)! Mereka mengajukan penawaran distribusi resmi untuk memperluas jangkauan penonton."
            score >= 50 -> "Studio $phName mengapresiasi kualitas penceritaan \"${targetWork.title}\" dan menawarkan kontrak penayangan streaming di platform mereka."
            else -> "Studio $phName bersedia mengakuisisi lisensi non-eksklusif \"${targetWork.title}\" sebagai pengisi katalog konten sekunder."
        }

        return ProductionHouseOffer(
            phName = phName,
            phTier = when {
                score >= 88 -> "Hollywood Prestige Studio"
                score >= 72 -> "Major Streaming Network"
                else -> "Independent Production House"
            },
            contentId = targetWork.id,
            contentTitle = targetWork.title,
            contentType = targetWork.type,
            engagementScore = score,
            lumpSumOffer = lumpSumOffer,
            royaltyUpfront = royaltyUpfront,
            monthlyRoyalty = monthlyRoyalty,
            contractDurationMonths = contractDurationMonths,
            pitchMessage = pitchMessage,
            durationSeconds = 20
        )
    }

    private fun roundToNiceNumber(value: Long): Long {
        return when {
            value > 10_000_000 -> (value / 100_000) * 100_000
            value > 1_000_000 -> (value / 25_000) * 25_000
            value > 100_000 -> (value / 5_000) * 5_000
            value > 10_000 -> (value / 500) * 500
            else -> (value / 100) * 100
        }
    }
}
