package com.picker.mylibrary

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.system.measureTimeMillis

object CompressImage {

    private fun compressImage(
        sourceFile: File,
        destinationFile: File,
    ): Long {
        return measureTimeMillis {
            val currentBitmap = BitmapFactory.decodeFile(sourceFile.absolutePath)
            currentBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                FileUtils.COMPRESS_QUALITY,
                FileOutputStream(destinationFile)
            )
        }
    }

    suspend fun compressImageCallBack(
        context: AppCompatActivity,
        sourceImageFile: File,
        callBack: (String, File, Uri) -> Unit
    ) =
        withContext(Dispatchers.IO) {
            val file =FileUtils.createImageFile(context)
            try {
                val timeToExecute = compressImage(sourceImageFile, file)
                android.os.Handler(Looper.getMainLooper()).postDelayed({
                    callBack.invoke(file.absolutePath, file, file.toUri())
                },timeToExecute)
            } catch (e: Exception) {
                Log.e("Rk", "Error : ${e.localizedMessage!!.toString()}")
            }
        }
}
