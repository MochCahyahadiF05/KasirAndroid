package com.example.kasirberas.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FileHelper(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Save bitmap sebagai gambar ke gallery
     */
    suspend fun saveBitmapToGallery(
        bitmap: Bitmap,
        transactionId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val timestamp = dateFormat.format(Date())
            val filename = "Receipt_${transactionId}_$timestamp.jpg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ menggunakan MediaStore
                saveToMediaStore(bitmap, filename)
            } else {
                // Android 9 dan bawah menggunakan external storage
                saveToExternalStorage(bitmap, filename)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save bitmap ke downloads folder
     */
    suspend fun saveBitmapToDownloads(
        bitmap: Bitmap,
        transactionId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val timestamp = dateFormat.format(Date())
            val filename = "Receipt_${transactionId}_$timestamp.jpg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ menggunakan MediaStore Downloads
                saveToDownloadsMediaStore(bitmap, filename)
            } else {
                // Android 9 dan bawah menggunakan Downloads folder
                saveToDownloadsFolder(bitmap, filename)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save menggunakan MediaStore untuk Android 10+
     */
    private fun saveToMediaStore(bitmap: Bitmap, filename: String): Result<String> {
        val contentResolver: ContentResolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/KasirApp")
        }

        val uri: Uri? = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        return uri?.let { imageUri ->
            contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                Result.success("Gambar berhasil disimpan ke Gallery")
            } ?: Result.failure(IOException("Tidak dapat membuka output stream"))
        } ?: Result.failure(IOException("Tidak dapat membuat URI"))
    }

    /**
     * Save menggunakan MediaStore Downloads untuk Android 10+
     */
    private fun saveToDownloadsMediaStore(bitmap: Bitmap, filename: String): Result<String> {
        val contentResolver: ContentResolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/KasirApp")
            }
        }

        val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        } else {
            null
        }

        return uri?.let { downloadUri ->
            contentResolver.openOutputStream(downloadUri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                Result.success("Gambar berhasil didownload ke folder Downloads")
            } ?: Result.failure(IOException("Tidak dapat membuka output stream"))
        } ?: Result.failure(IOException("Tidak dapat membuat URI"))
    }

    /**
     * Save ke external storage untuk Android 9 dan bawah
     */
    private fun saveToExternalStorage(bitmap: Bitmap, filename: String): Result<String> {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val kasirAppDir = File(picturesDir, "KasirApp")

        if (!kasirAppDir.exists()) {
            kasirAppDir.mkdirs()
        }

        val file = File(kasirAppDir, filename)

        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

                // Notify MediaScanner agar gambar muncul di gallery
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, file.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

                Result.success("Gambar berhasil disimpan ke ${file.absolutePath}")
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    /**
     * Save ke downloads folder untuk Android 9 dan bawah
     */
    private fun saveToDownloadsFolder(bitmap: Bitmap, filename: String): Result<String> {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val kasirAppDir = File(downloadsDir, "KasirApp")

        if (!kasirAppDir.exists()) {
            kasirAppDir.mkdirs()
        }

        val file = File(kasirAppDir, filename)

        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                Result.success("Gambar berhasil didownload ke ${file.absolutePath}")
            }
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    /**
     * Create temporary file untuk sharing atau printing
     */
    suspend fun createTempFile(bitmap: Bitmap, transactionId: String): Result<File> = withContext(Dispatchers.IO) {
        try {
            val timestamp = dateFormat.format(Date())
            val filename = "temp_receipt_${transactionId}_$timestamp.jpg"

            val tempFile = File(context.cacheDir, filename)

            FileOutputStream(tempFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }

            Result.success(tempFile)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    /**
     * Delete temporary file
     */
    fun deleteTempFile(file: File): Boolean {
        return if (file.exists()) {
            file.delete()
        } else {
            true
        }
    }

    /**
     * Check if external storage is available
     */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * Get readable file size
     */
    fun getReadableFileSize(sizeInBytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = sizeInBytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024.0
            unitIndex++
        }

        return String.format("%.1f %s", size, units[unitIndex])
    }
}