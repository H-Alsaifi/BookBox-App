package ca.dal.cs.csci4176.group01undergraduate.technicalSupport

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SupportViewModel : ViewModel() {
    private val _state = MutableLiveData<SupportState>()
    val state: LiveData<SupportState> = _state

    private val _submissionResult = MutableLiveData<SubmissionResult>()
    val submissionResult: LiveData<SubmissionResult> = _submissionResult

    fun processIntents(intent: SupportIntent) {
        when (intent) {
            is SupportIntent.ShowContactForm -> {
                _state.value = SupportState.ShowContactFormState
            }
            // Handle other intents
        }
    }

    fun submitSupportRequest(name: String, email: String, message: String) {
        _state.value = SupportState.SubmissionLoading

        viewModelScope.launch {
            val ref = FirebaseDatabase.getInstance().getReference("supportRequests")
            val requestId = ref.push().key // Generate a unique ID for the request

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
                        _state.postValue(SupportState.SubmissionError(task.exception?.message ?: "Unknown error"))
                    }
                }
            } ?: run {
                _submissionResult.postValue(SubmissionResult.Error("Failed to generate a unique ID for the support request."))
                _state.postValue(SupportState.SubmissionError("Failed to generate a unique ID for the support request."))
            }
        }
    }
}