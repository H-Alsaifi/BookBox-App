package ca.dal.cs.csci4176.group01undergraduate.api
//importing the needed libraries
import ca.dal.cs.csci4176.group01undergraduate.response.BooksApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

//defining the api service to make the other functions functional
interface BooksApiService {
    //a method to search the books by its ISBN
    @GET("volumes")
    fun searchBookByISBN(@Query("q") isbnQuery: String): Call<BooksApiResponse>
}