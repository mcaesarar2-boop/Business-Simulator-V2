package com.caesar.gametycoon.engine.cashflow

import com.caesar.gametycoon.engine.cashflow.adapters.*
import com.example.corporate.model.CashDistributionResult
import com.example.corporate.model.CorporateTier
import com.example.corporate.model.MergedDividendPolicy
import com.example.corporate.model.StandaloneDividendPolicy
import com.example.corporate.repository.CorporatePolicyRepository
import com.example.data.HoldingCompany
import com.example.data.OwnedBusiness
import com.example.data.PlayerState

/**
 * Granular cash flow distribution audit result for a single business unit.
 */
data class UnitCashFlowResult(
    val businessId: String,
    val businessName: String,
    val catalogId: String,
    val grossRevenue: Long,
    val expenses: Long,
    val netIncome: Long,
    val isMerged: Boolean,
    val parentHoldingId: String?,
    val retainedInUnit: Long,
    val holdingTreasuryPayout: Long,
    val megaHoldingPayout: Long,
    val lossCoveredByUnit: Long = 0L,
    val lossCoveredByHolding: Long = 0L,
    val lossPassedToMegaHolding: Long = 0L,
    val updatedBusiness: OwnedBusiness
) {
    /**
     * Converts to CashDistributionResult for seamless backwards compatibility
     * with CorporatePolicyRepository, UI dialogs, and existing tests.
     */
    fun toCashDistributionResult(): CashDistributionResult {
        return CashDistributionResult(
            businessId = businessId,
            businessName = businessName,
            netProfit = netIncome,
            retainedInUnit = retainedInUnit,
            holdingTreasuryTransfer = holdingTreasuryPayout,
            megaHoldingDividend = megaHoldingPayout,
            absorbedDeficit = lossCoveredByUnit + lossCoveredByHolding,
            sourceTier = CorporateTier.BUSINESS_UNIT,
            parentHoldingId = parentHoldingId
        )
    }
}

/**
 * Aggregate execution result for a Holding Company and its subsidiaries.
 */
data class HoldingPipelineExecutionResult(
    val updatedHolding: HoldingCompany,
    val holdingTreasuryNetDelta: Double,
    val megaHoldingPayoutDelta: Long,
    val subsidiaryResults: List<UnitCashFlowResult>
)

/**
 * Global execution result for a complete monthly corporate cash flow tick sweep.
 */
data class PipelineExecutionResult(
    val updatedState: PlayerState,
    val totalGrossRevenue: Long,
    val totalExpenses: Long,
    val totalNetIncome: Long,
    val totalUnitRetained: Long,
    val totalHoldingTreasuryInflow: Long,
    val totalMegaHoldingPayout: Long,
    val totalLossCoveredByUnits: Long,
    val totalLossCoveredByHoldings: Long,
    val totalLossPassedToMegaHolding: Long,
    val unitResults: List<UnitCashFlowResult>
)

/**
 * Central Cash Flow Pipeline Orchestrator.
 *
 * Implements the Universal 3-Tier Cascading Cash Flow Engine Rules:
 *
 * STEP 1: Local Unit Financial Resolution
 *   1. Calculate UnitNetIncome = UnitGrossRevenue - UnitExpenses.
 *   2. If UnitNetIncome < 0 (Net Loss):
 *      - Subtract loss directly from Unit.companyCash.
 *      - If Unit.companyCash reaches $0, pass remaining debt up to
 *        ParentHolding.holdingCash (if merged) or MegaHolding.cash (if standalone).
 *   3. If UnitNetIncome > 0 (Net Profit): Proceed to STEP 2.
 *
 * STEP 2: Hierarchy Routing Check
 *   - val isMerged = !unit.parentId.isNullOrEmpty() && findHolding(unit.parentId) != null
 *
 * STEP 3: Percentage-Based Dividend & Retention Split
 *   - SCENARIO A: Standalone Unit (isMerged == false -> 2-Tier):
 *       RetainedUnitCash = UnitNetIncome * (StandaloneUnitRetainPct / 100.0)
 *       MegaHoldingPayout = UnitNetIncome * (StandaloneMegaHoldingPayoutPct / 100.0)
 *       Execution: unit.companyCash += RetainedUnitCash, megaHoldingState.cash += MegaHoldingPayout
 *
 *   - SCENARIO B: Merged Subsidiary (isMerged == true -> 3-Tier):
 *       RetainedUnitCash = UnitNetIncome * (MergedUnitRetainPct / 100.0)
 *       HoldingTreasuryPayout = UnitNetIncome * (MergedHoldingTreasuryPct / 100.0)
 *       MegaHoldingDividend = UnitNetIncome * (MergedMegaHoldingDividendPct / 100.0)
 *       Execution:
 *         unit.companyCash += RetainedUnitCash
 *         parentHolding.holdingCash += HoldingTreasuryPayout  <-- FIXES $0 HOLDING PAYOUT BUG
 *         megaHoldingState.cash += MegaHoldingDividend
 */
