package com.demo.attendancepro.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    fun currentDate(): String = dateFormat.format(Date())
    fun currentTime(): String = timeFormat.format(Date())
}
