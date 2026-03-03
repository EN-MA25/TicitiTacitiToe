package com.example.ticititacititoe.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticititacititoe.profile.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FriendViewModel: ViewModel() {
    private val repository = FriendRepository()

    private val _friends = MutableStateFlow<List<User>>(emptyList())
    val friends = _friends.asStateFlow()

    private val _friendIds = MutableStateFlow<Set<String>>(emptySet())


    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents = _errorEvents.asSharedFlow()


    fun addFriend(currentUserId: String, friendId: String) {
        viewModelScope.launch {
            try {
                repository.addFriend(currentUserId, friendId)
            } catch (e: Exception){
                _errorEvents.emit("Failed to add friend: $friendId, try again!: ${e.message}")

            }
        }
    }


    fun deleteFriend(currentUserId: String, friendId: String) {
        viewModelScope.launch {
            try {
                repository.deleteFriend(currentUserId, friendId)

            } catch (e: Exception) {
                _errorEvents.emit("Failed to delete friend: $friendId, try again!: ${e.message}")
            }
        }
    }


    fun loadFriendsRealtime(currentUserId: String) {
        viewModelScope.launch {
            try {
                repository.listenToFriends(currentUserId)
                    .collect { friendList ->
                        _friends.value = friendList
                        _friendIds.value = friendList.map { it.id }.toSet()
                    }
            } catch (e: Exception) {
                _errorEvents.emit("Failed to fetch friends: ${e.message}")
            }
        }
    }




}