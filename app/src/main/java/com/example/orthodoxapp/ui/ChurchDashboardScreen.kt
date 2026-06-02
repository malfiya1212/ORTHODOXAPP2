package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.orthodoxapp.ui.components.AdminStatCard
import com.example.orthodoxapp.ui.components.ManagementActionCard
import com.example.orthodoxapp.ui.components.ManagementActionHorizontal
import com.example.orthodoxapp.ui.components.SimpleBarChart
import com.example.orthodoxapp.ui.theme.*
import com.example.orthodoxapp.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChurchDashboardScreen(
    viewModel: FinancialViewModel,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onNavigate: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val incomeRecords by viewModel.income.collectAsState()
    val expenseRecords by viewModel.expenses.collectAsState()
    val churches by viewModel.churches.collectAsState()
    
    val currentChurch = churches.find { it.id == currentUser?.churchId }
    val churchName = currentChurch?.name ?: "Local Parish"
    
    val totalIncome = incomeRecords.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val totalExpense = expenseRecords.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val pendingIncome = incomeRecords.filter { it.status == "PENDING" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense
    
    // Ethiopian Monthly calculation for graph
    val strings = com.example.orthodoxapp.util.LocalAppStrings.current
    val monthlyData = (0..11).map { monthIdx ->
        incomeRecords.filter { 
            com.example.orthodoxapp.util.EthiopianDateUtils.getEthiopianMonth(it.date) == monthIdx
        }.sumOf { it.amount }.toFloat()
    }
    val monthLabels = listOf("Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug")


    val users by viewModel.users.collectAsState()
    val memberCount = users.filter { it.roleId == 6L }.size
    val pendingRecords = incomeRecords.filter { it.status == "PENDING" }

    Scaffold(
        modifier = modifier,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OrthodoxBlue,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentUser?.name ?: "User",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PureLinen
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = OrthodoxGold, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Administrator of $churchName",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OrthodoxGold,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isRefreshing by viewModel.isRefreshing.collectAsState()
                            IconButton(onClick = { viewModel.refreshData() }) {
                                if (isRefreshing) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OrthodoxGold, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PureLinen)
                                }
                            }

                            var showMenu by remember { mutableStateOf(false) }
                            
                            IconButton(onClick = { onNavigate("notifications") }) {
                                BadgedBox(badge = { if (pendingRecords.isNotEmpty()) Badge { Text(pendingRecords.size.toString()) } }) {
                                    Icon(Icons.Default.Notifications, contentDescription = strings.notifications, tint = PureLinen)
                                }
                            }
                            
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
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddIncome() },
                containerColor = OrthodoxGold,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Record")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundLight),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- 1. FINANCIAL SUMMARY ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = OrthodoxBlueDark),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Column(modifier = Modifier.padding(28.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = OrthodoxGold.copy(alpha = 0.2f), modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = OrthodoxGold, modifier = Modifier.padding(8.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text("Net Parish Treasury", color = PureLinen.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "ETB ${String.format("%,.2f", balance)}",
                            color = PureLinen,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Income", color = PureLinen.copy(alpha = 0.5f), fontSize = 12.sp)
                                Text("ETB ${String.format("%,.0f", totalIncome)}", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Expenses", color = PureLinen.copy(alpha = 0.5f), fontSize = 12.sp)
                                Text("ETB ${String.format("%,.0f", totalExpense)}", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            }

            // --- 2. QUICK ACTIONS GRID (FUNCTIONAL) ---
            item {
                var showAnnounceDialog by remember { mutableStateOf(false) }
                if (showAnnounceDialog) {
                    var announceType by remember { mutableStateOf("Announcement") }
                    var announceTypeExpanded by remember { mutableStateOf(false) }
                    val typeOptions = listOf("Announcement", "Event")
                    
                    var announceTitle by remember { mutableStateOf("") }
                    var announceMessage by remember { mutableStateOf("") }
                    AlertDialog(
                        onDismissRequest = { showAnnounceDialog = false },
                        title = { Text("Announce Event / Program") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ExposedDropdownMenuBox(
                                    expanded = announceTypeExpanded,
                                    onExpandedChange = { announceTypeExpanded = !announceTypeExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = announceType,
                                        onValueChange = { },
                                        readOnly = true,
                                        label = { Text("Type") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = announceTypeExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = announceTypeExpanded,
                                        onDismissRequest = { announceTypeExpanded = false }
                                    ) {
                                        typeOptions.forEach { selectionOption ->
                                            DropdownMenuItem(
                                                text = { Text(selectionOption) },
                                                onClick = {
                                                    announceType = selectionOption
                                                    announceTypeExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = announceTitle,
                                    onValueChange = { announceTitle = it },
                                    label = { Text(if (announceType == "Event") "Event Title" else "Announcement Title") },
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = announceTitle.isBlank(),
                                    supportingText = if (announceTitle.isBlank()) { { Text("Title cannot be empty") } } else null
                                )
                                OutlinedTextField(
                                    value = announceMessage,
                                    onValueChange = { announceMessage = it },
                                    label = { Text("Details & Information") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    isError = announceMessage.isBlank(),
                                    supportingText = if (announceMessage.isBlank()) { { Text("Message cannot be empty") } } else null
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (announceTitle.isNotBlank() && announceMessage.isNotBlank()) {
                                        val fullTitle = "[$announceType] $announceTitle"
                                        viewModel.pushAnnouncement(fullTitle, announceMessage)
                                        showAnnounceDialog = false
                                    }
                                },
                                enabled = announceTitle.isNotBlank() && announceMessage.isNotBlank()
                            ) {
                                Text("Push to Members")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAnnounceDialog = false }) { Text("Cancel") }
                        }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ManagementActionCard("Income\nEntry", Icons.Default.AddCard, SuccessGreen, Modifier.weight(1f)) { onAddIncome() }
                    ManagementActionCard("Expense\nEntry", Icons.Default.Payments, ErrorRed, Modifier.weight(1f)) { onAddExpense() }
                }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ManagementActionCard("Approve\nFunds", Icons.AutoMirrored.Filled.FactCheck, WarningOrange, Modifier.weight(1f)) { onNavigate("approvals") }
                    ManagementActionCard("Audit\nReports", Icons.Default.Assessment, InfoBlue, Modifier.weight(1f)) { onNavigate("reports") }
                }
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ManagementActionCard("Announce\nEvent", Icons.Default.Campaign, OrthodoxGoldDark, Modifier.weight(1f)) { showAnnounceDialog = true }
                    Spacer(Modifier.weight(1f)) // Empty placeholder to keep grid balanced
                }
            }

            // --- 3. APPROVAL WORKFLOW ---
            if (pendingRecords.isNotEmpty()) {
                item {
                    var selectedRecord by remember { mutableStateOf<Income?>(null) }
                    
                    if (selectedRecord != null) {
                        AlertDialog(
                            onDismissRequest = { selectedRecord = null },
                            title = { Text("Transaction Details", fontWeight = FontWeight.ExtraBold) },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    val memberName = users.find { it.id == selectedRecord?.createdBy }?.name ?: "Unknown Member"
                                    DetailItem("Submitted By", memberName)
                                    DetailItem("Source", selectedRecord?.source ?: "")
                                    DetailItem("Amount", "ETB ${String.format("%,.2f", selectedRecord?.amount)}")
                                    DetailItem("Category", selectedRecord?.category ?: "Offering")
                                    DetailItem("Reference", selectedRecord?.referenceNumber ?: "N/A")
                                    DetailItem("Payment Method", selectedRecord?.paymentMethod ?: "Cash")
                                    DetailItem("Date", java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(selectedRecord?.date ?: 0L)))
                                    if (!selectedRecord?.description.isNullOrBlank()) {
                                        DetailItem("Description", selectedRecord?.description ?: "")
                                    }
                                }
                            },
                            confirmButton = {
                                Button(onClick = { 
                                    viewModel.approveIncome(selectedRecord!!.id)
                                    selectedRecord = null 
                                }, colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)) {
                                    Text("Approve Now")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { selectedRecord = null }) { Text("Close") }
                            }
                        )
                    }

                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Awaiting Blessing", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextPrimary)
                            Text("${pendingRecords.size} Pending", fontSize = 12.sp, color = WarningOrange, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(16.dp))
                        pendingRecords.take(2).forEach { record ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable { selectedRecord = record },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WarningOrange.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(shape = CircleShape, color = WarningOrange.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) {
                                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = WarningOrange, modifier = Modifier.padding(12.dp))
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            val memberName = users.find { it.id == record.createdBy }?.name ?: "Unknown Member"
                                            Text(memberName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text(record.source, fontSize = 13.sp, color = OrthodoxBlue, fontWeight = FontWeight.Medium)
                                            Text("Ref: ${record.referenceNumber ?: "N/A"}", fontSize = 12.sp, color = TextSecondary)
                                            Text("Category: ${record.category ?: "Offering"}", fontSize = 11.sp, color = TextSecondary)
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

            // --- 4. MEMBER SUMMARY (REAL DATA) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigate(Screen.Members.route) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF))
                ) {
                    Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = Color(0xFF0EA5E9).copy(alpha = 0.1f), modifier = Modifier.size(56.dp)) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = Color(0xFF0EA5E9), modifier = Modifier.padding(14.dp))
                        }
                        Spacer(Modifier.width(20.dp))
                        Column {
                            Text("Total Parish Population", fontSize = 13.sp, color = Color(0xFF0369A1), fontWeight = FontWeight.Bold)
                            Text("$memberCount Registered Members", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0C4A6E))
                            Text("Real-time parish data synchronization", fontSize = 11.sp, color = Color(0xFF0EA5E9))
                        }
                    }
                }
            }

            // --- 4. EXPENSE OVERSIGHT (DAILY/MONTHLY/ANNUAL) ---
            item {
                var selectedPeriod by remember { mutableIntStateOf(1) } // 0: Daily, 1: Monthly, 2: Annual
                val periods = listOf("Daily", "Monthly", "Annual")
                val cal = Calendar.getInstance()
                val startTime = when(selectedPeriod) {
                    0 -> { cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.timeInMillis }
                    1 -> { cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.timeInMillis }
                    else -> { cal.set(Calendar.MONTH, Calendar.JANUARY); cal.set(Calendar.DAY_OF_MONTH, 1); cal.timeInMillis }
                }
                val periodExpense = expenseRecords.filter { it.status == "APPROVED" && it.date >= startTime }.sumOf { it.amount }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Expense Oversight", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                            Surface(shape = RoundedCornerShape(8.dp), color = ErrorRed.copy(alpha = 0.1f)) {
                                Text("ETB ${String.format("%,.0f", periodExpense)}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        TabRow(
                            selectedTabIndex = selectedPeriod,
                            containerColor = Color.Transparent,
                            contentColor = OrthodoxBlue,
                            divider = {},
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedPeriod]),
                                    color = OrthodoxBlue,
                                    height = 2.dp
                                )
                            }
                        ) {
                        periods.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedPeriod == index,
                                    onClick = { selectedPeriod = index },
                                    text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedPeriod == index) FontWeight.Bold else FontWeight.Normal) }
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        val filteredPeriodExpenses = expenseRecords.filter { it.status == "APPROVED" && it.date >= startTime }.sortedByDescending { it.date }.take(3)
                        
                        if (filteredPeriodExpenses.isEmpty()) {
                            Text("No expenses recorded for this period.", fontSize = 11.sp, color = TextSecondary, modifier = Modifier.padding(vertical = 8.dp))
                        } else {
                            filteredPeriodExpenses.forEach { exp ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(exp.category, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(java.util.Date(exp.date)), fontSize = 10.sp, color = TextSecondary)
                                    }
                                    Text("-ETB ${String.format("%,.0f", exp.amount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                                }
                            }
                            if (expenseRecords.filter { it.status == "APPROVED" && it.date >= startTime }.size > 3) {
                                TextButton(
                                    onClick = { onNavigate("reports") },
                                    modifier = Modifier.align(Alignment.End),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("View All", fontSize = 11.sp, color = OrthodoxBlue)
                                }
                            }
                        }
                    }
                }
            }

            // --- 5. REVENUE CHART ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Revenue Flow", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                            Text("Annual Performance (EC)", fontSize = 12.sp, color = TextSecondary)
                        }
                        Spacer(Modifier.height(24.dp))
                        Box(modifier = Modifier.height(180.dp).fillMaxWidth()) {
                            SimpleBarChart(
                                data = if (monthlyData.all { it == 0f }) listOf(10f, 25f, 15f, 35f, 30f, 45f) else monthlyData,
                                labels = monthLabels,
                                barColor = OrthodoxBlue,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // --- 5. RECENT TRANSACTIONS ---
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent Transactions", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
                    TextButton(onClick = { onNavigate("transactions") }) { Text("View All") }
                }
            }

            val recentRecords = (incomeRecords + expenseRecords).sortedByDescending { 
                when(it) {
                    is Income -> it.date
                    is Expense -> it.date
                    else -> 0L
                }
            }.take(5)
            
            if (recentRecords.isEmpty()) {
                item {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = SurfaceWhite) {
                        Text("No recent activity recorded.", color = TextSecondary, modifier = Modifier.padding(24.dp))
                    }
                }
            } else {
                items(recentRecords) { record: Any ->
                    val (title, amount, color) = when(record) {
                        is Income -> Triple(record.source, "+ETB ${String.format("%,.0f", record.amount)}", SuccessGreen)
                        is Expense -> Triple(record.category, "-ETB ${String.format("%,.0f", record.amount)}", ErrorRed)
                        else -> Triple("Unknown", "0", Color.Gray)
                    }
                    TransactionItemRow(title, amount, color)
                }
            }


            // --- 7. ADMIN TOOLS ---
            item {
                Text("Administrative Operations", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ManagementActionHorizontal(title = "Parish Personnel", subtitle = "Clergy & church staff registry", icon = Icons.Default.PeopleAlt, color = Color(0xFF10B981)) { onNavigate("personnel") }
                    ManagementActionHorizontal(title = "Member Registry", subtitle = "Manage tithes & parish members", icon = Icons.Default.Groups, color = Color(0xFF0EA5E9)) { onNavigate(Screen.Members.route) }
                    ManagementActionHorizontal(title = "Accounts & Treasury", subtitle = "Manage bank accounts & funds", icon = Icons.Default.AccountBalance, color = Color(0xFFF59E0B)) { onNavigate(Screen.Treasury.route) }
                }
            }

            // --- 8. STRATEGIC MANAGEMENT ---
            item {
                Text("Strategic Growth", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ManagementActionHorizontal(title = "Chure Groups", subtitle = "Traditional mutual aid systems", icon = Icons.Default.Groups, color = Color(0xFF06B6D4)) { onNavigate("chure") }
                    ManagementActionHorizontal(title = "Parish Development", subtitle = "Strategic projects & vision 2026", icon = Icons.AutoMirrored.Filled.TrendingUp, color = Color(0xFF8B5CF6)) { onNavigate("projects") }
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(title: String, amount: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                    Icon(
                        if (color == SuccessGreen) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
            }
            Text(amount, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 16.sp)
        }
    }
}
@Composable
fun DetailItem(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
