package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.orthodoxapp.data.model.FinancialTransaction
import com.example.orthodoxapp.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsListScreen(viewModel: FinancialViewModel, onBack: () -> Unit = {}, modifier: Modifier = Modifier) {
    val transactions by viewModel.transactions.collectAsState()

    // Group transactions by date string
    val groupedTransactions = remember(transactions) {
        transactions.groupBy {
            SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(it.date))
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Financial History", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
                        Text("Transactions", fontWeight = FontWeight.Bold, color = PureLinen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrthodoxBlue,
                    titleContentColor = PureLinen,
                    navigationIconContentColor = PureLinen,
                    actionIconContentColor = PureLinen
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLight)
        ) {
            if (transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No transactions found.", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    groupedTransactions.forEach { (dateStr, transList) ->
                        item {
                            Text(
                                text = dateStr,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                fontSize = 12.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(transList) { transaction ->
                            TransactionListItem(transaction)
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(transaction: FinancialTransaction) {
    val isIncome = transaction.type == "INCOME"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = transaction.description,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "${if (isIncome) "+" else "-"}${String.format("%,.2f", transaction.amount)} Birr",
            color = if (isIncome) Color(0xFF388E3C) else Color(0xFFD32F2F),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
