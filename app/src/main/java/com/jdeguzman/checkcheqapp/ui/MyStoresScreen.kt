package com.jdeguzman.checkcheqapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import android.location.Location

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission") // we gate location features behind permission checks
@Composable
fun MyStoresScreen(
    onBack: () -> Unit,
    viewModel: MyStoresViewModel = hiltViewModel()
) {
    val pins by viewModel.pins.collectAsState()
    val dialogUi by viewModel.dialogUi.collectAsState()

    val context = LocalContext.current
    val fineLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                fineLocationPermission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    // Ask for permission on first composition
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(fineLocationPermission)
        }
    }

    // ---- map camera + my location ----
    val defaultCenter = remember { LatLng(49.8951, -97.1384) } // Winnipeg
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 11f)
    }

    var myLocation by remember { mutableStateOf<LatLng?>(null) }

    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // When permission is granted, try to get last known location
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        val here = LatLng(loc.latitude, loc.longitude)
                        myLocation = here
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(here, 13f)
                    }
                }
            } catch (_: SecurityException) {
                // permission revoked mid-way, ignore
            }
        }
    }

    // --- Near-me filter state for map ---
    var nearMeOnly by remember { mutableStateOf(false) }

    val visiblePins = remember(pins, myLocation, nearMeOnly) {
        val origin = myLocation  // local snapshot

        if (!nearMeOnly || origin == null) {
            pins
        } else {
            pins.filter { post ->
                val dist = computeDistanceMeters(
                    userLat = origin.latitude,
                    userLng = origin.longitude,
                    postLat = post.lat,
                    postLng = post.lng
                )
                dist != null && dist <= 1000f   // ≤ 1km
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CheckCheq price map") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
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
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = hasLocationPermission,
                    zoomControlsEnabled = true
                ),
                onMapLongClick = { latLng ->
                    viewModel.onMapLongClick(
                        lat = latLng.latitude,
                        lng = latLng.longitude
                    )
                }
            ) {
                visiblePins.forEach { pin ->
                    Marker(
                        state = MarkerState(LatLng(pin.lat, pin.lng)),
                        title = "${pin.storeName} - ${pin.itemName}",
                        snippet = "$${"%.2f".format(pin.price)}"
                    )
                }
            }

            // Near-me toggle overlay (if we have location)
            if (hasLocationPermission && myLocation != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Near me (≤ 1 km)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = nearMeOnly,
                            onCheckedChange = { nearMeOnly = it }
                        )
                    }
                }
            }

            if (dialogUi.showAddDialog) {
                AddPriceDialog(
                    onConfirm = { storeName, itemName, priceText, photoUri ->
                        val price = priceText.toDoubleOrNull() ?: 0.0
                        viewModel.onAddPin(storeName, itemName, price, photoUri)
                    },
                    onDismiss = { viewModel.onDismissDialog() }
                )
            }
        }
    }
}

/**
 * Small helper for distance in meters.
 */
/**
 * Small helper for distance in meters.
 */
private fun computeDistanceMeters(
    userLat: Double,
    userLng: Double,
    postLat: Double,
    postLng: Double
): Float? {
    val results = FloatArray(1)
    Location.distanceBetween(
        userLat,
        userLng,
        postLat,
        postLng,
        results
    )
    return results[0]
}



@Composable
fun AddPriceDialog(
    onConfirm: (
        storeName: String,
        itemName: String,
        priceText: String,
        photoUri: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var storeName by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var pickedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        pickedPhotoUri = uri
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

                Button(
                    onClick = { photoPickerLauncher.launch("image/*") }
                ) {
                    Text(
                        if (pickedPhotoUri == null) "Add photo"
                        else "Change photo"
                    )
                }

                if (pickedPhotoUri != null) {
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = pickedPhotoUri,
                        contentDescription = "Preview photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // (Optional) guard: don’t save empty store/item
                    if (storeName.isBlank() || itemName.isBlank() || priceText.isBlank()) {
                        // you could show a Snackbar or error later
                        return@TextButton
                    }

                    onConfirm(
                        storeName.trim(),
                        itemName.trim(),
                        priceText.trim(),
                        pickedPhotoUri?.toString()
                    )

                    // 🔒 Close the dialog so we don't create duplicates
                    onDismiss()
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

