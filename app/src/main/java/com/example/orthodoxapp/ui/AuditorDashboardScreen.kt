package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.ui.components.AdminStatCard
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditorDashboardScreen(viewModel: FinancialViewModel, onNavigate: (String) -> Unit = {}, modifier: Modifier = Modifier) {
    val auditLogs by viewModel.auditLogs.collectAsState()
    val incomeRecords by viewModel.income.collectAsState()
    val expenseRecords by viewModel.expenses.collectAsState()
    
    val totalRecords = incomeRecords.size + expenseRecords.size + auditLogs.size

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Integrity & Oversight", style = MaterialTheme.typography.labelMedium, color = OrthodoxGold)
                        Text("Auditor Dashboard", fontWeight = FontWeight.Bold, color = PureLinen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A)), // Deep midnight for serious audit feel
                actions = {
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
            // Stats
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminStatCard("Audit Events", "${auditLogs.size}", Icons.Default.Security, Color(0xFF6366F1), Modifier.weight(1f))
                    AdminStatCard("Total Records", "$totalRecords", Icons.AutoMirrored.Filled.FactCheck, SuccessGreen, Modifier.weight(1f))
                }
            }

            // Tools
            item {
                Text("Verification Systems", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AuditToolCard("System Logs", Icons.Default.History, Color(0xFF0F172A), Modifier.weight(1f)) { onNavigate("audit_logs") }
                    AuditToolCard("Balance Sheets", Icons.Default.Description, Color(0xFF6366F1), Modifier.weight(1f)) { onNavigate("reports") }
                    AuditToolCard("Verify Ledgers", Icons.AutoMirrored.Filled.PlaylistAddCheck, SuccessGreen, Modifier.weight(1f)) { onNavigate("transactions") }
                }
            }

            // Feed
            item {
                Text("Recent System Activity", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            }

            if (auditLogs.isEmpty()) {
                item {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = SurfaceWhite) {
                        Text("No audit logs found in the ledger.", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(24.dp))
                    }
                }
            } else {
                items(auditLogs.take(10)) { log ->
                    AuditorLogItem(log)
                }
            }
            
            item {
                Button(
                    onClick = { onNavigate("transactions") },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Comprehensive Financial Review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AuditToolCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = SurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

@Composable
fun AuditorLogItem(log: com.example.orthodoxapp.data.model.AuditLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(SuccessGreen, androidx.compose.foundation.shape.CircleShape))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(log.action, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text("Ref: #${log.recordId} | Table: ${log.tableName}", fontSize = 11.sp, color = TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}
