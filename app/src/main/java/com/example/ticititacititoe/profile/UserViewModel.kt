package com.example.ticititacititoe.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UserViewModel(): ViewModel() {
    private val repository =  UserRepository()



    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    fun searchUsers(searchTerm: String, currentUserId: String) {
        if(searchTerm.isBlank()) {
            _users.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val users = repository.searchUsers(searchTerm, currentUserId)
                _users.value = users
            } catch (exception: Exception) {
                _users.value = emptyList()
            }
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            try {
                val users = repository.getAllUsers()
                _users.value = users
            } catch (exception: Exception) {
                _users.value = emptyList()
            }
        }
    }

    fun fetchCurrentUser(){
        viewModelScope.launch {
            try{
                _currentUser.value = repository.getCurrentUser()
            }catch (e: Exception){
                _currentUser.value = null
            }
        }
    }

    fun getCurrentUserId(): String? {
        return repository.getCurrentUserId()
    }

    fun getUserDetailsById(userId: String?, callback: (User?) -> Unit) {
        if (userId != null) {
            repository.getUserDetailsById(userId, callback)
        }
    }



}