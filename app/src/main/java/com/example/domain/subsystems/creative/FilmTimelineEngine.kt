package com.example.domain.subsystems.creative

import kotlin.math.roundToInt

/**
 * Universal Production Phase (14 Universal Phases)
 * Standard framework unifying Live Action & Animation major studio pipelines.
 */
enum class UniversalPhase(
    val phaseNumber: Int,
    val phaseName: String,
    val icon: String,
    val description: String
) {
    CONCEPT_IP_DEV(1, "Concept & IP Development", "💡", "Ideasi konsep, IP/hak cipta, dan riset awal"),
    STORY_SCRIPT_DEV(2, "Story & Script Development", "📝", "Pengembangan cerita naskah & struktur skenario"),
    FINANCING_GREENLIGHT(3, "Financing / Packaging / Greenlight", "🤝", "Budgeting, casting sutradara & persetujuan studio"),
    CREATIVE_DEV(4, "Creative Development", "🎨", "Desain visual, world building & storyboard reel"),
    PRE_PRODUCTION(5, "Pre-Production", "📋", "Kru, perlengkapan, set, lokasi & penjadwalan syuting"),
    ASSET_PREPARATION(6, "Asset / Production Preparation", "🛠️", "Desain karakter, environment, modeling & rigging 3D"),
    PRINCIPAL_PRODUCTION(7, "Principal Production", "🎥", "Syuting utama (Live Action) atau Rekaman & Animasi utama"),
    EDITORIAL_ASSEMBLY(8, "Editorial / Assembly", "✂️", "Perakitan footage, story reel cut & picture lock"),
    TECHNICAL_PRODUCTION(9, "VFX / Animation / Technical Production", "✨", "VFX CGI, simulasi FX & pencahayaan/rendering"),
    SOUND_MUSIC(10, "Sound & Music", "🎵", "Scoring musik orkestra, ADR, foley & final audio mix"),
    POST_FINISHING(11, "Post / Finishing", "🎞️", "Color grading, compositing & visual online finishing"),
    TEST_REVISION(12, "Testing / Revision / Reshoot", "🔍", "Test screening penonton, revisi adegan & reshoot pickup"),
    MASTER_QC_LOCALIZATION(13, "Final Master / QC / Localization", "📦", "QC studio internal, mastering DCI & dubbing global"),
    MARKETING_RELEASE(14, "Marketing / Distribution / Release", "🚀", "Kampanye trailer, press premiere & rilis bioskop/OTT");

    val codeLabel: String get() = String.format("%02d %s", phaseNumber, phaseName)
}

/**
 * Specific Production Stage according to Disney/Marvel/Warner/Sony tentpole standard.
 */
data class TimelineStage(
    val stageNumber: Int,
    val name: String,
    val percentage: Int, // Exact % of total duration
    val sequentialStart: Float, // Cumulative start ratio 0.0 .. 1.0
    val sequentialEnd: Float, // Cumulative end ratio 0.0 .. 1.0
    val overlapStart: Float, // Realistic overlap start window
    val overlapEnd: Float, // Realistic overlap end window
    val universalPhase: UniversalPhase,
    val icon: String,
    val notes: String = ""
)

/**
 * Current timeline snapshot for a movie project.
 */
data class TimelineSnapshot(
    val isAnimation: Boolean,
    val totalMonths: Int,
    val elapsedMonths: Int,
    val remainingMonths: Int,
    val progressPercent: Int,
    val currentStage: TimelineStage,
    val activeConcurrentStages: List<TimelineStage>,
    val universalPhase: UniversalPhase,
    val displayLabel: String,
    val phaseDetailText: String
)

/**
 * Dedicated Engine for Film Studio Production Timelines.
 * Implements exact percentage allocations and realistic pipeline overlaps
 * for Live Action (16 stages) & Animation (20 stages) within the 14 Universal Phases.
 */
object FilmTimelineEngine {

