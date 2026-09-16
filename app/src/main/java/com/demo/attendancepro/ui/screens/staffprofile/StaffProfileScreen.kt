package com.demo.attendancepro.ui.screens.staffprofile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.demo.attendancepro.data.local.entity.AttendanceEntity
import com.demo.attendancepro.ui.components.SelfieImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffProfileScreen(
    viewModel: StaffProfileViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.staff?.name ?: "Staff Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            uiState.staff?.let { staff ->
                Text(
                    text = "Employee ID: ${staff.employeeId}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (uiState.attendanceRecords.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No attendance recorded yet.")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(uiState.attendanceRecords, key = { it.id }) { record ->
                        AttendanceRow(record)
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceRow(record: AttendanceEntity) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SelfieImage(path = record.selfiePath, modifier = Modifier.size(72.dp))
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = "${record.date}  ${record.time}", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "Lat: ${"%.5f".format(record.latitude)}, Lng: ${"%.5f".format(record.longitude)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
