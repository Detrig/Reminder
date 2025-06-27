package github.detrig.reminder.domain.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import github.detrig.reminder.R

class TaskNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: "Задача"
        val text = inputData.getString("text") ?: "Срок задачи наступил"
        val imageUri = inputData.getString("imageUri")

        createNotification(applicationContext, title, text, imageUri)

        return Result.success()
    }

    private fun createNotification(
        context: Context,
        title: String,
        text: String,
        imageUri: String?
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Напоминания",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Канал для напоминаний о задачах"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, "reminder_channel")
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            val bitmap = when {
                !imageUri.isNullOrBlank() -> {
                    Log.d("alz-04", "Trying to load image from URI: $imageUri")
                    try {
                        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(
                            Uri.parse(imageUri), "r"
                        )
                        parcelFileDescriptor?.use {
                            BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
                        } ?: throw Exception("Failed to open file descriptor")
                    } catch (e: Exception) {
                        Log.e("alz-04", "Error loading image from URI, using fallback", e)
                        BitmapFactory.decodeResource(context.resources, R.drawable.ic_timer)
                    }
                }

                else -> {
                    BitmapFactory.decodeResource(context.resources, R.drawable.ic_timer)
                }
            }

            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .setBigContentTitle(title)
                    .setSummaryText(text)
            )


            notificationBuilder.setLargeIcon(bitmap)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("alz-04", "Notification image error", e)
        }

        notificationManager.notify(1001, notificationBuilder.build())
    }
}