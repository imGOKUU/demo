package com.demo.attendancepro.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object StaffList : Screen("staff_list")
    object AddStaff : Screen("add_staff")
    object MarkAttendance : Screen("mark_attendance")

    object FaceEnrolment : Screen("face_enrolment/{staffId}") {
        fun createRoute(staffId: Long) = "face_enrolment/$staffId"
    }

    object StaffProfile : Screen("staff_profile/{staffId}") {
        fun createRoute(staffId: Long) = "staff_profile/$staffId"
    }

    companion object {
        const val ARG_STAFF_ID = "staffId"
    }
}
