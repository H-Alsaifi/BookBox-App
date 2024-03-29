package ca.dal.cs.csci4176.group01undergraduate.api
//importing the needed libraries
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//an object to provide a retrofit to access the Google Books API
object RetrofitInstance {
    //initialising a variables with the base url of the API
    private const val BASE_URL = "https://www.googleapis.com/books/v1/"
    //initialising a lazy for retrofit instance
    val api: BooksApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            //adding the json conversion
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BooksApiService::class.java)
    }
}