package ca.dal.cs.csci4176.group01undergraduate.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.dal.cs.csci4176.group01undergraduate.intent.SupportIntent
import ca.dal.cs.csci4176.group01undergraduate.state.SubmissionResult
import ca.dal.cs.csci4176.group01undergraduate.state.SupportState
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

// ViewModel responsible for managing data related to technical support feature

class SupportViewModel : ViewModel() {
    // LiveData to hold the current state of the support feature
    private val _state = MutableLiveData<SupportState?>()
    val state: LiveData<SupportState?> = _state

    // LiveData to hold the result of a submission operation
    private val _submissionResult = MutableLiveData<SubmissionResult?>()
    val submissionResult: LiveData<SubmissionResult?> = _submissionResult

    // Process intents to trigger state changes or actions in the ViewModel
    fun processIntents(intent: SupportIntent) {
        when (intent) {
            is SupportIntent.ShowContactForm -> {
                // Reset the state to allow showing the contact form again
                _state.value = SupportState.ShowContactFormState
                // Reset the submission result to clear previous results
                _submissionResult.value = null
            }
        }
    }

    // Submit a support request with the provided details
    fun submitSupportRequest(name: String, email: String, message: String) {
        // Set the state to indicate submission loading
        _state.value = SupportState.SubmissionLoading

        viewModelScope.launch {
            val ref = FirebaseDatabase.getInstance().getReference("supportRequests")
            val requestId = ref.push().key

            val requestMap = hashMapOf(
                "name" to name,
                "email" to email,
                "message" to message
            )

            requestId?.let {
                ref.child(it).setValue(requestMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _submissionResult.postValue(SubmissionResult.Success("Support request submitted successfully."))
                        _state.postValue(SupportState.SubmissionSuccess("Submission successful!"))
                    } else {
                        _submissionResult.postValue(SubmissionResult.Error("Failed to submit support request: ${task.exception?.message}"))
                        _state.postValue(
                            SupportState.SubmissionError(
                                task.exception?.message ?: "Unknown error"
                            )
                        )
                    }
                }
            } ?: run {
                _submissionResult.postValue(SubmissionResult.Error("Failed to generate a unique ID for the support request."))
                _state.postValue(SupportState.SubmissionError("Failed to generate a unique ID for the support request."))
            }
        }
    }

    // Add a method to reset the ViewModel to its initial state
    fun resetSubmissionState() {
        _submissionResult.value = null // Clear previous submission result
        _state.value = SupportState.ShowContactFormState // Set state to show the contact form again
    }
}