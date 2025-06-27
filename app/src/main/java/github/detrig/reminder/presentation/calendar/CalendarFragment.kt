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
import java.util.Calendar


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
                val clickedCalendar = calendarDay.calendar

                val tasksForDate = viewModel.getTasksByDate(clickedDate).toMutableList()

//                // Добавим задачи с повторением, если совпадает день недели
//                calendarDay?.let { day ->
//                    viewModel.tasksLiveData().value?.forEach { task ->
//                        if (task.periodicityDaysWithTime.any { it.first == day }) {
//                            tasksForDate.add(task)
//                        }
//                    }
//                }

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
        val today = Calendar.getInstance()
        val oneMonthLater = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }

        val tasksWithDates = tasks.filter { it.notificationDate.isNotBlank() }
        val repeatingTasks = tasks.filter { it.periodicityDaysWithTime.isNotEmpty() }

        //get Date-TaskCount map for multicolor
        val dateTaskMap = mutableMapOf<String, Int>()
        val allTasksDates = tasksWithDates.map { it.notificationDate }.toSet()
        tasksWithDates.forEach { task ->
            val date = task.notificationDate
            if (date in allTasksDates) {
                dateTaskMap[date]?.let {
                    dateTaskMap[date] = it + 1
                } ?: run {
                    dateTaskMap[date] = 1
                }
            }
        }

        // Отображение обычных задач с конкретной датой
        tasksWithDates.forEach { task ->
            try {
                val calendar = task.notificationDate.toCalendar()
                val calendarDay = CalendarDay(calendar).apply {
                    backgroundResource = R.color.light_green
                    //imageResource = if (task.isActive) R.drawable.im_notification_on else R.drawable.im_notification_off
                    labelColor = R.color.primary
                }
                calendarDays.add(calendarDay)
            } catch (e: Exception) {
                Log.e("Calendar", "Error parsing date: ${task.notificationDate}", e)
            }
        }

        // Генерация отображаемых дней для повторяющихся задач
//        var temp = today.clone() as Calendar
//        while (temp.before(oneMonthLater)) {
//            val dayOfWeek = when (temp.get(Calendar.DAY_OF_WEEK)) {
//                Calendar.MONDAY -> DAYS.MONDAY
//                Calendar.TUESDAY -> DAYS.TUESDAY
//                Calendar.WEDNESDAY -> DAYS.WEDNESDAY
//                Calendar.THURSDAY -> DAYS.THURSDAY
//                Calendar.FRIDAY -> DAYS.FRIDAY
//                Calendar.SATURDAY -> DAYS.SATURDAY
//                Calendar.SUNDAY -> DAYS.SUNDAY
//                else -> null
//            }
//
//            dayOfWeek?.let { day ->
//                repeatingTasks.forEach { task ->
//                    if (task.periodicityDaysWithTime.any { it.first == day }) {
//                        val calendarDay = CalendarDay(temp.clone() as Calendar).apply {
//                            imageResource = if (task.isActive) R.drawable.im_notification_on else R.drawable.im_notification_off
//                            labelColor = R.color.primary
//                        }
//                        calendarDays.add(calendarDay)
//                    }
//                }
//            }
//
//            temp.add(Calendar.DAY_OF_YEAR, 1)
//        }

        binding.calendarView.setCalendarDays(calendarDays)
    }
}