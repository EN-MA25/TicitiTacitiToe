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

<img width="270" height="570" alt="Screenshot_20260307_142255" src="https://github.com/user-attachments/assets/ce2eedec-140e-4dde-bb4d-c7ed0b846053" />
<img width="270" height="570" alt="Screenshot_20260307_140826" src="https://github.com/user-attachments/assets/10180ec9-66f8-48b6-8fa7-f552c0b1cc3b" />
<img width="270" height="570" alt="Screenshot_20260307_140849" src="https://github.com/user-attachments/assets/d0c8c1d2-7c55-4217-93df-dab7f34c115b" />
<img width="270" height="570" alt="Screenshot_20260307_140942" src="https://github.com/user-attachments/assets/ba25f089-e1f1-4f33-9626-c46a3da0b469" />
<img width="270" height="570" alt="Screenshot_20260307_141001" src="https://github.com/user-attachments/assets/7756a8e9-0206-4455-bd78-7cc27ec0bee7" />
<img width="270" height="570" alt="Screenshot_20260307_141048" src="https://github.com/user-attachments/assets/18c2124e-a9d3-4f9e-b7dc-3a56ec8cd304" />
<img width="270" height="570" alt="Screenshot_20260307_141227" src="https://github.com/user-attachments/assets/d50e53e1-cff2-4dfc-9f7d-0aed63537408" />
<img width="270" height="570" alt="Screenshot_20260307_141253" src="https://github.com/user-attachments/assets/54c0a2b2-f336-41f9-8325-2621c2292cdf" />
<img width="270" height="570" alt="Screenshot_20260307_141317" src="https://github.com/user-attachments/assets/9b41ca89-a363-49b1-abbf-9ec565325f14" />
<img width="270" height="570" alt="Screenshot_20260307_141731" src="https://github.com/user-attachments/assets/0f18e76f-7393-4ec9-9549-3db1cf7440f8" />
<img width="270" height="570" alt="Screenshot_20260307_141654" src="https://github.com/user-attachments/assets/10b561ac-1af2-4958-b781-effaee37127f" />
<img width="270" height="570" alt="Screenshot_20260307_141908" src="https://github.com/user-attachments/assets/64b20176-67e4-43c7-af2c-eee15be16c6f" />
<img width="270" height="570" alt="Screenshot_20260307_141946" src="https://github.com/user-attachments/assets/95ec7b1b-1025-47a9-9937-5e5affaf04ec" />
<img width="270" height="570" alt="Screenshot_20260307_142007" src="https://github.com/user-attachments/assets/722d0712-e35c-49b8-a5f4-fec36ad060ac" />
<img width="270" height="570" alt="Screenshot_20260307_142042" src="https://github.com/user-attachments/assets/3c857162-12e6-44d7-818d-1ca796216e94" />
<img width="270" height="570" alt="Screenshot_20260307_142122" src="https://github.com/user-attachments/assets/02b3710d-5639-485a-bee7-6e3a3b43d68c" />
<img width="270" height="570" alt="Screenshot_20260307_142217" src="https://github.com/user-attachments/assets/c8d7ad60-18fd-42e1-a76b-f6a32b28edcb" />
<img width="270" height="570" alt="Screenshot_20260307_142425" src="https://github.com/user-attachments/assets/e6f0f4cf-1e2a-4322-87b2-1d69e9cf9174" />



















