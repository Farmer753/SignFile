package ru.ll.sign.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun Uri.getOriginalFileName(context: Context): String? {
    return context.contentResolver.query(this, null, null, null, null)?.use {
        val nameColumnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        it.getString(nameColumnIndex)
    }
}