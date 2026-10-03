package com.example.data

data class StartupInvestment(
    val id: String,
    val name: String,
    val description: String,
    val requiredInvestment: Long,
    val potentialReturnMultiplier: Float,
    val durationMonths: Int,
    val successProbability: Float
)

data class ActiveStartupInvestment(
    val id: String,
    val startupName: String,
    val investedAmount: Long,
    val potentialReturn: Long,
    val monthsRemaining: Int,
    val successProbability: Float
)

fun generateYearlyStartups(year: Int): List<StartupInvestment> {
    val prefixes = listOf("Neuro", "Quantum", "Aero", "Bio", "Cyber", "Eco", "Fin", "Holo", "Omni", "Syn", "Neo", "Data", "Aura")
    val suffixes = listOf("Tech", "Systems", "Labs", "Dynamics", "Net", "Pulse", "Core", "Link", "Shift", "Gen", "Forge", "Matrix")
    val industries = listOf("Artificial Intelligence", "Biotech", "Space Exploration", "Green Energy", "Robotics", "Web3", "MedTech", "EdTech")
    
    val rnd = java.util.Random(year.toLong())
    val startups = mutableListOf<StartupInvestment>()
    for (i in 1..10) {
        val name = "${prefixes[rnd.nextInt(prefixes.size)]}${suffixes[rnd.nextInt(suffixes.size)]}"
        val desc = "Inovasi mutakhir di bidang ${industries[rnd.nextInt(industries.size)]}."
        val reqInv = (rnd.nextInt(50) + 5) * 10_000L // $50k to $540k
        val multiplier = 2f + rnd.nextFloat() * 18f // 2x to 20x
        val duration = 3 + rnd.nextInt(22) // 3 to 24 months
        val prob = 0.05f + rnd.nextFloat() * 0.35f // 5% to 40% success rate
        
        startups.add(StartupInvestment(
            id = "su_${year}_$i",
            name = name,
            description = desc,
            requiredInvestment = reqInv,
            potentialReturnMultiplier = multiplier,
            durationMonths = duration,
            successProbability = prob
        ))
    }
    return startups
}
