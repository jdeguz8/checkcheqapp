package com.jdeguzman.checkcheqapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jdeguzman.checkcheqapp.R

/**
 * Settings screen for CheckCheq.
 *
 * Responsibilities:
 * - Shows current account info (via [AuthViewModel])
 * - Lets the user sign out
 * - Lets the user configure:
 *      - default "near me" radius for feed filtering
 *      - whether the feed starts with "near me" enabled
 *      - default category filter
 * - Shows a short "About" section describing the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    // Distance / feed defaults come from SettingsViewModel
    val state by settingsViewModel.uiState.collectAsState()
    // Account info comes directly from AuthViewModel so it updates immediately
    val authState by authViewModel.uiState.collectAsState()

    val radiusOptions = listOf(500, 1000, 3000, 5000)
    val categoryOptions = listOf(
        "All", "Grocery", "Restaurant", "Cafe", "Bakery", "Fast food", "Other"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 🔹 Logo + app name header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.checkcheq_logo),
                contentDescription = "CheckCheq logo",
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = "CheckCheq",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 🔹 Account card (uses AuthViewModel)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium
                )

                if (authState.isSignedIn) {
                    Text(
                        text = authState.displayName ?: authState.email ?: "Signed in user",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    authState.email?.let {
                        if (it != authState.displayName) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            authViewModel.signOut()
                            // if SettingsViewModel tracks any auth-related flags, reset them here
                            settingsViewModel.onSignedOut()
                        }
                    ) {
                        Text("Sign out")
                    }
                } else {
                    Text(
                        text = "You’re not signed in.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = {
                            // in your NavHost, you could navigate to an Auth screen from here
                            // for now this is just a placeholder
                        }
                    ) {
                        Text("Sign in")
                    }
                }
            }
        }

        // 🔹 Distance & Feed defaults
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Distance & Feed defaults",
                    style = MaterialTheme.typography.titleMedium
                )

                // Near-me radius chips
                Text(
                    text = "Near me radius",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    radiusOptions.forEach { option ->
                        val label = if (option < 1000) {
                            "${option} m"
                        } else {
                            "${option / 1000} km"
                        }
                        FilterChip(
                            selected = state.nearMeRadiusMeters == option,
                            onClick = { settingsViewModel.onNearMeRadiusSelected(option) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Start feed with near-me toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start feed with near me enabled",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "If enabled, the feed will default to showing posts within your chosen radius.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.startWithNearMe,
                        onCheckedChange = { settingsViewModel.onStartWithNearMeChanged(it) }
                    )
                }

                // Default category filter dropdown
                Text(
                    text = "Default category filter",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )

                var catExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = !catExpanded }
                ) {
                    OutlinedTextField(
                        value = state.defaultCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Default category") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        categoryOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    settingsViewModel.onDefaultCategoryChanged(option)
                                    catExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // 🔹 About card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "CheckCheq is a crowd-sourced map of grocery and restaurant prices. " +
                            "Long-press on the map or search a place to add posts and help others discover deals nearby.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
