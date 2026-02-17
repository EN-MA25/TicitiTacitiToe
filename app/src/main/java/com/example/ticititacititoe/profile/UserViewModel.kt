package com.example.ticititacititoe.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(): ViewModel() {
    private val repository =  UserRepository()



    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()



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

    fun loadCurrentUser(id: String?) {
        if (id == null) return

        viewModelScope.launch {
            _currentUser.value = repository.getUserDetailsById(id)
        }
    }



}