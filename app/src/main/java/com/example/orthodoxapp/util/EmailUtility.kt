package com.example.orthodoxapp.util

import java.util.*
import javax.activation.CommandMap
import javax.activation.MailcapCommandMap
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

object EmailUtility {
    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = "587"
    private const val SENDER_EMAIL = "bwwmas@gmail.com"
    private const val SENDER_PASSWORD = "ooupjbrgcichdylk" // App Password provided by user

    suspend fun sendReferenceEmail(
        recipientEmail: String,
        referenceNumber: String,
        memberName: String,
        amount: Double,
        paymentMethod: String
    ): Boolean = withContext(Dispatchers.IO) {
        val currentThread = Thread.currentThread()
        val originalClassLoader = currentThread.contextClassLoader
        try {
            currentThread.contextClassLoader = EmailUtility::class.java.classLoader
            val mc = MailcapCommandMap()
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
            CommandMap.setDefaultCommandMap(mc)
            val properties = Properties().apply {
                put("mail.smtp.host", SMTP_HOST)
                put("mail.smtp.port", SMTP_PORT)
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.starttls.required", "true")
                put("mail.smtp.ssl.protocols", "TLSv1.2")
                put("mail.smtp.ssl.trust", SMTP_HOST)
                put("mail.smtp.timeout", "10000")
                put("mail.smtp.connectiontimeout", "10000")
            }
            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                }
            })
            session.debug = true
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SENDER_EMAIL))
                addRecipient(Message.RecipientType.TO, InternetAddress(recipientEmail))
                subject = "Payment Reference: $referenceNumber"
                setText(
                    "Dear Admin,\n\n" +
                    "A new payment has been initiated.\n" +
                    "Reference Number: $referenceNumber\n" +
                    "Member Name: $memberName\n" +
                    "Amount: $amount\n" +
                    "Payment Method: $paymentMethod\n\n" +
                    "Please verify receipt and approve the payment.\n\n" +
                    "Thank you."
                )
            }
            Transport.send(message)
            Log.d("EmailUtility", "Reference email sent to $recipientEmail")
            true
        } catch (e: Exception) {
            Log.e("EmailUtility", "Failed to send reference email: ${e.message}", e)
            false
        } finally {
            currentThread.contextClassLoader = originalClassLoader
        }
    }
    suspend fun sendOtpEmail(recipientEmail: String, otp: String): Boolean = withContext(Dispatchers.IO) {
        val currentThread = Thread.currentThread()
        val originalClassLoader = currentThread.contextClassLoader

        try {
            // Fix for JavaMail on Android Coroutine threads
            currentThread.contextClassLoader = EmailUtility::class.java.classLoader

            // Setup MailcapCommandMap for Android
            val mc = MailcapCommandMap()
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html")
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml")
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain")
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed")
            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822")
            CommandMap.setDefaultCommandMap(mc)

            val properties = Properties().apply {
                put("mail.smtp.host", SMTP_HOST)
                put("mail.smtp.port", SMTP_PORT)
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.starttls.required", "true")
                put("mail.smtp.ssl.protocols", "TLSv1.2")
                put("mail.smtp.ssl.trust", SMTP_HOST)
                // Add timeouts to prevent hanging
                put("mail.smtp.timeout", "10000")
                put("mail.smtp.connectiontimeout", "10000")
            }

            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                }
            })

            // Enable debug logging for JavaMail
            session.debug = true

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
            
            Log.d("EmailUtility", "OTP email sent successfully to $recipientEmail")
            true
        } catch (e: Exception) {
            Log.e("EmailUtility", "Failed to send OTP email: ${e.message}", e)
            e.printStackTrace()
            false
        } finally {
            // Restore classloader
            currentThread.contextClassLoader = originalClassLoader
        }
    }
}
