package com.example.ticititacititoe.leaderboard

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

class LeaderboardViewModel(): ViewModel() {
    private var repository = LeaderboardRepository()
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    private val _globalLeaderboard = MutableLiveData<List<User>>()
    val globalLeaderboard: LiveData<List<User>> = _globalLeaderboard

    private val _friendLeaderboard = MutableStateFlow<List<User>>(emptyList())
    val friendLeaderboard = _friendLeaderboard.asStateFlow()

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents = _errorEvents.asSharedFlow()


    fun getAllUsers(){
        viewModelScope.launch {
            try {
                val users = repository.getAllUsers()
                _users.value = users
            }catch (e: Exception){
                _users.value = emptyList()
                _errorEvents.emit("Failed to load allUsers: ${e.message}")

            }
        }
    }


    fun loadGlobalLeaderboard() {
        viewModelScope.launch {
            try {
                val users = repository.getAllUsers()
                val sortedUsers = users
                    .sortedByDescending { it.rating}
                _globalLeaderboard.value = sortedUsers
            } catch (e: Exception) {
                _globalLeaderboard.value = emptyList()
                _errorEvents.emit("Failed to load leaderboard: ${e.message}")

            }
        }
    }

    fun loadFriendLeaderboard(currentUserId: String) {
        viewModelScope.launch {
            try {
                val friends = repository.getFriendsForLeaderboard(currentUserId)
                    .sortedByDescending { it.rating }
                _friendLeaderboard.value = friends
            } catch (e: Exception) {
                _errorEvents.emit("Failed to load leaderboard: ${e.message}")
                _friendLeaderboard.value = emptyList()
            }
        }
    }




}