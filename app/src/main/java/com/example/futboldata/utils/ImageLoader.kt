package com.example.futboldata.utils

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable

object ImageLoader {

    fun loadBase64Image(
        base64: String,
        imageView: ImageView,
        @DrawableRes defaultDrawable: Int,
        targetSize: Int = 200
    ) {
        if (base64.isEmpty()) {
            imageView.setImageResource(defaultDrawable)
            return
        }

        try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                imageView.setImageResource(defaultDrawable)
                return
            }

            options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)
            imageView.setImageBitmap(bitmap)

        } catch (e: Exception) {
            Log.e("ImageLoader", "Error loading Base64 image: ${e.message}", e)
            imageView.setImageResource(defaultDrawable)
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun loadBase64AsDrawable(
        base64: String,
        context: android.content.Context,
        @DrawableRes defaultDrawable: Int,
        targetSize: Int = 24
    ): Drawable? {
        if (base64.isEmpty()) {
            return createDefaultDrawable(context, defaultDrawable, targetSize)
        }

        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)

            if (options.outWidth <= 0 || options.outHeight <= 0) {
                return createDefaultDrawable(context, defaultDrawable, targetSize)
            }

            options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)
            bitmap?.toDrawable(context.resources)?.apply {
                val sizeInPx = (targetSize * context.resources.displayMetrics.density).toInt()
                setBounds(0, 0, sizeInPx, sizeInPx)
            } ?: createDefaultDrawable(context, defaultDrawable, targetSize)

        } catch (e: Exception) {
            Log.e("ImageLoader", "Error loading Base64 as Drawable: ${e.message}", e)
            createDefaultDrawable(context, defaultDrawable, targetSize)
        }
    }

    private fun createDefaultDrawable(
        context: android.content.Context,
        @DrawableRes drawableRes: Int,
        targetSize: Int
    ): Drawable? {
        return ContextCompat.getDrawable(context, drawableRes)?.apply {
            val sizeInPx = (targetSize * context.resources.displayMetrics.density).toInt()
            setBounds(0, 0, sizeInPx, sizeInPx)
        }
    }
}