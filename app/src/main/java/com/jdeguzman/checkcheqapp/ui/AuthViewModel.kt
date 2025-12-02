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

/**
 * UI state for the authentication flow.
 *
 * Tracks loading state, whether the user is signed in, and basic
 * display information plus any error message from sign-in.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val displayName: String? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel responsible for authenticating the user with Firebase.
 *
 * Uses Google Sign-In ID tokens to sign in with FirebaseAuth and
 * exposes a simple [AuthUiState] to the UI.
 */
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

    /**
     * Sign in to Firebase using a Google ID token.
     *
     * Updates [uiState] to reflect loading, success, or failure.
     */
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
    /**
     * Sign the current user out of Firebase and reset auth UI state.
     */
    fun signOut() {
        auth.signOut()
        _uiState.value = AuthUiState(isSignedIn = false)
    }
    /**
     * Clear any error message shown in the auth UI.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
