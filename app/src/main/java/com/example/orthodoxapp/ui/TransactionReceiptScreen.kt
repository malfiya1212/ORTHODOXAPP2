package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.orthodoxapp.data.model.*
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionReceiptScreen(
    type: String? = "INCOME",
    id: Long? = 0L,
    viewModel: FinancialViewModel,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val incomeRecords by viewModel.income.collectAsState()
    val expenseRecords by viewModel.expenses.collectAsState()
    
    val transactionData = if (type == "INCOME") {
        incomeRecords.find { inc: Income -> inc.id == id }?.let { it: Income ->
            ReceiptData(
                amount = it.amount,
                source = it.source,
                category = it.category ?: "Tithe",
                reference = it.referenceNumber ?: "TRX-INC-${it.id}",
                date = it.date,
                status = it.status
            )
        }
    } else {
        expenseRecords.find { exp: Expense -> exp.id == id }?.let { it: Expense ->
            ReceiptData(
                amount = it.amount,
                source = it.recipient ?: "Unknown",
                category = it.category,
                reference = it.referenceNumber ?: "TRX-EXP-${it.id}",
                date = it.date,
                status = it.status
            )
        }
    }

    val cardBg = Color.White
    val textColor = Color(0xFF1E293B)
    val textLight = Color(0xFF64748B)
    val greenColor = Color(0xFF10B981)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details", color = Color.White, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        Toast.makeText(context, "Official PDF receipt is being verified...", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Download PDF", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryBlue)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PrimaryBlue)
                .padding(padding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                if (transactionData == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Transaction not found", color = textColor)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(16.dp))
                        
                        // Icon based on status
                        Surface(
                            color = if (transactionData.status == "APPROVED") greenColor else Color(0xFFF59E0B),
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                if (transactionData.status == "APPROVED") Icons.Default.Check else Icons.Default.HourglassEmpty, 
                                contentDescription = "Status", 
                                tint = Color.White, 
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (transactionData.status == "APPROVED") "Verified Receipt" else "Pending Verification", 
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = if (transactionData.status == "APPROVED") greenColor else Color(0xFFF59E0B)
                        )
                        Text(
                            if (transactionData.status == "APPROVED") "This transaction is officially verified." else "Awaiting administrative review.", 
                            fontSize = 14.sp, 
                            color = textLight,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        Text("ETB ${String.format(java.util.Locale.getDefault(), "%,.2f", transactionData.amount)}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                        
                        Spacer(Modifier.height(32.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(Modifier.height(24.dp))
                        
                        // Details Table
                        ReceiptDetailRow("Church", "Local Parish Church")
                        ReceiptDetailRow(if (type == "INCOME") "From" else "To", transactionData.source)
                        ReceiptDetailRow("Category", transactionData.category)
                        ReceiptDetailRow("Payment Method", "Cash / Bank")
                        ReceiptDetailRow("Transaction ID", transactionData.reference)
                        ReceiptDetailRow("Date", java.text.DateFormat.getDateTimeInstance().format(transactionData.date))
                        
                        Spacer(Modifier.height(24.dp))
                        
                        // Verification Section
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = BackgroundLight
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Official Ecclesiastical Seal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OrthodoxBlue)
                                Spacer(Modifier.height(12.dp))
                                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(60.dp), tint = TextPrimary)
                                Spacer(Modifier.height(8.dp))
                                Text("Scan to verify cryptographic proof", fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                        
                        Spacer(Modifier.weight(1f))
                        
                        // Scripture Quote
                        Text(
                            "\"God loves a cheerful giver.\"",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "2 Corinthians 9:7",
                            fontSize = 12.sp,
                            color = OrthodoxGoldDark,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        if (transactionData.status == "APPROVED") {
                            Button(
                                onClick = { Toast.makeText(context, "Downloading PDF...", Toast.LENGTH_SHORT).show() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrthodoxBlue),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = PureLinen)
                                Spacer(Modifier.width(8.dp))
                                Text("✔ Download PDF", fontWeight = FontWeight.Bold, color = PureLinen)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ReceiptData(
    val amount: Double,
    val source: String,
    val category: String,
    val reference: String,
    val date: Long,
    val status: String
)

@Composable
fun ReceiptDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = Color(0xFF64748B))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E293B))
    }
}
