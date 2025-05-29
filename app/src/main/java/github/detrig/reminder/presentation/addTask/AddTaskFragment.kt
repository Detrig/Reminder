package github.detrig.reminder.presentation.addTask

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import github.detrig.reminder.core.AbstractFragment
import github.detrig.reminder.core.ProvideViewModel
import github.detrig.reminder.databinding.FragmentAddTaskBinding
import github.detrig.reminder.domain.model.DAYS
import github.detrig.reminder.domain.model.Task
import github.detrig.reminder.presentation.tasksList.TasksListViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import androidx.core.net.toUri
import github.detrig.reminder.domain.utils.TaskReminderReceiver
import java.util.TimeZone

class AddTaskFragment : AbstractFragment<FragmentAddTaskBinding>() {

    private lateinit var viewModel: TasksListViewModel
    private var selectedDate = Calendar.getInstance()
    private var selectedTime = Calendar.getInstance()
    private val selectedDays = mutableMapOf<DAYS, Boolean>().apply {
        DAYS.values().forEach { day -> put(day, false) }
    }
    private var selectedImageUri: Uri? = null
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                // 🟢 ВАЖНО: взять разрешение
                requireActivity().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                selectedImageUri = it
                loadImageWithGlide(it)
            }
        }


    override fun bind(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentAddTaskBinding = FragmentAddTaskBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as ProvideViewModel).viewModel(TasksListViewModel::class.java)

        setupUI()
        setupListeners()
    }

    private fun setupUI() {

        binding.btnBack.setOnClickListener { viewModel.backToPreviousScreen() }

//        binding.etTitle.addTextChangedListener { text ->
//            binding.btnSave.isEnabled = text?.toString()?.isNotBlank() ?: false
//        }

        viewModel.clickedTaskLiveDataWrapper()?.let {
            if (it.title.isNotBlank()) {
                binding.etTitle.setText(it.title)
                binding.etDescription.setText(it.description)
                binding.etNotificationText.setText(it.notificationText)
                binding.tvSelectedDateTime.text = "${it.notificationDate} ${it.notificationTime}"
                binding.headerOfTask.text = "Редактирование задачи"
                it.periodicityDaysWithTime.forEach {
                    when (it.first) {
                        DAYS.MONDAY -> binding.chipMonday.isChecked = true
                        DAYS.TUESDAY -> binding.chipTuesday.isChecked = true
                        DAYS.WEDNESDAY -> binding.chipWednesday.isChecked = true
                        DAYS.THURSDAY -> binding.chipThursday.isChecked = true
                        DAYS.FRIDAY -> binding.chipFriday.isChecked = true
                        DAYS.SATURDAY -> binding.chipSaturday.isChecked = true
                        DAYS.SUNDAY -> binding.chipSunday.isChecked = true
                    }
                }
                if (it.imageUri.isNotBlank()) {
                    selectedImageUri = it.imageUri.toUri()
                    loadImageWithGlide(it.imageUri.toUri())

                }
            } else {
                binding.headerOfTask.text = "Новая задача"
            }
        }
    }

    private fun setupListeners() {
        binding.btnSelectDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnSelectTime.setOnClickListener {
            showTimePickerDialog()
        }

        binding.chipMonday.setOnCheckedChangeListener { _, isChecked ->
            selectedDays[DAYS.MONDAY] = isChecked
        }

        binding.chipTuesday.setOnCheckedChangeListener { _, isChecked ->
            selectedDays[DAYS.TUESDAY] = isChecked
        }

        binding.chipWednesday.setOnCheckedChangeListener { _, isChecked ->
            selectedDays[DAYS.WEDNESDAY] = isChecked
        }

        binding.chipThursday.setOnCheckedChangeListener { _, isChecked ->
            selectedDays[DAYS.THURSDAY] = isChecked
        }

        binding.chipFriday.setOnCheckedChangeListener { _, isChecked ->
            selectedDays[DAYS.FRIDAY] = isChecked
        }

        binding.chipSaturday.setOnCheckedChangeListener { _, isChecked ->
            selectedDays[DAYS.SATURDAY] = isChecked
        }

        binding.chipSunday.setOnCheckedChangeListener { _, isChecked ->
            selectedDays[DAYS.SUNDAY] = isChecked
        }

        // Save button
        binding.btnSave.setOnClickListener {
            saveTask()
        }

        binding.btnAddImage.setOnClickListener {
            pickImageLauncher.launch(arrayOf("image/*"))
        }

        binding.btnRemoveImage.setOnClickListener {
            selectedImageUri = "".toUri()
            binding.TaskImage.setImageDrawable(null)
            binding.TaskImage.visibility = View.GONE
            binding.btnRemoveImage.visibility = View.GONE
            binding.btnAddImage.text = "Добавить изображение"
            val currentTask = viewModel.clickedTaskLiveDataWrapper() ?: Task()
            viewModel.updateTaskStatus(currentTask.copy(imageUri = ""))
        }
    }

    private fun showDatePickerDialog() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { millis ->
            selectedDate.timeInMillis = millis
            updateDateTimeText()
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER_TAG")
    }

    private fun showTimePickerDialog() {
        val timePicker = MaterialTimePicker.Builder()
            .setTitleText("Выберите время")
            .setHour(selectedTime.get(Calendar.HOUR_OF_DAY))
            .setMinute(selectedTime.get(Calendar.MINUTE))
            .build()

        timePicker.addOnPositiveButtonClickListener {
            selectedTime.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            selectedTime.set(Calendar.MINUTE, timePicker.minute)
            updateDateTimeText()
        }

        timePicker.show(parentFragmentManager, "TIME_PICKER")
    }


    private fun updateDateTimeText() {
        // Обновим selectedDate, чтобы объединить дату и время
        selectedDate.set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
        selectedDate.set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
        selectedDate.set(Calendar.SECOND, 0)
        selectedDate.set(Calendar.MILLISECOND, 0)

        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        binding.tvSelectedDateTime.text = dateFormat.format(selectedDate.time)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun saveTask() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val clickedTask = viewModel.clickedTaskLiveDataWrapper() ?: Task()
        var task = Task()
        if (clickedTask.id.isNotBlank()) {
            task = Task(
                id = clickedTask.id,
                title = binding.etTitle.text.toString(),
                description = binding.etDescription.text.toString(),
                notificationText = binding.etNotificationText.text.toString(),
                notificationTime = timeFormat.format(selectedTime.time),
                notificationDate = dateFormat.format(selectedDate.time),
                periodicityDaysWithTime = getSelectedDays()
                    .filter { it.value }
                    .map { it.key to timeFormat.format(selectedTime.time) }
                    .toSet(),
                imageUri = (selectedImageUri?.toString())
                    ?: viewModel.clickedTaskLiveDataWrapper()?.imageUri ?: "",
                isActive = true
            )
        } else {
            task = Task(
                id = UUID.randomUUID().toString(),
                title = binding.etTitle.text.toString(),
                description = binding.etDescription.text.toString(),
                notificationText = binding.etNotificationText.text.toString(),
                notificationTime = timeFormat.format(selectedTime.time),
                notificationDate = dateFormat.format(selectedDate.time),
                periodicityDaysWithTime = selectedDays
                    .filter { it.value }
                    .map { it.key to timeFormat.format(selectedTime.time) }
                    .toSet(),
                imageUri = selectedImageUri.toString(),
                isActive = true
            )
        }
        viewModel.saveOrUpdateTask(task)

        val triggerDate = selectedDate.time
        val now = System.currentTimeMillis()


        if (triggerDate.time > now) {
            Log.d("alz-debug", "About to scheduleNotification")
            scheduleNotification(requireContext(), task, triggerDate.time)
        } else {
            Log.w("alz-debug", "Trigger time is in the past, skipping scheduling.")
        }
    }


    private fun loadImageWithGlide(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(binding.TaskImage)

        if (uri != null && uri.toString().isNotBlank() && uri.toString() != "null") {
            binding.TaskImage.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility = View.VISIBLE
            binding.btnAddImage.text = "Изменить изображение"
        }
    }


    fun scheduleNotification(context: Context, task: Task, triggerTime: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra("title", task.title)
            putExtra("text", task.notificationText)
            putExtra("imageUri", task.imageUri)
        }


        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Разрешение отсутствует — нужно либо запросить у пользователя, либо объяснить, что функция недоступна
                Log.w("alz-debug", "Cannot schedule exact alarms, permission denied")
                return
            }
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

    }

    private fun getSelectedDays(): MutableMap<DAYS, Boolean> {
        val selectedDays_check = mutableMapOf<DAYS, Boolean>()


        if (binding.chipMonday.isChecked == true) {
            selectedDays_check[DAYS.MONDAY] = true
        }
        if (binding.chipTuesday.isChecked) {
            selectedDays_check[DAYS.TUESDAY] = true
        }
        if (binding.chipWednesday.isChecked) {
            selectedDays_check[DAYS.WEDNESDAY] = true
        }
        if (binding.chipThursday.isChecked) {
            selectedDays_check[DAYS.THURSDAY] = true
        }
        if (binding.chipFriday.isChecked) {
            selectedDays_check[DAYS.FRIDAY] = true
        }
        if (binding.chipSaturday.isChecked) {
            selectedDays_check[DAYS.SATURDAY] = true
        }
        if (binding.chipSunday.isChecked) {
            selectedDays_check[DAYS.SUNDAY] = true
        }

        return selectedDays_check
    }

    fun parseDateTime(date: String, time: String): Long {
        val combined = "$date $time" // например, "2025-05-29 04:21"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        val dateObj = sdf.parse(combined)
        return dateObj?.time ?: 0L
    }

    override fun onDestroy() {
        super.onDestroy()

    }
}