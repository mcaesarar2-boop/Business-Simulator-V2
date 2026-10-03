package com.example.ma.engine

import com.example.data.HoldingCompany
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import com.example.data.calculateMegaHoldingValuation
import com.example.ma.data.MaBidderCatalog
import com.example.ma.model.AcquisitionOffer
import com.example.ma.model.MaBidder
import com.example.ma.model.NegotiationOutcome
import com.example.ma.model.NegotiationResult
import com.example.ma.model.OfferStatus
import com.example.ma.model.TargetLevel
import java.util.Locale
import kotlin.random.Random

/**
 * Core engine responsible for:
 * 1. Event trigger probability evaluation.
 * 2. Prerequisite valuation audits.
 * 3. Dynamic target selection across corporate hierarchy.
 * 4. Mathematical valuation multipliers and cash offer computations.
 * 5. Interactive counter-offer negotiation simulation.
 */
object MaOfferEngine {

    const val MIN_VALUATION_THRESHOLD_USD = 10_000_000L // $10,000,000 USD
    private const val BASE_TRIGGER_CHANCE = 0.025 // 2.5% per month (1.5% to 3.0% range)

    data class TargetCandidate(
        val id: String,
        val name: String,
        val level: TargetLevel,
        val valuation: Long,
        val currentStake: Double
    )

    /**
     * Determines whether an unsolicited M&A offer event should trigger for this month tick.
     */
    fun shouldTriggerEvent(playerState: PlayerState, randomOverride: Double? = null): Boolean {
        val roll = randomOverride ?: Random.nextDouble()
        if (roll > BASE_TRIGGER_CHANCE) return false

        // Prerequisite check: Must have at least one entity >= $10,000,000 USD
        val eligibleTargets = extractEligibleTargets(playerState)
        return eligibleTargets.isNotEmpty()
    }

    /**
     * Extracts all business entities eligible for acquisition offers.
     */
    fun extractEligibleTargets(playerState: PlayerState): List<TargetCandidate> {
        val list = mutableListOf<TargetCandidate>()

        // 1. Mega Holding Level
        val megaHoldingValuation = playerState.calculateMegaHoldingValuation()
        val megaHoldingStake = playerState.companyOwnershipPercent
        if (megaHoldingValuation >= MIN_VALUATION_THRESHOLD_USD && megaHoldingStake >= 25.0) {
            val megaName = if (playerState.megaHolding.companyName.isNotBlank()) {
                playerState.megaHolding.companyName
            } else {
                "Mega Holding Conglomerate Group"
            }
            list.add(
                TargetCandidate(
                    id = "mega_holding_apex",
                    name = megaName,
                    level = TargetLevel.MEGA_HOLDING,
                    valuation = megaHoldingValuation,
                    currentStake = megaHoldingStake
                )
            )
        }

        // 2. Sub-Holding Level
        playerState.holdingCompanies.forEach { holding ->
            val holdingVal = holding.calculateHoldingValuation()
            val holdingStake = holding.ownershipPercentage.toDouble()
            if (holdingVal >= MIN_VALUATION_THRESHOLD_USD && holdingStake >= 25.0) {
                list.add(
                    TargetCandidate(
                        id = holding.instanceId,
                        name = holding.name,
                        level = TargetLevel.SUB_HOLDING,
                        valuation = holdingVal,
                        currentStake = holdingStake
                    )
                )
            }
        }

        // 3. Individual Business Unit Level (Independent & Subsidiaries)
        val allBusinesses = mutableListOf<OwnedBusiness>()
        allBusinesses.addAll(playerState.ownedBusinesses)
        playerState.holdingCompanies.forEach { allBusinesses.addAll(it.subsidiaries) }

        allBusinesses.forEach { biz ->
            val bizVal = biz.calculateBusinessValuation()
            val bizStake = biz.ownershipPercent
            if (bizVal >= MIN_VALUATION_THRESHOLD_USD && bizStake >= 25.0) {
                list.add(
                    TargetCandidate(
                        id = biz.instanceId,
                        name = biz.name,
                        level = TargetLevel.UNIT_BUSINESS,
                        valuation = bizVal,
                        currentStake = bizStake
                    )
                )
            }
        }

        return list
    }

