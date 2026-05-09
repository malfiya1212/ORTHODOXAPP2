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
import com.example.orthodoxapp.data.model.UserRole
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordIncomeScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    val currentRole by viewModel.currentRole.collectAsState()
    val isMember = currentRole == UserRole.MEMBER

    var amount by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tithe") }
    var paymentMethod by remember { mutableStateOf("Cash") }
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
            Column {
                Text("Amount in ETB", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("0.00") },
                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, tint = OrthodoxBlue) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                )
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
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
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
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
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
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Choose Ethiopian Bank") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bankExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
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
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Choose Mobile Service") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mobileExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
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
            Column {
                Text("Date", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = "Today's Date", // Placeholder for actual picker
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("Note about this income...") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = OrthodoxBlue) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = OrthodoxBlue)
                )
            }

            // Receipt Upload Section
            Text("Proof of Payment (Optional)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextSecondary)
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { imagePickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(20.dp),
                color = SurfaceWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Receipt",
                            modifier = Modifier.height(150.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(48.dp), tint = OrthodoxBlue.copy(alpha = 0.4f))
                        Spacer(Modifier.height(12.dp))
                        Text("Tap to upload receipt", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OrthodoxBlue)
                        Text("JPG, PNG or PDF supported", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (amount.isEmpty() || source.isEmpty()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    } else {
                        val amt = amount.toDoubleOrNull() ?: 0.0
                        if (amt <= 0) {
                            Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
                        } else {
                            val finalMethod = when (paymentMethod) {
                                "Bank Transfer" -> if (selectedBank.isNotEmpty()) "Bank ($selectedBank)" else "Bank Transfer"
                                "Mobile Banking" -> if (selectedMobilePlatform.isNotEmpty()) "Mobile ($selectedMobilePlatform)" else "Mobile Banking"
                                else -> paymentMethod
                            }
                            viewModel.addIncome(amt, source, 1L, 1L, selectedCategory, description, finalMethod)
                            Toast.makeText(context, "Successfully Recorded", Toast.LENGTH_LONG).show()
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
