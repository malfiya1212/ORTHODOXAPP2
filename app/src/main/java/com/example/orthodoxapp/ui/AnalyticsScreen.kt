package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.orthodoxapp.ui.theme.PrimaryBlue
import com.example.orthodoxapp.ui.components.SimpleBarChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("System Analytics", fontWeight = FontWeight.Bold, color = Color.White) },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FE)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Performance Graph
            item {
                Text("Income vs Expense Patterns", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                        SimpleBarChart(
                            data = listOf(0.3f, 0.5f, 0.7f, 0.4f, 0.8f, 0.6f),
                            labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
                            barColor = PrimaryBlue
                        )
                    }
                }
            }

            // Fraud Detection / Security Alerts
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFF44336))
                    Spacer(Modifier.width(8.dp))
                    Text("Fraud Detection & Anomalies", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFF44336))
                }
            }

            item {
                AlertItem(
                    title = "Potential Duplicate Entry",
                    description = "Two transactions of 5,000 ETB recorded within 2 minutes at St. Mary Church.",
                    severity = "MEDIUM"
                )
            }

            item {
                AlertItem(
                    title = "Abnormal Amount Alert",
                    description = "Expense of 150,000 ETB for 'Maintenance' is 300% higher than monthly average.",
                    severity = "HIGH"
                )
            }

            // Church Performance Ranking
            item {
                Text("Top Performing Churches", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RankingItem("1. St. George Cathedral", "+24% Growth", Color(0xFF4CAF50))
                    RankingItem("2. Holy Trinity Church", "+18% Growth", Color(0xFF4CAF50))
                    RankingItem("3. St. Mary Parish", "+15% Growth", Color(0xFF4CAF50))
                }
            }
        }
    }
}

@Composable
fun AlertItem(title: String, description: String, severity: String) {
    val color = if (severity == "HIGH") Color(0xFFF44336) else Color(0xFFFF9800)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
            }
            Text(description, fontSize = 12.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun RankingItem(name: String, stat: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, fontWeight = FontWeight.Medium)
            Text(stat, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}
