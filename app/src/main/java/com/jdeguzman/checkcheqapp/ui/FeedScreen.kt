package com.jdeguzman.checkcheqapp.ui

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    onOpenMap: () -> Unit = {},
    onOpenPostDetails: (Long) -> Unit = {}
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
    }

    // Ask for permission and/or get last known location once
    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val granted = ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) userLocation = loc
            }
        } else {
            permissionLauncher.launch(permission)
        }
    }

    // --- filters ---
    var nearMeOnly by remember { mutableStateOf(false) }

    // Static list of categories for now
    val categoryOptions = listOf(
        "All",
        "Grocery",
        "Restaurant",
        "Cafe",
        "Bakery",
        "Fast food",
        "Other"
    )
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredPosts = remember(posts, userLocation, nearMeOnly, selectedCategory) {
        var base = posts.sortedByDescending { it.createdAt }

        if (selectedCategory != "All") {
            base = base.filter { post ->
                post.category?.equals(selectedCategory, ignoreCase = true) == true
            }
        }


        if (nearMeOnly && userLocation != null) {
            base = base.filter { post ->
                val dist = computeDistanceMetersForFeed(userLocation, post)
                dist != null && dist <= 1000f
            }
        }

        base
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Latest posts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            TextButton(onClick = onOpenMap) {
                Text("Open map")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Category chips
        CategoryFilterRow(
            options = categoryOptions,
            selected = selectedCategory,
            onSelectedChange = { selectedCategory = it }
        )

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
                androidx.compose.material3.Switch(
                    checked = nearMeOnly,
                    onCheckedChange = { nearMeOnly = it }
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Divider()

        Spacer(Modifier.height(8.dp))

        if (filteredPosts.isEmpty()) {
            Text(
                text = if (posts.isEmpty())
                    "No posts yet. Long-press on the map to add one."
                else
                    "No posts match the current filters."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPosts, key = { it.id }) { post ->
                    PricePostCard(
                        post = post,
                        userLocation = userLocation,
                        onImageClick = { selectedPost = post },
                        onCardClick = { onOpenPostDetails(post.id) }
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
private fun CategoryFilterRow(
    options: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelectedChange(option) },
                label = { Text(option) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun PricePostCard(
    post: PricePost,
    userLocation: Location?,
    onImageClick: () -> Unit = {},
    onCardClick: () -> Unit = {}
) {
    val formattedDate = remember(post.createdAt) {
        val date = Date(post.createdAt)
        DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT
        ).format(date)
    }

    val distanceText = remember(userLocation, post.lat, post.lng) {
        computeDistanceTextForFeed(userLocation, post)
    }

    val postedBy = post.postedBy ?: "Anonymous"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
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

                    if (!post.category.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = post.category!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

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

                    // Meta row: posted by + date + distance
                    Column {
                        Text(
                            text = "Posted by $postedBy",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

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
    }
}

// --- Helpers local to feed screen (no name clashes) ---

private fun computeDistanceMetersForFeed(
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

private fun computeDistanceTextForFeed(
    userLocation: Location?,
    post: PricePost
): String? {
    val meters = computeDistanceMetersForFeed(userLocation, post) ?: return null

    return if (meters < 1000f) {
        "${meters.toInt()} m away"
    } else {
        String.format("%.1f km away", meters / 1000f)
    }
}
