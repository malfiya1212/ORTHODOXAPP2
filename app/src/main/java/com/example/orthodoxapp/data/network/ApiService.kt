package com.example.orthodoxapp.data.network

import com.example.orthodoxapp.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Enterprise API Interface - Connection to ASP.NET Core Backend
 */
interface ApiService {

    // --- 🔐 AUTH MODULE ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body user: User): Response<User>

    // --- 💰 FINANCIAL MODULE ---
    @GET("financial/income")
    suspend fun getIncome(): Response<List<Income>>

    @POST("financial/income")
    suspend fun recordIncome(@Body income: Income): Response<Income>

    @POST("financial/income/{id}/approve")
    suspend fun approveIncome(@Path("id") id: Long): Response<Unit>

    @GET("financial/expenses")
    suspend fun getExpenses(): Response<List<Expense>>

    @POST("financial/expenses")
    suspend fun recordExpense(@Body expense: Expense): Response<Expense>

    // --- 🏢 ORGANIZATION MODULE ---
    @GET("organization/churches")
    suspend fun getChurches(): Response<List<Church>>

    @GET("organization/dioceses")
    suspend fun getDioceses(): Response<List<Diocese>>


    // --- 🛡️ AUDIT & REPORTING ---
    @GET("reports/church/{id}")
    suspend fun getChurchReport(@Path("id") id: Long): Response<FinancialSummary>

    // --- 💳 ENTERPRISE PAYMENT GATEWAY (Chapa, PayPal, Telebirr) ---
    @POST("payments/initialize")
    suspend fun initializePayment(@Body request: PaymentRequest): Response<PaymentInitResponse>

    @GET("payments/verify/{transactionRef}")
    suspend fun verifyPayment(@Path("transactionRef") txRef: String): Response<PaymentVerificationResponse>
}

// Data Classes for Network Transfer
data class LoginRequest(val email: String, val passwordHash: String)
data class AuthResponse(val token: String, val user: User)
data class FinancialSummary(val totalIncome: Double, val totalExpense: Double, val balance: Double)

// Payment Data Classes
data class PaymentRequest(
    val amount: Double,
    val currency: String = "ETB",
    val email: String,
    val firstName: String,
    val lastName: String,
    val purpose: String, // "TITHE", "DONATION", "CHURE"
    val provider: String // "CHAPA", "PAYPAL", "TELEBIRR"
)

data class PaymentInitResponse(
    val checkoutUrl: String,
    val transactionReference: String
)

data class PaymentVerificationResponse(
    val status: String, // "SUCCESS", "PENDING", "FAILED"
    val amountPaid: Double,
    val transactionReference: String,
    val message: String
)
