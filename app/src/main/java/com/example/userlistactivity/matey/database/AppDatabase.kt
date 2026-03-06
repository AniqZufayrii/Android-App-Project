package com.example.userlistactivity.matey.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Friend::class, Chore::class, Bill::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // DAOs will be defined here
    abstract fun friendDao(): FriendDao
    abstract fun choreDao(): ChoreDao
    abstract fun billDao(): BillDao

    companion object {
        // The @Volatile annotation ensures that the INSTANCE variable is always up-to-date
        // and visible to all execution threads. Changes made by one thread are immediately
        // visible to all other threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // The synchronized block ensures that only one thread can execute this code at a time,
            // preventing multiple instances of the database from being created.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // This is the name of the database file on the device
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                // return the instance
                instance
            }
        }
    }
}
