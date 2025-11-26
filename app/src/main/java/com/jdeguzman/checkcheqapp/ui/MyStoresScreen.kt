package com.jdeguzman.checkcheqapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStoresScreen(
    onBack: () -> Unit,
    viewModel: MyStoresViewModel = hiltViewModel()
) {
    val pins by viewModel.pins.collectAsState()
    val pendingLatLng by viewModel.pendingLatLng.collectAsState()

    // Center on Winnipeg for now
    val winnipeg = remember { LatLng(49.8951, -97.1384) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(winnipeg, 11f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CheckCheq price map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<") // you can swap this for a real back icon
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapLongClick = { latLng ->
                    viewModel.onMapLongClick(latLng)
                }
            ) {
                pins.forEach { pin ->
                    Marker(
                        state = MarkerState(position = pin.position),
                        title = pin.storeName,
                        snippet = "${pin.itemName} – $${pin.price}"
                    )
                }
            }

            if (pendingLatLng != null) {
                AddPinDialog(
                    onDismiss = { viewModel.cancelAddPin() },
                    onSave = { itemName, price, storeName ->
                        viewModel.addPin(itemName, price, storeName)
                    }
                )
            }
        }
    }
}

@Composable
private fun AddPinDialog(
    onDismiss: () -> Unit,
    onSave: (itemName: String, price: Double, storeName: String) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add price pin") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item (e.g. 2L milk)") }
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price (e.g. 4.99)") }
                )
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Store name") }
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val price = priceText.toDoubleOrNull()
                if (itemName.isBlank() || storeName.isBlank() || price == null) {
                    error = "Please enter item, price, and store."
                } else {
                    onSave(itemName, price, storeName)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
