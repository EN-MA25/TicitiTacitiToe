package com.example.ticititacititoe.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UserViewModel(): ViewModel() {
    private val repository =  UserRepository()


    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users
    fun searchUsers(searchTerm: String) {
        if(searchTerm.isBlank()) {
            _users.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val users = repository.searchUsers(searchTerm)
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

}