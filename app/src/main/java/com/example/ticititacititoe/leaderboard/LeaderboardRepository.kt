package com.example.ticititacititoe.leaderboard

import com.example.ticititacititoe.profile.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class LeaderboardRepository {
    private val db = Firebase.firestore

    suspend fun getAllUsers(): List<User>{
        val snapshot = db.collection("users")
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(id = doc.id)
        }

    }


    suspend fun getRating(userId: String): Double? {
        return try {
            val doc = db.collection("users")
                .document(userId)
                .get()
                .await()
            doc.getDouble("rating")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getFriendsForLeaderboard(currentUserId: String): List<User> {
        return try {
            val friendsSnapshot = db.collection("users")
                .document(currentUserId)
                .collection("friends")
                .get()
                .await()

            val friendIds = friendsSnapshot.documents.map { it.id }
            if (friendIds.isEmpty()) return emptyList()

            val users = friendIds.chunked(30).flatMap { chunk ->
                db.collection("users")
                    .whereIn(FieldPath.documentId(), chunk)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { doc ->
                        doc.toObject(User::class.java)?.copy(id = doc.id)
                    }
            }

            users
        } catch (e: Exception) {
            emptyList()
        }
    }







}
