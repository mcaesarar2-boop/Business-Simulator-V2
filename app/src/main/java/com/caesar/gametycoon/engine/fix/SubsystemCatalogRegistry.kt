package com.caesar.gametycoon.engine.fix

/**
 * Audit Fix: SubsystemCatalogRegistry
 *
 * Resolves the systematic double-ticking bug caused by catalog ID mismatches
 * between CoreBusinessEngine and specialized industry engines.
 *
 * MISMATCH MATRIX RESOLVED:
 * - Legacy "banking_finance"       -> Canonical "tycoon_bank"
 * - Legacy "aviation_subsystem"    -> Canonical "aviation_group"
 * - Legacy "construction_developer"-> Canonical "construction"
 * - Legacy "hospitality_hotel"     -> Canonical "hospitality_holding"
 * - Legacy "theme_park"            -> Canonical "theme_park_holding"
 * - Legacy "sports_football"       -> Canonical "football_club"
 */
object SubsystemCatalogRegistry {

    /**
     * Complete canonical catalog IDs for all 12 decoupled domain subsystems.
     */
    val CANONICAL_SPECIALIZED_CATALOG_IDS: Set<String> = setOf(
        "tycoon_bank",           // Private & Commercial Banking Subsystem
        "aviation_group",        // Aviation Group & Airline Fleet
        "construction",          // Civil Construction & Real Estate Developer
        "hospitality_holding",   // Hotels, Resorts & Luxury Hospitality
        "theme_park_holding",    // Theme Parks & City Land Parcels
        "football_club",         // Professional Football / Sports Club
        "content_creator",       // Creative Talent, Brand Deals & Royalties
        "streaming_service",     // Video Streaming & OTT Platform
        "media_production",      // Film Studio & Theatrical Box Office
        "mid_logistics",         // Freight Logistics & Fleet Routing
        "indie_game_publisher",  // Game Publisher & Incubation Studio
        "ai_cloud_provider"      // AI Cloud Provider & Data Center GPU Cluster
    )

    /**
     * Backward-compatibility translation dictionary mapping old/deprecated
     * subsystem IDs to their canonical database catalog IDs.
     */
    private val LEGACY_ALIAS_MAP: Map<String, String> = mapOf(
        "banking_finance" to "tycoon_bank",
        "aviation_subsystem" to "aviation_group",
        "construction_developer" to "construction",
        "hospitality_hotel" to "hospitality_holding",
        "theme_park" to "theme_park_holding",
        "sports_football" to "football_club"
    )

    /**
     * Checks if a business catalog ID is managed by a specialized domain engine.
     * Checks both canonical IDs and any legacy aliases.
     */
    fun isSpecializedSubsystem(catalogId: String?): Boolean {
        if (catalogId.isNullOrBlank()) return false
        val normalized = normalizeCatalogId(catalogId)
        return CANONICAL_SPECIALIZED_CATALOG_IDS.contains(normalized)
    }

    /**
     * Determines whether [com.example.core.engine.CoreBusinessEngine] should process this business.
     * Returns true ONLY if it is a general business (retail, F&B, mining, energy, tech startup)
     * and NOT an isolated subsystem.
     */
    fun shouldCoreBusinessEngineProcess(catalogId: String?): Boolean {
        return !isSpecializedSubsystem(catalogId)
    }

    /**
     * Translates any legacy alias into the canonical catalog ID.
     */
    fun normalizeCatalogId(catalogId: String): String {
        return LEGACY_ALIAS_MAP[catalogId] ?: catalogId
    }

    /**
     * Returns a human-readable title for the specialized subsystem.
     */
    fun getSubsystemName(catalogId: String): String {
        return when (normalizeCatalogId(catalogId)) {
            "tycoon_bank" -> "Tycoon Private Banking"
            "aviation_group" -> "Aviation Group & Fleet"
            "construction" -> "Construction & Infrastructure"
            "hospitality_holding" -> "Hospitality & Resorts"
            "theme_park_holding" -> "Theme Parks & Resorts"
            "football_club" -> "Football Club Management"
            "content_creator" -> "Content Creator Studio"
            "streaming_service" -> "Streaming Service Platform"
            "media_production" -> "Film Studio & Production"
            "mid_logistics" -> "Mid Logistics Transport"
            "indie_game_publisher" -> "Game Publisher & Incubator"
            "ai_cloud_provider" -> "AI Cloud & Data Center"
            else -> "Standard Business"
        }
    }
}
