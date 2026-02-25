package com.example.ticititacititoe.game.invitations

sealed class InviteState {
    object Idle : InviteState()
    object Pending : InviteState()
    object Accepted : InviteState()
    object Declined : InviteState()
}