object CentralCashFlowPipelineEngine {

    private val adapters: List<BusinessCashFlowAdapter> = listOf(
        FilmStudioCashAdapter(),
        ContentCreatorCashAdapter(),
        HospitalityCashAdapter(),
        AiCloudCashAdapter(),
        BankingCashAdapter(),
        AviationCashAdapter(),
        ConstructionCashAdapter(),
        FootballClubCashAdapter(),
        GenericCatalogCashAdapter()
    )

    /**
     * Resolves the appropriate adapter for any given business unit.
     */
    fun findAdapter(business: OwnedBusiness): BusinessCashFlowAdapter {
        return adapters.firstOrNull { it.canHandle(business) } ?: GenericCatalogCashAdapter()
    }

    /**
     * Finds the parent holding company for a business unit if it is merged.
     */
    fun findHolding(parentId: String?, holdingCompanies: List<HoldingCompany>): HoldingCompany? {
        if (parentId.isNullOrEmpty()) return null
        return holdingCompanies.firstOrNull { it.instanceId == parentId }
    }

    /**
     * Structural hierarchy check evaluating if a unit is merged into a Holding Company.
     */
    fun isUnitMerged(unit: OwnedBusiness, holdingCompanies: List<HoldingCompany>): Boolean {
        if (!unit.parentId.isNullOrEmpty() && findHolding(unit.parentId, holdingCompanies) != null) {
            return true
        }
        return holdingCompanies.any { holding ->
            holding.subsidiaries.any { it.instanceId == unit.instanceId }
        }
    }

    /**
     * Resolves local unit financials using the registered modular adapter.
     */
    fun resolveUnitFinancials(business: OwnedBusiness, state: PlayerState): UnitFinancialResolution {
        val adapter = findAdapter(business)
        return adapter.resolveFinancials(business, state)
    }

