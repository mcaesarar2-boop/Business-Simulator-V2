package com.example.data

data class FootballLeague(
    val country: String,
    val name: String,
    val level: Int,
    val clubs: List<String>,
    val promotionSlots: Int = 3,
    val relegationSlots: Int = 3
)

data class FootballPlayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val age: Int,
    val currentClub: String,
    val nationality: String,
    val position: String, // "GK", "DEF", "MID", "FWD"
    val rating: Int,      // OVR (1-99)
    val marketValue: Long, // in Euros (€)
    val monthlyWage: Long, // in Euros (€) per month
    val isRealPlayer: Boolean = false,
    val tacticalFit: String = "All-Round" // e.g. "Tiki-Taka", "Gegenpressing", "Counter Attack", "Direct Attack"
)

data class FootballManager(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val country: String,
    val previousClubOrNation: String,
    val fameDescription: String,
    val rating: Int,                  // 70 - 99
    val favoriteTactic: String,       // e.g. "Tiki-Taka (4-3-3)", "Gegenpressing (4-3-3)"
    val tacticalStyle: String,        // short code: "TIKI_TAKA", "GEGENPRESS", "COUNTER", "DIRECT", "PARK_BUS", "TOTAL_FOOTBALL"
    val monthlySalary: Long,          // in Euros (€)
    val minClubReputationRequired: Int,
    val minTransferBudgetRequired: Long,
    val morale: Int = 100             // 0 - 100
)

data class FootballSponsor(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sponsorName: String,
    val category: String, // "JERSEY" (Sponsor Baju) or "STADIUM" (Hak Nama Stadion)
    val industry: String, // "Airlines", "Automotive", "Fintech", "Telecom", "Beverages"
    val annualPayout: Long,
    val monthlyPayout: Long,
    val contractYears: Int = 2,
    val monthsRemaining: Int = 24,
    val bonusPerWin: Long = 0L
)

data class ManagerInboxItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val targetPlayer: FootballPlayer,
    val reason: String,
    val managerNote: String,
    val timestampMonth: Int,
    var status: String = "PENDING" // "PENDING", "APPROVED", "REJECTED"
)

data class FootballMatch(
    val matchday: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val isPlayed: Boolean = false,
    val matchEvents: List<String> = emptyList(),
    val matchSummary: String = ""
)

data class LeagueStanding(
    val clubName: String,
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val goalDifference: Int = 0,
    val points: Int = 0,
    val ovr: Int = 75,
    val isUserClub: Boolean = false
)

data class ManagerBudgetProposal(
    val requestedAmount: Long,
    val targetPosition: String,
    val reasonExplanation: String,
    val managerQuote: String,
    val estimatedSquadImpact: String
)

data class FootballClubState(
    val isInitialized: Boolean = false,
    val clubId: String = "",
    val clubName: String = "",
    val customChairmanName: String = "Chairman",
    val country: String = "England",
    val leagueName: String = "Premier League",
    val leagueLevel: Int = 1,
    val clubReputation: Int = 80,         // 1 - 100
    val acquisitionPriceUsd: Long = 0L,   // Acquisition price paid in USD
    val transferBudget: Long = 80_000_000L, // in Euros (€)
    val wageBudget: Long = 3_000_000L,      // in Euros (€) per month
    val squad: List<FootballPlayer> = emptyList(),
    val hiredManager: FootballManager? = null,
    val managerInbox: List<ManagerInboxItem> = emptyList(),
    val jerseySponsor: FootballSponsor? = null,
    val stadiumSponsor: FootballSponsor? = null,
    val availableSponsors: List<FootballSponsor> = emptyList(),
    val currentSeason: Int = 2026,
    val currentMatchday: Int = 1,
    val totalMatchdays: Int = 38,
    val fixtures: List<FootballMatch> = emptyList(),
    val standings: List<LeagueStanding> = emptyList(),
    val seasonHistory: List<String> = emptyList(),
    val teamMorale: Int = 85,             // 0 - 100
    val stadiumCapacity: Int = 50_000,
    val ticketPrice: Int = 45,            // Euros per ticket
    val matchdayAttendanceRate: Float = 0.90f,
    val monthlyTvRights: Long = 6_000_000L,
    val lastMonthRevenue: Long = 0L,
    val lastMonthExpenses: Long = 0L,
    val recentMatchResult: String? = null
) {
    val starting11Ovr: Int
        get() {
            if (squad.isEmpty()) return 70
            val sorted = squad.sortedByDescending { it.rating }
            val top11 = sorted.take(11)
            return if (top11.isNotEmpty()) top11.map { it.rating }.average().toInt() else 70
        }

    val totalMonthlyWages: Long
        get() = squad.sumOf { it.monthlyWage } + (hiredManager?.monthlySalary ?: 0L)

    val remainingMonthlyWageBudget: Long
        get() = wageBudget - totalMonthlyWages
}

enum class NegotiationStatus {
    ACCEPTED,
    DISCOUNT_SURPRISE,
    COUNTER_OFFER,
    REJECTED
}

data class ManagerNegotiationOutcome(
    val status: NegotiationStatus,
    val negotiatedSalary: Long,
    val negotiatedBonus: Long,
    val narrative: String,
    val counterSalary: Long = 0L,
    val counterBonus: Long = 0L,
    val discountOrHikePercent: Int = 0
)

data class PlayerNegotiationOutcome(
    val status: NegotiationStatus,
    val negotiatedFee: Long,
    val negotiatedWage: Long,
    val negotiatedBonus: Long,
    val narrative: String,
    val counterFee: Long = 0L,
    val counterWage: Long = 0L,
    val discountOrHikePercent: Int = 0
)
