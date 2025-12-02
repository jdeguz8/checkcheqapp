package com.jdeguzman.checkcheqapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission") // we guard all location calls with permission checks
@Composable
fun MyStoresScreen(
    onBack: () -> Unit,
    viewModel: MyStoresViewModel = hiltViewModel()
) {
    val pins by viewModel.pins.collectAsState()
    val dialogUi by viewModel.dialogUi.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- Location permission state ---
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
                // ignore
            }
        }
    }

    // --- REAL Google Places search ---
    // Make sure you've called Places.initialize(...) in Application or MainActivity
    val placesClient = remember {
        if (Places.isInitialized()) Places.createClient(context) else null
    }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var searchLoading by remember { mutableStateOf(false) }

    var sessionToken by remember { mutableStateOf(AutocompleteSessionToken.newInstance()) }

    fun runPlaceSearch(query: String) {
        val client = placesClient ?: return
        if (query.isBlank()) {
            searchResults = emptyList()
            return
        }

        searchLoading = true
        searchError = null

        val builder = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setSessionToken(sessionToken)
            .setTypeFilter(TypeFilter.ESTABLISHMENT) // stores, restaurants, etc.

        // Bias results around current location if we have it
        val origin = myLocation
        if (origin != null) {
            val delta = 0.25  // ~25km box
            val southWest = LatLng(origin.latitude - delta, origin.longitude - delta)
            val northEast = LatLng(origin.latitude + delta, origin.longitude + delta)
            builder.setLocationBias(RectangularBounds.newInstance(southWest, northEast))
        }

        // Optional: restrict to Canada
        builder.setCountries("CA")

        val request = builder.build()

        client.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                searchLoading = false
                searchResults = response.autocompletePredictions
            }
            .addOnFailureListener { e ->
                searchLoading = false
                searchError = e.localizedMessage ?: "Search failed"
                searchResults = emptyList()
            }
    }

    // When user taps a prediction
    val onPredictionClicked: (AutocompletePrediction) -> Unit = onPredictionClicked@{ prediction ->
        val client = placesClient ?: return@onPredictionClicked

        searchQuery = prediction.getFullText(null).toString()
        searchResults = emptyList()
        searchError = null
        searchLoading = true

        val placeId = prediction.placeId
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS
        )

        val request = FetchPlaceRequest.builder(placeId, placeFields)
            .setSessionToken(sessionToken)
            .build()

        client.fetchPlace(request)
            .addOnSuccessListener { response ->
                searchLoading = false
                val place = response.place
                val latLng = place.latLng

                if (latLng != null) {
                    scope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                        )
                    }

                    // 👉 Open Add Price dialog with store name prefilled
                    viewModel.onPlaceSelected(
                        lat = latLng.latitude,
                        lng = latLng.longitude,
                        storeName = place.name
                    )
                }
            }

            .addOnFailureListener { e ->
                searchLoading = false
                searchError = e.localizedMessage ?: "Failed to load place details"
            }
    }


    // --- Near-me filter state for map pins ---
    var nearMeOnly by remember { mutableStateOf(false) }

    val visiblePins = remember(pins, myLocation, nearMeOnly) {
        val origin = myLocation
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
                dist != null && dist <= 1000f // ≤ 1 km
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
            // --- Map ---
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

            // --- Search UI overlay (opaque, elevated) ---
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    tonalElevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { value ->
                                searchQuery = value
                                searchError = null
                                if (value.length >= 2) {
                                    runPlaceSearch(value)
                                } else {
                                    searchResults = emptyList()
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Search places (e.g. Walmart)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                when {
                                    searchLoading -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }

                                    searchQuery.isNotBlank() -> {
                                        IconButton(onClick = {
                                            searchQuery = ""
                                            searchResults = emptyList()
                                            searchError = null
                                            sessionToken =
                                                AutocompleteSessionToken.newInstance()
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Clear"
                                            )
                                        }
                                    }
                                }
                            }
                        )

                        if (searchLoading) {
                            Text(
                                text = "Searching…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (searchError != null) {
                            Text(
                                text = searchError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                if (searchResults.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column {
                            searchResults.forEach { prediction ->
                                val primary = prediction.getPrimaryText(null).toString()
                                val secondary =
                                    prediction.getSecondaryText(null).toString()

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onPredictionClicked(prediction) }
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = primary,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        if (secondary.isNotBlank()) {
                                            Text(
                                                text = secondary,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Near-me toggle overlay (pushed down below search)
            if (hasLocationPermission && myLocation != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 96.dp, end = 16.dp),
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

            // Add-price dialog
            if (dialogUi.showAddDialog) {
                AddPriceDialog(
                    initialStoreName = dialogUi.suggestedStoreName,
                    onConfirm = { storeName, itemName, priceText, photoUri, category ->
                        val price = priceText.toDoubleOrNull() ?: 0.0
                        viewModel.onAddPin(storeName, itemName, price, photoUri, category)
                    },
                    onDismiss = { viewModel.onDismissDialog() }
                )
            }

        }
    }
}

/**
 * Distance helper for the map-near-me filter.
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPriceDialog(
    initialStoreName: String? = null,
    onConfirm: (
        storeName: String,
        itemName: String,
        priceText: String,
        photoUri: String?,
        category: String?
    ) -> Unit,
    onDismiss: () -> Unit
)
 {
     var storeName by remember { mutableStateOf(initialStoreName.orEmpty()) }
    var itemName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var pickedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val categoryOptions =
        listOf("Grocery", "Restaurant", "Cafe", "Bakery", "Fast food", "Other")
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        pickedPhotoUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add price post") },
        text = {
            Column {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Store / restaurant name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item / dish name") },
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
                Spacer(Modifier.height(8.dp))

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        placeholder = { Text("Grocery, Restaurant…") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categoryOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedCategory = option
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

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
                    onConfirm(
                        storeName.trim(),
                        itemName.trim(),
                        priceText.trim(),
                        pickedPhotoUri?.toString(),
                        selectedCategory
                    )
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
