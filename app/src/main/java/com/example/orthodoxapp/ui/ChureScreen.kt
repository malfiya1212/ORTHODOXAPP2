package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.orthodoxapp.data.model.ChureGroup
import com.example.orthodoxapp.data.model.UserRole
import com.example.orthodoxapp.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChureScreen(viewModel: FinancialViewModel, onBack: () -> Unit = {}, onNavigate: (String) -> Unit = {}, modifier: Modifier = Modifier) {
    val groups by viewModel.groups.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val isMember = currentRole == UserRole.MEMBER
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Mutual Aid", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
                        Text("Chure System", fontWeight = FontWeight.Bold, color = PureLinen)
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
            if (!isMember) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = OrthodoxGold,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Group")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (isMember) {
                    MemberChureSummaryCard()
                } else {
                    ChureSummaryCard(groups.size)
                }
            }

            item {
                Text(if (isMember) "Available & Joined Groups" else "Active Chure Groups", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
            }

            if (groups.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No groups active. Create one to start.", color = TextSecondary)
                    }
                }
            } else {
                items(groups) { group ->
                    ChureGroupCard(group = group, isMember = isMember, onRecordPayment = { onNavigate("global_payment") })
                }
            }
        }
    }

    if (showAddDialog) {
        AddChureGroupDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, amount, frequency ->
                viewModel.createChureGroup(name, amount, frequency, currentUser?.churchId ?: 1L)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChureGroupDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Monthly") }
    var expanded by remember { mutableStateOf(false) }
    val frequencies = listOf("Weekly", "Bi-Weekly", "Monthly")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Chure Group") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Group Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Contribution Amount (ETB)") }, modifier = Modifier.fillMaxWidth())
                
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = { _ -> },
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        frequencies.forEach { f ->
                            DropdownMenuItem(text = { Text(f) }, onClick = { frequency = f; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, amount.toDoubleOrNull() ?: 0.0, frequency) }, enabled = name.isNotEmpty() && amount.isNotEmpty()) {
                Text("Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ChureSummaryCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF4CAF50))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("System Health", color = Color.Gray, fontSize = 12.sp)
                Text("$count Active Groups", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("Collection Rate: 94%", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MemberChureSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = PrimaryBlue.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Groups, contentDescription = null, tint = PrimaryBlue)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("My Participation", color = Color.Gray, fontSize = 12.sp)
                Text("1 Active Group", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("Next Payout: Nov 15", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ChureGroupCard(group: ChureGroup, isMember: Boolean = false, onRecordPayment: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(group.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryBlue)
                Surface(
                    color = Color(0xFFFF9800).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        group.frequency,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color(0xFFFF9800),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Contribution", fontSize = 12.sp, color = Color.Gray)
                    Text("${String.format(Locale.getDefault(), "%,.0f", group.contributionAmount)} ETB", fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Pool", fontSize = 12.sp, color = Color.Gray)
                    Text("${String.format(java.util.Locale.getDefault(), "%,.0f", group.contributionAmount * 12)} ETB", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { 0.65f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = PrimaryBlue,
                trackColor = PrimaryBlue.copy(alpha = 0.1f)
            )
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Paid: 12/18 members", fontSize = 11.sp, color = Color.Gray)
                Text("65% Collected", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }

            if (expanded) {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(Modifier.height(12.dp))
                
                if (isMember) {
                    Text("My Schedule & Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Next Payment Due:", fontSize = 13.sp, color = Color.Gray)
                        Text("Oct 30, 2026", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("My Next Payout:", fontSize = 13.sp, color = Color.Gray)
                        Text("Jan 15, 2027", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onRecordPayment,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Join Group / Make Payment")
                    }
                } else {
                    Text("Payment Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    
                    // Mock Member List for the Group
                    listOf("Abebe Bekele" to true, "Sara Tekle" to true, "Kebede Haile" to false).forEach { (name, paid) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name, fontSize = 13.sp)
                            if (paid) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                            } else {
                                TextButton(onClick = onRecordPayment, contentPadding = PaddingValues(0.dp)) {
                                    Text("Record Payment", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                tint = Color.Gray
            )
        }
    }
}
