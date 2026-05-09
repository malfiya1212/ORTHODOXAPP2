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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
fun MemberManagementScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf("Meskerem") }
    
    val ethiopianMonths = listOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas", "Tir", "Yekatit", 
        "Megabit", "Miyazia", "Ginbot", "Sene", "Hamle", "Nehasse"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Member & Tithe Registry", fontWeight = FontWeight.Bold, color = PureLinen)
                        Text("Parishioner Management", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
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
            FloatingActionButton(onClick = { /* Add Member */ }, containerColor = OrthodoxGold, contentColor = Color.Black) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Member")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundLight)) {
            // Search and Filter Header
            Surface(color = OrthodoxBlue, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by name or ID...", color = PureLinen.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PureLinen) },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = PureLinen.copy(alpha = 0.3f),
                            focusedBorderColor = OrthodoxGold,
                            cursorColor = OrthodoxGold,
                            focusedTextColor = PureLinen,
                            unfocusedTextColor = PureLinen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    ScrollableTabRow(
                        selectedTabIndex = ethiopianMonths.indexOf(selectedMonth),
                        containerColor = Color.Transparent,
                        contentColor = PureLinen,
                        edgePadding = 0.dp,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[ethiopianMonths.indexOf(selectedMonth)]),
                                color = OrthodoxGold
                            )
                        }
                    ) {
                        ethiopianMonths.forEach { month ->
                            Tab(
                                selected = selectedMonth == month,
                                onClick = { selectedMonth = month },
                                text = { Text(month, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Monthly Gisat Overview", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                }

                items(mockMembers.filter { it.name.contains(searchQuery, ignoreCase = true) }) { member ->
                    MemberRow(member, selectedMonth)
                }
            }
        }
    }
}

@Composable
fun MemberRow(member: ParishMember, month: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = OrthodoxBlue.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(member.name.take(1), fontWeight = FontWeight.Bold, color = OrthodoxBlue, fontSize = 18.sp)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(member.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text("Member ID: ${member.idCode}", fontSize = 11.sp, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                val isPaid = member.paymentStatus[month] ?: false
                Surface(
                    color = if (isPaid) SuccessGreen.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (isPaid) "Paid" else "Pending",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = if (isPaid) SuccessGreen else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                if (isPaid) {
                    Text("Birr ${member.monthlyAmount}", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }
    }
}

data class ParishMember(
    val name: String, 
    val idCode: String, 
    val monthlyAmount: Double, 
    val paymentStatus: Map<String, Boolean>
)

val mockMembers = listOf(
    ParishMember("Abebe Bikila", "P-001", 500.0, mapOf("Meskerem" to true, "Tikimt" to true, "Hidar" to false)),
    ParishMember("Tewodros Kassahun", "P-002", 1000.0, mapOf("Meskerem" to true, "Tikimt" to false, "Hidar" to false)),
    ParishMember("Mulugeta Tesfaye", "P-003", 250.0, mapOf("Meskerem" to true, "Tikimt" to true, "Hidar" to true)),
    ParishMember("Selamawit Yohannes", "P-004", 750.0, mapOf("Meskerem" to true, "Tikimt" to true, "Hidar" to false)),
    ParishMember("Gebre Egziabher", "P-005", 2000.0, mapOf("Meskerem" to false, "Tikimt" to false, "Hidar" to false))
)
