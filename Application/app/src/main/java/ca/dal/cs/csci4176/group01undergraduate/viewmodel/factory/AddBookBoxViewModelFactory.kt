package ca.dal.cs.csci4176.group01undergraduate.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ca.dal.cs.csci4176.group01undergraduate.repository.BookBoxRepository
import ca.dal.cs.csci4176.group01undergraduate.viewmodel.BookBoxViewModel

/**
 * A factory class for creating instances of [BookBoxViewModel].
 * This factory is used to inject dependencies into the ViewModel, such as the repository for accessing data,
 * and a function to check if location permissions have been granted.
 *
 * @property repository An instance of [BookBoxRepository] to be passed into ViewModel instances for data operations.
 * @property hasLocationPermission A lambda function that returns a [Boolean] indicating whether location permissions are granted.
 */
class BookBoxViewModelFactory(
    private val repository: BookBoxRepository,
    private val hasLocationPermission: () -> Boolean
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given [ViewModel] class, if supported.
     *
     * @param modelClass A [Class] object indicating the ViewModel class to instantiate.
     * @return A newly created ViewModel.
     * @throws IllegalArgumentException if the given ViewModel class is not supported by this factory.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BookBoxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BookBoxViewModel(repository, hasLocationPermission) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
