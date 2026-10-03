package com.example.businessunit.contentcreator.engine

import com.example.businessunit.contentcreator.model.ActiveCreatorContract
import com.example.businessunit.contentcreator.model.BrandDealOffer
import com.example.businessunit.contentcreator.model.BrandDealType
import com.example.businessunit.contentcreator.model.CoProductionFundingScheme
import com.example.businessunit.contentcreator.model.ContentStatus
import com.example.businessunit.contentcreator.model.ContentType
import com.example.businessunit.contentcreator.model.ContentWork
import com.example.businessunit.contentcreator.model.CreativeFocus
import com.example.businessunit.contentcreator.model.ProductionHouseOffer
import com.example.data.OwnedBusiness
import com.example.data.PlayerState

object ContentCreatorActionHandler {

    fun getContentCreatorBusiness(state: PlayerState, instanceId: String? = null): OwnedBusiness? {
        return if (!instanceId.isNullOrEmpty()) {
            state.ownedBusinesses.find { it.instanceId == instanceId }
                ?: state.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
                ?: state.ownedBusinesses.find { it.catalogId == "content_creator" }
                ?: state.holdingCompanies.flatMap { it.subsidiaries }.find { it.catalogId == "content_creator" }
        } else {
            state.ownedBusinesses.find { it.catalogId == "content_creator" }
                ?: state.holdingCompanies.flatMap { it.subsidiaries }.find { it.catalogId == "content_creator" }
        }
    }

