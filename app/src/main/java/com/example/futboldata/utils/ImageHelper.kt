package com.example.futboldata.utils

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.widget.Toast

class ImageHelper(private val context: Context) {

    fun convertUriToBase64(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }
}