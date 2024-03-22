package ca.dal.cs.csci4176.group01undergraduate.technicalSupport

sealed class SubmissionResult {
    class Success(val message: String) : SubmissionResult()
    class Error(val error: String) : SubmissionResult()
}