    /**
     * Executes the standardized 3-step pipeline on an individual business unit.
     *
     * @return Pair of [UnitCashFlowResult] and holdingCash delta (positive for treasury inflow,
     *         negative if holding absorbed debt).
     */
    fun processUnitCashFlow(
        unit: OwnedBusiness,
        parentHolding: HoldingCompany?,
        state: PlayerState,
        standalonePolicy: StandaloneDividendPolicy = StandaloneDividendPolicy.DEFAULT,
        mergedPolicy: MergedDividendPolicy = MergedDividendPolicy.DEFAULT,
        customFinancialResolution: UnitFinancialResolution? = null
    ): Pair<UnitCashFlowResult, Double> {
        // STEP 1: Local Unit Financial Resolution
        val resolution = customFinancialResolution ?: resolveUnitFinancials(unit, state)
        val gross = resolution.grossRevenue
        val expenses = resolution.expenses
        val unitNetIncome = gross - expenses
        var currentBusiness = resolution.updatedBusiness
        val businessName = currentBusiness.customName ?: currentBusiness.catalogId

        // STEP 2: Hierarchy Routing Check
        val actualHolding = parentHolding ?: (
            findHolding(unit.parentId, state.holdingCompanies) ?:
            state.holdingCompanies.firstOrNull { it.subsidiaries.any { s -> s.instanceId == unit.instanceId } }
        )
        val isMerged = actualHolding != null

        // Branch by Profit / Loss
        if (unitNetIncome < 0L) {
            // STEP 1.2: Net Loss handling with cascading debt absorption
            val loss = -unitNetIncome
            val availableUnitCash = currentBusiness.companyCash.toLong().coerceAtLeast(0L)
            val lossCoveredByUnit = minOf(availableUnitCash, loss)
            val remainingDebt = loss - lossCoveredByUnit

            val newUnitCash = (currentBusiness.companyCash - lossCoveredByUnit.toDouble()).coerceAtLeast(0.0)
            currentBusiness = currentBusiness.copy(companyCash = newUnitCash)
            if (currentBusiness.catalogId == "content_creator") {
                currentBusiness = currentBusiness.copy(contentCreatorCash = newUnitCash.toLong())
            }

            if (isMerged && actualHolding != null) {
                // Merged: Parent Holding treasury absorbs next
                val availableHoldingCash = actualHolding.holdingCash.toLong().coerceAtLeast(0L)
                val lossCoveredByHolding = minOf(availableHoldingCash, remainingDebt)
                val remainingDebtAfterHolding = remainingDebt - lossCoveredByHolding
                val lossPassedToMegaHolding = remainingDebtAfterHolding

                val result = UnitCashFlowResult(
                    businessId = currentBusiness.instanceId,
                    businessName = businessName,
                    catalogId = currentBusiness.catalogId,
                    grossRevenue = gross,
                    expenses = expenses,
                    netIncome = unitNetIncome,
                    isMerged = true,
                    parentHoldingId = actualHolding.instanceId,
                    retainedInUnit = -lossCoveredByUnit,
                    holdingTreasuryPayout = -lossCoveredByHolding,
                    megaHoldingPayout = -lossPassedToMegaHolding,
                    lossCoveredByUnit = lossCoveredByUnit,
                    lossCoveredByHolding = lossCoveredByHolding,
                    lossPassedToMegaHolding = lossPassedToMegaHolding,
                    updatedBusiness = currentBusiness
                )
                return Pair(result, -lossCoveredByHolding.toDouble())
            } else {
                // Standalone: Remaining debt passed directly to Mega Holding (Root Cash)
                val lossPassedToMegaHolding = remainingDebt

                val result = UnitCashFlowResult(
                    businessId = currentBusiness.instanceId,
                    businessName = businessName,
                    catalogId = currentBusiness.catalogId,
                    grossRevenue = gross,
                    expenses = expenses,
                    netIncome = unitNetIncome,
                    isMerged = false,
                    parentHoldingId = null,
                    retainedInUnit = -lossCoveredByUnit,
                    holdingTreasuryPayout = 0L,
                    megaHoldingPayout = -lossPassedToMegaHolding,
                    lossCoveredByUnit = lossCoveredByUnit,
                    lossCoveredByHolding = 0L,
                    lossPassedToMegaHolding = lossPassedToMegaHolding,
                    updatedBusiness = currentBusiness
                )
                return Pair(result, 0.0)
            }
        } else if (unitNetIncome == 0L) {
            val result = UnitCashFlowResult(
                businessId = currentBusiness.instanceId,
                businessName = businessName,
                catalogId = currentBusiness.catalogId,
                grossRevenue = gross,
                expenses = expenses,
                netIncome = 0L,
                isMerged = isMerged,
                parentHoldingId = actualHolding?.instanceId,
                retainedInUnit = 0L,
                holdingTreasuryPayout = 0L,
                megaHoldingPayout = 0L,
                updatedBusiness = currentBusiness
            )
            return Pair(result, 0.0)
        } else {
            // STEP 3: Percentage-Based Dividend & Retention Split
            if (!isMerged) {
                // SCENARIO A: STANDALONE UNIT (2-Tier Architecture)
                val retainPct = standalonePolicy.unitRetainedPercent
                val retainedUnitCash = (unitNetIncome * (retainPct / 100.0)).toLong()
                val megaHoldingPayout = unitNetIncome - retainedUnitCash

                val newUnitCash = currentBusiness.companyCash + retainedUnitCash.toDouble()
                currentBusiness = currentBusiness.copy(companyCash = newUnitCash)
                if (currentBusiness.catalogId == "content_creator") {
                    currentBusiness = currentBusiness.copy(contentCreatorCash = newUnitCash.toLong())
                }

                val result = UnitCashFlowResult(
                    businessId = currentBusiness.instanceId,
                    businessName = businessName,
                    catalogId = currentBusiness.catalogId,
                    grossRevenue = gross,
                    expenses = expenses,
                    netIncome = unitNetIncome,
                    isMerged = false,
                    parentHoldingId = null,
                    retainedInUnit = retainedUnitCash,
                    holdingTreasuryPayout = 0L,
                    megaHoldingPayout = megaHoldingPayout,
                    updatedBusiness = currentBusiness
                )
                return Pair(result, 0.0)
            } else {
                // SCENARIO B: MERGED SUBSIDIARY (3-Tier Architecture)
                val retainPct = mergedPolicy.unitRetainedPercent
                val holdingTreasuryPct = mergedPolicy.holdingTreasuryPercent
                val retainedUnitCash = (unitNetIncome * (retainPct / 100.0)).toLong()
                val holdingTreasuryPayout = (unitNetIncome * (holdingTreasuryPct / 100.0)).toLong()
                val megaHoldingDividend = (unitNetIncome - retainedUnitCash - holdingTreasuryPayout).coerceAtLeast(0L)

                val newUnitCash = currentBusiness.companyCash + retainedUnitCash.toDouble()
                currentBusiness = currentBusiness.copy(companyCash = newUnitCash)
                if (currentBusiness.catalogId == "content_creator") {
                    currentBusiness = currentBusiness.copy(contentCreatorCash = newUnitCash.toLong())
                }

                val result = UnitCashFlowResult(
                    businessId = currentBusiness.instanceId,
                    businessName = businessName,
                    catalogId = currentBusiness.catalogId,
                    grossRevenue = gross,
                    expenses = expenses,
                    netIncome = unitNetIncome,
                    isMerged = true,
                    parentHoldingId = actualHolding?.instanceId,
                    retainedInUnit = retainedUnitCash,
                    holdingTreasuryPayout = holdingTreasuryPayout,
                    megaHoldingPayout = megaHoldingDividend,
                    updatedBusiness = currentBusiness
                )
                return Pair(result, holdingTreasuryPayout.toDouble())
            }
        }
    }

