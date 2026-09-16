package com.demo.attendancepro.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.demo.attendancepro.ui.screens.addstaff.AddStaffScreen
import com.demo.attendancepro.ui.screens.faceenrolment.FaceEnrolmentScreen
import com.demo.attendancepro.ui.screens.login.LoginScreen
import com.demo.attendancepro.ui.screens.markattendance.MarkAttendanceScreen
import com.demo.attendancepro.ui.screens.staffprofile.StaffProfileScreen
import com.demo.attendancepro.ui.screens.staffslist.StaffListScreen
import com.demo.attendancepro.util.Role

@Composable
fun AttendanceNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role ->
                    val destination = if (role == Role.ADMIN) Screen.StaffList.route else Screen.MarkAttendance.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.StaffList.route) {
            StaffListScreen(
                onAddStaff = { navController.navigate(Screen.AddStaff.route) },
                onStaffClick = { staffId -> navController.navigate(Screen.StaffProfile.createRoute(staffId)) }
            )
        }

        composable(Screen.AddStaff.route) {
            AddStaffScreen(
                onBack = { navController.popBackStack() },
                onStaffCreated = { staffId ->
                    navController.navigate(Screen.FaceEnrolment.createRoute(staffId)) {
                        popUpTo(Screen.StaffList.route)
                    }
                }
            )
        }

        composable(
            route = Screen.FaceEnrolment.route,
            arguments = listOf(navArgument(Screen.ARG_STAFF_ID) { type = NavType.LongType })
        ) {
            FaceEnrolmentScreen(
                onBack = { navController.popBackStack(Screen.StaffList.route, inclusive = false) },
                onEnrolmentComplete = { navController.popBackStack(Screen.StaffList.route, inclusive = false) }
            )
        }

        composable(
            route = Screen.StaffProfile.route,
            arguments = listOf(navArgument(Screen.ARG_STAFF_ID) { type = NavType.LongType })
        ) {
            StaffProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.MarkAttendance.route) {
            MarkAttendanceScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.MarkAttendance.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
