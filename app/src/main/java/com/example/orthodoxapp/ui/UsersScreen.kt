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
            viewModel = viewModel,
            onDismiss = { showAddUserDialog = false },
            onConfirm = { name, email, roleName, churchId, dioceseId ->
                val roleId = when(roleName) {
                    "Super Admin" -> 1L
                    "Diocese Admin" -> 2L
                    "Church Admin" -> 3L // Updated to match getRoleFromId
                    else -> 6L // Member
                }
                
                viewModel.addUser(
                    name = name,
                    email = email,
                    roleId = roleId,
                    churchId = churchId,
                    dioceseId = dioceseId
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
fun AddUserDialog(
    viewModel: FinancialViewModel,
    onDismiss: () -> Unit, 
    onConfirm: (String, String, String, Long?, Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Member") }
    
    val dioceses by viewModel.dioceses.collectAsState()
    val churches by viewModel.churches.collectAsState()
    
    var selectedDioceseId by remember { mutableStateOf<Long?>(null) }
    var selectedChurchId by remember { mutableStateOf<Long?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Official / Member") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                var expandedRole by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { expandedRole = !expandedRole }
                ) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = { _ -> },
                        readOnly = true,
                        label = { Text("Assign Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = expandedRole, onDismissRequest = { expandedRole = false }) {
                        listOf("Member", "Church Admin", "Diocese Admin", "Super Admin").forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = { selectedRole = role; expandedRole = false }
                            )
                        }
                    }
                }

                // Diocese Selector for Diocese Admins or Church-level roles
                if (selectedRole != "Member" && selectedRole != "Super Admin") {
                    var expandedDiocese by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedDiocese,
                        onExpandedChange = { expandedDiocese = !expandedDiocese }
                    ) {
                        OutlinedTextField(
                            value = dioceses.find { it.id == selectedDioceseId }?.name ?: "Select Diocese",
                            onValueChange = { _ -> },
                            readOnly = true,
                            label = { Text("Assign to Diocese") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDiocese) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedDiocese, onDismissRequest = { expandedDiocese = false }) {
                            dioceses.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d.name) },
                                    onClick = { selectedDioceseId = d.id; expandedDiocese = false }
                                )
                            }
                        }
                    }
                }

                // Church Selector for Church-level roles
                if (selectedRole == "Church Admin") {
                    var expandedChurch by remember { mutableStateOf(false) }
                    val filteredChurches = churches.filter { it.dioceseId == selectedDioceseId }
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedChurch,
                        onExpandedChange = { expandedChurch = !expandedChurch }
                    ) {
                        OutlinedTextField(
                            value = filteredChurches.find { it.id == selectedChurchId }?.name ?: "Select Parish",
                            onValueChange = { _ -> },
                            readOnly = true,
                            label = { Text("Assign to Parish") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedChurch) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedChurch, onDismissRequest = { expandedChurch = false }) {
                            filteredChurches.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = { selectedChurchId = c.id; expandedChurch = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, email, selectedRole, selectedChurchId, selectedDioceseId) },
                colors = ButtonDefaults.buttonColors(containerColor = OrthodoxBlue),
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank() && email.isNotBlank()
            ) {
                Text("Register Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
