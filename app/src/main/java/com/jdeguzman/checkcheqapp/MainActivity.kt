package com.jdeguzman.checkcheqapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jdeguzman.checkcheqapp.ui.BasketScreen
import dagger.hilt.android.AndroidEntryPoint
import com.jdeguzman.checkcheqapp.ui.MyStoresScreen
import com.jdeguzman.checkcheqapp.ui.MyStoresViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                AppNav()
            }
        }
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "basket") {
        composable("basket") {
            BasketScreen(
                onCompare = { nav.navigate("map") } // reuse this as "go to map"
            )
        }
        composable("map") {
            MyStoresScreen(
                onBack = { nav.popBackStack() }
            )
        }
    }
}
