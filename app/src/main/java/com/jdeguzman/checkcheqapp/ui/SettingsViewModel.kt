package com.jdeguzman.checkcheqapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.jdeguzman.checkcheqapp.data.repository.SettingsRepository
import com.jdeguzman.checkcheqapp.domain.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel that exposes global app settings to the UI and persists changes.
 *
 * Responsibilities:
 * - Collects settings from [SettingsRepository.settingsFlow] (DataStore-backed)
 * - Reads the current Firebase user to populate account info for the Settings screen
 * - Provides intents for updating:
 *   - Near-me search radius
 *   - Whether the feed starts in "near me" mode
 *   - Default category filter
 *   - Theme mode (System / Light / Dark)
 * - Handles sign-out and clears user-related fields in [SettingsUiState].
 */

data class SettingsUiState(
    val isSignedIn: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,

    val nearMeRadiusMeters: Int = 1000,
    val startWithNearMe: Boolean = false,
    val defaultCategory: String = "All",

    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        // Observe DataStore settings and combine with Firebase user
        viewModelScope.launch {
            settingsRepo.settingsFlow.collect { data ->
                val user = auth.currentUser
                _uiState.update {
                    it.copy(
                        nearMeRadiusMeters = data.nearMeRadiusMeters,
                        startWithNearMe = data.startWithNearMe,
                        defaultCategory = data.defaultCategory,
                        themeMode = data.themeMode,
                        isSignedIn = user != null,
                        displayName = user?.displayName,
                        email = user?.email
                    )
                }
            }
        }
    }

    fun onNearMeRadiusSelected(meters: Int) {
        viewModelScope.launch {
            settingsRepo.setNearMeRadius(meters)
        }
    }

    fun onStartWithNearMeChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setStartWithNearMe(enabled)
        }
    }

    fun onDefaultCategoryChanged(category: String) {
        viewModelScope.launch {
            settingsRepo.setDefaultCategory(category)
        }
    }

    fun onThemeModeChanged(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepo.setThemeMode(mode)
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.update {
            it.copy(
                isSignedIn = false,
                displayName = null,
                email = null
            )
        }
    }
}
