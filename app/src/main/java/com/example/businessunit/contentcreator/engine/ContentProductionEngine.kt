package com.example.businessunit.contentcreator.engine

import com.example.businessunit.contentcreator.model.ContentScoreEvaluation
import com.example.businessunit.contentcreator.model.ContentType
import com.example.businessunit.contentcreator.model.ContentWork
import com.example.businessunit.contentcreator.model.CreativeFocus
import com.example.businessunit.contentcreator.model.ProductionHouseOffer
import kotlin.math.ln
import kotlin.math.pow
import kotlin.random.Random

object ContentProductionEngine {

    fun calculateDetailedScore(
        budget: Long,
        channelLevel: Int,
        employees: Int,
        contentType: ContentType,
        creativeFocus: CreativeFocus? = null,
        isStudioCoProd: Boolean = false,
        studioLevel: Int = 1
    ): ContentScoreEvaluation {
        // 1. Technical Score (Budget & Tim Produksi) - Max 30
        val baseBudget = budget.coerceAtLeast(1000L).toDouble()
        val budgetFactor = (ln(baseBudget / 1000.0) / ln(500.0)).coerceIn(0.0, 1.0)
        val employeeFactor = (employees / 20.0).coerceIn(0.0, 1.0)
        val studioBonus = if (isStudioCoProd) 0.15 + (studioLevel * 0.01) else 0.0

        val technicalRaw = ((budgetFactor * 0.70 + employeeFactor * 0.30 + studioBonus) * 30.0).toInt()
        val technicalScore = technicalRaw.coerceIn(5, 30)

        // 2. Concept & Narrative Score (Level & Sinergi Fokus) - Max 35
        val channelExp = (channelLevel / 100.0).coerceIn(0.0, 1.0)
        val focusSynergy = when {
            creativeFocus == CreativeFocus.STORY_NARRATIVE && (contentType == ContentType.SHORT_FILM || contentType == ContentType.FEATURE_FILM) -> 1.25
            creativeFocus == CreativeFocus.INVESTIGATION && (contentType == ContentType.DOCUMENTARY || contentType == ContentType.DEEP_DIVE_ESSAY) -> 1.25
            creativeFocus == CreativeFocus.CINEMATOGRAPHY && (isStudioCoProd || contentType == ContentType.FEATURE_FILM) -> 1.30
            creativeFocus == CreativeFocus.VIRAL_HOOK -> 1.15
            else -> 1.0
        }

        val conceptRaw = ((channelExp * 0.50 + 0.40) * focusSynergy * 35.0).toInt()
        val conceptScore = conceptRaw.coerceIn(8, 35)

        // 3. Audience & Virality Response - Max 35
        val luckRoll = Random.nextDouble(0.40, 1.0)
        val audienceRaw = (luckRoll * 35.0).toInt()
        val audienceScore = audienceRaw.coerceIn(5, 35)

        val totalScore = (technicalScore + conceptScore + audienceScore).coerceIn(10, 100)

        val (verdictTitle, verdictDesc) = when {
            totalScore >= 90 -> Pair(
                "🔥 KARYA MASTERPIECE VIRAL!",
                "Karya ini meledak di seluruh jagat maya! Dialog dikutip di mana-mana, rating kritikus tembus 9.5/10, dan Production House global berebut lisensi eksklusif."
            )
            totalScore >= 75 -> Pair(
                "✨ SANGAT SUKSES & MENDAPAT PUJIAN",
                "Kualitas produksi sinematik yang solid dengan hook cerita memukau. Menjadi perbincangan hangat di komunitas film & media."
            )
            totalScore >= 60 -> Pair(
                "👍 PERFORMA BAIK & MENGUNTUNGKAN",
                "Mendapatkan penerimaan positif dari audiens setia. Kualitas stabil dan layak ditawarkan ke mitra lisensi."
            )
            totalScore >= 40 -> Pair(
                "⚖️ RESPON CAMPURAN (RATA-RATA)",
                "Beberapa bagian menuai pujian teknis namun pacing dan naskah terasa kurang tajam bagi sebagian penonton."
            )
            else -> Pair(
                "⚠️ GAGAL VIRAL (FLOP)",
                "Eksekusi kurang maksimal dan terhambat algoritma. Karya ini menjadi pelajaran berharga untuk produksi berikutnya."
            )
        }

        return ContentScoreEvaluation(
            score = totalScore,
            verdictTitle = verdictTitle,
            verdictDescription = verdictDesc,
            technicalScore = technicalScore,
            conceptScore = conceptScore,
            audienceScore = audienceScore
        )
    }

    private val MAJOR_PH_NAMES = listOf(
        "Universal Stream PH", "Warner Max Studios", "Netflix Indie Lab", "Paramount Plus Global",
        "A24 Creative Vault", "Lionsgate Media", "Sony Pictures Digital", "Amazon Prime Originals"
    )

    fun generateTargetedOffer(work: ContentWork): ProductionHouseOffer {
        val score = work.engagementScore ?: 50
        val phName = MAJOR_PH_NAMES.random()
        val scoreMultiplier = (score / 50.0).pow(1.8)

        val lumpSum = (work.budget * (1.2 + scoreMultiplier * 0.8)).toLong()
        val durationMonths = listOf(12, 18, 24, 36).random()
        val monthlyRoyalty = ((work.budget * 0.08 * scoreMultiplier)).toLong().coerceAtLeast(500L)
        val upfront = (monthlyRoyalty * 3)

        val pitch = when {
            score >= 85 -> "Kami sangat terpukau dengan kualitas naskah dan sinematografi karya '${work.title}'. Kami menawarkan kontrak lisensi eksklusif jangka panjang."
            score >= 65 -> "Karya '${work.title}' memiliki potensi distribusi streaming yang kuat. Kami siap bermitra dengan skema royalti bulanan."
            else -> "Kami tertarik mengakuisisi hak tayang karya '${work.title}' untuk katalog kurasi niche kami."
        }

        return ProductionHouseOffer(
            phName = phName,
            phTier = if (score >= 80) "Major Global PH" else "Digital Streaming Network",
            contentId = work.id,
            contentTitle = work.title,
            contentType = work.type,
            engagementScore = score,
            lumpSumOffer = lumpSum,
            royaltyUpfront = upfront,
            monthlyRoyalty = monthlyRoyalty,
            contractDurationMonths = durationMonths,
            pitchMessage = pitch
        )
    }
}
