package com.example.ticititacititoe.profile.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ticititacititoe.R
import com.example.ticititacititoe.auth.AuthViewModel
import com.example.ticititacititoe.databinding.FragmentSearchUserBinding
import com.example.ticititacititoe.profile.UserViewModel
import com.example.ticititacititoe.profile.adapter.SearchUserRecyclerAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment




class SearchUserFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSearchUserBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var searchInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchUserRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

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
        adapter = SearchUserRecyclerAdapter(onUserClick = {user ->
            //Start game with user
        })

        recyclerView = binding.searchedUsersRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = adapter

        searchInput = binding.searchUserEditText
        userViewModel.fetchAllUsers()
        binding.searchButton.setOnClickListener {
            val searchTerm = searchInput.text.toString()
            if(searchTerm.isNotEmpty()) {
                userViewModel.searchUsers(searchTerm)
            }
        }

        searchInput.addTextChangedListener{text ->
            val query = text.toString().trim()

            if (query.isNotEmpty()) {
                userViewModel.searchUsers(query)
            } else {
                userViewModel.fetchAllUsers()
            }
        }
        userViewModel.users.observe(viewLifecycleOwner) {list ->
            adapter.submitList(list)
        }


    }
}