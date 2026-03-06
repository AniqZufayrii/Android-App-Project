package com.example.userlistactivity.matey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.userlistactivity.matey.database.AppRepository
import com.example.userlistactivity.matey.database.Bill
import com.example.userlistactivity.matey.database.Chore
import com.example.userlistactivity.matey.database.Friend
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    val allBills = repository.allBills.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allChores = repository.allChores.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allFriends = repository.allFriends.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun insertBill(bill: Bill) = viewModelScope.launch {
        repository.insertBill(bill)
    }

    fun updateBill(bill: Bill) = viewModelScope.launch {
        repository.updateBill(bill)
    }

    fun deleteBill(bill: Bill) = viewModelScope.launch {
        repository.deleteBill(bill)
    }

    fun insertChore(chore: Chore) = viewModelScope.launch {
        repository.insertChore(chore)
    }

    fun updateChore(chore: Chore) = viewModelScope.launch {
        repository.updateChore(chore)
    }

    fun deleteChore(chore: Chore) = viewModelScope.launch {
        repository.deleteChore(chore)
    }

    fun insertFriend(friend: Friend) = viewModelScope.launch {
        repository.insertFriend(friend)
    }

    fun deleteFriend(friend: Friend) = viewModelScope.launch {
        repository.deleteFriend(friend)
    }
}

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
