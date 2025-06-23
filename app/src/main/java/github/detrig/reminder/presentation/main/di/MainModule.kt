package github.detrig.reminder.presentation.main.di

import github.detrig.reminder.di.AbstractProvideViewModel
import github.detrig.reminder.di.Core
import github.detrig.reminder.di.Module
import github.detrig.reminder.di.ProvideViewModel
import github.detrig.reminder.presentation.main.MainViewModel

class MainModule(private val core: Core) : Module<MainViewModel> {
    override fun viewModel(): MainViewModel {
        return MainViewModel(core.navigation)
    }
}

class ProvideMainViewModel(
    val core: Core,
    next: ProvideViewModel
) : AbstractProvideViewModel(core, next, MainViewModel::class.java) {
    override fun module(): Module<*> = MainModule(core)
}