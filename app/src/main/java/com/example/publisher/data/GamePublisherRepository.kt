package com.example.publisher.data

import com.example.publisher.engine.GamePublisherEngine
import com.example.publisher.engine.LaunchFinancialResult
import com.example.publisher.model.CreativeAdjustmentType
import com.example.publisher.model.FundingTier
import com.example.publisher.model.GameGenre
import com.example.publisher.model.GameProject
import com.example.publisher.model.IndieStudio
import com.example.publisher.model.PitchStatus
import com.example.publisher.model.TargetPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import kotlin.random.Random

/**
 * Monthly simulation tick result summary for GamePublisherTickSubscriber integration.
 */
data class MonthlyPublisherTickResult(
    val totalSalesRevenue: Long,
    val maintenanceExpenses: Long,
    val netProfit: Long,
    val logSummaries: List<String>
)

/**
 * Repository and state holder for the Indie Game Publisher & Incubator module.
 */
class GamePublisherRepository {

    companion object {
        @Volatile
        private var instance: GamePublisherRepository? = null

        fun getInstance(): GamePublisherRepository {
            return instance ?: synchronized(this) {
                instance ?: GamePublisherRepository().also { instance = it }
            }
        }
    }

    private val _pitches = MutableStateFlow<List<IndieStudio>>(emptyList())
    val pitches: StateFlow<List<IndieStudio>> = _pitches.asStateFlow()

    private val _activeProjects = MutableStateFlow<List<GameProject>>(emptyList())
    val activeProjects: StateFlow<List<GameProject>> = _activeProjects.asStateFlow()

    private val _releasedProjects = MutableStateFlow<List<GameProject>>(emptyList())
    val releasedProjects: StateFlow<List<GameProject>> = _releasedProjects.asStateFlow()

    private val _publisherTreasury = MutableStateFlow(2_500_000L)
    val publisherTreasury: StateFlow<Long> = _publisherTreasury.asStateFlow()

    private val _publisherReputation = MutableStateFlow(78)
    val publisherReputation: StateFlow<Int> = _publisherReputation.asStateFlow()

    private val _lifetimePublisherProfit = MutableStateFlow(491_000L)
    val lifetimePublisherProfit: StateFlow<Long> = _lifetimePublisherProfit.asStateFlow()

    private val _lastLaunchResult = MutableStateFlow<LaunchFinancialResult?>(null)
    val lastLaunchResult: StateFlow<LaunchFinancialResult?> = _lastLaunchResult.asStateFlow()

    private val _currentSimulationMonth = MutableStateFlow(12)
    val currentSimulationMonth: StateFlow<Int> = _currentSimulationMonth.asStateFlow()

    init {
        seedInitialPublisherData()
    }

