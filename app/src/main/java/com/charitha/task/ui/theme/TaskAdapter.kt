package com.charitha.task.ui.theme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.charitha.task.R
import com.charitha.task.data.Task

class TaskAdapter(
    private val onDeleteClicked: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var taskList = emptyList<Task>()

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTaskTitle)
        val tvPriority: TextView = itemView.findViewById(R.id.tvPriorityBadge)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val currentTask = taskList[position]
        holder.tvTitle.text = currentTask.title
        holder.tvPriority.isVisible = currentTask.isHighPriority

        holder.btnDelete.setOnClickListener { onDeleteClicked(currentTask) }
    }

    override fun getItemCount(): Int = taskList.size

    fun submitList(newList: List<Task>) {
        taskList = newList
        notifyDataSetChanged() // Refreshes the screen list
    }
}