    /**
     * Executes cash distribution across an entire Holding Company and its subsidiaries.
     */
    fun processHoldingCompanyCashFlow(
        holding: HoldingCompany,
        state: PlayerState,
        policy: MergedDividendPolicy = MergedDividendPolicy.DEFAULT,
        overrideSubsidiaryNetProfits: Map<String, Long> = emptyMap()
    ): HoldingPipelineExecutionResult {
        var currentTreasuryCash = holding.holdingCash
        var totalMegaPayout = 0L
        val subResults = mutableListOf<UnitCashFlowResult>()

        val updatedSubs = holding.subsidiaries.map { sub ->
            val customResolution = overrideSubsidiaryNetProfits[sub.instanceId]?.let { net ->
                UnitFinancialResolution(
                    grossRevenue = if (net >= 0) net else 0L,
                    expenses = if (net < 0) -net else 0L,
                    updatedBusiness = sub
                )
            }

            val (subResult, holdingDelta) = processUnitCashFlow(
                unit = sub,
                parentHolding = holding.copy(holdingCash = currentTreasuryCash),
                state = state,
                mergedPolicy = policy,
                customFinancialResolution = customResolution
            )

            currentTreasuryCash = (currentTreasuryCash + holdingDelta).coerceAtLeast(0.0)
            totalMegaPayout += subResult.megaHoldingPayout
            subResults.add(subResult)
            subResult.updatedBusiness
        }

        val updatedHolding = holding.copy(
            subsidiaries = updatedSubs,
            holdingCash = currentTreasuryCash
        )

        return HoldingPipelineExecutionResult(
            updatedHolding = updatedHolding,
            holdingTreasuryNetDelta = currentTreasuryCash - holding.holdingCash,
            megaHoldingPayoutDelta = totalMegaPayout,
            subsidiaryResults = subResults
        )
    }