    private fun seedInitialPublisherData() {
        val studioA = IndieStudio(
            id = "studio-1",
            name = "Pixel Forge Collective",
            pitchTitle = "Chrono Rogue: Temporal Shift",
            logline = "A fast-paced tactical roguelike where rewinding time creates clone echoes that execute your past combos.",
            talentScore = 84.0,
            preferredGenre = GameGenre.ROGUELIKE,
            currentPitchStatus = PitchStatus.PENDING_REVIEW,
            teamSize = 5,
            foundedYear = 2022,
            requestedTier = FundingTier.STANDARD,
            requestedRevenueShare = 0.55
        )

        val studioB = IndieStudio(
            id = "studio-2",
            name = "Moonlit Meadow Interactive",
            pitchTitle = "Spirited Tea Haven",
            logline = "A cozy supernatural tea-brewing sim set in a magical forest train station where wandering yokai share ancient folklore.",
            talentScore = 91.0,
            preferredGenre = GameGenre.COZY_SIM,
            currentPitchStatus = PitchStatus.PENDING_REVIEW,
            teamSize = 3,
            foundedYear = 2021,
            requestedTier = FundingTier.BOOTSTRAP,
            requestedRevenueShare = 0.65
        )

        val studioC = IndieStudio(
            id = "studio-3",
            name = "Obsidian Abyss Studios",
            pitchTitle = "Eclipse of the Void",
            logline = "Dark Gothic soulslike featuring reactive parry mechanics and a colossal interconnected Victorian metropolis.",
            talentScore = 95.0,
            preferredGenre = GameGenre.SOULSLIKE,
            currentPitchStatus = PitchStatus.PENDING_REVIEW,
            teamSize = 14,
            foundedYear = 2019,
            requestedTier = FundingTier.TRIPLE_I,
            requestedRevenueShare = 0.50
        )

        _pitches.value = listOf(studioA, studioB, studioC)

        // Pre-seeded Active Project
        val activeDev = GameProject(
            id = "proj-active-1",
            title = "Aetheria: Wind & Iron",
            genre = GameGenre.ACTION_ADVENTURE,
            studioId = "studio-pre-1",
            studioName = "Windforge Games",
            fundingTier = FundingTier.STANDARD,
            targetPlatforms = setOf(TargetPlatform.PC_STEAM, TargetPlatform.NINTENDO_SWITCH),
            currentProgress = 65.0f,
            totalBudget = 345_000L,
            devTimeMonths = 10,
            currentMonthInDev = 6,
            bugRiskScore = 0.0,
            hypeScore = 63.0,
            qaInvestment = 160_000L,
            publisherRevenueShare = 0.60,
            posterImageUrl = "https://images.unsplash.com/photo-1579373903781-fd5c0c30c4cd?q=80&w=600&auto=format&fit=crop"
        )
        _activeProjects.value = listOf(activeDev)

        // Pre-seeded Released Project
        val releasedTitle = GameProject(
            id = "proj-released-1",
            title = "Neon Katana: Cyberfall",
            genre = GameGenre.CYBERPUNK,
            studioId = "studio-pre-2",
            studioName = "HyperGlitch Lab",
            fundingTier = FundingTier.BOOTSTRAP,
            targetPlatforms = setOf(TargetPlatform.PC_STEAM, TargetPlatform.PC_EPIC),
            currentProgress = 100.0f,
            totalBudget = 90_000L,
            devTimeMonths = 6,
            currentMonthInDev = 6,
            isReleased = true,
            releaseMonth = 1,
            lifetimeRevenue = 1_440_000L,
            totalSales = 69_600L,
            activePlayers = 4_300L,
            reviewScore = 88,
            publisherRevenueShare = 0.60,
            publisherLifetimeProfit = 863_100L,
            monthlyRevenue = 32_000L,
            patchCount = 2,
            posterImageUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?q=80&w=600&auto=format&fit=crop"
        )
        _releasedProjects.value = listOf(releasedTitle)
    }

    /**
     * Refreshes the incubation talent pool with newly submitted pitch proposals.
     */
    fun refreshPitches() {
        val studioNames = listOf(
            "Grim Ember Collective", "Voxel Rift Games", "Starlight Foundry",
            "Midnight Circuit", "Aetherium Dynamics", "Celestial Loom", "Neon Mirage"
        )
        val titles = listOf(
            "Starlight Expressways", "Rune Protocol: Protocol 7", "Wanderer of the Astral Sea",
            "Underground Syndicate", "Whispering Hollows", "Titanfall Forge"
        )
        val loglines = listOf(
            "Deep tactical dungeon crawler with elemental spell mixing and permadeath squads.",
            "Cozy slice-of-life bakery simulator where you bake enchanted pastries for mythological heroes.",
            "Cyberpunk stealth-infiltration game featuring neural hacking and high-stakes corporate espionage.",
            "Stylized high-speed hovercraft racer with modular weapon attachments and dynamic track deform."
        )

        val newPitches = (1..3).map {
            val tier = FundingTier.entries.random()
            val genre = GameGenre.entries.random()
            val studioName = "${studioNames.random()} #${Random.nextInt(10, 99)}"
            IndieStudio(
                name = studioName,
                pitchTitle = titles.random(),
                logline = loglines.random(),
                talentScore = Random.nextDouble(65.0, 96.0),
                preferredGenre = genre,
                currentPitchStatus = PitchStatus.PENDING_REVIEW,
                teamSize = Random.nextInt(2, 16),
                foundedYear = 2020 + Random.nextInt(0, 4),
                requestedTier = tier,
                requestedRevenueShare = Random.nextDouble(0.45, 0.70)
            )
        }

        _pitches.update { (newPitches + it).distinctBy { s -> s.name }.take(6) }
    }

