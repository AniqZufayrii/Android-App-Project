package com.example.userlistactivity.matey.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(bill: Bill)

    @Update
    suspend fun update(bill: Bill)

    @Query("SELECT * FROM bills ORDER BY id ASC")
    fun getAllBills(): Flow<List<Bill>>

    @Delete
    suspend fun delete(bill: Bill)
}
