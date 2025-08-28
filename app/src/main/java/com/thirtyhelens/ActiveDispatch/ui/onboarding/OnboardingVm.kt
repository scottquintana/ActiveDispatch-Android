package com.thirtyhelens.ActiveDispatch.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface UiState { object ShowTos : UiState; object ShowLocation : UiState; object Done : UiState }

class OnboardingViewModel(app: Application) : AndroidViewModel(app) {

    // Expose readiness for the splash to wait on
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    // UI state for which screen to show
    private val _ui = MutableStateFlow<UiState>(UiState.ShowTos)
    val ui: StateFlow<UiState> = _ui

    init {
        // Do the initial DataStore read once
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val tosAccepted = FirstRunPrefs.tosAccepted(ctx).first()
            val locationFlowDone = FirstRunPrefs.locationFlowDone(ctx).first()

            _ui.value = when {
                !tosAccepted -> UiState.ShowTos
                !locationFlowDone -> UiState.ShowLocation
                else -> UiState.Done
            }
            _isReady.value = true
        }
    }

    fun acceptTos() {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            FirstRunPrefs.setTosAccepted(ctx, true)
            if (_ui.value is UiState.ShowTos) _ui.value = UiState.ShowLocation
        }
    }

    fun completeLocationFlow() {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            FirstRunPrefs.setLocationFlowDone(ctx, true)
            _ui.value = UiState.Done
        }
    }
}