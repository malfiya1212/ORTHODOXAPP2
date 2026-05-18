package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.data.model.UserRole
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: FinancialViewModel, onBack: () -> Unit, onNavigate: (String) -> Unit) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    val darkModeEnabled by viewModel.isDarkMode.collectAsState()
    var biometricEnabled by remember { mutableStateOf(false) }
    val currentRole by viewModel.currentRole.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = PureLinen) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = PureLinen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrthodoxBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            SettingsSectionHeader("Account & Profile")
            
            ListItem(
                headlineContent = { Text("Church Profile") },
                supportingContent = { Text("Manage parish identity & details") },
                leadingContent = { Icon(Icons.Default.Church, contentDescription = null, tint = OrthodoxBlue) },
                modifier = Modifier.clickable { /* Profile */ }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Personal Profile") },
                supportingContent = { Text("Manage your personal information") },
                leadingContent = { Icon(Icons.Default.Person, contentDescription = null, tint = OrthodoxBlue) },
                modifier = Modifier.clickable { /* Personal Profile */ }
            )
            HorizontalDivider()
            
            ListItem(
                headlineContent = { Text("Change Password") },
                supportingContent = { Text("Update your security credentials") },
                leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = OrthodoxBlue) },
                modifier = Modifier.clickable { /* Change Password */ }
            )
            HorizontalDivider()

            SettingsSectionHeader("Appearance & Preferences")
            
            ListItem(
                headlineContent = { Text("App Language") },
                supportingContent = { Text("Current: English (አማርኛ available)") },
                trailingContent = { Icon(Icons.Default.Language, contentDescription = null, tint = OrthodoxBlue) },
                modifier = Modifier.clickable { onNavigate("language") }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Push Notifications") },
                supportingContent = { Text("Receive alerts for financial updates") },
                trailingContent = { Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it }) },
                leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null, tint = OrthodoxBlue) }
            )
            HorizontalDivider()

            SettingsSectionHeader("System")
            
            ListItem(
                headlineContent = { Text("Logout") },
                supportingContent = { Text("Securely sign out of your account") },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = ErrorRed) },
                modifier = Modifier.clickable { viewModel.logout() }
            )
            HorizontalDivider()
            
            ListItem(
                headlineContent = { Text("App Version") },
                supportingContent = { Text("1.5.0 (Tewahedo Edition)") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray) }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = OrthodoxBlue,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}
