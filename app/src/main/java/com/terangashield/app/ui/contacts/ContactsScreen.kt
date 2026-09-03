package com.terangashield.app.ui.contacts

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.TelecomManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.terangashield.app.R
import com.terangashield.app.ui.theme.IndigoNuit
import com.terangashield.app.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DeviceContact(val name: String, val phoneNumber: String)

/** Liste des contacts du téléphone, pour appeler directement — le clavier reste pour les autres numéros. */
@Composable
fun ContactsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val contacts by produceState(initialValue = emptyList<DeviceContact>()) {
        value = loadContacts(context)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                    Text(stringResource(R.string.contacts_title), style = MaterialTheme.typography.titleLarge)
                }
            }
            items(contacts, key = { it.phoneNumber }) { contact ->
                ContactRow(contact = contact, onClick = { placeCall(context, contact.phoneNumber) })
            }
        }
    }
}

@Composable
private fun ContactRow(contact: DeviceContact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(38.dp).background(IndigoNuit, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                (contact.name.firstOrNull() ?: '#').uppercase().toString(),
                color = White,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(contact.name, style = MaterialTheme.typography.titleMedium)
            Text(contact.phoneNumber, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun placeCall(context: Context, number: String) {
    val telecomManager = context.getSystemService(TelecomManager::class.java) ?: return
    runCatching { telecomManager.placeCall(Uri.fromParts("tel", number, null), null) }
}

private suspend fun loadContacts(context: Context): List<DeviceContact> = withContext(Dispatchers.IO) {
    val results = mutableListOf<DeviceContact>()
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER,
    )
    runCatching {
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC",
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val seen = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex) ?: continue
                val number = cursor.getString(numberIndex) ?: continue
                if (seen.add(number)) {
                    results.add(DeviceContact(name, number))
                }
            }
        }
    }
    results
}
