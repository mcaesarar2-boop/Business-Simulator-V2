package com.example.data

object CorporateFinanceManager {
    
    // Synergy Multiplier (+15% bonus)
    const val SYNERGY_MULTIPLIER = 1.15f

    fun calculateHoldingMargin(
        holding: HoldingCompany,
        playerState: PlayerState? = null
    ): Long {
        return holding.calculateHoldingMargin()
    }

    fun calculateHoldingValuation(
        holding: HoldingCompany,
        playerState: PlayerState? = null
    ): Long {
        return holding.calculateHoldingValuation()
    }

    fun calculateHoldingMonthlyRevenue(
        holding: HoldingCompany,
        playerState: PlayerState? = null
    ): Long {
        return holding.subsidiaries.sumOf { it.calculateGrossRevenue() }
    }

    fun calculateHoldingMonthlyMaintenance(
        holding: HoldingCompany,
        playerState: PlayerState? = null
    ): Long {
        return holding.subsidiaries.sumOf { it.calculateTotalExpenses() }
    }

    fun processIPO(
        holding: HoldingCompany,
        percentToSell: Float,
        playerState: PlayerState
    ): Pair<HoldingCompany, Long> {
        val currentValuation = calculateHoldingValuation(holding, playerState)
        val cashGained = (currentValuation * (percentToSell / 100f)).toLong()
        val updatedHolding = holding.copy(
            isPublic = true,
            ownershipPercentage = holding.ownershipPercentage - percentToSell
        )
        return Pair(updatedHolding, cashGained)
    }

    fun processDivestment(
        holding: HoldingCompany,
        playerState: PlayerState
    ): Long {
        val currentValuation = calculateHoldingValuation(holding, playerState)
        return (currentValuation * (holding.ownershipPercentage / 100f)).toLong()
    }
}
