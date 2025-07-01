package github.detrig.reminder.domain.utils.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import github.detrig.reminder.di.AppDependenciesProvider
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {

            val repo = AppDependenciesProvider.taskRepository()
            val scheduler = AppDependenciesProvider.scheduler()

            runBlocking {
                val now = System.currentTimeMillis()
                val tasks = repo.getAllTasks()
                tasks.filter { it.timestamp > now }.forEach {
                    scheduler.schedule(it, it.timestamp)
                }
            }
        }
    }
}