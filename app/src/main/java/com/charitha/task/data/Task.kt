package com.charitha.task.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks_table")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val priority: Int = 0, // 0: Low, 1: Medium, 2: High
    val isCompleted: Boolean = false,
    val color: Long = 0xFFFFFFFF, // Default White
    val deadline: Long? = null,
    val reminderTime: Long? = null,
) {
    val isHighPriority: Boolean get() = priority == 2
}
