package github.detrig.reminder.domain.utils.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import github.detrig.reminder.di.AppDependenciesProvider
import kotlinx.coroutines.runBlocking

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("BootReceiver", "Boot completed received")
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val repo = AppDependenciesProvider.taskRepository()
            val scheduler = AppDependenciesProvider.scheduler()

            runBlocking {
                val now = System.currentTimeMillis()
                val tasks = repo.getAllTasks()
                Log.d("BootReceiver", "Total tasks: ${tasks.size}")
                tasks.filter { it.timestamp > now }.forEach {
                    Log.d("BootReceiver", "Scheduling task: ${it.id} at ${it.timestamp}")
                    scheduler.schedule(it, it.timestamp)
                }
            }
        }
    }
}