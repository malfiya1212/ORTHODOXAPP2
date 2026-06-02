package com.example.orthodoxapp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.orthodoxapp.data.network.PaymentInitResponse
import com.example.orthodoxapp.data.network.PaymentVerificationResponse
import com.example.orthodoxapp.ui.theme.*
import com.example.orthodoxapp.data.model.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalPaymentScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    val donationTypes = listOf("Tithe (አስራት)", "Building Fund (ህንጻ)", "Monthly Payment (ወርሃዊ)", "Vow (ስለት)", "St. George (ቅዱስ ጊዮርጊስ)", "St. Mary (ማርያም)", "Offering (ቁርባን)", "Other (ሌላ)")
    var selectedDonationType by remember { mutableStateOf(donationTypes.first()) }
    
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
    var amountInput by remember { mutableStateOf("") }
    var userRefInput by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    
    val localMethods = listOf(
        PaymentMethod("Telebirr", "Mobile Money", Icons.Default.AccountBalance, Color(0xFF0066FF)),
        PaymentMethod("CBE Birr", "Mobile Money", Icons.Default.AccountBalance, Color(0xFF800080)),
        PaymentMethod("Chapa", "Direct Payment", Icons.Default.FlashOn, Color(0xFFE11D48)),
        PaymentMethod("M-PESA", "Mobile Money", Icons.Default.Smartphone, Color(0xFF00B140)),
        PaymentMethod("Amole", "Dashen Bank", Icons.Default.AccountBalance, Color(0xFFEAB308)),
        PaymentMethod("SantimPay", "Gateway", Icons.Default.Payments, Color(0xFF3B82F6)),
        PaymentMethod("YenePay", "Gateway", Icons.Default.Wallet, Color(0xFFF97316)),
        PaymentMethod("Bank Transfer", "Ethiopian Banks", Icons.Default.AccountBalance, Color(0xFFB45309))
    )

    val internationalMethods = listOf(
        PaymentMethod("PayPal", "Global Secure", Icons.Default.Language, Color(0xFF003087)),
        PaymentMethod("International Card", "Visa / Mastercard", Icons.Default.CreditCard, Color(0xFF10B981)),
        PaymentMethod("Stripe Checkout", "Global Gateway", Icons.Default.Payments, Color(0xFF6366F1))
    )
    
    val currentMethods = if (selectedCurrency.code == "ETB") localMethods else internationalMethods
    var selectedMethod by remember(selectedCurrency) { mutableStateOf<PaymentMethod>(currentMethods.first()) }
    
    val ethiopianBanks = listOf(
        "Commercial Bank of Ethiopia (CBE)",
        "Awash International Bank",
        "Dashen Bank",
        "Bank of Abyssinia",
        "United Bank (Hibret Bank)",
        "Wegagen Bank",
        "Nib International Bank",
        "Zemen Bank",
        "Oromia International Bank",
        "Cooperative Bank of Oromia",
        "Bunna International Bank",
        "Berhan International Bank",
        "Enat Bank",
        "Addis International Bank",
        "Amhara Bank",
        "Global Bank Ethiopia",
        "Abay Bank",
        "Debub Global Bank",
        "Lion International Bank",
        "Tsehay Bank",
        "Ahadu Bank",
        "Siinqee Bank",
        "Shabelle Bank"
    )
    var selectedBank by remember { mutableStateOf(ethiopianBanks.first()) }
    var showBankSelector by remember { mutableStateOf(false) }
    var generatedRef by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        generatedRef = "REF-${System.currentTimeMillis().toString().takeLast(8)}"
    }
    
    val pipelineSteps = listOf(
        "Initiating Secure Handshake",
        "Validating Identity",
        "Encryption of Payload",
        "Gateway Authorization",
        "Ledger Reconciliation",
        "Final Receipt Generation"
    )
    var currentStep by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var isPayPalFlowActive by remember { mutableStateOf(false) }
    var isCardFlowActive by remember { mutableStateOf(false) }
    var isStripeFlowActive by remember { mutableStateOf(false) }
    var payPalTransactionId by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Sacred Giving / Donation", color = PureLinen, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureLinen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrthodoxBlue)
            )
        },
        bottomBar = {
            if (!isProcessing && !showSuccess && !isPayPalFlowActive) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = SurfaceWhite
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        val enteredAmount = amountInput.toDoubleOrNull() ?: 0.0
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Total Amount", fontSize = 12.sp, color = TextSecondary)
                                Text("${selectedCurrency.code} ${String.format("%,.2f", enteredAmount)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = OrthodoxBlue)
                            }
                            
                            Button(
                                onClick = {
                                    if (selectedMethod.name == "PayPal") {
                                        isPayPalFlowActive = true
                                    } else if (selectedMethod.name == "Stripe Checkout") {
                                        isStripeFlowActive = true
                                    } else if (selectedMethod.name == "International Card") {
                                        isCardFlowActive = true
                                    } else {
                                        // Trigger standard flow
                                        isProcessing = true
                                        currentStep = 0
                                        scope.launch {
                                            val finalRef = if (userRefInput.isNotBlank()) userRefInput else generatedRef
                                            val convertedAmount = if (selectedCurrency.code == "ETB") enteredAmount else enteredAmount * selectedCurrency.rateToEtb
                                            
                                            viewModel.addIncome(
                                                amount = convertedAmount, 
                                                source = "${selectedMethod.name} ($selectedDonationType)", 
                                                accountId = 1L, 
                                                churchId = viewModel.currentUser.value?.churchId ?: 1L,
                                                category = if (selectedDonationType.contains("Tithe")) "Tithe" else "Offering",
                                                description = "Method: ${selectedMethod.name}. Ref: $finalRef",
                                                paymentMethod = selectedMethod.name,
                                                referenceNumber = finalRef,
                                                originalAmount = enteredAmount,
                                                originalCurrency = selectedCurrency.code
                                            ) 
                                            
                                            repeat(6) {
                                                delay(500)
                                                currentStep++
                                            }
                                            isProcessing = false
                                            showSuccess = true
                                        }
                                    }
                                },
                                modifier = Modifier.height(56.dp).padding(start = 16.dp).weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (selectedMethod.name) {
                                        "PayPal" -> Color(0xFF003087)
                                        "Stripe Checkout" -> Color(0xFF635BFF)
                                        "International Card" -> Color(0xFF10B981)
                                        else -> OrthodoxBlue
                                    }
                                ),
                                enabled = enteredAmount > 0 && userRefInput.isNotBlank()
                            ) {
                                if (selectedMethod.name == "PayPal") {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Pay with ", fontWeight = FontWeight.Bold)
                                        Text("PayPal", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyLarge.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                                    }
                                } else if (selectedMethod.name == "Stripe Checkout") {
                                    Text("Pay with Stripe", fontWeight = FontWeight.Bold)
                                } else if (selectedMethod.name == "International Card") {
                                    Text("Proceed to Secure Pay", fontWeight = FontWeight.Bold)
                                } else {
                                    Text("Submit Payment", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isPayPalFlowActive) {
            // ... (PayPal logic remains)
            PayPalSimulatedFlow(
                amount = amountInput.toDoubleOrNull() ?: 0.0,
                currency = selectedCurrency.code,
                onComplete = { txId ->
                    // ...
                    payPalTransactionId = txId
                    userRefInput = txId
                    isPayPalFlowActive = false
                    isProcessing = true
                    currentStep = 0
                    scope.launch {
                        val convertedAmount = (amountInput.toDoubleOrNull() ?: 0.0) * selectedCurrency.rateToEtb
                        viewModel.addIncome(
                            amount = convertedAmount,
                            source = "PayPal ($selectedDonationType)",
                            accountId = 1L,
                            churchId = viewModel.currentUser.value?.churchId ?: 1L,
                            category = if (selectedDonationType.contains("Tithe")) "Tithe" else "Offering",
                            description = "PayPal Transaction ID: $txId",
                            paymentMethod = "PayPal",
                            referenceNumber = txId,
                            originalAmount = amountInput.toDoubleOrNull() ?: 0.0,
                            originalCurrency = selectedCurrency.code
                        )
                        repeat(6) { delay(400); currentStep++ }
                        isProcessing = false
                        showSuccess = true
                    }
                },
                onCancel = { isPayPalFlowActive = false }
            )
        } else if (isCardFlowActive) {
            CardCheckoutSimulatedFlow(
                amount = amountInput.toDoubleOrNull() ?: 0.0,
                currency = selectedCurrency.code,
                onComplete = { txId ->
                    userRefInput = txId
                    isCardFlowActive = false
                    isProcessing = true
                    currentStep = 0
                    scope.launch {
                        val convertedAmount = (amountInput.toDoubleOrNull() ?: 0.0) * selectedCurrency.rateToEtb
                        viewModel.addIncome(
                            amount = convertedAmount,
                            source = "Card ($selectedDonationType)",
                            accountId = 1L,
                            churchId = viewModel.currentUser.value?.churchId ?: 1L,
                            category = if (selectedDonationType.contains("Tithe")) "Tithe" else "Offering",
                            description = "Card Transaction ID: $txId",
                            paymentMethod = "Card",
                            referenceNumber = txId,
                            originalAmount = amountInput.toDoubleOrNull() ?: 0.0,
                            originalCurrency = selectedCurrency.code
                        )
                        repeat(6) { delay(400); currentStep++ }
                        isProcessing = false
                        showSuccess = true
                    }
                },
                onCancel = { isCardFlowActive = false }
            )
        } else if (isStripeFlowActive) {
            StripeCheckoutSimulatedFlow(
                amount = amountInput.toDoubleOrNull() ?: 0.0,
                currency = selectedCurrency.code,
                donationType = selectedDonationType,
                onComplete = { txId ->
                    userRefInput = txId
                    isStripeFlowActive = false
                    isProcessing = true
                    currentStep = 0
                    scope.launch {
                        val convertedAmount = (amountInput.toDoubleOrNull() ?: 0.0) * selectedCurrency.rateToEtb
                        viewModel.addIncome(
                            amount = convertedAmount,
                            source = "Stripe ($selectedDonationType)",
                            accountId = 1L,
                            churchId = viewModel.currentUser.value?.churchId ?: 1L,
                            category = if (selectedDonationType.contains("Tithe")) "Tithe" else "Offering",
                            description = "Stripe Session: $txId",
                            paymentMethod = "Stripe",
                            referenceNumber = txId,
                            originalAmount = amountInput.toDoubleOrNull() ?: 0.0,
                            originalCurrency = selectedCurrency.code
                        )
                        repeat(6) { delay(400); currentStep++ }
                        isProcessing = false
                        showSuccess = true
                    }
                },
                onCancel = { isStripeFlowActive = false }
            )
        } else if (showSuccess) {
            PaymentSuccessScreen(generatedRef, onBack)
        } else if (isProcessing) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundLight)
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = OrthodoxBlue, modifier = Modifier.size(48.dp), strokeWidth = 4.dp)
                Spacer(Modifier.height(24.dp))
                Text("Securing Your Contribution", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(32.dp))

                pipelineSteps.forEachIndexed { index, step ->
                    val isDone = index < currentStep
                    val isCurrent = index == currentStep
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isDone) Icons.Default.CheckCircle
                            else if (isCurrent) Icons.Default.RadioButtonChecked
                            else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isDone) SuccessGreen
                                   else if (isCurrent) OrthodoxBlue
                                   else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            step,
                            fontSize = 13.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDone) SuccessGreen
                                   else if (isCurrent) OrthodoxBlue
                                   else Color.LightGray
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundLight)
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                val enteredAmount = amountInput.toDoubleOrNull() ?: 0.0

                // Security Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SuccessGreen.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Secure SSL-Encrypted Transaction", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }
                }

                // 1. Donation Purpose
                Column {
                    Text("Donation Purpose", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(donationTypes) { type: String ->
                            val isSelected = selectedDonationType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDonationType = type },
                                label = { Text(type) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrthodoxGold,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }

                // 2. Select Currency & Enter Amount
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(0.4f)) {
                        Text("Currency", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Box {
                            var expanded by remember { mutableStateOf(false) }
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("${selectedCurrency.flag} ${selectedCurrency.code}")
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                currencies.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.flag} ${c.code}") },
                                        onClick = { selectedCurrency = c; expanded = false }
                                    )
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(0.6f)) {
                        Text("Enter Amount", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { input: String -> amountInput = input },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text("0.00") },
                            suffix = { Text(selectedCurrency.code) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                        
                        if (selectedCurrency.code != "ETB" && enteredAmount > 0) {
                            val convertedAmount = enteredAmount * selectedCurrency.rateToEtb
                            Text(
                                text = "≈ ETB ${String.format("%,.2f", convertedAmount)}",
                                fontSize = 12.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }
                }

                // 3. Payment Method
                Column {
                    Text("Select Payment Method", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Spacer(Modifier.height(12.dp))
                    
                    currentMethods.forEach { method ->
                        val isSelected = selectedMethod == method
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { selectedMethod = method },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) method.brandColor.copy(alpha = 0.05f) else SurfaceWhite,
                            border = BorderStroke(2.dp, if (isSelected) method.brandColor else Color.Transparent),
                            shadowElevation = if (isSelected) 0.dp else 2.dp
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = method.brandColor.copy(alpha = 0.1f), modifier = Modifier.size(52.dp)) {
                                    Icon(method.icon, contentDescription = null, tint = method.brandColor, modifier = Modifier.padding(14.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(method.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(method.region, fontSize = 12.sp, color = TextSecondary)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.RadioButtonChecked, contentDescription = null, tint = method.brandColor)
                                }
                            }
                        }
                    }
                }

                // 3.5 Bank Selector (Conditional)
                // 3.5 Method-Specific Inputs
                if (selectedMethod.name == "Bank Transfer" || selectedMethod.name == "CBE Birr" || selectedMethod.name == "Telebirr" || selectedMethod.name == "Amole" || selectedMethod.name == "M-PESA") {
                    Column {
                        Text(if (selectedMethod.name == "Bank Transfer") "Select Your Bank" else "Source Account / Service", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showBankSelector = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, OrthodoxBlue.copy(alpha = 0.5f))
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(selectedBank, color = TextPrimary, modifier = Modifier.weight(1f))
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = OrthodoxBlue)
                            }
                        }
                        
                        if (showBankSelector) {
                            AlertDialog(
                                onDismissRequest = { showBankSelector = false },
                                title = { Text("Ethiopian Banks") },
                                text = {
                                    Column(modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 400.dp)) {
                                        ethiopianBanks.forEach { bank ->
                                            Text(
                                                text = bank,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { 
                                                        selectedBank = bank
                                                        showBankSelector = false 
                                                    }
                                                    .padding(16.dp),
                                                color = if (selectedBank == bank) OrthodoxBlue else TextPrimary,
                                                fontWeight = if (selectedBank == bank) FontWeight.Bold else FontWeight.Normal
                                            )
                                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showBankSelector = false }) { Text("Close") }
                                }
                            )
                        }
                    }
                } else if (selectedMethod.name == "International Card" || selectedMethod.name == "Stripe Checkout") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = InfoBlue.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, InfoBlue.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Secure Card Checkout", fontWeight = FontWeight.Bold, color = InfoBlue)
                            Text("Your card details are handled exclusively by our secure PCI-compliant gateway.", fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else if (selectedMethod.name == "Chapa") {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE11D48).copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Fast Secure Payment via Chapa", fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                            Text("You will be redirected to the secure Chapa payment gateway.", fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                }

                // 4. Reference Number (MANDATORY for all)
                Column {
                    Text("Transaction Reference", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text("Enter the unique ID / Reference from your payment provider", fontSize = 12.sp, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = userRefInput,
                        onValueChange = { input: String -> userRefInput = input },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("Reference Number") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                    )
                }

                // Summary and System Reference
                Card(
                    colors = CardDefaults.cardColors(containerColor = OrthodoxGold.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, OrthodoxGold.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Amount to Pay:", fontSize = 14.sp)
                            Text("${selectedCurrency.code} ${String.format("%,.2f", enteredAmount)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = OrthodoxBlue)
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = OrthodoxGold.copy(alpha = 0.1f))
                        Spacer(Modifier.height(12.dp))
                        Text("Payment Instruction:", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Please use the code below as the 'Reason' or 'Remark' in your transfer for verification.", fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = OrthodoxBlue.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                generatedRef, 
                                modifier = Modifier.padding(12.dp), 
                                fontSize = 20.sp, 
                                fontWeight = FontWeight.ExtraBold, 
                                color = OrthodoxBlue, 
                                textAlign = TextAlign.Center,
                                letterSpacing = 2.sp
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = OrthodoxBlue, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Need help? Contact Parish Treasury: +251 911...", fontSize = 11.sp, color = OrthodoxBlue)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Remove the old button from here as it's now in the bottomBar
            }
        }
    }
}

@Composable
fun PayPalSimulatedFlow(
    amount: Double,
    currency: String,
    onComplete: (String) -> Unit,
    onCancel: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var refNo by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(0) } // 0: Login, 1: Review, 2: Processing
    
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // PayPal Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("PayPal", fontWeight = FontWeight.Black, fontSize = 24.sp, color = Color(0xFF003087), style = MaterialTheme.typography.headlineSmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onCancel) { Icon(Icons.Default.Close, contentDescription = null) }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                
                when (step) {
                    0 -> {
                        Text("Log in with PayPal", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Securely access your account to pay", fontSize = 13.sp, color = TextSecondary)
                        Spacer(Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Email or mobile number") },
                            placeholder = { Text("user@example.com") },
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Password") },
                            shape = RoundedCornerShape(8.dp),
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { 
                                if (email.isNotEmpty() && password.isNotEmpty()) {
                                    step = 1 
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070BA)),
                            enabled = email.isNotEmpty() && password.isNotEmpty()
                        ) {
                            Text("Log In", fontWeight = FontWeight.Bold)
                        }
                    }
                    1 -> {
                        Text("Review your payment", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(20.dp))
                        
                        Surface(
                            color = Color(0xFFF7F9FA),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Pay with", fontSize = 14.sp)
                                    Text("Visa •••• 1234", fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Amount", fontSize = 14.sp)
                                    Text("$currency ${String.format("%,.2f", amount)}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        OutlinedTextField(
                            value = refNo,
                            onValueChange = { refNo = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Transaction Reference") },
                            placeholder = { Text("Enter Reference from Provider") },
                            shape = RoundedCornerShape(8.dp),
                            isError = refNo.isBlank()
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { 
                                step = 2 
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC439)),
                            enabled = refNo.isNotBlank()
                        ) {
                            Text("Complete Purchase", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        
                        LaunchedEffect(step) {
                            if (step == 2) {
                                delay(2000)
                                onComplete(refNo)
                            }
                        }
                    }
                    2 -> {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF0070BA))
                            Spacer(Modifier.height(16.dp))
                            Text("Authorizing Payment...", fontWeight = FontWeight.Bold)
                            Text("Please do not close this window", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
                
                if (step < 2) {
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Cancel and return to Orthodox App", color = Color(0xFF0070BA), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CardCheckoutSimulatedFlow(
    amount: Double,
    currency: String,
    onComplete: (String) -> Unit,
    onCancel: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var cardRefNo by remember { mutableStateOf("") }
    var flowStep by remember { mutableIntStateOf(0) } // 0: Input, 1: Authorizing, 2: 3D Secure, 3: Finalizing
    
    val cardType = when {
        cardNumber.startsWith("4") -> "Visa"
        cardNumber.startsWith("5") -> "Mastercard"
        cardNumber.startsWith("3") -> "Amex"
        else -> "Card"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)), // Light gray/blue "Gateway" background
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Gateway Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).statusBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Gray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Secure Checkout", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1E293B))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("HTTPS / SSL Encrypted", fontSize = 10.sp, color = SuccessGreen)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    // Simulated Gateway Logo
                    Text(
                        text = "OrthodoxPay",
                        fontWeight = FontWeight.ExtraBold,
                        color = OrthodoxBlue,
                        fontSize = 16.sp,
                        style = MaterialTheme.typography.titleMedium.copy(letterSpacing = (-1).sp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (flowStep == 0) {
                    // Order Summary Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Amount to Pay", fontSize = 12.sp, color = Color.Gray)
                                Text("$currency ${String.format("%,.2f", amount)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF0F172A))
                            }
                            Surface(color = Color(0xFFF1F5F9), shape = CircleShape) {
                                Text(currency, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Card Input Fields
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Card Details", fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                        
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = { input: String -> if (input.all { c -> c.isDigit() } && input.length <= 16) cardNumber = input },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Card Number") },
                            placeholder = { Text("0000 0000 0000 0000") },
                            trailingIcon = {
                                Text(cardType, fontWeight = FontWeight.Black, color = OrthodoxBlue, modifier = Modifier.padding(end = 12.dp))
                            },
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = expiry,
                                onValueChange = { input: String -> 
                                    if (input.length <= 5) {
                                        expiry = if (input.length == 2 && !input.contains("/")) "$input/" else input
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Expiry (MM/YY)") },
                                placeholder = { Text("12/28") },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                            )
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = { input: String -> if (input.all { c -> c.isDigit() } && input.length <= 4) cvv = input },
                                modifier = Modifier.weight(1f),
                                label = { Text("CVC/CVV") },
                                placeholder = { Text("123") },
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                            )
                        }

                        OutlinedTextField(
                            value = cardHolder,
                            onValueChange = { input: String -> cardHolder = input },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Cardholder Name") },
                            placeholder = { Text("J. DOE") },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                        )

                        OutlinedTextField(
                            value = cardRefNo,
                            onValueChange = { cardRefNo = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Transaction Reference") },
                            placeholder = { Text("Enter Reference from Provider") },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                        )

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { flowStep = 1 },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(8.dp),
                            enabled = cardNumber.length >= 4 && expiry.length == 5 && cvv.length >= 2 && cardHolder.isNotBlank() && cardRefNo.isNotBlank()
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Pay $currency ${String.format("%,.2f", amount)}", fontWeight = FontWeight.Bold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Your data is handled by our PCI-DSS server. We never store your full card details.", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                } else if (flowStep == 1) {
                    // Authorizing State
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 100.dp)) {
                        CircularProgressIndicator(color = OrthodoxBlue, modifier = Modifier.size(64.dp), strokeWidth = 6.dp)
                        Spacer(Modifier.height(24.dp))
                        Text("Contacting Bank...", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Secure Handshake in Progress", color = Color.Gray)
                        
                        LaunchedEffect(Unit) {
                            delay(2000)
                            flowStep = 2
                        }
                    }
                } else if (flowStep == 2) {
                    // 3D Secure / Verified by Visa Simulation
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (cardType == "Visa") "Verified by VISA" else "Mastercard ID Check",
                                fontWeight = FontWeight.Black,
                                color = if (cardType == "Visa") Color(0xFF1A1F71) else Color(0xFFEB001B),
                                fontSize = 18.sp
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("Authentication Required", fontWeight = FontWeight.Bold)
                            Text("A verification code was sent to your phone ending in ••89", fontSize = 12.sp, textAlign = TextAlign.Center, color = Color.Gray)
                            
                            Spacer(Modifier.height(24.dp))
                            var otp by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = otp,
                                onValueChange = { input: String -> otp = input },
                                label = { Text("Enter OTP") },
                                modifier = Modifier.width(150.dp),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                            
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { flowStep = 3 },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = OrthodoxBlue),
                                enabled = otp.length >= 4
                            ) {
                                Text("Submit Verification")
                            }
                        }
                    }
                } else if (flowStep == 3) {
                    // Finalizing
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 100.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(24.dp))
                        Text("Payment Approved!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Redirecting to Orthodox App...", color = Color.Gray)
                        
                        LaunchedEffect(Unit) {
                            delay(2000)
                            onComplete(cardRefNo)
                        }
                    }
                }
            }

            // Footer Logos
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Powered by ", fontSize = 10.sp, color = Color.LightGray)
                Text("STRIPE / ADYEN SECURE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun PaymentSuccessScreen(reference: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(shape = CircleShape, color = SuccessGreen.copy(alpha = 0.1f), modifier = Modifier.size(100.dp)) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.padding(24.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Contribution Received!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text("System Reference: $reference", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = OrthodoxBlue)
        Spacer(Modifier.height(24.dp))
        Text("Your contribution has been submitted successfully. An administrator will verify the reference number and approve the record shortly.", textAlign = TextAlign.Center, color = TextSecondary, lineHeight = 20.sp)
        
        Spacer(Modifier.height(48.dp))
        
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = OrthodoxBlue)) {
            Text("Back to Dashboard", fontWeight = FontWeight.Bold, color = PureLinen)
        }
    }
}


