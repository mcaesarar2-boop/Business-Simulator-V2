package com.example.domain.subsystems.aviation

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.AVIATION_AIRCRAFT_CATALOG
import com.example.data.AircraftInstance
import com.example.data.AviationHub
import com.example.data.DUMMY_AIRCRAFTS
import com.example.data.FlightRoute
import com.example.data.HubConstructionItem
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import java.util.UUID
import kotlin.math.min

/**
 * Domain subsystem engine for Aviation Group / Airline Holdings.
 * Handles:
 * 1. Fleet delivery countdowns and flight hours / wear degradation based on active routes.
 * 2. Aviation hub construction & facility expansion queues (e.g. VIP lounges, international terminals, cargo bays).
 * 3. Route passenger demand, load factors, ticket price revenue calculations, and aircraft capacity fulfillment.
 * 4. Operational expenses: aircraft maintenance, leasing obligations, hub maintenance, and flight crew salaries.
 * 5. User actions: buy/lease aircraft, build/upgrade aviation hubs, route management, fleet assignment.
 */
class AviationEngine : MonthlyTickSubscriber {

    override val subscriberId: String = "aviation_group"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        var totalAviationRevenue = 0L
        var totalAviationExpenses = 0L
        val policyRepo = com.example.corporate.repository.CorporatePolicyRepository.getInstance()
        val distributionResults = mutableListOf<com.example.corporate.model.CashDistributionResult>()

