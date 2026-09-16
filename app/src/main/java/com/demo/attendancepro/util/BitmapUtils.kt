package com.demo.attendancepro.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

object BitmapUtils {

    /**
     * CameraX writes capture rotation as an EXIF tag rather than rotating the pixel data itself.
     * This decodes [file], applies that rotation so the bitmap is upright, overwrites the file
     * with the corrected pixels (so later reads - e.g. attendance history thumbnails - don't need
     * to repeat this), and returns the upright bitmap.
     */
    fun decodeUprightAndNormalize(file: File): Bitmap {
        val original = BitmapFactory.decodeFile(file.absolutePath)
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        val upright = if (rotationDegrees == 0f) {
            original
        } else {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
        }

        if (upright !== original) {
            FileOutputStream(file).use { out -> upright.compress(Bitmap.CompressFormat.JPEG, 92, out) }
        }
        return upright
    }
}
