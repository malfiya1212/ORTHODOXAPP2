package com.example.orthodoxapp.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.data.model.Diocese
import com.example.orthodoxapp.ui.components.AdminStatCard
import com.example.orthodoxapp.ui.components.ManagementActionHorizontal
import com.example.orthodoxapp.ui.components.SimpleBarChart
import com.example.orthodoxapp.ui.theme.*
import com.example.orthodoxapp.data.model.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: FinancialViewModel,
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit = {}
) {
    val dioceses by viewModel.dioceses.collectAsState(initial = emptyList())
    val churches by viewModel.churches.collectAsState(initial = emptyList())
    val incomes by viewModel.income.collectAsState(initial = emptyList())
    val expenses by viewModel.expenses.collectAsState(initial = emptyList())
    val user by viewModel.currentUser.collectAsState(initial = null)

    val totalIncome = incomes.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense
    val formatCurrency = { amount: Double -> "ETB ${String.format(java.util.Locale.getDefault(), "%,.2f", amount)}" }

    val users by viewModel.users.collectAsState(initial = emptyList())
    val membersCount = users.filter { it.roleId == 6L }.size
    val pendingRecords = incomes.filter { it.status == "PENDING" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Holy Synod of Ethiopia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = PureLinen)
                        Text("National Administrative Dashboard", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold, letterSpacing = 1.sp)
                    }
                },
                actions = {
                    val isRefreshing by viewModel.isRefreshing.collectAsState()
                    IconButton(onClick = { viewModel.refreshData() }) {
                        if (isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OrthodoxGold, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = PureLinen)
                        }
                    }

                    IconButton(onClick = { onNavigate("notifications") }) {
                        BadgedBox(badge = { if (pendingRecords.isNotEmpty()) Badge { Text(pendingRecords.size.toString()) } }) {
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // --- 1. NATIONAL EXECUTIVE SUMMARY ---
            item {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("National Summary", fontWeight = FontWeight.Black, fontSize = 22.sp, color = TextPrimary)
                        TextButton(onClick = { onNavigate("reports") }) {
                            Text("View Full Reports", color = OrthodoxBlue)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    
                    // Large Treasury Card
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigate("reports") },
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = OrthodoxBlueDark),
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(28.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = OrthodoxGold.copy(alpha = 0.2f), shape = CircleShape) {
                                    Icon(Icons.Default.Public, contentDescription = null, tint = OrthodoxGold, modifier = Modifier.padding(12.dp))
                                }
                                Spacer(Modifier.width(16.dp))
                                Text("National Church Treasury", color = PureLinen.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(24.dp))
                            Text(formatCurrency(netBalance), color = PureLinen, fontSize = 36.sp, fontWeight = FontWeight.Black)
                            
                            Spacer(Modifier.height(24.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("National Income", color = PureLinen.copy(alpha = 0.5f), fontSize = 11.sp)
                                    Text(formatCurrency(totalIncome), color = SuccessGreen, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("National Expense", color = PureLinen.copy(alpha = 0.5f), fontSize = 11.sp)
                                    Text(formatCurrency(totalExpense), color = ErrorRed, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Stats Grid
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatSmallCard("Dioceses", "${dioceses.size}", Icons.Default.LocationCity, InfoBlue, Modifier.weight(1f))
                        StatSmallCard("Churches", "${churches.size}", Icons.Default.AccountBalance, OrthodoxGold, Modifier.weight(1f))
                        StatSmallCard("Members", "$membersCount", Icons.Default.Groups, Color(0xFF10B981), Modifier.weight(1f))
                    }
                }
            }

            // --- 2. FINANCIAL GROWTH (CHART) ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Financial Performance", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
                            Text("Year-to-Date", fontSize = 12.sp, color = TextSecondary)
                        }
                        Spacer(Modifier.height(24.dp))
                        Box(Modifier.height(200.dp).fillMaxWidth()) {
                            SimpleBarChart(
                                data = listOf(60f, 75f, 50f, 90f, 100f, 120f, 110f, 95f, 130f, 140f, 125f, 150f),
                                labels = listOf("Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug"),
                                barColor = OrthodoxBlue,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // --- 3. NATIONAL CHURCH MAP VIEW ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = OrthodoxBlue.copy(alpha = 0.05f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OrthodoxBlue.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("National Church Map", fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
                            TextButton(onClick = { onNavigate("national_map") }) { Text("Explore Map") }
                        }
                        Spacer(Modifier.height(16.dp))
                        // Schematic Map Placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color.White, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(80.dp), tint = OrthodoxBlue.copy(alpha = 0.1f))
                            Text("Interactive Map of 12 Dioceses", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- 4. PERFORMANCE LEAGUE ---
            item {
                Column {
                    Text("Regional Performance", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextPrimary)
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        PerformanceTableCard("Top Performing", dioceses.take(3), SuccessGreen, Modifier.weight(1f))
                        PerformanceTableCard("Attention Needed", dioceses.takeLast(2), ErrorRed, Modifier.weight(1f))
                    }
                }
            }

            // --- 5. NATIONAL ACTIVITY & ALERTS ---
            item {
                Column {
                    Text("Recent National Activity", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextPrimary)
                    Spacer(Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ActivityItem("New Church Added", "St. Mary, Gondar Diocese", Icons.Default.AccountBalance, InfoBlue)
                        if (pendingRecords.isNotEmpty()) {
                            ActivityItem("${pendingRecords.size} Pending Approvals", "Total: ${formatCurrency(pendingRecords.sumOf { it.amount })}", Icons.Default.Warning, WarningOrange)
                        }
                    }
                }
            }

            // --- 6. DIOCESE MANAGEMENT GRID ---
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Diocese Management", fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextPrimary)
                        Text("Oversee all national territories", fontSize = 11.sp, color = TextSecondary)
                    }
                    Row {
                        IconButton(onClick = { onNavigate(Screen.AddOrganization.createRoute("Diocese", 0L)) }) {
                            Icon(Icons.Default.AddBusiness, contentDescription = "Add Diocese", tint = OrthodoxBlue)
                        }
                        TextButton(onClick = { onNavigate("org_management") }) { Text("View All") }
                    }
                }
            }

            items(dioceses) { diocese ->
                val dioceseChurches = churches.filter { it.dioceseId == diocese.id }
                DioceseListItem(
                    diocese = diocese, 
                    churches = dioceseChurches,
                    incomes = incomes,
                    expenses = expenses,
                    onViewDashboard = { onNavigate(Screen.DioceseDashboard.createRoute(diocese.id)) }
                )
            }
        }
    }
}

