package com.example.aicloud.data

import android.content.Context
import android.content.SharedPreferences
import com.example.aicloud.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

/**
 * Isolated persistence and business operations repository for AI Cloud Provider & Infrastructure.
 */
class AiCloudRepository private constructor(context: Context? = null) {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("ai_cloud_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _companyName = MutableStateFlow("Apex Neural Infrastructure Corp.")
    val companyName: StateFlow<String> = _companyName.asStateFlow()

    private val _companyBalance = MutableStateFlow(5_000_000L) // Default internal treasury
    val companyBalance: StateFlow<Long> = _companyBalance.asStateFlow()

    private val _dataCenters = MutableStateFlow<List<AiDataCenter>>(emptyList())
    val dataCenters: StateFlow<List<AiDataCenter>> = _dataCenters.asStateFlow()

    private val _activeContracts = MutableStateFlow<List<ClientContract>>(emptyList())
    val activeContracts: StateFlow<List<ClientContract>> = _activeContracts.asStateFlow()

    private val _marketContracts = MutableStateFlow<List<ClientContract>>(AiCloudInitialData.generateInitialMarketContracts())
    val marketContracts: StateFlow<List<ClientContract>> = _marketContracts.asStateFlow()

    private val _unlockedResearchIds = MutableStateFlow<Set<String>>(emptySet())
    val unlockedResearchIds: StateFlow<Set<String>> = _unlockedResearchIds.asStateFlow()

    private val _lastReport = MutableStateFlow<AiCloudMonthlyReport?>(null)
    val lastReport: StateFlow<AiCloudMonthlyReport?> = _lastReport.asStateFlow()

    private val _incidents = MutableStateFlow<List<AiCloudIncident>>(emptyList())
    val incidents: StateFlow<List<AiCloudIncident>> = _incidents.asStateFlow()

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        loadFromStorage()
    }

