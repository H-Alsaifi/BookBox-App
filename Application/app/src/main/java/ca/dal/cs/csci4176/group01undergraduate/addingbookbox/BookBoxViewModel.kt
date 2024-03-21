package ca.dal.cs.csci4176.group01undergraduate.addingbookbox

import android.Manifest
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.BookBoxIntent
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.BookBoxRepository
import ca.dal.cs.csci4176.group01undergraduate.addingbookbox.states.BookBoxState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import android.net.Uri

class BookBoxViewModel(private val repository: BookBoxRepository, private val hasLocationPermission: () -> Boolean) : ViewModel() {

    private val intentsChannel = Channel<BookBoxIntent>(Channel.UNLIMITED)

    private val _state = MutableStateFlow(BookBoxState())
    val state: StateFlow<BookBoxState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            intentsChannel.consumeAsFlow().collect { intent ->
                when (intent) {
                    is BookBoxIntent.Load -> loadInitialState()
                    is BookBoxIntent.SubmitDetails -> submitDetails(intent)
                    is BookBoxIntent.UploadPicture -> uploadPicture(intent.pictureUri)
                    is BookBoxIntent.FetchCurrentLocation -> fetchCurrentLocation()
                }
            }
        }
    }

    fun offerIntent(intent: BookBoxIntent) {
        when (intent) {
            is BookBoxIntent.Load -> loadInitialState()
            is BookBoxIntent.SubmitDetails -> submitDetails(intent)
            is BookBoxIntent.UploadPicture -> uploadPicture(intent.pictureUri)
            is BookBoxIntent.FetchCurrentLocation -> fetchCurrentLocation()
            // If there are no other cases, you don't need the else
            else -> throw IllegalStateException("Unsupported intent type: $intent")
        }
    }


    private fun loadInitialState() {
        // Potentially load any initial data or settings
    }

    private fun submitDetails(intent: BookBoxIntent.SubmitDetails) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = repository.submitDetails(intent.name, intent.description, Uri.parse(intent.pictureUri))
            // Use isSuccessful and getOrElse to properly handle the Result
            _state.value = if (result.isSuccess) {
                _state.value.copy(isLoading = false, documentId = result.getOrNull(), error = null)
            } else {
                _state.value.copy(isLoading = false, error = result.exceptionOrNull() as? Exception)
            }
        }
    }

    private fun uploadPicture(pictureUri: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = repository.uploadPicture(Uri.parse(pictureUri)) // Ensure this is public/internal in the repository
            _state.value = if (result.isSuccess) {
                _state.value.copy(isLoading = false, imageUrl = result.getOrNull()?.toString(), error = null)
            } else {
                _state.value.copy(isLoading = false, error = result.exceptionOrNull() as? Exception)
            }
        }
    }


    private fun fetchCurrentLocation() {
        viewModelScope.launch {
            if (hasLocationPermission()) {
                _state.value = _state.value.copy(isLoading = true)
                val result = repository.getCurrentLocation()
                _state.value = when {
                    result.isSuccess -> {
                        _state.value.copy(isLoading = false, location = result.getOrNull(), error = null)
                    }
                    else -> {
                        _state.value.copy(isLoading = false, error = result.exceptionOrNull() as? Exception)
                    }
                }
            } else {
                _state.value = _state.value.copy(error = Exception("Location permission not granted"))
            }
        }
    }

}

