package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.foundation.clickable
import com.example.orthodoxapp.data.model.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: FinancialViewModel, filter: String = "", onBack: () -> Unit) {
    val allNotifications by viewModel.notifications.collectAsState()
    val notifications = remember(allNotifications, filter) {
        if (filter.isEmpty()) allNotifications
        else allNotifications.filter { it.title.startsWith("[$filter]") }
    }
    val incomeList by viewModel.income.collectAsState()
    val churches by viewModel.churches.collectAsState()
    val users by viewModel.users.collectAsState()
    var selectedNotification by remember { mutableStateOf<Notification?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Updates", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
                        Text("Notifications", fontWeight = FontWeight.Bold, color = PureLinen)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureLinen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrthodoxBlue)
            )
        }
    ) { padding ->
        if (selectedNotification != null) {
            val relatedIncome = incomeList.find { income -> income.id == selectedNotification?.entityId }
            val relatedChurch = churches.find { church -> church.id == relatedIncome?.churchId }
            val relatedUser = users.find { user -> user.id == relatedIncome?.createdBy }

            AlertDialog(
                onDismissRequest = { selectedNotification = null },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = when(selectedNotification?.type) {
                            "APPROVAL" -> Icons.Default.CheckCircle
                            "PAYMENT" -> Icons.Default.Receipt
                            "DONATION" -> Icons.Default.Favorite
                            "ALERT" -> Icons.Default.Warning
                            else -> Icons.Default.Notifications
                        },
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = when(selectedNotification?.type) {
                            "APPROVAL" -> SuccessGreen
                            "PAYMENT" -> InfoBlue
                            "DONATION" -> OrthodoxGoldDark
                            "ALERT" -> ErrorRed
                            else -> Color.Gray
                        }
                    )
                },
                title = { Text(selectedNotification?.title ?: "Notification Detail", fontWeight = FontWeight.ExtraBold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (selectedNotification?.type == "PAYMENT" || selectedNotification?.type == "APPROVAL") {
                            // Show detailed fields
                            NotificationDetailRow("User Name", relatedUser?.name ?: "Unknown Member")
                            NotificationDetailRow("Ref Number", relatedIncome?.referenceNumber ?: "N/A")
                            NotificationDetailRow("Church", relatedChurch?.name ?: "Local Parish")
                            NotificationDetailRow("Amount", "ETB ${String.format("%,.2f", relatedIncome?.amount ?: 0.0)}")
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.LightGray.copy(alpha = 0.5f))
                        }
                        
                        Text(selectedNotification?.message ?: "", style = MaterialTheme.typography.bodyMedium)
                        
                        Text(
                            text = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(selectedNotification?.date ?: 0L)),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                },
                confirmButton = {
                    if (selectedNotification?.type == "APPROVAL" && selectedNotification?.entityId != null) {
                        Button(
                            onClick = { 
                                viewModel.approveIncome(selectedNotification!!.entityId!!)
                                selectedNotification = null 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Text("Accept")
                        }
                    } else {
                        Button(onClick = { selectedNotification = null }) {
                            Text("Close")
                        }
                    }
                },
                dismissButton = {
                    if (selectedNotification?.type == "APPROVAL" && selectedNotification?.entityId != null) {
                        TextButton(
                            onClick = { 
                                viewModel.rejectIncome(selectedNotification!!.entityId!!)
                                selectedNotification = null 
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = ErrorRed)
                        ) {
                            Text("Reject")
                        }
                    }
                }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (notifications.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            androidx.compose.material3.Icon(Icons.Default.NotificationsNone, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                            Spacer(Modifier.height(8.dp))
                            Text("No new notifications.", color = TextSecondary)
                        }
                    }
                }
            } else {
                items(notifications) { notification: Notification ->
                    AlertRow(
                        title = notification.title,
                        subtitle = notification.message,
                        icon = when(notification.type) {
                            "APPROVAL" -> Icons.Default.CheckCircle
                            "PAYMENT" -> Icons.Default.Receipt
                            "DONATION" -> Icons.Default.Favorite
                            "ALERT" -> Icons.Default.Warning
                            else -> Icons.Default.Notifications
                        },
                        color = when(notification.type) {
                            "APPROVAL" -> SuccessGreen
                            "PAYMENT" -> InfoBlue
                            "DONATION" -> OrthodoxGoldDark
                            "ALERT" -> ErrorRed
                            else -> Color.Gray
                        },
                        onClick = { selectedNotification = notification }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertRow(
    title: String, 
    subtitle: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    color: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = color.copy(alpha = 0.1f)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    modifier = Modifier.padding(8.dp),
                    tint = color
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.LightGray)
        }
    }
}

@Composable
private fun NotificationDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

