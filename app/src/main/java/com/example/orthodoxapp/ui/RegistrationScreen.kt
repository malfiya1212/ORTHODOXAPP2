package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.ui.theme.PrimaryBlue
import com.example.orthodoxapp.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: FinancialViewModel,
    onRegisterSuccess: () -> Unit, 
    onBackToLogin: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    
    var isOtpSent by remember { mutableStateOf(false) }
    var isOtpVerified by remember { mutableStateOf(false) }
    
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    var passwordVisible by remember { mutableStateOf(false) }
    
    val loginState by viewModel.loginState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onRegisterSuccess()
            viewModel.resetLoginState()
        } else if (loginState is LoginState.Error) {
            hostState.showSnackbar((loginState as LoginState.Error).message)
        }
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isStrongPassword(password: String): Boolean {
        return password.length >= 6 // Simplified for professional yet usable feel
    }

    val strings = com.example.orthodoxapp.util.LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SnackbarHost(hostState)
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Join the Tewahedo Community",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )
        
        Text(
            text = "Enter your details to create a member account",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- 1. FULL NAME ---
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. EMAIL ADDRESS ---
        OutlinedTextField(
            value = emailOrPhone,
            onValueChange = { 
                emailOrPhone = it 
                emailError = if (it.isNotEmpty() && !isValidEmail(it)) "Invalid email address" else null
            },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = emailError != null,
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryBlue) },
            supportingText = { emailError?.let { Text(it, color = Color.Red) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. CHURCH SELECTION ---
        val churches by viewModel.churches.collectAsState()
        var selectedChurchId by remember { mutableStateOf<Long?>(null) }
        var churchExpanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = churches.find { it.id == selectedChurchId }?.name ?: "Select Your Church",
                onValueChange = {},
                readOnly = true,
                label = { Text("Parish / Church") },
                trailingIcon = { 
                    IconButton(onClick = { churchExpanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Church, contentDescription = null, tint = PrimaryBlue) }
            )
            DropdownMenu(
                expanded = churchExpanded,
                onDismissRequest = { churchExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                churches.forEach { church ->
                    DropdownMenuItem(
                        text = { Text(church.name) },
                        onClick = {
                            selectedChurchId = church.id
                            churchExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. PASSWORD ---
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it 
                passwordError = if (it.isNotEmpty() && !isStrongPassword(it)) "Password must be at least 6 characters" else null
            },
            label = { Text("Create Password") },
            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = passwordError != null,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue) },
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(icon, contentDescription = null)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 5. CONFIRM PASSWORD ---
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it 
                confirmPasswordError = if (it != password) "Passwords do not match" else null
            },
            label = { Text("Confirm Password") },
            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            isError = confirmPasswordError != null,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryBlue) },
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(icon, contentDescription = null)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- OTP FLOW ---
        if (!isOtpSent) {
            Button(
                onClick = { 
                    if (isValidEmail(emailOrPhone) && name.isNotEmpty() && selectedChurchId != null && password.isNotEmpty() && confirmPassword == password) {
                        viewModel.sendOtpToEmail(emailOrPhone) { success ->
                            if (success) isOtpSent = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isValidEmail(emailOrPhone) && name.isNotEmpty() && selectedChurchId != null && 
                          password.isNotEmpty() && confirmPassword == password && actionState !is ActionState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (actionState is ActionState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Send Verification OTP", fontWeight = FontWeight.Bold)
                }
            }
        } else if (!isOtpVerified) {
            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                label = { Text("Enter 6-Digit OTP") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = PrimaryBlue) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    if (viewModel.verifyOtp(otpCode)) {
                        isOtpVerified = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Verify & Complete Registration", fontWeight = FontWeight.Bold)
            }
            
            TextButton(onClick = { isOtpSent = false }) {
                Text("Back to Edit Details", color = Color.Gray)
            }
        }

        // Final registration trigger once verified
        LaunchedEffect(isOtpVerified) {
            if (isOtpVerified) {
                viewModel.register(
                    name = name, 
                    email = emailOrPhone, 
                    passwordRaw = password, 
                    roleName = "Member",
                    churchId = selectedChurchId
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account? ", color = Color.Gray)
            TextButton(onClick = onBackToLogin) {
                Text("Login", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}
