package com.example.ticititacititoe

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

object Util {

    fun formatTime(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }

}