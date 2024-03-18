package ca.dal.cs.csci4176.group01undergraduate.addBookISBN

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class BookViewModel(private val repository: BookRepository) : ViewModel() {
    private val _state = MutableLiveData<BookState>()
    val state: LiveData<BookState> = _state

    init {
        _state.value = BookState.Idle
    }

    fun processIntent(intent: BookIntent) {
        when (intent) {
            is BookIntent.SearchBookByISBN -> searchBookByISBN(intent.isbn)
        }
    }

    private fun searchBookByISBN(isbn: String) {
        val liveData = repository.searchBookByISBN(isbn)
        liveData.observeForever { bookState ->
            _state.value = bookState
        }
    }
}