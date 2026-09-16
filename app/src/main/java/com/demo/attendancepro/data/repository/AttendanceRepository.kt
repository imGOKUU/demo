package com.demo.attendancepro.data.repository

import com.demo.attendancepro.data.local.dao.AttendanceDao
import com.demo.attendancepro.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao
) {
    fun getAttendanceForStaff(staffId: Long): Flow<List<AttendanceEntity>> =
        attendanceDao.getForStaff(staffId)

    suspend fun recordAttendance(
        staffId: Long,
        date: String,
        time: String,
        selfiePath: String,
        latitude: Double,
        longitude: Double
    ): Long = attendanceDao.insert(
        AttendanceEntity(
            staffId = staffId,
            date = date,
            time = time,
            selfiePath = selfiePath,
            latitude = latitude,
            longitude = longitude,
            matched = true
        )
    )
}