    /**
     * Generates a dynamic unsolicited acquisition offer.
     */
    fun generateOffer(
        playerState: PlayerState,
        currentMonth: Int,
        currentYear: Int
    ): AcquisitionOffer? {
        val candidates = extractEligibleTargets(playerState)
        if (candidates.isEmpty()) return null

        val target = candidates.random()
        val bidder = MaBidderCatalog.getRandomBidder()

        // 1. Stake Requested: 10.0% to 60.0% (clamped to available ownership)
        val maxFeasibleStake = (target.currentStake - 10.0).coerceIn(10.0, 60.0)
        val rawStake = Random.nextDouble(10.0, maxFeasibleStake.coerceAtLeast(10.0))
        val stakeRequested = (Math.round(rawStake * 10.0) / 10.0).coerceIn(10.0, 60.0)

        // 2. Valuation Multiplier (Lowball, Fair, or Premium Hype)
        val multiplierCategoryRoll = Random.nextDouble()
        val valuationMultiplier = when {
            multiplierCategoryRoll < 0.25 -> {
                // Lowball Offer: 0.80x to 0.95x
                Random.nextDouble(0.80, 0.95)
            }
            multiplierCategoryRoll < 0.75 -> {
                // Fair Market Offer: 1.00x to 1.15x
                Random.nextDouble(1.00, 1.15)
            }
            else -> {
                // Premium Hype Offer: 1.25x to 2.50x
                Random.nextDouble(1.25, 2.50)
            }
        }
        val roundedMultiplier = Math.round(valuationMultiplier * 100.0) / 100.0

        val letterOfIntent = buildLetterOfIntentText(bidder, target, stakeRequested, roundedMultiplier)

        return AcquisitionOffer(
            bidder = bidder,
            targetEntityId = target.id,
            targetEntityName = target.name,
            targetLevel = target.level,
            currentEntityValuation = target.valuation,
            valuationMultiplier = roundedMultiplier,
            stakePercent = stakeRequested,
            status = OfferStatus.PENDING,
            creationMonth = currentMonth,
            creationYear = currentYear,
            stalledTicksRemaining = 3,
            currentOwnerStake = target.currentStake,
            letterOfIntent = letterOfIntent
        )
    }

