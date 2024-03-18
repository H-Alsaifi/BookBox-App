package ca.dal.cs.csci4176.group01undergraduate.addBookISBN

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.FirebaseDatabase


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

    fun addBookToFirebase(book: Book, result: (Boolean, String?) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Books")
        val bookId = databaseReference.push().key

        bookId?.let {
            databaseReference.child(it).setValue(book).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    result(true, null)
                } else {
                    result(false, task.exception?.message ?: "Unknown error")
                }
            }
        } ?: run {
            result(false, "Failed to generate a unique key for the book")
        }
    }

}