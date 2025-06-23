package github.detrig.reminder.core

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import github.detrig.reminder.di.ClearViewModel
import github.detrig.reminder.di.Core
import github.detrig.reminder.di.ProvideViewModel
import github.detrig.reminder.di.ViewModelFactory


class App : Application(), ProvideViewModel {

    private lateinit var factory: ViewModelFactory
    private lateinit var core: Core



    override fun onCreate() {
        super.onCreate()

        val clearViewModel: ClearViewModel = object : ClearViewModel {
            override fun clearViewModel(viewModelClass: Class<out ViewModel>) {
                factory.clearViewModel(viewModelClass)
            }
        }
        core = Core(this, clearViewModel)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        val taskDatabase = TaskDatabase.getInstance(this)

        val provideViewModel = ProvideViewModel.Base(core)
        factory = ViewModelFactory.Base(provideViewModel)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Reminders"
            val descriptionText = "Channel for task deadline reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("task_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun <T : ViewModel> viewModel(viewModelClass: Class<T>): T =
        factory.viewModel(viewModelClass)

}