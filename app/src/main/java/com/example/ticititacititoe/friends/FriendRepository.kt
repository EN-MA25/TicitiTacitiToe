package com.example.ticititacititoe.friends

import com.example.ticititacititoe.profile.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
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
}