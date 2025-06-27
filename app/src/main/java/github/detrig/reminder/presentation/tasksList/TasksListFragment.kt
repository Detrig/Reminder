package github.detrig.reminder.presentation.tasksList

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import github.detrig.reminder.core.AbstractFragment
import github.detrig.reminder.di.ProvideViewModel
import github.detrig.reminder.databinding.FragmentTasksListBinding
import github.detrig.reminder.domain.model.Task
import github.detrig.reminder.presentation.TasksListViewModel

class TasksListFragment : AbstractFragment<FragmentTasksListBinding>() {

    private lateinit var viewModel: TasksListViewModel
    private lateinit var dateTasksRcViewAdapter: DateTasksRcViewAdapter

    override fun bind(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTasksListBinding = FragmentTasksListBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as ProvideViewModel).viewModel(TasksListViewModel::class.java)

        initViews()
        viewModel.getAllTasks()

        viewModel.tasksLiveData().value?.let {
            dateTasksRcViewAdapter.update(ArrayList(it))
        }

        viewModel.tasksLiveData().observe(viewLifecycleOwner) {
            dateTasksRcViewAdapter.update(ArrayList(it))
        }
        printAllScheduledNotifications(requireContext())
    }

    private fun initViews() {
        dateTasksRcViewAdapter = DateTasksRcViewAdapter(object : TasksRcViewAdapter.OnTaskClickListener {
            override fun onClick(task: Task) {
                viewModel.addTaskScreen(task)
            }

            override fun onDeleteClick(task: Task) {
                AlertDialog.Builder(requireContext())
                    .setTitle("Удаление задачи")
                    .setMessage("Вы уверены, что хотите удалить задачу?")
                    .setPositiveButton("Удалить") { _, _ ->
                        viewModel.deleteTask(task)
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }

            override fun onStatusChangeClick(task: Task) {
                viewModel.updateTaskStatus(task.copy(isActive = !task.isActive))
            }
        })
        binding.tasksWithDateRcView.adapter = dateTasksRcViewAdapter

        binding.addTaskButton.setOnClickListener {
            viewModel.addTaskScreen(Task())
        }
    }

    //Debug
    fun printAllScheduledNotifications(context: Context) {
        val prefs = context.getSharedPreferences("work_manager_prefs", Context.MODE_PRIVATE)
        val allEntries = prefs.all

        if (allEntries.isEmpty()) {
            Log.d("alz-04", "Нет запланированных уведомлений")
            return
        }

        Log.d("alz-04", "Запланированные уведомления:")
        allEntries.forEach { (key, value) ->
            if (key.startsWith("task_")) {
                val taskId = key.removePrefix("task_")
                val workId = value.toString()
                Log.d("alz-04", "Task ID: $taskId, WorkManager ID: $workId")
            }
        }
    }
}

