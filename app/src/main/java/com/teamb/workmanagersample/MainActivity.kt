package com.teamb.workmanagersample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.work.*
import coil.compose.rememberImagePainter
import com.teamb.workmanagersample.common.WorkerKeys
import com.teamb.workmanagersample.ui.theme.WorkManagerSampleTheme
import com.teamb.workmanagersample.worker.ColorFilterWorker
import com.teamb.workmanagersample.worker.DownloadWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()

        val colorFilterRequest = OneTimeWorkRequestBuilder<ColorFilterWorker>()
            .build()

        val workManager = WorkManager.getInstance(applicationContext)

        setContent {
            WorkManagerSampleTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val workInfos = workManager
                        .getWorkInfosForUniqueWorkLiveData("download")
                        .observeAsState()
                        .value

                    val downloadInfo = remember(key1 = workInfos) {
                        workInfos?.find { it.id == downloadRequest.id }
                    }
                    val colorFilterInfo = remember(key1 = workInfos) {
                        workInfos?.find { it.id == colorFilterRequest.id }
                    }

                    val imagerUri by derivedStateOf {
                        val downloadUri = downloadInfo?.outputData
                            ?.getString(WorkerKeys.IMAGE_URI)?.toUri()
                        val filterUri = colorFilterInfo?.outputData
                            ?.getString(WorkerKeys.FILTER_URI)?.toUri()
                        filterUri ?: downloadUri
                    }

                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        imagerUri?.let { uri ->
                            Image(
                                painter = rememberImagePainter(data = uri),
                                contentDescription = "", Modifier.fillMaxWidth().height(250.dp)
                            )
                        }

                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )

                        Button(
                            onClick = {
                                workManager.beginUniqueWork(
                                    "download",
                                    ExistingWorkPolicy.KEEP,
                                    downloadRequest
                                ).then(
                                    colorFilterRequest
                                ).enqueue()

                            }, enabled = downloadInfo?.state != WorkInfo.State.RUNNING

                        ) {
                            Text(text = "Start  Download")
                        }

                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )

                        when (downloadInfo?.state) {
                            WorkInfo.State.ENQUEUED -> Text(text = "Download enqueued")
                            WorkInfo.State.RUNNING -> Text(text = "Download Running ")
                            WorkInfo.State.SUCCEEDED -> Text(text = "Download Success")
                            WorkInfo.State.FAILED -> Text(text = "Download Failed")
                            WorkInfo.State.BLOCKED -> Text(text = "Download Blocked")
                            WorkInfo.State.CANCELLED -> Text(text = "Download Canceled")
                            null -> Text(text = "Download Canceled")
                        }

                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )

                        when (colorFilterInfo?.state) {
                            WorkInfo.State.ENQUEUED -> Text(text = "Filter enqueued")
                            WorkInfo.State.RUNNING -> Text(text = "Filter Running ")
                            WorkInfo.State.SUCCEEDED -> Text(text = "Filter Success")
                            WorkInfo.State.FAILED -> Text(text = "Filter Failed")
                            WorkInfo.State.BLOCKED -> Text(text = "Filter Blocked")
                            WorkInfo.State.CANCELLED -> Text(text = "Filter Canceled")
                            null ->  Text(text = "Filter empty")
                        }
                    }
                }
            }
        }
    }
}

