package com.example.ticititacititoe.achievements

import com.example.ticititacititoe.user.model.User

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val condition: (User) -> Boolean
)
