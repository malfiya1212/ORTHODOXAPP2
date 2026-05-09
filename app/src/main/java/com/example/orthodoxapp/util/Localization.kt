package com.example.orthodoxapp.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.orthodoxapp.ui.FinancialViewModel

enum class Language(val code: String, val label: String) {
    ENGLISH("en", "English"),
    AMHARIC("am", "አማርኛ")
}

data class AppStrings(
    val loginTitle: String,
    val loginSubtitle: String,
    val emailLabel: String,
    val passwordLabel: String,
    val loginButton: String,
    val registerButton: String,
    val forgotPassword: String,
    val registerPrompt: String,
    val dashboard: String,
    val income: String,
    val expenses: String,
    val chure: String,
    val map: String,
    val notifications: String,
    val settings: String,
    val ethiopianMonths: List<String>
)

val EnglishStrings = AppStrings(
    loginTitle = "TEWAHEDO FINANCE MANAGER",
    loginSubtitle = "Manage • Transparency • Faith",
    emailLabel = "Email or Phone Number",
    passwordLabel = "Password",
    loginButton = "LOGIN",
    registerButton = "REGISTER",
    forgotPassword = "Forgot Password?",
    registerPrompt = "Don't have an account? Register",
    dashboard = "Dashboard",
    income = "Income",
    expenses = "Expenses",
    chure = "Chure",
    map = "Map",
    notifications = "Notifications",
    settings = "Settings",
    ethiopianMonths = listOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas", "Tir", "Yakatit", 
        "Magabit", "Miyazya", "Ginbot", "Sane", "Hamle", "Nehasse", "Pagume"
    )
)

val AmharicStrings = AppStrings(
    loginTitle = "ተዋህዶ የፋይናንስ አስተዳዳሪ",
    loginSubtitle = "አስተዳደር • ግልጽነት • እምነት",
    emailLabel = "ኢሜይል ወይም ስልክ ቁጥር",
    passwordLabel = "የይለፍ ቃል",
    loginButton = "ግባ",
    registerButton = "ተመዝገብ",
    forgotPassword = "የይለፍ ቃል ረስተዋል?",
    registerPrompt = "አካውንት የለዎትም? ይመዝገቡ",
    dashboard = "ዳሽቦርድ",
    income = "ገቢ",
    expenses = "ወጪ",
    chure = "ጩሬ",
    map = "ካርታ",
    notifications = "ማሳወቂያዎች",
    settings = "ቅንብሮች",
    ethiopianMonths = listOf(
        "መስከረም", "ጥቅምት", "ህዳር", "ታኅሣሥ", "ጥር", "የካቲት", 
        "መጋቢት", "ሚያዝያ", "ግንቦት", "ሰኔ", "ሐምሌ", "ነሐሴ", "ጳጉሜ"
    )
)

val LocalAppStrings = staticCompositionLocalOf { EnglishStrings }
