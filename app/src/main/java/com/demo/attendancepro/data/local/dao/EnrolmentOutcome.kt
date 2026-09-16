package com.demo.attendancepro.data.local.dao

sealed class EnrolmentOutcome {
    object Saved : EnrolmentOutcome()
    object AlreadyEnrolled : EnrolmentOutcome()
    object StaffNotFound : EnrolmentOutcome()
}