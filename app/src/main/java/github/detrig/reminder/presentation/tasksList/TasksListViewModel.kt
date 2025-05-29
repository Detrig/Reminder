package github.detrig.reminder.presentation.tasksList

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import github.detrig.reminder.core.Navigation
import github.detrig.reminder.core.Screen
import github.detrig.reminder.domain.model.Task
import github.detrig.reminder.domain.repository.TaskRepository
import github.detrig.reminder.domain.utils.AllTasksLiveDataWrapper
import github.detrig.reminder.domain.utils.ClickedTaskLiveDataWrapper
import github.detrig.reminder.presentation.addTask.AddTaskFragment
import github.detrig.reminder.presentation.addTask.AddTaskScreen
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TasksListViewModel(
    private val navigation: Navigation,
    private val taskRepository: TaskRepository,
    private val clickedTaskLiveDataWrapper : ClickedTaskLiveDataWrapper,
    private val allTasksLiveDataWrapper : AllTasksLiveDataWrapper,
    private val viewModelScope: CoroutineScope,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
    private val dispatcherIo: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    init {
        getAllTasks()
    }


    fun tasksLiveData() = allTasksLiveDataWrapper.liveData()
    fun clickedTaskLiveDataWrapper() = clickedTaskLiveDataWrapper.liveData().value

    fun getAllTasks() {
        viewModelScope.launch(dispatcherIo) {
            val allTasks = taskRepository.getAllTasks()

            withContext(dispatcherMain) {
                allTasksLiveDataWrapper.update(allTasks)
                Log.d("alz-04", "allTasksIds: ${allTasks.map {it.id}}")
            }
        }
    }

//    fun addTask(task: Task) {
//        viewModelScope.launch(dispatcherIo) {
//            taskRepository.insertTask(task)
//            Log.d("alz-04", "task added: $task")
//            withContext(dispatcherMain) {
//                navigation.update(Screen.Pop)
//            }
//        }
//    }

    fun deleteTask(task : Task) {
        viewModelScope.launch(dispatcherIo) {
            taskRepository.deleteTask(task)
            withContext(dispatcherMain) {
                val allTasksList = allTasksLiveDataWrapper.liveData().value?.toMutableList() ?: mutableListOf()
                allTasksList.remove(task)
                allTasksLiveDataWrapper.update(allTasksList)
            }
        }
    }

    fun saveOrUpdateTask(task: Task) {
        viewModelScope.launch(dispatcherIo) {
            val existingTask = taskRepository.getTaskById(task.id)
            Log.d("alz-04", "task: $task")
            Log.d("alz-04", "isTaskExist: $existingTask")
            if (existingTask != null) {
                taskRepository.updateTask(task)
            } else {
                taskRepository.insertTask(task)
            }

            withContext(dispatcherMain) {
                val currentList = allTasksLiveDataWrapper.liveData().value?.toMutableList() ?: mutableListOf()
                val index = currentList.indexOfFirst { it.id == task.id }

                if (index != -1) {
                    currentList[index] = task
                } else {
                    currentList.add(task)
                }

                allTasksLiveDataWrapper.update(currentList)
                backToPreviousScreen()
            }
        }
    }

    fun updateTaskStatus(task: Task) {
        viewModelScope.launch(dispatcherIo) {
            val existingTask = taskRepository.getTaskById(task.id)
            Log.d("alz-04", "task: $task")
            Log.d("alz-04", "isTaskExist: $existingTask")
            if (existingTask != null) {
                taskRepository.updateTask(task)
            } else {
                taskRepository.insertTask(task)
            }

            withContext(dispatcherMain) {
                val currentList = allTasksLiveDataWrapper.liveData().value?.toMutableList() ?: mutableListOf()
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

    fun getTasksByDate(date: String) : List<Task> {
//        viewModelScope.launch(dispatcherIo) {
//            val tasksForDate = taskRepository.getTasksByDate(date)
//            withContext(dispatcherMain) {
//                _tasksForClickedDate.value = tasksForDate
//            }
//        }
        val allTasks = allTasksLiveDataWrapper.liveData().value ?: emptyList()
        val tasksForDate = allTasks.filter { it.notificationDate == date }
        return tasksForDate
    }



    fun addTaskScreen(task: Task) {
        if (task.title.isNotBlank()) {
            clickedTaskLiveDataWrapper.update(task)
            navigation.update(AddTaskScreen)
        } else {
            clickedTaskLiveDataWrapper.update(Task())
            navigation.update(AddTaskScreen)
        }
    }
    fun backToPreviousScreen() = navigation.update(Screen.Pop)
}