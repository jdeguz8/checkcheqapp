package com.jdeguzman.checkcheqapp.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Immutable UI state for authentication.
 *
 * @property isLoading true while an authentication request is in progress.
 * @property isSignedIn true if a Firebase user is currently signed in.
 * @property displayName display name (username or Google profile name) of the signed-in user, if any.
 * @property email email address of the signed-in user, if any.
 * @property errorMessage latest error message to show in the UI, or null if no error.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,
    val errorMessage: String? = null
)

/**
 * ViewModel responsible for user authentication in CheckCheq.
 *
 * This ViewModel wraps FirebaseAuth and exposes a simple [AuthUiState] to the UI layer.
 *
 * Supported authentication methods:
 * - Google Sign-In via ID token ([signInWithGoogleIdToken])
 * - Email & password registration + sign-in ([registerWithEmail], [signInWithEmail])
 *
 * The ViewModel:
 * - Keeps track of the current Firebase user
 * - Exposes loading and error states to drive UI feedback
 * - Updates [displayName] and [email] after each successful auth operation
 */
@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Backing state for auth UI
    private val _uiState = MutableStateFlow(
        AuthUiState(
            isSignedIn = auth.currentUser != null,
            displayName = auth.currentUser?.displayName,
            email = auth.currentUser?.email
        )
    )

    /**
     * Public read-only state for composables to observe.
     */
    val uiState: StateFlow<AuthUiState> = _uiState

    /**
     * Sign in to Firebase using a Google ID token from GoogleSignInClient.
     *
     * This is called after Google Sign-In completes successfully and returns an ID token.
     * On success, [uiState] is updated with the new user's displayName and email.
     *
     * @param idToken Google ID token obtained from GoogleSignInAccount.idToken
     */
    fun signInWithGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user

                Log.d("CheckCheqAuth", "Firebase Google sign-in success: uid=${user?.uid}")

                _uiState.value = AuthUiState(
                    isLoading = false,
                    isSignedIn = user != null,
                    displayName = user?.displayName,
                    email = user?.email,
                    errorMessage = null
                )
            } catch (e: Exception) {
                Log.e("CheckCheqAuth", "Firebase Google sign-in failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignedIn = false,
                        errorMessage = e.localizedMessage ?: "Google sign-in failed"
                    )
                }
            }
        }
    }

    /**
     * Register a new Firebase user with email and password.
     *
     * If [username] is provided and not blank, the method also updates
     * the Firebase user's profile so that [displayName] reflects that username.
     * This username is then used throughout the app, for example as "postedBy".
     *
     * On success, the user is considered signed in.
     *
     * @param email email address to register.
     * @param password password for the new account.
     * @param username optional username to store as Firebase displayName.
     */
    fun registerWithEmail(email: String, password: String, username: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user

                Log.d("CheckCheqAuth", "Firebase email registration success: uid=${user?.uid}")

                // If the user provided a username, update the Firebase profile displayName
                if (user != null && !username.isNullOrBlank()) {
                    val profileUpdates = userProfileChangeRequest {
                        displayName = username
                    }
                    user.updateProfile(profileUpdates).await()
                }

                // Reload user to ensure displayName is up to date
                val refreshedUser = auth.currentUser

                _uiState.value = AuthUiState(
                    isLoading = false,
                    isSignedIn = refreshedUser != null,
                    displayName = refreshedUser?.displayName,
                    email = refreshedUser?.email,
                    errorMessage = null
                )
            } catch (e: Exception) {
                Log.e("CheckCheqAuth", "Email registration failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignedIn = false,
                        errorMessage = e.localizedMessage ?: "Email registration failed"
                    )
                }
            }
        }
    }

    /**
     * Sign in an existing Firebase user using email and password.
     *
     * On success, updates [uiState] with the signed-in user's displayName and email.
     *
     * @param email email address of the existing user.
     * @param password password associated with the account.
     */
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user

                Log.d("CheckCheqAuth", "Firebase email sign-in success: uid=${user?.uid}")

                _uiState.value = AuthUiState(
                    isLoading = false,
                    isSignedIn = user != null,
                    displayName = user?.displayName,
                    email = user?.email,
                    errorMessage = null
                )
            } catch (e: Exception) {
                Log.e("CheckCheqAuth", "Email sign-in failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignedIn = false,
                        errorMessage = e.localizedMessage ?: "Email sign-in failed"
                    )
                }
            }
        }
    }

    /**
     * Sign the current user out of Firebase and reset authentication state.
     *
     * After calling this, [uiState] reflects a signed-out user
     * with no display name, email, or error message.
     */
    fun signOut() {
        auth.signOut()
        _uiState.value = AuthUiState(isSignedIn = false)
    }

    /**
     * Clear any existing error message from the authentication UI state.
     *
     * Useful after the user dismisses an error snackbar or retries an operation.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