    /**
     * Executes the universal 3-tier cash flow pipeline sweep across all business units
     * and holding companies in the game state.
     */
    fun processMonthlyCashFlow(
        currentState: PlayerState,
        policyRepo: CorporatePolicyRepository = CorporatePolicyRepository.getInstance()
    ): PipelineExecutionResult {
        val unitResults = mutableListOf<UnitCashFlowResult>()
        val distributionResults = mutableListOf<CashDistributionResult>()

        var totalGrossRevenue = 0L
        var totalExpenses = 0L
        var totalNetIncome = 0L
        var totalUnitRetained = 0L
        var totalHoldingTreasuryInflow = 0L
        var totalMegaHoldingPayout = 0L
        var totalLossCoveredByUnits = 0L
        var totalLossCoveredByHoldings = 0L
        var totalLossPassedToMegaHolding = 0L

        // 1. Process Standalone Units
        val updatedOwnedBusinesses = currentState.ownedBusinesses.map { biz ->
            val isMerged = isUnitMerged(biz, currentState.holdingCompanies)
            if (!isMerged) {
                val policy = policyRepo.getStandalonePolicy(biz.instanceId)
                val (result, _) = processUnitCashFlow(
                    unit = biz,
                    parentHolding = null,
                    state = currentState,
                    standalonePolicy = policy
                )

                unitResults.add(result)
                distributionResults.add(result.toCashDistributionResult())

                totalGrossRevenue += result.grossRevenue
                totalExpenses += result.expenses
                totalNetIncome += result.netIncome
                totalUnitRetained += result.retainedInUnit
                totalMegaHoldingPayout += result.megaHoldingPayout
                totalLossCoveredByUnits += result.lossCoveredByUnit
                totalLossPassedToMegaHolding += result.lossPassedToMegaHolding

                result.updatedBusiness
            } else {
                biz
            }
        }

        // 2. Process Holding Companies & Merged Subsidiaries
        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            val holdingPolicy = policyRepo.getHoldingPolicy(holding.instanceId)
            val holdingExec = processHoldingCompanyCashFlow(
                holding = holding,
                state = currentState,
                policy = holdingPolicy
            )

            holdingExec.subsidiaryResults.forEach { subResult ->
                unitResults.add(subResult)
                distributionResults.add(subResult.toCashDistributionResult())

                totalGrossRevenue += subResult.grossRevenue
                totalExpenses += subResult.expenses
                totalNetIncome += subResult.netIncome
                totalUnitRetained += subResult.retainedInUnit
                totalHoldingTreasuryInflow += subResult.holdingTreasuryPayout
                totalMegaHoldingPayout += subResult.megaHoldingPayout
                totalLossCoveredByUnits += subResult.lossCoveredByUnit
                totalLossCoveredByHoldings += subResult.lossCoveredByHolding
                totalLossPassedToMegaHolding += subResult.lossPassedToMegaHolding
            }

            holdingExec.updatedHolding
        }

        // 3. Record full audit trail in policy repository
        if (distributionResults.isNotEmpty()) {
            policyRepo.recordDistributionResults(distributionResults)
        }

        // 4. Update Root Mega Holding balance strictly with net payout
        val updatedRootCash = (currentState.cash + totalMegaHoldingPayout).coerceAtLeast(0L)

        val updatedState = currentState.copy(
            cash = updatedRootCash,
            ownedBusinesses = updatedOwnedBusinesses,
            holdingCompanies = updatedHoldings
        )

        return PipelineExecutionResult(
            updatedState = updatedState,
            totalGrossRevenue = totalGrossRevenue,
            totalExpenses = totalExpenses,
            totalNetIncome = totalNetIncome,
            totalUnitRetained = totalUnitRetained,
            totalHoldingTreasuryInflow = totalHoldingTreasuryInflow,
            totalMegaHoldingPayout = totalMegaHoldingPayout,
            totalLossCoveredByUnits = totalLossCoveredByUnits,
            totalLossCoveredByHoldings = totalLossCoveredByHoldings,
            totalLossPassedToMegaHolding = totalLossPassedToMegaHolding,
            unitResults = unitResults
        )
    }
}
