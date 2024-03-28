package ca.dal.cs.csci4176.group01undergraduate.addBookISBN

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.location.Geocoder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.io.IOException
import java.util.Locale


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

//    fun addBookToFirebase(book: Book, result: (Boolean, String?) -> Unit) {
//        val databaseReference = FirebaseDatabase.getInstance().getReference("Books")
//        val bookId = databaseReference.push().key
//
//        bookId?.let {
//            databaseReference.child(it).setValue(book).addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    result(true, null)
//                } else {
//                    result(false, task.exception?.message ?: "Unknown error")
//                }
//            }
//        } ?: run {
//            result(false, "Failed to generate a unique key for the book")
//        }
//    }

//fun addBookToFirebase(book: Book, result: (Boolean, String?) -> Unit) {
//    val databaseReference = FirebaseDatabase.getInstance().getReference("Books")
//    val bookId = databaseReference.push().key
//    val userId = FirebaseAuth.getInstance().currentUser?.uid
//
//    bookId?.let { bid ->
//        databaseReference.child(bid).setValue(book).addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                // Book added successfully, now update the user's points
//                userId?.let { uid ->
//                    val userRef = FirebaseDatabase.getInstance().getReference("users").child(uid)
//                    userRef.child("points").get().addOnSuccessListener { dataSnapshot ->
//                        val currentPoints = dataSnapshot.getValue(Int::class.java) ?: 0
//                        val newPoints = currentPoints + 1  // Increment points by 1 for each book added
//                        userRef.child("points").setValue(newPoints).addOnCompleteListener { userTask ->
//                            if (userTask.isSuccessful) {
//                                result(true, null) // Success: Book added and points updated
//                            } else {
//                                result(false, userTask.exception?.message ?: "Failed to update user points")
//                            }
//                        }
//                    }.addOnFailureListener {
//                        result(false, "Failed to fetch current user points")
//                    }
//                } ?: run {
//                    result(false, "User is not logged in")
//                }
//            } else {
//                result(false, task.exception?.message ?: "Unknown error while adding book")
//            }
//        }
//    } ?: run {
//        result(false, "Failed to generate a unique key for the book")
//    }
//}

//    fun addBookToFirebase(book: Book, bookBoxKey: String?, result: (Boolean, String?) -> Unit) {
//        val databaseReference = FirebaseDatabase.getInstance().getReference("Books")
//        val bookId = databaseReference.push().key
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//
//        bookId?.let { bid ->
//            if (bookBoxKey != null) {
//                book.bookBoxID = bookBoxKey
//            };
//            databaseReference.child(bid).setValue(book).addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    bookBoxKey?.let { key ->
//                        val bookBoxRef = FirebaseDatabase.getInstance().getReference("bookBoxes").child(key)
//                        bookBoxRef.child("bookIDs").get().addOnSuccessListener { dataSnapshot ->
//                            // Use GenericTypeIndicator for a list of Strings
//                            val typeIndicator = object : GenericTypeIndicator<List<String>>() {}
//                            val currentBookIDs: List<String> = dataSnapshot.getValue(typeIndicator) ?: mutableListOf()
//
//                            // Proceed to add the book ID and update Firebase as before
//                            val updatedBookIDs = currentBookIDs.toMutableList().apply {
//                                add(bid)
//                            }
//                            bookBoxRef.child("bookIDs").setValue(updatedBookIDs).addOnCompleteListener { bookBoxTask ->
//                                if (bookBoxTask.isSuccessful) {
//                                    updateUserPoints(userId, bid, result)
//                                } else {
//                                    result(false, bookBoxTask.exception?.message ?: "Failed to update book box")
//                                }
//                            }
//                        }.addOnFailureListener {
//                            result(false, "Failed to fetch current book IDs for book box")
//                        }
//                    } ?: run {
//                        updateUserPoints(userId, bid, result)
//                    }
//                } else {
//                    result(false, task.exception?.message ?: "Unknown error while adding book")
//                }
//            }
//        } ?: run {
//            result(false, "Failed to generate a unique key for the book")
//        }
//    }

    fun addBookToFirebase(context: Context, book: Book, bookBoxKey: String?, result: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Books")
            val bookId = databaseReference.push().key
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            bookId?.let { bid ->
                if (bookBoxKey != null) {
                    book.bookBoxID = bookBoxKey
                    val bookBoxRef = FirebaseDatabase.getInstance().getReference("bookBoxes").child(bookBoxKey)
                    try {
                        val dataSnapshot = bookBoxRef.get().await()
                        val latitude = dataSnapshot.child("latitude").getValue(Double::class.java)
                        val longitude = dataSnapshot.child("longitude").getValue(Double::class.java)
                        if (latitude != null && longitude != null) {
                            val address = withContext(Dispatchers.IO) {
                                convertCoordinatesToAddress(context, latitude, longitude)
                            }
                            if (address != null) {
                                book.address = address
                            }
                        }
                    } catch (e: Exception) {
                        result(false, "Failed to fetch book box details or convert coordinates.")
                        return@launch
                    }
                }
                // Proceed to save the book to Firebase, now including the address if available
                try {
                    databaseReference.child(bid).setValue(book).await()
                    updateUserPoints(userId, bid, result)
                } catch (e: Exception) {
                    result(false, "Failed to add book to Firebase.")
                }
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


    private fun updateUserPoints(userId: String?, bookId: String, result: (Boolean, String?) -> Unit) {
        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            userRef.child("points").get().addOnSuccessListener { dataSnapshot ->
                val currentPoints = dataSnapshot.getValue(Int::class.java) ?: 0
                val newPoints = currentPoints + 1  // Increment points by 1 for each book added
                userRef.child("points").setValue(newPoints).addOnCompleteListener { userTask ->
                    if (userTask.isSuccessful) {
                        result(true, null) // Success: Book added and points updated
                    } else {
                        result(false, userTask.exception?.message ?: "Failed to update user points")
                    }
                }
            }.addOnFailureListener {
                result(false, "Failed to fetch current user points")
            }
        } else {
            result(false, "User is not logged in")
        }
    }

    fun getBookLiveData(bookId: String): LiveData<Book> {
        val liveData = MutableLiveData<Book>()
        val bookRef = FirebaseDatabase.getInstance().getReference("Books").child(bookId)

        bookRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val book = snapshot.getValue(Book::class.java)
                book?.let {
                    liveData.value = it
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Log error
            }
        })
        return liveData
    }


}