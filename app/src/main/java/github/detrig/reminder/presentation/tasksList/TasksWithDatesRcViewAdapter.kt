package github.detrig.reminder.presentation.tasksList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import github.detrig.reminder.R
import github.detrig.reminder.databinding.TasksWithDateRcViewItemBinding
import github.detrig.reminder.domain.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateTasksRcViewAdapter(
    private val listener: TasksRcViewAdapter.OnTaskClickListener
) : RecyclerView.Adapter<DateTasksRcViewAdapter.ViewHolder>() {

    private var dateItems: List<DateTaskItem> = emptyList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = TasksWithDateRcViewItemBinding.bind(view)

        fun bind(dateTaskItem: DateTaskItem, listener: TasksRcViewAdapter.OnTaskClickListener) {
            binding.tasksDateTV.text = formatDate(dateTaskItem.date)

            val tasksAdapter = TasksRcViewAdapter(listener)
            tasksAdapter.list.addAll(dateTaskItem.tasks)

            binding.tasksRcView.adapter = tasksAdapter
        }

        private fun formatDate(dateStr: String): String {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateStr) ?: return dateStr

            val outputFormat = if (isToday(date)) {
                SimpleDateFormat("'Today,' EEEE, d MMMM", Locale.getDefault())
            } else {
                SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
            }

            return outputFormat.format(date)
        }

        private fun isToday(date: Date): Boolean {
            val today = Calendar.getInstance()
            val calDate = Calendar.getInstance().apply { time = date }
            return today.get(Calendar.YEAR) == calDate.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == calDate.get(Calendar.DAY_OF_YEAR)
        }
    }

    fun update(newTasks: List<Task>) {
        val newMap = newTasks.groupBy { it.notificationDate.substringBefore(" ") }
            .toSortedMap(compareBy<String> { it })

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Calendar.getInstance().time)

        val filteredMap = newMap.filterKeys { it >= today }

        val newDateItems = filteredMap.map { (date, tasks) ->
            DateTaskItem(date, ArrayList(tasks))
        }

        val diffResult = DiffUtil.calculateDiff(
            DateTaskDiffCallback(dateItems, newDateItems)
        )

        dateItems = newDateItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.tasks_with_date_rc_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dateItems.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dateItems[position], listener)
    }

    private inner class DateTaskDiffCallback(
        private val oldItems: List<DateTaskItem>,
        private val newItems: List<DateTaskItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldItems.size

        override fun getNewListSize(): Int = newItems.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldItems[oldPos].date == newItems[newPos].date
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            val oldItem = oldItems[oldPos]
            val newItem = newItems[newPos]

            return oldItem.date == newItem.date &&
                    oldItem.tasks == newItem.tasks
        }

        override fun getChangePayload(oldPos: Int, newPos: Int): Any? {
            return super.getChangePayload(oldPos, newPos)
        }
    }

    data class DateTaskItem(
        val date: String,
        val tasks: ArrayList<Task>
    )
}