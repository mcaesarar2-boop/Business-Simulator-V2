package com.example.viewmodel

import com.example.data.*
import kotlin.random.Random

object CruiseEngine {

    /**
     * Calculates the monthly operations of a single Cruise Ship.
     * Returns the updated CruiseShip.
     */
    fun processShipMonthly(
        ship: CruiseShip,
        brandPrestige: Int,
        onPrestigeChange: (Int) -> Unit
    ): CruiseShip {
        // 1. Check if the ship is still in building phase
        if (ship.monthsUntilDelivery > 0) {
            val nextMonthsLeft = ship.monthsUntilDelivery - 1
            val progressPercent = ((ship.totalBuildTime - nextMonthsLeft).toDouble() / ship.totalBuildTime * 100).toInt().coerceIn(0, 100)
            return ship.copy(
                monthsUntilDelivery = nextMonthsLeft,
                constructionProgressPercent = progressPercent
            )
        }

        // 2. Check if the ship is in Drydock repair
        if (ship.isUnderDrydock) {
            val remain = ship.drydockMonthsRemaining - 1
            if (remain <= 0) {
                // Drydock is complete! Fully restore ship health
                return ship.copy(
                    isUnderDrydock = false,
                    drydockMonthsRemaining = 0,
                    hullHealth = 100.0,
                    engineHealth = 100.0,
                    monthsSinceDrydock = 0,
                    lastMonthPassengers = 0,
                    lastMonthTicketRevenue = 0L,
                    lastMonthOnboardRevenue = 0L,
                    lastMonthExpenses = (ship.pricePaid * 0.03).toLong(), // Drydock finishing wrap-up fee (3% of acquisition)
                    lastMonthAccidentOccurred = false,
                    lastMonthAccidentReport = "Drydock selesai! Kapal telah sepenuhnya direkondisi dan siap berlayar kembali."
                )
            } else {
                return ship.copy(
                    drydockMonthsRemaining = remain,
                    lastMonthPassengers = 0,
                    lastMonthTicketRevenue = 0L,
                    lastMonthOnboardRevenue = 0L,
                    lastMonthExpenses = (ship.pricePaid * 0.01).toLong(), // Drydock monthly ongoing fee (1% of buying cost)
                    lastMonthAccidentOccurred = false,
                    lastMonthAccidentReport = "Kapal sedang dalam pemeliharaan berkala Drydock (${remain} bulan tersisa). Seluruh kru fokus memoles lambung dan mesin kapal."
                )
            }
        }

        // 3. Normal Operating Mode
        val port = CRUISE_PORTS_CATALOG.find { it.id == ship.assignedPortId }
        if (port == null) {
            // Standby in drydock/harbor without routes
            return ship.copy(
                monthsSinceDrydock = ship.monthsSinceDrydock + 1,
                lastMonthPassengers = 0,
                lastMonthTicketRevenue = 0L,
                lastMonthOnboardRevenue = 0L,
                lastMonthExpenses = calculateBaseMaintenance(ship),
                lastMonthAccidentOccurred = false,
                lastMonthAccidentReport = "Kapal dalam kondisi Standby. Harap segera daftarkan rute pelabuhan berlayar agar menghasilkan omset."
            )
        }

        // --- CHECK BOAT ACCIDENT RISK ---
        val hullCondition = ship.hullHealth
        val engineCondition = ship.engineHealth
        
        // Base wear and tear
        // Samsung smart tech has -15% maintenance mod, and we'll reduce wear slightly too
        val wearReduction = if (ship.shipyard == ShipyardId.SAMSUNG) 0.8 else 1.0
        val hullWear = (0.5 + Random.nextDouble() * 0.8) * wearReduction
        val engineWear = (0.7 + Random.nextDouble() * 1.0) * wearReduction

        val newHullHealth = (hullCondition - hullWear).coerceIn(0.0, 100.0)
        val newEngineHealth = (engineCondition - engineWear).coerceIn(0.0, 100.0)
        
        // Drydock alert after 60 months
        val nextMonthsSinceDrydock = ship.monthsSinceDrydock + 1
        
        // Accident trigger chance (drastically increases if health < 50% or ignoring drydock > 60 months)
        var accidentChance = 0.005 // 0.5% base chance of issues
        if (newHullHealth < 50.0) accidentChance += 0.15
        if (newEngineHealth < 50.0) accidentChance += 0.25
        if (nextMonthsSinceDrydock > 60) accidentChance += 0.10 // ignoring drydock
        
        val hitAccident = Random.nextDouble() < accidentChance
        if (hitAccident) {
            // Accident occured!
            val updatedPrestige = (brandPrestige - 8).coerceAtLeast(10)
            onPrestigeChange(updatedPrestige)
            
            val repairExpenses = (ship.pricePaid * 0.04).toLong().coerceAtLeast(50_000L) // 4% of buy price
            return ship.copy(
                hullHealth = (newHullHealth - 5.0).coerceAtLeast(10.0),
                engineHealth = (newEngineHealth - 15.0).coerceAtLeast(5.0),
                monthsSinceDrydock = nextMonthsSinceDrydock,
                lastMonthPassengers = 0,
                lastMonthTicketRevenue = 0L,
                lastMonthOnboardRevenue = 0L,
                lastMonthExpenses = calculateBaseMaintenance(ship) + repairExpenses + port.portFee,
                lastMonthAccidentOccurred = true,
                lastMonthAccidentReport = "KECELAKAAN LAUT! Terjadi kegagalan operasional/mesin mati di tengah pelayaran menuju ${port.name}. Penumpang dievakuasi, pelayaran batal. Kerugian reparasi darurat sebesar $${repairExpenses} ditanggung kas divisi, prestise brand turun."
            )
        }

        // --- DEMAND / PASSENGER COMPUTATION ---
        // Ideal base ticket price for calculations is $250 based on average suite values
        val baseIdealPrice = 250.0
        val avgTicketPrice = (ship.ticketPriceRegular * 0.70 + ship.ticketPriceVip * 0.20 + ship.ticketPriceVvip * 0.08 + ship.ticketPriceGrandSuite * 0.02)
        val priceRatio = avgTicketPrice / baseIdealPrice
        // Price elasticity: demand decays if price ratio is high
        val priceFactor = when {
            priceRatio <= 0.5 -> 1.4
            priceRatio <= 0.8 -> 1.2
            priceRatio <= 1.0 -> 1.0
            priceRatio <= 1.5 -> 0.75 - (priceRatio - 1.0) * 0.5
            priceRatio <= 2.5 -> 0.40 - (priceRatio - 1.5) * 0.15
            else -> 0.05
        }

        // Facilities modifiers
        val activeFacilities = CRUISE_FACILITIES_CATALOG.filter { ship.builtFacilities.contains(it.id) }
        val sumFacilityDemandBuff = activeFacilities.sumOf { it.buffDemand }
        
        // Shipyard prestige/demand modifier
        val yardBonusMultiplier = when (ship.shipyard) {
            ShipyardId.MEYER_WERFT -> 1.10
            ShipyardId.FINCANTIERI -> 1.25 // VIPs/Sultans +25%
            ShipyardId.CHANTIERS -> 1.15
            else -> 1.0
        }

        val brandFactor = 0.5 + (brandPrestige / 100.0) // 0.5x to 1.5x based on brand prestige
        val computedDemand = (port.baseDemand * priceFactor * (1.0 + sumFacilityDemandBuff) * brandFactor * yardBonusMultiplier).toInt()
        val passengersCount = computedDemand.coerceIn(0, ship.maxPax)

        // --- PASSENGER SPLIT ---
        val passengersRegular = (passengersCount * 0.70).toInt()
        val passengersVip = (passengersCount * 0.20).toInt()
        val passengersVvip = (passengersCount * 0.08).toInt()
        val passengersGrandSuite = (passengersCount - passengersRegular - passengersVip - passengersVvip).coerceAtLeast(0)

        // --- REVENUE COMPUTATION ---
        val ticketRevenue = (passengersRegular * ship.ticketPriceRegular) +
                            (passengersVip * ship.ticketPriceVip) +
                            (passengersVvip * ship.ticketPriceVvip) +
                            (passengersGrandSuite * ship.ticketPriceGrandSuite)
        
        // Onboard Spend per passenger
        val baseOnboardSpend = 25L // standard $25 spend
        val sumOnboardSpendBuff = activeFacilities.sumOf { it.buffRevenue }
        val totalOnboardSpendPerPassenger = baseOnboardSpend + sumOnboardSpendBuff
        val onboardRevenue = passengersCount * totalOnboardSpendPerPassenger

        // --- MONTHLY EXPENSE COMPUTATION ---
        // Base Maintenance reduced if Samsung shipyard is used
        val baseMaint = calculateBaseMaintenance(ship)
        val shipyardMaintReduction = if (ship.shipyard == ShipyardId.SAMSUNG) 0.85 else 1.0
        val facilityMaintExpense = (activeFacilities.sumOf { it.maintenance } * shipyardMaintReduction).toLong()
        
        val fuelCrewCost = passengersCount * 30L // fuel, food, logistics for each boarded guest
        val totalMonthlyExpenses = baseMaint + facilityMaintExpense + port.portFee + fuelCrewCost

        // Let brand prestige slightly grow on solid journeys with over 70% occupancy
        val occupancyRatio = passengersCount.toDouble() / ship.maxPax
        if (occupancyRatio >= 0.70 && brandPrestige < 100) {
            onPrestigeChange((brandPrestige + 1).coerceAtMost(100))
        }

        return ship.copy(
            hullHealth = newHullHealth,
            engineHealth = newEngineHealth,
            monthsSinceDrydock = nextMonthsSinceDrydock,
            lastMonthPassengers = passengersCount,
            lastMonthPassengersRegular = passengersRegular,
            lastMonthPassengersVip = passengersVip,
            lastMonthPassengersVvip = passengersVvip,
            lastMonthPassengersGrandSuite = passengersGrandSuite,
            lastMonthTicketRevenue = ticketRevenue,
            lastMonthOnboardRevenue = onboardRevenue,
            lastMonthExpenses = totalMonthlyExpenses,
            lastMonthAccidentOccurred = false,
            lastMonthAccidentReport = "Pelayaran sukses berlayar ke ${port.name}. Okupansi mencapai ${(occupancyRatio * 100).toInt()}% dengan total ${passengersCount} tamu (Reg: $passengersRegular, VIP: $passengersVip, VVIP: $passengersVvip, Suite: $passengersGrandSuite)."
        )
    }

    /**
     * Compute base operational maintenance of ship physical class
     */
    fun calculateBaseMaintenance(ship: CruiseShip): Long {
        val baseVal = when (ship.shipClass) {
            CruiseShipClass.YACHT -> 40_000L
            CruiseShipClass.SMALL -> 60_000L
            CruiseShipClass.MIDSIZE -> 100_000L
            CruiseShipClass.LARGE -> 250_000L
            CruiseShipClass.MEGA_SHIP -> 500_000L
            CruiseShipClass.TITAN -> 1_000_000L
        }
        val factor = if (ship.shipyard == ShipyardId.SAMSUNG) 0.85 else 1.0
        return (baseVal * factor).toLong()
    }
}
