package com.jdeguzman.checkcheqapp.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val displayName: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(
        AuthUiState(
            isSignedIn = auth.currentUser != null,
            displayName = auth.currentUser?.displayName
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState

    fun signInWithGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user

                Log.d("CheckCheqAuth", "Firebase sign-in success: uid=${user?.uid}")

                _uiState.value = AuthUiState(
                    isLoading = false,
                    isSignedIn = user != null,
                    displayName = user?.displayName,
                    errorMessage = null
                )
            } catch (e: Exception) {
                Log.e("CheckCheqAuth", "Firebase sign-in failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignedIn = false,
                        errorMessage = e.localizedMessage ?: "Sign-in failed"
                    )
                }
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = AuthUiState(isSignedIn = false)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
