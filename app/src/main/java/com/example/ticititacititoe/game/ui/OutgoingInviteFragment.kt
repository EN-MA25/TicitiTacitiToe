package com.example.ticititacititoe.game.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.FragmentOutgoingInviteBinding
import com.example.ticititacititoe.game.GameInvitation
import com.example.ticititacititoe.game.InviteState
import com.example.ticititacititoe.game.MultiplayerGameViewModel
import com.example.ticititacititoe.onlinegame.OnlineGameActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class OutgoingInviteFragment : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var toUsername: String? = null
    private var toUserId: String? = null
    private var fromUserId: String? = null

    private lateinit var multiplayerGameViewModel: MultiplayerGameViewModel
    private lateinit var binding: FragmentOutgoingInviteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        multiplayerGameViewModel = ViewModelProvider(requireActivity())[MultiplayerGameViewModel::class.java]
        toUsername = arguments?.getString(ARG_TO_USER_NAME)
        toUserId = arguments?.getString(ARG_TO_USER_ID)
        fromUserId = arguments?.getString(ARG_FROM_USER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOutgoingInviteBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {

        private const val ARG_INVITE_ID = "invite_id"
        private const val ARG_TO_USER_NAME = "to_username"
        private const val ARG_TO_USER_ID = "to_user_id"
        private const val ARG_FROM_USER = "from_user"
        private const val ARG_FROM_USER_ID = "from_user_id"
        private const val ARG_GAME_ID = "game_id"

        @JvmStatic
        fun newInstance(invite: GameInvitation): OutgoingInviteFragment {
            val fragment = OutgoingInviteFragment()

            val args = Bundle().apply {
                putString(ARG_INVITE_ID, invite.id)
                putString(ARG_TO_USER_NAME, invite.toUsername)
                putString(ARG_TO_USER_ID, invite.toUserId)
                putString(ARG_GAME_ID, invite.gameId)
                putString(ARG_FROM_USER_ID, invite.fromUserId)
            }

            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val invitationText = binding.invitationTextView
        invitationText.text = getString(R.string.pending_invite_to, toUsername)

        multiplayerGameViewModel.startListeningToSentInvite(toUserId!!, fromUserId!!)

        // ================== Observe invite state ==================
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                multiplayerGameViewModel.inviteState.collect { state ->

                    // ================== Handling different conditions for invite ==================
                    when (state) {
                        is InviteState.Accepted -> {
                            delay(2000)

                            val intent = Intent(requireContext(), OnlineGameActivity::class.java)
                            intent.putExtra("currentUserId", toUserId)
                            intent.putExtra("fromUserId", fromUserId)
                            startActivity(intent)
                            dismiss()
                        }
                        is InviteState.Declined -> {
                            multiplayerGameViewModel.deleteInvitations(toUserId!!, fromUserId!!)
                            //Toast.makeText(requireContext(), "The opponent declined your invation", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                        else -> {
                            // Pending
                        }
                    }
                }
            }
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
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

        // =========== Call viewmodel to delete in db for both (when sender cut off invation  ===========
        if (fromUserId != null && toUserId != null) {
            multiplayerGameViewModel.deleteInvitations(
                toUserId!!,
                fromUserId!!,
                true
            )
        }
    }

}
