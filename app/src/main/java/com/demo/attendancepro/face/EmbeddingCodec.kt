package com.demo.attendancepro.face

/** [StaffEntity.faceEmbedding] is stored as a comma-separated string; these convert to/from that. */
fun FloatArray.encodeEmbedding(): String = joinToString(",")

fun String.decodeEmbedding(): FloatArray = split(",").map { it.toFloat() }.toFloatArray()