        val updatedOwned = currentState.ownedBusinesses.map { biz ->
            if (biz.catalogId == "aviation_group") {
                val (updatedBiz, rev, exp) = processAviationMonthly(biz)
                totalAviationRevenue += rev
                totalAviationExpenses += exp
                val netProfit = rev - exp
                val policy = policyRepo.getStandalonePolicy(biz.instanceId)
                val (finalBiz, result) = com.example.corporate.engine.CashFlowDistributionEngine.processStandaloneBusinessCashFlow(
                    business = updatedBiz,
                    netProfit = netProfit,
                    policy = policy
                )
                distributionResults.add(result)
                finalBiz
            } else biz
        }

        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            var changed = false
            var currentTreasuryCash = holding.holdingCash
            val holdingPolicy = policyRepo.getHoldingPolicy(holding.instanceId)
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.catalogId == "aviation_group") {
                    changed = true
                    val (updatedSub, rev, exp) = processAviationMonthly(sub)
                    totalAviationRevenue += rev
                    totalAviationExpenses += exp
                    val netProfit = rev - exp
                    val (finalSub, treasuryDelta, result) = com.example.corporate.engine.CashFlowDistributionEngine.processMergedSubsidiaryCashFlow(
                        subsidiary = updatedSub,
                        parentHolding = holding.copy(holdingCash = currentTreasuryCash),
                        netProfit = netProfit,
                        policy = holdingPolicy
                    )
                    currentTreasuryCash = (currentTreasuryCash + treasuryDelta).coerceAtLeast(0.0)
                    distributionResults.add(result)
                    finalSub
                } else sub
            }
            if (changed) holding.copy(subsidiaries = newSubs, holdingCash = currentTreasuryCash) else holding
        }

        if (distributionResults.isNotEmpty()) {
            policyRepo.recordDistributionResults(distributionResults)
        }

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = totalAviationRevenue,
            expenses = totalAviationExpenses,
            dividendToGlobal = 0L,
            logMessages = listOf(
                "Aviation: Total Flight Revenue $totalAviationRevenue, Total Operating Expenses $totalAviationExpenses"
            ),
            stateModifier = { state ->
                state.copy(
                    ownedBusinesses = updatedOwned,
                    holdingCompanies = updatedHoldings
                )
            }
        )
    }

    private fun processAviationMonthly(owned: OwnedBusiness): Triple<OwnedBusiness, Long, Long> {
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
            val nextQueue = mutableListOf<HubConstructionItem>()
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
                    val pDef = AVIATION_AIRCRAFT_CATALOG.find { it.id == plane.modelId }
                        ?: DUMMY_AIRCRAFTS.find { it.id == plane.modelId }
                    val maxCap = pDef?.maxPax ?: when (plane.modelId) {
                        "atr72" -> 72
                        "a320" -> 180
                        "b777" -> 350
                        else -> 150
                    }
                    (maxCap * (0.3 + 0.7 * (plane.condition / 100.0))).toInt()
                }
                if (totalCapacityOnRoute > 0) {
                    val dailyPax = min(route.baseDemand, totalCapacityOnRoute)
                    val monthlyPax = dailyPax * 30L
                    val routeRevenue = monthlyPax * route.ticketPrice
                    totalRev += routeRevenue
                }
            }
        }

        // Calculate expenses
        var totalExp = 0L
        updatedFleet.forEach { plane ->
            if (plane.status != "DELIVERING") {
                val pDef = AVIATION_AIRCRAFT_CATALOG.find { it.id == plane.modelId }
                    ?: DUMMY_AIRCRAFTS.find { it.id == plane.modelId }
                val baseUpkeep = if (pDef != null) {
                    pDef.price * 0.005
                } else {
                    when (plane.modelId) {
                        "atr72" -> 100000.0
                        "a320" -> 250000.0
                        "b777" -> 600000.0
                        else -> 150000.0
                    }
                }
                val finalUpkeep = baseUpkeep * (1.5 - plane.condition / 200.0)
                totalExp += finalUpkeep.toLong()
            }
            if (plane.isLeased) {
                totalExp += plane.leasePrice
            }
        }

        updatedHubs.forEach { hub ->
            var hubUpkeep = 100000L
            hub.activeUpgrades.forEach { upgId ->
                val addCost = when (upgId) {
                    "upg_dom" -> 50000L
                    "upg_intl_1" -> 150000L
                    "upg_vip" -> 100000L
                    "upg_intl_2" -> 300000L
                    "upg_cargo" -> 120000L
                    else -> 50000L
                }
                hubUpkeep += addCost
            }
            totalExp += hubUpkeep
        }

        val staffExpenses = updatedFleet.size * 12000L
        val grandTotalExpenses = totalExp + staffExpenses
        val netProfit = totalRev - grandTotalExpenses

        val updatedBusiness = owned.copy(
            airlineFleetComplex = updatedFleet,
            airlineHubsComplex = updatedHubs,
            customRevenue = totalRev
        )

        return Triple(updatedBusiness, totalRev, grandTotalExpenses)
    }

    // --- User Actions / Reducers ---

    fun buyAviationHub(
        business: OwnedBusiness,
        city: String,
        cost: Long,
        buildTime: Int = 0
    ): OwnedBusiness? {
        if (business.companyCash < cost) return null

        val newHub = AviationHub(
            city = city,
            baseCost = cost,
            activeUpgrades = emptyList(),
            constructionQueue = emptyList(),
            isConstructing = buildTime > 0,
            constructionMonthsLeft = buildTime
        )
        return business.copy(
            companyCash = business.companyCash - cost.toDouble(),
            airlineHubsComplex = business.airlineHubsComplex + newHub,
            airlineHubs = business.airlineHubs + city
        )
    }

    fun startHubUpgrade(
        business: OwnedBusiness,
        hubId: String,
        upgradeId: String,
        cost: Long,
        buildTime: Int
    ): OwnedBusiness? {
        if (business.companyCash < cost) return null

        val updatedHubs = business.airlineHubsComplex.map { hub ->
            if (hub.id == hubId) {
                val newQueueItem = HubConstructionItem(upgradeId, buildTime)
                hub.copy(constructionQueue = hub.constructionQueue + newQueueItem)
            } else hub
        }
        return business.copy(
            companyCash = business.companyCash - cost.toDouble(),
            airlineHubsComplex = updatedHubs
        )
    }

    fun buyAircraft(
        business: OwnedBusiness,
        modelId: String,
        cost: Long,
        deliveryTime: Int,
        isLeased: Boolean = false,
        leasePrice: Long = 0L,
        quantity: Int = 1
    ): OwnedBusiness? {
        val actualCost = if (isLeased) 0L else (cost * quantity)
        if (business.companyCash < actualCost) return null

        val newPlanes = (1..quantity).map {
            AircraftInstance(
                id = UUID.randomUUID().toString(),
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
        return business.copy(
            companyCash = business.companyCash - actualCost.toDouble(),
            airlineFleetComplex = business.airlineFleetComplex + newPlanes
        )
    }

    fun createFlightRoute(
        business: OwnedBusiness,
        originHubId: String,
        destination: String,
        distanceCategory: String,
        demand: Int,
        ticketPrice: Int
    ): OwnedBusiness {
        val newRoute = FlightRoute(
            originHubId = originHubId,
            destination = destination,
            distanceCategory = distanceCategory,
            baseDemand = demand,
            ticketPrice = ticketPrice,
            assignedAircraftIds = emptyList()
        )
        return business.copy(flightRoutes = business.flightRoutes + newRoute)
    }

    fun assignAircraftToRoute(
        business: OwnedBusiness,
        aircraftId: String,
        routeId: String?
    ): OwnedBusiness {
        val updatedFleet = business.airlineFleetComplex.map { plane ->
            if (plane.id == aircraftId) {
                plane.copy(
                    assignedRouteId = routeId,
                    status = if (routeId != null) "ASSIGNED" else "STANDBY"
                )
            } else plane
        }
        val updatedRoutes = business.flightRoutes.map { route ->
            val alreadyAssigned = route.assignedAircraftIds.contains(aircraftId)
            if (route.id == routeId) {
                if (!alreadyAssigned) route.copy(assignedAircraftIds = route.assignedAircraftIds + aircraftId)
                else route
            } else {
                if (alreadyAssigned) route.copy(assignedAircraftIds = route.assignedAircraftIds - aircraftId)
                else route
            }
        }
        return business.copy(airlineFleetComplex = updatedFleet, flightRoutes = updatedRoutes)
    }
}
