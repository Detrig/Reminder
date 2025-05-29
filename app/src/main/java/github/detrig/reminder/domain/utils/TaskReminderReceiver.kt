package github.detrig.reminder.domain.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import github.detrig.reminder.R

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("alz-debug", "Receiver triggered!")
        val title = intent?.getStringExtra("title") ?: "Задача"
        val text = intent?.getStringExtra("text") ?: "Срок задачи наступил"
        val imageUri = intent?.getStringExtra("imageUri")


        val notificationBuilder = NotificationCompat.Builder(context, "task_channel")
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        imageUri?.let {
            try {
                val bitmap = Glide.with(context).asBitmap().load(Uri.parse(it)).submit().get()
                notificationBuilder.setStyle(
                    NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Напоминания",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_timer) // добавь подходящую иконку
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(1001, notification)
    }
}