package com.example.ticititacititoe.chat

import com.google.firebase.Timestamp

data class ChatRoom(
    val roomId: String ="",
    val userIds: List<String?> = emptyList(),
    val timestamp: Timestamp  = Timestamp.now(),
)
