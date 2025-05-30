package github.detrig.reminder.domain.repository

import github.detrig.reminder.domain.model.Task

interface TaskRepository {
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun getTaskById(id: String): Task?
    suspend fun getAllTasks(): List<Task>
    suspend fun getTasksByDate(date: String): List<Task>
}