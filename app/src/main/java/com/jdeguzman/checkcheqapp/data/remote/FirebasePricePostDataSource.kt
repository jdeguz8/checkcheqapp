package com.jdeguzman.checkcheqapp.data.remote

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jdeguzman.checkcheqapp.domain.PricePost
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source for creating and uploading price posts to Firebase.
 *
 * Handles:
 * - Uploading images to Firebase Storage
 * - Writing post metadata to Firestore
 */
@Singleton
class FirebasePricePostDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    private val postsCollection = firestore.collection("price_posts")

    /**
     * Upload an image to Firebase Storage if the given [localUri] is non-null.
     *
     * @param localUri a string representation of a local [Uri], or `null`.
     * @return the download URL as a string, or `null` if no upload occurred.
     */
    suspend fun uploadPhotoIfNeeded(localUri: String?): String? {
        if (localUri.isNullOrEmpty()) return null

        val fileUri = Uri.parse(localUri)
        val fileName = "images/${System.currentTimeMillis()}_${fileUri.lastPathSegment}"
        val ref = storage.reference.child(fileName)

        // Upload file
        ref.putFile(fileUri).await()
        // Get download URL
        return ref.downloadUrl.await().toString()
    }

    /**
     * Add a post document to Firestore using the given [post] and image URL.
     *
     * @param post domain-level price post.
     * @param remotePhotoUrl optional remote image URL to store as `photoUrl`.
     * @return the generated Firestore document ID.
     */
    suspend fun addPostRemote(post: PricePost, remotePhotoUrl: String?): String {
        val docData = mapOf(
            "storeName" to post.storeName,
            "itemName" to post.itemName,
            "price" to post.price,
            "lat" to post.lat,
            "lng" to post.lng,
            "photoUrl" to remotePhotoUrl,
            "createdAt" to post.createdAt,
            "category" to post.category,
            "postedBy" to post.postedBy
        )

        val docRef = postsCollection.add(docData).await()
        return docRef.id
    }
}
