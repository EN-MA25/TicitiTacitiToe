package com.example.ticititacititoe.onlinegame.ui

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ticititacititoe.databinding.GameOverFragmentBinding
import com.example.ticititacititoe.game.invitations.InvitesViewModel
import com.example.ticititacititoe.onlinegame.OnlineGameViewModel
import com.example.ticititacititoe.onlinegame.model.GameResultEvent
import com.example.ticititacititoe.user.UserViewModel
import com.example.ticititacititoe.user.ui.AchievementsFragment
import com.example.ticititacititoe.user.ui.USER_ID
import kotlinx.coroutines.launch

class GameOverFragment(gameResult: GameResultEvent) : DialogFragment() {
    private var _gameResult = gameResult
    private var _binding: GameOverFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var invitesViewModel: InvitesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GameOverFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        invitesViewModel = ViewModelProvider(requireActivity())[InvitesViewModel::class.java]

        binding.closeButton.setOnClickListener {
            dismiss()
            requireActivity().finish()
        }

        // Just some examples how to show it
        binding.gameOverTextView.text = if (_gameResult.didWin) "Congratulation! You Won!" else "Sorry. You Lost!"
        binding.movesTextView.text = "Your new rating is ${_gameResult.user.rating}"

        if (_gameResult.newAchievements.isNotEmpty()) {
            // Show button that you got new achievements
            binding.newAchievementsButton.visibility = View.VISIBLE
            binding.newAchievementsButton.text = "You got ${_gameResult.newAchievements.size} new achievements"
            binding.newAchievementsButton.setOnClickListener {
                val fragment = AchievementsFragment().apply {
                    arguments = Bundle().apply {
                        putString(USER_ID, _gameResult.user.id)
                    }
                }
                fragment.show(parentFragmentManager, "achievement_fragment_dialog")
            }

        } else {
            binding.newAchievementsButton.visibility = View.GONE
            // Hide button

        }

        binding.playAgainButton.setOnClickListener {
            invitesViewModel.sendGameInvitation(_gameResult.user.id, _gameResult.user.username!!, _gameResult.opponentUserId, _gameResult.opponentUsername)
            dismiss()
            requireActivity().finish()
        }
    }
    override fun onStart() {
        super.onStart()
        val screenHeight = resources.displayMetrics.heightPixels
        val dialogHeight = (screenHeight * 0.40).toInt()

        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dialogHeight
            )
            val params = attributes
            params.gravity = Gravity.BOTTOM
            attributes = params
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}