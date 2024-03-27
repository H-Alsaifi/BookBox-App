package ca.dal.cs.csci4176.group01undergraduate.technicalSupport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ViewModel responsible for managing data related to technical support feature
class SupportViewModel : ViewModel() {
    // LiveData to hold the current state of the support feature
    private val _state = MutableLiveData<SupportState>()
    val state: LiveData<SupportState> = _state

    // LiveData to hold the result of a submission operation
    private val _submissionResult = MutableLiveData<SubmissionResult>()
    val submissionResult: LiveData<SubmissionResult> = _submissionResult

    // Process intents to trigger state changes or actions in the ViewModel
    fun processIntents(intent: SupportIntent) {
        when (intent) {
            // Intent to show the contact form
            is SupportIntent.ShowContactForm -> {
                _state.value = SupportState.ShowContactFormState
            }
            // Add handlers for other intents if needed in the future
        }
    }

    // Submit a support request with the provided details
    fun submitSupportRequest(name: String, email: String, message: String) {
        // Set the state to indicate submission loading
        _state.value = SupportState.SubmissionLoading

        // Use coroutines for asynchronous tasks
        viewModelScope.launch {
            // Get reference to Firebase Realtime Database
            val ref = FirebaseDatabase.getInstance().getReference("supportRequests")
            // Generate a unique ID for the request
            val requestId = ref.push().key

            // Create a map of request details
            val requestMap = hashMapOf(
                "name" to name,
                "email" to email,
                "message" to message
            )

            // Check if request ID generation was successful
            requestId?.let {
                // Push the request data to the database
                ref.child(it).setValue(requestMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Post success result and update state
                        _submissionResult.postValue(SubmissionResult.Success("Support request submitted successfully."))
                        _state.postValue(SupportState.SubmissionSuccess("Submission successful!"))
                    } else {
                        // Post error result and update state
                        _submissionResult.postValue(SubmissionResult.Error("Failed to submit support request: ${task.exception?.message}"))
                        _state.postValue(SupportState.SubmissionError(task.exception?.message ?: "Unknown error"))
                    }
                }
            } ?: run {
                // Post error result and update state if request ID generation failed
                _submissionResult.postValue(SubmissionResult.Error("Failed to generate a unique ID for the support request."))
                _state.postValue(SupportState.SubmissionError("Failed to generate a unique ID for the support request."))
            }
        }
    }
}