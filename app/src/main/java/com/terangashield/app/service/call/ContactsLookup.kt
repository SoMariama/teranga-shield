package com.terangashield.app.service.call

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract

object ContactsLookup {
    fun isKnownContact(context: Context, phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) return false
        val uri: Uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber),
        )
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup._ID),
                null,
                null,
                null,
            )
            cursor != null && cursor.moveToFirst()
        } catch (e: SecurityException) {
            false
        } finally {
            cursor?.close()
        }
    }
}
