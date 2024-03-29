package ca.dal.cs.csci4176.group01undergraduate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.dal.cs.csci4176.group01undergraduate.repository.BookBoxRepository
import ca.dal.cs.csci4176.group01undergraduate.state.BookBoxState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import android.net.Uri
import android.util.Log
import ca.dal.cs.csci4176.group01undergraduate.intent.BookBoxIntent

// BookBoxViewModel is responsible for handling UI-related data and logic for the BookBox feature.
// It communicates with a repository to perform data operations and updates its state accordingly.
class BookBoxViewModel(private val repository: BookBoxRepository, private val hasLocationPermission: () -> Boolean) : ViewModel() {

    // Channel for handling UI intents asynchronously.
    private val intentsChannel = Channel<BookBoxIntent>(Channel.UNLIMITED)

    // Internal MutableStateFlow for handling the view state, private to avoid exposing mutable state.
    private val _state = MutableStateFlow(BookBoxState())
    // Public version of the state, exposed as a read-only StateFlow.
    val state: StateFlow<BookBoxState> = _state.asStateFlow()

    // Initializer block, setting up a coroutine to listen to and process intents.
    init {
        viewModelScope.launch {
            intentsChannel.consumeAsFlow().collect { intent ->
                // When a new intent is received, handle it according to its type.
                when (intent) {
                    is BookBoxIntent.Load -> loadInitialState()
                    is BookBoxIntent.SubmitDetails -> submitDetails(intent)
                    is BookBoxIntent.UploadPicture -> uploadPicture(intent.pictureUri)
                    is BookBoxIntent.FetchCurrentLocation -> fetchCurrentLocation()
                }
            }
        }
    }

    // Method for external entities to offer intents to be processed by the ViewModel.
    fun offerIntent(intent: BookBoxIntent) {
        // Directly handle the intent without using the intentsChannel for immediate actions.
        when (intent) {
            is BookBoxIntent.Load -> loadInitialState()
            is BookBoxIntent.SubmitDetails -> submitDetails(intent)
            is BookBoxIntent.UploadPicture -> uploadPicture(intent.pictureUri)
            is BookBoxIntent.FetchCurrentLocation -> fetchCurrentLocation()
            else -> throw IllegalStateException("Unsupported intent type: $intent")
        }
    }

    // Loads any initial data or settings required by the ViewModel.
    private fun loadInitialState() {
        // Implementation can include fetching initial data, setting up defaults, etc.
    }

    // Handles the submission of details by the user.
    private fun submitDetails(intent: BookBoxIntent.SubmitDetails) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // Fetch location as part of the submission process.
            val locationResult = repository.getCurrentLocation()
            if (locationResult.isSuccess) {
                val location = locationResult.getOrNull()
                location?.let {
                    val result = repository.submitDetails(Uri.parse(intent.pictureUri), it, intent.description)
                    // Process the result of the submission.
                    _state.value = if (result.isSuccess) {
                        Log.d("ViewModel", "SubmitDetails was successful.")
                        _state.value.copy(isLoading = false, documentId = result.getOrNull(), isSuccessful = true, error = null)
                    } else {
                        Log.d("ViewModel", "SubmitDetails failed with error: ${result.exceptionOrNull()?.message}")
                        _state.value.copy(isLoading = false, error = result.exceptionOrNull() as? Exception)
                    }
                } ?: run {
                    // Handle the scenario where the location is null.
                    _state.value = _state.value.copy(isLoading = false, error = Exception("Location is null"))
                }
            } else {
                // Handle errors in fetching the location.
                _state.value = _state.value.copy(isLoading = false, error = Exception("Failed to fetch location"))
            }
        }
    }

    // Handles uploading a picture.
    private fun uploadPicture(pictureUri: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = repository.uploadPicture(Uri.parse(pictureUri))
            // Update state based on the result of the upload operation.
            _state.value = if (result.isSuccess) {
                _state.value.copy(isLoading = false, imageUrl = result.getOrNull()?.toString(), error = null)
            } else {
                _state.value.copy(isLoading = false, error = result.exceptionOrNull() as? Exception)
            }
        }
    }

    // Fetches the current location if permissions are granted.
    private fun fetchCurrentLocation() {
        viewModelScope.launch {
            if (hasLocationPermission()) {
                _state.value = _state.value.copy(isLoading = true)
                val result = repository.getCurrentLocation()
                // Update state based on the result of fetching the location.
                _state.value = when {
                    result.isSuccess -> {
                        _state.value.copy(isLoading = false, location = result.getOrNull(), error = null)
                    }
                    else -> {
                        _state.value.copy(isLoading = false, error = result.exceptionOrNull() as? Exception)
                    }
                }
            } else {
                // Handle lack of location permission.
                _state.value = _state.value.copy(error = Exception("Location permission not granted"))
            }
        }
    }
}

