package github.detrig.reminder.domain.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import github.detrig.reminder.domain.model.Task
import androidx.core.content.edit
import github.detrig.reminder.domain.utils.receivers.TaskReminderReceiver

class NotificationScheduler(
    private val context: Context,
    private val prefs: SharedPreferences
) {

    fun schedule(task: Task, triggerTime: Long) {
        Log.d("alz-alarm", "Alarm set for task ${task.id} at $triggerTime")
        val delay = triggerTime - System.currentTimeMillis()
        if (delay <= 0) return

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("title", task.title)
            putExtra("text", task.notificationText)
            putExtra("imageUri", task.imageUri)
            putExtra("notificationDate", task.notificationDate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d("alz-alarm", "alarmManager.canScheduleExactAlarms() = ${alarmManager.canScheduleExactAlarms()}")
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }

        prefs.edit {
            putLong("task_${task.id}", triggerTime)
        }
    }

    fun cancel(taskId: String) {
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        prefs.edit {
            remove("task_$taskId")
        }
    }
}