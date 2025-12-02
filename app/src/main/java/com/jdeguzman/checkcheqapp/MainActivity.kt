package com.jdeguzman.checkcheqapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.libraries.places.api.Places
import com.jdeguzman.checkcheqapp.ui.AuthScreen
import com.jdeguzman.checkcheqapp.ui.AuthViewModel
import com.jdeguzman.checkcheqapp.ui.FeedScreen
import com.jdeguzman.checkcheqapp.ui.MyStoresScreen
import com.jdeguzman.checkcheqapp.ui.MyStoresViewModel
import com.jdeguzman.checkcheqapp.ui.PricePostDetailsScreen
import com.jdeguzman.checkcheqapp.ui.SettingsScreen
import com.jdeguzman.checkcheqapp.ui.SettingsViewModel
import com.jdeguzman.checkcheqapp.ui.theme.CheckCheqTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialize Places SDK once
        if (!Places.isInitialized()) {
            Places.initialize(
                applicationContext,
                getString(R.string.google_maps_key)   // same key you use for Maps
            )
        }

        setContent {
            CheckCheqTheme {
                CheckCheqAppRoot()
            }
        }
    }
}

@Composable
fun CheckCheqAppRoot() {
    // One AuthViewModel for the whole activity
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()

    if (!authState.isSignedIn) {
        // 🔐 Not signed in → show auth screen
        AuthScreen(authViewModel = authViewModel)
    } else {
        // ✅ Signed in → show main app with bottom nav
        MainAppScaffold(authViewModel = authViewModel)
    }
}

@Composable
private fun MainAppScaffold(
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: "feed"

    // Shared VM for map + feed
    val myStoresViewModel: MyStoresViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == "feed",
                    onClick = {
                        navController.navigate("feed") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.List, contentDescription = "Feed") },
                    label = { Text("Feed") }
                )
                NavigationBarItem(
                    selected = currentRoute == "map",
                    onClick = {
                        navController.navigate("map") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
                    label = { Text("Map") }
                )
                NavigationBarItem(
                    selected = currentRoute == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "feed",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("feed") {
                FeedScreen(
                    viewModel = myStoresViewModel,
                    onOpenMap = { navController.navigate("map") },
                    onOpenPostDetails = { postId ->
                        navController.navigate("details/$postId")
                    }
                )
            }
            composable("map") {
                MyStoresScreen(
                    onBack = { navController.navigate("feed") },
                    viewModel = myStoresViewModel
                )
            }
            composable("settings") {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    authViewModel = authViewModel
                )
            }
            composable("details/{postId}") { backStackEntry ->
                val postId =
                    backStackEntry.arguments?.getString("postId")?.toLongOrNull() ?: -1L
                PricePostDetailsScreen(
                    postId = postId,
                    viewModel = myStoresViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
