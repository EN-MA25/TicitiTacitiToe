package com.example.ticititacititoe.user.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.FragmentOtherUserProfileBinding
import com.example.ticititacititoe.friends.FriendViewModel
import com.example.ticititacititoe.game.invitations.InvitesViewModel
import com.example.ticititacititoe.game.invitations.ui.IncomingInviteFragment
import com.example.ticititacititoe.user.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class OtherUserProfileFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentOtherUserProfileBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var friendViewModel: FriendViewModel
    private lateinit var invitesViewModel: InvitesViewModel

    private var currentUserId: String? = null
    private var currentUsername: String = ""

    private var otherUserId: String = ""
    private var otherUsername: String = ""


    companion object {
        fun newInstance(userId: String, username: String): OtherUserProfileFragment {
            return OtherUserProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                    putString("username", username)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        otherUserId = arguments?.getString("userId") ?: ""
        otherUsername = arguments?.getString("username") ?: ""


        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        invitesViewModel = ViewModelProvider(requireActivity())[InvitesViewModel::class.java]
        friendViewModel = ViewModelProvider(requireActivity())[FriendViewModel::class.java]

        currentUserId = userViewModel.getCurrentUserId()

        if (currentUserId != null) {
            invitesViewModel.startListeningForInvites(currentUserId!!)
            invitesViewModel.startListeningForOutgoingInvites(currentUserId!!)
            userViewModel.startFriendListener(currentUserId!!)
            userViewModel.fetchUserById(otherUserId)
        }

        friendViewModel.loadFriendsRealtime(currentUserId!!)




    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtherUserProfileBinding.inflate(inflater, container, false)
        return binding.root
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                invitesViewModel.incomingInvites.collect { invites ->
                    val existing = childFragmentManager.findFragmentByTag("invite_dialog")
                    if (invites.isNotEmpty()) {
                        IncomingInviteFragment.Companion
                            .newInstance(invites.first())
                            .show(childFragmentManager, "invite_dialog")
                    } else {
                        if (existing is IncomingInviteFragment) {
                            existing.dismissAllowingStateLoss()
                        }
                    }
                }
            }
        }



            viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                friendViewModel.friendId.collect { friendIds ->

                    if (friendIds.contains(otherUserId)) {
                        binding.friendsImageButton.setImageResource(R.drawable.delete_friend)
                        if (currentUserId != null) {
                            binding.friendsImageButton.setOnClickListener {
                                friendViewModel.deleteFriend(currentUserId!!, otherUserId)

                            }
                        }
                    } else {
                        binding.friendsImageButton.setImageResource(R.drawable.add_friend)
                        if (currentUserId != null) {
                            binding.friendsImageButton.setOnClickListener {
                                friendViewModel.addFriend(currentUserId!!, otherUserId)

                            }
                        }

                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.viewedUser.collect { user ->
                    binding.usernameTextView.text = "${user?.username} ${user?.rating}"
                    binding.initialsTextView.text = user?.username?.take(2)
                    binding.wonGamesNumberTextView.text = user?.wonGames.toString()
                    binding.lostGamesNumberTextView.text = user?.lostGames.toString()
                    binding.totalGamesNumberTextView.text = user?.totalGames.toString()
                    binding.currentStreakTextView.text =
                        getString(R.string.current_streak, user?.currentStreak)
                    binding.maxStreakTextView.text =
                        getString(R.string.max_streak, user?.maxStreak)

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.currentUser.collect { user ->
                    currentUsername = user?.username ?: ""
                }
            }
        }

        binding.backButton.setOnClickListener {
            dismiss()
        }

        binding.playButton.setOnClickListener {
            invitesViewModel.sendGameInvitation(
                currentUserId,
                currentUsername,
                otherUserId,
                otherUsername
            )
        }
    }
}