package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class FootballClubManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("football_club_storage", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _clubState = MutableStateFlow(loadClubState())
    val clubState: StateFlow<FootballClubState> = _clubState.asStateFlow()

    private fun loadClubState(): FootballClubState {
        val json = prefs.getString("saved_football_club", null)
        if (json != null) {
            try {
                val state = gson.fromJson(json, FootballClubState::class.java)
                if (state != null && state.isInitialized) {
                    return state
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return FootballClubState()
    }

    private fun saveClubState(state: FootballClubState) {
        prefs.edit().putString("saved_football_club", gson.toJson(state)).apply()
        _clubState.value = state
    }

    // ==============================================================
    // TUGAS 1: SETUP KLUB DENGAN AUTO-FILL SKUAD FALLBACK GENERIK
    // ==============================================================
    fun initializeClub(
        clubName: String,
        leagueName: String,
        chairmanName: String,
        acquisitionPriceUsd: Long = 0L
    ): FootballClubState {
        val league = FootballDatabase.leagues.find { it.name.equals(leagueName, ignoreCase = true) }
            ?: FootballDatabase.leagues.first()

        // Generate complete squad with fallback auto-fill (16 - 24 players)
        val fullSquad = FootballDatabase.generateCompleteSquadForClub(clubName, league)

        // Calculate reputation, transfer budget, wage budget
        val (reputation, transferBudget, wageBudget) = FootballDatabase.calculateInitialClubProfile(clubName, league)

        // TV Rights based on league level
        val tvRights = when (league.level) {
            1 -> if (league.country == "Indonesia") 250_000L else 7_500_000L
            2 -> if (league.country == "Indonesia") 80_000L else 1_800_000L
            3 -> if (league.country == "Indonesia") 25_000L else 400_000L
            else -> 100_000L
        }

        // Initialize Standings
        val clubsInLeague = league.clubs
        val standings = clubsInLeague.map { name ->
            val isUser = name.equals(clubName, ignoreCase = true)
            LeagueStanding(
                clubName = name,
                ovr = if (isUser) {
                    val top11 = fullSquad.sortedByDescending { it.rating }.take(11)
                    if (top11.isNotEmpty()) top11.map { it.rating }.average().toInt() else 75
                } else {
                    reputation - Random.nextInt(-3, 4)
                },
                isUserClub = isUser
            )
        }.sortedByDescending { it.ovr }

        // Generate Fixtures (Home & Away vs all clubs in league, or single round if huge)
        val opponents = clubsInLeague.filterNot { it.equals(clubName, ignoreCase = true) }
        val fixtures = mutableListOf<FootballMatch>()
        var matchdayCount = 1

        for (opp in opponents) {
            fixtures.add(
                FootballMatch(
                    matchday = matchdayCount++,
                    homeTeam = clubName,
                    awayTeam = opp
                )
            )
            fixtures.add(
                FootballMatch(
                    matchday = matchdayCount++,
                    homeTeam = opp,
                    awayTeam = clubName
                )
            )
        }

        val sponsorOffers = FootballDatabase.generateSponsorOffers(reputation, league.level)

        val newState = FootballClubState(
            isInitialized = true,
            clubId = java.util.UUID.randomUUID().toString(),
            clubName = clubName,
            customChairmanName = chairmanName.ifBlank { "Chairman" },
            country = league.country,
            leagueName = league.name,
            leagueLevel = league.level,
            clubReputation = reputation,
            acquisitionPriceUsd = acquisitionPriceUsd,
            transferBudget = transferBudget,
            wageBudget = wageBudget,
            squad = fullSquad,
            hiredManager = null,
            managerInbox = emptyList(),
            availableSponsors = sponsorOffers,
            currentSeason = 2026,
            currentMatchday = 1,
            totalMatchdays = fixtures.size,
            fixtures = fixtures,
            standings = standings,
            monthlyTvRights = tvRights,
            stadiumCapacity = when (league.level) {
                1 -> if (league.country == "Indonesia") 32_000 else 55_000
                2 -> if (league.country == "Indonesia") 15_000 else 28_000
                3 -> if (league.country == "Indonesia") 8_000 else 14_000
                else -> 8_000
            },
            ticketPrice = when (league.level) {
                1 -> if (league.country == "Indonesia") 8 else 48
                2 -> if (league.country == "Indonesia") 5 else 25
                else -> 15
            }
        )

        saveClubState(newState)
        return newState
    }

    fun injectBudget(amount: Long, fromSource: String): Pair<Boolean, String> {
        val current = _clubState.value
        val updated = current.copy(transferBudget = current.transferBudget + amount)
        saveClubState(updated)
        return Pair(true, "Berhasil menyuntikkan dana sebesar USD ${String.format("%,d", amount)} dari $fromSource ke anggaran belanja klub.")
    }

    fun modifyManagerMorale(delta: Int) {
        val current = _clubState.value
        val mgr = current.hiredManager ?: return
        val newMorale = (mgr.morale + delta).coerceIn(10, 100)
        val updated = current.copy(hiredManager = mgr.copy(morale = newMorale))
        saveClubState(updated)
    }

    fun modifyTeamMorale(delta: Int) {
        val current = _clubState.value
        val newMorale = (current.teamMorale + delta).coerceIn(10, 100)
        saveClubState(current.copy(teamMorale = newMorale))
    }

    // ==============================================================
    // TUGAS 2: SISTEM PEREKRUTAN MANAGER (AI LOGIC)
    // ==============================================================
    fun hireManager(manager: FootballManager): Pair<Boolean, String> {
        val current = _clubState.value
        if (!current.isInitialized) return Pair(false, "Klub belum diinisialisasi.")

        // Algoritma AI Penolakan Manager berdasarkan ClubReputation dan TransferBudget
        if (current.clubReputation < manager.minClubReputationRequired) {
            return Pair(
                false,
                "${manager.name} menolak tawaran Anda! 'Reputasi klub ${current.clubName} saat ini (${current.clubReputation}/100) belum memenuhi standar saya (minimal ${manager.minClubReputationRequired}). Saya mencari proyek yang memiliki reputasi lebih tinggi.'"
            )
        }

        if (current.transferBudget < manager.minTransferBudgetRequired) {
            val formattedReq = String.format("%,d", manager.minTransferBudgetRequired)
            val formattedHave = String.format("%,d", current.transferBudget)
            return Pair(
                false,
                "${manager.name} menolak tawaran Anda! 'Anggaran belanja klub saat ini (€$formattedHave) tidak mencukupi untuk mendukung visi taktik saya (minimal €$formattedReq).'"
            )
        }

        // Cek anggaran gaji
        if (current.remainingMonthlyWageBudget < manager.monthlySalary) {
            return Pair(
                false,
                "Anggaran gaji bulanan tidak mencukupi untuk membayar gaji ${manager.name} (€${String.format("%,d", manager.monthlySalary)}/bulan)."
            )
        }

        // Sukses merekrut! Generate rekomendasi transfer awal sesuai kelemahan skuad & taktik
        val initialWishlist = generateManagerWishlist(current.squad, manager)

        val updatedState = current.copy(
            hiredManager = manager.copy(morale = 100),
            managerInbox = initialWishlist
        )
        saveClubState(updatedState)

        return Pair(
            true,
            "Resmi! ${manager.name} telah menandatangani kontrak sebagai Manajer Kepala ${current.clubName} dengan taktik andalan ${manager.favoriteTactic}!"
        )
    }

    // NEGOSIASI KONTRAK PELATIH (DYNAMIC & RANDOM ALGORITHM)
    fun evaluateManagerNegotiation(
        manager: FootballManager,
        offeredSalary: Long,
        offeredBonus: Long
    ): ManagerNegotiationOutcome {
        val current = _clubState.value
        val baseSalary = manager.monthlySalary
        val ratio = offeredSalary.toDouble() / baseSalary.coerceAtLeast(1L)
        val repDiff = current.clubReputation - manager.minClubReputationRequired
        val roll = Random.nextDouble(0.0, 1.0)

        // 1. Reputasi klub terlalu jauh di bawah standar
        if (repDiff < -10) {
            return ManagerNegotiationOutcome(
                status = NegotiationStatus.REJECTED,
                negotiatedSalary = 0L,
                negotiatedBonus = 0L,
                narrative = "Negosiasi gagal total. ${manager.name} merasa reputasi klub (${current.clubReputation}/100) masih jauh di bawah standar profesionalnya (${manager.minClubReputationRequired}+)."
            )
        }

        // 2. DISKON / ANIMO TINGGI (Surprise Discount ~25-35% chance, atau reputasi klub tinggi)
        // Algoritma tidak melulu menaikkan harga: pelatih bisa rela memotong gaji jika menyukai proyek!
        val discountChance = if (current.clubReputation >= 85) 0.40 else 0.28
        if (roll < discountChance) {
            val discountPercent = Random.nextInt(5, 16) // Diskon 5% - 15%
            val discountedSalary = (baseSalary * (1.0 - discountPercent / 100.0)).toLong()
            val finalSalary = minOf(offeredSalary, discountedSalary)
            return ManagerNegotiationOutcome(
                status = NegotiationStatus.DISCOUNT_SURPRISE,
                negotiatedSalary = finalSalary,
                negotiatedBonus = offeredBonus,
                discountOrHikePercent = -discountPercent,
                narrative = "Kabar Luar Biasa! ${manager.name} sangat terkesan dengan visi jangka panjang dan stabilitas finansial klub. Pelatih bersedia memotong tuntutan gaji sebesar $discountPercent% demi memimpin skuad ${current.clubName}!"
            )
        }

        // 3. TAWANGAN DITERIMA LANGSUNG (Fair Offer)
        if (ratio >= 0.95 && roll < 0.75) {
            return ManagerNegotiationOutcome(
                status = NegotiationStatus.ACCEPTED,
                negotiatedSalary = offeredSalary,
                negotiatedBonus = offeredBonus,
                narrative = "Kesepakatan tercapai! Agen dan ${manager.name} menilai tawaran kontrak sebesar €${String.format("%,d", offeredSalary)}/bulan sangat layak dan profesional."
            )
        }

        // 4. COUNTER-OFFER / TAWAR-MENAWAR (~25% chance atau tawaran agak rendah)
        if (ratio >= 0.75 || roll < 0.85) {
            // Pelatih meminta penyesuaian: bisa sedikit naik (+5-10%) atau minta bonus performa
            val hikePercent = Random.nextInt(4, 12)
            val counterSal = (baseSalary * (1.0 + hikePercent / 100.0)).toLong()
            val counterBon = if (offeredBonus == 0L) 500_000L else offeredBonus + 250_000L
            return ManagerNegotiationOutcome(
                status = NegotiationStatus.COUNTER_OFFER,
                negotiatedSalary = offeredSalary,
                negotiatedBonus = offeredBonus,
                counterSalary = counterSal,
                counterBonus = counterBon,
                discountOrHikePercent = hikePercent,
                narrative = "Agen ${manager.name} menyambut baik ketertarikan klub, namun mengajukan counter-offer: gaji €${String.format("%,d", counterSal)}/bln dengan bonus target trofi €${String.format("%,d", counterBon)}."
            )
        }

        // 5. PENOLAKAN KARENA LOWBALL
        return ManagerNegotiationOutcome(
            status = NegotiationStatus.REJECTED,
            negotiatedSalary = 0L,
            negotiatedBonus = 0L,
            narrative = "Negosiasi mandek. Agen menganggap tawaran €${String.format("%,d", offeredSalary)}/bulan tidak menghargai rekam jejak ${manager.name}. Negosiasi dihentikan."
        )
    }

    fun hireManagerWithNegotiation(
        manager: FootballManager,
        agreedSalary: Long,
        agreedBonus: Long
    ): Pair<Boolean, String> {
        val current = _clubState.value
        if (!current.isInitialized) return Pair(false, "Klub belum diinisialisasi.")

        if (current.remainingMonthlyWageBudget < agreedSalary) {
            return Pair(false, "Sisa anggaran gaji bulanan klub tidak mencukupi untuk gaji yang disepakati (€${String.format("%,d", agreedSalary)}/bln).")
        }

        if (current.transferBudget < agreedBonus) {
            return Pair(false, "Transfer budget tidak cukup untuk membayar bonus tanda tangan (€${String.format("%,d", agreedBonus)}).")
        }

        val initialWishlist = generateManagerWishlist(current.squad, manager)
        val updatedManager = manager.copy(
            monthlySalary = agreedSalary,
            morale = 100
        )

        val updatedState = current.copy(
            hiredManager = updatedManager,
            transferBudget = current.transferBudget - agreedBonus,
            managerInbox = initialWishlist
        )
        saveClubState(updatedState)

        return Pair(
            true,
            "Resmi! ${manager.name} menandatangani kontrak manajer dengan gaji €${String.format("%,d", agreedSalary)}/bulan dan taktik andalan ${manager.favoriteTactic}!"
        )
    }

    fun fireManager(): Pair<Boolean, String> {
        val current = _clubState.value
        val mgr = current.hiredManager ?: return Pair(false, "Tidak ada manager yang sedang menjabat.")

        val severancePay = mgr.monthlySalary * 3
        val newTransferBudget = (current.transferBudget - severancePay).coerceAtLeast(0L)

        val updatedState = current.copy(
            hiredManager = null,
            transferBudget = newTransferBudget,
            managerInbox = emptyList()
        )
        saveClubState(updatedState)

        return Pair(
            true,
            "Kontrak ${mgr.name} telah diakhiri. Kompensasi pesangon sebesar €${String.format("%,d", severancePay)} telah dibayarkan."
        )
    }

    // ==============================================================
    // TUGAS 3: ALGORITMA BURSA TRANSFER DUA ARAH (TWO-WAY TRANSFERS)
    // ==============================================================
    private fun generateManagerWishlist(squad: List<FootballPlayer>, manager: FootballManager): List<ManagerInboxItem> {
        // Temukan posisi dengan rating terendah di skuad
        val avgGk = squad.filter { it.position == "GK" }.map { it.rating }.average().takeIf { !it.isNaN() } ?: 60.0
        val avgDef = squad.filter { it.position == "DEF" }.map { it.rating }.average().takeIf { !it.isNaN() } ?: 60.0
        val avgMid = squad.filter { it.position == "MID" }.map { it.rating }.average().takeIf { !it.isNaN() } ?: 60.0
        val avgFwd = squad.filter { it.position == "FWD" }.map { it.rating }.average().takeIf { !it.isNaN() } ?: 60.0

        val weakestPos = listOf(
            Pair("GK", avgGk),
            Pair("DEF", avgDef),
            Pair("MID", avgMid),
            Pair("FWD", avgFwd)
        ).minByOrNull { it.second }?.first ?: "DEF"

        // Cari target potensial dari database pemain nyata yang belum berada di klub ini
        val candidates = FootballDatabase.realPlayers.filter {
            it.position == weakestPos && !it.currentClub.equals(_clubState.value.clubName, ignoreCase = true)
        }.shuffled().take(2)

        val wishlist = mutableListOf<ManagerInboxItem>()
        for (cand in candidates) {
            wishlist.add(
                ManagerInboxItem(
                    targetPlayer = cand,
                    reason = "Memperkuat lini $weakestPos yang menjadi kelemahan utama skuad saat ini.",
                    managerNote = "Menurut analisis saya, ${cand.name} sangat cocok untuk melengkapi skema ${manager.favoriteTactic} kita musim ini.",
                    timestampMonth = _clubState.value.currentMatchday
                )
            )
        }
        return wishlist
    }

    // Logika 1: Manager Request (Approve / Reject)
    fun approveManagerTransfer(inboxId: String): Pair<Boolean, String> {
        val current = _clubState.value
        val inboxItem = current.managerInbox.find { it.id == inboxId }
            ?: return Pair(false, "Permintaan transfer tidak ditemukan.")

        if (inboxItem.status != "PENDING") {
            return Pair(false, "Permintaan ini sudah diproses sebelumnya.")
        }

        val player = inboxItem.targetPlayer
        if (current.transferBudget < player.marketValue) {
            return Pair(false, "Dana transfer tidak cukup untuk membeli ${player.name} (€${String.format("%,d", player.marketValue)}).")
        }

        if (current.remainingMonthlyWageBudget < player.monthlyWage) {
            return Pair(false, "Alokasi anggaran gaji bulanan tidak mencukupi untuk gaji ${player.name}.")
        }

        // Beli pemain
        val newSquad = current.squad + player.copy(currentClub = current.clubName)
        val newTransferBudget = current.transferBudget - player.marketValue
        val updatedInbox = current.managerInbox.map {
            if (it.id == inboxId) it.copy(status = "APPROVED") else it
        }

        // Manager Morale naik karena permintaannya disetujui!
        val updatedManager = current.hiredManager?.let {
            it.copy(morale = (it.morale + 10).coerceAtMost(100))
        }

        val updatedState = current.copy(
            squad = newSquad,
            transferBudget = newTransferBudget,
            managerInbox = updatedInbox,
            hiredManager = updatedManager
        )
        saveClubState(updatedState)

        return Pair(
            true,
            "Transfer Berhasil! ${player.name} resmi bergabung dengan ${current.clubName}. Manajer ${current.hiredManager?.name} sangat senang dengan dukungan Chairman (Morale +10)!"
        )
    }

    fun rejectManagerTransfer(inboxId: String): Pair<Boolean, String> {
        val current = _clubState.value
        val inboxItem = current.managerInbox.find { it.id == inboxId }
            ?: return Pair(false, "Permintaan transfer tidak ditemukan.")

        val updatedInbox = current.managerInbox.map {
            if (it.id == inboxId) it.copy(status = "REJECTED") else it
        }

        // Morale sedikit turun (-4)
        val updatedManager = current.hiredManager?.let {
            it.copy(morale = (it.morale - 4).coerceAtLeast(0))
        }

        val updatedState = current.copy(
            managerInbox = updatedInbox,
            hiredManager = updatedManager
        )
        saveClubState(updatedState)

        return Pair(true, "Tawaran transfer untuk ${inboxItem.targetPlayer.name} telah ditolak.")
    }

    // Logika 2: Owner Dictation (Paksa Beli oleh Owner) & Negosiasi Transfer
    fun evaluatePlayerNegotiation(
        player: FootballPlayer,
        offeredFee: Long,
        offeredWage: Long,
        signingBonus: Long
    ): PlayerNegotiationOutcome {
        val current = _clubState.value
        val feeRatio = offeredFee.toDouble() / player.marketValue.coerceAtLeast(1L)
        val wageRatio = offeredWage.toDouble() / player.monthlyWage.coerceAtLeast(1L)
        val roll = Random.nextDouble(0.0, 1.0)

        // 1. DISKON SPESIAL / BUTUH DANA SEGAR (25% - 35% chance)
        // Klub penjual butuh likuiditas atau pemain ngotot pindah, harga tidak selalu naik!
        val discountChance = if (player.age >= 30 || current.clubReputation >= 85) 0.38 else 0.28
        if (roll < discountChance) {
            val discountPercent = Random.nextInt(5, 18) // Diskon 5% - 17%
            val discountedFee = (player.marketValue * (1.0 - discountPercent / 100.0)).toLong()
            val finalFee = minOf(offeredFee, discountedFee)
            return PlayerNegotiationOutcome(
                status = NegotiationStatus.DISCOUNT_SURPRISE,
                negotiatedFee = finalFee,
                negotiatedWage = offeredWage,
                negotiatedBonus = signingBonus,
                discountOrHikePercent = -discountPercent,
                narrative = "Kabar Spektakuler! Klub pemilik ${player.currentClub} membutuhkan dana segar darurat untuk kepatuhan FFP. Mereka menyetujui diskon transfer fee sebesar $discountPercent% (€${String.format("%,d", finalFee)})!"
            )
        }

        // 2. KESEPAKATAN TERCAPAI LANGSUNG (Fair Deal)
        if (feeRatio >= 0.95 && wageRatio >= 0.95 && roll < 0.72) {
            return PlayerNegotiationOutcome(
                status = NegotiationStatus.ACCEPTED,
                negotiatedFee = offeredFee,
                negotiatedWage = offeredWage,
                negotiatedBonus = signingBonus,
                narrative = "Kesepakatan tercapai! Klub penjual dan agen ${player.name} menyetujui paket transfer senilai €${String.format("%,d", offeredFee)} tanpa hambatan."
            )
        }

        // 3. COUNTER-OFFER / PERMINTAAN TAMBAHAN (25% chance)
        if (feeRatio >= 0.75 || roll < 0.85) {
            val feeHikePercent = Random.nextInt(4, 14)
            val wageHikePercent = Random.nextInt(2, 10)
            val counterFee = (player.marketValue * (1.0 + feeHikePercent / 100.0)).toLong()
            val counterWage = (player.monthlyWage * (1.0 + wageHikePercent / 100.0)).toLong()
            return PlayerNegotiationOutcome(
                status = NegotiationStatus.COUNTER_OFFER,
                negotiatedFee = offeredFee,
                negotiatedWage = offeredWage,
                negotiatedBonus = signingBonus,
                counterFee = counterFee,
                counterWage = counterWage,
                discountOrHikePercent = feeHikePercent,
                narrative = "Direktur ${player.currentClub} dan agen menuntut kenaikan: Fee disesuaikan menjadi €${String.format("%,d", counterFee)} dan gaji €${String.format("%,d", counterWage)}/bulan."
            )
        }

        // 4. DITOLAK (Lowball)
        return PlayerNegotiationOutcome(
            status = NegotiationStatus.REJECTED,
            negotiatedFee = 0L,
            negotiatedWage = 0L,
            negotiatedBonus = 0L,
            narrative = "Tawaran ditolak mentah-mentah oleh ${player.currentClub}. Mereka menganggap tawaran €${String.format("%,d", offeredFee)} terlalu murah untuk pemain sekelas ${player.name}."
        )
    }

    fun forceBuyPlayer(
        player: FootballPlayer,
        agreedFee: Long = player.marketValue,
        agreedWage: Long = player.monthlyWage,
        signingBonus: Long = 0L
    ): Pair<Boolean, String> {
        val current = _clubState.value
        if (current.squad.any { it.name.equals(player.name, ignoreCase = true) }) {
            return Pair(false, "${player.name} sudah menjadi pemain ${current.clubName}.")
        }

        val totalOutlay = agreedFee + signingBonus
        if (current.transferBudget < totalOutlay) {
            return Pair(false, "Transfer Budget tidak cukup (€${String.format("%,d", current.transferBudget)} < €${String.format("%,d", totalOutlay)}).")
        }

        if (current.remainingMonthlyWageBudget < agreedWage) {
            return Pair(false, "Anggaran gaji bulanan klub tidak mencukupi untuk gaji €${String.format("%,d", agreedWage)}/bulan.")
        }

        val manager = current.hiredManager
        var moraleDeduction = 0
        var managerResigned = false
        var warningText = ""

        if (manager != null) {
            // Cek kecocokan taktik: jika gaya taktik pemain tidak sesuai taktik favorit manager
            val isTacticalMismatch = !player.tacticalFit.contains(manager.tacticalStyle, ignoreCase = true) &&
                    player.tacticalFit != "All-Round"

            if (isTacticalMismatch) {
                moraleDeduction = 25
                val newMorale = manager.morale - moraleDeduction
                if (newMorale <= 20) {
                    managerResigned = true
                    warningText = "\n\n⚠️ KEPUTUSAN KRITIS: Manajer ${manager.name} merasa otoritas taktisnya dirampas oleh Chairman karena membeli pemain yang bertolak belakang dengan filosofi bermainnya. ${manager.name} MEMUTUSKAN MENGUNDURKAN DIRI (RESIGN)!"
                } else {
                    warningText = "\n\n⚠️ PERINGATAN: Manajer ${manager.name} tidak menyukai pembelian paksa ini karena tidak sesuai dengan sistem ${manager.favoriteTactic} miliknya. Morale Manajer anjlok (-$moraleDeduction) menjadi $newMorale%!"
                }
            } else {
                moraleDeduction = 5
                warningText = "\n(Manajer menerima keputusan Chairman dengan sedikit catatan, Morale -5)."
            }
        }

        val newPlayer = player.copy(
            currentClub = current.clubName,
            monthlyWage = agreedWage,
            marketValue = agreedFee
        )
        val newSquad = current.squad + newPlayer
        val newTransferBudget = current.transferBudget - totalOutlay
        val updatedManager = if (managerResigned) null else manager?.let {
            it.copy(morale = (it.morale - moraleDeduction).coerceAtLeast(0))
        }

        val updatedState = current.copy(
            squad = newSquad,
            transferBudget = newTransferBudget,
            hiredManager = updatedManager
        )
        saveClubState(updatedState)

        return Pair(
            true,
            "Transfer Selesai! Chairman telah merampungkan transfer ${player.name} dengan mahar €${String.format("%,d", agreedFee)} dan gaji €${String.format("%,d", agreedWage)}/bulan.$warningText"
        )
    }

    fun sellPlayer(playerId: String): Pair<Boolean, String> {
        val current = _clubState.value
        val player = current.squad.find { it.id == playerId }
            ?: return Pair(false, "Pemain tidak ditemukan dalam skuad.")

        if (current.squad.size <= 16) {
            return Pair(false, "Tidak dapat menjual pemain. Skuad harus memiliki minimal 16 pemain.")
        }

        val saleProceeds = (player.marketValue * 0.90).toLong()
        val newSquad = current.squad.filterNot { it.id == playerId }
        val newTransferBudget = current.transferBudget + saleProceeds

        val updatedState = current.copy(
            squad = newSquad,
            transferBudget = newTransferBudget
        )
        saveClubState(updatedState)

        return Pair(
            true,
            "${player.name} telah dilepas ke bursa transfer seharga €${String.format("%,d", saleProceeds)}. Dana transfer telah ditambahkan ke kas klub!"
        )
    }

    // ==============================================================
    // TUGAS 4: FINANCIAL ENGINE (SPONSORSHIP, BROADCASTING & MATCHDAY)
    // ==============================================================
    fun signSponsor(sponsor: FootballSponsor): Pair<Boolean, String> {
        val current = _clubState.value
        val updated = if (sponsor.category == "JERSEY") {
            current.copy(
                jerseySponsor = sponsor,
                transferBudget = current.transferBudget + (sponsor.annualPayout / 4) // Uang muka 25%
            )
        } else {
            current.copy(
                stadiumSponsor = sponsor,
                transferBudget = current.transferBudget + (sponsor.annualPayout / 4)
            )
        }
        saveClubState(updated)
        return Pair(true, "Kontrak sponsor ${sponsor.sponsorName} resmi ditandatangani! Bonus tanda tangan telah masuk ke kas klub.")
    }

    fun injectCapitalFromPersonal(amount: Long, playerPersonalCash: Long): Pair<Boolean, String> {
        if (amount <= 0 || playerPersonalCash < amount) {
            return Pair(false, "Saldo pribadi Anda tidak mencukupi.")
        }
        val current = _clubState.value
        val updated = current.copy(transferBudget = current.transferBudget + amount)
        saveClubState(updated)
        return Pair(true, "Berhasil menyuntikkan dana modal sebesar €${String.format("%,d", amount)} ke Transfer Budget klub.")
    }

    fun withdrawDividendsToPersonal(amount: Long): Pair<Boolean, String> {
        val current = _clubState.value
        if (amount <= 0 || current.transferBudget < amount) {
            return Pair(false, "Kas transfer klub tidak mencukupi untuk penarikan dividen.")
        }
        val updated = current.copy(transferBudget = current.transferBudget - amount)
        saveClubState(updated)
        return Pair(true, "Berhasil menarik dividen sebesar €${String.format("%,d", amount)} ke rekening pribadi.")
    }

    fun processMonthlyClubFinances(): Triple<Long, Long, Long> {
        // Return Triple(Revenue, Expenses, NetMargin)
        val current = _clubState.value
        if (!current.isInitialized) return Triple(0L, 0L, 0L)

        // 1. Revenue
        val tvRevenue = current.monthlyTvRights
        val jerseyRevenue = current.jerseySponsor?.monthlyPayout ?: 0L
        val stadiumRevenue = current.stadiumSponsor?.monthlyPayout ?: 0L
        val matchdayPerMatch = (current.stadiumCapacity * current.matchdayAttendanceRate * current.ticketPrice).toLong()
        val matchdayRevenue = matchdayPerMatch * 2 // Rata-rata 2 pertandingan kandang per bulan
        val totalRevenue = tvRevenue + jerseyRevenue + stadiumRevenue + matchdayRevenue

        // 2. Expenses
        val playerWages = current.squad.sumOf { it.monthlyWage }
        val managerWage = current.hiredManager?.monthlySalary ?: 0L
        val facilitiesMaintenance = (current.stadiumCapacity * 12L)
        val totalExpenses = playerWages + managerWage + facilitiesMaintenance

        val netMargin = totalRevenue - totalExpenses
        val newTransferBudget = (current.transferBudget + netMargin).coerceAtLeast(0L)

        val updated = current.copy(
            transferBudget = newTransferBudget,
            lastMonthRevenue = totalRevenue,
            lastMonthExpenses = totalExpenses
        )
        saveClubState(updated)
        return Triple(totalRevenue, totalExpenses, netMargin)
    }

    // ==============================================================
    // TUGAS 5: MATCH SIMULATION & LEAGUE PROGRESSION (AUTO-RESOLVE)
    // ==============================================================
    fun simulateNextMatch(): FootballMatch? {
        val current = _clubState.value
        if (!current.isInitialized) return null

        val matchIndex = current.fixtures.indexOfFirst { !it.isPlayed }
        if (matchIndex == -1) {
            // Musim telah selesai!
            return null
        }

        val fixture = current.fixtures[matchIndex]
        val isHome = fixture.homeTeam.equals(current.clubName, ignoreCase = true)
        val opponentName = if (isHome) fixture.awayTeam else fixture.homeTeam

        val userTeamOvr = current.starting11Ovr
        val opponentStanding = current.standings.find { it.clubName.equals(opponentName, ignoreCase = true) }
        val oppOvr = opponentStanding?.ovr ?: (userTeamOvr - 2)

        // ==============================================================
        // ALGORITMA DINAMIS MATCH SIMULATION & UPSET FACTOR (15% - 20%)
        // ==============================================================
        val ovrDiff = userTeamOvr - oppOvr
        val isUserFavorite = ovrDiff >= 3
        val isUserUnderdog = ovrDiff <= -3

        // Modifier taktikal dan moral
        val managerRatingMod = ((current.hiredManager?.rating ?: 70) - 80) * 0.04
        val moraleMod = (current.teamMorale - 50) * 0.03
        val homeAdvantage = if (isHome) 0.12 else -0.12

        // Peluang acak kejutan (Upset Engine) sebesar 18% (15% - 20%)
        val upsetRoll = Random.nextDouble(0.0, 1.0)
        val isUpset = (isUserFavorite || isUserUnderdog) && (upsetRoll < 0.18)

        val (userGoals, oppGoals, wasUpsetOccurred) = if (isUpset) {
            if (isUserFavorite) {
                // RAKSASA TERGELINCIR: Tim unggulan (User) kalah atau ditahan imbang oleh tim underdog rating rendah!
                val shockLoss = Random.nextDouble() < 0.70
                if (shockLoss) {
                    val uGoals = Random.nextInt(0, 2)
                    val oGoals = uGoals + Random.nextInt(1, 2)
                    Triple(uGoals, oGoals, true)
                } else {
                    val drawGoals = Random.nextInt(0, 2)
                    Triple(drawGoals, drawGoals, true)
                }
            } else {
                // SENSASI GIANT KILLER: Tim kita yang berstatus underdog berhasil menumbangkan raksasa!
                val shockWin = Random.nextDouble() < 0.70
                if (shockWin) {
                    val oGoals = Random.nextInt(0, 2)
                    val uGoals = oGoals + Random.nextInt(1, 2)
                    Triple(uGoals, oGoals, true)
                } else {
                    val drawGoals = Random.nextInt(0, 2)
                    Triple(drawGoals, drawGoals, true)
                }
            }
        } else {
            // PROBABILITAS NORMAL BERDASARKAN RATING
            val strengthDiff = (ovrDiff * 0.05) + managerRatingMod + moraleMod + homeAdvantage
            val winProbability = (0.50 + strengthDiff).coerceIn(0.20, 0.80)
            val roll = Random.nextDouble(0.0, 1.0)
            val goals = when {
                roll < winProbability * 0.60 -> {
                    // Menang telak / meyakinkan
                    Pair(Random.nextInt(2, 5), Random.nextInt(0, 2))
                }
                roll < winProbability -> {
                    // Menang tipis
                    Pair(Random.nextInt(1, 3), Random.nextInt(0, 1))
                }
                roll < winProbability + 0.22 -> {
                    // Seri
                    val drawScore = Random.nextInt(0, 3)
                    Pair(drawScore, drawScore)
                }
                else -> {
                    // Kalah
                    Pair(Random.nextInt(0, 2), Random.nextInt(1, 4))
                }
            }
            Triple(goals.first, goals.second, false)
        }

        val homeScore = if (isHome) userGoals else oppGoals
        val awayScore = if (isHome) oppGoals else userGoals

        // Text Commentary Generator Berdasarkan Kejadian Pertandingan
        val events = mutableListOf<String>()
        events.add("🏟️ Kick-off: Peluit ditiup di hadapan ribuan pendukung fanatik di stadion!")
        val goalScorers = current.squad.filter { it.position in listOf("FWD", "MID") }.shuffled()

        if (wasUpsetOccurred) {
            if (isUserFavorite) {
                events.add("⚠️ 24' Tekanan Tinggi: ${current.clubName} mengurung pertahanan lawan, namun kiper lawan tampil kesurupan menepis tembakan!")
                events.add("🟨 38' Taktik park the bus lawan membuat lini depan kita frustrasi. Wasit memberi kartu kuning untuk pelanggaran taktis.")
                events.add("45' Jeda Babak Pertama: Lawan bertahan disiplin tanpa cela. Skor masih ketat.")
                events.add("💥 68' SIKLUS KEJUTAN (UPSET)! Lewat skema serangan balik kilat, lawan mencuri gol mengejutkan ke sudut tiang jauh!")
                if (userGoals > 0) {
                    val minute = Random.nextInt(75, 84)
                    val scorer = goalScorers.firstOrNull()?.name ?: "Pemain Depan"
                    events.add("$minute' ⚽ GOL! $scorer mencetak gol balasan memanfaatkan kemelut di kotak penalti!")
                }
                events.add("90+4' PELUIT PANJANG! Kejutan dramatis terjadi! Tim unggulan dipaksa tunduk oleh determinasi tim underdog!")
            } else {
                events.add("🛡️ 18' Disiplin Taktis: ${current.clubName} bermain spartan meredam agresivitas tim bintang lawan!")
                events.add("🧤 35' Penyelamatan kelas dunia! Kiper kita menggagalkan peluang emas lawan dengan refleks gemilang.")
                events.add("45' Jeda Babak: Semangat pantang menyerah skuad kita menuai apresiasi standing ovation dari suporter!")
                if (userGoals > 0) {
                    val minute = Random.nextInt(60, 78)
                    val scorer = goalScorers.firstOrNull()?.name ?: "Penyerang Kita"
                    events.add("🔥 $minute' SENSASIONAL (GIANT KILLING)! $scorer menyelesaikan serangan balik cepat menjadi gol spektakuler!")
                }
                events.add("90+3' PELUIT AKHIR! Kemenangan bersejarah! David menumbangkan Goliath di atas lapangan hijau!")
            }
        } else {
            if (userGoals > 0) {
                val minute = Random.nextInt(12, 44)
                val scorer = goalScorers.firstOrNull()?.name ?: "Striker"
                events.add("$minute' ⚽ GOL! Kerja sama taktis apik diselesaikan dengan tendangan terarah oleh $scorer!")
            }
            events.add("45' Peluit babak pertama dibunyikan. Strategi pelatih dievaluasi di ruang ganti.")
            if (oppGoals > 0) {
                val minute = Random.nextInt(52, 78)
                events.add("$minute' ⚽ Lawan melancarkan serangan balik cepat dan berhasil membobol gawang kita!")
            }
            if (userGoals > 1) {
                val minute = Random.nextInt(80, 89)
                val scorer = goalScorers.getOrNull(1)?.name ?: "Gelandang"
                events.add("$minute' ⚽ GOL TAMBAHAN! $scorer menyundul bola masuk memanfaatkan umpan silang akurat!")
            }
            events.add("90+3' Peluit panjang ditiup wasit. Pertandingan berakhir: ${fixture.homeTeam} $homeScore - $awayScore ${fixture.awayTeam}.")
        }

        val matchSummary = when {
            wasUpsetOccurred && userGoals > oppGoals -> "🔥 SENSASIONAL! Kemenangan kejutan nan bersejarah atas tim unggulan!"
            wasUpsetOccurred && userGoals < oppGoals -> "⚠️ KEJUTAN (UPSET)! Tim harus menelan hasil mengejutkan akibat serangan balik lawan."
            userGoals > oppGoals -> "Kemenangan spektakuler untuk ${current.clubName}!"
            userGoals == oppGoals -> "Pertandingan berakhir imbang yang dramatis dan penuh tensi."
            else -> "${current.clubName} harus mengakui keunggulan lawan kali ini."
        }

        val updatedFixture = fixture.copy(
            homeScore = homeScore,
            awayScore = awayScore,
            isPlayed = true,
            matchEvents = events,
            matchSummary = matchSummary
        )

        val updatedFixtures = current.fixtures.toMutableList()
        updatedFixtures[matchIndex] = updatedFixture

        // Update Klasemen (Standings)
        val isUserWin = userGoals > oppGoals
        val isUserDraw = userGoals == oppGoals
        val isUserLoss = userGoals < oppGoals

        val updatedStandings = current.standings.map { standing ->
            if (standing.clubName.equals(current.clubName, ignoreCase = true)) {
                standing.copy(
                    played = standing.played + 1,
                    won = standing.won + if (isUserWin) 1 else 0,
                    drawn = standing.drawn + if (isUserDraw) 1 else 0,
                    lost = standing.lost + if (isUserLoss) 1 else 0,
                    goalsFor = standing.goalsFor + userGoals,
                    goalsAgainst = standing.goalsAgainst + oppGoals,
                    goalDifference = (standing.goalsFor + userGoals) - (standing.goalsAgainst + oppGoals),
                    points = standing.points + (if (isUserWin) 3 else if (isUserDraw) 1 else 0)
                )
            } else if (standing.clubName.equals(opponentName, ignoreCase = true)) {
                standing.copy(
                    played = standing.played + 1,
                    won = standing.won + if (isUserLoss) 1 else 0,
                    drawn = standing.drawn + if (isUserDraw) 1 else 0,
                    lost = standing.lost + if (isUserWin) 1 else 0,
                    goalsFor = standing.goalsFor + oppGoals,
                    goalsAgainst = standing.goalsAgainst + userGoals,
                    goalDifference = (standing.goalsFor + oppGoals) - (standing.goalsAgainst + userGoals),
                    points = standing.points + (if (isUserLoss) 3 else if (isUserDraw) 1 else 0)
                )
            } else {
                // Simulasi pertandingan tim AI lainnya di matchday yang sama
                val aiRoll = Random.nextInt(0, 3)
                val (aiGf, aiGa) = Pair(Random.nextInt(0, 3), Random.nextInt(0, 3))
                val aiWin = aiGf > aiGa
                val aiDraw = aiGf == aiGa
                standing.copy(
                    played = standing.played + 1,
                    won = standing.won + (if (aiWin) 1 else 0),
                    drawn = standing.drawn + (if (aiDraw) 1 else 0),
                    lost = standing.lost + (if (!aiWin && !aiDraw) 1 else 0),
                    goalsFor = standing.goalsFor + aiGf,
                    goalsAgainst = standing.goalsAgainst + aiGa,
                    goalDifference = (standing.goalsFor + aiGf) - (standing.goalsAgainst + aiGa),
                    points = standing.points + (if (aiWin) 3 else if (aiDraw) 1 else 0)
                )
            }
        }.sortedWith(compareByDescending<LeagueStanding> { it.points }.thenByDescending { it.goalDifference }.thenByDescending { it.goalsFor })

        // Update Morale
        val newMorale = when {
            isUserWin -> (current.teamMorale + 4).coerceAtMost(100)
            isUserDraw -> current.teamMorale
            else -> (current.teamMorale - 5).coerceAtLeast(10)
        }

        val updatedState = current.copy(
            fixtures = updatedFixtures,
            standings = updatedStandings,
            currentMatchday = current.currentMatchday + 1,
            teamMorale = newMorale,
            recentMatchResult = "${fixture.homeTeam} $homeScore - $awayScore ${fixture.awayTeam}"
        )
        saveClubState(updatedState)

        // Cek jika seluruh jadwal telah selesai untuk memicu evaluasi Promosi / Degradasi
        if (updatedFixtures.none { !it.isPlayed }) {
            evaluateSeasonPromotionRelegation()
        }

        return updatedFixture
    }

    // Sistem Promosi & Degradasi di Akhir Musim
    fun evaluateSeasonPromotionRelegation(): String {
        val current = _clubState.value
        val userClubRank = current.standings.indexOfFirst { it.isUserClub } + 1
        val league = FootballDatabase.leagues.find { it.name.equals(current.leagueName, ignoreCase = true) }
            ?: return "Akhir Musim tercapai."

        val totalTeams = current.standings.size
        val isPromoted = userClubRank <= league.promotionSlots && league.level > 1
        val isRelegated = userClubRank > (totalTeams - league.relegationSlots) && league.relegationSlots > 0

        var outcomeMsg = ""
        var newLeagueLevel = current.leagueLevel
        var newLeagueName = current.leagueName
        var newReputation = current.clubReputation
        var newJerseySponsor = current.jerseySponsor
        var newStadiumSponsor = current.stadiumSponsor

        if (isPromoted) {
            // PROMOSI KE KASTA LEBIH TINGGI!
            val targetLevel = current.leagueLevel - 1
            val targetLeague = FootballDatabase.leagues.find { it.country == current.country && it.level == targetLevel }
            if (targetLeague != null) {
                newLeagueLevel = targetLevel
                newLeagueName = targetLeague.name
            }
            newReputation = (newReputation + 12).coerceAtMost(99)
            outcomeMsg = "🏆 SELAMAT! ${current.clubName} FINIS DI PERINGKAT $userClubRank DAN RESMI PROMOSI KE $newLeagueName! Reputasi klub melonjak menjadi $newReputation!"
        } else if (isRelegated) {
            // DEGRADASI KE KASTA LEBIH RENDAH!
            val targetLevel = current.leagueLevel + 1
            val targetLeague = FootballDatabase.leagues.find { it.country == current.country && it.level == targetLevel }
            if (targetLeague != null) {
                newLeagueLevel = targetLevel
                newLeagueName = targetLeague.name
            }
            newReputation = (newReputation - 15).coerceAtLeast(30)
            // Sponsor potong nilai kontrak karena degradasi
            newJerseySponsor = newJerseySponsor?.copy(
                annualPayout = (newJerseySponsor.annualPayout * 0.55).toLong(),
                monthlyPayout = (newJerseySponsor.monthlyPayout * 0.55).toLong()
            )
            newStadiumSponsor = newStadiumSponsor?.copy(
                annualPayout = (newStadiumSponsor.annualPayout * 0.55).toLong(),
                monthlyPayout = (newStadiumSponsor.monthlyPayout * 0.55).toLong()
            )
            outcomeMsg = "💔 DUKA DEGRADASI: ${current.clubName} finis di peringkat $userClubRank dan terdegradasi ke $newLeagueName. Reputasi klub anjlok menjadi $newReputation dan nilai sponsor dipangkas 45%."
        } else {
            outcomeMsg = "Musim ${current.currentSeason} telah selesai. ${current.clubName} finis di posisi $userClubRank dan bertahan di $newLeagueName."
        }

        // Catat di sejarah musim
        val newHistory = current.seasonHistory + "Musim ${current.currentSeason} ($current.leagueName): Posisi $userClubRank dari $totalTeams tim."

        // Reset fixtures & standings untuk musim baru
        val newSeasonNumber = current.currentSeason + 1
        val newLeague = FootballDatabase.leagues.find { it.name == newLeagueName } ?: league
        val newClubs = newLeague.clubs
        val newStandings = newClubs.map { name ->
            val isUser = name.equals(current.clubName, ignoreCase = true)
            LeagueStanding(
                clubName = name,
                ovr = if (isUser) current.starting11Ovr else newReputation - Random.nextInt(-3, 4),
                isUserClub = isUser
            )
        }.sortedByDescending { it.ovr }

        val opponents = newClubs.filterNot { it.equals(current.clubName, ignoreCase = true) }
        val newFixtures = mutableListOf<FootballMatch>()
        var matchdayCount = 1
        for (opp in opponents) {
            newFixtures.add(FootballMatch(matchday = matchdayCount++, homeTeam = current.clubName, awayTeam = opp))
            newFixtures.add(FootballMatch(matchday = matchdayCount++, homeTeam = opp, awayTeam = current.clubName))
        }

        val updatedState = current.copy(
            leagueLevel = newLeagueLevel,
            leagueName = newLeagueName,
            clubReputation = newReputation,
            jerseySponsor = newJerseySponsor,
            stadiumSponsor = newStadiumSponsor,
            seasonHistory = newHistory,
            currentSeason = newSeasonNumber,
            currentMatchday = 1,
            totalMatchdays = newFixtures.size,
            fixtures = newFixtures,
            standings = newStandings,
            availableSponsors = FootballDatabase.generateSponsorOffers(newReputation, newLeagueLevel)
        )
        saveClubState(updatedState)

        return outcomeMsg
    }
}
