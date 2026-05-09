package com.example.orthodoxapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(onBack: () -> Unit) {
    var selectedLanguage by remember { mutableStateOf("English") }
    
    val languages = listOf(
        LanguageItem("Amharic", "አማርኛ", "ET"),
        LanguageItem("English", "English", "US"),
        LanguageItem("Afaan Oromoo", "Oromoo", "ET"),
        LanguageItem("Tigrinya", "ትግርኛ", "ET"),
        LanguageItem("Somali", "Soomaali", "SO"),
        LanguageItem("Afar", "Qafaraf", "ET"),
        LanguageItem("Sidamo", "Sidaamu Afoo", "ET"),
        LanguageItem("Wolaytta", "Wolayttattuwa", "ET"),
        LanguageItem("Gurage", "ጉራጌ", "ET"),
        LanguageItem("Geez", "ግዕዝ (Liturgical)", "ET")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Language Settings", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF3F3D89))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            item {
                Text(
                    "Choose System Language",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            items(languages) { language ->
                LanguageRow(
                    language = language,
                    isSelected = selectedLanguage == language.name,
                    onClick = { selectedLanguage = language.name }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray)
            }
        }
    }
}

data class LanguageItem(val name: String, val nativeName: String, val countryCode: String)

@Composable
fun LanguageRow(language: LanguageItem, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) Color(0xFF3F3D89).copy(alpha = 0.1f) else Color.White
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = if (isSelected) Color(0xFF3F3D89) else Color.Gray)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(language.nativeName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isSelected) Color(0xFF3F3D89) else Color.Black)
                Text(language.name, fontSize = 12.sp, color = Color.Gray)
            }
        }
        
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF3F3D89))
        }
    }
}
