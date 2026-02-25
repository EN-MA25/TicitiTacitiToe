package com.example.ticititacititoe.friends

import android.util.Log
import com.example.ticititacititoe.profile.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.text.get
import kotlin.toString

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
}