    companion object {
        @Volatile
        private var INSTANCE: AiCloudRepository? = null

        fun initialize(context: Context): AiCloudRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AiCloudRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun getInstance(): AiCloudRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AiCloudRepository(null).also { INSTANCE = it }
            }
        }
    }

    /**
     * Synchronizes internal treasury balance with the canonical corporate OwnedBusiness cash.
     */
    fun syncExternalBalance(canonicalBalance: Long) {
        if (canonicalBalance >= 0 && _companyBalance.value != canonicalBalance) {
            _companyBalance.value = canonicalBalance
            saveToStorage()
        }
    }

    /**
     * Initializes a starter data center if player just bought the business.
     */
    fun setupInitialBusiness(customName: String = "Apex Neural Infrastructure Corp.", initialBalance: Long? = null) {
        _companyName.value = customName
        if (initialBalance != null && initialBalance > 0) {
            _companyBalance.value = initialBalance
        }
        if (_dataCenters.value.isEmpty()) {
            val starterDc = AiDataCenter(
                name = "Facility Alpha - Ashburn",
                region = CloudRegion.US_EAST_VIRGINIA,
                tier = DataCenterTier.TIER_1_STANDARD,
                coolingSystem = CoolingSystemType.AIR_COOLED_STANDARD,
                powerSource = PowerSourceType.MUNICIPAL_GRID,
                serverRacks = listOf(
                    ServerRackUnit(
                        customName = "Rack Pod A1",
                        gpuTier = GpuChipTier.ENTERPRISE_A100_80G,
                        quantity = 4,
                        healthConditionPct = 100.0,
                        ageMonths = 2
                    ),
                    ServerRackUnit(
                        customName = "Rack Pod A2",
                        gpuTier = GpuChipTier.BUDGET_L40S,
                        quantity = 6,
                        healthConditionPct = 100.0,
                        ageMonths = 4
                    )
                ),
                constructionMonthsRemaining = 0,
                isOperational = true
            )
            _dataCenters.value = listOf(starterDc)
        }
        _isInitialized.value = true
        saveToStorage()
    }

    fun updateCompanyName(newName: String) {
        _companyName.value = newName
        saveToStorage()
    }

    fun depositCapital(amount: Long): Boolean {
        if (amount <= 0) return false
        _companyBalance.value += amount
        saveToStorage()
        return true
    }

    fun withdrawCapital(amount: Long): Boolean {
        if (amount <= 0 || _companyBalance.value < amount) return false
        _companyBalance.value -= amount
        saveToStorage()
        return true
    }

    fun buildDataCenter(
        name: String,
        region: CloudRegion,
        tier: DataCenterTier,
        initialCooling: CoolingSystemType = CoolingSystemType.AIR_COOLED_STANDARD,
        initialPower: PowerSourceType = PowerSourceType.MUNICIPAL_GRID
    ): Pair<Boolean, String> {
        val totalCost = region.baseLandCostUsd + tier.constructionCostUsd
        if (_companyBalance.value < totalCost) {
            return Pair(false, "Insufficient internal company funds (Need $${String.format("%,d", totalCost)} USD).")
        }

        _companyBalance.value -= totalCost
        val newDc = AiDataCenter(
            name = name.ifBlank { "${region.displayName} DC #${_dataCenters.value.size + 1}" },
            region = region,
            tier = tier,
            coolingSystem = initialCooling,
            powerSource = initialPower,
            serverRacks = emptyList(),
            constructionMonthsRemaining = tier.buildMonths,
            isOperational = false
        )
        _dataCenters.value = _dataCenters.value + newDc
        addIncident(
            title = "New Data Center Groundbroken",
            description = "Construction begun on ${newDc.name} in ${region.displayName}. ETA: ${tier.buildMonths} months.",
            severity = IncidentSeverity.INFO
        )
        saveToStorage()
        return Pair(true, "Data Center construction project launched successfully!")
    }

    fun deployServerRack(
        dataCenterId: String,
        gpuTier: GpuChipTier,
        quantity: Int
    ): Pair<Boolean, String> {
        val dc = _dataCenters.value.find { it.id == dataCenterId } ?: return Pair(false, "Facility not found.")
        if (!dc.isOperational) return Pair(false, "Facility is still under construction.")

        val availableRackSlots = dc.tier.maxRackUnits - dc.totalRackUnitsUsed
        if (quantity > availableRackSlots) {
            return Pair(false, "Not enough rack unit floor capacity ($availableRackSlots slots remaining in tier).")
        }

        // Check if custom TPU requires research
        if (gpuTier == GpuChipTier.CUSTOM_ASIC_TPU_V5P && !_unlockedResearchIds.value.contains("tech_silicon_custom_tpu")) {
            return Pair(false, "Custom TPU v5p requires 'Proprietary ASIC Microarchitecture' R&D unlock first.")
        }

        val totalCost = gpuTier.costPerRackUsd * quantity
        if (_companyBalance.value < totalCost) {
            return Pair(false, "Insufficient funds to procure $quantity rack units ($${String.format("%,d", totalCost)} USD required).")
        }

        _companyBalance.value -= totalCost
        val newRack = ServerRackUnit(
            customName = "${gpuTier.displayName} Pod #${dc.serverRacks.size + 1}",
            gpuTier = gpuTier,
            quantity = quantity,
            healthConditionPct = 100.0,
            isOperational = true
        )
        val updatedDcs = _dataCenters.value.map {
            if (it.id == dataCenterId) it.copy(serverRacks = it.serverRacks + newRack) else it
        }
        _dataCenters.value = updatedDcs
        saveToStorage()
        return Pair(true, "Successfully deployed $quantity ${gpuTier.displayName} racks.")
    }

    fun upgradeCoolingSystem(dataCenterId: String, newCooling: CoolingSystemType): Pair<Boolean, String> {
        val dc = _dataCenters.value.find { it.id == dataCenterId } ?: return Pair(false, "Facility not found.")
        if (dc.coolingSystem == newCooling) return Pair(false, "Cooling system already active.")

        if (_companyBalance.value < newCooling.upgradeCostUsd) {
            return Pair(false, "Insufficient funds for cooling upgrade ($${String.format("%,d", newCooling.upgradeCostUsd)} USD).")
        }

        _companyBalance.value -= newCooling.upgradeCostUsd
        val updatedDcs = _dataCenters.value.map {
            if (it.id == dataCenterId) it.copy(coolingSystem = newCooling) else it
        }
        _dataCenters.value = updatedDcs
        saveToStorage()
        return Pair(true, "Facility cooling retrofitted to ${newCooling.displayName}.")
    }

    fun upgradePowerSource(dataCenterId: String, newPower: PowerSourceType): Pair<Boolean, String> {
        val dc = _dataCenters.value.find { it.id == dataCenterId } ?: return Pair(false, "Facility not found.")
        if (dc.powerSource == newPower) return Pair(false, "Power source already configured.")

        if (_companyBalance.value < newPower.upgradeCostUsd) {
            return Pair(false, "Insufficient funds for substation upgrade ($${String.format("%,d", newPower.upgradeCostUsd)} USD).")
        }

        _companyBalance.value -= newPower.upgradeCostUsd
        val updatedDcs = _dataCenters.value.map {
            if (it.id == dataCenterId) it.copy(powerSource = newPower) else it
        }
        _dataCenters.value = updatedDcs
        saveToStorage()
        return Pair(true, "Substation & grid micro-source upgraded to ${newPower.displayName}.")
    }

    fun repairHardware(dataCenterId: String, rackId: String): Pair<Boolean, String> {
        val dc = _dataCenters.value.find { it.id == dataCenterId } ?: return Pair(false, "Facility not found.")
        val rack = dc.serverRacks.find { it.id == rackId } ?: return Pair(false, "Rack unit not found.")

        val repairCost = ((100.0 - rack.healthConditionPct) * 150.0 * rack.quantity).toLong().coerceAtLeast(1_000L)
        if (_companyBalance.value < repairCost) {
            return Pair(false, "Insufficient funds for preventive maintenance ($${String.format("%,d", repairCost)} USD).")
        }

        _companyBalance.value -= repairCost
        val updatedRacks = dc.serverRacks.map {
            if (it.id == rackId) it.copy(healthConditionPct = 100.0, isOperational = true) else it
        }
        val updatedDcs = _dataCenters.value.map {
            if (it.id == dataCenterId) it.copy(serverRacks = updatedRacks) else it
        }
        _dataCenters.value = updatedDcs
        saveToStorage()
        return Pair(true, "Hardware rack inspected, thermal paste replaced, and restored to 100% health.")
    }

    /**
     * Performs routine servicing (cleaning, diagnostic sweep, thermal paste re-application).
     * Costs a modest service fee and boosts health condition by +20% (up to 98%).
     */
    fun serviceHardware(dataCenterId: String, rackId: String): Pair<Boolean, String> {
        val dc = _dataCenters.value.find { it.id == dataCenterId } ?: return Pair(false, "Facility not found.")
        val rack = dc.serverRacks.find { it.id == rackId } ?: return Pair(false, "Rack unit not found.")

        val cost = rack.routineServiceCostUsd
        if (_companyBalance.value < cost) {
            return Pair(false, "Dana kas internal tidak mencukupi untuk servis rutin ($${String.format("%,d", cost)} USD).")
        }

        _companyBalance.value -= cost
        val newHealth = (rack.healthConditionPct + 20.0).coerceAtMost(98.0)
        val updatedRacks = dc.serverRacks.map {
            if (it.id == rackId) it.copy(healthConditionPct = newHealth, isOperational = true) else it
        }
        _dataCenters.value = _dataCenters.value.map {
            if (it.id == dataCenterId) it.copy(serverRacks = updatedRacks) else it
        }
        saveToStorage()
        return Pair(true, "Servis berkala berhasil dilakukan. Kesehatan GPU kini ${String.format("%.0f", newHealth)}%.")
    }

    /**
     * Trade-in old GPU rack for a newer generation model, discounting the trade-in salvage credit from the price.
     */
    fun tradeInGpuRack(dataCenterId: String, oldRackId: String, newGpuTier: GpuChipTier): Pair<Boolean, String> {
        val dc = _dataCenters.value.find { it.id == dataCenterId } ?: return Pair(false, "Facility not found.")
        val oldRack = dc.serverRacks.find { it.id == oldRackId } ?: return Pair(false, "Rack unit not found.")

        if (newGpuTier == GpuChipTier.CUSTOM_ASIC_TPU_V5P && !_unlockedResearchIds.value.contains("tech_silicon_custom_tpu")) {
            return Pair(false, "Custom TPU v5p memerlukan riset 'Proprietary ASIC Microarchitecture'.")
        }

        val tradeInCredit = oldRack.resaleValueUsd
        val newRackCost = newGpuTier.costPerRackUsd * oldRack.quantity
        val netPayable = (newRackCost - tradeInCredit).coerceAtLeast(0L)

        if (_companyBalance.value < netPayable) {
            return Pair(false, "Dana kas tidak cukup untuk tukar tambah. Biaya bersih: $${String.format("%,d", netPayable)} USD (Nilai tukar GPU lama: $${String.format("%,d", tradeInCredit)} USD).")
        }

        _companyBalance.value -= netPayable
        val newRack = ServerRackUnit(
            customName = "${newGpuTier.displayName} Pod #${dc.serverRacks.size + 1}",
            gpuTier = newGpuTier,
            quantity = oldRack.quantity,
            healthConditionPct = 100.0,
            isOperational = true,
            ageMonths = 0
        )
        val updatedRacks = dc.serverRacks.map {
            if (it.id == oldRackId) newRack else it
        }
        _dataCenters.value = _dataCenters.value.map {
            if (it.id == dataCenterId) it.copy(serverRacks = updatedRacks) else it
        }
        saveToStorage()
        return Pair(true, "Berhasil tukar tambah ke ${oldRack.quantity}x ${newGpuTier.displayName}! Nilai tukar: $${String.format("%,d", tradeInCredit)} USD.")
    }

    /**
     * Decommissions/sells an old rack unit, freeing up rack capacity and returning salvage cash to treasury.
     */
    fun decommissionRack(dataCenterId: String, rackId: String): Pair<Boolean, String> {
        val dc = _dataCenters.value.find { it.id == dataCenterId } ?: return Pair(false, "Facility not found.")
        val rack = dc.serverRacks.find { it.id == rackId } ?: return Pair(false, "Rack unit not found.")

        val salvageValue = rack.resaleValueUsd
        _companyBalance.value += salvageValue

        val updatedRacks = dc.serverRacks.filterNot { it.id == rackId }
        _dataCenters.value = _dataCenters.value.map {
            if (it.id == dataCenterId) it.copy(serverRacks = updatedRacks) else it
        }
        saveToStorage()
        return Pair(true, "Rack ${rack.gpuTier.displayName} berhasil dilepas & dijual. Kas bertambah +$${String.format("%,d", salvageValue)} USD.")
    }

    fun signContract(contractId: String): Pair<Boolean, String> {
        val contract = _marketContracts.value.find { it.id == contractId } ?: return Pair(false, "Contract offer expired.")

        val totalAvailableTflops = _dataCenters.value.filter { it.isOperational }.sumOf { it.totalComputeTflops }
        val currentCommittedTflops = _activeContracts.value.sumOf { it.requiredTflops }
        val freeTflops = (totalAvailableTflops - currentCommittedTflops).coerceAtLeast(0.0)

        if (freeTflops < contract.requiredTflops * 0.7) {
            return Pair(false, "Cluster compute shortage: You need at least ${String.format("%,.0f", contract.requiredTflops)} TFLOPS (Available: ${String.format("%,.0f", freeTflops)} TFLOPS).")
        }

        _activeContracts.value = _activeContracts.value + contract
        _marketContracts.value = _marketContracts.value.filterNot { it.id == contractId }
        addIncident(
            title = "Contract Executed: ${contract.clientName}",
            description = "Secured ${contract.workloadType.displayName} agreement for $${String.format("%,d", contract.monthlyPaymentUsd)}/mo over ${contract.contractDurationMonthsTotal} months.",
            severity = IncidentSeverity.INFO
        )
        saveToStorage()
        return Pair(true, "Enterprise SLA Contract signed with ${contract.clientName}!")
    }

    fun cancelContract(contractId: String): Pair<Boolean, String> {
        val contract = _activeContracts.value.find { it.id == contractId } ?: return Pair(false, "Contract not found.")
        val penalty = (contract.outagePenaltyUsd * 0.5).toLong()

        _companyBalance.value = (_companyBalance.value - penalty).coerceAtLeast(0L)
        _activeContracts.value = _activeContracts.value.filterNot { it.id == contractId }
        addIncident(
            title = "Contract Terminated Early: ${contract.clientName}",
            description = "Early cancellation penalty paid: $${String.format("%,d", penalty)} USD.",
            severity = IncidentSeverity.WARNING,
            financialImpactUsd = penalty
        )
        saveToStorage()
        return Pair(true, "Contract terminated. Paid early exit fee.")
    }

    fun unlockResearch(techId: String): Pair<Boolean, String> {
        val tech = AiCloudInitialData.RESEARCH_CATALOG.find { it.id == techId } ?: return Pair(false, "R&D project not found.")
        if (_unlockedResearchIds.value.contains(techId)) return Pair(false, "Technology already patented and implemented.")

        if (_companyBalance.value < tech.costUsd) {
            return Pair(false, "Insufficient research budget ($${String.format("%,d", tech.costUsd)} USD required).")
        }

        _companyBalance.value -= tech.costUsd
        _unlockedResearchIds.value = _unlockedResearchIds.value + techId
        addIncident(
            title = "R&D Breakthrough: ${tech.name}",
            description = tech.description,
            severity = IncidentSeverity.INFO
        )
        saveToStorage()
        return Pair(true, "Research unlocked: ${tech.name} is now active across all facilities!")
    }

    fun addIncident(title: String, description: String, severity: IncidentSeverity, financialImpactUsd: Long = 0L) {
        val incident = AiCloudIncident(
            title = title,
            description = description,
            severity = severity,
            financialImpactUsd = financialImpactUsd
        )
        _incidents.value = (listOf(incident) + _incidents.value).take(30)
    }

    fun updateAllState(
        updatedDcs: List<AiDataCenter>,
        updatedActiveContracts: List<ClientContract>,
        updatedMarketContracts: List<ClientContract>,
        newReport: AiCloudMonthlyReport,
        netProfit: Long
    ) {
        _dataCenters.value = updatedDcs
        _activeContracts.value = updatedActiveContracts
        _marketContracts.value = updatedMarketContracts
        _lastReport.value = newReport
        _companyBalance.value = (_companyBalance.value + netProfit).coerceAtLeast(0L)
        saveToStorage()
    }

    private fun saveToStorage() {
        if (prefs == null) return
        try {
            val editor = prefs.edit()
            editor.putString("company_name", _companyName.value)
            editor.putLong("company_balance", _companyBalance.value)
            editor.putString("data_centers_json", gson.toJson(_dataCenters.value))
            editor.putString("active_contracts_json", gson.toJson(_activeContracts.value))
            editor.putString("market_contracts_json", gson.toJson(_marketContracts.value))
            editor.putString("unlocked_research_json", gson.toJson(_unlockedResearchIds.value))
            editor.putString("last_report_json", gson.toJson(_lastReport.value))
            editor.putString("incidents_json", gson.toJson(_incidents.value))
            editor.putBoolean("is_initialized", _isInitialized.value)
            editor.apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadFromStorage() {
        if (prefs == null) return
        try {
            _companyName.value = prefs.getString("company_name", "Apex Neural Infrastructure Corp.") ?: "Apex Neural Infrastructure Corp."
            _companyBalance.value = prefs.getLong("company_balance", 5_000_000L)
            _isInitialized.value = prefs.getBoolean("is_initialized", false)

            val dcsJson = prefs.getString("data_centers_json", null)
            if (!dcsJson.isNullOrEmpty()) {
                val type = object : TypeToken<List<AiDataCenter>>() {}.type
                val list: List<AiDataCenter> = gson.fromJson(dcsJson, type)
                _dataCenters.value = list
            }

            val actJson = prefs.getString("active_contracts_json", null)
            if (!actJson.isNullOrEmpty()) {
                val type = object : TypeToken<List<ClientContract>>() {}.type
                val list: List<ClientContract> = gson.fromJson(actJson, type)
                _activeContracts.value = list
            }

            val mktJson = prefs.getString("market_contracts_json", null)
            if (!mktJson.isNullOrEmpty()) {
                val type = object : TypeToken<List<ClientContract>>() {}.type
                val list: List<ClientContract> = gson.fromJson(mktJson, type)
                _marketContracts.value = list
            }

            val rchJson = prefs.getString("unlocked_research_json", null)
            if (!rchJson.isNullOrEmpty()) {
                val type = object : TypeToken<Set<String>>() {}.type
                val set: Set<String> = gson.fromJson(rchJson, type)
                _unlockedResearchIds.value = set
            }

            val repJson = prefs.getString("last_report_json", null)
            if (!repJson.isNullOrEmpty()) {
                val report: AiCloudMonthlyReport = gson.fromJson(repJson, AiCloudMonthlyReport::class.java)
                _lastReport.value = report
            }

            val incJson = prefs.getString("incidents_json", null)
            if (!incJson.isNullOrEmpty()) {
                val type = object : TypeToken<List<AiCloudIncident>>() {}.type
                val list: List<AiCloudIncident> = gson.fromJson(incJson, type)
                _incidents.value = list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
