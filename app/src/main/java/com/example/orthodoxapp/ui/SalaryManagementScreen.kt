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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.data.model.Employee
import com.example.orthodoxapp.data.model.Salary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryManagementScreen(viewModel: FinancialViewModel) {
    val employees by viewModel.employees.collectAsState()
    val salaries by viewModel.salaries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payroll & Staff", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A2E),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF16213E),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add Employee Dialog */ },
                containerColor = Color(0xFF4ECCA3)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Employee")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Staff Registry", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            items(employees) { employee ->
                SalaryEmployeeCard(employee)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Recent Payroll Cycles", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            items(salaries) { salary ->
                SalaryStatusCard(salary)
            }
        }
    }
}

@Composable
fun SalaryEmployeeCard(employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF533483).copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF533483))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(employee.fullName, color = Color.White, fontWeight = FontWeight.Bold)
                Text(employee.role, color = Color.Gray, fontSize = 12.sp)
            }
            Text(
                if (employee.isActive) "ACTIVE" else "INACTIVE",
                color = if (employee.isActive) Color.Green else Color.Red,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SalaryStatusCard(salary: Salary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E).copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Month ${salary.month}, ${salary.year}", color = Color.White, fontWeight = FontWeight.Medium)
                Text("Amount: $${salary.amount}", color = Color.Gray, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .background(
                        when (salary.status) {
                            "PAID" -> Color.Green.copy(alpha = 0.1f)
                            "APPROVED" -> Color.Cyan.copy(alpha = 0.1f)
                            else -> Color.Yellow.copy(alpha = 0.1f)
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    salary.status,
                    color = when (salary.status) {
                        "PAID" -> Color.Green
                        "APPROVED" -> Color.Cyan
                        else -> Color.Yellow
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
