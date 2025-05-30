package github.detrig.reminder.presentation.tasksList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import github.detrig.reminder.core.AbstractFragment
import github.detrig.reminder.core.ProvideViewModel
import github.detrig.reminder.databinding.FragmentTasksListBinding
import github.detrig.reminder.domain.model.DAYS
import github.detrig.reminder.domain.model.Task

class TasksListFragment : AbstractFragment<FragmentTasksListBinding>() {

    private lateinit var viewModel: TasksListViewModel
    private lateinit var tasksRcViewAdapter: TasksRcViewAdapter

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
            tasksRcViewAdapter.update(ArrayList(it))
        }

        viewModel.tasksLiveData().observe(viewLifecycleOwner) {
            tasksRcViewAdapter.update(ArrayList(it))
        }
    }

    private fun initViews() {
        tasksRcViewAdapter = TasksRcViewAdapter(object : TasksRcViewAdapter.OnTaskClickListener {
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
        binding.tasksRcView.adapter = tasksRcViewAdapter

        binding.addTaskButton.setOnClickListener {
            viewModel.addTaskScreen(Task())
        }
    }

}

