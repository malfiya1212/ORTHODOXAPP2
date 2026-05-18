package com.example.orthodoxapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.data.model.*
import com.example.orthodoxapp.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrganizationScreen(
    viewModel: FinancialViewModel, 
    onBack: () -> Unit,
    orgId: Long? = null,
    initialType: String? = null
) {
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedDioceseId by remember { mutableStateOf<Long?>(null) }
    var orgType by remember { mutableStateOf(initialType ?: "Church") }
    var adminName by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var confirmAdminPassword by remember { mutableStateOf("") }
    
    val dioceses by viewModel.dioceses.collectAsState()
    val churches by viewModel.churches.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var typeExpanded by remember { mutableStateOf(false) }
    var dioceseExpanded by remember { mutableStateOf(false) }
    val actionState by viewModel.actionState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionState) {
        if (actionState is ActionState.Success) {
            onBack()
            viewModel.resetActionState()
        }
    }

    // Load existing data if updating or pre-fill from user role
    LaunchedEffect(orgId, orgType, dioceses, churches, currentUser, currentRole) {
        if (orgId != null) {
            when (orgType) {
                "Diocese" -> dioceses.find { it.id == orgId }?.let { name = it.name }
                "Church" -> churches.find { it.id == orgId }?.let { 
                    name = it.name
                    location = it.location ?: ""
                    selectedDioceseId = it.dioceseId
                }
            }
        } else {
            // Pre-fill for Diocese Admin
            if (currentRole == UserRole.DIOCESE_ADMIN && selectedDioceseId == null) {
                selectedDioceseId = currentUser?.dioceseId
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (orgId == null) "Add New $orgType" else "Update $orgType", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Type Selector (Only if new)
            if (orgId == null) {
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = orgType,
                        onValueChange = { _ -> },
                        readOnly = true,
                        label = { Text("Organization Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Church / Parish") },
                            onClick = {
                                orgType = "Church"
                                typeExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Diocese / Eparchy") },
                            onClick = {
                                orgType = "Diocese"
                                typeExpanded = false
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Diocese Selector (Only for Church)
            if (orgType == "Church") {
                ExposedDropdownMenuBox(
                    expanded = dioceseExpanded,
                    onExpandedChange = { dioceseExpanded = !dioceseExpanded }
                ) {
                    OutlinedTextField(
                        value = dioceses.find { it.id == selectedDioceseId }?.name ?: "Select Diocese",
                        onValueChange = { _ -> },
                        readOnly = true,
                        label = { Text("Parent Diocese") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dioceseExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = dioceseExpanded,
                        onDismissRequest = { dioceseExpanded = false }
                    ) {
                        dioceses.forEach { diocese ->
                            DropdownMenuItem(
                                text = { Text(diocese.name) },
                                onClick = {
                                    selectedDioceseId = diocese.id
                                    dioceseExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("$orgType Name") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryBlue) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text(if (orgType == "Church") "Physical Location" else "Headquarters Location") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryBlue) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Administrative Account", fontWeight = FontWeight.Bold, color = PrimaryBlue)
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = adminName,
                onValueChange = { adminName = it },
                label = { Text("Admin Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = adminEmail,
                onValueChange = { adminEmail = it },
                label = { Text("Admin Email / Login ID") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryBlue) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = adminPassword,
                onValueChange = { adminPassword = it },
                label = { Text("Initial Admin Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmAdminPassword,
                onValueChange = { confirmAdminPassword = it },
                label = { Text("Confirm Admin Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                isError = confirmAdminPassword.isNotEmpty() && adminPassword != confirmAdminPassword
            )

            if (confirmAdminPassword.isNotEmpty() && adminPassword != confirmAdminPassword) {
                Text(
                    text = "Passwords do not match",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (adminPassword != confirmAdminPassword) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Passwords do not match")
                        }
                        return@Button
                    }
                    if (orgId == null) {
                        when (orgType) {
                            "Diocese" -> viewModel.addDiocese(name, location, null, adminName, adminEmail, adminPassword)
                            "Church" -> viewModel.addChurch(name, location, selectedDioceseId ?: currentUser?.dioceseId ?: 1L, adminName, adminEmail, adminPassword)
                        }
                    } else {
                        // Update logic...
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotEmpty() && (orgType == "Diocese" || selectedDioceseId != null) && 
                          adminPassword.isNotEmpty() && adminPassword == confirmAdminPassword &&
                          actionState !is ActionState.Loading
            ) {
                if (actionState is ActionState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (orgId == null) "Register $orgType" else "Update $orgType", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            if (actionState is ActionState.Error) {
                Text(
                    text = (actionState as ActionState.Error).message,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
