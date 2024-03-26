package ca.dal.cs.csci4176.group01undergraduate.addBookISBN

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator


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

    fun addBookToFirebase(book: Book, bookBoxKey: String?, result: (Boolean, String?) -> Unit) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Books")
        val bookId = databaseReference.push().key
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        bookId?.let { bid ->
            databaseReference.child(bid).setValue(book).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    bookBoxKey?.let { key ->
                        val bookBoxRef = FirebaseDatabase.getInstance().getReference("bookBoxes").child(key)
                        bookBoxRef.child("bookIDs").get().addOnSuccessListener { dataSnapshot ->
                            // Use GenericTypeIndicator for a list of Strings
                            val typeIndicator = object : GenericTypeIndicator<List<String>>() {}
                            val currentBookIDs: List<String> = dataSnapshot.getValue(typeIndicator) ?: mutableListOf()

                            // Proceed to add the book ID and update Firebase as before
                            val updatedBookIDs = currentBookIDs.toMutableList().apply {
                                add(bid)
                            }
                            bookBoxRef.child("bookIDs").setValue(updatedBookIDs).addOnCompleteListener { bookBoxTask ->
                                if (bookBoxTask.isSuccessful) {
                                    updateUserPoints(userId, bid, result)
                                } else {
                                    result(false, bookBoxTask.exception?.message ?: "Failed to update book box")
                                }
                            }
                        }.addOnFailureListener {
                            result(false, "Failed to fetch current book IDs for book box")
                        }
                    } ?: run {
                        updateUserPoints(userId, bid, result)
                    }
                } else {
                    result(false, task.exception?.message ?: "Unknown error while adding book")
                }
            }
        } ?: run {
            result(false, "Failed to generate a unique key for the book")
        }
    }

//    fun addBookToFirebase(book: Book, bookBoxKey: String?, result: (Boolean, String?) -> Unit) {
//        val databaseReference = FirebaseDatabase.getInstance().getReference("Books")
//        val bookId = databaseReference.push().key
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//
//        bookId?.let { bid ->
//            // Add the book to the "Books" collection
//            databaseReference.child(bid).setValue(book).addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    // Proceed only if bookBoxKey is provided
//                    bookBoxKey?.let { key ->
//                        val bookBoxRef = FirebaseDatabase.getInstance().getReference("bookBoxes").child(key)
//                        // Get or create the 'bookIDs' list under the selected book box
//                        bookBoxRef.child("bookIDs").get().addOnSuccessListener { dataSnapshot ->
//                            val currentBookIDs = dataSnapshot.getValue(List::class.java) as? MutableList<String> ?: mutableListOf()
//                            // Add the new book ID to the list
//                            currentBookIDs.add(bid)
//                            // Update the 'bookIDs' list in Firebase
////                            bookBoxRef.child("bookIDs").setValue(currentBookIDs).addOnCompleteListener { bookBoxTask ->
////                                if (bookBoxTask.isSuccessful) {
////                                    // Successfully updated the book box; now update the user's points
////                                    updateUserPoints(userId, bid, result)
////                                } else {
////                                    result(false, bookBoxTask.exception?.message ?: "Failed to update book box")
////                                }
////                            }
//                            bookBoxRef.child("bookIDs").get().addOnSuccessListener { dataSnapshot ->
//                                // Initialize an empty mutable list to hold the current book IDs
//                                val currentBookIDs = mutableListOf<String>()
//                                // Manually extract each book ID from the dataSnapshot
//                                dataSnapshot.children.forEach { childSnapshot ->
//                                    childSnapshot.getValue(String::class.java)?.let { id ->
//                                        currentBookIDs.add(id)
//                                    }
//                                }
//                                // Add the new book ID to the list
//                                currentBookIDs.add(bid)
//                                // Update the 'bookIDs' list in Firebase
//                                bookBoxRef.child("bookIDs").setValue(currentBookIDs).addOnCompleteListener { bookBoxTask ->
//                                    if (bookBoxTask.isSuccessful) {
//                                        // Successfully updated the book box; now update the user's points
//                                        updateUserPoints(userId, bid, result)
//                                    } else {
//                                        result(false, bookBoxTask.exception?.message ?: "Failed to update book box")
//                                    }
//                                }
//                            }.addOnFailureListener {
//                                result(false, "Failed to fetch current book IDs for book box")
//                            }
//
//                        }.addOnFailureListener {
//                            result(false, "Failed to fetch current book IDs for book box")
//                        }
//                    } ?: run {
//                        // No bookBoxKey is provided; directly update the user's points
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


}