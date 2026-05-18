package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.data.model.*
import com.example.orthodoxapp.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChurchDetailScreen(
    viewModel: FinancialViewModel,
    churchId: Long,
    onBack: () -> Unit
) {
    val churches by viewModel.churches.collectAsState()
    val church = churches.find { it.id == churchId }
    
    val members by viewModel.users.collectAsState()
    val churchMembers = members.filter { it.churchId == churchId }
    
    val income by viewModel.income.collectAsState()
    val churchIncome = income.filter { it.churchId == churchId }
    
    val expenses by viewModel.expenses.collectAsState()
    val churchExpenses = expenses.filter { it.churchId == churchId }

    var memberToEdit by remember { mutableStateOf<User?>(null) }
    if (memberToEdit != null) {
        AlertDialog(
            onDismissRequest = { memberToEdit = null },
            title = { Text("Update Member Status") },
            text = {
                Column {
                    Text("Update status for ${memberToEdit?.name}")
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = memberToEdit?.status == "ACTIVE", onClick = { 
                            val updated = memberToEdit?.copy(status = "ACTIVE")
                            if (updated != null) viewModel.updateUser(updated)
                            memberToEdit = null
                        })
                        Text("Active")
                        Spacer(Modifier.width(16.dp))
                        RadioButton(selected = memberToEdit?.status == "INACTIVE", onClick = { 
                            val updated = memberToEdit?.copy(status = "INACTIVE")
                            if (updated != null) viewModel.updateUser(updated)
                            memberToEdit = null
                        })
                        Text("Inactive")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { memberToEdit = null }) { Text("Close") } }
        )
    }

    val currentUser by viewModel.currentUser.collectAsState()
    val isMember = viewModel.currentRole.collectAsState().value == UserRole.MEMBER
    val isAlreadyJoined = currentUser?.churchId == churchId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(church?.name ?: "Church Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isMember && !isAlreadyJoined) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Join this Parish", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryBlue)
                            Text("Officially connect to this church to receive updates and record contributions.", textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    currentUser?.let { user ->
                                        viewModel.updateUser(user.copy(churchId = churchId))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AddHome, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Become a Member", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Parish Information", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        Spacer(Modifier.height(8.dp))
                        InfoRow("Location", church?.location ?: "N/A")
                        InfoRow("Status", church?.status ?: "ACTIVE")
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FinancialStatCard("Total Income", churchIncome.sumOf { it.amount }, Color(0xFF10B981), Modifier.weight(1f))
                    FinancialStatCard("Total Expenses", churchExpenses.sumOf { it.amount }, Color(0xFFEF4444), Modifier.weight(1f))
                }
            }

            item {
                Text("Registered Members (${churchMembers.size})", fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }

            items(churchMembers) { member ->
                val memberContribution = churchIncome.filter { it.createdBy == member.id }.sumOf { it.amount }
                MemberFinancialCard(member = member, contribution = memberContribution, onEditClick = { memberToEdit = member })
            }
        }
    }
}

@Composable
fun MemberFinancialCard(member: User, contribution: Double, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEditClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = PrimaryBlue.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        member.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        fontSize = 18.sp
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (member.status == "ACTIVE") Color(0xFF10B981).copy(alpha = 0.1f) else Color(0xFFEF4444).copy(alpha = 0.1f)
                    ) {
                        Text(
                            member.status,
                            fontSize = 10.sp,
                            color = if (member.status == "ACTIVE") Color(0xFF10B981) else Color(0xFFEF4444),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(member.email, fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Contribution", fontSize = 10.sp, color = Color.Gray)
                Text(
                    "${String.format(java.util.Locale.getDefault(), "%,.0f", contribution)} ETB",
                    fontWeight = FontWeight.Bold,
                    color = if (contribution > 0) Color(0xFF10B981) else Color.Gray,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Member", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FinancialStatCard(label: String, amount: Double, color: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text("${String.format(java.util.Locale.getDefault(), "%,.0f", amount)} ETB", color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
