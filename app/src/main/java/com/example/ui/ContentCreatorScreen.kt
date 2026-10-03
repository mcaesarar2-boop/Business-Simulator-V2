package com.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.viewmodel.GameViewModel

/**
 * Legacy forwarder for ContentCreatorScreen.
 * Canonical implementation has been modularized into:
 * com.example.businessunit.contentcreator.ui.ContentCreatorScreen
 */
@Composable
fun ContentCreatorScreen(
    navController: NavController,
    viewModel: GameViewModel? = null,
    gameViewModel: GameViewModel? = null,
    instanceId: String? = null,
    businessInstanceId: String? = null
) {
    val vm = viewModel ?: gameViewModel ?: return
    val id = instanceId ?: businessInstanceId
    com.example.businessunit.contentcreator.ui.ContentCreatorScreen(
        navController = navController,
        viewModel = vm,
        instanceId = id
    )
}
