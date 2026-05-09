package com.example.orthodoxapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    
    val dioceses by viewModel.dioceses.collectAsState()
    val churches by viewModel.churches.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()

    var typeExpanded by remember { mutableStateOf(false) }
    var dioceseExpanded by remember { mutableStateOf(false) }

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
            // Pre-fill for new records
            if (selectedDioceseId == null) selectedDioceseId = currentUser?.dioceseId
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (orgId == null) "Register Organization" else "Update Organization", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                .padding(24.dp)
        ) {
            Text("Organization Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Organization Type
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { if (orgId == null) typeExpanded = !typeExpanded }
            ) {
                OutlinedTextField(
                    value = orgType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Organization Type") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = orgId == null),
                    trailingIcon = { if (orgId == null) ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    shape = RoundedCornerShape(12.dp),
                    enabled = orgId == null
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    val availableTypes = if (currentRole == UserRole.SYNOD_ADMIN) listOf("Diocese") else listOf("Diocese", "Church")
                    availableTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                orgType = type
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Parent Selection based on type
            if (orgType == "Church") {
                ExposedDropdownMenuBox(
                    expanded = dioceseExpanded,
                    onExpandedChange = { dioceseExpanded = !dioceseExpanded }
                ) {
                    OutlinedTextField(
                        value = dioceses.find { it.id == selectedDioceseId }?.name ?: "Select Diocese",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Diocese") },
                        leadingIcon = { Icon(Icons.Default.Map, contentDescription = null, tint = PrimaryBlue) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dioceseExpanded) },
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

            if (orgType == "Church") {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Physical Location") },
                    leadingIcon = { Icon(Icons.Default.LocationCity, contentDescription = null, tint = PrimaryBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text("Primary Administrator", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                Spacer(modifier = Modifier.height(8.dp))
                
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
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (orgId == null) {
                        when (orgType) {
                            "Diocese" -> viewModel.addDiocese(name, location)
                            "Church" -> viewModel.addChurch(name, location, selectedDioceseId ?: currentUser?.dioceseId ?: 1L, adminName, adminEmail)
                        }
                    } else {
                        // Update logic...
                    }
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotEmpty() && (orgType == "Diocese" || selectedDioceseId != null)
            ) {
                Text(if (orgId == null) "Register $orgType" else "Update $orgType", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
