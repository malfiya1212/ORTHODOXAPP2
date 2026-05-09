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
import com.example.orthodoxapp.data.model.UserRole
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: FinancialViewModel, onBack: () -> Unit = {}, modifier: Modifier = Modifier) {
    val incomeRecords by viewModel.income.collectAsState()
    val expenseRecords by viewModel.expenses.collectAsState()
    val role by viewModel.currentRole.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(1) } // 0: Daily, 1: Monthly, 2: Annual
    val tabs = listOf("Daily", "Monthly", "Annual")

    val totalIncome = incomeRecords.filter { it.status == "APPROVED" }.sumOf { it.amount }
    val totalExpense = expenseRecords.filter { it.status == "APPROVED" }.sumOf { it.amount }
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
                            com.example.orthodoxapp.util.ReportExportUtility.exportToPdf(context, incomeRecords, expenseRecords, "${tabs[selectedTab]} Financial Report")
                        }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = PureLinen)
                        }
                        IconButton(onClick = { 
                            com.example.orthodoxapp.util.ReportExportUtility.exportToExcel(context, incomeRecords, expenseRecords, "${tabs[selectedTab]} Financial Report")
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
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
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
                        onClick = { com.example.orthodoxapp.util.ReportExportUtility.exportToPdf(context, incomeRecords, expenseRecords, "${tabs[selectedTab]} Financial Report") },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrthodoxBlue),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("PDF Report")
                    }
                    Button(
                        onClick = { com.example.orthodoxapp.util.ReportExportUtility.exportToExcel(context, incomeRecords, expenseRecords, "${tabs[selectedTab]} Financial Report") },
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
                        Text("Growth Analytics", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Spacer(Modifier.height(24.dp))
                        Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                            com.example.orthodoxapp.ui.components.SimpleBarChart(
                                data = listOf(45f, 60f, 55f, 75f, 85f, 90f),
                                labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
                                barColor = OrthodoxBlue,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
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

@Composable
fun DioceseProgressRow(diocese: String, progress: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(diocese, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun ReportSnapCard(label: String, amount: Double, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(36.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            Text(String.format(java.util.Locale.getDefault(), "%,.0f", amount), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }
    }
}
@Composable
fun HierarchyPathHeader(role: UserRole) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = OrthodoxGold.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, OrthodoxGold.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountTree, 
                contentDescription = null, 
                tint = OrthodoxGold, 
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(16.dp))
            val path = when(role) {
                UserRole.SYNOD_ADMIN -> "Holy Synod Council (National)"
                UserRole.DIOCESE_ADMIN -> "Synod > Diocese Administration"
                UserRole.CHURCH_ADMIN -> "Synod > Diocese > Parish"
                else -> "Tewahedo Hierarchy"
            }
            Text(
                text = path,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 0.5.sp
            )
        }
    }
}
