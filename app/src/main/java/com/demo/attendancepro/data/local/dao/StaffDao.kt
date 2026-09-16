package com.demo.attendancepro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.demo.attendancepro.data.local.entity.StaffEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StaffDao {

    @Insert
    suspend fun insert(staff: StaffEntity): Long

    @Update
    suspend fun update(staff: StaffEntity)

    @Query("SELECT * FROM staff ORDER BY name ASC")
    fun getAll(): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE id = :staffId")
    fun getById(staffId: Long): Flow<StaffEntity?>

    @Query("SELECT * FROM staff WHERE id = :staffId")
    suspend fun getByIdOnce(staffId: Long): StaffEntity?

    @Query("SELECT * FROM staff WHERE faceEmbedding IS NOT NULL")
    suspend fun getAllEnrolled(): List<StaffEntity>

    /**
     * Enrols a face for [staffId] only if that staff member doesn't already have one. Wrapped in
     * a single DB transaction (read-check-write) so this is enforced at the data layer, not just
     * in the calling ViewModel - no caller can race past the check and overwrite an existing
     * embedding.
     */
    @Transaction
    suspend fun saveEnrolmentIfAbsent(
        staffId: Long,
        encodedEmbedding: String,
        selfiePath: String
    ): EnrolmentOutcome {
        val staff = getByIdOnce(staffId) ?: return EnrolmentOutcome.StaffNotFound
        if (staff.faceEmbedding != null) return EnrolmentOutcome.AlreadyEnrolled
        update(staff.copy(faceEmbedding = encodedEmbedding, enrolledSelfiePath = selfiePath))
        return EnrolmentOutcome.Saved
    }
}