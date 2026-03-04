package com.example.ticititacititoe.game.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.FragmentChallengeBinding
import com.example.ticititacititoe.game.invitations.MultiplayerGameViewModel
import com.example.ticititacititoe.friends.ui.FriendFragment
import com.example.ticititacititoe.user.UserViewModel
import com.example.ticititacititoe.user.ui.SearchUserFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch


class ChallengeFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentChallengeBinding
    private lateinit var multiplayerGameViewModel: MultiplayerGameViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var currentUsername: String
    private lateinit var currentUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        multiplayerGameViewModel = ViewModelProvider(requireActivity())[MultiplayerGameViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        currentUserId = userViewModel.getCurrentUserId() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.currentUser.collect { user ->
                    currentUsername = user?.username ?: "null"
                }
            }
        }

        binding.searchPlayerButton.setOnClickListener {
            val dialog = SearchUserFragment()
            dialog.show(parentFragmentManager, "search_user_fragment_dialog")
        }

        binding.playOnPhoneButton.setOnClickListener {
            val intent = Intent(requireActivity(), GameActivity::class.java)
            startActivity(intent)
        }

        binding.playWithFriendButton.setOnClickListener {
            val dialog = FriendFragment()
            dialog.show(parentFragmentManager, "friend_fragment_dialog")
        }

        binding.playARandomDudeButton.setOnClickListener {
            multiplayerGameViewModel.enterQueue(currentUserId, currentUsername)
        }

        binding.backButton.setOnClickListener {
            dismiss()
        }
    }


    override fun onStart() {
        super.onStart()

        val dialog = dialog as? BottomSheetDialog ?: return

        dialog.window?.setWindowAnimations(R.style.DialogSlideInRightAnimation)

        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return

        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.peekHeight = 0
        behavior.skipCollapsed = true


    }







}
