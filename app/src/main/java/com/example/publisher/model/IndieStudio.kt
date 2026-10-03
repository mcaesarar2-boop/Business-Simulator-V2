package com.example.publisher.model

import java.util.UUID

/**
 * Status of an indie studio's pitch proposal to the publisher.
 */
enum class PitchStatus {
    PENDING_REVIEW,
    NEGOTIATING,
    SIGNED,
    REJECTED
}

/**
 * Popular indie gaming genres with distinct market dynamics.
 */
enum class GameGenre(
    val displayName: String,
    val marketSaturation: Double,
    val baseHypePotential: Double,
    val description: String
) {
    ROGUELIKE("Roguelike Deckbuilder", marketSaturation = 18_000.0, baseHypePotential = 1.25, "High replayability, strong Twitch & Steam community virality."),
    SOULSLIKE("Dark Souls-like RPG", marketSaturation = 25_000.0, baseHypePotential = 1.55, "Demanding combat, passionate hardcore audience, high polish expected."),
    METROIDVANIA("Action Metroidvania", marketSaturation = 14_000.0, baseHypePotential = 1.15, "Non-linear exploration, tight platforming, beloved indie staple."),
    COZY_SIM("Cozy Farming & Life Sim", marketSaturation = 20_000.0, baseHypePotential = 1.40, "Massive casual appeal, high wishlist conversion, long sales tail."),
    TACTICAL_RPG("Tactical Turn-Based RPG", marketSaturation = 12_000.0, baseHypePotential = 1.20, "Dedicated strategy niche, premium pricing tolerated."),
    HORROR_SURVIVAL("Psychological Horror", marketSaturation = 16_000.0, baseHypePotential = 1.35, "Streaming sensation, huge weekend launch spikes."),
    ACTION_ADVENTURE("Action Adventure", marketSaturation = 24_000.0, baseHypePotential = 1.30, "Dynamic exploration and cinematic combat."),
    CYBERPUNK("Cyberpunk RPG", marketSaturation = 22_000.0, baseHypePotential = 1.45, "Futuristic neon dystopia with immersive storytelling."),
    CYBERPUNK_ACTION("Cyberpunk Neo-Action", marketSaturation = 22_000.0, baseHypePotential = 1.45, "High aesthetic appeal, demanding visual fidelity."),
    RETRO_PLATFORMER("Retro Pixel Platformer", marketSaturation = 28_000.0, baseHypePotential = 0.95, "Saturated market, requires extraordinary gameplay to stand out.")
}

/**
 * Represents a small independent developer team seeking funding and publishing support.
 */
data class IndieStudio(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val pitchTitle: String,
    val logline: String,
    val talentScore: Double, // 1.0 to 100.0
    val preferredGenre: GameGenre,
    val currentPitchStatus: PitchStatus = PitchStatus.PENDING_REVIEW,
    val teamSize: Int = 4,
    val foundedYear: Int = 2023,
    val requestedTier: FundingTier = FundingTier.STANDARD,
    val requestedRevenueShare: Double = 0.60, // 60% to publisher by default
    val avatarUrl: String = ""
)
