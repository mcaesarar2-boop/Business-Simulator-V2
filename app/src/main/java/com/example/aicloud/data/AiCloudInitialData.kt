package com.example.aicloud.data

import com.example.aicloud.model.AiResearchTech
import com.example.aicloud.model.AiWorkloadType
import com.example.aicloud.model.ClientContract
import java.util.UUID

object AiCloudInitialData {

    val RESEARCH_CATALOG: List<AiResearchTech> = listOf(
        AiResearchTech(
            id = "tech_silicon_custom_tpu",
            name = "Proprietary ASIC Microarchitecture",
            category = "Hardware Silicon",
            description = "Unlocks Custom TPU v5p Pod Slices rack deployment with 35% higher energy efficiency.",
            costUsd = 15_000_000L,
            powerReductionPct = 0.15,
            revenueBoostPct = 0.10
        ),
        AiResearchTech(
            id = "tech_quantum_optics",
            name = "Co-Packaged Optical Interconnects",
            category = "Networking",
            description = "Sub-microsecond GPU cluster fabric switching. Boosts enterprise contract payout by +20%.",
            costUsd = 8_000_000L,
            revenueBoostPct = 0.20,
            reliabilityBoostPct = 0.05
        ),
        AiResearchTech(
            id = "tech_ai_pue_optimizer",
            name = "Autonomous Neural Cooling PUE Tuner",
            category = "Facility & AI",
            description = "Real-time AI telemetry adjusts chillers dynamically, lowering facility PUE by -0.06.",
            costUsd = 5_000_000L,
            pueImprovement = 0.06,
            powerReductionPct = 0.08
        ),
        AiResearchTech(
            id = "tech_immersion_cryo",
            name = "Dielectric Immersion Fluid Physics",
            category = "Thermal Engineering",
            description = "Eliminates thermal throttling completely, boosting hardware lifespan and uptime SLA.",
            costUsd = 12_000_000L,
            reliabilityBoostPct = 0.08,
            pueImprovement = 0.04
        ),
        AiResearchTech(
            id = "tech_nuclear_microgrid",
            name = "Direct SMR Nuclear Interfacing",
            category = "Energy Grid",
            description = "Zero-carbon baseload energy protocol allows gigawatt-scale data center expansion with -25% power cost.",
            costUsd = 30_000_000L,
            powerReductionPct = 0.25,
            revenueBoostPct = 0.15
        )
    )

    private val CLIENT_NAMES = listOf(
        "OpenFoundry AI", "Anthropic Global Systems", "DeepMind Horizons", "NEXUS Quantum Labs",
        "Apex Autonomous Mobility", "BioHelix Synthesis", "Aether Robotics", "Cognitive Stream API",
        "Orbital Aerospace Vision", "Hyperion Financial AI", "Kortex Medical Intelligence", "Synthetix VFX Engine"
    )

    fun generateRandomContract(existingIds: Set<String> = emptySet()): ClientContract {
        val workload = AiWorkloadType.values().random()
        val baseTflops = workload.minTflopsRequired * (0.8 + Math.random() * 1.5)
        val roundedTflops = Math.round(baseTflops / 100.0) * 100.0
        val duration = (3..12).random()
        val monthlyRate = (roundedTflops * workload.baseRatePerTflopsUsd * (0.9 + Math.random() * 0.3)).toLong()
        val penalty = (monthlyRate * workload.penaltyMultiplier * 0.4).toLong()

        val name = CLIENT_NAMES.filterNot { it in existingIds }.randomOrNull() ?: "${CLIENT_NAMES.random()} #${(100..999).random()}"

        return ClientContract(
            id = "contract_${UUID.randomUUID().toString().take(8)}",
            clientName = name,
            workloadType = workload,
            requiredTflops = roundedTflops,
            monthlyPaymentUsd = monthlyRate,
            contractDurationMonthsTotal = duration,
            monthsRemaining = duration,
            slaRequirementUptime = 99.90 + (Math.random() * 0.09),
            outagePenaltyUsd = penalty
        )
    }

    fun generateInitialMarketContracts(): List<ClientContract> {
        val list = mutableListOf<ClientContract>()
        val usedNames = mutableSetOf<String>()
        repeat(4) {
            val c = generateRandomContract(usedNames)
            usedNames.add(c.clientName)
            list.add(c)
        }
        return list
    }
}
