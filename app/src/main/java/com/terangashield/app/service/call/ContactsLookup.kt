package com.terangashield.app.service.call

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract

object ContactsLookup {
    fun isKnownContact(context: Context, phoneNumber: String): Boolean =
        getContactDisplayName(context, phoneNumber) != null

    /** Nom affiché du contact correspondant à ce numéro, ou `null` si inconnu du carnet d'adresses. */
    fun getContactDisplayName(context: Context, phoneNumber: String): String? {
        if (phoneNumber.isBlank()) return null
        val uri: Uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber),
        )
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.DISPLAY_NAME),
                null,
                null,
                null,
            )
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                cursor.getString(nameIndex)
            } else {
                null
            }
        } catch (e: SecurityException) {
            null
        } finally {
            cursor?.close()
        }
    }
}
