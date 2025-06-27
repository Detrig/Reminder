package github.detrig.reminder.domain.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import github.detrig.reminder.domain.model.Task
import androidx.core.content.edit
import java.util.concurrent.TimeUnit

class NotificationScheduler(
    private val context: Context,
    private val prefs: SharedPreferences
) {
    fun schedule(task: Task, triggerTime: Long) {
        val inputData = workDataOf(
            "title" to task.title,
            "text" to task.notificationText,
            "imageUri" to task.imageUri
        )

        val delay = triggerTime - System.currentTimeMillis()
        if (delay <= 0) return

        val workRequest = OneTimeWorkRequestBuilder<TaskNotificationWorker>()
            .setInputData(inputData)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("task_${task.id}")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
        prefs.edit { putString("task_${task.id}", workRequest.id.toString()) }
    }

    fun cancel(taskId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("task_$taskId")
        prefs.edit { remove("task_$taskId") }
    }
}