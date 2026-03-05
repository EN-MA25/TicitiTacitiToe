package com.example.ticititacititoe.user.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ticititacititoe.achievements.AchievementManager
import com.example.ticititacititoe.databinding.FragmentAchievementsBinding
import com.example.ticititacititoe.user.UserViewModel
import com.example.ticititacititoe.user.adapter.AchievementsRecyclerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

const val USER_ID = "USER_ID"

class AchievementsFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentAchievementsBinding
    private lateinit var adapter: AchievementsRecyclerAdapter

    private lateinit var userViewModel: UserViewModel
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = arguments?.getString(USER_ID)
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        userId?.let { userViewModel.fetchUserById(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.viewedUser.collect { user ->
                    user?.let {
                        val unlockedAchievements = AchievementManager.userAchievements(user)
                        val lockedAchievements = AchievementManager.lockedAchievements(user)
                        adapter = AchievementsRecyclerAdapter(unlockedAchievements, lockedAchievements)
                        binding.achievementsRecyclerView.layoutManager = LinearLayoutManager(requireActivity())
                        binding.achievementsRecyclerView.adapter = adapter
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(userId: String): AchievementsFragment {
            return AchievementsFragment().apply {
                arguments = Bundle().apply {
                    putString(USER_ID, userId)
                }
            }
        }
    }
}