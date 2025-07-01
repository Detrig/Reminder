package github.detrig.reminder.presentation.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import github.detrig.reminder.R
import github.detrig.reminder.core.App
import github.detrig.reminder.domain.model.Task
import github.detrig.reminder.domain.repository.TaskRepository
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TasksRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory { //Фабрика, возвращающая RemoteViews для каждого элемента списка

    private var todayTasks: List<Task> = emptyList()
    private lateinit var taskRepository : TaskRepository

    override fun onCreate() {
        val app = context.applicationContext as App
        taskRepository = app.core.taskRepository
    }

    override fun onDataSetChanged() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        runBlocking {
            todayTasks = taskRepository.getTasksByDate(today)
        }
    }

    override fun getViewAt(position: Int): RemoteViews {
        val task = todayTasks[position]
        val view = RemoteViews(context.packageName, R.layout.widget_task_item)
        view.setTextViewText(R.id.taskTitle, task.title)
        view.setTextViewText(R.id.taskCheckBox, if (task.isActive) "☐" else "☑")

        return view
    }

    override fun getCount() = todayTasks.size

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true

    override fun onDestroy() {}
}