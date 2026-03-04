package com.example.ticititacititoe.game.invitations.state

sealed class InviteState {
    object Idle : InviteState()
    object Pending : InviteState()
    object Accepted : InviteState()
    object Declined : InviteState()
}