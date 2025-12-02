package com.jdeguzman.checkcheqapp.data.remote

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.jdeguzman.checkcheqapp.domain.PricePost
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePricePostDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    private val postsCollection = firestore.collection("price_posts")

    suspend fun uploadPhotoIfNeeded(localUri: String?): String? {
        if (localUri.isNullOrEmpty()) return null

        val fileUri = Uri.parse(localUri)
        val fileName = "images/${System.currentTimeMillis()}_${fileUri.lastPathSegment}"
        val ref = storage.reference.child(fileName)

        // Upload file
        val uploadTask = ref.putFile(fileUri).await()
        // Get download URL
        return ref.downloadUrl.await().toString()
    }

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
