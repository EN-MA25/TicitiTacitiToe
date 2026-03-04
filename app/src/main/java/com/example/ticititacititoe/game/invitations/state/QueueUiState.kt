package com.example.ticititacititoe.game.invitations.state

data class QueueUiState(
    val isInQueue: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val matchId: String? = null,
    val queueSize: Int = 0
)