    // ==========================================
    // 🎬 LIVE ACTION TENTPOLE (16 STAGES, 100%)
    // ==========================================
    val LIVE_ACTION_STAGES: List<TimelineStage> = listOf(
        TimelineStage(1, "Concept / Idea Development", 4, 0.00f, 0.04f, 0.00f, 0.06f, UniversalPhase.CONCEPT_IP_DEV, "💡", "Ide dasar & eksplorasi konsep awal"),
        TimelineStage(2, "IP / Rights / Legal Development", 3, 0.04f, 0.07f, 0.02f, 0.08f, UniversalPhase.CONCEPT_IP_DEV, "⚖️", "Akuisisi lisensi IP & kliring hukum"),
        TimelineStage(3, "Story & Screenplay Development", 8, 0.07f, 0.15f, 0.05f, 0.17f, UniversalPhase.STORY_SCRIPT_DEV, "📝", "Penulisan draft naskah & revisi skenario"),
        TimelineStage(4, "Packaging & Financing", 5, 0.15f, 0.20f, 0.12f, 0.22f, UniversalPhase.FINANCING_GREENLIGHT, "💼", "Pendanaan, pitching produser & attach aktor"),
        TimelineStage(5, "Greenlight & Production Planning", 3, 0.20f, 0.23f, 0.18f, 0.25f, UniversalPhase.FINANCING_GREENLIGHT, "🟢", "Persetujuan lampu hijau dewan eksekutif studio"),
        TimelineStage(6, "Pre-Production", 15, 0.23f, 0.38f, 0.20f, 0.40f, UniversalPhase.PRE_PRODUCTION, "📋", "Desain set, lokasi, kostum & jadwal kru"),
        TimelineStage(7, "Principal Photography", 8, 0.38f, 0.46f, 0.35f, 0.48f, UniversalPhase.PRINCIPAL_PRODUCTION, "🎥", "Syuting fisik utama di panggung/lapangan"),
        TimelineStage(8, "Editorial / Assembly", 5, 0.46f, 0.51f, 0.40f, 0.58f, UniversalPhase.EDITORIAL_ASSEMBLY, "✂️", "Pemotongan adegan berjalan paralel saat syuting"),
        TimelineStage(9, "VFX / CGI Production", 20, 0.51f, 0.71f, 0.45f, 0.76f, UniversalPhase.TECHNICAL_PRODUCTION, "✨", "Pengerjaan efek visual masif & simulasi CG"),
        TimelineStage(10, "Reshoots / Pickups", 3, 0.71f, 0.74f, 0.65f, 0.75f, UniversalPhase.TEST_REVISION, "🎬", "Syuting ulang adegan kunci & pickup shots"),
        TimelineStage(11, "Sound / Music / ADR / Foley", 6, 0.74f, 0.80f, 0.62f, 0.85f, UniversalPhase.SOUND_MUSIC, "🎵", "Perekaman skor orkestra, efek suara & dialog dub"),
        TimelineStage(12, "Color / Online / Finishing", 4, 0.80f, 0.84f, 0.76f, 0.88f, UniversalPhase.POST_FINISHING, "🎨", "Color grading DI, conform & finishing visual"),
        TimelineStage(13, "Test Screening / Refinement", 3, 0.84f, 0.87f, 0.82f, 0.90f, UniversalPhase.TEST_REVISION, "🔍", "Uji coba screening audiens fokus studio"),
        TimelineStage(14, "Final QC / Mastering / Localization", 3, 0.87f, 0.90f, 0.85f, 0.95f, UniversalPhase.MASTER_QC_LOCALIZATION, "📦", "Mastering DCP bioskop & lokalisasi multi-bahasa"),
        TimelineStage(15, "Marketing / Publicity", 8, 0.90f, 0.98f, 0.68f, 0.99f, UniversalPhase.MARKETING_RELEASE, "📢", "Kampanye trailer, press junket & media blitz"),
        TimelineStage(16, "Distribution / Release Preparation", 3, 0.98f, 1.00f, 0.92f, 1.00f, UniversalPhase.MARKETING_RELEASE, "🚀", "Pengiriman DCP ke bioskop & persiapan premiere")
    )

