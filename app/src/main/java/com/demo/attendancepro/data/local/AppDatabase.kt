package com.demo.attendancepro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.demo.attendancepro.data.local.dao.AttendanceDao
import com.demo.attendancepro.data.local.dao.StaffDao
import com.demo.attendancepro.data.local.entity.AttendanceEntity
import com.demo.attendancepro.data.local.entity.StaffEntity

@Database(
    entities = [StaffEntity::class, AttendanceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun staffDao(): StaffDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        const val DATABASE_NAME = "attendance_pro.db"
    }
}
