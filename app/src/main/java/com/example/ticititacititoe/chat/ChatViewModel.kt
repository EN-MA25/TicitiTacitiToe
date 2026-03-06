package com.example.ticititacititoe.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticititacititoe.chat.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ChatViewModel: ViewModel() {
    private val repository = ChatRepository()
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents = _errorEvents.asSharedFlow()
    fun createChatRoom(
        gameId: String,
        userIds: MutableList<String?>
    ) {
        viewModelScope.launch {
            try {
                repository.createChatRoom(gameId, userIds)
            } catch (e: Exception) {
                _errorEvents.emit("Failed to create chat. restart game!: ${e.message}")
            }
        }

    }

    fun sendMessage(roomId: String, message: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                repository.sendMessage(roomId, message, currentUserId)
            } catch (e: Exception) {
                _errorEvents.emit("Unable to send message. Try again!: ${e.message}")
            }
        }
    }

    fun listenToChat(roomId: String) {
        viewModelScope.launch {
            try {
                repository.listenToChat(roomId)
                    .distinctUntilChanged()
                    .catch {
                        _messages.value = emptyList()
                    }
                    .collect { messages ->
                        _messages.value = messages
                    }
            } catch (e: Exception) {
                _errorEvents.emit("Failed to get chat messages!: ${e.message}")
            }
        }
    }


    fun deleteChat(gameId: String) {
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            try {
                repository.deleteChat(gameId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _errorEvents.emit("Failed to delete chat: $gameId, try again: ${e.message}")
            }
        }
    }

}