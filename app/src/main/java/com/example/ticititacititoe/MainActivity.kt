package com.example.ticititacititoe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import com.example.ticititacititoe.databinding.ActivityMainBinding
import com.example.ticititacititoe.game.ui.ChallengeFragment
import com.example.ticititacititoe.game.ui.GameActivity
import com.example.ticititacititoe.leaderboard.ui.LeaderboardActivity
import com.example.ticititacititoe.profile.ui.MyProfileActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ticititacititoe.auth.AuthViewModel
import com.example.ticititacititoe.game.invitations.MultiplayerGameInvitationFragment
import com.example.ticititacititoe.game.invitations.MultiplayerGameViewModel
import com.example.ticititacititoe.game.recentGame.RecentGameAdapter
import com.example.ticititacititoe.game.ui.OutgoingInviteFragment
import com.example.ticititacititoe.game.ui.QueueFragment
import com.example.ticititacititoe.profile.UserViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.example.ticititacititoe.onlinegame.OnlineGameViewModel


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var multiplayerGameViewModel: MultiplayerGameViewModel

    private lateinit var authViewModel: AuthViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var onlineGameViewModel: OnlineGameViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        userViewModel = ViewModelProvider(this)[UserViewModel::class.java]
        onlineGameViewModel = ViewModelProvider(this)[OnlineGameViewModel::class.java]


        multiplayerGameViewModel = ViewModelProvider(this)[MultiplayerGameViewModel::class.java]

        val currentUserId = userViewModel.getCurrentUserId()

        if (currentUserId != null) {
            multiplayerGameViewModel.startListeningForInvites(currentUserId)
            multiplayerGameViewModel.startListeningForOutgoingInvites(currentUserId)
        }



        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                multiplayerGameViewModel.queue.map {it.isInQueue}
                    .distinctUntilChanged()
                    .collect { isInQueue ->
                        val existing = supportFragmentManager.findFragmentByTag("queue_dialog")

                        if (isInQueue) {
                            if(existing == null) {
                                QueueFragment().show(supportFragmentManager, "queue_dialog")
                            }

                        } else {
                            if (existing is QueueFragment) {
                                existing.dismissAllowingStateLoss()
                            }
                        }
                    }





            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                multiplayerGameViewModel.incomingInvites.collect { invites ->
                    val existing = supportFragmentManager.findFragmentByTag("invite_dialog")

                    if (invites.isNotEmpty()) {
                        val invite = invites.first()

                        MultiplayerGameInvitationFragment
                            .newInstance(invite)
                            .show(supportFragmentManager, "invite_dialog")
                    } else {
                        if (existing is MultiplayerGameInvitationFragment) {
                            existing.dismissAllowingStateLoss()
                        }
                    }
            }


            }


        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                multiplayerGameViewModel.outgoingInvites.collect { invites ->
                    val existing =
                        supportFragmentManager.findFragmentByTag("pending_invite_dialog")

                    if(invites.isNotEmpty()) {
                        val invite = invites.first()

                        OutgoingInviteFragment
                            .newInstance(invite)
                            .show(supportFragmentManager, "pending_invite_dialog")

                    }else {
                        if (existing is OutgoingInviteFragment) {
                            existing.dismissAllowingStateLoss()
                        }
                    }
                }
            }

        }
        binding.recentGamesRecyclerView.layoutManager = LinearLayoutManager(this)


        binding.profileButton.setOnClickListener {
            val intent = Intent(this, MyProfileActivity::class.java)
            startActivity(intent)
        }


        binding.highscoreButton.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }

        binding.newGameButton.setOnClickListener {
           val dialog = ChallengeFragment()
            dialog.show(supportFragmentManager, "challenge_fragment_dialog")
        }

    }
    override fun onResume() {
        super.onResume()
        val currentUserId = userViewModel.getCurrentUserId()
        Log.d("RecentGames", "onResume userId: $currentUserId")
        if (currentUserId != null) {
            userViewModel.fetchCurrentUser()
            onlineGameViewModel.fetchRecentGames(currentUserId) { result ->
                result.onSuccess { games ->
                    runOnUiThread {
                        binding.recentGamesRecyclerView.adapter = RecentGameAdapter(games) { game ->
                            val currentUsername = userViewModel.currentUser.value?.username ?: return@RecentGameAdapter
                            multiplayerGameViewModel.sendGameInvitation(
                                currentUserId,
                                currentUsername,
                                game.opponentId,
                                game.opponentUsername
                            )
                        }
                    }
                }
            }
        }
    }

    fun newGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)

    }

    override fun onStop() {
        super.onStop()
        val userId = userViewModel.getCurrentUserId()
        if (userId != null) {
            multiplayerGameViewModel.leaveQueue(userId)
        }
    }
}