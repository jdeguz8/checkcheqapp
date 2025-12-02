package com.jdeguzman.checkcheqapp.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jdeguzman.checkcheqapp.data.repository.PricePostRepository
import com.jdeguzman.checkcheqapp.domain.PricePost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth


@HiltViewModel
class MyStoresViewModel @Inject constructor(
    private val repo: PricePostRepository
) : ViewModel() {

    // --- Firebase instances ---
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance("gs://checkcheq-demo.firebasestorage.app")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // UI state for the "add pin" dialog
    data class DialogUi(
        val showAddDialog: Boolean = false,
        val lat: Double? = null,
        val lng: Double? = null,
        val suggestedStoreName: String? = null
    )


    private val _dialogUi = MutableStateFlow(DialogUi())
    val dialogUi: StateFlow<DialogUi> = _dialogUi.asStateFlow()

    // Posts coming from Room via the repository
    val pins: StateFlow<List<PricePost>> =
        repo.observePosts()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun onMapLongClick(lat: Double, lng: Double) {
        _dialogUi.value = DialogUi(
            showAddDialog = true,
            lat = lat,
            lng = lng,
            suggestedStoreName = null
        )
    }

    fun onPlaceSelected(
        lat: Double,
        lng: Double,
        storeName: String?
    ) {
        _dialogUi.value = DialogUi(
            showAddDialog = true,
            lat = lat,
            lng = lng,
            suggestedStoreName = storeName
        )
    }

    fun onDismissDialog() {
        _dialogUi.value = DialogUi()
    }

    fun onAddPin(
        storeName: String,
        itemName: String,
        price: Double,
        photoUri: String?,
        category: String?      // 👈 now passed from dialog
    ) {
        val lat = _dialogUi.value.lat ?: return
        val lng = _dialogUi.value.lng ?: return

        // --- build "postedBy" from Firebase user ---
        val user = auth.currentUser
        val postedBy = user?.displayName
            ?: user?.email
            ?: "Anonymous"

        val post = PricePost(
            id = 0L,
            storeName = storeName.trim(),
            itemName = itemName.trim(),
            price = price,
            lat = lat,
            lng = lng,
            photoUri = photoUri, // local URI for UI / Room
            createdAt = System.currentTimeMillis(),
            category = category,
            postedBy = postedBy   // 👈 NEW
        )

        viewModelScope.launch {
            // 1) Save to Room (offline + feed UI)
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
                }
                .addOnFailureListener { e ->
                    Log.e("CheckCheq", "Failed to save post to Firestore", e)
                }

            _dialogUi.value = DialogUi()
        }
    }

    fun clearAllPosts() {
        viewModelScope.launch {
            repo.clear()
        }
    }

    // --- helper to upload to Firebase Storage ---
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