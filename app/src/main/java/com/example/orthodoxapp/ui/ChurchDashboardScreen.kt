package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChurchDashboardScreen(
    viewModel: FinancialViewModel,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val incomeRecords by viewModel.income.collectAsState()
    val expenseRecords by viewModel.expenses.collectAsState()
    
    val totalIncome = incomeRecords.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val totalExpense = expenseRecords.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense
    val memberCount = 125 // Placeholder
    val pendingRequests = incomeRecords.count { it.status == "PENDING" } + expenseRecords.count { it.status == "PENDING" }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("St. George Church", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        Text("Church Admin Dashboard", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F3D89)),
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SmallFloatingActionButton(
                    onClick = { /* Upload Receipt */ },
                    containerColor = Color.White,
                    contentColor = Color(0xFF3F3D89)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Upload Receipt")
                }
                ExtendedFloatingActionButton(
                    onClick = onAddIncome,
                    containerColor = Color(0xFF3F3D89),
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Record Income") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FE)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Church Stats
            // Daily Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3F3D89))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Current Church Balance", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                        Text("${String.format("%,.2f", balance)} ETB", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Income", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text("${String.format("%,.0f", totalIncome)}", color = Color(0xFF81C784), fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Expenses", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text("${String.format("%,.0f", totalExpense)}", color = Color(0xFFE57373), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminStatCard(
                        label = "Members",
                        value = "$memberCount",
                        icon = Icons.Default.People,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = "Pending",
                        value = "$pendingRequests",
                        icon = Icons.Default.PendingActions,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Actions
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onAddExpense,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Color(0xFFF44336))
                        Spacer(Modifier.width(4.dp))
                        Text("Expense", color = Color.Black, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { /* Submit Report */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(1.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = Color(0xFF3F3D89))
                        Spacer(Modifier.width(4.dp))
                        Text("Report", color = Color.Black, fontSize = 12.sp)
                    }
                }
            }

            // Chure Module Quick View
            item {
                Text("Chure System Status", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = Color(0xFF9C27B0).copy(alpha = 0.1f), shape = CircleShape) {
                                Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF9C27B0), modifier = Modifier.padding(8.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text("Next Payout Receiver", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Abebe Kebede", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Group: Sunday Morning Chure", fontSize = 14.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = 0.75f,
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Color(0xFF9C27B0),
                            trackColor = Color(0xFF9C27B0).copy(alpha = 0.1f)
                        )
                        Text("Collection: 75% complete", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // Recent Member Contributions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Contributions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    TextButton(onClick = {}) { Text("View All") }
                }
            }

            items(incomeRecords.take(5)) { income ->
                MemberContributionItem(income)
            }
        }
    }
}

@Composable
fun MemberContributionItem(income: com.example.orthodoxapp.data.model.Income) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color(0xFF3F3D89).copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(income.source.take(1).uppercase(), fontWeight = FontWeight.Bold, color = Color(0xFF3F3D89))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(income.source, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(income.category ?: "Tithe", fontSize = 12.sp, color = Color.Gray)
            }
            Text(
                "${String.format("%,.0f", income.amount)} ETB",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        }
    }
}
