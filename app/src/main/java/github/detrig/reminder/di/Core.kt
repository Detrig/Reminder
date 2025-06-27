package github.detrig.reminder.di

import android.content.Context
import github.detrig.reminder.core.Navigation
import github.detrig.reminder.data.TaskDatabase
import github.detrig.reminder.data.repository.TaskRepositoryImpl
import github.detrig.reminder.domain.utils.AllTasksLiveDataWrapper
import github.detrig.reminder.domain.utils.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class Core(context: Context, val clearViewModel: ClearViewModel) {

    //val prefs = context.getSharedPreferences("reminderAppData", Context.MODE_PRIVATE)
    val workPrefs = context.getSharedPreferences("work_manager_prefs", Context.MODE_PRIVATE)
    val notificationScheduler = NotificationScheduler(context, workPrefs)

    val taskDatabase = TaskDatabase.Companion.getInstance(context)

    val navigation = Navigation.Base()
    val taskRepository = TaskRepositoryImpl(taskDatabase.taskDao())
    val allTasksLiveDataWrapper = AllTasksLiveDataWrapper.Base()
    val viewModelScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
}