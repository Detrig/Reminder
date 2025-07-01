package github.detrig.reminder.di

import android.content.SharedPreferences
import github.detrig.reminder.domain.repository.TaskRepository
import github.detrig.reminder.domain.utils.NotificationScheduler

object AppDependenciesProvider {
    private lateinit var core: Core

    fun init(core: Core) {
        this.core = core
    }

    fun scheduler(): NotificationScheduler = core.notificationScheduler
    fun taskRepository(): TaskRepository = core.taskRepository
    fun prefs(): SharedPreferences = core.workPrefs
}