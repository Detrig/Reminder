package github.detrig.reminder.domain.model

import com.applandeo.materialcalendarview.CalendarDay
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val notificationText: String = "",
    val notificationTime: String = "",
    val notificationDate: String = "",
    val periodicityDaysWithTime: Set<Pair<Int, String>> = setOf(), //Calendar.Day, time
    val imageUri: String = "",
    val isActive: Boolean = true
) : Serializable

fun CalendarDay.toTaskDateFormat(): String {
    val calendar = this.calendar
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
}

fun String.toCalendar(): Calendar {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val date = dateFormat.parse(this) ?: throw IllegalArgumentException("Invalid date format")

    return Calendar.getInstance().apply {
        time = date
    }
}

