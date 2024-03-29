package ca.dal.cs.csci4176.group01undergraduate.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ca.dal.cs.csci4176.group01undergraduate.viewmodel.SupportViewModel

// Factory class responsible for creating instances of SupportViewModel
class SupportViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested class is SupportViewModel
        if (modelClass.isAssignableFrom(SupportViewModel::class.java)) {
            // If it is, create and return a new instance of SupportViewModel
            @Suppress("UNCHECKED_CAST")
            return SupportViewModel() as T
        }
        // If the requested class is not SupportViewModel, throw an IllegalArgumentException
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
