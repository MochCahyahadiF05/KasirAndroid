package com.example.kasirberas.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class ImageHelper(private val context: Context) {
    fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Compress bitmap
            val compressedBitmap = compressImage(bitmap)

            // Save to internal storage
            val filename = "product_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, "images")
            if (!file.exists()) file.mkdirs()

            val imageFile = File(file, filename)
            val outputStream = FileOutputStream(imageFile)
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.close()

            imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    private fun compressImage(bitmap: Bitmap): Bitmap {
        val maxWidth = 800
        val maxHeight = 600

        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun loadImageIntoView(imagePath: String, imageView: ImageView) {
        if (imagePath.isEmpty()) {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            return
        }

        val file = File(imagePath)
        if (file.exists()) {
            Glide.with(context)
                .load(file)
                .apply(RequestOptions()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop())
                .into(imageView)
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    fun deleteImage(imagePath: String): Boolean {
        if (imagePath.isEmpty()) return true

        val file = File(imagePath)
        return if (file.exists()) {
            file.delete()
        } else {
            true
        }
    }

}