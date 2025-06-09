package com.example.jaa.fcm
import android.content.pm.PackageManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Received FCM message: data=${remoteMessage.data}, notification=${remoteMessage.notification}")

        val vehicleId = remoteMessage.data["vehicle_id"]?.toIntOrNull()
        val customerId = remoteMessage.data["customer_id"]?.toIntOrNull()

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
                .setContentTitle("Schedule Your Pickup")
                .setContentText("Tap to schedule pickup for your vehicle.")
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