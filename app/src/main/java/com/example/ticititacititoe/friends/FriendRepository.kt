package com.example.ticititacititoe.friends

import com.example.ticititacititoe.profile.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FriendRepository {
    private val db = Firebase.firestore

    suspend fun addFriend(currentUserId: String, friendId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .document(friendId)
            .set(mapOf("addedAt" to System.currentTimeMillis()))
            .await()
    }

    suspend fun deleteFriend(currentUserId: String, friendId: String) {
        db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .document(friendId)
            .delete()
            .await()
    }


    fun listenToFriends(currentUserId: String): Flow<List<User>> = callbackFlow {
        val subscription = db.collection("users")
            .document(currentUserId)
            .collection("friends")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val friendList = snapshots?.documents?.mapNotNull { doc ->
                    User(
                        id = doc.id,
                        username = doc.getString("username") ?: ""
                    )
                } ?: emptyList()

                trySend(friendList)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun getFriendProfiles(currentUserId: String): List<User> {
        val snapshot = db.collection("users").document(currentUserId)
            .collection("friends").get().await()

        val friendIds = snapshot.documents.map { it.id }
        if (friendIds.isEmpty()) return emptyList()

        val userSnapshots = db.collection("users")
            .whereIn(FieldPath.documentId(), friendIds)
            .get().await()
        return userSnapshots.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id)
        }
    }
}