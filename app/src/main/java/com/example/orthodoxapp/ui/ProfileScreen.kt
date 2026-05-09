package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Church
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.ui.theme.*
import com.example.orthodoxapp.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    val currentUser by viewModel.currentUser.collectAsState()
    val role by viewModel.currentRole.collectAsState()
    
    val roleName = when(role) {
        UserRole.SYNOD_ADMIN -> "Holy Synod Admin"
        UserRole.DIOCESE_ADMIN -> "Diocese Admin"
        UserRole.CHURCH_ADMIN -> "Parish Admin"
        UserRole.ACCOUNTANT -> "Church Accountant"
        UserRole.AUDITOR -> "Church Auditor"
        UserRole.MEMBER -> "Church Member"
        else -> "Guest"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Personal Identity", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
                        Text("Profile Settings", fontWeight = FontWeight.Bold, color = PureLinen)
                    }
                },
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
                .background(BackgroundLight),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Profile Picture Placeholder
            Surface(
                modifier = Modifier.size(110.dp),
                shape = CircleShape,
                color = OrthodoxBlue.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(2.dp, OrthodoxGold)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = OrthodoxBlue, modifier = Modifier.size(56.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(currentUser?.name ?: "Unknown User", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(roleName, fontSize = 14.sp, color = OrthodoxGold, fontWeight = FontWeight.Bold)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    ProfileInfoRow(Icons.Default.Email, "Email Address", currentUser?.email ?: "Not set")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                    ProfileInfoRow(Icons.Default.Badge, "Ecclesiastical Role", roleName)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.3f))
                    
                    val churchName = if (role == UserRole.SYNOD_ADMIN) "Holy Synod National Council" else "St. Mary Parish"
                    ProfileInfoRow(Icons.Default.Church, "Assigned Parish", churchName)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrthodoxBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Edit Profile Information", fontWeight = FontWeight.Bold, color = PureLinen)
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = { viewModel.logout(); onBack() }
            ) {
                Text("Sign Out of Account", color = ErrorRed, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(10.dp), color = OrthodoxBlue.copy(alpha = 0.05f)) {
            Icon(icon, contentDescription = null, tint = OrthodoxBlue, modifier = Modifier.padding(8.dp).size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}
