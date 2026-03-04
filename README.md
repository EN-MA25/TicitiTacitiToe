# TicitiTacitiToe

An online multiplayer Tic-Tac-Toe game built with Kotlin for Android, using Firebase as the backend.

## Features

- **Online Multiplayer** — Play against other players in real-time
- **Local Mode** — Play against a friend on the same phone without an account
- **Authentication** — Email/password login, Google Sign-In, and password reset
- **Leaderboard** — Global and friends leaderboard ranked by rating
- **Recent Games** — View your last 5 games with results, moves, and timestamps
- **Chat** — In-game messaging between players
- **Friends System** — Add friends and view friend-specific leaderboard
- **Play Again** — Rematch opponents from recent games
- **Rating System** — ELO-based rating starting at 1300

## Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel) with Repository pattern
- **Backend:** Firebase (Auth, Firestore)
- **UI:** XML layouts with ViewBinding
- **Async:** Kotlin Coroutines, StateFlow, Callbacks
- **Libraries:** AndroidX, CardView, ConstraintLayout, Google Credential Manager

## Project Structure
```text
app/src/main/java/com/example/ticititacititoe/
│
├── auth/                         — Authentication
│   ├── AuthRepository.kt             — Firebase Auth operations
│   ├── AuthViewModel.kt              — Auth state management
│   ├── AuthUiState.kt                — UI states (Loading, LoggedIn, LoggedOut)
│   └── ui/
│       ├── LoginActivity.kt          — Login screen
│       ├── RegisterActivity.kt       — Registration screen
│       └── SplashActivity.kt         — Auto-login check on app start
│
├── game/                         — Game logic
│   ├── GameRepository.kt             — Game invitations
│   └── ui/
│       ├── GameActivity.kt           — Game board screen
│       └── QueueFragment.kt          — Matchmaking queue
│
├── onlinegame/                   — Online game data
│   ├── OnlineGameRepository.kt       — Firestore queries for recent games
│   ├── OnlineGameViewModel.kt        — Recent games state management
│   ├── RecentGame.kt                 — Data class for recent game
│   ├── OnlineGameResult.kt           — Data class for game results
│   └── RecentGameAdapter.kt          — RecyclerView adapter
│
├── leaderboard/                  — Leaderboard
│   ├── LeaderboardRepository.kt      — Fetch global/friends leaderboard
│   ├── LeaderboardViewModel.kt       — Leaderboard state management
│   └── LeaderboardAdapter.kt         — RecyclerView adapter
│
├── chat/                         — In-game chat
│   ├── ChatRepository.kt             — Send/receive messages
│   ├── ChatViewModel.kt              — Chat state management
│   └── ChatAdapter.kt                — RecyclerView adapter
│
├── profile/                      — User profile
│   ├── User.kt                       — User data class
│   └── ProfileFragment.kt            — Profile screen
│
├── multiplayer/                  — Multiplayer management
│   ├── MultiplayerGameViewModel.kt   — Invitations and matchmaking
│   └── MultiplayerGameInvitationFragment.kt — Accept/decline bottom sheet
│
└── MainActivity.kt              — Main screen
```


## Architecture

The app follows the **MVVM** pattern:
UI (Activity/Fragment) → ViewModel → Repository → Firebase



- **Repository** — Handles all Firebase operations (Firestore queries, Auth calls)
- **ViewModel** — Manages UI state using `StateFlow` and exposes data to the UI
- **Activity/Fragment** — Observes state changes and updates the UI

## Firebase Collections

| Collection | Description |
|---|---|
| `users` | User profiles (uid, username, rating, stats) |
| `onlineGameResult` | Game results (playerWhoWon, playerWhoLost, timestamp, movesMade) |
| `games` | Active game sessions |
| `chats` | Chat messages between players |

## Getting Started

1. Clone the repository
2. Open in Android Studio
3. Connect to your Firebase project (add `google-services.json`)
4. Build and run on an emulator or device
