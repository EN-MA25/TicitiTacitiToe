package com.example.ticititacititoe.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MultiplayerGameViewModel: ViewModel() {
    private val repository = GameRepository()

    private val _incomingInvites = MutableStateFlow<List<GameInvitation>>(emptyList())
    val incomingInvites = _incomingInvites.asStateFlow()

    private val shownInvites = mutableSetOf<String>()

    fun startListeningForInvites(userId: String) {
        viewModelScope.launch {
            repository.loadIncomingGameInvitations(userId)
                .collect { invites ->
                    _incomingInvites.value = invites
                }
        }
    }


    fun sendGameInvitation(
        fromUserId: String?,
        fromUserName: String,
        toUserId: String,
        toUserName: String
    ) {
        if (fromUserId != null) {
            viewModelScope.launch {
                try {
                    repository.sendGameInvite(fromUserId, fromUserName, toUserId, toUserName)
                } catch (exception: Exception) {
                    Log.e("Invite", "Failed to send invite", exception)
                }
            }
        }
    }

    fun deleteInvitations(currentUserId: String,
                          otherUserId: String) {
        viewModelScope.launch {
            try {
                repository.deleteInvitations(currentUserId, otherUserId)
            } catch (exception: Exception) {
                Log.e("Invite", "Failed to delete invite", exception)

            }
        }
    }
}