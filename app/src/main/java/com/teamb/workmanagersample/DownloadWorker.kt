package com.teamb.workmanagersample

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class CoroutineDownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
       // FileApi.instance.downloadImage()
    }
}