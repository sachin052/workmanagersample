package com.teamb.workmanagersample.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.teamb.workmanagersample.remote.FileApi
import com.teamb.workmanagersample.R
import com.teamb.workmanagersample.common.WorkerKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

class DownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        startForegroundService()
        delay(5000)

        val response = FileApi.instance.downloadImage()
        response.body()?.let { body ->
            return withContext(Dispatchers.IO) {
                val file = File(context.cacheDir, "image.jpg")
                val outputStream = FileOutputStream(file)

                outputStream.use { stream ->
                    try {
                        stream.write(body.bytes())
                    } catch (e: Exception) {
                        return@withContext Result.failure(
                            workDataOf(
                                WorkerKeys.ERROR_MSG to e.localizedMessage
                            )
                        )
                    }

                }
                return@withContext Result.success(
                    workDataOf(
                        WorkerKeys.IMAGE_URI to file.toUri().toString()
                    )
                )
            }

        }

        if (!response.isSuccessful) {
            if (response.code().toString().startsWith("5")) {
                return Result.retry()
            }
            return Result.failure(
                workDataOf(
                    WorkerKeys.ERROR_MSG to " invalid server response"
                )
            )
        }

        return Result.failure(
            workDataOf(
                WorkerKeys.ERROR_MSG to "unknown Error"
            )
        )
    }


    private suspend fun startForegroundService() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                NotificationCompat.Builder(context, "download_channel")
                    .setContentTitle("Downloading")
                    .setContentText("download in progress")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .build()
            )
        )
    }
}