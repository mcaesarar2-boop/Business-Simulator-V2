package com.example.filmstudio.repository

import com.example.data.MovieProject
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import com.example.domain.subsystems.creative.FilmTimelineEngine
import com.example.filmstudio.engine.FilmProductionMath

/**
 * Default implementation of FilmStudioRepository handling business and holding mutations.
 */
class FilmStudioRepositoryImpl : FilmStudioRepository {

    override fun produceMovie(
        currentState: PlayerState,
        instanceId: String,
        title: String,
        budget: Long,
        promoBudget: Long,
        genres: List<String>,
        isGlobal: Boolean,
        schedMonth: Int?,
        schedYear: Int?,
        filmFormat: String,
        productionFocus: String,
        scheduledReleaseDate: String?,
        targetDurationMonths: Int
    ): Pair<Boolean, PlayerState> {
        val totalCost = budget + promoBudget

        var owned: OwnedBusiness? = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null

        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }

        if (owned == null) return Pair(false, currentState)
        if (owned.companyCash < totalCost) return Pair(false, currentState)

        // Anti-Duplicate checking
        val cleanTitle = title.trim()
        if (owned.projectHistory.any { it.title.trim().equals(cleanTitle, ignoreCase = true) }) {
            return Pair(false, currentState)
        }

        // Distribution permission check
        val canGlobal = owned.level >= 20 ||
            owned.purchasedUpgrades.contains("la_global_distrib") ||
            owned.purchasedUpgrades.contains("anim_global_distrib")
        if (isGlobal && !canGlobal) return Pair(false, currentState)

        // Upgrades
        val hasSoundstage = owned.purchasedUpgrades.contains("la_soundstage")
        val hasPostPipeline = owned.purchasedUpgrades.contains("la_post_pipeline")
        val hasVirtualProd = owned.purchasedUpgrades.contains("la_virtual_prod")
        val hasVfxStunt = owned.purchasedUpgrades.contains("la_vfx_stunt")

        val hasAnimPipeline = owned.purchasedUpgrades.contains("anim_pipeline")
        val hasRenderFarm = owned.purchasedUpgrades.contains("anim_renderfarm")
        val hasMocapLab = owned.purchasedUpgrades.contains("anim_mocap_lab")
        val hasPhysicsSim = owned.purchasedUpgrades.contains("anim_physics_sim")

        val reviewScore = FilmProductionMath.calculateReviewScore(
            productionFocus = productionFocus,
            studioLevel = owned.level,
            genreCount = genres.size,
            hasSoundstage = hasSoundstage,
            hasPostPipeline = hasPostPipeline,
            hasVirtualProd = hasVirtualProd,
            hasMocapLab = hasMocapLab
        )

        val boxOffice = FilmProductionMath.calculateBoxOffice(
            budget = budget,
            promoBudget = promoBudget,
            reviewScore = reviewScore,
            isGlobal = isGlobal,
            studioLevel = owned.level,
            studioType = owned.studioType,
            hasVfxStunt = hasVfxStunt,
            hasPhysicsSim = hasPhysicsSim
        )

        val netProfit = boxOffice - totalCost
        val initMonths = if (isGlobal) 6 else 4

        val isAnimStudio = owned.studioType == "ANIMATION"
        val delayMonths = FilmTimelineEngine.calculateTotalDurationMonths(
            isAnimation = isAnimStudio,
            filmFormat = filmFormat,
            budget = budget,
            productionFocus = productionFocus,
            hasVirtualProd = hasVirtualProd,
            hasAnimPipeline = hasAnimPipeline,
            hasRenderFarm = hasRenderFarm,
            hasPostPipeline = hasPostPipeline,
            targetMonths = targetDurationMonths
        )

        val isScheduledFuture = schedMonth != null && schedYear != null &&
            (schedYear > currentState.inGameYear || (schedYear == currentState.inGameYear && schedMonth > currentState.inGameMonth))

        val initialPhase = if (isScheduledFuture) {
            "ANTREAN"
        } else {
            FilmTimelineEngine.getProgress(
                isAnimation = isAnimStudio,
                totalMonths = delayMonths,
                remainingMonths = delayMonths
            ).displayLabel
        }

