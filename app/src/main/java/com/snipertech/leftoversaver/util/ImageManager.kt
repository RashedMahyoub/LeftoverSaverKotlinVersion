package com.snipertech.leftoversaver.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore

class ImageManager {
    companion object {
        //method to get the file path from uri
        fun getPath(context: Context, uri: Uri): String? {
            var cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                var documentId: String = cursor.getString(0)
                documentId = documentId.substring(documentId.lastIndexOf(":") + 1)
                cursor.close()
                cursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Media._ID + " = ? ", arrayOf(documentId), null
                )
                cursor?.moveToFirst()
                val path: String? =
                    cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
                        ?.let { cursor.getString(it) }
                cursor?.close()
                return path
            }
            return ""
        }
    }
}