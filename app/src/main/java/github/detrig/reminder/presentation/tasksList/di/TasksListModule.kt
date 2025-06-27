package github.detrig.reminder.presentation.tasksList.di

import github.detrig.reminder.di.Core
import github.detrig.reminder.di.Module
import github.detrig.reminder.di.ProvideViewModel
import github.detrig.reminder.di.AbstractProvideViewModel
import github.detrig.reminder.presentation.TasksListViewModel

class TasksListModule(private val core: Core) : Module<TasksListViewModel> {
    override fun viewModel(): TasksListViewModel {
        return TasksListViewModel(
            core.clearViewModel,
            core.navigation,
            core.taskRepository,
            core.notificationScheduler,
            core.allTasksLiveDataWrapper,
            core.viewModelScope
        )
    }
}

class ProvideTasksListViewModel(val core: Core, next: ProvideViewModel) : AbstractProvideViewModel(
    core, next,
    TasksListViewModel::class.java
) {
    override fun module(): Module<*> = TasksListModule(core)
}