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
import com.bumptech.glide.Glide
import github.detrig.reminder.R
import github.detrig.reminder.databinding.TaskRcViewItemBinding
import github.detrig.reminder.domain.model.Task

class TasksRcViewAdapter(
    private val listener: OnTaskClickListener
) : RecyclerView.Adapter<TasksRcViewAdapter.ViewHolder>() {

    val list: ArrayList<Task> = arrayListOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = TaskRcViewItemBinding.bind(view)

        fun bind(task: Task, listener: OnTaskClickListener) = with(binding) {

            taskTitleTV.text = task.title
            taskNotificationTimeTV.text = "Время напоминания: " + task.notificationTime

            if (task.imageUri.isNotBlank() && task.imageUri != null) {
                Glide.with(itemView.context)
                    .load(task.imageUri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_timer)
                    .into(binding.taskImage)

            }

            when (task.isActive) {
                true -> {
                    statusTV.text = "Активно"
                    (statusTV.background as? GradientDrawable)?.setColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.light_green
                        )
                    )
                }

                false -> {
                    statusTV.text = "Завершено"
                    (statusTV.background as? GradientDrawable)?.setColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.light_red
                        )
                    )
                }
            }

            binding.taskMenuButton.setOnClickListener { view ->
                val popupMenu =
                    PopupMenu(ContextThemeWrapper(view.context, R.style.PopupMenu), view)
                popupMenu.menuInflater.inflate(R.menu.task_item_menu, popupMenu.menu)

                val statusMenuItem = popupMenu.menu.findItem(R.id.action_change_status)
                statusMenuItem.icon = if (task.isActive) {
                    ContextCompat.getDrawable(itemView.context, R.drawable.im_notification_on)
                } else {
                    ContextCompat.getDrawable(itemView.context, R.drawable.im_notification_off)
                }
                statusMenuItem.title = if (task.isActive) "Завершить" else "Активировать"

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.action_delete -> {
                            listener.onDeleteClick(task)
                            true
                        }

                        R.id.action_change_status -> {
                            listener.onStatusChangeClick(task)
                            true
                        }

                        else -> false
                    }
                }
                MenuCompat.setGroupDividerEnabled(popupMenu.menu, true)
                popupMenu.show()
            }

            itemView.setOnClickListener {
                listener.onClick(task)
            }
        }
    }

    fun update(newList: ArrayList<Task>) {
        val diffUtil = DiffUtilCallBack(list, newList)
        val diff = DiffUtil.calculateDiff(diffUtil)

        list.clear()
        list.addAll(newList)
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.task_rc_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position], listener)
    }

    class DiffUtilCallBack(
        private val old: List<Task>,
        private val new: List<Task>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = old.size

        override fun getNewListSize(): Int = new.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = old[oldItemPosition]
            val newItem = new[newItemPosition]

            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = old[oldItemPosition]
            val newItem = new[newItemPosition]

            return oldItem == newItem
        }

    }

    interface OnTaskClickListener {
        fun onClick(task: Task)
        fun onDeleteClick(task: Task)
        fun onStatusChangeClick(task: Task)
    }
}