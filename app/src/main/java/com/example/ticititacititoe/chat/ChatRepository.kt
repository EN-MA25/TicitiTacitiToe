package com.example.ticititacititoe.chat

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()


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

    suspend fun sendMessage(roomId: String, message: String) {
        
    }

}