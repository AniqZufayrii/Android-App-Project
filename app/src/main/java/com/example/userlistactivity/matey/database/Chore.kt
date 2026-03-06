package com.example.userlistactivity.matey.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.userlistactivity.matey.model.ChorePriority

@Entity(tableName = "chores")
data class Chore(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val assignedTo: String,
    val isDone: Boolean = false,
    val priority: ChorePriority = ChorePriority.MEDIUM
)