    fun updateContentCreator(
        state: PlayerState,
        targetInstanceId: String? = null,
        transform: (OwnedBusiness) -> OwnedBusiness
    ): PlayerState? {
        val target = getContentCreatorBusiness(state, targetInstanceId) ?: return null
        val targetId = target.instanceId

        var found = false
        val newOwned = state.ownedBusinesses.map { owned ->
            if (owned.instanceId == targetId) {
                found = true
                transform(owned)
            } else owned
        }

        val newHoldings = state.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.instanceId == targetId) {
                    found = true
                    transform(sub)
                } else sub
            }
            holding.copy(subsidiaries = newSubs)
        }

        if (!found) return null
        return state.copy(ownedBusinesses = newOwned, holdingCompanies = newHoldings)
    }

    fun produceContentWork(
        state: PlayerState,
        title: String,
        type: ContentType,
        budget: Long,
        targetStudioInstanceId: String? = null,
        fundingScheme: CoProductionFundingScheme = CoProductionFundingScheme.FULL_CREATOR,
        targetCreatorInstanceId: String? = null,
        creativeFocus: CreativeFocus? = null
    ): Pair<PlayerState?, ContentWork?> {
        val cc = getContentCreatorBusiness(state, targetCreatorInstanceId) ?: return Pair(null, null)

        val targetStudio = if (targetStudioInstanceId != null) {
            state.ownedBusinesses.firstOrNull { it.instanceId == targetStudioInstanceId }
                ?: state.holdingCompanies.flatMap { it.subsidiaries }.firstOrNull { it.instanceId == targetStudioInstanceId }
        } else null

        val creatorCut = (budget * fundingScheme.creatorRatio).toLong()
        val studioCut = (budget * fundingScheme.studioRatio)

        if (creatorCut > 0 && cc.contentCreatorCash < creatorCut) return Pair(null, null)
        if (studioCut > 0 && (targetStudio == null || targetStudio.companyCash < studioCut)) return Pair(null, null)

        val eval = ContentProductionEngine.calculateDetailedScore(
            budget = budget,
            channelLevel = cc.level,
            employees = cc.contentCreatorEmployees,
            contentType = type,
            creativeFocus = creativeFocus,
            isStudioCoProd = targetStudio != null,
            studioLevel = targetStudio?.level ?: 1
        )

        val isCreatorPrimary = fundingScheme.creatorRatio >= 0.5
        val newWork = ContentWork(
            title = title,
            type = type,
            budget = budget,
            engagementScore = eval.score,
            status = ContentStatus.AVAILABLE,
            partnerStudioName = targetStudio?.name,
            partnerStudioId = targetStudio?.instanceId,
            fundingScheme = if (targetStudio != null) fundingScheme else null,
            creativeFocus = creativeFocus?.displayName,
            receptionVerdict = eval.verdictTitle,
            receptionNote = eval.verdictDescription,
            isShadowRecord = !isCreatorPrimary && targetStudio != null
        )

        // If co-produced with studio, create movie project in studio catalog
        val movieProject = if (targetStudio != null) {
            val boxOffice = (budget * (eval.score / 40.0).coerceAtLeast(0.8)).toLong()
            com.example.data.MovieProject(
                title = title,
                budget = budget,
                genres = listOf(type.displayName),
                distributionScale = if (eval.score >= 75) "Global" else "Local",
                reviewScore = eval.score,
                boxOffice = boxOffice,
                netProfit = boxOffice - budget,
                status = if (fundingScheme.studioRatio >= 0.5) "IN_PRODUCTION" else "FINISHED",
                coProductionMeta = com.example.data.CoProductionMeta(
                    isCoProd = true,
                    partnerName = cc.customName ?: cc.name,
                    fundingType = fundingScheme.displayName,
                    revenueSplit = fundingScheme.creatorRatio
                )
            )
        } else null

        val ccId = cc.instanceId
        val targetStudioId = targetStudio?.instanceId

        val newBusinesses = state.ownedBusinesses.map { owned ->
            when {
                owned.instanceId == ccId -> {
                    owned.copy(
                        contentCreatorCash = owned.contentCreatorCash - creatorCut,
                        contentPortfolio = listOf(newWork) + owned.contentPortfolio
                    )
                }
                targetStudioId != null && owned.instanceId == targetStudioId -> {
                    val updatedProjects = if (movieProject != null) listOf(movieProject) + (owned.projectHistory ?: emptyList()) else owned.projectHistory
                    owned.copy(
                        companyCash = owned.companyCash - studioCut,
                        projectHistory = updatedProjects
                    )
                }
                else -> owned
            }
        }

        val updatedHoldings = state.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries.map { sub ->
                when {
                    sub.instanceId == ccId -> {
                        sub.copy(
                            contentCreatorCash = sub.contentCreatorCash - creatorCut,
                            contentPortfolio = listOf(newWork) + sub.contentPortfolio
                        )
                    }
                    targetStudioId != null && sub.instanceId == targetStudioId -> {
                        val updatedProjects = if (movieProject != null) listOf(movieProject) + (sub.projectHistory ?: emptyList()) else sub.projectHistory
                        sub.copy(
                            companyCash = sub.companyCash - studioCut,
                            projectHistory = updatedProjects
                        )
                    }
                    else -> sub
                }
            }
            holding.copy(subsidiaries = newSubs)
        }

        return Pair(state.copy(ownedBusinesses = newBusinesses, holdingCompanies = updatedHoldings), newWork)
    }

    fun levelUp(state: PlayerState, targetInstanceId: String? = null): PlayerState? {
        val cc = getContentCreatorBusiness(state, targetInstanceId) ?: return null
        if (cc.level >= 100) return null
        if (cc.level == 40 && !cc.contentCreatorOfficeUnlocked) return null

        val cost = (500.0 * Math.pow(1.18, (cc.level - 1).toDouble())).toLong()
        if (cc.contentCreatorCash < cost) return null

        val newLevel = cc.level + 1
        val newSubs = cc.contentCreatorSubscribers + (100.0 * Math.pow(1.16, newLevel.toDouble())).toLong()
        return updateContentCreator(state, targetInstanceId) { owned ->
            owned.copy(
                level = newLevel,
                contentCreatorSubscribers = newSubs,
                contentCreatorCash = owned.contentCreatorCash - cost
            )
        }
    }

    fun hireEmployee(state: PlayerState, targetInstanceId: String? = null): PlayerState? {
        val cc = getContentCreatorBusiness(state, targetInstanceId) ?: return null
        val maxEmp = when {
            cc.level >= 81 -> 100
            cc.level >= 61 -> 50
            cc.level >= 41 -> 20
            cc.level >= 21 -> 5
            else -> 0
        }
        if (cc.contentCreatorEmployees >= maxEmp) return null

        val cost = (1500.0 * Math.pow(1.2, cc.contentCreatorEmployees.toDouble())).toLong()
        if (cc.contentCreatorCash < cost) return null

        return updateContentCreator(state, targetInstanceId) { owned ->
            owned.copy(
                contentCreatorEmployees = owned.contentCreatorEmployees + 1,
                contentCreatorCash = owned.contentCreatorCash - cost
            )
        }
    }

    fun unlockOffice(state: PlayerState, targetInstanceId: String? = null): PlayerState? {
        val cc = getContentCreatorBusiness(state, targetInstanceId) ?: return null
        val cost = 5_000_000L
        if (cc.level == 40 && !cc.contentCreatorOfficeUnlocked && cc.contentCreatorCash >= cost) {
            return updateContentCreator(state, targetInstanceId) { owned ->
                owned.copy(
                    contentCreatorOfficeUnlocked = true,
                    contentCreatorCash = owned.contentCreatorCash - cost
                )
            }
        }
        return null
    }

    fun acceptBrandDealOffer(
        state: PlayerState,
        offer: BrandDealOffer,
        targetInstanceId: String? = null
    ): PlayerState? {
        val cc = getContentCreatorBusiness(state, targetInstanceId) ?: return null
        return updateContentCreator(state, targetInstanceId) { owned ->
            if (offer.dealType == BrandDealType.ONE_OFF) {
                owned.copy(
                    contentCreatorCash = owned.contentCreatorCash + offer.contractValue,
                    contentCreatorSubscribers = owned.contentCreatorSubscribers + (offer.contractValue / 10).coerceAtLeast(10L)
                )
            } else {
                if (owned.contentCreatorContracts.size >= 3) {
                    owned
                } else {
                    val newContract = ActiveCreatorContract(
                        brandName = offer.brandName,
                        tierLevel = offer.tierLevel,
                        categoryTag = offer.categoryTag,
                        monthlyPayout = offer.monthlyPayout,
                        totalMonths = offer.durationMonths,
                        remainingMonths = offer.durationMonths
                    )
                    owned.copy(contentCreatorContracts = owned.contentCreatorContracts + newContract)
                }
            }
        }
    }

    fun terminateContract(
        state: PlayerState,
        contractId: String,
        penaltyFee: Long = 0L,
        targetInstanceId: String? = null
    ): PlayerState? {
        val cc = getContentCreatorBusiness(state, targetInstanceId) ?: return null
        if (penaltyFee > 0 && cc.contentCreatorCash < penaltyFee) return null
        return updateContentCreator(state, targetInstanceId) { owned ->
            owned.copy(
                contentCreatorCash = (owned.contentCreatorCash - penaltyFee).coerceAtLeast(0L),
                contentCreatorContracts = owned.contentCreatorContracts.filter { it.id != contractId }
            )
        }
    }

    fun acceptPHOffer(
        state: PlayerState,
        offer: ProductionHouseOffer,
        isLumpSum: Boolean,
        targetInstanceId: String? = null
    ): PlayerState? {
        return updateContentCreator(state, targetInstanceId) { owned ->
            val updatedPortfolio = owned.contentPortfolio.map { work ->
                if (work.id == offer.contentId) {
                    if (isLumpSum) {
                        work.copy(
                            status = ContentStatus.ACQUIRED_LUMP_SUM,
                            acquiredLumpSum = offer.lumpSumOffer,
                            acquiredByPH = offer.phName
                        )
                    } else {
                        work.copy(
                            status = ContentStatus.LICENSED,
                            monthlyRoyalty = offer.monthlyRoyalty,
                            contractDurationMonths = offer.contractDurationMonths,
                            remainingContractMonths = offer.contractDurationMonths,
                            acquiredByPH = offer.phName
                        )
                    }
                } else work
            }

            val addedCash = if (isLumpSum) offer.lumpSumOffer else offer.royaltyUpfront
            owned.copy(
                contentCreatorCash = owned.contentCreatorCash + addedCash,
                contentPortfolio = updatedPortfolio
            )
        }
    }

    fun injectCash(
        state: PlayerState,
        amount: Long,
        targetInstanceId: String? = null
    ): PlayerState? {
        if (amount <= 0L || state.cash < amount) return null
        val updated = updateContentCreator(state, targetInstanceId) { owned ->
            owned.copy(contentCreatorCash = owned.contentCreatorCash + amount)
        } ?: return null
        return updated.copy(cash = updated.cash - amount)
    }

    fun withdrawCash(
        state: PlayerState,
        amount: Long,
        targetInstanceId: String? = null
    ): PlayerState? {
        val cc = getContentCreatorBusiness(state, targetInstanceId) ?: return null
        if (amount <= 0L || cc.contentCreatorCash < amount) return null
        val updated = updateContentCreator(state, targetInstanceId) { owned ->
            owned.copy(contentCreatorCash = owned.contentCreatorCash - amount)
        } ?: return null
        return updated.copy(cash = updated.cash + amount)
    }
}
