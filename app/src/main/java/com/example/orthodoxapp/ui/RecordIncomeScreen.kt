package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.example.orthodoxapp.data.model.*
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordIncomeScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    val currentRole by viewModel.currentRole.collectAsState()
    val isMember = currentRole == UserRole.MEMBER

    var amount by remember { mutableStateOf("") }
    val liveRates by viewModel.exchangeRates.collectAsState()
    val currencies = remember(liveRates) {
        listOf(
            com.example.orthodoxapp.data.model.Currency("ETB", "🇪🇹", 1.0),
            com.example.orthodoxapp.data.model.Currency("USD", "🇺🇸", liveRates["USD"] ?: 124.50),
            com.example.orthodoxapp.data.model.Currency("EUR", "🇪🇺", liveRates["EUR"] ?: 132.80),
            com.example.orthodoxapp.data.model.Currency("GBP", "🇬🇧", liveRates["GBP"] ?: 156.20)
        )
    }
    var selectedCurrency by remember(currencies) { mutableStateOf<com.example.orthodoxapp.data.model.Currency>(currencies.first()) }
    
    var source by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tithe") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var referenceNumber by remember { mutableStateOf("REC-${System.currentTimeMillis().toString().takeLast(6)}") }
    var description by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }
    
    val categories = listOf("Tithe", "Donation", "Offering", "Asset Sale", "Other")
    val paymentMethods = listOf("Cash", "Bank Transfer", "Mobile Banking")
    val ethiopianBanks = listOf(
        "Commercial Bank of Ethiopia (CBE)", "Awash Bank", "Dashen Bank", 
        "Bank of Abyssinia", "Hibret Bank", "United Bank", "Nib Bank", 
        "Wegagen Bank", "Zemen Bank", "Cooperative Bank of Oromia", 
        "Bunna Bank", "Berhan Bank", "Abay Bank", "Addis International Bank", 
        "Enat Bank", "Oromia International Bank", "Global Bank Ethiopia", "Amhara Bank"
    )
    val mobilePlatforms = listOf("telebirr", "M-PESA", "CBE Birr", "Amole", "HelloCash")

    var selectedBank by remember { mutableStateOf("") }
    var selectedMobilePlatform by remember { mutableStateOf("") }
    var bankExpanded by remember { mutableStateOf(false) }
    var mobileExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isMember) "Contribute" else "Record Parish Income", fontWeight = FontWeight.Bold, color = PureLinen) },
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = OrthodoxGold.copy(alpha = 0.05f)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = OrthodoxGold)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        if (isMember) "Your contribution helps our parish grow and serve the community." 
                        else "Ensure all tithes are recorded accurately for church accountability.",
                        fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium
                    )
                }
            }

            // Amount Input
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
                            shape = RoundedCornerShape(16.dp),
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
                        shape = RoundedCornerShape(16.dp),
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

            // Source Input
            if (!isMember) {
                Column {
                    Text("Member Name / Source", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = source,
                        onValueChange = { source = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("Search or enter member name") },
                        leadingIcon = { Icon(Icons.Default.PersonSearch, contentDescription = null, tint = OrthodoxBlue) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                    )
                }
            } else {
                LaunchedEffect(Unit) { source = "Self (Member)" }
            }

            // Category Selection
            Column {
                Text("Income Category", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
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
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null, tint = OrthodoxBlue) },
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

            // Payment Method Selection
            Column {
                Text("Payment Method", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = methodExpanded,
                    onExpandedChange = { methodExpanded = !methodExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { _ -> },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, tint = OrthodoxBlue) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                    )
                    ExposedDropdownMenu(
                        expanded = methodExpanded,
                        onDismissRequest = { methodExpanded = false }
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    paymentMethod = method
                                    methodExpanded = false
                                }
                            )
                        }
                    }
                }

                // Sub-selection for Bank
                if (paymentMethod == "Bank Transfer") {
                    Spacer(Modifier.height(16.dp))
                    Text("Select Bank", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = bankExpanded,
                        onExpandedChange = { bankExpanded = !bankExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedBank,
                            onValueChange = { _ -> },
                            readOnly = true,
                            placeholder = { Text("Choose Ethiopian Bank") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = OrthodoxBlue) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                        )
                        ExposedDropdownMenu(
                            expanded = bankExpanded,
                            onDismissRequest = { bankExpanded = false }
                        ) {
                            ethiopianBanks.forEach { bank ->
                                DropdownMenuItem(
                                    text = { Text(bank) },
                                    onClick = {
                                        selectedBank = bank
                                        bankExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Sub-selection for Mobile Banking
                if (paymentMethod == "Mobile Banking") {
                    Spacer(Modifier.height(16.dp))
                    Text("Select Platform", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = mobileExpanded,
                        onExpandedChange = { mobileExpanded = !mobileExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedMobilePlatform,
                            onValueChange = { _ -> },
                            readOnly = true,
                            placeholder = { Text("Choose Mobile Service") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mobileExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(16.dp),
                            leadingIcon = { Icon(Icons.Default.Smartphone, contentDescription = null, tint = OrthodoxBlue) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                        )
                        ExposedDropdownMenu(
                            expanded = mobileExpanded,
                            onDismissRequest = { mobileExpanded = false }
                        ) {
                            mobilePlatforms.forEach { platform ->
                                DropdownMenuItem(
                                    text = { Text(platform) },
                                    onClick = {
                                        selectedMobilePlatform = platform
                                        mobileExpanded = false
                                    }
                                )
                            }
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
                Box(modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }) {
                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = { _ -> },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false, // Disabled to prevent keyboard, but click is handled by Box
                        shape = RoundedCornerShape(16.dp),
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = OrthodoxBlue) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = OrthodoxBlue,
                            disabledTextColor = TextPrimary,
                            disabledLeadingIconColor = OrthodoxBlue,
                            disabledPlaceholderColor = TextSecondary
                        )
                    )
                }
            }

            // Description
            Column {
                Text("Description", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Note about this income...") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = OrthodoxBlue) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                )
            }

            // Reference Number
            Column {
                Text("Reference / Receipt Number", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = referenceNumber,
                    onValueChange = { referenceNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Enter Reference (Mandatory)") },
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = OrthodoxGold) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                )
                Text("All entries must include a verifiable reference ID.", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (amount.isEmpty() || source.isEmpty() || referenceNumber.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields including Reference", Toast.LENGTH_SHORT).show()
                    } else {
                        val amtRaw = amount.toDoubleOrNull() ?: 0.0
                        if (amtRaw <= 0) {
                            Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
                        } else {
                            val finalAmount = if (selectedCurrency.code == "ETB") amtRaw else amtRaw * selectedCurrency.rateToEtb
                            val finalMethod = when (paymentMethod) {
                                "Bank Transfer" -> if (selectedBank.isNotEmpty()) "Bank ($selectedBank)" else "Bank Transfer"
                                "Mobile Banking" -> if (selectedMobilePlatform.isNotEmpty()) "Mobile ($selectedMobilePlatform)" else "Mobile Banking"
                                else -> paymentMethod
                            }
                            val currentUser = viewModel.currentUser.value
                            val churchId = currentUser?.churchId ?: 1L
                            
                            val finalDescription = if (selectedCurrency.code != "ETB") {
                                "$description (Original: $amtRaw ${selectedCurrency.code})"
                            } else description

                            viewModel.addIncome(
                                amount = finalAmount,
                                source = source,
                                accountId = 1L,
                                churchId = churchId,
                                category = selectedCategory,
                                description = finalDescription,
                                paymentMethod = finalMethod,
                                referenceNumber = referenceNumber,
                                originalAmount = amtRaw,
                                originalCurrency = selectedCurrency.code
                            )
                            Toast.makeText(context, "Successfully Recorded! Ref: $referenceNumber", Toast.LENGTH_LONG).show()
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrthodoxBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("✔ Save Income", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PureLinen)
            }
        }
    }
}
