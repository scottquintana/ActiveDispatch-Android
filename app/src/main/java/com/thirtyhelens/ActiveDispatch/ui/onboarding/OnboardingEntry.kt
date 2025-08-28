package com.thirtyhelens.ActiveDispatch.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thirtyhelens.ActiveDispatch.ui.home.HomeScreen

@Composable
fun OnboardingEntry(vm: OnboardingViewModel) {
    val uiState = vm.ui.collectAsStateWithLifecycle().value

    when (uiState) {
        UiState.ShowTos -> TosScreen(
            onAcknowledge = { vm.acceptTos() }
        )

        UiState.ShowLocation -> LocationPermissionScreen(
            onFinished = { vm.completeLocationFlow() }
        )

        UiState.Done -> HomeScreen()
    }
}