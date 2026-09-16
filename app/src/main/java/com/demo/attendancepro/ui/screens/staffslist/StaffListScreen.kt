package com.demo.attendancepro.ui.screens.staffslist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.demo.attendancepro.data.local.entity.StaffEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffListScreen(
    viewModel: StaffListViewModel = hiltViewModel(),
    onAddStaff: () -> Unit,
    onStaffClick: (Long) -> Unit
) {
    val staffList by viewModel.staffList.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Staff") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddStaff) {
                Icon(Icons.Filled.Add, contentDescription = "Add staff")
            }
        }
    ) { padding ->
        if (staffList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No staff yet. Tap + to add one.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(staffList, key = { it.id }) { staff ->
                    StaffRow(staff = staff, onClick = { onStaffClick(staff.id) })
                }
            }
        }
    }
}

@Composable
private fun StaffRow(staff: StaffEntity, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null)
            }
        },
        headlineContent = { Text(staff.name) },
        supportingContent = { Text("ID: ${staff.employeeId}") },
        trailingContent = {
            if (staff.faceEmbedding != null) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Enrolled",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Text("Not enrolled", style = MaterialTheme.typography.bodySmall)
            }
        }
    )
}
