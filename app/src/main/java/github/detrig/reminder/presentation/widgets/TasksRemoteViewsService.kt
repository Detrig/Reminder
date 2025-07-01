package github.detrig.reminder.presentation.widgets

import android.content.Intent
import android.widget.RemoteViewsService

class TasksRemoteViewsService : RemoteViewsService() { //Сервис, создающий фабрику RemoteViews
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TasksRemoteViewsFactory(applicationContext)
    }
}