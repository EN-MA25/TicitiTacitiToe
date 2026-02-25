package com.example.ticititacititoe.leaderboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticititacititoe.profile.User
import kotlinx.coroutines.flow.MutableStateFlow
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


    fun getAllUsers(){
        viewModelScope.launch {
            try {
                val users = repository.getAllUsers()
                _users.value = users
            }catch (exception: Exception){
                _users.value = emptyList()
            }
        }
    }


    fun loadGlobalLeaderboard() {
        viewModelScope.launch {
            try {
                val users = repository.getAllUsers()
                val sortedUsers = users
                    .sortedByDescending { it.rating }
                _globalLeaderboard.value = sortedUsers
            } catch (exception: Exception) {
                _globalLeaderboard.value = emptyList()
            }
        }
    }

    fun loadFriendLeaderboard(currentUserId: String) {
        viewModelScope.launch {
            try {
                val friends = repository.getFriendsForLeaderboard(currentUserId)
                    .sortedByDescending { it.rating }
                _friendLeaderboard.value = friends
            } catch (exception: Exception) {
                // error message
                _friendLeaderboard.value = emptyList()
            }
        }
    }




}