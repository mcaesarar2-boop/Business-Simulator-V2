package com.example.viewmodel

import com.example.data.*
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object ApartmentEngine {

    /**
     * Process 1 simulation tick for Apartment Property Management.
     * dtSeconds: time step in seconds (e.g. 0.5s or 1.0s).
     */
    fun processTick(
        data: ApartmentPropertyData,
        dtSeconds: Float = 0.5f
    ): ApartmentPropertyData {
        var currentCash = data.internalCash
        var satisfaction = data.tenantSatisfaction
        var totalRentCollected = data.totalRentCollectedLifetime
        var totalMaintSpent = data.totalMaintenanceSpentLifetime

        // 1. Advance Active Incidents
        val updatedIncidents = mutableListOf<ApartmentIncident>()
        var penaltyMoveOuts = 0
        var penaltySatisfactionDrop = 0.0

        for (incident in data.activeIncidents) {
            val newRemaining = incident.remainingSeconds - dtSeconds.toInt()
            if (newRemaining <= 0) {
                // Incident Expired / Ignored by player!
                penaltySatisfactionDrop += incident.satisfactionPenalty
                penaltyMoveOuts += incident.moveOutTenantCount
            } else {
                updatedIncidents.add(incident.copy(remainingSeconds = max(1, newRemaining)))
            }
        }

        satisfaction = (satisfaction - penaltySatisfactionDrop).coerceIn(1.0, 5.0)

        // 2. Incident Generation Check (RNG)
        val now = System.currentTimeMillis()
        val securityFacility = data.installedFacilities.find { it == ApartmentFacilityType.SECURITY_24H }
        val incidentCooldownMs = if (securityFacility != null) 35_000L else 20_000L

        var lastIncTime = data.lastIncidentTimestamp
        if (updatedIncidents.size < 2 && (now - lastIncTime) > incidentCooldownMs) {
            val roll = Random.nextInt(100)
            val threshold = if (securityFacility != null) 30 else 50
            if (roll < threshold) {
                val newIncident = ApartmentDataGenerator.generateRandomIncident(data.buildingFloors, data.totalUnits)
                updatedIncidents.add(newIncident)
                lastIncTime = now
            }
        }

        // 3. Compute Target Tenant Satisfaction
        // Base: 3.2 ⭐ + sum of installed facilities bonus (capped at 5.0)
        var maxCap = data.maxSatisfactionCap
        // Active unresolved incidents penalty
        val activeIncidentsPenalty = updatedIncidents.size * 0.25
        val targetSatisfaction = (maxCap - activeIncidentsPenalty).coerceIn(1.0, 5.0)

        // Smooth gradual adjustment towards target satisfaction
        val satisfactionLerpSpeed = 0.05f * dtSeconds
        satisfaction += (targetSatisfaction - satisfaction) * satisfactionLerpSpeed
        satisfaction = (satisfaction).coerceIn(1.0, 5.0)

        // 4. Update Unit Categories Occupancy based on Fair Price & Demand Curve
        val totalToleranceBoost = data.installedFacilities.sumOf { it.rentToleranceBoost }

        val updatedCategories = data.unitCategories.map { cat ->
            if (!cat.isUnlocked) {
                cat
            } else {
                // Fair Price Benchmark calculation
                val satisfactionFactor = satisfaction / cat.type.targetSatisfactionFor100Percent
                val fairPrice = (cat.type.baseFairRent * satisfactionFactor + totalToleranceBoost).toLong().coerceAtLeast(100L)

                // Demand Ratio: fair price vs current player price
                val priceRatio = fairPrice.toDouble() / cat.monthlyRentPrice.toDouble().coerceAtLeast(1.0)

                // Target Occupancy fraction (0.05 - 1.0)
                val targetOccupancyFraction = when {
                    priceRatio >= 1.2 -> 1.0 // Murah banget -> 100% full
                    priceRatio >= 1.0 -> (0.85 + (priceRatio - 1.0) * 0.75).coerceIn(0.85, 1.0)
                    priceRatio >= 0.75 -> (0.60 + (priceRatio - 0.75) * 1.0).coerceIn(0.50, 0.85)
                    priceRatio >= 0.50 -> (0.25 + (priceRatio - 0.50) * 1.0).coerceIn(0.20, 0.50)
                    else -> 0.10 // Kemahalan parah -> 10% sisa penghuni
                }.toFloat()

                val targetOccupied = ((targetOccupancyFraction * cat.totalUnits).toInt() - penaltyMoveOuts).coerceIn(0, cat.totalUnits)

                // Smooth occupancy movement
                val currentOcc = cat.occupiedUnits
                val occDelta = when {
                    currentOcc < targetOccupied -> min(targetOccupied - currentOcc, max(1, (cat.totalUnits * 0.05).toInt()))
                    currentOcc > targetOccupied -> -min(currentOcc - targetOccupied, max(1, (cat.totalUnits * 0.05).toInt()))
                    else -> 0
                }

                cat.copy(occupiedUnits = (currentOcc + occDelta).coerceIn(0, cat.totalUnits))
            }
        }

        // 5. Continuous Rent Inflow & Operating Expenses
        val grossMonthlyRent = updatedCategories.filter { it.isUnlocked }.sumOf { it.totalMonthlyRent }
        val monthlyFacilityUpkeep = data.installedFacilities.sumOf { it.monthlyUpkeep }
        val monthlyStaffBaseCost = data.buildingFloors * 2_500L
        val totalMonthlyExpenses = monthlyFacilityUpkeep + monthlyStaffBaseCost

        // Cash flow per tick (converted from monthly rate, 30 days / month)
        val rentInflowTick = (grossMonthlyRent / 30.0 * dtSeconds).toLong()
        val expenseTick = (totalMonthlyExpenses / 30.0 * dtSeconds).toLong()
        val netDelta = rentInflowTick - expenseTick

        currentCash += netDelta
        totalRentCollected += rentInflowTick

        return data.copy(
            internalCash = currentCash,
            unitCategories = updatedCategories,
            installedFacilities = data.installedFacilities,
            activeIncidents = updatedIncidents,
            tenantSatisfaction = (Math.round(satisfaction * 10.0) / 10.0),
            totalRentCollectedLifetime = totalRentCollected,
            totalMaintenanceSpentLifetime = totalMaintSpent,
            lastIncidentTimestamp = lastIncTime
        )
    }

    fun setRentPrice(
        data: ApartmentPropertyData,
        unitType: ApartmentUnitType,
        newPrice: Long
    ): ApartmentPropertyData {
        val clampedPrice = newPrice.coerceIn(100L, 50_000L)
        val updated = data.unitCategories.map {
            if (it.type == unitType) it.copy(monthlyRentPrice = clampedPrice) else it
        }
        return data.copy(unitCategories = updated)
    }

    fun unlockUnitCategory(
        data: ApartmentPropertyData,
        unitType: ApartmentUnitType
    ): ApartmentPropertyData {
        val cat = data.unitCategories.find { it.type == unitType } ?: return data
        if (cat.isUnlocked) return data
        val cost = cat.type.unlockCost
        if (data.internalCash < cost) return data

        val updated = data.unitCategories.map {
            if (it.type == unitType) it.copy(isUnlocked = true, occupiedUnits = max(1, it.totalUnits / 3)) else it
        }
        return data.copy(
            internalCash = data.internalCash - cost,
            unitCategories = updated
        )
    }

    fun expandBuildingFloors(
        data: ApartmentPropertyData
    ): ApartmentPropertyData {
        val currentFloors = data.buildingFloors
        if (currentFloors >= 20) return data
        val cost = getFloorExpansionCost(currentFloors)
        if (data.internalCash < cost) return data

        // Expanding floors adds units to unlocked categories
        val updatedCategories = data.unitCategories.map { cat ->
            when (cat.type) {
                ApartmentUnitType.STUDIO -> cat.copy(totalUnits = cat.totalUnits + 10)
                ApartmentUnitType.TWO_BEDROOM -> cat.copy(totalUnits = cat.totalUnits + 6)
                ApartmentUnitType.PENTHOUSE -> cat.copy(totalUnits = cat.totalUnits + 2)
                ApartmentUnitType.COMMERCIAL_RETAIL -> cat.copy(totalUnits = cat.totalUnits + 1)
            }
        }

        return data.copy(
            internalCash = data.internalCash - cost,
            buildingFloors = currentFloors + 2,
            unitCategories = updatedCategories
        )
    }

    fun installFacility(
        data: ApartmentPropertyData,
        facility: ApartmentFacilityType
    ): ApartmentPropertyData {
        if (data.installedFacilities.contains(facility)) return data
        val cost = facility.installCost
        if (data.internalCash < cost) return data

        return data.copy(
            internalCash = data.internalCash - cost,
            installedFacilities = data.installedFacilities + facility
        )
    }

    fun resolveIncident(
        data: ApartmentPropertyData,
        incidentId: String
    ): ApartmentPropertyData {
        val incident = data.activeIncidents.find { it.id == incidentId } ?: return data
        val cost = incident.repairCost
        if (data.internalCash < cost) return data

        val updatedIncidents = data.activeIncidents.filter { it.id != incidentId }
        val boostedSatisfaction = (data.tenantSatisfaction + 0.2).coerceAtMost(5.0)

        return data.copy(
            internalCash = data.internalCash - cost,
            activeIncidents = updatedIncidents,
            tenantSatisfaction = boostedSatisfaction,
            totalMaintenanceSpentLifetime = data.totalMaintenanceSpentLifetime + cost
        )
    }

    fun ignoreIncident(
        data: ApartmentPropertyData,
        incidentId: String
    ): ApartmentPropertyData {
        val incident = data.activeIncidents.find { it.id == incidentId } ?: return data
        val updatedIncidents = data.activeIncidents.filter { it.id != incidentId }
        val penalizedSatisfaction = (data.tenantSatisfaction - incident.satisfactionPenalty).coerceAtLeast(1.0)

        return data.copy(
            activeIncidents = updatedIncidents,
            tenantSatisfaction = penalizedSatisfaction
        )
    }

    fun getFloorExpansionCost(currentFloors: Int): Long {
        return when (currentFloors) {
            8 -> 80_000L
            10 -> 150_000L
            12 -> 280_000L
            14 -> 500_000L
            16 -> 900_000L
            18 -> 1_500_000L
            else -> 2_500_000L
        }
    }

    data class ApartmentValuationBreakdown(
        val internalCash: Long,
        val buildingValue: Long,
        val facilitiesValue: Long,
        val occupancyGoodwill: Long,
        val satisfactionMultiplier: Double,
        val totalValuation: Long
    )

    fun calculateLiquidationValuation(data: ApartmentPropertyData): ApartmentValuationBreakdown {
        val cash = data.internalCash

        // Nilai Gedung & Konstruksi Fisik Lantai (Lantai Dasar + Floor Expansions)
        val buildingVal = data.buildingFloors * 65_000L

        // Fasilitas Terpasang (Depresiasi 85% dari install cost)
        val facVal = data.installedFacilities.sumOf { (it.installCost * 0.85).toLong() }

        // Unit Terbuka & Tenant Occupancy Goodwill (Kapitalisasi 4 bulan sewa dari unit terisi)
        val occGoodwill = data.unitCategories.filter { it.isUnlocked }.sumOf { cat ->
            cat.occupiedUnits * cat.monthlyRentPrice * 4L
        }

        // Kepuasan Penghuni Multiplier (1.0 - 5.0 ⭐)
        val satMult = when {
            data.tenantSatisfaction >= 4.7 -> 1.30
            data.tenantSatisfaction >= 4.3 -> 1.15
            data.tenantSatisfaction >= 3.8 -> 1.00
            data.tenantSatisfaction >= 3.0 -> 0.85
            else -> 0.70
        }

        val coreAssets = buildingVal + facVal + occGoodwill
        val adjustedCore = (coreAssets * satMult).toLong()
        val total = max(50_000L, cash + adjustedCore)

        return ApartmentValuationBreakdown(
            internalCash = cash,
            buildingValue = buildingVal,
            facilitiesValue = facVal,
            occupancyGoodwill = occGoodwill,
            satisfactionMultiplier = satMult,
            totalValuation = total
        )
    }
}
