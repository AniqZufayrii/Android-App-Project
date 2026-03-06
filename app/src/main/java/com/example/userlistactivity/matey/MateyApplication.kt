package com.example.userlistactivity.matey

import android.app.Application
import com.example.userlistactivity.matey.database.AppDatabase
import com.example.userlistactivity.matey.database.AppRepository

/**
 * The Application class. This is the first component of the app to be instantiated.
 * We use it to create a single, app-wide instance of our database and repository.
 */
class MateyApplication : Application() {
    // Using 'lazy' ensures the database and repository are only created when they're first needed.
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        AppRepository(
            database.friendDao(),
            database.choreDao(),
            database.billDao()
        )
    }
}