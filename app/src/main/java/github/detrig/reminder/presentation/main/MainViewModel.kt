package github.detrig.reminder.presentation.main

import androidx.lifecycle.ViewModel
import github.detrig.reminder.presentation.calendar.CalendarScreen
import github.detrig.reminder.core.Navigation
import github.detrig.reminder.presentation.tasksList.TasksListScreen

class MainViewModel(
    private val navigation: Navigation
) : ViewModel() {

    fun init(firstRun: Boolean) {
        if (firstRun)
            tasksListScreen()
    }

    fun navigationLiveData() = navigation.liveData()

    fun tasksListScreen() = navigation.update(TasksListScreen)

    fun calendarScreen(notificationDate : String = "") = navigation.update(CalendarScreen(notificationDate))
}