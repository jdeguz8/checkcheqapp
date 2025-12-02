package com.jdeguzman.checkcheqapp.ui

import android.location.Location
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.jdeguzman.checkcheqapp.domain.PricePost
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.util.Date

/**
 * Detail screen for a single [PricePost].
 *
 * Shows a large hero image (if present), store and item name, a prominent
 * price chip, category, creation time, and distance. Also includes a
 * "Location" section and an "Open in Maps" button that launches Google Maps
 * with a geo: URI for the post coordinates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricePostDetailsScreen(
    postId: Long,
    onBack: () -> Unit,
    viewModel: MyStoresViewModel = hiltViewModel()
) {
    val posts by viewModel.pins.collectAsState()
    val post = posts.firstOrNull { it.id == postId }

    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<Location?>(null) }

    // Grab last known location (best effort)
    LaunchedEffect(Unit) {
        try {
            fusedClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) userLocation = loc
            }
        } catch (_: SecurityException) {
            // ignore
        }
    }

    if (post == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Price details") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { inner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) {
                Text("Post not found")
            }
        }
        return
    }

    val formattedDate = remember(post.createdAt) {
        val date = Date(post.createdAt)
        DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT
        ).format(date)
    }

    val categoryLabel = post.category?.ifBlank { null } ?: "Uncategorized"
    val distanceText = remember(userLocation, post.lat, post.lng) {
        detailsDistanceText(userLocation, post)
    }
    val postedBy = post.postedBy ?: "Anonymous"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Price details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero image
            if (!post.photoUri.isNullOrEmpty()) {
                AsyncImage(
                    model = post.photoUri,
                    contentDescription = "Photo of ${post.itemName}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No photo", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Main info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = post.storeName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = post.itemName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                // Price + category chips (restaurant/grocery-friendly)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AssistChip(
                        onClick = { /* no-op */ },
                        label = {
                            Text(
                                "$${"%.2f".format(post.price)}",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    AssistChip(
                        onClick = { /* no-op */ },
                        label = { Text(categoryLabel) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Posted by + date + distance
                Text(
                    text = "Posted by $postedBy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Posted on $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (distanceText != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // Location + open in Maps
            val ctx = LocalContext.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Lat: ${"%.5f".format(post.lat)},  Lng: ${"%.5f".format(post.lng)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Use this pin to remember where you found this item or dish.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                androidx.compose.material3.Button(
                    onClick = {
                        val encodedLabel = URLEncoder.encode(
                            post.storeName,
                            StandardCharsets.UTF_8.toString()
                        )
                        val uri = Uri.parse(
                            "geo:${post.lat},${post.lng}?q=${post.lat},${post.lng}($encodedLabel)"
                        )
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        ctx.startActivity(intent)
                    }
                ) {
                    Text("Open in Maps")
                }
            }

            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            // “Why this post matters” – restaurant / grocery flavour text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Why this post matters",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = when (categoryLabel.lowercase()) {
                        "restaurant", "fast food" ->
                            "Great for tracking popular dishes and their prices across your favourite spots."
                        "cafe", "bakery" ->
                            "Perfect for remembering pastries, drinks, and treats worth coming back for."
                        "grocery" ->
                            "Helps you compare grocery prices across different stores so you can save on staples."
                        else ->
                            "Use this as a bookmark for any place or item you don’t want to forget."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Compute a human-readable distance string for the details screen.
 *
 * Uses the user's last known location and the post's coordinates.
 */
private fun detailsDistanceText(
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
