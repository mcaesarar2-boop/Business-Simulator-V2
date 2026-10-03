package com.example.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Model class for cloud saving to Firestore.
 * In Kotlin, providing default values for all constructor parameters
 * automatically generates a no-argument constructor, which is a hard requirement for Firestore.
 */
data class PlayerGameState(
    @DocumentId
    val userId: String = "",

    @PropertyName("last_saved_ms")
    val lastSavedMs: Long = System.currentTimeMillis(),

    @PropertyName("private_balance")
    val privateBalance: Long = 0L,

    @PropertyName("total_fortune")
    val totalFortune: Long = 0L,

    @PropertyName("in_game_month")
    val inGameMonth: Int = 1,

    @PropertyName("in_game_year")
    val inGameYear: Int = 1,

    @PropertyName("owned_businesses_count")
    val ownedBusinessesCount: Int = 0,

    // To prevent data parsing issues with deep hierarchies / custom classes / interfaces,
    // the full complex PlayerState is serialized into a JSON string for absolute data integrity.
    @PropertyName("full_state_json")
    val fullStateJson: String = ""
)
