package com.example.orthodoxapp.util

import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object EmailUtility {
    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = "587"
    private const val SENDER_EMAIL = "bwwmas@gmail.com"
    private const val SENDER_PASSWORD = "ooup jbrg cich dylk" // App Password provided by user

    suspend fun sendOtpEmail(recipientEmail: String, otp: String): Boolean = withContext(Dispatchers.IO) {
        val properties = Properties().apply {
            put("mail.smtp.host", SMTP_HOST)
            put("mail.smtp.port", SMTP_PORT)
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.starttls.required", "true")
            put("mail.smtp.ssl.protocols", "TLSv1.2")
        }

        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SENDER_EMAIL))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                subject = "Your Verification OTP - Tewahedo Connect"
                setText("""
                    Peace be with you,
                    
                    Your verification code for Tewahedo Connect registration is: $otp
                    
                    Please use this code to complete your registration. This code will expire soon.
                    
                    Blessings,
                    Tewahedo Connect Team
                """.trimIndent())
            }

            Transport.send(message)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
