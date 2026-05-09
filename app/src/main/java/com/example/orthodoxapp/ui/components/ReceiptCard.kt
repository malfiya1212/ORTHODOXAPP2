package com.example.orthodoxapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.orthodoxapp.data.model.PaymentReceipt

@Composable
fun ReceiptCard(receipt: PaymentReceipt) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(2.dp, Color(0xFFD4AF37), RoundedCornerShape(12.dp)), // Royal Gold border
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Church Name & Header
            Text(
                text = receipt.churchName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A) // Royal Blue
            )
            Text(
                text = "Official Receipt",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Amount
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%,.2f ETB".format(receipt.amount),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
                if (receipt.paymentMethod == "Diaspora" || receipt.amount != receipt.amount) {
                     // In production, we'd pass OriginalAmount in the Receipt DTO
                     Text(
                        text = "Original: ${receipt.amount} ${receipt.paymentMethod}", // Mocking original
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                     )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details
            ReceiptRow("Receipt No:", receipt.receiptNumber)
            ReceiptRow("Date:", receipt.paymentDate)
            ReceiptRow("Payment Type:", receipt.paymentType)
            ReceiptRow("Method:", receipt.paymentMethod)
            
            if (!receipt.referenceNumber.isNullOrEmpty()) {
                ReceiptRow("Ref No:", receipt.referenceNumber)
            }
            if (!receipt.memberName.isNullOrEmpty()) {
                ReceiptRow("Member:", receipt.memberName)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Footer
            Text(
                text = "Processed By: ${receipt.generatedBy}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // QR Code Verification Section
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // In production, ZXing library generates the actual QR bitmap from receipt.verificationUrl
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📷", style = MaterialTheme.typography.headlineMedium)
                    Text("SCAN TO VERIFY", fontSize = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp), fontWeight = FontWeight.Bold, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = receipt.verificationUrl,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF2563EB) // Clickable blue link styling
            )
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, fontWeight = FontWeight.Medium)
        Text(text = value, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
    }
}
