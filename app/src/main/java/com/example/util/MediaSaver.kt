package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class MediaSaver(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36")
                .build()
            chain.proceed(request)
        }
        .build()

    suspend fun saveImage(
        imageUrl: String,
        postId: String,
        index: Int,
        onProgress: (Float) -> Unit = {}
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.newCall(Request.Builder().url(imageUrl).build()).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to download image (HTTP ${response.code})"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty image body"))
            val filename = "TikSaver_${postId}_${index + 1}_${System.currentTimeMillis()}.jpg"

            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TikSaver")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val itemUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

                resolver.openOutputStream(itemUri)?.use { outputStream ->
                    writeStream(body.byteStream(), outputStream, body.contentLength(), onProgress)
                }

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)

                itemUri
            } else {
                val imagesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "TikSaver"
                )
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }

                val file = File(imagesDir, filename)
                FileOutputStream(file).use { outputStream ->
                    writeStream(body.byteStream(), outputStream, body.contentLength(), onProgress)
                }

                val fileUri = Uri.fromFile(file)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf("image/jpeg"),
                    null
                )
                fileUri
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveVideo(
        videoUrl: String,
        postId: String,
        onProgress: (Float) -> Unit = {}
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.newCall(Request.Builder().url(videoUrl).build()).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Failed to download video (HTTP ${response.code})"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Empty video body"))
            val filename = "TikSaver_${postId}_${System.currentTimeMillis()}.mp4"

            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/TikSaver")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val itemUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

                resolver.openOutputStream(itemUri)?.use { outputStream ->
                    writeStream(body.byteStream(), outputStream, body.contentLength(), onProgress)
                }

                values.clear()
                values.put(MediaStore.Video.Media.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)

                itemUri
            } else {
                val moviesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                    "TikSaver"
                )
                if (!moviesDir.exists()) {
                    moviesDir.mkdirs()
                }

                val file = File(moviesDir, filename)
                FileOutputStream(file).use { outputStream ->
                    writeStream(body.byteStream(), outputStream, body.contentLength(), onProgress)
                }

                val fileUri = Uri.fromFile(file)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(file.absolutePath),
                    arrayOf("video/mp4"),
                    null
                )
                fileUri
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun writeStream(
        inputStream: InputStream,
        outputStream: OutputStream,
        totalLength: Long,
        onProgress: (Float) -> Unit
    ) {
        val buffer = ByteArray(8192)
        var bytesRead: Int
        var downloaded: Long = 0

        inputStream.use { input ->
            outputStream.use { output ->
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                    if (totalLength > 0) {
                        onProgress(downloaded.toFloat() / totalLength.toFloat())
                    }
                }
                output.flush()
            }
        }
    }
}
