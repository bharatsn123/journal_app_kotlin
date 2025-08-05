package com.bharatcoding.journalapplication

import java.text.SimpleDateFormat
import java.util.*

data class Journal (
    val title: String = "",
    val thoughts: String = "",
    val imageUrl: String = "",      // Firebase Storage URL or empty string for no image
    val userId: String = "",
    val timeAdded: Int = 0,     // Unix timestamp in milliseconds
    val username: String = ""
) {
    val formattedTimeAdded: String
        get() {
            val date = Date(timeAdded.toLong())
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            return sdf.format(date)
        }
    
    val hasImage: Boolean
        get() = imageUrl.isNotEmpty()
}