data class PaymentMethod(val name: String, val region: String, val icon: ImageVector, val brandColor: Color)
@Composable
fun StripeCheckoutSimulatedFlow(
    amount: Double,
    currency: String,
    donationType: String,
    onComplete: (String) -> Unit,
    onCancel: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) } // 0: Redirecting, 1: Checkout Page, 2: Processing, 3: Success
    var email by remember { mutableStateOf("user@example.com") }
    var stripeCardNumber by remember { mutableStateOf("") }
    var stripeExpiry by remember { mutableStateOf("") }
    var stripeCvc by remember { mutableStateOf("") }
    var stripeRefNo by remember { mutableStateOf("") }
    
    Box(modifier = Modifier.fillMaxSize().background(if (step == 1) Color.White else Color(0xFFF6F9FC))) {
        when (step) {
            0 -> {
                // Redirecting simulation
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFF635BFF), strokeWidth = 3.dp)
                    Spacer(Modifier.height(24.dp))
                    Text("Redirecting to Stripe...", color = Color(0xFF424770), fontWeight = FontWeight.SemiBold)
                    
                    LaunchedEffect(Unit) {
                        delay(1500)
                        step = 1
                    }
                }
            }
            1 -> {
                // Stripe Hosted Page Simulation
                Column(modifier = Modifier.fillMaxSize()) {
                    // Stripe Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp).statusBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color(0xFF424770))
                        }
                        Text("Orthodox App", fontWeight = FontWeight.Bold, color = Color(0xFF424770))
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF424770), modifier = Modifier.size(14.dp))
                    }
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        // Product Summary
                        Text(donationType, fontSize = 14.sp, color = Color(0xFF424770))
                        Text("$currency ${String.format("%,.2f", amount)}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A2540))
                        
                        Spacer(Modifier.height(32.dp))
                        
                        // Payment Options
                        Text("Pay with", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0A2540))
                        Spacer(Modifier.height(16.dp))
                        
                        // Simulated Google Pay Button
                        Surface(
                            modifier = Modifier.fillMaxWidth().height(48.dp).clickable { step = 2 },
                            color = Color.Black,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Text("G Pay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE6EBF1))
                            Text(" or pay with card ", fontSize = 12.sp, color = Color(0xFF424770), modifier = Modifier.padding(horizontal = 8.dp))
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE6EBF1))
                        }
                        
                        // Card Fields
                        Text("Email", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF635BFF))
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text("Card information", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = stripeCardNumber,
                            onValueChange = { stripeCardNumber = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("1234 5678 1234 5678") },
                            trailingIcon = { Row(modifier = Modifier.padding(end = 8.dp)) { Icon(Icons.Default.CreditCard, null, tint = Color.LightGray) } },
                            shape = RoundedCornerShape(4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF635BFF))
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = stripeExpiry,
                                onValueChange = { stripeExpiry = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("MM / YY") },
                                shape = RoundedCornerShape(bottomStart = 4.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF635BFF))
                            )
                            OutlinedTextField(
                                value = stripeCvc,
                                onValueChange = { stripeCvc = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("CVC") },
                                shape = RoundedCornerShape(bottomEnd = 4.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF635BFF))
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("Transaction Reference", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                        OutlinedTextField(
                            value = stripeRefNo,
                            onValueChange = { stripeRefNo = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter Reference from Provider") },
                            shape = RoundedCornerShape(4.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF635BFF))
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        Button(
                            onClick = { 
                                if (email.isNotEmpty() && stripeCardNumber.isNotEmpty() && stripeRefNo.isNotEmpty()) {
                                    step = 2 
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF635BFF)),
                            shape = RoundedCornerShape(4.dp),
                            enabled = email.isNotEmpty() && stripeCardNumber.isNotEmpty() && stripeRefNo.isNotEmpty()
                        ) {
                            Text("Pay $currency ${String.format("%,.2f", amount)}", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            Text("Powered by ", fontSize = 12.sp, color = Color(0xFF424770))
                            Text("stripe", fontWeight = FontWeight.ExtraBold, color = Color(0xFF424770), fontSize = 18.sp)
                        }
                    }
                }
            }
            2 -> {
                // Processing
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFF635BFF))
                    Spacer(Modifier.height(24.dp))
                    Text("Processing payment...", color = Color(0xFF424770))
                    
                    LaunchedEffect(Unit) {
                        delay(2500)
                        step = 3
                    }
                }
            }
            3 -> {
                // Success redirect
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF24B47E), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("Payment Successful", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Returning to Orthodox App", color = Color.Gray)
                    
                    LaunchedEffect(Unit) {
                        delay(2000)
                        onComplete(stripeRefNo)
                    }
                }
            }
        }
    }
}
