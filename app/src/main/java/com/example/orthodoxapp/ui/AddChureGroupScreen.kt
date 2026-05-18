package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.orthodoxapp.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddChureGroupScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var contribution by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf("Monthly") }
    var expanded by remember { mutableStateOf(false) }
    
    val frequencies = listOf("Weekly", "Bi-Weekly", "Monthly")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Chure Group", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FE))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Group Configuration", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Group Name") },
                placeholder = { Text("e.g. Sunday Morning Chure") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null, tint = PrimaryBlue) }
            )

            OutlinedTextField(
                value = contribution,
                onValueChange = { contribution = it },
                label = { Text("Contribution Amount (ETB)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = PrimaryBlue) }
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedFrequency,
                    onValueChange = { _ -> },
                    readOnly = true,
                    label = { Text("Rotation Frequency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Update, contentDescription = null, tint = PrimaryBlue) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    frequencies.forEach { freq ->
                        DropdownMenuItem(
                            text = { Text(freq) },
                            onClick = {
                                selectedFrequency = freq
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val amount = contribution.toDoubleOrNull() ?: 0.0
                    viewModel.createChureGroup(name, amount, selectedFrequency, 1L) // Mock churchId 1
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Initialize Chure Group", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
