package com.example.ticititacititoe.achievements

import com.google.firebase.Timestamp

data class UserAchievement (
    val achievement: Achievement,
    val unlockedAt: Timestamp
)
