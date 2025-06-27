package github.detrig.reminder.domain.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtil {
    fun findDatesForDayInNext30Days(dayOfWeek: Int): List<String> {
        val result = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_MONTH, 1)
        }

        repeat(30) {
            if (calendar.get(Calendar.DAY_OF_WEEK) == dayOfWeek) {
                result.add(dateFormat.format(calendar.time))
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return result
    }

}