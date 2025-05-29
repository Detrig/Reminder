package github.detrig.reminder.core

import androidx.lifecycle.ViewModel
import github.detrig.reminder.data.TaskDatabase
import github.detrig.reminder.data.repository.TaskRepositoryImpl
import github.detrig.reminder.domain.utils.AllTasksLiveDataWrapper
import github.detrig.reminder.domain.utils.ClickedTaskLiveDataWrapper
import github.detrig.reminder.main.MainViewModel
import github.detrig.reminder.presentation.tasksList.TasksListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob


interface ProvideViewModel {

    fun <T : ViewModel> viewModel(viewModelClass: Class<T>): T

    class Base(
        private val clear: ClearViewModel,
        private val appDatabase: TaskDatabase
    ) : ProvideViewModel {

        private val navigation = Navigation.Base()
        private val taskRepository = TaskRepositoryImpl(appDatabase.taskDao())
        private val allTasksLiveDataWrapper = AllTasksLiveDataWrapper.Base()
        private val clickedTaskLiveDataWrapper = ClickedTaskLiveDataWrapper.Base()

        private val viewModelScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        override fun <T : ViewModel> viewModel(viewModelClass: Class<T>): T {
            return when (viewModelClass) {
                MainViewModel::class.java -> MainViewModel(navigation)
                TasksListViewModel::class.java -> TasksListViewModel(navigation, taskRepository, clickedTaskLiveDataWrapper, allTasksLiveDataWrapper, viewModelScope)
                else -> throw IllegalStateException("unknown viewModelClass $viewModelClass")
            } as T
        }
    }
}