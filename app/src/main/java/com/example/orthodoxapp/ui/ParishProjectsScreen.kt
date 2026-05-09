package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParishProjectsScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Strategic Development", fontWeight = FontWeight.Bold, color = PureLinen)
                        Text("Parish Growth & Projects", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = PureLinen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrthodoxBlue)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Add Project */ }, containerColor = OrthodoxGold, contentColor = Color.Black) {
                Icon(Icons.Default.Add, contentDescription = "New Project")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundLight),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                StrategyHeader()
            }

            item {
                Text("Active Strategic Projects", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
                Text("Tracking long-term church development", fontSize = 12.sp, color = TextSecondary)
            }

            items(mockProjects) { project ->
                ProjectCard(project)
            }

            item {
                Text("Quarterly Goals", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
            }

            items(mockGoals) { goal ->
                GoalCard(goal)
            }
        }
    }
}

@Composable
private fun StrategyHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = OrthodoxBlue),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = OrthodoxGold.copy(alpha = 0.2f), shape = CircleShape) {
                    Icon(Icons.Default.AutoGraph, contentDescription = null, tint = OrthodoxGold, modifier = Modifier.padding(12.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text("2026 Parish Vision", color = PureLinen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("Building a digitally resilient and spiritually vibrant community for the next generation.", color = PureLinen.copy(alpha = 0.8f), fontSize = 14.sp)
        }
    }
}

data class ChurchProject(val title: String, val category: String, val progress: Float, val budget: String, val deadline: String)

@Composable
private fun ProjectCard(project: ChurchProject) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(project.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text(project.category, fontSize = 12.sp, color = TextSecondary)
                }
                Surface(color = InfoBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                    Text("${(project.progress * 100).toInt()}%", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = InfoBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { project.progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = InfoBlue,
                trackColor = InfoBlue.copy(alpha = 0.1f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(project.budget, fontSize = 12.sp, color = TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(project.deadline, fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

data class StrategicGoal(val title: String, val status: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val color: Color)

@Composable
private fun GoalCard(goal: StrategicGoal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.5.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = goal.color.copy(alpha = 0.1f)) {
                Icon(goal.icon, contentDescription = null, tint = goal.color, modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(goal.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text(goal.status, fontSize = 11.sp, color = TextSecondary)
            }
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = if (goal.status == "Completed") SuccessGreen else Color.LightGray)
        }
    }
}

val mockProjects = listOf(
    ChurchProject("Temple Roof Renovation", "Infrastructure", 0.65f, "Birr 1.2M", "Dec 2026"),
    ChurchProject("Sunday School Building", "Education", 0.30f, "Birr 2.5M", "Aug 2027"),
    ChurchProject("Digital Archive Project", "IT & Media", 0.90f, "Birr 150K", "Nov 2026")
)

val mockGoals = listOf(
    StrategicGoal("Register 500 New Members", "On Track", Icons.Default.PersonAdd, SuccessGreen),
    StrategicGoal("Launch Digital Giving App", "Completed", Icons.Default.MobileFriendly, InfoBlue),
    StrategicGoal("Ordination of 5 New Deacons", "Pending", Icons.Default.HowToReg, OrthodoxGold)
)