    // ==========================================
    // 🎨 ANIMATED FEATURE (20 STAGES, 100%)
    // ==========================================
    val ANIMATION_STAGES: List<TimelineStage> = listOf(
        TimelineStage(1, "Concept / Original Idea / IP", 4, 0.00f, 0.04f, 0.00f, 0.06f, UniversalPhase.CONCEPT_IP_DEV, "💡", "Penciptaan ide orisinal & eksplorasi dunia animasi"),
        TimelineStage(2, "Story Development", 12, 0.04f, 0.16f, 0.03f, 0.18f, UniversalPhase.STORY_SCRIPT_DEV, "📖", "Rapat penulisan cerita ruang kreatif Pixar/Disney style"),
        TimelineStage(3, "Screenplay / Story Structure", 6, 0.16f, 0.22f, 0.12f, 0.24f, UniversalPhase.STORY_SCRIPT_DEV, "📝", "Penyusunan naskah adegan demi adegan lengkap"),
        TimelineStage(4, "Storyboard / Story Reel", 10, 0.22f, 0.32f, 0.18f, 0.34f, UniversalPhase.CREATIVE_DEV, "🎞️", "Sketsa storyboard beruntun & animatic story reel"),
        TimelineStage(5, "Greenlight / Production Planning", 3, 0.32f, 0.35f, 0.28f, 0.36f, UniversalPhase.FINANCING_GREENLIGHT, "🟢", "Studio persetujuan produksi animasi penuh"),
        TimelineStage(6, "Visual Development", 8, 0.35f, 0.43f, 0.30f, 0.46f, UniversalPhase.CREATIVE_DEV, "🎨", "Konsep seni, palet warna visual & lighting mood"),
        TimelineStage(7, "Character / Environment Design", 8, 0.43f, 0.51f, 0.38f, 0.54f, UniversalPhase.ASSET_PREPARATION, "👤", "Model sheet karakter & arsitektur environment"),
        TimelineStage(8, "Modeling / Rigging / LookDev", 9, 0.51f, 0.60f, 0.45f, 0.64f, UniversalPhase.ASSET_PREPARATION, "📐", "Pembuatan jala 3D, sistem tulang rigging & tekstur"),
        TimelineStage(9, "Voice Recording / Performance", 3, 0.60f, 0.63f, 0.48f, 0.66f, UniversalPhase.PRINCIPAL_PRODUCTION, "🎙️", "Perekaman suara pengisi karakter utama & scratch track"),
        TimelineStage(10, "Layout / Previsualization", 5, 0.63f, 0.68f, 0.55f, 0.70f, UniversalPhase.PRE_PRODUCTION, "📐", "Tata letak kamera 3D & koreografi adegan"),
        TimelineStage(11, "Animation", 15, 0.68f, 0.83f, 0.60f, 0.86f, UniversalPhase.PRINCIPAL_PRODUCTION, "🖥️", "Pemberian gerak dan akting karakter 3D frame-by-frame"),
        TimelineStage(12, "FX / Simulation / Crowd", 4, 0.83f, 0.87f, 0.75f, 0.90f, UniversalPhase.TECHNICAL_PRODUCTION, "💥", "Simulasi partikel air, kain, rambut & crowd AI"),
        TimelineStage(13, "Lighting / Rendering", 5, 0.87f, 0.92f, 0.80f, 0.94f, UniversalPhase.TECHNICAL_PRODUCTION, "💡", "Pencahayaan dramatis & komputasi render farm besar"),
        TimelineStage(14, "Compositing / Final Image", 2, 0.92f, 0.94f, 0.86f, 0.95f, UniversalPhase.POST_FINISHING, "🖼️", "Penggabungan multi-layer render menjadi citra final"),
        TimelineStage(15, "Editorial / Final Refinement", 3, 0.94f, 0.97f, 0.88f, 0.97f, UniversalPhase.EDITORIAL_ASSEMBLY, "✂️", "Pemangkasan tempo akhir & fine tune cerita"),
        TimelineStage(16, "Music / Sound / Final Mix", 3, 0.97f, 1.00f, 0.82f, 0.98f, UniversalPhase.SOUND_MUSIC, "🎼", "Scoring orkestra, lagu tema & tata audio surround"),
        TimelineStage(17, "Test Screening / Refinement", 2, 0.94f, 0.96f, 0.90f, 0.97f, UniversalPhase.TEST_REVISION, "🔍", "Screening tertutup anak & keluarga evaluasi tanggapan"),
        TimelineStage(18, "Final QC / Mastering / Localization", 2, 0.96f, 0.98f, 0.92f, 0.99f, UniversalPhase.MASTER_QC_LOCALIZATION, "📦", "Master render 4K HDR & dubbing berbagai bahasa dunia"),
        TimelineStage(19, "Marketing / Publicity", 5, 0.92f, 0.97f, 0.78f, 1.00f, UniversalPhase.MARKETING_RELEASE, "📢", "Kampanye promosi merchandise, trailer & media partner"),
        TimelineStage(20, "Distribution / Release", 1, 0.99f, 1.00f, 0.96f, 1.00f, UniversalPhase.MARKETING_RELEASE, "🚀", "Rilis bioskop global dan peluncuran theatrical")
    )

    /**
     * Calculates realistic randomized total duration (months) for a movie project.
     * Benchmarks:
     * - Live Action tentpole baseline ~48 months
     * - Animation tentpole baseline ~54 months
     * Influenced dynamically by format, budget, focus, and studio infrastructure speedups.
     */
    /**
     * Estimates the planned duration in months for scheduling and visual forecasting.
     */
    fun estimateProjectDuration(
        isAnimation: Boolean,
        filmFormat: String = "Feature Film",
        budget: Long = 0L,
        productionFocus: String? = "REGULER",
        hasVirtualProd: Boolean = false,
        hasAnimPipeline: Boolean = false,
        hasRenderFarm: Boolean = false,
        hasPostPipeline: Boolean = false
    ): Int {
        val isShort = filmFormat.equals("Short Film", ignoreCase = true)
        val avgBase = if (isAnimation) {
            if (isShort) 20 else 52
        } else {
            if (isShort) 6 else 46
        }
        val budgetInTenMillions = (budget / 10_000_000L).toInt()
        val budgetBonus = minOf(budgetInTenMillions, 8)
        val focusBonus = when (productionFocus?.uppercase()) {
            "MAHAKARYA" -> 8
            "KUALITAS" -> 4
            else -> 0
        }
        var speedup = 0
        if (hasVirtualProd) speedup += 3
        if (hasAnimPipeline) speedup += 4
        if (hasRenderFarm) speedup += if (isAnimation) 6 else 3
        if (hasPostPipeline) speedup += 2

        val total = avgBase + budgetBonus + focusBonus - speedup
        val minLimit = if (isShort) (if (isAnimation) 8 else 3) else (if (isAnimation) 28 else 20)
        return maxOf(minLimit, total)
    }

