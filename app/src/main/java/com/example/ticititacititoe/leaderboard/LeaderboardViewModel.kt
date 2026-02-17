package com.example.ticititacititoe.leaderboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticititacititoe.profile.User
import kotlinx.coroutines.launch

class LeaderboardViewModel(): ViewModel() {
    private var repository = LeaderboardRepository()
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

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

    fun getRatings(userId: String, callback: (Double?) -> Unit){
                 repository.getRatings(userId, callback)

    }




}