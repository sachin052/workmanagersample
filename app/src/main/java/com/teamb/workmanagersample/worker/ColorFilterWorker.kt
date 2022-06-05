package com.teamb.workmanagersample.worker

import android.content.Context
import android.graphics.*
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.teamb.workmanagersample.common.WorkerKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ColorFilterWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {

        val imageFile = workerParameters.inputData
            .getString(WorkerKeys.IMAGE_URI)
            ?.toUri()
            ?.toFile()

        return imageFile?.let { file ->
            val bmp = BitmapFactory.decodeFile(file.absolutePath)
            val resultBmp = bmp.copy(bmp.config, true)
            val paint = Paint()
            paint.colorFilter = LightingColorFilter(0x08ff04, 1)

            val canvas = Canvas(resultBmp)
            canvas.drawBitmap(resultBmp, 0f, 0f, paint)

            withContext(Dispatchers.IO) {
                delay(2000)
                val resultFile = File(context.cacheDir, "new_image.jpg")
                val outputStream = FileOutputStream(resultFile)
                val isSuccessFull = resultBmp.compress(
                    Bitmap.CompressFormat.JPEG, 90, outputStream
                )

                if (isSuccessFull) {
                    Result.success(
                        workDataOf(
                            WorkerKeys.FILTER_URI to resultFile.toUri().toString()
                        )
                    )
                } else {
                    Result.failure()
                }
            }
        } ?: Result.failure()

    }
}