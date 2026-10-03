package com.example.domain.subsystems.hospitality

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.HotelFacility
import com.example.data.HotelProperty
import com.example.data.OwnedBusiness
import com.example.data.OwnedProperty
import com.example.data.PlayerState
import com.example.data.PropertyItem
import com.example.data.RoomClassConfig
import com.example.data.RoomClassStrategy

/**
 * Domain subsystem engine for Hospitality (Hotels & Resorts) and Real Estate Properties.
 * Handles:
 * 1. Hotel seasonality, facility occupancy bonuses, and Mega Events (G20 Summit, Food Scandal).
 * 2. Room class allocation occupancy, custom pricing penalties, and prestige deficits.
 * 3. Staff salaries, room operational utilities, and facility maintenance expenses.
 * 4. Residential real estate rental yields and condition decay.
 * 5. User actions: property renovation, room re-allocation, hotel facility expansion.
 */
class HospitalitySubsystemEngine : MonthlyTickSubscriber {

    override val subscriberId: String = "hospitality_holding"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        var totalHospitalityProfit = 0L

        val policyRepo = com.example.corporate.repository.CorporatePolicyRepository.getInstance()
        val distributionResults = mutableListOf<com.example.corporate.model.CashDistributionResult>()

        val updatedOwned = currentState.ownedBusinesses.map { biz ->
            if (biz.catalogId == "hospitality_holding") {
                val (updatedHotels, profit) = processHotelsMonthly(biz.hospitalityProperties, event.currentMonth)
                totalHospitalityProfit += profit
                val policy = policyRepo.getStandalonePolicy(biz.instanceId)
                val (updatedBiz, result) = com.example.corporate.engine.CashFlowDistributionEngine.processStandaloneBusinessCashFlow(
                    business = biz.copy(hospitalityProperties = updatedHotels),
                    netProfit = profit,
                    policy = policy
                )
                distributionResults.add(result)
                updatedBiz
            } else biz
        }

        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            var changed = false
            var currentTreasuryCash = holding.holdingCash
            val holdingPolicy = policyRepo.getHoldingPolicy(holding.instanceId)
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.catalogId == "hospitality_holding") {
                    changed = true
                    val (updatedHotels, profit) = processHotelsMonthly(sub.hospitalityProperties, event.currentMonth)
                    totalHospitalityProfit += profit
                    val (updatedSub, treasuryDelta, result) = com.example.corporate.engine.CashFlowDistributionEngine.processMergedSubsidiaryCashFlow(
                        subsidiary = sub.copy(hospitalityProperties = updatedHotels),
                        parentHolding = holding.copy(holdingCash = currentTreasuryCash),
                        netProfit = profit,
                        policy = holdingPolicy
                    )
                    currentTreasuryCash = (currentTreasuryCash + treasuryDelta).coerceAtLeast(0.0)
                    distributionResults.add(result)
                    updatedSub
                } else sub
            }
            if (changed) holding.copy(subsidiaries = newSubs, holdingCash = currentTreasuryCash) else holding
        }

        if (distributionResults.isNotEmpty()) {
            policyRepo.recordDistributionResults(distributionResults)
        }

        // Process personal owned real estate rental income and condition wear
        var totalRentalIncome = 0L
        val updatedProperties = currentState.ownedProperties.map { prop ->
            val conditionDecay = (1..3).random()
            val newCondition = (prop.condition - conditionDecay).coerceAtLeast(20)
            val rentYield = (prop.currentEstimatedValue * 0.005 * (newCondition / 100.0)).toLong()
            totalRentalIncome += rentYield
            prop.copy(condition = newCondition)
        }

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = if (totalHospitalityProfit > 0) totalHospitalityProfit + totalRentalIncome else totalRentalIncome,
            expenses = if (totalHospitalityProfit < 0) -totalHospitalityProfit else 0L,
            dividendToGlobal = totalRentalIncome,
            logMessages = listOf("Hospitality: Hotel Net $totalHospitalityProfit, Rental Yield: $totalRentalIncome"),
            stateModifier = { state ->
                state.copy(
                    ownedBusinesses = updatedOwned,
                    holdingCompanies = updatedHoldings,
                    ownedProperties = updatedProperties,
                    privateBalance = state.privateBalance + totalRentalIncome
                )
            }
        )
    }

    private fun processHotelsMonthly(
        properties: List<HotelProperty>,
        currentMonth: Int
    ): Pair<List<HotelProperty>, Long> {
        var totalHospitalityProfit = 0L
        val updatedProperties = properties.map { hotel ->
            var h = hotel
            if (h.isConstructing) {
                if (h.remainingBuildMonths > 0) {
                    val newLeft = h.remainingBuildMonths - 1
                    h = h.copy(
                        remainingBuildMonths = newLeft,
                        isConstructing = newLeft > 0
                    )
                }
                return@map h
            }

            // 1. Seasonality
            val seasonBonus = when (currentMonth) {
                12, 1, 7, 8 -> 0.3
                2, 9 -> -0.2
                else -> 0.0
            }

            val hotelPrestige = h.tier.baseBuildCost / 1_000_000 + h.builtFacilities.size * 10

            // 2. Mega Events RNG
            val rng = (1..100).random()
            if (h.builtFacilities.contains(HotelFacility.CONVENTION_CENTER) && rng > 95) {
                h = h.copy(activeMegaEvent = "Tuan Rumah KTT G20!")
            } else if (rng < 3) {
                h = h.copy(activeMegaEvent = "Skandal Keracunan Makanan")
            } else {
                h = h.copy(activeMegaEvent = null)
            }

            var totalHotelRevenue = 0L
            var facilityBonus = h.builtFacilities.sumOf { it.bonusOccupancy }
            if (h.location.startsWith("Integrasi:")) {
                facilityBonus += 0.15
            }

            if (h.roomConfigs == null) {
                h.roomConfigs = mutableMapOf()
            }
            if (h.roomConfigs!!.isEmpty()) {
                val sc = RoomClassConfig(isEnabled = true, allocationPercent = 100.0, customPrice = h.tier.baseRoomRate)
                h.roomConfigs!!["STANDARD"] = sc
            }

            var sumOccupancy = 0.0
            var sumAllocation = 0.0

            for ((classKey, config) in h.roomConfigs!!) {
                if (!config.isEnabled || config.allocationPercent <= 0.0) continue
                sumAllocation += config.allocationPercent

                val roomClassEnum = try { RoomClassStrategy.valueOf(classKey) } catch (e: Exception) { continue }
                val allocatedRooms = (h.tier.maxRooms * (config.allocationPercent / 100.0)).toInt()

                val baseRate = (h.tier.baseRoomRate * roomClassEnum.priceMultiplier).toLong()
                val pricePenalty = ((config.customPrice - baseRate).toDouble() / baseRate) * 0.5
                val prestigeDeficit = maxOf(0, roomClassEnum.requiredPrestige - hotelPrestige.toInt())
                val prestigePenalty = (prestigeDeficit * 0.02)

                var finalOccupancy = 0.6 + seasonBonus + facilityBonus - pricePenalty - prestigePenalty

                if (h.activeMegaEvent == "Tuan Rumah KTT G20!") {
                    finalOccupancy = 1.0
                } else if (h.activeMegaEvent == "Skandal Keracunan Makanan") {
                    finalOccupancy *= 0.3
                }

                finalOccupancy = finalOccupancy.coerceIn(0.05, 1.0)
                config.lastMonthOccupancy = finalOccupancy

                val occupiedRooms = (allocatedRooms * finalOccupancy).toInt()
                val avgGuestsPerRoom = when (roomClassEnum) {
                    RoomClassStrategy.STANDARD, RoomClassStrategy.SUPERIOR -> 1.5
                    RoomClassStrategy.DELUXE, RoomClassStrategy.JUNIOR_SUITE -> 2.0
                    RoomClassStrategy.SUITE, RoomClassStrategy.PRESIDENTIAL -> 3.0
                }
                val totalGuestsForThisClass = (occupiedRooms * avgGuestsPerRoom * 30).toLong()

                val roomRevenue = (occupiedRooms * 30L) * config.customPrice
                val spendMultiplier = roomClassEnum.priceMultiplier
                val facilityRevenuePerGuest = h.builtFacilities.sumOf { it.bonusRevenue } / 1000L
                val facilityRevenueForThisClass = totalGuestsForThisClass * facilityRevenuePerGuest * spendMultiplier.toLong()

                config.lastMonthRevenue = roomRevenue + facilityRevenueForThisClass
                totalHotelRevenue += config.lastMonthRevenue

                sumOccupancy += finalOccupancy * (config.allocationPercent / 100.0)
            }

            if (sumAllocation > 0) {
                h = h.copy(lastMonthOccupancyRate = sumOccupancy / (sumAllocation / 100.0))
            } else {
                h = h.copy(lastMonthOccupancyRate = 0.0)
            }

            // Expenses calculation
            var totalStaff = 10
            var roomOperationalExpense = 0L

            for ((classKey, config) in h.roomConfigs!!) {
                if (!config.isEnabled || config.allocationPercent <= 0.0) continue

                val roomClassEnum = try { RoomClassStrategy.valueOf(classKey) } catch (e: Exception) { continue }
                val allocatedRooms = (h.tier.maxRooms * (config.allocationPercent / 100.0)).toInt()

                val staffRatio = when (roomClassEnum) {
                    RoomClassStrategy.STANDARD, RoomClassStrategy.SUPERIOR -> 10
                    RoomClassStrategy.DELUXE, RoomClassStrategy.JUNIOR_SUITE -> 5
                    RoomClassStrategy.SUITE, RoomClassStrategy.PRESIDENTIAL -> 2
                }
                totalStaff += maxOf(1, allocatedRooms / staffRatio)

                val utilityCostPerRoom = (roomClassEnum.priceMultiplier * 15L).toLong()
                roomOperationalExpense += (allocatedRooms * utilityCostPerRoom * 30L)
            }

            val avgSalary = 2000L + (h.tier.baseRoomRate * 5)
            val staffExpense = totalStaff * avgSalary

            var facilityMaintenanceExpense = 0L
            for (facility in h.builtFacilities) {
                facilityMaintenanceExpense += facility.maintenanceCost
                totalStaff += 5
            }

            val propertyTotalExpense = staffExpense + roomOperationalExpense + facilityMaintenanceExpense
            var revenue = totalHotelRevenue

            if (h.activeMegaEvent == "Tuan Rumah KTT G20!") revenue += 50_000_000L

            val profit = revenue - propertyTotalExpense
            h = h.copy(lastMonthRevenue = revenue, lastMonthExpense = propertyTotalExpense)
            totalHospitalityProfit += profit

            h
        }
        return Pair(updatedProperties, totalHospitalityProfit)
    }

    // --- User Actions / Reducers ---

    fun calculateRenovationCost(property: PropertyItem, condition: Int): Long {
        val missingCondition = 100 - condition
        return ((property.basePrice * missingCondition) / 200.0).toLong()
    }

    fun renovateProperty(
        ownedProperty: OwnedProperty,
        propertyItem: PropertyItem,
        currentBalance: Long
    ): Pair<OwnedProperty, Long>? {
        if (ownedProperty.condition >= 100) return null
        val cost = calculateRenovationCost(propertyItem, ownedProperty.condition)
        if (currentBalance < cost) return null

        val newEstimatedValue = (propertyItem.basePrice * 1.5).toLong()
        val updated = ownedProperty.copy(condition = 100, currentEstimatedValue = newEstimatedValue)
        return Pair(updated, cost)
    }
}
