package com.jdeguzman.checkcheqapp.ui

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.jdeguzman.checkcheqapp.data.repository.PricePostRepository
import com.jdeguzman.checkcheqapp.domain.PricePost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * ViewModel for the map and shared price posts.
 *
 * Responsibilities:
 * - Track map long-press / place selection and dialog UI state
 * - Observe Firestore `price_posts` in real time and expose them as [pins]
 * - Build and save new posts (Room + Firestore + Storage upload)
 * - Attach the current Firebase user as the `postedBy` metadata
 */
@HiltViewModel
class MyStoresViewModel @Inject constructor(
    private val repo: PricePostRepository
) : ViewModel() {

    // --- Firebase instances ---
    private val firestore = FirebaseFirestore.getInstance()
    private val storage =
        FirebaseStorage.getInstance("gs://checkcheq-demo.firebasestorage.app")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Public UI state for the Add Price dialog.
     */
    data class DialogUi(
        val showAddDialog: Boolean = false,
        val lat: Double? = null,
        val lng: Double? = null,
        val suggestedStoreName: String? = null
    )

    private val _dialogUi = MutableStateFlow(DialogUi())
    val dialogUi: StateFlow<DialogUi> = _dialogUi.asStateFlow()

    // --- Pins now come from Firestore ---
    private val _pins = MutableStateFlow<List<PricePost>>(emptyList())
    val pins: StateFlow<List<PricePost>> = _pins.asStateFlow()

    init {
        observeRemotePosts()
    }

    /**
     * Observe remote posts from Firestore and update [pins].
     */
    private fun observeRemotePosts() {
        firestore.collection("price_posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("CheckCheq", "Firestore listener error", e)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val posts = snapshot.documents.mapNotNull { doc ->
                    try {
                        val storeName = doc.getString("storeName") ?: return@mapNotNull null
                        val itemName = doc.getString("itemName") ?: ""
                        val price = doc.getDouble("price") ?: 0.0
                        val lat = doc.getDouble("lat") ?: return@mapNotNull null
                        val lng = doc.getDouble("lng") ?: return@mapNotNull null
                        val photoUrl = doc.getString("photoUrl")
                        val createdAt = doc.getLong("createdAt") ?: 0L
                        val category = doc.getString("category")
                        val postedBy = doc.getString("postedBy")

                        // Use createdAt as a stable id so details screen can find it
                        PricePost(
                            id = createdAt,
                            storeName = storeName,
                            itemName = itemName,
                            price = price,
                            lat = lat,
                            lng = lng,
                            photoUri = photoUrl,
                            createdAt = createdAt,
                            category = category,
                            postedBy = postedBy
                        )
                    } catch (ex: Exception) {
                        Log.e("CheckCheq", "Failed to map Firestore doc ${doc.id}", ex)
                        null
                    }
                }

                _pins.value = posts
            }
    }

    /**
     * Called when the user long-presses on the map.
     *
     * Stores the coordinates and shows the Add Price dialog.
     */
    fun onMapLongClick(lat: Double, lng: Double) {
        _dialogUi.value = DialogUi(
            showAddDialog = true,
            lat = lat,
            lng = lng,
            suggestedStoreName = null
        )
    }

    /**
     * Called when the user selects a place from Places autocomplete.
     *
     * Stores the place coordinates and suggested store name,
     * then shows the Add Price dialog.
     */
    fun onPlaceSelected(lat: Double, lng: Double, storeName: String?) {
        _dialogUi.value = DialogUi(
            showAddDialog = true,
            lat = lat,
            lng = lng,
            suggestedStoreName = storeName
        )
    }

    /**
     * Hide the Add Price dialog and clear any stored coordinates.
     */
    fun onDismissDialog() {
        _dialogUi.value = DialogUi()
    }

    fun onAddPin(
        storeName: String,
        itemName: String,
        price: Double,
        photoUri: String?,
        category: String?
    ) {
        val lat = _dialogUi.value.lat ?: return
        val lng = _dialogUi.value.lng ?: return

        // --- build "postedBy" from Firebase user ---
        val user = auth.currentUser
        val postedBy = user?.displayName
            ?: user?.email
            ?: "Anonymous"

        val createdAt = System.currentTimeMillis()

        val post = PricePost(
            id = createdAt,   // we’ll use createdAt as id for Firestore-backed posts
            storeName = storeName.trim(),
            itemName = itemName.trim(),
            price = price,
            lat = lat,
            lng = lng,
            photoUri = photoUri, // local URI (only used briefly; Firestore will have remote URL)
            createdAt = createdAt,
            category = category,
            postedBy = postedBy
        )

        viewModelScope.launch {
            // 1) Save to Room (optional offline cache)
            try {
                repo.add(post)
            } catch (e: Exception) {
                Log.e("CheckCheq", "Room insert failed", e)
            }

            // 2) Upload photo to Firebase Storage (if there is one)
            val remotePhotoUrl = try {
                uploadPhotoToFirebase(photoUri)
            } catch (e: Exception) {
                Log.e("CheckCheq", "Photo upload failed", e)
                null
            }

            // 3) Save Firestore doc with cloud photo URL + metadata
            val data = mapOf(
                "storeName" to post.storeName,
                "itemName" to post.itemName,
                "price" to post.price,
                "lat" to post.lat,
                "lng" to post.lng,
                "photoUrl" to (remotePhotoUrl ?: post.photoUri),
                "createdAt" to post.createdAt,
                "category" to post.category,
                "postedBy" to post.postedBy,
                "userId" to user?.uid
            )

            firestore.collection("price_posts")
                .add(data)
                .addOnSuccessListener { ref ->
                    Log.d("CheckCheq", "Saved post to Firestore as ${ref.id}")
                    // Listener in observeRemotePosts() will update _pins for everyone
                }
                .addOnFailureListener { e ->
                    Log.e("CheckCheq", "Failed to save post to Firestore", e)
                }

            _dialogUi.value = DialogUi()
        }
    }

    /**
     * Hide the Add Price dialog and clear any stored coordinates.
     */
    fun clearAllPosts() {
        viewModelScope.launch {
            try {
                repo.clear()
            } catch (e: Exception) {
                Log.e("CheckCheq", "Failed to clear Room posts", e)
            }
        }
    }

    /**
     * Upload a photo to Firebase Storage and return its download URL.
     *
     * @param localUri string representation of the local content URI
     * @return public download URL for the uploaded file, or null on failure
     */
    private suspend fun uploadPhotoToFirebase(localUri: String?): String? {
        if (localUri.isNullOrEmpty()) return null

        return try {
            val fileUri = localUri.toUri()
            val fileName =
                "images/${System.currentTimeMillis()}_${fileUri.lastPathSegment ?: "photo"}"
            val ref = storage.reference.child(fileName)

            // Upload file
            ref.putFile(fileUri).await()

            // Get download URL
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("CheckCheq", "uploadPhotoToFirebase() failed", e)
            null
        }
    }
}
