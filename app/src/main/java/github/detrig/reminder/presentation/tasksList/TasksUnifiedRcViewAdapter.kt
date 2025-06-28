package github.detrig.reminder.presentation.tasksList

import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import github.detrig.reminder.R
import github.detrig.reminder.databinding.TaskRcViewItemBinding
import github.detrig.reminder.databinding.TasksWithDateRcViewItemBinding
import github.detrig.reminder.domain.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TasksUnifiedRcViewAdapter(
    private val listener: OnTaskClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DATE = 0
        private const val TYPE_TASK = 1
    }

    private val items = mutableListOf<ListItem>()
    private val selectedItems = mutableSetOf<String>()
    var isSelectionMode = false


    fun update(newTasks: List<Task>) {
        val grouped = newTasks.groupBy { it.notificationDate.substringBefore(" ") }
            .toSortedMap()

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Calendar.getInstance().time)

        val filtered = grouped.filterKeys { it >= today }

        val newItems = mutableListOf<ListItem>()
        for ((date, tasks) in filtered) {
            newItems.add(ListItem.DateItem(date))
            for (task in tasks) {
                newItems.add(ListItem.TaskItem(task))
            }
        }
        val diffCallback = DiffUtilCallBack(items, newItems, selectedItems, selectedItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        items.clear()
        items.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    fun toggleSelection(taskId: String) {
        if (selectedItems.contains(taskId)) selectedItems.remove(taskId)
        else selectedItems.add(taskId)
        notifyItemChanged(items.indexOfFirst { it is ListItem.TaskItem && it.task.id == taskId })
    }

    fun clearSelection() {
        selectedItems.clear()
        isSelectionMode = false
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Task> {
        return items.filterIsInstance<ListItem.TaskItem>()
            .map { it.task }
            .filter { selectedItems.contains(it.id) }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.DateItem -> TYPE_DATE
            is ListItem.TaskItem -> TYPE_TASK
        }
    }

    inner class DateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = TasksWithDateRcViewItemBinding.bind(view)

        fun bind(item: ListItem.DateItem) {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(item.date)
            val outputFormat = if (date != null && isToday(date)) {
                SimpleDateFormat("'Сегодня,' EEEE, d MMMM", Locale.getDefault())
            } else {
                SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
            }
            binding.tasksDateTV.text = outputFormat.format(date ?: item.date)
        }

        private fun isToday(date: Date): Boolean {
            val today = Calendar.getInstance()
            val cal = Calendar.getInstance().apply { time = date }
            return today.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)
        }
    }

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = TaskRcViewItemBinding.bind(view)

        fun bind(task: Task) = with(binding) {
            taskTitleTV.text = task.title
            taskNotificationTimeTV.text = "Время напоминания: ${task.notificationTime}"

            updateSelectionState(task)

            itemView.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(task.id)
                    updateSelectionState(task)
                    listener.onSelectionChanged(selectedItems.size)
                } else {
                    listener.onClick(task)
                }
            }

            itemView.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleSelection(task.id)
                    listener.onActivateSelectionMode()
                    listener.onSelectionChanged(selectedItems.size)
                    true
                } else {
                    false
                }
            }

            if (task.imageUri.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(task.imageUri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_timer)
                    .into(taskImage)
            }

            statusTV.text = if (task.isActive) "Активно" else "Завершено"
            (statusTV.background as? GradientDrawable)?.setColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (task.isActive) R.color.light_green else R.color.light_red
                )
            )

            taskMenuButton.setOnClickListener { view ->
                val popup = PopupMenu(ContextThemeWrapper(view.context, R.style.PopupMenu), view)
                popup.menuInflater.inflate(R.menu.task_item_menu, popup.menu)

                val statusItem = popup.menu.findItem(R.id.action_change_status)
                statusItem.title = if (task.isActive) "Завершить" else "Активировать"

                popup.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_delete -> {
                            listener.onDeleteClick(task); true
                        }

                        R.id.action_change_status -> {
                            listener.onStatusChangeClick(task); true
                        }

                        else -> false
                    }
                }
                MenuCompat.setGroupDividerEnabled(popup.menu, true)
                popup.show()
            }
        }

        private fun updateSelectionState(task: Task) {
            binding.taskCardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    if (selectedItems.contains(task.id)) R.color.light_blue else R.color.light_gray
                )
            )
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            TYPE_DATE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.tasks_with_date_rc_view_item, parent, false)
                DateViewHolder(view)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.task_rc_view_item, parent, false)
                TaskViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.DateItem -> (holder as DateViewHolder).bind(item)
            is ListItem.TaskItem -> (holder as TaskViewHolder).bind(item.task)
        }
    }

    override fun getItemCount(): Int = items.size

    class DiffUtilCallBack(
        private val oldList: List<ListItem>,
        private val newList: List<ListItem>,
        private val oldSelectedIds: Set<String>,
        private val newSelectedIds: Set<String>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            val oldItem = oldList[oldPos]
            val newItem = newList[newPos]

            return when {
                oldItem is ListItem.DateItem && newItem is ListItem.DateItem ->
                    oldItem.date == newItem.date

                oldItem is ListItem.TaskItem && newItem is ListItem.TaskItem ->
                    oldItem.task.id == newItem.task.id

                else -> false
            }
        }

        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            val oldItem = oldList[oldPos]
            val newItem = newList[newPos]

            return when {
                oldItem is ListItem.DateItem && newItem is ListItem.DateItem ->
                    oldItem == newItem

                oldItem is ListItem.TaskItem && newItem is ListItem.TaskItem ->
                    oldItem.task == newItem.task &&
                            oldSelectedIds.contains(oldItem.task.id) == newSelectedIds.contains(
                        newItem.task.id
                    )

                else -> false
            }
        }
    }

    interface OnTaskClickListener {
        fun onClick(task: Task)
        fun onDeleteClick(task: Task)
        fun onStatusChangeClick(task: Task)
        fun onSelectionChanged(selectedCount: Int)
        fun onActivateSelectionMode()
    }
}

sealed class ListItem {
    data class DateItem(val date: String) : ListItem()
    data class TaskItem(val task: Task) : ListItem()
}