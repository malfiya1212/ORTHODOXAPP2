package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
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
import com.example.orthodoxapp.data.model.Employee
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonnelScreen(viewModel: FinancialViewModel, onAddPersonnel: () -> Unit = {}, onBack: () -> Unit) {
    val employees by viewModel.employees.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Parish Staffing", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
                        Text("Clergy & Personnel", fontWeight = FontWeight.Bold, color = PureLinen)
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
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = OrthodoxGold) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Personnel", tint = Color.Black)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundLight),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PersonnelSummaryCard(employees.size)
            }
            
            item {
                Text("Ecclesiastical Staff List", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
            }

            if (employees.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No personnel recorded for this location.", color = TextSecondary)
                    }
                }
            } else {
                items(employees) { employee ->
                    PersonnelEmployeeCard(employee)
                }
            }
        }
    }

    if (showAddDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, role, salary, phone ->
                viewModel.addEmployee(name, role, currentUser?.churchId ?: 1L, salary, phone)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmployeeDialog(onDismiss: () -> Unit, onConfirm: (String, String, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Priest") }
    var salary by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Head Priest", "Priest", "Deacon", "Secretary", "Security", "Maintenance")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Personnel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { _ -> },
                        readOnly = true,
                        label = { Text("Ecclesiastical Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        roles.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { role = r; expanded = false })
                        }
                    }
                }

                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Monthly Salary (ETB)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, role, salary.toDoubleOrNull() ?: 0.0, phone) }, enabled = name.isNotEmpty() && salary.isNotEmpty()) {
                Text("Register")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun PersonnelSummaryCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = OrthodoxBlueDark),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = OrthodoxGold.copy(alpha = 0.2f), modifier = Modifier.size(56.dp)) {
                Icon(Icons.Default.Groups, contentDescription = null, tint = OrthodoxGold, modifier = Modifier.padding(14.dp))
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text("Total Personnel", color = PureLinen.copy(alpha = 0.6f), fontSize = 12.sp)
                Text("$count Active Members", color = PureLinen, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text("Payroll status: Current", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PersonnelEmployeeCard(employee: Employee) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = OrthodoxBlue.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = when(employee.role) {
                        "Priest", "Head Priest" -> Icons.Default.AccountBalance
                        "Deacon" -> Icons.Default.Face
                        else -> Icons.Default.Person
                    },
                    contentDescription = null,
                    tint = OrthodoxBlue,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(employee.fullName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                Text(employee.role, color = TextSecondary, fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("ETB ${String.format("%,.0f", employee.salary)}", fontWeight = FontWeight.ExtraBold, color = OrthodoxBlue)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SuccessGreen.copy(alpha = 0.1f)
                ) {
                    Text(
                        "PAID",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                }
            }
        }
    }
}
