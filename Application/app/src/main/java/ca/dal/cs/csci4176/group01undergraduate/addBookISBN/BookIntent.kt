package ca.dal.cs.csci4176.group01undergraduate.addBookISBN

sealed class BookIntent {
    data class SearchBookByISBN(val isbn: String) : BookIntent()
}