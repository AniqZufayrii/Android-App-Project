package com.example.userlistactivity.matey.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.userlistactivity.matey.database.AppRepository
import com.example.userlistactivity.matey.database.Friend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel to bridge the UI and the data layer (Repository).
 * It provides data to the UI and survives configuration changes.
 */
class FriendViewModel(private val repository: AppRepository) : ViewModel() {

    // Use the Flow from the repository to get all friends.
    // The UI will collect this Flow and automatically update when the data changes.
    val allFriends: Flow<List<Friend>> = repository.allFriends

    /**
     * Launch a new coroutine to insert a friend in a non-blocking way.
     */
    fun insertFriend(friendName: String) = viewModelScope.launch {
        // We only need to pass the name, as the ID is auto-generated.
        val friend = Friend(name = friendName)
        repository.insertFriend(friend)
    }

    // You would add functions for Chores and Bills here as well, for example:
    // fun getChoresForFriend(friendId: Int) = repository.getChoresForFriend(friendId)
    // fun insertChore(chore: Chore) = viewModelScope.launch { repository.insertChore(chore) }
}

/**
 * A Factory class is needed to create a ViewModel instance with a non-empty constructor.
 * It allows us to pass the AppRepository into our FriendViewModel.
 */
class FriendViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the modelClass is of our FriendViewModel type
        if (modelClass.isAssignableFrom(FriendViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FriendViewModel(repository) as T
        }
        // If not, throw an exception
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}