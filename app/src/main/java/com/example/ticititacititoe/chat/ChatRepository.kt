package com.example.ticititacititoe.chat

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    suspend fun listenToChat(roomId: String): Flow<List<Message>> = callbackFlow {
       val listener =  db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.toObjects(Message::class.java)
                    ?: emptyList()

                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createChatRoom(
        gameId: String,
        userIds: List<String>,
                       ) {

       val chatRoomRef = db.collection("chatRooms")
           .document(gameId)

       val snapshot =  chatRoomRef
            .get()
            .await()

        if (!snapshot.exists()) {
            val chatRoomData = ChatRoom(
                roomId = gameId,
                userIds = userIds,
                timestamp = Timestamp.now(),
            )

            chatRoomRef.set(chatRoomData).await()
        }
    }

    suspend fun sendMessage(roomId: String, message: String, currentUserId: String) {
        val message = Message(
            roomId = roomId,
            message = message,
            createdAt = Timestamp.now(),
            senderId = currentUserId
        )
        db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .add(message)
            .await()
    }

}