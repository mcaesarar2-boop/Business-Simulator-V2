package com.caesar.gametycoon.stock.takeover

import androidx.compose.runtime.Composable
import com.example.data.StockItem

/**
 * Standard alias/wrapper for TakeoverBiddingDialog matching architectural specifications.
 */
@Composable
fun BiddingNegotiationDialog(
    stock: StockItem,
    currentOwnedShares: Long,
    playerCash: Long,
    onDismiss: () -> Unit,
    onTakeoverFinalized: (finalPrice: Double, targetStake: Double, customName: String) -> Unit
) {
    TakeoverBiddingDialog(
        stock = stock,
        currentOwnedShares = currentOwnedShares,
        playerCash = playerCash,
        onDismiss = onDismiss,
        onTakeoverFinalized = onTakeoverFinalized
    )
}
