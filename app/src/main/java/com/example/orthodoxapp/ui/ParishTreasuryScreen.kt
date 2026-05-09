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
fun ParishTreasuryScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Parish Treasury", fontWeight = FontWeight.Bold, color = PureLinen)
                        Text("Central Fund Management", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
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
            modifier = Modifier.fillMaxSize().padding(padding).background(BackgroundLight),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Total Balance Card
            item {
                TreasuryOverviewCard()
            }

            item {
                Text("Management of Dedicated Funds", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
                Text("Balances across specific church accounts", fontSize = 12.sp, color = TextSecondary)
            }

            items(fundAccounts) { fund ->
                FundCard(fund)
            }

            item {
                Text("Registered Bank Accounts", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = TextPrimary)
            }

            items(bankAccounts) { bank ->
                BankCard(bank)
            }
        }
    }
}

@Composable
private fun TreasuryOverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = OrthodoxBlue),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total Treasury Balance", color = PureLinen.copy(alpha = 0.7f), fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Text("Birr 4,250,000.00", color = PureLinen, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TreasuryStat(label = "Liquid Cash", value = "Birr 850K", icon = Icons.Default.Payments)
                TreasuryStat(label = "Bank Deposits", value = "Birr 3.4M", icon = Icons.Default.AccountBalance)
            }
        }
    }
}

@Composable
private fun TreasuryStat(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        color = Color.White.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = OrthodoxGold, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(label, color = PureLinen.copy(alpha = 0.6f), fontSize = 10.sp)
                Text(value, color = PureLinen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

data class FundAccount(val name: String, val balance: String, val lastAction: String, val color: Color)

@Composable
private fun FundCard(fund: FundAccount) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = fund.color.copy(alpha = 0.1f)) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = fund.color, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(fund.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text("Last update: ${fund.lastAction}", fontSize = 11.sp, color = TextSecondary)
            }
            Text(fund.balance, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = fund.color)
        }
    }
}

data class BankAccount(val bankName: String, val accountNo: String, val branch: String, val balance: String)

@Composable
private fun BankCard(bank: BankAccount) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(bank.bankName, fontWeight = FontWeight.Bold, color = OrthodoxBlue)
                Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(bank.accountNo, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
            Text(bank.branch, fontSize = 12.sp, color = TextSecondary)
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Current Balance", fontSize = 12.sp, color = TextSecondary)
                Text(bank.balance, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
    }
}

val fundAccounts = listOf(
    FundAccount("General Parish Fund", "Birr 2,150,000", "2 hours ago", OrthodoxBlue),
    FundAccount("Development & Construction", "Birr 1,400,000", "Yesterday", Color(0xFF8B5CF6)),
    FundAccount("Charity (Mahber)", "Birr 450,000", "3 days ago", Color(0xFF10B981)),
    FundAccount("Clergy Retirement Fund", "Birr 250,000", "1 week ago", Color(0xFFF59E0B))
)

val bankAccounts = listOf(
    BankAccount("Commercial Bank of Ethiopia", "100023456789", "Addis Ababa Main Branch", "Birr 2.8M"),
    BankAccount("Awash International Bank", "992834571", "Bole Branch", "Birr 600K")
)
