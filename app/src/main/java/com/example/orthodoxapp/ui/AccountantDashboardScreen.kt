package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.ui.components.SimpleDonutChart
import com.example.orthodoxapp.ui.components.AdminStatCard
import com.example.orthodoxapp.ui.theme.*
import com.example.orthodoxapp.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountantDashboardScreen(viewModel: FinancialViewModel, onNavigate: (String) -> Unit = {}, modifier: Modifier = Modifier) {
    val incomeRecords: List<Income> by viewModel.income.collectAsState(initial = emptyList())
    val expenseRecords: List<Expense> by viewModel.expenses.collectAsState(initial = emptyList())
    
    val totalIncome = incomeRecords.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val totalExpense = expenseRecords.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Parish Financials", style = MaterialTheme.typography.labelMedium, color = OrthodoxGold)
                        Text("Accountant Dashboard", fontWeight = FontWeight.Bold, color = PureLinen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrthodoxBlue),
                actions = {
                    IconButton(onClick = { /* Export PDF */ }) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export", tint = PureLinen)
                    }
                    IconButton(onClick = { onNavigate("notifications") }) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = PureLinen)
                    }
                    IconButton(onClick = { 
                        viewModel.logout()
                        onNavigate("login") 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = PureLinen)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Stats Row
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminStatCard("Available Fund", "ETB ${String.format("%,.0f", netBalance)}", Icons.Default.AccountBalance, OrthodoxBlue, Modifier.weight(1f))
                    AdminStatCard("Compliance", "100%", Icons.AutoMirrored.Filled.FactCheck, SuccessGreen, Modifier.weight(1f))
                }
            }

            // Quick Tools
            item {
                Text("Verification & Reporting", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AccountantToolCard("Ledgers", Icons.AutoMirrored.Filled.ReceiptLong, InfoBlue, Modifier.weight(1f)) { onNavigate("transactions") }
                    AccountantToolCard("Tithe Map", Icons.Default.Map, OrthodoxGold, Modifier.weight(1f)) { onNavigate("reports") }
                    AccountantToolCard("Certify", Icons.Default.Verified, SuccessGreen, Modifier.weight(1f)) { onNavigate("approvals") }
                }
            }

            // Donut Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Expenditure Ratio", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.Start))
                        Spacer(Modifier.height(24.dp))
                        SimpleDonutChart(
                            data = listOf(totalIncome.toFloat(), totalExpense.toFloat()),
                            colors = listOf(SuccessGreen, ErrorRed),
                            modifier = Modifier.size(160.dp),
                            centerText = "${if (totalIncome > 0) (totalExpense/totalIncome * 100).toInt() else 0}%",
                            centerSubText = "Spent"
                        )
                    }
                }
            }

            // Recent Items
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent Entries", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    TextButton(onClick = { onNavigate("transactions") }) { Text("Review All") }
                }
            }

            items(incomeRecords.take(3)) { item ->
                EntryRow(item.source, item.amount, "INCOME", item.status)
            }
            items(expenseRecords.take(3)) { item ->
                EntryRow(item.recipient ?: "Church Expense", item.amount, "EXPENSE", item.status)
            }
        }
    }
}

@Composable
fun AccountantToolCard(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

@Composable
fun EntryRow(title: String, amount: Double, type: String, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val color = if (type == "INCOME") SuccessGreen else ErrorRed
            Icon(
                if (type == "INCOME") Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = color
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text(status, fontSize = 11.sp, color = TextSecondary)
            }
            Text(
                "${if(type == "INCOME") "+" else "-"} ${String.format("%,.0f", amount)}",
                fontWeight = FontWeight.Bold,
                color = color,
                fontSize = 15.sp
            )
        }
    }
}
