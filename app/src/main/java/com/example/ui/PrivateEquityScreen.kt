package com.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.viewmodel.GameViewModel

/**
 * Legacy forwarder for PrivateEquityScreen.
 * Canonical implementation is located in com.example.privateequity.ui.PrivateEquityScreen.
 */
@Composable
fun PrivateEquityScreen(navController: NavController, viewModel: GameViewModel) {
    com.example.privateequity.ui.PrivateEquityScreen(navController = navController, viewModel = viewModel)
}
