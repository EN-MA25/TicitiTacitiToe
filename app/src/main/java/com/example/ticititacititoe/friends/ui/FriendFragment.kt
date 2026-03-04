package com.example.ticititacititoe.friends.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ticititacititoe.databinding.FragmentFriendBinding
import com.example.ticititacititoe.error.setupErrorObserver
import com.example.ticititacititoe.friends.FriendViewModel
import com.example.ticititacititoe.game.invitations.MultiplayerGameViewModel
import com.example.ticititacititoe.user.UserViewModel
import com.example.ticititacititoe.user.adapter.SearchUserRecyclerAdapter
import com.example.ticititacititoe.user.ui.OtherUserProfileFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class FriendFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentFriendBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var friendViewModel: FriendViewModel
    private lateinit var multiplayerGameViewModel: MultiplayerGameViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchUserRecyclerAdapter
    private lateinit var currentUsername: String
    private  var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        multiplayerGameViewModel = ViewModelProvider(requireActivity())[MultiplayerGameViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        friendViewModel = ViewModelProvider(requireActivity())[FriendViewModel::class.java]


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = userViewModel.getCurrentUserId() ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.currentUser.collect { user ->
                    currentUsername = user?.username ?: "null"
                }
            }
        }

        adapter = SearchUserRecyclerAdapter(onUserClick = {user ->
            multiplayerGameViewModel.sendGameInvitation(currentUserId, currentUsername, user.id, user.username!! )

        }, { user ->
            friendViewModel.addFriend(currentUserId!!, user.id) },
            {user ->
                friendViewModel.deleteFriend(currentUserId!!, user.id)

            },
            onInitialsClick = { user ->
                OtherUserProfileFragment
                    .newInstance(user.id, user.username ?: "")
                    .show(parentFragmentManager, "other_user_profile")
            })

        if (currentUserId != null) {
            userViewModel.startFriendListener(currentUserId!!)
            userViewModel.loadFriendProfiles(currentUserId!!)
        }

        setupErrorObserver(friendViewModel.errorEvents)


        recyclerView = binding.friendsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.friendUIList.collect { uiList ->
                    adapter.submitList(uiList)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog ?: return

        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return

        val displayMetrics = resources.displayMetrics
        bottomSheet.layoutParams.height = (displayMetrics.heightPixels * 0.95).toInt()

        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = true
    }
}