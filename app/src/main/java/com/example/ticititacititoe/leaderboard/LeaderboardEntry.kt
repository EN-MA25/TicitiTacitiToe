package com.example.ticititacititoe.leaderboard

import com.example.ticititacititoe.profile.User

data class LeaderboardEntry(
    val user: User,
    val rank: Int,
    val winRate: Double
)
