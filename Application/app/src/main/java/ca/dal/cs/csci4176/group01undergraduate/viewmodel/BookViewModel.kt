package ca.dal.cs.csci4176.group01undergraduate.viewmodel
//importing the needed libraries
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.location.Geocoder
import ca.dal.cs.csci4176.group01undergraduate.intent.BookIntent
import ca.dal.cs.csci4176.group01undergraduate.model.Book
import ca.dal.cs.csci4176.group01undergraduate.repository.BookRepository
import ca.dal.cs.csci4176.group01undergraduate.state.BookState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.util.Locale

/**
 * a method to manage book data
 * @param repository getting the repositories of the book
 */
class BookViewModel(private val repository: BookRepository) : ViewModel() {
    //initialising variables to get the state of the book
    private val _state = MutableLiveData<BookState>()
    val state: LiveData<BookState> = _state
    //initialising the state of the book and putting it in idle mode
    init {
        _state.value = BookState.Idle
    }
    //getting the intent of the book and actions related to it
    fun processIntent(intent: BookIntent) {
        when (intent) {
            is BookIntent.SearchBookByISBN -> searchBookByISBN(intent.isbn)
        }
    }
    //searching book by their ISBN
    private fun searchBookByISBN(isbn: String) {
        val liveData = repository.searchBookByISBN(isbn)
        liveData.observeForever { bookState ->
            _state.value = bookState
        }
    }
    //a method to add new books to firebase from the user
    fun addBookToFirebase(context: Context, book: Book, bookBoxKey: String?, result: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            //initializing variables to reference the database, book id, and user id taking each book
            val databaseReference = FirebaseDatabase.getInstance().getReference("Books")
            val bookId = databaseReference.push().key
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            //
            bookId?.let { bid ->
                //check if the book box is null
                if (bookBoxKey != null) {
                    //if it is not then set the book box id to the key
                    book.bookBoxID = bookBoxKey
                    //get reference of the book box in the firebase
                    val bookBoxRef = FirebaseDatabase.getInstance().getReference("bookBoxes").child(bookBoxKey)
                    try {
                        //trying to fetch the book box details from the database
                        val dataSnapshot = bookBoxRef.get().await()
                        //getting the longitude and latitude from the data fetched
                        val latitude = dataSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude = dataSnapshot.child("longitude").getValue(Double::class.java)
                        //check if the inputs are null or not
                        if (latitude != null && longitude != null) {
                            val address = withContext(Dispatchers.IO) {
                                convertCoordinatesToAddress(context, latitude, longitude)
                            }
                            //check if the address if not null
                            if (address != null) {
                                //if so then set the book address to the given address
                                book.address = address
                            }
                        }
                        //set an exception when failing to getting information of the book
                    } catch (e: Exception) {
                        result(false, "Failed to fetch book box details or convert coordinates.")
                        return@launch
                    }
                }
                // Proceed to save the book to Firebase, now including the address if available
                try {
                    databaseReference.child(bid).setValue(book).await()
                    //update the user points after returning the book
                    updateUserPoints(userId, result)
                    //if the book could not be added send an exception
                } catch (e: Exception) {
                    result(false, "Failed to add book to Firebase.")
                }
                //if there could not be a unique id for the book then send an exception
            } ?: run {
                result(false, "Failed to generate a unique key for the book.")
            }
        }
    }
    suspend fun convertCoordinatesToAddress(context: Context, latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses!!.isNotEmpty()) {
                val address = addresses[0]
                // Construct a single string from the address' components
                val addressFragments = with(address) {
                    (0..maxAddressLineIndex).map { getAddressLine(it) }
                }
                addressFragments.joinToString(separator = "\n")
            } else {
                "No address found"
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    /**
     * method to update the points of the user as they take and retrun a book
     * @param userId the unique id of each user
     * @param result the number of points a user got
     */
    private fun updateUserPoints(userId: String?, result: (Boolean, String?) -> Unit) {
        //checking if the user id is null
        if (userId != null) {
            //setting a reference to the user in the database
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            //referring to the realtime database to get the points
            userRef.child("points").get().addOnSuccessListener { dataSnapshot ->
                //variables to the current points of the user
                val currentPoints = dataSnapshot.getValue(Int::class.java) ?: 0
                //variable to the new points of the user and adding to it one
                val newPoints = currentPoints + 1  // Increment points by 1 for each book added
                userRef.child("points").setValue(newPoints).addOnCompleteListener { userTask ->
                    //check if user returned the book then add points
                    if (userTask.isSuccessful) {
                        result(true, null)
                        //else send an exception to the user
                    } else {
                        result(false, userTask.exception?.message ?: "Failed to update user points")
                    }
                }
                //if the points could not be fetched then send an exception
            }.addOnFailureListener {
                result(false, "Failed to fetch current user points")
            }
            //send an exception that user haven't logged in
        } else {
            result(false, "User is not logged in")
        }
    }
    /**
     * getting the live data of each of the books
     * @param bookId the unique id of each book
     */
    fun getBookLiveData(bookId: String): LiveData<Book> {
        //initializing variable for each live data of the book
        val liveData = MutableLiveData<Book>()
        //getting the reference of the book
        val bookRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId)

        //an addvaluelistener to get the changes that are going in the database
        bookRef.addValueEventListener(object : ValueEventListener {
            //a method called when data in the database is changed
            override fun onDataChange(snapshot: DataSnapshot) {
                //retrieving the book object class from the database
                val book = snapshot.getValue(Book::class.java)
                //if the book is not null then set a livedata for it
                book?.let {
                    liveData.value = it
                }
            }
            //a method called in case the listener was cancelled
            override fun onCancelled(error: DatabaseError) {
                // Log error
            }
        })
        //return the liveData with the book data
        return liveData
    }
}