package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.orthodoxapp.ui.theme.*
import com.example.orthodoxapp.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    var category by remember { mutableStateOf("Donation") }
    var amount by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("Sunday Collection") }
    var date by remember { mutableStateOf("May 15, 2024") }
    
    val liveRates by viewModel.exchangeRates.collectAsState()
    val currencies = remember(liveRates) {
        listOf(
            Currency("ETB", "🇪🇹", 1.0),
            Currency("USD", "🇺🇸", liveRates["USD"] ?: 124.50),
            Currency("EUR", "🇪🇺", liveRates["EUR"] ?: 132.80),
            Currency("GBP", "🇬🇧", liveRates["GBP"] ?: 156.20)
        )
    }
    var selectedCurrency by remember(currencies) { mutableStateOf(currencies.first()) }
    
    val currentUser by viewModel.currentUser.collectAsState()
    val referenceNumber by remember { mutableStateOf("INC-${System.currentTimeMillis().toString().takeLast(8)}") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Income", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F3D89))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Dropdown Placeholder
            Text("Category", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(0.4f)) {
                    Text("Currency", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    var currencyExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = !currencyExpanded }
                    ) {
                        OutlinedTextField(
                            value = "${selectedCurrency.flag} ${selectedCurrency.code}",
                            onValueChange = { _ -> },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                        )
                        ExposedDropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                            currencies.forEach { c ->
                                DropdownMenuItem(text = { Text("${c.flag} ${c.code}") }, onClick = { selectedCurrency = c; currencyExpanded = false })
                            }
                        }
                    }
                }
                Column(modifier = Modifier.weight(0.6f)) {
                    val enteredAmt = amount.toDoubleOrNull() ?: 0.0
                    Text("Amount (${selectedCurrency.code})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("0.00") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = OrthodoxBlue) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    if (selectedCurrency.code != "ETB" && enteredAmt > 0) {
                        Text(
                            "≈ ETB ${String.format("%,.2f", enteredAmt * selectedCurrency.rateToEtb)}",
                            fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                        )
                    }
                }
            }

            Text("Source", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            OutlinedTextField(
                value = source,
                onValueChange = { source = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text("Date", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            Text("Reference Number (System Generated)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            OutlinedTextField(
                value = referenceNumber,
                onValueChange = { _ -> },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Text("Receipt (Optional)", fontWeight = FontWeight.Medium, fontSize = 14.sp)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                onClick = {}
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    Text("Upload Receipt", color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amtRaw = amount.toDoubleOrNull() ?: 0.0
                    if (amtRaw > 0) {
                        val finalAmount = if (selectedCurrency.code == "ETB") amtRaw else amtRaw * selectedCurrency.rateToEtb
                        viewModel.addIncome(
                            amount = finalAmount,
                            source = source,
                            accountId = 1L, // Default account
                            churchId = currentUser?.churchId ?: 1L,
                            category = category,
                            description = "Manual Entry",
                            referenceNumber = referenceNumber,
                            originalAmount = amtRaw,
                            originalCurrency = selectedCurrency.code
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F3D89)),
                enabled = amount.isNotEmpty() && source.isNotEmpty()
            ) {
                Text("SAVE INCOME", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
