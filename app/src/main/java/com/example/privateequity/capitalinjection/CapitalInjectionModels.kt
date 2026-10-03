package com.example.privateequity.capitalinjection

import com.example.data.MarketNews
import com.example.data.PlayerState

/**
 * Result data class for Capital Injection operations.
 */
data class CapitalInjectionResult(
    val isSuccess: Boolean,
    val message: String,
    val updatedState: PlayerState? = null,
    val newsFeedItem: MarketNews? = null
)
