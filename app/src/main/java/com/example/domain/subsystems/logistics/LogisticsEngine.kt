package com.example.domain.subsystems.logistics

import com.example.core.engine.MonthTickEvent
import com.example.core.engine.MonthlyTickSubscriber
import com.example.core.engine.SubsystemTickResult
import com.example.data.FleetVehicle
import com.example.data.FleetVehicleStatus
import com.example.data.LogisticsCompanyData
import com.example.data.LogisticsContract
import com.example.data.LogisticsVehicleType
import com.example.data.OwnedBusiness
import com.example.data.PlayerState
import com.example.viewmodel.LogisticsEngine as LegacyLogisticsEngine

/**
 * Domain subsystem engine for Logistics & Distribution Hubs.
 * Handles:
 * 1. Monthly package flow, contract deliveries, inbound flow, and demurrage calculations.
 * 2. Fleet wear, automated drone/EV logistics dispatches, and route weather impacts.
 * 3. Monthly operating revenue vs. fuel/maintenance/fleet upkeep costs.
 * 4. User actions: contract signing, fleet acquisition/repairs, AI route dispatch upgrades.
 */
class LogisticsEngine : MonthlyTickSubscriber {

    override val subscriberId: String = "mid_logistics"

    override suspend fun onMonthlyTick(
        event: MonthTickEvent,
        currentState: PlayerState
    ): SubsystemTickResult {
        var totalLogisticsRevenue = 0L
        var totalLogisticsCosts = 0L

        val updatedOwned = currentState.ownedBusinesses.map { biz ->
            if (biz.catalogId == "mid_logistics") {
                val (updatedBiz, rev, costs) = processLogisticsMonthly(biz)
                totalLogisticsRevenue += rev
                totalLogisticsCosts += costs
                updatedBiz
            } else biz
        }

        val updatedHoldings = currentState.holdingCompanies.map { holding ->
            var changed = false
            val newSubs = holding.subsidiaries.map { sub ->
                if (sub.catalogId == "mid_logistics") {
                    changed = true
                    val (updatedSub, rev, costs) = processLogisticsMonthly(sub)
                    totalLogisticsRevenue += rev
                    totalLogisticsCosts += costs
                    updatedSub
                } else sub
            }
            if (changed) holding.copy(subsidiaries = newSubs) else holding
        }

        return SubsystemTickResult(
            subsystemId = subscriberId,
            revenue = totalLogisticsRevenue,
            expenses = totalLogisticsCosts,
            dividendToGlobal = 0L,
            logMessages = listOf(
                "Logistics: Revenue $totalLogisticsRevenue, Operating Costs $totalLogisticsCosts (Net: ${totalLogisticsRevenue - totalLogisticsCosts})"
            ),
            stateModifier = { state ->
                state.copy(
                    ownedBusinesses = updatedOwned,
                    holdingCompanies = updatedHoldings
                )
            }
        )
    }

    private fun processLogisticsMonthly(biz: OwnedBusiness): Triple<OwnedBusiness, Long, Long> {
        val initialCash = biz.logisticsData.internalCash

        // Run multi-step simulation iterations for the month
        var currentData = biz.logisticsData
        repeat(60) {
            currentData = LegacyLogisticsEngine.processTick(currentData, dtSeconds = 0.5f)
        }

        val cashDelta = currentData.internalCash - initialCash
        val revenue = if (cashDelta > 0) cashDelta else 0L
        val expenses = if (cashDelta < 0) -cashDelta else 0L

        val updatedBiz = biz.copy(
            logisticsData = currentData
        )
        return Triple(updatedBiz, revenue, expenses)
    }

    // --- User Actions / Reducers ---

    fun researchTech(business: OwnedBusiness, path: String): OwnedBusiness {
        val updatedData = LegacyLogisticsEngine.researchTech(business.logisticsData, path)
        return business.copy(logisticsData = updatedData)
    }

    fun toggleAutoDispatch(business: OwnedBusiness): OwnedBusiness {
        val updatedData = LegacyLogisticsEngine.toggleAutoDispatch(business.logisticsData)
        return business.copy(logisticsData = updatedData)
    }

    fun repairVehicle(business: OwnedBusiness, vehicleId: String): OwnedBusiness? {
        val vehicle = business.logisticsData.fleet.find { it.id == vehicleId } ?: return null
        val cost = LegacyLogisticsEngine.calculateRepairCost(vehicle)
        if (business.logisticsData.internalCash < cost) return null

        val updatedFleet = business.logisticsData.fleet.map {
            if (it.id == vehicleId) it.copy(conditionHp = 100.0, status = FleetVehicleStatus.IDLE)
            else it
        }
        val updatedData = business.logisticsData.copy(
            internalCash = business.logisticsData.internalCash - cost,
            fleet = updatedFleet
        )
        return business.copy(logisticsData = updatedData)
    }

    fun buyVehicle(business: OwnedBusiness, type: LogisticsVehicleType): OwnedBusiness? {
        if (business.logisticsData.internalCash < type.buyCost) return null

        val newVehicle = FleetVehicle(
            id = java.util.UUID.randomUUID().toString(),
            name = "${type.displayName} #${business.logisticsData.fleet.size + 1}",
            type = type,
            conditionHp = 100.0,
            status = FleetVehicleStatus.IDLE
        )
        val updatedData = business.logisticsData.copy(
            internalCash = business.logisticsData.internalCash - type.buyCost,
            fleet = business.logisticsData.fleet + newVehicle
        )
        return business.copy(logisticsData = updatedData)
    }
}
