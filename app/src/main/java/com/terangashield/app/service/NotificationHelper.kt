package com.terangashield.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.terangashield.app.R
import com.terangashield.app.ui.MainActivity

object NotificationHelper {
    const val CHANNEL_ANALYSIS = "call_analysis_foreground"
    const val CHANNEL_ALERTS = "risk_alerts"

    private const val NOTIFICATION_ID_ANALYSIS = 1001
    private const val NOTIFICATION_ID_ALERT = 1002

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ANALYSIS,
                "Analyse anti-arnaque en cours",
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALERTS,
                "Alertes de risque",
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )
    }

    fun analysisForegroundNotification(context: Context): android.app.Notification {
        ensureChannels(context)
        return NotificationCompat.Builder(context, CHANNEL_ANALYSIS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.consent_mic_title))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showHighRiskAlert(context: Context, titleRes: Int, bodyRes: Int) {
        ensureChannels(context)
        val intent = MainActivity.newIntent(context)
        val pendingIntent = androidx.core.app.TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(intent)
            .getPendingIntent(0, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(titleRes))
            .setContentText(context.getString(bodyRes))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID_ALERT, notification)
    }
}
