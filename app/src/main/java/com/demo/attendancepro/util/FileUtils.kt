package com.demo.attendancepro.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

object FileUtils {

    private const val SELFIES_DIR = "selfies"

    /** Creates a new, unique JPEG file under the app's private files dir for a selfie capture. */
    fun newSelfieFile(context: Context, prefix: String): File {
        val dir = File(context.filesDir, SELFIES_DIR).apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US).format(java.util.Date())
        return File(dir, "${prefix}_$timestamp.jpg")
    }
}
