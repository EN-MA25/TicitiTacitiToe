package com.example.ticititacititoe.game

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

import kotlin.collections.emptyList

class GameRepository {
    private val db = Firebase.firestore


    // Speaking to Firebase to fetch game info

    suspend fun sendGameInvite(fromUserId: String,
                       fromUsername: String,
                       toUserId: String,
                       toUsername: String) {

        val batch = db.batch()

        val requestRef = db.collection("users")
            .document(toUserId)
            .collection("gameInvitations")
            .document(fromUserId)

        val data = mapOf(
            "fromUserId" to fromUserId,
            "fromUsername" to fromUsername,
            "status" to "pending"
        )

        batch.set(requestRef, data)

        val outgoingRef = db.collection("users")
            .document(fromUserId)
            .collection("outgoingGameInvitation")
            .document(toUserId)

        val outgoingData = mapOf(
            "toUserId" to toUserId,
            "toUsername" to toUsername,
            "status" to "pending"
        )

        batch.set(outgoingRef, outgoingData)

        batch.commit().await()
    }

    suspend fun loadIncomingGameInvitations(currentUserId: String): Flow<List<GameInvitation>> =
        callbackFlow {

            val listener = db.collection("users")
                .document(currentUserId)
                .collection("gameInvitations")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val invites = snapshot.documents.mapNotNull {
                        it.toObject(GameInvitation::class.java)?.copy(id = it.id)
                    }

                    trySend(invites)
                }
            awaitClose { listener.remove() }

        }

    suspend fun deleteInvitations(currentUserId: String,
                          otherUserId: String) {
        val batch = db.batch()

        batch.delete(
            db.collection("users")
                .document(currentUserId)
                .collection("gameInvitations")
                .document(otherUserId)
        )

        batch.delete(
            db.collection("users")
                .document(otherUserId)
                .collection("outgoingGameInvitation")
                .document(currentUserId)
        )

        batch.commit().await()
    }

}