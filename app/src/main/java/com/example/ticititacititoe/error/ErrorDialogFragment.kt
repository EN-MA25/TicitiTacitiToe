package com.example.ticititacititoe.error

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.ticititacititoe.R
import com.example.ticititacititoe.databinding.FragmentErrorDialogBinding

class ErrorDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentErrorDialogBinding

    companion object{
        private const val ARG_MESSAGE = "error_message"

        fun newInstance(errorMessage: String): ErrorDialogFragment {
            val fragment = ErrorDialogFragment()
            val args = Bundle()
            args.putString(ARG_MESSAGE, errorMessage)
            fragment.arguments = args
            return fragment
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentErrorDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val message = arguments?.getString(ARG_MESSAGE) ?: "Unknown error occurred"
        binding.errorMessageTextView.text = message

        binding.okButton.setOnClickListener {
            dismiss()
        }
    }
}