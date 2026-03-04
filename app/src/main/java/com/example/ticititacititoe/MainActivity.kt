package com.example.ticititacititoe

import android.app.AlertDialog
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
import com.example.ticititacititoe.user.ui.MyProfileActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ticititacititoe.auth.AuthViewModel
import com.example.ticititacititoe.error.setupErrorObserver
import com.example.ticititacititoe.game.invitations.ui.MultiplayerGameInvitationFragment
import com.example.ticititacititoe.game.invitations.InvitesViewModel
import com.example.ticititacititoe.game.recentGame.RecentGameAdapter
import com.example.ticititacititoe.game.invitations.ui.OutgoingInviteFragment
import com.example.ticititacititoe.game.invitations.ui.QueueFragment
import com.example.ticititacititoe.user.UserViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.example.ticititacititoe.onlinegame.OnlineGameViewModel
import com.example.ticititacititoe.tutorial.TutorialFragment
import com.example.ticititacititoe.user.ui.OtherUserProfileFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.combine


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var invitesViewModel: InvitesViewModel

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


        invitesViewModel = ViewModelProvider(this)[InvitesViewModel::class.java]

        val currentUserId = userViewModel.getCurrentUserId()

        if (currentUserId != null) {
            invitesViewModel.startListeningForInvites(currentUserId)
            invitesViewModel.startListeningForOutgoingInvites(currentUserId)
        }

        setupErrorObserver(invitesViewModel.errorEvents)
        setupErrorObserver(userViewModel.errorEvents)





        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                invitesViewModel.queue.map {it.isInQueue}
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
                combine(
                    invitesViewModel.incomingInvites,
                    invitesViewModel.outgoingInvites
                ) { incoming, outgoing ->
                    Pair(incoming, outgoing)
                }.collect { (incoming, outgoing) ->

                    val incomingDialog = supportFragmentManager.findFragmentByTag("invite_dialog")
                    val outgoingDialog = supportFragmentManager.findFragmentByTag("pending_invite_dialog")

                    when {
                        incoming.isNotEmpty() -> {
                            if (outgoingDialog is OutgoingInviteFragment) {
                                outgoingDialog.dismissAllowingStateLoss()
                            }

                            if (incomingDialog == null) {
                                MultiplayerGameInvitationFragment
                                    .newInstance(incoming.first())
                                    .show(supportFragmentManager, "invite_dialog")
                            }
                        }

                        outgoing.isNotEmpty() -> {
                            if (incomingDialog is MultiplayerGameInvitationFragment) {
                                incomingDialog.dismissAllowingStateLoss()
                            }

                            if (outgoingDialog == null) {
                                OutgoingInviteFragment
                                    .newInstance(outgoing.first())
                                    .show(supportFragmentManager, "pending_invite_dialog")
                            }
                        }

                        else -> {
                            (incomingDialog as? BottomSheetDialogFragment)?.dismissAllowingStateLoss()
                            (outgoingDialog as? BottomSheetDialogFragment)?.dismissAllowingStateLoss()
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

        binding.tutorialButton.setOnClickListener {

            val dialog = TutorialFragment()
            dialog.show(supportFragmentManager, "tutorial_dialog")

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
                        binding.recentGamesRecyclerView.adapter = RecentGameAdapter(
                            recentGames = games,
                            onPlayAgainClick = { game ->
                                val currentUsername = userViewModel.currentUser.value?.username ?: return@RecentGameAdapter
                                invitesViewModel.sendGameInvitation(
                                    currentUserId,
                                    currentUsername,
                                    game.opponentId,
                                    game.opponentUsername
                                )
                            },
                            onUserClick = { game ->
                                OtherUserProfileFragment
                                    .newInstance(game.opponentId, game.opponentUsername)
                                    .show(supportFragmentManager, "other_user_profile")
                            },
                            onDeleteClick = { game ->
                                AlertDialog.Builder(this@MainActivity)
                                    .setTitle("Delete Game")
                                    .setMessage("Do you want to delete this game?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        onlineGameViewModel.deleteRecentGame(game.gameId, currentUserId) { _ ->
                                            runOnUiThread { onResume() }
                                        }
                                    }
                                    .setNegativeButton("No", null)
                                    .show()
                            }
                        )
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
            invitesViewModel.leaveQueue(userId)
        }
    }


}