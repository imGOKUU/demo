package com.demo.attendancepro.data.repository

import com.demo.attendancepro.data.local.dao.EnrolmentOutcome
import com.demo.attendancepro.data.local.dao.StaffDao
import com.demo.attendancepro.data.local.entity.StaffEntity
import com.demo.attendancepro.face.decodeEmbedding
import com.demo.attendancepro.face.encodeEmbedding
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StaffRepository @Inject constructor(
    private val staffDao: StaffDao
) {
    fun getAllStaff(): Flow<List<StaffEntity>> = staffDao.getAll()

    fun getStaffById(staffId: Long): Flow<StaffEntity?> = staffDao.getById(staffId)

    suspend fun getStaffOnce(staffId: Long): StaffEntity? = staffDao.getByIdOnce(staffId)

    suspend fun addStaff(name: String, employeeId: String): Long =
        staffDao.insert(StaffEntity(name = name, employeeId = employeeId))

    /**
     * Saves a face enrolment for [staffId], but only if that staff member has no enrolled face
     * yet - one staff record can only ever have one enrolled face at a time. Enforced atomically
     * in [StaffDao.saveEnrolmentIfAbsent], not just in the UI. A future "re-enrol" admin action
     * would need to explicitly clear the embedding first.
     */
    suspend fun saveEnrolment(staffId: Long, embedding: FloatArray, selfiePath: String): EnrolmentOutcome =
        staffDao.saveEnrolmentIfAbsent(staffId, embedding.encodeEmbedding(), selfiePath)

    /** All staff with a completed enrolment, as (staffId, embedding) pairs ready for matching. */
    suspend fun getEnrolledEmbeddings(): List<Pair<Long, FloatArray>> =
        staffDao.getAllEnrolled().mapNotNull { staff ->
            staff.faceEmbedding?.let { staff.id to it.decodeEmbedding() }
        }
}