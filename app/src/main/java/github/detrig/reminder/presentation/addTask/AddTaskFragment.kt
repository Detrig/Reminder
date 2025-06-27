package github.detrig.reminder.presentation.addTask

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
import github.detrig.reminder.di.ProvideViewModel
import github.detrig.reminder.databinding.FragmentAddTaskBinding
import github.detrig.reminder.domain.model.Task
import github.detrig.reminder.presentation.TasksListViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.applandeo.materialcalendarview.CalendarDay
import github.detrig.reminder.domain.utils.DateUtil
import github.detrig.reminder.domain.utils.TaskReminderReceiver
import java.util.Date
import java.util.TimeZone

class AddTaskFragment : AbstractFragment<FragmentAddTaskBinding>() {

    private lateinit var viewModel: TasksListViewModel
    private lateinit var clickedTask: Task
    private var selectedDate = Calendar.getInstance()
    private var selectedTime = Calendar.getInstance()
    private val selectedDays = mutableMapOf<Int, Boolean>()
    private val calendar = Calendar.getInstance()
    private val now = System.currentTimeMillis()
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

        clickedTask = requireArguments().getSerializable("TASK_KEY") as Task
        Log.d("alz-04", "task get: $clickedTask")

        clickedTask?.let {
            if (it.title.isNotBlank()) {
                binding.etTitle.setText(it.title)
                binding.etDescription.setText(it.description)
                binding.etNotificationText.setText(it.notificationText)
                binding.tvSelectedDateTime.text = "${it.notificationDate} ${it.notificationTime}"
                binding.headerOfTask.text = "Редактирование задачи"
                it.periodicityDaysWithTime.forEach {
                    when (it.first) {
                        Calendar.MONDAY -> binding.chipMonday.isChecked = true
                        Calendar.TUESDAY -> binding.chipTuesday.isChecked = true
                        Calendar.WEDNESDAY -> binding.chipWednesday.isChecked = true
                        Calendar.THURSDAY -> binding.chipThursday.isChecked = true
                        Calendar.FRIDAY -> binding.chipFriday.isChecked = true
                        Calendar.SATURDAY -> binding.chipSaturday.isChecked = true
                        Calendar.SUNDAY -> binding.chipSunday.isChecked = true
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
            selectedDays[Calendar.MONDAY] = isChecked
        }

        binding.chipTuesday.setOnCheckedChangeListener { _, isChecked ->
            selectedDays[Calendar.TUESDAY] = isChecked
        }

        binding.chipWednesday.setOnCheckedChangeListener { _, isChecked ->
            selectedDays[Calendar.WEDNESDAY] = isChecked
        }

        binding.chipThursday.setOnCheckedChangeListener { _, isChecked ->
            selectedDays[Calendar.THURSDAY] = isChecked
        }

        binding.chipFriday.setOnCheckedChangeListener { _, isChecked ->
            selectedDays[Calendar.FRIDAY] = isChecked
        }

        binding.chipSaturday.setOnCheckedChangeListener { _, isChecked ->
            selectedDays[Calendar.SATURDAY] = isChecked
        }

        binding.chipSunday.setOnCheckedChangeListener { _, isChecked ->
            selectedDays[Calendar.SUNDAY] = isChecked
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
            val currentTask = clickedTask ?: Task()
            viewModel.updateTaskStatus(currentTask.copy(imageUri = ""))
        }
    }

    private fun showDatePickerDialog() {
        val currentText = binding.tvSelectedDateTime.text.toString()
        val initialSelection = if (currentText != "Дата и время не выбраны") {
            try {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val date = dateFormat.parse(currentText)
                date?.time ?: MaterialDatePicker.todayInUtcMilliseconds()
            } catch (e: Exception) {
                MaterialDatePicker.todayInUtcMilliseconds()
            }
        } else {
            MaterialDatePicker.todayInUtcMilliseconds()
        }

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату")
            .setSelection(initialSelection)
            .build()

        datePicker.addOnPositiveButtonClickListener { millis ->
            selectedDate.timeInMillis = millis
            updateDateTimeText()
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER_TAG")
    }

    private fun showTimePickerDialog() {
        // Пытаемся получить время из текстового поля
        val currentText = binding.tvSelectedDateTime.text.toString()
        val (hour, minute) = if (currentText != "Дата и время не выбраны") {
            try {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val date = dateFormat.parse(currentText)
                val calendar = Calendar.getInstance().apply { time = date ?: Date() }
                calendar.get(Calendar.HOUR_OF_DAY) to calendar.get(Calendar.MINUTE)
            } catch (e: Exception) {
                selectedTime.get(Calendar.HOUR_OF_DAY) to selectedTime.get(Calendar.MINUTE)
            }
        } else {
            selectedTime.get(Calendar.HOUR_OF_DAY) to selectedTime.get(Calendar.MINUTE)
        }

        val timePicker = MaterialTimePicker.Builder()
            .setTitleText("Выберите время")
            .setHour(hour)
            .setMinute(minute)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            selectedTime.set(Calendar.HOUR_OF_DAY, timePicker.hour)
            selectedTime.set(Calendar.MINUTE, timePicker.minute)
            updateDateTimeText()
        }

        timePicker.show(parentFragmentManager, "TIME_PICKER")
    }


    private fun updateDateTimeText() {
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

        val clickedTask = clickedTask
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
                    ?: clickedTask.imageUri ?: "",
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

        val allTasksList = mutableListOf<Task>().apply {
            add(task) // Добавляем исходную задачу
        }

        task.periodicityDaysWithTime.forEach { (dayOfWeek, timeStr) ->
            val dates = DateUtil.findDatesForDayInNext30Days(dayOfWeek)

            dates.forEachIndexed { index, dateStr ->
                val newTask = task.copy(
                    id = task.id + allTasksList.size + 1,
                    notificationDate = dateStr // или сохраняйте Calendar, если нужно
                )
                allTasksList.add(newTask)
            }
        }
        Log.d("alz-04", "allTasksDate: ${allTasksList.map { it.notificationDate }}")


        viewModel.saveOrUpdateTask(allTasksList)


        val triggerDate = selectedDate.time
        val now = System.currentTimeMillis()


        if (triggerDate.time > now) {
            Log.d("alz-debug", "About to scheduleNotification")
            scheduleNotification(requireContext(), task, triggerDate.time)
        } else {
            Log.w("alz-debug", "Trigger time is in the past, skipping scheduling.")
        }

        //scheduleRecurringNotificationsForMonth(requireContext(), task)
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


    private fun scheduleRecurringNotificationsForMonth(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Месяц вперед от текущей даты
        val endDate = Calendar.getInstance().apply {
            add(Calendar.MONTH, 1)
        }.timeInMillis

        // Для каждого дня в периодичности
        task.periodicityDaysWithTime.forEach { (dayOfWeek, timeStr) ->

            // Парсим время (формат "HH:mm")
            val timeParts = timeStr.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            // Находим следующее вхождение этого дня недели
            calendar.timeInMillis = now
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Переходим к следующему указанному дню недели
            while (calendar.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            // Создаем уведомления для каждого вхождения до конца месяца
            while (calendar.timeInMillis <= endDate) {
                if (calendar.timeInMillis > now) {
                    val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                        putExtra("title", task.title)
                        putExtra("text", task.notificationText)
                        putExtra("imageUri", task.imageUri)
                    }

                    val requestCode = (task.id + calendar.timeInMillis.toString()).hashCode()
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (!alarmManager.canScheduleExactAlarms()) {
                            Log.w("alz-debug", "Cannot schedule exact alarms, permission denied")
                            continue
                        }
                    }

                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )

                    Log.d("alz-debug", "Scheduled notification for ${calendar.time}")
                }

                // Переходим к следующей неделе
                calendar.add(Calendar.DAY_OF_MONTH, 7)
            }
        }
    }

    private fun getSelectedDays(): MutableMap<Int, Boolean> { //DAY - isChecked
        val selectedDays_check = mutableMapOf<Int, Boolean>()

        if (binding.chipMonday.isChecked == true) {
            selectedDays_check[Calendar.MONDAY] = true
        }
        if (binding.chipTuesday.isChecked) {
            selectedDays_check[Calendar.TUESDAY] = true
        }
        if (binding.chipWednesday.isChecked) {
            selectedDays_check[Calendar.WEDNESDAY] = true
        }
        if (binding.chipThursday.isChecked) {
            selectedDays_check[Calendar.THURSDAY] = true
        }
        if (binding.chipFriday.isChecked) {
            selectedDays_check[Calendar.FRIDAY] = true
        }
        if (binding.chipSaturday.isChecked) {
            selectedDays_check[Calendar.SATURDAY] = true
        }
        if (binding.chipSunday.isChecked) {
            selectedDays_check[Calendar.SUNDAY] = true
        }

        return selectedDays_check
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}