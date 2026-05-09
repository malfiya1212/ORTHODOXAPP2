package com.example.orthodoxapp.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.ui.theme.PrimaryBlue
import com.example.orthodoxapp.ui.components.SimpleDonutChart
import com.example.orthodoxapp.ui.components.SimpleBarChart
import com.example.orthodoxapp.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedAnalyticsScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    val bgColor = Color(0xFFF4F6F8)
    val cardBg = Color.White
    val textLight = Color(0xFF64748B)
    val greenColor = Color(0xFF10B981)
    val redColor = Color(0xFFEF4444)
    val goldColor = Color(0xFFD4AF37)

    val incomes by viewModel.income.collectAsState(initial = emptyList())
    val expenses by viewModel.expenses.collectAsState(initial = emptyList())

    val totalIncome = incomes.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }

    // Time filter
    var selectedPeriod by remember { mutableStateOf("Monthly") }
    val periods = listOf("Daily", "Monthly", "Yearly")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Analytics", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Selector
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    periods.forEach { period ->
                        val isSelected = selectedPeriod == period
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPeriod = period },
                            label = { Text(period, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // KPI Summary Cards
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticsKpiCard("Total Income", "Birr ${String.format("%,.0f", totalIncome)}", "+12.5%", greenColor, Modifier.weight(1f))
                    AnalyticsKpiCard("Total Expense", "Birr ${String.format("%,.0f", totalExpense)}", "-3.2%", redColor, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticsKpiCard("Net Balance", "Birr ${String.format("%,.0f", totalIncome - totalExpense)}", "+9.3%", PrimaryBlue, Modifier.weight(1f))
                    AnalyticsKpiCard("Growth Rate", "15.7%", "vs last period", goldColor, Modifier.weight(1f))
                }
            }

            // Income Growth Line Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Income Growth Trend", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("6-month performance", fontSize = 12.sp, color = textLight)
                            }
                            Surface(shape = CircleShape, color = greenColor.copy(alpha = 0.1f), modifier = Modifier.size(36.dp)) {
                                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = greenColor, modifier = Modifier.padding(8.dp))
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        // Line Chart
                        val dataPoints = listOf(120f, 180f, 150f, 220f, 280f, 350f)
                        val labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
                        LineChart(
                            data = dataPoints,
                            labels = labels,
                            lineColor = PrimaryBlue,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Pie Chart: Donation Types
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Donation Type Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Distribution of revenue sources", fontSize = 12.sp, color = textLight)
                        Spacer(Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            SimpleDonutChart(
                                data = listOf(42f, 28f, 18f, 12f),
                                colors = listOf(PrimaryBlue, goldColor, greenColor, Color(0xFF8B5CF6)),
                                modifier = Modifier.size(140.dp),
                                centerText = "100%",
                                centerSubText = "Total"
                            )
                            Spacer(Modifier.width(24.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                DonationLegendRow("Tithes", "42%", PrimaryBlue)
                                DonationLegendRow("Donations", "28%", goldColor)
                                DonationLegendRow("Special Offering", "18%", greenColor)
                                DonationLegendRow("Fees & Other", "12%", Color(0xFF8B5CF6))
                            }
                        }
                    }
                }
            }

            // Diocese Heatmap
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Diocese Performance Heatmap", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Contribution intensity by diocese", fontSize = 12.sp, color = textLight)
                        Spacer(Modifier.height(16.dp))

                        val dioceses = listOf(
                            Triple("Addis Ababa", 0.95f, "Birr 1.2M"),
                            Triple("Amhara", 0.78f, "Birr 890K"),
                            Triple("Oromia", 0.65f, "Birr 720K"),
                            Triple("Tigray", 0.52f, "Birr 580K"),
                            Triple("Sidama", 0.40f, "Birr 450K"),
                            Triple("Harar", 0.28f, "Birr 310K")
                        )

                        dioceses.forEach { (name, intensity, amount) ->
                            HeatmapRow(name, intensity, amount)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Monthly Comparison Bars
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Monthly Comparison", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Income vs Expense per month", fontSize = 12.sp, color = textLight)
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            SimpleBarChart(
                                data = listOf(8f, 6f, 9f, 7f, 10f, 8f),
                                labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
                                barColor = PrimaryBlue,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            ChartLegend("Income", PrimaryBlue)
                            Spacer(Modifier.width(24.dp))
                            ChartLegend("Expense", goldColor)
                        }
                    }
                }
            }
        }
    }
}

// --- Composable Components ---

@Composable
fun LineChart(data: List<Float>, labels: List<String>, lineColor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        val maxVal = data.max()
        val minVal = data.min()
        val range = if (maxVal == minVal) 1f else maxVal - minVal
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val paddingBottom = 30f

        // Draw grid lines
        for (i in 0..3) {
            val y = paddingBottom + (size.height - paddingBottom) * (1 - i / 3f)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
        }

        // Draw line path
        val path = Path()
        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = paddingBottom + (size.height - paddingBottom) * (1 - (value - minVal) / range)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))

        // Draw data points
        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = paddingBottom + (size.height - paddingBottom) * (1 - (value - minVal) / range)
            drawCircle(lineColor, radius = 5f, center = Offset(x, y))
            drawCircle(Color.White, radius = 3f, center = Offset(x, y))
        }

        // Draw labels
        labels.forEachIndexed { i, label ->
            val x = i * stepX
            drawContext.canvas.nativeCanvas.drawText(
                label, x, size.height, android.graphics.Paint().apply {
                    textSize = 28f
                    color = android.graphics.Color.GRAY
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
fun AnalyticsKpiCard(title: String, value: String, trend: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 12.sp, color = Color(0xFF64748B))
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(Modifier.height(4.dp))
            Text(trend, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun DonationLegendRow(label: String, percent: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, color = Color(0xFF1E293B))
        Spacer(Modifier.width(8.dp))
        Text(percent, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun HeatmapRow(diocese: String, intensity: Float, amount: String) {
    val heatColor = Color(
        red = (1f - intensity) * 0.9f + intensity * 0.06f,
        green = (1f - intensity) * 0.9f + intensity * 0.73f,
        blue = (1f - intensity) * 0.9f + intensity * 0.51f
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(diocese, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(100.dp))
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            LinearProgressIndicator(
                progress = { intensity },
                modifier = Modifier.fillMaxWidth().height(20.dp),
                color = heatColor,
                trackColor = Color(0xFFF1F5F9)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
    }
}

@Composable
fun ChartLegend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 13.sp, color = Color(0xFF1E293B))
    }
}
