package com.example.userlistactivity.matey.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.userlistactivity.matey.model.BillCategory

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val date: String,
    val category: BillCategory = BillCategory.OTHER,
    val splitWith: String = "", // Added to store names of friends split with
    val paidFriends: String = "", // Comma-separated list of friends who have paid
    val isSettled: Boolean = false, // New field to archive bills instead of deleting
    val includeMe: Boolean = true // Whether the payer is included in the split
)
