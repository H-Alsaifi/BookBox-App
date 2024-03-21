package ca.dal.cs.csci4176.group01undergraduate.addingbookbox.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.models.AddBookBoxModel
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.intents.AddBookBoxIntent
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.states.AddBookBoxState
import kotlinx.coroutines.launch

class AddBookBoxViewModel(private val addBookBoxModel: AddBookBoxModel) : ViewModel() {

    // Make state private and expose it as LiveData
    private val _state = MutableLiveData<AddBookBoxState>()
    val state: LiveData<AddBookBoxState> get() = _state

    init {
        // Initialize with a default state
        _state.value = AddBookBoxState()
    }

    fun processIntent(intent: AddBookBoxIntent) {
        when (intent) {
            is AddBookBoxIntent.OpenAddBookBox -> {
                // Handle open add box view
            }
            is AddBookBoxIntent.SubmitDetails -> {
                viewModelScope.launch {
                    _state.value = _state.value?.copy(isLoading = true)
                    try {
                        val documentIdResult = addBookBoxModel.submitDetails(intent.name, intent.location, intent.description, intent.pictureUri)
                        documentIdResult.fold(
                            onSuccess = { documentId ->
                                // Update the state with the new document ID
                                _state.value = _state.value?.copy(
                                    isLoading = false,
                                    isSuccessful = true,
                                    documentId = documentId
                                )
                            },
                            onFailure = { throwable ->
                                // Check if the throwable is an exception and cast it
                                val exception = throwable as? Exception ?: Exception(throwable)
                                // Update the state with the error
                                _state.value = _state.value?.copy(
                                    isLoading = false,
                                    error = exception
                                )
                            }

                        )
                    } catch (e: Exception) {
                        // Update the state with the error
                        _state.value = _state.value?.copy(
                            isLoading = false,
                            error = e
                        )
                    }
                }
            }
            is AddBookBoxIntent.UploadPicture -> {
                viewModelScope.launch {
                    _state.value = _state.value?.copy(isLoading = true)
                    try {
                        val uri = addBookBoxModel.uploadPicture(intent.pictureUri)
                        _state.value = _state.value?.copy(isLoading = false, imageUrl = uri.toString())
                    } catch (exception: Exception) {
                        _state.value = _state.value?.copy(isLoading = false, error = exception)
                    }
                }
            }
            is AddBookBoxIntent.ConfirmAddition -> {
                // Handle confirmation
                _state.value = _state.value?.copy(isLoading = false, isSuccessful = true)
            }
        }
    }
}
