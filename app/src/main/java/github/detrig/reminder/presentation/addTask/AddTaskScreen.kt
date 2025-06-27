package github.detrig.reminder.presentation.addTask

import androidx.core.os.bundleOf
import github.detrig.reminder.core.Screen
import github.detrig.reminder.domain.model.Task

class AddTaskScreen(task: Task) : Screen.Replace(
    AddTaskFragment::class.java,
    bundleOf("TASK_KEY" to task)
)