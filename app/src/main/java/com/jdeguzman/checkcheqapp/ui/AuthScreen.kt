package com.jdeguzman.checkcheqapp.ui

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
 * Shows a Google Sign-In button, ties into [AuthViewModel],
 * and displays loading and error states during login.
 */
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel
) {
    val state by authViewModel.uiState.collectAsState()
    val context = LocalContext.current

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
                Log.d("CheckCheqAuth", "Got Google account: ${account.email}, hasIdToken=${idToken != null}")

                if (idToken != null) {
                    authViewModel.signInWithGoogleIdToken(idToken)
                } else {
                    Log.e("CheckCheqAuth", "idToken is null – check default_web_client_id / web client config")
                    authViewModel.clearError()
                }
            } catch (e: ApiException) {
                Log.e("CheckCheqAuth", "Google sign-in failed: statusCode=${e.statusCode}", e)
                authViewModel.clearError()
            }
        } else {
            Log.d("CheckCheqAuth", "Sign-in cancelled or failed before OK")
            authViewModel.clearError()
        }
    }

    // --- UI ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Welcome to CheckCheq",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Sign in with Google to see and share nearby price posts.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(32.dp))

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
