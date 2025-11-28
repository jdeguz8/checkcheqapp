package com.jdeguzman.checkcheqapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var useCad by remember { mutableStateOf(true) }

//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        Text(
//            text = "Settings",
//            style = MaterialTheme.typography.titleLarge
//        )
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text("Show prices in CAD")
//            Switch(checked = useCad, onCheckedChange = { useCad = it })
//        }

        Text(
            text = "CheckCheq is a crowd-sourced map of grocery prices. " +
                    "Long-press on the map to drop a pin and share a deal with others." +
                    "This screen is under construction and will include profile sections + Dark Mode etc ..",

            style = MaterialTheme.typography.bodyMedium
        )
    }
