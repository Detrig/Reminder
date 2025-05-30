package github.detrig.reminder.main

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import github.detrig.reminder.R
import github.detrig.reminder.core.ProvideViewModel
import github.detrig.reminder.databinding.ActivityMainBinding
import github.detrig.reminder.domain.utils.TaskReminderReceiver
import github.detrig.reminder.domain.utils.TestReceiver


class MainActivity : AppCompatActivity(), ProvideViewModel {

    private lateinit var binding : ActivityMainBinding
    private lateinit var viewModel : MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        givePermissions()

        viewModel = viewModel(MainViewModel::class.java)

        viewModel.navigationLiveData().observe(this) { screen ->
            screen.show(supportFragmentManager, R.id.fragmentContainer)
        }
        viewModel.init(savedInstanceState == null)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tasksList -> viewModel.tasksListScreen()
                R.id.tasksCalendar -> viewModel.calendarScreen()
            }
            true
        }
    }

    private fun givePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    override fun <T : ViewModel> viewModel(viewModelClass: Class<T>): T =
        (application as ProvideViewModel).viewModel(viewModelClass)
}