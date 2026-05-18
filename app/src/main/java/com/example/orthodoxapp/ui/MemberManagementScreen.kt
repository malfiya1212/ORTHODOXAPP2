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
import com.example.orthodoxapp.ui.theme.*
import com.example.orthodoxapp.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberManagementScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMemberForCert by remember { mutableStateOf<User?>(null) }

    val users by viewModel.users.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    val members = users.filter { it.roleId == 6L && it.churchId == currentUser?.churchId }

    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Member") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    currentUser?.churchId?.let { cid ->
                        viewModel.addUser(newName, newEmail, 6L, cid)
                    }
                    showAddDialog = false
                }) {
                    Text("Add Member")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (selectedMemberForCert != null) {
        var certTitle by remember { mutableStateOf("") }
        var certType by remember { mutableStateOf("Service") }
        var certDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedMemberForCert = null },
            title = { Text("Issue Certificate to ${selectedMemberForCert?.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = certTitle,
                        onValueChange = { certTitle = it },
                        label = { Text("Award Title (e.g. Baptism Certificate)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = certType,
                        onValueChange = { certType = it },
                        label = { Text("Category (Service, Baptism, etc.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = certDesc,
                        onValueChange = { certDesc = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.issueCertificate(selectedMemberForCert!!.id, certTitle, certType, certDesc)
                        selectedMemberForCert = null
                    },
                    enabled = certTitle.isNotBlank() && certType.isNotBlank()
                ) {
                    Text("Issue Award")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedMemberForCert = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Member & Tithe Registry", fontWeight = FontWeight.Bold, color = PureLinen)
                        Text("Parishioner Management", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
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
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = OrthodoxGold, contentColor = Color.Black) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Member")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundLight)) {
            Surface(color = OrthodoxBlue, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by name...", color = PureLinen.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PureLinen) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = PureLinen.copy(alpha = 0.3f),
                            focusedBorderColor = OrthodoxGold,
                            cursorColor = OrthodoxGold,
                            focusedTextColor = PureLinen,
                            unfocusedTextColor = PureLinen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Parish Members", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                }

                items(members.filter { it.name.contains(searchQuery, ignoreCase = true) }) { member ->
                    MemberRow(member, onAward = { selectedMemberForCert = member })
                }
            }
        }
    }
}

@Composable
fun MemberRow(member: User, onAward: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = OrthodoxBlue.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(member.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = OrthodoxBlue, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text(member.email, fontSize = 11.sp, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val isActive = member.status == "ACTIVE"
                Surface(
                    color = if (isActive) SuccessGreen.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (isActive) "Active" else "Inactive",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = if (isActive) SuccessGreen else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = onAward, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = "Award Certificate", tint = OrthodoxGoldDark, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
