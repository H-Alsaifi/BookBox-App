package ca.dal.cs.csci4176.group01undergraduate.technicalSupport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SupportViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SupportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SupportViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
