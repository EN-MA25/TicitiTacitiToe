package com.example.ticititacititoe.error

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

fun Fragment.setupErrorObserver(errorFlow: SharedFlow<String>) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            errorFlow.collect { message ->
                val existingFragment = parentFragmentManager.findFragmentByTag("error_dialog")
                if (existingFragment == null) {
                    val dialog = ErrorDialogFragment.newInstance(message)
                    dialog.show(parentFragmentManager, "error_dialog")
                }
            }
        }
    }
}


    fun AppCompatActivity.setupErrorObserver(errorFlow: SharedFlow<String>) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                errorFlow.collect { message ->
                    val dialog = ErrorDialogFragment.newInstance(message)
                    dialog.show(supportFragmentManager, "error_dialog")
                }
            }
        }
    }
