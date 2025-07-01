package github.detrig.reminder.presentation.calendar

import androidx.core.os.bundleOf
import github.detrig.reminder.core.Screen

class CalendarScreen(notificationDate: String) : Screen.Replace(
    CalendarFragment::class.java,
    bundleOf("NOTIFICATION_DATE_KEY" to notificationDate)
)