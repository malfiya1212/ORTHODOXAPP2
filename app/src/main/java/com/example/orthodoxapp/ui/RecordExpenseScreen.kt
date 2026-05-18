package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.orthodoxapp.ui.theme.*
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordExpenseScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Maintenance") }
    var expanded by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    val categories = listOf("Salary", "Maintenance", "Charity")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Expense", fontWeight = FontWeight.Bold, color = PureLinen) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = PureLinen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrthodoxBlue)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Approval Warning
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = WarningOrange.copy(alpha = 0.1f)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = WarningOrange)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "This expense will be submitted for approval. Funds will not be released until approved.",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                }
            }

            val currentUser = viewModel.currentUser.value
            val userChurch = viewModel.churches.value.find { it.id == currentUser?.churchId }
            val churchName = userChurch?.name ?: "General Parish"

            Text("Expense Details for $churchName", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (ETB)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = ErrorRed) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
            )

            // Category Selection
            Column {
                Text("Category", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { _ -> },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null, tint = OrthodoxBlue) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Date Selection
            var showDatePicker by remember { mutableStateOf(false) }
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
            val formattedDate = remember(datePickerState.selectedDateMillis) {
                val millis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(millis))
            }

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Column {
                Text("Date", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = formattedDate,
                    onValueChange = { _ -> },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    enabled = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = OrthodoxBlue) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrthodoxBlue,
                        unfocusedBorderColor = OrthodoxBlue
                    )
                )
            }

            // Description
            Column {
                Text("Description", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Recipient or Purpose") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = OrthodoxBlue) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                )
            }

            val generatedRef = remember { "EXP-${System.currentTimeMillis().toString().takeLast(8)}" }

            // System Generated Reference
            Column {
                Text("Payment Reference Number", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = generatedRef,
                    onValueChange = { _ -> },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = OrthodoxBlue) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrthodoxBlue,
                        unfocusedBorderColor = OrthodoxBlue.copy(alpha = 0.5f),
                        unfocusedTextColor = OrthodoxBlue,
                        focusedTextColor = OrthodoxBlue
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                )
                Text("This reference number replaces a physical receipt. Please save it for your records.", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (amount.isEmpty() || description.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    } else {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (amt <= 0) {
                            Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
                        } else {
                            val currentUser = viewModel.currentUser.value
                            val churchId = currentUser?.churchId ?: 1L
                            viewModel.addExpense(amt, recipient, 1L, churchId, selectedCategory, description, paymentMethod = "Cash", referenceNumber = generatedRef)
                            Toast.makeText(context, "Expense submitted for approval! Ref: $generatedRef", Toast.LENGTH_LONG).show()
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrthodoxBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("✔ Save Expense", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PureLinen)
            }
        }
    }
}
