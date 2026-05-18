package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.example.orthodoxapp.data.model.Certificate
import com.example.orthodoxapp.data.model.Church
import com.example.orthodoxapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateScreen(viewModel: FinancialViewModel, onBack: () -> Unit) {
    val certificates by viewModel.certificates.collectAsState()
    var selectedCert by remember { mutableStateOf<Certificate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Ecclesiastical Awards", style = MaterialTheme.typography.labelSmall, color = OrthodoxGold)
                        Text("My Certificates", fontWeight = FontWeight.Bold, color = PureLinen)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PureLinen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OrthodoxBlue)
            )
        }
    ) { padding ->
        if (selectedCert != null) {
            CertificateDetailDialog(selectedCert!!) { selectedCert = null }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Digital Recognition",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Text(
                    "Official certificates issued by your parish for spiritual service and participation.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
            }

            if (certificates.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            Spacer(Modifier.height(12.dp))
                            Text("No certificates issued yet.", color = TextSecondary, textAlign = TextAlign.Center)
                        }
                    }
                }
            } else {
                items(certificates) { cert ->
                    CertificateItem(cert) { selectedCert = cert }
                }
            }
        }
    }
}

@Composable
fun CertificateItem(cert: Certificate, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = OrthodoxGold.copy(alpha = 0.1f)
            ) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = OrthodoxGoldDark, modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cert.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                Text(cert.awardType, fontSize = 12.sp, color = OrthodoxBlue, fontWeight = FontWeight.Bold)
                Text(
                    java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(cert.issuedDate)),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.LightGray, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateDetailDialog(cert: Certificate, onDismiss: () -> Unit) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.95f)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = PureLinen,
            border = androidx.compose.foundation.BorderStroke(4.dp, Brush.linearGradient(listOf(OrthodoxGold, OrthodoxGoldDark)))
        ) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = OrthodoxBlue, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "CERTIFICATE OF RECOGNITION",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = OrthodoxBlue,
                    letterSpacing = 2.sp
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text("This is to certify that", fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "MEMBER OF PARISH", // Ideally we'd pass the name here too
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    "Has successfully completed / participated in:",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    cert.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = OrthodoxGoldDark,
                    textAlign = TextAlign.Center
                )
                
                if (!cert.description.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        cert.description,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(java.text.SimpleDateFormat("MMM dd, yyyy").format(java.util.Date(cert.issuedDate)), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        HorizontalDivider(Modifier.width(80.dp), color = Color.Black)
                        Text("Date", fontSize = 10.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(cert.certificateId, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.Gray)
                        HorizontalDivider(Modifier.width(80.dp), color = Color.Black)
                        Text("Serial No", fontSize = 10.sp)
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OrthodoxBlue)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
