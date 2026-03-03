package com.example.ticititacititoe.achievements

import com.example.ticititacititoe.profile.User

object AchievementManager {

    val achievements = listOf(
        Achievement("streak_3",  "Heating Up",  "Three wins in a row.") { user -> user.currentStreak >= 3},
        Achievement("streak_10",  "Absolute Domination",  "Ten wins in a row. No mercy.") { user -> user.currentStreak >= 10},
        Achievement("moves_10",  "Warm Up",  "10 moves made.") { user -> user.totalMovesMade >= 10},
        Achievement("moves_100",  "Grid Veteran",  "100 moves placed.") { user -> user.totalMovesMade >= 100},
        Achievement("games_10",  "Can't Stop",  "10 games played.") { user -> user.totalGames >= 10},
        Achievement("won_1",  "It Begins",  "Your first win.") { user -> user.wonGames >= 1},
        Achievement("won_10",  "Winning Habit",  "10 victories.") { user -> user.wonGames >= 10},
        Achievement("rating_1500",  "Tactical Mind",  "Reach 1500 rating.") { user -> user.rating >= 1500},
        Achievement("rating_2000",  "Grand Master",  "Reach 2000 rating.") { user -> user.rating >= 2000},
        Achievement("sunday",  "Sunday",  "You won a game on a Sunday!.") { user -> user.lastWonGame?.toDate()?.day == 0},
        Achievement("monday",  "Monday",  "You won a game on a Monday!.") { user -> user.lastWonGame?.toDate()?.day == 1},
        Achievement("tuesday",  "Tuesday",  "You won a game on a Tuesday!.") { user -> user.lastWonGame?.toDate()?.day == 2},
        Achievement("wednesday",  "Wednesday",  "You won a game on a Wednesday!.") { user -> user.lastWonGame?.toDate()?.day == 3},
        Achievement("thursday",  "Thursday",  "You won a game on a Thursday!.") { user -> user.lastWonGame?.toDate()?.day == 4},
        Achievement("friday",  "Friday",  "You won a game on a Friday!.") { user -> user.lastWonGame?.toDate()?.day == 5},
        Achievement("saturday",  "Saturday",  "You won a game on a Saturday!.") { user -> user.lastWonGame?.toDate()?.day == 6}

    )

    fun checkForNewAchievements(user: User): List<String> {
        return achievements
            .filter { achievement ->
                !user.achievements.containsKey(achievement.id) && achievement.condition(user)
            }
            .map {
                it.id
            }
    }

    fun userAchievements(user: User): List<Achievement> {
        return achievements
            .filter { achievement ->
                user.achievements.containsKey(achievement.id)
            }
    }
}