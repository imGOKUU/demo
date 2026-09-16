package com.demo.attendancepro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.demo.attendancepro.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert
    suspend fun insert(attendance: AttendanceEntity): Long

    @Query("SELECT * FROM attendance WHERE staffId = :staffId ORDER BY id DESC")
    fun getForStaff(staffId: Long): Flow<List<AttendanceEntity>>
}
