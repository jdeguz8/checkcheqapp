package com.jdeguzman.checkcheqapp.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
    onBack: () -> Unit,
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                    onConfirm = { storeName, itemName, priceText, imageUri ->
                        val price = priceText.toDoubleOrNull() ?: 0.0
                        viewModel.onAddPin(storeName, itemName, price, imageUri)
                    },
                    onDismiss = { viewModel.onDismissDialog() }
                )
            }
        }
    }
}

@Composable
fun AddPriceDialog(
    onConfirm: (storeName: String, itemName: String, priceText: String, imageUri: Uri?) -> Unit,
    onDismiss: () -> Unit
) {
    var storeName by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }

    // Photo state
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add store price") },
        text = {
            Column {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Store name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (imageUri != null) "Change photo" else "Add photo")
                }

                if (imageUri != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Photo selected",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(storeName, itemName, priceText, imageUri)
                }
            ) {
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
