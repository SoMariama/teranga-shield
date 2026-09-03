package com.terangashield.app.ui.util

import android.content.Context
import android.text.format.DateUtils

fun relativeTimestamp(context: Context, timestampMillis: Long): String =
    DateUtils.getRelativeTimeSpanString(
        timestampMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()
