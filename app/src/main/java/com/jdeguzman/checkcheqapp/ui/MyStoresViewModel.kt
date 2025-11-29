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

@HiltViewModel
class MyStoresViewModel @Inject constructor(
    private val repo: PricePostRepository
) : ViewModel() {

    // --- Firebase instances ---
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance("gs://checkcheq-demo.firebasestorage.app")

    // UI state for the "add pin" dialog
    data class DialogUi(
        val showAddDialog: Boolean = false,
        val lat: Double? = null,
        val lng: Double? = null
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
            lng = lng
        )
    }

    fun onDismissDialog() {
        _dialogUi.value = DialogUi()
    }

    fun onAddPin(
        storeName: String,
        itemName: String,
        price: Double,
        photoUri: String?
    ) {
        val lat = _dialogUi.value.lat ?: return
        val lng = _dialogUi.value.lng ?: return

        val basePost = PricePost(
            id = 0L,
            storeName = storeName.trim(),
            itemName = itemName.trim(),
            price = price,
            lat = lat,
            lng = lng,
            photoUri = photoUri, // local URI initially
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            // 1) Save to Room, get generated ID
            val newId = try {
                repo.add(basePost)
            } catch (e: Exception) {
                Log.e("CheckCheq", "Room insert failed", e)
                -1L
            }

            // 2) Upload photo to Firebase Storage (if there is one)
            val remotePhotoUrl = try {
                uploadPhotoToFirebase(photoUri)
            } catch (e: Exception) {
                Log.e("CheckCheq", "Photo upload failed", e)
                null
            }

            // 3) Save Firestore doc with cloud photo URL (or fallback)
            val finalPhotoUrl = remotePhotoUrl ?: basePost.photoUri

            val data = mapOf(
                "storeName" to basePost.storeName,
                "itemName" to basePost.itemName,
                "price" to basePost.price,
                "lat" to basePost.lat,
                "lng" to basePost.lng,
                "photoUrl" to finalPhotoUrl,
                "createdAt" to basePost.createdAt
            )

            firestore.collection("price_posts")
                .add(data)
                .addOnSuccessListener { ref ->
                    Log.d("CheckCheq", "Saved post to Firestore as ${ref.id}")
                }
                .addOnFailureListener { e ->
                    Log.e("CheckCheq", "Failed to save post to Firestore", e)
                }

            // 4) If we have a remote URL and a valid Room row, update local row too
            if (remotePhotoUrl != null && newId > 0L) {
                val updatedPost = basePost.copy(
                    id = newId,
                    photoUri = remotePhotoUrl
                )
                try {
                    repo.add(updatedPost)   // REPLACE same row with cloud URL
                    Log.d("CheckCheq", "Updated local post with remote photo URL")
                } catch (e: Exception) {
                    Log.e("CheckCheq", "Failed to update local post with remote URL", e)
                }
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
