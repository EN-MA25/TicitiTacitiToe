package com.example.ticititacititoe.leaderboard.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ticititacititoe.databinding.FragmentGlobalLeaderboardBinding
import com.example.ticititacititoe.leaderboard.LeaderboardAdapter
import com.example.ticititacititoe.leaderboard.LeaderboardViewModel




class GlobalLeaderboardFragment : Fragment() {

    private lateinit var binding: FragmentGlobalLeaderboardBinding
    private lateinit var leaderboardViewModel: LeaderboardViewModel
    private lateinit var adapter: LeaderboardAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGlobalLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        leaderboardViewModel = ViewModelProvider(requireActivity())[LeaderboardViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
        leaderboardViewModel.loadGlobalLeaderboard()
    }

    private fun observeViewModel() {
        leaderboardViewModel.globalLeaderboard.observe(viewLifecycleOwner){entries ->
          adapter.updateEntries(entries)
        }
    }

    private fun setupRecyclerView() {
        adapter = LeaderboardAdapter(emptyList())
        binding.globalLeaderboardRecyclerView.adapter = adapter
        binding.globalLeaderboardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }


}