package github.detrig.reminder.presentation.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.applandeo.materialcalendarview.CalendarDay
import com.applandeo.materialcalendarview.listeners.OnCalendarDayClickListener
import github.detrig.reminder.R
import github.detrig.reminder.core.AbstractFragment
import github.detrig.reminder.di.ProvideViewModel
import github.detrig.reminder.databinding.FragmentCalendarBinding
import github.detrig.reminder.domain.model.Task
import github.detrig.reminder.domain.model.toCalendar
import github.detrig.reminder.domain.model.toTaskDateFormat
import github.detrig.reminder.presentation.TasksListViewModel
import github.detrig.reminder.presentation.tasksList.TasksRcViewAdapter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CalendarFragment : AbstractFragment<FragmentCalendarBinding>() {

    private lateinit var viewModel: TasksListViewModel
    private lateinit var tasksRcViewAdapter: TasksRcViewAdapter
    private var clickedDate = ""

    override fun bind(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCalendarBinding = FragmentCalendarBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as ProvideViewModel).viewModel(TasksListViewModel::class.java)


        initViews()
        binding.calendarView.setOnCalendarDayClickListener(object : OnCalendarDayClickListener {
            override fun onClick(calendarDay: CalendarDay) {
                clickedDate = calendarDay.toTaskDateFormat()

                val tasksForDate = viewModel.getTasksByDate(clickedDate).toMutableList()

                tasksRcViewAdapter.update(ArrayList(tasksForDate))
            }
        })

        viewModel.tasksLiveData().observe(viewLifecycleOwner) {
            val tasksForClickedDay = it.filter { it.notificationDate == clickedDate }
            tasksRcViewAdapter.update(ArrayList(tasksForClickedDay))
            updateCalendar(it)
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
        binding.tasksRView.adapter = tasksRcViewAdapter


        viewModel.tasksLiveData().value?.let { tasks ->
            updateCalendar(tasks)
        }
    }

    private fun updateCalendar(tasks: List<Task>) {
        val calendarDays = ArrayList<CalendarDay>()
        val tasksWithDates = tasks.filter { it.notificationDate.isNotBlank() }

        //Set color workload for date
        //get Date-TaskCount map for multicolor
        val dateTaskMap = mutableMapOf<String, List<Task>>()
        val allTasksDates = tasksWithDates.map { it.notificationDate }.toSet()
        tasksWithDates.forEach { task ->
            val date = task.notificationDate
            if (date in allTasksDates) {
                dateTaskMap[date]?.let {
                    val list = dateTaskMap[date]!!.toMutableList()
                    list.add(task)
                    dateTaskMap[date] = list
                } ?: run {
                    dateTaskMap[date] = listOf(task)
                }
            }
        }
        Log.d("alz-04", "dateTaskMap: $dateTaskMap")

        dateTaskMap.forEach { (date, tasksForDateList) ->
            try {
                val calendar = date.toCalendar()

                val backgroundRes: Int = when (tasksForDateList.count()) {
                    1, 2 -> R.drawable.calendar_workload_status_level_1
                    3, 4 -> R.drawable.calendar_workload_status_level_2
                    5, 6 -> R.drawable.calendar_workload_status_level_3
                    else -> R.drawable.calendar_workload_status_level_4
                }

                var isAllTasksForDateDone = true
                for (task in tasksForDateList) {
                    if (task.isActive) {
                        isAllTasksForDateDone = false
                        break
                    }
                }

                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Calendar.getInstance().time)
                Log.d("alz-04", "today: $today, date: $date")
                val calendarDay = CalendarDay(calendar).apply {
                    labelColor = R.color.white
                    backgroundResource = backgroundRes
                    if (date < today)
                        imageResource = if (isAllTasksForDateDone) R.drawable.ic_done_all else R.drawable.ic_cross
                }

                calendarDays.add(calendarDay)
            } catch (e: Exception) {
                Log.e("Calendar", "Error parsing date: $date", e)
            }
        }

        binding.calendarView.setCalendarDays(calendarDays)
    }
}