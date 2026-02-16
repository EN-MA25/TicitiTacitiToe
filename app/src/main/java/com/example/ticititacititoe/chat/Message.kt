package com.example.ticititacititoe.chat

import com.google.firebase.Timestamp

data class Message(
    val id: String = "",
    val senderId: String = "",
    val message: String = "",
    val roomId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
)