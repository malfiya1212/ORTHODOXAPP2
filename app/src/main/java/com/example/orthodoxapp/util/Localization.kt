package com.example.orthodoxapp.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.orthodoxapp.ui.FinancialViewModel

enum class Language(val code: String, val label: String) {
    ENGLISH("en", "English"),
    AMHARIC("am", "አማርኛ"),
    OROMOO("om", "Afaan Oromoo"),
    TIGRINYA("ti", "ትግርኛ"),
    GEEZ("gez", "ግዕዝ"),
    SOMALI("so", "Soomaali"),
    AFAR("aa", "Qafaraf"),
    SIDAMO("sid", "Sidaamu Afoo"),
    WOLAYTTA("wal", "Wolayttattuwa"),
    GURAGE("sgw", "ጉራጌ"),
    HADIYISA("hdy", "Hadiyisa"),
    GAMO("gmv", "Gamo-Kello"),
    KAFA("kbr", "Kafficho"),
    AGEW("awn", "Agewigna"),
    BERTA("wti", "Berta"),
    ANYUAK("anu", "Anyuak")
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

val OromooStrings = AppStrings(
    loginTitle = "BULCHITAA FAAYINAANSII TEWAHEDO",
    loginSubtitle = "Bulchiinsa • Iftoomina • Amantaa",
    emailLabel = "Imeelii ykn Lakkoofsa Bilbilaa",
    passwordLabel = "Jecha Iccitii",
    loginButton = "SEENI",
    registerButton = "GALMOOFI",
    forgotPassword = "Jecha Iccitii dagattee?",
    registerPrompt = "Herrega hin qabduu? Galmoofi",
    dashboard = "Daashboordii",
    income = "Galii",
    expenses = "Baasii",
    chure = "Chure",
    map = "Kaartaa",
    notifications = "Beeksisa",
    settings = "Sajoo",
    ethiopianMonths = listOf(
        "Fulbaana", "Onkoloolessa", "Sadaasa", "Muddee", "Amajji", "Guraandhala", 
        "Bitooteessa", "Ebla", "Caamsa", "Waxabajjii", "Adoolessa", "Hagayya", "Qaammee"
    )
)

val TigrinyaStrings = AppStrings(
    loginTitle = "ተዋህዶ ፋይናንስ ኣካያዲ",
    loginSubtitle = "ምሕደራ • ግሉጽነት • እምነት",
    emailLabel = "ኢመይል ወይ ቁጽሪ ስልኪ",
    passwordLabel = "ፓስዎርድ",
    loginButton = "እቶ",
    registerButton = "ተመዝገብ",
    forgotPassword = "ፓስዎርድ ረሲዕኩም?",
    registerPrompt = "ኣካውንት የብልኩምን? ተመዝገቡ",
    dashboard = "ዳሽቦርድ",
    income = "ኣታዊ",
    expenses = "ወጻኢ",
    chure = "ጩሬ",
    map = "ካርታ",
    notifications = "መፍለጢታት",
    settings = "ስግንጥራት",
    ethiopianMonths = listOf(
        "መስከረም", "ጥቅምቲ", "ሕዳር", "ታሕሳስ", "ጥሪ", "ለካቲት", 
        "መጋቢት", "ሚያዝያ", "ግንቦት", "ሰነ", "ሓምለ", "ነሓሰ", "ጳጉሜን"
    )
)

val GeezStrings = AppStrings(
    loginTitle = "መጋቤ ንዋይ ተዋሕዶ",
    loginSubtitle = "መጋብነት • ብሩህነት • ሃይማኖት",
    emailLabel = "መልእኽቲ ወይ ስልክ",
    passwordLabel = "ቃል ምስጢር",
    loginButton = "ባእ",
    registerButton = "ተመዝገብ",
    forgotPassword = "ቃል ምስጢር ረሳዕከ?",
    registerPrompt = "ሕሳብ አልቦ? ተመዝገብ",
    dashboard = "ዐውደ ርእይ",
    income = "አትዎ",
    expenses = "ወፃኢ",
    chure = "ጩሬ",
    map = "ስዕለ ምድር",
    notifications = "ዜና",
    settings = "ሥርዓት",
    ethiopianMonths = listOf(
        "መስከረም", "ጥቅምት", "ኅዳር", "ታኅሣሥ", "ጥር", "የካቲት", 
        "መጋቢት", "ሚያዝያ", "ግንቦት", "ሰኔ", "ሐምሌ", "ነሐሴ", "ጳጕሜን"
    )
)

val SomaliStrings = EnglishStrings.copy(loginTitle = "MAAMULAHA MAALIYADDA TEWAHEDO", dashboard = "Dashboard (Soomaali)")
val AfarStrings = EnglishStrings.copy(loginTitle = "TEWAHEDO MAALUUMAT QAFARAF", dashboard = "Dashboard (Qafaraf)")
val SidamoStrings = EnglishStrings.copy(loginTitle = "TEWAHEDO FINANS MENEJERI", dashboard = "Dashboard (Sidaamu)")
val WolayttaStrings = EnglishStrings.copy(loginTitle = "TEWAHEDO MIISHSHAA KAALETTIYAA", dashboard = "Dashboard (Wolayttattuwa)")
val GurageStrings = EnglishStrings.copy(loginTitle = "ተዋህዶ ፋይናንስ (ጉራጌ)", dashboard = "ዳሽቦርድ (ጉራጌ)")
val HadiyisaStrings = EnglishStrings.copy(loginTitle = "TEWAHEDO FINANSE MANEJA", dashboard = "Dashboard (Hadiyisa)")
val GamoStrings = EnglishStrings.copy(loginTitle = "TEWAHEDO MIISHSHA AYSSIYAAGA", dashboard = "Dashboard (Gamo)")
val KafaStrings = EnglishStrings.copy(loginTitle = "TEWAHEDO FINANS MENEJERO", dashboard = "Dashboard (Kafficho)")
val AgewStrings = EnglishStrings.copy(loginTitle = "ተዋህዶ ፋይናንስ (Agewigna)", dashboard = "ዳሽቦርድ (Agewigna)")
val BertaStrings = EnglishStrings.copy(loginTitle = "TEWAHEDO FINANCE (Berta)", dashboard = "Dashboard (Berta)")
val AnyuakStrings = EnglishStrings.copy(loginTitle = "TEWAHEDO FINANCE (Anyuak)", dashboard = "Dashboard (Anyuak)")

val LocalAppStrings = staticCompositionLocalOf { EnglishStrings }
