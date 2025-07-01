package github.detrig.reminder.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import github.detrig.reminder.data.local.DayTimePair
import github.detrig.reminder.data.local.dao.TaskDao
import github.detrig.reminder.data.local.entities.TaskEntity
import github.detrig.reminder.domain.model.Task
import github.detrig.reminder.domain.repository.TaskRepository

class TaskRepositoryImpl(
    private val taskDao: TaskDao
) : TaskRepository {

    private val gson = Gson()
    private val dayTimeListType = object : TypeToken<List<DayTimePair>>() {}.type

    override suspend fun insertTask(task: Task) {
        taskDao.insert(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        taskDao.update(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.delete(task.toEntity())
    }

    override suspend fun getTaskById(id: String): Task? {
        return taskDao.getById(id)?.toDomain()
    }

    override suspend fun getAllTasks(): List<Task> {
        return taskDao.getAll().map { it.toDomain() }
    }

    override suspend fun getTasksByDate(date: String): List<Task> {
        return taskDao.getByDate(date).map { it.toDomain() }
    }

    // Мапперы Entity <-> Domain
    private fun Task.toEntity(): TaskEntity {
        return TaskEntity(
            id = id,
            title = title,
            description = description,
            notificationText = notificationText,
            notificationTime = notificationTime,
            notificationDate = notificationDate,
            timestamp = timestamp,
            periodicityDaysWithTime = gson.toJson(
                periodicityDaysWithTime.map { DayTimePair(it.first, it.second) }
            ),
            image = imageUri,
            isActive = isActive
        )
    }

    private fun TaskEntity.toDomain(): Task {
        val serializedList: List<DayTimePair> = gson.fromJson(
            periodicityDaysWithTime,
            dayTimeListType
        ) ?: emptyList()

        return Task(
            id = id,
            title = title,
            description = description,
            notificationText = notificationText,
            notificationTime = notificationTime,
            notificationDate = notificationDate,
            timestamp = timestamp,
            periodicityDaysWithTime = serializedList.map {
                it.day to it.time
            }.toSet(),
            imageUri = image,
            isActive = isActive
        )
    }
}