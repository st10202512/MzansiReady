package com.mzansiready.app.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mzansiready.app.data.model.FeedPost
import kotlinx.coroutines.tasks.await

class FeedRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val collection = db.collection("community_feed")

    // Real-time listener for the feed
    fun getFeedUpdates(onUpdate: (List<FeedPost>) -> Unit) {
        collection.orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FeedPost::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                onUpdate(posts)
            }
    }

    // Post a new message to the feed
    suspend fun postMessage(message: String): Result<Unit> {
        return try {
            val user = auth.currentUser
            if (user == null) {
                return Result.failure(Exception("User not logged in"))
            }

            val post = FeedPost(
                userId = user.uid,
                authorName = user.displayName ?: "Anonymous",
                message = message
            )

            collection.add(post).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
