package ca.dal.cs.csci4176.group01undergraduate.addingbookbox.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.models.AddBookBoxModel

class AddBookBoxViewModelFactory(private val addBookBoxModel: AddBookBoxModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddBookBoxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddBookBoxViewModel(addBookBoxModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

