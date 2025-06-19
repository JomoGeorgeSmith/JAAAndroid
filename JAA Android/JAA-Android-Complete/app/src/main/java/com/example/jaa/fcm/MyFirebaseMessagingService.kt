package com.example.jaa.fcm
import android.content.pm.PackageManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.jaa.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*
import android.content.Intent
import android.app.PendingIntent
import com.example.jaa.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New FCM Token: $token")
    }

    private fun parseDate(dateStr: String?): Date? {
        return try {
            if (dateStr == null) null
            else java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Received FCM message: data=${remoteMessage.data}, notification=${remoteMessage.notification}")

        val vehicleId = remoteMessage.data["vehicle_id"]?.toIntOrNull()
        val customerId = remoteMessage.data["customer_id"]?.toIntOrNull()
        val licensePlate = remoteMessage.data["license_plate"] ?: "Unknown Vehicle"
        val fitnessExpiry = parseDate(remoteMessage.data["fitness_expiration_date"])
        val insuranceExpiry = parseDate(remoteMessage.data["insurance_expiration_date"])
        val today = Calendar.getInstance().time
        val make = remoteMessage.data["make"] ?: ""
        val model = remoteMessage.data["model"] ?: ""
        val vehicleInfo = listOf(make, model).filter { it.isNotBlank() }.joinToString(" ")

        val expiredDocs = mutableListOf<String>()
        val expiringDocs = mutableListOf<String>()

        fitnessExpiry?.let {
            if (!it.after(today)) expiredDocs.add("fitness")
            else {
                val diff = (it.time - today.time) / (1000 * 60 * 60 * 24)
                if (diff <= 7) expiringDocs.add("fitness") else null
            }
        }
        insuranceExpiry?.let {
            if (!it.after(today)) expiredDocs.add("insurance")
            else {
                val diff = (it.time - today.time) / (1000 * 60 * 60 * 24)
                if (diff <= 7) expiringDocs.add("insurance") else null
            }
        }

        val docList = (expiredDocs + expiringDocs).joinToString(" and ")
        val status = when {
            expiredDocs.isNotEmpty() -> "are expired"
            expiringDocs.isNotEmpty() -> "are expiring soon"
            else -> "require your attention"
        }
        val contentTitle = "Your $vehicleInfo ($licensePlate) $docList $status."
        val contentText = "Please tap to view details."

        if (vehicleId != null) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "schedule")
                putExtra("vehicleId", vehicleId)
                putExtra("customerId", customerId)
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val channelId = "jaa_channel"

            val builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "JAA Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Channel for JAA document expiration alerts"
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Check permission before showing notification (Android 13+)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(Random().nextInt(), builder.build())
            } else {
                Log.w("FCM", "POST_NOTIFICATIONS permission not granted, notification not shown.")
            }
        }
    }
}