    fun calculateTotalDurationMonths(
        isAnimation: Boolean,
        filmFormat: String = "Feature Film",
        budget: Long = 0L,
        productionFocus: String? = "REGULER",
        hasVirtualProd: Boolean = false,
        hasAnimPipeline: Boolean = false,
        hasRenderFarm: Boolean = false,
        hasPostPipeline: Boolean = false,
        targetMonths: Int? = null
    ): Int {
        if (targetMonths != null && targetMonths > 0) {
            return targetMonths
        }
        val isShort = filmFormat.equals("Short Film", ignoreCase = true)
        
        val baseMonths = if (isAnimation) {
            if (isShort) (16..24).random() else (46..58).random() // ±54 months avg for Animated feature
        } else {
            if (isShort) (4..8).random() else (40..54).random() // ±48 months avg for Live-Action feature
        }

        // Scale adjustments based on budget scale (e.g. mega blockbusters > $50M-$100M need larger pipeline)
        val budgetInTenMillions = (budget / 10_000_000L).toInt()
        val budgetBonus = minOf(budgetInTenMillions, 8)

        // Production focus additions
        val focusBonus = when (productionFocus?.uppercase()) {
            "MAHAKARYA" -> (6..10).random()
            "KUALITAS" -> (3..6).random()
            else -> 0
        }

        // Studio technological pipeline speedups
        var speedup = 0
        if (hasVirtualProd) speedup += 3
        if (hasAnimPipeline) speedup += 4
        if (hasRenderFarm) speedup += if (isAnimation) 6 else 3
        if (hasPostPipeline) speedup += 2

        val total = baseMonths + budgetBonus + focusBonus - speedup
        val minLimit = if (isShort) (if (isAnimation) 8 else 3) else (if (isAnimation) 28 else 20)
        return maxOf(minLimit, total)
    }

    /**
     * Retrieves the complete timeline progress snapshot for a project.
     */
    fun getProgress(
        isAnimation: Boolean,
        totalMonths: Int,
        remainingMonths: Int
    ): TimelineSnapshot {
        val stages = if (isAnimation) ANIMATION_STAGES else LIVE_ACTION_STAGES
        val safeTotal = maxOf(1, totalMonths)
        val safeRemaining = remainingMonths.coerceIn(0, safeTotal)
        val elapsed = (safeTotal - safeRemaining).coerceAtLeast(0)
        
        // Progress ratio from 0.0 (just started) to 1.0 (finished)
        val ratio = (elapsed.toFloat() / safeTotal.toFloat()).coerceIn(0.0f, 1.0f)
        val progressPercent = (ratio * 100f).roundToInt().coerceIn(0, 100)

        // Find primary sequential stage
        val currentStage = stages.find { ratio >= it.sequentialStart && ratio < it.sequentialEnd }
            ?: stages.last()

        // Find all active concurrent departments due to realistic pipeline overlaps!
        val activeParallel = stages.filter {
            ratio >= it.overlapStart && ratio <= it.overlapEnd && it != currentStage
        }

        val universalPhase = currentStage.universalPhase
        val stageNum = currentStage.stageNumber
        val totalStagesCount = stages.size

        val displayLabel = "${currentStage.icon} ${currentStage.name} ($stageNum/$totalStagesCount)"
        val phaseDetail = buildString {
            append("${currentStage.name} (${currentStage.percentage}% durasi)")
            if (activeParallel.isNotEmpty()) {
                val parallelNames = activeParallel.take(2).joinToString(", ") { it.name }
                append(" | Dept Paralel: $parallelNames")
            }
        }

        return TimelineSnapshot(
            isAnimation = isAnimation,
            totalMonths = safeTotal,
            elapsedMonths = elapsed,
            remainingMonths = safeRemaining,
            progressPercent = progressPercent,
            currentStage = currentStage,
            activeConcurrentStages = activeParallel,
            universalPhase = universalPhase,
            displayLabel = displayLabel,
            phaseDetailText = phaseDetail
        )
    }

    /**
     * Returns the stage list for the specified studio type.
     */
    fun getStagesForType(isAnimation: Boolean): List<TimelineStage> {
        return if (isAnimation) ANIMATION_STAGES else LIVE_ACTION_STAGES
    }
}
