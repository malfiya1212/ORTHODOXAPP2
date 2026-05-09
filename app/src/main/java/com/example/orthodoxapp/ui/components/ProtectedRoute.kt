package com.example.orthodoxapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.orthodoxapp.security.SecurityManager
import com.example.orthodoxapp.ui.FinancialViewModel
import com.example.orthodoxapp.ui.Screen
import com.example.orthodoxapp.data.model.UserRole

/**
 * A wrapper component that enforces route-level security.
 * It checks if the user is authenticated and optionally if they hold the required role.
 * If unauthorized, it intercepts the navigation and redirects to the Login screen.
 */
@Composable
fun ProtectedRoute(
    navController: NavController,
    viewModel: FinancialViewModel,
    requiredRoles: List<UserRole> = emptyList(),
    content: @Composable () -> Unit
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val isTokenValid = SecurityManager.isTokenValid()

    LaunchedEffect(isTokenValid, currentRole) {
        if (!isTokenValid) {
            // Not logged in, redirect to Login
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true } // Clear entire backstack
                launchSingleTop = true
            }
        } else if (requiredRoles.isNotEmpty() && currentRole !in requiredRoles && currentRole != UserRole.SYNOD_ADMIN) {
            // Logged in but missing required role (Super Admin bypasses this)
            // Redirect to a generic dashboard or show unauthorized
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Dashboard.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Only render the content if authenticated and authorized
    if (isTokenValid && (requiredRoles.isEmpty() || currentRole in requiredRoles || currentRole == UserRole.SYNOD_ADMIN)) {
        content()
    }
}
