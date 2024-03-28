package ca.dal.cs.csci4176.group01undergraduate.addBookISBN

import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BookRepository {

    fun searchBookByISBN(isbn: String): MutableLiveData<BookState> {
        val liveData = MutableLiveData<BookState>(BookState.Loading)

        val call = RetrofitInstance.api.searchBookByISBN("isbn:$isbn")
        call.enqueue(object : Callback<BooksApiResponse> {
            override fun onResponse(call: Call<BooksApiResponse>, response: Response<BooksApiResponse>) {
                if (response.isSuccessful && response.body()?.items != null) {
                    val books = response.body()!!.items!!
                    if (books.isNotEmpty()) {
                        val book = books[0].volumeInfo
                        val authors = book.authors?.joinToString(", ") ?: "N/A"
                        val title = book.title ?: "N/A"
                        val description = book.description ?: "No description available."
                        val isbn = book.industryIdentifiers?.find { it.type == "ISBN_13" }?.identifier ?: "N/A"
                        val bookObj = Book(title, authors, isbn, description, 0, "", "") // Assuming rating is 0 for simplicity
                        liveData.postValue(BookState.Success(bookObj))
                        // TODO: Add book to Firebase here if required.
                    } else {
                        liveData.postValue(BookState.Error("No books found with that ISBN."))
                    }
                } else {
                    liveData.postValue(BookState.Error("Failed to fetch book data."))
                }
            }

            override fun onFailure(call: Call<BooksApiResponse>, t: Throwable) {
                liveData.postValue(BookState.Error("Network error: ${t.message}"))
            }
        })

        return liveData
    }
}