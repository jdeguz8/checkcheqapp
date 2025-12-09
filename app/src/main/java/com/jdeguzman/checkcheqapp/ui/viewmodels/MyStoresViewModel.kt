package com.jdeguzman.checkcheqapp.ui.viewmodels

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

import com.google.firebase.firestore.DocumentChange
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow


/**
 * ViewModel for the map and shared price posts.
 *
 * Responsibilities:
 * - Track map long-press / place selection and dialog UI state
 * - Observe Firestore `price_posts` in real time and expose them as [pins]
 * - Build and save new posts (Room + Firestore + Storage upload)
 * - Attach the current Firebase user as the `postedBy` and `ownerUid` metadata
 */
@HiltViewModel
class MyStoresViewModel @Inject constructor(
    private val repo: PricePostRepository
) : ViewModel() {


    // ---- retention policy ----
    private val retentionDays = 7L
    private val retentionMillis = retentionDays * 24L * 60L * 60L * 1000L

    // Keep track of which posts we’ve already notified the UI about
    private val notifiedPostIds = mutableSetOf<Long>()

    // One-shot events for "a new post was added"
    private val _newPostEvents = MutableSharedFlow<PricePost>(
        extraBufferCapacity = 16
    )
    val newPostEvents: SharedFlow<PricePost> = _newPostEvents.asSharedFlow()


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
        cleanupOldLocalPosts()
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

                val now = System.currentTimeMillis()
                val cutoff = now - retentionMillis

                // 1) Map the full snapshot into the list used by the UI
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
                        val ownerUid = doc.getString("userId")

                        // 🔹 Skip posts older than retention window
                        if (createdAt < cutoff) {
                            return@mapNotNull null
                        }

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
                            postedBy = postedBy,
                            ownerUid = ownerUid
                        )
                    } catch (ex: Exception) {
                        Log.e("CheckCheq", "Failed to map Firestore doc ${doc.id}", ex)
                        null
                    }
                }

                _pins.value = posts

                // 2) Look only at newly ADDED docs for notification purposes
                snapshot.documentChanges
                    .filter { change -> change.type == DocumentChange.Type.ADDED }
                    .forEach { change ->
                        val doc = change.document
                        try {
                            val storeName = doc.getString("storeName") ?: return@forEach
                            val itemName = doc.getString("itemName") ?: ""
                            val price = doc.getDouble("price") ?: 0.0
                            val lat = doc.getDouble("lat") ?: return@forEach
                            val lng = doc.getDouble("lng") ?: return@forEach
                            val photoUrl = doc.getString("photoUrl")
                            val createdAt = doc.getLong("createdAt") ?: 0L
                            val category = doc.getString("category")
                            val postedBy = doc.getString("postedBy")
                            val ownerUid = doc.getString("userId")

                            // Respect retention window here too
                            if (createdAt < cutoff) return@forEach

                            val newPost = PricePost(
                                id = createdAt,
                                storeName = storeName,
                                itemName = itemName,
                                price = price,
                                lat = lat,
                                lng = lng,
                                photoUri = photoUrl,
                                createdAt = createdAt,
                                category = category,
                                postedBy = postedBy,
                                ownerUid = ownerUid
                            )

                            // 🔔 Only emit once per post ID
                            if (notifiedPostIds.add(newPost.id)) {
                                viewModelScope.launch {
                                    _newPostEvents.emit(newPost)
                                }
                            }
                        } catch (ex: Exception) {
                            Log.e("CheckCheq", "Failed to map added Firestore doc ${doc.id}", ex)
                        }
                    }
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

        // --- build "postedBy" + ownerUid from Firebase user ---
        val user = auth.currentUser
        val postedBy = user?.displayName
            ?: user?.email
            ?: "Anonymous"
        val ownerUid = user?.uid

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
            postedBy = postedBy,
            ownerUid = ownerUid     // 🔹 make sure ownerUid is set
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
                "userId" to ownerUid                 // 🔹 keep this in sync with ownerUid
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
     * Full delete of all posts from local Room cache.
     * Firestore posts remain untouched.
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
     * Update an existing Firestore post's basic fields.
     *
     * - Looks up the Firestore document by [postId] (which is `createdAt`)
     * - Updates storeName, itemName, price, and category
     */
    fun updatePost(
        postId: Long,
        storeName: String,
        itemName: String,
        price: Double,
        category: String?
    ) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("price_posts")
                    .whereEqualTo("createdAt", postId)
                    .limit(1)
                    .get()
                    .await()

                val doc = snapshot.documents.firstOrNull() ?: run {
                    Log.w("CheckCheq", "updatePost: no document for createdAt=$postId")
                    return@launch
                }

                val updates = mapOf(
                    "storeName" to storeName.trim(),
                    "itemName" to itemName.trim(),
                    "price" to price,
                    "category" to category
                )

                firestore.collection("price_posts")
                    .document(doc.id)
                    .update(updates)
                    .await()

                Log.d("CheckCheq", "Updated Firestore post for createdAt=$postId")
            } catch (e: Exception) {
                Log.e("CheckCheq", "Failed to update Firestore post", e)
            }
        }
    }

    /**
     * Delete a post from Firestore by [postId] (which is `createdAt`).
     *
     * Local Room copy will remain unless you also clear it via [clearAllPosts] or add
     * repo-level delete support.
     */
    fun deletePost(postId: Long) {
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("price_posts")
                    .whereEqualTo("createdAt", postId)
                    .limit(1)
                    .get()
                    .await()

                val doc = snapshot.documents.firstOrNull() ?: run {
                    Log.w("CheckCheq", "deletePost: no document for createdAt=$postId")
                    return@launch
                }

                firestore.collection("price_posts")
                    .document(doc.id)
                    .delete()
                    .await()

                Log.d("CheckCheq", "Deleted Firestore post for createdAt=$postId")
            } catch (e: Exception) {
                Log.e("CheckCheq", "Failed to delete Firestore post", e)
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

    /**
     * Best-effort cleanup of old posts from local Room storage.
     *
     * Runs once when the ViewModel is created. This keeps the local DB
     * from growing forever even if Firestore still contains older docs.
     */
    private fun cleanupOldLocalPosts() {
        viewModelScope.launch {
            try {
                val cutoff = System.currentTimeMillis() - retentionMillis
                repo.deleteOlderThan(cutoff)
            } catch (e: Exception) {
                Log.e("CheckCheq", "Failed to cleanup old local posts", e)
            }
        }
    }
}