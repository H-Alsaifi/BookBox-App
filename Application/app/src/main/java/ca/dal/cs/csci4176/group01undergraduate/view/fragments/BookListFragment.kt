package ca.dal.cs.csci4176.group01undergraduate.view.fragments
//importing the needed libraries
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import ca.dal.cs.csci4176.group01undergraduate.R
import ca.dal.cs.csci4176.group01undergraduate.model.Book
import ca.dal.cs.csci4176.group01undergraduate.view.adapters.BooksAdapter
import com.google.firebase.database.*


class BookListFragment : Fragment() {

    //a companion object with an argument key to get the book box id
    companion object {
        private const val ARG_BOOK_BOX_ID = "bookBoxId"

        /**
         * function to create a new instance of booklistfragment with the specified book box id
         * @param bookBoxId getting the book box id of each box
         */
        fun newInstance(bookBoxId: String): BookListFragment {
            val args = Bundle()
            args.putString(ARG_BOOK_BOX_ID, bookBoxId)
            val fragment = BookListFragment()
            fragment.arguments = args
            return fragment
        }
    }

    //initializing variables for bookadapter, database refernce and the bookslist
    private lateinit var booksAdapter: BooksAdapter
    private lateinit var databaseReference: DatabaseReference
    private val booksList = mutableListOf<Book>()

    //an oncreate to be viewed when inflater activated
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_book_list, container, false)

        //getting the book box id from fragment arguments
        val bookBoxId = arguments?.getString(ARG_BOOK_BOX_ID) ?: return view
        databaseReference = FirebaseDatabase.getInstance().getReference("Books")

        //setting a RecyclerView to display the list of books
        val booksRecyclerView: RecyclerView = view.findViewById(R.id.rvBooks)
        booksAdapter = BooksAdapter(booksList) { selectedBook ->
            // Navigate to BookDetailsFragment with selectedBook details
            val fragment = BookDetailsFragment.newInstance(selectedBook)
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container, fragment)
                addToBackStack(null)
                commit()
            }
        }
        //setting the adapter to the RecyclerView
        booksRecyclerView.adapter = booksAdapter
        //fetching the list of the books for each book box
        fetchBooksForBookBox(bookBoxId)
        return view
    }

    /**
     * a method to fetch the list of books in a specified book box
     * @param bookBoxId the id of a specified book box
     */
    private fun fetchBooksForBookBox(bookBoxId: String) {
        //reteriving the books from the database in a specified book box
        databaseReference.orderByChild("bookBoxID").equalTo(bookBoxId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                //checking the changes in the database
                override fun onDataChange(snapshot: DataSnapshot) {
                    booksList.clear()
                    //getting a snapshot data of to the list of books
                    snapshot.children.mapNotNullTo(booksList) { it.getValue(Book::class.java) }
                    //notify the change that have been made
                    booksAdapter.notifyDataSetChanged()
                }

                //a method to be called in case the or listener is cancelled
                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                }
            })
    }
}

