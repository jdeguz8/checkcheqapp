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
 * UI-facing state for Settings screen + global app settings.
 */
data class SettingsUiState(
    val isSignedIn: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,

    val nearMeRadiusMeters: Int = 1000,
    val startWithNearMe: Boolean = false,
    val defaultCategory: String = "All",

    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontScale: Float = 1.0f,
    val notifyOnNearbyPosts: Boolean = true
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
                        fontScale = data.fontScale,
                        notifyOnNearbyPosts = data.notifyOnNearbyPosts,
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

    /**
     * Update the global text scale factor for the app.
     */
    fun onFontScaleChanged(scale: Float) {
        viewModelScope.launch {
            settingsRepo.setFontScale(scale)
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

    fun onNotifyOnNearbyPostsChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.setNotifyOnNearbyPosts(enabled)
        }
    }
}
