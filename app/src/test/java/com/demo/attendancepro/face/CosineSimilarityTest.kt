package com.demo.attendancepro.face

import org.junit.Assert.assertEquals
import org.junit.Test

class CosineSimilarityTest {

    @Test
    fun identicalVectors_haveSimilarityOne() {
        val a = floatArrayOf(1f, 2f, 3f)
        assertEquals(1f, cosineSimilarity(a, a), 1e-5f)
    }

    @Test
    fun orthogonalVectors_haveSimilarityZero() {
        val a = floatArrayOf(1f, 0f)
        val b = floatArrayOf(0f, 1f)
        assertEquals(0f, cosineSimilarity(a, b), 1e-5f)
    }

    @Test
    fun oppositeVectors_haveSimilarityNegativeOne() {
        val a = floatArrayOf(1f, 2f, 3f)
        val b = floatArrayOf(-1f, -2f, -3f)
        assertEquals(-1f, cosineSimilarity(a, b), 1e-5f)
    }

    @Test
    fun embeddingCodec_roundTrips() {
        val original = floatArrayOf(0.1f, -0.25f, 3f)
        val decoded = original.encodeEmbedding().decodeEmbedding()
        assertEquals(original.size, decoded.size)
        original.indices.forEach { assertEquals(original[it], decoded[it], 1e-5f) }
    }
}
