package com.example.userlistactivity.matey.database

import androidx.room.TypeConverter
import com.example.userlistactivity.matey.model.BillCategory
import com.example.userlistactivity.matey.model.ChorePriority

class Converters {
    @TypeConverter
    fun fromBillCategory(value: BillCategory): String {
        return value.name
    }

    @TypeConverter
    fun toBillCategory(value: String): BillCategory {
        return BillCategory.valueOf(value)
    }

    @TypeConverter
    fun fromChorePriority(value: ChorePriority): String {
        return value.name
    }

    @TypeConverter
    fun toChorePriority(value: String): ChorePriority {
        return ChorePriority.valueOf(value)
    }
}