package ca.dal.cs.csci4176.group01undergraduate.repository

import androidx.lifecycle.MutableLiveData
import ca.dal.cs.csci4176.group01undergraduate.state.BookState
import ca.dal.cs.csci4176.group01undergraduate.response.BooksApiResponse
import ca.dal.cs.csci4176.group01undergraduate.api.RetrofitInstance
import ca.dal.cs.csci4176.group01undergraduate.model.Book
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
                        val rating = book.averageRating ?: 0.0
                        val totalRatings = book.ratingsCount ?: 0
                        val bookObj =
                            Book(title, authors, isbn, description, rating, totalRatings, "", "") // Use the actual rating
                        liveData.postValue(BookState.Success(bookObj))
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