package com.example.domain.subsystems.sports

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.FootballClubManager
import com.example.data.FootballClubState
import com.example.data.FootballManager
import com.example.data.FootballMatch
import com.example.data.FootballPlayer
import com.example.data.FootballSponsor
import com.example.data.ManagerNegotiationOutcome
import com.example.data.PlayerNegotiationOutcome
import com.example.data.PlayerState

/**
 * Domain subsystem engine for Football / Sports Club.
 * Handles:
 * 1. Monthly club finances (TV rights, jersey sponsors, stadium sponsors, matchday ticketing vs. player/manager wages & stadium maintenance).
 * 2. Automatic monthly fixture progression and league standings.
 * 3. User actions: manager hiring/firing, player transfer negotiation/scouting, stadium sponsor deals, capital injection/dividends.
 */
class FootballClubEngine(
    private val clubManager: FootballClubManager
) : MonthlyTickSubscriber {

    override val subscriberId: String = "football_club"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        val clubState = clubManager.clubState.value
        if (!clubState.isInitialized) {
            return SubsystemTickResult(
                subsystemId = subscriberId,
                revenue = 0L,
                expenses = 0L,
                dividendToGlobal = 0L,
                logMessages = emptyList(),
                stateModifier = { it }
            )
        }

        // Process club monthly finances
        val (revenue, expenses, netMargin) = clubManager.processMonthlyClubFinances()

        // Auto-simulate current month's scheduled fixtures if any unplayed
        val matchResult = clubManager.simulateNextMatch()
        val matchLogs = if (matchResult != null) {
            listOf("Football Club: Matchday ${matchResult.matchday} vs ${matchResult.awayTeam} finished ${matchResult.homeScore}-${matchResult.awayScore}")
        } else {
            emptyList()
        }

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = revenue,
            expenses = expenses,
            dividendToGlobal = 0L, // Kept in club's transfer budget by default until player calls withdrawDividends
            logMessages = listOf(
                "Football: Revenue €$revenue, Expenses €$expenses, Net Margin €$netMargin"
            ) + matchLogs,
            stateModifier = { it }
        )
    }

    // --- User Actions / Reducers ---

    fun initializeClub(clubName: String, leagueName: String, chairmanName: String, acquisitionPriceUsd: Long): FootballClubState {
        return clubManager.initializeClub(clubName, leagueName, chairmanName, acquisitionPriceUsd)
    }

    fun hireManager(manager: FootballManager): Pair<Boolean, String> {
        return clubManager.hireManager(manager)
    }

    fun fireManager(): Pair<Boolean, String> {
        return clubManager.fireManager()
    }

    fun evaluateManagerNegotiation(manager: FootballManager, offeredSalary: Long, offeredBonus: Long): ManagerNegotiationOutcome {
        return clubManager.evaluateManagerNegotiation(manager, offeredSalary, offeredBonus)
    }

    fun hireManagerWithNegotiation(manager: FootballManager, agreedSalary: Long, agreedBonus: Long): Pair<Boolean, String> {
        return clubManager.hireManagerWithNegotiation(manager, agreedSalary, agreedBonus)
    }

    fun approveManagerTransfer(inboxId: String): Pair<Boolean, String> {
        return clubManager.approveManagerTransfer(inboxId)
    }

    fun rejectManagerTransfer(inboxId: String): Pair<Boolean, String> {
        return clubManager.rejectManagerTransfer(inboxId)
    }

    fun evaluatePlayerNegotiation(player: FootballPlayer, offeredFee: Long, offeredWage: Long, signingBonus: Long): PlayerNegotiationOutcome {
        return clubManager.evaluatePlayerNegotiation(player, offeredFee, offeredWage, signingBonus)
    }

    fun forceBuyPlayer(player: FootballPlayer, agreedFee: Long, agreedWage: Long, signingBonus: Long): Pair<Boolean, String> {
        return clubManager.forceBuyPlayer(player, agreedFee, agreedWage, signingBonus)
    }

    fun sellPlayer(playerId: String): Pair<Boolean, String> {
        return clubManager.sellPlayer(playerId)
    }

    fun signSponsor(sponsor: FootballSponsor): Pair<Boolean, String> {
        return clubManager.signSponsor(sponsor)
    }

    fun injectCapitalFromPersonal(amount: Long, currentCash: Long): Pair<Boolean, String> {
        return clubManager.injectCapitalFromPersonal(amount, currentCash)
    }

    fun withdrawDividendsToPersonal(amount: Long): Pair<Boolean, String> {
        return clubManager.withdrawDividendsToPersonal(amount)
    }

    fun simulateNextMatch(): FootballMatch? {
        return clubManager.simulateNextMatch()
    }
}
