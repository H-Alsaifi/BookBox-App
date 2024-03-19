package ca.dal.cs.csci4176.group01undergraduate.addBookISBN

sealed class BookState {
    object Idle : BookState()
    object Loading : BookState()
    data class Success(val book: Book) : BookState()
    data class Error(val error: String) : BookState()
}