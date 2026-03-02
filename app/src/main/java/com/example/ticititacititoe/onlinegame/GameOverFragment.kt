package com.example.ticititacititoe.onlinegame

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle


import com.example.ticititacititoe.databinding.GameOverFragmentBinding
import com.example.ticititacititoe.game.invitations.MultiplayerGameViewModel
import com.example.ticititacititoe.profile.UserViewModel
import kotlinx.coroutines.launch

class GameOverFragment(playerX: String?) : DialogFragment() {

    private var _playerX = playerX
    private var _binding: GameOverFragmentBinding? = null
    private val binding get() = _binding!!
    private var currentUserId: String? = ""
    private var opponentUserId: String? = ""
    private var opponentUsername: String? = ""
    private var currentUsername: String? = ""
    private lateinit var onlineGameViewModel: OnlineGameViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var multiplayerGameViewModel: MultiplayerGameViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GameOverFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        onlineGameViewModel = ViewModelProvider(requireActivity())[OnlineGameViewModel::class.java]
        multiplayerGameViewModel = ViewModelProvider(requireActivity())[MultiplayerGameViewModel::class.java]

        currentUserId = userViewModel.getCurrentUserId() ?: return

        binding.closeButton.setOnClickListener {
            dismiss()
            requireActivity().finish()
        }

        binding.playAgainButton.setOnClickListener {
        }

          userViewModel.getUserDetailsById(currentUserId) {user ->
              currentUsername = user?.username
          }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                onlineGameViewModel.gameResult.collect { result ->
                    result?.let { gameResult ->
                        binding.gameOverTextView.text = if (gameResult.playerWhoWon == currentUserId) {
                            if(gameResult.playerWhoWon == _playerX) "Player X Won" else "Player O Won"
                        } else {
                            if(gameResult.playerWhoLost == _playerX) "Player X Lost" else "Player O Lost"
                        }
                        if (currentUserId == gameResult.playerWhoWon) {
                            userViewModel.getUserDetailsById(gameResult.playerWhoLost) { user ->
                                opponentUsername = user?.username
                                opponentUserId = user?.id
                            }
                        } else {
                            userViewModel.getUserDetailsById(gameResult.playerWhoWon) { user ->
                                opponentUsername = user?.username
                                opponentUserId = user?.id
                            }
                        }

                        binding.playAgainButton.setOnClickListener {
                            multiplayerGameViewModel.sendGameInvitation(currentUserId, currentUsername!!, opponentUserId!!, opponentUsername!!)
                            dismiss()
                            requireActivity().finish()
                        }
                        binding.movesTextView.text = "${gameResult.movesMade} \n total moves made"
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}