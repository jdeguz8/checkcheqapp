package com.jdeguzman.checkcheqapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
    onBack: (() -> Unit)? = null,
    viewModel: MyStoresViewModel = hiltViewModel()
) {
    val pins by viewModel.pins.collectAsState()
    val dialogUi by viewModel.dialogUi.collectAsState()

    val winnipeg = remember { LatLng(49.8951, -97.1384) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(winnipeg, 11f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CheckCheq price map") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapLongClick = { latLng ->
                    viewModel.onMapLongClick(
                        lat = latLng.latitude,
                        lng = latLng.longitude
                    )
                }
            ) {
                pins.forEach { pin ->
                    Marker(
                        state = MarkerState(LatLng(pin.lat, pin.lng)),
                        title = "${pin.storeName} - ${pin.itemName}",
                        snippet = "$${"%.2f".format(pin.price)}"
                    )
                }
            }

            if (dialogUi.showAddDialog) {
                AddPriceDialog(
                    onConfirm = { storeName, itemName, priceText ->
                        val price = priceText.toDoubleOrNull() ?: 0.0
                        viewModel.onAddPin(storeName, itemName, price)
                    },
                    onDismiss = { viewModel.onDismissDialog() }
                )
            }
        }
    }
}

@Composable
fun AddPriceDialog(
    onConfirm: (storeName: String, itemName: String, priceText: String) -> Unit,
    onDismiss: () -> Unit
) {
    var storeName by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add store price") },
        text = {
            Column {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Store name") },
                    modifier = Modifier.fillMaxSize()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item name") }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(storeName, itemName, priceText) }) {
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
