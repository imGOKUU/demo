package com.demo.attendancepro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
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
}
