package com.example.ticititacititoe.chat

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
import androidx.recyclerview.widget.RecyclerView
import com.example.ticititacititoe.databinding.FragmentChatBinding
import com.example.ticititacititoe.error.setupErrorObserver
import com.example.ticititacititoe.profile.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch


class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private lateinit var userViewModel: UserViewModel

    private lateinit var behavior: BottomSheetBehavior<View>

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatRecyclerAdapter
    private  var currentUserId: String? = null
    private var gameId: String? = null
    private var opponentId: String? = null
    private var opponentUsername: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatViewModel = ViewModelProvider(requireActivity())[ChatViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        gameId = arguments?.getString("gameId")
        opponentId = arguments?.getString("opponentId")
        opponentUsername = arguments?.getString("opponentUsername")


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = userViewModel.getCurrentUserId()

        val gid = gameId ?: return
        chatViewModel.listenToChat(gid)

        adapter = ChatRecyclerAdapter(
            currentUserId!!,
            opponentUsername ?: ""
        )

        recyclerView = binding.chatRecyclerView


        val layoutManager = LinearLayoutManager(requireContext())

        recyclerView.layoutManager = layoutManager

        recyclerView.adapter = adapter

        binding.sendButton.setOnClickListener {

            val gid = gameId ?: return@setOnClickListener
            val uid = currentUserId ?: return@setOnClickListener
            val text = binding.messageEditText.text.toString()
            if (text.isBlank()) return@setOnClickListener

            chatViewModel.sendMessage(gid, text, uid)
            clearFields()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                chatViewModel.messages.collect { list ->
                    Log.d("CHAT", "messages size = ${list.size}")
                    adapter.submitList(list) {
                        recyclerView.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }
        }

        setupErrorObserver(chatViewModel.errorEvents)

    }

//    override fun onStart() {
//        super.onStart()
//
//        val dialog = dialog as? BottomSheetDialog ?: return
//        val bottomSheet = dialog.findViewById<View>(
//            com.google.android.material.R.id.design_bottom_sheet
//        ) ?: return
//
//        val behavior = BottomSheetBehavior.from(bottomSheet)
//
//        val screenHeight = resources.displayMetrics.heightPixels
//        val desiredHeight = (screenHeight * 0.35).toInt()
//
//        behavior.peekHeight = desiredHeight
//        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
//        behavior.skipCollapsed = false
//        behavior.isDraggable = true
//    }

    private fun clearFields() {
        binding.messageEditText.text.clear()
    }

}

