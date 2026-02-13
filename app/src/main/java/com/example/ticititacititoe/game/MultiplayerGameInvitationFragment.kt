package com.example.ticititacititoe.game

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.FragmentMultiplayerGameInvitationBinding
import com.example.ticititacititoe.profile.UserViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class MultiplayerGameInvitationFragment : BottomSheetDialogFragment() {
    private var fromUsername: String? = null
    private var fromUserId: String? = null
    private lateinit var currentUserId: String
    private lateinit var multiplayerGameViewModel: MultiplayerGameViewModel
    private lateinit var userViewModel: UserViewModel

    private lateinit var binding: FragmentMultiplayerGameInvitationBinding
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        multiplayerGameViewModel = ViewModelProvider(requireActivity())[MultiplayerGameViewModel::class.java]
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

        fromUsername = arguments?.getString(ARG_FROM_USER)
        fromUserId = arguments?.getString(ARG_FROM_USER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMultiplayerGameInvitationBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        private const val ARG_INVITE_ID = "invite_id"
        private const val ARG_FROM_USER = "from_user"
        private const val ARG_FROM_USER_ID = "from_user_id"
        private const val ARG_GAME_ID = "game_id"

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(invite: GameInvitation): MultiplayerGameInvitationFragment {
            val fragment = MultiplayerGameInvitationFragment()

            val args = Bundle().apply {
                putString(ARG_INVITE_ID, invite.id)
                putString(ARG_FROM_USER, invite.fromUsername)
                putString(ARG_GAME_ID, invite.gameId)
                putString(ARG_FROM_USER_ID, invite.fromUserId)
            }

            fragment.arguments = args
            return fragment
        }



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = userViewModel.getCurrentUserId() ?: return

        val invitationText = binding.invitationTextView

        invitationText.text = getString(R.string.has_challenged_you_in_a_blitz_game, fromUsername)

        binding.declineInviteButton.setOnClickListener {
            fromUserId?.let { multiplayerGameViewModel.deleteInvitations(currentUserId, it) }
            dismiss()
        }

        binding.acceptInviteButton.setOnClickListener {
            // Start blitz game
            fromUserId?.let { multiplayerGameViewModel.deleteInvitations(currentUserId, it) }
            dismiss()
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
        fromUserId?.let { multiplayerGameViewModel.deleteInvitations(currentUserId, it) }

    }






}