        val project = MovieProject(
            title = cleanTitle,
            budget = budget,
            genres = genres,
            distributionScale = if (isGlobal) "Global" else "Local",
            reviewScore = reviewScore,
            boxOffice = boxOffice,
            netProfit = netProfit,
            status = "IN_PRODUCTION",
            remainingMonths = initMonths,
            currentRevenue = 0L,
            targetMaxRevenue = boxOffice,
            productionPhase = initialPhase,
            productionDelayMonths = delayMonths,
            promoBudget = promoBudget,
            scheduledMonth = schedMonth,
            scheduledYear = schedYear,
            filmFormat = filmFormat,
            productionFocus = productionFocus,
            prodStartMonth = schedMonth ?: currentState.inGameMonth,
            prodStartYear = schedYear ?: currentState.inGameYear,
            totalProductionMonths = delayMonths,
            scheduledReleaseDate = scheduledReleaseDate
        )

        val newOwned = owned.copy(
            projectHistory = owned.projectHistory + project,
            companyCash = owned.companyCash - totalCost
        )

        val newState = if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map {
                        if (it.instanceId == instanceId) newOwned else it
                    }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            currentState.copy(holdingCompanies = newHoldings)
        } else {
            currentState.copy(
                ownedBusinesses = currentState.ownedBusinesses.map {
                    if (it.instanceId == instanceId) newOwned else it
                }
            )
        }

        return Pair(true, newState)
    }

    override fun cancelMovieProject(
        currentState: PlayerState,
        instanceId: String,
        projectTitle: String,
        refundAmount: Long
    ): PlayerState {
        var owned: OwnedBusiness? = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null

        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }

        if (owned == null) return currentState

        val newProjList = owned.projectHistory.filter { it.title != projectTitle }
        val newOwned = owned.copy(
            projectHistory = newProjList,
            companyCash = owned.companyCash + refundAmount
        )

        return if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map {
                        if (it.instanceId == instanceId) newOwned else it
                    }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            currentState.copy(holdingCompanies = newHoldings)
        } else {
            currentState.copy(
                ownedBusinesses = currentState.ownedBusinesses.map {
                    if (it.instanceId == instanceId) newOwned else it
                }
            )
        }
    }

    override fun polishMovieProject(
        currentState: PlayerState,
        instanceId: String,
        projectTitle: String,
        budgetCost: Long,
        extraMonths: Int
    ): Pair<Boolean, PlayerState> {
        var owned: OwnedBusiness? = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null

        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }

        if (owned == null) return Pair(false, currentState)

        val fromCompanyCash = owned.companyCash >= budgetCost
        if (!fromCompanyCash && currentState.cash < budgetCost) return Pair(false, currentState)

        val newProjList = owned.projectHistory.map { proj ->
            if (proj.title == projectTitle && proj.isQcPhase) {
                val scoreBump = (5..20).random()
                val score = (proj.internalScore ?: proj.reviewScore) + scoreBump
                proj.copy(
                    isQcPhase = false,
                    productionPhase = "Poles Visual",
                    productionDelayMonths = proj.productionDelayMonths + extraMonths,
                    internalScore = score.coerceIn(1, 99)
                )
            } else proj
        }

        val newOwned = owned.copy(
            projectHistory = newProjList,
            companyCash = if (fromCompanyCash) owned.companyCash - budgetCost else owned.companyCash
        )

        val newPlayerCash = if (!fromCompanyCash) currentState.cash - budgetCost else currentState.cash

        val newState = if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map {
                        if (it.instanceId == instanceId) newOwned else it
                    }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            currentState.copy(holdingCompanies = newHoldings, cash = newPlayerCash)
        } else {
            currentState.copy(
                cash = newPlayerCash,
                ownedBusinesses = currentState.ownedBusinesses.map {
                    if (it.instanceId == instanceId) newOwned else it
                }
            )
        }

        return Pair(true, newState)
    }

    override fun scheduleMovieRelease(
        currentState: PlayerState,
        instanceId: String,
        projectTitle: String,
        schedStr: String
    ): PlayerState {
        var owned: OwnedBusiness? = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null

        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }

        if (owned == null) return currentState

        val newProjList = owned.projectHistory.map { proj ->
            if (proj.title == projectTitle && proj.isQcPhase) {
                proj.copy(
                    isQcPhase = false,
                    isAwaitingRelease = true,
                    scheduledReleaseDate = schedStr
                )
            } else proj
        }

        val newOwned = owned.copy(projectHistory = newProjList)

        return if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map {
                        if (it.instanceId == instanceId) newOwned else it
                    }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            currentState.copy(holdingCompanies = newHoldings)
        } else {
            currentState.copy(
                ownedBusinesses = currentState.ownedBusinesses.map {
                    if (it.instanceId == instanceId) newOwned else it
                }
            )
        }
    }

    override fun startStreamingLicense(
        currentState: PlayerState,
        instanceId: String,
        title: String,
        licenseeName: String,
        fee: Long,
        duration: Int
    ): PlayerState {
        var isNested = false
        var holdingId: String? = null
        var owned: OwnedBusiness? = currentState.ownedBusinesses.find { it.instanceId == instanceId }

        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }

        if (owned == null) return currentState

        val newHistory = owned.projectHistory.map { proj ->
            if (proj.title == title && proj.status == "FINISHED") {
                proj.copy(
                    licenseeName = licenseeName,
                    licenseMonthlyFee = fee,
                    licenseRemainingMonths = duration
                )
            } else {
                proj
            }
        }

        val newOwned = owned.copy(projectHistory = newHistory)

        return if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { h ->
                if (h.instanceId == holdingId) {
                    val newSubs = h.subsidiaries.map { if (it.instanceId == instanceId) newOwned else it }
                    h.copy(subsidiaries = newSubs)
                } else h
            }
            currentState.copy(holdingCompanies = newHoldings)
        } else {
            val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == instanceId) newOwned else it }
            currentState.copy(ownedBusinesses = newOwnedList)
        }
    }

    override fun sellMovieIp(
        currentState: PlayerState,
        instanceId: String,
        title: String,
        sellPrice: Long
    ): PlayerState {
        var isNested = false
        var holdingId: String? = null
        var owned: OwnedBusiness? = currentState.ownedBusinesses.find { it.instanceId == instanceId }

        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }

        if (owned == null) return currentState

        val proj = owned.projectHistory.find { it.title == title && it.status == "FINISHED" } ?: return currentState
        val valueToDeduct = maxOf(0L, proj.netProfit)

        val newHistory = owned.projectHistory.filter { it != proj }
        val newOwned = owned.copy(
            projectHistory = newHistory,
            extraValuation = maxOf(0L, owned.extraValuation - valueToDeduct)
        )

        // Split revenue based on Co-Production funding scheme if applicable
        val meta = proj.coProductionMeta
        val (creatorShare, studioShare) = if (meta != null && meta.isCoProd) {
            when {
                meta.fundingType.contains("Creator", ignoreCase = true) -> Pair(sellPrice, 0L)
                meta.fundingType.contains("Studio", ignoreCase = true) -> Pair(0L, sellPrice)
                else -> {
                    val half = sellPrice / 2
                    Pair(half, sellPrice - half) // 50/50 split
                }
            }
        } else {
            Pair(0L, sellPrice)
        }

        val updatedBusinesses = currentState.ownedBusinesses.map { biz ->
            when {
                biz.instanceId == instanceId -> newOwned
                biz.catalogId == "content_creator" && creatorShare > 0 -> {
                    biz.copy(contentCreatorCash = biz.contentCreatorCash + creatorShare)
                }
                else -> biz
            }
        }

        val updatedHoldings = if (isNested && holdingId != null) {
            currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map { sub ->
                        when {
                            sub.instanceId == instanceId -> newOwned
                            sub.catalogId == "content_creator" && creatorShare > 0 -> {
                                sub.copy(contentCreatorCash = sub.contentCreatorCash + creatorShare)
                            }
                            else -> sub
                        }
                    }
                    holding.copy(subsidiaries = newSubs)
                } else {
                    holding.copy(
                        subsidiaries = holding.subsidiaries.map { sub ->
                            if (sub.catalogId == "content_creator" && creatorShare > 0) {
                                sub.copy(contentCreatorCash = sub.contentCreatorCash + creatorShare)
                            } else sub
                        }
                    )
                }
            }
        } else {
            currentState.holdingCompanies.map { holding ->
                holding.copy(
                    subsidiaries = holding.subsidiaries.map { sub ->
                        if (sub.catalogId == "content_creator" && creatorShare > 0) {
                            sub.copy(contentCreatorCash = sub.contentCreatorCash + creatorShare)
                        } else sub
                    }
                )
            }
        }

        return currentState.copy(
            ownedBusinesses = updatedBusinesses,
            holdingCompanies = updatedHoldings,
            cash = currentState.cash + studioShare
        )
    }
}
