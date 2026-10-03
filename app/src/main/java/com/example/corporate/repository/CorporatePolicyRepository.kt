package com.example.corporate.repository

import com.example.corporate.model.CashDistributionResult
import com.example.corporate.model.DividendPolicyPreset
import com.example.corporate.model.HoldingCashFlowSummary
import com.example.corporate.model.MergedDividendPolicy
import com.example.corporate.model.StandaloneDividendPolicy
import com.example.data.HoldingCompany
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository managing corporate dividend policies, profit allocation sliders,
 * and distribution history across Tier 1, Tier 2, and Tier 3 entities.
 * Fully decoupled from GameViewModel.
 */
class CorporatePolicyRepository private constructor() {

    private val _standalonePolicies = MutableStateFlow<Map<String, StandaloneDividendPolicy>>(emptyMap())
    val standalonePolicies: StateFlow<Map<String, StandaloneDividendPolicy>> = _standalonePolicies.asStateFlow()

    private val _holdingPolicies = MutableStateFlow<Map<String, MergedDividendPolicy>>(emptyMap())
    val holdingPolicies: StateFlow<Map<String, MergedDividendPolicy>> = _holdingPolicies.asStateFlow()

    private val _lastDistributionHistory = MutableStateFlow<List<CashDistributionResult>>(emptyList())
    val lastDistributionHistory: StateFlow<List<CashDistributionResult>> = _lastDistributionHistory.asStateFlow()

    fun getStandalonePolicy(businessId: String): StandaloneDividendPolicy {
        return _standalonePolicies.value[businessId] ?: StandaloneDividendPolicy.DEFAULT
    }

    fun getHoldingPolicy(holdingId: String): MergedDividendPolicy {
        return _holdingPolicies.value[holdingId] ?: MergedDividendPolicy.DEFAULT
    }

    fun setStandalonePolicy(businessId: String, policy: StandaloneDividendPolicy) {
        val current = _standalonePolicies.value.toMutableMap()
        current[businessId] = policy
        _standalonePolicies.value = current
    }

    fun setHoldingPolicy(holdingId: String, policy: MergedDividendPolicy) {
        val current = _holdingPolicies.value.toMutableMap()
        current[holdingId] = policy
        _holdingPolicies.value = current
    }

    fun applyPresetToStandalone(businessId: String, preset: DividendPolicyPreset) {
        val policy = when (preset) {
            DividendPolicyPreset.GROWTH_REINVESTMENT -> StandaloneDividendPolicy.GROWTH
            DividendPolicyPreset.CASH_COW_LIQUIDITY -> StandaloneDividendPolicy.CASH_COW
            else -> StandaloneDividendPolicy.DEFAULT
        }
        setStandalonePolicy(businessId, policy)
    }

    fun applyPresetToHolding(holdingId: String, preset: DividendPolicyPreset) {
        val policy = when (preset) {
            DividendPolicyPreset.GROWTH_REINVESTMENT -> MergedDividendPolicy.GROWTH
            DividendPolicyPreset.CASH_COW_LIQUIDITY -> MergedDividendPolicy.CASH_COW
            DividendPolicyPreset.TREASURY_ACCUMULATION -> MergedDividendPolicy.TREASURY_FOCUSED
            else -> MergedDividendPolicy.DEFAULT
        }
        setHoldingPolicy(holdingId, policy)
    }

    fun recordDistributionResults(results: List<CashDistributionResult>) {
        _lastDistributionHistory.value = results
    }

    fun getHoldingSummary(holding: HoldingCompany): HoldingCashFlowSummary {
        val history = _lastDistributionHistory.value.filter { it.parentHoldingId == holding.instanceId }
        val totalNet = history.sumOf { it.netProfit }
        val retained = history.sumOf { it.retainedInUnit }
        val toTreasury = history.sumOf { it.holdingTreasuryTransfer }
        val toMega = history.sumOf { it.megaHoldingDividend }

        return HoldingCashFlowSummary(
            holdingId = holding.instanceId,
            holdingName = holding.name,
            totalSubsidiariesNet = totalNet,
            retainedInSubsidiaries = retained,
            depositedToTreasury = toTreasury,
            paidToMegaHolding = toMega,
            currentTreasuryCash = holding.holdingCash
        )
    }

    companion object {
        @Volatile
        private var instance: CorporatePolicyRepository? = null

        fun getInstance(): CorporatePolicyRepository {
            return instance ?: synchronized(this) {
                instance ?: CorporatePolicyRepository().also { instance = it }
            }
        }
    }
}
