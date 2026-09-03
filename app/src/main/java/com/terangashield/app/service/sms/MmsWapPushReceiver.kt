package com.terangashield.app.service.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Requis par Android pour être sélectionnable comme app SMS par défaut (ROLE_SMS).
 * L'analyse anti-arnaque V1 porte sur les SMS ; le traitement complet du contenu MMS
 * (téléchargement, décodage multipart) n'est pas dans le périmètre de ce scaffold.
 */
class MmsWapPushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Accusé de réception minimal — pas de traitement du contenu MMS en V1.
    }
}
