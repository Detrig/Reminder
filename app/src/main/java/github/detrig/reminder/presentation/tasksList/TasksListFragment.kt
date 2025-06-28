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
    private lateinit var dateTasksRcViewAdapter: TasksUnifiedRcViewAdapter

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
        dateTasksRcViewAdapter =
            TasksUnifiedRcViewAdapter(object : TasksUnifiedRcViewAdapter.OnTaskClickListener {
                override fun onClick(task: Task) {
                    if (!dateTasksRcViewAdapter.isSelectionMode)
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

                override fun onSelectionChanged(selectedCount: Int) {
                    updateDeleteButton()
                }

                override fun onActivateSelectionMode() {
                    dateTasksRcViewAdapter.isSelectionMode = true
                }
            })
        binding.tasksWithDateRcView.adapter = dateTasksRcViewAdapter

        binding.addTaskButton.setOnClickListener {
            viewModel.addTaskScreen(Task())
        }

        binding.deleteTasksButton.setOnClickListener {
            showDeleteDialog(dateTasksRcViewAdapter.getSelectedItems())
        }
    }

    private fun showDeleteDialog(tasks: List<Task>) {
        AlertDialog.Builder(requireContext())
            .setTitle(if (tasks.size > 1) "Удаление задач" else "Удаление задачи")
            .setMessage(
                if (tasks.size > 1)
                    "Вы уверены, что хотите удалить выбранные задачи?"
                else
                    "Вы уверены, что хотите удалить задачу?"
            )
            .setPositiveButton("Удалить") { _, _ ->
                tasks.forEach {
                    viewModel.deleteTask(it)
                }
                dateTasksRcViewAdapter.clearSelection()
                updateDeleteButton()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun updateDeleteButton() {
        val selectedCount = dateTasksRcViewAdapter.getSelectedItems().size
        binding.deleteTasksButton.visibility = if (selectedCount > 0) View.VISIBLE else View.GONE
    }

    //Debug
    fun printAllScheduledNotifications(context: Context) {
        val prefs = context.getSharedPreferences("work_manager_prefs", Context.MODE_PRIVATE)
        val allEntries = prefs.all

        if (allEntries.isEmpty()) {
            Log.d("alz-04", "Нет запланированных уведомлений")
            return
        }

        //Log.d("alz-04", "Запланированные уведомления:")
        allEntries.forEach { (key, value) ->
            if (key.startsWith("task_")) {
                val taskId = key.removePrefix("task_")
                val workId = value.toString()
               // Log.d("alz-04", "Task ID: $taskId, WorkManager ID: $workId")
            }
        }
    }
}