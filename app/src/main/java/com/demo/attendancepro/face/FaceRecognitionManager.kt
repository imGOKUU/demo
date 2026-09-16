package com.demo.attendancepro.face

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.math.max
import kotlin.math.min

/** Outcome of trying to find exactly one usable face in a captured selfie. */
sealed class FaceDetectionResult {
    data class Success(val croppedFace: Bitmap) : FaceDetectionResult()
    object NoFaceDetected : FaceDetectionResult()
    object MultipleFacesDetected : FaceDetectionResult()
}

/** Outcome of comparing a freshly captured face against an enrolled staff member. */
sealed class MatchResult {
    data class Matched(val staffId: Long, val similarity: Float) : MatchResult()
    data class NotMatched(val bestSimilarity: Float?) : MatchResult()
}

/**
 * Single entry point for all face-recognition logic used across enrolment and attendance
 * marking: detect exactly one face (ML Kit), crop it, embed it (see [FaceEmbedder]) and compare
 * embeddings with cosine similarity.
 */
@Singleton
class FaceRecognitionManager @Inject constructor(
    private val faceEmbedder: FaceEmbedder
) {

    private val detector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .build()
    )

    /** Runs ML Kit face detection on [bitmap] and, if exactly one face is found, crops it. */
    suspend fun detectSingleFace(bitmap: Bitmap): FaceDetectionResult {
        val faces = detectFaces(bitmap)
        return when {
            faces.isEmpty() -> FaceDetectionResult.NoFaceDetected
            faces.size > 1 -> FaceDetectionResult.MultipleFacesDetected
            else -> FaceDetectionResult.Success(cropFace(bitmap, faces.first()))
        }
    }

    /** Generates an embedding for an already-cropped face bitmap. */
    fun embed(faceBitmap: Bitmap): FloatArray = faceEmbedder.embed(faceBitmap)

    /**
     * Compares [embedding] against every enrolled staff member and returns the closest match if
     * its cosine similarity clears [threshold].
     */
    fun findBestMatch(
        embedding: FloatArray,
        enrolledStaff: List<Pair<Long, FloatArray>>,
        threshold: Float = DEFAULT_MATCH_THRESHOLD
    ): MatchResult {
        var bestStaffId: Long? = null
        var bestSimilarity = -1f
        for ((staffId, enrolledEmbedding) in enrolledStaff) {
            val similarity = cosineSimilarity(embedding, enrolledEmbedding)
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity
                bestStaffId = staffId
            }
        }
        return if (bestStaffId != null && bestSimilarity >= threshold) {
            MatchResult.Matched(bestStaffId, bestSimilarity)
        } else {
            MatchResult.NotMatched(bestStaffId?.let { bestSimilarity })
        }
    }

    private suspend fun detectFaces(bitmap: Bitmap): List<Face> = suspendCancellableCoroutine { continuation ->
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        detector.process(inputImage)
            .addOnSuccessListener { faces ->
                if (continuation.isActive) continuation.resume(faces)
            }
            .addOnFailureListener {
                if (continuation.isActive) continuation.resume(emptyList())
            }
    }

    private fun cropFace(source: Bitmap, face: Face): Bitmap {
        val box = face.boundingBox
        val marginX = (box.width() * FACE_CROP_MARGIN).toInt()
        val marginY = (box.height() * FACE_CROP_MARGIN).toInt()

        val left = max(0, box.left - marginX)
        val top = max(0, box.top - marginY)
        val right = min(source.width, box.right + marginX)
        val bottom = min(source.height, box.bottom + marginY)

        val width = max(1, right - left)
        val height = max(1, bottom - top)

        return if (left == 0 && top == 0 && width == source.width && height == source.height) {
            source
        } else {
            Bitmap.createBitmap(source, left, top, width, height)
        }
    }

    companion object {
        const val DEFAULT_MATCH_THRESHOLD = 0.65f
        private const val FACE_CROP_MARGIN = 0.15f
    }
}
