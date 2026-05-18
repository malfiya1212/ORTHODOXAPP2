package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.orthodoxapp.ui.theme.*
import com.example.orthodoxapp.data.model.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: FinancialViewModel, onNavigate: (String) -> Unit = {}) {
    val currentUser by viewModel.currentUser.collectAsState()
    val incomeRecords by viewModel.income.collectAsState()
    val churches by viewModel.churches.collectAsState()
    
    // Find User's Church Name
    val userChurch = churches.find { it.id == currentUser?.churchId }
    val churchName = userChurch?.name ?: currentUser?.customChurchName ?: "General Parish"

    // --- DETAILED CALCULATIONS FOR MEMBER ---
    val totalApproved = incomeRecords.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val pendingContributions = incomeRecords.filter { it.status == "PENDING" }.size
    
    val totalTithe = incomeRecords.filter { it.status == "APPROVED" && it.category?.contains("Tithe", ignoreCase = true) == true }.sumOf { it.amount }
    val totalOffering = incomeRecords.filter { it.status == "APPROVED" && (it.category?.contains("Donation", ignoreCase = true) == true || it.category?.contains("Offering", ignoreCase = true) == true) }.sumOf { it.amount }
    
    // Calculate This Month's Giving
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    val startOfMonth = calendar.timeInMillis
    val thisMonthGiving = incomeRecords.filter { it.status == "APPROVED" && it.date >= startOfMonth }.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(churchName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PureLinen)
                        Text("Member Portal", fontSize = 11.sp, color = OrthodoxGold)
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate("profile") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = PureLinen)
                    }
                    IconButton(onClick = { 
                        viewModel.logout()
                        onNavigate("login") 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = PureLinen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrthodoxBlue)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- 1. PERSONAL SUMMARY (TOP CARD) ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = OrthodoxBlueDark),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.name ?: "Beloved Member",
                                    color = PureLinen,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Church, contentDescription = null, tint = OrthodoxGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = churchName, color = PureLinen.copy(alpha = 0.8f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            
                            Surface(
                                color = if (currentUser?.status == "ACTIVE") SuccessGreen.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (currentUser?.status == "ACTIVE") SuccessGreen else Color.Gray)
                            ) {
                                Text(
                                    text = currentUser?.status ?: "ACTIVE",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    color = if (currentUser?.status == "ACTIVE") SuccessGreen else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = PureLinen.copy(alpha = 0.1f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text(text = "TOTAL CONTRIBUTIONS", color = OrthodoxGold, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                        Text(text = "ETB ${String.format("%,.2f", totalApproved)}", color = PureLinen, fontSize = 36.sp, fontWeight = FontWeight.Black)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Monthly Progress Bar
                        val monthlyGoal = 5000.0
                        val progress = (thisMonthGiving / monthlyGoal).coerceIn(0.0, 1.0).toFloat()
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Monthly Goal Progress", color = PureLinen.copy(alpha = 0.6f), fontSize = 11.sp)
                                Text("${(progress * 100).toInt()}%", color = OrthodoxGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                color = OrthodoxGold,
                                trackColor = PureLinen.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }

            // --- 2. MY CONTRIBUTIONS (MAIN FEATURE) ---
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text("My Giving Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ContributionMetricCard("Total Tithe", totalTithe, OrthodoxBlue, Modifier.weight(1f))
                        ContributionMetricCard("Total Offering", totalOffering, InfoBlue, Modifier.weight(1f))
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    ContributionMetricCard(
                        "This Month Giving", 
                        thisMonthGiving, 
                        SuccessGreen, 
                        Modifier.fillMaxWidth(),
                        isFullWidth = true
                    )
                }
            }

            // --- 3. GIVE DONATION BUTTON ---
            item {
                Button(
                    onClick = { onNavigate("global_payment") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrthodoxGold, contentColor = Color.Black),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Give Tithe or Donation", fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 0.5.sp)
                }
            }

            // --- 4. SPIRITUAL PROGRESS & DAILY VERSE ---
            item {
                Row(modifier = Modifier.padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = OrthodoxBlue.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OrthodoxBlue, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Spiritual Path", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Level: Faithful", fontSize = 12.sp, color = OrthodoxBlue, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("3 Services Attended", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = OrthodoxGold.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = OrthodoxGold, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Morning", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Prayer", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // --- 5. PARISH ANNOUNCEMENTS ---
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Parish Announcements", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = OrthodoxBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(12.dp), color = OrthodoxBlue.copy(alpha = 0.1f)) {
                                Icon(Icons.Default.Event, contentDescription = null, tint = OrthodoxBlue, modifier = Modifier.padding(12.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("Annual Parish Meeting", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Next Sunday after Liturgy", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // --- 5. HISTORY LIST ---
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Recent History", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        TextButton(onClick = { onNavigate("income") }) { Text("View All", color = InfoBlue) }
                    }
                    
                    if (incomeRecords.isEmpty()) {
                        EmptyStateCard("No contribution records yet.")
                    } else {
                        incomeRecords.take(3).forEach { record ->
                            MemberTransactionRow(
                                title = record.source,
                                type = record.category ?: "Offering",
                                status = if (record.status == "APPROVED") "Approved" else "Pending",
                                amount = "ETB ${String.format("%,.0f", record.amount)}",
                                date = java.text.DateFormat.getDateInstance().format(record.date),
                                icon = if (record.category?.contains("Tithe") == true) Icons.Default.AutoAwesome else Icons.Default.Payments,
                                color = if (record.status == "APPROVED") SuccessGreen else WarningOrange
                            )
                        }
                    }
                }
            }

            // --- 6. CHURCH SERVICES ---
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text("Parish Services", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(12.dp))
                    
                    // Grid Layout for Services
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ServiceButton("My Tithe", Icons.Default.Payments, Modifier.weight(1f)) { onNavigate("income") }
                            ServiceButton("Certificates", Icons.Default.Badge, Modifier.weight(1f)) { onNavigate(Screen.Certificates.route) }
                            ServiceButton("Groups", Icons.Default.Groups, Modifier.weight(1f)) { onNavigate("chure") }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ServiceButton("Events", Icons.Default.Event, Modifier.weight(1f)) { onNavigate("notifications") }
                            ServiceButton("Programs", Icons.Default.CalendarMonth, Modifier.weight(1f)) { onNavigate("notifications") }
                            ServiceButton("Announce", Icons.Default.Campaign, Modifier.weight(1f)) { onNavigate("notifications") }
                        }
                    }
                }
            }

            // --- 7. SPIRITUAL MESSAGE ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = OrthodoxBlue.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = OrthodoxBlue, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "\"Each of you should give what you have decided in your heart to give, not reluctantly or under compulsion, for God loves a cheerful giver.\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("- 2 Corinthians 9:7", fontSize = 12.sp, color = OrthodoxBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- 8. HELP & SUPPORT ---
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text("Help & Support", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp).clickable { /* Open Support */ }, verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Help, contentDescription = null, tint = OrthodoxBlue)
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("Contact Parish Treasury", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("support@parish.com", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
private fun ContributionMetricCard(
    label: String, 
    amount: Double, 
    color: Color, 
    modifier: Modifier,
    isFullWidth: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = if (isFullWidth) Alignment.Start else Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ETB ${String.format("%,.0f", amount)}",
                color = color,
                fontSize = if (isFullWidth) 24.sp else 20.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun SummaryMiniCard(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ServiceButton(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrthodoxBlue)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(message, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun MemberTransactionRow(
    title: String, 
    type: String,
    status: String, 
    amount: String, 
    date: String, 
    icon: ImageVector, 
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(12.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Spacer(Modifier.width(8.dp))
                Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                    Text(type, color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }
            Text("$status • $date", fontSize = 12.sp, color = TextSecondary)
        }
        Text(amount, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
    }
}
