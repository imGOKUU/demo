package com.demo.attendancepro.di

import android.content.Context
import androidx.room.Room
import com.demo.attendancepro.data.local.AppDatabase
import com.demo.attendancepro.data.local.dao.AttendanceDao
import com.demo.attendancepro.data.local.dao.StaffDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME).build()

    @Provides
    fun provideStaffDao(database: AppDatabase): StaffDao = database.staffDao()

    @Provides
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao = database.attendanceDao()
}
