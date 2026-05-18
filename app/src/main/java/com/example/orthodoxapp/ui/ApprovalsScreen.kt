package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.data.model.*
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApprovalsScreen(viewModel: FinancialViewModel, onBack: () -> Unit, onNavigate: (String) -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val incomeList by viewModel.income.collectAsState(initial = emptyList())
    val expenseList by viewModel.expenses.collectAsState(initial = emptyList())
    
    val pendingIncome = incomeList.filter { it.status == "PENDING" }
    val pendingExpenses = expenseList.filter { it.status == "PENDING" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Financial Approvals", fontWeight = FontWeight.Bold, color = PureLinen)
                        Text("Verify and authorize records", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = PureLinen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrthodoxBlue)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundLight)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = OrthodoxBlue,
                contentColor = PureLinen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = OrthodoxGold
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Income (${pendingIncome.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Expenses (${pendingExpenses.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                PendingIncomeList(pendingIncome, onApprove = { viewModel.approveIncome(it) })
            } else {
                PendingExpenseList(pendingExpenses, onApprove = { viewModel.approveExpense(it) })
            }
        }
    }
}

@Composable
fun PendingIncomeList(items: List<Income>, onApprove: (Long) -> Unit) {
    if (items.isEmpty()) {
        EmptyState("No pending income records.")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { income ->
                ApprovalCard(
                    title = income.source,
                    amount = "Birr ${income.amount}",
                    category = income.category ?: "Tithe",
                    date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(income.date),
                    onApprove = { onApprove(income.id) },
                    onReject = { /* Implement Reject */ },
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
fun PendingExpenseList(items: List<Expense>, onApprove: (Long) -> Unit) {
    if (items.isEmpty()) {
        EmptyState("No pending expense requests.")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { expense ->
                ApprovalCard(
                    title = expense.recipient ?: "Parish Expense",
                    amount = "Birr ${expense.amount}",
                    category = expense.category,
                    date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(expense.date),
                    onApprove = { onApprove(expense.id) },
                    onReject = { /* Implement Reject */ },
                    color = ErrorRed
                )
            }
        }
    }
}

@Composable
fun ApprovalCard(title: String, amount: String, category: String, date: String, onApprove: () -> Unit, onReject: () -> Unit, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text(category, fontSize = 12.sp, color = TextSecondary)
                }
                Text(amount, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, size = 14.dp, tint = TextSecondary)
                Spacer(Modifier.width(4.dp))
                Text(date, fontSize = 12.sp, color = TextSecondary)
            }
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                ) {
                    Text("Reject")
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrthodoxBlue)
                ) {
                    Text("Approve")
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(Modifier.height(16.dp))
            Text(message, color = TextSecondary)
        }
    }
}

@Composable
private fun Icon(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    androidx.compose.material3.Icon(icon, contentDescription, modifier = Modifier.size(size), tint = tint)
}
