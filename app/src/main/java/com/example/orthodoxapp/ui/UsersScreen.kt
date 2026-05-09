package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.data.model.User
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    val users by viewModel.users.collectAsState()
    var showAddUserDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Member Management", fontWeight = FontWeight.Bold, color = PureLinen)
                        Text("Parish Registry", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = PureLinen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrthodoxBlue)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddUserDialog = true },
                containerColor = OrthodoxGold,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Member")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight)
        ) {
            var searchQuery by remember { mutableStateOf("") }
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                placeholder = { Text("Search members by name or email...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OrthodoxBlue) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrthodoxBlue,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.4f),
                    unfocusedContainerColor = SurfaceWhite,
                    focusedContainerColor = SurfaceWhite
                )
            )

            val filteredUsers = if (searchQuery.isEmpty()) users else {
                users.filter { 
                    it.name.contains(searchQuery, ignoreCase = true) || 
                    it.email.contains(searchQuery, ignoreCase = true) 
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredUsers) { user ->
                    UserCard(user)
                }
            }
        }
    }

    val currentUser by viewModel.currentUser.collectAsState()

    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { name, email, roleName ->
                val roleId = when(roleName) {
                    "Super Admin" -> 1L
                    "Diocese Admin" -> 2L
                    "Church Admin" -> 5L
                    "Accountant" -> 6L
                    "Auditor" -> 7L
                    else -> 9L // Member
                }
                
                viewModel.addUser(
                    name = name,
                    email = email,
                    roleId = roleId,
                    churchId = currentUser?.churchId
                )
                showAddUserDialog = false
            }
        )
    }
}

@Composable
fun UserCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = OrthodoxBlue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        user.name.take(1).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        color = OrthodoxBlue,
                        fontSize = 22.sp
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text(user.email, fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val roleName = when(user.roleId) {
                        1L -> "Super Admin"
                        2L -> "Diocese Admin"
                        3L -> "Church Admin"
                        4L -> "Accountant"
                        5L -> "Auditor"
                        else -> "Member"
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when(user.roleId) {
                            1L -> ErrorRed.copy(alpha = 0.1f)
                            2L, 3L -> OrthodoxGold.copy(alpha = 0.1f)
                            else -> InfoBlue.copy(alpha = 0.1f)
                        }
                    ) {
                        Text(
                            text = roleName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = when(user.roleId) {
                                1L -> ErrorRed
                                2L, 3L -> OrthodoxGoldDark
                                else -> InfoBlue
                            }
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = { /* View Contributions */ }) {
                    Icon(Icons.Default.Payments, contentDescription = "View Contributions", tint = SuccessGreen)
                }
                Text("Contributions", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Member") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New User") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                
                var expandedRole by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { expandedRole = !expandedRole }
                ) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                        listOf("Member", "Church Admin", "Accountant", "Auditor", "Diocese Admin", "Super Admin").forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = { selectedRole = role; expandedRole = false }
                            )
                        }
                    }
                }

            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, email, selectedRole) },
                colors = ButtonDefaults.buttonColors(containerColor = OrthodoxBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Register Member")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
