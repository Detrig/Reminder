package github.detrig.reminder.core

import android.app.Application
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

    override fun <T : ViewModel> viewModel(viewModelClass: Class<T>): T =
        factory.viewModel(viewModelClass)

}