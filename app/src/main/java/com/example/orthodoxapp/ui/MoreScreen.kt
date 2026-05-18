package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.orthodoxapp.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavController, viewModel: FinancialViewModel, modifier: Modifier = Modifier) {
    val currentRole by viewModel.currentRole.collectAsState()

    val allItems = remember(currentRole) {
        val list = mutableListOf<MenuItem>()
        
        // 1. User Management (Admins only)
        if (currentRole in listOf(UserRole.SYNOD_ADMIN, UserRole.DIOCESE_ADMIN, UserRole.CHURCH_ADMIN)) {
            list.add(MenuItem("User Management", Icons.Default.People, route = Screen.Users.route))
        }

        // 2. Role & Permission (Synod Admin only)
        if (currentRole == UserRole.SYNOD_ADMIN) {
            list.add(MenuItem("Role & Permission", Icons.Default.Security, route = Screen.Roles.route))
        }

        // 3. Organization Management (Diocese and above)
        if (currentRole in listOf(UserRole.SYNOD_ADMIN, UserRole.DIOCESE_ADMIN)) {
            list.add(MenuItem("Organization Management", Icons.Default.AccountTree, route = Screen.OrgManagement.route))
        }

        // 4. Approval Center (Admins only)
        if (currentRole in listOf(UserRole.SYNOD_ADMIN, UserRole.DIOCESE_ADMIN, UserRole.CHURCH_ADMIN)) {
            list.add(MenuItem("Approval Center", Icons.Default.CheckCircle, route = Screen.Approvals.route))
        }

        // 5. Shared Modules
        list.add(MenuItem("Notifications", Icons.Default.Notifications, route = Screen.Notifications.route))
        list.add(MenuItem("Documents", Icons.Default.Folder, route = Screen.Documents.route))
        list.add(MenuItem("System Settings", Icons.Default.Settings, route = Screen.Settings.route))
        
        // 6. Logout (Always visible)
        list.add(MenuItem("Logout", Icons.AutoMirrored.Filled.Logout, color = Color.Red, route = Screen.Login.route))
        
        list
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("System Modules", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F3D89))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            items(allItems) { item -> 
                MoreItem(item) { 
                    item.route?.let { route ->
                        if (item.title == "Logout") {
                            viewModel.logout()
                            navController.navigate(route) { popUpTo(0) { inclusive = true } }
                        } else {
                            navController.navigate(route)
                        }
                    }
                } 
            }
        }
    }
}


data class MenuItem(val title: String, val icon: ImageVector, val color: Color = Color.Black, val isSwitch: Boolean = false, val route: String? = null)

@Composable
fun MoreItem(item: MenuItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(item.title, color = item.color, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        if (item.isSwitch) {
            var checked by remember { mutableStateOf(false) }
            Switch(checked = checked, onCheckedChange = { checked = it })
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
