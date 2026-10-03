package com.example.publisher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.publisher.data.GamePublisherRepository
import com.example.publisher.engine.LaunchFinancialResult
import com.example.publisher.model.CreativeAdjustmentType
import com.example.publisher.model.FundingTier
import com.example.publisher.model.GameGenre
import com.example.publisher.model.GameProject
import com.example.publisher.model.IndieStudio
import com.example.publisher.model.TargetPlatform
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for the Publisher Incubator Hub.
 */
data class PublisherHubUiState(
    val selectedTabIndex: Int = 0, // 0: Incubator, 1: Active Dev, 2: Catalog
    val studioToSign: IndieStudio? = null,
    val projectForAdjustment: GameProject? = null,
    val projectForQaInjection: GameProject? = null,
    val projectForDetails: GameProject? = null,
    val showLaunchConfirmationFor: GameProject? = null,
    val showManualGameDialog: Boolean = false,
    val showTransferCashDialog: Boolean = false,
    val showLiquidationDialog: Boolean = false
)

/**
 * Dedicated ViewModel for the Indie Game Publisher & Incubator module.
 */
class GamePublisherViewModel(
    val repository: GamePublisherRepository = GamePublisherRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublisherHubUiState())
    val uiState: StateFlow<PublisherHubUiState> = _uiState.asStateFlow()

    private val _eventMessage = MutableSharedFlow<String>()
    val eventMessage: SharedFlow<String> = _eventMessage.asSharedFlow()

    // Pass-through flows from repository
    val pitches: StateFlow<List<IndieStudio>> = repository.pitches
    val activeProjects: StateFlow<List<GameProject>> = repository.activeProjects
    val releasedProjects: StateFlow<List<GameProject>> = repository.releasedProjects
    val publisherTreasury: StateFlow<Long> = repository.publisherTreasury
    val publisherReputation: StateFlow<Int> = repository.publisherReputation
    val lifetimePublisherProfit: StateFlow<Long> = repository.lifetimePublisherProfit
    val lastLaunchResult: StateFlow<LaunchFinancialResult?> = repository.lastLaunchResult
    val currentSimulationMonth: StateFlow<Int> = repository.currentSimulationMonth

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTabIndex = index)
    }

    fun scoutPitches() {
        repository.refreshPitches()
        emitMessage("Peluang pitch indie baru berhasil discout!")
    }

    fun openSignContractDialog(studio: IndieStudio) {
        _uiState.value = _uiState.value.copy(studioToSign = studio)
    }

    fun closeSignContractDialog() {
        _uiState.value = _uiState.value.copy(studioToSign = null)
    }

    fun confirmSignContract(
        studioId: String,
        tier: FundingTier,
        revShare: Double,
        platforms: Set<TargetPlatform>
    ) {
        val success = repository.acceptPitch(studioId, tier, revShare, platforms)
        if (success) {
            emitMessage("Kontrak publisher ditandatangani! Masuk tahap produksi.")
            closeSignContractDialog()
            selectTab(1)
        } else {
            emitMessage("Kas kas internal publisher tidak mencukupi untuk mendanai tier ini!")
        }
    }

    fun openManualGameDialog() {
        _uiState.value = _uiState.value.copy(showManualGameDialog = true)
    }

    fun closeManualGameDialog() {
        _uiState.value = _uiState.value.copy(showManualGameDialog = false)
    }

    fun createInHouseGame(
        title: String,
        genre: GameGenre,
        tier: FundingTier,
        platforms: Set<TargetPlatform>
    ) {
        val success = repository.createInHouseProject(
            title = title.ifBlank { "Project In-House Alpha" },
            genre = genre,
            fundingTier = tier,
            platforms = platforms
        )
        if (success) {
            emitMessage("Proyek game in-house '${title}' resmi dimulai!")
            closeManualGameDialog()
            selectTab(1)
        } else {
            emitMessage("Kas internal publisher tidak cukup untuk budget awal!")
        }
    }

    fun openTransferCashDialog() {
        _uiState.value = _uiState.value.copy(showTransferCashDialog = true)
    }

    fun closeTransferCashDialog() {
        _uiState.value = _uiState.value.copy(showTransferCashDialog = false)
    }

    fun openLiquidationDialog() {
        _uiState.value = _uiState.value.copy(showLiquidationDialog = true)
    }

    fun closeLiquidationDialog() {
        _uiState.value = _uiState.value.copy(showLiquidationDialog = false)
    }

    fun rejectPitch(studioId: String) {
        repository.rejectPitch(studioId)
        emitMessage("Proposal ditolak.")
    }

    fun togglePlatform(projectId: String, platform: TargetPlatform) {
        repository.togglePlatform(projectId, platform)
    }

    fun openAdjustmentDialog(project: GameProject) {
        _uiState.value = _uiState.value.copy(projectForAdjustment = project)
    }

    fun closeAdjustmentDialog() {
        _uiState.value = _uiState.value.copy(projectForAdjustment = null)
    }

    fun applyCreativeAdjustment(projectId: String, adjustment: CreativeAdjustmentType) {
        val success = repository.applyAdjustment(projectId, adjustment)
        if (success) {
            emitMessage("Penyesuaian kreatif '${adjustment.title}' diterapkan!")
            closeAdjustmentDialog()
        } else {
            emitMessage("Kas tidak mencukupi untuk ${adjustment.title} ($${adjustment.costBonus})")
        }
    }

    fun openQaDialog(project: GameProject) {
        _uiState.value = _uiState.value.copy(projectForQaInjection = project)
    }

    fun closeQaDialog() {
        _uiState.value = _uiState.value.copy(projectForQaInjection = null)
    }

    fun investQa(projectId: String, amount: Long) {
        val success = repository.investQaBudget(projectId, amount)
        if (success) {
            emitMessage("Suntikan dana QA $${amount} berhasil! Resiko bug porting berkurang drastis.")
            closeQaDialog()
        } else {
            emitMessage("Kas kas publisher tidak mencukupi.")
        }
    }

    fun openLaunchConfirmation(project: GameProject) {
        _uiState.value = _uiState.value.copy(showLaunchConfirmationFor = project)
    }

    fun closeLaunchConfirmation() {
        _uiState.value = _uiState.value.copy(showLaunchConfirmationFor = null)
    }

    fun launchGame(projectId: String) {
        closeLaunchConfirmation()
        val result = repository.launchGame(projectId)
        if (result != null) {
            emitMessage("Peluncuran Global Selesai! Skor Review: ${result.reviewScore}/100")
            selectTab(2)
        } else {
            emitMessage("Peluncuran gagal atau proyek tidak ditemukan.")
        }
    }

    fun dismissLaunchResultModal() {
        repository.dismissLaunchResult()
    }

    fun deployPatch(projectId: String) {
        val success = repository.deployPatch(projectId)
        if (success) {
            emitMessage("Update patch konten dirilis! Pemain aktif dan penjualan bangkit kembali.")
        } else {
            emitMessage("Perlu setidaknya $20,000 di kas untuk merilis patch.")
        }
    }

    fun openProjectDetails(project: GameProject) {
        _uiState.value = _uiState.value.copy(projectForDetails = project)
    }

    fun closeProjectDetails() {
        _uiState.value = _uiState.value.copy(projectForDetails = null)
    }

    fun advanceMonth() {
        repository.advanceMonth()
        emitMessage("Simulasi bisnis publisher maju 1 bulan!")
    }

    fun advanceWeek() = advanceMonth()

    fun calculateValuation(): Long = repository.calculateBusinessValuation()

    private fun emitMessage(msg: String) {
        viewModelScope.launch {
            _eventMessage.emit(msg)
        }
    }
}
