package github.detrig.reminder.di

import androidx.lifecycle.ViewModel
import github.detrig.reminder.presentation.main.di.ProvideMainViewModel
import github.detrig.reminder.presentation.tasksList.di.ProvideTasksListViewModel

interface ProvideViewModel {

    fun <T : ViewModel> viewModel(viewModelClass: Class<T>): T

    class Base(
        core: Core
    ) : ProvideViewModel {

        private lateinit var chain: ProvideViewModel

        init {
            chain = ProvideViewModel.Error()
            chain = ProvideMainViewModel(core, chain)
            chain = ProvideTasksListViewModel(core, chain)
        }

        override fun <T : ViewModel> viewModel(viewModelClass: Class<T>): T {
            return chain.viewModel(viewModelClass)
        }
    }

    class Error : ProvideViewModel {
        override fun <T : ViewModel> viewModel(viewModelClass: Class<T>): T {
            throw IllegalStateException("unknown viewModelClass $viewModelClass")
        }
    }
}