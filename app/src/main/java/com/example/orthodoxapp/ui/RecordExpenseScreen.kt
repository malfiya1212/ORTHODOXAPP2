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

            Text("Expense Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)

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
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
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
            Column {
                Text("Date", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = "Today's Date",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = OrthodoxBlue) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
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

            // Receipt Upload Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SurfaceWhite,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Receipt",
                            modifier = Modifier.size(120.dp).padding(bottom = 8.dp)
                        )
                    } else {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(48.dp), tint = OrthodoxBlue)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Attach Receipt / Invoice", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Proof of expense is required for approval", fontSize = 12.sp, color = TextSecondary)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text(if (selectedImageUri == null) "Choose File" else "Change File")
                    }
                }
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
                            viewModel.addExpense(amt, recipient, 1L, 1L, selectedCategory, description)
                            Toast.makeText(context, "Expense submitted for approval!", Toast.LENGTH_LONG).show()
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
