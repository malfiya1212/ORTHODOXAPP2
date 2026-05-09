package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
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
    
    // In a real app, we would fetch scoped data for this church
    // For now, we filter existing flows or use mocks if needed
    val members by viewModel.users.collectAsState()
    val churchMembers = members.filter { it.churchId == churchId }
    
    val income by viewModel.income.collectAsState()
    val churchIncome = income.filter { it.churchId == churchId }
    
    val expenses by viewModel.expenses.collectAsState()
    val churchExpenses = expenses.filter { it.churchId == churchId }

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
        if (church == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Church not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FE)),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Church, contentDescription = null, tint = PrimaryBlue)
                                Spacer(Modifier.width(12.dp))
                                Text("Basic Information", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            HorizontalDivider(Modifier.padding(vertical = 12.dp))
                            InfoRow("Location", church.location ?: "Not specified")
                            InfoRow("Status", church.status)
                            InfoRow("Members Registered", churchMembers.size.toString())
                        }
                    }
                }

                // Financial Overview
                item {
                    Text("Financial Summary", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        FinancialStatCard("Total Income", churchIncome.sumOf { it.amount }, Color(0xFF10B981), Modifier.weight(1f))
                        FinancialStatCard("Total Expense", churchExpenses.sumOf { it.amount }, Color(0xFFEF4444), Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    FinancialStatCard(
                        label = "Net Balance", 
                        amount = churchIncome.sumOf { it.amount } - churchExpenses.sumOf { it.amount }, 
                        color = PrimaryBlue, 
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Member Contributions Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Member Financial Activity", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Surface(
                            color = PrimaryBlue.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Total: ${churchMembers.size}", 
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (churchMembers.isEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White
                        ) {
                            Text("No members registered for this church", modifier = Modifier.padding(24.dp), color = Color.Gray)
                        }
                    }
                } else {
                    items(churchMembers) { member ->
                        val memberContribution = churchIncome.filter { it.createdBy == member.id }.sumOf { it.amount }
                        MemberFinancialCard(member, memberContribution)
                    }
                }
            }
        }
    }
}

@Composable
fun MemberFinancialCard(member: User, contribution: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Text(member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Text(member.email, fontSize = 12.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Contribution", fontSize = 10.sp, color = Color.Gray)
                Text(
                    "${String.format("%,.0f", contribution)} ETB",
                    fontWeight = FontWeight.Bold,
                    color = if (contribution > 0) Color(0xFF10B981) else Color.Gray,
                    fontSize = 14.sp
                )
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
            Text("${String.format("%,.0f", amount)} ETB", color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
