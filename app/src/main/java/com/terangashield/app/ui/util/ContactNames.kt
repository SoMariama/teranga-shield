package com.terangashield.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.terangashield.app.service.call.ContactsLookup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Résout les noms d'affichage pour une liste de numéros (contacts connus uniquement) sans
 * bloquer le thread principal — utilisé pour afficher un nom plutôt qu'un numéro brut dans les
 * listes Accueil/Appels/Messages quand le numéro correspond à un contact enregistré.
 */
@Composable
fun rememberContactNames(phoneNumbers: List<String>): Map<String, String> {
    val context = LocalContext.current
    val distinctNumbers = phoneNumbers.distinct()
    val names by produceState(initialValue = emptyMap(), distinctNumbers) {
        value = withContext(Dispatchers.IO) {
            distinctNumbers.mapNotNull { number ->
                ContactsLookup.getContactDisplayName(context, number)?.let { number to it }
            }.toMap()
        }
    }
    return names
}
