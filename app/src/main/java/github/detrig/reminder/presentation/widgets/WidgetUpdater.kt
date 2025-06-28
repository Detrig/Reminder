package github.detrig.reminder.presentation.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import github.detrig.reminder.R

object WidgetUpdater {
    fun update(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, TasksWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.widgetListView)
    }
}