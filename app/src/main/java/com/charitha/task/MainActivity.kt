package com.charitha.task

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay
import com.charitha.task.data.AppDatabase
import com.charitha.task.data.Task
import com.charitha.task.ui.theme.TaskTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notification permission denied. Reminders won't work.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val database = AppDatabase.getDatabase(this)

        setContent {
            TaskTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val taskList by database.taskDao().getAllTasks().collectAsState(initial = emptyList())
                    val context = LocalContext.current

                    var taskText by remember { mutableStateOf("") }
                    var priority by remember { mutableIntStateOf(0) } // 0: Low, 1: Medium, 2: High
                    var deadline by remember { mutableStateOf<Long?>(null) }
                    var reminderTime by remember { mutableStateOf<Long?>(null) }
                    var selectedTab by remember { mutableIntStateOf(0) } // 0: To Do, 1: Completed

                    val colorOptions = listOf(
                        Color.White,
                        Color(0xFFFFCDD2), // Pastel Red
                        Color(0xFFC8E6C9), // Pastel Green
                        Color(0xFFBBDEFB), // Pastel Blue
                        Color(0xFFFFF9C4), // Pastel Yellow
                        Color(0xFFF1F8E9)  // Light Green
                    )
                    var selectedColor by remember { mutableStateOf(colorOptions[0]) }

                    val pendingTasks = taskList.filter { !it.isCompleted }
                    val completedTasks = taskList.filter { it.isCompleted }

                    var showSplash by remember { mutableStateOf(true) }

                    LaunchedEffect(Unit) {
                        delay(2000) // Show splash for 2 seconds
                        showSplash = false
                    }

                    if (showSplash) {
                        SplashScreen()
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            // --- INPUT SECTION (Centered lower) ---
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1.5f), // Increased weight to push it lower
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = selectedColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        TextField(
                                            value = taskText,
                                            onValueChange = { taskText = it },
                                            placeholder = { Text("Enter a new task...") },
                                            modifier = Modifier.fillMaxWidth(),
                                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                                            colors = TextFieldDefaults.colors(
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedContainerColor = Color.Transparent
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Priority Selector
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Priority: ", fontSize = 14.sp, color = Color.Black)
                                            listOf("Low", "Med", "High").forEachIndexed { index, label ->
                                                FilterChip(
                                                    selected = priority == index,
                                                    onClick = { priority = index },
                                                    label = { Text(label) },
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row {
                                                // Deadline Picker
                                                IconButton(onClick = {
                                                    showDatePicker(context) { deadline = it }
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.DateRange,
                                                        contentDescription = "Deadline",
                                                        tint = if (deadline != null) MaterialTheme.colorScheme.primary else Color(0xFF006400) // Dark Green
                                                    )
                                                }

                                                // Reminder Picker
                                                IconButton(onClick = {
                                                    showDateTimePicker(context) { reminderTime = it }
                                                }) {
                                                    Icon(
                                                        imageVector = Icons.Default.Notifications,
                                                        contentDescription = "Reminder",
                                                        tint = if (reminderTime != null) MaterialTheme.colorScheme.primary else Color(0xFF8B4513) // Brown
                                                    )
                                                }
                                            }

                                            // Color Picker
                                            Row {
                                                colorOptions.forEach { color ->
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .padding(2.dp)
                                                            .clip(CircleShape)
                                                            .background(color)
                                                            .clickable { selectedColor = color }
                                                            .border(
                                                                if (selectedColor == color) BorderStroke(
                                                                    2.dp,
                                                                    Color.Gray
                                                                ) else BorderStroke(
                                                                    0.dp,
                                                                    Color.Transparent
                                                                ),
                                                                CircleShape
                                                            )
                                                    )
                                                }
                                            }
                                        }

                                        if (deadline != null || reminderTime != null) {
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                if (deadline != null) {
                                                    Text(
                                                        "Due: ${formatDate(deadline!!)} ",
                                                        fontSize = 12.sp,
                                                        color = Color.DarkGray
                                                    )
                                                }
                                                if (reminderTime != null) {
                                                    Text(
                                                        "Remind: ${formatDateTime(reminderTime!!)}",
                                                        fontSize = 12.sp,
                                                        color = Color.DarkGray
                                                    )
                                                }
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (taskText.isNotBlank()) {
                                                    val newTask = Task(
                                                        title = taskText,
                                                        priority = priority,
                                                        color = selectedColor.toArgb().toLong(),
                                                        deadline = deadline,
                                                        reminderTime = reminderTime
                                                    )
                                                    lifecycleScope.launch {
                                                        val id = database.taskDao().insertTask(newTask)

                                                        // Schedule reminder
                                                        if (reminderTime != null) {
                                                            val savedTask =
                                                                newTask.copy(id = id.toInt())
                                                            ReminderHelper.scheduleReminder(
                                                                context,
                                                                savedTask
                                                            )
                                                        }

                                                        taskText = ""
                                                        priority = 0
                                                        deadline = null
                                                        reminderTime = null
                                                        selectedColor = colorOptions[0]
                                                    }
                                                }
                                            },
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Text("Add Task")
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // --- TABS ---
                            TabRow(selectedTabIndex = selectedTab) {
                                Tab(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    text = { Text("To Do (${pendingTasks.size})") }
                                )
                                Tab(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    text = { Text("Completed (${completedTasks.size})") }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // --- TASK LISTS ---
                            Box(modifier = Modifier.weight(2f)) {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    val displayTasks =
                                        if (selectedTab == 0) pendingTasks else completedTasks

                                    if (displayTasks.isEmpty()) {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillParentMaxSize()
                                                    .padding(bottom = 64.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    if (selectedTab == 0) "No tasks yet!" else "No completed tasks.",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    } else {
                                        items(displayTasks) { task ->
                                            TaskItem(task, onToggle = {
                                                lifecycleScope.launch {
                                                    database.taskDao()
                                                        .updateTask(task.copy(isCompleted = it))
                                                }
                                            }, onDelete = {
                                                lifecycleScope.launch {
                                                    database.taskDao().deleteTask(task)
                                                }
                                            })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "App Logo",
                modifier = Modifier.size(150.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Task Master",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TaskItem(task: Task, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(task.color.toInt())),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onToggle
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 18.sp,
                    fontWeight = if (task.isHighPriority) FontWeight.Bold else FontWeight.Normal,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val priorityLabel = when (task.priority) {
                        1 -> "Medium"
                        2 -> "High"
                        else -> "Low"
                    }
                    val priorityColor = when (task.priority) {
                        1 -> Color(0xFFFFA000)
                        2 -> Color.Red
                        else -> Color.Gray
                    }
                    
                    Surface(
                        color = priorityColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, priorityColor)
                    ) {
                        Text(
                            priorityLabel,
                            fontSize = 10.sp,
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    if (task.deadline != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "📅 ${formatDate(task.deadline)}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

private fun showDatePicker(context: Context, onDateSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            onDateSelected(selectedCalendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun showDateTimePicker(context: Context, onDateTimeSelected: (Long) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth, hourOfDay, minute)
                    onDateTimeSelected(selectedCalendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
