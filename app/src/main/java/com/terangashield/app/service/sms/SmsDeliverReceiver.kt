package com.terangashield.app.service.sms

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Réception des SMS entrants — obligatoire pour être l'app SMS par défaut (ROLE_SMS).
 * Écrit le message dans le fournisseur système (à la charge de l'app par défaut) puis lance
 * l'analyse anti-arnaque.
 */
class SmsDeliverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_DELIVER_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val sender = messages[0].originatingAddress.orEmpty()
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
        val timestamp = messages[0].timestampMillis

        writeToProvider(context, sender, body, timestamp)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SmsProcessor(context.applicationContext).process(sender, body)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun writeToProvider(context: Context, sender: String, body: String, timestamp: Long) {
        runCatching {
            val values = ContentValues().apply {
                put(Telephony.Sms.ADDRESS, sender)
                put(Telephony.Sms.BODY, body)
                put(Telephony.Sms.DATE, timestamp)
                put(Telephony.Sms.READ, 0)
                put(Telephony.Sms.SEEN, 0)
                put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_INBOX)
            }
            context.contentResolver.insert(Telephony.Sms.Inbox.CONTENT_URI, values)
        }
    }
}
