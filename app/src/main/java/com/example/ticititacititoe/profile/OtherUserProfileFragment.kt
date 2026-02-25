package com.example.ticititacititoe.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.FragmentOtherUserProfileBinding
import com.example.ticititacititoe.friends.FriendViewModel
import com.example.ticititacititoe.game.invitations.MultiplayerGameViewModel
import com.example.ticititacititoe.profile.adapter.SearchUserRecyclerAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class OtherUserProfileFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentOtherUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOtherUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onStart() {
        super.onStart()

        val dialog = dialog as? BottomSheetDialog ?: return

        dialog.window?.setWindowAnimations(R.style.DialogSlideInRightAnimation)

        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?: return

        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.peekHeight = 0
        behavior.skipCollapsed = true


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            dismiss()
        }
    }
}