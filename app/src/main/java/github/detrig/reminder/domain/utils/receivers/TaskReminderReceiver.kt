package github.detrig.reminder.domain.utils.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import github.detrig.reminder.R
import github.detrig.reminder.presentation.main.MainActivity

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("alz-debug", "Receiver triggered!")
        val title = intent?.getStringExtra("title") ?: "Задача"
        val text = intent?.getStringExtra("text") ?: "Срок задачи наступил"
        val imageUri = intent?.getStringExtra("imageUri")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
                !imageUri.isNullOrEmpty() -> {
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

        val timestamp = intent?.getStringExtra("notificationDate")
        val calendarIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NOTIFICATION_DATE_KEY", timestamp)
        }

        // PendingIntent, сработает при клике по уведомлению
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            calendarIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Привязываем к уведомлению
        notificationBuilder.setContentIntent(contentIntent)

        notificationManager.notify(1001, notificationBuilder.build())
    }
}