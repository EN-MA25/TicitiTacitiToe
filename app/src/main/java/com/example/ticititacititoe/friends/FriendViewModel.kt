package com.example.ticititacititoe.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticititacititoe.game.invitations.GameInvitationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FriendViewModel: ViewModel() {
    private val repository = FriendRepository()
    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents = _errorEvents.asSharedFlow()

    fun addFriend(currentUserId: String, friendId: String) {
        viewModelScope.launch {
            try {
                repository.addFriend(currentUserId, friendId)
            } catch (e: Exception){
                _errorEvents.emit("Failed to add friend: $friendId, try again!")

            }
        }
    }


    fun deleteFriend(currentUserId: String, friendId: String) {
        viewModelScope.launch {
            try {
                repository.deleteFriend(currentUserId, friendId)

            } catch (e: Exception) {
                _errorEvents.emit("Failed to delete friend: $friendId, try again!")
            }
        }
    }


}