package com.example.ticititacititoe.game.invitations

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ticititacititoe.game.invitations.model.GameInvitation
import com.example.ticititacititoe.game.invitations.state.InviteState
import com.example.ticititacititoe.game.invitations.state.QueueUiState
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvitesViewModel: ViewModel() {
    private val repository = GameInvitationRepository()

    private val _incomingInvites = MutableStateFlow<List<GameInvitation>>(emptyList())
    val incomingInvites = _incomingInvites.asStateFlow()

    private val _outgoingInvites = MutableStateFlow<List<GameInvitation>>(emptyList())
    val outgoingInvites = _outgoingInvites.asStateFlow()

    private val _queue = MutableStateFlow(QueueUiState())
    val queue = _queue.asStateFlow()
    private var queueListenerStarted = false

    private val _inviteState = MutableStateFlow<InviteState>(InviteState.Idle)
    val inviteState: StateFlow<InviteState> = _inviteState.asStateFlow()

    private val _errorEvents = MutableSharedFlow<String>()
    val errorEvents = _errorEvents.asSharedFlow()

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

                } catch (e: Exception) {
                    _errorEvents.emit("Failed to send invite: ${e.message}")
                }
            }
        }
    }

    // ========= Accept invite ===========
    fun acceptInvite(currentUserId: String, fromUserId: String) {
        viewModelScope.launch {
            try {
                repository.acceptInvitation(currentUserId, fromUserId)

            } catch (e: Exception) {
                _errorEvents.emit("Failed to accept invite, try again!: ${e.message}")
            }
        }
    }

//     ========== Decline invite =======
    fun declineInvite(currentUserId: String, fromUserId: String) {
        viewModelScope.launch {
            try {
                repository.declineInvitation(currentUserId, fromUserId)
            } catch (e: Exception) {
                _errorEvents.emit("Failed to decline invite, try again!: ${e.message}")

            }
        }
    }

    // ======= Delete invite ========
    fun deleteInvitations(currentUserId: String,
                          otherUserId: String, deleteBothInvitations : Boolean = false) {
        viewModelScope.launch {
            try {
                repository.deleteInvitations(currentUserId, otherUserId, deleteBothInvitations)
            } catch (e: Exception) {
                _errorEvents.emit("Failed to delete invitations, try again!: ${e.message}")


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
            }catch (e: Exception) {
                Log.e("QUEUE_ERROR", "FAILED TO ADD", e)
                _errorEvents.emit("Failed to add to queue, try again!: ${e.message}")


                _queue.update { it.copy(isInQueue = false, isLoading = false, error = e.message) }

            }
        }
    }

    private fun startObservingQueue() {
        viewModelScope.launch {
            try {
                repository.observeQueueSize().collect { size ->
                    _queue.update { it.copy(queueSize = size) }
                }

            }catch (e: Exception) {
                _errorEvents.emit("Failed to load queue, try again!: ${e.message}")

            }

        }
    }

        fun leaveQueue(userId: String) {
        viewModelScope.launch {
            _queue.update { it.copy(isLoading = true, error = null) }

            try {
                repository.deleteFromQueue(userId)
                _queue.update {current -> current.copy(isInQueue = false, isLoading = false, error = null) }


            } catch (e: Exception) {
                _queue.update { it.copy(isInQueue = false, isLoading = false, error = e.message) }
                _errorEvents.emit("Failed to leave queue, try again!: ${e.message}")

            }
        }
    }
}