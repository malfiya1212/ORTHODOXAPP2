package com.example.orthodoxapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController
import com.example.orthodoxapp.data.local.AppDatabase
import com.example.orthodoxapp.data.network.NetworkClient
import com.example.orthodoxapp.data.model.UserRole
import com.example.orthodoxapp.data.repository.FinanceRepository
import com.example.orthodoxapp.ui.NavGraph
import com.example.orthodoxapp.ui.FinancialViewModel
import com.example.orthodoxapp.ui.Screen
import com.example.orthodoxapp.ui.theme.ORTHODOXAPPTheme

class MainActivity : ComponentActivity() {
    companion object {
        var navController: NavHostController? = null
    }

    private fun handleNotification(intent: Intent?) {
        val type = intent?.getStringExtra("notification_type")
        // Simple routing based on notification type
        when (type) {
            "payment_success" -> {
                navController?.navigate(com.example.orthodoxapp.ui.Screen.Notifications.route)
            }
            "weekly_summary" -> {
                navController?.navigate(com.example.orthodoxapp.ui.Screen.Notifications.route)
            }
            else -> {
                // Default action: do nothing or navigate to main dashboard
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Security & Session
        try {
            com.example.orthodoxapp.security.SecurityManager.initialize(this)
            
            val database = AppDatabase.getDatabase(applicationContext)
            com.example.orthodoxapp.util.ErrorHandler.initialize(database.financeDao())
            
            val repository = FinanceRepository(database, database.financeDao(), database.syncDao(), NetworkClient.apiService)
            val viewModel = FinancialViewModel(repository)
            
            enableEdgeToEdge()
            setContent {
                val isDarkMode by viewModel.isDarkMode.collectAsState()
                val currentLanguage by viewModel.currentLanguage.collectAsState()
                val strings = if (currentLanguage == com.example.orthodoxapp.util.Language.AMHARIC) 
                    com.example.orthodoxapp.util.AmharicStrings 
                else 
                    com.example.orthodoxapp.util.EnglishStrings

                ORTHODOXAPPTheme(darkTheme = isDarkMode) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        com.example.orthodoxapp.util.LocalAppStrings provides strings
                    ) {
                    val navController = rememberNavController()
                    androidx.compose.runtime.LaunchedEffect(navController) {
                        MainActivity.navController = navController
                    }
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    val role by viewModel.currentRole.collectAsState()
                    
                    val items = remember(role, strings) {
                        val baseItems = mutableListOf<Triple<Screen, String, androidx.compose.ui.graphics.vector.ImageVector>>(
                            Triple(
                                when (role) {
                                    UserRole.SYNOD_ADMIN -> Screen.SynodDashboard
                                    UserRole.DIOCESE_ADMIN -> Screen.DioceseDashboard
                                    UserRole.CHURCH_ADMIN -> Screen.ChurchDashboard
                                    UserRole.ACCOUNTANT -> Screen.AccountantDashboard
                                    UserRole.AUDITOR -> Screen.AuditorDashboard
                                    else -> Screen.Dashboard
                                }, 
                                strings.dashboard, 
                                Icons.Default.Dashboard
                            )
                        )
                        when (role) {
                            UserRole.SYNOD_ADMIN -> {
                                baseItems.add(Triple(Screen.OrgManagement, "Dioceses", Icons.Default.LocationCity))
                                baseItems.add(Triple(Screen.Approvals, "Approvals", Icons.AutoMirrored.Filled.FactCheck))
                                baseItems.add(Triple(Screen.Reports, "Reports", Icons.Default.Assessment))
                                baseItems.add(Triple(Screen.Profile, "Profile", Icons.Default.Person))
                            }
                            UserRole.DIOCESE_ADMIN -> {
                                baseItems.add(Triple(Screen.OrgManagement, "Churches", Icons.Default.Church))
                                baseItems.add(Triple(Screen.Approvals, "Approvals", Icons.AutoMirrored.Filled.FactCheck))
                                baseItems.add(Triple(Screen.Reports, "Reports", Icons.Default.Assessment))
                                baseItems.add(Triple(Screen.Profile, "Profile", Icons.Default.Person))
                            }
                            UserRole.CHURCH_ADMIN -> {
                                baseItems.add(Triple(Screen.Income, strings.income, Icons.Default.AccountBalanceWallet))
                                baseItems.add(Triple(Screen.Expense, strings.expenses, Icons.Default.Payments))
                                baseItems.add(Triple(Screen.Chure, strings.chure, Icons.Default.Groups))
                                baseItems.add(Triple(Screen.NationalMap, strings.map, Icons.Default.Map))
                            }
                            UserRole.MEMBER -> {
                                baseItems.add(Triple(Screen.Chure, strings.chure, Icons.Default.Groups))
                                baseItems.add(Triple(Screen.NationalMap, strings.map, Icons.Default.Map))
                                baseItems.add(Triple(Screen.Profile, "Profile", Icons.Default.Person))
                            }
                            else -> {
                                baseItems.add(Triple(Screen.Income, strings.income, Icons.Default.AccountBalanceWallet))
                                baseItems.add(Triple(Screen.Expense, strings.expenses, Icons.Default.Payments))
                                baseItems.add(Triple(Screen.Chure, strings.chure, Icons.Default.Groups))
                                baseItems.add(Triple(Screen.Profile, "Profile", Icons.Default.Person))
                            }
                        }
                        baseItems
                    }
                    
                    // Hide bottom bar on auth and utility screens
                    val showBottomBar = currentDestination?.route != Screen.Login.route && 
                                       currentDestination?.route != Screen.Splash.route &&
                                       currentDestination?.route != Screen.Register.route &&
                                       currentDestination?.route != Screen.ForgotPassword.route
                    
                    Scaffold(
                        bottomBar = {
                            if (showBottomBar) {
                                NavigationBar {
                                    items.forEach { item ->
                                        val screen = item.first
                                        val label = item.second
                                        val icon = item.third
                                        NavigationBarItem(
                                            icon = { Icon(icon, contentDescription = label) },
                                            label = { Text(label, maxLines = 1) },
                                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                            onClick = {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavGraph(
                            navController = navController,
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                    }
                }
            }
        } catch (e: Throwable) {
            android.util.Log.e("MainActivity", "CRITICAL STARTUP ERROR", e)
            enableEdgeToEdge()
            setContent {
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF991B1B)) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(24.dp))
                            Text("Critical Startup Error", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            Spacer(Modifier.height(16.dp))
                            Text(e.localizedMessage ?: "Unknown Error", color = Color.White.copy(alpha = 0.8f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { finish() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF991B1B))
                            ) {
                                Text("Close App")
                            }
                        }
                    }
                }
            }
        }
    }
}