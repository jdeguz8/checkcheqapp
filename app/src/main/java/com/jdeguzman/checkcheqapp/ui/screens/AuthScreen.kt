package com.jdeguzman.checkcheqapp.ui

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.jdeguzman.checkcheqapp.R

/**
 * High-level authentication screen for CheckCheq.
 *
 * Provides two flows:
 * - Login mode (default): email/password + Google Sign-In.
 * - Register mode: email/password + optional username.
 *
 * Also includes:
 * - Password visibility toggle.
 * - Mode toggle between sign-in and sign-up.
 * - Loading indicator and error snackbar via [AuthViewModel].
 *
 * @param authViewModel ViewModel that handles Firebase auth operations.
 */
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel
) {
    val state by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Local form state
    val (email, setEmail) = remember { mutableStateOf("") }
    val (password, setPassword) = remember { mutableStateOf("") }
    val (username, setUsername) = remember { mutableStateOf("") }

    // Password visibility toggle (false = hidden / masked by default)
    val (isPasswordVisible, setIsPasswordVisible) = remember { mutableStateOf(false) }

    // Whether we are in "register" or "login" mode
    val (isRegisterMode, setIsRegisterMode) = remember { mutableStateOf(false) }

    // --- Google Sign-In config ---
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(stringResource(id = R.string.default_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

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

    // --- Layout ---
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
            Image(
                painter = painterResource(id = R.drawable.checkcheq_logo),
                contentDescription = "CheckCheq logo",
                modifier = Modifier.size(140.dp)
            )

            Text(
                text = if (isRegisterMode) "Create your CheckCheq account" else "Welcome to CheckCheq",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = if (isRegisterMode)
                    "Sign up to start saving and sharing nearby prices."
                else
                    "Sign in to save and share nearby price posts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // --- Email / Password (+ username for register mode) ---

            OutlinedTextField(
                value = email,
                onValueChange = setEmail,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = setPassword,
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                trailingIcon = {
                    val icon = if (isPasswordVisible) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    }
                    val description = if (isPasswordVisible) {
                        "Hide password"
                    } else {
                        "Show password"
                    }

                    IconButton(onClick = { setIsPasswordVisible(!isPasswordVisible) }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = description
                        )
                    }
                }
            )

            if (isRegisterMode) {
                OutlinedTextField(
                    value = username,
                    onValueChange = setUsername,
                    label = { Text("Username (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            if (isRegisterMode) {
                // Create account button
                Button(
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
            } else {
                // Login button + Google sign-in in login mode
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Divider(modifier = Modifier.weight(1f))
                    Text("OR")
                    Divider(modifier = Modifier.weight(1f))
                }

                Button(
                    onClick = {
                        Log.d("CheckCheqAuth", "Launching Google sign-in intent")
                        launcher.launch(googleSignInClient.signInIntent)
                    },
                    enabled = !state.isLoading
                ) {
                    Text("Sign in with Google")
                }
            }

            if (state.isLoading) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            Spacer(Modifier.height(8.dp))

            // Toggle between login and register modes
            TextButton(
                onClick = {
                    setIsRegisterMode(!isRegisterMode)
                    authViewModel.clearError()
                }
            ) {
                Text(
                    if (isRegisterMode)
                        "Already have an account? Sign in"
                    else
                        "Not a user? Create an account"
                )
            }
        }

        // Error snackbar
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
