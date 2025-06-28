package github.detrig.reminder.presentation

import androidx.lifecycle.ViewModel
import github.detrig.reminder.di.ClearViewModel
import github.detrig.reminder.core.Navigation
import github.detrig.reminder.core.Screen
import github.detrig.reminder.domain.model.Task
import github.detrig.reminder.domain.repository.TaskRepository
import github.detrig.reminder.domain.utils.AllTasksLiveDataWrapper
import github.detrig.reminder.domain.utils.DateUtil
import github.detrig.reminder.domain.utils.NotificationScheduler
import github.detrig.reminder.presentation.addTask.AddTaskScreen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class TasksListViewModel(
    private val clearViewModel: ClearViewModel,
    private val navigation: Navigation,
    private val taskRepository: TaskRepository,
    private val notificationScheduler: NotificationScheduler,
    private val allTasksLiveDataWrapper: AllTasksLiveDataWrapper,
    private val viewModelScope: CoroutineScope,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
    private val dispatcherIo: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        getAllTasks()
    }

    fun tasksLiveData() = allTasksLiveDataWrapper.liveData()

    fun getAllTasks() {
        viewModelScope.launch(dispatcherIo) {
            val allTasks = taskRepository.getAllTasks()

            withContext(dispatcherMain) {
                allTasksLiveDataWrapper.update(allTasks)

            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch(dispatcherIo) {
            taskRepository.deleteTask(task)
            cancelReminder(task)
            withContext(dispatcherMain) {
                val allTasksList =
                    allTasksLiveDataWrapper.liveData().value?.toMutableList() ?: mutableListOf()
                allTasksList.remove(task)
                allTasksLiveDataWrapper.update(allTasksList)
            }
        }
    }

    fun saveOrUpdateTask(tasks: List<Task>) {
        viewModelScope.launch(dispatcherIo) {
            tasks.forEach { task ->
                val existingTask = taskRepository.getTaskById(task.id)

                if (existingTask != null) {
                    taskRepository.updateTask(task)
                    cancelReminder(existingTask)
                } else {
                    taskRepository.insertTask(task)
                }
                setReminder(
                    task,
                    DateUtil.getTriggerTimeMillis(task.notificationDate, task.notificationTime)
                )
            }
            withContext(dispatcherMain) {
                val currentList =
                    allTasksLiveDataWrapper.liveData().value?.toMutableList() ?: mutableListOf()

                tasks.forEach { task ->
                    val index = currentList.indexOfFirst { it.id == task.id }
                    if (index != -1) {
                        currentList[index] = task
                    } else {
                        currentList.add(task)
                    }
                }

                allTasksLiveDataWrapper.update(currentList)
                backToPreviousScreen()
            }
        }
    }

    fun updateTaskStatus(task: Task) {
        viewModelScope.launch(dispatcherIo) {
            val existingTask = taskRepository.getTaskById(task.id)

            if (!task.isActive) {
                cancelReminder(task)
            } else {
                setReminder(
                    task,
                    DateUtil.getTriggerTimeMillis(task.notificationDate, task.notificationTime)
                )
            }

            if (existingTask != null) {
                taskRepository.updateTask(task)
            } else {
                taskRepository.insertTask(task)
            }

            withContext(dispatcherMain) {
                val currentList =
                    allTasksLiveDataWrapper.liveData().value?.toMutableList() ?: mutableListOf()
                val index = currentList.indexOfFirst { it.id == task.id }

                if (index != -1) {
                    currentList[index] = task
                } else {
                    currentList.add(task)
                }

                allTasksLiveDataWrapper.update(currentList)
            }
        }
    }

    fun getTasksByDate(date: String): List<Task> {
        val allTasks = allTasksLiveDataWrapper.liveData().value ?: emptyList()
        val tasksForDate = allTasks.filter { it.notificationDate == date }
        return tasksForDate
    }

    fun setReminder(task: Task, time: Long) {
        notificationScheduler.schedule(task, time)
    }

    fun cancelReminder(task: Task) {
        notificationScheduler.cancel(task.id)
    }

    fun addTaskScreen(task: Task) {
        if (task.title.isNotBlank()) {
            navigation.update(AddTaskScreen(task))
        } else {
            navigation.update(AddTaskScreen(Task()))
        }
    }

    fun backToPreviousScreen() = navigation.update(Screen.Pop)
}
