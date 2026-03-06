package com.example.userlistactivity.matey.database

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val friendDao: FriendDao,
    private val choreDao: ChoreDao,
    private val billDao: BillDao
) {

    // --- Friend Functions ---

    // Using Flow to get all friends. The UI will update automatically on data change.
    val allFriends: Flow<List<Friend>> = friendDao.getAllFriends()

    // suspend function to insert a friend, handled in a coroutine.
    suspend fun insertFriend(friend: Friend) {
        friendDao.insert(friend)
    }

    suspend fun deleteFriend(friend: Friend) {
        friendDao.delete(friend)
    }

    // --- Chore Functions ---

    val allChores: Flow<List<Chore>> = choreDao.getAllChores()

    suspend fun insertChore(chore: Chore) {
        choreDao.insert(chore)
    }

    suspend fun updateChore(chore: Chore) {
        choreDao.update(chore)
    }

    suspend fun deleteChore(chore: Chore) {
        choreDao.delete(chore)
    }


    // --- Bill Functions ---

    val allBills: Flow<List<Bill>> = billDao.getAllBills()

    suspend fun insertBill(bill: Bill) {
        billDao.insert(bill)
    }

    suspend fun updateBill(bill: Bill) {
        billDao.update(bill)
    }

    suspend fun deleteBill(bill: Bill) {
        billDao.delete(bill)
    }
}
