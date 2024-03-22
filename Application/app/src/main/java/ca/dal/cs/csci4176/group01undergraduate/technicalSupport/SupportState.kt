package ca.dal.cs.csci4176.group01undergraduate.technicalSupport

sealed class SupportState {
    object ShowContactFormState : SupportState()
    object SubmissionLoading : SupportState()
    class SubmissionSuccess(val message: String) : SupportState()
    class SubmissionError(val error: String) : SupportState()
}