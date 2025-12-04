package com.jdeguzman.checkcheqapp.ui

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.jdeguzman.checkcheqapp.R

/**
 * Authentication screen for CheckCheq.
 *
 * This screen provides **two sign-in methods**:
 * - Email & password (with optional username on registration)
 * - Google Sign-In (ID token passed to Firebase Auth)
 *
 * The screen:
 * - Binds to [AuthViewModel] for auth state (loading, errors, user info)
 * - Shows a simple email / password / username form
 * - Exposes buttons to:
 *      - Sign in with email
 *      - Create an account with email + password + optional username
 *      - Sign in with Google
 * - Displays a snackbar for any auth error message
 */
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel
) {
    // Observe current auth state (loading, signed-in flag, displayName/email, error message)
    val state by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Local state holders for email / password / username input fields
    val (email, setEmail) = remember { mutableStateOf("") }
    val (password, setPassword) = remember { mutableStateOf("") }
    val (username, setUsername) = remember { mutableStateOf("") }

    // --- Google Sign-In configuration ---
    // Uses the web client ID from strings.xml to request an ID token.
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(stringResource(id = R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    // Launcher to handle the Activity result for Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("CheckCheqAuth", "onActivityResult: resultCode=${result.resultCode}")

        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                Log.d(
                    "CheckCheqAuth",
                    "Got Google account: ${account.email}, hasIdToken=${idToken != null}"
                )

                if (idToken != null) {
                    // Pass ID token to AuthViewModel so Firebase can sign in
                    authViewModel.signInWithGoogleIdToken(idToken)
                } else {
                    Log.e(
                        "CheckCheqAuth",
                        "idToken is null – check default_web_client_id / web client config"
                    )
                    authViewModel.clearError()
                }
            } catch (e: ApiException) {
                Log.e(
                    "CheckCheqAuth",
                    "Google sign-in failed: statusCode=${e.statusCode}",
                    e
                )
                authViewModel.clearError()
            }
        } else {
            Log.d("CheckCheqAuth", "Sign-in cancelled or failed before OK")
            authViewModel.clearError()
        }
    }

    // --- Screen layout ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Welcome to CheckCheq",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Sign in to save and share nearby price posts.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(8.dp))

            // --- Email / Password / Username section ---

            /**
             * Email input field.
             */
            OutlinedTextField(
                value = email,
                onValueChange = setEmail,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            /**
             * Password input field.
             * (In a real app, you may want to use a password visual transformation.)
             */
            OutlinedTextField(
                value = password,
                onValueChange = setPassword,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            /**
             * Optional username field used when registering a new account.
             * If filled in, it will become the Firebase user displayName.
             */
            OutlinedTextField(
                value = username,
                onValueChange = setUsername,
                label = { Text("Username (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Row of buttons for email-based sign-in / registration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        authViewModel.signInWithEmail(
                            email.trim(),
                            password.trim()
                        )
                    },
                    enabled = !state.isLoading
                ) {
                    Text("Sign in with Email")
                }

                TextButton(
                    onClick = {
                        authViewModel.registerWithEmail(
                            email = email.trim(),
                            password = password.trim(),
                            username = username.trim().ifBlank { null }
                        )
                    },
                    enabled = !state.isLoading
                ) {
                    Text("Create account")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Divider with OR between email methods and Google sign-in
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Divider(modifier = Modifier.weight(1f))
                Text("OR")
                Divider(modifier = Modifier.weight(1f))
            }

            // --- Google Sign-In button ---
            Button(
                onClick = {
                    Log.d("CheckCheqAuth", "Launching Google sign-in intent")
                    launcher.launch(googleSignInClient.signInIntent)
                },
                enabled = !state.isLoading
            ) {
                Text("Sign in with Google")
            }

            if (state.isLoading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }

        // Snackbar for auth error messages, anchored to bottom
        if (state.errorMessage != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Snackbar {
                    Text(text = state.errorMessage ?: "Error")
                }
            }
        }
    }
}
