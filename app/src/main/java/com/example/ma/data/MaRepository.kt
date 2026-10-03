package com.example.ma.data

import com.example.data.HoldingCompany
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import com.example.ma.engine.MaOfferEngine
import com.example.ma.model.AcquisitionOffer
import com.example.ma.model.NegotiationOutcome
import com.example.ma.model.NegotiationResult
import com.example.ma.model.OfferStatus
import com.example.ma.model.TargetLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Thread-safe state repository managing M&A deals, unsolicited LOI proposals,
 * board inbox holding, negotiation workflows, and equity-to-cash execution.
 */
class MaRepository private constructor() {

    companion object {
        @Volatile
        private var instance: MaRepository? = null

        fun getInstance(): MaRepository {
            return instance ?: synchronized(this) {
                instance ?: MaRepository().also { instance = it }
            }
        }
    }

    private val _activePopupOffer = MutableStateFlow<AcquisitionOffer?>(null)
    val activePopupOffer: StateFlow<AcquisitionOffer?> = _activePopupOffer.asStateFlow()

    private val _pendingStalledOffers = MutableStateFlow<List<AcquisitionOffer>>(emptyList())
    val pendingStalledOffers: StateFlow<List<AcquisitionOffer>> = _pendingStalledOffers.asStateFlow()

    private val _dealHistory = MutableStateFlow<List<AcquisitionOffer>>(emptyList())
    val dealHistory: StateFlow<List<AcquisitionOffer>> = _dealHistory.asStateFlow()

    private val _lastNegotiationResult = MutableStateFlow<NegotiationResult?>(null)
    val lastNegotiationResult: StateFlow<NegotiationResult?> = _lastNegotiationResult.asStateFlow()

    private val _showNegotiationDialog = MutableStateFlow(false)
    val showNegotiationDialog: StateFlow<Boolean> = _showNegotiationDialog.asStateFlow()

    private val _showInboxDialog = MutableStateFlow(false)
    val showInboxDialog: StateFlow<Boolean> = _showInboxDialog.asStateFlow()

    fun openOfferFromInbox(offer: AcquisitionOffer) {
        _activePopupOffer.value = offer
        _showInboxDialog.value = false
    }

    fun openNegotiationDialog() {
        _showNegotiationDialog.value = true
    }

    fun closeNegotiationDialog() {
        _showNegotiationDialog.value = false
        _lastNegotiationResult.value = null
    }

    fun openInboxDialog() {
        _showInboxDialog.value = true
    }

    fun closeInboxDialog() {
        _showInboxDialog.value = false
    }

    fun dismissPopup() {
        _activePopupOffer.value = null
    }

    /**
     * Accepts the offer: transfers equity stake and injects cash into the target treasury.
     */
    fun acceptOffer(offer: AcquisitionOffer, playerState: PlayerState): Pair<PlayerState, String> {
        val finalCash = offer.totalCashOffer
        val stakeSold = offer.stakePercent

        val updatedState = when (offer.targetLevel) {
            TargetLevel.MEGA_HOLDING -> {
                val newHoldingStake = (playerState.companyOwnershipPercent - stakeSold).coerceAtLeast(0.0)
                val newCash = playerState.cash + finalCash
                playerState.copy(
                    companyOwnershipPercent = newHoldingStake,
                    playerEquityShare = newHoldingStake,
                    cash = newCash
                )
            }
            TargetLevel.SUB_HOLDING -> {
                val updatedHoldings = playerState.holdingCompanies.map { holding ->
                    if (holding.instanceId == offer.targetEntityId) {
                        val newStake = (holding.ownershipPercentage - stakeSold.toFloat()).coerceAtLeast(0f)
                        val newCash = holding.holdingCash + finalCash.toDouble()
                        holding.copy(ownershipPercentage = newStake, holdingCash = newCash)
                    } else {
                        holding
                    }
                }
                playerState.copy(holdingCompanies = updatedHoldings)
            }
            TargetLevel.UNIT_BUSINESS -> {
                // Check independent businesses
                var foundInRoot = false
                val updatedOwned = playerState.ownedBusinesses.map { biz ->
                    if (biz.instanceId == offer.targetEntityId) {
                        foundInRoot = true
                        val newStake = (biz.ownershipPercent - stakeSold).coerceAtLeast(0.0)
                        val newCash = biz.companyCash + finalCash.toDouble()
                        biz.copy(ownershipPercent = newStake, companyCash = newCash)
                    } else {
                        biz
                    }
                }

                // Check subsidiaries in sub-holdings
                val updatedHoldings = playerState.holdingCompanies.map { holding ->
                    val updatedSubs = holding.subsidiaries.map { sub ->
                        if (sub.instanceId == offer.targetEntityId) {
                            val newStake = (sub.ownershipPercent - stakeSold).coerceAtLeast(0.0)
                            val newCash = sub.companyCash + finalCash.toDouble()
                            sub.copy(ownershipPercent = newStake, companyCash = newCash)
                        } else {
                            sub
                        }
                    }
                    holding.copy(subsidiaries = updatedSubs)
                }

                playerState.copy(
                    ownedBusinesses = updatedOwned,
                    holdingCompanies = updatedHoldings
                )
            }
        }

        val completedOffer = offer.copy(status = OfferStatus.ACCEPTED)
        _dealHistory.value = listOf(completedOffer) + _dealHistory.value
        _pendingStalledOffers.value = _pendingStalledOffers.value.filter { it.offerId != offer.offerId }
        _activePopupOffer.value = null
        _showNegotiationDialog.value = false
        _lastNegotiationResult.value = null

        val successMsg = "Transaksi M&A Berhasil! ${offer.bidder.name} resmi membeli ${String.format(Locale.US, "%.1f", stakeSold)}% saham ${offer.targetEntityName} senilai $${String.format(Locale.US, "%,d", finalCash)} USD."
        return Pair(updatedState, successMsg)
    }

