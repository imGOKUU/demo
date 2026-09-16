package com.demo.attendancepro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val employeeId: String,
    /** Comma-separated float embedding produced by [com.demo.attendancepro.face.FaceRecognitionManager]. Null until enrolled. */
    val faceEmbedding: String? = null,
    val enrolledSelfiePath: String? = null
)
