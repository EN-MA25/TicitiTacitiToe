package com.example.ticititacititoe.onlinegame

class OnlineGameResult(
    playerWhoWon: String?,
    playerWhoLost: String?,
    timestamp: Long = 0L,
    movesMade: Int = 0
) {
    val onlineGameResultId: String = ""
    val _playerWhoWon: String? = playerWhoWon
    val _playerWhoLost: String? = playerWhoLost
    val _timestamp: Long = timestamp
    val _movesMade: Int = movesMade
}