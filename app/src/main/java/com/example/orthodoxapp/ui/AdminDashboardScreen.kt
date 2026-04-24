package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.ui.theme.PrimaryBlue
import com.example.orthodoxapp.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: FinancialViewModel, modifier: Modifier = Modifier) {
    val transactions by viewModel.transactions.collectAsState()
    val incomeRecords by viewModel.income.collectAsState()
    val expenseRecords by viewModel.expenses.collectAsState()
    val churches by viewModel.churches.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val role by viewModel.currentRole.collectAsState()
    
    val canApprove = role == UserRole.SUPER_ADMIN || role == UserRole.REGIONAL_ADMIN || role == UserRole.CHURCH_ADMIN
    
    val pendingIncome = incomeRecords.filter { it.status == "PENDING" }
    val pendingExpenses = expenseRecords.filter { it.status == "PENDING" }
    
    val totalIncome = incomeRecords.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val totalExpense = expenseRecords.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("National System", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        Text("National Dashboard", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F3D89)),
                actions = {
                    IconButton(onClick = {}) {
                        BadgedBox(badge = { if (pendingIncome.size + pendingExpenses.size > 0) Badge { Text("${pendingIncome.size + pendingExpenses.size}") } }) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            )
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
            // Summary Cards
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminStatCard(
                        label = "Total Balance",
                        value = "${String.format("%,.2f", balance)} ETB",
                        icon = Icons.Default.AccountBalanceWallet,
                        color = Color(0xFF3F3D89),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminStatCard(
                        label = "Income (Approved)",
                        value = "${String.format("%,.2f", totalIncome)}",
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = "Expense (Approved)",
                        value = "${String.format("%,.2f", totalExpense)}",
                        icon = Icons.Default.TrendingDown,
                        color = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminStatCard(
                        label = "Total Regions",
                        value = "12", // Mocked for national scale
                        icon = Icons.Default.Public,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCard(
                        label = "Active Churches",
                        value = "${churches.size}",
                        icon = Icons.Default.Church,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Regional Performance Summary
            item {
                Text("Regional Performance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        RegionalPerformanceRow("Addis Ababa", 0.85f, Color(0xFF4CAF50))
                        Spacer(Modifier.height(12.dp))
                        RegionalPerformanceRow("Amhara", 0.65f, Color(0xFFFF9800))
                        Spacer(Modifier.height(12.dp))
                        RegionalPerformanceRow("Oromia", 0.45f, Color(0xFFF44336))
                    }
                }
            }

            // Fraud & Security Alerts
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF44336))
                    Spacer(Modifier.width(8.dp))
                    Text("Security & Fraud Alerts", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFF44336))
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFF44336))
                        Spacer(Modifier.width(12.dp))
                        Text("3 high-value transactions pending approval for over 48h", fontSize = 14.sp, color = Color(0xFFB71C1C))
                    }
                }
            }

            // Pending Approvals Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Pending Approvals", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("${pendingIncome.size + pendingExpenses.size} items", color = Color.Gray, fontSize = 14.sp)
                }
            }

            if (pendingIncome.isEmpty() && pendingExpenses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(Modifier.padding(32.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("All caught up! No pending approvals.", color = Color.Gray)
                        }
                    }
                }
            } else {
                items(pendingIncome) { record ->
                    ApprovalItem(
                        title = "Income: ${record.source}",
                        amount = record.amount,
                        type = "INCOME",
                        date = record.date,
                        showActions = canApprove,
                        onApprove = { viewModel.approveIncome(record.id) },
                        onReject = { viewModel.rejectIncome(record.id) }
                    )
                }
                items(pendingExpenses) { record ->
                    ApprovalItem(
                        title = "Expense: ${record.recipient}",
                        amount = record.amount,
                        type = "EXPENSE",
                        date = record.date,
                        showActions = canApprove,
                        onApprove = { viewModel.approveExpense(record.id) },
                        onReject = { viewModel.rejectExpense(record.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp).size(24.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun ApprovalItem(title: String, amount: Double, type: String, date: Long, showActions: Boolean, onApprove: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = (if (type == "INCOME") Color(0xFF4CAF50) else Color(0xFFF44336)).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        if (type == "INCOME") Icons.Default.Add else Icons.Default.Remove,
                        contentDescription = null,
                        tint = if (type == "INCOME") Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.padding(8.dp).size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${String.format("%,.2f", amount)} ETB", color = if (type == "INCOME") Color(0xFF4CAF50) else Color(0xFFF44336), fontWeight = FontWeight.Bold)
                }
            }
            if (showActions) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reject")
                    }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Approve")
                    }
                }
            }
        }
    }
}
@Composable
fun RegionalPerformanceRow(region: String, progress: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(region, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("${(progress * 100).toInt()}% reporting", fontSize = 12.sp, color = Color.Gray)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}
