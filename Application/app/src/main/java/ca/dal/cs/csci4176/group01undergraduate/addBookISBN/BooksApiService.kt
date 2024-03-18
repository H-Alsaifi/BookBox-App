package ca.dal.cs.csci4176.group01undergraduate.addBookISBN

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BooksApiService {
    @GET("volumes")
    fun searchBookByISBN(@Query("q") isbnQuery: String): Call<BooksApiResponse>
}