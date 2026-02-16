package com.example.ticititacititoe.game

import com.example.ticititacititoe.game.ui.QueueUiState
import com.example.ticititacititoe.profile.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

import kotlin.collections.emptyList

class GameRepository {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()


    // Speaking to Firebase to fetch game info

    fun observeQueueSize(): Flow<Int> = callbackFlow {

        val listener = db.collection("gameQueue")
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) {
                    trySend(0)
                    return@addSnapshotListener
                }

                trySend(snapshot.size())
            }

        awaitClose { listener.remove() }
    }
    suspend fun addToQueue(userId: String, username: String) {

        val data = mapOf(
            "userId" to userId,
            "status" to "pending",
            "username" to username
        )
        db.collection("gameQueue")
            .document(userId)
            .set(data)
            .await()
    }

    suspend fun deleteFromQueue(userId: String) {
        db.collection("gameQueue")
            .document(userId)
            .delete()
            .await()
    }
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
            "fromUserId" to fromUserId,
            "toUsername" to toUsername,
            "status" to "pending"
        )

        batch.set(outgoingRef, outgoingData)

        batch.commit().await()
    }




    suspend fun loadOutgoingGameInvitations(currentUserId: String): Flow<List<GameInvitation>> =
        callbackFlow {

            val listener = db.collection("users")
                .document(currentUserId)
                .collection("outgoingGameInvitation")
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

    fun listenToInvite(currentUserId: String, otherUserId: String): Pair<StateFlow<InviteState>, ListenerRegistration> {
        val stateFlow = MutableStateFlow<InviteState>(InviteState.Idle)

        val registration = db.collection("users")
            .document(currentUserId)
            .collection("outgoingGameInvitation")
            .document(otherUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                when (snapshot.getString("status")) {
                    "pending" -> stateFlow.value = InviteState.Pending
                    "accepted" -> stateFlow.value = InviteState.Accepted
                    "declined" -> stateFlow.value = InviteState.Declined
                }
            }

        return stateFlow to registration
    }

    fun acceptInvitation(currentUserId: String,
                         otherUserId: String) {

//        db.collection("gameInvitations")
//            .document(inviteId)
//            .update("status", "Accepted")

        db.collection("users")
            .document(currentUserId)
            .collection("gameInvitations")
            .document(otherUserId)
            .update("status", "accepted")

        db.collection("users")
            .document(otherUserId)
            .collection("outgoingGameInvitation")
            .document(currentUserId)
            .update("status", "accepted")
    }

    suspend fun deleteInvitations(currentUserId: String,
                          otherUserId: String, deleteBothInvitations: Boolean = false) {
        val batch = db.batch()

        // =========== delete invitations ==========
        if (deleteBothInvitations) {
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
        } else {

            if (auth.currentUser?.uid == currentUserId) {
                batch.delete(
                    db.collection("users")
                        .document(currentUserId)
                        .collection("gameInvitations")
                        .document(otherUserId)
                )
            } else {
                batch.delete(
                    db.collection("users")
                        .document(otherUserId)
                        .collection("outgoingGameInvitation")
                        .document(currentUserId)
                )
            }
        }
        batch.commit().await()
    }



}