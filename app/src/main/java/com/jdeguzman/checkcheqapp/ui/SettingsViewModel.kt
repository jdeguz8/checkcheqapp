package com.jdeguzman.checkcheqapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.jdeguzman.checkcheqapp.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isSignedIn: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,
    val nearMeRadiusMeters: Int = 1000,
    val startWithNearMe: Boolean = false,
    val defaultCategory: String = "All",
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepo.settingsFlow.collectLatest { s ->
                val user = auth.currentUser
                _uiState.value = SettingsUiState(
                    isSignedIn = user != null,
                    displayName = user?.displayName,
                    email = user?.email,
                    nearMeRadiusMeters = s.nearMeRadiusMeters,
                    startWithNearMe = s.startWithNearMe,
                    defaultCategory = s.defaultCategory,
                    isLoading = false
                )
            }
        }
    }

    fun onNearMeRadiusSelected(meters: Int) {
        viewModelScope.launch { settingsRepo.setNearMeRadius(meters) }
    }

    fun onStartWithNearMeChanged(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setStartWithNearMe(enabled) }
    }

    fun onDefaultCategoryChanged(category: String) {
        viewModelScope.launch { settingsRepo.setDefaultCategory(category) }
    }

    fun signOut() {
        auth.signOut()
        val current = _uiState.value
        _uiState.value = current.copy(
            isSignedIn = false,
            displayName = null,
            email = null
        )
    }
}