    /**
     * Accepts a pitch and creates an active development pipeline.
     */
    fun acceptPitch(
        studioId: String,
        chosenTier: FundingTier,
        agreedRevenueShare: Double,
        platforms: Set<TargetPlatform>
    ): Boolean {
        val studio = _pitches.value.find { it.id == studioId } ?: return false

        if (_publisherTreasury.value < chosenTier.baseBudget) {
            return false // Insufficient funds
        }

        _publisherTreasury.update { it - chosenTier.baseBudget }

        val newProject = GameProject(
            title = studio.pitchTitle,
            genre = studio.preferredGenre,
            studioId = studio.id,
            studioName = studio.name,
            fundingTier = chosenTier,
            targetPlatforms = platforms.ifEmpty { setOf(TargetPlatform.PC_STEAM) },
            totalBudget = chosenTier.baseBudget,
            devTimeMonths = chosenTier.baseDevMonths,
            currentMonthInDev = 0,
            currentProgress = 0.0f,
            bugRiskScore = 12.0,
            hypeScore = 30.0 * chosenTier.talentThresholdBonus,
            publisherRevenueShare = agreedRevenueShare,
            posterImageUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?q=80&w=600&auto=format&fit=crop"
        )

        _activeProjects.update { it + newProject }
        _pitches.update { list -> list.filter { it.id != studioId } }
        return true
    }

    /**
     * Creates an in-house project developed directly by the publisher's internal studio.
     */
    fun createInHouseProject(
        title: String,
        genre: GameGenre,
        fundingTier: FundingTier,
        platforms: Set<TargetPlatform>,
        publisherStudioName: String = "Vanguard Internal Studio"
    ): Boolean {
        val newProj = GamePublisherEngine.createInHouseProject(
            title = title,
            genre = genre,
            fundingTier = fundingTier,
            targetPlatforms = platforms,
            publisherStudioName = publisherStudioName
        )

        if (_publisherTreasury.value < newProj.totalBudget) {
            return false // Insufficient treasury
        }

        _publisherTreasury.update { it - newProj.totalBudget }
        _activeProjects.update { it + newProj }
        return true
    }

    /**
     * Rejects an indie studio pitch.
     */
    fun rejectPitch(studioId: String) {
        _pitches.update { list -> list.filter { it.id != studioId } }
    }

