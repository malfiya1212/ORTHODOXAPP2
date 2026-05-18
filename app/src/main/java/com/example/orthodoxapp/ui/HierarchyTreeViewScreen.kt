package com.example.orthodoxapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HierarchyTreeViewScreen(viewModel: FinancialViewModel, onBack: () -> Unit, onNavigate: (String) -> Unit = {}) {
    val dioceses by viewModel.dioceses.collectAsState(initial = emptyList())
    val churches by viewModel.churches.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Governance", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
                        Text("National Hierarchy", fontWeight = FontWeight.Bold, color = PureLinen)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = PureLinen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrthodoxBlue)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FE))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = OrthodoxBlue,
                    shadowElevation = 6.dp
                ) {
                    Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = OrthodoxGold, modifier = Modifier.size(56.dp)) {
                            Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color.Black, modifier = Modifier.padding(14.dp))
                        }
                        Spacer(Modifier.width(20.dp))
                        Column {
                            Text("Holy Synod", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold, fontWeight = FontWeight.Bold)
                            Text("Supreme Council", style = MaterialTheme.typography.headlineSmall, color = PureLinen, fontWeight = FontWeight.ExtraBold)
                            Text("National Governance Authority", fontSize = 11.sp, color = PureLinen.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            item {
                Text(
                    "Ecclesiastical Jurisdictions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            items(dioceses, key = { it.id }) { diocese ->
                DioceseNode(
                    diocese = diocese,
                    churches = churches.filter { it.dioceseId == diocese.id },
                    onNavigate = onNavigate
                )
            }
        }
    }
}

@Composable
fun DioceseNode(diocese: Diocese, churches: List<Church>, onNavigate: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        HierarchyItem(
            title = diocese.name,
            subtitle = "${churches.size} Churches",
            icon = Icons.Default.Public,
            level = 0,
            expanded = expanded,
            onToggle = { expanded = !expanded }
        )

        AnimatedVisibility(visible = expanded) {
            Column {
                churches.forEach { church ->
                    ChurchLeaf(church = church, level = 1, onNavigate = onNavigate)
                }
            }
        }
    }
}

@Composable
fun ChurchLeaf(church: Church, level: Int, onNavigate: (String) -> Unit) {
    HierarchyItem(
        title = church.name,
        subtitle = "Tap to view parish details",
        icon = Icons.Default.Church,
        level = level,
        expanded = false,
        onToggle = { onNavigate("church_detail/${church.id}") },
        isLeaf = false
    )
}

@Composable
fun HierarchyItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    level: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    isLeaf: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (level * 16).dp)
            .clickable(enabled = !isLeaf) { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = OrthodoxBlue.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    tint = OrthodoxBlue, 
                    modifier = Modifier.padding(8.dp).size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, fontSize = 11.sp, color = Color.Gray)
            }
            if (!isLeaf) {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }
    }
}
