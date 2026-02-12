package com.example.ticititacititoe.game.ui

data class QueueUiState(
    val isInQueue: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val matchId: String? = null
)
