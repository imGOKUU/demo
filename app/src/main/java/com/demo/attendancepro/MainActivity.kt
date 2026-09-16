package com.demo.attendancepro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.demo.attendancepro.navigation.AttendanceNavGraph
import com.demo.attendancepro.ui.theme.AttendanceProTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AttendanceProTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AttendanceNavGraph()
                }
            }
        }
    }
}
