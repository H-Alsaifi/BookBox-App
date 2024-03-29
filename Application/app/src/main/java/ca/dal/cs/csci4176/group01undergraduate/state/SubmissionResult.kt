package ca.dal.cs.csci4176.group01undergraduate.state

// Sealed class representing the result of a submission operation
sealed class SubmissionResult {
    // Success subclass representing successful submission with an associated message
    class Success(val message: String) : SubmissionResult()

    // Error subclass representing failed submission with an associated error message
    class Error(val error: String) : SubmissionResult()
}