@Composable
private fun StatSmallCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = TextPrimary)
            Text(label.uppercase(), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        }
    }
}

@Composable
private fun PerformanceTableCard(title: String, dioceses: List<Diocese>, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
            Spacer(Modifier.height(12.dp))
            dioceses.forEach { d ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(d.name, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    Text("↑", color = color, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(title: String, subtitle: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier = modifier) {
        Text(label, color = PureLinen.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(16.dp))
            Text(label, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun DioceseListItem(
    diocese: Diocese, 
    churches: List<Church>, 
    incomes: List<Income>,
    expenses: List<Expense>,
    onViewDashboard: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Calculate Aggregate Financials for this Diocese
    val churchIds = churches.map { it.id }.toSet()
    val dioceseIncomes = incomes.filter { it.churchId in churchIds && it.status == "APPROVED" }
    val dioceseExpenses = expenses.filter { it.churchId in churchIds && it.status == "APPROVED" }
    
    val totalIncome = dioceseIncomes.sumOf { it.amount }
    val totalExpense = dioceseExpenses.sumOf { it.amount }
    val balance = totalIncome - totalExpense
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = OrthodoxBlue.copy(alpha = 0.1f)
                ) {
                    Icon(Icons.Default.LocationCity, contentDescription = null, tint = OrthodoxBlue, modifier = Modifier.padding(14.dp))
                }
                Spacer(Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(diocese.name, fontWeight = FontWeight.Black, fontSize = 20.sp, color = TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = OrthodoxGold, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("${churches.size} Total Parishes", fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("ETB ${String.format(java.util.Locale.getDefault(), "%,.0f", balance)}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = OrthodoxBlue)
                    Text("Balance", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }

            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    // DIOCESE-LEVEL FINANCIAL REPORT
                    Text("DIOCESE FINANCIAL REPORT", fontSize = 11.sp, fontWeight = FontWeight.Black, color = OrthodoxGold, letterSpacing = 1.2.sp)
                    Spacer(Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DioceseMiniStat("Aggregate Income", totalIncome, SuccessGreen, Modifier.weight(1f))
                        DioceseMiniStat("Aggregate Expense", totalExpense, ErrorRed, Modifier.weight(1f))
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    // About Diocese Information
                    Surface(
                        color = BackgroundLight.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Lead Administrator", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text(diocese.bishopName ?: "Bishop Not Assigned", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            VerticalDivider(modifier = Modifier.height(30.dp).padding(horizontal = 16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Headquarters", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text(diocese.location ?: "National Office", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = onViewDashboard,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrthodoxBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("View Full Regional Audit Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DioceseMiniStat(label: String, amount: Double, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.05f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            Text("ETB ${String.format(java.util.Locale.getDefault(), "%,.0f", amount)}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}
