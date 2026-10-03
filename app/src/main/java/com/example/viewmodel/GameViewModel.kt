package com.example.viewmodel

import com.example.data.*
import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.content.SharedPreferences
import com.google.gson.Gson

import android.app.Application

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("tycoon_prefs", android.content.Context.MODE_PRIVATE)
    private val gson = Gson()

    val footballClubManager = com.example.data.FootballClubManager(application)
    val footballClubState: StateFlow<com.example.data.FootballClubState> = footballClubManager.clubState

    // Architecture Decoupling: Core Managers & Subsystem Engines
    val monthlyTickRegistry = com.example.core.engine.MonthlyTickRegistry()
    val coreBusinessEngine = com.example.core.engine.CoreBusinessEngine()
    val contentCreatorEngine = com.example.domain.subsystems.creative.ContentCreatorEngine()
    val streamingEngine = com.example.domain.subsystems.creative.StreamingEngine()
    val filmProductionEngine = com.example.domain.subsystems.creative.FilmProductionEngine()
    val bankingSubsystemEngine = com.example.domain.subsystems.banking.BankingSubsystemEngine()
    val themeParkSubsystemEngine = com.example.domain.subsystems.themepark.ThemeParkSubsystemEngine()
    val hospitalitySubsystemEngine = com.example.domain.subsystems.hospitality.HospitalitySubsystemEngine()
    val footballClubEngine = com.example.domain.subsystems.sports.FootballClubEngine(footballClubManager)
    val aviationEngine = com.example.domain.subsystems.aviation.AviationEngine()
    val logisticsEngine = com.example.domain.subsystems.logistics.LogisticsEngine()
    val constructionEngine = com.example.domain.subsystems.construction.ConstructionEngine()
    val gamePublisherEngine = com.example.publisher.engine.GamePublisherTickSubscriber()
    val aiCloudEngine = com.example.aicloud.engine.AiCloudTickSubscriber()
    val holdingInvestmentRepository = com.example.data.treasury.HoldingInvestmentRepository()
    val maRepository = com.example.ma.data.MaRepository.getInstance()
    val filmStudioRepository: com.example.filmstudio.repository.FilmStudioRepository = com.example.filmstudio.repository.FilmStudioRepositoryImpl()

    fun acceptMaOffer(offer: com.example.ma.model.AcquisitionOffer) {
        val (updated, _) = maRepository.acceptOffer(offer, _playerState.value)
        _playerState.value = updated
        saveState(_playerState.value)
    }

    fun rejectMaOffer(offer: com.example.ma.model.AcquisitionOffer) {
        maRepository.rejectOffer(offer)
    }

    fun stallMaOffer(offer: com.example.ma.model.AcquisitionOffer) {
        maRepository.stallOffer(offer)
    }

    fun submitMaCounterOffer(offer: com.example.ma.model.AcquisitionOffer, multiplier: Double, stake: Double): com.example.ma.model.NegotiationResult {
        return maRepository.submitCounterOffer(offer, multiplier, stake)
    }

    // Cloud Save & Authentication Repositories
    private val authRepository = com.example.data.AuthRepository()
    private val saveGameRepository = com.example.data.SaveGameRepository()

    // Cloud Save & Authentication UI status flows
    private val _cloudSyncProgress = MutableStateFlow(false)
    val cloudSyncProgress: StateFlow<Boolean> = _cloudSyncProgress.asStateFlow()

    private val _cloudSyncMessage = MutableStateFlow<String?>(null)
    val cloudSyncMessage: StateFlow<String?> = _cloudSyncMessage.asStateFlow()

    private val _lastSyncTimeMs = MutableStateFlow(prefs.getLong("last_cloud_sync_time", 0L))
    val lastSyncTimeMs: StateFlow<Long> = _lastSyncTimeMs.asStateFlow()

    fun initializeFootballClub(
        clubName: String, 
        leagueName: String, 
        chairmanName: String,
        acquisitionPriceUsd: Long = 0L,
        targetHoldingId: String? = null
    ): Pair<Boolean, String> {
        val current = _playerState.value
        if (targetHoldingId != null) {
            val holding = current.holdingCompanies.find { it.instanceId == targetHoldingId }
                ?: return Pair(false, "Holding Company tidak ditemukan.")
            if (acquisitionPriceUsd > 0 && holding.holdingCash < acquisitionPriceUsd) {
                return Pair(false, "Kas holding ${holding.name} tidak cukup (Dibutuhkan: USD ${String.format("%,d", acquisitionPriceUsd)}).")
            }
            val updatedHoldings = current.holdingCompanies.map {
                if (it.instanceId == targetHoldingId) it.copy(holdingCash = (it.holdingCash - acquisitionPriceUsd).coerceAtLeast(0.0)) else it
            }
            val footballBiz = com.example.data.OwnedBusiness(
                instanceId = "fc_${System.currentTimeMillis()}",
                catalogId = "football_club",
                customName = clubName,
                parentId = targetHoldingId
            )
            val updated = current.copy(
                holdingCompanies = updatedHoldings,
                ownedBusinesses = current.ownedBusinesses + footballBiz
            )
            _playerState.value = updated
            saveState(updated)
        } else {
            if (acquisitionPriceUsd > 0 && current.cash < acquisitionPriceUsd) {
                return Pair(false, "Saldo Kas Utama tidak cukup (Dibutuhkan: USD ${String.format("%,d", acquisitionPriceUsd)}).")
            }
            val footballBiz = com.example.data.OwnedBusiness(
                instanceId = "fc_${System.currentTimeMillis()}",
                catalogId = "football_club",
                customName = clubName
            )
            val updated = current.copy(
                cash = (current.cash - acquisitionPriceUsd).coerceAtLeast(0L),
                ownedBusinesses = current.ownedBusinesses + footballBiz
            )
            _playerState.value = updated
            saveState(updated)
        }

        footballClubManager.initializeClub(clubName, leagueName, chairmanName, acquisitionPriceUsd)
        return Pair(true, "Selamat! Akuisisi klub $clubName berhasil disahkan.")
    }

    fun injectCapitalFromHoldingToFootballClub(holdingId: String, amount: Long): Pair<Boolean, String> {
        val current = _playerState.value
        val holding = current.holdingCompanies.find { it.instanceId == holdingId }
            ?: return Pair(false, "Holding company tidak ditemukan.")
        if (holding.holdingCash < amount) {
            return Pair(false, "Kas holding ${holding.name} tidak mencukupi (Tersedia: USD ${String.format("%,d", holding.holdingCash.toLong())}).")
        }
        val updatedHoldings = current.holdingCompanies.map {
            if (it.instanceId == holdingId) it.copy(holdingCash = it.holdingCash - amount) else it
        }
        val updated = current.copy(holdingCompanies = updatedHoldings)
        _playerState.value = updated
        saveState(updated)

        footballClubManager.injectBudget(amount, "Holding: ${holding.name}")
        footballClubManager.modifyManagerMorale(+12)
        footballClubManager.modifyTeamMorale(+5)
        return Pair(true, "Berhasil menyuntikkan dana USD ${String.format("%,d", amount)} dari kas ${holding.name} ke anggaran transfer klub.")
    }

    fun approveAutonomousManagerBudget(proposal: com.example.data.ManagerBudgetProposal, source: String, holdingId: String? = null): Pair<Boolean, String> {
        if (source == "HOLDING" && !holdingId.isNullOrEmpty()) {
            return injectCapitalFromHoldingToFootballClub(holdingId, proposal.requestedAmount)
        } else {
            val currentCash = _playerState.value.cash
            if (currentCash < proposal.requestedAmount) {
                return Pair(false, "Saldo Kas Utama / Mega Holding tidak mencukupi untuk memenuhi permohonan manajer.")
            }
            val updated = _playerState.value.copy(cash = currentCash - proposal.requestedAmount)
            _playerState.value = updated
            saveState(updated)

            footballClubManager.injectBudget(proposal.requestedAmount, "Kas Utama / Mega Holding")
            footballClubManager.modifyManagerMorale(+15)
            footballClubManager.modifyTeamMorale(+5)
            return Pair(true, "Permohonan Manajer disetujui! Dana USD ${String.format("%,d", proposal.requestedAmount)} dialokasikan. Morale manajer melonjak (+15%)!")
        }
    }

    fun rejectAutonomousManagerBudget(): String {
        footballClubManager.modifyManagerMorale(-6)
        return "Permohonan anggaran manajer ditolak. Manajer kecewa atas pembatasan anggaran (-6% Morale)."
    }

    fun hireFootballManager(manager: com.example.data.FootballManager): Pair<Boolean, String> {
        return footballClubManager.hireManager(manager)
    }

    fun evaluateManagerNegotiation(
        manager: com.example.data.FootballManager,
        offeredSalary: Long,
        offeredBonus: Long
    ): com.example.data.ManagerNegotiationOutcome {
        return footballClubManager.evaluateManagerNegotiation(manager, offeredSalary, offeredBonus)
    }

    fun hireFootballManagerWithNegotiation(
        manager: com.example.data.FootballManager,
        agreedSalary: Long,
        agreedBonus: Long
    ): Pair<Boolean, String> {
        return footballClubManager.hireManagerWithNegotiation(manager, agreedSalary, agreedBonus)
    }

    fun fireFootballManager(): Pair<Boolean, String> {
        return footballClubManager.fireManager()
    }

    fun approveFootballTransfer(inboxId: String): Pair<Boolean, String> {
        return footballClubManager.approveManagerTransfer(inboxId)
    }

    fun rejectFootballTransfer(inboxId: String): Pair<Boolean, String> {
        return footballClubManager.rejectManagerTransfer(inboxId)
    }

    fun evaluatePlayerNegotiation(
        player: com.example.data.FootballPlayer,
        offeredFee: Long,
        offeredWage: Long,
        signingBonus: Long
    ): com.example.data.PlayerNegotiationOutcome {
        return footballClubManager.evaluatePlayerNegotiation(player, offeredFee, offeredWage, signingBonus)
    }

    fun forceBuyFootballPlayer(
        player: com.example.data.FootballPlayer,
        agreedFee: Long = player.marketValue,
        agreedWage: Long = player.monthlyWage,
        signingBonus: Long = 0L
    ): Pair<Boolean, String> {
        return footballClubManager.forceBuyPlayer(player, agreedFee, agreedWage, signingBonus)
    }

    fun sellFootballPlayer(playerId: String): Pair<Boolean, String> {
        return footballClubManager.sellPlayer(playerId)
    }

    fun signFootballSponsor(sponsor: com.example.data.FootballSponsor): Pair<Boolean, String> {
        return footballClubManager.signSponsor(sponsor)
    }

    fun injectCapitalToFootballClub(amount: Long): Pair<Boolean, String> {
        val currentCash = _playerState.value.cash
        if (amount <= 0 || currentCash < amount) {
            return Pair(false, "Saldo tunai pribadi tidak mencukupi (€${String.format("%,d", currentCash)}).")
        }
        val (ok, msg) = footballClubManager.injectCapitalFromPersonal(amount, currentCash)
        if (ok) {
            val updated = _playerState.value.copy(cash = currentCash - amount)
            _playerState.value = updated
            saveState(updated)
        }
        return Pair(ok, msg)
    }

    fun withdrawFootballDividends(amount: Long): Pair<Boolean, String> {
        val (ok, msg) = footballClubManager.withdrawDividendsToPersonal(amount)
        if (ok) {
            val updated = _playerState.value.copy(cash = _playerState.value.cash + amount)
            _playerState.value = updated
            saveState(updated)
        }
        return Pair(ok, msg)
    }

    fun simulateNextFootballMatch(): com.example.data.FootballMatch? {
        return footballClubManager.simulateNextMatch()
    }

    fun getCurrentUserEmail(): String? {
        return authRepository.getCurrentUserEmail()
    }

    fun isUserLoggedIn(): Boolean {
        return authRepository.getCurrentUserId() != null
    }

    fun signUpWithEmail(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _cloudSyncProgress.value = true
            _cloudSyncMessage.value = "Mendaftarkan akun..."
            val result = authRepository.signUp(email, password)
            _cloudSyncProgress.value = false
            if (result.isSuccess) {
                _cloudSyncMessage.value = "Pendaftaran berhasil!"
                onSuccess()
            } else {
                val errMsg = result.exceptionOrNull()?.message ?: "Terjadi kesalahan."
                _cloudSyncMessage.value = "Pendaftaran gagal: $errMsg"
                onError(errMsg)
            }
        }
    }

    fun signInWithEmail(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _cloudSyncProgress.value = true
            _cloudSyncMessage.value = "Menghubungkan ke server..."
            val result = authRepository.signIn(email, password)
            _cloudSyncProgress.value = false
            if (result.isSuccess) {
                _cloudSyncMessage.value = "Koneksi berhasil terjalin!"
                onSuccess()
                restoreGameFromCloud()
            } else {
                val errMsg = result.exceptionOrNull()?.message ?: "Terjadi kesalahan."
                _cloudSyncMessage.value = "Login gagal: $errMsg"
                onError(errMsg)
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _cloudSyncMessage.value = "Keluar dari akun."
    }

    fun backupGameToCloud() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _cloudSyncProgress.value = true
            _cloudSyncMessage.value = "Menyinkronkan data ke Cloud..."
            
            val currentState = _playerState.value
            val totalFortuneVal = currentState.netAssetValue(
                stockList = _stockList.value,
                cryptoList = _cryptoList.value,
                realEstateMarket = _realEstateMarket.value,
                collectionList = _collectionList.value,
                preciousMetalsList = _preciousMetalsList.value
            )
            
            val gameState = com.example.data.PlayerGameState(
                userId = userId,
                lastSavedMs = System.currentTimeMillis(),
                privateBalance = currentState.privateBalance,
                totalFortune = totalFortuneVal,
                inGameMonth = currentState.inGameMonth,
                inGameYear = currentState.inGameYear,
                ownedBusinessesCount = currentState.ownedBusinesses.size,
                fullStateJson = exportSaveGame()
            )

            val result = saveGameRepository.saveGameToCloud(userId, gameState)
            _cloudSyncProgress.value = false
            if (result.isSuccess) {
                _cloudSyncMessage.value = "Sinkronisasi berhasil!"
                _lastSyncTimeMs.value = System.currentTimeMillis()
                prefs.edit().putLong("last_cloud_sync_time", _lastSyncTimeMs.value).apply()
            } else {
                val errMsg = result.exceptionOrNull()?.message ?: "Koneksi terputus."
                _cloudSyncMessage.value = "Sinkronisasi gagal: $errMsg"
            }
        }
    }

    fun restoreGameFromCloud() {
        val userId = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _cloudSyncProgress.value = true
            _cloudSyncMessage.value = "Memuat data dari Cloud..."
            val result = saveGameRepository.loadGameFromCloud(userId)
            _cloudSyncProgress.value = false
            if (result.isSuccess) {
                val gameState = result.getOrThrow()
                if (gameState.fullStateJson.isNotEmpty()) {
                    val importSuccess = importSaveGame(gameState.fullStateJson)
                    if (importSuccess) {
                        _cloudSyncMessage.value = "Restorasi berhasil!"
                        _lastSyncTimeMs.value = gameState.lastSavedMs
                        prefs.edit().putLong("last_cloud_sync_time", _lastSyncTimeMs.value).apply()
                    } else {
                        _cloudSyncMessage.value = "Gagal memproses data game."
                    }
                } else {
                    _cloudSyncMessage.value = "Data simpanan kosong."
                }
            } else {
                val errMsg = result.exceptionOrNull()?.message ?: "Koneksi terputus."
                _cloudSyncMessage.value = "Gagal memuat: $errMsg"
            }
        }
    }

    private suspend fun startCloudAutoSaveLoop() {
        while (true) {
            delay(300_000L) // 5 minutes
            if (isUserLoggedIn()) {
                backupGameToCloud()
            }
        }
    }
    
    // helper: return <NewCompanyCash, AmountForGlobal>
    fun processDecentralizedCashFlow(netProfit: Long, currentCompanyCash: Double): Pair<Double, Long> {
        if (netProfit > 0) {
            val internalRetained = (netProfit * 0.6).toLong()
            val dividendToGlobal = netProfit - internalRetained
            return Pair(currentCompanyCash + internalRetained.toDouble(), dividendToGlobal)
        } else {
            val loss = -netProfit.toDouble()
            // Subtract loss entirely from company cash without taking anything from global/parent cash
            return Pair(currentCompanyCash - loss, 0L)
        }
    }

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private fun patchOwnedBusiness(business: com.example.data.OwnedBusiness): com.example.data.OwnedBusiness {
        val patchedBranches = (business.themeParkBranches ?: emptyList()).map { branch ->
            val ridesSafe = (branch.rides ?: emptyList()).map { ride ->
                ride.copy(
                    name = ride.name ?: "Wahana",
                    tierDescription = ride.tierDescription ?: "Wahana Kustom"
                )
            }.toMutableList()
            
            val facilitiesSafe = (branch.facilities ?: emptyList()).map { facility ->
                facility.copy(
                    id = facility.id ?: java.util.UUID.randomUUID().toString(),
                    zoneName = if (facility.zoneName.isNullOrBlank()) "Belum Terzonasi" else facility.zoneName,
                    imageUrl = facility.imageUrl
                )
            }
            
            branch.copy(
                facilities = facilitiesSafe,
                rides = ridesSafe,
                parkZones = branch.parkZones ?: mutableListOf(),
                priceRegular = if (branch.priceRegular == 0L) 15L else branch.priceRegular,
                priceTerusan = if (branch.priceTerusan == 0L) 35L else branch.priceTerusan,
                priceVIP = if (branch.priceVIP == 0L) 100L else branch.priceVIP,
                priceFamily = if (branch.priceFamily == 0L) 80L else branch.priceFamily,
                adBoostMultiplier = if (branch.adBoostMultiplier <= 0.0) 1.0 else branch.adBoostMultiplier
            )
        }
        val patchedSubs = (business.subsidiaries ?: emptyList()).map { patchOwnedBusiness(it) }
        return business.copy(
            studioType = business.studioType ?: "LIVE_ACTION",
            subsidiaries = patchedSubs,
            companyCash = business.companyCash ?: 0.0,
            themeParkBranches = patchedBranches
        )
    }

    private fun patchFoundations(list: List<com.example.data.FoundationEntity>?): List<com.example.data.FoundationEntity> {
        return try {
            (list ?: emptyList()).map { f ->
                val safeInstitutions = try {
                    (f.educationInstitutions ?: emptyList()).map { inst ->
                        val migratedTeachers = if (inst.teachers != null) {
                            var u = inst.teachers.umum ?: com.example.data.StaffRole()
                            var s = inst.teachers.spesialis ?: com.example.data.StaffRole()
                            var sn = inst.teachers.senior ?: com.example.data.StaffRole()
                            
                            if (u.customSalary == 0L) u = u.copy(customSalary = 3000L)
                            if (s.customSalary == 0L) s = s.copy(customSalary = 5000L)
                            if (sn.customSalary == 0L) sn = sn.copy(customSalary = 8000L)
                            
                            if (u.target == 0 && (u.active > 0 || u.recruiting > 0)) {
                                u = u.copy(target = u.active + u.recruiting)
                            }
                            if (s.target == 0 && (s.active > 0 || s.recruiting > 0)) {
                                s = s.copy(target = s.active + s.recruiting)
                            }
                            if (sn.target == 0 && (sn.active > 0 || sn.recruiting > 0)) {
                                sn = sn.copy(target = sn.active + sn.recruiting)
                            }
                            
                            @Suppress("DEPRECATION")
                            if (u.active == 0 && u.recruiting == 0 && (inst.teachers.umumCount ?: 0) > 0) {
                                u = u.copy(active = inst.teachers.umumCount ?: 0, target = inst.teachers.umumCount ?: 0)
                            }
                            @Suppress("DEPRECATION")
                            if (s.active == 0 && s.recruiting == 0 && (inst.teachers.spesialisCount ?: 0) > 0) {
                                s = s.copy(active = inst.teachers.spesialisCount ?: 0, target = inst.teachers.spesialisCount ?: 0)
                            }
                            @Suppress("DEPRECATION")
                            if (sn.active == 0 && sn.recruiting == 0 && (inst.teachers.seniorCount ?: 0) > 0) {
                                sn = sn.copy(active = inst.teachers.seniorCount ?: 0, target = inst.teachers.seniorCount ?: 0)
                            }
                            com.example.data.TeacherStaff(umum = u, spesialis = s, senior = sn)
                        } else {
                            com.example.data.TeacherStaff()
                        }

                        val migratedSupport = if (inst.supportStaff != null) {
                            var o = inst.supportStaff.ob ?: com.example.data.StaffRole()
                            var sat = inst.supportStaff.satpam ?: com.example.data.StaffRole()
                            var adm = inst.supportStaff.admin ?: com.example.data.StaffRole()
                            var ch = inst.supportStaff.chef ?: com.example.data.StaffRole()
                            
                            if (o.customSalary == 0L) o = o.copy(customSalary = 800L)
                            if (sat.customSalary == 0L) sat = sat.copy(customSalary = 1000L)
                            if (adm.customSalary == 0L) adm = adm.copy(customSalary = 1200L)
                            if (ch.customSalary == 0L) ch = ch.copy(customSalary = 2500L)
                            
                            if (o.target == 0 && (o.active > 0 || o.recruiting > 0)) {
                                o = o.copy(target = o.active + o.recruiting)
                            }
                            if (sat.target == 0 && (sat.active > 0 || sat.recruiting > 0)) {
                                sat = sat.copy(target = sat.active + sat.recruiting)
                            }
                            if (adm.target == 0 && (adm.active > 0 || adm.recruiting > 0)) {
                                adm = adm.copy(target = adm.active + adm.recruiting)
                            }
                            if (ch.target == 0 && (ch.active > 0 || ch.recruiting > 0)) {
                                ch = ch.copy(target = ch.active + ch.recruiting)
                            }
                            
                            @Suppress("DEPRECATION")
                            if (o.active == 0 && o.recruiting == 0 && (inst.supportStaff.janitorCount ?: 0) > 0) {
                                o = o.copy(active = inst.supportStaff.janitorCount ?: 0, target = inst.supportStaff.janitorCount ?: 0)
                            }
                            @Suppress("DEPRECATION")
                            if (sat.active == 0 && sat.recruiting == 0 && (inst.supportStaff.securityCount ?: 0) > 0) {
                                sat = sat.copy(active = inst.supportStaff.securityCount ?: 0, target = inst.supportStaff.securityCount ?: 0)
                            }
                            @Suppress("DEPRECATION")
                            if (adm.active == 0 && adm.recruiting == 0 && (inst.supportStaff.adminCount ?: 0) > 0) {
                                adm = adm.copy(active = inst.supportStaff.adminCount ?: 0, target = inst.supportStaff.adminCount ?: 0)
                            }
                            @Suppress("DEPRECATION")
                            if (ch.active == 0 && ch.recruiting == 0 && (inst.supportStaff.chefCount ?: 0) > 0) {
                                ch = ch.copy(active = inst.supportStaff.chefCount ?: 0, target = inst.supportStaff.chefCount ?: 0)
                            }
                            com.example.data.SupportStaff(ob = o, satpam = sat, admin = adm, chef = ch)
                        } else {
                            com.example.data.SupportStaff()
                        }

                        com.example.data.EducationInstitution(
                            id = inst.id ?: java.util.UUID.randomUUID().toString(),
                            name = inst.name ?: "Institusi Lama",
                            level = inst.level ?: "SD",
                            curriculumType = inst.curriculumType ?: "Merdeka",
                            facilityLevel = if (inst.facilityLevel <= 0) 1 else inst.facilityLevel,
                            accreditationPoints = if (inst.accreditationPoints < 0) 0 else inst.accreditationPoints,
                            monthlyOperationalCost = if (inst.monthlyOperationalCost < 0L) 100000L else inst.monthlyOperationalCost,
                            prestigeScore = if (inst.prestigeScore < 0) 0 else inst.prestigeScore,
                            imageUrl = inst.imageUrl ?: "",
                            monthlySpp = inst.monthlySpp,
                            currentStudents = inst.currentStudents,
                            buildingGrade = inst.buildingGrade ?: "Grade A",
                            baseMaintenanceCost = inst.baseMaintenanceCost,
                            additionalFacilities = inst.additionalFacilities ?: emptyList(),
                            constructionMonthsTotal = inst.constructionMonthsTotal,
                            constructionMonthsLeft = inst.constructionMonthsLeft,
                            isOperational = inst.isOperational || (inst.constructionMonthsLeft == 0 && inst.currentStudents > 0),
                            teachers = migratedTeachers,
                            supportStaff = migratedSupport
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AppDebug", "Error patching educationInstitutions, fallback to empty: ${e.message}")
                    emptyList()
                }

                val safeHealthInstitutions = try {
                    (f.healthInstitutions ?: emptyList()).map { inst ->
                        val migratedMedical = if (inst.medicalStaff != null) {
                            var p = inst.medicalStaff.perawat ?: com.example.data.StaffRole()
                            var du = inst.medicalStaff.dokterUmum ?: com.example.data.StaffRole()
                            var ds = inst.medicalStaff.dokterSpesialis ?: com.example.data.StaffRole()
                            
                            if (p.customSalary == 0L) p = p.copy(customSalary = 4000L)
                            if (du.customSalary == 0L) du = du.copy(customSalary = 8000L)
                            if (ds.customSalary == 0L) ds = ds.copy(customSalary = 15000L)
                            
                            if (p.target == 0 && (p.active > 0 || p.recruiting > 0)) {
                                p = p.copy(target = p.active + p.recruiting)
                            }
                            if (du.target == 0 && (du.active > 0 || du.recruiting > 0)) {
                                du = du.copy(target = du.active + du.recruiting)
                            }
                            if (ds.target == 0 && (ds.active > 0 || ds.recruiting > 0)) {
                                ds = ds.copy(target = ds.active + ds.recruiting)
                            }
                            com.example.data.MedicalStaff(perawat = p, dokterUmum = du, dokterSpesialis = ds)
                        } else {
                            com.example.data.MedicalStaff()
                        }

                        val migratedSupport = if (inst.supportStaff != null) {
                            var o = inst.supportStaff.ob ?: com.example.data.StaffRole()
                            var sat = inst.supportStaff.satpam ?: com.example.data.StaffRole()
                            var adm = inst.supportStaff.admin ?: com.example.data.StaffRole()
                            var ch = inst.supportStaff.chef ?: com.example.data.StaffRole()
                            
                            if (o.customSalary == 0L) o = o.copy(customSalary = 800L)
                            if (sat.customSalary == 0L) sat = sat.copy(customSalary = 1000L)
                            if (adm.customSalary == 0L) adm = adm.copy(customSalary = 1200L)
                            if (ch.customSalary == 0L) ch = ch.copy(customSalary = 2500L)
                            
                            if (o.target == 0 && (o.active > 0 || o.recruiting > 0)) {
                                o = o.copy(target = o.active + o.recruiting)
                            }
                            if (sat.target == 0 && (sat.active > 0 || sat.recruiting > 0)) {
                                sat = sat.copy(target = sat.active + sat.recruiting)
                            }
                            if (adm.target == 0 && (adm.active > 0 || adm.recruiting > 0)) {
                                adm = adm.copy(target = adm.active + adm.recruiting)
                            }
                            if (ch.target == 0 && (ch.active > 0 || ch.recruiting > 0)) {
                                ch = ch.copy(target = ch.active + ch.recruiting)
                            }
                            com.example.data.SupportStaff(ob = o, satpam = sat, admin = adm, chef = ch)
                        } else {
                            com.example.data.SupportStaff()
                        }

                        com.example.data.HealthInstitution(
                            id = inst.id ?: java.util.UUID.randomUUID().toString(),
                            name = inst.name ?: "Klinik Lama",
                            level = inst.level ?: "Klinik",
                            serviceType = inst.serviceType ?: "Reguler",
                            facilityLevel = if (inst.facilityLevel <= 0) 1 else inst.facilityLevel,
                            accreditationPoints = if (inst.accreditationPoints < 0) 0 else inst.accreditationPoints,
                            monthlyOperationalCost = if (inst.monthlyOperationalCost < 0L) 15000L else inst.monthlyOperationalCost,
                            prestigeScore = if (inst.prestigeScore < 0) 0 else inst.prestigeScore,
                            imageUrl = inst.imageUrl ?: "",
                            monthlyBillPerPatient = inst.monthlyBillPerPatient,
                            currentPatients = inst.currentPatients,
                            buildingGrade = inst.buildingGrade ?: "Grade A",
                            baseMaintenanceCost = inst.baseMaintenanceCost,
                            additionalFacilities = inst.additionalFacilities ?: emptyList(),
                            constructionMonthsTotal = inst.constructionMonthsTotal,
                            constructionMonthsLeft = inst.constructionMonthsLeft,
                            isOperational = inst.isOperational || (inst.constructionMonthsLeft == 0 && inst.currentPatients > 0),
                            medicalStaff = migratedMedical,
                            supportStaff = migratedSupport
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AppDebug", "Error patching healthInstitutions, fallback to empty: ${e.message}")
                    emptyList()
                }

                val safeCharityInstitutions = try {
                    (f.charityInstitutions ?: emptyList()).map { inst ->
                        val migratedStaff = if (inst.charityStaff != null) {
                            var r = inst.charityStaff.relawan ?: com.example.data.StaffRole()
                            var ss = inst.charityStaff.staffSosial ?: com.example.data.StaffRole()
                            var ap = inst.charityStaff.ahliProgram ?: com.example.data.StaffRole()
                            
                            if (r.customSalary == 0L) r = r.copy(customSalary = 500L)
                            if (ss.customSalary == 0L) ss = ss.copy(customSalary = 3000L)
                            if (ap.customSalary == 0L) ap = ap.copy(customSalary = 7000L)
                            
                            if (r.target == 0 && (r.active > 0 || r.recruiting > 0)) {
                                r = r.copy(target = r.active + r.recruiting)
                            }
                            if (ss.target == 0 && (ss.active > 0 || ss.recruiting > 0)) {
                                ss = ss.copy(target = ss.active + ss.recruiting)
                            }
                            if (ap.target == 0 && (ap.active > 0 || ap.recruiting > 0)) {
                                ap = ap.copy(target = ap.active + ap.recruiting)
                            }
                            com.example.data.CharityStaff(relawan = r, staffSosial = ss, ahliProgram = ap)
                        } else {
                            com.example.data.CharityStaff()
                        }

                        com.example.data.CharityInstitution(
                            id = inst.id ?: java.util.UUID.randomUUID().toString(),
                            name = inst.name ?: "Badan Amal",
                            level = inst.level ?: "Humanitarian Aid",
                            scope = inst.scope ?: "Lokal",
                            facilityLevel = if (inst.facilityLevel <= 0) 1 else inst.facilityLevel,
                            accreditationPoints = if (inst.accreditationPoints < 0) 0 else inst.accreditationPoints,
                            prestigeScore = if (inst.prestigeScore < 0) 0 else inst.prestigeScore,
                            imageUrl = inst.imageUrl ?: "",
                            baseMaintenanceCost = inst.baseMaintenanceCost,
                            constructionTotalMonths = inst.constructionTotalMonths,
                            constructionLeftMonths = inst.constructionLeftMonths,
                            isOperational = inst.isOperational || (inst.constructionLeftMonths == 0 && inst.monthlyBeneficiaries > 0),
                            monthlyBeneficiaries = inst.monthlyBeneficiaries,
                            maxCapacity = if (inst.maxCapacity <= 0) com.example.data.calculateCharityMaxCapacity(inst.level ?: "Humanitarian Aid", inst.scope ?: "Lokal") else inst.maxCapacity,
                            additionalFacilities = inst.additionalFacilities ?: emptyList(),
                            charityStaff = migratedStaff,
                            buildingGrade = inst.buildingGrade ?: "Grade A"
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AppDebug", "Error patching charityInstitutions, fallback to empty: ${e.message}")
                    emptyList()
                }

                com.example.data.FoundationEntity(
                    id = f.id ?: java.util.UUID.randomUUID().toString(),
                    name = f.name ?: "Yayasan Tanpa Nama",
                    type = f.type ?: com.example.data.FoundationType.EDUCATION,
                    isLegalized = f.isLegalized,
                    constructionMonthsLeft = f.constructionMonthsLeft,
                    endowmentFund = f.endowmentFund,
                    facilities = f.facilities ?: emptyList(),
                    educationInstitutions = safeInstitutions,
                    healthInstitutions = safeHealthInstitutions,
                    charityInstitutions = safeCharityInstitutions
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("AppDebug", "Error patching foundations, fallback to empty: ${e.message}")
            emptyList()
        }
    }

    private val _monthProgress = kotlinx.coroutines.flow.MutableStateFlow(0f)
    private fun loadState(): PlayerState {
        val json = prefs.getString("player_state", null)
        if (json != null) {
            try {
                val state = gson.fromJson(json, PlayerState::class.java)
                
                // Auto-Migration saat Startup
                val patchedBusinesses = state.ownedBusinesses?.map { patchOwnedBusiness(it) } ?: emptyList()

                val patchedHoldings = state.holdingCompanies?.map { holding ->
                    val patchedSubs = holding.subsidiaries?.map { patchOwnedBusiness(it) } ?: emptyList()
                    holding.copy(subsidiaries = patchedSubs)
                } ?: emptyList()

                val migratedState = state.copy(
                    rebrandedCompanies = state.rebrandedCompanies ?: emptyMap(),
                    megaHolding = state.megaHolding ?: com.example.data.MegaHoldingState(),
                    ownedBusinesses = patchedBusinesses,
                    holdingCompanies = patchedHoldings,
                    activeInvestorsLoans = state.activeInvestorsLoans ?: emptyList(),
                    privateLedgerHistory = state.privateLedgerHistory ?: emptyList(),
                    financialHistory = state.financialHistory ?: emptyList(),
                    activeSubscriptions = state.activeSubscriptions ?: emptyList(),
                    allSubscriptions = if (state.allSubscriptions.isNullOrEmpty()) {
                        com.example.data.defaultLifestyleItems.map { defaultItem ->
                            val isActive = state.activeSubscriptions?.contains(defaultItem.name) == true
                            val isOwned = state.ownedGadgets?.contains(defaultItem.name) == true
                            defaultItem.copy(isActive = isActive, isOwned = isOwned)
                        }
                    } else {
                        val currentNames = state.allSubscriptions.map { it.name }.toSet()
                        val missingDefaults = com.example.data.defaultLifestyleItems.filterNot { currentNames.contains(it.name) }
                        state.allSubscriptions + missingDefaults
                    },
                    travelDestinations = if (state.travelDestinations.isNullOrEmpty()) {
                        com.example.data.defaultTravelDestinations
                    } else {
                        state.travelDestinations
                    },
                    totalTripsTaken = state.totalTripsTaken,
                    foundations = patchFoundations(state.foundations)
                )
                
                // Simpan pembaruan jika ini adalah migrasi sukses
                prefs.edit().putString("player_state", gson.toJson(migratedState)).apply()
                return migratedState
            } catch(e: Exception) { 
                e.printStackTrace()
                android.util.Log.e("AppDebug", "Init error parsing JSON, protecting save: ${e.message}")
                // Kritis: JANGAN hapus data save (prefs.edit().remove().apply()), lindungi data lama pemain!
                // Return default state but flag it so it won't be saved over
                return PlayerState().copy(cash = -1L) // Using cash = -1L as a flag for failed load
            }
        }
        return PlayerState()
    }

    fun exportSaveGame(): String {
        val savePayload = _playerState.value.copy(
            customMarketAssets = _realEstateMarket.value,
            customCollectionAssets = _collectionList.value,
            customHousingAssets = _housingList.value
        )
        return gson.toJson(savePayload)
    }

    fun importSaveGame(jsonString: String): Boolean {
        try {
            val importedState = gson.fromJson(jsonString, com.example.data.PlayerState::class.java)
            if (importedState != null && importedState.cash >= -1L) {
                val patchedBusinesses = importedState.ownedBusinesses?.map { patchOwnedBusiness(it) } ?: emptyList()
                val patchedHoldings = importedState.holdingCompanies?.map { holding ->
                    val patchedSubs = holding.subsidiaries?.map { patchOwnedBusiness(it) } ?: emptyList()
                    holding.copy(subsidiaries = patchedSubs)
                } ?: emptyList()
                val finalState = importedState.copy(
                    ownedBusinesses = patchedBusinesses,
                    holdingCompanies = patchedHoldings,
                    activeInvestorsLoans = importedState.activeInvestorsLoans ?: emptyList(),
                    privateLedgerHistory = importedState.privateLedgerHistory ?: emptyList(),
                    financialHistory = importedState.financialHistory ?: emptyList(),
                    activeSubscriptions = importedState.activeSubscriptions ?: emptyList(),
                    foundations = patchFoundations(importedState.foundations)
                )
                _playerState.value = finalState
                
                if (importedState.customMarketAssets != null) {
                    _realEstateMarket.value = importedState.customMarketAssets
                    saveProperties(importedState.customMarketAssets)
                }
                
                if (importedState.customCollectionAssets != null) {
                    val base = com.example.data.initialCollectionItems + com.example.data.initialVehicleItems
                    val baseIds = base.map { it.id }.toSet()
                    val merged = base.toMutableList()
                    for (item in importedState.customCollectionAssets) {
                        val itemId = (item.id as String?) ?: ""
                        if (itemId.isNotEmpty() && itemId !in baseIds) {
                            val sanitizedItem = com.example.data.CollectionItem(
                                id = itemId,
                                categoryId = (item.categoryId as String?) ?: "",
                                name = (item.name as String?) ?: "",
                                description = (item.description as String?) ?: "",
                                basePrice = item.basePrice,
                                imageUrl = (item.imageUrl as String?) ?: "",
                                releaseYear = item.releaseYear,
                                type = (item.type as String?) ?: ""
                            )
                            merged.add(sanitizedItem)
                        }
                    }
                    _collectionList.value = merged
                    saveCollections(merged)
                }
                
                if (importedState.customHousingAssets != null) {
                    val base = com.example.data.initialHousingItems
                    val baseIds = base.map { it.id }.toSet()
                    val merged = base.toMutableList()
                    for (item in importedState.customHousingAssets) {
                        val itemId = (item.id as String?) ?: ""
                        if (itemId.isNotEmpty() && !itemId.startsWith("hs_") && itemId !in baseIds) {
                            val sanitizedItem = com.example.data.HousingItem(
                                id = itemId,
                                name = (item.name as String?) ?: "",
                                location = (item.location as String?) ?: "",
                                type = (item.type as String?) ?: "",
                                buyPrice = item.buyPrice,
                                rentPrice = item.rentPrice,
                                imageUrl = (item.imageUrl as String?) ?: ""
                            )
                            merged.add(sanitizedItem)
                        }
                    }
                    _housingList.value = merged
                    saveHousing(merged)
                }
                
                autoResolveMissingHousing(importedState)
                
                prefs.edit().putString("player_state", gson.toJson(importedState.copy(lastSavedTimeMs = System.currentTimeMillis()))).apply()
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("AppDebug", "Import Save failed: ${e.message}")
        }
        return false
    }

    private fun saveState(state: PlayerState) {
        if (state.cash == -1L) {
            android.util.Log.e("AppDebug", "Save blocked due to corrupted state.")
            return
        }
        val jsonOld = prefs.getString("player_state", null)
        if (jsonOld != null) {
            try {
                val oldState = gson.fromJson(jsonOld, PlayerState::class.java)
                if (oldState.netWorth > 10_000_000 && state.netWorth < 1_000_000 && state.lastMonthExpenses < 1_000_000) {
                    android.util.Log.e("AppDebug", "Save blocked due to suspicious drop in net worth: from ${oldState.netWorth} to ${state.netWorth}")
                    return
                }
            } catch (e: Exception) {
                // Ignore if old state parse fails 
            }
        }
        prefs.edit().putString("player_state", gson.toJson(state.copy(lastSavedTimeMs = System.currentTimeMillis()))).apply()
        if (isUserLoggedIn()) {
            backupGameToCloud()
        }
    }
    val monthProgress: kotlinx.coroutines.flow.StateFlow<Float> = _monthProgress.asStateFlow()

    private fun loadCustomProperties(): List<com.example.data.PropertyItem> {
        val json = prefs.getString("property_list_state", null)
        if (json != null) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<com.example.data.PropertyItem>>() {}.type
                val savedList: List<com.example.data.PropertyItem> = gson.fromJson(json, type)
                if (savedList.isNotEmpty()) return savedList
            } catch(e: Exception) { 
                android.util.Log.e("AppDebug", "Init error (loadCustomProperties): ${e.message}")
                prefs.edit().remove("property_list_state").apply()
            }
        }
        return com.example.data.initialRealEstateCatalog
    }

    private fun saveProperties(list: List<com.example.data.PropertyItem>) {
        prefs.edit().putString("property_list_state", gson.toJson(list)).apply()
    }

    private val _realEstateMarket = MutableStateFlow(com.example.data.initialRealEstateCatalog)
    val realEstateMarket: StateFlow<List<com.example.data.PropertyItem>> = _realEstateMarket.asStateFlow()

    private val _cryptoList = MutableStateFlow(com.example.data.initialCryptoList)
    val cryptoList: StateFlow<List<com.example.data.CryptoItem>> = _cryptoList.asStateFlow()

    private fun loadCustomCollections(): List<com.example.data.CollectionItem> {
        val baseList = com.example.data.initialCollectionItems + com.example.data.initialVehicleItems
        val json = prefs.getString("collection_list_state", null)
        if (json != null) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<com.example.data.CollectionItem>>() {}.type
                val savedList: List<com.example.data.CollectionItem> = gson.fromJson(json, type)
                if (savedList.isNotEmpty()) {
                    val baseIds = baseList.map { it.id }.toSet()
                    val merged = baseList.toMutableList()
                    for (item in savedList) {
                        val itemId = (item.id as String?) ?: ""
                        if (itemId.isNotEmpty() && itemId !in baseIds) {
                            val sanitizedItem = com.example.data.CollectionItem(
                                id = itemId,
                                categoryId = (item.categoryId as String?) ?: "",
                                name = (item.name as String?) ?: "",
                                description = (item.description as String?) ?: "",
                                basePrice = item.basePrice,
                                imageUrl = (item.imageUrl as String?) ?: "",
                                releaseYear = item.releaseYear,
                                type = (item.type as String?) ?: ""
                            )
                            merged.add(sanitizedItem)
                        }
                    }
                    return merged
                }
            } catch(e: Exception) { 
                android.util.Log.e("AppDebug", "Init error (loadCustomCollections): ${e.message}")
                prefs.edit().remove("collection_list_state").apply()
            }
        }
        return baseList
    }
    
    private fun saveCollections(list: List<com.example.data.CollectionItem>) {
        prefs.edit().putString("collection_list_state", gson.toJson(list)).apply()
    }

    private val _collectionList = MutableStateFlow(com.example.data.initialCollectionItems + com.example.data.initialVehicleItems)
    val collectionList: StateFlow<List<com.example.data.CollectionItem>> = _collectionList.asStateFlow()

    private val _currentYearStartups = MutableStateFlow(com.example.data.generateYearlyStartups(1))
    val currentYearStartups: StateFlow<List<com.example.data.StartupInvestment>> = _currentYearStartups.asStateFlow()

    private val _preciousMetalsList = MutableStateFlow(com.example.data.initialPreciousMetals)
    val preciousMetalsList: StateFlow<List<com.example.data.PreciousMetal>> = _preciousMetalsList.asStateFlow()

    private fun loadCustomHousing(): List<com.example.data.HousingItem> {
        val baseList = com.example.data.initialHousingItems
        val json = prefs.getString("housing_list_state", null)
        if (json != null) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<com.example.data.HousingItem>>() {}.type
                val savedList: List<com.example.data.HousingItem> = gson.fromJson(json, type)
                if (savedList.isNotEmpty()) {
                    val baseIds = baseList.map { it.id }.toSet()
                    val merged = baseList.toMutableList()
                    for (item in savedList) {
                        val itemId = (item.id as String?) ?: ""
                        if (itemId.isNotEmpty() && !itemId.startsWith("hs_") && itemId !in baseIds) {
                            val sanitizedItem = com.example.data.HousingItem(
                                id = itemId,
                                name = (item.name as String?) ?: "",
                                location = (item.location as String?) ?: "",
                                type = (item.type as String?) ?: "",
                                buyPrice = item.buyPrice,
                                rentPrice = item.rentPrice,
                                imageUrl = item.imageUrl
                            )
                            merged.add(sanitizedItem)
                        }
                    }
                    return merged
                }
            } catch(e: Exception) { 
                prefs.edit().remove("housing_list_state").apply()
            }
        }
        return baseList
    }

    private fun saveHousing(list: List<com.example.data.HousingItem>) {
        prefs.edit().putString("housing_list_state", gson.toJson(list)).apply()
    }

    private fun autoResolveMissingHousing(state: PlayerState) {
        val currentHs = _housingList.value.toMutableList()
        val hsIds = currentHs.map { it.id }.toSet()
        val mutableHsIds = hsIds.toMutableSet()
        var addedAny = false
        
        for (owned in state.ownedHouses) {
            if (owned.housingId.isNotEmpty() && owned.housingId !in mutableHsIds) {
                val placeholder = com.example.data.HousingItem(
                    id = owned.housingId,
                    name = if (owned.housingId.startsWith("prop_custom_")) "Mansion Kustom" else "Hunian Kustom",
                    location = "Unknown",
                    type = "Custom Property",
                    buyPrice = owned.purchasedPrice,
                    rentPrice = (owned.purchasedPrice / 200).coerceAtLeast(1L),
                    imageUrl = owned.customImageUrl ?: "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=400&q=80"
                )
                currentHs.add(placeholder)
                mutableHsIds.add(owned.housingId)
                addedAny = true
            }
        }
        
        for (rented in state.rentedHouses) {
            if (rented.housingId.isNotEmpty() && rented.housingId !in mutableHsIds) {
                val placeholder = com.example.data.HousingItem(
                    id = rented.housingId,
                    name = if (rented.housingId.startsWith("prop_custom_")) "Mansion Sewa Kustom" else "Sewa Hunian Kustom",
                    location = "Unknown",
                    type = "Custom Property",
                    buyPrice = rented.monthlyRent * 200,
                    rentPrice = rented.monthlyRent,
                    imageUrl = rented.customImageUrl ?: "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=400&q=80"
                )
                currentHs.add(placeholder)
                mutableHsIds.add(rented.housingId)
                addedAny = true
            }
        }
        
        if (addedAny) {
            _housingList.value = currentHs
            saveHousing(currentHs)
        }
    }

    private val _housingList = MutableStateFlow(loadCustomHousing())
    val housingList: StateFlow<List<com.example.data.HousingItem>> = _housingList.asStateFlow()

    private val _tycoonList = MutableStateFlow(com.example.data.getInitialBillionaires())
    val tycoonList: StateFlow<List<com.example.data.Tycoon>> = _tycoonList.asStateFlow()



    // --- Advanced General & Mini Game Experiment Settings ---
    private val _monthDurationSeconds = MutableStateFlow(120f) // default 120 seconds for game-month
    val monthDurationSeconds: StateFlow<Float> = _monthDurationSeconds.asStateFlow()

    private val _stockIntervalSeconds = MutableStateFlow(30.0f) // default stock fluctuation interval
    val stockIntervalSeconds: StateFlow<Float> = _stockIntervalSeconds.asStateFlow()

    // Default SVG Path (Crown configuration)
    private val _companyLogoSvgPath = MutableStateFlow("M 10 90 L 10 30 L 35 60 L 50 20 L 65 60 L 90 30 L 90 90 Z")
    val companyLogoSvgPath: StateFlow<String> = _companyLogoSvgPath.asStateFlow()

    private val _companyLogoFillColorHex = MutableStateFlow("#FFD700") // gold
    val companyLogoFillColorHex: StateFlow<String> = _companyLogoFillColorHex.asStateFlow()

    private val _isNotificationEnabled = MutableStateFlow(true)
    val isNotificationEnabled: StateFlow<Boolean> = _isNotificationEnabled.asStateFlow()

    private val _isDarkModeSimulated = MutableStateFlow(true)
    val isDarkModeSimulated: StateFlow<Boolean> = _isDarkModeSimulated.asStateFlow()

    private val _soundVolume = MutableStateFlow(0.8f)
    val soundVolume: StateFlow<Float> = _soundVolume.asStateFlow()

    private val _gameDifficulty = MutableStateFlow("Normal") // "Easy", "Normal", "Hard", "Elite Tycoon"
    val gameDifficulty: StateFlow<String> = _gameDifficulty.asStateFlow()

    private val _marketVolatilityFactor = MutableStateFlow(1.0f) // Volatility/Shift factor: 0.1x to 5.0x
    val marketVolatilityFactor: StateFlow<Float> = _marketVolatilityFactor.asStateFlow()

    fun updateMonthDuration(seconds: Float) {
        _monthDurationSeconds.value = seconds.coerceIn(10f, 7200f)
    }

    fun updateStockInterval(seconds: Float) {
        _stockIntervalSeconds.value = seconds.coerceIn(1.0f, 60.0f)
    }

    fun updateCompanyLogo(svgPath: String, colorHex: String) {
        _companyLogoSvgPath.value = svgPath
        _companyLogoFillColorHex.value = colorHex
    }

    private val _useShortNumberFormat = MutableStateFlow(false)
    val useShortNumberFormat: StateFlow<Boolean> = _useShortNumberFormat.asStateFlow()

    fun updateGameDifficultySettings(difficulty: String, volatility: Float) {
        _gameDifficulty.value = difficulty
        _marketVolatilityFactor.value = volatility.coerceIn(0.1f, 5.0f)
    }

    fun updateGeneralSettings(isNotification: Boolean, isDarkMode: Boolean, volume: Float, useShortNum: Boolean) {
        _isNotificationEnabled.value = isNotification
        _isDarkModeSimulated.value = isDarkMode
        _soundVolume.value = volume.coerceIn(0f, 1f)
        _useShortNumberFormat.value = useShortNum
    }
    
    fun injectCapitalToBusiness(instanceId: String, amount: Long): Boolean {
        val currentState = _playerState.value
        if (amount <= 0) return false
        return updateBusinessCash(currentState, instanceId, amount.toDouble(), amount, isDeposit = true)
    }

    fun withdrawCapitalFromBusiness(instanceId: String, amount: Long): Boolean {
        val currentState = _playerState.value
        if (amount <= 0) return false
        return updateBusinessCash(currentState, instanceId, amount.toDouble(), amount, isDeposit = false)
    }

    fun injectCapitalToHolding(holdingId: String, amount: Long): Boolean {
        val currentState = _playerState.value
        if (currentState.cash < amount || amount <= 0) return false
        val holding = currentState.holdingCompanies.find { it.instanceId == holdingId } ?: return false
        val updatedHoldings = currentState.holdingCompanies.map { 
            if (it.instanceId == holdingId) it.copy(holdingCash = it.holdingCash + amount) else it 
        }
        _playerState.value = currentState.copy(cash = currentState.cash - amount, holdingCompanies = updatedHoldings)
        saveState(_playerState.value)
        return true
    }

    fun withdrawCapitalFromHolding(holdingId: String, amount: Long): Boolean {
        val currentState = _playerState.value
        if (amount <= 0) return false
        val holding = currentState.holdingCompanies.find { it.instanceId == holdingId } ?: return false
        if (holding.holdingCash < amount) return false
        val updatedHoldings = currentState.holdingCompanies.map { 
            if (it.instanceId == holdingId) it.copy(holdingCash = it.holdingCash - amount) else it 
        }
        _playerState.value = currentState.copy(cash = currentState.cash + amount, holdingCompanies = updatedHoldings)
        saveState(_playerState.value)
        return true
    }

    fun setBusinessCash(instanceId: String, newCash: Double) {
        val currentState = _playerState.value
        var changed = false
        val updatedBusinesses = currentState.ownedBusinesses.map { owned ->
            if (owned.instanceId == instanceId) {
                changed = true
                owned.copy(companyCash = newCash.coerceAtLeast(0.0))
            } else owned
        }
        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            var subChanged = false
            val updatedSubs = holding.subsidiaries.map { sub ->
                if (sub.instanceId == instanceId) {
                    changed = true
                    subChanged = true
                    sub.copy(companyCash = newCash.coerceAtLeast(0.0))
                } else sub
            }
            if (subChanged) holding.copy(subsidiaries = updatedSubs) else holding
        }
        if (changed) {
            _playerState.value = currentState.copy(
                ownedBusinesses = updatedBusinesses,
                holdingCompanies = updatedHoldings
            )
            saveState(_playerState.value)
        }
    }

    fun purchaseThemeParkLand(businessInstanceId: String, landType: ThemeParkLandType) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val newBidding = com.example.data.ActiveBidding(landType = landType)
            val newBiddings = owned.activeThemeParkBiddings + newBidding
            val newOwned = owned.copy(activeThemeParkBiddings = newBiddings)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun submitThemeParkBiddingOffer(businessInstanceId: String, biddingId: String, offer: Long) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBiddings = owned.activeThemeParkBiddings.map { bidding ->
                if (bidding.id == biddingId) {
                    bidding.copy(
                        phase = com.example.data.BiddingPhase.WAITING_REPLY,
                        monthsLeft = 2,
                        playerOffer = offer
                    )
                } else bidding
            }
            val newOwned = owned.copy(activeThemeParkBiddings = updatedBiddings)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun cancelThemeParkBidding(businessInstanceId: String, biddingId: String) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBiddings = owned.activeThemeParkBiddings.filter { it.id != biddingId }
            val newOwned = owned.copy(activeThemeParkBiddings = updatedBiddings)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun resolveThemeParkBiddingDeal(businessInstanceId: String, biddingId: String): Boolean {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val bidding = owned.activeThemeParkBiddings.find { it.id == biddingId } ?: return false
            val cost = bidding.currentAskingPrice // Deal price is current asking price
            if (owned.companyCash >= cost) {
                val newBranch = com.example.data.ThemeParkBranch(
                    locationName = bidding.landType.locationName,
                    landType = bidding.landType,
                    remainingBiddingMonths = 0 // Wait, what if remainingBiddingMonths was used for construction? The prompt says "BUAT ThemeParkBranch baru dengan status lahan ini, lalu hapus objek ActiveBidding ini dari list." Since bidding is already done, let's set it to 0 so it's directly operational / constructable.
                )
                val updatedBranches = owned.themeParkBranches + newBranch
                val updatedBiddings = owned.activeThemeParkBiddings.filter { it.id != biddingId }
                val newOwned = owned.copy(
                    companyCash = owned.companyCash - cost,
                    themeParkBranches = updatedBranches,
                    activeThemeParkBiddings = updatedBiddings
                )
                
                if (isNested && holdingId != null) {
                    val newHoldings = currentState.holdingCompanies.map { holding ->
                        if (holding.instanceId == holdingId) {
                            val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                            holding.copy(subsidiaries = newSubs)
                        } else holding
                    }
                    _playerState.value = currentState.copy(holdingCompanies = newHoldings)
                } else {
                    val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                    _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
                }
                saveState(_playerState.value)
                return true
            }
        }
        return false
    }

    fun buildThemeParkRide(
        businessInstanceId: String,
        branchId: String,
        rideTier: RideTier,
        customRideName: String,
        imageUrl: String? = null,
        zoneName: String? = null,
        ipThemeTitle: String? = null,
        ipThemeScore: Int? = null
    ): Boolean {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null && owned.companyCash >= rideTier.cost) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val newRide = ThemeParkRide(
                        name = customRideName,
                        constructionMonthsLeft = rideTier.buildMonths,
                        isConstructing = true,
                        tierDescription = rideTier.description,
                        cost = rideTier.cost,
                        imageUrl = if (imageUrl.isNullOrBlank()) null else imageUrl,
                        zoneName = zoneName,
                        ipThemeTitle = ipThemeTitle,
                        ipThemeScore = ipThemeScore
                    )
                    branch.copy(rides = (branch.rides + newRide).toMutableList())
                } else branch
            }
            
            val newOwned = owned.copy(
                companyCash = owned.companyCash - rideTier.cost,
                themeParkBranches = updatedBranches
            )
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
            return true
        }
        return false
    }

    fun purchaseAdPackage(
        businessInstanceId: String,
        branchId: String,
        adName: String,
        durationMonths: Int,
        boostMultiplier: Double,
        cost: Long
    ): Boolean {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null && owned.companyCash >= cost) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(
                        activeAdName = adName,
                        adMonthsLeft = durationMonths,
                        adBoostMultiplier = boostMultiplier
                    )
                } else branch
            }
            
            val newOwned = owned.copy(
                companyCash = owned.companyCash - cost,
                themeParkBranches = updatedBranches
            )
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
            return true
        }
        return false
    }

    fun buildThemeParkFacility(
        businessInstanceId: String,
        branchId: String,
        catalogEntry: com.example.viewmodel.ThemeParkEngine.ThemeParkFacilityCatalogEntry,
        customZoneName: String = "Belum Terzonasi"
    ): Boolean {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null && owned.companyCash >= catalogEntry.buildCost) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val newFacility = ThemeParkFacility(
                        catalogId = catalogEntry.catalogId,
                        name = catalogEntry.name,
                        buildCost = catalogEntry.buildCost,
                        maintenanceCost = catalogEntry.maintenanceCost,
                        fnbBoostPercent = catalogEntry.fnbBoostPercent,
                        appealBoost = catalogEntry.appealBoost,
                        zoneName = customZoneName
                    )
                    branch.copy(facilities = branch.facilities + newFacility)
                } else branch
            }
            
            val newOwned = owned.copy(
                companyCash = owned.companyCash - catalogEntry.buildCost,
                themeParkBranches = updatedBranches
            )
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
            return true
        }
        return false
    }

    fun updateThemeParkFacilityDetails(
        businessInstanceId: String,
        branchId: String,
        facilityId: String,
        newName: String,
        newZoneName: String,
        newImageUrl: String? = null
    ) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val updatedFac = branch.facilities.map { fac ->
                        if (fac.id == facilityId) {
                            fac.copy(name = newName, zoneName = newZoneName, imageUrl = newImageUrl)
                        } else fac
                    }
                    branch.copy(facilities = updatedFac)
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun demolishThemeParkFacility(
        businessInstanceId: String,
        branchId: String,
        facilityId: String
    ) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(facilities = branch.facilities.filter { it.id != facilityId })
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun toggleThemeParkRidePause(
        businessInstanceId: String,
        branchId: String,
        rideId: String
    ) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val updatedRides = branch.rides.map { ride ->
                        if (ride.id == rideId) {
                            ride.copy(isPaused = !ride.isPaused)
                        } else ride
                    }
                    branch.copy(rides = updatedRides.toMutableList())
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun demolishThemeParkRide(
        businessInstanceId: String,
        branchId: String,
        rideId: String
    ) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val updatedRides = branch.rides.filter { it.id != rideId }
                    branch.copy(rides = updatedRides.toMutableList())
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun startThemeParkRideMaintenance(
        businessInstanceId: String,
        branchId: String,
        rideId: String
    ): Boolean {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val branch = owned.themeParkBranches.find { it.id == branchId } ?: return false
            val ride = branch.rides.find { it.id == rideId } ?: return false
            
            val tier = RideTier.values().find { it.cost == ride.cost }?.level ?: 1
            val duration = (tier / 2).coerceAtLeast(1)
            val costToPay = ride.maintenanceCost * duration
            
            if (owned.companyCash >= costToPay) {
                val updatedBranches = owned.themeParkBranches.map { b ->
                    if (b.id == branchId) {
                        val updatedRides = b.rides.map { r ->
                            if (r.id == rideId) {
                                r.copy(
                                    isUnderMaintenance = true,
                                    maintenanceMonthsLeft = duration,
                                    isPaused = false
                                )
                            } else r
                        }
                        b.copy(rides = updatedRides.toMutableList())
                    } else b
                }
                
                val newOwned = owned.copy(
                    companyCash = owned.companyCash - costToPay,
                    themeParkBranches = updatedBranches
                )
                
                if (isNested && holdingId != null) {
                    val newHoldings = currentState.holdingCompanies.map { holding ->
                        if (holding.instanceId == holdingId) {
                            val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                            holding.copy(subsidiaries = newSubs)
                        } else holding
                    }
                    _playerState.value = currentState.copy(holdingCompanies = newHoldings)
                } else {
                    val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                    _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
                }
                saveState(_playerState.value)
                return true
            }
        }
        return false
    }

    fun deleteThemeParkBranch(businessInstanceId: String, branchId: String) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val branchToRemove = owned.themeParkBranches.find { it.id == branchId }
            if (branchToRemove != null) {
                val landRefund = (branchToRemove.landType.basePrice * 0.5).toLong()
                val updatedBranches = owned.themeParkBranches.filter { it.id != branchId }
                val newOwned = owned.copy(
                    themeParkBranches = updatedBranches,
                    companyCash = owned.companyCash + landRefund
                )
                
                if (isNested && holdingId != null) {
                    val newHoldings = currentState.holdingCompanies.map { holding ->
                        if (holding.instanceId == holdingId) {
                            val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                            holding.copy(subsidiaries = newSubs)
                        } else holding
                    }
                    _playerState.value = currentState.copy(holdingCompanies = newHoldings)
                } else {
                    val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                    _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
                }
                saveState(_playerState.value)
            }
        }
    }

    fun updateThemeParkBranchImage(businessInstanceId: String, branchId: String, newImageUrl: String?) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(imageUrl = if (newImageUrl.isNullOrBlank()) null else newImageUrl)
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun moveThemeParkZone(businessInstanceId: String, branchId: String, index: Int, isUp: Boolean) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val newZones = branch.parkZones.toMutableList()
                    if (isUp && index > 0) {
                        val temp = newZones[index]
                        newZones[index] = newZones[index - 1]
                        newZones[index - 1] = temp
                    } else if (!isUp && index < newZones.size - 1) {
                        val temp = newZones[index]
                        newZones[index] = newZones[index + 1]
                        newZones[index + 1] = temp
                    }
                    branch.copy(parkZones = newZones)
                } else branch
            }
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun addThemeParkZone(businessInstanceId: String, branchId: String, zoneName: String) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null && zoneName.isNotBlank()) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId && !branch.parkZones.contains(zoneName)) {
                    val newZones = branch.parkZones.toMutableList()
                    newZones.add(zoneName)
                    branch.copy(parkZones = newZones)
                } else branch
            }
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun updateThemeParkRideZoneAndIP(businessInstanceId: String, branchId: String, rideId: String, newZoneName: String?, newIpTitle: String?, newIpScore: Int?) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val updatedRides = branch.rides.map { ride ->
                        if (ride.id == rideId) {
                            ride.copy(
                                zoneName = newZoneName,
                                ipThemeTitle = newIpTitle,
                                ipThemeScore = newIpScore
                            )
                        } else ride
                    }
                    branch.copy(rides = updatedRides.toMutableList())
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun updateThemeParkRideDetails(businessInstanceId: String, branchId: String, rideId: String, newName: String, newImageUrl: String?) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    val updatedRides = branch.rides.map { ride ->
                        if (ride.id == rideId) {
                            ride.copy(
                                name = newName,
                                imageUrl = if (newImageUrl.isNullOrBlank()) null else newImageUrl
                            )
                        } else ride
                    }
                    branch.copy(rides = updatedRides.toMutableList())
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun launchThemeParkBranch(businessInstanceId: String, branchId: String) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(isLaunched = true)
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun activateThemeParkHype(businessInstanceId: String, branchId: String, cost: Long): Boolean {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null && owned.companyCash >= cost) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(hasHypeMarketing = true, hypeMonthsLeft = 1)
                } else branch
            }
            
            val newOwned = owned.copy(
                companyCash = owned.companyCash - cost,
                themeParkBranches = updatedBranches
            )
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
            return true
        }
        return false
    }

    fun updateThemeParkTicketPrices(businessInstanceId: String, branchId: String, pRegular: Long, pTerusan: Long, pVIP: Long, pFamily: Long) {
        val currentState = _playerState.value
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        var isNested = false
        var holdingId: String? = null
        if (owned == null) {
            currentState.holdingCompanies.forEach { h ->
                val sub = h.subsidiaries.find { it.instanceId == businessInstanceId }
                if (sub != null) {
                    owned = sub
                    isNested = true
                    holdingId = h.instanceId
                }
            }
        }
        if (owned != null) {
            val updatedBranches = owned!!.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(
                        priceRegular = pRegular,
                        priceTerusan = pTerusan,
                        priceVIP = pVIP,
                        priceFamily = pFamily
                    )
                } else branch
            }
            val newOwned = owned!!.copy(themeParkBranches = updatedBranches)
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    fun renameThemeParkBranch(businessInstanceId: String, branchId: String, newName: String) {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == businessInstanceId }
        
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == businessInstanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        
        if (owned != null) {
            val updatedBranches = owned.themeParkBranches.map { branch ->
                if (branch.id == branchId) {
                    branch.copy(customName = newName)
                } else branch
            }
            
            val newOwned = owned.copy(themeParkBranches = updatedBranches)
            
            if (isNested && holdingId != null) {
                val newHoldings = currentState.holdingCompanies.map { holding ->
                    if (holding.instanceId == holdingId) {
                        val newSubs = holding.subsidiaries.map { if (it.instanceId == businessInstanceId) newOwned else it }
                        holding.copy(subsidiaries = newSubs)
                    } else holding
                }
                _playerState.value = currentState.copy(holdingCompanies = newHoldings)
            } else {
                val newOwnedList = currentState.ownedBusinesses.map { if (it.instanceId == businessInstanceId) newOwned else it }
                _playerState.value = currentState.copy(ownedBusinesses = newOwnedList)
            }
            saveState(_playerState.value)
        }
    }

    private fun updateBusinessCash(currentState: PlayerState, instanceId: String, businessCashChange: Double, playerCashChange: Long, isDeposit: Boolean): Boolean {
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        if (owned == null) return false
        
        if (isDeposit) {
            if (isNested && holdingId != null) {
                val holding = currentState.holdingCompanies.find { it.instanceId == holdingId }!!
                if (holding.holdingCash < playerCashChange) return false
            } else {
                if (currentState.cash < playerCashChange) return false
            }
            if (owned.companyCash + businessCashChange < 0) return false // Should not happen
        } else {
            if (owned.companyCash - businessCashChange < 0) return false
        }
        
        val actualBusinessChange = if (isDeposit) businessCashChange else -businessCashChange
        val newOwned = owned.copy(companyCash = owned.companyCash + actualBusinessChange)
        
        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map { if (it.instanceId == instanceId) newOwned else it }
                    val newHoldingCash = if (isDeposit) holding.holdingCash - playerCashChange else holding.holdingCash + playerCashChange
                    holding.copy(subsidiaries = newSubs, holdingCash = newHoldingCash)
                } else holding
            }
            _playerState.value = currentState.copy(
                holdingCompanies = newHoldings
            )
        } else {
            val newGlobalCash = if (isDeposit) currentState.cash - playerCashChange else currentState.cash + playerCashChange
            _playerState.value = currentState.copy(
                cash = newGlobalCash,
                ownedBusinesses = currentState.ownedBusinesses.map { if (it.instanceId == instanceId) newOwned else it }
            )
        }
        saveState(_playerState.value)
        return true
    }

    fun setPlayerCash(amount: Long) {
        val currentState = _playerState.value
        val updatedState = currentState.copy(
            cash = amount
        )
        val newNetWorth = updatedState.netAssetValue(
            stockList = _stockList.value,
            cryptoList = _cryptoList.value,
            realEstateMarket = _realEstateMarket.value,
            collectionList = _collectionList.value,
            preciousMetalsList = _preciousMetalsList.value
        )
        _playerState.value = updatedState.copy(netWorth = newNetWorth)
        saveState(_playerState.value)
    }

    fun investInStartup(startupId: String) {
        val startup = _currentYearStartups.value.find { it.id == startupId } ?: return
        val currentCash = _playerState.value.cash
        
        if (currentCash >= startup.requiredInvestment) {
            val newCash = currentCash - startup.requiredInvestment
            val activeList = _playerState.value.activeStartupInvestments.toMutableList()
            
            val activeInv = com.example.data.ActiveStartupInvestment(
                id = java.util.UUID.randomUUID().toString(),
                startupName = startup.name,
                investedAmount = startup.requiredInvestment,
                potentialReturn = (startup.requiredInvestment * startup.potentialReturnMultiplier).toLong(),
                monthsRemaining = startup.durationMonths,
                successProbability = startup.successProbability
            )
            activeList.add(activeInv)
            
            // Remove from available startups for this year
            val updatedStartups = _currentYearStartups.value.filter { it.id != startupId }
            _currentYearStartups.value = updatedStartups
            
            _playerState.value = _playerState.value.copy(
                cash = newCash,
                activeStartupInvestments = activeList
            )
        }
    }

    fun buyCrypto(symbol: String, price: Double, amount: Double) {
        val requiredCashUsd = price * amount
        val currentCash = _playerState.value.cash

        if (currentCash >= requiredCashUsd) {
            val newCash = currentCash - requiredCashUsd.toLong()
            val ownedList = _playerState.value.ownedCrypto.toMutableList()
            
            val existing = ownedList.find { it.symbol == symbol }
            if (existing != null) {
                val totalAmount = existing.amount + amount
                val newAveragePrice = ((existing.amount * existing.averagePrice) + (amount * price)) / totalAmount
                ownedList.remove(existing)
                ownedList.add(com.example.data.OwnedCrypto(symbol, newAveragePrice, totalAmount))
            } else {
                ownedList.add(com.example.data.OwnedCrypto(symbol, price, amount))
            }
            
            _playerState.value = _playerState.value.copy(
                cash = newCash,
                ownedCrypto = ownedList
            )
        }
    }

    fun buyStock(ticker: String, price: Double, quantity: Long) {
        val stockToBuy = _stockList.value.find { it.ticker == ticker } ?: return
        val currentState = _playerState.value
        if (!com.caesar.gametycoon.stock.takeover.StockAcquisitionRepository.canBuyRetailShares(currentState, stockToBuy, quantity)) {
            return
        }
        val isIndo = ticker.contains(".JK")
        val requiredCashUsd = price * quantity
        val priceInUsd = price
        val currentCash = _playerState.value.cash

        if (currentCash >= requiredCashUsd) {
            val currentState = _playerState.value
            val existingStocks = currentState.ownedStocks.toMutableList()
            val existingIndex = existingStocks.indexOfFirst { it.ticker == ticker }
            
            if (existingIndex != -1) {
                val existing = existingStocks[existingIndex]
                val newShares = existing.shares + quantity
                if (newShares < 0L) return // anti-overflow
                val newAvgPrice = ((existing.shares * existing.averagePrice) + (quantity * priceInUsd)) / newShares
                existingStocks[existingIndex] = existing.copy(shares = newShares, averagePrice = newAvgPrice)
            } else {
                existingStocks.add(OwnedStock(ticker, priceInUsd, quantity))
            }

            _playerState.value = currentState.copy(
                cash = currentCash - requiredCashUsd.toLong(),
                ownedStocks = existingStocks,
                corporateStockPortfolio = existingStocks
            )
        }
    }

    fun sellStock(ticker: String, price: Double, quantity: Long) {
        val stockToSell = _stockList.value.find { it.ticker == ticker } ?: return
        val isIndo = ticker.contains(".JK")
        val revenueUsd = price * quantity

        val currentState = _playerState.value
        val existingStocks = currentState.ownedStocks.toMutableList()
        val existingIndex = existingStocks.indexOfFirst { it.ticker == ticker }

        if (existingIndex != -1) {
            val existing = existingStocks[existingIndex]
            if (existing.shares >= quantity) {
                val newShares = existing.shares - quantity
                if (newShares == 0L) {
                    existingStocks.removeAt(existingIndex)
                } else {
                    existingStocks[existingIndex] = existing.copy(shares = newShares)
                }
                
                _playerState.value = currentState.copy(
                    cash = currentState.cash + revenueUsd.toLong(),
                    ownedStocks = existingStocks,
                    corporateStockPortfolio = existingStocks
                )
            }
        }
    }

    private fun logToPrivateLedger(state: PlayerState, title: String, amount: Long, isIncome: Boolean): PlayerState {
        if (amount <= 0L) return state
        val record = com.example.data.PrivateLedgerRecord(
            monthTick = state.inGameMonth,
            title = title,
            amount = Math.abs(amount),
            isIncome = isIncome
        )
        val newList = (listOf(record) + state.privateLedgerHistory).take(200)
        return state.copy(privateLedgerHistory = newList)
    }

    fun buyPrivateStock(ticker: String, price: Double, quantity: Long) {
        val stockToBuy = _stockList.value.find { it.ticker == ticker } ?: return
        val requiredCashUsd = price * quantity
        val currentPrivateBalance = _playerState.value.privateBalance

        if (currentPrivateBalance >= requiredCashUsd) {
            val currentState = _playerState.value
            val existingStocks = currentState.privateStockPortfolio.toMutableList()
            val existingIndex = existingStocks.indexOfFirst { it.ticker == ticker }
            
            if (existingIndex != -1) {
                val existing = existingStocks[existingIndex]
                val newShares = existing.shares + quantity
                if (newShares < 0L) return
                val newAvgPrice = ((existing.shares * existing.averagePrice) + (quantity * price)) / newShares
                existingStocks[existingIndex] = existing.copy(shares = newShares, averagePrice = newAvgPrice)
            } else {
                existingStocks.add(OwnedStock(ticker, price, quantity))
            }

            val nextState = currentState.copy(
                privateBalance = currentPrivateBalance - requiredCashUsd.toLong(),
                privateStockPortfolio = existingStocks
            )
            val loggedState = logToPrivateLedger(nextState, "Beli Saham $ticker ($quantity Lembar)", requiredCashUsd.toLong(), false)
            _playerState.value = loggedState
            saveState(loggedState)
        }
    }

    fun sellPrivateStock(ticker: String, price: Double, quantity: Long) {
        val stockToSell = _stockList.value.find { it.ticker == ticker } ?: return
        val revenueUsd = price * quantity
        val currentState = _playerState.value
        val existingStocks = currentState.privateStockPortfolio.toMutableList()
        val existingIndex = existingStocks.indexOfFirst { it.ticker == ticker }

        if (existingIndex != -1) {
            val existing = existingStocks[existingIndex]
            if (existing.shares >= quantity) {
                val newShares = existing.shares - quantity
                if (newShares == 0L) {
                    existingStocks.removeAt(existingIndex)
                } else {
                    existingStocks[existingIndex] = existing.copy(shares = newShares)
                }
                
                val nextState = currentState.copy(
                    privateBalance = currentState.privateBalance + revenueUsd.toLong(),
                    privateStockPortfolio = existingStocks
                )
                val loggedState = logToPrivateLedger(nextState, "Jual Saham $ticker ($quantity Lembar)", revenueUsd.toLong(), true)
                _playerState.value = loggedState
                saveState(loggedState)
            }
        }
    }
    
    fun getSortedStocks(list: List<StockItem>, activeFilter: String): List<StockItem> {
        return when (activeFilter) {
            "Highest Dividend" -> list.sortedByDescending { getMarketStats(it).dividendYield }
            "Lowest Dividend" -> list.sortedBy { getMarketStats(it).dividendYield }
            "Highest Market Cap" -> list.sortedByDescending { it.sharesOutstanding * it.currentPrice }
            "Lowest Market Cap" -> list.sortedBy { it.sharesOutstanding * it.currentPrice }
            else -> list
        }
    }

    fun resetGameProgress() {
        _monthProgress.value = 0f
        _playerState.value = PlayerState(
            cash = 0,
            netWorth = 0,
            inGameMonth = 1,
            inGameYear = 1,
            lastMonthIncome = 0,
            lastMonthExpenses = 0,
            lastMonthNetProfit = 0,
            ownedBusinesses = emptyList(),
            ownedStocks = emptyList(),
            ownedProperties = emptyList(),
            ownedCrypto = emptyList(),
            activeStartupInvestments = emptyList(),
            ownedCollections = emptyList(),
            ownedMetals = emptyMap(),
            ownedHouses = emptyList(),
            rentedHouses = emptyList(),
            customBusinessCatalog = emptyList(),
            rebrandedCompanies = emptyMap()
        )
        saveState(_playerState.value)
    }

    fun rebrandCompany(ticker: String, oldName: String, newName: String) {
        val currentState = _playerState.value
        val updatedRebrands = currentState.rebrandedCompanies.toMutableMap()
        updatedRebrands[ticker] = newName
        
        _playerState.value = currentState.copy(rebrandedCompanies = updatedRebrands)
        saveState(_playerState.value)
        
        val newsText = "\uD83D\uDEA8 MEGA AKUISISI: $oldName telah resmi diakuisisi secara penuh dan kini berganti nama menjadi $newName! Pasar merespon dengan takjub."
        val newsItem = MarketNews(
            id = "rebrand_${System.currentTimeMillis()}",
            text = newsText,
            type = "BULL"
        )
        _newsFeed.value = (listOf(newsItem) + _newsFeed.value).take(20)
    }

    fun executeStrategicTakeover(stock: com.example.data.StockItem, finalPrice: Double, targetStake: Double, customName: String? = null) {
        val result = com.caesar.gametycoon.stock.takeover.StockAcquisitionRepository.executeTakeover(_playerState.value, stock, finalPrice, targetStake, customName)
        if (result.isSuccess) {
            _playerState.value = result.updatedPlayerState
            saveState(result.updatedPlayerState)
            result.marketNews?.let { _newsFeed.value = (listOf(it) + _newsFeed.value).take(20) }
        }
    }

    fun integrateStockToHolding(ticker: String, newName: String) {
        val currentState = _playerState.value
        val ownedStocks = currentState.ownedStocks.toMutableList()
        val stockIndex = ownedStocks.indexOfFirst { it.ticker == ticker && !it.isIntegratedToHolding }
        if (stockIndex == -1) return
        
        ownedStocks[stockIndex] = ownedStocks[stockIndex].copy(isIntegratedToHolding = true)
        
        val liveStock = _stockList.value.find { it.ticker == ticker } ?: return
        
        val fallbackCatalogId = "corporate_hq" 
        val newBusiness = com.example.data.OwnedBusiness(
            instanceId = java.util.UUID.randomUUID().toString(),
            catalogId = fallbackCatalogId, 
            customName = newName,
            level = 1,
            acquiredStockTicker = ticker,
            parentId = null
        )
        
        var addedToHolding = false
        var updatedHoldings = currentState.holdingCompanies
        if (currentState.megaHolding.isActive && newBusiness.acquiredStockTicker == null) {
            val hQ = currentState.holdingCompanies.firstOrNull() 
            if (hQ != null) {
                updatedHoldings = currentState.holdingCompanies.map { h ->
                    if (h.instanceId == hQ.instanceId) {
                        h.copy(subsidiaries = h.subsidiaries + newBusiness)
                    } else h
                }
                addedToHolding = true
            }
        }
        
        if (addedToHolding) {
            _playerState.value = currentState.copy(
                ownedStocks = ownedStocks,
                corporateStockPortfolio = ownedStocks,
                holdingCompanies = updatedHoldings
            )
        } else {
            _playerState.value = currentState.copy(
                ownedStocks = ownedStocks,
                corporateStockPortfolio = ownedStocks,
                ownedBusinesses = currentState.ownedBusinesses + newBusiness
            )
        }
        
        saveState(_playerState.value)
        
        val newsText = "\uD83C\uDFE2 CORPORATE MERGER: Perusahaan Publik $ticker telah dicabut dari publik (Go-Private) dan diintegrasikan secara penuh ke dalam Mega Holding sebagai $newName!"
        val mergerNewsItem = MarketNews(
            id = "merger_${System.currentTimeMillis()}",
            text = newsText,
            type = "BULL"
        )
        _newsFeed.value = (listOf(mergerNewsItem) + _newsFeed.value).take(20)
    }

    private val _stockList = MutableStateFlow<List<StockItem>>(emptyList())
    val stockList: StateFlow<List<StockItem>> = _stockList.asStateFlow()

    private val initialPrices = mutableMapOf<String, Double>()

    private fun startCryptoMarketLoop() {
        viewModelScope.launch {
            val initialCryptoPrices = com.example.data.initialCryptoList.associate { it.symbol to it.currentPrice }
            while (true) {
                delay((_stockIntervalSeconds.value * 1000f).toLong().coerceAtLeast(100L))
                val volatilityMultiplier = _marketVolatilityFactor.value * 2.5f // Crypto is highly volatile
                val triggerNews = Math.random() < 0.10
                
                var shock = 0.0
                var newsItem: MarketNews? = null
                
                if (triggerNews) {
                    val rand = Math.random()
                    if (rand < 0.5) {
                        shock = 0.04 + (Math.random() * 0.06) // PUMP: +4% to +10%
                        newsItem = MarketNews(id = "crypto_b_${System.currentTimeMillis()}", text = "CRYPTO PUMP: Institusi besar mulai adopsi masal blockchain!", type = "BULL")
                    } else {
                        shock = -0.04 - (Math.random() * 0.06) // CRASH: -4% to -10%
                        newsItem = MarketNews(id = "crypto_b_${System.currentTimeMillis()}", text = "CRYPTO CRASH: Regulasi ketat memukul pasar kripto!", type = "BEAR")
                    }
                    val newFeeds = listOf(newsItem) + _newsFeed.value
                    _newsFeed.value = newFeeds.take(20)
                }

                val updatedCrypto = _cryptoList.value.map { crypto ->
                    val baseline = initialCryptoPrices[crypto.symbol] ?: crypto.currentPrice
                    val cryptoTrend = 0.0005 // Mild trend
                    val cryptoVol = 0.015 * volatilityMultiplier
                    
                    val newPrice = com.example.ui.calculateFluctuatingPrice(
                        currentPrice = crypto.currentPrice,
                        volatility = cryptoVol,
                        trend = cryptoTrend,
                        eventShock = shock,
                        minPrice = 0.000001
                    )
                    val newChangeAbs = newPrice - baseline
                    val newChangePct = (newChangeAbs / baseline) * 100
                    
                    crypto.copy(
                        currentPrice = newPrice,
                        changePercentage = newChangePct
                    )
                }.toMutableList()
                
                _cryptoList.value = updatedCrypto
            }
        }
    }

    private fun startTycoonMarketLoop() {
        viewModelScope.launch {
            while (true) {
                delay((_stockIntervalSeconds.value * 2000f).toLong().coerceAtLeast(200L))
                updateTycoons()
            }
        }
    }
    
    private fun updateTycoons() {
        val currentTycoons = _tycoonList.value
        val player = _playerState.value
        
        // Remove old player dummy entry if exists
        var updated = currentTycoons.filter { !it.isPlayer }.toMutableList()
        
        // add player
        updated.add(com.example.data.Tycoon("player", "You", player.netWorth, true))
        
        updated = updated.map { tycoon ->
            if (tycoon.isPlayer) {
                tycoon
            } else {
                val fluctuation = (Math.random() * 0.03 - 0.01) // -1% to +2%
                val newWorth = tycoon.netWorth + (tycoon.netWorth * fluctuation).toLong()
                tycoon.copy(netWorth = newWorth)
            }
        }.toMutableList()
        
        _tycoonList.value = updated.sortedByDescending { it.netWorth }
    }
    
    val earningsReport: StateFlow<EarningsReport> = kotlinx.coroutines.flow.combine(
        _playerState, _stockList, _realEstateMarket, _cryptoList
    ) { state, stockList, realEstateMarket, cryptoList ->
        var business = 0L
        state.ownedBusinesses.forEach { owned ->
            if (owned.parentId.isNullOrEmpty()) {
                business += owned.calculateGrossRevenue()
            }
        }
        state.holdingCompanies.forEach { holding ->
            business += com.example.data.CorporateFinanceManager.calculateHoldingMonthlyRevenue(holding, state)
        }

        var rent = 0L
        state.ownedProperties.forEach { owned ->
            val propItem = realEstateMarket.find { it.id == owned.propertyId }
            if (propItem != null) {
                val isSultan = owned.condition == 100 && owned.currentEstimatedValue > propItem.basePrice
                val multiplier = if (isSultan) 1.5 else (owned.condition / 100.0)
                rent += (propItem.baseRentalIncome * multiplier).toLong()
            }
        }

        var dividends = 0.0
        state.ownedStocks.forEach { owned ->
            val liveStock = stockList.find { it.ticker == owned.ticker }
            if (liveStock != null) {
                val isIndo = owned.ticker.contains(".JK")
                val currentPriceUsd = liveStock.currentPrice
                val stats = com.example.data.getMarketStats(liveStock)
                dividends += (owned.shares * currentPriceUsd) * (stats.dividendYield / 100.0 / 12.0)
            }
        }

        var cryptoProfit = 0L
        state.ownedCrypto.forEach { owned ->
            val livePrice = cryptoList.find { it.symbol == owned.symbol }?.currentPrice ?: owned.averagePrice
            val profit = (livePrice - owned.averagePrice) * owned.amount
            cryptoProfit += profit.toLong()
        }

        EarningsReport(business, rent, dividends.toLong(), cryptoProfit)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = EarningsReport()
    )

    init { 
        // Register all decoupled domain engines into MonthlyTickRegistry
        monthlyTickRegistry.registerSubscriber(coreBusinessEngine)
        monthlyTickRegistry.registerSubscriber(contentCreatorEngine)
        monthlyTickRegistry.registerSubscriber(streamingEngine)
        monthlyTickRegistry.registerSubscriber(filmProductionEngine)
        monthlyTickRegistry.registerSubscriber(bankingSubsystemEngine)
        monthlyTickRegistry.registerSubscriber(themeParkSubsystemEngine)
        monthlyTickRegistry.registerSubscriber(hospitalitySubsystemEngine)
        monthlyTickRegistry.registerSubscriber(footballClubEngine)
        monthlyTickRegistry.registerSubscriber(aviationEngine)
        monthlyTickRegistry.registerSubscriber(logisticsEngine)
        monthlyTickRegistry.registerSubscriber(constructionEngine)
        monthlyTickRegistry.registerSubscriber(gamePublisherEngine)
        monthlyTickRegistry.registerSubscriber(aiCloudEngine)

        try {
            if (com.google.firebase.FirebaseApp.getApps(application).isEmpty()) {
                try {
                    com.google.firebase.FirebaseApp.initializeApp(application)
                } catch (e: Exception) {
                    val options = com.google.firebase.FirebaseOptions.Builder()
                        .setApplicationId("1:354378335041:android:b37d60e8709c411f809c41")
                        .setApiKey("AIzaSyDummyKeyForGracefulDegradation")
                        .setProjectId("mega-holding-simulator")
                        .build()
                    com.google.firebase.FirebaseApp.initializeApp(application, options)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseInit", "Error: ${e.message}")
        }

        try {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    // Safe Initialization inside IO Thread
                    _playerState.value = loadState()
                    _realEstateMarket.value = loadCustomProperties()
                    _collectionList.value = loadCustomCollections()
                    autoResolveMissingHousing(_playerState.value)
                    
                    val initialStocks = generateStockData()
                    _stockList.value = initialStocks
                    initialStocks.forEach { initialPrices[it.ticker] = it.currentPrice }

                    val currentState = _playerState.value
                    val now = System.currentTimeMillis()
                    val elapsedMs = now - currentState.lastSavedTimeMs
                    
                    // Idle Ratio: 1 Real Life Day (86,400,000 ms) = 1 In-Game Year (12 Months)
                    // Which means 1 offline In-Game Month = 7,200,000 ms (2 Real Hours)
                    val offlineMonthMs = 7_200_000L
                    
                    if (elapsedMs > offlineMonthMs && currentState.lastSavedTimeMs > 0) {
                        val missedMonths = (elapsedMs / offlineMonthMs).toInt().coerceAtMost(24) // Max 2 In-Game Years progression offline
                        
                        repeat(missedMonths) {
                            processMonthlyTick(true) 
                        }
                        _playerState.value = _playerState.value.copy(lastSavedTimeMs = System.currentTimeMillis())
                        saveState(_playerState.value)
                    } else if (currentState.lastSavedTimeMs > 0) {
                        // Update saved time directly without tick if not enough time has passed
                        _playerState.value = _playerState.value.copy(lastSavedTimeMs = now)
                        saveState(_playerState.value)
                    }

                    launch(kotlinx.coroutines.Dispatchers.Main) {
                        startGameLoop() 
                        startStockMarketLoop()
                        startCryptoMarketLoop()
                        startTycoonMarketLoop()
                        launch {
                            startCloudAutoSaveLoop()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("AppDebug", "Init error in IO thread: ${e.message}")
                    resetGameProgress()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AppDebug", "Init error: ${e.message}")
            resetGameProgress()
        }
    }

    private val _newsFeed = MutableStateFlow(listOf(
        MarketNews("0", "Sesi Pasar dibuka. Seluruh pasar global dan domestik beroperasi normal.", "NEUTRAL")
    ))
    val newsFeed: StateFlow<List<MarketNews>> = _newsFeed.asStateFlow()

    private var newsCounter = 1

    private enum class MarketTrend {
        BULL_MARKET, BEAR_MARKET, STEADY_GROWTH, STEADY_BLEED, THE_LOST_DECADE, LONG_TERM_CYCLICAL, WHIPSAW_TRAP
    }

    private data class StockTrendState(
        var currentTrend: MarketTrend,
        var durationLeftMs: Long,
        var cyclePhase: Int = 1
    )

    private val stockTrends = mutableMapOf<String, StockTrendState>()

    private fun assignNewTrend(ticker: String, previousTrend: MarketTrend?): StockTrendState {
        val monthMs = _monthDurationSeconds.value * 1000L
        val yearMs = 12 * monthMs
        
        // Post-Crash Whipsaw Rule
        if (previousTrend == MarketTrend.BEAR_MARKET && Math.random() < 0.60) {
            return StockTrendState(MarketTrend.WHIPSAW_TRAP, (Math.random() * yearMs).toLong())
        }
        
        // Forced follow-up after Whipsaw Trap
        if (previousTrend == MarketTrend.WHIPSAW_TRAP) {
            val next = if (Math.random() < 0.5) MarketTrend.STEADY_BLEED else MarketTrend.BEAR_MARKET
            val dur = ((if(next == MarketTrend.BEAR_MARKET) 1 else 2) + Math.random() * 2) * yearMs
            return StockTrendState(next, dur.toLong())
        }
        
        // Standard distribution
        val rand = Math.random()
        val (trend, durationYears) = when {
            rand < 0.15 -> MarketTrend.BULL_MARKET to (1 + Math.random() * 2)
            rand < 0.30 -> MarketTrend.BEAR_MARKET to (1 + Math.random() * 1)
            rand < 0.50 -> MarketTrend.STEADY_GROWTH to (3 + Math.random() * 2)
            rand < 0.70 -> MarketTrend.STEADY_BLEED to (2 + Math.random() * 2)
            rand < 0.85 -> MarketTrend.THE_LOST_DECADE to (5 + Math.random() * 5)
            else -> MarketTrend.LONG_TERM_CYCLICAL to listOf(3.0, 5.0, 7.0, 10.0).random()
        }
        return StockTrendState(trend, (durationYears * yearMs).toLong())
    }

    private fun pushMarketNewsForTransition(ticker: String, name: String, targetTrend: MarketTrend) {
        val (text, type) = when (targetTrend) {
            MarketTrend.BULL_MARKET -> "INSIDER TIP: Laporan keuangan $ticker ($name) sangat positif! Broker memprediksi lonjakan harga tajam." to "BULL"
            MarketTrend.BEAR_MARKET -> "PANIC SELL: Skandal internal melanda $ticker ($name). Harga diprediksi akan terjun bebas!" to "BEAR"
            MarketTrend.THE_LOST_DECADE -> "ANALISIS: Prospek $ticker ($name) dinilai stagnan untuk beberapa tahun ke depan. Pasar merespon dingin." to "NEUTRAL"
            MarketTrend.WHIPSAW_TRAP -> "BREAKING: $ticker ($name) tiba-tiba meroket keras hari ini! Momen kebangkitan atau hanya jebakan banteng (Bull Trap)?" to "BULL"
            MarketTrend.LONG_TERM_CYCLICAL -> "MARKET WATCH: $ticker ($name) berpotensi mengalami volatilitas ekstrem. Awas ayunan harga yang liar!" to "NEUTRAL"
            else -> return // STEADY_GROWTH, STEADY_BLEED do not need to spam news
        }
        
        val newsItem = MarketNews(
            id = newsCounter.toString(),
            text = text,
            type = type
        )
        newsCounter++
        _newsFeed.value = (listOf(newsItem) + _newsFeed.value).take(20)
    }

    private fun startStockMarketLoop() {
        viewModelScope.launch {
            while (true) {
                val delayMs = (_stockIntervalSeconds.value * 1000f).toLong().coerceAtLeast(100L)
                delay(delayMs)
                
                val volatility = _marketVolatilityFactor.value
                val monthMs = _monthDurationSeconds.value * 1000L
                
                val updatedList = _stockList.value.map { stock ->
                    val baseline = initialPrices[stock.ticker] ?: stock.currentPrice
                    
                    var trendState = stockTrends[stock.ticker]
                    var newlyTransitioned = false
                    val oldTrend = trendState?.currentTrend
                    
                    if (trendState == null || trendState.durationLeftMs <= 0) {
                        trendState = assignNewTrend(stock.ticker, oldTrend)
                        stockTrends[stock.ticker] = trendState
                        newlyTransitioned = true
                    } else {
                        trendState.durationLeftMs -= delayMs
                    }
                    
                    var baseDrift = 0.0
                    var randVolatility = 0.0
                    
                    when (trendState.currentTrend) {
                        MarketTrend.BULL_MARKET -> {
                            baseDrift = 0.003
                            randVolatility = 0.012
                        }
                        MarketTrend.BEAR_MARKET -> {
                            baseDrift = -0.004
                            randVolatility = 0.015
                        }
                        MarketTrend.STEADY_GROWTH -> {
                            baseDrift = 0.0015
                            randVolatility = 0.004
                        }
                        MarketTrend.STEADY_BLEED -> {
                            baseDrift = -0.0015
                            randVolatility = 0.004
                        }
                        MarketTrend.THE_LOST_DECADE -> {
                            baseDrift = 0.0
                            randVolatility = 0.001
                        }
                        MarketTrend.LONG_TERM_CYCLICAL -> {
                            val cyclePhase = (trendState.durationLeftMs / monthMs).toInt() % 2
                            baseDrift = if (cyclePhase == 0) 0.008 else -0.008
                            randVolatility = 0.018
                        }
                        MarketTrend.WHIPSAW_TRAP -> {
                            baseDrift = 0.006
                            randVolatility = 0.025
                        }
                    }
                    
                    // Calculation using Random Walk with Drift algorithm
                    val safeCurrentPrice = if (stock.currentPrice <= 0.00) 0.05 else stock.currentPrice
                    val effectiveVolatility = randVolatility * volatility
                    val newPrice = com.example.ui.calculateFluctuatingPrice(
                        currentPrice = safeCurrentPrice,
                        volatility = effectiveVolatility,
                        trend = baseDrift,
                        eventShock = 0.0,
                        minPrice = 0.01
                    )
                    val newChangeAbs = newPrice - baseline
                    val newChangePct = (newChangeAbs / baseline) * 100
                    val newHistory = (stock.priceHistory + newPrice).takeLast(40)
                    
                    // Alert system hook logic
                    if (newlyTransitioned && Math.random() < 0.35) {
                        pushMarketNewsForTransition(stock.ticker, stock.name, trendState.currentTrend)
                    }

                    stock.copy(
                        currentPrice = newPrice,
                        changeAbsolute = newChangeAbs,
                        changePercentage = newChangePct,
                        priceHistory = newHistory
                    )
                }.toMutableList()

                _stockList.value = updatedList

                // Inverse Correlation: Global Stock Market vs. Gold & Precious Metals (Safe Haven Assets)
                val totalStocksCount = stockTrends.size
                val bearCount = stockTrends.values.count { 
                    it.currentTrend == MarketTrend.BEAR_MARKET || it.currentTrend == MarketTrend.STEADY_BLEED || it.currentTrend == MarketTrend.THE_LOST_DECADE
                }
                val bearRatio = if (totalStocksCount > 0) bearCount.toDouble() / totalStocksCount else 0.0

                val updatedMetals = _preciousMetalsList.value.map { metal ->
                    val metalVolatility = 0.005 * volatility // Low volatility commodity
                    val metalTrend = 0.001 // Baseline inflation drift

                    // Safe Haven Inverse Shock: Stock Crash -> Gold Surge; Stock Bull -> Gold Stagnation
                    val metalEventShock = when {
                        bearRatio > 0.45 -> 0.020 + (Math.random() * 0.015) // Flight to safety during stock panic
                        bearRatio > 0.30 -> 0.008 + (Math.random() * 0.008) // Mild market uncertainty
                        bearRatio < 0.15 -> -0.003 - (Math.random() * 0.004) // Strong bull market, gold/metals stagnate
                        else -> 0.0
                    }

                    val newPrice = com.example.ui.calculateFluctuatingPrice(
                        currentPrice = metal.currentPrice,
                        volatility = metalVolatility,
                        trend = metalTrend,
                        eventShock = metalEventShock,
                        minPrice = 0.01
                    )
                    metal.copy(currentPrice = newPrice)
                }
                _preciousMetalsList.value = updatedMetals
            }
        }
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            while(true) {
                delay(100) // update setiap 100ms
                updateProgress()
            }
        }
    }

    private fun updateProgress() {
        val currentState = _playerState.value
        val now = System.currentTimeMillis()
        
        // Process Active Upgrades for regular businesses
        var businessesChanged = false
        val newBusinesses = currentState.ownedBusinesses.map { business ->
            var updatedBusiness = business
            if (business.catalogId == "content_creator") {
                businessesChanged = true
                val addedProgress = 1f / 1200f // 120 seconds cycle, update is every 100ms (0.1s)
                var newProgress = business.contentCreatorProgress + addedProgress
                var newSubs = business.contentCreatorSubscribers
                var newCash = business.contentCreatorCash

                if (newProgress >= 1f) {
                    newProgress = 0f
                    var income = (newSubs * 0.05).toLong()
                    val multiplier = 1.0 + (business.contentCreatorEmployees * 0.05)
                    income = (income * multiplier).toLong()
                    
                    newSubs += (business.level * (business.contentCreatorEmployees + 1) * (5..15).random())
                    
                    if (business.level >= 61) {
                        if ((1..100).random() < 10) {
                            val brandBonus = (100_000..500_000).random().toLong() * (business.level / 10)
                            newCash += brandBonus
                        }
                    }
                    newCash += income
                }
                updatedBusiness = updatedBusiness.copy(
                    contentCreatorProgress = newProgress,
                    contentCreatorSubscribers = newSubs,
                    contentCreatorCash = newCash
                )
            }

            if (business.catalogId == "mid_logistics") {
                businessesChanged = true
                val updatedLogistics = LogisticsEngine.processTick(business.logisticsData, dtSeconds = 0.1f)
                updatedBusiness = updatedBusiness.copy(logisticsData = updatedLogistics)
            }

            if (business.catalogId == "upper_realestate") {
                businessesChanged = true
                val updatedApartment = ApartmentEngine.processTick(business.apartmentData, dtSeconds = 0.1f)
                updatedBusiness = updatedBusiness.copy(apartmentData = updatedApartment)
            }

            val completedUpgrades = updatedBusiness.activeUpgrades.filter { now >= it.finishTimeMs }
            if (completedUpgrades.isNotEmpty()) {
                businessesChanged = true
                var newUpgradeLevels = updatedBusiness.upgradeLevels
                var newPurchasedUpgrades = updatedBusiness.purchasedUpgrades
                var levelsGained = 0
                completedUpgrades.forEach { upgrade ->
                    newUpgradeLevels = newUpgradeLevels + (upgrade.selectedUpgradeId to upgrade.targetLevel)
                    newPurchasedUpgrades = newPurchasedUpgrades + upgrade.selectedUpgradeId
                    levelsGained += 1
                }
                updatedBusiness.copy(
                    activeUpgrades = updatedBusiness.activeUpgrades.filter { now < it.finishTimeMs },
                    upgradeLevels = newUpgradeLevels,
                    purchasedUpgrades = newPurchasedUpgrades,
                    level = updatedBusiness.level + levelsGained
                )
            } else updatedBusiness
        }

        // Process Active Upgrades for holding companies
        var holdingsChanged = false
        val newHoldings = currentState.holdingCompanies.map { holding ->
            var holdingSelfChanged = false
            val newSubs = holding.subsidiaries.map { business ->
                var updatedBusiness = business
                if (business.catalogId == "content_creator") {
                    holdingsChanged = true
                    holdingSelfChanged = true
                    val addedProgress = 1f / 1200f
                    var newProgress = business.contentCreatorProgress + addedProgress
                    var newSubsCount = business.contentCreatorSubscribers
                    var newCash = business.contentCreatorCash
                    if (newProgress >= 1f) {
                        newProgress = 0f
                        var income = (newSubsCount * 0.05).toLong()
                        val multiplier = 1.0 + (business.contentCreatorEmployees * 0.05)
                        income = (income * multiplier).toLong()
                        newSubsCount += (business.level * (business.contentCreatorEmployees + 1) * (5..15).random())
                        if (business.level >= 61) {
                            if ((1..100).random() < 10) {
                                val brandBonus = (100_000..500_000).random().toLong() * (business.level / 10)
                                newCash += brandBonus
                            }
                        }
                        newCash += income
                    }
                    updatedBusiness = updatedBusiness.copy(
                        contentCreatorProgress = newProgress,
                        contentCreatorSubscribers = newSubsCount,
                        contentCreatorCash = newCash
                    )
                }

                if (business.catalogId == "mid_logistics") {
                    holdingsChanged = true
                    holdingSelfChanged = true
                    val updatedLogistics = LogisticsEngine.processTick(business.logisticsData, dtSeconds = 0.1f)
                    updatedBusiness = updatedBusiness.copy(logisticsData = updatedLogistics)
                }

                if (business.catalogId == "upper_realestate") {
                    holdingsChanged = true
                    holdingSelfChanged = true
                    val updatedApartment = ApartmentEngine.processTick(business.apartmentData, dtSeconds = 0.1f)
                    updatedBusiness = updatedBusiness.copy(apartmentData = updatedApartment)
                }

                val completedUpgrades = updatedBusiness.activeUpgrades.filter { now >= it.finishTimeMs }
                if (completedUpgrades.isNotEmpty()) {
                    holdingsChanged = true
                    holdingSelfChanged = true
                    var newUpgradeLevels = updatedBusiness.upgradeLevels
                    var newPurchasedUpgrades = updatedBusiness.purchasedUpgrades
                    var levelsGained = 0
                    completedUpgrades.forEach { upgrade ->
                        newUpgradeLevels = newUpgradeLevels + (upgrade.selectedUpgradeId to upgrade.targetLevel)
                        newPurchasedUpgrades = newPurchasedUpgrades + upgrade.selectedUpgradeId
                        levelsGained += 1
                    }
                    updatedBusiness.copy(
                        activeUpgrades = updatedBusiness.activeUpgrades.filter { now < it.finishTimeMs },
                        upgradeLevels = newUpgradeLevels,
                        purchasedUpgrades = newPurchasedUpgrades,
                        level = updatedBusiness.level + levelsGained
                    )
                } else updatedBusiness
            }
            if (holdingSelfChanged) holding.copy(subsidiaries = newSubs) else holding
        }

        if (businessesChanged || holdingsChanged) {
            _playerState.value = currentState.copy(
                ownedBusinesses = if (businessesChanged) newBusinesses else currentState.ownedBusinesses,
                holdingCompanies = if (holdingsChanged) newHoldings else currentState.holdingCompanies
            )
        }

        val durationMs = _monthDurationSeconds.value * 1000f
        val step = 100f / durationMs // 100ms from game loop rate
        val newProgress = _monthProgress.value + step

        if (newProgress >= 1f) {
            processMonthlyTick()
        } else {
            _monthProgress.value = newProgress
        }
    }

    private fun processMonthlyTick(isOffline: Boolean = false) {
        _monthProgress.value = 0f
        val currentState = _playerState.value
        
        var newMonth = currentState.inGameMonth + 1
        var newYear = currentState.inGameYear
        if (newMonth > 12) {
            newMonth = 1
            newYear += 1
        }
        
        val tickEvent = com.example.core.engine.MonthTickEvent(
            currentMonth = newMonth,
            currentYear = newYear,
            totalMonthsElapsed = (newYear - 2026) * 12 + newMonth,
            isOfflineCatchup = isOffline,
            deltaMonths = 1
        )
        
        viewModelScope.launch {
            val (updatedState, financials) = monthlyTickRegistry.dispatchTick(tickEvent, currentState)
            
            // 1. Calculate Gross Corporate Profit from Mega Holding
            var megaHoldingMonthlyProfit = updatedState.ownedBusinesses.sumOf {
                val ct = com.example.data.getCatalogItem(it.catalogId, updatedState)
                if (ct != null) com.example.data.getBusinessStats(it, ct, updatedState).let { (rev, mnt) -> rev - mnt } else 0L
            } + updatedState.holdingCompanies.sumOf { h ->
                h.subsidiaries.sumOf { sub ->
                    val ct = com.example.data.getCatalogItem(sub.catalogId, updatedState)
                    if (ct != null) com.example.data.getBusinessStats(sub, ct, updatedState).let { (rev, mnt) -> rev - mnt } else 0L
                }
            }
            if (megaHoldingMonthlyProfit < 0) megaHoldingMonthlyProfit = 0L
            val businessValue = updatedState.calculateMegaHoldingValuation()

            // 2. Corporate Taxes & Notary Auto-Payment
            val activeCorpTaxRate = if (updatedState.taxLegalReport.isTaxHavenActive) 0.05 else 0.20
            val monthlyCorpTax = (megaHoldingMonthlyProfit * activeCorpTaxRate).toLong()
            var currentCorpCash = (updatedState.cash + financials.netIncome).coerceAtLeast(0L)
            var updatedCorporateTaxPaid = updatedState.corporateTaxPaid
            var updatedTotalTaxPaid = updatedState.totalTaxPaid
            var updatedUnpaidTaxes = updatedState.taxLegalReport.unpaidTaxes
            var updatedFrozenBusinessId = updatedState.taxLegalReport.frozenBusinessId
            val updatedLawsuits = updatedState.taxLegalReport.activeLawsuits.toMutableList()

            if (updatedState.taxLegalReport.hasNotary) {
                // Notary automatically handles taxes and retainer ($1,000/month) from corporate cash
                val notaryMonthlyFee = 1000L
                val totalTaxAndFeeDue = monthlyCorpTax + notaryMonthlyFee + updatedUnpaidTaxes
                if (currentCorpCash >= totalTaxAndFeeDue) {
                    currentCorpCash -= totalTaxAndFeeDue
                    updatedCorporateTaxPaid += (monthlyCorpTax + updatedUnpaidTaxes)
                    updatedTotalTaxPaid += (monthlyCorpTax + updatedUnpaidTaxes)
                    updatedUnpaidTaxes = 0L
                    updatedFrozenBusinessId = null
                } else {
                    val payable = currentCorpCash.coerceAtLeast(0L)
                    currentCorpCash = 0L
                    val paidTax = (payable - notaryMonthlyFee).coerceAtLeast(0L)
                    updatedCorporateTaxPaid += paidTax
                    updatedTotalTaxPaid += paidTax
                    updatedUnpaidTaxes = (totalTaxAndFeeDue - payable).coerceAtLeast(0L)
                }
            } else {
                // No notary: corporate taxes accumulate in unpaidTaxes
                updatedUnpaidTaxes += monthlyCorpTax
                if (updatedUnpaidTaxes > (megaHoldingMonthlyProfit * 3).coerceAtLeast(500_000L) && updatedState.ownedBusinesses.isNotEmpty()) {
                    if (updatedFrozenBusinessId == null) {
                        updatedFrozenBusinessId = updatedState.ownedBusinesses.first().instanceId
                    }
                }
            }

            // Tax Haven audit risk (5% monthly chance)
            if (updatedState.taxLegalReport.isTaxHavenActive && Math.random() < 0.05) {
                val auditFine = (monthlyCorpTax * 5).coerceAtLeast(100_000L)
                val auditLawsuit = com.example.data.ActiveLawsuit(
                    id = "lawsuit_audit_${System.currentTimeMillis()}",
                    title = "Pemeriksaan Khusus Ditjen Pajak (Tax Haven)",
                    description = "Kantor Pajak menemukan ketidakwajaran pelaporan dana offshore. Terbit Surat Ketetapan Pajak Kurang Bayar (SKPKB).",
                    scaleFactor = auditFine
                )
                if (updatedLawsuits.size < 4) {
                    updatedLawsuits.add(auditLawsuit)
                }
            }

            // Lawsuit random event (3% monthly chance if company has substantial businesses)
            if (updatedState.ownedBusinesses.size + updatedState.holdingCompanies.size > 0 && updatedLawsuits.size < 3 && Math.random() < 0.03) {
                updatedLawsuits.add(com.example.data.generateRandomLawsuit(businessValue))
            }

            // 2b. Private Equity Debt Service (Cicilan Hutang PE dari Kas Utama Perusahaan)
            val peDebtTick = com.example.privateequity.engine.PrivateEquityEngine.processMonthlyDebtTick(
                state = updatedState,
                availableCorpCash = currentCorpCash
            )
            currentCorpCash = (currentCorpCash - peDebtTick.totalPaid).coerceAtLeast(0L)
            val updatedActiveInvestorsLoans = peDebtTick.updatedLoans

            // 3. Executive Remuneration & Family Office Private Wealth Inflow
            val grossSalary = (megaHoldingMonthlyProfit * (updatedState.currentCeoSalaryPercent / 100.0)).toLong()
            if (grossSalary > 0 && currentCorpCash >= grossSalary) {
                currentCorpCash -= grossSalary
            }

            val monthlyPerks = (businessValue * 0.000005).toLong()
            val grossPersonalIncome = grossSalary + monthlyPerks
            val pph21Tax = calculateProgressiveTax(grossPersonalIncome, updatedState.privateTaxServiceLevel)

            var privateStockDividends = 0L
            updatedState.privateStockPortfolio.forEach { stock ->
                if (stock.shares > 0 && stock.averagePrice > 0.0) {
                    val div = (stock.shares * stock.averagePrice * 0.003).toLong()
                    privateStockDividends += div
                }
            }

            // Corporate Dividends (every 6 months)
            var playerDividendsReceived = 0L
            if (newMonth % 6 == 0 && updatedState.currentDividendPercent > 0.0) {
                val laba6Bln = updatedState.financialHistory.takeLast(6).sumOf { it.netIncome }
                if (laba6Bln > 0) {
                    val divPool = (laba6Bln * (updatedState.currentDividendPercent / 100.0)).toLong()
                    val playerDivGross = (divPool * (updatedState.companyOwnershipPercent / 100.0)).toLong()
                    if (playerDivGross > 0 && currentCorpCash >= playerDivGross) {
                        currentCorpCash -= playerDivGross
                        val divTaxRate = if (updatedState.privateTaxServiceLevel == 2) 0.05 else 0.10
                        val divTax = (playerDivGross * divTaxRate).toLong()
                        playerDividendsReceived = playerDivGross - divTax
                        updatedTotalTaxPaid += divTax
                        updatedCorporateTaxPaid += divTax
                    }
                }
            }

            // Annual Bonus / Tantiem (every 12 months)
            var playerTantiemReceived = 0L
            if (newMonth == 12 && updatedState.currentTantiemPercent > 0.0) {
                val laba12Bln = updatedState.financialHistory.takeLast(12).sumOf { it.netIncome }
                if (laba12Bln > 0) {
                    val tantiemGross = (laba12Bln * (updatedState.currentTantiemPercent / 100.0)).toLong()
                    if (tantiemGross > 0 && currentCorpCash >= tantiemGross) {
                        currentCorpCash -= tantiemGross
                        val tantiemTaxRate = if (updatedState.privateTaxServiceLevel == 2) 0.05 else 0.15
                        val tantiemTax = (tantiemGross * tantiemTaxRate).toLong()
                        playerTantiemReceived = tantiemGross - tantiemTax
                        updatedTotalTaxPaid += tantiemTax
                        updatedCorporateTaxPaid += tantiemTax
                    }
                }
            }

            // 4. Private Outflows (Lifestyle, Upkeep, Debt Interest)
            val lifestyleCost = updatedState.monthlyLifestyleCost
            val rentedHousingCost = updatedState.allSubscriptions.filter { it.isActive && it.tabCategory == "housing" }.sumOf { it.price }
            val ownedHousingMaint = updatedState.ownedHouses.sumOf { (it.purchasedPrice * 0.001).toLong() }
            val vehicleUpkeep = updatedState.ownedCollections.filter { owned ->
                val cat = _collectionList.value.find { c -> c.id == owned.itemId }?.categoryId
                listOf("cars", "motorcycles", "yachts", "airplanes").contains(cat)
            }.sumOf { (it.purchasedPrice * 0.001).toLong() }
            val totalUpkeep = rentedHousingCost + ownedHousingMaint + vehicleUpkeep
            val lombardInterest = (updatedState.personalDebt * 0.005).toLong()

            val totalPrivateInflow = grossPersonalIncome + privateStockDividends + playerDividendsReceived + playerTantiemReceived
            val totalPrivateOutflow = pph21Tax + lifestyleCost + totalUpkeep + lombardInterest

            val netPrivateDelta = totalPrivateInflow - totalPrivateOutflow
            val newPrivateBalance = (updatedState.privateBalance + netPrivateDelta).coerceAtLeast(0L)

            val nextPersonalTaxPaid = updatedState.personalTaxPaid + pph21Tax
            val nextTotalTaxPaid = updatedTotalTaxPaid + pph21Tax

            // 5. Private Ledger History logging
            var ledgerState = updatedState
            if (grossPersonalIncome > 0L) {
                ledgerState = logToPrivateLedger(ledgerState, "Gaji & Tunjangan CEO kotor", grossPersonalIncome, true)
            }
            if (pph21Tax > 0L) {
                ledgerState = logToPrivateLedger(ledgerState, "Pajak PPh 21 Progresif", pph21Tax, false)
            }
            if (privateStockDividends > 0L) {
                ledgerState = logToPrivateLedger(ledgerState, "Dividen Saham Pribadi (Passive Income)", privateStockDividends, true)
            }
            if (playerDividendsReceived > 0L) {
                ledgerState = logToPrivateLedger(ledgerState, "Dividen Korporasi Holding", playerDividendsReceived, true)
            }
            if (playerTantiemReceived > 0L) {
                ledgerState = logToPrivateLedger(ledgerState, "Bonus Kinerja Tahunan (Tantiem)", playerTantiemReceived, true)
            }
            if (totalUpkeep > 0L) {
                ledgerState = logToPrivateLedger(ledgerState, "Lifestyle Upkeep & Perawatan Aset", totalUpkeep, false)
            }
            if (lifestyleCost > 0L) {
                ledgerState = logToPrivateLedger(ledgerState, "Tagihan Gaya Hidup & Keamanan", lifestyleCost, false)
            }
            if (lombardInterest > 0L) {
                ledgerState = logToPrivateLedger(ledgerState, "Bunga Pinjaman Agunan Lombard", lombardInterest, false)
            }

            // 6. Board Approvals Countdown
            var nextCeoSalaryPercent = updatedState.currentCeoSalaryPercent
            var nextPendingCeoSalary = updatedState.pendingCeoSalaryPercent
            var nextBoardApprovalMonths = updatedState.boardApprovalMonthsLeft
            var boardMsg = updatedState.boardReplyMessage

            if (nextPendingCeoSalary != null) {
                if (nextBoardApprovalMonths > 1) {
                    nextBoardApprovalMonths -= 1
                } else {
                    nextCeoSalaryPercent = nextPendingCeoSalary
                    nextPendingCeoSalary = null
                    nextBoardApprovalMonths = 0
                    boardMsg = "Dewan Direksi & Komisaris menyetujui usulan penyesuaian Gaji CEO menjadi ${String.format(java.util.Locale.US, "%.1f", nextCeoSalaryPercent)}%!"
                }
            }

            var nextDividendPercent = updatedState.currentDividendPercent
            var nextPendingDividend = updatedState.pendingDividendPercent
            var nextDividendApprovalMonths = updatedState.dividendApprovalMonthsLeft

            if (nextPendingDividend != null) {
                if (nextDividendApprovalMonths > 1) {
                    nextDividendApprovalMonths -= 1
                } else {
                    nextDividendPercent = nextPendingDividend
                    nextPendingDividend = null
                    nextDividendApprovalMonths = 0
                    boardMsg = "RUPS menyetujui kebijakan alokasi dividen sebesar ${String.format(java.util.Locale.US, "%.1f", nextDividendPercent)}%!"
                }
            }

            var nextTantiemPercent = updatedState.currentTantiemPercent
            var nextPendingTantiem = updatedState.pendingTantiemPercent
            var nextTantiemApprovalMonths = updatedState.tantiemApprovalMonthsLeft

            if (nextPendingTantiem != null) {
                if (nextTantiemApprovalMonths > 1) {
                    nextTantiemApprovalMonths -= 1
                } else {
                    nextTantiemPercent = nextPendingTantiem
                    nextPendingTantiem = null
                    nextTantiemApprovalMonths = 0
                    boardMsg = "Komite Remunerasi menyetujui usulan bonus tantiem tahunan sebesar ${String.format(java.util.Locale.US, "%.1f", nextTantiemPercent)}%!"
                }
            }

            // 7. Append Financial History
            val newFinancialRecord = com.example.data.MonthlyFinancialRecord(
                monthTick = newMonth + (newYear - 2026) * 12,
                totalRevenue = financials.totalRevenue,
                totalExpense = financials.totalExpenses,
                netIncome = financials.netIncome
            )
            val newFinancialHistory = (updatedState.financialHistory + newFinancialRecord).takeLast(60)

            val updatedPersonalDeposits = ledgerState.timeDeposits.map { 
                it.copy(monthsRemaining = (it.monthsRemaining - 1).coerceAtLeast(0)) 
            }
            val updatedHoldingDeposits = ledgerState.megaHolding.holdingTimeDeposits.map { 
                it.copy(monthsRemaining = (it.monthsRemaining - 1).coerceAtLeast(0)) 
            }

            // Monthly Treasury Tick: Bond Coupons & RDPT NAV appreciation
            val treasuryTick = holdingInvestmentRepository.processMonthlyTick(
                bonds = ledgerState.megaHolding.holdingBonds,
                currentNav = ledgerState.megaHolding.holdingRdptNav
            )

            // Sync core timeline and financials
            val finalState = ledgerState.copy(
                inGameMonth = newMonth,
                inGameYear = newYear,
                cash = currentCorpCash + treasuryTick.totalCouponCollected,
                timeDeposits = updatedPersonalDeposits,
                megaHolding = ledgerState.megaHolding.copy(
                    holdingTimeDeposits = updatedHoldingDeposits,
                    holdingBonds = treasuryTick.updatedBonds,
                    holdingRdptNav = treasuryTick.updatedNav,
                    totalBondCouponIncomeEarned = ledgerState.megaHolding.totalBondCouponIncomeEarned + treasuryTick.totalCouponCollected
                ),
                lastMonthIncome = financials.totalRevenue,
                lastMonthExpenses = financials.totalExpenses,
                lastMonthNetProfit = financials.netIncome,
                lastSavedTimeMs = System.currentTimeMillis(),
                
                taxLegalReport = updatedState.taxLegalReport.copy(
                    unpaidTaxes = updatedUnpaidTaxes,
                    frozenBusinessId = updatedFrozenBusinessId,
                    activeLawsuits = updatedLawsuits
                ),
                corporateTaxPaid = updatedCorporateTaxPaid,
                personalTaxPaid = nextPersonalTaxPaid,
                totalTaxPaid = nextTotalTaxPaid,
                
                privateBalance = newPrivateBalance,
                currentCeoSalaryPercent = nextCeoSalaryPercent,
                pendingCeoSalaryPercent = nextPendingCeoSalary,
                boardApprovalMonthsLeft = nextBoardApprovalMonths,
                currentDividendPercent = nextDividendPercent,
                pendingDividendPercent = nextPendingDividend,
                dividendApprovalMonthsLeft = nextDividendApprovalMonths,
                currentTantiemPercent = nextTantiemPercent,
                pendingTantiemPercent = nextPendingTantiem,
                tantiemApprovalMonthsLeft = nextTantiemApprovalMonths,
                boardReplyMessage = boardMsg,
                
                activeInvestorsLoans = updatedActiveInvestorsLoans,
                financialHistory = newFinancialHistory
            )
            
            _playerState.value = syncTvValuation(finalState)
            if (!isOffline) saveState(_playerState.value)

            // Corporate M&A tick progression (stalled expiry & unsolicited LOI generation)
            maRepository.processMonthlyTick(_playerState.value, newMonth, newYear)
        }
    }

    
    fun addProperty(name: String, location: String, price: Long, rental: Long, imageUrl: String = "") {
        val finalUrl = if (imageUrl.isBlank()) "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=400&q=80" else imageUrl
        
        val newPropId = "prop_custom_${System.currentTimeMillis()}"
        val newProp = com.example.data.PropertyItem(newPropId, name, location, "Custom", price, rental)
        val currentMarket = _realEstateMarket.value.toMutableList()
        currentMarket.add(newProp)
        _realEstateMarket.value = currentMarket
        saveProperties(currentMarket)
        
        val newHs = com.example.data.HousingItem(newPropId, name, location, "Custom Property", price, rental, imageUrl = finalUrl)
        val currentHs = _housingList.value.toMutableList()
        currentHs.add(newHs)
        _housingList.value = currentHs
        saveHousing(currentHs)
    }

    fun addCollectionItem(categoryId: String, name: String, desc: String, price: Long, imageUrl: String = "", releaseYear: Int? = null) {
        val newId = "col_custom_${System.currentTimeMillis()}"
        val newItem = com.example.data.CollectionItem(newId, categoryId, name, desc, price, imageUrl, releaseYear)
        val currentList = _collectionList.value.toMutableList()
        currentList.add(newItem)
        _collectionList.value = currentList
        saveCollections(currentList)
    }

    fun removeCollectionItem(itemId: String) {
        val currentList = _collectionList.value.toMutableList()
        currentList.removeAll { it.id == itemId }
        _collectionList.value = currentList
        saveCollections(currentList)
    }

    fun removeProperty(propertyId: String) {
        val currentMarket = _realEstateMarket.value.toMutableList()
        currentMarket.removeAll { it.id == propertyId }
        _realEstateMarket.value = currentMarket
        saveProperties(currentMarket)
    }

    fun updateCollectionImageUrl(itemId: String, newUrl: String) {
        val currentList = _collectionList.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == itemId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(imageUrl = newUrl)
            _collectionList.value = currentList
            saveCollections(currentList)
        }
    }

    fun buyMetal(metalId: String, amount: Double) {
        val metal = _preciousMetalsList.value.find { it.id == metalId } ?: return
        val totalCost = (metal.currentPrice * amount).toLong()
        val currentState = _playerState.value
        
        if (currentState.privateBalance >= totalCost) {
            val currentOwned = currentState.ownedMetals[metalId] ?: 0.0
            val currentAvg = currentState.ownedMetalsAveragePrices[metalId] ?: 0.0
            val newTotalAmount = currentOwned + amount
            val newAvg = if (newTotalAmount > 0) ((currentAvg * currentOwned) + (metal.currentPrice * amount)) / newTotalAmount else metal.currentPrice

            val updatedMetals = currentState.ownedMetals.toMutableMap()
            updatedMetals[metalId] = newTotalAmount
            val updatedAvgs = currentState.ownedMetalsAveragePrices.toMutableMap()
            updatedAvgs[metalId] = newAvg

            val nextState = currentState.copy(
                privateBalance = currentState.privateBalance - totalCost,
                ownedMetals = updatedMetals,
                ownedMetalsAveragePrices = updatedAvgs
            )
            val loggedState = logToPrivateLedger(nextState, "Beli Logam Mulia ($metalId)", totalCost, false)
            _playerState.value = loggedState
            saveState(loggedState)
        }
    }

    fun sellMetal(metalId: String, amount: Double) {
        val metal = _preciousMetalsList.value.find { it.id == metalId } ?: return
        val currentState = _playerState.value
        val currentOwned = currentState.ownedMetals[metalId] ?: 0.0
        
        if (currentOwned >= amount) {
            val totalRevenue = (metal.currentPrice * amount).toLong()
            val updatedMetals = currentState.ownedMetals.toMutableMap()
            updatedMetals[metalId] = currentOwned - amount
            val updatedAvgs = currentState.ownedMetalsAveragePrices.toMutableMap()
            if (updatedMetals[metalId]!! <= 0.0001) {
                updatedMetals.remove(metalId) // Cleanup floating point issues
                updatedAvgs.remove(metalId)
            }

            val nextState = currentState.copy(
                privateBalance = currentState.privateBalance + totalRevenue,
                ownedMetals = updatedMetals,
                ownedMetalsAveragePrices = updatedAvgs
            )
            val loggedState = logToPrivateLedger(nextState, "Jual Logam Mulia ($metalId)", totalRevenue, true)
            _playerState.value = loggedState
            saveState(loggedState)
        }
    }

    fun openTimeDeposit(principal: Long, durationMonths: Int) {
        val currentState = _playerState.value
        if (currentState.privateBalance >= principal && principal > 0) {
            val interestRate = when (durationMonths) {
                3 -> 0.05 // 5% total
                6 -> 0.12 // 12% total
                12 -> 0.30 // 30% total
                else -> 0.0
            }
            
            val newDeposit = com.example.data.TimeDeposit(
                id = java.util.UUID.randomUUID().toString(),
                principal = principal,
                durationMonths = durationMonths,
                monthsRemaining = durationMonths,
                interestRate = interestRate
            )
            
            val updatedDeposits = currentState.timeDeposits.toMutableList()
            updatedDeposits.add(newDeposit)
            
            val nextState = currentState.copy(
                privateBalance = currentState.privateBalance - principal,
                timeDeposits = updatedDeposits
            )
            val loggedState = logToPrivateLedger(nextState, "Buka Deposito Berjangka ($durationMonths Bln)", principal, false)
            _playerState.value = loggedState
            saveState(loggedState)
        }
    }

    fun withdrawTimeDeposit(depositId: String, isEarly: Boolean) {
        val currentState = _playerState.value
        val deposit = currentState.timeDeposits.find { it.id == depositId } ?: return
        
        val returnAmount = if (isEarly) {
            // Apply 5% penalty on principal
            (deposit.principal * 0.95).toLong()
        } else {
            // Full principal + interest
            (deposit.principal + (deposit.principal * deposit.interestRate)).toLong()
        }
        
        val updatedDeposits = currentState.timeDeposits.filter { it.id != depositId }
        val label = if (isEarly) "Pencairan Deposito Lebih Awal" else "Pencairan Deposito Jatuh Tempo"
        
        val nextState = currentState.copy(
            privateBalance = currentState.privateBalance + returnAmount,
            timeDeposits = updatedDeposits
        )
        val loggedState = logToPrivateLedger(nextState, label, returnAmount, true)
        _playerState.value = loggedState
        saveState(loggedState)
    }

    fun buyHoldingMetal(metalId: String, amount: Double) {
        val metal = _preciousMetalsList.value.find { it.id == metalId } ?: return
        val totalCost = (metal.currentPrice * amount).toLong()
        val currentState = _playerState.value
        
        if (currentState.cash >= totalCost && totalCost > 0) {
            val currentOwned = currentState.megaHolding.holdingMetals[metalId] ?: 0.0
            val currentAvg = currentState.megaHolding.holdingMetalsAveragePrices[metalId] ?: 0.0
            val newTotalAmount = currentOwned + amount
            val newAvg = if (newTotalAmount > 0) ((currentAvg * currentOwned) + (metal.currentPrice * amount)) / newTotalAmount else metal.currentPrice

            val updatedMetals = currentState.megaHolding.holdingMetals.toMutableMap()
            updatedMetals[metalId] = newTotalAmount
            val updatedAvgs = currentState.megaHolding.holdingMetalsAveragePrices.toMutableMap()
            updatedAvgs[metalId] = newAvg

            val nextState = currentState.copy(
                cash = currentState.cash - totalCost,
                megaHolding = currentState.megaHolding.copy(
                    holdingMetals = updatedMetals,
                    holdingMetalsAveragePrices = updatedAvgs
                )
            )
            _playerState.value = nextState
            saveState(nextState)
        }
    }

    fun sellHoldingMetal(metalId: String, amount: Double) {
        val metal = _preciousMetalsList.value.find { it.id == metalId } ?: return
        val currentState = _playerState.value
        val currentOwned = currentState.megaHolding.holdingMetals[metalId] ?: 0.0
        
        if (currentOwned >= amount && amount > 0) {
            val totalRevenue = (metal.currentPrice * amount).toLong()
            val updatedMetals = currentState.megaHolding.holdingMetals.toMutableMap()
            updatedMetals[metalId] = currentOwned - amount
            val updatedAvgs = currentState.megaHolding.holdingMetalsAveragePrices.toMutableMap()
            if (updatedMetals[metalId]!! <= 0.0001) {
                updatedMetals.remove(metalId)
                updatedAvgs.remove(metalId)
            }

            val nextState = currentState.copy(
                cash = currentState.cash + totalRevenue,
                megaHolding = currentState.megaHolding.copy(
                    holdingMetals = updatedMetals,
                    holdingMetalsAveragePrices = updatedAvgs
                )
            )
            _playerState.value = nextState
            saveState(nextState)
        }
    }

    fun openHoldingTimeDeposit(principal: Long, durationMonths: Int) {
        val currentState = _playerState.value
        if (currentState.cash >= principal && principal > 0) {
            val interestRate = when (durationMonths) {
                3 -> 0.05
                6 -> 0.12
                12 -> 0.30
                else -> 0.0
            }
            
            val newDeposit = com.example.data.TimeDeposit(
                id = java.util.UUID.randomUUID().toString(),
                principal = principal,
                durationMonths = durationMonths,
                monthsRemaining = durationMonths,
                interestRate = interestRate
            )
            
            val updatedDeposits = currentState.megaHolding.holdingTimeDeposits.toMutableList()
            updatedDeposits.add(newDeposit)
            
            val nextState = currentState.copy(
                cash = currentState.cash - principal,
                megaHolding = currentState.megaHolding.copy(
                    holdingTimeDeposits = updatedDeposits
                )
            )
            _playerState.value = nextState
            saveState(nextState)
        }
    }

    fun withdrawHoldingTimeDeposit(depositId: String, isEarly: Boolean) {
        val currentState = _playerState.value
        val deposit = currentState.megaHolding.holdingTimeDeposits.find { it.id == depositId } ?: return
        
        val returnAmount = if (isEarly) {
            (deposit.principal * 0.95).toLong()
        } else {
            (deposit.principal + (deposit.principal * deposit.interestRate)).toLong()
        }
        
        val updatedDeposits = currentState.megaHolding.holdingTimeDeposits.filter { it.id != depositId }
        
        val nextState = currentState.copy(
            cash = currentState.cash + returnAmount,
            megaHolding = currentState.megaHolding.copy(
                holdingTimeDeposits = updatedDeposits
            )
        )
        _playerState.value = nextState
        saveState(nextState)
    }

    fun buyHoldingBond(bond: com.example.data.treasury.CorporateBond, amount: Long): Pair<Boolean, String?> {
        val currentState = _playerState.value
        val result = holdingInvestmentRepository.purchaseBond(
            bond = bond,
            investmentAmount = amount,
            currentMonth = currentState.inGameMonth,
            currentYear = currentState.inGameYear,
            currentHoldings = currentState.megaHolding.holdingBonds,
            availableCash = currentState.cash
        )
        return result.fold(
            onSuccess = { (updatedBonds, cost) ->
                val nextState = currentState.copy(
                    cash = currentState.cash - cost,
                    megaHolding = currentState.megaHolding.copy(holdingBonds = updatedBonds)
                )
                _playerState.value = nextState
                saveState(nextState)
                Pair(true, null)
            },
            onFailure = { error ->
                Pair(false, error.message ?: "Gagal membeli obligasi.")
            }
        )
    }

    fun liquidateHoldingBondEarly(holdingId: String): Pair<Boolean, String?> {
        val currentState = _playerState.value
        val result = holdingInvestmentRepository.liquidateBondEarly(
            holdingId = holdingId,
            currentHoldings = currentState.megaHolding.holdingBonds
        )
        return result.fold(
            onSuccess = { (updatedBonds, cashGained) ->
                val nextState = currentState.copy(
                    cash = currentState.cash + cashGained,
                    megaHolding = currentState.megaHolding.copy(holdingBonds = updatedBonds)
                )
                _playerState.value = nextState
                saveState(nextState)
                Pair(true, null)
            },
            onFailure = { error ->
                Pair(false, error.message ?: "Gagal melikuidasi obligasi.")
            }
        )
    }

    fun claimMaturedHoldingBond(holdingId: String): Pair<Boolean, String?> {
        val currentState = _playerState.value
        val result = holdingInvestmentRepository.claimMaturedBond(
            holdingId = holdingId,
            currentHoldings = currentState.megaHolding.holdingBonds
        )
        return result.fold(
            onSuccess = { (updatedBonds, cashGained) ->
                val nextState = currentState.copy(
                    cash = currentState.cash + cashGained,
                    megaHolding = currentState.megaHolding.copy(holdingBonds = updatedBonds)
                )
                _playerState.value = nextState
                saveState(nextState)
                Pair(true, null)
            },
            onFailure = { error ->
                Pair(false, error.message ?: "Gagal mengklaim pokok obligasi.")
            }
        )
    }

    fun topUpHoldingRdpt(amount: Long): Pair<Boolean, String?> {
        val currentState = _playerState.value
        val fund = holdingInvestmentRepository.fundCatalog.value
        val currentNav = currentState.megaHolding.holdingRdptNav
        val result = holdingInvestmentRepository.topUpRdpt(
            fund = fund,
            currentNav = currentNav,
            depositAmount = amount,
            currentHolding = currentState.megaHolding.holdingRdpt,
            availableCash = currentState.cash
        )
        return result.fold(
            onSuccess = { (updatedHolding, cost) ->
                val nextState = currentState.copy(
                    cash = currentState.cash - cost,
                    megaHolding = currentState.megaHolding.copy(holdingRdpt = updatedHolding)
                )
                _playerState.value = nextState
                saveState(nextState)
                Pair(true, null)
            },
            onFailure = { error ->
                Pair(false, error.message ?: "Gagal top up RDPT.")
            }
        )
    }

    fun redeemHoldingRdpt(units: Double): Pair<Boolean, String?> {
        val currentState = _playerState.value
        val currentNav = currentState.megaHolding.holdingRdptNav
        val result = holdingInvestmentRepository.redeemRdpt(
            unitsToRedeem = units,
            currentNav = currentNav,
            currentHolding = currentState.megaHolding.holdingRdpt
        )
        return result.fold(
            onSuccess = { (updatedHolding, cashGained) ->
                val nextState = currentState.copy(
                    cash = currentState.cash + cashGained,
                    megaHolding = currentState.megaHolding.copy(holdingRdpt = updatedHolding)
                )
                _playerState.value = nextState
                saveState(nextState)
                Pair(true, null)
            },
            onFailure = { error ->
                Pair(false, error.message ?: "Gagal mencairkan unit RDPT.")
            }
        )
    }

    fun getBusinessSlotUpgradePrice(): Long {
        val extraSlots = _playerState.value.maxBusinessSlots - 11
        var price = 1_000_000.0 // 1 Million
        for (i in 0 until extraSlots) {
            price *= 1.5
        }
        return price.toLong()
    }

    fun addCash(amount: Long) {
        val currentState = _playerState.value
        _playerState.value = currentState.copy(cash = currentState.cash + amount)
    }

    fun getContentCreatorBusiness(instanceId: String? = null): com.example.data.OwnedBusiness? {
        val state = _playerState.value
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

    fun updateContentCreatorBusiness(targetInstanceId: String? = null, transform: (com.example.data.OwnedBusiness) -> com.example.data.OwnedBusiness): Boolean {
        val currentState = _playerState.value
        val target = getContentCreatorBusiness(targetInstanceId) ?: return false
        val targetId = target.instanceId
        
        var foundInOwned = false
        val newOwned = currentState.ownedBusinesses.map { owned ->
            if (owned.instanceId == targetId) {
                foundInOwned = true
                transform(owned)
            } else {
                owned
            }
        }
        
        var foundInHolding = false
        val newHoldings = currentState.holdingCompanies.map { holding ->
            var subUpdated = false
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.instanceId == targetId) {
                    subUpdated = true
                    foundInHolding = true
                    transform(sub)
                } else {
                    sub
                }
            }
            if (subUpdated) holding.copy(subsidiaries = newSubs) else holding
        }
        
        if (!foundInOwned && !foundInHolding) return false
        
        val nextState = currentState.copy(
            ownedBusinesses = newOwned,
            holdingCompanies = newHoldings
        )
        _playerState.value = nextState
        saveState(nextState)
        return true
    }

    fun syncContentCreator(level: Int, income: Long, targetInstanceId: String? = null) {
        updateContentCreatorBusiness(targetInstanceId) { owned ->
            owned.copy(level = level, customRevenue = income)
        }
    }

    fun injectCashToContentCreator(amount: Long, targetInstanceId: String? = null): Boolean {
        val currentState = _playerState.value
        if (currentState.cash >= amount && amount > 0) {
            val success = updateContentCreatorBusiness(targetInstanceId) { owned ->
                owned.copy(contentCreatorCash = owned.contentCreatorCash + amount)
            }
            if (success) {
                val nextState = _playerState.value.copy(cash = _playerState.value.cash - amount)
                _playerState.value = nextState
                saveState(nextState)
                return true
            }
        }
        return false
    }

    fun withdrawCashFromContentCreator(amount: Long, targetInstanceId: String? = null): Boolean {
        val cc = getContentCreatorBusiness(targetInstanceId) ?: return false
        if (cc.contentCreatorCash >= amount && amount > 0) {
            val success = updateContentCreatorBusiness(targetInstanceId) { owned ->
                owned.copy(contentCreatorCash = owned.contentCreatorCash - amount)
            }
            if (success) {
                val nextState = _playerState.value.copy(cash = _playerState.value.cash + amount)
                _playerState.value = nextState
                saveState(nextState)
                return true
            }
        }
        return false
    }

    fun deleteContentCreatorBusiness(targetInstanceId: String? = null) {
        val currentState = _playerState.value
        val target = getContentCreatorBusiness(targetInstanceId) ?: return
        val targetId = target.instanceId
        val refundedCash = target.contentCreatorCash
        
        val newOwned = currentState.ownedBusinesses.filterNot { it.instanceId == targetId }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            holding.copy(subsidiaries = holding.subsidiaries.filterNot { it.instanceId == targetId })
        }
        val nextState = currentState.copy(
            cash = currentState.cash + refundedCash,
            ownedBusinesses = newOwned,
            holdingCompanies = newHoldings
        )
        _playerState.value = nextState
        saveState(nextState)
    }

    fun levelUpContentCreator(targetInstanceId: String? = null): Boolean {
        val cc = getContentCreatorBusiness(targetInstanceId) ?: return false
        if (cc.level >= 100) return false
        if (cc.level == 40 && !cc.contentCreatorOfficeUnlocked) return false

        val cost = (500.0 * Math.pow(1.18, (cc.level - 1).toDouble())).toLong()
        if (cc.contentCreatorCash >= cost) {
            val newLevel = cc.level + 1
            val newSubs = cc.contentCreatorSubscribers + (100.0 * Math.pow(1.16, newLevel.toDouble())).toLong()
            return updateContentCreatorBusiness(targetInstanceId) { owned ->
                owned.copy(
                    level = newLevel,
                    contentCreatorSubscribers = newSubs,
                    contentCreatorCash = owned.contentCreatorCash - cost
                )
            }
        }
        return false
    }

    fun hireEmployeeContentCreator(targetInstanceId: String? = null): Boolean {
        val cc = getContentCreatorBusiness(targetInstanceId) ?: return false
        val maxEmp = when {
            cc.level >= 81 -> 100
            cc.level >= 61 -> 50
            cc.level >= 41 -> 20
            cc.level >= 21 -> 5
            else -> 0
        }
        if (cc.contentCreatorEmployees >= maxEmp) return false

        val cost = (1500.0 * Math.pow(1.2, cc.contentCreatorEmployees.toDouble())).toLong()
        if (cc.contentCreatorCash >= cost) {
            return updateContentCreatorBusiness(targetInstanceId) { owned ->
                owned.copy(
                    contentCreatorEmployees = owned.contentCreatorEmployees + 1,
                    contentCreatorCash = owned.contentCreatorCash - cost
                )
            }
        }
        return false
    }

    fun unlockOfficeContentCreator(targetInstanceId: String? = null): Boolean {
        val cc = getContentCreatorBusiness(targetInstanceId) ?: return false
        val cost = 5_000_000L
        if (cc.level == 40 && !cc.contentCreatorOfficeUnlocked && cc.contentCreatorCash >= cost) {
            return updateContentCreatorBusiness(targetInstanceId) { owned ->
                owned.copy(
                    contentCreatorOfficeUnlocked = true,
                    contentCreatorCash = owned.contentCreatorCash - cost
                )
            }
        }
        return false
    }

    fun acceptContentCreatorBrandDealOffer(offer: BrandDealOffer, targetInstanceId: String? = null): Boolean {
        val cc = getContentCreatorBusiness(targetInstanceId) ?: return false
        if (offer.dealType == BrandDealType.CONTRACT && cc.contentCreatorContracts.size >= 3) {
            return false // Slot penuh
        }
        return updateContentCreatorBusiness(targetInstanceId) { owned ->
            when (offer.dealType) {
                BrandDealType.ONE_OFF -> {
                    owned.copy(
                        contentCreatorCash = owned.contentCreatorCash + offer.contractValue
                    )
                }
                BrandDealType.CONTRACT -> {
                    val currentList = owned.contentCreatorContracts
                    val newContract = ActiveCreatorContract(
                        id = offer.id,
                        brandName = offer.brandName,
                        tierLevel = offer.tierLevel,
                        categoryTag = offer.categoryTag,
                        monthlyPayout = offer.monthlyPayout,
                        totalMonths = offer.durationMonths,
                        remainingMonths = offer.durationMonths,
                        totalPaidSoFar = 0L
                    )
                    owned.copy(
                        contentCreatorContracts = currentList + newContract
                    )
                }
            }
        }
    }

    fun terminateContentCreatorContract(contractId: String, penaltyFee: Long = 0L, targetInstanceId: String? = null): Boolean {
        val cc = getContentCreatorBusiness(targetInstanceId) ?: return false
        return updateContentCreatorBusiness(targetInstanceId) { owned ->
            owned.copy(
                contentCreatorCash = (owned.contentCreatorCash - penaltyFee).coerceAtLeast(0L),
                contentCreatorContracts = owned.contentCreatorContracts.filterNot { it.id == contractId }
            )
        }
    }

    fun acceptContentCreatorBrandDeal(amount: Long, subsReward: Long = 0, targetInstanceId: String? = null): Boolean {
        return updateContentCreatorBusiness(targetInstanceId) { owned ->
            owned.copy(
                contentCreatorCash = owned.contentCreatorCash + amount,
                contentCreatorSubscribers = owned.contentCreatorSubscribers + subsReward
            )
        }
    }

    fun rejectContentCreatorBrandDeal(subsReward: Long, targetInstanceId: String? = null): Boolean {
        if (subsReward <= 0) return true
        return updateContentCreatorBusiness(targetInstanceId) { owned ->
            owned.copy(
                contentCreatorSubscribers = owned.contentCreatorSubscribers + subsReward
            )
        }
    }

    // ==========================================
    // CONTENT CREATOR: PRODUKSI KARYA ORIGINAL & IP ROUTING
    // ==========================================

    fun produceContentWork(
        title: String,
        type: ContentType,
        budget: Long,
        targetStudioInstanceId: String? = null,
        fundingScheme: CoProductionFundingScheme = CoProductionFundingScheme.FULL_CREATOR,
        targetCreatorInstanceId: String? = null,
        creativeFocus: com.example.data.CreativeFocus? = null
    ): ContentWork? {
        val (nextState, work) = com.example.businessunit.contentcreator.engine.ContentCreatorActionHandler.produceContentWork(
            state = _playerState.value,
            title = title,
            type = type,
            budget = budget,
            targetStudioInstanceId = targetStudioInstanceId,
            fundingScheme = fundingScheme,
            targetCreatorInstanceId = targetCreatorInstanceId,
            creativeFocus = creativeFocus
        )
        if (nextState != null && work != null) {
            _playerState.value = nextState
            saveState(nextState)
            return work
        }
        return null
    }

    fun routeContentWorkToFilmStudio(
        title: String,
        type: ContentType,
        budget: Long,
        promoBudget: Long = 0L,
        genres: List<String> = emptyList(),
        isGlobal: Boolean = true,
        schedMonth: Int? = null,
        schedYear: Int? = null,
        filmFormat: String = "Feature Film",
        productionFocus: String = "REGULER",
        targetStudioInstanceId: String? = null,
        fundingScheme: CoProductionFundingScheme = CoProductionFundingScheme.FULL_CREATOR,
        targetCreatorInstanceId: String? = null
    ): Boolean {
        val currentState = _playerState.value
        val cc = getContentCreatorBusiness(targetCreatorInstanceId) ?: return false

        val filmStudio = if (targetStudioInstanceId != null) {
            currentState.ownedBusinesses.firstOrNull { it?.instanceId == targetStudioInstanceId }
                ?: currentState.holdingCompanies.flatMap { it.subsidiaries }.firstOrNull { it?.instanceId == targetStudioInstanceId }
        } else {
            currentState.ownedBusinesses.firstOrNull { it?.catalogId == "media_production" }
                ?: currentState.holdingCompanies.flatMap { it.subsidiaries }.firstOrNull { it?.catalogId == "media_production" }
        } ?: return false

        val totalCost = budget + promoBudget

        // Determine funding allocation
        val creatorCut = (totalCost * fundingScheme.creatorRatio).toLong()
        val studioCut = (totalCost * fundingScheme.studioRatio)

        if (creatorCut > 0 && cc.contentCreatorCash < creatorCut) return false
        if (studioCut > 0 && filmStudio.companyCash < studioCut) return false

        val cleanTitle = title.trim()

        // Anti-Duplicate checking in Studio
        if (filmStudio.projectHistory.any { it.title.trim().equals(cleanTitle, ignoreCase = true) }) {
            return false // duplicate title
        }

        val resolvedGenres = if (genres.isNotEmpty()) {
            genres
        } else {
            listOf(when (type) {
                ContentType.FEATURE_FILM -> "Movie"
                ContentType.SHORT_FILM -> "Short Film"
                ContentType.DOCUMENTARY -> "Documentary"
                ContentType.DEEP_DIVE_ESSAY -> "Drama"
            })
        }

        // Quality / Review Score calculation manipulated by Production Focus
        val reviewScore = when (productionFocus) {
            "KUALITAS" -> (65..100).random()
            "MAHAKARYA" -> (85..100).random()
            else -> {
                val lowerBound = minOf(15 + (filmStudio.level * 2), 70)
                var score = (lowerBound..100).random()
                if (resolvedGenres.size >= 3) {
                    if ((0..1).random() == 0) {
                        score -= (10..25).random()
                    } else {
                        score += (10..20).random()
                    }
                    score = score.coerceIn(1, 100)
                }
                score
            }
        }

        val distMult = if (isGlobal) 2.5 else 1.0
        val levelBonus = 1.0 + (filmStudio.level * 0.05)
        val promoRatio = if (budget > 0) promoBudget.toDouble() / budget.toDouble() else 0.0
        val promoMult = 1.0 + (promoRatio * 0.5).coerceAtMost(2.0)
        val viralMult = if (reviewScore > 85) 1.5 + ((reviewScore - 85) * 0.1) else 1.0
        val animQualityMult = if (filmStudio.studioType == "ANIMATION" && budget > 50000000) 1.5 else 1.0

        val boxOffice = if (reviewScore < 40) {
            (totalCost * (0.1 + (promoRatio * 0.1).coerceAtMost(0.4))).toLong()
        } else if (reviewScore > 85 && isGlobal) {
            (totalCost * (5..10).random() * levelBonus * promoMult * viralMult * animQualityMult).toLong()
        } else {
            val performance = reviewScore / 100.0
            (totalCost * performance * distMult * levelBonus * 3.0 * promoMult * viralMult * animQualityMult).toLong()
        }

        val netProfit = boxOffice - totalCost
        val initMonths = if (isGlobal) 6 else 4

        val isAnimStudio = filmStudio.studioType == "ANIMATION"
        val delayMonths = com.example.domain.subsystems.creative.FilmTimelineEngine.calculateTotalDurationMonths(
            isAnimation = isAnimStudio,
            filmFormat = filmFormat,
            budget = budget,
            productionFocus = productionFocus
        )

        val isScheduledFuture = schedMonth != null && schedYear != null &&
            (schedYear > currentState.inGameYear || (schedYear == currentState.inGameYear && schedMonth > currentState.inGameMonth))

        val initialCoProdPhase = if (isScheduledFuture) {
            "ANTREAN"
        } else {
            com.example.domain.subsystems.creative.FilmTimelineEngine.getProgress(
                isAnimation = isAnimStudio,
                totalMonths = delayMonths,
                remainingMonths = delayMonths
            ).displayLabel
        }

        val fundingTypeStr = fundingScheme.displayName
        val revenueSplit = fundingScheme.creatorRatio

        val meta = com.example.data.CoProductionMeta(
            isCoProd = true,
            partnerName = cc.name,
            fundingType = fundingTypeStr,
            revenueSplit = revenueSplit
        )

        // If studio bears >= 50% funding, studio gets active Box Office pipeline project
        // Otherwise studio gets a completed collaboration archive record
        val newProject = if (fundingScheme.studioRatio >= 0.5) {
            com.example.data.MovieProject(
                title = cleanTitle,
                budget = budget,
                genres = resolvedGenres,
                distributionScale = if (isGlobal) "Global" else "Local",
                reviewScore = reviewScore,
                boxOffice = boxOffice,
                netProfit = netProfit,
                status = "IN_PRODUCTION",
                remainingMonths = initMonths,
                currentRevenue = 0L,
                targetMaxRevenue = boxOffice,
                productionPhase = initialCoProdPhase,
                productionDelayMonths = delayMonths,
                promoBudget = promoBudget,
                scheduledMonth = schedMonth,
                scheduledYear = schedYear,
                filmFormat = filmFormat,
                productionFocus = productionFocus,
                coProductionMeta = meta,
                prodStartMonth = schedMonth ?: currentState.inGameMonth,
                prodStartYear = schedYear ?: currentState.inGameYear,
                totalProductionMonths = delayMonths
            )
        } else {
            com.example.data.MovieProject(
                title = cleanTitle,
                budget = budget,
                genres = resolvedGenres,
                distributionScale = if (isGlobal) "Global" else "Local",
                reviewScore = reviewScore,
                boxOffice = boxOffice,
                netProfit = netProfit,
                status = "FINISHED",
                remainingMonths = 0,
                currentRevenue = boxOffice,
                targetMaxRevenue = boxOffice,
                productionPhase = "TAYANG",
                productionDelayMonths = 0,
                promoBudget = promoBudget,
                scheduledMonth = schedMonth,
                scheduledYear = schedYear,
                filmFormat = filmFormat,
                productionFocus = productionFocus,
                coProductionMeta = meta,
                releaseMonth = currentState.inGameMonth,
                releaseYear = currentState.inGameYear,
                theaterStartMonth = currentState.inGameMonth,
                theaterStartYear = currentState.inGameYear,
                totalTheatricalMonths = 1,
                totalProductionMonths = delayMonths
            )
        }

        val shadowWork = com.example.data.ContentWork(
            id = java.util.UUID.randomUUID().toString(),
            title = cleanTitle,
            type = type,
            budget = totalCost,
            engagementScore = if (fundingScheme.creatorRatio >= 0.5) reviewScore.coerceIn(1, 100) else null,
            status = ContentStatus.AVAILABLE,
            monthlyRoyalty = 0L,
            acquiredLumpSum = 0L,
            acquiredByPH = null,
            contractDurationMonths = null,
            remainingContractMonths = null,
            partnerStudioName = filmStudio.name,
            partnerStudioId = filmStudio.instanceId,
            fundingScheme = fundingScheme,
            isShadowRecord = fundingScheme.creatorRatio < 0.5,
            movieProjectId = cleanTitle
        )

        val targetInstanceId = filmStudio.instanceId
        val ccId = cc.instanceId

        val updatedBusinesses = currentState.ownedBusinesses.map { biz ->
            when {
                biz?.instanceId == ccId -> {
                    biz.copy(
                        contentCreatorCash = biz.contentCreatorCash - creatorCut,
                        contentPortfolio = biz.contentPortfolio + shadowWork
                    )
                }
                biz?.instanceId == targetInstanceId -> {
                    biz.copy(
                        companyCash = biz.companyCash - studioCut,
                        projectHistory = (biz.projectHistory ?: emptyList()) + newProject
                    )
                }
                else -> biz
            }
        }

        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries.map { sub ->
                when {
                    sub?.instanceId == ccId -> {
                        sub.copy(
                            contentCreatorCash = sub.contentCreatorCash - creatorCut,
                            contentPortfolio = sub.contentPortfolio + shadowWork
                        )
                    }
                    sub?.instanceId == targetInstanceId -> {
                        sub.copy(
                            companyCash = sub.companyCash - studioCut,
                            projectHistory = (sub.projectHistory ?: emptyList()) + newProject
                        )
                    }
                    else -> sub
                }
            }
            holding.copy(subsidiaries = newSubs)
        }

        val nextState = currentState.copy(
            ownedBusinesses = updatedBusinesses,
            holdingCompanies = updatedHoldings
        )
        _playerState.value = nextState
        saveState(nextState)
        return true
    }

    fun acceptPHLumpSumOffer(offer: ProductionHouseOffer, targetInstanceId: String? = null): Boolean {
        return updateContentCreatorBusiness(targetInstanceId) { owned ->
            val updatedPortfolio = owned.contentPortfolio.map { work ->
                if (work.id == offer.contentId) {
                    work.copy(
                        status = ContentStatus.ACQUIRED_LUMP_SUM,
                        acquiredLumpSum = offer.lumpSumOffer,
                        acquiredByPH = offer.phName
                    )
                } else {
                    work
                }
            }
            owned.copy(
                contentCreatorCash = owned.contentCreatorCash + offer.lumpSumOffer,
                contentPortfolio = updatedPortfolio
            )
        }
    }

    fun acceptPHRoyaltyOffer(offer: ProductionHouseOffer, targetInstanceId: String? = null): Boolean {
        return updateContentCreatorBusiness(targetInstanceId) { owned ->
            val updatedPortfolio = owned.contentPortfolio.map { work ->
                if (work.id == offer.contentId) {
                    work.copy(
                        status = ContentStatus.LICENSED,
                        monthlyRoyalty = offer.monthlyRoyalty,
                        acquiredByPH = offer.phName,
                        contractDurationMonths = offer.contractDurationMonths,
                        remainingContractMonths = offer.contractDurationMonths
                    )
                } else {
                    work
                }
            }
            owned.copy(
                contentCreatorCash = owned.contentCreatorCash + offer.royaltyUpfront,
                contentPortfolio = updatedPortfolio
            )
        }
    }

    fun acceptContentCreatorPHOffer(offer: ProductionHouseOffer, isLumpSum: Boolean, targetInstanceId: String? = null): Boolean {
        return if (isLumpSum) {
            acceptPHLumpSumOffer(offer, targetInstanceId)
        } else {
            acceptPHRoyaltyOffer(offer, targetInstanceId)
        }
    }

    fun rejectPHOffer(offer: ProductionHouseOffer) {
        // Offer dismissed in UI
    }

    fun rejectPHOffer(offerId: String) {
        // Offer dismissed in UI
    }

    // ==========================================
    // STREAMING SERVICE / OTT PLATFORM OPERATIONS
    // ==========================================

    fun getStreamingServiceBusiness(instanceId: String? = null): com.example.data.OwnedBusiness? {
        val state = _playerState.value
        return if (!instanceId.isNullOrEmpty()) {
            state.ownedBusinesses.find { it.instanceId == instanceId }
                ?: state.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
                ?: state.ownedBusinesses.find { it.catalogId == "streaming_service" }
                ?: state.holdingCompanies.flatMap { it.subsidiaries }.find { it.catalogId == "streaming_service" }
        } else {
            state.ownedBusinesses.find { it.catalogId == "streaming_service" }
                ?: state.holdingCompanies.flatMap { it.subsidiaries }.find { it.catalogId == "streaming_service" }
        }
    }

    fun updateStreamingServiceBusiness(targetInstanceId: String? = null, transform: (com.example.data.OwnedBusiness) -> com.example.data.OwnedBusiness): Boolean {
        val currentState = _playerState.value
        val target = getStreamingServiceBusiness(targetInstanceId) ?: return false
        val targetId = target.instanceId

        var foundInOwned = false
        val newOwned = currentState.ownedBusinesses.map { owned ->
            if (owned.instanceId == targetId) {
                foundInOwned = true
                transform(owned)
            } else {
                owned
            }
        }

        var foundInHolding = false
        val newHoldings = currentState.holdingCompanies.map { holding ->
            var subUpdated = false
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.instanceId == targetId) {
                    subUpdated = true
                    foundInHolding = true
                    transform(sub)
                } else {
                    sub
                }
            }
            if (subUpdated) holding.copy(subsidiaries = newSubs) else holding
        }

        if (!foundInOwned && !foundInHolding) return false

        val nextState = currentState.copy(
            ownedBusinesses = newOwned,
            holdingCompanies = newHoldings
        )
        _playerState.value = nextState
        saveState(nextState)
        return true
    }

    fun injectCashToStreamingService(amount: Long, targetInstanceId: String? = null): Boolean {
        val currentState = _playerState.value
        if (currentState.cash >= amount && amount > 0) {
            val success = updateStreamingServiceBusiness(targetInstanceId) { owned ->
                val currentData = owned.streamingData
                owned.copy(streamingData = currentData.copy(streamingCash = currentData.streamingCash + amount))
            }
            if (success) {
                val nextState = _playerState.value.copy(cash = _playerState.value.cash - amount)
                _playerState.value = nextState
                saveState(nextState)
                return true
            }
        }
        return false
    }

    fun withdrawCashFromStreamingService(amount: Long, targetInstanceId: String? = null): Boolean {
        val st = getStreamingServiceBusiness(targetInstanceId) ?: return false
        if (st.streamingData.streamingCash >= amount && amount > 0) {
            val success = updateStreamingServiceBusiness(targetInstanceId) { owned ->
                val currentData = owned.streamingData
                owned.copy(streamingData = currentData.copy(streamingCash = currentData.streamingCash - amount))
            }
            if (success) {
                val nextState = _playerState.value.copy(cash = _playerState.value.cash + amount)
                _playerState.value = nextState
                saveState(nextState)
                return true
            }
        }
        return false
    }

    fun upgradeServerTier(targetInstanceId: String? = null): Boolean {
        val st = getStreamingServiceBusiness(targetInstanceId) ?: return false
        val currentTier = st.streamingData.serverTier
        if (currentTier >= com.example.data.STREAMING_SERVER_TIERS.size) return false

        val nextSpec = com.example.data.STREAMING_SERVER_TIERS[currentTier] // next tier
        val upgradeCost = nextSpec.upgradeCost

        if (st.streamingData.streamingCash >= upgradeCost) {
            return updateStreamingServiceBusiness(targetInstanceId) { owned ->
                val currentData = owned.streamingData
                owned.copy(
                    streamingData = currentData.copy(
                        serverTier = nextSpec.tier,
                        serverHealth = 100,
                        serverOutage = false,
                        streamingCash = currentData.streamingCash - upgradeCost
                    )
                )
            }
        }
        return false
    }

    fun upgradeStreamingTech(techType: String, targetInstanceId: String? = null): Boolean {
        val st = getStreamingServiceBusiness(targetInstanceId) ?: return false
        val data = st.streamingData
        var cost = 50_000L
        var canUpgrade = false

        val updatedData = when (techType) {
            "ENCODING" -> {
                if (data.encodingLevel < 5) {
                    cost = 75_000L * data.encodingLevel
                    if (data.streamingCash >= cost) {
                        canUpgrade = true
                        data.copy(encodingLevel = data.encodingLevel + 1, streamingCash = data.streamingCash - cost)
                    } else data
                } else data
            }
            "AI_ALGO" -> {
                if (data.aiAlgorithmLevel < 5) {
                    cost = 60_000L * data.aiAlgorithmLevel
                    if (data.streamingCash >= cost) {
                        canUpgrade = true
                        data.copy(aiAlgorithmLevel = data.aiAlgorithmLevel + 1, streamingCash = data.streamingCash - cost)
                    } else data
                } else data
            }
            "DRM" -> {
                if (data.drmProtectionLevel < 5) {
                    cost = 45_000L * data.drmProtectionLevel
                    if (data.streamingCash >= cost) {
                        canUpgrade = true
                        data.copy(drmProtectionLevel = data.drmProtectionLevel + 1, streamingCash = data.streamingCash - cost)
                    } else data
                } else data
            }
            "LOCALIZATION" -> {
                if (data.localizationLevel < 5) {
                    cost = 50_000L * data.localizationLevel
                    if (data.streamingCash >= cost) {
                        canUpgrade = true
                        data.copy(localizationLevel = data.localizationLevel + 1, streamingCash = data.streamingCash - cost)
                    } else data
                } else data
            }
            "MARKETING" -> {
                if (data.marketingTier < 5) {
                    cost = 100_000L * data.marketingTier
                    if (data.streamingCash >= cost) {
                        canUpgrade = true
                        data.copy(marketingTier = data.marketingTier + 1, streamingCash = data.streamingCash - cost)
                    } else data
                } else data
            }
            else -> data
        }

        if (canUpgrade) {
            return updateStreamingServiceBusiness(targetInstanceId) { owned ->
                owned.copy(streamingData = updatedData)
            }
        }
        return false
    }

    fun addLicensedFilmContract(offer: com.example.data.ExternalFilmContractOffer, targetInstanceId: String? = null): Boolean {
        val st = getStreamingServiceBusiness(targetInstanceId) ?: return false
        if (st.streamingData.streamingCash < offer.upfrontLicenseCost) return false

        val newContent = com.example.data.StreamingContent(
            title = offer.title,
            type = offer.type,
            source = com.example.data.StreamingContentSource.LICENSED_CONTRACT,
            rating = offer.rating,
            views = 50_000L,
            monthlyViewerSpike = offer.estimatedMonthlyViewers,
            isViral = offer.isViralCandidate,
            licenseExpiryMonths = offer.contractDurationMonths,
            monthlyLicenseFee = offer.monthlyLicenseFee,
            genre = offer.genre,
            synopsis = offer.synopsis
        )

        return updateStreamingServiceBusiness(targetInstanceId) { owned ->
            val d = owned.streamingData
            owned.copy(
                streamingData = d.copy(
                    streamingCash = d.streamingCash - offer.upfrontLicenseCost,
                    streamingCatalog = listOf(newContent) + d.streamingCatalog
                )
            )
        }
    }

    fun distributeMovieToInHouseOtt(studioInstanceId: String, projectTitle: String, targetOttInstanceId: String? = null): Boolean {
        val currentState = _playerState.value
        val targetOtt = if (!targetOttInstanceId.isNullOrEmpty()) {
            currentState.ownedBusinesses.find { it.instanceId == targetOttInstanceId }
                ?: currentState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == targetOttInstanceId }
        } else {
            currentState.ownedBusinesses.find { it.catalogId == "streaming_service" }
                ?: currentState.holdingCompanies.flatMap { it.subsidiaries }.find { it.catalogId == "streaming_service" }
        } ?: return false

        var isStudioNested = false
        var studioHoldingId: String? = null
        var studio = currentState.ownedBusinesses.find { it.instanceId == studioInstanceId }
        if (studio == null) {
            for (holding in currentState.holdingCompanies) {
                studio = holding.subsidiaries.find { it.instanceId == studioInstanceId }
                if (studio != null) {
                    isStudioNested = true
                    studioHoldingId = holding.instanceId
                    break
                }
            }
        }
        if (studio == null) return false

        val project = studio.projectHistory.find { it.title.equals(projectTitle, ignoreCase = true) } ?: return false

        val newContent = com.example.data.StreamingContent(
            title = project.title,
            type = when (project.filmFormat) {
                "Short Film" -> com.example.data.StreamingContentType.INDIE_DARLING
                "Animation" -> com.example.data.StreamingContentType.ANIME_ANIMATION
                "Dokumenter", "Documentary" -> com.example.data.StreamingContentType.DOCUMENTARY
                else -> com.example.data.StreamingContentType.BLOCKBUSTER_MOVIE
            },
            source = com.example.data.StreamingContentSource.IN_HOUSE_STUDIO,
            rating = project.reviewScore,
            views = (project.currentRevenue / 50L).coerceAtLeast(100_000L),
            monthlyViewerSpike = (project.currentRevenue / 100L).coerceAtLeast(25_000L),
            isViral = project.reviewScore >= 80,
            licenseExpiryMonths = 0, // Perpetual in-house
            monthlyLicenseFee = 0L,
            genre = project.genres.firstOrNull() ?: "Cinema Exclusive",
            synopsis = "Mahakarya sinema dari studio in-house (${studio.name}) yang disiarkan eksklusif di ${targetOtt.name}."
        )

        // 1. Update Studio's project history item
        val updatedProjects = studio.projectHistory.map { p ->
            if (p.title.equals(projectTitle, ignoreCase = true)) {
                p.copy(
                    licenseeName = "${targetOtt.name} (In-House OTT)",
                    licenseMonthlyFee = 0L,
                    licenseRemainingMonths = 999
                )
            } else p
        }
        val updatedStudio = studio.copy(projectHistory = updatedProjects)

        // 2. Update OTT's streaming catalog
        val ottData = targetOtt.streamingData
        val updatedOttCatalog = if (ottData.streamingCatalog.any { it.title.equals(project.title, ignoreCase = true) }) {
            ottData.streamingCatalog
        } else {
            listOf(newContent) + ottData.streamingCatalog
        }
        val updatedOtt = targetOtt.copy(streamingData = ottData.copy(streamingCatalog = updatedOttCatalog))

        // 3. Update player state (supporting both top-level and nested holding businesses)
        var state = _playerState.value

        if (isStudioNested && studioHoldingId != null) {
            state = state.copy(
                holdingCompanies = state.holdingCompanies.map { h ->
                    if (h.instanceId == studioHoldingId) {
                        h.copy(subsidiaries = h.subsidiaries.map { if (it.instanceId == studioInstanceId) updatedStudio else it })
                    } else h
                }
            )
        } else {
            state = state.copy(
                ownedBusinesses = state.ownedBusinesses.map { if (it.instanceId == studioInstanceId) updatedStudio else it }
            )
        }

        var isOttNested = false
        var ottHoldingId: String? = null
        if (state.ownedBusinesses.none { it.instanceId == targetOtt.instanceId }) {
            for (h in state.holdingCompanies) {
                if (h.subsidiaries.any { it.instanceId == targetOtt.instanceId }) {
                    isOttNested = true
                    ottHoldingId = h.instanceId
                    break
                }
            }
        }

        if (isOttNested && ottHoldingId != null) {
            state = state.copy(
                holdingCompanies = state.holdingCompanies.map { h ->
                    if (h.instanceId == ottHoldingId) {
                        h.copy(subsidiaries = h.subsidiaries.map { if (it.instanceId == targetOtt.instanceId) updatedOtt else it })
                    } else h
                }
            )
        } else {
            state = state.copy(
                ownedBusinesses = state.ownedBusinesses.map { if (it.instanceId == targetOtt.instanceId) updatedOtt else it }
            )
        }

        _playerState.value = syncTvValuation(state)
        saveState(_playerState.value)
        return true
    }

    fun pullFromFilmStudioToOtt(studioInstanceId: String, projectTitle: String, targetInstanceId: String? = null): Boolean {
        return distributeMovieToInHouseOtt(studioInstanceId, projectTitle, targetInstanceId)
    }

    fun pullFromContentCreatorBankToOtt(creatorInstanceId: String, workTitle: String, targetInstanceId: String? = null): Boolean {
        val currentState = _playerState.value
        val st = getStreamingServiceBusiness(targetInstanceId) ?: return false
        val creator = currentState.ownedBusinesses.find { it.instanceId == creatorInstanceId }
            ?: currentState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == creatorInstanceId }
            ?: return false

        val work = creator.contentPortfolio.find { it.title.equals(workTitle, ignoreCase = true) } ?: return false

        val newContent = com.example.data.StreamingContent(
            title = work.title,
            type = when (work.type) {
                com.example.data.ContentType.DOCUMENTARY -> com.example.data.StreamingContentType.DOCUMENTARY
                com.example.data.ContentType.SHORT_FILM -> com.example.data.StreamingContentType.INDIE_DARLING
                else -> com.example.data.StreamingContentType.ORIGINAL_SERIES
            },
            source = com.example.data.StreamingContentSource.CONTENT_BANK,
            rating = work.engagementScore ?: 78,
            views = 75_000L,
            monthlyViewerSpike = 15_000L,
            isViral = (work.engagementScore ?: 0) >= 82,
            licenseExpiryMonths = 0, // In-house IP
            monthlyLicenseFee = 0L,
            genre = work.creativeFocus ?: "Digital Masterpiece",
            synopsis = "Konten premium dari Bank Konten Creator studio yang diekspansi menjadi tayangan OTT berbayar."
        )

        return updateStreamingServiceBusiness(targetInstanceId) { owned ->
            val d = owned.streamingData
            if (d.streamingCatalog.any { it.title.equals(work.title, ignoreCase = true) }) {
                owned
            } else {
                owned.copy(streamingData = d.copy(streamingCatalog = listOf(newContent) + d.streamingCatalog))
            }
        }
    }

    fun produceOriginalOttTitle(
        title: String,
        type: com.example.data.StreamingContentType,
        genre: String,
        budget: Long,
        synopsis: String,
        targetInstanceId: String? = null
    ): Boolean {
        val st = getStreamingServiceBusiness(targetInstanceId) ?: return false
        if (st.streamingData.streamingCash < budget) return false

        val ratingScore = when {
            budget >= 2_000_000L -> (85..98).random()
            budget >= 500_000L -> (75..90).random()
            else -> (65..82).random()
        }

        val estimatedViewers = (budget / 10L).coerceIn(20_000L, 5_000_000L)
        val isViral = ratingScore >= 82

        val newContent = com.example.data.StreamingContent(
            title = title,
            type = type,
            source = com.example.data.StreamingContentSource.ORIGINAL_OTT,
            rating = ratingScore,
            views = (budget / 20L).coerceAtLeast(10_000L),
            monthlyViewerSpike = estimatedViewers,
            isViral = isViral,
            licenseExpiryMonths = 0,
            monthlyLicenseFee = 0L,
            genre = genre,
            synopsis = synopsis.ifBlank { "Produksi serial OTT orisinal beranggaran tinggi yang digarap eksklusif untuk memanjakan penonton setia." }
        )

        return updateStreamingServiceBusiness(targetInstanceId) { owned ->
            val d = owned.streamingData
            owned.copy(
                streamingData = d.copy(
                    streamingCash = d.streamingCash - budget,
                    streamingCatalog = listOf(newContent) + d.streamingCatalog
                )
            )
        }
    }

    fun terminateStreamingContract(contentId: String, targetInstanceId: String? = null): Boolean {
        return updateStreamingServiceBusiness(targetInstanceId) { owned ->
            val d = owned.streamingData
            owned.copy(streamingData = d.copy(streamingCatalog = d.streamingCatalog.filterNot { it.id == contentId }))
        }
    }

    fun deleteStreamingServiceBusiness(targetInstanceId: String? = null) {
        val currentState = _playerState.value
        val target = getStreamingServiceBusiness(targetInstanceId) ?: return
        val targetId = target.instanceId

        val newOwned = currentState.ownedBusinesses.filterNot { it.instanceId == targetId }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            holding.copy(subsidiaries = holding.subsidiaries.filterNot { it.instanceId == targetId })
        }
        val nextState = currentState.copy(
            ownedBusinesses = newOwned,
            holdingCompanies = newHoldings
        )
        _playerState.value = nextState
        saveState(nextState)
    }

    // ==========================================
    // BANKING UNIT STATE & OPERATIONS
    // ==========================================

    fun getBankingBusiness(instanceId: String): OwnedBusiness? {
        val state = _playerState.value
        return state.ownedBusinesses.firstOrNull { it?.instanceId == instanceId }
            ?: state.holdingCompanies.flatMap { it.subsidiaries }.firstOrNull { it?.instanceId == instanceId }
    }

    fun updateBankingBusiness(instanceId: String, transform: (com.example.data.OwnedBusiness) -> com.example.data.OwnedBusiness): Boolean {
        val currentState = _playerState.value
        val biz = getBankingBusiness(instanceId) ?: return false
        val targetId = biz.instanceId
        
        var foundInOwned = false
        val newOwned = currentState.ownedBusinesses.map { owned ->
            if (owned.instanceId == targetId) {
                foundInOwned = true
                transform(owned)
            } else {
                owned
            }
        }
        
        var foundInHolding = false
        val newHoldings = currentState.holdingCompanies.map { holding ->
            var subUpdated = false
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.instanceId == targetId) {
                    subUpdated = true
                    foundInHolding = true
                    transform(sub)
                } else {
                    sub
                }
            }
            if (subUpdated) holding.copy(subsidiaries = newSubs) else holding
        }
        
        if (!foundInOwned && !foundInHolding) return false
        
        val nextState = currentState.copy(
            ownedBusinesses = newOwned,
            holdingCompanies = newHoldings
        )
        _playerState.value = nextState
        saveState(nextState)
        return true
    }

    fun processBankMonthlyTick(instanceId: String) {
        val currentState = _playerState.value
        val biz = getBankingBusiness(instanceId) ?: return
        val allBiz = currentState.ownedBusinesses + currentState.holdingCompanies.flatMap { it.subsidiaries }
        val (updatedBankData, updatedAll) = BankingEngine.processMonthlyTick(biz.bankingData, allBiz)
        val updatedMap = updatedAll.associateBy { it.instanceId }
        
        val newOwned = currentState.ownedBusinesses.map { b ->
            if (b.instanceId == instanceId) b.copy(bankingData = updatedBankData)
            else updatedMap[b.instanceId] ?: b
        }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.instanceId == instanceId) sub.copy(bankingData = updatedBankData)
                else updatedMap[sub.instanceId] ?: sub
            }
            holding.copy(subsidiaries = newSubs)
        }
        val nextState = currentState.copy(
            ownedBusinesses = newOwned,
            holdingCompanies = newHoldings
        )
        _playerState.value = nextState
        saveState(nextState)
    }

    fun refreshBankLoanApplications(instanceId: String) {
        val currentState = _playerState.value
        val biz = getBankingBusiness(instanceId) ?: return
        val allBiz = currentState.ownedBusinesses + currentState.holdingCompanies.flatMap { it.subsidiaries }
        val newPipeline = BankingEngine.generateApplicationPipeline(
            currentTier = biz.bankingData.currentTier,
            baseLendingRate = biz.bankingData.lendingInterestRate,
            ownedBusinesses = allBiz,
            count = 6
        )
        val updatedBankData = biz.bankingData.copy(incomingApplications = newPipeline)
        updateBankingBusiness(instanceId) { it.copy(bankingData = updatedBankData) }
    }

    fun approveBankLoan(instanceId: String, application: LoanApplication): Boolean {
        val currentState = _playerState.value
        val biz = getBankingBusiness(instanceId) ?: return false
        val allBiz = currentState.ownedBusinesses + currentState.holdingCompanies.flatMap { it.subsidiaries }
        val result = BankingEngine.approveLoan(biz.bankingData, application, allBiz) ?: return false
        val (updatedBankData, updatedAll) = result
        val updatedMap = updatedAll.associateBy { it.instanceId }

        val newOwned = currentState.ownedBusinesses.map { b ->
            if (b.instanceId == instanceId) b.copy(bankingData = updatedBankData)
            else updatedMap[b.instanceId] ?: b
        }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.instanceId == instanceId) sub.copy(bankingData = updatedBankData)
                else updatedMap[sub.instanceId] ?: sub
            }
            holding.copy(subsidiaries = newSubs)
        }
        val nextState = currentState.copy(
            ownedBusinesses = newOwned,
            holdingCompanies = newHoldings
        )
        _playerState.value = nextState
        saveState(nextState)
        return true
    }

    fun rejectBankLoan(instanceId: String, applicationId: String) {
        val biz = getBankingBusiness(instanceId) ?: return
        val updatedBankData = BankingEngine.rejectLoan(biz.bankingData, applicationId)
        updateBankingBusiness(instanceId) { it.copy(bankingData = updatedBankData) }
    }

    fun writeOffBankNplLoan(instanceId: String, loanId: String) {
        val biz = getBankingBusiness(instanceId) ?: return
        val updatedBankData = BankingEngine.writeOffNplLoan(biz.bankingData, loanId)
        updateBankingBusiness(instanceId) { it.copy(bankingData = updatedBankData) }
    }

    fun setBankInterestRates(instanceId: String, depositRate: Double, lendingRate: Double) {
        val biz = getBankingBusiness(instanceId) ?: return
        val updatedBankData = biz.bankingData.copy(
            depositInterestRate = depositRate,
            lendingInterestRate = lendingRate
        )
        updateBankingBusiness(instanceId) { it.copy(bankingData = updatedBankData) }
    }

    fun unlockBankTier(instanceId: String, tier: BankTier): Boolean {
        val biz = getBankingBusiness(instanceId) ?: return false
        val bankData = biz.bankingData
        if (bankData.totalCustomerDepositsDpk < tier.minDpkRequired || bankData.internalCash < tier.upgradeCost) {
            return false
        }
        val updatedBankData = bankData.copy(
            currentTier = tier,
            internalCash = bankData.internalCash - tier.upgradeCost
        )
        return updateBankingBusiness(instanceId) { it.copy(bankingData = updatedBankData) }
    }

    fun updateBankAiRiskManager(instanceId: String, config: AiRiskManagerData) {
        val biz = getBankingBusiness(instanceId) ?: return
        val updatedBankData = biz.bankingData.copy(aiRiskManager = config)
        updateBankingBusiness(instanceId) { it.copy(bankingData = updatedBankData) }
    }

    fun upgradeBankAiRiskManager(instanceId: String): Boolean {
        val biz = getBankingBusiness(instanceId) ?: return false
        val ai = biz.bankingData.aiRiskManager
        val upgradeCost = ai.nextLevelUpgradeCost
        if (upgradeCost <= 0L || biz.bankingData.internalCash < upgradeCost) return false
        val updatedAi = ai.copy(level = ai.level + 1)
        val updatedBankData = biz.bankingData.copy(
            internalCash = biz.bankingData.internalCash - upgradeCost,
            aiRiskManager = updatedAi
        )
        return updateBankingBusiness(instanceId) { it.copy(bankingData = updatedBankData) }
    }

    fun triggerBankAiRiskCycle(instanceId: String) {
        val currentState = _playerState.value
        val biz = getBankingBusiness(instanceId) ?: return
        val allBiz = currentState.ownedBusinesses + currentState.holdingCompanies.flatMap { it.subsidiaries }
        val (updatedBankData, updatedAll) = BankingEngine.runAiRiskCycle(biz.bankingData, allBiz)
        val updatedMap = updatedAll.associateBy { it.instanceId }
        val newOwned = currentState.ownedBusinesses.map { b ->
            if (b.instanceId == instanceId) b.copy(bankingData = updatedBankData)
            else updatedMap[b.instanceId] ?: b
        }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.instanceId == instanceId) sub.copy(bankingData = updatedBankData)
                else updatedMap[sub.instanceId] ?: sub
            }
            holding.copy(subsidiaries = newSubs)
        }
        val nextState = currentState.copy(
            ownedBusinesses = newOwned,
            holdingCompanies = newHoldings
        )
        _playerState.value = nextState
        saveState(nextState)
    }

    fun injectCapitalToBank(instanceId: String, amount: Long): Boolean {
        val currentState = _playerState.value
        if (currentState.cash < amount) return false
        val success = updateBankingBusiness(instanceId) { b ->
            b.copy(bankingData = b.bankingData.copy(internalCash = b.bankingData.internalCash + amount))
        }
        if (success) {
            val nextState = _playerState.value.copy(cash = _playerState.value.cash - amount)
            _playerState.value = nextState
            saveState(nextState)
            return true
        }
        return false
    }

    fun withdrawCapitalFromBank(instanceId: String, amount: Long): Boolean {
        val biz = getBankingBusiness(instanceId) ?: return false
        if (biz.bankingData.internalCash < amount) return false
        val success = updateBankingBusiness(instanceId) { b ->
            b.copy(bankingData = b.bankingData.copy(internalCash = b.bankingData.internalCash - amount))
        }
        if (success) {
            val nextState = _playerState.value.copy(cash = _playerState.value.cash + amount)
            _playerState.value = nextState
            saveState(nextState)
            return true
        }
        return false
    }

    fun calculateBankLiquidationValuation(instanceId: String): Long {
        val biz = getBankingBusiness(instanceId) ?: return 0L
        val data = biz.bankingData
        val performingLoans = data.activeLoans.filter { it.healthStatus != LoanHealthStatus.SETTLED && it.healthStatus != LoanHealthStatus.NON_PERFORMING }.sumOf { it.remainingPrincipal }
        return data.internalCash + (performingLoans * 0.7).toLong()
    }

    fun liquidateBank(instanceId: String): Boolean {
        val currentState = _playerState.value
        val valuation = calculateBankLiquidationValuation(instanceId)
        val newBusinesses = currentState.ownedBusinesses.filterNot { it.instanceId == instanceId }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            holding.copy(subsidiaries = holding.subsidiaries.filterNot { it.instanceId == instanceId })
        }
        val nextState = currentState.copy(
            cash = currentState.cash + valuation,
            ownedBusinesses = newBusinesses,
            holdingCompanies = newHoldings
        )
        _playerState.value = nextState
        saveState(nextState)
        return true
    }

    fun deductCash(amount: Long): Boolean {
        val currentState = _playerState.value
        if (currentState.cash >= amount) {
            _playerState.value = currentState.copy(cash = currentState.cash - amount)
            return true
        }
        return false
    }

    fun upgradeBusinessSlot() {
        val currentState = _playerState.value
        val cost = getBusinessSlotUpgradePrice()
        if (currentState.cash >= cost) {
            _playerState.value = currentState.copy(
                cash = currentState.cash - cost,
                maxBusinessSlots = currentState.maxBusinessSlots + 1
            )
        }
    }

    fun buyProperty(propertyId: String) {
        val currentState = _playerState.value
        val property = _realEstateMarket.value.find { it.id == propertyId }
        val cost = property?.currentPrice ?: 0L
        if (property != null && currentState.privateBalance >= cost) {
            val newBalance = currentState.privateBalance - cost
            val ownedList = currentState.ownedProperties.toMutableList()
            ownedList.add(com.example.data.OwnedProperty(propertyId, cost, property.basePrice, property.condition))
            
            // Tax impact handled implicitly because RealEstate value adds to netWorth and wealth tax
            val nextState = currentState.copy(privateBalance = newBalance, ownedProperties = ownedList)
            val loggedState = logToPrivateLedger(nextState, "Beli Properti Komersial ($propertyId)", cost, false)
            _playerState.value = loggedState
            saveState(loggedState)
        }
    }

    fun getRenovationCost(property: com.example.data.PropertyItem, condition: Int): Long {
        val missingCondition = 100 - condition
        return ((property.basePrice * missingCondition) / 200.0).toLong()
    }

    fun renovateProperty(propertyId: String) {
        val currentState = _playerState.value
        val ownedProp = currentState.ownedProperties.find { it.propertyId == propertyId }
        val propItem = _realEstateMarket.value.find { it.id == propertyId }
        if (ownedProp != null && propItem != null && ownedProp.condition < 100) {
            val cost = getRenovationCost(propItem, ownedProp.condition)
            if (currentState.privateBalance >= cost) {
                val newBalance = currentState.privateBalance - cost
                val newEstimatedValue = (propItem.basePrice * 1.5).toLong()
                val updatedProp = ownedProp.copy(condition = 100, currentEstimatedValue = newEstimatedValue)
                val newList = currentState.ownedProperties.map { if (it.propertyId == propertyId) updatedProp else it }
                val nextState = currentState.copy(privateBalance = newBalance, ownedProperties = newList)
                val loggedState = logToPrivateLedger(nextState, "Renovasi Properti ($propertyId)", cost, false)
                _playerState.value = loggedState
                saveState(loggedState)
            }
        }
    }

    fun sellPropertySultan(propertyId: String) {
        val currentState = _playerState.value
        val ownedProp = currentState.ownedProperties.find { it.propertyId == propertyId }
        val propItem = _realEstateMarket.value.find { it.id == propertyId }
        if (ownedProp != null && propItem != null && ownedProp.condition == 100) {
            val sellPrice = ownedProp.currentEstimatedValue
            val newBalance = currentState.privateBalance + sellPrice
            val newList = currentState.ownedProperties.filterNot { it.propertyId == propertyId }
            val nextState = currentState.copy(privateBalance = newBalance, ownedProperties = newList)
            val loggedState = logToPrivateLedger(nextState, "Jual Properti Komersial ($propertyId)", sellPrice, true)
            _playerState.value = loggedState
            saveState(loggedState)
        }
    }

    fun buyCollection(itemId: String): String? {
        val currentState = _playerState.value
        val item = _collectionList.value.find { it.id == itemId } ?: return "Item tidak ditemukan."
        if (currentState.privateBalance < item.basePrice) {
            return "Kas Pribadi tidak cukup! Tarik Gaji atau Dividen dari Family Office terlebih dahulu."
        }
        val newBalance = currentState.privateBalance - item.basePrice
        val ownedList = currentState.ownedCollections.toMutableList()
        ownedList.add(com.example.data.OwnedCollection(itemId = itemId, purchasedPrice = item.basePrice))
        val nextState = currentState.copy(privateBalance = newBalance, ownedCollections = ownedList)
        val newState = logToPrivateLedger(nextState, "Beli Koleksi (${item.name})", item.basePrice, false)
        _playerState.value = newState
        saveState(newState)
        return null
    }
    
    fun updateCollectionImage(instanceId: String, imageUrl: String) {
        val currentState = _playerState.value
        val updatedCollections = currentState.ownedCollections.map { 
            if (it.instanceId == instanceId) it.copy(customImageUrl = imageUrl) else it 
        }
        _playerState.value = currentState.copy(ownedCollections = updatedCollections)
    }

    fun buyResidentialProperty(housingId: String): String? {
        val currentState = _playerState.value
        val currentMarket = _housingList.value
        val item = currentMarket.find { it.id == housingId }
        
        if (item == null) {
            return "Properti tidak ditemukan di Marketplace."
        }
        if (currentState.privateBalance < item.buyPrice) {
            return "Kas Pribadi tidak cukup! Tarik Gaji atau Dividen dari Family Office terlebih dahulu."
        }
        
        // Add to owned
        val newBalance = currentState.privateBalance - item.buyPrice
        val ownedList = currentState.ownedHouses.toMutableList()
        val existing = ownedList.find { it.housingId == housingId }
        if (existing != null) {
            return "Anda sudah memiliki properti ini."
        }
        
        ownedList.add(com.example.data.OwnedHousing(housingId = housingId, purchasedPrice = item.buyPrice, customImageUrl = item.imageUrl))
        val nextState = currentState.copy(privateBalance = newBalance, ownedHouses = ownedList)
        val newState = logToPrivateLedger(nextState, "Beli Properti Hunian (${item.name})", item.buyPrice, false)
        _playerState.value = newState
        saveState(newState)
        
        return null
    }

    fun rentHousing(housingId: String): String? {
        val currentState = _playerState.value
        val item = _housingList.value.find { it.id == housingId }
        if (item == null) {
            return "Properti tidak ditemukan."
        }
        if (currentState.privateBalance < item.rentPrice) {
            return "Kas Pribadi tidak cukup! Tarik Gaji atau Dividen dari Family Office terlebih dahulu."
        }
        val newBalance = currentState.privateBalance - item.rentPrice
        val rentedList = currentState.rentedHouses.toMutableList()
        rentedList.add(com.example.data.RentedHousing(housingId = housingId, monthlyRent = item.rentPrice))
        val nextState = currentState.copy(privateBalance = newBalance, rentedHouses = rentedList)
        val newState = logToPrivateLedger(nextState, "Sewa Properti Hunian (${item.name})", item.rentPrice, false)
        _playerState.value = newState
        saveState(newState)
        return null
    }

    fun updateHousingImage(instanceId: String, imageUrl: String, isRented: Boolean) {
        val currentState = _playerState.value
        if (isRented) {
            val updated = currentState.rentedHouses.map { 
                if (it.instanceId == instanceId) it.copy(customImageUrl = imageUrl) else it 
            }
            _playerState.value = currentState.copy(rentedHouses = updated)
        } else {
            val updated = currentState.ownedHouses.map { 
                if (it.instanceId == instanceId) it.copy(customImageUrl = imageUrl) else it 
            }
            _playerState.value = currentState.copy(ownedHouses = updated)
        }
    }

    fun sellHousing(instanceId: String, sellPrice: Long) {
        val currentState = _playerState.value
        val itemToSell = currentState.ownedHouses.find { it.instanceId == instanceId }
        
        if (itemToSell != null) {
            val updatedOwnedHouses = currentState.ownedHouses.filter { it.instanceId != instanceId }
            val newBalance = currentState.privateBalance + sellPrice
            val nextState = currentState.copy(
                privateBalance = newBalance,
                ownedHouses = updatedOwnedHouses,
                lastMonthIncome = currentState.lastMonthIncome + sellPrice
            )
            
            val itemInfo = _housingList.value.find { it.id == itemToSell.housingId }
            val nameLabel = itemInfo?.name ?: instanceId
            val newState = logToPrivateLedger(nextState, "Jual Properti Hunian ($nameLabel)", sellPrice, true)
            _playerState.value = newState
            saveState(newState)
            
            // Re-add to market
            if (itemInfo == null) {
                // If it was custom or removed, let's try to restore? No, if it's missing from market, just leave it sold
            } else {
                val currentMarket = _housingList.value.toMutableList()
                if (currentMarket.none { it.id == itemToSell.housingId }) {
                    currentMarket.add(itemInfo)
                    _housingList.value = currentMarket
                    saveHousing(currentMarket)
                }
            }
        }
    }

    fun sellCollection(instanceId: String, sellPrice: Long) {
        val currentState = _playerState.value
        val itemToSell = currentState.ownedCollections.find { it.instanceId == instanceId }
        
        if (itemToSell != null) {
            val updatedCollections = currentState.ownedCollections.filter { it.instanceId != instanceId }
            val newBalance = currentState.privateBalance + sellPrice
            val nextState = currentState.copy(
                privateBalance = newBalance,
                ownedCollections = updatedCollections,
                lastMonthIncome = currentState.lastMonthIncome + sellPrice
            )
            
            val itemInfo = _collectionList.value.find { it.id == itemToSell.itemId }
            val nameLabel = itemInfo?.name ?: instanceId
            val newState = logToPrivateLedger(nextState, "Jual Koleksi ($nameLabel)", sellPrice, true)
            _playerState.value = newState
            saveState(newState)
        }
    }

    // --- TAX & LEGAL ---
    
    fun toggleNotary(enabled: Boolean) {
        val currentState = _playerState.value
        val newState = currentState.copy(
            taxLegalReport = currentState.taxLegalReport.copy(hasNotary = enabled)
        )
        _playerState.value = newState
        saveState(newState)
    }
    
    fun payTaxesManually(amount: Long): Boolean {
        val currentState = _playerState.value
        if (amount <= 0L) return false
        if (currentState.cash >= amount && currentState.taxLegalReport.unpaidTaxes > 0) {
            val payAmount = Math.min(amount, currentState.taxLegalReport.unpaidTaxes)
            val newUnpaid = currentState.taxLegalReport.unpaidTaxes - payAmount
            val newFrozenId = if (newUnpaid <= 0) null else currentState.taxLegalReport.frozenBusinessId
            val newState = currentState.copy(
                cash = currentState.cash - payAmount,
                corporateTaxPaid = currentState.corporateTaxPaid + payAmount,
                totalTaxPaid = currentState.totalTaxPaid + payAmount,
                taxLegalReport = currentState.taxLegalReport.copy(
                    unpaidTaxes = newUnpaid,
                    frozenBusinessId = newFrozenId
                )
            )
            _playerState.value = newState
            saveState(newState)
            return true
        }
        return false
    }
    
    fun resolveLawsuit(lawsuitId: String, lawyerTier: Int): String {
        // lawyerTier: 0 = Settlement, 1 = Intern (40% win), 2 = Premium (95% win)
        val currentState = _playerState.value
        val lawsuit = currentState.taxLegalReport.activeLawsuits.find { it.id == lawsuitId } ?: return "Gugatan tidak ditemukan"
        
        val scale = lawsuit.scaleFactor
        var isWon = false
        var totalCost = 0L
        val resultMessage: String

        when (lawyerTier) {
            1 -> {
                val lawyerFee = (scale * 0.1).toLong()
                isWon = Math.random() < 0.40
                totalCost = lawyerFee + if (isWon) 0L else scale
                resultMessage = if (isWon) "🎉 Sukses! Pengacara Magang berhasil memenangkan perkara."
                               else "❌ Pengacara Magang kalah di sidang pengadilan."
            }
            2 -> {
                val lawyerFee = (scale * 0.4).toLong()
                isWon = Math.random() < 0.95
                totalCost = lawyerFee + if (isWon) 0L else scale
                resultMessage = if (isWon) "🎉 Sukses! Firma Hukum Premium berhasil memenangkan dan menutup kasus."
                               else "❌ Putusan banding hakim menolak pembelaan."
            }
            else -> {
                totalCost = (scale * 1.5).toLong()
                resultMessage = "🤝 Kesepakatan Damai luar pengadilan disetujui."
            }
        }
        
        if (currentState.cash >= totalCost) {
            val updatedLawsuits = currentState.taxLegalReport.activeLawsuits.filterNot { it.id == lawsuitId }
            val newState = currentState.copy(
                cash = currentState.cash - totalCost,
                taxLegalReport = currentState.taxLegalReport.copy(activeLawsuits = updatedLawsuits)
            )
            _playerState.value = newState
            saveState(newState)
            return resultMessage
        } else {
            return "Kas korporasi tidak mencukupi untuk menyelesaikan perkara ini!"
        }
    }

    fun toggleTaxHaven() {
        val currentState = _playerState.value
        val newState = currentState.copy(
            taxLegalReport = currentState.taxLegalReport.copy(
                isTaxHavenActive = !currentState.taxLegalReport.isTaxHavenActive
            )
        )
        _playerState.value = newState
        saveState(newState)
    }

    fun payUnpaidTaxes() {
        val currentState = _playerState.value
        val unpaid = currentState.taxLegalReport.unpaidTaxes
        if (unpaid > 0 && currentState.cash >= unpaid) {
            val newState = currentState.copy(
                cash = currentState.cash - unpaid,
                corporateTaxPaid = currentState.corporateTaxPaid + unpaid,
                totalTaxPaid = currentState.totalTaxPaid + unpaid,
                taxLegalReport = currentState.taxLegalReport.copy(unpaidTaxes = 0, frozenBusinessId = null)
            )
            _playerState.value = newState
            saveState(newState)
        }
    }

    fun calculateEventCompanyValuation(business: com.example.data.OwnedBusiness): Long {
        val internalCash = business.companyCash.toLong()
        val standardAssetsVal = (business.eoOwnedAssets ?: emptyMap()).entries.sumOf { (assetName, qty) ->
            (getAssetPurchasePrice(assetName) * qty).toLong()
        }
        val customAssetsVal = (business.eoCustomAssets ?: emptyList()).sumOf {
            (it.quantity * it.priceUnit).toLong()
        }
        val totalAssetsInWarehouse = standardAssetsVal + customAssetsVal
        val prestigeBonus = business.eoPrestige * 10_000L
        return internalCash + totalAssetsInWarehouse + prestigeBonus
    }

    fun calculateSoftwareHouseValuation(
        business: com.example.data.OwnedBusiness,
        appProjects: List<com.example.data.AppProject> = _playerState.value.appProjects
    ): Long {
        val internalCash = business.companyCash.toLong()
        val liveSaaS = appProjects.filter { 
            (it.status == com.example.data.ProjectStatus.MAINTENANCE || it.kanbanColumn == "DEPLOYED") && 
            it.type == com.example.data.ProjectType.INDEPENDENT_SAAS && !it.isBugFixTask 
        }
        val totalMRR = liveSaaS.sumOf { it.currentMrr }.toLong()
        val devCount = business.softwareHouseData.uiUxDesigners + business.softwareHouseData.frontendDevelopers + business.softwareHouseData.backendEngineers
        val devTeamVal = devCount * 5_000L
        val annualMrr = totalMRR * 12L
        return internalCash + annualMrr + devTeamVal
    }

    fun liquidateBusiness(instanceId: String) {
        val currentState = _playerState.value
        var isNested = false
        var parentHoldingId: String? = null
        
        var business = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        
        if (business == null) {
            for (holding in currentState.holdingCompanies) {
                business = holding.subsidiaries.find { it.instanceId == instanceId }
                if (business != null) {
                    isNested = true
                    parentHoldingId = holding.instanceId
                    break
                }
            }
        }
        
        if (business == null) return
        val catalogItem = getCatalogItem(business.catalogId, currentState) ?: return
        
        val valuation = when (business.catalogId) {
            "aviation_group" -> {
                val fleetVal = business.airlineFleetComplex.sumOf { pl ->
                    val pDef = com.example.data.AVIATION_AIRCRAFT_CATALOG.find { it.id == pl.modelId }
                    if (pl.isLeased) 0L else (pDef?.price ?: 0L)
                }
                val hubsVal = business.airlineHubsComplex.sumOf { it.baseCost }
                val businessCashVal = business.companyCash.toLong()
                val baseVal = catalogItem.costToBuy
                val totalAssets = baseVal + fleetVal + hubsVal + businessCashVal
                (totalAssets * 0.70).toLong()
            }
            "media_radio" -> {
                calculateEventCompanyValuation(business)
            }
            "upper_tech" -> {
                calculateSoftwareHouseValuation(business, currentState.appProjects)
            }
            else -> {
                com.example.data.getBusinessValuation(business, catalogItem)
            }
        }
        
        val newAppProjects = if (business.catalogId == "upper_tech") {
            currentState.appProjects.filter { it.type != com.example.data.ProjectType.INDEPENDENT_SAAS && it.kanbanColumn != "IN_PROGRESS" }
        } else {
            currentState.appProjects
        }

        if (isNested && parentHoldingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == parentHoldingId) {
                    holding.copy(
                        holdingCash = holding.holdingCash + valuation,
                        subsidiaries = holding.subsidiaries.filter { it.instanceId != instanceId }
                    )
                } else holding
            }
            _playerState.value = currentState.copy(
                holdingCompanies = newHoldings,
                appProjects = newAppProjects
            )
        } else {
            _playerState.value = currentState.copy(
                cash = currentState.cash + valuation,
                ownedBusinesses = currentState.ownedBusinesses.filter { it.instanceId != instanceId },
                appProjects = newAppProjects
            )
        }
    }

    fun demergerHolding(holdingInstanceId: String) {
        val currentState = _playerState.value
        val holding = currentState.holdingCompanies.find { it.instanceId == holdingInstanceId } ?: return
        
        val children = holding.subsidiaries.map { it.copy(parentId = null) }
        val refundCash = holding.holdingCash.toLong().coerceAtLeast(0L)
        
        val nextState = currentState.copy(
            cash = currentState.cash + refundCash,
            holdingCompanies = currentState.holdingCompanies.filter { it.instanceId != holdingInstanceId },
            ownedBusinesses = currentState.ownedBusinesses + children
        )
        _playerState.value = nextState
        saveState(nextState)
    }

    fun mergeBusinesses(businessIds: List<String>, mergerName: String, mergerType: String): Boolean {
        if (businessIds.size < 2) return false
        
        val currentState = _playerState.value
        val businessesToMerge = currentState.ownedBusinesses.filter { businessIds.contains(it.instanceId) }
            .ifEmpty { currentState.ownedBusinesses.filter { businessIds.contains(it.catalogId) } }
        
        if (businessesToMerge.size < 2) return false
        
        // Guard clause: Perusahaan hasil akuisisi pasar modal TIDAK BOLEH digabungkan
        if (businessesToMerge.any { it.acquiredStockTicker != null }) {
            return false
        }
        
        var totalValuation = 0L
        businessesToMerge.forEach { owned ->
            val catalogItem = getCatalogItem(owned.catalogId, currentState) ?: return@forEach
            totalValuation += getBusinessValuation(owned, catalogItem)
        }
        
        val mergeFee = (totalValuation * 0.10).toLong() // 10% legal fee for IPO/Merger
        if (currentState.cash < mergeFee) return false
        
        val finalName = if (mergerName.contains(mergerType, ignoreCase = true)) mergerName else "$mergerName $mergerType"
        val newHoldingId = java.util.UUID.randomUUID().toString()
        val preparedSubsidiaries = businessesToMerge.map { it.copy(parentId = newHoldingId) }
        val newHolding = com.example.data.HoldingCompany(
            instanceId = newHoldingId,
            name = finalName,
            subsidiaries = preparedSubsidiaries
        )
        
        val mergedInstanceIds = businessesToMerge.map { it.instanceId }.toSet()
        val newOwnedList = currentState.ownedBusinesses.filterNot { mergedInstanceIds.contains(it.instanceId) }.toMutableList()
        val newHoldingsList = currentState.holdingCompanies.toMutableList()
        newHoldingsList.add(newHolding)
       
        val nextState = currentState.copy(
            cash = currentState.cash - mergeFee,
            ownedBusinesses = newOwnedList,
            holdingCompanies = newHoldingsList
        )
        _playerState.value = nextState
        saveState(nextState)
        return true
    }

    fun processIPO(holdingId: String, percentToSell: Float) {
        val currentState = _playerState.value
        val holding = currentState.holdingCompanies.find { it.instanceId == holdingId } ?: return
        
        if (holding.isPublic) return // Already went public
        
        val (updatedHolding, cashGained) = com.example.data.CorporateFinanceManager.processIPO(holding, percentToSell, currentState)
        
        val updatedHoldings = currentState.holdingCompanies.map { 
            if(it.instanceId == holdingId) updatedHolding else it 
        }
        
        _playerState.value = currentState.copy(
            holdingCompanies = updatedHoldings,
            cash = currentState.cash + cashGained
        )
        saveState(_playerState.value)
    }

    fun restructureBusiness(sourceInstanceId: String, targetId: String, isTargetHolding: Boolean) {
        val currentState = _playerState.value
        if (isTargetHolding) {
            val sourceBusiness = currentState.ownedBusinesses.find { it.instanceId == sourceInstanceId } ?: return
            val updatedBusinesses = currentState.ownedBusinesses.filter { it.instanceId != sourceInstanceId }
            val updatedHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == targetId) {
                    holding.copy(subsidiaries = holding.subsidiaries + sourceBusiness.copy(parentId = targetId))
                } else {
                    holding
                }
            }
            _playerState.value = currentState.copy(
                ownedBusinesses = updatedBusinesses,
                holdingCompanies = updatedHoldings
            )
        } else {
            val updatedBusinesses = currentState.ownedBusinesses.map { biz ->
                if (biz.instanceId == sourceInstanceId) {
                    biz.copy(parentId = targetId)
                } else {
                    biz
                }
            }
            _playerState.value = currentState.copy(ownedBusinesses = updatedBusinesses)
        }
        saveState(_playerState.value)
    }

    fun ejectBusinessToRoot(sourceInstanceId: String, holdingId: String) {
        val currentState = _playerState.value
        
        var foundBusiness: com.example.data.OwnedBusiness? = null
        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            if (holding.instanceId == holdingId) {
                val match = holding.subsidiaries.find { it.instanceId == sourceInstanceId }
                if (match != null) {
                    foundBusiness = match.copy(parentId = null)
                }
                holding.copy(subsidiaries = holding.subsidiaries.filter { it.instanceId != sourceInstanceId })
            } else {
                holding
            }
        }
        
        val updatedBusinesses = currentState.ownedBusinesses.map { biz ->
            if (biz.instanceId == sourceInstanceId) {
                biz.copy(parentId = null)
            } else {
                biz
            }
        }.toMutableList()
        
        foundBusiness?.let { extracted ->
            if (!updatedBusinesses.any { it.instanceId == sourceInstanceId }) {
                updatedBusinesses.add(extracted)
            }
        }
        
        _playerState.value = currentState.copy(
            ownedBusinesses = updatedBusinesses,
            holdingCompanies = updatedHoldings
        )
        saveState(_playerState.value)
    }

    fun renameHoldingCompany(holdingId: String, newName: String) {
        val currentState = _playerState.value
        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            if (holding.instanceId == holdingId) {
                holding.copy(name = newName)
            } else {
                holding
            }
        }
        _playerState.value = currentState.copy(holdingCompanies = updatedHoldings)
        saveState(_playerState.value)
    }

    fun formMegaHolding(name: String, includeInvestments: Boolean, investmentCompanyName: String = "") {
        val currentState = _playerState.value
        val newState = currentState.copy(
            megaHolding = com.example.data.MegaHoldingState(
                isActive = true,
                companyName = name,
                includesInvestments = includeInvestments,
                investmentCompanyName = investmentCompanyName,
                ownershipPercentage = 100.0
            )
        )
        _playerState.value = newState
        saveState(newState)
    }

    fun ipoMegaHolding(percentageToSell: Double) {
        val currentState = _playerState.value
        if (!currentState.megaHolding.isActive || percentageToSell <= 0 || percentageToSell > currentState.megaHolding.ownershipPercentage) return
        
        val businessValue = currentState.ownedBusinesses.sumOf {
            val catalogItem = getCatalogItem(it.catalogId, currentState)
            if (catalogItem != null) getBusinessValuation(it, catalogItem) else 0L
        }
        val holdingValue = currentState.holdingCompanies.sumOf { holding ->
            val subVal = holding.subsidiaries.sumOf { sub ->
                val catalogItem = getCatalogItem(sub.catalogId, currentState)
                if (catalogItem != null) getBusinessValuation(sub, catalogItem) else 0L
            }
            subVal
        }
        var baseMegaValuation = businessValue + holdingValue
        if (currentState.megaHolding.includesInvestments) {
            val stocksValue = currentState.ownedStocks.sumOf { owned ->
                val liveStock = _stockList.value.find { it.ticker == owned.ticker }
                val livePrice = liveStock?.currentPrice ?: owned.averagePrice
                (owned.shares * livePrice).toLong()
            }
            baseMegaValuation += stocksValue
        }
        
        val cashGained = (baseMegaValuation * (percentageToSell / 100.0)).toLong()
        
        val newState = currentState.copy(
            cash = currentState.cash + cashGained,
            megaHolding = currentState.megaHolding.copy(
                ownershipPercentage = currentState.megaHolding.ownershipPercentage - percentageToSell
            )
        )
        _playerState.value = newState
        saveState(newState)
    }

    fun processDivestment(holdingId: String) {
        val currentState = _playerState.value
        val holding = currentState.holdingCompanies.find { it.instanceId == holdingId } ?: return
        
        val cashGained = com.example.data.CorporateFinanceManager.processDivestment(holding, currentState)
        val updatedHoldings = currentState.holdingCompanies.filterNot { it.instanceId == holdingId }
        
        _playerState.value = currentState.copy(
            holdingCompanies = updatedHoldings,
            cash = currentState.cash + cashGained
        )
        saveState(_playerState.value)
    }

    fun buyBusinessForHolding(holdingId: String, businessId: String, customName: String? = null, studioType: String = "LIVE_ACTION", vendorId: String? = null, firstHub: String? = null) {
        val currentState = _playerState.value
        val catalogItem = getCatalogItem(businessId, currentState) ?: return
        val holding = currentState.holdingCompanies.find { it.instanceId == holdingId } ?: return

        if (currentState.cash >= catalogItem.costToBuy) {
            var newOwnedBusiness = com.example.data.OwnedBusiness(
                catalogId = businessId,
                customName = customName,
                level = 1,
                purchasedUpgrades = emptySet(),
                upgradeLevels = emptyMap(),
                studioType = studioType,
                airlineHubs = if (firstHub != null) listOf(firstHub) else emptyList()
            )
            
            var updatedBusinesses = currentState.ownedBusinesses
            var updatedHoldings = currentState.holdingCompanies
            
            if (vendorId != null) {
                val profitForConstruction = (catalogItem.costToBuy * 0.40)
                newOwnedBusiness = newOwnedBusiness.copy(
                    isUpgrading = true,
                    upgradeDelayMonths = 3
                )
                
                updatedBusinesses = updatedBusinesses.map { biz ->
                    if (biz.instanceId == vendorId) {
                        val newTender = com.example.data.ConstructionProject(
                            name = "Internal: ${customName ?: catalogItem.name}",
                            totalContractValue = profitForConstruction,
                            durationMonths = 3,
                            remainingMonths = 3
                        )
                        biz.copy(
                            companyCash = biz.companyCash + profitForConstruction.toLong(),
                            activeTenders = biz.activeTenders + newTender
                        )
                    } else biz
                }
                
                updatedHoldings = updatedHoldings.map { h ->
                    val newSubs = h.subsidiaries.map { biz ->
                        if (biz.instanceId == vendorId) {
                            val newTender = com.example.data.ConstructionProject(
                                name = "Internal: ${customName ?: catalogItem.name}",
                                totalContractValue = profitForConstruction,
                                durationMonths = 3,
                                remainingMonths = 3
                            )
                            biz.copy(
                                companyCash = biz.companyCash + profitForConstruction.toLong(),
                                activeTenders = biz.activeTenders + newTender
                            )
                        } else biz
                    }
                    h.copy(subsidiaries = newSubs)
                }
            }
            
            val updatedHolding = updatedHoldings.find { it.instanceId == holdingId }?.copy(
                subsidiaries = (updatedHoldings.find { it.instanceId == holdingId }?.subsidiaries ?: emptyList()) + newOwnedBusiness
            )
            
            if (updatedHolding != null) {
                updatedHoldings = updatedHoldings.map { if (it.instanceId == holdingId) updatedHolding else it }
            }
            
            _playerState.value = currentState.copy(
                cash = currentState.cash - catalogItem.costToBuy,
                ownedBusinesses = updatedBusinesses,
                holdingCompanies = updatedHoldings
            )
            saveState(_playerState.value)
        }
    }

    fun buyBusiness(businessId: String, customName: String? = null, studioType: String = "LIVE_ACTION", vendorId: String? = null, firstHub: String? = null, parentId: String? = null) {
        val currentState = _playerState.value
        val catalogItem = getCatalogItem(businessId, currentState) ?: return

        val slotsUsed = currentState.ownedBusinesses.size + currentState.holdingCompanies.size
        if (slotsUsed >= currentState.maxBusinessSlots) return

        if (currentState.cash >= catalogItem.costToBuy) {
            var newOwned = com.example.data.OwnedBusiness(
                catalogId = businessId,
                customName = customName,
                level = 1,
                purchasedUpgrades = emptySet(),
                studioType = studioType,
                airlineHubs = if (firstHub != null) listOf(firstHub) else emptyList(),
                parentId = parentId
            )
            
            var updatedBusinesses = currentState.ownedBusinesses
            var updatedHoldings = currentState.holdingCompanies
            
            if (vendorId != null) {
                val profitForConstruction = (catalogItem.costToBuy * 0.40)
                newOwned = newOwned.copy(
                    isUpgrading = true,
                    upgradeDelayMonths = 3
                )
                
                updatedBusinesses = updatedBusinesses.map { biz ->
                    if (biz.instanceId == vendorId) {
                        val newTender = com.example.data.ConstructionProject(
                            name = "Internal: ${customName ?: catalogItem.name}",
                            totalContractValue = profitForConstruction,
                            durationMonths = 3,
                            remainingMonths = 3
                        )
                        biz.copy(
                            companyCash = biz.companyCash + profitForConstruction.toLong(),
                            activeTenders = biz.activeTenders + newTender
                        )
                    } else biz
                }
                
                updatedHoldings = updatedHoldings.map { h ->
                    val newSubs = h.subsidiaries.map { biz ->
                        if (biz.instanceId == vendorId) {
                            val newTender = com.example.data.ConstructionProject(
                                name = "Internal: ${customName ?: catalogItem.name}",
                                totalContractValue = profitForConstruction,
                                durationMonths = 3,
                                remainingMonths = 3
                            )
                            biz.copy(
                                companyCash = biz.companyCash + profitForConstruction.toLong(),
                                activeTenders = biz.activeTenders + newTender
                            )
                        } else biz
                    }
                    h.copy(subsidiaries = newSubs)
                }
            }
            
            updatedBusinesses = updatedBusinesses + newOwned

            _playerState.value = currentState.copy(
                cash = currentState.cash - catalogItem.costToBuy,
                ownedBusinesses = updatedBusinesses,
                holdingCompanies = updatedHoldings
            )
        }
        saveState(_playerState.value)
    }

    fun acquireAircraft(businessId: String, type: String, name: String, isNew: Boolean, cost: Long, capacity: Int, maintenanceCost: Double) {
        val currentState = _playerState.value
        if (currentState.cash < cost) return
        
        val newAircraft = com.example.data.Aircraft(
            type = type,
            name = name,
            capacity = capacity,
            isUsed = !isNew,
            condition = if (isNew) 100 else (60..85).random(),
            maintenanceCost = maintenanceCost,
            deliveryDelay = if (isNew) (1..3).random() else 0
        )
        
        // Find if it's in owned businesses
        var updated = false
        val newBusinesses = currentState.ownedBusinesses.map { biz ->
            if (biz.instanceId == businessId) {
                updated = true
                biz.copy(airlineFleet = biz.airlineFleet + newAircraft)
            } else biz
        }
        
        if (updated) {
            _playerState.value = currentState.copy(cash = currentState.cash - cost, ownedBusinesses = newBusinesses)
            saveState(_playerState.value)
            return
        }
        
        // Otherwise, it might be inside a holding company
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries.map { biz ->
                if (biz.instanceId == businessId) {
                    updated = true
                    biz.copy(airlineFleet = biz.airlineFleet + newAircraft)
                } else biz
            }
            holding.copy(subsidiaries = newSubs)
        }
        
        if (updated) {
             _playerState.value = currentState.copy(cash = currentState.cash - cost, holdingCompanies = newHoldings)
             saveState(_playerState.value)
        }
    }

    fun updateBusiness(businessId: String, cost: Long = 0L, mapper: (com.example.data.OwnedBusiness) -> com.example.data.OwnedBusiness) {
        val currentState = _playerState.value
        if (currentState.cash < cost) return
        
        var updated = false
        val newBusinesses = currentState.ownedBusinesses.map { biz ->
            if (biz.instanceId == businessId) {
                updated = true
                mapper(biz)
            } else biz
        }
        
        if (updated) {
            _playerState.value = currentState.copy(
                cash = currentState.cash - cost,
                ownedBusinesses = newBusinesses
            )
            saveState(_playerState.value)
            return
        }
        
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries.map { biz ->
                if (biz.instanceId == businessId) {
                    updated = true
                    mapper(biz)
                } else biz
            }
            holding.copy(subsidiaries = newSubs)
        }
        
        if (updated) {
            _playerState.value = currentState.copy(
                cash = currentState.cash - cost,
                holdingCompanies = newHoldings
            )
            saveState(_playerState.value)
        }
    }

    fun buyAviationHubComplex(businessId: String, city: String, cost: Long, buildTime: Int = 0) {
        updateBusiness(businessId, cost = 0L) { biz ->
            val finalCost = if (biz.companyCash >= cost) cost else 0L
            val newHub = com.example.data.AviationHub(
                city = city,
                baseCost = cost,
                activeUpgrades = emptyList(),
                constructionQueue = emptyList(),
                isConstructing = buildTime > 0,
                constructionMonthsLeft = buildTime
            )
            biz.copy(
                companyCash = biz.companyCash - finalCost,
                airlineHubsComplex = biz.airlineHubsComplex + newHub,
                airlineHubs = biz.airlineHubs + city
            )
        }
    }

    fun startHubUpgradeComplex(businessId: String, hubId: String, upgradeId: String, cost: Long, buildTime: Int) {
        updateBusiness(businessId, cost = cost) { biz ->
            val updatedHubs = biz.airlineHubsComplex.map { hub ->
                if (hub.id == hubId) {
                    val newQueueItem = com.example.data.HubConstructionItem(upgradeId, buildTime)
                    hub.copy(constructionQueue = hub.constructionQueue + newQueueItem)
                } else hub
            }
            biz.copy(airlineHubsComplex = updatedHubs)
        }
    }

    fun buyComplexAircraft(businessId: String, modelId: String, cost: Long, deliveryTime: Int, isLeased: Boolean = false, leasePrice: Long = 0L, quantity: Int = 1) {
        val actualCost = if (isLeased) 0L else (cost * quantity)
        updateBusiness(businessId, cost = 0L) { biz ->
            val newPlanes = (1..quantity).map {
                com.example.data.AircraftInstance(
                    id = java.util.UUID.randomUUID().toString(),
                    modelId = modelId,
                    condition = 100.0,
                    status = "DELIVERING",
                    monthsUntilDelivery = deliveryTime,
                    stationedHubId = null,
                    assignedRouteId = null,
                    isLeased = isLeased,
                    leasePrice = leasePrice
                )
            }
            biz.copy(
                companyCash = biz.companyCash - actualCost,
                airlineFleetComplex = biz.airlineFleetComplex + newPlanes
            )
        }
    }

    fun assignAircraftToHubComplex(businessId: String, aircraftId: String, hubId: String?) {
        updateBusiness(businessId) { biz ->
            val updatedFleet = biz.airlineFleetComplex.map { plane ->
                if (plane.id == aircraftId) {
                    plane.copy(
                        stationedHubId = hubId,
                        status = if (hubId == null) "STANDBY" else plane.status,
                        assignedRouteId = null
                    )
                } else plane
            }
            val updatedRoutes = biz.flightRoutes.map { route ->
                if (route.assignedAircraftIds.contains(aircraftId)) {
                    route.copy(assignedAircraftIds = route.assignedAircraftIds - aircraftId)
                } else route
            }
            biz.copy(airlineFleetComplex = updatedFleet, flightRoutes = updatedRoutes)
        }
    }

    fun createFlightRouteComplex(businessId: String, originHubId: String, destination: String, distanceCategory: String, demand: Int, ticketPrice: Int) {
        updateBusiness(businessId) { biz ->
            val newRoute = com.example.data.FlightRoute(
                originHubId = originHubId,
                destination = destination,
                distanceCategory = distanceCategory,
                baseDemand = demand,
                ticketPrice = ticketPrice,
                assignedAircraftIds = emptyList()
            )
            biz.copy(flightRoutes = biz.flightRoutes + newRoute)
        }
    }

    fun deleteFlightRouteComplex(businessId: String, routeId: String) {
        updateBusiness(businessId) { biz ->
            val updatedRoutes = biz.flightRoutes.filter { it.id != routeId }
            val updatedFleet = biz.airlineFleetComplex.map { plane ->
                if (plane.assignedRouteId == routeId) {
                    plane.copy(assignedRouteId = null, status = "STANDBY")
                } else plane
            }
            biz.copy(flightRoutes = updatedRoutes, airlineFleetComplex = updatedFleet)
        }
    }

    fun assignAircraftToRouteComplex(businessId: String, aircraftId: String, routeId: String?) {
        updateBusiness(businessId) { biz ->
            val updatedFleet = biz.airlineFleetComplex.map { plane ->
                if (plane.id == aircraftId) {
                    plane.copy(
                        assignedRouteId = routeId,
                        status = if (routeId != null) "ASSIGNED" else "STANDBY"
                    )
                } else plane
            }
            val updatedRoutes = biz.flightRoutes.map { route ->
                val alreadyAssigned = route.assignedAircraftIds.contains(aircraftId)
                if (route.id == routeId) {
                    if (!alreadyAssigned) {
                        route.copy(assignedAircraftIds = route.assignedAircraftIds + aircraftId)
                    } else route
                } else {
                    if (alreadyAssigned) {
                        route.copy(assignedAircraftIds = route.assignedAircraftIds - aircraftId)
                    } else route
                }
            }
            biz.copy(airlineFleetComplex = updatedFleet, flightRoutes = updatedRoutes)
        }
    }

    fun processHealthcareMonthly(owned: com.example.data.OwnedBusiness): Triple<com.example.data.OwnedBusiness, Long, Long> {
        // 1. Epidemic Event Progression & Random Trigger
        var currentEpidemic = owned.activeEpidemicEvent
        if (currentEpidemic != null) {
            val remain = currentEpidemic.remainingMonths - 1
            if (remain <= 0) {
                currentEpidemic = null
            } else {
                currentEpidemic = currentEpidemic.copy(remainingMonths = remain)
            }
        } else {
            // 10% RNG chance if healthcare units exist
            if (owned.healthcareSubsidiaries.isNotEmpty() && kotlin.random.Random.nextDouble() < 0.10) {
                val epidemicList = listOf(
                    com.example.data.HealthcareEpidemicEvent(
                        title = "Musim Demam Berdarah (Dengue Outbreak)",
                        description = "Penyebaran nyamuk Aedes aegypti menyebabkan lonjakan kasus DBD mendadak di seluruh kota!",
                        severityMultiplier = 3.0,
                        durationMonths = 3,
                        remainingMonths = 3,
                        icon = "🦟",
                        effectDescription = "Klaim Asuransi Melonjak +300% & Kapasitas Kasur RS Penuh!"
                    ),
                    com.example.data.HealthcareEpidemicEvent(
                        title = "Wabah Virus Mutasi Baru (Influenza Strain)",
                        description = "Strain virus saluran pernapasan baru menyebar masif. Pasien IGD dan klaim asuransi meledak!",
                        severityMultiplier = 3.5,
                        durationMonths = 3,
                        remainingMonths = 3,
                        icon = "🦠",
                        effectDescription = "Klaim Asuransi Melonjak +350% & BOR RS Mencapai 100%!"
                    ),
                    com.example.data.HealthcareEpidemicEvent(
                        title = "Gelombang Polusi Udara Ekstrem (ISPA)",
                        description = "Kualitas udara memburuk drastis, memicu lonjakan kasus pernapasan dan rawat inap poli spesialis.",
                        severityMultiplier = 2.5,
                        durationMonths = 2,
                        remainingMonths = 2,
                        icon = "🌫️",
                        effectDescription = "Klaim Asuransi +250% & Poli Spesialis Paru Penuh!"
                    ),
                    com.example.data.HealthcareEpidemicEvent(
                        title = "Ledakan Keracunan Makanan Massal",
                        description = "Kontaminasi pasokan bahan makanan pesta kota menyebabkan ribuan warga dilarikan ke IGD.",
                        severityMultiplier = 2.8,
                        durationMonths = 2,
                        remainingMonths = 2,
                        icon = "⚠️",
                        effectDescription = "IGD Darurat Membludak & Klaim Asuransi Naik +280%!"
                    )
                )
                currentEpidemic = epidemicList.random()
            }
        }

        val epidemicMultiplier = currentEpidemic?.severityMultiplier ?: 1.0

        // 2. Handle upgrade delays
        val readyUnits = owned.healthcareSubsidiaries.map { u ->
            var unit = u
            if (unit.isUpgrading) {
                val delay = unit.upgradeDelayMonths - 1
                if (delay <= 0) {
                    unit = unit.copy(isUpgrading = false, upgradeDelayMonths = 0)
                } else {
                    unit = unit.copy(upgradeDelayMonths = delay)
                }
            }
            unit
        }

        val hospitals = readyUnits.filter { (it.type == "HOSPITAL" || it.type == "CLINIC") && !it.isUpgrading }
        val insurances = readyUnits.filter { it.type == "INSURANCE" && !it.isUpgrading }

        // Track hospital available beds and in-network assignments
        class HospCapacity(
            val id: String,
            val totalBeds: Int,
            var inNetworkPatients: Int = 0,
            var inNetworkRevenueBonus: Double = 0.0
        )

        val hospCapacities = hospitals.associate { h ->
            val baseBeds = if (h.type == "HOSPITAL") (100 + (h.level - 1) * 20) else (30 + (h.level - 1) * 10)
            val deptBeds = (h.igdLevel * 40) + (h.specialistLevel * 25)
            val beds = maxOf(h.totalBeds, baseBeds + deptBeds)
            h.id to HospCapacity(h.id, beds)
        }.toMutableMap()

        // 3. Process Insurance Units (Payer)
        val updatedInsuranceMap = mutableMapOf<String, com.example.data.HealthcareUnit>()

        for (ins in insurances) {
            val premium = ins.monthlyPremium.coerceIn(20.0, 500.0)
            val baseGrowth = when {
                premium <= 30.0 -> (1500..3000).random()
                premium <= 50.0 -> (800..1800).random()
                premium <= 80.0 -> (300..900).random()
                premium <= 120.0 -> (100..400).random()
                premium <= 200.0 -> (20..150).random()
                else -> (-50..50).random()
            }
            val tierMultiplier = when (ins.tierCategory) {
                "PREMIUM" -> 1.5
                "ELITE" -> 2.5
                else -> 1.0
            }
            val newMembers = maxOf(100L, ins.members + (baseGrowth * tierMultiplier).toLong())
            val premiumRevenue = newMembers * premium

            // Claim Attack
            val baseSickRate = kotlin.random.Random.nextDouble(0.04, 0.07)
            val effectiveSickRate = (baseSickRate * epidemicMultiplier).coerceAtMost(0.45)
            val sickPatients = maxOf(1L, (newMembers * effectiveSickRate).toLong())
            val avgClaimCost = (2500.0 * tierMultiplier)
            val totalClaimsCost = sickPatients * avgClaimCost

            var inNetCount = 0L
            var outNetCount = 0L
            var inNetAmount = 0.0
            var outNetAmount = 0.0

            // Route sick patients into available In-Network Hospital beds
            var remainingSick = sickPatients
            for ((_, cap) in hospCapacities) {
                if (remainingSick <= 0) break
                val freeBeds = cap.totalBeds - cap.inNetworkPatients
                if (freeBeds > 0) {
                    val take = minOf(remainingSick, freeBeds.toLong()).toInt()
                    cap.inNetworkPatients += take
                    val payoutTransfer = take * avgClaimCost
                    cap.inNetworkRevenueBonus += payoutTransfer

                    inNetCount += take
                    inNetAmount += payoutTransfer
                    remainingSick -= take
                }
            }

            outNetCount = remainingSick
            outNetAmount = outNetCount * avgClaimCost

            val insOpEx = premiumRevenue * 0.10
            val netInsCashFlow = premiumRevenue - insOpEx - totalClaimsCost
            val newInsCash = ins.unitCash + netInsCashFlow

            val lossRatio = (totalClaimsCost / maxOf(1.0, premiumRevenue)) * 100.0
            val synergyRate = (inNetCount.toDouble() / maxOf(1L, sickPatients)) * 100.0

            updatedInsuranceMap[ins.id] = ins.copy(
                members = newMembers,
                monthlyRevenue = premiumRevenue,
                unitCash = newInsCash,
                lastMonthClaimsPaid = totalClaimsCost,
                lastMonthClaimsCount = sickPatients,
                inNetworkClaimsCount = inNetCount,
                outNetworkClaimsCount = outNetCount,
                inNetworkClaimsAmount = inNetAmount,
                outNetworkClaimsAmount = outNetAmount,
                lossRatio = lossRatio,
                synergyRate = synergyRate
            )
        }

        // 4. Process Hospital / Clinic Units (Provider)
        var totalDividendsFromHealthcare = 0L
        var extraV = 0L
        val updatedHospitalMap = mutableMapOf<String, com.example.data.HealthcareUnit>()

        for (hosp in hospitals) {
            val cap = hospCapacities[hosp.id]
            val totalBeds = cap?.totalBeds ?: maxOf(hosp.totalBeds, (hosp.igdLevel * 40) + (hosp.specialistLevel * 25) + 100)
            val inNetPatients = cap?.inNetworkPatients ?: 0
            val inNetClaimRevenue = cap?.inNetworkRevenueBonus ?: 0.0

            val igdOrganic = (hosp.igdLevel * (30..50).random() * (if (currentEpidemic != null) 2.2 else 1.0)).toInt()
            val specOrganic = (hosp.specialistLevel * (15..30).random()).toInt()

            val totalIncoming = inNetPatients + igdOrganic + specOrganic
            val admittedPatients = minOf(totalIncoming, totalBeds)
            val rejectedPatients = maxOf(0, totalIncoming - totalBeds)

            val bor = (admittedPatients.toDouble() / maxOf(1, totalBeds)) * 100.0

            val igdRev = (minOf(igdOrganic + inNetPatients, admittedPatients)) * 1500.0 * (1.0 + (hosp.igdLevel - 1) * 0.15)
            val specRev = minOf(specOrganic, maxOf(0, admittedPatients - igdOrganic - inNetPatients)) * 4500.0 * (1.0 + (hosp.specialistLevel - 1) * 0.30)
            val pharmaRev = (admittedPatients * 600.0 * (1.0 + (hosp.pharmacyLevel - 1) * 0.25)) + (hosp.pharmacyLevel * 8000.0)

            val totalHospRev = igdRev + specRev + pharmaRev + inNetClaimRevenue
            val hospExpenses = (totalBeds * 200.0) + (hosp.igdLevel * 8000.0) + (hosp.specialistLevel * 20000.0) + (hosp.pharmacyLevel * 4000.0)

            val netHospProfit = totalHospRev - hospExpenses
            var hospCash = hosp.unitCash

            if (netHospProfit > 0) {
                val dividend = netHospProfit * 0.20
                totalDividendsFromHealthcare += dividend.toLong()
                hospCash += (netHospProfit - dividend)
            } else {
                hospCash += netHospProfit
            }

            extraV += (totalBeds * 15_000L + (hosp.igdLevel + hosp.specialistLevel + hosp.pharmacyLevel) * 400_000L)

            updatedHospitalMap[hosp.id] = hosp.copy(
                members = admittedPatients.toLong(),
                totalBeds = totalBeds,
                currentOccupiedBeds = admittedPatients,
                bor = bor,
                rejectedPatientsLastMonth = rejectedPatients,
                monthlyRevenue = totalHospRev,
                unitCash = hospCash
            )
        }

        for ((_, ins) in updatedInsuranceMap) {
            extraV += (ins.members * 1200L)
        }

        val allFinalUnits = readyUnits.map { original ->
            updatedInsuranceMap[original.id] ?: updatedHospitalMap[original.id] ?: original
        }

        val updatedBusiness = owned.copy(
            healthcareSubsidiaries = allFinalUnits,
            activeEpidemicEvent = currentEpidemic,
            extraValuation = owned.extraValuation + extraV
        )

        return Triple(updatedBusiness, totalDividendsFromHealthcare, extraV)
    }

    fun calculateAviationExpenses(owned: com.example.data.OwnedBusiness): Long {
        return owned.calculateTotalExpenses()
    }

    fun processAviationMonthlyTick(owned: com.example.data.OwnedBusiness): com.example.data.OwnedBusiness {
        val updatedFleet = owned.airlineFleetComplex.map { plane ->
            if (plane.status == "DELIVERING") {
                val remain = plane.monthsUntilDelivery - 1
                if (remain <= 0) {
                    plane.copy(status = "STANDBY", monthsUntilDelivery = 0)
                } else {
                    plane.copy(monthsUntilDelivery = remain)
                }
            } else {
                val activeRoute = owned.flightRoutes.find { r -> r.assignedAircraftIds.contains(plane.id) }
                val degradeRate = if (activeRoute != null) 2.0 else 0.5
                val newCond = (plane.condition - degradeRate).coerceAtLeast(0.0)
                plane.copy(condition = newCond)
            }
        }

        val updatedHubs = owned.airlineHubsComplex.map { hub ->
            var nextIsConstructing = hub.isConstructing
            var nextMonthsLeft = hub.constructionMonthsLeft
            if (hub.isConstructing) {
                val rem = hub.constructionMonthsLeft - 1
                if (rem <= 0) {
                    nextIsConstructing = false
                    nextMonthsLeft = 0
                } else {
                    nextMonthsLeft = rem
                }
            }

            val completedUpgrades = hub.activeUpgrades.toMutableList()
            val nextQueue = mutableListOf<com.example.data.HubConstructionItem>()
            hub.constructionQueue.forEach { item ->
                val remain = item.monthsRemaining - 1
                if (remain <= 0) {
                    completedUpgrades.add(item.upgradeId)
                } else {
                    nextQueue.add(item.copy(monthsRemaining = remain))
                }
            }
            hub.copy(
                activeUpgrades = completedUpgrades,
                constructionQueue = nextQueue,
                isConstructing = nextIsConstructing,
                constructionMonthsLeft = nextMonthsLeft
            )
        }

        var totalRev = 0L
        owned.flightRoutes.forEach { route ->
            val participatingPlanes = updatedFleet.filter { plane -> 
                route.assignedAircraftIds.contains(plane.id) && plane.status != "DELIVERING" 
            }
            if (participatingPlanes.isNotEmpty()) {
                val totalCapacityOnRoute = participatingPlanes.sumOf { plane ->
                    val pDef = com.example.data.AVIATION_AIRCRAFT_CATALOG.find { it.id == plane.modelId }
                        ?: com.example.data.DUMMY_AIRCRAFTS.find { it.id == plane.modelId }
                    val maxCap = pDef?.maxPax ?: when (plane.modelId) {
                        "atr72" -> 72
                        "a320" -> 180
                        "b777" -> 350
                        else -> 150
                    }
                    (maxCap * (0.3 + 0.7 * (plane.condition / 100.0))).toInt()
                }
                if (totalCapacityOnRoute > 0) {
                    val dailyPax = kotlin.math.min(route.baseDemand, totalCapacityOnRoute)
                    val monthlyPax = dailyPax * 30L
                    val routeRevenue = monthlyPax * route.ticketPrice
                    totalRev += routeRevenue
                }
            }
        }

        return owned.copy(
            airlineFleetComplex = updatedFleet,
            airlineHubsComplex = updatedHubs,
            customRevenue = totalRev
        )
    }

    fun processCruiseMonthlyTick(owned: com.example.data.OwnedBusiness): com.example.data.OwnedBusiness {
        var currentPrestige = owned.cruiseBrandPrestige
        val updatedShips = owned.cruiseShips?.map { ship ->
            com.example.viewmodel.CruiseEngine.processShipMonthly(ship, currentPrestige) { np ->
                currentPrestige = np
            }
        } ?: emptyList()
        return owned.copy(
            cruiseShips = updatedShips,
            cruiseBrandPrestige = currentPrestige
        )
    }

    fun calculateCruiseNetProfit(owned: com.example.data.OwnedBusiness): Long {
        val catalogItem = getCatalogItem(owned.catalogId, _playerState.value) ?: return 0L
        val totalRev = owned.cruiseShips?.sumOf { it.lastMonthTicketRevenue + it.lastMonthOnboardRevenue } ?: 0L
        val totalExp = (owned.cruiseShips?.sumOf { it.lastMonthExpenses } ?: 0L) + catalogItem.monthlyMaintenanceCost
        return totalRev - totalExp
    }

    fun orderCruiseShip(
        businessId: String,
        name: String,
        shipClass: com.example.data.CruiseShipClass,
        shipyard: com.example.data.ShipyardId
    ): Boolean {
        var success = false
        updateBusiness(businessId) { owned ->
            val finalPrice = (shipClass.basePrice * (1.0 + shipyard.costModifier)).toLong()
            if (owned.companyCash >= finalPrice) {
                success = true
                val totalBuild = (shipClass.baseBuildTime - shipyard.buildTimeReduction).coerceAtLeast(3)
                val newShip = com.example.data.CruiseShip(
                    name = name,
                    shipClass = shipClass,
                    shipyard = shipyard,
                    maxPax = shipClass.maxPax,
                    pricePaid = finalPrice,
                    monthsUntilDelivery = totalBuild,
                    totalBuildTime = totalBuild
                )
                owned.copy(
                    companyCash = owned.companyCash - finalPrice,
                    cruiseShips = (owned.cruiseShips ?: emptyList()) + newShip
                )
            } else {
                owned
            }
        }
        return success
    }

    fun renameCruiseShip(businessId: String, shipId: String, newName: String, customImageUrl: String?) {
        updateBusiness(businessId) { owned ->
            val updated = (owned.cruiseShips ?: emptyList()).map { ship ->
                if (ship.id == shipId) {
                    ship.copy(name = newName, customImageUrl = customImageUrl)
                } else ship
            }
            owned.copy(cruiseShips = updated)
        }
    }

    fun updateCruiseShipTicketPrices(
        businessId: String,
        shipId: String,
        regular: Long,
        vip: Long,
        vvip: Long,
        grandSuite: Long
    ) {
        updateBusiness(businessId) { owned ->
            val updated = (owned.cruiseShips ?: emptyList()).map { ship ->
                if (ship.id == shipId) {
                    ship.copy(
                        ticketPriceRegular = regular,
                        ticketPriceVip = vip,
                        ticketPriceVvip = vvip,
                        ticketPriceGrandSuite = grandSuite
                    )
                } else ship
            }
            owned.copy(cruiseShips = updated)
        }
    }

    fun scrapCruiseShip(businessId: String, shipId: String) {
        updateBusiness(businessId) { owned ->
            val targetShip = (owned.cruiseShips ?: emptyList()).find { it.id == shipId }
            if (targetShip != null) {
                val refund = (targetShip.pricePaid * 0.15).toLong()
                val updatedShips = (owned.cruiseShips ?: emptyList()).filter { it.id != shipId }
                owned.copy(
                    companyCash = owned.companyCash + refund,
                    cruiseShips = updatedShips
                )
            } else owned
        }
    }

    fun assignCruiseShipPort(businessId: String, shipId: String, portId: String?) {
        updateBusiness(businessId) { owned ->
            val updated = (owned.cruiseShips ?: emptyList()).map { ship ->
                if (ship.id == shipId) {
                    ship.copy(assignedPortId = portId)
                } else ship
            }
            owned.copy(cruiseShips = updated)
        }
    }

    fun buyCruiseFacility(businessId: String, shipId: String, facilityId: String): Boolean {
        var success = false
        val facility = com.example.data.CRUISE_FACILITIES_CATALOG.find { it.id == facilityId } ?: return false
        updateBusiness(businessId) { owned ->
            val ship = (owned.cruiseShips ?: emptyList()).find { it.id == shipId }
            if (ship != null && owned.companyCash >= facility.cost && !ship.builtFacilities.contains(facilityId)) {
                success = true
                val updatedShips = owned.cruiseShips.map { s ->
                    if (s.id == shipId) {
                        s.copy(builtFacilities = s.builtFacilities + facilityId)
                    } else s
                }
                owned.copy(
                    companyCash = owned.companyCash - facility.cost,
                    cruiseShips = updatedShips
                )
            } else {
                owned
            }
        }
        return success
    }

    fun sendCruiseShipToDrydock(businessId: String, shipId: String): Boolean {
        var success = false
        updateBusiness(businessId) { owned ->
            val ship = (owned.cruiseShips ?: emptyList()).find { it.id == shipId }
            if (ship != null && !ship.isUnderDrydock && ship.monthsUntilDelivery == 0) {
                success = true
                val updatedShips = owned.cruiseShips.map { s ->
                    if (s.id == shipId) {
                        s.copy(
                            isUnderDrydock = true,
                            drydockMonthsRemaining = 2,
                            lastMonthExpenses = (s.pricePaid * 0.02).toLong()
                        )
                    } else s
                }
                owned.copy(
                    cruiseShips = updatedShips
                )
            } else {
                owned
            }
        }
        return success
    }

    fun unlockCruisePort(businessId: String, portId: String, cost: Long): Boolean {
        var success = false
        updateBusiness(businessId) { owned ->
            if (owned.companyCash >= cost && !(owned.cruisePortsUnlocked ?: emptyList()).contains(portId)) {
                success = true
                owned.copy(
                    companyCash = owned.companyCash - cost,
                    cruisePortsUnlocked = (owned.cruisePortsUnlocked ?: emptyList()) + portId
                )
            } else owned
        }
        return success
    }

    fun buildHotelProperty(instanceId: String, name: String, location: String, tier: com.example.data.HotelTier) {
        val currentState = _playerState.value
        val cost = tier.baseBuildCost
        
        var foundInOwned = true
        var targetIndex = currentState.ownedBusinesses.indexOfFirst { it.instanceId == instanceId }
        var targetBusiness: com.example.data.OwnedBusiness? = null
        
        if (targetIndex != -1) {
            targetBusiness = currentState.ownedBusinesses[targetIndex]
        } else {
            foundInOwned = false
            for (holding in currentState.holdingCompanies) {
                targetIndex = holding.subsidiaries.indexOfFirst { it.instanceId == instanceId }
                if (targetIndex != -1) {
                    targetBusiness = holding.subsidiaries[targetIndex]
                    // We also need to get the holding company index to update it
                    break
                }
            }
        }
        
        if (targetBusiness == null) return
        
        if (targetBusiness.companyCash >= cost) {
            val newProp = com.example.data.HotelProperty(
                name = name,
                location = location,
                tier = tier,
                isConstructing = true,
                remainingBuildMonths = tier.buildMonths,
                customRoomRate = tier.baseRoomRate,
                builtFacilities = mutableListOf()
            )
            
            val updatedHospitality = targetBusiness.hospitalityProperties + newProp
            val updatedBusiness = targetBusiness.copy(
                companyCash = targetBusiness.companyCash - cost,
                hospitalityProperties = updatedHospitality
            )
            
            if (foundInOwned) {
                val newList = currentState.ownedBusinesses.toMutableList()
                newList[currentState.ownedBusinesses.indexOfFirst { it.instanceId == instanceId }] = updatedBusiness
                _playerState.value = currentState.copy(ownedBusinesses = newList)
            } else {
                val newHoldings = currentState.holdingCompanies.toMutableList()
                val holdingIdx = newHoldings.indexOfFirst { it.subsidiaries.any { s -> s.instanceId == instanceId } }
                if (holdingIdx != -1) {
                    val holding = newHoldings[holdingIdx]
                    val subIdx = holding.subsidiaries.indexOfFirst { it.instanceId == instanceId }
                    val newSubs = holding.subsidiaries.toMutableList()
                    newSubs[subIdx] = updatedBusiness
                    newHoldings[holdingIdx] = holding.copy(subsidiaries = newSubs)
                    _playerState.value = currentState.copy(holdingCompanies = newHoldings)
                }
            }
            saveState(_playerState.value)
        }
    }

    fun updateHotelImage(instanceId: String, hotelId: String, imageUrl: String) {
        val currentState = _playerState.value
        fun updateBusinessList(businesses: List<com.example.data.OwnedBusiness>): Pair<List<com.example.data.OwnedBusiness>, Boolean> {
            var changed = false
            val newList = businesses.map { bus ->
                if (bus.instanceId == instanceId) {
                    val ph = bus.hospitalityProperties.map { h ->
                        if (h.id == hotelId) h.copy(imageUrl = imageUrl) else h
                    }
                    changed = true
                    bus.copy(hospitalityProperties = ph)
                } else bus
            }
            return Pair(newList, changed)
        }
        val (newOwned, ownedChanged) = updateBusinessList(currentState.ownedBusinesses)
        if (ownedChanged) {
            _playerState.value = currentState.copy(ownedBusinesses = newOwned)
            saveState(_playerState.value)
            return
        }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val (newSubs, subChanged) = updateBusinessList(holding.subsidiaries)
            holding.copy(subsidiaries = newSubs)
        }
        _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        saveState(_playerState.value)
    }

    fun updateHotelRoomStrategy(instanceId: String, hotelId: String, strategy: String) {
        val currentState = _playerState.value
        fun updateBusinessList(businesses: List<com.example.data.OwnedBusiness>): Pair<List<com.example.data.OwnedBusiness>, Boolean> {
            var changed = false
            val newList = businesses.map { bus ->
                if (bus.instanceId == instanceId) {
                    val ph = bus.hospitalityProperties.map { h ->
                        if (h.id == hotelId) h.copy(targetRoomStrategy = strategy) else h
                    }
                    changed = true
                    bus.copy(hospitalityProperties = ph)
                } else bus
            }
            return Pair(newList, changed)
        }
        val (newOwned, ownedChanged) = updateBusinessList(currentState.ownedBusinesses)
        if (ownedChanged) {
            _playerState.value = currentState.copy(ownedBusinesses = newOwned)
            saveState(_playerState.value)
            return
        }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val (newSubs, subChanged) = updateBusinessList(holding.subsidiaries)
            holding.copy(subsidiaries = newSubs)
        }
        _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        saveState(_playerState.value)
    }

    fun updateRoomClassConfig(instanceId: String, hotelId: String, roomClassName: String, config: com.example.data.RoomClassConfig) {
        val currentState = _playerState.value
        fun updateBusinessList(businesses: List<com.example.data.OwnedBusiness>): Pair<List<com.example.data.OwnedBusiness>, Boolean> {
            var changed = false
            val newList = businesses.map { bus ->
                if (bus.instanceId == instanceId) {
                    val ph = bus.hospitalityProperties.map { h ->
                        if (h.id == hotelId) {
                            val newConfigs = h.roomConfigs?.toMutableMap() ?: mutableMapOf()
                            newConfigs[roomClassName] = config
                            h.copy(roomConfigs = newConfigs)
                        } else h
                    }
                    changed = true
                    bus.copy(hospitalityProperties = ph)
                } else bus
            }
            return Pair(newList, changed)
        }
        val (newOwned, ownedChanged) = updateBusinessList(currentState.ownedBusinesses)
        if (ownedChanged) {
            _playerState.value = currentState.copy(ownedBusinesses = newOwned)
            saveState(_playerState.value)
            return
        }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val (newSubs, subChanged) = updateBusinessList(holding.subsidiaries)
            holding.copy(subsidiaries = newSubs)
        }
        _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        saveState(_playerState.value)
    }

    fun updateHotelRoomRate(instanceId: String, hotelId: String, newRate: Long) {
        val currentState = _playerState.value
        
        fun updateBusinessList(businesses: List<com.example.data.OwnedBusiness>): Pair<List<com.example.data.OwnedBusiness>, Boolean> {
            var changed = false
            val newList = businesses.map { bus ->
                if (bus.instanceId == instanceId) {
                    val ph = bus.hospitalityProperties.map { h ->
                        if (h.id == hotelId) h.copy(customRoomRate = newRate) else h
                    }
                    changed = true
                    bus.copy(hospitalityProperties = ph)
                } else bus
            }
            return Pair(newList, changed)
        }
        
        val (newOwned, ownedChanged) = updateBusinessList(currentState.ownedBusinesses)
        if (ownedChanged) {
            _playerState.value = currentState.copy(ownedBusinesses = newOwned)
            saveState(_playerState.value)
            return
        }
        
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val (newSubs, subChanged) = updateBusinessList(holding.subsidiaries)
            if (subChanged) holding.copy(subsidiaries = newSubs) else holding
        }
        _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        saveState(_playerState.value)
    }

    fun buildHotelFacility(instanceId: String, hotelId: String, facility: com.example.data.HotelFacility) {
        val currentState = _playerState.value
        val cost = facility.buildCost
        
        fun updateBusinessList(businesses: List<com.example.data.OwnedBusiness>): Pair<List<com.example.data.OwnedBusiness>, Boolean> {
            var changed = false
            val newList = businesses.map { bus ->
                if (bus.instanceId == instanceId && bus.companyCash >= cost) {
                    val ph = bus.hospitalityProperties.map { h ->
                        if (h.id == hotelId && !h.builtFacilities.contains(facility)) {
                            val bf = h.builtFacilities.toMutableList()
                            bf.add(facility)
                            changed = true
                            h.copy(builtFacilities = bf)
                        } else h
                    }
                    if (changed) {
                        bus.copy(companyCash = bus.companyCash - cost, hospitalityProperties = ph)
                    } else bus
                } else bus
            }
            return Pair(newList, changed)
        }
        
        val (newOwned, ownedChanged) = updateBusinessList(currentState.ownedBusinesses)
        if (ownedChanged) {
            _playerState.value = currentState.copy(ownedBusinesses = newOwned)
            saveState(_playerState.value)
            return
        }
        
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val (newSubs, subChanged) = updateBusinessList(holding.subsidiaries)
            if (subChanged) holding.copy(subsidiaries = newSubs) else holding
        }
        _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        saveState(_playerState.value)
    }
    fun addDivisionToAcquiredBusiness(parentInstanceId: String, catalogId: String, customName: String?, cost: Long): String? {
        val currentState = _playerState.value
        val parent = currentState.ownedBusinesses.find { it.instanceId == parentInstanceId }
        if (parent == null) {
            return "Perusahaan induk tidak ditemukan."
        }
        if (parent.acquiredStockTicker == null) {
            return "Perusahaan induk bukan perusahaan hasil akuisisi publik."
        }
        if (parent.companyCash < cost) {
            val neededStr = com.example.ui.formatCurrencyRingkas(cost, false)
            val currentStr = com.example.ui.formatCurrencyRingkas(parent.companyCash.toLong(), false)
            return "Kas internal perusahaan tidak mencukupi (Butuh $neededStr, Kas: $currentStr). Silakan lakukan suntik dana terlebih dahulu."
        }
        
        val newDivision = com.example.data.OwnedBusiness(
            catalogId = catalogId,
            customName = customName,
            level = 1,
            companyCash = 0.0
        )
        
        val updatedParent = parent.copy(
            companyCash = parent.companyCash - cost,
            subsidiaries = parent.subsidiaries + newDivision
        )
        
        val updatedOwnedList = currentState.ownedBusinesses.map {
            if (it.instanceId == parentInstanceId) updatedParent else it
        }
        
        _playerState.value = currentState.copy(
            ownedBusinesses = updatedOwnedList
        )
        saveState(_playerState.value)
        return null
    }

    fun purchaseUpgrade(instanceId: String, upgradeId: String): String? {
        val currentState = _playerState.value
        
        // Find if it's in regular businesses
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null
        
        if (owned == null) {
            // Check in holdings
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = holding.instanceId
                    break
                }
            }
        }
        
        if (owned == null) return "Bisnis tidak ditemukan"

        val catalogItem = getCatalogItem(owned.catalogId, currentState) ?: return "Katalog bisnis tidak ditemukan"
        val upgrade = catalogItem.upgrades.find { it.id == upgradeId } ?: return "Upgrade tidak ditemukan"

        val currentLevel = owned.upgradeLevels[upgradeId] ?: if (owned.purchasedUpgrades.contains(upgradeId)) 1 else 0
        if (currentLevel >= upgrade.maxLevel) return "Level upgrade sudah maksimal"

        var costMultiplierTotal = 1.0f
        repeat(currentLevel) { costMultiplierTotal *= upgrade.costMultiplier }
        val cost = (upgrade.baseCost * costMultiplierTotal).toLong()

        if (owned.companyCash < cost) {
            val shortCost = com.example.ui.formatCurrencyRingkas(cost, false)
            val shortCash = com.example.ui.formatCurrencyRingkas(owned.companyCash.toLong(), false)
            return "Kas Perusahaan tidak mencukupi! (Kas: $shortCash, Butuh: $shortCost). Silakan suntik modal dari dompet pribadi."
        }

        val durationMs = (60000L * Math.pow(1.3, currentLevel.toDouble())).toLong()
        val now = System.currentTimeMillis()
        
        val newActiveUpgrade = ActiveUpgrade(
            selectedUpgradeId = upgradeId,
            targetLevel = currentLevel + 1,
            startTimeMs = now,
            finishTimeMs = now + durationMs
        )

        val newOwned = owned.copy(
            activeUpgrades = owned.activeUpgrades + newActiveUpgrade,
            companyCash = owned.companyCash - cost
        )
        
        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map { 
                        if (it.instanceId == instanceId) newOwned else it 
                    }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            _playerState.value = currentState.copy(
                holdingCompanies = newHoldings
            )
        } else {
            _playerState.value = currentState.copy(
                ownedBusinesses = currentState.ownedBusinesses.map {
                    if (it.instanceId == instanceId) newOwned else it
                }
            )
        }
        saveState(_playerState.value)
        return null
    }

    fun startParentBusinessRealtimeUpgrade(instanceId: String, cost: Long): String? {
        val currentState = _playerState.value
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
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
        
        if (owned == null) return "Bisnis tidak ditemukan"
        if (owned.level >= 50) return "Level bisnis sudah maksimal (Level 50)"
        if (owned.isUpgradingRealTime) return "Bisnis sedang dalam proses upgrade"
        
        if (owned.companyCash < cost) {
            val shortCost = com.example.ui.formatCurrencyRingkas(cost, false)
            val shortCash = com.example.ui.formatCurrencyRingkas(owned.companyCash.toLong(), false)
            return "Kas Perusahaan tidak mencukupi! (Kas: $shortCash, Butuh: $shortCost). Silakan suntik modal dari dompet pribadi."
        }

        val durationInSeconds = 30 + (owned.level * 12)
        val newOwned = owned.copy(
            companyCash = owned.companyCash - cost,
            isUpgradingRealTime = true,
            upgradeEndTimeRealTime = System.currentTimeMillis() + (durationInSeconds * 1000L)
        )
        
        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map { 
                        if (it.instanceId == instanceId) newOwned else it 
                    }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            _playerState.value = currentState.copy(
                holdingCompanies = newHoldings
            )
        } else {
            _playerState.value = currentState.copy(
                ownedBusinesses = currentState.ownedBusinesses.map {
                    if (it.instanceId == instanceId) newOwned else it
                }
            )
        }
        saveState(_playerState.value)
        return null
    }

    fun finishBusinessRealtimeUpgrade(instanceId: String) {
        val currentState = _playerState.value
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
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
        
        if (owned == null) return
        if (!owned.isUpgradingRealTime) return
        
        val newOwned = owned.copy(
            level = owned.level + 1,
            isUpgradingRealTime = false,
            upgradeEndTimeRealTime = 0L
        )
        
        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map { 
                        if (it.instanceId == instanceId) newOwned else it 
                    }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            _playerState.value = currentState.copy(
                holdingCompanies = newHoldings
            )
        } else {
            _playerState.value = currentState.copy(
                ownedBusinesses = currentState.ownedBusinesses.map {
                    if (it.instanceId == instanceId) newOwned else it
                }
            )
        }
        saveState(_playerState.value)
    }

    fun produceMovie(
        instanceId: String,
        title: String,
        budget: Long,
        promoBudget: Long,
        genres: List<String>,
        isGlobal: Boolean,
        schedMonth: Int? = null,
        schedYear: Int? = null,
        filmFormat: String = "Feature Film",
        productionFocus: String = "REGULER",
        scheduledReleaseDate: String? = null,
        targetDurationMonths: Int? = null
    ): Boolean {
        val (success, newState) = filmStudioRepository.produceMovie(
            currentState = _playerState.value,
            instanceId = instanceId,
            title = title,
            budget = budget,
            promoBudget = promoBudget,
            genres = genres,
            isGlobal = isGlobal,
            schedMonth = schedMonth,
            schedYear = schedYear,
            filmFormat = filmFormat,
            productionFocus = productionFocus,
            scheduledReleaseDate = scheduledReleaseDate,
            targetDurationMonths = targetDurationMonths ?: 0
        )
        if (success) {
            _playerState.value = newState
            saveState(_playerState.value)
        }
        return success
    }

    fun cancelMovieProject(instanceId: String, projectTitle: String, refundAmount: Long) {
        _playerState.value = filmStudioRepository.cancelMovieProject(
            currentState = _playerState.value,
            instanceId = instanceId,
            projectTitle = projectTitle,
            refundAmount = refundAmount
        )
        saveState(_playerState.value)
    }

    fun polishMovieProject(instanceId: String, projectTitle: String, budgetCost: Long, extraMonths: Int) {
        val (success, newState) = filmStudioRepository.polishMovieProject(
            currentState = _playerState.value,
            instanceId = instanceId,
            projectTitle = projectTitle,
            budgetCost = budgetCost,
            extraMonths = extraMonths
        )
        if (success) {
            _playerState.value = newState
            saveState(_playerState.value)
        }
    }

    fun scheduleMovieRelease(instanceId: String, projectTitle: String, schedStr: String) {
        _playerState.value = filmStudioRepository.scheduleMovieRelease(
            currentState = _playerState.value,
            instanceId = instanceId,
            projectTitle = projectTitle,
            schedStr = schedStr
        )
        saveState(_playerState.value)
    }

    fun getBookedTimeSlots(): List<String> {
        return _playerState.value.activeTvPrograms.filter { it.active }.flatMap { it.timeSlots }
    }

    fun updateTvProgramSchedule(programId: String, newTimeSlots: List<String>) {
        val currentState = _playerState.value
        val updatedPrograms = currentState.activeTvPrograms.map {
            if (it.id == programId) it.copy(timeSlots = newTimeSlots) else it
        }
        _playerState.value = currentState.copy(activeTvPrograms = updatedPrograms)
        saveState(_playerState.value)
    }

    fun updateTvStation(instanceId: String, costCompanyCash: Long = 0L, mapper: (com.example.data.TvStationData) -> com.example.data.TvStationData): Boolean {
        val currentState = _playerState.value
        var found = false
        
        val newBusinesses = currentState.ownedBusinesses.map { biz ->
            if (biz.instanceId == instanceId) {
                if (biz.companyCash < costCompanyCash) return false
                found = true
                val newTvData = mapper(biz.tvStationData)
                biz.copy(
                    companyCash = biz.companyCash - costCompanyCash,
                    tvStationData = newTvData
                )
            } else biz
        }
        
        if (found) {
            _playerState.value = syncTvValuation(currentState.copy(ownedBusinesses = newBusinesses))
            saveState(_playerState.value)
            return true
        }
        
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries.map { biz ->
                if (biz.instanceId == instanceId) {
                    if (biz.companyCash < costCompanyCash) return false
                    found = true
                    val newTvData = mapper(biz.tvStationData)
                    biz.copy(
                        companyCash = biz.companyCash - costCompanyCash,
                        tvStationData = newTvData
                    )
                } else biz
            }
            holding.copy(subsidiaries = newSubs)
        }
        
        if (found) {
            _playerState.value = syncTvValuation(currentState.copy(holdingCompanies = newHoldings))
            saveState(_playerState.value)
            return true
        }
        return false
    }

    fun buildTvFacility(instanceId: String, type: com.example.data.TvFacilityType, customName: String): Pair<Boolean, String> {
        val facilityName = customName.trim().ifEmpty { type.displayName }
        val newFacility = com.example.data.TvStudioFacility(
            name = facilityName,
            type = type
        )
        val success = updateTvStation(instanceId, costCompanyCash = type.buildCost) { currentData ->
            currentData.copy(facilities = currentData.facilities + newFacility)
        }
        return if (success) {
            Pair(true, "Berhasil membangun ${newFacility.name} (${type.buildCost})")
        } else {
            Pair(false, "Kas internal stasiun TV tidak mencukupi untuk biaya pembangunan (${type.buildCost})")
        }
    }

    fun demolishTvFacility(instanceId: String, facilityId: String): Pair<Boolean, String> {
        val currentState = _playerState.value
        val activeProg = currentState.activeTvPrograms.find { it.active && it.assignedStudioId == facilityId }
        if (activeProg != null) {
            return Pair(false, "Tidak bisa membongkar fasilitas! Studio ini sedang digunakan oleh program aktif '${activeProg.title}'.")
        }
        val success = updateTvStation(instanceId, costCompanyCash = 0L) { currentData ->
            currentData.copy(facilities = currentData.facilities.filter { it.id != facilityId })
        }
        return if (success) Pair(true, "Fasilitas studio berhasil dibongkar.") else Pair(false, "Gagal membongkar studio.")
    }

    fun renameTvFacility(instanceId: String, facilityId: String, newName: String): Pair<Boolean, String> {
        if (newName.isBlank()) return Pair(false, "Nama studio tidak boleh kosong.")
        val success = updateTvStation(instanceId, costCompanyCash = 0L) { currentData ->
            currentData.copy(facilities = currentData.facilities.map { 
                if (it.id == facilityId) it.copy(name = newName.trim()) else it 
            })
        }
        return if (success) Pair(true, "Nama studio berhasil diperbarui.") else Pair(false, "Gagal mengubah nama.")
    }

    fun hireTvDirector(instanceId: String, role: com.example.data.TvDirectorRole): Pair<Boolean, String> {
        val success = updateTvStation(instanceId, costCompanyCash = 0L) { currentData ->
            currentData.copy(hiredDirectors = currentData.hiredDirectors + role.name)
        }
        return if (success) Pair(true, "Berhasil merekrut ${role.displayName}!") else Pair(false, "Gagal merekrut direktur.")
    }

    fun fireTvDirector(instanceId: String, role: com.example.data.TvDirectorRole): Pair<Boolean, String> {
        val currentState = _playerState.value
        val reliantProgs = currentState.activeTvPrograms.filter { it.active && com.example.data.getRequiredDirectorRole(it.type) == role }
        if (reliantProgs.isNotEmpty()) {
            val names = reliantProgs.take(2).joinToString { it.title }
            return Pair(false, "Tidak bisa memberhentikan direktur! Program aktif ($names) masih berjalan di bawah divisinya.")
        }
        val success = updateTvStation(instanceId, costCompanyCash = 0L) { currentData ->
            currentData.copy(hiredDirectors = currentData.hiredDirectors - role.name)
        }
        return if (success) Pair(true, "${role.displayName} berhasil diberhentikan.") else Pair(false, "Gagal memberhentikan direktur.")
    }

    fun hireTvCrews(instanceId: String, amount: Int): Pair<Boolean, String> {
        if (amount <= 0) return Pair(false, "Jumlah kru tidak valid.")
        val hiringCost = amount * 1_000L
        val success = updateTvStation(instanceId, costCompanyCash = hiringCost) { currentData ->
            currentData.copy(totalCrews = currentData.totalCrews + amount)
        }
        return if (success) {
            Pair(true, "Berhasil merekrut $amount kru produksi baru (Biaya rekrut: $hiringCost).")
        } else {
            Pair(false, "Kas internal tidak cukup untuk biaya perekrutan kru ($hiringCost).")
        }
    }

    fun layoffTvCrews(instanceId: String, amount: Int): Pair<Boolean, String> {
        val currentState = _playerState.value
        var biz = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        if (biz == null) {
            for (h in currentState.holdingCompanies) {
                biz = h.subsidiaries.find { it.instanceId == instanceId }
                if (biz != null) break
            }
        }
        val currentCrews = biz?.tvStationData?.totalCrews ?: 0
        val usedCrews = currentState.activeTvPrograms.filter { it.active }.sumOf { it.requiredCrews }
        if (currentCrews - amount < usedCrews) {
            return Pair(false, "Tidak bisa memangkas kru! Minimal $usedCrews kru masih bertugas di program aktif.")
        }
        if (currentCrews - amount < 5) {
            return Pair(false, "Stasiun TV wajib mempertahankan minimal 5 kru operasional.")
        }
        val success = updateTvStation(instanceId, costCompanyCash = 0L) { currentData ->
            currentData.copy(totalCrews = maxOf(5, currentData.totalCrews - amount))
        }
        return if (success) Pair(true, "Berhasil merampingkan $amount kru operasional.") else Pair(false, "Gagal merampingkan kru.")
    }

    fun acquireDewanPersCertification(instanceId: String): Pair<Boolean, String> {
        val certCost = 50_000L
        val currentState = _playerState.value
        var biz = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        if (biz == null) {
            for (h in currentState.holdingCompanies) {
                biz = h.subsidiaries.find { it.instanceId == instanceId }
                if (biz != null) break
            }
        }
        val tvData = biz?.tvStationData ?: com.example.data.TvStationData()
        if (tvData.reputation < 50.0) {
            return Pair(false, "Reputasi stasiun TV minimal 50.0 untuk Sertifikasi Dewan Pers (Saat ini: ${tvData.reputation.toInt()}).")
        }
        val hasNewsroom = tvData.facilities.any { it.type == com.example.data.TvFacilityType.NEWSROOM }
        if (!hasNewsroom) {
            return Pair(false, "Wajib memiliki minimal 1 fasilitas 'Newsroom & Studio Berita'!")
        }
        val success = updateTvStation(instanceId, costCompanyCash = certCost) { currentData ->
            currentData.copy(
                dewanPersCertified = true,
                reputation = currentData.reputation + 10.0
            )
        }
        return if (success) Pair(true, "Selamat! Stasiun TV resmi tersertifikasi Dewan Pers.") else Pair(false, "Kas internal tidak cukup ($certCost).")
    }

    fun acquireNationalBroadcastLicense(instanceId: String): Pair<Boolean, String> {
        val licenseCost = 75_000L
        val currentState = _playerState.value
        var biz = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        if (biz == null) {
            for (h in currentState.holdingCompanies) {
                biz = h.subsidiaries.find { it.instanceId == instanceId }
                if (biz != null) break
            }
        }
        if ((biz?.level ?: 1) < 2) {
            return Pair(false, "Stasiun TV harus minimal Level 2 untuk Lisensi Penyiaran Nasional!")
        }
        val success = updateTvStation(instanceId, costCompanyCash = licenseCost) { currentData ->
            currentData.copy(nationalBroadcastLicense = true)
        }
        return if (success) Pair(true, "Lisensi Penyiaran Terestrial Nasional Resmi Terbit!") else Pair(false, "Kas internal tidak cukup ($licenseCost).")
    }

    fun buildRegionalTransmissionTower(instanceId: String, regionKey: String): Pair<Boolean, String> {
        val region = try {
            com.example.data.TvRegionalTransmission.valueOf(regionKey)
        } catch (e: Exception) {
            return Pair(false, "Wilayah transmisi tidak ditemukan.")
        }
        val success = updateTvStation(instanceId, costCompanyCash = region.buildCost) { currentData ->
            currentData.copy(unlockedTransmissions = currentData.unlockedTransmissions + regionKey)
        }
        return if (success) {
            Pair(true, "Berhasil membangun ${region.displayName}! Jangkauan +${region.populationCoverage}.")
        } else {
            Pair(false, "Kas internal tidak cukup untuk pembangunan menara ini (${region.buildCost}).")
        }
    }

    fun addTvProgramWithDetails(
        instanceId: String,
        title: String,
        type: String,
        productionCost: Double,
        isPremiumRights: Boolean = false,
        finalCost: Long = productionCost.toLong(),
        durationMonths: Int = -1,
        timeSlots: List<String> = emptyList(),
        assignedStudioId: String? = null
    ): Pair<Boolean, String> {
        val currentState = _playerState.value
        
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        var isNested = false
        var holdingId: String? = null
        if (owned == null) {
            for (holding in currentState.holdingCompanies) {
                owned = holding.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) { isNested = true; holdingId = holding.instanceId; break }
            }
        }
        if (owned == null) return Pair(false, "Unit bisnis TV tidak ditemukan.")
        
        if (title.isBlank()) return Pair(false, "Judul program tidak boleh kosong.")
        if (timeSlots.isEmpty()) return Pair(false, "Pilih minimal 1 slot jam tayang!")

        if (currentState.activeTvPrograms.any { it.title.equals(title.trim(), ignoreCase = true) } || 
            currentState.ipLibraryHistory.any { it.title.equals(title.trim(), ignoreCase = true) }) {
            return Pair(false, "Judul program '$title' sudah pernah digunakan.")
        }

        if (owned.companyCash < finalCost) {
            return Pair(false, "Kas internal stasiun TV tidak mencukupi ($finalCost dibutuhkan).")
        }

        val tvData = owned.tvStationData

        // 1. Director requirement check
        if (!isPremiumRights) {
            val reqDirector = com.example.data.getRequiredDirectorRole(type)
            if (reqDirector != null && !tvData.hiredDirectors.contains(reqDirector.name)) {
                return Pair(false, "Wajib merekrut ${reqDirector.displayName} (${reqDirector.titleRole}) di tab Struktur Organisasi!")
            }

            // 2. Dewan Pers certification check
            if (com.example.data.isDewanPersRequired(type) && !tvData.dewanPersCertified) {
                return Pair(false, "Wajib memiliki Sertifikasi Dewan Pers di tab Lisensi & Transmisi untuk program Berita / Investigasi!")
            }

            // 3. Physical Studio check
            if (assignedStudioId == null) {
                return Pair(false, "Pilih Studio Fisik yang tersedia untuk memproduksi program ini!")
            }
            val studio = tvData.facilities.find { it.id == assignedStudioId }
                ?: return Pair(false, "Studio yang dipilih tidak ditemukan.")

            val compatTypes = com.example.data.getCompatibleStudioTypes(type)
            if (!compatTypes.contains(studio.type)) {
                return Pair(false, "Studio '${studio.name}' (${studio.type.displayName}) tidak kompatibel untuk genre $type!")
            }

            // Schedule clash check on the same studio
            val clashProg = currentState.activeTvPrograms.find { prog ->
                prog.active && prog.assignedStudioId == assignedStudioId && prog.timeSlots.any { slot -> timeSlots.contains(slot) }
            }
            if (clashProg != null) {
                return Pair(false, "Jadwal Bentrok! Studio '${studio.name}' sudah digunakan oleh '${clashProg.title}' pada jam tersebut.")
            }

            // 4. Crew requirement check
            val reqCrews = com.example.data.getRequiredCrewsForProgram(type)
            val usedCrews = currentState.activeTvPrograms.filter { it.active }.sumOf { it.requiredCrews }
            if (usedCrews + reqCrews > tvData.totalCrews) {
                val availableCrews = maxOf(0, tvData.totalCrews - usedCrews)
                return Pair(false, "Kru produksi tidak cukup! Butuh $reqCrews kru (Tersedia: $availableCrews kru standby). Rekrut kru di tab Struktur Organisasi.")
            }

            // 5. Broadcast simultaneous program capacity
            val activeCount = currentState.activeTvPrograms.count { it.active }
            if (activeCount >= tvData.maxSimultaneousPrograms) {
                return Pair(false, "Kapasitas transmisi penuh! Bangun Master Control Room tambahan di tab Manajemen Fasilitas.")
            }
        }

        val baseRating = if (isPremiumRights) {
            80.0 + (Math.random() * 15.0) // 80 - 95%
        } else {
            when (type) {
                "Pencarian Bakat (Talent Show)" -> 40.0 + (Math.random() * 40.0)
                "Investigasi Kriminal" -> 20.0 + (Math.random() * 20.0)
                "Sinetron" -> 25.0 + (Math.random() * 35.0)
                "Reality Show" -> 20.0 + (Math.random() * 40.0)
                "Berita" -> 25.0 + (Math.random() * 25.0)
                else -> 15.0 + (Math.random() * 35.0)
            }
        }

        val effectiveRating = (baseRating + tvData.totalTransmissionRatingBonus).coerceIn(1.0, 99.0)
        
        val timeMuls = timeSlots.map { slot ->
            val hour = slot.substringBefore(":").toIntOrNull() ?: 12
            val isHalfHour = slot.substringAfter(":") == "30"
            val minutes = hour * 60 + (if (isHalfHour) 30 else 0)
            
            if (minutes in 6 * 60..11 * 60 + 30) 1.0
            else if (minutes in 12 * 60..17 * 60 + 30) 0.6
            else if (minutes in 18 * 60..22 * 60 + 30) 2.5
            else 0.3
        }
        val avgMultiplier = if (timeMuls.isNotEmpty()) timeMuls.average() else 1.0

        val adRevenue = (productionCost * (effectiveRating / 10.0)) * avgMultiplier * tvData.totalTransmissionRevenueMultiplier

        val selectedStudio = tvData.facilities.find { it.id == assignedStudioId }
        val reqCrews = if (isPremiumRights) 10 else com.example.data.getRequiredCrewsForProgram(type)

        val newProgram = com.example.data.TvProgram(
            id = java.util.UUID.randomUUID().toString(),
            title = title.trim(),
            type = type,
            productionCost = productionCost,
            monthlyAdRevenue = adRevenue,
            rating = effectiveRating,
            active = true,
            remainingMonths = durationMonths,
            timeSlots = timeSlots,
            assignedStudioId = assignedStudioId,
            assignedStudioName = selectedStudio?.name,
            requiredCrews = reqCrews
        )
        val newList = currentState.activeTvPrograms + newProgram
        
        val newOwned = owned.copy(companyCash = owned.companyCash - finalCost)

        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    holding.copy(subsidiaries = holding.subsidiaries.map { if (it.instanceId == instanceId) newOwned else it })
                } else holding
            }
            _playerState.value = currentState.copy(
                activeTvPrograms = newList,
                holdingCompanies = newHoldings
            )
        } else {
            _playerState.value = currentState.copy(
                activeTvPrograms = newList,
                ownedBusinesses = currentState.ownedBusinesses.map { if (it.instanceId == instanceId) newOwned else it }
            )
        }

        _playerState.value = syncTvValuation(_playerState.value)
        saveState(_playerState.value)
        return Pair(true, "Program '$title' berhasil mengudara!")
    }

    fun addTvProgram(
        instanceId: String,
        title: String,
        type: String,
        productionCost: Double,
        isPremiumRights: Boolean = false,
        finalCost: Long = productionCost.toLong(),
        durationMonths: Int = -1,
        timeSlots: List<String> = emptyList(),
        assignedStudioId: String? = null
    ): Boolean {
        val result = addTvProgramWithDetails(
            instanceId = instanceId,
            title = title,
            type = type,
            productionCost = productionCost,
            isPremiumRights = isPremiumRights,
            finalCost = finalCost,
            durationMonths = durationMonths,
            timeSlots = timeSlots,
            assignedStudioId = assignedStudioId
        )
        return result.first
    }

    fun cancelTvProgram(programId: String) {
        val currentState = _playerState.value
        val progToCancel = currentState.activeTvPrograms.find { it.id == programId }
        val updatedPrograms = currentState.activeTvPrograms.filter { it.id != programId }
        
        if (progToCancel != null && progToCancel.isOriginalIP) {
            val archivedProg = progToCancel.copy(active = false, remainingMonths = 0)
            val newIpLibrary = currentState.ipLibraryHistory + archivedProg
            _playerState.value = currentState.copy(activeTvPrograms = updatedPrograms, ipLibraryHistory = newIpLibrary)
        } else {
            _playerState.value = currentState.copy(activeTvPrograms = updatedPrograms)
        }
        
        _playerState.value = syncTvValuation(_playerState.value)
        saveState(_playerState.value)
    }

    fun rebootTvProgram(programId: String) {
        val currentState = _playerState.value
        val archivedProg = currentState.ipLibraryHistory.find { it.id == programId } ?: return
        
        val rebootCost = (archivedProg.productionCost * 2).toLong()
        if (currentState.cash >= rebootCost) {
            val newRating = when (archivedProg.type) {
                "Pencarian Bakat (Talent Show)" -> 40.0 + (Math.random() * 40.0) 
                "Investigasi Kriminal" -> Math.random() * 20.0
                "Sinetron" -> 10.0 + (Math.random() * 30.0) 
                "Reality Show" -> 5.0 + (Math.random() * 40.0)
                else -> Math.random() * 40.0 
            }
            val newAdRevenue = archivedProg.productionCost * (newRating / 10.0)
            
            val rebootedProg = archivedProg.copy(
                active = true,
                rating = newRating,
                monthlyAdRevenue = newAdRevenue,
                totalAccumulatedProfit = 0.0,
                monthsAired = 0,
                remainingMonths = -1
            )
            
            val updatedIpLibrary = currentState.ipLibraryHistory.filter { it.id != programId }
            val updatedActive = currentState.activeTvPrograms + rebootedProg
            
            _playerState.value = currentState.copy(
                cash = currentState.cash - rebootCost,
                ipLibraryHistory = updatedIpLibrary,
                activeTvPrograms = updatedActive
            )
            _playerState.value = syncTvValuation(_playerState.value)
            saveState(_playerState.value)
        }
    }

    fun startStreamingLicense(instanceId: String, title: String, licenseeName: String, fee: Long, duration: Int) {
        val newState = filmStudioRepository.startStreamingLicense(
            currentState = _playerState.value,
            instanceId = instanceId,
            title = title,
            licenseeName = licenseeName,
            fee = fee,
            duration = duration
        )
        _playerState.value = syncTvValuation(newState)
        saveState(_playerState.value)
    }

    fun sellMovieIp(instanceId: String, title: String, sellPrice: Long) {
        _playerState.value = filmStudioRepository.sellMovieIp(
            currentState = _playerState.value,
            instanceId = instanceId,
            title = title,
            sellPrice = sellPrice
        )
        saveState(_playerState.value)
    }

    fun sellTvIp(programId: String, sellPrice: Long) {
        val currentState = _playerState.value
        val prog = currentState.ipLibraryHistory.find { it.id == programId }
        val ownedTv = currentState.ownedBusinesses.find { getCatalogItem(it.catalogId, currentState)?.id == "media_tv" }

        if (prog != null) {
            val updatedHistory = currentState.ipLibraryHistory.filter { it.id != programId }

            _playerState.value = currentState.copy(
                ipLibraryHistory = updatedHistory,
                cash = currentState.cash + sellPrice
            )
            _playerState.value = syncTvValuation(_playerState.value)
            saveState(_playerState.value)
        }
    }

    fun startAppProject(
        title: String,
        type: com.example.data.ProjectType,
        budgetCost: Double,
        targetRevenue: Double,
        devTimeMonths: Int,
        targetBusinessId: String? = null
    ) {
        val currentState = _playerState.value
        val newProject = com.example.data.AppProject(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            type = type,
            budgetCost = budgetCost,
            targetRevenue = targetRevenue,
            devTimeMonths = devTimeMonths,
            targetBusinessId = targetBusinessId,
            kanbanColumn = "IN_PROGRESS",
            status = com.example.data.ProjectStatus.DEVELOPMENT,
            isAssigned = true
        )
        _playerState.value = currentState.copy(
            appProjects = currentState.appProjects + newProject
        )
        saveState(_playerState.value)
    }

    fun addProjectToBacklog(
        title: String,
        type: com.example.data.ProjectType,
        budgetCost: Double,
        targetRevenue: Double,
        devTimeMonths: Int,
        targetBusinessId: String? = null,
        reqUiUx: Int = 1,
        reqFrontend: Int = 1,
        reqBackend: Int = 1,
        description: String = "",
        clientName: String = "Klien Korporat",
        categoryTag: String = "Web & Mobile"
    ): String? {
        val currentState = _playerState.value
        val newProject = com.example.data.AppProject(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            type = type,
            budgetCost = budgetCost,
            targetRevenue = targetRevenue,
            devTimeMonths = devTimeMonths,
            targetBusinessId = targetBusinessId,
            kanbanColumn = "BACKLOG",
            status = com.example.data.ProjectStatus.DEVELOPMENT,
            requiredUiUx = reqUiUx,
            requiredFrontend = reqFrontend,
            requiredBackend = reqBackend,
            assignedUiUx = 0,
            assignedFrontend = 0,
            assignedBackend = 0,
            isAssigned = false,
            description = description,
            clientName = clientName,
            categoryTag = categoryTag
        )
        _playerState.value = currentState.copy(
            appProjects = currentState.appProjects + newProject
        )
        saveState(_playerState.value)
        return "Proyek '$title' berhasil ditambahkan ke Backlog!"
    }

    fun startProjectDirectly(
        instanceId: String,
        title: String,
        type: com.example.data.ProjectType,
        budgetCost: Double,
        targetRevenue: Double,
        devTimeMonths: Int,
        targetBusinessId: String? = null,
        reqUiUx: Int = 1,
        reqFrontend: Int = 1,
        reqBackend: Int = 1,
        description: String = "",
        clientName: String = "Klien Korporat",
        categoryTag: String = "Web & Mobile"
    ): String? {
        val currentState = _playerState.value
        val biz = currentState.ownedBusinesses.find { it.instanceId == instanceId }
            ?: currentState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
        val softData = biz?.softwareHouseData ?: com.example.data.SoftwareHouseCompanyData()

        val currentlyAssignedUiUx = currentState.appProjects.filter { it.kanbanColumn == "IN_PROGRESS" || it.isAssigned }.sumOf { it.assignedUiUx }
        val currentlyAssignedFe = currentState.appProjects.filter { it.kanbanColumn == "IN_PROGRESS" || it.isAssigned }.sumOf { it.assignedFrontend }
        val currentlyAssignedBe = currentState.appProjects.filter { it.kanbanColumn == "IN_PROGRESS" || it.isAssigned }.sumOf { it.assignedBackend }

        val idleUiUx = (softData.uiUxDesigners - currentlyAssignedUiUx).coerceAtLeast(0)
        val idleFe = (softData.frontendDevelopers - currentlyAssignedFe).coerceAtLeast(0)
        val idleBe = (softData.backendEngineers - currentlyAssignedBe).coerceAtLeast(0)

        if (idleUiUx < reqUiUx || idleFe < reqFrontend || idleBe < reqBackend) {
            // Add to backlog instead
            addProjectToBacklog(title, type, budgetCost, targetRevenue, devTimeMonths, targetBusinessId, reqUiUx, reqFrontend, reqBackend, description, clientName, categoryTag)
            return "Dev team tidak mencukupi saat ini. Proyek disimpan ke Backlog!"
        }

        val newProject = com.example.data.AppProject(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            type = type,
            budgetCost = budgetCost,
            targetRevenue = targetRevenue,
            devTimeMonths = devTimeMonths,
            targetBusinessId = targetBusinessId,
            kanbanColumn = "IN_PROGRESS",
            status = com.example.data.ProjectStatus.DEVELOPMENT,
            requiredUiUx = reqUiUx,
            requiredFrontend = reqFrontend,
            requiredBackend = reqBackend,
            assignedUiUx = reqUiUx,
            assignedFrontend = reqFrontend,
            assignedBackend = reqBackend,
            isAssigned = true,
            description = description,
            clientName = clientName,
            categoryTag = categoryTag
        )
        _playerState.value = currentState.copy(
            appProjects = currentState.appProjects + newProject
        )
        saveState(_playerState.value)
        return "Tim berhasil di-assign! Proyek '$title' sekarang In Progress!"
    }

    fun assignTeamToProject(instanceId: String, projectId: String, uiUx: Int, frontend: Int, backend: Int): String? {
        val currentState = _playerState.value
        val biz = currentState.ownedBusinesses.find { it.instanceId == instanceId }
            ?: currentState.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
        val softData = biz?.softwareHouseData ?: com.example.data.SoftwareHouseCompanyData()

        val otherAssignedUiUx = currentState.appProjects.filter { it.id != projectId && (it.kanbanColumn == "IN_PROGRESS" || it.isAssigned) }.sumOf { it.assignedUiUx }
        val otherAssignedFe = currentState.appProjects.filter { it.id != projectId && (it.kanbanColumn == "IN_PROGRESS" || it.isAssigned) }.sumOf { it.assignedFrontend }
        val otherAssignedBe = currentState.appProjects.filter { it.id != projectId && (it.kanbanColumn == "IN_PROGRESS" || it.isAssigned) }.sumOf { it.assignedBackend }

        val idleUiUx = (softData.uiUxDesigners - otherAssignedUiUx).coerceAtLeast(0)
        val idleFe = (softData.frontendDevelopers - otherAssignedFe).coerceAtLeast(0)
        val idleBe = (softData.backendEngineers - otherAssignedBe).coerceAtLeast(0)

        if (idleUiUx < uiUx || idleFe < frontend || idleBe < backend) {
            return "Developer idle tidak cukup! Butuh ($uiUx UX, $frontend FE, $backend BE), tersedia ($idleUiUx UX, $idleFe FE, $idleBe BE)."
        }

        val updatedProjects = currentState.appProjects.map { proj ->
            if (proj.id == projectId) {
                proj.copy(
                    kanbanColumn = "IN_PROGRESS",
                    status = com.example.data.ProjectStatus.DEVELOPMENT,
                    assignedUiUx = uiUx,
                    assignedFrontend = frontend,
                    assignedBackend = backend,
                    isAssigned = true
                )
            } else proj
        }

        _playerState.value = currentState.copy(appProjects = updatedProjects)
        saveState(_playerState.value)
        return "Kru berhasil ditugaskan! Proyek dipindahkan ke kolom In Progress."
    }

    fun unassignTeamFromProject(instanceId: String, projectId: String): String? {
        val currentState = _playerState.value
        val updatedProjects = currentState.appProjects.map { proj ->
            if (proj.id == projectId) {
                proj.copy(
                    kanbanColumn = "BACKLOG",
                    assignedUiUx = 0,
                    assignedFrontend = 0,
                    assignedBackend = 0,
                    isAssigned = false
                )
            } else proj
        }
        _playerState.value = currentState.copy(appProjects = updatedProjects)
        saveState(_playerState.value)
        return "Tim berhasil ditarik. Proyek dikembalikan ke Backlog."
    }

    fun cancelBacklogProject(projectId: String): String? {
        val currentState = _playerState.value
        _playerState.value = currentState.copy(
            appProjects = currentState.appProjects.filter { it.id != projectId }
        )
        saveState(_playerState.value)
        return "Proyek berhasil dihapus dari Backlog."
    }

    fun hireDevSpecialist(instanceId: String, role: String, useCompanyCash: Boolean): String? {
        val currentState = _playerState.value
        val hireCost = when (role) {
            "UI_UX" -> 8_000L
            "FRONTEND" -> 12_000L
            "BACKEND" -> 16_000L
            else -> 10_000L
        }

        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        if (owned == null) {
            for (h in currentState.holdingCompanies) {
                owned = h.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = h.instanceId
                    break
                }
            }
        }
        if (owned == null) return "Unit bisnis tidak ditemukan"

        val softData = owned.softwareHouseData
        val newSoftData = when (role) {
            "UI_UX" -> softData.copy(uiUxDesigners = softData.uiUxDesigners + 1)
            "FRONTEND" -> softData.copy(frontendDevelopers = softData.frontendDevelopers + 1)
            "BACKEND" -> softData.copy(backendEngineers = softData.backendEngineers + 1)
            else -> softData
        }

        if (useCompanyCash) {
            if (owned.companyCash < hireCost) return "Kas Perusahaan tidak cukup (${hireCost})"
            val newOwned = owned.copy(
                companyCash = owned.companyCash - hireCost,
                softwareHouseData = newSoftData
            )
            updateOwnedBusinessInState(instanceId, newOwned, isNested, holdingId)
        } else {
            if (currentState.cash < hireCost) return "Saldo Pribadi tidak cukup (${hireCost})"
            val newOwned = owned.copy(softwareHouseData = newSoftData)
            val updatedState = currentState.copy(cash = currentState.cash - hireCost)
            _playerState.value = updatedState
            updateOwnedBusinessInState(instanceId, newOwned, isNested, holdingId)
        }
        return "Berhasil merekrut spesialis $role!"
    }

    fun fireDevSpecialist(instanceId: String, role: String): String? {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        if (owned == null) {
            for (h in currentState.holdingCompanies) {
                owned = h.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = h.instanceId
                    break
                }
            }
        }
        if (owned == null) return "Unit bisnis tidak ditemukan"

        val softData = owned.softwareHouseData
        val newSoftData = when (role) {
            "UI_UX" -> {
                if (softData.uiUxDesigners <= 0) return "Tidak ada UI/UX designer untuk diberhentikan."
                softData.copy(uiUxDesigners = softData.uiUxDesigners - 1)
            }
            "FRONTEND" -> {
                if (softData.frontendDevelopers <= 0) return "Tidak ada Frontend developer untuk diberhentikan."
                softData.copy(frontendDevelopers = softData.frontendDevelopers - 1)
            }
            "BACKEND" -> {
                if (softData.backendEngineers <= 0) return "Tidak ada Backend engineer untuk diberhentikan."
                softData.copy(backendEngineers = softData.backendEngineers - 1)
            }
            else -> softData
        }

        val newOwned = owned.copy(softwareHouseData = newSoftData)
        updateOwnedBusinessInState(instanceId, newOwned, isNested, holdingId)
        return "Spesialis $role berhasil diberhentikan."
    }

    fun upgradeTechInfrastructure(instanceId: String, techType: String, useCompanyCash: Boolean): String? {
        val currentState = _playerState.value
        var isNested = false
        var holdingId: String? = null
        var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
        if (owned == null) {
            for (h in currentState.holdingCompanies) {
                owned = h.subsidiaries.find { it.instanceId == instanceId }
                if (owned != null) {
                    isNested = true
                    holdingId = h.instanceId
                    break
                }
            }
        }
        if (owned == null) return "Unit bisnis tidak ditemukan"

        val softData = owned.softwareHouseData
        val (cost, newSoftData, successMsg) = when (techType) {
            "CICD" -> {
                if (softData.hasCiCdPipeline) return "CI/CD Pipeline sudah aktif!"
                Triple(50_000L, softData.copy(hasCiCdPipeline = true), "Automated CI/CD Pipeline berhasil diintegrasikan (+25% Speed)!")
            }
            "AI_COPILOT" -> {
                if (softData.hasAiCopilot) return "AI Code Assistant sudah aktif!"
                Triple(75_000L, softData.copy(hasAiCopilot = true), "AI Code Assistant berhasil dipasang (+50% Dev Productivity)!")
            }
            "SERVER_TIER" -> {
                if (softData.serverTier >= 5) return "Server sudah mencapai kapasitas maksimum (Tier 5 Hyperscale)!"
                val nextTier = softData.serverTier + 1
                val tierCost = when (nextTier) {
                    2 -> 25_000L
                    3 -> 100_000L
                    4 -> 350_000L
                    5 -> 1_200_000L
                    else -> 25_000L
                }
                Triple(tierCost, softData.copy(serverTier = nextTier), "Cloud Server berhasil di-upgrade ke Tier $nextTier!")
            }
            "MICROSERVICES" -> {
                if (softData.hasMicroservices) return "Arsitektur Microservices sudah aktif!"
                Triple(150_000L, softData.copy(hasMicroservices = true), "Microservices Architecture aktif (-30% resource load per user)!")
            }
            "AUTOMATED_QA" -> {
                if (softData.hasAutomatedQa) return "Automated QA Suite sudah aktif!"
                Triple(90_000L, softData.copy(hasAutomatedQa = true), "Automated QA & Testing Suite aktif (Bug incidents -50%, Churn -2%)!")
            }
            else -> return "Tipe upgrade tidak dikenal."
        }

        if (useCompanyCash) {
            if (owned.companyCash < cost) return "Kas Perusahaan tidak cukup (${cost})"
            val newOwned = owned.copy(
                companyCash = owned.companyCash - cost,
                softwareHouseData = newSoftData
            )
            updateOwnedBusinessInState(instanceId, newOwned, isNested, holdingId)
        } else {
            if (currentState.cash < cost) return "Saldo Pribadi tidak cukup (${cost})"
            val newOwned = owned.copy(softwareHouseData = newSoftData)
            val updatedState = currentState.copy(cash = currentState.cash - cost)
            _playerState.value = updatedState
            updateOwnedBusinessInState(instanceId, newOwned, isNested, holdingId)
        }
        return successMsg
    }

    fun sellSaaSProject(projectId: String, instanceId: String? = null): String? {
        val currentState = _playerState.value
        val project = currentState.appProjects.find { it.id == projectId }
        if (project != null && project.type == com.example.data.ProjectType.INDEPENDENT_SAAS && 
            (project.status == com.example.data.ProjectStatus.MAINTENANCE || project.kanbanColumn == "DEPLOYED")) {
            val acquisitionValue = (project.currentMrr * 50).toLong().coerceAtLeast(100_000L)
            
            // Record valuation in softwareHouseData if instanceId provided
            if (instanceId != null) {
                var isNested = false
                var holdingId: String? = null
                var owned = currentState.ownedBusinesses.find { it.instanceId == instanceId }
                if (owned == null) {
                    for (h in currentState.holdingCompanies) {
                        owned = h.subsidiaries.find { it.instanceId == instanceId }
                        if (owned != null) {
                            isNested = true
                            holdingId = h.instanceId
                            break
                        }
                    }
                }
                if (owned != null) {
                    val softData = owned.softwareHouseData
                    val newOwned = owned.copy(
                        companyCash = owned.companyCash + acquisitionValue,
                        softwareHouseData = softData.copy(
                            totalAcquisitionValue = softData.totalAcquisitionValue + acquisitionValue
                        )
                    )
                    updateOwnedBusinessInState(instanceId, newOwned, isNested, holdingId)
                }
            }

            _playerState.value = currentState.copy(
                cash = if (instanceId == null) currentState.cash + acquisitionValue else currentState.cash,
                appProjects = currentState.appProjects.filter { it.id != projectId }
            )
            saveState(_playerState.value)
            return "Selamat! Produk SaaS '${project.title}' berhasil diakuisisi seharga ${acquisitionValue} (50x MRR)!"
        }
        return "SaaS belum memenuhi syarat untuk dijual."
    }

    private fun updateOwnedBusinessInState(instanceId: String, newOwned: com.example.data.OwnedBusiness, isNested: Boolean, holdingId: String?) {
        val currentState = _playerState.value
        if (isNested && holdingId != null) {
            val newHoldings = currentState.holdingCompanies.map { holding ->
                if (holding.instanceId == holdingId) {
                    val newSubs = holding.subsidiaries.map { if (it.instanceId == instanceId) newOwned else it }
                    holding.copy(subsidiaries = newSubs)
                } else holding
            }
            _playerState.value = currentState.copy(holdingCompanies = newHoldings)
        } else {
            val newBusinesses = currentState.ownedBusinesses.map { if (it.instanceId == instanceId) newOwned else it }
            _playerState.value = currentState.copy(ownedBusinesses = newBusinesses)
        }
        saveState(_playerState.value)
    }

    private fun syncTvValuation(currentState: PlayerState): PlayerState {
        try {
            var activeProgramSum = 0L
            currentState.activeTvPrograms?.forEach { prog ->
                activeProgramSum += prog.productionCost.toLong()
            }

            var ipLibrarySum = 0L
            currentState.ipLibraryHistory?.forEach { item ->
                ipLibrarySum += ((item.productionCost * 0.5) + item.totalAccumulatedProfit).toLong()
            }

            val totalExtraTvValuation = (activeProgramSum + ipLibrarySum).coerceAtLeast(0L)

            val updatedBusinesses = currentState.ownedBusinesses?.map { b ->
                val cat = getCatalogItem(b.catalogId, currentState)
                if (cat?.id == "media_tv" || cat?.category == com.example.data.BusinessCategory.ENTERTAINMENT) {
                    if (cat.id == "media_tv") {
                        b.copy(extraValuation = totalExtraTvValuation)
                    } else b
                } else b
            } ?: emptyList()

            val updatedHoldings = currentState.holdingCompanies?.map { holding ->
                val updatedSubs = holding.subsidiaries?.map { sub ->
                    val cat = getCatalogItem(sub.catalogId, currentState)
                    if (cat?.id == "media_tv") {
                        sub.copy(extraValuation = totalExtraTvValuation)
                    } else sub
                } ?: emptyList()
                holding.copy(subsidiaries = updatedSubs)
            } ?: emptyList()

            return currentState.copy(
                ownedBusinesses = updatedBusinesses,
                holdingCompanies = updatedHoldings
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return currentState
        }
    }

    fun repairDataStructure() {
        try {
            val currentState = _playerState.value
            
            // Step 1: Patch Mega Holding (Prevent Null / Reset Format)
            @Suppress("SENSELESS_COMPARISON")
            val megaHolding = if (currentState.megaHolding != null) currentState.megaHolding else com.example.data.MegaHoldingState(isActive = false, ownershipPercentage = 100.0)

            // Step 2: Patch Businesses (TV & Film)
            @Suppress("SENSELESS_COMPARISON")
            val rawBusinesses = if (currentState.ownedBusinesses != null) currentState.ownedBusinesses else emptyList()
            val patchedBusinesses = rawBusinesses.map { business ->
                try {
                    val catalogItem = getCatalogItem(business.catalogId, currentState)
                    if (catalogItem?.category == com.example.data.BusinessCategory.ENTERTAINMENT && catalogItem.name.contains("Film")) {
                        // Patch Movie Projects
                        @Suppress("SENSELESS_COMPARISON")
                        val rawProjects = if (business.projectHistory != null) business.projectHistory else emptyList()
                        val patchedProjects = rawProjects.map { proj ->
                            @Suppress("SENSELESS_COMPARISON")
                            proj.copy(
                                status = if (proj.status != null) proj.status else "FINISHED",
                                remainingMonths = proj.remainingMonths ?: 0
                            )
                        }
                        @Suppress("SENSELESS_COMPARISON")
                        val type = if (business.studioType == null || business.studioType.isBlank()) "LIVE_ACTION" else business.studioType
                        business.copy(
                            projectHistory = patchedProjects,
                            studioType = type
                        )
                    } else {
                        @Suppress("SENSELESS_COMPARISON")
                        val type = if (business.studioType == null || business.studioType.isBlank()) "LIVE_ACTION" else business.studioType
                        business.copy(studioType = type)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    business
                }
            }

            // Patch TV Programs 
            @Suppress("SENSELESS_COMPARISON")
            val rawActiveTv = if (currentState.activeTvPrograms != null) currentState.activeTvPrograms else emptyList()
            val patchedActiveTv = rawActiveTv.map { prog ->
                try {
                    @Suppress("SENSELESS_COMPARISON")
                    prog.copy(
                        timeSlots = if (prog.timeSlots != null) prog.timeSlots else emptyList(),
                        isOriginalIP = if (prog.isOriginalIP != null) prog.isOriginalIP else true,
                        monthsAired = prog.monthsAired ?: 0,
                        totalAccumulatedProfit = prog.totalAccumulatedProfit ?: 0.0
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    prog
                }
            }
            
            @Suppress("SENSELESS_COMPARISON")
            val rawLibraryTv = if (currentState.ipLibraryHistory != null) currentState.ipLibraryHistory else emptyList()
            val patchedLibraryTv = rawLibraryTv.map { prog ->
                try {
                    @Suppress("SENSELESS_COMPARISON")
                    prog.copy(
                        timeSlots = if (prog.timeSlots != null) prog.timeSlots else emptyList(),
                        isOriginalIP = if (prog.isOriginalIP != null) prog.isOriginalIP else true,
                        monthsAired = prog.monthsAired ?: 0,
                        totalAccumulatedProfit = prog.totalAccumulatedProfit ?: 0.0
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    prog
                }
            }

            @Suppress("SENSELESS_COMPARISON")
            val rawHoldings = if (currentState.holdingCompanies != null) currentState.holdingCompanies else emptyList()
            val patchedHoldings = rawHoldings.map { holding ->
                val patchedSubs = holding.subsidiaries.map { sub ->
                    val catalogItem = getCatalogItem(sub.catalogId, currentState)
                    if (catalogItem?.category == com.example.data.BusinessCategory.ENTERTAINMENT && catalogItem.name.contains("Film")) {
                        @Suppress("SENSELESS_COMPARISON")
                        val rawProjects = if (sub.projectHistory != null) sub.projectHistory else emptyList()
                        val patchedProjects = rawProjects.map { proj ->
                            @Suppress("SENSELESS_COMPARISON")
                            proj.copy(
                                status = if (proj.status != null) proj.status else "FINISHED",
                                remainingMonths = proj.remainingMonths ?: 0
                            )
                        }
                        @Suppress("SENSELESS_COMPARISON")
                        val type = if (sub.studioType == null || sub.studioType.isBlank()) "LIVE_ACTION" else sub.studioType
                        sub.copy(projectHistory = patchedProjects, studioType = type)
                    } else {
                        @Suppress("SENSELESS_COMPARISON")
                        val type = if (sub.studioType == null || sub.studioType.isBlank()) "LIVE_ACTION" else sub.studioType
                        sub.copy(studioType = type)
                    }
                }
                holding.copy(subsidiaries = patchedSubs)
            }

            val tempState = currentState.copy(
                megaHolding = megaHolding,
                ownedBusinesses = patchedBusinesses,
                holdingCompanies = patchedHoldings,
                activeTvPrograms = patchedActiveTv,
                ipLibraryHistory = patchedLibraryTv
            )

            // Step 3: Recalculate netWorth safely using the single source of truth helper function
            val newNetWorth = tempState.netAssetValue(
                stockList = _stockList.value,
                cryptoList = _cryptoList.value,
                realEstateMarket = _realEstateMarket.value,
                collectionList = _collectionList.value,
                preciousMetalsList = _preciousMetalsList.value
            )

            // Step 4: Save Data
            _playerState.value = tempState.copy(netWorth = newNetWorth)
            saveState(_playerState.value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openRestaurantBranch(instanceId: String, cost: Long, branchName: String): String? {
        val state = _playerState.value
        
        var modified = false
        var insufficientFunds = false
        
        val newBusinesses = state.ownedBusinesses.map { 
            if (it.instanceId == instanceId) {
                if (it.companyCash < cost) {
                    insufficientFunds = true
                    it
                } else {
                    modified = true
                    val newBranch = com.example.data.OwnedBusiness(
                        instanceId = java.util.UUID.randomUUID().toString(),
                        catalogId = "RESTAURANT_BRANCH",
                        customName = branchName.ifBlank { "Cabang " + (it.subsidiaries.size + 1) },
                        level = 1,
                        isUpgrading = true,
                        upgradeDelayMonths = 3
                    )
                    it.copy(companyCash = it.companyCash - cost, subsidiaries = it.subsidiaries + newBranch)
                }
            } else it
        }
        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { s ->
                if (s.instanceId == instanceId) {
                    if (s.companyCash < cost) {
                        insufficientFunds = true
                        s
                    } else {
                        modified = true
                        val newBranch = com.example.data.OwnedBusiness(
                            instanceId = java.util.UUID.randomUUID().toString(),
                            catalogId = "RESTAURANT_BRANCH",
                            customName = branchName.ifBlank { "Cabang " + (s.subsidiaries.size + 1) },
                            level = 1,
                            isUpgrading = true,
                            upgradeDelayMonths = 3
                        )
                        s.copy(companyCash = s.companyCash - cost, subsidiaries = s.subsidiaries + newBranch)
                    }
                } else s
            }
            h.copy(subsidiaries = newSubs)
        }
        
        if (insufficientFunds) return "Kas Perusahaan tidak cukup."
        if (!modified) return "Bisnis tidak ditemukan."
        
        _playerState.value = state.copy(ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
        return null
    }

    fun upgradeRestaurantBranch(parentInstanceId: String, branchId: String, action: String, actionCost: Long): String? {
        val state = _playerState.value
        var modified = false
        var errorMsg: String? = null
        
        val cost = actionCost
        val delayTime = if (action == "LEVEL_UP") 2 else 4

        val newBusinesses = state.ownedBusinesses.map { parent ->
            if (parent.instanceId == parentInstanceId) {
                if (parent.companyCash < cost) {
                    errorMsg = "Kas Perusahaan tidak cukup."
                    parent
                } else {
                    val branchIndex = parent.subsidiaries.indexOfFirst { it.instanceId == branchId }
                    if (branchIndex != -1) {
                        val branchLevel = parent.subsidiaries[branchIndex].level
                        val durationInSeconds = 30 + (branchLevel * 12)
                        if (!parent.subsidiaries[branchIndex].isUpgradingRealTime) {
                            modified = true
                            val updatedBranch = parent.subsidiaries[branchIndex].copy(
                                isUpgradingRealTime = true,
                                upgradeEndTimeRealTime = System.currentTimeMillis() + (durationInSeconds * 1000L),
                                pendingAction = action
                            )
                            val newList = parent.subsidiaries.toMutableList()
                            newList[branchIndex] = updatedBranch
                            parent.copy(companyCash = parent.companyCash - cost, subsidiaries = newList)
                        } else {
                            if (errorMsg == null) errorMsg = "Sedang diproses."
                            parent
                        }
                    } else {
                        if (errorMsg == null) errorMsg = "Cabang tidak ditemukan."
                        parent
                    }
                }
            } else parent
        }
        
        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { parent ->
                if (parent.instanceId == parentInstanceId) {
                    if (parent.companyCash < cost) {
                        errorMsg = "Kas Perusahaan tidak cukup."
                        parent
                    } else {
                        val branchIndex = parent.subsidiaries.indexOfFirst { it.instanceId == branchId }
                        val branchLevel = parent.subsidiaries[branchIndex].level
                        val durationInSeconds = 30 + (branchLevel * 12)
                        if (branchIndex != -1 && !parent.subsidiaries[branchIndex].isUpgradingRealTime) {
                            modified = true
                            val updatedBranch = parent.subsidiaries[branchIndex].copy(
                                isUpgradingRealTime = true,
                                upgradeEndTimeRealTime = System.currentTimeMillis() + (durationInSeconds * 1000L),
                                pendingAction = action
                            )
                            val newList = parent.subsidiaries.toMutableList()
                            newList[branchIndex] = updatedBranch
                            parent.copy(companyCash = parent.companyCash - cost, subsidiaries = newList)
                        } else {
                            if (branchIndex != -1 && errorMsg == null) errorMsg = "Sedang diproses." else if (errorMsg == null) errorMsg = "Cabang tidak ditemukan."
                            parent
                        }
                    }
                } else parent
            }
            h.copy(subsidiaries = newSubs)
        }
        
        if (errorMsg != null) return errorMsg
        if (!modified) return "Gagal update."
        
        _playerState.value = state.copy(ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
        return null
    }

    fun finishRestaurantBranchRealtimeUpgrade(parentInstanceId: String, branchId: String) {
        val state = _playerState.value
        
        val newBusinesses = state.ownedBusinesses.map { parent ->
            if (parent.instanceId == parentInstanceId) {
                val branchIndex = parent.subsidiaries.indexOfFirst { it.instanceId == branchId }
                if (branchIndex != -1 && parent.subsidiaries[branchIndex].isUpgradingRealTime) {
                    val branch = parent.subsidiaries[branchIndex]
                    val updatedBranch = when (branch.pendingAction) {
                        "LEVEL_UP" -> branch.copy(level = branch.level + 1, isUpgradingRealTime = false, upgradeEndTimeRealTime = 0L, pendingAction = null)
                        "MICHELIN" -> branch.copy(michelinStars = branch.michelinStars + 1, isUpgradingRealTime = false, upgradeEndTimeRealTime = 0L, pendingAction = null)
                        else -> branch.copy(isUpgradingRealTime = false, upgradeEndTimeRealTime = 0L, pendingAction = null)
                    }
                    val newList = parent.subsidiaries.toMutableList()
                    newList[branchIndex] = updatedBranch
                    parent.copy(subsidiaries = newList)
                } else parent
            } else parent
        }
        
        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { parent ->
                if (parent.instanceId == parentInstanceId) {
                    val branchIndex = parent.subsidiaries.indexOfFirst { it.instanceId == branchId }
                    if (branchIndex != -1 && parent.subsidiaries[branchIndex].isUpgradingRealTime) {
                        val branch = parent.subsidiaries[branchIndex]
                        val updatedBranch = when (branch.pendingAction) {
                            "LEVEL_UP" -> branch.copy(level = branch.level + 1, isUpgradingRealTime = false, upgradeEndTimeRealTime = 0L, pendingAction = null)
                            "MICHELIN" -> branch.copy(michelinStars = branch.michelinStars + 1, isUpgradingRealTime = false, upgradeEndTimeRealTime = 0L, pendingAction = null)
                            else -> branch.copy(isUpgradingRealTime = false, upgradeEndTimeRealTime = 0L, pendingAction = null)
                        }
                        val newList = parent.subsidiaries.toMutableList()
                        newList[branchIndex] = updatedBranch
                        parent.copy(subsidiaries = newList)
                    } else parent
                } else parent
            }
            h.copy(subsidiaries = newSubs)
        }
        
        _playerState.value = state.copy(ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
    }

    fun startConstructionTender(instanceId: String, name: String, contractValue: Long, duration: Int, initialCapital: Long, useCompanyCash: Boolean): String? {
        val state = _playerState.value
        
        var modified = false
        val newProject = com.example.data.ConstructionProject(
            name = name,
            totalContractValue = contractValue.toDouble(),
            durationMonths = duration,
            remainingMonths = duration,
            isFinished = false
        )

        val newBusinesses = state.ownedBusinesses.map { 
            if (it.instanceId == instanceId) {
                if (useCompanyCash) {
                    if (it.companyCash < initialCapital) return "Kas Perusahaan tidak cukup."
                    modified = true
                    it.copy(companyCash = it.companyCash - initialCapital, activeTenders = it.activeTenders + newProject)
                } else {
                    if (state.cash < initialCapital) return "Dana Kas Pribadi tidak cukup."
                    modified = true
                    it.copy(activeTenders = it.activeTenders + newProject)
                }
            } else it
        }
        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { s ->
                if (s.instanceId == instanceId) {
                    if (useCompanyCash) {
                         if (s.companyCash < initialCapital) return "Kas Perusahaan tidak cukup."
                         modified = true
                         s.copy(companyCash = s.companyCash - initialCapital, activeTenders = s.activeTenders + newProject)
                    } else {
                         if (state.cash < initialCapital) return "Dana Kas Pribadi tidak cukup."
                         modified = true
                         s.copy(activeTenders = s.activeTenders + newProject)
                    }
                } else s
            }
            h.copy(subsidiaries = newSubs)
        }

        if (modified) {
            _playerState.value = state.copy(
                cash = if (!useCompanyCash) state.cash - initialCapital else state.cash,
                ownedBusinesses = newBusinesses,
                holdingCompanies = newHoldings
            )
            saveState(_playerState.value)
            return null
        }
        return "Bisnis tidak ditemukan."
    }

    fun takeClientProject(instanceId: String, projectId: String): String? {
        val state = _playerState.value
        var modified = false
        var errorMsg: String? = null

        val newBusinesses = state.ownedBusinesses.map { 
            if (it.instanceId == instanceId) {
                val proj = it.availableClientProjects.find { p -> p.id == projectId }
                if (proj != null) {
                    modified = true
                    it.copy(
                        availableClientProjects = it.availableClientProjects.filterNot { p -> p.id == projectId },
                        activeTenders = it.activeTenders + proj
                    )
                } else {
                    if (errorMsg == null) errorMsg = "Proyek tidak ditemukan."
                    it
                }
            } else it
        }
        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { s ->
                if (s.instanceId == instanceId) {
                    val proj = s.availableClientProjects.find { p -> p.id == projectId }
                    if (proj != null) {
                        modified = true
                        s.copy(
                            availableClientProjects = s.availableClientProjects.filterNot { p -> p.id == projectId },
                            activeTenders = s.activeTenders + proj
                        )
                    } else {
                        if (errorMsg == null) errorMsg = "Proyek tidak ditemukan."
                        s
                    }
                } else s
            }
            h.copy(subsidiaries = newSubs)
        }

        if (errorMsg != null && !modified) return errorMsg
        if (!modified) return "Instansi Bisnis tidak ditemukan."

        _playerState.value = state.copy(ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
        return null
    }

    fun allocateConstructionPhase(instanceId: String, projectId: String): String? {
        val state = _playerState.value
        var modified = false
        var errorMsg: String? = null

        fun processBiz(biz: com.example.data.OwnedBusiness): com.example.data.OwnedBusiness {
            if (biz.instanceId != instanceId) return biz
            val proj = biz.activeTenders.find { it.id == projectId }
            if (proj == null) {
                errorMsg = "Proyek tidak ditemukan."
                return biz
            }
            if (proj.isFinished || proj.currentPhaseIndex >= proj.phases.size) {
                errorMsg = "Proyek sudah selesai."
                return biz
            }

            val availCrews = ConstructionEngine.getAvailableCrews(biz)
            val availMach = ConstructionEngine.getAvailableMachinery(biz)

            if (availCrews < proj.requiredCrews) {
                errorMsg = "Kru tidak mencukupi! Butuh ${proj.requiredCrews} kru aktif (Tersedia: $availCrews). Rekrut kru tambahan terlebih dahulu."
                return biz
            }
            if (availMach < proj.requiredMachinery) {
                errorMsg = "Alat berat tidak mencukupi! Butuh ${proj.requiredMachinery} unit (Tersedia: $availMach). Beli armada alat berat tambahan."
                return biz
            }

            val updatedPhases = proj.phases.mapIndexed { idx, ph ->
                if (idx == proj.currentPhaseIndex) ph.copy(isAllocated = true)
                else ph
            }
            val updatedProjects = biz.activeTenders.map {
                if (it.id == projectId) it.copy(phases = updatedPhases)
                else it
            }
            modified = true
            return biz.copy(activeTenders = updatedProjects)
        }

        val newBusinesses = state.ownedBusinesses.map { processBiz(it) }
        val newHoldings = state.holdingCompanies.map { h ->
            h.copy(subsidiaries = h.subsidiaries.map { processBiz(it) })
        }

        if (errorMsg != null) return errorMsg
        if (!modified) return "Unit bisnis tidak ditemukan."

        _playerState.value = state.copy(ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
        return null
    }

    fun submitConstructionTenderBid(
        instanceId: String,
        tenderId: String,
        bidAmount: Long,
        useCompanyCash: Boolean,
        useInHouseLogistics: Boolean
    ): Pair<ConstructionEngine.BiddingResult?, String?> {
        val state = _playerState.value
        var targetBiz: com.example.data.OwnedBusiness? = null

        state.ownedBusinesses.find { it.instanceId == instanceId }?.let { targetBiz = it }
        if (targetBiz == null) {
            for (h in state.holdingCompanies) {
                val sub = h.subsidiaries.find { it.instanceId == instanceId }
                if (sub != null) {
                    targetBiz = sub
                    break
                }
            }
        }

        if (targetBiz == null) return Pair(null, "Unit bisnis konstruksi tidak ditemukan.")
        val biz = targetBiz!!

        val tender = biz.constructionData.availableTenderMarket.find { it.id == tenderId }
            ?: return Pair(null, "Tender lelang tidak ditemukan atau telah kedaluwarsa.")

        val bond = tender.minBidBond
        if (useCompanyCash) {
            if (biz.companyCash < bond) return Pair(null, "Kas Perusahaan tidak mencukupi jaminan penawaran (${com.example.ui.formatCurrencyRingkas(bond.toDouble(), false)}).")
        } else {
            if (state.cash < bond) return Pair(null, "Kas Pribadi tidak mencukupi jaminan penawaran (${com.example.ui.formatCurrencyRingkas(bond.toDouble(), false)}).")
        }

        val hasLogistics = useInHouseLogistics || ConstructionEngine.hasLogisticsSynergy(state)
        val result = ConstructionEngine.evaluateBid(tender, bidAmount, biz.constructionData.trustScore, hasLogistics)

        var newCash = state.cash
        var newCompanyCash = biz.companyCash
        var newActiveTenders = biz.activeTenders
        val remainingMarket = biz.constructionData.availableTenderMarket.filterNot { it.id == tenderId }
        var newTrust = biz.constructionData.trustScore

        if (result.isWon) {
            // Deduct security deposit
            if (useCompanyCash) {
                newCompanyCash -= bond
            } else {
                newCash -= bond
            }
            newTrust = (newTrust + result.trustScoreDelta).coerceIn(0, 100)

            val createdProject = com.example.data.ConstructionProject(
                name = tender.title,
                totalContractValue = bidAmount.toDouble(),
                durationMonths = tender.durationMonths,
                remainingMonths = tender.durationMonths,
                isFinished = false,
                clientName = tender.clientName,
                clientType = tender.clientType,
                projectScale = tender.projectScale,
                ownerEstimateBudget = tender.ownerEstimateBudget,
                agreedBidPrice = bidAmount,
                currentPhaseIndex = 0,
                phases = tender.phases,
                requiredCrews = tender.requiredCrews,
                requiredMachinery = tender.requiredMachinery,
                initialSecurityDeposit = bond,
                usesInHouseLogistics = hasLogistics
            )
            newActiveTenders = newActiveTenders + createdProject
        }

        val updatedFirmData = biz.constructionData.copy(
            trustScore = newTrust,
            availableTenderMarket = remainingMarket
        )
        val updatedBiz = biz.copy(
            companyCash = newCompanyCash,
            activeTenders = newActiveTenders,
            constructionData = updatedFirmData
        )

        val newBusinesses = state.ownedBusinesses.map { if (it.instanceId == instanceId) updatedBiz else it }
        val newHoldings = state.holdingCompanies.map { h ->
            h.copy(subsidiaries = h.subsidiaries.map { if (it.instanceId == instanceId) updatedBiz else it })
        }

        _playerState.value = state.copy(
            cash = newCash,
            ownedBusinesses = newBusinesses,
            holdingCompanies = newHoldings
        )
        saveState(_playerState.value)

        return Pair(result, null)
    }

    fun resolveConstructionEvent(instanceId: String, projectId: String, actionChoice: String): String? {
        val state = _playerState.value
        var modified = false
        var errorMsg: String? = null

        fun processBiz(biz: com.example.data.OwnedBusiness): com.example.data.OwnedBusiness {
            if (biz.instanceId != instanceId) return biz
            val proj = biz.activeTenders.find { it.id == projectId }
            if (proj == null || proj.activeEvent == null) {
                errorMsg = "Tidak ada insiden aktif pada proyek ini."
                return biz
            }

            val event = proj.activeEvent
            var compCash = biz.companyCash
            var trust = biz.constructionData.trustScore

            when (actionChoice) {
                "PAY_COST" -> {
                    if (compCash < event.costImpact) {
                        errorMsg = "Kas Perusahaan tidak mencukupi untuk menanggung biaya insiden ini."
                        return biz
                    }
                    compCash -= event.costImpact
                }
                "CLAIM_INSURANCE" -> {
                    val insCost = (event.costImpact * 0.35).toLong()
                    if (compCash < insCost) {
                        errorMsg = "Kas Perusahaan tidak mencukupi untuk deductible asuransi (${com.example.ui.formatCurrencyRingkas(insCost.toDouble(), false)})."
                        return biz
                    }
                    compCash -= insCost
                    trust = (trust + 1).coerceAtMost(100)
                }
                "USE_IN_HOUSE" -> {
                    val matCost = (event.costImpact * 0.5).toLong()
                    if (compCash < matCost) {
                        errorMsg = "Kas Perusahaan tidak mencukupi untuk material in-house."
                        return biz
                    }
                    compCash -= matCost
                }
                "DISMISS" -> {
                    // Just dismiss if resolved
                }
            }

            val updatedProjects = biz.activeTenders.map {
                if (it.id == projectId) it.copy(activeEvent = event.copy(isResolved = true))
                else it
            }
            modified = true
            val updatedData = biz.constructionData.copy(trustScore = trust)
            return biz.copy(companyCash = compCash, activeTenders = updatedProjects, constructionData = updatedData)
        }

        val newBusinesses = state.ownedBusinesses.map { processBiz(it) }
        val newHoldings = state.holdingCompanies.map { h ->
            h.copy(subsidiaries = h.subsidiaries.map { processBiz(it) })
        }

        if (errorMsg != null) return errorMsg
        if (!modified) return "Unit bisnis tidak ditemukan."

        _playerState.value = state.copy(ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
        return null
    }

    fun upgradeConstructionCapacity(instanceId: String, upgradeType: String, useCompanyCash: Boolean): String? {
        val state = _playerState.value
        var targetBiz: com.example.data.OwnedBusiness? = null

        state.ownedBusinesses.find { it.instanceId == instanceId }?.let { targetBiz = it }
        if (targetBiz == null) {
            for (h in state.holdingCompanies) {
                val sub = h.subsidiaries.find { it.instanceId == instanceId }
                if (sub != null) {
                    targetBiz = sub
                    break
                }
            }
        }

        if (targetBiz == null) return "Unit bisnis konstruksi tidak ditemukan."
        val biz = targetBiz!!

        val (cost, newCrews, newMach, newCert, trustGain) = when (upgradeType) {
            "RECRUIT_CREW" -> {
                val cur = biz.constructionData.maxCrews
                val c = (cur * 350_000L)
                val nextCrews = cur + 2
                ConstructionUpgradeSpecs(c, nextCrews, biz.constructionData.maxMachinery, biz.constructionData.safetyCertLevel, 1)
            }
            "BUY_MACHINERY" -> {
                val cur = biz.constructionData.maxMachinery
                val c = (cur * 150_000L)
                val nextMach = cur + 4
                ConstructionUpgradeSpecs(c, biz.constructionData.maxCrews, nextMach, biz.constructionData.safetyCertLevel, 2)
            }
            "SAFETY_CERT" -> {
                val cur = biz.constructionData.safetyCertLevel
                if (cur >= 5) return "Sertifikasi K3 sudah mencapai level Master Platinum maksimal."
                val c = cur * 1_200_000L
                ConstructionUpgradeSpecs(c, biz.constructionData.maxCrews, biz.constructionData.maxMachinery, cur + 1, 10)
            }
            else -> return "Tipe ekspansi tidak dikenal."
        }

        var newCash = state.cash
        var newCompanyCash = biz.companyCash

        if (useCompanyCash) {
            if (newCompanyCash < cost) return "Kas Perusahaan tidak mencukupi (${com.example.ui.formatCurrencyRingkas(cost.toDouble(), false)})."
            newCompanyCash -= cost
        } else {
            if (newCash < cost) return "Kas Pribadi tidak mencukupi (${com.example.ui.formatCurrencyRingkas(cost.toDouble(), false)})."
            newCash -= cost
        }

        val updatedData = biz.constructionData.copy(
            maxCrews = newCrews,
            maxMachinery = newMach,
            safetyCertLevel = newCert,
            trustScore = (biz.constructionData.trustScore + trustGain).coerceIn(0, 100)
        )
        val updatedBiz = biz.copy(companyCash = newCompanyCash, constructionData = updatedData)

        val newBusinesses = state.ownedBusinesses.map { if (it.instanceId == instanceId) updatedBiz else it }
        val newHoldings = state.holdingCompanies.map { h ->
            h.copy(subsidiaries = h.subsidiaries.map { if (it.instanceId == instanceId) updatedBiz else it })
        }

        _playerState.value = state.copy(
            cash = newCash,
            ownedBusinesses = newBusinesses,
            holdingCompanies = newHoldings
        )
        saveState(_playerState.value)
        return null
    }

    fun refreshConstructionTenderMarket(instanceId: String): String? {
        val state = _playerState.value
        var modified = false

        fun processBiz(biz: com.example.data.OwnedBusiness): com.example.data.OwnedBusiness {
            if (biz.instanceId != instanceId) return biz
            val newTenders = ConstructionEngine.generateTenderMarket(biz.level, biz.constructionData.trustScore)
            modified = true
            return biz.copy(constructionData = biz.constructionData.copy(availableTenderMarket = newTenders))
        }

        val newBusinesses = state.ownedBusinesses.map { processBiz(it) }
        val newHoldings = state.holdingCompanies.map { h ->
            h.copy(subsidiaries = h.subsidiaries.map { processBiz(it) })
        }

        if (!modified) return "Unit bisnis tidak ditemukan."

        _playerState.value = state.copy(ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
        return null
    }

    fun buildHealthcareUnit(instanceId: String, name: String, type: String, vendorId: String?, level: Int = 1): String? {
        val state = _playerState.value
        var modified = false
        var errorMsg: String? = null

        val cost = when (type) {
            "HOSPITAL" -> (500_000L).toDouble() + (level * 100_000L)
            "INSURANCE" -> 2_000_000.0
            "CLINIC" -> 150_000.0
            else -> 0.0
        }

        if (state.cash < cost.toLong()) return "Dana Kas Pribadi tidak cukup."

        val isUpgrading = vendorId != null
        val delayMonths = if (isUpgrading) 3 else 0

        val newUnit = com.example.data.HealthcareUnit(
            name = name,
            type = type,
            level = level,
            isUpgrading = isUpgrading,
            upgradeDelayMonths = delayMonths,
            unitCash = if (type == "INSURANCE") 1_000_000.0 else 0.0 // Starting cash for insurance
        )

        var constructorProfit = 0L
        if (vendorId != null) {
            constructorProfit = (cost * 0.4).toLong()
        }

        val newBusinesses = state.ownedBusinesses.map { biz ->
            if (biz.instanceId == instanceId) {
                modified = true
                biz.copy(healthcareSubsidiaries = biz.healthcareSubsidiaries + newUnit)
            } else if (biz.instanceId == vendorId) {
                val newTender = com.example.data.ConstructionProject(
                    name = "Internal: $name",
                    totalContractValue = constructorProfit.toDouble(),
                    durationMonths = delayMonths,
                    remainingMonths = delayMonths
                )
                biz.copy(companyCash = biz.companyCash + constructorProfit.toDouble(), activeTenders = biz.activeTenders + newTender)
            } else biz
        }

        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { s ->
                if (s.instanceId == instanceId) {
                    modified = true
                    s.copy(healthcareSubsidiaries = s.healthcareSubsidiaries + newUnit)
                } else if (s.instanceId == vendorId) {
                    val newTender = com.example.data.ConstructionProject(
                        name = "Internal: $name",
                        totalContractValue = constructorProfit.toDouble(),
                        durationMonths = delayMonths,
                        remainingMonths = delayMonths
                    )
                    s.copy(companyCash = s.companyCash + constructorProfit.toDouble(), activeTenders = s.activeTenders + newTender)
                } else s
            }
            h.copy(subsidiaries = newSubs)
        }

        if (errorMsg != null) return errorMsg
        if (!modified) return "Instansi Bisnis tidak ditemukan."

        _playerState.value = state.copy(cash = state.cash - cost.toLong(), ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
        return null
    }

    fun upgradeMedicalDepartment(instanceId: String, unitId: String, department: String): String? {
        val state = _playerState.value
        var errorMsg: String? = null
        
        val modified = updateEoBusiness(instanceId) { biz ->
            val updatedUnits = biz.healthcareSubsidiaries.map { unit ->
                if (unit.id == unitId) {
                    val cost = when (department) {
                        "IGD" -> unit.igdLevel * 80_000.0
                        "SPECIALIST" -> unit.specialistLevel * 150_000.0
                        "PHARMACY" -> unit.pharmacyLevel * 50_000.0
                        "BEDS" -> 100_000.0
                        else -> 100_000.0
                    }
                    if (unit.unitCash < cost) {
                        errorMsg = "Kas internal unit tidak mencukupi untuk upgrade departemen (${cost.toLong()})."
                        unit
                    } else {
                        val newCash = unit.unitCash - cost
                        when (department) {
                            "IGD" -> unit.copy(
                                igdLevel = unit.igdLevel + 1,
                                totalBeds = unit.totalBeds + 40,
                                unitCash = newCash
                            )
                            "SPECIALIST" -> unit.copy(
                                specialistLevel = unit.specialistLevel + 1,
                                totalBeds = unit.totalBeds + 25,
                                unitCash = newCash
                            )
                            "PHARMACY" -> unit.copy(
                                pharmacyLevel = unit.pharmacyLevel + 1,
                                unitCash = newCash
                            )
                            "BEDS" -> unit.copy(
                                totalBeds = unit.totalBeds + 50,
                                unitCash = newCash
                            )
                            else -> unit
                        }
                    }
                } else unit
            }
            biz.copy(healthcareSubsidiaries = updatedUnits)
        }
        
        if (errorMsg != null) return errorMsg
        if (!modified) return "Instansi bisnis tidak ditemukan."
        return null
    }

    fun updateInsurancePremium(instanceId: String, unitId: String, premium: Double): Boolean {
        val clamped = premium.coerceIn(20.0, 500.0)
        return updateEoBusiness(instanceId) { biz ->
            val updatedUnits = biz.healthcareSubsidiaries.map { unit ->
                if (unit.id == unitId) unit.copy(monthlyPremium = clamped) else unit
            }
            biz.copy(healthcareSubsidiaries = updatedUnits)
        }
    }

    fun injectHealthcareUnitCash(instanceId: String, unitId: String, amount: Long): String? {
        val state = _playerState.value
        if (state.cash < amount) return "Dana kas pribadi tidak mencukupi."
        if (amount <= 0) return "Nominal injeksi tidak valid."

        var found = false
        val newBusinesses = state.ownedBusinesses.map { biz ->
            if (biz.instanceId == instanceId) {
                val newUnits = biz.healthcareSubsidiaries.map { u ->
                    if (u.id == unitId) {
                        found = true
                        u.copy(unitCash = u.unitCash + amount, reserveFund = u.reserveFund + amount)
                    } else u
                }
                biz.copy(healthcareSubsidiaries = newUnits)
            } else biz
        }

        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { s ->
                if (s.instanceId == instanceId) {
                    val newUnits = s.healthcareSubsidiaries.map { u ->
                        if (u.id == unitId) {
                            found = true
                            u.copy(unitCash = u.unitCash + amount, reserveFund = u.reserveFund + amount)
                        } else u
                    }
                    s.copy(healthcareSubsidiaries = newUnits)
                } else s
            }
            h.copy(subsidiaries = newSubs)
        }

        if (!found) return "Unit medis tidak ditemukan."

        _playerState.value = state.copy(cash = state.cash - amount, ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
        saveState(_playerState.value)
        return null
    }

    fun upgradeHealthcareTier(instanceId: String, unitId: String): String? {
        val state = _playerState.value
        var errorMsg: String? = null
        
        val modified = updateEoBusiness(instanceId) { biz ->
            val updatedUnits = biz.healthcareSubsidiaries.map { unit ->
                if (unit.id == unitId) {
                    if (unit.type == "INSURANCE") {
                        val (nextTier, cost) = when (unit.tierCategory) {
                            "BASIC" -> "PREMIUM" to 1_500_000.0
                            "PREMIUM" -> "ELITE" to 5_000_000.0
                            else -> null to 0.0
                        }
                        if (nextTier == null) {
                            errorMsg = "Asuransi sudah mencapai tier tertinggi (ELITE)."
                            unit
                        } else if (unit.unitCash < cost) {
                            errorMsg = "Kas internal unit tidak mencukupi (${cost.toLong()})."
                            unit
                        } else {
                            unit.copy(tierCategory = nextTier, unitCash = unit.unitCash - cost)
                        }
                    } else {
                        // General hospital level upgrade
                        val cost = 100_000.0 * unit.level
                        if (unit.unitCash < cost) {
                            errorMsg = "Kas internal unit tidak cukup."
                            unit
                        } else {
                            unit.copy(level = unit.level + 1, totalBeds = unit.totalBeds + 50, unitCash = unit.unitCash - cost)
                        }
                    }
                } else unit
            }
            biz.copy(healthcareSubsidiaries = updatedUnits)
        }

        if (errorMsg != null) return errorMsg
        if (!modified) return "Instansi bisnis tidak ditemukan."
        return null
    }

    private fun updateEoBusiness(instanceId: String, updateBlock: (com.example.data.OwnedBusiness) -> com.example.data.OwnedBusiness): Boolean {
        val state = _playerState.value
        var modified = false
        val newBusinesses = state.ownedBusinesses.map { s ->
            if (s.instanceId == instanceId) {
                modified = true
                updateBlock(s)
            } else s
        }
        val newHoldings = state.holdingCompanies.map { h ->
            val newSubs = h.subsidiaries.map { s ->
                if (s.instanceId == instanceId) {
                    modified = true
                    updateBlock(s)
                } else s
            }
            h.copy(subsidiaries = newSubs)
        }
        if (modified) {
            _playerState.value = state.copy(ownedBusinesses = newBusinesses, holdingCompanies = newHoldings)
            saveState(_playerState.value)
        }
        return modified
    }

    fun getAssetPurchasePrice(asset: String): Double {
        return when (asset) {
            "Stage" -> 20000.0
            "Sound" -> 15000.0
            "Lighting" -> 12000.0
            "LED" -> 25000.0
            "Power" -> 15000.0
            "Security" -> 8000.0
            "Toilet" -> 5000.0
            "Barricade" -> 6000.0
            "Ambulance" -> 30000.0
            "Tent" -> 10000.0
            "Truss" -> 15000.0
            "Forklift" -> 12000.0
            "Truck" -> 18000.0
            "Warehouse" -> 50000.0
            "Helicopter" -> 500000.0
            else -> 10000.0
        }
    }

    fun getDivisionHiringCost(div: String): Double {
        return when (div) {
            "Sales" -> 15000.0
            "Creative" -> 12000.0
            "Production" -> 20000.0
            "Multimedia" -> 18000.0
            "Talent" -> 25000.0
            "Logistics" -> 15000.0
            "Finance" -> 22000.0
            "Legal" -> 25000.0
            "Marketing" -> 15000.0
            else -> 10000.0
        }
    }

    fun getHqUpgradeCost(hq: String): Double {
        return when (hq) {
            "HOUSE" -> 0.0
            "OFFICE" -> 50000.0
            "REGIONAL" -> 200000.0
            "NATIONAL" -> 1000000.0
            "INTERNATIONAL" -> 5000000.0
            else -> 0.0
        }
    }

    fun getHqDisplayName(hq: String): String {
        return when (hq) {
            "HOUSE" -> "Rumah Pribadi"
            "OFFICE" -> "Kantor Sewaan"
            "REGIONAL" -> "Kantor Regional"
            "NATIONAL" -> "Kantor Nasional"
            "INTERNATIONAL" -> "Markas Internasional"
            else -> hq
        }
    }

    fun formRentalDivision(instanceId: String): String? {
        val state = _playerState.value
        val cost = 100000L
        if (state.cash < cost) return "Kas pribadi kurang dari $100,000"
        
        var success = false
        val ok = updateEoBusiness(instanceId) { biz ->
            success = true
            biz.copy(hasRentalDivision = true)
        }
        if (ok && success) {
            _playerState.value = _playerState.value.copy(cash = _playerState.value.cash - cost)
            saveState(_playerState.value)
            return null
        }
        return "Bisnis tidak ditemukan."
    }

    fun acceptClientEvent(instanceId: String, eventId: String): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val ev = biz.clientEventRequests.find { it.id == eventId }
            if (ev == null) {
                errorMsg = "Tawaran tidak valid."
                biz
            } else {
                val newEv = ev.copy(phase = "PLANNING")
                biz.copy(
                    clientEventRequests = biz.clientEventRequests.filter { it.id != eventId },
                    activeEvents = biz.activeEvents + newEv
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun startCustomEvent(instanceId: String, name: String, category: String, pax: Int, tb: Double, eoFee: Double, techFee: Double, useInHouse: Boolean): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val requirements = when (category) {
                "Birthday Party" -> listOf("Sound", "Lighting")
                "Wedding" -> listOf("Stage", "Sound", "Lighting", "LED")
                "Graduation" -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security")
                "Corporate Gathering" -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security")
                "Concert" -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance")
                "Festival" -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance", "Tent")
                "Exhibition" -> listOf("LED", "Power", "Security", "Toilet", "Barricade", "Forklift", "Truck")
                "Sports Event" -> listOf("Sound", "Power", "Security", "Toilet", "Barricade", "Ambulance")
                "Government Event" -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security")
                "International Summit" -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance", "Helicopter")
                else -> listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security")
            }
            val weather = if (kotlin.random.Random.nextDouble() < 0.3) "RAINY" else "SUNNY"
            val isOutdoor = listOf("Wedding", "Pensi", "Konser", "Festival", "Sports").any { category.contains(it, ignoreCase = true) }
            val event = com.example.data.EventProject(
                name = name,
                category = category,
                pax = pax,
                totalBudget = tb,
                eoFee = eoFee,
                techFee = techFee,
                useInHouseTech = useInHouse,
                executionEndTime = 0L,
                weather = weather,
                isOutdoor = isOutdoor,
                requirements = requirements,
                phase = "PLANNING"
            )
            biz.copy(activeEvents = biz.activeEvents + event)
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun startSpecialEvent(instanceId: String, specName: String, specCategory: String, budget: Double, fee: Double, requirements: List<String>, prestigeReward: Int): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            if (biz.eoCompletedSpecialEvents.contains(specName)) {
                errorMsg = "Kamu sudah menyelesaikan event prestige ini!"
                biz
            } else if (biz.eoPrestige < prestigeReward - 25) { // some threshold
                errorMsg = "Prestige perusahaan kamu belum cukup untuk event akbar ini!"
                biz
            } else {
                val weather = if (kotlin.random.Random.nextDouble() < 0.3) "RAINY" else "SUNNY"
                val event = com.example.data.EventProject(
                    name = specName,
                    category = specCategory,
                    pax = 10000,
                    totalBudget = budget,
                    eoFee = fee,
                    techFee = budget * 0.4,
                    useInHouseTech = false,
                    executionEndTime = 0L,
                    weather = weather,
                    isOutdoor = true,
                    requirements = requirements,
                    phase = "PLANNING",
                    isSpecial = true,
                    requiredPrestige = prestigeReward // Store the reward here for ease of access
                )
                biz.copy(activeEvents = biz.activeEvents + event)
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun rentAssetForEvent(instanceId: String, eventId: String, reqName: String): String? {
        var errorMsg: String? = null
        val rentCost = getAssetPurchasePrice(reqName) * 0.05
        
        val ok = updateEoBusiness(instanceId) { biz ->
            val ev = biz.activeEvents.find { it.id == eventId }
            if (ev == null) {
                errorMsg = "Event tidak ditemukan."
                biz
            } else if (biz.companyCash < rentCost) {
                errorMsg = "Kas internal tidak mencukupi untuk sewa vendor ($${String.format("%,.0f", rentCost)})."
                biz
            } else {
                val updatedEv = ev.copy(rentedAssets = ev.rentedAssets + reqName)
                biz.copy(
                    companyCash = biz.companyCash - rentCost,
                    activeEvents = biz.activeEvents.map { if (it.id == eventId) updatedEv else it }
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun startEventExecution(instanceId: String, eventId: String): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val ev = biz.activeEvents.find { it.id == eventId }
            if (ev == null) {
                errorMsg = "Event tidak ditemukan."
                biz
            } else if (biz.companyCash < ev.totalBudget) {
                errorMsg = "Kas internal tidak cukup modal untuk mengeksekusi event ($${String.format("%,.0f", ev.totalBudget)})."
                biz
            } else {
                // Random incident: 40% chance
                val hasIncident = kotlin.random.Random.nextDouble() < 0.40
                val incidentName = if (hasIncident) {
                    listOf("GENSET_BROKEN", "VENDOR_LATE", "ARTIST_CANCELED", "TICKETS_OVERSOLD", "DEMONSTRATION", "STRONG_WINDS").random()
                } else null
                
                // Duration of event execution in milliseconds: 30 seconds
                val durationMs = 30000L
                val updatedEv = ev.copy(
                    phase = "EXECUTING",
                    executionStartTime = System.currentTimeMillis(),
                    executionEndTime = System.currentTimeMillis() + durationMs,
                    activeIncident = incidentName,
                    incidentResolved = false,
                    incidentImpactQuality = 0.0,
                    incidentImpactCost = 0.0
                )
                biz.copy(
                    companyCash = biz.companyCash - ev.totalBudget,
                    activeEvents = biz.activeEvents.map { if (it.id == eventId) updatedEv else it }
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun resolveEventIncident(instanceId: String, eventId: String, choiceIndex: Int): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val ev = biz.activeEvents.find { it.id == eventId }
            if (ev == null || ev.activeIncident == null) {
                errorMsg = "Incident tidak aktif."
                biz
            } else {
                var qualityImpact = 0.0
                var costImpact = 0.0
                
                when (ev.activeIncident) {
                    "GENSET_BROKEN" -> {
                        if (choiceIndex == 0) { // Emergency generator
                            costImpact = 10000.0
                            qualityImpact = 0.0
                        } else if (choiceIndex == 1) { // Backup generator (if owned)
                            if ((biz.eoOwnedAssets["Power"] ?: 0) > 0) {
                                costImpact = 0.0
                                qualityImpact = 0.0
                            } else {
                                costImpact = 5000.0
                                qualityImpact = -10.0
                            }
                        } else { // Do nothing
                            costImpact = 0.0
                            qualityImpact = -30.0
                        }
                    }
                    "VENDOR_LATE" -> {
                        if (choiceIndex == 0) { // Pay courier
                            costImpact = 5000.0
                            qualityImpact = 0.0
                        } else { // Wait
                            costImpact = 0.0
                            qualityImpact = -15.0
                        }
                    }
                    "ARTIST_CANCELED" -> {
                        if (choiceIndex == 0) { // Replace artist
                            costImpact = 20000.0
                            qualityImpact = 0.0
                        } else if (choiceIndex == 1) { // Negotiate discount
                            costImpact = 8000.0
                            qualityImpact = -10.0
                        } else { // Cancel parts
                            costImpact = 0.0
                            qualityImpact = -40.0
                        }
                    }
                    "TICKETS_OVERSOLD" -> {
                        if (choiceIndex == 0) { // Upgrade area
                            costImpact = 15000.0
                            qualityImpact = 0.0
                        } else { // Leave crowded
                            costImpact = 0.0
                            qualityImpact = -25.0
                        }
                    }
                    "DEMONSTRATION" -> {
                        if (choiceIndex == 0) { // Extra security
                            costImpact = 12000.0
                            qualityImpact = 0.0
                        } else { // Negotiate
                            costImpact = 0.0
                            qualityImpact = -20.0
                        }
                    }
                    "STRONG_WINDS" -> {
                        if (choiceIndex == 0) { // Reinforce rigging
                            costImpact = 8000.0
                            qualityImpact = 0.0
                        } else { // Do nothing
                            costImpact = 0.0
                            qualityImpact = -30.0
                        }
                    }
                }
                
                if (biz.companyCash < costImpact) {
                    errorMsg = "Kas internal tidak cukup untuk keputusan ini!"
                    biz
                } else {
                    val updatedEv = ev.copy(
                        incidentResolved = true,
                        incidentImpactQuality = qualityImpact,
                        incidentImpactCost = costImpact
                    )
                    biz.copy(
                        companyCash = biz.companyCash - costImpact,
                        activeEvents = biz.activeEvents.map { if (it.id == eventId) updatedEv else it }
                    )
                }
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun calculateEventResults(instanceId: String, eventId: String): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val ev = biz.activeEvents.find { it.id == eventId }
            if (ev == null) {
                errorMsg = "Event tidak ditemukan."
                biz
            } else {
                val divisions = biz.eoDivisions ?: emptySet()
                
                // 1. Calculate Quality
                var baseQuality = 100.0
                if (divisions.contains("Creative")) {
                    baseQuality += 5.0
                }
                
                // Missing assets deductions
                var missingCount = 0
                for (req in ev.requirements) {
                    val mappedType = when (req.lowercase()) {
                        "stage" -> "stage"
                        "sound" -> "sound system"
                        "lighting" -> "lighting"
                        "led" -> "led wall"
                        "power" -> "power generator"
                        "security" -> "security"
                        "toilet" -> "mobile toilet"
                        "barricade" -> "barricade"
                        "ambulance" -> "ambulance"
                        "tent" -> "tent & truss"
                        "truss" -> "tent & truss"
                        "forklift" -> "heavy equipment"
                        "truck" -> "logistics truck"
                        "warehouse" -> "warehouse storage"
                        "helicopter" -> "vip heli"
                        else -> req.lowercase()
                    }
                    val hasCustom = (biz.eoCustomAssets ?: emptyList()).any { it.type.lowercase() == mappedType && it.quantity > 0 }
                    val isOwned = ((biz.eoOwnedAssets[req] ?: 0) > 0) || hasCustom
                    val isRented = ev.rentedAssets.contains(req)
                    if (!isOwned && !isRented) {
                        missingCount++
                    }
                }
                val requirementDeduction = missingCount * 12.0
                
                // Weather impact
                var weatherDeduction = 0.0
                var weatherCost = 0.0
                if (ev.isOutdoor && ev.weather == "RAINY") {
                    val hasCustomTent = (biz.eoCustomAssets ?: emptyList()).any { it.type.lowercase() == "tent & truss" && it.quantity > 0 }
                    val hasTent = (biz.eoOwnedAssets["Tent"] ?: 0) > 0 || ev.rentedAssets.contains("Tent") || hasCustomTent
                    val hasTruss = (biz.eoOwnedAssets["Truss"] ?: 0) > 0 || ev.rentedAssets.contains("Truss") || hasCustomTent
                    if (hasTent || hasTruss) {
                        weatherCost = 1000.0
                    } else {
                        weatherDeduction = 25.0
                        weatherCost = 5000.0
                    }
                }
                
                // Incident impact
                var incidentDeduction = 0.0
                if (ev.activeIncident != null) {
                    if (!ev.incidentResolved) {
                        incidentDeduction = when (ev.activeIncident) {
                            "GENSET_BROKEN" -> 30.0
                            "VENDOR_LATE" -> 15.0
                            "ARTIST_CANCELED" -> 40.0
                            "TICKETS_OVERSOLD" -> 25.0
                            "DEMONSTRATION" -> 20.0
                            "STRONG_WINDS" -> 30.0
                            else -> 20.0
                        }
                    } else {
                        incidentDeduction = ev.incidentImpactQuality
                    }
                }
                
                val finalQuality = (baseQuality - requirementDeduction - weatherDeduction - incidentDeduction).coerceIn(0.0, 100.0)
                
                // 2. Star rating
                var rating = when {
                    finalQuality >= 90.0 -> 5.0
                    finalQuality >= 75.0 -> 4.0
                    finalQuality >= 55.0 -> 3.0
                    finalQuality >= 35.0 -> 2.0
                    else -> 1.0
                }
                if (divisions.contains("Talent")) {
                    rating = (rating + 0.3).coerceAtMost(5.0)
                }
                
                val reviewText = when {
                    rating >= 4.8 -> "SANGAT LUAR BIASA! Semua terencana dengan sempurna, profesional, dan klien sangat puas dengan hasil kerja kami!"
                    rating >= 4.0 -> "Acara berjalan dengan lancar dan cukup rapi. Klien puas dan memberikan feedback positif."
                    rating >= 3.0 -> "Biasa saja. Beberapa kendala teknis kecil mengganggu jalannya acara, tapi overall ok."
                    rating >= 2.0 -> "Cukup mengecewakan. Banyak fasilitas utama yang kurang lengkap dan tidak sesuai ekspektasi."
                    else -> "Bencana total! Acara hancur lebur, genset bermasalah, dan penonton ricuh! Media massa menyoroti kegagalan fatal kami."
                }
                
                // 3. Profit calculations
                var profit = ev.eoFee
                var multiplier = 1.0
                if (divisions.contains("Production")) multiplier += 0.10
                if (divisions.contains("Finance")) multiplier += 0.05
                profit *= multiplier
                profit -= weatherCost
                
                val updatedEv = ev.copy(
                    phase = "REVIEW",
                    quality = finalQuality,
                    resultRating = rating,
                    resultReviewText = reviewText,
                    finalProfit = profit
                )
                
                biz.copy(
                    activeEvents = biz.activeEvents.map { if (it.id == eventId) updatedEv else it }
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun collectEventEarnings(instanceId: String, eventId: String): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val ev = biz.activeEvents.find { it.id == eventId }
            if (ev == null || ev.phase != "REVIEW") {
                errorMsg = "Event belum selesai direview."
                biz
            } else {
                val rating = ev.resultRating
                val divisions = biz.eoDivisions ?: emptySet()
                
                // Reputation change
                var repChange = when {
                    rating >= 4.5 -> 8.0
                    rating >= 3.5 -> 4.0
                    rating >= 2.5 -> 1.0
                    rating >= 1.5 -> -5.0
                    else -> -15.0
                }
                if (repChange > 0 && divisions.contains("Marketing")) {
                    repChange *= 1.20
                }
                if (repChange < 0 && divisions.contains("Legal")) {
                    repChange *= 0.50
                }
                
                val newReputation = (biz.eoReputation + repChange).coerceIn(0.0, 100.0)
                
                // Prestige points
                val basePrestigeReward = when (ev.category) {
                    "Birthday Party" -> 1
                    "Wedding" -> 2
                    "Graduation" -> 2
                    "Corporate Gathering" -> 3
                    "Concert" -> 5
                    "Festival" -> 6
                    "Exhibition" -> 4
                    "Sports Event" -> 5
                    "Government Event" -> 6
                    "International Summit" -> 10
                    else -> 1
                }
                val totalPrestigeReward = if (ev.isSpecial) ev.requiredPrestige else basePrestigeReward
                
                val newPrestige = biz.eoPrestige + totalPrestigeReward
                val newSpecialEvents = if (ev.isSpecial) biz.eoCompletedSpecialEvents + ev.name else biz.eoCompletedSpecialEvents
                val totalPayout = ev.totalBudget + ev.finalProfit
                
                biz.copy(
                    companyCash = biz.companyCash + totalPayout,
                    eoReputation = newReputation,
                    eoPrestige = newPrestige,
                    eoCompletedSpecialEvents = newSpecialEvents,
                    activeEvents = biz.activeEvents.filter { it.id != eventId }
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun upgradeEoHq(instanceId: String): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val currentHq = biz.eoCompanyHqLevel ?: "HOUSE"
            val nextHq = when (currentHq) {
                "HOUSE" -> "OFFICE"
                "OFFICE" -> "REGIONAL"
                "REGIONAL" -> "NATIONAL"
                "NATIONAL" -> "INTERNATIONAL"
                else -> null
            }
            if (nextHq == null) {
                errorMsg = "HQ sudah berada di tingkat maksimal!"
                biz
            } else {
                val cost = getHqUpgradeCost(nextHq)
                if (biz.companyCash < cost) {
                    errorMsg = "Kas internal tidak cukup untuk upgrade HQ ($${String.format("%,.0f", cost)})."
                    biz
                } else {
                    biz.copy(
                        eoCompanyHqLevel = nextHq,
                        companyCash = biz.companyCash - cost
                    )
                }
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun hireEoDivision(instanceId: String, divName: String): String? {
        var errorMsg: String? = null
        val cost = getDivisionHiringCost(divName)
        val ok = updateEoBusiness(instanceId) { biz ->
            val hq = biz.eoCompanyHqLevel ?: "HOUSE"
            val divisions = biz.eoDivisions ?: emptySet()
            
            val isLocked = when (hq) {
                "HOUSE" -> !listOf("Production", "Logistics", "Creative", "Marketing").contains(divName)
                "OFFICE" -> !listOf("Production", "Logistics", "Creative", "Marketing", "Sales", "Finance", "Multimedia").contains(divName)
                "REGIONAL" -> !listOf("Production", "Logistics", "Creative", "Marketing", "Sales", "Finance", "Multimedia", "Talent", "Legal").contains(divName)
                else -> false
            }
            
            val maxDivs = when (hq) {
                "HOUSE" -> 3
                "OFFICE" -> 5
                "REGIONAL" -> 7
                else -> 9
            }
            
            if (isLocked) {
                errorMsg = "Divisi ini terkunci untuk tingkat HQ saat ini (${getHqDisplayName(hq)}). Upgrade HQ Anda terlebih dahulu!"
                biz
            } else if (divisions.size >= maxDivs) {
                errorMsg = "Kapasitas divisi penuh! HQ ${getHqDisplayName(hq)} hanya dapat menampung maksimal $maxDivs divisi. Upgrade HQ Anda!"
                biz
            } else if (divisions.contains(divName)) {
                errorMsg = "Divisi ini sudah dibentuk."
                biz
            } else if (biz.companyCash < cost) {
                errorMsg = "Kas internal tidak cukup untuk membentuk divisi $divName ($${String.format("%,.0f", cost)})."
                biz
            } else {
                biz.copy(
                    eoDivisions = divisions + divName,
                    companyCash = biz.companyCash - cost
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun buyEoAsset(instanceId: String, assetName: String): String? {
        var errorMsg: String? = null
        val cost = getAssetPurchasePrice(assetName)
        val ok = updateEoBusiness(instanceId) { biz ->
            val hq = biz.eoCompanyHqLevel ?: "HOUSE"
            val currentTotal = (biz.eoOwnedAssets ?: emptyMap()).values.sum() + (biz.eoCustomAssets ?: emptyList()).sumOf { it.quantity }
            val capacity = when (hq) {
                "HOUSE" -> 5
                "OFFICE" -> 15
                "REGIONAL" -> 40
                "NATIONAL" -> 100
                else -> 9999
            }
            if (currentTotal + 1 > capacity) {
                errorMsg = "Gudang penuh! HQ ${getHqDisplayName(hq)} hanya menampung maksimal $capacity unit aset. Upgrade HQ Anda!"
                biz
            } else if (biz.companyCash < cost) {
                errorMsg = "Kas internal tidak cukup untuk membeli aset $assetName ($${String.format("%,.0f", cost)})."
                biz
            } else {
                val owned = biz.eoOwnedAssets ?: emptyMap()
                val currentCount = owned[assetName] ?: 0
                val updatedOwned = owned.toMutableMap()
                updatedOwned[assetName] = currentCount + 1
                biz.copy(
                    eoOwnedAssets = updatedOwned,
                    companyCash = biz.companyCash - cost
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun buyEoCustomAsset(instanceId: String, name: String, type: String, quantity: Int, priceUnit: Double, imageUrl: String?): String? {
        var errorMsg: String? = null
        val totalCost = priceUnit * quantity
        val ok = updateEoBusiness(instanceId) { biz ->
            val hq = biz.eoCompanyHqLevel ?: "HOUSE"
            val currentTotal = (biz.eoOwnedAssets ?: emptyMap()).values.sum() + (biz.eoCustomAssets ?: emptyList()).sumOf { it.quantity }
            val capacity = when (hq) {
                "HOUSE" -> 5
                "OFFICE" -> 15
                "REGIONAL" -> 40
                "NATIONAL" -> 100
                else -> 9999
            }
            if (currentTotal + quantity > capacity) {
                errorMsg = "Gudang penuh! HQ ${getHqDisplayName(hq)} hanya menampung maksimal $capacity unit aset. Upgrade HQ Anda!"
                biz
            } else if (biz.companyCash < totalCost) {
                errorMsg = "Kas internal tidak cukup untuk membeli aset ini ($${String.format("%,.0f", totalCost)})."
                biz
            } else {
                val newAsset = com.example.data.EoCustomAsset(
                    name = name,
                    type = type,
                    quantity = quantity,
                    priceUnit = priceUnit,
                    imageUrl = if (imageUrl.isNullOrBlank()) null else imageUrl
                )
                biz.copy(
                    eoCustomAssets = (biz.eoCustomAssets ?: emptyList()) + newAsset,
                    companyCash = biz.companyCash - totalCost
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun editEoCustomAsset(instanceId: String, assetId: String, newName: String, newImageUrl: String?): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val exists = biz.eoCustomAssets?.any { it.id == assetId } ?: false
            if (!exists) {
                errorMsg = "Aset tidak ditemukan."
                biz
            } else {
                val updatedList = biz.eoCustomAssets.map { asset ->
                    if (asset.id == assetId) {
                        asset.copy(
                            name = newName,
                            imageUrl = if (newImageUrl.isNullOrBlank()) null else newImageUrl
                        )
                    } else asset
                }
                biz.copy(eoCustomAssets = updatedList)
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    fun sellEoCustomAsset(instanceId: String, assetId: String): String? {
        var errorMsg: String? = null
        val ok = updateEoBusiness(instanceId) { biz ->
            val asset = biz.eoCustomAssets?.find { it.id == assetId }
            if (asset == null) {
                errorMsg = "Aset tidak ditemukan."
                biz
            } else {
                val returnAmount = asset.priceUnit * asset.quantity * 0.5
                val updatedList = biz.eoCustomAssets.filter { it.id != assetId }
                biz.copy(
                    eoCustomAssets = updatedList,
                    companyCash = biz.companyCash + returnAmount
                )
            }
        }
        if (!ok) return "Bisnis tidak ditemukan."
        return errorMsg
    }

    data class EventTemplate(
        val category: String,
        val minPax: Int,
        val maxPax: Int,
        val baseCostPerPax: Double,
        val feePercent: Double,
        val isOutdoor: Boolean,
        val requirements: List<String>,
        val requiredHqs: List<String>,
        val minReputation: Double,
        val tier: Int
    )

    private val eventTemplates = listOf(
        EventTemplate("Birthday Party", 50, 200, 30.0, 0.30, false, listOf("Sound", "Lighting"), listOf("HOUSE", "OFFICE", "REGIONAL", "NATIONAL", "INTERNATIONAL"), 0.0, 2),
        EventTemplate("Wedding", 200, 1000, 50.0, 0.35, true, listOf("Stage", "Sound", "Lighting", "LED"), listOf("HOUSE", "OFFICE", "REGIONAL", "NATIONAL", "INTERNATIONAL"), 10.0, 3),
        EventTemplate("Graduation", 300, 1500, 20.0, 0.20, false, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security"), listOf("HOUSE", "OFFICE", "REGIONAL", "NATIONAL", "INTERNATIONAL"), 20.0, 3),
        EventTemplate("Corporate Gathering", 100, 500, 80.0, 0.25, false, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security"), listOf("OFFICE", "REGIONAL", "NATIONAL", "INTERNATIONAL"), 30.0, 4),
        EventTemplate("Concert", 3000, 20000, 40.0, 0.30, true, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance"), listOf("REGIONAL", "NATIONAL", "INTERNATIONAL"), 50.0, 4),
        EventTemplate("Festival", 5000, 40000, 50.0, 0.40, true, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance", "Tent"), listOf("NATIONAL", "INTERNATIONAL"), 65.0, 4),
        EventTemplate("Exhibition", 1000, 10000, 35.0, 0.25, false, listOf("LED", "Power", "Security", "Toilet", "Barricade", "Forklift", "Truck"), listOf("OFFICE", "REGIONAL", "NATIONAL", "INTERNATIONAL"), 40.0, 4),
        EventTemplate("Sports Event", 5000, 30000, 25.0, 0.20, true, listOf("Sound", "Power", "Security", "Toilet", "Barricade", "Ambulance"), listOf("REGIONAL", "NATIONAL", "INTERNATIONAL"), 55.0, 4),
        EventTemplate("Government Event", 500, 3000, 60.0, 0.35, false, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security"), listOf("REGIONAL", "NATIONAL", "INTERNATIONAL"), 70.0, 4),
        EventTemplate("International Summit", 1000, 5000, 150.0, 0.30, false, listOf("Stage", "Sound", "Lighting", "LED", "Power", "Security", "Toilet", "Barricade", "Ambulance", "Helicopter"), listOf("NATIONAL", "INTERNATIONAL"), 85.0, 5)
    )

    fun generateEventRequestsForBusiness(owned: com.example.data.OwnedBusiness): List<com.example.data.EventProject> {
        val hq = owned.eoCompanyHqLevel ?: "HOUSE"
        val reputation = owned.eoReputation
        val divisions = owned.eoDivisions ?: emptySet()
        
        val filtered = eventTemplates.filter { t ->
            t.requiredHqs.contains(hq) && reputation >= t.minReputation
        }
        val pool = if (filtered.isEmpty()) {
            eventTemplates.filter { it.category == "Birthday Party" }
        } else {
            filtered
        }
        
        var count = (2..5).random()
        if (divisions.contains("Marketing")) {
            count += (1..2).random()
        }
        
        val list = mutableListOf<com.example.data.EventProject>()
        for (i in 0 until count) {
            val template = pool.random()
            val pax = (template.minPax..template.maxPax).random()
            var tb = pax * template.baseCostPerPax * (0.8 + kotlin.random.Random.nextDouble(0.0, 0.4))
            
            if (divisions.contains("Sales")) {
                tb *= 1.15
            }
            
            val eoFee = tb * template.feePercent
            val techFee = tb * 0.40
            val weather = if (kotlin.random.Random.nextDouble() < 0.30) "RAINY" else "SUNNY"
            
            val clientPrefix = when (template.tier) {
                2 -> listOf("Bpk. Budi", "Siska & Rio", "Ibu Dewi", "SMA 1 Merdeka").random()
                3 -> listOf("CV Jaya Makmur", "Universitas Abadi", "Pemkot Regional", "Konser Indie Bandung").random()
                4 -> listOf("PT Telkom Indonesia", "Pesta Rakyat Raya", "Pemerintah Nasional", "Festival Musik Kebangsaan").random()
                else -> listOf("G20 Secretariat", "World Expo Group", "Asian Games Council", "International Summit Org").random()
            }
            
            list.add(com.example.data.EventProject(
                name = "$clientPrefix - ${template.category}",
                category = template.category,
                pax = pax,
                totalBudget = tb,
                eoFee = eoFee,
                techFee = techFee,
                useInHouseTech = false,
                executionEndTime = 0L,
                tier = template.tier,
                isSpecial = false,
                weather = weather,
                isOutdoor = template.isOutdoor,
                requirements = template.requirements,
                phase = "PLANNING"
            ))
        }
        return list
    }

    fun completeEventProject(instanceId: String, eventId: String) {
        // Keeps backwards compatibility of the method name just in case
        calculateEventResults(instanceId, eventId)
    }

    // ==========================================
    // FAMILY OFFICE ACTIONS
    // ==========================================

    fun updateMonthlyCeoSalary(salary: Long) {
        val state = _playerState.value
        _playerState.value = state.copy(monthlyCeoSalary = salary)
        saveState(_playerState.value)
    }

    fun submitCeoSalaryRequest(proposedPercent: Double) {
        val state = _playerState.value
        _playerState.value = state.copy(
            pendingCeoSalaryPercent = proposedPercent,
            boardApprovalMonthsLeft = 2
        )
        saveState(_playerState.value)
    }

    fun dismissBoardReplyMessage() {
        val state = _playerState.value
        _playerState.value = state.copy(boardReplyMessage = null)
        saveState(_playerState.value)
    }

    fun submitDividendRequest(proposedPercent: Double) {
        val state = _playerState.value
        _playerState.value = state.copy(
            pendingDividendPercent = proposedPercent,
            dividendApprovalMonthsLeft = 2
        )
        saveState(_playerState.value)
    }

    fun submitTantiemRequest(proposedPercent: Double) {
        val state = _playerState.value
        _playerState.value = state.copy(
            pendingTantiemPercent = proposedPercent,
            tantiemApprovalMonthsLeft = 2
        )
        saveState(_playerState.value)
    }

    fun withdrawCorporateDividends(percent: Double): String? {
        val state = _playerState.value
        val totalHoldingCash = state.holdingCompanies.sumOf { it.holdingCash } + state.ownedBusinesses.sumOf { it.companyCash }
        if (totalHoldingCash <= 0.0) return "Tidak ada kas holding/perusahaan untuk ditarik."
        
        val amountRequested = totalHoldingCash * (percent / 100.0)
        if (amountRequested <= 0) return "Jumlah penarikan harus lebih besar dari 0"
        
        var remainingDeduct = amountRequested
        val updatedHoldings = state.holdingCompanies.map { h ->
            if (remainingDeduct <= 0.0) h
            else {
                val v = h.holdingCash
                val d = Math.min(v, remainingDeduct)
                remainingDeduct -= d
                h.copy(holdingCash = v - d)
            }
        }
        val updatedBusinesses = state.ownedBusinesses.map { b ->
            if (remainingDeduct <= 0.0) b
            else {
                val v = b.companyCash
                val d = Math.min(v, remainingDeduct)
                remainingDeduct -= d
                b.copy(companyCash = v - d)
            }
        }
        
        val taxRate = if (state.privateTaxServiceLevel == 2) 0.05 else 0.15
        val taxPaid = (amountRequested * taxRate).toLong()
        val netToPlayer = (amountRequested - taxPaid).toLong()
        
        val newRetainedEarnings = (state.retainedEarnings - amountRequested.toLong()).coerceAtLeast(0L)
        
        val intermediateState = state.copy(
            holdingCompanies = updatedHoldings,
            ownedBusinesses = updatedBusinesses,
            privateBalance = state.privateBalance + netToPlayer,
            corporateTaxPaid = state.corporateTaxPaid + taxPaid,
            totalTaxPaid = state.totalTaxPaid + taxPaid,
            retainedEarnings = newRetainedEarnings
        )
        val newState = logToPrivateLedger(intermediateState, "Pencairan Dividen Perusahaan", netToPlayer, true)
        
        val newsList = listOf(
            MarketNews(
                id = "fo_dividend_${System.currentTimeMillis()}",
                text = "FAMILY OFFICE: Berhasil mencairkan dividen perusahaan sebesar $${com.example.ui.formatCurrencyRingkas(amountRequested, false)}. Dikenakan pajak dividen ${(taxRate * 100).toInt()}% ($${com.example.ui.formatCurrencyRingkas(taxPaid.toDouble(), false)}). Bersih masuk kas pribadi: $${com.example.ui.formatCurrencyRingkas(netToPlayer.toDouble(), false)}.",
                type = "BULL"
            )
        ) + _newsFeed.value
        _newsFeed.value = newsList.take(20)
        
        _playerState.value = newState
        saveState(_playerState.value)
        return null
    }

    fun borrowLombardLoan(amount: Long, limitLTV: Double = 0.20): String? {
        val state = _playerState.value
        
        val totalBusinessValuation = state.ownedBusinesses.sumOf {
            val catalogItem = com.example.data.getCatalogItem(it.catalogId, state)
            if (catalogItem != null) com.example.data.getBusinessValuation(it, catalogItem) else 0L
        }
        val totalHoldingValuation = state.holdingCompanies.sumOf { holding ->
            holding.subsidiaries.sumOf { sub ->
                val catalogItem = com.example.data.getCatalogItem(sub.catalogId, state)
                if (catalogItem != null) com.example.data.getBusinessValuation(sub, catalogItem) else 0L
            }
        }
        val businessValuation = totalBusinessValuation + totalHoldingValuation
        val maxAllowedDebt = (businessValuation * limitLTV).toLong()
        
        if (state.personalDebt + amount > maxAllowedDebt) {
            return "Limit utang terlampaui! Maksimal LTV adalah 20% dari Valuasi Bisnis ($${com.example.ui.formatCurrencyRingkas(maxAllowedDebt.toDouble(), false)})."
        }
        
        val nextState = state.copy(
            personalDebt = state.personalDebt + amount,
            privateBalance = state.privateBalance + amount
        )
        val newState = logToPrivateLedger(nextState, "Pencairan Pinjaman Lombard (Agunan Saham)", amount, true)
        
        val newsList = listOf(
            MarketNews(
                id = "fo_lombard_borrow_${System.currentTimeMillis()}",
                text = "LOMBARD LOAN: Anda berhasil mencairkan pinjaman agunan saham sebesar $${com.example.ui.formatCurrencyRingkas(amount.toDouble(), false)}. Kas Pribadi bertambah, utang bertambah.",
                type = "BULL"
            )
        ) + _newsFeed.value
        _newsFeed.value = newsList.take(20)
        
        _playerState.value = newState
        saveState(_playerState.value)
        return null
    }

    fun injectCapitalToCompany(amount: Long): String? {
        val result = com.example.privateequity.capitalinjection.CapitalInjectionEngine.injectCapitalToCompany(
            state = _playerState.value,
            amount = amount
        )
        if (!result.isSuccess || result.updatedState == null) {
            return result.message
        }
        _playerState.value = result.updatedState
        saveState(result.updatedState)
        if (result.newsFeedItem != null) {
            _newsFeed.value = (listOf(result.newsFeedItem) + _newsFeed.value).take(20)
        }
        return null
    }

    fun applyForInvestorsLoan(sectorName: String, loanAmount: Long, tenorMonths: Int, interestRate: Double, dilutionMultiplier: Double, fundingType: com.example.privateequity.model.FundingType): String? {
        val result = com.example.privateequity.engine.PrivateEquityEngine.applyForInvestorLoan(
            state = _playerState.value,
            sectorName = sectorName,
            loanAmount = loanAmount,
            tenorMonths = tenorMonths,
            interestRate = interestRate,
            dilutionMultiplier = dilutionMultiplier,
            fundingType = fundingType
        )
        if (!result.isSuccess || result.updatedState == null) {
            return result.message
        }
        _playerState.value = result.updatedState
        saveState(result.updatedState)
        if (result.newsFeedItem != null) {
            _newsFeed.value = (listOf(result.newsFeedItem) + _newsFeed.value).take(20)
        }
        return null
    }

    fun repayLombardLoan(amount: Long): String? {
        val state = _playerState.value
        if (amount <= 0) return "Jumlah pembayaran harus lebih dari 0"
        if (state.privateBalance < amount) return "Kas pribadi Anda tidak mencukupi untuk pembayaran ini."
        
        val repayAmount = Math.min(amount, state.personalDebt)
        
        val nextState = state.copy(
            personalDebt = state.personalDebt - repayAmount,
            privateBalance = state.privateBalance - repayAmount
        )
        val newState = logToPrivateLedger(nextState, "Pelunasan Pinjaman Lombard", repayAmount, false)
        
        val newsList = listOf(
            MarketNews(
                id = "fo_lombard_repay_${System.currentTimeMillis()}",
                text = "LOMBARD LOAN: Anda membayar utang agunan saham sebesar $${com.example.ui.formatCurrencyRingkas(repayAmount.toDouble(), false)} menggunakan kas pribadi.",
                type = "NEUTRAL"
            )
        ) + _newsFeed.value
        _newsFeed.value = newsList.take(20)
        
        _playerState.value = newState
        saveState(_playerState.value)
        return null
    }

    fun sellMegaHoldingShares(percent: Double): String? {
        val state = _playerState.value
        if (percent <= 0.0 || percent > state.companyOwnershipPercent) {
            return "Persentase penjualan tidak valid atau melampaui kepemilikan Anda saat ini."
        }
        if (state.companyOwnershipPercent - percent < 51.0) {
            return "Anda tidak boleh menjual saham jika total kepemilikan Anda akan jatuh di bawah 51% (Syarat mutlak kontrol perusahaan)."
        }
        
        val totalBusinessValuation = state.ownedBusinesses.sumOf {
            val catalogItem = com.example.data.getCatalogItem(it.catalogId, state)
            if (catalogItem != null) com.example.data.getBusinessValuation(it, catalogItem) else 0L
        }
        val totalHoldingValuation = state.holdingCompanies.sumOf { holding ->
            holding.subsidiaries.sumOf { sub ->
                val catalogItem = com.example.data.getCatalogItem(sub.catalogId, state)
                if (catalogItem != null) com.example.data.getBusinessValuation(sub, catalogItem) else 0L
            }
        }
        val businessValuation = totalBusinessValuation + totalHoldingValuation
        
        val transactionValue = (businessValuation * (percent / 100.0)).toLong()
        val newOwnership = (state.companyOwnershipPercent - percent).coerceAtLeast(0.0)
        
        val intermediateState = state.copy(
            companyOwnershipPercent = newOwnership,
            playerEquityShare = newOwnership,
            privateBalance = state.privateBalance + transactionValue,
            megaHolding = state.megaHolding.copy(ownershipPercentage = newOwnership)
        )
        val newState = logToPrivateLedger(intermediateState, "Penjualan Saham Holding (${String.format(java.util.Locale.US, "%.1f", percent)}%)", transactionValue, true)
        
        val newsList = listOf(
            MarketNews(
                id = "fo_shares_sale_${System.currentTimeMillis()}",
                text = "SECONDARY SALE: Menjual ${String.format(java.util.Locale.US, "%.1f", percent)}% saham kepemilikan holding seharga $${com.example.ui.formatCurrencyRingkas(transactionValue.toDouble(), false)} kepada investor privat. Dana masuk kas pribadi.",
                type = "BULL"
            )
        ) + _newsFeed.value
        _newsFeed.value = newsList.take(20)
        
        _playerState.value = newState
        saveState(_playerState.value)
        return null
    }

    fun buybackMegaHoldingShares(percent: Double): String? {
        val state = _playerState.value
        if (percent <= 0.0) {
            return "Persentase buyback tidak valid."
        }
        if (state.companyOwnershipPercent >= 100.0) {
            return "Kepemilikan saham Anda sudah 100%."
        }
        val maxBuyback = 100.0 - state.companyOwnershipPercent
        val actualPercent = percent.coerceAtMost(maxBuyback)

        val totalBusinessValuation = state.ownedBusinesses.sumOf {
            val catalogItem = com.example.data.getCatalogItem(it.catalogId, state)
            if (catalogItem != null) com.example.data.getBusinessValuation(it, catalogItem) else 0L
        }
        val totalHoldingValuation = state.holdingCompanies.sumOf { holding ->
            holding.subsidiaries.sumOf { sub ->
                val catalogItem = com.example.data.getCatalogItem(sub.catalogId, state)
                if (catalogItem != null) com.example.data.getBusinessValuation(sub, catalogItem) else 0L
            }
        }
        val businessValuation = (totalBusinessValuation + totalHoldingValuation).coerceAtLeast(100000L)
        val buybackCost = (businessValuation * (actualPercent / 100.0)).toLong()

        if (state.privateBalance < buybackCost) {
            return "Kas pribadi tidak mencukupi untuk buyback ${String.format(java.util.Locale.US, "%.1f", actualPercent)}% saham ($${com.example.ui.formatCurrencyRingkas(buybackCost.toDouble(), false)})."
        }

        val newOwnership = (state.companyOwnershipPercent + actualPercent).coerceAtMost(100.0)

        val intermediateState = state.copy(
            companyOwnershipPercent = newOwnership,
            playerEquityShare = newOwnership,
            privateBalance = state.privateBalance - buybackCost,
            megaHolding = state.megaHolding.copy(ownershipPercentage = newOwnership)
        )
        val newState = logToPrivateLedger(intermediateState, "Buyback Saham Holding (${String.format(java.util.Locale.US, "%.1f", actualPercent)}%)", buybackCost, false)

        val newsList = listOf(
            MarketNews(
                id = "fo_shares_buyback_${System.currentTimeMillis()}",
                text = "SHARE BUYBACK: Membeli kembali ${String.format(java.util.Locale.US, "%.1f", actualPercent)}% saham holding seharga $${com.example.ui.formatCurrencyRingkas(buybackCost.toDouble(), false)} dari investor.",
                type = "BULL"
            )
        ) + _newsFeed.value
        _newsFeed.value = newsList.take(20)

        _playerState.value = newState
        saveState(_playerState.value)
        return null
    }

    fun calculateProgressiveTax(income: Long, serviceLevel: Int = 0): Long {
        if (income <= 0) return 0L
        var tax = 0.0
        var remaining = income.toDouble()
        
        // Bracket 1: 0 - 50,000 (5%)
        val b1 = Math.min(remaining, 50000.0)
        tax += b1 * 0.05
        remaining -= b1
        
        // Bracket 2: 50,001 - 250,000 (15%) -> size 200,000
        if (remaining > 0) {
            val b2 = Math.min(remaining, 200000.0)
            tax += b2 * 0.15
            remaining -= b2
        }
        
        // Bracket 3: 250,001 - 500,000 (25%) -> size 250,000
        if (remaining > 0) {
            val b3 = Math.min(remaining, 250000.0)
            tax += b3 * 0.25
            remaining -= b3
        }
        
        // Bracket 4: 500,001 - 5,000,000 (30% -> 25% if Tax Lawyer)
        if (remaining > 0) {
            val b4 = Math.min(remaining, 4500000.0)
            val r4 = if (serviceLevel == 2) 0.25 else 0.30
            tax += b4 * r4
            remaining -= b4
        }
        
        // Bracket 5: > 5,000,000 (35% -> 25% if Tax Lawyer)
        if (remaining > 0) {
            val r5 = if (serviceLevel == 2) 0.25 else 0.35
            tax += remaining * r5
        }
        
        return tax.toLong()
    }

    fun setPrivateTaxServiceLevel(level: Int) {
        val currentState = _playerState.value
        _playerState.value = currentState.copy(privateTaxServiceLevel = level)
        saveState(_playerState.value)
    }

    fun reportSptTahunan(): String {
        val currentState = _playerState.value
        _playerState.value = currentState.copy(
            isSptReportedThisYear = true,
            consecutiveUnreportedSpt = 0
        )
        saveState(_playerState.value)
        return "SPT Tahunan & Audit Aset berhasil dilaporkan untuk tahun ini!"
    }

    // ==========================================
    // LIFESTYLE & PERSONAL SPENDING ACTIONS
    // ==========================================

    fun toggleSubscription(name: String, monthlyCost: Long) {
        val state = _playerState.value
        val isCurrentlyActive = state.activeSubscriptions.contains(name)
        val nextSubscriptions = if (isCurrentlyActive) {
            state.activeSubscriptions.filter { it != name }
        } else {
            state.activeSubscriptions + name
        }
        val nextCost = if (isCurrentlyActive) {
            (state.monthlyLifestyleCost - monthlyCost).coerceAtLeast(0L)
        } else {
            state.monthlyLifestyleCost + monthlyCost
        }
        val nextAllSubs = state.allSubscriptions.map { sub ->
            if (sub.name == name) {
                sub.copy(isActive = !isCurrentlyActive)
            } else {
                sub
            }
        }
        _playerState.value = state.copy(
            activeSubscriptions = nextSubscriptions,
            allSubscriptions = nextAllSubs,
            monthlyLifestyleCost = nextCost
        )
        saveState(_playerState.value)
    }

    fun addLifestyleItem(
        tabCategory: String,
        sectionName: String,
        name: String,
        price: Long,
        imgUrl: String,
        desc: String,
        isRecurring: Boolean = false
    ) {
        val state = _playerState.value
        val newItem = com.example.data.LifestyleItem(
            id = java.util.UUID.randomUUID().toString(),
            tabCategory = tabCategory,
            sectionName = sectionName,
            name = name,
            price = price,
            imgUrl = imgUrl,
            desc = desc,
            isActive = false,
            isOwned = false,
            isCustom = true,
            isRecurring = isRecurring,
            fundedCount = 0
        )
        val nextAllItems = state.allSubscriptions + newItem
        _playerState.value = state.copy(
            allSubscriptions = nextAllItems
        )
        saveState(_playerState.value)
    }

    fun updateLifestyleItem(
        id: String,
        name: String,
        price: Long,
        sectionName: String,
        desc: String,
        imgUrl: String,
        isRecurring: Boolean = false
    ) {
        val state = _playerState.value
        val nextAllItems = state.allSubscriptions.map { item ->
            if (item.id == id) {
                val priceDiff = price - item.price
                if (item.isActive && (item.tabCategory == "langganan" || item.tabCategory == "wellness" || item.tabCategory == "pengeluaran_kustom")) {
                    val nextCost = (state.monthlyLifestyleCost + priceDiff).coerceAtLeast(0L)
                    _playerState.value = _playerState.value.copy(monthlyLifestyleCost = nextCost)
                }
                item.copy(name = name, price = price, sectionName = sectionName, desc = desc, imgUrl = imgUrl, isRecurring = isRecurring)
            } else {
                item
            }
        }
        _playerState.value = _playerState.value.copy(
            allSubscriptions = nextAllItems
        )
        saveState(_playerState.value)
    }

    fun deleteLifestyleItem(id: String) {
        val state = _playerState.value
        val itemToDelete = state.allSubscriptions.find { it.id == id } ?: return
        
        val nextCost = if (itemToDelete.isActive && (itemToDelete.tabCategory == "langganan" || itemToDelete.tabCategory == "wellness" || itemToDelete.tabCategory == "pengeluaran_kustom")) {
            (state.monthlyLifestyleCost - itemToDelete.price).coerceAtLeast(0L)
        } else {
            state.monthlyLifestyleCost
        }
        
        val nextAllItems = state.allSubscriptions.filter { it.id != id }
        _playerState.value = state.copy(
            allSubscriptions = nextAllItems,
            monthlyLifestyleCost = nextCost
        )
        saveState(_playerState.value)
    }

    fun toggleLifestyleItemActive(id: String) {
        val state = _playerState.value
        val nextAllItems = state.allSubscriptions.map { item ->
            if (item.id == id) {
                val nextActive = !item.isActive
                val costDiff = if (nextActive) item.price else -item.price
                val nextCost = (state.monthlyLifestyleCost + costDiff).coerceAtLeast(0L)
                
                val nextActiveSubs = if (nextActive) {
                    state.activeSubscriptions + item.name
                } else {
                    state.activeSubscriptions.filter { it != item.name }
                }
                
                _playerState.value = _playerState.value.copy(
                    activeSubscriptions = nextActiveSubs,
                    monthlyLifestyleCost = nextCost
                )
                item.copy(isActive = nextActive)
            } else {
                item
            }
        }
        _playerState.value = _playerState.value.copy(
            allSubscriptions = nextAllItems
        )
        saveState(_playerState.value)
    }

    fun purchaseLifestyleItemOwned(id: String): Boolean {
        val state = _playerState.value
        val item = state.allSubscriptions.find { it.id == id } ?: return false
        if (state.privateBalance < item.price) return false
        
        val reducedState = state.copy(
            privateBalance = state.privateBalance - item.price,
            ownedGadgets = if (item.tabCategory == "gadget") state.ownedGadgets + item.name else state.ownedGadgets,
            totalCharityDonated = if (item.tabCategory == "filantropi") state.totalCharityDonated + item.price else state.totalCharityDonated
        )
        
        val nextAllItems = reducedState.allSubscriptions.map { itm ->
            if (itm.id == id) {
                itm.copy(isOwned = true, fundedCount = itm.fundedCount + 1)
            } else {
                itm
            }
        }
        
        val finalState = reducedState.copy(allSubscriptions = nextAllItems)
        val ledgerTitle = when (item.tabCategory) {
            "gadget" -> "Beli Gadget: ${item.name}"
            "filantropi" -> "Donasi Filantropi: ${item.name}"
            "pengeluaran_kustom" -> "Danai Proposal: ${item.name}"
            else -> "Beli Item: ${item.name}"
        }
        _playerState.value = logToPrivateLedger(finalState, ledgerTitle, item.price, false)
        saveState(_playerState.value)
        return true
    }

    fun reFundLifestyleProposal(id: String): Boolean {
        val state = _playerState.value
        val item = state.allSubscriptions.find { it.id == id } ?: return false
        if (state.privateBalance < item.price) return false

        val reducedState = state.copy(
            privateBalance = state.privateBalance - item.price
        )
        val nextAllItems = reducedState.allSubscriptions.map { itm ->
            if (itm.id == id) {
                itm.copy(isOwned = true, fundedCount = itm.fundedCount + 1)
            } else {
                itm
            }
        }
        val finalState = reducedState.copy(allSubscriptions = nextAllItems)
        val ledgerTitle = "Danai Ulang: ${item.name}"
        _playerState.value = logToPrivateLedger(finalState, ledgerTitle, item.price, false)
        saveState(_playerState.value)
        return true
    }

    fun goOnLifestyleExpedition(id: String): Boolean {
        val state = _playerState.value
        val item = state.allSubscriptions.find { it.id == id } ?: return false
        if (state.privateBalance < item.price) return false
        
        val reducedState = state.copy(
            privateBalance = state.privateBalance - item.price,
            travelHistory = state.travelHistory + 1
        )
        
        val ledgerTitle = "Pergi Liburan: ${item.name}"
        _playerState.value = logToPrivateLedger(reducedState, ledgerTitle, item.price, false)
        saveState(_playerState.value)
        return true
    }

    fun bookPrivateTravel(destinationId: String, days: Int, totalCost: Long, extraDetails: String): Boolean {
        val state = _playerState.value
        val destination = state.travelDestinations.find { it.id == destinationId } ?: return false
        if (state.privateBalance < totalCost) return false
        
        val reducedState = state.copy(
            privateBalance = state.privateBalance - totalCost,
            travelHistory = state.travelHistory + 1,
            totalTripsTaken = state.totalTripsTaken + 1
        )
        
        val ledgerTitle = "Travel Concierge: ${destination.name} ($days Hari - $extraDetails)"
        _playerState.value = logToPrivateLedger(reducedState, ledgerTitle, totalCost, false)
        saveState(_playerState.value)
        return true
    }

    fun addCustomTravelDestination(name: String, region: String, pricePerDay: Long, imageUrl: String) {
        val state = _playerState.value
        val newDestination = com.example.data.TravelDestination(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            region = region,
            pricePerDay = pricePerDay,
            imageUrl = imageUrl,
            isCustom = true
        )
        val nextDestinations = state.travelDestinations + newDestination
        _playerState.value = state.copy(
            travelDestinations = nextDestinations
        )
        saveState(_playerState.value)
    }

    fun deleteTravelDestination(destinationId: String) {
        val state = _playerState.value
        val nextDestinations = state.travelDestinations.filter { it.id != destinationId }
        _playerState.value = state.copy(
            travelDestinations = nextDestinations
        )
        saveState(_playerState.value)
    }

    fun editTravelDestinationImageUrl(destinationId: String, newImageUrl: String) {
        val state = _playerState.value
        val nextDestinations = state.travelDestinations.map {
            if (it.id == destinationId) {
                it.copy(imageUrl = newImageUrl)
            } else {
                it
            }
        }
        _playerState.value = state.copy(
            travelDestinations = nextDestinations
        )
        saveState(_playerState.value)
    }

    fun purchaseTechGadget(name: String, price: Long): Boolean {
        val state = _playerState.value
        if (state.privateBalance < price) return false
        
        val nextGadgets = state.ownedGadgets + name
        val reducedState = state.copy(
            privateBalance = state.privateBalance - price,
            ownedGadgets = nextGadgets
        )
        val nextAllSubs = reducedState.allSubscriptions.map { sub ->
            if (sub.name == name) {
                sub.copy(isOwned = true)
            } else {
                sub
            }
        }
        _playerState.value = logToPrivateLedger(reducedState.copy(allSubscriptions = nextAllSubs), "Beli Gadget: $name", price, false)
        saveState(_playerState.value)
        return true
    }

    fun goOnTravelExpedition(name: String, cost: Long): Boolean {
        val state = _playerState.value
        if (state.privateBalance < cost) return false
        
        val reducedState = state.copy(
            privateBalance = state.privateBalance - cost,
            travelHistory = state.travelHistory + 1
        )
        _playerState.value = logToPrivateLedger(reducedState, "Pergi Liburan: $name", cost, false)
        saveState(_playerState.value)
        return true
    }

    fun donateToCharity(amount: Long): Boolean {
        val state = _playerState.value
        if (amount <= 0 || state.privateBalance < amount) return false
        
        val reducedState = state.copy(
            privateBalance = state.privateBalance - amount,
            totalCharityDonated = state.totalCharityDonated + amount
        )
        _playerState.value = logToPrivateLedger(reducedState, "Donasi Amal Kemanusiaan", amount, false)
        saveState(_playerState.value)
        return true
    }

    fun createPrivateFoundation(name: String, type: com.example.data.FoundationType): Boolean {
        val state = _playerState.value
        if (state.privateBalance < type.legalCost) return false

        val newFoundation = com.example.data.FoundationEntity(
            name = name,
            type = type,
            constructionMonthsLeft = type.setupMonths,
            isLegalized = false,
            educationInstitutions = emptyList()
        )
        val nextFoundations = state.foundations + newFoundation
        val reducedState = state.copy(
            privateBalance = state.privateBalance - type.legalCost,
            foundations = nextFoundations
        )
        _playerState.value = logToPrivateLedger(reducedState, "Mendirikan Yayasan: $name (${type.label})", type.legalCost, false)
        saveState(_playerState.value)
        return true
    }

    fun deletePrivateFoundation(foundationId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val nextFoundations = state.foundations.filter { it.id != foundationId }
        val updatedState = state.copy(
            foundations = nextFoundations
        )
        _playerState.value = logToPrivateLedger(updatedState, "Menghibahkan Yayasan: ${foundation.name}", 0L, false)
        saveState(_playerState.value)
        return true
    }

    fun injectEndowmentFund(foundationId: String, amount: Long): Boolean {
        val state = _playerState.value
        if (state.privateBalance < amount || amount <= 0) return false

        val nextFoundations = state.foundations.map {
            if (it.id == foundationId) {
                it.copy(endowmentFund = it.endowmentFund + amount)
            } else {
                it
            }
        }
        val reducedState = state.copy(
            privateBalance = state.privateBalance - amount,
            foundations = nextFoundations
        )
        val fName = state.foundations.find { it.id == foundationId }?.name ?: "Yayasan"
        _playerState.value = logToPrivateLedger(reducedState, "Suntik Dana Abadi: $fName", amount, false)
        saveState(_playerState.value)
        return true
    }

    fun buildFoundationFacility(
        foundationId: String,
        name: String,
        category: String,
        tier: String,
        buildCost: Long,
        buildMonths: Int,
        monthlyOps: Long,
        prestigeReward: Long
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        if (!foundation.isLegalized) return false
        if (foundation.endowmentFund < buildCost) return false

        val newFacility = com.example.data.FoundationFacility(
            name = name,
            category = category,
            tier = tier,
            buildCost = buildCost,
            buildMonthsLeft = buildMonths,
            monthlyOperationalCost = monthlyOps,
            prestigeReward = prestigeReward,
            isOperational = false
        )
        
        val nextFoundations = state.foundations.map {
            if (it.id == foundationId) {
                it.copy(
                    endowmentFund = it.endowmentFund - buildCost,
                    facilities = it.facilities + newFacility
                )
            } else {
                it
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun buildEducationInstitution(foundationId: String, name: String, level: String): Boolean {
        return buildEducationInstitution(foundationId, name, level, "Grade A", 0L)
    }

    fun buildEducationInstitution(
        foundationId: String,
        name: String,
        level: String,
        buildingGrade: String,
        baseMaintenanceCost: Long
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        if (!foundation.isLegalized) return false
        
        val cost = when (level) {
            "TK" -> 200000L
            "SD" -> 500000L
            "SMA" -> 1500000L
            "UNIV" -> 5000000L
            else -> 200000L
        }
        
        if (foundation.endowmentFund < cost) return false
        
        val basePrestige = when (level) {
            "TK" -> 5
            "SD" -> 15
            "SMA" -> 40
            "UNIV" -> 100
            else -> 5
        }
        
        val defaultCurriculum = if (level == "UNIV") "Nasional (Teaching Univ)" else "Nasional"
        
        val gradeObj = com.example.data.BUILDING_GRADES.find { it.name == buildingGrade }
        val duration = gradeObj?.constructionMonths ?: 0

        val newInst = com.example.data.EducationInstitution(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            level = level,
            curriculumType = defaultCurriculum,
            facilityLevel = 1,
            accreditationPoints = 0,
            monthlyOperationalCost = com.example.data.calculateEduOperationalCost(level, 1, defaultCurriculum),
            prestigeScore = basePrestige,
            imageUrl = "",
            currentStudents = 0,
            monthlySpp = 0L,
            buildingGrade = buildingGrade,
            baseMaintenanceCost = baseMaintenanceCost,
            additionalFacilities = emptyList(),
            constructionMonthsTotal = duration,
            constructionMonthsLeft = duration,
            isOperational = false
        )
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    endowmentFund = f.endowmentFund - cost,
                    educationInstitutions = (f.educationInstitutions ?: emptyList()) + newInst
                )
            } else {
                f
            }
        }
        
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun activateEducationInstitution(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f -> f.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val randomStudents = when (inst.level) {
            "TK" -> (100..300).random()
            "SD" -> (200..600).random()
            "SMA" -> (300..800).random()
            "UNIV" -> (5000..15000).random()
            else -> 0
        }
        
        val updatedInst = inst.copy(
            isOperational = true,
            currentStudents = randomStudents
        )
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateEducationInstitutionProfile(foundationId: String, institutionId: String, newName: String, newImageUrl: String): Boolean {
        val state = _playerState.value
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                val updatedInstitutions = (f.educationInstitutions ?: emptyList()).map { inst ->
                    if (inst.id == institutionId) {
                        inst.copy(name = newName, imageUrl = newImageUrl)
                    } else {
                        inst
                    }
                }
                f.copy(educationInstitutions = updatedInstitutions)
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun upgradeEduFacility(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        if (inst.facilityLevel >= 5) return false

        val baseUpgradeCost = when (inst.level) {
            "TK" -> 150000L
            "SD" -> 400000L
            "SMA" -> 1200000L
            "UNIV" -> 4000000L
            else -> 150000L
        }
        val cost = baseUpgradeCost * inst.facilityLevel

        if (foundation.endowmentFund < cost) return false

        val nextLevel = inst.facilityLevel + 1
        val basePrestige = when (inst.level) {
            "TK" -> 5
            "SD" -> 15
            "SMA" -> 40
            "UNIV" -> 100
            else -> 5
        }
        val nextPrestige = basePrestige * nextLevel
        val nextOps = com.example.data.calculateEduOperationalCost(inst.level, nextLevel, inst.curriculumType)

        val updatedInst = inst.copy(
            facilityLevel = nextLevel,
            prestigeScore = nextPrestige,
            monthlyOperationalCost = nextOps
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    endowmentFund = f.endowmentFund - cost,
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun changeEduCurriculum(foundationId: String, institutionId: String, newCurriculum: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val nextStudents = if (inst.level == "TK") {
            when (newCurriculum) {
                "Nasional" -> (100..300).random()
                "Montessori" -> (60..90).random()
                "Waldorf" -> (30..40).random()
                else -> (100..300).random()
            }
        } else if (inst.level == "SD") {
            when (newCurriculum) {
                "Nasional" -> (200..600).random()
                "Agama Terpadu" -> (150..450).random()
                "Nasional Plus (Bilingual)" -> (100..300).random()
                "Cambridge Primary" -> (80..200).random()
                else -> (200..600).random()
            }
        } else if (inst.level == "SMA") {
            when (newCurriculum) {
                "Nasional" -> (300..800).random()
                "Kejuruan (SMK)" -> (250..600).random()
                "Cambridge (A-Level)" -> (150..400).random()
                "IB (International Baccalaureate)" -> (100..300).random()
                else -> (300..800).random()
            }
        } else if (inst.level == "UNIV") {
            when (newCurriculum) {
                "Nasional (Teaching Univ)" -> (5000..15000).random()
                "Internasional (Double Degree)" -> (3000..8000).random()
                "World-Class Research Univ" -> (1000..4000).random()
                else -> (5000..15000).random()
            }
        } else {
            inst.currentStudents
        }

        val nextOps = com.example.data.calculateEduOperationalCost(inst.level, inst.facilityLevel, newCurriculum)
        val updatedInst = inst.copy(
            curriculumType = newCurriculum,
            monthlyOperationalCost = nextOps,
            currentStudents = nextStudents
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun deleteEducationInstitution(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).filter { it.id != institutionId }
                )
            } else {
                f
            }
        }
        val updatedState = state.copy(foundations = nextFoundations)
        _playerState.value = logToPrivateLedger(updatedState, "Menghibahkan Fasilitas: ${inst.name} (${inst.level})", 0L, false)
        saveState(_playerState.value)
        return true
    }

    fun updateEducationInstitutionSpp(foundationId: String, institutionId: String, newSpp: Long): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedInst = inst.copy(monthlySpp = newSpp)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateInstitutionCurriculum(foundationId: String, institutionId: String, newCurriculum: String): Boolean {
        return changeEduCurriculum(foundationId, institutionId, newCurriculum)
    }

    fun updateInstitutionCurriculum(institutionId: String, newCurriculum: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f ->
            (f.educationInstitutions ?: emptyList()).any { it.id == institutionId }
        } ?: return false
        return changeEduCurriculum(foundation.id, institutionId, newCurriculum)
    }

    fun updateInstitutionSpp(foundationId: String, institutionId: String, inputSpp: Long): Boolean {
        return updateEducationInstitutionSpp(foundationId, institutionId, inputSpp)
    }

    fun updateInstitutionSpp(institutionId: String, inputSpp: Long): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f ->
            (f.educationInstitutions ?: emptyList()).any { it.id == institutionId }
        } ?: return false
        return updateEducationInstitutionSpp(foundation.id, institutionId, inputSpp)
    }

    fun buildAdditionalFacility(
        foundationId: String,
        institutionId: String,
        typeId: String,
        name: String,
        customName: String,
        gradeId: String,
        maintenanceCost: Long,
        constructionCost: Long,
        constructionTotalMonths: Int,
        constructionLeftMonths: Int
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        
        // Jelas jika saldo tidak cukup
        if (foundation.endowmentFund < constructionCost) {
            return false
        }
        
        val newFacility = com.example.data.FacilityItem(
            id = java.util.UUID.randomUUID().toString(),
            typeId = typeId,
            name = name,
            baseName = name,
            customName = if (customName.isBlank()) name else customName,
            gradeName = gradeId,
            maintenanceCost = maintenanceCost,
            constructionTotalMonths = constructionTotalMonths,
            constructionLeftMonths = constructionLeftMonths
        )
        
        val updatedFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                val updatedInstitutions = (f.educationInstitutions ?: emptyList()).map { inst ->
                    if (inst.id == institutionId) {
                        inst.copy(
                            additionalFacilities = (inst.additionalFacilities ?: emptyList()) + newFacility,
                            prestigeScore = inst.prestigeScore + when (typeId) {
                                "tk_playground" -> 8
                                "tk_pool" -> 5
                                "tk_computer_lab" -> 12
                                "tk_nap_room" -> 6
                                else -> 5
                            },
                            accreditationPoints = Math.min(100, inst.accreditationPoints + when (typeId) {
                                "tk_playground" -> 5
                                "tk_pool" -> 2
                                "tk_computer_lab" -> 10
                                "tk_nap_room" -> 4
                                else -> 3
                            })
                        )
                    } else {
                        inst
                    }
                }
                f.copy(
                    endowmentFund = f.endowmentFund - constructionCost,
                    educationInstitutions = updatedInstitutions
                )
            } else {
                f
            }
        }
        
        _playerState.value = state.copy(foundations = updatedFoundations)
        saveState(_playerState.value)
        return true
    }

    fun hireTeacher(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedTeachers = when (type) {
            "umum" -> inst.teachers.copy(umum = inst.teachers.umum.copy(
                target = inst.teachers.umum.target + 1,
                recruiting = inst.teachers.umum.recruiting + 1
            ))
            "spesialis" -> inst.teachers.copy(spesialis = inst.teachers.spesialis.copy(
                target = inst.teachers.spesialis.target + 1,
                recruiting = inst.teachers.spesialis.recruiting + 1
            ))
            "senior" -> inst.teachers.copy(senior = inst.teachers.senior.copy(
                target = inst.teachers.senior.target + 1,
                recruiting = inst.teachers.senior.recruiting + 1
            ))
            else -> inst.teachers
        }
        
        val updatedInst = inst.copy(teachers = updatedTeachers)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun fireTeacher(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedTeachers = when (type) {
            "umum" -> {
                val r = inst.teachers.umum
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.teachers.copy(umum = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "spesialis" -> {
                val r = inst.teachers.spesialis
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.teachers.copy(spesialis = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "senior" -> {
                val r = inst.teachers.senior
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.teachers.copy(senior = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            else -> inst.teachers
        }
        
        val updatedInst = inst.copy(teachers = updatedTeachers)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun hireSupportStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedSupport = when (type) {
            "janitor" -> inst.supportStaff.copy(ob = inst.supportStaff.ob.copy(
                target = inst.supportStaff.ob.target + 1,
                recruiting = inst.supportStaff.ob.recruiting + 1
            ))
            "security" -> inst.supportStaff.copy(satpam = inst.supportStaff.satpam.copy(
                target = inst.supportStaff.satpam.target + 1,
                recruiting = inst.supportStaff.satpam.recruiting + 1
            ))
            "admin" -> inst.supportStaff.copy(admin = inst.supportStaff.admin.copy(
                target = inst.supportStaff.admin.target + 1,
                recruiting = inst.supportStaff.admin.recruiting + 1
            ))
            "chef" -> inst.supportStaff.copy(chef = inst.supportStaff.chef.copy(
                target = inst.supportStaff.chef.target + 1,
                recruiting = inst.supportStaff.chef.recruiting + 1
            ))
            else -> inst.supportStaff
        }
        
        val updatedInst = inst.copy(supportStaff = updatedSupport)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun fireSupportStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedSupport = when (type) {
            "janitor" -> {
                val r = inst.supportStaff.ob
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(ob = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "security" -> {
                val r = inst.supportStaff.satpam
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(satpam = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "admin" -> {
                val r = inst.supportStaff.admin
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(admin = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "chef" -> {
                val r = inst.supportStaff.chef
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(chef = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            else -> inst.supportStaff
        }
        
        val updatedInst = inst.copy(supportStaff = updatedSupport)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateStaffSalary(
        foundationId: String,
        institutionId: String,
        isTeacher: Boolean,
        roleType: String,
        newSalary: Long
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f -> f.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedInst = if (isTeacher) {
            val updatedTeachers = when (roleType) {
                "umum" -> inst.teachers.copy(umum = inst.teachers.umum.copy(customSalary = newSalary))
                "spesialis" -> inst.teachers.copy(spesialis = inst.teachers.spesialis.copy(customSalary = newSalary))
                "senior" -> inst.teachers.copy(senior = inst.teachers.senior.copy(customSalary = newSalary))
                else -> inst.teachers
            }
            inst.copy(teachers = updatedTeachers)
        } else {
            val updatedSupport = when (roleType) {
                "janitor" -> inst.supportStaff.copy(ob = inst.supportStaff.ob.copy(customSalary = newSalary))
                "security" -> inst.supportStaff.copy(satpam = inst.supportStaff.satpam.copy(customSalary = newSalary))
                "admin" -> inst.supportStaff.copy(admin = inst.supportStaff.admin.copy(customSalary = newSalary))
                "chef" -> inst.supportStaff.copy(chef = inst.supportStaff.chef.copy(customSalary = newSalary))
                else -> inst.supportStaff
            }
            inst.copy(supportStaff = updatedSupport)
        }
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun renameFacility(foundationId: String, institutionId: String, facilityId: String, newName: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).map { fac ->
            if (fac.id == facilityId) {
                fac.copy(customName = newName)
            } else {
                fac
            }
        }
        
        val updatedInst = inst.copy(additionalFacilities = updatedFacilities)
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun deleteFacility(foundationId: String, institutionId: String, facilityId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.educationInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).filter { it.id != facilityId }
        
        val updatedInst = inst.copy(additionalFacilities = updatedFacilities)
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    educationInstitutions = (f.educationInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun buildHealthInstitution(foundationId: String, name: String, level: String): Boolean {
        return buildHealthInstitution(foundationId, name, level, "Grade A", 0L)
    }

    fun buildHealthInstitution(
        foundationId: String,
        name: String,
        level: String,
        buildingGrade: String,
        baseMaintenanceCost: Long
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        if (!foundation.isLegalized) return false
        
        val cost = when (level) {
            "Klinik" -> 300000L
            "RS Umum" -> 1000000L
            "RS Khusus" -> 3000000L
            "RS Internasional" -> 10000000L
            else -> 300000L
        }
        
        if (foundation.endowmentFund < cost) return false
        
        val basePrestige = when (level) {
            "Klinik" -> 8
            "RS Umum" -> 25
            "RS Khusus" -> 70
            "RS Internasional" -> 180
            else -> 8
        }
        
        val defaultServiceType = "Reguler"
        
        val gradeObj = com.example.data.BUILDING_GRADES.find { it.name == buildingGrade }
        val duration = gradeObj?.constructionMonths ?: 0

        val newInst = com.example.data.HealthInstitution(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            level = level,
            serviceType = defaultServiceType,
            facilityLevel = 1,
            accreditationPoints = 0,
            monthlyOperationalCost = 0L,
            prestigeScore = basePrestige,
            imageUrl = "",
            currentPatients = 0,
            monthlyBillPerPatient = 0L,
            buildingGrade = buildingGrade,
            baseMaintenanceCost = baseMaintenanceCost,
            additionalFacilities = emptyList(),
            constructionMonthsTotal = duration,
            constructionMonthsLeft = duration,
            isOperational = false
        )
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    endowmentFund = f.endowmentFund - cost,
                    healthInstitutions = (f.healthInstitutions ?: emptyList()) + newInst
                )
            } else {
                f
            }
        }
        
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun activateHealthInstitution(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f -> f.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val randomPatients = when (inst.level) {
            "Klinik" -> (15..50).random()
            "RS Umum" -> (80..200).random()
            "RS Khusus" -> (120..350).random()
            "RS Internasional" -> (200..600).random()
            else -> 0
        }
        
        val updatedInst = inst.copy(
            isOperational = true,
            currentPatients = randomPatients
        )
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateHealthInstitutionProfile(foundationId: String, institutionId: String, newName: String, newImageUrl: String): Boolean {
        val state = _playerState.value
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                val updatedInstitutions = (f.healthInstitutions ?: emptyList()).map { inst ->
                    if (inst.id == institutionId) {
                        inst.copy(name = newName, imageUrl = newImageUrl)
                    } else {
                        inst
                    }
                }
                f.copy(healthInstitutions = updatedInstitutions)
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun upgradeHealthFacility(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        if (inst.facilityLevel >= 5) return false

        val baseUpgradeCost = when (inst.level) {
            "Klinik" -> 250000L
            "RS Umum" -> 800000L
            "RS Khusus" -> 2500000L
            "RS Internasional" -> 8000000L
            else -> 250000L
        }
        val cost = baseUpgradeCost * inst.facilityLevel

        if (foundation.endowmentFund < cost) return false

        val nextLevel = inst.facilityLevel + 1
        val basePrestige = when (inst.level) {
            "Klinik" -> 8
            "RS Umum" -> 25
            "RS Khusus" -> 70
            "RS Internasional" -> 180
            else -> 8
        }
        val nextPrestige = basePrestige * nextLevel

        val updatedInst = inst.copy(
            facilityLevel = nextLevel,
            prestigeScore = nextPrestige
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    endowmentFund = f.endowmentFund - cost,
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun changeHealthServiceType(foundationId: String, institutionId: String, newServiceType: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val nextPatients = when (newServiceType) {
            "Subsidi" -> (50..150).random()
            "Reguler" -> (30..100).random()
            "VIP" -> (15..45).random()
            "VVIP" -> (5..20).random()
            else -> (30..100).random()
        }

        val updatedInst = inst.copy(
            serviceType = newServiceType,
            currentPatients = nextPatients
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun deleteHealthInstitution(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).filter { it.id != institutionId }
                )
            } else {
                f
            }
        }
        val updatedState = state.copy(foundations = nextFoundations)
        _playerState.value = logToPrivateLedger(updatedState, "Menghibahkan Layanan Medis: ${inst.name} (${inst.level})", 0L, false)
        saveState(_playerState.value)
        return true
    }

    fun updateHealthInstitutionBill(foundationId: String, institutionId: String, newBill: Long): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedInst = inst.copy(monthlyBillPerPatient = newBill)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateHealthBill(institutionId: String, inputBill: Long): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f ->
            (f.healthInstitutions ?: emptyList()).any { it.id == institutionId }
        } ?: return false
        return updateHealthInstitutionBill(foundation.id, institutionId, inputBill)
    }

    fun buildHealthAdditionalFacility(
        foundationId: String,
        institutionId: String,
        typeId: String,
        name: String,
        customName: String,
        gradeId: String,
        maintenanceCost: Long,
        constructionCost: Long,
        constructionTotalMonths: Int,
        constructionLeftMonths: Int
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        
        if (foundation.endowmentFund < constructionCost) {
            return false
        }
        
        val newFacility = com.example.data.FacilityItem(
            id = java.util.UUID.randomUUID().toString(),
            typeId = typeId,
            name = name,
            baseName = name,
            customName = if (customName.isBlank()) name else customName,
            gradeName = gradeId,
            maintenanceCost = maintenanceCost,
            constructionTotalMonths = constructionTotalMonths,
            constructionLeftMonths = constructionLeftMonths
        )
        
        val updatedFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                val updatedInstitutions = (f.healthInstitutions ?: emptyList()).map { inst ->
                    if (inst.id == institutionId) {
                        inst.copy(
                            additionalFacilities = (inst.additionalFacilities ?: emptyList()) + newFacility,
                            prestigeScore = inst.prestigeScore + 10,
                            accreditationPoints = Math.min(100, inst.accreditationPoints + 8)
                        )
                    } else {
                        inst
                    }
                }
                f.copy(
                    endowmentFund = f.endowmentFund - constructionCost,
                    healthInstitutions = updatedInstitutions
                )
            } else {
                f
            }
        }
        
        _playerState.value = state.copy(foundations = updatedFoundations)
        saveState(_playerState.value)
        return true
    }

    fun hireMedicalStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedMedical = when (type) {
            "perawat" -> inst.medicalStaff.copy(perawat = inst.medicalStaff.perawat.copy(
                target = inst.medicalStaff.perawat.target + 1,
                recruiting = inst.medicalStaff.perawat.recruiting + 1
            ))
            "dokterUmum" -> inst.medicalStaff.copy(dokterUmum = inst.medicalStaff.dokterUmum.copy(
                target = inst.medicalStaff.dokterUmum.target + 1,
                recruiting = inst.medicalStaff.dokterUmum.recruiting + 1
            ))
            "dokterSpesialis" -> inst.medicalStaff.copy(dokterSpesialis = inst.medicalStaff.dokterSpesialis.copy(
                target = inst.medicalStaff.dokterSpesialis.target + 1,
                recruiting = inst.medicalStaff.dokterSpesialis.recruiting + 1
            ))
            else -> inst.medicalStaff
        }
        
        val updatedInst = inst.copy(medicalStaff = updatedMedical)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun fireMedicalStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedMedical = when (type) {
            "perawat" -> {
                val r = inst.medicalStaff.perawat
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.medicalStaff.copy(perawat = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "dokterUmum" -> {
                val r = inst.medicalStaff.dokterUmum
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.medicalStaff.copy(dokterUmum = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "dokterSpesialis" -> {
                val r = inst.medicalStaff.dokterSpesialis
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.medicalStaff.copy(dokterSpesialis = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            else -> inst.medicalStaff
        }
        
        val updatedInst = inst.copy(medicalStaff = updatedMedical)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun hireHealthSupportStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedSupport = when (type) {
            "janitor" -> inst.supportStaff.copy(ob = inst.supportStaff.ob.copy(
                target = inst.supportStaff.ob.target + 1,
                recruiting = inst.supportStaff.ob.recruiting + 1
            ))
            "security" -> inst.supportStaff.copy(satpam = inst.supportStaff.satpam.copy(
                target = inst.supportStaff.satpam.target + 1,
                recruiting = inst.supportStaff.satpam.recruiting + 1
            ))
            "admin" -> inst.supportStaff.copy(admin = inst.supportStaff.admin.copy(
                target = inst.supportStaff.admin.target + 1,
                recruiting = inst.supportStaff.admin.recruiting + 1
            ))
            "chef" -> inst.supportStaff.copy(chef = inst.supportStaff.chef.copy(
                target = inst.supportStaff.chef.target + 1,
                recruiting = inst.supportStaff.chef.recruiting + 1
            ))
            else -> inst.supportStaff
        }
        
        val updatedInst = inst.copy(supportStaff = updatedSupport)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun fireHealthSupportStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedSupport = when (type) {
            "janitor" -> {
                val r = inst.supportStaff.ob
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(ob = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "security" -> {
                val r = inst.supportStaff.satpam
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(satpam = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "admin" -> {
                val r = inst.supportStaff.admin
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(admin = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "chef" -> {
                val r = inst.supportStaff.chef
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.supportStaff.copy(chef = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            else -> inst.supportStaff
        }
        
        val updatedInst = inst.copy(supportStaff = updatedSupport)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateHealthStaffSalary(
        foundationId: String,
        institutionId: String,
        isMedical: Boolean,
        roleType: String,
        newSalary: Long
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f -> f.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedInst = if (isMedical) {
            val updatedMedical = when (roleType) {
                "perawat" -> inst.medicalStaff.copy(perawat = inst.medicalStaff.perawat.copy(customSalary = newSalary))
                "dokterUmum" -> inst.medicalStaff.copy(dokterUmum = inst.medicalStaff.dokterUmum.copy(customSalary = newSalary))
                "dokterSpesialis" -> inst.medicalStaff.copy(dokterSpesialis = inst.medicalStaff.dokterSpesialis.copy(customSalary = newSalary))
                else -> inst.medicalStaff
            }
            inst.copy(medicalStaff = updatedMedical)
        } else {
            val updatedSupport = when (roleType) {
                "janitor" -> inst.supportStaff.copy(ob = inst.supportStaff.ob.copy(customSalary = newSalary))
                "security" -> inst.supportStaff.copy(satpam = inst.supportStaff.satpam.copy(customSalary = newSalary))
                "admin" -> inst.supportStaff.copy(admin = inst.supportStaff.admin.copy(customSalary = newSalary))
                "chef" -> inst.supportStaff.copy(chef = inst.supportStaff.chef.copy(customSalary = newSalary))
                else -> inst.supportStaff
            }
            inst.copy(supportStaff = updatedSupport)
        }
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun renameHealthFacility(foundationId: String, institutionId: String, facilityId: String, newName: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).map { fac ->
            if (fac.id == facilityId) {
                fac.copy(customName = newName)
            } else {
                fac
            }
        }
        
        val updatedInst = inst.copy(additionalFacilities = updatedFacilities)
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun deleteHealthFacility(foundationId: String, institutionId: String, facilityId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.healthInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        
        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).filter { it.id != facilityId }
        
        val updatedInst = inst.copy(additionalFacilities = updatedFacilities)
        
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    healthInstitutions = (f.healthInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun buildCharityInstitution(
        foundationId: String,
        name: String,
        level: String,
        buildingGrade: String,
        baseMaintenanceCost: Long
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        if (!foundation.isLegalized) return false

        val cost = when (level) {
            "Humanitarian Aid" -> 150000L
            "Social Care" -> 300000L
            "Disaster Relief" -> 800000L
            "Community Empowerment" -> 500000L
            else -> 150000L
        }

        if (foundation.endowmentFund < cost) return false

        val basePrestige = when (level) {
            "Humanitarian Aid" -> 10
            "Social Care" -> 20
            "Disaster Relief" -> 60
            "Community Empowerment" -> 40
            else -> 10
        }

        val gradeObj = com.example.data.BUILDING_GRADES.find { it.name == buildingGrade }
        val duration = gradeObj?.constructionMonths ?: 1

        val newInst = com.example.data.CharityInstitution(
            id = java.util.UUID.randomUUID().toString(),
            name = name,
            level = level,
            scope = "Lokal",
            facilityLevel = 1,
            accreditationPoints = 0,
            prestigeScore = basePrestige,
            imageUrl = "",
            baseMaintenanceCost = baseMaintenanceCost,
            constructionTotalMonths = duration,
            constructionLeftMonths = duration,
            isOperational = false,
            monthlyBeneficiaries = 0,
            maxCapacity = com.example.data.calculateCharityMaxCapacity(level, "Lokal"),
            additionalFacilities = emptyList(),
            charityStaff = com.example.data.CharityStaff(),
            buildingGrade = buildingGrade
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    endowmentFund = f.endowmentFund - cost,
                    charityInstitutions = (f.charityInstitutions ?: emptyList()) + newInst
                )
            } else {
                f
            }
        }

        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun activateCharityInstitution(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f -> f.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val initialBeneficiaries = when (inst.level) {
            "Humanitarian Aid" -> (10..50).random()
            "Social Care" -> (5..20).random()
            "Disaster Relief" -> (20..80).random()
            "Community Empowerment" -> (10..30).random()
            else -> 5
        }

        val updatedInst = inst.copy(
            isOperational = true,
            monthlyBeneficiaries = initialBeneficiaries
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun changeCharityScope(foundationId: String, institutionId: String, newScope: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val nextCapacity = com.example.data.calculateCharityMaxCapacity(inst.level, newScope)

        val updatedInst = inst.copy(
            scope = newScope,
            maxCapacity = nextCapacity
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun deleteCharityInstitution(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { f -> f.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).filter { it.id != institutionId }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun upgradeCharityInstitutionFacility(foundationId: String, institutionId: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false
        if (inst.facilityLevel >= 5) return false

        val baseUpgradeCost = when (inst.level) {
            "Humanitarian Aid" -> 200000L
            "Social Care" -> 400000L
            "Disaster Relief" -> 1000000L
            "Community Empowerment" -> 600000L
            else -> 200000L
        }
        val cost = baseUpgradeCost * inst.facilityLevel

        if (foundation.endowmentFund < cost) return false

        val nextLevel = inst.facilityLevel + 1
        val basePrestige = when (inst.level) {
            "Humanitarian Aid" -> 15
            "Social Care" -> 30
            "Disaster Relief" -> 90
            "Community Empowerment" -> 60
            else -> 15
        }
        val nextPrestige = basePrestige * nextLevel

        val updatedInst = inst.copy(
            facilityLevel = nextLevel,
            prestigeScore = nextPrestige
        )

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    endowmentFund = f.endowmentFund - cost,
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun hireCharityStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedStaff = when (type) {
            "relawan" -> inst.charityStaff.copy(relawan = inst.charityStaff.relawan.copy(
                target = inst.charityStaff.relawan.target + 1,
                recruiting = inst.charityStaff.relawan.recruiting + 1
            ))
            "staffSosial" -> inst.charityStaff.copy(staffSosial = inst.charityStaff.staffSosial.copy(
                target = inst.charityStaff.staffSosial.target + 1,
                recruiting = inst.charityStaff.staffSosial.recruiting + 1
            ))
            "ahliProgram" -> inst.charityStaff.copy(ahliProgram = inst.charityStaff.ahliProgram.copy(
                target = inst.charityStaff.ahliProgram.target + 1,
                recruiting = inst.charityStaff.ahliProgram.recruiting + 1
            ))
            else -> inst.charityStaff
        }

        val updatedInst = inst.copy(charityStaff = updatedStaff)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun fireCharityStaff(foundationId: String, institutionId: String, type: String): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedStaff = when (type) {
            "relawan" -> {
                val r = inst.charityStaff.relawan
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.charityStaff.copy(relawan = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "staffSosial" -> {
                val r = inst.charityStaff.staffSosial
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.charityStaff.copy(staffSosial = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            "ahliProgram" -> {
                val r = inst.charityStaff.ahliProgram
                val nextTarget = (r.target - 1).coerceAtLeast(0)
                val nextRecruiting = if (r.recruiting > 0) r.recruiting - 1 else 0
                val nextActive = if (r.recruiting == 0 && r.active > 0) r.active - 1 else r.active
                inst.charityStaff.copy(ahliProgram = r.copy(target = nextTarget, recruiting = nextRecruiting, active = nextActive))
            }
            else -> inst.charityStaff
        }

        val updatedInst = inst.copy(charityStaff = updatedStaff)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateCharityStaffSalary(foundationId: String, institutionId: String, type: String, newSalary: Long): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedStaff = when (type) {
            "relawan" -> inst.charityStaff.copy(relawan = inst.charityStaff.relawan.copy(customSalary = newSalary))
            "staffSosial" -> inst.charityStaff.copy(staffSosial = inst.charityStaff.staffSosial.copy(customSalary = newSalary))
            "ahliProgram" -> inst.charityStaff.copy(ahliProgram = inst.charityStaff.ahliProgram.copy(customSalary = newSalary))
            else -> inst.charityStaff
        }

        val updatedInst = inst.copy(charityStaff = updatedStaff)
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun addCharityFacilityItem(
        foundationId: String,
        institutionId: String,
        typeId: String,
        name: String,
        customName: String,
        gradeId: String,
        maintenanceCost: Long,
        constructionCost: Long,
        constructionTotalMonths: Int,
        constructionLeftMonths: Int
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false

        if (foundation.endowmentFund < constructionCost) {
            return false
        }

        val newFacility = com.example.data.FacilityItem(
            id = java.util.UUID.randomUUID().toString(),
            typeId = typeId,
            name = name,
            baseName = name,
            customName = if (customName.isBlank()) name else customName,
            gradeName = gradeId,
            maintenanceCost = maintenanceCost,
            constructionTotalMonths = constructionTotalMonths,
            constructionLeftMonths = constructionLeftMonths
        )

        val updatedFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                val updatedInstitutions = (f.charityInstitutions ?: emptyList()).map { inst ->
                    if (inst.id == institutionId) {
                        inst.copy(
                            additionalFacilities = (inst.additionalFacilities ?: emptyList()) + newFacility,
                            prestigeScore = inst.prestigeScore + 10,
                            accreditationPoints = Math.min(100, inst.accreditationPoints + 5)
                        )
                    } else {
                        inst
                    }
                }
                f.copy(
                    endowmentFund = f.endowmentFund - constructionCost,
                    charityInstitutions = updatedInstitutions
                )
            } else {
                f
            }
        }

        _playerState.value = state.copy(foundations = updatedFoundations)
        saveState(_playerState.value)
        return true
    }

    fun renameCharityFacilityItem(
        foundationId: String,
        institutionId: String,
        facilityId: String,
        newName: String
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).map { fac ->
            if (fac.id == facilityId) fac.copy(customName = newName) else fac
        }

        val updatedInst = inst.copy(additionalFacilities = updatedFacilities)

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun deleteCharityFacilityItem(
        foundationId: String,
        institutionId: String,
        facilityId: String
    ): Boolean {
        val state = _playerState.value
        val foundation = state.foundations.find { it.id == foundationId } ?: return false
        val inst = (foundation.charityInstitutions ?: emptyList()).find { it.id == institutionId } ?: return false

        val updatedFacilities = (inst.additionalFacilities ?: emptyList()).filter { it.id != facilityId }

        val updatedInst = inst.copy(additionalFacilities = updatedFacilities)

        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                f.copy(
                    charityInstitutions = (f.charityInstitutions ?: emptyList()).map { if (it.id == institutionId) updatedInst else it }
                )
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    fun updateCharityInstitutionProfile(foundationId: String, institutionId: String, newName: String, newImageUrl: String): Boolean {
        val state = _playerState.value
        val nextFoundations = state.foundations.map { f ->
            if (f.id == foundationId) {
                val updatedInstitutions = (f.charityInstitutions ?: emptyList()).map { inst ->
                    if (inst.id == institutionId) {
                        inst.copy(name = newName, imageUrl = newImageUrl)
                    } else {
                        inst
                    }
                }
                f.copy(charityInstitutions = updatedInstitutions)
            } else {
                f
            }
        }
        _playerState.value = state.copy(foundations = nextFoundations)
        saveState(_playerState.value)
        return true
    }

    // ==========================================
    // LOGISTICS OPERATIONS (SRC Express)
    // ==========================================
    fun getLogisticsBusiness(instanceId: String): OwnedBusiness? {
        val state = _playerState.value
        return state.ownedBusinesses.find { it.instanceId == instanceId }
            ?: state.ownedBusinesses.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
            ?: state.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
            ?: state.holdingCompanies.flatMap { it.subsidiaries }.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
    }

    fun updateLogisticsBusiness(instanceId: String, transform: (com.example.data.LogisticsCompanyData) -> com.example.data.LogisticsCompanyData) {
        val currentState = _playerState.value
        val newBusinesses = currentState.ownedBusinesses.map { b ->
            if (b.instanceId == instanceId) {
                b.copy(logisticsData = transform(b.logisticsData))
            } else {
                val newSubs = b.subsidiaries.map { sub ->
                    if (sub.instanceId == instanceId) sub.copy(logisticsData = transform(sub.logisticsData)) else sub
                }
                b.copy(subsidiaries = newSubs)
            }
        }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries.map { b ->
                if (b.instanceId == instanceId) {
                    b.copy(logisticsData = transform(b.logisticsData))
                } else {
                    val newInnerSubs = b.subsidiaries.map { sub ->
                        if (sub.instanceId == instanceId) sub.copy(logisticsData = transform(sub.logisticsData)) else sub
                    }
                    b.copy(subsidiaries = newInnerSubs)
                }
            }
            holding.copy(subsidiaries = newSubs)
        }
        _playerState.value = currentState.copy(
            ownedBusinesses = newBusinesses,
            holdingCompanies = newHoldings
        )
    }

    fun deployFleetVehicle(instanceId: String, vehicleId: String) {
        updateLogisticsBusiness(instanceId) { data ->
            LogisticsEngine.deployVehicle(data, vehicleId)
        }
    }

    fun deployAllIdleFleet(instanceId: String) {
        updateLogisticsBusiness(instanceId) { data ->
            LogisticsEngine.deployAllIdle(data)
        }
    }

    fun repairFleetVehicle(instanceId: String, vehicleId: String) {
        updateLogisticsBusiness(instanceId) { data ->
            LogisticsEngine.repairVehicle(data, vehicleId)
        }
    }

    fun repairAllFleet(instanceId: String) {
        updateLogisticsBusiness(instanceId) { data ->
            LogisticsEngine.repairAllVehicles(data)
        }
    }

    fun buyFleetVehicle(instanceId: String, type: com.example.data.LogisticsVehicleType, customName: String? = null) {
        updateLogisticsBusiness(instanceId) { data ->
            LogisticsEngine.buyVehicle(data, type, customName)
        }
    }

    fun upgradeWarehouseCapacity(instanceId: String) {
        updateLogisticsBusiness(instanceId) { data ->
            LogisticsEngine.upgradeWarehouseCapacity(data)
        }
    }

    fun signLogisticsContract(instanceId: String, contractId: String) {
        updateLogisticsBusiness(instanceId) { data ->
            LogisticsEngine.signContract(data, contractId)
        }
    }

    fun cancelLogisticsContract(instanceId: String, contractId: String) {
        updateLogisticsBusiness(instanceId) { data ->
            LogisticsEngine.cancelContract(data, contractId)
        }
    }

    fun researchLogisticsTech(instanceId: String, path: String) {
        updateLogisticsBusiness(instanceId) { data ->
            LogisticsEngine.researchTech(data, path)
        }
    }

    fun toggleLogisticsAutoDispatch(instanceId: String) {
        updateLogisticsBusiness(instanceId) { data ->
            LogisticsEngine.toggleAutoDispatch(data)
        }
    }

    fun injectCapitalToLogistics(instanceId: String, amount: Long) {
        val currentState = _playerState.value
        if (currentState.cash < amount || amount <= 0) return
        _playerState.value = currentState.copy(cash = currentState.cash - amount)
        updateLogisticsBusiness(instanceId) { data ->
            data.copy(internalCash = data.internalCash + amount)
        }
    }

    fun withdrawCapitalFromLogistics(instanceId: String, amount: Long) {
        val b = getLogisticsBusiness(instanceId) ?: return
        if (b.logisticsData.internalCash < amount || amount <= 0) return
        updateLogisticsBusiness(instanceId) { data ->
            data.copy(internalCash = data.internalCash - amount)
        }
        _playerState.update { it.copy(cash = it.cash + amount) }
    }

    // ==========================================
    // APARTMENT PROPERTY OPERATIONS (SRC Grand Apartment)
    // ==========================================
    fun getApartmentBusiness(instanceId: String): OwnedBusiness? {
        val state = _playerState.value
        return state.ownedBusinesses.find { it.instanceId == instanceId }
            ?: state.ownedBusinesses.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
            ?: state.holdingCompanies.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
            ?: state.holdingCompanies.flatMap { it.subsidiaries }.flatMap { it.subsidiaries }.find { it.instanceId == instanceId }
    }

    fun updateApartmentBusiness(instanceId: String, transform: (com.example.data.ApartmentPropertyData) -> com.example.data.ApartmentPropertyData) {
        val currentState = _playerState.value
        val newBusinesses = currentState.ownedBusinesses.map { b ->
            if (b.instanceId == instanceId) {
                b.copy(apartmentData = transform(b.apartmentData))
            } else {
                val newSubs = b.subsidiaries.map { sub ->
                    if (sub.instanceId == instanceId) sub.copy(apartmentData = transform(sub.apartmentData)) else sub
                }
                b.copy(subsidiaries = newSubs)
            }
        }
        val newHoldings = currentState.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries.map { b ->
                if (b.instanceId == instanceId) {
                    b.copy(apartmentData = transform(b.apartmentData))
                } else {
                    val newInnerSubs = b.subsidiaries.map { sub ->
                        if (sub.instanceId == instanceId) sub.copy(apartmentData = transform(sub.apartmentData)) else sub
                    }
                    b.copy(subsidiaries = newInnerSubs)
                }
            }
            holding.copy(subsidiaries = newSubs)
        }
        _playerState.value = currentState.copy(
            ownedBusinesses = newBusinesses,
            holdingCompanies = newHoldings
        )
    }

    fun setApartmentRentPrice(instanceId: String, unitType: com.example.data.ApartmentUnitType, newPrice: Long) {
        updateApartmentBusiness(instanceId) { data ->
            ApartmentEngine.setRentPrice(data, unitType, newPrice)
        }
    }

    fun unlockApartmentUnitType(instanceId: String, unitType: com.example.data.ApartmentUnitType) {
        updateApartmentBusiness(instanceId) { data ->
            ApartmentEngine.unlockUnitCategory(data, unitType)
        }
    }

    fun expandApartmentFloors(instanceId: String) {
        updateApartmentBusiness(instanceId) { data ->
            ApartmentEngine.expandBuildingFloors(data)
        }
    }

    fun installApartmentFacility(instanceId: String, facility: com.example.data.ApartmentFacilityType) {
        updateApartmentBusiness(instanceId) { data ->
            ApartmentEngine.installFacility(data, facility)
        }
    }

    fun resolveApartmentIncident(instanceId: String, incidentId: String) {
        updateApartmentBusiness(instanceId) { data ->
            ApartmentEngine.resolveIncident(data, incidentId)
        }
    }

    fun ignoreApartmentIncident(instanceId: String, incidentId: String) {
        updateApartmentBusiness(instanceId) { data ->
            ApartmentEngine.ignoreIncident(data, incidentId)
        }
    }

    fun injectCapitalToApartment(instanceId: String, amount: Long) {
        val currentState = _playerState.value
        if (currentState.cash < amount || amount <= 0) return
        _playerState.value = currentState.copy(cash = currentState.cash - amount)
        updateApartmentBusiness(instanceId) { data ->
            data.copy(internalCash = data.internalCash + amount)
        }
    }

    fun withdrawCapitalFromApartment(instanceId: String, amount: Long) {
        val b = getApartmentBusiness(instanceId) ?: return
        if (b.apartmentData.internalCash < amount || amount <= 0) return
        updateApartmentBusiness(instanceId) { data ->
            data.copy(internalCash = data.internalCash - amount)
        }
        _playerState.update { it.copy(cash = it.cash + amount) }
    }

    // ==========================================
    // LIQUIDATION & EXIT STRATEGY OPERATIONS
    // ==========================================
    fun liquidateLogisticsBusiness(instanceId: String): Long {
        val currentState = _playerState.value
        val business = getLogisticsBusiness(instanceId) ?: return 0L
        val valuation = LogisticsEngine.calculateLiquidationValuation(business.logisticsData).totalValuation

        val newBusinesses = currentState.ownedBusinesses
            .filter { it.instanceId != instanceId }
            .map { b ->
                b.copy(subsidiaries = b.subsidiaries.filter { it.instanceId != instanceId })
            }

        val newHoldings = currentState.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries
                .filter { it.instanceId != instanceId }
                .map { sub ->
                    sub.copy(subsidiaries = sub.subsidiaries.filter { it.instanceId != instanceId })
                }
            holding.copy(subsidiaries = newSubs)
        }

        _playerState.value = currentState.copy(
            cash = currentState.cash + valuation,
            ownedBusinesses = newBusinesses,
            holdingCompanies = newHoldings
        )
        return valuation
    }

    fun liquidateApartmentBusiness(instanceId: String): Long {
        val currentState = _playerState.value
        val business = getApartmentBusiness(instanceId) ?: return 0L
        val valuation = ApartmentEngine.calculateLiquidationValuation(business.apartmentData).totalValuation

        val newBusinesses = currentState.ownedBusinesses
            .filter { it.instanceId != instanceId }
            .map { b ->
                b.copy(subsidiaries = b.subsidiaries.filter { it.instanceId != instanceId })
            }

        val newHoldings = currentState.holdingCompanies.map { holding ->
            val newSubs = holding.subsidiaries
                .filter { it.instanceId != instanceId }
                .map { sub ->
                    sub.copy(subsidiaries = sub.subsidiaries.filter { it.instanceId != instanceId })
                }
            holding.copy(subsidiaries = newSubs)
        }

        _playerState.value = currentState.copy(
            cash = currentState.cash + valuation,
            ownedBusinesses = newBusinesses,
            holdingCompanies = newHoldings
        )
        return valuation
    }
}