    /**
     * Simulates the interactive counter-offer negotiation outcome.
     *
     * @param offer Original acquisition offer
     * @param counterMultiplierDemand Demanded valuation multiplier (e.g. 1.20x to 2.0x of original offer)
     * @param counterStakeDemand Demanded stake percentage
     */
    fun simulateNegotiation(
        offer: AcquisitionOffer,
        counterMultiplierDemand: Double,
        counterStakeDemand: Double
    ): NegotiationResult {
        val premiumOverOriginal = (counterMultiplierDemand / offer.valuationMultiplier) - 1.0
        val bidderAggressiveness = offer.bidder.aggressiveness

        // Calculate dynamic walk-away risk (10% to 40%)
        val demandPenalty = (premiumOverOriginal * 60.0).coerceIn(0.0, 30.0)
        val aggressivenessPenalty = (bidderAggressiveness * 10.0).toDouble()
        val walkAwayRiskPercent = (12.0 + demandPenalty + aggressivenessPenalty).toInt().coerceIn(10, 45)

        val roll = Random.nextInt(100)

        return when {
            // Outcome C (Walk Away): If roll falls in walk-away bracket
            roll < walkAwayRiskPercent -> {
                NegotiationResult(
                    outcome = NegotiationOutcome.WALKED_AWAY,
                    requestedMultiplier = counterMultiplierDemand,
                    agreedMultiplier = offer.valuationMultiplier,
                    agreedStake = offer.stakePercent,
                    finalCashOffer = 0L,
                    headline = "Negosiasi Buntu — Investor Menarik Diri!",
                    detailMessage = "${offer.bidder.name} menganggap valuasi balasan terlalu tinggi dan memutuskan untuk membatalkan seluruh proses akuisisi secara sepihak.",
                    walkAwayRiskPercent = walkAwayRiskPercent
                )
            }
            // Outcome B (Re-counter / Compromise): Next 25-30% bracket
            roll < (walkAwayRiskPercent + 30) -> {
                // Compromise multiplier halfway between original and counter demand
                val compromiseMultiplier = Math.round(((offer.valuationMultiplier + counterMultiplierDemand) / 2.0) * 100.0) / 100.0
                val compromiseStake = counterStakeDemand
                val compromiseCash = ((offer.currentEntityValuation * compromiseMultiplier) * (compromiseStake / 100.0)).toLong()

                NegotiationResult(
                    outcome = NegotiationOutcome.RE_COUNTERED,
                    requestedMultiplier = counterMultiplierDemand,
                    agreedMultiplier = compromiseMultiplier,
                    agreedStake = compromiseStake,
                    finalCashOffer = compromiseCash,
                    headline = "Kompromi — Tawaran Tandingan Balik dari Investor",
                    detailMessage = "${offer.bidder.name} menyetujui sebagian kenaikan valuasi pada ${String.format(Locale.US, "%.2f", compromiseMultiplier)}x dengan injeksi kas sebesar $${String.format(Locale.US, "%,d", compromiseCash)} USD.",
                    walkAwayRiskPercent = walkAwayRiskPercent
                )
            }
            // Outcome A (Accepted): Remaining probability (Highest chance)
            else -> {
                val agreedMultiplier = counterMultiplierDemand
                val agreedStake = counterStakeDemand
                val finalCash = ((offer.currentEntityValuation * agreedMultiplier) * (agreedStake / 100.0)).toLong()

                NegotiationResult(
                    outcome = NegotiationOutcome.ACCEPTED,
                    requestedMultiplier = counterMultiplierDemand,
                    agreedMultiplier = agreedMultiplier,
                    agreedStake = agreedStake,
                    finalCashOffer = finalCash,
                    headline = "Kesepakatan Tercapai! Tawaran Balik Diterima!",
                    detailMessage = "Komite Investasi ${offer.bidder.name} menyetujui seluruh syarat tandingan Anda pada valuasi ${String.format(Locale.US, "%.2f", agreedMultiplier)}x!",
                    walkAwayRiskPercent = walkAwayRiskPercent
                )
            }
        }
    }

    private fun buildLetterOfIntentText(
        bidder: MaBidder,
        target: TargetCandidate,
        stake: Double,
        multiplier: Double
    ): String {
        val levelScope = when (target.level) {
            TargetLevel.MEGA_HOLDING -> "entitas konglomerasi puncak"
            TargetLevel.SUB_HOLDING -> "divisi sub-holding strategis"
            TargetLevel.UNIT_BUSINESS -> "unit bisnis operasional"
        }

        val tone = when {
            multiplier >= 1.30 -> "Berdasarkan audit komparatif mendalam dan momentum pertumbuhan yang eksponensial, dewan direksi kami memutuskan untuk mengajukan penawaran akuisisi bernilai premium tinggi."
            multiplier >= 1.00 -> "Kami melihat sinergi portofolio jangka panjang yang solid dan bersedia menyerap kepemilikan minoritas/mayoritas pada nilai wajar pasar konsensus."
            else -> "Mempertimbangkan kondisi likuiditas dan dinamika makroekonomi saat ini, kami bermaksud mengeksekusi restrukturisasi permodalan terarah dengan penawaran tunai cepat."
        }

        return "Dengan hormat,\n\n" +
                "${bidder.name} secara resmi menyampaikan Letter of Intent (LOI) tanpa ikatan hukum awal untuk mengakuisisi ${String.format(Locale.US, "%.1f", stake)}% porsi kepemilikan modal pada ${target.name} ($levelScope).\n\n" +
                "$tone\n\n" +
                "Seluruh dana likuiditas akan langsung disuntikkan secara tunai penuh ke kas perbendaharaan korporasi begitu transaksi disahkan."
    }
}
