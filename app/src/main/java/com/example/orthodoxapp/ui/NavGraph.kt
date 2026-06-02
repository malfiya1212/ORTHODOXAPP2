package com.example.orthodoxapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import com.example.orthodoxapp.data.model.UserRole
import com.example.orthodoxapp.ui.components.ProtectedRoute

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object SynodDashboard : Screen("synod_dashboard")
    object DioceseDashboard : Screen("diocese_dashboard?id={id}") {
        fun createRoute(id: Long) = "diocese_dashboard?id=$id"
        val baseRoute = "diocese_dashboard"
    }
    object ChurchDashboard : Screen("church_dashboard")
    object Dashboard : Screen("dashboard") // Default/Member
    object Income : Screen("income")
    object AddIncome : Screen("add_income")
    object Expense : Screen("expense")
    object AddExpense : Screen("add_expense")
    object Transactions : Screen("transactions")
    object Approvals : Screen("approvals")

    object Reports : Screen("reports")
    object OrgManagement : Screen("org_management")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications?filter={filter}") {
        fun createRoute(filter: String = "") = if (filter.isNotEmpty()) "notifications?filter=$filter" else "notifications"
    }
    object Chure : Screen("chure")
    object Hierarchy : Screen("hierarchy")
    object NationalMap : Screen("national_map")
    object GlobalPayment : Screen("global_payment")
    object Users : Screen("users")
    object Roles : Screen("roles")
    object Documents : Screen("documents")
    object Personnel : Screen("personnel")
    object Assets : Screen("assets")
    object AuditLogs : Screen("audit_logs")
    object Projects : Screen("projects")
    object Treasury : Screen("treasury")
    object Members : Screen("members")
    object Certificates : Screen("certificates")
    object Language : Screen("language")
    object Receipt : Screen("receipt/{type}/{id}") {
        fun createRoute(type: String, id: Long) = "receipt/$type/$id"
    }
    object ChurchDetail : Screen("church_detail/{id}") {
        fun createRoute(id: Long) = "church_detail/$id"
    }
    object AddOrganization : Screen("add_organization/{type}/{parentId}") {
        fun createRoute(type: String, parentId: Long) = "add_organization/$type/$parentId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController, 
    viewModel: FinancialViewModel, 
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController, 
        startDestination = Screen.Splash.route, 
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onGetStarted = {
                navController.navigate(Screen.Login.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
            })
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { role ->
                    navController.navigate(getDashboardRouteForRoleString(role)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(Screen.Register.route) {
            RegistrationScreen(
                viewModel = viewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onResetSuccess = { 
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SynodDashboard.route) {
            ProtectedRoute(navController, viewModel, listOf(UserRole.SYNOD_ADMIN)) {
                AdminDashboardScreen(viewModel, onNavigate = { navController.navigate(it) })
            }
        }

        composable(
            "diocese_dashboard?id={id}",
            arguments = listOf(navArgument("id") { defaultValue = "" })
        ) { backStackEntry ->
            val dioceseId = backStackEntry.arguments?.getString("id")?.toLongOrNull()
            ProtectedRoute(navController, viewModel, listOf(UserRole.SYNOD_ADMIN, UserRole.DIOCESE_ADMIN)) {
                DioceseDashboardScreen(viewModel, overrideDioceseId = dioceseId, onNavigate = { navController.navigate(it) })
            }
        }
        composable(Screen.Reports.route) {
            ProtectedRoute(navController, viewModel, listOf(UserRole.SYNOD_ADMIN, UserRole.DIOCESE_ADMIN, UserRole.CHURCH_ADMIN)) {
                ReportsScreen(viewModel, onBack = { navController.popBackStack() })
            }
        }
        composable(Screen.Personnel.route) {
            ProtectedRoute(navController, viewModel, listOf(UserRole.SYNOD_ADMIN, UserRole.DIOCESE_ADMIN, UserRole.CHURCH_ADMIN)) {
                PersonnelScreen(viewModel, onAddPersonnel = { /* TODO */ }, onBack = { navController.popBackStack() })
            }
        }
        composable(Screen.Assets.route) {
            ProtectedRoute(navController, viewModel, listOf(UserRole.SYNOD_ADMIN, UserRole.DIOCESE_ADMIN, UserRole.CHURCH_ADMIN)) {
                AssetsScreen(viewModel, onAddAsset = { /* TODO */ }, onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.AuditLogs.route) {
            ProtectedRoute(navController, viewModel, listOf(UserRole.SYNOD_ADMIN, UserRole.DIOCESE_ADMIN)) {
                AuditLogScreen(viewModel, onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.Users.route) {
            ProtectedRoute(navController, viewModel, listOf(UserRole.SYNOD_ADMIN, UserRole.DIOCESE_ADMIN, UserRole.CHURCH_ADMIN)) {
                UsersScreen(viewModel, onBack = { navController.popBackStack() })
            }
        }

        composable(Screen.ChurchDashboard.route) {
            ProtectedRoute(navController, viewModel, listOf(UserRole.CHURCH_ADMIN)) {
                ChurchDashboardScreen(viewModel, onNavigate = { navController.navigate(it) }, onAddIncome = { navController.navigate(Screen.AddIncome.route) }, onAddExpense = { navController.navigate(Screen.AddExpense.route) })
            }
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(viewModel, onNavigate = { navController.navigate(it) })
        }

        composable(Screen.Income.route) { IncomeScreen(viewModel, onAddIncome = { navController.navigate(Screen.AddIncome.route) }, onBack = { navController.popBackStack() }) }
        composable(Screen.AddIncome.route) { RecordIncomeScreen(viewModel, onBack = { navController.popBackStack() }) }
        composable(Screen.Expense.route) { ExpenseScreen(viewModel, onAddExpense = { navController.navigate(Screen.AddExpense.route) }, onBack = { navController.popBackStack() }) }
        composable(Screen.AddExpense.route) { RecordExpenseScreen(viewModel, onBack = { navController.popBackStack() }) }
        composable(Screen.Transactions.route) { TransactionsListScreen(viewModel, onBack = { navController.popBackStack() }) }
        composable(Screen.Approvals.route) { ApprovalsScreen(viewModel, onBack = { navController.popBackStack() }, onNavigate = { navController.navigate(it) }) }
        composable(Screen.Settings.route) { SettingsScreen(viewModel, onBack = { navController.popBackStack() }, onNavigate = { navController.navigate(it) }) }
        composable(Screen.Language.route) { LanguageScreen(viewModel, onBack = { navController.popBackStack() }) }
        composable(Screen.Profile.route) { ProfileScreen(viewModel, onBack = { navController.popBackStack() }) }
        composable(Screen.Hierarchy.route) { 
            HierarchyTreeViewScreen(viewModel, onBack = { navController.popBackStack() }, onNavigate = { navController.navigate(it) }) 
        }
        composable(Screen.OrgManagement.route) {
            OrganizationManagementScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddClick = { type -> navController.navigate(Screen.AddOrganization.createRoute(type ?: "Church", 0L)) },
                onEditClick = { type, id -> navController.navigate(Screen.AddOrganization.createRoute(type, id)) }
            )
        }
        composable(
            route = Screen.Notifications.route,
            arguments = listOf(navArgument("filter") { defaultValue = "" })
        ) { backStackEntry ->
            val filter = backStackEntry.arguments?.getString("filter") ?: ""
            NotificationsScreen(viewModel, filter = filter, onBack = { navController.popBackStack() })
        }
        composable(Screen.Chure.route) {
            ChureScreen(viewModel, onBack = { navController.popBackStack() }, onNavigate = { navController.navigate(it) })
        }
        composable(Screen.NationalMap.route) {
            NationalMapViewScreen(viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.AddOrganization.route) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "Church"
            val parentId = backStackEntry.arguments?.getString("parentId")?.toLongOrNull() ?: 0L
            AddOrganizationScreen(
                viewModel = viewModel,
                initialType = type,
                orgId = if (parentId > 0L) parentId else null,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.GlobalPayment.route) { GlobalPaymentScreen(viewModel, onBack = { navController.popBackStack() }) }
        composable(Screen.Receipt.route) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "INCOME"
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            TransactionReceiptScreen(type = type, id = id, viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.ChurchDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
            ChurchDetailScreen(viewModel = viewModel, churchId = id, onBack = { navController.popBackStack() })
        }
        composable(Screen.Projects.route) {
            ProtectedRoute(navController, viewModel, listOf(UserRole.CHURCH_ADMIN)) {
                ParishProjectsScreen(viewModel, onBack = { navController.popBackStack() })
            }
        }
        composable(Screen.Treasury.route) {
            ProtectedRoute(navController, viewModel, listOf(UserRole.CHURCH_ADMIN)) {
                ParishTreasuryScreen(viewModel, onBack = { navController.popBackStack() })
            }
        }
        composable(Screen.Members.route) {
            ProtectedRoute(navController, viewModel, listOf(UserRole.CHURCH_ADMIN)) {
                MemberManagementScreen(viewModel, onBack = { navController.popBackStack() })
            }
        }
        composable(Screen.Certificates.route) {
            CertificateScreen(viewModel, onBack = { navController.popBackStack() })
        }
    }
}

private fun getDashboardRouteForRoleString(role: String): String = when (role) {
    "synod_admin" -> Screen.SynodDashboard.route
    "diocese_admin" -> "diocese_dashboard" // This will match the template with default id
    "church_admin" -> Screen.ChurchDashboard.route
    else -> Screen.Dashboard.route
}