    /**
     * Toggles a target platform on an active project and recalculates porting bug risk.
     */
    fun togglePlatform(projectId: String, platform: TargetPlatform) {
        _activeProjects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val currentPlatforms = proj.targetPlatforms.toMutableSet()
                    if (currentPlatforms.contains(platform)) {
                        if (currentPlatforms.size > 1) {
                            currentPlatforms.remove(platform)
                        }
                    } else {
                        currentPlatforms.add(platform)
                    }

                    val updatedRisk = GamePublisherEngine.calculatePortingBugRisk(
                        platforms = currentPlatforms,
                        baseBudget = proj.totalBudget,
                        qaInvestment = proj.qaInvestment,
                        currentBugRisk = proj.bugRiskScore
                    )

                    proj.copy(
                        targetPlatforms = currentPlatforms,
                        bugRiskScore = updatedRisk
                    )
                } else proj
            }
        }
    }

    /**
     * Applies a creative direction adjustment during development.
     */
    fun applyAdjustment(projectId: String, adjustment: CreativeAdjustmentType): Boolean {
        if (_publisherTreasury.value < adjustment.costBonus) return false

        _publisherTreasury.update { it - adjustment.costBonus }
        _activeProjects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    GamePublisherEngine.applyCreativeAdjustment(proj, adjustment)
                } else proj
            }
        }
        return true
    }

    /**
     * Directly injects emergency QA budget to suppress porting bug risk.
     */
    fun investQaBudget(projectId: String, amount: Long): Boolean {
        if (_publisherTreasury.value < amount) return false

        _publisherTreasury.update { it - amount }
        _activeProjects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    val newQa = proj.qaInvestment + amount
                    val newRisk = GamePublisherEngine.calculatePortingBugRisk(
                        platforms = proj.targetPlatforms,
                        baseBudget = proj.totalBudget,
                        qaInvestment = newQa,
                        currentBugRisk = proj.bugRiskScore - (amount / 3_000.0)
                    )
                    proj.copy(
                        qaInvestment = newQa,
                        bugRiskScore = kotlin.math.max(0.0, newRisk)
                    )
                } else proj
            }
        }
        return true
    }

    /**
     * Launches an active game worldwide.
     */
    fun launchGame(projectId: String): LaunchFinancialResult? {
        val proj = _activeProjects.value.find { it.id == projectId } ?: return null
        val talentScore = if (proj.isInHouseProject) 88.0 else 80.0

        val launchResult = GamePublisherEngine.calculateLaunchFinancials(proj, talentScore)

        val releasedProject = proj.copy(
            isReleased = true,
            releaseMonth = _currentSimulationMonth.value,
            reviewScore = launchResult.reviewScore,
            lifetimeRevenue = launchResult.finalRevenue,
            totalSales = launchResult.unitsSold,
            activePlayers = launchResult.initialActivePlayers,
            publisherLifetimeProfit = launchResult.publisherProfit,
            monthlyRevenue = (launchResult.finalRevenue * 0.35).toLong(),
            currentProgress = 100.0f
        )

        _publisherTreasury.update { it + launchResult.publisherProfit }
        _lifetimePublisherProfit.update { it + launchResult.publisherProfit }

        val repDelta = if (launchResult.reviewScore >= 85) +4 else if (launchResult.reviewScore >= 70) +1 else -3
        _publisherReputation.update { kotlin.math.max(10, kotlin.math.min(100, it + repDelta)) }

        _lastLaunchResult.value = launchResult
        _activeProjects.update { list -> list.filter { it.id != projectId } }
        _releasedProjects.update { list -> listOf(releasedProject) + list }

        return launchResult
    }

    fun dismissLaunchResult() {
        _lastLaunchResult.value = null
    }

    fun deployPatch(projectId: String, cost: Long = 20_000L): Boolean {
        if (_publisherTreasury.value < cost) return false

        _publisherTreasury.update { it - cost }
        _releasedProjects.update { list ->
            list.map { proj ->
                if (proj.id == projectId) {
                    GamePublisherEngine.deployPostLaunchPatch(proj, cost)
                } else proj
            }
        }
        return true
    }

    /**
     * Injects capital into the publisher's treasury.
     */
    fun injectCapital(amount: Long) {
        if (amount > 0) {
            _publisherTreasury.update { it + amount }
        }
    }

    /**
     * Withdraws cash/dividend from the publisher's treasury.
     */
    fun withdrawCapital(amount: Long): Boolean {
        if (amount <= 0 || _publisherTreasury.value < amount) return false
        _publisherTreasury.update { it - amount }
        return true
    }

    /**
     * Calculates total business valuation for liquidation.
     */
    fun calculateBusinessValuation(): Long {
        val cashVal = _publisherTreasury.value
        val catalogVal = _releasedProjects.value.sumOf { it.monthlyRevenue * 12L }
        val devAssetsVal = _activeProjects.value.sumOf { (it.totalBudget * (it.currentProgress / 100f)).toLong() }
        val reputationMultiplier = 1.0 + (_publisherReputation.value / 200.0)
        return ((cashVal + catalogVal + devAssetsVal) * reputationMultiplier).toLong()
    }

    /**
     * Advances simulation by 1 Month across all active pipelines and released game catalog.
     */
    fun advanceMonth(): MonthlyPublisherTickResult {
        _currentSimulationMonth.update { it + 1 }

        val logs = mutableListOf<String>()

        // 1. Advance Active Projects
        var totalMonthlyDevExpenses = 0L
        _activeProjects.update { list ->
            list.map { proj ->
                val simulated = GamePublisherEngine.simulateMonthlyDevelopment(proj, talentScore = 80.0)
                val monthlyBurn = simulated.totalBudget / simulated.devTimeMonths.coerceAtLeast(1)
                totalMonthlyDevExpenses += monthlyBurn
                simulated
            }
        }

        // 2. Advance Released Catalog Sales & Back-Catalog Royalties
        var monthlyPublisherEarnings = 0L
        var totalGrossRevenue = 0L
        _releasedProjects.update { list ->
            list.map { proj ->
                val simulated = GamePublisherEngine.simulateMonthlyReleasedSales(proj)
                val diffProfit = simulated.publisherLifetimeProfit - proj.publisherLifetimeProfit
                if (diffProfit > 0) {
                    monthlyPublisherEarnings += diffProfit
                    totalGrossRevenue += simulated.monthlyRevenue
                    logs.add("🎮 ${proj.title}: Pendapatan kotor ${simulated.monthlyRevenue}, profit publisher ${diffProfit}")
                }
                simulated
            }
        }

        if (monthlyPublisherEarnings > 0) {
            _publisherTreasury.update { it + monthlyPublisherEarnings }
            _lifetimePublisherProfit.update { it + monthlyPublisherEarnings }
        }

        return MonthlyPublisherTickResult(
            totalSalesRevenue = totalGrossRevenue,
            maintenanceExpenses = totalMonthlyDevExpenses,
            netProfit = monthlyPublisherEarnings - totalMonthlyDevExpenses,
            logSummaries = logs
        )
    }

    // Compatibility alias
    fun advanceWeek() = advanceMonth()
}
