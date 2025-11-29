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
import androidx.compose.material3.*
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
import java.text.DateFormat
import java.util.Date


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
        // if not granted, we just won't show distance or near-me filter
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

    // --- "near me" filter state ---
    var nearMeOnly by remember { mutableStateOf(false) }

    val filteredPosts = remember(posts, userLocation, nearMeOnly) {
        if (!nearMeOnly || userLocation == null) {
            posts
        } else {
            posts.filter { post ->
                val dist = computeDistanceMeters(userLocation, post)
                dist != null && dist <= 1000f  // within 1km
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Latest price posts",
                style = MaterialTheme.typography.titleLarge
            )

            TextButton(onClick = onOpenMap) {
                Text("Open map")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Near-me toggle (only meaningful if we have location)
        if (userLocation != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Only show posts near me (≤ 1 km)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = nearMeOnly,
                    onCheckedChange = { nearMeOnly = it }
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        if (filteredPosts.isEmpty()) {
            Text("No posts${if (posts.isEmpty()) "" else " matching filter"}. Long-press on the map to add one.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPosts, key = { it.id }) { post ->
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
    val formattedDate = remember(post.createdAt) {
        val date = Date(post.createdAt)
        DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT
        ).format(date)
    }

    val distanceText = computeDistanceText(userLocation, post)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Thumbnail on the left
            if (!post.photoUri.isNullOrEmpty()) {
                AsyncImage(
                    model = post.photoUri,
                    contentDescription = "Photo of ${post.itemName}",
                    modifier = Modifier
                        .size(80.dp)
                        .clickable { onImageClick() },
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = post.storeName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = post.itemName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(6.dp))

                // Price pill
                Text(
                    text = "$${"%.2f".format(post.price)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Spacer(Modifier.height(6.dp))

                // Meta row: date + distance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    if (distanceText != null) {
                        Text(
                            text = "• $distanceText",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}


// Helper: compute distance in meters (for filter)
private fun computeDistanceMeters(
    userLocation: Location?,
    post: PricePost
): Float? {
    if (userLocation == null) return null
    val results = FloatArray(1)
    Location.distanceBetween(
        userLocation.latitude,
        userLocation.longitude,
        post.lat,
        post.lng,
        results
    )
    return results[0]
}

// Helper: build nice "X m / Y km away" text
private fun computeDistanceText(
    userLocation: Location?,
    post: PricePost
): String? {
    val meters = computeDistanceMeters(userLocation, post) ?: return null

    return if (meters < 1000f) {
        "${meters.toInt()} m away"
    } else {
        String.format("%.1f km away", meters / 1000f)
    }
}
