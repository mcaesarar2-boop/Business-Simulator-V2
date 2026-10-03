package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Clean State Repository responsible for local persistence, auto-migration,
 * and thread-safe loading and updating of [PlayerState] and custom asset catalogues.
 */
class GameStateRepository(
    private val context: Context,
    private val prefs: SharedPreferences = context.getSharedPreferences("tycoon_prefs", Context.MODE_PRIVATE),
    private val gson: Gson = Gson(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val saveGameRepository: SaveGameRepository = SaveGameRepository()
) {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _realEstateMarket = MutableStateFlow<List<PropertyItem>>(initialRealEstateCatalog)
    val realEstateMarket: StateFlow<List<PropertyItem>> = _realEstateMarket.asStateFlow()

    private val _collectionList = MutableStateFlow<List<CollectionItem>>(initialCollectionItems + initialVehicleItems)
    val collectionList: StateFlow<List<CollectionItem>> = _collectionList.asStateFlow()

    private val _housingList = MutableStateFlow<List<HousingItem>>(initialHousingItems)
    val housingList: StateFlow<List<HousingItem>> = _housingList.asStateFlow()

    private val _cloudSyncProgress = MutableStateFlow(false)
    val cloudSyncProgress: StateFlow<Boolean> = _cloudSyncProgress.asStateFlow()

    private val _cloudSyncMessage = MutableStateFlow<String?>(null)
    val cloudSyncMessage: StateFlow<String?> = _cloudSyncMessage.asStateFlow()

    private val _lastSyncTimeMs = MutableStateFlow(prefs.getLong("last_cloud_sync_time", 0L))
    val lastSyncTimeMs: StateFlow<Long> = _lastSyncTimeMs.asStateFlow()

    suspend fun loadInitialData() = withContext(Dispatchers.IO) {
        val loaded = loadState()
        _playerState.value = loaded
        _realEstateMarket.value = loadCustomProperties()
        _collectionList.value = loadCustomCollections()
        _housingList.value = loadCustomHousing()
        autoResolveMissingHousing(loaded)
    }

    fun updateState(transform: (PlayerState) -> PlayerState): PlayerState {
        val updated = transform(_playerState.value)
        _playerState.value = updated
        saveState(updated)
        return updated
    }

    fun setStateDirectly(newState: PlayerState, persist: Boolean = true) {
        _playerState.value = newState
        if (persist) {
            saveState(newState)
        }
    }

    fun saveState(state: PlayerState) {
        if (state.cash == -1L) {
            android.util.Log.e("GameStateRepository", "Save blocked due to corrupted state.")
            return
        }
        val jsonOld = prefs.getString("player_state", null)
        if (jsonOld != null) {
            try {
                val oldState = gson.fromJson(jsonOld, PlayerState::class.java)
                if (oldState.netWorth > 10_000_000 && state.netWorth < 1_000_000 && state.lastMonthExpenses < 1_000_000) {
                    android.util.Log.e(
                        "GameStateRepository",
                        "Save blocked due to suspicious drop in net worth: from ${oldState.netWorth} to ${state.netWorth}"
                    )
                    return
                }
            } catch (_: Exception) {}
        }
        prefs.edit().putString("player_state", gson.toJson(state.copy(lastSavedTimeMs = System.currentTimeMillis()))).apply()
    }

    private fun loadState(): PlayerState {
        val json = prefs.getString("player_state", null)
        if (json != null) {
            try {
                val state = gson.fromJson(json, PlayerState::class.java)
                val patchedBusinesses = state.ownedBusinesses?.map { patchOwnedBusiness(it) } ?: emptyList()
                val patchedHoldings = state.holdingCompanies?.map { holding ->
                    val patchedSubs = holding.subsidiaries?.map { patchOwnedBusiness(it) } ?: emptyList()
                    holding.copy(subsidiaries = patchedSubs)
                } ?: emptyList()

                val migratedState = state.copy(
                    rebrandedCompanies = state.rebrandedCompanies ?: emptyMap(),
                    megaHolding = state.megaHolding ?: MegaHoldingState(),
                    ownedBusinesses = patchedBusinesses,
                    holdingCompanies = patchedHoldings,
                    activeInvestorsLoans = state.activeInvestorsLoans ?: emptyList(),
                    privateLedgerHistory = state.privateLedgerHistory ?: emptyList(),
                    financialHistory = state.financialHistory ?: emptyList(),
                    activeSubscriptions = state.activeSubscriptions ?: emptyList(),
                    allSubscriptions = if (state.allSubscriptions.isNullOrEmpty()) {
                        defaultLifestyleItems.map { defaultItem ->
                            val isActive = state.activeSubscriptions?.contains(defaultItem.name) == true
                            val isOwned = state.ownedGadgets?.contains(defaultItem.name) == true
                            defaultItem.copy(isActive = isActive, isOwned = isOwned)
                        }
                    } else {
                        val currentNames = state.allSubscriptions.map { it.name }.toSet()
                        val missingDefaults = defaultLifestyleItems.filterNot { currentNames.contains(it.name) }
                        state.allSubscriptions + missingDefaults
                    },
                    travelDestinations = if (state.travelDestinations.isNullOrEmpty()) {
                        defaultTravelDestinations
                    } else {
                        state.travelDestinations
                    },
                    totalTripsTaken = state.totalTripsTaken,
                    foundations = patchFoundations(state.foundations)
                )

                prefs.edit().putString("player_state", gson.toJson(migratedState)).apply()
                return migratedState
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("GameStateRepository", "Init error parsing JSON: ${e.message}")
                return PlayerState().copy(cash = -1L)
            }
        }
        return PlayerState()
    }

    private fun patchOwnedBusiness(business: OwnedBusiness): OwnedBusiness {
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

    private fun patchFoundations(list: List<FoundationEntity>?): List<FoundationEntity> {
        return try {
            (list ?: emptyList()).map { f ->
                val safeInstitutions = try {
                    (f.educationInstitutions ?: emptyList()).map { inst ->
                        val migratedTeachers = if (inst.teachers != null) {
                            var u = inst.teachers.umum ?: StaffRole()
                            var s = inst.teachers.spesialis ?: StaffRole()
                            var sn = inst.teachers.senior ?: StaffRole()
                            if (u.customSalary == 0L) u = u.copy(customSalary = 3000L)
                            if (s.customSalary == 0L) s = s.copy(customSalary = 5000L)
                            if (sn.customSalary == 0L) sn = sn.copy(customSalary = 8000L)
                            if (u.target == 0 && (u.active > 0 || u.recruiting > 0)) u = u.copy(target = u.active + u.recruiting)
                            if (s.target == 0 && (s.active > 0 || s.recruiting > 0)) s = s.copy(target = s.active + s.recruiting)
                            if (sn.target == 0 && (sn.active > 0 || sn.recruiting > 0)) sn = sn.copy(target = sn.active + sn.recruiting)
                            TeacherStaff(umum = u, spesialis = s, senior = sn)
                        } else TeacherStaff()

                        val migratedSupport = if (inst.supportStaff != null) {
                            var o = inst.supportStaff.ob ?: StaffRole()
                            var sat = inst.supportStaff.satpam ?: StaffRole()
                            var adm = inst.supportStaff.admin ?: StaffRole()
                            var ch = inst.supportStaff.chef ?: StaffRole()
                            if (o.customSalary == 0L) o = o.copy(customSalary = 800L)
                            if (sat.customSalary == 0L) sat = sat.copy(customSalary = 1000L)
                            if (adm.customSalary == 0L) adm = adm.copy(customSalary = 1200L)
                            if (ch.customSalary == 0L) ch = ch.copy(customSalary = 2500L)
                            if (o.target == 0 && (o.active > 0 || o.recruiting > 0)) o = o.copy(target = o.active + o.recruiting)
                            if (sat.target == 0 && (sat.active > 0 || sat.recruiting > 0)) sat = sat.copy(target = sat.active + sat.recruiting)
                            if (adm.target == 0 && (adm.active > 0 || adm.recruiting > 0)) adm = adm.copy(target = adm.active + adm.recruiting)
                            if (ch.target == 0 && (ch.active > 0 || ch.recruiting > 0)) ch = ch.copy(target = ch.active + ch.recruiting)
                            SupportStaff(ob = o, satpam = sat, admin = adm, chef = ch)
                        } else SupportStaff()

                        EducationInstitution(
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
                } catch (_: Exception) {
                    emptyList()
                }

                FoundationEntity(
                    id = f.id ?: java.util.UUID.randomUUID().toString(),
                    name = f.name ?: "Yayasan Tanpa Nama",
                    type = f.type ?: FoundationType.EDUCATION,
                    isLegalized = f.isLegalized,
                    constructionMonthsLeft = f.constructionMonthsLeft,
                    endowmentFund = f.endowmentFund,
                    facilities = f.facilities ?: emptyList(),
                    educationInstitutions = safeInstitutions,
                    healthInstitutions = f.healthInstitutions ?: emptyList(),
                    charityInstitutions = f.charityInstitutions ?: emptyList()
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadCustomProperties(): List<PropertyItem> {
        val json = prefs.getString("property_list_state", null)
        if (json != null) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<PropertyItem>>() {}.type
                val savedList: List<PropertyItem> = gson.fromJson(json, type)
                if (savedList.isNotEmpty()) return savedList
            } catch (_: Exception) {
                prefs.edit().remove("property_list_state").apply()
            }
        }
        return initialRealEstateCatalog
    }

    fun saveProperties(list: List<PropertyItem>) {
        _realEstateMarket.value = list
        prefs.edit().putString("property_list_state", gson.toJson(list)).apply()
    }

    private fun loadCustomCollections(): List<CollectionItem> {
        val baseList = initialCollectionItems + initialVehicleItems
        val json = prefs.getString("collection_list_state", null)
        if (json != null) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<CollectionItem>>() {}.type
                val savedList: List<CollectionItem> = gson.fromJson(json, type)
                if (savedList.isNotEmpty()) {
                    val baseIds = baseList.map { it.id }.toSet()
                    val merged = baseList.toMutableList()
                    for (item in savedList) {
                        val itemId = item.id
                        if (itemId.isNotEmpty() && itemId !in baseIds) {
                            merged.add(item)
                        }
                    }
                    return merged
                }
            } catch (_: Exception) {
                prefs.edit().remove("collection_list_state").apply()
            }
        }
        return baseList
    }

    fun saveCollections(list: List<CollectionItem>) {
        _collectionList.value = list
        prefs.edit().putString("collection_list_state", gson.toJson(list)).apply()
    }

    private fun loadCustomHousing(): List<HousingItem> {
        val baseList = initialHousingItems
        val json = prefs.getString("housing_list_state", null)
        if (json != null) {
            try {
                val type = object : com.google.gson.reflect.TypeToken<List<HousingItem>>() {}.type
                val savedList: List<HousingItem> = gson.fromJson(json, type)
                if (savedList.isNotEmpty()) {
                    val baseIds = baseList.map { it.id }.toSet()
                    val merged = baseList.toMutableList()
                    for (item in savedList) {
                        val itemId = item.id
                        if (itemId.isNotEmpty() && !itemId.startsWith("hs_") && itemId !in baseIds) {
                            merged.add(item)
                        }
                    }
                    return merged
                }
            } catch (_: Exception) {
                prefs.edit().remove("housing_list_state").apply()
            }
        }
        return baseList
    }

    fun saveHousing(list: List<HousingItem>) {
        _housingList.value = list
        prefs.edit().putString("housing_list_state", gson.toJson(list)).apply()
    }

    private fun autoResolveMissingHousing(state: PlayerState) {
        val currentHs = _housingList.value.toMutableList()
        val hsIds = currentHs.map { it.id }.toSet()
        val mutableHsIds = hsIds.toMutableSet()
        var addedAny = false

        for (owned in state.ownedHouses) {
            if (owned.housingId.isNotEmpty() && owned.housingId !in mutableHsIds) {
                val placeholder = HousingItem(
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

        if (addedAny) {
            _housingList.value = currentHs
            saveHousing(currentHs)
        }
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
            val importedState = gson.fromJson(jsonString, PlayerState::class.java)
            if (importedState != null && importedState.cash >= -1L) {
                _playerState.value = importedState
                saveState(importedState)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    suspend fun backupGameToCloud() = withContext(Dispatchers.IO) {
        val uid = authRepository.getCurrentUserId() ?: return@withContext
        val state = _playerState.value
        _cloudSyncProgress.value = true
        _cloudSyncMessage.value = "Menyimpan ke Cloud..."
        val payload = PlayerGameState(
            userId = uid,
            lastSavedMs = System.currentTimeMillis(),
            privateBalance = state.privateBalance,
            totalFortune = state.netWorth,
            inGameMonth = state.inGameMonth,
            inGameYear = state.inGameYear,
            ownedBusinessesCount = state.ownedBusinesses.size,
            fullStateJson = exportSaveGame()
        )
        val res = saveGameRepository.saveGameToCloud(uid, payload)
        _cloudSyncProgress.value = false
        if (res.isSuccess) {
            _lastSyncTimeMs.value = System.currentTimeMillis()
            prefs.edit().putLong("last_cloud_sync_time", _lastSyncTimeMs.value).apply()
            _cloudSyncMessage.value = "Backup Cloud Sukses!"
        } else {
            _cloudSyncMessage.value = "Backup Gagal: ${res.exceptionOrNull()?.message}"
        }
    }

    suspend fun restoreGameFromCloud() = withContext(Dispatchers.IO) {
        val uid = authRepository.getCurrentUserId() ?: return@withContext
        _cloudSyncProgress.value = true
        _cloudSyncMessage.value = "Mengunduh dari Cloud..."
        val res = saveGameRepository.loadGameFromCloud(uid)
        _cloudSyncProgress.value = false
        if (res.isSuccess) {
            val payload = res.getOrNull()
            if (payload != null && payload.fullStateJson.isNotEmpty()) {
                val ok = importSaveGame(payload.fullStateJson)
                if (ok) {
                    _lastSyncTimeMs.value = payload.lastSavedMs
                    prefs.edit().putLong("last_cloud_sync_time", _lastSyncTimeMs.value).apply()
                    _cloudSyncMessage.value = "Data Cloud Berhasil Dipulihkan!"
                } else {
                    _cloudSyncMessage.value = "Gagal memproses data game dari Cloud."
                }
            } else {
                _cloudSyncMessage.value = "Data backup Cloud kosong."
            }
        } else {
            _cloudSyncMessage.value = "Unduh Gagal: ${res.exceptionOrNull()?.message}"
        }
    }
}
