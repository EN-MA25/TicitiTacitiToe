package com.example.ticititacititoe.leaderboard.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.FragmentGlobalLeaderboardBinding
import com.example.ticititacititoe.leaderboard.LeaderboardAdapter
import com.example.ticititacititoe.leaderboard.LeaderboardViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class GlobalLeaderboardFragment : Fragment() {

    private lateinit var binding: FragmentGlobalLeaderboardBinding
    private lateinit var leaderboardViewModel: LeaderboardViewModel
    private lateinit var adapter: LeaderboardAdapter
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGlobalLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        leaderboardViewModel = ViewModelProvider(requireActivity())[LeaderboardViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
        
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

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GlobalLeaderboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}