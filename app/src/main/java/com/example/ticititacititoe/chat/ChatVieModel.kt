package com.example.ticititacititoe.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ChatVieModel: ViewModel() {
    private val repository = ChatRepository()

    fun createChatRoom(
        gameId: String,
        userIds: List<String>
    ) {
        viewModelScope.launch {
            try {
                repository.createChatRoom(gameId, userIds)
            } catch (exception: Exception) {
                // Unable to create chat
            }
        }

    }

    fun sendMessage(roomId: String, message: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                repository.sendMessage(roomId, message, currentUserId)
            } catch (exception: Exception) {
                // Unable to send message 
            }
        }
    }
}