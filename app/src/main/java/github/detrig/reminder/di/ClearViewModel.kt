package github.detrig.reminder.di

import androidx.lifecycle.ViewModel

interface ClearViewModel {
    fun clearViewModel(viewModelClass : Class<out ViewModel>)
}