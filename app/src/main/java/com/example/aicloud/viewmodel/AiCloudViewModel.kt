package com.example.aicloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aicloud.data.AiCloudRepository
import com.example.aicloud.model.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AiCloudOverviewStats(
    val totalPflops: Double = 0.0,
    val totalTflops: Double = 0.0,
    val totalFacilities: Int = 0,
    val operationalFacilities: Int = 0,
    val totalPowerCapacityMw: Double = 0.0,
    val currentPowerDrawMw: Double = 0.0,
    val averagePue: Double = 1.25,
    val activeContractsCount: Int = 0,
    val activeContractRevenueMonthly: Long = 0L,
    val hasOverloadWarning: Boolean = false
)

class AiCloudViewModel(
    private val repository: AiCloudRepository = AiCloudRepository.getInstance()
) : ViewModel() {

    val companyName: StateFlow<String> = repository.companyName
    val companyBalance: StateFlow<Long> = repository.companyBalance
    val dataCenters: StateFlow<List<AiDataCenter>> = repository.dataCenters
    val activeContracts: StateFlow<List<ClientContract>> = repository.activeContracts
    val marketContracts: StateFlow<List<ClientContract>> = repository.marketContracts
    val unlockedResearchIds: StateFlow<Set<String>> = repository.unlockedResearchIds
    val lastReport: StateFlow<AiCloudMonthlyReport?> = repository.lastReport
    val incidents: StateFlow<List<AiCloudIncident>> = repository.incidents

    val overviewStats: StateFlow<AiCloudOverviewStats> = repository.dataCenters.map { dcs ->
        val operationalDcs = dcs.filter { it.isOperational }
        val totalTflops = operationalDcs.sumOf { it.totalComputeTflops }
        val totalPflops = totalTflops / 1000.0
        val totalCapMw = operationalDcs.sumOf { it.effectivePowerCapacityMw }
        val totalDrawMw = operationalDcs.sumOf { it.currentFacilityPowerMw }
        val avgPue = if (operationalDcs.isNotEmpty()) operationalDcs.map { it.effectivePue }.average() else 1.25
        val hasOverload = operationalDcs.any { it.isOverloaded }
        val contracts = repository.activeContracts.value

        AiCloudOverviewStats(
            totalPflops = totalPflops,
            totalTflops = totalTflops,
            totalFacilities = dcs.size,
            operationalFacilities = operationalDcs.size,
            totalPowerCapacityMw = totalCapMw,
            currentPowerDrawMw = totalDrawMw,
            averagePue = avgPue,
            activeContractsCount = contracts.size,
            activeContractRevenueMonthly = contracts.sumOf { it.monthlyPaymentUsd },
            hasOverloadWarning = hasOverload
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AiCloudOverviewStats()
    )

    fun initializeIfNeeded(name: String, initialBalance: Long? = null) {
        if (!repository.isInitialized.value) {
            repository.setupInitialBusiness(name, initialBalance)
        } else if (initialBalance != null && initialBalance > 0) {
            repository.syncExternalBalance(initialBalance)
        }
    }

    fun syncExternalBalance(canonicalBalance: Long) {
        repository.syncExternalBalance(canonicalBalance)
    }

    fun updateName(name: String) {
        repository.updateCompanyName(name)
    }

    fun buildDataCenter(
        name: String,
        region: CloudRegion,
        tier: DataCenterTier,
        cooling: CoolingSystemType,
        power: PowerSourceType,
        onResult: (Boolean, String) -> Unit
    ) {
        val result = repository.buildDataCenter(name, region, tier, cooling, power)
        onResult(result.first, result.second)
    }

    fun deployRacks(
        dataCenterId: String,
        gpuTier: GpuChipTier,
        quantity: Int,
        onResult: (Boolean, String) -> Unit
    ) {
        val result = repository.deployServerRack(dataCenterId, gpuTier, quantity)
        onResult(result.first, result.second)
    }

    fun upgradeCooling(
        dataCenterId: String,
        newCooling: CoolingSystemType,
        onResult: (Boolean, String) -> Unit
    ) {
        val result = repository.upgradeCoolingSystem(dataCenterId, newCooling)
        onResult(result.first, result.second)
    }

    fun upgradePower(
        dataCenterId: String,
        newPower: PowerSourceType,
        onResult: (Boolean, String) -> Unit
    ) {
        val result = repository.upgradePowerSource(dataCenterId, newPower)
        onResult(result.first, result.second)
    }

    fun repairRack(
        dataCenterId: String,
        rackId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val result = repository.repairHardware(dataCenterId, rackId)
        onResult(result.first, result.second)
    }

    fun serviceRack(
        dataCenterId: String,
        rackId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val result = repository.serviceHardware(dataCenterId, rackId)
        onResult(result.first, result.second)
    }

    fun tradeInRack(
        dataCenterId: String,
        rackId: String,
        newGpuTier: GpuChipTier,
        onResult: (Boolean, String) -> Unit
    ) {
        val result = repository.tradeInGpuRack(dataCenterId, rackId, newGpuTier)
        onResult(result.first, result.second)
    }

    fun decommissionRack(
        dataCenterId: String,
        rackId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val result = repository.decommissionRack(dataCenterId, rackId)
        onResult(result.first, result.second)
    }

    fun signContract(contractId: String, onResult: (Boolean, String) -> Unit) {
        val result = repository.signContract(contractId)
        onResult(result.first, result.second)
    }

    fun cancelContract(contractId: String, onResult: (Boolean, String) -> Unit) {
        val result = repository.cancelContract(contractId)
        onResult(result.first, result.second)
    }

    fun unlockResearch(techId: String, onResult: (Boolean, String) -> Unit) {
        val result = repository.unlockResearch(techId)
        onResult(result.first, result.second)
    }

    fun depositCapital(amount: Long): Boolean {
        return repository.depositCapital(amount)
    }

    fun withdrawCapital(amount: Long): Boolean {
        return repository.withdrawCapital(amount)
    }
}
