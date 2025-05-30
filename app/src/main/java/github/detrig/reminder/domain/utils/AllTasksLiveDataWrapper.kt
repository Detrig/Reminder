package github.detrig.reminder.domain.utils

import github.detrig.reminder.core.ListLiveDataWrapper
import github.detrig.reminder.domain.model.Task

interface AllTasksLiveDataWrapper : ListLiveDataWrapper.All<Task> {
    class Base : AllTasksLiveDataWrapper, ListLiveDataWrapper.Abstract<Task>()
}