package com.example.userlistactivity.matey.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {

    // Inserts a friend into the 'friends' table. If the friend already exists, it ignores the new one.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(friend: Friend)

    // Selects all friends from the 'friends' table and orders them by ID.
    // Flow allows the UI to automatically update when the data changes.
    @Query("SELECT * FROM friends ORDER BY id ASC")
    fun getAllFriends(): Flow<List<Friend>>

    @Delete
    suspend fun delete(friend: Friend)
}