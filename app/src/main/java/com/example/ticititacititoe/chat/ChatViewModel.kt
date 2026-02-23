package com.example.ticititacititoe.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class ChatViewModel: ViewModel() {
    private val repository = ChatRepository()
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()
    fun createChatRoom(
        gameId: String,
        userIds: MutableList<String?>
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

    fun listenToChat(roomId: String) {
        viewModelScope.launch {

            repository.listenToChat(roomId)
                .distinctUntilChanged()
                .catch {
                    _messages.value = emptyList()
                }
                .collect { messages ->
                    _messages.value = messages

                }
        }
    }





}