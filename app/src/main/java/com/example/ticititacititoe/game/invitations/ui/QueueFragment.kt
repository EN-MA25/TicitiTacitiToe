package com.example.ticititacititoe.game.invitations.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.FragmentQueueBinding
import com.example.ticititacititoe.game.invitations.InvitesViewModel
import com.example.ticititacititoe.user.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class QueueFragment : BottomSheetDialogFragment() {
    private lateinit var invitesViewModel: InvitesViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var currentUserId: String
    private lateinit var binding: FragmentQueueBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        invitesViewModel = ViewModelProvider(requireActivity())[InvitesViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = userViewModel.getCurrentUserId() ?: return
        binding.queueTextView.text = getString(R.string.waiting_for_player)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                invitesViewModel.queue.collect { state ->
                    val queueSize = state.queueSize
                    binding.inQueueTextView.text = "$queueSize: in queue"
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog ?: return

        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return

        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        behavior.isDraggable = true
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        invitesViewModel.leaveQueue(currentUserId)
    }
}