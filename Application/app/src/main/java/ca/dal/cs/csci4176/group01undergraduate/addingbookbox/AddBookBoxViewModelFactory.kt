package ca.dal.cs.csci4176.group01undergraduate.addingbookbox.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.BookBoxRepository
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.BookBoxViewModel

class BookBoxViewModelFactory(
    private val repository: BookBoxRepository,
    private val hasLocationPermission: () -> Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookBoxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookBoxViewModel(repository, hasLocationPermission) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

