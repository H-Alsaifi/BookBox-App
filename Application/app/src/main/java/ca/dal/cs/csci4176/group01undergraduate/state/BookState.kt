package ca.dal.cs.csci4176.group01undergraduate.state

import ca.dal.cs.csci4176.group01undergraduate.model.Book

sealed class BookState {
    data object Idle : BookState()
    data object Loading : BookState()
    data class Success(val book: Book) : BookState()
    data class Error(val error: String) : BookState()
}