package com.example.ticititacititoe.game

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticititacititoe.game.ui.QueueUiState
import com.example.ticititacititoe.profile.User
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MultiplayerGameViewModel: ViewModel() {
    private val repository = GameRepository()

    private val _incomingInvites = MutableStateFlow<List<GameInvitation>>(emptyList())
    val incomingInvites = _incomingInvites.asStateFlow()

    private val _outgoingInvites = MutableStateFlow<List<GameInvitation>>(emptyList())
    val outgoingInvites = _outgoingInvites.asStateFlow()

    private val _queue = MutableStateFlow(QueueUiState())
    val queue = _queue.asStateFlow()
    private var queueListenerStarted = false

    private val _inviteState = MutableStateFlow<InviteState>(InviteState.Idle)
    val inviteState: StateFlow<InviteState> = _inviteState.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }

    fun startListeningToSentInvite(currentUserId: String, otherUserId: String) {
        // Ask repository for the Flow + ListenerRegistration
        val (flow, registration) = repository.listenToInvite(otherUserId, currentUserId)
        listenerRegistration = registration

        // Collect the Flow in the ViewModel's scope
        viewModelScope.launch {
            flow.collect { state ->
                _inviteState.value = state
            }
        }
    }

    fun startListeningForInvites(userId: String) {
        viewModelScope.launch {
            repository.loadIncomingGameInvitations(userId)
                .collect { invites ->
                    _incomingInvites.value = invites
                }
        }
    }

    fun startListeningForOutgoingInvites(currentUserId: String) {
        viewModelScope.launch {
            repository.loadOutgoingGameInvitations(currentUserId)
                .collect { invites ->
                    _outgoingInvites.value = invites
                }
        }
    }


    fun sendGameInvitation(
        fromUserId: String?,
        fromUserName: String,
        toUserId: String,
        toUserName: String
    ) {
        if (fromUserId != null) {
            viewModelScope.launch {
                try {
                    repository.sendGameInvite(fromUserId, fromUserName, toUserId, toUserName)
                } catch (exception: Exception) {
                    Log.e("Invite", "Failed to send invite", exception)
                }
            }
        }
    }

    fun acceptInvite(currentUserId: String, fromUserId: String) {
        repository.acceptInvitation(currentUserId, fromUserId)
    }

    fun deleteInvitations(currentUserId: String,
                          otherUserId: String) {
        viewModelScope.launch {
            try {
                repository.deleteInvitations(currentUserId, otherUserId)
            } catch (exception: Exception) {
                Log.e("Invite", "Failed to delete invite", exception)

            }
        }
    }

    fun enterQueue(userId: String, username: String) {
        viewModelScope.launch {
            _queue.update { it.copy(isLoading = true, error = null) }
            try {
                repository.addToQueue(userId, username)
                _queue.update {current -> current.copy(isInQueue = true, isLoading = false, error = null) }

                if (!queueListenerStarted) {
                    startObservingQueue()
                    queueListenerStarted = true
                }
            }catch (exception: Exception) {
                Log.e("QUEUE_ERROR", "FAILED TO ADD", exception)

                _queue.update { it.copy(isInQueue = false, isLoading = false, error = exception.message) }

            }
        }
    }

    private fun startObservingQueue() {
        viewModelScope.launch {

            repository.observeQueueSize().collect { size ->

                _queue.update { it.copy(queueSize = size) }
            }
        }
    }




        fun leaveQueue(userId: String) {
        viewModelScope.launch {
            _queue.update { it.copy(isLoading = true, error = null) }

            try {
                repository.deleteFromQueue(userId)
                _queue.update {current -> current.copy(isInQueue = false, isLoading = false, error = null) }


            } catch (exception: Exception) {
                _queue.update { it.copy(isInQueue = false, isLoading = false, error = exception.message) }
            }
        }
    }
}