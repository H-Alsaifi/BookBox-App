package ca.dal.cs.csci4176.group01undergraduate.state

// Sealed class representing the different states of the technical support feature
sealed class SupportState {
    // Object representing the state where the contact form should be shown
    object ShowContactFormState : SupportState()

    // Object representing the state where a submission is in progress
    object SubmissionLoading : SupportState()

    // Class representing the state where a submission is successful, with an associated message
    class SubmissionSuccess(val message: String) : SupportState()

    // Class representing the state where a submission encountered an error, with an associated error message
    class SubmissionError(val error: String) : SupportState()
}
