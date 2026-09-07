package com.terangashield.app.service.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Filet de sécurité pour la réception des SMS : SMS_RECEIVED n'est diffusé par le système QUE
 * lorsque l'app N'EST PAS (ou plus) le gestionnaire SMS par défaut — c'est l'exact complément de
 * [SmsDeliverReceiver], qui ne reçoit lui que SMS_DELIVER (réservé au gestionnaire par défaut).
 * Sans ce filet, un octroi de rôle qui échoue silencieusement ou est révoqué en arrière-plan fait
 * disparaître les nouveaux SMS de l'application sans qu'aucune erreur ne soit visible. On ne peut
 * pas écrire dans `content://sms` sans être l'app par défaut, donc on saute [writeToProvider] et
 * on se contente d'analyser + d'enregistrer dans Room pour que le message apparaisse quand même.
 */
class SmsReceivedFallbackReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val sender = messages[0].originatingAddress.orEmpty()
        val body = messages.joinToString(separator = "") { it.messageBody.orEmpty() }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SmsProcessor(context.applicationContext).process(sender, body)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
