package com.example.ticititacititoe.game.invitations.model

data class GameInvitation(
    val id: String = "",
    val toUsername: String = "",
    val toUserId: String = "",
    val fromUserId: String = "",
    val fromUsername: String = "",
    val gameId: String = "",
    val timestamp: Long = 0L,
    var pending: String? = ""


)