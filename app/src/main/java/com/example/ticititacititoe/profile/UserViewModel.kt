package com.example.ticititacititoe.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticititacititoe.friends.FriendRepository
import com.example.ticititacititoe.profile.ui.UserSearchUIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserViewModel(): ViewModel() {
    private val repository =  UserRepository()

    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser = _selectedUser.asStateFlow()
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()
    private val _friends = MutableStateFlow<List<User>>(emptyList())
    private val _friendIds = MutableStateFlow<Set<String>>(emptySet())
    private val friendRepository = FriendRepository()
    private val _viewedUser = MutableStateFlow<User?>(null)
    val viewedUser = _viewedUser.asStateFlow()

    fun fetchUserById(userId: String) {
        viewModelScope.launch {
            try {
                _viewedUser.value = repository.getUserDetailsById(userId)
            } catch (e: Exception) {
                _viewedUser.value = null
            }
        }
    }

    fun startFriendListener(currentUserId: String) {
        viewModelScope.launch {
            friendRepository.listenToFriends(currentUserId).collect { friendList ->
                _friendIds.value = friendList.map { it.id }.toSet()
            }
        }
    }
    val searchUIList: StateFlow<List<UserSearchUIModel>> = combine(_users, _friendIds) { users, friendIds ->
        users.map { user ->
            val myId = getCurrentUserId()
            users.filter { user -> user.id != myId }
            UserSearchUIModel(
                user = user,
                isFriend = friendIds.contains(user.id)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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

//    fun loadUserById(id: String?) {
//        if (id == null) return
//
//        viewModelScope.launch {
//            _selectedUser.value = repository.getUserDetailsById(id)
//        }
//    }

    fun getUserDetailsById(userId: String?, callback: (User?) -> Unit) {
        if (userId != null) {
            repository.getUserDetailsById(userId, callback)
        }
    }

    fun updateUserAfterGame(me: User, opponent: User, didWin: Boolean, movesMade: Int) {
        repository.updateUserAfterGame(me, opponent, didWin, movesMade)
    }

    fun loadFriendProfiles(currentUserId: String) {
        viewModelScope.launch {
            try {
                val friends = friendRepository.getFriendProfiles(currentUserId)
                _friends.value = friends
            } catch (e: Exception) {
//                _errorEvents.emit("Could not fetch friend profiles")
            }
        }
    }


    val friendUIList: StateFlow<List<UserSearchUIModel>> = combine(
        _friends,
        _friendIds
    ) { profiles, friendIds ->
        profiles.filter { user ->
            friendIds.contains(user.id)
        }.map { user ->
            UserSearchUIModel(
                user = user,
                isFriend = true
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}