    /**
     * Rejects an offer permanently.
     */
    fun rejectOffer(offer: AcquisitionOffer) {
        val rejectedOffer = offer.copy(status = OfferStatus.REJECTED)
        _dealHistory.value = listOf(rejectedOffer) + _dealHistory.value
        _pendingStalledOffers.value = _pendingStalledOffers.value.filter { it.offerId != offer.offerId }
        _activePopupOffer.value = null
        _showNegotiationDialog.value = false
    }

    /**
     * Stalls the offer, moving it to the Board Decision inbox for 3 ticks.
     */
    fun stallOffer(offer: AcquisitionOffer) {
        val stalledOffer = offer.copy(
            status = OfferStatus.STALLED,
            stalledTicksRemaining = 3
        )
        _pendingStalledOffers.value = listOf(stalledOffer) + _pendingStalledOffers.value.filter { it.offerId != offer.offerId }
        _activePopupOffer.value = null
        _showNegotiationDialog.value = false
    }

    /**
     * Submits an interactive counter-offer.
     */
    fun submitCounterOffer(
        offer: AcquisitionOffer,
        counterMultiplierDemand: Double,
        counterStakeDemand: Double
    ): NegotiationResult {
        val result = MaOfferEngine.simulateNegotiation(offer, counterMultiplierDemand, counterStakeDemand)
        _lastNegotiationResult.value = result

        when (result.outcome) {
            NegotiationOutcome.ACCEPTED -> {
                val updatedOffer = offer.copy(
                    valuationMultiplier = result.agreedMultiplier,
                    stakePercent = result.agreedStake,
                    status = OfferStatus.NEGOTIATING,
                    counterRoundsCount = offer.counterRoundsCount + 1
                )
                _activePopupOffer.value = updatedOffer
            }
            NegotiationOutcome.RE_COUNTERED -> {
                val updatedOffer = offer.copy(
                    valuationMultiplier = result.agreedMultiplier,
                    stakePercent = result.agreedStake,
                    status = OfferStatus.NEGOTIATING,
                    counterRoundsCount = offer.counterRoundsCount + 1
                )
                _activePopupOffer.value = updatedOffer
            }
            NegotiationOutcome.WALKED_AWAY -> {
                val abortedOffer = offer.copy(status = OfferStatus.WITHDRAWN)
                _dealHistory.value = listOf(abortedOffer) + _dealHistory.value
                _pendingStalledOffers.value = _pendingStalledOffers.value.filter { it.offerId != offer.offerId }
            }
        }

        return result
    }

    /**
     * Executes monthly tick progression for stalled offers and checks for new unsolicited offers.
     */
    fun processMonthlyTick(
        playerState: PlayerState,
        currentMonth: Int,
        currentYear: Int
    ): Pair<PlayerState, List<String>> {
        val notifications = mutableListOf<String>()

        // 1. Advance stalled offers (decrement ticks remaining)
        val nextStalled = mutableListOf<AcquisitionOffer>()
        _pendingStalledOffers.value.forEach { stalled ->
            val updatedTicks = stalled.stalledTicksRemaining - 1
            if (updatedTicks <= 0) {
                val expired = stalled.copy(status = OfferStatus.EXPIRED, stalledTicksRemaining = 0)
                _dealHistory.value = listOf(expired) + _dealHistory.value
                notifications.add("Tawaran akuisisi dari ${stalled.bidder.name} pada ${stalled.targetEntityName} telah KEDALUWARSA.")
            } else {
                nextStalled.add(stalled.copy(stalledTicksRemaining = updatedTicks))
            }
        }
        _pendingStalledOffers.value = nextStalled

        // 2. Evaluate unsolicited offer RNG trigger (if no popup active)
        if (_activePopupOffer.value == null) {
            if (MaOfferEngine.shouldTriggerEvent(playerState)) {
                val newOffer = MaOfferEngine.generateOffer(playerState, currentMonth, currentYear)
                if (newOffer != null) {
                    _activePopupOffer.value = newOffer
                    notifications.add("SURAT MASUK: ${newOffer.bidder.name} mengajukan penawaran akuisisi untuk ${newOffer.targetEntityName}!")
                }
            }
        }

        return Pair(playerState, notifications)
    }

    /**
     * Debug/Testing helper to force trigger an M&A offer.
     */
    fun forceTriggerOffer(playerState: PlayerState, currentMonth: Int, currentYear: Int): Boolean {
        val newOffer = MaOfferEngine.generateOffer(playerState, currentMonth, currentYear)
        if (newOffer != null) {
            _activePopupOffer.value = newOffer
            return true
        }
        return false
    }
}
