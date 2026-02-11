package com.example.ticititacititoe.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository): ViewModel() {
    // Speaking to UserRepository

    private val _searchResults = MutableLiveData<List<User>>()
    val searchResults: LiveData<List<User>> = _searchResults

    fun searchUsers(searchTerm: String) {
        if(searchTerm.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val users = repository.searchUsers(searchTerm)
                _searchResults.value = users
            } catch (exception: Exception) {
                _searchResults.value = emptyList()
            }
        }
    }

}