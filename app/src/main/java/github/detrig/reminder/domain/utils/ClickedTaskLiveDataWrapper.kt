package github.detrig.reminder.domain.utils

import github.detrig.reminder.core.LiveDataWrapper
import github.detrig.reminder.domain.model.Task

interface ClickedTaskLiveDataWrapper : LiveDataWrapper.Mutable<Task> {
    class Base : ClickedTaskLiveDataWrapper, LiveDataWrapper.Abstract<Task>()
}