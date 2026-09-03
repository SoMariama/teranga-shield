package com.terangashield.app.service.sms

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.telephony.SmsManager

/**
 * Réponse rapide depuis l'écran d'appel entrant — requis par Android pour être sélectionnable
 * comme app SMS par défaut (ROLE_SMS).
 */
class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val destination = intent?.data?.schemeSpecificPart
        val message = intent?.getStringExtra(Intent.EXTRA_TEXT)
        if (!destination.isNullOrBlank() && !message.isNullOrBlank()) {
            runCatching {
                @Suppress("DEPRECATION")
                SmsManager.getDefault().sendTextMessage(destination, null, message, null, null)
            }
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
