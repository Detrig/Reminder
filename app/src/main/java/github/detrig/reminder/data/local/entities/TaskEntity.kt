package github.detrig.reminder.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val notificationText: String,
    val notificationTime: String,
    val notificationDate: String,
    val timestamp: Long = 0L,
    val periodicityDaysWithTime: String,  // JSON: "[{"day":"MONDAY","time":"12:00"}]"
    val image: String,
    val isActive: Boolean
)