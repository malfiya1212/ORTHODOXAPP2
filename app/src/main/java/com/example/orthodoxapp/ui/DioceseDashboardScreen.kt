package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Assessment
import java.util.Calendar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.data.model.*
import com.example.orthodoxapp.ui.components.AdminStatCard
import com.example.orthodoxapp.ui.components.ManagementActionHorizontal
import com.example.orthodoxapp.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DioceseDashboardScreen(
    viewModel: FinancialViewModel,
    modifier: Modifier = Modifier,
    overrideDioceseId: Long? = null,
    onNavigate: (String) -> Unit = {}
) {
    // Data collection
    val churches by viewModel.churches.collectAsState(initial = emptyList())
    val incomes by viewModel.income.collectAsState(initial = emptyList())
    val expenses by viewModel.expenses.collectAsState(initial = emptyList())
    val dioceses by viewModel.dioceses.collectAsState(initial = emptyList())
    val user by viewModel.currentUser.collectAsState(initial = null)
    
    val effectiveDioceseId = overrideDioceseId ?: user?.dioceseId
    val currentDiocese = dioceses.find { it.id == effectiveDioceseId }
    val currentDioceseName = currentDiocese?.name ?: "My Diocese"

    // FILTER DATA BASED ON DIOCESE (Crucial for Synod Admin view)
    val churchesInThisDiocese = if (effectiveDioceseId != null) {
        churches.filter { it.dioceseId == effectiveDioceseId }
    } else {
        emptyList()
    }
    val churchIds = churchesInThisDiocese.map { it.id }.toSet()

    val filteredIncomes = if (overrideDioceseId != null) {
        incomes.filter { it.churchId in churchIds }
    } else {
        incomes
    }

    val filteredExpenses = if (overrideDioceseId != null) {
        expenses.filter { it.churchId in churchIds }
    } else {
        expenses
    }

    val totalIncome = filteredIncomes.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val totalExpense = filteredExpenses.filter { it.status == "APPROVED" }.sumOf { it.amount }
    
    // Monthly Calculations
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    val startOfMonth = calendar.timeInMillis
    
    val monthlyIncome = filteredIncomes.filter { it.status == "APPROVED" && it.date >= startOfMonth }.sumOf { it.amount }
    val monthlyExpense = filteredExpenses.filter { it.status == "APPROVED" && it.date >= startOfMonth }.sumOf { it.amount }
    
    // Pending Metrics
    val pendingRecords = filteredIncomes.filter { it.status == "PENDING" }
    val pendingCount = pendingRecords.size
    val pendingAmount = pendingRecords.sumOf { it.amount }

    val formatCurrency = { amount: Double -> "ETB ${String.format(Locale.getDefault(), "%,.2f", amount)}" }
    val formatCompact = { amount: Double -> "ETB ${String.format(Locale.getDefault(), "%,.0f", amount)}" }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(currentDioceseName, style = MaterialTheme.typography.labelSmall, color = OrthodoxGold, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("Diocese Dashboard", fontWeight = FontWeight.Black, color = PureLinen, fontSize = 22.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate("notifications") }) {
                        BadgedBox(badge = { if (pendingCount > 0) Badge { Text(pendingCount.toString()) } }) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = PureLinen)
                        }
                    }
                    
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = PureLinen)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                onClick = { 
                                    showMenu = false
                                    onNavigate("settings") 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                onClick = { 
                                    showMenu = false
                                    onNavigate("profile") 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                                onClick = { 
                                    showMenu = false
                                    viewModel.logout()
                                    onNavigate("login")
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrthodoxBlue)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(Screen.AddOrganization.createRoute("Church", user?.dioceseId ?: 1L)) },
                containerColor = OrthodoxGold,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Church")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundLight),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // --- 1. EXPANDED FINANCIAL SUMMARY ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = OrthodoxBlueDark),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = OrthodoxGold, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Aggregate Treasury Balance", color = OrthodoxGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(formatCurrency(totalIncome - totalExpense), color = PureLinen, fontSize = 34.sp, fontWeight = FontWeight.Black)
                            }
                            
                            // Church Count Badge
                            Surface(color = PureLinen.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${churches.size}", color = PureLinen, fontWeight = FontWeight.Black, fontSize = 18.sp)
                                    Text("CHURCHES", color = PureLinen.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        HorizontalDivider(color = PureLinen.copy(alpha = 0.1f))
                        Spacer(Modifier.height(20.dp))
                        
                        // Monthly Breakdown
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("This Month Income", color = PureLinen.copy(alpha = 0.5f), fontSize = 11.sp)
                                Text(formatCompact(monthlyIncome), color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("This Month Expense", color = PureLinen.copy(alpha = 0.5f), fontSize = 11.sp)
                                Text(formatCompact(monthlyExpense), color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                        
                        // Pending Approvals
                        if (pendingCount > 0) {
                            Spacer(Modifier.height(20.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { onNavigate("approvals") },
                                color = OrthodoxGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = OrthodoxGold, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text("$pendingCount Awaiting Blessing", color = OrthodoxGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(formatCompact(pendingAmount), color = OrthodoxGold, fontSize = 14.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }

            // --- 2. SUPERVISORY ALERTS & APPROVALS ---
            item {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Awaiting Diocese Blessing", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextPrimary)
                        if (pendingRecords.isNotEmpty()) {
                            TextButton(onClick = { onNavigate("approvals") }) {
                                Text("View All Approvals", color = OrthodoxBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    
                    if (pendingRecords.isEmpty()) {
                        ReminderItem(
                            title = "Consolidated Report",
                            message = "Quarterly diocese performance report is due in 3 days.",
                            icon = Icons.Default.Event,
                            color = InfoBlue
                        ) { onNavigate("reports") }
                    } else {
                        // Show Preview List of Approvals
                        pendingRecords.take(2).forEach { record ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OrthodoxGold.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(shape = CircleShape, color = OrthodoxGold.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) {
                                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = OrthodoxGold, modifier = Modifier.padding(12.dp))
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(record.source, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            val church = churches.find { it.id == record.churchId }
                                            Text("Parish: ${church?.name ?: "Local Parish"}", fontSize = 12.sp, color = TextSecondary)
                                        }
                                        Text("ETB ${String.format("%,.0f", record.amount)}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
                                    }
                                    Spacer(Modifier.height(20.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedButton(
                                            onClick = { viewModel.rejectIncome(record.id) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f))
                                        ) {
                                            Text("Reject")
                                        }
                                        Button(
                                            onClick = { viewModel.approveIncome(record.id) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                                        ) {
                                            Text("Approve")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 4. CHURCH PERFORMANCE OVERVIEW (REAL CONTROL) ---
            item {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Church-Level Control", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextPrimary)
                        TextButton(onClick = { onNavigate("org_management") }) { Text("Manage All Parishes", fontWeight = FontWeight.Bold) }
                    }
                    Spacer(Modifier.height(16.dp))
                    
                    if (churches.isEmpty()) {
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = SurfaceWhite) {
                            Text("Initializing parish monitoring...", color = TextSecondary, modifier = Modifier.padding(24.dp))
                        }
                    } else {
                        churches.take(3).forEach { church ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { onNavigate(Screen.ChurchDetail.createRoute(church.id)) },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(shape = RoundedCornerShape(14.dp), color = OrthodoxBlue.copy(alpha = 0.1f), modifier = Modifier.size(52.dp)) {
                                        Icon(Icons.Default.Church, contentDescription = null, tint = OrthodoxBlue, modifier = Modifier.padding(14.dp))
                                    }
                                    Spacer(Modifier.width(16.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(church.name, fontWeight = FontWeight.Black, fontSize = 16.sp, color = TextPrimary)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(color = SuccessGreen, shape = CircleShape, modifier = Modifier.size(6.dp)) {}
                                            Spacer(Modifier.width(6.dp))
                                            Text("Active Control", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("ETB 0.00", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                        Text("Balance", fontSize = 10.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 5. PERFORMANCE ANALYTICS (INTERACTIVE) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Performance Analytics", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
                                Text("Real-time revenue stream", fontSize = 12.sp, color = TextSecondary)
                            }
                            IconButton(onClick = { onNavigate("reports") }) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Full Analytics", tint = OrthodoxBlue)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        Box(Modifier.height(180.dp).fillMaxWidth()) {
                            SimpleBarChart(
                                data = listOf(45f, 52f, 48f, 65f, 70f, 75f),
                                labels = listOf("Mes", "Tik", "Hid", "Tah", "Tir", "Yak"),
                                barColor = OrthodoxBlue,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // --- 6. ACTIONABLE REPORTING & AUDIT ---
            item {
                Column {
                    Text("Reporting & Integrity", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextPrimary)
                    Spacer(Modifier.height(16.dp))
                    ManagementActionHorizontal(
                        title = "Generate Reports", 
                        subtitle = "Quarterly, Semi-annual, and Annual audits", 
                        icon = Icons.Default.Assessment, 
                        color = Color(0xFF0EA5E9)
                    ) { onNavigate("reports") }
                    Spacer(Modifier.height(12.dp))
                    ManagementActionHorizontal(
                        title = "Audit & Monitoring", 
                        subtitle = "Full transparency and system activity tracking", 
                        icon = Icons.Default.Security, 
                        color = Color(0xFF8B5CF6)
                    ) { onNavigate("audit_logs") }
                }
            }
        }
    }
}

@Composable
private fun PerformanceMetricCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun ReminderItem(title: String, message: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = color, modifier = Modifier.size(36.dp)) {
                Icon(icon, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(8.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                Text(message, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun ParishListItem(church: Church, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = OrthodoxBlue.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Church, contentDescription = null, tint = OrthodoxBlue, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(church.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(church.location ?: "Ethiopia", fontSize = 12.sp, color = TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun DioceseDashboardContent(churchesCount: Int, income: Double, expense: Double, format: (Double) -> String) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Stats Section - Using a more spacious layout
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AdminStatCardHorizontal(
                    label = "Active Parishes",
                    value = "$churchesCount",
                    icon = Icons.Default.Church,
                    color = OrthodoxBlue
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    AdminStatCardSmall(
                        label = "Revenue",
                        value = format(income),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    AdminStatCardSmall(
                        label = "Expenses",
                        value = format(expense),
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        color = ErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Revenue Performance", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                        Text("Last 6 Months", fontSize = 12.sp, color = TextSecondary)
                    }
                    Spacer(Modifier.height(24.dp))
                    val strings = com.example.orthodoxapp.util.LocalAppStrings.current
                    Box(Modifier.height(220.dp).fillMaxWidth()) {
                        SimpleBarChart(
                            data = listOf(45f, 52f, 48f, 65f, 70f, 75f, 80f, 85f, 90f, 95f, 100f, 110f),
                            labels = strings.ethiopianMonths.take(12).map { it.take(3) },
                            barColor = OrthodoxBlue,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = OrthodoxGold.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrthodoxGold.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = OrthodoxGold, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(6.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Text("Pro Insight: Parish contributions are up 12% this quarter. Schedule a review with the finance committee to allocate surplus funds.", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun AdminStatCardHorizontal(label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = color.copy(alpha = 0.1f), modifier = Modifier.size(56.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(14.dp))
            }
            Column {
                Text(label, fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Text(value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            }
        }
    }
}

@Composable
fun AdminStatCardSmall(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text(label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun ChurchListContent(churches: List<Church>, onNavigate: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(churches) { church ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigate("church_dashboard") },
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(52.dp), shape = RoundedCornerShape(12.dp), color = OrthodoxBlue.copy(alpha = 0.1f)) {
                        Icon(Icons.Default.Church, contentDescription = null, tint = OrthodoxBlue, modifier = Modifier.padding(14.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(church.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text(church.location ?: "Ethiopia", fontSize = 12.sp, color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Active", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialContent(incomes: List<Income>, expenses: List<Expense>, format: (Double) -> String) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text("Recent Diocese Income", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary) }
        items(incomes.take(5)) { income: Income ->
            FinancialRow(income.source, format(income.amount), SuccessGreen, Icons.Default.ArrowCircleUp)
        }
        item { Spacer(Modifier.height(16.dp)); Text("Recent Diocese Expenses", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary) }
        items(expenses.take(5)) { expense: Expense ->
            FinancialRow(expense.category, format(expense.amount), ErrorRed, Icons.Default.ArrowCircleDown)
        }
    }
}

@Composable
fun FinancialRow(label: String, amount: String, color: Color, icon: ImageVector) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = SurfaceWhite), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(label, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Text(amount, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        }
    }
}
