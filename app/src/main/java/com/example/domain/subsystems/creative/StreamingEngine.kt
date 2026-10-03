package com.example.domain.subsystems.creative

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import com.example.data.STREAMING_SERVER_TIERS
import com.example.data.StreamingContent
import com.example.data.StreamingContentSource
import com.example.data.StreamingContentType
import kotlin.math.max

/**
 * Isolated domain engine for the Streaming Service (OTT) subsystem.
 * Handles:
 * 1. Monthly server load, outage calculations, server health recovery/degrade.
 * 2. Subscriber growth/churn based on server stability, catalog hits, and marketing tiers.
 * 3. Monthly subscription gross revenue, catalog license decrement, and server upkeep costs.
 * 4. User actions: producing original OTT titles, upgrading server tiers, marketing campaigns, distributing in-house movies.
 */
class StreamingEngine : MonthlyTickSubscriber {

    override val subscriberId: String = "streaming_service"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        var totalGrossRevenue = 0L
        var totalOperatingCosts = 0L

        val updatedOwned = currentState.ownedBusinesses.map { biz ->
            if (biz.catalogId == "streaming_service") {
                val (updatedBiz, gross, costs) = processBusinessMonthly(biz)
                totalGrossRevenue += gross
                totalOperatingCosts += costs
                updatedBiz
            } else biz
        }

        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            var changed = false
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.catalogId == "streaming_service") {
                    changed = true
                    val (updatedSub, gross, costs) = processBusinessMonthly(sub)
                    totalGrossRevenue += gross
                    totalOperatingCosts += costs
                    updatedSub
                } else sub
            }
            if (changed) holding.copy(subsidiaries = newSubs) else holding
        }

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = totalGrossRevenue,
            expenses = totalOperatingCosts,
            dividendToGlobal = 0L,
            logMessages = listOf(
                "Streaming Platform: Gross revenue $totalGrossRevenue, Costs $totalOperatingCosts (Net: ${totalGrossRevenue - totalOperatingCosts})"
            ),
            stateModifier = { state ->
                state.copy(
                    ownedBusinesses = updatedOwned,
                    holdingCompanies = updatedHoldings
                )
            }
        )
    }

    private fun processBusinessMonthly(business: OwnedBusiness): Triple<OwnedBusiness, Long, Long> {
        val d = business.streamingData
        val spec = STREAMING_SERVER_TIERS.find { it.tier == d.serverTier }
            ?: STREAMING_SERVER_TIERS.first()

        var viralHits = 0
        var totalCatalogBonusTraffic = 0L
        val updatedCatalog = d.streamingCatalog.map { item ->
            var currentItem = item
            if (currentItem.rating >= 82 || currentItem.isViral) {
                viralHits++
                totalCatalogBonusTraffic += currentItem.monthlyViewerSpike * 2
            } else {
                totalCatalogBonusTraffic += currentItem.monthlyViewerSpike
            }

            if (currentItem.licenseExpiryMonths > 0) {
                val nextExpiry = currentItem.licenseExpiryMonths - 1
                currentItem = currentItem.copy(
                    licenseExpiryMonths = nextExpiry,
                    views = currentItem.views + currentItem.monthlyViewerSpike
                )
            } else {
                currentItem = currentItem.copy(
                    views = currentItem.views + currentItem.monthlyViewerSpike
                )
            }
            currentItem
        }.filter { it.licenseExpiryMonths > 0 || it.source != StreamingContentSource.LICENSED_CONTRACT }

        val baseTraffic = (d.subscribers * 1.5).toLong() + totalCatalogBonusTraffic
        val finalTraffic = if (viralHits > 0) (baseTraffic * (1.0 + (viralHits * 0.3))).toLong() else baseTraffic
        val isOverloaded = finalTraffic > spec.maxCapacity

        val nextOutage = isOverloaded
        val nextHealth = if (isOverloaded) (d.serverHealth - 20).coerceAtLeast(15) else (d.serverHealth + 10).coerceAtMost(100)
        val subGrowth = if (isOverloaded) {
            -(d.subscribers * 0.10).toLong()
        } else {
            val growthRate = 0.04 * (1.0 + d.marketingTier * 0.25) * (1.0 + d.aiAlgorithmLevel * 0.1)
            (finalTraffic * growthRate).toLong() + (business.level * 300L)
        }
        val nextSubscribers = (d.subscribers + subGrowth).coerceAtLeast(100L)

        val monthlyGross = (nextSubscribers * d.subscriptionFee).toLong()
        val monthlyCosts = spec.monthlyUpkeep + updatedCatalog.sumOf { it.monthlyLicenseFee } + (nextSubscribers * 0.15).toLong()
        val netStreamingProfit = monthlyGross - monthlyCosts

        val updatedBusiness = business.copy(
            streamingData = d.copy(
                subscribers = nextSubscribers,
                currentTraffic = finalTraffic,
                peakTraffic = max(d.peakTraffic, finalTraffic),
                serverOutage = nextOutage,
                serverHealth = nextHealth,
                streamingCatalog = updatedCatalog,
                streamingCash = (d.streamingCash + netStreamingProfit).coerceAtLeast(0L)
            )
        )
        return Triple(updatedBusiness, monthlyGross, monthlyCosts)
    }

    // --- User Intent Handlers / Reducers ---

    fun produceOriginalOttTitle(
        business: OwnedBusiness,
        title: String,
        type: StreamingContentType,
        genre: String,
        budget: Long,
        synopsis: String
    ): OwnedBusiness? {
        val d = business.streamingData
        if (d.streamingCash < budget) return null

        val ratingScore = when {
            budget >= 2_000_000L -> (85..98).random()
            budget >= 500_000L -> (75..90).random()
            else -> (65..82).random()
        }

        val estimatedViewers = (budget / 10L).coerceIn(20_000L, 5_000_000L)
        val isViral = ratingScore >= 82

        val newContent = StreamingContent(
            title = title,
            type = type,
            source = StreamingContentSource.ORIGINAL_OTT,
            rating = ratingScore,
            views = (budget / 20L).coerceAtLeast(10_000L),
            monthlyViewerSpike = estimatedViewers,
            isViral = isViral,
            licenseExpiryMonths = 0,
            monthlyLicenseFee = 0L,
            genre = genre,
            synopsis = synopsis.ifBlank { "Produksi serial OTT orisinal beranggaran tinggi yang digarap eksklusif untuk memanjakan penonton setia." }
        )

        return business.copy(
            streamingData = d.copy(
                streamingCash = d.streamingCash - budget,
                streamingCatalog = listOf(newContent) + d.streamingCatalog
            )
        )
    }

    fun distributeMovieToInHouseOtt(
        ottBusiness: OwnedBusiness,
        projectTitle: String,
        filmFormat: String,
        reviewScore: Int,
        currentRevenue: Long
    ): OwnedBusiness {
        val d = ottBusiness.streamingData
        val newContent = StreamingContent(
            title = projectTitle,
            type = when (filmFormat) {
                "Short Film" -> StreamingContentType.INDIE_DARLING
                "Animation" -> StreamingContentType.ANIME_ANIMATION
                "Dokumenter", "Documentary" -> StreamingContentType.DOCUMENTARY
                else -> StreamingContentType.BLOCKBUSTER_MOVIE
            },
            source = StreamingContentSource.IN_HOUSE_STUDIO,
            rating = reviewScore,
            views = (currentRevenue / 50L).coerceAtLeast(100_000L),
            monthlyViewerSpike = (currentRevenue / 100L).coerceAtLeast(25_000L),
            isViral = reviewScore >= 80,
            licenseExpiryMonths = 0, // Perpetual in-house
            monthlyLicenseFee = 0L,
            genre = "Studio In-House Production",
            synopsis = "Karya film spektakuler yang didistribusikan secara eksklusif ke platform streaming milik grup konglomerasi."
        )

        return if (d.streamingCatalog.any { it.title.equals(projectTitle, ignoreCase = true) }) {
            ottBusiness
        } else {
            ottBusiness.copy(
                streamingData = d.copy(
                    streamingCatalog = listOf(newContent) + d.streamingCatalog
                )
            )
        }
    }
}
