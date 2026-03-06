package com.example.userlistactivity.matey.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChoreDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(chore: Chore)

    @Query("SELECT * FROM chores ORDER BY id ASC")
    fun getAllChores(): Flow<List<Chore>>

    @Update
    suspend fun update(chore: Chore)

    @Delete
    suspend fun delete(chore: Chore)
}