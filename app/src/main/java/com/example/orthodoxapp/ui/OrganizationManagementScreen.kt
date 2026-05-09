package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Church
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.example.orthodoxapp.data.model.UserRole
import com.example.orthodoxapp.data.model.Diocese
import com.example.orthodoxapp.data.model.Church

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationManagementScreen(
    viewModel: FinancialViewModel, 
    onBack: () -> Unit, 
    onAddClick: (String?) -> Unit,
    onEditClick: (String, Long) -> Unit
) {
    val churches by viewModel.churches.collectAsState(initial = emptyList())
    val dioceses by viewModel.dioceses.collectAsState(initial = emptyList())
    val role by viewModel.currentRole.collectAsState()
    
    val tabs = remember(role) {
        if (role == UserRole.SYNOD_ADMIN) listOf("Dioceses", "Churches") else listOf("Churches")
    }
    var selectedTab by remember { mutableStateOf(0) }

    var showAdminDialog by remember { mutableStateOf(false) }
    var targetingChurchId by remember { mutableStateOf<Long?>(null) }
    var targetingChurchName by remember { mutableStateOf("") }
    
    var adminName by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }

    if (showAdminDialog) {
        AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            title = { Text("Assign Admin for $targetingChurchName") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = adminName, onValueChange = { adminName = it }, label = { Text("Admin Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = adminEmail, onValueChange = { adminEmail = it }, label = { Text("Admin Email") }, modifier = Modifier.fillMaxWidth())
                    Text("Password will be set to 'church123' by default.", fontSize = 11.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = {
                    targetingChurchId?.let { id ->
                        viewModel.addChurchAdmin(id, adminName, adminEmail)
                        showAdminDialog = false
                    }
                }) { Text("Confirm Assignment") }
            },
            dismissButton = { TextButton(onClick = { showAdminDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Church Hierarchy Management", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F3D89))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    val currentType = when(tabs.getOrElse(selectedTab) { "" }) {
                        "Dioceses" -> "Diocese"
                        "Churches" -> "Church"
                        else -> null
                    }
                    onAddClick(currentType) 
                },
                containerColor = Color(0xFF3F3D89),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            if (tabs.size > 1) {
                TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = Color(0xFF3F3D89)) {
                    tabs.forEachIndexed { index, title ->
                        Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val currentTabTitle = tabs.getOrElse(selectedTab) { "" }
                when (currentTabTitle) {
                    "Dioceses" -> {
                        items(dioceses) { diocese ->
                            OrgItemCard(
                                title = diocese.name, 
                                subtitle = "Bishop: ${diocese.bishopName ?: "Not Assigned"}", 
                                icon = Icons.Default.Public,
                                onEdit = { onEditClick("Diocese", diocese.id) },
                                onClick = { selectedTab = tabs.indexOf("Churches") }
                            )
                        }
                    }
                    "Churches" -> {
                        items(churches) { church ->
                            val parentDiocese = dioceses.find { it.id == church.dioceseId }?.name ?: "Unknown"
                            OrgItemCard(
                                title = church.name, 
                                subtitle = "Diocese: $parentDiocese", 
                                icon = Icons.Default.Church,
                                onEdit = { onEditClick("Church", church.id) },
                                showAdminAction = role == UserRole.SYNOD_ADMIN || role == UserRole.DIOCESE_ADMIN,
                                onAdminClick = { 
                                    targetingChurchId = church.id
                                    targetingChurchName = church.name
                                    showAdminDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrgItemCard(
    title: String, 
    subtitle: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    showAdminAction: Boolean = false,
    onAdminClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    var isActive by remember { mutableStateOf(true) }
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isActive) Color.White else Color(0xFFEEEEEE))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = (if (isActive) Color(0xFF3F3D89) else Color.Gray).copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = if (isActive) Color(0xFF3F3D89) else Color.Gray, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isActive) Color.Black else Color.Gray)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF3F3D89))
                }
                
                if (showAdminAction && isActive) {
                    IconButton(onClick = onAdminClick) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Assign Admin", tint = Color(0xFF3F3D89))
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = isActive, 
                    onCheckedChange = { isActive = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF3F3D89))
                )
                Text(if (isActive) "Active" else "Inactive", fontSize = 10.sp, color = if (isActive) Color(0xFF3F3D89) else Color.Gray)
            }
        }
    }
}
