package com.demo.attendancepro.face

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.Closeable
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Loads a TFLite face-embedding model from assets and turns a cropped face [Bitmap] into a
 * fixed-length embedding vector. See [ModelSpec] docs for which model is bundled and why -
 * MobileFaceNet could not be sourced with a verifiable license inside this environment, so a
 * same-family FaceNet TFLite model is used instead (documented in the project README).
 */
@Singleton
class FaceEmbedder @Inject constructor(
    @ApplicationContext context: Context
) : Closeable {

    private val interpreter: Interpreter = Interpreter(
        loadModelFile(context),
        Interpreter.Options().apply { numThreads = 4 }
    )

    /** Runs the model on [faceBitmap] and returns an [ModelSpec.EMBEDDING_SIZE]-length embedding. */
    fun embed(faceBitmap: Bitmap): FloatArray {
        val input = preprocess(faceBitmap)
        val output = Array(1) { FloatArray(ModelSpec.EMBEDDING_SIZE) }
        interpreter.run(input, output)
        return output[0]
    }

    override fun close() {
        interpreter.close()
    }

    /**
     * Resizes to the model's input size and applies per-image standardization
     * (x' = (x - mean) / max(std, 1/sqrt(n))), matching the model's training preprocessing.
     */
    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val size = ModelSpec.INPUT_SIZE
        val resized = Bitmap.createScaledBitmap(bitmap, size, size, true)
        val pixels = IntArray(size * size)
        resized.getPixels(pixels, 0, size, 0, 0, size, size)

        val raw = FloatArray(size * size * 3)
        var i = 0
        for (pixel in pixels) {
            raw[i++] = ((pixel shr 16) and 0xFF).toFloat()
            raw[i++] = ((pixel shr 8) and 0xFF).toFloat()
            raw[i++] = (pixel and 0xFF).toFloat()
        }

        val mean = raw.average().toFloat()
        var std = sqrt(raw.sumOf { (it - mean).toDouble().pow(2) } / raw.size).toFloat()
        std = max(std, 1f / sqrt(raw.size.toFloat()))

        val buffer = ByteBuffer.allocateDirect(4 * raw.size).order(ByteOrder.nativeOrder())
        for (value in raw) {
            buffer.putFloat((value - mean) / std)
        }
        buffer.rewind()
        return buffer
    }

    private fun loadModelFile(context: Context): ByteBuffer {
        context.assets.openFd(ModelSpec.ASSET_FILE_NAME).use { fd ->
            FileInputStream(fd.fileDescriptor).use { input ->
                return input.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    fd.startOffset,
                    fd.declaredLength
                )
            }
        }
    }

    object ModelSpec {
        const val ASSET_FILE_NAME = "facenet.tflite"
        const val INPUT_SIZE = 160
        const val EMBEDDING_SIZE = 128
    }
}

/** Cosine similarity between two equal-length embedding vectors, in [-1, 1]. */
fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
    require(a.size == b.size) { "Embeddings must be the same length" }
    var dot = 0f
    var normA = 0f
    var normB = 0f
    for (i in a.indices) {
        dot += a[i] * b[i]
        normA += a[i] * a[i]
        normB += b[i] * b[i]
    }
    val denom = sqrt(normA) * sqrt(normB)
    return if (denom == 0f) 0f else dot / denom
}
