package ca.dal.cs.csci4176.group01undergraduate.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ca.dal.cs.csci4176.group01undergraduate.navigation.NavigationReducer
import ca.dal.cs.csci4176.group01undergraduate.viewmodel.NavigationViewModel

// Factory class for creating instances of NavigationViewModel
class NavigationViewModelFactory(private val reducer: NavigationReducer): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NavigationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NavigationViewModel(reducer) as T
        }
        throw java.lang.IllegalArgumentException("Unknown ViewModel class")
    }
}