package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhonelinkLock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(viewModel: FinancialViewModel, onBack: () -> Unit, onResetSuccess: () -> Unit) {
    var step by remember { mutableIntStateOf(1) } // 1: Email/Phone, 2: OTP, 3: New Password
    var identifier by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val actionState by viewModel.actionState.collectAsState()

    LaunchedEffect(actionState) {
        when (actionState) {
            is ActionState.Success -> {
                if (step == 3) {
                    onResetSuccess()
                    viewModel.resetActionState()
                }
                isLoading = false
            }
            is ActionState.Error -> {
                errorMessage = (actionState as ActionState.Error).message
                isLoading = false
            }
            is ActionState.Loading -> {
                isLoading = true
            }
            else -> {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Surface(
                color = PrimaryBlue.copy(alpha = 0.1f),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (step == 2) Icons.Default.VpnKey else Icons.Default.PhonelinkLock,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            when (step) {
                1 -> {
                    Text("Forgot Password?", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text(
                        "Enter your email address to receive a 6-digit OTP code.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    OutlinedTextField(
                        value = identifier,
                        onValueChange = { 
                            identifier = it
                            errorMessage = null
                        },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = {
                            errorMessage = null
                            viewModel.sendOtpToEmail(identifier) { success ->
                                if (success) step = 2
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = identifier.isNotEmpty() && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Send OTP Code", fontWeight = FontWeight.Bold)
                    }
                }
                2 -> {
                    Text("Verify OTP", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text(
                        "We've sent a code to $identifier",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { 
                            if (it.length <= 6) otp = it 
                            errorMessage = null
                        },
                        label = { Text("Enter 6-digit Code") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center)
                    )
                    Text(
                        "Didn't receive the code? Please check your spam folder.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { 
                            if (viewModel.verifyOtp(otp)) {
                                step = 3
                            } else {
                                errorMessage = "Invalid OTP code. Try again."
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = otp.length == 6,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify & Continue", fontWeight = FontWeight.Bold)
                    }
                }
                3 -> {
                    Text("New Password", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text(
                        "Set a strong password for your account.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { 
                            newPassword = it
                            errorMessage = null
                        },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { 
                            errorMessage = null
                            viewModel.resetPassword(identifier, newPassword) 
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = newPassword.isNotEmpty() && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Update Password", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
