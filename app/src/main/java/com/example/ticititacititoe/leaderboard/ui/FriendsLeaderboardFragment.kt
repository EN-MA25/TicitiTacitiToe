package com.example.ticititacititoe.leaderboard.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ticititacititoe.databinding.FragmentFriendsLeaderboardBinding
import com.example.ticititacititoe.error.setupErrorObserver
import com.example.ticititacititoe.leaderboard.LeaderboardAdapter
import com.example.ticititacititoe.leaderboard.LeaderboardViewModel
import com.example.ticititacititoe.user.UserViewModel
import kotlinx.coroutines.launch

class FriendsLeaderboardFragment : Fragment() {
    private lateinit var binding: FragmentFriendsLeaderboardBinding
    private lateinit var leaderboardViewModel: LeaderboardViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var adapter: LeaderboardAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendsLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        leaderboardViewModel = ViewModelProvider(requireActivity())[LeaderboardViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        setupRecyclerView()

        val currentUserId = userViewModel.getCurrentUserId() ?: return

        leaderboardViewModel.loadFriendLeaderboard(currentUserId)
        setupErrorObserver(leaderboardViewModel.errorEvents)
        setupErrorObserver(userViewModel.errorEvents)



        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                leaderboardViewModel.friendLeaderboard.collect { entries ->
                 adapter.updateEntries(entries)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = LeaderboardAdapter(emptyList())
        binding.friendsLeaderboardRecyclerView.adapter = adapter
        binding.friendsLeaderboardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }


}