package com.example.ticititacititoe.profile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ticititacititoe.databinding.FragmentSearchUserBinding
import com.example.ticititacititoe.error.setupErrorObserver
import com.example.ticititacititoe.friends.FriendViewModel
import com.example.ticititacititoe.game.invitations.MultiplayerGameViewModel
import com.example.ticititacititoe.profile.UserViewModel
import com.example.ticititacititoe.profile.adapter.SearchUserRecyclerAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch


class SearchUserFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSearchUserBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var friendViewModel: FriendViewModel

    private lateinit var multiplayerGameViewModel: MultiplayerGameViewModel
    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchUserRecyclerAdapter

    private lateinit var currentUsername: String
    private  var currentUserId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        multiplayerGameViewModel = ViewModelProvider(requireActivity())[MultiplayerGameViewModel::class.java]
        friendViewModel = ViewModelProvider(requireActivity())[FriendViewModel::class.java]


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchUserBinding.inflate(inflater, container,false)
        return binding.root
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = userViewModel.getCurrentUserId()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.currentUser.collect { user ->
                    currentUsername = user?.username ?: "null"
                }
            }
        }

        setupErrorObserver(friendViewModel.errorEvents)
        setupErrorObserver(userViewModel.errorEvents)







        adapter = SearchUserRecyclerAdapter(onUserClick = {user ->
            multiplayerGameViewModel.sendGameInvitation(currentUserId, currentUsername, user.id, user.username!!)
        }, {user ->
            friendViewModel.addFriend( currentUserId!!, user.id)},
            onDeleteFriendClick = {user ->
                friendViewModel.deleteFriend( currentUserId!!, user.id)
            })


        if (currentUserId != null) {
            userViewModel.startFriendListener(currentUserId!!)
        }
        recyclerView = binding.searchedUsersRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

        searchInput = binding.searchUserEditText
        userViewModel.fetchAllUsers()
        binding.searchButton.setOnClickListener {
            val searchTerm = searchInput.text.toString()
            if(searchTerm.isNotEmpty()) {
                    userViewModel.searchUsers(searchTerm, currentUserId!!)
            }
        }

        searchInput.addTextChangedListener{text ->
            val query = text.toString().trim()
            if (query.isNotEmpty()) {
                userViewModel.searchUsers(query, currentUserId!!)
            } else {
                userViewModel.fetchAllUsers()
            }
        }

//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                userViewModel.users.collect { list ->
//                    adapter.submitList(list)
//                }
//            }
//        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.searchUIList.collect { uiList ->
                    adapter.submitList(uiList)
                }
            }
        }


    }
}