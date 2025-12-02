package com.jdeguzman.checkcheqapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jdeguzman.checkcheqapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel
) {
    val state by settingsViewModel.uiState.collectAsState()

    val radiusOptions = listOf(500, 1000, 3000, 5000)
    val categoryOptions = listOf(
        "All", "Grocery", "Restaurant", "Cafe", "Bakery", "Fast food", "Other"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Logo + app name
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

        // 🔹 Account
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium
                )

                if (state.isSignedIn) {
                    Text(
                        text = state.displayName ?: state.email ?: "Signed in user",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    state.email?.let {
                        if (it != state.displayName) {
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
                            // Update both VMs so UI + root react
                            settingsViewModel.signOut()
                            authViewModel.signOut()
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
                }
            }
        }

        // 🔹 Distance & Feed defaults (unchanged, just using settingsViewModel)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Distance & Feed defaults",
                    style = MaterialTheme.typography.titleMedium
                )

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
                            label = { Text(label) }
                        )
                    }
                }

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

        // 🔹 About card (leave as you have it, or keep this)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                // If BuildConfig is being annoying, you can hardcode the version here for now.
            }
        }
    }
}
