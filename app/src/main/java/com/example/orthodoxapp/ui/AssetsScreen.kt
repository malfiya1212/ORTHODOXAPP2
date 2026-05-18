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
import com.example.orthodoxapp.data.model.ChurchAsset
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(viewModel: FinancialViewModel, onAddAsset: () -> Unit = {}, onBack: () -> Unit) {
    val assets by viewModel.assets.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Sacred Registry", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
                        Text("Assets & Property", fontWeight = FontWeight.Bold, color = PureLinen)
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
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = OrthodoxGold) {
                Icon(Icons.Default.AddHomeWork, contentDescription = "Add Asset", tint = Color.Black)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundLight),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AssetPortfolioCard(assets.sumOf { it.value })
            }
            
            item {
                Text("Ecclesiastical Asset Registry", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
            }

            if (assets.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No assets recorded for this location.", color = TextSecondary)
                    }
                }
            } else {
                items(assets) { asset ->
                    AssetCard(asset)
                }
            }
        }
    }

    if (showAddDialog) {
        AddAssetDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, value, desc ->
                viewModel.addAsset(name, type, value, currentUser?.churchId ?: 1L, desc)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssetDialog(onDismiss: () -> Unit, onConfirm: (String, String, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Sacred Object") }
    var value by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val types = listOf("Land", "Building", "Vehicle", "Sacred Object", "Furniture", "Electronics")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New Asset") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Asset Name") }, modifier = Modifier.fillMaxWidth())
                
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = { _ -> },
                        readOnly = true,
                        label = { Text("Asset Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        types.forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { type = t; expanded = false })
                        }
                    }
                }

                OutlinedTextField(value = value, onValueChange = { value = it }, label = { Text("Estimated Value (ETB)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, type, value.toDoubleOrNull() ?: 0.0, desc) }, enabled = name.isNotEmpty() && value.isNotEmpty()) {
                Text("Register Asset")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AssetPortfolioCard(totalValue: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(28.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = SuccessGreen.copy(alpha = 0.2f), modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = SuccessGreen, modifier = Modifier.padding(8.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text("Total Asset Valuation", color = PureLinen.copy(alpha = 0.6f), fontSize = 14.sp)
            }
            Spacer(Modifier.height(20.dp))
            Text("ETB ${String.format("%,.0f", totalValue)}", color = PureLinen, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Text("Estimated market value of all property", color = PureLinen.copy(alpha = 0.4f), fontSize = 11.sp)
        }
    }
}

@Composable
fun AssetCard(asset: ChurchAsset) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = when(asset.type) {
                    "Land" -> Color(0xFFDCFCE7)
                    "Sacred Object" -> Color(0xFFFEF3C7)
                    else -> Color(0xFFDBEAFE)
                }
            ) {
                Icon(
                    imageVector = when(asset.type) {
                        "Land" -> Icons.Default.Landscape
                        "Sacred Object" -> Icons.Default.HistoryEdu
                        "Vehicle" -> Icons.Default.DirectionsCar
                        else -> Icons.Default.Inventory
                    },
                    contentDescription = null,
                    tint = when(asset.type) {
                        "Land" -> Color(0xFF166534)
                        "Sacred Object" -> Color(0xFF92400E)
                        else -> Color(0xFF1E40AF)
                    },
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(asset.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                Text(asset.type, color = TextSecondary, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("ETB ${String.format("%,.0f", asset.value)}", fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text(asset.condition, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if(asset.condition == "EXCELLENT") SuccessGreen else Color.Gray)
            }
        }
    }
}
