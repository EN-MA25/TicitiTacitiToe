package com.example.ticititacititoe.chat

import com.example.ticititacititoe.chat.model.ChatRoom
import com.example.ticititacititoe.chat.model.Message
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    fun listenToChat(roomId: String): Flow<List<Message>> = callbackFlow {
       val listener =  db.collection("chatRooms")
            .document(roomId)
            .collection("messages")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createChatRoom(
        gameId: String,
        userIds: MutableList<String?>,){
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

    suspend fun deleteChat(gameId: String) {
        val chatRoomRef = db.collection("chatRooms").document(gameId)
        val messageRef = chatRoomRef.collection("messages")

        // ======== get all messages =========
        val messagesSnapshot = messageRef.get().await()


        val batch = db.batch()

        // ======== Loop throguh every message and add to delete in batch =========
        for (document in messagesSnapshot.documents) {
            batch.delete(document.reference)
        }

        // ======== Delete chatroom =========
        batch.delete(db.collection("chatRooms")
            .document(gameId))

        // ======== Do everything simultaneously =========
        batch.commit().await()
    }



}