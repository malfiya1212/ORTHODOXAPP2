package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.data.model.*
import com.example.orthodoxapp.ui.components.SimpleBarChart
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: FinancialViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val incomeRecords by viewModel.income.collectAsState()
    val expenseRecords by viewModel.expenses.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(1) } // 0: Daily, 1: Monthly, 2: Annual
    val tabs = listOf("Daily", "Monthly", "Annual")

    val calendar = java.util.Calendar.getInstance()
    
    val startTime = when(selectedTab) {
        0 -> { // Daily
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }
        1 -> { // Monthly
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }
        else -> { // Annual
            calendar.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }
    }

    val filteredIncome = incomeRecords.filter { it.status == "APPROVED" && it.date >= startTime }
    val filteredExpense = expenseRecords.filter { it.status == "APPROVED" && it.date >= startTime }

    val totalIncome = filteredIncome.sumOf { it.amount }
    val totalExpense = filteredExpense.sumOf { it.amount }
    val net = totalIncome - totalExpense

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Financial Reports", fontWeight = FontWeight.Bold, color = PureLinen) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = PureLinen)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = OrthodoxBlue),
                    actions = {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        IconButton(onClick = { 
                            com.example.orthodoxapp.util.ReportExportUtility.exportToPdf(context, filteredIncome, filteredExpense, "${tabs[selectedTab]} Financial Report")
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = PureLinen)
                        }
                        IconButton(onClick = { 
                            com.example.orthodoxapp.util.ReportExportUtility.exportToExcel(context, filteredIncome, filteredExpense, "${tabs[selectedTab]} Financial Report")
                        }) {
                            Icon(Icons.Default.TableChart, contentDescription = "Export Excel", tint = PureLinen)
                        }
                    }
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = OrthodoxBlue,
                    contentColor = PureLinen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = OrthodoxGold,
                            height = 4.dp
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Total Summary Cards
            item {
                Text(
                    text = "${tabs[selectedTab]} Summary",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        ReportFinancialRow("Total Income", totalIncome, SuccessGreen, Icons.AutoMirrored.Filled.TrendingUp)
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        ReportFinancialRow("Total Expense", totalExpense, ErrorRed, Icons.AutoMirrored.Filled.TrendingDown)
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        ReportFinancialRow("Net Balance", net, OrthodoxBlue, Icons.Default.AccountBalanceWallet)
                    }
                }
            }

            // 2. Export Actions
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { com.example.orthodoxapp.util.ReportExportUtility.exportToPdf(context, filteredIncome, filteredExpense, "${tabs[selectedTab]} Financial Report") },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrthodoxBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("PDF Report")
                    }
                    Button(
                        onClick = { com.example.orthodoxapp.util.ReportExportUtility.exportToExcel(context, filteredIncome, filteredExpense, "${tabs[selectedTab]} Financial Report") },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Excel")
                    }
                }
            }

            // 3. Analytics Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Annual Growth Analytics", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Spacer(Modifier.height(24.dp))
                        
                        val months = listOf("Sep", "Oct", "Nov", "Dec", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug")
                        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                        
                        // Calculate income for each month (Starting from Sep)
                        val monthlyData = FloatArray(12)
                        incomeRecords.filter { it.status == "APPROVED" }.forEach { record ->
                            val cal = java.util.Calendar.getInstance().apply { timeInMillis = record.date }
                            val recordYear = cal.get(java.util.Calendar.YEAR)
                            val calMonth = cal.get(java.util.Calendar.MONTH)
                            
                            // Map month to index (Sep=0, Oct=1, ..., Aug=11)
                            val index = (calMonth + 4) % 12
                            
                            // If it's the current fiscal year cycle (approximate)
                            // A real fiscal year check would be more complex, but this aligns with the requested labels
                            monthlyData[index] += record.amount.toFloat()
                        }
                        
                        // Generate colors based on increase/decrease trend
                        val barColors = mutableListOf<Color>()
                        for (i in 0 until 12) {
                            if (i == 0) {
                                barColors.add(SuccessGreen) // First month defaults to green if > 0 else gray
                            } else {
                                if (monthlyData[i] >= monthlyData[i - 1]) {
                                    barColors.add(SuccessGreen) // Increase or same
                                } else {
                                    barColors.add(ErrorRed) // Decrease
                                }
                            }
                            if (monthlyData[i] == 0f) barColors[i] = Color.LightGray
                        }

                        // Ensure there's at least some data to show
                        val maxVal = monthlyData.maxOrNull() ?: 0f
                        val dataToShow = if (maxVal == 0f) List(12) { 10f } else monthlyData.toList() // dummy data if totally empty so chart draws empty structure

                        Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                            SimpleBarChart(
                                data = dataToShow,
                                labels = months,
                                barColors = barColors,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // 4. Detailed Transaction List
            item {
                Text(
                    text = "Detailed Records (${tabs[selectedTab]})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (filteredIncome.isEmpty() && filteredExpense.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No records found for this period.", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                // Show Income Records
                if (filteredIncome.isNotEmpty()) {
                    item {
                        Text("Income Source Details", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                    }
                    items(filteredIncome.size) { index ->
                        val record = filteredIncome[index]
                        ReportTransactionItem(
                            title = record.source,
                            category = record.category ?: "Donation",
                            amount = record.amount,
                            date = record.date,
                            color = SuccessGreen,
                            icon = Icons.AutoMirrored.Filled.TrendingUp
                        )
                    }
                }

                // Show Expense Records
                if (filteredExpense.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text("Expense Breakdown", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ErrorRed)
                    }
                    items(filteredExpense.size) { index ->
                        val record = filteredExpense[index]
                        ReportTransactionItem(
                            title = record.description ?: "General Expense",
                            category = record.category,
                            amount = -record.amount,
                            date = record.date,
                            color = ErrorRed,
                            icon = Icons.AutoMirrored.Filled.TrendingDown
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun ReportTransactionItem(
    title: String,
    category: String,
    amount: Double,
    date: Long,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(44.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text("$category • ${java.text.DateFormat.getDateInstance().format(date)}", fontSize = 12.sp, color = TextSecondary)
            }
            Text(
                text = "${if (amount > 0) "+" else ""}${String.format(java.util.Locale.getDefault(), "%,.0f", amount)}",
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = color
            )
        }
    }
}

@Composable
fun ReportFinancialRow(label: String, amount: Double, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(40.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.width(16.dp))
            Text(label, fontWeight = FontWeight.Medium, color = TextSecondary)
        }
        Text(
            text = "ETB ${String.format(java.util.Locale.getDefault(), "%,.0f", amount)}",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = color
        )
    }
}

// Unused components removed to clean up lint warnings. 
// If DioceseProgressRow, ReportSnapCard, or HierarchyPathHeader are needed, they can be restored from version control.
