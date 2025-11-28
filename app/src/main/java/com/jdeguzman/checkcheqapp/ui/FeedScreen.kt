package com.jdeguzman.checkcheqapp.ui

import android.Manifest
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.jdeguzman.checkcheqapp.domain.PricePost

@Composable
fun FeedScreen(
    viewModel: MyStoresViewModel = hiltViewModel(),
    onOpenMap: () -> Unit = {}
) {
    val posts by viewModel.pins.collectAsState()

    var selectedPost by remember { mutableStateOf<PricePost?>(null) }

    // --- user location state ---
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<Location?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) userLocation = loc
            }
        }
        // if not granted, we just won't show distance
    }

    // Ask for permission and / or get last known location once
    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val granted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (granted) {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) userLocation = loc
            }
        } else {
            permissionLauncher.launch(permission)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Latest price posts",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        if (posts.isEmpty()) {
            Text("No posts yet. Long-press on the map to add one.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    PricePostCard(
                        post = post,
                        userLocation = userLocation,
                        onImageClick = { selectedPost = post }
                    )
                }
            }
        }
    }

    // Full-screen photo dialog
    if (selectedPost?.photoUri != null) {
        Dialog(onDismissRequest = { selectedPost = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { selectedPost = null }
            ) {
                AsyncImage(
                    model = selectedPost!!.photoUri,
                    contentDescription = "Full photo of ${selectedPost!!.itemName}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun PricePostCard(
    post: PricePost,
    userLocation: Location?,
    onImageClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = post.storeName, style = MaterialTheme.typography.titleMedium)
            Text(text = post.itemName, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$${"%.2f".format(post.price)}",
                style = MaterialTheme.typography.bodyLarge
            )

            // --- Distance text: "X m away" or "Y km away" ---
            val distanceText by remember(userLocation, post.lat, post.lng) {
                mutableStateOf(computeDistanceText(userLocation, post))
            }

            if (distanceText != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = distanceText!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (!post.photoUri.isNullOrEmpty()) {
                Spacer(Modifier.height(8.dp))
                AsyncImage(
                    model = post.photoUri,
                    contentDescription = "Photo of ${post.itemName}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clickable { onImageClick() },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

// Helper: build nice "X m / Y km away" text
private fun computeDistanceText(
    userLocation: Location?,
    post: PricePost
): String? {
    if (userLocation == null) return null

    val results = FloatArray(1)
    Location.distanceBetween(
        userLocation.latitude,
        userLocation.longitude,
        post.lat,
        post.lng,
        results
    )
    val meters = results[0]

    return if (meters < 1000f) {
        "${meters.toInt()} m away"
    } else {
        String.format("%.1f km away", meters / 1000f)
    }
}
